/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.xdm.visitor;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.modules.xml.xdm.Util;
import org.netbeans.modules.xml.xdm.XDMModel;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.netbeans.modules.xml.xdm.nodes.Text;
import org.w3c.dom.NodeList;

/**
 *
 * @author Ayub Khan
 */
public class NodeByPositionVisitorTest extends TestCase{
    
    /** Creates a new instance of NodeByPositionVisitorTest */
    public NodeByPositionVisitorTest() {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(NodeByPositionVisitorTest.class);
        
        return suite;
    }
 
    public void testFindPosition(){
        FindVisitor instance = new FindVisitor();
        
        Document root = xmlModel.getDocument();
        
        NodeByPositionVisitor pfVisitor = new NodeByPositionVisitor(root);
        
        //element company
        Element company = (Element)root.getChildNodes().item(0);
        Node result = instance.find(root, company.getId());
        assertEquals(company, result);
        
		//start-tag:25-82, end-tag:513-522
        Node findCompany = pfVisitor.getContainingElement(83);
        this.assertEquals("Found company by position",company, findCompany);
		
		//start-tag:89-194, end-tag:219-228
		Element firstEmployee = (Element) company.getChildNodes().item(1);
		Node empNameText = pfVisitor.getContainingNode(213);//Vidhya Narayanan
		this.assertEquals("Found first employee by position",
				firstEmployee.getChildNodes().item(0), 
				empNameText);
		//Demonstrates that we are returning the containing element of text node "Vidhya Narayanan"
        Node findFirstEmployee = pfVisitor.getContainingElement(213);//Vidhya Narayanan
        this.assertEquals("Found first employee by position",firstEmployee, 
				findFirstEmployee);
		
		//start-tag:89-194, end-tag:219-228
		Node phoneNumber = pfVisitor.getContainingNode(193);
		this.assertEquals("Found first employee by position",
				firstEmployee.getAttributes().item(3), 
				phoneNumber);
		//Demonstrates that we are returning the containing element of attribute phone		
        Node findFirstEmployeeAgain = pfVisitor.getContainingElement(193);
        this.assertEquals("Found first employee by another position again",
				findFirstEmployee, findFirstEmployeeAgain);
		
		//start-tag:259-318, end-tag:327-337
		Element secondEmployee = (Element) company.getChildNodes().item(5);
		empNameText = pfVisitor.getContainingNode(326);//A Person
		this.assertEquals("Found first employee by position",
				secondEmployee.getChildNodes().item(0), 
				empNameText);
		//Demonstrates that we are returning the containing element of text node "Vidhya Narayanan"		
        Node findSecondEmployee = pfVisitor.getContainingElement(326);
        this.assertEquals("Found second employee by position",
				secondEmployee, findSecondEmployee);
		
		//start-tag:259-318, end-tag:327-337
        Node findSecondEmployeeAgain = pfVisitor.getContainingElement(337);
        this.assertEquals("Found second employee by another position again",
				findSecondEmployee, findSecondEmployeeAgain);
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
