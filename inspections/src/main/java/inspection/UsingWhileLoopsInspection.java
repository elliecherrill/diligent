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

//TODO: Extract commonality out (this, for loops, streams)
public final class UsingWhileLoopsInspection extends AbstractBaseJavaLocalInspectionTool {

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
        return "Check if using 'while' loops";
    }

    @Override
    @NonNls
    @NotNull
    public String getShortName() {
        return "UsingWhileLoops";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        if (Utils.isInspectionOn(holder, "while-loops")) {
            return new WhileVisitor(holder, true);
        }

        if (Utils.isInspectionOn(holder, "no-while-loops")) {
            return new WhileVisitor(holder, false);
        }

        return new JavaElementVisitor() {};
    }

    private static class WhileVisitor extends JavaElementVisitor {

        private final ProblemsHolder holder;
        private final FeedbackHolder feedbackHolder;
        private final boolean expectingWhile;

        private boolean whileFound;

        public WhileVisitor(ProblemsHolder holder, boolean expectingWhile) {
            this.holder = holder;
            this.expectingWhile = expectingWhile;

            feedbackHolder = FeedbackHolder.getInstance();
            whileFound = false;
        }

        @Override
        public void visitWhileStatement(PsiWhileStatement statement) {
            super.visitWhileStatement(statement);

            if (Utils.hasErrorsInFile(statement)) {
                return;
            }

            whileFound = true;
        }

        @Override
        public void visitDoWhileStatement(PsiDoWhileStatement statement) {
            super.visitDoWhileStatement(statement);

            if (Utils.hasErrorsInFile(statement)) {
                return;
            }

            whileFound = true;
        }

        @Override
        public void visitJavaFile(PsiJavaFile file) {
            super.visitJavaFile(file);

            if (Utils.hasErrorsInFile(file)) {
                return;
            }

            if (expectingWhile) {
                FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(file), "while-loops", PsiStmtType.FILE);

                if (!whileFound) {
                    Feedback feedback = new Feedback(-1, "While loops are not being used in this file.", file.getName());
                    feedbackHolder.addFeedback(holder.getProject(), file.getName(), feedbackId, feedback);
                } else {
                    feedbackHolder.fixFeedback(holder.getProject(), file.getName(), feedbackId);
                }

            } else {
                FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(file), "no-while-loops", PsiStmtType.FILE);

                if (whileFound) {
                    Feedback feedback = new Feedback(-1, "While loops are being used in this file.", file.getName());
                    feedbackHolder.addFeedback(holder.getProject(), file.getName(), feedbackId, feedback);
                } else {
                    feedbackHolder.fixFeedback(holder.getProject(), file.getName(), feedbackId);
                }
            }
        }
    }
}
