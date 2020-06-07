package inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import feedback.Feedback;
import feedback.FeedbackHolder;
import feedback.FeedbackIdentifier;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import util.FeedbackType;
import util.InspectionPriority;
import util.PsiStmtType;
import util.Utils;

public final class ConstructorsFirstInspection extends AbstractBaseJavaLocalInspectionTool {

    private static final String INSPECTION_NAME = "constructors-first";

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
        return "Constructors should be first methods of a class.";
    }

    @Override
    @NonNls
    @NotNull
    public String getShortName() {
        return "ConstructorsFirst";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        InspectionPriority priority = Utils.getInspectionPriority(holder, INSPECTION_NAME);
        if (priority == InspectionPriority.NONE) {
            return new JavaElementVisitor() {
            };
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
            public void visitClass(PsiClass aClass) {
                super.visitClass(aClass);

                if (Utils.hasErrorsInFile(aClass)) {
                    return;
                }

                PsiMethod[] methods = aClass.getAllMethods();

                String filename = aClass.getContainingFile().getName();
                String className = aClass.getName();

                boolean prevIsCons = true;

                for (PsiMethod m : methods) {
                    int line = Utils.getLineNumber(m);
                    FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(m), INSPECTION_NAME, PsiStmtType.METHOD, line);

                    if (isClassConstructor(m, className) && !prevIsCons) {
                        Feedback feedback = new Feedback(line,
                                filename,
                                line + INSPECTION_NAME,
                                priority,
                                className,
                                FeedbackType.CONSTRUCTORS_FIRST);
                        feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
                        continue;
                    }

                    if (!isClassConstructor(m, className)) {
                        prevIsCons = false;
                    }

                    feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                }

            }

            private boolean isClassConstructor(PsiMethod method, String className) {
                return method.isConstructor() && method.getName().equals(className);
            }
        };
    }
}
