package inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import util.FeedbackHolder;
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

        if (!Utils.isInspectionOn(holder,"inheritance")) {
            return new JavaElementVisitor() {};
        }

        return new InheritanceVisitor(holder);
    }

    private static class InheritanceVisitor extends JavaElementVisitor {

        private final ProblemsHolder holder;
        FeedbackHolder feedbackHolder = FeedbackHolder.getInstance();

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

            //TODO: make sure names and checks match up (i.e. is inheritance being used or *not* being used?
            holder.registerProblem(file.getOriginalElement(), "Inheritance is not being used in this file.", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
//            feedbackHolder.addFeedback(holder.getProject(), file.getName(), new Feedback(0, "Inheritance is not being used in this file.", file.getName()));
//            feedbackHolder.writeToFile();
            //TODO add fixfeedback (once it's been fixed)


//            TODO
//            if (Utils.hasErrorsInFile(statement)) {
//                return;
//            }
        }
    }
}
