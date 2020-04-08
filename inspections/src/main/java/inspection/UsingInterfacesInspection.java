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

        if (!Utils.isInspectionOn(holder,"interfaces")) {
            return new JavaElementVisitor() {};
        }

        return new JavaElementVisitor() {

            FeedbackHolder feedbackHolder = FeedbackHolder.getInstance();

            // Check if interfaces are used anywhere in *this* file (not considering inner classes)
            // How would you do this project wide?
            @Override
            public void visitJavaFile(PsiJavaFile file) {
                super.visitJavaFile(file);

                // TODO: Finish this

                PsiElement[] children = file.getChildren();
                for (PsiElement child : children) {
                    if (child instanceof PsiClass) {
                        PsiClass childClass = (PsiClass) child;
                        PsiReferenceList implementsList = childClass.getImplementsList();
                        if (Utils.containsImplements(implementsList.getText())) {
                            return;
                        }
                    }
                }

                holder.registerProblem(file.getOriginalElement(), "Interfaces are not being used in this file.", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
//                feedbackHolder.addFeedback(holder.getProject(), file.getName(), new Feedback(0, "Interfaces is not being used in this file.", file.getName()));
//                feedbackHolder.writeToFile();
                //TODO add fixfeedback (once it's been fixed)

            }
        };
    }
}
