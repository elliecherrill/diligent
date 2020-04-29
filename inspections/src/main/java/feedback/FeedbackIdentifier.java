package feedback;

import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPsiElementPointer;
import util.PsiStmtType;

public class FeedbackIdentifier {

    private final SmartPsiElementPointer<PsiElement> pointer;
    private final SmartPsiElementPointer<PsiElement> clonePointer;
    private final boolean hasClonePointer;
    private final String feedbackType;
    private final PsiElement initialElement;
    private final PsiStmtType type;

    public FeedbackIdentifier(SmartPsiElementPointer<PsiElement> pointer, SmartPsiElementPointer<PsiElement> clonePointer, String feedbackType, PsiStmtType type) {
        this.pointer = pointer;
        this.clonePointer = clonePointer;
        this.feedbackType = feedbackType;
        this.type = type;

        hasClonePointer = clonePointer != null;
        initialElement = pointer.getElement();
    }

    public FeedbackIdentifier(SmartPsiElementPointer<PsiElement> pointer, String feedbackType, PsiStmtType type) {
        this(pointer, null, feedbackType, type);
    }

    public SmartPsiElementPointer<PsiElement> getPointer() {
        return pointer;
    }

    private String getFeedbackType() {
        return feedbackType;
    }

    public PsiElement getInitialElement() {
        return initialElement;
    }

    public PsiStmtType getType() {
        return type;
    }

    public boolean isDeleted() {
        if (hasClonePointer) {
            return clonePointer.getElement() == null || pointer.getElement() == null;
        }

        return pointer.getElement() == null;
    }

    @Override
    public String toString() {
        return pointer.getElement() + " : " + feedbackType + " : " + initialElement.getText() + " : " + type;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof FeedbackIdentifier) {
            FeedbackIdentifier otherFeedbackId = (FeedbackIdentifier) other;

            PsiStmtType otherType = otherFeedbackId.getType();
            PsiElement otherElement = otherFeedbackId.getInitialElement();

            if ((type == PsiStmtType.LEFT_THIS_EXPR || type == PsiStmtType.RIGHT_THIS_EXPR) && otherType == type) {
                return otherFeedbackId.getFeedbackType().equals(feedbackType);
            }

            return otherType == type &&
                    otherElement.getText().equals(initialElement.getText()) &&
                    otherFeedbackId.getFeedbackType().equals(feedbackType);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return 29 * pointer.hashCode() + 13 * feedbackType.hashCode();
    }
}
