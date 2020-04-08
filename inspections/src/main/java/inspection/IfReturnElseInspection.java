package inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import util.Feedback;
import util.FeedbackHolder;
import util.Utils;

public final class IfReturnElseInspection extends AbstractBaseJavaLocalInspectionTool {
    @Override
    @NotNull
    public String getDisplayName() {
        return "Redundant 'else' (due to return in 'if')";
    }

    public IfReturnElseInspection() {}

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

        if (!Utils.isInspectionOn(holder,"redundant-else")) {
            return new JavaElementVisitor() {};
        }

        return new JavaElementVisitor() {

            FeedbackHolder feedbackHolder = FeedbackHolder.getInstance();

            @Override
            public void visitFile(@NotNull PsiFile file) {
                super.visitFile(file);

                feedbackHolder.writeToFile();
            }

            @Override
            public void visitIfStatement(PsiIfStatement statement) {
                super.visitIfStatement(statement);
                // Check if there is an else case
                if (statement.getElseBranch() != null) {
                    // Is the last line of the body a return statement?
                    PsiBlockStatement thenStmt = (PsiBlockStatement) statement.getThenBranch();
                    PsiCodeBlock thenBody = thenStmt.getCodeBlock();
                    PsiStatement[] stmts = thenBody.getStatements();

                    boolean endsWithReturn = false;
                    for (PsiStatement stmt : stmts) {
                        if (stmt instanceof PsiReturnStatement) {
                            endsWithReturn = true;
                        }
                    }

                    String filename = statement.getContainingFile().getName();
                    String feedbackId = statement.hashCode() + "redundant-else";

                    if (endsWithReturn && statement.getElseBranch() != null) {
                        holder.registerProblem(statement.getElseElement().getOriginalElement(),
                                "Unnecessary 'else' branch", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, new Feedback(Utils.getLineNumber(statement), "Unnecessary 'else' branch", filename));
                    } else {
                        feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                    }
                }
            }


        };
    }
}
