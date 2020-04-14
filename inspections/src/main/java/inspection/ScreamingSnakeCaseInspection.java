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

        if (!Utils.isInspectionOn(holder, "screaming-snake-case")) {
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
            public void visitClass(PsiClass aClass) {
                super.visitClass(aClass);

                if (!aClass.isEnum()) {
                    return;
                }

                // Check all enum constants are in SCREAMING_SNAKE_CASE
                String filename = aClass.getContainingFile().getName();

                for (PsiField field : aClass.getAllFields()) {
                    if (field instanceof PsiEnumConstant) {
                        PsiEnumConstant enumConstant = (PsiEnumConstant) field;
                        FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(enumConstant), "screaming-snake-case", PsiStmtType.ENUM);

                        if (!Utils.isUpperSnakeCase(enumConstant.getName())) {
                            Feedback feedback = new Feedback(Utils.getLineNumber(enumConstant), "Enum constants should be in SCREAMING_SNAKE_CASE.", filename);
                            feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
                        } else {
                            feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                        }
                    }
                }

            }

            //TODO: Ask whether this should be for variables as well?
            //TODO: https://google.github.io/styleguide/javaguide.html#s5.2.4-constant-names
            // TODO: does it need to cover all cases in above^
            @Override
            public void visitField(PsiField field) {
                super.visitField(field);

                if (Utils.hasErrorsInFile(field)) {
                    return;
                }

                PsiModifierList modifierList = field.getModifierList();

                if (modifierList != null) {
                    if (!Utils.isImmutable(field.getType())) {
                        return;
                    }

                    if (modifierList.hasModifierProperty(PsiModifier.FINAL) && modifierList.hasModifierProperty(PsiModifier.STATIC)) {
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
