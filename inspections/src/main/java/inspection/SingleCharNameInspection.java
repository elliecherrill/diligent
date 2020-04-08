package inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import util.Feedback;
import util.FeedbackHolder;
import util.Utils;

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

                feedbackHolder.writeToFile();
            }

            @Override
            public void visitField(PsiField field) {
                super.visitField(field);

                String feedbackId = field.hashCode() + "single-char-name";
                String filename = field.getContainingFile().getName();
                String projectPath = Utils.getProjectPath(field);

                if (field.getName().length() == 1) {
                    holder.registerProblem(field.getNameIdentifier(), "Field names should be more than one character in length.", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    feedbackHolder.addFeedback(projectPath, filename, feedbackId, new Feedback(Utils.getLineNumber(field), "Field names should be more than one character in length.", filename));
                } else {
                    feedbackHolder.fixFeedback(projectPath, filename,feedbackId);
                }
            }

            @Override
            public void visitDeclarationStatement(PsiDeclarationStatement statement) {
                super.visitDeclarationStatement(statement);

                PsiElement[] elements = statement.getDeclaredElements();
                for (PsiElement e : elements) {
                    // Local variables (not in for loops)
                    if (e instanceof PsiLocalVariable) {
                        PsiLocalVariable localElement = (PsiLocalVariable) e;

                        String feedbackId = localElement.hashCode() + "single-char-name";
                        String filename = statement.getContainingFile().getName();
                        String projectPath = Utils.getProjectPath(statement);

                        if (!(statement.getParent() instanceof PsiForeachStatement || statement.getParent() instanceof PsiForStatement)) {
                            if (localElement.getName().length() == 1) {
                                holder.registerProblem(localElement.getNameIdentifier(), "Variable names should be more than one character in length.", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                                feedbackHolder.addFeedback(projectPath, filename, feedbackId, new Feedback(Utils.getLineNumber(localElement), "Variable names should be more than one character in length.", filename));
                            } else {
                                feedbackHolder.fixFeedback(projectPath, filename,feedbackId);
                            }
                        }
                    }
                }
            }

            @Override
            public void visitMethod(PsiMethod method) {
                super.visitMethod(method);

                // Method names
                String feedbackId = method.hashCode() + "single-char-name";
                String filename = method.getContainingFile().getName();
                String projectPath = Utils.getProjectPath(method);

                if (method.getName().length() == 1) {
                    holder.registerProblem(method.getNameIdentifier(), "Method names should be more than one character in length.", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    feedbackHolder.addFeedback(projectPath, filename, feedbackId, new Feedback(Utils.getLineNumber(method), "Method names should be more than one character in length.", filename));
                } else {
                    feedbackHolder.fixFeedback(projectPath, filename, feedbackId);
                }

                // Parameter names
                PsiParameterList paramList = method.getParameterList();
                PsiParameter[] params = paramList.getParameters();
                for (PsiParameter p : params) {
                    feedbackId = p.hashCode() + "single-char-name";

                    if (p.getName().length() == 1) {
                        holder.registerProblem(p.getNameIdentifier(), "Parameter names should be more than one character in length.", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        feedbackHolder.addFeedback(projectPath, filename, feedbackId, new Feedback(Utils.getLineNumber(p), "Parameter names should be more than one character in length.", filename));
                    } else {
                        feedbackHolder.fixFeedback(projectPath, filename, feedbackId);
                    }
                }
            }
        };
    }
}
