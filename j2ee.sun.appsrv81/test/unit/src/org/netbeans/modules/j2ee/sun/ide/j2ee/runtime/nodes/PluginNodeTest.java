/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * PluginNodeTest.java
 * JUnit based test
 *
 * Created on March 17, 2004, 10:26 AM
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.nodes;

import junit.framework.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.*;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.actions.RegisterServerAction;
import java.util.Collection;
import org.netbeans.modules.j2ee.sun.share.SecurityMasterListModel;

import java.util.Arrays;

import org.netbeans.modules.j2ee.sun.share.SunDeploymentFactory;
/**
 *
 * @author vkraemer
 */
public class PluginNodeTest extends TestCase {
    
    public void testHasRequiredChildren() {
        String startingPoint = System.getProperty("java.home");
        java.io.File f = new java.io.File(startingPoint);
        assertTrue(PluginNode.hasRequiredChildren(f,null));
        java.util.Collection l = new java.util.ArrayList();
        assertTrue(PluginNode.hasRequiredChildren(f, l));
        l.add("lib");
        assertTrue(PluginNode.hasRequiredChildren(f, l));
        l.add("bin");
        assertTrue(PluginNode.hasRequiredChildren(f, l));
        l.add("foobaloo");
        assertTrue(!PluginNode.hasRequiredChildren(f, l));
        f = new java.io.File(startingPoint+java.io.File.separator+
            "COPYRIGHT");
        assertTrue(!PluginNode.hasRequiredChildren(f, l));
    }
    
 /*obsolete with nb4.1, now j2ee platofmr manager is used
  public void testStaticGetters() {
        java.io.File fo = PluginNode.getInstallRootStatic();
        assertNotNull(fo);
        String path = fo.getAbsolutePath();
        if (java.io.File.separator.equals("/"))
            assertEquals(PluginNode.UNIX_DEFAULT_INSTALL_ROOT, path);
        else
            assertEquals(PluginNode.WINDOWS_DEFAULT_INSTALL_ROOT, path);
        assertEquals(path, System.getProperty(PluginNode.INSTALL_ROOT_PROP_NAME));
        PluginNode pn = new PluginNode(new SunDeploymentFactory());
        fo = new java.io.File(".");
        pn.setInstallRoot(fo);
        fo = PluginNode.getInstallRootStatic();
        assertEquals(fo.getAbsolutePath(), System.getProperty(PluginNode.INSTALL_ROOT_PROP_NAME));        
    }
 */   
/*    public void testContainsSameElements() {
        PluginNode node = new PluginNode(new SunDeploymentFactory());
        String[] newValues = new String[] { "a", "b", "c" };
        SecurityMasterListModel pModel = 
            SecurityMasterListModel.getGroupMasterModel();
        assertTrue(!node.containsSameElements(pModel, newValues)); 
        node.setGroupList(newValues);
        assertTrue(node.containsSameElements(pModel,newValues)); 
    }
  */  
    public void testCreateSheet() {
        PluginNode node = new PluginNode(new SunDeploymentFactory());
        node.createSheet();
    }
        
    
    public void testChangeListener() {
        PluginNode node = new PluginNode(new SunDeploymentFactory());
        node.addPCL(); 
        node.setLogLevel("INFO");
    }
        
    
    public void testSetLogLevel() {
        PluginNode node = new PluginNode(new SunDeploymentFactory());
        node.setLogLevel("foo");
        assertEquals("OFF", node.getLogLevel());
        node.setLogLevel("SEVERE");
        assertEquals("SEVERE", node.getLogLevel());
    }
    
    public void testCreate() {
        PluginNode node = new PluginNode(new SunDeploymentFactory());
        assertEquals("OFF", node.getLogLevel());
    }
        
    
    /** Test of getUserList method, of class org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.nodes.PluginNode. */
    public void testUserListRoutines() {
        PluginNode node = new PluginNode(new SunDeploymentFactory());
        String[] values = node.getUserList();
        String[] newValues = new String[] { "a", "b", "c" };
        node.setUserList(newValues);
        String[] read = node.getUserList();
        assertTrue("wrong length: " + read.length, read.length == 3);
        assertEquals("a", read[0]);
        assertEquals("b", read[1]);
        assertEquals("c", read[2]);
        newValues[0] = "x";
        newValues[2] = "z";
        node.setUserList(newValues);
        read = node.getUserList();
        assertTrue("arry length is wrong: " + read.length, read.length == 3);
        assertEquals("x", read[0]);
        assertEquals("b", read[1]);
        assertEquals("z", read[2]);
    }

    public void testGroupListRoutines() {
        PluginNode node = new PluginNode(new SunDeploymentFactory());
        String[] values = node.getGroupList();
        String[] newValues = new String[] { "a", "b", "c" };
        node.setGroupList(newValues);
        String[] read = node.getGroupList();
        assertTrue(read.length == 3);
        assertEquals("a", read[0]);
        assertEquals("b", read[1]);
        assertEquals("c", read[2]);
        newValues[0] = "x";
        newValues[2] = "z";
        node.setGroupList(newValues);
        read = node.getGroupList();
        assertTrue("arry length is wrong: " + read.length, read.length == 3);
        assertEquals("x", read[0]);
        assertEquals("b", read[1]);
        assertEquals("z", read[2]);
    }
    
    public PluginNodeTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(PluginNodeTest.class);
        return suite;
    }
        
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
