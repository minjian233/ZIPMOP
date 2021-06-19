
import java.util.ArrayList;

public class NonTerminal extends Symbol {

    public NonTerminal(){

        this.setChildren(null);
    }

    public NonTerminal(String name){

        setName(name);
    }

    public NonTerminal(ArrayList<Symbol> children, String name){

        setChildren(children);

        setName(name);
    }

    @Override

    public boolean isTerminal(){

        return false;
    }


}
