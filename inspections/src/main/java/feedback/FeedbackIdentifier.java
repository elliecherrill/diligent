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
    private final int line;

    public FeedbackIdentifier(SmartPsiElementPointer<PsiElement> pointer, SmartPsiElementPointer<PsiElement> clonePointer, String feedbackType, PsiStmtType type, int line) {
        this.pointer = pointer;
        this.clonePointer = clonePointer;
        this.feedbackType = feedbackType;
        this.type = type;

        hasClonePointer = clonePointer != null;
        initialElement = pointer.getElement();
        this.line = line;
    }

    public FeedbackIdentifier(SmartPsiElementPointer<PsiElement> pointer, String feedbackType, PsiStmtType type, int line) {
        this(pointer, null, feedbackType, type, line);
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

    //TODO: what about when you fix UPPER_CAMEL_CASE, x += 1 vs x = x + 1
    public String getInitialElementText() {
        return initialElement.getText().toLowerCase();
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

    public int getLine() {
        return line;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("\n");
        sb.append("\t POINTER: " +pointer.getElement());

        sb.append("\n");
        sb.append("\t CLONE POINTER: " + (hasClonePointer ? clonePointer.getElement() : "none"));

        sb.append("\n");
        sb.append("\t FEEDBACK TYPE: " +feedbackType);

        sb.append("\n");
        sb.append("\t INITIAL ELEMENT: "+initialElement.getText());

        sb.append("\n");
        sb.append("\t TYPE: "+type);

        sb.append("\n");
        sb.append("\t LINE: "+line);

        sb.append("\n");

        return sb.toString();
    }

    // TODO: for x = x + 1 >> x += 1 (these will be same element, i.e. initial element won't be null, but
    // different text - need to consider this)
    @Override
    public boolean equals(Object other) {
        if (other instanceof FeedbackIdentifier) {
            FeedbackIdentifier otherFeedbackId = (FeedbackIdentifier) other;

            PsiStmtType otherType = otherFeedbackId.getType();

            if ((type == PsiStmtType.LEFT_THIS_EXPR || type == PsiStmtType.RIGHT_THIS_EXPR) && otherType == type) {
                return otherFeedbackId.getFeedbackType().equals(feedbackType);
            }

            if (otherType == type &&
                    otherFeedbackId.getInitialElementText().equals(getInitialElementText()) &&
                    otherFeedbackId.getFeedbackType().equals(feedbackType)) {
                return true;
            }

            return otherType == type &&
                    otherFeedbackId.getLine() == line &&
                    otherFeedbackId.getFeedbackType().equals(feedbackType);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return 29 * pointer.hashCode() + 13 * feedbackType.hashCode();
    }
}
