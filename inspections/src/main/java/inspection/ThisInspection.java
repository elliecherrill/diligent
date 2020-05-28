package inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import feedback.Feedback;
import feedback.FeedbackHolder;
import feedback.FeedbackIdentifier;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import util.FeedbackType;
import util.InspectionPriority;
import util.PsiStmtType;
import util.Utils;

public final class ThisInspection extends AbstractBaseJavaLocalInspectionTool {

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
        return "Overuse of this.";
    }

    @Override
    @NonNls
    @NotNull
    public String getShortName() {
        return "This";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        InspectionPriority priority = Utils.getInspectionPriority(holder, "this");
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
            public void visitThisExpression(PsiThisExpression expression) {
                super.visitThisExpression(expression);

                if (Utils.hasErrorsInFile(expression)) {
                    return;
                }

                String filename = expression.getContainingFile().getName();
                int line = Utils.getLineNumber(expression);
                FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(expression), "this-" + line, PsiStmtType.THIS_EXPR, line);
                inspectThisExpression(expression, filename, feedbackId);
            }

            private void inspectThisExpression(PsiThisExpression thisExpr, String filename, FeedbackIdentifier feedbackId) {
                String thisVar = Utils.getThisId(thisExpr);
                if (thisVar != null) {
                    if (!Utils.isInScope(thisVar, thisExpr)) {
                        int line = Utils.getLineNumber(thisExpr);
                        Feedback feedback = new Feedback(line,
                                filename,
                                line + "-this",
                                priority,
                                Utils.getClassName(thisExpr),
                                Utils.getMethodName(thisExpr),
                                FeedbackType.REDUNDANT_THIS);
                        feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
                        holder.registerProblem(thisExpr, "this", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);

                    } else {
                        feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                    }
                }
            }
        };
    }
}
