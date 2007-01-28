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
package org.netbeans.modules.visualweb.designer.markup;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;




/**
 * XXX Copied from former insync/Factories
 *
 * <p>Description: </p>
 * @author Carl Quinn
 * @version 1.0
 */
final class RaveDocumentBuilderFactory {

    DocumentBuilderFactory domFactory;  // DOM parser factory
    //SAXParserFactory saxFactory;        // SAX parser factory

    private static RaveDocumentBuilderFactory instance;

    private static final String PROPERTY_NAME_DOCUMENT_BUILDER_FACTORY
        = "javax.xml.parsers.DocumentBuilderFactory"; // NOI18N
    private static final String PROPERTY_VALUE_XERCES_DOCUMENT_BUILDER_FACTORY
        = "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl"; // NOI18N

    /**
     * Make the singleton & instantiate some shared worker objects
     */
    private RaveDocumentBuilderFactory() {
        //tidy = new Tidy();
        //Configuration conf = tidy.getConfiguration();

        // XXX CHECK Assuring the xerces is used, which document is expected (implementing EvenTarget) in insync.
        String oldValue = System.getProperty(PROPERTY_NAME_DOCUMENT_BUILDER_FACTORY);
        try {
            System.setProperty(PROPERTY_NAME_DOCUMENT_BUILDER_FACTORY, PROPERTY_VALUE_XERCES_DOCUMENT_BUILDER_FACTORY);

            // Setup our XML DOM Document Builder (parser) factory
            domFactory = DocumentBuilderFactory.newInstance();
            //domFactory.setCoalescing(false);
            //domFactory.setExpandEntityReferences(false);
            //domFactory.setIgnoringComments(false);
            //domFactory.setIgnoringElementContentWhitespace(true);
            domFactory.setNamespaceAware(true);
            domFactory.setValidating(false);
            //domFactory.setFeature("http://apache.org/xml/features/continue-after-fatal-error",
            // true);
            /*
              Trace.trace("insync.markup",
                          "MUF DocumentBuilderFactory" +
                               " c:" + domFactory.isCoalescing() +
                               " eer:" + domFactory.isExpandEntityReferences() +
                               " ic:" + domFactory.isIgnoringComments() +
                               " iecw:" + domFactory.isIgnoringElementContentWhitespace() +
                               " na:" + domFactory.isNamespaceAware() +
                               " v:" + domFactory.isValidating());
             Trace.out.flush();*/

            // Setup
	    //saxFactory = SAXParserFactory.newInstance();
            //saxFactory.setNamespaceAware(true);
            //saxFactory.setValidating(false);
            //to set features: saxFactory.setFeature("", true);
        }
        catch (FactoryConfigurationError ex) {
//            Trace.trace("insync.markup", "Exception creating factory");
//            Trace.trace("insync.markup", e);
            ex.printStackTrace();
        } finally {
           System.setProperty(PROPERTY_NAME_DOCUMENT_BUILDER_FACTORY, oldValue);
        }
    }

    //SAXParser newSaxParser() throws ParserConfigurationException, SAXException {
    //    return saxFactory.newSAXParser();
    //}

    public static DocumentBuilder newDocumentBuilder(boolean useCss, boolean sourceDocument) throws ParserConfigurationException {
        return getInstance().createDocumentBuilder(useCss, sourceDocument);
    }

    private DocumentBuilder createDocumentBuilder(boolean useCss, boolean sourceDocument) throws ParserConfigurationException {
        if (useCss) {
            java.util.Hashtable attrs = null;
            // TODO - duplicate any attributes?
            try {
                return new RaveDocumentBuilder(domFactory, attrs, sourceDocument);
            } catch (Exception ex) {
                ex.printStackTrace();
                // fall through and use document builder below
            }
            /* I MIGHT be able to do this instead. But how do I set the property?
               There's no setProperty. setFeature doesn't seem to be it.
            domFactory.setProperty("http://apache.org/xml/properties/dom/document-class-name",
                                   "org.netbeans.modules.visualweb.insync.markup.RaveDocument");
            */
        }
        return domFactory.newDocumentBuilder();
    }

    private static synchronized RaveDocumentBuilderFactory getInstance() {
        if (instance == null)
            instance = new RaveDocumentBuilderFactory();
        return instance;
    }

}
