/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.core.windows.view.dnd;


import java.awt.Component;
import java.awt.Point;
import java.awt.Shape;

import org.netbeans.core.windows.view.ViewElement;

import org.openide.windows.TopComponent;


/**
 * Interface which allows container to provide support for dynamic
 * drop target indication, thus handling possible
 * drop operations for all its sub components and actually
 * provides the drop operation to the container.
 *
 * @author  Peter Zavadsky
 *
 * @see DropTargetGlassPane
 */
public interface TopComponentDroppable {
    /** Gets <code>Shape</code> object needed to used as indicator
     * of possible drop operation.
     * @param location within the container's glass pane coordinates */
    public Shape getIndicationForLocation(Point location);

    /** Gets constraint to be used for specified location
     * of possible drop operation.
     * @param location within the container's glass pane coordinates 
     * @return can return <code>null</code> if default constraints should
     *          should be used */
    public Object getConstraintForLocation(Point location);

    /** Gets actual drop component, i.e. the one which absobs the possible dropped
     * top component. Used to detect its bounds, for drop indication. */
    public Component getDropComponent();
    
    /** Gets view element into which to perform the drop operation. */
    public ViewElement getDropViewElement();
    
    // XXX
    /** Checks whether the specified TopComponent can be dropped. */
    public boolean canDrop(TopComponent transfer, Point location);
    
    // XXX
    /** Checks whether this droppable supports kind of winsys transfer.
     * Either <code>Constants.MODE_KIND_EDITOR</code> or <code>Constants.MODE_KIND_VIEW or both. */
    public boolean supportsKind(int kind, TopComponent transfer);
}
