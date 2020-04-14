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

public final class CamelCaseInspection extends AbstractBaseJavaLocalInspectionTool {

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
        return "Method and variable names should be in camelCase.";
    }

    @Override
    @NonNls
    @NotNull
    public String getShortName() {
        return "CamelCase";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {

        if (!Utils.isInspectionOn(holder,"camelcase")) {
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

            @Override
            public void visitField(PsiField field) {
                super.visitField(field);

                if (Utils.hasErrorsInFile(field)) {
                    return;
                }

                // If immutable and has modifiers 'static final' then don't need to check for camelCase
                PsiModifierList modifierList = field.getModifierList();
                if (modifierList != null) {
                    if ((modifierList.hasModifierProperty(PsiModifier.FINAL)) && (modifierList.hasModifierProperty(PsiModifier.STATIC))) {
                        if (Utils.isImmutable(field.getType())) {
                            return;
                        }
                    }
                }

                String filename = field.getContainingFile().getName();
                FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(field),"camelcase", PsiStmtType.FIELD);

                if (!Utils.isCamelCase(field.getName())) {
                    Feedback feedback = new Feedback(Utils.getLineNumber(field), "Field names should be in camelCase.", filename);
                    feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
                } else {
                    feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                }
            }

            @Override
            public void visitDeclarationStatement(PsiDeclarationStatement statement) {
                super.visitDeclarationStatement(statement);

                if (Utils.hasErrorsInFile(statement)) {
                    return;
                }

                PsiElement[] elements = statement.getDeclaredElements();
                for (PsiElement e : elements) {
                    // Local variables
                    if (e instanceof PsiLocalVariable) {
                        PsiLocalVariable localElement = (PsiLocalVariable) e;

                        String filename = statement.getContainingFile().getName();
                        FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(localElement),"camelcase", PsiStmtType.LOCAL_VAR);

                        if (!(Utils.isCamelCase(localElement.getName()))) {
                            Feedback feedback = new Feedback(Utils.getLineNumber(statement), "Variable names should be in camelCase.", filename);
                            feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
                        } else {
                            feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                        }
                    }
                }
            }

            @Override
            public void visitMethod(PsiMethod method) {
                super.visitMethod(method);

                if (Utils.hasErrorsInFile(method)) {
                    return;
                }

                // Method names
                String filename = method.getContainingFile().getName();
                FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(method),"camelcase", PsiStmtType.METHOD);

                if (!method.isConstructor()) {
                    if (!(Utils.isCamelCase(method.getName()))) {
                        Feedback feedback = new Feedback(Utils.getLineNumber(method), "Method names should be in camelCase.", filename);
                        feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
                    } else {
                        feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                    }
                }

                // Parameter names
                PsiParameterList paramList = method.getParameterList();
                PsiParameter[] params = paramList.getParameters();

                for (PsiParameter p : params) {
                    feedbackId = new FeedbackIdentifier(Utils.getPointer(p),"camelcase", PsiStmtType.PARAMETER);
                    if (!(Utils.isCamelCase(p.getName()))) {
                        Feedback feedback = new Feedback(Utils.getLineNumber(p), "Parameter names should be in camelCase.", filename);
                        feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
                    } else {
                        feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                    }
                }
            }
        };
    }
}
