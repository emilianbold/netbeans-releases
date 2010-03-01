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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.editor.errorstripe;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
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
import org.netbeans.modules.editor.errorstripe.caret.CaretMark;
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
public class AnnotationView extends JComponent implements FoldHierarchyListener, MouseListener, MouseMotionListener, DocumentListener, PropertyChangeListener, Accessible {
    
    /*package private*/ static final ErrorManager ERR = ErrorManager.getDefault().getInstance("org.netbeans.modules.editor.errorstripe.AnnotationView"); // NOI18N
    
    /*package private*/ static final ErrorManager TIMING_ERR = ErrorManager.getDefault().getInstance("org.netbeans.modules.editor.errorstripe.AnnotationView.timing"); // NOI18N
    
    private static final int STATUS_BOX_SIZE = 7;
    private static final int THICKNESS = STATUS_BOX_SIZE + 6;
    /*package private*/ static final int PIXELS_FOR_LINE = 3/*height / lines*/;
    /*package private*/ static final int LINE_SEPARATOR_SIZE = 1/*2*/;
    /*package private*/ static final int HEIGHT_OFFSET = 20;
    
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
        // Set the name to be able to check for this component when "errorStripeOnly" property
        // is turned on for the pane in CustomizableSideBar.
        setName("errorStripe");
        
        repaintTask = RequestProcessor.getDefault().create(repaintTaskRunnable = new RepaintTask());
        data = new AnnotationViewDataImpl(this, pane);
        
        FoldHierarchy.get(pane).addFoldHierarchyListener(this);
        pane.addPropertyChangeListener(this);

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
        
        if (this.doc != null) {
            this.doc.removeDocumentListener(this);
            this.doc = null;
        }
        
        if (newDocument instanceof BaseDocument) {
            this.doc = (BaseDocument) pane.getDocument();
            this.doc.addDocumentListener(this);
        }
        
        data.register(this.doc);
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
        int y = (topOffset() - STATUS_BOX_SIZE) / 2;
        
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
        int y = (topOffset() - STATUS_BOX_SIZE) / 2;
	
        busyIcon.paintIcon(this, g, x, y); // NOI18N
	
        g.setColor(STATUS_DOWN_PART_COLOR);
        
        g.drawLine(x - 1, y - 1, x + STATUS_BOX_SIZE, y - 1              );
        g.drawLine(x - 1, y - 1, x - 1,               y + STATUS_BOX_SIZE);
        
        g.setColor(STATUS_UP_PART_COLOR);
        
        g.drawLine(x - 1,               y + STATUS_BOX_SIZE, x + STATUS_BOX_SIZE, y + STATUS_BOX_SIZE);
        g.drawLine(x + STATUS_BOX_SIZE, y - 1,               x + STATUS_BOX_SIZE, y + STATUS_BOX_SIZE);
	
    }
    
    private static final Color GLOBAL_RED = new Color(0xFF2A1C);
    private static final Color GLOBAL_YELLOW = new Color(0xE1AA00);
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
                drawOneColorGlobalStatus(g, UIManager.getColor("Panel.background")); // NOI18N
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
                    throw new IllegalStateException("Unknown up-to-date type: " + type); // NOI18N
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

        Color backColor = UIManager.getColor("NbEditorGlyphGutter.background"); //NOI18N
        if( null == backColor )
            backColor = UIManager.getColor("Panel.background"); // NOI18N
        g.setColor(backColor);
        
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
                    
                    
                    //g.fillRect(1, (int) start, THICKNESS - 2, PIXELS_FOR_LINE);                            
                    //* 3D Version
                    if ( m.getType() != Mark.TYPE_CARET ) {
                        g.fillRect(1, (int) start , THICKNESS - 2, PIXELS_FOR_LINE);                            
                        //g.draw3DRect(1, (int) start, THICKNESS - 3, PIXELS_FOR_LINE - 1, true);
                    }
                    //*/                       
                    if ((startLine <= currentline && currentline <= endLine) || m.getType() == Mark.TYPE_CARET ) {
                        drawCurrentLineMark(g, (int)start);
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

    private void drawCurrentLineMark(Graphics g, int start) {
        g.setColor( CaretMark.getCaretMarkColor());
        g.drawLine(2, start + PIXELS_FOR_LINE / 2, THICKNESS - 3, start + PIXELS_FOR_LINE / 2 );        
        g.fillRect( THICKNESS / 2 - PIXELS_FOR_LINE / 2, start, PIXELS_FOR_LINE, PIXELS_FOR_LINE );
        g.draw3DRect( THICKNESS / 2 - PIXELS_FOR_LINE / 2, start, PIXELS_FOR_LINE - 1, PIXELS_FOR_LINE - 1, true );
        
    }
    
    /*private*/ void fullRepaint() {
        fullRepaint(false);
    }
    
    /*private*/ void fullRepaint(final boolean clearMarksCache) {
        fullRepaint(clearMarksCache, false);
    }
    
    /*private*/ void fullRepaint(final boolean clearMarksCache, final boolean clearModelToViewCache) {
        synchronized (repaintTaskRunnable) {
            repaintTaskRunnable.setClearMarksCache(clearMarksCache);
            repaintTaskRunnable.setClearModelToViewCache(clearModelToViewCache);
            repaintTask.schedule(QUIET_TIME);
        }
    }
    
    private class RepaintTask implements Runnable {
        private boolean clearMarksCache;
        private boolean clearModelToViewCache;

        public void setClearMarksCache(boolean clearMarksCache) {
            this.clearMarksCache |= clearMarksCache;
        }
        
        public void setClearModelToViewCache(boolean clearModelToViewCache) {
            this.clearModelToViewCache |= clearModelToViewCache;
        }
        
        private synchronized boolean readAndDestroyClearMarksCache() {
            boolean result = clearMarksCache;
            
            clearMarksCache = false;
            
            return result;
        }

        private synchronized boolean readAndDestroyClearModelToViewCache() {
            boolean result = clearModelToViewCache;
            
            clearModelToViewCache = false;
            
            return result;
        }
        
        public void run() {
            final boolean clearMarksCache = readAndDestroyClearMarksCache();
            final boolean clearModelToViewCache= readAndDestroyClearModelToViewCache();
            
            //Fix for #54193:
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    synchronized (AnnotationView.this) {
                        if (clearMarksCache) {
                            data.clear();
                        }
                        if (clearModelToViewCache) {
                            modelToViewCache = null;
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
    
    double getUsableHeight() {
        //fix for issue #54080:
        //find the scrollpane which contains the pane:
        Component scrollPaneCandidade = pane.getParent();
        
        if (scrollPaneCandidade != null && !(scrollPaneCandidade instanceof JScrollPane)) {
            scrollPaneCandidade = scrollPaneCandidade.getParent();
        }
        
        Insets scrollBar = UIManager.getInsets("Nb.Editor.ErrorStripe.ScrollBar.Insets"); // NOI18N
        
        if (scrollPaneCandidade == null || !(scrollPaneCandidade instanceof JScrollPane) || scrollBar == null) {
            //no help for #54080:
            return getHeight() - HEIGHT_OFFSET;
        }
        
        JScrollPane scrollPane = (JScrollPane) scrollPaneCandidade;
        int visibleHeight = scrollPane.getViewport().getExtentSize().height;
        
        int topButton = topOffset();
        int bottomButton = scrollBar.bottom;
        
        return visibleHeight - topButton - bottomButton;
    }
    
    int topOffset() {
        Insets scrollBar = UIManager.getInsets("Nb.Editor.ErrorStripe.ScrollBar.Insets"); // NOI18N
        
        if (scrollBar == null) {
            //no help for #54080:
            return HEIGHT_OFFSET;
        }
        
        return (HEIGHT_OFFSET > scrollBar.top ? HEIGHT_OFFSET : scrollBar.top) + PIXELS_FOR_LINE;
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
                return r + topOffset();
            } else {
                double position = r / getComponentHeight();
                int    blocksCount = (int) (getUsableHeight() / (PIXELS_FOR_LINE + LINE_SEPARATOR_SIZE));
                int    block = (int) (position * blocksCount);
                
                return block * (PIXELS_FOR_LINE + LINE_SEPARATOR_SIZE) + topOffset();
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
                int positionOffset = pane.viewToModel(new Point(1, (int) (offset - topOffset())));
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
                int    block = (int) ((offset - topOffset()) / (PIXELS_FOR_LINE + LINE_SEPARATOR_SIZE));
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
        
        if (y <= topOffset()) {
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
                if (description != null) {
                    // #122422 - some descriptions are intentionaly a valid HTML and don't want to be escaped
                    if (description.startsWith(HTML_PREFIX_LOWERCASE) || description.startsWith(HTML_PREFIX_UPPERCASE)) {
                        return description;
                    } else {
                        return "<html><body>" + translate(description); // NOI18N
                    }
                }
            }
        }
        
        return null;
    }
    
    private static final String HTML_PREFIX_LOWERCASE = "<html"; //NOI18N
    private static final String HTML_PREFIX_UPPERCASE = "<HTML"; //NOI18N
    private static String[] c = new String[] {"&", "<", ">", "\n", "\""}; // NOI18N
    private static String[] tags = new String[] {"&amp;", "&lt;", "&gt;", "<br>", "&quot;"}; // NOI18N
    
    private String translate(String input) {
        for (int cntr = 0; cntr < c.length; cntr++) {
            input = input.replaceAll(c[cntr], tags[cntr]);
        }
        
        return input;
    }

    public void foldHierarchyChanged(FoldHierarchyEvent evt) {
        //fix for #63402: clear the modelToViewCache after folds changed:
        //#64498: do not take monitor on this here:
        fullRepaint(false, true);
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
    
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJComponent() {
                public AccessibleRole getAccessibleRole() {
                    return AccessibleRole.PANEL;
                }
            };
            accessibleContext.setAccessibleName(NbBundle.getMessage(AnnotationView.class, "ACSN_AnnotationView")); //NOI18N
            accessibleContext.setAccessibleDescription(NbBundle.getMessage(AnnotationView.class, "ACSD_AnnotationView")); //NOI18N
        }
        return accessibleContext;
    }
}
