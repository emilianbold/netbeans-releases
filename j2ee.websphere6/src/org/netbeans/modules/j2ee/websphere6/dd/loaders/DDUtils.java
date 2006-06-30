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
package org.netbeans.modules.j2ee.websphere6.dd.loaders;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
//import org.netbeans.modules.j2ee.websphere6.ddloaders.webbnd.*;
import org.openide.filesystems.FileObject;

import org.netbeans.modules.schema2beans.BaseBean;

/*import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.impl.web.WebAppProxy;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;*/
import java.io.*;
import org.xml.sax.*;
//import java.util.*;


/**
 *
 * @author  mkuchtiak
 * @author  dlipin
 */
public class DDUtils {

    private static final String EXCEPTION_PREFIX="version"; //NOI18N   
    
    /** Finds a name similar to requested that uniquely identifies 
     *  element between the other elements of the same name.
     *
     * @param elements checked elements
     * @param identifier name of tag that contains identification value
     * @param o object to be checked
     * @return a free element name
     */
     
      

    /** Parsing just for detecting the version  SAX parser used
    */
    public static String getVersion(InputSource is) throws IOException, SAXException {
        javax.xml.parsers.SAXParserFactory fact = javax.xml.parsers.SAXParserFactory.newInstance();
        fact.setValidating(false);
        try {
            javax.xml.parsers.SAXParser parser = fact.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            reader.setContentHandler(new VersionHandler());
            reader.setEntityResolver(DDResolver.getInstance());
            try {
                reader.parse(is);
            } catch (SAXException ex) {
                String message = ex.getMessage();
                if (message!=null && message.startsWith(EXCEPTION_PREFIX))
                    return message.substring(EXCEPTION_PREFIX.length());
                else throw new SAXException(org.openide.util.NbBundle.getMessage(DDUtils.class, "MSG_cannotParse"),ex);
            }
            throw new SAXException(org.openide.util.NbBundle.getMessage(DDUtils.class, "MSG_cannotFindRoot"));
        } catch(javax.xml.parsers.ParserConfigurationException ex) {
            throw new SAXException(org.openide.util.NbBundle.getMessage(DDUtils.class, "MSG_parserProblem"),ex);
        }
    }
    
    private static class VersionHandler extends org.xml.sax.helpers.DefaultHandler {
        public void startElement(String uri, String localName, String rawName, Attributes atts) throws SAXException {
            if ("WebAppBinging".equals(rawName)) { //NOI18N
                String version = atts.getValue("version"); //NOI18N
                throw new SAXException(EXCEPTION_PREFIX);
            }
        }
    }
  
    private static class DDResolver implements EntityResolver {
        static DDResolver resolver;
        static synchronized DDResolver getInstance() {
            if (resolver==null) {
                resolver=new DDResolver();
            }
            return resolver;
        }        
        public InputSource resolveEntity (String publicId, String systemId) {
            String resource=null;
            // return a proper input source
            if ("-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN".equals(publicId)) { //NOI18N
                resource="/org/netbeans/modules/j2ee/dd/impl/resources/web-app_2_3.dtd"; //NOI18N
            } else if ("-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN".equals(publicId)) { //NOI18N
                resource="/org/netbeans/modules/j2ee/dd/impl/resources/web-app_2_2.dtd"; //NOI18N
            } else if (systemId!=null && systemId.endsWith("web-app_2_4.xsd")) {
                resource="/org/netbeans/modules/j2ee/dd/impl/resources/web-app_2_4.xsd"; //NOI18N
            }
            if (resource==null) return null;
            java.net.URL url = this.getClass().getResource(resource);
            return new InputSource(url.toString());
        }
    }
    
    private static class ErrorHandler implements org.xml.sax.ErrorHandler {
        private int errorType=-1;
        SAXParseException error;

        public void warning(org.xml.sax.SAXParseException sAXParseException) throws org.xml.sax.SAXException {
            if (errorType<0) {
                errorType=0;
                error=sAXParseException;
            }
            //throw sAXParseException;
        }
        public void error(org.xml.sax.SAXParseException sAXParseException) throws org.xml.sax.SAXException {
            if (errorType<1) {
                errorType=1;
                error=sAXParseException;
            }
            //throw sAXParseException;
        }        
        public void fatalError(org.xml.sax.SAXParseException sAXParseException) throws org.xml.sax.SAXException {
            errorType=2;
            throw sAXParseException;
        }
        
        public int getErrorType() {
            return errorType;
        }
        public SAXParseException getError() {
            return error;
        }        
    }
    
    public static SAXParseException parse (InputSource is) 
            throws org.xml.sax.SAXException, java.io.IOException {
        ErrorHandler errorHandler = new ErrorHandler();
        try {
            SAXParser parser = createSAXParserFactory().newSAXParser();
            XMLReader reader = parser.getXMLReader();
            reader.setErrorHandler(errorHandler);
            //reader.setEntityResolver(DDResolver.getInstance());
            //reader.setFeature("http://apache.org/xml/features/validation/schema", true); // NOI18N
            //reader.setFeature("http://xml.org/sax/features/validation",  true); // NOI18N
            //reader.setFeature("http://xml.org/sax/features/namespaces",  true); // NOI18N
            reader.parse(is);
            SAXParseException error = errorHandler.getError();
            if (error!=null) return error;
        } catch (ParserConfigurationException ex) {
            throw new SAXException(ex.getMessage());
        } catch (SAXException ex) {
            throw ex;
        }
        return null;
    }
    
    /** Method that retrieves SAXParserFactory to get the parser prepared to validate against XML schema
     */
    private static SAXParserFactory createSAXParserFactory() throws ParserConfigurationException {
        try {
            SAXParserFactory fact = SAXParserFactory.newInstance();
            if (fact!=null) {
                try {
                    fact.getClass().getMethod("getSchema", new Class[]{}); //NOI18N
                    return fact;
                } catch (NoSuchMethodException ex) {}
            }
            return (SAXParserFactory) Class.forName("org.apache.xerces.jaxp.SAXParserFactoryImpl").newInstance(); // NOI18N
        } catch (Exception ex) {
            throw new ParserConfigurationException(ex.getMessage());
        }
    }
}