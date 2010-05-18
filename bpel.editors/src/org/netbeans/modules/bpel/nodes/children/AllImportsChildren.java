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
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.bpel.core.helper.api.BusinessProcessHelper;
import org.netbeans.modules.soa.ui.nodes.NodeFactory;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.nodes.CategoryFolderNode;
import org.netbeans.modules.bpel.nodes.ReloadableChildren;
import org.netbeans.modules.bpel.properties.Constants.StereotypeFilter;
import org.netbeans.modules.bpel.editors.api.Constants.VariableStereotype;
import org.netbeans.modules.bpel.model.api.support.ExtensionModelRetriever;
import org.netbeans.modules.bpel.model.api.support.Utils;
import org.netbeans.modules.bpel.properties.Util;
import org.netbeans.modules.xml.reference.ReferenceChild;
import org.netbeans.modules.xml.reference.ReferenceUtil;
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
public class AllImportsChildren extends Children.Keys {
    
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
        BusinessProcessHelper bpHelper = (BusinessProcessHelper)myLookup.
                lookup(BusinessProcessHelper.class);
        //
        ArrayList<Node> nodesList = new ArrayList<Node>();
        ArrayList<Node> notImportedNodesList = new ArrayList<Node>();
        ArrayList<FileObject> importedFiles = new ArrayList<FileObject>();

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
        // Create Schema file nodes
        Collection<FileObject> schemaFiles = bpHelper.getSchemaFilesInProject();
        for (FileObject schemaFile : schemaFiles) {
            if (importedFiles.contains(schemaFile)) {
                // Skip imported Schema files
                continue;
            }
            //
            ModelSource modelSource = Utilities.getModelSource(
                    schemaFile, true);
            if (modelSource != null) {
                SchemaModel schemaModel = SchemaModelFactory.getDefault().
                        getModel(modelSource);
                if (schemaModel != null) {
                    Node newNode = nodeFactory.createNode(
                            NodeType.SCHEMA_FILE, schemaModel, myLookup);
                    if (newNode != null) {
                        notImportedNodesList.add(newNode);
                    }
                }
            }
        }
        //
        // Create WSDL file nodes
        Collection<FileObject> wsdlFiles = bpHelper.getWSDLFilesInProject();
        for (FileObject wsdlFile : wsdlFiles) {
            if (importedFiles.contains(wsdlFile)) {
                // Skip imported WSDL files
                continue;
            }
            //
            ModelSource modelSource = Utilities.getModelSource(
                    wsdlFile, true);
            if (modelSource != null) {
                WSDLModel wsdlModel = WSDLModelFactory.getDefault().
                        getModel(modelSource);
                if (wsdlModel != null) {
                    Node newNode = nodeFactory.createNode(
                            NodeType.WSDL_FILE, wsdlModel, myLookup);
                    if (newNode != null) {
                        notImportedNodesList.add(newNode);
                    }
                }
            }
        }

        // Fault Message
        nodesList.add(0, nodeFactory.createNode(NodeType.FAULT_MESSAGE,
                ExtensionModelRetriever.getFaultMessageModel(), myLookup));

        // Add Not Imported files folder
        if (!notImportedNodesList.isEmpty()) {
            Children notImported = new Children.Array();
            notImported.add(notImportedNodesList.toArray(
                    new Node[notImportedNodesList.size()]));
            //
            Node notImportedNodesFolder = new CategoryFolderNode(
                    NodeType.NOT_IMPORTED_FILES, notImported, myLookup);
            nodesList.add(0, notImportedNodesFolder);
        }
        // Add referenced projects folder
        Project myProj = Utils.safeGetProject(bpelModel);
        
        if (myProj != null) {
            //
            List<Project> refProjList = ReferenceUtil.getReferencedProjects(myProj);
            if (refProjList != null && !refProjList.isEmpty()) {
                Children children = new RefProjectsChildren(refProjList, myLookup);
                Node refProjFolder = new CategoryFolderNode(
                        NodeType.REFERENCED_PROJECTS, children, myLookup);
                nodesList.add(0, refProjFolder);
            }
            //
            // Add referenced resources folder
            ArrayList<Node> refResNodesList = new ArrayList<Node>();
            //
            List<ReferenceChild> xsdNodes = ReferenceUtil.getReferencedResources(myProj, ReferenceUtil.XSD);
//System.out.println();
//System.out.println("xsd: " + xsdNodes.size());
            for (ReferenceChild xsdNode : xsdNodes) {
                Node newNode = nodeFactory.createNode(NodeType.REFERENCED_RESOURCE, xsdNode, myLookup);
                if (newNode != null) {
                    refResNodesList.add(newNode);
                }
            }
            //
            List<ReferenceChild> wsdlNodes = ReferenceUtil.getReferencedResources(myProj, ReferenceUtil.WSDL);

            for (ReferenceChild wsdlNode : wsdlNodes) {
                Node newNode = nodeFactory.createNode(NodeType.REFERENCED_RESOURCE, wsdlNode, myLookup);
                if (newNode != null) {
                    refResNodesList.add(newNode);
                }
            }
            //
            if (!refResNodesList.isEmpty()) {
                Children refResChildren = new Children.Array();
                refResChildren.add(refResNodesList.toArray(new Node[refResNodesList.size()]));
                Node refResourcesFolder = new CategoryFolderNode(NodeType.REFERENCED_RESOURCES, refResChildren, myLookup);
                nodesList.add(0, refResourcesFolder);
            }
        }
        //
        // Add Global Catalog folder
        boolean globalCatalogAllowed = true;
        if ( globalCatalogAllowed ) {
            // Create Schema file nodes
            Children children = new BpelGlobalCatalogChildren(bpelModel, myLookup);
            nodesList.add(0, new CategoryFolderNode(NodeType.BPEL_GLOBAL_CATALOG, children, myLookup));
        }
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
        //
        Node[] resultNodes = nodesList.toArray(new Node[nodesList.size()]);
        return resultNodes;
    }
    
}
