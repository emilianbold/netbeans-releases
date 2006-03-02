/*
 * FindVisitorTest.java
 * JUnit based test
 *
 * Created on October 14, 2005, 1:39 PM
 */

package org.netbeans.modules.xml.xdm.visitor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import junit.framework.*;
import org.netbeans.modules.xml.xdm.XDMModel;
import org.netbeans.modules.xml.xdm.nodes.*;
import org.netbeans.modules.xml.xdm.Util;

/**
 *
 * @author ajit
 */
public class FindVisitorTest extends TestCase {
    
    public FindVisitorTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(FindVisitorTest.class);
        
        return suite;
    }
    
    public void testFind() {
        System.out.println("find");
        
        FindVisitor instance = new FindVisitor();

        Document root = xmlModel.getDocument();

        // try to find company
        Element company = (Element)root.getChildNodes().item(0);
        Node result = instance.find(root, company.getId());
        assertEquals(company, result);

        // try to find attribute
        Element employee = (Element)company.getChildNodes().item(1);
        Attribute attr = (Attribute)employee.getAttributes().item(0);
        result = instance.find(root, attr.getId());
        assertEquals(attr, result);

        // try to find text
        Text txt = (Text)employee.getChildNodes().item(0);
        result = instance.find(root, txt.getId());
        assertEquals(txt, result);
    }
    
    protected void setUp() throws Exception {
        xmlModel = Util.loadXDMModel("visitor/test.xml");
        xmlModel.sync();
    }
    
    private XDMModel xmlModel;
}
