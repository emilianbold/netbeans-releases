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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.navigation.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.visual.model.ObjectSceneEvent;
import org.netbeans.api.visual.model.ObjectSceneListener;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.vmd.VMDNodeWidget;
import org.netbeans.modules.web.jsf.navigation.NavigationCaseEdge;
import org.netbeans.modules.web.jsf.navigation.Page;
import org.netbeans.modules.web.jsf.navigation.PageFlowView;
import org.netbeans.modules.web.jsf.navigation.Pin;
import org.openide.nodes.Node;


/**
 *
 * @author joelle
 */
public class PFObjectSceneListener implements ObjectSceneListener {

    private static final UnsupportedOperationException uoe = new UnsupportedOperationException("Not supported yet.");
    PageFlowView tc;

    public PFObjectSceneListener(PageFlowView tc) {
        this.tc = tc;
    }

    public void objectAdded(ObjectSceneEvent event, Object addedObject) {
        throw uoe;
    }

    public void objectRemoved(ObjectSceneEvent event, Object removedObject) {
        throw uoe;
    }

    public void objectStateChanged(ObjectSceneEvent event, Object changedObject, ObjectState prevState, ObjectState newState) {
        throw uoe;
    }

    public void selectionChanged(ObjectSceneEvent event, Set<Object> prevSelection, Set<Object> newSelection) {
        PageFlowScene scene = (PageFlowScene) event.getObjectScene();
        Set<NavigationCaseEdge> releventEdges = new HashSet<NavigationCaseEdge>();
        Set<Node> selected = new HashSet<Node>();
        for (Object obj : newSelection) {
            if (obj instanceof PageFlowSceneElement) {
                PageFlowSceneElement element = (PageFlowSceneElement) obj;
                selected.add(element.getNode());
                releventEdges.addAll(getRelevantEdges(element, scene));
            }
        }
        scene.setHighlightedObjects(releventEdges);
        if (selected.isEmpty()) {
            tc.setDefaultActivatedNode();
        } else {
            tc.setActivatedNodes(selected.toArray(new Node[selected.size()]));
        }
    }

    public void highlightingChanged(ObjectSceneEvent event, Set<Object> prevHighlighting, Set<Object> newHighlighting) {
        throw uoe;
    }

    public void hoverChanged(ObjectSceneEvent event, Object prevHoveredObject, Object newHoveredObject) {
        throw uoe;
    }

    public void focusChanged(ObjectSceneEvent event, Object prevFocusedObject, Object newFocusedObject) {
        throw uoe;
    }

    private Set<NavigationCaseEdge> getRelevantEdges(PageFlowSceneElement element, PageFlowScene scene) {

        Set<NavigationCaseEdge> edgeSet = new HashSet<NavigationCaseEdge>();
        if (element instanceof Page) {
            Page page = (Page) element;
            Collection<Pin> pins = scene.getNodePins(page);
            for (Pin pin : pins) {
                Collection<NavigationCaseEdge> edges = scene.findPinEdges(pin, true, false);
                edgeSet.addAll(edges);
            }
        } else if ( element instanceof Pin){
            Collection<NavigationCaseEdge> edges = scene.findPinEdges((Pin)element, true, false);
            edgeSet.addAll(edges);
        }
        return edgeSet;
    }
}
