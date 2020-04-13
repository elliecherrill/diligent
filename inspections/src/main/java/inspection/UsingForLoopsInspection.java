package inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import util.FeedbackHolder;
import util.Utils;

public final class UsingForLoopsInspection extends AbstractBaseJavaLocalInspectionTool {

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
        return "Check if using 'for' loops";
    }

    @Override
    @NonNls
    @NotNull
    public String getShortName() {
        return "UsingForLoops";
    }

    @NotNull
    @Override
    // TODO: Finish this
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {

        if (!Utils.isInspectionOn(holder,"for-loops")) {
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

            // Check if for loop is used anywhere in this class
            // How would you do this project wide?
            @Override
            public void visitClass(PsiClass aClass) {
                super.visitClass(aClass);

                if (Utils.hasErrorsInFile(aClass)) {
                    return;
                }

                boolean forLoopFound = false;

                PsiClass[] innerClasses = aClass.getAllInnerClasses();
                PsiMethod[] methods = aClass.getAllMethods();
                PsiMethod[] constructors = aClass.getConstructors();

                // Check all inner classes
                for (PsiClass innerClass : innerClasses) {
                }

                // Check all methods

            }
        };
    }
}
