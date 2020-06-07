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

public final class UsingInheritanceInspection extends AbstractBaseJavaLocalInspectionTool {

    private static final String POS_INSPECTION_NAME = "inheritance";
    private static final String NEG_INSPECTION_NAME = "no-" + POS_INSPECTION_NAME;

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

        if (Utils.getInspectionPriority(holder, POS_INSPECTION_NAME) != InspectionPriority.NONE) {
            return new InheritanceVisitor(holder, true);
        }

        if (Utils.getInspectionPriority(holder, NEG_INSPECTION_NAME) != InspectionPriority.NONE) {
            return new InheritanceVisitor(holder, false);
        }

        return new JavaElementVisitor() {};
    }

    private static class InheritanceVisitor extends JavaElementVisitor {

        private final ProblemsHolder holder;
        private final FeedbackHolder feedbackHolder;
        private final boolean expectingInheritance;

        private boolean inheritanceFound;

        public InheritanceVisitor(ProblemsHolder holder, boolean expectingInheritance) {
            this.holder = holder;
            this.expectingInheritance = expectingInheritance;

            feedbackHolder = FeedbackHolder.getInstance();
            inheritanceFound = false;
        }

        @Override
        public void visitClass(PsiClass aClass) {
            super.visitClass(aClass);

            if (Utils.hasErrorsInFile(aClass)) {
                return;
            }

            PsiReferenceList extendsList = aClass.getExtendsList();

            if (extendsList != null) {
                if (Utils.containsExtends(extendsList.getText())) {
                    inheritanceFound = true;
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

            if (expectingInheritance) {
                if (!inheritanceFound) {
                    feedbackHolder.addTip(holder.getProject(), TipType.INHERITANCE, filename);
                } else {
                    feedbackHolder.fixTip(holder.getProject(), TipType.INHERITANCE, filename);
                }
            } else {
                if (inheritanceFound) {
                    feedbackHolder.addTip(holder.getProject(), TipType.NO_INHERITANCE, filename);
                } else {
                    feedbackHolder.fixTip(holder.getProject(), TipType.NO_INHERITANCE, filename);
                }
            }

            feedbackHolder.writeToFile();
        }
    }
}
