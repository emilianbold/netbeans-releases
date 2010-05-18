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
package org.netbeans.modules.soa.ui.axinodes;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.soa.ui.nodes.NodeFactory;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIModelFactory;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SimpleType;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Loads the list of child nodes for Schema component
 * or a Schema as a whole.
 *
 * @author nk160297
 */
public class AxiomChildren extends Children.Keys {
    
    private Lookup myLookup;
    
    public AxiomChildren(SchemaModel schemaModel, Lookup lookup) {
        myLookup = lookup;
        //
        setKeys(new Object[] {schemaModel});
    }
    
    public AxiomChildren(AXIModel axiModel, Lookup lookup) {
        myLookup = lookup;
        //
        setKeys(new Object[] {axiModel});
    }
    
    public AxiomChildren(SchemaComponent schemaComp, Lookup lookup) {
        myLookup = lookup;
        //
        setKeys(new Object[] {schemaComp});
    }
    
    public AxiomChildren(AXIComponent axiComp, Lookup lookup) {
        myLookup = lookup;
        //
        setKeys(new Object[] {axiComp});
    }
    
    protected Node[] createNodes(Object key) {
        if (key instanceof SchemaModel) {
            return createGlobalElementNodes((SchemaModel)key);
        } else if (key instanceof AXIModel) {
            return createGlobalAxiElementNodes((AXIModel)key);
        } else if (key instanceof SchemaComponent) {
            return createNodes((SchemaComponent)key);
        } else if (key instanceof AXIComponent) {
            return createNodes((AXIComponent)key);
        } else {
            return new Node[0];
        }
    }
    
    protected Node[] createNodes(SchemaComponent schemaComp) {
        NamedComponentReference<? extends GlobalType> typeRef = null;
        //
        AXIDocument axiDocument = null;
        //
        if (schemaComp instanceof GlobalElement) {
            GlobalElement element = (GlobalElement)schemaComp;
            if (element != null) {
                axiDocument = getAxiDocument(axiDocument, element);
                //
                AXIComponent axiComponent = AxiomUtils.findGlobalComponent(
                        axiDocument, Element.class, element);
                //
                if (axiComponent == null) {
                    typeRef = element.getType();
                } else {
                    return AxiomUtils.processAxiComponent(axiComponent, myLookup);
                }
            }
        } else if (schemaComp instanceof GlobalType) {
            GlobalType type = (GlobalType)schemaComp;
            if (type != null && !(type instanceof SimpleType)) {
                axiDocument = getAxiDocument(axiDocument, type);
                AXIComponent axiComponent = AxiomUtils.findGlobalComponent(
                        axiDocument, ContentModel.class, type);
                return AxiomUtils.processAxiComponent(axiComponent, myLookup);
            }
        }
        //
        return new Node[0];
    }
    
    protected Node[] createNodes(AXIComponent axiComponent) {
        return AxiomUtils.processAxiComponent(axiComponent, myLookup);
    }
    
    protected Node[] createGlobalElementNodes(SchemaModel schemaModel) {
        AXIModel axiModel = AXIModelFactory.getDefault().getModel(schemaModel);
        return createGlobalAxiElementNodes(axiModel);
    }
    
    protected Node[] createGlobalAxiElementNodes(AXIModel axiModel) {
        NodeFactory nodeFactory =
                (NodeFactory)myLookup.lookup(NodeFactory.class);
        assert nodeFactory != null : "Node factory has to be specified"; // NOI18N
        //
        ArrayList<Node> nodesList = new ArrayList<Node>();
        //
        if (axiModel != null) {
            AXIDocument axiDocument = axiModel.getRoot();
            if (axiDocument != null) {
                List<Element> elementsList = axiDocument.getChildren(Element.class);
                for (Element element : elementsList) {
                    Node newNode = nodeFactory.createNode(
                            NodeType.ELEMENT, element, myLookup);
                    if (newNode != null) {
                        nodesList.add(newNode);
                    }
                }
            }
        }
        //
        Node[] nodes = nodesList.toArray(new Node[nodesList.size()]);
        return nodes;
    }
    
    private AXIDocument getAxiDocument(AXIDocument axiDocument,
            SchemaComponent comp) {
        if (axiDocument == null) {
            SchemaModel model = comp.getModel();
            AXIModel axiModel = AXIModelFactory.getDefault().getModel(model);
            if (axiModel != null) {
                axiDocument = axiModel.getRoot();
            }
        }
        //
        return axiDocument;
    }
    
}

