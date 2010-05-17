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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.bpel.design.DiagramView;
import org.netbeans.modules.bpel.design.decoration.ComponentsDescriptor;
import org.netbeans.modules.bpel.design.decoration.Decoration;
import org.netbeans.modules.bpel.design.decoration.Positioner;
import org.netbeans.modules.bpel.design.decoration.components.ZoomableDecorationComponent;

/**
 *
 * @author aa160298
 */
public class DiagramViewLayout implements java.awt.LayoutManager {

    public void addLayoutComponent(String name, Component comp) {
    }

    public void removeLayoutComponent(Component comp) {
    }

    public Dimension preferredLayoutSize(Container parent) {
        synchronized (parent.getTreeLock()) {
            DiagramView diagramView = (DiagramView) parent;
            DesignView designView = diagramView.getDesignView();

            int w = 0;
            int h = 0;

            double k = designView.getCorrectedZoom();

            FBounds bounds = diagramView.getContentSize();

            w = (int) Math.round(k * bounds.width + MARGIN_LEFT + MARGIN_RIGHT);
            h = (int) Math.round(k * bounds.height + MARGIN_TOP + MARGIN_BOTTOM);
            
            if (parent instanceof ProcessView) {
//                int count = diagramView.getComponentCount();
//                for (int i = 0; i < count; i++) {
//                    Component c = diagramView.getComponent(i);
//                    if (c instanceof NavigationTools) {
//                        continue;
//                    }
//
//                    w = Math.max(w, c.getX() + c.getWidth() + MARGIN_RIGHT);
//                    h = Math.max(h, c.getY() + c.getHeight() + MARGIN_BOTTOM);
//                }
                
                TriScrollPane scrollPane = designView.getScrollPane();
                int leftWidth = scrollPane.getLeftPreferredWidth();
                int rightWidth = scrollPane.getRightPreferredWidth();
                
                w += leftWidth + rightWidth;
            }

            return new Dimension(w, h);
        }
    }

    public Dimension minimumLayoutSize(Container parent) {
        return preferredLayoutSize(parent);
    }

    public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
            DiagramView diagramView = (DiagramView) parent;
            DesignView designView = diagramView.getDesignView();

            double k = designView.getCorrectedZoom();

            FBounds bounds = diagramView.getContentSize();
            
            int offsetX = (int) Math.round((diagramView.getWidth() 
                    - bounds.width * k) / 2.0);
            int offsetY = (int) Math.round((diagramView.getHeight() 
                    - bounds.height * k) / 2.0);
            
             diagramView.setOffsets(offsetX, offsetY);

            if (parent.getComponentCount() > 0) {
                repositionDecorations(parent);
            }

//            Component component = parent.getComponent(0);
//            if (!(component instanceof NavigationTools)) {
//                return;
//            }
//            component.setBounds(0, 0, parent.getWidth(), parent.getHeight());
            
            
        }
    }

    private void repositionDecorations(Container parent) {
        DiagramView diagramView = (DiagramView) parent;
        DesignView designView = diagramView.getDesignView();

        Iterator<Pattern> itr = diagramView.getPatterns();
        while (itr.hasNext()) {
            Pattern pattern = itr.next();
            
            Decoration decoration = designView.getDecoration(pattern);

            if ((decoration == null) || !decoration.hasComponents()) {
                continue;
            }

            double zoom = designView.getCorrectedZoom();

            //group components by positioneer
            HashMap<Positioner, List<Component>> positionerComponents =
                    new HashMap<Positioner, List<Component>>();

            ComponentsDescriptor components = decoration.getComponents();
            int componentsCount = components.getComponentCount();

            for (int i = 0; i < componentsCount; i++) {
                Component c = components.getComponent(i);
                Positioner p = components.getPositioner(i);

                List<Component> list = positionerComponents.get(p);

                if (list == null) {
                    list = new ArrayList<Component>(componentsCount);
                    positionerComponents.put(p, list);
                }

                list.add(c);

                if (c instanceof ZoomableDecorationComponent) {
                    //apply zoom
                    ((ZoomableDecorationComponent) c).setZoom(zoom);
                }
            }

            //call positioners for each group
            for (Positioner p : positionerComponents.keySet()) {
                p.position(pattern, positionerComponents.get(p), zoom);
            }
        }
    }
    public static int MARGIN_TOP = 32;
    public static int MARGIN_LEFT = 18;
    public static int MARGIN_BOTTOM = 32;
    public static int MARGIN_RIGHT = 18;
}
