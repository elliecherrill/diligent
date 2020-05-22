package inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import feedback.Feedback;
import feedback.FeedbackHolder;
import feedback.FeedbackIdentifier;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import util.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class CloneInspection extends AbstractBaseJavaLocalInspectionTool {
    @Override
    @NotNull
    public String getDisplayName() {
        return "Similar code in code blocks and switch cases.";
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
        InspectionPriority priority = Utils.getInspectionPriority(holder, "clone");
        if (priority != InspectionPriority.NONE) {
            return new CloneVisitor(holder, priority);
        }

        return new JavaElementVisitor() {
        };
    }

    private static class CloneVisitor extends JavaElementVisitor {
        private final ProblemsHolder holder;
        private final FeedbackHolder feedbackHolder;
        private final InspectionPriority priority;

        CloneVisitor(ProblemsHolder holder, InspectionPriority priority) {
            this.holder = holder;
            this.priority = priority;
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
            Map<PsiPrefixExpression, CloneExpression> prefixMap = new HashMap<>();
            Map<PsiPostfixExpression, CloneExpression> postfixMap = new HashMap<>();

            PsiStatement[][] cases = CodeCloneUtils.getCaseBlocks(switchBody);
            cloneInit(cases, declarationMap, assignmentMap, ifStmtMap,
                    methodCallMap, returnMap, forLoopMap, forEachLoopMap,
                    whileLoopMap, doWhileLoopMap, switchMap, assertMap,
                    tryMap, throwMap, prefixMap, postfixMap);

            List<Integer> rangeOfCases = IntStream.range(0, cases.length).boxed().collect(Collectors.toList());
            List<Set<Integer>> clones = new ArrayList<>(cases.length);

            // If we have an entire case where duplicate / similar has been detected for every line in another case
            for (int i = 0; i < cases.length; i++) {
                //Only consider error when all cases are > 0 in length (excluding break;)
                if (cases[i].length < 1 || cases[i][0] == null) {
                    return;
                }

                Set<Location> intersection = getClones(cases[i][0],
                        declarationMap, assignmentMap,
                        ifStmtMap, methodCallMap,
                        returnMap, forLoopMap,
                        forEachLoopMap, whileLoopMap,
                        doWhileLoopMap,
                        switchMap, assertMap,
                        tryMap, throwMap,
                        prefixMap, postfixMap);

                if (intersection == null || intersection.size() == 0) {
                    intersection = new LinkedHashSet<>();
                    intersection.add(new Location(-1, -1));
                }

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
                            tryMap, throwMap,
                            prefixMap, postfixMap);

                    if (currClones == null) {
                        Set<Location> dummy = new LinkedHashSet<>();
                        dummy.add(new Location(-1, -1));
                        intersection = CodeCloneUtils.getCombinedClones(intersection, dummy);
                    } else {
                        intersection = CodeCloneUtils.getCombinedClones(intersection, currClones);
                    }
                }

                // Add itself to its clones
                Set<Integer> caseClones = new LinkedHashSet<>();
                caseClones.add(i);

                for (int blockIndex : rangeOfCases) {
                    if (CodeCloneUtils.containsBlockClone(intersection, blockIndex, true) != null) {
                        caseClones.add(blockIndex);
                    }
                }

                clones.add(caseClones);
            }

            String filename = statement.getContainingFile().getName();
            int line = Utils.getLineNumber(statement);
            FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(statement), line + "switch-clone", PsiStmtType.SWITCH, line);

            if (CodeCloneUtils.transitiveClosureOfClones(clones, rangeOfCases)) {
                Feedback feedback = new Feedback(line,
                        filename,
                        line + "-switch-clone",
                        priority,
                        Utils.getClassName(statement),
                        Utils.getMethodName(statement),
                        FeedbackType.SWITCH_CLONE);
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

            inspectPolyadicExpressions(aClass);

            Pair<PsiCodeBlock[], List<Integer>> codeBlocksWithParents = CodeCloneUtils.getAllCodeBlocks(aClass);
            PsiCodeBlock[] codeBlocks = codeBlocksWithParents.getFirst();
            List<Integer> parents = codeBlocksWithParents.getSecond();

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
            Map<PsiPrefixExpression, CloneExpression> prefixMap = new HashMap<>();
            Map<PsiPostfixExpression, CloneExpression> postfixMap = new HashMap<>();

            PsiStatement[][] blockBodies = CodeCloneUtils.getBlockBodies(codeBlocks);
            cloneInit(blockBodies, declarationMap, assignmentMap, ifStmtMap,
                    methodCallMap, returnMap, forLoopMap, forEachLoopMap,
                    whileLoopMap, doWhileLoopMap, switchMap, assertMap,
                    tryMap, throwMap, prefixMap, postfixMap);

            List<Integer> rangeOfBlocks = IntStream.range(0, blockBodies.length).boxed().collect(Collectors.toList());
            String filename = aClass.getContainingFile().getName();

            // If we have an entire method where duplicate / similar has been detected for every line in another method
            for (int i = 0; i < blockBodies.length; i++) {
                // Empty block
                if (blockBodies[i][0] == null) {
                    continue;
                }

                Set<Location> intersection = getClones(blockBodies[i][0],
                        declarationMap, assignmentMap,
                        ifStmtMap, methodCallMap,
                        returnMap, forLoopMap,
                        forEachLoopMap, whileLoopMap,
                        doWhileLoopMap,
                        switchMap, assertMap,
                        tryMap, throwMap,
                        prefixMap, postfixMap);

                if (intersection == null || intersection.size() == 0) {
                    intersection = new LinkedHashSet<>();
                    intersection.add(new Location(-1, -1));
                }

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
                            tryMap, throwMap,
                            prefixMap, postfixMap);

                    if (currClones == null) {
                        Set<Location> dummy = new LinkedHashSet<>();
                        dummy.add(new Location(-1, -1));
                        intersection = CodeCloneUtils.getCombinedClones(intersection, dummy);
                    } else {
                        intersection = CodeCloneUtils.getCombinedClones(intersection, currClones);
                    }
                }

                boolean hasClone = false;

                for (int blockIndex : rangeOfBlocks) {
                    FeedbackIdentifier feedbackId;
                    int line;
                    String methodName;
                    if (i > blockIndex) {
                        line = Utils.getLineNumber(codeBlocks[i]);
                        methodName = Utils.getMethodName(codeBlocks[i]);
                        feedbackId = new FeedbackIdentifier(Utils.getPointer(codeBlocks[i]), Utils.getPointer(codeBlocks[blockIndex]), blockIndex + "-block-clone", PsiStmtType.BLOCK, line);
                    } else {
                        line = Utils.getLineNumber(codeBlocks[blockIndex]);
                        methodName = Utils.getMethodName(codeBlocks[blockIndex]);
                        feedbackId = new FeedbackIdentifier(Utils.getPointer(codeBlocks[blockIndex]), Utils.getPointer(codeBlocks[i]), i + "-block-clone", PsiStmtType.BLOCK, line);
                    }

                    Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> cloneSequence = CodeCloneUtils.containsBlockClone(intersection, blockIndex, false);
                    if (cloneSequence != null) {
                        Feedback feedback = new Feedback(line,
                                filename,
                                line + "-block-clone",
                                priority,
                                aClass.getName(),
                                methodName,
                                FeedbackType.CLONE);
                        feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
                        hasClone = true;
                        //TODO: remove this after testing
                        try {
                            FileUtils.writeStringToFile(new File("clones.txt"),
                                    "CODE BLOCK CLONE >> \n " + CodeCloneUtils.printCodeBlock(codeBlocks[i], cloneSequence.getFirst()) + " \n >> \n" + CodeCloneUtils.printCodeBlock(codeBlocks[blockIndex], cloneSequence.getSecond()) + "\n",
                                    StandardCharsets.UTF_8, true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                    }
                }

                if (hasClone) {
                    i = CodeCloneUtils.getNextNonNested(parents, i);
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
                               Map<PsiThrowStatement, CloneExpression> throwMap,
                               Map<PsiPrefixExpression, CloneExpression> prefixMap,
                               Map<PsiPostfixExpression, CloneExpression> postfixMap) {
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
                                tryMap, throwMap,
                                prefixMap, postfixMap);
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
            compareStatements(prefixMap);
            compareStatements(postfixMap);
        }

        private <T extends PsiElement> void compareStatements(Map<T, CloneExpression> map) {
            for (Map.Entry<T, CloneExpression> entry : map.entrySet()) {
                T entryKey = entry.getKey();
                CloneExpression entryValue = entry.getValue();
                String[] entryStringRep = entryValue.getStringRep();

                for (Map.Entry<T, CloneExpression> otherEntry : map.entrySet()) {
                    T otherEntryKey = otherEntry.getKey();
                    CloneExpression otherEntryValue = otherEntry.getValue();
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
                        if (CodeCloneUtils.sameIfCondition(entryStringRep, otherEntryStringRep)) {
                            PsiIfStatement ifStmt = (PsiIfStatement) entryKey;
                            PsiIfStatement otherIfStmt = (PsiIfStatement) otherEntryKey;

                            if (areSimilarBlocks(ifStmt.getThenBranch(), otherIfStmt.getThenBranch())) {
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
                        if (CodeCloneUtils.changeInRHS(entryStringRep, otherEntryStringRep)) {
                            update = true;
                        } else if (CodeCloneUtils.changeInOp(entryStringRep, otherEntryStringRep)) {
                            update = true;
                        } else if (CodeCloneUtils.changeInLHS(entryStringRep, otherEntryStringRep)) {
                            update = true;
                        }
                    } else if (entryKey instanceof PsiForStatement) {
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
                    } else if (entryKey instanceof PsiPrefixExpression) {
                        if (CodeCloneUtils.prefixExprChangeInVar(entryStringRep, otherEntryStringRep)) {
                            update = true;
                        } else if (CodeCloneUtils.prefixExprChangeInOp(entryStringRep, otherEntryStringRep)) {
                            update = true;
                        }
                    } else if (entryKey instanceof PsiPostfixExpression) {
                        if (CodeCloneUtils.postfixExprChangeInVar(entryStringRep, otherEntryStringRep)) {
                            update = true;
                        } else if (CodeCloneUtils.postfixExprChangeInOp(entryStringRep, otherEntryStringRep)) {
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
            Map<PsiPrefixExpression, CloneExpression> prefixMap = new HashMap<>();
            Map<PsiPostfixExpression, CloneExpression> postfixMap = new HashMap<>();

            PsiCodeBlock body = switchStmt.getBody();
            PsiCodeBlock otherBody = otherSwitchStmt.getBody();

            if (body == null && otherBody == null) {
                return true;
            }

            if (body == null || otherBody == null) {
                return false;
            }

            PsiStatement[][] blocks = CodeCloneUtils.getSameCaseBlocks(body, otherBody);
            cloneInit(blocks, declarationMap, assignmentMap, ifStmtMap, methodCallMap, returnMap,
                    forLoopMap, forEachLoopMap, whileLoopMap, doWhileLoopMap, switchMap, assertMap,
                    tryMap, throwMap, prefixMap, postfixMap);

            // For every array of statements need to find clones for every statement in the *following* array
            for (int i = 0; i < blocks.length; i += 2) {
                if (blocks[i][0] == null) {
                    continue;
                }

                Set<Location> intersection = getClones(blocks[i][0],
                        declarationMap, assignmentMap,
                        ifStmtMap, methodCallMap,
                        returnMap, forLoopMap,
                        forEachLoopMap, whileLoopMap,
                        doWhileLoopMap,
                        switchMap, assertMap,
                        tryMap, throwMap,
                        prefixMap, postfixMap);

                if (intersection == null || intersection.size() == 0) {
                    intersection = new LinkedHashSet<>();
                    intersection.add(new Location(-1, -1));
                }

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
                            tryMap, throwMap,
                            prefixMap, postfixMap);

                    if (currClones == null) {
                        Set<Location> dummy = new LinkedHashSet<>();
                        dummy.add(new Location(-1, -1));
                        intersection = CodeCloneUtils.getCombinedClones(intersection, dummy);
                    } else {
                        intersection = CodeCloneUtils.getCombinedClones(intersection, currClones);
                    }
                }

                // Must be a clone of the following block
                //TODO: should we pass true here?
                if (CodeCloneUtils.containsBlockClone(intersection, i + 1, false) == null) {
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
            Map<PsiPrefixExpression, CloneExpression> prefixMap = new HashMap<>();
            Map<PsiPostfixExpression, CloneExpression> postfixMap = new HashMap<>();

            PsiStatement[][] blocks = CodeCloneUtils.getBlocks(stat, otherStat);
            cloneInit(blocks, declarationMap, assignmentMap, ifStmtMap, methodCallMap, returnMap,
                    forLoopMap, forEachLoopMap, whileLoopMap, doWhileLoopMap, switchMap, assertMap,
                    tryMap, throwMap, prefixMap, postfixMap);

            if (blocks[0][0] == null && blocks[1][0] == null) {
                return true;
            }

            if (blocks[0][0] == null || blocks[1][0] == null) {
                return false;
            }

            //Both not empty - need to check they are the same

            for (int i = 0; i < blocks.length; i++) {
                Set<Location> intersection = getClones(blocks[i][0],
                        declarationMap, assignmentMap,
                        ifStmtMap, methodCallMap,
                        returnMap, forLoopMap,
                        forEachLoopMap, whileLoopMap,
                        doWhileLoopMap,
                        switchMap, assertMap,
                        tryMap, throwMap,
                        prefixMap, postfixMap);

                if (intersection == null || intersection.size() == 0) {
                    intersection = new LinkedHashSet<>();
                    intersection.add(new Location(-1, -1));
                }

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
                            tryMap, throwMap,
                            prefixMap, postfixMap);
                    if (currClones == null) {
                        Set<Location> dummy = new LinkedHashSet<>();
                        dummy.add(new Location(-1, -1));
                        intersection = CodeCloneUtils.getCombinedClones(intersection, dummy);
                    } else {
                        intersection = CodeCloneUtils.getCombinedClones(intersection, currClones);
                    }
                }

                int otherBlock = i == 0 ? 1 : 0;
                //TODO: true here?
                if (CodeCloneUtils.containsBlockClone(intersection, otherBlock, false) != null) {
                    return true;
                }
            }

            return false;
        }

        private void updateCloneSet(CloneExpression cloneExpr, CloneExpression otherCloneExpr) {
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
                                  Map<PsiThrowStatement, CloneExpression> throwMap,
                                  Map<PsiPrefixExpression, CloneExpression> prefixMap,
                                  Map<PsiPostfixExpression, CloneExpression> postfixMap) {
            String[] stringRep = TokeniseUtils.getStmtAsStringArray(stat);

            int statementCount = CodeCloneUtils.getStatementCount(stat);
            location.setStatementCount(statementCount);

            if (stringRep == null) {
                return;
            }

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
                if (expr instanceof PsiPrefixExpression) {
                    PsiPrefixExpression prefixExpr = (PsiPrefixExpression) expr;
                    prefixMap.put(prefixExpr, new CloneExpression(stringRep, location));
                    return;
                }
                if (expr instanceof PsiPostfixExpression) {
                    PsiPostfixExpression postfixExpr = (PsiPostfixExpression) expr;
                    postfixMap.put(postfixExpr, new CloneExpression(stringRep, location));
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
                                        Map<PsiThrowStatement, CloneExpression> throwMap,
                                        Map<PsiPrefixExpression, CloneExpression> prefixMap,
                                        Map<PsiPostfixExpression, CloneExpression> postfixMap) {
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
                if (expr instanceof PsiPrefixExpression) {
                    PsiPrefixExpression prefixExpr = (PsiPrefixExpression) expr;
                    CloneExpression cloneExpr = prefixMap.get(prefixExpr);
                    return cloneExpr.getClones();
                }
                if (expr instanceof PsiPostfixExpression) {
                    PsiPostfixExpression postfixExpr = (PsiPostfixExpression) expr;
                    CloneExpression cloneExpr = postfixMap.get(postfixExpr);
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

            return null;
        }

        private void inspectPolyadicExpressions(PsiClass aClass) {
            PsiPolyadicExpression[] polyExprs = CodeCloneUtils.getAllPolyadicExpressions(aClass);
            if (polyExprs.length <= 1) {
                return;
            }

            Map<PsiPolyadicExpression, String[]> polyadicMap = new HashMap<>();
            Map<PsiPolyadicExpression, Integer> polyadicLocationMap = new HashMap<>();

            for (int i = 0; i < polyExprs.length; i++) {
                PsiPolyadicExpression expr = polyExprs[i];
                polyadicMap.put(expr, TokeniseUtils.getPolyExprAsStringArray(expr));
                polyadicLocationMap.put(expr, i);
            }

            String filename = aClass.getContainingFile().getName();

            for (Map.Entry<PsiPolyadicExpression, String[]> expr : polyadicMap.entrySet()) {
                PsiPolyadicExpression exprKey = expr.getKey();
                String[] exprStringRep = expr.getValue();

                for (Map.Entry<PsiPolyadicExpression, String[]> otherExpr : polyadicMap.entrySet()) {
                    PsiPolyadicExpression otherExprKey = otherExpr.getKey();
                    String[] otherExprStringRep = otherExpr.getValue();

                    if (exprKey.equals(otherExprKey)) {
                        continue;
                    }

                    FeedbackIdentifier feedbackId;
                    int line;
                    String methodName;
                    if (polyadicLocationMap.get(exprKey) > polyadicLocationMap.get(otherExprKey)) {
                        line = Utils.getLineNumber(exprKey);
                        methodName = Utils.getMethodName(exprKey);
                        feedbackId = new FeedbackIdentifier(Utils.getPointer(exprKey), Utils.getPointer(otherExprKey), polyadicLocationMap.get(otherExprKey) + "-polyadic-clone", PsiStmtType.POLYADIC_EXPR, line);
                    } else {
                        line = Utils.getLineNumber(otherExprKey);
                        methodName = Utils.getMethodName(otherExprKey);
                        feedbackId = new FeedbackIdentifier(Utils.getPointer(otherExprKey), Utils.getPointer(exprKey), polyadicLocationMap.get(exprKey) + "-polyadic-clone", PsiStmtType.POLYADIC_EXPR, line);
                    }

                    if (Arrays.equals(exprStringRep, otherExprStringRep)) {
                        Feedback feedback = new Feedback(line,
                                filename,
                                line + "-polyadic-clone",
                                priority,
                                aClass.getName(),
                                methodName,
                                FeedbackType.EXPR_CLONE);
                        feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
                    } else {
                        feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                    }
                }
            }
        }
    }
}

