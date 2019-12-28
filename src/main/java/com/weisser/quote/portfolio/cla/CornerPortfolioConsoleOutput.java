package com.weisser.quote.portfolio.cla;

import java.text.DecimalFormat;

/**
 * Output corner portfolios to stdout.
 *
 * TODO Collect corner portfolios in a collection.
 *
 * @author Stefan Weisser
 */
public class CornerPortfolioConsoleOutput implements CornerPortfolioOuput {

    /**
     * Creates a new output object.
     */
    public CornerPortfolioConsoleOutput() {
    }

    /**
     * Do initialization. See
     * com.weisser.portfolio.todd.CornerPortfolioOuput#setup().
     */
    @Override
    public void init() {
        System.out.println("CP Num  E        SD       LambdaE                a0                a1                a2  weights");
    }

    /**
     * Output one segment of the corner portfolio. See
     * com.weisser.portfolio.todd.CornerPortfolioOuput#cornerPortfolio(com.weisser.portfolio.todd.InputVariables,
     * com.weisser.portfolio.todd.OptimizerVariables)
     *
     * @param inputVars Input variables.
     * @param optimizerVars Optimizer variables.
     */
    @Override
    public void cornerPortfolio(InputVariables inputVars, OptimizerVariables optimizerVars, int clacount) {
        DecimalFormat f1 = new DecimalFormat("#0.00000");
        DecimalFormat f3 = new DecimalFormat("#0.000"); // for the weights
        DecimalFormat f4 = new DecimalFormat("#000"); // for the clacount

        System.out.print(f4.format(clacount) + "     ");
        System.out.print(f1.format(optimizerVars.portfolioExpectedReturn) + "  ");
        System.out.print(f1.format(Math.sqrt(optimizerVars.portfolioVariance)) + "  ");
        System.out.print(f1.format(optimizerVars.lambdaE) + "  ");

        if (optimizerVars.a0 != Optimizer.INVALID) {
            System.out.print(String.format("%1$16.8f", optimizerVars.a0) + "  ");
            System.out.print(String.format("%1$16.8f", optimizerVars.a1) + "  ");
            System.out.print(String.format("%1$16.8f", optimizerVars.a2) + "  ");
        } else {
            System.out.print("               -  ");
            System.out.print("               -  ");
            System.out.print("               -  ");
        }

        for (int j = 0; j < inputVars.getNumSecurities(); j++) {
            System.out.print(f3.format(optimizerVars.portfolioWeights[j]) + "  ");
        }

        System.out.println();
    }
}
