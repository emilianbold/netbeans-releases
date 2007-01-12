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
import org.netbeans.modules.bpel.design.nodes.NodeType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.model.SchemaModel;
//import org.netbeans.modules.xml.schema.ui.nodes.categorized.CategorizedSchemaNodeFactory;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

public class MessagePartNode extends BpelNode<Part> {
    
    public MessagePartNode(Part reference, final Lookup lookup) {
        // super(reference, new MySchemaChildren(), lookup);
        super(reference, Children.LEAF, lookup);
        //
        // Check if the node should be considered as leaf.
        if (isChildrenAllowed()) {
            Children.MUTEX.postWriteRequest(new Runnable() {
                public void run() {
                    setChildren(new MySchemaChildren(lookup));
                }
            });
        }
    }
    
    public NodeType getNodeType() {
        return NodeType.MESSAGE_PART;
    }
    
    public String getDisplayName() {
        Part ref = getReference();
        return ref == null ? null : ref.getName();
    }
    
    private static class MySchemaChildren extends Children.Keys {
        
        private Lookup myLookup;
        
        public MySchemaChildren(Lookup lookup) {
            super();
            myLookup = lookup;
            // It's necesary to specify any key. Otherwise it doesn't work.
            setKeys(new Object[] {new Object()});
        }
        
        protected Node[] createNodes(Object key) {
            Node parentNode = getNode();
            if (parentNode != null) {
                assert parentNode instanceof MessagePartNode;
                Part part = ((MessagePartNode)parentNode).getReference();
                if (part == null) {
                    return new Node[0];
                }
                
                NamedComponentReference<GlobalElement> elementRef = part.getElement();
                if (elementRef != null) {
                    GlobalElement element = elementRef.get();
                    if (element != null) {
                        
                        ArrayList<Class<? extends SchemaComponent>> filters = 
                                new ArrayList<Class<? extends SchemaComponent>>();
                        filters.add(GlobalType.class);
                        filters.add(GlobalElement.class);
                        //                                        
                        /*
                        CategorizedSchemaNodeFactory nodeFactory =
                                new CategorizedSchemaNodeFactory(
                                (SchemaModel)element.getModel(), filters, myLookup);
                        Node elementTypeNode = nodeFactory.createNode(element);
                        //                                          
                        return new Node[] {elementTypeNode};
                        */
                    }
                }
            }
            return null;
        }
    }
}
