/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Utility class collecting library methods related to XML processing.
 * @author Petr Kuzel, Jesse Glick
 */
public final class XMLUtil extends Object {

    @SuppressWarnings("unchecked")
    private static final ThreadLocal<DocumentBuilder>[] builderTL = new ThreadLocal[4];
    static {
        for (int i = 0; i < 4; i++) {
            builderTL[i] = new ThreadLocal<DocumentBuilder>();
        }
    }
    public static Document parse (
            InputSource input, 
            boolean validate, 
            boolean namespaceAware,
            ErrorHandler errorHandler,             
            EntityResolver entityResolver
        ) throws IOException, SAXException {
        
        int index = (validate ? 0 : 1) + (namespaceAware ? 0 : 2);
        DocumentBuilder builder = builderTL[index].get();
        if (builder == null) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(validate);
            factory.setNamespaceAware(namespaceAware);

            try {
                builder = factory.newDocumentBuilder();
            } catch (ParserConfigurationException ex) {
                throw new SAXException(ex);
            }
            builderTL[index].set(builder);
        }
        
        if (errorHandler != null) {
            builder.setErrorHandler(errorHandler);
        }
        
        if (entityResolver != null) {
            builder.setEntityResolver(entityResolver);
        }
        
        return builder.parse(input);
    }
    
    public static Document createDocument(String rootQName) throws DOMException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            return factory.newDocumentBuilder().getDOMImplementation().createDocument(null, rootQName, null);
        } catch (ParserConfigurationException ex) {
            throw (DOMException)new DOMException(DOMException.NOT_SUPPORTED_ERR, "Cannot create parser").initCause(ex); // NOI18N
        }
    }
    
    private static DOMImplementation getDOMImplementation() throws DOMException { //can be made public
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    
        try {
            return factory.newDocumentBuilder().getDOMImplementation();
        } catch (ParserConfigurationException ex) {
            throw (DOMException)new DOMException(DOMException.NOT_SUPPORTED_ERR, "Cannot create parser").initCause(ex); // NOI18N
        }        
    }

    // Cf. org.openide.xml.XMLUtil.
    private static final String IDENTITY_XSLT_WITH_INDENT =
            "<xsl:stylesheet version='1.0' " + // NOI18N
            "xmlns:xsl='http://www.w3.org/1999/XSL/Transform' " + // NOI18N
            "xmlns:xalan='http://xml.apache.org/xslt' " + // NOI18N
            "exclude-result-prefixes='xalan'>" + // NOI18N
            "<xsl:output method='xml' indent='yes' xalan:indent-amount='4'/>" + // NOI18N
            "<xsl:template match='@*|node()'>" + // NOI18N
            "<xsl:copy>" + // NOI18N
            "<xsl:apply-templates select='@*|node()'/>" + // NOI18N
            "</xsl:copy>" + // NOI18N
            "</xsl:template>" + // NOI18N
            "</xsl:stylesheet>"; // NOI18N

    public static void write(Document doc, OutputStream out) throws IOException {
        // XXX note that this may fail to write out namespaces correctly if the document
        // is created with namespaces and no explicit prefixes; however no code in
        // this package is likely to be doing so
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer(
                    new StreamSource(new StringReader(IDENTITY_XSLT_WITH_INDENT)));
            DocumentType dt = doc.getDoctype();
            if (dt != null) {
                String pub = dt.getPublicId();
                if (pub != null) {
                    t.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, pub);
                }
                t.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, dt.getSystemId());
            }
            t.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); // NOI18N
            Source source = new DOMSource(doc);
            Result result = new StreamResult(out);
            t.transform(source, result);
        } catch (Exception e) {
            throw (IOException)new IOException(e.toString()).initCause(e);
        } catch (TransformerFactoryConfigurationError e) {
            throw (IOException)new IOException(e.toString()).initCause(e);
        }
    }

    public static void write(Element el, OutputStream out) throws IOException {
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer(
                    new StreamSource(new StringReader(IDENTITY_XSLT_WITH_INDENT)));
            t.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); // NOI18N
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            Source source = new DOMSource(el);
            Result result = new StreamResult(out);
            t.transform(source, result);
        } catch (Exception e) {
            throw (IOException) new IOException(e.toString()).initCause(e);
        } catch (TransformerFactoryConfigurationError e) {
            throw (IOException) new IOException(e.toString()).initCause(e);
        }
    }

    /**
     * Search for an XML element in the direct children of a parent.
     * DOM provides a similar method but it does a recursive search
     * which we do not want. It also gives a node list and we want
     * only one result.
     * @param parent a parent element
     * @param name the intended local name
     * @param namespace the intended namespace (or null)
     * @return the one child element with that name, or null if none or more than one
     */
    public static Element findElement(Element parent, String name, String namespace) {
        Element result = null;
        NodeList l = parent.getChildNodes();
        for (int i = 0; i < l.getLength(); i++) {
            if (l.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element)l.item(i);
                if ((namespace == null && name.equals(el.getTagName())) ||
                    (namespace != null && name.equals(el.getLocalName()) &&
                                          namespace.equals(el.getNamespaceURI()))) {
                    if (result == null) {
                        result = el;
                    } else {
                        return null;
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * Extract nested text from an element.
     * Currently does not handle coalescing text nodes, CDATA sections, etc.
     * @param parent a parent element
     * @return the nested text, or null if none was found
     */
    static String findText(Element parent) {
        NodeList l = parent.getChildNodes();
        for (int i = 0; i < l.getLength(); i++) {
            if (l.item(i).getNodeType() == Node.TEXT_NODE) {
                Text text = (Text)l.item(i);
                return text.getNodeValue();
            }
        }
        return null;
    }
    
    /**
     * Find all direct child elements of an element.
     * More useful than {@link Element#getElementsByTagNameNS} because it does
     * not recurse into recursive child elements.
     * Children which are all-whitespace text nodes or comments are ignored; others cause
     * an exception to be thrown.
     * @param parent a parent element in a DOM tree
     * @return a list of direct child elements (may be empty)
     * @throws IllegalArgumentException if there are non-element children besides whitespace
     */
    static List<Element> findSubElements(Element parent) throws IllegalArgumentException {
        NodeList l = parent.getChildNodes();
        List<Element> elements = new ArrayList<Element>(l.getLength());
        for (int i = 0; i < l.getLength(); i++) {
            Node n = l.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                elements.add((Element)n);
            } else if (n.getNodeType() == Node.TEXT_NODE) {
                String text = ((Text)n).getNodeValue();
                if (text.trim().length() > 0) {
                    throw new IllegalArgumentException("non-ws text encountered in " + parent + ": " + text); // NOI18N
                }
            } else if (n.getNodeType() == Node.COMMENT_NODE) {
                // OK, ignore
            } else {
                throw new IllegalArgumentException("unexpected non-element child of " + parent + ": " + n); // NOI18N
            }
        }
        return elements;
    }
    
}
