/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 *	This generated bean class TemplateGroup
 *	matches the schema element 'templateGroup'.
 *
 *	Generated on Wed Oct 22 17:28:35 PDT 2008
 *
 *	This class matches the root element of the XML Schema,
 *	and is the root of the bean graph.
 *
 * 	templateGroup <templateGroup> : TemplateGroup
 * 		[attr: namespace CDATA #IMPLIED  : java.lang.String]
 * 		[attr: prefix CDATA #IMPLIED  : java.lang.String]
 * 		[attr: skeleton CDATA #IMPLIED  : boolean]
 * 		template <template> : TemplateType[1,n]
 * 			[attr: name CDATA #IMPLIED  : java.lang.String]
 * 			[attr: default CDATA #IMPLIED  : boolean]
 * 			[attr: skeleton CDATA #IMPLIED  : boolean]
 * 			[attr: mode CDATA #IMPLIED  : java.lang.String]
 * 			wsdlTemplate <wsdlTemplate> : WsdlTemplateType
 * 				[attr: file CDATA #IMPLIED  : java.lang.String]
 * 			wsdlElement <wsdlElement> : WsdlElementType[1,n]
 * 				[attr: name CDATA #IMPLIED  : java.lang.String]
 * 				extensionElement <extensionElement> : ExtensionElementType[1,n]
 * 					[attr: name CDATA #IMPLIED  : java.lang.String]
 * 					extensionAttr <extensionAttr> : ExtensionAttrType[1,n]
 * 						[attr: name CDATA #IMPLIED  : java.lang.String]
 * 						[attr: defaultValue CDATA #IMPLIED  : java.lang.String]
 *
 * @Generated
 */

package org.netbeans.modules.xml.wsdl.bindingsupport.template;

public class TemplateGroup {
	public static final String NAMESPACE = "Namespace";	// NOI18N
	public static final String PREFIX = "Prefix";	// NOI18N
	public static final String SKELETON = "Skeleton";	// NOI18N
	public static final String TEMPLATE = "Template";	// NOI18N

	private java.lang.String _Namespace;
	private java.lang.String _Prefix;
	private boolean _Skeleton;
	private java.util.List _Template = new java.util.ArrayList();	// List<TemplateType>
	private java.lang.String schemaLocation;

	/**
	 * Normal starting point constructor.
	 */
	public TemplateGroup() {
	}

	/**
	 * Required parameters constructor
	 */
	public TemplateGroup(org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateType[] template) {
		if (template!= null) {
			((java.util.ArrayList) _Template).ensureCapacity(template.length);
			for (int i = 0; i < template.length; ++i) {
				_Template.add(template[i]);
			}
		}
	}

	/**
	 * Deep copy
	 */
	public TemplateGroup(org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateGroup source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public TemplateGroup(org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateGroup source, boolean justData) {
		_Namespace = source._Namespace;
		_Prefix = source._Prefix;
		_Skeleton = source._Skeleton;
		for (java.util.Iterator it = source._Template.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateType srcElement = (org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateType)it.next();
			_Template.add((srcElement == null) ? null : newTemplateType(srcElement, justData));
		}
		schemaLocation = source.schemaLocation;
	}

	// This attribute is optional
	public void setNamespace(java.lang.String value) {
		_Namespace = value;
	}

	public java.lang.String getNamespace() {
		return _Namespace;
	}

	// This attribute is optional
	public void setPrefix(java.lang.String value) {
		_Prefix = value;
	}

	public java.lang.String getPrefix() {
		return _Prefix;
	}

	// This attribute is optional
	public void setSkeleton(boolean value) {
		_Skeleton = value;
	}

	public boolean isSkeleton() {
		return _Skeleton;
	}

	// This attribute is an array containing at least one element
	public void setTemplate(org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateType[] value) {
		if (value == null)
			value = new TemplateType[0];
		_Template.clear();
		((java.util.ArrayList) _Template).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_Template.add(value[i]);
		}
	}

	public void setTemplate(int index, org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateType value) {
		_Template.set(index, value);
	}

	public org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateType[] getTemplate() {
		TemplateType[] arr = new TemplateType[_Template.size()];
		return (TemplateType[]) _Template.toArray(arr);
	}

	public java.util.List fetchTemplateList() {
		return _Template;
	}

	public org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateType getTemplate(int index) {
		return (TemplateType)_Template.get(index);
	}

	// Return the number of template
	public int sizeTemplate() {
		return _Template.size();
	}

	public int addTemplate(org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateType value) {
		_Template.add(value);
		int positionOfNewItem = _Template.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeTemplate(org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateType value) {
		int pos = _Template.indexOf(value);
		if (pos >= 0) {
			_Template.remove(pos);
		}
		return pos;
	}

	public void _setSchemaLocation(String location) {
		schemaLocation = location;
	}

	public String _getSchemaLocation() {
		return schemaLocation;
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateType newTemplateType() {
		return new org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateType();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateType newTemplateType(TemplateType source, boolean justData) {
		return new org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateType(source, justData);
	}

	public void write(org.openide.filesystems.FileObject fo) throws java.io.IOException {
		org.openide.filesystems.FileLock lock = fo.lock();
		try {
			java.io.OutputStream out = fo.getOutputStream(lock);
			write(out);
			out.close();
		} finally {
			lock.releaseLock();
		}
	}

	public void write(org.openide.filesystems.FileObject dir, String filename) throws java.io.IOException {
		org.openide.filesystems.FileObject file = dir.getFileObject(filename);
		if (file == null) {
			file = dir.createData(filename);
		}
		write(file);
	}

	public void write(java.io.File f) throws java.io.IOException {
		java.io.OutputStream out = new java.io.FileOutputStream(f);
		try {
			write(out);
		} finally {
			out.close();
		}
	}

	public void write(java.io.OutputStream out) throws java.io.IOException {
		write(out, null);
	}

	public void write(java.io.OutputStream out, String encoding) throws java.io.IOException {
		java.io.Writer w;
		if (encoding == null) {
			encoding = "UTF-8";	// NOI18N
		}
		w = new java.io.BufferedWriter(new java.io.OutputStreamWriter(out, encoding));
		write(w, encoding);
		w.flush();
	}

	/**
	 * Print this Java Bean to @param out including an XML header.
	 * @param encoding is the encoding style that @param out was opened with.
	 */
	public void write(java.io.Writer out, String encoding) throws java.io.IOException {
		out.write("<?xml version='1.0'");	// NOI18N
		if (encoding != null)
			out.write(" encoding='"+encoding+"'");	// NOI18N
		out.write(" ?>\n");	// NOI18N
		writeNode(out, "templateGroup", "");	// NOI18N
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "templateGroup";
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
		out.write(" xmlns='");	// NOI18N
		out.write("http://xml.netbeans.org/schema/templates");	// NOI18N
		out.write("'");	// NOI18N
		if (schemaLocation != null) {
			namespaceMap.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");
			out.write(" xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='");
			out.write(schemaLocation);
			out.write("'");	// NOI18N
		}
		writeNodeAttributes(out, nodeName, namespace, indent, namespaceMap);
		out.write(">\n");
		writeNodeChildren(out, nodeName, namespace, indent, namespaceMap);
		out.write(indent);
		out.write("</");
		if (namespace != null) {
			out.write((String)namespaceMap.get(namespace));
			out.write(":");
		}
		out.write(nodeName);
		out.write(">\n");
	}

	protected void writeNodeAttributes(java.io.Writer out, String nodeName, String namespace, String indent, java.util.Map namespaceMap) throws java.io.IOException {
		// namespace is an attribute with namespace http://xml.netbeans.org/schema/templates
		if (_Namespace != null) {
			out.write(" namespace='");
			org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateGroup.writeXML(out, _Namespace, true);
			out.write("'");	// NOI18N
		}
		// prefix is an attribute with namespace http://xml.netbeans.org/schema/templates
		if (_Prefix != null) {
			out.write(" prefix='");
			org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateGroup.writeXML(out, _Prefix, true);
			out.write("'");	// NOI18N
		}
		// skeleton is an attribute with namespace http://xml.netbeans.org/schema/templates
		out.write(" skeleton='");
		out.write(_Skeleton ? "true" : "false");
		out.write("'");	// NOI18N
	}

	protected void writeNodeChildren(java.io.Writer out, String nodeName, String namespace, String indent, java.util.Map namespaceMap) throws java.io.IOException {
		String nextIndent = indent + "	";
		for (java.util.Iterator it = _Template.iterator(); it.hasNext(); ) {
			org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateType element = (org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateType)it.next();
			if (element != null) {
				element.writeNode(out, "template", null, nextIndent, namespaceMap);
			}
		}
	}

	public static TemplateGroup read(org.openide.filesystems.FileObject fo) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		java.io.InputStream in = fo.getInputStream();
		try {
			return read(in);
		} finally {
			in.close();
		}
	}

	public static TemplateGroup read(java.io.File f) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		java.io.InputStream in = new java.io.FileInputStream(f);
		try {
			return read(in);
		} finally {
			in.close();
		}
	}

	public static TemplateGroup read(java.io.InputStream in) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		return read(new org.xml.sax.InputSource(in), false, null, null);
	}

	/**
	 * Warning: in readNoEntityResolver character and entity references will
	 * not be read from any DTD in the XML source.
	 * However, this way is faster since no DTDs are looked up
	 * (possibly skipping network access) or parsed.
	 */
	public static TemplateGroup readNoEntityResolver(java.io.InputStream in) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		return read(new org.xml.sax.InputSource(in), false,
			new org.xml.sax.EntityResolver() {
			public org.xml.sax.InputSource resolveEntity(String publicId, String systemId) {
				java.io.ByteArrayInputStream bin = new java.io.ByteArrayInputStream(new byte[0]);
				return new org.xml.sax.InputSource(bin);
			}
		}
			, null);
	}

	public static TemplateGroup read(org.xml.sax.InputSource in, boolean validate, org.xml.sax.EntityResolver er, org.xml.sax.ErrorHandler eh) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
		dbf.setValidating(validate);
		dbf.setNamespaceAware(true);
		javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
		if (er != null)	db.setEntityResolver(er);
		if (eh != null)	db.setErrorHandler(eh);
		org.w3c.dom.Document doc = db.parse(in);
		return read(doc);
	}

	public static TemplateGroup read(org.w3c.dom.Document document) {
		TemplateGroup aTemplateGroup = new TemplateGroup();
		aTemplateGroup.readFromDocument(document);
		return aTemplateGroup;
	}

	protected void readFromDocument(org.w3c.dom.Document document) {
		readNode(document.getDocumentElement());
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
			String xsiPrefix = "xsi";
			for (java.util.Iterator it = namespacePrefixes.keySet().iterator(); 
				it.hasNext(); ) {
				String prefix = (String) it.next();
				String ns = (String) namespacePrefixes.get(prefix);
				if ("http://www.w3.org/2001/XMLSchema-instance".equals(ns)) {
					xsiPrefix = prefix;
					break;
				}
			}
			attr = (org.w3c.dom.Attr) attrs.getNamedItem(""+xsiPrefix+":schemaLocation");
			if (attr != null) {
				attrValue = attr.getValue();
				schemaLocation = attrValue;
			}
			readNodeAttributes(node, namespacePrefixes, attrs);
		}
		readNodeChildren(node, namespacePrefixes);
	}

	protected void readNodeAttributes(org.w3c.dom.Node node, java.util.Map namespacePrefixes, org.w3c.dom.NamedNodeMap attrs) {
		org.w3c.dom.Attr attr;
		java.lang.String attrValue;
		attr = (org.w3c.dom.Attr) attrs.getNamedItem("namespace");
		if (attr != null) {
			attrValue = attr.getValue();
			_Namespace = attrValue;
		}
		attr = (org.w3c.dom.Attr) attrs.getNamedItem("prefix");
		if (attr != null) {
			attrValue = attr.getValue();
			_Prefix = attrValue;
		}
		attr = (org.w3c.dom.Attr) attrs.getNamedItem("skeleton");
		if (attr != null) {
			attrValue = attr.getValue();
			_Skeleton = java.lang.Boolean.valueOf(attrValue).booleanValue();
		}
	}

	protected void readNodeChildren(org.w3c.dom.Node node, java.util.Map namespacePrefixes) {
		org.w3c.dom.NodeList children = node.getChildNodes();
		for (int i = 0, size = children.getLength(); i < size; ++i) {
			org.w3c.dom.Node childNode = children.item(i);
			String childNodeName = (childNode.getLocalName() == null ? childNode.getNodeName().intern() : childNode.getLocalName().intern());
			String childNodeValue = "";
			if (childNode.getFirstChild() != null) {
				childNodeValue = childNode.getFirstChild().getNodeValue();
			}
			if (childNodeName == "template") {
				TemplateType aTemplate = newTemplateType();
				aTemplate.readNode(childNode, namespacePrefixes);
				_Template.add(aTemplate);
			}
			else {
				// Found extra unrecognized childNode
			}
		}
	}

	/**
	 * Takes some text to be printed into an XML stream and escapes any
	 * characters that might make it invalid XML (like '<').
	 */
	public static void writeXML(java.io.Writer out, String msg) throws java.io.IOException {
		writeXML(out, msg, true);
	}

	public static void writeXML(java.io.Writer out, String msg, boolean attribute) throws java.io.IOException {
		if (msg == null)
			return;
		int msgLength = msg.length();
		for (int i = 0; i < msgLength; ++i) {
			char c = msg.charAt(i);
			writeXML(out, c, attribute);
		}
	}

	public static void writeXML(java.io.Writer out, char msg, boolean attribute) throws java.io.IOException {
		if (msg == '&')
			out.write("&amp;");
		else if (msg == '<')
			out.write("&lt;");
		else if (msg == '>')
			out.write("&gt;");
		else if (attribute) {
			if (msg == '"')
				out.write("&quot;");
			else if (msg == '\'')
				out.write("&apos;");
			else if (msg == '\n')
				out.write("&#xA;");
			else if (msg == '\t')
				out.write("&#x9;");
			else
				out.write(msg);
		}
		else
			out.write(msg);
	}

	public void changePropertyByName(String name, Object value) {
		if (name == null) return;
		name = name.intern();
		if (name == "namespace")
			setNamespace((java.lang.String)value);
		else if (name == "prefix")
			setPrefix((java.lang.String)value);
		else if (name == "skeleton")
			setSkeleton(((java.lang.Boolean)value).booleanValue());
		else if (name == "template")
			addTemplate((TemplateType)value);
		else if (name == "template[]")
			setTemplate((TemplateType[]) value);
		else
			throw new IllegalArgumentException(name+" is not a valid property name for TemplateGroup");
	}

	public Object fetchPropertyByName(String name) {
		if (name == "namespace")
			return getNamespace();
		if (name == "prefix")
			return getPrefix();
		if (name == "skeleton")
			return (isSkeleton() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if (name == "template[]")
			return getTemplate();
		throw new IllegalArgumentException(name+" is not a valid property name for TemplateGroup");
	}

	public String nameSelf() {
		return "/TemplateGroup";
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
			if (child == _Namespace) {
				if (returnConstName) {
					return NAMESPACE;
				} else if (returnSchemaName) {
					return "namespace";
				} else if (returnXPathName) {
					return "@namespace";
				} else {
					return "Namespace";
				}
			}
			if (child == _Prefix) {
				if (returnConstName) {
					return PREFIX;
				} else if (returnSchemaName) {
					return "prefix";
				} else if (returnXPathName) {
					return "@prefix";
				} else {
					return "Prefix";
				}
			}
		}
		if (childObj instanceof TemplateType) {
			TemplateType child = (TemplateType) childObj;
			int index = 0;
			for (java.util.Iterator it = _Template.iterator(); 
				it.hasNext(); ) {
				org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateType element = (org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateType)it.next();
				if (child == element) {
					if (returnConstName) {
						return TEMPLATE;
					} else if (returnSchemaName) {
						return "template";
					} else if (returnXPathName) {
						return "template[position()="+index+"]";
					} else {
						return "Template."+Integer.toHexString(index);
					}
				}
				++index;
			}
		}
		if (childObj instanceof java.lang.Boolean) {
			java.lang.Boolean child = (java.lang.Boolean) childObj;
			if (((java.lang.Boolean)child).booleanValue() == _Skeleton) {
				if (returnConstName) {
					return SKELETON;
				} else if (returnSchemaName) {
					return "skeleton";
				} else if (returnXPathName) {
					return "@skeleton";
				} else {
					return "Skeleton";
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
		for (java.util.Iterator it = _Template.iterator(); it.hasNext(); ) {
			org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateType element = (org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateType)it.next();
			if (element != null) {
				if (recursive) {
					element.childBeans(true, beans);
				}
				beans.add(element);
			}
		}
	}

	public boolean equals(Object o) {
		return o instanceof org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateGroup && equals((org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateGroup) o);
	}

	public boolean equals(org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateGroup inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (!(_Namespace == null ? inst._Namespace == null : _Namespace.equals(inst._Namespace))) {
			return false;
		}
		if (!(_Prefix == null ? inst._Prefix == null : _Prefix.equals(inst._Prefix))) {
			return false;
		}
		if (!(_Skeleton == inst._Skeleton)) {
			return false;
		}
		if (sizeTemplate() != inst.sizeTemplate())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _Template.iterator(), it2 = inst._Template.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateType element = (org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateType)it.next();
			org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateType element2 = (org.netbeans.modules.xml.wsdl.bindingsupport.template.TemplateType)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		return true;
	}

	public int hashCode() {
		int result = 17;
		result = 37*result + (_Namespace == null ? 0 : _Namespace.hashCode());
		result = 37*result + (_Prefix == null ? 0 : _Prefix.hashCode());
		result = 37*result + (_Skeleton ? 0 : 1);
		result = 37*result + (_Template == null ? 0 : _Template.hashCode());
		return result;
	}

}


/*
		The following schema file has been used for generation:

<?xml version="1.0" encoding="UTF-8"?>

<!--
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.


The contents of this file are subject to the terms of either the GNU
General Public License Version 2 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://www.netbeans.org/cddl-gplv2.html
or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License file at
nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
particular file as subject to the "Classpath" exception as provided
by Sun in the GPL Version 2 section of the License file that
accompanied this code. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

Contributor(s):

 The Original Software is NetBeans. The Initial Developer of the Original
 Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 Microsystems, Inc. All Rights Reserved.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
-->


<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"
            targetNamespace="http://xml.netbeans.org/schema/templates"
            xmlns:tns="http://xml.netbeans.org/schema/templates"
            elementFormDefault="qualified">
    <xsd:element name="templateGroup">
        <xsd:complexType>
            <xsd:sequence>
                <xsd:element name="template" type="tns:templateType" maxOccurs="unbounded"/>
            </xsd:sequence>
            <xsd:attribute name="namespace" type="xsd:string"/>
            <xsd:attribute name="prefix" type="xsd:string"/>
            <xsd:attribute name="skeleton" type="xsd:boolean"/>
        </xsd:complexType>
    </xsd:element>
    <xsd:complexType name="templateType">
        <xsd:sequence>
            <xsd:element name="wsdlTemplate" type="tns:wsdlTemplateType" maxOccurs="1"/>
            <xsd:element name="wsdlElement" type="tns:wsdlElementType" maxOccurs="unbounded"/>
        </xsd:sequence>
        <xsd:attribute name="name" type="xsd:string"/>
        <xsd:attribute name="default" type="xsd:boolean"/>
        <xsd:attribute name="skeleton" type="xsd:boolean"/>
        <xsd:attribute name="mode" type="xsd:string"/>
    </xsd:complexType>
    <xsd:complexType name="wsdlElementType">
        <xsd:sequence>
            <xsd:element name="extensionElement" type="tns:extensionElementType" maxOccurs="unbounded"/>
        </xsd:sequence>
        <xsd:attribute name="name" type="xsd:string"/>
    </xsd:complexType>
    <xsd:complexType name="wsdlTemplateType">
        <xsd:attribute name="file" type="xsd:string"/>
    </xsd:complexType>
    <xsd:complexType name="extensionElementType">
        <xsd:sequence>
            <xsd:element name="extensionAttr" type="tns:extensionAttrType" maxOccurs="unbounded"/>
        </xsd:sequence>
        <xsd:attribute name="name" type="xsd:string"/>
    </xsd:complexType>
    <xsd:complexType name="extensionAttrType">
        <xsd:attribute name="name" type="xsd:string"/>
        <xsd:attribute name="defaultValue" type="xsd:string"/>
    </xsd:complexType>
</xsd:schema>

*/
