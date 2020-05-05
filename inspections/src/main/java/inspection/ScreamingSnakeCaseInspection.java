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
        InspectionPriority priority = Utils.getInspectionPriority(holder, "screaming-snake-case");
        if (priority == InspectionPriority.NONE) {
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

                int index = 0;
                for (PsiField field : aClass.getAllFields()) {
                    if (field instanceof PsiEnumConstant) {
                        PsiEnumConstant enumConstant = (PsiEnumConstant) field;
                        int line = Utils.getLineNumber(enumConstant);
                        FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(enumConstant), "screaming-snake-case", PsiStmtType.ENUM, line);

                        if (!Utils.isUpperSnakeCase(enumConstant.getName())) {
                            Feedback feedback = new Feedback(line,
                                    "Enum constants should be in SCREAMING_SNAKE_CASE.",
                                    filename,
                                    line + "-" + index + "-screaming-snake-case",
                                    priority);
                            feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
                        } else {
                            feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                        }

                        index++;
                    }
                }

            }

            @Override
            public void visitField(PsiField field) {
                super.visitField(field);

                if (Utils.hasErrorsInFile(field)) {
                    return;
                }

                PsiModifierList modifierList = field.getModifierList();

                if (modifierList != null) {
                    if (!Utils.isImmutable(field.getType(), field)) {
                        return;
                    }

                    //TODO: fix in missing else of this?
                    if (modifierList.hasModifierProperty(PsiModifier.FINAL) && modifierList.hasModifierProperty(PsiModifier.STATIC)) {
                        int line = Utils.getLineNumber(field);
                        FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(field), "screaming-snake-case", PsiStmtType.FIELD, line);
                        String filename = field.getContainingFile().getName();

                        if (!Utils.isUpperSnakeCase(field.getName())) {
                            Feedback feedback = new Feedback(line,
                                    "Constant field names should be in SCREAMING_SNAKE_CASE.",
                                    filename,
                                    line + "-" + field.getName() + "-screaming-snake-case",
                                    priority);
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
