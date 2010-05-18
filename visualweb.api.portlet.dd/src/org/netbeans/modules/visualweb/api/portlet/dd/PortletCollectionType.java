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
 *	This generated bean class PortletCollectionType
 *	matches the schema element 'portlet-collectionType'.
 *  The root bean class is PortletApp
 *
 *	===============================================================
 *	
 *				The portlet-collectionType is used to identify a subset
 *				of portlets within a portlet application to which a
 *				security constraint applies.
 *				Used in: security-constraint
 *				
 *	===============================================================
 *	Generated on Tue Apr 26 19:08:25 MDT 2005
 * @Generated
 */

package org.netbeans.modules.visualweb.api.portlet.dd;

public class PortletCollectionType implements org.netbeans.modules.visualweb.api.portlet.dd.PortletCollectionTypeInterface, org.netbeans.modules.visualweb.api.portlet.dd.CommonBean {
	public static final String PORTLET_NAME = "PortletName";	// NOI18N

	private java.util.List _PortletName = new java.util.ArrayList();	// List<java.lang.String>

	/**
	 * Normal starting point constructor.
	 */
	public PortletCollectionType() {
	}

	/**
	 * Required parameters constructor
	 */
	public PortletCollectionType(java.lang.String[] portletName) {
		if (portletName!= null) {
			((java.util.ArrayList) _PortletName).ensureCapacity(portletName.length);
			for (int i = 0; i < portletName.length; ++i) {
				_PortletName.add(portletName[i]);
			}
		}
	}

	/**
	 * Deep copy
	 */
	public PortletCollectionType(org.netbeans.modules.visualweb.api.portlet.dd.PortletCollectionType source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public PortletCollectionType(org.netbeans.modules.visualweb.api.portlet.dd.PortletCollectionType source, boolean justData) {
		for (java.util.Iterator it = source._PortletName.iterator();
			it.hasNext(); ) {
			java.lang.String srcElement = (java.lang.String)it.next();
			_PortletName.add(srcElement);
		}
	}

	// This attribute is an array containing at least one element
	public void setPortletName(java.lang.String[] value) {
		if (value == null)
			value = new java.lang.String[0];
		_PortletName.clear();
		((java.util.ArrayList) _PortletName).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_PortletName.add(value[i]);
		}
	}

	public void setPortletName(int index, java.lang.String value) {
		_PortletName.set(index, value);
	}

	public java.lang.String[] getPortletName() {
		java.lang.String[] arr = new java.lang.String[_PortletName.size()];
		return (java.lang.String[]) _PortletName.toArray(arr);
	}

	public java.util.List fetchPortletNameList() {
		return _PortletName;
	}

	public java.lang.String getPortletName(int index) {
		return (java.lang.String)_PortletName.get(index);
	}

	// Return the number of portletName
	public int sizePortletName() {
		return _PortletName.size();
	}

	public int addPortletName(java.lang.String value) {
		_PortletName.add(value);
		int positionOfNewItem = _PortletName.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removePortletName(java.lang.String value) {
		int pos = _PortletName.indexOf(value);
		if (pos >= 0) {
			_PortletName.remove(pos);
		}
		return pos;
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "portlet-collectionType";
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
		for (java.util.Iterator it = _PortletName.iterator(); 
			it.hasNext(); ) {
			java.lang.String element = (java.lang.String)it.next();
			if (element != null) {
				out.write(nextIndent);
				out.write("<portlet-name");	// NOI18N
				out.write(">");	// NOI18N
				org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.writeXML(out, element, false);
				out.write("</portlet-name>\n");	// NOI18N
			}
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
			if (childNodeName == "portlet-name") {
				java.lang.String aPortletName;
				aPortletName = childNodeValue;
				_PortletName.add(aPortletName);
			}
			else {
				// Found extra unrecognized childNode
			}
		}
	}

	public void validate() throws org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException {
		boolean restrictionFailure = false;
		boolean restrictionPassed = false;
		// Validating property portletName
		if (sizePortletName() == 0) {
			throw new org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException("sizePortletName() == 0", org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException.FailureType.NULL_VALUE, "portletName", this);	// NOI18N
		}
	}

	public void changePropertyByName(String name, Object value) {
		if (name == null) return;
		name = name.intern();
		if (name == "portletName")
			addPortletName((java.lang.String)value);
		else if (name == "portletName[]")
			setPortletName((java.lang.String[]) value);
		else
			throw new IllegalArgumentException(name+" is not a valid property name for PortletCollectionType");
	}

	public Object fetchPropertyByName(String name) {
		if (name == "portletName[]")
			return getPortletName();
		throw new IllegalArgumentException(name+" is not a valid property name for PortletCollectionType");
	}

	public String nameSelf() {
		return "PortletCollectionType";
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
			int index = 0;
			for (java.util.Iterator it = _PortletName.iterator(); 
				it.hasNext(); ) {
				java.lang.String element = (java.lang.String)it.next();
				if (child == element) {
					if (returnConstName) {
						return PORTLET_NAME;
					} else if (returnSchemaName) {
						return "portlet-name";
					} else if (returnXPathName) {
						return "portlet-name[position()="+index+"]";
					} else {
						return "PortletName."+Integer.toHexString(index);
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
	}

	public boolean equals(Object o) {
		return o instanceof org.netbeans.modules.visualweb.api.portlet.dd.PortletCollectionType && equals((org.netbeans.modules.visualweb.api.portlet.dd.PortletCollectionType) o);
	}

	public boolean equals(org.netbeans.modules.visualweb.api.portlet.dd.PortletCollectionType inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (sizePortletName() != inst.sizePortletName())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _PortletName.iterator(), it2 = inst._PortletName.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			java.lang.String element = (java.lang.String)it.next();
			java.lang.String element2 = (java.lang.String)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		return true;
	}

	public int hashCode() {
		int result = 17;
		result = 37*result + (_PortletName == null ? 0 : _PortletName.hashCode());
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

