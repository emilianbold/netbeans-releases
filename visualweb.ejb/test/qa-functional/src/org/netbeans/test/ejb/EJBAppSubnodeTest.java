/*
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.

Oracle and Java are registered trademarks of Oracle and/or its affiliates.
Other names may be trademarks of their respective owners.


The contents of this file are subject to the terms of either the GNU
General Public License Version 2 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://www.netbeans.org/cddl-gplv2.html
or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License file at
nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
particular file as subject to the "Classpath" exception as provided
by Oracle in the GPL Version 2 section of the License file that
accompanied this code. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

Contributor(s):

The Original Software is NetBeans. The Initial Developer of the Original
Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
Microsystems, Inc. All Rights Reserved.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
*/

package org.netbeans.test.ejb;

import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.*;
import org.netbeans.junit.NbTestSuite;
import junit.framework.Test;
import junit.framework.*;

import org.netbeans.modules.visualweb.gravy.dataconnectivity.ServerNavigatorOperator;
import org.netbeans.modules.visualweb.gravy.model.deployment.ApplicationServer;
import org.netbeans.modules.visualweb.gravy.plugins.PluginsOperator;
import org.netbeans.modules.visualweb.gravy.*;

import javax.swing.tree.*;
import java.awt.event.*;
import java.io.*;

/**
 *
 * @author Roman Mostyka
 */
public class EJBAppSubnodeTest extends RaveTestCase {

    private static String pluginName = "Visual Web JSF Backwards Compatibility Kit";
    public String _projectPath = System.getProperty("xtest.workdir") + File.separator + "projects" + File.separator;
    public String _projectName = "HelloWorldEJB";
    public ServerNavigatorOperator server;
    public JTreeOperator sntree;
  
    public EJBAppSubnodeTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        NbTestSuite suite = new NbTestSuite("EJB Application Subnode");
        suite.addTest(new EJBAppSubnodeTest("testPreparation"));
        suite.addTest(new EJBAppSubnodeTest("testEJB_Subnode_01_CheckPopupMenu"));
        suite.addTest(new EJBAppSubnodeTest("testEJB_Subnode_02_ExpandCollapse"));
        suite.addTest(new EJBAppSubnodeTest("testEJB_Subnode_04_Modify"));
        suite.addTest(new EJBAppSubnodeTest("testEJB_Subnode_05_Remove"));
        suite.addTest(new EJBAppSubnodeTest("testEJB_Subnode_06_Properties"));
/*        
        // can't be automated: suite.addTest(new EJBAppSubnodeTest("testEJB_Subnode_03_Refresh"));
*/        
        return suite;
    }

    /** method called before each testcase
    */
    protected void setUp() {}

    public void testPreparation() {
        PluginsOperator.getInstance().installAvailablePlugins(pluginName);
        ApplicationServer as = EJBTestUtils.getApplicationServer();
        as.start();
        TestUtils.wait(1000);
    }

    public void testEJB_Subnode_01_CheckPopupMenu() {
        String ejbSetName = "ValidJAR",
               ejbJarDir  = "EJB_Valid_JAR_Valid_EAR",
               ejbJarName = ejbJarDir + "Client.jar";
        EJBTestUtils.addEJB(ejbSetName, EJBTestUtils.RMI_IIOP_PORT_APPSERVER,
                EJBTestUtils.EJB_JARS_PATH + 
                File.separator + ejbJarDir + File.separator + ejbJarName, "");
        EJBTestUtils.endAddEJB();
        server.showNavigatorOperator().getTree().findPath(EJBTestUtils.ejbNode + "|" + ejbSetName);
        TestUtils.wait(1000);
        
        String 
            ejbSetNodeName = ejbSetName,
            ejbSetPath = EJBTestUtils.ejbNode + "|" + ejbSetNodeName;
        
        server = ServerNavigatorOperator.showNavigatorOperator();
        Util.wait(1000);
        sntree = server.getTree();
        sntree.callPopupOnPath(sntree.findPath(ejbSetPath));
        Util.wait(1000);
        
        JPopupMenuOperator snpm = new JPopupMenuOperator();
        Util.wait(1000);
        
        checkPopupMenuItems(snpm);
        Util.wait(1000);
        
        sntree.pressKey(KeyEvent.VK_ESCAPE);
        try {
          snpm = new JPopupMenuOperator();
          fail("Popup menu of EJB set [" + ejbSetNodeName + "] is still visible, but it should disappear");
        } catch (Exception e) {
            TestUtils.outMsg("+++ Popup menu of EJB set [" + ejbSetNodeName + 
                "] has disappeared after pressing ESC");
        }
        Util.wait(1000);
        
        sntree.callPopupOnPath(sntree.findPath(ejbSetPath));
        Util.wait(1000);
        snpm = new JPopupMenuOperator();
        Util.wait(1000);
        sntree.clickOnPath(sntree.findPath(EJBTestUtils.ejbNode));
        Util.wait(1000);
        try {
          snpm = new JPopupMenuOperator();
          fail("Popup menu of EJB set [" + ejbSetNodeName + "] is still visible, but it should disappear");
        } catch (Exception e) {
            TestUtils.outMsg("+++ Popup menu of EJB set [" + ejbSetNodeName + 
                "] has disappeared after click of mouse button on the other place");
        }
        
    }
 
    private void checkPopupMenuItems(JPopupMenuOperator menuOperator) {
        EJBTestUtils.checkPopupMenuItemList(menuOperator, new String[] {
            EJBTestUtils.refreshPopup,
            EJBTestUtils.modifyPopup,
            EJBTestUtils.removePopup,
            EJBTestUtils.configurePopup,
            EJBTestUtils.exportPopup}
        );
    }
    
    public void testEJB_Subnode_02_ExpandCollapse() {
        String ejbSetName = "ValidJAR",
               ejbSetNodeName = ejbSetName,
               ejbNodeName = "GreeterEJB_HelloWorld",
               ejbSetPath = EJBTestUtils.ejbNode + "|" + ejbSetNodeName,
               ejbPath = ejbSetPath + "|" + ejbNodeName;
        
        server = ServerNavigatorOperator.showNavigatorOperator();
        Util.wait(1000);
        sntree = server.getTree();

        TreePath ejbTreePath = sntree.findPath(ejbPath);
        Util.wait(1000);

        TestUtils.outMsg("+++ Tree path [" + ejbPath + "] is expanded = " + sntree.isExpanded(ejbTreePath));
        
        sntree.expandPath(ejbTreePath);
        Util.wait(1000);
        
        if (sntree.isExpanded(ejbTreePath)) {
            TestUtils.outMsg("+++ Tree path [" + ejbPath + "] is expanded = " + sntree.isExpanded(ejbTreePath));
        } else {
            fail("Tree path [" + ejbPath + "] isn't expanded");
        }

        TreePath rootEjbTreePath = sntree.findPath(EJBTestUtils.ejbNode);
        TestUtils.outMsg("+++ Tree path [" + EJBTestUtils.ejbNode + "] is collapsed = " + sntree.isCollapsed(rootEjbTreePath));
        
        sntree.collapsePath(rootEjbTreePath);
        Util.wait(1000);
        if (sntree.isCollapsed(rootEjbTreePath)) {
            TestUtils.outMsg("+++ Tree path [" + EJBTestUtils.ejbNode + "] is collapsed = " + sntree.isCollapsed(rootEjbTreePath));
        } else {
            fail("Tree path [" + EJBTestUtils.ejbNode + "] isn't collapsed");
        }
        EJBTestUtils.removeEJB(ejbSetName);
    }

    /**
     * Can't be automated (see test specification)
     */
    public void testEJB_Subnode_03_Refresh() {
        EJBTestUtils.addEJB("13700", EJBTestUtils.EJB_JARS_PATH + 
                File.separator + "Example01" + File.separator + "Example01Client.jar", 
                EJBTestUtils.EJB_JARS_PATH + 
                File.separator + "Example01" + File.separator + "Example01.ear");
        EJBTestUtils.endAddEJB();
        // undeploy EJB set
        // modify its source code to add new business method
        // modify or create new appropriate EAR-file
        // deploy modified EAR-file
        server = ServerNavigatorOperator.showNavigatorOperator();
        Util.wait(1000);
        server.pushPopup(EJBTestUtils.ejbNode + "|DeployedEjbApp1", EJBTestUtils.refreshPopup);
    }
  
    public void testEJB_Subnode_04_Modify() {
        String ejbSetName = "Modified_EJB_Set",
               ejbJarDir  = "EJB_Valid_JAR_Valid_EAR",
               ejbJarName = ejbJarDir + "Client.jar";
        EJBTestUtils.addEJB(ejbSetName, "23700",
            EJBTestUtils.EJB_JARS_PATH + 
            File.separator + ejbJarDir + File.separator + ejbJarName, "");
        EJBTestUtils.endAddEJB();
        server.showNavigatorOperator().getTree().findPath(EJBTestUtils.ejbNode + "|" + ejbSetName);
        Util.wait(1000);

        // create and verify application (the exception should be thrown after project deployment:
        // javax.naming.NameNotFoundException: No object bound for java:comp/env/GreeterEJB_HelloWorld)
        String errMsg = EJBTestUtils.createProjectWithEJB(ejbSetName, "getGreeting", 
            null, "NameNotFoundException", false, false);
        String prjName = EJBTestUtils.getLastCreatedPrjName();
        if (errMsg == null) {
            String textField_ID =  EJBTestUtils.getLastTextField_ID();
            EJBTestUtils.modifyEJBSet(ejbSetName, 2, EJBTestUtils.RMI_IIOP_PORT_APPSERVER);
            errMsg = EJBTestUtils.checkDeploymentWithEJB(prjName, textField_ID, 
                    "Hello World", true);
            EJBTestUtils.doSaveAll();
        }
        EJBTestUtils.removeEJB(ejbSetName);
        Util.wait(1000);
        new QueueTool().waitEmpty();
        EJBTestUtils.doCloseProject(prjName);
        Util.wait(1000);
        new QueueTool().waitEmpty();
        if (errMsg != null) {
            Assert.fail(errMsg);
        }
    }
  
    public void testEJB_Subnode_05_Remove() {
        String ejbSetName = "Removed_EJB_Set",
               ejbJarDir  = "EJB_Valid_JAR_Valid_EAR",
               ejbJarName = ejbJarDir + "Client.jar";
        EJBTestUtils.addEJB(ejbSetName, EJBTestUtils.RMI_IIOP_PORT_APPSERVER,
            EJBTestUtils.EJB_JARS_PATH + 
            File.separator + ejbJarDir + File.separator + ejbJarName, "");
        EJBTestUtils.endAddEJB();
        Util.wait(1000);
        
        EJBTestUtils.removeEJB(ejbSetName);
        Util.wait(1000);
        try {
            server.showNavigatorOperator().getTree().findPath(EJBTestUtils.ejbNode + "|" + ejbSetName);
            fail("EJB Set [" + ejbSetName + "] has not been removed.");
        }
        catch (Exception e) {
            TestUtils.outMsg("+++ EJB Set [" + ejbSetName + "] has been removed.");
        }
    }
  
    public void testEJB_Subnode_06_Properties() {
        String oldEjbSetName = "EJB_Set_Old",
               ejbJarDir  = "EJB_Valid_JAR_Valid_EAR",
               ejbJarName = ejbJarDir + "Client.jar",
               newEjbSetName = "EJB_Set_New", 
               newRMIPort = "12345", 
               newServerHost = "new_server_host";
        EJBTestUtils.addEJB(oldEjbSetName, EJBTestUtils.RMI_IIOP_PORT_APPSERVER,
            EJBTestUtils.EJB_JARS_PATH + 
            File.separator + ejbJarDir + File.separator + ejbJarName, "");
        EJBTestUtils.endAddEJB();
        Util.wait(1000);
        
        server = ServerNavigatorOperator.showNavigatorOperator();
        Util.wait(1000);
        sntree = server.getTree();
        try {
            sntree.selectPath(sntree.findPath(EJBTestUtils.ejbNode + "|" + oldEjbSetName));
            TestUtils.outMsg("+++ EJB Set [" + oldEjbSetName + "] has been found.");
        } catch (Exception e) {
            fail("+++ EJB Set [" + oldEjbSetName + "] has not been found.");
        }
        EJBTestUtils.changeEjbSetProperties(newEjbSetName, newRMIPort, newServerHost);
        EJBTestUtils.verifyModifiedEJBSet(newEjbSetName, new String[] {
            newEjbSetName, newServerHost, newRMIPort});
        
        EJBTestUtils.removeEJB(newEjbSetName);
        TestUtils.wait(1000);
    }
}
