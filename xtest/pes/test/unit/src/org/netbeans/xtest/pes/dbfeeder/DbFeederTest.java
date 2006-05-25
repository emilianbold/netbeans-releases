/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
        Thread.sleep(60000);
        
        //PESLogger.setConsoleLoggingLevel("FINEST");
        System.setProperty("pes.dbfeeder.config", configFile.getAbsolutePath());
        DbFeeder.main(null);
        
        // verify inserted data in database
        Connection connection = config.getDatabaseConnection();
        DbUtils dBUtils = new DbUtils(connection);
        try {
            Object xtestResultsReportId = dBUtils.queryFirst("XTestResultsReport", "id", "build", "200699999999");
            Object testbagId = dBUtils.queryFirst("Testbag", "id", "XTestResultsReport_id", xtestResultsReportId);
            Object testbagName = dBUtils.queryFirst("Testbag", "name", "id", testbagId);
            assertEquals("Testbag name differs.", "Out Of The Box Startup", testbagName);
            Object unitTestSuiteId = dBUtils.queryFirst("UnitTestSuite", "id", "Testbag_id", testbagId);
            Object unitTestSuiteName = dBUtils.queryFirst("UnitTestSuite", "name", "id", unitTestSuiteId);
            assertEquals("UnitTestSuite name differs.", "footprint.FootprintTests", unitTestSuiteName);
            Object unitTestCaseTblId = dBUtils.queryFirst("UnitTestCase_tbl", "id", "UnitTestSuite_id", unitTestSuiteId);
            Object unitTestCaseDefId = dBUtils.queryFirst("UnitTestCase_tbl", "UnitTestCase_def_id", "id", unitTestCaseTblId);
            Object unitTestCaseName = dBUtils.queryFirst("UnitTestCase_def", "name", "id", unitTestCaseDefId);
            assertEquals("UnitTestCase name differs.", "testOutOfTheBoxStartup", unitTestCaseName);
            Object performanceDataValue = dBUtils.queryFirst("PerformanceData", "value", "UnitTestSuite_id", unitTestSuiteId);
            assertEquals("PerformanceData value differs.", "81724", performanceDataValue.toString());
        } finally {
            System.out.println("FINALLY");
            // delete test data
            dBUtils.deleteFromTable("XTestResultsReport", "build = '200699999999'");
            connection.close();
        }
    }
}
