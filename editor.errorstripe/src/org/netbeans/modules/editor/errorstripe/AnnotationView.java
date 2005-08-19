/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
import java.text.MessageFormat;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import javax.swing.text.View;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldHierarchyEvent;
import org.netbeans.api.editor.fold.FoldHierarchyListener;

import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseTextUI;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.errorstripe.privatespi.Mark;
import org.netbeans.spi.editor.errorstripe.UpToDateStatus;
import org.openide.ErrorManager;
import org.netbeans.modules.editor.errorstripe.privatespi.Status;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/**
 *
 * @author Jan Lahoda
 */
public class AnnotationView extends JComponent implements FoldHierarchyListener, MouseListener, MouseMotionListener, DocumentListener, PropertyChangeListener {
    
    /*package private*/ static final ErrorManager ERR = ErrorManager.getDefault().getInstance("org.netbeans.modules.editor.errorstripe.AnnotationView"); // NOI18N
    
    /*package private*/ static final ErrorManager TIMING_ERR = ErrorManager.getDefault().getInstance("org.netbeans.modules.editor.errorstripe.AnnotationView.timing"); // NOI18N
    
    private static final int STATUS_BOX_SIZE = 7;
    private static final int THICKNESS = STATUS_BOX_SIZE + 6;
    /*package private*/ static final int PIXELS_FOR_LINE = 3/*height / lines*/;
    /*package private*/ static final int LINE_SEPARATOR_SIZE = 1/*2*/;
    /*package private*/ static final int HEIGHT_OFFSET = 20;
    /*package private*/ static final int HEIGHT_LOWER_OFFSET = 10;
    
    /*package private*/ static final int UPPER_HANDLE = 4;
    /*package private*/ static final int LOWER_HANDLE = 4;
    
    private BaseDocument doc;
    private JTextComponent  pane;
    
    private static final Color STATUS_UP_PART_COLOR = Color.WHITE;
    private static final Color STATUS_DOWN_PART_COLOR = new Color(0xCDCABB);
    
    private static final int QUIET_TIME = 100;
    
    private final RequestProcessor.Task repaintTask;
    private final RepaintTask           repaintTaskRunnable;
    
    private AnnotationViewData data;
    
    private static Icon busyIcon;
    
    static {
        busyIcon = new ImageIcon(AnnotationView.class.getResource("resources/hodiny.gif"));
    }
    
//    public AnnotationView(JTextComponent pane) {
//        this(pane, null);
//    }
    
    /** Creates a new instance of AnnotationViewBorder */
    public AnnotationView(JTextComponent pane/*, List/ *<MarkProviderCreator>* / creators*/) {
        this.pane = pane;
        
        FoldHierarchy.get(pane).addFoldHierarchyListener(this);
        
        pane.addPropertyChangeListener(this);
        
        repaintTask = RequestProcessor.getDefault().create(repaintTaskRunnable = new RepaintTask());
        
        data = new AnnotationViewDataImpl(this, pane);
        
        updateForNewDocument();
        
        addMouseListener(this);
        addMouseMotionListener(this);
        
        setOpaque(true);
        
        setToolTipText(org.openide.util.NbBundle.getBundle(AnnotationView.class).getString("TP_ErrorStripe"));
    }
    
    /*package private for tests*/AnnotationViewData getData() {
        return data;
    }
    
    private synchronized void updateForNewDocument() {
        data.unregister();
        Document newDocument = pane.getDocument();
        
        if (!(newDocument instanceof BaseDocument)) {
            this.doc = null;
        } else {
            this.doc = (BaseDocument) pane.getDocument();
            this.doc.addDocumentListener(this);
        }
        
        data.register(this.doc);
//        gatherProviders(pane);
//        addListenersToProviders();
    }
        
    /*package private for tests*/int[] getLinesSpan(int currentLine) {
        double position  = modelToView(currentLine);
        
        if (position == (-1))
            return new int[] {currentLine, currentLine};
            
        int    startLine = currentLine;
        int    endLine   = currentLine;
        
        while (position == modelToView(startLine - 1) && startLine > 0)
            startLine--;
        
        while ((endLine + 1) < Utilities.getRowCount(doc) && position == modelToView(endLine + 1))
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
        int x = (THICKNESS - STATUS_BOX_SIZE) / 2;
        int y = (HEIGHT_OFFSET - STATUS_BOX_SIZE) / 2;
	
        busyIcon.paintIcon(this, g, x, y); // NOI18N
	
        g.setColor(STATUS_DOWN_PART_COLOR);
        
        g.drawLine(x - 1, y - 1, x + STATUS_BOX_SIZE, y - 1              );
        g.drawLine(x - 1, y - 1, x - 1,               y + STATUS_BOX_SIZE);
        
        g.setColor(STATUS_UP_PART_COLOR);
        
        g.drawLine(x - 1,               y + STATUS_BOX_SIZE, x + STATUS_BOX_SIZE, y + STATUS_BOX_SIZE);
        g.drawLine(x + STATUS_BOX_SIZE, y - 1,               x + STATUS_BOX_SIZE, y + STATUS_BOX_SIZE);
	
    }
    
    private static final Color GLOBAL_RED = new Color(0xFB4C48);
    private static final Color GLOBAL_YELLOW = Color.YELLOW;
    private static final Color GLOBAL_GREEN = new Color(0x65B56B);
    
    private Color getColorForGlobalStatus(Status status) {
        if (Status.STATUS_ERROR == status)
            return GLOBAL_RED;
        
        if (Status.STATUS_WARNING == status)
            return GLOBAL_YELLOW;
        
        return GLOBAL_GREEN;
    }
    
    private void drawGlobalStatus(Graphics g) {
        UpToDateStatus type = data.computeTotalStatusType();
        Color resultingColor;
        
        if (type == UpToDateStatus.UP_TO_DATE_DIRTY) {
                drawOneColorGlobalStatus(g, UIManager.getColor("Panel.background"));
        } else {
            if (type == UpToDateStatus.UP_TO_DATE_PROCESSING) {
//                Status totalStatus = data.computeTotalStatus();
//                
                drawInProgressGlobalStatus(g, null/*Status.getDefaultColor(totalStatus)*/);
            } else {
                if (type == UpToDateStatus.UP_TO_DATE_OK) {
                    Status totalStatus = data.computeTotalStatus();
                    
                    drawOneColorGlobalStatus(g, getColorForGlobalStatus(totalStatus));
                } else {
                    throw new IllegalStateException("Unknown up-to-date type: " + type);
                }
            }
        }
    }
    
    private int getCurrentLine() {
        int offset = pane.getCaretPosition(); //TODO: AWT?
        Document doc = pane.getDocument();
        int line = -1;
        
        if (doc instanceof StyledDocument) {
            line = NbDocument.findLineNumber((StyledDocument) doc, offset);
        }
        
        return line;
    }
    
    public void paintComponent(Graphics g) {
//        Thread.dumpStack();
        long startTime = System.currentTimeMillis();
        super.paintComponent(g);
        
        Color oldColor = g.getColor();
        
        g.setColor(UIManager.getColor("Panel.background")); // NOI18N
        
        g.fillRect(0, 0, getWidth(), getHeight());
        
//        SortedMap marks = getMarkMap();
        int currentline = getCurrentLine();
        int annotatedLine = data.findNextUsedLine(-1);
        
        while (annotatedLine != Integer.MAX_VALUE) {
//            System.err.println("annotatedLine = " + annotatedLine );
            int[] lineSpan  = getLinesSpan(annotatedLine);
            int   startLine = lineSpan[0];
            int   endLine   = lineSpan[1];
            
            Mark m = data.getMainMarkForBlock(startLine, endLine);
            
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
                    
                    if (startLine <= currentline && currentline <= endLine && m.getType() != Mark.TYPE_CARET) {
                        g.fillRect(0, (int) start, THICKNESS - 1, PIXELS_FOR_LINE);
                    } else {
                        g.drawRect(0, (int) start, THICKNESS - 2, PIXELS_FOR_LINE - 1);
                    }
                }
            }
            
            annotatedLine = data.findNextUsedLine(endLine);
        }
        
        drawGlobalStatus(g);
        
        g.setColor(oldColor);
        
        long end = System.currentTimeMillis();
        
        if (TIMING_ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            TIMING_ERR.log("AnnotationView.paintComponent consumed: " + (end - startTime));
        }
    }

    /*private*/ void fullRepaint() {
        fullRepaint(false);
    }
    
    /*private*/ void fullRepaint(final boolean clearMarksCache) {
        synchronized (repaintTaskRunnable) {
            repaintTaskRunnable.setClearMarksCache(clearMarksCache);
            repaintTask.schedule(QUIET_TIME);
        }
    }
    
    private class RepaintTask implements Runnable {
        private boolean clearMarksCache;

        public void setClearMarksCache(boolean clearMarksCache) {
            this.clearMarksCache |= clearMarksCache;
        }
        
        private synchronized boolean readAndDestroyClearMarksCache() {
            boolean result = clearMarksCache;
            
            clearMarksCache = false;
            
            return result;
        }

        public void run() {
            final boolean clearMarksCache = readAndDestroyClearMarksCache();
            
            //Fix for #54193:
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (clearMarksCache) {
                        synchronized (AnnotationView.this) {
                            data.clear();
                        }
                    }
                    
                    invalidate();
                    repaint();
                }
            });
        }
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
    
    private int[] modelToViewCache = null;
    private int lines = -1;
    private int height = -1;
    
    private int getYFromPos(int offset) throws BadLocationException {
        TextUI ui = pane.getUI();
        int result;
        
        if (ui instanceof BaseTextUI) {
            result = ((BaseTextUI) ui).getYFromPos(offset);
        } else {
            Rectangle r = pane.modelToView(offset);
            
            result = r != null ? r.y : 0;
        }
        
        if (result == 0) {
            return -1;
        } else {
            return result;
        }
    }
    
    private synchronized int getModelToViewImpl(int line) throws BadLocationException {
        int docLines = Utilities.getRowCount(doc);
        
        if (modelToViewCache == null || height != pane.getHeight() || lines != docLines) {
            modelToViewCache = new int[Utilities.getRowCount(doc) + 2];
            lines = Utilities.getRowCount(doc);
            height = pane.getHeight();
        }
        
        if (line >= docLines)
            return -1;
        
        int result = modelToViewCache[line + 1];
        
        if (result == 0) {
            int lineOffset = Utilities.getRowStartFromLineOffset((BaseDocument) pane.getDocument(), line);
            
            modelToViewCache[line + 1] = result = getYFromPos(lineOffset);
        }
        
        if (result == (-1))
            result = 0;
        
        return result;
    }
    
    /*package private*/ double modelToView(int line) {
        try {
            int r = getModelToViewImpl(line);
            
            if (r == (-1))
                return -1.0;
            
            if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ERR.log(ErrorManager.INFORMATIONAL, "AnnotationView.modelToView: line=" + line); // NOI18N
//                ERR.log(ErrorManager.INFORMATIONAL, "AnnotationView.modelToView: lineOffset=" + lineOffset); // NOI18N
                ERR.log(ErrorManager.INFORMATIONAL, "AnnotationView.modelToView: r=" + r); // NOI18N
                ERR.log(ErrorManager.INFORMATIONAL, "AnnotationView.modelToView: getComponentHeight()=" + getComponentHeight()); // NOI18N
                ERR.log(ErrorManager.INFORMATIONAL, "AnnotationView.modelToView: getUsableHeight()=" + getUsableHeight()); // NOI18N
            }
            
            if (getComponentHeight() <= getUsableHeight()) {
                //1:1 mapping:
                return r + HEIGHT_OFFSET;
            } else {
                double position = r / getComponentHeight();
                int    blocksCount = (int) (getUsableHeight() / (PIXELS_FOR_LINE + LINE_SEPARATOR_SIZE));
                int    block = (int) (position * blocksCount);
                
                return block * (PIXELS_FOR_LINE + LINE_SEPARATOR_SIZE) + HEIGHT_OFFSET;
            }
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
            return -1.0;
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
                
                if (yPos == (int) yPos)
                    yPos -= 1;
                
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
            return data.getMainMarkForBlock(startLine, endLine);
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
        
        int relativeMax = Math.max(UPPER_HANDLE + 1, LOWER_HANDLE + 1);
        
        for (short relative = 1; relative < relativeMax && a == null; relative++) {
            if (relative <= UPPER_HANDLE) {
                a = getMarkForPointImpl(point + relative);
                
                if (ERR.isLoggable(VIEW_TO_MODEL_IMPORTANCE)) {
                    ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.getAnnotationForPoint: a=" + a); // NOI18N
                    ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.getAnnotationForPoint: relative=" + relative); // NOI18N
                }
            }
            
            if (relative <= LOWER_HANDLE && a == null) {
                a = getMarkForPointImpl(point - relative);
                
                if (ERR.isLoggable(VIEW_TO_MODEL_IMPORTANCE)) {
                    ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.getAnnotationForPoint: a=" + a); // NOI18N
                    ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.getAnnotationForPoint: relative=-" + relative); // NOI18N
                }
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
            int[] errWar = data.computeErrorsAndWarnings();
            int errors = errWar[0];
            int warnings = errWar[1];
            
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
    
    private static String[] c = new String[] {"&", "<", ">", "\n", "\""}; // NOI18N
    private static String[] tags = new String[] {"&amp;", "&lt;", "&gt;", "<br>", "&quot;"}; // NOI18N
    
    private String translate(String input) {
        for (int cntr = 0; cntr < c.length; cntr++) {
            input = input.replaceAll(c[cntr], tags[cntr]);
        }
        
        return input;
    }

    public void foldHierarchyChanged(FoldHierarchyEvent evt) {
        fullRepaint();
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

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == this.pane && "document".equals(evt.getPropertyName())) {
            updateForNewDocument();
            return ;
        }
        
        fullRepaint();
    }
    
}
