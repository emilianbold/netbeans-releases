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
 *	This generated bean class PortletInfoType
 *	matches the schema element 'portlet-infoType'.
 *  The root bean class is PortletApp
 *
 *	Generated on Tue Apr 26 19:08:25 MDT 2005
 * @Generated
 */

package org.netbeans.modules.visualweb.api.portlet.dd;

public class PortletInfoType implements org.netbeans.modules.visualweb.api.portlet.dd.PortletInfoTypeInterface, org.netbeans.modules.visualweb.api.portlet.dd.CommonBean {
	public static final String ID = "Id";	// NOI18N
	public static final String TITLE = "Title";	// NOI18N
	public static final String SHORT_TITLE = "ShortTitle";	// NOI18N
	public static final String KEYWORDS = "Keywords";	// NOI18N

	private java.lang.String _Id;
	private java.lang.String _Title;
	private java.lang.String _ShortTitle;
	private java.lang.String _Keywords;

	/**
	 * Normal starting point constructor.
	 */
	public PortletInfoType() {
		_Title = "";
	}

	/**
	 * Required parameters constructor
	 */
	public PortletInfoType(java.lang.String title) {
		_Title = title;
	}

	/**
	 * Deep copy
	 */
	public PortletInfoType(org.netbeans.modules.visualweb.api.portlet.dd.PortletInfoType source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public PortletInfoType(org.netbeans.modules.visualweb.api.portlet.dd.PortletInfoType source, boolean justData) {
		_Id = source._Id;
		_Title = source._Title;
		_ShortTitle = source._ShortTitle;
		_Keywords = source._Keywords;
	}

	// This attribute is optional
	public void setId(java.lang.String value) {
		_Id = value;
	}

	public java.lang.String getId() {
		return _Id;
	}

	// This attribute is mandatory
	public void setTitle(java.lang.String value) {
		_Title = value;
	}

	public java.lang.String getTitle() {
		return _Title;
	}

	// This attribute is optional
	public void setShortTitle(java.lang.String value) {
		_ShortTitle = value;
	}

	public java.lang.String getShortTitle() {
		return _ShortTitle;
	}

	// This attribute is optional
	public void setKeywords(java.lang.String value) {
		_Keywords = value;
	}

	public java.lang.String getKeywords() {
		return _Keywords;
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "portlet-infoType";
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
		if (_Title != null) {
			out.write(nextIndent);
			out.write("<title");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.writeXML(out, _Title, false);
			out.write("</title>\n");	// NOI18N
		}
		if (_ShortTitle != null) {
			out.write(nextIndent);
			out.write("<short-title");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.writeXML(out, _ShortTitle, false);
			out.write("</short-title>\n");	// NOI18N
		}
		if (_Keywords != null) {
			out.write(nextIndent);
			out.write("<keywords");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.writeXML(out, _Keywords, false);
			out.write("</keywords>\n");	// NOI18N
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
			if (childNodeName == "title") {
				_Title = childNodeValue;
			}
			else if (childNodeName == "short-title") {
				_ShortTitle = childNodeValue;
			}
			else if (childNodeName == "keywords") {
				_Keywords = childNodeValue;
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
		// Validating property title
		if (getTitle() == null) {
			throw new org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException("getTitle() == null", org.netbeans.modules.visualweb.api.portlet.dd.PortletApp.ValidateException.FailureType.NULL_VALUE, "title", this);	// NOI18N
		}
		// Validating property shortTitle
		// Validating property keywords
	}

	public void changePropertyByName(String name, Object value) {
		if (name == null) return;
		name = name.intern();
		if (name == "id")
			setId((java.lang.String)value);
		else if (name == "title")
			setTitle((java.lang.String)value);
		else if (name == "shortTitle")
			setShortTitle((java.lang.String)value);
		else if (name == "keywords")
			setKeywords((java.lang.String)value);
		else
			throw new IllegalArgumentException(name+" is not a valid property name for PortletInfoType");
	}

	public Object fetchPropertyByName(String name) {
		if (name == "id")
			return getId();
		if (name == "title")
			return getTitle();
		if (name == "shortTitle")
			return getShortTitle();
		if (name == "keywords")
			return getKeywords();
		throw new IllegalArgumentException(name+" is not a valid property name for PortletInfoType");
	}

	public String nameSelf() {
		return "PortletInfoType";
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
			if (child == _Title) {
				if (returnConstName) {
					return TITLE;
				} else if (returnSchemaName) {
					return "title";
				} else if (returnXPathName) {
					return "title";
				} else {
					return "Title";
				}
			}
			if (child == _ShortTitle) {
				if (returnConstName) {
					return SHORT_TITLE;
				} else if (returnSchemaName) {
					return "short-title";
				} else if (returnXPathName) {
					return "short-title";
				} else {
					return "ShortTitle";
				}
			}
			if (child == _Keywords) {
				if (returnConstName) {
					return KEYWORDS;
				} else if (returnSchemaName) {
					return "keywords";
				} else if (returnXPathName) {
					return "keywords";
				} else {
					return "Keywords";
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
		return o instanceof org.netbeans.modules.visualweb.api.portlet.dd.PortletInfoType && equals((org.netbeans.modules.visualweb.api.portlet.dd.PortletInfoType) o);
	}

	public boolean equals(org.netbeans.modules.visualweb.api.portlet.dd.PortletInfoType inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (!(_Id == null ? inst._Id == null : _Id.equals(inst._Id))) {
			return false;
		}
		if (!(_Title == null ? inst._Title == null : _Title.equals(inst._Title))) {
			return false;
		}
		if (!(_ShortTitle == null ? inst._ShortTitle == null : _ShortTitle.equals(inst._ShortTitle))) {
			return false;
		}
		if (!(_Keywords == null ? inst._Keywords == null : _Keywords.equals(inst._Keywords))) {
			return false;
		}
		return true;
	}

	public int hashCode() {
		int result = 17;
		result = 37*result + (_Id == null ? 0 : _Id.hashCode());
		result = 37*result + (_Title == null ? 0 : _Title.hashCode());
		result = 37*result + (_ShortTitle == null ? 0 : _ShortTitle.hashCode());
		result = 37*result + (_Keywords == null ? 0 : _Keywords.hashCode());
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

