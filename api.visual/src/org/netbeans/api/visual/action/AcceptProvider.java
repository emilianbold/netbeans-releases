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
import java.awt.datatransfer.Transferable;

/**
 * This interface controls an accept (drag & drop) action.
 *
 * @author David Kaspar
 */
public interface AcceptProvider {

    /**
     * Checks whether a transferable can be dropped on a widget at a specific point.
     * @param widget the widget could be dropped
     * @param point the drop location in local coordination system of the widget
     * @param transferable the transferable
     * @return the state
     */
    ConnectorState isAcceptable (Widget widget, Point point, Transferable transferable);

    /**
     * Handles the drop of a transferable.
     * @param widget the widget where the transferable is dropped
     * @param point the drop location in local coordination system of the widget
     * @param transferable the transferable
     */
    void accept (Widget widget, Point point, Transferable transferable);

}
