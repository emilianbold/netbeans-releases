/**
 *	This generated bean class PropertyParam
 *	matches the schema element 'property-param'.
 *  The root bean class is DynamicProperties
 *
 *	Generated on Wed Sep 29 16:29:53 PDT 2004
 * @Generated
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.data;

public class PropertyParam {
	public static final String PARAM_NAME = "ParamName";	// NOI18N
	public static final String PARAM_TYPE = "ParamType";	// NOI18N
	public static final String PARAM_LABEL = "ParamLabel";	// NOI18N
	public static final String PARAM_VALIDATOR = "ParamValidator";	// NOI18N
	public static final String DEFAULT_VALUE = "DefaultValue";	// NOI18N
	public static final String HELP_ID = "HelpId";	// NOI18N
	public static final String PARAM_DESCRIPTION = "ParamDescription";	// NOI18N

	private String _ParamName;
	private ParamType _ParamType;
	private String _ParamLabel;
	private String _ParamValidator;
	private String _DefaultValue;
	private String _HelpId;
	private String _ParamDescription;

	/**
	 * Normal starting point constructor.
	 */
	public PropertyParam() {
		_ParamName = "";
		_ParamType = newParamType();
	}

	/**
	 * Required parameters constructor
	 */
	public PropertyParam(String paramName, org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.ParamType paramType) {
		_ParamName = paramName;
		_ParamType = paramType;
	}

	/**
	 * Deep copy
	 */
	public PropertyParam(org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyParam source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public PropertyParam(org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyParam source, boolean justData) {
		_ParamName = source._ParamName;
		_ParamType = (source._ParamType == null) ? null : newParamType(source._ParamType, justData);
		_ParamLabel = source._ParamLabel;
		_ParamValidator = source._ParamValidator;
		_DefaultValue = source._DefaultValue;
		_HelpId = source._HelpId;
		_ParamDescription = source._ParamDescription;
	}

	// This attribute is mandatory
	public void setParamName(String value) {
		_ParamName = value;
	}

	public String getParamName() {
		return _ParamName;
	}

	// This attribute is mandatory
	public void setParamType(org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.ParamType value) {
		_ParamType = value;
	}

	public org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.ParamType getParamType() {
		return _ParamType;
	}

	// This attribute is optional
	public void setParamLabel(String value) {
		_ParamLabel = value;
	}

	public String getParamLabel() {
		return _ParamLabel;
	}

	// This attribute is optional
	public void setParamValidator(String value) {
		_ParamValidator = value;
	}

	public String getParamValidator() {
		return _ParamValidator;
	}

	// This attribute is optional
	public void setDefaultValue(String value) {
		_DefaultValue = value;
	}

	public String getDefaultValue() {
		return _DefaultValue;
	}

	// This attribute is optional
	public void setHelpId(String value) {
		_HelpId = value;
	}

	public String getHelpId() {
		return _HelpId;
	}

	// This attribute is optional
	public void setParamDescription(String value) {
		_ParamDescription = value;
	}

	public String getParamDescription() {
		return _ParamDescription;
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.ParamType newParamType() {
		return new org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.ParamType();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.ParamType newParamType(ParamType source, boolean justData) {
		return new org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.ParamType(source, justData);
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "property-param";
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
		out.write(">\n");
		String nextIndent = indent + "	";
		if (_ParamName != null) {
			out.write(nextIndent);
			out.write("<param-name");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.DynamicProperties.writeXML(out, _ParamName, false);
			out.write("</param-name>\n");	// NOI18N
		}
		if (_ParamType != null) {
			_ParamType.writeNode(out, "param-type", null, nextIndent, namespaceMap);
		}
		if (_ParamLabel != null) {
			out.write(nextIndent);
			out.write("<param-label");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.DynamicProperties.writeXML(out, _ParamLabel, false);
			out.write("</param-label>\n");	// NOI18N
		}
		if (_ParamValidator != null) {
			out.write(nextIndent);
			out.write("<param-validator");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.DynamicProperties.writeXML(out, _ParamValidator, false);
			out.write("</param-validator>\n");	// NOI18N
		}
		if (_DefaultValue != null) {
			out.write(nextIndent);
			out.write("<default-value");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.DynamicProperties.writeXML(out, _DefaultValue, false);
			out.write("</default-value>\n");	// NOI18N
		}
		if (_HelpId != null) {
			out.write(nextIndent);
			out.write("<help-id");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.DynamicProperties.writeXML(out, _HelpId, false);
			out.write("</help-id>\n");	// NOI18N
		}
		if (_ParamDescription != null) {
			out.write(nextIndent);
			out.write("<param-description");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.DynamicProperties.writeXML(out, _ParamDescription, false);
			out.write("</param-description>\n");	// NOI18N
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
		}
		org.w3c.dom.NodeList children = node.getChildNodes();
		for (int i = 0, size = children.getLength(); i < size; ++i) {
			org.w3c.dom.Node childNode = children.item(i);
			String childNodeName = (childNode.getLocalName() == null ? childNode.getNodeName().intern() : childNode.getLocalName().intern());
			String childNodeValue = "";
			if (childNode.getFirstChild() != null) {
				childNodeValue = childNode.getFirstChild().getNodeValue();
			}
			if (childNodeName == "param-name") {
				_ParamName = childNodeValue;
			}
			else if (childNodeName == "param-type") {
				_ParamType = newParamType();
				_ParamType.readNode(childNode, namespacePrefixes);
			}
			else if (childNodeName == "param-label") {
				_ParamLabel = childNodeValue;
			}
			else if (childNodeName == "param-validator") {
				_ParamValidator = childNodeValue;
			}
			else if (childNodeName == "default-value") {
				_DefaultValue = childNodeValue;
			}
			else if (childNodeName == "help-id") {
				_HelpId = childNodeValue;
			}
			else if (childNodeName == "param-description") {
				_ParamDescription = childNodeValue;
			}
			else {
				// Found extra unrecognized childNode
			}
		}
	}

	public void changePropertyByName(String name, Object value) {
		if (name == null) return;
		name = name.intern();
		if (name == "paramName")
			setParamName((String)value);
		else if (name == "paramType")
			setParamType((ParamType)value);
		else if (name == "paramLabel")
			setParamLabel((String)value);
		else if (name == "paramValidator")
			setParamValidator((String)value);
		else if (name == "defaultValue")
			setDefaultValue((String)value);
		else if (name == "helpId")
			setHelpId((String)value);
		else if (name == "paramDescription")
			setParamDescription((String)value);
		else
			throw new IllegalArgumentException(name+" is not a valid property name for PropertyParam");
	}

	public Object fetchPropertyByName(String name) {
		if (name == "paramName")
			return getParamName();
		if (name == "paramType")
			return getParamType();
		if (name == "paramLabel")
			return getParamLabel();
		if (name == "paramValidator")
			return getParamValidator();
		if (name == "defaultValue")
			return getDefaultValue();
		if (name == "helpId")
			return getHelpId();
		if (name == "paramDescription")
			return getParamDescription();
		throw new IllegalArgumentException(name+" is not a valid property name for PropertyParam");
	}

	public String nameSelf() {
		return "PropertyParam";
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
			if (child == _ParamName) {
				if (returnConstName) {
					return PARAM_NAME;
				} else if (returnSchemaName) {
					return "param-name";
				} else if (returnXPathName) {
					return "param-name";
				} else {
					return "ParamName";
				}
			}
			if (child == _ParamLabel) {
				if (returnConstName) {
					return PARAM_LABEL;
				} else if (returnSchemaName) {
					return "param-label";
				} else if (returnXPathName) {
					return "param-label";
				} else {
					return "ParamLabel";
				}
			}
			if (child == _ParamValidator) {
				if (returnConstName) {
					return PARAM_VALIDATOR;
				} else if (returnSchemaName) {
					return "param-validator";
				} else if (returnXPathName) {
					return "param-validator";
				} else {
					return "ParamValidator";
				}
			}
			if (child == _DefaultValue) {
				if (returnConstName) {
					return DEFAULT_VALUE;
				} else if (returnSchemaName) {
					return "default-value";
				} else if (returnXPathName) {
					return "default-value";
				} else {
					return "DefaultValue";
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
			if (child == _ParamDescription) {
				if (returnConstName) {
					return PARAM_DESCRIPTION;
				} else if (returnSchemaName) {
					return "param-description";
				} else if (returnXPathName) {
					return "param-description";
				} else {
					return "ParamDescription";
				}
			}
		}
		if (childObj instanceof ParamType) {
			ParamType child = (ParamType) childObj;
			if (child == _ParamType) {
				if (returnConstName) {
					return PARAM_TYPE;
				} else if (returnSchemaName) {
					return "param-type";
				} else if (returnXPathName) {
					return "param-type";
				} else {
					return "ParamType";
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
		if (_ParamType != null) {
			if (recursive) {
				_ParamType.childBeans(true, beans);
			}
			beans.add(_ParamType);
		}
	}

	public boolean equals(Object o) {
		return o instanceof org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyParam && equals((org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyParam) o);
	}

	public boolean equals(org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.PropertyParam inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (!(_ParamName == null ? inst._ParamName == null : _ParamName.equals(inst._ParamName))) {
			return false;
		}
		if (!(_ParamType == null ? inst._ParamType == null : _ParamType.equals(inst._ParamType))) {
			return false;
		}
		if (!(_ParamLabel == null ? inst._ParamLabel == null : _ParamLabel.equals(inst._ParamLabel))) {
			return false;
		}
		if (!(_ParamValidator == null ? inst._ParamValidator == null : _ParamValidator.equals(inst._ParamValidator))) {
			return false;
		}
		if (!(_DefaultValue == null ? inst._DefaultValue == null : _DefaultValue.equals(inst._DefaultValue))) {
			return false;
		}
		if (!(_HelpId == null ? inst._HelpId == null : _HelpId.equals(inst._HelpId))) {
			return false;
		}
		if (!(_ParamDescription == null ? inst._ParamDescription == null : _ParamDescription.equals(inst._ParamDescription))) {
			return false;
		}
		return true;
	}

	public int hashCode() {
		int result = 17;
		result = 37*result + (_ParamName == null ? 0 : _ParamName.hashCode());
		result = 37*result + (_ParamType == null ? 0 : _ParamType.hashCode());
		result = 37*result + (_ParamLabel == null ? 0 : _ParamLabel.hashCode());
		result = 37*result + (_ParamValidator == null ? 0 : _ParamValidator.hashCode());
		result = 37*result + (_DefaultValue == null ? 0 : _DefaultValue.hashCode());
		result = 37*result + (_HelpId == null ? 0 : _HelpId.hashCode());
		result = 37*result + (_ParamDescription == null ? 0 : _ParamDescription.hashCode());
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

