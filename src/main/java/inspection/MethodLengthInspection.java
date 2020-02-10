package inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import util.Utils;

public final class MethodLengthInspection extends AbstractBaseJavaLocalInspectionTool {

    private static final int MAX_METHOD_LENGTH = 20;

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

    @Override
    @Nls
    @NotNull
    public String getDisplayName() {
        return "Method length should not be longer than " + MAX_METHOD_LENGTH + " statements.";
    }

    @Override
    @NonNls
    @NotNull
    public String getShortName() {
        return "MethodLength";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {

            @Override
            public void visitMethod(PsiMethod method) {
                super.visitMethod(method);

                PsiCodeBlock body = method.getBody();
                if (body != null && body.getStatementCount() >= MAX_METHOD_LENGTH) {
                    holder.registerProblem(Utils.removeWhitespaceUntilNext(body.getFirstBodyElement()), "Method length should not be longer than " + MAX_METHOD_LENGTH + " statements.", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
            }
        };
    }
}
