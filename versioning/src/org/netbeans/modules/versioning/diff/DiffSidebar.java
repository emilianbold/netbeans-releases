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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.versioning.diff;

import org.netbeans.editor.*;
import org.netbeans.editor.Utilities;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldHierarchyListener;
import org.netbeans.api.editor.fold.FoldHierarchyEvent;
import org.netbeans.api.diff.Difference;
import org.netbeans.spi.diff.DiffProvider;
import org.netbeans.modules.diff.builtin.provider.BuiltInDiffProvider;
import org.netbeans.modules.diff.EncodedReaderFactory;
import org.netbeans.modules.editor.errorstripe.privatespi.MarkProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.text.*;
import java.awt.event.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.*;
import java.util.List;
import java.io.*;

/**
 * Left editor sidebar showing changes in the file against the base version.
 * 
 * @author Maros Sandor
 */
class DiffSidebar extends JComponent implements DocumentListener, ComponentListener, PropertyChangeListener, FoldHierarchyListener {
    
    private static final int BAR_WIDTH = 9;
    
    private final JTextComponent  textComponent;

    private final EditorUI        editorUI;
    private final FoldHierarchy   foldHierarchy;
    private final BaseDocument    document;
    
    private boolean                 annotated;
    private Difference []           currentDiff;
    private DiffMarkProvider        markProvider;

    private Color colorAdded =      new Color(150, 255, 150);
    private Color colorChanged =    new Color(150, 150, 255);
    private Color colorRemoved =    new Color(255, 160, 180);
    private Color colorBorder =     new Color(102, 102, 102);
    
    private final DiffSidebarProvider.OriginalContent originalContent;
    private String                              originalContentBuffer;
    
    private RequestProcessor.Task   refreshDiffTask;

    public DiffSidebar(JTextComponent target, DiffSidebarProvider.OriginalContent content) {
        this.textComponent = target;
        this.originalContent = content;
        this.editorUI = Utilities.getEditorUI(target);
        this.foldHierarchy = FoldHierarchy.get(editorUI.getComponent());
        this.document = editorUI.getDocument();
        this.markProvider = new DiffMarkProvider();
        setToolTipText("");
        refreshDiffTask = RequestProcessor.getDefault().create(new RefreshDiffTask());
        setMaximumSize(new Dimension(BAR_WIDTH, Integer.MAX_VALUE));
    }

    public String getToolTipText(MouseEvent event) {
        Difference diff = getDifferenceAt(event);
        return getShortDescription(diff);
    }

    static String getShortDescription(Difference diff) {
        if (diff == null) return null;
        switch (diff.getType()) {
            case Difference.ADD:
                return diff.getSecondEnd() - diff.getSecondStart() + 1 + " lines added";
            case Difference.CHANGE:
                return diff.getFirstEnd() - diff.getFirstStart() + 1 + " lines changed";
            case Difference.DELETE:
                return diff.getFirstEnd() - diff.getFirstStart() + 1 + " lines deleted";
            default:
                throw new IllegalStateException("Unknown difference type: " + diff.getType());
        }
    }

    protected void processMouseEvent(MouseEvent event) {
        super.processMouseEvent(event);
        if (event.isPopupTrigger()) {
            Difference diff = getDifferenceAt(event);
            if (diff == null) return;
            showPopup(event, diff);
        }
    }
    
    private Difference getDifferenceAt(MouseEvent event) {
        if (currentDiff == null) return null;
        int line = getLineFromMouseEvent(event);
        if (line == -1) return null;
        Difference diff = getDifference(line + 1);
        if (diff == null) {
            // delete annotations (arrows) are rendered between lines
            diff = getDifference(line);
            if (diff != null && diff.getType() != Difference.DELETE) diff = null;
        }
        return diff;
    }

    private void showPopup(MouseEvent event, final Difference diff) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem item = new JMenuItem("Rollback");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rollback(diff);
            }
        });
        menu.add(item);

        menu.show(event.getComponent(), event.getX(), event.getY());
    }

    private void rollback(Difference diff) {
        try {
            if (diff.getType() == Difference.ADD) {
                int start = Utilities.getRowStartFromLineOffset(document, diff.getSecondStart() - 1);
                int end = Utilities.getRowStartFromLineOffset(document, diff.getSecondEnd());
                document.remove(start, end - start);
            } else if (diff.getType() == Difference.CHANGE) {
                int start = Utilities.getRowStartFromLineOffset(document, diff.getSecondStart() - 1);
                int end = Utilities.getRowStartFromLineOffset(document, diff.getSecondEnd());
                document.replace(start, end - start, diff.getFirstText(), null);
            } else {
                int start = Utilities.getRowStartFromLineOffset(document, diff.getSecondStart());
                document.insertString(start, diff.getFirstText(), null);
            }
            refreshDiff();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private int getLineFromMouseEvent(MouseEvent e){
        int line = -1;
        if (editorUI != null) {
            try{
                JTextComponent component = editorUI.getComponent();
                BaseTextUI textUI = (BaseTextUI)component.getUI();
                int clickOffset = textUI.viewToModel(component, new Point(0, e.getY()));
                line = Utilities.getLineOffset(document, clickOffset);
            }catch (BadLocationException ble){
            }
        }
        return line;
    }

    public void setSidebarVisible(boolean visible) {
        if (visible) {
            showSidebar();
        } else {
            hideSidebar();
        }
    }

    private void showSidebar() {
        annotated = true;

        document.addDocumentListener(this);
        textComponent.addComponentListener(this);
        editorUI.addPropertyChangeListener(this);
        foldHierarchy.addFoldHierarchyListener(this);
        originalContent.addPropertyChangeListener(this);

        refreshDiff();
        revalidate();  // resize the component
    }

    private void hideSidebar() {
        annotated = false;

        originalContent.removePropertyChangeListener(this);
        foldHierarchy.removeFoldHierarchyListener(this);
        editorUI.removePropertyChangeListener(this);
        textComponent.removeComponentListener(this);
        document.removeDocumentListener(this);

        refreshDiff();
        revalidate();
    }

    private Reader getDocumentReader() {
        JTextComponent component = editorUI.getComponent();
        if (component == null) return null;

        Document doc = component.getDocument();
        try {
            return new StringReader(doc.getText(0, doc.getLength()));
        } catch (BadLocationException e) {
            return null;
        }
    }
    
    private void refreshDiff() {
        refreshDiffTask.schedule(50);
    }
        
    MarkProvider getMarkProvider() {
        return markProvider;
    }

    private static void copyStreamsCloseAll(Writer writer, Reader reader) throws IOException {
        char [] buffer = new char[2048];
        int n;
        while ((n = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, n);
        }
        writer.close();
        reader.close();
    }

    public Dimension getPreferredSize() {
        Dimension dim = textComponent.getSize();
        dim.width = annotated ? BAR_WIDTH : 0;
        return dim;
    }
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Rectangle clip = g.getClipBounds();

        JTextComponent component = editorUI.getComponent();
        if (component == null) return;

        BaseTextUI textUI = (BaseTextUI)component.getUI();
        View rootView = Utilities.getDocumentView(component);
        if (rootView == null) return;

        g.setColor(backgroundColor());
        g.fillRect(clip.x, clip.y, clip.width, clip.height);
        if (currentDiff == null || currentDiff.length == 0) return;
        
        AbstractDocument doc = (AbstractDocument)component.getDocument();
        doc.readLock();
        try{
            foldHierarchy.lock();
            try{
                int startPos = textUI.getPosFromY(clip.y);
                int startViewIndex = rootView.getViewIndex(startPos,Position.Bias.Forward);
                int rootViewCount = rootView.getViewCount();

                if (startViewIndex >= 0 && startViewIndex < rootViewCount) {
                    // find the nearest visible line with an annotation
                    Rectangle rec = textUI.modelToView(component, rootView.getView(startViewIndex).getStartOffset());
                    int y = (rec == null) ? 0 : rec.y;
                    int [] yCoords = new int[3];

                    int clipEndY = clip.y + clip.height;
                    Element rootElem = textUI.getRootView(component).getElement();

                    View view = rootView.getView(startViewIndex);
                    int line = rootElem.getElementIndex(view.getStartOffset());
                    line++; // make it 1-based
                    if (line == 1 && currentDiff[0].getSecondStart() == 0 && currentDiff[0].getType() == Difference.DELETE) {
                        g.setColor(getColor(currentDiff[0]));
                        yCoords[0] = y - editorUI.getLineAscent() / 2;
                        yCoords[1] = y;
                        yCoords[2] = y + editorUI.getLineAscent() / 2;
                        g.fillPolygon(new int [] { 0, BAR_WIDTH, 0 }, yCoords, 3);
                    }
                    
                    for (int i = startViewIndex; i < rootViewCount; i++){
                        view = rootView.getView(i);
                        line = rootElem.getElementIndex(view.getStartOffset());
                        line++; // make it 1-based
                        Difference ad = getDifference(line);
                        if (ad != null) {
                            g.setColor(getColor(ad));
                            if (ad.getType() == Difference.DELETE) {
                                yCoords[0] = y + editorUI.getLineAscent();
                                yCoords[1] = y + editorUI.getLineAscent() * 3 / 2;
                                yCoords[2] = y + editorUI.getLineAscent() * 2 - 1;
                                g.fillPolygon(new int [] { 2, BAR_WIDTH, 2 }, yCoords, 3);
                                g.setColor(colorBorder);
                                g.drawLine(2, yCoords[0], 2, yCoords[2]);
                            } else {
                                g.fillRect(3, y, BAR_WIDTH - 3, editorUI.getLineHeight());
                                g.setColor(colorBorder);
                                g.drawLine(2, y, 2, y + editorUI.getLineHeight());
                                if (ad.getSecondStart() == line) {
                                    g.drawLine(2, y, BAR_WIDTH - 1, y);
                                }
                                if (ad.getSecondEnd() == line) {
                                    g.drawLine(2, y + editorUI.getLineHeight(), BAR_WIDTH - 1, y + editorUI.getLineHeight());
                                }
                            }
                        }
                        y += editorUI.getLineHeight();
                        if (y >= clipEndY) {
                            break;
                        }
                    }
                }
            } finally {
                foldHierarchy.unlock();
            }
        } catch (BadLocationException ble){
            ErrorManager.getDefault().notify(ble);
        } finally {
            doc.readUnlock();
        }
    }

    private Color getColor(Difference ad) {
        if (ad.getType() == Difference.ADD) return colorAdded;
        if (ad.getType() == Difference.CHANGE) return colorChanged;
        return colorRemoved;
    }

    private Difference getDifference(int line) {
        if (line < 0) return null;
        for (int i = 0; i < currentDiff.length; i++) {
            Difference difference = currentDiff[i];
            if (line < difference.getSecondStart()) return null;
            if (difference.getType() == Difference.DELETE && line == difference.getSecondStart()) return difference;
            if (line <= difference.getSecondEnd()) return difference;
        }
        return null;
    }

    private Color backgroundColor() {
        if (textComponent != null) {
            return textComponent.getBackground();
        }

        return Color.WHITE;
    }

    public void insertUpdate(DocumentEvent e) {
        refreshDiff();
    }

    public void removeUpdate(DocumentEvent e) {
        refreshDiff();
    }

    public void changedUpdate(DocumentEvent e) {
        refreshDiff();
    }

    public void componentResized(ComponentEvent e) {
        revalidate();
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentShown(ComponentEvent e) {
    }

    public void componentHidden(ComponentEvent e) {
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String id = evt.getPropertyName();
        if (EditorUI.COMPONENT_PROPERTY.equals(id)) {  // NOI18N
            if (evt.getNewValue() == null){
                hideSidebar();
            }
        } else if (DiffSidebarProvider.OriginalContent.PROP_CONTENT_CHANGED.equals(id)) {
            synchronized(this) {
                originalContentBuffer = null;
                refreshDiff();
            }
        }
    }

    public void foldHierarchyChanged(FoldHierarchyEvent evt) {
        repaint();
    }

    /**
     * Integration provider for the error stripe.
     */
    private class DiffMarkProvider extends MarkProvider {

        private List<DiffMark> marks;

        public DiffMarkProvider() {
            marks = getMarksForDifferences();
        }

        public List getMarks() {
            return marks;
        }

        void refresh() {
            List<DiffMark> oldMarks = marks;
            marks = getMarksForDifferences();
            firePropertyChange(PROP_MARKS, oldMarks, marks);
        }

        private List<DiffMark> getMarksForDifferences() {
            if (currentDiff == null) return Collections.emptyList();
            List<DiffMark> marks = new ArrayList<DiffMark>(currentDiff.length);
            for (int i = 0; i < currentDiff.length; i++) {
                Difference difference = currentDiff[i];
                marks.add(new DiffMark(difference, getColor(difference)));
            }
            return marks;
        }
    }

    /**
     * RP task to compute new diff after a change in the document or a change in the base text.
     */
    public class RefreshDiffTask implements Runnable {

        public void run() {
            synchronized(DiffSidebar.this) {
                computeDiff();
                repaint();
                markProvider.refresh();
            }
        }

        private void computeDiff() {
            if (!annotated) {
                currentDiff = null;
                return;
            }
            fetchOriginalContent();
            if (originalContentBuffer == null) {
                currentDiff = null;
                return;
            }
            Reader working = getDocumentReader();
            if (working == null) {
                // TODO: what to do in this case? let's keep the old dirrerence set for now
                return;
            }
            DiffProvider diff = Lookup.getDefault().lookup(DiffProvider.class);
            if (diff == null) {
                currentDiff = null;
                return;
            }
            boolean isTrim = false;
            if (diff instanceof BuiltInDiffProvider) {
                isTrim = ((BuiltInDiffProvider) diff).isTrimLines();
                ((BuiltInDiffProvider) diff).setTrimLines(false);
            }
            try {
                currentDiff = diff.computeDiff(new StringReader(originalContentBuffer), working);
            } catch (IOException e) {
                currentDiff = null;
            }
            if (diff instanceof BuiltInDiffProvider) {
                ((BuiltInDiffProvider) diff).setTrimLines(isTrim);
            }
        }

        private void fetchOriginalContent() {
            if (originalContentBuffer != null) return;
            File file = originalContent.getOriginalFile();
            if (file == null) return;

            FileObject fo = FileUtil.toFileObject(file);
            if (fo == null) return;
            
            StringWriter w = new StringWriter(2048);
            try {
                Reader original = EncodedReaderFactory.getDefault().getReader(file, fo.getMIMEType());
                copyStreamsCloseAll(w, original);
                originalContentBuffer = w.toString();
            } catch (IOException e) {
                // ignore, we will show no diff
            }
        }
    }
}
