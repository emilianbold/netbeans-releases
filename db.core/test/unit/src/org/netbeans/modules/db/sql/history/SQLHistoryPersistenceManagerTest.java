/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.sql.history;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.db.sql.history.SQLHistory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 * A Test based on NbTestCase. It is a NetBeans extension to JUnit TestCase
 * which among othres allows to compare files via assertFile methods, create
 * working directories for testcases, write to log files, compare log files
 * against reference (golden) files, etc.
 * 
 * More details here http://xtest.netbeans.org/NbJUnit/NbJUnit-overview.html.
 * 
 * @author John Baker
 */
public class SQLHistoryPersistenceManagerTest extends NbTestCase {

    /** Default constructor.
     * @param testName name of particular test case
     */
    public SQLHistoryPersistenceManagerTest(String testName) {
        super(testName);
    }

    /** Creates suite from particular test cases. You can define order of testcases here. */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new SQLHistoryPersistenceManagerTest("testExecuteStatements"));
        suite.addTest(new SQLHistoryPersistenceManagerTest("testMultipleExecutions"));
        return suite;
    }

    /* Method allowing test execution directly from the IDE. */
    public static void main(java.lang.String[] args) {
        // run whole suite
        junit.textui.TestRunner.run(suite());
    // run only selected test case
    //junit.textui.TestRunner.run(new SQLHistoryPersistentManagerTest("test1"));
    }

    /** Called before every test case. */
    public void setUp() {
        System.out.println("########  " + getName() + "  #######");
    }

    /** Called after every test case. */
    public void tearDown() {
        try {
            clearWorkDir();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /** Test testExecuteStatements passes if no exceptions occur. */
    public void testExecuteStatements() {
        try {
            List<SQLHistory> sqlHistoryList = new ArrayList<SQLHistory>();
            sqlHistoryList.add(new SQLHistory("jdbc:// mysql", "select * from TRAVEL.PERSON", DateFormat.getInstance().parse("07/10/96 4:5 PM, PDT")));
            FileObject fo = FileUtil.toFileObject(getWorkDir());
            sqlHistoryList.add(new SQLHistory("jdbc:// oracle", "select * from PERSON", DateFormat.getInstance().parse("07/10/96 4:5 PM, PDT")));
            SQLHistoryPersistenceManager.getInstance().create(fo, sqlHistoryList);
        } catch (SQLHistoryException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        } catch (ParseException pe) {
            Exceptions.printStackTrace(pe);
        }
    }
    
    
    /** Test testMultipleExecutions passes if no exceptions occur. */
    public void testMultipleExecutions() {
        try {
            List<SQLHistory> sqlHistoryList = new ArrayList<SQLHistory>();
            sqlHistoryList.add(new SQLHistory("jdbc:// derby", "select * from TRAVEL.TRIP", DateFormat.getInstance().parse("07/10/96 4:5 PM, PDT")));
            FileObject fo = FileUtil.toFileObject(getWorkDir());
            SQLHistoryPersistenceManager.getInstance().create(fo, sqlHistoryList);
            sqlHistoryList.add(new SQLHistory("jdbc:// postgres", "select * from TRAVEL.TRIP", DateFormat.getInstance().parse("07/10/96 4:5 PM, PDT")));
            fo = FileUtil.toFileObject(getWorkDir());
            SQLHistoryPersistenceManager.getInstance().create(fo, sqlHistoryList);
        } catch (SQLHistoryException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        } catch (ParseException pe) {
            Exceptions.printStackTrace(pe);
        }
    }
}