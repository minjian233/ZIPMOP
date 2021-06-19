
public enum Operator {

    AND, OR, IMPLICATION, EVENTUALLY, GLOBALLY, NOT, PROPOSITION, NEXT;

    public boolean isAnd(){

        return this.ordinal() == AND.ordinal() ;
    }

    public boolean isOr(){

        return this.ordinal() == OR.ordinal();
    }

    public boolean isImplication(){

        return this.ordinal() == IMPLICATION.ordinal();

    }

    public boolean isEventually(){

        return this.ordinal() == EVENTUALLY.ordinal();

    }
    public boolean isGlobally(){

        return this.ordinal() == GLOBALLY.ordinal();
    }

    public boolean isNot(){

        return this.ordinal() == NOT.ordinal();
    }

    public boolean isProposition() {

        return this.ordinal() == PROPOSITION.ordinal();
    }

    public boolean isNext(){

        return this.ordinal() == NEXT.ordinal();
    }

}
