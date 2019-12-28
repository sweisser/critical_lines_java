
package com.weisser.quote.portfolio.cla;

/**
 * Interface for output (otr further processing) of the corner portfolios.
 */
public interface CornerPortfolioOuput {
	/**
	 * Display column headers on STDOUT.
	 */
	void init();

	/**
	 * Display the current corner portfolio on STDOUT.
	 * @param inputVars The input variables.
	 * @param optimizerVars The output variables.
	 */
	void cornerPortfolio(InputVariables inputVars, OptimizerVariables optimizerVars, int clacount);
}
