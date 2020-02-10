package inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public final class SingleCharNameInspection extends AbstractBaseJavaLocalInspectionTool {

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
        return "Method and variable names should be more than one character in length.";
    }

    @Override
    @NonNls
    @NotNull
    public String getShortName() {
        return "SingleCharName";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {

            @Override
            public void visitField(PsiField field) {
                super.visitField(field);

                if (field.getName().length() == 1) {
                    holder.registerProblem(field.getNameIdentifier(), "Field names should be more than one character in length.", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
            }

            @Override
            public void visitDeclarationStatement(PsiDeclarationStatement statement) {
                super.visitDeclarationStatement(statement);

                PsiElement[] elements = statement.getDeclaredElements();
                for (PsiElement e : elements) {
                    // Local variables (not in for loops)
                    if (e instanceof PsiLocalVariable) {
                        PsiLocalVariable localElement = (PsiLocalVariable) e;
                        if (localElement.getName().length() == 1) {
                            if (!(statement.getParent() instanceof PsiForeachStatement || statement.getParent() instanceof PsiForStatement)) {
                                holder.registerProblem(localElement.getNameIdentifier(), "Variable names should be more than one character in length.", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                            }
                        }
                    }
                }
            }

            @Override
            public void visitMethod(PsiMethod method) {
                super.visitMethod(method);

                // Method names
                if (method.getName().length() == 1) {
                    holder.registerProblem(method.getNameIdentifier(), "Method names should be more than one character in length.", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }

                // Parameter names
                PsiParameterList paramList = method.getParameterList();
                PsiParameter[] params = paramList.getParameters();
                for (PsiParameter p : params) {
                    if (p.getName().length() == 1) {
                        holder.registerProblem(p.getNameIdentifier(), "Parameter names should be more than one character in length.", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    }
                }
            }
        };
    }
}
