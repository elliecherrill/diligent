package inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import feedback.Feedback;
import feedback.FeedbackHolder;
import feedback.FeedbackIdentifier;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import util.CodeCloneUtils;
import util.Pair;
import util.PsiStmtType;
import util.Utils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class CaseCloneInspection extends AbstractBaseJavaLocalInspectionTool {
    @Override
    @NotNull
    public String getDisplayName() {
        return "Similar code in switch cases";
    }

    public CaseCloneInspection() {
    }

    @Override
    @NotNull
    public String getShortName() {
        return "CaseClone";
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

        CloneVisitor(ProblemsHolder holder) {
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

            //TODO: I think can merge getCaseBlocks and iteration below to reduce complexity
            PsiStatement[][] cases = CodeCloneUtils.getCaseBlocks(switchBody);

            // Iterate through all statements and add to corresponding LOCATION and STRING REP maps
            for (int i = 0; i < cases.length; i++) {
                for (int j = 0; j < cases[0].length; j++) {
                    PsiStatement stat = cases[i][j];
                    if (stat != null) {
                        // Calculate string representation and add to map
                        StatType type = addStatToMap(stat);

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

            compareAssignments();
            compareIfStatements();
            compareDeclarations();
            compareMethodCalls();

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
            }

            String filename = statement.getContainingFile().getName();
            FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(statement), "clone", PsiStmtType.SWITCH);

            if (transitiveClosureOfClones(clones, rangeOfCases)) {
                int line = Utils.getLineNumber(statement);
                Feedback feedback = new Feedback(line, "All cases in switch statement are clones of one another.", filename, line + "-clone");
                feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
            } else {
                feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
            }
        }

        private void compareMethodCalls() {
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
                        updateCloneSet(entryKey, otherEntryKey);
                        updateCloneSet(otherEntryKey, entryKey);

                        continue;
                    }
                }
            }
        }

        private void compareDeclarations() {
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
                        updateCloneSet(entryKey, otherEntryKey);
                        updateCloneSet(otherEntryKey, entryKey);
                        continue;
                    }

                    if (CodeCloneUtils.declChangeInVarName(entryValue, otherEntryValue)) {
                        update = true;
                    }

                    if (update) {
                        updateCloneSet(entryKey, otherEntryKey);
                        updateCloneSet(otherEntryKey, entryKey);
                    }
                }
            }
        }

        private void compareIfStatements() {
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

                    // If statements are considered similar if
                    // 1. They are identical
                    // 2. They have identical conditions and similar bodies
                    // 3. They have identical bodies and similar conditions

                    //TODO: what about else cases?

                    if (Arrays.equals(entryValue, otherEntryValue)) {
                        updateCloneSet(entryKey, otherEntryKey);
                        updateCloneSet(otherEntryKey, entryKey);

                        continue;
                    }

                    //TODO: Finish this - I don't think sameIfCondition is working...
                    if (CodeCloneUtils.sameIfCondition(entryValue, otherEntryValue)) {
                        if (haveSimilarBodies(entryKey, otherEntryKey)) {
                            update = true;
                        }
                    } else if (CodeCloneUtils.sameIfBody(entryValue, otherEntryValue)) {
                        if (CodeCloneUtils.conditionChangeInLhs(entryValue, otherEntryValue)) {
                            update = true;
                        } else if (CodeCloneUtils.conditionChangeInRhs(entryValue, otherEntryValue)) {
                            update = true;
                        }
                    }

                    if (update) {
                        updateCloneSet(entryKey, otherEntryKey);
                        updateCloneSet(otherEntryKey, entryKey);
                    }
                }
            }
        }

        //TODO: finish this - use clone detection
        private boolean haveSimilarBodies(PsiIfStatement ifStmt, PsiIfStatement otherIfStmt) {
            PsiStatement body = ifStmt.getThenBranch();
            PsiStatement otherBody = otherIfStmt.getThenBranch();

            boolean emptyBody = false;
            boolean otherEmptyBody = false;

            if (body instanceof PsiBlockStatement) {
                PsiBlockStatement blockStmtBody = (PsiBlockStatement) body;
                PsiCodeBlock codeBlockBody = blockStmtBody.getCodeBlock();
                if (codeBlockBody.getStatements().length == 0) {
                    emptyBody = true;
                }
            }

            if (otherBody instanceof PsiBlockStatement) {
                PsiBlockStatement otherBlockStmtBody = (PsiBlockStatement) otherBody;
                PsiCodeBlock otherCodeBlockBody = otherBlockStmtBody.getCodeBlock();
                if (otherCodeBlockBody.getStatements().length == 0) {
                    otherEmptyBody = true;
                }
            }

            if (emptyBody != otherEmptyBody) {
                return false;
            }

            if (emptyBody) {
                return true;
            }

            return false;
        }

        private void compareAssignments() {
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
                        updateCloneSet(entryKey, otherEntryKey);
                        updateCloneSet(otherEntryKey, entryKey);

                        continue;
                    }

                    if (CodeCloneUtils.changeInLiteral(entryValue, otherEntryValue)) {
                        update = true;
                    }

                    if (CodeCloneUtils.changeInOp(entryValue, otherEntryValue)) {
                        update = true;
                    }

                    if (update) {
                        updateCloneSet(entryKey, otherEntryKey);
                        updateCloneSet(otherEntryKey, entryKey);
                    }
                }
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

            assert true : "Unknown statement type";
            return null;
        }

        private Set<Integer> getClones(PsiStatement stat) {
            if (stat instanceof PsiExpressionStatement) {
                PsiExpression expr = ((PsiExpressionStatement) stat).getExpression();
                if (expr instanceof PsiAssignmentExpression) {
                    PsiAssignmentExpression assExpr = (PsiAssignmentExpression) expr;
                    return assignmentCloneMap.getOrDefault(assExpr, Collections.emptySet());
                }

                if (expr instanceof PsiMethodCallExpression) {
                    PsiMethodCallExpression callExpr = (PsiMethodCallExpression) expr;
                    return methodCallCloneMap.getOrDefault(callExpr, Collections.emptySet());
                }
            }

            if (stat instanceof PsiIfStatement) {
                PsiIfStatement ifStmt = (PsiIfStatement) stat;
                return ifStmtCloneMap.getOrDefault(ifStmt, Collections.emptySet());
            }

            if (stat instanceof PsiDeclarationStatement) {
                PsiDeclarationStatement declStmt = (PsiDeclarationStatement) stat;
                return declarationCloneMap.getOrDefault(declStmt, Collections.emptySet());
            }

            assert true : "Unknown statement type";
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

