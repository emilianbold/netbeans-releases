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
class FakeScrollbarPeer extends FakeComponentPeer
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
        Delegate() {
            this.setBackground(SystemColor.scrollbar);
        }
        
        public void paint(Graphics g) {
            Scrollbar target = (Scrollbar) _target;
            Dimension sz = target.getSize();
            int scrollRange = target.getMaximum() - target.getMinimum();
            int scrollValue = target.getValue() - target.getMinimum();
            int thumbAmount = target.getVisibleAmount();

            g.setColor(target.getBackground());

            FakePeerUtils.drawScrollbar(g,
                                        0, 0, sz.width, sz.height,
                                        target.getOrientation(),
                                        target.isEnabled(),
                                        true,
                                        scrollValue,
                                        thumbAmount,
                                        scrollRange);
        }

        public Dimension getMinimumSize() {
            Scrollbar target =(Scrollbar) _target;
            return target.getOrientation() == Scrollbar.HORIZONTAL ?
                new Dimension(3 * FakePeerUtils.SCROLL_W, FakePeerUtils.SCROLL_H) :
                new Dimension(FakePeerUtils.SCROLL_W, 3 * FakePeerUtils.SCROLL_H);
        }
    }
}
