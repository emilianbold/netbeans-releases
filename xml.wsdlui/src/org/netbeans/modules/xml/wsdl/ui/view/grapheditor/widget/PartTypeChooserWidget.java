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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
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
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.api.property.ElementOrTypePropertyPanel;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.border.BgBorder;
import org.netbeans.modules.xml.wsdl.ui.view.grapheditor.layout.LeftRightLayout;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.NodesFactory;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.PartNode;
import org.netbeans.modules.xml.wsdl.ui.wsdl.nodes.XSDBuiltInTypeFolderNode;
import org.netbeans.modules.xml.wsdl.ui.wsdl.nodes.XSDTypesNode;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author anjeleevich
 */
public class PartTypeChooserWidget extends Widget implements ActionListener {
    
    private LabelWidget partTypeLabel;
    private ButtonWidget showPartTypeChooserButton;
    private Part part;
    
    public PartTypeChooserWidget(Scene scene, Part part) {
        super(scene);
        
        this.part = part;
        
        partTypeLabel = new LabelWidget(scene, 
                MessagesUtils.getPartTypeOrElementString(part));
        partTypeLabel.setFont(scene.getDefaultFont());
        
        showPartTypeChooserButton = new ButtonWidget(scene, "...");
        showPartTypeChooserButton.setMargin(new Insets(1, 2, 1, 2));
        showPartTypeChooserButton.setActionListener(this);
        
        addChild(partTypeLabel);
        addChild(showPartTypeChooserButton);
        
        setLayout(new LeftRightLayout(8));
        setBorder(BORDER);
    }
    
    
    ButtonWidget getPartTypeChooserButton() {
        return showPartTypeChooserButton;
    }
    
    public void actionPerformed(ActionEvent event) {
        TypeChooserPanel typeChooserPanel = new TypeChooserPanel(part);
        DialogDescriptor descriptor = typeChooserPanel.getDialogDescriptor();
        
        Object result = DialogDisplayer.getDefault().notify(descriptor);
        
        if (result == DialogDescriptor.OK_OPTION) {
            SchemaComponent sc = typeChooserPanel.getSchemaComponent();
            if (sc != null) {
                WSDLModel model = part.getModel();
                
                if (sc instanceof GlobalType) {
                    try {
                        if (model.startTransaction()) {
                            part.setType(model.getDefinitions().createSchemaReference(
                                    (GlobalType) sc, GlobalType.class));
                            part.setElement(null);
                        }
                    } finally {
                        model.endTransaction();
                    }
                } else if (sc instanceof GlobalElement) {
                    try {
                        if (model.startTransaction()) {
                            part.setElement(model.getDefinitions().createSchemaReference(
                                    (GlobalElement) sc, GlobalElement.class));
                            part.setType(null);
                        }
                    } finally {
                        model.endTransaction();
                    }
                }
            }
        }
    }
    
    
    public static final Border BORDER = new BgBorder(
            new Insets(0, 0, 0, 0), new Insets(1, 8, 1, 1), null, Color.WHITE);
    
    
    private static class TypeChooserPanel extends JPanel 
            implements ExplorerManager.Provider 
    {
        private Part part;
        private BeanTreeView beanTreeView;
        private ExplorerManager explorerManager;
        private Node root;
        
        private SchemaComponent schemaComponent = null;
        
        private DialogDescriptor dialogDescriptor;
        
        public TypeChooserPanel(Part part) {
            super(new BorderLayout());

            this.part = part;
            
            PartNode partNode = new PartNode(part);

            root = new AbstractNode(new Children.Array());
            populateRootNode();
            
            beanTreeView = new BeanTreeView();
            beanTreeView.setRootVisible(false);
            beanTreeView.setSelectionMode(TreeSelectionModel
                    .SINGLE_TREE_SELECTION);
            beanTreeView.setPopupAllowed(false);
            beanTreeView.expandNode(root);
            beanTreeView.setDefaultActionAllowed(false);
            Utility.expandNodes(beanTreeView, 4, root);
            
            explorerManager = new ExplorerManager();
            explorerManager.setRootContext(root);
            explorerManager.addPropertyChangeListener(
                    new ExplorerPropertyChangeListener());
            
            add(beanTreeView, BorderLayout.CENTER);
            
            dialogDescriptor = new DialogDescriptor(this, 
                    partNode.getDisplayName());
            dialogDescriptor.setValid(false);
        }
        
        
        public DialogDescriptor getDialogDescriptor() {
            return dialogDescriptor;
        }
        
        
        public SchemaComponent getSchemaComponent() {
            return schemaComponent;
        }


        private void populateRootNode() {
            //type in current wsdl document
            Types types = part.getModel().getDefinitions().getTypes();
            if(types != null) {
                XSDTypesNode typesNode = new XSDTypesNode(NodesFactory
                        .getInstance().create(types), types.getSchemas());
                root.getChildren().add(new Node[] { 
                    new ElementOrTypePropertyPanel.EnabledNode(typesNode) });
            }

            // built in schema types
            XSDBuiltInTypeFolderNode builtInTypes 
                    = new XSDBuiltInTypeFolderNode();
            
            root.getChildren().add(new Node[] { 
                new ElementOrTypePropertyPanel.EnabledNode(builtInTypes)});

            //imported schemas
            List<Class<? extends SchemaComponent>> filters 
                    = new ArrayList<Class<? extends SchemaComponent>>();
            
            filters.add(GlobalSimpleType.class);
            filters.add(GlobalComplexType.class);
            filters.add(GlobalElement.class);
            filters.add(SchemaModelReference.class);
            
            Collection<Import> importedSchemas = part.getModel().getDefinitions()
                    .getImports();
            
            for (Import imp : importedSchemas) {
                List list = imp.getModel().findSchemas(imp.getNamespace());
                if (list != null && !list.isEmpty()) {
                    Schema schema = (Schema) list.get(0);
                    SchemaNodeFactory factory = new CategorizedSchemaNodeFactory(
                            schema.getModel(), filters, Lookup.EMPTY);
                    Node node = factory.createNode(schema);
                    root.getChildren().add(new Node[] { 
                        new ElementOrTypePropertyPanel.EnabledNode(node) });
                }
            }
            
        }
        
        
        public ExplorerManager getExplorerManager() {
            return explorerManager;
        }
        
        
        class ExplorerPropertyChangeListener implements PropertyChangeListener {
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
                    Node[] nodes = (Node[]) evt.getNewValue();
                    if(nodes.length > 0) {
                        Node node = nodes[0];
                        //set the selected node to null and state as invalid by default
                        
                        schemaComponent = null;
                        dialogDescriptor.setValid(false);
                        // mEnv.setState(PropertyEnv.STATE_INVALID);

                        SchemaComponent sc = null;
                        SchemaComponentReference reference = (SchemaComponentReference) node.getLookup().lookup(SchemaComponentReference.class);
                        
                        if (reference != null) {
                            sc = reference.get();
                        }
                        if (sc == null) {
                            sc = (SchemaComponent) node.getLookup().lookup(SchemaComponent.class);
                        }

                        if (sc != null && (sc instanceof GlobalType || sc instanceof GlobalElement)) {
                            schemaComponent = sc;
                            dialogDescriptor.setValid(true);
                        } else {
                            schemaComponent = null;
                            dialogDescriptor.setValid(false);
                        }
                    }
                }
            }
        }         
    }
}
