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
package org.netbeans.modules.visual.graph.layout;

import org.netbeans.api.visual.graph.layout.*;
import java.awt.Point;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.visual.graph.layout.hierarchicalsupport.BarycenterCrossingMinimizer;
import org.netbeans.modules.visual.graph.layout.hierarchicalsupport.BarycenterXCoordinateAssigner;
import org.netbeans.modules.visual.graph.layout.hierarchicalsupport.DirectedGraph;
import org.netbeans.modules.visual.graph.layout.hierarchicalsupport.DirectedGraph.DummyVertex;
import org.netbeans.modules.visual.graph.layout.hierarchicalsupport.DirectedGraph.Vertex;
import org.netbeans.modules.visual.graph.layout.hierarchicalsupport.LayeredGraph;
import org.netbeans.modules.visual.graph.layout.hierarchicalsupport.VertexInsertionLayerAssigner;

/**
 * Takes the generic nodes and edges from a GraphScene and lays them out in 
 * hierarchical layers. Each node that is not a target node for an edge is marked
 * as a root of the graph. Multiple tree are generated for multiple roots.
 */
public class HierarchicalLayout<N, E> extends GraphLayout {

    private GraphScene<N, E> scene;
    private DirectedGraph graph;
    private final boolean animate;

    /**
     * Creates a new instance of HierarchicalLayout.
     * @param scene the scene containing the nodes and edges.
     */
    public HierarchicalLayout(GraphScene<N,E> scene, boolean animate) {
        this.scene = scene;
        this.animate = animate ;
    }

    /**
     * Called from UniversalGraph.layoutGraph
     * @param uGraph
     */
    @Override
    protected void performGraphLayout(UniversalGraph uGraph) {

        graph = DirectedGraph.createGraph(uGraph, scene);

//        TODO: add cycle removal. These cycles cause exceptions.
//        This does not currently do anything. 
//        EdgeReversingCycleRemover cycleRemover = new EdgeReversingCycleRemover();
//        graph = cycleRemover.removeCycles(graph);

        VertexInsertionLayerAssigner layerAssigner = new VertexInsertionLayerAssigner();
        LayeredGraph layeredGraph = layerAssigner.assignLayers(graph);

        BarycenterCrossingMinimizer crossingMinimizer = new BarycenterCrossingMinimizer();
        layeredGraph = crossingMinimizer.minimizeCrossings(layeredGraph);

        //assign grid coordinates. These are not graphical coordinates
        BarycenterXCoordinateAssigner coordinateAssigner = new BarycenterXCoordinateAssigner();
        layeredGraph = coordinateAssigner.assignCoordinates(layeredGraph);

        determineGraphicalCoordinates(layeredGraph);
        layoutNodes();
    }

    private void determineGraphicalCoordinates(LayeredGraph lGraph) {

        List<List<Vertex>> layers = lGraph.getLayers();

        int originX = 50;
        int originY = 50;

        int xGutter = 20;
        int yGutter = 100;

        int maxColNum = -1;

        //determine the grid coordinates
        int[] rows = new int[layers.size()]; //contains row heights

        Hashtable<Integer, Integer> cols = new Hashtable<Integer, Integer>();

        int layerCount = 0;
        for (List<Vertex> layer : layers) {
            int rowHeight = -1;

            for (Vertex v : layer) {
                //checking row height
                int vHeight = v.getSize().height;
                if (vHeight > rowHeight) {
                    rowHeight = vHeight;
                //checking column width
                }
                int col = (int) v.getX();
                if (col > maxColNum) {
                    maxColNum = col;
                }
                int width = v.getSize().width;

                col = col - 1; //adjust back to zero indexing

                Integer colWidth = cols.get(col); //get the current col width

                if (colWidth == null) {
                    cols.put(col, width);
                } else if (width > colWidth) {
                    cols.remove(col);
                    cols.put(col, width);
                }

            }
            rows[layerCount] = rowHeight;
            layerCount++;
        }

        Point[][] grid = new Point[rows.length][maxColNum];
        int y = originY;
        for (int row = 0; row < rows.length; row++) {
            int x = originX;
            for (int col = 0; col < maxColNum; col++) {
                grid[row][col] = new Point(x, y);
                x += cols.get(col) + xGutter;
            }

            y += rows[row] + yGutter;
        }

        layerCount = 0; //reusing variable

        for (List<Vertex> layer : layers) {
            for (Vertex v : layer) {
                if (!(v instanceof DummyVertex)) {
                    Point p = grid[layerCount][v.getX() - 1]; //adjusting for zero index

                    v.setX(p.x);
                    v.setY(p.y);
                } else {
                    //this is now a control point issue. But, we will handle the 
                    //routing with the current router.
//                    ((DummyVertex) v).getOriginalEdge().getEdgeDesignElement().addSegment(new Point2D.Float(x, y));
                }
            }
            layerCount++;
        }
    }


    /**
     * 
     * @param uGraph
     * @param nodes
     */
    @Override
    protected void performNodesLayout(UniversalGraph uGraph, Collection nodes) {
        //do nothing
    }

    private void layoutNodes() {
        //TODO: do you need to set the router to a direct router here. Maybe this 
        //should be done at the call, not here.

        Collection<Vertex> vertices = graph.getVertices();

        for (Vertex v : vertices) {

            N node = (N) v.getNodeDesignElement();
            //if the vertex is a dummy, there is no
            //node associated with it.
            if (node == null) {
                continue;
            }

            Widget w = scene.findWidget(node);

            Point p = new Point((int) v.getX(), (int) v.getY());

            if (animate)
                scene.getSceneAnimator().animatePreferredLocation(w, p);
            w.resolveBounds(p, w.getBounds());

        }
    }
}
