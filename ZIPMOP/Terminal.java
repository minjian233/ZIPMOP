public class Terminal extends Symbol {

    public Terminal(String s){

        setChildren(null);

        setName(s);

    }
    @Override
    public boolean isTerminal(){

        return true;

    }



}
