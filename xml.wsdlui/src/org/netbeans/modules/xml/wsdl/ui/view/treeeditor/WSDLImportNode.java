/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */


package org.netbeans.modules.xml.wsdl.ui.view.treeeditor;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.util.logging.Logger;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.ActionHelper;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.WSDLDataObject;
import org.netbeans.modules.xml.wsdl.ui.view.ImportWSDLCustomizer;
import org.netbeans.modules.xml.xam.ui.customizer.Customizer;
import org.netbeans.modules.xml.xam.ui.customizer.CustomizerProvider;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
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
        super(new WSDLImportChildFactory(wsdlConstruct), 
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
    
     public static class WSDLImportChildFactory extends ChildFactory implements Refreshable {
        private Import wsdlImport;
        
    
        public WSDLImportChildFactory(Import wsdlConstruct) {
            super();
            this.wsdlImport = wsdlConstruct;
        }
        
        @Override
        protected Node createNodeForKey(Object key) {
            if(key instanceof Definitions) { 
                try {
                    Definitions definitions = (Definitions) key;
                    // Create a lookup with save cookie of parent wsdl, so that save can be called on imported wsdl's nodes
                    DataObject dobj = ActionHelper.getDataObject(wsdlImport.getModel());
                    Lookup lookup = null;
                    if (dobj != null) {
                        lookup = new ProxyLookup(new Lookup[] {
                                Lookups.exclude(dobj.getNodeDelegate().getLookup(), new Class[] {
                                    Node.class,
                                    DataObject.class
                                })});
                    }
                    DataObject dataObj = DataObject.find(definitions.getModel().getModelSource().getLookup().lookup(FileObject.class));
                    if(dataObj != null && dataObj instanceof WSDLDataObject) {
                        Node node = NodesFactory.getInstance().create(definitions);
                        FilterNode filterNode = null;
                        if (lookup != null) {
                            filterNode = new ReadOnlyNode(node, lookup);
                        } else {
                            filterNode = new ReadOnlyNode(node);
                        }
                        return filterNode;
                    }
                } catch(Exception ex) {
                    AbstractNode node = new AbstractNode(Children.LEAF) {};
                    node.setDisplayName("Error: could not load wsdl" );
                    return node;
                }
            }
            return Node.EMPTY;
            
            //return super.createNodes(key);
            
        }
        
        @Override
        protected boolean createKeys(List keys) {
            List<WSDLModel> models = wsdlImport.getModel().findWSDLModel(wsdlImport.getNamespace());
            //getImportedObject();
            for (WSDLModel model : models) {
                if(model != null && model.getDefinitions() != null) {
                    keys.add(model.getDefinitions());
                }
            }
            //keys.addAll(super.getKeys());
            //return keys;
            return true;
        }

        public void refreshChildren(boolean immediate) {
            refresh(immediate);
        }
        
    
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
                    Lookup lookup = null;
                    if (dobj != null) {
                        lookup = new ProxyLookup(new Lookup[] {
                                Lookups.exclude(dobj.getNodeDelegate().getLookup(), new Class[] {
                                    Node.class,
                                    DataObject.class
                                })});
                    }
                    DataObject dataObj = DataObject.find(definitions.getModel().getModelSource().getLookup().lookup(FileObject.class));
                    if(dataObj != null && dataObj instanceof WSDLDataObject) {
                        Node node = NodesFactory.getInstance().create(definitions);
                        FilterNode filterNode = null;
                        if (lookup != null) {
                            filterNode = new ReadOnlyNode(node, lookup);
                        } else {
                            filterNode = new ReadOnlyNode(node);
                        }
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

