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
import util.InspectionPriority;
import util.PsiStmtType;
import util.Utils;

public final class StringComparisonInspection extends AbstractBaseJavaLocalInspectionTool {

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
        return "String comparison using == instead of .equals().";
    }

    @Override
    @NonNls
    @NotNull
    public String getShortName() {
        return "StringComparison";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        InspectionPriority priority = Utils.getInspectionPriority(holder, "string-comparison");
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
            public void visitBinaryExpression(PsiBinaryExpression expression) {
                super.visitBinaryExpression(expression);

                if (Utils.hasErrorsInFile(expression)) {
                    return;
                }


                String filename = expression.getContainingFile().getName();
                int line = Utils.getLineNumber(expression);
                FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(expression), "string-comparison", PsiStmtType.BIN_EXPR, line);

                if ((expression.getROperand() != null) && (expression.getLOperand().getType() != null) && (expression.getROperand().getType() != null)) {
                    IElementType op = expression.getOperationTokenType();
                    if (op.equals(JavaTokenType.EQEQ) || op.equals(JavaTokenType.NE)) {
                        if (Utils.isString(expression.getLOperand().getType()) && Utils.isString(expression.getROperand().getType())) {
                            String aimString = op.equals(JavaTokenType.EQEQ) ? ".equals()" : "! .equals()";
                            String opString = op.equals(JavaTokenType.EQEQ) ? "==" : "!=";
                            Feedback feedback = new Feedback(line,
                                    "String comparison should use '" + aimString + "', instead of '" + opString + "'.",
                                    filename,
                                    line + "-string-comparison",
                                    priority,
                                    Utils.getClassName(expression),
                                    Utils.getMethodName(expression));
                            feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
                            return;
                        }
                    }
                }

                feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
            }
        };
    }
}
