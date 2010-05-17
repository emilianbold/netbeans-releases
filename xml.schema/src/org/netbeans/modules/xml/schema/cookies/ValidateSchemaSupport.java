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

package org.netbeans.modules.xml.schema.cookies;

import java.io.*;
import java.net.*;

import org.xml.sax.*;

import org.netbeans.spi.xml.cookies.*;
import org.netbeans.api.xml.parsers.SAXEntityParser;
import org.netbeans.api.xml.services.UserCatalog;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.xml.sax.helpers.DefaultHandler;

/**
 * CheckXMLCookie and ValidateXMLCookie implementation for XML Schemas.
 *
 * @author  Petr Kuzel
 */
public final class ValidateSchemaSupport extends ValidateXMLSupport {
    
    /** Creates a new instance of CheckSchemaSupport */
    public ValidateSchemaSupport(InputSource inputSource) {
        super( inputSource);
    }

    /**
     * In validating mode create XMLReader able to parse XML Schemas.
     */
    protected XMLReader createParser(boolean validate) {
        final String XERCES_FEATURE_PREFIX = "http://apache.org/xml/features/";         // NOI18N
        
        XMLReader parser = super.createParser(validate);
        if (parser == null) return null;
        
        // for validaton use wrapping parser
        if (validate) {
            // we urgently need XML Schema aware parser
            try {
                if (parser.getFeature(XERCES_FEATURE_PREFIX + "validation/schema") == false) {  // NOI18N
                    //??? try get Xerces explicitly, no such library except ou outdate X2b4 exists
                    return null;
                } else {
                    // check all schema oddities with Xerces parser
                    parser.setFeature(XERCES_FEATURE_PREFIX + "validation/schema-full-checking", true);  // NOI18N
                    return new SchemaChecker(parser);
                }
            } catch (SAXException ex) {
                return null;
            }
            
        } else {
            return parser;
        }
    }

    /**
     * Redefine wrapping policy.
     */
    private static class SchemaChecker extends SAXEntityParser {
        
        private static String SCHEMA_NS = "http://www.w3.org/2001/XMLSchema";
        
        // schema target ns
        private String ns = null;
        
        public SchemaChecker(XMLReader parser) {
            super( parser);
        }

        /**
         * Create fake document referencing XML Schema document.
         * Besides it must guarantee that the first entity resolution
         * query caused by parsing of wrapped document is
         * resolved at a context where XML Schema InputSource
         * is expected. It is SAXEntityParser logic.
         */
        protected InputSource wrapInputSource(InputSource inputSource) {
            String targetNamespace = getTargetNamespace();
            String url = inputSource.getSystemId();
            StringBuffer buffer = new StringBuffer(256);
            String namespace = "http://www.w3.org/2001/XMLSchema-instance";     // NOI18N
            buffer.append("<schemaWrapper xmlns:xsi='" + namespace + "' ");     // NOI18N
            
            
            if (targetNamespace != null) {
                buffer.append("xmlns='").append(targetNamespace).append("' ");              // NOI18N
                buffer.append("xsi:schemaLocation='").append(targetNamespace).append(' ').append(url).append("'/>");    // NOI18N
            } else {
                buffer.append("xsi:noNamespaceSchemaLocation='").append(url).append("'/>"); // NOI18N
            }
            StringReader reader = new StringReader(buffer.toString());
            InputSource input = new InputSource();
            input.setCharacterStream(reader);
            return input;
        }
        
        /**
         * Filter out all complains about wrong namespace.
         */
        protected boolean propagateException(SAXParseException ex) {
            if (super.propagateException(ex)) {
                //??? it works with most parser impls :-(
                String message = ex.getMessage();
                if (message == null) return true;  //???
                if (getTargetNamespace() == null || message.indexOf(getTargetNamespace()) < 0) {
                    return message.indexOf("schemaWrapper") < 0;                // NOI18N
                }
            }
            return false;
        }

        public void parse(InputSource input) throws SAXException, IOException {            
            ShareableInputSource shared = ShareableInputSource.create(input);
            try {
                ns = parseForTargetNamespace(shared);
                shared.reset();                
                super.parse(shared);
            } finally {
                shared.closeAll();
            }
        }
        
        /**
         * @return ns or <code>null</code> is chameleon schema.
         */
        private String getTargetNamespace() {
            return ns;
        }

        private String parseForTargetNamespace(InputSource schema) throws SAXException, IOException {
            try {
                XMLReader reader = XMLUtil.createXMLReader(false, true);
                EntityResolver resolver = UserCatalog.getDefault().getEntityResolver(); 
                if (resolver != null) {
                    reader.setEntityResolver(resolver);
                }

                TargetNSScanner sniffer = new TargetNSScanner();
                reader.setContentHandler(sniffer);
                reader.setErrorHandler(sniffer);
                reader.parse(schema);
            } catch (Stop ex) {
                return ex.getNamespace();
            } 
            return null;
        }

        // root element must be schema element
        private class TargetNSScanner extends DefaultHandler {
            public void startElement(String uri, String local, String qname, Attributes attrs) throws SAXException {
                if ("schema".equals(local) && SCHEMA_NS.equals(uri)) {          // NOI18N
                    String targetNS = attrs.getValue("targetNamespace");        // NOI18N
                    throw new Stop(targetNS);
                }
                // no schema root element
                throw new SAXException(NbBundle.getMessage(ValidateSchemaSupport.class, "MSG_missing_schema"));
            }
        }
        
        private static class Stop extends SAXException {
            Stop(String ns) {
                super(ns);
            }

            String getNamespace() {
                return getMessage();
            }
        }
    }
}
