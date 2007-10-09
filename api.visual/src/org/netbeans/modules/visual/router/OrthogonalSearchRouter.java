/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.visual.router;

import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.router.CollisionsCollector;
import org.netbeans.api.visual.router.ConnectionWidgetCollisionsCollector;
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
    private ConnectionWidgetCollisionsCollector connectionWidgetCollector;

    public OrthogonalSearchRouter (CollisionsCollector collector) {
        this.collector = collector;
    }

    public OrthogonalSearchRouter (ConnectionWidgetCollisionsCollector collector) {
        this.connectionWidgetCollector = collector;
    }

    public java.util.List<Point> routeConnection (ConnectionWidget widget) {
        Anchor sourceAnchor = widget.getSourceAnchor ();
        Anchor targetAnchor = widget.getTargetAnchor ();
        if (sourceAnchor == null  ||  targetAnchor == null)
            return Collections.emptyList ();

        ArrayList<Rectangle> verticalCollisions = new ArrayList<Rectangle> ();
        ArrayList<Rectangle> horizontalCollisions = new ArrayList<Rectangle> ();
        if (collector != null)
            collector.collectCollisions (verticalCollisions, horizontalCollisions);
        else
            connectionWidgetCollector.collectCollisions (widget, verticalCollisions, horizontalCollisions);


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
