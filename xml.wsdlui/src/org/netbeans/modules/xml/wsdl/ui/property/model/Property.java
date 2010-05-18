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

/**
 *  This generated bean class Property
 *  matches the schema element 'Property'.
 *  The root bean class is ElementProperties
 *
 *  ===============================================================
 *  Property represents each attribute that would be created for the Node in the wsdleditor tree.
 *                  It defines a way to specify customizers for attributes. 
 *                  There are 3 types of Property customizers:
 *                  SchemaCustomizer : The default Customizer is the SchemaCustomizer, which shows drop downs for enumerations and boolean attributes,
 *                  and String customizer for all other types. So if there is no Property defined for a attribute, it will have 
 *                  SchemaCustomizer.
 *                  BuiltInCustomizer : specifies a way to put already defined customizer to be shown. Examples are part chooser, message chooser etc.
 *                  NewCustomizer : provides a way to create a custom customizer specific to the user requirement.  When using this the developer has
 *                  to implement the SPI org.netbeans.modules.xml.wsdl.ui.spi.WSDLLookupProvider, and add a implementation of 
 *                  org.netbeans.modules.xml.wsdl.ui.spi.NewCustomizerProvider, which will provide the custom Node.Property to be shown in the 
 *                  wsdl editor property sheet.
 *              
 *  ===============================================================
 *  Generated on Mon Feb 05 17:54:51 PST 2007
 * @Generated
 */

package org.netbeans.modules.xml.wsdl.ui.property.model;

public class Property {
    public static final String ATTRIBUTENAME = "AttributeName"; // NOI18N
    public static final String ISNAMEABLEATTRIBUTE = "IsNameableAttribute"; // NOI18N
    public static final String DECORATORATTRIBUTE = "DecoratorAttribute";   // NOI18N
    public static final String GROUPNAME = "GroupName"; // NOI18N
    public static final String PROPERTYORDER = "PropertyOrder"; // NOI18N
    public static final String SCHEMACUSTOMIZER = "SchemaCustomizer";   // NOI18N
    public static final String BUILTINCUSTOMIZER = "BuiltInCustomizer"; // NOI18N
    public static final String NEWCUSTOMIZER = "NewCustomizer"; // NOI18N

    private java.lang.String _AttributeName;
    private boolean _IsNameableAttribute = false;
    private javax.xml.namespace.QName _DecoratorAttribute;
    private java.lang.String _GroupName;
    private int _PropertyOrder;
    private java.lang.Boolean _SchemaCustomizer;
    private BuiltInCustomizer _BuiltInCustomizer;
    private String _NewCustomizer;

    /**
     * Normal starting point constructor.
     */
    public Property() {
        _AttributeName = "";
    }

    /**
     * Required parameters constructor
     */
    public Property(java.lang.String attributeName, boolean isNameableAttribute) {
        _AttributeName = attributeName;
        _IsNameableAttribute = isNameableAttribute;
    }

    /**
     * Deep copy
     */
    public Property(org.netbeans.modules.xml.wsdl.ui.property.model.Property source) {
        this(source, false);
    }

    /**
     * Deep copy
     * @param justData just copy the XML relevant data
     */
    public Property(org.netbeans.modules.xml.wsdl.ui.property.model.Property source, boolean justData) {
        _AttributeName = source._AttributeName;
        _IsNameableAttribute = source._IsNameableAttribute;
        _DecoratorAttribute = source._DecoratorAttribute;
        _GroupName = source._GroupName;
        _PropertyOrder = source._PropertyOrder;
        _SchemaCustomizer = source._SchemaCustomizer;
        _BuiltInCustomizer = (source._BuiltInCustomizer == null) ? null : newBuiltInCustomizer(source._BuiltInCustomizer, justData);
        _NewCustomizer = source._NewCustomizer;
    }

    // This attribute is mandatory
    public void setAttributeName(java.lang.String value) {
        _AttributeName = value;
    }

    public java.lang.String getAttributeName() {
        return _AttributeName;
    }

    // This attribute is mandatory
    public void setIsNameableAttribute(boolean value) {
        _IsNameableAttribute = value;
    }

    public boolean isIsNameableAttribute() {
        return _IsNameableAttribute;
    }

    // This attribute is optional
    public void setDecoratorAttribute(javax.xml.namespace.QName value) {
        _DecoratorAttribute = value;
    }

    public javax.xml.namespace.QName getDecoratorAttribute() {
        return _DecoratorAttribute;
    }

    // This attribute is optional
    public void setGroupName(java.lang.String value) {
        _GroupName = value;
    }

    public java.lang.String getGroupName() {
        return _GroupName;
    }

    // This attribute is optional
    public void setPropertyOrder(int value) {
        _PropertyOrder = value;
    }

    public int getPropertyOrder() {
        return _PropertyOrder;
    }

    // This attribute is mandatory
    public void setSchemaCustomizer(java.lang.Boolean value) {
        _SchemaCustomizer = value;
        if (value != null) {
            // It's a mutually exclusive property.
            setBuiltInCustomizer(null);
            setNewCustomizer(null);
        }
    }

    public java.lang.Boolean getSchemaCustomizer() {
        return _SchemaCustomizer;
    }

    // This attribute is mandatory
    public void setBuiltInCustomizer(org.netbeans.modules.xml.wsdl.ui.property.model.BuiltInCustomizer value) {
        _BuiltInCustomizer = value;
        if (value != null) {
            // It's a mutually exclusive property.
            setSchemaCustomizer(null);
            setNewCustomizer(null);
        }
    }

    public org.netbeans.modules.xml.wsdl.ui.property.model.BuiltInCustomizer getBuiltInCustomizer() {
        return _BuiltInCustomizer;
    }

    // This attribute is mandatory
    public void setNewCustomizer(String value) {
        _NewCustomizer = value;
        if (value != null) {
            // It's a mutually exclusive property.
            setSchemaCustomizer(null);
            setBuiltInCustomizer(null);
        }
    }

    public String getNewCustomizer() {
        return _NewCustomizer;
    }

    /**
     * Create a new bean using it's default constructor.
     * This does not add it to any bean graph.
     */
    public org.netbeans.modules.xml.wsdl.ui.property.model.BuiltInCustomizer newBuiltInCustomizer() {
        return new org.netbeans.modules.xml.wsdl.ui.property.model.BuiltInCustomizer();
    }

    /**
     * Create a new bean, copying from another one.
     * This does not add it to any bean graph.
     */
    public org.netbeans.modules.xml.wsdl.ui.property.model.BuiltInCustomizer newBuiltInCustomizer(BuiltInCustomizer source, boolean justData) {
        return new org.netbeans.modules.xml.wsdl.ui.property.model.BuiltInCustomizer(source, justData);
    }

    public void writeNode(java.io.Writer out) throws java.io.IOException {
        String myName;
        myName = "Property";
        writeNode(out, myName, ""); // NOI18N
    }

    public void writeNode(java.io.Writer out, String nodeName, String indent) throws java.io.IOException {
        writeNode(out, nodeName, null, indent, new java.util.HashMap());
    }

    /**
     * It's not recommended to call this method directly.
     */
    public void writeNode(java.io.Writer out, String nodeName, String namespace, String indent, java.util.Map namespaceMap) throws java.io.IOException {
        out.write(indent);
        out.write("<");
        if (namespace != null) {
            out.write((String)namespaceMap.get(namespace));
            out.write(":");
        }
        out.write(nodeName);
        // Work out any namespaces.
        boolean firstNSAddition = true;
        if (_DecoratorAttribute != null && _DecoratorAttribute.getNamespaceURI() != null && !"".equals(_DecoratorAttribute.getNamespaceURI())) {
            String prefix = (String) namespaceMap.get(_DecoratorAttribute.getNamespaceURI());
            if (prefix == null || "".equals(prefix)) {
                prefix = _DecoratorAttribute.getPrefix();
                if (prefix == null || "".equals(prefix)) {
                    prefix = "decoratorAttribute_ns__";
                }
                // Need to make sure it's a unique prefix too.
                boolean changed;
                do {
                    changed = false;
                    for (java.util.Iterator valueIt = namespaceMap.values().iterator(); 
                        valueIt.hasNext(); ) {
                        String otherPrefix = (String) valueIt.next();
                        if (prefix.equals(otherPrefix)) {
                            prefix += "_";
                            changed = true;
                        }
                    }
                } while (changed);
                if (firstNSAddition) {
                    firstNSAddition = false;
                    // Copy on write
                    namespaceMap = new java.util.HashMap(namespaceMap);
                }
                namespaceMap.put(_DecoratorAttribute.getNamespaceURI(), prefix);
                out.write(" xmlns:");
                out.write(prefix);
                out.write("='");
                out.write(_DecoratorAttribute.getNamespaceURI());
                out.write("'");
            }
        }
        writeNodeAttributes(out, nodeName, namespace, indent, namespaceMap);
        out.write(">\n");
        writeNodeChildren(out, nodeName, namespace, indent, namespaceMap);
        out.write(indent);
        out.write("</");
        if (namespace != null) {
            out.write((String)namespaceMap.get(namespace));
            out.write(":");
        }
        out.write(nodeName);
        out.write(">\n");
    }

    protected void writeNodeAttributes(java.io.Writer out, String nodeName, String namespace, String indent, java.util.Map namespaceMap) throws java.io.IOException {
        // attributeName is an attribute with namespace http://xml.netbeans.org/schema/wsdlui/property
        if (_AttributeName != null) {
            out.write(" attributeName='");
            org.netbeans.modules.xml.wsdl.ui.property.model.ElementProperties.writeXML(out, _AttributeName, true);
            out.write("'"); // NOI18N
        }
        // isNameableAttribute is an attribute with namespace http://xml.netbeans.org/schema/wsdlui/property
        out.write(" isNameableAttribute='");
        out.write(_IsNameableAttribute ? "true" : "false");
        out.write("'"); // NOI18N
        // decoratorAttribute is an attribute with namespace http://xml.netbeans.org/schema/wsdlui/property
        if (_DecoratorAttribute != null) {
            out.write(" decoratorAttribute='");
            if (_DecoratorAttribute.getNamespaceURI() != null && !"".equals(_DecoratorAttribute.getNamespaceURI())) {
                out.write((String) namespaceMap.get(_DecoratorAttribute.getNamespaceURI()));
                out.write(":");
            }
            org.netbeans.modules.xml.wsdl.ui.property.model.ElementProperties.writeXML(out, _DecoratorAttribute.getLocalPart(), true);
            out.write("'"); // NOI18N
        }
        // groupName is an attribute with namespace http://xml.netbeans.org/schema/wsdlui/property
        if (_GroupName != null) {
            out.write(" groupName='");
            org.netbeans.modules.xml.wsdl.ui.property.model.ElementProperties.writeXML(out, _GroupName, true);
            out.write("'"); // NOI18N
        }
        // propertyOrder is an attribute with namespace http://xml.netbeans.org/schema/wsdlui/property
        out.write(" propertyOrder='");
        out.write(""+_PropertyOrder);
        out.write("'"); // NOI18N
    }

    protected void writeNodeChildren(java.io.Writer out, String nodeName, String namespace, String indent, java.util.Map namespaceMap) throws java.io.IOException {
        String nextIndent = indent + "  ";
        if (_SchemaCustomizer != null) {
            out.write(nextIndent);
            out.write("<SchemaCustomizer"); // NOI18N
            out.write(">"); // NOI18N
            out.write(_SchemaCustomizer.toString());
            out.write("</SchemaCustomizer>\n"); // NOI18N
        }
        if (_BuiltInCustomizer != null) {
            _BuiltInCustomizer.writeNode(out, "BuiltInCustomizer", null, nextIndent, namespaceMap);
        }
        if (_NewCustomizer != null) {
            out.write(nextIndent);
            out.write("<NewCustomizer");    // NOI18N
            out.write(">"); // NOI18N
            org.netbeans.modules.xml.wsdl.ui.property.model.ElementProperties.writeXML(out, _NewCustomizer, false);
            out.write("</NewCustomizer>\n");    // NOI18N
        }
    }

    public void readNode(org.w3c.dom.Node node) {
        readNode(node, new java.util.HashMap());
    }

    public void readNode(org.w3c.dom.Node node, java.util.Map namespacePrefixes) {
        if (node.hasAttributes()) {
            org.w3c.dom.NamedNodeMap attrs = node.getAttributes();
            org.w3c.dom.Attr attr;
            java.lang.String attrValue;
            boolean firstNamespaceDef = true;
            for (int attrNum = 0; attrNum < attrs.getLength(); ++attrNum) {
                attr = (org.w3c.dom.Attr) attrs.item(attrNum);
                String attrName = attr.getName();
                if (attrName.startsWith("xmlns:")) {
                    if (firstNamespaceDef) {
                        firstNamespaceDef = false;
                        // Dup prefix map, so as to not write over previous values, and to make it easy to clear out our entries.
                        namespacePrefixes = new java.util.HashMap(namespacePrefixes);
                    }
                    String attrNSPrefix = attrName.substring(6, attrName.length());
                    namespacePrefixes.put(attrNSPrefix, attr.getValue());
                }
            }
            readNodeAttributes(node, namespacePrefixes, attrs);
        }
        readNodeChildren(node, namespacePrefixes);
    }

    protected void readNodeAttributes(org.w3c.dom.Node node, java.util.Map namespacePrefixes, org.w3c.dom.NamedNodeMap attrs) {
        org.w3c.dom.Attr attr;
        java.lang.String attrValue;
        attr = (org.w3c.dom.Attr) attrs.getNamedItem("attributeName");
        if (attr != null) {
            attrValue = attr.getValue();
            _AttributeName = attrValue;
        }
        attr = (org.w3c.dom.Attr) attrs.getNamedItem("isNameableAttribute");
        if (attr != null) {
            attrValue = attr.getValue();
            _IsNameableAttribute = java.lang.Boolean.valueOf(attrValue).booleanValue();
        }
        attr = (org.w3c.dom.Attr) attrs.getNamedItem("decoratorAttribute");
        if (attr != null) {
            attrValue = attr.getValue();
            int colonPos = attrValue.indexOf(':');
            if (colonPos < 0) {
                _DecoratorAttribute = new javax.xml.namespace.QName(attrValue);
            } else {
                java.util.Map nsPrefixes = new java.util.HashMap(namespacePrefixes);
                for (int attrNum = 0; attrNum < attrs.getLength(); 
                    ++attrNum) {
                    attr = (org.w3c.dom.Attr) attrs.item(attrNum);
                    String attrName = attr.getName();
                    if (attrName.startsWith("xmlns:")) {
                        String attrNSPrefix = attrName.substring(6, attrName.length());
                        nsPrefixes.put(attrNSPrefix, attr.getValue());
                    }
                }
                String prefix = attrValue.substring(0, colonPos);
                String ns = (String) nsPrefixes.get(prefix);
                String localPart = attrValue.substring(colonPos+1, attrValue.length());
                _DecoratorAttribute = new javax.xml.namespace.QName(ns, localPart, prefix);
            }
        }
        attr = (org.w3c.dom.Attr) attrs.getNamedItem("groupName");
        if (attr != null) {
            attrValue = attr.getValue();
            _GroupName = attrValue;
        }
        attr = (org.w3c.dom.Attr) attrs.getNamedItem("propertyOrder");
        if (attr != null) {
            attrValue = attr.getValue();
            _PropertyOrder = Integer.parseInt(attrValue);
        }
    }

    protected void readNodeChildren(org.w3c.dom.Node node, java.util.Map namespacePrefixes) {
        org.w3c.dom.NodeList children = node.getChildNodes();
        for (int i = 0, size = children.getLength(); i < size; ++i) {
            org.w3c.dom.Node childNode = children.item(i);
            String childNodeName = (childNode.getLocalName() == null ? childNode.getNodeName().intern() : childNode.getLocalName().intern());
            String childNodeValue = "";
            if (childNode.getFirstChild() != null) {
                childNodeValue = childNode.getFirstChild().getNodeValue();
            }
            org.w3c.dom.NamedNodeMap attrs = childNode.getAttributes();
            org.w3c.dom.Attr attr;
            java.lang.String attrValue;
            if (childNodeName == "SchemaCustomizer") {
                if (childNode.getFirstChild() == null)
                    _SchemaCustomizer = Boolean.TRUE;
                else
                    _SchemaCustomizer = java.lang.Boolean.valueOf(childNodeValue);
            }
            else if (childNodeName == "BuiltInCustomizer") {
                _BuiltInCustomizer = newBuiltInCustomizer();
                _BuiltInCustomizer.readNode(childNode, namespacePrefixes);
            }
            else if (childNodeName == "NewCustomizer") {
                _NewCustomizer = childNodeValue;
            }
            else {
                // Found extra unrecognized childNode
            }
        }
    }

    public void changePropertyByName(String name, Object value) {
        if (name == null) return;
        name = name.intern();
        if (name == "attributeName")
            setAttributeName((java.lang.String)value);
        else if (name == "isNameableAttribute")
            setIsNameableAttribute(((java.lang.Boolean)value).booleanValue());
        else if (name == "decoratorAttribute")
            setDecoratorAttribute((javax.xml.namespace.QName)value);
        else if (name == "groupName")
            setGroupName((java.lang.String)value);
        else if (name == "propertyOrder")
            setPropertyOrder(((java.lang.Integer)value).intValue());
        else if (name == "schemaCustomizer")
            setSchemaCustomizer((java.lang.Boolean)value);
        else if (name == "builtInCustomizer")
            setBuiltInCustomizer((BuiltInCustomizer)value);
        else if (name == "newCustomizer")
            setNewCustomizer((String)value);
        else
            throw new IllegalArgumentException(name+" is not a valid property name for Property");
    }

    public Object fetchPropertyByName(String name) {
        if (name == "attributeName")
            return getAttributeName();
        if (name == "isNameableAttribute")
            return (isIsNameableAttribute() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
        if (name == "decoratorAttribute")
            return getDecoratorAttribute();
        if (name == "groupName")
            return getGroupName();
        if (name == "propertyOrder")
            return new java.lang.Integer(getPropertyOrder());
        if (name == "schemaCustomizer")
            return getSchemaCustomizer();
        if (name == "builtInCustomizer")
            return getBuiltInCustomizer();
        if (name == "newCustomizer")
            return getNewCustomizer();
        throw new IllegalArgumentException(name+" is not a valid property name for Property");
    }

    public String nameSelf() {
        return "Property";
    }

    public String nameChild(Object childObj) {
        return nameChild(childObj, false, false);
    }

    /**
     * @param childObj  The child object to search for
     * @param returnSchemaName  Whether or not the schema name should be returned or the property name
     * @return null if not found
     */
    public String nameChild(Object childObj, boolean returnConstName, boolean returnSchemaName) {
        return nameChild(childObj, returnConstName, returnSchemaName, false);
    }

    /**
     * @param childObj  The child object to search for
     * @param returnSchemaName  Whether or not the schema name should be returned or the property name
     * @return null if not found
     */
    public String nameChild(Object childObj, boolean returnConstName, boolean returnSchemaName, boolean returnXPathName) {
        if (childObj instanceof BuiltInCustomizer) {
            BuiltInCustomizer child = (BuiltInCustomizer) childObj;
            if (child == _BuiltInCustomizer) {
                if (returnConstName) {
                    return BUILTINCUSTOMIZER;
                } else if (returnSchemaName) {
                    return "BuiltInCustomizer";
                } else if (returnXPathName) {
                    return "BuiltInCustomizer";
                } else {
                    return "BuiltInCustomizer";
                }
            }
        }
        if (childObj instanceof java.lang.Boolean) {
            java.lang.Boolean child = (java.lang.Boolean) childObj;
            if (((java.lang.Boolean)child).booleanValue() == _IsNameableAttribute) {
                if (returnConstName) {
                    return ISNAMEABLEATTRIBUTE;
                } else if (returnSchemaName) {
                    return "isNameableAttribute";
                } else if (returnXPathName) {
                    return "@isNameableAttribute";
                } else {
                    return "IsNameableAttribute";
                }
            }
            if (child == _SchemaCustomizer) {
                if (returnConstName) {
                    return SCHEMACUSTOMIZER;
                } else if (returnSchemaName) {
                    return "SchemaCustomizer";
                } else if (returnXPathName) {
                    return "SchemaCustomizer";
                } else {
                    return "SchemaCustomizer";
                }
            }
        }
        if (childObj instanceof javax.xml.namespace.QName) {
            javax.xml.namespace.QName child = (javax.xml.namespace.QName) childObj;
            if (child == _DecoratorAttribute) {
                if (returnConstName) {
                    return DECORATORATTRIBUTE;
                } else if (returnSchemaName) {
                    return "decoratorAttribute";
                } else if (returnXPathName) {
                    return "@decoratorAttribute";
                } else {
                    return "DecoratorAttribute";
                }
            }
        }
        if (childObj instanceof java.lang.Integer) {
            java.lang.Integer child = (java.lang.Integer) childObj;
            if (((java.lang.Integer)child).intValue() == _PropertyOrder) {
                if (returnConstName) {
                    return PROPERTYORDER;
                } else if (returnSchemaName) {
                    return "propertyOrder";
                } else if (returnXPathName) {
                    return "@propertyOrder";
                } else {
                    return "PropertyOrder";
                }
            }
        }
        if (childObj instanceof java.lang.String) {
            java.lang.String child = (java.lang.String) childObj;
            if (child == _AttributeName) {
                if (returnConstName) {
                    return ATTRIBUTENAME;
                } else if (returnSchemaName) {
                    return "attributeName";
                } else if (returnXPathName) {
                    return "@attributeName";
                } else {
                    return "AttributeName";
                }
            }
            if (child == _GroupName) {
                if (returnConstName) {
                    return GROUPNAME;
                } else if (returnSchemaName) {
                    return "groupName";
                } else if (returnXPathName) {
                    return "@groupName";
                } else {
                    return "GroupName";
                }
            }
            if (child == _NewCustomizer) {
                if (returnConstName) {
                    return NEWCUSTOMIZER;
                } else if (returnSchemaName) {
                    return "NewCustomizer";
                } else if (returnXPathName) {
                    return "NewCustomizer";
                } else {
                    return "NewCustomizer";
                }
            }
        }
        return null;
    }

    /**
     * Return an array of all of the properties that are beans and are set.
     */
    public java.lang.Object[] childBeans(boolean recursive) {
        java.util.List children = new java.util.LinkedList();
        childBeans(recursive, children);
        java.lang.Object[] result = new java.lang.Object[children.size()];
        return (java.lang.Object[]) children.toArray(result);
    }

    /**
     * Put all child beans into the beans list.
     */
    public void childBeans(boolean recursive, java.util.List beans) {
        if (_BuiltInCustomizer != null) {
            if (recursive) {
                _BuiltInCustomizer.childBeans(true, beans);
            }
            beans.add(_BuiltInCustomizer);
        }
    }

    public boolean equals(Object o) {
        return o instanceof org.netbeans.modules.xml.wsdl.ui.property.model.Property && equals((org.netbeans.modules.xml.wsdl.ui.property.model.Property) o);
    }

    public boolean equals(org.netbeans.modules.xml.wsdl.ui.property.model.Property inst) {
        if (inst == this) {
            return true;
        }
        if (inst == null) {
            return false;
        }
        if (!(_AttributeName == null ? inst._AttributeName == null : _AttributeName.equals(inst._AttributeName))) {
            return false;
        }
        if (!(_IsNameableAttribute == inst._IsNameableAttribute)) {
            return false;
        }
        if (!(_DecoratorAttribute == null ? inst._DecoratorAttribute == null : _DecoratorAttribute.equals(inst._DecoratorAttribute))) {
            return false;
        }
        if (!(_GroupName == null ? inst._GroupName == null : _GroupName.equals(inst._GroupName))) {
            return false;
        }
        if (!(_PropertyOrder == inst._PropertyOrder)) {
            return false;
        }
        if (!(_SchemaCustomizer == null ? inst._SchemaCustomizer == null : _SchemaCustomizer.equals(inst._SchemaCustomizer))) {
            return false;
        }
        if (!(_BuiltInCustomizer == null ? inst._BuiltInCustomizer == null : _BuiltInCustomizer.equals(inst._BuiltInCustomizer))) {
            return false;
        }
        if (!(_NewCustomizer == null ? inst._NewCustomizer == null : _NewCustomizer.equals(inst._NewCustomizer))) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int result = 17;
        result = 37*result + (_AttributeName == null ? 0 : _AttributeName.hashCode());
        result = 37*result + (_IsNameableAttribute ? 0 : 1);
        result = 37*result + (_DecoratorAttribute == null ? 0 : _DecoratorAttribute.hashCode());
        result = 37*result + (_GroupName == null ? 0 : _GroupName.hashCode());
        result = 37*result + (_PropertyOrder);
        result = 37*result + (_SchemaCustomizer == null ? 0 : _SchemaCustomizer.hashCode());
        result = 37*result + (_BuiltInCustomizer == null ? 0 : _BuiltInCustomizer.hashCode());
        result = 37*result + (_NewCustomizer == null ? 0 : _NewCustomizer.hashCode());
        return result;
    }

}


/*
        The following schema file has been used for generation:

<?xml version="1.0" encoding="UTF-8"?>

<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"
            targetNamespace="http://xml.netbeans.org/schema/wsdlui/property"
            xmlns:tns="http://xml.netbeans.org/schema/wsdlui/property"
            elementFormDefault="qualified">
    <xsd:element name="ElementProperties">
        <xsd:annotation>
            <xsd:documentation xml:lang="en-US">Root node for specifying customizers for a element.
                This needs to be on the GlobalElement which would represent the node in the WSDL tree.
            If this is defined in local elements it is ignored.</xsd:documentation>
        </xsd:annotation>
        <xsd:complexType>
            <xsd:sequence>
                <xsd:element ref="tns:PropertyGroup" maxOccurs="unbounded" />
                <xsd:element ref="tns:Property" maxOccurs="unbounded" />
                <xsd:element ref="tns:GroupedProperty" maxOccurs="unbounded" />
            </xsd:sequence>
        </xsd:complexType>
    </xsd:element>            
    
    <xsd:element name="PropertyGroup">
        <xsd:annotation>
            <xsd:documentation xml:lang="en-US">Used to create groups in the property sheet. 
                By default, if no groups are defined all the properties will be shown 
                in the default Property sheet called "Properties".
                name : defines the name of the Group.
                groupOrder : defines the order in which the groups will be created. The groupOrder starts with 1.
                isDefault : overrides the default property sheet to be this group rather than "Properties".
                This enables the user to put non-customized properties (which do not have a Property defined in this xml) to go into this property sheet.
                
                
            </xsd:documentation>
        </xsd:annotation>
        <xsd:complexType>
            <xsd:attribute name="name" type="xsd:string" use="required"/>
            <xsd:attribute name="groupOrder" type="xsd:int"/>
            <xsd:attribute name="isDefault" type="xsd:boolean" default="false"/>
        </xsd:complexType>
    </xsd:element>
    <xsd:element name="Property">
        <xsd:annotation>
            <xsd:documentation xml:lang="en-US">Property represents each attribute that would be created for the Node in the wsdleditor tree.
                It defines a way to specify customizers for attributes. 
                There are 3 types of Property customizers:
                SchemaCustomizer : The default Customizer is the SchemaCustomizer, which shows drop downs for enumerations and boolean attributes,
                and String customizer for all other types. So if there is no Property defined for a attribute, it will have 
                SchemaCustomizer.
                BuiltInCustomizer : specifies a way to put already defined customizer to be shown. Examples are part chooser, message chooser etc.
                NewCustomizer : provides a way to create a custom customizer specific to the user requirement.  When using this the developer has
                to implement the SPI org.netbeans.modules.xml.wsdl.ui.spi.WSDLLookupProvider, and add a implementation of 
                org.netbeans.modules.xml.wsdl.ui.spi.NewCustomizerProvider, which will provide the custom Node.Property to be shown in the 
                wsdl editor property sheet.
            </xsd:documentation>
        </xsd:annotation>
        <xsd:complexType>
            <xsd:choice>
                <xsd:element name="SchemaCustomizer"/>
                <xsd:element name="BuiltInCustomizer">
                    <xsd:complexType xmlns:xsd="http://www.w3.org/2001/XMLSchema">
                        <xsd:choice>
                            <xsd:element name="DependsOnCustomizer">
                                <xsd:annotation>
                                    <xsd:documentation xml:lang="en-US">Use a built-in customizer whose value(s) depend on some other attribute 
                                        of the same element or some other source.                                        
                                    </xsd:documentation>
                                </xsd:annotation>
                                <xsd:complexType>
                                    <xsd:choice>
                                        <xsd:element name="StaticCustomizer">
                                            <xsd:annotation>
                                                <xsd:documentation xml:lang="en-US">dependsOnAttributeName :  the attribute on which the value(s) of the chooser would depend on.
                                                    For example: some elements may have a attribute for message and another for part, and the PartsChooser should show parts from the message that is selected in the message attribute.
                                                    In that the dependsOnAttributeName for PartChooser would be message.
                                                </xsd:documentation>
                                            </xsd:annotation>
                                            <xsd:complexType>
                                                <xsd:attribute name="dependsOnAttributeName" type="xsd:QName"/>
                                            </xsd:complexType>
                                        </xsd:element>
                                        <!--No use case as of yet, xsd:element name="DynamicCustomizer">
                                            <xsd:annotation>
                                                <xsd:documentation xml:lang="en-US">
                                                    
                                                </xsd:documentation>
                                            </xsd:annotation>
                                            <xsd:complexType>
                                                <xsd:attribute name="dependsOnAttributeValueType" type="xsd:string"/>
                                                <xsd:attribute name="attributeValueProviderClass" type="xsd:string"/>
                                            </xsd:complexType>
                                        </xsd:element-->
                                    </xsd:choice>
                                    <xsd:attribute name="name" type="tns:builtInCustomizerTypes"/>
                                </xsd:complexType>
                            </xsd:element>
                            <xsd:element name="SimpleCustomizer">
                                <xsd:annotation>
                                    <xsd:documentation xml:lang="en-US">
                                        Use the builtin chooser that are available (the names are defined under builtInCustomizerTypes simple type as enumerations, 
                                        name: specifies which builtin chooser to use.
                                    </xsd:documentation>
                                </xsd:annotation>
                                <xsd:complexType xmlns:xsd="http://www.w3.org/2001/XMLSchema">
                                    <xsd:sequence/>
                                    <xsd:attribute name="name" type="tns:builtInCustomizerTypes"/>
                                </xsd:complexType>
                            </xsd:element>
                        </xsd:choice>
                    </xsd:complexType>
                </xsd:element>
                <xsd:element ref="tns:NewCustomizer"/>
            </xsd:choice>
            <xsd:attribute name="attributeName" type="xsd:string" use="required"/>
            <xsd:attribute name="isNameableAttribute" type="xsd:boolean" default="false"/>
            <xsd:attribute name="decoratorAttribute" type="xsd:QName"/>
            <xsd:attribute name="groupName" type="xsd:string"/>
            <xsd:attribute name="propertyOrder" type="xsd:int"/>
        </xsd:complexType>
    </xsd:element>
    <xsd:element name="GroupedProperty">
        <xsd:annotation>
            <xsd:documentation xml:lang="en-US">Some attributes in a element are mutually exclusive, so in the UI, for unambiguous usage, the user may want to add a single property chooser for 2 or more attributes, which will set the appropriate attribute depending on some criteria that the customizer may determine.
                groupedAttributeNames : specify all the mutually exclusive attributes. There will be a single customizer for all these attributes.
                groupName : specifies which PropertyGroup this belongs to.
                propertyOrder : specifies the order in the PropertyGroup where this property would be placed.
                displayName: specifies the Display name of the combined chooser.
            </xsd:documentation>
        </xsd:annotation>
        <xsd:complexType>
            <xsd:choice>
                <xsd:element name="BuiltInCustomizer" >
                    <xsd:annotation>
                        <xsd:documentation xml:lang="en-US">To use pre-built customizers.
                        </xsd:documentation>
                    </xsd:annotation>
                    <xsd:complexType>
                        <xsd:choice>
                            <xsd:element name="ElementOrTypeChooser">
                                <xsd:annotation>
                                    <xsd:documentation xml:lang="en-US">Shows a Tree based selector, which shows all the elements/types from Inline/Imported schemas.
                                        elementAttributeName : the attribute on which GlobalElement data type would be set.
                                        typeAttributeName : the attribute on which GlobalType data type would be set.
                                    </xsd:documentation>
                                </xsd:annotation>
                                <xsd:complexType>
                                    <xsd:attribute name="elementAttributeName" type="xsd:NCName"/>
                                    <xsd:attribute name="typeAttributeName" type="xsd:NCName"/>
                                </xsd:complexType>
                            </xsd:element>
                            <xsd:element name="ElementOrTypeOrMessagePartChooser">
                                <xsd:annotation>
                                    <xsd:documentation xml:lang="en-US">Shows a Tree based selector, which shows all the elements/types from Inline/Imported schemas and also the messages from all imported and existing wsdls.
                                        elementAttributeName : the attribute on which GlobalElement data type would be set.
                                        typeAttributeName : the attribute on which GlobalType data type would be set.
                                        messageAttributeName : the attribute on which Message data type would be set.
                                        partAttributeName : the attribute on which part would be set.
                                        This chooser can select between a GlobalElement or GlobalType or a wsdl Part.
                                        
                                    </xsd:documentation>
                                </xsd:annotation>                                
                                <xsd:complexType>
                                    <xsd:attribute name="elementAttributeName" type="xsd:NCName"/>
                                    <xsd:attribute name="typeAttributeName" type="xsd:NCName"/>
                                    <xsd:attribute name="messageAttributeName" type="xsd:NCName"/>
                                    <xsd:attribute name="partAttributeName" type="xsd:NCName"/>
                                </xsd:complexType>
                            </xsd:element>
                        </xsd:choice>
                    </xsd:complexType>
                </xsd:element>
                <xsd:element ref="tns:NewCustomizer"/>
            </xsd:choice>
            <xsd:attribute name="groupedAttributeNames" type="tns:attributeList" use="required"/>
            <xsd:attribute name="groupName" type="xsd:string"/>
            <xsd:attribute name="propertyOrder" type="xsd:int"/>
            <xsd:attribute name="displayName" type="xsd:NCName" use="required"/>
        </xsd:complexType>
    </xsd:element>
    
    
    <xsd:element name="NewCustomizer">
        <xsd:annotation>
            <xsd:documentation xml:lang="en-US">Provides a way for developer to provide a custom property customizer for the attribute, if the builtin chooser dont satisfy their requirements.
When using this the developer has to implement the SPI org.netbeans.modules.xml.wsdl.ui.spi.WSDLLookupProvider, and add a implementation of org.netbeans.modules.xml.wsdl.ui.spi.NewCustomizerProvider, which will provide the custom Node.Property to be shown in the wsdl editor property sheet.
            </xsd:documentation>
        </xsd:annotation>
    </xsd:element>
    
    
    <xsd:simpleType name="builtInCustomizerTypes">
        <xsd:restriction base="xsd:string">
            <xsd:enumeration value="MessageChooser">
                <xsd:annotation>
                    <xsd:documentation xml:lang="en-US">Shows a drop down of all messages in the current WSDL document and also ones in imported WSDL documents.</xsd:documentation>
                </xsd:annotation>
            </xsd:enumeration>
            <xsd:enumeration value="PartChooser">
                <xsd:annotation>
                    <xsd:documentation xml:lang="en-US">Show a drop down of all parts for a message. By default, the chooser assumes that it is in the binding section under input/output/fault, and shows all the parts for the message selected in the input/output/fault.
    If not, then the dependsOnCustomizer needs to be used to specify the attribute which represents the message, whose parts will be shown</xsd:documentation>
                </xsd:annotation>
            </xsd:enumeration>
            <xsd:enumeration value="PortTypeChooser">
                <xsd:annotation>
                    <xsd:documentation xml:lang="en-US">Show a drop down of all port types in the WSDL Document/Imported WSDL Documents.</xsd:documentation>
                </xsd:annotation>
            </xsd:enumeration>
            <xsd:enumeration value="PartsChooser">
                <xsd:annotation>
                    <xsd:documentation xml:lang="en-US">Show a dialog of all parts for a message, from which multiple parts can be selected. By default, the chooser assumes that it is in the binding section under input/output/fault, and shows all the parts for the message selected in the input/output/fault.
    If not, then the dependsOnCustomizer needs to be used to specify the attribute which represents the message, whose parts will be shown</xsd:documentation>
                </xsd:annotation>
            </xsd:enumeration>
        </xsd:restriction>
    </xsd:simpleType>
    
    <xsd:simpleType name="attributeList">
        <xsd:list itemType="xsd:string"/>
    </xsd:simpleType>
    
</xsd:schema>

*/
