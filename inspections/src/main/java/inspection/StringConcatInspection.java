package inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
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
        InspectionPriority priority = Utils.getInspectionPriority(holder, "string-concat");
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

                if (Utils.hasErrorsInFile(statement)) {
                    return;
                }

                PsiStatement body = statement.getBody();
                Pair<List<PsiStatement>, List<PsiStatement>> stringConcats = getStringConcatInBody(body);

                if (stringConcats == null) {
                    return;
                }

                String filename = statement.getContainingFile().getName();
                reportStringConcatFeedback(stringConcats.getFirst(), stringConcats.getSecond(), filename);
            }

            @Override
            public void visitDoWhileStatement(PsiDoWhileStatement statement) {
                super.visitDoWhileStatement(statement);

                if (Utils.hasErrorsInFile(statement)) {
                    return;
                }

                PsiStatement body = statement.getBody();
                Pair<List<PsiStatement>, List<PsiStatement>> stringConcats = getStringConcatInBody(body);

                if (stringConcats == null) {
                    return;
                }

                String filename = statement.getContainingFile().getName();
                reportStringConcatFeedback(stringConcats.getFirst(), stringConcats.getSecond(), filename);
            }

            @Override
            public void visitForStatement(PsiForStatement statement) {
                super.visitForStatement(statement);

                if (Utils.hasErrorsInFile(statement)) {
                    return;
                }

                PsiStatement body = statement.getBody();
                Pair<List<PsiStatement>, List<PsiStatement>> stringConcats = getStringConcatInBody(body);

                if (stringConcats == null) {
                    return;
                }

                String filename = statement.getContainingFile().getName();
                reportStringConcatFeedback(stringConcats.getFirst(), stringConcats.getSecond(), filename);
            }

            @Override
            public void visitForeachStatement(PsiForeachStatement statement) {
                super.visitForeachStatement(statement);

                if (Utils.hasErrorsInFile(statement)) {
                    return;
                }

                PsiStatement body = statement.getBody();
                Pair<List<PsiStatement>, List<PsiStatement>> stringConcats = getStringConcatInBody(body);

                if (stringConcats == null) {
                    return;
                }

                String filename = statement.getContainingFile().getName();
                reportStringConcatFeedback(stringConcats.getFirst(), stringConcats.getSecond(), filename);
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

            private Pair<List<PsiStatement>, List<PsiStatement>> getStringConcatInBody(PsiStatement body) {
                if (!(body instanceof PsiBlockStatement)) {
                    return null;
                }

                List<PsiStatement> errorStats = new ArrayList<>();
                List<PsiStatement> fixStats = new ArrayList<>();

                PsiBlockStatement blockStat = (PsiBlockStatement) body;
                PsiCodeBlock codeBlock = blockStat.getCodeBlock();
                List<PsiStatement> stats = getAllStatements(codeBlock);

                for (PsiStatement stat : stats) {
                    if (!(stat instanceof PsiExpressionStatement)) {
                        fixStats.add(stat);
                        continue;
                    }

                    PsiExpressionStatement exprStat = (PsiExpressionStatement) stat;
                    PsiExpression expr = exprStat.getExpression();

                    if (!(expr instanceof PsiAssignmentExpression)) {
                        fixStats.add(stat);
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
                                errorStats.add(stat);
                                continue;
                            } else {
                                PsiExpression rhsExpr = assExpr.getRExpression();
                                if (rhsExpr instanceof PsiPolyadicExpression) {
                                    PsiPolyadicExpression polyRhsExpr = (PsiPolyadicExpression) rhsExpr;
                                    PsiExpression[] operands = polyRhsExpr.getOperands();

                                    if (polyRhsExpr.getOperationTokenType().equals(JavaTokenType.PLUS)) {
                                        for (PsiExpression op : operands) {
                                            if (op instanceof PsiReferenceExpression) {
                                                PsiReferenceExpression refExpr = (PsiReferenceExpression) op;
                                                boolean equalsLhs = refExpr.getText().equals(lhsVar);

                                                if (equalsLhs) {
                                                    errorStats.add(stat);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    fixStats.add(stat);
                }

                return new Pair<>(errorStats, fixStats);
            }

            private void reportStringConcatFeedback(List<PsiStatement> errorStatements, List<PsiStatement> fixStatements, String filename) {
                for (PsiStatement stat : errorStatements) {
                    int line = Utils.getLineNumber(stat);
                    Feedback feedback = new Feedback(line,
                            filename,
                            line + "-string-concat",
                            priority,
                            Utils.getClassName(stat),
                            Utils.getMethodName(stat),
                            FeedbackType.STRING_CONCAT);
                    FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(stat), "string-concat", PsiStmtType.STATEMENT, line);
                    feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
                    holder.registerProblem(stat, "string-concat", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }

                for (PsiStatement stat : fixStatements) {
                    int line = Utils.getLineNumber(stat);
                    FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(stat), "string-concat", PsiStmtType.STATEMENT, line);
                    feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                }
            }
        };
    }
}
