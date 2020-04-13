package inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import util.*;

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

        if (!Utils.isInspectionOn(holder,"redundant-else")) {
            return new JavaElementVisitor() {};
        }

        return new JavaElementVisitor() {

            FeedbackHolder feedbackHolder = FeedbackHolder.getInstance();

            @Override
            public void visitFile(@NotNull PsiFile file) {
                super.visitFile(file);

                if (Utils.hasErrorsInFile(file)) {
                    return;
                }

                feedbackHolder.writeToFile();
            }

            @Override
            public void visitIfStatement(PsiIfStatement statement) {
                super.visitIfStatement(statement);

                if (Utils.hasErrorsInFile(statement)) {
                    return;
                }

                String filename = statement.getContainingFile().getName();
                FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(statement), "redundant-else", PsiStmtType.IF);

                // Check if there is an else case
                if (statement.getElseBranch() != null) {
                    PsiBlockStatement thenStmt = (PsiBlockStatement) statement.getThenBranch();
                    PsiCodeBlock thenBody = thenStmt.getCodeBlock();
                    PsiStatement[] stmts = thenBody.getStatements();

                    boolean endsWithReturn = false;
                    for (PsiStatement stmt : stmts) {
                        if (stmt instanceof PsiReturnStatement) {
                            endsWithReturn = true;
                            break;
                        }
                    }

                    if (endsWithReturn) {
                        Feedback feedback = new Feedback(Utils.getLineNumber(statement), "Unnecessary 'else' branch", filename);
                        feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
                    } else {
                        feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                    }
                } else {
                    feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                }
            }


        };
    }
}
