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

package org.netbeans.modules.merge.builtin.visualizer;

import java.awt.Color;
import java.io.Reader;
import java.io.Writer;
import java.io.IOException;

import org.netbeans.api.diff.Difference;

/**
 * This class controls the merge process.
 *
 * @author  Martin Entlicher
 */
public class MergeControl extends Object {
    
    private Color colorAdded;
    private Color colorMissing;
    private Color colorChanged;
    
    //private MergeDialogComponent component;
    private MergePanel panel;
    private Difference[] diffs;
    /** The shift of differences */
    private int[][] diffShifts;
    
    /** Creates a new instance of MergeControl */
    public MergeControl(/*MergeDialogComponent component, */MergePanel panel) {
        //this.component = component;
        this.panel = panel;
    }
    
    public void initialize(Difference[] diffs, String name1, String title1, Reader r1,
                           String name2, String title2, Reader r2,
                           String name3, String title3, Writer w3, String mimeType,
                           Color colorAdded, Color colorChanged, Color colorMissing) {
        this.diffs = diffs;
        this.diffShifts = new int[diffs.length][2];
        panel.setMimeType1(mimeType);
        panel.setMimeType2(mimeType);
        panel.setMimeType3(mimeType);
        panel.setSource1Title(title1);
        panel.setSource2Title(title2);
        panel.setResultSourceTitle(title3);
        try {
            panel.setSource1(r1);
            panel.setSource2(r2);
            panel.setResultSource(new java.io.StringReader(""));
        } catch (IOException ioex) {
            org.openide.TopManager.getDefault().notifyException(ioex);
        }
        this.colorAdded = colorAdded;
        this.colorChanged = colorChanged;
        this.colorMissing = colorMissing;
        insertEmptyLines(true);
        setDiffHighlight(true);
        copyToResult();
    }
    
    private void insertEmptyLines(boolean updateActionLines) {
        int n = diffs.length;
        for(int i = 0; i < n; i++) {
            Difference action = diffs[i];
            int n1 = action.getFirstStart() + diffShifts[i][0];
            int n2 = action.getFirstEnd() + diffShifts[i][0];
            int n3 = action.getSecondStart() + diffShifts[i][1];
            int n4 = action.getSecondEnd() + diffShifts[i][1];
            if (updateActionLines && i < n - 1) {
                diffShifts[i + 1][0] = diffShifts[i][0];
                diffShifts[i + 1][1] = diffShifts[i][1];
            }
            switch (action.getType()) {
                case Difference.DELETE:
                    panel.addEmptyLines2(n3, n2 - n1 + 1);
                    if (updateActionLines && i < n - 1) {
                        diffShifts[i+1][1] += n2 - n1 + 1;
                    }
                    break;
                case Difference.ADD:
                    panel.addEmptyLines1(n1, n4 - n3 + 1);
                    if (updateActionLines && i < n - 1) {
                        diffShifts[i+1][0] += n4 - n3 + 1;
                    }
                    break;
                case Difference.CHANGE:
                    int r1 = n2 - n1;
                    int r2 = n4 - n3;
                    if (r1 < r2) {
                        panel.addEmptyLines1(n2, r2 - r1);
                        if (updateActionLines && i < n - 1) {
                            diffShifts[i+1][0] += r2 - r1;
                        }
                    } else if (r1 > r2) {
                        panel.addEmptyLines2(n4, r1 - r2);
                        if (updateActionLines && i < n - 1) {
                            diffShifts[i+1][1] += r1 - r2;
                        }
                    }
                    break;
            }
        }
    }
    
    private void setDiffHighlight(boolean set) {
        int n = diffs.length;
        //D.deb("Num Actions = "+n); // NOI18N
        for(int i = 0; i < n; i++) {
            Difference action = diffs[i];
            int n1 = action.getFirstStart() + diffShifts[i][0];
            int n2 = action.getFirstEnd() + diffShifts[i][0];
            int n3 = action.getSecondStart() + diffShifts[i][1];
            int n4 = action.getSecondEnd() + diffShifts[i][1];
            //D.deb("Action: "+action.getAction()+": ("+n1+","+n2+","+n3+","+n4+")"); // NOI18N
            switch (action.getType()) {
            case Difference.DELETE:
                if (set) panel.highlightRegion1(n1, n2, colorMissing);
                else panel.highlightRegion1(n1, n2, java.awt.Color.white);
                break;
            case Difference.ADD:
                if (set) panel.highlightRegion2(n3, n4, colorAdded);
                else panel.highlightRegion2(n3, n4, java.awt.Color.white);
                break;
            case Difference.CHANGE:
                if (set) {
                    panel.highlightRegion1(n1, n2, colorChanged);
                    panel.highlightRegion2(n3, n4, colorChanged);
                } else {
                    panel.highlightRegion1(n1, n2, java.awt.Color.white);
                    panel.highlightRegion2(n3, n4, java.awt.Color.white);
                }
                break;
            }
        }
    }
    
    private void copyToResult() {
        int n = diffs.length;
        int line1 = 1;
        int line3 = 1;
        for(int i = 0; i < n; i++) {
            Difference action = diffs[i];
            int n1 = action.getFirstStart() + diffShifts[i][0];
            int n2 = action.getFirstEnd() + diffShifts[i][0];
            int n3 = action.getSecondStart() + diffShifts[i][1];
            int n4 = action.getSecondEnd() + diffShifts[i][1];
            //System.out.println("diff = "+n1+", "+n2+", "+n3+", "+n4+"; copy("+(line1 - 1)+", "+(n1-1)+", "+line3+")");
            if (n1 >= line1) panel.copySource1ToResult(line1 - 1, n1 - 1, line3);
            line3 += n1 - line1;
            int length = Math.max(n2 - n1, n4 - n3);
            panel.addEmptyLines3(line3, length + 1);
            panel.highlightRegion3(line3, line3 + length, colorMissing);
            line3 += length + 1;
            line1 = n2 + 1;
        }
        //System.out.println("copy("+(line1 - 1)+", -1, "+line3+")");
        panel.copySource1ToResult(line1 - 1, -1, line3);
    }

}
