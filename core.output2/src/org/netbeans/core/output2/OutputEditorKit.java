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
 * OutputEditorKit.java
 *
 * Created on May 9, 2004, 4:34 PM
 */

package org.netbeans.core.output2;

import java.awt.Rectangle;
import javax.swing.JEditorPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.openide.ErrorManager;

/**
 * A simple editor kit which provides instances of ExtPlainView/ExtWrappedPlainView as its views.
 *
 * @author  Tim Boudreau
 */
final class OutputEditorKit extends DefaultEditorKit implements javax.swing.text.ViewFactory, ChangeListener {
    private final boolean wrapped;
    private final JTextComponent comp;

    /** Creates a new instance of OutputEditorKit */
    public OutputEditorKit(boolean wrapped, JTextComponent comp) {
        this.comp = comp;
        this.wrapped = wrapped;
    }

    public WrappedTextView view() {
        return lastWrappedView;
    }

    private WrappedTextView lastWrappedView = null;
    public javax.swing.text.View create(Element element) {
        javax.swing.text.View result =
                wrapped ? (javax.swing.text.View) new WrappedTextView(element, comp) :
                (javax.swing.text.View) new ExtPlainView (element, comp);
        lastWrappedView = wrapped ? (WrappedTextView) result : null;
        if (wrapped) {
            lastWrappedView.updateInfo(null);
        }
        return result;
    }

    public javax.swing.text.ViewFactory getViewFactory() {
        return this;
    }

    public boolean isWrapped() {
        return wrapped;
    }
    
    public void install(JEditorPane c) {
        super.install(c);
        if (wrapped) {
            c.getCaret().addChangeListener(this);
        }
    }
    
    public void deinstall(JEditorPane c) {
        super.deinstall(c);
        if (wrapped) {
            c.getCaret().removeChangeListener(this);
        }
    }    
    
    private int lastMark = -1;
    private int lastDot = -1;
    private static final Rectangle scratch = new Rectangle();
    
    /**
     * Manages repainting when the selection changes
     */
    public void stateChanged (ChangeEvent ce) {
        int mark = comp.getSelectionStart();
        int dot = comp.getSelectionEnd();
        boolean hasSelection = mark != dot;
        boolean hadSelection = lastMark != lastDot;
        
//        System.err.println("Change: " + mark + " : " + dot + "/" + lastMark + ":" + lastDot + " hadSelection " + hadSelection + " hasSelection " + hasSelection);
        
        if (lastMark != mark || lastDot != dot) {
            int begin = Math.min (mark, dot);
            int end = Math.max (mark, dot);
            int oldBegin = Math.min (lastMark, lastDot);
            int oldEnd = Math.max (lastMark, lastDot);
            
            if (hadSelection && hasSelection) {
                if (begin != oldBegin) {
                    int startChar = Math.min (begin, oldBegin);
                    int endChar = Math.max (begin, oldBegin);
                    repaintRange (startChar, endChar);
                } else {
                    int startChar = Math.min (end, oldEnd);
                    int endChar = Math.max (end, oldEnd);
                    repaintRange (startChar, endChar);
                }
            } else if (hadSelection && !hasSelection) {
                repaintRange (oldBegin, oldEnd);
            } 
            
        }
        lastMark = mark;
        lastDot = dot;
    }
    
    private void repaintRange (int start, int end) {
        try {
            Rectangle r = (Rectangle) view().modelToView(end, scratch, Position.Bias.Forward);
            int y1 = r.y + r.height;
            r = (Rectangle) view().modelToView(start, scratch, Position.Bias.Forward);
            r.x = 0;
            r.width = comp.getWidth();
            r.height = y1 - r.y;
//            System.err.println("RepaintRange " + start + " to " + end + ": " + r);
            comp.repaint (r);
        } catch (BadLocationException e) {
            comp.repaint();
            ErrorManager.getDefault().notify(e);
        }
    }
}
