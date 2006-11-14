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
package org.netbeans.modules.visual.router;

import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.router.RouterFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author David Kaspar
 */
public class DirectRouter implements Router {

    public DirectRouter () {
    }

    public List<Point> routeConnection (ConnectionWidget widget) {
        ArrayList<Point> list = new ArrayList<Point> ();

        Anchor sourceAnchor = widget.getSourceAnchor ();
        Anchor targetAnchor = widget.getTargetAnchor ();
        if (sourceAnchor != null  &&  targetAnchor != null) {
            list.add (sourceAnchor.compute(widget.getSourceAnchorEntry ()).getAnchorSceneLocation());
            list.add (targetAnchor.compute(widget.getTargetAnchorEntry ()).getAnchorSceneLocation());
        }

        return list;
    }

}
