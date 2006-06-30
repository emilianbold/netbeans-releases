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


package org.netbeans.modules.form.fakepeer;

import java.awt.*;

/**
 *
 * @author Tran Duc Trung
 */
class FakeScrollPanePeer extends FakeContainerPeer
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
            ScrollPane scrollPane = (ScrollPane) _target;
            int n = scrollPane.getComponentCount();
            return n > 0 ?
                     scrollPane.getComponent(n-1).getMinimumSize():
                     new Dimension(100, 100);
        }
    }
}
