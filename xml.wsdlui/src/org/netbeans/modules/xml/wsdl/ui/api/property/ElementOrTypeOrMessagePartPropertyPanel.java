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
 * Created on May 16, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.wsdl.ui.api.property;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JPanel;

import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaNodeFactory;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.CategorizedSchemaNodeFactory;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.ImportFolderNode;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.MessageFolderNode;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.NodesFactory;
import org.netbeans.modules.xml.wsdl.ui.wsdl.nodes.XSDBuiltInTypeFolderNode;
import org.netbeans.modules.xml.wsdl.ui.wsdl.nodes.XSDTypesNode;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;



/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ElementOrTypeOrMessagePartPropertyPanel extends JPanel {

    private PropertyEnv mEnv;

    private WsdlPartnerLinkTypeTreeView mTreeView;

    private Node mSelectedNode;

    private WSDLModel mModel;

    private ElementOrTypeOrMessagePartProvider mProv;


    public ElementOrTypeOrMessagePartPropertyPanel(ElementOrTypeOrMessagePartProvider prov, PropertyEnv env) {
        this.mProv = prov;
        this.mEnv = env;
        this.mEnv.setState(PropertyEnv.STATE_INVALID);

        this.mModel = mProv.getModel();

        initGUI();
    }

    private void initGUI() {
        this.setLayout(new BorderLayout());
        ElementOrTypeOrMessagePart elementOrTypeOrMessagePart = mProv.getValue();
        
        this.mTreeView = new WsdlPartnerLinkTypeTreeView(elementOrTypeOrMessagePart);
        this.add(BorderLayout.CENTER, this.mTreeView);
    }

    /** Override method to detect the OK button */
    @Override
    public void removeNotify() {
        if (mEnv.getState().equals(PropertyEnv.STATE_VALID)) {
            if (mSelectedNode != null) {
                ElementOrTypeOrMessagePart elementOrTypeOrMessagePart = null;
                WSDLComponent comp = (WSDLComponent) mSelectedNode.getLookup().lookup(WSDLComponent.class);
                if (comp != null) {
                    if (comp instanceof Part) {
                        elementOrTypeOrMessagePart = new ElementOrTypeOrMessagePart((Part) comp, mProv.getModel());
                    }
                } else {
                    SchemaComponent sc = null;
                    SchemaComponentReference reference = (SchemaComponentReference) mSelectedNode.getLookup().lookup(SchemaComponentReference.class);
                    if (reference != null) {
                        sc = reference.get();
                    }
                    if (sc == null) {
                        sc = (SchemaComponent) mSelectedNode.getLookup().lookup(SchemaComponent.class);
                    }

                    if (sc != null) {
                        if (sc instanceof GlobalType) {
                            elementOrTypeOrMessagePart = createType((GlobalType) sc);
                        } else if (sc instanceof GlobalElement) {
                            elementOrTypeOrMessagePart = createElement((GlobalElement) sc);
                        }
                    }
                }
                

                
                if(elementOrTypeOrMessagePart != null) {
                    this.firePropertyChange(ElementOrTypePropertyEditor.PROP_NAME, null, elementOrTypeOrMessagePart);
                }

            }

        } else {
        }
        super.removeNotify();
    }

    private ElementOrTypeOrMessagePart createElement(GlobalElement element) {
        return new ElementOrTypeOrMessagePart(element, mProv.getModel());
    }

    private ElementOrTypeOrMessagePart createType(GlobalType type) {
        return new ElementOrTypeOrMessagePart(type, mProv.getModel());
    }

    private class WsdlPartnerLinkTypeTreeView
            extends JPanel implements ExplorerManager.Provider {

        private BeanTreeView btv;

        private ExplorerManager manager;


        public static final String PROP_VALID_NODE_SELECTED = "PROP_VALID_NODE_SELECTED";//NOI18N

        public static final String PROP_DUPLICATE_NODE_SELECTED = "PROP_DUPLICATE_NODE_SELECTED"; //NOI18N

        public PropertyChangeSupport pChangeSupport = new PropertyChangeSupport(this);

        private Node mRootNode;

        private ElementOrTypeOrMessagePart previousSelection;




        public WsdlPartnerLinkTypeTreeView(ElementOrTypeOrMessagePart elementOrType) {
            initGUI();
            previousSelection = elementOrType;
        }


        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pChangeSupport.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pChangeSupport.removePropertyChangeListener(listener);
        }

        private void initGUI() {
            this.setLayout(new BorderLayout());

            manager = new ExplorerManager();
            manager.addPropertyChangeListener(new ExplorerPropertyChangeListener());

            mRootNode = new AbstractNode(new Children.Array());
            populateRootNode();
            manager.setRootContext( mRootNode );

            
            // Create the templates view
            btv = new BeanTreeView();
            btv.setRootVisible( false );
            btv.setSelectionMode( javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION );
            btv.setPopupAllowed( false );
            btv.expandNode(mRootNode);
            btv.setDefaultActionAllowed(false);
            Utility.expandNodes(btv, 4, mRootNode);
            manager.setExploredContext(mRootNode);
            this.add(btv, BorderLayout.CENTER);
            btv.setName(NbBundle.getMessage(ElementOrTypeOrMessagePartPropertyPanel.class, "ElementOrTypeOrMessagePartPropertyPanel.btv.name")); // NOI18N
            btv.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ElementOrTypeOrMessagePartPropertyPanel.class, "ElementOrTypeOrMessagePartPropertyPanel.btv.AccessibleContext.accessibleName")); // NOI18N
            btv.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ElementOrTypeOrMessagePartPropertyPanel.class, "ElementOrTypeOrMessagePartPropertyPanel.btv.AccessibleContext.accessibleDescription")); // NOI18N
        }

        public ExplorerManager getExplorerManager() {
            return manager;
        }

        BeanTreeView getTreeView() {
            return this.btv;
        }

        private void populateRootNode() {
            //show current messages
            MessageFolderNode msgFolderNode = new MessageFolderNode(mModel.getDefinitions());
            mRootNode.getChildren().add(new Node[] {new EnabledNode(msgFolderNode)});
            
            //show wsdl imports
            ImportFolderNode impFolderNode = new ImportFolderNode(mModel.getDefinitions());
            mRootNode.getChildren().add(new Node[] {new EnabledNode(impFolderNode)});
            
            
            //type in current wsdl document
            Types types = mModel.getDefinitions().getTypes();
            if(types != null) {
                XSDTypesNode typesNode = new XSDTypesNode(NodesFactory.getInstance().create(types), types.getSchemas());
                mRootNode.getChildren().add(new Node[] {new EnabledNode(typesNode)});
            }

            // built in schema types
            XSDBuiltInTypeFolderNode builtInTypes = new XSDBuiltInTypeFolderNode();
            mRootNode.getChildren().add(new Node[] {new EnabledNode(builtInTypes)});

            //imported schemas
            List<Class<? extends SchemaComponent>> filters = new ArrayList<Class<? extends SchemaComponent>>();
            filters.add(GlobalSimpleType.class);
            filters.add(GlobalComplexType.class);
            filters.add(GlobalElement.class);
            filters.add(SchemaModelReference.class);
            Collection<Import> importedSchemas = mModel.getDefinitions().getImports();
            for (Import imp : importedSchemas) {
                List list = imp.getModel().findSchemas(imp.getNamespace());
                if (list != null && list.size() > 0) {
                    Schema schema = (Schema) list.get(0);
                    SchemaNodeFactory factory = new CategorizedSchemaNodeFactory(
                            schema.getModel(), filters, Lookup.EMPTY);
                    Node node = factory.createNode(schema);
                    mRootNode.getChildren().add(new Node[] { new EnabledNode(node) });
                }
            }
            
        }



        class ExplorerPropertyChangeListener implements PropertyChangeListener {

            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
                    Node[] nodes = (Node[]) evt.getNewValue();
                    if(nodes.length > 0) {
                        Node node = nodes[0];
                        //set the selected node to null and state as invalid by default
                        mSelectedNode = null;
                        mEnv.setState(PropertyEnv.STATE_INVALID);
                        
                        WSDLComponent comp = (WSDLComponent) node.getLookup().lookup(WSDLComponent.class);
                        if (comp != null && comp instanceof Part) {
                            mSelectedNode = node;
                            mEnv.setState(PropertyEnv.STATE_VALID);
                            return;
                        }
                        SchemaComponent sc = null;
                        SchemaComponentReference reference = (SchemaComponentReference) node.getLookup().lookup(SchemaComponentReference.class);
                        if (reference != null) {
                            sc = reference.get();
                        }
                        if (sc == null) {
                            sc = (SchemaComponent) node.getLookup().lookup(SchemaComponent.class);
                        }
                        
                        if (sc != null && (sc instanceof GlobalType || sc instanceof GlobalElement)) {
                            mSelectedNode = node;
                            mEnv.setState(PropertyEnv.STATE_VALID);
                        }
                    }
                }
            }
        }
    }
    
    /*
     * Filternode to make the nodes look enabled.
     */
    public static class EnabledNode extends FilterNode {
        private static Pattern pattern = Pattern.compile("(^<font.*>)(.*)(<.*>$)");

        public EnabledNode(Node node) {
            super(node, new EnabledChildren(node));
        }
        
        @Override
        public String getHtmlDisplayName()
        {
            //strips off font tag, to make it not grey. IZ  
            String retValue = super.getHtmlDisplayName();
            if(retValue == null) retValue = getDisplayName();
            
            
            if(retValue != null) {
                Matcher matcher  = pattern.matcher(retValue);
                if (matcher.find()) {
                    return matcher.group(2);
                }
            }
            return retValue;
        }
        
        
    }
    
    private static class EnabledChildren extends FilterNode.Children {

        public EnabledChildren(Node node) {
            super(node);
        }
        
        @Override
        protected Node[] createNodes(Node n) {
             return new Node[] {new EnabledNode(n)};
        }
        
    }
    
    
}

