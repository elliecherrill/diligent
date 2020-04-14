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

public final class SingleCharNameInspection extends AbstractBaseJavaLocalInspectionTool {

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
        return "Method and variable names should be more than one character in length.";
    }

    @Override
    @NonNls
    @NotNull
    public String getShortName() {
        return "SingleCharName";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {

        if (!Utils.isInspectionOn(holder,"single-char-name")) {
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

                FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(field), "single-char-name", PsiStmtType.FIELD);
                String filename = field.getContainingFile().getName();

                if (field.getName().length() == 1) {
                    Feedback feedback = new Feedback(Utils.getLineNumber(field), "Field names should be more than one character in length.", filename);
                    feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
                } else {
                    feedbackHolder.fixFeedback(holder.getProject(), filename,feedbackId);
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
                    // Local variables (not in for loops)
                    if (e instanceof PsiLocalVariable) {
                        PsiLocalVariable localElement = (PsiLocalVariable) e;

                        FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(localElement), "single-char-name", PsiStmtType.LOCAL_VAR);
                        String filename = statement.getContainingFile().getName();

                        if (!(statement.getParent() instanceof PsiForeachStatement || statement.getParent() instanceof PsiForStatement)) {
                            if (localElement.getName().length() == 1) {
                                Feedback feedback = new Feedback(Utils.getLineNumber(localElement), "Variable names should be more than one character in length.", filename);
                                feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
                            } else {
                                feedbackHolder.fixFeedback(holder.getProject(), filename,feedbackId);
                            }
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
                FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(method), "single-char-name", PsiStmtType.METHOD);
                String filename = method.getContainingFile().getName();

                if (method.getName().length() == 1) {
                    Feedback feedback = new Feedback(Utils.getLineNumber(method), "Method names should be more than one character in length.", filename);
                    feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
                } else {
                    feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                }

                // Parameter names
                PsiParameterList paramList = method.getParameterList();
                PsiParameter[] params = paramList.getParameters();
                for (PsiParameter p : params) {
                    feedbackId = new FeedbackIdentifier(Utils.getPointer(p), "single-char-name", PsiStmtType.PARAMETER);

                    if (p.getName().length() == 1) {
                        Feedback feedback = new Feedback(Utils.getLineNumber(p), "Parameter names should be more than one character in length.", filename);
                        feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
                    } else {
                        feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                    }
                }
            }
        };
    }
}
