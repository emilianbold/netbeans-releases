/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.nodes;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.bpel.design.nodes.NodeType;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.properties.ResolverUtility;
import org.netbeans.modules.bpel.properties.Util;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
//import org.netbeans.modules.xml.schema.ui.nodes.categorized.CategorizedSchemaNodeFactory;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author ads
 */
public class SchemaFileNode extends BpelNode<SchemaModel> {
    
    /**
     * @param name
     */
    public SchemaFileNode(SchemaModel model, Lookup lookup) {
        super(model, new SchemaTypeChildren(model, lookup), lookup);
    }
    
    public SchemaFileNode(SchemaModel model, Children children,  Lookup lookup) {
        super(model, children, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.SCHEMA_FILE;
    }
    
    protected String getNameImpl() {
        SchemaModel ref = getReference();
        if (ref == null) {
            return null;
        }
        FileObject fo = (FileObject)ref.getModelSource().
                getLookup().lookup(FileObject.class);
        if (fo != null) {
            String result = ResolverUtility.
                    calculateRelativePathName(fo, getLookup());
            if (result != null && result.length() != 0) {
                return result;
            }
        }
        //
        return "[" + Constants.MISSING + "] " + super.getNameImpl(); // NOI18N
    }
    
    protected String getImplHtmlDisplayName() {
        return Util.getGrayString(super.getImplHtmlDisplayName());
    }

    static class SchemaTypeChildren extends Children.Keys {
        
        private Lookup myLookup;
        
        // @SuppressWarnings("unchecked")
        SchemaTypeChildren(SchemaModel model, Lookup lookup){
            myLookup = lookup;
            setKeys(new Object[] {model});
        }
        
        
        /* (non-Javadoc)
         * @see org.openide.nodes.Children.Keys#createNodes(java.lang.Object)
         */
        protected Node[] createNodes( Object key ) {
            
            // Create the schema tree.
            if(key instanceof SchemaModel) {
                SchemaModel schemaModel = (SchemaModel) key;
                Schema schema = schemaModel.getSchema();
                Collection<GlobalElement> globalElements = schema.getElements();
                //
                ArrayList<Node> nodesList = new ArrayList<Node>();
                //
                ArrayList<Class<? extends SchemaComponent>> filters =
                        new ArrayList<Class<? extends SchemaComponent>>();
                filters.add(GlobalElement.class);
                /*
                CategorizedSchemaNodeFactory nodeFactory =
                        new CategorizedSchemaNodeFactory(
                        schemaModel, filters, myLookup);
                //
                for(GlobalElement element: globalElements){
                    Node newNode = nodeFactory.createNode(element);
                    nodesList.add(newNode);
                }
                //*/
                return nodesList.toArray(new Node[nodesList.size()]);
            }
            return null;
        }
    }
}
