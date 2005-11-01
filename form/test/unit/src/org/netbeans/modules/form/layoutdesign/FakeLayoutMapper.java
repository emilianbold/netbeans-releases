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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.HashMap;
import org.netbeans.modules.form.FormModel;

/**
 * VisualMapper implementation for layout tests. Works based on explicitly set
 * bounds and baseline positions.
 */

public class FakeLayoutMapper implements VisualMapper, LayoutConstants {

    private FormModel fm = null;
    private HashMap contInterior = null;
    private HashMap baselinePosition = null;
    private HashMap prefPaddingInParent = null;
    private HashMap prefPadding = null;
    private HashMap compBounds = null;
    private HashMap compMinSize = null;
    private HashMap compPrefSize = null;
    private HashMap hasExplicitPrefSize = null;
    
    public FakeLayoutMapper(FormModel fm, 
                            HashMap contInterior, 
                            HashMap baselinePosition, 
                            HashMap prefPaddingInParent,
                            HashMap compBounds,
                            HashMap compMinSize,
                            HashMap compPrefSize,
                            HashMap hasExplicitPrefSize,
                            HashMap prefPadding) {
        this.fm = fm;
        this.contInterior = contInterior;
        this.baselinePosition = baselinePosition;
        this.prefPaddingInParent = prefPaddingInParent;
        this.compBounds = compBounds;
        this.compMinSize = compMinSize;
        this.compPrefSize = compPrefSize;
        this.hasExplicitPrefSize = hasExplicitPrefSize;
        this.prefPadding = prefPadding;
    }
    
    // -------

    public Rectangle getComponentBounds(String componentId) {
        return (Rectangle) compBounds.get(componentId);
    }

    public Rectangle getContainerInterior(String componentId) {
        return (Rectangle) contInterior.get(componentId);
    }

    public Dimension getComponentMinimumSize(String componentId) {
        return (Dimension) compMinSize.get(componentId);
    }

    public Dimension getComponentPreferredSize(String componentId) {
        return (Dimension) compPrefSize.get(componentId);
    }

    public boolean hasExplicitPreferredSize(String componentId) {
        return ((Boolean) hasExplicitPrefSize.get(componentId)).booleanValue();
    }

    public int getBaselinePosition(String componentId, int width, int height) {
        String id = componentId + "-" + width + "-" + height; //NOI18N
        return ((Integer) baselinePosition.get(id)).intValue();
    }

    public int getPreferredPadding(String comp1Id,
                                   String comp2Id,
                                   int dimension,
                                   int comp2Alignment,
                                   int paddingType)
    {
        String id = comp1Id + "-" + comp2Id  + "-" + dimension + "-" + comp2Alignment + "-" + paddingType; //NOI18N
        Integer pad = (Integer) prefPadding.get(id);
        return pad != null ? pad.intValue() : 6;
    }

    public int getPreferredPaddingInParent(String parentId,
                                           String compId,
                                           int dimension,
                                           int compAlignment)
    {
        String id = parentId + "-" + compId + "-" + dimension + "-" + compAlignment; //NOI18N
        Integer pad = (Integer) prefPaddingInParent.get(id);
        return pad != null ? pad.intValue() : 10;
    }

    public boolean[] getComponentResizability(String compId, boolean[] resizability) {
        resizability[0] = resizability[1] = true;
        return resizability;
    }

    public void rebuildLayout(String contId) {
    }
    
}
