package util;

import com.intellij.psi.*;

import javax.annotation.Nonnull;
import java.util.*;

public class CodeCloneUtils {

    public static PsiStatement[][] getCaseBlocks(@Nonnull PsiCodeBlock body) {
        // Ignoring default case
        PsiStatement[] bodyStatements = body.getStatements();

        int numCases = 0;
        int numStats = 0;
        boolean defaultCase = false;
        List<Integer> statements = new ArrayList<>();

        for (PsiStatement stat : bodyStatements) {
            if (stat instanceof PsiSwitchLabelStatement) {
                PsiSwitchLabelStatement labelStat = (PsiSwitchLabelStatement) stat;

                if (labelStat.isDefaultCase()) {
                    defaultCase = true;
                    continue;
                }

                defaultCase = false;
                numCases++;
                statements.add(numStats);
                numStats = 0;
            } else if (!defaultCase && !(stat instanceof PsiBreakStatement)) {
                numStats++;
            }
        }
        statements.add(numStats);

        PsiStatement[][] caseBlocks = new PsiStatement[numCases][Collections.max(statements)];
        int caseIndex = -1;
        int statIndex = 0;
        defaultCase = false;
        for (PsiStatement stat : bodyStatements) {
            if (stat instanceof PsiSwitchLabelStatement) {
                PsiSwitchLabelStatement labelStat = (PsiSwitchLabelStatement) stat;

                if (labelStat.isDefaultCase()) {
                    defaultCase = true;
                    continue;
                }

                defaultCase = false;
                caseIndex++;
                statIndex = 0;
            } else if (!defaultCase && !(stat instanceof PsiBreakStatement)) {
                caseBlocks[caseIndex][statIndex] = stat;
                statIndex++;
            }
        }

        return caseBlocks;
    }

    public static PsiStatement[][] getSameCaseBlocks(@Nonnull PsiCodeBlock body, @Nonnull PsiCodeBlock otherBody) {
        // Ignoring default case

        int sameCases = 0;
        // Returns a pair of case labels and number of statements in cases
        Pair<List<String>, List<Integer>> otherLabels = getCaseLabels(otherBody);

        PsiStatement[] bodyStatements = body.getStatements();
        List<Integer> statements = new ArrayList<>();

        List<Integer> bodyIndices = new ArrayList<>();
        List<Integer> otherBodyIndices = new ArrayList<>();

        int caseIndex = -1;
        int numStats = 0;
        for (PsiStatement stat : bodyStatements) {
            if (stat instanceof PsiSwitchLabelStatement) {
                caseIndex++;
                String caseLabel = getCaseLabel((PsiSwitchLabelStatement) stat);
                int index = otherLabels.getFirst().indexOf(caseLabel);
                if (index > -1) {
                    sameCases++;
                    statements.add(numStats);
                    otherBodyIndices.add(index);
                    bodyIndices.add(caseIndex);
                }
                numStats = 0;
            } else if (!(stat instanceof PsiBreakStatement)) {
                numStats++;
            }
        }
        statements.add(numStats);

        int maxStatements = Math.max(Collections.max(statements), Collections.max(otherLabels.getSecond()));

        PsiStatement[][] otherCaseBlocks = getCaseStatements(otherBody, otherLabels.getFirst().size(), maxStatements);

        PsiStatement[][] caseBlocks = new PsiStatement[sameCases * 2][maxStatements];
        caseIndex = -1;
        int insertCaseIndex = -2;
        int statIndex = 0;
        for (PsiStatement stat : bodyStatements) {
            if (stat instanceof PsiSwitchLabelStatement) {
                caseIndex++;
                statIndex = 0;

                if (bodyIndices.contains(caseIndex)) {
                    insertCaseIndex += 2;
                }

            } else if (!(stat instanceof PsiBreakStatement)) {
                if (bodyIndices.contains(caseIndex)) {
                    caseBlocks[insertCaseIndex][statIndex] = stat;
                    statIndex++;
                }
            }
        }

        insertCaseIndex = 1;
        for (int i : otherBodyIndices) {
            caseBlocks[insertCaseIndex] = otherCaseBlocks[i];
            insertCaseIndex += 2;
        }

        return caseBlocks;
    }

    private static String getCaseLabel(PsiSwitchLabelStatement switchLabel) {
        StringBuffer sb = new StringBuffer();

        PsiElement[] children = switchLabel.getChildren();

        for (PsiElement child : children) {
            if (child instanceof PsiExpressionList) {
                PsiExpressionList exprList = (PsiExpressionList) child;
                PsiExpression[] exprs = exprList.getExpressions();
                for (PsiExpression expr : exprs) {
                    sb.append(TokeniseUtils.getExprAsString(expr));
                }
                break;
            }
        }

        return sb.toString();
    }

    private static Pair<List<String>, List<Integer>> getCaseLabels(PsiCodeBlock body) {
        List<String> labels = new ArrayList<>();
        List<Integer> statements = new ArrayList<>();
        PsiStatement[] bodyStatements = body.getStatements();

        String currCase;
        int currStats = 0;
        for (PsiStatement stat : bodyStatements) {
            if (stat instanceof PsiSwitchLabelStatement) {
                currCase = getCaseLabel((PsiSwitchLabelStatement) stat);
                labels.add(currCase);

                statements.add(currStats);
                currStats = 0;
            } else if (!(stat instanceof PsiBreakStatement)) {
                currStats++;
            }
        }
        statements.add(currStats);

        return new Pair<>(labels, statements);
    }

    private static PsiStatement[][] getCaseStatements(PsiCodeBlock body, int numCases, int maxStats) {
        PsiStatement[][] caseBlocks = new PsiStatement[numCases][maxStats];

        int caseIndex = -1;
        int statIndex = 0;
        for (PsiStatement stat : body.getStatements()) {
            if (stat instanceof PsiSwitchLabelStatement) {
                caseIndex++;
                statIndex = 0;
            } else if (!(stat instanceof PsiBreakStatement)) {
                caseBlocks[caseIndex][statIndex] = stat;
                statIndex++;
            }
        }

        return caseBlocks;
    }

    public static PsiStatement[][] getBlockBodies(PsiCodeBlock[] blocks) {
        int numBlocks = blocks.length;
        int maxStatementCount = Arrays.stream(blocks).map(PsiCodeBlock::getStatementCount).max(Integer::compare).get();

        PsiStatement[][] blockBodies = new PsiStatement[numBlocks][maxStatementCount];
        int statIndex = 0;
        for (int blockIndex = 0; blockIndex < numBlocks; blockIndex++) {
            PsiCodeBlock b = blocks[blockIndex];
            if (b.isEmpty()) {
                continue;
            }

            PsiStatement[] bodyStats = b.getStatements();

            for (PsiStatement s : bodyStats) {
                blockBodies[blockIndex][statIndex] = s;
                statIndex++;
            }
            statIndex = 0;
        }

        return blockBodies;
    }

    public static PsiStatement[][] getBlocks(PsiStatement blockBody, PsiStatement otherBlockBody) {
        List<Integer> statements = new ArrayList<>();

        statements.add(getNumStats(blockBody));
        statements.add(getNumStats(otherBlockBody));

        PsiStatement[][] blocks = new PsiStatement[2][Collections.max(statements)];

        if (blockBody != null) {
            addStats(blocks, 0, blockBody);
        }

        if (otherBlockBody != null) {
            addStats(blocks, 1, otherBlockBody);
        }

        return blocks;
    }

    private static int getNumStats(PsiStatement branch) {
        if (branch == null) {
            return 0;
        }

        int numStats = 0;
        if (branch instanceof PsiBlockStatement) {
            PsiBlockStatement branchBlock = (PsiBlockStatement) branch;
            PsiCodeBlock branchCodeBlock = branchBlock.getCodeBlock();
            PsiStatement[] branchStats = branchCodeBlock.getStatements();
            for (PsiStatement s : branchStats) {
                if (!(s instanceof PsiWhiteSpace) && !(s instanceof PsiComment)) {
                    numStats++;
                }
            }
        }
        return numStats;
    }

    private static void addStats(PsiStatement[][] ifBlocks, int index, PsiStatement branch) {
        int statIndex = 0;
        if (branch instanceof PsiBlockStatement) {
            PsiBlockStatement branchBlock = (PsiBlockStatement) branch;
            PsiCodeBlock branchCodeBlock = branchBlock.getCodeBlock();
            PsiStatement[] branchStats = branchCodeBlock.getStatements();
            for (PsiStatement s : branchStats) {
                if (!(s instanceof PsiWhiteSpace) && !(s instanceof PsiComment)) {
                    ifBlocks[index][statIndex] = s;
                    statIndex++;
                }
            }
        }
    }

    public static boolean changeInLHS(String[] first, String[] second) {
        // RHS, type of LHS, operator the same
        // LHS different
        int firstTypeIndex = getStartIndex("LHS-TYPE", first) + 1;
        int firstEndTypeIndex = getStartIndex("LHS-VAR", first);

        int secondTypeIndex = getStartIndex("LHS-TYPE", first) + 1;
        int secondEndTypeIndex = getStartIndex("LHS-VAR", first);

        int firstOpIndex = getStartIndex("OP", first);
        int secondOpIndex = getStartIndex("OP", second);

        return Arrays.equals(first, firstTypeIndex, firstEndTypeIndex, second, secondTypeIndex, secondEndTypeIndex) &&
                Arrays.equals(first, firstOpIndex, first.length, second, secondOpIndex, second.length);
    }

    public static boolean changeInRHS(String[] first, String[] second) {
        // LHS and operator the same
        // RHS different
        int firstRhsIndex = getStartIndex("RHS", first);
        int secondRhsIndex = getStartIndex("RHS", second);

        return Arrays.equals(first, 0, firstRhsIndex, second, 0, secondRhsIndex);
    }

    public static boolean changeInOp(String[] first, String[] second) {
        // LHS and RHS the same
        // Operator different
        int firstOpIndex = getStartIndex("OP", first);
        int firstEndOpIndex = getStartIndex("RHS", first);

        int secondOpIndex = getStartIndex("OP", second);
        int secondEndOpIndex = getStartIndex("RHS", second);

        return Arrays.equals(first, 0, firstOpIndex, second, 0, secondOpIndex) &&
                Arrays.equals(first, firstEndOpIndex, first.length, second, secondEndOpIndex, second.length);
    }

    public static boolean prefixExprChangeInVar(String[] first, String[] second) {
        // Same operator and type of var, Change in var name
        int firstTypeIndex = getStartIndex("PREFIX-TYPE", first) + 1;
        int firstEndTypeIndex = getStartIndex("PREFIX-VAR", first);

        int secondTypeIndex = getStartIndex("PREFIX-TYPE", first) + 1;
        int secondEndTypeIndex = getStartIndex("PREFIX-VAR", first);

        int firstOpIndex = getStartIndex("PREFIX-OP", first);
        int secondOpIndex = getStartIndex("PREFIX-OP", second);

        return Arrays.equals(first, firstTypeIndex, firstEndTypeIndex, second, secondTypeIndex, secondEndTypeIndex) &&
                Arrays.equals(first, firstOpIndex, first.length, second, secondOpIndex, second.length);
    }

    public static boolean prefixExprChangeInOp(String[] first, String[] second) {
        // Same var name and type, Change in op
        int firstEndVarIndex = getStartIndex("PREFIX-OP", first);
        int secondEndVarIndex = getStartIndex("PREFIX-OP", second);

        return Arrays.equals(first, 0, firstEndVarIndex, second, 0, secondEndVarIndex);
    }

    public static boolean postfixExprChangeInVar(String[] first, String[] second) {
        // Same operator and type of var, Change in var name
        int firstTypeIndex = getStartIndex("POSTFIX-TYPE", first) + 1;
        int firstEndTypeIndex = getStartIndex("POSTFIX-VAR", first);

        int secondTypeIndex = getStartIndex("POSTFIX-TYPE", first) + 1;
        int secondEndTypeIndex = getStartIndex("POSTFIX-VAR", first);

        int firstOpIndex = getStartIndex("POSTFIX-OP", first);
        int secondOpIndex = getStartIndex("POSTFIX-OP", second);

        return Arrays.equals(first, firstTypeIndex, firstEndTypeIndex, second, secondTypeIndex, secondEndTypeIndex) &&
                Arrays.equals(first, firstOpIndex, first.length, second, secondOpIndex, second.length);
    }

    public static boolean postfixExprChangeInOp(String[] first, String[] second) {
        // Same var name and type, Change in op
        int firstEndVarIndex = getStartIndex("POSTFIX-OP", first);
        int secondEndVarIndex = getStartIndex("POSTFIX-OP", second);

        return Arrays.equals(first, 0, firstEndVarIndex, second, 0, secondEndVarIndex);
    }

    public static boolean sameIfCondition(String[] first, String[] second) {
        int firstCondIndex = getStartIndex("IF-COND", first) + 1;
        int firstCondEndIndex = getStartIndex("IF-THEN", first);
        int secondCondIndex = getStartIndex("IF-COND", second) + 1;
        int secondCondEndIndex = getStartIndex("IF-THEN", second);

        return Arrays.equals(first, firstCondIndex, firstCondEndIndex, second, secondCondIndex, secondCondEndIndex);
    }

    public static boolean sameWhileCondition(String[] first, String[] second) {
        int firstCondIndex = getStartIndex("WHILE-COND", first) + 1;
        int firstCondEndIndex = getStartIndex("END-WHILE-COND", first);
        int secondCondIndex = getStartIndex("WHILE-COND", second) + 1;
        int secondCondEndIndex = getStartIndex("END-WHILE-COND", second);

        return Arrays.equals(first, firstCondIndex, firstCondEndIndex, second, secondCondIndex, secondCondEndIndex);
    }

    public static boolean sameDoWhileCondition(String[] first, String[] second) {
        int firstCondIndex = getStartIndex("DOWHILE-COND", first) + 1;
        int firstCondEndIndex = getStartIndex("END-DOWHILE-COND", first);
        int secondCondIndex = getStartIndex("DOWHILE-COND", second) + 1;
        int secondCondEndIndex = getStartIndex("END-DOWHILE-COND", second);

        return Arrays.equals(first, firstCondIndex, firstCondEndIndex, second, secondCondIndex, secondCondEndIndex);
    }

    public static boolean conditionChangeInRhs(String[] first, String[] second) {
        // LHS and operator the same
        // RHS different
        int firstRhsIndex = getStartIndex("BINEXPRRHS", first);
        int secondRhsIndex = getStartIndex("BINEXPRRHS", second);

        return Arrays.equals(first, 0, firstRhsIndex, second, 0, secondRhsIndex);
    }

    public static boolean conditionChangeInLhs(String[] first, String[] second) {
        // RHS and operator the same
        // LHS different
        int firstOpIndex = getStartIndex("BINEXPROP", first);
        int secondOpIndex = getStartIndex("BINEXPROP", second);

        return Arrays.equals(first, firstOpIndex, first.length, second, secondOpIndex, second.length);
    }

    public static boolean conditionPartial(String[] first, String[] second) {
        String[] firstCond = Arrays.copyOfRange(first, getStartIndex("IF-COND", first) + 1, getStartIndex("IF-THEN", first));
        String[] secondCond = Arrays.copyOfRange(second, getStartIndex("IF-COND", second) + 1, getStartIndex("IF-THEN", second));


        List<Pair<Integer, Integer>> firstBinExpr = getAllBinExpr(firstCond);
        List<Pair<Integer, Integer>> secondBinExpr = getAllBinExpr(secondCond);

        for (Pair<Integer, Integer> firstIndex : firstBinExpr) {
            for (Pair<Integer, Integer> secondIndex : secondBinExpr) {
                if (equalWithCondVar(firstCond, firstIndex, secondCond, secondIndex)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean equalWithCondVar(String[] first, Pair<Integer, Integer> firstIndex, String[] second, Pair<Integer, Integer> secondIndex) {
        int length = firstIndex.getSecond() - firstIndex.getFirst();

        if (length != secondIndex.getSecond() - secondIndex.getFirst()) {
            return false;
        }

        for (int i = 0; i < length; i++) {
                if (!first[firstIndex.getFirst() + i].equals(second[secondIndex.getFirst() + i]) &&
                        !first[firstIndex.getFirst() + i].startsWith("CONDVAR") &&
                        !second[secondIndex.getFirst() + i].startsWith("CONDVAR")) {
                    return false;
                }

        }

        return true;
    }

    private static List<Pair<Integer, Integer>> getAllBinExpr(String[] arr) {
        List<Pair<Integer, Integer>> allBinExpr = new ArrayList<>();
        Stack<Integer> startIndexes = new Stack<>();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals("BINEXPR")) {
                startIndexes.push(i);
            } else if (arr[i].equals("END-BINEXPR")) {
                assert !startIndexes.empty() : "Stack empty, no corresponding start token " +print(arr);
                allBinExpr.add(new Pair<>(startIndexes.pop(), i + 1));
            }
        }

        return allBinExpr;
    }

    public static boolean sameIfBody(String[] first, String[] second) {
        int firstThenIndex = getStartIndex("IF-THEN", first);
        int secondThenIndex = getStartIndex("IF-THEN", second);

        return equalWithCondVar(first, new Pair<>(firstThenIndex, first.length), second, new Pair<>(secondThenIndex, second.length));
    }

    public static boolean sameForSetup(String[] first, String[] second) {
        int firstBodyIndex = getStartIndex("FOR-BODY", first);
        int secondBodyIndex = getStartIndex("FOR-BODY", second);

        return Arrays.equals(first, 0, firstBodyIndex, second, 0, secondBodyIndex);
    }

    public static boolean sameForEachSetup(String[] first, String[] second) {
        int firstTypeIndex = getStartIndex("FOREACH-TYPE", first) + 1;
        int secondTypeIndex = getStartIndex("FOREACH-TYPE", second) + 1;

        if (!(first[firstTypeIndex].equals(second[secondTypeIndex]))) {
            return false;
        }

        int firstIteratedIndex = getStartIndex("FOREACH-IN", first) + 1;
        int secondIteratedIndex = getStartIndex("FOREACH-IN", second) + 1;

        return first[firstIteratedIndex].equals(second[secondIteratedIndex]);
    }

    public static boolean declChangeInVarName(String[] first, String[] second) {
        // int z = 0 and int x = 0 are the same we just have called them different names
        // Same type and initialiser (if they have one)
        // Different variable name
        int firstNameIndex = getStartIndex("NAME", first);
        int firstEndNameIndex = getStartIndex("INIT", first);
        int secondNameIndex = getStartIndex("NAME", second);
        int secondEndNameIndex = getStartIndex("INIT", second);

        boolean sameType = Arrays.equals(first, 0, firstNameIndex, second, 0, secondNameIndex);

        if (!sameType) {
            return false;
        }

        if (firstEndNameIndex == -1 && secondEndNameIndex == -1) {
            return true;
        }

        if (firstEndNameIndex != -1 && secondEndNameIndex != -1) {
            return Arrays.equals(first, firstEndNameIndex, first.length, second, secondEndNameIndex, second.length);
        }

        return false;
    }

    public static boolean sameSwitchVar(String[] first, String[] second) {
        int firstVarIndex = getStartIndex("SWITCH-VAR", first) + 1;
        int firstEndVarIndex = getStartIndex("END-SWITCH-VAR", first);
        int secondVarIndex = getStartIndex("SWITCH-VAR", second) + 1;
        int secondEndVarIndex = getStartIndex("END-SWITCH-VAR", second);

        return Arrays.equals(first, firstVarIndex, firstEndVarIndex, second, secondVarIndex, secondEndVarIndex);
    }

    private static int getStartIndex(String toFind, String[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(toFind)) {
                return i;
            }
        }

        return -1;
    }

    public static boolean transitiveClosureOfClones(List<Set<Integer>> cases, List<Integer> aim) {
        if (cases.isEmpty()) {
            return false;
        }

        Set<Integer> currClones = new LinkedHashSet<>(cases.get(0));

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

    public static Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> containsBlockClone(Set<Location> clones, int blockIndex, boolean innerBlock) {
        if (clones == null || clones.isEmpty()) {
            return null;
        }

        Set<Location> clonesInBlock = getClonesInCodeBlock(clones, blockIndex, true);

        if (clonesInBlock.isEmpty()) {
            return null;
        }

        int longestSeq = 0;
        int seqStart = -1;
        int seqEnd = -1;
        int otherStart = -1;
        int otherEnd = -1;

        int prev = -1;
        int currSeq = 0;
        int currStart = 0;
        int currOtherStart = 0;

        int index = 0;
        for (Location l : clonesInBlock) {
            if (index == 0) {
                currOtherStart = l.getLine();
            }

            if (l.getLine() != prev + 1) {
                if (currSeq > longestSeq) {
                    longestSeq = currSeq;

                    seqStart = currStart;
                    seqEnd = index;

                    otherStart = currOtherStart;
                    otherEnd = prev + 1;
                }

                currStart = index;
                currOtherStart = l.getLine();
                prev = l.getLine();
                currSeq = 0;
            } else {
                prev = l.getLine();
            }

            currSeq += l.getStatementCount();
            index++;
        }

        if (currSeq > longestSeq) {
            longestSeq = currSeq;

            seqStart = currStart;
            seqEnd = index;

            otherStart = currOtherStart;
            otherEnd = prev + 1;
        }

        if ((innerBlock && longestSeq > 0) || longestSeq > 1) {
            return new Pair<>(new Pair<>(seqStart, seqEnd), new Pair<>(otherStart, otherEnd));
        }

        return null;
    }

    public static Set<Location> getCombinedClones(Set<Location> firstClones, Set<Location> secondClones) {
        if (firstClones == null) {
            return null;
        }

        Set<Location> combinedClones = new LinkedHashSet<>();
        Set<Location> toAdd = new LinkedHashSet<>();
        List<Integer> seenCodeBlocks = new ArrayList<>();

        // To allow additional lines at start of code block
        boolean prevIsNull = true;
        // To allow additional lines at end of code block
        boolean followingIsNull = false;

        if (!getClonesInCodeBlock(secondClones, -1, false).isEmpty()) {
            combinedClones.addAll(firstClones);
            combinedClones.add(new Location(-1, -1));
            return combinedClones;
        }

        if (onlyContainsNullCase(firstClones)) {
            combinedClones.addAll(firstClones);
            combinedClones.addAll(secondClones);
            return combinedClones;
        }

        for (Location location : firstClones) {
            int currCodeBlock = location.getCodeBlock();

            if ((currCodeBlock == -1) && !prevIsNull) {
                followingIsNull = true;
            }

            if ((currCodeBlock != -1) && followingIsNull) {
                return null;
            }

            if (currCodeBlock != -1) {
                prevIsNull = false;

                if (!(seenCodeBlocks.contains(currCodeBlock))) {
                    seenCodeBlocks.add(currCodeBlock);

                    Set<Location> secondInBlock = getClonesInCodeBlock(secondClones, currCodeBlock, false);

                    if (!(secondInBlock.isEmpty())) {
                        combinedClones.addAll(getClonesInCodeBlock(firstClones, currCodeBlock, false));
                        toAdd.addAll(secondInBlock);
                    }

                }
            } else {
                combinedClones.add(location);
            }
        }

        combinedClones.addAll(toAdd);

        return combinedClones;
    }

    private static boolean onlyContainsNullCase(Set<Location> clones) {
        if (clones == null) {
            return false;
        }

        return clones.size() == getClonesInCodeBlock(clones, -1, false).size();
    }

    public static Set<Location> getClonesInCodeBlock(Set<Location> clones, int codeBlock, boolean showAdditional) {
        Set<Location> clonesInCodeBlock = new LinkedHashSet<>();

        for (Location l : clones) {
            if (l.getCodeBlock() == codeBlock) {
                clonesInCodeBlock.add(l);
            }
            if (showAdditional && l.getCodeBlock() == -1) {
                clonesInCodeBlock.add(l);
            }
        }

        return clonesInCodeBlock;
    }

    public static String printCodeBlock(PsiCodeBlock codeBlock, Pair<Integer, Integer> sequence) {
        StringBuffer sb = new StringBuffer();

        int endIndex = Math.min(codeBlock.getStatementCount(), sequence.getSecond());

        for (int i = sequence.getFirst(); i < endIndex; i++) {
            sb.append(codeBlock.getStatements()[i].getText());
            sb.append(" ");
        }

        return sb.toString();
    }

    public static Pair<PsiCodeBlock[], List<Integer>> getAllCodeBlocks(PsiClass aClass) {
        Pair<List<PsiCodeBlock>, List<Integer>> codeBlocks = getCodeBlocks(aClass, -1);
        return new Pair<>(codeBlocks.getFirst().toArray(new PsiCodeBlock[0]), codeBlocks.getSecond());
    }

    private static Pair<List<PsiCodeBlock>, List<Integer>> getCodeBlocks(PsiElement elem, int parent) {
        List<PsiCodeBlock> codeBlocks = new ArrayList<>();
        List<Integer> parentIndex = new ArrayList<>();

        int newParent = parent;
        for (PsiElement child : elem.getChildren()) {
            if (child instanceof PsiCodeBlock && !(elem instanceof PsiSwitchStatement)) {
                PsiCodeBlock block = (PsiCodeBlock) child;
                // Only consider code blocks with more than one statement
                if (getCodeBlockCount(block) > 1) {
                    codeBlocks.add(block);
                    parentIndex.add(parent);
                    newParent++;
                }
            }

            Pair<List<PsiCodeBlock>, List<Integer>> childRes = getCodeBlocks(child, newParent);
            codeBlocks.addAll(childRes.getFirst());
            parentIndex.addAll(childRes.getSecond());
        }

        return new Pair<>(codeBlocks, parentIndex);
    }

    public static PsiPolyadicExpression[] getAllPolyadicExpressions(PsiClass aClass) {
        return getPolyadicExpressions(aClass).toArray(new PsiPolyadicExpression[0]);
    }

    private static List<PsiPolyadicExpression> getPolyadicExpressions(PsiElement elem) {
        List<PsiPolyadicExpression> polyExprs = new ArrayList<>();

        for (PsiElement child : elem.getChildren()) {
            if ((child instanceof PsiPolyadicExpression) && !(child instanceof PsiBinaryExpression)) {
                PsiPolyadicExpression expr = (PsiPolyadicExpression) child;
                polyExprs.add(expr);
            }

            polyExprs.addAll(getPolyadicExpressions(child));
        }

        return polyExprs;
    }

    public static String print(String[] arr) {
        StringBuffer sb = new StringBuffer();

        for (String s : arr) {
            sb.append(s);
            sb.append(" ");
        }

        return sb.toString();
    }

    public static Set<Location> removeCodeBlock(Set<Location> clones, int toRemove) {
        Set<Location> newClones = new LinkedHashSet<>();

        for (Location l : clones) {
            if (l.getCodeBlock() != toRemove) {
                newClones.add(l);
            }
        }

        return newClones;
    }

    public static boolean contains(Set<Location> clones, int toFind) {
        for (Location l : clones) {
            if (l.getCodeBlock() == toFind) {
                return true;
            }
        }

        return false;
    }

    public static int getStatementCount(PsiStatement stat) {
        int currCount = 0;

        if (stat instanceof PsiIfStatement) {
            PsiIfStatement ifStat = (PsiIfStatement) stat;

            return getIfStatementCount(ifStat);
        }

        if (stat instanceof PsiLoopStatement) {
            PsiLoopStatement loopStat = (PsiLoopStatement) stat;

            currCount++;
            if (loopStat.getBody() != null) {
                currCount += getStatementCount(loopStat.getBody());
            }

            return currCount;
        }

        if (stat instanceof PsiSwitchStatement) {
            PsiSwitchStatement switchStat = (PsiSwitchStatement) stat;

            return getSwitchStatementCount(switchStat);
        }

        if (stat instanceof PsiTryStatement) {
            PsiTryStatement tryStat = (PsiTryStatement) stat;

            return getTryStatementCount(tryStat);
        }

        if (stat instanceof PsiBlockStatement) {
            PsiBlockStatement blockStat = (PsiBlockStatement) stat;
            PsiCodeBlock codeBlock = blockStat.getCodeBlock();
            currCount += getCodeBlockCount(codeBlock);
            return currCount;
        }

        return currCount + 1;
    }

    private static int getCodeBlockCount(PsiCodeBlock block) {
        int currCount = 0;

        for (PsiStatement stat : block.getStatements()) {
            currCount += getStatementCount(stat);
        }

        return currCount;
    }

    private static int getIfStatementCount(PsiIfStatement ifStat) {
        int currCount = 0;

        if (ifStat.getCondition() != null) {
            currCount++;
        }

        PsiStatement thenBranch = ifStat.getThenBranch();
        if (thenBranch != null) {
            currCount += getStatementCount(thenBranch) + 1;
        }

        PsiStatement elseBranch = ifStat.getElseBranch();
        if (elseBranch != null) {
            currCount += getStatementCount(elseBranch) + 1;
        }

        return currCount;
    }

    private static int getTryStatementCount(PsiTryStatement tryStat) {
        int currCount = 0;

        PsiCodeBlock tryBlock = tryStat.getTryBlock();
        if (tryBlock != null) {
            currCount += getCodeBlockCount(tryBlock) + 1;
        }

        PsiCodeBlock[] catchBlocks = tryStat.getCatchBlocks();
        for (PsiCodeBlock block : catchBlocks) {
            currCount += getCodeBlockCount(block) + 1;
        }

        PsiCodeBlock finallyBlock = tryStat.getFinallyBlock();
        if (finallyBlock != null) {
            currCount += getCodeBlockCount(finallyBlock) + 1;
        }

        return currCount;
    }

    private static int getSwitchStatementCount(PsiSwitchStatement switchStat) {
        int currCount = 0;

        if (switchStat.getExpression() != null) {
            currCount++;
        }

        PsiCodeBlock switchBody = switchStat.getBody();
        if (switchBody != null) {
            currCount += getCodeBlockCount(switchBody);
        }

        return currCount;
    }

    public static int getNextNonNested(List<Integer> list, int currIndex) {
        int toFind = list.get(currIndex);
        for (int i = currIndex + 1; i < list.size(); i++) {
            if (list.get(i) <= toFind) {
                return i - 1;
            }
        }

        return list.size();
    }
}
