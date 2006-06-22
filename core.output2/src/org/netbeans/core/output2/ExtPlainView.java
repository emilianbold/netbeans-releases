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
 * ExtPlainView.java
 *
 * Created on May 9, 2004, 4:29 PM
 */

package org.netbeans.core.output2;

import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

/**
 * Extension to PlainView which can paint hyperlinked lines in different
 * colors.  For the limited styles that the output window supports, this
 * is considerably simpler and has less overhead than the default handling
 * of StyledDocument.
 *
 * @author  Tim Boudreau
 */
class ExtPlainView extends PlainView {
    private final Segment SEGMENT = new Segment(); 

    /** set antialiasing hints when it's requested. */
    private static final boolean antialias = Boolean.getBoolean ("swing.aatext") || //NOI18N
                                             "Aqua".equals (UIManager.getLookAndFeel().getID()); // NOI18N

    private static Map hintsMap = null;
    
    static final Map getHints() {
        if (hintsMap == null) {
            //Thanks to Phil Race for making this possible
            hintsMap = (Map)(Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints")); //NOI18N
            if (hintsMap == null) {
                hintsMap = new HashMap();
                if (antialias)
                    hintsMap.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            }
        }
        return hintsMap;
    }
    
    /** Creates a new instance of ExtPlainView */
    ExtPlainView(Element elem) {
        super (elem);
    }

    public void paint(Graphics g, Shape allocation) {
        ((Graphics2D)g).addRenderingHints(getHints());
        super.paint(g, allocation);
    }
    
    protected int drawSelectedText(Graphics g, int x,
                                   int y, int p0, int p1) throws BadLocationException {
                                       
        Document doc = getDocument();
        if (doc instanceof OutputDocument) {                                         
            Segment s = SwingUtilities.isEventDispatchThread() ? SEGMENT : 
                new Segment(); 
            doc.getText(p0, p1 - p0, s);
            g.setColor(getColorForLocation(p0, doc, true));
            int ret = Utilities.drawTabbedText(s, x, y, g, this, p0);
            if (g.getColor() == WrappedTextView.selectedLinkFg || g.getColor() == WrappedTextView.selectedImportantLinkFg) {
                //#47263 - start hyperlink underline at first
                //non-whitespace character
                underline(g, s, x, p0, y);
            }
            return ret;
        } else {
            return super.drawUnselectedText (g, x, y, p0, p1);
        }
    }

    
    protected int drawUnselectedText(Graphics g, int x, int y, 
                                     int p0, int p1) throws BadLocationException {
        Document doc = getDocument();
        if (doc instanceof OutputDocument) {                                         
            Segment s = SwingUtilities.isEventDispatchThread() ? SEGMENT : 
                new Segment(); 
            doc.getText(p0, p1 - p0, s);
            g.setColor(getColorForLocation(p0, doc, false));
            int ret = Utilities.drawTabbedText(s, x, y, g, this, p0);
            if (g.getColor() == WrappedTextView.selectedLinkFg || g.getColor() == WrappedTextView.selectedImportantLinkFg) {
                //#47263 - start hyperlink underline at first
                //non-whitespace character
                underline(g, s, x, p0, y);
            }
            return ret;
        } else {
            return super.drawUnselectedText (g, x, y, p0, p1);
        }
    }

    private void underline(Graphics g, Segment s, int x, int p0, int y) {
        int wid = g.getFontMetrics().charWidth(' '); //NOI18N
        char[] txt = s.array;
        int txtOffset = s.offset;
        int txtCount = s.count;
        int n = s.offset + s.count;
        int tabCount = 0;
        int wsCount = 0;
        for (int i = s.offset; i < n; i++) {
            if (txt[i] == '\t') { //NOI18N
                x = (int) nextTabStop((float) x,
                p0 + i - txtOffset);
                tabCount++;
            } else if (Character.isWhitespace(txt[i])) {
                x += wid;
                wsCount++;
            } else {
                break;
            }
        }
        int end = x + (wid * (txtCount - (wsCount + tabCount + 1)));
        if (end > x) {
            g.drawLine (x, y+1, end, y+1);
        }
    }

    private static Color getColorForLocation (int start, Document d, boolean selected) {
        OutputDocument od = (OutputDocument) d;
        int line = od.getElementIndex (start);
        boolean hyperlink = od.getLines().isHyperlink(line);
        boolean important = hyperlink ? od.getLines().isImportantHyperlink(line) : false;
        boolean isErr = od.getLines().isErr(line);
        
        return hyperlink ? 
            (important ? 
                (selected ? 
                    WrappedTextView.selectedImportantLinkFg : 
                    WrappedTextView.unselectedImportantLinkFg) :
                (selected ?
                    WrappedTextView.selectedLinkFg : 
                    WrappedTextView.unselectedLinkFg)) :
            (selected ? 
                (isErr ? 
                    WrappedTextView.selectedErr : 
                    WrappedTextView.selectedFg) : 
                (isErr ? 
                    WrappedTextView.unselectedErr : 
                    WrappedTextView.unselectedFg));
    }

}
