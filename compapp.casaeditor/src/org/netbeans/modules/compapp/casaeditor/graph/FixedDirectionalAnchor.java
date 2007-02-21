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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
* FixedDirectionalAnchor.java
*
* Created on November 28, 2006, 9:50 PM
*
* To change this template, choose Tools | Template Manager
* and open the template in the editor.
*/

package org.netbeans.modules.compapp.casaeditor.graph;

import java.awt.Point;
import java.awt.Rectangle;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.Anchor.Direction;
import org.netbeans.api.visual.anchor.Anchor.Entry;
import org.netbeans.api.visual.anchor.Anchor.Result;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.graph.RegionUtilities.Directions;
import org.netbeans.modules.visual.util.GeomUtil;

/**
 *
 * @author rdara
 */
public final class FixedDirectionalAnchor extends Anchor {

    private Directions kind;
    private int gap;

    public FixedDirectionalAnchor(Widget widget, Directions kind, int gap) {
        super (widget);
        this.kind = kind;
        this.gap = gap;
    }

    public Result compute (Entry entry) {
        Point relatedLocation = getRelatedSceneLocation ();
        Point oppositeLocation = getOppositeSceneLocation (entry);

        Widget widget = getRelatedWidget();
        Rectangle bounds = widget.convertLocalToScene(widget.getBounds());
        Point center = GeomUtil.center (bounds);

        switch (kind) {
            case LEFT:
                    return new Anchor.Result (new Point (bounds.x - gap, center.y), Direction.LEFT);
            case RIGHT:
                    return new Anchor.Result (new Point (bounds.x + bounds.width + gap, center.y), Direction.RIGHT);
            case TOP:
                    return new Anchor.Result (new Point (center.x, bounds.y - gap), Direction.TOP);
            case BOTTOM:
                    return new Anchor.Result (new Point (center.x, bounds.y + bounds.height + gap), Direction.BOTTOM);
        }
        return null;
    }
};
