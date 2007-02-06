/*
 * XMLSyntaxParserTest.java
 * JUnit based test
 *
 * Created on September 26, 2005, 12:38 PM
 */

package org.netbeans.modules.xml.xdm.nodes;

import java.util.List;
import junit.framework.*;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.xdm.visitor.FlushVisitor;
import org.netbeans.modules.xml.xdm.Util;
import org.w3c.dom.NodeList;

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
        BaseDocument basedoc = (BaseDocument)Util.getResourceAsDocument("nodes/test.xml");
        XMLSyntaxParser parser = new XMLSyntaxParser();
        Document doc = parser.parse(basedoc);
        assertNotNull("Document can not be null", doc);
        FlushVisitor fv = new FlushVisitor();
        String docBuf = fv.flushModel(doc);
        assertEquals("The document should be unaltered",basedoc.getText(0,basedoc.getLength()),docBuf);
    }
	
    /**
     * Test of parse method, of class org.netbeans.modules.xmltools.xmlmodel.nodes.XMLSyntaxParser.
     */
    public void testParseInvalid() throws Exception {
        BaseDocument basedoc = (BaseDocument)Util.getResourceAsDocument("nodes/invalid.xml");
        XMLSyntaxParser parser = new XMLSyntaxParser();
        try {
            Document doc = parser.parse(basedoc);
            assertTrue("Should not come here", false);
        } catch(Exception ex) {
            assertTrue("Invalid Token exception" ,
                    ex.getMessage().contains("Invalid token") && ex.getMessage().contains("sss"));
        }
    }	
    
    /**
     * Test of parse method, of class org.netbeans.modules.xmltools.xmlmodel.nodes.XMLSyntaxParser.
     */
    public void testParseInvalidTag() throws Exception {
        BaseDocument basedoc = (BaseDocument)Util.getResourceAsDocument("nodes/invalidtag.xml");
        XMLSyntaxParser parser = new XMLSyntaxParser();
        try {
            Document doc = parser.parse(basedoc);
            assertTrue("Should not come here", false);
        } catch(Exception ex) {
            assertTrue("Invalid Token exception" ,
                    ex.getMessage().contains("Invalid token") && ex.getMessage().contains("sss"));
        }
    }
    
    /**
     * Test of parse method, of class org.netbeans.modules.xmltools.xmlmodel.nodes.XMLSyntaxParser.
     */
    public void testParseInvalidTag2() throws Exception {
        BaseDocument basedoc = (BaseDocument)Util.getResourceAsDocument("nodes/invalidtag2.xml");
        XMLSyntaxParser parser = new XMLSyntaxParser();
        try {
            Document doc = parser.parse(basedoc);
            assertTrue("Should not come here", false);
        } catch(Exception ex) {
            assertTrue("Invalid Token exception" ,
                    ex.getMessage().contains("Invalid token '</a' does not end with '>'"));
        }
    }   
    
    /**
     * Test of parse method, of class org.netbeans.modules.xmltools.xmlmodel.nodes.XMLSyntaxParser.
     */
    public void testParseInvalidTag3() throws Exception {
        BaseDocument basedoc = (BaseDocument)Util.getResourceAsDocument("nodes/invalidtag3.xml");
        XMLSyntaxParser parser = new XMLSyntaxParser();
        try {
            Document doc = parser.parse(basedoc);
            assertTrue("Should not come here", false);
        } catch(Exception ex) {
            assertTrue("Invalid Token exception" ,
                    ex.getMessage().contains("Invalid token '<' found in document"));
        }
    }
    
    /**
     * Test of parse method, of class org.netbeans.modules.xmltools.xmlmodel.nodes.XMLSyntaxParser.
     */
    public void testParseInvalidTag4() throws Exception {
        BaseDocument basedoc = (BaseDocument)Util.getResourceAsDocument("nodes/invalidtag4.xml");
        XMLSyntaxParser parser = new XMLSyntaxParser();
        try {
            Document doc = parser.parse(basedoc);
            assertTrue("Should not come here", false);
        } catch(Exception ex) {
            assertTrue("Invalid Token exception" ,
                    ex.getMessage().contains("Invalid token '</b' does not end with '>'"));
        }
    }    
    
    /**
     * Test of parse method, of class org.netbeans.modules.xmltools.xmlmodel.nodes.XMLSyntaxParser.
     */
    public void testParseValidTag() throws Exception {
        BaseDocument basedoc = (BaseDocument)Util.getResourceAsDocument("nodes/validtag.xml");
        XMLSyntaxParser parser = new XMLSyntaxParser();
        try {
            Document doc = parser.parse(basedoc);            
        } catch(Exception ex) {
            assertTrue("Should not come here", false);
        }
    }    

    public void testParsePI() throws Exception {
        BaseDocument basedoc = (BaseDocument)Util.getResourceAsDocument("resources/PI_after_prolog.xml");
        XMLSyntaxParser parser = new XMLSyntaxParser();

        Document doc = parser.parse(basedoc);            
        List<Token> tokens = doc.getTokens();
        assertEquals(12, tokens.size());
        assertEquals(TokenType.TOKEN_PI_START_TAG, tokens.get(0).getType());
        assertEquals(TokenType.TOKEN_PI_END_TAG, tokens.get(4).getType());
        assertEquals(TokenType.TOKEN_PI_START_TAG, tokens.get(6).getType());
        assertEquals(TokenType.TOKEN_PI_NAME, tokens.get(7).getType());
        assertEquals("Siebel-Property-Set", tokens.get(7).getValue());
        assertEquals(TokenType.TOKEN_PI_VAL, tokens.get(9).getType());
        NodeList nl = doc.getChildNodes();
        assertEquals(2, nl.getLength());    
    }    

    /**
     * Test of parse method, of class org.netbeans.modules.xmltools.xmlmodel.nodes.XMLSyntaxParser.
     * Test the parsing of doctype
     */
    public void testParseDoctype() throws Exception {
        BaseDocument basedoc = (BaseDocument)Util.getResourceAsDocument("nodes/testDoctype.xml");
        XMLSyntaxParser parser = new XMLSyntaxParser();
        Document doc = parser.parse(basedoc);
        assertNotNull("Document can not be null", doc);
        FlushVisitor fv = new FlushVisitor();
        String docBuf = fv.flushModel(doc);
        assertEquals("The document should be unaltered",basedoc.getText(0,basedoc.getLength()),docBuf);
    }
	
}
