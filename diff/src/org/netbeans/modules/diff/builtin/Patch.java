/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.diff.builtin;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.netbeans.api.diff.Difference;

/**
 * Utility class for patch application.
 *
 * @author  Martin Entlicher
 */
public class Patch extends Reader {
    
    private Difference[] diffs;
    private BufferedReader source;
    private int currDiff = 0;
    private int line = 1;
    private StringBuffer buff = new StringBuffer();
    
    /** Creates a new instance of Patch */
    private Patch(Difference[] diffs, Reader source) {
        this.diffs = diffs;
        this.source = new BufferedReader(source);
    }
    
    /**
     * Apply the patch to the source.
     * @param diffs The differences to patch
     * @param source The source stream
     * @return The patched stream
     * @throws IOException When reading from the source stread fails
     * @throws ParseException When the source does not match the patch to be applied
     */
    public static Reader apply(Difference[] diffs, Reader source) {//throws IOException, ParseException {
        return new Patch(diffs, source);
    }
    
    /**
     * Parse the differences.
     */
    public static Difference[] parse(Reader source) throws IOException {
        return parseContextDiff(source);
    }
    
    public int read(char[] cbuf, int off, int length) throws java.io.IOException {
        if (buff.length() < length) {
            doRetrieve(length - buff.length());
        }
        int ret = Math.min(buff.length(), length);
        if (ret == 0) return -1;
        String retStr = buff.substring(0, ret);
        char[] retChars = retStr.toCharArray();
        System.arraycopy(retChars, 0, cbuf, off, ret);
        buff.delete(0, ret);
        return ret;
    }
    
    public void close() throws java.io.IOException {
        source.close();
    }
    
    private void doRetrieve(int length) throws IOException {
        for (int size = 0; size < length; line++) {
            if (currDiff < diffs.length && line == diffs[currDiff].getFirstStart()) {
                if (compareText(source, diffs[currDiff].getFirstText())) {
                    buff.append(diffs[currDiff].getSecondText());
                    currDiff++;
                } else {
                    throw new IOException("Patch not applicable.");
                }
            }
            String lineStr = source.readLine();
            if (lineStr == null) break;
            buff.append(lineStr);
            buff.append('\n');
        }
    }
    
    private boolean compareText(BufferedReader source, String text) throws IOException {
        if (text.length() == 0) return true;
        char[] chars = new char[text.length()];
        source.read(chars);
        line += numChars('\n', chars);
        String readStr = new String(chars);
        //System.out.println("Comparing text of the diff:\n'"+text+"'\nWith the read text:\n'"+readStr+"'\n");
        //System.out.println("  EQUALS = "+readStr.equals(text));
        return readStr.equals(text);
    }
    
    private static int numChars(char c, char[] chars) {
        int n = 0;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == c) n++;
        }
        return n;
    }
    
    private static final String CONTEXT_MARK1B = "*** ";
    private static final String CONTEXT_MARK1E = " ****";
    private static final String CONTEXT_MARK2B = "--- ";
    private static final String CONTEXT_MARK2E = " ----";
    private static final String CONTEXT_MARK_DELIMETER = ",";
    private static final String DIFFERENCE_DELIMETER = "***************";
    private static final String LINE_PREP = "  ";
    private static final String LINE_PREP_ADD = "+ ";
    private static final String LINE_PREP_REMOVE = "- ";
    private static final String LINE_PREP_CHANGE = "! ";
    
    private static Difference[] parseContextDiff(Reader in) throws IOException {
        BufferedReader br = new BufferedReader(in);
        ArrayList diffs = new ArrayList();
        String line;
        do {
            do {
                line = br.readLine();
            } while (line != null && !DIFFERENCE_DELIMETER.equals(line));
            int[] firstInterval = new int[2];
            line = br.readLine();
            if (line != null && line.startsWith(CONTEXT_MARK1B)) {
                try {
                    readNums(line, CONTEXT_MARK1B.length(), firstInterval);
                } catch (NumberFormatException nfex) {
                    throw new IOException(nfex.getLocalizedMessage());
                }
            } else continue;
            ArrayList firstChanges = new ArrayList(); // List of intervals and texts
            line = fillChanges(firstInterval, br, CONTEXT_MARK2B, firstChanges);
            int[] secondInterval = new int[2];
            if (line != null && line.startsWith(CONTEXT_MARK2B)) {
                try {
                    readNums(line, CONTEXT_MARK2B.length(), secondInterval);
                } catch (NumberFormatException nfex) {
                    throw new IOException(nfex.getLocalizedMessage());
                }
            } else continue;
            ArrayList secondChanges = new ArrayList(); // List of intervals and texts
            line = fillChanges(secondInterval, br, DIFFERENCE_DELIMETER, secondChanges);
            mergeChanges(firstInterval, secondInterval, firstChanges, secondChanges, diffs);
        } while (line != null);
        return (Difference[]) diffs.toArray(new Difference[diffs.size()]);
    }
    
    private static void readNums(String str, int off, int[] values) throws NumberFormatException {
        int end = str.indexOf(CONTEXT_MARK_DELIMETER, off);
        if (end > 0) {
            values[0] = Integer.parseInt(str.substring(off, end).trim());
        } else throw new NumberFormatException("Missing comma.");
        off = end + 1;
        end = str.indexOf(' ', off);
        if (end > 0) {
            values[1] = Integer.parseInt(str.substring(off, end).trim());
        } else throw new NumberFormatException("Missing final space.");
    }

    private static String fillChanges(int[] interval, BufferedReader br,
                                      String untilStartsWith, List changes) throws IOException {
        String line = br.readLine();
        for (int pos = interval[0]; pos <= interval[1]; pos++) {
            if (line == null || line.startsWith(untilStartsWith)) break;
            if (line.startsWith(LINE_PREP_ADD)) {
                int[] changeInterval = new int[3];
                changeInterval[0] = pos;
                changeInterval[2] = Difference.ADD;
                StringBuffer changeText = new StringBuffer();
                changeText.append(line.substring(LINE_PREP_ADD.length()));
                changeText.append('\n');
                do {
                    line = br.readLine();
                    if (line.startsWith(LINE_PREP_ADD)) {
                        changeText.append(line.substring(LINE_PREP_ADD.length()));
                        changeText.append('\n');
                    } else {
                        break;
                    }
                    pos++;
                } while (true);
                changeInterval[1] = pos;
                changes.add(changeInterval);
                changes.add(changeText.toString());
            } else if (line.startsWith(LINE_PREP_REMOVE)) {
                int[] changeInterval = new int[3];
                changeInterval[0] = pos;
                changeInterval[2] = Difference.DELETE;
                StringBuffer changeText = new StringBuffer();
                changeText.append(line.substring(LINE_PREP_REMOVE.length()));
                changeText.append('\n');
                do {
                    line = br.readLine();
                    if (line.startsWith(LINE_PREP_REMOVE)) {
                        changeText.append(line.substring(LINE_PREP_REMOVE.length()));
                        changeText.append('\n');
                    } else {
                        break;
                    }
                    pos++;
                } while (true);
                changeInterval[1] = pos;
                changes.add(changeInterval);
                changes.add(changeText.toString());
            } else if (line.startsWith(LINE_PREP_CHANGE)) {
                int[] changeInterval = new int[3];
                changeInterval[0] = pos;
                changeInterval[2] = Difference.CHANGE;
                StringBuffer changeText = new StringBuffer();
                changeText.append(line.substring(LINE_PREP_CHANGE.length()));
                changeText.append('\n');
                do {
                    line = br.readLine();
                    if (line.startsWith(LINE_PREP_CHANGE)) {
                        changeText.append(line.substring(LINE_PREP_CHANGE.length()));
                        changeText.append('\n');
                    } else {
                        break;
                    }
                    pos++;
                } while (true);
                changeInterval[1] = pos;
                changes.add(changeInterval);
                changes.add(changeText.toString());
            } else {
                line = br.readLine();
            }
        }
        return line;
    }
    
    private static void mergeChanges(int[] firstInterval, int[] secondInterval,
                              List firstChanges, List secondChanges, List diffs) {
        int p1, p2;
        int n1 = firstChanges.size();
        int n2 = secondChanges.size();
        //System.out.println("mergeChanges(("+firstInterval[0]+", "+firstInterval[1]+"), ("+secondInterval[0]+", "+secondInterval[1]+"))");
        //System.out.println("firstChanges.size() = "+n1);
        //System.out.println("secondChanges.size() = "+n2);
        for (p1 = p2 = 0; p1 < n1 || p2 < n2; ) {
            boolean isAddRemove = true;
            while (isAddRemove && p1 < n1) {
                int[] interval = (int[]) firstChanges.get(p1);
                isAddRemove = interval[2] == Difference.ADD || interval[2] == Difference.DELETE;
                if (isAddRemove) {
                    diffs.add(new Difference(interval[2], interval[0], interval[1],
                                             secondInterval[0] + interval[0] - firstInterval[0],
                                             secondInterval[0] + interval[1] - firstInterval[0],
                                             (String) firstChanges.get(p1 + 1), ""));
                    p1 += 2;
                }
            }
            isAddRemove = true;
            while (isAddRemove && p2 < n2) {
                int[] interval = (int[]) secondChanges.get(p2);
                isAddRemove = interval[2] == Difference.ADD || interval[2] == Difference.DELETE;
                if (isAddRemove) {
                    diffs.add(new Difference(interval[2],
                                             firstInterval[0] + interval[0] - secondInterval[0],
                                             firstInterval[0] + interval[1] - secondInterval[0],
                                             interval[0], interval[1],
                                             "", (String) secondChanges.get(p2 + 1)));
                    p2 += 2;
                }
            }
            // Change is remaining
            if (p1 < n1 && p2 < n2) {
                int[] interval1 = (int[]) firstChanges.get(p1);
                int[] interval2 = (int[]) secondChanges.get(p2);
                diffs.add(new Difference(interval1[2], interval1[0], interval1[1],
                                         interval2[0], interval2[1],
                                         (String) firstChanges.get(p1 + 1),
                                         (String) secondChanges.get(p2 + 1)));
                p1 += 2;
                p2 += 2;
            }
        }
    }
    
}
