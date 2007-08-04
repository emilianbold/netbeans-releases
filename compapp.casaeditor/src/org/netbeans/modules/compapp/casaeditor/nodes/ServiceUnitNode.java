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
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServiceEngineServiceUnit;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.LoadWSDLPortsAction;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.AddConsumesPinAction;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.AddProvidesPinAction;
import org.netbeans.modules.compapp.casaeditor.properties.PropertyUtils;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

import javax.swing.*;
import org.netbeans.modules.compapp.casaeditor.Constants;
import org.netbeans.modules.compapp.casaeditor.model.casa.impl.CasaAttribute;

/**
 *
 * @author Josh Sandusky
 */
public class ServiceUnitNode extends CasaNode {
    
    private static final Image ICON = Utilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/nodes/resources/ServiceUnitNode.png");     // NOI18N
    
    private static final String CHILD_ID_PROVIDES_LIST = "ProvidesList";        // NOI18N
    private static final String CHILD_ID_CONSUMES_LIST = "ConsumesList";        // NOI18N
    
    
    public ServiceUnitNode(CasaServiceEngineServiceUnit component, CasaNodeFactory factory) {
        super(component, new MyChildren(component, factory), factory);
    }
    
    
    protected void addCustomActions(List<Action> actions) {
        CasaServiceEngineServiceUnit su = (CasaServiceEngineServiceUnit) getData();
        if (su == null) {
            return;
        }
        
        if (su.isInternal()) {
            actions.add(SystemAction.get(LoadWSDLPortsAction.class));
        } else {
            actions.add(SystemAction.get(AddConsumesPinAction.class));
            actions.add(SystemAction.get(AddProvidesPinAction.class));
        }
    }

    @Override
    public String getName() {
        CasaServiceEngineServiceUnit su = (CasaServiceEngineServiceUnit) getData();
        if (su != null) {
            return NbBundle.getMessage(getClass(), "LBL_ServiceUnit");      // NOI18N
        }
        return super.getName();
    }


    @Override
    public String getHtmlDisplayName() {
        try {
            String htmlDisplayName = getName();
            CasaServiceEngineServiceUnit casaSU = (CasaServiceEngineServiceUnit) getData();
            String decoration = null;
            if (casaSU != null) {
                decoration = NbBundle.getMessage(WSDLEndpointNode.class, "LBL_NameAttr",        // NOI18N
                        casaSU.getUnitName());
            }
            if (decoration == null) {
                return htmlDisplayName;
            }
            return htmlDisplayName + " <font color='#999999'>"+decoration+"</font>";        // NOI18N
        } catch (Throwable t) {
            // getHtmlDisplayName MUST recover gracefully.
            return getBadName();
        }
    }

    protected void setupPropertySheet(Sheet sheet) {
        final CasaServiceEngineServiceUnit casaSU = (CasaServiceEngineServiceUnit) getData();
        if (casaSU == null) {
            return;
        }        
        Sheet.Set identificationProperties =
                getPropertySet(sheet, PropertyUtils.PropertiesGroups.IDENTIFICATION_SET);
        
        PropertyUtils.installServiceUnitNameProperty(
                identificationProperties, this, casaSU,
                CasaAttribute.UNIT_NAME.getName(),
                "serviceUnitName",                                      // NOI18N
                NbBundle.getMessage(getClass(), "PROP_Name"),           // NOI18N
                NbBundle.getMessage(getClass(), "PROP_Name"));          // NOI18N
        
        Node.Property<String> descriptionSupport = new PropertySupport.ReadOnly<String>(
                "description", // NOI18N
                String.class, 
                NbBundle.getMessage(getClass(), "PROP_Description"),    // NOI18N
                Constants.EMPTY_STRING) {
            public String getValue() {
                return casaSU.getDescription();
            }
        };
        identificationProperties.put(descriptionSupport);
        
        Sheet.Set targetProperties =
                getPropertySet(sheet, PropertyUtils.PropertiesGroups.TARGET_SET);
        Node.Property<String> artifactsZipSupport = new PropertySupport.ReadOnly<String>(
                "artifactsZip", // NOI18N
                String.class, 
                NbBundle.getMessage(getClass(), "PROP_ArtifactsZip"),   // NOI18N
                Constants.EMPTY_STRING) {
            public String getValue() {
                return casaSU.getArtifactsZip();
            }
        };
        targetProperties.put(artifactsZipSupport);
        
        Node.Property<String> componentNameSupport = new PropertySupport.ReadOnly<String>(
                "componentName", // NOI18N
                String.class, 
                NbBundle.getMessage(getClass(), "PROP_ComponentName"),  // NOI18N
                Constants.EMPTY_STRING) {
            public String getValue() {
                return casaSU.getComponentName();
            }
        };
        targetProperties.put(componentNameSupport);
    }

    
    private static class MyChildren extends CasaNodeChildren {
        public MyChildren(CasaComponent component, CasaNodeFactory factory) {
            super(component, factory);
        }
        protected Node[] createNodes(Object key) {
            assert key instanceof String;
            CasaServiceEngineServiceUnit serviceUnit = (CasaServiceEngineServiceUnit) getData();
            if (serviceUnit != null) {
                CasaWrapperModel model = mNodeFactory.getCasaModel();
                if (model != null) {
                    String keyName = (String) key;
                    if (keyName.equals(CHILD_ID_CONSUMES_LIST)) {
                        return new Node[] { mNodeFactory.createNode_consumesList(serviceUnit.getConsumes()) };
                    } else if (keyName.equals(CHILD_ID_PROVIDES_LIST)) {
                        return new Node[] { mNodeFactory.createNode_providesList(serviceUnit.getProvides()) };
                    }
                }
            }
            return null;
        }
        public Object getChildKeys(Object data)  {
            List<String> children = new ArrayList<String>();
            children.add(CHILD_ID_CONSUMES_LIST);
            children.add(CHILD_ID_PROVIDES_LIST);
            return children;
        }
    }
    
    public Image getIcon(int type) {
        return ICON;
    }
    
    public Image getOpenedIcon(int type) {
        return ICON;
    }
    
    public boolean isEditable(String propertyType) {
        CasaServiceEngineServiceUnit su = (CasaServiceEngineServiceUnit) getData();
        if (su != null) {
            return getModel().isEditable(su, propertyType);
        }
        return false;
    }
    
    public boolean isDeletable() {
        CasaServiceEngineServiceUnit su = (CasaServiceEngineServiceUnit) getData();
        if (su != null) {
            return getModel().isDeletable(su);
        }
        return false;
    }
}
