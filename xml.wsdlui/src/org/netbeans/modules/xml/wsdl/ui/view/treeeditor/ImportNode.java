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


package org.netbeans.modules.xml.wsdl.ui.view.treeeditor;

import java.awt.Image;
import java.beans.PropertyEditor;
import java.util.logging.Level;

import javax.swing.Action;

import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.ui.actions.CommonAddExtensibilityAttributeAction;
import org.netbeans.modules.xml.wsdl.ui.actions.RemoveAttributesAction;
import org.netbeans.modules.xml.wsdl.ui.api.property.PropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.view.property.BaseAttributeProperty;
import org.netbeans.modules.xml.wsdl.ui.view.property.ImportLocationPropertyEditor;
import org.netbeans.modules.xml.xam.ui.actions.GoToAction;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.NewAction;
import org.openide.actions.PasteAction;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;


/**
 *
 * @author Ritesh Adval
 *
 * @version $Revision$
 */
public class ImportNode extends WSDLElementContainerNode {
    
    private static final String NAMESPACE_PROP = "namespace";//NOI18N
    private Import mWSDLConstruct;
            
    Image ICON  = Utilities.loadImage
     ("org/netbeans/modules/xml/wsdl/ui/view/resources/import-include-redefine.png");
    
    private ImportPropertyAdapter mPropertyAdapter;

    private static final SystemAction[] ACTIONS = new SystemAction[]{
        SystemAction.get(CutAction.class),
        SystemAction.get(CopyAction.class),
        SystemAction.get(PasteAction.class),
        null,
        SystemAction.get(NewAction.class),
        SystemAction.get(DeleteAction.class),
        null,
        SystemAction.get(CommonAddExtensibilityAttributeAction.class),
        SystemAction.get(RemoveAttributesAction.class),
        null,
        SystemAction.get(GoToAction.class),
        null,
        SystemAction.get(PropertiesAction.class)
    };

    public ImportNode(Children children,
                      Import wsdlConstruct) {
        super(children, wsdlConstruct);
        mWSDLConstruct = wsdlConstruct;
        this.mPropertyAdapter = new ImportPropertyAdapter();
    }
    
    
    @Override
    public Image getIcon(int type) {
        return ICON;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return ICON;
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return ACTIONS;
    }

    public Object getWSDLConstruct() {
        return mWSDLConstruct;
    }
    
    @Override
    protected void refreshAttributesSheetSet()  {
        Sheet.Set ss = createPropertiesSheetSet();
        
        try {
            //namespace
            Node.Property namespaceProperty = new BaseAttributeProperty(mPropertyAdapter, 
                                                                        String.class, 
                                                                        NAMESPACE_PROP);
            namespaceProperty.setName(NbBundle.getMessage(ImportNode.class, "PROP_NAMESPACE_DISPLAYNAME"));
            namespaceProperty.setShortDescription(NbBundle.getMessage(ImportNode.class, "NAMESPACE_DESC"));
            ss.put(namespaceProperty);
            
            //location
            Node.Property locationProperty = new ImportLocationProperty(mPropertyAdapter, 
                    String.class, 
                    "getLocation", 
                    "setLocation");//NOI18N
            locationProperty.setName(NbBundle.getMessage(ImportNode.class, "PROP_LOCATION_DISPLAYNAME"));
            locationProperty.setShortDescription(NbBundle.getMessage(ImportNode.class, "LOCATION_DESC"));
            ss.put(locationProperty);
            

            
        } catch(Exception ex) {
            mLogger.log(Level.SEVERE, "failed to create property sheet for "+ mWSDLConstruct, ex);
        }
    }

    @Override
    protected void updateDisplayName() {
        setDisplayName(mWSDLConstruct.getNamespace());
    }
    
    public class ImportPropertyAdapter extends PropertyAdapter {
        
        public ImportPropertyAdapter() {
            super(getWSDLComponent());
        }
        
        public void setLocation(String location) {
            getWSDLComponent().getModel().startTransaction();
            ((Import) getWSDLComponent()).setLocation(location);
                getWSDLComponent().getModel().endTransaction();
         }
         
         public String getLocation() {
             if(mWSDLConstruct.getLocation() == null) {
                 return "";
             }
             
             return mWSDLConstruct.getLocation();
         }
         
         public void setNamespace(String namespace) {
             getWSDLComponent().getModel().startTransaction();
             ((Import) getWSDLComponent()).setNamespace(namespace);
                 getWSDLComponent().getModel().endTransaction();
         }
         
         public String getNamespace() {
             if(mWSDLConstruct.getNamespace() == null) {
                 return "";
             }
             
             return mWSDLConstruct.getNamespace();
         } 
         
    }

    private final class ImportLocationProperty
            extends BaseAttributeProperty {

        public ImportLocationProperty(PropertyAdapter instance, Class valueType,
                String getter, String setter) throws NoSuchMethodException {
            super(instance, valueType, getter, setter);
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            return new ImportLocationPropertyEditor(mWSDLConstruct);
        }
    }

    @Override
    public String getTypeDisplayName() {
        return NbBundle.getMessage(ImportNode.class, "LBL_UnrecognizedImport_TypeDisplayName");
    }

}
