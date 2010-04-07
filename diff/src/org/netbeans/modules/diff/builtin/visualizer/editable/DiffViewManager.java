/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.diff.builtin.visualizer.editable;

import org.netbeans.api.diff.Difference;
import org.netbeans.editor.*;
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
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.*;
import org.openide.util.RequestProcessor;

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
    
    private int                     cachedDiffSerial;
    private DecoratedDifference []  decorationsCached = new DecoratedDifference[0];
    private HighLight []            secondHilitesCached = new HighLight[0];
    private HighLight []            firstHilitesCached = new HighLight[0];
    private final ScrollMapCached   scrollMap = new ScrollMapCached();
    private final RequestProcessor.Task highlightComputeTask;
    
    public DiffViewManager(EditableDiffView master) {
        this.master = master;
        this.leftContentPanel = master.getEditorPane1();
        this.rightContentPanel = master.getEditorPane2();
        highlightComputeTask = new RequestProcessor("DiffViewHighlightsComputer", 1, true, false).create(new HighlightsComputeTask());
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

    private final Boolean [] smartScrollDisabled = new Boolean[] { Boolean.FALSE };
    
    public void runWithSmartScrollingDisabled(Runnable runnable) {
        synchronized (smartScrollDisabled) {
            smartScrollDisabled[0] = true;
        }
        try {
            runnable.run();
        } catch (Exception e) {
            Logger.getLogger(DiffViewManager.class.getName()).log(Level.SEVERE, "", e);
        } finally {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    synchronized (smartScrollDisabled) {
                        smartScrollDisabled[0] = false;
                    }
                }
            });
        }
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
        JScrollBar leftScrollBar = leftContentPanel.getScrollPane().getVerticalScrollBar();
        JScrollBar rightScrollBar = rightContentPanel.getScrollPane().getVerticalScrollBar();
        if (e.getSource() == leftContentPanel.getScrollPane().getVerticalScrollBar().getModel()) {
            int value = leftScrollBar.getValue();
            leftContentPanel.getActionsScrollPane().getVerticalScrollBar().setValue(value);
            if (myScrollEvent) return;
            myScrollEvent = true;
        } else {
            int value = rightScrollBar.getValue();
            rightContentPanel.getActionsScrollPane().getVerticalScrollBar().setValue(value);
            if (myScrollEvent) return;
            myScrollEvent = true;
            boolean doSmartScroll;
            synchronized (smartScrollDisabled) {
                doSmartScroll = !smartScrollDisabled[0];
            }
            if (doSmartScroll) {
                smartScroll();
                master.updateCurrentDifference();
            }
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
    
    private int rightHeightCached;
    private void updateDifferences() {
        assert EventQueue.isDispatchThread();
        int mds = master.getDiffSerial();
        if (mds <= cachedDiffSerial && rightContentPanel.getEditorPane().getSize().height == rightHeightCached) {
            return;
        }
        rightHeightCached = rightContentPanel.getEditorPane().getSize().height;
        cachedDiffSerial = mds;
        computeDecorations();
        master.getEditorPane1().getLinesActions().repaint();
        master.getEditorPane2().getLinesActions().repaint();
        firstHilitesCached = secondHilitesCached = new HighLight[0];
        // interrupt running highlight scan and start new one outside of AWT
        highlightComputeTask.cancel();
        highlightComputeTask.schedule(0);
    }

    public DecoratedDifference [] getDecorations() {
        if (EventQueue.isDispatchThread()) {
            updateDifferences();
        }
        return decorationsCached;
    }

    public HighLight[] getSecondHighlights() {
        if (EventQueue.isDispatchThread()) {
            updateDifferences();
        }
        return secondHilitesCached;
    }

    public HighLight[] getFirstHighlights() {
        if (EventQueue.isDispatchThread()) {
            updateDifferences();
        }
        return firstHilitesCached;
    }
    
    private void computeFirstHighlights() {
        List<HighLight> hilites = new ArrayList<HighLight>();
        Document doc = leftContentPanel.getEditorPane().getDocument();
        DecoratedDifference[] decorations = decorationsCached;
        for (DecoratedDifference dd : decorations) {
            if (Thread.interrupted()) {
                return;
            }
            Difference diff = dd.getDiff();
            if (dd.getBottomLeft() == -1) continue;
            int start = getRowStartFromLineOffset(doc, diff.getFirstStart() - 1);
            if (isOneLineChange(diff)) {
                CorrectRowTokenizer firstSt = new CorrectRowTokenizer(diff.getFirstText());
                CorrectRowTokenizer secondSt = new CorrectRowTokenizer(diff.getSecondText());
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
        DecoratedDifference[] decorations = decorationsCached;
        for (DecoratedDifference dd : decorations) {
            if (Thread.interrupted()) {
                return;
            }
            Difference diff = dd.getDiff();
            if (dd.getBottomRight() == -1) continue;
            int start = getRowStartFromLineOffset(doc, diff.getSecondStart() - 1);
            if (isOneLineChange(diff)) {
                CorrectRowTokenizer firstSt = new CorrectRowTokenizer(diff.getFirstText());
                CorrectRowTokenizer secondSt = new CorrectRowTokenizer(diff.getSecondText());
                for (int i = diff.getSecondStart(); i <= diff.getSecondEnd(); i++) {
                    try {
                        String firstRow = firstSt.nextToken();
                        String secondRow = secondSt.nextToken();
                        List<HighLight> rowhilites = computeSecondRowHilites(start, firstRow, secondRow);
                        hilites.addAll(rowhilites);
                        start += secondRow.length() + 1;
                    } catch (Exception e) {
                        //
                    }
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
        
        Document document = master.getEditorPane2().getEditorPane().getDocument();
        View rootLeftView = Utilities.getDocumentView(leftContentPanel.getEditorPane());
        View rootRightView = Utilities.getDocumentView(rightContentPanel.getEditorPane());
        if (rootLeftView == null || rootRightView == null) return;
        
        Difference [] diffs = master.getDifferences();
        DecoratedDifference[] decorations = new DecoratedDifference[diffs.length];
        for (int i = 0; i < diffs.length; i++) {
            Difference difference = diffs[i];
            DecoratedDifference dd = new DecoratedDifference(difference, canRollback(document, difference));
            Rectangle leftStartRect = getRectForView(leftContentPanel.getEditorPane(), rootLeftView, difference.getFirstStart() - 1, false);
            Rectangle leftEndRect = getRectForView(leftContentPanel.getEditorPane(), rootLeftView, difference.getFirstEnd() - 1, true);
            Rectangle rightStartRect = getRectForView(rightContentPanel.getEditorPane(), rootRightView, difference.getSecondStart() - 1, false);
            Rectangle rightEndRect = getRectForView(rightContentPanel.getEditorPane(), rootRightView, difference.getSecondEnd() - 1, true);
            if (leftStartRect == null || leftEndRect == null || rightStartRect == null || rightEndRect == null) {
                decorations = new DecoratedDifference[0];
                break;
            }
            if (difference.getType() == Difference.ADD) {
                dd.topRight = rightStartRect.y;
                dd.bottomRight = rightEndRect.y + rightEndRect.height;
                dd.topLeft = leftStartRect.y + leftStartRect.height;
                dd.floodFill = true;
            } else if (difference.getType() == Difference.DELETE) {
                dd.topLeft = leftStartRect.y;
                dd.bottomLeft = leftEndRect.y + leftEndRect.height;
                dd.topRight = rightStartRect.y + rightStartRect.height;
                dd.floodFill = true;
            } else {
                dd.topRight = rightStartRect.y;
                dd.bottomRight = rightEndRect.y + rightEndRect.height;
                dd.topLeft = leftStartRect.y;
                dd.bottomLeft = leftEndRect.y + leftEndRect.height;
                dd.floodFill = true;
            }
            decorations[i] = dd;
        }
        decorationsCached = decorations;
    }

    private Rectangle getRectForView (JTextComponent comp, View rootView, int lineNumber, boolean endOffset) {
        if (lineNumber == -1 || lineNumber >= rootView.getViewCount()) {
            return new Rectangle();
        }
        Rectangle rect = null;
        View view = rootView.getView(lineNumber);
        try {
            rect = view == null ? null : comp.modelToView(endOffset ? view.getEndOffset() - 1 : view.getStartOffset());
        } catch (BadLocationException ex) {
            //
        }
        return rect;
    }

    private boolean canRollback(Document doc, Difference diff) {
        if (!(doc instanceof GuardedDocument)) return true;
        GuardedDocument document = (GuardedDocument) doc;
        int start, end;
        if (diff.getType() == Difference.DELETE) {
            start = end = Utilities.getRowStartFromLineOffset(document, diff.getSecondStart());
        } else {
            start = Utilities.getRowStartFromLineOffset(document, diff.getSecondStart() - 1);
            end = Utilities.getRowStartFromLineOffset(document, diff.getSecondEnd());
        }
        MarkBlockChain mbc = ((GuardedDocument) document).getGuardedBlockChain();
        return (mbc.compareBlock(start, end) & MarkBlock.OVERLAP) == 0;
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
        
        int [] map = scrollMap.getScrollMap(rightPane.getEditorPane().getSize().height, master.getDiffSerial());
        
        int rightOffet = rightPane.getScrollPane().getVerticalScrollBar().getValue();
        if (rightOffet >= map.length) return;
        leftPane.getScrollPane().getVerticalScrollBar().setValue(map[rightOffet]);
    }

    private int computeLeftOffsetToMatchDifference(DifferencePosition differenceMatchStart, int rightOffset, View rootLeftView, View rootRightView) {

        Difference diff = differenceMatchStart.getDiff();
        boolean matchStart = differenceMatchStart.isStart();
        
        int value;
        int valueSecond;
        Rectangle leftStartRect = getRectForView(leftContentPanel.getEditorPane(), rootLeftView, diff.getFirstStart() - 1, false);
        Rectangle leftEndRect = getRectForView(leftContentPanel.getEditorPane(), rootLeftView, diff.getFirstEnd() - 1, true);
        Rectangle rightStartRect = getRectForView(rightContentPanel.getEditorPane(), rootRightView, diff.getSecondStart() - 1, false);
        Rectangle rightEndRect = getRectForView(rightContentPanel.getEditorPane(), rootRightView, diff.getSecondEnd() - 1, true);
        if (matchStart) {
            value = leftStartRect.y + leftStartRect.height;        // kde zacina prva, 180
            valueSecond = rightStartRect.y + rightStartRect.height; // kde by zacinala druha, napr. 230
        } else {
            if (diff.getType() == Difference.ADD) {
                value = leftStartRect.y;        // kde zacina prva, 180
                valueSecond = rightStartRect.y + rightStartRect.height; // kde by zacinala druha, napr. 230
            } else {
                value = leftEndRect.y + leftEndRect.height;        // kde zacina prva, 180
                if (diff.getType() == Difference.DELETE) {
                    value += leftEndRect.height;
                    valueSecond = rightStartRect.y + rightStartRect.height; // kde by zacinala druha, napr. 230
                } else {
                    valueSecond = rightEndRect.y + rightEndRect.height; // kde by zacinala druha, napr. 230
                }
            }
        }

        // druha je na 400
        int secondOffset = rightOffset - valueSecond;
        
        value += secondOffset;
        if (diff.getType() == Difference.ADD) value += rightStartRect.height;
        if (diff.getType() == Difference.DELETE) value -= leftStartRect.height;
        
        return value;
    }
    
    private DifferencePosition findDifferenceToMatch(int rightOffset, int rightViewportHeight) {
        
        DecoratedDifference candidate = null;
        
        DecoratedDifference [] diffs = getDecorations();
        for (DecoratedDifference dd : diffs) {
            if (dd.getTopRight() > rightOffset + rightViewportHeight) break;
            if (dd.getBottomRight() != -1) {
                if (dd.getBottomRight() <= rightOffset) continue;
            } else {
                if (dd.getTopRight() <= rightOffset) continue;
            }
            if (candidate != null) {
                if (candidate.getDiff().getType() == Difference.DELETE) {
                    candidate = dd;
                } else if (candidate.getTopRight() < rightOffset) { 
                    candidate = dd;
                } else if (dd.getTopRight() <= rightOffset + rightViewportHeight / 2) { 
                    candidate = dd;
                }
            } else {
                candidate = dd;
            }
        }
        if (candidate == null) return null;
        boolean matchStart = candidate.getTopRight() > rightOffset + rightViewportHeight / 2;
        if (candidate.getDiff().getType() == Difference.DELETE && candidate.getTopRight() < rightOffset + rightViewportHeight * 4 / 5) matchStart = false;
        if (candidate.getDiff().getType() == Difference.DELETE && candidate == diffs[diffs.length -1]) matchStart = false;
        return new DifferencePosition(candidate.getDiff(), matchStart);
    }

    double getScrollFactor() {
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
        private final Difference    diff;
        private final boolean       canRollback;
        private int         topLeft;            // top line in the left pane
        private int         bottomLeft = -1;    // bottom line in the left pane, -1 for ADDs
        private int         topRight;
        private int         bottomRight = -1;   // bottom line in the right pane, -1 for DELETEs
        private boolean     floodFill;          // should the whole difference be highlited

        public DecoratedDifference(Difference difference, boolean canRollback) {
            diff = difference;
            this.canRollback = canRollback;
        }

        public boolean canRollback() {
            return canRollback;
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

    /**
     * Java StringTokenizer does not work if the very first character is a delimiter.
     */
    private static class CorrectRowTokenizer {
        
        private final String s;
        private int idx;

        public CorrectRowTokenizer(String s) {
            this.s = s;
        }

        public String nextToken() {
            String token = null;
            for (int end = idx; end < s.length(); end++) {
                if (s.charAt(end) == '\n') {
                    token = s.substring(idx, end);
                    idx = end + 1;
                    break;
                }
            }
            return token;
        }
    }

    private class ScrollMapCached {
        
        private int     rightPanelHeightCached;
        private int []  scrollMapCached;
        private int     diffSerialCached;

        public synchronized int[] getScrollMap(int rightPanelHeight, int diffSerial) {
            if (rightPanelHeight != rightPanelHeightCached || diffSerialCached != diffSerial || scrollMapCached == null) {
                diffSerialCached = diffSerial;
                rightPanelHeightCached = rightPanelHeight;
                scrollMapCached = compute();
            }
            return scrollMapCached;
        }

        private int [] compute() {
            DiffContentPanel rightPane = master.getEditorPane2();

            int rightViewportHeight = rightPane.getScrollPane().getViewport().getViewRect().height; 

            int [] scrollMap = new int[rightPanelHeightCached];

            EditorUI editorUI = org.netbeans.editor.Utilities.getEditorUI(leftContentPanel.getEditorPane());
            if (editorUI == null) return scrollMap;

            int lastOffset = 0;
            View rootLeftView = Utilities.getDocumentView(leftContentPanel.getEditorPane());
            View rootRightView = Utilities.getDocumentView(rightContentPanel.getEditorPane());
            if (rootLeftView == null || rootRightView == null) return scrollMap;
            for (int rightOffset = 0; rightOffset < rightPanelHeightCached; rightOffset++) {
                DifferencePosition dpos = findDifferenceToMatch(rightOffset, rightViewportHeight);
                int leftOffset;
                if (dpos == null) {
                    leftOffset = lastOffset + rightOffset;
                } else {
                    leftOffset = computeLeftOffsetToMatchDifference(dpos, rightOffset, rootLeftView, rootRightView);
                    lastOffset = leftOffset - rightOffset;
                }
                scrollMap[rightOffset] = leftOffset;
            }
            scrollMap = smooth(scrollMap);
            return scrollMap;
        }

        private int[] smooth(int[] map) {
            int [] newMap = new int [map.length];
            int leftShift = 0;
            float correction = 0.0f;
            for (int i = 0; i < map.length; i++) {
                int leftOffset = map[i];
                int requestedShift = leftOffset - i; 
                if (requestedShift > leftShift) {
                    if (correction > requestedShift - leftShift) correction = requestedShift - leftShift;
                    leftShift += correction;
                    correction += 0.02f;
                } else if (requestedShift < leftShift) {
                    leftShift -= 1;
                } else {
                    correction = 1.0f;
                }
                newMap[i] = i + leftShift;
            }
            return newMap;
        }
    }

    /**
     * Counts differences for rows
     */
    private class HighlightsComputeTask implements Runnable {
        private int diffSerial;

        @Override
        public void run() {
            diffSerial = cachedDiffSerial;
            computeSecondHighlights();
            if (diffSerial != cachedDiffSerial) {
                return;
            }
            computeFirstHighlights();
            if (diffSerial == cachedDiffSerial) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        master.getEditorPane1().fireHilitingChanged();
                        master.getEditorPane2().fireHilitingChanged();
                    }
                });
            }
        }
    }
}
