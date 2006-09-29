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

/**
 * This interface controls a select action.
 *
 * @author David Kaspar
 */
public interface SelectProvider {

    /**
     * Called to check whether aiming is allowed
     * @param widget the aimed widget
     * @param localLocation the local location of a mouse cursor while aiming is invoked by an user
     * @param invertSelection if true, then the invert selection is invoked by an user.
     * @return true, if aiming is allowed and widget is set to aimed state while mouse button is pressed;
     *         false, if aiming is disallowed and widget is not set to aimed state at any time.
     */
    boolean isAimingAllowed (Widget widget, Point localLocation, boolean invertSelection);

    /**
     * Called to check whether the selection is allowed.
     * @param widget the selected widget
     * @param localLocation the local location of a mouse cursor while selection is invoked by an user
     * @param invertSelection if true, then the invert selection is invoked by an user.
     * @return true, if selection is allowed; false, if selection is disallowed
     */
    boolean isSelectionAllowed (Widget widget, Point localLocation, boolean invertSelection);

    /**
     * Called to perform the selection.
     * @param widget the selected widget
     * @param localLocation the local location of a mouse cursor while selection is invoked by an user
     * @param invertSelection if true, then the invert selection is invoked by an user.
     */
    void select (Widget widget, Point localLocation, boolean invertSelection);

}
