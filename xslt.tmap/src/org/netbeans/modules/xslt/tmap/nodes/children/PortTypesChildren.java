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
package org.netbeans.modules.xslt.tmap.nodes.children;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.soa.ui.nodes.NodeFactory;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xslt.tmap.model.api.Import;
import org.netbeans.modules.xslt.tmap.nodes.NodeType;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * 
 * @author Vitaly Bychkov
 */
public class PortTypesChildren extends Children.Keys {
    
    private Lookup myLookup;
    private static final Logger LOGGER = Logger.getLogger(PortTypesChildren.class.getName());
    
    public PortTypesChildren(WSDLModel wsdlModel, Lookup lookup) {
        myLookup = lookup;
        //
        setKeys(new Object[] {wsdlModel});
    }
    
    public PortTypesChildren(Import importObj, Lookup lookup) {
        myLookup = lookup;
        //
        WSDLModel wsdlModel = null;
        try {
            wsdlModel = importObj.getImportModel();
        } catch (CatalogModelException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        if (wsdlModel != null) {
            setKeys(new Object[] {wsdlModel});
        }
    }
    
    protected Node[] createNodes(Object key) {
        if (!(key instanceof WSDLModel)) {
            return new Node[0];
        }
        WSDLModel wsdlModel = (WSDLModel)key;
        NodeFactory nodeFactory =
                (NodeFactory)myLookup.lookup(NodeFactory.class);
        ArrayList<Node> nodesList = new ArrayList<Node>();
        
        //
        Definitions defs = wsdlModel.getDefinitions();
        Collection<PortType> portTypes = defs.getPortTypes();
        if (portTypes != null) {
            Node ptNode = null;
            for (PortType portType : portTypes) {
                ptNode = nodeFactory.createNode(
                        NodeType.PORT_TYPE, portType, myLookup);
                if (ptNode != null) {
                    nodes.add(ptNode);
                }
            }
        }
        //
        Node[] nodesArr = nodesList.toArray(new Node[nodesList.size()]);
        return nodesArr;
    }
}
