
package org.netbeans.modules.junit;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import org.openide.*;
import org.openide.TopManager;
import org.openide.filesystems.*;
import org.openide.explorer.propertysheet.*;
import org.openide.util.NbBundle;
import junit.framework.*;

public class JUnitCfgOfCreateTest extends TestCase {
    
    public JUnitCfgOfCreateTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(JUnitCfgOfCreateTest.class);
        
        return suite;
    }
    
    /** Test of configure method, of class org.netbeans.modules.junit.JUnitCfgOfCreate. */
    public void testConfigure() {
        System.out.println("testConfigure");
        fail("GUI dependent test.");
    }
    
    /** Test of setNewFileSystem method, of class org.netbeans.modules.junit.JUnitCfgOfCreate. */
    public void testSetNewFileSystem() {
        System.out.println("testSetNewFileSystem");
        // tested in configure test
    }
    
    /** Test of getNewFileSystem method, of class org.netbeans.modules.junit.JUnitCfgOfCreate. */
    public void testGetNewFileSystem() {
        System.out.println("testGetNewFileSystem");
        // tested in configure test
    }
    
}
