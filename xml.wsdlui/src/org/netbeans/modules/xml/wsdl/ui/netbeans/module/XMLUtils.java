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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.wsdl.ui.netbeans.module;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

public class XMLUtils {


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
            throws org.xml.sax.SAXException, ParserConfigurationException, java.io.IOException {
        XMLUtils.ErrorHandler errorHandler = new XMLUtils.ErrorHandler();
        try {
            javax.xml.parsers.SAXParserFactory factory = javax.xml.parsers.SAXParserFactory.newInstance();
            javax.xml.parsers.SAXParser saxParser = null;
            XMLReader reader = null;
            
            //Set namespaceAware to true to get a parser that corresponds to
            // the default SAX2 namespace feature setting.  This is necessary
            // because the default value from JAXP 1.0 was defined to be false.
            factory.setNamespaceAware(true);
            //factory.setValidating(true);
                
            saxParser = factory.newSAXParser();
                
            
            
            reader = saxParser.getXMLReader();
            
            reader.setErrorHandler(errorHandler);
            //reader.setFeature("http://xml.org/sax/features/validation",  true); // NOI18N
            //reader.setFeature("http://xml.org/sax/features/namespaces",  true); // NOI18N
            reader.parse(is);
            SAXParseException error = errorHandler.getError();
            if (error!=null) return error;
        } catch (SAXException ex) {
            throw ex;
        } catch (ParserConfigurationException ex) {
            throw ex;
        }
        return null;
    }
   
}

