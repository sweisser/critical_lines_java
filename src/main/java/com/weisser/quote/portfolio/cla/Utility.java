package com.weisser.quote.portfolio.cla;

/**
 * A utility class for resizing arrays without losing its content.
 */
public final class Utility {
    /**
     * Disable constructor.
     */
    private Utility() {
    }

    public static int[] redim(int[] oldArray, int newSize) {
        int[] newArray = new int[newSize];

        int minLength = Math.min(oldArray.length, newArray.length);
        System.arraycopy(oldArray, 0, newArray, 0, minLength);

        return newArray;
    }

    public static State[] redim(State[] oldArray, int newSize) {
        State[] newArray = new State[newSize];

        int minLength = Math.min(oldArray.length, newArray.length);
        System.arraycopy(oldArray, 0, newArray, 0, minLength);

        return newArray;
    }

    public static double[] redim(double[] oldArray, int newSize) {
        double[] newArray = new double[newSize];

        int minLength = Math.min(oldArray.length, newArray.length);
        System.arraycopy(oldArray, 0, newArray, 0, minLength);

        return newArray;
    }

    public static double[][] redim(double[][] oldMatrix, int newRowSize, int newColumnSize) {
        int oldRowSize = oldMatrix.length;
        int oldColumnSize = oldMatrix[0].length;

        int minRowSize = Math.min(oldRowSize, newRowSize);
        int minColumnSize = Math.min(oldColumnSize, newColumnSize);

        double[][] newMatrix = new double[newRowSize][newColumnSize];
        for (int i = 0; i < minRowSize; i++) {
            if (minColumnSize >= 0) System.arraycopy(oldMatrix[i], 0, newMatrix[i], 0, minColumnSize);
        }
        return newMatrix;
    }
}
