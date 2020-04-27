package util;

import java.util.Set;

public class CloneExpression {

    private final String[] stringRep;
    private final Location location;
    private Set<Location> clones;

    private CloneExpression(String[] stringRep, Location location, Set<Location> clones) {
        this.stringRep = stringRep;
        this.location = location;
        this.clones = clones;
    }

    public CloneExpression(String[] stringRep, Location location) {
        this(stringRep, location, null);
    }

    public String[] getStringRep() {
        return stringRep;
    }

    public Location getLocation() {
        return location;
    }

    public Set<Location> getClones() {
        return clones;
    }

    public void setClones(Set<Location> clones) {
        this.clones = clones;
    }
}
