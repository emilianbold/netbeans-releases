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

package org.netbeans.modules.project.libraries;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.openide.xml.XMLUtil;
import org.xml.sax.*;

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
    
    private java.lang.StringBuffer buffer;
    
    private LibraryDeclarationConvertor parslet;
    
    private LibraryDeclarationHandler handler;
    
    private java.util.Stack context;

    
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
        context = new java.util.Stack();
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
    public final void startElement(java.lang.String ns, java.lang.String name, java.lang.String qname, Attributes attrs) throws SAXException {
        dispatch(true);
        context.push(new Object[] {qname, new org.xml.sax.helpers.AttributesImpl(attrs)});
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
    public final void endElement(java.lang.String ns, java.lang.String name, java.lang.String qname) throws SAXException {
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
    public final void processingInstruction(java.lang.String target, java.lang.String data) throws SAXException {
    }
    
    /**
     * This SAX interface method is implemented by the parser.
     *
     */
    public final void startPrefixMapping(final java.lang.String prefix, final java.lang.String uri) throws SAXException {
    }
    
    /**
     * This SAX interface method is implemented by the parser.
     *
     */
    public final void endPrefixMapping(final java.lang.String prefix) throws SAXException {
    }
    
    /**
     * This SAX interface method is implemented by the parser.
     *
     */
    public final void skippedEntity(java.lang.String name) throws SAXException {
    }
    
    private void dispatch(final boolean fireOnlyIfMixed) throws SAXException {
        if (fireOnlyIfMixed && buffer.length() == 0) return; //skip it
        
        Object[] ctx = (Object[]) context.peek();
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
    public void parse(final InputSource input) throws SAXException, javax.xml.parsers.ParserConfigurationException, java.io.IOException {
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
    public void parse(final java.net.URL url) throws SAXException, javax.xml.parsers.ParserConfigurationException, java.io.IOException {
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
    public static void parse(final InputSource input, final LibraryDeclarationHandler handler, final LibraryDeclarationConvertor parslet) throws SAXException, javax.xml.parsers.ParserConfigurationException, java.io.IOException {
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
    public static void parse(final java.net.URL url, final LibraryDeclarationHandler handler, final LibraryDeclarationConvertor parslet) throws SAXException, javax.xml.parsers.ParserConfigurationException, java.io.IOException {
        parse(new InputSource(url.toExternalForm()), handler, parslet);
    }
    
    private static void parse(final InputSource input, final LibraryDeclarationParser recognizer) throws SAXException, javax.xml.parsers.ParserConfigurationException, java.io.IOException {
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

