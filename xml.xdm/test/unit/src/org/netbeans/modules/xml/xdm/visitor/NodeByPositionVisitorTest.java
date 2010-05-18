/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * 
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
