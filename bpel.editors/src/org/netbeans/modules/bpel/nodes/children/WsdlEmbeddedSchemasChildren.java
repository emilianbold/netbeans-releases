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
import org.netbeans.modules.soa.ui.nodes.NodeFactory;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.properties.ResolverUtility;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Shows the list of schemas is embedded to a WSDL.
 *
 * @author nk160297
 */
public class WsdlEmbeddedSchemasChildren extends Children.Keys {
    
    private Lookup myLookup;
    
    public WsdlEmbeddedSchemasChildren(WSDLModel wsdlModel, Lookup lookup) {
        myLookup = lookup;
        //
        setKeys(new Object[] {wsdlModel});
    }
    
    protected Node[] createNodes(Object key) {
        assert key instanceof WSDLModel;
        WSDLModel wsdlModel = (WSDLModel)key;
        NodeFactory nodeFactory = (NodeFactory)myLookup.lookup(NodeFactory.class);
        ArrayList<Node> nodesList = new ArrayList<Node>();
        //
        Definitions defs = wsdlModel.getDefinitions();
        if (defs != null) {
            Types types = defs.getTypes();
            if (types != null) {
                Collection<Schema> schemas = types.getSchemas();
                for (Schema schema : schemas) {
                    SchemaModel origSchemaModel = getOriginalSchemaModel(types, schema);
//                    System.out.println("origSchemaModel: "+origSchemaModel);
                    if (origSchemaModel != null) {
                        Schema origSchema = origSchemaModel.getSchema();
                        if (origSchema != null) {
                            schema = origSchema;
                        }
                    }
                    
                    Node newNode = nodeFactory.createNode(
                            NodeType.EMBEDDED_SCHEMA, schema, myLookup);
                    nodesList.add(newNode);
                }
            }
        }
        //
        Node[] nodes = nodesList.toArray(new Node[nodesList.size()]);
        return nodes;
    }

    private SchemaModel getOriginalSchemaModel(Types types, Schema schema) {
        if (types == null) {
            return null;
        }
//        return ResolverUtility.getImportedScemaModel("OTA_TravelItinerary.xsd", myLookup);
        
        return null;
    }

}
