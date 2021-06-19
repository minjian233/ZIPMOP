
public class Op {


    public Operator operatorType;

    public boolean value;

    public Op leftChild;

    public Op rightChild;

    public boolean GNext;

    public boolean fixed;

    public char Proposition;

    // need this boolean to update Gnext when buffsize is 0. this boolean is different from the nonEmpty boolean in the checker class.
    public boolean nonEmpty;

    public Op(Operator type) {

        this.operatorType = type;

        if (type.isGlobally()) {

            value = true;
        } else {
            value = false;
        }
        fixed = false;

    }

    public void setLeftChild(Op child) {

        this.leftChild = child;
    }

    public void setRightChild(Op child) {

        this.rightChild = child;

    }


    public Op(char proposition) {
        this.operatorType = Operator.PROPOSITION;

        this.Proposition = proposition;
    }


    public boolean getVal() {

        return this.value;
    }
    //a Op is fixed if its value can no longer change.

    public boolean isFixed() {

        return this.fixed;
    }


    //
    // returns whether the h is changed and uopdate the color.

    /**
     * Delta function defined in the paper. It automatically updates the "h"(the valuation) and returns a boolean
     * indicating whether state changed.
     *
     * @param event events read
     * @param buff  buff value of the automaton
     * @return a boolean tells if state changed.
     */
    public boolean delta(char event, String buff) {


        boolean changed = false;

        if (isFixed()) {

            return changed;
        }

        if (operatorType.isProposition()) {

            this.value = (this.Proposition == event);

            return false;

        }
        // a global/final node will be reached if and only if it is not fixed, namely its value was true/false before the
        // current event is processed.
        if (operatorType.isGlobally()) {

            changed = this.leftChild.delta(event, buff);

            this.value = this.leftChild.value;

            if (this.value == false) {

                changed = true;

                this.fixed = true;
            }

            return changed;

        }

        if (operatorType.isEventually()) {

            changed = this.leftChild.delta(event, buff);

            this.value = this.leftChild.value;

            if (this.value == true) {

                changed = true;

                this.fixed = true;
            }

            return changed;

        }

        if (operatorType.isAnd()) {

            this.value = true;

            changed = this.leftChild.delta(event, buff) | this.rightChild.delta(event, buff);


            if (leftChild.value == false) {

                this.value = false;

                if (leftChild.isFixed()) {

                    this.fixed = true;
                }
                return changed;
            }

            if (rightChild.value == false) {

                this.value = false;

                if (rightChild.isFixed()) {

                    this.fixed = true;
                }
                return changed;
            }

        }

        if (operatorType.isOr()) {

            this.value = false;

            changed = this.leftChild.delta(event, buff);
            changed = changed | this.rightChild.delta(event, buff);


            if (leftChild.value == true) {

                this.value = true;

                if (leftChild.isFixed()) {

                    this.fixed = true;
                }
                return changed;
            }

            if (rightChild.value == true) {

                this.value = true;

                if (rightChild.isFixed()) {

                    this.fixed = true;
                }
                return changed;
            }
        }

        if (operatorType.isImplication()) {

            Op antecedent = leftChild;

            Op consequence = rightChild;

            changed = changed | antecedent.delta(event, buff) | consequence.delta(event, buff);

            if (antecedent.value == true && consequence.value == false) {

                this.value = false;

                this.fixed = antecedent.fixed && consequence.fixed;
            } else {

                this.value = true;

                if (consequence.value == true && consequence.isFixed()) {

                    this.fixed = true;
                }

            }


        }

        if (operatorType.isNot()) {

            changed = leftChild.delta(event, buff);

            this.value = !leftChild.value;
        }
        if (operatorType.isNext()) {
            //The case is very subtle. When Gnext is true, the formula is the form of X^kG(\varphi)
            //it is true if and only if X^{k-1} was true before its value gets updated
            //When buff size is 0, all X\varphi are false by definition.
            //

            //if the current subformula is X^kG\varphi
            if (GNext) {
                if (!nonEmpty) {
                    nonEmpty = true;
                    this.value = false;
                    return leftChild.delta(event, buff);
                }
                // value changed
                changed = this.value != leftChild.value;
                this.value = leftChild.value;
                if (leftChild.fixed) {
                    changed = true;
                    fixed = true;
                    return changed;
                }
                changed = leftChild.delta(event, buff) | changed;
            } else {
                changed = false;
                if (buff.length() == 0) {
                    this.value = false;
                    return false;
                }
                this.leftChild.delta(buff.charAt(0), buff.substring(1));

                this.value = leftChild.value;
            }


        }

        return changed;

    }

    /**
     * update values that are not tracked by state after postState is calculated. This function only needs to be
     * called once.
     *
     * @param event
     * @param prefix
     */
    public void updateNonState(char event, String prefix) {

        if (this.fixed) {
            return;
        }
        if (this.operatorType.isGlobally() || this.operatorType.isEventually()) {
            return;
        }
        if (this.GNext) {
            return;
        }
        if (operatorType.isProposition()) {

            this.value = (this.Proposition == event);

        }
        if (operatorType.isAnd()) {

            this.value = true;
            leftChild.updateNonState(event, prefix);
            rightChild.updateNonState(event, prefix);
            if (leftChild.value == false || rightChild.value == false) {

                this.value = false;

            }


        }

        if (operatorType.isOr()) {

            this.value = false;
            leftChild.updateNonState(event, prefix);
            rightChild.updateNonState(event, prefix);

            if (leftChild.value == true || rightChild.value == true) {

                this.value = true;

            }
        }

        if (operatorType.isImplication()) {

            leftChild.updateNonState(event, prefix);
            rightChild.updateNonState(event, prefix);

            Op antecedent = leftChild;

            Op consequence = rightChild;


            if (antecedent.value == true && consequence.value == false) {

                this.value = false;

            } else {
                this.value = true;
            }
        }

        if (operatorType.isNot()) {

            leftChild.updateNonState(event, prefix);

            this.value = !leftChild.value;
        }
        //subformula in ltl[x]
        if (operatorType.isNext()) {
            if (prefix.length() == 0) {
                this.value = false;
            }
            this.leftChild.updateNonState(prefix.charAt(0), prefix.substring(1));

            this.value = leftChild.value;
        }

    }


    public void printTree(int layer) {

        if (this.operatorType.isProposition()) {
            System.out.println("layer :" + layer + " with proposition: " + this.Proposition);
            return;
        } else {
            String formula = "";
            if (this.operatorType.isGlobally()) {
                formula = "G";
            } else if (this.operatorType.isEventually()) {
                formula = "F";
            } else if (this.operatorType.isImplication()) {
                formula = "->";
            } else if (this.operatorType.isImplication()) {
                formula = "->";
            } else if (this.operatorType.isOr()) {
                formula = "|";
            } else if (this.operatorType.isAnd()) {
                formula = "&";
            } else if (this.operatorType.isNot()) {
                formula = "!";
            } else if (this.operatorType.isNext()) {
                if (this.GNext) {
                    formula = "X_{G}";
                } else
                    formula = "X";

            }
            System.out.println("layer :" + layer + "with " + formula);
            leftChild.printTree(layer + 1);
            if (rightChild != null) {
                rightChild.printTree(layer + 1);
            }

        }
    }
}
