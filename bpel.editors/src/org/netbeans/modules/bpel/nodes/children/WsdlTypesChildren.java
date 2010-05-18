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
import org.netbeans.modules.bpel.nodes.CategoryFolderNode;
import org.netbeans.modules.soa.ui.nodes.NodeFactory;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.support.ImportHelper;
import org.netbeans.modules.bpel.properties.Constants.StereotypeFilter;
import org.netbeans.modules.bpel.editors.api.Constants.VariableStereotype;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Loads the list of Message Type nodes
 * related to the specific WSDL file or WSDL Import.
 * Also the "Embedded Schema Types" folder can be loaded. 
 *
 * Also the embedded Schema types are shown.
 *
 * @author nk160297
 */
public class WsdlTypesChildren extends Children.Keys {
    
    private Lookup myLookup;
    
    public WsdlTypesChildren(WSDLModel wsdlModel, Lookup lookup) {
        myLookup = lookup;
        //
        setKeys(new Object[] {wsdlModel});
    }
    
    public WsdlTypesChildren(Import importObj, Lookup lookup) {
        myLookup = lookup;
        //
        WSDLModel wsdlModel = ImportHelper.getWsdlModel(importObj, true);
        if (wsdlModel != null) {
            setKeys(new Object[] {wsdlModel});
        }
    }
    
    protected Node[] createNodes(Object key) {
        assert key instanceof WSDLModel;
        WSDLModel wsdlModel = (WSDLModel)key;
        NodeFactory nodeFactory =
                (NodeFactory)myLookup.lookup(NodeFactory.class);
        ArrayList<Node> nodesList = new ArrayList<Node>();
        //
        Children children = new WsdlEmbeddedSchemasChildren(wsdlModel, myLookup);
        Node newNode = new CategoryFolderNode(
                NodeType.EMBEDDED_SCHEMAS_FOLDER, children, myLookup);
        if (newNode != null) {
            nodesList.add(newNode);
        }
        //
        StereotypeFilter stFilter = (StereotypeFilter)myLookup
                .lookup(StereotypeFilter.class);
        boolean showMessage = true;
        if (stFilter != null) {
            showMessage = false;
            VariableStereotype[] vstTypes = stFilter.getAllowedStereotypes();
            for (VariableStereotype elem : vstTypes) {
                if (VariableStereotype.MESSAGE.equals(elem)) {
                    showMessage = true;
                }
            }
        }

        if (showMessage) {
            Collection<Message> messages = wsdlModel.getDefinitions().getMessages();
            for (Message message : messages) {
                newNode = nodeFactory.createNode(
                        NodeType.MESSAGE_TYPE, message, myLookup);
                nodesList.add(newNode);
            }
        }
        //
        Node[] nodes = nodesList.toArray(new Node[nodesList.size()]);
        return nodes;
    }
}
