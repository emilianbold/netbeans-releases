/*
 * XMLSyntaxParserTest.java
 * JUnit based test
 *
 * Created on September 26, 2005, 12:38 PM
 */

package org.netbeans.modules.xml.xdm.nodes;

import junit.framework.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.xdm.visitor.FlushVisitor;
import org.netbeans.modules.xml.xdm.Util;

/**
 *
 * @author Administrator
 */
public class XMLSyntaxParserTest extends TestCase {
    
    public XMLSyntaxParserTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(XMLSyntaxParserTest.class);
        
        return suite;
    }
    
    /**
     * Test of parse method, of class org.netbeans.modules.xmltools.xmlmodel.nodes.XMLSyntaxParser.
     */
    public void testParse() throws Exception {
        System.out.println("testParse");
        BaseDocument basedoc = (BaseDocument)Util.getResourceAsDocument("nodes/test.xml");
        XMLSyntaxParser parser = new XMLSyntaxParser(basedoc);
        Document doc = parser.parse();
        assertNotNull("Document can not be null", doc);
        FlushVisitor fv = new FlushVisitor();
        String docBuf = fv.flushModel(doc);
        assertEquals("The document should be unaltered",basedoc.getText(0,basedoc.getLength()),docBuf);
    }
    
}
