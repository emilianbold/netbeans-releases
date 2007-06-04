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
 *	This generated bean class TemplateType
 *	matches the schema element 'templateType'.
 *  The root bean class is TemplateGroup
 *
 *	Generated on Thu Sep 14 11:59:06 PDT 2006
 * @Generated
 */

package org.netbeans.modules.xml.wsdl.bindingsupport.template;

public class TemplateType {
	public static final String NAME = "Name";	// NOI18N
	public static final String DEFAULT = "Default";	// NOI18N
	public static final String WSDLELEMENT = "WsdlElement";	// NOI18N

	private java.lang.String _Name;
	private boolean _Default;
	private java.util.List _WsdlElement = new java.util.ArrayList();	// List<WsdlElementType>

	/**
	 * Normal starting point constructor.
	 */
	public TemplateType() {
	}

	/**
	 * Required parameters constructor
	 */
	public TemplateType(org.netbeans.modules.xml.wsdl.bindingsupport.template.WsdlElementType[] wsdlElement) {
		if (wsdlElement!= null) {
			((java.util.ArrayList) _WsdlElement).ensureCapacity(wsdlElement.length);
			for (int i = 0; i < wsdlElement.length; ++i) {
				_WsdlElement.add(wsdlElement[i]);
			}
		}
	}

	/**
	 * Deep copy
	 */
	public TemplateType(org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateType source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public TemplateType(org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateType source, boolean justData) {
		_Name = source._Name;
		_Default = source._Default;
		for (java.util.Iterator it = source._WsdlElement.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.xml.wsdl.bindingsupport.template.WsdlElementType srcElement = (org.netbeans.modules.xml.wsdl.bindingsupport.template.WsdlElementType)it.next();
			_WsdlElement.add((srcElement == null) ? null : newWsdlElementType(srcElement, justData));
		}
	}

	// This attribute is optional
	public void setName(java.lang.String value) {
		_Name = value;
	}

	public java.lang.String getName() {
		return _Name;
	}

	// This attribute is optional
	public void setDefault(boolean value) {
		_Default = value;
	}

	public boolean isDefault() {
		return _Default;
	}

	// This attribute is an array containing at least one element
	public void setWsdlElement(org.netbeans.modules.xml.wsdl.bindingsupport.template.WsdlElementType[] value) {
		if (value == null)
			value = new WsdlElementType[0];
		_WsdlElement.clear();
		((java.util.ArrayList) _WsdlElement).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_WsdlElement.add(value[i]);
		}
	}

	public void setWsdlElement(int index, org.netbeans.modules.xml.wsdl.bindingsupport.template.WsdlElementType value) {
		_WsdlElement.set(index, value);
	}

	public org.netbeans.modules.xml.wsdl.bindingsupport.template.WsdlElementType[] getWsdlElement() {
		WsdlElementType[] arr = new WsdlElementType[_WsdlElement.size()];
		return (WsdlElementType[]) _WsdlElement.toArray(arr);
	}

	public java.util.List fetchWsdlElementList() {
		return _WsdlElement;
	}

	public org.netbeans.modules.xml.wsdl.bindingsupport.template.WsdlElementType getWsdlElement(int index) {
		return (WsdlElementType)_WsdlElement.get(index);
	}

	// Return the number of wsdlElement
	public int sizeWsdlElement() {
		return _WsdlElement.size();
	}

	public int addWsdlElement(org.netbeans.modules.xml.wsdl.bindingsupport.template.WsdlElementType value) {
		_WsdlElement.add(value);
		int positionOfNewItem = _WsdlElement.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeWsdlElement(org.netbeans.modules.xml.wsdl.bindingsupport.template.WsdlElementType value) {
		int pos = _WsdlElement.indexOf(value);
		if (pos >= 0) {
			_WsdlElement.remove(pos);
		}
		return pos;
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.xml.wsdl.bindingsupport.template.WsdlElementType newWsdlElementType() {
		return new org.netbeans.modules.xml.wsdl.bindingsupport.template.WsdlElementType();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.xml.wsdl.bindingsupport.template.WsdlElementType newWsdlElementType(WsdlElementType source, boolean justData) {
		return new org.netbeans.modules.xml.wsdl.bindingsupport.template.WsdlElementType(source, justData);
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "templateType";
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
		// name is an attribute with namespace http://xml.netbeans.org/schema/templates
		if (_Name != null) {
			out.write(" name='");
			org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateGroup.writeXML(out, _Name, true);
			out.write("'");	// NOI18N
		}
		// default is an attribute with namespace http://xml.netbeans.org/schema/templates
		out.write(" default='");
		out.write(_Default ? "true" : "false");
		out.write("'");	// NOI18N
	}

	protected void writeNodeChildren(java.io.Writer out, String nodeName, String namespace, String indent, java.util.Map namespaceMap) throws java.io.IOException {
		String nextIndent = indent + "	";
		for (java.util.Iterator it = _WsdlElement.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.xml.wsdl.bindingsupport.template.WsdlElementType element = (org.netbeans.modules.xml.wsdl.bindingsupport.template.WsdlElementType)it.next();
			if (element != null) {
				element.writeNode(out, "wsdlElement", null, nextIndent, namespaceMap);
			}
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
		attr = (org.w3c.dom.Attr) attrs.getNamedItem("name");
		if (attr != null) {
			attrValue = attr.getValue();
			_Name = attrValue;
		}
		attr = (org.w3c.dom.Attr) attrs.getNamedItem("default");
		if (attr != null) {
			attrValue = attr.getValue();
			_Default = java.lang.Boolean.valueOf(attrValue).booleanValue();
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
			if (childNodeName == "wsdlElement") {
				WsdlElementType aWsdlElement = newWsdlElementType();
				aWsdlElement.readNode(childNode, namespacePrefixes);
				_WsdlElement.add(aWsdlElement);
			}
			else {
				// Found extra unrecognized childNode
			}
		}
	}

	public void changePropertyByName(String name, Object value) {
		if (name == null) return;
		name = name.intern();
		if (name == "name")
			setName((java.lang.String)value);
		else if (name == "default")
			setDefault(((java.lang.Boolean)value).booleanValue());
		else if (name == "wsdlElement")
			addWsdlElement((WsdlElementType)value);
		else if (name == "wsdlElement[]")
			setWsdlElement((WsdlElementType[]) value);
		else
			throw new IllegalArgumentException(name+" is not a valid property name for TemplateType");
	}

	public Object fetchPropertyByName(String name) {
		if (name == "name")
			return getName();
		if (name == "default")
			return (isDefault() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if (name == "wsdlElement[]")
			return getWsdlElement();
		throw new IllegalArgumentException(name+" is not a valid property name for TemplateType");
	}

	public String nameSelf() {
		return "TemplateType";
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
			if (child == _Name) {
				if (returnConstName) {
					return NAME;
				} else if (returnSchemaName) {
					return "name";
				} else if (returnXPathName) {
					return "@name";
				} else {
					return "Name";
				}
			}
		}
		if (childObj instanceof WsdlElementType) {
			WsdlElementType child = (WsdlElementType) childObj;
			int index = 0;
			for (java.util.Iterator it = _WsdlElement.iterator(); 
				it.hasNext(); ) {
				org.netbeans.modules.xml.wsdl.bindingsupport.template.WsdlElementType element = (org.netbeans.modules.xml.wsdl.bindingsupport.template.WsdlElementType)it.next();
				if (child == element) {
					if (returnConstName) {
						return WSDLELEMENT;
					} else if (returnSchemaName) {
						return "wsdlElement";
					} else if (returnXPathName) {
						return "wsdlElement[position()="+index+"]";
					} else {
						return "WsdlElement."+Integer.toHexString(index);
					}
				}
				++index;
			}
		}
		if (childObj instanceof java.lang.Boolean) {
			java.lang.Boolean child = (java.lang.Boolean) childObj;
			if (((java.lang.Boolean)child).booleanValue() == _Default) {
				if (returnConstName) {
					return DEFAULT;
				} else if (returnSchemaName) {
					return "default";
				} else if (returnXPathName) {
					return "@default";
				} else {
					return "Default";
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
		for (java.util.Iterator it = _WsdlElement.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.xml.wsdl.bindingsupport.template.WsdlElementType element = (org.netbeans.modules.xml.wsdl.bindingsupport.template.WsdlElementType)it.next();
			if (element != null) {
				if (recursive) {
					element.childBeans(true, beans);
				}
				beans.add(element);
			}
		}
	}

	public boolean equals(Object o) {
		return o instanceof org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateType && equals((org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateType) o);
	}

	public boolean equals(org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateType inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (!(_Name == null ? inst._Name == null : _Name.equals(inst._Name))) {
			return false;
		}
		if (!(_Default == inst._Default)) {
			return false;
		}
		if (sizeWsdlElement() != inst.sizeWsdlElement())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _WsdlElement.iterator(), it2 = inst._WsdlElement.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			org.netbeans.modules.xml.wsdl.bindingsupport.template.WsdlElementType element = (org.netbeans.modules.xml.wsdl.bindingsupport.template.WsdlElementType)it.next();
			org.netbeans.modules.xml.wsdl.bindingsupport.template.WsdlElementType element2 = (org.netbeans.modules.xml.wsdl.bindingsupport.template.WsdlElementType)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		return true;
	}

	public int hashCode() {
		int result = 17;
		result = 37*result + (_Name == null ? 0 : _Name.hashCode());
		result = 37*result + (_Default ? 0 : 1);
		result = 37*result + (_WsdlElement == null ? 0 : _WsdlElement.hashCode());
		return result;
	}

}


/*
		The following schema file has been used for generation:

<?xml version="1.0" encoding="UTF-8"?>

<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"
            targetNamespace="http://xml.netbeans.org/schema/templates"
            xmlns:tns="http://xml.netbeans.org/schema/templates"
            elementFormDefault="qualified">
    <xsd:element name="templateGroup">
        <xsd:complexType>
            <xsd:sequence>
                <xsd:element name="template" type="tns:templateType" maxOccurs="unbounded"/>
            </xsd:sequence>
            <xsd:attribute name="namespace" type="xsd:string"/>
            <xsd:attribute name="prefix" type="xsd:string"/>
        </xsd:complexType>
    </xsd:element>
    <xsd:complexType name="templateType">
        <xsd:sequence>
            <xsd:element name="wsdlElement" type="tns:wsdlElementType" maxOccurs="unbounded"/>
        </xsd:sequence>
        <xsd:attribute name="name" type="xsd:string"/>
        <xsd:attribute name="default" type="xsd:boolean"/>
    </xsd:complexType>
    <xsd:complexType name="wsdlElementType">
        <xsd:sequence>
            <xsd:element name="extensionElement" type="tns:extensionElementType" maxOccurs="unbounded"/>
        </xsd:sequence>
        <xsd:attribute name="name" type="xsd:string"/>
    </xsd:complexType>
    <xsd:complexType name="extensionElementType">
        <xsd:sequence>
            <xsd:element name="extensionAttr" type="tns:extensionAttrType" maxOccurs="unbounded"/>
        </xsd:sequence>
        <xsd:attribute name="name" type="xsd:string"/>
    </xsd:complexType>
    <xsd:complexType name="extensionAttrType">
        <xsd:attribute name="name" type="xsd:string"/>
        <xsd:attribute name="defaultValue" type="xsd:string"/>
    </xsd:complexType>
</xsd:schema>


*/
