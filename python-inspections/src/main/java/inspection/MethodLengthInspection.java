package inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.python.psi.*;
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

public final class MethodLengthInspection extends AbstractBaseJavaLocalInspectionTool {

    private static final int MAX_METHOD_LENGTH = 20;

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
        return "Method length should not be longer than " + MAX_METHOD_LENGTH + " statements.";
    }

    @Override
    @NonNls
    @NotNull
    public String getShortName() {
        return "MethodLength";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        InspectionPriority priority = Utils.getInspectionPriority(holder, "method-length");
        if (priority == InspectionPriority.NONE) {
            return new JavaElementVisitor() {
            };
        }

        return new PyElementVisitor() {

            FeedbackHolder feedbackHolder = FeedbackHolder.getInstance();

            @Override
            public void visitPyFile(PyFile node) {
                super.visitPyFile(node);

                if (Utils.hasErrorsInFile(node)) {
                    return;
                }

                feedbackHolder.writeToFile();
            }

            @Override
            public void visitPyFunction(PyFunction node) {
                super.visitPyFunction(node);

                if (Utils.hasErrorsInFile(node)) {
                    return;
                }

                PyStatementList statList = node.getStatementList();
                PyStatement[] stats = statList.getStatements();

                String filename = node.getContainingFile().getName();
                int line = Utils.getLineNumber(node);
                FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(node), "method-length", PsiStmtType.METHOD, line);

                if (stats.length >= MAX_METHOD_LENGTH) {
                    Feedback feedback = new Feedback(line,
                            filename,
                            line + "-method-length",
                            priority,
                            filename,
                            FeedbackType.METHOD_LENGTH);
                    feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
                } else {
                    feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                }
            }

        };
    }
}
