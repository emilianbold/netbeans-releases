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
package org.netbeans.api.visual.router;

import org.netbeans.api.visual.widget.ConnectionWidget;

import java.awt.*;
import java.util.List;

/**
 * This class is responsible for routing path of connection widgets.
 * Built-in routers could be created by RouterFactory class. Routers are assigned to connection widgets using
 * ConnectionWidget.setRouter method.
 *
 * @author David Kaspar
 */
public interface Router {

    /**
     * Routes a path for a connection widget. The path is specified by a list of control points.
     * @param widget the connection widget
     * @return the list of control points
     */
    public List<Point> routeConnection (ConnectionWidget widget);

}
