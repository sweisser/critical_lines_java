/*
 * Created on 12.08.2006
 */
package com.weisser.quote.portfolio.cla;

import java.io.PrintStream;

public class States {

    /**
     * Set of In variables.
     */
    private CSet inVars;

    /**
     * Set of Out variables.
     */
    private CSet outVars;

    /**
     * Variable states.
     */
    private State[] state;

    /**
     * Initializes the variable states.
     *
     * @param size Number of variables.
     */
    public States(int size) {
        state = new State[size];          // ReDim State(1 To n + m) As Integer

        inVars = new CSet();             // Set inVars = New cSet
        inVars.initialize(size);        // TODO kann evtl. gleich in den Konstruktor wandern

        outVars = new CSet();            // Set outVars = New cSet
        outVars.initialize(size);
    }

    /**
     * @param j Variable number.
     * @return True if variable j is Out at upper limit.
     */
    public boolean isUp(int j) {
        return state[j] == State.STATE_UPPER;
    }

    /**
     * @param j Variable number.
     * @return True if variable j is Out at lower limit.
     */
    public boolean isLo(int j) {
        return state[j] == State.STATE_LOWER;
    }

    /**
     * Variable jIn goes IN.
     *
     * @param jIn The variable to move to "in" set.
     */
    public void goIn(int jIn) {
        outVars.delete(jIn);    // Delete from OUT set
        inVars.add(jIn);        // Add to IN set
        state[jIn] = State.STATE_IN;
    }

    /**
     * Variable jOut goes OUT.
     *
     * @param jOut The variable to move to "out" set.
     * @param outDirection The out direction, can be one of Markowitz.HIGHER or
     */
    public void goOut(int jOut, Direction outDirection, InputVariables inputVars) {
        inVars.delete(jOut);    // Delete from IN set
        // Add to OUT set if security or slack variable (not ABV).

        if (jOut <= inputVars.getNumSecurities() + inputVars.getNumSlackVars() - 1) {
            outVars.add(jOut);
        }
        if (outDirection == Direction.Higher) {
            state[jOut] = State.STATE_UPPER;
        } else {
            state[jOut] = State.STATE_LOWER;
        }
    }

    /**
     * Returns the number of variables in the "out" set.
     *
     * @return The number of variables in the "out" set.
     */
    public int getOutVarCount() {
        return outVars.count();
    }

    /**
     * Returns the number of variables in the "in" set.
     *
     * @return The number of variables in the "in" set.
     */
    public int getInVarCount() {
        return inVars.count();
    }

    /**
     * Returns the current position of the given "in" set member.
     *
     * @return the current position of the given "in" set member.
     */
    public int getInVarPosition(int member) {
        return inVars.position(member);
    }

    /**
     * Returns the current position of the given "out" set member.
     *
     * @return the current position of the given "out" set member.
     */
    public int getOutVarPosition(int member) {
        return outVars.position(member);
    }

    public int getOutVar(int member) {
        return outVars.elementAt(member);
    }

    public int getInVar(int member) {
        return inVars.elementAt(member);
    }

    /**
     * Add variable i to the "in" set.
     *
     * @param i the variable to add.
     */
    public void addInVar(int i) {
        inVars.add(i);
    }

    /**
     * Add variable i to the "out" set.
     *
     * @param i the variable to add.
     */
    public void addOutVar(int i) {
        outVars.add(i);
    }

    public void setState(int i, State val) {
        state[i] = val;
    }

    /**
     * Resizes the "in" and the "out" set to the given size. TODO Hier aufpassen
     * wegen der Arraygrenzen. Size sollte die gewünschte Arraygröße (Anzahl
     * Elemente angeben.)
     *
     * @param size The new size of both sets.
     */
    public void resize(int size) {
        inVars.resize(size);
        outVars.resize(size);
    }

    public void redimStates(int size) {
        state = Utility.redim(state, size);
    }

    public void dump(PrintStream out) {
        out.print("state   ");
        for (int i = 0; i < state.length; i++) {
            out.print(" " + state[i] + " ");
        }
        out.println();

        out.print("inVars  ");
        for (int i = 0; i < inVars.count(); i++) {
            out.print(" " + (inVars.elementAt(i)) + " ");
        }
        out.println();

        out.print("outVars ");
        for (int i = 0; i < outVars.count(); i++) {
            out.print(" " + (outVars.elementAt(i)) + " ");
        }
        out.println();
    }
}
