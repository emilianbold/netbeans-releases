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

/**
 *	This generated bean class NewCustomizer
 *	matches the schema element 'NewCustomizer'.
 *  The root bean class is ElementProperties
 *
 *	Generated on Tue Jan 30 20:45:13 PST 2007
 * @Generated
 */

package org.netbeans.modules.xml.wsdl.ui.property.model;

public class NewCustomizer {
	public static final String CLASSNAME = "ClassName";	// NOI18N

	private java.lang.String _ClassName;

	/**
	 * Normal starting point constructor.
	 */
	public NewCustomizer() {
	}

	/**
	 * Deep copy
	 */
	public NewCustomizer(org.netbeans.modules.xml.wsdl.ui.property.model.NewCustomizer source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public NewCustomizer(org.netbeans.modules.xml.wsdl.ui.property.model.NewCustomizer source, boolean justData) {
		_ClassName = source._ClassName;
	}

	// This attribute is optional
	public void setClassName(java.lang.String value) {
		_ClassName = value;
	}

	public java.lang.String getClassName() {
		return _ClassName;
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "NewCustomizer";
		writeNode(out, myName, "");	// NOI18N
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
		writeNodeAttributes(out, nodeName, namespace, indent, namespaceMap);
		writeNodeChildren(out, nodeName, namespace, indent, namespaceMap);
		out.write("/>\n");
	}

	protected void writeNodeAttributes(java.io.Writer out, String nodeName, String namespace, String indent, java.util.Map namespaceMap) throws java.io.IOException {
		// className is an attribute with namespace http://xml.netbeans.org/schema/wsdlui/property
		if (_ClassName != null) {
			out.write(" className='");
			org.netbeans.modules.xml.wsdl.ui.property.model.ElementProperties.writeXML(out, _ClassName, true);
			out.write("'");	// NOI18N
		}
	}

	protected void writeNodeChildren(java.io.Writer out, String nodeName, String namespace, String indent, java.util.Map namespaceMap) throws java.io.IOException {
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
		attr = (org.w3c.dom.Attr) attrs.getNamedItem("className");
		if (attr != null) {
			attrValue = attr.getValue();
			_ClassName = attrValue;
		}
	}

	protected void readNodeChildren(org.w3c.dom.Node node, java.util.Map namespacePrefixes) {
	}

	public void changePropertyByName(String name, Object value) {
		if (name == null) return;
		name = name.intern();
		if (name == "className")
			setClassName((java.lang.String)value);
		else
			throw new IllegalArgumentException(name+" is not a valid property name for NewCustomizer");
	}

	public Object fetchPropertyByName(String name) {
		if (name == "className")
			return getClassName();
		throw new IllegalArgumentException(name+" is not a valid property name for NewCustomizer");
	}

	public String nameSelf() {
		return "NewCustomizer";
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
		if (childObj instanceof java.lang.String) {
			java.lang.String child = (java.lang.String) childObj;
			if (child == _ClassName) {
				if (returnConstName) {
					return CLASSNAME;
				} else if (returnSchemaName) {
					return "className";
				} else if (returnXPathName) {
					return "@className";
				} else {
					return "ClassName";
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
	}

	public boolean equals(Object o) {
		return o instanceof org.netbeans.modules.xml.wsdl.ui.property.model.NewCustomizer && equals((org.netbeans.modules.xml.wsdl.ui.property.model.NewCustomizer) o);
	}

	public boolean equals(org.netbeans.modules.xml.wsdl.ui.property.model.NewCustomizer inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (!(_ClassName == null ? inst._ClassName == null : _ClassName.equals(inst._ClassName))) {
			return false;
		}
		return true;
	}

	public int hashCode() {
		int result = 17;
		result = 37*result + (_ClassName == null ? 0 : _ClassName.hashCode());
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
        <xsd:complexType>
            <xsd:sequence>
                <xsd:element ref="tns:PropertyGroup" maxOccurs="unbounded" />
                <xsd:element ref="tns:Property" maxOccurs="unbounded" />
            </xsd:sequence>
        </xsd:complexType>
    </xsd:element>            
    
    <xsd:element name="PropertyGroup">
        <xsd:complexType>
            <xsd:attribute name="name" type="xsd:string" use="required"/>
            <xsd:attribute name="groupOrder" type="xsd:int"/>
        </xsd:complexType>
    </xsd:element>
    <xsd:element name="Property">
        <xsd:complexType>
            <xsd:choice>
                <xsd:element name="SchemaCustomizer">
                </xsd:element>
                <xsd:element name="BuiltInCustomizer">
                    <xsd:complexType xmlns:xsd="http://www.w3.org/2001/XMLSchema">
                        <xsd:choice>
                            <xsd:element name="DependsOnCustomizer">
                                <xsd:complexType>
                                    <xsd:choice>
                                        <xsd:element name="StaticCustomizer">
                                            <xsd:complexType>
                                                <xsd:attribute name="dependsOnAttributeName" type="xsd:QName"/>
                                            </xsd:complexType>
                                        </xsd:element>
                                        <xsd:element name="DynamicCustomizer">
                                            <xsd:complexType>
                                                <xsd:attribute name="dependsOnAttributeValueType" type="xsd:string"/>
                                                <xsd:attribute name="attributeValueProviderClass" type="xsd:string"/>
                                            </xsd:complexType>
                                        </xsd:element>
                                    </xsd:choice>
                                    <xsd:attribute name="name" type="tns:builtInCustomizerTypes"/>
                                </xsd:complexType>
                            </xsd:element>
                            <xsd:element name="SimpleCustomizer">
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
            <xsd:attribute name="groupName" type="xsd:string"/>
            <xsd:attribute name="propertyOrder" type="xsd:int"/>
        </xsd:complexType>
    </xsd:element>
    <xsd:element name="GroupedProperty">
        <xsd:complexType>
            <xsd:choice>
                <xsd:element name="BuiltInCustomizer">
                    <xsd:complexType>
                        <xsd:attribute name="name" type="tns:GroupedAttributeBuiltInCustomizerTypes"/>
                        <xsd:attribute name="attributeValueSetterClass" type="xsd:string"/>
                    </xsd:complexType>
                </xsd:element>
                <xsd:element ref="tns:NewCustomizer"/>
            </xsd:choice>
            <xsd:attribute name="groupedAttributeNames" type="tns:attributeList"/>
            <xsd:attribute name="groupName" type="xsd:string"/>
            <xsd:attribute name="propertyOrder" type="xsd:int"/>
            <xsd:attribute name="groupDisplayName" type="xsd:string"/>
        </xsd:complexType>
    </xsd:element>
    
    <xsd:element name="NewCustomizer">
        <xsd:complexType>
            <xsd:attribute name="className" type="xsd:string"/>
        </xsd:complexType>
    </xsd:element>
                
    <xsd:simpleType name="builtInCustomizerTypes">
		<xsd:restriction base="xsd:string">
                        <xsd:enumeration value="MessageChooser"/>
			<xsd:enumeration value="PartChooser"/>
		</xsd:restriction>
    </xsd:simpleType>
    
    <xsd:simpleType name="GroupedAttributeBuiltInCustomizerTypes">
		<xsd:restriction base="xsd:string">
                        <xsd:enumeration value="ElementOrTypeChooser"/>
		</xsd:restriction>
    </xsd:simpleType>
    
    
    <xsd:simpleType name="attributeList">
        <xsd:list itemType="xsd:QName"/>
    </xsd:simpleType>
        
</xsd:schema>

*/
