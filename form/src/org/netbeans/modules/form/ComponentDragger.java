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

package org.netbeans.modules.form;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.*;
import java.util.List;

import org.netbeans.modules.form.layoutsupport.*;

/**
 *
 * @author Tran Duc Trung
 */

class ComponentDragger
{
    private FormDesigner formDesigner;
    private HandleLayer handleLayer;
    private RADVisualComponent[] selectedComponents;
    private Point hotspot;
    private Point mousePosition;
    
    ComponentDragger(FormDesigner formDesigner,
                     HandleLayer handleLayer,
                     RADVisualComponent[] selectedComponents,
                     Point hotspot) {
        this.formDesigner = formDesigner;
        this.handleLayer = handleLayer;
        this.selectedComponents = selectedComponents;
        this.hotspot = hotspot;
        this.mousePosition = hotspot;
    }

    void mouseDragged(Point p) {
        mousePosition = p;
        handleLayer.repaint();
    }

    void dropComponents(Point point) {
        RADVisualContainer metacont = handleLayer.getMetaContainerAt(point);
        if (metacont == null)
            return;
        
        LayoutSupport layoutSupport = metacont.getLayoutSupport();
        if (layoutSupport == null)
            return;
        
        Component comp = (Component) formDesigner.getComponent(metacont);
        if (comp == null)
            return;
        Container cont = metacont.getContainerDelegate(comp);

        FormModel model = formDesigner.getModel();
        int indices[] = new int[selectedComponents.length];

        Point posInCont = SwingUtilities.convertPoint(handleLayer, point, cont);

        for (int i = 0; i < selectedComponents.length; i++) {
            RADVisualComponent metacomp = selectedComponents[i];

            if (metacomp == metacont
                || (metacomp instanceof RADVisualContainer
                    && ((RADVisualContainer)metacomp).isAncestorOf(metacont))
                ) {
                selectedComponents[i] = null;
                continue;
            }
                
            Component component = (Component) formDesigner.getComponent(metacomp);
            Point posInComp = SwingUtilities.convertPoint(handleLayer,
                                                          hotspot,
                                                          component);
            indices[i] = layoutSupport.getNewIndex(
                cont, posInCont, component, posInComp);
        }

        for (int i = 0; i < selectedComponents.length; i++) {
            RADVisualComponent metacomp = selectedComponents[i];
            if (metacomp == null)
                continue;
            
            Component component = (Component) formDesigner.getComponent(metacomp);
            if (component == null || component.getParent() != cont)
                continue;

            Point posInComp = SwingUtilities.convertPoint(handleLayer,
                                                          hotspot,
                                                          component);
            
            LayoutSupport.ConstraintsDesc constr =
                layoutSupport.getNewConstraints(cont, posInCont, component, posInComp);
            
            if (indices[i] < 0 && constr== null) {
                selectedComponents[i] = null;
            }
        }

        Component[] components = cont.getComponents();
        LinkedList newComponents = new LinkedList();
        LinkedList constraints = new LinkedList();
        
        for (int i = 0; i < components.length; i++) {
            RADComponent metacomp = formDesigner.getMetaComponent(components[i]);
            if (metacomp instanceof RADVisualComponent) {
                newComponents.add(metacomp);
                constraints.add(((RADVisualComponent)metacomp).getConstraintsDesc(
                                    layoutSupport.getClass()));
            }
        }
        
        for (int i = 0; i < selectedComponents.length; i++) {
            RADVisualComponent metacomp = selectedComponents[i];
            if (metacomp == null)
                continue;
            
            int newindex = indices[i];
            int oldindex = newComponents.indexOf(metacomp);
            if (oldindex >= 0) {
                newComponents.remove(metacomp);
                if (oldindex < newindex) {
                    newindex--;
                }
            }
            if ( newindex < 0 || newindex >= components.length) {
                newComponents.add(metacomp);
            }
            else {
                if (newindex >= newComponents.size())
                    newComponents.add(metacomp);
                else
                    newComponents.add(newindex, metacomp);
            }
            Component component = (Component) formDesigner.getComponent(metacomp);
            Point posInComp = SwingUtilities.convertPoint(handleLayer,
                                                          hotspot,
                                                          component);

            LayoutSupport.ConstraintsDesc constr =
                layoutSupport.getNewConstraints(cont, posInCont, component, posInComp);
            constraints.add(constr);
        }
        
        int start = 0;
        for (int i = 0; i < newComponents.size() && i < components.length; i++) {
            if (newComponents.get(i) != components[i]) {
                start = i;
                break;
            }
        }

        List oldSelectedComponents = formDesigner.getSelectedComponents();
        
        for (int i = start; i < newComponents.size(); i++) {
            RADComponent metacomp = (RADComponent) newComponents.get(i);
            model.removeComponent(metacomp);
        }
            
        for (int i = start; i < newComponents.size(); i++) {
            RADVisualComponent metacomp = (RADVisualComponent) newComponents.get(i);
            LayoutSupport.ConstraintsDesc constr =
                (LayoutSupport.ConstraintsDesc) constraints.get(i);
            if (constr != null) {
                constr = layoutSupport.fixConstraints(constr);
            }
            model.addVisualComponent(metacomp, metacont, constr);
        }

        formDesigner.clearSelection();
        Iterator iter = oldSelectedComponents.iterator();
        while (iter.hasNext()) {
            formDesigner.addComponentToSelection(
                (RADComponent) iter.next());
        }
    }

    void paintDragFeekback(Graphics2D g) {
        Stroke oldStroke = g.getStroke();
        Stroke stroke = new BasicStroke((float) 2.0,
                                            BasicStroke.CAP_SQUARE,
                                            BasicStroke.JOIN_MITER,
                                            (float) 10.0,
                                            new float[] { (float) 2.0, (float) 6.0 },
                                            0 );
        g.setStroke(stroke);

        Color oldColor = g.getColor();
        g.setColor(Color.blue);
        
        RADVisualContainer metacont = handleLayer.getMetaContainerAt(mousePosition);
        LayoutSupport layoutSupport =
            (metacont == null) ? null : metacont.getLayoutSupport();
        Component comp =
            (metacont == null) ? null : (Component) formDesigner.getComponent(metacont);
        Container cont = (comp == null) ? null : metacont.getContainerDelegate(comp);

        Point posInCont;
        if (cont != null) {
            posInCont = SwingUtilities.convertPoint(handleLayer,
                                                    mousePosition,
                                                    cont);
            paintTargetContainerFeedback(g, cont);
        }
        else
            posInCont = null;

        Point contPos = SwingUtilities.convertPoint(cont, new Point(0, 0), handleLayer);
        
        for (int i = 0; i < selectedComponents.length; i++) {
            RADVisualComponent metacomp = selectedComponents[i];
            if (!(formDesigner.getComponent(metacomp) instanceof Component))
                continue;
            
            if (metacomp != metacont && layoutSupport != null) {
                Component component = (Component) formDesigner.getComponent(metacomp);
                Point posInComp = SwingUtilities.convertPoint(handleLayer,
                                                              hotspot,
                                                              component);
                
                int newIndex = layoutSupport.getNewIndex(
                    cont, posInCont, component, posInComp);
                LayoutSupport.ConstraintsDesc newConstraints =
                    layoutSupport.getNewConstraints(
                        cont, posInCont, component, posInComp);
                
                if (newIndex >= 0 || newConstraints != null) {
                    g.translate(contPos.x, contPos.y);
                    boolean drawn = layoutSupport.paintDragFeedback(
                        cont, component, newConstraints, newIndex, g);
                    g.translate(- contPos.x, - contPos.y);
                    if (drawn)
                        continue;
                }
            }
            paintDragFeedback(g, metacomp);
        }
        
        g.setColor(oldColor);
        g.setStroke(oldStroke);
    }

    private void paintDragFeedback(Graphics2D g, RADVisualComponent metacomp) {
        Object comp = formDesigner.getComponent(metacomp);
        if (!(comp instanceof Component) || !((Component)comp).isShowing())
            return;

        Component component = (Component) comp;

        Rectangle rect = component.getBounds();
        rect = SwingUtilities.convertRectangle(component.getParent(),
                                               rect,
                                               handleLayer);

        rect.translate(mousePosition.x - hotspot.x,
                       mousePosition.y - hotspot.y);
        
        g.draw(new Rectangle2D.Double(rect.x, rect.y, rect.width, rect.height));

        if (metacomp instanceof RADVisualContainer) {
            RADVisualComponent[] children =
                ((RADVisualContainer)metacomp).getSubComponents();
            for (int i = 0; i < children.length; i++) {
                paintDragFeedback(g, children[i]);
            }
        }
    }

    private void paintTargetContainerFeedback(Graphics2D g, Container cont) {
        Stroke oldStroke = g.getStroke();
        Stroke stroke = new BasicStroke((float) 2.0,
                                            BasicStroke.CAP_SQUARE,
                                            BasicStroke.JOIN_MITER,
                                            (float) 10.0,
                                            new float[] { (float) 2.0, (float) 6.0 },
                                            0 );
        g.setStroke(stroke);

        Color oldColor = g.getColor();
        g.setColor(Color.red);
        
        Rectangle rect = new Rectangle(new Point(0,0), cont.getSize());
        rect = SwingUtilities.convertRectangle(cont,
                                               rect,
                                               handleLayer);
        g.draw(new Rectangle2D.Double(rect.x, rect.y, rect.width, rect.height));
        g.setColor(oldColor);
        g.setStroke(oldStroke);
    }
}
