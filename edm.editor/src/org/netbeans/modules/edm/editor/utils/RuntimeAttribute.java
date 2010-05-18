/*
 * BEGIN_HEADER - DO NOT EDIT
 * 
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * If applicable add the following below this CDDL HEADER,
 * with the fields enclosed by brackets "[]" replaced with
 * your own identifying information: Portions Copyright
 * [year] [name of copyright owner]
 */

/*
 * @(#)RuntimeAttribute.java 
 *
 * Copyright 2004-2007 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * END_HEADER - DO NOT EDIT
 */
package org.netbeans.modules.edm.editor.utils;

import java.sql.Types;

import org.w3c.dom.Element;

import org.netbeans.modules.edm.model.EDMException;
import org.openide.util.NbBundle;

/**
 * Encapsulates the value of a state variable as a name-value tuple.
 * 
 * @author Ritesh Adval
 */
public class RuntimeAttribute {

    private static final String TAG_ATTR = "runtimeAttr";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_TYPE = "type";
    private static final String ATTR_VALUE = "value";
    private String attributeName;
    private Object attributeValue;
    private int jdbcType;

    public RuntimeAttribute() {
    }

    public RuntimeAttribute(String name, String value, int type) {
        if (StringUtil.isNullString(name)) {
            throw new IllegalArgumentException(NbBundle.getMessage(RuntimeAttribute.class, "ERROR_empty_String_value_for_name."));
        }

        if (value == null) {
            throw new IllegalArgumentException(NbBundle.getMessage(RuntimeAttribute.class, "Must_supply_non-null_Object_ref_for_value."));
        }

        jdbcType = type;

        attributeName = name;
        attributeValue = value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        }

        boolean response = false;

        if (o instanceof RuntimeAttribute) {
            RuntimeAttribute attr = (RuntimeAttribute) o;

            response = (attributeName != null) ? attributeName.equals(attr.attributeName) : (attr.attributeName == null);
            response &= (jdbcType == attr.jdbcType);
            response &= (attributeValue != null) ? attributeValue.equals(attr.attributeValue) : (attr.attributeValue != null);
        }

        return response;
    }

    public String getAttributeName() {
        return this.attributeName;
    }

    public Object getAttributeObject() {
        return this.attributeValue;
    }

    public String getAttributeValue() {
        return this.attributeValue.toString();
    }

    public int getJdbcType() {
        return this.jdbcType;
    }

    @Override
    public int hashCode() {
        int hashCode = 0;

        hashCode += (attributeName != null) ? attributeName.hashCode() : 0;
        hashCode += jdbcType;
        hashCode += (attributeValue != null) ? attributeValue.hashCode() : 0;

        return hashCode;
    }

    public void parseXMLString(Element xmlElement) throws EDMException {
        attributeName = xmlElement.getAttribute(ATTR_NAME);
        attributeValue = xmlElement.getAttribute(ATTR_VALUE);
        String typeStr = xmlElement.getAttribute(ATTR_TYPE);
        try {
            jdbcType = Integer.parseInt(typeStr);
        } catch (NumberFormatException e) {
            throw new EDMException(NbBundle.getMessage(RuntimeAttribute.class, "ERROR_Invalid_JDBC_type"));
        }
    }

    public void setAttributeName(String aName) {
        this.attributeName = aName;
    }

    public void setAttributeValue(Object aValue) {
        this.attributeValue = aValue;
    }

    public void setAttributeValue(String aValue) {
        this.attributeValue = aValue;
    }

    public void setJdbcType(int aType) {
        this.jdbcType = aType;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append("RuntimeAttribute: {").append(attributeName).append("=");
        if (attributeValue == null) {
            buf.append("<null>");
        } else {
            switch (jdbcType) {
                case Types.CHAR:
                case Types.DATE:
                case Types.TIME:
                case Types.TIMESTAMP:
                case Types.VARCHAR:
                    buf.append("'").append(attributeValue).append("'");
                    break;

                default:
                    buf.append(attributeValue);
            }
        }
        buf.append("}");

        return buf.toString();
    }

    public String toXMLString() {
        return toXMLString("");
    }

    public String toXMLString(String prefix) {
        StringBuffer xml = new StringBuffer();

        if (prefix == null) {
            prefix = "";
        }

        if (attributeValue != null) {
            xml.append(prefix);
            xml.append("<" + TAG_ATTR + " ");
            xml.append(ATTR_NAME + "=\"" + attributeName).append("\" ");
            xml.append(ATTR_TYPE + "=\"" + jdbcType).append("\" ");
            xml.append(ATTR_VALUE + "=\"");
            xml.append((attributeValue != null) ? XmlUtil.escapeXML(attributeValue.toString()) : "");
            xml.append("\" />\n");
        }

        return xml.toString();
    }
}
