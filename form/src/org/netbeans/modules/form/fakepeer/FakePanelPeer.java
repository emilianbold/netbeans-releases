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
import java.awt.peer.PanelPeer;

/**
 *
 * @author Tran Duc Trung
 */

class FakePanelPeer extends FakeContainerPeer implements PanelPeer
{
    FakePanelPeer(Panel target) {
        super(target);
    }

    Component createDelegate() {
        return new Delegate();
    }

    //
    //
    //

    private class Delegate extends Component
    {
        public void paint(Graphics g) {
            Dimension sz = _target.getSize();

            Color c = _target.getBackground();
            if (c == null)
                c = SystemColor.window;
            g.setColor(c);
            
            g.fillRect(0, 0, sz.width, sz.height);
        }
    }
}
