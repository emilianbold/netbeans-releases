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

public interface VisualMapper {

    int PADDING_RELATED = 0;
    int PADDING_UNRELATED = 1;

//    String getTopComponentId();

    /**
     * Provides actual bounds (position and size) of a component - as it appears
     * in the visual design area. The position should be in coordinates of the
     * whole design visualization.
     * @param componentId
     * @return actual bounds of given component, null if the component is not
     *         currently visualized in the design area
     */
    Rectangle getComponentBounds(String componentId);
    
    /**
     * Provides preferred size of a component in the specified dimension.
     *
     * @param componentId ID of the component
     * @param dimension dimension (HORIZONTAL or VERTICAL)
     * @return preferred size of a component in the specified dimension.
     */
    int getComponentPreferredSize(String componentId, int dimension);

    /**
     * Provides actual position and size of the interior of a component
     * container - as it appears in the visual design area. (The interior
     * differs from the outer bounds in that it should reflect the borders
     * or other insets). The position should be in coordinates of the whole
     * design visualization.
     * @param componentId
     * @return actual interior of given component, null if the component is not
     *         currently visualized in the design area
     */
    Rectangle getContainerInterior(String componentId);

    /**
     * Provides preferred padding (optimal amount of space) between two components.
     * @param component1Id first component Id
     * @param component2Id second component Id
     * @param dimension the dimension (HORIZONTAL or VERTICAL) in which the
     *        components are positioned
     * @param comp2Alignment the edge (LEADING or TRAILING) at which the second
     *        component is placed next to the first component
     * @param paddingType padding type (PADDING_RELATED or PADDING_UNRELATED)
     * @return preferred padding (amount of space) between the given components
     */
    int getPreferredPadding(String component1Id,
                            String component2Id,
                            int dimension,
                            int comp2Alignment,
                            int paddingType);

    /**
     * Provides preferred padding (optimal amount of space) between a component
     * and its parent's border.
     * @param parentId Id of the parent container
     * @param componentId Id of the component
     * @param dimension the dimension (HORIZONTAL or VERTICAL) in which the
     *        component is positioned
     * @param compALignment the edge (LEADING or TRAILING) of the component
     *        which should be placed next to the parent's border
     * @return preferred padding (amount of space) between the component and its
     *         parent's border
     */
    int getPreferredPaddingInParent(String parentId,
                                    String componentId,
                                    int dimension,
                                    int compAlignment);

    int getBaselinePosition(String componentId);

    boolean[] getComponentResizability(String compId, boolean[] resizability);

    /**
     * Rebuilds the layout of given container. Called if LayoutDesigner needs
     * immediate update of the layout according to the model.
     */
    void rebuildLayout(String containerId);
}
