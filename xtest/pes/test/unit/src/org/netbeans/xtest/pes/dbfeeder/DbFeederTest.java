/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.xtest.pes.dbfeeder;

import java.io.File;
import java.sql.Connection;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.xtest.pes.dbfeeder.DbFeeder;
import org.netbeans.xtest.pes.dbfeeder.DbFeederConfig;
import org.netbeans.xtest.pes.dbfeeder.DbUtils;
import org.netbeans.xtest.util.FileUtils;

/**
 * Test upload data using DbFeeder.
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class DbFeederTest extends NbTestCase {

    public DbFeederTest(String testName) {
        super(testName);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite(DbFeederTest.class);
        return suite;
    }

    /** Upload prepared data to database. Connection properties are read from
     * config in data dir.
     */
    public void testDbFeeder() throws Exception {
        File configFile = new File(getDataDir(), "dbfeeder_config.xml");
        DbFeederConfig config = DbFeederConfig.loadCondfig(configFile);
        File incomingDir = config.getWorkDirs().getIncoming();
        // copy data to incoming dir
        File xmlData = new File(getDataDir(), "pes-beetle-060517-134225.xml");
        File zipData = new File(getDataDir(), "pes-beetle-060517-134225.zip");
        FileUtils.copyFileToDir(xmlData, incomingDir);
        FileUtils.copyFileToDir(zipData, incomingDir);
        // Need to sleep 60 s because DbFeeder checks that incoming reports are older that 
        // 60 s. It should prevent processing of partially copied files.
        System.out.println("#### Waiting 60 seconds... ####");
        Thread.sleep(60000);
        
        //PESLogger.setConsoleLoggingLevel("FINEST");
        System.setProperty("pes.dbfeeder.config", configFile.getAbsolutePath());
        DbFeeder.main(null);
        
        // verify inserted data in database
        Connection connection = config.getDatabaseConnection();
        DbUtils dBUtils = new DbUtils(connection);
        try {
            Object xtestResultsReportId = dBUtils.queryFirst("XTestResultsReport", "id", "build", "200699999999");
            System.out.println("xtestResultsReportId="+xtestResultsReportId);
            Object testbagId = dBUtils.queryFirst("Testbag", "id", "XTestResultsReport_id", xtestResultsReportId);
            Object testbagName = dBUtils.queryFirst("Testbag", "name", "id", testbagId);
            assertEquals("Testbag name differs.", "Out Of The Box Startup", testbagName);
            Object testSuiteId = dBUtils.queryFirst("TestSuite", "id", "Testbag_id", testbagId);
            Object testSuiteNameId = dBUtils.queryFirst("TestSuite", "TestSuiteName_id", "id", testSuiteId);
            Object testSuiteName = dBUtils.queryFirst("TestSuiteName", "name", "id", testSuiteNameId);
            assertEquals("TestSuite name differs.", "footprint.FootprintTests", testSuiteName);
            Object testCaseId = dBUtils.queryFirst("TestCase", "id", "TestSuite_id", testSuiteId);
            Object testCaseNameId = dBUtils.queryFirst("TestCase", "TestCaseName_id", "id", testCaseId);
            Object testCaseName = dBUtils.queryFirst("TestCaseName", "name", "id", testCaseNameId);
            assertEquals("TestCase name differs.", "testOutOfTheBoxStartup", testCaseName);
            Object performanceDataValue = dBUtils.queryFirst("PerformanceData", "value", "TestSuite_id", testSuiteId);
            assertEquals("PerformanceData value differs.", "81724", performanceDataValue.toString());
        } finally {
            System.out.println("FINALLY");
            // delete test data
            dBUtils.deleteFromTable("XTestResultsReport", "build = '200699999999'");
            connection.close();
        }
    }
}
