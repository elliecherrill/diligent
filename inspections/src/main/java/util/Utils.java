package util;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

public final class Utils {

    private static final String CAMEL_CASE = "([a-z]+[0-9]*[A-Z]*[^\\W_]*)+";
    private static final String UPPER_SNAKE_CASE = "([A-Z]+_?)+";
    private static final Notifier NOTIFIER = new Notifier();
    private static boolean configNotFound = false;

    public static PsiElement removeWhitespaceUntilPrev(PsiElement prev) {
        while (prev instanceof PsiWhiteSpace || prev instanceof PsiComment) {
            prev = prev.getPrevSibling();
        }

        return prev;
    }

    public static boolean isCamelCase(String name) {
        return name.matches(CAMEL_CASE);
    }

    public static boolean isUpperSnakeCase(String name) {
        return name.matches(UPPER_SNAKE_CASE);
    }

    public static boolean containsImplements(String name) {
        return name.contains("implements");
    }

    public static boolean containsExtends(String name) {
        return name.contains("extends");
    }

    public static int getLineNumber(PsiElement element) {
        PsiFile containingFile = element.getContainingFile();
        Project project = containingFile.getProject();
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        Document document = psiDocumentManager.getDocument(containingFile);
        int textOffset = element.getTextOffset();

        assert document != null;

        return document.getLineNumber(textOffset) + 1;
    }

    public static String getClassName(PsiElement element) {
        assert element != null : "No containing class found";

        if (element instanceof PsiClass) {
            PsiClass aClass = (PsiClass) element;
            return aClass.getName();
        }

        return getClassName(element.getParent());
    }

    public static String getMethodName(PsiElement element) {
        if (element == null) {
            return null;
        }

        if (element instanceof PsiMethod) {
            PsiMethod method = (PsiMethod) element;
            return method.getName();
        }

        return getMethodName(element.getParent());
    }

    public static InspectionPriority getInspectionPriority(ProblemsHolder holder, String inspectionName) {
        String projectPath = holder.getProject().getBasePath();
        try {
            Object obj = new JSONParser().parse(new FileReader(projectPath + "/diligent.json"));
            JSONObject jo = (JSONObject) obj;
            JSONArray highInspections = (JSONArray) jo.get("high");
            JSONArray mediumInspections = (JSONArray) jo.get("medium");
            JSONArray lowInspections = (JSONArray) jo.get("low");

            for (Object inspection : highInspections) {
                String highInspection = (String) inspection;
                if (highInspection.equals(inspectionName)) {
                    return InspectionPriority.HIGH;
                }
            }

            for (Object inspection : mediumInspections) {
                String mediumInspection = (String) inspection;
                if (mediumInspection.equals(inspectionName)) {
                    return InspectionPriority.MEDIUM;
                }
            }

            for (Object inspection : lowInspections) {
                String lowInspection = (String) inspection;
                if (lowInspection.equals(inspectionName)) {
                    return InspectionPriority.LOW;
                }
            }

        } catch (IOException | ParseException e) {
            if (!configNotFound) {
                NOTIFIER.notifyError(holder.getProject(), "Diligent", "No configuration file found at '" + projectPath + "/diligent.json'");
            }
            configNotFound = true;
            return InspectionPriority.NONE;
        }

        return InspectionPriority.NONE;
    }

    public static boolean hasErrorsInFile(PsiElement element) {
        PsiFile file;

        if (element instanceof PsiFile) {
            file = (PsiFile) element;
        } else {
            file = element.getContainingFile();
        }

        return hasErrors(file);
    }

    private static boolean hasErrors(PsiElement element) {
        if (element instanceof PsiErrorElement) {
            return true;
        }

        for (PsiElement child : element.getChildren()) {
            if (hasErrors(child)) {
                return true;
            }
        }

        return false;
    }

    public static SmartPsiElementPointer<PsiElement> getPointer(PsiElement element) {
        return SmartPointerManager.createPointer(element);
    }

    public static boolean isString(PsiType type) {
        if (type == null) {
            return false;
        }
        return type.getCanonicalText().equals("java.lang.String");
    }

    public static boolean isImmutable(PsiType type, PsiField field) {
        // Type is primitive
        if (type instanceof PsiPrimitiveType) {
            return true;
        }

        // Type is String
        if (isString(type)) {
            return true;
        }

        // Type is array and it is empty
        if (type instanceof PsiArrayType) {
            PsiArrayType arrayType = (PsiArrayType) type;
            PsiExpression init = field.getInitializer();
            if (init == null) {
                return false;
            }

            if (init instanceof PsiArrayInitializerExpression) {
                PsiArrayInitializerExpression arrayInit = (PsiArrayInitializerExpression) init;
                PsiExpression[] inits = arrayInit.getInitializers();

                if (inits.length == 0) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isStream(PsiReferenceExpression expression) {
        for (PsiElement child : expression.getChildren()) {
            if (child instanceof PsiIdentifier) {
                PsiIdentifier id = (PsiIdentifier) child;
                String idText = id.getText();
                if (idText.equals("stream") || idText.equals("Stream") || idText.equals("IntStream") || idText.equals("ParallelStream")) {
                    return true;
                }
            }
        }

        return false;
    }

    public static String getThisId(PsiThisExpression expression) {
        PsiElement sibling = expression.getNextSibling();

        do {
            if (sibling instanceof PsiIdentifier) {
                PsiIdentifier id = (PsiIdentifier) sibling;
                return id.getText();
            }
            sibling = sibling.getNextSibling();
        } while (sibling != null);

        return null;
    }

    public static boolean isInScope(String var, PsiElement block) {
        PsiMethod method = getContainingMethod(block);

        if (method == null) {
            return false;
        }

        // Need to check parameters of containing method
        PsiParameterList paramList = method.getParameterList();
        PsiParameter[] params = paramList.getParameters();
        for (PsiParameter p : params) {
            if (p.getName().equals(var)) {
                return true;
            }
        }

        // Need to check local variables within the method
        PsiCodeBlock body = method.getBody();
        if (body == null) {
            return false;
        }

        return containsLocalVariable(var, body);
    }

    private static PsiMethod getContainingMethod(PsiElement block) {
        PsiElement parent = block.getParent();
        do {
            if (parent instanceof PsiMethod) {
                return (PsiMethod) parent;
            }
            parent = parent.getParent();
        } while (parent != null);

        return null;
    }

    private static boolean containsLocalVariable(String var, PsiCodeBlock block) {
        for (PsiStatement stat : block.getStatements()) {
            if (stat instanceof PsiDeclarationStatement) {
                PsiDeclarationStatement declStat = (PsiDeclarationStatement) stat;

                for (PsiElement elem : declStat.getDeclaredElements()) {
                    if (elem instanceof PsiLocalVariable) {
                        PsiLocalVariable localVar = (PsiLocalVariable) elem;

                        if (localVar.getName().equals(var)) {
                            return true;
                        }
                    }
                }
            } else if (stat instanceof PsiLoopStatement) {
                PsiLoopStatement loopStat = (PsiLoopStatement) stat;

                if (loopStat.getBody() instanceof PsiBlockStatement) {
                    PsiBlockStatement loopBody = (PsiBlockStatement) loopStat.getBody();

                    return containsLocalVariable(var, loopBody.getCodeBlock());
                }

            } else if (stat instanceof PsiSwitchStatement) {
                PsiSwitchStatement switchStat = (PsiSwitchStatement) stat;

                if (switchStat.getBody() instanceof PsiBlockStatement) {
                    PsiBlockStatement switchBody = (PsiBlockStatement) switchStat.getBody();

                    return containsLocalVariable(var, switchBody.getCodeBlock());
                }

            } else if (stat instanceof PsiIfStatement) {
                PsiIfStatement ifStat = (PsiIfStatement) stat;

                if (ifStat.getThenBranch() instanceof PsiBlockStatement) {
                    PsiBlockStatement thenBody = (PsiBlockStatement) ifStat.getThenBranch();

                    if (containsLocalVariable(var, thenBody.getCodeBlock())) {
                        return true;
                    }
                }

                if (ifStat.getElseBranch() instanceof PsiBlockStatement) {
                    PsiBlockStatement elseBody = (PsiBlockStatement) ifStat.getElseBranch();

                    if (containsLocalVariable(var, elseBody.getCodeBlock())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean containsLiteral(PsiElement element, IElementType literalKeyword) {
        if (element instanceof PsiJavaToken) {
            PsiJavaToken token = (PsiJavaToken) element;
            if (token.getTokenType().equals(literalKeyword)) {
                return true;
            }
        }

        PsiElement[] children = element.getChildren();
        for (PsiElement child : children) {
            if (containsLiteral(child, literalKeyword)) {
                return true;
            }
        }

        return false;
    }
}