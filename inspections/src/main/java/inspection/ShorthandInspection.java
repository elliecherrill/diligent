package inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import feedback.Feedback;
import feedback.FeedbackHolder;
import feedback.FeedbackIdentifier;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import util.*;

public final class ShorthandInspection extends AbstractBaseJavaLocalInspectionTool {

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
        return "Use shorthand assignment operators.";
    }

    @Override
    @NonNls
    @NotNull
    public String getShortName() {
        return "Shorthand";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {

        if (!Utils.isInspectionOn(holder, "shorthand-assignment")) {
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
            public void visitAssignmentExpression(PsiAssignmentExpression expression) {
                super.visitAssignmentExpression(expression);

                if (Utils.hasErrorsInFile(expression)) {
                    return;
                }


                IElementType op = expression.getOperationTokenType();
                if (!op.equals(JavaTokenType.EQ)) {
                    return;
                }

                PsiExpression lhsExpr = expression.getLExpression();
                PsiExpression rhsExpr = expression.getRExpression();

                if (rhsExpr == null) {
                    return;
                }

                boolean registerProblem = false;

                if (lhsExpr instanceof PsiReferenceExpression) {
                    PsiReferenceExpression refLhsExpr = (PsiReferenceExpression) lhsExpr;
                    String lhsVar = refLhsExpr.getText();

                    if (rhsExpr instanceof PsiBinaryExpression) {
                        PsiBinaryExpression binExpr = (PsiBinaryExpression) rhsExpr;
                        PsiExpression binLhsExpr = binExpr.getLOperand();
                        IElementType binOp = binExpr.getOperationTokenType();
                        PsiExpression binRhsExpr = binExpr.getROperand();

                        if (binLhsExpr instanceof PsiReferenceExpression) {
                            PsiReferenceExpression binRefExpr = (PsiReferenceExpression) binLhsExpr;
                            if (binRefExpr.getText().equals(lhsVar) && isOpAndAssignment(binOp)) {
                                registerProblem = true;
                            }
                        } else if (binRhsExpr instanceof PsiReferenceExpression) {
                            PsiReferenceExpression binRefExpr = (PsiReferenceExpression) binRhsExpr;
                            if (binRefExpr.getText().equals(lhsVar) && isOpAndAssignment(binOp)) {
                                registerProblem = true;

                            }
                        }
                    }
                }

                String filename = expression.getContainingFile().getName();
                FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(expression), "shorthand-assignment", PsiStmtType.BIN_EXPR);


                if (registerProblem) {
                    int line = Utils.getLineNumber(expression);
                    Feedback feedback = new Feedback(line, "Can use shorthand operation and assignment.", filename, line + "-shorthand-assignment");
                    feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
                } else {
                    feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                }

            }

            private boolean isOpAndAssignment(IElementType op) {
                return (op.equals(JavaTokenType.XOR)) || (op.equals(JavaTokenType.OR)) || (op.equals(JavaTokenType.AND))
                        || (op.equals(JavaTokenType.PERC)) || (op.equals(JavaTokenType.DIV)) || (op.equals(JavaTokenType.ASTERISK))
                        || (op.equals(JavaTokenType.MINUS)) || (op.equals(JavaTokenType.PLUS));
            }
        };
    }
}
