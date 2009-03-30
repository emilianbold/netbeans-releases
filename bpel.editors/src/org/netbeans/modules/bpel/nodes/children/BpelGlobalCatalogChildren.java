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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.bpel.core.BPELCatalog;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.soa.ui.nodes.NodeFactory;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.locator.CatalogModelFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Shows the list of Bpel Global Catalog Elements
 *
 * @author Vitaly Bychkov
 */
public class BpelGlobalCatalogChildren extends Children.Keys {
    
    private Lookup myLookup;
    private BPELCatalog myBpelGlobalCatalog;
    private BpelModel myBpelModel;
    
    public BpelGlobalCatalogChildren(BpelModel model, BPELCatalog bpelCatalog, Lookup lookup) {
        myLookup = lookup;
        myBpelGlobalCatalog = bpelCatalog;
        myBpelModel = model;
        //
        Iterator<String> publicIDs = bpelCatalog.getPublicIDs();
        List<String> keys = new ArrayList<String>();
        if (publicIDs != null) {
            while (publicIDs.hasNext()) {
                String pubId = publicIDs.next();
//                String sysId = myBpelGlobalCatalog.getSystemID(pubId);
                if (pubId != null) {
                    String sysID = bpelCatalog.getSystemID(pubId);
                    keys.add(sysID);
                }
            }
        }
        
        setKeys(keys);
    }
    
    protected Node[] createNodes(Object key) {
        if(key instanceof String) {
            try {
                String sysID = (String) key;
                ModelSource modelSource = CatalogModelFactory.getDefault()
                        .getCatalogModel(myBpelModel.getModelSource())
                            .getModelSource(new URI(sysID));
                if (modelSource != null) {
                    SchemaModel schemaModel = SchemaModelFactory.getDefault().
                            getModel(modelSource);
                    if (schemaModel != null) {
                        NodeFactory nodeFactory =
                                myLookup.lookup(NodeFactory.class);
                        Node newNode = nodeFactory.createNode(
                                NodeType.SCHEMA_FILE, schemaModel, myLookup);
                        if (newNode != null) {
                            return new Node[] {newNode};
                        }
                    }
                }
                
//                return new Node[]{new PrimitiveTypeNode((GlobalSimpleType) key, myLookup)};
//            } catch (SAXException ex) {
//                ex.printStackTrace();
//            } catch (IOException ex) {
//                ex.printStackTrace();
            } catch (CatalogModelException ex) {
                ex.printStackTrace();
            } catch (URISyntaxException ex) {
                ex.printStackTrace();
            }
        }
        return new Node[0];
    }
    
}
