/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

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
