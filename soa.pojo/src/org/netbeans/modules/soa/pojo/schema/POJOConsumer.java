/**
 *	This generated bean class POJOConsumer
 *	matches the schema element 'POJOConsumer'.
 *  The root bean class is POJOs
 *
 *	Generated on Wed Oct 14 16:40:57 PDT 2009
 * @Generated
 */

package org.netbeans.modules.soa.pojo.schema;

public class POJOConsumer {
	public static final String INTERFACE = "Interface";	// NOI18N
	public static final String OPERATION = "Operation";	// NOI18N
	public static final String INPUTMESSAGETYPE = "InputMessageType";	// NOI18N
	public static final String INVOKEPATTERN = "InvokePattern";	// NOI18N
	public static final String INVOKEINPUTTYPE = "InvokeInputType";	// NOI18N
	public static final String INVOKERETURNTYPE = "InvokeReturnType";	// NOI18N

	private javax.xml.namespace.QName _Interface;
	private javax.xml.namespace.QName _Operation;
	private javax.xml.namespace.QName _InputMessageType;
	private String _InvokePattern;
	private String _InvokeInputType;
	private String _InvokeReturnType;

	/**
	 * Normal starting point constructor.
	 */
	public POJOConsumer() {
		_Interface = new javax.xml.namespace.QName("");
		_Operation = new javax.xml.namespace.QName("");
		_InvokePattern = "";
		_InvokeInputType = "";
		_InvokeReturnType = "";
	}

	/**
	 * Required parameters constructor
	 */
	public POJOConsumer(javax.xml.namespace.QName a_interface, javax.xml.namespace.QName operation, String invokePattern, String invokeInputType, String invokeReturnType) {
		_Interface = a_interface;
		_Operation = operation;
		_InvokePattern = invokePattern;
		_InvokeInputType = invokeInputType;
		_InvokeReturnType = invokeReturnType;
	}

	/**
	 * Deep copy
	 */
	public POJOConsumer(org.netbeans.modules.soa.pojo.schema.POJOConsumer source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public POJOConsumer(org.netbeans.modules.soa.pojo.schema.POJOConsumer source, boolean justData) {
		_Interface = source._Interface;
		_Operation = source._Operation;
		_InputMessageType = source._InputMessageType;
		_InvokePattern = source._InvokePattern;
		_InvokeInputType = source._InvokeInputType;
		_InvokeReturnType = source._InvokeReturnType;
	}

	// This attribute is mandatory
	public void setInterface(javax.xml.namespace.QName value) {
		_Interface = value;
	}

	public javax.xml.namespace.QName getInterface() {
		return _Interface;
	}

	// This attribute is mandatory
	public void setOperation(javax.xml.namespace.QName value) {
		_Operation = value;
	}

	public javax.xml.namespace.QName getOperation() {
		return _Operation;
	}

	// This attribute is optional
	public void setInputMessageType(javax.xml.namespace.QName value) {
		_InputMessageType = value;
	}

	public javax.xml.namespace.QName getInputMessageType() {
		return _InputMessageType;
	}

	// This attribute is mandatory
	public void setInvokePattern(String value) {
		_InvokePattern = value;
	}

	public String getInvokePattern() {
		return _InvokePattern;
	}

	// This attribute is mandatory
	public void setInvokeInputType(String value) {
		_InvokeInputType = value;
	}

	public String getInvokeInputType() {
		return _InvokeInputType;
	}

	// This attribute is mandatory
	public void setInvokeReturnType(String value) {
		_InvokeReturnType = value;
	}

	public String getInvokeReturnType() {
		return _InvokeReturnType;
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "POJOConsumer";
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
		// Work out any namespaces.
		boolean firstNSAddition = true;
		if (_Interface != null && _Interface.getNamespaceURI() != null && !"".equals(_Interface.getNamespaceURI())) {
			String prefix = (String) namespaceMap.get(_Interface.getNamespaceURI());
			if (prefix == null || "".equals(prefix)) {
				prefix = _Interface.getPrefix();
				if (prefix == null || "".equals(prefix)) {
					prefix = "Interface_ns__";
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
				namespaceMap.put(_Interface.getNamespaceURI(), prefix);
				out.write(" xmlns:");
				out.write(prefix);
				out.write("='");
				out.write(_Interface.getNamespaceURI());
				out.write("'");
			}
		}
		if (_Operation != null && _Operation.getNamespaceURI() != null && !"".equals(_Operation.getNamespaceURI())) {
			String prefix = (String) namespaceMap.get(_Operation.getNamespaceURI());
			if (prefix == null || "".equals(prefix)) {
				prefix = _Operation.getPrefix();
				if (prefix == null || "".equals(prefix)) {
					prefix = "Operation_ns__";
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
				namespaceMap.put(_Operation.getNamespaceURI(), prefix);
				out.write(" xmlns:");
				out.write(prefix);
				out.write("='");
				out.write(_Operation.getNamespaceURI());
				out.write("'");
			}
		}
		if (_InputMessageType != null && _InputMessageType.getNamespaceURI() != null && !"".equals(_InputMessageType.getNamespaceURI())) {
			String prefix = (String) namespaceMap.get(_InputMessageType.getNamespaceURI());
			if (prefix == null || "".equals(prefix)) {
				prefix = _InputMessageType.getPrefix();
				if (prefix == null || "".equals(prefix)) {
					prefix = "InputMessageType_ns__";
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
				namespaceMap.put(_InputMessageType.getNamespaceURI(), prefix);
				out.write(" xmlns:");
				out.write(prefix);
				out.write("='");
				out.write(_InputMessageType.getNamespaceURI());
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
	}

	protected void writeNodeChildren(java.io.Writer out, String nodeName, String namespace, String indent, java.util.Map namespaceMap) throws java.io.IOException {
		String nextIndent = indent + "	";
		if (_Interface != null) {
			out.write(nextIndent);
			out.write("<Interface");	// NOI18N
			String nsPrefix_Interface = null;
			if (_Interface.getNamespaceURI() != null && !"".equals(_Interface.getNamespaceURI())) {
				nsPrefix_Interface = (String) namespaceMap.get(_Interface.getNamespaceURI());
			}
			out.write(">");	// NOI18N
			if (_Interface.getNamespaceURI() != null && !"".equals(_Interface.getNamespaceURI())) {
				out.write((String) namespaceMap.get(_Interface.getNamespaceURI()));
				out.write(":");
			}
			org.netbeans.modules.soa.pojo.schema.POJOs.writeXML(out, _Interface.getLocalPart(), false);
			out.write("</Interface>\n");	// NOI18N
		}
		if (_Operation != null) {
			out.write(nextIndent);
			out.write("<Operation");	// NOI18N
			String nsPrefix_Operation = null;
			if (_Operation.getNamespaceURI() != null && !"".equals(_Operation.getNamespaceURI())) {
				nsPrefix_Operation = (String) namespaceMap.get(_Operation.getNamespaceURI());
			}
			out.write(">");	// NOI18N
			if (_Operation.getNamespaceURI() != null && !"".equals(_Operation.getNamespaceURI())) {
				out.write((String) namespaceMap.get(_Operation.getNamespaceURI()));
				out.write(":");
			}
			org.netbeans.modules.soa.pojo.schema.POJOs.writeXML(out, _Operation.getLocalPart(), false);
			out.write("</Operation>\n");	// NOI18N
		}
		if (_InputMessageType != null) {
			out.write(nextIndent);
			out.write("<InputMessageType");	// NOI18N
			String nsPrefix_InputMessageType = null;
			if (_InputMessageType.getNamespaceURI() != null && !"".equals(_InputMessageType.getNamespaceURI())) {
				nsPrefix_InputMessageType = (String) namespaceMap.get(_InputMessageType.getNamespaceURI());
			}
			out.write(">");	// NOI18N
			if (_InputMessageType.getNamespaceURI() != null && !"".equals(_InputMessageType.getNamespaceURI())) {
				out.write((String) namespaceMap.get(_InputMessageType.getNamespaceURI()));
				out.write(":");
			}
			org.netbeans.modules.soa.pojo.schema.POJOs.writeXML(out, _InputMessageType.getLocalPart(), false);
			out.write("</InputMessageType>\n");	// NOI18N
		}
		if (_InvokePattern != null) {
			out.write(nextIndent);
			out.write("<InvokePattern");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.soa.pojo.schema.POJOs.writeXML(out, _InvokePattern, false);
			out.write("</InvokePattern>\n");	// NOI18N
		}
		if (_InvokeInputType != null) {
			out.write(nextIndent);
			out.write("<InvokeInputType");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.soa.pojo.schema.POJOs.writeXML(out, _InvokeInputType, false);
			out.write("</InvokeInputType>\n");	// NOI18N
		}
		if (_InvokeReturnType != null) {
			out.write(nextIndent);
			out.write("<InvokeReturnType");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.soa.pojo.schema.POJOs.writeXML(out, _InvokeReturnType, false);
			out.write("</InvokeReturnType>\n");	// NOI18N
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
			if (childNodeName == "Interface") {
				int colonPos = childNodeValue.indexOf(':');
				if (colonPos < 0) {
					_Interface = new javax.xml.namespace.QName(childNodeValue);
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
					String prefix = childNodeValue.substring(0, colonPos);
					String ns = (String) nsPrefixes.get(prefix);
					String localPart = childNodeValue.substring(colonPos+1, childNodeValue.length());
					_Interface = new javax.xml.namespace.QName(ns, localPart, prefix);
				}
			}
			else if (childNodeName == "Operation") {
				int colonPos = childNodeValue.indexOf(':');
				if (colonPos < 0) {
					_Operation = new javax.xml.namespace.QName(childNodeValue);
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
					String prefix = childNodeValue.substring(0, colonPos);
					String ns = (String) nsPrefixes.get(prefix);
					String localPart = childNodeValue.substring(colonPos+1, childNodeValue.length());
					_Operation = new javax.xml.namespace.QName(ns, localPart, prefix);
				}
			}
			else if (childNodeName == "InputMessageType") {
				int colonPos = childNodeValue.indexOf(':');
				if (colonPos < 0) {
					_InputMessageType = new javax.xml.namespace.QName(childNodeValue);
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
					String prefix = childNodeValue.substring(0, colonPos);
					String ns = (String) nsPrefixes.get(prefix);
					String localPart = childNodeValue.substring(colonPos+1, childNodeValue.length());
					_InputMessageType = new javax.xml.namespace.QName(ns, localPart, prefix);
				}
			}
			else if (childNodeName == "InvokePattern") {
				_InvokePattern = childNodeValue;
			}
			else if (childNodeName == "InvokeInputType") {
				_InvokeInputType = childNodeValue;
			}
			else if (childNodeName == "InvokeReturnType") {
				_InvokeReturnType = childNodeValue;
			}
			else {
				// Found extra unrecognized childNode
			}
		}
	}

	public void changePropertyByName(String name, Object value) {
		if (name == null) return;
		name = name.intern();
		if (name == "interface")
			setInterface((javax.xml.namespace.QName)value);
		else if (name == "operation")
			setOperation((javax.xml.namespace.QName)value);
		else if (name == "inputMessageType")
			setInputMessageType((javax.xml.namespace.QName)value);
		else if (name == "invokePattern")
			setInvokePattern((String)value);
		else if (name == "invokeInputType")
			setInvokeInputType((String)value);
		else if (name == "invokeReturnType")
			setInvokeReturnType((String)value);
		else
			throw new IllegalArgumentException(name+" is not a valid property name for POJOConsumer");
	}

	public Object fetchPropertyByName(String name) {
		if (name == "interface")
			return getInterface();
		if (name == "operation")
			return getOperation();
		if (name == "inputMessageType")
			return getInputMessageType();
		if (name == "invokePattern")
			return getInvokePattern();
		if (name == "invokeInputType")
			return getInvokeInputType();
		if (name == "invokeReturnType")
			return getInvokeReturnType();
		throw new IllegalArgumentException(name+" is not a valid property name for POJOConsumer");
	}

	public String nameSelf() {
		return "POJOConsumer";
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
			if (child == _InvokePattern) {
				if (returnConstName) {
					return INVOKEPATTERN;
				} else if (returnSchemaName) {
					return "InvokePattern";
				} else if (returnXPathName) {
					return "InvokePattern";
				} else {
					return "InvokePattern";
				}
			}
			if (child == _InvokeInputType) {
				if (returnConstName) {
					return INVOKEINPUTTYPE;
				} else if (returnSchemaName) {
					return "InvokeInputType";
				} else if (returnXPathName) {
					return "InvokeInputType";
				} else {
					return "InvokeInputType";
				}
			}
			if (child == _InvokeReturnType) {
				if (returnConstName) {
					return INVOKERETURNTYPE;
				} else if (returnSchemaName) {
					return "InvokeReturnType";
				} else if (returnXPathName) {
					return "InvokeReturnType";
				} else {
					return "InvokeReturnType";
				}
			}
		}
		if (childObj instanceof javax.xml.namespace.QName) {
			javax.xml.namespace.QName child = (javax.xml.namespace.QName) childObj;
			if (child == _Interface) {
				if (returnConstName) {
					return INTERFACE;
				} else if (returnSchemaName) {
					return "Interface";
				} else if (returnXPathName) {
					return "Interface";
				} else {
					return "Interface";
				}
			}
			if (child == _Operation) {
				if (returnConstName) {
					return OPERATION;
				} else if (returnSchemaName) {
					return "Operation";
				} else if (returnXPathName) {
					return "Operation";
				} else {
					return "Operation";
				}
			}
			if (child == _InputMessageType) {
				if (returnConstName) {
					return INPUTMESSAGETYPE;
				} else if (returnSchemaName) {
					return "InputMessageType";
				} else if (returnXPathName) {
					return "InputMessageType";
				} else {
					return "InputMessageType";
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
		return o instanceof org.netbeans.modules.soa.pojo.schema.POJOConsumer && equals((org.netbeans.modules.soa.pojo.schema.POJOConsumer) o);
	}

	public boolean equals(org.netbeans.modules.soa.pojo.schema.POJOConsumer inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (!(_Interface == null ? inst._Interface == null : _Interface.equals(inst._Interface))) {
			return false;
		}
		if (!(_Operation == null ? inst._Operation == null : _Operation.equals(inst._Operation))) {
			return false;
		}
		if (!(_InputMessageType == null ? inst._InputMessageType == null : _InputMessageType.equals(inst._InputMessageType))) {
			return false;
		}
		if (!(_InvokePattern == null ? inst._InvokePattern == null : _InvokePattern.equals(inst._InvokePattern))) {
			return false;
		}
		if (!(_InvokeInputType == null ? inst._InvokeInputType == null : _InvokeInputType.equals(inst._InvokeInputType))) {
			return false;
		}
		if (!(_InvokeReturnType == null ? inst._InvokeReturnType == null : _InvokeReturnType.equals(inst._InvokeReturnType))) {
			return false;
		}
		return true;
	}

	public int hashCode() {
		int result = 17;
		result = 37*result + (_Interface == null ? 0 : _Interface.hashCode());
		result = 37*result + (_Operation == null ? 0 : _Operation.hashCode());
		result = 37*result + (_InputMessageType == null ? 0 : _InputMessageType.hashCode());
		result = 37*result + (_InvokePattern == null ? 0 : _InvokePattern.hashCode());
		result = 37*result + (_InvokeInputType == null ? 0 : _InvokeInputType.hashCode());
		result = 37*result + (_InvokeReturnType == null ? 0 : _InvokeReturnType.hashCode());
		return result;
	}

}


/*
		The following schema file has been used for generation:

<?xml version="1.0" encoding="UTF-8"?>
<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"
            targetNamespace="http://xml.netbeans.org/schema/POJOConfig"
            xmlns:tns="http://xml.netbeans.org/schema/POJOConfig"
            elementFormDefault="qualified">

    <xsd:element name="POJOs">
        <xsd:complexType>
            <xsd:sequence>
                <xsd:element ref="tns:POJOProviders" minOccurs="0"/>
                <xsd:element ref="tns:POJOConsumers" minOccurs="0"/>
                <!-- Do not use POJO, uses POJOProviders and POJOProvider -->
                <xsd:element ref="tns:POJO" maxOccurs="unbounded" minOccurs="0"/>
            </xsd:sequence>
            <xsd:attribute name="version" type="xsd:decimal"/>
        </xsd:complexType>
    </xsd:element>

    <xsd:element name="POJO">
        <xsd:complexType xmlns:xsd="http://www.w3.org/2001/XMLSchema">
            <xsd:sequence>
                <xsd:element name="class-name" type="xsd:string"/>
                <xsd:element name="package" type="xsd:string"/>
                <xsd:element name="ep-name" type="xsd:string" minOccurs="0"/>
                <xsd:element name="update-wsdl-during-build" type="xsd:boolean" default="false"/>
                <xsd:element name="wsdl-location" type="xsd:string" minOccurs="0"/>
                <xsd:element name="orig-wsdl-location-type" type="xsd:string" minOccurs="0"/>
                <xsd:element name="orig-wsdl-location" type="xsd:string" minOccurs="0"/>
            </xsd:sequence>
        </xsd:complexType>
    </xsd:element>

    <xsd:element name="POJOProviders">
        <xsd:complexType>
            <xsd:sequence maxOccurs="unbounded" minOccurs="0">
                <xsd:element ref="tns:POJOProvider"/>
            </xsd:sequence>
        </xsd:complexType>
    </xsd:element>

    <xsd:element name="POJOProvider">
        <xsd:complexType xmlns:xsd="http://www.w3.org/2001/XMLSchema">
            <xsd:sequence>
                <xsd:element name="class-name" type="xsd:string"/>
                <xsd:element name="package" type="xsd:string"/>
                <xsd:element name="ep-name" type="xsd:string" minOccurs="0"/>
                <xsd:element name="update-wsdl-during-build" type="xsd:boolean" default="false"/>
                <xsd:element name="wsdl-location" type="xsd:string" minOccurs="0"/>
                <xsd:element name="orig-wsdl-location-type" type="xsd:string" minOccurs="0"/>
                <xsd:element name="orig-wsdl-location" type="xsd:string" minOccurs="0"/>
            </xsd:sequence>
        </xsd:complexType>
    </xsd:element>

    <xsd:element name="POJOConsumer">
        <xsd:complexType>
            <xsd:sequence>
                <xsd:element name="Interface" type="xsd:QName"></xsd:element>
                <xsd:element name="Operation" type="xsd:QName"></xsd:element>
                <xsd:element name="InputMessageType" type="xsd:QName" minOccurs="0"></xsd:element>
                <xsd:element name="InvokePattern" type="xsd:String"></xsd:element>
                <xsd:element name="InvokeInputType" type="xsd:String"></xsd:element>
                <xsd:element name="InvokeReturnType" type="xsd:String" ></xsd:element>
            </xsd:sequence>
        </xsd:complexType>
    </xsd:element>
    <xsd:element name="POJOConsumers">
        <xsd:complexType>
            <xsd:sequence maxOccurs="unbounded" minOccurs="0">
                <xsd:element ref="tns:POJOConsumer"/>
            </xsd:sequence>
        </xsd:complexType>
    </xsd:element>
</xsd:schema>

*/
