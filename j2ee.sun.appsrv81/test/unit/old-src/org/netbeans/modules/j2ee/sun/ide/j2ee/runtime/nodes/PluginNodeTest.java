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
    
}
