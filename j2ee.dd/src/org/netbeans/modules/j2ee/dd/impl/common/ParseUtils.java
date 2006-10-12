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

package org.netbeans.modules.j2ee.dd.impl.common;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.xml.sax.*;


/** Class that collects XML parsing utility methods for web applications. It is 
 * implementation private for this module, however it is also intended to be used by 
 * the DDLoaders modules, which requires tighter coupling with ddapi and has an 
 * implementation dependency on it.
 *
 * @author Petr Jiricka
 */
public class ParseUtils {
  
    public static final String EXCEPTION_PREFIX="version:"; //NOI18N
    
    /** Parsing just for detecting the version  SAX parser used
     */
    public static String getVersion(java.io.InputStream is, org.xml.sax.helpers.DefaultHandler versionHandler,
        EntityResolver ddResolver) throws java.io.IOException, SAXException {
        javax.xml.parsers.SAXParserFactory fact = javax.xml.parsers.SAXParserFactory.newInstance();
        fact.setValidating(false);
        try {
            javax.xml.parsers.SAXParser parser = fact.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            reader.setContentHandler(versionHandler);
            reader.setEntityResolver(ddResolver);
            try {
                reader.parse(new InputSource(is));
            } catch (SAXException ex) {
                is.close();
                String message = ex.getMessage();
                if (message!=null && message.startsWith(EXCEPTION_PREFIX))
                    return message.substring(EXCEPTION_PREFIX.length());
                else throw new SAXException(NbBundle.getMessage(ParseUtils.class, "MSG_cannotParse"),ex);
            }
            is.close();
            throw new SAXException(NbBundle.getMessage(ParseUtils.class, "MSG_cannotFindRoot"));
        } catch(javax.xml.parsers.ParserConfigurationException ex) {
            throw new SAXException(NbBundle.getMessage(ParseUtils.class, "MSG_parserProblem"),ex);
        }
    }
    
    /** Parsing just for detecting the version  SAX parser used
    */
    public static String getVersion(InputSource is, org.xml.sax.helpers.DefaultHandler versionHandler,
        EntityResolver ddResolver) throws IOException, SAXException {
        javax.xml.parsers.SAXParserFactory fact = javax.xml.parsers.SAXParserFactory.newInstance();
        fact.setValidating(false);
        try {
            javax.xml.parsers.SAXParser parser = fact.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            reader.setContentHandler(versionHandler);
            reader.setEntityResolver(ddResolver);
            try {
                reader.parse(is);
            } catch (SAXException ex) {
                String message = ex.getMessage();
                if (message!=null && message.startsWith(EXCEPTION_PREFIX))
                    return message.substring(EXCEPTION_PREFIX.length());
                else throw new SAXException(NbBundle.getMessage(ParseUtils.class, "MSG_cannotParse"),ex);
            }
            throw new SAXException(NbBundle.getMessage(ParseUtils.class, "MSG_cannotFindRoot"));
        } catch(javax.xml.parsers.ParserConfigurationException ex) {
            throw new SAXException(NbBundle.getMessage(ParseUtils.class, "MSG_parserProblem"),ex);
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
    
    public static SAXParseException parseDD(InputSource is, EntityResolver ddResolver) 
            throws org.xml.sax.SAXException, java.io.IOException {
        ErrorHandler errorHandler = new ErrorHandler();
        try {
            SAXParser parser = createSAXParserFactory().newSAXParser();
            XMLReader reader = parser.getXMLReader();
            reader.setErrorHandler(errorHandler);
            reader.setEntityResolver(ddResolver);
            reader.setFeature("http://apache.org/xml/features/validation/schema", true); // NOI18N
            reader.setFeature("http://xml.org/sax/features/validation",  true); // NOI18N
            reader.setFeature("http://xml.org/sax/features/namespaces",  true); // NOI18N
            reader.parse(is);
            SAXParseException error = errorHandler.getError();
            if (error!=null) return error;
        } catch (ParserConfigurationException ex) {
            SAXException sax = new SAXException(ex.getMessage(), ex);
            sax.initCause(ex);
            throw sax;
        } catch (SAXException ex) {
            throw ex;
        } catch (IllegalArgumentException ex) {
            // yes, this may happen, see issue #71738
            SAXException sax = new SAXException(ex.getMessage(), ex);
            sax.initCause(ex);
            throw sax;
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
