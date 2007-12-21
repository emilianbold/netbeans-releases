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
package org.netbeans.modules.bpel.properties.editors.nodes.factory;

import org.netbeans.modules.soa.ui.nodes.NodeFactory;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.nodes.CategoryFolderNode;
import org.netbeans.modules.bpel.nodes.children.BpelUserFaultsChildren;
import org.netbeans.modules.bpel.nodes.children.FaultNameStandardChildren;
import org.netbeans.modules.bpel.nodes.children.WsdlFaultsChildren;
import org.netbeans.modules.bpel.nodes.children.WsdlImportsChildren;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.soa.ui.ExtendedLookup;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * This factory create the nodes tree for the Fault Name chooser.
 * It contains the following structure:
 *
 * Process
 *  |-System Faults 
 *     |-System Fault 1
 *     |-System Fault 2
 *     |-System Fault 3
 *  |-WSDL Files 
 *     |-Wsdl File 1
 *        |- WSDL Fault 1
 *        |- WSDL Fault 2
 *        |- WSDL Fault 3
 *     |-Wsdl File 2
 *     |-Wsdl File 3
 *
 * @author nk160297
 */
public class FaultNameChooserNodeFactory implements NodeFactory<NodeType> {
    
    private NodeFactory myDelegate;
    
    /** Creates a new instance of PropertyChooserNodeFactory */
    public FaultNameChooserNodeFactory(NodeFactory delegate) {
        myDelegate = delegate;
    }
    
    public Node createNode(NodeType nodeType, Object ref, 
            Children children, Lookup lookup) {
        return myDelegate.createNode(nodeType, ref, children, lookup);
    }
    
    public Node createNode(NodeType nodeType, Object ref, Lookup lookup) {
        //
        Children children;
        Node newNode = null;
        //
        NodeFactory nodeFactory =
                (NodeFactory)lookup.lookup(NodeFactory.class);
        if (!(nodeFactory instanceof MessageExchangeChooserNodeFactory)) {
            lookup = new ExtendedLookup(lookup, this);
        }
        //
        switch (nodeType) {
            case PROCESS:
                assert ref instanceof Process;
                children = new Children.Array();
                //
                // Prepare Children Classes;
                FaultNameStandardChildren standardFaultsChildren = 
                        new FaultNameStandardChildren(this, lookup);
                WsdlImportsChildren wsdlImportFaultsChildren = 
                        new WsdlImportsChildren((Process)ref, lookup);
                BpelUserFaultsChildren userDefFaultsChildren = 
                        new BpelUserFaultsChildren((Process)ref, lookup, 
                        standardFaultsChildren, wsdlImportFaultsChildren);
                //
                children.add(new Node[] {
                    new CategoryFolderNode(NodeType.SYSTEM_FAULTS_FOLDER,
                            standardFaultsChildren, lookup),
                    new CategoryFolderNode(NodeType.WSDL_FILES_FOLDER,
                            wsdlImportFaultsChildren, lookup),
                    new CategoryFolderNode(NodeType.BPEL_FAULTS_FOLDER,
                            userDefFaultsChildren, lookup)
                });
                //
                newNode = myDelegate.createNode(nodeType, ref, children, lookup);
                return newNode;
            case IMPORT_WSDL:
                assert ref instanceof Import;
                Import importObj = (Import)ref;
                String importTypeName = importObj.getImportType();
                Constants.StandardImportType importType =
                        Constants.StandardImportType.forName(importTypeName);
                switch (importType) {
                    case IMPORT_WSDL:
                        // children = new MessageTypeChildren(importObj, lookup);
                        children = new WsdlFaultsChildren(importObj, lookup);
                        newNode = myDelegate.createNode(
                                nodeType, ref, children, lookup);
                        break;
                }
                return newNode;
            case WSDL_FILE:
                assert ref instanceof WSDLModel;
                WSDLModel wsdlModel = (WSDLModel)ref;
                //
                children = new WsdlFaultsChildren(wsdlModel, lookup);
                newNode = myDelegate.createNode(nodeType, ref, children, lookup);
                return newNode;
            default:
                newNode = myDelegate.createNode(nodeType, ref, lookup);
                return newNode;
        }
    }
}
