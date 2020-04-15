package inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import feedback.FeedbackHolder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import util.*;

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

        if (Utils.isInspectionOn(holder, "clone")) {
            return new CloneVisitor(holder);

        }

        return new JavaElementVisitor() {
        };
    }

    private static class CloneVisitor extends JavaElementVisitor {
        private final ProblemsHolder holder;
        private final FeedbackHolder feedbackHolder;

        // String representation
        private Map<PsiDeclarationStatement, String[]> declarationMap;
        private Map<PsiAssignmentExpression, String[]> assignmentMap;
        private Map<PsiIfStatement, String[]> ifStmtMap;
        private Map<PsiMethodCallExpression, String[]> methodCallMap;

        // Location
        private Map<PsiDeclarationStatement, Pair<Integer, Integer>> declarationLocationMap;
        private Map<PsiAssignmentExpression, Pair<Integer, Integer>> assignmentLocationMap;
        private Map<PsiIfStatement, Pair<Integer, Integer>> ifStmtLocationMap;
        private Map<PsiMethodCallExpression, Pair<Integer, Integer>> methodCallLocationMap;

        // Location of clones
        private Map<PsiDeclarationStatement, Set<Integer>> declarationCloneMap;
        private Map<PsiAssignmentExpression, Set<Integer>> assignmentCloneMap;
        private Map<PsiIfStatement, Set<Integer>> ifStmtCloneMap;
        private Map<PsiMethodCallExpression, Set<Integer>> methodCallCloneMap;

        public CloneVisitor(ProblemsHolder holder) {
            this.holder = holder;
            feedbackHolder = FeedbackHolder.getInstance();

            declarationMap = new HashMap<>();
            assignmentMap = new HashMap<>();
            ifStmtMap = new HashMap<>();
            methodCallMap = new HashMap<>();

            declarationLocationMap = new HashMap<>();
            assignmentLocationMap = new HashMap<>();
            ifStmtLocationMap = new HashMap<>();
            methodCallLocationMap = new HashMap<>();


            declarationCloneMap = new HashMap<>();
            assignmentCloneMap = new HashMap<>();
            ifStmtCloneMap = new HashMap<>();
            methodCallCloneMap = new HashMap<>();
        }

        @Override
        public void visitFile(@NotNull PsiFile file) {
            super.visitFile(file);

            if (Utils.hasErrorsInFile(file)) {
                return;
            }

            feedbackHolder.writeToFile();
        }

        @Override
        public void visitSwitchStatement(PsiSwitchStatement statement) {
            super.visitSwitchStatement(statement);

            if (Utils.hasErrorsInFile(statement)) {
                return;
            }

            PsiCodeBlock switchBody = statement.getBody();

            if (switchBody == null) {
                return;
            }

            PsiStatement[][] cases = CodeCloneUtils.getCaseBlocks(switchBody);

            // Iterate through all statements and add to corresponding LOCATION and STRING REP maps
            for (int i = 0; i < cases.length; i++) {
                for (int j = 0; j < cases[0].length; j++) {
                    PsiStatement stat = cases[i][j];
                    if (stat != null) {
                        // Calculate string representation and add to map
                        StatType type = addStatToMap(stat);

                        assert type != null : "Unexpected PsiStatement type found.";

                        // Add to location map
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

                        updateCloneSet(entryKey, otherEntryKey);
                        updateCloneSet(otherEntryKey, entryKey);

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
                        updateCloneSet(entryKey, otherEntryKey);
                        updateCloneSet(otherEntryKey, entryKey);
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

                        updateCloneSet(entryKey, otherEntryKey);
                        updateCloneSet(otherEntryKey, entryKey);

                        continue;
                    }

                    if (CodeCloneUtils.sameIfCondition(entryValue, otherEntryValue)) {
                        holder.registerProblem(entryKey.getCondition(),
                                "Same 'if' condition in switch case (" + entryKey.getText() + " " + otherEntryKey.getText() + ")",
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING);

                        update = true;
                    } else if (CodeCloneUtils.conditionChangeInLhs(entryValue, otherEntryValue)) {
                        holder.registerProblem(entryKey.getCondition(),
                                "Similar 'if' condition in switch case - differs by LHS (" + entryKey.getText() + " " + otherEntryKey.getText() + ")",
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING);

                        update = true;
                    } else if (CodeCloneUtils.conditionChangeInRhs(entryValue, otherEntryValue)) {
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
                        updateCloneSet(entryKey, otherEntryKey);
                        updateCloneSet(otherEntryKey, entryKey);
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

                        updateCloneSet(entryKey, otherEntryKey);
                        updateCloneSet(otherEntryKey, entryKey);
                        continue;
                    }

                    if (CodeCloneUtils.declChangeInVarName(entryValue, otherEntryValue)) {
                        holder.registerProblem(entryKey,
                                "Similar declaration statement in switch case - differs by variable name (" + entryKey.getText() + " " + otherEntryKey.getText() + ")",
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING);

                        update = true;
                    }

                    if (update) {
                        updateCloneSet(entryKey, otherEntryKey);
                        updateCloneSet(otherEntryKey, entryKey);
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

                        updateCloneSet(entryKey, otherEntryKey);
                        updateCloneSet(otherEntryKey, entryKey);

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

                Set<Integer> firstClones = getClones(cases[i][0]);

                if (firstClones.size() == 0) {
                    continue;
                }

                Set<Integer> intersection = new HashSet<>(firstClones);

                for (int j = 1; j < cases[0].length; j++) {
                    if (cases[i][j] == null) {
                        break;
                    }
                    Set<Integer> currClones = getClones(cases[i][j]);
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

//                    if (!intersection.isEmpty()) {
//                        holder.registerProblem(cases[i][0],
//                                "Clone 'case' block (clone of " + intersection + " )",
//                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
//                    }
            }

            if (transitiveClosureOfClones(clones, rangeOfCases)) {
                holder.registerProblem(statement,
                        "All cases in switch statement are clones of one another",
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING);

                //TODO: add feedback like this instead of to problems holder
//                    feedbackHolder.addFeedback(holder.getProject(), statement.getContainingFile().getName(), new Feedback(Utils.getLineNumber(statement), "All cases in switch are clones", statement.getContainingFile().getName()));

                //TODO call fixFeedback when the error has been fixed
            }
        }

        private boolean transitiveClosureOfClones(List<Set<Integer>> cases, List<Integer> aim) {
            if (cases.isEmpty()) {
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

        private void updateCloneSet(PsiAssignmentExpression entryKey, PsiAssignmentExpression otherEntryKey) {
            Set<Integer> existingClones = assignmentCloneMap.get(entryKey);
            int caseIndex = assignmentLocationMap.get(otherEntryKey).getFirst();
            if (existingClones == null) {
                existingClones = new HashSet<>();
            }
            existingClones.add(caseIndex);
            assignmentCloneMap.put(entryKey, existingClones);
        }

        private void updateCloneSet(PsiMethodCallExpression entryKey, PsiMethodCallExpression otherEntryKey) {
            Set<Integer> existingClones = methodCallCloneMap.get(entryKey);
            int caseIndex = methodCallLocationMap.get(otherEntryKey).getFirst();
            if (existingClones == null) {
                existingClones = new HashSet<>();
            }
            existingClones.add(caseIndex);
            methodCallCloneMap.put(entryKey, existingClones);
        }

        private void updateCloneSet(PsiDeclarationStatement entryKey, PsiDeclarationStatement otherEntryKey) {
            Set<Integer> existingClones = declarationCloneMap.get(entryKey);
            int caseIndex = declarationLocationMap.get(otherEntryKey).getFirst();
            if (existingClones == null) {
                existingClones = new HashSet<>();
            }
            existingClones.add(caseIndex);
            declarationCloneMap.put(entryKey, existingClones);
        }

        private void updateCloneSet(PsiIfStatement entryKey, PsiIfStatement otherEntryKey) {
            Set<Integer> existingClones = ifStmtCloneMap.get(entryKey);
            int caseIndex = ifStmtLocationMap.get(otherEntryKey).getFirst();
            if (existingClones == null) {
                existingClones = new HashSet<>();
            }
            existingClones.add(caseIndex);
            ifStmtCloneMap.put(entryKey, existingClones);
        }

        private StatType addStatToMap(PsiStatement stat) {
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

        private Set<Integer> getClones(PsiStatement stat) {
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

        enum StatType {
            ASSIGNMENT,
            METHOD_CALL,
            IF,
            DECLARATION
        }
    }
}

