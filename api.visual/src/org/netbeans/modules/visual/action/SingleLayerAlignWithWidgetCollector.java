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
    private boolean outerBounds;

    public SingleLayerAlignWithWidgetCollector (LayerWidget collectionLayer, boolean outerBounds) {
        this.collectionLayer = collectionLayer;
        this.outerBounds = outerBounds;
    }

    public java.util.List<Rectangle> getRegions (Widget movingWidget) {
        java.util.List<Widget> children = collectionLayer.getChildren ();
        ArrayList<Rectangle> regions = new ArrayList<Rectangle> (children.size ());
        for (Widget widget : children)
            if (widget != movingWidget)
                regions.add (widget.convertLocalToScene (outerBounds ? widget.getBounds () : widget.getClientArea ()));
        return regions;
    }

}
