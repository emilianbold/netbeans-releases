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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.AnnotationDesc;
import org.netbeans.editor.Annotations;
import org.netbeans.editor.Annotations.AnnotationsListener;

import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.openide.ErrorManager;

/**
 *
 * @author Jan Lahoda
 */
public class AnnotationView extends JComponent implements AnnotationsListener, MouseListener, MouseMotionListener {
    
    private static final ErrorManager ERR = ErrorManager.getDefault().getInstance("org.netbeans.modules.editor.errorstripe.AnnotationView");
    
    private static final int STATUS_BOX_SIZE = 7;
    private static final int THICKNESS = STATUS_BOX_SIZE + 6;
    private static final int PIXELS_FOR_LINE = 2/*height / lines*/;
    private static final int LINE_SEPARATOR_SIZE = 2;
    private static final int HEIGHT_OFFSET = 20;
    
    private BaseDocument doc;
    private JTextComponent  pane;
    
    private static final Color[] DEFAULT_STATUS_COLORS = new Color[] {Color.GREEN, Color.YELLOW, Color.RED};
    
    private static final Color BACKGROUND_COLOR = new Color(224, 224, 224);
    private static final Color STATUS_UP_PART_COLOR = Color.WHITE;
    private static final Color STATUS_DOWN_PART_COLOR = new Color(180, 180, 180);
    
    /** Creates a new instance of AnnotationViewBorder */
    public AnnotationView(JTextComponent pane) {
        this.pane = pane;
        
        this.doc = (BaseDocument) pane.getDocument();
        
        doc.getAnnotations().addAnnotationsListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        
        setOpaque(true);
        
        setToolTipText("Error Stripe");
    }
    
    private static class AnnotationStatusPair implements Comparable {
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
            return status.compareTo(((AnnotationStatusPair) o).getStatus());
        }

    }

    private List/*<Status>*/ getStatusesForLine(int line) {
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
        
        Status s = getStatusForAnnotationDescription(annotations.getActiveAnnotation(line));
        
        if (s != null) {
            result.add(new AnnotationStatusPair(s, annotations.getActiveAnnotation(line)));
        }
        
        return result;
    }
    
    private List/*<Status>*/ getStatusesForLinesBlock(int currentLine) {
        Annotations annotations = doc.getAnnotations();
        int linesInBlock = computeLinesForBlock();
        int startingLine = (currentLine / linesInBlock) * linesInBlock;
        int endingLine = startingLine + linesInBlock;
        
        int current = startingLine;
        
        List result = new ArrayList();
        
        while ((current = annotations.getNextLineWithAnnotation(current)) != (-1) && current < endingLine) {
            result.addAll(getStatusesForLine(current));
            current++;
        }
        
        Collections.sort(result);
        
        return result;
    }
    
    private void drawGlobalStatus(Graphics g) {
        Status totalStatus = computeTotalStatus();
        Color  totalColor  = totalStatus.getEnhancedColor();
        
        if (totalColor == null) {
            totalColor = DEFAULT_STATUS_COLORS[totalStatus.getStatus()];
        }
        
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
    
    /**
     * Paints the border for the specified component with the specified 
     * position and size.
     * @param c the component for which this border is being painted
     * @param g the paint graphics
     * @param x the x position of the painted border
     * @param y the y position of the painted border
     * @param width the width of the painted border
     * @param height the height of the painted border
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Color oldColor = g.getColor();
        
        g.setColor(BACKGROUND_COLOR);
        
        g.fillRect(0, 0, getWidth(), getHeight());
        
        int annotatedLine = doc.getAnnotations().getNextLineWithAnnotation(-1);
        
        while (annotatedLine != (-1)) {
            List/*<Status>*/ statuses = getStatusesForLinesBlock(annotatedLine);
            
            if (statuses.size() > 0) {
                Status s = ((AnnotationStatusPair) statuses.get(0)).getStatus();
                int start = modelToView(annotatedLine);
                
                if (s != null) {
                    Color color = s.getEnhancedColor();
                    
                    if (color == null) {
                        color = DEFAULT_STATUS_COLORS[s.getStatus()];
                    }
                    
                    g.setColor(color);
                    g.fillRect(0, start, THICKNESS - 1, PIXELS_FOR_LINE);
                }
            }
            
            annotatedLine = doc.getAnnotations().getNextLineWithAnnotation(annotatedLine + 1);
        }
        
        drawGlobalStatus(g);
        
        g.setColor(oldColor);
    }
    
    private Status getStatusForAnnotationDescription(AnnotationDesc description) {
        return StatusForAnnotationTypeQuery.getDefault().getStatusForAnnotationType(description.getAnnotationType());
    }
    
    private Status computeTotalStatus() {
        int targetStatus = Status.STATUS_OK;
        int annotatedLine = -1;
        int linesInBlock = computeLinesForBlock();
        int totalLines = Utilities.getRowCount(doc);
        
        while ((annotatedLine = doc.getAnnotations().getNextLineWithAnnotation(annotatedLine)) != (-1) && annotatedLine < totalLines) {
            List/*<Status>*/ statuses = getStatusesForLinesBlock(annotatedLine);
            
            for (Iterator i = statuses.iterator(); i.hasNext(); ) {
                Status s = ((AnnotationStatusPair) i.next()).getStatus();
                
                targetStatus = Status.getCompoundStatus(targetStatus, s.getStatus());
            }
            
            annotatedLine = (annotatedLine / linesInBlock) * linesInBlock + linesInBlock;
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
        invalidate();
        repaint();
    }
    
    private double computeLineSeparatorSize() {
        int lines = Utilities.getRowCount(doc);
        double proposedAnnotationSize = ((double) (getHeight() - HEIGHT_OFFSET)) / lines;
        double result;
        
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            ERR.log(ErrorManager.INFORMATIONAL, "computeLineSeparatorSize: proposedAnnotationSize=" + proposedAnnotationSize);
        }
        
        if (proposedAnnotationSize > (LINE_SEPARATOR_SIZE  + PIXELS_FOR_LINE)) {
            int lineHeight = Utilities.getEditorUI(pane).getLineHeight();
            
            if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ERR.log(ErrorManager.INFORMATIONAL, "computeLineSeparatorSize: lineHeight=" + lineHeight);
            }

            if (lineHeight < proposedAnnotationSize) {
                result = lineHeight - PIXELS_FOR_LINE;
            } else {
                result = proposedAnnotationSize - PIXELS_FOR_LINE;
            }
        } else {
            result = LINE_SEPARATOR_SIZE;
        }
        
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            ERR.log(ErrorManager.INFORMATIONAL, "computeLineSeparatorSize: result=" + result);
        }
        
        return result;
    }
    
    private int computeLinesForBlock() {
        int lines = Utilities.getRowCount(doc);
        double lineSeparatorSize = computeLineSeparatorSize();
        int result = (int) (lines / ((getHeight() - HEIGHT_OFFSET) / (PIXELS_FOR_LINE + lineSeparatorSize)));
        
        if (result == 0)
            result++;
        
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            ERR.log(ErrorManager.INFORMATIONAL, "computeLinesForBlock: result=" + result);
        }
        
        return result;
    }
    
    private int modelToView(int line) {
        int lines = Utilities.getRowCount(doc);
        int linesForPixel = computeLinesForBlock();
        double lineSeparatorSize = computeLineSeparatorSize();
        
        return (int) ((line / linesForPixel) * (PIXELS_FOR_LINE + lineSeparatorSize) + HEIGHT_OFFSET);
    }
    
    private int viewToModel(int offset) {
        int lines = Utilities.getRowCount(doc);
        int linesForPixel = computeLinesForBlock();
        double lineSeparatorSize = computeLineSeparatorSize();
        int linesStart = (int) ((offset - HEIGHT_OFFSET) / (PIXELS_FOR_LINE + lineSeparatorSize) * linesForPixel);
        int linesEnd = linesStart + linesForPixel;
        int line = doc.getAnnotations().getNextLineWithAnnotation(linesStart);
        
        if (line >= linesEnd)
            return -1;
        else
            return line;
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
        int annotated = viewToModel(e.getPoint().y);
        
        resetCursor();
        
        if (annotated != (-1))
            pane.setCaretPosition(Utilities.getRowStartFromLineOffset(doc, annotated));
    }
    
    private void resetCursor() {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
    
    private void checkCursor(MouseEvent e) {
        int annotated = viewToModel(e.getPoint().y);
        
        if (annotated == (-1)) {
            resetCursor();
            return ;
        }
        
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public String getToolTipText(MouseEvent event) {
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            ERR.log(ErrorManager.INFORMATIONAL, "getToolTipText: event=" + event);
        }
        int y = event.getY();
        
        if (y <= HEIGHT_OFFSET) {
            int errors = 0;
            int warnings = 0;
            int annotatedLine = -1;
            int linesInBlock = computeLinesForBlock();
            int totalLines = Utilities.getRowCount(doc);
            
            while ((annotatedLine = doc.getAnnotations().getNextLineWithAnnotation(annotatedLine)) != (-1) && annotatedLine < totalLines) {
                List/*<Status>*/ statuses = getStatusesForLinesBlock(annotatedLine);
                
                for (Iterator i = statuses.iterator(); i.hasNext(); ) {
                    Status s = ((AnnotationStatusPair) i.next()).getStatus();
                    
                    errors += s.getStatus() == Status.STATUS_ERROR ? 1 : 0;
                    warnings += s.getStatus() == Status.STATUS_WARNING ? 1 : 0;
                }
                
                annotatedLine = (annotatedLine / linesInBlock) * linesInBlock + linesInBlock;
            }
            
            if (errors == 0 && warnings == 0) {
                return "No errors";
            }
            
            if (errors == 0 && warnings != 0) {
                return "" + warnings + " warning(s)";
            }
            
            if (errors != 0 && warnings == 0) {
                return "" + errors + " error(s)";
            }
            
            return "" + errors + " error(s), " + warnings + " warning(s)";
        }
        
        int line = viewToModel(y);
        
        if (line != (-1)) {
            List/*<Status>*/ statuses = getStatusesForLinesBlock(line);
            int index = 0;
            
            while (index < statuses.size()) {
                String description = ((AnnotationStatusPair) statuses.get(0)).getAnnotation().getShortDescription();
                
                if (description != null)
                    return description;
            }
            
            return null;
        }
        
        return null;
    }
    
}
