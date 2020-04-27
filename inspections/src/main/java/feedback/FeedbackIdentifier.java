package feedback;

import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPsiElementPointer;
import util.PsiStmtType;

public class FeedbackIdentifier {

    private final SmartPsiElementPointer<PsiElement> pointer;
    private final String feedbackType;
    private final PsiElement initialElement;
    private final PsiStmtType type;
    private final String initElementTextPrefix;

    public FeedbackIdentifier(SmartPsiElementPointer<PsiElement> pointer, String feedbackType, PsiStmtType type, String initElementTextPrefix) {
        this.pointer = pointer;
        this.feedbackType = feedbackType;
        this.type = type;
        this.initElementTextPrefix = initElementTextPrefix;
        initialElement = pointer.getElement();
    }

    public FeedbackIdentifier(SmartPsiElementPointer<PsiElement> pointer, String feedbackType, PsiStmtType type) {
        this(pointer, feedbackType, type, null);
    }

    public SmartPsiElementPointer<PsiElement> getPointer() {
        return pointer;
    }

    public String getFeedbackType() {
        return feedbackType;
    }

    public PsiElement getInitialElement() {
        return initialElement;
    }

    public String getInitElementText() {
        if (initElementTextPrefix == null) {
            return initialElement.getText();
        }

        return initElementTextPrefix + initialElement.getText();
    }

    public PsiStmtType getType() {
        return type;
    }

    public boolean isDeleted() {
        return pointer.getElement() == null;
    }

    @Override
    public String toString() {
        return pointer.getElement() + " : " + feedbackType + " : " + getInitElementText() + " : " + type;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof FeedbackIdentifier) {
            FeedbackIdentifier otherFeedbackId = (FeedbackIdentifier) other;

            PsiStmtType otherType = otherFeedbackId.getType();

            if ((type == PsiStmtType.LEFT_THIS_EXPR || type == PsiStmtType.RIGHT_THIS_EXPR) && otherType == type) {
                return otherFeedbackId.getFeedbackType().equals(feedbackType);
            }

            return otherType == type &&
                    otherFeedbackId.getInitElementText().equals(getInitElementText()) &&
                    otherFeedbackId.getFeedbackType().equals(feedbackType);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return 29 * pointer.hashCode() + 13 * feedbackType.hashCode();
    }
}
