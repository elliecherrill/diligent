package inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import util.CodeCloneUtils;

import java.util.Arrays;

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

                PsiExpressionStatement[][] cases = CodeCloneUtils.getCaseBlocks(switchBody);

                String[] firstFirst = CodeCloneUtils.getExprAsString(cases[0][0]);
                String[] secondFirst = CodeCloneUtils.getExprAsString(cases[1][0]);
                String[] secondSecond = CodeCloneUtils.getExprAsString(cases[1][1]);


                if (Arrays.equals(firstFirst, secondFirst)) {
                    holder.registerProblem(cases[1][0],
                            "EQUAL 00 10", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }

                if (Arrays.equals(firstFirst, secondSecond)) {
                    holder.registerProblem(cases[1][1],
                            "EQUAL 00 11", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }


            }
        };
    }
}