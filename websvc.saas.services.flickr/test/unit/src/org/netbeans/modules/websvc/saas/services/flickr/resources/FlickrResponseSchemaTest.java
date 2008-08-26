/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.saas.services.flickr.resources;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author lukas
 */
public class FlickrResponseSchemaTest extends NbTestCase {

    private static final Logger LOG = Logger.getLogger(FlickrResponseSchemaTest.class.getName());
    private static Schema schema;
    private File file;


    static {
        // create a SchemaFactory capable of understanding WXS schemas
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        // load a WXS schema, represented by a Schema instance
        String resourceName = "/org/netbeans/modules/websvc/saas/services/flickr/resources/FlickrResponse.xsd"; //NOI18N
        Source schemaFile = new StreamSource(FlickrResponseSchemaTest.class.getResourceAsStream(resourceName));
        try {
            schema = factory.newSchema(schemaFile);
        } catch (SAXException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private FlickrResponseSchemaTest(String name) {
        super(name);
    }

    private FlickrResponseSchemaTest(String name, File f) {
        super(name);
        this.file = f;
    }

    public static TestSuite suite() {
        return createTestSuite(new FlickrResponseSchemaTest("empty")); //NOI18N
    }

    public void validate() {
        assertNotNull("null schema", schema); //NOI18N
        LOG.fine("Validating: " + file.getName()); //NOI18N
        try {
            // parse an XML document into a DOM tree
            DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = parser.parse(file);

            Validator validator = schema.newValidator();
            try {
                // validate the DOM tree
                validator.validate(new DOMSource(document));
            } catch (SAXException ex) {
                fail("validation of " + file.getName() + " failed: " + ex.getLocalizedMessage()); //NOI18N
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        } catch (SAXException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private static TestSuite createTestSuite(NbTestCase t) {
        TestSuite ts = new NbTestSuite();
        File dataDir = t.getDataDir();
        File[] testCases = dataDir.listFiles();
        Arrays.sort(testCases);
        for (File f : testCases) {
            if (f.isFile()) {
                ts.addTest(new FlickrResponseSchemaTest("validate", f)); //NOI18N
            }
        }
        return ts;
    }
}
