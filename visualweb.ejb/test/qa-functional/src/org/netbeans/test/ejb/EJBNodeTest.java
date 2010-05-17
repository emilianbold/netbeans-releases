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

import org.netbeans.modules.visualweb.gravy.dataconnectivity.*;
import org.netbeans.modules.visualweb.gravy.*;

import java.awt.event.*;
import java.io.*;

/**
 * @author Roman Mostyka
 */
public class EJBNodeTest extends RaveTestCase {
    public ServerNavigatorOperator server;
    public JTreeOperator sntree;
  
    public EJBNodeTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        NbTestSuite suite = new NbTestSuite("EJB Node");
        suite.addTest(new EJBNodeTest("testEJB_Node_01_Popup"));
        suite.addTest(new EJBNodeTest("testEJB_Node_02_ExpandCollapse"));
        suite.addTest(new EJBNodeTest("testEJB_Node_03_AddDialog"));
        return suite;
    }

    /** method called before each testcase
    */
    protected void setUp() {}

    public void testEJB_Node_01_Popup() {
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(1000);
        sntree = server.getTree();
        sntree.callPopupOnPath(sntree.findPath(EJBTestUtils.ejbNode));
        TestUtils.wait(1000);
        JPopupMenuOperator snpm = new JPopupMenuOperator();
        TestUtils.wait(1000);
        
        EJBTestUtils.checkPopupMenuItemList(snpm, new String[] {
            EJBTestUtils.sessionEJBPopup, 
            EJBTestUtils.importEJBPopup,
            EJBTestUtils.exportEJBsPopup}
        );
        TestUtils.wait(1000);

        String timeoutName = "WindowWaiter.WaitWindowTimeout";
        long  timeoutValue = JemmyProperties.getCurrentTimeout(timeoutName);
        JemmyProperties.getCurrentTimeouts().setTimeout(timeoutName, 8000);
        
        sntree.pressKey(KeyEvent.VK_ESCAPE);
        TestUtils.wait(1000);
        try {
            snpm = new JPopupMenuOperator();
            fail("Popup menu is still displayed after pressing the key ESC.");
        } catch (Exception e) {
            e.printStackTrace();
            TestUtils.outMsg("+++ Popup menu has disappeared after pressing the key ESC.");
        }
        TestUtils.wait(1000);
        
        sntree.callPopupOnPath(sntree.findPath(EJBTestUtils.ejbNode));
        TestUtils.wait(1000);
        snpm = new JPopupMenuOperator();
        TestUtils.wait(1000);
        sntree.clickOnPath(sntree.findPath(EJBTestUtils.ejbNode));
        TestUtils.wait(1000);
        try {
            snpm = new JPopupMenuOperator();
            fail("Popup menu is still displayed after mouse clicking.");
        } catch (Exception e) {
            e.printStackTrace();
            TestUtils.outMsg("+++ Popup menu has disappeared after mouse clicking.");
        }
    }
  
    public void testEJB_Node_02_ExpandCollapse() {
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
        
        String[] ejbSetNames = new String[] {
            "ValidJAR",
            "ValidJAR_ValidEAR",
            "ValidJAR_InvalidEAR"
        };
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(1000);
        sntree = server.getTree();
        sntree.collapsePath(sntree.findPath(EJBTestUtils.ejbNode));
        TestUtils.wait(1000);
        sntree.expandPath(sntree.findPath(EJBTestUtils.ejbNode));
        TestUtils.wait(1000);
        for (int i = 0; i < ejbSetNames.length; ++i) {
            sntree.findPath(EJBTestUtils.ejbNode + "|" + ejbSetNames[i]);
        }
        TestUtils.wait(1000);
        new QueueTool().waitEmpty();
        for (int i = 0; i < ejbSetNames.length; ++i) {
            EJBTestUtils.removeEJB(ejbSetNames[i]);
        }
    }
  
    public void testEJB_Node_03_AddDialog() {
        server = ServerNavigatorOperator.showNavigatorOperator();
        TestUtils.wait(1000);
        sntree = server.getTree();
        server.pushPopup(EJBTestUtils.ejbNode, EJBTestUtils.sessionEJBPopup);
        TestUtils.wait(1000);
        new JButtonOperator (new JDialogOperator(), EJBTestUtils.btn_Cancel).pushNoBlock();
        TestUtils.wait(1000);
        new QueueTool().waitEmpty();
    }
}
