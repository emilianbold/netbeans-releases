/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
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
            boolean enabled = target.isEnabled();

            // background
            Color c = getBackground();
            if (c == null)
                c = SystemColor.scrollbar; // light gray
            g.setColor(c);

            FakePeerUtils.drawScrollbar(g,0,0,w,h,target.getOrientation(),target.isEnabled(),
                                        true,scrollValue,thumbAmount,scrollRange);
        }

        public Dimension getMinimumSize() {
            Scrollbar target =(Scrollbar) _target;
            return target.getOrientation() == Scrollbar.HORIZONTAL ?
                new Dimension(3 * FakePeerUtils.SCROLL_W, FakePeerUtils.SCROLL_H) :
                new Dimension(FakePeerUtils.SCROLL_W, 3 * FakePeerUtils.SCROLL_H);
        }
    }
}
