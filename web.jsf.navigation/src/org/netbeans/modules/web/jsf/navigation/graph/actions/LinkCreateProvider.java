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
 *
 * LinkCreateProvider.java
 *
 * Created on January 29, 2007, 12:55 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf.navigation.graph.actions;


import java.awt.Point;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.netbeans.modules.web.jsf.navigation.PageFlowController;
import org.netbeans.modules.web.jsf.navigation.Page;
import org.netbeans.modules.web.jsf.navigation.Pin;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowScene;

/**
 *
 * @author joelle
 */
public class LinkCreateProvider implements ConnectProvider {
    
    private final PageFlowScene graphScene;
    Page source = null;
    Page target = null;
    Pin pinNode = null;
    
    /**
     * Creates a new instance of LinkCreateProvider
     * @param graphScene
     *
     */
    public LinkCreateProvider(PageFlowScene graphScene) {
        this.graphScene = graphScene;
    }
    
    public boolean isSourceWidget(Widget sourceWidget) {
        
        Object object = graphScene.findObject(sourceWidget);
        source = null;
        pinNode = null;
        if (graphScene.isPin(object)){
            pinNode = (Pin)object;
            source = pinNode.getPage();
        } else if ( graphScene.isNode(object) ){
            source = (Page)object;
        }
        
        return source != null;
        
    }
    
    public ConnectorState isTargetWidget(Widget sourceWidget, Widget targetWidget) {
        target = null;
        Object object = graphScene.findObject(targetWidget);
        target = graphScene.isNode(object) ? (Page) object : null;
        if (target != null) {
            return ConnectorState.ACCEPT;
        }
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
        PageFlowController pfc = graphScene.getPageFlowView().getPageFlowController();
        if ( pfc != null && sourceWidget != null && targetWidget != null ) {
            
            NavigationCase caseNode = pfc.createLink(source, target, pinNode);
//            assert caseNode != null;
//            assert caseNode.getToViewId() != null;
//            assert caseNode.getFromOutcome() != null;
            
            graphScene.validate();
        }
        //            addEdge (edge);
        //            setEdgeSource (edge, source);
        //            setEdgeTarget (edge, target);
    }
    
}
