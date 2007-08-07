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

package org.netbeans.updater;

import java.io.IOException;
import java.io.OutputStream;
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
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Utility class collecting library methods related to XML processing.
 * Stolen from nbbuild/antsrc and openide/.../xml.
 * @author Petr Kuzel, Jesse Glick
 */
public final class XMLUtil extends Object {

    public static Document parse (
            InputSource input, 
            boolean validate, 
            boolean namespaceAware,
            ErrorHandler errorHandler,             
            EntityResolver entityResolver
        ) throws IOException, SAXException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();        
        factory.setValidating(validate);
        factory.setNamespaceAware(namespaceAware);            
        DocumentBuilder builder = null;
        try {
             builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new SAXException(ex);
        }
            
        if (errorHandler != null) {
            builder.setErrorHandler(errorHandler);
        }
        
        if (entityResolver != null) {
            builder.setEntityResolver(entityResolver);
        }

        assert input != null : "InputSource cannot be null";
        
        return builder.parse(input);            
    }
    
    public static Document createDocument(String rootQName) throws DOMException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            return factory.newDocumentBuilder ().getDOMImplementation ().createDocument (null, rootQName, null);
        } catch (ParserConfigurationException ex) {
            throw (DOMException)new DOMException(DOMException.NOT_SUPPORTED_ERR, "Cannot create parser").initCause(ex); // NOI18N
        }
    }
    
    public static void write(Document doc, OutputStream out) throws IOException {
        // XXX note that this may fail to write out namespaces correctly if the document
        // is created with namespaces and no explicit prefixes; however no code in
        // this package is likely to be doing so
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            DocumentType dt = doc.getDoctype();
            if (dt != null) {
                String pub = dt.getPublicId();
                if (pub != null) {
                    t.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, pub);
                }
                t.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, dt.getSystemId());
            }
            t.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); // NOI18N
            t.setOutputProperty(OutputKeys.INDENT, "yes"); // NOI18N
            t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); // NOI18N
            Source source = new DOMSource(doc);
            Result result = new StreamResult(out);
            t.transform(source, result);
        } catch (Exception e) {
            throw (IOException)new IOException(e.toString()).initCause(e);
        } catch (TransformerFactoryConfigurationError e) {
            throw (IOException)new IOException(e.toString()).initCause(e);
        }
    }

    /** Entity resolver that knows about AU DTDs, so no network is needed.
     * @author Jesse Glick
     */
    public static EntityResolver createAUResolver() {
        return new EntityResolver() {
            public InputSource resolveEntity(String publicID, String systemID) throws IOException, SAXException {
                if ("-//NetBeans//DTD Autoupdate Catalog 1.0//EN".equals(publicID)) { // NOI18N
                    return new InputSource(XMLUtil.class.getResource("resources/autoupdate-catalog-1_0.dtd").toString());
                } else if ("-//NetBeans//DTD Autoupdate Module Info 1.0//EN".equals(publicID)) { // NOI18N
                    return new InputSource(XMLUtil.class.getResource("resources/autoupdate-info-1_0.dtd").toString());
                } else if ("-//NetBeans//DTD Autoupdate Catalog 2.0//EN".equals(publicID)) { // NOI18N
                    return new InputSource(XMLUtil.class.getResource("resources/autoupdate-catalog-2_0.dtd").toString());
                } else if ("-//NetBeans//DTD Autoupdate Module Info 2.0//EN".equals(publicID)) { // NOI18N
                    return new InputSource(XMLUtil.class.getResource("resources/autoupdate-info-2_0.dtd").toString());
                } else if ("-//NetBeans//DTD Autoupdate Catalog 2.2//EN".equals(publicID)) { // NOI18N
                    return new InputSource(XMLUtil.class.getResource("resources/autoupdate-catalog-2_2.dtd").toString());
                } else if ("-//NetBeans//DTD Autoupdate Module Info 2.2//EN".equals(publicID)) { // NOI18N
                    return new InputSource(XMLUtil.class.getResource("resources/autoupdate-info-2_2.dtd").toString());
                } else if ("-//NetBeans//DTD Autoupdate Catalog 2.3//EN".equals(publicID)) { // NOI18N
                    return new InputSource(XMLUtil.class.getResource("resources/autoupdate-catalog-2_3.dtd").toString());
                } else if ("-//NetBeans//DTD Autoupdate Module Info 2.3//EN".equals(publicID)) { // NOI18N
                    return new InputSource(XMLUtil.class.getResource("resources/autoupdate-info-2_3.dtd").toString());
                } else if ("-//NetBeans//DTD Autoupdate Catalog 2.4//EN".equals(publicID)) { // NOI18N
                    return new InputSource(XMLUtil.class.getResource("resources/autoupdate-catalog-2_4.dtd").toString());
                } else if ("-//NetBeans//DTD Autoupdate Module Info 2.4//EN".equals(publicID)) { // NOI18N
                    return new InputSource(XMLUtil.class.getResource("resources/autoupdate-info-2_4.dtd").toString());
                } else if ("-//NetBeans//DTD Autoupdate Catalog 2.5//EN".equals(publicID)) { // NOI18N
                    return new InputSource(XMLUtil.class.getResource("resources/autoupdate-catalog-2_5.dtd").toString());
                } else if ("-//NetBeans//DTD Autoupdate Module Info 2.5//EN".equals(publicID)) { // NOI18N
                    return new InputSource(XMLUtil.class.getResource("resources/autoupdate-info-2_5.dtd").toString());
                } else {
                    return null;
                }
            }
        };
    }
    
}
