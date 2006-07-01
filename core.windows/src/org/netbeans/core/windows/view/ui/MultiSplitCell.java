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

package org.netbeans.core.windows.view.ui;

import java.awt.Component;
import org.netbeans.core.windows.view.ViewElement;

/**
 * A wrapper class for a component displayed in MultiSplitPane.
 */
class MultiSplitCell {

    private ViewElement view;
    //normalized resize weight, used internally only
    private double normalizedResizeWeight = 0.0;
    private double initialSplitWeight;
    //the size (widht or height) required by this component, used when resizing all split components
    private int requiredSize = -1;
    private boolean dirty = false;
    private boolean isHorizontalSplit;
    
    MultiSplitCell( ViewElement view, double initialSplitWeight, boolean isHorizontalSplit ) {
        this.view = view;
        this.initialSplitWeight = initialSplitWeight;
        this.isHorizontalSplit = isHorizontalSplit;
    }

    public boolean equals( Object o ) {
        if( o instanceof MultiSplitCell ) {
            MultiSplitCell cell = (MultiSplitCell)o;
            return getComponent().equals( cell.getComponent() );
        }
        return super.equals( o );
    }
    
    boolean isDirty() {
        return dirty;
    }
    
    void setDirty( boolean isDirty ) {
        this.dirty = isDirty;
    }
    
    void maybeResetToInitialSize( int newSize ) {
        if( -1 == requiredSize ) {
            requiredSize = getSize();
            if( requiredSize <= 0 || requiredSize >= newSize ) {
                requiredSize = (int)(newSize * initialSplitWeight + 0.5);
            }
            dirty = true;
        }
    }
    
    double getResizeWeight() {
        return view.getResizeWeight();
    }
    
    Component getComponent() {
        return view.getComponent();
    }

    /**
     * @param dividerSize The width of splitter bar.
     * @return The minimum size of this cell. If this cell is a split cell then the
     * result is a sum of minimum sizes of all children cells.
     */
    int getMinimumSize() {
        if( isHorizontalSplit )
            return getComponent().getMinimumSize().width;
        return getComponent().getMinimumSize().height;
    }
    
    int getRequiredSize() {
        if( -1 == requiredSize ) {
            if( isHorizontalSplit ) 
                return getComponent().getPreferredSize().width;
            return getComponent().getPreferredSize().height;
        }
        return requiredSize;
    }
    /**
     * Adjust cell's dimensions.
     */
    void layout( int x, int y, int width, int height ) {
        if( isHorizontalSplit ) {
            dirty |= x != getLocation() || requiredSize != width;
            requiredSize = width;
        } else {
            dirty |= y != getLocation() || requiredSize != height;
            requiredSize = height;
        }
        getComponent().setBounds( x, y, width, height );
    }
    
    void setRequiredSize( int newRequiredSize ) {
        dirty |= newRequiredSize != requiredSize;
        this.requiredSize = newRequiredSize;
    }
    
    int getLocation() {
        if( isHorizontalSplit )
            return getComponent().getLocation().x;
        return getComponent().getLocation().y;
    }
    
    int getSize() {
        if( isHorizontalSplit )
            return getComponent().getSize().width;
        return getComponent().getSize().height;
    }
    
    double getNormalizedResizeWeight() {
        return normalizedResizeWeight;
    }
    
    void setNormalizedResizeWeight( double newNormalizedResizeWeight ) {
        this.normalizedResizeWeight = newNormalizedResizeWeight;
    }
    
    ViewElement getViewElement() {
        return view;
    }
}
