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
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.View;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldHierarchyEvent;
import org.netbeans.api.editor.fold.FoldHierarchyListener;
import org.netbeans.editor.AnnotationDesc;
import org.netbeans.editor.Annotations;
import org.netbeans.editor.Annotations.AnnotationsListener;

import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
/*package private*/ class AnnotationView extends JComponent implements AnnotationsListener, FoldHierarchyListener, MouseListener, MouseMotionListener {
    
    /*package private*/ static final ErrorManager ERR = ErrorManager.getDefault().getInstance("org.netbeans.modules.editor.errorstripe.AnnotationView"); // NOI18N
    
    private static final int STATUS_BOX_SIZE = 7;
    private static final int THICKNESS = STATUS_BOX_SIZE + 6;
    private static final int PIXELS_FOR_LINE = 2/*height / lines*/;
    private static final int LINE_SEPARATOR_SIZE = 0/*2*/;
    /*package private*/ static final int HEIGHT_OFFSET = 20;
    /*package private*/ static final int HEIGHT_LOWER_OFFSET = 10;
    
    private BaseDocument doc;
    private JTextComponent  pane;
    
    private static final Color STATUS_UP_PART_COLOR = Color.WHITE;
    private static final Color STATUS_DOWN_PART_COLOR = new Color(180, 180, 180);
    
    /** Creates a new instance of AnnotationViewBorder */
    public AnnotationView(JTextComponent pane) {
        this.pane = pane;
        
        FoldHierarchy.get(pane).addFoldHierarchyListener(this);
        
        this.doc = (BaseDocument) pane.getDocument();
        
        doc.getAnnotations().addAnnotationsListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        
        setOpaque(true);
        
        setToolTipText(org.openide.util.NbBundle.getBundle(AnnotationView.class).getString("TP_ErrorStripe"));
    }
    
    /*package private*/ static class AnnotationStatusPair implements Comparable {
        private Status status;
        private AnnotationDesc annotation;
        
        public AnnotationStatusPair(Status status, AnnotationDesc annotation) {
            this.status = status;
            this.annotation = annotation;
        }

        public Status getStatus() {
            return status;
        }

        public AnnotationDesc getAnnotation() {
            return annotation;
        }

        public int compareTo(Object o) {
            int statusCompare = -status.compareTo(((AnnotationStatusPair) o).getStatus());
            
            if (statusCompare == 0) {
                return annotation.getLine() - ((AnnotationStatusPair) o).annotation.getLine();
            } else {
                return statusCompare;
            }
        }

        public boolean equals(Object o) {
            if (!(o instanceof AnnotationStatusPair))
                return false;
            
            AnnotationStatusPair remote = (AnnotationStatusPair) o;
            
            if (annotation != remote.annotation)
                return false;
            
            if (!status.equals(remote.status))
                return false;
            
            return true;
        }
        
        public int hashCode() {
            return 53 ^ status.hashCode() ^ annotation.hashCode();
        }
    }

    private List/*<AnnotationStatusPair>*/ getStatusesForLine(int line) {
        return getStatusesForLineImpl(doc, line);
    }
    
    private static List/*<AnnotationStatusPair>*/ getStatusesForLineImpl(BaseDocument doc, int line) {
        Annotations annotations = doc.getAnnotations();
        List result = new ArrayList();
        
        if (annotations.getNumberOfAnnotations(line) > 1) {
            AnnotationDesc[] descriptions = annotations.getPasiveAnnotations(line);
            
            for (int cntr = 0; cntr < descriptions.length; cntr++) {
                Status s = getStatusForAnnotationDescription(descriptions[cntr]);
                
                if (s != null) {
                    result.add(new AnnotationStatusPair(s, descriptions[cntr]));
                }
            }
        }
        
        AnnotationDesc desc = annotations.getActiveAnnotation(line);
        Status         s    = null;
        
        if (desc != null)
            s = getStatusForAnnotationDescription(desc);
        
        if (s != null) {
            result.add(new AnnotationStatusPair(s, annotations.getActiveAnnotation(line)));
        }
        
        return result;
    }
    
    private List/*<AnnotationStatusPair>*/ getStatusesForBlock(int startLine, int endLine) {
        return getStatusesForBlockImpl(doc, startLine, endLine);
    }
    
    /*package private*/ static List/*<AnnotationStatusPair>*/ getStatusesForBlockImpl(BaseDocument doc, int startLine, int endLine) {
        Annotations annotations = doc.getAnnotations();
        int current = startLine;
        
        List result = new ArrayList();
        
        while ((current = annotations.getNextLineWithAnnotation(current)) != (-1) && current <= endLine) {
            result.addAll(getStatusesForLineImpl(doc, current));
            current++;
        }
        
        Collections.sort(result);
        
        return result;
    }
    
    private int[] getLinesSpan(int currentLine) {
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
    
    private void drawGlobalStatus(Graphics g) {
        Status totalStatus = computeTotalStatus();
        Color  totalColor  = totalStatus.getColor();
        
        assert totalColor != null;
        
        g.setColor(totalColor);
        
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
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Color oldColor = g.getColor();
        
        g.setColor(UIManager.getColor("Panel.background")); // NOI18N
        
        g.fillRect(0, 0, getWidth(), getHeight());
        
        int annotatedLine = doc.getAnnotations().getNextLineWithAnnotation(-1);
        
        while (annotatedLine != (-1)) {
            int[] lineSpan  = getLinesSpan(annotatedLine);
            int   startLine = lineSpan[0];
            int   endLine   = lineSpan[1];
            
            List/*<Status>*/ statuses = getStatusesForBlock(startLine, endLine);
            
            if (statuses.size() > 0) {
                Status s = ((AnnotationStatusPair) statuses.get(0)).getStatus();
                double start = modelToView(annotatedLine);
                
                if (s != null) {
                    Color color = s.getColor();
                    
                    assert color != null;
                    
                    g.setColor(color);
                    g.fillRect(0, (int) start, THICKNESS - 1, PIXELS_FOR_LINE);
                }
            }
            
            annotatedLine = doc.getAnnotations().getNextLineWithAnnotation(endLine + 1);
        }
        
        drawGlobalStatus(g);
        
        g.setColor(oldColor);
    }
    
    private static Status getStatusForAnnotationDescription(AnnotationDesc description) {
        return StatusForAnnotationTypeQuery.getDefault().getStatusForAnnotationType(description.getAnnotationType());
    }
    
    private Status computeTotalStatus() {
        int targetStatus = Status.STATUS_OK;
        int annotatedLine = -1;
        int totalLines = Utilities.getRowCount(doc);
        
        while ((annotatedLine = doc.getAnnotations().getNextLineWithAnnotation(annotatedLine)) != (-1) && annotatedLine < totalLines) {
            List/*<Status>*/ statuses = getStatusesForLine(annotatedLine);
            
            for (Iterator i = statuses.iterator(); i.hasNext(); ) {
                Status s = ((AnnotationStatusPair) i.next()).getStatus();
                
                targetStatus = Status.getCompoundStatus(targetStatus, s.getStatus());
            }
            
            annotatedLine++;
        }
        
        return new Status(targetStatus);
    }

    public void changedLine(int Line) {
//        int start = modelToView(line);
//        
//        repaint(0, start, THICKNESS - 1, pixelsForLine);
        changedAll();
    }

    public void changedAll() {
        //Fix for #54193:
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                invalidate();
                repaint();
            }
        });
    }
    
    private double getComponentHeight() {
        return pane.getUI().getRootView(pane).getPreferredSpan(View.Y_AXIS);
    }
    
    private double getUsableHeight() {
        return getHeight() - HEIGHT_OFFSET - HEIGHT_LOWER_OFFSET;
    }
    
    /*package private*/ double modelToView(int line) {
        try {
            int lineOffset = Utilities.getRowStartFromLineOffset((BaseDocument) pane.getDocument(), line);
            
            Rectangle r = pane.modelToView(lineOffset);
            
            if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ERR.log(ErrorManager.INFORMATIONAL, "AnnotationView.modelToView: line=" + line); // NOI18N
                ERR.log(ErrorManager.INFORMATIONAL, "AnnotationView.modelToView: lineOffset=" + lineOffset); // NOI18N
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
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
            return -1;
        }
    }
    
    private static final int VIEW_TO_MODEL_IMPORTANCE = ErrorManager.INFORMATIONAL;
    
    /*package private*/ int[] viewToModel(double offset) {
        if (ERR.isLoggable(VIEW_TO_MODEL_IMPORTANCE)) {
            ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.viewToModel: offset=" + offset); // NOI18N
        }
        
        int lowerBound = 0;
        int upperBound = Utilities.getRowCount((BaseDocument) pane.getDocument());
        
        int foundLine  = -1;
        int lastTestedLine = -1;
        
        while ((upperBound - lowerBound) > 1) {
            int testedLine = (lowerBound + upperBound) / 2;
            double testedOffset = modelToView(testedLine);
            boolean isAbove = (offset - PIXELS_FOR_LINE - LINE_SEPARATOR_SIZE / 2) <= testedOffset;
            boolean isBelow = testedOffset <= (offset + LINE_SEPARATOR_SIZE / 2 + 1);
            
            if (ERR.isLoggable(VIEW_TO_MODEL_IMPORTANCE)) {
                ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.viewToModel: testedLine=" + testedLine); // NOI18N
                ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.viewToModel: testedOffset=" + testedOffset); // NOI18N
                ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.viewToModel: isAbove=" + isAbove); // NOI18N
                ERR.log(VIEW_TO_MODEL_IMPORTANCE, "AnnotationView.viewToModel: isBelow=" + isBelow); // NOI18N
            }

            if (isAbove && isBelow) {
                foundLine = testedLine;
                break;
            }
            
            if (isAbove) {
                upperBound = testedLine;
                lastTestedLine = testedLine;
            }
            
            if (isBelow) {
                lowerBound = testedLine;
                lastTestedLine = testedLine;
            }
        }
        
        if (foundLine == (-1)) {
            foundLine = lastTestedLine;
        }
        
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            ERR.log(ErrorManager.INFORMATIONAL, "AnnotationView.modelToView: foundLine=" + foundLine); // NOI18N
        }
        
        return getLinesSpan(foundLine);
    }
    
    private AnnotationDesc getAnnotationForPointImpl(double point) {
        int[] lineSpan   = viewToModel(point);
        int   startLine  = lineSpan[0];
        int   endLine    = lineSpan[1];
        
        if (startLine != (-1)) {
            List/*<Status>*/ statuses = getStatusesForBlock(startLine, endLine);
            
            if (statuses.size() > 0) {
                return ((AnnotationStatusPair) statuses.get(0)).getAnnotation();
            }
        }
        
        return null;
    }

    /*package private*/ AnnotationDesc getAnnotationForPoint(double point) {
        AnnotationDesc a = getAnnotationForPointImpl(point);
        
        if (a == null) {
            a = getAnnotationForPointImpl(point - 2);
        }
        
        if (a == null) {
            a = getAnnotationForPointImpl(point + 2 + PIXELS_FOR_LINE);
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
        
        AnnotationDesc annotation = getAnnotationForPoint(e.getPoint().getY());
        
        if (annotation != null) {
            pane.setCaretPosition(annotation.getOffset());
        }
    }
    
    private void resetCursor() {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
    
    private void checkCursor(MouseEvent e) {
        AnnotationDesc annotation = getAnnotationForPoint(e.getPoint().getY());
        
        if (annotation == null) {
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
            int annotatedLine = -1;
            int totalLines = Utilities.getRowCount(doc);
            
            while ((annotatedLine = doc.getAnnotations().getNextLineWithAnnotation(annotatedLine)) != (-1) && annotatedLine < totalLines) {
                List/*<Status>*/ statuses = getStatusesForLine(annotatedLine);
                
                for (Iterator i = statuses.iterator(); i.hasNext(); ) {
                    Status s = ((AnnotationStatusPair) i.next()).getStatus();
                    
                    errors += s.getStatus() == Status.STATUS_ERROR ? 1 : 0;
                    warnings += s.getStatus() == Status.STATUS_WARNING ? 1 : 0;
                }
                
                annotatedLine++;
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
        
        AnnotationDesc annotation = getAnnotationForPoint(y);
        
        if (annotation != null) {
            String description = annotation.getShortDescription();
            
            if (description != null) {
                return "<html>" + description.replaceAll("\n", "<br>"); // NOI18N
            }
        }
        
        return null;
    }

    public void foldHierarchyChanged(FoldHierarchyEvent evt) {
        changedAll();
    }
    
}
