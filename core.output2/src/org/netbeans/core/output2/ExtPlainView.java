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

import org.openide.ErrorManager;

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
    private static final Segment SEGMENT = new Segment(); 
    private static Color selectedFg;
    private static Color unselectedFg;
    private static Color selectedLinkFg;
    private static Color unselectedLinkFg;
    private static Color selectedErr;
    private static Color unselectedErr;
    
    static {
        //XXX clean these colors up a bit for different look and feels
        selectedFg = UIManager.getColor("textText") == null ? Color.BLACK : //NOI18N
            UIManager.getColor("textText"); //NOI18N
        unselectedFg = selectedFg;
        
        selectedLinkFg = Color.BLUE;
        unselectedLinkFg = selectedLinkFg;
        
        //XXX it looks like current ant runs simply print *everything* to
        //stderr, which may make trying to color it separately a bit useless.
        selectedErr = new Color (164, 0, 0);
        unselectedErr = selectedErr;
    }

    private JTextComponent comp;
    /** Creates a new instance of ExtPlainView */
    public ExtPlainView(Element elem, JTextComponent comp) {
        super (elem);
        this.comp = comp;
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
            if (g.getColor() == selectedLinkFg) {
                g.drawLine (x, y+1, x + ret, y+1);
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
            if (g.getColor() == unselectedLinkFg) {
                g.drawLine (x, y+1, x + ret, y+1);
            }
            return ret;
        } else {
            return super.drawUnselectedText (g, x, y, p0, p1);
        }
    }

    private Rectangle scratch = new Rectangle();
    public void paint(Graphics g, Shape allocation) {
        /*
        //Highlight caret row - need to add support for repainting a horizontal line whenever the
        //caret changes lines for this to work.

        int dot = comp.getCaret().getDot();
        int mark = comp.getCaret().getMark();
        if (dot == mark && dot > 0 && dot <= getDocument().getLength()) {
            try {
                Rectangle r = comp.modelToView(dot);
                r.x = 0;
                r.width = comp.getWidth();
                Rectangle clip = allocation instanceof Rectangle ? (Rectangle) allocation : allocation.getBounds();
                if (clip.y < r.y && clip.y + clip.height > r.y) {
                    scratch.setBounds (r);
                    scratch.x = Math.max (r.x, clip.x);
                    scratch.y = Math.max (r.y, clip.y);
                    if (scratch.x > 0) {
                        scratch.width -= scratch.x;
                    }
                    if (scratch.y > r.y) {
                        scratch.height -= (scratch.y - r.y);
                    }
                    g.setColor (new Color (245, 245, 237));
                    g.fillRect (scratch.x, scratch.y, scratch.width, scratch.height);
                }
            } catch (BadLocationException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        */

        super.paint (g, allocation);
    }

    private static Color getColorForLocation (int start, Document d, boolean selected) {
        OutputDocument od = (OutputDocument) d;
        int line = od.getElementIndex (start);
        boolean hyperlink = od.isHyperlink(line);
        boolean isErr = od.isErr(line);
        return hyperlink ? selected ? selectedLinkFg : unselectedLinkFg :
            selected ? isErr ? selectedErr : selectedFg : isErr ? unselectedErr : unselectedFg;
    }

}
