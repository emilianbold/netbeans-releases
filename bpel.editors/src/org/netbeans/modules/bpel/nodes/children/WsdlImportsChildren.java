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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.core.helper.api.BusinessProcessHelper;
import org.netbeans.modules.soa.ui.nodes.NodeFactory;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.properties.Util;
import org.netbeans.modules.bpel.properties.editors.controls.filter.ChildTypeFilter;
import org.netbeans.modules.bpel.nodes.WsdlFileNode;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.nodes.FaultNode;
import org.netbeans.modules.bpel.nodes.ImportWsdlNode;
import org.netbeans.modules.bpel.nodes.ReloadableChildren;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Supports the list of WSDL files and WSDL Import nodes of the Process
 *
 * @author nk160297
 */
public class WsdlImportsChildren extends Children.Keys
        implements ReloadableChildren {

    private Lookup myLookup;
    private Process myKey;
    
    public WsdlImportsChildren(Process process, Lookup lookup) {
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
        NodeFactory nodeFactory = (NodeFactory)myLookup.lookup(NodeFactory.class);
        BpelModel bpelModel = (BpelModel)myLookup.lookup(BpelModel.class);
        FileObject bpelFo = (FileObject)bpelModel.getModelSource().
                getLookup().lookup(FileObject.class);
        FileObject bpelFolderFo = bpelFo.getParent();
        //
        BpelNode.NodeTypeComparator comparator =
                new BpelNode.NodeTypeComparator(
                ImportWsdlNode.class, WsdlFileNode.class);
        //
        ArrayList<Node> nodesList = new ArrayList<Node>();
        ArrayList<FileObject> importedFiles = new ArrayList<FileObject>();
        //
        // Create WSDL imports nodes
        Import[] importsArr = process.getImports();
        for (Import importObj : importsArr) {
            String importType = importObj.getImportType();
            if (Import.WSDL_IMPORT_TYPE.equals(importType)) {
                String location = importObj.getLocation();
                if (location != null) {
                    FileObject importFo = 
                            Util.getRelativeFO(bpelFolderFo, location);
                    if (importFo != null) {
                        // Collect imported WSDL files to exclude them later
                        importedFiles.add(importFo);
                    }
                }
                //
                Node newWsdlImportNode = nodeFactory.createNode(
                        NodeType.IMPORT_WSDL, importObj, myLookup);
                nodesList.add(newWsdlImportNode);
            }
        }
        //
        // Check if it is necessary to show not imported WSDL files
        boolean showWsdlFiles = true;
        ChildTypeFilter filter =
                (ChildTypeFilter)myLookup.lookup(ChildTypeFilter.class);
        Node parentNode = getNode();
        if (filter != null && parentNode != null && parentNode instanceof BpelNode) {
            NodeType parentNodeType = ((BpelNode)parentNode).getNodeType();
            showWsdlFiles = filter.isPairAllowed(
                    parentNodeType, NodeType.WSDL_FILE);
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
        Collections.sort(nodesList, comparator);
        Node[] nodes = nodesList.toArray(new Node[nodesList.size()]);
        return nodes;
    }
    
    public void reload() {
        setKeys(new Object[] {new Object()});
        setKeys(new Object[] {myKey});
        // refreshKey(myKey); // this method invoke exception :-( 
    }
    
    /**
     * This method is used by the BpelUserFaultsChildren to extract 
     * all fault names are defined in all imported WSDL files. 
     * 
     * The method relies on the specific hierarchy strucure of nodes. 
     * It will not work properly if this structure is changed. It looks 
     * like a hack but it helps to optimize performance. The fault names 
     * are taken from the nodes tree instead of to be taken from WSDL files
     * again. 
     */ 
    public Set<QName> getImportedWsdlFaultNames() {
        HashSet<QName> result = new HashSet<QName>();
        //
        Node[] nodesArr = getNodes();
        for (Node node : nodesArr) {
            if (!(node instanceof ImportWsdlNode)) {
                continue;
            }
            //
            ImportWsdlNode importWsdlNode = (ImportWsdlNode)node;
            Node[] faultNodeArr = importWsdlNode.getChildren().getNodes();
            //
            for (Node fNode : faultNodeArr) {
                if(!(fNode instanceof FaultNode)) {
                    continue;
                }
                //
                FaultNode faultNode = (FaultNode)fNode;
                QName faultName = faultNode.getReference();
                //
                // The namespace URI has to be specified before
                assert faultName.getNamespaceURI() != null;
                //
                result.add(faultName);
            }
        }
        //
        return result;
    }
}
