package feedback;

import util.TipType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TipHolder {

    private final Map<TipType, Tip> tips;

    private boolean isCurrent;

    public TipHolder() {
        tips = new ConcurrentHashMap<>();
        isCurrent = true;
    }

    public boolean isEmpty() {
        return tips.size() == 0;
    }

    public void update() {
        isCurrent = true;
    }

    public boolean addTip(TipType tipType, String filename) {
        Tip tip = tips.get(tipType);

        if (tip == null) {
            tip = new Tip(tipType);
            tips.put(tipType, tip);
            isCurrent = false;
        } else {
            boolean updated = tip.removeSatisfied(filename);
            if (updated) {
                isCurrent = false;
            }
        }

        return isCurrent;
    }

    public boolean fixTip(TipType tipType, String filename) {
        Tip tip = tips.get(tipType);

        boolean updated;

        if (tip == null) {
            tip = new Tip(tipType);
            updated = tip.addSatisfied(filename);
            tips.put(tipType, tip);
        } else {
            updated = tip.addSatisfied(filename);
        }

        if (updated) {
            isCurrent = false;
        }

        return isCurrent;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public String toHTMLString() {
        return "<div onClick=\"location.href = './project-tips.html'\" id=\"file\">\n" +
                "   <div id=\"filecontainer\">\n" +
                "       <div id=\"filetitle\">\n" +
                "           Project Tips \n" +
                "       </div>\n" +
                "   </div> " +
                "</div>\n";
    }

    public String toSelectedHTMLString() {
        return "<div onClick=\"location.href = './project-tips.html'\" id=\"file\" style=\"font-weight: 550;border: 5px solid white;\">\n" +
                "   <div id=\"filecontainer\">\n" +
                "       <div id=\"filetitle\">\n" +
                "           Project Tips \n" +
                "       </div>\n" +
                "   </div> " +
                "</div>\n";
    }

    public String getTipsAsHTMLString() {
        StringBuffer sb = new StringBuffer();

        for (Map.Entry entry : tips.entrySet()) {
            Tip t = (Tip) entry.getValue();
            sb.append(t.toHTMLString());
        }

        return sb.toString();
    }
}
