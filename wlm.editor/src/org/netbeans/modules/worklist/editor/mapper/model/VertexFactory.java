/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.mapper.model;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.soa.mappercore.model.Constant;
import org.netbeans.modules.soa.mappercore.model.Operation;
import org.netbeans.modules.soa.mappercore.model.Function;
import org.netbeans.modules.soa.mappercore.model.GraphSubset;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.model.VertexItem;
import org.netbeans.modules.xml.time.Deadline;
import org.netbeans.modules.xml.time.Duration;
import org.netbeans.modules.xml.time.TimeUtil;
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
//      if (object instanceof XmlLiteralDataObject) {
//          return createXmlLiteral((XmlLiteralDataObject) object);
//      }
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
        
        try {
            if (value != null && value.length() > 0) {
            
            }
            Duration duration = TimeUtil.parseDuration(value, true, false);
            
            if (duration != null) {
                Constant newVertex = new Constant(XPathStringLiteral.class,
                        DURATION_ICON);
                VertexItem contentItem = new VertexItem(newVertex, duration, value, Duration.class);
                newVertex.addItem(contentItem);
                return newVertex;
            }
        } catch (Exception ex) {
            // Ignore exception. Just continue.
        }
        try {
            Deadline deadline = TimeUtil.parseDeadline(value, false);

            if (deadline != null) {
                Constant newVertex = new Constant(XPathStringLiteral.class,
                        DEADLINE_ICON);
                VertexItem contentItem = new VertexItem(newVertex, deadline, value, Deadline.class);
                newVertex.addItem(contentItem);
                return newVertex;
            }
        } catch (Exception ex) {
            // Ignore exception. Just continue.
        }
        
        //
        Constant newVertex = new Constant(XPathStringLiteral.class,
                STRING_ICON);
        VertexItem contentItem = new VertexItem(newVertex, value, String.class);
        newVertex.addItem(contentItem);
        return newVertex;
    }

//    public Constant createXmlLiteral(XmlLiteralDataObject value) {
//        Constant newVertex = new Constant(Literal.class, XML_LITERAL_ICON);
//        VertexItem contentItem = new VertexItem(newVertex, value,
//                value.getTextContent(), XmlLiteralDataObject.class);
//        newVertex.addItem(contentItem);
//        return newVertex;
//    }
   
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
    
    private static final String IMAGE_FOLDER =
            "/org/netbeans/modules/worklist/editor/mapper/palette/image/";

    private static final Icon NUMBER_ICON = new ImageIcon(VertexFactory.class
            .getResource(IMAGE_FOLDER + "numeric.gif")); // NOI18N

    private static final Icon STRING_ICON = new ImageIcon(VertexFactory.class
            .getResource(IMAGE_FOLDER + "string.gif")); // NOI18N

   private static final Icon DURATION_ICON = new ImageIcon(VertexFactory.class.
           getResource(IMAGE_FOLDER + "duration.gif")); // NOI18N

   private static final Icon DEADLINE_ICON = new ImageIcon(VertexFactory.class.
           getResource(IMAGE_FOLDER + "deadline.gif")); // NOI18N
   
    private static final Icon XML_LITERAL_ICON = new ImageIcon(VertexFactory.class.
            getResource(IMAGE_FOLDER + "literal.gif")); // NOI18N
}
    