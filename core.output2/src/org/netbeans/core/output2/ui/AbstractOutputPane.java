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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.output2.ui;

import java.awt.Rectangle;
import javax.swing.plaf.TextUI;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import org.netbeans.core.output2.OutputDocument;
import org.openide.util.Exceptions;

/**
 * A scroll pane containing an editor pane, with special handling of the caret
 * and scrollbar - until a keyboard or mouse event, after a call to setDocument(),
 * the caret and scrollbar are locked to the last line of the document.  This avoids
 * "jumping" scrollbars as the position of the caret (and thus the scrollbar) get updated
 * to reposition them at the bottom of the document on every document change.
 *
 * @author  Tim Boudreau
 */
public abstract class AbstractOutputPane extends JScrollPane implements DocumentListener, MouseListener, MouseMotionListener, KeyListener, ChangeListener, MouseWheelListener, Runnable {
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

    @Override
    public void requestFocus() {
        textView.requestFocus();
    }
    
    @Override
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
        if (isWrapped()) {
            //Saves having OutputEditorKit have to do its own listening
            getViewport().revalidate();
            getViewport().repaint();
        }
    }
    
    public abstract boolean isWrapped();
    public abstract void setWrapped (boolean val);

    public boolean hasSelection() {
        return textView.getSelectionStart() != textView.getSelectionEnd();
    }

    boolean isScrollLocked() {
        return locked;
    }

    /**
     * Ensure that the document is scrolled all the way to the bottom (unless
     * some user event like scrolling or placing the caret has unlocked it).
     * <p>
     * Note that this method is always called on the event queue, since 
     * OutputDocument only fires changes on the event queue.
     */
    public final void ensureCaretPosition() {
        if (locked) {           
            //Make sure the scrollbar is updated *after* the document change
            //has been processed and the scrollbar model's maximum updated
            if (!enqueued) {
                SwingUtilities.invokeLater(this);
                enqueued = true;
            }
        }
    }
    
    /** True when invokeLater has already been called on this instance */
    private boolean enqueued = false;
    /**
     * Scrolls the pane to the bottom, invokeLatered to ensure all state has
     * been updated, so the bottom really *is* the bottom.
     */
    public void run() {
        enqueued = false;
        getVerticalScrollBar().setValue(getVerticalScrollBar().getModel().getMaximum());
        getHorizontalScrollBar().setValue(getHorizontalScrollBar().getModel().getMinimum());
    }

    public int getSelectionStart() {
        return textView.getSelectionStart();
    }
    
    public int getSelectionEnd() {
        return textView.getSelectionEnd();
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
        //#107354
        OCaret oc = new OCaret();
        oc.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        textView.setCaret (oc);
        
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
        int size;
        if (i != null) {
            size = i.intValue();
        } else {
            Font f = (Font) UIManager.get("controlFont");
            size = f != null ? f.getSize() : 11;
        }
        textView.setFont (new Font ("Monospaced", Font.PLAIN, size)); //NOI18N
        setBorder (BorderFactory.createEmptyBorder());
        setViewportBorder (BorderFactory.createEmptyBorder());
        
        Color c = UIManager.getColor("nb.output.selectionBackground");
        if (c != null) {
            textView.setSelectionColor(c);
        }
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
        if (doc != null) {
            textView.setDocument(doc);
            doc.addDocumentListener(this);
            lockScroll();
            recentlyReset = true;
            pendingCaretLine = -1;
        } else {
            textView.setDocument (new PlainDocument());
            textView.setEditorKit(new DefaultEditorKit());
        }
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
                getCaret().setDot(position);
            }
            if (idx + 3 < getLineCount()) {
                try {
                    Rectangle r = textView.modelToView(textView.getDocument().getDefaultRootElement().getElement(idx + 3).getStartOffset());
                    if (r != null) { //Will be null if maximized - no parent, no coordinate space
                        textView.scrollRectToVisible(r);
                    }
                } catch (BadLocationException ble) {
                    Exceptions.printStackTrace(ble);
                }
            }
            inSendCaretToLine = false;
            return true;
        }
    }


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

    protected abstract void caretEnteredLine (int line);
    
    protected abstract void lineClicked (int line, Point p);
    
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

    @Override
    public final void paint (Graphics g) {
        if (fontHeight == -1) {
            fontHeight = g.getFontMetrics(textView.getFont()).getHeight();
            fontWidth = g.getFontMetrics(textView.getFont()).charWidth('m'); //NOI18N
        }
        super.paint(g);
    }

//***********************Listener implementations*****************************

    public void stateChanged(ChangeEvent e) {
        if (e.getSource() instanceof JViewport) {
            if (locked) {
                ensureCaretPosition();
            }
        } else if (e.getSource() == getVerticalScrollBar().getModel()) {
            if (!locked) { //XXX check if doc is still being written?
                BoundedRangeModel mdl = getVerticalScrollBar().getModel();
                if (mdl.getValue() + mdl.getExtent() == mdl.getMaximum()) {
                    lockScroll();
                }
            }
        } else {
            if (!locked) {
                maybeSendCaretEnteredLine();
            }
            boolean hasSelection = textView.getSelectionStart() != textView.getSelectionEnd();
            if (hasSelection != hadSelection) {
                hadSelection = hasSelection;
                hasSelectionChanged (hasSelection);
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
        if (EventQueue.getCurrentEvent() instanceof MouseEvent) {
            //User may have clicked a hyperlink, in which case, we'll test
            //it and see if it's really in the text of the hyperlink - so
            //don't do anything here
            return;
        }
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
            if (sel != hadSelection) {
                hadSelection = sel;
                hasSelectionChanged (sel);
            }
        }
    }


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
        if (e.getOffset() > getCaretPos() && (locked || !(e instanceof OutputDocument.DO))) {
            getCaret().setDot(e.getOffset() + e.getLength());
        }
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
    public void setMouseLine (int line, Point p) {
        if (mouseLine != line) {
            mouseLine = line;
        }
    }
    
    public final void setMouseLine (int line) {
        setMouseLine (line, null);
    }


    public void mouseMoved(MouseEvent e) {
        Point p = e.getPoint();
        int pos = textView.viewToModel(p);
        if (pos < getLength()) {
            int line = getDocument().getDefaultRootElement().getElementIndex(pos);
            int lineStart = getDocument().getDefaultRootElement().getElement(line).getStartOffset();
            int lineLength = getDocument().getDefaultRootElement().getElement(line).getEndOffset() -
                    lineStart;

            try {
                Rectangle r = textView.modelToView(lineStart + lineLength -1);
                int maxX = r.x + r.width;
                boolean inLine = p.x <= maxX;
                if (isWrapped()) {
                    Rectangle ra = textView.modelToView(lineStart);
                    if (ra.y <= r.y) {
                        if (p.y < r.y) {
                            inLine = true;
                        }
                    }
                }
                
                if (inLine) {
                    setMouseLine (line, p);
                } else {
                    setMouseLine(-1);
                }
            } catch (BadLocationException ble) {
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
    
    public void mousePressed(MouseEvent e) {
        if (locked && !e.isPopupTrigger()) {
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
                if (line >= 0) {
                    lineClicked(line, e.getPoint());
                    e.consume(); //do NOT allow this window's caret to steal the focus from editor window
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
        int max = sbmodel.getMaximum();
        int range = sbmodel.getExtent();

        int currPosition = sbmodel.getValue();
        if (e.getSource() == textView) {
            int newPosition = Math.max (0, Math.min (sbmodel.getMaximum(),
                currPosition + (e.getUnitsToScroll() * textView.getFontMetrics(textView.getFont()).getHeight())));
            // height is a magic constant because of #57532
            sbmodel.setValue (newPosition);
            if (newPosition + range >= max) {
                lockScroll();
                return;
            }
        }
        unlockScroll();
    }

    Caret getCaret() {
        return textView.getCaret();
    }
    
    private class OCaret extends DefaultCaret {
        @Override
        public void setSelectionVisible(boolean val) {
            super.setSelectionVisible(true);
            super.setBlinkRate(0);
        }
        @Override
        public boolean isSelectionVisible() {
            return true;
        }
        @Override
        public void setBlinkRate(int rate) {
            super.setBlinkRate(0);
        }
 
        @Override
        public boolean isVisible() { return true; }
        
        @Override
        public void paint(Graphics g) {
            JTextComponent component = textView;
            if(isVisible() && y >= 0) {
                try {
                    TextUI mapper = component.getUI();
                    Rectangle r = mapper.modelToView(component, getDot(), Position.Bias.Forward);

                    if ((r == null) || ((r.width == 0) && (r.height == 0))) {
                        return;
                    }
                    if (width > 0 && height > 0 &&
                                    !this._contains(r.x, r.y, r.width, r.height)) {
                        // We seem to have gotten out of sync and no longer
                        // contain the right location, adjust accordingly.
                        Rectangle clip = g.getClipBounds();

                        if (clip != null && !clip.contains(this)) {
                            // Clip doesn't contain the old location, force it
                            // to be repainted lest we leave a caret around.
                            repaint();
                        }
 //                       System.err.println("WRONG! Caret dot m2v = " + r + " but my bounds are " + x + "," + y + "," + width + "," + height);
                        
                        // This will potentially cause a repaint of something
                        // we're already repainting, but without changing the
                        // semantics of damage we can't really get around this.
                        damage(r);
                    }
                    g.setColor(component.getCaretColor());
                    g.drawLine(r.x, r.y, r.x, r.y + r.height - 1);
                    g.drawLine(r.x+1, r.y, r.x+1, r.y + r.height - 1);

                } catch (BadLocationException e) {
                    // can't render I guess
//                    System.err.println("Can't render cursor");
                }
            }
        }    
        
        private boolean _contains(int X, int Y, int W, int H) {
            int w = this.width;
            int h = this.height;
            if ((w | h | W | H) < 0) {
                // At least one of the dimensions is negative...
                return false;
            }
            // Note: if any dimension is zero, tests below must return false...
            int x = this.x;
            int y = this.y;
            if (X < x || Y < y) {
                return false;
            }
            if (W > 0) {
                w += x;
                W += X;
                if (W <= X) {
                    // X+W overflowed or W was zero, return false if...
                    // either original w or W was zero or
                    // x+w did not overflow or
                    // the overflowed x+w is smaller than the overflowed X+W
                    if (w >= x || W > w) {
                        return false;
                    }
                } else {
                    // X+W did not overflow and W was not zero, return false if...
                    // original w was zero or
                    // x+w did not overflow and x+w is smaller than X+W
                    if (w >= x && W > w) {
                        //This is the bug in DefaultCaret - returns false here
                        return true;
                    }
                }
            }
            else if ((x + w) < X) {
                return false;
            }
            if (H > 0) {
                h += y;
                H += Y;
                if (H <= Y) {
                    if (h >= y || H > h) return false;
                } else {
                    if (h >= y && H > h) return false;
                }
            }
            else if ((y + h) < Y) {
                return false;
            }
            return true;
        }        

        @Override
        public void mouseReleased(MouseEvent e) {
            if( !e.isConsumed() ) {
                super.mouseReleased(e);
            }
        }
    }
}
