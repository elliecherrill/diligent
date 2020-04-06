package inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import util.Feedback;
import util.FeedbackHolder;
import util.Utils;

public final class CamelCaseInspection extends AbstractBaseJavaLocalInspectionTool {

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
        return "Method and variable names should be in camelCase.";
    }

    @Override
    @NonNls
    @NotNull
    public String getShortName() {
        return "CamelCase";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {

            FeedbackHolder feedbackHolder = new FeedbackHolder();

            @Override
            public void visitFile(@NotNull PsiFile file) {
                super.visitFile(file);

                feedbackHolder.writeToFile();
            }

            @Override
            public void visitField(PsiField field) {
                super.visitField(field);

                if (field.getModifierList() != null) {
                    if (field.getModifierList().hasModifierProperty("final")) {
                        return;
                    }
                }

                if (!Utils.isCamelCase(field.getName())) {
                    holder.registerProblem(field.getNameIdentifier(), "Field names should be in camelCase.", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
//                  // TODO: get line number (rather than offset)
                    feedbackHolder.addFeedback(new Feedback(field.getTextOffset(), "Field names should be in camelCase."));
                }
            }

            @Override
            public void visitDeclarationStatement(PsiDeclarationStatement statement) {
                super.visitDeclarationStatement(statement);

                PsiElement[] elements = statement.getDeclaredElements();
                for (PsiElement e : elements) {
                    // Local variables
                    if (e instanceof PsiLocalVariable) {
                        PsiLocalVariable localElement = (PsiLocalVariable) e;
                        if (!(Utils.isCamelCase(localElement.getName()))) {
                            holder.registerProblem(localElement.getNameIdentifier(), "Variable names should be in camelCase.", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                            feedbackHolder.addFeedback(new Feedback(statement.getTextOffset(), "Variable names should be in camelCase."));
                        }
                    }
                }
            }

            @Override
            public void visitMethod(PsiMethod method) {
                super.visitMethod(method);

                // Method names
                if (!(Utils.isCamelCase(method.getName()))) {
                    holder.registerProblem(method.getNameIdentifier(), "Method names should be in camelCase.", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    feedbackHolder.addFeedback(new Feedback(method.getTextOffset(), "Method names should be in camelCase."));
                }

                // Parameter names
                PsiParameterList paramList = method.getParameterList();
                PsiParameter[] params = paramList.getParameters();
                for (PsiParameter p : params) {
                    if (!(Utils.isCamelCase(p.getName()))) {
                        holder.registerProblem(p.getNameIdentifier(), "Parameter names should be in camelCase.", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        feedbackHolder.addFeedback(new Feedback(p.getTextOffset(), "Parameter names should be in camelCase."));
                    }
                }
            }
        };
    }
}
