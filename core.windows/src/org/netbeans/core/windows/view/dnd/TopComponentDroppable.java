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


package org.netbeans.core.windows.view.dnd;


import org.netbeans.core.windows.view.ViewElement;
import org.openide.windows.TopComponent;

import java.awt.*;


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
