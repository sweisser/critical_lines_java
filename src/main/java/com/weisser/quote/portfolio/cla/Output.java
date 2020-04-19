package com.weisser.quote.portfolio.cla;

import java.io.FileOutputStream;
import java.io.PrintStream;

public interface Output {
    PrintStream simplexScreenOutputStream();
    PrintStream claScreenOutputStream();

    FileOutputStream simplexDebugFileOutputStream();
    FileOutputStream claDebugFileOutputStream();

    void openSimplexDebugFile();
    void closeSimplexDebugFile();

    void openCLADebugFile();
    void closeCLADebugFile();

    void dump(String name, int value);
    void dump(String name, double value);
    void dump(String name, double[][] m);

    void dumpVector(String name, int[] vector);
    void dumpVector(String name, double[] vector);

    void println_simplex(String s);
    void println_cla(String s);

    void resetDebugCount();
    void dumpDebugCount();

    void increaseDebugCount();
}
