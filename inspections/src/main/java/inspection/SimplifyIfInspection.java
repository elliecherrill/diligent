package inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import feedback.Feedback;
import feedback.FeedbackHolder;
import feedback.FeedbackIdentifier;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import util.InspectionPriority;
import util.PsiStmtType;
import util.Utils;

public final class SimplifyIfInspection extends AbstractBaseJavaLocalInspectionTool {

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @Override
    @Nls
    @NotNull
    public String getGroupDisplayName() {
        return GroupNames.BUGS_GROUP_NAME;
    }

    @Override
    @Nls
    @NotNull
    public String getDisplayName() {
        return "If statement can be simplified.";
    }

    @Override
    @NonNls
    @NotNull
    public String getShortName() {
        return "SimplifyIf";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        InspectionPriority priority = Utils.getInspectionPriority(holder, "simplify-if");
        if (priority == InspectionPriority.NONE) {
            return new JavaElementVisitor() {
            };
        }

        return new JavaElementVisitor() {

            FeedbackHolder feedbackHolder = FeedbackHolder.getInstance();

            @Override
            public void visitFile(@NotNull PsiFile file) {
                super.visitFile(file);

                if (Utils.hasErrorsInFile(file)) {
                    return;
                }

                feedbackHolder.writeToFile();
            }

            @Override
            public void visitIfStatement(PsiIfStatement statement) {
                super.visitIfStatement(statement);

                if (Utils.hasErrorsInFile(statement)) {
                    return;
                }

                PsiStatement thenStat = statement.getThenBranch();
                PsiStatement elseStat = statement.getElseBranch();

                String filename = statement.getContainingFile().getName();
                FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(statement), "simplify-if", PsiStmtType.IF);

                if (thenStat == null || elseStat == null) {
                    feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                    return;
                }

                if (getBranchResult(thenStat) + getBranchResult(elseStat) == 1) {
                    int line = Utils.getLineNumber(statement);
                    Feedback feedback = new Feedback(line,
                            "'if' statement can be simplified",
                            filename,
                            line + "-simplify-if",
                            priority);
                    feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
                } else {
                    feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                }
            }

            private int getBranchResult(PsiStatement stat) {
                if (stat instanceof PsiBlockStatement) {
                    PsiBlockStatement blockStat = (PsiBlockStatement) stat;
                    PsiCodeBlock block = blockStat.getCodeBlock();

                    if (block.getStatementCount() == 1) {
                        PsiStatement s = block.getStatements()[0];

                        if (Utils.containsLiteral(s, JavaTokenType.TRUE_KEYWORD)) {
                            return 1;
                        }

                        if (Utils.containsLiteral(s, JavaTokenType.FALSE_KEYWORD)) {
                            return 0;
                        }
                    }
                }

                return -1;
            }
        };
    }
}
