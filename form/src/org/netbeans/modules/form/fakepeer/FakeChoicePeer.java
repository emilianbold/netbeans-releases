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
import java.awt.peer.ChoicePeer;

/**
 *
 * @author Tran Duc Trung
 */

class FakeChoicePeer extends FakeComponentPeer implements ChoicePeer
{
    FakeChoicePeer(Choice target) {
        super(target);
    }

    Component createDelegate() {
        return new Delegate();
    }

    public void add(String item, int index) {
    }

    public void remove(int index) {
    }

    public void removeAll() {
    }

    public void select(int index) {
    }

    public void addItem(String item, int index) {
        add(item, index);
    }

    //
    //
    //

    private class Delegate extends Component
    {
        public void paint(Graphics g) {
            Choice target =(Choice) _target;

            Dimension sz = target.getSize();
            FontMetrics fm = g.getFontMetrics();
            int w = sz.width,
                h = sz.height,
                fh = fm.getHeight(), // font's height
                comph = h > fh+4 ? fh+4 : h, // component's height
                y = (h-comph)/2; // component's vertical position

            // background & border
            Color c = getBackground();
            if (c == null)
                c = SystemColor.window; // white
            g.setColor(c);
            FakePeerUtils.drawLoweredBox(g, 0,y,w,comph);

            // selected text
            String item = target.getSelectedItem();
            if (item != null) {
                c = getForeground();
                if (c == null)
                    c = SystemColor.controlText;
                g.setColor(c);
                g.setFont(target.getFont());

                g.setClip(2,y+2,w-4,comph-4);

                int ih = fh - fm.getDescent(), // item's height
                    iy = y + 1 + ih;

                g.drawString(item, 4, iy);
            }

            // combo-box button (Windows style)
            FakePeerUtils.drawArrowButton(g, w-BUT_W-2, y+2, BUT_W, comph-4, 4);
        }

        public Dimension getMinimumSize() {
            String label =((Choice)_target).getSelectedItem();
            FontMetrics fm = this.getFontMetrics(_target.getFont());
            int w = label != null ? fm.stringWidth(label)+5 : 8,
                h = fm.getHeight();

            return new Dimension(w + 4 + BUT_W, h + 4);
        }
    }

    private static final int BUT_W = 16, BUT_H = 16;
}
