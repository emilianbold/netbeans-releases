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

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;



/**
 * Slow diff with better user-friendly diff format.
 * Compares whole lines.
 *
 * @author  ehucka
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
    public boolean diff(String first, String second, String diff) throws IOException {
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
    public boolean diff(File firstFile, File secondFile, File diffFile) throws IOException {
        LineNumberReader first = new LineNumberReader(new FileReader(firstFile));
        LineNumberReader second = new LineNumberReader(new FileReader(secondFile));
        String passLine;
        String testLine;
        
        if (diffFile == null) {
            while ((passLine = second.readLine()) != null) {
                testLine = first.readLine();
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
            String[] testLines,passLines;
            //read lines
            ArrayList tmp=new ArrayList();
            while ((testLine = first.readLine()) != null) {
                tmp.add(testLine);
            }
            testLines=(String[])(tmp.toArray(new String[tmp.size()]));
            tmp.clear();
            while ((passLine = second.readLine()) != null) {
                tmp.add(passLine);
            }
            passLines=(String[])(tmp.toArray(new String[tmp.size()]));
            tmp.clear();
            first.close();
            second.close();
            
            //make indicies
            ArrayList[] indiciestest=new ArrayList[testLines.length];
            
            for (int i=0;i < passLines.length;i++) {
                for (int j=0;j < testLines.length;j++) {
                    if (compareLines(passLines[i], testLines[j])) { //if equals add to indicies
                        IndexValue iv=new IndexValue(testLines[j], j, i);
                        if (indiciestest[j] == null) {
                            indiciestest[j]=new ArrayList();
                        }
                        indiciestest[j].add(iv);
                    }
                }
            }
            
            //find path
            IndexValue[] path=new IndexValue[testLines.length];
            int lastValue=0;
            for (int i=0;i < indiciestest.length;i++) {
                ArrayList list=indiciestest[i];
                if (list == null) {
                    continue;
                }
                for (int j=0;j < list.size();j++) {
                    IndexValue ind=(IndexValue)(list.get(j));
                    if (path[i] == null || ind.isBetterThan(path[i], lastValue)) {
                        boolean history=false;
                        int lastIndex=-1;
                        for (int k=0;k < i;k++) {
                            if (path[k] != null) {
                                lastIndex=path[k].passIndex;
                                if (path[k].passIndex == ind.passIndex) {
                                    history=true;
                                    break;
                                }
                            }
                        }
                        if (!history && lastIndex < ind.passIndex) {
                            path[i]=ind;
                            lastValue=path[i].value;
                        }
                    }
                }
            }
            
            //generate result list
            ArrayList result=new ArrayList();
            
            for (int i=0;i < path.length;i++) {
                if (path[i] != null) {
                    passLines[path[i].passIndex]=null;
                    testLines[path[i].index]=null;
                }
            }
            for (int i=0;i < testLines.length;i++) {
                if (testLines[i] != null) {
                    result.add(new Result(testLines[i], i, false));
                }
            }
            for (int i=0;i < passLines.length;i++) {
                if (passLines[i] != null) {
                    result.add(new Result(passLines[i], i, true));
                }
            }
            if (result.size() > 0) {
                Collections.sort(result);
                PrintStream ps=new PrintStream(new FileOutputStream(diffFile));
                for (int i=0;i < result.size();i++) {
                    ps.println(result.get(i));
                }
                ps.close();
                return true;
            }
        }
        return false;
    }
    
    public String formatOutput(boolean positive, int lineIndex, String line) {
        char[] ret=new char[14];
        
        int index=0;
        if (!positive) {
            ret[index++]='-';
            ret[index++]='p';
            ret[index++]='a';
            ret[index++]='s';
            ret[index++]='s';
        } else {
            ret[index++]='+';
            ret[index++]='t';
            ret[index++]='e';
            ret[index++]='s';
            ret[index++]='t';
        }
        ret[index++]='[';
        String tmp=String.valueOf(lineIndex);
        for (int i=0;i < tmp.length();i++) {
            ret[index++]=tmp.charAt(i);
        }
        ret[index++]=']';
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
    
    class Result implements Comparable {
        int index;
        String line;
        boolean missing=false;
        public Result(String line, int index, boolean missing) {
            this.index=index;
            this.line=line;
            this.missing=missing;
        }
        
        public int compareTo(Object o) {
            return (index - ((Result)o).index);
        }
        
        public String toString() {
            return formatOutput(!missing, index, line);
        }
    }
    
    class IndexValue implements Comparable {
        public int index;
        public int passIndex;
        public int value;
        public String line;
        
        public IndexValue(String line, int index, int srcIndex) {
            this.index=index;
            this.line=line;
            this.passIndex=srcIndex;
            value=index-srcIndex;
        }
        
        public boolean isBetterThan(IndexValue v, int lastValue) {
            if (value >= lastValue && v.value >= lastValue) {
                return (value < v.value);
            } else if (value < lastValue && v.value < lastValue) {
                return (value < v.value);
            } else if (value < lastValue) {
                return false;
            } else if (v.value < lastValue) {
                return true;
            }
            return (value < v.value);
        }
        
        public int compareTo(Object o) {
            return (value - ((IndexValue)o).value);
        }
        
        public String toString() {
            return "["+String.valueOf(index)+", "+String.valueOf(passIndex)+"] = "+String.valueOf(value);
        }
    }
}
