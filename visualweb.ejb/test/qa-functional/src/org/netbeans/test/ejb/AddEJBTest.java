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

import org.netbeans.modules.visualweb.gravy.model.deployment.ApplicationServer;
import org.netbeans.modules.visualweb.gravy.plugins.PluginsOperator;
import org.netbeans.modules.visualweb.gravy.dataconnectivity.*;
import org.netbeans.modules.visualweb.gravy.*;

import java.io.*;

/**
 *
 * @author Roman Mostyka
 */
public class AddEJBTest extends RaveTestCase {

    private static String pluginName = "Visual Web JSF Backwards Compatibility Kit";
    private static int lbIndex = EJBTestUtils.getLabelIndex();

    public ServerNavigatorOperator server;
    public JTreeOperator sntree;
  
    public AddEJBTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        NbTestSuite suite = new NbTestSuite("Add Set of Session EJBs");
        suite.addTest(new AddEJBTest("testPreparation"));
        suite.addTest(new AddEJBTest("testEJB_Dia_01_NoJAR"));
        suite.addTest(new AddEJBTest("testEJB_Dia_02_CancelDialog"));
        suite.addTest(new AddEJBTest("testEJB_Dia_03_InvalidJAR"));
        suite.addTest(new AddEJBTest("testEJB_Dia_04_ValidJAR"));
        suite.addTest(new AddEJBTest("testEJB_Dia_05_ValidJAR_ValidEAR"));
        suite.addTest(new AddEJBTest("testEJB_Dia_06_ValidJAR_InvalidEAR"));
        suite.addTest(new AddEJBTest("testEJB_Dia_07_NoJAR_ValidEAR"));
        suite.addTest(new AddEJBTest("testEJB_Dia_08_InvalidJAR_InvalidEAR"));
        suite.addTest(new AddEJBTest("testEJB_Dia_09_ValidJAR_NoPackage"));
        suite.addTest(new AddEJBTest("testEJB_Dia_10_ValidJARs_2EJBs"));
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

    public void testEJB_Dia_01_NoJAR() {
        EJBTestUtils.addEJB();
        TestUtils.wait(1000);
        JDialogOperator dlg_error = new JDialogOperator();
        String errMsg = null;
        if (!(new JLabelOperator(dlg_error, lbIndex).getText().trim().equals(EJBTestUtils.msg_emptyJAR))) {
            errMsg = "Wrong message!";
        }
        TestUtils.wait(1000);
        new JButtonOperator(new JDialogOperator(), EJBTestUtils.btn_Cancel).pushNoBlock();

        Util.wait(1000);
        new QueueTool().waitEmpty();
        if (errMsg != null) {
            fail(errMsg);
        }
    }
  
    public void testEJB_Dia_02_CancelDialog() {
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(1000);
        sntree = server.getTree();
        server.pushPopup(EJBTestUtils.ejbNode, EJBTestUtils.sessionEJBPopup);
        TestUtils.wait(1000);
        JDialogOperator addDialog = new JDialogOperator();
        new JButtonOperator(addDialog, EJBTestUtils.btn_Cancel).pushNoBlock();
        Util.wait(1000);
        new QueueTool().waitEmpty();
    }
  
    public void testEJB_Dia_03_InvalidJAR() {
        String ejbSetName = "InvalidJAR",
               ejbJarDir  = "EJB_Invalid_JAR",
               ejbJarName = "EJB_Invalid_Client_JAR.jar";
        EJBTestUtils.addEJB(ejbSetName, EJBTestUtils.RMI_IIOP_PORT_APPSERVER,
                EJBTestUtils.EJB_JARS_PATH + 
                File.separator + ejbJarDir + File.separator + ejbJarName, "");
        
        JDialogOperator dlg_error = new JDialogOperator();
        Util.wait(1000);
        
        String dialogErrMessage = new JLabelOperator(dlg_error, lbIndex).getText().trim();
        if (dialogErrMessage.indexOf(EJBTestUtils.msg_noDescriptor) == -1) {
            Util.wait(1000);
            new JButtonOperator(new JDialogOperator(), EJBTestUtils.btn_Cancel).pushNoBlock();
            Util.wait(1000);
            new QueueTool().waitEmpty();
            fail("Invalid Client JAR: Wrong message in the error-dialog!");
        }
        Util.wait(1000);
        new QueueTool().waitEmpty();
        
        new JButtonOperator(new JDialogOperator(), EJBTestUtils.btn_Cancel).pushNoBlock();
        Util.wait(1000);
        new QueueTool().waitEmpty();
        try {
            server.showNavigatorOperator().getTree().findPath(EJBTestUtils.ejbNode + "|" + ejbSetName);
            fail("EJB Set subnode [" + ejbSetName + "] has been found under the Server's tree node [" + EJBTestUtils.ejbNode + "]");
        } catch (Exception e) {
            TestUtils.outMsg("+++ EJB Set subnode [" + ejbSetName + "] not found under the Server's tree node [" + EJBTestUtils.ejbNode + "]");
        }
        Util.wait(1000);
        new QueueTool().waitEmpty();
    }
  
    public void testEJB_Dia_04_ValidJAR() {
        String ejbSetName = "ValidJAR",
               ejbJarDir  = "EJB_Valid_JAR_Valid_EAR",
               ejbJarName = ejbJarDir + "Client.jar";
        EJBTestUtils.addEJB(ejbSetName, EJBTestUtils.RMI_IIOP_PORT_APPSERVER,
                EJBTestUtils.EJB_JARS_PATH + 
                File.separator + ejbJarDir + File.separator + ejbJarName, "");
        EJBTestUtils.endAddEJB();
        server.showNavigatorOperator().getTree().findPath(EJBTestUtils.ejbNode + "|" + ejbSetName);
        TestUtils.wait(1000);

        // create and verify application
        String errMsg = EJBTestUtils.createProjectWithEJB(ejbSetName, "getGreeting", 
            new String[] {
                ejbJarName, 
                ejbSetName + "ClientWrapper.jar", 
                ejbSetName + "DesignTime.jar", 
            }, 
            "Hello World", true, true);
                    
        EJBTestUtils.removeEJB(ejbSetName);

        Util.wait(1000);
        new QueueTool().waitEmpty();
        if (errMsg != null) {
            Assert.fail(errMsg);
        }
    }
  
    public void testEJB_Dia_05_ValidJAR_ValidEAR() {
        String ejbSetName = "ValidJAR_ValidEAR",
               ejbJarDir  = "EJB_Valid_JAR_Valid_EAR",
               ejbJarName = ejbJarDir + "Client.jar",
               ejbEarName = ejbJarDir + ".ear";
        EJBTestUtils.addEJB(ejbSetName, EJBTestUtils.RMI_IIOP_PORT_APPSERVER,
            EJBTestUtils.EJB_JARS_PATH + 
            File.separator + ejbJarDir + File.separator + ejbJarName, 
            EJBTestUtils.EJB_JARS_PATH + 
            File.separator + ejbJarDir + File.separator + ejbEarName);
        EJBTestUtils.endAddEJB();
        
        server.showNavigatorOperator().getTree().findPath(EJBTestUtils.ejbNode + "|" + ejbSetName);
        TestUtils.wait(1000);

        // create and verify application
        String errMsg = EJBTestUtils.createProjectWithEJB(ejbSetName, "getGreeting", 
            new String[] {
                ejbJarName, 
                ejbSetName + "ClientWrapper.jar", 
                ejbSetName + "DesignTime.jar", 
            }, 
            "Hello World", true, true);
                    
        EJBTestUtils.removeEJB(ejbSetName);

        Util.wait(1000);
        new QueueTool().waitEmpty();
        if (errMsg != null) {
            Assert.fail(errMsg);
        }
    }
  
    public void testEJB_Dia_06_ValidJAR_InvalidEAR() {
        String ejbSetName = "ValidJAR_InvalidEAR",
               ejbJarDir  = "EJB_Valid_JAR_Valid_EAR",
               ejbEarDir  = "EJB_Invalid_EAR",
               ejbJarName = ejbJarDir + "Client.jar",
               ejbEarName = ejbEarDir + ".ear";
        EJBTestUtils.addEJB(ejbSetName, EJBTestUtils.RMI_IIOP_PORT_APPSERVER,
            EJBTestUtils.EJB_JARS_PATH + 
            File.separator + ejbJarDir + File.separator + ejbJarName, 
            EJBTestUtils.EJB_JARS_PATH + 
            File.separator + ejbEarDir + File.separator + ejbEarName);
        EJBTestUtils.endAddEJB();
        
        server.showNavigatorOperator().getTree().findPath(EJBTestUtils.ejbNode + "|" + ejbSetName);
        TestUtils.wait(1000);

        // create and verify application
        String errMsg = EJBTestUtils.createProjectWithEJB(ejbSetName, "getGreeting", 
            new String[] {
                ejbJarName, 
                ejbSetName + "ClientWrapper.jar", 
                ejbSetName + "DesignTime.jar", 
            }, 
            "Hello World", true, true);
                    
        EJBTestUtils.removeEJB(ejbSetName);

        Util.wait(1000);
        new QueueTool().waitEmpty();
        if (errMsg != null) {
            Assert.fail(errMsg);
        }
    }
  
    public void testEJB_Dia_07_NoJAR_ValidEAR() {
        String ejbSetName = "NoJAR_ValidEAR",
               ejbEarDir  = "EJB_Valid_JAR_Valid_EAR",
               ejbEarName = ejbEarDir + ".ear";
        EJBTestUtils.addEJB(ejbSetName, EJBTestUtils.RMI_IIOP_PORT_APPSERVER, "", 
            EJBTestUtils.EJB_JARS_PATH + 
            File.separator + ejbEarDir + File.separator + ejbEarName);
        
        JDialogOperator dlg_error = new JDialogOperator();
        Util.wait(1000);
        
        String dialogErrMessage = new JLabelOperator(dlg_error, lbIndex).getText().trim();
        if (dialogErrMessage.indexOf(EJBTestUtils.msg_emptyJAR) == -1) {
            Util.wait(1000);
            new JButtonOperator(new JDialogOperator(), EJBTestUtils.btn_Cancel).pushNoBlock();
            Util.wait(1000);
            new QueueTool().waitEmpty();
            fail("No client JAR and valid EAR: Wrong message in the error-dialog!");
        }
        Util.wait(1000);
        new JButtonOperator(new JDialogOperator(), EJBTestUtils.btn_Cancel).pushNoBlock();
        Util.wait(1000);
        new QueueTool().waitEmpty();
        try {
            server.showNavigatorOperator().getTree().findPath(EJBTestUtils.ejbNode + "|" + ejbSetName);
            fail("EJB Set subnode [" + ejbSetName + "] has been found under the Server's tree node [" + EJBTestUtils.ejbNode + "]");
        } catch (Exception e) {
            TestUtils.outMsg("+++ EJB Set subnode [" + ejbSetName + "] not found under the Server's tree node [" + EJBTestUtils.ejbNode + "]");
        }
        Util.wait(1000);
        new QueueTool().waitEmpty();
    }

    public void testEJB_Dia_08_InvalidJAR_InvalidEAR() {
        String ejbSetName = "InvalidJAR_InvalidEAR",
               ejbJarDir  = "EJB_Invalid_JAR",
               ejbEarDir  = "EJB_Invalid_EAR",
               ejbJarName = "EJB_Invalid_Client_JAR.jar",
               ejbEarName = ejbEarDir + ".ear";
        EJBTestUtils.addEJB(ejbSetName, EJBTestUtils.RMI_IIOP_PORT_APPSERVER,
            EJBTestUtils.EJB_JARS_PATH + 
            File.separator + ejbJarDir + File.separator + ejbJarName, 
            EJBTestUtils.EJB_JARS_PATH + 
            File.separator + ejbEarDir + File.separator + ejbEarName);
        
        JDialogOperator dlg_error = new JDialogOperator();
        Util.wait(1000);
        
        String dialogErrMessage = new JLabelOperator(dlg_error, lbIndex).getText().trim();
        if (dialogErrMessage.indexOf(EJBTestUtils.msg_noDescriptor) == -1) {
            Util.wait(1000);
            new JButtonOperator(new JDialogOperator(), EJBTestUtils.btn_Cancel).pushNoBlock();
            Util.wait(1000);
            new QueueTool().waitEmpty();
            fail("Invalid JAR and Invalid EAR: Wrong message in the error-dialog!");
        }
        Util.wait(1000);
        new JButtonOperator(new JDialogOperator(), EJBTestUtils.btn_Cancel).pushNoBlock();
        Util.wait(1000);
        new QueueTool().waitEmpty();
        try {
            server.showNavigatorOperator().getTree().findPath(EJBTestUtils.ejbNode + "|" + ejbSetName);
            fail("EJB Set subnode [" + ejbSetName + "] has been found under the Server's tree node [" + EJBTestUtils.ejbNode + "]");
        } catch (Exception e) {
            TestUtils.outMsg("+++ EJB Set subnode [" + ejbSetName + "] not found under the Server's tree node [" + EJBTestUtils.ejbNode + "]");
        }
        Util.wait(1000);
        new QueueTool().waitEmpty();
    }
  
    public void testEJB_Dia_09_ValidJAR_NoPackage() {
        String ejbSetName = "ValidJAR_NoPackage",
               ejbJarDir  = "EJB_Valid_JAR_No_Package",
               ejbEarDir  = "EJB_Valid_JAR_No_Package",
               ejbJarName = ejbJarDir + "Client.jar",
               ejbEarName = ejbEarDir + ".ear";
        
        EJBTestUtils.addEJB(ejbSetName, EJBTestUtils.RMI_IIOP_PORT_APPSERVER,
            EJBTestUtils.EJB_JARS_PATH + 
            File.separator + ejbJarDir + File.separator + ejbJarName, 
            EJBTestUtils.EJB_JARS_PATH + 
            File.separator + ejbEarDir + File.separator + ejbEarName);
        
        JDialogOperator dlg_error = new JDialogOperator();
        Util.wait(1000);
        
        String 
            dialogErrMessage = new JLabelOperator(dlg_error, lbIndex).getText().trim(),
            checkMessage = EJBTestUtils.msg_noEJBsFound + " " + EJBTestUtils.msg_skipped;
        if (dialogErrMessage.indexOf(checkMessage) == -1) {
            Util.wait(1000);
            new JButtonOperator(new JDialogOperator(), EJBTestUtils.btn_Cancel).pushNoBlock();
            Util.wait(1000);
            new QueueTool().waitEmpty();
            fail("Valid JAR without Package: Wrong message in the error-dialog!");
        }
        Util.wait(1000);
        new JButtonOperator(new JDialogOperator(), EJBTestUtils.btn_Cancel).pushNoBlock();
        Util.wait(1000);
        new QueueTool().waitEmpty();
        try {
            server.showNavigatorOperator().getTree().findPath(EJBTestUtils.ejbNode + "|" + ejbSetName);
            fail("EJB Set subnode [" + ejbSetName + "] has been found under the Server's tree node [" + EJBTestUtils.ejbNode + "]");
        } catch (Exception e) {
            TestUtils.outMsg("+++ EJB Set subnode [" + ejbSetName + "] not found under the Server's tree node [" + EJBTestUtils.ejbNode + "]");
        }

        Util.wait(1000);
        new QueueTool().waitEmpty();
    }
  
    public void testEJB_Dia_10_ValidJARs_2EJBs() {
        String ejbSetName = "Valid_JARs_2EJBs",
               ejbJarDir  = "EJB_Valid_JARs_2EJBs",
               ejbEarDir  = "EJB_Valid_JARs_2EJBs",
               ejbJarName = ejbJarDir + "Client.jar",
               ejbEarName = ejbEarDir + ".ear";
        
        EJBTestUtils.addEJB(ejbSetName, EJBTestUtils.RMI_IIOP_PORT_APPSERVER,
            EJBTestUtils.EJB_JARS_PATH + 
            File.separator + ejbJarDir + File.separator + ejbJarName, 
            EJBTestUtils.EJB_JARS_PATH + 
            File.separator + ejbJarDir + File.separator + ejbEarName);
        EJBTestUtils.endAddEJB();
        
        server.showNavigatorOperator().getTree().findPath(EJBTestUtils.ejbNode + "|" + ejbSetName);
        TestUtils.wait(1000);

        // create and verify application
        String errMsg = EJBTestUtils.createProjectWithEJB(ejbSetName, "getFarewell", 
            new String[] {
                ejbJarName, 
                ejbSetName + "ClientWrapper.jar", 
                ejbSetName + "DesignTime.jar", 
            }, 
            "Good bye", true, true);
                    
        EJBTestUtils.removeEJB(ejbSetName);

        Util.wait(1000);
        new QueueTool().waitEmpty();
        if (errMsg != null) {
            Assert.fail(errMsg);
        }
    }
}
