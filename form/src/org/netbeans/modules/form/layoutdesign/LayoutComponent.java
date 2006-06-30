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

package org.netbeans.modules.form.layoutdesign;

import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;

/**
 * This class manages layout information about a component in the layout.
 * It refers to corresponding layout intervals (horizontal and vertical) in the
 * layout structure.
 *
 * A layout component can be found according to its Id in the layout model.
 *
 * The component may serve the role of layout container - then it also defines
 * top (root) intervals (horizontal and vertical) for its internal layouts.
 * 
 * @see LayoutInterval
 *
 * @author Tomas Pavek
 */

public final class LayoutComponent implements LayoutConstants {

    // Identification of the component in the model
    private String componentId;

    // The parent component of this component
    private LayoutComponent parentComponent;

    // Layout intervals representing the component in the layout hierarchy.
    // There is one interval for each dimension.
    private LayoutInterval[] layoutIntervals;

    // Potential resizability of the component in the design area.
    private boolean[] resizability;

    // Root layout intervals of a container layout. There is one interval for
    // each dimension. Defined by components that are layout containers, i.e.
    // managing layout of their subcomponents. Otherwise the array is null.
    private LayoutInterval[] layoutRoots;

    // Subcomponents of this component.
    private java.util.List subComponents;
    
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    // horizontal size-link
    private int horizontalLinkId = NOT_EXPLICITLY_DEFINED;
    
    // vertical size-link
    private int verticalLinkId = NOT_EXPLICITLY_DEFINED;
    
    // -----
    // setup

    public LayoutComponent(String id, boolean isContainer) {
        if (id == null)
            throw new NullPointerException();
        componentId = id;
        layoutIntervals = new LayoutInterval[DIM_COUNT];
        for (int i=0; i < DIM_COUNT; i++) {
            layoutIntervals[i] = new LayoutInterval(SINGLE);
            layoutIntervals[i].setComponent(this);
            layoutIntervals[i].setSizes(USE_PREFERRED_SIZE,
                                        NOT_EXPLICITLY_DEFINED,
                                        USE_PREFERRED_SIZE);
        }
        if (isContainer) {
            layoutRoots = new LayoutInterval[DIM_COUNT];
            for (int i=0; i < DIM_COUNT; i++) {
                layoutRoots[i] = new LayoutInterval(PARALLEL);
//                layoutRoots[i].setSizes(NOT_EXPLICITLY_DEFINED,
//                                        NOT_EXPLICITLY_DEFINED,
//                                        Short.MAX_VALUE);
            }
        }
    }

    public LayoutComponent(String id, boolean isContainer, int initialWidth, int initialHeight) {
        this(id, isContainer);
        if (isContainer) {
            for (int i=0; i < DIM_COUNT; i++) {
                LayoutInterval gap = new LayoutInterval(SINGLE);
                gap.setSizes(0, i==HORIZONTAL ? initialWidth : initialHeight, Short.MAX_VALUE);
                layoutRoots[i].add(gap, 0);
            }
        }
        else {
            layoutIntervals[HORIZONTAL].setPreferredSize(initialWidth);
            layoutIntervals[VERTICAL].setPreferredSize(initialHeight);
        }
    }

    void setLayoutInterval(LayoutInterval interval, int dimension) {
        layoutIntervals[dimension] = interval;
    }

    void setResizability(boolean[] resizability) {
        this.resizability = resizability;
    }

    boolean[] getResizability() {
        return resizability;
    }

    // -------
    // public methods

    public String getId() {
        return componentId;
    }

    public LayoutComponent getParent() {
        return parentComponent;
    }

    public boolean isParentOf(LayoutComponent comp) {
        do {
            comp = comp.getParent();
            if (comp == this)
                return true;
        }
        while (comp != null);
        return false;
    }

    public LayoutInterval getLayoutInterval(int dimension) {
        return layoutIntervals[dimension];
    }

    public boolean isLayoutContainer() {
        return layoutRoots != null;
    }

    public LayoutInterval getLayoutRoot(int dimension) {
        return layoutRoots[dimension];
    }
    
    LayoutInterval[] getLayoutRoots() {
        return layoutRoots;
    }

    // --------

    public Iterator getSubcomponents() {
        return subComponents != null && subComponents.size() > 0 ?
               subComponents.iterator() : Collections.EMPTY_LIST.iterator();
    }
    
    int getSubComponentCount() {
        return (subComponents == null) ? 0 : subComponents.size();
    }
    
    LayoutComponent getSubComponent(int index) {
        return (LayoutComponent)subComponents.get(index);
    }

    int indexOf(LayoutComponent comp) {
        return subComponents != null ? subComponents.indexOf(comp) : -1;
    }

//    int add(LayoutComponent comp) {
//        return add(comp, -1);
//    }

    int add(LayoutComponent comp, int index) {
        assert isLayoutContainer();

        if (subComponents == null) {
            subComponents = new LinkedList();
        }
        if (index < 0) {
            index = subComponents.size();
        }
        subComponents.add(index, comp);
        comp.parentComponent = this;

        return index;
    }

    int remove(LayoutComponent comp) {
        int index;
        if (subComponents != null) {
            index = subComponents.indexOf(comp);
            if (index >= 0) {
                subComponents.remove(index);
                comp.parentComponent = null;
            }
        }
        else index = -1;
        return index;
    }

    void setLayoutContainer(boolean isContainer, LayoutInterval[] roots) {
        if (isContainer != isLayoutContainer()) {
            if (isContainer) {
                if (roots == null) {
                    layoutRoots = new LayoutInterval[DIM_COUNT];
                    for (int i=0; i < DIM_COUNT; i++) {
                        layoutRoots[i] = new LayoutInterval(PARALLEL);
                    }
                } else {
                    layoutRoots = roots;
                }
            }
            else {
                layoutRoots = null;
                subComponents = null;
            }
        }
    }

    // -----

    static LayoutComponent getCommonParent(LayoutComponent comp1, LayoutComponent comp2) {
        // Find all parents of given components
        Iterator parents1 = parentsOfComponent(comp1).iterator();
        Iterator parents2 = parentsOfComponent(comp2).iterator();
        LayoutComponent parent1 = (LayoutComponent)parents1.next();
        LayoutComponent parent2 = (LayoutComponent)parents2.next();

        // Candidate for the common parent
        LayoutComponent parent = null;
        while (parent1 == parent2) {
            parent = parent1;
            if (parents1.hasNext()) {
                parent1 = (LayoutComponent)parents1.next();
            } else {
                break;
            }
            if (parents2.hasNext()) {
                parent2 = (LayoutComponent)parents2.next();
            } else {
                break;
            }
        }
        return parent;
    }

    private static List parentsOfComponent(LayoutComponent comp) {
        List parents = new LinkedList();
        while (comp != null) {
            parents.add(0, comp);
            comp = comp.getParent();
        }
        return parents;
    }

    // -----
    // current state of the layout - current position and size of component
    // kept to be available quickly for the layout designer

    void setCurrentBounds(Rectangle bounds, int baseline) {
        LayoutRegion space = layoutIntervals[0].getCurrentSpace();;
        space.set(bounds, baseline > 0 ? bounds.y + baseline : LayoutRegion.UNKNOWN);
        for (int i=1; i < layoutIntervals.length; i++) {
            layoutIntervals[i].setCurrentSpace(space);
        }
    }

    void setCurrentInterior(Rectangle bounds) {
        LayoutRegion space = null;
        for (int i=0; i < layoutRoots.length; i++) {
            if (space == null) {
                space = layoutRoots[i].getCurrentSpace();
                space.set(bounds, LayoutRegion.UNKNOWN);
            }
            else {
                layoutRoots[i].setCurrentSpace(space);
            }
        }
    }
    
    /**
     * @return whether this intervals size is linked with some other component in a direction horizontal or vertical
     */
    public boolean isLinkSized(int dimension) {
        if (dimension == HORIZONTAL) {
            return NOT_EXPLICITLY_DEFINED != horizontalLinkId;
        }
        return NOT_EXPLICITLY_DEFINED != verticalLinkId;
    }
    
    /**
     * @return whether this intervals size is linked with some other component in a direction horizontal or vertical
     */
    public int getLinkSizeId(int dimension) {
        if (dimension == HORIZONTAL) {
            return horizontalLinkId;
        }
        return verticalLinkId;
    }

    /**
     * @return whether this intervals size is linked with some other component in a direction horizontal or vertical
     */
    public void setLinkSizeId(int id, int dimension) {
        if (dimension == HORIZONTAL) {
            horizontalLinkId = id;
        } else {
            verticalLinkId = id;
        }
        
    }
    
    // Listener support
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
    
    void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
    
}
