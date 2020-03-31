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

public final class UsingInheritanceInspection extends AbstractBaseJavaLocalInspectionTool {

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
        return "Check if using inheritance";
    }

    @Override
    @NonNls
    @NotNull
    public String getShortName() {
        return "UsingInheritance";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new InheritanceVisitor(holder);
    }

    private static class InheritanceVisitor extends JavaElementVisitor {

        private final ProblemsHolder holder;


        public InheritanceVisitor(ProblemsHolder holder) {
            this.holder = holder;
        }

        // Check if inheritance is used anywhere in *this* file (not considering inner classes)
        // How would you do this project wide?
        // TODO: Finish this
        @Override
        public void visitJavaFile(PsiJavaFile file) {
            super.visitJavaFile(file);

            PsiElement[] children = file.getChildren();
            for (PsiElement child : children) {
                if (child instanceof PsiClass) {
                    PsiClass childClass = (PsiClass) child;
                    child.accept(this);
                    PsiReferenceList extendsList = childClass.getExtendsList();
                    if (Utils.containsExtends(extendsList.getText())) {
                        return;
                    }
                }
            }

            holder.registerProblem(file.getOriginalElement(), "Inheritance is not being used in this file.", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
        }
    }
}
