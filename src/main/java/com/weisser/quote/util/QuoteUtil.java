package com.weisser.quote.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Various utility functions.
 * @author Stefan Weisser
 */
public class QuoteUtil {

	//
	// ANSI Codes.
	//
	public static String ANSI_RED = "\033[31m";
	public static String ANSI_GREEN = "\033[32m";
	public static String ANSI_BLUE = "\033[34m";
	public static String ANSI_NORMAL = "\033[0m";
	public static String ANSI_WHITE = "\033[37m";
	public static String ANSI_GREY = "\033[0;37m";

	/**
	 * Format types for {@link #round} and {@link #roundByFactor}.
	 */
	public static enum FormatType { fix, exp }

	/**
	 * Runden mittels BigDecimal.
	 * @param  d der zu rundende Gleitkommawert.
	 * @param  scale die Anzahl der Nachkommastellen, falls type = fix,
	 *         die Anzahl der tragenden Stellen - 1,  falls type = exp.
	 *         scale sollte >= 0 sein (negative Werte werden auf 0 gesetzt).
	 * @param  mode die Rundungsart: einer der Rundungsarten von BigDecimal,
	 *         seit 1.5 in java.math.RoundingMode.
	 * @param  type ein Element von "enum FormatType {fix, exp}" gibt an,
	 *         auf welche Stellen sich die Rundung beziehen soll.
	 *         FormatType.exp ('Exponential') steht für tragende Stellen,
	 *         FormatType.fix ('Fixkomma') steht für Nachkommastellen.
	 * @return Der gerundete Gleitkommawert.
	 * Anmerkung: Für die Werte double NaN und ±Infinity
	 * liefert round den Eingabewert unverändert zurück.
	 * 
	 * Beispiel:
     * <p/> 
	 * <pre>
	 * double d = -Math.exp(702);
     * 
     * for (int scale = 0; scale &lt; 6; scale++) {
	 *     System.out.println(round(d,scale,RoundingMode.HALF_EVEN,FormatType.exp));
     * }
	 * </pre>
	 */
	public static double round(double d, int scale, RoundingMode mode, FormatType type) {
		if (Double.isNaN(d) || Double.isInfinite(d)) {
			return d;
		}
		scale = Math.max(scale, 0);  // Verhindert negative scale-Werte
		BigDecimal bd = BigDecimal.valueOf(d);
		if (type == FormatType.exp) {
			BigDecimal bc = new BigDecimal(bd.unscaledValue(), bd.precision() - 1);
			return ((bc.setScale(scale, mode)).scaleByPowerOfTen(bc.scale() - bd.scale())).doubleValue();
		}
		return (bd.setScale(scale, mode)).doubleValue();
	}

	public static double round(double d, int scale) {
		return round(d, scale, RoundingMode.HALF_EVEN, QuoteUtil.FormatType.fix);
	}
	
	/**
	 * Round by division through the given factor.
	 * @param d The double to be rounded.
	 * @param roundingFactor The rounding factor.
	 * @return The rounded double.
	 */
	public static double roundByFactor(double d, double roundingFactor) {
		return Math.round(d * roundingFactor) / roundingFactor;
	}

	/**
	 * Format a string that it has exactly size characters.
	 *
	 * For strings, that are longer or have a length equal to size,
	 * only the first size characters will be returned.
	 *
	 * For strings, that are shorter, spaces are added to the right.
	 *
	 * For null strings, a string object consisting of exactly size spaces
	 * will be returned.
	 *
	 * TODO To improve performance, let the caller allocate the buffer of
	 * correct size and reuse it. Useful, if the caller is doing many calls of
	 * the same desired string size.
	 *
	 * @param s
	 * @param size
	 * @return The formatted string.
	 */

	public static String formatFixedLength(String s, int size) {
		StringBuilder buf = new StringBuilder(size);

		if (s == null) {
			while(buf.length() < size) {
				buf.append(' ');
			}
			return buf.toString();
		} else if (s.length() < size) {
			buf.append(s);
			while(buf.length() < size) {
				buf.append(' ');
			}
			return buf.toString();
		} else if (s.length() >= size) {
			return s.substring(0, size);
		}

		return buf.toString();
	}


	/**
	 * Displays the given matrix of doubles on a PrintStream.
	 * @param out The PrintStream.
	 * @param name The name of matrix (It's just used to identify it in the print stream).
	 * @param m The matrix of doubles.
	 */
	public static void showMatrix(PrintStream out, String name, double[][] m) {
		out.println(name);
		for (int i = 0; i < m.length; i++) {
			out.print("[ ");
			for (int j = 0; j < m[i].length; j++) {
				out.print(m[i][j] + "     ");
			}
			out.println(" ]");
		}
	}

	/**
	 * Exports the given matrix into a CSV file.
	 * @param m The matrix to print to a file.
	 * @param filename
	 */
	public static void exportMatrixCSV(double[][] m, String filename) {
		try {
			PrintWriter out = new PrintWriter(new FileWriter(filename));

			for (int i = 0; i < m.length; i++) {
				out.print("[ ");
				for (int j = 0; j < m[i].length; j++) {
					out.print(m[i][j] + "     ");
				}
				out.println(" ]");
			}
			out.close();
		} catch (IOException e){
			System.err.println("Error: " + e.getMessage());
		}
	}
}
