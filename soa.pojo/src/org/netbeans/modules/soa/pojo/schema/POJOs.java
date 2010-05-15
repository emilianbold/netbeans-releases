/**
 *	This generated bean class POJOs
 *	matches the schema element 'POJOs'.
 *
 *	Generated on Wed Oct 14 16:40:57 PDT 2009
 *
 *	This class matches the root element of the XML Schema,
 *	and is the root of the bean graph.
 *
 * 	POJOs <POJOs> : POJOs
 * 		[attr: version CDATA #IMPLIED  : java.math.BigDecimal]
 * 		POJOProviders <POJOProviders> : POJOProviders[0,1]
 * 			(
 * 			  POJOProvider <POJOProvider> : POJOProvider
 * 			  	className <class-name> : java.lang.String
 * 			  	package <package> : java.lang.String
 * 			  	epName <ep-name> : java.lang.String[0,1]
 * 			  	updateWsdlDuringBuild <update-wsdl-during-build> : boolean
 * 			  	wsdlLocation <wsdl-location> : java.lang.String[0,1]
 * 			  	origWsdlLocationType <orig-wsdl-location-type> : java.lang.String[0,1]
 * 			  	origWsdlLocation <orig-wsdl-location> : java.lang.String[0,1]
 * 			)[0,n]
 * 		POJOConsumers <POJOConsumers> : POJOConsumers[0,1]
 * 			(
 * 			  POJOConsumer <POJOConsumer> : POJOConsumer
 * 			  	interface <Interface> : javax.xml.namespace.QName
 * 			  	operation <Operation> : javax.xml.namespace.QName
 * 			  	inputMessageType <InputMessageType> : javax.xml.namespace.QName[0,1]
 * 			  	invokePattern <InvokePattern> : String
 * 			  	invokeInputType <InvokeInputType> : String
 * 			  	invokeReturnType <InvokeReturnType> : String
 * 			)[0,n]
 * 		pojo <POJO> : Pojo[0,n]
 * 			className <class-name> : java.lang.String
 * 			package <package> : java.lang.String
 * 			epName <ep-name> : java.lang.String[0,1]
 * 			updateWsdlDuringBuild <update-wsdl-during-build> : boolean
 * 			wsdlLocation <wsdl-location> : java.lang.String[0,1]
 * 			origWsdlLocationType <orig-wsdl-location-type> : java.lang.String[0,1]
 * 			origWsdlLocation <orig-wsdl-location> : java.lang.String[0,1]
 *
 * @Generated
 */

package org.netbeans.modules.soa.pojo.schema;

public class POJOs {
	public static final String VERSION = "Version";	// NOI18N
	public static final String POJOPROVIDERS = "POJOProviders";	// NOI18N
	public static final String POJOCONSUMERS = "POJOConsumers";	// NOI18N
	public static final String POJO = "Pojo";	// NOI18N

	private java.math.BigDecimal _Version;
	private POJOProviders _POJOProviders;
	private POJOConsumers _POJOConsumers;
	private java.util.List _Pojo = new java.util.ArrayList();	// List<Pojo>
	private java.lang.String schemaLocation;

	/**
	 * Normal starting point constructor.
	 */
	public POJOs() {
	}

	/**
	 * Deep copy
	 */
	public POJOs(org.netbeans.modules.soa.pojo.schema.POJOs source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public POJOs(org.netbeans.modules.soa.pojo.schema.POJOs source, boolean justData) {
		_Version = source._Version;
		_POJOProviders = (source._POJOProviders == null) ? null : newPOJOProviders(source._POJOProviders, justData);
		_POJOConsumers = (source._POJOConsumers == null) ? null : newPOJOConsumers(source._POJOConsumers, justData);
		for (java.util.Iterator it = source._Pojo.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.soa.pojo.schema.Pojo srcElement = (org.netbeans.modules.soa.pojo.schema.Pojo)it.next();
			_Pojo.add((srcElement == null) ? null : newPojo(srcElement, justData));
		}
		schemaLocation = source.schemaLocation;
	}

	// This attribute is optional
	public void setVersion(java.math.BigDecimal value) {
		_Version = value;
	}

	public java.math.BigDecimal getVersion() {
		return _Version;
	}

	// This attribute is optional
	public void setPOJOProviders(org.netbeans.modules.soa.pojo.schema.POJOProviders value) {
		_POJOProviders = value;
	}

	public org.netbeans.modules.soa.pojo.schema.POJOProviders getPOJOProviders() {
		return _POJOProviders;
	}

	// This attribute is optional
	public void setPOJOConsumers(org.netbeans.modules.soa.pojo.schema.POJOConsumers value) {
		_POJOConsumers = value;
	}

	public org.netbeans.modules.soa.pojo.schema.POJOConsumers getPOJOConsumers() {
		return _POJOConsumers;
	}

	// This attribute is an array, possibly empty
	public void setPojo(org.netbeans.modules.soa.pojo.schema.Pojo[] value) {
		if (value == null)
			value = new Pojo[0];
		_Pojo.clear();
		((java.util.ArrayList) _Pojo).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_Pojo.add(value[i]);
		}
	}

	public void setPojo(int index, org.netbeans.modules.soa.pojo.schema.Pojo value) {
		_Pojo.set(index, value);
	}

	public org.netbeans.modules.soa.pojo.schema.Pojo[] getPojo() {
		Pojo[] arr = new Pojo[_Pojo.size()];
		return (Pojo[]) _Pojo.toArray(arr);
	}

	public java.util.List fetchPojoList() {
		return _Pojo;
	}

	public org.netbeans.modules.soa.pojo.schema.Pojo getPojo(int index) {
		return (Pojo)_Pojo.get(index);
	}

	// Return the number of pojo
	public int sizePojo() {
		return _Pojo.size();
	}

	public int addPojo(org.netbeans.modules.soa.pojo.schema.Pojo value) {
		_Pojo.add(value);
		int positionOfNewItem = _Pojo.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removePojo(org.netbeans.modules.soa.pojo.schema.Pojo value) {
		int pos = _Pojo.indexOf(value);
		if (pos >= 0) {
			_Pojo.remove(pos);
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
	public org.netbeans.modules.soa.pojo.schema.POJOProviders newPOJOProviders() {
		return new org.netbeans.modules.soa.pojo.schema.POJOProviders();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.soa.pojo.schema.POJOProviders newPOJOProviders(POJOProviders source, boolean justData) {
		return new org.netbeans.modules.soa.pojo.schema.POJOProviders(source, justData);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.soa.pojo.schema.POJOConsumers newPOJOConsumers() {
		return new org.netbeans.modules.soa.pojo.schema.POJOConsumers();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.soa.pojo.schema.POJOConsumers newPOJOConsumers(POJOConsumers source, boolean justData) {
		return new org.netbeans.modules.soa.pojo.schema.POJOConsumers(source, justData);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.soa.pojo.schema.Pojo newPojo() {
		return new org.netbeans.modules.soa.pojo.schema.Pojo();
	}

	/**
	 * Create a new bean, copying from another one.
	 * This does not add it to any bean graph.
	 */
	public org.netbeans.modules.soa.pojo.schema.Pojo newPojo(Pojo source, boolean justData) {
		return new org.netbeans.modules.soa.pojo.schema.Pojo(source, justData);
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
		writeNode(out, "POJOs", "");	// NOI18N
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "POJOs";
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
		out.write("http://xml.netbeans.org/schema/POJOConfig");	// NOI18N
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
		// version is an attribute with namespace http://xml.netbeans.org/schema/POJOConfig
		if (_Version != null) {
			out.write(" version='");
			out.write(_Version.toString());
			out.write("'");	// NOI18N
		}
	}

	protected void writeNodeChildren(java.io.Writer out, String nodeName, String namespace, String indent, java.util.Map namespaceMap) throws java.io.IOException {
		String nextIndent = indent + "	";
		if (_POJOProviders != null) {
			_POJOProviders.writeNode(out, "POJOProviders", null, nextIndent, namespaceMap);
		}
		if (_POJOConsumers != null) {
			_POJOConsumers.writeNode(out, "POJOConsumers", null, nextIndent, namespaceMap);
		}
		for (java.util.Iterator it = _Pojo.iterator(); it.hasNext(); ) {
			org.netbeans.modules.soa.pojo.schema.Pojo element = (org.netbeans.modules.soa.pojo.schema.Pojo)it.next();
			if (element != null) {
				element.writeNode(out, "POJO", null, nextIndent, namespaceMap);
			}
		}
	}

	public static POJOs read(org.openide.filesystems.FileObject fo) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		java.io.InputStream in = fo.getInputStream();
		try {
			return read(in);
		} finally {
			in.close();
		}
	}

	public static POJOs read(java.io.File f) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		java.io.InputStream in = new java.io.FileInputStream(f);
		try {
			return read(in);
		} finally {
			in.close();
		}
	}

	public static POJOs read(java.io.InputStream in) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		return read(new org.xml.sax.InputSource(in), false, null, null);
	}

	/**
	 * Warning: in readNoEntityResolver character and entity references will
	 * not be read from any DTD in the XML source.
	 * However, this way is faster since no DTDs are looked up
	 * (possibly skipping network access) or parsed.
	 */
	public static POJOs readNoEntityResolver(java.io.InputStream in) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		return read(new org.xml.sax.InputSource(in), false,
			new org.xml.sax.EntityResolver() {
			public org.xml.sax.InputSource resolveEntity(String publicId, String systemId) {
				java.io.ByteArrayInputStream bin = new java.io.ByteArrayInputStream(new byte[0]);
				return new org.xml.sax.InputSource(bin);
			}
		}
			, null);
	}

	public static POJOs read(org.xml.sax.InputSource in, boolean validate, org.xml.sax.EntityResolver er, org.xml.sax.ErrorHandler eh) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
		dbf.setValidating(validate);
		dbf.setNamespaceAware(true);
		javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
		if (er != null)	db.setEntityResolver(er);
		if (eh != null)	db.setErrorHandler(eh);
		org.w3c.dom.Document doc = db.parse(in);
		return read(doc);
	}

	public static POJOs read(org.w3c.dom.Document document) {
		POJOs aPOJOs = new POJOs();
		aPOJOs.readFromDocument(document);
		return aPOJOs;
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
		attr = (org.w3c.dom.Attr) attrs.getNamedItem("version");
		if (attr != null) {
			attrValue = attr.getValue();
			_Version = new java.math.BigDecimal(attrValue);
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
			if (childNodeName == "POJOProviders") {
				_POJOProviders = newPOJOProviders();
				_POJOProviders.readNode(childNode, namespacePrefixes);
			}
			else if (childNodeName == "POJOConsumers") {
				_POJOConsumers = newPOJOConsumers();
				_POJOConsumers.readNode(childNode, namespacePrefixes);
			}
			else if (childNodeName == "POJO") {
				Pojo aPojo = newPojo();
				aPojo.readNode(childNode, namespacePrefixes);
				_Pojo.add(aPojo);
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
		if (name == "version")
			setVersion((java.math.BigDecimal)value);
		else if (name == "POJOProviders")
			setPOJOProviders((POJOProviders)value);
		else if (name == "POJOConsumers")
			setPOJOConsumers((POJOConsumers)value);
		else if (name == "pojo")
			addPojo((Pojo)value);
		else if (name == "pojo[]")
			setPojo((Pojo[]) value);
		else
			throw new IllegalArgumentException(name+" is not a valid property name for POJOs");
	}

	public Object fetchPropertyByName(String name) {
		if (name == "version")
			return getVersion();
		if (name == "POJOProviders")
			return getPOJOProviders();
		if (name == "POJOConsumers")
			return getPOJOConsumers();
		if (name == "pojo[]")
			return getPojo();
		throw new IllegalArgumentException(name+" is not a valid property name for POJOs");
	}

	public String nameSelf() {
		return "/POJOs";
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
		if (childObj instanceof Pojo) {
			Pojo child = (Pojo) childObj;
			int index = 0;
			for (java.util.Iterator it = _Pojo.iterator(); it.hasNext(); ) {
				org.netbeans.modules.soa.pojo.schema.Pojo element = (org.netbeans.modules.soa.pojo.schema.Pojo)it.next();
				if (child == element) {
					if (returnConstName) {
						return POJO;
					} else if (returnSchemaName) {
						return "POJO";
					} else if (returnXPathName) {
						return "POJO[position()="+index+"]";
					} else {
						return "Pojo."+Integer.toHexString(index);
					}
				}
				++index;
			}
		}
		if (childObj instanceof java.math.BigDecimal) {
			java.math.BigDecimal child = (java.math.BigDecimal) childObj;
			if (child == _Version) {
				if (returnConstName) {
					return VERSION;
				} else if (returnSchemaName) {
					return "version";
				} else if (returnXPathName) {
					return "@version";
				} else {
					return "Version";
				}
			}
		}
		if (childObj instanceof POJOConsumers) {
			POJOConsumers child = (POJOConsumers) childObj;
			if (child == _POJOConsumers) {
				if (returnConstName) {
					return POJOCONSUMERS;
				} else if (returnSchemaName) {
					return "POJOConsumers";
				} else if (returnXPathName) {
					return "POJOConsumers";
				} else {
					return "POJOConsumers";
				}
			}
		}
		if (childObj instanceof POJOProviders) {
			POJOProviders child = (POJOProviders) childObj;
			if (child == _POJOProviders) {
				if (returnConstName) {
					return POJOPROVIDERS;
				} else if (returnSchemaName) {
					return "POJOProviders";
				} else if (returnXPathName) {
					return "POJOProviders";
				} else {
					return "POJOProviders";
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
		if (_POJOProviders != null) {
			if (recursive) {
				_POJOProviders.childBeans(true, beans);
			}
			beans.add(_POJOProviders);
		}
		if (_POJOConsumers != null) {
			if (recursive) {
				_POJOConsumers.childBeans(true, beans);
			}
			beans.add(_POJOConsumers);
		}
		for (java.util.Iterator it = _Pojo.iterator(); it.hasNext(); ) {
			org.netbeans.modules.soa.pojo.schema.Pojo element = (org.netbeans.modules.soa.pojo.schema.Pojo)it.next();
			if (element != null) {
				if (recursive) {
					element.childBeans(true, beans);
				}
				beans.add(element);
			}
		}
	}

	public boolean equals(Object o) {
		return o instanceof org.netbeans.modules.soa.pojo.schema.POJOs && equals((org.netbeans.modules.soa.pojo.schema.POJOs) o);
	}

	public boolean equals(org.netbeans.modules.soa.pojo.schema.POJOs inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (!(_Version == null ? inst._Version == null : _Version.equals(inst._Version))) {
			return false;
		}
		if (!(_POJOProviders == null ? inst._POJOProviders == null : _POJOProviders.equals(inst._POJOProviders))) {
			return false;
		}
		if (!(_POJOConsumers == null ? inst._POJOConsumers == null : _POJOConsumers.equals(inst._POJOConsumers))) {
			return false;
		}
		if (sizePojo() != inst.sizePojo())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _Pojo.iterator(), it2 = inst._Pojo.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			org.netbeans.modules.soa.pojo.schema.Pojo element = (org.netbeans.modules.soa.pojo.schema.Pojo)it.next();
			org.netbeans.modules.soa.pojo.schema.Pojo element2 = (org.netbeans.modules.soa.pojo.schema.Pojo)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		return true;
	}

	public int hashCode() {
		int result = 17;
		result = 37*result + (_Version == null ? 0 : _Version.hashCode());
		result = 37*result + (_POJOProviders == null ? 0 : _POJOProviders.hashCode());
		result = 37*result + (_POJOConsumers == null ? 0 : _POJOConsumers.hashCode());
		result = 37*result + (_Pojo == null ? 0 : _Pojo.hashCode());
		return result;
	}

}


/*
		The following schema file has been used for generation:

<?xml version="1.0" encoding="UTF-8"?>
<xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema"
            targetNamespace="http://xml.netbeans.org/schema/POJOConfig"
            xmlns:tns="http://xml.netbeans.org/schema/POJOConfig"
            elementFormDefault="qualified">

    <xsd:element name="POJOs">
        <xsd:complexType>
            <xsd:sequence>
                <xsd:element ref="tns:POJOProviders" minOccurs="0"/>
                <xsd:element ref="tns:POJOConsumers" minOccurs="0"/>
                <!-- Do not use POJO, uses POJOProviders and POJOProvider -->
                <xsd:element ref="tns:POJO" maxOccurs="unbounded" minOccurs="0"/>
            </xsd:sequence>
            <xsd:attribute name="version" type="xsd:decimal"/>
        </xsd:complexType>
    </xsd:element>

    <xsd:element name="POJO">
        <xsd:complexType xmlns:xsd="http://www.w3.org/2001/XMLSchema">
            <xsd:sequence>
                <xsd:element name="class-name" type="xsd:string"/>
                <xsd:element name="package" type="xsd:string"/>
                <xsd:element name="ep-name" type="xsd:string" minOccurs="0"/>
                <xsd:element name="update-wsdl-during-build" type="xsd:boolean" default="false"/>
                <xsd:element name="wsdl-location" type="xsd:string" minOccurs="0"/>
                <xsd:element name="orig-wsdl-location-type" type="xsd:string" minOccurs="0"/>
                <xsd:element name="orig-wsdl-location" type="xsd:string" minOccurs="0"/>
            </xsd:sequence>
        </xsd:complexType>
    </xsd:element>

    <xsd:element name="POJOProviders">
        <xsd:complexType>
            <xsd:sequence maxOccurs="unbounded" minOccurs="0">
                <xsd:element ref="tns:POJOProvider"/>
            </xsd:sequence>
        </xsd:complexType>
    </xsd:element>

    <xsd:element name="POJOProvider">
        <xsd:complexType xmlns:xsd="http://www.w3.org/2001/XMLSchema">
            <xsd:sequence>
                <xsd:element name="class-name" type="xsd:string"/>
                <xsd:element name="package" type="xsd:string"/>
                <xsd:element name="ep-name" type="xsd:string" minOccurs="0"/>
                <xsd:element name="update-wsdl-during-build" type="xsd:boolean" default="false"/>
                <xsd:element name="wsdl-location" type="xsd:string" minOccurs="0"/>
                <xsd:element name="orig-wsdl-location-type" type="xsd:string" minOccurs="0"/>
                <xsd:element name="orig-wsdl-location" type="xsd:string" minOccurs="0"/>
            </xsd:sequence>
        </xsd:complexType>
    </xsd:element>

    <xsd:element name="POJOConsumer">
        <xsd:complexType>
            <xsd:sequence>
                <xsd:element name="Interface" type="xsd:QName"></xsd:element>
                <xsd:element name="Operation" type="xsd:QName"></xsd:element>
                <xsd:element name="InputMessageType" type="xsd:QName" minOccurs="0"></xsd:element>
                <xsd:element name="InvokePattern" type="xsd:String"></xsd:element>
                <xsd:element name="InvokeInputType" type="xsd:String"></xsd:element>
                <xsd:element name="InvokeReturnType" type="xsd:String" ></xsd:element>
            </xsd:sequence>
        </xsd:complexType>
    </xsd:element>
    <xsd:element name="POJOConsumers">
        <xsd:complexType>
            <xsd:sequence maxOccurs="unbounded" minOccurs="0">
                <xsd:element ref="tns:POJOConsumer"/>
            </xsd:sequence>
        </xsd:complexType>
    </xsd:element>
</xsd:schema>

*/
