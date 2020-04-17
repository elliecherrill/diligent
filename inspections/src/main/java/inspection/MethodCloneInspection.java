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

        CloneVisitor(ProblemsHolder holder) {
            this.holder = holder;
            feedbackHolder = FeedbackHolder.getInstance();

            declarationMap = new HashMap<>();
            assignmentMap = new HashMap<>();
            ifStmtMap = new HashMap<>();
            methodCallMap = new HashMap<>();
            returnMap = new HashMap<>();
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

            // Iterate through all statements and add to corresponding LOCATION and STRING REP maps
            for (int i = 0; i < methodBodies.length; i++) {
                for (int j = 0; j < methodBodies[0].length; j++) {
                    PsiStatement stat = methodBodies[i][j];
                    if (stat != null) {
                        // Add string representation and location to map with corresponding expression
                        Pair<Integer, Integer> location = new Pair<>(i, j);
                        addStatToMap(stat, location);
                    }
                }
            }

            compareStatements(assignmentMap, StatType.ASSIGNMENT);
            compareStatements(ifStmtMap, StatType.IF);
            compareStatements(declarationMap, StatType.DECLARATION);
            compareStatements(methodCallMap, StatType.METHOD_CALL);
            compareStatements(returnMap, StatType.RETURN);

            List<Integer> rangeOfMethods = IntStream.range(0, methodBodies.length - 1).boxed().collect(Collectors.toList());
            String filename = aClass.getContainingFile().getName();

            // If we have an entire method where duplicate / similar has been detected for every line in another method
            for (int i = 0; i < methodBodies.length; i++) {
                // Empty method
                if (methodBodies[i][0] == null) {
                    continue;
                }

                Set<Integer> firstClones = getClones(methodBodies[i][0]);

                if (firstClones == null || firstClones.size() == 0) {
                    continue;
                }

                Set<Integer> intersection = new HashSet<>(firstClones);

                for (int j = 1; j < methodBodies[0].length; j++) {
                    if (methodBodies[i][j] == null) {
                        break;
                    }
                    Set<Integer> currClones = getClones(methodBodies[i][j]);
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

        private <T extends PsiElement> void compareStatements(Map<T, CloneExpression<T>> map, StatType statType) {
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

                    if (statType == StatType.DECLARATION) {
                        if (CodeCloneUtils.declChangeInVarName(entryStringRep, otherEntryStringRep)) {
                            update = true;
                        }
                    } else if (statType == StatType.IF) {
                        // If statements are considered similar if
                        // 1. They are identical
                        // 2. They have identical conditions and similar bodies
                        // 3. They have identical bodies and similar conditions

                        //TODO: Finish this - I don't think sameIfCondition is working...
                        if (CodeCloneUtils.sameIfCondition(entryStringRep, otherEntryStringRep)) {
//                            if (haveSimilarBodies(entryKey, otherEntryKey)) {
//                                update = true;
//                            }
                        } else if (CodeCloneUtils.sameIfBody(entryStringRep, otherEntryStringRep)) {
                            if (CodeCloneUtils.conditionChangeInLhs(entryStringRep, otherEntryStringRep)) {
                                update = true;
                            } else if (CodeCloneUtils.conditionChangeInRhs(entryStringRep, otherEntryStringRep)) {
                                update = true;
                            }
                        }
                    } else if (statType == StatType.ASSIGNMENT) {
                        if (CodeCloneUtils.changeInLiteral(entryStringRep, otherEntryStringRep)) {
                            update = true;
                        }

                        if (CodeCloneUtils.changeInOp(entryStringRep, otherEntryStringRep)) {
                            update = true;
                        }
                    }

                    if (update) {
                        updateCloneSet(entryValue, otherEntryValue);
                        updateCloneSet(otherEntryValue, entryValue);
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

        private StatType addStatToMap(PsiStatement stat, Pair<Integer, Integer> location) {
            //TODO: make this nicer - we find the type here but then do it inside getStatAsStringArray as well
            String[] stringRep = CodeCloneUtils.getStmtAsStringArray(stat);

            if (stat instanceof PsiExpressionStatement) {
                PsiExpression expr = ((PsiExpressionStatement) stat).getExpression();
                if (expr instanceof PsiAssignmentExpression) {
                    PsiAssignmentExpression assExpr = (PsiAssignmentExpression) expr;
                    assignmentMap.put(assExpr, new CloneExpression<>(assExpr, stringRep, location));
                    return StatType.ASSIGNMENT;
                }

                if (expr instanceof PsiMethodCallExpression) {
                    PsiMethodCallExpression callExpr = (PsiMethodCallExpression) expr;
                    methodCallMap.put(callExpr, new CloneExpression<>(callExpr, stringRep, location));
                    return StatType.METHOD_CALL;
                }
            }

            if (stat instanceof PsiIfStatement) {
                PsiIfStatement ifStmt = (PsiIfStatement) stat;
                ifStmtMap.put(ifStmt, new CloneExpression<>(ifStmt, stringRep, location));
                return StatType.IF;
            }

            if (stat instanceof PsiDeclarationStatement) {
                PsiDeclarationStatement declStmt = (PsiDeclarationStatement) stat;
                declarationMap.put(declStmt, new CloneExpression<>(declStmt, stringRep, location));
                return StatType.DECLARATION;
            }

            if (stat instanceof PsiReturnStatement) {
                PsiReturnStatement returnStat = (PsiReturnStatement) stat;
                returnMap.put(returnStat, new CloneExpression<>(returnStat, stringRep, location));
                return StatType.RETURN;
            }

            assert true : "Unknown statement type";
            return null;
        }

        private Set<Integer> getClones(PsiStatement stat) {
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

            assert true : "Unknown statement type";
            return null;
        }

        enum StatType {
            ASSIGNMENT,
            METHOD_CALL,
            IF,
            DECLARATION,
            RETURN
        }
    }
}

