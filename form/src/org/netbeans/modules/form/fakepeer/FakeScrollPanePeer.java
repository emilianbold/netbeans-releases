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


package org.netbeans.modules.form.fakepeer;

import java.awt.*;
import java.awt.peer.ScrollPanePeer;

/**
 *
 * @author Tran Duc Trung
 */

class FakeScrollPanePeer extends FakeContainerPeer implements ScrollPanePeer
{
    FakeScrollPanePeer(ScrollPane target) {
        super(target);
    }

    Component createDelegate() {
        return new Delegate();
    }

    public int getHScrollbarHeight() {
        return 16;
    }

    public int getVScrollbarWidth() {
        return 16;
    }

    public void setScrollPosition(int x, int y) {}
    public void childResized(int w, int h) {}
    public void setUnitIncrement(Adjustable adj, int u) {}
    public void setValue(Adjustable adj, int v) {}

    //
    //
    //

    private class Delegate extends Component
    {
//        Delegate() {
//            this.setBackground(SystemColor.scrollbar);
//        }
        
        public void paint(Graphics g) {
            ScrollPane target = (ScrollPane) _target;
            Dimension sz = target.getSize();

            g.setColor(target.getBackground());
            FakePeerUtils.drawLoweredBox(g,0,0,sz.width,sz.height);
        }

        public Dimension getMinimumSize() {
            return ((ScrollPane)_target).getViewportSize();
        }
    }
}
