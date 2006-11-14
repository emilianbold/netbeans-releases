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
