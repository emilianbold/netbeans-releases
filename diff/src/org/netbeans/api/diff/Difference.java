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

package org.netbeans.api.diff;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * This class represents a single difference between two files.
 *
 * @author  Martin Entlicher
 */
public class Difference extends Object implements Serializable {

    /** Delete type of difference - a portion of a file was removed in the other */
    public static final int DELETE = 0;

    /** Add type of difference - a portion of a file was added in the other */
    public static final int ADD = 1;

    /** Change type of difference - a portion of a file was changed in the other */
    public static final int CHANGE = 2;
    
    private int type = 0;
    private int f1Line1 = 0;
    private int f1Line2 = 0;
    private int f2Line1 = 0;
    private int f2Line2 = 0;
    private List f1LineDiffs;
    private List f2LineDiffs;
    
    /** The text of the difference. For ADD the newly added text, for CHANGE
     * the new text which the old is changed to.
     */
    private String text;
    
    private static final long serialVersionUID = 7638201981188907148L;
    
    /**
     * Creates a new instance of Difference
     * @param type The type of the difference. Must be one of the {@link DELETE},
     *             {@link ADD} or {@link CHANGE}
     * @param f1line1 The line number on which the difference starts in the first file.
     * @param f1line2 The line number on which the difference ends in the first file.
     * @param f2line1 The line number on which the difference starts in the second file.
     * @param f2line2 The line number on which the difference ends in the second file.
     */
    public Difference(int type, int f1Line1, int f1Line2, int f2Line1, int f2Line2) {
        this(type, f1Line1, f1Line2, f2Line1, f2Line2, null, null);
        //System.out.println(this);
    }
    
    /**
     * Creates a new instance of Difference
     * @param type The type of the difference. Must be one of the {@link DELETE},
     *             {@link ADD} or {@link CHANGE}
     * @param f1line1 The line number on which the difference starts in the first file.
     * @param f1line2 The line number on which the difference ends in the first file.
     * @param f2line1 The line number on which the difference starts in the second file.
     * @param f2line2 The line number on which the difference ends in the second file.
     * @param f1LineDiffs The list of differences on lines in the first file.
     *                    The list contains instances of {@link Difference.Line}.
     *                    Can be <code>null</code> when there are no line differences.
     * @param f2LineDiffs The list of differences on lines in the second file.
     *                    The list contains instances of {@link Difference.Line}.
     *                    Can be <code>null</code> when there are no line differences.
     */
    public Difference(int type, int f1Line1, int f1Line2, int f2Line1, int f2Line2,
                      List f1LineDiffs, List f2LineDiffs) {
        if (type > 2 || type < 0) {
            throw new IllegalArgumentException("Bad Difference type = "+type);
        }
        this.type = type;
        this.f1Line1 = f1Line1;
        this.f1Line2 = f1Line2;
        this.f2Line1 = f2Line1;
        this.f2Line2 = f2Line2;
        this.f1LineDiffs = f1LineDiffs;
        this.f2LineDiffs = f2LineDiffs;
    }

    /**
     * Get the difference type. It's one of {@link DELETE}, {@link ADD} or {@link CHANGE}.
     */
    public int getType() {
        return this.type;
    }
    
    /**
     * Get the line number on which the difference starts in the first file.
     */
    public int getFile1Line1() {
        return this.f1Line1;
    }

    /**
     * Get the line number on which the difference ends in the first file.
     */
    public int getFile1Line2() {
        return this.f1Line2;
    }
    
    /**
     * Get the line number on which the difference starts in the second file.
     */
    public int getFile2Line1() {
        return this.f2Line1;
    }
    
    /**
     * Get the line number on which the difference ends in the second file.
     */
    public int getFile2Line2() {
        return this.f2Line2;
    }
    
    /**
     * The list of differences on lines in the first file.
     * The list contains instances of {@link Difference.Line}.
     * Can be <code>null</code> when there are no line differences.
     */
    public List getFile1LineDiffs() {
        return f1LineDiffs;
    }
    
    /**
     * The list of differences on lines in the second file.
     * The list contains instances of {@link Difference.Line}.
     * Can be <code>null</code> when there are no line differences.
     */
    public List getFile2LineDiffs() {
        return f2LineDiffs;
    }
    
    /**
     * Set the text content of the difference.
     */
    public void setText(String text) {
        this.text = text;
    }
    
    /**
     * Get the text content of the difference.
     */
    public String getText() {
        return text;
    }
    
    public void shiftFile1Lines(int numLines) {
        f1Line1 += numLines;
        f1Line2 += numLines;
        if (f1LineDiffs != null) {
            shiftLineDiffs(f1LineDiffs, numLines);
        }
    }
    
    public void shiftFile2Lines(int numLines) {
        f2Line1 += numLines;
        f2Line2 += numLines;
        if (f2LineDiffs != null) {
            shiftLineDiffs(f2LineDiffs, numLines);
        }
    }
    
    private static void shiftLineDiffs(List lineDiffs, int numLines) {
        for (Iterator it = lineDiffs.iterator(); it.hasNext(); ) {
            LineDiff lDiff = (LineDiff) it.next();
            lDiff.shiftLine(numLines);
        }
    }
    
    public String toString() {
        return "Difference("+((type == ADD) ? "ADD" : (type == DELETE) ? "DELETE" : "CHANGE")+", "+
               f1Line1+", "+f1Line2+", "+f2Line1+", "+f2Line2+")";
    }
    
    /**
     * This class represents a difference on a single line.
     */
    public static class LineDiff extends Object implements Serializable {
        
        private int type;
        private int line;
        private int pos1;
        private int pos2;
        private String text;
        
        private static final long serialVersionUID = 7638201981188907149L;
    
        /**
          * Creates a new instance of LineDiff
          * @param type The type of the difference. Must be one of the {@link DELETE},
          *             {@link ADD} or {@link CHANGE}
          * @param line The line number
          * @param pos1 The position on which the difference starts on this line.
          * @param pos2 The position on which the difference ends on this line.
          */
        public LineDiff(int type, int line, int pos1, int pos2) {
            if (type > 2 || type < 0) {
                throw new IllegalArgumentException("Bad Difference type = "+type);
            }
            this.type = type;
            this.line = line;
            this.pos1 = pos1;
            this.pos2 = pos2;
        }
        
        /**
          * Get the difference type. It's one of {@link DELETE}, {@link ADD} or {@link CHANGE}.
          */
        public int getType() {
            return this.type;
        }
    
        /**
          * Get the line number.
          */
        public int getLine() {
            return this.line;
        }
        
        /**
          * Get the position on which the difference starts on this line.
          */
        public int getPosition1() {
            return this.pos1;
        }
        
        /**
          * Get the position on which the difference ends on this line.
          */
        public int getPosition2() {
            return this.pos2;
        }
        
        /**
         * Set the text content of the difference.
         */
        public void setText(String text) {
            this.text = text;
        }
        
        /**
         * Get the text content of the difference.
         */
        public String getText() {
            return text;
        }
        
        public void shiftLine(int numLines) {
            line += numLines;
        }
        
    }
    
}
