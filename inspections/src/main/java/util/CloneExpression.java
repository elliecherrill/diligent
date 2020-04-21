package util;

import java.util.Set;

public class CloneExpression {

    private final String[] stringRep;
    private Pair<Integer, Integer> location;
    private Set<Integer> clones;

    private CloneExpression(String[] stringRep, Pair<Integer, Integer> location, Set<Integer> clones) {
        this.stringRep = stringRep;
        this.location = location;
        this.clones = clones;
    }

    public CloneExpression(String[] stringRep, Pair<Integer, Integer> location) {
        this(stringRep, location, null);
    }

    public String[] getStringRep() {
        return stringRep;
    }

    public Pair<Integer, Integer> getLocation() {
        return location;
    }

    public Set<Integer> getClones() {
        return clones;
    }

    public void setClones(Set<Integer> clones) {
        this.clones = clones;
    }
}
