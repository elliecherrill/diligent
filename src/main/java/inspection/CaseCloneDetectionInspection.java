package inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import com.sun.tools.javac.code.Attribute;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import util.CodeCloneUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class CaseCloneDetectionInspection extends AbstractBaseJavaLocalInspectionTool {
    @Override
    @NotNull
    public String getDisplayName() {
        return "Similar code in switch cases";
    }

    public CaseCloneDetectionInspection() {
    }

    @Override
    @NotNull
    public String getShortName() {
        return "CaseCloneDetection";
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @Override
    @Nls
    @NotNull
    public String getGroupDisplayName() {
        return GroupNames.BUGS_GROUP_NAME;
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {

            @Override
            public void visitSwitchStatement(PsiSwitchStatement statement) {
                super.visitSwitchStatement(statement);

                PsiCodeBlock switchBody = statement.getBody();
                if (switchBody == null) {
                    return;
                }

                //TODO: will these always be expression statements?
                PsiExpressionStatement[][] cases = CodeCloneUtils.getCaseBlocks(switchBody);

                Map<PsiElement, String[]> elementMap = new HashMap<>();

                for (int i = 0; i < cases.length; i++) {
                    for (int j = 0; j < cases[0].length; j++) {
                        if (cases[i][j] != null) {
                            elementMap.put(cases[i][j], CodeCloneUtils.getExprAsString(cases[i][j]));
                        }
                    }
                }

                for (Map.Entry<PsiElement, String[]> entry : elementMap.entrySet()) {
                    for (Map.Entry<PsiElement, String[]> otherEntry : elementMap.entrySet()) {
                        if (entry.getKey().equals(otherEntry.getKey())) {
                            continue;
                        }

                        if (Arrays.equals(entry.getValue(), otherEntry.getValue())) {
                            holder.registerProblem(otherEntry.getKey(), "Duplicate expression in switch case", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        }
                    }
                }


            }
        };
    }
}