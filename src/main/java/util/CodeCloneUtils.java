package util;

import com.intellij.psi.*;

import javax.print.DocFlavor;
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

    public static String[] getStatAsStringArray(PsiStatement stmt) {
        return getStatAsString(stmt).toArray(new String[0]);
    }

    private static List<String> getStatAsString(PsiStatement stmt) {
        if (stmt instanceof PsiExpressionStatement) {
            return getExprAsString((PsiExpressionStatement) stmt);
        }

        if (stmt instanceof PsiIfStatement) {
            return getIfStatAsString((PsiIfStatement) stmt);
        }

        if (stmt instanceof PsiBreakStatement) {
            return new ArrayList<>();
        }

        if (stmt instanceof PsiBlockStatement) {
            return getBlockStatAsString((PsiBlockStatement) stmt);
        }

        return null;
    }

    private static List<String> getBlockStatAsString(PsiBlockStatement stmt) {
        List<String> blockStmtAsString = new ArrayList<>();

        PsiStatement[] blockStmts = stmt.getCodeBlock().getStatements();

        for (PsiStatement blockStmt : blockStmts) {
            blockStmtAsString.add("STMT");
            blockStmtAsString.addAll(getStatAsString(blockStmt));
        }

        return blockStmtAsString;

    }

    private static List<String> getIfStatAsString(PsiIfStatement stmt) {
        List<String> ifStmtAsString = new ArrayList<>();

        PsiExpression condExpr = stmt.getCondition();

        if (condExpr != null) {
            ifStmtAsString.add("COND");
            ifStmtAsString.addAll(getBinExprAsString((PsiBinaryExpression) condExpr));
        }

        PsiStatement thenStmt = stmt.getThenBranch();
        if (thenStmt != null) {
            ifStmtAsString.add("THEN");
            ifStmtAsString.addAll(getStatAsString(thenStmt));
        }

        PsiStatement elseStmt = stmt.getElseBranch();
        if (elseStmt != null) {
            ifStmtAsString.add("ELSE");
            ifStmtAsString.addAll(getStatAsString(elseStmt));
        }

        return ifStmtAsString;
    }

    private static List<String> getBinExprAsString(PsiBinaryExpression binExpr) {
        List<String> binExprAsString = new ArrayList<>();

        PsiExpression leftExpr = binExpr.getLOperand();
        if (leftExpr instanceof PsiReferenceExpression) {
            binExprAsString.add(getRefAsString((PsiReferenceExpression) leftExpr));
        }

        if (leftExpr instanceof PsiLiteralExpression) {
            binExprAsString.add(getLiteralAsString((PsiLiteralExpression) leftExpr));
        }

        binExprAsString.add(getOpAsString(binExpr.getOperationSign()));

        PsiExpression rightExpr = binExpr.getROperand();
        if (rightExpr instanceof PsiReferenceExpression) {
            binExprAsString.add(getRefAsString((PsiReferenceExpression) rightExpr));
        }

        if (rightExpr instanceof PsiLiteralExpression) {
            binExprAsString.add(getLiteralAsString((PsiLiteralExpression) rightExpr));
        }

        return binExprAsString;
    }

    private static List<String> getExprAsString(PsiExpressionStatement exprStmt) {
        List<String> exprAsString = new ArrayList<>();

        PsiExpression expr = exprStmt.getExpression();

        if (expr instanceof PsiAssignmentExpression) {
            PsiAssignmentExpression assignmentExpr = (PsiAssignmentExpression) expr;
            PsiExpression leftExpr = assignmentExpr.getLExpression();

            if (leftExpr instanceof PsiReferenceExpression) {
                exprAsString.add(getRefAsString((PsiReferenceExpression) leftExpr));
            }

            PsiJavaToken opToken = assignmentExpr.getOperationSign();
            exprAsString.add(getOpAsString(opToken));

            PsiExpression rightExpr = assignmentExpr.getRExpression();

            if (rightExpr instanceof PsiReferenceExpression) {
                exprAsString.add(getRefAsString((PsiReferenceExpression) rightExpr));
            }

            if (rightExpr instanceof PsiLiteralExpression) {
                exprAsString.add(getLiteralAsString((PsiLiteralExpression) rightExpr));
            }
        }

        if (expr instanceof PsiMethodCallExpression) {
            //TODO: Handle method calls

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

    //TODO: what if they have different lengths?
    public static boolean changeInLiteral(String[] first, String[] second) {
        int newLen = first.length - 1;

        for (int i = 0; i < newLen; i++) {
            if (!first[i].equals(second[i])) {
                return false;
            }
        }

        return true;

    }

    //TODO: what if they have different lengths?
    public static boolean changeInOp(String[] first, String[] second) {
        int op = 1;

        for (int i = 0; i < first.length; i++) {
            if (i != op) {
                if (!first[i].equals(second[i])) {
                    return false;
                }
            }
        }

        return true;

    }

    public static boolean sameCondition(String[] first, String[] second) {
        int firstCondIndex = getStartIndex("COND", first) + 1;
        int firstCondEndIndex = getStartIndex("THEN",first);
        int secondCondIndex = getStartIndex("COND", second) + 1;
        int secondCondEndIndex = getStartIndex("THEN", second);

        return Arrays.equals(first, firstCondIndex, firstCondEndIndex, second, secondCondIndex, secondCondEndIndex);
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
