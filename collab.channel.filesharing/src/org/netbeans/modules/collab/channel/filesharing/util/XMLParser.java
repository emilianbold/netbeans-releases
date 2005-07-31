/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.channel.filesharing.util;

import com.sun.collablet.CollabException;

import org.openide.xml.EntityCatalog;
import org.w3c.dom.*;
import org.xml.sax.*;

import java.io.*;

import java.net.*;

import javax.xml.parsers.*;


/**
 *
 * @author Ayub Khan ayub.khan@sun.com
 */
public class XMLParser {
    /**
     * returns the root element of the DOM constructed from given URL (XML doc.)
     */
    public static Element parse(URL url) throws CollabException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        dbf.setNamespaceAware(true);

        // No need to validate
        dbf.setValidating(false);
        dbf.setIgnoringComments(true);
        dbf.setIgnoringElementContentWhitespace(true);

        DocumentBuilder db = null;

        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new CollabException(ex);
        }

        //Set ErrorHandler
        OutputStreamWriter errorWriter = new OutputStreamWriter(System.err);
        db.setErrorHandler(new MyErrorHandler(new PrintWriter(errorWriter, true)));

        //Set entity resolver. Not really needed, since we're not validating
        db.setEntityResolver(EntityCatalog.getDefault());

        Document doc = null;

        try {
            doc = db.parse(url.toString());
        } catch (java.net.MalformedURLException murlex) {
            throw new CollabException(murlex);
        } catch (org.xml.sax.SAXException saxx) {
            throw new CollabException(saxx);
        } catch (java.io.IOException iox) {
            throw new CollabException(iox);
        }

        return doc.getDocumentElement();
    }

    // Error handler to report errors and warnings
    private static class MyErrorHandler implements ErrorHandler {
        /** Error handler output goes here */
        private PrintWriter out;

        MyErrorHandler(PrintWriter out) {
            this.out = out;
        }

        /**
         * Returns a string describing parse exception details
         */
        private String getParseExceptionInfo(SAXParseException spe) {
            String systemId = spe.getSystemId();

            if (systemId == null) {
                systemId = "null"; // NOI18N
            }

            String info = "URI=" + systemId + // NOI18N
                " Line=" + spe.getLineNumber() + // NOI18N
                ": " + spe.getMessage(); // NOI18N

            return info;
        }

        // The following methods are standard SAX ErrorHandler methods.
        // See SAX documentation for more info.
        public void warning(SAXParseException spe) throws SAXException {
            out.println("Warning: " + getParseExceptionInfo(spe)); // NOI18N
        }

        public void error(SAXParseException spe) throws SAXException {
            String message = "Error: " + getParseExceptionInfo(spe); // NOI18N
            throw new SAXException(message);
        }

        public void fatalError(SAXParseException spe) throws SAXException {
            String message = "Fatal Error: " + getParseExceptionInfo(spe); // NOI18N
            throw new SAXException(message);
        }
    }
}
