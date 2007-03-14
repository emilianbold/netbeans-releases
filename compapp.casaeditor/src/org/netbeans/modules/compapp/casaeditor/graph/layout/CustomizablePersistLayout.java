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

package org.netbeans.modules.compapp.casaeditor.graph.layout;

import java.awt.Point;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.graph.CasaNodeWidget;

/**
 *
 * @author Josh Sandusky
 */
public abstract class CustomizablePersistLayout implements Layout {
    
    private int mYSpacing;
    private boolean mIsPersisting;
    private boolean mIsAnimating;
    private boolean mIsAdjustingForOverlapOnly;
    
    
    public void justify(Widget widget) {
    }
    
    public boolean requiresJustification (Widget widget) {
        return false;
    }
    
    public void setYSpacing(int spacing) {
        mYSpacing = spacing;
    }
    
    public void setIsAdjustingForOverlapOnly(boolean isOverlapOnly) {
        mIsAdjustingForOverlapOnly = isOverlapOnly;
    }
    
    public void setIsPersisting(boolean isPersisting) {
        mIsPersisting = isPersisting;
    }

    public void setIsAnimating(boolean isAnimating) {
        mIsAnimating = isAnimating;
    }
    
    protected int getYSpacing() {
        return mYSpacing;
    }
    
    protected boolean isAdjustingForOverlapOnly() {
        return mIsAdjustingForOverlapOnly;
    }
    
    protected boolean isAnimating() {
        return mIsAnimating;
    }
    
    protected boolean isPersisting() {
        return mIsPersisting;
    }
    
    protected int moveWidget(CasaNodeWidget widget, Point location, int nextYStart) {
        if (mIsAdjustingForOverlapOnly) {
            location.y = nextYStart > widget.getLocation().y ? nextYStart : widget.getLocation().y;
            nextYStart = location.y + widget.getEntireBounds().height + mYSpacing;
        } else {
            nextYStart += widget.getEntireBounds().height + mYSpacing;
        }
        if (isPersisting()) {
            widget.persistLocation(location);
        }
        if (isAnimating()) {
            widget.getScene().getSceneAnimator().animatePreferredLocation(widget, location);
        } else {
            widget.setPreferredLocation(location);
        }
        return nextYStart;
    }
}
