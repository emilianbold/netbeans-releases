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
        Delegate() {
            this.setBackground(SystemColor.control);
            this.setForeground(SystemColor.controlText);
        }
        
        public void paint(Graphics g) {
            Button target =(Button) _target;
            Dimension sz = target.getSize();

            g.setColor(target.getBackground());
            FakePeerUtils.drawButton(g, 0, 0, sz.width, sz.height);

            String label = target.getLabel();
            if (label == null)
                return;
            
            g.setFont(target.getFont());

            FontMetrics fm = g.getFontMetrics();
            int w = fm.stringWidth(label),
                h = fm.getHeight() - fm.getDescent(),
                x = (sz.width - w) / 2,
                y = (sz.height - h) / 2 + h - 2;
            
            if (target.isEnabled()) {
                g.setColor(target.getForeground());
            }
            else {
                g.setColor(SystemColor.controlLtHighlight);
                g.drawString(label, x+1, y+1);
                g.setColor(SystemColor.controlShadow);
            }

            g.drawString(label, x, y);
        }

        public Dimension getMinimumSize() {
            String label = ((Button)_target).getLabel();

            FontMetrics fm = this.getFontMetrics(this.getFont());
            int w = fm.stringWidth(label);
            int h = fm.getHeight();

            return new Dimension(w + MARGINS.left + MARGINS.right,
                                 h + MARGINS.top + MARGINS.bottom);
        }
    }

    private static final Insets MARGINS = new Insets(4, 8, 4, 8);
}
