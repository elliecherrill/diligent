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

        if (!Utils.isInspectionOn(holder, "this")) {
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
            public void visitCodeBlock(PsiCodeBlock block) {
                super.visitCodeBlock(block);

                if (Utils.hasErrorsInFile(block)) {
                    return;
                }

                String filename = block.getContainingFile().getName();

                for (PsiStatement stat : block.getStatements()) {
                    if (stat instanceof PsiExpressionStatement) {
                        PsiExpressionStatement exprStat = (PsiExpressionStatement) stat;

                        PsiExpression expr = exprStat.getExpression();

                        if (expr instanceof PsiAssignmentExpression) {
                            PsiAssignmentExpression assExpr = (PsiAssignmentExpression) expr;

                            FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(assExpr), "this-" + Utils.getLineNumber(stat), PsiStmtType.LEFT_THIS_EXPR);
                            PsiExpression leftExpr = assExpr.getLExpression();
                            inspectThisExpression(leftExpr, block, stat, filename, feedbackId);

                            feedbackId = new FeedbackIdentifier(Utils.getPointer(assExpr), "this-" + Utils.getLineNumber(stat), PsiStmtType.RIGHT_THIS_EXPR);
                            PsiExpression rightExpr = assExpr.getRExpression();
                            inspectThisExpression(rightExpr, block, stat, filename, feedbackId);
                        }
                    }
                }
            }

            private void inspectThisExpression(PsiExpression expr, PsiCodeBlock block, PsiStatement stat, String filename, FeedbackIdentifier feedbackId,) {
                PsiThisExpression thisExpr = Utils.getThisExpression(expr);
                if (thisExpr != null) {
                    String thisVar = Utils.getThisId(thisExpr);
                    if (thisVar != null) {
                        if (!Utils.isInScope(thisVar, block)) {
                            Feedback feedback = new Feedback(Utils.getLineNumber(stat), "Unnecessary 'this' keyword", filename);
                            feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
                        } else {
                            feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                        }
                    }
                } else {
                    feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                }
            }
        };
    }
}
