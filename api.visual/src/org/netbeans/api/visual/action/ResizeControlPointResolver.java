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
 * This interface is used by ResizeAction to resolve if and which control point is being dragged for particular resizing.
 *
 * @author David Kaspar
 */
public interface ResizeControlPointResolver {

    /**
     * Resolves which control point is being dragged by user.
     * @param widget the widget where the user is invoking the resizing
     * @param point the mouse cursor location in local coordination system of the widget
     * @return the control point; if null, then resizing action is denied
     */
    ResizeProvider.ControlPoint resolveControlPoint (Widget widget, Point point);

}
