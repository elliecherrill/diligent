package util;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;

public final class Utils {

    private static final String CAMEL_CASE = "([a-z]+[A-Z]*\\w*)+";
    private static final String UPPER_SNAKE_CASE = "([A-Z]+_?)+";

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
        int lineNumber = document.getLineNumber(textOffset) + 1;

        return lineNumber;
    }
}