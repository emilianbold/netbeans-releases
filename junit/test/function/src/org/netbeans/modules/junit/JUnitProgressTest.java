
package org.netbeans.modules.junit;

import java.awt.*;
import java.awt.event.*;
import javax.swing.SwingUtilities;
import org.openide.*;
import org.openide.TopManager;
import org.openide.util.NbBundle;
import junit.framework.*;

public class JUnitProgressTest extends TestCase {
    
    public JUnitProgressTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(JUnitProgressTest.class);
        
        return suite;
    }
    
    /** Test of showMe method, of class org.netbeans.modules.junit.JUnitProgress. */
    public void testShowMe() {
        System.out.println("testShowMe");
        fail("GUI dependent test.");
    }
    
    /** Test of hideMe method, of class org.netbeans.modules.junit.JUnitProgress. */
    public void testHideMe() {
        System.out.println("testHideMe");
        fail("GUI dependent test.");
    }
    
    /** Test of actionPerformed method, of class org.netbeans.modules.junit.JUnitProgress. */
    public void testActionPerformed() {
        System.out.println("testActionPerformed");
        fail("GUI dependent test.");
    }
    
    /** Test of setMessage method, of class org.netbeans.modules.junit.JUnitProgress. */
    public void testSetMessage() {
        System.out.println("testSetMessage");
        fail("GUI dependent test.");
    }
    
    /** Test of isCanceled method, of class org.netbeans.modules.junit.JUnitProgress. */
    public void testIsCanceled() {
        System.out.println("testIsCanceled");
        fail("GUI dependent test.");
    }
    
}
