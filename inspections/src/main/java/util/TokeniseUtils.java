package util;

import com.intellij.psi.*;

import java.util.ArrayList;
import java.util.List;

public class TokeniseUtils {

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

        if (stmt instanceof PsiWhileStatement) {
            return getWhileStmtAsString((PsiWhileStatement) stmt);
        }

        if (stmt instanceof PsiDoWhileStatement) {
            return getDoWhileStmtAsString((PsiDoWhileStatement) stmt);
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
        forEachStmtAsString.add("FOREACH-TYPE");
        forEachStmtAsString.add(getTypeAsString(param.getType()));
        String paramName = param.getName();
        forEachStmtAsString.add("FOREACH-PARAM");

        forEachStmtAsString.add("FOREACH-IN");
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

    private static List<String> getWhileStmtAsString(PsiWhileStatement stmt) {
        List<String> whileStmtAsString = new ArrayList<>();

        whileStmtAsString.add("WHILE");

        if (stmt.getCondition() != null) {
            whileStmtAsString.add("WHILE-COND");
            whileStmtAsString.addAll(getExprAsString(stmt.getCondition()));
            whileStmtAsString.add("END-WHILE-COND");
        }

        if (stmt.getBody() != null) {
            whileStmtAsString.add("WHILE-BODY");
            whileStmtAsString.addAll(getStmtAsString(stmt.getBody()));
            whileStmtAsString.add("END-WHILE-BODY");
        }

        whileStmtAsString.add("END-WHILE");

        return whileStmtAsString;
    }

    private static List<String> getDoWhileStmtAsString(PsiDoWhileStatement stmt) {
        List<String> doWhileStmtAsString = new ArrayList<>();

        doWhileStmtAsString.add("DOWHILE");

        if (stmt.getBody() != null) {
            doWhileStmtAsString.add("DOWHILE-BODY");
            doWhileStmtAsString.addAll(getStmtAsString(stmt.getBody()));
            doWhileStmtAsString.add("END-DOWHILE-BODY");
        }

        if (stmt.getCondition() != null) {
            doWhileStmtAsString.add("DOWHILE-COND");
            doWhileStmtAsString.addAll(getExprAsString(stmt.getCondition()));
            doWhileStmtAsString.add("END-DOWHILE-COND");
        }

        doWhileStmtAsString.add("END-DOWHILE");

        return doWhileStmtAsString;
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
            ifStmtAsString.add("IF-COND");
            ifStmtAsString.addAll(getBinExprAsString((PsiBinaryExpression) condExpr));
        }

        PsiStatement thenStmt = stmt.getThenBranch();
        if (thenStmt != null) {
            ifStmtAsString.add("IF-THEN");
            ifStmtAsString.addAll(getStmtAsString(thenStmt));
        }

        PsiStatement elseStmt = stmt.getElseBranch();
        if (elseStmt != null) {
            ifStmtAsString.add("IF-ELSE");
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

    public static List<String> getExprAsString(PsiExpression expr) {
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
}
