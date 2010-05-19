/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.edm.editor.utils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Element;

import org.netbeans.modules.edm.model.EDMException;
import org.openide.util.NbBundle;

/**
 * Encapsulates the value of a state variable as a name-value tuple.
 * 
 * @author Ahimanikya Satapathy
 */
public class Attribute implements Cloneable {

    public static final String TAG_ATTR = "attr";

    /* Log4J category string */
    static final String LOG_CATEGORY = Attribute.class.getName();

    private static final String ATTR_NAME = "name";
    private static final String ATTR_TYPE = "type";
    private static final String ATTR_VALUE = "value";

    private static final List LEGAL_TYPES = Arrays.asList(new String[] { String.class.getName(), Integer.class.getName(), Boolean.class.getName(),
            List.class.getName(), ArrayList.class.getName()});

    public static boolean isValidType(String typeName) {
        return LEGAL_TYPES.contains(typeName);
    }

    private String attributeName;
    private String attributeType;
    private Object attributeValue;

    public Attribute() {
    }

    public Attribute(Attribute src) throws EDMException {
        if (src == null) {
            throw new IllegalArgumentException(NbBundle.getMessage(Attribute.class, "ERROR_can_not_create_new_instance",new Object[] {}) + src);
        }

        copyFrom(src);
    }

    public Attribute(String name, Object value) {
        if (StringUtil.isNullString(name)) {
            throw new IllegalArgumentException(NbBundle.getMessage(Attribute.class, "ERROR_empty_String_value_for_name."));
        }

        if (value == null) {
            throw new IllegalArgumentException(NbBundle.getMessage(Attribute.class, "ERROR_null_Object_ref_for_value."));
        }

        attributeType = value.getClass().getName();
        if (!Attribute.isValidType(attributeType)) {
            throw new IllegalArgumentException(NbBundle.getMessage(Attribute.class, "ERROR_Invalid_type") + attributeType);
        }

        attributeName = name;
        attributeValue = value;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Attribute attr;
        try {
            attr = new Attribute(this);
        } catch (EDMException ex) {
            throw new CloneNotSupportedException(NbBundle.getMessage(Attribute.class, "ERROR_can_not_create_clone") + this.toString());
        }
        return attr;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        }

        boolean response = false;

        if (o instanceof Attribute) {
            Attribute attr = (Attribute) o;

            response = (attributeName != null) ? attributeName.equals(attr.attributeName) : (attr.attributeName == null);
            response &= (attributeType != null) ? attributeType.equals(attr.attributeType) : (attr.attributeType == null);
            response &= (attributeValue != null) ? attributeValue.equals(attr.attributeValue) : (attr.attributeValue != null);
        }

        return response;
    }

    public String getAttributeName() {
        return this.attributeName;
    }

    public String getAttributeType() {
        return this.attributeType;
    }

    public Object getAttributeValue() {
        return this.attributeValue;
    }

    @Override
    public int hashCode() {
        int hashCode = 0;

        hashCode += (attributeName != null) ? attributeName.hashCode() : 0;
        hashCode += (attributeType != null) ? attributeType.hashCode() : 0;
        hashCode += (attributeValue != null) ? attributeValue.hashCode() : 0;

        return hashCode;
    }

    public void parseXMLString(Element xmlElement) throws EDMException {
        attributeName = xmlElement.getAttribute(ATTR_NAME);
        String value = xmlElement.getAttribute(ATTR_VALUE);
        if (value != null) {
            createValueFor(value, xmlElement.getAttribute(ATTR_TYPE));
        }
    }

    public void setAttributeName(String aName) {
        this.attributeName = aName;
    }

    public void setAttributeType(String aType) {
        this.attributeType = aType;
    }

    public void setAttributeValue(Object aValue) {
        this.attributeValue = aValue;
    }

    public void setAttributeValue(String aValue) {
        this.attributeValue = aValue;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer(50);
        buf.append("name = " + this.getAttributeName() + "\n");
        buf.append("type = " + this.getAttributeType() + "\n");
        buf.append("value = " + this.getAttributeValue());
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
            xml.append(ATTR_TYPE + "=\"" + attributeType).append("\" ");
            xml.append(ATTR_VALUE + "=\"");
            if (attributeType.equals(List.class.getName()) || attributeType.equals(ArrayList.class.getName())) {
                xml.append((StringUtil.createDelimitedStringFrom((List) attributeValue)));
            } else {
                xml.append((attributeValue != null) ? XmlUtil.escapeXML(attributeValue.toString()) : "null");
            }
            xml.append("\" />\n");
        }

        return xml.toString();
    }

    private void copyFrom(Attribute src) throws EDMException {
        this.setAttributeName(src.getAttributeName());
        this.setAttributeType(src.getAttributeType());
        if (src.getAttributeValue() != null) {
            createValueFor(src.getAttributeValue().toString(), src.getAttributeType());
        }
    }

    private void createValueFor(String value, String typeName) throws EDMException {
        if (StringUtil.isNullString(typeName)) {
            throw new IllegalArgumentException(NbBundle.getMessage(Attribute.class, "ERROR_empty_String_value_for_typeName."));
        }

        if (!isValidType(typeName)) {
            throw new IllegalArgumentException(NbBundle.getMessage(Attribute.class, "ERROR_Invalid_type") + typeName);
        }

        if (value == null) {
            throw new IllegalArgumentException(NbBundle.getMessage(Attribute.class, "Must_supply_non-null_Object_ref_for_value."));
        }

        attributeType = typeName;
        if (typeName.equals(List.class.getName()) || typeName.equals(ArrayList.class.getName())) {
            attributeValue = StringUtil.createStringListFrom(value);
        } else if (typeName.equals("java.lang.String")) {
            attributeValue = value;
        } else {
            try {
                Class objClass = Class.forName(typeName, true, getClass().getClassLoader());
                Constructor constructor = objClass.getConstructor(new Class[] { String.class});
                attributeValue = constructor.newInstance(new Object[] { value});
            } catch (NoSuchMethodException e) {
                throw new EDMException(NbBundle.getMessage(Attribute.class, "ERROR_constructor_with_single_String_parameter") + typeName, e);
            } catch (Exception e) {
                throw new EDMException(NbBundle.getMessage(Attribute.class, "ERROR_Could_not_load_class") + attributeType + "'.", e);
            }
        }
    }
}
