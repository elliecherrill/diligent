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

                if (Utils.hasErrorsInFile(file)) {
                    return;
                }

                feedbackHolder.writeToFile();
            }

            @Override
            public void visitField(PsiField field) {
                super.visitField(field);

                if (Utils.hasErrorsInFile(field)) {
                    return;
                }

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
                FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(field), "fields-first", PsiStmtType.FIELD);

                if (registerProblem) {
                    Feedback feedback = new Feedback(Utils.getLineNumber(field), "Declare fields at the top", filename);
                    feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
                } else {
                    feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                }
            }

        };
    }
}
