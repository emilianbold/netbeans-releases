/*
 * XMLModelTest.java
 * JUnit based test
 *
 * Created on August 5, 2005, 12:13 PM
 */

package org.netbeans.modules.xml.xdm.diff;
import java.util.List;
import junit.framework.*;
import org.netbeans.modules.xml.xdm.Util;
import org.netbeans.modules.xml.xdm.diff.Change.AttributeChange;
import org.netbeans.modules.xml.xdm.diff.Change.AttributeDiff;
import org.netbeans.modules.xml.xdm.nodes.Attribute;
import org.netbeans.modules.xml.xdm.nodes.Node;

/**
 *
 * @author Ayub Khan
 */
public class XDMUtilTest extends TestCase {
    
    public XDMUtilTest(String testName) {
        super(testName);
    }
    
    public void testPrettyPrintXML() throws Exception {
        XDMUtil util = new XDMUtil();
        String indent = "    ";
        String expected = XDMUtil.XML_PROLOG+"\n"+
                "<test>\n"+
                indent+"<a>\n"+
                indent+indent+"<b/>\n"+
                indent+"</a>\n"+
                "</test>\n";
        String xml = XDMUtil.XML_PROLOG+"<test><a><b/></a></test>";        
        String changed = util.prettyPrintXML(xml, indent);
        assertEquals("pretty print", expected, changed);
    }
    
    public void testPrettyPrintXML2() throws Exception {
        XDMUtil util = new XDMUtil();
        String indent = "    ";
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><SOAP-ENV:Body xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns0=\"http://xml.netbeans.org/schema/SynchronousSample\" xmlns=\"http://xml.netbeans.org/schema/SynchronousSample\"><typeA xmlns=\"http://xml.netbeans.org/schema/SynchronousSample\"><ns0:paramA xmlns:ns0=\"http://xml.netbeans.org/schema/SynchronousSample\">Hello World!</ns0:paramA></typeA></SOAP-ENV:Body>";        
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
                "<SOAP-ENV:Body xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns0=\"http://xml.netbeans.org/schema/SynchronousSample\" xmlns=\"http://xml.netbeans.org/schema/SynchronousSample\">\n"+
                indent+"<typeA xmlns=\"http://xml.netbeans.org/schema/SynchronousSample\">\n"+
                indent+indent+"<ns0:paramA xmlns:ns0=\"http://xml.netbeans.org/schema/SynchronousSample\">Hello World!</ns0:paramA>\n"+
                indent+"</typeA>\n"+
                "</SOAP-ENV:Body>\n";
        String changed = util.prettyPrintXML(xml, indent);
        assertEquals("pretty print", expected, changed);
    }    
    
    public void testPrettyPrintXMLNegative() throws Exception {
        XDMUtil util = new XDMUtil();
        String indent = "    ";
        String expected = XDMUtil.XML_PROLOG;
        String xml = XDMUtil.XML_PROLOG;
        String changed = util.prettyPrintXML(xml, indent);
        assertEquals("pretty print", expected, changed);
    }
    
    public void testCompareXMLEquals() throws Exception {
        String indent = "    ";
        XDMUtil.ComparisonCriteria criteria = XDMUtil.ComparisonCriteria.EQUAL;
        //Only Element and Attribute order change
        String xml1 = XDMUtil.XML_PROLOG+"<test x=\"x1\" y=\"y1\"><a><b1/><b2/></a></test>";
        String xml2 = XDMUtil.XML_PROLOG+"<test y=\"y1\" x=\"x1\"><a><b2/><b1/></a></test>";
        List<Difference> diffs = compareXML(xml1, xml2, criteria);
        assertEquals("pretty print", 0, diffs.size());
        
        //Attribute value change. Element and Attribute order change
        xml1 = XDMUtil.XML_PROLOG+"<test x=\"x1\" y=\"y1\"><a><b1/><b2/></a></test>";
        xml2 = XDMUtil.XML_PROLOG+"<test y=\"y1\" x=\"x2\"><a><b2/><b1/></a></test>";
        diffs = compareXML(xml1, xml2, criteria);
        assertEquals("pretty print size", 1, diffs.size());
        
        //Attribute added. Element and Attribute order change
        xml1 = XDMUtil.XML_PROLOG+"<test x=\"x1\" y=\"y1\"><a><b1/><b2/></a></test>";
        xml2 = XDMUtil.XML_PROLOG+"<test y=\"y1\" x=\"x1\" z=\"z\"><a><b2/><b1/></a></test>";
        diffs = compareXML(xml1, xml2, criteria);
        assertEquals("pretty print size", 1, diffs.size());
        assertTrue("pretty print attribute change", ((Change)diffs.get(0)).isAttributeChanged());
        assertEquals("pretty print attribute change size", 1,
                ((Change)diffs.get(0)).getAttrChanges().size());
    }
    
    public void testCompareXMLIdentical() throws Exception {
        XDMUtil util = new XDMUtil();
        String indent = "    ";
        XDMUtil.ComparisonCriteria criteria = XDMUtil.ComparisonCriteria.IDENTICAL;
        //Only Element and Attribute order change
        String xml1 = XDMUtil.XML_PROLOG+"<test x=\"x1\" y=\"y1\"><a><b1/><b2/></a></test>";
        String xml2 = XDMUtil.XML_PROLOG+"<test y=\"y1\" x=\"x1\"><a><b2/><b1/></a></test>";
        List<Difference> diffs = compareXML(xml1, xml2, criteria);
        assertEquals("pretty print", 2, diffs.size());
        
        //Attribute value change. Element and Attribute order change
        xml1 = XDMUtil.XML_PROLOG+"<test x=\"x1\" y=\"y1\"><a><b1/><b2/></a></test>";
        xml2 = XDMUtil.XML_PROLOG+"<test y=\"y1\" x=\"x2\"><a><b2/><b1/></a></test>";
        diffs = compareXML(xml1, xml2, criteria);
        assertEquals("pretty print size", 2, diffs.size());//1 - Attr pos, value change, 1 - Element pos change
        assertTrue("pretty print attribute change", ((Change)diffs.get(0)).isAttributeChanged());
        assertTrue("pretty print element pos change", ((Change)diffs.get(1)).isPositionChanged());
        
        List<Change.AttributeDiff> attrDiffs = ((Change)diffs.get(0)).getAttrChanges();
        assertEquals("pretty print attribute change size", 2, attrDiffs.size());
        Change.AttributeChange change1 = (Change.AttributeChange) attrDiffs.get(0);
        assertTrue("pretty print attribute pos & token change", change1.isPositionChanged());
        assertTrue("pretty print attribute pos & token change", change1.isTokenChanged());
        Change.AttributeChange change2 = (Change.AttributeChange) attrDiffs.get(1);
        assertTrue("pretty print attribute pos only change", change2.isPositionChanged());
        assertFalse("pretty print attribute no token change", change2.isTokenChanged());
        
        //Attribute added. Element and Attribute order change
        xml1 = XDMUtil.XML_PROLOG+"<test x=\"x1\" y=\"y1\"><a><b1/><b2/></a></test>";
        xml2 = XDMUtil.XML_PROLOG+"<test y=\"y1\" x=\"x2\" z=\"z\"><a><b2/><b1/></a></test>";
        diffs = compareXML(xml1, xml2, criteria);
        assertEquals("pretty print size", 2, diffs.size()); //1 - Attr pos, value change + add, 1 - Element pos change
        assertTrue("pretty print attribute change", ((Change)diffs.get(0)).isAttributeChanged());
        assertTrue("pretty print element pos change", ((Change)diffs.get(1)).isPositionChanged());
        
        attrDiffs = ((Change)diffs.get(0)).getAttrChanges();
        assertEquals("pretty print attribute change size", 3, attrDiffs.size());
        change1 = (Change.AttributeChange) attrDiffs.get(0);
        assertTrue("pretty print attribute pos & token change", change1.isPositionChanged());
        assertTrue("pretty print attribute pos & token change", change1.isTokenChanged());
        change2 = (Change.AttributeChange) attrDiffs.get(1);
        assertTrue("pretty print attribute pos only change", change2.isPositionChanged());
        assertFalse("pretty print attribute no token change", change2.isTokenChanged());
        Change.AttributeAdd add = (Change.AttributeAdd) attrDiffs.get(2);
        assertEquals("pretty print attribute pos only change", 2, add.getNewAttributePosition());
        
        //Attribute added. Element and Attribute order change + Ignore whitespaces
        xml1 = XDMUtil.XML_PROLOG+"<test x=\"x1\" y =\"y1\"><a> <b1/><b2/></a></test>";
        xml2 = XDMUtil.XML_PROLOG+"<test y=\"y1\" x=\" x2\" z=\"z\"> <a><b2/><b1/> </a></test>";
        diffs = compareXML(xml1, xml2, criteria);
        assertEquals("pretty print size", 2, diffs.size()); //1 - Attr pos, value change + add, 1 - Element pos change
        assertTrue("pretty print attribute change", ((Change)diffs.get(0)).isAttributeChanged());
        assertTrue("pretty print element pos change", ((Change)diffs.get(1)).isPositionChanged());
       
        attrDiffs = ((Change)diffs.get(0)).getAttrChanges();
        assertEquals("pretty print attribute change size", 3, attrDiffs.size());
        change1 = (Change.AttributeChange) attrDiffs.get(0);
        assertTrue("pretty print attribute pos & token change", change1.isPositionChanged());
        assertTrue("pretty print attribute pos & token change", change1.isTokenChanged());
        change2 = (Change.AttributeChange) attrDiffs.get(1);
        assertTrue("pretty print attribute pos only change", change2.isPositionChanged());
        assertTrue("pretty print attribute no token change", change2.isTokenChanged());
        add = (Change.AttributeAdd) attrDiffs.get(2);
        assertEquals("pretty print attribute pos only change", 2, add.getNewAttributePosition());
    }
    
    public void testCompareXMLIdentical2() throws Exception {
        XDMUtil util = new XDMUtil();
        XDMUtil.ComparisonCriteria criteria = XDMUtil.ComparisonCriteria.IDENTICAL;
        //Only NS Attribute delete and add
        String xml1 = XDMUtil.XML_PROLOG+"<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/1999/XMLSchema\" xmlns:xsi=\"http://www.w3.org/1999/XMLSchema-instance\" xmlns=\"http://xml.netbeans.org/schema/SynchronousSample\">\n      <SOAP-ENV:Header/>\n      <SOAP-ENV:Body>\n         <typeA>\n            <paramA>Hello World</paramA>\n         </typeA>\n      </SOAP-ENV:Body>\n   </SOAP-ENV:Envelope>";
        String xml2 = XDMUtil.XML_PROLOG+"<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/1999/XMLSchema\" xmlns:xsi=\"http://www.w3.org/1999/XMLSchema-instance\">\n  <SOAP-ENV:Header/>\n  <SOAP-ENV:Body>\n    <typeA xmlns=\"http://xml.netbeans.org/schema/SynchronousSample\">\n      <paramA>Hello World</paramA>\n    </typeA>\n  </SOAP-ENV:Body>\n</SOAP-ENV:Envelope>";
        List<Difference> diffs = compareXML(xml1, xml2, criteria);
        assertEquals("compare identical XML", 0, diffs.size());
    }
    
    /**
     * Test the comparision of defaultnamespace and no defautl but element has namsespace
     */
    public void testComparePrefix() throws Exception {
        String xml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><h:person xmlns:h=\"http://xml.netbeans.org/schema/SynchronousSample\">   <h:name>   <h:first>TT</h:first>   <h:last>LL</h:last>   </h:name></h:person>";
        String xml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><a:person xmlns:a=\"http://xml.netbeans.org/schema/SynchronousSample\">   <a:name>   <a:first>TT</a:first>   <a:last>LL</a:last>   </a:name></a:person>";
        
        XDMUtil util = new XDMUtil();
        XDMUtil.ComparisonCriteria criteria = XDMUtil.ComparisonCriteria.IDENTICAL;
        
        List<Difference> diffs = compareXML(xml1, xml2 , criteria);
        assertEquals("testComparePrefix is equal?", 0, diffs.size());
    }
    
    
    /**
     * Test extra unused namespace
     */
    public void testCompareExtraNamespace() throws Exception {
        String xml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/1999/XMLSchema\" xmlns:xsi=\"http://www.w3.org/1999/XMLSchema-instance\" xmlns=\"http://xml.netbeans.org/schema/SynchronousSample\">\n      <SOAP-ENV:Header/>\n      <SOAP-ENV:Body>\n         <typeA>\n            <paramA>Hello World</paramA>\n         </typeA>\n      </SOAP-ENV:Body>\n   </SOAP-ENV:Envelope>";
        String xml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/1999/XMLSchema\" xmlns:xsi=\"http://www.w3.org/1999/XMLSchema-instance\" xmlns=\"http://xml.netbeans.org/schema/SynchronousSample\" xmlns:extra=\"http://www.w3.org/1999/XMLSchema-instance\">\n      <SOAP-ENV:Header/>\n      <SOAP-ENV:Body>\n         <typeA>\n            <paramA>Hello World</paramA>\n         </typeA>\n      </SOAP-ENV:Body>\n   </SOAP-ENV:Envelope>";
        
        XDMUtil util = new XDMUtil();
        XDMUtil.ComparisonCriteria criteria = XDMUtil.ComparisonCriteria.IDENTICAL;
        
        List<Difference> diffs = compareXML(xml1, xml2 , criteria);
        assertEquals("testCompareExtraNamespace is equal?", 0, diffs.size());
    }
    
    
/* Test both xml has schemaLocation defined and there's extra space in one xml between elements.
 */
    public void testCompareWhitespaceOutofElement_SchemaLoc() throws Exception {
        String xml1 = "<?xml version=\"1.0\"?><email xmlns=\"http://xml.netbeans.org/schema/SynchronousSample\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://xml.netbeans.org/schema/SynchronousSample email.xsd\"><to>DD</to><from>CC</from><note>my note</note></email>";
        String xml2 = "<?xml version=\"1.0\"?><email xmlns=\"http://xml.netbeans.org/schema/SynchronousSample\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://xml.netbeans.org/schema/SynchronousSample email.xsd\"><to>DD</to><from>CC</from>\n    <note>my note</note>  </email>";
        
        XDMUtil util = new XDMUtil();
        XDMUtil.ComparisonCriteria criteria = XDMUtil.ComparisonCriteria.IDENTICAL;
        
        List<Difference> diffs = compareXML(xml1, xml2 , criteria);
        assertEquals("testCompareWhitespaceOutofElement_SchemaLoc?", 0, diffs.size());
    }
    
    
/* Test both xml with NO schemaLocation defined and there's extra space in one xml between elements.
 */
    public void testCompareWhitespaceOutofElement_NoSchemaLoc() throws Exception {
        String xml1 = "<?xml version=\"1.0\"?><email xmlns=\"http://xml.netbeans.org/schema/SynchronousSample\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><to>DD</to><from>CC</from><note>my note</note></email>";
        String xml2 = "<?xml version=\"1.0\"?><email xmlns=\"http://xml.netbeans.org/schema/SynchronousSample\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><to>DD</to><from>CC</from>\n    <note>my note</note>  </email>";
        
        XDMUtil util = new XDMUtil();
        XDMUtil.ComparisonCriteria criteria = XDMUtil.ComparisonCriteria.IDENTICAL;
        
        List<Difference> diffs = compareXML(xml1, xml2 , criteria);
        assertEquals("testCompareWhitespaceOutofElement_NoSchemaLoc?", 0, diffs.size());
    }
    
    /**
     * Test extra whitespace betweeen attributes
     */
    public void testCompareExtraWhiteSpaceBetweenAttr() throws Exception {
        String xml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:syn=\"http://xml.netbeans.org/schema/SynchronousSample\">  <soapenv:Body>    <syn:typeA>      <syn:paramA extra=\"1\">?string?</syn:paramA>    </syn:typeA>  </soapenv:Body></soapenv:Envelope>";
        String xml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:syn=\"http://xml.netbeans.org/schema/SynchronousSample\">  <soapenv:Body>    <syn:typeA>      <syn:paramA  extra=\"1\">?string?</syn:paramA>    </syn:typeA>  </soapenv:Body></soapenv:Envelope>";
        
        XDMUtil util = new XDMUtil();
        XDMUtil.ComparisonCriteria criteria = XDMUtil.ComparisonCriteria.IDENTICAL;
        
        List<Difference> diffs = compareXML(xml1, xml2 , criteria);
        assertEquals("testCompareExtraWhiteSpaceBetweenAttr is equal?", 0, diffs.size());
    }
    
    /**
     * Test xml one has schema location and the other one does not
     */
    public void testCompareXMLWSchemaLocation() throws Exception {
        String xml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><soapenv:Envelope xsi:schemaLocation=\"http://schemas.xmlsoap.org/soap/envelope/ http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:syn=\"http://xml.netbeans.org/schema/SynchronousSample\">  <soapenv:Body>    <syn:typeA>      <syn:paramA>?string?</syn:paramA>    </syn:typeA>  </soapenv:Body></soapenv:Envelope>";
        String xml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:syn=\"http://xml.netbeans.org/schema/SynchronousSample\">  <soapenv:Body>    <syn:typeA>      <syn:paramA>?string?</syn:paramA>    </syn:typeA>  </soapenv:Body></soapenv:Envelope>";
        
        XDMUtil util = new XDMUtil();
        XDMUtil.ComparisonCriteria criteria = XDMUtil.ComparisonCriteria.IDENTICAL;
        
        List<Difference> diffs = compareXML(xml1, xml2 , criteria);
        assertEquals("testCompareXMLWSchemaLocation is equal?", 0, diffs.size());
    }
    
    /**
     * Test the comparision of xml with different url for same prefix
     */
    public void testCompareXMLSamePrefixDifferentURL() throws Exception {
        String xml1="<ns1:test xmlns:ns1=\"xyz\"></ns1:test>";
        String xml2="<ns1:test xmlns:ns1=\"abc\"></ns1:test>";
        
        XDMUtil util = new XDMUtil();
        XDMUtil.ComparisonCriteria criteria = XDMUtil.ComparisonCriteria.IDENTICAL;
        
        List<Difference> diffs = compareXML(xml1, xml2 , criteria);
        assertEquals("testComparePrefix is equal?", 2, diffs.size());
    }
    
    /**
     * Test the comparision of xml with same url for different prefix
     */
    public void testCompareXMLDifferentPrefixSameURL() throws Exception {
        String xml1="<ns1:test xmlns:ns1=\"xyz\"></ns1:test>";
        String xml2="<ns2:test xmlns:ns2=\"xyz\"></ns2:test>";
        
        XDMUtil util = new XDMUtil();
        XDMUtil.ComparisonCriteria criteria = XDMUtil.ComparisonCriteria.IDENTICAL;
        
        List<Difference> diffs = compareXML(xml1, xml2 , criteria);
        assertEquals("testComparePrefix is equal?", 0, diffs.size());
    }
    
    /**
     * Test xml one has schema location and the other one does not
     */
    public void testCompareXMLWithWhitespace() throws Exception {
        String xml1 = "<A><B></B></A>";
        String xml2 = "<A> <B></B></A>";
        
        XDMUtil util = new XDMUtil();
        XDMUtil.ComparisonCriteria criteria = XDMUtil.ComparisonCriteria.IDENTICAL;
        
        List<Difference> diffs = compareXML(xml1, xml2 , criteria);
        assertEquals("testCompareXMLWSchemaLocation is equal?", 0, diffs.size());
    }
    
    /**
     * Test xml one has schema location and the other one does not
     */
    public void testCompareXMLWithTextChange() throws Exception {
        String xml1 = "<A><B></B></A>";
        String xml2 = "<A> XYZ <B></B></A>";
        
        XDMUtil util = new XDMUtil();
        XDMUtil.ComparisonCriteria criteria = XDMUtil.ComparisonCriteria.IDENTICAL;
        
        List<Difference> diffs = compareXML(xml1, xml2 , criteria);
        assertEquals("testCompareXMLWSchemaLocation is equal?", 1, diffs.size());
    }
    
    public void testFilterAttributeOrderChange() throws Exception {
        XDMUtil.ComparisonCriteria criteria = XDMUtil.ComparisonCriteria.IDENTICAL;
        //Only Attribute order change
        String xml1 = XDMUtil.XML_PROLOG+"<test x=\"x1\" y=\"y1\"><a><b1/><b2/></a></test>";
        String xml2 = XDMUtil.XML_PROLOG+"<test y=\"y1\" x=\"x1\"><a><b1/><b2/></a></test>";
        List<Difference> diffs = compareXML(xml1, xml2, criteria);
        assertEquals("attr order & token change", 1, diffs.size());
        assertEquals("attr order & token change", 2, 
                ((Change)diffs.get(0)).getAttrChanges().size());//x and y changed positions
        XDMUtil.filterAttributeOrderChange(diffs);  //<- new filter for ignoring attr changes
        assertEquals("attr order & token change", 0, diffs.size());
        
        //Only Attribute order and token change
        xml1 = XDMUtil.XML_PROLOG+"<test x=\"x1\" y=\"y1\"><a><b1/><b2/></a></test>";
        xml2 = XDMUtil.XML_PROLOG+"<test y=\"y1\" x=\"x2\"><a><b1/><b2/></a></test>";
        diffs = compareXML(xml1, xml2, criteria);
        assertEquals("attr order & token change", 1, diffs.size());
        assertEquals("attr order & token change", 2, 
                ((Change)diffs.get(0)).getAttrChanges().size());
        XDMUtil.filterAttributeOrderChange(diffs);  //<- new filter for ignoring attr changes
        assertEquals("attr order & token change", 1, diffs.size());
        assertEquals("attr token change", 1, 
                ((Change)diffs.get(0)).getAttrChanges().size());
    }
    
    public void testFindOffsets() throws Exception {
        XDMUtil util = new XDMUtil();
        String indent = "    ";
        XDMUtil.ComparisonCriteria criteria = XDMUtil.ComparisonCriteria.IDENTICAL;
        //Only Element and Attribute order change
        String xml1 = XDMUtil.XML_PROLOG+"<test x=\"x1\" y=\"y1\"><a><b1/><b2/></a></test>";
        String xml2 = XDMUtil.XML_PROLOG+"<test y=\"y1\" x=\"x1\"><a><b2/><b1/></a></test>";
        List<Difference> diffs = compareXML(xml1, xml2, criteria);
        assertEquals("pretty print", 2, diffs.size());
        
        Difference d = diffs.get(1);
        assertTrue("diff: ", d instanceof Change);
        NodeInfo oldInfo = d.getOldNodeInfo();
        Node oldNode = oldInfo.getNode();
        NodeInfo info = d.getNewNodeInfo();
        Node node = info.getNode();
        if(oldNode != null)
            assertEquals("old change begin: ", 50, XDMUtil.findPosition(oldNode));
        if(node != null)
            assertEquals("new change begin: ", 45, XDMUtil.findPosition(node));

        d = diffs.get(0);
        assertTrue("diff: ", d instanceof Change);
        oldInfo = d.getOldNodeInfo();
        oldNode = oldInfo.getNode();
        info = d.getNewNodeInfo();
        node = info.getNode();
        if(oldNode != null)
            assertEquals("old change begin: ", 22, XDMUtil.findPosition(oldNode));
        if(node != null)
            assertEquals("new change begin: ", 22, XDMUtil.findPosition(node));            
        if(d instanceof Change) {
            List<AttributeDiff> attrDiffs = ((Change)d).getAttrChanges();
            AttributeDiff ad = attrDiffs.get(0);
            assertTrue("attr diff: ", ad instanceof AttributeChange);
            Attribute oldAttr = ad.getOldAttribute();
            Attribute attr = ad.getNewAttribute();
            if(oldAttr != null)
                assertEquals("old attr change begin: ", 28, XDMUtil.findPosition(oldAttr));
            if(attr != null)
                assertEquals("new attr change begin: ", 35, XDMUtil.findPosition(attr));

            ad = attrDiffs.get(1);
            assertTrue("attr diff: ", ad instanceof AttributeChange);
            oldAttr = ad.getOldAttribute();
            attr = ad.getNewAttribute();
            if(oldAttr != null)
                assertEquals("old attr change begin: ", 35, XDMUtil.findPosition(oldAttr));
            if(attr != null)
                assertEquals("new attr change begin: ", 28, XDMUtil.findPosition(attr));
        }
    }
    
    public void FIXME_testCompareWithDiffInLeafNodes() throws Exception {
        String s1 = Util.getResourceAsString("resources/testdiff1_0.xml");
        String s2 = Util.getResourceAsString("resources/testdiff1_1.xml");
        assertEquals(1, new XDMUtil().compareXML(s1, s1, XDMUtil.ComparisonCriteria.EQUAL).size());
    }
    
    public List<Difference> compareXML(String xml1, String xml2,
            XDMUtil.ComparisonCriteria criteria)
            throws Exception {
        return new XDMUtil().compareXML(xml1, xml2, criteria);
    }    
}
