/*
 * PositionFinderVisitorTest.java
 *
 * Created on October 26, 2005, 8:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.xdm.visitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.modules.xml.xdm.Util;
import org.netbeans.modules.xml.xdm.XDMModel;
import org.netbeans.modules.xml.xdm.nodes.Attribute;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.netbeans.modules.xml.xdm.nodes.Text;
import org.w3c.dom.NodeList;

/**
 *
 * @author rico
 */
public class PositionFinderVisitorTest extends TestCase{
    
    /** Creates a new instance of PositionFinderVisitorTest */
    public PositionFinderVisitorTest() {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(PositionFinderVisitorTest.class);
        
        return suite;
    }
 
    public void testFindPosition(){
        FindVisitor instance = new FindVisitor();
        
        Document root = xmlModel.getDocument();
        
        PositionFinderVisitor pfVisitor = new PositionFinderVisitor();
        
        //element company
        Element company = (Element)root.getChildNodes().item(0);
        Node result = instance.find(root, company.getId());
        assertEquals(company, result);
        
        int position = pfVisitor.findPosition(root, company);
        this.assertEquals("Position of root element",25, position);
        System.out.println("position of company element: " + position);
        
        //newline char
        Text t = (Text)company.getChildNodes().item(0);
        position = pfVisitor.findPosition(root, t);
        this.assertEquals("Position of newline ", 83, position);
        System.out.println("position of newline text: " + position);
        
        //employee element
        Element employee = (Element)company.getChildNodes().item(1);
        position = pfVisitor.findPosition(root, employee);
        assertEquals("Position of employee element",89, position);
        System.out.println("position of employee element: " + position);
        
        //ssn attribute
        Attribute attr = (Attribute)employee.getAttributes().item(0);
        position = pfVisitor.findPosition(root, attr);
        System.out.println("position of ssn attribute: " + position);
        assertEquals("Position of ssn attribute",99, position);
        
        //id attribute
        attr = (Attribute)employee.getAttributes().item(1);
        position = pfVisitor.findPosition(root, attr);
        assertEquals("Position of id attribute",119, position);
        System.out.println("position of id attribute: " + position);
        
        //address attribute
        attr = (Attribute)employee.getAttributes().item(2);
        position = pfVisitor.findPosition(root, attr);
        assertEquals("Position of address attribute",136, position);
        System.out.println("position of address attribute: " + position);
        
        //phone attribute
        attr = (Attribute)employee.getAttributes().item(3);
        position = pfVisitor.findPosition(root, attr);
        assertEquals("Position of phone attribute with embedded whitespaces",172, position);
        System.out.println("position of phone attribute: " + position);
        
        //text value child node of employee
        Text txt = (Text)employee.getChildNodes().item(0);
        position = pfVisitor.findPosition(root, txt);
        assertEquals("Position of text child node",195, position);
        System.out.println("position of child value text: " + position);
        
        //comment
        txt = (Text)company.getChildNodes().item(3);
        position = pfVisitor.findPosition(root, txt);
        assertEquals("Position of comment (after newline)",238, position);
        System.out.println("position of comment text: " + position);
        
        //second employee element
        Element employee2 = (Element)company.getChildNodes().item(5);
        position = pfVisitor.findPosition(root, employee2);
        assertEquals("Position of second employee element",259, position);
        System.out.println("position of employee2 element: " + position);
    }
	
    public void testFindPosition2() throws IOException, Exception {
		
        xmlModel = Util.loadXDMModel("visitor/testPosition.xsd");
        xmlModel.sync();	
		
        FindVisitor instance = new FindVisitor();
        
        Document root = xmlModel.getDocument();
        
        PositionFinderVisitor pfVisitor = new PositionFinderVisitor();
        
        //element schema
        Element schema = (Element)root.getChildNodes().item(8);
        Node result = instance.find(root, schema.getId());
        assertEquals(schema, result);
        
        int position = pfVisitor.findPosition(root, schema);
        this.assertEquals("Position of schema element",215, position);
        System.out.println("position of company element: " + position);
        
        //global element
        Element ge = (Element)schema.getChildNodes().item(3);
        position = pfVisitor.findPosition(root, ge);
        assertEquals("Position of employee element",564, position);
        System.out.println("position of employee element: " + position);
    }	
    
    protected void setUp() throws Exception {
        xmlModel = Util.loadXDMModel("visitor/testPosition.xml");
        xmlModel.sync();
    }
    
    private void dumpTextNodes(Node node){
        NodeList nodes = node.getChildNodes();
        System.out.println("number of children: " + nodes.getLength());
        int counter = 0;
        for(int i = 0; i < nodes.getLength(); i++){
            Node n = (Node)nodes.item(i);
            System.out.println("child " + ++counter + ": "+ n.getClass().getName());
            if(n instanceof Text){
                Text t = ((Text)n);
                System.out.println("text length: " + t.getText().length());
                System.out.println("text:" + t.getText() +"++");
                
            }
        }
    }
    
    private XDMModel xmlModel;
    
}
