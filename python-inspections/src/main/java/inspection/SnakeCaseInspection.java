package inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import com.jetbrains.python.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import util.Utils;

public final class SnakeCaseInspection extends AbstractBaseJavaLocalInspectionTool {

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
        return "Method and variable names should be in snake_case.";
    }

    @Override
    @NonNls
    @NotNull
    public String getShortName() {
        return "SnakeCase";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new PyElementVisitor() {

            @Override
            public void visitPyAssignmentStatement(PyAssignmentStatement node) {
                super.visitPyAssignmentStatement(node);

                PyExpression expr = node.getLeftHandSideExpression();
                if (expr instanceof PyTargetExpression) {
                    PyTargetExpression targetExpr = (PyTargetExpression) expr;

                    if (targetExpr.getName() == null) {
                        return;
                    }

                    if (!Utils.isSnakeCase(targetExpr.getName())) {
                        holder.registerProblem(node, "Method and variable names should be in snake_case.", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    }
                }

            }

            @Override
            public void visitPyFunction(PyFunction node) {
                super.visitPyFunction(node);

                if (node.getName() == null) {
                    return;
                }

                if (!isIgnored(node.getName())) {
                    if (!Utils.isSnakeCase(node.getName())) {
                        holder.registerProblem(node, "Method and variable names should be in snake_case.", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    }
                }

                PyParameterList paramList = node.getParameterList();
                PyParameter[] params = paramList.getParameters();

                for (PyParameter p : params) {
                    if (p.getName() != null && !Utils.isSnakeCase(p.getName())) {
                        holder.registerProblem(p, "Method and variable names should be in snake_case.", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    }
                }


            }

            private boolean isIgnored(String funcName) {
                return funcName.startsWith("__") && funcName.endsWith("__");
            }

            //            @Override
//            public void visitDeclarationStatement(PsiDeclarationStatement statement) {
//                super.visitDeclarationStatement(statement);
//
//                if (Utils.hasErrorsInFile(statement)) {
//                    return;
//                }
//
//                PsiElement[] elements = statement.getDeclaredElements();
//                for (PsiElement e : elements) {
//                    // Local variables
//                    if (e instanceof PsiLocalVariable) {
//                        PsiLocalVariable localElement = (PsiLocalVariable) e;
//
//                        String filename = statement.getContainingFile().getName();
//                        int line = Utils.getLineNumber(statement);
//                        FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(localElement),"camelcase", PsiStmtType.LOCAL_VAR, line);
//
//                        if (!(Utils.isCamelCase(localElement.getName()))) {
//                            Feedback feedback = new Feedback(line, filename, line + "-" + localElement.getName() + "-camelcase", priority, Utils.getClassName(statement), Utils.getMethodName(statement), FeedbackType.CAMELCASE);
//                            feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
//                        } else {
//                            feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void visitMethod(PsiMethod method) {
//                super.visitMethod(method);
//
//                if (Utils.hasErrorsInFile(method)) {
//                    return;
//                }
//
//                // Method names
//                String filename = method.getContainingFile().getName();
//                String className = Utils.getClassName(method);
//                int line = Utils.getLineNumber(method);
//                FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(method),"camelcase", PsiStmtType.METHOD, line);
//
//                if (!method.isConstructor()) {
//                    if (!(Utils.isCamelCase(method.getName()))) {
//                        Feedback feedback = new Feedback(line, filename, line + "-camelcase", priority, className, FeedbackType.CAMELCASE);
//                        feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
//                    } else {
//                        feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
//                    }
//                }
//
//                // Parameter names
//                PsiParameterList paramList = method.getParameterList();
//                PsiParameter[] params = paramList.getParameters();
//
//                for (PsiParameter p : params) {
//                    line = Utils.getLineNumber(p);
//                    feedbackId = new FeedbackIdentifier(Utils.getPointer(p),"camelcase", PsiStmtType.PARAMETER, line);
//                    if (!(Utils.isCamelCase(p.getName()))) {
//                        Feedback feedback = new Feedback(line, filename, line + "-" + p.getName() + "-camelcase", priority, className, FeedbackType.CAMELCASE);
//                        feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
//                    } else {
//                        feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
//                    }
//                }
//            }
        };
    }
}
