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
 * The class is used for specifying a logic for accept action. An accept action can be created  
 * @author David Kaspar
 */
public interface AcceptProvider {

    /**
     *
     * @param widget
     * @param point
     * @param transferable
     * @return
     */
    boolean isAcceptable (Widget widget, Point point, Transferable transferable);

    void accept (Widget widget, Point point, Transferable transferable);

}
