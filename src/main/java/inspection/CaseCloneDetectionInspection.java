package inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import util.CodeCloneUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class CaseCloneDetectionInspection extends AbstractBaseJavaLocalInspectionTool {
    @Override
    @NotNull
    public String getDisplayName() {
        return "Similar code in switch cases";
    }

    public CaseCloneDetectionInspection() {
    }

    @Override
    @NotNull
    public String getShortName() {
        return "CaseCloneDetection";
    }

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

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {

            @Override
            public void visitSwitchStatement(PsiSwitchStatement statement) {
                super.visitSwitchStatement(statement);

                PsiCodeBlock switchBody = statement.getBody();
                if (switchBody == null) {
                    return;
                }

                PsiStatement[][] cases = CodeCloneUtils.getCaseBlocks(switchBody);

                Map<PsiDeclarationStatement, String[]> declarationMap = new HashMap<>();
                Map<PsiAssignmentExpression, String[]> assignmentMap = new HashMap<>();
                Map<PsiIfStatement, String[]> ifStmtMap = new HashMap<>();
                Map<PsiMethodCallExpression, String[]> methodCallMap = new HashMap<>();

                for (PsiStatement[] c : cases) {
                    for (PsiStatement stat : c) {
                        if (stat != null) {
                            addStatToMap(stat, declarationMap, assignmentMap, ifStmtMap, methodCallMap);
                        }
                    }
                }

                // Compare all assignment expressions
                for (Map.Entry<PsiAssignmentExpression, String[]> assExpr : assignmentMap.entrySet()) {
                    for (Map.Entry<PsiAssignmentExpression, String[]> otherAssExpr : assignmentMap.entrySet()) {
                        PsiAssignmentExpression entryKey = assExpr.getKey();
                        PsiAssignmentExpression otherEntryKey = otherAssExpr.getKey();

                        String[] entryValue = assExpr.getValue();
                        String[] otherEntryValue = otherAssExpr.getValue();

                        if (entryKey.equals(otherEntryKey)) {
                            continue;
                        }

                        if (Arrays.equals(entryValue, otherEntryValue)) {
                            holder.registerProblem(entryKey,
                                    "Duplicate assignment expression in switch case (" + entryKey.getText() + " " + otherEntryKey.getText() + ")",
                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                            continue;
                        }

                        if (CodeCloneUtils.changeInLiteral(entryValue, otherEntryValue)) {
                            holder.registerProblem(entryKey,
                                    "Similar assignment expression in switch case - differs by RHS (" + entryKey.getText() + " " + otherEntryKey.getText() + ")",
                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        }

                        if (CodeCloneUtils.changeInOp(entryValue, otherEntryValue)) {
                            holder.registerProblem(otherEntryKey, "Similar assignment expression in switch case - differs by op (" + entryKey.getText() + " " + otherEntryKey.getText() + ")",
                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        }
                    }
                }

                // Compare all if statements
                for (Map.Entry<PsiIfStatement, String[]> ifStmt : ifStmtMap.entrySet()) {
                    for (Map.Entry<PsiIfStatement, String[]> otherIfStmt : ifStmtMap.entrySet()) {
                        PsiIfStatement entryKey = ifStmt.getKey();
                        PsiIfStatement otherEntryKey = otherIfStmt.getKey();

                        String[] entryValue = ifStmt.getValue();
                        String[] otherEntryValue = otherIfStmt.getValue();

                        if (entryKey.equals(otherEntryKey)) {
                            continue;
                        }

                        if (Arrays.equals(entryValue, otherEntryValue)) {
                            holder.registerProblem(entryKey,
                                    "Duplicate 'if' statement in switch case (" + entryKey.getText() + " " + otherEntryKey.getText() + ")",
                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                            continue;
                        }

                        if (CodeCloneUtils.sameIfCondition(entryValue, otherEntryValue)) {
                            holder.registerProblem(entryKey.getCondition(),
                                    "Same 'if' condition in switch case (" + entryKey.getText() + " " + otherEntryKey.getText() + ")",
                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        } else if (CodeCloneUtils.conditionChangeInLhs(entryValue,otherEntryValue)) {
                            holder.registerProblem(entryKey.getCondition(),
                                    "Similar 'if' condition in switch case - differs by LHS (" + entryKey.getText() + " " + otherEntryKey.getText() + ")",
                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        } else if (CodeCloneUtils.conditionChangeInRhs(entryValue,otherEntryValue)) {
                            holder.registerProblem(entryKey.getCondition(),
                                    "Similar 'if' condition in switch case - differs by RHS (" + entryKey.getText() + " " + otherEntryKey.getText() + ")",
                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        }

                        if (CodeCloneUtils.sameIfBody(entryValue, otherEntryValue)) {
                            holder.registerProblem(entryKey.getThenBranch(),
                                    "Same 'if' body (" + entryKey.getText() + " " + otherEntryKey.getText() + ")",
                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        }
                    }
                }

                // Compare all declarations
                for (Map.Entry<PsiDeclarationStatement, String[]> declStmt : declarationMap.entrySet()) {
                    for (Map.Entry<PsiDeclarationStatement, String[]> otherDeclStmt : declarationMap.entrySet()) {
                        PsiDeclarationStatement entryKey = declStmt.getKey();
                        PsiDeclarationStatement otherEntryKey = otherDeclStmt.getKey();

                        String[] entryValue = declStmt.getValue();
                        String[] otherEntryValue = otherDeclStmt.getValue();

                        if (entryKey.equals(otherEntryKey)) {
                            continue;
                        }

                        if (Arrays.equals(entryValue, otherEntryValue)) {
                            holder.registerProblem(entryKey,
                                    "Duplicate declaration statement in switch case (" + entryKey.getText() + " " + otherEntryKey.getText() + ")",
                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                            continue;
                        }

                        if (CodeCloneUtils.declChangeInVarName(entryValue,otherEntryValue)) {
                            holder.registerProblem(entryKey,
                                    "Similar declaration statement in switch case - differs by variable name (" + entryKey.getText() + " " + otherEntryKey.getText() + ")",
                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        }
                    }
                }

                // Compare all method calls
                for (Map.Entry<PsiMethodCallExpression, String[]> methodCall : methodCallMap.entrySet()) {
                    for (Map.Entry<PsiMethodCallExpression, String[]> otherMethodCall : methodCallMap.entrySet()) {
                        PsiMethodCallExpression entryKey = methodCall.getKey();
                        PsiMethodCallExpression otherEntryKey = otherMethodCall.getKey();

                        String[] entryValue = methodCall.getValue();
                        String[] otherEntryValue = otherMethodCall.getValue();

                        if (entryKey.equals(otherEntryKey)) {
                            continue;
                        }

                        if (Arrays.equals(entryValue, otherEntryValue)) {
                            holder.registerProblem(entryKey,
                                    "Duplicate method call in switch case (" + entryKey.getText() + " " + otherEntryKey.getText() + ")",
                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                            continue;
                        }
                    }
                }
            }
        };
    }

    private void addStatToMap(PsiStatement stat, Map<PsiDeclarationStatement, String[]> declarationMap,
                              Map<PsiAssignmentExpression, String[]> assignmentMap, Map<PsiIfStatement, String[]> ifStmtMap,
                              Map<PsiMethodCallExpression, String[]> methodCallMap) {
        //TODO: make this nicer - we find the type here but then do it inside getStatAsStringArray as well
        String[] stringRep = CodeCloneUtils.getStmtAsStringArray(stat);

        if (stat instanceof PsiExpressionStatement) {
            PsiExpression expr = ((PsiExpressionStatement) stat).getExpression();
            if (expr instanceof PsiAssignmentExpression) {
                PsiAssignmentExpression assExpr = (PsiAssignmentExpression) expr;
                assignmentMap.put(assExpr, stringRep);
                return;
            }

            if (expr instanceof PsiMethodCallExpression) {
                PsiMethodCallExpression callExpr = (PsiMethodCallExpression) expr;
                methodCallMap.put(callExpr, stringRep);
                return;
            }
        }

        if (stat instanceof PsiIfStatement) {
            PsiIfStatement ifStmt = (PsiIfStatement) stat;
            ifStmtMap.put(ifStmt, stringRep);
            return;
        }

        if (stat instanceof PsiDeclarationStatement) {
            PsiDeclarationStatement declStmt = (PsiDeclarationStatement) stat;
            declarationMap.put(declStmt, stringRep);
        }
    }
}