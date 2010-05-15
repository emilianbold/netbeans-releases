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
import org.netbeans.modules.bpel.core.helper.api.BusinessProcessHelper;
import org.netbeans.modules.soa.ui.nodes.NodeFactory;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.properties.editors.controls.filter.ChildTypeFilter;
import org.netbeans.modules.bpel.nodes.SchemaFileNode;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.nodes.ImportSchemaNode;
import org.netbeans.modules.bpel.nodes.ReloadableChildren;
import org.netbeans.modules.bpel.properties.Util;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Supports the list of Schema files and Schema Import nodes of the Process
 *
 * @author nk160297
 */
public class SchemaImportsChildren extends Children.Keys
        implements ReloadableChildren {

    private Lookup myLookup;
    private Process myKey;
    
    public SchemaImportsChildren(Process process, Lookup lookup) {
        myLookup = lookup;
        myKey = process;
        setKeys(new Object[] {process});
    }
    
    protected Node[] createNodes(Object key) {
        if (!(key instanceof Process)) {
            return null;
        }
        Process process = (Process)key;
        //
        NodeFactory nodeFactory =
                (NodeFactory)myLookup.lookup(NodeFactory.class);
        BpelModel bpelModel =
                (BpelModel)myLookup.lookup(BpelModel.class);
        FileObject bpelFo = (FileObject)bpelModel.getModelSource().
                getLookup().lookup(FileObject.class);
        FileObject bpelFolderFo = bpelFo.getParent();
        //
        BpelNode.NodeTypeComparator comparator =
                new BpelNode.NodeTypeComparator(
                ImportSchemaNode.class, SchemaFileNode.class);
        //
        ArrayList<Node> nodesList = new ArrayList<Node>();
        ArrayList<FileObject> importedFiles = new ArrayList<FileObject>();
        //
        // Create Schema imports nodes
        Import[] importsArr = process.getImports();
        for (Import importObj : importsArr) {
            String importType = importObj.getImportType();
            if (Import.SCHEMA_IMPORT_TYPE.equals(importType)) {
                String location = importObj.getLocation();
                if (location != null) {
                    FileObject importFo = 
                            Util.getRelativeFO(bpelFolderFo, location);
                    if (importFo != null) {
                        // Collect imported Schema files to exclude them later
                        importedFiles.add(importFo);
                    }
                }
                //
                Node newSchemaImportNode = nodeFactory.createNode(
                        NodeType.IMPORT_SCHEMA, importObj, myLookup);
                nodesList.add(newSchemaImportNode);
            }
        }
        //
        // Check if it is necessary to show not imported Schema files
        boolean showSchemaFiles = true;
        ChildTypeFilter filter =
                (ChildTypeFilter)myLookup.lookup(ChildTypeFilter.class);
        Node parentNode = getNode();
        if (filter != null && parentNode != null && parentNode instanceof BpelNode) {
            NodeType parentNodeType = ((BpelNode)parentNode).getNodeType();
            showSchemaFiles = filter.isPairAllowed(
                    parentNodeType, NodeType.SCHEMA_FILE);
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
        Collections.sort(nodesList, comparator);
        Node[] nodes = nodesList.toArray(new Node[nodesList.size()]);
        return nodes;
    }
    
    public void reload() {
        setKeys(new Object[] {new Object()});
        setKeys(new Object[] {myKey});
        // refreshKey(myKey); // this method invoke exception :-( 
    }
}
