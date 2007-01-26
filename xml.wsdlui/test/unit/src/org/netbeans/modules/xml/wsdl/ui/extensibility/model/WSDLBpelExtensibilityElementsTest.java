/*
 * WSDLBpelExtensibilityElementsTest.java
 *
 * Created on January 23, 2007, 4:07 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.extensibility.model;

import junit.framework.*;
import org.netbeans.modules.xml.wsdl.ui.TestLookup;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Lookup;

/**
 *
 * @author radval
 */
public class WSDLBpelExtensibilityElementsTest extends TestCase {
    
    static {
        try {
           System.setProperty("org.openide.util.Lookup", TestLookup.class.getName());
           Lookup l = Lookup.getDefault();
           if(l instanceof TestLookup) {
               XMLFileSystem x = new XMLFileSystem(WSDLBpelExtensibilityElementsTest.class.getResource("/org/netbeans/modules/wsdlextensions/jms/resources/layer.xml"));
              
               ((TestLookup) l).setup(x);
           }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    
    /** Creates a new instance of WSDLBpelExtensibilityElementsTest */
    public WSDLBpelExtensibilityElementsTest() {
    }
    
    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }
    
    public void testMe() {
        
    }
}
