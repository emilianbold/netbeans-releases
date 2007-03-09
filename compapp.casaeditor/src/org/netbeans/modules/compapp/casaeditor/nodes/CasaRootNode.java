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
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.properties.LookAndFeelProperty;
import org.netbeans.modules.compapp.casaeditor.properties.PropertyUtils;
import org.netbeans.modules.compapp.projects.jbi.ui.actions.AddProjectAction;
import org.openide.ErrorManager;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

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
        return true;
    }
    
    public Action[] getActions(boolean context) {
        try {
            final Project jbiProject = getModel().getJBIProject();
            return new Action[] { 
                new AddJBIModuleAction(jbiProject)
            };
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
        return super.getActions(context);
    }
    
    
    
    private static class MyChildren extends CasaNodeChildren {
        private WeakReference mReference;
        public MyChildren(Object data, CasaNodeFactory factory) {
            super(data, factory);
            mReference = new WeakReference(data);
        }
        protected Node[] createNodes(Object key) {
            String keyName = (String) key;
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

    
    
    private static class AddJBIModuleAction extends AbstractAction {
        
        private WeakReference mProjectReference;
        
        public AddJBIModuleAction(Project jbiProject) {
            super(NbBundle.getMessage(CasaRootNode.class, "LBL_AddProjectAction_Name"), null);
            mProjectReference = new WeakReference(jbiProject);
        }
        
        public void actionPerformed(ActionEvent e) {
            Project jbiProject = (Project) mProjectReference.get();
            if (jbiProject != null) {
                new AddProjectAction().perform(jbiProject);
            }
        }
    }
}
