/*
 * LinkCreateProvider.java
 *
 * Created on January 29, 2007, 12:55 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf.navigation.graph.actions;

import org.netbeans.modules.web.jsf.navigation.NavigableComponent;
import org.netbeans.modules.web.jsf.navigation.Page;
import org.netbeans.modules.web.jsf.navigation.Pin;
import org.netbeans.modules.web.jsf.navigation.graph.NavigationBridgeUtilities;
import java.awt.Point;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.web.jsf.navigation.graph.NavigationGraphScene;

/**
 *
 * @author joelle
 */
public class LinkCreateProvider implements ConnectProvider {
    
    private NavigationGraphScene graphScene;
    Page source = null;
    Page target = null;
    NavigableComponent navComp = null;
    
    /**
     * Creates a new instance of LinkCreateProvider
     */
    public LinkCreateProvider(NavigationGraphScene graphScene) {
        this.graphScene = graphScene;
    }
    
    public boolean isSourceWidget(Widget sourceWidget) {
        
        Object object = graphScene.findObject(sourceWidget);
        source = null;
        navComp = null;
        if (graphScene.isPin(object)){
            Pin pin = (Pin)object;
            source = pin.getPage();
            navComp = pin.getNavComp();
        } else if ( graphScene.isNode(object) ){
            source = (Page)object;
        }
        return source != null;
        
    }
    
    public ConnectorState isTargetWidget(Widget sourceWidget, Widget targetWidget) {
        target = null;
        Object object = graphScene.findObject(targetWidget);
        target = graphScene.isNode(object) ? (Page) object : null;
        if (target != null)
            return ConnectorState.ACCEPT;
        return object != null ? ConnectorState.REJECT_AND_STOP : ConnectorState.REJECT;
//
//        if (targetWidget instanceof VMDNodeWidget ) {
//            return ConnectorState.ACCEPT;
//        }
//
//        // Only allow it to be attached to the default pin.
////        if (graphScene.isPin(targetWidget) &&
////                ((Pin)graphScene.findObject(targetWidget)).getNavComp() == null ) {
////            return ConnectorState.ACCEPT;
////        }
//        return ConnectorState.REJECT_AND_STOP;
    }
    
    public boolean hasCustomTargetWidgetResolver(Scene scene) {
        return false;
    }
    
    public Widget resolveTargetWidget(Scene scene, Point sceneLocation) {
        return null;
    }
    
    public void createConnection(Widget sourceWidget, Widget targetWidget) {
        if ( sourceWidget != null && targetWidget != null ) {
            NavigationBridgeUtilities.getInstance().createLink(source, target, navComp);
        }
//            addEdge (edge);
//            setEdgeSource (edge, source);
//            setEdgeTarget (edge, target);
    }
    
}
