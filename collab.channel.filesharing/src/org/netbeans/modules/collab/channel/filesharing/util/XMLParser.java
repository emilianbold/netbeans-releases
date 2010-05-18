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
