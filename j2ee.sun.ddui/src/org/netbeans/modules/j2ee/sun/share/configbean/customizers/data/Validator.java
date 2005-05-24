/**
 *	This generated bean class Validator
 *	matches the schema element 'validator'.
 *  The root bean class is DynamicProperties
 *
 *	Generated on Wed Sep 29 16:29:53 PDT 2004
 * @Generated
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.data;

public class Validator {
	public static final String VALIDATOR_NAME = "ValidatorName";	// NOI18N
	public static final String VALIDATOR_PATTERN = "ValidatorPattern";	// NOI18N

	private String _ValidatorName;
	private String _ValidatorPattern;

	/**
	 * Normal starting point constructor.
	 */
	public Validator() {
		_ValidatorName = "";
		_ValidatorPattern = "";
	}

	/**
	 * Required parameters constructor
	 */
	public Validator(String validatorName, String validatorPattern) {
		_ValidatorName = validatorName;
		_ValidatorPattern = validatorPattern;
	}

	/**
	 * Deep copy
	 */
	public Validator(org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.Validator source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public Validator(org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.Validator source, boolean justData) {
		_ValidatorName = source._ValidatorName;
		_ValidatorPattern = source._ValidatorPattern;
	}

	// This attribute is mandatory
	public void setValidatorName(String value) {
		_ValidatorName = value;
	}

	public String getValidatorName() {
		return _ValidatorName;
	}

	// This attribute is mandatory
	public void setValidatorPattern(String value) {
		_ValidatorPattern = value;
	}

	public String getValidatorPattern() {
		return _ValidatorPattern;
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "validator";
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
		if (_ValidatorName != null) {
			out.write(nextIndent);
			out.write("<validator-name");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.DynamicProperties.writeXML(out, _ValidatorName, false);
			out.write("</validator-name>\n");	// NOI18N
		}
		if (_ValidatorPattern != null) {
			out.write(nextIndent);
			out.write("<validator-pattern");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.DynamicProperties.writeXML(out, _ValidatorPattern, false);
			out.write("</validator-pattern>\n");	// NOI18N
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
			if (childNodeName == "validator-name") {
				_ValidatorName = childNodeValue;
			}
			else if (childNodeName == "validator-pattern") {
				_ValidatorPattern = childNodeValue;
			}
			else {
				// Found extra unrecognized childNode
			}
		}
	}

	public void changePropertyByName(String name, Object value) {
		if (name == null) return;
		name = name.intern();
		if (name == "validatorName")
			setValidatorName((String)value);
		else if (name == "validatorPattern")
			setValidatorPattern((String)value);
		else
			throw new IllegalArgumentException(name+" is not a valid property name for Validator");
	}

	public Object fetchPropertyByName(String name) {
		if (name == "validatorName")
			return getValidatorName();
		if (name == "validatorPattern")
			return getValidatorPattern();
		throw new IllegalArgumentException(name+" is not a valid property name for Validator");
	}

	public String nameSelf() {
		return "Validator";
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
			if (child == _ValidatorName) {
				if (returnConstName) {
					return VALIDATOR_NAME;
				} else if (returnSchemaName) {
					return "validator-name";
				} else if (returnXPathName) {
					return "validator-name";
				} else {
					return "ValidatorName";
				}
			}
			if (child == _ValidatorPattern) {
				if (returnConstName) {
					return VALIDATOR_PATTERN;
				} else if (returnSchemaName) {
					return "validator-pattern";
				} else if (returnXPathName) {
					return "validator-pattern";
				} else {
					return "ValidatorPattern";
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
		return o instanceof org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.Validator && equals((org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.Validator) o);
	}

	public boolean equals(org.netbeans.modules.j2ee.sun.share.configbean.customizers.data.Validator inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (!(_ValidatorName == null ? inst._ValidatorName == null : _ValidatorName.equals(inst._ValidatorName))) {
			return false;
		}
		if (!(_ValidatorPattern == null ? inst._ValidatorPattern == null : _ValidatorPattern.equals(inst._ValidatorPattern))) {
			return false;
		}
		return true;
	}

	public int hashCode() {
		int result = 17;
		result = 37*result + (_ValidatorName == null ? 0 : _ValidatorName.hashCode());
		result = 37*result + (_ValidatorPattern == null ? 0 : _ValidatorPattern.hashCode());
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

