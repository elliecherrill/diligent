package util;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyFunction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

public final class Utils {

    private static final String SNAKE_CASE = "([a-z]+_?)+";
    private static final Notifier NOTIFIER = new Notifier();
    private static boolean configNotFound = false;

    public static boolean isSnakeCase(String name) {
        return name.matches(SNAKE_CASE);
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

    public static String getFunctionName(PsiElement element) {
        if (element == null) {
            return null;
        }

        if (element instanceof PyFunction) {
            PyFunction function = (PyFunction) element;
            return function.getName();
        }

        return getFunctionName(element.getParent());
    }

    public static PyFunction getFunction(PsiElement element) {
        if (element == null) {
            return null;
        }

        if (element instanceof PyFunction) {
            return (PyFunction) element;
        }

        return getFunction(element.getParent());
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

    public static boolean isIgnored(String funcName) {
        return funcName.startsWith("__") && funcName.endsWith("__");
    }
}