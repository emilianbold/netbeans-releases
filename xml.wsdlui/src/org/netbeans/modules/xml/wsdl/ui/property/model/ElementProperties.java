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
 *  This generated bean class ElementProperties
 *  matches the schema element 'ElementProperties'.
 *
 *  ===============================================================
 *  Root node for specifying customizers for a element.
 *                  This needs to be on the GlobalElement which would represent the node in the WSDL tree.
 *              If this is defined in local elements it is ignored.
 *  ===============================================================
 *  Generated on Mon Feb 05 17:54:51 PST 2007
 *
 *  This class matches the root element of the XML Schema,
 *  and is the root of the bean graph.
 *
 *  elementProperties <ElementProperties> : ElementProperties
 *      propertyGroup <PropertyGroup> : PropertyGroup[1,n]
 *          [attr: name CDATA #REQUIRED  : java.lang.String]
 *          [attr: groupOrder CDATA #IMPLIED  : int]
 *          [attr: isDefault CDATA #IMPLIED false : boolean]
 *      property <Property> : Property[1,n]
 *          [attr: attributeName CDATA #REQUIRED  : java.lang.String]
 *          [attr: isNameableAttribute CDATA #IMPLIED false : boolean]
 *          [attr: decoratorAttribute CDATA #IMPLIED  : javax.xml.namespace.QName]
 *          [attr: groupName CDATA #IMPLIED  : java.lang.String]
 *          [attr: propertyOrder CDATA #IMPLIED  : int]
 *          | schemaCustomizer <SchemaCustomizer> : java.lang.Boolean
 *          | builtInCustomizer <BuiltInCustomizer> : BuiltInCustomizer
 *          |   | dependsOnCustomizer <DependsOnCustomizer> : DependsOnCustomizer
 *          |   |   [attr: name CDATA #IMPLIED MessageChooser : java.lang.String]   [enumeration (MessageChooser), enumeration (PartChooser), enumeration (PortTypeChooser), enumeration (PartsChooser)]
 *          |   |   staticCustomizer <StaticCustomizer> : StaticCustomizer
 *          |   |       [attr: dependsOnAttributeName CDATA #IMPLIED  : javax.xml.namespace.QName]
 *          |   | simpleCustomizer <SimpleCustomizer> : SimpleCustomizer
 *          |   |   [attr: name CDATA #IMPLIED MessageChooser : java.lang.String]   [enumeration (MessageChooser), enumeration (PartChooser), enumeration (PortTypeChooser), enumeration (PartsChooser)]
 *          | newCustomizer <NewCustomizer> : String
 *      groupedProperty <GroupedProperty> : GroupedProperty[1,n]
 *          [attr: groupedAttributeNames CDATA #REQUIRED ]
 *          [attr: groupName CDATA #IMPLIED  : java.lang.String]
 *          [attr: propertyOrder CDATA #IMPLIED  : int]
 *          [attr: displayName CDATA #REQUIRED  : java.lang.String]     [whiteSpace (collapse)]
 *          | builtInCustomizer <BuiltInCustomizer> : BuiltInCustomizerGroupedProperty
 *          |   | elementOrTypeChooser <ElementOrTypeChooser> : ElementOrTypeChooser
 *          |   |   [attr: elementAttributeName CDATA #IMPLIED  : java.lang.String]     [whiteSpace (collapse)]
 *          |   |   [attr: typeAttributeName CDATA #IMPLIED  : java.lang.String]    [whiteSpace (collapse)]
 *          |   | elementOrTypeOrMessagePartChooser <ElementOrTypeOrMessagePartChooser> : ElementOrTypeOrMessagePartChooser
 *          |   |   [attr: elementAttributeName CDATA #IMPLIED  : java.lang.String]     [whiteSpace (collapse)]
 *          |   |   [attr: typeAttributeName CDATA #IMPLIED  : java.lang.String]    [whiteSpace (collapse)]
 *          |   |   [attr: messageAttributeName CDATA #IMPLIED  : java.lang.String]     [whiteSpace (collapse)]
 *          |   |   [attr: partAttributeName CDATA #IMPLIED  : java.lang.String]    [whiteSpace (collapse)]
 *          | newCustomizer <NewCustomizer> : String
 *
 * @Generated
 */

package org.netbeans.modules.xml.wsdl.ui.property.model;

public class ElementProperties {
    public static final String PROPERTYGROUP = "PropertyGroup"; // NOI18N
    public static final String PROPERTY = "Property";   // NOI18N
    public static final String GROUPEDPROPERTY = "GroupedProperty"; // NOI18N

    private java.util.List _PropertyGroup = new java.util.ArrayList();  // List<PropertyGroup>
    private java.util.List _Property = new java.util.ArrayList();   // List<Property>
    private java.util.List _GroupedProperty = new java.util.ArrayList();    // List<GroupedProperty>
    private java.lang.String schemaLocation;

    /**
     * Normal starting point constructor.
     */
    public ElementProperties() {
    }

    /**
     * Required parameters constructor
     */
    public ElementProperties(org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup[] propertyGroup, org.netbeans.modules.xml.wsdl.ui.property.model.Property[] property, org.netbeans.modules.xml.wsdl.ui.property.model.GroupedProperty[] groupedProperty) {
        if (propertyGroup!= null) {
            ((java.util.ArrayList) _PropertyGroup).ensureCapacity(propertyGroup.length);
            for (int i = 0; i < propertyGroup.length; ++i) {
                _PropertyGroup.add(propertyGroup[i]);
            }
        }
        if (property!= null) {
            ((java.util.ArrayList) _Property).ensureCapacity(property.length);
            for (int i = 0; i < property.length; ++i) {
                _Property.add(property[i]);
            }
        }
        if (groupedProperty!= null) {
            ((java.util.ArrayList) _GroupedProperty).ensureCapacity(groupedProperty.length);
            for (int i = 0; i < groupedProperty.length; ++i) {
                _GroupedProperty.add(groupedProperty[i]);
            }
        }
    }

    /**
     * Deep copy
     */
    public ElementProperties(org.netbeans.modules.xml.wsdl.ui.property.model.ElementProperties source) {
        this(source, false);
    }

    /**
     * Deep copy
     * @param justData just copy the XML relevant data
     */
    public ElementProperties(org.netbeans.modules.xml.wsdl.ui.property.model.ElementProperties source, boolean justData) {
        for (java.util.Iterator it = source._PropertyGroup.iterator(); 
            it.hasNext(); ) {
            org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup srcElement = (org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup)it.next();
            _PropertyGroup.add((srcElement == null) ? null : newPropertyGroup(srcElement, justData));
        }
        for (java.util.Iterator it = source._Property.iterator(); 
            it.hasNext(); ) {
            org.netbeans.modules.xml.wsdl.ui.property.model.Property srcElement = (org.netbeans.modules.xml.wsdl.ui.property.model.Property)it.next();
            _Property.add((srcElement == null) ? null : newProperty(srcElement, justData));
        }
        for (java.util.Iterator it = source._GroupedProperty.iterator(); 
            it.hasNext(); ) {
            org.netbeans.modules.xml.wsdl.ui.property.model.GroupedProperty srcElement = (org.netbeans.modules.xml.wsdl.ui.property.model.GroupedProperty)it.next();
            _GroupedProperty.add((srcElement == null) ? null : newGroupedProperty(srcElement, justData));
        }
        schemaLocation = source.schemaLocation;
    }

    // This attribute is an array containing at least one element
    public void setPropertyGroup(org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup[] value) {
        if (value == null)
            value = new PropertyGroup[0];
        _PropertyGroup.clear();
        ((java.util.ArrayList) _PropertyGroup).ensureCapacity(value.length);
        for (int i = 0; i < value.length; ++i) {
            _PropertyGroup.add(value[i]);
        }
    }

    public void setPropertyGroup(int index, org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup value) {
        _PropertyGroup.set(index, value);
    }

    public org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup[] getPropertyGroup() {
        PropertyGroup[] arr = new PropertyGroup[_PropertyGroup.size()];
        return (PropertyGroup[]) _PropertyGroup.toArray(arr);
    }

    public java.util.List fetchPropertyGroupList() {
        return _PropertyGroup;
    }

    public org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup getPropertyGroup(int index) {
        return (PropertyGroup)_PropertyGroup.get(index);
    }

    // Return the number of propertyGroup
    public int sizePropertyGroup() {
        return _PropertyGroup.size();
    }

    public int addPropertyGroup(org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup value) {
        _PropertyGroup.add(value);
        int positionOfNewItem = _PropertyGroup.size()-1;
        return positionOfNewItem;
    }

    /**
     * Search from the end looking for @param value, and then remove it.
     */
    public int removePropertyGroup(org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup value) {
        int pos = _PropertyGroup.indexOf(value);
        if (pos >= 0) {
            _PropertyGroup.remove(pos);
        }
        return pos;
    }

    // This attribute is an array containing at least one element
    public void setProperty(org.netbeans.modules.xml.wsdl.ui.property.model.Property[] value) {
        if (value == null)
            value = new Property[0];
        _Property.clear();
        ((java.util.ArrayList) _Property).ensureCapacity(value.length);
        for (int i = 0; i < value.length; ++i) {
            _Property.add(value[i]);
        }
    }

    public void setProperty(int index, org.netbeans.modules.xml.wsdl.ui.property.model.Property value) {
        _Property.set(index, value);
    }

    public org.netbeans.modules.xml.wsdl.ui.property.model.Property[] getProperty() {
        Property[] arr = new Property[_Property.size()];
        return (Property[]) _Property.toArray(arr);
    }

    public java.util.List fetchPropertyList() {
        return _Property;
    }

    public org.netbeans.modules.xml.wsdl.ui.property.model.Property getProperty(int index) {
        return (Property)_Property.get(index);
    }

    // Return the number of property
    public int sizeProperty() {
        return _Property.size();
    }

    public int addProperty(org.netbeans.modules.xml.wsdl.ui.property.model.Property value) {
        _Property.add(value);
        int positionOfNewItem = _Property.size()-1;
        return positionOfNewItem;
    }

    /**
     * Search from the end looking for @param value, and then remove it.
     */
    public int removeProperty(org.netbeans.modules.xml.wsdl.ui.property.model.Property value) {
        int pos = _Property.indexOf(value);
        if (pos >= 0) {
            _Property.remove(pos);
        }
        return pos;
    }

    // This attribute is an array containing at least one element
    public void setGroupedProperty(org.netbeans.modules.xml.wsdl.ui.property.model.GroupedProperty[] value) {
        if (value == null)
            value = new GroupedProperty[0];
        _GroupedProperty.clear();
        ((java.util.ArrayList) _GroupedProperty).ensureCapacity(value.length);
        for (int i = 0; i < value.length; ++i) {
            _GroupedProperty.add(value[i]);
        }
    }

    public void setGroupedProperty(int index, org.netbeans.modules.xml.wsdl.ui.property.model.GroupedProperty value) {
        _GroupedProperty.set(index, value);
    }

    public org.netbeans.modules.xml.wsdl.ui.property.model.GroupedProperty[] getGroupedProperty() {
        GroupedProperty[] arr = new GroupedProperty[_GroupedProperty.size()];
        return (GroupedProperty[]) _GroupedProperty.toArray(arr);
    }

    public java.util.List fetchGroupedPropertyList() {
        return _GroupedProperty;
    }

    public org.netbeans.modules.xml.wsdl.ui.property.model.GroupedProperty getGroupedProperty(int index) {
        return (GroupedProperty)_GroupedProperty.get(index);
    }

    // Return the number of groupedProperty
    public int sizeGroupedProperty() {
        return _GroupedProperty.size();
    }

    public int addGroupedProperty(org.netbeans.modules.xml.wsdl.ui.property.model.GroupedProperty value) {
        _GroupedProperty.add(value);
        int positionOfNewItem = _GroupedProperty.size()-1;
        return positionOfNewItem;
    }

    /**
     * Search from the end looking for @param value, and then remove it.
     */
    public int removeGroupedProperty(org.netbeans.modules.xml.wsdl.ui.property.model.GroupedProperty value) {
        int pos = _GroupedProperty.indexOf(value);
        if (pos >= 0) {
            _GroupedProperty.remove(pos);
        }
        return pos;
    }

    public void _setSchemaLocation(String location) {
        schemaLocation = location;
    }

    public String _getSchemaLocation() {
        return schemaLocation;
    }

    /**
     * Create a new bean using it's default constructor.
     * This does not add it to any bean graph.
     */
    public org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup newPropertyGroup() {
        return new org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup();
    }

    /**
     * Create a new bean, copying from another one.
     * This does not add it to any bean graph.
     */
    public org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup newPropertyGroup(PropertyGroup source, boolean justData) {
        return new org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup(source, justData);
    }

    /**
     * Create a new bean using it's default constructor.
     * This does not add it to any bean graph.
     */
    public org.netbeans.modules.xml.wsdl.ui.property.model.Property newProperty() {
        return new org.netbeans.modules.xml.wsdl.ui.property.model.Property();
    }

    /**
     * Create a new bean, copying from another one.
     * This does not add it to any bean graph.
     */
    public org.netbeans.modules.xml.wsdl.ui.property.model.Property newProperty(Property source, boolean justData) {
        return new org.netbeans.modules.xml.wsdl.ui.property.model.Property(source, justData);
    }

    /**
     * Create a new bean using it's default constructor.
     * This does not add it to any bean graph.
     */
    public org.netbeans.modules.xml.wsdl.ui.property.model.GroupedProperty newGroupedProperty() {
        return new org.netbeans.modules.xml.wsdl.ui.property.model.GroupedProperty();
    }

    /**
     * Create a new bean, copying from another one.
     * This does not add it to any bean graph.
     */
    public org.netbeans.modules.xml.wsdl.ui.property.model.GroupedProperty newGroupedProperty(GroupedProperty source, boolean justData) {
        return new org.netbeans.modules.xml.wsdl.ui.property.model.GroupedProperty(source, justData);
    }

    public void write(org.openide.filesystems.FileObject fo) throws java.io.IOException {
        org.openide.filesystems.FileLock lock = fo.lock();
        try {
            java.io.OutputStream out = fo.getOutputStream(lock);
            write(out);
            out.close();
        } finally {
            lock.releaseLock();
        }
    }

    public void write(org.openide.filesystems.FileObject dir, String filename) throws java.io.IOException {
        org.openide.filesystems.FileObject file = dir.getFileObject(filename);
        if (file == null) {
            file = dir.createData(filename);
        }
        write(file);
    }

    public void write(java.io.File f) throws java.io.IOException {
        java.io.OutputStream out = new java.io.FileOutputStream(f);
        try {
            write(out);
        } finally {
            out.close();
        }
    }

    public void write(java.io.OutputStream out) throws java.io.IOException {
        write(out, null);
    }

    public void write(java.io.OutputStream out, String encoding) throws java.io.IOException {
        java.io.Writer w;
        if (encoding == null) {
            encoding = "UTF-8"; // NOI18N
        }
        w = new java.io.BufferedWriter(new java.io.OutputStreamWriter(out, encoding));
        write(w, encoding);
        w.flush();
    }

    /**
     * Print this Java Bean to @param out including an XML header.
     * @param encoding is the encoding style that @param out was opened with.
     */
    public void write(java.io.Writer out, String encoding) throws java.io.IOException {
        out.write("<?xml version='1.0'");   // NOI18N
        if (encoding != null)
            out.write(" encoding='"+encoding+"'");  // NOI18N
        out.write(" ?>\n"); // NOI18N
        writeNode(out, "ElementProperties", "");    // NOI18N
    }

    public void writeNode(java.io.Writer out) throws java.io.IOException {
        String myName;
        myName = "ElementProperties";
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
        out.write(" xmlns='");  // NOI18N
        out.write("http://xml.netbeans.org/schema/wsdlui/property");    // NOI18N
        out.write("'"); // NOI18N
        if (schemaLocation != null) {
            namespaceMap.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");
            out.write(" xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='");
            out.write(schemaLocation);
            out.write("'"); // NOI18N
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
    }

    protected void writeNodeChildren(java.io.Writer out, String nodeName, String namespace, String indent, java.util.Map namespaceMap) throws java.io.IOException {
        String nextIndent = indent + "  ";
        for (java.util.Iterator it = _PropertyGroup.iterator(); 
            it.hasNext(); ) {
            org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup element = (org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup)it.next();
            if (element != null) {
                element.writeNode(out, "PropertyGroup", null, nextIndent, namespaceMap);
            }
        }
        for (java.util.Iterator it = _Property.iterator(); it.hasNext(); ) {
            org.netbeans.modules.xml.wsdl.ui.property.model.Property element = (org.netbeans.modules.xml.wsdl.ui.property.model.Property)it.next();
            if (element != null) {
                element.writeNode(out, "Property", null, nextIndent, namespaceMap);
            }
        }
        for (java.util.Iterator it = _GroupedProperty.iterator(); 
            it.hasNext(); ) {
            org.netbeans.modules.xml.wsdl.ui.property.model.GroupedProperty element = (org.netbeans.modules.xml.wsdl.ui.property.model.GroupedProperty)it.next();
            if (element != null) {
                element.writeNode(out, "GroupedProperty", null, nextIndent, namespaceMap);
            }
        }
    }

    public static ElementProperties read(org.openide.filesystems.FileObject fo) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
        java.io.InputStream in = fo.getInputStream();
        try {
            return read(in);
        } finally {
            in.close();
        }
    }

    public static ElementProperties read(java.io.File f) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
        java.io.InputStream in = new java.io.FileInputStream(f);
        try {
            return read(in);
        } finally {
            in.close();
        }
    }

    public static ElementProperties read(java.io.InputStream in) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
        return read(new org.xml.sax.InputSource(in), false, null, null);
    }

    /**
     * Warning: in readNoEntityResolver character and entity references will
     * not be read from any DTD in the XML source.
     * However, this way is faster since no DTDs are looked up
     * (possibly skipping network access) or parsed.
     */
    public static ElementProperties readNoEntityResolver(java.io.InputStream in) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
        return read(new org.xml.sax.InputSource(in), false,
            new org.xml.sax.EntityResolver() {
            public org.xml.sax.InputSource resolveEntity(String publicId, String systemId) {
                java.io.ByteArrayInputStream bin = new java.io.ByteArrayInputStream(new byte[0]);
                return new org.xml.sax.InputSource(bin);
            }
        }
            , null);
    }

    public static ElementProperties read(org.xml.sax.InputSource in, boolean validate, org.xml.sax.EntityResolver er, org.xml.sax.ErrorHandler eh) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf.setValidating(validate);
        dbf.setNamespaceAware(true);
        javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
        if (er != null) db.setEntityResolver(er);
        if (eh != null) db.setErrorHandler(eh);
        org.w3c.dom.Document doc = db.parse(in);
        return read(doc);
    }

    public static ElementProperties read(org.w3c.dom.Document document) {
        ElementProperties aElementProperties = new ElementProperties();
        aElementProperties.readFromDocument(document);
        return aElementProperties;
    }

    protected void readFromDocument(org.w3c.dom.Document document) {
        readNode(document.getDocumentElement());
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
            String xsiPrefix = "xsi";
            for (java.util.Iterator it = namespacePrefixes.keySet().iterator(); 
                it.hasNext(); ) {
                String prefix = (String) it.next();
                String ns = (String) namespacePrefixes.get(prefix);
                if ("http://www.w3.org/2001/XMLSchema-instance".equals(ns)) {
                    xsiPrefix = prefix;
                    break;
                }
            }
            attr = (org.w3c.dom.Attr) attrs.getNamedItem(""+xsiPrefix+":schemaLocation");
            if (attr != null) {
                attrValue = attr.getValue();
                schemaLocation = attrValue;
            }
            readNodeAttributes(node, namespacePrefixes, attrs);
        }
        readNodeChildren(node, namespacePrefixes);
    }

    protected void readNodeAttributes(org.w3c.dom.Node node, java.util.Map namespacePrefixes, org.w3c.dom.NamedNodeMap attrs) {
        org.w3c.dom.Attr attr;
        java.lang.String attrValue;
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
            if (childNodeName == "PropertyGroup") {
                PropertyGroup aPropertyGroup = newPropertyGroup();
                aPropertyGroup.readNode(childNode, namespacePrefixes);
                _PropertyGroup.add(aPropertyGroup);
            }
            else if (childNodeName == "Property") {
                Property aProperty = newProperty();
                aProperty.readNode(childNode, namespacePrefixes);
                _Property.add(aProperty);
            }
            else if (childNodeName == "GroupedProperty") {
                GroupedProperty aGroupedProperty = newGroupedProperty();
                aGroupedProperty.readNode(childNode, namespacePrefixes);
                _GroupedProperty.add(aGroupedProperty);
            }
            else {
                // Found extra unrecognized childNode
            }
        }
    }

    /**
     * Takes some text to be printed into an XML stream and escapes any
     * characters that might make it invalid XML (like '<').
     */
    public static void writeXML(java.io.Writer out, String msg) throws java.io.IOException {
        writeXML(out, msg, true);
    }

    public static void writeXML(java.io.Writer out, String msg, boolean attribute) throws java.io.IOException {
        if (msg == null)
            return;
        int msgLength = msg.length();
        for (int i = 0; i < msgLength; ++i) {
            char c = msg.charAt(i);
            writeXML(out, c, attribute);
        }
    }

    public static void writeXML(java.io.Writer out, char msg, boolean attribute) throws java.io.IOException {
        if (msg == '&')
            out.write("&amp;");
        else if (msg == '<')
            out.write("&lt;");
        else if (msg == '>')
            out.write("&gt;");
        else if (attribute) {
            if (msg == '"')
                out.write("&quot;");
            else if (msg == '\'')
                out.write("&apos;");
            else if (msg == '\n')
                out.write("&#xA;");
            else if (msg == '\t')
                out.write("&#x9;");
            else
                out.write(msg);
        }
        else
            out.write(msg);
    }

    public void changePropertyByName(String name, Object value) {
        if (name == null) return;
        name = name.intern();
        if (name == "propertyGroup")
            addPropertyGroup((PropertyGroup)value);
        else if (name == "propertyGroup[]")
            setPropertyGroup((PropertyGroup[]) value);
        else if (name == "property")
            addProperty((Property)value);
        else if (name == "property[]")
            setProperty((Property[]) value);
        else if (name == "groupedProperty")
            addGroupedProperty((GroupedProperty)value);
        else if (name == "groupedProperty[]")
            setGroupedProperty((GroupedProperty[]) value);
        else
            throw new IllegalArgumentException(name+" is not a valid property name for ElementProperties");
    }

    public Object fetchPropertyByName(String name) {
        if (name == "propertyGroup[]")
            return getPropertyGroup();
        if (name == "property[]")
            return getProperty();
        if (name == "groupedProperty[]")
            return getGroupedProperty();
        throw new IllegalArgumentException(name+" is not a valid property name for ElementProperties");
    }

    public String nameSelf() {
        return "/ElementProperties";
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
        if (childObj instanceof GroupedProperty) {
            GroupedProperty child = (GroupedProperty) childObj;
            int index = 0;
            for (java.util.Iterator it = _GroupedProperty.iterator(); 
                it.hasNext(); ) {
                org.netbeans.modules.xml.wsdl.ui.property.model.GroupedProperty element = (org.netbeans.modules.xml.wsdl.ui.property.model.GroupedProperty)it.next();
                if (child == element) {
                    if (returnConstName) {
                        return GROUPEDPROPERTY;
                    } else if (returnSchemaName) {
                        return "GroupedProperty";
                    } else if (returnXPathName) {
                        return "GroupedProperty[position()="+index+"]";
                    } else {
                        return "GroupedProperty."+Integer.toHexString(index);
                    }
                }
                ++index;
            }
        }
        if (childObj instanceof Property) {
            Property child = (Property) childObj;
            int index = 0;
            for (java.util.Iterator it = _Property.iterator(); 
                it.hasNext(); ) {
                org.netbeans.modules.xml.wsdl.ui.property.model.Property element = (org.netbeans.modules.xml.wsdl.ui.property.model.Property)it.next();
                if (child == element) {
                    if (returnConstName) {
                        return PROPERTY;
                    } else if (returnSchemaName) {
                        return "Property";
                    } else if (returnXPathName) {
                        return "Property[position()="+index+"]";
                    } else {
                        return "Property."+Integer.toHexString(index);
                    }
                }
                ++index;
            }
        }
        if (childObj instanceof PropertyGroup) {
            PropertyGroup child = (PropertyGroup) childObj;
            int index = 0;
            for (java.util.Iterator it = _PropertyGroup.iterator(); 
                it.hasNext(); ) {
                org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup element = (org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup)it.next();
                if (child == element) {
                    if (returnConstName) {
                        return PROPERTYGROUP;
                    } else if (returnSchemaName) {
                        return "PropertyGroup";
                    } else if (returnXPathName) {
                        return "PropertyGroup[position()="+index+"]";
                    } else {
                        return "PropertyGroup."+Integer.toHexString(index);
                    }
                }
                ++index;
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
        for (java.util.Iterator it = _PropertyGroup.iterator(); 
            it.hasNext(); ) {
            org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup element = (org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup)it.next();
            if (element != null) {
                if (recursive) {
                    element.childBeans(true, beans);
                }
                beans.add(element);
            }
        }
        for (java.util.Iterator it = _Property.iterator(); it.hasNext(); ) {
            org.netbeans.modules.xml.wsdl.ui.property.model.Property element = (org.netbeans.modules.xml.wsdl.ui.property.model.Property)it.next();
            if (element != null) {
                if (recursive) {
                    element.childBeans(true, beans);
                }
                beans.add(element);
            }
        }
        for (java.util.Iterator it = _GroupedProperty.iterator(); 
            it.hasNext(); ) {
            org.netbeans.modules.xml.wsdl.ui.property.model.GroupedProperty element = (org.netbeans.modules.xml.wsdl.ui.property.model.GroupedProperty)it.next();
            if (element != null) {
                if (recursive) {
                    element.childBeans(true, beans);
                }
                beans.add(element);
            }
        }
    }

    public boolean equals(Object o) {
        return o instanceof org.netbeans.modules.xml.wsdl.ui.property.model.ElementProperties && equals((org.netbeans.modules.xml.wsdl.ui.property.model.ElementProperties) o);
    }

    public boolean equals(org.netbeans.modules.xml.wsdl.ui.property.model.ElementProperties inst) {
        if (inst == this) {
            return true;
        }
        if (inst == null) {
            return false;
        }
        if (sizePropertyGroup() != inst.sizePropertyGroup())
            return false;
        // Compare every element.
        for (java.util.Iterator it = _PropertyGroup.iterator(), it2 = inst._PropertyGroup.iterator(); 
            it.hasNext() && it2.hasNext(); ) {
            org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup element = (org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup)it.next();
            org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup element2 = (org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup)it2.next();
            if (!(element == null ? element2 == null : element.equals(element2))) {
                return false;
            }
        }
        if (sizeProperty() != inst.sizeProperty())
            return false;
        // Compare every element.
        for (java.util.Iterator it = _Property.iterator(), it2 = inst._Property.iterator(); 
            it.hasNext() && it2.hasNext(); ) {
            org.netbeans.modules.xml.wsdl.ui.property.model.Property element = (org.netbeans.modules.xml.wsdl.ui.property.model.Property)it.next();
            org.netbeans.modules.xml.wsdl.ui.property.model.Property element2 = (org.netbeans.modules.xml.wsdl.ui.property.model.Property)it2.next();
            if (!(element == null ? element2 == null : element.equals(element2))) {
                return false;
            }
        }
        if (sizeGroupedProperty() != inst.sizeGroupedProperty())
            return false;
        // Compare every element.
        for (java.util.Iterator it = _GroupedProperty.iterator(), it2 = inst._GroupedProperty.iterator(); 
            it.hasNext() && it2.hasNext(); ) {
            org.netbeans.modules.xml.wsdl.ui.property.model.GroupedProperty element = (org.netbeans.modules.xml.wsdl.ui.property.model.GroupedProperty)it.next();
            org.netbeans.modules.xml.wsdl.ui.property.model.GroupedProperty element2 = (org.netbeans.modules.xml.wsdl.ui.property.model.GroupedProperty)it2.next();
            if (!(element == null ? element2 == null : element.equals(element2))) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int result = 17;
        result = 37*result + (_PropertyGroup == null ? 0 : _PropertyGroup.hashCode());
        result = 37*result + (_Property == null ? 0 : _Property.hashCode());
        result = 37*result + (_GroupedProperty == null ? 0 : _GroupedProperty.hashCode());
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
