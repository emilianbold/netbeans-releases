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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
            if (oldValue == null) {
                System.clearProperty(PROPERTY_NAME_DOCUMENT_BUILDER_FACTORY);
            } else {
                System.setProperty(PROPERTY_NAME_DOCUMENT_BUILDER_FACTORY, oldValue);
            }
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
