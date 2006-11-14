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
