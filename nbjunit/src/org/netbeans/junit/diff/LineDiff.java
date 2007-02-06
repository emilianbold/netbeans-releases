/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.junit.diff;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Line Diff with formated textual output.
 *
 *
 * @author  ehucka
 */
public class LineDiff implements Diff {
    
    public static final int CONTEXT = 3;
    
    boolean ignoreCase;
    boolean ignoreEmptyLines;
    
    public LineDiff() {
        this(false, false);
    }
    
    public LineDiff(boolean ignoreCase) {
        this(ignoreCase, false);
    }
    
    public LineDiff(boolean ignoreCase, boolean ignoreEmptyLines) {
        this.ignoreCase = ignoreCase;
        this.ignoreEmptyLines = ignoreEmptyLines;
    }
    
    public boolean getIgnoreCase() {
        return ignoreCase;
    }
    
    /**
     * @param l1 first line to compare
     * @param l2 second line to compare
     * @return true if lines equal
     */
    protected boolean compareLines(String l1,String l2) {
        if (getIgnoreCase()) {
            if (l1.equalsIgnoreCase(l2))
                return true;
        } else {
            if (l1.equals(l2))
                return true;
        }
        return false;
    }
    
    /**
     * @param first first file to compare
     * @param second second file to compare
     * @param diff difference file, caller can pass null value, when results are not needed.
     * @return true iff files differ
     */
    public boolean diff(String ref, String pass, String diff) throws IOException {
        File fFirst = new File(ref);
        File fSecond = new File(pass);
        File fDiff = null != diff ? new File(diff) : null;
        return diff(fFirst, fSecond, fDiff);
    }
    
    /**
     * @param refFile first file to compare -- ref
     * @param passFile second file to compare -- golden
     * @param diffFile difference file, caller can pass null value, when results are not needed.
     * @return true iff files differ
     */
    public boolean diff(File refFile,File passFile,File diffFile) throws IOException {
        LineNumberReader first = new LineNumberReader(new FileReader(refFile));
        LineNumberReader second = new LineNumberReader(new FileReader(passFile));
        String line;
        
        String[] refLines, passLines;
        
        //read golden file
        List<String> tmp = new ArrayList<String>(64);
        while ((line = second.readLine()) != null) {
            if (ignoreEmptyLines && line.trim().length() == 0) {
                continue;
            }
            tmp.add(line);
        }
        passLines = tmp.toArray(new String[tmp.size()]);
        tmp.clear();
        second.close();
        //read ref file
        tmp = new ArrayList<String>(64);
        while ((line = first.readLine()) != null) {
            if (ignoreEmptyLines && line.trim().length() == 0) {
                continue;
            }
            tmp.add(line);
        }
        refLines = tmp.toArray(new String[tmp.size()]);
        tmp.clear();
        first.close();
        //collect differences
        List<Result> results = findDifferences(passLines, refLines);
        //without differences it can be finished here
        if (results.size() == 0) return false;
        if (diffFile == null) return results.size() > 0;
        //merge
        merge(results);
        //print
        printResults(passLines, refLines, results, diffFile);
        return results.size() > 0;
    }
    
    /**
     * compare right to left lines (pass to ref) and left to right lines
     *
     */
    private List<Result> findDifferences(String[] passLines, String[] refLines) {
        int stepLeft = 0;
        int stepRight = 0;
        //test is left, pass is right
        List<Result> results = new ArrayList<Result>(64);
        //start right
        boolean right = true;
        
        while (stepRight < passLines.length || stepLeft < refLines.length) {
            if (right) {
                if (stepRight >= passLines.length) {
                    if (stepLeft < refLines.length) {
                        results.add(new Result(stepLeft, refLines.length, stepRight, true));  //add new lines
                    }
                    break;
                }
                String v = passLines[stepRight];
                int found = find(v, refLines, stepLeft);
                if (found >= 0) {
                    if (found > stepLeft) {
                        if (found-stepLeft >= 2) { //could be wrong jump - try tp skip left
                            right=false;
                            continue;
                        } else {
                            results.add(new Result(stepLeft, found, stepRight, true));  //add new lines
                        }
                    }
                    stepLeft=found+1;
                } else {
                    results.add(new Result(stepRight, stepRight+1, false));  //add one missing
                    //switch to left
                    right=false;
                }
                stepRight++;
            } else {
                if (stepLeft >= refLines.length) {
                    if (stepRight < passLines.length) {
                        results.add(new Result(stepRight, passLines.length-1, false));  //add missing lines
                    }
                    break;
                }
                String v = refLines[stepLeft];
                int found = find(v, passLines, stepRight);
                if (found >= 0) {
                    if (found > stepRight) {
                        results.add(new Result(stepRight, found, false));  //add missing lines
                    }
                    stepRight=found+1;
                } else {
                    results.add(new Result(stepLeft, stepLeft+1, stepRight, true));  //add one new
                    //switch to right
                    right=true;
                }
                stepLeft++;
            }
        }
        return results;
    }
    
    private void printResults(String[] passLines, String[] refLines, List<Result> results, File diffFile) throws IOException {
        int numLength = (refLines.length > passLines.length) ? String.valueOf(refLines.length).length() :
            String.valueOf(passLines.length).length();
        PrintStream ps = new PrintStream(new FileOutputStream(diffFile));
        boolean precontext=false;
        for (int i = 0; i < results.size(); i++) {
            Result rs = results.get(i);
            if (!precontext) {
                int si = rs.passIndex-CONTEXT;
                if (si < 0) si = 0;
                for (int j=si;j < rs.passIndex;j++) {
                    printContext(passLines, ps, j, numLength);
                }
            } else {
                precontext=false;
            }
            results.get(i).print(passLines, refLines, ps, numLength);
            int e1 = (rs.newLine) ? rs.passIndex : rs.end;
            int e2 = e1+CONTEXT;
            if (i < results.size()-1 && results.get(i+1).passIndex < e2) {
                e2 = results.get(i+1).passIndex;
                precontext=true;
            } else if (e2 > passLines.length) {
                e2=passLines.length;
            }
            for (int j=e1;j < e2;j++) {
                printContext(passLines, ps, j, numLength);
            }
        }
        ps.close();
    }
    
    private int find(String value, String[] lines, int startIndex) {
        for (int i  = startIndex;i < lines.length;i++) {
            if (compareLines(value, lines[i])) {
                return i;
            }
        }
        return -1;
    }
    
    private void merge(List<Result> results) {
        for (int i  = 0;i < results.size()-1;i++) {
            if (results.get(i).newLine && results.get(i+1).newLine &&
                    results.get(i).end == results.get(i+1).start) {
                results.get(i).end = results.get(i+1).end;
                results.remove(i+1);
                i--;
            }
        }
    }
    
    private void printContext(String[] passLines, PrintStream ps, int lineNumber, int numLength) {
        String num=String.valueOf(lineNumber+1);
        int rem=numLength+1-num.length();
        for (int j=0;j < rem;j++)
            ps.print(' ');
        ps.print(num);
        ps.print("   ");
        ps.println(passLines[lineNumber]);
    }
    
    static class Result {
        
        boolean newLine = false;
        int start, end;
        int passIndex;
        
        public Result(int start, int end, int passIndex, boolean newLine) {
            this.start = start;
            this.end = end;
            this.passIndex = passIndex;
            this.newLine = newLine;
        }
        
        public Result(int start, int end, boolean newLine) {
            this.passIndex = start;
            this.start = start;
            this.end = end;
            this.newLine = newLine;
        }
        
        public void print(String[] passLines, String[] refLines, PrintStream ps, int numLength) {
            for  (int i=start;i < end;i++) {
                if (newLine) {
                    for (int j=0;j < numLength+2;j++)
                        ps.print(' ');
                    ps.print("+ ");
                    ps.println(refLines[i]);
                } else {
                    String num=String.valueOf(i+1);
                    int rem=numLength+1-num.length();
                    for (int j=0;j < rem;j++)
                        ps.print(' ');
                    ps.print(num);
                    ps.print(" - ");
                    ps.println(passLines[i]);
                }
            }
        }
    }
}