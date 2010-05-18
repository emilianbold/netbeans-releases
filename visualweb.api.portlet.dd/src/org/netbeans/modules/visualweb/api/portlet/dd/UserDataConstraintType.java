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
 *	This generated bean class UserDataConstraintType
 *	matches the schema element 'user-data-constraintType'.
 *  The root bean class is PortletApp
 *
 *	===============================================================
 *	
 *				The user-data-constraintType is used to indicate how
 *				data communicated between the client and portlet should be
 *				protected.
 *				Used in: security-constraint
 *				
 *	===============================================================
 *	Generated on Tue Apr 26 19:08:25 MDT 2005
 * @Generated
 */

package org.netbeans.modules.visualweb.api.portlet.dd;

public class UserDataConstraintType implements org.netbeans.modules.visualweb.api.portlet.dd.UserDataConstraintTypeInterface, org.netbeans.modules.visualweb.api.portlet.dd.CommonBean {
	public static final String ID = "Id";	// NOI18N
	public static final String DESCRIPTION = "Description";	// NOI18N
	public static final String DESCRIPTIONXMLLANG = "DescriptionXmlLang";	// NOI18N
	public static final String TRANSPORT_GUARANTEE = "TransportGuarantee";	// NOI18N

	private java.lang.String _Id;
	private java.util.List _Description = new java.util.ArrayList();	// List<java.lang.String>
	private java.util.List _DescriptionXmlLang = new java.util.ArrayList();	// List<java.lang.String>
	private java.lang.String _TransportGuarantee = "NONE";

	/**
	 * Normal starting point constructor.
	 */
	public UserDataConstraintType() {
	}

	/**
	 * Required parameters constructor
	 */
	public UserDataConstraintType(java.lang.String transportGuarantee) {
		_TransportGuarantee = transportGuarantee;
	}

	/**
	 * Deep copy
	 */
	public UserDataConstraintType(org.netbeans.modules.visualweb.api.portlet.dd.UserDataConstraintType source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public UserDataConstraintType(org.netbeans.modules.visualweb.api.portlet.dd.UserDataConstraintType source, boolean justData) {
		_Id = source._Id;
		for (java.util.Iterator it = source._Description.iterator();
			it.hasNext(); ) {
			java.lang.String srcElement = (java.lang.String)it.next();
			_Description.add(srcElement);
		}
		for (java.util.Iterator it = source._DescriptionXmlLang.iterator();
			it.hasNext(); ) {
			java.lang.String srcElement = (java.lang.String)it.next();
			_DescriptionXmlLang.add(srcElement);
		}
		_TransportGuarantee = source._TransportGuarantee;
	}

	// This attribute is optional
	public void setId(java.lang.String value) {
		_Id = value;
	}

	public java.lang.String getId() {
		return _Id;
	}

	// This attribute is an array, possibly empty
	public void setDescription(java.lang.String[] value) {
		if (value == null)
			value = new java.lang.String[0];
		_Description.clear();
		((java.util.ArrayList) _Description).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_Description.add(value[i]);
		}
	}

	public void setDescription(int index, java.lang.String value) {
		_Description.set(index, value);
	}

	public java.lang.String[] getDescription() {
		java.lang.String[] arr = new java.lang.String[_Description.size()];
		return (java.lang.String[]) _Description.toArray(arr);
	}

	public java.util.List fetchDescriptionList() {
		return _Description;
	}

	public java.lang.String getDescription(int index) {
		return (java.lang.String)_Description.get(index);
	}

	// Return the number of description
	public int sizeDescription() {
		return _Description.size();
	}

	public int addDescription(java.lang.String value) {
		_Description.add(value);
		int positionOfNewItem = _Description.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeDescription(java.lang.String value) {
		int pos = _Description.indexOf(value);
		if (pos >= 0) {
			_Description.remove(pos);
		}
		return pos;
	}

	// This attribute is an array, possibly empty
	public void setDescriptionXmlLang(java.lang.String[] value) {
		if (value == null)
			value = new java.lang.String[0];
		_DescriptionXmlLang.clear();
		((java.util.ArrayList) _DescriptionXmlLang).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_DescriptionXmlLang.add(value[i]);
		}
	}

	public void setDescriptionXmlLang(int index, java.lang.String value) {
		for (int size = _DescriptionXmlLang.size(); index >= size; ++size) {
			_DescriptionXmlLang.add(null);
		}
		_DescriptionXmlLang.set(index, value);
	}

	public java.lang.String[] getDescriptionXmlLang() {
		java.lang.String[] arr = new java.lang.String[_DescriptionXmlLang.size()];
		return (java.lang.String[]) _DescriptionXmlLang.toArray(arr);
	}

	public java.util.List fetchDescriptionXmlLangList() {
		return _DescriptionXmlLang;
	}

	public java.lang.String getDescriptionXmlLang(int index) {
		return (java.lang.String)_DescriptionXmlLang.get(index);
	}

	// Return the number of descriptionXmlLang
	public int sizeDescriptionXmlLang() {
		return _DescriptionXmlLang.size();
	}

	public int addDescriptionXmlLang(java.lang.String value) {
		_DescriptionXmlLang.add(value);
		int positionOfNewItem = _DescriptionXmlLang.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeDescriptionXmlLang(java.lang.String value) {
		int pos = _DescriptionXmlLang.indexOf(value);
		if (pos >= 0) {
			_DescriptionXmlLang.remove(pos);
		}
		return pos;
	}

	// This attribute is mandatory
	public void setTransportGuarantee(java.lang.String value) {
		_TransportGuarantee = value;
	}

	public java.lang.String getTransportGuarantee() {
		return _TransportGuarantee;
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "user-data-constraintType";
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
		for (java.util.Iterator it = _Description.iterator(); 
			it.hasNext(); ) {
			java.lang.String element = (java.lang.String)it.next();
			if (element != null) {
				out.write(nextIndent);
				out.write("<description");	// NOI18N
				if (index < sizeDescriptionXmlLang()) {
					// xml:lang is an attribute with namespace http://www.w3.org/XML/1998/namespace
					if (getDescriptionXmlLang(index) != null) {
						out.write(" xml:lang='");
						org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.writeXML(out, getDescriptionXmlLang(index), true);
						out.write("'");	// NOI18N
					}
				}
				out.write(">");	// NOI18N
				org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.writeXML(out, element, false);
				out.write("</description>\n");	// NOI18N
			}
			++index;
		}
		if (_TransportGuarantee != null) {
			out.write(nextIndent);
			out.write("<transport-guarantee");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.writeXML(out, _TransportGuarantee, false);
			out.write("</transport-guarantee>\n");	// NOI18N
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
			if (childNodeName == "description") {
				java.lang.String aDescription;
				aDescription = childNodeValue;
				attr = (org.w3c.dom.Attr) attrs.getNamedItem("xml:lang");
				if (attr != null) {
					attrValue = attr.getValue();
				} else {
					attrValue = null;
				}
				java.lang.String processedValueFor_DescriptionXmlLang;
				processedValueFor_DescriptionXmlLang = attrValue;
				addDescriptionXmlLang(processedValueFor_DescriptionXmlLang);
				_Description.add(aDescription);
			}
			else if (childNodeName == "transport-guarantee") {
				_TransportGuarantee = childNodeValue;
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
		// Validating property description
		// Validating property descriptionXmlLang
		// Validating property transportGuarantee
		if (getTransportGuarantee() == null) {
			throw new org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException("getTransportGuarantee() == null", org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException.FailureType.NULL_VALUE, "transportGuarantee", this);	// NOI18N
		}
		final java.lang.String[] enumRestrictionTransportGuarantee = {"NONE", "INTEGRAL", "CONFIDENTIAL"};
		restrictionFailure = true;
		for (int _index2 = 0; 
			_index2 < enumRestrictionTransportGuarantee.length; ++_index2) {
			if (enumRestrictionTransportGuarantee[_index2].equals(getTransportGuarantee())) {
				restrictionFailure = false;
				break;
			}
		}
		if (restrictionFailure) {
			throw new org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException("getTransportGuarantee() enumeration test", org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException.FailureType.ENUM_RESTRICTION, "transportGuarantee", this);	// NOI18N
		}
	}

	public void changePropertyByName(String name, Object value) {
		if (name == null) return;
		name = name.intern();
		if (name == "id")
			setId((java.lang.String)value);
		else if (name == "description")
			addDescription((java.lang.String)value);
		else if (name == "description[]")
			setDescription((java.lang.String[]) value);
		else if (name == "descriptionXmlLang")
			addDescriptionXmlLang((java.lang.String)value);
		else if (name == "descriptionXmlLang[]")
			setDescriptionXmlLang((java.lang.String[]) value);
		else if (name == "transportGuarantee")
			setTransportGuarantee((java.lang.String)value);
		else
			throw new IllegalArgumentException(name+" is not a valid property name for UserDataConstraintType");
	}

	public Object fetchPropertyByName(String name) {
		if (name == "id")
			return getId();
		if (name == "description[]")
			return getDescription();
		if (name == "descriptionXmlLang[]")
			return getDescriptionXmlLang();
		if (name == "transportGuarantee")
			return getTransportGuarantee();
		throw new IllegalArgumentException(name+" is not a valid property name for UserDataConstraintType");
	}

	public String nameSelf() {
		return "UserDataConstraintType";
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
			for (java.util.Iterator it = _Description.iterator(); 
				it.hasNext(); ) {
				java.lang.String element = (java.lang.String)it.next();
				if (child == element) {
					if (returnConstName) {
						return DESCRIPTION;
					} else if (returnSchemaName) {
						return "description";
					} else if (returnXPathName) {
						return "description[position()="+index+"]";
					} else {
						return "Description."+Integer.toHexString(index);
					}
				}
				++index;
			}
			index = 0;
			for (java.util.Iterator it = _DescriptionXmlLang.iterator(); 
				it.hasNext(); ) {
				java.lang.String element = (java.lang.String)it.next();
				if (child == element) {
					if (returnConstName) {
						return DESCRIPTIONXMLLANG;
					} else if (returnSchemaName) {
						return "xml:lang";
					} else if (returnXPathName) {
						return "@xml:lang[position()="+index+"]";
					} else {
						return "DescriptionXmlLang."+Integer.toHexString(index);
					}
				}
				++index;
			}
			if (child == _TransportGuarantee) {
				if (returnConstName) {
					return TRANSPORT_GUARANTEE;
				} else if (returnSchemaName) {
					return "transport-guarantee";
				} else if (returnXPathName) {
					return "transport-guarantee";
				} else {
					return "TransportGuarantee";
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
		return o instanceof org.netbeans.modules.visualweb.api.portlet.dd.UserDataConstraintType && equals((org.netbeans.modules.visualweb.api.portlet.dd.UserDataConstraintType) o);
	}

	public boolean equals(org.netbeans.modules.visualweb.api.portlet.dd.UserDataConstraintType inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (!(_Id == null ? inst._Id == null : _Id.equals(inst._Id))) {
			return false;
		}
		if (sizeDescription() != inst.sizeDescription())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _Description.iterator(), it2 = inst._Description.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			java.lang.String element = (java.lang.String)it.next();
			java.lang.String element2 = (java.lang.String)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		if (sizeDescriptionXmlLang() != inst.sizeDescriptionXmlLang())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _DescriptionXmlLang.iterator(), it2 = inst._DescriptionXmlLang.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			java.lang.String element = (java.lang.String)it.next();
			java.lang.String element2 = (java.lang.String)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		if (!(_TransportGuarantee == null ? inst._TransportGuarantee == null : _TransportGuarantee.equals(inst._TransportGuarantee))) {
			return false;
		}
		return true;
	}

	public int hashCode() {
		int result = 17;
		result = 37*result + (_Id == null ? 0 : _Id.hashCode());
		result = 37*result + (_Description == null ? 0 : _Description.hashCode());
		result = 37*result + (_DescriptionXmlLang == null ? 0 : _DescriptionXmlLang.hashCode());
		result = 37*result + (_TransportGuarantee == null ? 0 : _TransportGuarantee.hashCode());
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

