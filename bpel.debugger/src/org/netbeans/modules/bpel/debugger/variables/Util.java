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

package org.netbeans.modules.bpel.debugger.variables;

import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Alexander Zgursky
 */
public final class Util {
    
    /** Creates a new instance of Util */
    private Util() {
    }
    
    public static Element parseXmlElement(String xml) {
        Document doc = null;
        if (xml != null && xml.length() > 0) {
            InputSource is = new InputSource(new StringReader(xml));
            try {
                doc = getDocumentBuilder().parse(is);
            } catch (ParserConfigurationException e) {
                //TODO:handle this properly
                //e.printStackTrace();
            } catch (IOException e) {
                //TODO:handle this properly
                //e.printStackTrace();
            } catch (SAXException e) {
                //TODO:handle this properly
                //e.printStackTrace();
            }
        }

        if (doc != null) {
            return doc.getDocumentElement();
        } else {
            return null;
        }
    }
    
    private static DocumentBuilder cDocumentBuilder;
    
    private static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        if (cDocumentBuilder == null) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringComments(false);
            factory.setNamespaceAware(true);
            factory.setIgnoringElementContentWhitespace(false);
            cDocumentBuilder = factory.newDocumentBuilder();
        }
        return cDocumentBuilder;
    }
}
