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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.diff.builtin.visualizer.editable;

import org.netbeans.api.diff.Difference;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.EditorKit;
import javax.accessibility.AccessibleContext;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Represents left and/or right side of the main split pane.
 *
 * @author Maros Sandor
 */
class DiffContentPanel extends JComponent implements HighlightsContainer {

    private final EditableDiffView master;
    private final boolean isFirst;

    private final DecoratedEditorPane     editorPane;
    private JScrollPane                   scrollPane;
    private final LineNumbersActionsBar   linesActions;
    private final JScrollPane             actionsScrollPane;

    private Difference[] currentDiff;
    
    public DiffContentPanel(EditableDiffView master, boolean isFirst) {
        this.master = master;
        this.isFirst = isFirst;

        setLayout(new BorderLayout());

        editorPane = new DecoratedEditorPane(this);
        editorPane.setEditable(false);
        scrollPane = new JScrollPane(editorPane);
        add(scrollPane);
        
        linesActions = new LineNumbersActionsBar(this, master.isActionsEnabled());
        actionsScrollPane = new JScrollPane(linesActions);
        actionsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        actionsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        actionsScrollPane.setBorder(null);
        add(actionsScrollPane, isFirst ? BorderLayout.LINE_END : BorderLayout.LINE_START);
        
        editorPane.putClientProperty(DiffHighlightsLayerFactory.HIGHLITING_LAYER_ID, this);
        if (!isFirst) {
            editorPane.putClientProperty("errorStripeOnly", Boolean.TRUE);
            editorPane.putClientProperty("code-folding-enable", false);
        }
    }
    
    void initActions() {
        //TODO: copied from CloneableEditor - this has no effect
        ActionMap paneMap = editorPane.getActionMap();
        ActionMap am = getActionMap();
        am.setParent(paneMap);
        paneMap.put(DefaultEditorKit.cutAction, getAction(DefaultEditorKit.cutAction));
        paneMap.put(DefaultEditorKit.copyAction, getAction(DefaultEditorKit.copyAction));
        paneMap.put("delete", getAction(DefaultEditorKit.deleteNextCharAction)); // NOI18N
        paneMap.put(DefaultEditorKit.pasteAction, getAction(DefaultEditorKit.pasteAction));
    }
    
    private Action getAction(String key) {
        if (key == null) {
            return null;
        }

        // Try to find the action from kit.
        EditorKit kit = editorPane.getEditorKit();

        if (kit == null) { // kit is cleared in closeDocument()

            return null;
        }

        Action[] actions = kit.getActions();

        for (int i = 0; i < actions.length; i++) {
            if (key.equals(actions[i].getValue(Action.NAME))) {
                return actions[i];
            }
        }

        return null;
    }
    
    LineNumbersActionsBar getLinesActions() {
        return linesActions;
    }

    public JScrollPane getActionsScrollPane() {
        return actionsScrollPane;
    }

    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    public Difference[] getCurrentDiff() {
        return currentDiff;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public void setCurrentDiff(Difference[] currentDiff) {
        this.currentDiff = currentDiff;
        editorPane.setDifferences(currentDiff);
        linesActions.onDiffSetChanged();
        fireHilitingChanged();
//        revalidate();
    }

    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        Container parent = getParent();
        if (parent instanceof JViewport) {
            if (parent.getWidth() > d.width) {
                d = new Dimension(parent.getWidth(), d.height);
            }
        }
        return d;
    }

    public DecoratedEditorPane getEditorPane() {
        return editorPane;
    }

    public AccessibleContext getAccessibleContext() {
        return editorPane.getAccessibleContext();
    }

    public EditableDiffView getMaster() {
        return master;
    }

    // === Highliting ======================================================================== 
    
    HighlightsContainer getHighlightsContainer() {
        return this;
    }

    public HighlightsSequence getHighlights(int start, int end) {
        return new DiffHighlightsSequence(start, end);
    }

    private final List<HighlightsChangeListener> listeners = new ArrayList<HighlightsChangeListener>(1);
    
    public void addHighlightsChangeListener(HighlightsChangeListener listener) {
        synchronized(listeners) {
            listeners.add(listener);
        }
    }

    public void removeHighlightsChangeListener(HighlightsChangeListener listener) {
        synchronized(listeners) {
            listeners.remove(listener);
        }
    }
    
    void fireHilitingChanged() {
        synchronized(listeners) {
            for (HighlightsChangeListener listener : listeners) {
              listener.highlightChanged(new HighlightsChangeEvent(this, 0, Integer.MAX_VALUE));
            }
        }
    }

    void onUISettingsChanged() {
        editorPane.repaint();
        linesActions.onUISettingsChanged();
        actionsScrollPane.revalidate();
        actionsScrollPane.repaint();
        revalidate();
        repaint();
    }
    
    public void setCustomEditor(JComponent c) {
        remove(scrollPane);
        // The present editorPane will already be wrapped with the new custom editor
        // including the new scrollpane that needs to be re-assigned
        Component viewPort = editorPane.getParent();
        if (viewPort instanceof JViewport) {
            viewPort = viewPort.getParent();
            if (viewPort instanceof JScrollPane) {
                scrollPane = (JScrollPane)viewPort;
                add(c);
            }
        }
    }

    /**
     * Iterates over all found differences.
     */
    private class DiffHighlightsSequence implements HighlightsSequence {
        
        private final int       endOffset;
        private final int       startOffset;

        private int             currentHiliteIndex = -1;             
        private DiffViewManager.HighLight [] hilites;

        public DiffHighlightsSequence(int start, int end) {
            this.startOffset = start;
            this.endOffset = end;
            lookupHilites();
        }

        private void lookupHilites() {
            List<DiffViewManager.HighLight> list = new ArrayList<DiffViewManager.HighLight>();
            DiffViewManager.HighLight[] allHilites = isFirst ? master.getManager().getFirstHighlights() : master.getManager().getSecondHighlights(); 
            for (DiffViewManager.HighLight hilite : allHilites) {
                if (hilite.getEndOffset() < startOffset) continue;
                if (hilite.getStartOffset() > endOffset) break;
                list.add(hilite);
            }
            hilites = list.toArray(new DiffViewManager.HighLight[list.size()]);
        }

        public boolean moveNext() {
            if (currentHiliteIndex >= hilites.length - 1) return false;
            currentHiliteIndex++;
            return true;
        }

        public int getStartOffset() {
            return hilites[currentHiliteIndex].getStartOffset();
        }

        public int getEndOffset() {
            return hilites[currentHiliteIndex].getEndOffset();
        }

        public AttributeSet getAttributes() {
            return hilites[currentHiliteIndex].getAttrs();
        }
    }
}
