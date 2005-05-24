/**
 *	This generated bean class ParamType
 *	matches the schema element 'param-type'.
 *  The root bean class is DynamicProperties
 *
 *	Generated on Wed Sep 29 16:29:53 PDT 2004
 * @Generated
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.data;

public class ParamType {
	public static final String TYPE = "Type";	// NOI18N
	public static final String EDITABLE = "Editable";	// NOI18N
	public static final String REQUIRED = "Required";	// NOI18N
	public static final String PARAM_VALUE = "ParamValue";	// NOI18N
	public static final String PARAM_LOCALE = "ParamLocale";	// NOI18N
	public static final String PARAM_CHARSET = "ParamCharset";	// NOI18N
	public static final String PARAM_MIN = "ParamMin";	// NOI18N
	public static final String PARAM_MAX = "ParamMax";	// NOI18N

	private java.lang.String _Type = "text";
	private java.lang.String _Editable = "false";
	private java.lang.String _Required = "true";
	private java.util.List _ParamValue = new java.util.ArrayList();	// List<String>
	private boolean _ParamLocale;
	private boolean _ParamCharset;
	private String _ParamMin;
	private String _ParamMax;

	/**
	 * Normal starting point constructor.
	 */
	public ParamType() {
	}

	/**
	 * Required parameters constructor
	 */
	public ParamType(java.lang.String type, java.lang.String editable, java.lang.String required) {
		_Type = type;
		_Editable = editable;
		_Required = required;
	}

	/**
	 * Deep copy
	 */
	public ParamType(org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.ParamType source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public ParamType(org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.ParamType source, boolean justData) {
		_Type = source._Type;
		_Editable = source._Editable;
		_Required = source._Required;
		for (java.util.Iterator it = source._ParamValue.iterator(); 
			it.hasNext(); ) {
			String srcElement = (String)it.next();
			_ParamValue.add(srcElement);
		}
		_ParamLocale = source._ParamLocale;
		_ParamCharset = source._ParamCharset;
		_ParamMin = source._ParamMin;
		_ParamMax = source._ParamMax;
	}

	// This attribute is mandatory
	public void setType(java.lang.String value) {
		_Type = value;
	}

	public java.lang.String getType() {
		return _Type;
	}

	// This attribute is mandatory
	public void setEditable(java.lang.String value) {
		_Editable = value;
	}

	public java.lang.String getEditable() {
		return _Editable;
	}

	// This attribute is mandatory
	public void setRequired(java.lang.String value) {
		_Required = value;
	}

	public java.lang.String getRequired() {
		return _Required;
	}

	// This attribute is an array, possibly empty
	public void setParamValue(String[] value) {
		if (value == null)
			value = new String[0];
		_ParamValue.clear();
		((java.util.ArrayList) _ParamValue).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_ParamValue.add(value[i]);
		}
		if (value != null && value.length > 0) {
			// It's a mutually exclusive property.
			setParamLocale(false);
			setParamCharset(false);
		}
	}

	public void setParamValue(int index, String value) {
		_ParamValue.set(index, value);
	}

	public String[] getParamValue() {
		String[] arr = new String[_ParamValue.size()];
		return (String[]) _ParamValue.toArray(arr);
	}

	public java.util.List fetchParamValueList() {
		return _ParamValue;
	}

	public String getParamValue(int index) {
		return (String)_ParamValue.get(index);
	}

	// Return the number of paramValue
	public int sizeParamValue() {
		return _ParamValue.size();
	}

	public int addParamValue(String value) {
		_ParamValue.add(value);
		int positionOfNewItem = _ParamValue.size()-1;
		if (positionOfNewItem == 0) {
			// It's a mutually exclusive property.
			setParamLocale(false);
			setParamCharset(false);
		}
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeParamValue(String value) {
		int pos = _ParamValue.indexOf(value);
		if (pos >= 0) {
			_ParamValue.remove(pos);
		}
		return pos;
	}

	// This attribute is mandatory
	public void setParamLocale(boolean value) {
		_ParamLocale = value;
		if (value != false) {
			// It's a mutually exclusive property.
			setParamValue(null);
			setParamCharset(false);
		}
	}

	public boolean isParamLocale() {
		return _ParamLocale;
	}

	// This attribute is mandatory
	public void setParamCharset(boolean value) {
		_ParamCharset = value;
		if (value != false) {
			// It's a mutually exclusive property.
			setParamValue(null);
			setParamLocale(false);
		}
	}

	public boolean isParamCharset() {
		return _ParamCharset;
	}

	// This attribute is optional
	public void setParamMin(String value) {
		_ParamMin = value;
	}

	public String getParamMin() {
		return _ParamMin;
	}

	// This attribute is optional
	public void setParamMax(String value) {
		_ParamMax = value;
	}

	public String getParamMax() {
		return _ParamMax;
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "param-type";
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
		// type is an attribute with namespace null
		if (_Type != null) {
			out.write(" type='");
			org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.DynamicProperties.writeXML(out, _Type, true);
			out.write("'");	// NOI18N
		}
		// editable is an attribute with namespace null
		if (_Editable != null) {
			out.write(" editable='");
			org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.DynamicProperties.writeXML(out, _Editable, true);
			out.write("'");	// NOI18N
		}
		// required is an attribute with namespace null
		if (_Required != null) {
			out.write(" required='");
			org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.DynamicProperties.writeXML(out, _Required, true);
			out.write("'");	// NOI18N
		}
		out.write(">\n");
		String nextIndent = indent + "	";
		for (java.util.Iterator it = _ParamValue.iterator(); it.hasNext(); 
			) {
			String element = (String)it.next();
			if (element != null) {
				out.write(nextIndent);
				out.write("<param-value");	// NOI18N
				out.write(">");	// NOI18N
				org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.DynamicProperties.writeXML(out, element, false);
				out.write("</param-value>\n");	// NOI18N
			}
		}
		if (_ParamLocale) {
			out.write(nextIndent);
			out.write("<param-locale");	// NOI18N
			out.write("/>\n");	// NOI18N
		}
		if (_ParamCharset) {
			out.write(nextIndent);
			out.write("<param-charset");	// NOI18N
			out.write("/>\n");	// NOI18N
		}
		if (_ParamMin != null) {
			out.write(nextIndent);
			out.write("<param-min");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.DynamicProperties.writeXML(out, _ParamMin, false);
			out.write("</param-min>\n");	// NOI18N
		}
		if (_ParamMax != null) {
			out.write(nextIndent);
			out.write("<param-max");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.DynamicProperties.writeXML(out, _ParamMax, false);
			out.write("</param-max>\n");	// NOI18N
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
			attr = (org.w3c.dom.Attr) attrs.getNamedItem("type");
			if (attr != null) {
				attrValue = attr.getValue();
				_Type = attrValue;
			}
			attr = (org.w3c.dom.Attr) attrs.getNamedItem("editable");
			if (attr != null) {
				attrValue = attr.getValue();
				_Editable = attrValue;
			}
			attr = (org.w3c.dom.Attr) attrs.getNamedItem("required");
			if (attr != null) {
				attrValue = attr.getValue();
				_Required = attrValue;
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
			if (childNodeName == "param-value") {
				String aParamValue;
				aParamValue = childNodeValue;
				_ParamValue.add(aParamValue);
			}
			else if (childNodeName == "param-locale") {
				if (childNode.getFirstChild() == null)
					_ParamLocale = true;
				else
					_ParamLocale = java.lang.Boolean.valueOf(childNodeValue).booleanValue();
			}
			else if (childNodeName == "param-charset") {
				if (childNode.getFirstChild() == null)
					_ParamCharset = true;
				else
					_ParamCharset = java.lang.Boolean.valueOf(childNodeValue).booleanValue();
			}
			else if (childNodeName == "param-min") {
				_ParamMin = childNodeValue;
			}
			else if (childNodeName == "param-max") {
				_ParamMax = childNodeValue;
			}
			else {
				// Found extra unrecognized childNode
			}
		}
	}

	public void changePropertyByName(String name, Object value) {
		if (name == null) return;
		name = name.intern();
		if (name == "type")
			setType((java.lang.String)value);
		else if (name == "editable")
			setEditable((java.lang.String)value);
		else if (name == "required")
			setRequired((java.lang.String)value);
		else if (name == "paramValue")
			addParamValue((String)value);
		else if (name == "paramValue[]")
			setParamValue((String[]) value);
		else if (name == "paramLocale")
			setParamLocale(((java.lang.Boolean)value).booleanValue());
		else if (name == "paramCharset")
			setParamCharset(((java.lang.Boolean)value).booleanValue());
		else if (name == "paramMin")
			setParamMin((String)value);
		else if (name == "paramMax")
			setParamMax((String)value);
		else
			throw new IllegalArgumentException(name+" is not a valid property name for ParamType");
	}

	public Object fetchPropertyByName(String name) {
		if (name == "type")
			return getType();
		if (name == "editable")
			return getEditable();
		if (name == "required")
			return getRequired();
		if (name == "paramValue[]")
			return getParamValue();
		if (name == "paramLocale")
			return (isParamLocale() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if (name == "paramCharset")
			return (isParamCharset() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if (name == "paramMin")
			return getParamMin();
		if (name == "paramMax")
			return getParamMax();
		throw new IllegalArgumentException(name+" is not a valid property name for ParamType");
	}

	public String nameSelf() {
		return "ParamType";
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
			if (child == _Type) {
				if (returnConstName) {
					return TYPE;
				} else if (returnSchemaName) {
					return "type";
				} else if (returnXPathName) {
					return "@type";
				} else {
					return "Type";
				}
			}
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
			if (child == _Required) {
				if (returnConstName) {
					return REQUIRED;
				} else if (returnSchemaName) {
					return "required";
				} else if (returnXPathName) {
					return "@required";
				} else {
					return "Required";
				}
			}
			int index = 0;
			for (java.util.Iterator it = _ParamValue.iterator(); 
				it.hasNext(); ) {
				String element = (String)it.next();
				if (child == element) {
					if (returnConstName) {
						return PARAM_VALUE;
					} else if (returnSchemaName) {
						return "param-value";
					} else if (returnXPathName) {
						return "param-value[position()="+index+"]";
					} else {
						return "ParamValue."+Integer.toHexString(index);
					}
				}
				++index;
			}
			if (child == _ParamMin) {
				if (returnConstName) {
					return PARAM_MIN;
				} else if (returnSchemaName) {
					return "param-min";
				} else if (returnXPathName) {
					return "param-min";
				} else {
					return "ParamMin";
				}
			}
			if (child == _ParamMax) {
				if (returnConstName) {
					return PARAM_MAX;
				} else if (returnSchemaName) {
					return "param-max";
				} else if (returnXPathName) {
					return "param-max";
				} else {
					return "ParamMax";
				}
			}
		}
		if (childObj instanceof java.lang.Boolean) {
			java.lang.Boolean child = (java.lang.Boolean) childObj;
			if (((java.lang.Boolean)child).booleanValue() == _ParamLocale) {
				if (returnConstName) {
					return PARAM_LOCALE;
				} else if (returnSchemaName) {
					return "param-locale";
				} else if (returnXPathName) {
					return "param-locale";
				} else {
					return "ParamLocale";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _ParamCharset) {
				if (returnConstName) {
					return PARAM_CHARSET;
				} else if (returnSchemaName) {
					return "param-charset";
				} else if (returnXPathName) {
					return "param-charset";
				} else {
					return "ParamCharset";
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
		return o instanceof org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.ParamType && equals((org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.ParamType) o);
	}

	public boolean equals(org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.ParamType inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (!(_Type == null ? inst._Type == null : _Type.equals(inst._Type))) {
			return false;
		}
		if (!(_Editable == null ? inst._Editable == null : _Editable.equals(inst._Editable))) {
			return false;
		}
		if (!(_Required == null ? inst._Required == null : _Required.equals(inst._Required))) {
			return false;
		}
		if (sizeParamValue() != inst.sizeParamValue())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _ParamValue.iterator(), it2 = inst._ParamValue.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			String element = (String)it.next();
			String element2 = (String)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		if (!(_ParamLocale == inst._ParamLocale)) {
			return false;
		}
		if (!(_ParamCharset == inst._ParamCharset)) {
			return false;
		}
		if (!(_ParamMin == null ? inst._ParamMin == null : _ParamMin.equals(inst._ParamMin))) {
			return false;
		}
		if (!(_ParamMax == null ? inst._ParamMax == null : _ParamMax.equals(inst._ParamMax))) {
			return false;
		}
		return true;
	}

	public int hashCode() {
		int result = 17;
		result = 37*result + (_Type == null ? 0 : _Type.hashCode());
		result = 37*result + (_Editable == null ? 0 : _Editable.hashCode());
		result = 37*result + (_Required == null ? 0 : _Required.hashCode());
		result = 37*result + (_ParamValue == null ? 0 : _ParamValue.hashCode());
		result = 37*result + (_ParamLocale ? 0 : 1);
		result = 37*result + (_ParamCharset ? 0 : 1);
		result = 37*result + (_ParamMin == null ? 0 : _ParamMin.hashCode());
		result = 37*result + (_ParamMax == null ? 0 : _ParamMax.hashCode());
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

