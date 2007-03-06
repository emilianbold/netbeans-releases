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

import java.awt.event.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeEvent;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.AncestorEvent;
import javax.swing.text.*;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.modules.diff.NestableDiffView;
import org.netbeans.modules.diff.builtin.provider.BuiltInDiffProvider;
import org.netbeans.modules.diff.builtin.visualizer.GraphicalDiffVisualizer;
import org.netbeans.modules.diff.builtin.visualizer.SourceTranslatorAction;

import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.ErrorManager;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.cookies.SaveCookie;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.api.diff.DiffView;
import org.netbeans.spi.diff.DiffProvider;
import org.netbeans.spi.diff.DiffVisualizer;

/**
 * Panel that shows differences between two files. The code here was originally distributed among DiffPanel and
 * DiffComponent classes.
 * 
 * @author Maros Sandor
 */
public class EditableDiffView implements DiffView, NestableDiffView, DocumentListener, AncestorListener, PropertyChangeListener {

    // === Default Diff Colors ===========================================================
    private Color colorMissing = new Color(255, 160, 180);
    private Color colorAdded   = new Color(180, 255, 180);
    private Color colorChanged = new Color(160, 200, 255);
    private Color colorLines   = Color.DARK_GRAY;
    private Color COLOR_READONLY_BG = new Color(240,240,240);

    private final Difference [] NO_DIFFERENCES = new Difference[0];
    
    /**
     * Left (first) half of the Diff view, contains the editor pane, actions bar and line numbers bar.
     */
    private DiffContentPanel jEditorPane1;

    /**
     * Right (second) half of the Diff view, contains the editor pane, actions bar and line numbers bar.
     */
    private DiffContentPanel jEditorPane2;

    private boolean secondSourceAvailable;
    private boolean firstSourceAvailable;
    
    private JViewport jViewport2;

    final JLabel fileLabel1 = new JLabel();
    final JLabel fileLabel2 = new JLabel();
    final JPanel filePanel1 = new JPanel();
    final JPanel filePanel2 = new JPanel();
    final JSplitPane jSplitPane1 = new JSplitPane();

    private int diffSerial;
    private Difference[] diffs = NO_DIFFERENCES;
   
    private int currentDiffIndex = -1;
    
    private int totalHeight = 0;
    private int totalLines = 0;

    private int horizontalScroll1ChangedValue = -1;
    private int horizontalScroll2ChangedValue = -1;
    
    private RequestProcessor.Task   refreshDiffTask;
    private DiffViewManager manager;
    
    private boolean actionsEnabled;
    private DiffSplitPaneUI spui;
    
    /**
     * The right pane is editable IFF editableCookie is not null.
     */ 
    private EditorCookie.Observable editableCookie;
    private Document editableDocument;
    private UndoRedo.Manager editorUndoRedo;

    public EditableDiffView() {
    }

    public EditableDiffView(final StreamSource ss1, final StreamSource ss2) throws IOException {
        refreshDiffTask = RequestProcessor.getDefault().create(new RefreshDiffTask());
        initColors();
        String title1 = ss1.getTitle();
        if (title1 == null) title1 = NbBundle.getMessage(EditableDiffView.class, "CTL_DiffPanel_NoTitle"); // NOI18N
        String title2 = ss2.getTitle();
        if (title2 == null) title2 = NbBundle.getMessage(EditableDiffView.class, "CTL_DiffPanel_NoTitle"); // NOI18N
        String mimeType1 = ss1.getMIMEType();
        String mimeType2 = ss2.getMIMEType();
        if (mimeType1 == null) mimeType1 = mimeType2;
        if (mimeType2 == null) mimeType2 = mimeType1;
        
        actionsEnabled = ss2.isEditable();
                
        jEditorPane1 = new DiffContentPanel(this, true);
        jEditorPane2 = new DiffContentPanel(this, false);
        
        initComponents ();
        jSplitPane1.setName(org.openide.util.NbBundle.getMessage(EditableDiffView.class, "DiffComponent.title")); // NOI18N
        spui = new DiffSplitPaneUI(jSplitPane1);
        jSplitPane1.setUI(spui);
        jSplitPane1.setResizeWeight(0.5);
        jSplitPane1.setDividerSize(32);
        jSplitPane1.putClientProperty("PersistenceType", "Never"); // NOI18N
        jSplitPane1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EditableDiffView.class, "ACS_DiffPanelA11yName"));  // NOI18N
        jSplitPane1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditableDiffView.class, "ACS_DiffPanelA11yDesc"));  // NOI18N
        jEditorPane1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EditableDiffView.class, "ACS_EditorPane1A11yName"));  // NOI18N
        jEditorPane1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditableDiffView.class, "ACS_EditorPane1A11yDescr"));  // NOI18N
        jEditorPane2.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(EditableDiffView.class, "ACS_EditorPane2A11yName"));  // NOI18N
        jEditorPane2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(EditableDiffView.class, "ACS_EditorPane2A11yDescr"));  // NOI18N

        jSplitPane1.addAncestorListener(this);
        
        setSourceTitle(fileLabel1, title1);
        setSourceTitle(fileLabel2, title2);
        
        final String f1 = mimeType1;
        final String f2 = mimeType2;
        try {
            Runnable awtTask = new Runnable() {
                public void run() {
                    jEditorPane1.getEditorPane().setEditorKit(CloneableEditorSupport.getEditorKit(f1));
                    jEditorPane2.getEditorPane().setEditorKit(CloneableEditorSupport.getEditorKit(f2));
                    
                    try {
                        setSource1(ss1);
                        setSource2(ss2);
                    } catch (IOException ioex) {
                        org.openide.ErrorManager.getDefault().notify(ioex);
                    }
                    
                    if (!secondSourceAvailable) {
                        jEditorPane2.getEditorPane().setText(NbBundle.getMessage(EditableDiffView.class, "CTL_DiffPanel_NoContent")); // NOI18N
                    }
                    if (!firstSourceAvailable) {
                        jEditorPane1.getEditorPane().setText(NbBundle.getMessage(EditableDiffView.class, "CTL_DiffPanel_NoContent")); // NOI18N
                    }

                    Color borderColor = UIManager.getColor("scrollpane_border"); // NOI18N
                    if (borderColor == null) borderColor = UIManager.getColor("controlShadow"); // NOI18N

                    jEditorPane1.getScrollPane().setBorder(null);
                    jEditorPane2.getScrollPane().setBorder(null);
                    
                    jEditorPane1.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor));
                    jEditorPane2.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor));
                    jSplitPane1.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor));
                }
            };
            if (SwingUtilities.isEventDispatchThread()) {
                awtTask.run();
            } else {
                 SwingUtilities.invokeAndWait(awtTask);
            }
        } catch (InterruptedException e) {
            ErrorManager err = ErrorManager.getDefault();
            err.notify(e);
        } catch (InvocationTargetException e) {
            ErrorManager err = ErrorManager.getDefault();
            err.notify(e);
        }

        refreshDiffTask.run();
        
        manager = new DiffViewManager(this);
        manager.init();
    }

    /**
     * @return true if Move, Replace, Insert and Move All actions should be visible and enabled, false otherwise
     */
    public boolean isActionsEnabled() {
        return actionsEnabled;
    }
   
    private void initColors() {
        Lookup.Result<DiffVisualizer> dv = Lookup.getDefault().lookup(new Lookup.Template<DiffVisualizer>(DiffVisualizer.class));
        Collection c = dv.allInstances();
        for (Iterator i = c.iterator(); i.hasNext();) {
            Object o = i.next();
            if (o instanceof GraphicalDiffVisualizer) {
                GraphicalDiffVisualizer gdv = (GraphicalDiffVisualizer) o;
                colorAdded = gdv.getColorAdded();
                colorChanged = gdv.getColorChanged();
                colorMissing = gdv.getColorMissing();
            }
        }
    }

    public void ancestorAdded(AncestorEvent event) {
        expandFolds();
        initGlobalSizes();
        addChangeListeners();
        refreshDiff(50);        

        if (editableCookie == null) return;
        refreshEditableDocument();
        editableCookie.addPropertyChangeListener(this); 
    }

    private void refreshEditableDocument() {
        Document doc = null;
        try {
            doc = editableCookie.openDocument();
        } catch (IOException e) {
            Logger.getLogger(EditableDiffView.class.getName()).log(Level.INFO, "Getting new Document from EditorCookie", e); // NOI18N
            return;
        }
        editableDocument.removeDocumentListener(this);
        if (doc != editableDocument) {
            editableDocument = doc;
            jEditorPane2.getEditorPane().setDocument(editableDocument);
            refreshDiff(20);
        }
        editableDocument.addDocumentListener(this);
    }

    public void ancestorRemoved(AncestorEvent event) {
        if (editableCookie != null) {
            editableDocument.removeDocumentListener(this);
            saveModifiedDocument();
            editableCookie.removePropertyChangeListener(this);
            if (editableCookie.getOpenedPanes() == null) {
                editableCookie.close();
            }
        }
    }

    private void saveModifiedDocument() {
        DataObject dao = (DataObject) editableDocument.getProperty(Document.StreamDescriptionProperty);
        if (dao != null) {
            SaveCookie sc = dao.getCookie(SaveCookie.class);
            if (sc != null) {
                try {
                    sc.save();
                } catch (IOException e) {
                    Logger.getLogger(EditableDiffView.class.getName()).log(Level.INFO, "Error saving Diff document", e); // NOI18N
                }
            }
        }
    }

    public void ancestorMoved(AncestorEvent event) {
    }

    public void insertUpdate(DocumentEvent e) {
        refreshDiff(50);
    }

    public void removeUpdate(DocumentEvent e) {
        refreshDiff(50);
    }

    public void changedUpdate(DocumentEvent e) {
        refreshDiff(50);
    }
    
    Color getColor(Difference ad) {
        if (ad.getType() == Difference.ADD) return colorAdded;
        if (ad.getType() == Difference.CHANGE) return colorChanged;
        return colorMissing;
    }
    
    JComponent getMyDivider() {
        return spui.divider.getDivider();
    }

    DiffContentPanel getEditorPane1() {
        return jEditorPane1;
    }

    DiffContentPanel getEditorPane2() {
        return jEditorPane2;
    }

    public DiffViewManager getManager() {
        return manager;
    }

    Difference[] getDifferences() {
        return diffs;
    }

    /**
     * Rolls back a difference in the second document.
     * 
     * @param diff a difference to roll back, null to remove all differences
     */ 
    void rollback(Difference diff) {
        if (diff == null) {
            try {
                Document dest = getEditorPane2().getEditorPane().getDocument();
                Document src = getEditorPane1().getEditorPane().getDocument();
                dest.remove(0, dest.getLength());
                dest.insertString(0, src.getText(0, src.getLength()), null);
            } catch (BadLocationException e) {
                ErrorManager.getDefault().notify(e);
            }
            return;
        }
        try {
            Document document = getEditorPane2().getEditorPane().getDocument();
            if (diff.getType() == Difference.ADD) {
                int start = DiffViewManager.getRowStartFromLineOffset(document, diff.getSecondStart() - 1);
                int end = DiffViewManager.getRowStartFromLineOffset(document, diff.getSecondEnd());
                document.remove(start, end - start);
            } else if (diff.getType() == Difference.DELETE) {
                int start = DiffViewManager.getRowStartFromLineOffset(document, diff.getSecondStart());
                document.insertString(start, diff.getFirstText(), null);
            } else {
                int start = DiffViewManager.getRowStartFromLineOffset(document, diff.getSecondStart() - 1);
                int end = DiffViewManager.getRowStartFromLineOffset(document, diff.getSecondEnd());
                document.remove(start, end - start);
                document.insertString(start, diff.getFirstText(), null);
            }
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    class DiffSplitPaneUI extends BasicSplitPaneUI {

        final DiffSplitPaneDivider divider;

        public DiffSplitPaneUI(JSplitPane splitPane) {
            this.splitPane = splitPane;
            divider = new DiffSplitPaneDivider(this, EditableDiffView.this);
        }

        public BasicSplitPaneDivider createDefaultDivider() {
            return divider;
        }
    }
    
    public boolean requestFocusInWindow() {
        return jEditorPane1.requestFocusInWindow();
    }

    public JComponent getComponent() {
        return jSplitPane1;
    }

    public int getDifferenceCount() {
        return diffs.length;
    }

    public boolean canSetCurrentDifference() {
        return true;
    }

    public void setCurrentDifference(int diffNo) throws UnsupportedOperationException {
        if (diffNo < -1 || diffNo >= diffs.length) throw new IllegalArgumentException("Illegal difference number: " + diffNo); // NOI18N
        if (diffNo == -1) {
        } else {
            currentDiffIndex = diffNo;
            showCurrentLine();
        }
    }

    public int getCurrentDifference() throws UnsupportedOperationException {
        int firstVisibleLine;
        int lastVisibleLine;
        int candidate = currentDiffIndex;
        if (jViewport2 != null) {
            int viewHeight = jViewport2.getViewSize().height;
            java.awt.Point p1;
            initGlobalSizes(); // The window might be resized in the mean time.
            p1 = jViewport2.getViewPosition();
            int HALFLINE_CEILING = 2;  // compensation for rounding error and partially visible lines
            float firstPct = ((float)p1.y / (float)viewHeight);
            firstVisibleLine =  (int) (firstPct * totalLines) + HALFLINE_CEILING;
            float lastPct = ((float)(jViewport2.getHeight() + p1.y) / (float)viewHeight);
            lastVisibleLine = (int) (lastPct * totalLines) - HALFLINE_CEILING;

            for (int i = 0; i<diffs.length; i++) {
                int startLine = diffs[i].getSecondStart();
                int endLine = diffs[i].getSecondEnd();  // there no remove changes in right pane
                if (firstVisibleLine < startLine && startLine < lastVisibleLine
                || firstVisibleLine < endLine && endLine < lastVisibleLine) {
                    if (i == currentDiffIndex) {
                        return currentDiffIndex; // current is visible, eliminate hazards use it.
                    }
                    candidate = i;  // takes last visible, optimalized for Next>
                }
            }
        }

        return candidate;
    }

    public JToolBar getToolBar() {
        return null;
    }

    private void showCurrentLine() {
        Difference diff = diffs[currentDiffIndex];
        
        int off1, off2;
        initGlobalSizes(); // The window might be resized in the mean time.
        try {
            off1 = org.openide.text.NbDocument.findLineOffset((StyledDocument) jEditorPane1.getEditorPane().getDocument(), diff.getFirstStart() - 1);
            off2 = org.openide.text.NbDocument.findLineOffset((StyledDocument) jEditorPane2.getEditorPane().getDocument(), diff.getSecondStart() - 1);

            jEditorPane1.getEditorPane().setCaretPosition(off1);
            jEditorPane2.getEditorPane().setCaretPosition(off2);
        } catch (IndexOutOfBoundsException ex) {
            ErrorManager.getDefault().notify(ex);
        }

        // scroll the left pane accordingly
        manager.scroll();
    }
    
    /** This method is called from within the constructor to initialize the form.
     */
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jSplitPane1.setDividerSize(4);
        filePanel1.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        filePanel1.add(jEditorPane1, gridBagConstraints);

        fileLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        fileLabel1.setLabelFor(jEditorPane1);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        filePanel1.add(fileLabel1, gridBagConstraints);

        jSplitPane1.setLeftComponent(filePanel1);

        filePanel2.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        filePanel2.add(jEditorPane2, gridBagConstraints);

        fileLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        fileLabel2.setLabelFor(jEditorPane2);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        filePanel2.add(fileLabel2, gridBagConstraints);

        jSplitPane1.setRightComponent(filePanel2);
    }

    // Code for dispatching events from components to event handlers.

    private void expandFolds() {
        FoldHierarchy fh = FoldHierarchy.get(jEditorPane1.getEditorPane());
        FoldUtilities.expandAll(fh);
        fh = FoldHierarchy.get(jEditorPane2.getEditorPane());
        FoldUtilities.expandAll(fh);
    }

    private void initGlobalSizes() {
        StyledDocument doc1 = (StyledDocument) jEditorPane1.getEditorPane().getDocument();
        StyledDocument doc2 = (StyledDocument) jEditorPane2.getEditorPane().getDocument();
        int numLines1 = org.openide.text.NbDocument.findLineNumber(doc1, doc1.getEndPosition().getOffset());
        int numLines2 = org.openide.text.NbDocument.findLineNumber(doc2, doc2.getEndPosition().getOffset());

        int numLines = Math.max(numLines1, numLines2);
        if (numLines < 1) numLines = 1;
        this.totalLines = numLines;
        int totHeight = jEditorPane1.getSize().height;
        int value = jEditorPane2.getSize().height;
        if (value > totHeight) totHeight = value;
        this.totalHeight = totHeight;
    }

    // NestableDiffView implementation ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public void joinScrollPane(final JScrollPane pane) {
        jEditorPane1.getScrollPane().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        jEditorPane1.getScrollPane().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jEditorPane2.getScrollPane().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        jEditorPane2.getScrollPane().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jEditorPane1.getScrollPane().setWheelScrollingEnabled(false);
        jEditorPane2.getScrollPane().setWheelScrollingEnabled(false);
        jEditorPane1.getScrollPane().addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                pane.dispatchEvent(e);
            }
        });
        jEditorPane2.getScrollPane().addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                pane.dispatchEvent(e);
            }
        });
        jEditorPane1.getEditorPane().getCaret().setVisible(false);
        jEditorPane2.getEditorPane().getCaret().setVisible(false);

        // map JEditorPane keystroke to JScrollPane action registered for even keystroke
        KeyStroke[] keyStrokes = new KeyStroke[] {
            KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0),
            KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0),

            KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0),
            KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0),

            KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0),
            KeyStroke.getKeyStroke(KeyEvent.VK_HOME, InputEvent.CTRL_MASK),

            KeyStroke.getKeyStroke(KeyEvent.VK_HOME, InputEvent.CTRL_MASK),
            KeyStroke.getKeyStroke(KeyEvent.VK_HOME, InputEvent.CTRL_MASK),
            
            KeyStroke.getKeyStroke(KeyEvent.VK_END, 0),
            KeyStroke.getKeyStroke(KeyEvent.VK_END, InputEvent.CTRL_MASK),

            KeyStroke.getKeyStroke(KeyEvent.VK_END, InputEvent.CTRL_MASK),
            KeyStroke.getKeyStroke(KeyEvent.VK_END, InputEvent.CTRL_MASK),

            KeyStroke.getKeyStroke(KeyEvent.VK_END, 0),
            KeyStroke.getKeyStroke(KeyEvent.VK_END, 0),

            KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),
            KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0),

            KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),
            KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0),

            KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
            KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),

            KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
            KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
        };

        for (int i = 0; i<keyStrokes.length; i+=2) {
            KeyStroke stroke = keyStrokes[i];
            KeyStroke stroke2 = keyStrokes[i+1];
            Object pane1Key = jEditorPane1.getInputMap().get(stroke);
            Object pane2Key = jEditorPane2.getInputMap().get(stroke);
            Object scrollKey = pane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).get(stroke2);
            if (scrollKey != null) {
                final Action scrollAction = pane.getActionMap().get(scrollKey);
                jEditorPane1.getActionMap().put(pane1Key, new SourceTranslatorAction(scrollAction, pane));
                jEditorPane2.getActionMap().put(pane2Key, new SourceTranslatorAction(scrollAction, pane));
            } else {
                // System.err.println("No JScrollPane binding for " + stroke);  // HOME, END
            }
        }

    }

    public int getInnerScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return jEditorPane1.getEditorPane().getScrollableUnitIncrement(visibleRect, orientation, direction);
    }

    public int getInnerScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return jEditorPane1.getEditorPane().getScrollableBlockIncrement(visibleRect, orientation, direction);
    }
    
    public int getInnerWidth() {
        Dimension d1 = jEditorPane1.getScrollPane().getViewport().getView().getPreferredSize();
        Dimension d2 = jEditorPane2.getScrollPane().getViewport().getView().getPreferredSize();
        return Math.max(d1.width, d2.width) * 2;
    }

    public void setInnerWidth(int width) {
        Dimension dim = jEditorPane1.getScrollPane().getViewport().getViewSize();
        dim.width = width/2;
        jEditorPane1.getScrollPane().getViewport().setViewSize(dim);

        dim = jEditorPane2.getScrollPane().getViewport().getViewSize();
        dim.width = width/2;
        jEditorPane2.getScrollPane().getViewport().setViewSize(dim);

        jSplitPane1.setDividerLocation(0.5);
    }

    public void setHorizontalPosition(int pos) {
        pos /= 2;

        Point p = jEditorPane1.getScrollPane().getViewport().getViewPosition();
        p.x =  pos;
        jEditorPane1.getScrollPane().getViewport().setViewPosition(p);

        p = jEditorPane2.getScrollPane().getViewport().getViewPosition();
        p.x =  pos;
        jEditorPane2.getScrollPane().getViewport().setViewPosition(p);
    }

    /** Return change's top y-axis position. */
    public int getChangeY(int change) {
        Difference diff = diffs[change];
        int line = diff.getFirstStart();
        int padding = 5;
        if (line <= 5) {
            padding = line/2;
        }
        initGlobalSizes();
        int ypos = (totalHeight*(line - padding - 1))/(totalLines + 1);
        ypos += fileLabel1.getHeight();
        return ypos;
    }

    private void joinScrollBars() {
        final JScrollBar scrollBarH1 = jEditorPane1.getScrollPane().getHorizontalScrollBar();
        final JScrollBar scrollBarH2 = jEditorPane2.getScrollPane().getHorizontalScrollBar();

        scrollBarH1.getModel().addChangeListener(new javax.swing.event.ChangeListener()  {
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                int value = scrollBarH1.getValue();
                if (value == horizontalScroll1ChangedValue) return;
                int max1 = scrollBarH1.getMaximum();
                int max2 = scrollBarH2.getMaximum();
                int ext1 = scrollBarH1.getModel().getExtent();
                int ext2 = scrollBarH2.getModel().getExtent();
                if (max1 == ext1) horizontalScroll2ChangedValue = 0;
                else horizontalScroll2ChangedValue = (value*(max2 - ext2))/(max1 - ext1);
                horizontalScroll1ChangedValue = -1;
                scrollBarH2.setValue(horizontalScroll2ChangedValue);
            }
        });
        scrollBarH2.getModel().addChangeListener(new javax.swing.event.ChangeListener()  {
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                int value = scrollBarH2.getValue();
                if (value == horizontalScroll2ChangedValue) return;
                int max1 = scrollBarH1.getMaximum();
                int max2 = scrollBarH2.getMaximum();
                int ext1 = scrollBarH1.getModel().getExtent();
                int ext2 = scrollBarH2.getModel().getExtent();
                if (max2 == ext2) horizontalScroll1ChangedValue = 0;
                else horizontalScroll1ChangedValue = (value*(max1 - ext1))/(max2 - ext2);
                horizontalScroll2ChangedValue = -1;
                scrollBarH1.setValue(horizontalScroll1ChangedValue);
            }
        });
        jSplitPane1.setDividerLocation(0.5);
    }

    private void customizeEditor(JEditorPane editor) {
        StyledDocument doc;
        Document document = editor.getDocument();
        try {
            doc = (StyledDocument) editor.getDocument();
        } catch(ClassCastException e) {
            doc = new DefaultStyledDocument();
            try {
                doc.insertString(0, document.getText(0, document.getLength()), null);
            } catch (BadLocationException ble) {
                // leaving the document empty
            }
            editor.setDocument(doc);
        }
    }
    
    private void addChangeListeners() {
        jEditorPane1.getEditorPane().addPropertyChangeListener("font", new java.beans.PropertyChangeListener() { // NOI18N
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        diffSerial++;   // we need to re-compute decorations, font size changed
                        initGlobalSizes();
                        jEditorPane1.onUISettingsChanged();
                        getComponent().revalidate();
                        getComponent().repaint();
                    }
                });
            }
        });
        jEditorPane2.getEditorPane().addPropertyChangeListener("font", new java.beans.PropertyChangeListener() { // NOI18N
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        diffSerial++;   // we need to re-compute decorations, font size changed
                        initGlobalSizes();
                        jEditorPane2.onUISettingsChanged();                        
                        getComponent().revalidate();
                        getComponent().repaint();
                    }
                });
            }
        });
    }

    private void setSource1(StreamSource ss) throws IOException {
        firstSourceAvailable = false; 
        EditorKit kit = jEditorPane1.getEditorPane().getEditorKit();
        if (kit == null) throw new IOException("Missing Editor Kit"); // NOI18N

        Document sdoc = getSourceDocument(ss);
        Document doc = sdoc != null ? sdoc : kit.createDefaultDocument();
        if (!(doc instanceof StyledDocument)) {
            doc = new DefaultStyledDocument(new StyleContext());
            kit = new StyledEditorKit();
            jEditorPane1.getEditorPane().setEditorKit(kit);
        }
        if (sdoc == null) {
            Reader r = ss.createReader();
            if (r != null) {
                firstSourceAvailable = true;
                try {
                    kit.read(r, doc, 0);
                } catch (javax.swing.text.BadLocationException e) {
                    throw new IOException("Can not locate the beginning of the document."); // NOI18N
                } finally {
                    r.close();
                }
            }
        } else {
            firstSourceAvailable = true;
        }
        jEditorPane1.initActions();        
        jEditorPane1.getEditorPane().setDocument(doc);
        customizeEditor(jEditorPane1.getEditorPane());
        jViewport2 = jEditorPane2.getScrollPane().getViewport();
    }
    
    private Document getSourceDocument(StreamSource ss) {
        Document sdoc = null;
        FileObject fo = ss.getLookup().lookup(FileObject.class);
        if (fo != null) {
            try {
                DataObject dao = DataObject.find(fo);
                EditorCookie ec = dao.getCookie(EditorCookie.class);
                if (ec != null) {
                    sdoc = ec.openDocument();
                }
            } catch (Exception e) {
                // fallback to other means of obtaining the source
            }
        } else {
            sdoc = ss.getLookup().lookup(Document.class);
        }
        return sdoc;
    }

    private void setSource2(StreamSource ss) throws IOException {
        secondSourceAvailable = false;         
        EditorKit kit = jEditorPane2.getEditorPane().getEditorKit();
        if (kit == null) throw new IOException("Missing Editor Kit"); // NOI18N
        
        Document sdoc = getSourceDocument(ss);
        if (sdoc != null && ss.isEditable()) {
            DataObject dao = (DataObject) sdoc.getProperty(Document.StreamDescriptionProperty);
            if (dao != null) {
                EditorCookie cookie = dao.getCookie(EditorCookie.class);
                if (cookie instanceof EditorCookie.Observable) {
                    editableCookie = (EditorCookie.Observable) cookie;
                    editableDocument = sdoc;
                    editorUndoRedo = getUndoRedo(cookie);
                }
            }
        }
        Document doc = sdoc != null ? sdoc : kit.createDefaultDocument();
        if (!(doc instanceof StyledDocument)) {
            doc = new DefaultStyledDocument(new StyleContext());
            kit = new StyledEditorKit();
            jEditorPane2.getEditorPane().setEditorKit(kit);
        }
        if (sdoc == null) {
            Reader r = ss.createReader();
            if (r != null) {
                secondSourceAvailable = true;
                try {
                    kit.read(r, doc, 0);
                } catch (javax.swing.text.BadLocationException e) {
                    throw new IOException("Can not locate the beginning of the document."); // NOI18N
                } finally {
                    r.close();
                }
            }
        } else {
            secondSourceAvailable = true;
        }
        jEditorPane2.initActions();
        jSplitPane1.putClientProperty(UndoRedo.class, editorUndoRedo);
        jEditorPane2.getEditorPane().setDocument(doc);
        jEditorPane2.getEditorPane().setEditable(editableCookie != null);
        if (jEditorPane2.getEditorPane().isEditable()) {
            jEditorPane1.getEditorPane().setBackground(COLOR_READONLY_BG);
        }
        
        customizeEditor(jEditorPane2.getEditorPane());
        joinScrollBars();
    }
    
    private UndoRedo.Manager getUndoRedo(EditorCookie cookie) {
        // TODO: working around #96543 
        try {
            Method method = CloneableEditorSupport.class.getDeclaredMethod("getUndoRedo"); // NOI18N
            method.setAccessible(true);
            return (UndoRedo.Manager) method.invoke(cookie);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void propertyChange(final PropertyChangeEvent evt) {
        if (EditorCookie.Observable.PROP_DOCUMENT.equals(evt.getPropertyName())) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    refreshEditableDocument();
                }
            }); 
        }
    }

    public void setSourceTitle(JLabel label, String title) {
        label.setText(title);
        // Set the minimum size in 'x' direction to a low value, so that the splitter can be moved to corner locations
        label.setMinimumSize(new Dimension(3, label.getMinimumSize().height));
    }
    
    public void setDocument1(Document doc) {
        if (doc != null) {
            jEditorPane1.getEditorPane().setDocument(doc);
        }
    }
    
    public void setDocument2(Document doc) {
        if (doc != null) {
            jEditorPane2.getEditorPane().setDocument(doc);
        }
    }

    private void refreshDiff(int delayMillis) {
        refreshDiffTask.schedule(delayMillis);
    }

    public class RefreshDiffTask implements Runnable {

        public void run() {
            synchronized(EditableDiffView.this) {
                computeDiff();
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        jEditorPane1.setCurrentDiff(diffs);
                        jEditorPane2.setCurrentDiff(diffs);
                        jSplitPane1.repaint();
                    }
                });
            }
        }

        private boolean equals(Difference[] a, Difference[] b) {
            if (a == null || b == null || a.length != b.length) return false;
            for (int i = 0; i < a.length; i++) {
                Difference ad = a[i];
                Difference bd = b[i];
                if (ad.getType() != bd.getType() ||
                        ad.getFirstStart() != bd.getFirstStart() ||
                        ad.getSecondStart() != bd.getSecondStart() ||
                        ad.getFirstEnd() != bd.getFirstEnd() ||
                        ad.getSecondEnd() != bd.getSecondEnd()) return false;
            }
            return true;
        }

        private void computeDiff() {
            if (!secondSourceAvailable || !firstSourceAvailable) {
                diffs = NO_DIFFERENCES;
                return;
            }
            
            Reader first = null;
            Reader second = null;
            try {
                first = new StringReader(jEditorPane1.getEditorPane().getDocument().getText(0, jEditorPane1.getEditorPane().getDocument().getLength()));
                second = new StringReader(jEditorPane2.getEditorPane().getDocument().getText(0, jEditorPane2.getEditorPane().getDocument().getLength()));
            } catch (BadLocationException e) {
                ErrorManager.getDefault().notify(e);
            }

            DiffProvider diff = Lookup.getDefault().lookup(DiffProvider.class);
            if (diff == null) {
                diffs = NO_DIFFERENCES;
                return;
            }
            boolean isTrim = false;
            if (diff instanceof BuiltInDiffProvider) {
                isTrim = ((BuiltInDiffProvider) diff).isTrimLines();
                ((BuiltInDiffProvider) diff).setTrimLines(false);
            }
            try {
                diffs = diff.computeDiff(first, second);
                diffSerial++;
            } catch (IOException e) {
                diffs = NO_DIFFERENCES;
            }
            if (diff instanceof BuiltInDiffProvider) {
                ((BuiltInDiffProvider) diff).setTrimLines(isTrim);
            }
        }
    }
    
    int getDiffSerial() {
        return diffSerial;
    }

    static Difference getFirstDifference(Difference [] diff, int line) {
        if (line < 0) return null;
        for (int i = 0; i < diff.length; i++) {
            Difference difference = diff[i];
            if (line < difference.getFirstStart()) return null;
            if (difference.getType() == Difference.ADD && line == difference.getFirstStart()) return difference;
            if (line <= difference.getFirstEnd()) return difference;
        }
        return null;
    }

    static Difference getSecondDifference(Difference [] diff, int line) {
        if (line < 0) return null;
        for (int i = 0; i < diff.length; i++) {
            Difference difference = diff[i];
            if (line < difference.getSecondStart()) return null;
            if (difference.getType() == Difference.DELETE && line == difference.getSecondStart()) return difference;
            if (line <= difference.getSecondEnd()) return difference;
        }
        return null;
    }
    
    Color getColorLines() {
        return colorLines;
    }

    private PropertyChangeSupport support = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener l) {
        support.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        support.removePropertyChangeListener(l);
    }
}
