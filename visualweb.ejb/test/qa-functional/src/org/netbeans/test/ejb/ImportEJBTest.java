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

import java.io.*;

/**
 *
 * @author Roman Mostyka
 */
public class ImportEJBTest extends RaveTestCase {
    public ServerNavigatorOperator server;
    public JTreeOperator sntree;

    public ImportEJBTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        NbTestSuite suite = new NbTestSuite("EJB Import");
        suite.addTest(new ImportEJBTest("testImportEJB_01_Import_1_EJBSet"));
        suite.addTest(new ImportEJBTest("testImportEJB_02_Import_2_EJBSets"));
        suite.addTest(new ImportEJBTest("testImportEJB_03_Clear_Import_1_EJBSet"));
        return suite;
    }

    /** method called before each testcase
    */
    protected void setUp() {}

    public void testImportEJB_01_Import_1_EJBSet() {
        String ejbSetName_1 = "ValidJAR",
               ejbJarDir  = "EJB_Valid_JAR_Valid_EAR",
               ejbJarName = ejbJarDir + "Client.jar";
        EJBTestUtils.addEJB(ejbSetName_1, EJBTestUtils.RMI_IIOP_PORT_APPSERVER,
                EJBTestUtils.EJB_JARS_PATH + 
                File.separator + ejbJarDir + File.separator + ejbJarName, "");
        EJBTestUtils.endAddEJB();
        server.showNavigatorOperator().getTree().findPath(EJBTestUtils.ejbNode + "|" + ejbSetName_1);
        TestUtils.wait(1000);

        String[] ejbSetNames = new String[] {ejbSetName_1};
        String errMsg = EJBTestUtils.exportRequiredEJBSets(ejbSetNames);
        
        EJBTestUtils.removeEJB(ejbSetName_1);
        if (errMsg == null) {
            errMsg = EJBTestUtils.importEJBSets("", ejbSetNames, false, true);
        }
        Util.wait(1000);
        new QueueTool().waitEmpty();
        if (errMsg != null) {
            Assert.fail(errMsg);
        }
    }
  
    public void testImportEJB_02_Import_2_EJBSets() {
        String ejbSetName_1 = "ValidJAR",
               ejbJarDir  = "EJB_Valid_JAR_Valid_EAR",
               ejbJarName = ejbJarDir + "Client.jar";
        EJBTestUtils.addEJB(ejbSetName_1, EJBTestUtils.RMI_IIOP_PORT_APPSERVER,
                EJBTestUtils.EJB_JARS_PATH + 
                File.separator + ejbJarDir + File.separator + ejbJarName, "");
        EJBTestUtils.endAddEJB();
        server.showNavigatorOperator().getTree().findPath(EJBTestUtils.ejbNode + "|" + ejbSetName_1);
        TestUtils.wait(1000);

        String ejbSetName_2 = "Valid_JARs_2EJBs";
        ejbJarDir  = "EJB_Valid_JARs_2EJBs";
        ejbJarName = ejbJarDir + "Client.jar";
        
        EJBTestUtils.addEJB(ejbSetName_2, EJBTestUtils.RMI_IIOP_PORT_APPSERVER,
            EJBTestUtils.EJB_JARS_PATH + 
            File.separator + ejbJarDir + File.separator + ejbJarName, "");
        EJBTestUtils.endAddEJB();
        
        String[] ejbSetNames = new String[] {ejbSetName_1, ejbSetName_2};
        String errMsg = EJBTestUtils.exportRequiredEJBSets(ejbSetNames);
        
        EJBTestUtils.removeEJB(ejbSetName_1);
        EJBTestUtils.removeEJB(ejbSetName_2);
        if (errMsg == null) {
            errMsg = EJBTestUtils.importEJBSets("", ejbSetNames, false, true);
        }
        Util.wait(1000);
        new QueueTool().waitEmpty();
        if (errMsg != null) {
            Assert.fail(errMsg);
        }
    }
  
    public void testImportEJB_03_Clear_Import_1_EJBSet() {
        String ejbSetName_1 = "ValidJAR",
               ejbJarDir  = "EJB_Valid_JAR_Valid_EAR",
               ejbJarName = ejbJarDir + "Client.jar";
        EJBTestUtils.addEJB(ejbSetName_1, EJBTestUtils.RMI_IIOP_PORT_APPSERVER,
                EJBTestUtils.EJB_JARS_PATH + 
                File.separator + ejbJarDir + File.separator + ejbJarName, "");
        EJBTestUtils.endAddEJB();
        server.showNavigatorOperator().getTree().findPath(EJBTestUtils.ejbNode + "|" + ejbSetName_1);
        TestUtils.wait(1000);

        String ejbSetName_2 = "Valid_JARs_2EJBs";
        ejbJarDir  = "EJB_Valid_JARs_2EJBs";
        ejbJarName = ejbJarDir + "Client.jar";
        
        EJBTestUtils.addEJB(ejbSetName_2, EJBTestUtils.RMI_IIOP_PORT_APPSERVER,
            EJBTestUtils.EJB_JARS_PATH + 
            File.separator + ejbJarDir + File.separator + ejbJarName, "");
        EJBTestUtils.endAddEJB();
        
        String[] ejbSetNames = new String[] {ejbSetName_1, ejbSetName_2};
        String errMsg = EJBTestUtils.exportRequiredEJBSets(ejbSetNames);
        
        EJBTestUtils.removeEJB(ejbSetName_1);
        EJBTestUtils.removeEJB(ejbSetName_2);
        if (errMsg == null) {
            ejbSetNames = new String[] {ejbSetName_1};
            errMsg = EJBTestUtils.importEJBSets("", ejbSetNames, true, true);
        }
        Util.wait(1000);
        new QueueTool().waitEmpty();
        
        if (errMsg == null) {
            // the 2nd EJB Set's subnode should not be found
            server = ServerNavigatorOperator.showNavigatorOperator();
            Util.wait(1000);
            new QueueTool().waitEmpty();
            sntree = server.getTree();
            Util.wait(1000);
            try {
                sntree.findPath(EJBTestUtils.ejbNode + "|" + ejbSetName_2);
                errMsg = "EJB-subnode [" + ejbSetName_2 + "] has been found under tree node [" + EJBTestUtils.ejbNode + "]";
                EJBTestUtils.removeEJB(ejbSetName_2);
            } catch (TimeoutExpiredException tee) {
                TestUtils.outMsg("+++ EJB-subnode [" + ejbSetName_2 + 
                    "] is not found under tree node [" + EJBTestUtils.ejbNode + "]");
            }
        }
        if (errMsg != null) {
            Assert.fail(errMsg);
        }
    }
}
