/*
 * Created on 13.08.2006
 */
package com.weisser.quote.portfolio.cla;

/**
 * The internal optimizer variables.
 *
 * TODO Refactoring - introduce getter / setter for the public variables.
 *      In the original VBA code, these were global variables btw.
 */
public class OptimizerVariables {
	/**
	 * Portfolio weights.
	 * Original name: X
	 */
	public double[] portfolioWeights;

	/**
	 * Inverse of IN columns of A (Simplex).
	 */
	public double[][] Ai;

	/**
	 * Portfolio expected return.
	 * Original name: E
	 */
	public double portfolioExpectedReturn;

	/**
	 * Portfolio variance.
	 * Original name: V
	 */
	public double portfolioVariance;

	/**
	 * LambdaE.
	 */
	public double lambdaE;

	/**
	 * V = f(E).
	 */
	public double a0, a1, a2;

	/**
	 * Allocate all public arrays. For variables
	 * used in simplex phase 1 we increase the number of
	 * variables by m for artificial basis variables.
	 */
	public OptimizerVariables(int n, int m) {
	    Ai = new double[m][m];
	    portfolioWeights = new double[n + m];
	}
}
