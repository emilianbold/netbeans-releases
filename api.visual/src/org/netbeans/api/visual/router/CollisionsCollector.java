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
package org.netbeans.api.visual.router;

import java.awt.*;
import java.util.List;

/**
 * This class is used for collecting collision regions. There are two separate types of regions - vertical and horizontal.
 *
 * @author David Kaspar
 */
public interface CollisionsCollector {

    /**
     * Gathers collision collections and fill up the lists of vertical and horizontal collisions.
     * @param verticalCollisions the list of vertical collisions
     * @param horizontalCollisions the list of horizontal collisions
     */
    public void collectCollisions (List<Rectangle> verticalCollisions, List<Rectangle> horizontalCollisions);

}
