package inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import feedback.FeedbackHolder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import util.InspectionPriority;
import util.TipType;
import util.Utils;

public final class UsingInterfacesInspection extends AbstractBaseJavaLocalInspectionTool {

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
        return "Check if using interfaces";
    }

    @Override
    @NonNls
    @NotNull
    public String getShortName() {
        return "UsingInterfaces";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        if (Utils.getInspectionPriority(holder,"interfaces") != InspectionPriority.NONE) {
            return new InterfacesVisitor(holder, true);
        }

        if (Utils.getInspectionPriority(holder,"no-interfaces") != InspectionPriority.NONE) {
            return new InterfacesVisitor(holder, false);
        }

        return new JavaElementVisitor() {};
    }

    private static class InterfacesVisitor extends JavaElementVisitor {

        private final ProblemsHolder holder;
        private final FeedbackHolder feedbackHolder;
        private final boolean expectingInterfaces;

        private boolean interfacesFound;

        public InterfacesVisitor(ProblemsHolder holder, boolean expectingInterfaces) {
            this.holder = holder;
            this.expectingInterfaces = expectingInterfaces;

            feedbackHolder = FeedbackHolder.getInstance();
            interfacesFound = false;
        }

        @Override
        public void visitClass(PsiClass aClass) {
            super.visitClass(aClass);

            if (Utils.hasErrorsInFile(aClass)) {
                return;
            }

            if (aClass.isInterface()) {
                interfacesFound = true;
                return;
            }

            PsiReferenceList implementsList = aClass.getImplementsList();

            if (implementsList != null) {
                if (Utils.containsImplements(implementsList.getText())) {
                    interfacesFound = true;
                    return;
                }
            }

            for (PsiElement child : aClass.getAllInnerClasses()) {
                child.accept(this);
            }
        }

        @Override
        public void visitJavaFile(PsiJavaFile file) {
            super.visitJavaFile(file);

            if (Utils.hasErrorsInFile(file)) {
                return;
            }

            String filename = file.getName();

            if (expectingInterfaces) {
                if (!interfacesFound) {
                    feedbackHolder.addTip(holder.getProject(), TipType.INTERFACES, filename);
                } else {
                    feedbackHolder.fixTip(holder.getProject(), TipType.INTERFACES, filename);
                }
            } else {
                if (interfacesFound) {
                    feedbackHolder.addTip(holder.getProject(), TipType.NO_INTERFACES, filename);
                } else {
                    feedbackHolder.fixTip(holder.getProject(), TipType.NO_INTERFACES, filename);
                }
            }

            feedbackHolder.writeToFile();
        }
    }
}
