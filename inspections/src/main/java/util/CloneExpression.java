package util;

import com.intellij.psi.PsiElement;

import java.util.Set;

public class CloneExpression<T extends PsiElement> {

    private final Clone cloneType;
    private final T expr;
    private final String[] stringRep;
    private Pair<Integer, Integer> location;
    private Set<Integer> clones;

    private CloneExpression(Clone cloneType, T expr, String[] stringRep, Pair<Integer, Integer> location, Set<Integer> clones) {
        this.cloneType = cloneType;
        this.expr = expr;
        this.stringRep = stringRep;
        this.location = location;
        this.clones = clones;
    }

    public CloneExpression(T expr, String[] stringRep, Pair<Integer, Integer> location) {
        this(null, expr, stringRep, location, null);
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
