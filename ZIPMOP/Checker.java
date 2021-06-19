
import java.util.ArrayList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

/**
 * To understand the documentation for this class, a basic understanding of the paper is required.
 */
public class Checker {
    // map[s] returns a set of buff values such that (s,buff) has been checked within the current state
    static HashMap<String,HashSet<String>> map;

    //current valuation.
    static Op h;
    //the buff of the backward automata
    static StringBuffer buff;
    //size of the buff, namely the maximum depth of nested next of subformulas in ltl[x].
    int buffSize;

    long timeElapsed;
    //the prefix of the word. Namely the first buffsize+1 events.
    static String prefix;

    boolean nonEmpty;

    public Checker(int buffSize){

        map= new HashMap<String, HashSet<String>>();

        buff = new StringBuffer();

        this.buffSize = buffSize;


        this.nonEmpty = false;


    }

    public boolean modelChecking(Op formula, Symbol initialSymbol){

        h = formula;
        prefixInit(initialSymbol);
        long start = System.nanoTime();
        postState(initialSymbol);
        boolean res = this.evaluateFormula();
        this.timeElapsed = System.nanoTime()-start;

        return res;

    }

    /**
     * this method implement the pseudo code given in the paper. postState will keep updating the valuation
     * until it goes through the whole SLP grammar.
     * @param initialSymbol
     */

    public void postState(Symbol initialSymbol){

        int count = 0;

        Stack<Symbol> s = new Stack<>();

        s.push(initialSymbol);

        while(!s.isEmpty()){

            Symbol currentSymbol = s.pop();

            HashSet<String> set = map.get(currentSymbol.getName());
            //if the pair (currentSymbol,buff) is already executed in the current state, pop next symbol
            if(set!=null && set.contains(buff.toString())){
                //update buff
                updateBuff(currentSymbol);

                continue;
            }
            else{
                //symbol read for first timeElapsed in the current state
                if(set==null){
                    set = new HashSet<String>();
                    map.put(currentSymbol.getName(),set);
                }
                //record (currentSymbol, buff)
                set.add(buff.toString());
                if(currentSymbol.isTerminal()){

                    nonEmpty = true;
                    //else if h changes, clear map.
                    if(h.delta(currentSymbol.getKprefix().charAt(0), buff.toString())){

                        map.clear();
                    }

                    updateBuff(currentSymbol);
                }
                else {

                    for(int i = 0; i<currentSymbol.getChildren().size();i++){

                        s.push(currentSymbol.getChildren().get(i));

                    }


                }
            }

        }

    }

    /**
     * Note that the postState function only computes state, which are valuations for all F,G,and some X subformulas.
     * Sometimes the value of a ltl formula also depends on other subformulas in ltl[x]. For example: a & G(b). The
     * post state functions only tells us the value of  G(b) so we need to do another step to calculate the value for
     * a & G(b). All we need to do is to find the first buffsize+1 events and run delta again.
     */
    public boolean evaluateFormula(){
        //empty projection
        if(prefix.length()==0){
            return false;
        }
        else{
            h.updateNonState(prefix.charAt(0),prefix.substring(1));
            return h.getVal();
        }
    }

    /**
     * This method initializes the kPrefix attribute for each symbol, which is a string of the first k bits of
     * its language. We do not penalize the running timeElapsed by adding timeElapsed taken to run this method because it could be
     * done simultaneously when reading in the grammar.
     *
     * @param symbol
     */

    public void prefixInit(Symbol symbol)

    {
        if(symbol.isTerminal()){

            return;

        }
        if(symbol.getKprefix()==null){

            ArrayList<Symbol> children = symbol.getChildren();

            StringBuffer suffix = new StringBuffer();


            for(int i = 0 ; i<symbol.getChildren().size();i++){

                Symbol currentChild = children.get(i);
                if(currentChild.getKprefix()==null){
                    if(!currentChild.isTerminal())
                        prefixInit(currentChild);
                }

                if(currentChild.isTerminal()){
                    char e = currentChild.getKprefix().charAt(0);
                    if(suffix.length()< buffSize+1){
                        suffix.append(e);
                    }
                }
                else{
                    String childSuffix= currentChild.getKprefix();
                    if(suffix.length()< buffSize+1)
                        suffix.append(childSuffix);

                }

            }
            //inital symbol
            int end = buffSize+1 > suffix.length()? suffix.length():buffSize+1;
            if(symbol.getName().equals("0")){

                prefix = suffix.substring(0,end);
            }

            symbol.setKprefix(suffix.substring(0,end));
        }
    }


    public void updateBuff(Symbol currentSymbol)

    {
        if(buffSize==0){
            return;
        }
        if(currentSymbol.isTerminal()){

                buff.insert(0,currentSymbol.getKprefix());

        }
        else{
            buff.insert(0,currentSymbol.getKprefix());
        }
        if(buffSize < buff.length()){


            buff.delete(buffSize, buff.length());


        }


    }

}

