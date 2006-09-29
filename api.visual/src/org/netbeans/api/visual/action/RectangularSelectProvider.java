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

import java.awt.*;

/**
 * This interface controls a rectangular select action.
 * @author David Kaspar
 */
public interface RectangularSelectProvider {

    /**
     * Called for perfoming a selection specified by a rectangle in scene coordination system.
     * @param sceneSelection the selection rectangle in scene coordination system.
     */
    void performSelection (Rectangle sceneSelection);

}
