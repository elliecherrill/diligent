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

                feedbackHolder.writeToFile();
            }

            @Override
            public void visitField(PsiField field) {
                super.visitField(field);

                if (field.getModifierList() != null) {
                    if (field.getModifierList().hasModifierProperty("final")) {
                        return;
                    }
                }

                String feedbackId = field.hashCode() + "camelcase";
                String filename = field.getContainingFile().getName();
                String projectPath = Utils.getProjectPath(field);

                if (!Utils.isCamelCase(field.getName())) {
                    holder.registerProblem(field.getNameIdentifier(), "Field names should be in camelCase.", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    feedbackHolder.addFeedback(projectPath, filename, feedbackId, new Feedback(Utils.getLineNumber(field), "Field names should be in camelCase.", filename));
                } else {
                    feedbackHolder.fixFeedback(projectPath, filename,feedbackId);
                }
            }

            @Override
            public void visitDeclarationStatement(PsiDeclarationStatement statement) {
                super.visitDeclarationStatement(statement);

                PsiElement[] elements = statement.getDeclaredElements();
                for (PsiElement e : elements) {
                    // Local variables
                    if (e instanceof PsiLocalVariable) {
                        PsiLocalVariable localElement = (PsiLocalVariable) e;

                        String feedbackId = localElement.hashCode() + "camelcase";
                        String filename = statement.getContainingFile().getName();
                        String projectPath = Utils.getProjectPath(statement);

                        if (!(Utils.isCamelCase(localElement.getName()))) {
                            holder.registerProblem(localElement.getNameIdentifier(), "Variable names should be in camelCase.", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                            feedbackHolder.addFeedback(projectPath, filename, feedbackId, new Feedback(Utils.getLineNumber(statement), "Variable names should be in camelCase.", filename));
                        } else {
                            feedbackHolder.fixFeedback(projectPath, filename, feedbackId);
                        }
                    }
                }
            }

            @Override
            public void visitMethod(PsiMethod method) {
                super.visitMethod(method);

                // Method names
                String feedbackId = method.hashCode() + "camelcase";
                String filename = method.getContainingFile().getName();
                String projectPath = Utils.getProjectPath(method);

                if (!method.isConstructor()) {
                    if (!(Utils.isCamelCase(method.getName()))) {
                        holder.registerProblem(method.getNameIdentifier(), "Method names should be in camelCase.", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        feedbackHolder.addFeedback(projectPath, filename, feedbackId, new Feedback(Utils.getLineNumber(method), "Method names should be in camelCase.", filename));
                    } else {
                        feedbackHolder.fixFeedback(projectPath, filename, feedbackId);
                    }
                }

                // Parameter names
                PsiParameterList paramList = method.getParameterList();
                PsiParameter[] params = paramList.getParameters();

                for (PsiParameter p : params) {
                    feedbackId = p.hashCode() + "camelcase";
                    if (!(Utils.isCamelCase(p.getName()))) {
                        holder.registerProblem(p.getNameIdentifier(), "Parameter names should be in camelCase.", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        feedbackHolder.addFeedback(projectPath, filename, feedbackId, new Feedback(Utils.getLineNumber(p), "Parameter names should be in camelCase.", filename));
                    } else {
                        feedbackHolder.fixFeedback(projectPath, filename, feedbackId);
                    }
                }
            }
        };
    }
}
