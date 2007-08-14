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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.CasaDataObject;
import org.netbeans.modules.compapp.casaeditor.graph.CasaNodeWidget;
import org.netbeans.modules.compapp.casaeditor.graph.CasaNodeWidgetBinding;
import org.netbeans.modules.compapp.casaeditor.graph.CasaNodeWidgetEngine;
import org.netbeans.modules.compapp.casaeditor.graph.CasaPinWidget;
import org.netbeans.modules.compapp.casaeditor.graph.CasaRegionWidget;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConnection;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpointRef;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConsumes;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServiceEngineServiceUnit;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPort;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaProvides;

/**
 *
 * @author Josh Sandusky
 */
public class CasaDesignModelListener implements PropertyChangeListener {
    
    private CasaDataObject mDataObject;
    private CasaModelGraphScene mScene;
    
    
    public CasaDesignModelListener(CasaDataObject dataObject, CasaModelGraphScene scene) {
        mDataObject = dataObject;
        mScene = scene;
        mScene.getModel().addPropertyChangeListener(this);
    }
    
    public void cleanup() {
        mScene.getModel().removePropertyChangeListener(this);
    }
    
    public void propertyChange(final PropertyChangeEvent evt) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                propertyUpdate(evt);
            }
        });
    }
    
    private void propertyUpdate(PropertyChangeEvent evt) {
        String name   = evt.getPropertyName();
        Object source = evt.getSource();
        
        if        (name.equals(CasaWrapperModel.PROPERTY_MODEL_RELOAD)) {
            reloadGraph();
            
        } else if (name.equals(CasaWrapperModel.PROPERTY_CONNECTION_REMOVED)) {
            removeConnection((CasaConnection) source);
            
        } else if (name.equals(CasaWrapperModel.PROPERTY_CONNECTION_ADDED)) {
            addConnection((CasaConnection) source);
            
        } else if (name.equals(CasaWrapperModel.PROPERTY_CASA_PORT_ADDED)) {
            addCasaPort((CasaPort) source);
            
        } else if (name.equals(CasaWrapperModel.PROPERTY_CASA_PORT_REMOVED)) {
            removeCasaPort((CasaPort) source);
            
        } else if (name.equals(CasaWrapperModel.PROPERTY_ENDPOINT_REMOVED)) {
            removeEndpoint((CasaEndpointRef) source);
            
        } else if (name.equals(CasaWrapperModel.PROPERTY_ENDPOINT_ADDED)) {
            addEndpoint((CasaEndpointRef) source);
            
        } else if (name.equals(CasaWrapperModel.PROPERTY_SERVICE_ENGINE_SERVICE_UNIT_ADDED)) {
            addServiceUnit((CasaServiceEngineServiceUnit) source);
            
        } else if (name.equals(CasaWrapperModel.PROPERTY_SERVICE_ENGINE_SERVICE_UNIT_REMOVED)) {
            removeServiceUnit((CasaServiceEngineServiceUnit) source);
            
        } else if (name.equals(CasaWrapperModel.PROPERTY_ENDPOINT_NAME_CHANGED)) {
            renameEndpoint((CasaComponent) source);
            
        } else if (name.equals(CasaWrapperModel.PROPERTY_ENDPOINT_SERVICE_QNAME_CHANGED)) {
            renameEndpointTooltip((CasaComponent) source);
            
        } else if (name.equals(CasaWrapperModel.PROPERTY_ENDPOINT_INTERFACE_QNAME_CHANGED)) {
            // FIXME
            
        } else if (name.equals(CasaWrapperModel.PROPERTY_SERVICE_UNIT_RENAMED)) {
            renameServiceUnit((CasaServiceEngineServiceUnit) source);
        }
    }
    
    private void reloadGraph() {
        mScene.getModel().removePropertyChangeListener(this);
        CasaModelGraphUtilities.renderModel(mScene.getModel(), mScene);
        mScene.getModel().addPropertyChangeListener(this);
    }
    
    private void removeConnection(CasaConnection connection) {
        if (mScene.findWidget(connection) != null) {
            mScene.removeEdge(connection);
            mScene.updateEdgeRouting(null);
            // validate
            mScene.validate();
        }
    }
    
    private void addConnection(CasaConnection connection) {
        CasaWrapperModel model = mScene.getModel();
        CasaConsumes consumes = (CasaConsumes) model.getCasaEndpointRef(connection, true);
        CasaProvides provides = (CasaProvides) model.getCasaEndpointRef(connection, false);
        CasaModelGraphUtilities.createEdge(connection, consumes, provides, mScene, true);
        // update selection
        mScene.updateSelectionAndRequestFocus(connection);
    }
    
    private void addCasaPort(CasaPort casaPort) {
        Widget w = CasaModelGraphUtilities.createNode(
                casaPort,
                mScene.getModel(),
                mScene,
                casaPort.getX(),
                casaPort.getY());
        // update selection before forcing the layout - 
        // layout scrolls to selected object if necessary
        mScene.updateSelectionAndRequestFocus(casaPort);
        // Force a layout to ensure the widget fits at the suggested location.
        mScene.progressiveRegionLayout(mScene.getBindingRegion(), true);
        
        CasaModelGraphUtilities.ensureVisibity(w);
    }
    
    private void removeCasaPort(CasaPort casaPort) {
        mScene.removeNode(casaPort);
        // validate
        mScene.validate();
    }
    
    private void addServiceUnit(CasaServiceEngineServiceUnit serviceUnit) {
        Widget w = CasaModelGraphUtilities.createNode(
                serviceUnit,
                mScene.getModel(),
                mScene,
                serviceUnit.getX(),
                serviceUnit.getY());
        // update selection before forcing the layout - 
        // layout scrolls to selected object if necessary
        mScene.updateSelectionAndRequestFocus(serviceUnit);
        // Force a layout to ensure the widget fits at the suggested location.
        mScene.progressiveRegionLayout(
                serviceUnit.isInternal() ? mScene.getEngineRegion() : mScene.getExternalRegion(),
                true);
        
        CasaModelGraphUtilities.ensureVisibity(w);
    }
    
    private void removeServiceUnit(CasaServiceEngineServiceUnit serviceUnit) {
        mScene.removeNode(serviceUnit);
        // validate
        mScene.validate();
    }
    
    private void removeEndpoint(CasaEndpointRef endpoint) {
        CasaPinWidget pinWidget = (CasaPinWidget) mScene.findWidget(endpoint);
        if (pinWidget != null) {
            CasaNodeWidget nodeWidget = CasaModelGraphUtilities.findNodeWidget(pinWidget);
            mScene.removePin(endpoint);
            if (nodeWidget instanceof CasaNodeWidgetEngine) {
                // The service unit widget bounds can change if a pin is deleted.
                // Ensure the bounds updates properly.
                ((CasaNodeWidgetEngine) nodeWidget).readjustBounds();
            } else {
                // validate
                mScene.validate();
            }
        }
    }
    
    private void addEndpoint(CasaEndpointRef endpoint) {
        CasaWrapperModel model = mScene.getModel();
        CasaPort casaPort = model.getCasaPort(endpoint);
        CasaRegionWidget region = null;
        if (casaPort != null) {
            CasaModelGraphUtilities.createPin(
                    casaPort,
                    endpoint,
                    endpoint.getEndpointName(),
                    mScene,
                    true);
            region = mScene.getBindingRegion();
        } else {
            CasaServiceEngineServiceUnit serviceUnit = model.getCasaEngineServiceUnit(endpoint);
            CasaModelGraphUtilities.createPin(
                    serviceUnit,
                    endpoint,
                    endpoint.getEndpointName(),
                    mScene,
                    true);
            CasaModelGraphUtilities.ensureVisibity(mScene.findWidget(serviceUnit));
            region = serviceUnit.isInternal() ? 
                mScene.getEngineRegion() : mScene.getExternalRegion();
        }
        // update selection
        mScene.updateSelectionAndRequestFocus(endpoint);
        // Force a layout to ensure no overlap occurs after a pin is added.
        mScene.progressiveRegionLayout(region, true);
    }
    
    private void renameEndpoint(CasaComponent component) {
        Widget widget = mScene.findWidget(component);
        if (widget instanceof CasaPinWidget) {
            CasaPinWidget pinWidget = (CasaPinWidget) widget;
            CasaEndpointRef endpointRef = (CasaEndpointRef) component;
            pinWidget.setProperties(endpointRef.getEndpointName());
            pinWidget.setToolTipText(CasaModelGraphUtilities.getToolTipName(endpointRef.getParent(), endpointRef, mScene.getModel()));
            
        } else if (widget instanceof CasaNodeWidgetBinding) {
            CasaNodeWidgetBinding portWidget = (CasaNodeWidgetBinding) widget;
            CasaPort casaPort = (CasaPort) component;
            CasaWrapperModel model = mScene.getModel();
            portWidget.setEndpointLabel(casaPort.getEndpointName());
        }
    }
    
    private void renameEndpointTooltip(CasaComponent component) {
        Widget widget = mScene.findWidget(component);
        if (widget instanceof CasaPinWidget) {
            CasaPinWidget pinWidget = (CasaPinWidget) widget;
            CasaEndpointRef endpointRef = (CasaEndpointRef) component;
            pinWidget.setToolTipText(CasaModelGraphUtilities.getToolTipName(endpointRef.getParent(), endpointRef, mScene.getModel()));
        }
    }
    
    private void renameServiceUnit(CasaServiceEngineServiceUnit su) {
        Widget widget = mScene.findWidget(su);
        if (widget != null) {
            CasaNodeWidget nodeWidget = (CasaNodeWidget) widget;
            CasaModelGraphUtilities.updateNodeProperties(
                    mScene.getModel(),
                    su,
                    nodeWidget);
        }
    }
}
