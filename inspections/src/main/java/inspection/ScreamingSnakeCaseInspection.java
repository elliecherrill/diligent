package inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import util.*;

public final class ScreamingSnakeCaseInspection extends AbstractBaseJavaLocalInspectionTool {

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
        return "Static final constants should be in SCREAMING_SNAKE_CASE.";
    }

    @Override
    @NonNls
    @NotNull
    public String getShortName() {
        return "ScreamingSnakeCase";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {

        if (!Utils.isInspectionOn(holder,"screaming-snake-case")) {
            return new JavaElementVisitor() {};
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

            //TODO: Ask whether this should be for variables as well?
            @Override
            public void visitField(PsiField field) {
                super.visitField(field);

                if (Utils.hasErrorsInFile(field)) {
                    return;
                }

                if (field.getModifierList() != null) {
                    if (field.getModifierList().hasModifierProperty("final") && field.getModifierList().hasModifierProperty("static")) {
                        FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(field), "screaming-snake-case", PsiStmtType.FIELD);
                        String filename = field.getContainingFile().getName();

                        if (!Utils.isUpperSnakeCase(field.getName())) {
                            Feedback feedback = new Feedback(Utils.getLineNumber(field), "Constant field names should be in SCREAMING_SNAKE_CASE.", filename);
                            feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
                        } else {
                            feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                        }
                    }
                }
            }
        };
    }
}
