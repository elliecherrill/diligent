package inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import util.*;

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

        if (Utils.isInspectionOn(holder,"inheritance")) {
            return new InheritanceVisitor(holder, true);
        }

        if (Utils.isInspectionOn(holder,"no-inheritance")) {
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
                }
            }

            if (!inheritanceFound) {
                for (PsiElement child : aClass.getAllInnerClasses()) {
                    child.accept(this);
                }
            }
        }

        @Override
        public void visitJavaFile(PsiJavaFile file) {
            super.visitJavaFile(file);

            if (Utils.hasErrorsInFile(file)) {
                return;
            }

            if (expectingInheritance) {
                FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(file),"inheritance", PsiStmtType.FILE);

                if (!inheritanceFound) {
                    Feedback feedback = new Feedback(-1, "Inheritance is not being used in this file.", file.getName());
                    feedbackHolder.addFeedback(holder.getProject(), file.getName(), feedbackId, feedback);
                } else {
                    holder.registerProblem(file.getOriginalElement(), "Inheritance is being used in this file.", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    feedbackHolder.fixFeedback(holder.getProject(), file.getName(), feedbackId);
                }

            } else {
                FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(file),"no-inheritance", PsiStmtType.FILE);

                if (inheritanceFound) {
                    Feedback feedback = new Feedback(-1, "Inheritance is being used in this file.", file.getName());
                    feedbackHolder.addFeedback(holder.getProject(), file.getName(), feedbackId, feedback);
                } else {
                    feedbackHolder.fixFeedback(holder.getProject(), file.getName(), feedbackId);
                }
            }
        }
    }
}
