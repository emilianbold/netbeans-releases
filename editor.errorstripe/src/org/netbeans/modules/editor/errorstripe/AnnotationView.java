/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.errorstripe;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.View;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldHierarchyEvent;
import org.netbeans.api.editor.fold.FoldHierarchyListener;

import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.errorstripe.spi.Mark;
import org.netbeans.modules.editor.errorstripe.spi.MarkProvider;
import org.netbeans.modules.editor.errorstripe.spi.MarkProviderCreator;
import org.openide.ErrorManager;
import org.netbeans.modules.editor.errorstripe.spi.Status;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.FolderLookup;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.Lookup.Template;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Jan Lahoda
 */
public class AnnotationView extends JComponent implements FoldHierarchyListener, MouseListener, MouseMotionListener, PropertyChangeListener, DocumentListener {
    
    /*package private*/ static final ErrorManager ERR = ErrorManager.getDefault().getInstance("org.netbeans.modules.editor.errorstripe.AnnotationView"); // NOI18N
    
    private static final int STATUS_BOX_SIZE = 7;
    private static final int THICKNESS = STATUS_BOX_SIZE + 6;
    /*package private*/ static final int PIXELS_FOR_LINE = 2/*height / lines*/;
    /*package private*/ static final int LINE_SEPARATOR_SIZE = 0/*2*/;
    /*package private*/ static final int HEIGHT_OFFSET = 20;
    /*package private*/ static final int HEIGHT_LOWER_OFFSET = 10;
    
    /*package private*/ static final int UPPER_HANDLE = 4;
    /*package private*/ static final int LOWER_HANDLE = 4;
    
    private BaseDocument doc;
    private JTextComponent  pane;
    
    private static final Color STATUS_UP_PART_COLOR = Color.WHITE;
    private static final Color STATUS_DOWN_PART_COLOR = new Color(180, 180, 180);
    
    private List/*<MarkProviderCreator>*/ creators;
    private List/*<MarkProvider>*/ providers;
    
    private List/*<Mark>*/ currentMarks = null;
    private SortedMap/*<Mark>*/ marksMap = null;
    
    public AnnotationView(JTextComponent pane) {
        this(pane, null);
    }
    
    /** Creates a new instance of AnnotationViewBorder */
    public AnnotationView(JTextComponent pane, List/*<MarkProviderCreator>*/ creators) {
        this.pane = pane;
        this.creators = creators;
        
        FoldHierarchy.get(pane).addFoldHierarchyListener(this);
        
        pane.addPropertyChangeListener(this);
        
        updateForNewDocument();
        
        addMouseListener(this);
        addMouseMotionListener(this);
        
        setOpaque(true);
        
        setToolTipText(org.openide.util.NbBundle.getBundle(AnnotationView.class).getString("TP_ErrorStripe"));
    }
    
    private synchronized void updateForNewDocument() {
        Document newDocument = pane.getDocument();
        
        if (!(newDocument instanceof BaseDocument)) {
            this.doc = null;
        } else {
            this.doc = (BaseDocument) pane.getDocument();
            this.doc.addDocumentListener(this);
        }
            
        if (this.creators == null) {
            this.providers = gatherProviders(pane);
        } else {
            List providers = new ArrayList();
            
            for (Iterator c = creators.iterator(); c.hasNext(); ) {
                MarkProviderCreator creator = (MarkProviderCreator) c.next();
                MarkProvider provider = creator.createMarkProvider(this.doc);
                
                if (provider != null)
                    providers.add(provider);
            }
            
            this.providers = providers;
        }
        
        addListenersToProviders();
    }
    
    private static List gatherProviders(JTextComponent pane) {
        try {
            List result = new ArrayList();
            BaseKit kit = Utilities.getKit(pane);
            
            if (kit == null)
                return Collections.EMPTY_LIST;
            
            String content = kit.getContentType();
            BaseDocument document = (BaseDocument) pane.getDocument();
            FileObject baseFolder = Repository.getDefault().getDefaultFileSystem().findResource("Editors/text/base/Services");
            FileObject contentFolder = Repository.getDefault().getDefaultFileSystem().findResource("Editors/" + content + "/Services");
            
            if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ERR.log(ErrorManager.INFORMATIONAL, "baseFolder = " + baseFolder );
            }
            
            DataObject baseDO = baseFolder != null ? DataObject.find(baseFolder) : null;
            
            if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ERR.log(ErrorManager.INFORMATIONAL, "baseDO = " + baseDO );
            }
            
            Lookup baseLookup = baseFolder != null ? new FolderLookup((DataFolder) baseDO).getLookup() : Lookup.EMPTY;
            
            if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ERR.log(ErrorManager.INFORMATIONAL, "contentFolder = " + contentFolder );
            }
            
            DataObject contentDO = contentFolder != null ? DataObject.find(contentFolder) : null;
            Lookup contentLookup = contentFolder != null ? new FolderLookup((DataFolder) contentDO).getLookup() : Lookup.EMPTY;
            
            Lookup lookup = new ProxyLookup(new Lookup[] {baseLookup, contentLookup});
            
            Result creators = lookup.lookup(new Template(MarkProviderCreator.class));
            
            for (Iterator i = creators.allInstances().iterator(); i.hasNext(); ) {
                MarkProviderCreator creator = (MarkProviderCreator) i.next();
                
                if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                    ERR.log(ErrorManager.INFORMATIONAL, "creator = " + creator );
                }
                
                MarkProvider provider = creator.createMarkProvider(document);
                
                if (provider != null)
                    result.add(provider);
            }
            
            return result;
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
            return Collections.EMPTY_LIST;
        }
    }
    
    //TODO: remove after not used
    private void addListenersToProviders() {
        for (Iterator p = providers.iterator(); p.hasNext(); ) {
            MarkProvider provider = (MarkProvider) p.next();
            
            SPIAccessor.getDefault().addPropertyChangeListener(provider, this);
        }
    }
    
    /*package private*/ static List/*<Mark>*/ createMergedMarks(List/*<MarkProvider>*/ providers) {
        List result = new ArrayList();
        
        for (Iterator p = providers.iterator(); p.hasNext(); ) {
            MarkProvider provider = (MarkProvider) p.next();
            
            result.addAll(provider.getMarks());
        }
        
        return result;
    }
    
    /*package private for tests*/synchronized List/*<Mark>*/ getMergedMarks() {
        if (currentMarks == null) {
            currentMarks = createMergedMarks(providers);
        }
        
        return currentMarks;
    }
    
    /*package private*/ static List/*<Mark>*/ getStatusesForLineImpl(int line, SortedMap marks) {
        List inside = (List) marks.get(new Integer(line));
        
        if (inside == null)
            return Collections.EMPTY_LIST;
        
        return inside;
    }
    
    /*package private*/ Mark getMainMarkForBlock(int startLine, int endLine) {
        return getMainMarkForBlockImpl(startLine, endLine, getMarkMap());
    }
    
    /*package private*/ static Mark getMainMarkForBlockImpl(int startLine, int endLine, SortedMap marks) {
        int current = startLine - 1;
        Mark found = null;
        
        while ((current = findNextUsedLine(current, marks)) != Integer.MAX_VALUE && current <= endLine) {
            for (Iterator i = getStatusesForLineImpl(/*doc, */current, marks).iterator(); i.hasNext(); ) {
                Mark newMark = (Mark) i.next();
                
                if (found == null || newMark.getStatus().compareTo(found.getStatus()) > 0) {
                    found = newMark;
                }
            }
            current++;
        }
        
        return found;
    }
    
    /*package private for tests*/int[] getLinesSpan(int currentLine) {
        double position  = modelToView(currentLine);
        
        if (position == (-1))
            return new int[] {currentLine, currentLine};
            
        int    startLine = currentLine;
        int    endLine   = currentLine;
        
        while (position == modelToView(startLine - 1))//TODO startLine > 0
            startLine--;
        
        while (position == modelToView(endLine + 1))//TODO endLine < line count
            endLine++;
        
        return new int[] {startLine, endLine};
    }
    
    private void drawOneColorGlobalStatus(Graphics g, Color color) {
        g.setColor(color);
        
        int x = (THICKNESS - STATUS_BOX_SIZE) / 2;
        int y = (HEIGHT_OFFSET - STATUS_BOX_SIZE) / 2;
        
        g.fillRect(x, y, STATUS_BOX_SIZE, STATUS_BOX_SIZE);
        
        g.setColor(STATUS_DOWN_PART_COLOR);
        
        g.drawLine(x - 1, y - 1, x + STATUS_BOX_SIZE, y - 1              );
        g.drawLine(x - 1, y - 1, x - 1,               y + STATUS_BOX_SIZE);
        
        g.setColor(STATUS_UP_PART_COLOR);
        
        g.drawLine(x - 1,               y + STATUS_BOX_SIZE, x + STATUS_BOX_SIZE, y + STATUS_BOX_SIZE);
        g.drawLine(x + STATUS_BOX_SIZE, y - 1,               x + STATUS_BOX_SIZE, y + STATUS_BOX_SIZE);
    }
    
    private void drawInProgressGlobalStatus(Graphics g, Color color) {
        drawOneColorGlobalStatus(g, color);
        
        g.setColor(Color.BLACK);
        
        int x = (THICKNESS - STATUS_BOX_SIZE) / 2;
        int y = (HEIGHT_OFFSET - STATUS_BOX_SIZE) / 2;
        
        g.drawString("*", x + 1, y + STATUS_BOX_SIZE * 3 / 2 - 1);
    }
    
    private void drawGlobalStatus(Graphics g) {
        int type = computeTotalStatusType();
        Color resultingColor;
        
        switch (type) {
            case MarkProvider.UP_TO_DATE_DIRTY:
                drawOneColorGlobalStatus(g, Color.GRAY);
                break;
            case MarkProvider.UP_TO_DATE_PROCESSING:
                {
                    Status totalStatus = computeTotalStatus();

                    drawInProgressGlobalStatus(g, Status.getDefaultColor(totalStatus));
                    break;
                }
            case MarkProvider.UP_TO_DATE_OK:
                {
                    Status totalStatus = computeTotalStatus();

                    drawOneColorGlobalStatus(g, Status.getDefaultColor(totalStatus));
                    break;
                }
            default:
                throw new IllegalStateException();
        }
    }
    
    /*package private*/ static int findNextUsedLine(int from, SortedMap/*<Mark>*/ marks) {
        SortedMap next = marks.tailMap(new Integer(from + 1));
        
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            ERR.log("AnnotationView.findNextUsedLine from: " + from);
            ERR.log("AnnotationView.findNextUsedLine marks: " + marks);
            ERR.log("AnnotationView.findNextUsedLine next: " + next);
        }
        
        if (next.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        
        Integer nextLine = (Integer) next.firstKey();
        
        return nextLine.intValue();
    }
    
    private void registerMark(Mark mark) {
        int[] span = mark.getAssignedLines();
        
        for (int line = span[0]; line <= span[1]; line++) {
            Integer lineInt = new Integer(line);
            
            List inside = (List) marksMap.get(lineInt);
            
            if (inside == null) {
                marksMap.put(lineInt, inside = new ArrayList());
            }
            
            inside.add(mark);
        }
    }
    
    private void unregisterMark(Mark mark) {
        int[] span = mark.getAssignedLines();
        
        for (int line = span[0]; line <= span[1]; line++) {
            Integer lineInt = new Integer(line);
            
            List inside = (List) marksMap.get(lineInt);
            
            if (inside != null) {
                inside.remove(mark);
                
                if (inside.size() == 0) {
                    marksMap.remove(lineInt);
                }
            }
        }
    }
    
    /*package private for tests*/synchronized SortedMap getMarkMap() {
        if (marksMap == null) {
            List/*<Mark>*/ marks = getMergedMarks();
            marksMap = new TreeMap();
            
            for (Iterator i = marks.iterator(); i.hasNext(); ) {
                Mark mark = (Mark) i.next();
                
                registerMark(mark);
            }
        }
        
        return marksMap;
    }
    
    public void paintComponent(Graphics g) {
        long startTime = System.currentTimeMillis();
        super.paintComponent(g);
        
        Color oldColor = g.getColor();
        
        g.setColor(UIManager.getColor("Panel.background")); // NOI18N
        
        g.fillRect(0, 0, getWidth(), getHeight());
        
        SortedMap marks = getMarkMap();
        
        int annotatedLine = findNextUsedLine(-1, marks);
        
        while (annotatedLine != Integer.MAX_VALUE) {
//            System.err.println("annotatedLine = " + annotatedLine );
            int[] lineSpan  = getLinesSpan(annotatedLine);
            int   startLine = lineSpan[0];
            int   endLine   = lineSpan[1];
            
            Mark m = getMainMarkForBlock(startLine, endLine);
            
            if (m != null) {
                Status s = m.getStatus();
                double start = modelToView(annotatedLine);
                
                if (s != null) {
//                    System.err.println("m = " + m );
                    Color color = m.getEnhancedColor();
                    
                    if (color == null)
                        color = Status.getDefaultColor(s);
                    
                    assert color != null;
                    
                    g.setColor(color);
                    g.fillRect(0, (int) start, THICKNESS - 1, PIXELS_FOR_LINE);
                }
            }
            
            annotatedLine = findNextUsedLine(endLine, marks);
        }
        
        drawGlobalStatus(g);
        
        g.setColor(oldColor);
        
        long end = System.currentTimeMillis();
        
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            ERR.log("AnnotationView.paintComponent consumed: " + (end - startTime));
        }
    }
    
    /*package private for tests*/Status computeTotalStatus() {
        int targetStatus = Status.STATUS_OK;
        Collection/*<Mark>*/ marks = getMergedMarks();
        
        for (Iterator m = marks.iterator(); m.hasNext(); ) {
            Mark mark = (Mark) m.next();
            Status s = mark.getStatus();
            
            targetStatus = Status.getCompoundStatus(s.getStatus(), targetStatus);
        }
        
        return new Status(targetStatus);
    }
    
    /*package private for tests*/int computeTotalStatusType() {
        int statusType = MarkProvider.UP_TO_DATE_OK;
        
        for (Iterator p = providers.iterator(); p.hasNext(); ) {
            MarkProvider provider = (MarkProvider) p.next();
            int newType = provider.getUpToDate();
            
            if (newType > statusType) {
                statusType = newType;
            }
        }
        
        return statusType;
    }

    private void fullRepaint() {
        fullRepaint(false);
    }
    
    private void fullRepaint(final boolean clearMarksCache) {
        //Fix for #54193:
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (clearMarksCache) {
                    synchronized (AnnotationView.this) {
                        currentMarks = null;
                        marksMap = null;
                    }
                }
                
                invalidate();
                repaint();
            }
        });
    }
    
    public void changedAll() {
        fullRepaint(true);
    }
    
    private void documentChange() {
        fullRepaint(lines != Utilities.getRowCount(doc));
    }
    
    private double getComponentHeight() {
        return pane.getUI().getRootView(pane).getPreferredSpan(View.Y_AXIS);
    }
    
    private double getUsableHeight() {
        return getHeight() - HEIGHT_OFFSET - HEIGHT_LOWER_OFFSET;
    }
    
    private Rectangle[] modelToViewCache = null;
    private int lines = -1;
    private int height = -1;
    
    private synchronized Rectangle getModelToViewImpl(int line) {
        try {
            if (modelToViewCache == null || height != pane.getHeight() || lines != Utilities.getRowCount(doc)) {
                modelToViewCache = new Rectangle[Utilities.getRowCount(doc) + 2];
                lines = Utilities.getRowCount(doc);
                height = pane.getHeight();
            }
            
            Rectangle result = modelToViewCache[line + 1];
            
            if (result == null) {
                int lineOffset = Utilities.getRowStartFromLineOffset((BaseDocument) pane.getDocument(), line);
                
                modelToViewCache[line + 1] = result = pane.modelToView(lineOffset);
            }
            
            return result;
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
            return null;
        }
    }
    
    /*package private*/ double modelToView(int line) {
            Rectangle r = getModelToViewImpl(line);
            
            if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ERR.log(ErrorManager.INFORMATIONAL, "AnnotationView.modelToView: line=" + line); // NOI18N
//                ERR.log(ErrorManager.INFORMATIONAL, "AnnotationView.modelToView: lineOffset=" + lineOffset); // NOI18N
                ERR.log(ErrorManager.INFORMATIONAL, "AnnotationView.modelToView: r=" + r); // NOI18N
                ERR.log(ErrorManager.INFORMATIONAL, "AnnotationView.modelToView: getComponentHeight()=" + getComponentHeight()); // NOI18N
                ERR.log(ErrorManager.INFORMATIONAL, "AnnotationView.modelToView: getUsableHeight()=" + getUsableHeight()); // NOI18N
            }
            
            if (r == null) {
                return -1;
            }
            
            if (getComponentHeight() <= getUsableHeight()) {
                //1:1 mapping:
                return r.getY() + HEIGHT_OFFSET;
            } else {
                double position = r.getY() / getComponentHeight();
                int    blocksCount = (int) (getUsableHeight() / (PIXELS_FOR_LINE + LINE_SEPARATOR_SIZE));
                int    block = (int) (position * blocksCount);
                
                return block * (PIXELS_FOR_LINE + LINE_SEPARATOR_SIZE) + HEIGHT_OFFSET;
            }
    }
    
    private static final int VIEW_TO_MODEL_IMPORTANCE = ErrorManager.INFORMATIONAL;
    
    /*package private*/ int[] viewToModel(double offset) {
        try {
            if (getComponentHeight() <= getUsableHeight()) {
                //1:1 mapping:
                int positionOffset = pane.viewToModel(new Point(1, (int) (offset - HEIGHT_OFFSET)));
                int line = Utilities.getLineOffset(doc, positionOffset);
                
                if (ERR.isLoggable(VIEW_TO_MODEL_IMPORTANCE)) {
                    ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.viewToModel: line=" + line); // NOI18N
                }
                
                double position = modelToView(line);
                
                if (offset < position || offset >= (position + PIXELS_FOR_LINE))
                    return null;
                
                return getLinesSpan(line);
            } else {
                int    blocksCount = (int) (getUsableHeight() / (PIXELS_FOR_LINE + LINE_SEPARATOR_SIZE));
                int    block = (int) ((offset - HEIGHT_OFFSET) / (PIXELS_FOR_LINE + LINE_SEPARATOR_SIZE));
                double yPos = (getComponentHeight() * block) / blocksCount;
                int    positionOffset = pane.viewToModel(new Point(0, (int) yPos));
                int    line = Utilities.getLineOffset(doc, positionOffset) + 1;
                int[] span = getLinesSpan(line);
                double normalizedOffset = modelToView(span[0]);
                
                if (ERR.isLoggable(VIEW_TO_MODEL_IMPORTANCE)) {
                    ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.viewToModel: offset=" + offset); // NOI18N
                    ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.viewToModel: block=" + block); // NOI18N
                    ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.viewToModel: blocksCount=" + blocksCount); // NOI18N
                    ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.viewToModel: pane.getHeight()=" + pane.getHeight()); // NOI18N
                    ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.viewToModel: yPos=" + yPos); // NOI18N
                    ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.viewToModel: positionOffset=" + positionOffset); // NOI18N
                    ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.viewToModel: line=" + line); // NOI18N
                    ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.viewToModel: normalizedOffset=" + normalizedOffset); // NOI18N
                }
                
                if (offset < normalizedOffset || offset >= (normalizedOffset + PIXELS_FOR_LINE)) {
                    return null;
                }
                
                if (block < 0)
                    return null;
                
                return span;
            }
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
            return null;
        }
    }
    
    private Mark getMarkForPointImpl(double point) {
        int[] lineSpan   = viewToModel(point);
        
        if (lineSpan == null)
            return null;
        
        int   startLine  = lineSpan[0];
        int   endLine    = lineSpan[1];
        
        if (startLine != (-1)) {
            return getMainMarkForBlock(startLine, endLine);
        }
        
        return null;
    }

    /*package private*/ Mark getMarkForPoint(double point) {
        //Normalize the point:
        point = ((int) (point / (PIXELS_FOR_LINE + LINE_SEPARATOR_SIZE))) * (PIXELS_FOR_LINE + LINE_SEPARATOR_SIZE);
        
        Mark a = getMarkForPointImpl(point);
        
        if (ERR.isLoggable(VIEW_TO_MODEL_IMPORTANCE)) {
            ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.getAnnotationForPoint: point=" + point); // NOI18N
            ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.getAnnotationForPoint: a=" + a); // NOI18N
        }
        
        for (short relative = 1; relative < UPPER_HANDLE + 1 && a == null; relative++) {
            a = getMarkForPointImpl(point + relative);

            if (ERR.isLoggable(VIEW_TO_MODEL_IMPORTANCE)) {
                ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.getAnnotationForPoint: a=" + a); // NOI18N
                ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.getAnnotationForPoint: relative=" + relative); // NOI18N
            }
        }
        
        for (short relative = 1; relative < LOWER_HANDLE + 1 && a == null; relative++) {
            a = getMarkForPointImpl(point - relative);

            if (ERR.isLoggable(VIEW_TO_MODEL_IMPORTANCE)) {
                ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.getAnnotationForPoint: a=" + a); // NOI18N
                ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.getAnnotationForPoint: relative=-" + relative); // NOI18N
            }
        }
        
        return a;
    }
    
    public Dimension getMaximumSize() {
        return new Dimension(THICKNESS, Integer.MAX_VALUE);
    }

    public Dimension getMinimumSize() {
        return new Dimension(THICKNESS, Integer.MIN_VALUE);
    }

    public Dimension getPreferredSize() {
        return new Dimension(THICKNESS, Integer.MAX_VALUE);
    }

    public void mouseReleased(MouseEvent e) {
        //NOTHING:
        resetCursor();
    }

    public void mousePressed(MouseEvent e) {
        resetCursor();
    }

    public void mouseMoved(MouseEvent e) {
        checkCursor(e);
    }

    public void mouseExited(MouseEvent e) {
        resetCursor();
    }

    public void mouseEntered(MouseEvent e) {
        checkCursor(e);
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
        resetCursor();
        
        Mark mark = getMarkForPoint(e.getPoint().getY());
        
        if (mark!= null) {
            pane.setCaretPosition(Utilities.getRowStartFromLineOffset(doc, mark.getAssignedLines()[0]));
        }
    }
    
    private void resetCursor() {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
    
    private void checkCursor(MouseEvent e) {
        Mark mark = getMarkForPoint(e.getPoint().getY());
        
        if (mark == null) {
            resetCursor();
            return ;
        }
        
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public String getToolTipText(MouseEvent event) {
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            ERR.log(ErrorManager.INFORMATIONAL, "getToolTipText: event=" + event); // NOI18N
        }
        int y = event.getY();
        
        if (y <= HEIGHT_OFFSET) {
            int errors = 0;
            int warnings = 0;
            Collection/*<Mark>*/ marks = getMergedMarks();
            
            for (Iterator m = marks.iterator(); m.hasNext(); ) {
                Mark mark = (Mark) m.next();
                Status s = mark.getStatus();
                    
                errors += s.getStatus() == Status.STATUS_ERROR ? 1 : 0;
                warnings += s.getStatus() == Status.STATUS_WARNING ? 1 : 0;
            }
            
            if (errors == 0 && warnings == 0) {
                return NbBundle.getBundle(AnnotationView.class).getString("TP_NoErrors"); // NOI18N
            }
            
            if (errors == 0 && warnings != 0) {
                return MessageFormat.format(NbBundle.getBundle(AnnotationView.class).getString("TP_X_warning(s)"), new Object[] {new Integer(warnings)}); // NOI18N
            }
            
            if (errors != 0 && warnings == 0) {
                return MessageFormat.format(NbBundle.getBundle(AnnotationView.class).getString("TP_X_error(s)"), new Object[] {new Integer(errors)}); // NOI18N
            }
            
            return MessageFormat.format(NbBundle.getBundle(AnnotationView.class).getString("TP_X_error(s)_Y_warning(s)"), new Object[] {new Integer(errors), new Integer(warnings)}); // NOI18N
        }
        
        Mark mark = getMarkForPoint(y);
        
        if (mark != null) {
            String description = mark.getShortDescription();
            
            if (description != null) {
                return "<html><body>" + translate(description); // NOI18N
            }
        }
        
        return null;
    }
    
    private static String[] c = new String[] {"&", "<", ">", "\n", "\""};
    private static String[] tags = new String[] {"&amp;", "&lt;", "&gt;", "<br>", "&quot;"};
    
    private String translate(String input) {
        for (int cntr = 0; cntr < c.length; cntr++) {
            input = input.replaceAll(c[cntr], tags[cntr]);
        }
        
        return input;
    }

    public void foldHierarchyChanged(FoldHierarchyEvent evt) {
        fullRepaint();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if ("marks".equals(evt.getPropertyName())) {
            synchronized (this) {
                Collection nue = (Collection) evt.getNewValue();
                Collection old = (Collection) evt.getOldValue();
                List added = new ArrayList(nue);
                List removed = new ArrayList(old);
                
                added.removeAll(old);
                removed.removeAll(nue);
                
                if (marksMap != null) {
                    for (Iterator i = removed.iterator(); i.hasNext(); ) {
                        unregisterMark((Mark) i.next());
                    }
                    
                    for (Iterator i = added.iterator(); i.hasNext(); ) {
                        registerMark((Mark) i.next());
                    }
                }
                
                fullRepaint();
                
                if (currentMarks != null) {
                    currentMarks.removeAll(removed);
                    currentMarks.addAll(added);
                }
                return ;
            }
        }
        
        if (evt.getSource() == this.pane && "document".equals(evt.getPropertyName())) {
            updateForNewDocument();
            changedAll();
            return ;
        }
        
        changedAll();
    }

    public void removeUpdate(DocumentEvent e) {
        documentChange();
    }
    
    public void insertUpdate(DocumentEvent e) {
        documentChange();
    }
    
    public void changedUpdate(DocumentEvent e) {
        //ignored...
    }

}
