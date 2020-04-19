/*
 * Created on 30.08.2006
 */
package com.weisser.quote.portfolio.cla;

public class OptimizerTestApp {
    public static void main(String[] args) {

        // Test the CSet
        // TODO Move to tests.
        CSet c = new CSet(15);
        c.add(1);
        c.add(6);
        c.add(5);
        c.add(2);
        c.add(8);
        c.add(12);

        System.out.println(c);
        c.delete(6);
        System.out.println(c);
        c.deleteAt(2);
        System.out.println(c);

        //
        // <M2> Read the inputs
        //
        Inputs inputs = new Inputs();
        InputVariables inputVars = new InputVariables();
        inputs.read(inputVars);

        //
        // Init the optimizer and run optimize.
        //
        Output output = new OutputImpl();
        output.openSimplexDebugFile();
        output.openCLADebugFile();

        Optimizer m = new Optimizer(inputVars, output);
        m.optimize();

        output.closeSimplexDebugFile();
        output.closeCLADebugFile();
    }
}
