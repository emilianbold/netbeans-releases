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

import java.util.EventObject;

/**
 * Holds information about a change in the layout model. Is able to undo/redo
 * the change.
 */

final class LayoutEvent extends EventObject {

    static final int COMPONENT_ADDED = 1;
    static final int COMPONENT_REMOVED = 2;
    static final int INTERVAL_ADDED = 3;
    static final int INTERVAL_REMOVED = 4;
    static final int INTERVAL_ALIGNMENT_CHANGED = 5;
    static final int GROUP_ALIGNMENT_CHANGED = 6;
    static final int INTERVAL_SIZE_CHANGED = 7;
    static final int INTERVAL_ATTRIBUTES_CHANGED = 8;

    private int changeType;

    private LayoutComponent component;
    private LayoutComponent parentComp;
    private LayoutInterval interval;
    private LayoutInterval parentInt;
    private int index;
    private int oldAlignment;
    private int newAlignment;
    private int oldAttributes;
    private int newAttributes;
    private int[] oldSizes;
    private int[] newSizes;

    // -----
    // setup

    LayoutEvent(LayoutModel source, int changeType) {
        super(source);
        this.changeType = changeType;
    }

    void setComponent(LayoutComponent comp, LayoutComponent parent, int index) {
        this.component = comp;
        this.parentComp = parent;
        this.index = index;
    }

    void setInterval(LayoutInterval interval, LayoutInterval parent, int index) {
        this.interval = interval;
        this.parentInt = parent;
        this.index = index;
    }

    void setAlignment(LayoutInterval interval, int oldAlign, int newAlign) {
        this.interval = interval;
        this.oldAlignment = oldAlign;
        this.newAlignment = newAlign;
    }

    void setAttributes(LayoutInterval interval, int oldAttributes, int newAttributes) {
        this.interval = interval;
        this.oldAttributes = oldAttributes;
        this.newAttributes = newAttributes;
    }

    void setSize(LayoutInterval interval,
                 int oldMin, int oldPref, int oldMax,
                 int newMin, int newPref, int newMax)
    {
        this.interval = interval;
        this.oldSizes = new int[] { oldMin, oldPref, oldMax };
        this.newSizes = new int[] { newMin, newPref, newMax };
    }

    // -----
    // getters

    LayoutModel getModel() {
        return (LayoutModel) source;
    }

    int getType() {
        return changeType;
    }

    LayoutComponent getComponent() {
        return component;
    }

    LayoutComponent getParentComponent() {
        return parentComp;
    }

    LayoutInterval getInterval() {
        return interval;
    }

    LayoutInterval getParentInterval() {
        return parentInt;
    }

    int getIndex() {
        return index;
    }

    // -----
    // undo & redo

    void undo() {
        switch (changeType) {
            case COMPONENT_ADDED:
                undoComponentAddition();
                break;
            case COMPONENT_REMOVED:
                undoComponentRemoval();
                break;
            case INTERVAL_ADDED:
                undoIntervalAddition();
                break;
            case INTERVAL_REMOVED:
                undoIntervalRemoval();
                break;
            case INTERVAL_ALIGNMENT_CHANGED:
                // getModel().setIntervalAlignment(interval, oldAlignment);
                interval.setAlignment(oldAlignment);
                break;
            case GROUP_ALIGNMENT_CHANGED:
                // getModel().setGroupAlignment(interval, oldAlignment);
                interval.setGroupAlignment(oldAlignment);
                break;
            case INTERVAL_SIZE_CHANGED:
                // getModel().setIntervalSize(interval, oldSizes[0], oldSizes[1], oldSizes[2]);
                interval.setSizes(oldSizes[0], oldSizes[1], oldSizes[2]);
                break;
            case INTERVAL_ATTRIBUTES_CHANGED:
                interval.setAttributes(oldAttributes);
                break;
        }
    }

    void redo() {
        switch (changeType) {
            case COMPONENT_ADDED:
                undoComponentRemoval();
                break;
            case COMPONENT_REMOVED:
                undoComponentAddition();
                break;
            case INTERVAL_ADDED:
                undoIntervalRemoval();
                break;
            case INTERVAL_REMOVED:
                undoIntervalAddition();
                break;
            case INTERVAL_ALIGNMENT_CHANGED:
                // getModel().setIntervalAlignment(interval, newAlignment);
                interval.setAlignment(newAlignment);
                break;
            case GROUP_ALIGNMENT_CHANGED:
                // getModel().setGroupAlignment(interval, newAlignment);
                interval.setGroupAlignment(newAlignment);
                break;
            case INTERVAL_SIZE_CHANGED:
                // getModel().setIntervalSize(interval, newSizes[0], newSizes[1], newSizes[2]);
                interval.setSizes(newSizes[0], newSizes[1], newSizes[2]);
                break;
            case INTERVAL_ATTRIBUTES_CHANGED:
                interval.setAttributes(newAttributes);
                break;
        }
    }

    private void undoComponentAddition() {
        getModel().removeComponent(component);
    }

    private void undoComponentRemoval() {
        getModel().addComponent(component, parentComp, index);
    }

    private void undoIntervalAddition() {
        getModel().removeInterval(parentInt, index);
    }

    private void undoIntervalRemoval() {
        getModel().addInterval(interval, parentInt, index);
    }
}
