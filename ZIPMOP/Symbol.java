

import java.util.ArrayList;

/**
 * we are assuming that the grammar is in the normal form: for each Symbol A, there is only one rule it's on the right and
 * the rule is either A->BC or A->a.
 */

public abstract class Symbol {

    private ArrayList<Symbol> children;

    private String name;

    private String kprefix;

    public String getKprefix() {
        return kprefix;
    }


    public Symbol() {

    }

    public void setChildren(ArrayList<Symbol> children) {

        this.children = children;

    }

    public ArrayList<Symbol> getChildren() {

        return children;

    }

    public String getName() {

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setKprefix(String value){
        this.kprefix = value;
    }


    public abstract boolean isTerminal();

}
