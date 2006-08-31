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
package org.netbeans.api.visual.router;

import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.modules.visual.router.DirectRouter;
import org.netbeans.modules.visual.router.FreeRouter;
import org.netbeans.modules.visual.router.OrthogonalSearchRouter;
import org.netbeans.modules.visual.router.WidgetsCollisionCollector;

/**
 * @author David Kaspar
 */
public final class RouterFactory {

    private static final Router ROUTER_DIRECT = new DirectRouter ();
    private static final Router ROUTER_FREE = new FreeRouter ();


    private RouterFactory () {
    }

    public static Router createDirectRouter () {
        return ROUTER_DIRECT;
    }

    public static Router createFreeRouter () {
        return ROUTER_FREE;
    }

    public static Router createOrthogonalSearchRouter (LayerWidget... layers) {
        return createOrthogonalSearchRouter (createWidgetsCollisionCollector (layers));
    }

    public static Router createOrthogonalSearchRouter (CollisionsCollector collector) {
        assert collector != null;
        return new OrthogonalSearchRouter (collector);
    }

    private static CollisionsCollector createWidgetsCollisionCollector (LayerWidget... layers) {
        return new WidgetsCollisionCollector (layers);
    }

}
