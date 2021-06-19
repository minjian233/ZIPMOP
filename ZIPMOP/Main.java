

import java.io.*;
import java.util.*;

public class Main {

    static HashMap<String,Character> events;
    static int numberOfEvents;
    static int nestedNext;
    public static void main(String[] args) throws IOException {
        numberOfEvents=65;
        nestedNext= 0;
        events = new HashMap<>();
        if(args.length<2){
            System.out.println("unable to parse command");
            System.exit(1);
        }
        String filename = args[0];
        String formula = "";
        String projection = "";
        formula = args[1];
        Symbol initialSymbol = SLP(filename);

        Op h = LTLparser(formula);

        //System.out.println(projectEvents);
        Checker checker = new Checker(nestedNext);
        boolean res = checker.modelChecking(h,initialSymbol);
        //conventionally return false on empty projected traces.
        if(!checker.nonEmpty){
            res = false;
        }
        System.out.println(res+" "+checker.timeElapsed);
/**

**/

    }

    /** the method parses an SLP file into symbol class.
     * The method assumes that every rule in the grammar is in the form of  "A -> B C ...N" where each symbol
     * is represented by a natural number. The initial symbol is assumed to be 0.  Terminal symbols
     * are marked by [] and each rule is separated by '\n'.
     * example: 0 -> 1 1\n 1 -> [0] 2\n  2 -> [1] [1] represents the string " 0 1 1 0 1 1" .
     *
     * @param filename
     * @return the methods returns the initial symbol.
     */
    public static Symbol SLP(String filename) {

        HashMap<String, Symbol> map = new HashMap<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));

            for (String line = reader.readLine().trim(); line != null; line = reader.readLine()) {
                String[] rule = line.split("->");
                String currentLabel = rule[0].trim();
                ArrayList<Symbol> childrenList = new ArrayList<>();
                String[] childrenLabel = rule[1].trim().split(" ");
                int length = childrenLabel.length;
                for (int i = 0; i < length; i++) {
                    String childLabel = childrenLabel[i];
                    Symbol currChild;
                    if (childLabel.charAt(0) != '[') {
                        if (map.containsKey(childLabel)) {
                            currChild =  map.get(childLabel);
                        } else {
                            currChild = new NonTerminal(childLabel);
                            map.put(childLabel, currChild);
                        }

                        childrenList.add(currChild);
                    } else {
                        String terminalValue = childLabel.substring(1, childLabel.length() - 1);
                        if (map.containsKey(childLabel)) {
                            currChild =  map.get(childLabel);
                        } else {
                            numberOfEvents++;
                            events.put(terminalValue,(char)numberOfEvents);
                            currChild = new Terminal(childLabel);
                            currChild.setKprefix(Character.toString((char)numberOfEvents));
                            map.put(childLabel, currChild);
                        }

                        childrenList.add(currChild);
                    }
                }

                Symbol currSymbol;
                if (!map.containsKey(currentLabel)) {
                    currSymbol = new NonTerminal(currentLabel);
                    map.put(currentLabel, currSymbol);
                } else {
                    currSymbol =  map.get(currentLabel);
                }

                currSymbol.setChildren(childrenList);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
/**
        try {
            FileWriter myWriter = new FileWriter("/Users/minjian/Desktop/badges/compressedAlgorithm/src/LTL/uncompress.txt",true);
            for(int i = 0; i<1000000;i++){
                if(map.containsKey(Integer.toString(i))){
                    String rule = Integer.toString(i)+" ->";
                    Symbol symbol = map.get(Integer.toString(i));
                    for(Symbol child:symbol.getChildren()){
                        rule+=" "+child.getName();
                    }
                    myWriter.write(rule+" "+"\n");

                }
                else{
                    break;
                }
            }

            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

**/
        return (Symbol) map.get("0");

    }

    /**
     * The method parses a given LTL formula. F,G X are temporal operators and &,|,!,-> are binary operators.
     * the subformula within a pair of parenthesis is assumed to be well-founded. "a (& b)" is not a valid input as
     * "& b" is not a well-founded formula. As mentioned in the paper, we require the user to push each "X" as far as
     * possible before feed into the parser.
     * @param formula
     * @return
     */

    public static Op LTLparser(String formula) {
        formula = formula+' ';
        Op root = null;
        Op prev = null;
        Set<Character> tokens = new HashSet<>(Arrays.asList('G', 'F','X','!',' ', '(',')','-','&','|'));
        int counter = 0;
        Stack<Character> stack = new Stack<>();
        int index = 0;
        boolean parenthesis;
        while (index < formula.length()) {
            parenthesis = false;
            char label = formula.charAt(index);
            Op node = null;
            if (!stack.isEmpty()) {
                if (label != ')') {
                    if (label == '(') {
                        counter++;
                    }
                    stack.push(label);
                    index++;
                    continue;
                } else {
                    counter--;
                    if (counter == 0) {
                        StringBuffer subformula = new StringBuffer();
                        while (!stack.isEmpty()) {
                            subformula.insert(0, stack.pop());
                        }
                        subformula.delete(0, 1);

                        node = LTLparser(subformula.toString());

                        parenthesis = true;
                    } else {
                        stack.push(label);
                        index++;
                        continue;
                    }
                }
            } else {
                if (label == ' ') {
                    index++;
                    continue;
                }
                if (label == 'G') {
                    node = new Op(Operator.GLOBALLY);
                } else if (label == 'F') {
                    node = new Op(Operator.EVENTUALLY);
                } else if (label == '!') {
                    node = new Op(Operator.NOT);
                } else if (label == 'X') {
                    node = new Op(Operator.NEXT);
                    int i = index;
                    int nextCounter = 0 ;
                    while (formula.charAt(i) == 'X' || formula.charAt(i) == '(') {
                        if(formula.charAt(i) == 'X'){
                            nextCounter++;
                        }
                        i++;
                        if (i == formula.length()) {
                            System.out.println("fail to parse the given LTL formula");
                            System.exit(0);
                        }
                    }
                    if (formula.charAt(i) == 'G') {
                        node.GNext = true;
                    }
                    else{
                        if(nextCounter>nestedNext){
                            nestedNext = nextCounter;
                        }
                    }
                } else if (label == '(') {
                    counter++;
                    stack.push(label);
                    index++;
                    continue;
                } else if (label == '&') {
                    node = new Op(Operator.AND);
                } else if (label == '|') {
                    node = new Op(Operator.OR);
                } else if (label == '-') {
                    if (index == formula.length() || formula.charAt(index + 1) != '>') {
                        System.out.println("fail to parse the given LTL formula1");
                        System.exit(1);
                    } else {
                        node = new Op(Operator.IMPLICATION);
                        index++;
                    }
                }
                //propositions
                else {
                    if (prev != null && prev.operatorType.isProposition()) {
                        System.out.println("fail to parse the given LTL formula2");
                        System.exit(1);
                    }
                    int eventSize = 0;
                    while (!tokens.contains(formula.charAt(index + eventSize))) {
                        eventSize++;
                    }

                    String event = formula.substring(index, index + eventSize);

                    if (events.containsKey(event)) {
                        node = new Op(events.get(event));
                    } else {
                        numberOfEvents++;
                        events.put(event, (char) (numberOfEvents));
                        node = new Op((char) (numberOfEvents));
                    }
                    if (eventSize != 0) {
                        index = index + eventSize - 1;
                    }
                }
            }
            //first symbol read
            if (prev == null) {
                prev = node;
                root = node;
                index++;
                continue;
            }
            // when the symhol read is "&","|","->", the prev node must be a complete subformula
            if (!parenthesis && (node.operatorType.isAnd() || node.operatorType.isOr() || node.operatorType.isImplication())) {

                node.setLeftChild(root);

                root = node;

                //otherwise
            } else {
                if (prev.operatorType.isAnd() || prev.operatorType.isOr() || prev.operatorType.isImplication()) {
                    prev.setRightChild(node);

                } else {
                    prev.setLeftChild(node);

                }

            }
            prev = node;
            index++;
        }

        return root;

    }


}
