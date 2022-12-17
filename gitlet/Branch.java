package gitlet;
import java.io.Serializable;

public class Branch implements Serializable, Comparable<Branch> {
    String name;
    CTree.CNode pointer;

    public Branch(String name, CTree.CNode pointer) {
        this.name = name;
        this.pointer = pointer;
    }

    @Override
    public int compareTo(Branch other) {
        return this.name.compareTo(other.name);
    }

    @Override
    public boolean equals(Object o) {
        Branch b = (Branch) o;
        return this.name.equals(b.name);
    }
}
