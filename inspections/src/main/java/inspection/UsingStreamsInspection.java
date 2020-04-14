package inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import util.*;

//TODO: Extract commonality out (this, for loops, while loops)
public final class UsingStreamsInspection extends AbstractBaseJavaLocalInspectionTool {

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
        return "Check if using streams";
    }

    @Override
    @NonNls
    @NotNull
    public String getShortName() {
        return "UsingStreams";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        if (Utils.isInspectionOn(holder, "streams")) {
            return new StreamVisitor(holder, true);
        }

        if (Utils.isInspectionOn(holder, "no-streams")) {
            return new StreamVisitor(holder, false);
        }

        return new JavaElementVisitor() {};
    }

    private class StreamVisitor extends JavaElementVisitor {

        private final ProblemsHolder holder;
        private final FeedbackHolder feedbackHolder;
        private final boolean expectingStream;

        private boolean streamFound;

        public StreamVisitor(ProblemsHolder holder, boolean expectingStream) {
            this.holder = holder;
            this.expectingStream = expectingStream;

            feedbackHolder = FeedbackHolder.getInstance();
            streamFound = false;
        }

        @Override
        public void visitReferenceExpression(PsiReferenceExpression expression) {
            super.visitReferenceExpression(expression);

            for (PsiElement child : expression.getChildren()) {
                if (child instanceof PsiIdentifier) {
                    PsiIdentifier id = (PsiIdentifier) child;
                    //TODO: move this to utils
                    if (id.getText().equals("stream") || id.getText().equals("Stream") || id.getText().equals("IntStream") || id.getText().equals("ParallelStream")) {
                        streamFound = true;
                        return;
                    }
                }
            }
        }

        @Override
        public void visitJavaFile(PsiJavaFile file) {
            super.visitJavaFile(file);

            if (Utils.hasErrorsInFile(file)) {
                return;
            }

            if (expectingStream) {
                FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(file), "streams", PsiStmtType.FILE);

                if (!streamFound) {
                    Feedback feedback = new Feedback(-1, "Streams are not being used in this file.", file.getName());
                    feedbackHolder.addFeedback(holder.getProject(), file.getName(), feedbackId, feedback);
                } else {
                    feedbackHolder.fixFeedback(holder.getProject(), file.getName(), feedbackId);
                }

            } else {
                FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(file), "no-streams", PsiStmtType.FILE);

                if (streamFound) {
                    Feedback feedback = new Feedback(-1, "Streams are being used in this file.", file.getName());
                    feedbackHolder.addFeedback(holder.getProject(), file.getName(), feedbackId, feedback);
                } else {
                    feedbackHolder.fixFeedback(holder.getProject(), file.getName(), feedbackId);
                }
            }
        }
    }
}
