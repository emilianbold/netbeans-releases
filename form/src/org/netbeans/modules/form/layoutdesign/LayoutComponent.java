/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.layoutdesign;

import java.awt.Rectangle;
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

    // -----
    // setup

    public LayoutComponent(String id, boolean isContainer) {
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

    // --------

    Iterator getSubcomponents() {
        return subComponents != null && subComponents.size() > 0 ?
               subComponents.iterator() : Collections.EMPTY_LIST.iterator();
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

    void setLayoutContainer(boolean isContainer) {
        if (isContainer != isLayoutContainer()) {
            if (isContainer) {
                layoutRoots = new LayoutInterval[DIM_COUNT];
                for (int i=0; i < DIM_COUNT; i++) {
                    layoutRoots[i] = new LayoutInterval(PARALLEL);
                }
            }
            else {
                layoutRoots = null;
                subComponents = null;
            }
        }
    }

    // -----
    // current state of the layout - current position and size of component
    // kept to be available quickly for the layout designer

    void setCurrentBounds(Rectangle bounds, int baseline) {
        LayoutRegion space = null;
        for (int i=0; i < layoutIntervals.length; i++) {
            if (space == null) {
                space = layoutIntervals[i].getCurrentSpace();
                space.set(bounds, baseline > 0 ? bounds.y + baseline : LayoutRegion.UNKNOWN);
            }
            else {
                layoutIntervals[i].setCurrentSpace(space);
            }
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
}
