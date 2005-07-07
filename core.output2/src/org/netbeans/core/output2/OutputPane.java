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

import java.awt.Point;
import java.awt.event.MouseEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Segment;
import org.netbeans.core.output2.settings.OutputSettings;
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

    protected void lineClicked(int line, Point p) {
        if (!(getDocument() instanceof OutputDocument) || !inLeadingOrTrailingWhitespace(line, p)) {
            findOutputTab().lineClicked(line);
        }
    }
    
    private boolean linePressed (int line, Point p) {
        boolean result;
        if (!(getDocument() instanceof OutputDocument) || !inLeadingOrTrailingWhitespace(line, p)) {
            result = findOutputTab().linePressed (line, p);
        } else {
            result = false;
        }
        return result;
    }

    protected void postPopupMenu(Point p, Component src) {
        findOutputTab().postPopupMenu(p, src);
    }

    public void setMouseLine (int line, Point p) {
        Document doc = getDocument();
        if (doc instanceof OutputDocument) {
            boolean link = line != -1 && ((OutputDocument) doc).getLines().isHyperlink(line);
            if (link && p != null) {
                //#47256 - Don't set the cursor if the mouse if over
                //whitespace
                if (inLeadingOrTrailingWhitespace(line, p)) {
                    link = false;
                    line = -1;
                }
            }
            textView.setCursor(link ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) :
                    Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        }
        super.setMouseLine(line, p);
    }
    
    private boolean inLeadingOrTrailingWhitespace (int line, Point p) {
        if (line == -1) {
            return true;
        }
        assert getDocument() instanceof OutputDocument;
        assert p != null;
        OutputDocument doc = (OutputDocument) getDocument();
        int lineStart = doc.getLineStart(line);
        int lineEnd = doc.getLineEnd(line);

        try {
            doc.getText (lineStart, lineEnd - lineStart, seg);
            char curr = seg.first();
            while (Character.isWhitespace(curr) && curr != Segment.DONE) {
                lineStart++;
                curr = seg.next();
            }
            curr = seg.last();
            while (Character.isWhitespace(curr) && curr != Segment.DONE) {
                lineEnd--;
                curr = seg.previous();
            }
            if (lineEnd <= lineStart) {
                line = -1;
            } else {
                Rectangle startRect = textView.modelToView(lineStart);
                Rectangle endRect = textView.modelToView(lineEnd);
                if (p.y >= startRect.y && p.y <= endRect.y && isWrapped()) {
                    endRect.x = 0;
                    endRect.width = getWidth();
                }
                boolean cursorIsNotOverLeadingOrTrailingWhitespace = 
                    p.x >= startRect.x && p.y >= startRect.y &&
                    p.x <= endRect.x + endRect.width &&
                    p.y <= endRect.y + endRect.height;
                if (!cursorIsNotOverLeadingOrTrailingWhitespace) {
                    line = -1;
                }
            }
        } catch (BadLocationException e) {
            //do nothing
        }
        return line == -1;
    }
    
    private Segment seg = new Segment();
    
    /**
     * Only calls super if there are hyperlinks in the document to avoid huge
     * numbers of calls to viewToModel if the cursor is never going to be 
     * changed anyway.
     */
    public void mouseMoved (MouseEvent evt) {
        Document doc = getDocument();
        if (doc instanceof OutputDocument) {
            if (((OutputDocument) doc).getLines().hasHyperlinks()) {
                super.mouseMoved(evt);
            }
        }
    }
    
    public void mousePressed(MouseEvent e) {
        super.mousePressed(e);
        if (e.getSource() == textView && SwingUtilities.isLeftMouseButton(e)) {
            int pos = textView.viewToModel(e.getPoint());
            if (pos != -1) {
                int line = textView.getDocument().getDefaultRootElement().getElementIndex(pos);
                if (line >= 0) {
                    if (linePressed(line, e.getPoint())) {
                        e.consume();
                        return;
                    }
                }
            }
        }
        //Refine possibly to focus just what is important..
        findOutputTab().setToFocus((Component)e.getSource());
        findOutputTab().requestActive();
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
    
    
    public void setWrapped (boolean val) {
        if (val != isWrapped() || !(getEditorKit() instanceof OutputEditorKit)) {
            OutputSettings.getDefault().setWrap(val);
            final int pos = textView.getCaret().getDot();
            Cursor cursor = textView.getCursor();
            try {
                textView.setCursor (Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                setEditorKit (new OutputEditorKit(val, textView));
            } finally {
                textView.setCursor (cursor);
            }
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
            if (getDocument() instanceof OutputDocument && ((OutputDocument) getDocument()).getLines().isGrowing()) {
                lockScroll();
            }
            if (!val) {
                //If there are long lines, it will suddenly get scrolled to the right
                //with the non-wrapping editor kit, so fix that
                getHorizontalScrollBar().setValue(getHorizontalScrollBar().getModel().getMinimum());
            }
            validate();
        }
    }
    
    public boolean isWrapped() {
        if (getEditorKit() instanceof OutputEditorKit) {
            return getEditorKit() instanceof OutputEditorKit 
              && ((OutputEditorKit) getEditorKit()).isWrapped();
        } else {
            return OutputSettings.getDefault().isWrap();
        }
    }
    
    private static final boolean GTK = "GTK".equals(UIManager.getLookAndFeel().getID());
    protected JEditorPane createTextView() {
        JEditorPane result = GTK ? new GEP() : new JEditorPane();
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
                    view.setChanged();
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
    
    private static final class GEP extends JEditorPane {
        public java.awt.Color getBackground() {
            return UIManager.getColor("text");
        }
    }

}
