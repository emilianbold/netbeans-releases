/**
 *	This generated bean class Pojo
 *	matches the schema element 'POJO'.
 *  The root bean class is POJOs
 *
 *	Generated on Wed Oct 14 16:40:57 PDT 2009
 * @Generated
 */

package org.netbeans.modules.soa.pojo.schema;

/**
 *  
 * @author gpatil
 */
public class Pojo {
	public static final String CLASS_NAME = "ClassName";	// NOI18N
	public static final String PACKAGE = "Package";	// NOI18N
	public static final String EP_NAME = "EpName";	// NOI18N
	public static final String UPDATE_WSDL_DURING_BUILD = "UpdateWsdlDuringBuild";	// NOI18N
	public static final String WSDL_LOCATION = "WsdlLocation";	// NOI18N
	public static final String ORIG_WSDL_LOCATION_TYPE = "OrigWsdlLocationType";	// NOI18N
	public static final String ORIG_WSDL_LOCATION = "OrigWsdlLocation";	// NOI18N

	private java.lang.String _ClassName;
	private java.lang.String _Package;
	private java.lang.String _EpName;
	private boolean _UpdateWsdlDuringBuild = false;
	private java.lang.String _WsdlLocation;
	private java.lang.String _OrigWsdlLocationType;
	private java.lang.String _OrigWsdlLocation;

	/**
	 * Normal starting point constructor.
	 */
	public Pojo() {
		_ClassName = "";
		_Package = "";
	}

	/**
	 * Required parameters constructor
	 */
	public Pojo(java.lang.String className, java.lang.String a_package, boolean updateWsdlDuringBuild) {
		_ClassName = className;
		_Package = a_package;
		_UpdateWsdlDuringBuild = updateWsdlDuringBuild;
	}

	/**
	 * Deep copy
	 */
	public Pojo(org.netbeans.modules.soa.pojo.schema.Pojo source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public Pojo(org.netbeans.modules.soa.pojo.schema.Pojo source, boolean justData) {
		_ClassName = source._ClassName;
		_Package = source._Package;
		_EpName = source._EpName;
		_UpdateWsdlDuringBuild = source._UpdateWsdlDuringBuild;
		_WsdlLocation = source._WsdlLocation;
		_OrigWsdlLocationType = source._OrigWsdlLocationType;
		_OrigWsdlLocation = source._OrigWsdlLocation;
	}

	// This attribute is mandatory
	public void setClassName(java.lang.String value) {
		_ClassName = value;
	}

	public java.lang.String getClassName() {
		return _ClassName;
	}

	// This attribute is mandatory
	public void setPackage(java.lang.String value) {
		_Package = value;
	}

	public java.lang.String getPackage() {
		return _Package;
	}

	// This attribute is optional
	public void setEpName(java.lang.String value) {
		_EpName = value;
	}

	public java.lang.String getEpName() {
		return _EpName;
	}

	// This attribute is mandatory
	public void setUpdateWsdlDuringBuild(boolean value) {
		_UpdateWsdlDuringBuild = value;
	}

	public boolean isUpdateWsdlDuringBuild() {
		return _UpdateWsdlDuringBuild;
	}

	// This attribute is optional
	public void setWsdlLocation(java.lang.String value) {
		_WsdlLocation = value;
	}

	public java.lang.String getWsdlLocation() {
		return _WsdlLocation;
	}

	// This attribute is optional
	public void setOrigWsdlLocationType(java.lang.String value) {
		_OrigWsdlLocationType = value;
	}

	public java.lang.String getOrigWsdlLocationType() {
		return _OrigWsdlLocationType;
	}

	// This attribute is optional
	public void setOrigWsdlLocation(java.lang.String value) {
		_OrigWsdlLocation = value;
	}

	public java.lang.String getOrigWsdlLocation() {
		return _OrigWsdlLocation;
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "POJO";
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
	}

	protected void writeNodeChildren(java.io.Writer out, String nodeName, String namespace, String indent, java.util.Map namespaceMap) throws java.io.IOException {
		String nextIndent = indent + "	";
		if (_ClassName != null) {
			out.write(nextIndent);
			out.write("<class-name");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.soa.pojo.schema.POJOs.writeXML(out, _ClassName, false);
			out.write("</class-name>\n");	// NOI18N
		}
		if (_Package != null) {
			out.write(nextIndent);
			out.write("<package");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.soa.pojo.schema.POJOs.writeXML(out, _Package, false);
			out.write("</package>\n");	// NOI18N
		}
		if (_EpName != null) {
			out.write(nextIndent);
			out.write("<ep-name");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.soa.pojo.schema.POJOs.writeXML(out, _EpName, false);
			out.write("</ep-name>\n");	// NOI18N
		}
		out.write(nextIndent);
		out.write("<update-wsdl-during-build");	// NOI18N
		out.write(">");	// NOI18N
		out.write(_UpdateWsdlDuringBuild ? "true" : "false");
		out.write("</update-wsdl-during-build>\n");	// NOI18N
		if (_WsdlLocation != null) {
			out.write(nextIndent);
			out.write("<wsdl-location");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.soa.pojo.schema.POJOs.writeXML(out, _WsdlLocation, false);
			out.write("</wsdl-location>\n");	// NOI18N
		}
		if (_OrigWsdlLocationType != null) {
			out.write(nextIndent);
			out.write("<orig-wsdl-location-type");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.soa.pojo.schema.POJOs.writeXML(out, _OrigWsdlLocationType, false);
			out.write("</orig-wsdl-location-type>\n");	// NOI18N
		}
		if (_OrigWsdlLocation != null) {
			out.write(nextIndent);
			out.write("<orig-wsdl-location");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.soa.pojo.schema.POJOs.writeXML(out, _OrigWsdlLocation, false);
			out.write("</orig-wsdl-location>\n");	// NOI18N
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
			if (childNodeName == "class-name") {
				_ClassName = childNodeValue;
			}
			else if (childNodeName == "package") {
				_Package = childNodeValue;
			}
			else if (childNodeName == "ep-name") {
				_EpName = childNodeValue;
			}
			else if (childNodeName == "update-wsdl-during-build") {
				if (childNode.getFirstChild() == null)
					_UpdateWsdlDuringBuild = true;
				else
					_UpdateWsdlDuringBuild = java.lang.Boolean.valueOf(childNodeValue).booleanValue();
			}
			else if (childNodeName == "wsdl-location") {
				_WsdlLocation = childNodeValue;
			}
			else if (childNodeName == "orig-wsdl-location-type") {
				_OrigWsdlLocationType = childNodeValue;
			}
			else if (childNodeName == "orig-wsdl-location") {
				_OrigWsdlLocation = childNodeValue;
			}
			else {
				// Found extra unrecognized childNode
			}
		}
	}

	public void changePropertyByName(String name, Object value) {
		if (name == null) return;
		name = name.intern();
		if (name == "className")
			setClassName((java.lang.String)value);
		else if (name == "package")
			setPackage((java.lang.String)value);
		else if (name == "epName")
			setEpName((java.lang.String)value);
		else if (name == "updateWsdlDuringBuild")
			setUpdateWsdlDuringBuild(((java.lang.Boolean)value).booleanValue());
		else if (name == "wsdlLocation")
			setWsdlLocation((java.lang.String)value);
		else if (name == "origWsdlLocationType")
			setOrigWsdlLocationType((java.lang.String)value);
		else if (name == "origWsdlLocation")
			setOrigWsdlLocation((java.lang.String)value);
		else
			throw new IllegalArgumentException(name+" is not a valid property name for Pojo");
	}

	public Object fetchPropertyByName(String name) {
		if (name == "className")
			return getClassName();
		if (name == "package")
			return getPackage();
		if (name == "epName")
			return getEpName();
		if (name == "updateWsdlDuringBuild")
			return (isUpdateWsdlDuringBuild() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if (name == "wsdlLocation")
			return getWsdlLocation();
		if (name == "origWsdlLocationType")
			return getOrigWsdlLocationType();
		if (name == "origWsdlLocation")
			return getOrigWsdlLocation();
		throw new IllegalArgumentException(name+" is not a valid property name for Pojo");
	}

	public String nameSelf() {
		return "Pojo";
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
					return CLASS_NAME;
				} else if (returnSchemaName) {
					return "class-name";
				} else if (returnXPathName) {
					return "class-name";
				} else {
					return "ClassName";
				}
			}
			if (child == _Package) {
				if (returnConstName) {
					return PACKAGE;
				} else if (returnSchemaName) {
					return "package";
				} else if (returnXPathName) {
					return "package";
				} else {
					return "Package";
				}
			}
			if (child == _EpName) {
				if (returnConstName) {
					return EP_NAME;
				} else if (returnSchemaName) {
					return "ep-name";
				} else if (returnXPathName) {
					return "ep-name";
				} else {
					return "EpName";
				}
			}
			if (child == _WsdlLocation) {
				if (returnConstName) {
					return WSDL_LOCATION;
				} else if (returnSchemaName) {
					return "wsdl-location";
				} else if (returnXPathName) {
					return "wsdl-location";
				} else {
					return "WsdlLocation";
				}
			}
			if (child == _OrigWsdlLocationType) {
				if (returnConstName) {
					return ORIG_WSDL_LOCATION_TYPE;
				} else if (returnSchemaName) {
					return "orig-wsdl-location-type";
				} else if (returnXPathName) {
					return "orig-wsdl-location-type";
				} else {
					return "OrigWsdlLocationType";
				}
			}
			if (child == _OrigWsdlLocation) {
				if (returnConstName) {
					return ORIG_WSDL_LOCATION;
				} else if (returnSchemaName) {
					return "orig-wsdl-location";
				} else if (returnXPathName) {
					return "orig-wsdl-location";
				} else {
					return "OrigWsdlLocation";
				}
			}
		}
		if (childObj instanceof java.lang.Boolean) {
			java.lang.Boolean child = (java.lang.Boolean) childObj;
			if (((java.lang.Boolean)child).booleanValue() == _UpdateWsdlDuringBuild) {
				if (returnConstName) {
					return UPDATE_WSDL_DURING_BUILD;
				} else if (returnSchemaName) {
					return "update-wsdl-during-build";
				} else if (returnXPathName) {
					return "update-wsdl-during-build";
				} else {
					return "UpdateWsdlDuringBuild";
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
		return o instanceof org.netbeans.modules.soa.pojo.schema.Pojo && equals((org.netbeans.modules.soa.pojo.schema.Pojo) o);
	}

	public boolean equals(org.netbeans.modules.soa.pojo.schema.Pojo inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (!(_ClassName == null ? inst._ClassName == null : _ClassName.equals(inst._ClassName))) {
			return false;
		}
		if (!(_Package == null ? inst._Package == null : _Package.equals(inst._Package))) {
			return false;
		}
		if (!(_EpName == null ? inst._EpName == null : _EpName.equals(inst._EpName))) {
			return false;
		}
		if (!(_UpdateWsdlDuringBuild == inst._UpdateWsdlDuringBuild)) {
			return false;
		}
		if (!(_WsdlLocation == null ? inst._WsdlLocation == null : _WsdlLocation.equals(inst._WsdlLocation))) {
			return false;
		}
		if (!(_OrigWsdlLocationType == null ? inst._OrigWsdlLocationType == null : _OrigWsdlLocationType.equals(inst._OrigWsdlLocationType))) {
			return false;
		}
		if (!(_OrigWsdlLocation == null ? inst._OrigWsdlLocation == null : _OrigWsdlLocation.equals(inst._OrigWsdlLocation))) {
			return false;
		}
		return true;
	}

	public int hashCode() {
		int result = 17;
		result = 37*result + (_ClassName == null ? 0 : _ClassName.hashCode());
		result = 37*result + (_Package == null ? 0 : _Package.hashCode());
		result = 37*result + (_EpName == null ? 0 : _EpName.hashCode());
		result = 37*result + (_UpdateWsdlDuringBuild ? 0 : 1);
		result = 37*result + (_WsdlLocation == null ? 0 : _WsdlLocation.hashCode());
		result = 37*result + (_OrigWsdlLocationType == null ? 0 : _OrigWsdlLocationType.hashCode());
		result = 37*result + (_OrigWsdlLocation == null ? 0 : _OrigWsdlLocation.hashCode());
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
