package util;

import com.intellij.psi.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class CodeCloneUtils {

    public static PsiStatement[][] getCaseBlocks(PsiCodeBlock body) {
        // How to not iterate through twice?
        PsiStatement[] bodyStatements = body.getStatements();

        int numCases = 0;
        int numStats = 0;
        List<Integer> statements = new ArrayList<>();
        for (PsiStatement stat : bodyStatements) {
            if (stat instanceof PsiSwitchLabelStatement) {
                numCases++;
                statements.add(numStats);
                numStats = 0;
            }
            if (!(stat instanceof PsiSwitchLabelStatement) && !(stat instanceof  PsiBreakStatement)) {
                numStats++;
            }
        }

        PsiStatement[][] caseBlocks = new PsiStatement[numCases][Collections.max(statements)];
        int caseIndex = -1;
        int statIndex = 0;
        for (PsiStatement stat : bodyStatements) {
            if (stat instanceof PsiSwitchLabelStatement) {
                caseIndex++;
                statIndex = 0;
            }

            if (!(stat instanceof PsiSwitchLabelStatement) && !(stat instanceof  PsiBreakStatement)) {
                caseBlocks[caseIndex][statIndex] = stat;
                statIndex++;
            }
        }

        return caseBlocks;
    }

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

        return null;
    }

    private static List<String> getDeclStmtAsString(PsiDeclarationStatement stmt) {
        List<String> declStmtAsString = new ArrayList<>();

        //TODO: try with multiple elements
        PsiElement[] elements = stmt.getDeclaredElements();

        for (PsiElement elem : elements) {
            // TODO: what else could it be?
            if (elem instanceof PsiLocalVariable) {
                declStmtAsString.addAll(getLocalVarAsString((PsiLocalVariable) elem));
            }
        }

        return declStmtAsString;
    }

    private static List<String> getLocalVarAsString(PsiLocalVariable var) {
        List<String> localVarAsString = new ArrayList<>();

        localVarAsString.add("TYPE");
        PsiTypeElement type = var.getTypeElement();
        localVarAsString.add(type.getType().getCanonicalText());

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
        List<String> blockStmtAsString = new ArrayList<>();

        PsiStatement[] blockStmts = stmt.getCodeBlock().getStatements();

        for (PsiStatement blockStmt : blockStmts) {
            blockStmtAsString.add("STMT");
            blockStmtAsString.addAll(getStmtAsString(blockStmt));
        }

        return blockStmtAsString;

    }

    private static List<String> getIfStmtAsString(PsiIfStatement stmt) {
        List<String> ifStmtAsString = new ArrayList<>();

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

        return binExprAsString;
    }

    private static List<String> getExprStmtAsString(PsiExpressionStatement exprStmt) {
        PsiExpression expr = exprStmt.getExpression();

        return getExprAsString(expr);
    }

    private static List<String> getExprAsString(PsiExpression expr) {
        List<String> exprAsString = new ArrayList<>();

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
        }

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
        int firstCondEndIndex = getStartIndex("THEN",first);
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

        return Arrays.equals(first,0, firstRhsIndex, second, 0, secondRhsIndex);
    }

    public static boolean conditionChangeInLhs(String[] first, String[] second) {
        // RHS and operator the same
        // LHS different
        int firstOpIndex = getStartIndex("BINEXPROP", first);
        int secondOpIndex = getStartIndex("BINEXPROP", second);

        return Arrays.equals(first,firstOpIndex, first.length, second, secondOpIndex, second.length);
    }

    public static boolean sameIfBody(String[] first, String[] second) {
        int firstThenIndex = getStartIndex("THEN", first);
        int secondThenIndex = getStartIndex("THEN", second);

        return Arrays.equals(first, firstThenIndex, first.length, second, secondThenIndex, second.length);
    }

    public static boolean declChangeInVarName(String[] first, String[] second) {
        // int z = 0 and int x = 0 are the same we just have called them different names
        // Same type and initialiser (if they have one)
        // Different variable name
        int firstNameIndex = getStartIndex("NAME", first);
        int firstEndNameIndex = getStartIndex("INIT",first);
        int secondNameIndex = getStartIndex("NAME", second);
        int secondEndNameIndex = getStartIndex("INIT",second);

        boolean sameType = Arrays.equals(first, 0, firstNameIndex, second, 0, secondNameIndex);

        if (!sameType) {
            return false;
        }

        if (firstEndNameIndex == -1 && secondEndNameIndex == -1) {
            return true;
        }

        if (firstEndNameIndex != -1 && secondEndNameIndex != -1) {
            return Arrays.equals(first,firstEndNameIndex, first.length, second, secondEndNameIndex, second.length);
        }

        return false;
    }

    private static int getStartIndex(String toFind, String[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(toFind)) {
                return i;
            }
        }

        return -1;
    }

    private static boolean isNumeric(String str) {
        return str.matches("(\\d)+");
    }

    public static String output(String[] str) {
        StringBuilder sb = new StringBuilder();

        for (String s : str) {
            sb.append(s);
        }

        return sb.toString();
    }
}
