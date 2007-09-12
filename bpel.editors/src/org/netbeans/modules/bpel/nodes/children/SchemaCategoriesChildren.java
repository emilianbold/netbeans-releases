/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
