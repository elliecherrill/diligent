package util;

import com.intellij.psi.*;

import javax.print.DocFlavor;
import java.util.ArrayList;
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

    public static String[] getStatAsString(PsiStatement stmt) {
        if (stmt instanceof PsiExpressionStatement) {
            return getExprAsString((PsiExpressionStatement) stmt);
        }

        // TODO: if if statement

        return null;
    }

    private static String[] getExprAsString(PsiExpressionStatement exprStmt) {
        List<String> exprAsString = new ArrayList<>();

        PsiExpression expr = exprStmt.getExpression();

        if (expr instanceof PsiAssignmentExpression) {
            PsiAssignmentExpression assignmentExpr = (PsiAssignmentExpression) expr;
            PsiExpression leftExpr = assignmentExpr.getLExpression();

            if (leftExpr instanceof PsiReferenceExpression) {
                PsiReferenceExpression leftRefExpr = (PsiReferenceExpression) leftExpr;
                String leftId = getIdentifierString(leftRefExpr);

                if (leftId != null) {
                    exprAsString.add(leftId);
                }
            }

            PsiJavaToken opToken = assignmentExpr.getOperationSign();
            String opString = opToken.getTokenType().toString();
            exprAsString.add(opString);

            PsiExpression rightExpr = assignmentExpr.getRExpression();

            // If RHS is a variable
            if (rightExpr instanceof PsiReferenceExpression) {
                PsiReferenceExpression rightRefExpr = (PsiReferenceExpression) rightExpr;
                String rightId = getIdentifierString(rightRefExpr);

                if (rightId != null) {
                    exprAsString.add(rightId);
                }
            }

            // If RHS is a literal
            if (rightExpr instanceof PsiLiteralExpression) {
                PsiLiteralExpression rightLitExpr = (PsiLiteralExpression) rightExpr;
                Object rightVal = rightLitExpr.getValue();

                if (rightVal != null) {
                    String litString = rightVal.toString();
                    exprAsString.add(litString);
                }
            }
        }

        return exprAsString.toArray(new String[0]);
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
        // TODO: try to break this
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

    private static boolean isNumeric(String str) {
        return str.matches("(\\d)+");
    }
}
