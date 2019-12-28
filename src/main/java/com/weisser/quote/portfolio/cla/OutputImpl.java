package com.weisser.quote.portfolio.cla;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class OutputImpl implements Output {
    public FileOutputStream simplexDebugFileOutputStream;
    public PrintStream simplexDebugFilePrintStream;

    public FileOutputStream claDebugFileOutputStream;
    public PrintStream claDebugFilePrintStream;

    public int dbgCount;

    public OutputImpl() {
        this.dbgCount = 0;
    }

    @Override
    public void println_simplex(String s) {
        simplexDebugFilePrintStream.println(s);
    }

    @Override
    public void println_cla(String s) {
        claDebugFilePrintStream.println(s);
    }

    @Override
    public void resetDebugCount() {
        this.dbgCount = 0;
    }

    @Override
    public void dumpDebugCount() {
        simplexDebugFilePrintStream.println(">> " + dbgCount);
    }

    @Override
    public void increaseDebugCount() {
        this.dbgCount++;
    }

    public void openSimplexDebugFile() {
        try {
            simplexDebugFileOutputStream = new FileOutputStream("java_simplex_debug.log");
            simplexDebugFilePrintStream = new PrintStream(simplexDebugFileOutputStream);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void openCLADebugFile() {
        try {
        claDebugFileOutputStream = new FileOutputStream("java_cla_debug.log");
        claDebugFilePrintStream = new PrintStream(claDebugFileOutputStream);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void closeSimplexDebugFile() {
        try {
            simplexDebugFilePrintStream.close();
            simplexDebugFileOutputStream.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void closeCLADebugFile() {
        try {
            claDebugFilePrintStream.close();
            claDebugFileOutputStream.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public PrintStream simplexScreenOutputStream() {
        return simplexDebugFilePrintStream;
    }

    @Override
    public PrintStream claScreenOutputStream() {
        return claDebugFilePrintStream;
    }

    @Override
    public FileOutputStream simplexDebugFileOutputStream() {
        return simplexDebugFileOutputStream;
    }

    @Override
    public FileOutputStream claDebugFileOutputStream() {
        return claDebugFileOutputStream;
    }

    @Override
    public void dumpVector(String name, int[] vector) {
        simplexDebugFilePrintStream.print(name);
        for (int i = 0; i < vector.length; i++) {
            simplexDebugFilePrintStream.print(" " + vector[i] + " ");
        }
        simplexDebugFilePrintStream.println();
    }

    @Override
    public void dumpVector(String name, double[] vector) {
        simplexDebugFilePrintStream.print(name);
        for (int i = 0; i < vector.length; i++) {
            simplexDebugFilePrintStream.print(" " + vector[i] + " ");
        }
        simplexDebugFilePrintStream.println();
    }

    @Override
    public void dump(String name, int value) {
        simplexDebugFilePrintStream.println(name + value);
    }

    @Override
    public void dump(String name, double value) {
        simplexDebugFilePrintStream.println(name + value);
    }

    @Override
    public  void dump(String name, double[][] m) {
        simplexDebugFilePrintStream.println(name);
        for (int i = 0; i < m.length; i++) {
            simplexDebugFilePrintStream.print("[ ");
            for (int j = 0; j < m[i].length; j++) {
                simplexDebugFilePrintStream.print(m[i][j] + "     ");
            }
            simplexDebugFilePrintStream.println(" ]");
        }
    }
}
