/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.xml.xsd;

import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.XMLReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import org.netbeans.api.xml.services.UserCatalog;

/**
 *
 * @author  anovak
 */
public class XSDParser {

    /** Creates a new instance of XSDParser */
    public XSDParser() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Error: missing file parameter required or too many args");
        }
        
        java.io.FileInputStream fistr = new java.io.FileInputStream(args[0]);
        
        SAXParserFactory factory = SAXParserFactory.newInstance();
        XMLReader reader = factory.newSAXParser().getXMLReader();
        XSDContentHandler handler = new XSDContentHandler(System.out);
        reader.setContentHandler(handler);
        reader.parse(new InputSource(fistr));
    }

    public XSDGrammar parse(InputSource in) {

        XSDContentHandler handler = new XSDContentHandler(System.out);
        
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            XMLReader reader = factory.newSAXParser().getXMLReader();

            UserCatalog catalog = UserCatalog.getDefault();
            EntityResolver res = (catalog == null ? null : catalog.getEntityResolver());

            if (res != null) { 
                reader.setEntityResolver(res);
            }

            reader.setContentHandler(handler);
            reader.parse(in);
            return handler.getGrammar();
        } catch (org.xml.sax.SAXException ex) {
            if (Boolean.getBoolean("netbeans.debug.xml") ||  Boolean.getBoolean("netbeans.debug.exceptions")) {  //NOI18N
                ex.printStackTrace();            
                if (ex.getException() instanceof RuntimeException) {
                    ex.getException().printStackTrace();  //???
                }            
            }
            return handler.getGrammar();  // better partial result than nothing
        } catch (java.io.IOException ex) {
            if (Boolean.getBoolean("netbeans.debug.xml")) {  // NOI18N
                ex.printStackTrace();
            }
            return handler.getGrammar();  // better partial result than nothing
        } catch (javax.xml.parsers.ParserConfigurationException e) {
            if (Boolean.getBoolean("netbeans.debug.xml")) {  // NOI18N
                e.printStackTrace();
            }
            return handler.getGrammar();  // better partial result than nothing
        }            
    }
}
