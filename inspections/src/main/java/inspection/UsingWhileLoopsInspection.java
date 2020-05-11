package inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import feedback.FeedbackHolder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import util.InspectionPriority;
import util.TipType;
import util.Utils;

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
        if (Utils.getInspectionPriority(holder, "while-loops") != InspectionPriority.NONE) {
            return new WhileVisitor(holder, true);
        }

        if (Utils.getInspectionPriority(holder, "no-while-loops") != InspectionPriority.NONE) {
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

            String filename = file.getName();

            if (expectingWhile) {
                if (!whileFound) {
                    feedbackHolder.addTip(holder.getProject(), TipType.WHILE_LOOPS, filename);
                } else {
                    feedbackHolder.fixTip(holder.getProject(), TipType.WHILE_LOOPS, filename);
                }
            } else {
                if (whileFound) {
                    feedbackHolder.addTip(holder.getProject(), TipType.NO_WHILE_LOOPS, filename);
                } else {
                    feedbackHolder.fixTip(holder.getProject(), TipType.NO_WHILE_LOOPS, filename);
                }
            }

            feedbackHolder.writeToFile();
        }
    }
}
