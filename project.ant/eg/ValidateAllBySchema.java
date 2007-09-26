/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

import java.io.File;
import java.util.Arrays;
import java.util.StringTokenizer;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Tries to validate all the example XML metadata files
 * according to available XML schemas.
 * @author Jesse Glick
 */
public class ValidateAllBySchema {

    /**
     * args[0] = comma-separated list of files to validate
     * args[1] = comma-separated list of available XML Schema files
     */
    public static void main(String[] args) throws Exception {
        File[] xmls = split(args[0]);
        File[] schemas = split(args[1]);
        String[] schemaUris = new String[schemas.length];
        for (int i = 0; i < schemas.length; i++) {
            schemaUris[i] = schemas[i].toURI().toString();
        }
        System.err.println("Validating against " + Arrays.asList(schemas));
        SAXParserFactory f;
        // #46847: needs to work on both JDK 1.4 and 1.5.
        try {
            f = (SAXParserFactory)Class.forName("com.sun.org.apache.xerces.jaxp.SAXParserFactoryImpl").newInstance();
        } catch (ClassNotFoundException e) {
            f = (SAXParserFactory)Class.forName("org.apache.xerces.jaxp.SAXParserFactoryImpl").newInstance();
        }
        f.setNamespaceAware(true);
        f.setValidating(true);
        SAXParser p = f.newSAXParser();
        p.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                      "http://www.w3.org/2001/XMLSchema");
        p.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource",
                      schemaUris);
        int exit = 0;
        for (int i = 0; i < xmls.length; i++) {
            System.err.println("Parsing " + xmls[i] + "...");
            try {
                p.parse(xmls[i].toURI().toString(), new Handler());
            } catch (SAXParseException e) {
                System.err.println(e.getSystemId() + ":" + e.getLineNumber() + ": " + e.getLocalizedMessage());
                exit = 1;
            }
        }
        System.err.println("All files validated.");
        System.exit(exit);
    }
    private static final class Handler extends DefaultHandler {
        public void warning(SAXParseException e) throws SAXException {
            throw e;
        }
        public void error(SAXParseException e) throws SAXException {
            throw e;
        }
        public void fatalError(SAXParseException e) throws SAXException {
            throw e;
        }
    }
    private static File[] split(String s) {
        StringTokenizer tok = new StringTokenizer(s, ",");
        File[] files = new File[tok.countTokens()];
        for (int i = 0; i < files.length; i++) {
            files[i] = new File(tok.nextToken());
        }
        return files;
    }
}
