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
package org.netbeans.test.dataprovider.cachedrowsetdataprovider;

import java.util.*;
import java.io.*;
import junit.framework.Test;
import junit.framework.*;
import org.netbeans.junit.*;
import org.netbeans.modules.visualweb.gravy.*;
import org.netbeans.jemmy.*;
import org.netbeans.test.dataprovider.common.*;

public class CachedRowSetDataProvider_AcceptanceTests extends BaseTests {
    static {
        TEST_METHOD_ARRAY = new String[] {
            "testAcceptance_01",
            "testAcceptance_02",
            "testAcceptance_03",
            "testAcceptance_04",
            "testAcceptance_05",
            "testAcceptance_06",
            "testAcceptance_07",
            "testAcceptance_08",
            "testAcceptance_09",

            "testAcceptance_00"
        };
    }
    
    public CachedRowSetDataProvider_AcceptanceTests(String testName) {
        super(testName);
    }

    public static Test suite() {
        return suite(CachedRowSetDataProvider_AcceptanceTests.class,
            "CachedRowSet DataProvider Acceptance Tests");
    }
    
    public void testAcceptance_04() { // bind Drop Dow List to DB table
        Utils.logMsg("=== testAcceptance_04() ===");
        if (errMsg != null) fail(STOP_TEST_BECAUSE_PREVIOUS_TEST_FAILS);

        //JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
        Util.wait(1000);
        errMsg = new PersonDropDownList().makePersonDropDownList();
        Util.wait(1000);
        new QueueTool().waitEmpty();
        if (errMsg != null) {
            fail(errMsg);
        }
    }
    
    public void testAcceptance_05() { // bind Table to DB table
        Utils.logMsg("=== testAcceptance_05() ===");
        if (errMsg != null) fail(STOP_TEST_BECAUSE_PREVIOUS_TEST_FAILS);

        //JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
        Util.wait(1000);
        TripTable tripTable = new TripTable();
        errMsg = tripTable.makeTripTable();
        Util.wait(500);
        new QueueTool().waitEmpty();
        if (errMsg != null) {
            fail(errMsg);
        }
    }
    
    public void testAcceptance_06() { // modify java code for Drop Down List event handler and method prerender()
        Utils.logMsg("=== testAcceptance_06() ===");
        if (errMsg != null) fail(STOP_TEST_BECAUSE_PREVIOUS_TEST_FAILS);

        //JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
        Util.wait(1000);
        String componentID = TestPropertiesHandler.getTestProperty(
            "ID_DropDownList_For_DBTablePerson");
        errMsg = new PersonDropDownList(componentID).modifyJavaCode();
        Util.wait(1000);
        new QueueTool().waitEmpty();
        if (errMsg != null) {
            fail(errMsg);
        }
    }
    
    /**
    Test case for checking the bug 
    http://www.netbeans.org/issues/show_bug.cgi?id=119529
    */
    public void testAcceptance_07() { // bind Text Field to DB table
        Utils.logMsg("=== testAcceptance_07() ===");
        if (errMsg != null) fail(STOP_TEST_BECAUSE_PREVIOUS_TEST_FAILS);

        //JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
        Util.wait(1000);
        TextFieldPageBeanDataProvider textField = new TextFieldPageBeanDataProvider();
        errMsg = textField.makeTextField();
        Util.wait(500);
        new QueueTool().waitEmpty();
        if (errMsg != null) {
            fail(errMsg);
        }
    }
    
    /**
    Test case for checking the bug 
    http://www.netbeans.org/issues/show_bug.cgi?id=119919
    */
    public void testAcceptance_08() { // bind Text Field to data provider from SessionBean
        Utils.logMsg("=== testAcceptance_08() ===");
        if (errMsg != null) fail(STOP_TEST_BECAUSE_PREVIOUS_TEST_FAILS);

        //JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
        Util.wait(1000);
        TextFieldSessionBeanDataProvider textField = new TextFieldSessionBeanDataProvider();
        errMsg = textField.makeTextField();
        Util.wait(500);
        new QueueTool().waitEmpty();
        if (errMsg != null) {
            fail(errMsg);
        }
    }
    
    public void testAcceptance_09() { // run a project and check a working of a web-application
        Utils.logMsg("=== testAcceptance_09() ===");
        if (errMsg != null) fail(STOP_TEST_BECAUSE_PREVIOUS_TEST_FAILS);

        //JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
        Util.wait(1000);
        RunProject runProjectInstance = new RunProject();
        errMsg = runProjectInstance.runProject();
        webResponseCode = runProjectInstance.getWebResponseCode();
        Util.wait(1000);
        new QueueTool().waitEmpty();
        if (errMsg != null) {
            fail(errMsg);
        }
    }
}
