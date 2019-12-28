package com.weisser.quote.portfolio.cla;

// TODO Remove all IO classes. Create an interface that you can implement as a user.

public class Simplex {

    /**
     * A debug file handle.
     */
    public Output output;

    // In the book it was a Visual Basic Dialog Box displayed to the user.
    boolean allowDegenerate = true;

    /**
     * Objective function coefficients.
     */
    double[] z;

    /**
     * Price vector.
     */
    double[] price;

    /**
     * Profitability vector.
     */
    double[] profit;

    /**
     * Rate of adjustment of IN variables.
     */
    double[] adjRate;

    /**
     * Number of IN ABVs (artificial basis variables). (cant be made local, used in other methods read/write).
     */
    int numInABVs;

    public Simplex(int n, int m, Output output) {
        this.z = new double[n + m];
        this.price = new double[m];
        this.profit = new double[n];
        this.adjRate = new double[m];

        this.output = output;
    }

    public SimplexPhaseResult run(States variableStates, InputVariables inputVars, OptimizerVariables optimizerVars) {
        int i, j;

        int n = inputVars.getNumVariables();
        int m = inputVars.getNumConstraints();

        // <S1> initialize all variables other than ABVs (artifical basis variables) to be OUT at their
        // lower limits.
        for (j = 0; j < n; j++) {
            variableStates.addOutVar(j);
            variableStates.setState(j, State.STATE_LOWER);
            optimizerVars.portfolioWeights[j] = inputVars.lowerLimits[j];
            z[j] = 0;
        }

        // <S2> Set up ABVs.
        for (i = 0; i < m; i++) {
            double temp = inputVars.constraintRHS[i];

            for (j = 0; j < n; j++) {
                temp -= inputVars.constraintLHS[i][j] * inputVars.lowerLimits[j];
            }
            if (temp >= 0) {
                inputVars.constraintLHS[i][n + i] = 1;
            } else {
                inputVars.constraintLHS[i][n + i] = -1;
            }
            optimizerVars.Ai[i][i] = inputVars.constraintLHS[i][n + i];

            variableStates.addInVar(n + i); // inVars.Add n + i
            variableStates.setState(n + i, State.STATE_IN);

            optimizerVars.portfolioWeights[n + i] = Math.abs(temp);
            // objective function "expected return"
            z[n + i] = -1;
        }

        // starting number of IN ABVs
        numInABVs = m;

        // <S3> Run simplex phase 1
        SimplexPhaseResult returnCode = simplexPhase(SimplexPhase.PHASE_0, variableStates, inputVars, optimizerVars);

        output.println_simplex("Simplex phase one done.");
        output.dumpVector("x       ", optimizerVars.portfolioWeights);

        if (returnCode == SimplexPhaseResult.OK) {
            // <S4> No ABVs are IN (not degenerate).
            // Reallocate arrays to delete elements for ABVs.

            inputVars.lowerLimits = Utility.redim(inputVars.lowerLimits, n);
            inputVars.upperLimits = Utility.redim(inputVars.upperLimits, n);
            optimizerVars.portfolioWeights = Utility.redim(optimizerVars.portfolioWeights, n);
            inputVars.constraintLHS = Utility.redim(inputVars.constraintLHS, m, n);
            variableStates.redimStates(n);
        } else if (returnCode == SimplexPhaseResult.ERROR_DEGENERATE) {
            // <S5> Degenerate problem--One or more ABVs still IN.
            System.err.println("Degenerate Problem. Program will continue");
            System.err.println("WARNING, this part of code has not been tested until now.");

            if (allowDegenerate) {
                returnCode = SimplexPhaseResult.OK; // Allow program to continue
                inputVars.setNumVariables(n + m); // Add in ABVs to variable count.
                n = inputVars.getNumVariables();
                inputVars.setExpectedReturnSize(n);

                profit = new double[n];

                // Set upper limits an ABVs to zero
                for (i = 0; i < m; i++) {
                    inputVars.upperLimits[n - m + i] = Optimizer.EPSILON;
                }

                // increase size of MMat() while preserving contents.
                double[][] Mtemp = new double[inputVars.getNumSecurities()][inputVars.getNumSecurities()];

                for (i = 0; i < inputVars.getNumSecurities(); i++) {
                    for (j = 0; j < inputVars.getNumSecurities(); j++) {
                        Mtemp[i][j] = inputVars.mMat[i][j];
                    }
                }

                int newSize = n + m;
                inputVars.mMat = Utility.redim(inputVars.mMat, newSize, newSize);

                for (i = 0; i < inputVars.getNumSecurities(); i++) {
                    for (j = 0; j < inputVars.getNumSecurities(); j++) {
                        inputVars.mMat[i][j] = Mtemp[i][j]; // MMat(i, j) = Mtemp(i, j)
                        // objective function "expected return"
                    }
                }

                variableStates.resize(n + m);
            }
        }

        if (returnCode == SimplexPhaseResult.OK) {
            // <56> Run simplex phase 2
            // Objective is now to maximize expected return
            for (j = 0; j < n; j++) {
                z[j] = inputVars.getExpectedReturn(j);
            }
            returnCode = simplexPhase(SimplexPhase.PHASE_1, variableStates, inputVars, optimizerVars);
            if (returnCode == SimplexPhaseResult.OK) {
                // <S7> Ensure unique solution
                alterMu(variableStates, inputVars);
            }
        }

        // Erase z, Price, Profit, AdjRate
        z = null;
        price = null;
        profit = null;
        adjRate = null;

        return returnCode;
    }

    /**
     * Performs one simplex step.
     */
    private SimplexPhaseResult simplexPhase(SimplexPhase simplexPhase,
                                            States variableStates,
                                            InputVariables inputVars,
                                            OptimizerVariables optimizerVars) {
        int i0, i, j0, j;
        int k;
        Direction inDirection;
        int jMax = -1;			// in VB Version ist es 0, das passt aber wegen der anderen Indizes hier nicht. 
        double profitMax, sum;

        SimplexPhaseResult simplexPhaseResult = SimplexPhaseResult.OK;

        output.resetDebugCount();

        while (true) {
            // Debug
            dumpStates(variableStates, inputVars, optimizerVars);
            output.increaseDebugCount();

            // <S10> Compute price for each constraint.
            // price[i]: Price for the i-th artificial basis variable.
            for (i = 0; i < inputVars.getNumConstraints(); i++) {
                sum = 0;
                for (j = 0; j < variableStates.getInVarCount(); j++) {
                    sum -= optimizerVars.Ai[j][i] * z[variableStates.getInVar(j)];
                }
                price[i] = sum;
            }

            output.dumpVector("price   ", price);
            output.dump("A       ", inputVars.constraintLHS);
            output.dumpVector("z       ", z);

            // <S11> Compute profit for each "out" variable coming "in".
            // profit[j]: Profit for variable j.
            //
            profitMax = 0;             // ProfitMax = 0#
            for (j0 = 0; j0 < variableStates.getOutVarCount(); j0++) {
                j = variableStates.getOutVar(j0);
                sum = z[j];
                for (i = 0; i < inputVars.getNumConstraints(); i++) {
                    sum += inputVars.constraintLHS[i][j] * price[i];
                }
                if (variableStates.isUp(j)) {
                    sum = -sum;
                }
                profit[j] = sum;
                if (profit[j] >= profitMax) {
                    jMax = j;
                    profitMax = profit[j];
                }
            }

            output.dumpVector("profit  ", profit);

            if (profitMax < Optimizer.EPSILON) {
                // <S12> No profit from any OUT variable coming IN.
                if (simplexPhase == SimplexPhase.PHASE_0) {
                    // degenerate or infeasible problem.
                    for (j0 = 0; j0 < numInABVs; j0++) {
                        j = variableStates.getInVar(variableStates.getInVarCount() + 1 - j0);
                        if (optimizerVars.portfolioWeights[j] > Optimizer.EPSILON) {
                            // An IN ABV is not zero--infeasible problem
                            simplexPhaseResult = SimplexPhaseResult.ERROR_INFEASIBLE;
                            return simplexPhaseResult;
                        }
                    }
                    // All IN ABVS are zero--degenerate problem
                    simplexPhaseResult = SimplexPhaseResult.ERROR_DEGENERATE;  // (nIABV > 0)
                }
                break;   // Exit Do
            }

            if (variableStates.isUp(jMax)) {
                inDirection = Direction.Lower;
            } else {
                inDirection = Direction.Higher;
            }

            // <S13> Compute rate of adjustment for each IN variable as
            // variable jMax comes IN (AdjRate = - Ai * A(ALL,jMax)).
            for (i = 0; i < inputVars.getNumConstraints(); i++) {
                sum = 0;
                for (k = 0; k < inputVars.getNumConstraints(); k++) {
                    sum -= optimizerVars.Ai[i][k] * inputVars.constraintLHS[k][jMax];
                }
                if (inDirection == Direction.Lower) {
                    sum = -sum;
                }
                adjRate[i] = sum;
            }

            // <S14> Compute theta, the maximum amount that variable jMax
            // can change before another IN variable hits a limit and is
            // forced OUT. Also determine which variable is forced OUT.
            // Here we compute theta such that it will always be positive.
            double theta, tmpTheta;
            int iOut;
            Direction outDirection;

            //iOut = 0;    // 0 indicates variable coming In also goes out.
            iOut = -1;
            outDirection = inDirection;
            if (inputVars.upperLimits[jMax] == Optimizer.INFINITY) {
                theta = Optimizer.INFINITY;
            } else {
                theta = inputVars.upperLimits[jMax] - inputVars.lowerLimits[jMax];
            }

            for (i = 0; i < inputVars.getNumConstraints(); i++) {
                j = variableStates.getInVar(i);
                if (adjRate[i] < -Optimizer.EPSILON) {
                    // Check for variable hitting lower limit
                    tmpTheta = (inputVars.lowerLimits[j] - optimizerVars.portfolioWeights[j]) / adjRate[i];
                    if (tmpTheta < theta) {
                        theta = tmpTheta;
                        iOut = i;
                        outDirection = Direction.Lower;
                    }
                } else if (adjRate[i] > Optimizer.EPSILON && inputVars.upperLimits[j] != Optimizer.INFINITY) {
                    // Check for variable hitting upper limit
                    tmpTheta = (inputVars.upperLimits[j] - optimizerVars.portfolioWeights[j]) / adjRate[i];
                    if (tmpTheta < theta) {
                        theta = tmpTheta;
                        iOut = i;
                        outDirection = Direction.Higher;
                    }
                }
            }

            // <S15> Check for failure to find a variable to go OUT.
            if (theta == Optimizer.INFINITY) {
                simplexPhaseResult = SimplexPhaseResult.ERROR_UNBOUNDED;
                return simplexPhaseResult;
            }

            output.println_simplex("iOut   " + iOut);

            // Get "j" Index of variable going OUT.
            int jOut;

            // vital part: always check iOut when debugging
            if (iOut >= 0) {
                jOut = variableStates.getInVar(iOut);
            } else {
                jOut = jMax; // variable coming IN is also going OUT.
            }

            output.println_simplex("jOut   " + jOut);

            // <S16> update the IN variables (x's).
            for (i0 = 0; i0 < inputVars.getNumConstraints(); i0++) {
                j = variableStates.getInVar(i0);
                optimizerVars.portfolioWeights[j] += theta * adjRate[i0];
            }
            if (inDirection == Direction.Higher) {
                optimizerVars.portfolioWeights[jMax] += theta;
            } else {
                optimizerVars.portfolioWeights[jMax] -= theta;
            }

            // <S17> variable iMax goes IN
            variableStates.goIn(jMax);

            // <S18> variable gOut goes OUT
            variableStates.goOut(jOut, outDirection, inputVars);

            // <S19> Update Alnverse If var going OUT is not var coming IN.
            if (jMax != jOut) {
                updateAInverse(variableStates, inputVars, optimizerVars, iOut, jMax, inDirection);
            }

            output.println_simplex("nIABV   " + numInABVs);

            if (simplexPhase == SimplexPhase.PHASE_0 && jOut >= inputVars.getNumVariables()) {
                // Artificial basis variable went out
                numInABVs--;
                if (numInABVs == 0) {
                    break;    // Exit Do // All ABVS OUT--End of phase 1
                }
            }
        }
        return simplexPhaseResult;
    }

    /**
     * <S20> Update Ai (inverse if A(ALL,IN)) for new IN set.
     */
    private void updateAInverse(States variableStates,
                                InputVariables inputVars,
                                OptimizerVariables optimizerVars,
                                int iOut,
                                int jMax,
                                Direction InDirection) {
        double temp;
        int i, k;

        for (i = 0; i < inputVars.getNumConstraints(); i++) {
            if (i != iOut) {
                temp = adjRate[i] / adjRate[iOut];

                for (k = 0; k < inputVars.getNumConstraints(); k++) {
                    optimizerVars.Ai[i][k] -= optimizerVars.Ai[iOut][k] * temp;
                }
            }
        }

        if (InDirection == Direction.Higher) {
            temp = -adjRate[iOut];
        } else {
            temp = adjRate[iOut];
        }

        for (k = 0; k < inputVars.getNumConstraints(); k++) {
            optimizerVars.Ai[iOut][k] /= temp;
        }

        // <S21> Reorder rows of Ai to stay consistent with inVars.
        reorderAiRows(inputVars, optimizerVars, iOut, variableStates.getInVarPosition(jMax));
    }

    /**
     * Reorder the rows of Ai to stay consistent with Invars (ascending order).
     * In C or C++ this is more efficiently handled by manipulating pointers to
     * rows.
     */
    private void reorderAiRows(InputVariables inputVars, OptimizerVariables optimizerVars, int delRow, int addRow) {
        int i, j;
        double temp;

        if (addRow > delRow) {
            for (j = 0; j < inputVars.getNumConstraints(); j++) {
                temp = optimizerVars.Ai[delRow][j];

                for (i = delRow; i <= addRow - 1; i++) {
                    optimizerVars.Ai[i][j] = optimizerVars.Ai[i + 1][j];
                }
                optimizerVars.Ai[addRow][j] = temp;
            }
        } else if (addRow < delRow) {
            for (j = 0; j < inputVars.getNumConstraints(); j++) {
                temp = optimizerVars.Ai[delRow][j];
                for (i = delRow; i >= addRow + 1; i--) {
                    optimizerVars.Ai[i][j] = optimizerVars.Ai[i - 1][j];
                }
                optimizerVars.Ai[addRow][j] = temp;
            }
        }
    }

    /**
     * <S30> Alter mu's as required to ensure unique solution.
     */
    private void alterMu(States variableStates, InputVariables inputVars) {
        int j0, j;

        for (j0 = 0; j0 < variableStates.getOutVarCount(); j0++) {
            j = variableStates.getOutVar(j0);
            if (profit[j] > -0.000001) {
                double mu = inputVars.getExpectedReturn(j);
                if (variableStates.isLo(j)) {
                    inputVars.setExpectedReturn(j, mu - 0.000001);
                } else {
                    inputVars.setExpectedReturn(j, mu + 0.000001);
                }
            }
        }
    }

    /**
     * Dump important variables states at each step for debugging purposes.
     */
    private void dumpStates(States variableStates, InputVariables inputVars, OptimizerVariables optimizerVars) {
        output.dumpDebugCount();
        variableStates.dump(output.simplexScreenOutputStream());

        // dumpVector(out, "adjRate ", adjRate);
        output.dumpVector("x       ", optimizerVars.portfolioWeights);
        output.dump("nIABV    ", numInABVs);
        // out.println(variableStates); out.println(inputVars);
        // out.println(optimizerVars);
    }
}
