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
package org.netbeans.modules.bpel.properties.editors.nodes.factory;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.bpel.design.nodes.NodeFactory;
import org.netbeans.modules.bpel.design.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.nodes.children.AllImportsChildren;
import org.netbeans.modules.bpel.nodes.children.SimpleMessageChildren;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.properties.Constants.VariableStereotype;
import org.netbeans.modules.bpel.properties.ExtendedLookup;
import org.netbeans.modules.bpel.nodes.children.WsdlTypesChildren;
import org.netbeans.modules.bpel.nodes.children.PrimitiveTypeChildren;
import org.netbeans.modules.bpel.nodes.children.SchemaImportsChildren;
import org.netbeans.modules.bpel.nodes.children.WsdlEmbeddedSchemasChildren;
import org.netbeans.modules.bpel.nodes.children.WsdlImportsChildren;
import org.netbeans.modules.bpel.properties.Constants.StereotypeFilter;
import org.netbeans.modules.bpel.properties.ResolverUtility;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
//import org.netbeans.modules.xml.schema.ui.nodes.categorized.CategorizedSchemaNodeFactory;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 */
public class PropertyAliasTypeChooserNodeFactory implements NodeFactory {
    
    private NodeFactory myDelegate;
    
    /** Creates a new instance of PropertyChooserNodeFactory */
    public PropertyAliasTypeChooserNodeFactory(NodeFactory delegate) {
        myDelegate = delegate;
    }
    
    public Node createNode(NodeType nodeType,
            Object ref, Object diagramRef,
            Children children, Lookup lookup) {
        return myDelegate.createNode(nodeType, ref, diagramRef, children, lookup);
    }
    
    public Node createNode(NodeType nodeType,
            Object ref, Object diagramRef,
            Lookup lookup) {
        //
        Children children = null;
        Node newNode = null;
        //
        NodeFactory nodeFactory =
                (NodeFactory)lookup.lookup(NodeFactory.class);
        if (!(nodeFactory instanceof TypeChooserNodeFactory)) {
            lookup = new ExtendedLookup(lookup, this);
        }
        //
        switch (nodeType) {
            case PROCESS:
                assert ref instanceof Process;
                //
//                StereotypeFilter stFilter = (StereotypeFilter)lookup.
//                        lookup(StereotypeFilter.class);
//                if (stFilter != null && stFilter.isSingleStereotype()) {
//                    VariableStereotype vs = stFilter.getAllowedStereotypes()[0];
//                    switch (vs) {
//                        case GLOBAL_SIMPLE_TYPE:
//                        case GLOBAL_COMPLEX_TYPE:
//                        case GLOBAL_TYPE:
//                        case GLOBAL_ELEMENT:
//                            children = new SchemaImportsChildren(
//                                    (Process)ref, lookup);
//                            break;
//                        case MESSAGE:
                            children = new WsdlImportsChildren(
                                    (Process)ref, lookup);
//                            break;
//                        case PRIMITIVE_TYPE:
//                            children = new PrimitiveTypeChildren(lookup);
//                            break;
//                        default:
//                            children = Children.LEAF;
//                            break;
//                    }
//                } else {
//                    children = new AllImportsChildren((Process)ref, lookup);
//                }
                //
                newNode = myDelegate.createNode(
                        nodeType, ref, diagramRef, children, lookup);
                return newNode;
            case IMPORT:
//            case IMPORT_SCHEMA:
            case IMPORT_WSDL:
                assert ref instanceof Import;
                Import importObj = (Import)ref;
                String importTypeName = importObj.getImportType();
                Constants.StandardImportType importType =
                        Constants.StandardImportType.forName(importTypeName);
                switch (importType) {
                    case IMPORT_WSDL:
                        children = new WsdlTypesChildren(importObj, lookup);
                        newNode = myDelegate.createNode(
                                nodeType, ref, diagramRef, children, lookup);
                        break;
//                    case IMPORT_SCHEMA:
//                        SchemaModel schemaModel =
//                                ResolverUtility.getImportedScemaModel(
//                                importObj.getLocation(), lookup);
//                        children = createStandardSchemaChildren(
//                                schemaModel, lookup);
//                        newNode = myDelegate.createNode(
//                                nodeType, ref, diagramRef, children, lookup);
//                        break;
                    case IMPORT_UNKNOWN:
                        // Create a node without childern
                        newNode = myDelegate.createNode(
                                nodeType, ref, diagramRef, lookup);
                        break;
                }
                return newNode;
            case WSDL_FILE:
                assert ref instanceof WSDLModel;
                WSDLModel wsdlModel = (WSDLModel)ref;
                //
                children = new WsdlTypesChildren(wsdlModel, lookup);
                newNode = myDelegate.createNode(
                        nodeType, ref, diagramRef, children, lookup);
                return newNode;
//            case SCHEMA_FILE:
//                assert ref instanceof SchemaModel;
//                SchemaModel schemaModel = (SchemaModel)ref;
//                //
//                children = createStandardSchemaChildren(schemaModel, lookup);
//                newNode = myDelegate.createNode(
//                        nodeType, ref, diagramRef, children, lookup);
//                return newNode;
            case MESSAGE_TYPE :
                assert ref instanceof Message;
                children = new SimpleMessageChildren((Message)ref, lookup);
                newNode = myDelegate.createNode(
                        nodeType, ref, diagramRef, children, lookup);
                return newNode;
            case STEREOTYPE_GROUP:
                assert ref instanceof VariableStereotype;
                VariableStereotype vsType = (VariableStereotype)ref;
                //
                switch (vsType) {
                    case PRIMITIVE_TYPE:
                        children = new PrimitiveTypeChildren(lookup);
                        newNode = myDelegate.createNode(
                                nodeType, ref, diagramRef, children, lookup);
                        return newNode;
                }
            case EMBEDDED_SCHEMAS_FOLDER:
//                assert ref instanceof Model;
//                if (ref instanceof WSDLModel) {
//                    children = new WsdlEmbeddedSchemasChildren((WSDLModel)ref, lookup);
//                }
//                newNode = myDelegate.createNode(
//                        nodeType, ref, diagramRef, children, lookup);
                return newNode;
//            case EMBEDDED_SCHEMA:
//                assert ref instanceof Schema;
//                SchemaModel model = ((Schema)ref).getModel();
//                children = createStandardSchemaChildren(model , lookup);
//                newNode = myDelegate.createNode(
//                        nodeType, ref, diagramRef, children, lookup);
//                return newNode;
            default:
                newNode = myDelegate.createNode(
                        nodeType, ref, diagramRef, null, lookup);
                return newNode;
        }
    }
    
////    private Children createStandardSchemaChildren(
////            SchemaModel schemaModel, Lookup lookup) {
////        Children children = null;
////        //
////        if (schemaModel != null) {
////            Schema schema = schemaModel.getSchema();
////            if (schema != null) {
////                SchemaComponentReference<Schema> schemaRef =
////                        SchemaComponentReference.create(schema);
////                //
////                StereotypeFilter stFilter = (StereotypeFilter)lookup.
////                        lookup(StereotypeFilter.class);
////                CategorizedSchemaNodeFactory csFactory = null;
////                if (stFilter == null) {
////                    csFactory = new CategorizedSchemaNodeFactory(
////                            schemaModel, lookup);
////                } else {
////                    ArrayList<Class<? extends SchemaComponent>> childTypes =
////                            stFilter.constructSchemaFilter();
////                    //
////                    csFactory = new CategorizedSchemaNodeFactory(
////                            schemaModel, childTypes, lookup);
////                }
////                children = csFactory.createChildren(schemaRef);
////            }
////        }
////        //
////        if (children == null) {
////            children = Children.LEAF;
////        }
////        //
////        return children;
////    }
    
}
