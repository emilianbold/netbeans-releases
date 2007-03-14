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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.SwingUtilities;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.Widget.Dependency;
import org.netbeans.modules.compapp.casaeditor.Constants;
import org.netbeans.modules.compapp.casaeditor.graph.CasaNodeWidget;
import org.netbeans.modules.compapp.casaeditor.graph.CasaPinWidget;
import org.netbeans.modules.compapp.casaeditor.graph.CasaRegionWidget;
import org.netbeans.modules.compapp.casaeditor.graph.RegionUtilities;
import org.netbeans.modules.compapp.casaeditor.graph.layout.CasaCollisionCollector;
import org.netbeans.modules.compapp.casaeditor.graph.layout.ModelLoadLayoutInfo;
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
    
    
    public static void renderModel(CasaWrapperModel model, CasaModelGraphScene scene)
    {
        try {
            safeRenderModel(model, scene);
        } catch (Throwable t) {
            scene.autoLayout(false, false);
            ErrorManager.getDefault().notify(t);
        }
    }
    
    private static void safeRenderModel(CasaWrapperModel model, CasaModelGraphScene scene)
    {
        if (model == null || scene == null) {
            return;
        }
        
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
        for (CasaPort endpoint : model.getCasaPorts()) {
            createNode(endpoint, model, scene, -2, -2); //null); // FIXME
        }
        
        for (CasaServiceEngineServiceUnit su : model.getServiceEngineServiceUnits()) {
            createNode(su, model, scene, -2, -2); //null); // FIXME
        }
        
        // add connections
        for (CasaConnection connection : model.getCasaConnectionList(false)) {
            CasaConsumes consumes = (CasaConsumes) model.getCasaEndpoint(connection, true);
            CasaProvides provides = (CasaProvides) model.getCasaEndpoint(connection, false);
            if (consumes != null && provides != null) {
                createEdge(connection, consumes, provides, scene, false);
            }
        }
        
        // layout
        Map<Widget, ModelLoadLayoutInfo> modelRestoreInfoMap = 
                CasaModelGraphUtilities.restoreViewPositions(model, scene);
        if (modelRestoreInfoMap == null) {
            // We have no previously set view positions, so persist the
            // new locations once the auto-layout determines them.
            scene.autoLayout(true, false);
        } else {
            scene.modelLoadLayout(modelRestoreInfoMap);
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
        scene.setRouter(RouterFactory.createOrthogonalSearchRouter(new CasaCollisionCollector(
                bindingRegionWidget,
                engineRegionWidget,
                externalRegionWidget,
                scene.getConnectionLayer())));
        
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
        if (!updateNodeProperties(model, casaPort, widget)) {
            return null;
        }
        
        CasaConsumes consumes = casaPort.getConsumes(); 
        if (consumes != null) {
            createPin(casaPort, consumes, null, scene, false);
        }
        CasaProvides provides = casaPort.getProvides();
        if (provides != null) {
            createPin(casaPort, provides, null, scene, false);
        }

        // set the location
        if (x > 0 && y > 0) {
            scene.validate();
            Point pos = adjustLocation(widget, x, y, true);
            widget.setPreferredLocation(pos);
        } else {
            scene.invokeRegionLayout(scene.getBindingRegion(), false);
        }
        
        widget.invokeDependencies();
        
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
        updateNodeProperties(model, su, widget);
        for (CasaProvides provides : su.getProvides()) {
            createPin(su, provides, model.getEndpointName(provides), scene, false);
        }
        for (CasaConsumes consumes : su.getConsumes()) {
            createPin(su, consumes, model.getEndpointName(consumes), scene, false);
        }

        // set the location
        if (x > 0 && y > 0) {
            scene.validate();
            Point pos = adjustLocation(widget, x, y, false);
            widget.setPreferredLocation(pos);
        } else {
            scene.invokeRegionLayout(scene.getEngineRegion(), false);
        }
        
        RegionUtilities.stretchSceneWidthOnly(scene);
        widget.invokeDependencies();

        return widget;
    }

    public static boolean updateNodeProperties(
            CasaWrapperModel model, 
            CasaPort casaPort, 
            CasaNodeWidget widget)
    {
        String name = getShortNameInUpperCase(model.getEndpointName(casaPort));

        String bcCompName = model.getBindingComponentName(casaPort);
        if (bcCompName == null || bcCompName.length() == 0) {
            ErrorManager.getDefault().notify(new UnsupportedOperationException(
                     NbBundle.getMessage(CasaModelGraphUtilities.class, "Error_No_Binding_Component_name_for_endpoint") + name));   // NOI18N
            return false;
        }
        // String bindingType = model.getDefaultBindingComponents().get(bcCompName);
        String bindingType = casaPort.getBindingType();
        if (bindingType == null) {
            bindingType = model.getDefaultBindingComponents().get(bcCompName);
        }
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
        String type = model.getServiceUnitComponentName(su);
        type = JbiDefaultComponentInfo.getDisplayName(type).toUpperCase();
        widget.setNodeProperties(name, type);
    }

    private static String getShortNameInUpperCase(String str) {
        int shortNameIndex = str.lastIndexOf('.') + 1;
        if (shortNameIndex > 0 && shortNameIndex < str.length()) {
            return str.substring(shortNameIndex).toUpperCase();
        }
        return str;
    }

    // Ensure the suggestedLocation will properly fit in the widget's region.
    // Adjust the location if necessary.
    private static Point adjustLocation(
            Widget widget, 
            int suggestedX, 
            int suggestedY,
            boolean isRightAligned)
    {
        CasaRegionWidget region = (CasaRegionWidget) widget.getParentWidget();
        // Ensure widget location is not on top of the region label.
        if (suggestedY < region.getLabelYOffset()) {
            suggestedY = region.getLabelYOffset();
        }
        Dimension widgetSize = widget.getBounds().getSize();
        if (isRightAligned) {
            suggestedX = region.getBounds().width - widgetSize.width;
        } else if (suggestedX + widgetSize.width > region.getBounds().width) {
            suggestedX = 
                    region.getBounds().width - 
                    widgetSize.width - 
                    30; // Position the widget a short gap from the right edge.
            if (suggestedX  < 0) {
                suggestedX = 0;
            }
        }

        return new Point(suggestedX, suggestedY);
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
        

        CasaWrapperModel model = scene.getModel();
        CasaEndpointRef endPointRef = (CasaEndpointRef) pin;
        String toolTip =model.getServiceQName(endPointRef).toString() + Constants.PERIOD;
        if(node instanceof CasaServiceEngineServiceUnit) {
            toolTip += model.getEndpointName(endPointRef);
        } else {
            toolTip += endPointRef.getEndpoint().getQName().getLocalPart().toString();
        }
        pinWidget.setToolTip(toolTip);
        
        if (doUpdate) {
            scene.validate();
            CasaNodeWidget nodeWidget = (CasaNodeWidget) scene.findWidget(node);
            nodeWidget.invokeDependencies();
        }
        return pinWidget;
    }
    
    /**
     * Callers to this method must remember to call scene.validate().
     */
    public static void createEdge (
            CasaConnection connection, 
            CasaConsumes source, 
            CasaProvides target, 
            CasaModelGraphScene scene,
            boolean doUpdate)
    {
        scene.addEdge (connection);
        scene.setEdgeSource (connection, source);
        scene.setEdgeTarget (connection, target);
        if (doUpdate) {
            scene.validate();
        }
    }
    
    private static Map restoreViewPositions(CasaWrapperModel model, CasaModelGraphScene scene) {
        Map<Widget, ModelLoadLayoutInfo> modelRestoreInfoMap = new HashMap<Widget, ModelLoadLayoutInfo>();
        
        Rectangle bindingRegionBounds = scene.getBindingRegion().getBounds();
        for (CasaPort port : model.getCasaPorts()) {
            Point portPoint = checkServiceUnitBounds(
                    port, 
                    port.getX(),
                    port.getY(),
                    bindingRegionBounds, 
                    model, 
                    scene);
            if (portPoint != null) {
                ModelLoadLayoutInfo info = new ModelLoadLayoutInfo(portPoint);
                modelRestoreInfoMap.put(scene.findWidget(port), info);
            } else {
                return null;
            }
        }
        
        Rectangle engineRegionBounds = scene.getEngineRegion().getBounds();
        Rectangle externalRegionBounds = scene.getExternalRegion().getBounds();
        for (CasaServiceEngineServiceUnit su : model.getServiceEngineServiceUnits()) {
            Point suPoint = null;
            if (!su.isInternal()) {
                suPoint = checkServiceUnitBounds(
                        su,
                        su.getX(),
                        su.getY(),
                        externalRegionBounds,
                        model,
                        scene);
            } else {
                suPoint = checkServiceUnitBounds(
                        su,
                        su.getX(),
                        su.getY(),
                        engineRegionBounds,
                        model,
                        scene);
            }
            if (suPoint != null) {
                ModelLoadLayoutInfo info = new ModelLoadLayoutInfo(suPoint);
                modelRestoreInfoMap.put(scene.findWidget(su), info);
            } else {
                return null;
            }
        }
        
        return modelRestoreInfoMap;
    }
    
    private static Point checkServiceUnitBounds(
            Object modelObject, 
            int x, 
            int y,
            Rectangle regionBounds, 
            CasaWrapperModel model, 
            CasaModelGraphScene scene)
    {
        if (y <= 0) {
            return null;
        }
        Widget widget = scene.findWidget(modelObject);
        if (widget == null) {
            return null;
        }
        // TODO check whether widget can fit within the region boundaries?
        return new Point(x, y);
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
}
