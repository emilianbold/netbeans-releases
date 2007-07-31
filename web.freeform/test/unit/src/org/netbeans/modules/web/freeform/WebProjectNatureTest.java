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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.freeform;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import org.netbeans.junit.NbTestCase;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * Test stuff in WebProjectNature.
 * @author Jesse Glick, Tomas Mysik
 * @see org.netbeans.modules.java.freeform.JavaProjectNature
 */
public class WebProjectNatureTest extends NbTestCase {

    public WebProjectNatureTest(String name) {
        super(name);
    }

    public void testUpgradeSchema() throws Exception {
        // Formatting has to be the same as Xerces' formatter produces for this test to pass:
        String xml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                      "<web-data xmlns=\"http://www.netbeans.org/ns/freeform-project-web/1\">\n" +
                      "    <!-- Hello there. -->\n" +
                      "    <foo bar=\"baz\" quux=\"whatever\">hello</foo>\n" +
                      "    <x>OK</x>\n" +
                      "</web-data>\n";
        String xml2expected = xml1.replaceAll("/1", "/2");
        Document doc1 = XMLUtil.parse(new InputSource(new StringReader(xml1)), false, true, null, null);
        Element el1 = doc1.getDocumentElement();
        Element el2 = LookupProviderImpl.upgradeSchema(el1);
        Document doc2 = XMLUtil.createDocument(WebProjectNature.EL_WEB, WebProjectNature.NS_WEB_2, null, null);
        doc2.removeChild(doc2.getDocumentElement());
        doc2.appendChild(doc2.importNode(el2, true));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtil.write(doc2, baos, "UTF-8");
        String xml2actual = baos.toString("UTF-8").replaceAll(System.getProperty("line.separator"), "\n");
        assertEquals("Correct upgrade result", xml2expected, xml2actual);
    }
    
}
