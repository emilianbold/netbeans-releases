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
package org.netbeans.modules.diff.builtin.visualizer.editable;

import org.netbeans.api.diff.Difference;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.diff.DiffProvider;
import org.openide.util.Lookup;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.*;
import java.io.StringReader;
import java.io.IOException;

/**
 * Handles interaction among Diff components: editor panes, scroll bars, action bars and the split pane.
 * 
 * @author Maros Sandor
 */
class DiffViewManager implements ChangeListener {
    
    private final EditableDiffView master;
    
    private final DiffContentPanel leftContentPanel;
    private final DiffContentPanel rightContentPanel;

    /**
     * True when this class caused the current scroll event, false otherwise.
     */ 
    private boolean myScrollEvent;
    
    private int lastScrollOffset;

    private int                     cachedDiffSerial;
    private DecoratedDifference []  decorationsCached = new DecoratedDifference[0];
    private HighLight []            secondHilitesCached = new HighLight[0];
    private HighLight []            firstHilitesCached = new HighLight[0];
    
    public DiffViewManager(EditableDiffView master) {
        this.master = master;
        this.leftContentPanel = master.getEditorPane1();
        this.rightContentPanel = master.getEditorPane2();
    }
    
    void init() {
        initScrolling();
    }

    private void initScrolling() {
        leftContentPanel.getScrollPane().getVerticalScrollBar().getModel().addChangeListener(this);
        rightContentPanel.getScrollPane().getVerticalScrollBar().getModel().addChangeListener(this);
        // The vertical scroll bar must be there for mouse wheel to work correctly.
        // However it's not necessary to be seen (but must be visible so that the wheel will work).
        leftContentPanel.getScrollPane().getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
    }

    public void stateChanged(ChangeEvent e) {
        JScrollBar leftScrollBar = leftContentPanel.getScrollPane().getVerticalScrollBar();
        JScrollBar rightScrollBar = rightContentPanel.getScrollPane().getVerticalScrollBar();
        if (e.getSource() == leftContentPanel.getScrollPane().getVerticalScrollBar().getModel()) {
            int value = leftScrollBar.getValue();
            leftContentPanel.getActionsScrollPane().getVerticalScrollBar().setValue(value);
            if (myScrollEvent) return;
            myScrollEvent = true;
            rightScrollBar.setValue((int) (value / getScrollFactor()));
        } else {
            int value = rightScrollBar.getValue();
            rightContentPanel.getActionsScrollPane().getVerticalScrollBar().setValue(value);
            if (myScrollEvent) return;
            myScrollEvent = true;
            smartScroll();
        }
        master.getMyDivider().repaint();
        myScrollEvent = false;
    }
    
    public void scroll() {
        myScrollEvent = true;
        smartScroll();
        master.getMyDivider().repaint();
        myScrollEvent = false;
    }
    
    EditableDiffView getMaster() {
        return master;
    }
    
    private void updateDifferences() {
        assert Thread.holdsLock(this);
        int mds = master.getDiffSerial();
        if (mds <= cachedDiffSerial) return;
        cachedDiffSerial = mds;
        computeDecorations();
        computeSecondHighlights();
        computeFirstHighlights();
    }

    public synchronized DecoratedDifference [] getDecorations() {
        updateDifferences();
        return decorationsCached;
    }

    public synchronized HighLight[] getSecondHighlights() {
        updateDifferences();
        return secondHilitesCached;
    }

    public synchronized HighLight[] getFirstHighlights() {
        updateDifferences();
        return firstHilitesCached;
    }
    
    private void computeFirstHighlights() {
        List<HighLight> hilites = new ArrayList<HighLight>();
        Document doc = leftContentPanel.getEditorPane().getDocument();
        for (DecoratedDifference dd : decorationsCached) {
            Difference diff = dd.getDiff();
            if (dd.getBottomLeft() == -1) continue;
            int start = getRowStartFromLineOffset(doc, diff.getFirstStart() - 1);
            if (isOneLineChange(diff)) {
                StringTokenizer firstSt = new StringTokenizer(diff.getFirstText(), "\n");
                StringTokenizer secondSt = new StringTokenizer(diff.getSecondText(), "\n");
                for (int i = diff.getSecondStart(); i <= diff.getSecondEnd(); i++) {
                    String firstRow = firstSt.nextToken();                 
                    String secondRow = secondSt.nextToken();                 
                    List<HighLight> rowhilites = computeFirstRowHilites(start, firstRow, secondRow);
                    hilites.addAll(rowhilites);
                    start += firstRow.length() + 1;
                }
            } else {
                int end = getRowStartFromLineOffset(doc, diff.getFirstEnd());
                if (end == -1) {
                    end = doc.getLength();
                }
                SimpleAttributeSet attrs = new SimpleAttributeSet();
                StyleConstants.setBackground(attrs, master.getColor(diff));
                attrs.addAttribute(HighlightsContainer.ATTR_EXTENDS_EOL, Boolean.TRUE);
                hilites.add(new HighLight(start, end, attrs));
            }
        }
        firstHilitesCached = hilites.toArray(new HighLight[hilites.size()]);
    }
    
    static int getRowStartFromLineOffset(Document doc, int lineIndex) {
        if (doc instanceof BaseDocument) {
            return Utilities.getRowStartFromLineOffset((BaseDocument) doc, lineIndex);
        } else {
            // TODO: find row start from line offet
            Element element = doc.getDefaultRootElement();
            Element line = element.getElement(lineIndex);
            return line.getStartOffset();
        }
    }
    
    private void computeSecondHighlights() {
        List<HighLight> hilites = new ArrayList<HighLight>();
        Document doc = rightContentPanel.getEditorPane().getDocument();
        for (DecoratedDifference dd : decorationsCached) {
            Difference diff = dd.getDiff();
            if (dd.getBottomRight() == -1) continue;
            int start = getRowStartFromLineOffset(doc, diff.getSecondStart() - 1);
            if (isOneLineChange(diff)) {
                StringTokenizer firstSt = new StringTokenizer(diff.getFirstText(), "\n");
                StringTokenizer secondSt = new StringTokenizer(diff.getSecondText(), "\n");
                for (int i = diff.getSecondStart(); i <= diff.getSecondEnd(); i++) {
                    String firstRow = firstSt.nextToken();                 
                    String secondRow = secondSt.nextToken();                 
                    List<HighLight> rowhilites = computeSecondRowHilites(start, firstRow, secondRow);
                    hilites.addAll(rowhilites);
                    start += secondRow.length() + 1;
                }
            } else {
                int end = getRowStartFromLineOffset(doc, diff.getSecondEnd());
                if (end == -1) {
                    end = doc.getLength();
                }
                SimpleAttributeSet attrs = new SimpleAttributeSet();
                StyleConstants.setBackground(attrs, master.getColor(diff));
                attrs.addAttribute(HighlightsContainer.ATTR_EXTENDS_EOL, Boolean.TRUE);
                hilites.add(new HighLight(start, end, attrs));
            }
        }
        secondHilitesCached = hilites.toArray(new HighLight[hilites.size()]);
    }

    private List<HighLight> computeFirstRowHilites(int rowStart, String left, String right) {
        List<HighLight> hilites = new ArrayList<HighLight>(4);
        
        String leftRows = wordsToRows(left);  
        String rightRows = wordsToRows(right);

        DiffProvider diffprovider = Lookup.getDefault().lookup(DiffProvider.class);
        if (diffprovider == null) {
            return hilites;
        }

        Difference[] diffs;
        try {
            diffs = diffprovider.computeDiff(new StringReader(leftRows), new StringReader(rightRows));
        } catch (IOException e) {
            return hilites;
        }

        // what we can hilite in first source
        for (Difference diff : diffs) {
            if (diff.getType() == Difference.ADD) continue;
            int start = rowOffset(leftRows, diff.getFirstStart());
            int end = rowOffset(leftRows, diff.getFirstEnd() + 1);
            
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setBackground(attrs, master.getColor(diff));
            hilites.add(new HighLight(rowStart + start, rowStart + end, attrs));
        }
        return hilites;
    }    
    
    private List<HighLight> computeSecondRowHilites(int rowStart, String left, String right) {
        List<HighLight> hilites = new ArrayList<HighLight>(4);
        
        String leftRows = wordsToRows(left);  
        String rightRows = wordsToRows(right);

        DiffProvider diffprovider = Lookup.getDefault().lookup(DiffProvider.class);
        if (diffprovider == null) {
            return hilites;
        }

        Difference[] diffs;
        try {
            diffs = diffprovider.computeDiff(new StringReader(leftRows), new StringReader(rightRows));
        } catch (IOException e) {
            return hilites;
        }

        // what we can hilite in second source
        for (Difference diff : diffs) {
            if (diff.getType() == Difference.DELETE) continue;
            int start = rowOffset(rightRows, diff.getSecondStart());
            int end = rowOffset(rightRows, diff.getSecondEnd() + 1);
            
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setBackground(attrs, master.getColor(diff));
            hilites.add(new HighLight(rowStart + start, rowStart + end, attrs));
        }
        return hilites;
    }

    /**
     * 1-based row index.
     * 
     * @param row
     * @param rowIndex
     * @return
     */
    private int rowOffset(String row, int rowIndex) {
        if (rowIndex == 1) return 0; 
        int newLines = 0;
        for (int i = 0; i < row.length(); i++) {
            char c = row.charAt(i);
            if (c == '\n') {
                newLines++;
                if (--rowIndex == 1) {
                    return i + 1 - newLines;
                }
            }
        }
        return row.length();
    }

    private String wordsToRows(String s) {
        StringBuilder sb = new StringBuilder(s.length() * 2);
        StringTokenizer st = new StringTokenizer(s, " \t\n[]{};:'\",.<>/?-=_+\\|~!@#$%^&*()", true); // NOI18N
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.length() == 0) continue;
            sb.append(token);
            sb.append('\n');
        }
        return sb.toString();
    }

    private boolean isOneLineChange(Difference diff) {
        return diff.getType() == Difference.CHANGE && 
                diff.getFirstEnd() - diff.getFirstStart() == diff.getSecondEnd() - diff.getSecondStart();
    }

    private void computeDecorations() {
        
        EditorUI editorUI = org.netbeans.editor.Utilities.getEditorUI(rightContentPanel.getEditorPane());
        int lineHeight = editorUI.getLineHeight();
        
        Difference [] diffs = master.getDifferences();
        decorationsCached = new DecoratedDifference[diffs.length];
        for (int i = 0; i < diffs.length; i++) {
            Difference difference = diffs[i];
            DecoratedDifference dd = new DecoratedDifference(difference);
            
            if (difference.getType() == Difference.ADD) {
                dd.topRight = (difference.getSecondStart() - 1) * lineHeight;
                dd.bottomRight = difference.getSecondEnd() * lineHeight;
                dd.topLeft = difference.getFirstStart() * lineHeight;
                dd.floodFill = true;
            } else if (difference.getType() == Difference.DELETE) {
                dd.topLeft = (difference.getFirstStart() - 1) * lineHeight;
                dd.bottomLeft = difference.getFirstEnd() * lineHeight;
                dd.topRight = difference.getSecondStart() * lineHeight;
                dd.floodFill = true;
            } else {
                dd.topRight = (difference.getSecondStart() - 1) * lineHeight;
                dd.bottomRight = difference.getSecondEnd() * lineHeight;
                dd.topLeft = (difference.getFirstStart() - 1) * lineHeight;
                dd.bottomLeft = difference.getFirstEnd() * lineHeight;
                dd.floodFill = true;
            }
            decorationsCached[i] = dd;
        }
    }

    /**
     * 1. find the difference whose top (first line) is closest to the center of the screen. If there is no difference on screen, proceed to #5
     * 2. find line offset of the found difference in the other document
     * 3. scroll the other document so that the difference starts on the same visual line
     * 
     * 5. scroll the other document proportionally
     */ 
    private void smartScroll() {
        DiffContentPanel rightPane = master.getEditorPane2();
        DiffContentPanel leftPane = master.getEditorPane1();        
/*
        if (rightPane.getScrollPane().getVerticalScrollBar().getModel().getValue() + rightPane.getScrollPane().getVerticalScrollBar().getModel().getExtent() >=
                rightPane.getScrollPane().getVerticalScrollBar().getModel().getMaximum()) {
            leftPane.getScrollPane().getVerticalScrollBar().getModel().setValue(leftPane.getScrollPane().getVerticalScrollBar().getModel().getMaximum());
            return;
        }
*/
        
        DifferencePosition toMatch = findDifferenceToMatch();
        if (toMatch == null) {
            int value = rightPane.getScrollPane().getVerticalScrollBar().getValue();
//            value = (int) (initiator == rightEditorPane ? value * getScrollFactor() : value / getScrollFactor());
            value += lastScrollOffset;
            leftPane.getScrollPane().getVerticalScrollBar().setValue(value);
        } else {
            lastScrollOffset = scrollToMatchDifference(toMatch);
        }
    }

    private int scrollToMatchDifference(DifferencePosition differenceMatchStart) {

        DiffContentPanel rightPane = master.getEditorPane2();
        DiffContentPanel leftPane = master.getEditorPane1();
        
        Difference diff = differenceMatchStart.getDiff();
        boolean matchStart = differenceMatchStart.isStart();
        
        EditorUI editorUI = org.netbeans.editor.Utilities.getEditorUI(leftPane.getEditorPane());
        
        int value;
        int valueSecond;
        if (matchStart) {
            value = diff.getFirstStart() * editorUI.getLineHeight();        // kde zacina prva, 180
            valueSecond = diff.getSecondStart() * editorUI.getLineHeight(); // kde by zacinala druha, napr. 230
        } else {
            if (diff.getType() == Difference.ADD) {
                value = diff.getFirstStart() * editorUI.getLineHeight();        // kde zacina prva, 180
                value -= editorUI.getLineHeight();
                valueSecond = diff.getSecondEnd() * editorUI.getLineHeight(); // kde by zacinala druha, napr. 230
            } else {
                value = diff.getFirstEnd() * editorUI.getLineHeight();        // kde zacina prva, 180
                if (diff.getType() == Difference.DELETE) value += editorUI.getLineHeight();
                valueSecond = diff.getSecondEnd() * editorUI.getLineHeight(); // kde by zacinala druha, napr. 230
            }
        }

        // druha je na 400
        int currentSecond = rightPane.getScrollPane().getVerticalScrollBar().getValue();
        int secondOffset = currentSecond - valueSecond;
        
        value += secondOffset;
        if (diff.getType() == Difference.ADD) value += editorUI.getLineHeight();
        if (diff.getType() == Difference.DELETE) value -= editorUI.getLineHeight();
        
        leftPane.getScrollPane().getVerticalScrollBar().setValue(value);
        return value - rightPane.getScrollPane().getVerticalScrollBar().getValue();
    }

    private DifferencePosition findDifferenceToMatch() {
        
        DiffContentPanel rightPane = master.getEditorPane2();

        DecoratedDifference candidate = null;
        Rectangle rightClip = rightPane.getScrollPane().getViewport().getViewRect();
        
        DecoratedDifference [] diffs = getDecorations();
        for (DecoratedDifference dd : diffs) {
            if (dd.getTopRight() > rightClip.y + rightClip.height) break;
            if (dd.getBottomRight() != -1 && dd.getBottomRight() <= rightClip.y) continue;
            if (candidate != null) {
                if (candidate.getDiff().getType() == Difference.DELETE) {
                    candidate = dd;
                } else if (candidate.getTopRight() < rightClip.y) { 
                    candidate = dd;
                } else if (dd.getTopRight() <= rightClip.y + rightClip.height / 2) { 
                    candidate = dd;
                }
            } else {
                candidate = dd;
            }
        }
        if (candidate == null) return null;
        boolean isHigh = (candidate.getTopRight() < rightClip.y + rightClip.height / 3);
        boolean isLow = (candidate.getTopRight() < rightClip.y + rightClip.height * 2 / 3);
        boolean matchEnd = candidate.getDiff().getType() == Difference.DELETE && isLow || 
                           candidate.getDiff().getType() == Difference.ADD && isHigh ||
                           candidate.getDiff().getType() != Difference.ADD && candidate == diffs[diffs.length - 1];
        return new DifferencePosition(candidate.getDiff(), !matchEnd);
    }

    private double getScrollFactor() {
        BoundedRangeModel m1 = leftContentPanel.getScrollPane().getVerticalScrollBar().getModel();
        BoundedRangeModel m2 = rightContentPanel.getScrollPane().getVerticalScrollBar().getModel();
        return ((double) m1.getMaximum() - m1.getExtent()) / (m2.getMaximum() - m2.getExtent());
    }


    /**
     * The split pane needs to be repainted along with editor.
     * 
     * @param decoratedEditorPane the pane that is currently repainting
     */ 
    void editorPainting(DecoratedEditorPane decoratedEditorPane) {
        if (!decoratedEditorPane.isFirst()) {
            JComponent mydivider = master.getMyDivider();
            mydivider.paint(mydivider.getGraphics());
        }
    }
    
    public static class DifferencePosition {
        
        private Difference  diff;
        private boolean     isStart;

        public DifferencePosition(Difference diff, boolean start) {
            this.diff = diff;
            isStart = start;
        }

        public Difference getDiff() {
            return diff;
        }

        public boolean isStart() {
            return isStart;
        }
    }    

    public static class DecoratedDifference {
        private Difference  diff;
        private int         topLeft;            // top line in the left pane
        private int         bottomLeft = -1;    // bottom line in the left pane, -1 for ADDs
        private int         topRight;
        private int         bottomRight = -1;   // bottom line in the right pane, -1 for DELETEs
        private boolean     floodFill;          // should the whole difference be highlited

        public DecoratedDifference(Difference difference) {
            diff = difference;
        }

        public Difference getDiff() {
            return diff;
        }

        public int getTopLeft() {
            return topLeft;
        }

        public int getBottomLeft() {
            return bottomLeft;
        }

        public int getTopRight() {
            return topRight;
        }

        public int getBottomRight() {
            return bottomRight;
        }

        public boolean isFloodFill() {
            return floodFill;
        }
    }

    public static class HighLight {
        
        private final int           startOffset;
        private final int           endOffset;
        private final AttributeSet  attrs;

        public HighLight(int startOffset, int endOffset, AttributeSet attrs) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.attrs = attrs;
        }

        public int getStartOffset() {
            return startOffset;
        }

        public int getEndOffset() {
            return endOffset;
        }

        public AttributeSet getAttrs() {
            return attrs;
        }
    }
}
