package util;

import com.intellij.psi.*;

import javax.annotation.Nonnull;
import java.util.*;

public class CodeCloneUtils {

    //TODO: do we want to ignore default case?
    public static PsiStatement[][] getCaseBlocks(@Nonnull PsiCodeBlock body) {
        // TODO: How to not iterate through twice?
        PsiStatement[] bodyStatements = body.getStatements();

        int numCases = 0;
        int numStats = 0;
        boolean defaultCase = false;
        List<Integer> statements = new ArrayList<>();
        for (PsiStatement stat : bodyStatements) {
            if (stat instanceof PsiSwitchLabelStatement) {
                if (((PsiSwitchLabelStatement) stat).isDefaultCase()) {
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
                if (((PsiSwitchLabelStatement) stat).isDefaultCase()) {
                    defaultCase = true;
                    continue;
                }
                defaultCase = false;
                caseIndex++;
                statIndex = 0;
            }

            if (!defaultCase) {
                if (!(stat instanceof PsiSwitchLabelStatement) && !(stat instanceof PsiBreakStatement)) {
                    caseBlocks[caseIndex][statIndex] = stat;
                    statIndex++;
                }
            }
        }

        return caseBlocks;
    }

    public static PsiStatement[][] getSameCaseBlocks(@Nonnull PsiCodeBlock body, @Nonnull PsiCodeBlock otherBody) {
        //TODO: consider default case??
        // TODO: How to not iterate through twice?

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

        String currCase = null;
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

    public static boolean changeInLiteral(String[] first, String[] second) {
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

    public static boolean sameIfBody(String[] first, String[] second) {
        int firstThenIndex = getStartIndex("IF-THEN", first);
        int secondThenIndex = getStartIndex("IF-THEN", second);

        return Arrays.equals(first, firstThenIndex, first.length, second, secondThenIndex, second.length);
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

    public static boolean transitiveClosureOfClones(List<Set<Location>> cases, List<Integer> aim) {
        if (cases.isEmpty()) {
            return false;
        }

        Set<Location> currClones = new LinkedHashSet<>(cases.get(0));

        for (int i = 1; i < cases.size(); i++) {
            if (Collections.disjoint(currClones, cases.get(i))) {
                currClones = cases.get(i);
            } else {
                currClones.addAll(cases.get(i));
            }

            if (containsAll(currClones, aim)) {
                return true;
            }
        }

        return false;
    }

    private static boolean containsAll(Set<Location> clones, List<Integer> aim) {
        for (int i : aim) {
            if (getClonesInCodeBlock(clones, i).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    public static boolean isBlockClone(Set<Location> clones, int blockIndex) {
        if (clones.isEmpty()) {
            return false;
        }

        Set<Location> clonesInBlock = getClonesInCodeBlock(clones, blockIndex);

        if (clonesInBlock.isEmpty()) {
            return false;
        }

        int prev = -1;
        for (Location l : clonesInBlock) {
            if (l.getLine() < prev) {
                return false;
            } else {
                prev = l.getLine();
            }
        }

        return true;
    }

    public static Set<Location> getCombinedClones(Set<Location> firstClones, Set<Location> secondClones) {
        Set<Location> combinedClones = new LinkedHashSet<>();
        List<Integer> seenCodeBlocks = new ArrayList<>();

        for (Location location : firstClones) {
            int currCodeBlock = location.getCodeBlock();

            if (!(seenCodeBlocks.contains(currCodeBlock))) {
                seenCodeBlocks.add(currCodeBlock);

                Set<Location> secondInBlock = getClonesInCodeBlock(secondClones, currCodeBlock);

                if (!(secondInBlock.isEmpty())) {
                    combinedClones.addAll(getClonesInCodeBlock(firstClones, currCodeBlock));
                    combinedClones.addAll(secondInBlock);
                }

            }
        }

        return combinedClones;
    }

    public static Set<Location> getClonesInCodeBlock(Set<Location> clones, int codeBlock) {
        Set<Location> clonesInCodeBlock = new LinkedHashSet<>();

        for (Location l : clones) {
            if (l.getCodeBlock() == codeBlock) {
                clonesInCodeBlock.add(l);
            }
        }

        return clonesInCodeBlock;
    }

    public static String printCodeBlock(PsiCodeBlock codeBlock) {
        StringBuffer sb = new StringBuffer();

        for (PsiStatement s : codeBlock.getStatements()) {
            sb.append(s.getText());
        }

        return sb.toString();
    }

    public static PsiCodeBlock[] getAllCodeBlocks(PsiClass aClass) {
        return getCodeBlocks(aClass).toArray(new PsiCodeBlock[0]);
    }

    private static List<PsiCodeBlock> getCodeBlocks(PsiElement elem) {
        List<PsiCodeBlock> codeBlocks = new ArrayList<>();

        for (PsiElement child : elem.getChildren()) {
            if (child instanceof PsiCodeBlock) {
                PsiCodeBlock block = (PsiCodeBlock) child;
                // Only consider code blocks with more than one statement
                if (block.getStatementCount() > 1) {
                    codeBlocks.add((PsiCodeBlock) child);
                }
            }

            if (child instanceof PsiSwitchStatement) {
                //TODO
            } else {
                codeBlocks.addAll(getCodeBlocks(child));
            }
        }

        return codeBlocks;
    }

    private static String print(String[] arr) {
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
}
