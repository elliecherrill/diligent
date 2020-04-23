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
        return "Similar code in methods and switch cases.";
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

            Map<PsiDeclarationStatement, CloneExpression> declarationMap = new HashMap<>();
            Map<PsiAssignmentExpression, CloneExpression> assignmentMap = new HashMap<>();
            Map<PsiIfStatement, CloneExpression> ifStmtMap = new HashMap<>();
            Map<PsiMethodCallExpression, CloneExpression> methodCallMap = new HashMap<>();
            Map<PsiReturnStatement, CloneExpression> returnMap = new HashMap<>();
            Map<PsiForStatement, CloneExpression> forLoopMap = new HashMap<>();
            Map<PsiForeachStatement, CloneExpression> forEachLoopMap = new HashMap<>();
            Map<PsiWhileStatement, CloneExpression> whileLoopMap = new HashMap<>();
            Map<PsiDoWhileStatement, CloneExpression> doWhileLoopMap = new HashMap<>();
            Map<PsiSwitchStatement, CloneExpression> switchMap = new HashMap<>();
            Map<PsiAssertStatement, CloneExpression> assertMap = new HashMap<>();
            Map<PsiTryStatement, CloneExpression> tryMap = new HashMap<>();
            Map<PsiThrowStatement, CloneExpression> throwMap = new HashMap<>();

            PsiStatement[][] cases = CodeCloneUtils.getCaseBlocks(switchBody);
            cloneInit(cases, declarationMap, assignmentMap, ifStmtMap,
                    methodCallMap, returnMap, forLoopMap, forEachLoopMap,
                    whileLoopMap, doWhileLoopMap, switchMap, assertMap,
                    tryMap, throwMap);

            List<Integer> rangeOfCases = IntStream.range(0, cases.length - 1).boxed().collect(Collectors.toList());
            List<Set<Location>> clones = new ArrayList<>(cases.length);

            // If we have an entire case where duplicate / similar has been detected for every line in another case
            for (int i = 0; i < cases.length; i++) {
                // Empty case
                //TODO: consider fallthrough
                if ((cases[i][0] == null) || (cases[i][1] == null)){
                    return;
                    //Only consider error when all cases are > 1 in length (excluding break;)
                }

                Set<Location> firstClones = getClones(cases[i][0],
                        declarationMap, assignmentMap,
                        ifStmtMap, methodCallMap,
                        returnMap, forLoopMap,
                        forEachLoopMap, whileLoopMap,
                        doWhileLoopMap,
                        switchMap, assertMap,
                        tryMap, throwMap);

                if (firstClones == null || firstClones.size() == 0) {
                    continue;
                }

                Set<Location> intersection = new LinkedHashSet<>(firstClones);

                for (int j = 1; j < cases[0].length; j++) {
                    if (cases[i][j] == null) {
                        break;
                    }
                    Set<Location> currClones = getClones(cases[i][j],
                            declarationMap, assignmentMap,
                            ifStmtMap, methodCallMap,
                            returnMap, forLoopMap,
                            forEachLoopMap, whileLoopMap,
                            doWhileLoopMap,
                            switchMap, assertMap,
                            tryMap, throwMap);

                    if (currClones == null) {
                        intersection.clear();
                        break;
                    } else {
                        intersection = CodeCloneUtils.getCombinedClones(intersection, currClones);
                    }
                }

                // Add itself to its clones
                intersection.add(new Location(i, 0));

                clones.add(intersection);
            }

            String filename = statement.getContainingFile().getName();
            int line = Utils.getLineNumber(statement);
            FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(statement), line + "switch-clone", PsiStmtType.SWITCH);

            if (CodeCloneUtils.transitiveClosureOfClones(clones, rangeOfCases)) {
                Feedback feedback = new Feedback(line,
                        "All cases in switch statement are clones of one another.", filename,
                        line + "-switch-clone");
                feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
            } else {
                feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
            }

        }

        @Override
        public void visitClass(PsiClass aClass) {
            super.visitClass(aClass);

            if (Utils.hasErrorsInFile(aClass)) {
                return;
            }

            PsiCodeBlock[] codeBlocks = CodeCloneUtils.getAllCodeBlocks(aClass);

            if (codeBlocks.length <= 1) {
                return;
            }

            Map<PsiDeclarationStatement, CloneExpression> declarationMap = new HashMap<>();
            Map<PsiAssignmentExpression, CloneExpression> assignmentMap = new HashMap<>();
            Map<PsiIfStatement, CloneExpression> ifStmtMap = new HashMap<>();
            Map<PsiMethodCallExpression, CloneExpression> methodCallMap = new HashMap<>();
            Map<PsiReturnStatement, CloneExpression> returnMap = new HashMap<>();
            Map<PsiForStatement, CloneExpression> forLoopMap = new HashMap<>();
            Map<PsiForeachStatement, CloneExpression> forEachLoopMap = new HashMap<>();
            Map<PsiWhileStatement, CloneExpression> whileLoopMap = new HashMap<>();
            Map<PsiDoWhileStatement, CloneExpression> doWhileLoopMap = new HashMap<>();
            Map<PsiSwitchStatement, CloneExpression> switchMap = new HashMap<>();
            Map<PsiAssertStatement, CloneExpression> assertMap = new HashMap<>();
            Map<PsiTryStatement, CloneExpression> tryMap = new HashMap<>();
            Map<PsiThrowStatement, CloneExpression> throwMap = new HashMap<>();

            PsiStatement[][] blockBodies = CodeCloneUtils.getBlockBodies(codeBlocks);
            cloneInit(blockBodies, declarationMap, assignmentMap, ifStmtMap,
                    methodCallMap, returnMap, forLoopMap, forEachLoopMap,
                    whileLoopMap, doWhileLoopMap, switchMap, assertMap,
                    tryMap, throwMap);

            List<Integer> rangeOfBlocks = IntStream.range(0, blockBodies.length - 1).boxed().collect(Collectors.toList());
            String filename = aClass.getContainingFile().getName();

            // If we have an entire method where duplicate / similar has been detected for every line in another method
            for (int i = 0; i < blockBodies.length; i++) {
                // Empty block
                if (blockBodies[i][0] == null) {
                    continue;
                }

                Set<Location> firstClones = getClones(blockBodies[i][0],
                        declarationMap, assignmentMap,
                        ifStmtMap, methodCallMap,
                        returnMap, forLoopMap,
                        forEachLoopMap, whileLoopMap,
                        doWhileLoopMap,
                        switchMap, assertMap,
                        tryMap, throwMap);

                Set<Location> intersection;
                if (firstClones == null || firstClones.size() == 0) {
                    intersection = new LinkedHashSet<>();
                } else {
                    intersection = new LinkedHashSet<>(firstClones);

                    for (int j = 1; j < blockBodies[0].length; j++) {
                        if (blockBodies[i][j] == null) {
                            break;
                        }
                        Set<Location> currClones = getClones(blockBodies[i][j],
                                declarationMap, assignmentMap,
                                ifStmtMap, methodCallMap,
                                returnMap, forLoopMap,
                                forEachLoopMap, whileLoopMap,
                                doWhileLoopMap,
                                switchMap, assertMap,
                                tryMap, throwMap);

                        if (currClones == null) {
                            intersection.clear();
                            break;
                        } else {
                            intersection = CodeCloneUtils.getCombinedClones(intersection, currClones);
                        }
                    }
                }

                for (int blockIndex : rangeOfBlocks) {
                    FeedbackIdentifier feedbackId;
                    if (i > blockIndex) {
                        feedbackId = new FeedbackIdentifier(Utils.getPointer(codeBlocks[i]), blockIndex + "-block-clone", PsiStmtType.BLOCK);
                    } else {
                        feedbackId = new FeedbackIdentifier(Utils.getPointer(codeBlocks[blockIndex]), i + "-block-clone", PsiStmtType.BLOCK);
                    }

                    if (CodeCloneUtils.isBlockClone(intersection, blockIndex)) {
                        int line = Utils.getLineNumber(codeBlocks[i]);
                        Feedback feedback = new Feedback(line,
                                "Block \'" + CodeCloneUtils.printCodeBlock(codeBlocks[i]) + "\' is clone of block \'" + CodeCloneUtils.printCodeBlock(codeBlocks[blockIndex]) + "\'.",
                                filename,
                                line + "-block-clone");
                        feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
                    } else {
                        feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                    }
                }
            }
        }

        private void cloneInit(PsiStatement[][] bodies,
                               Map<PsiDeclarationStatement, CloneExpression> declarationMap,
                               Map<PsiAssignmentExpression, CloneExpression> assignmentMap,
                               Map<PsiIfStatement, CloneExpression> ifStmtMap,
                               Map<PsiMethodCallExpression, CloneExpression> methodCallMap,
                               Map<PsiReturnStatement, CloneExpression> returnMap,
                               Map<PsiForStatement, CloneExpression> forLoopMap,
                               Map<PsiForeachStatement, CloneExpression> forEachLoopMap,
                               Map<PsiWhileStatement, CloneExpression> whileLoopMap,
                               Map<PsiDoWhileStatement, CloneExpression> doWhileLoopMap,
                               Map<PsiSwitchStatement, CloneExpression> switchMap,
                               Map<PsiAssertStatement, CloneExpression> assertMap,
                               Map<PsiTryStatement, CloneExpression> tryMap,
                               Map<PsiThrowStatement, CloneExpression> throwMap) {
            // Iterate through all statements and add to corresponding LOCATION and STRING REP maps
            for (int i = 0; i < bodies.length; i++) {
                for (int j = 0; j < bodies[0].length; j++) {
                    PsiStatement stat = bodies[i][j];
                    if (stat != null) {
                        // Add string representation and location to map with corresponding expression
                        Location location = new Location(i, j);
                        addStatToMap(stat, location,
                                declarationMap, assignmentMap,
                                ifStmtMap, methodCallMap,
                                returnMap, forLoopMap,
                                forEachLoopMap, whileLoopMap,
                                doWhileLoopMap,
                                switchMap, assertMap,
                                tryMap, throwMap);
                    }
                }
            }

            compareStatements(assignmentMap);
            compareStatements(ifStmtMap);
            compareStatements(declarationMap);
            compareStatements(methodCallMap);
            compareStatements(returnMap);
            compareStatements(forLoopMap);
            compareStatements(forEachLoopMap);
            compareStatements(whileLoopMap);
            compareStatements(doWhileLoopMap);
            compareStatements(switchMap);
            compareStatements(assertMap);
            compareStatements(tryMap);
            compareStatements(throwMap);
        }

        private <T extends PsiElement> void compareStatements(Map<T, CloneExpression> map) {
            //TODO: should we only be considering statements in *different* method / if / cases ?
            for (Map.Entry<T, CloneExpression> entry : map.entrySet()) {
                for (Map.Entry<T, CloneExpression> otherEntry : map.entrySet()) {
                    T entryKey = entry.getKey();
                    T otherEntryKey = otherEntry.getKey();

                    CloneExpression entryValue = entry.getValue();
                    CloneExpression otherEntryValue = otherEntry.getValue();

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
                        //TODO: to ask about vice versa (same body, similar condition)
                        if (CodeCloneUtils.sameForSetup(entryStringRep, otherEntryStringRep)) {
                            PsiForStatement forStmt = (PsiForStatement) entryKey;
                            PsiForStatement otherForStmt = (PsiForStatement) otherEntryKey;

                            if (areSimilarBlocks(forStmt.getBody(), otherForStmt.getBody())) {
                                update = true;
                            }
                        }
                    } else if (entryKey instanceof PsiForeachStatement) {
                        if (CodeCloneUtils.sameForEachSetup(entryStringRep, otherEntryStringRep)) {
                            PsiForeachStatement forEachStmt = (PsiForeachStatement) entryKey;
                            PsiForeachStatement otherForEachStmt = (PsiForeachStatement) otherEntryKey;

                            if (areSimilarBlocks(forEachStmt.getBody(), otherForEachStmt.getBody())) {
                                update = true;
                            }
                        }
                    } else if (entryKey instanceof PsiWhileStatement) {
                        if (CodeCloneUtils.sameWhileCondition(entryStringRep, otherEntryStringRep)) {
                            PsiWhileStatement whileStmt = (PsiWhileStatement) entryKey;
                            PsiWhileStatement otherWhileStmt = (PsiWhileStatement) otherEntryKey;

                            if (areSimilarBlocks(whileStmt.getBody(), otherWhileStmt.getBody())) {
                                update = true;
                            }
                        }
                    } else if (entryKey instanceof PsiDoWhileStatement) {
                        if (CodeCloneUtils.sameDoWhileCondition(entryStringRep, otherEntryStringRep)) {
                            PsiDoWhileStatement doWhileStmt = (PsiDoWhileStatement) entryKey;
                            PsiDoWhileStatement otherDoWhileStmt = (PsiDoWhileStatement) otherEntryKey;

                            if (areSimilarBlocks(doWhileStmt.getBody(), otherDoWhileStmt.getBody())) {
                                update = true;
                            }
                        }
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

        private boolean haveSimilarIfBodies(PsiIfStatement ifStmt, PsiIfStatement otherIfStmt) {
            //TODO: consider else cases (else vs else-if)
            return areSimilarBlocks(ifStmt.getThenBranch(), otherIfStmt.getThenBranch());
        }

        private boolean haveSimilarSwitchBodies(PsiSwitchStatement switchStmt, PsiSwitchStatement otherSwitchStmt) {
            Map<PsiDeclarationStatement, CloneExpression> declarationMap = new HashMap<>();
            Map<PsiAssignmentExpression, CloneExpression> assignmentMap = new HashMap<>();
            Map<PsiIfStatement, CloneExpression> ifStmtMap = new HashMap<>();
            Map<PsiMethodCallExpression, CloneExpression> methodCallMap = new HashMap<>();
            Map<PsiReturnStatement, CloneExpression> returnMap = new HashMap<>();
            Map<PsiForStatement, CloneExpression> forLoopMap = new HashMap<>();
            Map<PsiForeachStatement, CloneExpression> forEachLoopMap = new HashMap<>();
            Map<PsiWhileStatement, CloneExpression> whileLoopMap = new HashMap<>();
            Map<PsiDoWhileStatement, CloneExpression> doWhileLoopMap = new HashMap<>();
            Map<PsiSwitchStatement, CloneExpression> switchMap = new HashMap<>();
            Map<PsiAssertStatement, CloneExpression> assertMap = new HashMap<>();
            Map<PsiTryStatement, CloneExpression> tryMap = new HashMap<>();
            Map<PsiThrowStatement, CloneExpression> throwMap = new HashMap<>();

            PsiCodeBlock body = switchStmt.getBody();
            PsiCodeBlock otherBody = otherSwitchStmt.getBody();

            if ((body == null) && (otherBody == null)) {
                return true;
            }

            if ((body == null) || (otherBody == null)) {
                return false;
            }

            PsiStatement[][] blocks = CodeCloneUtils.getSameCaseBlocks(body, otherBody);
            cloneInit(blocks, declarationMap, assignmentMap, ifStmtMap, methodCallMap, returnMap,
                    forLoopMap, forEachLoopMap, whileLoopMap, doWhileLoopMap, switchMap, assertMap,
                    tryMap, throwMap);

            // For every array of statements need to find clones for every statement in the *following* array
            for (int i = 0; i < blocks.length; i += 2) {
                if (blocks[i][0] == null) {
                    continue;
                }

                Set<Location> firstClones = getClones(blocks[i][0],
                        declarationMap, assignmentMap,
                        ifStmtMap, methodCallMap,
                        returnMap, forLoopMap,
                        forEachLoopMap, whileLoopMap,
                        doWhileLoopMap,
                        switchMap, assertMap,
                        tryMap, throwMap);

                if (firstClones == null || firstClones.size() == 0) {
                    return false;
                }

                Set<Location> intersection = new LinkedHashSet<>(firstClones);

                for (int j = 1; j < blocks[0].length; j++) {
                    if (blocks[i][j] == null) {
                        break;
                    }
                    Set<Location> currClones = getClones(blocks[i][j],
                            declarationMap, assignmentMap,
                            ifStmtMap, methodCallMap,
                            returnMap, forLoopMap,
                            forEachLoopMap, whileLoopMap,
                            doWhileLoopMap,
                            switchMap, assertMap,
                            tryMap, throwMap);
                    if (currClones == null) {
                        intersection.clear();
                        break;
                    } else {
                        intersection = CodeCloneUtils.getCombinedClones(intersection, currClones);
                    }
                }
                //TODO: remove this when we stop comparing to ourselves in compareStatements
                intersection = CodeCloneUtils.removeCodeBlock(intersection, i);

                if (intersection.isEmpty()) {
                    return false;
                }

                //Must be a clone of the following
                if (CodeCloneUtils.getClonesInCodeBlock(intersection, i + 1).isEmpty()) {
                    return false;
                }
            }

            return true;
        }

        private boolean areSimilarBlocks(PsiStatement stat, PsiStatement otherStat) {
            Map<PsiDeclarationStatement, CloneExpression> declarationMap = new HashMap<>();
            Map<PsiAssignmentExpression, CloneExpression> assignmentMap = new HashMap<>();
            Map<PsiIfStatement, CloneExpression> ifStmtMap = new HashMap<>();
            Map<PsiMethodCallExpression, CloneExpression> methodCallMap = new HashMap<>();
            Map<PsiReturnStatement, CloneExpression> returnMap = new HashMap<>();
            Map<PsiForStatement, CloneExpression> forLoopMap = new HashMap<>();
            Map<PsiForeachStatement, CloneExpression> forEachLoopMap = new HashMap<>();
            Map<PsiWhileStatement, CloneExpression> whileLoopMap = new HashMap<>();
            Map<PsiDoWhileStatement, CloneExpression> doWhileLoopMap = new HashMap<>();
            Map<PsiSwitchStatement, CloneExpression> switchMap = new HashMap<>();
            Map<PsiAssertStatement, CloneExpression> assertMap = new HashMap<>();
            Map<PsiTryStatement, CloneExpression> tryMap = new HashMap<>();
            Map<PsiThrowStatement, CloneExpression> throwMap = new HashMap<>();

            PsiStatement[][] blocks = CodeCloneUtils.getBlocks(stat, otherStat);
            cloneInit(blocks, declarationMap, assignmentMap, ifStmtMap, methodCallMap, returnMap,
                    forLoopMap, forEachLoopMap, whileLoopMap, doWhileLoopMap, switchMap, assertMap,
                    tryMap, throwMap);

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
                    Set<Location> firstClones = getClones(blocks[i][0],
                            declarationMap, assignmentMap,
                            ifStmtMap, methodCallMap,
                            returnMap, forLoopMap,
                            forEachLoopMap, whileLoopMap,
                            doWhileLoopMap,
                            switchMap, assertMap,
                            tryMap, throwMap);

                    if (firstClones == null || firstClones.size() == 0) {
                        return false;
                    }

                    Set<Location> intersection = new LinkedHashSet<>(firstClones);

                    for (int j = 1; j < blocks[0].length; j++) {
                        if (blocks[i][j] == null) {
                            break;
                        }
                        Set<Location> currClones = getClones(blocks[i][j],
                                declarationMap, assignmentMap,
                                ifStmtMap, methodCallMap,
                                returnMap, forLoopMap,
                                forEachLoopMap, whileLoopMap,
                                doWhileLoopMap,
                                switchMap, assertMap,
                                tryMap, throwMap);
                        if (currClones == null) {
                            intersection.clear();
                            break;
                        } else {
                            intersection = CodeCloneUtils.getCombinedClones(intersection, currClones);
                        }
                    }
                    //TODO: remove this when we stop comparing to ourselves in compareStatements
                    intersection = CodeCloneUtils.removeCodeBlock(intersection, i);

                    if (intersection.isEmpty()) {
                        return false;
                    }
                }
            }

            return true;
        }

        private <T extends PsiElement> void updateCloneSet(CloneExpression cloneExpr, CloneExpression otherCloneExpr) {
            Set<Location> existingClones = cloneExpr.getClones();
            Location location = otherCloneExpr.getLocation();
            if (existingClones == null) {
                existingClones = new LinkedHashSet<>();
            }
            existingClones.add(location);
            cloneExpr.setClones(existingClones);
        }

        private void addStatToMap(PsiStatement stat, Location location,
                                  Map<PsiDeclarationStatement, CloneExpression> declarationMap,
                                  Map<PsiAssignmentExpression, CloneExpression> assignmentMap,
                                  Map<PsiIfStatement, CloneExpression> ifStmtMap,
                                  Map<PsiMethodCallExpression, CloneExpression> methodCallMap,
                                  Map<PsiReturnStatement, CloneExpression> returnMap,
                                  Map<PsiForStatement, CloneExpression> forLoopMap,
                                  Map<PsiForeachStatement, CloneExpression> forEachLoopMap,
                                  Map<PsiWhileStatement, CloneExpression> whileLoopMap,
                                  Map<PsiDoWhileStatement, CloneExpression> doWhileLoopMap,
                                  Map<PsiSwitchStatement, CloneExpression> switchMap,
                                  Map<PsiAssertStatement, CloneExpression> assertMap,
                                  Map<PsiTryStatement, CloneExpression> tryMap,
                                  Map<PsiThrowStatement, CloneExpression> throwMap) {
            //TODO: make this nicer - we find the type here but then do it inside getStatAsStringArray as well
            String[] stringRep = TokeniseUtils.getStmtAsStringArray(stat);

            if (stat instanceof PsiExpressionStatement) {
                PsiExpression expr = ((PsiExpressionStatement) stat).getExpression();
                if (expr instanceof PsiAssignmentExpression) {
                    PsiAssignmentExpression assExpr = (PsiAssignmentExpression) expr;
                    assignmentMap.put(assExpr, new CloneExpression(stringRep, location));
                    return;
                }

                if (expr instanceof PsiMethodCallExpression) {
                    PsiMethodCallExpression callExpr = (PsiMethodCallExpression) expr;
                    methodCallMap.put(callExpr, new CloneExpression(stringRep, location));
                    return;
                }
            }

            if (stat instanceof PsiIfStatement) {
                PsiIfStatement ifStmt = (PsiIfStatement) stat;
                ifStmtMap.put(ifStmt, new CloneExpression(stringRep, location));
                return;
            }

            if (stat instanceof PsiDeclarationStatement) {
                PsiDeclarationStatement declStmt = (PsiDeclarationStatement) stat;
                declarationMap.put(declStmt, new CloneExpression(stringRep, location));
                return;
            }

            if (stat instanceof PsiReturnStatement) {
                PsiReturnStatement returnStat = (PsiReturnStatement) stat;
                returnMap.put(returnStat, new CloneExpression(stringRep, location));
                return;
            }

            if (stat instanceof PsiForStatement) {
                PsiForStatement forStat = (PsiForStatement) stat;
                forLoopMap.put(forStat, new CloneExpression(stringRep, location));
                return;
            }

            if (stat instanceof PsiForeachStatement) {
                PsiForeachStatement forEachStat = (PsiForeachStatement) stat;
                forEachLoopMap.put(forEachStat, new CloneExpression(stringRep, location));
                return;
            }

            if (stat instanceof PsiWhileStatement) {
                PsiWhileStatement whileStat = (PsiWhileStatement) stat;
                whileLoopMap.put(whileStat, new CloneExpression(stringRep, location));
                return;
            }

            if (stat instanceof PsiDoWhileStatement) {
                PsiDoWhileStatement doWhileStat = (PsiDoWhileStatement) stat;
                doWhileLoopMap.put(doWhileStat, new CloneExpression(stringRep, location));
                return;
            }

            if (stat instanceof PsiSwitchStatement) {
                PsiSwitchStatement switchStat = (PsiSwitchStatement) stat;
                switchMap.put(switchStat, new CloneExpression(stringRep, location));
                return;
            }

            if (stat instanceof PsiAssertStatement) {
                PsiAssertStatement assertStat = (PsiAssertStatement) stat;
                assertMap.put(assertStat, new CloneExpression(stringRep, location));
                return;
            }

            if (stat instanceof PsiTryStatement) {
                PsiTryStatement tryStat = (PsiTryStatement) stat;
                tryMap.put(tryStat, new CloneExpression(stringRep, location));
                return;
            }

            if (stat instanceof PsiThrowStatement) {
                PsiThrowStatement throwStat = (PsiThrowStatement) stat;
                throwMap.put(throwStat, new CloneExpression(stringRep, location));
                return;
            }

            assert false : "Unknown statement type " + stat.toString();
        }

        private Set<Location> getClones(PsiStatement stat,
                                       Map<PsiDeclarationStatement, CloneExpression> declarationMap,
                                       Map<PsiAssignmentExpression, CloneExpression> assignmentMap,
                                       Map<PsiIfStatement, CloneExpression> ifStmtMap,
                                       Map<PsiMethodCallExpression, CloneExpression> methodCallMap,
                                       Map<PsiReturnStatement, CloneExpression> returnMap,
                                       Map<PsiForStatement, CloneExpression> forLoopMap,
                                       Map<PsiForeachStatement, CloneExpression> forEachLoopMap,
                                       Map<PsiWhileStatement, CloneExpression> whileLoopMap,
                                       Map<PsiDoWhileStatement, CloneExpression> doWhileLoopMap,
                                       Map<PsiSwitchStatement, CloneExpression> switchMap,
                                       Map<PsiAssertStatement, CloneExpression> assertMap,
                                       Map<PsiTryStatement, CloneExpression> tryMap,
                                       Map<PsiThrowStatement, CloneExpression> throwMap) {
            if (stat instanceof PsiExpressionStatement) {
                PsiExpression expr = ((PsiExpressionStatement) stat).getExpression();
                if (expr instanceof PsiAssignmentExpression) {
                    PsiAssignmentExpression assExpr = (PsiAssignmentExpression) expr;
                    CloneExpression cloneExpr = assignmentMap.get(assExpr);
                    return cloneExpr.getClones();
                }

                if (expr instanceof PsiMethodCallExpression) {
                    PsiMethodCallExpression callExpr = (PsiMethodCallExpression) expr;
                    CloneExpression cloneExpr = methodCallMap.get(callExpr);
                    return cloneExpr.getClones();
                }
            }

            if (stat instanceof PsiIfStatement) {
                PsiIfStatement ifStmt = (PsiIfStatement) stat;
                CloneExpression cloneExpr = ifStmtMap.get(ifStmt);
                return cloneExpr.getClones();
            }

            if (stat instanceof PsiDeclarationStatement) {
                PsiDeclarationStatement declStmt = (PsiDeclarationStatement) stat;
                CloneExpression cloneExpr = declarationMap.get(declStmt);
                return cloneExpr.getClones();
            }

            if (stat instanceof PsiReturnStatement) {
                PsiReturnStatement returnStmt = (PsiReturnStatement) stat;
                CloneExpression cloneExpr = returnMap.get(returnStmt);
                return cloneExpr.getClones();
            }

            if (stat instanceof PsiForStatement) {
                PsiForStatement forStat = (PsiForStatement) stat;
                CloneExpression cloneExpr = forLoopMap.get(forStat);
                return cloneExpr.getClones();
            }

            if (stat instanceof PsiForeachStatement) {
                PsiForeachStatement forEachStat = (PsiForeachStatement) stat;
                CloneExpression cloneExpr = forEachLoopMap.get(forEachStat);
                return cloneExpr.getClones();
            }

            if (stat instanceof PsiWhileStatement) {
                PsiWhileStatement whileStat = (PsiWhileStatement) stat;
                CloneExpression cloneExpr = whileLoopMap.get(whileStat);
                return cloneExpr.getClones();
            }

            if (stat instanceof PsiDoWhileStatement) {
                PsiDoWhileStatement doWhileStat = (PsiDoWhileStatement) stat;
                CloneExpression cloneExpr = doWhileLoopMap.get(doWhileStat);
                return cloneExpr.getClones();
            }

            if (stat instanceof PsiSwitchStatement) {
                PsiSwitchStatement switchStat = (PsiSwitchStatement) stat;
                CloneExpression cloneExpr = switchMap.get(switchStat);
                return cloneExpr.getClones();
            }

            if (stat instanceof PsiAssertStatement) {
                PsiAssertStatement assertStat = (PsiAssertStatement) stat;
                CloneExpression cloneExpr = assertMap.get(assertStat);
                return cloneExpr.getClones();
            }

            if (stat instanceof PsiTryStatement) {
                PsiTryStatement tryStat = (PsiTryStatement) stat;
                CloneExpression cloneExpr = tryMap.get(tryStat);
                return cloneExpr.getClones();
            }

            if (stat instanceof PsiThrowStatement) {
                PsiThrowStatement throwStat = (PsiThrowStatement) stat;
                CloneExpression cloneExpr = throwMap.get(throwStat);
                return cloneExpr.getClones();
            }

            assert false : "Unknown statement type " + stat.toString();
            return null;
        }
    }
}

