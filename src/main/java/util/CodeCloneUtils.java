package util;

import com.intellij.psi.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CodeCloneUtils {

    public static PsiExpressionStatement[][] getCaseBlocks(PsiCodeBlock body) {
        // How to not iterate through twice?
        PsiStatement[] bodyStatements = body.getStatements();

        int numCases = 0;
        int numExpr = 0;
        List<Integer> expressions = new ArrayList<>();
        for (PsiStatement stat : bodyStatements) {
            if (stat instanceof PsiSwitchLabelStatement) {
                numCases++;
                expressions.add(numExpr);
                numExpr = 0;
            }
            if (stat instanceof PsiExpressionStatement) {
                numExpr++;
            }
        }

        PsiExpressionStatement[][] caseBlocks = new PsiExpressionStatement[numCases][Collections.max(expressions)];
        int caseIndex = -1;
        int exprIndex = 0;
        for (PsiStatement stat : bodyStatements) {
            if (stat instanceof PsiSwitchLabelStatement) {
                caseIndex++;
                exprIndex = 0;
            }

            // Are they always going to be PsiExpressionStatement?
            if (stat instanceof PsiExpressionStatement) {
                caseBlocks[caseIndex][exprIndex] = (PsiExpressionStatement) stat;
                exprIndex++;
            }
        }

        return caseBlocks;
    }

    // String array vs string
    public static String[] getExprAsString(PsiExpressionStatement exprStmt) {
        // WHAT SHOULD THIS VAL BE?
        String[] exprAsString = new String[3];
        int count = 0;

        PsiExpression expr = exprStmt.getExpression();

        if (expr instanceof PsiAssignmentExpression) {
            PsiAssignmentExpression assignmentExpr = (PsiAssignmentExpression) expr;
            PsiExpression leftExpr = assignmentExpr.getLExpression();

            if (leftExpr instanceof PsiReferenceExpression) {
                PsiReferenceExpression leftRefExpr = (PsiReferenceExpression) leftExpr;
                String leftId = getIdentifierString(leftRefExpr);

                if (leftId != null) {
                    exprAsString[count] = leftId;
                    count++;
                }
            }

            PsiJavaToken opToken = assignmentExpr.getOperationSign();
            exprAsString[count] = opToken.getTokenType().toString();
            count++;

            PsiExpression rightExpr = assignmentExpr.getRExpression();

            // If RHS is a variable
            if (rightExpr instanceof PsiReferenceExpression) {
                PsiReferenceExpression rightRefExpr = (PsiReferenceExpression) rightExpr;
                String rightId = getIdentifierString(rightRefExpr);

                if (rightId != null) {
                    exprAsString[count] = rightId;
                    count++;
                }
            }

            // If RHS is a literal
            if (rightExpr instanceof PsiLiteralExpression) {
                PsiLiteralExpression rightLitExpr = (PsiLiteralExpression) rightExpr;

                exprAsString[count] = rightLitExpr.getValue().toString();
                count++;

            }
        }

        return exprAsString;
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
}
