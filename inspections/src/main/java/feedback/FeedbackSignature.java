package feedback;

import util.FeedbackType;
import util.ReportLevel;

public class FeedbackSignature {

    private final FeedbackType feedbackType;
    private final ReportLevel reportLevel;
    private final String location;

    public FeedbackSignature(FeedbackType feedbackType, ReportLevel reportLevel, String location) {
        this.feedbackType = feedbackType;
        this.reportLevel = reportLevel;
        this.location = location;
    }

    public FeedbackType getFeedbackType() {
        return feedbackType;
    }

    public ReportLevel getReportLevel() {
        return reportLevel;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj instanceof FeedbackSignature) {
            FeedbackSignature other = (FeedbackSignature) obj;

            return other.getReportLevel() == reportLevel &&
                    other.getFeedbackType() == feedbackType &&
                    location.equals(other.location);
        }

        return false;
    }
}
