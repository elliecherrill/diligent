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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class StringConcatInspection extends AbstractBaseJavaLocalInspectionTool {

    private static final String INSPECTION_NAME = "string-concat";

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
        return "String concatenation in a loop.";
    }

    @Override
    @NonNls
    @NotNull
    public String getShortName() {
        return "StringConcat";
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
            public void visitWhileStatement(PsiWhileStatement statement) {
                super.visitWhileStatement(statement);

                detectStringConcatInLoop(statement);
            }

            @Override
            public void visitDoWhileStatement(PsiDoWhileStatement statement) {
                super.visitDoWhileStatement(statement);

                detectStringConcatInLoop(statement);
            }

            @Override
            public void visitForStatement(PsiForStatement statement) {
                super.visitForStatement(statement);

                detectStringConcatInLoop(statement);
            }

            @Override
            public void visitForeachStatement(PsiForeachStatement statement) {
                super.visitForeachStatement(statement);

                detectStringConcatInLoop(statement);
            }

            private List<PsiStatement> getAllStatements(PsiCodeBlock codeBlock) {
                PsiStatement[] statements = codeBlock.getStatements();
                List<PsiStatement> allStatements = new ArrayList<>(Arrays.asList(statements));

                for (PsiStatement stat : statements) {
                    if (stat instanceof PsiLoopStatement) {
                        PsiLoopStatement loopStat = (PsiLoopStatement) stat;
                        PsiStatement bodyStat = loopStat.getBody();
                        if (bodyStat instanceof PsiBlockStatement) {
                            PsiBlockStatement blockStat = (PsiBlockStatement) bodyStat;
                            allStatements.addAll(getAllStatements(blockStat.getCodeBlock()));
                        }
                    } else if (stat instanceof PsiIfStatement) {
                        PsiIfStatement ifStat = (PsiIfStatement) stat;
                        PsiStatement thenBranch = ifStat.getThenBranch();
                        if (thenBranch instanceof PsiBlockStatement) {
                            PsiBlockStatement thenBlockStat = (PsiBlockStatement) thenBranch;
                            allStatements.addAll(getAllStatements(thenBlockStat.getCodeBlock()));
                        }

                        PsiStatement elseBranch = ifStat.getElseBranch();
                        if (elseBranch instanceof PsiBlockStatement) {
                            PsiBlockStatement elseBlockStat = (PsiBlockStatement) elseBranch;
                            allStatements.addAll(getAllStatements(elseBlockStat.getCodeBlock()));
                        }
                    }
                }

                return allStatements;
            }

            private void detectStringConcatInLoop(PsiLoopStatement loopStat) {
                if (Utils.hasErrorsInFile(loopStat)) {
                    return;
                }

                PsiStatement body = loopStat.getBody();

                if (!(body instanceof PsiBlockStatement)) {
                    return;
                }

                String filename = body.getContainingFile().getName();

                PsiBlockStatement blockStat = (PsiBlockStatement) body;
                PsiCodeBlock codeBlock = blockStat.getCodeBlock();
                List<PsiStatement> stats = getAllStatements(codeBlock);

                for (PsiStatement stat : stats) {
                    if (!(stat instanceof PsiExpressionStatement)) {
                        fixFeedback(filename, stat);
                        continue;
                    }

                    PsiExpressionStatement exprStat = (PsiExpressionStatement) stat;
                    PsiExpression expr = exprStat.getExpression();

                    if (!(expr instanceof PsiAssignmentExpression)) {
                        fixFeedback(filename, stat);
                        continue;
                    }

                    PsiAssignmentExpression assExpr = (PsiAssignmentExpression) expr;

                    PsiExpression lhsExpr = assExpr.getLExpression();
                    if (lhsExpr instanceof PsiReferenceExpression) {
                        PsiReferenceExpression refLhsExpr = (PsiReferenceExpression) lhsExpr;

                        if (Utils.isString(refLhsExpr.getType())) {
                            // LHS is a string
                            String lhsVar = refLhsExpr.getText();

                            if (assExpr.getOperationTokenType().equals(JavaTokenType.PLUSEQ)) {
                                // Appending via .. += ..
                                addFeedback(filename, stat);
                                continue;
                            } else if (assExpr.getOperationTokenType().equals(JavaTokenType.EQ)) {
                                PsiExpression rhsExpr = assExpr.getRExpression();

                                if (rhsExpr instanceof PsiPolyadicExpression) {
                                    // Look for appending via .. = .. + ..
                                    if (inspectRhsPolyadic((PsiPolyadicExpression) rhsExpr, lhsVar)) {
                                        addFeedback(filename, stat);
                                        continue;
                                    }
                                } else if (rhsExpr instanceof PsiMethodCallExpression) {
                                    // Look for appending via .. = .. .concat(..)
                                    if (inspectRhsMethod((PsiMethodCallExpression) rhsExpr, lhsVar, false)) {
                                        addFeedback(filename, stat);
                                        continue;
                                    }
                                }
                            }
                        }
                    }

                    fixFeedback(filename, stat);
                }
            }

            private boolean inspectRhsPolyadic(PsiPolyadicExpression polyExpr, String lhsVar) {
                PsiExpression[] operands = polyExpr.getOperands();

                if (polyExpr.getOperationTokenType().equals(JavaTokenType.PLUS)) {
                    for (PsiExpression op : operands) {
                        if (op instanceof PsiReferenceExpression) {
                            PsiReferenceExpression refExpr = (PsiReferenceExpression) op;
                            if (refExpr.getText().equals(lhsVar)) {
                                return true;
                            }
                        }
                    }
                }

                return false;
            }

            private boolean inspectRhsMethod(PsiMethodCallExpression methodCallExpr, String lhsVar, boolean concatFound) {
                PsiReferenceExpression refExpr = methodCallExpr.getMethodExpression();
                boolean concatFoundNow = false;
                if (refExpr.getReferenceName() != null && refExpr.getReferenceName().equals("concat")) {
                    concatFoundNow = true;
                }

                if (refExpr.getQualifierExpression() != null) {
                    PsiExpression qualifierExpr = refExpr.getQualifierExpression();
                    if (qualifierExpr.getText().equals(lhsVar) && (concatFound || concatFoundNow)) {
                        return true;
                    }
                }

                PsiExpression qualifierExpr = refExpr.getQualifierExpression();
                if (qualifierExpr instanceof PsiMethodCallExpression) {
                    PsiMethodCallExpression innerMethodCallExpr = (PsiMethodCallExpression) qualifierExpr;
                    return inspectRhsMethod(innerMethodCallExpr, lhsVar, concatFound || concatFoundNow);
                }

                return false;
            }

            private void addFeedback(String filename, PsiStatement stat) {
                int line = Utils.getLineNumber(stat);
                Feedback feedback = new Feedback(line,
                        filename,
                        line + INSPECTION_NAME,
                        priority,
                        Utils.getClassName(stat),
                        Utils.getMethodName(stat),
                        FeedbackType.STRING_CONCAT);
                FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(stat), INSPECTION_NAME, PsiStmtType.STATEMENT, line);
                feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
            }

            private void fixFeedback(String filename, PsiStatement stat) {
                int line = Utils.getLineNumber(stat);
                FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(stat), INSPECTION_NAME, PsiStmtType.STATEMENT, line);
                feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
            }
        };
    }
}
