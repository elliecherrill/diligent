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
import util.*;

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

        if (!Utils.isInspectionOn(holder, "method-length")) {
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
            public void visitMethod(PsiMethod method) {
                super.visitMethod(method);

                if (Utils.hasErrorsInFile(method)) {
                    return;
                }

                PsiCodeBlock body = method.getBody();

                String filename = method.getContainingFile().getName();
                FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(method), "method-length", PsiStmtType.METHOD);

                if (body != null && body.getStatementCount() >= MAX_METHOD_LENGTH) {
                    Feedback feedback = new Feedback(Utils.getLineNumber(method), "Method length should not be longer than " + MAX_METHOD_LENGTH + " statements.", filename);
                    feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
                } else {
                    feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                }
            }
        };
    }
}
