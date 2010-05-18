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

package org.netbeans.modules.bpel.design.decoration;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.DiagramView;
import org.netbeans.modules.bpel.design.geometry.FPoint;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.design.model.patterns.CompositePattern;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;



public class ComponentsDescriptor implements Descriptor, Iterable<Component> {


    private List<PairComponentPositioner> pairs
            = new ArrayList<PairComponentPositioner>();


    public ComponentsDescriptor() {}
    
    
    public void add(Component component) {
        add(component, TOP_LR);
    }
    
    
    public void add(Component component, Positioner positioner) {
        myComponent = component;
        pairs.add(new PairComponentPositioner(component, positioner));
    }

    private Component myComponent;
    
    public Component getComponent() {
      return myComponent;
    }
    
    public void addAll(ComponentsDescriptor components) {
        int count = components.getComponentCount();
        for (int i = 0; i < count; i++) {
            add(components.getComponent(i), components.getPositioner(i));
        }
    }
    

    public int getComponentCount() {
        return pairs.size();
    }
    
    
    public Component getComponent(int i) {
        return pairs.get(i).getComponent();
    }
    
    
    public Positioner getPositioner(int i) {
        return pairs.get(i).getPositioner();
    }
    
    
    public Positioner getPositioner(Component c){
        for (PairComponentPositioner pair : pairs) {
            if (pair.getComponent() == c) {
                return pair.getPositioner();
            }
        }
        return null;
    }

    
    public Iterator<Component> iterator() {
        return new ComponentIterator(pairs);
    } 
    
    
    private static class PairComponentPositioner {
        private Component component;
        private Positioner positioner;
        
        
        public PairComponentPositioner(
                Component component, 
                Positioner positioner) 
        {
            this.component = component;
            this.positioner = positioner;
        }
        
        
        public Component getComponent() {
            return component;
        }
        
        
        public Positioner getPositioner() {
            return positioner;
        }
    }
    
    
    private static class ComponentIterator implements Iterator<Component> {
        
        private Iterator<PairComponentPositioner> iterator;
        
        public ComponentIterator(Collection<PairComponentPositioner> list) {
            iterator = list.iterator();
        }
        
        
        public boolean hasNext() { 
            return iterator.hasNext();
        }
        
        
        public Component next() {
            return iterator.next().getComponent();
        }
        
        
        public void remove() {
            iterator.remove();
        }
    }
    
    
    
    //counter-clockwise starting from topright conner
    
    public static final Positioner TOP_LR = new Positioner() {
        private static final int HSPACING = 0;
        private static final int VSPACING = 3;
        
        public void position(Pattern pattern, Collection<Component> components, 
                double zoom) 
        {
            
            FPoint topRight = getPatternBounds(pattern).getTopLeft();
            DiagramView view = pattern.getView();
            
            Point p = view.convertDiagramToScreen(topRight);
            
            
            int x = p.x;
            int y = p.y - VSPACING;
            
            for (Component component : components){
                
                Dimension size = component.getPreferredSize();
                int width = size.width;
                int height = size.height;
                
                component.setBounds(x, y - height, width, height);
                
                x -= width + HSPACING;
            }
        }
    };
    
    
    public static final Positioner TOP_RL = new Positioner() {
        private static final int HSPACING = 0;
        private static final int VSPACING = 3;
        
        public void position(Pattern pattern, Collection<Component> components, 
                double zoom) 
        {
            FPoint topRight = getPatternBounds(pattern).getTopRight();
            
            DiagramView view = pattern.getView();
            
            Point p = view.convertDiagramToScreen(topRight);
            
            int x = p.x;
            int y = p.y - VSPACING;
            
            for (Component component : components){
                
                Dimension size = component.getPreferredSize();
                int width = size.width;
                int height = size.height;
                
                component.setBounds(x - width, y - height, width, height);
                
                x -= width + HSPACING;
            }
        }
        
    };


    public static final Positioner LEFT_TB = new Positioner() {
        private static final int HSPACING = 2;
        private static final int VSPACING = 0;
        
        public void position(Pattern pattern, Collection<Component> components, 
                double zoom) {
            
            FPoint topLeft = getPatternBounds(pattern).getTopLeft();
            
            DiagramView view = pattern.getView();
            
            Point p = view.convertDiagramToScreen(topLeft);
            
            int x = p.x - HSPACING;
            int y = p.y;
            
            for (Component component : components){
                Dimension size = component.getPreferredSize();
                int width = size.width;
                int height = size.height;
                
                component.setBounds(x - width, y, width, height);
                
                y += height + VSPACING;
            }
        }
        
    };
    
    
    
    public static final Positioner TOP_CENTER = new Positioner() {
        private static final int HSPACING = 2;
        
        public void position(Pattern pattern, Collection<Component> components, 
                double zoom) 
        {
            VisualElement element = null;
                    
            if (pattern instanceof CompositePattern) {
                element = ((CompositePattern) pattern).getBorder();
                if (element == null) {
                    element = pattern.getFirstElement();
                }
            } else {
                element = pattern.getFirstElement();
            }
            
             DiagramView view = pattern.getView();
            
            Point center = view.convertDiagramToScreen(new FPoint(element.getCenterX(), element.getY()));
            
            
            int width = 0;
            
            for (Component c : components) {
                width += c.getPreferredSize().width;
            }
            
            width += Math.max(0, (components.size() - 1) * HSPACING);
            
            int y = center.y;
            int x = center.x - width / 2;
            
            for (Component c : components) {
                Dimension size = c.getPreferredSize();
                c.setBounds(x, y - size.height / 2, size.width, size.height);
                x += size.width + HSPACING;
            }
        }
    };
    
    
    
    public static final Positioner RIGHT_TB = new Positioner() {
        private static final int HSPACING = 2;
        private static final int VSPACING = 0;
        
        public void position(Pattern pattern, Collection<Component> components, 
                double zoom) 
        {
            
            FPoint topRight = getPatternBounds(pattern).getTopRight();
            
            DiagramView view = pattern.getView();
            
            Point p = view.convertDiagramToScreen(topRight);
            
            int x = p.x + HSPACING;
            int y = p.y;
            
            for (Component component : components){
                Dimension size = component.getPreferredSize();
                int width = size.width;
                int height = size.height;
                
                component.setBounds(x, y, width, height);
                
                y += height + VSPACING;
            }
        }
        
    };
    
    
    //clockwise starting from bottomright conner
    public static final Positioner BOTTOM_RL = new Positioner() {
        private static final int SPACING = 2;
        
        public void position(Pattern pattern, Collection<Component> components, 
                double zoom) 
        {
            
            FPoint bottomRight = pattern.getBounds().getBottomRight();
            
            DiagramView view = pattern.getView();
            
            Point p = view.convertDiagramToScreen(bottomRight);
            
            int x = p.x;
            int y = p.y + SPACING;
            
            for (Component component: components){
                Dimension size = component.getPreferredSize();
                x -= size.width;
                component.setBounds(x, y, size.width, size.height);
                x -= SPACING;
            }
        }
    };
    
}
