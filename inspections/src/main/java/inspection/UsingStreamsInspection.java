package inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiReferenceExpression;
import feedback.FeedbackHolder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import util.TipType;
import util.Utils;

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

            if (Utils.hasErrorsInFile(expression)) {
                return;
            }

            if (streamFound) {
                return;
            }

            streamFound = Utils.isStream(expression);
        }

        @Override
        public void visitJavaFile(PsiJavaFile file) {
            super.visitJavaFile(file);

            if (Utils.hasErrorsInFile(file)) {
                return;
            }

            String filename = file.getName();

            if (expectingStream) {
                if (!streamFound) {
                    feedbackHolder.addTip(holder.getProject(), TipType.STREAMS, filename);
                } else {
                    feedbackHolder.fixTip(holder.getProject(), TipType.STREAMS, filename);
                }

            } else {
                if (streamFound) {
                    feedbackHolder.addTip(holder.getProject(), TipType.NO_STREAMS, filename);
                } else {
                    feedbackHolder.fixTip(holder.getProject(), TipType.NO_STREAMS, filename);
                }
            }

            feedbackHolder.writeToFile();
        }
    }
}
