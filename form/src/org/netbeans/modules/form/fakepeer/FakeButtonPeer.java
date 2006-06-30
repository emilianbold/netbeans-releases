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
class FakeButtonPeer extends FakeComponentPeer
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
