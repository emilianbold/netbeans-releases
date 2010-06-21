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

package org.netbeans.modules.encoder.ui.basic;

import java.util.List;
import java.util.ResourceBundle;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlBeans;
import org.netbeans.modules.xml.schema.model.Choice;
import org.netbeans.modules.xml.schema.model.ComplexType;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.NameableSchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SimpleContent;
import org.netbeans.modules.xml.schema.model.SimpleType;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.LocalType;
import org.netbeans.modules.xml.schema.model.TypeContainer;

/**
 * Schema utility class.
 *
 * @author Jun Xu
 */
public class SchemaUtility {
    
    public static final Attribute TYPE_ATTR =
            new Attribute() {
                public Class getMemberType() {
                    return null;
                }

                public String getName() {
                    return "type"; //NOI18N
                }

                public Class getType() {
                    return String.class;
                }
            };

    private static final ResourceBundle _bundle =
            ResourceBundle.getBundle("org/netbeans/modules/encoder/ui/basic/Bundle"); //NOI18N

    /**
     * Tests if the element is of simple type or a complex type with
     * simple content.
     *
     * @param elem the element declaration schema component
     */
    public static boolean isSimpleContent(Element elem) {
        if (elem == null) {
            throw new NullPointerException(
                    _bundle.getString("schema_util.exp.elem_is_null")); //NOI18N
        }
        Object xmlType = getXMLType(elem); 
        if (xmlType == null) {
            return false;
        }
        if (xmlType instanceof String) {  //NOI18N
            if ("anyType".equals(xmlType)) {
                return false;
            }
            return true;
        }
        if (xmlType instanceof SimpleType) {
            return true;
        }
        //Must be complex type
        if (((ComplexType) xmlType).getDefinition() instanceof SimpleContent) {
            return true;
        }
        return false;
    }
    
    /**
     * Tests if an element's type contains a choice group.
     *
     * @param elem the element declaration schema component
     */
    public static boolean isChoice(Element elem) {
        if (elem == null) {
            throw new NullPointerException(
                    _bundle.getString("schema_util.exp.elem_is_null")); //NOI18N
        }
        Object xmlType = getXMLType(elem); 
        if (!(xmlType instanceof ComplexType)) {
            return false;
        }
        //Must be complex type
        return ((ComplexType) xmlType).getDefinition() instanceof Choice;
    }
    
    /**
     * Gets the XML type of an element declaration.  The return value must be
     * one of the following:
     *     <code>null</code> - either the element does not have a type or the
     *                         type is not resolvable
     *     an instance of String - the element is of XSD built-in type (
     *                             the string contains the local name if the type)
     *     an instance of SimpleType - the element is of simple type
     *     an instance of ComplexType - the element is of complex type
     */
    public static Object getXMLType(Element elem) {
        if (elem == null) {
            throw new NullPointerException(
                    _bundle.getString("schema_util.exp.elem_is_null")); //NOI18N
        }
        
        String type = elem.getAttribute(TYPE_ATTR);
        if (type == null) {
            List<SimpleType> stList = elem.getChildren(SimpleType.class);
            if (stList != null && stList.size() > 0) {
                return stList.get(0);
            }
            List<ComplexType> ctList = elem.getChildren(ComplexType.class);
            if (ctList != null && ctList.size() > 0) {
                return ctList.get(0);
            }
            //no type attribute specified and no inline simple or complex type
            return null;
        }
        String prefix = "";  //NOI18N
        int pos;
        if ((pos = type.indexOf(':')) >= 0 && type.length() > pos + 1) {
            prefix = type.substring(0, pos);
            type = type.substring(pos + 1);
        }
        if (elem.getPeer() == null) {
            return false;
        }
        String nsURI = elem.getPeer().lookupNamespaceURI(prefix);
        QName typeQName;
        if (nsURI == null || nsURI.length() == 0) {
            typeQName = new QName(type);
        } else {
            typeQName = new QName(nsURI, type);
        }
        SchemaType schemaType = XmlBeans.getBuiltinTypeSystem().findType(typeQName);
        if (schemaType != null && schemaType.isBuiltinType()) {
            //built-in type, return the type's local name
            return typeQName.getLocalPart();
        }
        GlobalSimpleType st = elem.getModel().resolve(
                typeQName.getNamespaceURI(), typeQName.getLocalPart(), GlobalSimpleType.class);
        if (st != null) {
            return st;
        }
        GlobalComplexType ct = elem.getModel().resolve(
                typeQName.getNamespaceURI(), typeQName.getLocalPart(), GlobalComplexType.class);
        if (ct != null) {
            return ct;
        }
        //not resolvable
        return null;
    }
    
    /**
     * Checks if the given element is of Any type.
     * @param elem an Element instance.
     * @return true if the given element is of Any type, or false otherwise.
     */
    public static boolean isOfAnyType(Element elem) {
        if (!(elem instanceof TypeContainer)) {
            return false;
        }
        GlobalType type = null;
        if (((TypeContainer) elem).getType() != null
                && ((TypeContainer) elem).getType().get() != null) {
            type = ((TypeContainer) elem).getType().get();
        }
        LocalType localType = ((TypeContainer) elem).getInlineType();
        if (type == null && localType == null) {
            return true;
        }
        if (type == null || !"anyType".equals(type.getName())) { //NOI18N
            return false;
        }
        if (type.getModel() == null
                || XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(
                       type.getModel().getSchema().getTargetNamespace())) {
            return true;
        }
        return false;
    }
    
    public static boolean isAnyType(SchemaComponent type) {
        if (type == null) {
            return true;
        }
        if (!(type instanceof GlobalType)) {
            return false;
        }
        if (!"anyType".equals(((GlobalType) type).getName())) { //NOI18N
            return false;
        }
        if (type.getModel() == null
                || XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(
                       type.getModel().getSchema().getTargetNamespace())) {
            return true;
        }
        return false;
    }
    
    public static String getNCName(SchemaComponent sc) {
        if (sc instanceof NameableSchemaComponent) {
            return ((NameableSchemaComponent) sc).getName();
        }
        return null;
    }
    
    public static String getNCNamePath(SchemaComponent[] scs) {
        if (scs == null || scs.length == 0) {
            return "";  //NOI18N
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < scs.length; i++) {
            if ((scs[i] instanceof Element) && !(scs[i] instanceof ElementReference)
                    && (scs[i] instanceof NameableSchemaComponent)) {
                sb.append('/').append(((NameableSchemaComponent) scs[i]).getName());
            }
        }
        return sb.toString();
    }
}
