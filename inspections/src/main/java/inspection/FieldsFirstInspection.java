package inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import feedback.Feedback;
import feedback.FeedbackHolder;
import feedback.FeedbackIdentifier;
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
        InspectionPriority priority = Utils.getInspectionPriority(holder, "fields-first");
        if (priority == InspectionPriority.NONE) {
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
                        IElementType tokenType = prevToken.getTokenType();
                        if (!tokenType.equals(JavaTokenType.LBRACE) &&
                                !tokenType.equals(JavaTokenType.COMMA)) {
                            registerProblem = true;
                        }
                    } else {
                        registerProblem = true;
                    }

                }

                String filename = field.getContainingFile().getName();
                int line = Utils.getLineNumber(field);
                FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(field), "fields-first", PsiStmtType.FIELD, line);

                if (registerProblem) {
                    Feedback feedback = new Feedback(line,
                            filename,
                            line + "-" + field.getName() + "-fields-first",
                            priority,
                            Utils.getClassName(field),
                            FeedbackType.FIELDS_FIRST);
                    feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
                    holder.registerProblem(field, "fields-first", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);

                } else {
                    feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                }
            }

        };
    }
}
