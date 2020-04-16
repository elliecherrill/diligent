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
import util.*;

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

            if (expectingFor) {
                FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(file), "for-loops", PsiStmtType.FILE);

                if (!forFound) {
                    Feedback feedback = new Feedback(-1, "For loops are not being used in this file.", file.getName(), "file-for-loops");
                    feedbackHolder.addFeedback(holder.getProject(), file.getName(), feedbackId, feedback);
                } else {
                    feedbackHolder.fixFeedback(holder.getProject(), file.getName(), feedbackId);
                }

            } else {
                FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(file), "no-for-loops", PsiStmtType.FILE);

                if (forFound) {
                    Feedback feedback = new Feedback(-1, "For loops are being used in this file.", file.getName(), "file-no-for-loops");
                    feedbackHolder.addFeedback(holder.getProject(), file.getName(), feedbackId, feedback);
                } else {
                    feedbackHolder.fixFeedback(holder.getProject(), file.getName(), feedbackId);
                }
            }

            feedbackHolder.writeToFile();
        }
    }
}
