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
import org.netbeans.modules.visualweb.gravy.*;

import javax.swing.tree.*;
import java.util.*;
import java.io.*;

/**
 *
 * @author Roman Mostyka
 */
public class ExportEJBTest extends RaveTestCase {
    
    private ServerNavigatorOperator server;
    private JTreeOperator sntree;
    private static String[] ejbSetNames;
  
    public ExportEJBTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        NbTestSuite suite = new NbTestSuite("EJB Export");
        suite.addTest(new ExportEJBTest("testExportEJB_01_Default_Export_1_EJB"));
        suite.addTest(new ExportEJBTest("testExportEJB_02_Specify_Path_1_EJB"));
        suite.addTest(new ExportEJBTest("testExportEJB_03_Default_Export_All_EJBs"));
        suite.addTest(new ExportEJBTest("testExportEJB_04_Default_Export_All_EJBs"));
        suite.addTest(new ExportEJBTest("testExportEJB_05_Default_Export_Required_EJB"));
        suite.addTest(new ExportEJBTest("testExportEJB_06_Cancel"));
        suite.addTest(new ExportEJBTest("testExportEJB_07_Help"));
        return suite;
    }

    /** method called before each testcase */
    protected void setUp() {}

    public void testExportEJB_01_Default_Export_1_EJB() {
        String ejbSetName = "Export_EJB_Set",
               ejbJarDir  = "EJB_Valid_JAR_Valid_EAR",
               ejbJarName = ejbJarDir + "Client.jar";
        EJBTestUtils.addEJB(ejbSetName, EJBTestUtils.RMI_IIOP_PORT_APPSERVER,
                EJBTestUtils.EJB_JARS_PATH + 
                File.separator + ejbJarDir + File.separator + ejbJarName, "");
        EJBTestUtils.endAddEJB();
        TestUtils.wait(1000);

        String errMsg = EJBTestUtils.exportEJBSet(ejbSetName, true);
        EJBTestUtils.removeEJB(ejbSetName);
        Util.wait(1000);
        new QueueTool().waitEmpty();
        if (errMsg != null) {
            Assert.fail(errMsg);
        }
    }
  
    public void testExportEJB_02_Specify_Path_1_EJB() {
        String ejbSetName = "Export_EJB_Set",
               ejbJarDir  = "EJB_Valid_JAR_Valid_EAR",
               ejbJarName = ejbJarDir + "Client.jar",
               exportJarName = "export_EJB_Jar.jar",
               exportJarAbsoluteName = System.getProperty("xtest.workdir") + "/" + exportJarName;
        
        EJBTestUtils.addEJB(ejbSetName, EJBTestUtils.RMI_IIOP_PORT_APPSERVER,
                EJBTestUtils.EJB_JARS_PATH + 
                File.separator + ejbJarDir + File.separator + ejbJarName, "");
        EJBTestUtils.endAddEJB();
        TestUtils.wait(1000);

        String errMsg = EJBTestUtils.exportEJBSet(ejbSetName, exportJarAbsoluteName, true);
        EJBTestUtils.removeEJB(ejbSetName);
        Util.wait(1000);
        new QueueTool().waitEmpty();
        if (errMsg != null) {
            Assert.fail(errMsg);
        }
    }
  
    public void testExportEJB_03_Default_Export_All_EJBs() {
        String ejbSetName = "ValidJAR",
               ejbJarDir  = "EJB_Valid_JAR_Valid_EAR",
               ejbJarName = ejbJarDir + "Client.jar";
        EJBTestUtils.addEJB(ejbSetName, EJBTestUtils.RMI_IIOP_PORT_APPSERVER,
                EJBTestUtils.EJB_JARS_PATH + 
                File.separator + ejbJarDir + File.separator + ejbJarName, "");
        EJBTestUtils.endAddEJB();
        ejbSetName = "ValidJAR_ValidEAR";
        ejbJarDir  = "EJB_Valid_JAR_Valid_EAR";
        ejbJarName = ejbJarDir + "Client.jar";
        String ejbEarName = ejbJarDir + ".ear";
        EJBTestUtils.addEJB(ejbSetName, EJBTestUtils.RMI_IIOP_PORT_APPSERVER,
            EJBTestUtils.EJB_JARS_PATH + 
            File.separator + ejbJarDir + File.separator + ejbJarName, 
            EJBTestUtils.EJB_JARS_PATH + 
            File.separator + ejbJarDir + File.separator + ejbEarName);
        EJBTestUtils.endAddEJB();
        ejbSetName = "ValidJAR_InvalidEAR";
        ejbJarDir  = "EJB_Valid_JAR_Valid_EAR";
        String ejbEarDir  = "EJB_Invalid_EAR";
        ejbJarName = ejbJarDir + "Client.jar";
        ejbEarName = ejbEarDir + ".ear";
        EJBTestUtils.addEJB(ejbSetName, EJBTestUtils.RMI_IIOP_PORT_APPSERVER,
            EJBTestUtils.EJB_JARS_PATH + 
            File.separator + ejbJarDir + File.separator + ejbJarName, 
            EJBTestUtils.EJB_JARS_PATH + 
            File.separator + ejbEarDir + File.separator + ejbEarName);
        EJBTestUtils.endAddEJB();
        
        ejbSetNames = new String[] {
            "ValidJAR",
            "ValidJAR_ValidEAR",
            "ValidJAR_InvalidEAR"
        };
        ejbSetName = "ValidJAR_ValidEAR";
        String ejbTreeNodePath = EJBTestUtils.ejbNode + "|" + ejbSetName;
        
        List ejbSetNameList = EJBTestUtils.getAvailableEJBSetNameList();
        String errMsg = EJBTestUtils.exportAllEJBSets(ejbTreeNodePath, ejbSetNameList, true, true);
        Util.wait(500);
        new QueueTool().waitEmpty();
        if (errMsg != null) {
            Assert.fail(errMsg);
        }
    }
  
    public void testExportEJB_04_Default_Export_All_EJBs() {
        String ejbTreeNodePath = EJBTestUtils.ejbNode;

        List ejbSetNameList = EJBTestUtils.getAvailableEJBSetNameList();
        String errMsg = EJBTestUtils.exportAllEJBSets(ejbTreeNodePath, ejbSetNameList, false, true);
        Util.wait(500);
        new QueueTool().waitEmpty();
        if (errMsg != null) {
            Assert.fail(errMsg);
        }
    }
  
    public void testExportEJB_05_Default_Export_Required_EJB() {
        String ejbSetName = "ValidJAR_ValidEAR";
        String errMsg = EJBTestUtils.exportRequiredEJBSets(new String[] {ejbSetName});
        Util.wait(500);
        new QueueTool().waitEmpty();
        if (errMsg != null) {
            Assert.fail(errMsg);
        }
    }
 
    public void testExportEJB_06_Cancel() {
        String ejbSetName = "Export_EJB_Set",
               ejbJarDir  = "EJB_Valid_JAR_Valid_EAR",
               ejbJarName = ejbJarDir + "Client.jar";
        EJBTestUtils.addEJB(ejbSetName, EJBTestUtils.RMI_IIOP_PORT_APPSERVER,
                EJBTestUtils.EJB_JARS_PATH + 
                File.separator + ejbJarDir + File.separator + ejbJarName, "");
        EJBTestUtils.endAddEJB();
        TestUtils.wait(1000);
        
        server = ServerNavigatorOperator.showNavigatorOperator();
        Util.wait(1000);
        String ejbTreeNodePath = EJBTestUtils.ejbNode + "|" + ejbSetName;
        server.pushPopup(ejbTreeNodePath, EJBTestUtils.exportEJBPopup);
        Util.wait(1000);
        
        JDialogOperator dlg_export = new JDialogOperator(EJBTestUtils.dlg_exportEJB);
        Util.wait(1000);
        new QueueTool().waitEmpty();
        
        new JButtonOperator(dlg_export, EJBTestUtils.btn_Cancel).pushNoBlock();
        Util.wait(1000);
        new QueueTool().waitEmpty();
        
        String errMsg = null;
        try {
            dlg_export = new JDialogOperator(EJBTestUtils.dlg_exportEJB);
            Util.wait(1000);
            errMsg = "Dialog [" + dlg_export.getTitle() + "] exists";
        } catch (Exception e) {
            TestUtils.outMsg("+++ Dialog [" + dlg_export.getTitle() + "] not found");
        }
        EJBTestUtils.removeEJB(ejbSetName);
        Util.wait(1000);
        new QueueTool().waitEmpty();
        if (errMsg != null) {
            Assert.fail(errMsg);
        }
    }
    
    public void testExportEJB_07_Help() {
        server = ServerNavigatorOperator.showNavigatorOperator();
        Util.wait(1000);
        String ejbTreeNodePath = EJBTestUtils.ejbNode;
        server.pushPopup(ejbTreeNodePath, EJBTestUtils.exportEJBsPopup);
        Util.wait(1000);
        
        JDialogOperator dialog = new JDialogOperator(EJBTestUtils.dlg_exportEJB);
        Util.wait(1000);
        new QueueTool().waitEmpty();
        
        new JButtonOperator(dialog, EJBTestUtils.btn_Help).pushNoBlock();
        Util.wait(10000);
        new QueueTool().waitEmpty();
        
        String errMsg = null;
        try {
            try {
                dialog = new JDialogOperator(EJBTestUtils.dlg_export_help);
                Util.wait(1000);
                TestUtils.outMsg("+++ Dialog [" + dialog.getTitle() + "] exists");
                dialog.close();
            }
            catch (Exception e) {
                JFrameOperator frame = new JFrameOperator(EJBTestUtils.dlg_export_help);
                Util.wait(1000);
                TestUtils.outMsg("+++ Frame [" + frame.getTitle() + "] exists");
                frame.close();
            }

            Util.wait(1000);
            new QueueTool().waitEmpty();
            
            dialog = new JDialogOperator(EJBTestUtils.dlg_exportEJB);
            Util.wait(1000);
            new QueueTool().waitEmpty();
            new JButtonOperator(dialog, EJBTestUtils.btn_Cancel).pushNoBlock();
            Util.wait(1000);
            new QueueTool().waitEmpty();
        } catch (Exception e) {
            errMsg = "Dialog [" + EJBTestUtils.dlg_export_help + "] not found";
        }
        for (int i = 0; i < ejbSetNames.length; ++i) {
            EJBTestUtils.removeEJB(ejbSetNames[i]);
        }
        Util.wait(1000);
        new QueueTool().waitEmpty();
        if (errMsg != null) {
            Assert.fail(errMsg);
        }
        EJBTestUtils.getApplicationServer().stop();
    }
}
