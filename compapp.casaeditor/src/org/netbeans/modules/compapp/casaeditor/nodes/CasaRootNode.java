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

package org.netbeans.modules.compapp.casaeditor.nodes;

import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.AddWSDLPortsAction;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.AutoLayoutAction;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.AddExternalServiceUnitAction;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.AddJBIModuleAction;
import org.netbeans.modules.compapp.casaeditor.properties.LookAndFeelProperty;
import org.netbeans.modules.compapp.casaeditor.properties.PropertyUtils;
import org.netbeans.modules.compapp.projects.jbi.ui.actions.AddProjectAction;
import org.openide.ErrorManager;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Josh Sandusky
 */
public class CasaRootNode extends CasaNode {

    private static final Image ICON = Utilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/nodes/resources/CasaRootNode.png"); // NOI18N

    private static final String CHILD_ID_WSDL_ENDPOINTS  = "WSDLEndpoints";     // NOI18N
    private static final String CHILD_ID_SERVICE_ENGINES = "ServiceEngines";    // NOI18N
    private static final String CHILD_ID_CONNECTIONS     = "Connections";       // NOI18N

    private static final String[] CHILD_TYPES = {
        CHILD_ID_WSDL_ENDPOINTS,
        CHILD_ID_SERVICE_ENGINES,
        CHILD_ID_CONNECTIONS
    };


    public CasaRootNode(Object data, CasaNodeFactory factory) {
        super(data, new MyChildren(data, factory), factory);
    }


    public String getName() {
        DataObject dataObject = getDataObject();
        if (dataObject != null) {
            return dataObject.getName();
        }
        return NbBundle.getMessage(getClass(), "LBL_CasaModel");        // NOI18N
    }

    protected void setupPropertySheet(Sheet sheet) {
        final CasaWrapperModel model = (CasaWrapperModel) getData();
        if (model == null) {
            return;
        }

        Sheet.Set mainPropertySet =
                getPropertySet(sheet, PropertyUtils.PropertiesGroups.MAIN_SET);

        String propertyName = NbBundle.getMessage(LookAndFeelProperty.class, "LBL_LookAndFeel"); // NOI18N
        try {
            Node.Property property = new LookAndFeelProperty(this);
            mainPropertySet.put(property);
        } catch (Exception e) {
            mainPropertySet.put(PropertyUtils.createErrorProperty(propertyName));
            ErrorManager.getDefault().notify(e);
        }
    }

    public Image getIcon(int type) {
        return ICON;
    }

    public Image getOpenedIcon(int type) {
        return ICON;
    }

    public boolean isValidSceneActionForLocation(Action action, Widget widget, Point sceneLocation) {
        if (action instanceof AddJBIModuleAction) {
            CasaModelGraphScene scene = (CasaModelGraphScene) widget.getScene();
            Widget engineRegion = scene.getEngineRegion();
            Rectangle engineRegionRect =
                    engineRegion.convertLocalToScene(new Rectangle(engineRegion.getBounds().width, engineRegion.getBounds().height));
            if (engineRegionRect.contains(sceneLocation)) {
                return true;
            }
            return false;
        }

        else if (action instanceof AddWSDLPortsAction) {
            CasaModelGraphScene scene = (CasaModelGraphScene) widget.getScene();
            Widget bindingRegion = scene.getBindingRegion();
            Rectangle bindingRegionRect =
                    bindingRegion.convertLocalToScene(new Rectangle(bindingRegion.getBounds().width, bindingRegion.getBounds().height));
            if (bindingRegionRect.contains(sceneLocation)) {
                return true;
            }
            return false;
        }

        else if (action instanceof AddExternalServiceUnitAction) {
            CasaModelGraphScene scene = (CasaModelGraphScene) widget.getScene();
            Widget externalRegion = scene.getExternalRegion();
            Rectangle externalRegionRect =
                    externalRegion.convertLocalToScene(new Rectangle(externalRegion.getBounds().width, externalRegion.getBounds().height));
            if (externalRegionRect.contains(sceneLocation)) {
                return true;
            }
            return false;
        }

        return true;
    }

    protected void addCustomActions(List<Action> actions) {
        final Project jbiProject = getModel().getJBIProject();
        actions.add(new AddJBIModuleAction(jbiProject));
        actions.add(SystemAction.get(AddWSDLPortsAction.class));
        actions.add(SystemAction.get(AddExternalServiceUnitAction.class));
        actions.add(null);
        actions.add(new AutoLayoutAction(getDataObject()));
    }



    private static class MyChildren extends CasaNodeChildren<String> {
        private WeakReference mReference;
        public MyChildren(Object data, CasaNodeFactory factory) {
            super(data, factory);
            mReference = new WeakReference<Object>(data);
        }
        protected Node[] createNodes(String keyName) {
            if (mReference.get() != null) {
                try {
                    CasaWrapperModel model = (CasaWrapperModel) mReference.get();
                    if (keyName.equals(CHILD_ID_WSDL_ENDPOINTS)) {
                        return new Node[] {
                            mNodeFactory.createNode_portList(model.getCasaPorts()) };
                    } else if (keyName.equals(CHILD_ID_SERVICE_ENGINES)) {
                        return new Node[] {
                            mNodeFactory.createNode_suList(model.getServiceEngineServiceUnits()) };
                    } else if (keyName.equals(CHILD_ID_CONNECTIONS)) {
                        return new Node[] {
                            mNodeFactory.createNode_connectionList(model.getCasaConnectionList(false)) };
                    }
                } catch (Exception e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
            return null;
        }
        public Object getChildKeys(Object data)  {
            return CHILD_TYPES;
        }
    }
}
