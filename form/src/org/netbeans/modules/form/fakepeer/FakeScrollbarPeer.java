/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

/* $Id$ */

package org.netbeans.modules.form.fakepeer;

import java.awt.*;
import java.awt.peer.ScrollbarPeer;

/**
 *
 * @author Tran Duc Trung
 */

class FakeScrollbarPeer extends FakeComponentPeer implements ScrollbarPeer
{
    FakeScrollbarPeer(Scrollbar target) {
        super(target);
    }

    Component createDelegate() {
        return new Delegate();
    }

    public void setValues(int value, int visible, int minimum, int maximum) {}
    public void setLineIncrement(int l) {}
    public void setPageIncrement(int l) {}

    //
    //
    //

    private class Delegate extends Component
    {
        public void paint(Graphics g) {
            Scrollbar target =(Scrollbar) _target;

            Dimension sz = target.getSize();
            int w = sz.width,
                h = sz.height,
                scrollRange = target.getMaximum() - target.getMinimum(),
                scrollValue = target.getValue() - target.getMinimum(),
                thumbAmount = target.getVisibleAmount();

            // background
            Color c = getBackground();
            if (c == null)
                c = SystemColor.scrollbar; // light gray
            g.setColor(c);
            g.fillRect(0, 0, w, h);

            // border (Windows style)
            g.setColor(SystemColor.controlShadow);
            g.drawRect(0,0,w-1,h-1);
            g.setColor(SystemColor.control);
            g.drawRect(1,1,w-3,h-3);

            if (target.getOrientation() == Scrollbar.HORIZONTAL) {
                int butW;
                if (w >= 2*SCROLL_W) {
                    butW = SCROLL_W;
                    int wFT = w - 2*butW; // width that remains for the "thumb"
                    if (wFT >= 4) { // paint the thumb
                        int thumbW = scrollRange > 0 ? wFT * thumbAmount / scrollRange : wFT;
                        if (thumbW < 6) thumbW = 6;
                        if (thumbW > wFT) thumbW = wFT;
                        scrollRange -= scrollValue;
                        int thumbX = (scrollRange > 0 ? scrollValue * (wFT - thumbW) / scrollRange : 0) + butW;

                        FakePeerUtils.drawScrollThumb(g,thumbX,0,thumbW,h);
                    }
                } else butW = w/2;
                if (butW >= 4) { // paint "arrow" buttons
                    FakePeerUtils.drawArrowButton(g,0,0,butW,h,1); // the left one <
                    FakePeerUtils.drawArrowButton(g,w-butW,0,butW,h,2); // the right one >
                }
            } else { // == Scrollbar.VERTICAL
                int butH;
                if (h >= 2*SCROLL_H) {
                    butH = SCROLL_H;
                    int hFT = h - 2*butH; // height that remains for the "thumb"
                    if (hFT >= 4) { // paint the thumb
                        int thumbH = scrollRange > 0 ? hFT * thumbAmount / scrollRange : hFT;
                        if (thumbH < 6) thumbH = 6;
                        if (thumbH > hFT) thumbH = hFT;
                        scrollRange -= scrollValue;
                        int thumbY = (scrollRange > 0 ? scrollValue * (hFT - thumbH) / scrollRange : 0) + butH;
                        
                        FakePeerUtils.drawScrollThumb(g,0,thumbY,w,thumbH);
                    }
                } else butH = h/2;
                if (butH >= 4) { // paint "arrow" buttons
                    FakePeerUtils.drawArrowButton(g,0,0,w,butH,3); // the upper one ^
                    FakePeerUtils.drawArrowButton(g,0,h-butH,w,butH,4); // the lower one v
                }
            }
        }

        public Dimension getMinimumSize() {
            Scrollbar target =(Scrollbar) _target;
            return target.getOrientation() == Scrollbar.HORIZONTAL ?
                   new Dimension(3*SCROLL_W,SCROLL_H) :
                   new Dimension(SCROLL_W,3*SCROLL_H);
        }
    }

    private static final int SCROLL_W = 16, SCROLL_H = 16;
}
