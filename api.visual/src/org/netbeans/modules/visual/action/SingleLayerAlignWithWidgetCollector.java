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
package org.netbeans.modules.visual.action;

import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.action.AlignWithWidgetCollector;

import java.awt.*;
import java.util.ArrayList;

/**
 * @author David Kaspar
 */
public final class SingleLayerAlignWithWidgetCollector implements AlignWithWidgetCollector {

    private LayerWidget collectionLayer;

    public SingleLayerAlignWithWidgetCollector (LayerWidget collectionLayer) {
        this.collectionLayer = collectionLayer;
    }

    public java.util.List<Rectangle> getRegions (Widget movingWidget) {
        java.util.List<Widget> children = collectionLayer.getChildren ();
        ArrayList<Rectangle> regions = new ArrayList<Rectangle> (children.size ());
        for (Widget widget : children)
            if (widget != movingWidget)
                regions.add (widget.convertLocalToScene (widget.getBounds ()));
        return regions;
    }

}
