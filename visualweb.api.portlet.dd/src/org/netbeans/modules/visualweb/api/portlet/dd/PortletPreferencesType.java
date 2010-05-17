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
 *	This generated bean class PortletPreferencesType
 *	matches the schema element 'portlet-preferencesType'.
 *  The root bean class is PortletApp
 *
 *	===============================================================
 *	
 *				Portlet persistent preference store.
 *				Used in: portlet
 *				
 *	===============================================================
 *	Generated on Tue Apr 26 19:08:25 MDT 2005
 * @Generated
 */

package org.netbeans.modules.visualweb.api.portlet.dd;

public class PortletPreferencesType implements org.netbeans.modules.visualweb.api.portlet.dd.PortletPreferencesTypeInterface, org.netbeans.modules.visualweb.api.portlet.dd.CommonBean {
	public static final String ID = "Id";	// NOI18N
	public static final String PREFERENCE = "Preference";	// NOI18N
	public static final String PREFERENCES_VALIDATOR = "PreferencesValidator";	// NOI18N

	private java.lang.String _Id;
	private java.util.List _Preference = new java.util.ArrayList();	// List<PreferenceType>
	private java.lang.String _PreferencesValidator;

	/**
	 * Normal starting point constructor.
	 */
	public PortletPreferencesType() {
	}

	/**
	 * Deep copy
	 */
	public PortletPreferencesType(org.netbeans.modules.visualweb.api.portlet.dd.PortletPreferencesType source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public PortletPreferencesType(org.netbeans.modules.visualweb.api.portlet.dd.PortletPreferencesType source, boolean justData) {
		_Id = source._Id;
		for (java.util.Iterator it = source._Preference.iterator();
			it.hasNext(); ) {
			org.netbeans.modules.visualweb.api.portlet.dd.PreferenceType srcElement = (org.netbeans.modules.visualweb.api.portlet.dd.PreferenceType)it.next();
			_Preference.add((srcElement == null) ? null : newPreferenceType(srcElement, justData));
		}
		_PreferencesValidator = source._PreferencesValidator;
	}

	// This attribute is optional
	public void setId(java.lang.String value) {
		_Id = value;
	}

	public java.lang.String getId() {
		return _Id;
	}

	// This attribute is an array, possibly empty
	public void setPreference(org.netbeans.modules.visualweb.api.portlet.dd.PreferenceType[] value) {
		if (value == null)
			value = new PreferenceType[0];
		_Preference.clear();
		((java.util.ArrayList) _Preference).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_Preference.add(value[i]);
		}
	}

	public void setPreference(int index, org.netbeans.modules.visualweb.api.portlet.dd.PreferenceType value) {
		_Preference.set(index, value);
	}

	public org.netbeans.modules.visualweb.api.portlet.dd.PreferenceType[] getPreference() {
		PreferenceType[] arr = new PreferenceType[_Preference.size()];
		return (PreferenceType[]) _Preference.toArray(arr);
	}

	public java.util.List fetchPreferenceList() {
		return _Preference;
	}

	public org.netbeans.modules.visualweb.api.portlet.dd.PreferenceType getPreference(int index) {
		return (PreferenceType)_Preference.get(index);
	}

	// Return the number of preference
	public int sizePreference() {
		return _Preference.size();
	}

	public int addPreference(org.netbeans.modules.visualweb.api.portlet.dd.PreferenceType value) {
		_Preference.add(value);
		int positionOfNewItem = _Preference.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removePreference(org.netbeans.modules.visualweb.api.portlet.dd.PreferenceType value) {
		int pos = _Preference.indexOf(value);
		if (pos >= 0) {
			_Preference.remove(pos);
		}
		return pos;
	}

	// This attribute is optional
	public void setPreferencesValidator(java.lang.String value) {
		_PreferencesValidator = value;
	}

	public java.lang.String getPreferencesValidator() {
		return _PreferencesValidator;
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.visualweb.api.portlet.dd.PreferenceType newPreferenceType() {
		return new org.netbeans.modules.visualweb.api.portlet.dd.PreferenceType();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.visualweb.api.portlet.dd.PreferenceType newPreferenceType(PreferenceType source, boolean justData) {
		return new org.netbeans.modules.visualweb.api.portlet.dd.PreferenceType(source, justData);
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "portlet-preferencesType";
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
		for (java.util.Iterator it = _Preference.iterator(); it.hasNext(); 
			) {
			org.netbeans.modules.visualweb.api.portlet.dd.PreferenceType element = (org.netbeans.modules.visualweb.api.portlet.dd.PreferenceType)it.next();
			if (element != null) {
				element.writeNode(out, "preference", null, nextIndent, namespaceMap);
			}
		}
		if (_PreferencesValidator != null) {
			out.write(nextIndent);
			out.write("<preferences-validator");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.writeXML(out, _PreferencesValidator, false);
			out.write("</preferences-validator>\n");	// NOI18N
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
			if (childNodeName == "preference") {
				PreferenceType aPreference = newPreferenceType();
				aPreference.readNode(childNode, namespacePrefixes);
				_Preference.add(aPreference);
			}
			else if (childNodeName == "preferences-validator") {
				_PreferencesValidator = childNodeValue;
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
		// Validating property preference
		for (int _index = 0; _index < sizePreference(); ++_index) {
			org.netbeans.modules.visualweb.api.portlet.dd.PreferenceType element = getPreference(_index);
			if (element != null) {
				element.validate();
			}
		}
		// Validating property preferencesValidator
	}

	public void changePropertyByName(String name, Object value) {
		if (name == null) return;
		name = name.intern();
		if (name == "id")
			setId((java.lang.String)value);
		else if (name == "preference")
			addPreference((PreferenceType)value);
		else if (name == "preference[]")
			setPreference((PreferenceType[]) value);
		else if (name == "preferencesValidator")
			setPreferencesValidator((java.lang.String)value);
		else
			throw new IllegalArgumentException(name+" is not a valid property name for PortletPreferencesType");
	}

	public Object fetchPropertyByName(String name) {
		if (name == "id")
			return getId();
		if (name == "preference[]")
			return getPreference();
		if (name == "preferencesValidator")
			return getPreferencesValidator();
		throw new IllegalArgumentException(name+" is not a valid property name for PortletPreferencesType");
	}

	public String nameSelf() {
		return "PortletPreferencesType";
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
			if (child == _PreferencesValidator) {
				if (returnConstName) {
					return PREFERENCES_VALIDATOR;
				} else if (returnSchemaName) {
					return "preferences-validator";
				} else if (returnXPathName) {
					return "preferences-validator";
				} else {
					return "PreferencesValidator";
				}
			}
		}
		if (childObj instanceof PreferenceType) {
			PreferenceType child = (PreferenceType) childObj;
			int index = 0;
			for (java.util.Iterator it = _Preference.iterator(); 
				it.hasNext(); ) {
				org.netbeans.modules.visualweb.api.portlet.dd.PreferenceType element = (org.netbeans.modules.visualweb.api.portlet.dd.PreferenceType)it.next();
				if (child == element) {
					if (returnConstName) {
						return PREFERENCE;
					} else if (returnSchemaName) {
						return "preference";
					} else if (returnXPathName) {
						return "preference[position()="+index+"]";
					} else {
						return "Preference."+Integer.toHexString(index);
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
		for (java.util.Iterator it = _Preference.iterator(); it.hasNext(); 
			) {
			org.netbeans.modules.visualweb.api.portlet.dd.PreferenceType element = (org.netbeans.modules.visualweb.api.portlet.dd.PreferenceType)it.next();
			if (element != null) {
				if (recursive) {
					element.childBeans(true, beans);
				}
				beans.add(element);
			}
		}
	}

	public boolean equals(Object o) {
		return o instanceof org.netbeans.modules.visualweb.api.portlet.dd.PortletPreferencesType && equals((org.netbeans.modules.visualweb.api.portlet.dd.PortletPreferencesType) o);
	}

	public boolean equals(org.netbeans.modules.visualweb.api.portlet.dd.PortletPreferencesType inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (!(_Id == null ? inst._Id == null : _Id.equals(inst._Id))) {
			return false;
		}
		if (sizePreference() != inst.sizePreference())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _Preference.iterator(), it2 = inst._Preference.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			org.netbeans.modules.visualweb.api.portlet.dd.PreferenceType element = (org.netbeans.modules.visualweb.api.portlet.dd.PreferenceType)it.next();
			org.netbeans.modules.visualweb.api.portlet.dd.PreferenceType element2 = (org.netbeans.modules.visualweb.api.portlet.dd.PreferenceType)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		if (!(_PreferencesValidator == null ? inst._PreferencesValidator == null : _PreferencesValidator.equals(inst._PreferencesValidator))) {
			return false;
		}
		return true;
	}

	public int hashCode() {
		int result = 17;
		result = 37*result + (_Id == null ? 0 : _Id.hashCode());
		result = 37*result + (_Preference == null ? 0 : _Preference.hashCode());
		result = 37*result + (_PreferencesValidator == null ? 0 : _PreferencesValidator.hashCode());
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

