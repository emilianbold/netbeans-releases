/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.api.visual.action;

import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.util.Collection;

/**
 * This interface is used for collecting regions for which the moving widget has to be checked.
 *
 * @author David Kaspar
 */
public interface AlignWithWidgetCollector {

    /**
     * Returns a collection of regions (in scene coordination system) for a specified moving widget.
     * @param movingWidget the moving widget
     * @return the collection of regions
     */
    Collection<Rectangle> getRegions (Widget movingWidget);

}
