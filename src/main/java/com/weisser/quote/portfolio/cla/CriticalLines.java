package com.weisser.quote.portfolio.cla;

public class CriticalLines {
    private Output output;

    private double[] alphav;
    private double[] betav;
    private double[] xi;
    private double[] bbar;
    private double[][] Mi;

    /**
     * Variables for {@link #iteration(States, InputVariables, OptimizerVariables, int)}.
     */
    private int jMaxA;
    private Direction outDirection;
    private int jMaxB;
    private Direction inDirection;
    private double lambdaA, lambdaB;

    /**
     * Variables for calcCornerPortfolios().
     */
    private double oldLambdaE;

    /**
     * Initialize the Critical Line Algorithm.
     *
     * TODO Almost all methods of this class need access to variableStates, inputVars, optimizerVars.
     *      We should take them in the constructor or in setup and store them to shorten the parameter lists of
     *      the methods.
     */
    public void setup(States variableStates, InputVariables inputVars, OptimizerVariables optimizerVars, Output output) {
        int j0, j;
        int k0, k, i;
        double sum;
        int n = inputVars.getNumVariables();
        int m = inputVars.getNumConstraints();

        this.output = output;

        // In VB sind die mit ReDim alphav(1 To n + m) As Double angelegt.
        alphav = new double[n + m];
        betav = new double[n + m];
        xi = new double[n + m];
        bbar = new double[n + m];
        Mi = new double[n + m][n + m];

        for (j0 = 0; j0 < variableStates.getOutVarCount(); j0++) {
            j = variableStates.getOutVar(j0);   // variableStates.getOutVar(j0);
            alphav[j] = optimizerVars.portfolioWeights[j];
            betav[j] = 0.0;
        }

        for (j = n; j < n + m; j++) {
            variableStates.addInVar(j);
        }

        // <C4> Add A and A' to MMat (already contains C)
        for (i = 0; i < m; i++) {
            for (j = 0; j < n; j++) {
                inputVars.mMat[n + i][j] = inputVars.constraintLHS[i][j];
                inputVars.mMat[j][n + i] = inputVars.constraintLHS[i][j];
            }
        }

        // <C5> Compute bbar vector.
        for (j0 = 0; j0 < variableStates.getInVarCount(); j0++) {
            j = variableStates.getInVar(j0);

            if (j <= n - 1) {
                sum = 0.0;
            } else {
                sum = inputVars.constraintRHS[j - n];  // should be okay !!!
            }

            for (k0 = 0; k0 < variableStates.getOutVarCount(); k0++) {
                k = variableStates.getOutVar(k0);
                sum = sum - inputVars.mMat[j][k] * optimizerVars.portfolioWeights[k];
            }
            bbar[j] = sum;
        }

        //<C6> Set up initial Mi (M-bar-inverse)
        // Mi = |  0                Ai       |
        //      | Ai'   -Ai' * C(IN,IN) * Ai |

        // First copy Ai and Ai'
        for (j0 = 0; j0 < m; j0++) {
            j = variableStates.getInVar(j0);

            for (i = 0; i < m; i++) {
                Mi[n + i][j] = optimizerVars.Ai[j0][i];
                Mi[j][n + i] = Mi[n + i][j];
            }
        }

        // T = Ai' * C(IN,IN)
        double[][] T = new double[m][m];

        for (i = 0; i < m; i++) {
            for (j0 = 0; j0 < m; j0++) {
                j = variableStates.getInVar(j0);
                sum = 0;
                for (k = 0; k < m; k++) {
                    sum -= optimizerVars.Ai[k][i] * inputVars.mMat[variableStates.getInVar(k)][j];
                }
                T[i][j0] = sum;
            }
        }

        // Lower right portion of Mi is then T * Ai
        for (i = 0; i < m; i++) {
            for (j = 0; j < m; j++) {
                sum = 0.0;
                for (k = 0; k < m; k++) {
                    sum += T[i][k] * optimizerVars.Ai[k][j];
                }
                Mi[n + i][n + j] = sum;
            }
        }
    }

    /**
     * Iteration cycle.
     */
    public void iteration(States variableStates, InputVariables inputVars, OptimizerVariables optimizerVars, int clacount) {
        int j0, j, k0, k;
        double tempLambdaA, tempLambdaB;
        double alpha, beta, gamma, delta;

        variableStates.dump(output.simplexScreenOutputStream());

        // <C10> If this is not the first iteration, then add or delete the
        // variable determined in previous iteration.
        if (clacount > 1) {
            output.println_cla("<C10>" + clacount + "; lambdaA = " + lambdaA + " lambdaB = " + lambdaB);
            output.println_cla("<C10>" + clacount + "; jMaxA = " + jMaxA + " jMaxB = " + jMaxB);
            output.println_cla("<C10>" + clacount + "; inDirection = " + inDirection + " outDirection = " + outDirection);

            if (lambdaA > lambdaB) {
                deleteVariable(variableStates, inputVars, optimizerVars, jMaxA, outDirection, clacount);
            } else {
                addVariable(variableStates, inputVars, optimizerVars, jMaxB, inDirection, clacount);
            }
        }

        // <C11> Determine which IN variable wants to go OUT first.
        jMaxA = -1;
        lambdaA = 0;
        outDirection = Direction.InitialState;

        for (j0 = 0; j0 < variableStates.getInVarCount(); j0++) {
            // compute alpha and beta for variable.
            j = variableStates.getInVar(j0);
            alpha = 0;
            beta = 0;

            for (k0 = 0; k0 < variableStates.getInVarCount(); k0++) {
                k = variableStates.getInVar(k0);
                alpha = alpha + Mi[j][k] * bbar[k];
                //if (k <= inputVars.n) {
                if (k <= inputVars.getNumVariables() - 1) {
                    output.println_cla("<C11>" + clacount + "; k <= n; " + k + " <= " + (inputVars.getNumVariables() - 1));
                    beta = beta + Mi[j][k] * inputVars.getExpectedReturn(k);
                }
            }

            alphav[j] = alpha;
            betav[j] = beta;

            if (j < inputVars.getNumVariables() - 1) {
                output.println_cla("<C11>" + clacount + "; j < n; " + j + " <= " + (inputVars.getNumVariables() - 1));

                // For non-lambda variable check for going OUT.
                if (beta > Optimizer.EPSILON) {
                    // Check for hitting lower limit.
                    tempLambdaA = (inputVars.lowerLimits[j] - alpha) / beta;
                    if (tempLambdaA >= lambdaA) {
                        jMaxA = j;
                        lambdaA = tempLambdaA;
                        outDirection = Direction.Lower;
                    }
                } else if (inputVars.upperLimits[j] < Optimizer.INFINITY && beta < -Optimizer.EPSILON) {
                    // Check for hitting upper limit.
                    tempLambdaA = (inputVars.upperLimits[j] - alpha) / beta;
                    if (tempLambdaA >= lambdaA) {
                        jMaxA = j;
                        lambdaA = tempLambdaA;
                        outDirection = Direction.Higher;
                    }
                }
                output.println_cla("<C11>" + clacount + "; lambdaA = " + lambdaA);
            }
        }


        // <C12> Determine which OUT variable wants to come IN first.
        jMaxB = -1;
        lambdaB = 0;
        inDirection = Direction.InitialState;

        for (j0 = 0; j0 < variableStates.getOutVarCount(); j0++) {
            // Compute gamma and delta for variable.
            j = variableStates.getOutVar(j0);

            output.println_cla("<C12a>" + clacount + "; j0 = " + j0 + " j = " + j);

            gamma = 0;
            delta = -inputVars.getExpectedReturn(j);

            for (k = 0; k < inputVars.getNumVariables() + inputVars.getNumConstraints(); k++) {

                output.println_cla("<C12b>" + clacount + "; k = " + k);

                gamma += inputVars.mMat[j][k] * alphav[k];
                delta += inputVars.mMat[j][k] * betav[k];
            }

            output.println_cla("<C12b1>" + clacount + "; gamma = " + gamma);
            output.println_cla("<C12b2>" + clacount + "; delta = " + delta);

            if (variableStates.isLo(j)) {
                output.println_cla("<C12b3>" + clacount);

                if (delta > Optimizer.EPSILON) {
                    output.println_cla("<C12b4>" + clacount);

                    // Check for variable coming off lower limit.
                    tempLambdaB = -gamma / delta;

                    if (tempLambdaB >= lambdaB) {
                        jMaxB = j;
                        lambdaB = tempLambdaB;
                        inDirection = Direction.Higher;
                    }
                }
            } else {
                if (delta < -Optimizer.EPSILON) {   // 'at upper limit

                    output.println_cla("<C12b5>" + clacount);

                    // Check for variable coming off upper limit.
                    tempLambdaB = -gamma / delta;
                    if (tempLambdaB >= lambdaB) {
                        jMaxB = j;
                        lambdaB = tempLambdaB;
                        inDirection = Direction.Lower;
                    }
                }
                output.println_cla("<C12c>" + clacount + "; lambdaB = " + lambdaB);
            }
        }

        // <C13> The new lambda-E is the greater of lambda-A and lambda-B.
        // If lambda-A is greater, then a variable first goes OUT as
        // lambda-E is decreased. If lambda-B is greater, then a
        // variable first comes IN is as lambda-E is decreased.
        optimizerVars.lambdaE = Math.max(lambdaA, lambdaB);
        optimizerVars.lambdaE = Math.max(optimizerVars.lambdaE, 0);

        // <C14> Calculate the new corner portfolio, the E and v for
        // new corner portfolio, and a0, al, and a2 between this and
        // previous corner portfolio.
        calcCornerPortfolio(variableStates, inputVars, optimizerVars, clacount);
    }

    /**
     * Do updates required for variable jAdd to come IN.
     */
    private void addVariable(States variableStates, InputVariables inputVars, OptimizerVariables optimizerVars, int jAdd, Direction direction, int clacount) {
        int j0, j;
        int k0, k;
        double sum, xij;

        output.println_cla("<addvar>" + clacount + "; var = " + jAdd + " direction = " + direction);

        // <C20> update Mi for variable coming IN.
        // xi = Mi(IN,IN) * M(IN,jAdd);

        for (j0 = 0; j0 < variableStates.getInVarCount(); j0++) {
            j = variableStates.getInVar(j0);

            output.println_cla("<C20a>" + clacount + "; invars.count = " + variableStates.getInVarCount());
            output.println_cla("<C20a>" + clacount + "; j0 = " + j0 + " j = " + j);

            sum = 0;
            for (k0 = 0; k0 < variableStates.getInVarCount(); k0++) {
                k = variableStates.getInVar(k0);

                output.println_cla("<C20b>" + clacount + "; k0 = " + k0 + " k = " + k);

                sum += Mi[j][k] * inputVars.mMat[k][jAdd];
            }
            xi[j] = sum;
        }

        xij = inputVars.mMat[jAdd][jAdd];

        for (k0 = 0; k0 < variableStates.getInVarCount(); k0++) {
            k = variableStates.getInVar(k0);

            output.println_cla("<C20c>" + clacount + "; k0 = " + k0 + " k = " + k);

            xij -= inputVars.mMat[jAdd][k] * xi[k];
        }

        // Mi(IN,IN) += Xi*xi.T/xij
        // Mi(jAdd,IN) = Mi(IN,jAdd) = -xi / xij
        // for (j0 = 1; j0 <= variableStates.getInVarCount(); j0++) {
        for (j0 = 0; j0 < variableStates.getInVarCount(); j0++) {
            j = variableStates.getInVar(j0);

            output.println_cla("<C20d>" + clacount + "; j0 = " + j0 + " j = " + j);

            for (k0 = 0; k0 <= j0 - 1; k0++) {
                k = variableStates.getInVar(k0);

                output.println_cla("<C20e>" + clacount + "; k0 = " + k0 + " k = " + k);

                Mi[j][k] += xi[j] * xi[k] / xij;
                Mi[k][j] = Mi[j][k];
            }
            Mi[j][j] += xi[j] * xi[j] / xij;
            Mi[j][jAdd] = -xi[j] / xij;
            Mi[jAdd][j] = Mi[j][jAdd];
        }
        Mi[jAdd][jAdd] = 1.0 / xij;

        // <C21> Update bbar for the current IN variables
        for (j0 = 0; j0 < variableStates.getInVarCount(); j0++) {
            j = variableStates.getInVar(j0);

            output.println_cla("<C21>" + clacount + "; j0 = " + j0 + " j = " + j);

            bbar[j] += inputVars.mMat[j][jAdd] * optimizerVars.portfolioWeights[jAdd];
        }

        variableStates.goIn(jAdd);      // Variable jAdd goes IN

        // <C22> Compute bbar for new IN variable.
        sum = 0;

        for (j0 = 0; j0 < variableStates.getOutVarCount(); j0++) {
            j = variableStates.getOutVar(j0);

            output.println_cla("<C22>" + clacount + "; j0 = " + j0 + " j = " + j);

            sum -= inputVars.mMat[jAdd][j] * optimizerVars.portfolioWeights[j];
        }
        bbar[jAdd] = sum;
    }

    /**
     * Do updates required for variable jDel to go OUT.
     */
    private void deleteVariable(States variableStates,
                                InputVariables inputVars,
                                OptimizerVariables optimizerVars,
                                int jDel, Direction direction,
                                int clacount) {
        int j0, j, k0, k;

        output.println_cla("<delvar>" + clacount + "; var = " + jDel + " direction = " + direction);

        // <C30> update alpha and beta vectors for variable going OUT
        alphav[jDel] = optimizerVars.portfolioWeights[jDel];
        betav[jDel] = 0;
        variableStates.goOut(jDel, direction, inputVars);   // <C31> variable jDel goes OUT

        // <C32> Update Mi and bbar for variable going OUT.
        for (j0 = 0; j0 < variableStates.getInVarCount(); j0++) {
            j = variableStates.getInVar(j0);

            output.println_cla("<C32>" + clacount + "; j0 = " + j0 + " j = " + j);

            for (k0 = 0; k0 < variableStates.getInVarCount(); k0++) {
                k = variableStates.getInVar(k0);

                output.println_cla("<C32>" + clacount + "; k0 = " + k0 + " k = " + k);

                Mi[j][k] -= Mi[j][jDel] * Mi[jDel][k] / Mi[jDel][jDel];
            }
        }

        // <C33> Update bbar(IN)
        for (j0 = 0; j0 < variableStates.getInVarCount(); j0++) {
            j = variableStates.getInVar(j0);

            output.println_cla("<C33>" + clacount + "; j0 = " + j0 + " j = " + j);

            bbar[j] -= inputVars.mMat[j][jDel] * optimizerVars.portfolioWeights[jDel];
        }
    }

    /**
     * Calculate the new corner portfolio and statistics.
     */
    private void calcCornerPortfolio(States variableStates,
                                     InputVariables inputVars,
                                     OptimizerVariables optimizerVars,
                                     int clacount) {
        int j, j0, k;

        // <C40> Calculate the new corner portfolio.
        for (j0 = 0; j0 < variableStates.getInVarCount() - inputVars.getNumConstraints(); j0++) {
            j = variableStates.getInVar(j0);

            output.println_cla("<C40>" + clacount + "; j0 = " + j0 + " j = " + j);

            optimizerVars.portfolioWeights[j] = alphav[j] + betav[j] * optimizerVars.lambdaE;
        }

        // <C41> Calculate dE_dLambda
        double dE_dLambdaE = 0;

        for (j0 = 0; j0 < variableStates.getInVarCount() - inputVars.getNumConstraints(); j0++) {
            j = variableStates.getInVar(j0);

            output.println_cla("<C41>" + clacount + "; j0 = " + j0 + " j = " + j);

            dE_dLambdaE += betav[j] * inputVars.getExpectedReturn(j);
        }

        if (dE_dLambdaE < 0.000000001) {
            // <C42> "kink" in curve, compute E and v from scratch
            optimizerVars.a0 = Optimizer.INVALID;
            optimizerVars.a1 = Optimizer.INVALID;
            optimizerVars.a2 = Optimizer.INVALID;
            optimizerVars.portfolioExpectedReturn = 0;
            optimizerVars.portfolioVariance = 0;

            for (j = 0; j < inputVars.getNumSecurities(); j++) {
                optimizerVars.portfolioExpectedReturn += inputVars.getExpectedReturn(j) * optimizerVars.portfolioWeights[j];
                optimizerVars.portfolioVariance += inputVars.mMat[j][j] * optimizerVars.portfolioWeights[j] * optimizerVars.portfolioWeights[j];

                for (k = 0; k <= j - 1; k++) {
                    optimizerVars.portfolioVariance += 2 * inputVars.mMat[j][k] * optimizerVars.portfolioWeights[j] * optimizerVars.portfolioWeights[k];
                }
            }
        } else {
            // <C43> compute a0, al, a2, E, and V.
            optimizerVars.a2 = 1.0 / dE_dLambdaE;
            optimizerVars.a1 = 2.0 * (oldLambdaE - optimizerVars.a2 * optimizerVars.portfolioExpectedReturn);
            optimizerVars.a0 = optimizerVars.portfolioVariance - optimizerVars.a1 * optimizerVars.portfolioExpectedReturn - optimizerVars.a2 * optimizerVars.portfolioExpectedReturn * optimizerVars.portfolioExpectedReturn;
            optimizerVars.portfolioExpectedReturn += (optimizerVars.lambdaE - oldLambdaE) * dE_dLambdaE;
            optimizerVars.portfolioVariance = optimizerVars.a0 + optimizerVars.a1 * optimizerVars.portfolioExpectedReturn + optimizerVars.a2 * optimizerVars.portfolioExpectedReturn * optimizerVars.portfolioExpectedReturn;
        }
        oldLambdaE = optimizerVars.lambdaE;
    }

    public void dumpStates() {
        output.dumpVector("alphav   ", alphav);
        output.dumpVector("betav    ", betav);
    }
}
