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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.compapp.casaeditor.design;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.Constants;
import org.netbeans.modules.compapp.casaeditor.graph.CasaNodeWidget;
import org.netbeans.modules.compapp.casaeditor.graph.CasaPinWidget;
import org.netbeans.modules.compapp.casaeditor.graph.CasaRegionWidget;
import org.netbeans.modules.compapp.casaeditor.graph.RegionUtilities;
import org.netbeans.modules.compapp.casaeditor.graph.layout.CasaCollisionCollector;
import org.netbeans.modules.compapp.casaeditor.graph.layout.CasaOrthogonalSearchRouter;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConnection;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConsumes;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpointRef;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServiceEngineServiceUnit;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPort;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaProvides;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaRegion;
import org.netbeans.modules.compapp.projects.jbi.api.JbiDefaultComponentInfo;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Josh Sandusky
 */
public class CasaModelGraphUtilities {
    
    
    public static void renderModel(final CasaWrapperModel model, final CasaModelGraphScene scene)
    {
        try {
            safeRenderModel(model, scene);
        } catch (final Throwable t) {
            scene.autoLayout(false);
            ErrorManager.getDefault().notify(t);
        }
    }
    
    private static void safeRenderModel(CasaWrapperModel model, CasaModelGraphScene scene)
    {
        if (model == null || scene == null) {
            return;
        }
        
        boolean wasModified = scene.isModified();
        
        // clean up any pre-existing model widgets
        
        for (CasaComponent ob : new ArrayList<CasaComponent>(scene.getEdges())) {
            scene.removeEdge(ob);
        }
        for (CasaComponent ob : new ArrayList<CasaComponent>(scene.getNodes())) {
            scene.removeNode(ob);
        }
        for (CasaComponent ob : new ArrayList<CasaComponent>(scene.getRegions())) {
            scene.removeRegion(ob);
        }
        
        // add JBIComponent objects to scene
        
        // add regions
        createRegions(model, scene);
        
        // add nodes and ports
        for (CasaPort casaPort : model.getCasaPorts()) {
            createNode(casaPort, model, scene, casaPort.getX(), casaPort.getY());
        }
        
        for (CasaServiceEngineServiceUnit su : model.getServiceEngineServiceUnits()) {
            createNode(su, model, scene, su.getX(), su.getY());
        }
        

        // validate, and layout if required
        scene.validate();
        renderLayout(scene);
        
        
        // add connections last
        for (CasaConnection connection : model.getCasaConnectionList(false)) {
            CasaConsumes consumes = (CasaConsumes) model.getCasaEndpointRef(connection, true);
            CasaProvides provides = (CasaProvides) model.getCasaEndpointRef(connection, false);
            if (consumes != null && provides != null) {
                createEdge(connection, consumes, provides, scene, false);
            }
        }
        scene.setOrthogonalRouter(new CasaOrthogonalSearchRouter(new CasaCollisionCollector(
                scene.getBindingRegion(),
                scene.getEngineRegion(),
                scene.getExternalRegion(),
                scene.getConnectionLayer())));
        scene.updateEdgeRouting();
        scene.validate();
        
        
        if (!wasModified && scene.isModified()) {
            // Auto-layouts may cause widget positions to fix themselves.
            // In this case, the modified flag will be true - we need to save.
            scene.save();
        }
    }

    private static void renderLayout(final CasaModelGraphScene scene) {
        boolean isPartial = false;
        boolean isFull    = true;
        
        // determine what kind of layout is required, if any
        for (CasaComponent node : scene.getNodes()) {
            Widget widget = scene.findWidget(node);
            if (!(widget instanceof CasaNodeWidget)) {
                continue;
            }
            Point point = widget.getPreferredLocation();
            boolean isBadPoint = point == null || point.x < 0 || point.y < 0;
            if (isBadPoint) {
                isPartial = true;
            }
            if (node instanceof CasaPort && !isBadPoint) {
                isFull = false;
            }
            if (node instanceof CasaServiceEngineServiceUnit) {
                CasaServiceEngineServiceUnit su = (CasaServiceEngineServiceUnit) node;
                if (su.isDefined() && !isBadPoint) {
                    isFull = false;
                }
                if (!su.isInternal() && !isBadPoint) {
                    isFull = false;
                }
            }
        }
        
        if (isPartial && !isFull) {
            scene.progressiveLayout(true);
            RegionUtilities.stretchScene(scene);
        } else if (isFull) {
            scene.autoLayout(false);
        }
    }
    
    /**
     * Callers to this method must remember to call scene.validate().
     */
    public static void createRegions(CasaWrapperModel model, CasaModelGraphScene scene) {

        CasaRegion bindingRegion  = model.getCasaRegion(CasaRegion.Name.WSDL_ENDPOINTS);
        CasaRegion engineRegion   = model.getCasaRegion(CasaRegion.Name.JBI_MODULES);
        CasaRegion externalRegion = model.getCasaRegion(CasaRegion.Name.EXTERNAL_MODULES);

        CasaRegionWidget bindingRegionWidget  = (CasaRegionWidget) scene.addRegion(bindingRegion);
        CasaRegionWidget engineRegionWidget   = (CasaRegionWidget) scene.addRegion(engineRegion);
        CasaRegionWidget externalRegionWidget = (CasaRegionWidget) scene.addRegion(externalRegion);

        // Ensure the engine region is on top so that its banner, if displayed,
        // won't get hidden by the other regions.
        engineRegionWidget.bringToFront();

        bindingRegionWidget.setPreferredLocation(new Point(  0, 0));
        int bindingWidth = bindingRegion.getWidth();
        if (bindingWidth <= 0) {
            bindingWidth = 200;
        }
        bindingRegionWidget.setPreferredBounds(new Rectangle(bindingWidth, RegionUtilities.DEFAULT_HEIGHT));

        engineRegionWidget.setPreferredLocation(new Point(bindingWidth, 0));
        int engineWidth = engineRegion.getWidth();
        if (engineWidth <= 0) {
            engineWidth = 500;
        }
        engineRegionWidget.setPreferredBounds(new Rectangle(engineWidth, RegionUtilities.DEFAULT_HEIGHT));

        externalRegionWidget.setPreferredLocation(new Point(bindingWidth + engineWidth, 0));
        int externalWidth = externalRegion.getWidth();
        if (externalWidth <= 0) {
            externalWidth = 200;
        }
        externalRegionWidget.setPreferredBounds(new Rectangle(externalWidth, RegionUtilities.DEFAULT_HEIGHT));

        // Set up a new connection router to account for the widgets in our regions.
        // We, temporarily, do not use the standard OrthogonalSearchRouter. We used a slightly
        // modified version that can support passing in the context ConnectionWidget so that
        // connections that share endpoints do not collide with each other.
//        scene.setRouter(RouterFactory.createOrthogonalSearchRouter(new CasaCollisionCollector(
//        scene.setRouter(new CasaOrthogonalSearchRouter(new CasaCollisionCollector(
//                bindingRegionWidget,
//                engineRegionWidget,
//                externalRegionWidget,
//                scene.getConnectionLayer())));

        // Resizers
        scene.getLeftResizer().setPreferredLocation( new Point(
                engineRegionWidget.getPreferredLocation().x - RegionUtilities.RESIZER_HALF_WIDTH,
                0));
        scene.getMiddleResizer().setPreferredLocation(new Point(
                externalRegionWidget.getPreferredLocation().x - RegionUtilities.RESIZER_HALF_WIDTH,
                0));

        RegionUtilities.stretchScene(scene);
    }

    /**
     * Callers to this method must remember to call scene.validate().
     */
    public static Widget createNode(
            CasaPort casaPort,
            CasaWrapperModel model,
            CasaModelGraphScene scene,
            int x,
            int y)
    {
        CasaNodeWidget widget = (CasaNodeWidget) scene.addNode(casaPort);

        CasaConsumes consumes = casaPort.getConsumes();
        if (consumes != null) {
            createPin(casaPort, consumes, null, scene, false);
        }
        CasaProvides provides = casaPort.getProvides();
        if (provides != null) {
            createPin(casaPort, provides, null, scene, false);
        }
        scene.validate();

        // set the location
        if (x > 0 && y > 0) {
            widget.setPreferredLocation(new Point(x, y));
        }
        
        return widget;
    }

    public static Widget createNode(
            CasaServiceEngineServiceUnit su,
            CasaWrapperModel model,
            CasaModelGraphScene scene,
            int x,
            int y)
    {
        CasaNodeWidget widget = (CasaNodeWidget) scene.addNode(su);
        
        for (CasaProvides provides : su.getProvides()) {
            createPin(su, provides, provides.getEndpointName(), scene, false);
        }
        for (CasaConsumes consumes : su.getConsumes()) {
            createPin(su, consumes, consumes.getEndpointName(), scene, false);
        }
        scene.validate();

        // set the location
        if (x > 0 && y > 0) {
            widget.setPreferredLocation(new Point(x, y));
        }
        
        return widget;
    }

    public static boolean updateNodeProperties(
            CasaWrapperModel model,
            CasaPort casaPort,
            CasaNodeWidget widget)
    {
        String name = getShortNameInUpperCase(casaPort.getEndpointName());

        String bcCompName = model.getBindingComponentName(casaPort);
        if (bcCompName == null || bcCompName.length() == 0) {
            ErrorManager.getDefault().notify(new UnsupportedOperationException(
                     NbBundle.getMessage(CasaModelGraphUtilities.class, "Error_No_Binding_Component_name_for_endpoint") + name));   // NOI18N
            return false;
        }
        String bindingType = model.getBindingType(casaPort);
        if (bindingType == null) {
            ErrorManager.getDefault().notify(new UnsupportedOperationException(
                    NbBundle.getMessage(CasaModelGraphUtilities.class, "Error_Invalid_Binding_Component") + bcCompName));
            return false;
        }
        bindingType = bindingType.toUpperCase();

        widget.setNodeProperties(name, bindingType);

        return true;
    }

    public static void updateNodeProperties(
            CasaWrapperModel model,
            CasaServiceEngineServiceUnit su,
            CasaNodeWidget widget)
    {
        String name = su.getUnitName();
        String type = su.getComponentName();
        type = JbiDefaultComponentInfo.getDisplayName(type).toUpperCase();
        if (type.endsWith("SERVICEENGINE")) {  // NOI18N
            type = type.substring(0, type.length() - 13);
        }
        widget.setNodeProperties(name, type);
    }

    private static String getShortNameInUpperCase(String str) {
        int shortNameIndex = str.lastIndexOf('.') + 1;
        if (shortNameIndex > 0 && shortNameIndex < str.length()) {
            return str.substring(shortNameIndex).toUpperCase();
        }
        return str;
    }

    public static CasaPinWidget createPin (
            CasaComponent node,
            CasaComponent pin,
            String name,
            CasaModelGraphScene scene,
            boolean doUpdate)
    {
        CasaPinWidget pinWidget = (CasaPinWidget) scene.addPin(node, pin);
        pinWidget.setProperties(name);
        pinWidget.setToolTip(getToolTipName(node, pin, scene.getModel()));

        if (doUpdate) {
            scene.validate();
            CasaNodeWidget nodeWidget = (CasaNodeWidget) scene.findWidget(node);
        }
        return pinWidget;
    }

    /**
     * Callers to this method must remember to call scene.validate().
     */
    public static ConnectionWidget createEdge (
            CasaConnection connection,
            CasaConsumes source,
            CasaProvides target,
            CasaModelGraphScene scene,
            boolean doUpdate)
    {
        ConnectionWidget widget = (ConnectionWidget) scene.addEdge (connection);
        scene.setEdgeSource (connection, source);
        scene.setEdgeTarget (connection, target);
        if (doUpdate) {
            scene.updateEdgeRouting();
            scene.validate();
        }
        return widget;
    }

    public static CasaNodeWidget findNodeWidget(CasaPinWidget pinWidget) {
        Widget parentWidget = pinWidget.getParentWidget();
        CasaNodeWidget nodeWidget = null;
        while (parentWidget != null) {
            if (parentWidget instanceof CasaNodeWidget) {
                nodeWidget = (CasaNodeWidget) parentWidget;
                break;
            }
            parentWidget = parentWidget.getParentWidget();
        }
        return nodeWidget;
    }

    public static String getToolTipName(CasaComponent node, CasaComponent pin, CasaWrapperModel model) {
        String toolTip = "";
        if(pin instanceof CasaEndpointRef) {
            CasaEndpointRef endPointRef = (CasaEndpointRef) pin;
            toolTip = endPointRef.getServiceQName().toString();

            if(toolTip != null && toolTip.trim().length() > 0) {
                toolTip += Constants.PERIOD;
            }
            toolTip += endPointRef.getEndpointName();
        }
        return toolTip;
    }
    
    /*
     * Either a region or any widget could be made visible.
     */
    public static void ensureVisibity(Widget w) {
        w.getScene().getView().scrollRectToVisible(w.convertLocalToScene(w.getBounds()));
    }
}
