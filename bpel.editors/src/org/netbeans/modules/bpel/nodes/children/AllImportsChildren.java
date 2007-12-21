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
package org.netbeans.modules.bpel.nodes.children;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import org.netbeans.modules.bpel.core.BPELCatalog;
import org.netbeans.modules.bpel.core.helper.api.BusinessProcessHelper;
import org.netbeans.modules.soa.ui.nodes.NodeFactory;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.properties.editors.controls.filter.ChildTypeFilter;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.nodes.CategoryFolderNode;
import org.netbeans.modules.bpel.nodes.ReloadableChildren;
import org.netbeans.modules.bpel.properties.Constants.StereotypeFilter;
import org.netbeans.modules.bpel.editors.api.Constants.VariableStereotype;
import org.netbeans.modules.bpel.properties.Util;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Supports the list of Schema & WSDL files and Schema & WSDL Import nodes of the Process
 *
 * @author nk160297
 */
public class AllImportsChildren extends Children.Keys
        implements ReloadableChildren {
    
    private Lookup myLookup;
    private Process myProcess;
    
    public AllImportsChildren(Process process, Lookup lookup) {
        myLookup = lookup;
        myProcess = process;
        setKeys(createKeys());
    }
    
    protected Node[] createNodes(Object key) {
        Node[] result = null;
        //
        if (key instanceof Node) {
            result = new Node[]{(Node)key};
        }
        //
        return result;
    }
    
    protected Node[] createKeys() {
        //
        NodeFactory nodeFactory =
                (NodeFactory)myLookup.lookup(NodeFactory.class);
        BpelModel bpelModel =
                (BpelModel)myLookup.lookup(BpelModel.class);
        FileObject bpelFo = (FileObject)bpelModel.getModelSource().
                getLookup().lookup(FileObject.class);
        FileObject bpelFolderFo = bpelFo.getParent();
        //
        ArrayList<Node> nodesList = new ArrayList<Node>();
        ArrayList<FileObject> importedFiles = new ArrayList<FileObject>();
        //
        // Create import nodes
        Import[] importsArr = myProcess.getImports();
        for (Import importObj : importsArr) {
            String importType = importObj.getImportType();
            String location = importObj.getLocation();
            if (location != null) {
                FileObject importFo = Util.getRelativeFO(bpelFolderFo, location);
                if (importFo != null) {
                    // Collect imported Schema files to exclude them later
                    importedFiles.add(importFo);
                }
            }
            //
            if (Import.SCHEMA_IMPORT_TYPE.equals(importType)) {
                Node newSchemaImportNode = nodeFactory.createNode(
                        NodeType.IMPORT_SCHEMA, importObj, myLookup);
                nodesList.add(newSchemaImportNode);
            } else if (Import.WSDL_IMPORT_TYPE.equals(importType)) {
                Node newSchemaImportNode = nodeFactory.createNode(
                        NodeType.IMPORT_WSDL, importObj, myLookup);
                nodesList.add(newSchemaImportNode);
            }
        }
        //
        // Check if it is necessary to show not imported Schema files
        boolean showSchemaFiles = true;
        boolean showWsdlFiles = true;
        ChildTypeFilter filter =
                (ChildTypeFilter)myLookup.lookup(ChildTypeFilter.class);
        if (filter != null) {
            // Here the ChildTypeFilter is implied which doesn't require
            // a parent node, so the null is used.
            // See the TypeChooserPanel.constructRootNode()
            showSchemaFiles = filter.isPairAllowed(null, NodeType.SCHEMA_FILE);
            showWsdlFiles = filter.isPairAllowed(null, NodeType.WSDL_FILE);
        }
        //
        // Create Schema file nodes
        if (showSchemaFiles) {
            BusinessProcessHelper bpHelper = (BusinessProcessHelper)myLookup.
                    lookup(BusinessProcessHelper.class);
            Collection<FileObject> schemaFiles = bpHelper.getSchemaFilesInProject();
            for (FileObject schemaFile : schemaFiles) {
                if (importedFiles.contains(schemaFile)) {
                    // Skip imported Schema files
                    continue;
                }
                //
                String extension = schemaFile.getExt();
                {
                    ModelSource modelSource = Utilities.getModelSource(
                            schemaFile, true);
                    if (modelSource != null) {
                        SchemaModel schemaModel = SchemaModelFactory.getDefault().
                                getModel(modelSource);
                        if (schemaModel != null) {
                            Node newNode = nodeFactory.createNode(
                                    NodeType.SCHEMA_FILE, schemaModel, myLookup);
                            if (newNode != null) {
                                nodesList.add(newNode);
                            }
                        }
                    }
                }
            }
        }
        //
        // Create WSDL file nodes
        if (showWsdlFiles) {
            BusinessProcessHelper bpHelper = (BusinessProcessHelper)myLookup.
                    lookup(BusinessProcessHelper.class);
            Collection<FileObject> wsdlFiles = bpHelper.getWSDLFilesInProject();
            for (FileObject wsdlFile : wsdlFiles) {
                if (importedFiles.contains(wsdlFile)) {
                    // Skip imported WSDL files
                    continue;
                }
                //
                String extension = wsdlFile.getExt();
                {
                    ModelSource modelSource = Utilities.getModelSource(
                            wsdlFile, true);
                    if (modelSource != null) {
                        WSDLModel wsdlModel = WSDLModelFactory.getDefault().
                                getModel(modelSource);
                        if (wsdlModel != null) {
                            Node newNode = nodeFactory.createNode(
                                    NodeType.WSDL_FILE, wsdlModel, myLookup);
                            if (newNode != null) {
                                nodesList.add(newNode);
                            }
                        }
                    }
                }
            }
        }
        //
        // Sort all nodes alphabetically
        Comparator comparator = new BpelNode.NameComparator();
        Collections.sort(nodesList, comparator);
        //
        // Add "Build In Types" category node if they are allowed
        // The node is added to the beginning of the list!
        StereotypeFilter stFilter = (StereotypeFilter)myLookup.
                lookup(StereotypeFilter.class);
        boolean primitiveTypesAllowed = true; // allowed by default
        if (stFilter != null) {
            primitiveTypesAllowed = stFilter.
                    isStereotypeAllowed(VariableStereotype.PRIMITIVE_TYPE);
        }
        //
        if (primitiveTypesAllowed) {
            Children children = new PrimitiveTypeChildren(myLookup);
            Node primitiveTypesNode = new CategoryFolderNode(
                    NodeType.PRIMITIVE_TYPE, children, myLookup);
            nodesList.add(0, primitiveTypesNode);
        }
        
        boolean globalBpelCatalogAllowed = true;
        if ( globalBpelCatalogAllowed ) {
            
            BPELCatalog bpelCatalog = BPELCatalog.getDefault();

        //
        // Create Schema file nodes
            
            Children children = bpelCatalog != null 
                    ? new BpelGlobalCatalogChildren(bpelModel, bpelCatalog, myLookup)
                    : Children.LEAF;
            Node globalBpelCatalogNode = new CategoryFolderNode(
                    NodeType.BPEL_GLOBAL_CATALOG, children, myLookup);
            nodesList.add(1, globalBpelCatalogNode);
        }
        //
        Node[] nodes = nodesList.toArray(new Node[nodesList.size()]);
        return nodes;
    }
    
    public void reload() {
        // setKeys(new Object[] {new Object()});
        setKeys(createKeys());
    }
}
