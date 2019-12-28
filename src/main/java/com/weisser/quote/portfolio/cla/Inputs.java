package com.weisser.quote.portfolio.cla;

/**
 * Module Inputs.
 *
 * In the original code, this class is responsible for reading the input data of the optimization problem
 * from the excel sheet.
 *
 * Here we have hardwired the test case from the book.
 *
 * TODO Refactoring - InputVariables should be used as the interface to the algorithm.
 *   Inputs und InputVariables should be joined together.
 *   All InputVariables should not be changed during the optimization run.
 *   If copies are needed, wrap them in other objects.
 *
 * TODO Refactoring - Define a class for the constraints (constraint type, left hand side, right hand side).
 */
public class Inputs {

    /**
     * Fills the data into inputVars.
     *
     * @param inputVars The variables object to read into.
     * @return 0 if okay, -1 when an error occured during setup.
     */
    public int read(InputVariables inputVars) {
        inputVars.setEndLambdaE(0.00001);

        char[] conTypes = {'=', '>', '<'};
        inputVars.init(10, conTypes);

        //
        // Constraints
        //
        double[][] tmpA = {
            {1.00, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00},
            {1.00, 1.00, 1.00, 0.50, 0.00, 0.00, 0.00, 0.00, 0.00, 0.00},
            {0.00, 0.00, 0.00, 0.50, 1.00, 1.00, 1.00, 0.00, 0.00, 0.00}
        };
        double[] tmpB = {1.0, 0.2, 0.5};
        inputVars.setConstraints(tmpA, tmpB);

        //
        // Lower and upper limits, expected returns.
        //
        double[] tmpL = {0.10, 0.00, 0.00, 0.00, 0.10, 0.00, 0.00, 0.00, 0.00, 0.00};
        inputVars.setLowerBoundaries(tmpL);

        double[] tmpU = {0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30, 0.30};
        inputVars.setUpperBoundaries(tmpU);

        double[] tmpMu = {1.175, 1.190, 0.396, 1.120, 0.346, 0.679, 0.089, 0.730, 0.481, 1.080};
        inputVars.setExpectedReturns(tmpMu);

        double[] tmpCov = {
            0.4075516,
            0.0317584, 0.9063047,
            0.0518392, 0.0313639, 0.1949090,
            0.0566390, 0.0268726, 0.0440849, 0.1952847,
            0.0330226, 0.0191717, 0.0300677, 0.0277735, 0.3405911,
            0.0082778, 0.0093438, 0.0132274, 0.0052667, 0.0077706, 0.1598387,
            0.0216594, 0.0249504, 0.0352597, 0.0137581, 0.0206784, 0.0210558, 0.6805671,
            0.0133242, 0.0076104, 0.0115493, 0.0078088, 0.0073641, 0.0051869, 0.0137788, 0.9552692,
            0.0343476, 0.0287487, 0.0427563, 0.0291418, 0.0254266, 0.0172374, 0.0462703, 0.0106553, 0.3168158,
            0.0224990, 0.0133687, 0.0205730, 0.0164038, 0.0128408, 0.0072378, 0.0192609, 0.0076096, 0.0185432, 0.1107929};
        inputVars.setCovariance(tmpCov);

        return 0;
    }
}
