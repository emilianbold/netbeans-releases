/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.modules.loaders.properties;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.util.Hashtable;
import java.util.Date;
import java.util.Enumeration;

/**
 * Contains conversion utilities which allow reading and storing a properties file 
 * while preserving formatting and comments that user may have entered.
 *
 * @author Petr Jiricka
 */
public class UtilConvert {

    /**
     * Constructor
     */
    public UtilConvert() {
    }

    public  static final String keyValueSeparators = "=: \t\r\n\f";

    public  static final String strictKeyValueSeparators = "=:";
                                                      
    /** Differs from Sun's implementation in that it does not save ' ' as '\ '. */
    private static final String specialSaveChars = "=:\t\r\n\f#!";

    public  static final String whiteSpaceChars = " \t\r\n\f";

    /**
     * Reads a property list (key and element pairs) from the input stream.
     * <p>
     * Every property occupies one line of the input stream. Each line
     * is terminated by a line terminator (<code>\n</code> or <code>\r</code>
     * or <code>\r\n</code>). Lines from the input stream are processed until
     * end of file is reached on the input stream.
     * <p>
     * A line that contains only whitespace or whose first non-whitespace
     * character is an ASCII <code>#</code> or <code>!</code> is ignored
     * (thus, <code>#</code> or <code>!</code> indicate comment lines).
     * <p>
     * Every line other than a blank line or a comment line describes one
     * property to be added to the table (except that if a line ends with \,
     * then the following line, if it exists, is treated as a continuation
     * line, as described
     * below). The key consists of all the characters in the line starting
     * with the first non-whitespace character and up to, but not including,
     * the first ASCII <code>=</code>, <code>:</code>, or whitespace
     * character. All of the key termination characters may be included in
     * the key by preceding them with a \.
     * Any whitespace after the key is skipped; if the first non-whitespace
     * character after the key is <code>=</code> or <code>:</code>, then it
     * is ignored and any whitespace characters after it are also skipped.
     * All remaining characters on the line become part of the associated
     * element string. Within the element string, the ASCII
     * escape sequences <code>\t</code>, <code>\n</code>,
     * <code>\r</code>, <code>\\</code>, <code>\"</code>, <code>\'</code>,
     * <code>\ &#32;</code> &#32;(a backslash and a space), and
     * <code>\\u</code><i>xxxx</i> are recognized and converted to single
     * characters. Moreover, if the last character on the line is
     * <code>\</code>, then the next line is treated as a continuation of the
     * current line; the <code>\</code> and line terminator are simply
     * discarded, and any leading whitespace characters on the continuation
     * line are also discarded and are not part of the element string.
     * <p>
     * As an example, each of the following four lines specifies the key
     * <code>"Truth"</code> and the associated element value
     * <code>"Beauty"</code>:
     * <p>
     * <pre>
     * Truth = Beauty
     *	Truth:Beauty
     * Truth			:Beauty
     * </pre>
     * As another example, the following three lines specify a single
     * property:
     * <p>
     * <pre>
     * fruits				apple, banana, pear, \
     *                                  cantaloupe, watermelon, \
     *                                  kiwi, mango
     * </pre>
     * The key is <code>"fruits"</code> and the associated element is:
     * <p>
     * <pre>"apple, banana, pear, cantaloupe, watermelon,kiwi, mango"</pre>
     * Note that a space appears before each <code>\</code> so that a space
     * will appear after each comma in the final result; the <code>\</code>,
     * line terminator, and leading whitespace on the continuation line are
     * merely discarded and are <i>not</i> replaced by one or more other
     * characters.
     * <p>
     * As a third example, the line:
     * <p>
     * <pre>cheeses
     * </pre>
     * specifies that the key is <code>"cheeses"</code> and the associated
     * element is the empty string.<p>
     *
     * @param      in   the input stream.
     * @exception  IOException  if an error occurred when reading from the
     *               input stream.
     */
    public static Hashtable load(InputStream inStream) throws IOException {
        Hashtable ht = new Hashtable();

        BufferedReader in = new BufferedReader(new InputStreamReader(inStream, "8859_1"));
	while (true) {
            // Get next line
            String line = in.readLine();
            if(line == null)
                return ht;

            if (line.length() > 0) {
                // Continue lines that end in slashes if they are not comments
                char firstChar = line.charAt(0);
                if ((firstChar != '#') && (firstChar != '!')) {
                    while (continueLine(line)) {
                        String nextLine = in.readLine();
                        if(nextLine == null)
                            nextLine = new String("");
                        String loppedLine = line.substring(0, line.length()-1);
                        // Advance beyond whitespace on new line
                        int startIndex=0;
                        for(startIndex=0; startIndex<nextLine.length(); startIndex++)
                            if (whiteSpaceChars.indexOf(nextLine.charAt(startIndex)) == -1)
                                break;
                        nextLine = nextLine.substring(startIndex,nextLine.length());
                        line = new String(loppedLine+nextLine);
                    }
                    // Find start of key
                    int len = line.length();
                    int keyStart;
                    for(keyStart=0; keyStart<len; keyStart++) {
                        if(whiteSpaceChars.indexOf(line.charAt(keyStart)) == -1)
                            break;
                    }
                    // Find separation between key and value
                    int separatorIndex;
                    for(separatorIndex=keyStart; separatorIndex<len; separatorIndex++) {
                        char currentChar = line.charAt(separatorIndex);
                        if (currentChar == '\\')
                            separatorIndex++;
                        else if(keyValueSeparators.indexOf(currentChar) != -1)
                            break;
                    }

                    // Skip over whitespace after key if any
                    int valueIndex;
                    for (valueIndex=separatorIndex+1; valueIndex<len; valueIndex++)
                        if (whiteSpaceChars.indexOf(line.charAt(valueIndex)) == -1)
                            break;

                    // Skip over one non whitespace key value separators if any
                    if (valueIndex < len)
                        if (strictKeyValueSeparators.indexOf(line.charAt(valueIndex)) != -1)
                            valueIndex++;

                    // Skip over white space after other separators if any
                    while (valueIndex < len) {
                        if (whiteSpaceChars.indexOf(line.charAt(valueIndex)) == -1)
                            break;
                        valueIndex++;
                    }
                    String key = line.substring(keyStart, separatorIndex);
                    String value = (separatorIndex < len) ? line.substring(valueIndex, len) : "";

                    // Convert then store key and value
                    key = loadConvert(key);
                    value = loadConvert(value);
                    ht.put(key, value);
                }
            }
	}
    }

    /*
     * Returns true if the given line is a line that must
     * be appended to the next line
     */
    private static boolean continueLine (String line) {
        int slashCount = 0;
        int index = line.length() - 1;
        while((index >= 0) && (line.charAt(index--) == '\\'))
            slashCount++;
        return (slashCount % 2 == 1);
    }

    /*
     * Returns true if the given line is a line that must
     * be appended to the next line
     */
    public static boolean continueLine (StringBuffer line) {
        int slashCount = 0;
        int index = line.length() - 1;
        while((index >= 0) && (line.charAt(index--) == '\\'))
            slashCount++;
        return (slashCount % 2 == 1);
    }

    /*
     * Converts encoded \\uxxxx to unicode chars
     * and changes special saved chars to their original forms
     */
    public static String loadConvert (String theString) {
        char aChar;
        int len = theString.length();
        StringBuffer outBuffer = new StringBuffer(len);

        for(int x=0; x<len; ) {
            aChar = theString.charAt(x++);
            if (aChar == '\\') {
                aChar = theString.charAt(x++);
                if(aChar == 'u') {
                    // Read the xxxx
                    int value=0;
		    for (int i=0; i<4; i++) {
		        aChar = theString.charAt(x++);
		        switch (aChar) {
		          case '0': case '1': case '2': case '3': case '4':
		          case '5': case '6': case '7': case '8': case '9':
		             value = (value << 4) + aChar - '0';
			     break;
			  case 'a': case 'b': case 'c':
                          case 'd': case 'e': case 'f':
			     value = (value << 4) + 10 + aChar - 'a';
			     break;
			  case 'A': case 'B': case 'C':
                          case 'D': case 'E': case 'F':
			     value = (value << 4) + 10 + aChar - 'A';
			     break;
			  default:
                              throw new IllegalArgumentException(
                                           "Malformed \\uxxxx encoding.");
                        }
                    }
                    outBuffer.append((char)value);
                } else {
                    if (aChar == 't') aChar = '\t';
                    else if (aChar == 'r') aChar = '\r';
                    else if (aChar == 'n') aChar = '\n';
                    else if (aChar == 'f') aChar = '\f';
                    outBuffer.append(aChar);
                }
            } else
                outBuffer.append(aChar);
        }
        return outBuffer.toString();
    }

    /*
     * Converts unicodes to encoded \\uxxxx
     * and writes out any of the characters in specialSaveChars
     * with a preceding slash.
     * Differs from Sun's implementation in that it does not save ' ' as '\ '. 
     */
    public static String saveConvert(String theString) {
        char aChar;
        int len = theString.length();
        StringBuffer outBuffer = new StringBuffer(len*2);

        for(int x=0; x<len; ) {
            aChar = theString.charAt(x++);
            switch(aChar) {
                case '\\':outBuffer.append('\\'); outBuffer.append('\\');
                          continue;
                case '\t':outBuffer.append('\\'); outBuffer.append('t');
                          continue;
                case '\n':outBuffer.append('\\'); outBuffer.append('n');
                          continue;
                case '\r':outBuffer.append('\\'); outBuffer.append('r');
                          continue;
                case '\f':outBuffer.append('\\'); outBuffer.append('f');
                          continue;
                default:
                    if ((aChar < 20) || (aChar > 127)) {
                        outBuffer.append('\\');
                        outBuffer.append('u');
                        outBuffer.append(toHex((aChar >> 12) & 0xF));
                        outBuffer.append(toHex((aChar >> 8) & 0xF));
                        outBuffer.append(toHex((aChar >> 4) & 0xF));
                        outBuffer.append(toHex((aChar >> 0) & 0xF));
                    }
                    else {
                        if (specialSaveChars.indexOf(aChar) != -1)
                            outBuffer.append('\\');
                        outBuffer.append(aChar);
                    }
            }
        }
        return outBuffer.toString();
    }


    /**
     * Writes this property list (key and element pairs) in this
     * <code>Properties</code> table to the output stream in a format suitable
     * for loading into a <code>Properties</code> table using the
     * <code>load</code> method.
     * <p>
     * Properties from the defaults table of this <code>Properties</code>
     * table (if any) are <i>not</i> written out by this method.
     * <p>
     * If the header argument is not null, then an ASCII <code>#</code>
     * character, the header string, and a line separator are first written
     * to the output stream. Thus, the <code>header</code> can serve as an
     * identifying comment.
     * <p>
     * Next, a comment line is always written, consisting of an ASCII
     * <code>#</code> character, the current date and time (as if produced
     * by the <code>toString</code> method of <code>Date</code> for the
     * current time), and a line separator as generated by the Writer.
     * <p>
     * Then every entry in this <code>Properties</code> table is written out,
     * one per line. For each entry the key string is written, then an ASCII
     * <code>=</code>, then the associated element string. Each character of
     * the element string is examined to see whether it should be rendered as
     * an escape sequence. The ASCII characters <code>\</code>, tab, newline,
     * and carriage return are written as <code>\\</code>, <code>\t</code>,
     * <code>\n</code>, and <code>\r</code>, respectively. Characters less
     * than <code>\u0020</code> and characters greater than
     * <code>\u007E</code> are written as <code>\\u</code><i>xxxx</i> for
     * the appropriate hexadecimal value <i>xxxx</i>. Space characters, but
     * not embedded or trailing space characters, are written with a preceding
     * <code>\</code>. The key and value characters <code>#</code>,
     * <code>!</code>, <code>=</code>, and <code>:</code> are written with a
     * preceding slash to ensure that they are properly loaded.
     * <p>
     * After the entries have been written, the output stream is flushed.  The
     * output stream remains open after this method returns.
     *
     * @param   out      an output stream.
     * @param   header   a description of the property list.
     * @exception  ClassCastException  if this <code>Properties</code> object
     *             contains any keys or values that are not <code>Strings</code>.
     */
    public static synchronized void store(OutputStream out, String header, Hashtable ht)
    throws IOException
    {
        BufferedWriter awriter;
        awriter = new BufferedWriter(new OutputStreamWriter(out, "8859_1"));
        if (header != null)
            writeln(awriter, "#" + header);
        writeln(awriter, "#" + new Date().toString());
        for (Enumeration e = ht.keys(); e.hasMoreElements();) {
            String key = (String)e.nextElement();
            String val = (String)ht.get(key);
            key = saveConvert(key);
            val = saveConvert(val);
            writeln(awriter, key + "=" + val);
        }
        awriter.flush();
    }

    private static void writeln(BufferedWriter bw, String s) throws IOException {
        bw.write(s);
        bw.newLine();
    }


    /**
     * Convert a nibble to a hex character
     * @param	nibble	the nibble to convert.
     */
    private static char toHex(int nibble) {
	return hexDigit[(nibble & 0xF)];
    }

    /** A table of hex digits */
    private static final char[] hexDigit = {
	'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    };
}
