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
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
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

        XMLReader reader = XMLUtil.createXMLReader(false);
        reader.setContentHandler(versionHandler);
        reader.setEntityResolver(ddResolver);
        try {
            reader.parse(new InputSource(is));
        } catch (SAXException ex) {
            is.close();
            String message = ex.getMessage();
            if (message != null && message.startsWith(EXCEPTION_PREFIX)) {
                return message.substring(EXCEPTION_PREFIX.length());
            } else {
                throw new SAXException(NbBundle.getMessage(ParseUtils.class, "MSG_cannotParse"), ex);
            }
        }
        is.close();
        throw new SAXException(NbBundle.getMessage(ParseUtils.class, "MSG_cannotFindRoot"));
    }
    
    /** Parsing just for detecting the version  SAX parser used
    */
    public static String getVersion(InputSource is, org.xml.sax.helpers.DefaultHandler versionHandler, 
            EntityResolver ddResolver) throws IOException, SAXException {
        XMLReader reader = XMLUtil.createXMLReader(false);
        reader.setContentHandler(versionHandler);
        reader.setEntityResolver(ddResolver);
        try {
            reader.parse(is);
        } catch (SAXException ex) {
            String message = ex.getMessage();
            if (message != null && message.startsWith(EXCEPTION_PREFIX)) {
                return message.substring(EXCEPTION_PREFIX.length());
            } else {
                throw new SAXException(NbBundle.getMessage(ParseUtils.class, "MSG_cannotParse"), ex);
            }
        }
        throw new SAXException(NbBundle.getMessage(ParseUtils.class, "MSG_cannotFindRoot"));
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
            XMLReader reader = XMLUtil.createXMLReader();
            reader.setErrorHandler(errorHandler);
            reader.setEntityResolver(ddResolver);
            reader.setFeature("http://apache.org/xml/features/validation/schema", true); // NOI18N
            reader.setFeature("http://xml.org/sax/features/validation",  true); // NOI18N
            reader.setFeature("http://xml.org/sax/features/namespaces",  true); // NOI18N
            reader.parse(is);
            SAXParseException error = errorHandler.getError();
            if (error!=null) return error;
        } catch (IllegalArgumentException ex) {
            // yes, this may happen, see issue #71738
            SAXException sax = new SAXException(ex.getMessage(), ex);
            sax.initCause(ex);
            throw sax;
        }
        return null;
    }
  
}
