package com.weisser.quote.portfolio.cla;

import java.util.Arrays;

/**
 * All InputVariables for the optimization algorithm.
 *
 * For some methods, there is a specific order, in which they have to be called:
 *
 * <ul>
 *     <li>Set the number of securities.
 *     <li>Set the constraint types.
 *     <li>Call Init().
 *     <li>Set other parameters as desired.
 * </ul>
 */
public class InputVariables {

    /**
     * Number of securities (NumSecs).
     */
    private int numSecurities;

    /**
     * Number of slack variables (numSlackVars).
     */
    private int numSlackVars;

    /**
     * Number of variables n = numSecurities+numSlackVars.
     */
    private int numVariables;

    /**
     * Number of constraints m.
     */
    private int numConstraints;

    /**
     * Expected returns vector. Size: n elements (0..n-1)
     */
    private double[] mu;

    public double getExpectedReturn(int i) {
        return mu[i];
    }

    public void setExpectedReturn(int i, double r) {
        mu[i] = r;
    }

    public void setExpectedReturnSize(int n) {
        mu = Utility.redim(mu, n);
    }

    /**
     * Lower limits vector. Size: n+m elements (0..n+m-1)
     */
    public double[] lowerLimits;

    /**
     * Upper limits vector. Size: n+m elements (0..n+m-1)
     */
    public double[] upperLimits;

    /**
     * M matrix. Size: n+m rows, n+m columns (0..n+m-1)x(0..n+m-1)
     */
    public double[][] mMat;

    /**
     * Constraint coefficients matrix. Size: m rows, n+m columns
     * (0..m-1)x(0..n+m-1)
     * (Originally A[][])
     */
    public double[][] constraintLHS;

    /**
     * Constraint types array.
     */
    public ConstraintType[] conType;

    /**
     * Constraint right-hand sides (originally: b[])
     */
    public double[] constraintRHS;

    /**
     * Stop after this lambdaE (min 1E-5). Default = 0.00001.
     */
    private double endLambdaE = 0.00001;

    /**
     * Stop after this many corner portfolios. Default = 100.
     */
    private int maxCornerPortfolios = 100;

    /**
     * Set maximum number of corner portfolios. The default value for a new
     * InputVariables object is 100.
     *
     * @param max The new maximum number of corner portfolios.
     */
    public void setMaxCornerPortfolios(int max) {
        this.maxCornerPortfolios = max;
    }

    /**
     * Returns the current setting for the maximum number of corner portfolios.
     *
     * @return The current setting for the maximum number of corner portfolios.
     */
    public int getMaxCornerPortfolios() {
        return maxCornerPortfolios;
    }

    /**
     * Sets endLambdaE. The default value for a new InputVariables object is
     * 0.00001.
     *
     * @param lambda The new value for endLambdaE.
     */
    public void setEndLambdaE(double lambda) {
        this.endLambdaE = Math.max(lambda, 0.00000001);
    }

    /**
     * Returns the current value of endLambdaE.
     *
     * @return The current value of endLambdaE.
     */
    public double getEndLambdaE() {
        return endLambdaE;
    }

    /**
     * Set the number of securities in the portfolio.
     *
     * @param numSecurities The number of securities in the portfolio.
     */
    public void setNumSecurities(int numSecurities) {
        this.numSecurities = numSecurities;
        updateNumVariables();
    }

    /**
     * Returns the number of securities.
     *
     * @return The current number of securities.
     */
    public int getNumSecurities() {
        return numSecurities;
    }

    /**
     * Sets the number of constraints for optimization.
     *
     * @param numConstraints The number of constraints.
     */
    public void setNumConstraints(int numConstraints) {
        this.numConstraints = numConstraints;
    }

    /**
     * Returns the number of constraints.
     *
     * @return The number of constraints.
     */
    public int getNumConstraints() {
        return numConstraints;
    }

    /**
     * Number of variables n = numSecurities + numSlackVars.
     *
     * @return The number of variables.
     */
    public int getNumVariables() {
        return numVariables;
    }

    public void setNumVariables(int n) {
        this.numVariables = n;
    }

    public void updateNumVariables() {
        numVariables = numSecurities + numSlackVars;
    }

    /**
     * This method determines the number of slack variables and stores the
     * constraint types. It also determines the total number of variables. The
     * number of securities should have been set before calling this method.
     *
     * @param conTypes A char[] array with chars '=', '<' or '>' specifying the
     * constraint type.
     * @return 0 if everything went okay. -1 if there was an error, e.g. a wrong
     * constraint type.
     */
    private int setConTypes(char[] conTypes) {
        numSlackVars = 0;

        conType = new ConstraintType[numConstraints];

        for (int i = 0; i < numConstraints; i++) {
            switch (conTypes[i]) {
                case '=':
                    conType[i] = ConstraintType.EQUAL;
                    break;
                case '<':
                    conType[i] = ConstraintType.LESS_THAN;
                    numSlackVars++;
                    break;
                case '>':
                    conType[i] = ConstraintType.GREATER_THAN;
                    numSlackVars++;
                    break;
                default:
                    System.err.println("Wrong constraint type given.");
                    updateNumVariables();
                    return -1;
            }
        }

        updateNumVariables();
        return 0;
    }

    /**
     * Read a column vector from range r to V().
     *
     * @param src The source vector.
     * @param dest The destination vector.
     * @param numElements The number of elements in the vector.
     */
    public void readVector(double[] src, double[] dest, int numElements) {
        if (numElements >= 0) System.arraycopy(src, 0, dest, 0, numElements);
    }

    /**
     * Read a symmetric matrix from range r to matrix().
     *
     * @param src The source matrix.
     * @param dest The destination matrix.
     * @param numRows The number of rows in the matrix.
     */
    public void readSymMatrix(double[] src, double[][] dest, int numRows) {
        int sumIdx = 0;
        for (int i = 0; i < numRows; i++) {
            // For j = 1 To i
            for (int j = 0; j <= i; j++) {
                int srcIdx = sumIdx + j;

                dest[i][j] = src[srcIdx];
                //System.out.println("src[" + srcIdx + "] -> dest[" + i + ", " + j + "]");

                if (j != i) {
                    dest[j][i] = dest[i][j];
                }
            }
            sumIdx += i + 1;
        }
    }

    /**
     * Read a matrix from range r to matrix().
     *
     * @param src The source matrix.
     * @param dest The destination matrix.
     * @param numRows The number of rows in the matrix.
     * @param numCols The number of columns in the matrix.
     */
    public void readMatrix(double[][] src, double[][] dest, int numRows, int numCols) {
        for (int i = 0; i < numRows; i++) {
            if (numCols >= 0) System.arraycopy(src[i], 0, dest[i], 0, numCols);
        }
    }

    /**
     * Set the lower boundaries for the weights of each security.
     *
     * @param lowerBoundaries Array of lower boundaries. must have the save
     * length as the current setting of numSecurities.
     */
    public void setLowerBoundaries(double[] lowerBoundaries) {
        if (numSecurities != lowerBoundaries.length) {
            System.err.println("setLowerBoundaries: numSecurities (" + numSecurities + ") and lowerBoundaries.length (" + lowerBoundaries.length + ") mismatch");
        } else {
            readVector(lowerBoundaries, lowerLimits, numSecurities);
        }
    }

    /**
     * Set the upper boundaries for the weights of each security.
     *
     * @param upperBoundaries Array of upper boundaries. must have the save
     * length as the current setting of numSecurities.
     */
    public void setUpperBoundaries(double[] upperBoundaries) {
        if (numSecurities != upperBoundaries.length) {
            System.err.println("setUpperBoundaries: numSecurities (" + numSecurities + ") and upperBoundaries.length (" + upperBoundaries.length + ") mismatch");
        } else {
            readVector(upperBoundaries, upperLimits, numSecurities);
        }
    }

    /**
     * Set the expected returns for each security.
     *
     * @param expectedReturns Array of expected returns for each security.
     */
    public void setExpectedReturns(double[] expectedReturns) {
        if (numSecurities != expectedReturns.length) {
            System.err.println("setExpectedReturns: numSecurities (" + numSecurities + ") and expectedReturns.length (" + expectedReturns.length + ") mismatch");
        } else {
            readVector(expectedReturns, mu, numSecurities);
        }
    }

    /**
     * Set the constraint left hand side (LHS) and right hand side (RHS).
     *
     * @param constraintLHS Left hand side, a matrix with numConstraints rows
     * and numSecurities columns.
     * @param constraintRHS Right hand side, a vector with numConstraints
     * elements.
     */
    public void setConstraints(double[][] constraintLHS, double[] constraintRHS) {
        readMatrix(constraintLHS, this.constraintLHS, numConstraints, numSecurities);
        readVector(constraintRHS, this.constraintRHS, numConstraints);
    }

    /**
     * Set the default constraint that says the sum of all weights must be 1.0.
     * The method automatically creates the constraint arrays for LHS and RHS of
     * the correct size (according to the current setting of
     * <tt>numSecurities</tt>).
     */
    public void setDefaultConstraint() {
        double[][] lhs = new double[1][numSecurities];
        double[] rhs = {1.0};

        for (int i = 0; i < numSecurities; i++) {
            lhs[0][i] = 1.0;
        }

        readMatrix(lhs, this.constraintLHS, numConstraints, numSecurities);
        readVector(rhs, this.constraintRHS, numConstraints);
    }

    /**
     * Set the covariance matrix. The matrix is given as an double array here.
     * Because the matrix is symetric, we only pass one half of the matrix. If
     * you need a version that accepts the full matrix, use {@link #setCovarianceMatrix}
     *
     * @param covariance The covariance matrix in array form.
     */
    public void setCovariance(double[] covariance) {
        readSymMatrix(covariance, this.mMat, numSecurities);
    }

    /**
     * Set the covariance matrix. The matrix is given as an double array here.
     * Because the matrix is symetric, we only pass one half of the matrix. If
     * you need a version that accepts the full matrix, use {@link #setCovarianceMatrix}
     *
     * @param covarianceMat The covariance matrix of dimension numSecurities x
     * numSecurities.
     */
    public void setCovarianceMatrix(double[][] covarianceMat) {
        readMatrix(covarianceMat, this.mMat, numSecurities, numSecurities);
    }

    /**
     * Allocate all public arrays except <tt>conType()</tt>, which is allocated
     * by Inputs.Read. For variables used in Simplex phase 1 we increase the
     * number of variables by m for artificial basis variables.
     */
    public void init(int numSecurities, char[] conTypes) {
        setNumSecurities(numSecurities);

        setNumConstraints(conTypes.length);
        System.out.println("Constraints: " + conTypes.length);

        // TODO Refactoring: Better error handling. Define an Exception for this case.
        if (setConTypes(conTypes) == -1) {
            return;
        }

        mu = new double[numVariables];
        lowerLimits = new double[numVariables + numConstraints];
        upperLimits = new double[numVariables + numConstraints];
        Arrays.fill(upperLimits, Optimizer.INFINITY);

        mMat = new double[numVariables + numConstraints][numVariables + numConstraints];
        constraintLHS = new double[numConstraints][numVariables + numConstraints];
        constraintRHS = new double[numConstraints];
    }

    public int getNumSlackVars() {
        return numSlackVars;
    }

    public void setNumSlackVars(int numSlackVars) {
        this.numSlackVars = numSlackVars;
    }
}
