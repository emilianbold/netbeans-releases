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
import java.awt.peer.CheckboxPeer;

/**
 *
 * @author Tran Duc Trung
 */

class FakeCheckboxPeer extends FakeComponentPeer implements CheckboxPeer
{
    FakeCheckboxPeer(Checkbox target) {
        super(target);
    }

    Component createDelegate() {
        return new Delegate();
    }

    public void setState(boolean state) {
    }

    public void setCheckboxGroup(CheckboxGroup g) {
    }

    public void setLabel(String label) {
    }

    //
    //
    //

    private class Delegate extends Component
    {
        public void paint(Graphics g) {
            Checkbox target =(Checkbox) _target;

            Dimension sz = target.getSize();
            int bx = 0,
                by = (sz.height - BOX_H) / 2;

            // background
            Color c = getBackground();
            if (c == null)
                c = SystemColor.control;
            g.setColor(c);
            g.fillRect(0, 0, sz.width, sz.height);

            // label
            String label = target.getLabel();
            if (label != null) {
                g.setFont(target.getFont());

                FontMetrics fm = g.getFontMetrics();
                int h = fm.getHeight() - fm.getDescent(),
                    x = 18,
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
                by = y - h + 2;
            }

            // the check-box (Windows like - lowered, white background)
            if (target.getCheckboxGroup() == null) {
                g.setColor(SystemColor.window);
                FakePeerUtils.drawLoweredBox(g,bx,by,BOX_W,BOX_H);

                if (target.getState()) { // checkbox is checked
                    g.setColor(SystemColor.controlText);
                    for (int i=1; i < drCheckPosX_W.length; i++)
                        g.drawLine(drCheckPosX_W[i-1]+bx,drCheckPosY_W[i-1]+by,
                                   drCheckPosX_W[i]+bx,drCheckPosY_W[i]+by);
                }
            } else { // radio button
                if (radButtIcon1 == null || radButtIcon2 == null)
                    initRBImages();

                g.drawImage(target.getState() ? radButtIcon2:radButtIcon1, bx+1, by+1, this);
            }
        }

        public Dimension getMinimumSize() {
            String label = ((Checkbox)_target).getLabel();

            FontMetrics fm = this.getFontMetrics(_target.getFont());
            int w = fm.stringWidth(label);
            int h = fm.getHeight();

            return new Dimension(w + 6+BOX_W+4, h + 4);
        }

        void initRBImages() {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            java.net.URL source = getClass().getResource("radbutt1.gif");
            radButtIcon1 = toolkit.getImage(source);
            source = getClass().getResource("radbutt2.gif");
            radButtIcon2 = toolkit.getImage(source);

            MediaTracker mt = new MediaTracker(this);
            mt.addImage(radButtIcon1,0);
            mt.addImage(radButtIcon2,1);
            try {
                mt.waitForAll();
            } catch (java.lang.InterruptedException e) {
            }
        }
    }

    private static final int BOX_W = 16, BOX_H = 16;
    private static final int[] drCheckPosX_W = { 4,6,10,10,6,4,4,6,10 };
    private static final int[] drCheckPosY_W = { 6,8,4,5,9,7,8,10,6 };

    private static Image radButtIcon1 = null;
    private static Image radButtIcon2 = null;
}
