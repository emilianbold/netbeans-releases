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

/**
 *
 * @author Tran Duc Trung
 */

public class FakePeerContainer extends Container
{
    public FakePeerContainer() {
        super();
        setFont(new Font("Dialog", Font.PLAIN, 12)); // NOI18N
    }

    public void addNotify() {
        FakePeerSupport.attachFakePeerRecursively(this);
        super.addNotify();
    }

    protected void addImpl(Component comp, Object constraints, int index) {
        FakePeerSupport.attachFakePeer(comp);
        super.addImpl(comp, constraints, index);
    }
    
    public void update(Graphics g) {
    }

    public void paint(Graphics g) {
        Dimension sz = getSize();
//        Shape oldClip = g.getClip();
//        g.setClip(0, 0, sz.width, sz.height);

        Color c = SystemColor.control;
        g.setColor(c);
        g.fillRect(0, 0, sz.width, sz.height);
//        g.setClip(oldClip);

        super.paint(g);
        paintFakePeersRecursively(g, this);
    }

    private static void paintFakePeersRecursively(Graphics g, Container container) {
        if (!container.isVisible())
            return;

        Component components[] = container.getComponents();
        int ncomponents = components.length;

        Rectangle clip = g.getClipBounds();
        for (int i = 0; i < ncomponents; i++) {
            Component comp = components[i];
            if (comp != null &&
                comp.getPeer() instanceof FakePeer &&
                comp.isVisible()) {
                Rectangle cr = comp.getBounds();
                if ((clip == null) || cr.intersects(clip)) {
                    Graphics cg = g.create(cr.x, cr.y, cr.width, cr.height);
                    cg.setFont(comp.getFont());
                    try {
//                        System.err.println("** painting " + comp.getPeer());
//                        System.err.println("**   bounds = " + cr);
                        comp.getPeer().paint(cg);
                    }
                    finally {
                        cg.dispose();
                    }
                }
            }
            if (comp instanceof Container) {
                Rectangle cr = comp.getBounds();
                if ((clip == null) || cr.intersects(clip)) {
                    Graphics cg = g.create(cr.x, cr.y, cr.width, cr.height);
                    paintFakePeersRecursively(cg,(Container) comp);
                    cg.dispose();
                }
            }
        }
    }
}
