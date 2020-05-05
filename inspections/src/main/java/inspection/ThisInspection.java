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
                            int line = Utils.getLineNumber(stat);

                            FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(assExpr), "this-" + line, PsiStmtType.LEFT_THIS_EXPR, line);
                            PsiExpression leftExpr = assExpr.getLExpression();
                            inspectThisExpression(leftExpr, block, stat, filename, feedbackId);

                            feedbackId = new FeedbackIdentifier(Utils.getPointer(assExpr), "this-" + line, PsiStmtType.RIGHT_THIS_EXPR, line);
                            PsiExpression rightExpr = assExpr.getRExpression();
                            inspectThisExpression(rightExpr, block, stat, filename, feedbackId);
                        }
                    }
                }
            }

            private void inspectThisExpression(PsiExpression expr, PsiCodeBlock block, PsiStatement stat, String filename, FeedbackIdentifier feedbackId) {
                PsiThisExpression thisExpr = Utils.getThisExpression(expr);
                if (thisExpr != null) {
                    String thisVar = Utils.getThisId(thisExpr);
                    if (thisVar != null) {
                        if (!Utils.isInScope(thisVar, block)) {
                            int line = Utils.getLineNumber(stat);
                            Feedback feedback = new Feedback(line,
                                    "Unnecessary 'this' keyword",
                                    filename,
                                    line + "-this",
                                    priority,
                                    Utils.getClassName(stat),
                                    Utils.getMethodName(stat));
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
