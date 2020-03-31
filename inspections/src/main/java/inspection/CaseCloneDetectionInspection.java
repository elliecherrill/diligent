package inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import util.CodeCloneUtils;
import util.Feedback;
import util.FeedbackHolder;
import util.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
            FeedbackHolder feedbackHolder = new FeedbackHolder();

            @Override
            public void visitFile(@NotNull PsiFile file) {
                super.visitFile(file);

                //TODO: move up to this level
                //feedbackHolder.writeToFile();
            }

            @Override
            public void visitSwitchStatement(PsiSwitchStatement statement) {
                super.visitSwitchStatement(statement);

                PsiCodeBlock switchBody = statement.getBody();

                if (switchBody == null) {
                    return;
                }

                PsiStatement[][] cases = CodeCloneUtils.getCaseBlocks(switchBody);

                // String representation
                Map<PsiDeclarationStatement, String[]> declarationMap = new HashMap<>();
                Map<PsiAssignmentExpression, String[]> assignmentMap = new HashMap<>();
                Map<PsiIfStatement, String[]> ifStmtMap = new HashMap<>();
                Map<PsiMethodCallExpression, String[]> methodCallMap = new HashMap<>();

                // Location
                Map<PsiDeclarationStatement, Pair<Integer, Integer>> declarationLocationMap = new HashMap<>();
                Map<PsiAssignmentExpression, Pair<Integer, Integer>> assignmentLocationMap = new HashMap<>();
                Map<PsiIfStatement, Pair<Integer, Integer>> ifStmtLocationMap = new HashMap<>();
                Map<PsiMethodCallExpression, Pair<Integer, Integer>> methodCallLocationMap = new HashMap<>();

                // Location of clones
                Map<PsiDeclarationStatement, Set<Integer>> declarationCloneMap = new HashMap<>();
                Map<PsiAssignmentExpression, Set<Integer>> assignmentCloneMap = new HashMap<>();
                Map<PsiIfStatement, Set<Integer>> ifStmtCloneMap = new HashMap<>();
                Map<PsiMethodCallExpression, Set<Integer>> methodCallCloneMap = new HashMap<>();

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

                            updateCloneSet(assignmentCloneMap, assignmentLocationMap, entryKey, otherEntryKey);
                            updateCloneSet(assignmentCloneMap, assignmentLocationMap, otherEntryKey, entryKey);

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
                            updateCloneSet(assignmentCloneMap, assignmentLocationMap, entryKey, otherEntryKey);
                            updateCloneSet(assignmentCloneMap, assignmentLocationMap, otherEntryKey, entryKey);
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

                            updateCloneSet(ifStmtCloneMap, ifStmtLocationMap, entryKey, otherEntryKey);
                            updateCloneSet(ifStmtCloneMap, ifStmtLocationMap, otherEntryKey, entryKey);

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
                            updateCloneSet(ifStmtCloneMap, ifStmtLocationMap, entryKey, otherEntryKey);
                            updateCloneSet(ifStmtCloneMap, ifStmtLocationMap, otherEntryKey, entryKey);
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

                            updateCloneSet(declarationCloneMap, declarationLocationMap, entryKey, otherEntryKey);
                            updateCloneSet(declarationCloneMap, declarationLocationMap, otherEntryKey, entryKey);
                            continue;
                        }

                        if (CodeCloneUtils.declChangeInVarName(entryValue,otherEntryValue)) {
                            holder.registerProblem(entryKey,
                                    "Similar declaration statement in switch case - differs by variable name (" + entryKey.getText() + " " + otherEntryKey.getText() + ")",
                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING);

                            update = true;
                        }

                        if (update) {
                            updateCloneSet(declarationCloneMap, declarationLocationMap, entryKey, otherEntryKey);
                            updateCloneSet(declarationCloneMap, declarationLocationMap, otherEntryKey, entryKey);
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

                            updateCloneSet(methodCallCloneMap, methodCallLocationMap, entryKey, otherEntryKey);
                            updateCloneSet(methodCallCloneMap, methodCallLocationMap, otherEntryKey, entryKey);

                            continue;
                        }
                    }
                }

                List<Integer> rangeOfCases = IntStream.range(0, cases.length - 1).boxed().collect(Collectors.toList());
                List<Set<Integer>> clones = new ArrayList<>(cases.length);

                // If we have an entire case where duplicate / similar has been detected for every line in another case
                for (int i = 0; i < cases.length; i++) {
                    // Empty case
                    //TODO: consider fallthrough
                    if (cases[i][0] == null) {
                        continue;
                    }

                    Set<Integer> firstClones = getClones(cases[i][0], declarationCloneMap, assignmentCloneMap, ifStmtCloneMap, methodCallCloneMap);

                    if (firstClones.size() == 0) {
                        continue;
                    }

                    Set<Integer> intersection = new HashSet<>(firstClones);

                    for (int j = 1; j < cases[0].length; j++) {
                        if (cases[i][j] == null) {
                            break;
                        }
                        Set<Integer> currClones = getClones(cases[i][j], declarationCloneMap, assignmentCloneMap, ifStmtCloneMap, methodCallCloneMap);
                        if (currClones == null) {
                            intersection.clear();
                            break;
                        } else {
                            intersection.retainAll(currClones);
                        }
                    }

                    // Add itself to its clones
                    intersection.add(i);

                    clones.add(intersection);

                    if (!intersection.isEmpty()) {
                        holder.registerProblem(cases[i][0],
                                "Clone 'case' block (clone of " + intersection + " )",
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    }
                }

                if (transitiveClosureOfClones(clones, rangeOfCases)) {
                    holder.registerProblem(statement,
                            "All cases in switch are clones",
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING);

                    //TODO: add feedback like this instead of to problems holder
                    //TODO: get line number (this is offset - not right)
                    feedbackHolder.addFeedback(new Feedback(statement.getTextOffset(), "All cases in switch are clones"));
                }
                // TODO: move up to visitFile method
                feedbackHolder.writeToFile();
            }

        };
    }

    private boolean transitiveClosureOfClones(List<Set<Integer>> cases, List<Integer> aim) {
        if (cases.isEmpty()){
            return false;
        }

        Set<Integer> currClones = new HashSet<>(cases.get(0));

        for (int i = 1; i < cases.size(); i++) {
            if (Collections.disjoint(currClones, cases.get(i))) {
                currClones = cases.get(i);
            } else {
                currClones.addAll(cases.get(i));
            }

            if (currClones.containsAll(aim)) {
                return true;
            }
        }

        return false;
    }

    private void updateCloneSet(Map<PsiAssignmentExpression, Set<Integer>> assignmentCloneMap,
                                Map<PsiAssignmentExpression, Pair<Integer, Integer>> assignmentLocationMap,
                                PsiAssignmentExpression entryKey, PsiAssignmentExpression otherEntryKey) {
        Set<Integer> existingClones = assignmentCloneMap.get(entryKey);
        int caseIndex = assignmentLocationMap.get(otherEntryKey).getFirst();
        if (existingClones == null) {
            existingClones = new HashSet<>();
        }
        existingClones.add(caseIndex);
        assignmentCloneMap.put(entryKey, existingClones);
    }

    private void updateCloneSet(Map<PsiMethodCallExpression, Set<Integer>> methodCallCloneMap,
                                Map<PsiMethodCallExpression, Pair<Integer, Integer>> methodCallLocationMap,
                                PsiMethodCallExpression entryKey, PsiMethodCallExpression otherEntryKey) {
        Set<Integer> existingClones = methodCallCloneMap.get(entryKey);
        int caseIndex = methodCallLocationMap.get(otherEntryKey).getFirst();
        if (existingClones == null) {
            existingClones = new HashSet<>();
        }
        existingClones.add(caseIndex);
        methodCallCloneMap.put(entryKey, existingClones);
    }

    private void updateCloneSet(Map<PsiDeclarationStatement, Set<Integer>> declarationCloneMap,
                                Map<PsiDeclarationStatement, Pair<Integer, Integer>> declarationLocationMap,
                                PsiDeclarationStatement entryKey, PsiDeclarationStatement otherEntryKey) {
        Set<Integer> existingClones = declarationCloneMap.get(entryKey);
        int caseIndex = declarationLocationMap.get(otherEntryKey).getFirst();
        if (existingClones == null) {
            existingClones = new HashSet<>();
        }
        existingClones.add(caseIndex);
        declarationCloneMap.put(entryKey, existingClones);
    }

    private void updateCloneSet(Map<PsiIfStatement, Set<Integer>> ifStmtCloneMap,
                                Map<PsiIfStatement, Pair<Integer, Integer>> ifStmtLocationMap,
                                PsiIfStatement entryKey, PsiIfStatement otherEntryKey) {
        Set<Integer> existingClones = ifStmtCloneMap.get(entryKey);
        int caseIndex = ifStmtLocationMap.get(otherEntryKey).getFirst();
        if (existingClones == null) {
            existingClones = new HashSet<>();
        }
        existingClones.add(caseIndex);
        ifStmtCloneMap.put(entryKey, existingClones);
    }

    //TODO: could we make this nicer?
//    private void updateCloneDetected(boolean[][] cloneDetected, Map<PsiMethodCallExpression, Pair<Integer, Integer>> methodCallLocationMap,
//                                     PsiMethodCallExpression entryKey, PsiMethodCallExpression otherEntryKey) {
//        Pair<Integer, Integer> entryLocation = methodCallLocationMap.get(entryKey);
//        cloneDetected[entryLocation.getFirst()][entryLocation.getSecond()] = true;
//
//        Pair<Integer, Integer> otherEntryLocation = methodCallLocationMap.get(otherEntryKey);
//        cloneDetected[otherEntryLocation.getFirst()][otherEntryLocation.getSecond()] = true;
//    }
//
//    private void updateCloneDetected(boolean[][] cloneDetected, Map<PsiDeclarationStatement, Pair<Integer, Integer>> declarationLocationMap,
//                                     PsiDeclarationStatement entryKey, PsiDeclarationStatement otherEntryKey) {
//        Pair<Integer, Integer> entryLocation = declarationLocationMap.get(entryKey);
//        cloneDetected[entryLocation.getFirst()][entryLocation.getSecond()] = true;
//
//        Pair<Integer, Integer> otherEntryLocation = declarationLocationMap.get(otherEntryKey);
//        cloneDetected[otherEntryLocation.getFirst()][otherEntryLocation.getSecond()] = true;
//    }
//
//    private void updateCloneDetected(boolean[][] cloneDetected, Map<PsiIfStatement, Pair<Integer, Integer>> ifStmtLocationMap,
//                                     PsiIfStatement entryKey, PsiIfStatement otherEntryKey) {
//        Pair<Integer, Integer> entryLocation = ifStmtLocationMap.get(entryKey);
//        cloneDetected[entryLocation.getFirst()][entryLocation.getSecond()] = true;
//
//        Pair<Integer, Integer> otherEntryLocation = ifStmtLocationMap.get(otherEntryKey);
//        cloneDetected[otherEntryLocation.getFirst()][otherEntryLocation.getSecond()] = true;
//    }
//
//    private void updateCloneDetected(boolean[][] cloneDetected, Map<PsiAssignmentExpression, Pair<Integer, Integer>> assignmentLocationMap,
//                                     PsiAssignmentExpression entryKey, PsiAssignmentExpression otherEntryKey) {
//        Pair<Integer, Integer> entryLocation = assignmentLocationMap.get(entryKey);
//        cloneDetected[entryLocation.getFirst()][entryLocation.getSecond()] = true;
//
//        Pair<Integer, Integer> otherEntryLocation = assignmentLocationMap.get(otherEntryKey);
//        cloneDetected[otherEntryLocation.getFirst()][otherEntryLocation.getSecond()] = true;
//    }


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

    private Set<Integer> getClones(PsiStatement stat, Map<PsiDeclarationStatement, Set<Integer>> declarationCloneMap,
                                  Map<PsiAssignmentExpression, Set<Integer>> assignmentCloneMap, Map<PsiIfStatement, Set<Integer>> ifStmtCloneMap,
                                  Map<PsiMethodCallExpression, Set<Integer>> methodCallCloneMap) {
        if (stat instanceof PsiExpressionStatement) {
            PsiExpression expr = ((PsiExpressionStatement) stat).getExpression();
            if (expr instanceof PsiAssignmentExpression) {
                PsiAssignmentExpression assExpr = (PsiAssignmentExpression) expr;
                return assignmentCloneMap.get(assExpr);
            }

            if (expr instanceof PsiMethodCallExpression) {
                PsiMethodCallExpression callExpr = (PsiMethodCallExpression) expr;
                return methodCallCloneMap.get(callExpr);
            }
        }

        if (stat instanceof PsiIfStatement) {
            PsiIfStatement ifStmt = (PsiIfStatement) stat;
            return ifStmtCloneMap.get(ifStmt);
        }

        if (stat instanceof PsiDeclarationStatement) {
            PsiDeclarationStatement declStmt = (PsiDeclarationStatement) stat;
            return declarationCloneMap.get(declStmt);
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