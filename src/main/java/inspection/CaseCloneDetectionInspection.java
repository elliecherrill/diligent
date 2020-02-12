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

                PsiStatement[][] cases = CodeCloneUtils.getCaseBlocks(switchBody);

                Map<PsiElement, String[]> elementMap = new HashMap<>();

                for (PsiStatement[] c : cases) {
                    for (PsiStatement stat : c) {
                        if (stat != null) {
                            elementMap.put(stat, CodeCloneUtils.getStatAsStringArray(stat));
                        }
                    }
                }

                for (Map.Entry<PsiElement, String[]> entry : elementMap.entrySet()) {
                    for (Map.Entry<PsiElement, String[]> otherEntry : elementMap.entrySet()) {
                        PsiElement entryKey = entry.getKey();
                        PsiElement otherEntryKey = otherEntry.getKey();

                        String[] entryValue = entry.getValue();
                        String[] otherEntryValue = otherEntry.getValue();

                        if (entryKey.equals(otherEntryKey)) {
                            continue;
                        }

                        if (Arrays.equals(entryValue, otherEntryValue)) {
                            holder.registerProblem(entryKey,
                                    "Duplicate expression in switch case (" + entryKey.getText() + " " + otherEntryKey.getText() + ")",
                                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                        }

                        if (entryKey instanceof PsiExpressionStatement && otherEntryKey instanceof PsiExpressionStatement) {
                            if (CodeCloneUtils.changeInLiteral(entryValue, otherEntryValue)) {
                                holder.registerProblem(entryKey,
                                        "Similar expression in switch case - differs by RHS (" + entryKey.getText() + " " + otherEntryKey.getText() + ")",
                                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                            }

//                          if (CodeCloneUtils.changeInOp(entry.getValue(), otherEntry.getValue())) {
//                              holder.registerProblem(otherEntry.getKey(), "Similar expression in switch case (differs by RHS)", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
//                          }
                        }

                        if (entryKey instanceof PsiIfStatement && otherEntryKey instanceof PsiIfStatement) {
                            if (CodeCloneUtils.sameCondition(entryValue, otherEntryValue)) {
                                PsiIfStatement ifStmt = (PsiIfStatement) entryKey;
                                holder.registerProblem(ifStmt.getCondition(),
                                        "Same 'if' condition (" + entryKey.getText() + " " + otherEntryKey.getText() + ")",
                                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                            }
                        }
                    }
                }
            }
        };
    }
}