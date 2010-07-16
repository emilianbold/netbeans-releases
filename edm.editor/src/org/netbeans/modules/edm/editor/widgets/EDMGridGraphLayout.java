/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.edm.editor.widgets;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.Anchor.Entry;
import org.netbeans.api.visual.graph.layout.GraphLayout;
import org.netbeans.api.visual.graph.layout.UniversalGraph;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.edm.editor.ui.view.MashupDataObjectProvider;

/**
 *
 * @author Shankari
 */
public final class EDMGridGraphLayout<N, E> extends GraphLayout<N, E> {

    private boolean checker = false;
    private int horizontalGap = 64;
    private int verticalGap = 64;
    //static ObjectScene scene;
    static EDMGraphScene scene;
    public EDMGridGraphLayout() {
    }

    public EDMGridGraphLayout<EDMNodeWidget, E> setChecker(boolean checker) {
        this.checker = checker;
        return (EDMGridGraphLayout<EDMNodeWidget, E>) this;
    }

    public EDMGridGraphLayout<EDMNodeWidget, E> setGaps(int horizontalGap,
            int verticalGap) {
        this.horizontalGap = horizontalGap;
        this.verticalGap = verticalGap;
        return (EDMGridGraphLayout<EDMNodeWidget, E>) this;
    }
    LinkedList<EDMNodeWidget> queueForWidgets = new LinkedList<EDMNodeWidget>();

    protected void performGraphLayout(UniversalGraph<N, E> graph) {
        scene = (EDMGraphScene) graph.getScene();
        List<Widget> allNodesCollection = scene.getNodesInScene();
        HashSet<Widget> unresolvedNodesSet = new HashSet<Widget>(allNodesCollection);
        final HashMap<Widget, Collection<Widget>> node2connectedMap = new HashMap<Widget, Collection<Widget>>();
        HashSet<Widget> unconnectedNodesCollection = new HashSet<Widget>();
        HashSet<Widget> connectedNodesCollection = new HashSet<Widget>();
        LinkedList<Widget> queue = new LinkedList<Widget>();

        HashMap<Widget, Point> node2gridMap = new HashMap<Widget, Point>();
        HashMap<Widget, Point> Unconnectednodes2gridMap = new HashMap<Widget, Point>();
        Rectangle gridBounds = new Rectangle();
        HashSet<EDMNodeWidget> rootJoincoll = new HashSet<EDMNodeWidget>();
        HashSet<EDMNodeWidget> Joincoll = new HashSet<EDMNodeWidget>();
        HashMap<EDMNodeWidget, Collection<EDMNodeWidget>> widgetMap = new HashMap<EDMNodeWidget, Collection<EDMNodeWidget>>();
        for (Widget node : unresolvedNodesSet) {
            //EDMNodeWidget w = (EDMNodeWidget)node;
            HashSet<Widget> connectedSet = new HashSet<Widget>();
            try {
                HashMap<EDMNodeWidget, Anchor> edgesMap = scene.getEdgesMap();
                EDMNodeAnchor widget = (EDMNodeAnchor) edgesMap.get(node);
                connectedSet.add(widget.getRelatedWidget());
                List<Anchor.Entry> entries = widget.getEntries();
                for (Entry entry : entries) {
                    EDMNodeAnchor attached = (EDMNodeAnchor) entry.getAttachedAnchor();
                    EDMNodeAnchor opposite = (EDMNodeAnchor) entry.getOppositeAnchor();
                    EDMNodeWidget ndWidget1 = (EDMNodeWidget) attached.getRelatedWidget();
                    EDMNodeWidget ndWidget2 = (EDMNodeWidget) opposite.getRelatedWidget();
                    if (ndWidget1.getNodeName().trim().equalsIgnoreCase("ROOT JOIN")) {
                        rootJoincoll.add(ndWidget2);
                        widgetMap.put(ndWidget1, rootJoincoll);
                    } /*else if (ndWidget1.getNodeName().trim().equalsIgnoreCase("JOIN")) {
                if ((!ndWidget2.getNodeName().trim().equalsIgnoreCase("ROOT JOIN"))) {
                Joincoll.add(ndWidget2);
                widgetMap.put(ndWidget1, Joincoll);
                }
                }*/
                }
            } catch (Exception e) {
                //ignore
            }
            node2connectedMap.put(node, connectedSet);
        }
        for (;;) {
            Widget node = queue.isEmpty() ? findNodeWithMaxEdges(
                    unresolvedNodesSet, node2connectedMap) : queue.poll();
            if (node == null) {
                break;
            }
            queueForWidgets.add((EDMNodeWidget) node);

            unresolvedNodesSet.remove(node);
            Point index = new Point();
            ArrayList<Widget> connectedList = new ArrayList<Widget>(node2connectedMap.get(node));
            if (connectedList.size() == 0) { // Unconnected Nodes
                unconnectedNodesCollection.add(node);
                Point center1 = Unconnectednodes2gridMap.get(node);
                if (center1 == null) {
                    EDMNodeWidget ndWidget = (EDMNodeWidget) scene.findWidget(node);
                    center1 = findCenter(Unconnectednodes2gridMap);
                    Point grid = resolvePoint(Unconnectednodes2gridMap, center1, index);
                    Unconnectednodes2gridMap.put(node, grid);
                    gridBounds.add(grid);
                }
            } else { // Connected Nodes
                connectedNodesCollection.add(node);
                Point center = node2gridMap.get(node);
                if (center == null) {
                    center = findCenter(node2gridMap);
                    node2gridMap.put(node, center);
                    gridBounds.add(center);
                }
            }
        }//End of infinite for loop     
        layoutUnconnectedNodes(graph, unconnectedNodesCollection);
        layoutConnectedNodes(graph, connectedNodesCollection, widgetMap);
        placedNodes.clear();
    }
    HashSet<EDMNodeWidget> placedNodes = new HashSet<EDMNodeWidget>();

    protected void layoutConnectedNodes(UniversalGraph<N, E> graph, HashSet<Widget> connectedNodesCollection, HashMap<EDMNodeWidget, Collection<EDMNodeWidget>> widgetMap) {
        //boolean firstTime = true;
        Point p = new Point(10, 15);
        resolveChildren();
        Point pt = placeRootJoin(graph, p);//(10,15)
        Point pt1 = placeRootJoinChildrenNodes(graph, pt);//(129,15) and (129,63)       
        for (int i = 0; i < childrenMap.size() - 1; i++) {
            pt1 = placeJoinNodes(graph, getJoinWidget(), pt1);
        }
    }

    private Point placeRootJoin(UniversalGraph<N, E> graph, Point p) {
        setResolvedNodeLocationInGraph(graph, rootJoinWidget, new Point(10, 15));                
        return p;
    }

    private Point placeRootJoinChildrenNodes(UniversalGraph<N, E> graph, Point p) {
        if (p == null) {
            p = new Point();
        }
        List<Anchor.Entry> list = childrenMap.get(rootJoinWidget);
        EDMNodeWidget ndWidget1 = (EDMNodeWidget) list.get(0).getOppositeAnchor().getRelatedWidget();
        EDMNodeWidget ndWidget2 = (EDMNodeWidget) list.get(1).getOppositeAnchor().getRelatedWidget();
        if (ndWidget1.getNodeName().trim().equalsIgnoreCase("JOIN")) {
            setJoinWidget(ndWidget1);
        }

        p.y = p.y + rootJoinWidget.getBounds().height + 100;        
        setResolvedNodeLocationInGraph(graph, ndWidget1, new Point(10, p.y)); //10,150        
        int width = ndWidget1.getBounds().width + 150;
        int height1 = ndWidget1.getBounds().height;

        setResolvedNodeLocationInGraph(graph, ndWidget2, new Point(width, p.y));//150,250
        int height2 = ndWidget2.getBounds().height;
        h1 = Math.max(height1, height2);
        return p;
    }
    int h1;

    private Point placeJoinNodes(UniversalGraph<N, E> graph, EDMNodeWidget joinWd, Point p) {

        if (p == null) {
            p = new Point();
        }
        List<Anchor.Entry> list = childrenMap.get(joinWd);
        if (list != null) {
            EDMNodeWidget ndWidget1 = (EDMNodeWidget) list.get(0).getOppositeAnchor().getRelatedWidget();
            EDMNodeWidget ndWidget2 = (EDMNodeWidget) list.get(1).getOppositeAnchor().getRelatedWidget();
            if (ndWidget1.getNodeName().trim().equalsIgnoreCase("JOIN")) {
                setJoinWidget(ndWidget1);
            } else if (ndWidget2.getNodeName().trim().equalsIgnoreCase("JOIN")) {
                setJoinWidget(ndWidget2);
            }
            p.y = p.y + h1 + 100;            
            setResolvedNodeLocationInGraph(graph, ndWidget1, new Point(10, p.y)); //10,550
            int width = ndWidget1.getBounds().width + 150;
            int height1 = ndWidget1.getBounds().height;

            setResolvedNodeLocationInGraph(graph, ndWidget2, new Point(width, p.y)); //250,550
            int height2 = ndWidget2.getBounds().height;
            h1 = Math.max(height1, height2);
            return p;
        }
        return p;
    }
    private Map<EDMNodeWidget, List<Anchor.Entry>> childrenMap = new HashMap<EDMNodeWidget, List<Anchor.Entry>>();

    private void resolveChildren() {
        HashMap<EDMNodeWidget, Anchor> edgesMap = scene.getEdgesMap();
        for (EDMNodeWidget edmWd : queueForWidgets) {
            if (edmWd.getNodeName().trim().equalsIgnoreCase("Root Join")) {
                ArrayList<Anchor.Entry> entryList = new ArrayList<Anchor.Entry>();
                rootJoinWidget = edmWd;
                EDMNodeAnchor anchor = (EDMNodeAnchor) edgesMap.get(edmWd);
                List<Anchor.Entry> entries = anchor.getEntries();
                if (entries != null) {
                    for (int i = 0; i < entries.size(); i++) {
                        entryList.add(entries.get(i));
                    }
                    childrenMap.put(rootJoinWidget, entryList);
                }
            } else if (edmWd.getNodeName().trim().equalsIgnoreCase("Join")) {
                EDMNodeAnchor anchor = (EDMNodeAnchor) edgesMap.get(edmWd);
                ArrayList<Anchor.Entry> entryList = new ArrayList<Anchor.Entry>();
                List<Anchor.Entry> entries = anchor.getEntries();
                if (entries != null) {
                    for (int i = 1; i < entries.size(); i++) {
                        entryList.add(entries.get(i));
                    }
                    childrenMap.put(edmWd, entryList);
                }
            }
        }
    }
    EDMNodeWidget joinWidget = null;
    EDMNodeWidget rootJoinWidget = null;

    private EDMNodeWidget getJoinWidget() {
        return joinWidget;
    }

    private void setJoinWidget(EDMNodeWidget join) {
        joinWidget = join;
    }

    private void layoutUnconnectedNodes(UniversalGraph<N, E> graph, HashSet<Widget> allNodes) {
        int x = 350;
        int y = 5;
        for (Widget node : allNodes) {
            Point p = new Point(x, y);
            setResolvedNodeLocationInGraph(graph, node, p);
            int width = node.getBounds().width;
            x = x + width + 20;
        }

    }

    public void setResolvedNodeLocationInGraph(UniversalGraph<N, E> graph, Widget edmNodeWidget, Point newPreferredLocation) {
        if (!placedNodes.contains(edmNodeWidget)) {
            graph.getScene().getSceneAnimator().animatePreferredLocation(edmNodeWidget, newPreferredLocation);
            EDMNodeWidget wd = (EDMNodeWidget) edmNodeWidget;
            placedNodes.add(wd);
            try {
                MashupDataObjectProvider.getProvider().getActiveDataObject().persistGUIInfo(newPreferredLocation, (EDMNodeWidget) edmNodeWidget, edmNodeWidget.getBounds());
                MashupDataObjectProvider.getProvider().getActiveDataObject().setModified(false);
            } catch (Exception e) {
            }
        }
    }

    private <N> Point resolvePoint(HashMap<N, Point> node2grid,
            Point center, Point index) {
        for (;;) {
            int max = 8 * index.y;
            index.x++;
            if (index.x >= max) {
                index.y++;
                index.x -= max;
            }

            Point point = index2point(index);
            point.x += center.x;
            point.y += center.y;
            if (checker) {
                if (((point.x + point.y) & 1) != 0) {
                    continue;
                }

            }
            if (!isOccupied(node2grid, point)) {
                return point;
            }

        }
    }

    private <N> Point findCenter(HashMap<N, Point> node2grid) {
        int add = checker ? 2 : 1;
        for (int x = 0;; x +=
                        add) {
            Point point = new Point(x, 0);
            if (!isOccupied(node2grid, point)) {
                return point;
            }

        }
    }

    private static Point index2point(Point index) {
        int indexPos = index.x;
        int indexLevel = index.y;
        if (indexPos < indexLevel) {
            return new Point(indexLevel, indexPos);
        } else if (indexPos < 3 * indexLevel) {
            return new Point(indexLevel - (indexPos - indexLevel),
                    indexLevel);
        } else if (indexPos < 5 * indexLevel) {
            return new Point(-indexLevel, indexLevel - (indexPos - 3 * indexLevel));
        } else if (indexPos < 7 * indexLevel) {
            return new Point((indexPos - 5 * indexLevel) - indexLevel,
                    -indexLevel);
        } else if (indexPos < 8 * indexLevel) {
            return new Point(indexLevel, (indexPos - 7 * indexLevel) - indexLevel);
        }

        throw new InternalError("Index: " + indexPos);
    }

    private static <N> boolean isOccupied(HashMap<N, Point> node2grid,
            Point point) {
        for (Point p : node2grid.values()) {
            if (point.x == p.x && point.y == p.y) {
                return true;
            }

        }
        return false;
    }

    private static <N> N findNodeWithMaxEdges(
            HashSet<N> unresolvedNodes,
            HashMap<N, Collection<N>> node2connected) {
        N bestNode = null;
        int bestCount = Integer.MIN_VALUE;
        for (N node : unresolvedNodes) {
            int i = node2connected.get(node).size();
            if (i > bestCount) {
                bestNode = node;
                bestCount =
                        i;
            }

        }
        return bestNode;
    }

    protected void performNodesLayout(UniversalGraph<N, E> graph,
            Collection<N> nodes) {
    }
}
