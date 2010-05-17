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
import java.util.Set;
import java.util.TreeSet;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import org.netbeans.modules.soa.ui.nodes.NodeFactory;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.support.ImportHelper;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.properties.ResolverUtility;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Supports the list of Fault Nodes which correspods to faults
 * declared at the specified WSDL files and WSDL Import
 *
 * @author nk160297
 */
public class WsdlFaultsChildren extends Children.Keys {
    
    private Lookup myLookup;
    
    public WsdlFaultsChildren(WSDLModel wsdlModel, Lookup lookup) {
        myLookup = lookup;
        setKeys(new Object[] {wsdlModel});
    }
    
    public WsdlFaultsChildren(Import importObj, Lookup lookup) {
        myLookup = lookup;
        //
        WSDLModel wsdlModel = ImportHelper.getWsdlModel(importObj, true);
        if (wsdlModel != null) {
            setKeys(new Object[] {wsdlModel});
        }
    }
    
    protected Node[] createNodes(Object key) {
        if (!(key instanceof WSDLModel)) {
            return null;
        }
        WSDLModel wsdlModel = (WSDLModel)key;
        ArrayList<Node> nodesList = new ArrayList<Node>();
        NodeFactory nodeFactory = myLookup.lookup(NodeFactory.class);
        BpelNode.DisplayNameComparator comparator =
                new BpelNode.DisplayNameComparator();
        //
        Set<String> faultNamesSet = new TreeSet<String>();
        //
        Collection<PortType> portTypes = wsdlModel.getDefinitions().getPortTypes();
        for (PortType portType : portTypes) {
            Collection<Operation> operations = portType.getOperations();
            for (Operation operation : operations) {
                Collection<Fault> faults = operation.getFaults();
                for (Fault fault : faults) {
                    faultNamesSet.add(fault.getName());
                }
            }
        }
        //
        String namespace = wsdlModel.getDefinitions().getTargetNamespace();
        BpelModel bpelModel = myLookup.lookup(BpelModel.class);
        NamespaceContext nsContext = bpelModel.getProcess().getNamespaceContext();
        String prefix = nsContext.getPrefix(namespace);
        //
        for (String faultName : faultNamesSet) {
            QName faultQName = null;
            //
            if (prefix == null || prefix.length() == 0) {
                faultQName = new QName(namespace, faultName);
            } else {
                faultQName = new QName(namespace, faultName, prefix);
            }
            //
            Node newNode = nodeFactory.createNode(
                    NodeType.FAULT, faultQName, myLookup);
            nodesList.add(newNode);
        }
        //
        Collections.sort(nodesList, comparator);
        Node[] nodes = nodesList.toArray(new Node[nodesList.size()]);
        return nodes;
    }
    
}
