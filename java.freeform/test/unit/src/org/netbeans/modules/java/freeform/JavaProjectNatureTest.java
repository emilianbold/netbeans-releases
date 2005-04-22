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

package org.netbeans.modules.java.freeform;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import org.netbeans.junit.NbTestCase;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * Test stuff in JavaProjectNature.
 * @author Jesse Glick
 */
public class JavaProjectNatureTest extends NbTestCase {
    
    public JavaProjectNatureTest(String name) {
        super(name);
    }
    
    public void testUpgradeSchema() throws Exception {
        // Formatting has to be the same as Xerces' formatter produces for this test to pass:
        String xml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                      "<java-data xmlns=\"http://www.netbeans.org/ns/freeform-project-java/1\">\n" +
                      "    <!-- Hello there. -->\n" +
                      "    <foo bar=\"baz\" quux=\"whatever\">hello</foo>\n" +
                      "    <x>OK</x>\n" +
                      "</java-data>\n";
        String xml2expected = xml1.replaceAll("/1", "/2");
        Document doc1 = XMLUtil.parse(new InputSource(new StringReader(xml1)), false, true, null, null);
        Element el1 = doc1.getDocumentElement();
        Element el2 = JavaProjectNature.upgradeSchema(el1);
        Document doc2 = XMLUtil.createDocument(JavaProjectNature.EL_JAVA, JavaProjectNature.NS_JAVA_2, null, null);
        doc2.removeChild(doc2.getDocumentElement());
        doc2.appendChild(doc2.importNode(el2, true));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtil.write(doc2, baos, "UTF-8");
        String xml2actual = baos.toString("UTF-8").replaceAll(System.getProperty("line.separator"), "\n");
        assertEquals("Correct upgrade result", xml2expected, xml2actual);
    }
    
}
