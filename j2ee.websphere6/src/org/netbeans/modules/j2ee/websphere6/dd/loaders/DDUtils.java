/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.j2ee.websphere6.dd.loaders;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
//import org.netbeans.modules.j2ee.websphere6.ddloaders.webbnd.*;

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
//    public static String getVersion(InputSource is) throws IOException, SAXException {
//        javax.xml.parsers.SAXParserFactory fact = javax.xml.parsers.SAXParserFactory.newInstance();
//        fact.setValidating(false);
//        try {
//            javax.xml.parsers.SAXParser parser = fact.newSAXParser();
//            XMLReader reader = parser.getXMLReader();
//            reader.setContentHandler(new VersionHandler());
//            reader.setEntityResolver(DDResolver.getInstance());
//            try {
//                reader.parse(is);
//            } catch (SAXException ex) {
//                String message = ex.getMessage();
//                if (message!=null && message.startsWith(EXCEPTION_PREFIX))
//                    return message.substring(EXCEPTION_PREFIX.length());
//                else throw new SAXException(org.openide.util.NbBundle.getMessage(DDUtils.class, "MSG_cannotParse"),ex);
//            }
//            throw new SAXException(org.openide.util.NbBundle.getMessage(DDUtils.class, "MSG_cannotFindRoot"));
//        } catch(javax.xml.parsers.ParserConfigurationException ex) {
//            throw new SAXException(org.openide.util.NbBundle.getMessage(DDUtils.class, "MSG_parserProblem"),ex);
//        }
//    }
    
//    private static class VersionHandler extends org.xml.sax.helpers.DefaultHandler {
//        public void startElement(String uri, String localName, String rawName, Attributes atts) throws SAXException {
//            if ("WebAppBinging".equals(rawName)) { //NOI18N
//                String version = atts.getValue("version"); //NOI18N
//                throw new SAXException(EXCEPTION_PREFIX);
//            }
//        }
//    }
  
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
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
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
}
