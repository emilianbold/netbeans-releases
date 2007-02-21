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

/*
 * ServiceUnitNode.java
 *
 * Created on November 2, 2006, 8:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.compapp.casaeditor.nodes;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServiceEngineServiceUnit;
import org.netbeans.modules.compapp.casaeditor.nodes.actions.ServiceUnitAction;
import org.netbeans.modules.compapp.casaeditor.properties.PropertyUtils;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

import javax.swing.*;
import org.netbeans.modules.compapp.casaeditor.model.casa.impl.CasaAttribute;
/**
 *
 * @author Josh Sandusky
 */
public class ServiceUnitNode extends CasaNode {
    
    private static final Image ICON = Utilities.loadImage(
            "org/netbeans/modules/compapp/casaeditor/nodes/resources/ServiceUnitNode.png");
    
    private static final String CHILD_ID_PROVIDES_LIST = "ProvidesList";
    private static final String CHILD_ID_CONSUMES_LIST = "ConsumesList";
    
    
    public ServiceUnitNode(CasaComponent component, Children children, Lookup lookup) {
        super(component, children, lookup);
    }
    
    public ServiceUnitNode(CasaComponent component, Lookup lookup) {
        super(component, new MyChildren(component, lookup), lookup);
    }
    
    public Action[] getActions(boolean context) {
        List actions = new ArrayList();
        Action[] parentActions = super.getActions(context);
        for (Action parentAction : parentActions) {
            actions.add(parentAction);
        }
        actions.add(null);
        actions.add(SystemAction.get(ServiceUnitAction.class));
        return (Action[]) actions.toArray(new Action[actions.size()]);
    }

    @Override
    public String getName() {
        CasaServiceEngineServiceUnit su = (CasaServiceEngineServiceUnit) getData();
        if (su != null) {
            return NbBundle.getMessage(getClass(), "LBL_ServiceUnit");
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
                CasaWrapperModel model = (CasaWrapperModel) casaSU.getModel(); // TMP
                decoration = NbBundle.getMessage(WSDLEndpointNode.class, "LBL_NameAttr",
                        casaSU.getUnitName());
            }
            if (decoration == null) {
                return htmlDisplayName;
            }
            return htmlDisplayName + " <font color='#999999'>"+decoration+"</font>";
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
        CasaWrapperModel model = (CasaWrapperModel) casaSU.getModel();
        
        Sheet.Set identificationProperties =
                getPropertySet(sheet, PropertyUtils.PropertiesGroups.IDENTIFICATION_SET);
        
        PropertyUtils.installServiceUnitNameProperty(
                identificationProperties, this, casaSU,
                CasaAttribute.UNIT_NAME.getName(),
                "serviceUnitName",
                NbBundle.getMessage(getClass(), "PROP_Name"),
                NbBundle.getMessage(getClass(), "PROP_Name"));
        
        Node.Property descriptionSupport = new PropertySupport.ReadOnly(
                "description", // NO18N
                String.class, 
                NbBundle.getMessage(getClass(), "PROP_Description"), 
                "") {
            public String getValue() {
                return casaSU.getDescription();
            }
        };
        identificationProperties.put(descriptionSupport);
        
        Sheet.Set targetProperties =
                getPropertySet(sheet, PropertyUtils.PropertiesGroups.TARGET_SET);
        Node.Property artifactsZipSupport = new PropertySupport.ReadOnly(
                "artifactsZip", // NO18N
                String.class, 
                NbBundle.getMessage(getClass(), "PROP_ArtifactsZip"), 
                "") {
            public String getValue() {
                return casaSU.getArtifactsZip();
            }
        };
        targetProperties.put(artifactsZipSupport);
        
        Node.Property componentNameSupport = new PropertySupport.ReadOnly(
                "componentName", // NO18N
                String.class, 
                NbBundle.getMessage(getClass(), "PROP_ComponentName"), 
                "") {
            public String getValue() {
                return casaSU.getComponentName();
            }
        };
        targetProperties.put(componentNameSupport);
    }

    
    private static class MyChildren extends CasaNodeChildren {
        public MyChildren(CasaComponent component, Lookup lookup) {
            super(component, lookup);
        }
        protected Node[] createNodes(Object key) {
            assert key instanceof String;
            CasaServiceEngineServiceUnit serviceUnit = (CasaServiceEngineServiceUnit) getData();
            if (serviceUnit != null) {
                CasaWrapperModel model = getCasaModel(serviceUnit);
                if (model != null) {
                    String keyName = (String) key;
                    if (keyName.equals(CHILD_ID_CONSUMES_LIST)) {
                        return new Node[] { new ConsumesListNode(serviceUnit.getConsumes(), mLookup) };
                    } else if (keyName.equals(CHILD_ID_PROVIDES_LIST)) {
                        return new Node[] { new ProvidesListNode(serviceUnit.getProvides(), mLookup) };
                    }
                }
            }
            return null;
        }
        public Object getChildKeys(Object data)  {
            List children = new ArrayList();
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
