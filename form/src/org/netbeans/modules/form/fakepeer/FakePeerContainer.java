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

public class FakePeerContainer extends Container
{
    public FakePeerContainer() {
        super();
        setFont(FakePeerSupport.getDefaultAWTFont());
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

        Component components[] = FakePeerSupport.getComponents(container);
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
