package inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import feedback.FeedbackHolder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import util.TipType;
import util.Utils;

public final class UsingForLoopsInspection extends AbstractBaseJavaLocalInspectionTool {

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
        return "Check if using 'for' loops";
    }

    @Override
    @NonNls
    @NotNull
    public String getShortName() {
        return "UsingForLoops";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        if (Utils.isInspectionOn(holder, "for-loops")) {
            return new ForVisitor(holder, true);
        }

        if (Utils.isInspectionOn(holder, "no-for-loops")) {
            return new ForVisitor(holder, false);
        }

        return new JavaElementVisitor() {};
    }

    private static class ForVisitor extends JavaElementVisitor {

        private final ProblemsHolder holder;
        private final FeedbackHolder feedbackHolder;
        private final boolean expectingFor;

        private boolean forFound;

        public ForVisitor(ProblemsHolder holder, boolean expectingFor) {
            this.holder = holder;
            this.expectingFor = expectingFor;

            feedbackHolder = FeedbackHolder.getInstance();
            forFound = false;
        }

        @Override
        public void visitForeachStatement(PsiForeachStatement statement) {
            super.visitForeachStatement(statement);

            if (Utils.hasErrorsInFile(statement)) {
                return;
            }

            forFound = true;
        }

        @Override
        public void visitForStatement(PsiForStatement statement) {
            super.visitForStatement(statement);

            if (Utils.hasErrorsInFile(statement)) {
                return;
            }

            forFound = true;
        }

        @Override
        public void visitJavaFile(PsiJavaFile file) {
            super.visitJavaFile(file);

            if (Utils.hasErrorsInFile(file)) {
                return;
            }

            String filename = file.getName();

            if (expectingFor) {
                if (!forFound) {
                    feedbackHolder.addTip(holder.getProject(), TipType.FOR_LOOPS, filename);
                } else {
                    feedbackHolder.fixTip(holder.getProject(), TipType.FOR_LOOPS, filename);
                }
            } else {
                if (forFound) {
                    feedbackHolder.addTip(holder.getProject(), TipType.NO_FOR_LOOPS, filename);
                } else {
                    feedbackHolder.fixTip(holder.getProject(), TipType.NO_FOR_LOOPS, filename);
                }
            }

            feedbackHolder.writeToFile();
        }
    }
}
