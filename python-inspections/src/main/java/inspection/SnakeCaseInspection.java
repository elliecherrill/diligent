package inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.python.psi.*;
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
        InspectionPriority priority = Utils.getInspectionPriority(holder, "snake-case");
        if (priority == InspectionPriority.NONE) {
            return new JavaElementVisitor() {
            };
        }

        return new PyElementVisitor() {

            FeedbackHolder feedbackHolder = FeedbackHolder.getInstance();

            @Override
            public void visitPyFile(PyFile node) {
                super.visitPyFile(node);

                if (Utils.hasErrorsInFile(node)) {
                    return;
                }

                feedbackHolder.writeToFile();
            }

            @Override
            public void visitPyAssignmentStatement(PyAssignmentStatement node) {
                super.visitPyAssignmentStatement(node);

                if (Utils.hasErrorsInFile(node)) {
                    return;
                }

                PyExpression expr = node.getLeftHandSideExpression();
                if (expr instanceof PyTargetExpression) {
                    PyTargetExpression targetExpr = (PyTargetExpression) expr;

                    if (targetExpr.getName() == null) {
                        return;
                    }

                    String filename = node.getContainingFile().getName();
                    int line = Utils.getLineNumber(node);
                    FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(targetExpr), "snake-case", PsiStmtType.LOCAL_VAR, line);

                    if (!Utils.isSnakeCase(targetExpr.getName())) {
                        Feedback feedback = new Feedback(line, filename, line + "-" + targetExpr.getName() + "-snake-case", priority, filename, Utils.getFunctionName(node), FeedbackType.SNAKECASE);
                        feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
                    } else {
                        feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                    }

                } else if (expr instanceof PyTupleExpression) {
                    PyTupleExpression tupleExpr = (PyTupleExpression) expr;
                    PyExpression[] elements = tupleExpr.getElements();
                    for (PyExpression e : elements) {
                        if (e instanceof PyTargetExpression) {
                            PyTargetExpression targetExpr = (PyTargetExpression) e;

                            if (targetExpr.getName() == null) {
                                continue;
                            }

                            String filename = node.getContainingFile().getName();
                            int line = Utils.getLineNumber(node);
                            FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(targetExpr), "snake-case", PsiStmtType.LOCAL_VAR, line);

                            if (!Utils.isSnakeCase(targetExpr.getName())) {
                                Feedback feedback = new Feedback(line, filename, line + "-" + targetExpr.getName() + "-snake-case", priority, filename, Utils.getFunctionName(node), FeedbackType.SNAKECASE);
                                feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
                            } else {
                                feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                            }
                        }
                    }
                }

            }


            @Override
            public void visitPyFunction(PyFunction node) {
                super.visitPyFunction(node);

                if (node.getName() == null) {
                    return;
                }

                String filename = node.getContainingFile().getName();
                int line = Utils.getLineNumber(node);
                FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(node), "snake-case", PsiStmtType.METHOD, line);


                if (!Utils.isIgnored(node.getName())) {
                    if (!Utils.isSnakeCase(node.getName())) {
                        Feedback feedback = new Feedback(line, filename, line + "-snake-case", priority, filename, FeedbackType.SNAKECASE);
                        feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
                    } else {
                        feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                    }
                }

                PyParameterList paramList = node.getParameterList();
                PyParameter[] params = paramList.getParameters();

                for (PyParameter p : params) {
                    line = Utils.getLineNumber(p);
                    feedbackId = new FeedbackIdentifier(Utils.getPointer(p), "snake-case", PsiStmtType.PARAMETER, line);
                    if (p.getName() != null && !Utils.isSnakeCase(p.getName())) {
                        Feedback feedback = new Feedback(line, filename, line + "-" + p.getName() + "-snake-case", priority, filename, FeedbackType.SNAKECASE);
                        feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
                    } else {
                        feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                    }
                }
            }
        };
    }
}
