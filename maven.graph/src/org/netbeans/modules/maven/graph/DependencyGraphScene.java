/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.maven.graph;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.netbeans.api.project.Project;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.EditProvider;
import org.netbeans.api.visual.action.MoveProvider;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.action.SelectProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.layout.SceneLayout;
import org.netbeans.api.visual.model.ObjectSceneEvent;
import org.netbeans.api.visual.model.ObjectSceneListener;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.maven.api.CommonArtifactActions;
import org.netbeans.modules.maven.indexer.api.ui.ArtifactViewer;

/**
 *
 * @author Milos Kleint 
 */
public class DependencyGraphScene extends GraphScene<ArtifactGraphNode, ArtifactGraphEdge> {
    
    private LayerWidget mainLayer;
    private LayerWidget connectionLayer;
    private ArtifactGraphNode rootNode;
    private final AllActionsProvider allActionsP = new AllActionsProvider();
    
//    private GraphLayout layout;
    private WidgetAction moveAction = ActionFactory.createMoveAction(null, allActionsP);
    private WidgetAction popupMenuAction = ActionFactory.createPopupMenuAction(allActionsP);
    private WidgetAction zoomAction = ActionFactory.createMouseCenteredZoomAction(1.1);
    private WidgetAction panAction = ActionFactory.createPanAction();
    private WidgetAction editAction = ActionFactory.createEditAction(allActionsP);
    private FruchtermanReingoldLayout layout;
    private int maxDepth = 0;
    private final MavenProject project;
    private final Project nbProject;
    private final DependencyGraphTopComponent tc;
    private SceneLayout fitViewL;
    
    /** Creates a new instance ofla DependencyGraphScene */
    DependencyGraphScene(MavenProject prj, Project nbProj, DependencyGraphTopComponent tc) {
        project = prj;
        nbProject = nbProj;
        this.tc = tc;
        mainLayer = new LayerWidget(this);
        addChild(mainLayer);
        connectionLayer = new LayerWidget(this);
        addChild(connectionLayer);
        //hoverAction = createObjectHoverAction();
        //getActions ().addAction (ActionFactory.createMouseCenteredZoomAction (1.1));
        getActions().addAction(this.createObjectHoverAction());
        getActions().addAction(ActionFactory.createSelectAction(allActionsP));
        getActions().addAction(zoomAction);
        getActions().addAction(panAction);
        getActions().addAction(editAction);
        //addObjectSceneListener(new SceneListener(), ObjectSceneEventType.OBJECT_HOVER_CHANGED, ObjectSceneEventType.OBJECT_SELECTION_CHANGED);
    }


    void cleanLayout(JScrollPane panel) {
//        GraphLayout layout = GraphLayoutFactory.createHierarchicalGraphLayout(this, true, false);
//        layout.layoutGraph(this);
        layout =  new FruchtermanReingoldLayout(this, panel);
        layout.invokeLayout();
    }
    
    ArtifactGraphNode getRootGraphNode() {
        return rootNode;
    }

    int getMaxNodeDepth() {
        return maxDepth;
    }

    Project getNbProject () {
        return nbProject;
    }

    MavenProject getMavenProject () {
        return project;
    }

    boolean isAnimated () {
        return true;
    }

    ArtifactGraphNode getGraphNodeRepresentant(DependencyNode node) {
        for (ArtifactGraphNode grnode : getNodes()) {
            if (grnode.represents(node)) {
                return grnode;
            }
        }
        throw new IllegalStateException();
    }
    
    protected Widget attachNodeWidget(ArtifactGraphNode node) {
        if (node.getPrimaryLevel() > maxDepth) {
            maxDepth = node.getPrimaryLevel();
        }
        Widget root = new ArtifactWidget(this, node);
        mainLayer.addChild(root);
        node.setWidget(root);
        if (rootNode == null) {
            rootNode = node;
        }
        root.setOpaque(true);
        
        root.getActions().addAction(this.createObjectHoverAction());
        root.getActions().addAction(this.createSelectAction());
        root.getActions().addAction(moveAction);
        root.getActions().addAction(editAction);
        root.getActions().addAction(popupMenuAction);
        
        return root;
    }
    
    protected Widget attachEdgeWidget(ArtifactGraphEdge edge) {
        EdgeWidget connectionWidget = new EdgeWidget(this, edge);
        connectionLayer.addChild(connectionWidget);
        return connectionWidget;
    }
    
    protected void attachEdgeSourceAnchor(ArtifactGraphEdge edge,
            ArtifactGraphNode oldsource,
            ArtifactGraphNode source) {
        ((ConnectionWidget) findWidget(edge)).setSourceAnchor(AnchorFactory.createRectangularAnchor(findWidget(source)));
        
    }
    
    protected void attachEdgeTargetAnchor(ArtifactGraphEdge edge,
            ArtifactGraphNode oldtarget,
            ArtifactGraphNode target) {
        ArtifactWidget wid = (ArtifactWidget)findWidget(target);
        ((ConnectionWidget) findWidget(edge)).setTargetAnchor(AnchorFactory.createRectangularAnchor(wid));
    }

    void highlightRelated (ArtifactGraphNode node) {
        List<ArtifactGraphNode> highlightNodes = new ArrayList<ArtifactGraphNode>();
        List<ArtifactGraphEdge> highlightEdges = new ArrayList<ArtifactGraphEdge>();

        highlightNodes.add(node);

        @SuppressWarnings("unchecked")
        List<DependencyNode> children = (List<DependencyNode>)node.getArtifact().getChildren();
        for (DependencyNode n : children) {
            highlightNodes.add(getGraphNodeRepresentant(n));
        }

        highlightEdges.addAll(findNodeEdges(node, true, false));

        DependencyNode curDepN = node.getArtifact();
        DependencyNode parentDepN;
        ArtifactGraphNode grNode;
        while ((parentDepN = curDepN.getParent()) != null) {
            grNode = getGraphNodeRepresentant(parentDepN);
            highlightEdges.addAll(findEdgesBetween(grNode, getGraphNodeRepresentant(curDepN)));
            highlightNodes.add(grNode);
            curDepN = parentDepN;
        }

        EdgeWidget ew;
        for (ArtifactGraphEdge curE : getEdges()) {
            ew = (EdgeWidget) findWidget(curE);
            if (highlightEdges.contains(curE)) {
                ew.setState(EdgeWidget.HIGHLIGHTED);
            } else {
                ew.setState(EdgeWidget.DISABLED);
            }
        }

        ArtifactWidget aw;
        boolean isHighlight;
        for (ArtifactGraphNode curN : getNodes()) {
            aw = (ArtifactWidget) findWidget(curN);
            isHighlight = highlightNodes.contains(curN);
            aw.setGrayed(!isHighlight);
            aw.setReadable(isHighlight);
        }

    }

    private class AllActionsProvider implements PopupMenuProvider, 
            MoveProvider, EditProvider, SelectProvider {

/*        public void select(Widget wid, Point arg1, boolean arg2) {
            System.out.println("select called...");
            Widget w = wid;
            while (w != null) {
                ArtifactGraphNode node = (ArtifactGraphNode)findObject(w);
                if (node != null) {
                    setSelectedObjects(Collections.singleton(node));
                    System.out.println("selected object: " + node.getArtifact().getArtifact().getArtifactId());
                    highlightRelated(node);
                    ((ArtifactWidget)w).setSelected(true);
                    return;
                }
                w = w.getParentWidget();
            }
        }*/

        /*** PopupMenuProvider ***/

        @SuppressWarnings("unchecked")
        public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {
            JPopupMenu popupMenu = new JPopupMenu();
            ArtifactGraphNode node = (ArtifactGraphNode)findObject(widget);
            Action a = CommonArtifactActions.createViewArtifactDetails(node.getArtifact().getArtifact(), project.getRemoteArtifactRepositories());
            a.putValue("PANEL_HINT", ArtifactViewer.HINT_GRAPH); //NOI18N
            popupMenu.add(a);
            return popupMenu;
        }

        /*** MoveProvider ***/

        public void movementStarted (Widget widget) {
            widget.bringToFront();
        }
        public void movementFinished (Widget widget) {
        }
        public Point getOriginalLocation (Widget widget) {
            return widget.getPreferredLocation ();
        }
        public void setNewLocation (Widget widget, Point location) {
            widget.setPreferredLocation (location);
        }

        /*** EditProvider ***/

        public void edit(Widget widget) {
            System.out.println("Edit called...");
            ArtifactGraphNode grN = (ArtifactGraphNode) findObject(widget);
            if (grN != null) {
                highlightRelated(grN);
            } else {
                // scene doubleclick
                getFitToViewLayout().invokeLayout();
            }
        }

        public boolean isAimingAllowed(Widget widget, Point localLocation, boolean invertSelection) {
            return false;
        }

        public boolean isSelectionAllowed(Widget widget, Point localLocation, boolean invertSelection) {
            return true;
        }

        public void select(Widget widget, Point localLocation, boolean invertSelection) {
            DependencyGraphScene.this.tc.depthHighlight();
        }
    }

    @Override
    protected void notifyStateChanged(ObjectState previousState, ObjectState state) {
        super.notifyStateChanged(previousState, state);

        if (!previousState.isSelected() && state.isSelected()) {
            tc.depthHighlight();
        }
    }

    private SceneLayout getFitToViewLayout () {
        if (fitViewL == null) {
            fitViewL = new FitToViewLayout(this);
        }
        return fitViewL;
    }

    private class FitToViewLayout extends SceneLayout {

        public FitToViewLayout(Scene scene) {
            super(scene);
        }

        @Override
        protected void performLayout() {
            Rectangle rectangle = new Rectangle (0, 0, 1, 1);
            for (Widget widget : getChildren ())
                rectangle = rectangle.union (widget.convertLocalToScene (widget.getBounds ()));
            rectangle.grow(5, 5);
            Dimension dim = rectangle.getSize();
            Dimension viewDim = DependencyGraphScene.this.tc.getScrollPane().
                    getViewportBorderBounds ().getSize ();
            double zf = Math.min ((double) viewDim.width / dim.width, (double) viewDim.height / dim.height);
            if (isAnimated()) {
                getSceneAnimator().animateZoomFactor(zf);
            } else {
                setZoomFactor (zf);
            }
        }
    }

    public class SceneListener implements ObjectSceneListener {
        
        public void selectionChanged(ObjectSceneEvent state,
                                     Set<Object> oldSet,
                                     Set<Object> newSet) {
        }

        public void objectAdded(ObjectSceneEvent event, Object addedObject) {
        }

        public void objectRemoved(ObjectSceneEvent event, Object removedObject) {
        }

        public void objectStateChanged(ObjectSceneEvent event, Object changedObject, ObjectState previousState, ObjectState newState) {
        }

        public void highlightingChanged(ObjectSceneEvent event, Set<Object> previousHighlighting, Set<Object> newHighlighting) {
        }

        public void hoverChanged(ObjectSceneEvent event, Object previousHoveredObject, Object newHoveredObject) {
            ArtifactGraphNode newH = (ArtifactGraphNode)newHoveredObject;
            ArtifactGraphNode prevH = (ArtifactGraphNode)previousHoveredObject;
            if (prevH != null) {
                // prevH.setHovering(false);
            }
            if (newH == null) {
                //hide detail component
            } else {
                //show detail component
            }
        }

        public void focusChanged(ObjectSceneEvent event, Object previousFocusedObject, Object newFocusedObject) {
        }

    }
}
