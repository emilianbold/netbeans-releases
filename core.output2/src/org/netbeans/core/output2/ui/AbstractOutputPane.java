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
import org.openide.util.Lookup;

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
    
    private int fontHeight = -1;
    private int fontWidth = -1;
    protected JEditorPane textView;
    int lastCaretLine = 0;
    boolean hadSelection = false;
    boolean recentlyReset = false;

    public AbstractOutputPane() {
        textView = createTextView();
        init();
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
        if (pendingCaretLine != -1) {
            if (!sendCaretToLine (pendingCaretLine, pendingCaretSelect)) {
                ensureCaretPosition();
            }
        } else {
            ensureCaretPosition();
        }
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

    private static final Rectangle rect = new Rectangle();
    public final void ensureCaretPosition() {
        if (locked) {            
            rect.setBounds(0, textView.getHeight() - 2, 1, 1);
            textView.scrollRectToVisible(
                rect);
        }
    }

    public int getSelectionStart() {
        return textView.getSelectionStart();
    }

    public void setSelection (int start, int end) {
        int rstart = Math.min (start, end);
        int rend = Math.max (start, end);
        if (rstart == rend) {
            getCaret().setDot(rstart);
        } else {
            textView.setSelectionStart(rstart);
            textView.setSelectionEnd(rend);
        }
    }

    public void selectAll() {
        unlockScroll();
        getCaret().setVisible(true);
        textView.setSelectionStart(0);
        textView.setSelectionEnd(getLength());
    }

    public boolean isAllSelected() {
        return textView.getSelectionStart() == 0 && textView.getSelectionEnd() == getLength();
    }

    protected void init() {
        setViewportView(textView);
        textView.setEditable(false);

        textView.addMouseListener(this);
        textView.addMouseWheelListener(this);
        textView.addMouseMotionListener(this);
        textView.addKeyListener(this);
        textView.setCaret (new OCaret());
        
        getCaret().setVisible(true);
        getCaret().setBlinkRate(0);
        getCaret().setSelectionVisible(true);
        
        getVerticalScrollBar().getModel().addChangeListener(this);
        getVerticalScrollBar().addMouseMotionListener(this);
        
        getViewport().addMouseListener(this);
        getVerticalScrollBar().addMouseListener(this);
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_AS_NEEDED);
        setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
        addMouseListener(this);

        getCaret().addChangeListener(this);
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
    
    /**
     * This method is here for use *only* by unit tests.
     */
    public final JTextComponent getTextView() {
        return textView;
    }

    public final void copy() {
        if (getCaret().getDot() != getCaret().getMark()) {
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
        pendingCaretLine = -1;
    }
    
    protected void setEditorKit(EditorKit kit) {
        Document doc = textView.getDocument();
        
        textView.setEditorKit(kit);
        textView.setDocument(doc);
        updateKeyBindings();
        getCaret().setVisible(true);
        getCaret().setBlinkRate(0);
    }
    
    /**
     * Setting the editor kit will clear the action map/key map connection
     * to the TopComponent, so we reset it here.
     */
    protected final void updateKeyBindings() {
        Keymap keymap = textView.getKeymap();
        keymap.removeKeyStrokeBinding(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
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
    
    /**
     * If we are sending the caret to a hyperlinked line, but it is < 3 lines
     * from the bottom, we will hold the line number in this field until there
     * are enough lines that it will be semi-centered.
     */
    private int pendingCaretLine = -1;
    private boolean pendingCaretSelect = false;
    private boolean inSendCaretToLine = false;
    
    public final boolean sendCaretToLine(int idx, boolean select) {
        int count = getLineCount();
        if (count - idx < 3) {
            pendingCaretLine = idx;
            pendingCaretSelect = select;
            return false;
        } else {
            inSendCaretToLine = true;
            pendingCaretLine = -1;
            unlockScroll();
            getCaret().setVisible(true);
            getCaret().setSelectionVisible(true);
            Element el = textView.getDocument().getDefaultRootElement().getElement(Math.min(idx, getLineCount() - 1));
            int position = el.getStartOffset();
            if (select) {
                getCaret().setDot (el.getEndOffset()-1);
                getCaret().moveDot (position);
                getCaret().setSelectionVisible(true);
                textView.repaint();
            } else {            
                if (idx + 3 < getLineCount()) {
                    getCaret().setDot(position);
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
            inSendCaretToLine = false;
            return true;
        }
    }
    
    
    
    protected abstract int getWrappedHeight();
    
    public final void lockScroll() {
        if (!locked) {
            locked = true;
        }
    }
    
    public final void unlockScroll() {
        if (locked) {
            locked = false;
        }
    }

    protected abstract boolean shouldRelock(int dot);

    protected abstract void caretEnteredLine (int line);
    
    protected abstract void lineClicked (int line);
    
    protected abstract void postPopupMenu (Point p, Component src);
    
    public final int getCaretLine() {
        int result = -1;
        int charPos = getCaret().getDot();
        if (charPos > 0) {
            result = textView.getDocument().getDefaultRootElement().getElementIndex(charPos);
        }
        return result;
    }

    public final int getCaretPos() {
        return getCaret().getDot();
    }

    public final void paint (Graphics g) {
        if (fontHeight == -1) {
            fontHeight = g.getFontMetrics(textView.getFont()).getHeight();
            fontWidth = g.getFontMetrics(textView.getFont()).charWidth('m'); //NOI18N
        }
        super.paint(g);
    }


    protected abstract boolean shouldRelockScrollBar(int currVal);


//***********************Listener implementations*****************************

    public void stateChanged(ChangeEvent e) {
        if (e.getSource() instanceof JViewport) {
            if (locked) {
                ensureCaretPosition();
            }
        } else if (e.getSource() == getVerticalScrollBar().getModel()) {
            if (!locked) { //XXX check if doc is still being written?
                BoundedRangeModel mdl = getVerticalScrollBar().getModel();
                if (mdl.getValue() == mdl.getMaximum()) {
                    Thread.dumpStack();
                    lockScroll();
                }
            }
        } else {
            if (!locked) {
                maybeSendCaretEnteredLine();
            }
        }
    }

    private boolean caretLineChanged() {
        int line = getCaretLine();
        boolean result = line != lastCaretLine;
        lastCaretLine = line;
        return result;
    }

    private void maybeSendCaretEnteredLine() {
        //Don't message the controller if we're programmatically setting
        //the selection, or if the caret moved because output was written - 
        //it can cause the controller to send events to OutputListeners which
        //should only happen for user events
        if ((!locked && caretLineChanged()) && !inSendCaretToLine) {
            int line = getCaretLine();
            boolean sel = textView.getSelectionStart() != textView.getSelectionEnd();
            if (line != -1 && !sel) {
                caretEnteredLine(getCaretLine());
            }
            if (isWrapped()) {
                //We need to force a repaint to erase all of the old selection
                //if we're doing our own painting
                int dot = getCaret().getDot();
                int mark = getCaret().getMark();
                if ((((dot > mark) != (lastKnownDot > lastKnownMark)) && !(lastKnownDot == lastKnownMark)) || ((lastKnownDot == lastKnownMark) != (dot == mark))){
                    int begin = Math.min (Math.min(lastKnownDot, lastKnownMark), Math.min(dot, mark));
                    int end = Math.max (Math.max(lastKnownDot, lastKnownMark), Math.max (dot, mark));
                }
            }
            if (sel != hadSelection) {
                hadSelection = sel;
                hasSelectionChanged (sel);
            }
        }
        lastKnownMark = getCaret().getMark();
        lastKnownDot = getCaret().getDot();
    }
    
    private int lastKnownMark = -1;
    private int lastKnownDot = -1;

    private void hasSelectionChanged(boolean sel) {
        ((AbstractOutputTab) getParent()).hasSelectionChanged(sel);
    }

    public final void changedUpdate(DocumentEvent e) {
        //Ensure it is consumed
        e.getLength();
        documentChanged();
    }

    public final void insertUpdate(DocumentEvent e) {
        //Ensure it is consumed
        e.getLength();
        documentChanged();
    }

    public final void removeUpdate(DocumentEvent e) {
        //Ensure it is consumed
        e.getLength();
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
        if (e.getSource() == getVerticalScrollBar()) {
            int y = e.getY();
            if (y > getVerticalScrollBar().getHeight()) {
                lockScroll();
            }
        }
    }

    public final void mousePressed(MouseEvent e) {
        if (locked) {
            Element el = getDocument().getDefaultRootElement().getElement(getLineCount()-1);
            getCaret().setDot(el.getStartOffset());
            unlockScroll();
            //We should now set the caret position so the caret doesn't
            //seem to ignore the first click
            if (e.getSource() == textView) {
                getCaret().setDot (textView.viewToModel(e.getPoint()));
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
        if (keyEvent.getKeyCode() == KeyEvent.VK_END) {
            lockScroll();
        } else {
            unlockScroll();
        }
    }

    public void keyReleased(KeyEvent keyEvent) {
    }

    public void keyTyped(KeyEvent keyEvent) {
    }

    public final void mouseWheelMoved(MouseWheelEvent e) {
        BoundedRangeModel sbmodel = getVerticalScrollBar().getModel();
        int currPosition = sbmodel.getValue();
        unlockScroll();
        if (e.getSource() == textView) {
            int newPosition = Math.max (0, Math.min (sbmodel.getMaximum(),
                currPosition + (e.getUnitsToScroll() * (sbmodel.getExtent() / 4))));
            sbmodel.setValue (newPosition);
        }
    }

    Caret getCaret() {
        return textView.getCaret();
    }
    
    private class OCaret extends DefaultCaret {
        public void setSelectionVisible(boolean val) {
            super.setSelectionVisible(true);
        }
        public boolean isSelectionVisible() {
            return true;
        }
        public void setVisible() {}
        public boolean isVisible() { return true; }
    }

}
