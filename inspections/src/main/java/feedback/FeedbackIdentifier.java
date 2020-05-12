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
    private final PsiStmtType stmtType;
    private final int line;

    public FeedbackIdentifier(SmartPsiElementPointer<PsiElement> pointer, SmartPsiElementPointer<PsiElement> clonePointer, String feedbackType, PsiStmtType stmtType, int line) {
        this.pointer = pointer;
        this.clonePointer = clonePointer;
        this.feedbackType = feedbackType;
        this.stmtType = stmtType;

        hasClonePointer = clonePointer != null;
        initialElement = pointer.getElement();
        this.line = line;
    }

    public FeedbackIdentifier(SmartPsiElementPointer<PsiElement> pointer, String feedbackType, PsiStmtType stmtType, int line) {
        this(pointer, null, feedbackType, stmtType, line);
    }

    public SmartPsiElementPointer<PsiElement> getPointer() {
        return pointer;
    }

    private String getFeedbackType() {
        return feedbackType;
    }

    public String getInitialElementText() {
        return initialElement.getText();
    }

    public PsiStmtType getStmtType() {
        return stmtType;
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
    public boolean equals(Object other) {
        if (other instanceof FeedbackIdentifier) {
            FeedbackIdentifier otherFeedbackId = (FeedbackIdentifier) other;

            PsiStmtType otherType = otherFeedbackId.getStmtType();

            if (otherType == stmtType &&
                    otherFeedbackId.getInitialElementText().equals(getInitialElementText()) &&
                    otherFeedbackId.getFeedbackType().equals(feedbackType)) {
                return true;
            }

            return otherType == stmtType &&
                    otherFeedbackId.getLine() == line &&
                    otherFeedbackId.getFeedbackType().equals(feedbackType);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return 29 * pointer.hashCode() + 13 * feedbackType.hashCode();
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
        sb.append("\t TYPE: "+ stmtType);

        sb.append("\n");
        sb.append("\t LINE: "+line);

        sb.append("\n");

        return sb.toString();
    }
}
