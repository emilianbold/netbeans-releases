/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * LineDiff.java
 *
 * Created on March 28, 2002, 9:49 AM
 */

package org.netbeans.junit.diff;

import org.netbeans.junit.diff.Diff;
import java.io.File;
import java.io.FileOutputStream;
import java.io.LineNumberReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 *
 * @author  jlahoda, ehucka
 */
public class LineDiff implements Diff {
    
    private boolean ignoreCase;
    
    /** Creates a new instance of LineDiff */
    public LineDiff(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }
    
    public LineDiff() {
        this(false);
    }
    
    public boolean getIgnoreCase() {
        return ignoreCase;
    }
    
    /**
     * @param l1 first line to compare
     * @param l2 second line to compare
     * @return true if lines equal
     */
    private boolean compareLines(String l1,String l2) {
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
    public boolean diff(String first, String second, String diff) throws java.io.IOException {
        File fFirst = new File(first);
        File fSecond = new File(second);
        File fDiff = null != diff ? new File(diff) : null;
        return diff(fFirst, fSecond, fDiff);
    }
    
    /**
     * @param first first file to compare -- ref
     * @param second second file to compare -- golden
     * @param diff difference file, caller can pass null value, when results are not needed.
     * @return true iff files differ
     */
    public boolean diff(java.io.File firstFile, java.io.File secondFile, java.io.File diffFile) throws java.io.IOException {
        LineNumberReader first = new LineNumberReader(new FileReader(firstFile));
        LineNumberReader second = new LineNumberReader(new FileReader(secondFile));
        String passLine;
        String testLine;
        
        if (diffFile == null) {
            while ((passLine = first.readLine()) != null) {
                testLine = second.readLine();
                if (testLine == null) {
                    first.close();
                    second.close();
                    return true;
                }
                if (!compareLines(passLine,testLine)) {
                    first.close();
                    second.close();
                    return true;
                }
            }
        } else {
            ArrayList testLines,passLines;
            ArrayList retls=new ArrayList();
            
            testLines=new ArrayList();
            while ((passLine = first.readLine()) != null) {
                testLines.add(passLine);
            }
            passLines=new ArrayList();
            while ((testLine = second.readLine()) != null) {
                passLines.add(testLine);
            }
            first.close();
            second.close();
            
            int j=0,bj;
            boolean found;
            
            for (int i=0;i < passLines.size();i++) { //go through golden file
                //remains
                if (j >= testLines.size()) {
                    for (int k=i;k < passLines.size();k++) {
                        retls.add(formatOutput(false, testLines.size(), k+1, (String)passLines.get(k)));
                    }
                    break;
                }
                
                passLine=(String)(passLines.get(i));
                testLine=(String)(testLines.get(j));
                if (!compareLines(passLine,testLine)) {
                    found=false;
                    //read all lines to end of test file - finding pass line
                    for (int k=j;k < testLines.size();k++) {
                        testLine = (String)(testLines.get(k));
                        if (compareLines(passLine,testLine)) { //found last passLine - all between last pass line and this line are new lines
                            for (int l=j;l < k;l++) {
                                retls.add(formatOutput(true, l+1, i+1, (String)testLines.get(l)));
                            }
                            j=k;
                            found=true;
                            break;
                        }
                    }
                    if (!found) { //last pass line is not found at all - it is missing
                        retls.add(formatOutput(false, j+1, i+1, passLine));
                        j--;
                    }
                }
                j++;
            }
            //remains lines in test file
            if (j < testLines.size()) {
                for (int i=j;i < testLines.size();i++) {
                    retls.add(formatOutput(true, i+1, passLines.size(), (String)testLines.get(i)));
                }
            }
            
            if (retls.size() > 0) {
                PrintStream pw=null;
                pw=new PrintStream(new FileOutputStream(diffFile));
                for (int i=0;i < retls.size();i++) {
                    pw.println(retls.get(i));
                }
                pw.close();
                return true;
            }
        }
        return false;
    }
    
    //+ 1234 - 1234
    public String formatOutput(boolean positive, int testLine, int passLine, String line) {
        char[] ret=new char[15];
        
        int index=0;
        if (positive) {
            ret[index++]='+';
        } else {
            ret[index++]='-';
        }
        ret[index++]=' ';
        String tmp=String.valueOf(testLine);
        for (int i=0;i < tmp.length();i++) {
            ret[index++]=tmp.charAt(i);
        }
        ret[index++]=' ';ret[index++]='-';ret[index++]=' ';
        tmp=String.valueOf(passLine);
        for (int i=0;i < tmp.length();i++) {
            ret[index++]=tmp.charAt(i);
        }
        for (int i=index;i < ret.length;i++) {
            ret[i]=' ';
        }
        return new String(ret)+line;
    }
    
    public static void main(String[] argv) {
        try {
            LineDiff diff=new LineDiff(true);
            diff.diff("/tmp/diff/test.ref", "/tmp/diff/test.pass","/tmp/diff/test.diff");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
