/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
import java.util.Collections;
import java.util.List;

/**
 * Slow diff with better user-friendly output format.
 * Compares whole lines. Built on "Weighted matching in graphs".
 *
 *
 * @author  ehucka
 */
public class LineDiff implements Diff {
    
    boolean ignoreCase;
    boolean ignoreEmptyLines;
    
    /** Creates a new instance of LineDiff */
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
    public boolean diff(String first, String second, String diff) throws IOException {
        File fFirst = new File(first);
        File fSecond = new File(second);
        File fDiff = null != diff ? new File(diff) : null;
        return diff(fFirst, fSecond, fDiff);
    }
    
    /**
     * @param firstFile first file to compare -- ref
     * @param secondFile second file to compare -- golden
     * @param diffFile difference file, caller can pass null value, when results are not needed.
     * @return true iff files differ
     */
    public boolean diff(File firstFile, File secondFile, File diffFile) throws IOException {
        LineNumberReader first = new LineNumberReader(new FileReader(firstFile));
        LineNumberReader second = new LineNumberReader(new FileReader(secondFile));
        String passLine;
        String testLine;
        int lastPassIndex=0;
        boolean match=true;
        
        String[] testLines,passLines;
        //read lines
        List<String> tmp = new ArrayList<String>(128);
        while ((passLine = second.readLine()) != null) {
            if (ignoreEmptyLines && passLine.trim().length() == 0) {
                continue;
            }
            tmp.add(passLine);
        }
        passLines = tmp.toArray(new String[tmp.size()]);
        tmp.clear();
        second.close();
        //first matching
        while ((testLine = first.readLine()) != null) {
            if (ignoreEmptyLines && testLine.trim().length() == 0) {
                continue;
            }
            if (match) {
                if (lastPassIndex < passLines.length) {
                    match=compareLines(passLines[lastPassIndex++],testLine);
                    if (!match) {
                        lastPassIndex--;
                        if (diffFile == null) { //no diff file
                            first.close();
                            return true;
                        }
                    }
                } else {
                    match=false;
                    if (diffFile == null) { //no diff file
                        first.close();
                        return true;
                    }
                }
            }
            tmp.add(testLine);
        }
        first.close();
        match &= passLines.length == tmp.size();
        if (match || diffFile == null) {
            return !match;
        }
        testLines = tmp.toArray(new String[tmp.size()]);
        
        //make indicies
        List<List<IndexValue>> passindices = new ArrayList<List<IndexValue>>(passLines.length-lastPassIndex);
        
        for (int i=lastPassIndex;i < passLines.length;i++) {
            passindices.set(i-lastPassIndex, new ArrayList<IndexValue>(testLines.length-lastPassIndex));
            for (int j=lastPassIndex;j < testLines.length;j++) {
                if (compareLines(passLines[i], testLines[j])) { //if equals add to indicies
                    IndexValue iv=new IndexValue(j, i);
                    passindices.get(i-lastPassIndex).add(iv);
                }
            }
            //init indicies values - better is to have low difference between indicies (index-passIndex)
            for (int j=0;j < passindices.get(i-lastPassIndex).size();j++) {
                IndexValue iv = passindices.get(i-lastPassIndex).get(j);
                iv.value=10.0/(1.0+Math.abs(iv.index-iv.passIndex));
            }
        }
        
        List<IndexValue> tmp2 = new ArrayList<IndexValue>();
        //update values (weights) - add value to indicies which has same deltas
        for (int i=0; i < passindices.size(); i++) {
            for (int j=0; j < passindices.get(i).size(); j++) {
                IndexValue iv = passindices.get(i).get(j);
                if (tmp2.contains(iv)) continue;
                int delta=iv.index-iv.passIndex;
                List<IndexValue> path = new ArrayList<IndexValue>(128);
                path.add(iv);
                int ind=i+1;
                while (ind < passindices.size()) {
                    if (passindices.get(ind).isEmpty()) {
                        break;
                    }
                    boolean found=false;
                    for (int k=0; k < passindices.get(ind).size(); k++) {
                        IndexValue iv2 = passindices.get(ind).get(k);
                        int delta2=iv2.index-iv2.passIndex;
                        if (delta2 == delta) {
                            path.add(iv2);
                            found=true;
                            break;
                        }
                    }
                    if (!found) {
                        break;
                    }
                    ind++;
                }
                if (path.size() == 1) continue;
                //path <=> sequence with same value of (index-passIndex)
                //long sequences are probably matching - strong weight
                for (int k=0;k < path.size();k++) {
                    IndexValue iv2=(IndexValue)(path.get(k));
                    iv2.value*=path.size()*10.0;
                    tmp2.add(iv2);
                }
            }
        }
        //preprocessing - sorting
        for (int i=0; i < passindices.size(); i++) {
            if (passindices.get(i).size() > 1) {
                Collections.sort(passindices.get(i));
            }
        }
        //cut matchings with weak weight and which are crossing the heavy weight
        //forward
        for (int i=0; i < passindices.size() - 1; i++) {
            if (passindices.get(i).isEmpty()) continue;
            boolean once=false;
            do {
                once=false;
                IndexValue better = passindices.get(i).get(0);
                IndexValue nextbetter=null;
                if (!passindices.get(i+1).isEmpty())
                    nextbetter = passindices.get(i+1).get(0);
                if (nextbetter != null && nextbetter.value > better.value) {
                    if ((nextbetter.index-nextbetter.passIndex) < (better.index-better.passIndex)) {
                        passindices.get(i).remove(better);
                        once=true;
                    }
                }
            } while (once && !passindices.get(i).isEmpty());
        }
        //backward
        for (int i = passindices.size() - 1; i >= 1; i--) {
            if (passindices.get(i).isEmpty()) continue;
            boolean once=false;
            do {
                once=false;
                IndexValue better = passindices.get(i).get(0);
                IndexValue prebetter=null;
                if (!passindices.get(i-1).isEmpty())
                    prebetter = passindices.get(i-1).get(0);
                if (prebetter != null && prebetter.value > better.value) {
                    if ((prebetter.index-prebetter.passIndex) > (better.index-better.passIndex)) {
                        passindices.get(i).remove(better);
                        once=true;
                    }
                }
            } while (once && !passindices.get(i).isEmpty());
        }
        //generate result list
        List<Result> result=new ArrayList<Result>(128);
        
        for (int i=lastPassIndex;i < passLines.length;i++) {
            if (!passindices.get(i-lastPassIndex).isEmpty()) {
                IndexValue best=null;
                int ind=0;
                while (best == null && ind < passindices.get(i-lastPassIndex).size()) {
                    IndexValue iv = passindices.get(i-lastPassIndex).get(ind++);
                    if (testLines[iv.index] != null) {
                        best=iv;
                        testLines[iv.index]=null;
                        break;
                    }
                }
            } else {
                result.add(new Result(passLines[i], i, true));
            }
        }
        
        for (int i=lastPassIndex;i < testLines.length;i++) {
            if (testLines[i] != null) {
                result.add(new Result(testLines[i], i, false));
            }
        }
        if (result.size() > 0) {
            Collections.sort(result);
            PrintStream ps=new PrintStream(new FileOutputStream(diffFile));
            for (int i=0;i < result.size();i++) {
                ps.println(result.get(i));
            }
            ps.close();
            //return true;
        }
        
        return !match;
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
        String tmp=String.valueOf(lineIndex+1);
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
            File dir=new File("/tmp/diff");
            for (int i=1;i < 10;i++) {
                File subdir=new File(dir, String.valueOf(i));
                File golden=new File(subdir, "old.txt");
                File test=new File(subdir, "new.txt");
                if (golden.exists() && test.exists()) {
                    System.out.println("make diff of "+i);
                    diff.diff(test, golden, new File(subdir, "test.diff"));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    class Result implements Comparable<Result> {
        int index;
        String line;
        boolean missing=false;
        public Result(String line, int index, boolean missing) {
            this.index=index;
            this.line=line;
            this.missing=missing;
        }
        
        public int compareTo(Result o) {
            return index - o.index;
        }
        
        public String toString() {
            return formatOutput(!missing, index, line);
        }
    }
    
    class IndexValue implements Comparable<IndexValue> {
        public int index;
        public int passIndex;
        public double value;
        
        public IndexValue(int index, int srcIndex) {
            this.index=index;
            this.passIndex=srcIndex;
            value=1.0;
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
        
        public int compareTo(IndexValue o) {
            return (int) (o.value - value);
        }
        
        public String toString() {
            return "["+String.valueOf(index+1)+", "+String.valueOf(passIndex+1)+"] = "+String.valueOf(value);
        }
    }
}