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

public final class MethodCloneInspection extends AbstractBaseJavaLocalInspectionTool {
    @Override
    @NotNull
    public String getDisplayName() {
        return "Similar code in methods";
    }

    public MethodCloneInspection() {
    }

    @Override
    @NotNull
    public String getShortName() {
        return "MethodClone";
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

        private Map<PsiDeclarationStatement, CloneExpression<PsiDeclarationStatement>> declarationMap;
        private Map<PsiAssignmentExpression, CloneExpression<PsiAssignmentExpression>> assignmentMap;
        private Map<PsiIfStatement, CloneExpression<PsiIfStatement>> ifStmtMap;
        private Map<PsiMethodCallExpression, CloneExpression<PsiMethodCallExpression>> methodCallMap;
        private Map<PsiReturnStatement, CloneExpression<PsiReturnStatement>> returnMap;
        private Map<PsiForStatement, CloneExpression<PsiForStatement>> forLoopMap;
        private Map<PsiForeachStatement, CloneExpression<PsiForeachStatement>> forEachLoopMap;
        private Map<PsiSwitchStatement, CloneExpression<PsiSwitchStatement>> switchMap;
        private Map<PsiAssertStatement, CloneExpression<PsiAssertStatement>> assertMap;
        private Map<PsiTryStatement, CloneExpression<PsiTryStatement>> tryMap;

        CloneVisitor(ProblemsHolder holder) {
            this.holder = holder;
            feedbackHolder = FeedbackHolder.getInstance();

            declarationMap = new HashMap<>();
            assignmentMap = new HashMap<>();
            ifStmtMap = new HashMap<>();
            methodCallMap = new HashMap<>();
            returnMap = new HashMap<>();
            forLoopMap = new HashMap<>();
            forEachLoopMap = new HashMap<>();
            switchMap = new HashMap<>();
            assertMap = new HashMap<>();
            tryMap = new HashMap<>();
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
        public void visitClass(PsiClass aClass) {
            super.visitClass(aClass);

            if (Utils.hasErrorsInFile(aClass)) {
                return;
            }

            PsiMethod[] methods = aClass.getMethods();

            if (methods.length == 0) {
                return;
            }

            PsiStatement[][] methodBodies = CodeCloneUtils.getMethodBodies(methods);
            cloneInit(methodBodies, declarationMap, assignmentMap, ifStmtMap,
                        methodCallMap, returnMap, forLoopMap, forEachLoopMap,
                        switchMap, assertMap, tryMap);

            List<Integer> rangeOfMethods = IntStream.range(0, methodBodies.length - 1).boxed().collect(Collectors.toList());
            String filename = aClass.getContainingFile().getName();

            // If we have an entire method where duplicate / similar has been detected for every line in another method
            for (int i = 0; i < methodBodies.length; i++) {
                // Empty method
                if (methodBodies[i][0] == null) {
                    continue;
                }

                Set<Integer> firstClones = getClones(methodBodies[i][0],
                        declarationMap, assignmentMap,
                        ifStmtMap, methodCallMap,
                        returnMap, forLoopMap,
                        forEachLoopMap,
                        switchMap, assertMap,
                        tryMap);

                if (firstClones == null || firstClones.size() == 0) {
                    continue;
                }

                Set<Integer> intersection = new HashSet<>(firstClones);

                for (int j = 1; j < methodBodies[0].length; j++) {
                    if (methodBodies[i][j] == null) {
                        break;
                    }
                    Set<Integer> currClones = getClones(methodBodies[i][j],
                            declarationMap, assignmentMap,
                            ifStmtMap, methodCallMap,
                            returnMap, forLoopMap,
                            forEachLoopMap,
                            switchMap, assertMap,
                            tryMap);
                    if (currClones == null) {
                        intersection.clear();
                        break;
                    } else {
                        intersection.retainAll(currClones);
                    }
                }

                //If it has at least one clone
                FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(methods[i]), "clone", PsiStmtType.SWITCH);

                for (Integer methodIndex : rangeOfMethods) {
                    if (intersection.contains(methodIndex)) {
                        int line = Utils.getLineNumber(methods[i]);
                        Feedback feedback = new Feedback(line, "Method \'" + methods[i].getName() + "\' is clone of method \'" + methods[methodIndex].getName() + "\'.", filename, line + "-clone");
                        feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
                    } else {
                        feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                    }
                }
            }
        }

        private void cloneInit(PsiStatement[][] bodies,
                               Map<PsiDeclarationStatement, CloneExpression<PsiDeclarationStatement>> declarationMap,
                               Map<PsiAssignmentExpression, CloneExpression<PsiAssignmentExpression>> assignmentMap,
                               Map<PsiIfStatement, CloneExpression<PsiIfStatement>> ifStmtMap,
                               Map<PsiMethodCallExpression, CloneExpression<PsiMethodCallExpression>> methodCallMap,
                               Map<PsiReturnStatement, CloneExpression<PsiReturnStatement>> returnMap,
                               Map<PsiForStatement, CloneExpression<PsiForStatement>> forLoopMap,
                               Map<PsiForeachStatement, CloneExpression<PsiForeachStatement>> forEachLoopMap,
                               Map<PsiSwitchStatement, CloneExpression<PsiSwitchStatement>> switchMap,
                               Map<PsiAssertStatement, CloneExpression<PsiAssertStatement>> assertMap,
                               Map<PsiTryStatement, CloneExpression<PsiTryStatement>> tryMap) {
            // Iterate through all statements and add to corresponding LOCATION and STRING REP maps
            for (int i = 0; i < bodies.length; i++) {
                for (int j = 0; j < bodies[0].length; j++) {
                    PsiStatement stat = bodies[i][j];
                    if (stat != null) {
                        // Add string representation and location to map with corresponding expression
                        Pair<Integer, Integer> location = new Pair<>(i, j);
                        addStatToMap(stat, location,
                                declarationMap, assignmentMap,
                                ifStmtMap, methodCallMap,
                                returnMap, forLoopMap,
                                forEachLoopMap,
                                switchMap, assertMap,
                                tryMap);
                    }
                }
            }

            compareStatements(assignmentMap);
            compareStatements(ifStmtMap);
            compareStatements(declarationMap);
            compareStatements(methodCallMap);
            compareStatements(returnMap);
            compareStatements(forLoopMap);
            compareStatements(switchMap);
            compareStatements(assertMap);
            compareStatements(tryMap);
            compareStatements(forEachLoopMap);
        }

        private <T extends PsiElement> void compareStatements(Map<T, CloneExpression<T>> map) {
            //TODO: should we only be considering statements in *different* method / if / cases ?
            for (Map.Entry<T, CloneExpression<T>> entry : map.entrySet()) {
                for (Map.Entry<T, CloneExpression<T>> otherEntry : map.entrySet()) {
                    T entryKey = entry.getKey();
                    T otherEntryKey = otherEntry.getKey();

                    CloneExpression<T> entryValue = entry.getValue();
                    CloneExpression<T> otherEntryValue = otherEntry.getValue();

                    String[] entryStringRep = entryValue.getStringRep();
                    String[] otherEntryStringRep = otherEntryValue.getStringRep();

                    if (entryKey.equals(otherEntryKey)) {
                        continue;
                    }

                    if (Arrays.equals(entryStringRep, otherEntryStringRep)) {
                        updateCloneSet(entryValue, otherEntryValue);
                        updateCloneSet(otherEntryValue, entryValue);
                        continue;
                    }

                    boolean update = false;

                    if (entryKey instanceof PsiDeclarationStatement) {
                        if (CodeCloneUtils.declChangeInVarName(entryStringRep, otherEntryStringRep)) {
                            update = true;
                        }
                    } else if (entryKey instanceof PsiIfStatement) {
                        // If statements are considered similar if
                        // 1. They are identical
                        // 2. They have identical conditions and similar bodies
                        // 3. They have identical bodies and similar conditions
                        if (CodeCloneUtils.sameIfCondition(entryStringRep, otherEntryStringRep)) {
                            PsiIfStatement ifStmt = (PsiIfStatement) entryKey;
                            PsiIfStatement otherIfStmt = (PsiIfStatement) otherEntryKey;

                            if (haveSimilarIfBodies(ifStmt, otherIfStmt)) {
                                update = true;
                            }
                        } else if (CodeCloneUtils.sameIfBody(entryStringRep, otherEntryStringRep)) {
                            if (CodeCloneUtils.conditionChangeInLhs(entryStringRep, otherEntryStringRep)) {
                                update = true;
                            } else if (CodeCloneUtils.conditionChangeInRhs(entryStringRep, otherEntryStringRep)) {
                                update = true;
                            }
                        }
                    } else if (entryKey instanceof PsiAssignmentExpression) {
                        if (CodeCloneUtils.changeInLiteral(entryStringRep, otherEntryStringRep)) {
                            update = true;
                        }

                        if (CodeCloneUtils.changeInOp(entryStringRep, otherEntryStringRep)) {
                            update = true;
                        }
                    } else if (entryKey instanceof PsiForStatement) {
                        if (CodeCloneUtils.sameForSetup(entryStringRep, otherEntryStringRep)) {
                            PsiForStatement forStmt = (PsiForStatement) entryKey;
                            PsiForStatement otherForStmt = (PsiForStatement) otherEntryKey;

                            if (haveSimilarForBodies(forStmt, otherForStmt)) {
                                update = true;
                            }
                        }

                        //TODO: to ask about vice versa (same body, similar condition)
                    } else if (entryKey instanceof PsiSwitchStatement) {
                        if (CodeCloneUtils.sameSwitchVar(entryStringRep, otherEntryStringRep)) {
                            PsiSwitchStatement switchStmt = (PsiSwitchStatement) entryKey;
                            PsiSwitchStatement otherSwitchStmt = (PsiSwitchStatement) otherEntryKey;

                            if (haveSimilarSwitchBodies(switchStmt, otherSwitchStmt)) {
                                update = true;
                            }
                        }
                    }

                    if (update) {
                        updateCloneSet(entryValue, otherEntryValue);
                        updateCloneSet(otherEntryValue, entryValue);
                    }
                }
            }
        }

        private String print(String[] arr) {
            StringBuffer sb = new StringBuffer();

            for (String s : arr) {
                sb.append(s);
                sb.append(" ");
            }

            return sb.toString();
        }

        private boolean haveSimilarIfBodies(PsiIfStatement ifStmt, PsiIfStatement otherIfStmt) {
            Map<PsiDeclarationStatement, CloneExpression<PsiDeclarationStatement>> declarationMap = new HashMap<>();
            Map<PsiAssignmentExpression, CloneExpression<PsiAssignmentExpression>> assignmentMap = new HashMap<>();
            Map<PsiIfStatement, CloneExpression<PsiIfStatement>> ifStmtMap = new HashMap<>();
            Map<PsiMethodCallExpression, CloneExpression<PsiMethodCallExpression>> methodCallMap = new HashMap<>();
            Map<PsiReturnStatement, CloneExpression<PsiReturnStatement>> returnMap = new HashMap<>();
            Map<PsiForStatement, CloneExpression<PsiForStatement>> forLoopMap = new HashMap<>();
            Map<PsiForeachStatement, CloneExpression<PsiForeachStatement>> forEachLoopMap = new HashMap<>();
            Map<PsiSwitchStatement, CloneExpression<PsiSwitchStatement>> switchMap = new HashMap<>();
            Map<PsiAssertStatement, CloneExpression<PsiAssertStatement>> assertMap = new HashMap<>();
            Map<PsiTryStatement, CloneExpression<PsiTryStatement>> tryMap = new HashMap<>();

            //TODO: consider else cases (else vs else-if)
            return areSimilarBlocks(ifStmt.getThenBranch(), otherIfStmt.getThenBranch(),
                    declarationMap, assignmentMap,
                    ifStmtMap, methodCallMap,
                    returnMap, forLoopMap,
                    forEachLoopMap,
                    switchMap, assertMap,
                    tryMap);
        }

        private boolean haveSimilarForBodies(PsiForStatement forStmt, PsiForStatement otherForStmt) {
            Map<PsiDeclarationStatement, CloneExpression<PsiDeclarationStatement>> declarationMap = new HashMap<>();
            Map<PsiAssignmentExpression, CloneExpression<PsiAssignmentExpression>> assignmentMap = new HashMap<>();
            Map<PsiIfStatement, CloneExpression<PsiIfStatement>> ifStmtMap = new HashMap<>();
            Map<PsiMethodCallExpression, CloneExpression<PsiMethodCallExpression>> methodCallMap = new HashMap<>();
            Map<PsiReturnStatement, CloneExpression<PsiReturnStatement>> returnMap = new HashMap<>();
            Map<PsiForStatement, CloneExpression<PsiForStatement>> forLoopMap = new HashMap<>();
            Map<PsiForeachStatement, CloneExpression<PsiForeachStatement>> forEachLoopMap = new HashMap<>();
            Map<PsiSwitchStatement, CloneExpression<PsiSwitchStatement>> switchMap = new HashMap<>();
            Map<PsiAssertStatement, CloneExpression<PsiAssertStatement>> assertMap = new HashMap<>();
            Map<PsiTryStatement, CloneExpression<PsiTryStatement>> tryMap = new HashMap<>();

            return areSimilarBlocks(forStmt.getBody(), otherForStmt.getBody(),
                    declarationMap, assignmentMap,
                    ifStmtMap, methodCallMap,
                    returnMap, forLoopMap,
                    forEachLoopMap,
                    switchMap, assertMap,
                    tryMap);
        }

        private boolean haveSimilarSwitchBodies(PsiSwitchStatement switchStmt, PsiSwitchStatement otherSwitchStmt) {
            Map<PsiDeclarationStatement, CloneExpression<PsiDeclarationStatement>> declarationMap = new HashMap<>();
            Map<PsiAssignmentExpression, CloneExpression<PsiAssignmentExpression>> assignmentMap = new HashMap<>();
            Map<PsiIfStatement, CloneExpression<PsiIfStatement>> ifStmtMap = new HashMap<>();
            Map<PsiMethodCallExpression, CloneExpression<PsiMethodCallExpression>> methodCallMap = new HashMap<>();
            Map<PsiReturnStatement, CloneExpression<PsiReturnStatement>> returnMap = new HashMap<>();
            Map<PsiForStatement, CloneExpression<PsiForStatement>> forLoopMap = new HashMap<>();
            Map<PsiForeachStatement, CloneExpression<PsiForeachStatement>> forEachLoopMap = new HashMap<>();
            Map<PsiSwitchStatement, CloneExpression<PsiSwitchStatement>> switchMap = new HashMap<>();
            Map<PsiAssertStatement, CloneExpression<PsiAssertStatement>> assertMap = new HashMap<>();
            Map<PsiTryStatement, CloneExpression<PsiTryStatement>> tryMap = new HashMap<>();

            PsiCodeBlock body = switchStmt.getBody();
            PsiCodeBlock otherBody = otherSwitchStmt.getBody();

            if ((body == null) && (otherBody == null)) {
                return true;
            }

            if ((body == null) || (otherBody == null)) {
                return false;
            }

            PsiStatement[][] blocks = CodeCloneUtils.getSameCaseBlocks(body, otherBody);
            cloneInit(blocks, declarationMap, assignmentMap, ifStmtMap, methodCallMap, returnMap, forLoopMap, forEachLoopMap,
                    switchMap, assertMap, tryMap);

            // For every array of statements need to find clones for every statement in the *following* array
            for (int i = 0; i < blocks.length; i+=2) {
                Set<Integer> firstClones = getClones(blocks[i][0],
                        declarationMap, assignmentMap,
                        ifStmtMap, methodCallMap,
                        returnMap, forLoopMap,
                        forEachLoopMap,
                        switchMap, assertMap,
                        tryMap);

                if (firstClones == null || firstClones.size() == 0) {
                    return false;
                }

                Set<Integer> intersection = new HashSet<>(firstClones);

                for (int j = 1; j < blocks[0].length; j++) {
                    if (blocks[i][j] == null) {
                        break;
                    }
                    Set<Integer> currClones = getClones(blocks[i][j],
                            declarationMap, assignmentMap,
                            ifStmtMap, methodCallMap,
                            returnMap, forLoopMap,
                            forEachLoopMap,
                            switchMap, assertMap,
                            tryMap);
                    if (currClones == null) {
                        intersection.clear();
                        break;
                    } else {
                        intersection.retainAll(currClones);
                    }
                }
                //TODO: remove this when we stop comparing to ourselves in compareStatements
                intersection.remove(i);

                if (intersection.isEmpty()) {
                    return false;
                }

                //Must be a clone of the following
                if (!(intersection.contains(i + 1))) {
                    return false;
                }
            }

            return true;
        }

        private boolean areSimilarBlocks(PsiStatement stat, PsiStatement otherStat,
                                         Map<PsiDeclarationStatement, CloneExpression<PsiDeclarationStatement>> declarationMap,
                                         Map<PsiAssignmentExpression, CloneExpression<PsiAssignmentExpression>> assignmentMap,
                                         Map<PsiIfStatement, CloneExpression<PsiIfStatement>> ifStmtMap,
                                         Map<PsiMethodCallExpression, CloneExpression<PsiMethodCallExpression>> methodCallMap,
                                         Map<PsiReturnStatement, CloneExpression<PsiReturnStatement>> returnMap,
                                         Map<PsiForStatement, CloneExpression<PsiForStatement>> forLoopMap,
                                         Map<PsiForeachStatement, CloneExpression<PsiForeachStatement>> forEachLoopMap,
                                         Map<PsiSwitchStatement, CloneExpression<PsiSwitchStatement>> switchMap,
                                         Map<PsiAssertStatement, CloneExpression<PsiAssertStatement>> assertMap,
                                         Map<PsiTryStatement, CloneExpression<PsiTryStatement>> tryMap) {
            PsiStatement[][] blocks = CodeCloneUtils.getBlocks(stat, otherStat);
            cloneInit(blocks, declarationMap, assignmentMap, ifStmtMap, methodCallMap, returnMap, forLoopMap, forEachLoopMap,
                    switchMap, assertMap, tryMap);

            if ((blocks[0][0] == null) && (blocks[1][0] != null)) {
                return false;
            }

            if ((blocks[0][0] != null) && (blocks[1][0] == null)) {
                return false;
            }

            if (blocks[0][0] != null) {
                //Both not empty - need to check they are the same

                //TODO: do we need to loop here? or just compare first to the second?
                //TODO: does similar mean we allow for extra / fewer statements?
                for (int i = 0; i < blocks.length; i++) {
                    Set<Integer> firstClones = getClones(blocks[i][0],
                            declarationMap, assignmentMap,
                            ifStmtMap, methodCallMap,
                            returnMap, forLoopMap,
                            forEachLoopMap,
                            switchMap, assertMap,
                            tryMap);

                    if (firstClones == null || firstClones.size() == 0) {
                        return false;
                    }

                    Set<Integer> intersection = new HashSet<>(firstClones);

                    for (int j = 1; j < blocks[0].length; j++) {
                        if (blocks[i][j] == null) {
                            break;
                        }
                        Set<Integer> currClones = getClones(blocks[i][j],
                                declarationMap, assignmentMap,
                                ifStmtMap, methodCallMap,
                                returnMap, forLoopMap,
                                forEachLoopMap,
                                switchMap, assertMap,
                                tryMap);
                        if (currClones == null) {
                            intersection.clear();
                            break;
                        } else {
                            intersection.retainAll(currClones);
                        }
                    }
                    //TODO: remove this when we stop comparing to ourselves in compareStatements
                    intersection.remove(i);

                    if (intersection.isEmpty()) {
                        return false;
                    }
                }
            }

            return true;
        }

        private <T extends PsiElement> void updateCloneSet(CloneExpression<T> cloneExpr,
                                                           CloneExpression<T> otherCloneExpr) {
            Set<Integer> existingClones = cloneExpr.getClones();
            int caseIndex = otherCloneExpr.getLocation().getFirst();
            if (existingClones == null) {
                existingClones = new HashSet<>();
            }
            existingClones.add(caseIndex);
            cloneExpr.setClones(existingClones);
        }

        private void addStatToMap(PsiStatement stat, Pair<Integer, Integer> location,
                                  Map<PsiDeclarationStatement, CloneExpression<PsiDeclarationStatement>> declarationMap,
                                  Map<PsiAssignmentExpression, CloneExpression<PsiAssignmentExpression>> assignmentMap,
                                  Map<PsiIfStatement, CloneExpression<PsiIfStatement>> ifStmtMap,
                                  Map<PsiMethodCallExpression, CloneExpression<PsiMethodCallExpression>> methodCallMap,
                                  Map<PsiReturnStatement, CloneExpression<PsiReturnStatement>> returnMap,
                                  Map<PsiForStatement, CloneExpression<PsiForStatement>> forLoopMap,
                                  Map<PsiForeachStatement, CloneExpression<PsiForeachStatement>> forEachLoopMap,
                                  Map<PsiSwitchStatement, CloneExpression<PsiSwitchStatement>> switchMap,
                                  Map<PsiAssertStatement, CloneExpression<PsiAssertStatement>> assertMap,
                                  Map<PsiTryStatement, CloneExpression<PsiTryStatement>> tryMap) {
            //TODO: make this nicer - we find the type here but then do it inside getStatAsStringArray as well
            String[] stringRep = CodeCloneUtils.getStmtAsStringArray(stat);

            if (stat instanceof PsiExpressionStatement) {
                PsiExpression expr = ((PsiExpressionStatement) stat).getExpression();
                if (expr instanceof PsiAssignmentExpression) {
                    PsiAssignmentExpression assExpr = (PsiAssignmentExpression) expr;
                    assignmentMap.put(assExpr, new CloneExpression<>(assExpr, stringRep, location));
                    return;
                }

                if (expr instanceof PsiMethodCallExpression) {
                    PsiMethodCallExpression callExpr = (PsiMethodCallExpression) expr;
                    methodCallMap.put(callExpr, new CloneExpression<>(callExpr, stringRep, location));
                    return;
                }
            }

            if (stat instanceof PsiIfStatement) {
                PsiIfStatement ifStmt = (PsiIfStatement) stat;
                ifStmtMap.put(ifStmt, new CloneExpression<>(ifStmt, stringRep, location));
                return;
            }

            if (stat instanceof PsiDeclarationStatement) {
                PsiDeclarationStatement declStmt = (PsiDeclarationStatement) stat;
                declarationMap.put(declStmt, new CloneExpression<>(declStmt, stringRep, location));
                return;
            }

            if (stat instanceof PsiReturnStatement) {
                PsiReturnStatement returnStat = (PsiReturnStatement) stat;
                returnMap.put(returnStat, new CloneExpression<>(returnStat, stringRep, location));
                return;
            }

            if (stat instanceof PsiForStatement) {
                PsiForStatement forStat = (PsiForStatement) stat;
                forLoopMap.put(forStat, new CloneExpression<>(forStat, stringRep, location));
                return;
            }

            if (stat instanceof PsiForeachStatement) {
                PsiForeachStatement forEachStat = (PsiForeachStatement) stat;
                forEachLoopMap.put(forEachStat, new CloneExpression<>(forEachStat, stringRep, location));
                return;
            }

            if (stat instanceof PsiSwitchStatement) {
                PsiSwitchStatement switchStat = (PsiSwitchStatement) stat;
                switchMap.put(switchStat, new CloneExpression<>(switchStat, stringRep, location));
                return;
            }

            if (stat instanceof PsiAssertStatement) {
                PsiAssertStatement assertStat = (PsiAssertStatement) stat;
                assertMap.put(assertStat, new CloneExpression<>(assertStat, stringRep, location));
                return;
            }

            if (stat instanceof PsiTryStatement) {
                PsiTryStatement tryStat = (PsiTryStatement) stat;
                tryMap.put(tryStat, new CloneExpression<>(tryStat, stringRep, location));
                return;
            }

            assert false : "Unknown statement type " + stat.toString();
        }

        private Set<Integer> getClones(PsiStatement stat,
                                       Map<PsiDeclarationStatement, CloneExpression<PsiDeclarationStatement>> declarationMap,
                                       Map<PsiAssignmentExpression, CloneExpression<PsiAssignmentExpression>> assignmentMap,
                                       Map<PsiIfStatement, CloneExpression<PsiIfStatement>> ifStmtMap,
                                       Map<PsiMethodCallExpression, CloneExpression<PsiMethodCallExpression>> methodCallMap,
                                       Map<PsiReturnStatement, CloneExpression<PsiReturnStatement>> returnMap,
                                       Map<PsiForStatement, CloneExpression<PsiForStatement>> forLoopMap,
                                       Map<PsiForeachStatement, CloneExpression<PsiForeachStatement>> forEachLoopMap,
                                       Map<PsiSwitchStatement, CloneExpression<PsiSwitchStatement>> switchMap,
                                       Map<PsiAssertStatement, CloneExpression<PsiAssertStatement>> assertMap,
                                       Map<PsiTryStatement, CloneExpression<PsiTryStatement>> tryMap) {
            if (stat instanceof PsiExpressionStatement) {
                PsiExpression expr = ((PsiExpressionStatement) stat).getExpression();
                if (expr instanceof PsiAssignmentExpression) {
                    PsiAssignmentExpression assExpr = (PsiAssignmentExpression) expr;
                    CloneExpression<PsiAssignmentExpression> cloneExpr = assignmentMap.get(assExpr);
                    return cloneExpr.getClones();
                }

                if (expr instanceof PsiMethodCallExpression) {
                    PsiMethodCallExpression callExpr = (PsiMethodCallExpression) expr;
                    CloneExpression<PsiMethodCallExpression> cloneExpr = methodCallMap.get(callExpr);
                    return cloneExpr.getClones();
                }
            }

            if (stat instanceof PsiIfStatement) {
                PsiIfStatement ifStmt = (PsiIfStatement) stat;
                CloneExpression<PsiIfStatement> cloneExpr = ifStmtMap.get(ifStmt);
                return cloneExpr.getClones();
            }

            if (stat instanceof PsiDeclarationStatement) {
                PsiDeclarationStatement declStmt = (PsiDeclarationStatement) stat;
                CloneExpression<PsiDeclarationStatement> cloneExpr = declarationMap.get(declStmt);
                return cloneExpr.getClones();
            }

            if (stat instanceof PsiReturnStatement) {
                PsiReturnStatement returnStmt = (PsiReturnStatement) stat;
                CloneExpression<PsiReturnStatement> cloneExpr = returnMap.get(returnStmt);
                return cloneExpr.getClones();
            }

            if (stat instanceof PsiForStatement) {
                PsiForStatement forStat = (PsiForStatement) stat;
                CloneExpression<PsiForStatement> cloneExpr = forLoopMap.get(forStat);
                return cloneExpr.getClones();
            }

            if (stat instanceof PsiForeachStatement) {
                PsiForeachStatement forEachStat = (PsiForeachStatement) stat;
                CloneExpression<PsiForeachStatement> cloneExpr = forEachLoopMap.get(forEachStat);
                return cloneExpr.getClones();
            }

            if (stat instanceof PsiSwitchStatement) {
                PsiSwitchStatement switchStat = (PsiSwitchStatement) stat;
                CloneExpression<PsiSwitchStatement> cloneExpr = switchMap.get(switchStat);
                return cloneExpr.getClones();
            }

            if (stat instanceof PsiAssertStatement) {
                PsiAssertStatement assertStat = (PsiAssertStatement) stat;
                CloneExpression<PsiAssertStatement> cloneExpr = assertMap.get(assertStat);
                return cloneExpr.getClones();
            }

            if (stat instanceof PsiTryStatement) {
                PsiTryStatement tryStat = (PsiTryStatement) stat;
                CloneExpression<PsiTryStatement> cloneExpr = tryMap.get(tryStat);
                return cloneExpr.getClones();
            }

            assert false : "Unknown statement type " + stat.toString();
            return null;
        }
    }
}

