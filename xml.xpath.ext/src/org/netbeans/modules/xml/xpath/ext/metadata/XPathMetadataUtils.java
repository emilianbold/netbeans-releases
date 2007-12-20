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

package org.netbeans.modules.xml.xpath.ext.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.xml.xpath.ext.schema.CheckTypeDerivationVisitor;
import org.netbeans.modules.xml.schema.model.ComplexType;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.LocalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.model.SimpleType;
import org.netbeans.modules.xml.schema.model.TypeContainer;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author nk160297
 */
public final class XPathMetadataUtils {

    /**
     * Takes a list of abstract arguments and convert it to the list 
     * of arguments' descriptors. The ArgumentGrop objects are filtered out.
     * 
     * @param sourceArgList
     * @param returnMandatoryOnly specifies if only mandatory fields are required. 
     * @return
     */
    public static List<ArgumentDescriptor> getArgDescriptorsList(
            List<AbstractArgument> sourceArgList, boolean returnMandatoryOnly) {
        return populateArgList(sourceArgList, null, returnMandatoryOnly);
    }

    /**
     * Traverses the description of arguments and collects all 
     * argument descriptors. 
     * This method is recursive because arguments' description can have
     * hierarchical structure.
     * 
     * @param sourceArgList
     * @param resutlArgList
     * @return
     */
    private static List<ArgumentDescriptor> populateArgList(
            List<AbstractArgument> sourceArgList, 
            List<ArgumentDescriptor> resutlArgList, 
            boolean returnMandatoryOnly) {
        //
        for (AbstractArgument argument : sourceArgList) {
            if (returnMandatoryOnly && !argument.isMandatory()) {
                // Skip optional arguments if only mandatory ones are requested.
                continue;
            }
            //
            if (argument instanceof ArgumentDescriptor) {
                //
                if (resutlArgList == null) {
                    // Lazy initialization
                    resutlArgList = new ArrayList<ArgumentDescriptor>();
                }
                //
                resutlArgList.add((ArgumentDescriptor)argument);
            } else if (argument instanceof ArgumentGroup) {
                List<AbstractArgument> subArgList = 
                        ((ArgumentGroup)argument).getArgumentList();
                //
                resutlArgList = populateArgList(
                        subArgList, resutlArgList, returnMandatoryOnly);
            }
        }
        //
        return resutlArgList;
    }
    
    public static GlobalType findPrimitiveType(String typeName) {
        if (typeName == null || typeName.length() == 0) {
            return null;
        }
        //
        Collection<GlobalSimpleType> primitiveTypes = 
                SchemaModelFactory.getDefault().getPrimitiveTypesModel().
                getSchema().getSimpleTypes();
        for (GlobalSimpleType type : primitiveTypes) {
            if (typeName.equals(type.getName())) {
                return type;
            }
        }
        //
        return null;
    }

    /**
     * Checks if the derived type is derived from the base type. 
     * @param base
     * @param derived
     * @return
     */
    public static boolean isTypeDerived(SchemaComponent base, 
            SchemaComponent derived) {
        if (base instanceof GlobalType) {
            CheckTypeDerivationVisitor ctdVisitor = 
                    new CheckTypeDerivationVisitor((GlobalType)base, derived);
            return ctdVisitor.isDerived();
        } else {
            // any type can be derived only from a global type
            return false;
        }
    }

    /**
     * Tries determine the most appropriate XPath type by the schema component.
     * @param schemaComp
     * @return
     */
    public static XPathType calculateXPathType(SchemaComponent schemaComp) {
        //
        SchemaComponent schemaType = getSchemaType(schemaComp);
        //
        if (schemaType == null) {
            return null;
        }
        //
        if (schemaType instanceof SimpleType) {
            SchemaModel primitiveTypesModel = 
                    SchemaModelFactory.getDefault().getPrimitiveTypesModel();
            //
            GlobalSimpleType stringType = primitiveTypesModel.
                    findByNameAndType("string", GlobalSimpleType.class); // NOI18N
            if (schemaType.equals(stringType) || 
                    XPathMetadataUtils.isTypeDerived(schemaType, stringType)) {
                return XPathType.STRING_TYPE;
            }
            //
            GlobalSimpleType booleanType = primitiveTypesModel.
                    findByNameAndType("boolean", GlobalSimpleType.class); // NOI18N
            if (schemaType.equals(booleanType) || 
                    XPathMetadataUtils.isTypeDerived(schemaType, booleanType)) {
                return XPathType.BOOLEAN_TYPE;
            }
            //
            GlobalSimpleType decimalType = primitiveTypesModel.
                    findByNameAndType("decimal", GlobalSimpleType.class); // NOI18N
            if (schemaType.equals(booleanType) || 
                    XPathMetadataUtils.isTypeDerived(schemaType, decimalType)) {
                return XPathType.NUMBER_TYPE;
            }
            //
            GlobalSimpleType doubleType = primitiveTypesModel.
                    findByNameAndType("double", GlobalSimpleType.class); // NOI18N
            if (schemaType.equals(booleanType) || 
                    XPathMetadataUtils.isTypeDerived(schemaType, doubleType)) {
                return XPathType.NUMBER_TYPE;
            }
            //
            GlobalSimpleType floatType = primitiveTypesModel.
                    findByNameAndType("float", GlobalSimpleType.class); // NOI18N
            if (schemaType.equals(booleanType) || 
                    XPathMetadataUtils.isTypeDerived(schemaType, floatType)) {
                return XPathType.NUMBER_TYPE;
            }
            //
            GlobalSimpleType hexBinaryType = primitiveTypesModel.
                    findByNameAndType("hexBinary", GlobalSimpleType.class); // NOI18N
            if (schemaType.equals(booleanType) || 
                    XPathMetadataUtils.isTypeDerived(schemaType, hexBinaryType)) {
                return XPathType.NUMBER_TYPE;
            }
            //
            return XPathType.ANY_TYPE;
        } else if (schemaType instanceof ComplexType) {
            if (schemaComp instanceof Element) {
                if (isElementRepeating((Element)schemaComp)) {
                    return XPathType.NODE_SET_TYPE;
                } else {
                    return XPathType.NODE_TYPE;
                }
            }
        } 
        //
        return null;
    }
    
    /**
     * Determines if the specified element is repeating. 
     * @param element
     * @return
     */
    public static boolean isElementRepeating(Element element) {
        boolean isRepeating = false;
        String maxOccoursStr = null;
        if (element instanceof GlobalElement) {
            return false;
        } else if (element instanceof LocalElement) {
            LocalElement lElement = (LocalElement)element;
            //
            maxOccoursStr = lElement.getMaxOccursEffective();
        } else if (element instanceof ElementReference) {
            ElementReference elementRef = (ElementReference)element;
            //
            maxOccoursStr = elementRef.getMaxOccursEffective();
        }
        //
        if (maxOccoursStr != null) {
            try {
                int maxOccoursInt = Integer.parseInt(maxOccoursStr);
                isRepeating = maxOccoursInt > 1;  
            } catch (NumberFormatException ex) {
                // Do Nothing
                isRepeating = true;
            }
        }
        //
        return isRepeating;
    }
    
    /**
     * @param schemaComp
     * @return the schema type of a schema component. 
     */
    public static SchemaComponent getSchemaType(SchemaComponent schemaComp) {
        if (schemaComp instanceof GlobalType || 
                schemaComp instanceof LocalType) {
            return schemaComp;
        } else if (schemaComp instanceof TypeContainer) {
            NamedComponentReference<? extends GlobalType> gTypeRef = 
                    ((TypeContainer)schemaComp).getType();
            if (gTypeRef != null) {
                return gTypeRef.get();
            } else {
                return ((TypeContainer)schemaComp).getInlineType();
            }
        } else if (schemaComp instanceof ElementReference) { 
            NamedComponentReference<GlobalElement> gElementRef = 
                    ((ElementReference)schemaComp).getRef();
            if (gElementRef != null) {
                GlobalElement gElement = gElementRef.get();
                if (gElement != null) {
                    return getSchemaType(gElement);
                }
            }
        } else if (schemaComp instanceof LocalAttribute) { 
            NamedComponentReference<GlobalSimpleType> gTypeRef = 
                    ((LocalAttribute)schemaComp).getType();
            if (gTypeRef != null) {
                return gTypeRef.get();
            } else {
                return ((LocalAttribute)schemaComp).getInlineType();
            }
        } else if (schemaComp instanceof GlobalAttribute) {
            NamedComponentReference<GlobalSimpleType> gTypeRef = 
                    ((GlobalAttribute)schemaComp).getType();
            if (gTypeRef != null) {
                return gTypeRef.get();
            } else {
                return ((GlobalAttribute)schemaComp).getInlineType();
            }
        }
        //
        return null;
    }
    
}

