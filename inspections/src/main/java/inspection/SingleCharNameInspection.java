package inspection;

import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import feedback.Feedback;
import feedback.FeedbackHolder;
import feedback.FeedbackIdentifier;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import util.FeedbackType;
import util.InspectionPriority;
import util.PsiStmtType;
import util.Utils;

public final class SingleCharNameInspection extends AbstractBaseJavaLocalInspectionTool {

    private static final String INSPECTION_NAME = "single-char-name";

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
        return "Method and variable names should be more than one character in length.";
    }

    @Override
    @NonNls
    @NotNull
    public String getShortName() {
        return "SingleCharName";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        InspectionPriority priority = Utils.getInspectionPriority(holder, INSPECTION_NAME);
        if (priority == InspectionPriority.NONE) {
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
            public void visitField(PsiField field) {
                super.visitField(field);

                if (Utils.hasErrorsInFile(field)) {
                    return;
                }

                String filename = field.getContainingFile().getName();
                int line = Utils.getLineNumber(field);
                FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(field), INSPECTION_NAME, PsiStmtType.FIELD, line);

                if (field.getName().length() == 1) {
                    Feedback feedback = new Feedback(line,
                            filename,
                            line + "-" + field.getName() + INSPECTION_NAME,
                            priority,
                            Utils.getClassName(field),
                            FeedbackType.SINGLE_CHAR_NAME);
                    feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
                } else {
                    feedbackHolder.fixFeedback(holder.getProject(), filename,feedbackId);
                }
            }

            @Override
            public void visitDeclarationStatement(PsiDeclarationStatement statement) {
                super.visitDeclarationStatement(statement);

                if (Utils.hasErrorsInFile(statement)) {
                    return;
                }

                PsiElement[] elements = statement.getDeclaredElements();

                int index = 0;
                for (PsiElement e : elements) {
                    // Local variables (not in for loops)
                    if (e instanceof PsiLocalVariable) {
                        PsiLocalVariable localElement = (PsiLocalVariable) e;

                        String filename = statement.getContainingFile().getName();
                        int line = Utils.getLineNumber(localElement);
                        FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(localElement), INSPECTION_NAME, PsiStmtType.LOCAL_VAR, line);

                        if (!(statement.getParent() instanceof PsiForeachStatement || statement.getParent() instanceof PsiForStatement)) {
                            if (localElement.getName().length() == 1) {
                                Feedback feedback = new Feedback(line,
                                        filename,
                                        line + "-" + index + INSPECTION_NAME,
                                        priority,
                                        Utils.getClassName(statement),
                                        Utils.getMethodName(statement),
                                        FeedbackType.SINGLE_CHAR_NAME);
                                feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
                            } else {
                                feedbackHolder.fixFeedback(holder.getProject(), filename,feedbackId);
                            }
                        }
                    }

                    index++;
                }
            }

            @Override
            public void visitMethod(PsiMethod method) {
                super.visitMethod(method);

                if (Utils.hasErrorsInFile(method)) {
                    return;
                }

                // Method names
                String filename = method.getContainingFile().getName();
                int line = Utils.getLineNumber(method);
                String className = Utils.getClassName(method);
                FeedbackIdentifier feedbackId = new FeedbackIdentifier(Utils.getPointer(method), INSPECTION_NAME, PsiStmtType.METHOD, line);

                if (method.getName().length() == 1) {
                    Feedback feedback = new Feedback(line,
                            filename,
                            line + INSPECTION_NAME,
                            priority,
                            className,
                            FeedbackType.SINGLE_CHAR_NAME);
                    feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
                } else {
                    feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                }

                // Parameter names
                PsiParameterList paramList = method.getParameterList();
                PsiParameter[] params = paramList.getParameters();

                int index = 0;
                for (PsiParameter p : params) {
                    line = Utils.getLineNumber(p);
                    feedbackId = new FeedbackIdentifier(Utils.getPointer(p), INSPECTION_NAME, PsiStmtType.PARAMETER, line);

                    if (p.getName().length() == 1) {
                        Feedback feedback = new Feedback(line,
                                filename,
                                line + "-" + index + INSPECTION_NAME,
                                priority,
                                className,
                                FeedbackType.SINGLE_CHAR_NAME);
                        feedbackHolder.addFeedback(holder.getProject(), filename, feedbackId, feedback);
                    } else {
                        feedbackHolder.fixFeedback(holder.getProject(), filename, feedbackId);
                    }

                    index++;
                }
            }
        };
    }
}
