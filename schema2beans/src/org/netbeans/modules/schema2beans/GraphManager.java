/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.schema2beans;

import java.util.*;
import java.io.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 *
 *
 */
public class GraphManager extends Object {
    
    public static interface Writer {
	public void write(OutputStream out, Document doc);
    }
    
    public static interface Factory {
        public org.w3c.dom.Document createDocument(InputStream in,
                                                   boolean validate);
    }
    
    Document	document = null;
    NodeFactory	factory = null;
    HashMap 	bindingsMap  = new HashMap();
    BaseBean	root;
    private boolean writeCData = false;
    
    //	When set to null (default), use XMLDocument instead
    private Factory		docFactory;
    private Writer		docWriter;

    private String		docTypePublic;
    private String		docTypeSystem;
    
    //
    //	The key is the input stream. This is how we can get the
    //	factory/writer when we are asked to build a Dom graph.
    //
    static Map	factoryMap = Collections.synchronizedMap(new HashMap());
    static Map	writerMap = Collections.synchronizedMap(new HashMap());
    

    public GraphManager(BaseBean root) {
        this.root = root;
    }
    
    /**
     *	Associate a factory to a stream
     */
    public static void setFactory(InputStream in,
				  GraphManager.Factory factory)  throws Schema2BeansException {
	setFactory(in, factory, null);
    }
    
    /**
     *  Set an external factory to use instead of the default one
     */
    public static void setFactory(InputStream in, GraphManager.Factory factory,
				  GraphManager.Writer writer) throws Schema2BeansException {
	if (in == null)
	    throw new Schema2BeansException(Common.getMessage(
		"InputStreamCantBeNull_msg"));
	
	if (factory != null)
	    GraphManager.factoryMap.put(in, factory);
	else
	    GraphManager.factoryMap.remove(in);
	
	if (writer != null)
	    GraphManager.writerMap.put(in, writer);
	else
	    GraphManager.writerMap.remove(in);
    }
    
    /**
     *  Set an external writer to use instead of the default one
     */
    public void setWriter(GraphManager.Writer writer) {
        this.docWriter = writer;
    }

    public void setWriteCData(boolean value) {
        writeCData = value;
    }
    
    public static Node createRootElementNode(String name) throws Schema2BeansRuntimeException {
        String s = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +// NOI18N
            "<" + name + "/>";	// NOI18N

        ByteArrayInputStream in = new ByteArrayInputStream(s.getBytes());
        Document doc = GraphManager.createXmlDocument(in, false);
        NodeList children = doc.getChildNodes();
        int length = children.getLength();
        for (int i = 0; i < length; ++i) {
            Node node = children.item(i);
            if (node instanceof DocumentType) {
                //System.out.println("Found DocumentType where there should be none.");
                doc.removeChild(node);
                --length;
            }
        }
        return doc;
    }
    
    //
    //	Document created for this GraphManager. Called by the generated bean.
    //
    public void setXmlDocument(Node doc) throws Schema2BeansRuntimeException {
	if (doc instanceof Document) {
	    this.document = (Document)doc;
	    this.setNodeFactory((Document)doc);
	    
	    //
	    //	The factory/writer should know about the doc now
	    //	and no more about the original InputStream.
	    //	(if the user specified a factory/writer)
	    //
	    Object o = GraphManager.factoryMap.get(doc);
	    if (o != null) {
		this.docFactory = (GraphManager.Factory)o;
		GraphManager.factoryMap.remove(doc);
	    }
	    
	    o = GraphManager.writerMap.get(doc);
	    if (o != null) {
		this.docWriter = (GraphManager.Writer)o;
		GraphManager.writerMap.remove(doc);
	    }
	}
	else
	    throw new Schema2BeansRuntimeException(Common.getMessage(
		"CantFindFactory_msg"));
    }
    
    
    /**
     * This returns the DOM Document object, root
     * of the current DOM graph.  Operations that cause structural
     * modifications to the DOM graph are not allowed.  Indeed,
     * modifying the DOM graph directly would cause the bean graph
     * and its internal representation to be out of sync.
     */
    public Document getXmlDocument() {
	return this.document;
    }
    
    public void setDoctype(String publicId, String systemId) {
        //System.out.println("GraphManager.setDoctype: publicId="+publicId+" systemId="+systemId);
	this.docTypePublic = publicId;
	this.docTypeSystem = systemId;
    }
    
    /**
     *	Parse the DOM tree until the element named 'name' is found.
     *	Return the node of the name or null if not found.
     *	This method is used by the root bean generated class to get
     *	the root element of the DOM tree and start building the
     *	bean graph from here.
     */
    public static Node getElementNode(String name, Node doc) {
	Node n;
	for (n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
	    if (n.getNodeType() == Node.ELEMENT_NODE
	    && n.getNodeName().equals(name)) {
		break;
	    }
	}
	return n;
    }
    
    /**
     *	This method is called by the createRoot() method of the root bean
     *	(part of the BaseBean class). The doc might not be available
     *	at the time of this call. In such a case, the method
     *	completeRootBinding is called afterwards with the doc value to complete
     *	the setup of the root.
     *
     *	This makes sure that the root element of the object bindings
     *	between the beans and the DOM Nodes is created, before that the
     *	recursing creation of the graph begins.
     */
    public void createRootBinding(BaseBean beanRoot, BeanProp prop, Node doc) throws Schema2BeansException {
	prop.registerDomNode(doc, null, beanRoot);
	if (doc != null)
	    this.bindingsMap.put(doc, beanRoot.binding);
    }
    
    public void completeRootBinding(BaseBean beanRoot, Node doc) {
	this.bindingsMap.put(doc, beanRoot.binding);
	beanRoot.binding.setNode(doc);
    }
    
    /**
     *	This method sets the DOM nodes factory.
     */
    public void setNodeFactory(Document doc) {
	this.factory = new NodeFactory(doc);
    }
    
    /**
     *  Return the DOM node factory
     */
    public NodeFactory getNodeFactory() {
	return this.factory;
    }
    
    /**
     *	Return the root of the bean graph
     */
    public BaseBean getBeanRoot() {
	return this.root;
    }

    // Find the root element, skipping over DOCTYPEs, comments, etc.
    // Returns null if there isn't one.
    static protected Element getRootElement(Document doc) {
        return doc.getDocumentElement();
    }

    // Given @param doc what should it's DOCTYPE name be.
    static protected String getDocTypeName(Document doc) {
        // First look for a DOCTYPE
        NodeList children = doc.getChildNodes();
        int length = children.getLength();
        for (int i = 0; i < length; ++i) {
            Node node = children.item(i);
            if (node instanceof DocumentType) {
                DocumentType docType = (DocumentType) node;
                return docType.getName();
            }
        }
        // Otherwise, check the first node of the actual document
        Node rootNode = getRootElement(doc);
        return rootNode.getNodeName();
    }

    /**
     *	OutputStream version of write()
     */
    void write(OutputStream out)
	throws IOException, Schema2BeansException {
	//
	//	Code specific to the DOM implementation:
	//
	if (this.document == null)
	    throw new Schema2BeansException(Common.getMessage("CantGetDocument_msg"));
	if (this.docWriter != null)
	    this.docWriter.write(out, this.document);
	else {
        write(out, null);
	}
    }

    protected void write(OutputStream out, String encoding) throws java.io.IOException, Schema2BeansException {
        java.io.Writer w;
        if (encoding == null)
            encoding = "UTF-8";
        w = new BufferedWriter(new OutputStreamWriter(out, encoding));
        write(w, encoding);
        w.flush();
    }

    protected void write(java.io.Writer out) throws java.io.IOException, Schema2BeansException {
        write(out, (String) null);
    }

    protected void write(java.io.Writer out, String encoding) throws java.io.IOException, Schema2BeansException {
        out.write("<?xml version=\"1.0\"");    // NOI18N
        if (encoding != null) {
            out.write(" encoding=\""+encoding+"\"?>\n");    // NOI18N
        } else
            out.write(" encoding=\"UTF-8\"?>\n");    // NOI18N
        if (docTypePublic != null || docTypeSystem != null) {
            String docName = getDocTypeName(document);
            DocumentType docType = document.getDoctype();
            NamedNodeMap entities = null;
            if (docType != null)
                entities = docType.getEntities();
            write(out, docName, docTypePublic, docTypeSystem, entities);
            out.write("\n");
        }
        NodeList children = document.getChildNodes();
        int length = children.getLength();
        // First print out any DocumentTypes
        for (int i = 0; i < length; ++i) {
            Node node = children.item(i);
            if (node instanceof DocumentType) {
                write(out, node);
                out.write("\n");
            }
        }
        // Now print everything, but DocumentTypes
        for (int i = 0; i < length; ++i) {
            Node node = children.item(i);
            if (!(node instanceof DocumentType)) {
                write(out, node);
                out.write("\n");
            }
        }

        out.flush();
    }

    public void write(java.io.Writer out, Node node) throws java.io.IOException, Schema2BeansException {
        boolean needsReturnBetweenChildren = false;

        NodeList children = node.getChildNodes();
        if (node instanceof Element) {
            out.write("<"+node.getNodeName());
            write(out, node.getAttributes());
            if (children.getLength() == 0) {
                out.write("/>");
                return;
            }
            out.write(">");
        } else if (node instanceof Text) {
            printXML(out, node.getNodeValue(), false);
        } else if (node instanceof Document) {
            needsReturnBetweenChildren = true;
        } else if (node instanceof DocumentType) {
            write(out, (DocumentType) node);
        } else if (node instanceof Comment) {
            write(out, (Comment) node);
        } else if (node instanceof Entity) {
            write(out, (Entity) node);
        } else if (node instanceof ProcessingInstruction) {
            write(out, (ProcessingInstruction) node);
        } else {
            System.out.println("! schema2beans found unknown node type in DOM graph:");
            System.out.println("write: node.getClass="+node.getClass()+" node="+node);
            System.out.println("write: nodename="+node.getNodeName()+" nodevalue="+node.getNodeValue());
            System.out.println("write: getAttributes="+node.getAttributes());
        }
        
        int length = children.getLength();
        for (int i = 0; i < length; ++i) {
            write(out, children.item(i));
            if (needsReturnBetweenChildren)
                out.write("\n");
        }
        if (node instanceof Element) {
            out.write("</"+node.getNodeName()+">");
        }
    }

    protected void write(java.io.Writer out, DocumentType docType) throws java.io.IOException, Schema2BeansException {
        //System.out.println("! FOUND DOCTYPE for "+docType.getName());
        if (docTypePublic != null || docTypeSystem != null) {
            // The header printing has already taken care of the DOCTYPE.
            return;
        }
        write(out, docType.getName(), docType.getPublicId(),
              docType.getSystemId(), docType.getEntities());
    }

    protected void write(java.io.Writer out, String docName, String publicId,
                         String systemId, NamedNodeMap entities) throws java.io.IOException, Schema2BeansException {
        out.write("<!DOCTYPE "+docName);	// NOI18N
        if (publicId != null) {
            out.write(" PUBLIC \"");	// NOI18N
            XMLUtil.printXML(out, publicId);
            out.write("\"");	// NOI18N
            if (systemId == null)
                systemId = "SYSTEM";	// NOI18N
        }
        if (systemId != null) {
            out.write(" \"");	// NOI18N
            XMLUtil.printXML(out, systemId);
            out.write("\"");	// NOI18N
        }
        if (entities != null) {
            int length = entities.getLength();
            if (length > 0) {
                out.write(" [");	// NOI18N
                for (int i = 0; i < length; ++i) {
                    Node node = entities.item(i);
                    write(out, node);
                }
                out.write("]");	// NOI18N
            }
        }
        out.write(">");	// NOI18N
    }

    protected void write(java.io.Writer out, Comment comment) throws java.io.IOException {
        // Does not need to have anything escaped (no printXML).
        out.write("<!--");
        String text = comment.getNodeValue();
        // A comment is not allow to have "--" inside of it.
        int pos = text.indexOf("--");
        while (pos >= 0) {
            out.write(text.substring(0, pos));
            out.write("&#x2d;&#x2d;");
            text = text.substring(pos+2, text.length());
            pos = text.indexOf("--");
        }
        out.write(text);
        out.write("-->");
    }

    protected void write(java.io.Writer out, Entity entity) throws java.io.IOException {
        out.write("<!ENTITY "+entity.getNodeName());
        /*
          We don't seem to be able to get any useful info out of the
          Entity object.
          
        out.write(" notation ");
        if (entity.getNotationName() != null)
            out.write(entity.getNotationName());
        out.write(" publicid ");
        if (entity.getPublicId() != null)
            out.write(entity.getPublicId());
        out.write(" systemid ");
        if (entity.getSystemId() != null)
            out.write(entity.getSystemId());
        */
        out.write(" UNKNOWN>");
    }

    protected void write(java.io.Writer out, ProcessingInstruction pi) throws java.io.IOException {
        // Does not need to have anything escaped (no printXML).
        if ("xml".equals(pi.getTarget())) {
            // We've already printed out the standard xml PI, suppress this one.
            return;
        }
        out.write("<?"+pi.getTarget()+" "+pi.getData()+"?>");
    }

    /**
     * This is used to print attributes.
     */
    protected void write(java.io.Writer out, NamedNodeMap nodes) throws java.io.IOException {
        int length = nodes.getLength();
        for (int i = 0; i < length; ++i) {
            Node node = nodes.item(i);
            out.write(" ");
            out.write(node.getNodeName());
            out.write("=\"");
            XMLUtil.printXML(out, node.getNodeValue());
            out.write("\"");
        }
    }

    protected void printXML(java.io.Writer out, String msg, boolean attribute) throws java.io.IOException {
        if (writeCData && msg.indexOf("]]>") < 0) {
            boolean shouldEscape = XMLUtil.shouldEscape(msg);
            if (shouldEscape)
                out.write("<![CDATA[");
            out.write(msg);
            if (shouldEscape)
                out.write("]]>");
        } else
            XMLUtil.printXML(out, msg, attribute);
    }
    
    /**
     * Take the current DOM tree and readjust whitespace so that it
     * looks pretty.
     */
    public void reindent(String indent) {
        reindent(document, -1, indent);
    }

    protected boolean reindent(Node node, int level, String indent) {
        String nodeValue = node.getNodeValue();

        boolean hasOnlyWhitespaceTextChildren = true;
        NodeList children = node.getChildNodes();
        int length = children.getLength();
        for (int i = 0; i < length; ++i) {
            if (!reindent(children.item(i), level+1, indent))
                hasOnlyWhitespaceTextChildren = false;
        }

        /*
        try {
            printLevel(System.out, level, indent,
                       node.getNodeName()+": \""+nodeValue+"\"\n");
            printLevel(System.out, level, indent,
                       "hasOnlyWhitespaceTextChildren="+hasOnlyWhitespaceTextChildren+"\n");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        */

        if (hasOnlyWhitespaceTextChildren && level >= 0  && length > 0) {
            // We can reindent this one.  So, go thru each child node
            // and make sure it's intendation is where we want it.
            
            StringBuffer idealWhitespaceBuf = new StringBuffer();
            printLevel(idealWhitespaceBuf, level, indent);
            String idealFinalWhitespace = "\n" + idealWhitespaceBuf.toString().intern();
            printLevel(idealWhitespaceBuf, 1, indent);
            String idealChildWhitespace = "\n"+idealWhitespaceBuf.toString().intern();
            //System.out.println("idealChildWhitespace='"+idealChildWhitespace+"'");
            //
            // Check to make sure the last child node is a text node.
            // If not, insert the correct spacing at the end.
            //
            if (length > 1 && !(children.item(length-1) instanceof Text)) {
                //System.out.println("Inserting additional whitespace at end of child list.");
                node.appendChild(document.createTextNode(idealFinalWhitespace));
                ++length;
            }
            //System.out.println("node.getNodeName="+node.getNodeName()+" children.length="+length);
            
            boolean shouldBeTextNode = true;  // This alternates
            Text textNode;
            for (int i = 0; i < length; ++i) {
                Node childNode = children.item(i);
                boolean isTextNode = (childNode instanceof Text);
                //System.out.println("shouldBeTextNode="+shouldBeTextNode+" isTextNode="+isTextNode+" "+childNode.getNodeName());
                if (shouldBeTextNode) {
                    if (isTextNode) {
                        String childNodeValue = childNode.getNodeValue().intern();
                        if (length == 1) {
                            // We have a single text child, don't mess with
                            // it's contents.
                            continue;
                        }
                        
                        textNode = (Text) childNode;
                        // Need to make sure it has the correct whitespace
                        if (i == length-1) {
                            if (idealFinalWhitespace != childNodeValue) {
                                //System.out.println("!Incorrect whitespace on final!");
                                if (textNode.getLength() > 0)
                                    textNode.deleteData(0, textNode.getLength());
                                textNode.appendData(idealFinalWhitespace);
                            }
                            
                        } else {
                            if (idealChildWhitespace != childNodeValue) {
                                //System.out.println("!Incorrect whitespace: '"+childNodeValue+"' versus ideal of '"+idealChildWhitespace+"'");
                                textNode.deleteData(0, textNode.getLength());
                                textNode.appendData(idealChildWhitespace);
                            }
                        }
                        shouldBeTextNode ^= true;
                    } else {
                        // Need to insert a whitespace node
                        //System.out.println("Need to insert a whitespace node before "+childNode.getNodeName()+": "+childNode.getNodeValue());
                        if (i == length-1) {
                            //System.out.println("It's a final one!");
                            node.insertBefore(document.createTextNode(idealChildWhitespace), childNode);
                            node.appendChild(document.createTextNode(idealFinalWhitespace));
                            ++length;
                        } else {
                            //System.out.println("Not final.");
                            node.insertBefore(document.createTextNode(idealChildWhitespace), childNode);
                        }
                        //
                        // We updated our list while going thru it at the same
                        // time, so update our indices to account for the
                        // new growth.
                        //
                        ++i;  
                        ++length;
                    }
                } else {
                    if (isTextNode) {
                        // The last whitespace node is correct, so this one
                        // must be extra.
                        //System.out.println("Extra unneeded whitespace");
                        node.removeChild(childNode);
                        --i;
                        --length;
                        if (i == length-1 && i >= 0) {
                            //System.out.println("It's a final one!");
                            // Go back and fix up the last node.
                            childNode = children.item(i);
                            String childNodeValue = childNode.getNodeValue().intern();
                            if (idealFinalWhitespace != childNodeValue) {
                                textNode = (Text) childNode;
                                //System.out.println("!Incorrect whitespace on final!");
                                if (textNode.getLength() > 0)
                                    textNode.deleteData(0, textNode.getLength());
                                textNode.appendData(idealFinalWhitespace);
                            }
                        }
                    } else {
                        // This is just right.
                        //System.out.println("This is just right.");
                        shouldBeTextNode ^= true;
                    }
                }
            }
        }

        // Let my caller know if I'm a Text node that has only whitespace
        // or not.
        if (node instanceof Text) {
            if (nodeValue == null)
                return true;
            return (nodeValue.trim().equals(""));
        }
        return true;
    }

    /**
     * Indent by 2 spaces for every @level.
     */
    protected static void printLevel(java.io.Writer out, int level, String indent) throws java.io.IOException {
        StringBuffer outBuf = new StringBuffer();
        printLevel(outBuf, level, indent);
        out.write(outBuf.toString());
    }

    protected static void printLevel(StringBuffer out, int level, String indent) {
        for (int i = 0; i < level; ++i) {
            out.append(indent);
        }
    }
    
    protected static void printLevel(java.io.Writer out, int level, String indent, String text) throws java.io.IOException {
        StringBuffer outBuf = new StringBuffer();
        printLevel(outBuf, level, indent, text);
        out.write(outBuf.toString());
    }

    protected static void printLevel(OutputStream out, int level, String indent, String text) throws java.io.IOException {
        OutputStreamWriter w = new OutputStreamWriter(out);
        printLevel(w, level, indent, text);
        w.flush();
    }

    protected static void printLevel(StringBuffer out, int level,
                                     String indent, String text) {
        printLevel(out, level, indent);
        out.append(text);
    }

    /**
     *	Creates a DOM document from the input stream.
     */
    public static Document createXmlDocument(InputStream in, boolean validate) throws Schema2BeansRuntimeException {
	return createXmlDocument(in, validate, null);
    }

    private static InputStream tee(InputStream in) throws IOException {
        byte[] buf = new byte[4096];
        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        int totalLength = 0;
        int len;
        while ((len = in.read(buf, 0, 4096)) > 0) {
            ba.write(buf, 0, len);
            totalLength += len;
        }
        System.out.println("schema2beans: in (length="+totalLength+"):");
        System.out.println(ba.toString());
        ByteArrayInputStream bain = new ByteArrayInputStream(ba.toByteArray());
        return bain;
    }

    /**
     *	Creates a DOM document from the input stream.
     */
    public static Document createXmlDocument(InputStream in, boolean validate,
                                             EntityResolver er) throws Schema2BeansRuntimeException {
        if (in == null)
            throw new IllegalArgumentException("in == null");	// NOI18N
        try {
            if (DDLogFlags.debug) {
                // Dump the contents to stdout
                in = tee(in);
            }
	    
            //
            //	Change the references to map the newly created doc
            //	The BaseBean instance is not created yet. The doc
            //	document will be used to get back the factories.
            //
            Object o = GraphManager.factoryMap.get(in);
            if (o != null) {
                GraphManager.Factory f = (GraphManager.Factory)o;
		
                Document doc = f.createDocument(in, validate);
		
                GraphManager.factoryMap.remove(in);
                GraphManager.factoryMap.put(doc, o);
		
                Object o2 = GraphManager.writerMap.get(in);
                if (o2 != null) {
                    GraphManager.writerMap.remove(in);
                    GraphManager.writerMap.put(doc, o2);
                }
                return doc;
            }
            else {
                return createXmlDocument(new InputSource(in), validate, er, null);
            }
        } catch (Schema2BeansException e) {
            throw new Schema2BeansRuntimeException(e);
        } catch (IOException e) {
            throw new Schema2BeansRuntimeException(e);
        }
    }


    public static Document createXmlDocument(InputSource in, boolean validate) throws Schema2BeansException {
	return createXmlDocument(in, validate, null, null);
    }


    public static Document createXmlDocument(InputSource in, boolean validate,
                                             EntityResolver er, ErrorHandler eh) throws Schema2BeansException {
        if (in == null)
            throw new IllegalArgumentException("in == null");	// NOI18N
        if (validate == false && er == null) {
            // The client is not interested in any validation, so make
            // see to it that any entity resolution doesn't hit the network
            er = NullEntityResolver.newInstance();
        }
        try {
            //	Build a Document using JAXP
            DocumentBuilderFactory dbf =
                DocumentBuilderFactory.newInstance();
            dbf.setValidating(validate);

            DocumentBuilder db = dbf.newDocumentBuilder();
            if (er != null)
                db.setEntityResolver(er);
            if (eh != null)
                db.setErrorHandler(eh);

            if (DDLogFlags.debug) {
                System.out.println("createXmlDocument: validate="+validate+" dbf="+dbf+" db="+db+" er="+er);
            }

            return db.parse(in);
        } catch (javax.xml.parsers.ParserConfigurationException e) {
            throw new Schema2BeansNestedException(Common.getMessage("CantCreateXMLDOMDocument_msg"), e);
        } catch (org.xml.sax.SAXException e) {
            throw new Schema2BeansNestedException(Common.getMessage("CantCreateXMLDOMDocument_msg"), e);
        } catch (IOException e) {
            throw new Schema2BeansNestedException(Common.getMessage("CantCreateXMLDOMDocument_msg"), e);
        }
    }
    
    /**
     *	This method is called by the generated beans when they are
     *	building themselves from a DOM tree.
     *	Typically, the first root bean calls this method with the
     *	DOM root node and the list of the properties that are expected
     *	under this node.
     *	This method parses the DOM sub-node of the node and matches their names
     *	with the names of the properties. When a match is found, the
     *	bean property object is called with the node found. If the node
     *	has no match in the bean properties, the node is ignored but
     *	the event is logged as it might reveal a problem in the bean tree
     *	(DTD element missing in the bean class graph).
     *
     */
    public void fillProperties(BeanProp[] prop, Node node) throws Schema2BeansException {
        BaseBean 	bean;
        DOMBinding 	binding, newBinding;
	
        if (prop == null || node == null)
            return;
	
        if (this.bindingsMap.get(node) == null) {
            throw new Schema2BeansException(Common.getMessage(
                                                              "CurrentNodeHasNoBinding_msg", new Integer(node.hashCode())));
        }

        // Store the property's dtdName's into a map for fast lookup,
        // and be able to handle multiple properties with the same name.
        Map dtdName2Prop = new HashMap();	// Map<String, BeanProp>
        Map dupDtdNames = new HashMap();	// Map<String, List<BeanProp>>
        for(int i=0; i<prop.length; i++) {
            String dtdName = prop[i].dtdName;
            if (dtdName2Prop.containsKey(dtdName)) {
                //System.out.println("Found duplicate dtdName="+dtdName);
                List dupList = (List) dupDtdNames.get(dtdName);
                if (dupList == null) {
                    dupList = new ArrayList();
                    //dupList.add(dtdName2Prop.get(dtdName));
                    dupDtdNames.put(dtdName, dupList);
                }
                dupList.add(prop[i]);
            } else {
                dtdName2Prop.put(dtdName, prop[i]);
            }
        }
	
        //  Assume that the DOM parsing takes longer than prop parsing
        Map dupDtdNameIterators = new HashMap();	// Map<String, Iterator<BeanProp>>
        for (Node n = node.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                String eltName = n.getNodeName();
		
                //System.out.println("eltName="+eltName);
                BeanProp matchingProp = (BeanProp) dtdName2Prop.get(eltName);
                if (matchingProp != null) {
                    List dupList = (List) dupDtdNames.get(eltName);
                    if (dupList != null) {
                        // There are mutliple BeanProp's with the same dtd name,
                        // figure out which one we should pick.
                        if (!Common.isArray(matchingProp.type)) {
                            Iterator propIt = (Iterator) dupDtdNameIterators.get(eltName);
                            //System.out.println("propIt="+propIt);
                            if (propIt == null) {
                                // First time, let the matchingProp load it,
                                // but set it up for next time.
                                propIt = dupList.iterator();
                                dupDtdNameIterators.put(eltName, propIt);
                            } else if (propIt.hasNext()) {
                                matchingProp = (BeanProp) propIt.next();
                            }
                        }
                    }
                    binding = (DOMBinding)this.bindingsMap.get(n);
			
                    if (DDLogFlags.debug) {
                        String s = eltName + " N(" + n.hashCode()+") - " +
                            (binding==null?"new node":"already bound B(" +
                             binding.hashCode() +")");
                        TraceLogger.put(TraceLogger.DEBUG,
                                        TraceLogger.SVC_DD,
                                        DDLogFlags.DBG_BLD, 1,
                                        DDLogFlags.FOUNDNODE, s);
                    }
			
                    newBinding =
                        matchingProp.registerDomNode(n, binding, null);
			
                    if (newBinding != null) {
			    
                        if (Common.isBean(matchingProp.type))
                            bean = (BaseBean)newBinding.getBean(matchingProp);
                        else
                            bean = null;
			    
                        if (DDLogFlags.debug) {
                            String s = "B(" + newBinding.hashCode() +
                                ") - " + matchingProp.getPropClass().getName();
                            TraceLogger.put(TraceLogger.DEBUG,
                                            TraceLogger.SVC_DD,
                                            DDLogFlags.DBG_BLD, 1,
                                            DDLogFlags.BOUNDNODE, s);
                        }
			    
                        if (bean != null) {
                            //
                            //  The property was a bean, fill up this bean.
                            //  This is were the recursing call in the
                            //  creation of the bean graph happens.
                            //
                            if (binding == null)
                                this.bindingsMap.put(n, newBinding);
                            bean.createBean(n, this);
                        }
                    }
                } else {
                    // log that there is no matching
                    if (DDLogFlags.debug) {
                        TraceLogger.put(TraceLogger.DEBUG,
                                        TraceLogger.SVC_DD,
                                        DDLogFlags.DBG_BLD, 1,
                                        DDLogFlags.NONODE, eltName);
                    }
                }
            } else {
                // Log that this is not an element
                short t = n.getNodeType();
                String v = n.getNodeValue();
                if (DDLogFlags.debug) {
                    TraceLogger.put(TraceLogger.DEBUG,
                                    TraceLogger.SVC_DD,
                                    DDLogFlags.DBG_BLD, 1,
                                    DDLogFlags.NOTELT,
                                    DDFactory.typeToString(t) +
                                    " = " + Common.dumpHex(v));
                }
            }
        }
    }
    
    //////////////////////
    //
    //	Event misc. methods, base on the name of the PropertyChanged event
    //
    //	BaseBean 	getPropertyParent(String name)
    //	String 		getPropertyParentName(String name)
    //	String 		getPropertyName(String name)
    //	int			getPropertyIndex(String name)
    //	String		getAttributeName(String name)
    //	boolean		isAttribute(String name)
    //
    
    /**
     *	Return the bean holding the property 'name' as a BaseBean object
     */
    public BaseBean getPropertyParent(String name) {
	if (!name.startsWith("/"))	// NOI18N
	    throw new IllegalArgumentException(Common.getMessage(
		"NameShouldStartWithSlash_msg", name));
	
	int		i1, i2, i;
	String		beanName, indexName;
	BaseBean	curBean = null;
	
	i1 = 0;
	
	do {
	    i1 = name.indexOf('/', i1);
	    if (i1 != -1) {
		i1++;
		i2 = name.indexOf('/', i1);
		if (i2 == -1) {
		    if (curBean == null)
			curBean = this.root;
		    //	We reached the property, return the bean found
		    break;
		}
		
		beanName = name.substring(i1, i2);
		i = beanName.indexOf('.');
		
		if (i != -1) {
		    indexName = beanName.substring(i+1);
		    beanName = beanName.substring(0, i);
		    
		    if (indexName.indexOf('i') != -1)
			throw new IllegalStateException(
			Common.getMessage(
			    "CantFindBeanBecausePartOfNameRemoved_msg",
			    beanName, name));
		}
		else
		    indexName = "0";	// NOI18N
		
		if (curBean == null)
		    curBean = this.root;
		else
		    curBean = curBean.propertyById(beanName,
						   Integer.parseInt(indexName, 
								    16));
		
		if (curBean == null)
		    throw new IllegalStateException(Common.getMessage(
			"CantFindBeanMayHaveBeenRemoved_msg", beanName,  name));

	    }
	} while (i1 != -1);
	
	return curBean;
    }
    
    
    public String getKeyPropertyName(String propName, String[] prop,
				     String[] key) {
	return this.getKeyPropertyName(propName, prop, key, false);
    }
    
    public String getKeyPropertyName(String propName) {
	return this.getKeyPropertyName(propName, null, null, true);
    }
    
    /**
     *	Return the bean holding the property 'name' as a BaseBean object
     */
    public String getKeyPropertyName(String propName, String[] prop,
				     String[] key, boolean keyName) {

	StringBuffer	keyPropName = new StringBuffer();
	BaseBean	curBean = this.root;
	String 		beanName, indexName;
	String		name = propName;
	
	if (name.charAt(0) == '/')
	    name = name.substring(1);
	
	do {
	    int i = name.indexOf('/');
	    if (i != -1) {
		beanName = name.substring(0, i);
		name = name.substring(i+1);
	    }
	    else {
		beanName = name;
		name = null;
	    }
	    
	    i = beanName.indexOf('.');
	    
	    if (i != -1) {
		indexName = beanName.substring(i+1);
		beanName = beanName.substring(0, i);
		
		if (indexName.indexOf('i') != -1)
		    throw new IllegalStateException(
		    Common.getMessage(
			"CantFindBeanBecausePartOfNameRemoved_msg",
			beanName, propName));
	    }
	    else
		indexName = "0";	// NOI18N
	    
	    
	    if (this.root.hasName(beanName)) {
		curBean = this.root;
	    } else {
		if (curBean.getProperty(beanName).isBean()) {
		    curBean = curBean.propertyById(beanName,
						   Integer.parseInt(indexName, 
								    16));
		} else
		    curBean = null;
	    }
	    
	    keyPropName.append(beanName);
	    
	    if (prop != null && curBean != null) {
		//	If a property name/key is defined, add it to the path
		for (i=0; i<prop.length; i++) {
		    if (prop[i].equals(beanName)) {
			keyPropName.append(".");	// NOI18N
			keyPropName.append(key[i]);
			keyPropName.append("=");	// NOI18N
			String v = (String)curBean.getValue(key[i], 0);
			if (v != null)
			    keyPropName.append(v);
			break;
		    }
		}
	    } else if (keyName && curBean != null) {
		//	If any property has 'name', use it
		BaseProperty[] l = curBean.listProperties();
		for (i=0; i<l.length; i++) {
		    String n = l[i].getName();
		    if (n.toLowerCase().indexOf("name") != -1) { // NOI18N
			keyPropName.append(".");	// NOI18N
			keyPropName.append(n);
			keyPropName.append("=");	// NOI18N
			String v = (String)curBean.getValue(n, 0);
			if (v != null)
			    keyPropName.append(v);
			break;
		    }
		}
	    }
	    
	    if (name != null)
		keyPropName.append("/");			// NOI18N
	    
	} while (name != null && curBean != null);
	
	return keyPropName.toString();
    }
    
    
    /**
     *	Return the bean holding the property 'name' as a BaseBean object
     */
    public static String trimPropertyName(String propName) {
	StringBuffer name = new StringBuffer();
	int i, j;
	i = 0;
	do {
	    j = propName.indexOf('.', i);
	    if (j==-1) {
		name.append(propName.substring(i));
	    } else {
		name.append(propName.substring(i, j));
		i = propName.indexOf('/', j);
	    }
	} while(j!=-1 && i!=-1);
	
	return name.toString();
    }
    
    /**
     *	Return the name of the bean holding the property 'name'
     */
    public String getPropertyParentName(String name) {
	int i = name.lastIndexOf('/');
	if (i != -1)
	    name = name.substring(0, i);
	i = name.lastIndexOf('/');
	if (i != -1)
	    name = name.substring(i+1);
	i = name.lastIndexOf('.');
	if (i != -1)
	    name = name.substring(0, i);
	
	return name;
    }
    
    /**
     *	Return the name of the property of the PropertyChangeEvent named name.
     *	Any index or attribute is removed from the name of the event.
     *
     *	single property: 	/Book/Chapter.2/Comment	-> Comment
     *	indexed property:	/Book/Chapter.4  	-> Chapter
     *	attribute:		/Book/Chapter.2:title	-> Chapter
     *
     */
    public String getPropertyName(String name) {
	int i = name.lastIndexOf('/');
	if (i != -1)
	    name = name.substring(i+1);
	//	Remove the index value
	i = name.lastIndexOf('.');
	if (i != -1)
	    name = name.substring(0, i);
	//	If there is a still an attribute, remove it
	i = name.lastIndexOf(':');
	if (i != -1)
	    name = name.substring(0, i);
	
	return name;
    }
    
    /**
     *	Return the name of the attribute if this is the name of an attribute,
     *	return null otherwise.
     *
     *	single property: 	/Book/Chapter.2/Comment	-> null
     *	indexed property:	/Book/Chapter.4  	-> null
     *	attribute:		/Book/Chapter.2:title	-> title
     *
     */
    public String getAttributeName(String name) {
	int i = name.lastIndexOf(':');
	if (i != -1)
	    name = name.substring(i+1);
	else
	    name = null;
	return name;
    }
    
    /**
     *	Return true if this is the name of an attribute
     */
    public boolean isAttribute(String name) {
	int i = name.lastIndexOf(':');
	return (i != -1);
    }
    
    /**
     *	Return the index value of the property, as a string
     */
    private String extractPropertyIndex(String name) {
	int i = name.lastIndexOf('/');
	if (i != -1)
	    name = name.substring(i+1);
	i = name.lastIndexOf('.');
	if (i != -1) {
	    name = name.substring(i+1);
	    i = name.lastIndexOf(':');
	    if (i != -1)
		name = name.substring(0, i);
	}
	else
	    name = null;
	return name;
    }
    
    
    /**
     *	If the property is an indexed property, return the index of
     *	the property.
     */
    public int getPropertyIndex(String name) {
	String index = this.extractPropertyIndex(name);
	if (index != null) {
	    int i = index.lastIndexOf('i');
	    if (i != -1) {
		//  This is a removed property - return the old value
		return Integer.parseInt(index.substring(i+1));
	    }
	    else {
		//  Get the current index value
		BaseBean bean = this.getPropertyParent(name);
		if (bean != null) {
		    BeanProp bp = bean.beanProp(this.getPropertyName(name));
		    
		    if (bp != null)
			return bp.idToIndex(Integer.parseInt(index, 16));
		}
	    }
	}
	
	return -1;
    }
    
    //
    //	Events misc. methods
    //
    /////////////////////////////
    
    static public void debug(boolean d) {
	DDLogFlags.debug = d;
    }
    
    //
    //	Default values for scalar types. The idea is to allow the user to
    //	change the following default values (TODO).
    //
    public Object defaultScalarValue(int type) {
	switch(type & Common.MASK_TYPE) {
	    case Common.TYPE_STRING:
		return "";	// NOI18N
	    case Common.TYPE_BOOLEAN:
		return Boolean.FALSE;
	    case Common.TYPE_BYTE:
		return new Byte((byte)0);
	    case Common.TYPE_CHAR:
		return new Character('\0');
	    case Common.TYPE_SHORT:
		return new Short((short)0);
	    case Common.TYPE_INT:
		return new Integer(0);
	    case Common.TYPE_LONG:
		return new Long(0);
	    case Common.TYPE_FLOAT:
		return new Float(0.0);
	    case Common.TYPE_DOUBLE:
		return new Double(0.0);
	    default:
            throw new IllegalArgumentException(Common.getMessage("UnknownType", type));
	}
    }
}

