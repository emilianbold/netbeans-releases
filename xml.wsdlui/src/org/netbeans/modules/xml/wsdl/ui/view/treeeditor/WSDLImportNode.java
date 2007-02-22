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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.cookies.WSDLDefinitionNodeCookie;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.WSDLDataObject;
import org.netbeans.modules.xml.wsdl.ui.property.model.Property;
import org.netbeans.modules.xml.wsdl.ui.view.ImportWSDLCustomizer;
import org.netbeans.modules.xml.xam.ui.customizer.Customizer;
import org.netbeans.modules.xml.xam.ui.customizer.CustomizerProvider;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.Node.PropertySet;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;


/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class WSDLImportNode extends ImportNode {
    
     @SuppressWarnings("hiding") Image ICON  = Utilities.loadImage
     ("org/netbeans/modules/xml/wsdl/ui/view/resources/import-wsdl.png");
   
    public WSDLImportNode(Import wsdlConstruct) {
        super(new WSDLImportNodeChildren(wsdlConstruct), 
              wsdlConstruct);
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
    public String getTypeDisplayName() {
        return NbBundle.getMessage(WSDLImportNode.class, "LBL_WSDLImportNode_TypeDisplayName");
    }

    @Override
    public boolean hasCustomizer() {
        return true;
    }

    @Override
    public CustomizerProvider getCustomizerProvider() {
        return new CustomizerProvider() {
            public Customizer getCustomizer() {
                return new ImportWSDLCustomizer((Import) getWSDLComponent());
            }
        };
    }

    @Override
    protected void updateDisplayName() {
        Import imp = (Import) getWSDLComponent();
        setDisplayName(imp.getLocation());
    }

     public static class WSDLImportNodeChildren extends GenericWSDLComponentChildren {
        
        private Import mWsdlConstruct;
    
        public WSDLImportNodeChildren(Import wsdlConstruct) {
            super(wsdlConstruct);
            this.mWsdlConstruct = wsdlConstruct;
        }
        
        @Override
        protected Node[] createNodes(Object key) {
            if(key instanceof WSDLModel) { 
                WSDLModel document = (WSDLModel) key;
                Definitions definitions = document.getDefinitions();
                try {
                    
                    DataObject dataObj = DataObject.find((FileObject) document.getModelSource().getLookup().lookup(FileObject.class));
                    if(dataObj != null && dataObj instanceof WSDLDataObject) {
                        WSDLDefinitionNodeCookie cookie = Utils.getWSDLDefinitionNodeCookie(getNode());
                        ExplorerManager manager = null;
                        TopComponent topComponent = null;
                        
                        if(cookie != null) {
                            manager = cookie.getDefinitionsNode().getExplorerManager();
                            topComponent = cookie.getDefinitionsNode().getTopComponent();
                        }
                        
                        DefinitionsNode node = new DefinitionsNode(definitions);
                        FilterNode filterNode = new ReadOnlyNode(node);
                        return new Node[] {filterNode};
                    }
                } catch(Exception ex) {
                    AbstractNode node = new AbstractNode(Children.LEAF) {};
                    node.setDisplayName("Error: could not load wsdl" );
                    return new Node[] {node};
                }
            }
            
            return super.createNodes(key);
            
        }
        
        @Override
        @SuppressWarnings("unchecked")
        protected Collection getKeys() {
            ArrayList keys = new ArrayList();
            List<WSDLModel> models = this.mWsdlConstruct.getModel().findWSDLModel(mWsdlConstruct.getNamespace());
            //getImportedObject();
            for (WSDLModel model : models) {
                if(model != null && model.getDefinitions() != null) {
                    keys.add(model);
                }
            }
            keys.addAll(super.getKeys());
            return keys;
        }
        
    
    }
     
     public static class ReadOnlyNode extends FilterNode {
         
         public ReadOnlyNode(Node original) {
             super(original, new ReadOnlyChildren(original));
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
        
        public ReadOnlyChildren(Node node) {
            super(node);
        }
        
        @Override
        protected Node[] createNodes(Node n) {
             return new Node[] {new ReadOnlyNode(n)};
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

