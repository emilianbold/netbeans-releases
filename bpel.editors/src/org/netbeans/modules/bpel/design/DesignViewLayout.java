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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.bpel.design;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.design.layout.LayoutManager;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;

/**
 *
 * @author aa160298
 */
public class DesignViewLayout implements java.awt.LayoutManager {
    
    
    public void addLayoutComponent(String name, Component comp) {}
    public void removeLayoutComponent(Component comp) {}

    
    public Dimension preferredLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            DesignView designView = (DesignView) parent;
            
            int w = 0;
            int h = 0;

            Pattern rp = designView.getModel().getRootPattern();

            double k = designView.getCorrectedZoom();

            if (rp != null) {
                FBounds bounds = rp.getBounds();
                
                w = (int) Math.round(k * bounds.width 
                        + MARGIN_LEFT + MARGIN_RIGHT);
                h = (int) Math.round(k * bounds.height 
                        + MARGIN_TOP + MARGIN_BOTTOM);
            }
            
            int count = designView.getComponentCount();
            for (int i = 0; i < count; i++) {
                Component c = designView.getComponent(i);
                if (c instanceof NavigationTools) continue;
                
                w = Math.max(w, c.getX() + c.getWidth() + MARGIN_RIGHT);
                h = Math.max(h, c.getY() + c.getHeight() + MARGIN_BOTTOM);
            }
            
            return new Dimension(w, h);
        }
    }

    
    public Dimension minimumLayoutSize(Container parent) {
        return preferredLayoutSize(parent);
    }

    
    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            if (parent.getComponentCount() == 0) return;
            Component component = parent.getComponent(0);
            if (!(component instanceof NavigationTools)) return;
            component.setBounds(0, 0, parent.getWidth(), parent.getHeight());
        }
    }
    


    public static int MARGIN_TOP = 32;
    public static int MARGIN_LEFT = 32;
    public static int MARGIN_BOTTOM = 32;
    public static int MARGIN_RIGHT = 32;    
}
 