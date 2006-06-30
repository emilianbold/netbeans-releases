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
    static final int INTERVAL_LINKSIZE_CHANGED = 9;
    static final int CONTAINER_ATTR_CHANGED = 10;
    static final int COMPONENT_REGISTERED = 11;
    static final int COMPONENT_UNREGISTERED = 12;

    private int changeType;

    private LayoutComponent component;
    private LayoutComponent parentComp;
    private LayoutInterval interval;
    private LayoutInterval parentInt;
    private LayoutInterval[] layoutRoots;
    private int index;
    private int oldAlignment;
    private int newAlignment;
    private int oldAttributes;
    private int newAttributes;
    private int[] oldSizes;
    private int[] newSizes;

    private int oldLinkSizeId;
    private int newLinkSizeId;

    private int dimension;

    // -----
    // setup

    LayoutEvent(LayoutModel source, int changeType) {
        super(source);
        this.changeType = changeType;
    }
    
    void setComponent(LayoutComponent comp) {
        this.component = comp;
    }

    void setComponent(LayoutComponent comp, LayoutComponent parent, int index) {
        this.component = comp;
        this.parentComp = parent;
        this.index = index;
    }

    void setContainer(LayoutComponent comp, LayoutInterval[] roots) {
        this.component = comp;
        this.layoutRoots = roots;
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

    void setLinkSizeGroup(LayoutComponent component, int oldLinkSizeId, int newLinkSizeId, int dimension) {
        if (component == null) {
            Thread.dumpStack();
        }
        this.component = component;
        this.oldLinkSizeId = oldLinkSizeId;
        this.newLinkSizeId = newLinkSizeId;
        this.dimension = dimension;
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
            case INTERVAL_LINKSIZE_CHANGED:
                undoLinkSize(oldLinkSizeId);
                break;
            case CONTAINER_ATTR_CHANGED:
                changeContainerAttr();
                break;
            case COMPONENT_REGISTERED:
                undoComponentRegistration();
                break;
            case COMPONENT_UNREGISTERED:
                undoComponentUnregistration();
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
            case INTERVAL_LINKSIZE_CHANGED:
                undoLinkSize(newLinkSizeId);
                break;
            case CONTAINER_ATTR_CHANGED:
                changeContainerAttr();
                break;
            case COMPONENT_REGISTERED:
                undoComponentUnregistration();
                break;
            case COMPONENT_UNREGISTERED:
                undoComponentRegistration();
                break;
        }
    }
    
    private void undoLinkSize(int id) {
        getModel().removeComponentFromLinkSizedGroup(component, dimension);
        if (!(id == LayoutConstants.NOT_EXPLICITLY_DEFINED)) {
            getModel().addComponentToLinkSizedGroupImpl(id, component.getId(), dimension);
        }
    }

    private void undoComponentAddition() {
        getModel().removeComponentImpl(component);
    }

    private void undoComponentRemoval() {
        getModel().addComponentImpl(component, parentComp, index);
    }

    private void undoIntervalAddition() {
        getModel().removeInterval(parentInt, index);
    }

    private void undoIntervalRemoval() {
        getModel().addInterval(interval, parentInt, index);
    }
    
    private void changeContainerAttr() {
        component.setLayoutContainer(!component.isLayoutContainer(), layoutRoots);
    }
    
    private void undoComponentRegistration() {
        getModel().unregisterComponentImpl(component);
    }
    
    private void undoComponentUnregistration() {
        getModel().registerComponentImpl(component);
    }
    
}
