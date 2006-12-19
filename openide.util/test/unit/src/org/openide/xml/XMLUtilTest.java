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

package org.openide.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharConversionException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.net.URLClassLoader;
import javax.xml.parsers.DocumentBuilderFactory;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

public class XMLUtilTest extends NbTestCase {
    
    public XMLUtilTest(String testName) {
        super(testName);
    }
    
    public void testCreateXMLReader() {
        
        XMLReader parser = null;
        
        try {
            parser = XMLUtil.createXMLReader();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
                
        // Add your test code below by replacing the default call to fail.
        if (parser == null) fail("Cannot create XML reader");
    }
    
    public void testCreateDocument() {
       
        Document doc = null;
        try {
            doc = XMLUtil.createDocument("root", null, null, null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        // Add your test code below by replacing the default call to fail.
        if (doc == null) fail("The test case is empty.");
    }
    
    public void testWrite() throws Exception {
        String data = "<foo bar=\"val\"><baz/></foo>";
        Document doc = XMLUtil.parse(new InputSource(new StringReader(data)), false, true, null, null);
        //System.err.println("XMLUtil.parse impl class: " + doc.getClass().getName());
        Element el = doc.getDocumentElement();
        assertEquals("foo", el.getNodeName());
        assertEquals("val", el.getAttribute("bar"));
        NodeList l = el.getElementsByTagName("*");
        assertEquals(1, l.getLength());
        Element el2 = (Element)l.item(0);
        assertEquals("baz", el2.getLocalName());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtil.write(doc, baos, "UTF-8");
        String data2 = baos.toString("UTF-8");
        //System.err.println("testWrite: data2:\n" + data2);
        assertTrue(data2, data2.indexOf("foo") != -1);
        assertTrue(data2, data2.indexOf("bar") != -1);
        assertTrue(data2, data2.indexOf("baz") != -1);
        assertTrue(data2, data2.indexOf("val") != -1);
    }
    
    /** Test that read/write DOCTYPE works too. */
    public void testDocType() throws Exception {
        String data = "<!DOCTYPE foo PUBLIC \"The foo DTD\" \"http://nowhere.net/foo.dtd\"><foo><x/><x/></foo>";
        Document doc = XMLUtil.parse(new InputSource(new StringReader(data)), true, true, new Handler(), new Resolver());
        DocumentType t = doc.getDoctype();
        assertNotNull(t);
        assertEquals("foo", t.getName());
        assertEquals("The foo DTD", t.getPublicId());
        assertEquals("http://nowhere.net/foo.dtd", t.getSystemId());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtil.write(doc, baos, "UTF-8");
        String data2 = baos.toString("UTF-8");
        //System.err.println("data2:\n" + data2);
        assertTrue(data2, data2.indexOf("foo") != -1);
        assertTrue(data2, data2.indexOf("x") != -1);
        assertTrue(data2, data2.indexOf("DOCTYPE") != -1);
        assertTrue(data2, data2.indexOf("The foo DTD") != -1);
        assertTrue(data2, data2.indexOf("http://nowhere.net/foo.dtd") != -1);
    }
    private static final class Handler implements ErrorHandler {
        public void error(SAXParseException exception) throws SAXException {
            throw exception;
        }
        public void fatalError(SAXParseException exception) throws SAXException {
            throw exception;
        }
        public void warning(SAXParseException exception) throws SAXException {
            throw exception;
        }
    }
    private static final class Resolver implements EntityResolver {
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            assertEquals("The foo DTD", publicId);
            assertEquals("http://nowhere.net/foo.dtd", systemId);
            String data = "<!ELEMENT foo (x+)><!ELEMENT x EMPTY>";
            return new InputSource(new StringReader(data));
        }
    }
    
    public void testToAttributeValue() throws IOException {
        String result = null;
        try {
            result = XMLUtil.toAttributeValue("\t\r\n &'<\"");
        } catch (CharConversionException ex) {            
        }
        
        assertEquals("Basic escape test failed", "\t\r\n &amp;&apos;&lt;&quot;", result);
        
        try {
            XMLUtil.toAttributeValue(new String(new byte[] { 0 }));
            fail("Forbidden character accepted.");
        } catch (CharConversionException ex) {            
        }

        try {
            XMLUtil.toAttributeValue(new String(new byte[] { 31 }));
            fail("Forbidden character accepted.");
        } catch (CharConversionException ex) {            
        }        
    }
    
    public void testElementToContent() {
        String result = null;
        
        try {
            result = XMLUtil.toElementContent("]]>\t\r\n &<>");
        } catch (CharConversionException ex) {
        }
        
        assertEquals("Basic escape test failed", "]]&gt;\t\r\n &amp;&lt;>", result);
        
        try {
            XMLUtil.toElementContent(new String(new byte[] { 0 }));
            fail("Forbidden character accepted.");
        } catch (CharConversionException ex) {            
        }

        try {
            XMLUtil.toElementContent(new String(new byte[] { 31 }));
            fail("Forbidden character accepted.");
        } catch (CharConversionException ex) {            
        }        
                
    }
    
    public void testToHex() {
        
        byte[] data = new byte[] {0, 1, 15, 16, (byte)255};
        String s = XMLUtil.toHex(data, 0, data.length);
        
        // Add your test code below by replacing the default call to fail.
        if (s.equalsIgnoreCase("00010f10ff") == false) {
            fail("toHex() =" + s);
        }
    }
    
    public void testFromHex() {
        
        char[] hex = "00010f10ff".toCharArray();
        try {
            byte[] ret = XMLUtil.fromHex(hex, 0, hex.length);
            if (ret[0] != 0 || ret[1] != 1 || ret[2] != 15 || ret[3] != 16 || ret[4] != (byte)255) {
                fail("fromHex()");
            }
        } catch (IOException ex) {
            fail(ex.getMessage());
        }
                
    }
    
    /**
     * Check that reading and writing namespaces works.
     * @see "#36294"
     */
    public void testNamespaces() throws Exception {
        String data = "<foo xmlns='bar'><baz/></foo>";
        Document doc = XMLUtil.parse(new InputSource(new StringReader(data)), false, true, null, null);
        //System.err.println("XMLUtil.parse impl class: " + doc.getClass().getName());
        Element el = doc.getDocumentElement();
        assertEquals("foo", el.getNodeName());
        assertEquals("foo", el.getTagName());
        assertEquals("foo", el.getLocalName());
        assertEquals("bar", el.getNamespaceURI());
        NodeList l = el.getElementsByTagName("*");
        assertEquals(1, l.getLength());
        Element el2 = (Element)l.item(0);
        assertEquals("baz", el2.getLocalName());
        assertEquals("bar", el2.getNamespaceURI());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtil.write(doc, baos, "UTF-8");
        String data2 = baos.toString("UTF-8");
        //System.err.println("testNamespaces: data2:\n" + data2);
        assertTrue(data2, data2.indexOf("foo") != -1);
        assertTrue(data2, data2.indexOf("bar") != -1);
        doc = XMLUtil.parse(new InputSource(new ByteArrayInputStream(baos.toByteArray())), false, true, null, null);
        el = doc.getDocumentElement();
        assertEquals("foo", el.getLocalName());
        assertEquals("bar", el.getNamespaceURI());
        l = el.getElementsByTagName("*");
        assertEquals(1, l.getLength());
        el2 = (Element)l.item(0);
        assertEquals("baz", el2.getLocalName());
        assertEquals("bar", el2.getNamespaceURI());
        doc = XMLUtil.createDocument("foo2", "bar2", null, null);
        //System.err.println("XMLUtil.createDocument impl class: " + doc.getClass().getName());
        doc.getDocumentElement().appendChild(doc.createElementNS("bar2", "baz2"));
        baos = new ByteArrayOutputStream();
        XMLUtil.write(doc, baos, "UTF-8");
        data2 = baos.toString("UTF-8");
        assertTrue(data2, data2.indexOf("foo2") != -1);
        assertTrue("namespace 'bar2' of root element mentioned in output: " + data2, data2.indexOf("bar2") != -1);
        doc = XMLUtil.parse(new InputSource(new ByteArrayInputStream(baos.toByteArray())), false, true, null, null);
        el = doc.getDocumentElement();
        assertEquals("foo2", el.getLocalName());
        assertEquals("bar2", el.getNamespaceURI());
        l = el.getElementsByTagName("*");
        assertEquals(1, l.getLength());
        el2 = (Element)l.item(0);
        assertEquals("baz2", el2.getLocalName());
        assertEquals("bar2", el2.getNamespaceURI());
    }
    
    /**
     * Check more namespace stuff, since JAXP has a lot of bugs...
     * @see "#6308026"
     */
    public void testNamespaces2() throws Exception {
        String data = "<root xmlns='root'/>";
        Document doc = XMLUtil.parse(new InputSource(new StringReader(data)), false, true, null, null);
        doc.getDocumentElement().appendChild(doc.createElementNS("child", "child"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtil.write(doc, baos, "UTF-8");
        //System.err.println("testNamespaces2:\n" + baos);
        doc = XMLUtil.parse(new InputSource(new ByteArrayInputStream(baos.toByteArray())), false, true, null, null);
        Element el = doc.getDocumentElement();
        assertEquals("root", el.getLocalName());
        assertEquals("root", el.getNamespaceURI());
        NodeList l = el.getElementsByTagName("*");
        assertEquals(1, l.getLength());
        el = (Element) l.item(0);
        assertEquals("child", el.getLocalName());
        assertEquals("Correct namespaces in " + baos.toString(), "child", el.getNamespaceURI());
    }
    
    public void testIndentation() throws Exception {
        Document doc = XMLUtil.createDocument("root", null, null, null);
        doc.getDocumentElement().appendChild(doc.createElement("child"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtil.write(doc, baos, "UTF-8");
        String data = baos.toString()/*#62680*/.replaceAll("\r\n", "\n");
        assertTrue("had reasonable indentation in\n" + data, data.indexOf("<root>\n    <child/>\n</root>\n") != -1);
    }
    
    /** cf. #62006 */
    public void testIndentation2() throws Exception {
        // XXX currently it seems that the JDK 5/6 serializer adds an extra \n after DOCTYPE, for no apparent reason!
        // While the Mantis serializer inserts a useless line break in the middle...
        // so we don't check formatting on that part.
        // Also serializers may arbitrarily reorder the doctype, so don't even look at it (just make sure it is there).
        String doctype = "<!DOCTYPE p PUBLIC \"random DTD\" \"" + XMLUtilTest.class.getResource("random.dtd") + "\">\n";
        String data =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                doctype +
                "<!--\n" +
                "Some license or whatever.\n" +
                "-->\n" +
                "<?stylesheet location=\"here\"?>\n" +
                "<p>\n" +
                "    <t/>\n" +
                "    <c>\n" +
                "        <d>\n" +
                "            <s/>\n" +
                "        </d>\n" +
                "    </c>\n" +
                "</p>\n";
        Document doc = XMLUtil.parse(new InputSource(new StringReader(data)), false, false, null, null);
        Element d = (Element) doc.getElementsByTagName("d").item(0);
        Element c = (Element) d.getParentNode();
        Element d2 = (Element) DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().importNode(d, true);
        c.removeChild(d);
        c.appendChild(doc.importNode(d2, true));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtil.write(doc, baos, "UTF-8");
        String data2 = baos.toString().replaceAll("\r\n", "\n");
        //System.err.println("normalized data:\n" + ignoreSpaceChanges(data, doctype) + "\nnormalized data2:\n" + ignoreSpaceChanges(data2, doctype));
        assertEquals("identity replacement should not mess up indentation in \n" + data2, ignoreSpaceChanges(data, doctype), ignoreSpaceChanges(data2, doctype));
    }
    private static String ignoreSpaceChanges(String text, String fuzzy) {
        // Yes this is confusing!
        // Inner regexp:
        // Input: <!DOCTYPE p PUBLIC ...>\n
        // Output: \Q<!DOCTYPE\E\s+\Qp\E\s+\QPUBLIC...>\E\s+\Q\E
        // Outer regexp:
        // Input: stuff\n<!DOCTYPE p\nPUBLIC ...>\n\nmore stuff
        // Output: stuff\n<!DOCTYPE p PUBLIC ...>\nmore stuff
        String regexp = "\\Q" + fuzzy.replaceAll("\\s+", "\\\\E\\\\s+\\\\Q") + "\\E";
        //System.err.println("regexp='" + regexp + "' text='" + text + "' fuzzy='" + fuzzy + "' result='" + text.replaceFirst(regexp, "") + "'");
        return text.replaceFirst(regexp, "");
    }
    
    public void testSignificantWhitespace() throws Exception {
        String data =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<r>\n" +
                "    <p>This is <em>not</em> a test!</p>\n" +
                "</r>\n";
        Document doc = XMLUtil.parse(new InputSource(new StringReader(data)), false, false, null, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtil.write(doc, baos, "UTF-8");
        String data2 = baos.toString().replaceAll("\r\n", "\n");
        assertEquals("identity replacement should not mess up significant whitespace", data, data2);
    }
}
