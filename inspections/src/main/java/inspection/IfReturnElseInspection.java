package inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public final class IfReturnElseInspection extends AbstractBaseJavaLocalInspectionTool {
    @Override
    @NotNull
    public String getDisplayName() {
        return "Redundant 'else' (due to return in 'if')";
    }

    public IfReturnElseInspection() {}

    @Override
    @NotNull
    public String getShortName() {
        return "IfReturnElse";
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
            public void visitIfStatement(PsiIfStatement statement) {
                super.visitIfStatement(statement);
                // Check if there is an else case
                if (statement.getElseBranch() != null) {
                    // Is the last line of the body a return statement?
                    PsiBlockStatement thenStmt = (PsiBlockStatement) statement.getThenBranch();
                    PsiCodeBlock thenBody = thenStmt.getCodeBlock();
                    PsiStatement[] stmts = thenBody.getStatements();

                    boolean endsWithReturn = false;
                    for (PsiStatement stmt : stmts) {
                        if (stmt instanceof PsiReturnStatement) {
                            endsWithReturn = true;
                        }
                    }

                    if (endsWithReturn && statement.getElseBranch() != null) {
                        holder.registerProblem(statement.getElseElement().getOriginalElement(),
                                "Unnecessary 'else' branch", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                    }
                }
            }


        };
    }
}
