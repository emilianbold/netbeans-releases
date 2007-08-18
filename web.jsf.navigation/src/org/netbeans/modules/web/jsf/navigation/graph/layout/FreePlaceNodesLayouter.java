/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.web.jsf.navigation.graph.layout;

import org.netbeans.modules.web.jsf.navigation.graph.*;
import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.visual.model.ObjectSceneEvent;
import org.netbeans.api.visual.model.ObjectSceneEventType;
import org.netbeans.api.visual.model.ObjectSceneListener;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.Scene.SceneListener;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.web.jsf.navigation.Page;

/**
 * @author David Kaspar
 */
// TODO - perfomance - scalability problem
public final class FreePlaceNodesLayouter {
    PageFlowScene scene;
    public FreePlaceNodesLayouter(PageFlowScene scene ) {
        this(scene, false);
    }
    
    public static final void performLayout(PageFlowScene scene) {
        final FreePlaceNodesLayouter layout = new FreePlaceNodesLayouter(scene, true);
        final Collection<Page> pages = scene.getNodes();
        for( Page page: pages){
            final Widget widget = scene.findWidget(page);
            widget.setPreferredLocation(null);
        }
        scene.validate(); /* to make sure the nodes are set in a different place*/
        layout.layoutNodesLocations(scene, scene.getNodes());
        scene.validate();
    }
    
    private FreePlaceNodesLayouter(PageFlowScene scene, boolean isOneTimeUse){
        this.scene = scene;
        if( !isOneTimeUse){            
            scene.addSceneListener(new PageFlowSceneListener(scene));
            scene.addObjectSceneListener(new PageFlowObjectSceneListener(scene), ObjectSceneEventType.OBJECT_ADDED);
        }
    }
    
    public FreePlaceNodesLayouter( PageFlowScene scene, Rectangle visibleRectangle ){
        this(scene);
    }
    
    
    private final HashMap<String,Point> positions = new HashMap<String,Point> ();
    public void layoutNodesLocations( PageFlowScene scene, Collection<Page> nodes) {
        final Collection<Page> allNodes = scene.getNodes();
        for( Page node : nodes ) {
            final Widget nodeWidget = scene.findWidget(node);
            
            if( nodeWidget == null ) {
                return;
            } 

            if ( nodeWidget.getPreferredLocation() != null ) {
                /* If the getPreferredLocation has already been set by something else. */
                /* The unique ID for a node is it's display name defined by: node.getDisplayName() */
                positions.put(node.getDisplayName(), nodeWidget.getPreferredLocation());
            } else  {
                Point point = positions.get(node.getDisplayName());
                if ( point == null ) {
                    point = getNewComponentLocation(scene, positions, allNodes);
                }
                positions.put(node.getDisplayName(), point);
                nodeWidget.setPreferredLocation(point);
            }
        }
        
    }
    
    public void addNode( Page node ) {
        nodesAdded.add(node);
    }
    
    int SEP_X = 250;
    int SEP_Y = 150;
    private Point getNewComponentLocation(PageFlowScene scene, HashMap positions, Collection<Page> nodes) {
        for (int a = 0; ; a++) {
            for (int b = 0; b <= a; b++) {
                final int x =  SEP_Y + SEP_X * (a - b);
                final int y =  SEP_Y * (1 + b);
                if (isThereEmptyPlace(scene, positions, nodes, x, y)) {
                    return new Point(x, y);
                }
            }
        }
    }
    
    private boolean isThereEmptyPlace(PageFlowScene scene, HashMap positions, Collection<Page> nodes, int x, int y) {
        final Rectangle rectangle = new Rectangle(x, y, 100, 150);
        if (nodes != null) {
            for( Page node : nodes) {
                Point location = (Point) positions.get(node.getDisplayName());
                if (location == null) {
                    location = scene.findWidget(node).getLocation();
                }
                if (location != null && rectangle.contains(location)) {
                    return false;
                }
                
                final Rectangle bounds = scene.findWidget(node).getBounds();
                if (bounds != null && rectangle.contains(bounds)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private static final UnsupportedOperationException uoe = new UnsupportedOperationException("Not supported yet");
    
    
    Collection<Page> nodesAdded = new HashSet<Page>();
    private class PageFlowObjectSceneListener implements ObjectSceneListener{
        PageFlowScene scene;
        public PageFlowObjectSceneListener(PageFlowScene scene) {
            this.scene = scene;
        }
        
        public void objectAdded(ObjectSceneEvent event, Object addedObject) {
            if( scene.isNode(addedObject) ) {
                nodesAdded.add((Page)addedObject);
            }
        }
        
        public void objectRemoved(ObjectSceneEvent event, Object removedObject) {
            throw uoe;
        }
        
        public void objectStateChanged(ObjectSceneEvent event,
                Object changedObject,
                ObjectState prevState,
                ObjectState newState) {
            System.out.println("State Changed");
            throw uoe;
        }
        
        public void selectionChanged(ObjectSceneEvent event,
                Set<Object> prevSelection,
                Set<Object> newSelection) {
            throw uoe;
        }
        
        public void highlightingChanged(ObjectSceneEvent event,
                Set<Object> prevHighlighting,
                Set<Object> newHighlighting) {
            throw uoe;
        }
        
        public void hoverChanged(ObjectSceneEvent event,
                Object prevHoveredObject,
                Object newHoveredObject) {
            throw uoe;
        }
        
        public void focusChanged(ObjectSceneEvent event,
                Object prevFocusedObject,
                Object newFocusedObject) {
            throw uoe;
        }
        
    }
    /* Once validated is called all the nodes have been drawn.  Bounds and location
     * can then be determineded.  Set the final location and validate automatically
     * gets called one last time.
     */
    private class PageFlowSceneListener implements SceneListener {
        PageFlowScene scene;
        public PageFlowSceneListener(PageFlowScene scene) {
            this.scene = scene;
        }
        
        public void sceneRepaint() {
        }
        
        public void sceneValidating() {
        }
        
        public void sceneValidated() {
            if( nodesAdded.size() > 0 ) {
                layoutNodesLocations(scene, nodesAdded);
                nodesAdded.clear();
            }
            
        }
    };
    
    
}
