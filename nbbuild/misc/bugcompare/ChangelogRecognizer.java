/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

public class ChangelogRecognizer implements org.xml.sax.DocumentHandler {
    private java.lang.StringBuffer buffer;
    
    private ChangelogParslet parslet;
    
    private ChangelogHandler handler;
    
    private java.util.Stack context;
    
    public ChangelogRecognizer(final ChangelogHandler handler, final ChangelogParslet parslet) {
        this.parslet = parslet;
        this.handler = handler;
        buffer = new StringBuffer(111);
        context = new java.util.Stack();
    }
    
    public void setDocumentLocator(org.xml.sax.Locator locator) {
    }
    
    public void startDocument() throws org.xml.sax.SAXException {
    }
    
    public void endDocument() throws org.xml.sax.SAXException {
    }
    
    public void startElement(java.lang.String name, org.xml.sax.AttributeList attrs) throws org.xml.sax.SAXException {
        dispatch(true);
        context.push(new Object[] {name, new org.xml.sax.helpers.AttributeListImpl(attrs)});
    }
    
    public void endElement(java.lang.String name) throws org.xml.sax.SAXException {
        dispatch(false);
        context.pop();
    }
    
    public void characters(char[] chars, int start, int len) throws org.xml.sax.SAXException {
        buffer.append(chars, start, len);
    }
    
    public void ignorableWhitespace(char[] chars, int start, int len) throws org.xml.sax.SAXException {
    }
    
    public void processingInstruction(java.lang.String target, java.lang.String data) throws org.xml.sax.SAXException {
    }
    
    private void dispatch(final boolean fireOnlyIfMixed) throws org.xml.sax.SAXException {
        if (fireOnlyIfMixed && buffer.length() == 0) return; //skip it
        
        Object[] ctx = (Object[]) context.peek();
        String here = (String) ctx[0];
        org.xml.sax.AttributeList attrs = (org.xml.sax.AttributeList) ctx[1];
        if ("author".equals(here)) {
            if (fireOnlyIfMixed) throw new IllegalStateException("Unexpected mixed content element or a parser reporting whitespaces via characters() event!");
            handler.handle_author(buffer.length() == 0 ? null : buffer.toString(), attrs);
        } else if ("branch".equals(here)) {
            if (fireOnlyIfMixed) throw new IllegalStateException("Unexpected mixed content element or a parser reporting whitespaces via characters() event!");
            handler.handle_branch(buffer.length() == 0 ? null : buffer.toString(), attrs);
        } else if ("branchroot".equals(here)) {
            if (fireOnlyIfMixed) throw new IllegalStateException("Unexpected mixed content element or a parser reporting whitespaces via characters() event!");
            handler.handle_branchroot(buffer.length() == 0 ? null : buffer.toString(), attrs);
        } else if ("changelog".equals(here)) {
            if (fireOnlyIfMixed) throw new IllegalStateException("Unexpected mixed content element or a parser reporting whitespaces via characters() event!");
            handler.handle_changelog(buffer.length() == 0 ? null : buffer.toString(), attrs);
        } else if ("commondir".equals(here)) {
            if (fireOnlyIfMixed) throw new IllegalStateException("Unexpected mixed content element or a parser reporting whitespaces via characters() event!");
            handler.handle_commondir(buffer.length() == 0 ? null : buffer.toString(), attrs);
        } else if ("date".equals(here)) {
            if (fireOnlyIfMixed) throw new IllegalStateException("Unexpected mixed content element or a parser reporting whitespaces via characters() event!");
            handler.handle_date(buffer.length() == 0 ? null : buffer.toString(), attrs);
        } else if ("entry".equals(here)) {
            if (fireOnlyIfMixed) throw new IllegalStateException("Unexpected mixed content element or a parser reporting whitespaces via characters() event!");
            handler.handle_entry(buffer.length() == 0 ? null : buffer.toString(), attrs);
        } else if ("file".equals(here)) {
            if (fireOnlyIfMixed) throw new IllegalStateException("Unexpected mixed content element or a parser reporting whitespaces via characters() event!");
            handler.handle_file(buffer.length() == 0 ? null : buffer.toString(), attrs);
        } else if ("msg".equals(here)) {
            if (fireOnlyIfMixed) throw new IllegalStateException("Unexpected mixed content element or a parser reporting whitespaces via characters() event!");
            handler.handle_msg(buffer.length() == 0 ? null : buffer.toString(), attrs);
        } else if ("name".equals(here)) {
            if (fireOnlyIfMixed) throw new IllegalStateException("Unexpected mixed content element or a parser reporting whitespaces via characters() event!");
            handler.handle_name(buffer.length() == 0 ? null : buffer.toString(), attrs);
        } else if ("revision".equals(here)) {
            if (fireOnlyIfMixed) throw new IllegalStateException("Unexpected mixed content element or a parser reporting whitespaces via characters() event!");
            handler.handle_revision(buffer.length() == 0 ? null : buffer.toString(), attrs);
        } else if ("tag".equals(here)) {
            if (fireOnlyIfMixed) throw new IllegalStateException("Unexpected mixed content element or a parser reporting whitespaces via characters() event!");
            handler.handle_tag(buffer.length() == 0 ? null : buffer.toString(), attrs);
        } else if ("time".equals(here)) {
            if (fireOnlyIfMixed) throw new IllegalStateException("Unexpected mixed content element or a parser reporting whitespaces via characters() event!");
            handler.handle_time(buffer.length() == 0 ? null : buffer.toString(), attrs);
        } else if ("utag".equals(here)) {
            if (fireOnlyIfMixed) throw new IllegalStateException("Unexpected mixed content element or a parser reporting whitespaces via characters() event!");
            handler.handle_utag(buffer.length() == 0 ? null : buffer.toString(), attrs);
        } else if ("weekday".equals(here)) {
            if (fireOnlyIfMixed) throw new IllegalStateException("Unexpected mixed content element or a parser reporting whitespaces via characters() event!");
            handler.handle_weekday(buffer.length() == 0 ? null : buffer.toString(), attrs);
        } else {
            //do not care
        }
        buffer.delete(0, buffer.length());
    }
    
    /**
     * The recognizer entry method taking an InputSource.
     * @param input InputSource to be parsed.
     * @throws java.io.IOException on I/O error.
     * @throws org.xml.sax.SAXException propagated exception thrown by a DocumentHandler.
     * @throws javax.xml.parsers.ParserConfigurationException a parser satisfining requested configuration can not be created.
     * @throws javax.xml.parsers.FactoryConfigurationRrror if the implementation can not be instantiated.
     */
    public void parse(final org.xml.sax.InputSource input) throws org.xml.sax.SAXException, javax.xml.parsers.ParserConfigurationException, java.io.IOException {
        parse(input, this);
    }
    
    /**
     * The recognizer entry method taking a URL.
     * @param url URL source to be parsed.
     * @throws java.io.IOException on I/O error.
     * @throws org.xml.sax.SAXException propagated exception thrown by a DocumentHandler.
     * @throws javax.xml.parsers.ParserConfigurationException a parser satisfining requested configuration can not be created.
     * @throws javax.xml.parsers.FactoryConfigurationRrror if the implementation can not be instantiated.
     */
    public void parse(final java.net.URL url) throws org.xml.sax.SAXException, javax.xml.parsers.ParserConfigurationException, java.io.IOException {
        parse(new org.xml.sax.InputSource(url.toExternalForm()), this);
    }
    
    /**
     * The recognizer entry method taking an Inputsource.
     * @param input InputSource to be parsed.
     * @throws java.io.IOException on I/O error.
     * @throws org.xml.sax.SAXException propagated exception thrown by a DocumentHandler.
     * @throws javax.xml.parsers.ParserConfigurationException a parser satisfining requested configuration can not be created.
     * @throws javax.xml.parsers.FactoryConfigurationRrror if the implementation can not be instantiated.
     */
    public static void parse(final org.xml.sax.InputSource input, final ChangelogHandler handler, final ChangelogParslet parslet) throws org.xml.sax.SAXException, javax.xml.parsers.ParserConfigurationException, java.io.IOException {
        parse(input, new ChangelogRecognizer(handler, parslet));
    }
    
    /**
     * The recognizer entry method taking a URL.
     * @param url URL source to be parsed.
     * @throws java.io.IOException on I/O error.
     * @throws org.xml.sax.SAXException propagated exception thrown by a DocumentHandler.
     * @throws javax.xml.parsers.ParserConfigurationException a parser satisfining requested configuration can not be created.
     * @throws javax.xml.parsers.FactoryConfigurationRrror if the implementation can not be instantiated.
     */
    public static void parse(final java.net.URL url, final ChangelogHandler handler, final ChangelogParslet parslet) throws org.xml.sax.SAXException, javax.xml.parsers.ParserConfigurationException, java.io.IOException {
        parse(new org.xml.sax.InputSource(url.toExternalForm()), handler, parslet);
    }
    
    private static void parse(final org.xml.sax.InputSource input, final ChangelogRecognizer recognizer) throws org.xml.sax.SAXException, javax.xml.parsers.ParserConfigurationException, java.io.IOException {
        javax.xml.parsers.SAXParserFactory factory = javax.xml.parsers.SAXParserFactory.newInstance();
        factory.setValidating(false);  //the code was generated according DTD
        factory.setNamespaceAware(false);  //the code was generated according DTD
        org.xml.sax.Parser parser = factory.newSAXParser().getParser();
        parser.setDocumentHandler(recognizer);
        parser.parse(input);
    }
    
    
}