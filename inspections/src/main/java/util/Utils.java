package util;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
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
        while (prev instanceof PsiWhiteSpace) {
            prev = prev.getPrevSibling();
        }

        return prev;
    }

    public static PsiElement removeWhitespaceUntilNext(PsiElement next) {
        while (next instanceof PsiWhiteSpace) {
            next = next.getNextSibling();
        }

        return next;
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

    public static String getProjectPath(PsiElement element) {
        PsiFile containingFile = element.getContainingFile();
        Project project = containingFile.getProject();

        return project.getBasePath();
    }

    public static boolean isInspectionOn(ProblemsHolder holder, String inspectionName) {
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
                    return true;
                }
            }

            for (Object inspection : mediumInspections) {
                String mediumInspection = (String) inspection;
                if (mediumInspection.equals(inspectionName)) {
                    return true;
                }
            }

            for (Object inspection : lowInspections) {
                String lowInspection = (String) inspection;
                if (lowInspection.equals(inspectionName)) {
                    return true;
                }
            }

        } catch (IOException | ParseException e) {
            if (!configNotFound) {
                NOTIFIER.notifyError(holder.getProject(), "Diligent", "No configuration file found at '" + projectPath + "/diligent.json'");
            }
            configNotFound = true;
            return false;
        }

        return false;
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
}