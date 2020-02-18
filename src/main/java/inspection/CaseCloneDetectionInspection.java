package inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import util.CodeCloneUtils;
import util.Pair;

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

                boolean[][] cloneDetected = new boolean[cases.length][cases[0].length];

                Map<PsiDeclarationStatement, String[]> declarationMap = new HashMap<>();
                Map<PsiAssignmentExpression, String[]> assignmentMap = new HashMap<>();
                Map<PsiIfStatement, String[]> ifStmtMap = new HashMap<>();
                Map<PsiMethodCallExpression, String[]> methodCallMap = new HashMap<>();

                Map<PsiDeclarationStatement, Pair<Integer, Integer>> declarationLocationMap = new HashMap<>();
                Map<PsiAssignmentExpression, Pair<Integer, Integer>> assignmentLocationMap = new HashMap<>();
                Map<PsiIfStatement, Pair<Integer, Integer>> ifStmtLocationMap = new HashMap<>();
                Map<PsiMethodCallExpression, Pair<Integer, Integer>> methodCallLocationMap = new HashMap<>();

                for (int i = 0; i < cases.length; i++) {
                    for (int j = 0; j < cases[0].length; j++) {
                        PsiStatement stat = cases[i][j];
                        if (stat != null) {
                            StatType type = addStatToMap(stat, declarationMap, assignmentMap, ifStmtMap, methodCallMap);

                            assert type != null: "Unexpected PsiStatement type found.";

                            Pair<Integer, Integer> location = new Pair<>(i, j);

                            if (type == StatType.ASSIGNMENT) {
                                PsiExpression expr = ((PsiExpressionStatement) stat).getExpression();
                                assignmentLocationMap.put((PsiAssignmentExpression) expr, location);
                            } else if (type == StatType.DECLARATION) {
                                declarationLocationMap.put((PsiDeclarationStatement) stat, location);
                            } else if (type == StatType.METHOD_CALL) {
                                PsiExpression expr = ((PsiExpressionStatement) stat).getExpression();
                                methodCallLocationMap.put((PsiMethodCallExpression) expr, location);
                            } else if (type == StatType.IF) {
                                ifStmtLocationMap.put((PsiIfStatement) stat, location);
                            }
                        } else {
                            cloneDetected[i][j] = true;
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

                        boolean update = false;

                        if (entryKey.equals(otherEntryKey)) {
                            continue;
                        }

                        if (Arrays.equals(entryValue, otherEntryValue)) {
                            holder.registerProblem(entryKey,
                                    "Duplicate assignment expression in switch case (" + entryKey.getText() + " " + otherEntryKey.getText() + ")",
                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING);

                            updateCloneDetected(cloneDetected, assignmentLocationMap, entryKey, otherEntryKey);
                            continue;
                        }

                        if (CodeCloneUtils.changeInLiteral(entryValue, otherEntryValue)) {
                            holder.registerProblem(entryKey,
                                    "Similar assignment expression in switch case - differs by RHS (" + entryKey.getText() + " " + otherEntryKey.getText() + ")",
                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING);

                            update = true;
                        }

                        if (CodeCloneUtils.changeInOp(entryValue, otherEntryValue)) {
                            holder.registerProblem(otherEntryKey, "Similar assignment expression in switch case - differs by op (" + entryKey.getText() + " " + otherEntryKey.getText() + ")",
                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING);

                            update = true;
                        }

                        if (update) {
                            updateCloneDetected(cloneDetected, assignmentLocationMap, entryKey, otherEntryKey);
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

                        boolean update = false;

                        if (entryKey.equals(otherEntryKey)) {
                            continue;
                        }

                        if (Arrays.equals(entryValue, otherEntryValue)) {
                            holder.registerProblem(entryKey,
                                    "Duplicate 'if' statement in switch case (" + entryKey.getText() + " " + otherEntryKey.getText() + ")",
                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING);

                            updateCloneDetected(cloneDetected, ifStmtLocationMap, entryKey, otherEntryKey);
                            continue;
                        }

                        if (CodeCloneUtils.sameIfCondition(entryValue, otherEntryValue)) {
                            holder.registerProblem(entryKey.getCondition(),
                                    "Same 'if' condition in switch case (" + entryKey.getText() + " " + otherEntryKey.getText() + ")",
                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING);

                            update = true;
                        } else if (CodeCloneUtils.conditionChangeInLhs(entryValue,otherEntryValue)) {
                            holder.registerProblem(entryKey.getCondition(),
                                    "Similar 'if' condition in switch case - differs by LHS (" + entryKey.getText() + " " + otherEntryKey.getText() + ")",
                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING);

                            update = true;
                        } else if (CodeCloneUtils.conditionChangeInRhs(entryValue,otherEntryValue)) {
                            holder.registerProblem(entryKey.getCondition(),
                                    "Similar 'if' condition in switch case - differs by RHS (" + entryKey.getText() + " " + otherEntryKey.getText() + ")",
                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING);

                            update = true;
                        }

                        if (CodeCloneUtils.sameIfBody(entryValue, otherEntryValue)) {
                            holder.registerProblem(entryKey.getThenBranch(),
                                    "Same 'if' body (" + entryKey.getText() + " " + otherEntryKey.getText() + ")",
                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING);

                            update = true;
                        }

                        if (update) {
                            updateCloneDetected(cloneDetected, ifStmtLocationMap, entryKey, otherEntryKey);
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

                        boolean update = false;

                        if (entryKey.equals(otherEntryKey)) {
                            continue;
                        }

                        if (Arrays.equals(entryValue, otherEntryValue)) {
                            holder.registerProblem(entryKey,
                                    "Duplicate declaration statement in switch case (" + entryKey.getText() + " " + otherEntryKey.getText() + ")",
                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING);

                            updateCloneDetected(cloneDetected, declarationLocationMap, entryKey, otherEntryKey);
                            continue;
                        }

                        if (CodeCloneUtils.declChangeInVarName(entryValue,otherEntryValue)) {
                            holder.registerProblem(entryKey,
                                    "Similar declaration statement in switch case - differs by variable name (" + entryKey.getText() + " " + otherEntryKey.getText() + ")",
                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING);

                            update = true;
                        }

                        if (update) {
                            updateCloneDetected(cloneDetected, declarationLocationMap, entryKey, otherEntryKey);
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

                            updateCloneDetected(cloneDetected, methodCallLocationMap, entryKey, otherEntryKey);
                            continue;
                        }
                    }
                }

                // If we have an entire case where duplicate / similar has been detected for every line
                for (int i = 0; i < cases.length; i++) {
                    if (CodeCloneUtils.isAllTrue(cloneDetected[i])) {
                        if (cases[i][0] != null) {
                            holder.registerProblem(cases[i][0],
                                    "Clone 'case' block",
                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        }
                    }
                }
            }
        };
    }

    //TODO: could we make this nicer?
    private void updateCloneDetected(boolean[][] cloneDetected, Map<PsiMethodCallExpression, Pair<Integer, Integer>> methodCallLocationMap,
                                     PsiMethodCallExpression entryKey, PsiMethodCallExpression otherEntryKey) {
        Pair<Integer, Integer> entryLocation = methodCallLocationMap.get(entryKey);
        cloneDetected[entryLocation.getFirst()][entryLocation.getSecond()] = true;

        Pair<Integer, Integer> otherEntryLocation = methodCallLocationMap.get(otherEntryKey);
        cloneDetected[otherEntryLocation.getFirst()][otherEntryLocation.getSecond()] = true;
    }

    private void updateCloneDetected(boolean[][] cloneDetected, Map<PsiDeclarationStatement, Pair<Integer, Integer>> declarationLocationMap,
                                     PsiDeclarationStatement entryKey, PsiDeclarationStatement otherEntryKey) {
        Pair<Integer, Integer> entryLocation = declarationLocationMap.get(entryKey);
        cloneDetected[entryLocation.getFirst()][entryLocation.getSecond()] = true;

        Pair<Integer, Integer> otherEntryLocation = declarationLocationMap.get(otherEntryKey);
        cloneDetected[otherEntryLocation.getFirst()][otherEntryLocation.getSecond()] = true;
    }

    private void updateCloneDetected(boolean[][] cloneDetected, Map<PsiIfStatement, Pair<Integer, Integer>> ifStmtLocationMap,
                                     PsiIfStatement entryKey, PsiIfStatement otherEntryKey) {
        Pair<Integer, Integer> entryLocation = ifStmtLocationMap.get(entryKey);
        cloneDetected[entryLocation.getFirst()][entryLocation.getSecond()] = true;

        Pair<Integer, Integer> otherEntryLocation = ifStmtLocationMap.get(otherEntryKey);
        cloneDetected[otherEntryLocation.getFirst()][otherEntryLocation.getSecond()] = true;
    }

    private void updateCloneDetected(boolean[][] cloneDetected, Map<PsiAssignmentExpression, Pair<Integer, Integer>> assignmentLocationMap,
                                     PsiAssignmentExpression entryKey, PsiAssignmentExpression otherEntryKey) {
        Pair<Integer, Integer> entryLocation = assignmentLocationMap.get(entryKey);
        cloneDetected[entryLocation.getFirst()][entryLocation.getSecond()] = true;

        Pair<Integer, Integer> otherEntryLocation = assignmentLocationMap.get(otherEntryKey);
        cloneDetected[otherEntryLocation.getFirst()][otherEntryLocation.getSecond()] = true;
    }


    private StatType addStatToMap(PsiStatement stat, Map<PsiDeclarationStatement, String[]> declarationMap,
                              Map<PsiAssignmentExpression, String[]> assignmentMap, Map<PsiIfStatement, String[]> ifStmtMap,
                              Map<PsiMethodCallExpression, String[]> methodCallMap) {
        //TODO: make this nicer - we find the type here but then do it inside getStatAsStringArray as well
        String[] stringRep = CodeCloneUtils.getStmtAsStringArray(stat);

        if (stat instanceof PsiExpressionStatement) {
            PsiExpression expr = ((PsiExpressionStatement) stat).getExpression();
            if (expr instanceof PsiAssignmentExpression) {
                PsiAssignmentExpression assExpr = (PsiAssignmentExpression) expr;
                assignmentMap.put(assExpr, stringRep);
                return StatType.ASSIGNMENT;
            }

            if (expr instanceof PsiMethodCallExpression) {
                PsiMethodCallExpression callExpr = (PsiMethodCallExpression) expr;
                methodCallMap.put(callExpr, stringRep);
                return StatType.METHOD_CALL;
            }
        }

        if (stat instanceof PsiIfStatement) {
            PsiIfStatement ifStmt = (PsiIfStatement) stat;
            ifStmtMap.put(ifStmt, stringRep);
            return StatType.IF;
        }

        if (stat instanceof PsiDeclarationStatement) {
            PsiDeclarationStatement declStmt = (PsiDeclarationStatement) stat;
            declarationMap.put(declStmt, stringRep);
            return StatType.DECLARATION;
        }

        return null;
    }
}

enum StatType {
    ASSIGNMENT,
    METHOD_CALL,
    IF,
    DECLARATION
}