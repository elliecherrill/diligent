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

public final class UnusedVariablesInspection extends AbstractBaseJavaLocalInspectionTool {

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
        return "Unused variables can be replaced with '_'";
    }

    @Override
    @NonNls
    @NotNull
    public String getShortName() {
        return "UnusedVariables";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        InspectionPriority priority = Utils.getInspectionPriority(holder, "unused-var");
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

                String filename = node.getContainingFile().getName();
                int line = Utils.getLineNumber(node);
                FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(node), "unused-var", PsiStmtType.METHOD, line);

                PyExpression expr = node.getLeftHandSideExpression();

                if (expr instanceof PyTupleExpression) {
                    PyTupleExpression tupleExpr = (PyTupleExpression) expr;
                    PyExpression[] elems = tupleExpr.getElements();

                    for (PyExpression e : elems) {
                        if (isNotUsed(e, Utils.getFunction(e), node)) {
                            Feedback feedback = new Feedback(line,
                                    filename,
                                    line + "-unused-var",
                                    priority,
                                    filename,
                                    FeedbackType.UNUSED_VARIABLES);
                            feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
                        } else {
                            feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                        }
                    }
                }
            }

            private boolean isNotUsed(PyExpression expr, PyFunction function, PyAssignmentStatement currStat) {
                assert function != null : "Not contained within PyFunction";

                if (expr.getName() == null || expr.getName().equals("_")) {
                    return false;
                }

                PyStatementList statList = function.getStatementList();
                PyStatement[] stats = statList.getStatements();

                for (PyStatement s : stats) {
                    if (currStat.equals(s)) {
                        continue;
                    }
                    if (contains(s, expr)) {
                        return false;
                    }
                }

                return true;
            }

            private boolean contains(PyStatement stat, PyExpression expr) {
                if (stat instanceof PyAssignmentStatement) {
                    return assStatContains((PyAssignmentStatement) stat, expr);
                } else if (stat instanceof PyAugAssignmentStatement) {
                    return augAssStatContains((PyAugAssignmentStatement) stat, expr);
                } else if (stat instanceof PyExpressionStatement) {
                    return exprStatContains((PyExpressionStatement) stat, expr);
                } else if (stat instanceof PyIfStatement) {
                    return ifStatContains((PyIfStatement) stat, expr);
                } else if (stat instanceof PyForStatement) {
                    return forStatContains((PyForStatement) stat, expr);
                } else if (stat instanceof PyReturnStatement) {
                    return returnStatContains((PyReturnStatement) stat, expr);
                }

                return false;
            }

            private boolean assStatContains(PyAssignmentStatement stat, PyExpression expr) {
                PyExpression lhsExpr = stat.getLeftHandSideExpression();
                PyExpression rhsExpr = stat.getAssignedValue();

                return exprContains(lhsExpr, expr) || exprContains(rhsExpr, expr);
            }

            private boolean exprContains(PyExpression expr, PyExpression exprToFind) {
                if (expr == null) {
                    return false;
                }

                if (expr instanceof PyBinaryExpression) {
                    return binExprContains((PyBinaryExpression) expr, exprToFind);
                } else if (expr instanceof PyCallExpression) {
                    return callExprContains((PyCallExpression) expr, exprToFind);
                } else if (expr instanceof PyParenthesizedExpression) {
                    return parenExprContains((PyParenthesizedExpression) expr, exprToFind);
                } else if (expr instanceof PyReferenceExpression) {
                    return refExprContains((PyReferenceExpression) expr, exprToFind);
                }

                return false;
            }

            private boolean binExprContains(PyBinaryExpression binExpr, PyExpression expr) {
                return exprContains(binExpr.getLeftExpression(), expr) ||
                        exprContains(binExpr.getRightExpression(), expr);
            }

            private boolean callExprContains(PyCallExpression callExpr, PyExpression expr) {
                PyExpression[] args = callExpr.getArguments();

                for (PyExpression e : args) {
                    if (exprContains(e, expr)) {
                        return true;
                    }
                }

                return false;
            }

            private boolean parenExprContains(PyParenthesizedExpression parenExpr, PyExpression expr) {
                return exprContains(parenExpr.getContainedExpression(), expr);
            }

            private boolean refExprContains(PyReferenceExpression refExpr, PyExpression expr) {
                return refExpr.getName() != null && refExpr.getName().equals(expr.getName());
            }

            private boolean augAssStatContains(PyAugAssignmentStatement stat, PyExpression expr) {
                PyExpression lhsExpr = stat.getTarget();
                PyExpression rhsExpr = stat.getValue();

                return exprContains(lhsExpr, expr) || exprContains(rhsExpr, expr);
            }

            private boolean exprStatContains(PyExpressionStatement stat, PyExpression expr) {
                return exprContains(stat.getExpression(), expr);
            }

            private boolean ifStatContains(PyIfStatement stat, PyExpression expr) {
                PyIfPart ifPart = stat.getIfPart();

                if (ifPartContains(ifPart, expr)) {
                    return true;
                }

                PyIfPart[] elifParts = stat.getElifParts();

                for (PyIfPart part : elifParts) {
                    if (ifPartContains(part, expr)) {
                        return true;
                    }
                }

                return false;
            }

            private boolean ifPartContains(PyIfPart ifPart, PyExpression expr) {
                PyExpression condExpr = ifPart.getCondition();

                if (exprContains(condExpr, expr)) {
                    return true;
                }

                PyStatementList statList = ifPart.getStatementList();
                PyStatement[] stats = statList.getStatements();

                for (PyStatement s : stats) {
                    if (contains(s, expr)) {
                        return true;
                    }
                }

                return false;
            }

            private boolean forStatContains(PyForStatement stat, PyExpression expr) {
                PyForPart forPart = stat.getForPart();
                PyExpression source = forPart.getSource();

                if (exprContains(source, expr)) {
                    return true;
                }

                PyStatementList statList = forPart.getStatementList();
                PyStatement[] stats = statList.getStatements();

                for (PyStatement s : stats) {
                    if (contains(s, expr)) {
                        return true;
                    }
                }

                return false;
            }

            private boolean returnStatContains(PyReturnStatement stat, PyExpression expr) {
                return exprContains(stat.getExpression(), expr);
            }
        };
    }
}
