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
package org.netbeans.core.output2;

import java.awt.event.MouseEvent;
import org.netbeans.core.output2.ui.AbstractOutputPane;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;


class OutputPane extends AbstractOutputPane implements ComponentListener {
    protected void documentChanged() {
        super.documentChanged();
        findOutputTab().documentChanged();
    }

    protected void caretEnteredLine(int line) {
        findOutputTab().caretEnteredLine(line);
    }

    protected void lineClicked(int line) {
        findOutputTab().lineClicked(line);
    }

    protected void postPopupMenu(Point p, Component src) {
        findOutputTab().postPopupMenu(p, src);
    }

    protected boolean shouldRelockScrollBar(int currVal) {
        return findOutputTab().shouldRelockScrollBar(currVal);
    }

    public void setMouseLine (int line) {
        if (line != getMouseLine()) {
            Document doc = getDocument();
            if (doc instanceof OutputDocument) {
                boolean link = line != -1 && ((OutputDocument) doc).isHyperlink(line);
                textView.setCursor(link ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) :
                        Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
            }
        }
        super.setMouseLine(line);
    }
    
    
    /**
     * Only calls super if there are hyperlinks in the document to avoid huge
     * numbers of calls to viewToModel if the cursor is never going to be 
     * changed anyway.
     */
    public void mouseMoved (MouseEvent evt) {
        Document doc = getDocument();
        if (doc instanceof OutputDocument) {
            if (((OutputDocument) doc).hasHyperlinks()) {
                super.mouseMoved(evt);
            }
        }
    }

    private OutputTab findOutputTab() {
        return  (OutputTab) SwingUtilities.getAncestorOfClass (OutputTab.class, this);
    }

    protected void setDocument (Document doc) {
        if (doc == null) {
            Document d = getDocument();
            if (d != null) {
                d.removeDocumentListener(this);
            }
            textView.setDocument (new PlainDocument());
            return;
        }
        textView.setEditorKit (new OutputEditorKit(isWrapped(), textView));
        super.setDocument(doc);
        updateKeyBindings();
    }
    
    private static boolean wrapByDefault = Boolean.getBoolean("nb.output.wrap"); //NOI18N
    public void setWrapped (boolean val) {
        if (val != isWrapped() || !(getEditorKit() instanceof OutputEditorKit)) {
            wrapByDefault = val;
            final int pos = textView.getCaret().getDot();
            setEditorKit (new OutputEditorKit(val, textView));
            if (val) {
                getViewport().addChangeListener(this);
            } else {
                getViewport().removeChangeListener(this);
            }
            
            //Don't try to set the caret position until the view has
            //been fully readjusted to its new dimensions, scroll bounds, etc.
            SwingUtilities.invokeLater (new Runnable() {
                private boolean first = true;
                public void run() {
                    if (first) {
                        first = false;
                        SwingUtilities.invokeLater(this);
                        return;
                    }
                    textView.getCaret().setDot(pos);
                }
            });
            if (getDocument() instanceof OutputDocument && ((OutputDocument) getDocument()).stillGrowing()) {
                lockScroll();
            }
            validate();
        }
    }
    
    public boolean isWrapped() {
        if (getEditorKit() instanceof OutputEditorKit) {
            return getEditorKit() instanceof OutputEditorKit 
              && ((OutputEditorKit) getEditorKit()).isWrapped();
        } else {
            return wrapByDefault;
        }
    }
    
    protected JEditorPane createTextView() {
        JEditorPane result = new JEditorPane();
        result.addComponentListener(this);
        
        // we don't want the background to be gray even though the text there is not editable
        result.setDisabledTextColor(result.getBackground());
        
        return result;
    }

    private int prevW = -1;
    public void componentResized(ComponentEvent e) {
        int w = textView.getWidth();
        if (prevW != w) {
            if (isWrapped()) {
                WrappedTextView view = ((OutputEditorKit) getEditorKit()).view();
                if (view != null) {
                    view.setChanged(true);
                    textView.repaint();
                }
            }
        }
        prevW = w;
    }

    public void componentMoved(ComponentEvent e) {
        //do nothing
    }

    public void componentShown(ComponentEvent e) {
        //do nothing
    }

    public void componentHidden(ComponentEvent e) {
        //do nothing
    }
    
    protected int getWrappedHeight() {
        WrappedTextView view = ((OutputEditorKit) getEditorKit()).view();
        if (view != null) {
            return (int) view.getMinimumSpan (WrappedTextView.Y_AXIS);
        }
        return textView.getHeight();
        
    }

    protected boolean shouldRelock(int dot) {
        return findOutputTab().shouldRelock(dot);
    }

}
