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
import util.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class CloneInspection extends AbstractBaseJavaLocalInspectionTool {
    @Override
    @NotNull
    public String getDisplayName() {
        return "Similar code";
    }

    public CloneInspection() {
    }

    @Override
    @NotNull
    public String getShortName() {
        return "Clone";
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

        CloneVisitor(ProblemsHolder holder) {
            this.holder = holder;
            feedbackHolder = FeedbackHolder.getInstance();
        }

        @Override
        public void visitFile(@NotNull PsiFile file) {
            super.visitFile(file);

            if (Utils.hasErrorsInFile(file)) {
                return;
            }

            feedbackHolder.writeToFile();
        }

//        @Override
//        public void visitClass(PsiClass aClass) {
//            super.visitClass(aClass);
//
//            if (Utils.hasErrorsInFile(aClass)) {
//                return;
//            }
//
//            PsiMethod[] methods = aClass.getMethods();
//
//            if (methods.length == 0) {
//                return;
//            }
//
//            PsiStatement[][] methodBodies = CodeCloneUtils.getMethodBodies(methods);
//
//            // String representation
//            Map<PsiDeclarationStatement, String[]> declarationMap = new HashMap<>();
//            Map<PsiAssignmentExpression, String[]> assignmentMap = new HashMap<>();
//            Map<PsiIfStatement, String[]> ifStmtMap = new HashMap<>();
//            Map<PsiMethodCallExpression, String[]> methodCallMap = new HashMap<>();
//
//            // Location
//            Map<PsiDeclarationStatement, Pair<Integer, Integer>> declarationLocationMap = new HashMap<>();
//            Map<PsiAssignmentExpression, Pair<Integer, Integer>> assignmentLocationMap = new HashMap<>();
//            Map<PsiIfStatement, Pair<Integer, Integer>> ifStmtLocationMap = new HashMap<>();
//            Map<PsiMethodCallExpression, Pair<Integer, Integer>> methodCallLocationMap = new HashMap<>();
//
//            // Location of clones
//            Map<PsiDeclarationStatement, Set<Integer>> declarationCloneMap = new HashMap<>();
//            Map<PsiAssignmentExpression, Set<Integer>> assignmentCloneMap = new HashMap<>();
//            Map<PsiIfStatement, Set<Integer>> ifStmtCloneMap = new HashMap<>();
//            Map<PsiMethodCallExpression, Set<Integer>> methodCallCloneMap = new HashMap<>();
//
//            // Iterate through all statements and add to corresponding LOCATION and STRING REP maps
//            for (int i = 0; i < methodBodies.length; i++) {
//                for (int j = 0; j < methodBodies[0].length; j++) {
//                    PsiStatement stat = methodBodies[i][j];
//                    if (stat == null) {
//                        continue;
//                    }
//
//                    // Calculate string representation and add to map
//                    StatType type = addStatToMap(stat, declarationMap, assignmentMap, ifStmtMap, methodCallMap);
//
//                    // Add to location map
//                    Pair<Integer, Integer> location = new Pair<>(i, j);
//
//                    if (type == StatType.ASSIGNMENT) {
//                        PsiExpression expr = ((PsiExpressionStatement) stat).getExpression();
//                        assignmentLocationMap.put((PsiAssignmentExpression) expr, location);
//                    } else if (type == StatType.DECLARATION) {
//                        declarationLocationMap.put((PsiDeclarationStatement) stat, location);
//                    } else if (type == StatType.METHOD_CALL) {
//                        PsiExpression expr = ((PsiExpressionStatement) stat).getExpression();
//                        methodCallLocationMap.put((PsiMethodCallExpression) expr, location);
//                    } else if (type == StatType.IF) {
//                        ifStmtLocationMap.put((PsiIfStatement) stat, location);
//                    }
//                }
//            }
//
//            compareAssignments(assignmentMap, assignmentCloneMap, assignmentLocationMap);
//            compareIfStatements(ifStmtMap, ifStmtCloneMap, ifStmtLocationMap);
//            compareDeclarations(declarationMap, declarationCloneMap, declarationLocationMap);
//            compareMethodCalls(methodCallMap, methodCallCloneMap, methodCallLocationMap);
//
//            List<Integer> rangeOfMethods = IntStream.range(0, methodBodies.length - 1).boxed().collect(Collectors.toList());
//            String filename = aClass.getContainingFile().getName();
//
//            // If we have an entire method where duplicate / similar has been detected for every line in another method
//            for (int i = 0; i < methodBodies.length; i++) {
//                // Empty method
//                if (methodBodies[i][0] == null) {
//                    continue;
//                }
//
//                Set<Integer> firstClones = getClones(methodBodies[i][0], assignmentCloneMap, declarationCloneMap, ifStmtCloneMap, methodCallCloneMap);
//
//                if (firstClones.size() == 0) {
//                    continue;
//                }
//
//                Set<Integer> intersection = new HashSet<>(firstClones);
//
//                for (int j = 1; j < methodBodies[0].length; j++) {
//                    if (methodBodies[i][j] == null) {
//                        break;
//                    }
//                    Set<Integer> currClones = getClones(methodBodies[i][j], assignmentCloneMap, declarationCloneMap, ifStmtCloneMap, methodCallCloneMap);
//                    if (currClones == null) {
//                        intersection.clear();
//                        break;
//                    } else {
//                        intersection.retainAll(currClones);
//                    }
//                }
//
//                //If it has at least one clone
//                FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(methods[i]), "clone", PsiStmtType.SWITCH);
//
//                for (Integer methodIndex : rangeOfMethods) {
//                    if (intersection.contains(methodIndex)) {
//                        int line = Utils.getLineNumber(methods[i]);
//                        Feedback feedback = new Feedback(line, "Method \'" + methods[i].getName() + "\' is clone of method \'" + methods[methodIndex].getName() + "\'.", filename, line + "-clone");
//                        feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
//                    } else {
//                        feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
//                    }
//                }
//            }
//        }

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

            // Iterate through all statements and add to corresponding LOCATION and STRING REP maps
            for (int i = 0; i < cases.length; i++) {
                for (int j = 0; j < cases[0].length; j++) {
                    PsiStatement stat = cases[i][j];
                    if (stat != null) {
                        // Calculate string representation and add to map
                        StatType type = addStatToMap(stat, declarationMap, assignmentMap, ifStmtMap, methodCallMap);

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

            compareAssignments(assignmentMap, assignmentCloneMap, assignmentLocationMap);
            compareIfStatements(ifStmtMap, ifStmtCloneMap, ifStmtLocationMap);
            compareDeclarations(declarationMap, declarationCloneMap, declarationLocationMap);
            compareMethodCalls(methodCallMap, methodCallCloneMap, methodCallLocationMap);

            List<Integer> rangeOfCases = IntStream.range(0, cases.length - 1).boxed().collect(Collectors.toList());
            List<Set<Integer>> clones = new ArrayList<>(cases.length);

            // If we have an entire case where duplicate / similar has been detected for every line in another case
            for (int i = 0; i < cases.length; i++) {
                // Empty case
                //TODO: consider fallthrough
                if (cases[i][0] == null) {
                    continue;
                }

                Set<Integer> firstClones = getClones(cases[i][0], assignmentCloneMap, declarationCloneMap, ifStmtCloneMap, methodCallCloneMap);

                if (firstClones.size() == 0) {
                    continue;
                }

                Set<Integer> intersection = new HashSet<>(firstClones);

                for (int j = 1; j < cases[0].length; j++) {
                    if (cases[i][j] == null) {
                        break;
                    }
                    Set<Integer> currClones = getClones(cases[i][j], assignmentCloneMap, declarationCloneMap, ifStmtCloneMap, methodCallCloneMap);
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

        private void compareMethodCalls(Map<PsiMethodCallExpression, String[]> methodCallMap,
                                        Map<PsiMethodCallExpression, Set<Integer>> methodCallCloneMap,
                                        Map<PsiMethodCallExpression, Pair<Integer, Integer>> methodCallLocationMap) {
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
                        updateCloneSet(entryKey, otherEntryKey, methodCallCloneMap, methodCallLocationMap);
                        updateCloneSet(otherEntryKey, entryKey, methodCallCloneMap, methodCallLocationMap);

                        continue;
                    }
                }
            }
        }

        private void compareDeclarations(Map<PsiDeclarationStatement, String[]> declarationMap,
                                         Map<PsiDeclarationStatement, Set<Integer>> declarationCloneMap,
                                         Map<PsiDeclarationStatement, Pair<Integer, Integer>> declarationLocationMap) {
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
                        updateCloneSet(entryKey, otherEntryKey, declarationCloneMap, declarationLocationMap);
                        updateCloneSet(otherEntryKey, entryKey, declarationCloneMap, declarationLocationMap);
                        continue;
                    }

                    if (CodeCloneUtils.declChangeInVarName(entryValue, otherEntryValue)) {
                        update = true;
                    }

                    if (update) {
                        updateCloneSet(entryKey, otherEntryKey, declarationCloneMap, declarationLocationMap);
                        updateCloneSet(otherEntryKey, entryKey, declarationCloneMap, declarationLocationMap);
                    }
                }
            }
        }

        private void compareIfStatements(Map<PsiIfStatement, String[]> ifStmtMap,
                                         Map<PsiIfStatement, Set<Integer>> ifStmtCloneMap,
                                         Map<PsiIfStatement, Pair<Integer, Integer>> ifStmtLocationMap) {
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
                        updateCloneSet(entryKey, otherEntryKey, ifStmtCloneMap, ifStmtLocationMap);
                        updateCloneSet(otherEntryKey, entryKey, ifStmtCloneMap, ifStmtLocationMap);

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
                        updateCloneSet(entryKey, otherEntryKey, ifStmtCloneMap, ifStmtLocationMap);
                        updateCloneSet(otherEntryKey, entryKey, ifStmtCloneMap, ifStmtLocationMap);
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

        private void compareAssignments(Map<PsiAssignmentExpression, String[]> assignmentMap,
                                        Map<PsiAssignmentExpression, Set<Integer>> assignmentCloneMap,
                                        Map<PsiAssignmentExpression, Pair<Integer, Integer>> assignmentLocationMap) {
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
                        updateCloneSet(entryKey, otherEntryKey, assignmentCloneMap, assignmentLocationMap);
                        updateCloneSet(otherEntryKey, entryKey, assignmentCloneMap, assignmentLocationMap);

                        continue;
                    }

                    if (CodeCloneUtils.changeInLiteral(entryValue, otherEntryValue)) {
                        update = true;
                    }

                    if (CodeCloneUtils.changeInOp(entryValue, otherEntryValue)) {
                        update = true;
                    }

                    if (update) {
                        updateCloneSet(entryKey, otherEntryKey, assignmentCloneMap, assignmentLocationMap);
                        updateCloneSet(otherEntryKey, entryKey, assignmentCloneMap, assignmentLocationMap);
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

        private void updateCloneSet(PsiAssignmentExpression entryKey, PsiAssignmentExpression otherEntryKey,
                                    Map<PsiAssignmentExpression, Set<Integer>> assignmentCloneMap,
                                    Map<PsiAssignmentExpression, Pair<Integer, Integer>> assignmentLocationMap) {
            Set<Integer> existingClones = assignmentCloneMap.get(entryKey);
            int caseIndex = assignmentLocationMap.get(otherEntryKey).getFirst();
            if (existingClones == null) {
                existingClones = new HashSet<>();
            }
            existingClones.add(caseIndex);
            assignmentCloneMap.put(entryKey, existingClones);
        }

        private void updateCloneSet(PsiMethodCallExpression entryKey, PsiMethodCallExpression otherEntryKey,
                                    Map<PsiMethodCallExpression, Set<Integer>> methodCallCloneMap,
                                    Map<PsiMethodCallExpression, Pair<Integer, Integer>> methodCallLocationMap) {
            Set<Integer> existingClones = methodCallCloneMap.get(entryKey);
            int caseIndex = methodCallLocationMap.get(otherEntryKey).getFirst();
            if (existingClones == null) {
                existingClones = new HashSet<>();
            }
            existingClones.add(caseIndex);
            methodCallCloneMap.put(entryKey, existingClones);
        }

        private void updateCloneSet(PsiDeclarationStatement entryKey, PsiDeclarationStatement otherEntryKey,
                                    Map<PsiDeclarationStatement, Set<Integer>> declarationCloneMap,
                                    Map<PsiDeclarationStatement, Pair<Integer, Integer>> declarationLocationMap) {
            Set<Integer> existingClones = declarationCloneMap.get(entryKey);
            int caseIndex = declarationLocationMap.get(otherEntryKey).getFirst();
            if (existingClones == null) {
                existingClones = new HashSet<>();
            }
            existingClones.add(caseIndex);
            declarationCloneMap.put(entryKey, existingClones);
        }

        private void updateCloneSet(PsiIfStatement entryKey, PsiIfStatement otherEntryKey,
                                    Map<PsiIfStatement, Set<Integer>> ifStmtCloneMap,
                                    Map<PsiIfStatement, Pair<Integer, Integer>> ifStmtLocationMap) {
            Set<Integer> existingClones = ifStmtCloneMap.get(entryKey);
            int caseIndex = ifStmtLocationMap.get(otherEntryKey).getFirst();
            if (existingClones == null) {
                existingClones = new HashSet<>();
            }
            existingClones.add(caseIndex);
            ifStmtCloneMap.put(entryKey, existingClones);
        }

        private StatType addStatToMap(PsiStatement stat, Map<PsiDeclarationStatement,
                String[]> declarationMap, Map<PsiAssignmentExpression,
                String[]> assignmentMap, Map<PsiIfStatement,
                String[]> ifStmtMap, Map<PsiMethodCallExpression,
                String[]> methodCallMap) {
            //TODO: make this nicer - we find the type here but then do it inside getStatAsStringArray as well
            String[] stringRep = TokeniseUtils.getStmtAsStringArray(stat);

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

            assert false : "Unknown statement type";
            return null;
        }

        private Set<Integer> getClones(PsiStatement stat,
                                       Map<PsiAssignmentExpression, Set<Integer>> assignmentCloneMap,
                                       Map<PsiDeclarationStatement, Set<Integer>> declarationCloneMap,
                                       Map<PsiIfStatement, Set<Integer>> ifStmtCloneMap,
                                       Map<PsiMethodCallExpression, Set<Integer>> methodCallCloneMap) {
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

            assert false : "Unknown statement type";
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

