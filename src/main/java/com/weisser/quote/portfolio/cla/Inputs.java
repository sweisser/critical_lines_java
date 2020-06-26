package com.weisser.quote.portfolio.cla;

/**
 * Module Inputs.
 *
 * In the original code, this class was responsible for reading the input data of the optimization problem
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
    public void read(InputVariables inputVars) {
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
    }

    public void read1(InputVariables inputVars) {
        inputVars.setEndLambdaE(0.00001);

        char[] conTypes = {'='};
        inputVars.init(11, conTypes);

        //
        // Constraints
        //
        double[][] tmpA =
                {
                        { 1.00, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00 },
                };
        double[] tmpB = { 1.0 };
        inputVars.setConstraints(tmpA, tmpB);

        //
        // Lower and upper limits, expected returns.
        //
        double[] tmpL = { 0.003, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.002, 0.002 };
        inputVars.setLowerBoundaries(tmpL);

        double[] tmpU = { 0.003, 0.350, 0.350, 0.350, 0.100, 0.100, 0.350, 0.350, 0.350, 0.100, 0.100 };
        inputVars.setUpperBoundaries(tmpU);

        double[] tmpMu =
                {
                        -0.00180453535071047, -0.00159576562379307, 0.00504580504430248, 0.00693673030759708, 0.02025457623320640, 0.01881327391737990, 0.00819210032583716, 0.04959567136167170, 0.04883020747769260, 0.04458828189108920, 0.05638728131532670,
                };
        inputVars.setExpectedReturns(tmpMu);

        double[][] tmpCov =
                {
                        { 1.75032752535272E-07,  4.16815790023598E-06,  1.09013327353007E-05,  6.46384607479174E-06, 1.02465696051639E-05,    1.05416967482231E-05,    1.50054738205233E-05,    1.14779527601482E-05,    1.83172440963750E-06,    -1.65290582646624E-06,   1.00611245335712E-05, },
                        { 4.16815790023598E-06,  1.35424347501119E-05,  3.94346735773140E-05,  1.58353715471880E-05, 8.31825121087282E-06,    1.84479755409853E-05,    4.00111483491390E-05,    1.42975096463588E-05,    -3.13228419428149E-06,   7.55083453713765E-06,    5.71189853580017E-06, },
                        { 1.09013327353007E-05,  3.94346735773140E-05,  2.03077813068697E-04,  8.28038391861013E-05, 3.51687229590016E-06,    3.04146373101172E-05,    1.76633342868116E-04,    8.71942590644389E-06,    -8.73672589729711E-06,   9.66584807065849E-06,    6.13511689721174E-06, },
                        { 6.46384607479174E-06,  1.58353715471880E-05,  8.28038391861013E-05,  7.83599371352068E-05, 9.08306034441343E-05,    8.85415854124304E-05,    1.00366676992752E-04,    1.36205169583194E-04,    9.53899497458210E-05,    8.69295728533315E-05,    1.57411258984934E-04, },
                        { 1.02465696051639E-05,  8.31825121087282E-06,  3.51687229590016E-06,  9.08306034441343E-05, 3.82908262861540E-04,    3.76408518530427E-04,    9.73008916892730E-05,    4.80235018266035E-04,    2.24179437437307E-04,    1.35500814068050E-04,    4.90671630002765E-04, },
                        { 1.05416967482231E-05,  1.84479755409853E-05,  3.04146373101172E-05,  8.85415854124304E-05, 3.76408518530427E-04,    5.90090134502874E-04,    1.19053723649872E-04,    4.30077981657510E-04,    9.11566011847697E-05,    3.01122228826443E-05,    5.20442297877776E-04, },
                        { 1.50054738205233E-05,  4.00111483491390E-05,  1.76633342868116E-04,  1.00366676992752E-04, 9.73008916892730E-05,    1.19053723649872E-04,    2.31034562313384E-04,    1.86684778887628E-04,    9.67150813869892E-05,    1.19214319511525E-04,    1.69497573736519E-04, },
                        { 1.14779527601482E-05,  1.42975096463588E-05,  8.71942590644389E-06,  1.36205169583194E-04, 4.80235018266035E-04,    4.30077981657510E-04,    1.86684778887628E-04,    1.25635399730801E-03,    8.64638952112010E-04,    7.63842660936492E-04,    9.43065202860434E-04, },
                        { 1.83172440963750E-06,  -3.13228419428149E-06, -8.73672589729711E-06, 9.53899497458210E-05, 2.24179437437307E-04,    9.11566011847697E-05,    9.67150813869892E-05,    8.64638952112010E-04,    1.15295268921757E-03,    8.90587116868042E-04,    8.06621421335402E-04, },
                        { -1.65290582646624E-06, 7.55083453713765E-06,  9.66584807065849E-06,  8.69295728533315E-05, 1.35500814068050E-04,    3.01122228826443E-05,    1.19214319511525E-04,    7.63842660936492E-04,    8.90587116868042E-04,    1.49544134549717E-03,    7.48919958892219E-04, },
                        { 1.00611245335712E-05,  5.71189853580017E-06,  6.13511689721174E-06,  1.57411258984934E-04, 4.90671630002765E-04,    5.20442297877776E-04,    1.69497573736519E-04,    9.43065202860434E-04,    8.06621421335402E-04,    7.48919958892219E-04,    1.54404845021622E-03, },
                };

        inputVars.setCovarianceMatrix(tmpCov);
    }

}
