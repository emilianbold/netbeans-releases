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
import java.util.Collections;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.nodes.CategoryFolderNode;
import org.netbeans.modules.xml.reference.ReferenceChild;
import org.netbeans.modules.xml.reference.ReferenceUtil;
import org.netbeans.modules.soa.ui.nodes.NodeFactory;
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
 * Shows the list of childern of a Project node
 *
 * @author Nikita Krjukov
 */
public class ProjectChildren extends Children.Keys {

    private Lookup myLookup;
    
    public ProjectChildren(Project proj, Lookup lookup) {
        myLookup = lookup;
        //
        setKeys(Collections.singletonList(proj));
    }
    
    protected Node[] createNodes(Object key) {
        assert key instanceof Project;
        ArrayList<Node> nodesList = new ArrayList<Node>();
        NodeFactory nodeFactory =
                (NodeFactory)myLookup.lookup(NodeFactory.class);
        Project refProj = (Project) key;
        //
        // Add project's WSDL
        List<FileObject> wsdlFiles = ReferenceUtil.getWSDLFilesRecursively(refProj, true);
        if (wsdlFiles != null && !wsdlFiles.isEmpty()) {
            for (FileObject wsdlFile : wsdlFiles) {
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
        //
        // Add project's XSD
        List<FileObject> xsdFiles = ReferenceUtil.getXSDFilesRecursively(refProj, true);
        if (xsdFiles != null && !xsdFiles.isEmpty()) {
            for (FileObject xsdFile : xsdFiles) {
                ModelSource modelSource = Utilities.getModelSource(
                        xsdFile, true);
                if (modelSource != null) {
                    SchemaModel xsdModel = SchemaModelFactory.getDefault().
                            getModel(modelSource);
                    if (xsdModel != null) {
                        Node newNode = nodeFactory.createNode(
                                NodeType.SCHEMA_FILE, xsdModel, myLookup);
                        if (newNode != null) {
                            nodesList.add(newNode);
                        }
                    }
                }
            }
        }
        //
        // Add referenced resources
        ArrayList<Node> refResNodesList = new ArrayList<Node>();
        List<ReferenceChild> xsdNodes = ReferenceUtil.getReferencedResources(refProj, ReferenceUtil.XSD);

        for (ReferenceChild xsdNode : xsdNodes) {
            Node newNode = nodeFactory.createNode(
                    NodeType.REFERENCED_RESOURCE, xsdNode, myLookup);
            if (newNode != null) {
                refResNodesList.add(newNode);
            }
        }
        //
        List<ReferenceChild> wsdlNodes = ReferenceUtil.getReferencedResources(refProj, ReferenceUtil.WSDL);
        for (ReferenceChild wsdlNode : wsdlNodes) {
            Node newNode = nodeFactory.createNode(
                    NodeType.REFERENCED_RESOURCE, wsdlNode, myLookup);
            if (newNode != null) {
                refResNodesList.add(newNode);
            }
        }
        //
        if (!refResNodesList.isEmpty()) {
            Children refResChildren = new Children.Array();
            refResChildren.add(refResNodesList.toArray(
                    new Node[refResNodesList.size()]));
            Node refResourcesFolder = new CategoryFolderNode(
                    NodeType.REFERENCED_RESOURCES, refResChildren, myLookup);
            nodesList.add(1, refResourcesFolder);
        }
        //
        // Add referenced projects
        List<Project> refProjList = ReferenceUtil.getReferencedProjects(refProj);
        if (refProjList != null && !refProjList.isEmpty()) {
            Children children = new RefProjectsChildren(refProjList, myLookup);
            Node refProjFolder = new CategoryFolderNode(
                    NodeType.REFERENCED_PROJECTS, children, myLookup);
            nodesList.add(0, refProjFolder);
        }
        //
        Node[] resultNodes = nodesList.toArray(new Node[nodesList.size()]);
        return resultNodes;
    }
}
