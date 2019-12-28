package com.weisser.quote.portfolio.cla;

public class Optimizer {
	private Output debugOutput;

	/**
	 * Close to zero.
	 */
	public static final double EPSILON = 0.00000001;

	/**
	 * Internally used value for infinity.
	 */
	public static final double INFINITY = 1E+30;

	/**
	 * Internally used value for invalid.
	 */
	public static final double INVALID = 9.999E+99;

	/**
	 * The critical lines algorithm.
	 */
	private CriticalLines criticalLines;

	/**
	 * Output of the corner portfolios.
	 */
	private CornerPortfolioOuput output;

	/**
	 * States of the variables.
	 */
	private States variableStates;

	/**
	 * The input variables.
	 */
	private InputVariables inputVars;

	/**
	 * Internal optimizer variables.
	 */
	private OptimizerVariables optimizerVars;

	/**
	 * Constructor.
	 * @param input All input variables for the optimization.
	 */
	public Optimizer(InputVariables input, Output debugOutput) {
		this.debugOutput = debugOutput;
		this.inputVars = input;
		this.criticalLines = new CriticalLines();
		this.output = new CornerPortfolioConsoleOutput();
		this.variableStates = new States(inputVars.getNumVariables() + inputVars.getNumConstraints());
		this.optimizerVars  = new OptimizerVariables(inputVars.getNumVariables(), inputVars.getNumConstraints());
	}

	/**
	 * Main optimization routine.
	 */
	public void optimize() {
		Simplex simplex = new Simplex(inputVars.getNumVariables(), inputVars.getNumConstraints(), this.debugOutput);

	    // <M3> Set up inequality constraints and slack variables
	    // Index to next slack variable.
	    int j = inputVars.getNumSecurities();

	    for (int i = 0; i < inputVars.getNumConstraints(); i++) {
		    if (inputVars.conType[i] != ConstraintType.EQUAL) {
		        if (inputVars.conType[i] == ConstraintType.GREATER_THAN) {
					// convert "greater than" constraint to "less than"
			        for (int k = 0; k < inputVars.getNumSecurities(); k++) {
			        	inputVars.constraintLHS[i][k] = -inputVars.constraintLHS[i][k];
			        }
			        inputVars.constraintRHS[i] = -inputVars.constraintRHS[i];
		        }

		        // slack variable coefficient
		        inputVars.constraintLHS[i][j] = 1;
		        j++;
		    }
		}

	    // <M4> Setup for outputs
	    output.init();

	  	// <M5> run simplex algorithm
	    // TODO Initialization and Instantiation of Simplex.

		SimplexPhaseResult rc = simplex.run(variableStates, inputVars, optimizerVars);

		if (rc != SimplexPhaseResult.OK) {
		    // <M6> fatal error in simplex
		    if (rc == SimplexPhaseResult.ERROR_INFEASIBLE) {
		    	System.err.println("Infeasible problem. Check constraints and limits.");
		    } else if (rc == SimplexPhaseResult.ERROR_UNBOUNDED) {
		    	System.err.println("Unbounded E. Make sure you have a valid budget constraint.");
		    } else if (rc == SimplexPhaseResult.ERROR_DEGENERATE) {
		    	// Message was already displayed by Simplex.Run
		    	System.err.println("Degenerate Problem.");
		    }
		    return;
		}

		// <M7> Set up for critical line algorithm.
		criticalLines.setup(variableStates, inputVars, optimizerVars, debugOutput);

		// <M8> Trace out the efficient frontier.
		for (int clacount = 1; clacount <= inputVars.getMaxCornerPortfolios(); clacount++) {

			// TODO We could move some of the static variables used within iteration here!!!
			// TODO Place them in a class first and try the algorithm. Object of the class becomes a new parameter
			//   of iteration() and cornerPortfolio()

			criticalLines.iteration(variableStates, inputVars, optimizerVars, clacount);
			output.cornerPortfolio(inputVars, optimizerVars, clacount);

			if (optimizerVars.lambdaE < inputVars.getEndLambdaE()) {
				break; // Exit for loop
			}
		}
	}

	/**
	 * Returns the portfolio weights after the optimization.
	 * @return The portfolio weights after the optimization.
	 */
	public double[] getWeights() {
		return optimizerVars.portfolioWeights;
	}
}
