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
import java.awt.peer.ButtonPeer;
import javax.swing.*;


/**
 *
 * @author Tran Duc Trung
 */

class FakeButtonPeer extends FakeComponentPeer implements ButtonPeer
{
    FakeButtonPeer(Button target) {
        super(target);
    }

    Component createDelegate() {
        return new Delegate();
    }

    public void setLabel(String label) {
    }

    //
    //
    //

    private class Delegate extends Component
    {
        public void paint(Graphics g) {
            Button target =(Button) _target;

            Dimension sz = target.getSize();

            // background & border
            Color c = getBackground();
            if (c == null)
                c = SystemColor.control; // light gray
            g.setColor(c);
            FakePeerUtils.drawButton(g, 0, 0, sz.width, sz.height);

            // label
            String label = target.getLabel();
            if (label != null) {
                g.setFont(_target.getFont());

                FontMetrics fm = g.getFontMetrics();
                int w = fm.stringWidth(label),
                    h = fm.getHeight() - fm.getDescent(),
                    x = (sz.width - w) / 2,
                    y = (sz.height - h) / 2 + h - 2;

                if (target.isEnabled()) {
                    c = getForeground();
                    if (c == null)
                        c = SystemColor.controlText;
                    g.setColor(c);
                } else {
                    g.setColor(SystemColor.controlLtHighlight);
                    g.drawString(label, x+1, y+1);
                    g.setColor(SystemColor.controlShadow);
                }

                g.drawString(label, x, y);
            }
        }

        public Dimension getMinimumSize() {
            String label =((Button)_target).getLabel();

            FontMetrics fm = this.getFontMetrics(_target.getFont());
            int w = fm.stringWidth(label);
            int h = fm.getHeight();

            return new Dimension(w + MARGINS.left + MARGINS.right,
                                 h + MARGINS.top + MARGINS.bottom);
        }
    }

    private static final Insets MARGINS = new Insets(4, 8, 4, 8);
}
