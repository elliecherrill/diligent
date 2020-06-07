package inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import feedback.Feedback;
import feedback.FeedbackHolder;
import feedback.FeedbackIdentifier;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import util.FeedbackType;
import util.InspectionPriority;
import util.PsiStmtType;
import util.Utils;

public final class IfReturnElseInspection extends AbstractBaseJavaLocalInspectionTool {

    private static final String INSPECTION_NAME = "redundant-else";

    @Override
    @NotNull
    public String getDisplayName() {
        return "Redundant 'else' (due to return in 'if')";
    }

    public IfReturnElseInspection() {
    }

    @Override
    @NotNull
    public String getShortName() {
        return "IfReturnElse";
    }

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

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        InspectionPriority priority = Utils.getInspectionPriority(holder, INSPECTION_NAME);
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

                String filename = statement.getContainingFile().getName();
                int line = Utils.getLineNumber(statement);
                FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(statement), INSPECTION_NAME, PsiStmtType.IF, line);

                // Check if there is an else case
                if (statement.getElseBranch() != null) {
                    boolean endsWithReturn = false;

                    PsiStatement thenStmt = statement.getThenBranch();
                    if (thenStmt instanceof PsiBlockStatement) {
                        PsiBlockStatement blockStat = (PsiBlockStatement) thenStmt;
                        PsiCodeBlock thenBody = blockStat.getCodeBlock();
                        PsiStatement[] stmts = thenBody.getStatements();

                        for (PsiStatement stmt : stmts) {
                            if (stmt instanceof PsiReturnStatement) {
                                endsWithReturn = true;
                                break;
                            }
                        }
                    } else if (thenStmt instanceof PsiReturnStatement) {
                        endsWithReturn = true;
                    }

                    if (endsWithReturn) {
                        Feedback feedback = new Feedback(line,
                                filename,
                                line + INSPECTION_NAME,
                                priority,
                                Utils.getClassName(statement),
                                Utils.getMethodName(statement),
                                FeedbackType.REDUNDANT_ELSE);
                        feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
                    } else {
                        feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                    }
                } else {
                    feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                }
            }


        };
    }
}
