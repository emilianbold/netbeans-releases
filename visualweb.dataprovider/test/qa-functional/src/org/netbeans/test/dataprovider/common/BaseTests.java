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
package org.netbeans.test.dataprovider.common;

import java.util.*;
import java.io.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.*;
import org.netbeans.junit.*;
import org.netbeans.modules.visualweb.gravy.*;
import org.netbeans.jemmy.*;
import java.lang.reflect.*;

public abstract class BaseTests extends RaveTestCase implements Constants {
    protected static final String STOP_TEST_BECAUSE_PREVIOUS_TEST_FAILS = 
        "Test is not performed because a previous test has been failed";
    protected static String errMsg;
    protected static int webResponseCode;
    protected static boolean isTestSuiteRunning = false, isProjectCreated;

    protected static String[] TEST_METHOD_ARRAY = {
        "testAcceptance_01",
        "testAcceptance_02",
        "testAcceptance_03",

        "testAcceptance_00"
    };
    
    public BaseTests(String testName) {
        super(testName);
    }

    protected static <T extends BaseTests> Test suite(Class<T> classTestSet, 
        String nameTestSuite) {
        Utils.openLog();
        TestPropertiesHandler.readTestProperties();
        List<String> testMethodsNames = TestPropertiesHandler.getTestMethodNames(TEST_METHOD_ARRAY);
        
        TestSuite suite = new TestSuite(nameTestSuite);
        for (String testMethodName : testMethodsNames) {
            //suite.addTest(new BaseTests(testMethodName));
            try {
                Constructor<T> constructor = classTestSet.getConstructor(String.class);
                suite.addTest(constructor.newInstance(testMethodName));
            } catch(Exception e) {
                e.printStackTrace(Utils.logStream);
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return suite;
    }

    public void testAcceptance_01() { // checkDBConnection
        Utils.logMsg("=== testAcceptance_01() ===");
        errMsg = null;
        webResponseCode = -1;
        isProjectCreated = false;
        if (! isTestSuiteRunning) {
            isTestSuiteRunning = true;
        } else {
            TestPropertiesHandler.nextTestProperties();
        }
 
        if (errMsg != null) fail(STOP_TEST_BECAUSE_PREVIOUS_TEST_FAILS);
                
        Util.wait(1000);
        
        errMsg = new DatabaseTests().checkDBConnection();
        Util.wait(1000);
        new QueueTool().waitEmpty();
        if (errMsg != null) {
            fail(errMsg);
        }
    }

    public void testAcceptance_02() { // checkAppServer
        Utils.logMsg("=== testAcceptance_02() ===");
        if (errMsg != null) fail(STOP_TEST_BECAUSE_PREVIOUS_TEST_FAILS);
        
        //JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
        Util.wait(1000);
        errMsg = new AppServerTests().checkAppServer();
        Util.wait(1000);
        new QueueTool().waitEmpty();
        if (errMsg != null) {
            fail(errMsg);
        }
    }

    public void testAcceptance_03() { // createNewProject
        Utils.logMsg("=== testAcceptance_03() ===");
        if (errMsg != null) fail(STOP_TEST_BECAUSE_PREVIOUS_TEST_FAILS);

        //JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
        Util.wait(1000);
        errMsg = new ProjectTests().createNewProject();
        Util.wait(1000);
        new QueueTool().waitEmpty();
        if (errMsg != null) {
            fail(errMsg);
        }
        isProjectCreated = true;
    }
    
    public void testAcceptance_00() { // undeploy, close project
        Utils.logMsg("=== testAcceptance_00() ===");
        Util.wait(1000);
        Utils.logMsg(""); // don't remove this statement
        errMsg = null;
        if (webResponseCode == WEB_RESPONSE_CODE_OK) {
            errMsg = new ProjectTests().undeployCurrentProject();
        } else {
            Utils.logMsg("+++ Project undeployment is skipped because this project wasn't deployed properly");
        }
        if (isProjectCreated) {
            String errText = new ProjectTests().closeCurrentProject();
            if (errText != null) {
                errMsg = (errMsg == null ? errText : errMsg + " " + errText);
            }
        }
        Util.wait(1000);
        new QueueTool().waitEmpty();
        if (errMsg != null) {
            fail(errMsg);
        }
    }

    public void writeRefData(Object... dataArray) {
        writeRefData(true, dataArray);
    }
    public void writeRefData(boolean needChangeSpaces, Object... dataArray) {
        for (Object obj : dataArray) {
            String s = (needChangeSpaces ? 
                changeExtraWhiteSpaces(obj.toString()) : obj.toString());
            ref(s);
        }
    }
    public static String changeExtraWhiteSpaces(String s){
        return s.replaceAll("\t"," ").replaceAll("\n"," ").replaceAll(
               "\r"," ").replaceAll("  *"," ").trim();
    }
   
    @Override
    protected void setUp() throws Exception {
        //Utils.logStream = getLog();
    }
    @Override
    protected void tearDown() throws Exception {}
}
