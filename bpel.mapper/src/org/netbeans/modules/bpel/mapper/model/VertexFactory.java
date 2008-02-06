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

package org.netbeans.modules.bpel.mapper.model;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.ImageIcon;
import org.netbeans.modules.soa.mappercore.model.Constant;
import org.netbeans.modules.soa.mappercore.model.Operation;
import org.netbeans.modules.soa.mappercore.model.Function;
import org.netbeans.modules.soa.mappercore.model.GraphSubset;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.model.VertexItem;
import org.netbeans.modules.xml.xpath.ext.CoreFunctionType;
import org.netbeans.modules.xml.xpath.ext.CoreOperationType;
import org.netbeans.modules.xml.xpath.ext.XPathNumericLiteral;
import org.netbeans.modules.xml.xpath.ext.XPathStringLiteral;
import org.netbeans.modules.xml.xpath.ext.metadata.AbstractArgument;
import org.netbeans.modules.xml.xpath.ext.metadata.ArgumentDescriptor;
import org.netbeans.modules.xml.xpath.ext.metadata.ArgumentGroup;
import org.netbeans.modules.xml.xpath.ext.metadata.OperationMetadata;
import org.netbeans.modules.xml.xpath.ext.metadata.CoreFunctionMetadata;
import org.netbeans.modules.xml.xpath.ext.metadata.ExtFunctionMetadata;
import org.netbeans.modules.xml.xpath.ext.metadata.GeneralFunctionMetadata;
import org.netbeans.modules.xml.xpath.ext.metadata.StubExtFunction;
import org.netbeans.modules.xml.xpath.ext.metadata.XPathType;

/**
 * Constructs Vertex objects for operatins, functions and constants.
 * This class is stateless, so it is used as a singleton.
 * 
 * @author nk160297
 */
public final class VertexFactory  {
        
    private static VertexFactory singleton = new VertexFactory();
    
    public static VertexFactory getInstance() {
        return singleton;
    }
    
    public static Class getEditorClass(ArgumentDescriptor argDescr) {
        XPathType argType = argDescr.getArgumentType();
        if (argType == XPathType.STRING_TYPE) {
            return String.class;
        } else if (argType == XPathType.NUMBER_TYPE) {
            return Number.class;
        } else {
            return null;
        }
    }
    
    public static VertexItem constructVItem(
            Vertex vertex, ArgumentDescriptor argDescr) {
        //
        String argTypeName = argDescr.getArgumentType().getName();
        VertexItem newVItem = new VertexItem(vertex, argDescr, null, 
                getEditorClass(argDescr), argTypeName, false);
        return newVItem;
    }
    
    public static VertexItem constructHairline(
            Vertex vertex, Object dataObject) {
        //
        VertexItem newVItem = new VertexItem(vertex, dataObject, null, 
                null, null, true);
        return newVItem;
    }
    
    // vlv
    private Vertex createVertex(Object object) {
      if (object instanceof CoreFunctionType) {
        return createCoreFunction((CoreFunctionType) object);
      }
      if (object instanceof ExtFunctionMetadata) {
          return createExtFunction((ExtFunctionMetadata)object);
      }
      if (object instanceof CoreOperationType) {
        return createCoreOperation((CoreOperationType) object);
      }
      if (object instanceof String) {
        return createStringLiteral((String) object);
      }
      if (object instanceof Number) {
        return createNumericLiteral((Number) object);
      }
      return null;
    }

    // vlv
    public GraphSubset createGraphSubset(Object object) {
//System.out.println("VertexFactory.createGraphSubset: " + object);
      Vertex vertex = createVertex(object);

      if (vertex == null) {
        return null;
      }
      return new GraphSubset(vertex);
    }
    
    public Function createCoreFunction(CoreFunctionType functionType) {
        if (functionType == null) {
          return null;
        }
        CoreFunctionMetadata metadata = functionType.getMetadata();
        //
        // Create a new Vertex itself
        Function newVertex = new Function(
                functionType, 
                metadata.getIcon(), 
                metadata.getDisplayName(), 
                metadata.getResultType().getName());
        //
        addVertexItems(newVertex, metadata);
        //
        return newVertex;
    }

    public Operation createCoreOperation(CoreOperationType operationType) {
        if (operationType == null) {
          return null;
        }
        OperationMetadata metadata = operationType.getMetadata();
        //
        // Create a new Vertex itself
        Operation newOper = new Operation(
                operationType, metadata.getIcon());
        //
        addVertexItems(newOper, metadata);
        //
        return newOper;
    }

    public Function createExtFunction(ExtFunctionMetadata metadata) {
        if (metadata == null || metadata == StubExtFunction.NULL_METADATA_STUB) {
            return null;
        }
        //
        if (StubExtFunction.STUB_FUNC_NAME.equals(metadata.getName())) {
            // Skip the stub() function. It doesn't intended to be shown.
            return null;
        }
        //
        XPathType resultType = metadata.getResultType();
        if (resultType == null) {
            resultType = XPathType.ANY_TYPE;
        }
        //
        Function newVertex = new Function(
                    metadata,
                    metadata.getIcon(), 
                    metadata.getDisplayName(), 
                    resultType.getName());
        //
        addVertexItems(newVertex, metadata);
        //
        return newVertex;
    }

    public Constant createNumericLiteral(Number value) { 
        Constant newVertex = new Constant(XPathNumericLiteral.class, 
                NUMBER_ICON);
        VertexItem contentItem = new VertexItem(newVertex, value, Number.class);
        newVertex.addItem(contentItem);
        return newVertex;
    }

    public Constant createStringLiteral(String value) {
        Constant newVertex = new Constant(XPathStringLiteral.class, 
                STRING_ICON);
        VertexItem contentItem = new VertexItem(newVertex, value, String.class);
        newVertex.addItem(contentItem);
        return newVertex;
    }
    
    //==========================================================================
    
    private void addVertexItems(Vertex vertex, GeneralFunctionMetadata metadata) {
        List<AbstractArgument> argList = metadata.getArguments();
        if (argList == null) {
            return; // Nothing to add
        }
        //
        List<VertexItem> itemsList = new ArrayList<VertexItem>();
        populateArguments(argList, vertex, itemsList, true);
        //
        for (VertexItem vItem : itemsList) {
            vertex.addItem(vItem);
        }
    }

    private void populateArguments(List<AbstractArgument> argList, Vertex vertex, 
            List<VertexItem> itemsList, boolean addEmbracingHairlines) {
        for (AbstractArgument argument : argList) {
            if (argument instanceof ArgumentDescriptor) {
                populateArgDescrItem(vertex, (ArgumentDescriptor)argument, 
                        itemsList, addEmbracingHairlines);
            } else if (argument instanceof ArgumentGroup) {
                populateGroupItems(vertex, (ArgumentGroup)argument, 
                        itemsList, addEmbracingHairlines);
            }
        }
    }
    
    private void populateArgDescrItem(Vertex vertex, ArgumentDescriptor argDescr, 
            List<VertexItem> itemsList, boolean addEmbracingHairlines) {
        //
        String argTypeName = argDescr.getArgumentType().getName();
        //
        // TODO: Save the argument descriptor in the vertex item as a data object. 
        // It should be helpful later when links are connected. 
        //
        if (!argDescr.isRepeated()) {
            // Simple case - there is only one simple argument
            VertexItem newVItem = constructVItem(vertex, argDescr);
            itemsList.add(newVItem);
        } else {
            // Complex case - the argument is repeated. 
            int minOccurs = argDescr.getMinOccurs();
            // if min count = 0 then at least one vertex has to be added
            if (minOccurs == 0) {
                minOccurs = 1;
            }
            //
            // Add the first hireline
            if (addEmbracingHairlines) {
                VertexItem newVItem = constructHairline(vertex, argDescr);
                itemsList.add(newVItem);
            }
            //
            boolean isFirst = true;
            for (int index = 0; index < minOccurs; index++) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    // Add intermediate hairlines
                    VertexItem newVItem = constructHairline(vertex, argDescr);
                    itemsList.add(newVItem);
                }
                //
                // Add real item
                VertexItem newVItem = constructVItem(vertex, argDescr);
                itemsList.add(newVItem);
            }
            //
            // Add the final hireline
            if (addEmbracingHairlines) {
                VertexItem newVItem = constructHairline(vertex, argDescr);
                itemsList.add(newVItem);
            }
        }
    }
    
    private void populateGroupItems(Vertex vertex, ArgumentGroup argumentGroup, 
            List<VertexItem> itemsList, boolean addEmbracingHairlines) {
        //
        List<AbstractArgument> subArgList = argumentGroup.getArgumentList();
        //
        if (!argumentGroup.isRepeated()) {
            // Simple case - the group is not repeated
            populateArguments(subArgList, vertex, itemsList, addEmbracingHairlines);
        } else {
            // Complex case - the group is repeated
            int minCount = argumentGroup.getMinOccurs();
            // if min count = 0 then at least one vertex has to be added
            if (minCount == 0) {
                minCount = 1;
            }
            //
            // Add the first hireline
            if (addEmbracingHairlines) {
                VertexItem newVItem = constructHairline(vertex, argumentGroup);
                itemsList.add(newVItem);
            }
            //
            boolean isFirst = true;
            for (int index = 0; index < minCount; index++) {
                //
                if (isFirst) {
                    isFirst = false;
                } else {
                    // Add intermediate hairlines
                    VertexItem newVItem = constructHairline(vertex, argumentGroup);
                    itemsList.add(newVItem);
                }
                //
                // Add real sub items
                populateArguments(subArgList, vertex, itemsList, false);
            }
            //
            // Add the final hireline
            if (addEmbracingHairlines) {
                VertexItem newVItem = constructHairline(vertex, argumentGroup);
                itemsList.add(newVItem);
            }
        }
    }
    
    public List<VertexItem> createGroupItems(Vertex vertex, 
            ArgumentGroup argumentGroup) {
        List<VertexItem> itemsList = new ArrayList<VertexItem>();
        populateGroupItems(vertex, argumentGroup, itemsList, false);
        return itemsList;
    }
    
    
    private static final Icon NUMBER_ICON = new ImageIcon(VertexFactory.class
            .getResource(
            "/org/netbeans/modules/bpel/mapper/palette/image/numeric.gif")); // NOI18N

    private static final Icon STRING_ICON = new ImageIcon(VertexFactory.class
            .getResource(
            "/org/netbeans/modules/bpel/mapper/palette/image/string.gif")); // NOI18N

}
    