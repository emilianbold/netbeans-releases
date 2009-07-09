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
package org.netbeans.modules.web.frameworks.facelets.parser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Petr Pisl
 */

public class SaxParser {
    public static String DEFAULT_PREFIX = "$default$";
    public static String DEFAULT_TEMPLATE = "$default_template$";
    
    private final static Pattern XmlDeclaration = Pattern.compile("^<\\?xml.+?version=['\"](.+?)['\"](.+?encoding=['\"]((.+?))['\"])?.*?\\?>");
    
    /** Creates a new instance of SaxParser */
    public SaxParser() {
    }
    
    private static class ParserHandler extends DefaultHandler implements
            LexicalHandler {
        
        private ParserResult result;
        private String faceletPrefix = "ui";    //NOI18N
        
        public ParserHandler(ParserResult res){
            this.result = res;
        }
        
        public void startDTD(String name, String publicId, String systemId) throws SAXException {
        }
        
        public void endDTD() throws SAXException {
        }
        
        public void startEntity(String name) throws SAXException {
            //System.out.println("startEntity: " + name);
        }
        
        public void endEntity(String name) throws SAXException {
            //System.out.println("endEntity: " + name);
        }
        
        public void startCDATA() throws SAXException {
        }
        
        public void endCDATA() throws SAXException {
        }
        
        public void startPrefixMapping(String prefix, String uri)
        throws SAXException {
            if (prefix == null || "".equals(prefix))
                prefix =DEFAULT_PREFIX;
            result.getPrefixMapper().put(prefix, uri);
            if ("http://java.sun.com/jsf/facelets".equals(uri))
                faceletPrefix = prefix;
            //System.out.println("startPrefixMapping: " + prefix + " -> " + uri);
        }
        
        public void endPrefixMapping(String prefix) throws SAXException {
            //System.out.println("endPrefixMapping - prefix: " + prefix);
        }
        
        public void startElement(String uri, String localName, String qName,
                Attributes attrs) throws SAXException {
            //System.out.println("startElement (uri: " + uri + ", localName: " + localName + ", qName: " + qName);
            if (qName.equals(faceletPrefix+":insert")){
                String value = attrs.getValue("name");
                if (value == null || "".equals(value))
                    value = DEFAULT_TEMPLATE;
                result.getTemplateData().add(value);
            }
//                for (int i = 0; i < attrs.getLength(); i++) {
//                    System.out.println("   uri: " +  attrs.getURI(i) + ", localName: " +attrs.getLocalName(i)
//                    + ", qName: " + attrs.getQName(i) + ", value: " + attrs.getValue(i));
//                }
        }
        
        public void endElement(String uri, String localName, String qName)
        throws SAXException {
//            System.out.println("endElement (uri: " + uri + ", localName: " + localName
//                    + ", qName: " + qName);
        }
        
        public void comment(char[] ch, int start, int length) throws SAXException {
        }
        
        public InputSource resolveEntity(String publicId, String systemId)
        throws SAXException {
//            System.out.println("resolveEntity: " + publicId + " - " + systemId);
            String dtd = "default.dtd";
            /*if ("-//W3C//DTD XHTML 1.0 Transitional//EN".equals(publicId)) {
                dtd = "xhtml1-transitional.dtd";
            } else if (systemId != null && systemId.startsWith("file:/")) {
                return new InputSource(systemId);
            }*/
            URL url = Thread.currentThread().getContextClassLoader()
            .getResource(dtd);
            return new InputSource(url.toString());
        }
    }
    
    
    public ParserResult doCompile(InputStream src) throws IOException {
        InputStream is = null;
        String encoding = "UTF-8";
        ParserResultImpl result = new ParserResultImpl();
        
        try {
            is = new BufferedInputStream(src, 1024);
            
            result.setEncoding(writeXmlDecl(is));
            
            ParserHandler handler = new ParserHandler(result);
            SAXParser parser = this.createSAXParser(handler);
            parser.parse(is, handler);
        } catch (SAXException e) {
            e.printStackTrace(System.out);
        } catch (ParserConfigurationException e) {
            e.printStackTrace(System.out);
        } finally {
            if (is != null) {
                is.close();
            }
        }
        
        return result;
    }
    
    protected static final String writeXmlDecl(InputStream is)
            throws IOException {
        is.mark(128);
        String encoding = "UTF-8";
        try {
            byte[] b = new byte[128];
            if (is.read(b) > 0) {
                String r = new String(b);
                Matcher m = XmlDeclaration.matcher(r);
                if (m.find()) {
                    if (m.group(3) != null) {
                        encoding = m.group(3);
                    }
                }
            }
        } finally {
            is.reset();
        }
        return encoding;
    }
    
    private final SAXParser createSAXParser(ParserHandler handler)
    throws SAXException, ParserConfigurationException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://xml.org/sax/features/namespace-prefixes",
                true);
        factory.setFeature("http://xml.org/sax/features/validation", true);
        factory.setValidating(true);
        SAXParser parser = factory.newSAXParser();
        XMLReader reader = parser.getXMLReader();
        reader.setProperty("http://xml.org/sax/properties/lexical-handler",
                handler);
        reader.setErrorHandler(handler);
        reader.setEntityResolver(handler);
        return parser;
    }
    
    static class ParserResultImpl implements ParserResult{
        
        private String encoding;
        private Map<String,String>prefixMapper;
        private Collection<String> templatesData;
        
        /** Creates a new instance of ParserResultImpl */
        public ParserResultImpl(){
            encoding = "UTF-8";
            prefixMapper = new Hashtable();
            templatesData = new ArrayList();
        }
        public ParserResultImpl(String encoding, Map prefixMapper, Collection templatesData) {
            this.encoding = encoding; //NOI18N
            this.prefixMapper = prefixMapper;
            this.templatesData = templatesData;
        }
        
        public String getEncoding() {
            return encoding;
        }
        
        protected void setEncoding(String encoding){
            this.encoding = encoding;
        }
        
        public Map<String, String> getPrefixMapper() {
            return prefixMapper;
        }
        
        public Collection<String> getTemplateData() {
            return templatesData;
        }
        
        public String toString(){
            StringBuffer sb = new StringBuffer();
            sb.append("Encoding: ").append(encoding);
            sb.append("\nPrefix Mapping Count: ").append(prefixMapper.size());
            if (prefixMapper.size()>0)
                for (String prefix : prefixMapper.keySet()) {
                    sb.append("\n    ").append(prefixMapper.get(prefix));
                    sb.append(" -> ").append(prefix);
                }
            sb.append("Templates Count: ").append(templatesData.size());
            for (String name : templatesData) {
                sb.append("\n").append(name);
            }
            return sb.toString();
        }
    }
}
