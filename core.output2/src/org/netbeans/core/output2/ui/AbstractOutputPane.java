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
/*
 * OutputPane.java
 *
 * Created on May 14, 2004, 6:45 PM
 */

package org.netbeans.core.output2.ui;

import org.netbeans.core.output2.Controller;
import org.openide.ErrorManager;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A scroll pane containing an editor pane, with special handling of the caret
 * and scrollbar - until a keyboard or mouse event, after a call to setDocument(),
 * the caret and scrollbar are locked to the last line of the document.  This avoids
 * "jumping" scrollbars as the position of the caret (and thus the scrollbar) get updated
 * to reposition them at the bottom of the document on every document change.
 *
 * @author  Tim Boudreau
 */
public abstract class AbstractOutputPane extends JScrollPane implements DocumentListener, MouseListener, MouseMotionListener, KeyListener, ChangeListener, MouseWheelListener {
    private boolean locked = true;
    private SBModel sbmodel = new SBModel();
    private SBModel sbmodel2 = new SBModel(true);
    private OCaret caret = new OCaret();
    private int fontHeight = -1;
    private int fontWidth = -1;
    protected JEditorPane textView;
    int lastCaretLine = 0;
    boolean hadSelection = false;
    boolean recentlyReset = false;

    public AbstractOutputPane() {
        textView = createTextView();
        init();
        setFocusable(false);
    }
    
    public int getFontWidth() {
        return fontWidth;
    }
    
    public int getFontHeight() {
        return fontHeight;
    }
    
    public void requestFocus() {
        textView.requestFocus();
    }
    
    public boolean requestFocusInWindow() {
        return textView.requestFocusInWindow();
    }
    
    protected abstract JEditorPane createTextView();

    protected void documentChanged() {
        lastLength = -1;
        if (recentlyReset && isShowing()) {
            recentlyReset = false;
        }
        if (locked) {
            setMouseLine(-1);
        }
    }
    
    public abstract boolean isWrapped();
    public abstract void setWrapped (boolean val);

    public boolean hasSelection() {
        return textView.getSelectionStart() != textView.getSelectionEnd();
    }

    public final void ensureCaretPosition() {
        if (locked) {            
            sbmodel.fire();
            caret.fire();
            revalidate();
        }
    }

    public int getSelectionStart() {
        return textView.getSelectionStart();
    }

    public void setSelection (int start, int end) {
        int rstart = Math.min (start, end);
        int rend = Math.max (start, end);
        if (rstart == rend) {
            caret.setDot(rstart);
        } else {
            textView.setSelectionStart(rstart);
            textView.setSelectionEnd(rend);
        }
    }

    public void selectAll() {
        unlockScroll();
        caret.setVisible(true);
        textView.setSelectionStart(0);
        textView.setSelectionEnd(getLength());
    }

    public boolean isAllSelected() {
        return textView.getSelectionStart() == 0 && textView.getSelectionEnd() == getLength();
    }

    protected void init() {
        getVerticalScrollBar().setModel(sbmodel);
        setViewportView(textView);
        textView.setCaret(caret);
        textView.setEditable(false);
        textView.setOpaque(false);
        textView.addMouseListener(this);
        textView.addMouseWheelListener(this);
        textView.addMouseMotionListener(this);
        textView.addKeyListener(this);

        getViewport().addMouseListener(this);
        getHorizontalScrollBar().setModel(sbmodel2);
        getHorizontalScrollBar().addMouseListener(this);
        getVerticalScrollBar().addMouseListener(this);
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_AS_NEEDED);
        setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
        addMouseListener(this);
//        setOpaque(false);
//        getViewport().setOpaque(false);
        caret.addChangeListener(this);
        Integer i = (Integer) UIManager.get("customFontSize"); //NOI18N
        int size = 11;
        if (i != null) {
            size = i.intValue();
        }
        textView.setFont (new Font ("Monospaced", Font.PLAIN, size)); //NOI18N
        setBorder (BorderFactory.createEmptyBorder());
        setViewportBorder (BorderFactory.createEmptyBorder());
    }

    public final Document getDocument() {
        return textView.getDocument();
    }

    public final void copy() {
        if (caret.getDot() != caret.getMark()) {
            textView.copy();
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    protected void setDocument (Document doc) {
        if (hasSelection()) {
            hasSelectionChanged(false);
        }
        hadSelection = false;
        lastCaretLine = 0;
        lastLength = -1;
        Document old = textView.getDocument();
        old.removeDocumentListener(this);
        textView.setDocument(doc);
        doc.addDocumentListener(this);
        lockScroll();
        recentlyReset = true;
    }
    
    protected void setEditorKit(EditorKit kit) {
        Document doc = textView.getDocument();
        textView.setEditorKit(kit);
        textView.setDocument(doc);
    }
    
    protected EditorKit getEditorKit() {
        return textView.getEditorKit();
    }
    
    public final int getLineCount() {
        return textView.getDocument().getDefaultRootElement().getElementCount();
    }

    private int lastLength = -1;
    public final int getLength() {
        if (lastLength == -1) {
            lastLength = textView.getDocument().getLength();
        }
        return lastLength;
    }
    
    public final void sendCaretToLine(int idx, boolean select) {
        unlockScroll();
        caret.setVisible(true);
        caret.setSelectionVisible(true);
        Element el = textView.getDocument().getDefaultRootElement().getElement(idx);
        int position = el.getStartOffset();
        if (Controller.log) Controller.log ("Send caret to line " + idx + " select: " + select + " position " + position);
        if (select) {
            caret.setDot (el.getEndOffset()-1);
            caret.moveDot (position);
            caret.setSelectionVisible(true);
            textView.repaint();
        } else {
            caret.setDot(position);
            //Less lines if wrapped - could end up out of view
            if (idx + (isWrapped() ? 1 : 3) < getLineCount()) {
                //Ensure a little more than the requested line is in view
                try {
                    Rectangle r = textView.modelToView(textView.getDocument().getDefaultRootElement().getElement (idx + 3).getStartOffset());
                    if (Controller.log) Controller.log ("Trying to ensure some lines below the new caret line are visible - scrolling into view " + r);
                    if (r != null) { //Will be null if maximized - no parent, no coordinate space
                        textView.scrollRectToVisible(r);
                    }
                } catch (BadLocationException ble) {
                    ErrorManager.getDefault().notify(ble);
                }
            }
        }
    }
    
    protected abstract int getWrappedHeight();
    
    public final void lockScroll() {
        if (!locked) {
            locked = true;
            caret.locked();
            sbmodel.locked();
            sbmodel2.locked();
        }
    }
    
    public final void unlockScroll() {
        if (locked) {
            sbmodel.beforeRelease();
            caret.beforeRelease();
            sbmodel2.beforeRelease();
            locked = false;
            sbmodel.released();
            sbmodel2.released();
            caret.released();
        }
    }

    protected abstract boolean shouldRelock(int dot);

    protected abstract void caretEnteredLine (int line);
    
    protected abstract void lineClicked (int line);
    
    protected abstract void postPopupMenu (Point p, Component src);
    
    public final int getCaretLine() {
        int result = -1;
        int charPos = caret.getDot();
        if (charPos > 0) {
            result = textView.getDocument().getDefaultRootElement().getElementIndex(charPos);
        }
        return result;
    }

    public final int getCaretPos() {
        return caret.getDot();
    }

    public final void paint (Graphics g) {
        if (fontHeight == -1) {
            fontHeight = g.getFontMetrics(textView.getFont()).getHeight();
            fontWidth = g.getFontMetrics(textView.getFont()).charWidth('m'); //NOI18N
        }
        super.paint(g);
    }


    protected abstract boolean shouldRelockScrollBar(int currVal);

    private final class SBModel implements BoundedRangeModel {
        private ArrayList list = new ArrayList();
        private ChangeEvent evt = new ChangeEvent(this);
        private boolean adjusting = false;
        public int value = 0;
        private boolean horizontal = false;

        public SBModel() {

        }

        public SBModel(boolean horiz) {
            this.horizontal = horiz;
        }


        public void locked() {
            fire();
        }

        public void beforeRelease() {
            value = horizontal ? 0 : getMaximum();
        }

        public void released() {
            fire();
        }

        public synchronized void addChangeListener(ChangeListener changeListener) {
            list.add(changeListener);
        }

        public synchronized void removeChangeListener(ChangeListener changeListener) {
            list.remove(changeListener);
        }

        public synchronized void fire() {
            for (Iterator i = list.iterator(); i.hasNext();) {
                ((ChangeListener)i.next()).stateChanged(evt);
            }
        }

        public int getExtent() {
            return horizontal ? AbstractOutputPane.this.getViewport().getExtentSize().width
                    : AbstractOutputPane.this.getViewport().getExtentSize().height;
        }

        public int getMaximum() {
            return horizontal ?
                  textView.getWidth()
                : isWrapped() ?
                getWrappedHeight() :
                fontHeight * getLineCount();
        }

        public int getMinimum() {
            return 0;
        }

        public int getValue() {
            return locked ? horizontal ? getMinimum() : getMaximum() : value;
        }

        public boolean getValueIsAdjusting() {
            return adjusting;
        }

        public void setExtent(int val) {
        }

        public void setMaximum(int param) {
        }

        public void setMinimum(int param) {
        }

        public void setRangeProperties(int value, int extent, int min, int max, boolean adjusting) {
            setValue(value);
        }

        public void setValue(int val) {
            value = val;
            if (!locked) {
                fire();
            }
        }

        public void setValueIsAdjusting(boolean val) {
            boolean wasAdjusting = adjusting;
            if (!horizontal) {
                if (!val && shouldRelockScrollBar(0) && wasAdjusting) {
                    double end = getMaximum() - getExtent();
                    double percentage = value / end;
                    if (Controller.log) Controller.log ("Trying to reacquire scroll lock " +
                            "for end " + end + " val " + value + " perc " + percentage);
                    if (percentage >= 0.95) {
                        lockScroll();
                    }

                }
            }
            adjusting = val;

        }
    }

    /**
     * A Caret that can be locked to the bottom of the document - if we were to
     * set its position on each document change, the scrollbar would be constantly
     * jumping, out of sync with repaints of the document, and everything would be
     * doing more work, and run slower.
     */
    private final class OCaret extends DefaultCaret {

        public void fire() {
            super.fireStateChanged();
        }

        protected void fireStateChanged() {
            if (locked) {
                return;
            }
            fire();
        }

        public void locked() {
            fire();
        }

        public void beforeRelease() {
            //Some machinations to ensure the correct state
            super.setSelectionVisible(false);
            super.setSelectionVisible(true);
            super.setDot(getLength()-1);
        }

        public void released() {
            fire();
        }

        public int getDot() {
            return locked ? getLength()-1 : super.getDot();
        }

        public void moveDot(int amt) {
            if (!locked) {
                super.moveDot(amt);
            }
        }

        public void setDot (int dot) {
            super.setDot (dot);
        }

        public int getMark() {
            return locked ? getDot() : super.getMark();
        }

        public boolean isSelectionVisible() {
            return locked ? textView.hasFocus() : true;
        }

        public boolean isVisible() {
            return locked ? textView.hasFocus() : textView.hasFocus() || super.isVisible();
        }

        public void setBlinkRate(int rate) {
            //Do nothing - Aqua UI furiously tries to repaint even though
            //we return 0 for the blink rate, so block it
        }

        protected void moveCaret(MouseEvent e) {
            int oldDot = getDot();
            int oldMark = getMark();
            super.moveCaret(e);
            if (oldDot != oldMark) {
                repaintFormerSelection (oldDot, oldMark);
            }
        }

        protected void positionCaret(MouseEvent e) {
            int oldDot = getDot();
            int oldMark = getMark();
            super.positionCaret(e);
            if (oldDot != oldMark) {
                repaintFormerSelection (oldDot, oldMark);
            }
        }


        /**
         * We are doing our own painting when word wrapped, and WrappedTextView attaches no
         * listeners to anything, so we repaint the former selection ourselves.
         * @param oldStart The former selection start character
         * @param oldEnd The former selection end character
         */
        private void repaintFormerSelection (int oldStart, int oldEnd) {
            if (isWrapped()) {
                try {
                    Rectangle oldSelStart = textView.modelToView(Math.min(oldEnd, oldStart));
                    Rectangle oldSelEnd = textView.modelToView(Math.max(oldEnd, oldStart));
                    Rectangle toRepaint = new Rectangle (
                        0,
                        oldSelStart.y,
                        textView.getWidth(),
                        (oldSelEnd.y + oldSelEnd.height) - oldSelStart.y
                    );
                    textView.repaint (toRepaint);
                } catch (BadLocationException e) {
                    ErrorManager.getDefault().notify(e);
                    textView.repaint();
                }
            }
        }
    }


//***********************Listener implementations*****************************

    public void stateChanged(ChangeEvent e) {
        if (e.getSource() instanceof JViewport) {
            if (locked) {
//                sbmodel.fire();
            }
        } else {        
            maybeSendCaretEnteredLine();
        }
    }

    private boolean caretLineChanged() {
        int line = getCaretLine();
        boolean result = line != lastCaretLine;
        lastCaretLine = line;
        return result;
    }

    private void maybeSendCaretEnteredLine() {
        if (!locked && caretLineChanged()) {
            int line = getCaretLine();
            boolean sel = textView.getSelectionStart() != textView.getSelectionEnd();
            if (line != -1 && !sel) {
                caretEnteredLine(getCaretLine());
            }
            if (isWrapped()) {
                //We need to force a repaint to erase all of the old selection
                //if we're doing our own painting
                int dot = caret.getDot();
                int mark = caret.getMark();
                if ((((dot > mark) != (lastKnownDot > lastKnownMark)) && !(lastKnownDot == lastKnownMark)) || ((lastKnownDot == lastKnownMark) != (dot == mark))){
                    int begin = Math.min (Math.min(lastKnownDot, lastKnownMark), Math.min(dot, mark));
                    int end = Math.max (Math.max(lastKnownDot, lastKnownMark), Math.max (dot, mark));
                    caret.repaintFormerSelection(Math.min(
                        lastKnownDot, lastKnownMark), 
                        Math.max(lastKnownDot, lastKnownMark));
                }
            }
            if (sel != hadSelection) {
                hadSelection = sel;
                hasSelectionChanged (sel);
            }
        }
        lastKnownMark = caret.getMark();
        lastKnownDot = caret.getDot();
    }
    
    private int lastKnownMark = -1;
    private int lastKnownDot = -1;

    private void hasSelectionChanged(boolean sel) {
        ((AbstractOutputTab) getParent()).hasSelectionChanged(sel);
    }

    public final void changedUpdate(DocumentEvent e) {
        //Ensure it is consumed
        e.getLength();
        ensureCaretPosition();
        documentChanged();
    }

    public final void insertUpdate(DocumentEvent e) {
        //Ensure it is consumed
        e.getLength();
        ensureCaretPosition();
        documentChanged();
    }

    public final void removeUpdate(DocumentEvent e) {
        //Ensure it is consumed
        e.getLength();
        ensureCaretPosition();
        documentChanged();
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
        setMouseLine (-1);
    }

    private int mouseLine = -1;
    public void setMouseLine (int line) {
        if (mouseLine != line) {
            mouseLine = line;
        }
    }

    public int getMouseLine() {
        return mouseLine;
    }


    public void mouseMoved(MouseEvent e) {
        if (isWrapped() && getLength() > 100000) {
            //Getting the position iterates the entire line count
            //in wrapped mode, at least for now.  Just don't adjust
            //cursor for links at the end of huge files, it's too
            //expensive
            return;
        }
        Point p = e.getPoint();
        int pos = textView.viewToModel(p);
        if (pos < getLength()) {
            int line = getDocument().getDefaultRootElement().getElementIndex(pos);
            int lineStart = getDocument().getDefaultRootElement().getElement(line).getStartOffset();
            int lineLength = getDocument().getDefaultRootElement().getElement(line).getEndOffset() -
                    lineStart;

            int left = getInsets().left;
            int maxX = left + (fontWidth * lineLength);
//            System.err.println ("maxX " + maxX + " x " + p.x + " lineLength " + lineLength + " pos " + pos + " lineStart " + lineStart);

            if (p.x <= maxX) {
                setMouseLine (line);
            } else {
                setMouseLine(-1);
            }
        }
    }

    public void mouseDragged(MouseEvent e) {

    }

    public final void mousePressed(MouseEvent e) {
        if (locked) {
            Element el = getDocument().getDefaultRootElement().getElement(getLineCount()-1);
            caret.setDot(el.getStartOffset());
            unlockScroll();
            //We should now set the caret position so the caret doesn't
            //seem to ignore the first click
            if (e.getSource() == textView) {
                caret.setDot (textView.viewToModel(e.getPoint()));
            }
        }
        if (e.isPopupTrigger()) {
            //Convert immediately to our component space - if the 
            //text view scrolls before the component is opened, popup can
            //appear above the top of the screen
            Point p = SwingUtilities.convertPoint((Component) e.getSource(), 
                e.getPoint(), this);
            
            postPopupMenu (p, this);
        }
    }

    public final void mouseReleased(MouseEvent e) {
        if (e.getSource() == textView && SwingUtilities.isLeftMouseButton(e)) {
            int pos = textView.viewToModel(e.getPoint());
            if (pos != -1) {
                int line = textView.getDocument().getDefaultRootElement().getElementIndex(pos);
                if (line > 0) {
                    lineClicked(line);
                }
            }
        }
        if (e.isPopupTrigger()) {
            Point p = SwingUtilities.convertPoint((Component) e.getSource(), 
            //Convert immediately to our component space - if the 
            //text view scrolls before the component is opened, popup can
            //appear above the top of the screen
                e.getPoint(), this);
            
            postPopupMenu (p, this);
        }
    }
    
    public void keyPressed(KeyEvent keyEvent) {
        unlockScroll();
    }

    public void keyReleased(KeyEvent keyEvent) {
    }

    public void keyTyped(KeyEvent keyEvent) {
    }

    public final void mouseWheelMoved(MouseWheelEvent e) {
        int currPosition = sbmodel.getValue();
        unlockScroll();
        if (e.getSource() == textView) {
            int newPosition = Math.max (0, Math.min (sbmodel.getMaximum(),
                currPosition + (e.getUnitsToScroll() * (sbmodel.getExtent() / 4))));
            sbmodel.setValue (newPosition);
        }
    }
}
