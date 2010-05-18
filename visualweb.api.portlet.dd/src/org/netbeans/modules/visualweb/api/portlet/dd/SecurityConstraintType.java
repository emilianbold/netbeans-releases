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
 *	This generated bean class SecurityConstraintType
 *	matches the schema element 'security-constraintType'.
 *  The root bean class is PortletApp
 *
 *	===============================================================
 *	
 *				The security-constraintType is used to associate
 *				intended security constraints with one or more portlets.
 *				Used in: portlet-app
 *				
 *	===============================================================
 *	Generated on Tue Apr 26 19:08:25 MDT 2005
 * @Generated
 */

package org.netbeans.modules.visualweb.api.portlet.dd;

public class SecurityConstraintType implements org.netbeans.modules.visualweb.api.portlet.dd.SecurityConstraintTypeInterface, org.netbeans.modules.visualweb.api.portlet.dd.CommonBean {
	public static final String ID = "Id";	// NOI18N
	public static final String DISPLAY_NAME = "DisplayName";	// NOI18N
	public static final String DISPLAYNAMEXMLLANG = "DisplayNameXmlLang";	// NOI18N
	public static final String PORTLET_COLLECTION = "PortletCollection";	// NOI18N
	public static final String USER_DATA_CONSTRAINT = "UserDataConstraint";	// NOI18N

	private java.lang.String _Id;
	private java.util.List _DisplayName = new java.util.ArrayList();	// List<java.lang.String>
	private java.util.List _DisplayNameXmlLang = new java.util.ArrayList();	// List<java.lang.String>
	private PortletCollectionType _PortletCollection;
	private UserDataConstraintType _UserDataConstraint;

	/**
	 * Normal starting point constructor.
	 */
	public SecurityConstraintType() {
		_PortletCollection = newPortletCollectionType();
		_UserDataConstraint = newUserDataConstraintType();
	}

	/**
	 * Required parameters constructor
	 */
	public SecurityConstraintType(org.netbeans.modules.visualweb.api.portlet.dd.PortletCollectionType portletCollection, org.netbeans.modules.visualweb.api.portlet.dd.UserDataConstraintType userDataConstraint) {
		_PortletCollection = portletCollection;
		_UserDataConstraint = userDataConstraint;
	}

	/**
	 * Deep copy
	 */
	public SecurityConstraintType(org.netbeans.modules.visualweb.api.portlet.dd.SecurityConstraintType source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public SecurityConstraintType(org.netbeans.modules.visualweb.api.portlet.dd.SecurityConstraintType source, boolean justData) {
		_Id = source._Id;
		for (java.util.Iterator it = source._DisplayName.iterator();
			it.hasNext(); ) {
			java.lang.String srcElement = (java.lang.String)it.next();
			_DisplayName.add(srcElement);
		}
		for (java.util.Iterator it = source._DisplayNameXmlLang.iterator();
			it.hasNext(); ) {
			java.lang.String srcElement = (java.lang.String)it.next();
			_DisplayNameXmlLang.add(srcElement);
		}
		_PortletCollection = (source._PortletCollection == null) ? null : newPortletCollectionType(source._PortletCollection, justData);
		_UserDataConstraint = (source._UserDataConstraint == null) ? null : newUserDataConstraintType(source._UserDataConstraint, justData);
	}

	// This attribute is optional
	public void setId(java.lang.String value) {
		_Id = value;
	}

	public java.lang.String getId() {
		return _Id;
	}

	// This attribute is an array, possibly empty
	public void setDisplayName(java.lang.String[] value) {
		if (value == null)
			value = new java.lang.String[0];
		_DisplayName.clear();
		((java.util.ArrayList) _DisplayName).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_DisplayName.add(value[i]);
		}
	}

	public void setDisplayName(int index, java.lang.String value) {
		_DisplayName.set(index, value);
	}

	public java.lang.String[] getDisplayName() {
		java.lang.String[] arr = new java.lang.String[_DisplayName.size()];
		return (java.lang.String[]) _DisplayName.toArray(arr);
	}

	public java.util.List fetchDisplayNameList() {
		return _DisplayName;
	}

	public java.lang.String getDisplayName(int index) {
		return (java.lang.String)_DisplayName.get(index);
	}

	// Return the number of displayName
	public int sizeDisplayName() {
		return _DisplayName.size();
	}

	public int addDisplayName(java.lang.String value) {
		_DisplayName.add(value);
		int positionOfNewItem = _DisplayName.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeDisplayName(java.lang.String value) {
		int pos = _DisplayName.indexOf(value);
		if (pos >= 0) {
			_DisplayName.remove(pos);
		}
		return pos;
	}

	// This attribute is an array, possibly empty
	public void setDisplayNameXmlLang(java.lang.String[] value) {
		if (value == null)
			value = new java.lang.String[0];
		_DisplayNameXmlLang.clear();
		((java.util.ArrayList) _DisplayNameXmlLang).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_DisplayNameXmlLang.add(value[i]);
		}
	}

	public void setDisplayNameXmlLang(int index, java.lang.String value) {
		for (int size = _DisplayNameXmlLang.size(); index >= size; ++size) {
			_DisplayNameXmlLang.add(null);
		}
		_DisplayNameXmlLang.set(index, value);
	}

	public java.lang.String[] getDisplayNameXmlLang() {
		java.lang.String[] arr = new java.lang.String[_DisplayNameXmlLang.size()];
		return (java.lang.String[]) _DisplayNameXmlLang.toArray(arr);
	}

	public java.util.List fetchDisplayNameXmlLangList() {
		return _DisplayNameXmlLang;
	}

	public java.lang.String getDisplayNameXmlLang(int index) {
		return (java.lang.String)_DisplayNameXmlLang.get(index);
	}

	// Return the number of displayNameXmlLang
	public int sizeDisplayNameXmlLang() {
		return _DisplayNameXmlLang.size();
	}

	public int addDisplayNameXmlLang(java.lang.String value) {
		_DisplayNameXmlLang.add(value);
		int positionOfNewItem = _DisplayNameXmlLang.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeDisplayNameXmlLang(java.lang.String value) {
		int pos = _DisplayNameXmlLang.indexOf(value);
		if (pos >= 0) {
			_DisplayNameXmlLang.remove(pos);
		}
		return pos;
	}

	// This attribute is mandatory
	public void setPortletCollection(org.netbeans.modules.visualweb.api.portlet.dd.PortletCollectionType value) {
		_PortletCollection = value;
	}

	public org.netbeans.modules.visualweb.api.portlet.dd.PortletCollectionType getPortletCollection() {
		return _PortletCollection;
	}

	// This attribute is mandatory
	public void setUserDataConstraint(org.netbeans.modules.visualweb.api.portlet.dd.UserDataConstraintType value) {
		_UserDataConstraint = value;
	}

	public org.netbeans.modules.visualweb.api.portlet.dd.UserDataConstraintType getUserDataConstraint() {
		return _UserDataConstraint;
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.visualweb.api.portlet.dd.PortletCollectionType newPortletCollectionType() {
		return new org.netbeans.modules.visualweb.api.portlet.dd.PortletCollectionType();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.visualweb.api.portlet.dd.PortletCollectionType newPortletCollectionType(PortletCollectionType source, boolean justData) {
		return new org.netbeans.modules.visualweb.api.portlet.dd.PortletCollectionType(source, justData);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.visualweb.api.portlet.dd.UserDataConstraintType newUserDataConstraintType() {
		return new org.netbeans.modules.visualweb.api.portlet.dd.UserDataConstraintType();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.visualweb.api.portlet.dd.UserDataConstraintType newUserDataConstraintType(UserDataConstraintType source, boolean justData) {
		return new org.netbeans.modules.visualweb.api.portlet.dd.UserDataConstraintType(source, justData);
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "security-constraintType";
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
		int index = 0;
		for (java.util.Iterator it = _DisplayName.iterator(); 
			it.hasNext(); ) {
			java.lang.String element = (java.lang.String)it.next();
			if (element != null) {
				out.write(nextIndent);
				out.write("<display-name");	// NOI18N
				if (index < sizeDisplayNameXmlLang()) {
					// xml:lang is an attribute with namespace http://www.w3.org/XML/1998/namespace
					if (getDisplayNameXmlLang(index) != null) {
						out.write(" xml:lang='");
						org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.writeXML(out, getDisplayNameXmlLang(index), true);
						out.write("'");	// NOI18N
					}
				}
				out.write(">");	// NOI18N
				org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.writeXML(out, element, false);
				out.write("</display-name>\n");	// NOI18N
			}
			++index;
		}
		if (_PortletCollection != null) {
			_PortletCollection.writeNode(out, "portlet-collection", null, nextIndent, namespaceMap);
		}
		if (_UserDataConstraint != null) {
			_UserDataConstraint.writeNode(out, "user-data-constraint", null, nextIndent, namespaceMap);
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
			org.w3c.dom.NamedNodeMap attrs = childNode.getAttributes();
			org.w3c.dom.Attr attr;
			java.lang.String attrValue;
			if (childNodeName == "display-name") {
				java.lang.String aDisplayName;
				aDisplayName = childNodeValue;
				attr = (org.w3c.dom.Attr) attrs.getNamedItem("xml:lang");
				if (attr != null) {
					attrValue = attr.getValue();
				} else {
					attrValue = null;
				}
				java.lang.String processedValueFor_DisplayNameXmlLang;
				processedValueFor_DisplayNameXmlLang = attrValue;
				addDisplayNameXmlLang(processedValueFor_DisplayNameXmlLang);
				_DisplayName.add(aDisplayName);
			}
			else if (childNodeName == "portlet-collection") {
				_PortletCollection = newPortletCollectionType();
				_PortletCollection.readNode(childNode, namespacePrefixes);
			}
			else if (childNodeName == "user-data-constraint") {
				_UserDataConstraint = newUserDataConstraintType();
				_UserDataConstraint.readNode(childNode, namespacePrefixes);
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
		// Validating property displayName
		for (int _index = 0; _index < sizeDisplayName(); ++_index) {
			java.lang.String element = getDisplayName(_index);
			if (element != null) {
				// has whitespace restriction
				if (restrictionFailure) {
					throw new org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException("element whiteSpace (collapse)", org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException.FailureType.DATA_RESTRICTION, "displayName", this);	// NOI18N
				}
			}
		}
		// Validating property displayNameXmlLang
		// Validating property portletCollection
		if (getPortletCollection() == null) {
			throw new org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException("getPortletCollection() == null", org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException.FailureType.NULL_VALUE, "portletCollection", this);	// NOI18N
		}
		getPortletCollection().validate();
		// Validating property userDataConstraint
		if (getUserDataConstraint() == null) {
			throw new org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException("getUserDataConstraint() == null", org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException.FailureType.NULL_VALUE, "userDataConstraint", this);	// NOI18N
		}
		getUserDataConstraint().validate();
	}

	public void changePropertyByName(String name, Object value) {
		if (name == null) return;
		name = name.intern();
		if (name == "id")
			setId((java.lang.String)value);
		else if (name == "displayName")
			addDisplayName((java.lang.String)value);
		else if (name == "displayName[]")
			setDisplayName((java.lang.String[]) value);
		else if (name == "displayNameXmlLang")
			addDisplayNameXmlLang((java.lang.String)value);
		else if (name == "displayNameXmlLang[]")
			setDisplayNameXmlLang((java.lang.String[]) value);
		else if (name == "portletCollection")
			setPortletCollection((PortletCollectionType)value);
		else if (name == "userDataConstraint")
			setUserDataConstraint((UserDataConstraintType)value);
		else
			throw new IllegalArgumentException(name+" is not a valid property name for SecurityConstraintType");
	}

	public Object fetchPropertyByName(String name) {
		if (name == "id")
			return getId();
		if (name == "displayName[]")
			return getDisplayName();
		if (name == "displayNameXmlLang[]")
			return getDisplayNameXmlLang();
		if (name == "portletCollection")
			return getPortletCollection();
		if (name == "userDataConstraint")
			return getUserDataConstraint();
		throw new IllegalArgumentException(name+" is not a valid property name for SecurityConstraintType");
	}

	public String nameSelf() {
		return "SecurityConstraintType";
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
			int index = 0;
			for (java.util.Iterator it = _DisplayName.iterator(); 
				it.hasNext(); ) {
				java.lang.String element = (java.lang.String)it.next();
				if (child == element) {
					if (returnConstName) {
						return DISPLAY_NAME;
					} else if (returnSchemaName) {
						return "display-name";
					} else if (returnXPathName) {
						return "display-name[position()="+index+"]";
					} else {
						return "DisplayName."+Integer.toHexString(index);
					}
				}
				++index;
			}
			index = 0;
			for (java.util.Iterator it = _DisplayNameXmlLang.iterator(); 
				it.hasNext(); ) {
				java.lang.String element = (java.lang.String)it.next();
				if (child == element) {
					if (returnConstName) {
						return DISPLAYNAMEXMLLANG;
					} else if (returnSchemaName) {
						return "xml:lang";
					} else if (returnXPathName) {
						return "@xml:lang[position()="+index+"]";
					} else {
						return "DisplayNameXmlLang."+Integer.toHexString(index);
					}
				}
				++index;
			}
		}
		if (childObj instanceof UserDataConstraintType) {
			UserDataConstraintType child = (UserDataConstraintType) childObj;
			if (child == _UserDataConstraint) {
				if (returnConstName) {
					return USER_DATA_CONSTRAINT;
				} else if (returnSchemaName) {
					return "user-data-constraint";
				} else if (returnXPathName) {
					return "user-data-constraint";
				} else {
					return "UserDataConstraint";
				}
			}
		}
		if (childObj instanceof PortletCollectionType) {
			PortletCollectionType child = (PortletCollectionType) childObj;
			if (child == _PortletCollection) {
				if (returnConstName) {
					return PORTLET_COLLECTION;
				} else if (returnSchemaName) {
					return "portlet-collection";
				} else if (returnXPathName) {
					return "portlet-collection";
				} else {
					return "PortletCollection";
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
		if (_PortletCollection != null) {
			if (recursive) {
				_PortletCollection.childBeans(true, beans);
			}
			beans.add(_PortletCollection);
		}
		if (_UserDataConstraint != null) {
			if (recursive) {
				_UserDataConstraint.childBeans(true, beans);
			}
			beans.add(_UserDataConstraint);
		}
	}

	public boolean equals(Object o) {
		return o instanceof org.netbeans.modules.visualweb.api.portlet.dd.SecurityConstraintType && equals((org.netbeans.modules.visualweb.api.portlet.dd.SecurityConstraintType) o);
	}

	public boolean equals(org.netbeans.modules.visualweb.api.portlet.dd.SecurityConstraintType inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (!(_Id == null ? inst._Id == null : _Id.equals(inst._Id))) {
			return false;
		}
		if (sizeDisplayName() != inst.sizeDisplayName())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _DisplayName.iterator(), it2 = inst._DisplayName.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			java.lang.String element = (java.lang.String)it.next();
			java.lang.String element2 = (java.lang.String)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		if (sizeDisplayNameXmlLang() != inst.sizeDisplayNameXmlLang())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _DisplayNameXmlLang.iterator(), it2 = inst._DisplayNameXmlLang.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			java.lang.String element = (java.lang.String)it.next();
			java.lang.String element2 = (java.lang.String)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		if (!(_PortletCollection == null ? inst._PortletCollection == null : _PortletCollection.equals(inst._PortletCollection))) {
			return false;
		}
		if (!(_UserDataConstraint == null ? inst._UserDataConstraint == null : _UserDataConstraint.equals(inst._UserDataConstraint))) {
			return false;
		}
		return true;
	}

	public int hashCode() {
		int result = 17;
		result = 37*result + (_Id == null ? 0 : _Id.hashCode());
		result = 37*result + (_DisplayName == null ? 0 : _DisplayName.hashCode());
		result = 37*result + (_DisplayNameXmlLang == null ? 0 : _DisplayNameXmlLang.hashCode());
		result = 37*result + (_PortletCollection == null ? 0 : _PortletCollection.hashCode());
		result = 37*result + (_UserDataConstraint == null ? 0 : _UserDataConstraint.hashCode());
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

