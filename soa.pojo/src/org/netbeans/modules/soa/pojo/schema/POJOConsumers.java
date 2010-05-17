/**
 *	This generated bean class POJOConsumers
 *	matches the schema element 'POJOConsumers'.
 *  The root bean class is POJOs
 *
 *	Generated on Wed Oct 14 16:40:57 PDT 2009
 * @Generated
 */

package org.netbeans.modules.soa.pojo.schema;

public class POJOConsumers {
	public static final String POJOCONSUMER = "POJOConsumer";	// NOI18N

	private java.util.List _POJOConsumer = new java.util.ArrayList();	// List<POJOConsumer>

	/**
	 * Normal starting point constructor.
	 */
	public POJOConsumers() {
	}

	/**
	 * Deep copy
	 */
	public POJOConsumers(org.netbeans.modules.soa.pojo.schema.POJOConsumers source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public POJOConsumers(org.netbeans.modules.soa.pojo.schema.POJOConsumers source, boolean justData) {
		for (java.util.Iterator it = source._POJOConsumer.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.soa.pojo.schema.POJOConsumer srcElement = (org.netbeans.modules.soa.pojo.schema.POJOConsumer)it.next();
			_POJOConsumer.add((srcElement == null) ? null : newPOJOConsumer(srcElement, justData));
		}
	}

	// This attribute is an array, possibly empty
	public void setPOJOConsumer(org.netbeans.modules.soa.pojo.schema.POJOConsumer[] value) {
		if (value == null)
			value = new POJOConsumer[0];
		_POJOConsumer.clear();
		((java.util.ArrayList) _POJOConsumer).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_POJOConsumer.add(value[i]);
		}
	}

	public void setPOJOConsumer(int index, org.netbeans.modules.soa.pojo.schema.POJOConsumer value) {
		_POJOConsumer.set(index, value);
	}

	public org.netbeans.modules.soa.pojo.schema.POJOConsumer[] getPOJOConsumer() {
		POJOConsumer[] arr = new POJOConsumer[_POJOConsumer.size()];
		return (POJOConsumer[]) _POJOConsumer.toArray(arr);
	}

	public java.util.List fetchPOJOConsumerList() {
		return _POJOConsumer;
	}

	public org.netbeans.modules.soa.pojo.schema.POJOConsumer getPOJOConsumer(int index) {
		return (POJOConsumer)_POJOConsumer.get(index);
	}

	// Return the number of POJOConsumer
	public int sizePOJOConsumer() {
		return _POJOConsumer.size();
	}

	public int addPOJOConsumer(org.netbeans.modules.soa.pojo.schema.POJOConsumer value) {
		_POJOConsumer.add(value);
		int positionOfNewItem = _POJOConsumer.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removePOJOConsumer(org.netbeans.modules.soa.pojo.schema.POJOConsumer value) {
		int pos = _POJOConsumer.indexOf(value);
		if (pos >= 0) {
			_POJOConsumer.remove(pos);
		}
		return pos;
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.soa.pojo.schema.POJOConsumer newPOJOConsumer() {
		return new org.netbeans.modules.soa.pojo.schema.POJOConsumer();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.soa.pojo.schema.POJOConsumer newPOJOConsumer(POJOConsumer source, boolean justData) {
		return new org.netbeans.modules.soa.pojo.schema.POJOConsumer(source, justData);
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "POJOConsumers";
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
		for (java.util.Iterator it = _POJOConsumer.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.soa.pojo.schema.POJOConsumer element = (org.netbeans.modules.soa.pojo.schema.POJOConsumer)it.next();
			if (element != null) {
				element.writeNode(out, "POJOConsumer", null, nextIndent, namespaceMap);
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
			if (childNodeName == "POJOConsumer") {
				POJOConsumer aPOJOConsumer = newPOJOConsumer();
				aPOJOConsumer.readNode(childNode, namespacePrefixes);
				_POJOConsumer.add(aPOJOConsumer);
			}
			else {
				// Found extra unrecognized childNode
			}
		}
	}

	public void changePropertyByName(String name, Object value) {
		if (name == null) return;
		name = name.intern();
		if (name == "POJOConsumer")
			addPOJOConsumer((POJOConsumer)value);
		else if (name == "POJOConsumer[]")
			setPOJOConsumer((POJOConsumer[]) value);
		else
			throw new IllegalArgumentException(name+" is not a valid property name for POJOConsumers");
	}

	public Object fetchPropertyByName(String name) {
		if (name == "POJOConsumer[]")
			return getPOJOConsumer();
		throw new IllegalArgumentException(name+" is not a valid property name for POJOConsumers");
	}

	public String nameSelf() {
		return "POJOConsumers";
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
		if (childObj instanceof POJOConsumer) {
			POJOConsumer child = (POJOConsumer) childObj;
			int index = 0;
			for (java.util.Iterator it = _POJOConsumer.iterator(); 
				it.hasNext(); ) {
				org.netbeans.modules.soa.pojo.schema.POJOConsumer element = (org.netbeans.modules.soa.pojo.schema.POJOConsumer)it.next();
				if (child == element) {
					if (returnConstName) {
						return POJOCONSUMER;
					} else if (returnSchemaName) {
						return "POJOConsumer";
					} else if (returnXPathName) {
						return "POJOConsumer[position()="+index+"]";
					} else {
						return "POJOConsumer."+Integer.toHexString(index);
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
		for (java.util.Iterator it = _POJOConsumer.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.soa.pojo.schema.POJOConsumer element = (org.netbeans.modules.soa.pojo.schema.POJOConsumer)it.next();
			if (element != null) {
				if (recursive) {
					element.childBeans(true, beans);
				}
				beans.add(element);
			}
		}
	}

	public boolean equals(Object o) {
		return o instanceof org.netbeans.modules.soa.pojo.schema.POJOConsumers && equals((org.netbeans.modules.soa.pojo.schema.POJOConsumers) o);
	}

	public boolean equals(org.netbeans.modules.soa.pojo.schema.POJOConsumers inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (sizePOJOConsumer() != inst.sizePOJOConsumer())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _POJOConsumer.iterator(), it2 = inst._POJOConsumer.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			org.netbeans.modules.soa.pojo.schema.POJOConsumer element = (org.netbeans.modules.soa.pojo.schema.POJOConsumer)it.next();
			org.netbeans.modules.soa.pojo.schema.POJOConsumer element2 = (org.netbeans.modules.soa.pojo.schema.POJOConsumer)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		return true;
	}

	public int hashCode() {
		int result = 17;
		result = 37*result + (_POJOConsumer == null ? 0 : _POJOConsumer.hashCode());
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
