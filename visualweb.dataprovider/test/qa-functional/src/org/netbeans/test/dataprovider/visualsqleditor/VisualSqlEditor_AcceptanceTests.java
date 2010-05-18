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
package org.netbeans.test.dataprovider.visualsqleditor;

import java.util.*;
import java.io.*;
import junit.framework.Test;
import junit.framework.*;
import org.netbeans.junit.*;
import org.netbeans.modules.visualweb.gravy.*;
import org.netbeans.jemmy.*;
import org.netbeans.test.dataprovider.common.*;

public class VisualSqlEditor_AcceptanceTests extends BaseTests {

    static {
        TEST_METHOD_ARRAY = new String[] {
            "testAcceptance_01", // check DB connection
            "testAcceptance_04",
            "testAcceptance_05",
            "testAcceptance_06",
            "testAcceptance_07",
            "testAcceptance_08",
            "testAcceptance_09",
 
            "testAcceptance_02", // check & start application server
            "testAcceptance_03", // create a new VW project
            "testAcceptance_10",
            "testAcceptance_11",
            
            "testAcceptance_00"
        };
    }
    
    public VisualSqlEditor_AcceptanceTests(String testName) {
        super(testName);
    }

    public static Test suite() {
        return suite(VisualSqlEditor_AcceptanceTests.class, 
            "Visual Sql Editor Acceptance Tests");
    }
    
    public void testAcceptance_04() { // add several DB tables to Query Editor
        Utils.logMsg("=== testAcceptance_04() ===");
        if (errMsg != null) fail(STOP_TEST_BECAUSE_PREVIOUS_TEST_FAILS);

        Util.wait(1000);
        try {
            new AcceptanceTests(this).checkQueryEditor_AddDBTables();
        } catch(Throwable t) {
            t.printStackTrace(Utils.logStream);
            errMsg = t.getMessage();
        }
        Util.wait(1000);
        new QueueTool().waitEmpty();
        if (errMsg != null) {
            fail(errMsg);
        }
    }
    
    public void testAcceptance_05() { // remove DB tables from Query Editor
        Utils.logMsg("=== testAcceptance_05() ===");
        if (errMsg != null) fail(STOP_TEST_BECAUSE_PREVIOUS_TEST_FAILS);

        Util.wait(1000);
        try {
            new AcceptanceTests(this).checkQueryEditor_RemoveDBTables();
        } catch(Throwable t) {
            t.printStackTrace(Utils.logStream);
            errMsg = t.getMessage();
        }
        Util.wait(1000);
        new QueueTool().waitEmpty();
        if (errMsg != null) {
            fail(errMsg);
        }
    }
    
    public void testAcceptance_06() { // parse SQL queries in Query Editor
        Utils.logMsg("=== testAcceptance_06() ===");
        if (errMsg != null) fail(STOP_TEST_BECAUSE_PREVIOUS_TEST_FAILS);

        Util.wait(1000);
        try {
            new AcceptanceTests(this).checkQueryEditor_ParseSQLQuery();
        } catch(Throwable t) {
            t.printStackTrace(Utils.logStream);
            errMsg = t.getMessage();
        }
        Util.wait(1000);
        new QueueTool().waitEmpty();
        if (errMsg != null) {
            fail(errMsg);
        }
    }
    
    public void testAcceptance_07() { // run SQL query with in Query Editor
        Utils.logMsg("=== testAcceptance_07() ===");
        errMsg = null;
        if (errMsg != null) fail(STOP_TEST_BECAUSE_PREVIOUS_TEST_FAILS);

        Util.wait(1000);
        try {
            new AcceptanceTests(this).checkQueryEditor_RunSQLQuery();
        } catch(Throwable t) {
            t.printStackTrace(Utils.logStream);
            errMsg = t.getMessage();
        }
        Util.wait(1000);
        new QueueTool().waitEmpty();
        if (errMsg != null) {
            fail(errMsg);
        }
    }
    
    public void testAcceptance_08() { // run SQL query with ORDER clause in Query Editor
        Utils.logMsg("=== testAcceptance_08() ===");
        if (errMsg != null) fail(STOP_TEST_BECAUSE_PREVIOUS_TEST_FAILS);

        Util.wait(1000);
        try {
            new AcceptanceTests(this).checkQueryEditor_RunOrderedSQLQuery();
        } catch(Throwable t) {
            t.printStackTrace(Utils.logStream);
            errMsg = t.getMessage();
        }
        Util.wait(1000);
        new QueueTool().waitEmpty();
        if (errMsg != null) {
            fail(errMsg);
        }
    }
    
    public void testAcceptance_09() { // run SQL query with criteria in Query Editor
        Utils.logMsg("=== testAcceptance_09() ===");
        if (errMsg != null) fail(STOP_TEST_BECAUSE_PREVIOUS_TEST_FAILS);

        Util.wait(1000);
        try {
            new AcceptanceTests(this).checkQueryEditor_RunSQLQueryWithCriteria();
        } catch(Throwable t) {
            t.printStackTrace(Utils.logStream);
            errMsg = t.getMessage();
        }
        Util.wait(1000);
        new QueueTool().waitEmpty();
        if (errMsg != null) {
            fail(errMsg);
        }
    }
    
    public void testAcceptance_10() { // open Query Editor for CachedRowSet
        Utils.logMsg("=== testAcceptance_10() ===");
        if (errMsg != null) fail(STOP_TEST_BECAUSE_PREVIOUS_TEST_FAILS);

        Util.wait(1000);
        try {
            new AcceptanceTests(this).checkQueryEditor_QueryEditorForCachedRowSet();
        } catch(Throwable t) {
            t.printStackTrace(Utils.logStream);
            errMsg = t.getMessage();
        }
        Util.wait(1000);
        new QueueTool().waitEmpty();
        if (errMsg != null) {
            fail(errMsg);
        }
    }
    
    public void testAcceptance_11() { // modify SQL query and reopen Query Editor for CachedRowSet
        Utils.logMsg("=== testAcceptance_11() ===");
        if (errMsg != null) fail(STOP_TEST_BECAUSE_PREVIOUS_TEST_FAILS);

        Util.wait(1000);
        try {
            new AcceptanceTests(this).checkQueryEditor_ModifyQueryForCachedRowSet();
        } catch(Throwable t) {
            t.printStackTrace(Utils.logStream);
            errMsg = t.getMessage();
        }
        Util.wait(1000);
        new QueueTool().waitEmpty();
        if (errMsg != null) {
            fail(errMsg);
        }
    }
}
