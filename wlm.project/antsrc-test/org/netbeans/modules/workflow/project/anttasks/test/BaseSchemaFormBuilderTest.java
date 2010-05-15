package org.netbeans.modules.workflow.project.anttasks.test;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.chiba.tools.schemabuilder.SchemaFormBuilder;
import org.netbeans.modules.wlm.project.anttasks.BaseSchemaFormBuilder;
import org.netbeans.modules.wlm.project.anttasks.XmlUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


public class BaseSchemaFormBuilderTest extends TestCase {

    /**
     * Test  build xform from purchaseOrder2.xsd and transform the xform to
     * xhtml
     * @throws Exception
     */
    public void testBuildForm_PurchaseOrder2() throws Exception {
         SchemaFormBuilder builder1;
         SchemaFormBuilder builder2;
        
        URL url = getClass().getResource("ComplaintsBpelToWLM.xsd");
//         URL url = getClass().getResource("purchaseOrder2.xsd");

        // builder = new BaseSchemaFormBuilder("purchaseOrder");
        Map defaultValue = new HashMap();
        // defaultValue.put ("po:orderId", "instance('input')/po:orderId");
        // defaultValue.put ("po:approveDate", "local-date()");

        org.xml.sax.XMLReader reader = makeXMLReader();
        reader.setContentHandler(new Sink(defaultValue));
        
//        reader.parse(new org.xml.sax.InputSource(getClass().getResourceAsStream("mappings.xml")));
        
//        defaultValue = new HashMap();

       builder1 = new BaseSchemaFormBuilder("resolution", true, defaultValue);
        
//        builder1 = new BaseSchemaFormBuilder("purchaseOrder", true, defaultValue);
      
        
        
        builder2 = new BaseSchemaFormBuilder("complaintInput");
//        builder2 = new BaseSchemaFormBuilder("orderReply");

        
        Document form1 = builder1.buildForm(url.getPath());
        Document form2 = builder2.buildForm(url.getPath());
        
        String xmlString = XmlUtil.toXml(form1, "UTF-8", true);
        System.out.println(xmlString);
        
        
        xmlString = XmlUtil.toXml(form2, "UTF-8", true);
        System.out.println(xmlString);
        
        url = getClass().getResource("inputtransform.xsl");
        File inputXslt = new File (url.getPath());
        url = getClass().getResource("outputtransform.xsl");
        File outputXslt = new File (url.getPath());
       
        
        Node outtransformed = XmlUtil.transformToDoc(new DOMSource(form1), new StreamSource(outputXslt));
        Node intransformed = XmlUtil.transformToDoc(new DOMSource(form2), new StreamSource(inputXslt));

        Element root = ((Document)intransformed).createElement("body");
        root.appendChild(((Document)intransformed).getDocumentElement());
        root.appendChild(((Document)intransformed).importNode(((Document)outtransformed).getDocumentElement(), true));
 

         xmlString = XmlUtil.toXml(root, "UTF-8", true);
        
        

        System.out.println(xmlString);
    }


    /**
     * __UNDOCUMENTED__
     * 
     * @throws Exception
     *             __UNDOCUMENTED__
     */
    protected void setUp() throws Exception {
    }

    /**
     * __UNDOCUMENTED__
     */
    protected void tearDown() {
    }

    final public static org.xml.sax.XMLReader makeXMLReader() throws Exception {
        final javax.xml.parsers.SAXParserFactory saxParserFactory = javax.xml.parsers.SAXParserFactory
                .newInstance();
        final javax.xml.parsers.SAXParser saxParser = saxParserFactory.newSAXParser();
        final org.xml.sax.XMLReader parser = saxParser.getXMLReader();
        return parser;
    }

    final class Sink extends org.xml.sax.helpers.DefaultHandler implements
            org.xml.sax.ContentHandler {
        private Map mMap = null;

        public Sink(Map map) {
            super();
            mMap = map;
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {
            // TODO Auto-generated method stub
            if (qName.equals("mp:mapping")) {
                String path = "";
                String value = "";
                for (int i = 0; i < attributes.getLength(); i++) {
                    String attName = attributes.getQName(i);
                    if (attName.equals("path")) {
                        path = attributes.getValue(i);
                    } else if (attName.equals("value")) {
                        value = attributes.getValue(i);
                    }
                }
                mMap.put(path, value);
            }
            super.startElement(uri, localName, qName, attributes);
        }

    }

}

// end of class
