package util;

import com.intellij.psi.*;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class CodeCloneUtils {

    public static PsiStatement[][] getCaseBlocks(@Nonnull PsiCodeBlock body) {
        // TODO: How to not iterate through twice?
        PsiStatement[] bodyStatements = body.getStatements();

        int numCases = 0;
        int numStats = 0;
        List<Integer> statements = new ArrayList<>();
        for (PsiStatement stat : bodyStatements) {
            if (stat instanceof PsiSwitchLabelStatement) {
                numCases++;
                statements.add(numStats);
                numStats = 0;
            } else if (!(stat instanceof PsiBreakStatement)) {
                numStats++;
            }
        }
        statements.add(numStats);

        PsiStatement[][] caseBlocks = new PsiStatement[numCases][Collections.max(statements)];
        int caseIndex = -1;
        int statIndex = 0;
        for (PsiStatement stat : bodyStatements) {
            if (stat instanceof PsiSwitchLabelStatement) {
                caseIndex++;
                statIndex = 0;
            }

            if (!(stat instanceof PsiSwitchLabelStatement) && !(stat instanceof PsiBreakStatement)) {
                caseBlocks[caseIndex][statIndex] = stat;
                statIndex++;
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
                    sb.append(getExprAsString(expr));
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

    public static PsiStatement[][] getMethodBodies(PsiMethod[] methods) {
        //1. Get number of methods and max number of statements (excluding whitespace / comments)
        //2. Then create array
        //3. Put statements into the array

        int numMethods = methods.length;
        int numStats = 0;
        List<Integer> statements = new ArrayList<>();
        for (PsiMethod m : methods) {
            if (m.getBody() == null) {
                statements.add(numStats);
                continue;
            }

            PsiStatement[] bodyStats = m.getBody().getStatements();

            for (PsiStatement s : bodyStats) {
                if (!(s instanceof PsiWhiteSpace) && !(s instanceof PsiComment)) {
                    numStats++;
                }
            }
            statements.add(numStats);
            numStats = 0;
        }


        PsiStatement[][] methodBlocks = new PsiStatement[numMethods][Collections.max(statements)];
        int statIndex = 0;
        for (int methodIndex = 0; methodIndex < methods.length; methodIndex++) {
            PsiMethod m = methods[methodIndex];
            if (m.getBody() == null) {
                continue;
            }

            PsiStatement[] bodyStats = m.getBody().getStatements();

            for (PsiStatement s : bodyStats) {
                if (!(s instanceof PsiWhiteSpace) && !(s instanceof PsiComment)) {
                    methodBlocks[methodIndex][statIndex] = s;
                    statIndex++;
                }
            }
            statIndex = 0;
        }

        return methodBlocks;
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

    //TODO: move tokenisation into different file
    //TODO: standardise tokenisation (e.g. start and end flags)
    public static String[] getStmtAsStringArray(PsiStatement stmt) {
        return getStmtAsString(stmt).toArray(new String[0]);
    }

    private static List<String> getStmtAsString(PsiStatement stmt) {
        if (stmt instanceof PsiExpressionStatement) {
            return getExprStmtAsString((PsiExpressionStatement) stmt);
        }

        if (stmt instanceof PsiIfStatement) {
            return getIfStmtAsString((PsiIfStatement) stmt);
        }

        if (stmt instanceof PsiBreakStatement) {
            return new ArrayList<>();
        }

        if (stmt instanceof PsiBlockStatement) {
            return getBlockStmtAsString((PsiBlockStatement) stmt);
        }

        if (stmt instanceof PsiDeclarationStatement) {
            return getDeclStmtAsString((PsiDeclarationStatement) stmt);
        }

        if (stmt instanceof PsiReturnStatement) {
            return getReturnStmtAsString((PsiReturnStatement) stmt);
        }

        if (stmt instanceof PsiForStatement) {
            return getForStmtAsString((PsiForStatement) stmt);
        }

        if (stmt instanceof PsiForeachStatement) {
            return getForEachStmtAsString((PsiForeachStatement) stmt);
        }

        if (stmt instanceof PsiSwitchStatement) {
            return getSwitchStmtAsString((PsiSwitchStatement) stmt);
        }

        if (stmt instanceof PsiSwitchLabelStatement) {
            return getSwitchLabelStmtAsString((PsiSwitchLabelStatement) stmt);
        }

        if (stmt instanceof PsiAssertStatement) {
            return getAssertStmtAsString((PsiAssertStatement) stmt);
        }

        if (stmt instanceof PsiTryStatement) {
            return getTryStmtAsString((PsiTryStatement) stmt);
        }

        return null;
    }

    private static List<String> getTryStmtAsString(PsiTryStatement stmt) {
        List<String> tryStmtAsString = new ArrayList<>();

        if (stmt.getTryBlock() != null) {
            tryStmtAsString.add("TRY");
            tryStmtAsString.addAll(getCodeBlockAsString(stmt.getTryBlock()));
            tryStmtAsString.add("END-TRY");
        }

        if (stmt.getCatchBlocks().length > 0) {
            for (PsiCatchSection c : stmt.getCatchSections()) {
                if ((c.getCatchType() != null) && (c.getParameter() != null) && (c.getCatchBlock() != null)) {
                    tryStmtAsString.add("CATCH");

                    tryStmtAsString.add("PARAM");
                    tryStmtAsString.add(getTypeAsString(c.getCatchType()));
                    String exceptionName = c.getParameter().getName();
                    tryStmtAsString.add("EXCEPTION");

                    List<String> catchBlock = getCodeBlockAsString(c.getCatchBlock());
                    findAndReplaceVar(catchBlock, exceptionName, "EXCEPTION");
                    tryStmtAsString.addAll(catchBlock);

                    tryStmtAsString.add("END-CATCH");
                }
            }
        }

        if (stmt.getFinallyBlock() != null) {
            tryStmtAsString.add("FINALLY");
            tryStmtAsString.addAll(getCodeBlockAsString(stmt.getFinallyBlock()));
            tryStmtAsString.add("END-FINALLY");
        }

        return tryStmtAsString;
    }

    private static List<String> getAssertStmtAsString(PsiAssertStatement stmt) {
        //Only consider assertion condition (not description)
        List<String> assertStmtAsString = new ArrayList<>();

        assertStmtAsString.add("ASSERT");
        assertStmtAsString.addAll(getExprAsString(stmt.getAssertCondition()));
        assertStmtAsString.add("END-ASSERT");

        return assertStmtAsString;
    }

    private static List<String> getSwitchLabelStmtAsString(PsiSwitchLabelStatement stmt) {
        List<String> switchLabelStmtAsString = new ArrayList<>();

        switchLabelStmtAsString.add("CASELABEL");

        PsiElement[] children = stmt.getChildren();

        for (PsiElement child : children) {
            if (child instanceof PsiExpressionList) {
                PsiExpressionList exprList = (PsiExpressionList) child;
                PsiExpression[] exprs = exprList.getExpressions();
                for (PsiExpression expr : exprs) {
                    switchLabelStmtAsString.addAll(getExprAsString(expr));
                }
                break;
            }
        }

        switchLabelStmtAsString.add("END-CASELABEL");

        return switchLabelStmtAsString;
    }

    private static List<String> getSwitchStmtAsString(PsiSwitchStatement stmt) {
        List<String> switchStmtAsString = new ArrayList<>();

        switchStmtAsString.add("SWITCH");

        PsiElement[] children = stmt.getChildren();
        for (PsiElement child : children) {
            if (child instanceof PsiReferenceExpression) {
                switchStmtAsString.add(getRefAsString((PsiReferenceExpression) child));
                break;
            }
        }

        PsiCodeBlock switchBody = stmt.getBody();

        if (switchBody != null) {
            PsiStatement[] stats = switchBody.getStatements();

            for (PsiStatement s : stats) {
                switchStmtAsString.addAll(getStmtAsString(s));
            }
        }

        switchStmtAsString.add("END-SWITCH");

        return switchStmtAsString;
    }

    private static List<String> getForStmtAsString(PsiForStatement stmt) {
        List<String> forStmtAsString = new ArrayList<>();

        forStmtAsString.add("FOR");
        PsiStatement initStmt = stmt.getInitialization();
        PsiExpression limitExpr = stmt.getCondition();
        PsiStatement updateStmt = stmt.getUpdate();

        String indexVar = "";
        if (initStmt instanceof PsiDeclarationStatement) {
            List<String> initAsString = getDeclStmtAsString((PsiDeclarationStatement) initStmt);

            indexVar = initAsString.get(initAsString.indexOf("NAME") + 1);

            forStmtAsString.add("FOR-INIT");
            forStmtAsString.addAll(initAsString);
        }

        forStmtAsString.add("FOR-COND");
        forStmtAsString.addAll(getExprAsString(limitExpr));

        forStmtAsString.add("FOR-UPDATE");
        getStmtAsString(updateStmt);

        forStmtAsString.add("FOR-BODY");
        PsiStatement forBody = stmt.getBody();
        forStmtAsString.addAll(getStmtAsString(forBody));

        findAndReplaceVar(forStmtAsString, indexVar, "FOR-INDEX");

        forStmtAsString.add("END-FOR");

        return forStmtAsString;
    }

    private static List<String> getForEachStmtAsString(PsiForeachStatement stmt) {
        List<String> forEachStmtAsString = new ArrayList<>();

        forEachStmtAsString.add("FOREACH");
        PsiParameter param = stmt.getIterationParameter();
        forEachStmtAsString.add(getTypeAsString(param.getType()));
        String paramName = param.getName();
        forEachStmtAsString.add("FOREACH-PARAM");

        forEachStmtAsString.add("IN");
        forEachStmtAsString.addAll(getExprAsString(stmt.getIteratedValue()));

        if (stmt.getBody() != null) {
            forEachStmtAsString.add("FOREACH-BODY");
            List<String> body = getStmtAsString(stmt.getBody());
            findAndReplaceVar(body, paramName, "FOREACH-PARAM");
            forEachStmtAsString.addAll(body);
            forEachStmtAsString.add("END-FOREACH-BODY");
        }

        forEachStmtAsString.add("END-FOREACH");

        return forEachStmtAsString;
    }

    private static void findAndReplaceVar(List<String> list, String toFind, String replacement) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(toFind)) {
                list.set(i, replacement);
            }
        }
    }

    private static List<String> getReturnStmtAsString(PsiReturnStatement stmt) {
        List<String> returnStmtAsString = new ArrayList<>();

        returnStmtAsString.add("RETURN");

        PsiExpression returnExpr = stmt.getReturnValue();

        if (returnExpr != null) {
            returnStmtAsString.addAll(getExprAsString(returnExpr));
        }

        returnStmtAsString.add("END-RETURN");

        return returnStmtAsString;
    }

    private static List<String> getDeclStmtAsString(PsiDeclarationStatement stmt) {
        List<String> declStmtAsString = new ArrayList<>();

        declStmtAsString.add("DECLARATION");

        //TODO: try with multiple elements
        PsiElement[] elements = stmt.getDeclaredElements();

        for (PsiElement elem : elements) {
            // TODO: what else could it be?
            if (elem instanceof PsiLocalVariable) {
                declStmtAsString.addAll(getLocalVarAsString((PsiLocalVariable) elem));
            }
        }

        declStmtAsString.add("END-DECLARATION");

        return declStmtAsString;
    }

    private static List<String> getLocalVarAsString(PsiLocalVariable var) {
        List<String> localVarAsString = new ArrayList<>();

        localVarAsString.add("TYPE");
        PsiTypeElement type = var.getTypeElement();
        localVarAsString.add(getTypeAsString(type.getType()));

        localVarAsString.add("NAME");
        localVarAsString.add(var.getName());

        if (var.hasInitializer()) {
            localVarAsString.add("INIT");
            PsiExpression varInit = var.getInitializer();
            localVarAsString.addAll(getExprAsString(varInit));
        }

        return localVarAsString;
    }

    private static List<String> getBlockStmtAsString(PsiBlockStatement stmt) {
        return getCodeBlockAsString(stmt.getCodeBlock());
    }

    private static List<String> getCodeBlockAsString(PsiCodeBlock stmt) {
        List<String> codeBlockAsString = new ArrayList<>();

        PsiStatement[] blockStmts = stmt.getStatements();

        for (PsiStatement blockStmt : blockStmts) {
            codeBlockAsString.add("STMT");
            codeBlockAsString.addAll(getStmtAsString(blockStmt));
            codeBlockAsString.add("END-STMT");
        }

        return codeBlockAsString;
    }

    private static List<String> getIfStmtAsString(PsiIfStatement stmt) {
        List<String> ifStmtAsString = new ArrayList<>();

        ifStmtAsString.add("IF");

        PsiExpression condExpr = stmt.getCondition();

        if (condExpr != null) {
            ifStmtAsString.add("COND");
            ifStmtAsString.addAll(getBinExprAsString((PsiBinaryExpression) condExpr));
        }

        PsiStatement thenStmt = stmt.getThenBranch();
        if (thenStmt != null) {
            ifStmtAsString.add("THEN");
            ifStmtAsString.addAll(getStmtAsString(thenStmt));
        }

        PsiStatement elseStmt = stmt.getElseBranch();
        if (elseStmt != null) {
            ifStmtAsString.add("ELSE");
            ifStmtAsString.addAll(getStmtAsString(elseStmt));
        }

        ifStmtAsString.add("END-IF");

        return ifStmtAsString;
    }

    private static List<String> getBinExprAsString(PsiBinaryExpression binExpr) {
        List<String> binExprAsString = new ArrayList<>();

        binExprAsString.add("BINEXPRLHS");

        PsiExpression leftExpr = binExpr.getLOperand();
        if (leftExpr instanceof PsiReferenceExpression) {
            binExprAsString.add(getRefAsString((PsiReferenceExpression) leftExpr));
        }

        if (leftExpr instanceof PsiLiteralExpression) {
            binExprAsString.add(getLiteralAsString((PsiLiteralExpression) leftExpr));
        }

        if (leftExpr instanceof PsiBinaryExpression) {
            binExprAsString.addAll(getBinExprAsString((PsiBinaryExpression) leftExpr));
        }

        binExprAsString.add("END-BINEXPRLHS");

        binExprAsString.add("BINEXPROP");
        binExprAsString.add(getOpAsString(binExpr.getOperationSign()));

        binExprAsString.add("BINEXPRRHS");

        PsiExpression rightExpr = binExpr.getROperand();
        if (rightExpr instanceof PsiReferenceExpression) {
            binExprAsString.add(getRefAsString((PsiReferenceExpression) rightExpr));
        }

        if (rightExpr instanceof PsiLiteralExpression) {
            binExprAsString.add(getLiteralAsString((PsiLiteralExpression) rightExpr));
        }

        if (rightExpr instanceof PsiBinaryExpression) {
            binExprAsString.addAll(getBinExprAsString((PsiBinaryExpression) rightExpr));
        }

        binExprAsString.add("END-BINEXPRRHS");

        return binExprAsString;
    }

    private static List<String> getExprStmtAsString(PsiExpressionStatement exprStmt) {
        PsiExpression expr = exprStmt.getExpression();

        return getExprAsString(expr);
    }

    private static List<String> getExprAsString(PsiExpression expr) {
        List<String> exprAsString = new ArrayList<>();

        exprAsString.add("EXPR");

        if (expr instanceof PsiAssignmentExpression) {
            exprAsString.add("LHS");
            PsiAssignmentExpression assignmentExpr = (PsiAssignmentExpression) expr;
            PsiExpression leftExpr = assignmentExpr.getLExpression();

            if (leftExpr instanceof PsiReferenceExpression) {
                exprAsString.add(getRefAsString((PsiReferenceExpression) leftExpr));
            }

            exprAsString.add("OP");
            PsiJavaToken opToken = assignmentExpr.getOperationSign();
            exprAsString.add(getOpAsString(opToken));

            exprAsString.add("RHS");
            PsiExpression rightExpr = assignmentExpr.getRExpression();

            if (rightExpr instanceof PsiReferenceExpression) {
                exprAsString.add(getRefAsString((PsiReferenceExpression) rightExpr));
            }

            if (rightExpr instanceof PsiLiteralExpression) {
                exprAsString.add(getLiteralAsString((PsiLiteralExpression) rightExpr));
            }

        } else if (expr instanceof PsiMethodCallExpression) {
            PsiMethodCallExpression methodCallExpr = (PsiMethodCallExpression) expr;
            PsiReferenceExpression refExpr = methodCallExpr.getMethodExpression();

            exprAsString.add("CALL");
            exprAsString.add(getRefAsString(refExpr));

            PsiExpressionList paramList = methodCallExpr.getArgumentList();
            PsiExpression[] params = paramList.getExpressions();

            if (params.length > 0) {
                exprAsString.add("PARAMS");
            }

            for (PsiExpression param : params) {
                exprAsString.addAll(getExprAsString(param));
            }

        } else if (expr instanceof PsiReferenceExpression) {
            exprAsString.add(getRefAsString((PsiReferenceExpression) expr));

        } else if (expr instanceof PsiLiteralExpression) {
            exprAsString.add(getLiteralAsString((PsiLiteralExpression) expr));
        } else if (expr instanceof PsiPostfixExpression) {
            PsiPostfixExpression postfixExpr = (PsiPostfixExpression) expr;
            exprAsString.addAll(getExprAsString(postfixExpr.getOperand()));
            exprAsString.add(getOpAsString(postfixExpr.getOperationSign()));
        } else if (expr instanceof PsiPrefixExpression) {
            PsiPrefixExpression prefixExpr = (PsiPrefixExpression) expr;
            exprAsString.addAll(getExprAsString(prefixExpr.getOperand()));
            exprAsString.add(getOpAsString(prefixExpr.getOperationSign()));
        } else if (expr instanceof PsiBinaryExpression) {
            exprAsString.addAll(getBinExprAsString((PsiBinaryExpression) expr));
        }

        exprAsString.add("END-EXPR");

        return exprAsString;
    }

    private static String getOpAsString(PsiJavaToken opToken) {
        if (opToken.getTokenType() == null) {
            return "";
        }

        return opToken.getTokenType().toString();
    }

    private static String getLiteralAsString(PsiLiteralExpression litExpr) {
        Object val = litExpr.getValue();

        if (val != null) {
            return val.toString();
        }

        return "";
    }

    private static String getRefAsString(PsiReferenceExpression refExpr) {
        String id = getIdentifierString(refExpr);

        if (id != null) {
            return id;
        }

        return "";
    }

    private static String getTypeAsString(PsiType type) {
        return type.getCanonicalText();
    }

    private static String getIdentifierString(PsiReferenceExpression refExpr) {
        for (PsiElement child : refExpr.getChildren()) {
            if (child instanceof PsiIdentifier) {
                PsiIdentifier childIdentifier = (PsiIdentifier) child;
                return childIdentifier.getText();
            }
        }

        return null;
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
        int firstCondIndex = getStartIndex("COND", first) + 1;
        int firstCondEndIndex = getStartIndex("THEN", first);
        int secondCondIndex = getStartIndex("COND", second) + 1;
        int secondCondEndIndex = getStartIndex("THEN", second);

        // if no condition
        if (firstCondIndex == firstCondEndIndex || secondCondIndex == secondCondEndIndex) {
            return false;
        }

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
        int firstThenIndex = getStartIndex("THEN", first);
        int secondThenIndex = getStartIndex("THEN", second);

        return Arrays.equals(first, firstThenIndex, first.length, second, secondThenIndex, second.length);
    }

    public static boolean sameForSetup(String[] first, String[] second) {
        int firstBodyIndex = getStartIndex("FOR-BODY", first);
        int secondBodyIndex = getStartIndex("FOR-BODY", second);

        return Arrays.equals(first, 0, firstBodyIndex, second, 0, secondBodyIndex);
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
        int firstVarIndex = getStartIndex("SWITCH", first) + 1;
        int secondVarIndex = getStartIndex("SWITCH", second) + 1;

        return first[firstVarIndex].equals(second[secondVarIndex]);
    }

    private static int getStartIndex(String toFind, String[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(toFind)) {
                return i;
            }
        }

        return -1;
    }
}
