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

/**
 * This interface decorates a rectangular select action.
 *
 * @author David Kaspar
 */
public interface RectangularSelectDecorator {

    /**
     * Creates a widget which will be resized and placed into a scene and will represent the rectangular selection.
     * @return the selection widget
     */
    // TODO - API consistency maybe there could be a scene parameter added
    Widget createSelectionWidget ();

}
