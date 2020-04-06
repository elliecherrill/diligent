package inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
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
                }

                // Parameter names
                PsiParameterList paramList = method.getParameterList();
                PsiParameter[] params = paramList.getParameters();
                for (PsiParameter p : params) {
                    if (!(Utils.isCamelCase(p.getName()))) {
                        holder.registerProblem(p.getNameIdentifier(), "Parameter names should be in camelCase.", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    }
                }
            }
        };
    }
}
