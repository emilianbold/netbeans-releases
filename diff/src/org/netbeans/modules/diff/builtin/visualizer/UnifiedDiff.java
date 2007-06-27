/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.diff.builtin.visualizer;

import org.netbeans.api.diff.Difference;

import java.io.*;
import java.util.*;

/**
 * Unified diff engine.
 * 
 * @author Maros Sandor
 */
final class UnifiedDiff {
    
    private final TextDiffVisualizer.TextDiffInfo diffInfo;
    private BufferedReader baseReader; 
    private BufferedReader modifiedReader;
    private final String newline;
    private boolean baseEndsWithNewline;
    private boolean modifiedEndsWithNewline;
    
    private int   currentBaseLine;
    private int   currentModifiedLine;

    public UnifiedDiff(TextDiffVisualizer.TextDiffInfo diffInfo) {
        this.diffInfo = diffInfo;
        currentBaseLine = 1;
        currentModifiedLine = 1;
        this.newline = System.getProperty("line.separator");
    }
    
    public String computeDiff() throws IOException {
        baseReader = checkEndingNewline(diffInfo.createFirstReader(), true);
        modifiedReader = checkEndingNewline(diffInfo.createSecondReader(), false);
        
        StringBuilder buffer = new StringBuilder();
        buffer.append("--- ");
        buffer.append(diffInfo.getName1());
        buffer.append(newline);
        buffer.append("+++ ");
        buffer.append(diffInfo.getName2());
        buffer.append(newline);
        
        Difference[] diffs = diffInfo.getDifferences();
        
        for (int currentDifference = 0; currentDifference < diffs.length; ) {
            // the new hunk will span differences from currentDifference to lastDifference (exclusively)
            int lastDifference = getLastIndex(currentDifference);
            Hunk hunk = computeHunk(currentDifference, lastDifference);
            dumpHunk(buffer, hunk);
            currentDifference = lastDifference;
        }
        
        return buffer.toString();
    }
    
    private BufferedReader checkEndingNewline(Reader reader, boolean isBase) throws IOException {
        StringWriter sw = new StringWriter();
        copyStreamsCloseAll(sw, reader);
        String s = sw.toString();
        char endingChar = s.length() == 0 ? 0 : s.charAt(s.length() - 1);
        if (isBase) {
            baseEndsWithNewline = endingChar == '\n' || endingChar == '\r';
        } else {
            modifiedEndsWithNewline = endingChar == '\n' || endingChar == '\r';
        }
        return new BufferedReader(new StringReader(s));
    }

    private Hunk computeHunk(int firstDifference, int lastDifference) throws IOException {

        Hunk hunk = new Hunk();
        
        Difference firstDiff = diffInfo.getDifferences()[firstDifference];
        int contextLines = diffInfo.getContextNumLines();

        int skipLines;
        if (firstDiff.getType() == Difference.ADD) {
            if (contextLines > firstDiff.getFirstStart()) {
                contextLines = firstDiff.getFirstStart();
            }
            skipLines = firstDiff.getFirstStart() - contextLines - currentBaseLine + 1;
        } else {
            if (contextLines >= firstDiff.getFirstStart()) {
                contextLines = firstDiff.getFirstStart() - 1;
            }
            skipLines = firstDiff.getFirstStart() - contextLines - currentBaseLine;
        }
        
        // move file pointers to the beginning of hunk
        while (skipLines-- > 0) {
            readLine(baseReader);
            readLine(modifiedReader);
        }
        
        hunk.baseStart = currentBaseLine;
        hunk.modifiedStart = currentModifiedLine;
        
        // output differences with possible contextual lines in-between
        for (int i = firstDifference; i < lastDifference; i++) {
            Difference diff = diffInfo.getDifferences()[i];
            writeContextLines(hunk, diff.getFirstStart() - currentBaseLine + ((diff.getType() == Difference.ADD) ? 1 : 0));
            
            if (diff.getFirstEnd() > 0) {
                int n = diff.getFirstEnd() - diff.getFirstStart() + 1;
                outputLines(hunk, baseReader, "-", n);
                hunk.baseCount += n;
                if (!baseEndsWithNewline && i == diffInfo.getDifferences().length - 1 && diff.getFirstEnd() == currentBaseLine - 1) {
                    hunk.lines.add("\\ No newline at end of file");
                }
            }
            if (diff.getSecondEnd() > 0) {
                int n = diff.getSecondEnd() - diff.getSecondStart() + 1;
                outputLines(hunk, modifiedReader, "+", n);
                hunk.modifiedCount += n;
                if (!modifiedEndsWithNewline && i == diffInfo.getDifferences().length - 1 && diff.getSecondEnd() == currentModifiedLine - 1) {
                    hunk.lines.add("\\ No newline at end of file");
                }
            }
        }
        
        // output bottom context lines 
        writeContextLines(hunk, diffInfo.getContextNumLines());
        
        if (hunk.modifiedCount == 0) hunk.modifiedStart = 0;    // empty file
        if (hunk.baseCount == 0) hunk.baseStart = 0;    // empty file
        return hunk;
    }
    
    private void writeContextLines(Hunk hunk, int count) throws IOException {
        while (count-- > 0) {
            String line = readLine(baseReader);
            if (line == null) return;
            hunk.lines.add(" " + line);
            readLine(modifiedReader);  // move the modified file pointer as well
            hunk.baseCount++;
            hunk.modifiedCount++;
        }
    }

    private String readLine(BufferedReader reader) throws IOException {
        String s = reader.readLine();
        if (s != null) {
            if (reader == baseReader) currentBaseLine++;
            if (reader == modifiedReader) currentModifiedLine++;
        }
        return s;
    }

    private void outputLines(Hunk hunk, BufferedReader reader, String mode, int n) throws IOException {
        while (n-- > 0) {
            String line = readLine(reader);
            hunk.lines.add(mode + line);
        }
    }

    private int getLastIndex(int firstIndex) {
        int contextLines = diffInfo.getContextNumLines() * 2;
        Difference [] diffs = diffInfo.getDifferences();
        for (++firstIndex; firstIndex < diffs.length; firstIndex++) {
            Difference prevDiff = diffs[firstIndex - 1]; 
            Difference currentDiff = diffs[firstIndex];
            int prevEnd = 1 + ((prevDiff.getType() == Difference.ADD) ? prevDiff.getFirstStart() : prevDiff.getFirstEnd());
            int curStart = (currentDiff.getType() == Difference.ADD) ? (currentDiff.getFirstStart() + 1) : currentDiff.getFirstStart();
            if (curStart - prevEnd > contextLines) {
                break;
            }
        }
        return firstIndex;
    }
    
    private void dumpHunk(StringBuilder buffer, Hunk hunk) {
        buffer.append("@@ -");
        buffer.append(Integer.toString(hunk.baseStart));
        if (hunk.baseCount != 1) {
            buffer.append(",");
            buffer.append(Integer.toString(hunk.baseCount));
        }
        buffer.append(" +");
        buffer.append(Integer.toString(hunk.modifiedStart));
        if (hunk.modifiedCount != 1) {
            buffer.append(",");
            buffer.append(Integer.toString(hunk.modifiedCount));
        }
        buffer.append(" @@");
        buffer.append(newline);
        for (String line : hunk.lines) {
            buffer.append(line);
            buffer.append(newline);
        }
    }

    private static void copyStreamsCloseAll(Writer writer, Reader reader) throws IOException {
        char [] buffer = new char[4096];
        int n;
        while ((n = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, n);
        }
        writer.close();
        reader.close();
    }
    
    private class Hunk {
        int baseStart; 
        int baseCount;
        int modifiedStart; 
        int modifiedCount;
        List<String> lines = new ArrayList<String>();    // each line begins with a space, plus or minus sign 
    }
}
