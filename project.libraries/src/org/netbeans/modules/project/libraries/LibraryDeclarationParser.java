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

package org.netbeans.modules.project.libraries;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Stack;
import javax.xml.parsers.ParserConfigurationException;
import org.openide.xml.XMLUtil;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

/**
 * The class reads XML documents according to specified DTD and
 * translates all related events into LibraryDeclarationHandler events.
 * <p>Usage sample:
 * <pre>
 *    LibraryDeclarationParser parser = new LibraryDeclarationParser(...);
 *    parser.parse(new InputSource("..."));
 * </pre>
 * <p><b>Warning:</b> the class is machine generated. DO NOT MODIFY</p>
 *
 */
public class LibraryDeclarationParser implements ContentHandler, EntityResolver {
    
    private StringBuffer buffer;
    
    private LibraryDeclarationConvertor parslet;
    
    private LibraryDeclarationHandler handler;
    
    private Stack<Object[]> context;

    
    /**
     * Creates a parser instance.
     * @param handler handler interface implementation (never <code>null</code>
     * It is recommended that it could be able to resolve at least the DTD.@param parslet convertors implementation (never <code>null</code>
     *
     */
    public LibraryDeclarationParser(final LibraryDeclarationHandler handler, final LibraryDeclarationConvertor parslet) {
        this.parslet = parslet;
        this.handler = handler;
        buffer = new StringBuffer(111);
        context = new Stack<Object[]>();
    }
    
    /**
     * This SAX interface method is implemented by the parser.
     *
     */
    public final void setDocumentLocator(Locator locator) {
    }
    
    /**
     * This SAX interface method is implemented by the parser.
     *
     */
    public final void startDocument() throws SAXException {
    }
    
    /**
     * This SAX interface method is implemented by the parser.
     *
     */
    public final void endDocument() throws SAXException {
    }
    
    /**
     * This SAX interface method is implemented by the parser.
     *
     */
    public final void startElement(String ns, String name, String qname, Attributes attrs) throws SAXException {
        dispatch(true);
        context.push(new Object[] {qname, new AttributesImpl(attrs)});
        if ("volume".equals(qname)) {
            handler.start_volume(attrs);
        } else if ("library".equals(qname)) {
            handler.start_library(attrs);
        }
    }
    
    /**
     * This SAX interface method is implemented by the parser.
     *
     */
    public final void endElement(String ns, String name, String qname) throws SAXException {
        dispatch(false);
        context.pop();
        if ("volume".equals(qname)) {
            handler.end_volume();
        } else if ("library".equals(qname)) {
            handler.end_library();
        }
    }
    
    /**
     * This SAX interface method is implemented by the parser.
     *
     */
    public final void characters(char[] chars, int start, int len) throws SAXException {
        buffer.append(chars, start, len);
    }
    
    /**
     * This SAX interface method is implemented by the parser.
     *
     */
    public final void ignorableWhitespace(char[] chars, int start, int len) throws SAXException {
    }
    
    /**
     * This SAX interface method is implemented by the parser.
     *
     */
    public final void processingInstruction(String target, String data) throws SAXException {
    }
    
    /**
     * This SAX interface method is implemented by the parser.
     *
     */
    public final void startPrefixMapping(final String prefix, final String uri) throws SAXException {
    }
    
    /**
     * This SAX interface method is implemented by the parser.
     *
     */
    public final void endPrefixMapping(final String prefix) throws SAXException {
    }
    
    /**
     * This SAX interface method is implemented by the parser.
     *
     */
    public final void skippedEntity(String name) throws SAXException {
    }
    
    private void dispatch(final boolean fireOnlyIfMixed) throws SAXException {
        if (fireOnlyIfMixed && buffer.length() == 0) return; //skip it
        
        Object[] ctx = context.peek();
        String here = (String) ctx[0];
        Attributes attrs = (Attributes) ctx[1];
        if ("description".equals(here)) {
            if (fireOnlyIfMixed) throw new IllegalStateException("Unexpected characters() event! (Missing DTD?)");
            handler.handle_description (buffer.length() == 0 ? null : buffer.toString(), attrs);
        } else if ("type".equals(here)) {
            if (fireOnlyIfMixed) throw new IllegalStateException("Unexpected characters() event! (Missing DTD?)");
            handler.handle_type(buffer.length() == 0 ? null : buffer.toString(), attrs);
        } else if ("resource".equals(here)) {
            if (fireOnlyIfMixed) throw new IllegalStateException("Unexpected characters() event! (Missing DTD?)");
            handler.handle_resource(parslet.parseResource(buffer.length() == 0 ? null : buffer.toString()), attrs);
        } else if ("name".equals(here)) {
            if (fireOnlyIfMixed) throw new IllegalStateException("Unexpected characters() event! (Missing DTD?)");
            handler.handle_name(buffer.length() == 0 ? null : buffer.toString(), attrs);
        } else if ("localizing-bundle".equals(here)) {
            if (fireOnlyIfMixed) throw new IllegalStateException("Unexpected characters() event! (Missing DTD?)");
            handler.handle_localizingBundle(buffer.length() == 0 ? null : buffer.toString(), attrs);
        } else {
            //do not care
        }
        buffer.delete(0, buffer.length());
    }
    
    /**
     * The recognizer entry method taking an InputSource.
     * @param input InputSource to be parsed.
     * @throws java.io.IOException on I/O error.
     * @throws SAXException propagated exception thrown by a DocumentHandler.
     * @throws javax.xml.parsers.ParserConfigurationException a parser satisfining requested configuration can not be created.
     * @throws javax.xml.parsers.FactoryConfigurationRrror if the implementation can not be instantiated.
     *
     */
    public void parse(final InputSource input) throws SAXException, ParserConfigurationException, IOException {
        parse(input, this);
    }
    
    /**
     * The recognizer entry method taking a URL.
     * @param url URL source to be parsed.
     * @throws java.io.IOException on I/O error.
     * @throws SAXException propagated exception thrown by a DocumentHandler.
     * @throws javax.xml.parsers.ParserConfigurationException a parser satisfining requested configuration can not be created.
     * @throws javax.xml.parsers.FactoryConfigurationRrror if the implementation can not be instantiated.
     *
     */
    public void parse(final URL url) throws SAXException, ParserConfigurationException, IOException {
        parse(new InputSource(url.toExternalForm()), this);
    }
    
    /**
     * The recognizer entry method taking an Inputsource.
     * @param input InputSource to be parsed.
     * @throws java.io.IOException on I/O error.
     * @throws SAXException propagated exception thrown by a DocumentHandler.
     * @throws javax.xml.parsers.ParserConfigurationException a parser satisfining requested configuration can not be created.
     * @throws javax.xml.parsers.FactoryConfigurationRrror if the implementation can not be instantiated.
     *
     */
    public static void parse(final InputSource input, final LibraryDeclarationHandler handler, final LibraryDeclarationConvertor parslet) throws SAXException, ParserConfigurationException, IOException {
        parse(input, new LibraryDeclarationParser(handler, parslet));
    }
    
    /**
     * The recognizer entry method taking a URL.
     * @param url URL source to be parsed.
     * @throws java.io.IOException on I/O error.
     * @throws SAXException propagated exception thrown by a DocumentHandler.
     * @throws javax.xml.parsers.ParserConfigurationException a parser satisfining requested configuration can not be created.
     * @throws javax.xml.parsers.FactoryConfigurationRrror if the implementation can not be instantiated.
     *
     */
    public static void parse(final URL url, final LibraryDeclarationHandler handler, final LibraryDeclarationConvertor parslet) throws SAXException, ParserConfigurationException, IOException {
        parse(new InputSource(url.toExternalForm()), handler, parslet);
    }
    
    private static void parse(final InputSource input, final LibraryDeclarationParser recognizer) throws SAXException, ParserConfigurationException, IOException {
        try {
            XMLReader parser = XMLUtil.createXMLReader(false, false);
            parser.setContentHandler(recognizer);
            parser.setErrorHandler(recognizer.getDefaultErrorHandler());
            parser.setEntityResolver(recognizer);
            parser.parse(input);
        } finally {
            //Recover recognizer internal state from exceptions to be reusable
            if (!recognizer.context.empty()) {
                recognizer.context.clear();
            }
            if (recognizer.buffer.length() > 0) {
                recognizer.buffer.delete(0, recognizer.buffer.length());
            }
        }
    }
    
    /**
     * Creates default error handler used by this parser.
     * @return org.xml.sax.ErrorHandler implementation
     *
     */
    protected ErrorHandler getDefaultErrorHandler() {
        return new ErrorHandler() {
            public void error(SAXParseException ex) throws SAXException  {
                throw ex;
            }
            
            public void fatalError(SAXParseException ex) throws SAXException {
                throw ex;
            }
            
            public void warning(SAXParseException ex) throws SAXException {
                // ignore
            }
        };
        
    }
    
    /** Implementation of entity resolver. Points to the local DTD
     * for our public ID */
    public InputSource resolveEntity (String publicId, String systemId)
    throws SAXException {
        if ("-//NetBeans//DTD Library Declaration 1.0//EN".equals(publicId)) {
            InputStream is = new ByteArrayInputStream(new byte[0]);
            return new InputSource(is);
        }
        return null; // i.e. follow advice of systemID
    }
}

