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
 *	This generated bean class PreferenceType
 *	matches the schema element 'preferenceType'.
 *  The root bean class is PortletApp
 *
 *	===============================================================
 *	
 *				Persistent preference values that may be used for customization
 *				and personalization by the portlet.
 *				Used in: portlet-preferences
 *				
 *	===============================================================
 *	Generated on Tue Apr 26 19:08:25 MDT 2005
 * @Generated
 */

package org.netbeans.modules.visualweb.api.portlet.dd;

public class PreferenceType implements org.netbeans.modules.visualweb.api.portlet.dd.PreferenceTypeInterface, org.netbeans.modules.visualweb.api.portlet.dd.CommonBean {
	public static final String ID = "Id";	// NOI18N
	public static final String NAME = "Name";	// NOI18N
	public static final String VALUE = "Value";	// NOI18N
	public static final String READ_ONLY = "ReadOnly";	// NOI18N

	private java.lang.String _Id;
	private java.lang.String _Name;
	private java.util.List _Value = new java.util.ArrayList();	// List<java.lang.String>
	private java.lang.String _ReadOnly;

	/**
	 * Normal starting point constructor.
	 */
	public PreferenceType() {
		_Name = "";
	}

	/**
	 * Required parameters constructor
	 */
	public PreferenceType(java.lang.String name) {
		_Name = name;
	}

	/**
	 * Deep copy
	 */
	public PreferenceType(org.netbeans.modules.visualweb.api.portlet.dd.PreferenceType source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public PreferenceType(org.netbeans.modules.visualweb.api.portlet.dd.PreferenceType source, boolean justData) {
		_Id = source._Id;
		_Name = source._Name;
		for (java.util.Iterator it = source._Value.iterator();
			it.hasNext(); ) {
			java.lang.String srcElement = (java.lang.String)it.next();
			_Value.add(srcElement);
		}
		_ReadOnly = source._ReadOnly;
	}

	// This attribute is optional
	public void setId(java.lang.String value) {
		_Id = value;
	}

	public java.lang.String getId() {
		return _Id;
	}

	// This attribute is mandatory
	public void setName(java.lang.String value) {
		_Name = value;
	}

	public java.lang.String getName() {
		return _Name;
	}

	// This attribute is an array, possibly empty
	public void setValue(java.lang.String[] value) {
		if (value == null)
			value = new java.lang.String[0];
		_Value.clear();
		((java.util.ArrayList) _Value).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_Value.add(value[i]);
		}
	}

	public void setValue(int index, java.lang.String value) {
		_Value.set(index, value);
	}

	public java.lang.String[] getValue() {
		java.lang.String[] arr = new java.lang.String[_Value.size()];
		return (java.lang.String[]) _Value.toArray(arr);
	}

	public java.util.List fetchValueList() {
		return _Value;
	}

	public java.lang.String getValue(int index) {
		return (java.lang.String)_Value.get(index);
	}

	// Return the number of value
	public int sizeValue() {
		return _Value.size();
	}

	public int addValue(java.lang.String value) {
		_Value.add(value);
		int positionOfNewItem = _Value.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeValue(java.lang.String value) {
		int pos = _Value.indexOf(value);
		if (pos >= 0) {
			_Value.remove(pos);
		}
		return pos;
	}

	// This attribute is optional
	public void setReadOnly(java.lang.String value) {
		_ReadOnly = value;
	}

	public java.lang.String getReadOnly() {
		return _ReadOnly;
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "preferenceType";
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
		// id is an attribute with namespace http://java.sun.com/xml/ns/portlet/portlet-app_1_0.xsd
		if (_Id != null) {
			out.write(" id='");
			org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.writeXML(out, _Id, true);
			out.write("'");	// NOI18N
		}
		out.write(">\n");
		String nextIndent = indent + "	";
		if (_Name != null) {
			out.write(nextIndent);
			out.write("<name");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.writeXML(out, _Name, false);
			out.write("</name>\n");	// NOI18N
		}
		for (java.util.Iterator it = _Value.iterator(); it.hasNext(); ) {
			java.lang.String element = (java.lang.String)it.next();
			if (element != null) {
				out.write(nextIndent);
				out.write("<value");	// NOI18N
				out.write(">");	// NOI18N
				org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.writeXML(out, element, false);
				out.write("</value>\n");	// NOI18N
			}
		}
		if (_ReadOnly != null) {
			out.write(nextIndent);
			out.write("<read-only");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.writeXML(out, _ReadOnly, false);
			out.write("</read-only>\n");	// NOI18N
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
			attr = (org.w3c.dom.Attr) attrs.getNamedItem("id");
			if (attr != null) {
				attrValue = attr.getValue();
				_Id = attrValue;
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
			if (childNodeName == "name") {
				_Name = childNodeValue;
			}
			else if (childNodeName == "value") {
				java.lang.String aValue;
				aValue = childNodeValue;
				_Value.add(aValue);
			}
			else if (childNodeName == "read-only") {
				_ReadOnly = childNodeValue;
			}
			else {
				// Found extra unrecognized childNode
			}
		}
	}

	public void validate() throws org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException {
		boolean restrictionFailure = false;
		boolean restrictionPassed = false;
		// Validating property id
		// Validating property name
		if (getName() == null) {
			throw new org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException("getName() == null", org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException.FailureType.NULL_VALUE, "name", this);	// NOI18N
		}
		// Validating property value
		// Validating property readOnly
		if (getReadOnly() != null) {
			final java.lang.String[] enumRestrictionReadOnly = {"true", "false"};
			restrictionFailure = true;
			for (int _index2 = 0; 
				_index2 < enumRestrictionReadOnly.length; ++_index2) {
				if (enumRestrictionReadOnly[_index2].equals(getReadOnly())) {
					restrictionFailure = false;
					break;
				}
			}
			if (restrictionFailure) {
				throw new org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException("getReadOnly() enumeration test", org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException.FailureType.ENUM_RESTRICTION, "readOnly", this);	// NOI18N
			}
		}
	}

	public void changePropertyByName(String name, Object value) {
		if (name == null) return;
		name = name.intern();
		if (name == "id")
			setId((java.lang.String)value);
		else if (name == "name")
			setName((java.lang.String)value);
		else if (name == "value")
			addValue((java.lang.String)value);
		else if (name == "value[]")
			setValue((java.lang.String[]) value);
		else if (name == "readOnly")
			setReadOnly((java.lang.String)value);
		else
			throw new IllegalArgumentException(name+" is not a valid property name for PreferenceType");
	}

	public Object fetchPropertyByName(String name) {
		if (name == "id")
			return getId();
		if (name == "name")
			return getName();
		if (name == "value[]")
			return getValue();
		if (name == "readOnly")
			return getReadOnly();
		throw new IllegalArgumentException(name+" is not a valid property name for PreferenceType");
	}

	public String nameSelf() {
		return "PreferenceType";
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
			if (child == _Id) {
				if (returnConstName) {
					return ID;
				} else if (returnSchemaName) {
					return "id";
				} else if (returnXPathName) {
					return "@id";
				} else {
					return "Id";
				}
			}
			if (child == _Name) {
				if (returnConstName) {
					return NAME;
				} else if (returnSchemaName) {
					return "name";
				} else if (returnXPathName) {
					return "name";
				} else {
					return "Name";
				}
			}
			int index = 0;
			for (java.util.Iterator it = _Value.iterator(); it.hasNext(); 
				) {
				java.lang.String element = (java.lang.String)it.next();
				if (child == element) {
					if (returnConstName) {
						return VALUE;
					} else if (returnSchemaName) {
						return "value";
					} else if (returnXPathName) {
						return "value[position()="+index+"]";
					} else {
						return "Value."+Integer.toHexString(index);
					}
				}
				++index;
			}
			if (child == _ReadOnly) {
				if (returnConstName) {
					return READ_ONLY;
				} else if (returnSchemaName) {
					return "read-only";
				} else if (returnXPathName) {
					return "read-only";
				} else {
					return "ReadOnly";
				}
			}
		}
		return null;
	}

	/**
	 * Return an array of all of the properties that are beans and are set.
	 */
	public org.netbeans.modules.visualweb.api.portlet.dd.CommonBean[] childBeans(boolean recursive) {
		java.util.List children = new java.util.LinkedList();
		childBeans(recursive, children);
		org.netbeans.modules.visualweb.api.portlet.dd.CommonBean[] result = new org.netbeans.modules.visualweb.api.portlet.dd.CommonBean[children.size()];
		return (org.netbeans.modules.visualweb.api.portlet.dd.CommonBean[]) children.toArray(result);
	}

	/**
	 * Put all child beans into the beans list.
	 */
	public void childBeans(boolean recursive, java.util.List beans) {
	}

	public boolean equals(Object o) {
		return o instanceof org.netbeans.modules.visualweb.api.portlet.dd.PreferenceType && equals((org.netbeans.modules.visualweb.api.portlet.dd.PreferenceType) o);
	}

	public boolean equals(org.netbeans.modules.visualweb.api.portlet.dd.PreferenceType inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (!(_Id == null ? inst._Id == null : _Id.equals(inst._Id))) {
			return false;
		}
		if (!(_Name == null ? inst._Name == null : _Name.equals(inst._Name))) {
			return false;
		}
		if (sizeValue() != inst.sizeValue())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _Value.iterator(), it2 = inst._Value.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			java.lang.String element = (java.lang.String)it.next();
			java.lang.String element2 = (java.lang.String)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		if (!(_ReadOnly == null ? inst._ReadOnly == null : _ReadOnly.equals(inst._ReadOnly))) {
			return false;
		}
		return true;
	}

	public int hashCode() {
		int result = 17;
		result = 37*result + (_Id == null ? 0 : _Id.hashCode());
		result = 37*result + (_Name == null ? 0 : _Name.hashCode());
		result = 37*result + (_Value == null ? 0 : _Value.hashCode());
		result = 37*result + (_ReadOnly == null ? 0 : _ReadOnly.hashCode());
		return result;
	}

	public String toString() {
		java.io.StringWriter sw = new java.io.StringWriter();
		try {
			writeNode(sw);
		} catch (java.io.IOException e) {
			// How can we actually get an IOException on a StringWriter?
			throw new RuntimeException(e);
		}
		return sw.toString();
	}

}

