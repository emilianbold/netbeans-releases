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
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.router.CollisionsCollector;
import org.netbeans.api.visual.widget.ConnectionWidget;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author David Kaspar
 */
public final class OrthogonalSearchRouter implements Router {

    static final int SPACING_EDGE = 8;
    static final int SPACING_NODE = 16;

    private CollisionsCollector collector;

    public OrthogonalSearchRouter (CollisionsCollector collector) {
        this.collector = collector;
    }

    public java.util.List<Point> routeConnection (ConnectionWidget widget) {
        Anchor sourceAnchor = widget.getSourceAnchor ();
        Anchor targetAnchor = widget.getTargetAnchor ();
        if (sourceAnchor == null  ||  targetAnchor == null)
            return Collections.emptyList ();

        ArrayList<Rectangle> verticalCollisions = new ArrayList<Rectangle> ();
        ArrayList<Rectangle> horizontalCollisions = new ArrayList<Rectangle> ();
        collector.collectCollisions (verticalCollisions, horizontalCollisions);

        Anchor.Result sourceResult = sourceAnchor.compute(widget.getSourceAnchorEntry ());
        Anchor.Result targetResult = targetAnchor.compute(widget.getTargetAnchorEntry ());
        Point sourcePoint = sourceResult.getAnchorSceneLocation();
        Point targetPoint = targetResult.getAnchorSceneLocation();

        Solution bestSolution = new Solution (Integer.MAX_VALUE >> 2, Arrays.asList (sourcePoint, targetPoint));

        for (Anchor.Direction sourceDirection : sourceResult.getDirections ()) {
            for (Anchor.Direction targetDirection : targetResult.getDirections ()) {
                Solution solution = new OrthogonalSearchRouterCore (widget.getScene (), verticalCollisions, horizontalCollisions, sourcePoint, sourceDirection, targetPoint, targetDirection).route ();
                if (solution != null  &&  solution.compareTo (bestSolution) > 0)
                    bestSolution = solution;
            }
        }

        return bestSolution.getPoints ();
    }

    static final class Solution implements Comparable<Solution> {

        private int price;
        private List<Point> points;

        public Solution (int price, List<Point> points) {
            this.price = price;
            this.points = points;
        }

        public int getPrice () {
            return price;
        }

        public List<Point> getPoints () {
            return points;
        }

        public int compareTo (Solution other) {
            return other.price - price;
        }

    }

}
