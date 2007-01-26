/**
 *	This generated bean class ElementProperties
 *	matches the schema element 'ElementProperties'.
 *
 *	Generated on Tue Jan 23 19:08:48 PST 2007
 *
 *	This class matches the root element of the XML Schema,
 *	and is the root of the bean graph.
 *
 * 	elementProperties <ElementProperties> : ElementProperties
 * 		[attr: elementName CDATA #REQUIRED  : java.lang.String]
 * 		propertyGroup <PropertyGroup> : PropertyGroup[1,n]
 * 			[attr: name CDATA #REQUIRED  : java.lang.String]
 * 			[attr: groupOrder CDATA #IMPLIED  : int]
 * 		property <Property> : Property[1,n]
 * 			[attr: attributeName CDATA #REQUIRED  : java.lang.String]
 * 			[attr: groupName CDATA #IMPLIED  : java.lang.String]
 * 			[attr: propertyOrder CDATA #IMPLIED  : int]
 * 			| builtInCustomizer <BuiltInCustomizer> : BuiltInCustomizer
 * 			| 	[attr: name CDATA #IMPLIED  : java.lang.String]
 * 			| newCustomizer <NewCustomizer> : NewCustomizer
 * 			| 	[attr: className CDATA #IMPLIED  : java.lang.String]
 *
 * @Generated
 */

package org.netbeans.modules.xml.wsdl.ui.property.model;

public class ElementProperties {
	public static final String ELEMENTNAME = "ElementName";	// NOI18N
	public static final String PROPERTYGROUP = "PropertyGroup";	// NOI18N
	public static final String PROPERTY = "Property";	// NOI18N

	private java.lang.String _ElementName;
	private java.util.List _PropertyGroup = new java.util.ArrayList();	// List<PropertyGroup>
	private java.util.List _Property = new java.util.ArrayList();	// List<Property>
	private java.lang.String schemaLocation;

	/**
	 * Normal starting point constructor.
	 */
	public ElementProperties() {
		_ElementName = "";
	}

	/**
	 * Required parameters constructor
	 */
	public ElementProperties(java.lang.String elementName, org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup[] propertyGroup, org.netbeans.modules.xml.wsdl.ui.property.model.Property[] property) {
		_ElementName = elementName;
		if (propertyGroup!= null) {
			((java.util.ArrayList) _PropertyGroup).ensureCapacity(propertyGroup.length);
			for (int i = 0; i < propertyGroup.length; ++i) {
				_PropertyGroup.add(propertyGroup[i]);
			}
		}
		if (property!= null) {
			((java.util.ArrayList) _Property).ensureCapacity(property.length);
			for (int i = 0; i < property.length; ++i) {
				_Property.add(property[i]);
			}
		}
	}

	/**
	 * Deep copy
	 */
	public ElementProperties(org.netbeans.modules.xml.wsdl.ui.property.model.ElementProperties source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public ElementProperties(org.netbeans.modules.xml.wsdl.ui.property.model.ElementProperties source, boolean justData) {
		_ElementName = source._ElementName;
		for (java.util.Iterator it = source._PropertyGroup.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup srcElement = (org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup)it.next();
			_PropertyGroup.add((srcElement == null) ? null : newPropertyGroup(srcElement, justData));
		}
		for (java.util.Iterator it = source._Property.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.xml.wsdl.ui.property.model.Property srcElement = (org.netbeans.modules.xml.wsdl.ui.property.model.Property)it.next();
			_Property.add((srcElement == null) ? null : newProperty(srcElement, justData));
		}
		schemaLocation = source.schemaLocation;
	}

	// This attribute is mandatory
	public void setElementName(java.lang.String value) {
		_ElementName = value;
	}

	public java.lang.String getElementName() {
		return _ElementName;
	}

	// This attribute is an array containing at least one element
	public void setPropertyGroup(org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup[] value) {
		if (value == null)
			value = new PropertyGroup[0];
		_PropertyGroup.clear();
		((java.util.ArrayList) _PropertyGroup).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_PropertyGroup.add(value[i]);
		}
	}

	public void setPropertyGroup(int index, org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup value) {
		_PropertyGroup.set(index, value);
	}

	public org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup[] getPropertyGroup() {
		PropertyGroup[] arr = new PropertyGroup[_PropertyGroup.size()];
		return (PropertyGroup[]) _PropertyGroup.toArray(arr);
	}

	public java.util.List fetchPropertyGroupList() {
		return _PropertyGroup;
	}

	public org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup getPropertyGroup(int index) {
		return (PropertyGroup)_PropertyGroup.get(index);
	}

	// Return the number of propertyGroup
	public int sizePropertyGroup() {
		return _PropertyGroup.size();
	}

	public int addPropertyGroup(org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup value) {
		_PropertyGroup.add(value);
		int positionOfNewItem = _PropertyGroup.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removePropertyGroup(org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup value) {
		int pos = _PropertyGroup.indexOf(value);
		if (pos >= 0) {
			_PropertyGroup.remove(pos);
		}
		return pos;
	}

	// This attribute is an array containing at least one element
	public void setProperty(org.netbeans.modules.xml.wsdl.ui.property.model.Property[] value) {
		if (value == null)
			value = new Property[0];
		_Property.clear();
		((java.util.ArrayList) _Property).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_Property.add(value[i]);
		}
	}

	public void setProperty(int index, org.netbeans.modules.xml.wsdl.ui.property.model.Property value) {
		_Property.set(index, value);
	}

	public org.netbeans.modules.xml.wsdl.ui.property.model.Property[] getProperty() {
		Property[] arr = new Property[_Property.size()];
		return (Property[]) _Property.toArray(arr);
	}

	public java.util.List fetchPropertyList() {
		return _Property;
	}

	public org.netbeans.modules.xml.wsdl.ui.property.model.Property getProperty(int index) {
		return (Property)_Property.get(index);
	}

	// Return the number of property
	public int sizeProperty() {
		return _Property.size();
	}

	public int addProperty(org.netbeans.modules.xml.wsdl.ui.property.model.Property value) {
		_Property.add(value);
		int positionOfNewItem = _Property.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeProperty(org.netbeans.modules.xml.wsdl.ui.property.model.Property value) {
		int pos = _Property.indexOf(value);
		if (pos >= 0) {
			_Property.remove(pos);
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
	public org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup newPropertyGroup() {
		return new org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup newPropertyGroup(PropertyGroup source, boolean justData) {
		return new org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup(source, justData);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.xml.wsdl.ui.property.model.Property newProperty() {
		return new org.netbeans.modules.xml.wsdl.ui.property.model.Property();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.xml.wsdl.ui.property.model.Property newProperty(Property source, boolean justData) {
		return new org.netbeans.modules.xml.wsdl.ui.property.model.Property(source, justData);
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

	public void write(final org.openide.filesystems.FileObject dir, final String filename) throws java.io.IOException {
		org.openide.filesystems.FileSystem fs = dir.getFileSystem();
		fs.runAtomicAction(new org.openide.filesystems.FileSystem.AtomicAction()
		{
			public void run() throws java.io.IOException {
				org.openide.filesystems.FileObject file = dir.getFileObject(filename);
				if (file == null) {
					file = dir.createData(filename);
				}
				write(file);
			}
		}
		);
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
		writeNode(out, "ElementProperties", "");	// NOI18N
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "ElementProperties";
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
		out.write("http://xml.netbeans.org/schema/wsdlui/property");	// NOI18N
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
		// elementName is an attribute with namespace http://xml.netbeans.org/schema/wsdlui/property
		if (_ElementName != null) {
			out.write(" elementName='");
			org.netbeans.modules.xml.wsdl.ui.property.model.ElementProperties.writeXML(out, _ElementName, true);
			out.write("'");	// NOI18N
		}
	}

	protected void writeNodeChildren(java.io.Writer out, String nodeName, String namespace, String indent, java.util.Map namespaceMap) throws java.io.IOException {
		String nextIndent = indent + "	";
		for (java.util.Iterator it = _PropertyGroup.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup element = (org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup)it.next();
			if (element != null) {
				element.writeNode(out, "PropertyGroup", null, nextIndent, namespaceMap);
			}
		}
		for (java.util.Iterator it = _Property.iterator(); it.hasNext(); ) {
			org.netbeans.modules.xml.wsdl.ui.property.model.Property element = (org.netbeans.modules.xml.wsdl.ui.property.model.Property)it.next();
			if (element != null) {
				element.writeNode(out, "Property", null, nextIndent, namespaceMap);
			}
		}
	}

	public static ElementProperties read(org.openide.filesystems.FileObject fo) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		java.io.InputStream in = fo.getInputStream();
		try {
			return read(in);
		} finally {
			in.close();
		}
	}

	public static ElementProperties read(java.io.File f) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		java.io.InputStream in = new java.io.FileInputStream(f);
		try {
			return read(in);
		} finally {
			in.close();
		}
	}

	public static ElementProperties read(java.io.InputStream in) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		return read(new org.xml.sax.InputSource(in), false, null, null);
	}

	/**
	 * Warning: in readNoEntityResolver character and entity references will
	 * not be read from any DTD in the XML source.
	 * However, this way is faster since no DTDs are looked up
	 * (possibly skipping network access) or parsed.
	 */
	public static ElementProperties readNoEntityResolver(java.io.InputStream in) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		return read(new org.xml.sax.InputSource(in), false,
			new org.xml.sax.EntityResolver() {
			public org.xml.sax.InputSource resolveEntity(String publicId, String systemId) {
				java.io.ByteArrayInputStream bin = new java.io.ByteArrayInputStream(new byte[0]);
				return new org.xml.sax.InputSource(bin);
			}
		}
			, null);
	}

	public static ElementProperties read(org.xml.sax.InputSource in, boolean validate, org.xml.sax.EntityResolver er, org.xml.sax.ErrorHandler eh) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
		dbf.setValidating(validate);
		dbf.setNamespaceAware(true);
		javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
		if (er != null)	db.setEntityResolver(er);
		if (eh != null)	db.setErrorHandler(eh);
		org.w3c.dom.Document doc = db.parse(in);
		return read(doc);
	}

	public static ElementProperties read(org.w3c.dom.Document document) {
		ElementProperties aElementProperties = new ElementProperties();
		aElementProperties.readFromDocument(document);
		return aElementProperties;
	}

	protected void readFromDocument(org.w3c.dom.Document document) {
		readNode(document.getDocumentElement());
	}

	protected static class ReadState {
		int lastElementType;
		int elementPosition;
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
		attr = (org.w3c.dom.Attr) attrs.getNamedItem("elementName");
		if (attr != null) {
			attrValue = attr.getValue();
			_ElementName = attrValue;
		}
	}

	protected void readNodeChildren(org.w3c.dom.Node node, java.util.Map namespacePrefixes) {
		org.w3c.dom.NodeList children = node.getChildNodes();
		for (int i = 0, size = children.getLength(); i < size; ++i) {
			org.w3c.dom.Node childNode = children.item(i);
			if (!(childNode instanceof org.w3c.dom.Element)) {
				continue;
			}
			String childNodeName = (childNode.getLocalName() == null ? childNode.getNodeName().intern() : childNode.getLocalName().intern());
			String childNodeValue = "";
			if (childNode.getFirstChild() != null) {
				childNodeValue = childNode.getFirstChild().getNodeValue();
			}
			boolean recognized = readNodeChild(childNode, childNodeName, childNodeValue, namespacePrefixes);
			if (!recognized) {
				// Found extra unrecognized childNode
			}
		}
	}

	protected boolean readNodeChild(org.w3c.dom.Node childNode, String childNodeName, String childNodeValue, java.util.Map namespacePrefixes) {
		// assert childNodeName == childNodeName.intern()
		if (childNodeName == "PropertyGroup") {
			PropertyGroup aPropertyGroup = newPropertyGroup();
			aPropertyGroup.readNode(childNode, namespacePrefixes);
			_PropertyGroup.add(aPropertyGroup);
		}
		else if (childNodeName == "Property") {
			Property aProperty = newProperty();
			aProperty.readNode(childNode, namespacePrefixes);
			_Property.add(aProperty);
		}
		else {
			return false;
		}
		return true;
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
		if (name == "elementName")
			setElementName((java.lang.String)value);
		else if (name == "propertyGroup")
			addPropertyGroup((PropertyGroup)value);
		else if (name == "propertyGroup[]")
			setPropertyGroup((PropertyGroup[]) value);
		else if (name == "property")
			addProperty((Property)value);
		else if (name == "property[]")
			setProperty((Property[]) value);
		else
			throw new IllegalArgumentException(name+" is not a valid property name for ElementProperties");
	}

	public Object fetchPropertyByName(String name) {
		if (name == "elementName")
			return getElementName();
		if (name == "propertyGroup[]")
			return getPropertyGroup();
		if (name == "property[]")
			return getProperty();
		throw new IllegalArgumentException(name+" is not a valid property name for ElementProperties");
	}

	public String nameSelf() {
		return "/ElementProperties";
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
			if (child == _ElementName) {
				if (returnConstName) {
					return ELEMENTNAME;
				} else if (returnSchemaName) {
					return "elementName";
				} else if (returnXPathName) {
					return "@elementName";
				} else {
					return "ElementName";
				}
			}
		}
		if (childObj instanceof Property) {
			Property child = (Property) childObj;
			int index = 0;
			for (java.util.Iterator it = _Property.iterator(); 
				it.hasNext(); ) {
				org.netbeans.modules.xml.wsdl.ui.property.model.Property element = (org.netbeans.modules.xml.wsdl.ui.property.model.Property)it.next();
				if (child == element) {
					if (returnConstName) {
						return PROPERTY;
					} else if (returnSchemaName) {
						return "Property";
					} else if (returnXPathName) {
						return "Property[position()="+index+"]";
					} else {
						return "Property."+Integer.toHexString(index);
					}
				}
				++index;
			}
		}
		if (childObj instanceof PropertyGroup) {
			PropertyGroup child = (PropertyGroup) childObj;
			int index = 0;
			for (java.util.Iterator it = _PropertyGroup.iterator(); 
				it.hasNext(); ) {
				org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup element = (org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup)it.next();
				if (child == element) {
					if (returnConstName) {
						return PROPERTYGROUP;
					} else if (returnSchemaName) {
						return "PropertyGroup";
					} else if (returnXPathName) {
						return "PropertyGroup[position()="+index+"]";
					} else {
						return "PropertyGroup."+Integer.toHexString(index);
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
		for (java.util.Iterator it = _PropertyGroup.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup element = (org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup)it.next();
			if (element != null) {
				if (recursive) {
					element.childBeans(true, beans);
				}
				beans.add(element);
			}
		}
		for (java.util.Iterator it = _Property.iterator(); it.hasNext(); ) {
			org.netbeans.modules.xml.wsdl.ui.property.model.Property element = (org.netbeans.modules.xml.wsdl.ui.property.model.Property)it.next();
			if (element != null) {
				if (recursive) {
					element.childBeans(true, beans);
				}
				beans.add(element);
			}
		}
	}

	public boolean equals(Object o) {
		return o instanceof org.netbeans.modules.xml.wsdl.ui.property.model.ElementProperties && equals((org.netbeans.modules.xml.wsdl.ui.property.model.ElementProperties) o);
	}

	public boolean equals(org.netbeans.modules.xml.wsdl.ui.property.model.ElementProperties inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (!(_ElementName == null ? inst._ElementName == null : _ElementName.equals(inst._ElementName))) {
			return false;
		}
		if (sizePropertyGroup() != inst.sizePropertyGroup())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _PropertyGroup.iterator(), it2 = inst._PropertyGroup.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup element = (org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup)it.next();
			org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup element2 = (org.netbeans.modules.xml.wsdl.ui.property.model.PropertyGroup)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		if (sizeProperty() != inst.sizeProperty())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _Property.iterator(), it2 = inst._Property.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			org.netbeans.modules.xml.wsdl.ui.property.model.Property element = (org.netbeans.modules.xml.wsdl.ui.property.model.Property)it.next();
			org.netbeans.modules.xml.wsdl.ui.property.model.Property element2 = (org.netbeans.modules.xml.wsdl.ui.property.model.Property)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		return true;
	}

	public int hashCode() {
		int result = 17;
		result = 37*result + (_ElementName == null ? 0 : _ElementName.hashCode());
		result = 37*result + (_PropertyGroup == null ? 0 : _PropertyGroup.hashCode());
		result = 37*result + (_Property == null ? 0 : _Property.hashCode());
		return result;
	}

}


/*
		The following schema file has been used for generation:

<?xml version="1.0" encoding="UTF-8"?>

<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"
            targetNamespace="http://xml.netbeans.org/schema/wsdlui/property"
            xmlns:tns="http://xml.netbeans.org/schema/wsdlui/property"
            elementFormDefault="qualified">
    <xsd:element name="ElementProperties">
        <xsd:complexType>
            <xsd:sequence>
                <xsd:element ref="tns:PropertyGroup" maxOccurs="unbounded" />
                <xsd:element ref="tns:Property" maxOccurs="unbounded" />
            </xsd:sequence>
            <xsd:attribute name="elementName" type="xsd:string" use="required"/>
        </xsd:complexType>
    </xsd:element>            
    <xsd:element name="PropertyGroup">
        <xsd:complexType>
            <xsd:attribute name="name" type="xsd:string" use="required"/>
            <xsd:attribute name="groupOrder" type="xsd:int"/>
        </xsd:complexType>
    </xsd:element>
    <xsd:element name="Property">
        <xsd:complexType>
            <xsd:choice>
                <xsd:element name="BuiltInCustomizer">
                    <xsd:complexType xmlns:xsd="http://www.w3.org/2001/XMLSchema">
                        <xsd:sequence/>
                        <xsd:attribute name="name" type="xsd:string"/>
                    </xsd:complexType>
                </xsd:element>
                <xsd:element name="NewCustomizer">
                    <xsd:complexType>
                        <xsd:attribute name="className" type="xsd:string"/>
                    </xsd:complexType>
                </xsd:element>
            </xsd:choice>
            <xsd:attribute name="attributeName" type="xsd:string" use="required"/>
            <xsd:attribute name="groupName" type="xsd:string"/>
            <xsd:attribute name="propertyOrder" type="xsd:int"/>
        </xsd:complexType>
    </xsd:element>
</xsd:schema>

*/
