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
import java.lang.reflect.InvocationTargetException;
import java.util.List;
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
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;


/**
 *
 * @author Ritesh Adval
 *
 * @version 
 */
public class ImportNode extends WSDLElementNode<Import> {
    
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

    @Override
    protected void refreshAttributesSheetSet(Sheet sheet)  {
        Sheet.Set ss = sheet.get(Sheet.PROPERTIES);
        
        try {
            //namespace
            Node.Property namespaceProperty = new BaseAttributeProperty(mPropertyAdapter, 
                                                                        String.class, 
                                                                        NAMESPACE_PROP);
            namespaceProperty.setName(Import.NAMESPACE_URI_PROPERTY);
            namespaceProperty.setDisplayName(NbBundle.getMessage(ImportNode.class, "PROP_NAMESPACE_DISPLAYNAME"));
            namespaceProperty.setShortDescription(NbBundle.getMessage(ImportNode.class, "NAMESPACE_DESC"));
            ss.put(namespaceProperty);
            
            //location
            Node.Property locationProperty = new ImportLocationProperty(mPropertyAdapter, 
                    String.class, 
                    "getLocation", 
                    "setLocation");//NOI18N
            locationProperty.setName(Import.LOCATION_PROPERTY);
            locationProperty.setDisplayName(NbBundle.getMessage(ImportNode.class, "PROP_LOCATION_DISPLAYNAME"));
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
            (getWSDLComponent()).setLocation(location);
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
             (getWSDLComponent()).setNamespace(namespace);
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
    
    
    public static class ReadOnlyNode extends FilterNode {
        
        public ReadOnlyNode(Node original, InstanceContent content, List objList) {
            super(original, new ReadOnlyChildren(original, objList), new ProxyLookup(new Lookup[] {new AbstractLookup(content), original.getLookup()}));
            if (objList != null) {
                for (Object obj : objList) {
                    content.add(obj);
                }
            }
        }
        
        @Override
       public javax.swing.Action[] getActions(boolean context) {
            return new javax.swing.Action[] {};
        }
        
         
       @Override
       public PropertySet[] getPropertySets () {
           PropertySet[] propertySet = super.getPropertySets();
           for(int i = 0; i < propertySet.length; i++) {
               PropertySet pSet = propertySet[i];
               ReadOnlyPropertySet rpSet = new ReadOnlyPropertySet(pSet);
               propertySet[i] = rpSet;
           }
           return propertySet;
       }
       
       @Override
       public boolean canRename()
       {
           return false;
       }
       
       @Override
       public boolean canDestroy()
       {
           return false;
       }
       
       @Override
       public boolean canCut()
       {
           return false;
       }
       
       @Override
       public boolean canCopy()
       {
           return false;
       }
       
       @Override
       public boolean hasCustomizer()
       {
           return false;
       }
    }
    
    
    public static class ReadOnlyChildren extends FilterNode.Children {
       
        private List objList;

        public ReadOnlyChildren(Node node, List objList) {
            super(node);
            this.objList = objList;
        }
        
        @Override
        protected Node copyNode(Node node) {
            return new ReadOnlyNode(node, new InstanceContent(), objList);
        }
   } 
   
   public static class ReadOnlyProperty extends Node.Property {
           
       private Node.Property mDelegate;
           
       public ReadOnlyProperty(Node.Property delegate) {
           super(delegate.getClass());
           this.mDelegate = delegate;
           this.setDisplayName(this.mDelegate.getDisplayName());
           this.setName(this.mDelegate.getName());
           this.setShortDescription(this.mDelegate.getShortDescription());
           this.setExpert(this.mDelegate.isExpert());
           this.setHidden(this.mDelegate.isHidden());
           this.setPreferred(this.mDelegate.isPreferred());
           
       }
       
       @Override
       public boolean equals(Object property) {
           return this.mDelegate.equals(property);
       }
       
       @Override
       public String getHtmlDisplayName() {
           return this.mDelegate.getHtmlDisplayName();
       }
       
       @Override
       public PropertyEditor getPropertyEditor() {
           return this.mDelegate.getPropertyEditor();
       }
       
       @Override
       public Class getValueType() {
           return this.mDelegate.getValueType();
       }
       
       @Override
       public int hashCode() {
           return this.mDelegate.hashCode();
       }
       
       @Override
       public boolean isDefaultValue() {
           return this.mDelegate.isDefaultValue();
       }
       
       @Override
       public void restoreDefaultValue() throws IllegalAccessException,
               InvocationTargetException {
           this.mDelegate.restoreDefaultValue();
       }
       
       @Override
       public boolean supportsDefaultValue() {
           return this.mDelegate.supportsDefaultValue();
       }
       
       @Override
       public boolean canRead() {
           return true;
       }
       
       @Override
       public boolean canWrite() {
           return false;
       }
       
       @Override
       public Object getValue() throws IllegalAccessException,
               InvocationTargetException {
           return mDelegate.getValue();
       }
       
       @Override
       public void setValue(Object val) throws IllegalAccessException,
               IllegalArgumentException, InvocationTargetException {
           //do nothing
       }
   }
   
   public static class ReadOnlyPropertySet extends Node.PropertySet {
           
       private Node.PropertySet mDelegate;
       
       public ReadOnlyPropertySet(Node.PropertySet delegate) {
           super(delegate.getName(), delegate.getDisplayName(), delegate.getShortDescription());
           this.mDelegate = delegate;
       }
       
       @Override
       public Property[] getProperties() {
           Property[] properties = this.mDelegate.getProperties();
           for(int i = 0; i < properties.length; i++) {
               Property p = properties[i];
               ReadOnlyProperty rp = new ReadOnlyProperty(p);
               properties[i] = rp;
           }
           
           return properties;
       }    
   }

}
