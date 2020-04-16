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
import util.*;

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
        if (Utils.isInspectionOn(holder,"interfaces")) {
            return new InterfacesVisitor(holder, true);
        }

        if (Utils.isInspectionOn(holder,"no-interfaces")) {
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

            if (expectingInterfaces) {
                FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(file),"interfaces", PsiStmtType.FILE);

                if (!interfacesFound) {
                    Feedback feedback = new Feedback(-1, "Interfaces are not being used in this file.", file.getName(), "file-interfaces");
                    feedbackHolder.addFeedback(holder.getProject(), file.getName(), feedbackId, feedback);
                } else {
                    feedbackHolder.fixFeedback(holder.getProject(), file.getName(), feedbackId);
                }

            } else {
                FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(file),"no-interfaces", PsiStmtType.FILE);

                if (interfacesFound) {
                    Feedback feedback = new Feedback(-1, "Interfaces are being used in this file.", file.getName(), "file-no-interfaces");
                    feedbackHolder.addFeedback(holder.getProject(), file.getName(), feedbackId, feedback);
                } else {
                    feedbackHolder.fixFeedback(holder.getProject(), file.getName(), feedbackId);
                }
            }

            feedbackHolder.writeToFile();
        }
    }
}
