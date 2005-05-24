/**
 *	This generated bean class PropertyList
 *	matches the schema element 'property-list'.
 *  The root bean class is DynamicProperties
 *
 *	Generated on Wed Sep 29 16:29:53 PDT 2004
 * @Generated
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.data;

public class PropertyList {
	public static final String EDITABLE = "Editable";	// NOI18N
	public static final String DESCRIPTION = "Description";	// NOI18N
	public static final String BUNDLEPATH = "BundlePath";	// NOI18N
	public static final String PROPERTY_NAME = "PropertyName";	// NOI18N
	public static final String PROPERTY_PARAM = "PropertyParam";	// NOI18N
	public static final String HELP_ID = "HelpId";	// NOI18N

	private java.lang.String _Editable = "false";
	private java.lang.String _Description = "true";
	private java.lang.String _BundlePath;
	private String _PropertyName;
	private java.util.List _PropertyParam = new java.util.ArrayList();	// List<PropertyParam>
	private String _HelpId;

	/**
	 * Normal starting point constructor.
	 */
	public PropertyList() {
		_PropertyName = "";
	}

	/**
	 * Required parameters constructor
	 */
	public PropertyList(java.lang.String editable, java.lang.String description, String propertyName, org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyParam[] propertyParam) {
		_Editable = editable;
		_Description = description;
		_PropertyName = propertyName;
		if (propertyParam!= null) {
			((java.util.ArrayList) _PropertyParam).ensureCapacity(propertyParam.length);
			for (int i = 0; i < propertyParam.length; ++i) {
				_PropertyParam.add(propertyParam[i]);
			}
		}
	}

	/**
	 * Deep copy
	 */
	public PropertyList(org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyList source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public PropertyList(org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyList source, boolean justData) {
		_Editable = source._Editable;
		_Description = source._Description;
		_BundlePath = source._BundlePath;
		_PropertyName = source._PropertyName;
		for (java.util.Iterator it = source._PropertyParam.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyParam srcElement = (org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyParam)it.next();
			_PropertyParam.add((srcElement == null) ? null : newPropertyParam(srcElement, justData));
		}
		_HelpId = source._HelpId;
	}

	// This attribute is mandatory
	public void setEditable(java.lang.String value) {
		_Editable = value;
	}

	public java.lang.String getEditable() {
		return _Editable;
	}

	// This attribute is mandatory
	public void setDescription(java.lang.String value) {
		_Description = value;
	}

	public java.lang.String getDescription() {
		return _Description;
	}

	// This attribute is optional
	public void setBundlePath(java.lang.String value) {
		_BundlePath = value;
	}

	public java.lang.String getBundlePath() {
		return _BundlePath;
	}

	// This attribute is mandatory
	public void setPropertyName(String value) {
		_PropertyName = value;
	}

	public String getPropertyName() {
		return _PropertyName;
	}

	// This attribute is an array containing at least one element
	public void setPropertyParam(org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyParam[] value) {
		if (value == null)
			value = new PropertyParam[0];
		_PropertyParam.clear();
		((java.util.ArrayList) _PropertyParam).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_PropertyParam.add(value[i]);
		}
	}

	public void setPropertyParam(int index, org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyParam value) {
		_PropertyParam.set(index, value);
	}

	public org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyParam[] getPropertyParam() {
		PropertyParam[] arr = new PropertyParam[_PropertyParam.size()];
		return (PropertyParam[]) _PropertyParam.toArray(arr);
	}

	public java.util.List fetchPropertyParamList() {
		return _PropertyParam;
	}
	public org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyParam getPropertyParam(int index) {
		return (PropertyParam)_PropertyParam.get(index);
	}

	// Return the number of propertyParam
	public int sizePropertyParam() {
		return _PropertyParam.size();
	}

	public int addPropertyParam(org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyParam value) {
		_PropertyParam.add(value);
		int positionOfNewItem = _PropertyParam.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removePropertyParam(org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyParam value) {
		int pos = _PropertyParam.indexOf(value);
		if (pos >= 0) {
			_PropertyParam.remove(pos);
		}
		return pos;
	}

	// This attribute is optional
	public void setHelpId(String value) {
		_HelpId = value;
	}

	public String getHelpId() {
		return _HelpId;
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyParam newPropertyParam() {
		return new org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyParam();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyParam newPropertyParam(PropertyParam source, boolean justData) {
		return new org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyParam(source, justData);
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "property-list";
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
		// editable is an attribute with namespace null
		if (_Editable != null) {
			out.write(" editable='");
			org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.DynamicProperties.writeXML(out, _Editable, true);
			out.write("'");	// NOI18N
		}
		// description is an attribute with namespace null
		if (_Description != null) {
			out.write(" description='");
			org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.DynamicProperties.writeXML(out, _Description, true);
			out.write("'");	// NOI18N
		}
		// bundle-path is an attribute with namespace null
		if (_BundlePath != null) {
			out.write(" bundle-path='");
			org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.DynamicProperties.writeXML(out, _BundlePath, true);
			out.write("'");	// NOI18N
		}
		out.write(">\n");
		String nextIndent = indent + "	";
		if (_PropertyName != null) {
			out.write(nextIndent);
			out.write("<property-name");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.DynamicProperties.writeXML(out, _PropertyName, false);
			out.write("</property-name>\n");	// NOI18N
		}
		for (java.util.Iterator it = _PropertyParam.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyParam element = (org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyParam)it.next();
			if (element != null) {
				element.writeNode(out, "property-param", null, nextIndent, namespaceMap);
			}
		}
		if (_HelpId != null) {
			out.write(nextIndent);
			out.write("<help-id");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.DynamicProperties.writeXML(out, _HelpId, false);
			out.write("</help-id>\n");	// NOI18N
		}
		out.write(indent);
		out.write("</");
		if (namespace != null) {
			out.write((String)namespaceMap.get(namespace));
			out.write(":");
		}
		out.write(nodeName);
		out.write(">\n");
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
			attr = (org.w3c.dom.Attr) attrs.getNamedItem("editable");
			if (attr != null) {
				attrValue = attr.getValue();
				_Editable = attrValue;
			}
			attr = (org.w3c.dom.Attr) attrs.getNamedItem("description");
			if (attr != null) {
				attrValue = attr.getValue();
				_Description = attrValue;
			}
			attr = (org.w3c.dom.Attr) attrs.getNamedItem("bundle-path");
			if (attr != null) {
				attrValue = attr.getValue();
				_BundlePath = attrValue;
			}
		}
		org.w3c.dom.NodeList children = node.getChildNodes();
		for (int i = 0, size = children.getLength(); i < size; ++i) {
			org.w3c.dom.Node childNode = children.item(i);
			String childNodeName = (childNode.getLocalName() == null ? childNode.getNodeName().intern() : childNode.getLocalName().intern());
			String childNodeValue = "";
			if (childNode.getFirstChild() != null) {
				childNodeValue = childNode.getFirstChild().getNodeValue();
			}
			if (childNodeName == "property-name") {
				_PropertyName = childNodeValue;
			}
			else if (childNodeName == "property-param") {
				PropertyParam aPropertyParam = newPropertyParam();
				aPropertyParam.readNode(childNode, namespacePrefixes);
				_PropertyParam.add(aPropertyParam);
			}
			else if (childNodeName == "help-id") {
				_HelpId = childNodeValue;
			}
			else {
				// Found extra unrecognized childNode
			}
		}
	}

	public void changePropertyByName(String name, Object value) {
		if (name == null) return;
		name = name.intern();
		if (name == "editable")
			setEditable((java.lang.String)value);
		else if (name == "description")
			setDescription((java.lang.String)value);
		else if (name == "bundlePath")
			setBundlePath((java.lang.String)value);
		else if (name == "propertyName")
			setPropertyName((String)value);
		else if (name == "propertyParam")
			addPropertyParam((PropertyParam)value);
		else if (name == "propertyParam[]")
			setPropertyParam((PropertyParam[]) value);
		else if (name == "helpId")
			setHelpId((String)value);
		else
			throw new IllegalArgumentException(name+" is not a valid property name for PropertyList");
	}

	public Object fetchPropertyByName(String name) {
		if (name == "editable")
			return getEditable();
		if (name == "description")
			return getDescription();
		if (name == "bundlePath")
			return getBundlePath();
		if (name == "propertyName")
			return getPropertyName();
		if (name == "propertyParam[]")
			return getPropertyParam();
		if (name == "helpId")
			return getHelpId();
		throw new IllegalArgumentException(name+" is not a valid property name for PropertyList");
	}

	public String nameSelf() {
		return "PropertyList";
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
			if (child == _Editable) {
				if (returnConstName) {
					return EDITABLE;
				} else if (returnSchemaName) {
					return "editable";
				} else if (returnXPathName) {
					return "@editable";
				} else {
					return "Editable";
				}
			}
			if (child == _Description) {
				if (returnConstName) {
					return DESCRIPTION;
				} else if (returnSchemaName) {
					return "description";
				} else if (returnXPathName) {
					return "@description";
				} else {
					return "Description";
				}
			}
			if (child == _BundlePath) {
				if (returnConstName) {
					return BUNDLEPATH;
				} else if (returnSchemaName) {
					return "bundle-path";
				} else if (returnXPathName) {
					return "@bundle-path";
				} else {
					return "BundlePath";
				}
			}
			if (child == _PropertyName) {
				if (returnConstName) {
					return PROPERTY_NAME;
				} else if (returnSchemaName) {
					return "property-name";
				} else if (returnXPathName) {
					return "property-name";
				} else {
					return "PropertyName";
				}
			}
			if (child == _HelpId) {
				if (returnConstName) {
					return HELP_ID;
				} else if (returnSchemaName) {
					return "help-id";
				} else if (returnXPathName) {
					return "help-id";
				} else {
					return "HelpId";
				}
			}
		}
		if (childObj instanceof PropertyParam) {
			PropertyParam child = (PropertyParam) childObj;
			int index = 0;
			for (java.util.Iterator it = _PropertyParam.iterator(); 
				it.hasNext(); ) {
				org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyParam element = (org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyParam)it.next();
				if (child == element) {
					if (returnConstName) {
						return PROPERTY_PARAM;
					} else if (returnSchemaName) {
						return "property-param";
					} else if (returnXPathName) {
						return "property-param[position()="+index+"]";
					} else {
						return "PropertyParam."+Integer.toHexString(index);
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
		for (java.util.Iterator it = _PropertyParam.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyParam element = (org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyParam)it.next();
			if (element != null) {
				if (recursive) {
					element.childBeans(true, beans);
				}
				beans.add(element);
			}
		}
	}

	public boolean equals(Object o) {
		return o instanceof org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyList && equals((org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyList) o);
	}

	public boolean equals(org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyList inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (!(_Editable == null ? inst._Editable == null : _Editable.equals(inst._Editable))) {
			return false;
		}
		if (!(_Description == null ? inst._Description == null : _Description.equals(inst._Description))) {
			return false;
		}
		if (!(_BundlePath == null ? inst._BundlePath == null : _BundlePath.equals(inst._BundlePath))) {
			return false;
		}
		if (!(_PropertyName == null ? inst._PropertyName == null : _PropertyName.equals(inst._PropertyName))) {
			return false;
		}
		if (sizePropertyParam() != inst.sizePropertyParam())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _PropertyParam.iterator(), it2 = inst._PropertyParam.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyParam element = (org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyParam)it.next();
			org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyParam element2 = (org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyParam)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		if (!(_HelpId == null ? inst._HelpId == null : _HelpId.equals(inst._HelpId))) {
			return false;
		}
		return true;
	}

	public int hashCode() {
		int result = 17;
		result = 37*result + (_Editable == null ? 0 : _Editable.hashCode());
		result = 37*result + (_Description == null ? 0 : _Description.hashCode());
		result = 37*result + (_BundlePath == null ? 0 : _BundlePath.hashCode());
		result = 37*result + (_PropertyName == null ? 0 : _PropertyName.hashCode());
		result = 37*result + (_PropertyParam == null ? 0 : _PropertyParam.hashCode());
		result = 37*result + (_HelpId == null ? 0 : _HelpId.hashCode());
		return result;
	}

}


/*
		The following schema file has been used for generation:

<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : dynamic-properties.dtd
    Created on : January 28, 2004, 8:48 PM
    Author     : Peter Williams
    Description:
        Purpose of the document follows.
		
	DTD for definition of properties, their editors and validators to allow for
	name/value pair property editing to be handled nicely in the plugin.
-->

<!-- The file is a list of property lists -->
<!ELEMENT dynamic-properties (property-list*, validator*)>

<!-- Each property list can be fixed or editable and has a name -->
<!ELEMENT property-list (property-name, property-param+, help-id?)>
<!ATTLIST property-list editable CDATA "false"
						description CDATA "true"
						bundle-path CDATA #IMPLIED>

<!ELEMENT property-name (#PCDATA)>

<!-- 
	Each element in a property list has a name.  It may also have a type, a
    validator, and possibly a min and/or max if the type is 'number'.  Lastly,
	it could have a helpId
-->
<!ELEMENT property-param (param-name, param-type, param-label?, param-validator?, default-value?, 
						  help-id?, param-description?)>

<!ELEMENT param-name (#PCDATA)>

<!-- 
	There are four allowed types: boolean, text, number, and list.  The editable
	attribute is ignored for all types except the list type.
	
	For boolean properties, default value should be string using the boolean
	  'ENTITY' definitions in the sun-xxx dtd's, preferably true/false.
	For text properties, you should provide a validator (or none to allow
	  arbitrary text) and a default value if desired.
	For number properties, the number is assumed to be a signed long integer.
	  Use the min & max params to specify a range if required.
	For list properties, use as many param-value entries as necessary to represent
	  the list.  If the list is editable, set the editable attribute on the type.
	  One exception here is if the list is the list of charsets or locales, use
	  param-locale or param-charset to specify this.  These lists are provided by
	  the Locale and Charset classes in the JVM.

	(I'm not actually defining ENTITY's here because Schema2Beans does not support them.) 
-->
<!ELEMENT param-type ((param-value* | param-locale | param-charset), param-min?, param-max?)>
<!ATTLIST param-type type CDATA "text" 
					 editable CDATA "false"
					 required CDATA "true">

<!ELEMENT param-value (#PCDATA)>
<!ELEMENT param-locale EMPTY>
<!ELEMENT param-charset EMPTY>
<!ELEMENT param-min (#PCDATA)>
<!ELEMENT param-max (#PCDATA)>

<!--
	The text label (actually, will become bundle string id) to be used for the 
	value field instead of the word 'Value'
-->
<!ELEMENT param-label (#PCDATA)>

<!--
	Validators are used to ensure the text in a text field matches a specific
	pattern.  The following validators are supported:  (Can I support java
	regular expression patterns here?  It would make it lots easier!!!)
	
		directory:	A directory path specification
		javaid:		A legal java identifier (allows java keywords though)
		url:		A URL string
		domain:		A domain.  This is probably similar to javaid + represents a server domain
		package:	A legal java package name, e.g. javaid's separated by periods.
		memorysize:	A number followed by kb or mb (case insensitive)
		classid:	A windows classid (GUID)
-->
<!ELEMENT param-validator (#PCDATA)>

<!--
	String that will become the default value for the property.  If the property
	value must fit a specific pattern, the default-value must qualify.
-->
<!ELEMENT default-value (#PCDATA)>

<!--
	The help id for this field (or panel if specified at the property level,
	which is likely what we'll do.
-->
<!ELEMENT help-id (#PCDATA)>


<!--
	ID of string in bundle (see property-list attributes) to use for default
	description.
-->
<!ELEMENT param-description (#PCDATA)>

<!--  !PW this would be used by property-param once it's done

	Version of the appserver this property-param belongs to.  Not present means
	the property is applicable to all versions.
	
	Allowable Strings:  major[.minor][pe|se|ee]
		Major version is require.
		Minor is optional (not present matches all)
		Type is optional (not present matches all)
		
	For range attribute, valid values are:
		ending, only, starting
<!ELEMENT appserver-version (#PCDATA)>
<!ATTLIST appserver-version range CDATA #IMPLIED>
-->

<!--
	Validator definition.  These are referred to by name from the <param-validator>
	entry in <property-param>, above.
-->
<!ELEMENT validator (validator-name, validator-pattern)>

<!ELEMENT validator-name (#PCDATA)>

<!ELEMENT validator-pattern (#PCDATA)>



*/

