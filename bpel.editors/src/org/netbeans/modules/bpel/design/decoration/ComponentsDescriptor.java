/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
        pairs.add(new PairComponentPositioner(component, positioner));
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
            
            Point p = DesignView.convertDiagramToScreen(topRight, zoom);
            
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
            
            Point p = DesignView.convertDiagramToScreen(topRight, zoom);
            
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
            
            Point p = DesignView.convertDiagramToScreen(topLeft, zoom);
            
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
            
            Point center = DesignView.convertDiagramToScreen(
                    new FPoint(element.getCenterX(), element.getY()), zoom);
            
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
            
            Point p = DesignView.convertDiagramToScreen(topRight, zoom);
            
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
            
            Point p = DesignView.convertDiagramToScreen(bottomRight, zoom);
            
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
