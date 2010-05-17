/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.uml.drawingarea.support;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.graph.layout.GraphLayout;
import org.netbeans.api.visual.graph.layout.GraphLayoutListener;
import org.netbeans.api.visual.graph.layout.UniversalGraph;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;

/**
 *
 * @author krichard
 */
public class ContainerAgnosticLayout<N, E> implements GraphLayoutListener {

    private GraphScene gScene = null;
    private ArrayList<Widget> hiddenWidgets = new ArrayList<Widget>();
    private ArrayList<RestoreEdge> restoreEdges = null;

    public ContainerAgnosticLayout(GraphScene gScene, GraphLayout layout) {
        layout.addGraphLayoutListener(this);
        this.gScene = gScene;

        //check the nodes to see if any of them are a child of a container node. If they
        //are, set visibility to false to trigger the UnviseralGraph not to return them,
        //and add them to a list for unhiding after the layout is complete.
        Collection<N> nodes = gScene.getNodes();
        for (N node : nodes) {

            Widget w = gScene.findWidget(node);

            if (w == null) {
                continue;
            }

            boolean onscene=false;
            Widget parent = w.getParentWidget();
            if(parent!=null && parent instanceof LayerWidget)
            {
                parent=parent.getParentWidget();
                if(parent!=null && parent instanceof DesignerScene)
                {
                    onscene=true;
                }
            }

            if (!onscene) {
                w.setVisible(false);
                hiddenWidgets.add(w);
            }

        }

        //check the connections going into and coming out of the hidden widgets. To make 
        //the layout work better, all connections that cross the boundary of a container
        //widget will have its appropriate end reassigned to the container node. If both
        //the target and the source are children of a container node, then the connection
        //will be hidden.

        restoreEdges = new ArrayList<RestoreEdge>();

        for (Widget w : hiddenWidgets) {
            Object node = gScene.findObject(w);

            //node is the source
            Collection<E> edges = gScene.findNodeEdges(node, true, false);

            // _______                       _______
            // |     |                       |     |
            // |  S  |---------------------->|  T  |
            // |_____|                       |_____|
            // 
            // (1) get the outgoing edges means that the node is the source.
            // (2) get the target
            // (3) check to see if the parent of the source is the same as the parent
            //     of the target. If the parents are the same, then the edge does
            //     not cross a container's boundary.
            //   (3a) if (same) do nothing
            //   (3b) if (not same) set the source of the connection to the current
            //        source's parent (this should be the container).
            //
            // note that this is repeated for the the case of the node being the 
            // target.

            for (E edge : edges) {
                ConnectionWidget cw = (ConnectionWidget) gScene.findWidget(edge);
                Widget target = cw.getTargetAnchor().getRelatedWidget();
                Widget parentWidget = w.getParentWidget();
                if (parentWidget != target.getParentWidget()) {

                    //it is possible that the parent is not the base widget. Find the
                    //base widget and connect source/target to it.
                    while (parentWidget!=null && parentWidget.getParentWidget()!=null && !(parentWidget.getParentWidget().getParentWidget() instanceof DesignerScene)) {
                        parentWidget = parentWidget.getParentWidget();
                    }

                    Object parent = gScene.findObject(parentWidget);
                    restoreEdges.add(new RestoreEdge(true, edge, node));
                    gScene.setEdgeSource(edge, parent);
                }
            }

            //This is the same code as above except for this has node as target
            //instead of source.
            edges = gScene.findNodeEdges(node, false, true);

            for (E edge : edges) {
                ConnectionWidget cw = (ConnectionWidget) gScene.findWidget(edge);
                Widget source = cw.getSourceAnchor().getRelatedWidget();
                Widget parentWidget = w.getParentWidget();
                if (parentWidget != source.getParentWidget()) {

                    while (parentWidget!=null && parentWidget.getParentWidget()!=null && !(parentWidget.getParentWidget().getParentWidget() instanceof DesignerScene)) {
                        parentWidget = parentWidget.getParentWidget();
                    }

                    Object parent = gScene.findObject(parentWidget);
                    restoreEdges.add(new RestoreEdge(false, edge, node));
                    gScene.setEdgeTarget(edge, parent);
                }
            }

        }
    }

    public void graphLayoutStarted(UniversalGraph graph) {
        //since the UniversalGraph is already build by the time this method is
        //called, all the "hiding" had to be done in the constructor.
    }

    public void graphLayoutFinished(UniversalGraph graph) {

        //The nodes in the containers were hidden so that the layout passed over
        //them. Unhide them in there original position.
        for (Widget widget : hiddenWidgets) {
            widget.setVisible(true);
        }

        //The edges that had their source or target end changed to layout the
        // container as a normal node need to be reset to their original configuration.
        for (RestoreEdge rEdge : restoreEdges) {
            if (rEdge.isSource) {
                gScene.setEdgeSource(rEdge.edge, rEdge.node);

            } else {
                gScene.setEdgeTarget(rEdge.edge, rEdge.node);
            }
        }
    }

    public void nodeLocationChanged(UniversalGraph graph, Object node, Point previousPreferredLocation, Point newPreferredLocation) {
        //do nothing
    }

    class RestoreEdge<E> {

        public final E edge;
        public final boolean isSource;
        public final Object node;

        public RestoreEdge(boolean isSource, E edge, Object node) {
            this.edge = edge;
            this.isSource = isSource;
            this.node = node;
        }
    }
}
