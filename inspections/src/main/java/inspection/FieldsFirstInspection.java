package inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import util.Feedback;
import util.FeedbackHolder;
import util.Utils;

public final class FieldsFirstInspection extends AbstractBaseJavaLocalInspectionTool {

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
        return "Fields should be declared at the top of the file";
    }

    @Override
    @NonNls
    @NotNull
    public String getShortName() {
        return "FieldsFirst";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {

        if (!Utils.isInspectionOn(holder,"fields-first")) {
            return new JavaElementVisitor() {};
        }

        return new JavaElementVisitor() {

            FeedbackHolder feedbackHolder = FeedbackHolder.getInstance();

            @Override
            public void visitFile(@NotNull PsiFile file) {
                super.visitFile(file);

                feedbackHolder.writeToFile();
            }

            @Override
            public void visitField(PsiField field) {
                super.visitField(field);

                if (field instanceof PsiEnumConstant) {
                    return;
                }

                PsiElement prev = field.getPrevSibling();
                boolean registerProblem = false;

                prev = Utils.removeWhitespaceUntilPrev(prev);

                // Register problem if previous element is *not* another field declaration or '{'
                if (!(prev instanceof PsiField)) {
                    if (prev instanceof PsiJavaToken) {
                        PsiJavaToken prevToken = (PsiJavaToken) prev;
                        if (!(prevToken.getTokenType().equals(JavaTokenType.LBRACE))) {
                            registerProblem = true;
                        }
                    } else {
                        registerProblem = true;
                    }

                }

                String filename = field.getContainingFile().getName();
                String feedbackId = field.hashCode() + "fields-first";

                if (registerProblem) {
                    holder.registerProblem(field.getNameIdentifier(), "Declare fields at the top", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, new Feedback(Utils.getLineNumber(field), "Declare fields at the top", filename));
                } else {
                    feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                }
            }

        };
    }
}
