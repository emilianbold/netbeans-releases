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
import org.netbeans.modules.bpel.nodes.CategoryFolderNode;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Supports the list of standard categories of schema elements:
 * Global Complex Types, Global Simple Types, Global Elements.
 * This list is immutable so it doesn't matter to do it reloadable.
 *
 * @author nk160297
 */
public class SchemaCategoriesChildren extends Children.Array {
    
    public SchemaCategoriesChildren(SchemaModel model, Lookup lookup) {
        super();
        //
        List<Node> nodesList = new ArrayList<Node>();
        Node newNode;
        //
        newNode = new CategoryFolderNode(NodeType.GLOBAL_COMPLEX_TYPE,
                new GlobalComplexTypeChildren(model, lookup),
                lookup);
        nodesList.add(newNode);
        //
        newNode = new CategoryFolderNode(NodeType.GLOBAL_SIMPLE_TYPE,
                new GlobalSimpleTypeChildren(model, lookup),
                lookup);
        nodesList.add(newNode);
        //
        newNode = new CategoryFolderNode(NodeType.GLOBAL_ELEMENT,
                new GlobalElementChildren(model, lookup),
                lookup);
        nodesList.add(newNode);
        //
        Node[] nodesArr = nodesList.toArray(new Node[nodesList.size()]);
        add(nodesArr);
    }
    
    public static class GlobalComplexTypeChildren extends Children.Keys {
        
        private Lookup myLookup;
        
        public GlobalComplexTypeChildren(SchemaModel model, Lookup lookup) {
            myLookup = lookup;
            //
            setKeys(new Object[] {model});
        }
        
        protected Node[] createNodes(Object key) {
            assert key instanceof SchemaModel;
            SchemaModel model = (SchemaModel)key;
            NodeFactory nodeFactory =
                    (NodeFactory)myLookup.lookup(NodeFactory.class);
            ArrayList<Node> nodesList = new ArrayList<Node>();
            //
            Collection<GlobalComplexType> types = model.getSchema().getComplexTypes();
            for (GlobalComplexType type : types) {
                Node newNode = nodeFactory.createNode(
                        NodeType.GLOBAL_COMPLEX_TYPE, type, myLookup);
                nodesList.add(newNode);
            }
            //
            Node[] nodes = nodesList.toArray(new Node[nodesList.size()]);
            return nodes;
        }
    }
    
    public static class GlobalSimpleTypeChildren extends Children.Keys {
        
        private Lookup myLookup;
        
        public GlobalSimpleTypeChildren(SchemaModel model, Lookup lookup) {
            myLookup = lookup;
            //
            setKeys(new Object[] {model});
        }
        
        protected Node[] createNodes(Object key) {
            assert key instanceof SchemaModel;
            SchemaModel model = (SchemaModel)key;
            NodeFactory nodeFactory =
                    (NodeFactory)myLookup.lookup(NodeFactory.class);
            ArrayList<Node> nodesList = new ArrayList<Node>();
            //
            Collection<GlobalSimpleType> types = model.getSchema().getSimpleTypes();
            for (GlobalSimpleType type : types) {
                Node newNode = nodeFactory.createNode(
                        NodeType.GLOBAL_SIMPLE_TYPE, type, myLookup);
                nodesList.add(newNode);
            }
            //
            Node[] nodes = nodesList.toArray(new Node[nodesList.size()]);
            return nodes;
        }
    }
    
    public static class GlobalElementChildren extends Children.Keys {
        
        private Lookup myLookup;
        
        public GlobalElementChildren(SchemaModel model, Lookup lookup) {
            myLookup = lookup;
            //
            setKeys(new Object[] {model});
        }
        
        protected Node[] createNodes(Object key) {
            assert key instanceof SchemaModel;
            SchemaModel model = (SchemaModel)key;
            NodeFactory nodeFactory =
                    (NodeFactory)myLookup.lookup(NodeFactory.class);
            ArrayList<Node> nodesList = new ArrayList<Node>();
            //
            Collection<GlobalElement> elements = model.getSchema().getElements();
            for (GlobalElement element : elements) {
                Node newNode = nodeFactory.createNode(
                        NodeType.GLOBAL_ELEMENT, element, myLookup);
                nodesList.add(newNode);
            }
            //
            Node[] nodes = nodesList.toArray(new Node[nodesList.size()]);
            return nodes;
        }
    }
    
    
}
