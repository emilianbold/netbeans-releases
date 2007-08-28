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
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.ActionHelper;
import org.netbeans.modules.xml.wsdl.ui.cookies.DataObjectCookieDelegate;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.WSDLDataObject;
import org.netbeans.modules.xml.wsdl.ui.view.ImportWSDLCustomizer;
import org.netbeans.modules.xml.xam.ui.cookies.GetComponentCookie;
import org.netbeans.modules.xml.xam.ui.customizer.Customizer;
import org.netbeans.modules.xml.xam.ui.customizer.CustomizerProvider;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;


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
                return new ImportWSDLCustomizer(getWSDLComponent());
            }
        };
    }

    @Override
    protected void updateDisplayName() {
        Import imp = getWSDLComponent();
        setDisplayName(imp.getLocation());
    }

     public static class WSDLImportNodeChildren extends GenericWSDLComponentChildren<Import> {
        
    
        public WSDLImportNodeChildren(Import wsdlConstruct) {
            super(wsdlConstruct);
        }
        
        @Override
        protected Node[] createNodes(Object key) {
            if(key instanceof Definitions) { 
                try {
                    Definitions definitions = (Definitions) key;
                    // Create a lookup with save cookie of parent wsdl, so that save can be called on imported wsdl's nodes
                    DataObject dobj = ActionHelper.getDataObject(getWSDLComponent().getModel());
                    List list = new ArrayList();
                    list.add(new DataObjectCookieDelegate(dobj));
                    
                    
                    DataObject dataObj = DataObject.find(definitions.getModel().getModelSource().getLookup().lookup(FileObject.class));
                    if(dataObj != null && dataObj instanceof WSDLDataObject) {
                        DefinitionsNode node = new DefinitionsNode(definitions);
                        FilterNode filterNode = new ReadOnlyNode(node, new InstanceContent(), list);
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
        public final Collection<WSDLComponent> getKeys() {
            ArrayList<WSDLComponent> keys = new ArrayList<WSDLComponent>();
            List<WSDLModel> models = getWSDLComponent().getModel().findWSDLModel(getWSDLComponent().getNamespace());
            //getImportedObject();
            for (WSDLModel model : models) {
                if(model != null && model.getDefinitions() != null) {
                    keys.add(model.getDefinitions());
                }
            }
            keys.addAll(super.getKeys());
            return keys;
        }
        
    
    }
     
    @Override
    public boolean hasChildren() {
         List<WSDLModel> models = getWSDLComponent().getModel().findWSDLModel(getWSDLComponent().getNamespace());
         //getImportedObject();
         for (WSDLModel model : models) {
             if(model != null && model.getDefinitions() != null) {
                 return true;
             }
         }
         return super.hasChildren();
    }
     
}

