/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.xtest.pes.dbfeeder;

import java.io.File;
import java.sql.Connection;
import java.sql.Timestamp;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.xtest.pe.xmlbeans.PerformanceData;
import org.netbeans.xtest.pe.xmlbeans.SystemInfo;
import org.netbeans.xtest.pe.xmlbeans.TestBag;
import org.netbeans.xtest.pe.xmlbeans.TestRun;
import org.netbeans.xtest.pe.xmlbeans.UnitTestCase;
import org.netbeans.xtest.pe.xmlbeans.UnitTestSuite;
import org.netbeans.xtest.pe.xmlbeans.XTestResultsReport;
import org.netbeans.xtest.pes.PESLogger;
import org.netbeans.xtest.pes.dbfeeder.DbFeederConfig;
import org.netbeans.xtest.pes.dbfeeder.DbUtils;

/** Test of org.netbeans.xtest.pes.dbfeeder.DbStorage.
 *
 * @author Jiri Skrivanek
 */
public class DbStorageTest extends NbTestCase {
    
    /** Creates a new instance of DbStorageTest */
    public DbStorageTest(String testName) {
        super(testName);
    }
    
    /** Executes entire suite. */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /** Creates suite. */
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new DbStorageTest("testStoreSystemInfo"));
        suite.addTest(new DbStorageTest("testStorePerformanceData"));
        suite.addTest(new DbStorageTest("testStoreEmptyString"));
        suite.addTest(new DbStorageTest("testStoreUnitTestCase"));
        suite.addTest(new DbStorageTest("testStoreUnitTestSuite"));
        suite.addTest(new DbStorageTest("testStoreTestBag"));
        suite.addTest(new DbStorageTest("testStoreXTestResultsReport"));
        return suite;
    }

    private Connection connection;
    private DbUtils dBUtils;
    private DbStorage dbs;
    
    /** Setup before test case. */
    public void setUp() throws Exception {
        System.out.println("########  "+getName()+"  #######");
        // init database connection
        File configFile = new File(getDataDir(), "dbfeeder_config.xml");
        DbFeederConfig config = DbFeederConfig.loadCondfig(configFile);
        
        PESLogger.setConsoleLoggingLevel("FINEST");
        System.setProperty("pes.dbfeeder.config", configFile.getAbsolutePath());
        
        connection = config.getDatabaseConnection();
        dBUtils = new DbUtils(connection);
        dbs = new DbStorage(connection);
    }
    
    /** Test storeSystemInfo method.
     * - create SystemInfo
     * - insert SystemInfo to database
     * - try to insert the same SystemInfo into database
     * - check duplicate SystemInfo is not inserted
     * - check SystemInfo with correct values is inserted
     * - delete inserted SystemInfo
     */
    public void testStoreSystemInfo() throws Exception {
        SystemInfo info = new SystemInfo();
        //System.out.println("SystemInfo="+info.getHost()+" "+info.getJavaVersion()+" "+info.getOsArch()+" "+info.getOsName()+" "+info.getOsVersion());
        info.setHost("DUMMYHOST");

        try {
            long systemInfoId = dbs.storeSystemInfo(info);
            connection.commit();
            long systemInfoId2 = dbs.storeSystemInfo(info);
            assertEquals("DbStorage.storeSystemInfo should not insert duplicate SystemInfo", systemInfoId, systemInfoId2);
            Object javaVersion = dBUtils.queryFirst("SystemInfo", "JAVAVERSION", "HOST", "DUMMYHOST");
            assertEquals("JAVAVERSION column differs.", info.getJavaVersion(), javaVersion);
        } finally {
            // delete test data
            dBUtils.deleteFromTable("SystemInfo", "host = 'DUMMYHOST'");
            connection.close();
        }
    }
    
    /** Test storePerformanceData method.
     * - create PerformanceData
     * - insert PerformanceData to database
     * - check PerformanceData with correct values is inserted
     * - delete inserted PerformanceData
     */
    public void testStorePerformanceData() throws Exception {
        PerformanceData performanceData = new PerformanceData();
        performanceData.setName("DUMMYPERFORMANCEDATA");
        performanceData.setRunOrder(1);
        performanceData.setThreshold(10);
        performanceData.setUnit("ms");
        performanceData.setValue(100);

        Object perfDataNameId = null;
        try {
            long testSuiteId = ((Number)dBUtils.getFirst("TESTSUITE", "ID")).longValue();
            dbs.storePerformanceData(performanceData, testSuiteId);
            connection.commit();
            perfDataNameId = dBUtils.queryFirst("PERFDATANAME", "ID", "NAME", "DUMMYPERFORMANCEDATA");
            assertNotNull("PerformanceData not inserted into PERFDATANAME", perfDataNameId);
            Object perfDataNameUnit = dBUtils.queryFirst("PERFDATANAME", "UNIT", "NAME", "DUMMYPERFORMANCEDATA");
            assertEquals("PERFDATANAME.UNIT has wrong value.", performanceData.getUnit(), perfDataNameUnit);
            int perfDataNameRunOrder = ((Number)dBUtils.queryFirst("PERFDATANAME", "RUNORDER", "NAME", "DUMMYPERFORMANCEDATA")).intValue();
            assertEquals("PERFDATANAME.RUNORDER has wrong value.", performanceData.getRunOrder(), perfDataNameRunOrder);
            int perfDataNameThreshold = ((Number)dBUtils.queryFirst("PERFDATANAME", "THRESHOLD", "NAME", "DUMMYPERFORMANCEDATA")).intValue();
            assertEquals("PERFDATANAME.THRESHOLD has wrong value.", performanceData.getThreshold(), perfDataNameThreshold);
            long performanceDataValue = ((Number)dBUtils.queryFirst("PERFORMANCEDATA", "VALUE", "PERFDATANAME_ID", perfDataNameId)).longValue();
            assertEquals("PERFORMANCEDATA.VALUE has wrong value.", performanceData.getValue(), performanceDataValue);
        } finally {
            // delete test data
            dBUtils.deleteFromTable("PERFORMANCEDATA", "PERFDATANAME_ID = '"+perfDataNameId+"'");
            dBUtils.deleteFromTable("PERFDATANAME", "ID = '"+perfDataNameId+"'");
            connection.close();
        }
    }

    /** Test that we correctly handle empty strings (look at DbUtils.insertAutoIncrement()).
     * The problem is that statement.setObject(i, "") inserts null into Oracle database. Then we
     * cannot find it when searching for empty string. We need to prevent such
     * inconsistency and that's why we set null value for empty strings.
     * - create PerformanceData with empty string for unit
     * - insert PerformanceData to database
     * - check PerformanceData with correct values is inserted
     * - delete inserted PerformanceData
     */
    public void testStoreEmptyString() throws Exception {
        PerformanceData performanceData = new PerformanceData();
        performanceData.setName("DUMMYPERFORMANCEDATA");
        performanceData.setRunOrder(1);
        performanceData.setThreshold(10);
        performanceData.setUnit("");
        performanceData.setValue(100);

        Object perfDataNameId = null;
        try {
            long testSuiteId = ((Number)dBUtils.getFirst("TESTSUITE", "ID")).longValue();
            dbs.storePerformanceData(performanceData, testSuiteId);
            connection.commit();
            perfDataNameId = dBUtils.queryFirst("PERFDATANAME", "ID", "NAME", "DUMMYPERFORMANCEDATA");
            assertNotNull("PerformanceData not inserted into PERFDATANAME", perfDataNameId);
            Object perfDataNameUnit = dBUtils.queryFirst("PERFDATANAME", "UNIT", "NAME", "DUMMYPERFORMANCEDATA");
            assertNull("PERFDATANAME.UNIT should be null.", perfDataNameUnit);
            int perfDataNameRunOrder = ((Number)dBUtils.queryFirst("PERFDATANAME", "RUNORDER", "NAME", "DUMMYPERFORMANCEDATA")).intValue();
            assertEquals("PERFDATANAME.RUNORDER has wrong value.", performanceData.getRunOrder(), perfDataNameRunOrder);
            int perfDataNameThreshold = ((Number)dBUtils.queryFirst("PERFDATANAME", "THRESHOLD", "NAME", "DUMMYPERFORMANCEDATA")).intValue();
            assertEquals("PERFDATANAME.THRESHOLD has wrong value.", performanceData.getThreshold(), perfDataNameThreshold);
            long performanceDataValue = ((Number)dBUtils.queryFirst("PERFORMANCEDATA", "VALUE", "PERFDATANAME_ID", perfDataNameId)).longValue();
            assertEquals("PERFORMANCEDATA.VALUE has wrong value.", performanceData.getValue(), performanceDataValue);
        } finally {
            // delete test data
            dBUtils.deleteFromTable("PERFORMANCEDATA", "PERFDATANAME_ID = '"+perfDataNameId+"'");
            dBUtils.deleteFromTable("PERFDATANAME", "ID = '"+perfDataNameId+"'");
            connection.close();
        }
    }

    /** Test storeUnitTestCase method.
     * - create UnitTestCase bean
     * - insert UnitTestCase to database
     * - check UnitTestCase inserted into table TESTCASERESULT, TESTCASENAME, TESTCASE
     * - delete inserted UnitTestCase
     */
    public void testStoreUnitTestCase() throws Exception {
        UnitTestCase testCase = new UnitTestCase();
        testCase.setName("DUMMYNAME");
        testCase.setClassName("DUMMYCLASSNAME");
        testCase.setResult("DUMMYRESULT");
        testCase.setMessage("DUMMYMESSAGE");
        testCase.setTime(100);
        testCase.setFailReason("DUMMYFAILREASON");
        testCase.setStackTrace("DUMMYSTACKTRACE");
                
        Object testCaseNameId = null;
        Object testCaseResultId = null;
        try {
            long testSuiteId = ((Number)dBUtils.getFirst("TESTSUITE", "ID")).longValue();
            dbs.storeUnitTestCase(testCase, testSuiteId);
            connection.commit();
            // check TESTCASENAME table
            testCaseNameId = dBUtils.queryFirst("TESTCASENAME", "ID", "NAME", "DUMMYNAME");
            assertNotNull("Not inserted into TESTCASENAME", testCaseNameId);
            Object testCaseNameClassname = dBUtils.queryFirst("TESTCASENAME", "CLASSNAME", "NAME", "DUMMYNAME");
            assertEquals("TESTCASENAME.CLASSNAME has wrong value.", testCase.getClassName(), testCaseNameClassname);
            // check TESTCASERESULT table
            testCaseResultId = dBUtils.queryFirst("TESTCASERESULT", "ID", "RESULT", "DUMMYRESULT");
            assertNotNull("Not inserted into TESTCASERESULT", testCaseResultId);
            // check TESTCASE table
            Object testCaseMessage = dBUtils.queryFirst("TESTCASE", "MESSAGE", "TESTCASENAME_ID", testCaseNameId);
            assertEquals("TESTCASE.MESSAGE has wrong value", testCase.getMessage(), testCaseMessage);
            Object testCaseExecTime = dBUtils.queryFirst("TESTCASE", "EXECTIME", "TESTCASENAME_ID", testCaseNameId);
            assertEquals("TESTCASE.EXECTIME has wrong value", testCase.getTime(), ((Number)testCaseExecTime).longValue());
            Object testCaseFailReason = dBUtils.queryFirst("TESTCASE", "FAILREASON", "TESTCASENAME_ID", testCaseNameId);
            assertEquals("TESTCASE.FAILREASON has wrong value", testCase.getFailReason(), testCaseFailReason);
            Object testCaseStacktrace = dBUtils.queryFirst("TESTCASE", "STACKTRACE", "TESTCASENAME_ID", testCaseNameId);
            assertEquals("TESTCASE.STACKTRACE has wrong value", testCase.getStackTrace(), testCaseStacktrace);
        } finally {
            // delete test data
            dBUtils.deleteFromTable("TESTCASE", "TESTCASENAME_ID = '"+testCaseNameId+"'");
            dBUtils.deleteFromTable("TESTCASENAME", "ID = '"+testCaseNameId+"'");
            dBUtils.deleteFromTable("TESTCASERESULT", "ID = '"+testCaseResultId+"'");
            connection.close();
        }
    }

    /** Test storeUnitTestSuite method.
     * - create UnitTestSuite bean
     * - insert UnitTestSuite to database
     * - check UnitTestSuite inserted into table TESTSUITENAME, TESTSUITE
     * - delete inserted UnitTestSuite
     */
    public void testStoreUnitTestSuite() throws Exception {
        UnitTestSuite testSuite = new UnitTestSuite();
        testSuite.setName("DUMMYNAME");
        // other are not stored into database
        testSuite.setTestsError(111);
        testSuite.setTestsExpectedFail(222);
        testSuite.setTestsFail(333);
        testSuite.setTestsPass(444);
        testSuite.setTestsTotal(555);
        testSuite.setTestsUnexpectedPass(666);
        testSuite.setTime(777);
        testSuite.setTimeStamp(new Timestamp(System.currentTimeMillis()));
        testSuite.setUnexpectedFailure("DUMMYUNEXPECTEDFAILURE");
                
        Object testSuiteNameId = null;
        try {
            long testBagId = ((Number)dBUtils.getFirst("TESTBAG", "ID")).longValue();
            dbs.storeUnitTestSuite(testSuite, testBagId);
            connection.commit();
            // check TESTSUITENAME table
            testSuiteNameId = dBUtils.queryFirst("TESTSUITENAME", "ID", "NAME", "DUMMYNAME");
            assertNotNull("Not inserted into TESTSUITENAME", testSuiteNameId);
            // check TESTSUITE table
            Object testSuiteId = dBUtils.queryFirst("TESTSUITE", "ID", "TESTSUITENAME_ID", testSuiteNameId);
            assertNotNull("Not inserted into TESTSUITE table.", testSuiteId);
        } finally {
            // delete test data
            dBUtils.deleteFromTable("TESTSUITE", "TESTSUITENAME_ID = '"+testSuiteNameId+"'");
            dBUtils.deleteFromTable("TESTSUITENAME", "ID = '"+testSuiteNameId+"'");
            connection.close();
        }
    }

    /** Test storeUnitTestBag method.
     * - create TestBag bean
     * - insert TestBag to database
     * - check TestBag inserted into table TESTBAG
     * - delete inserted TestBag
     */
    public void testStoreTestBag() throws Exception {
        TestBag testBag = new TestBag();
        testBag.setBagID("DUMMYBAGID");
        testBag.setName("DUMMYNAME");
        testBag.setModule("DUMMYMODULE");
        testBag.setTestType("DUMMYTESTTYPE");
        testBag.setTestAttribs("DUMMYTESTATTRIBS");
        testBag.setTime(777);
        // other are not stored into database
        testBag.setExecutor("DUMMYEXECUTOR");
        testBag.setRunID("DUMMYRUNID");
        testBag.setTestsError(111);
        testBag.setTestsExpectedFail(222);
        testBag.setTestsFail(333);
        testBag.setTestsPass(444);
        testBag.setTestsTotal(555);
        testBag.setTestsUnexpectedPass(666);
        testBag.setTimeStamp(new Timestamp(System.currentTimeMillis()));
        testBag.setUnexpectedFailure("DUMMYUNEXPECTEDFAILURE");
        
        Object testBagId = null;
        try {
            long xtestResultsReportId = ((Number)dBUtils.getFirst("XTESTRESULTSREPORT", "ID")).longValue();
            dbs.storeTestBag(testBag, xtestResultsReportId, null);
            connection.commit();
            // check TESTBAG table
            testBagId = dBUtils.queryFirst("TESTBAG", "ID", "NAME", "DUMMYNAME");
            assertNotNull("Not inserted into TESTBAG", testBagId);
            Object value = dBUtils.queryFirst("TESTBAG", "BAGID", "ID", testBagId);
            assertEquals("TESTBAG.BAGID has wrong value", testBag.getBagID(), value);
            value = dBUtils.queryFirst("TESTBAG", "MODULE", "ID", testBagId);
            assertEquals("TESTBAG.MODULE has wrong value", testBag.getModule(), value);
            value = dBUtils.queryFirst("TESTBAG", "TESTTYPE", "ID", testBagId);
            assertEquals("TESTBAG.TESTTYPE has wrong value", testBag.getTestType(), value);
            value = dBUtils.queryFirst("TESTBAG", "TESTATTRIBS", "ID", testBagId);
            assertEquals("TESTBAG.TESTATTRIBS has wrong value", testBag.getTestAttribs(), value);
            value = dBUtils.queryFirst("TESTBAG", "TIME", "ID", testBagId);
            assertEquals("TESTBAG.TIME has wrong value", testBag.getTime(), ((Number)value).longValue());
        } finally {
            // delete test data
            dBUtils.deleteFromTable("TESTBAG", "ID = '"+testBagId+"'");
            connection.close();
        }
    }
    
    /** Test storeXTestResultsReport method.
     * - create XTestResultsReport bean
     * - insert XTestResultsReport to database
     * - check XTestResultsReport inserted into table XTESTRESULTSREPORT and TESTEDTYPE
     * - delete inserted XTESTRESULTSREPORT
     */
    public void testStoreXTestResultsReport() throws Exception {
        XTestResultsReport xtr = new XTestResultsReport();
        xtr.setBuild("DUMMYBUILD");
        //xtr.setProject_id("DUMMYPROJECTID");
        //xtr.setSystemInfo_id(0);
        xtr.setTestedType("DUMMYTESTEDTYPE");
        xtr.setTime(777);
        xtr.setWebLink("DUMMYWEBLINK");
        xtr.setTimeStamp(new Timestamp(System.currentTimeMillis()));
        // other are not stored into database
        xtr.setBrokenModules("DUMMYBROKENMODULES");
        xtr.setHost("DUMMYHOST");
        xtr.setInvalidMessage("DUMMYINVALIDMESSAGE");
        xtr.setProject("DUMMYPROJECT");
        xtr.setTeam("DUMMYTEAM");
        xtr.setTestingGroup("DUMMYTESTINGGROUP");
        xtr.setTestsError(111);
        xtr.setTestsExpectedFail(222);
        xtr.setTestsFail(333);
        xtr.setTestsPass(444);
        xtr.setTestsTotal(555);
        xtr.setTestsUnexpectedPass(666);
        
        Object xtestResultsReportId = null;
        Object testedTypeId = null;
        long systemInfoId = -1;
        try {
            String projectId = dBUtils.getFirst("PROJECT", "ID").toString();
            xtr.setProject_id(projectId);
            TestRun testRun = new TestRun();
            testRun.xmlel_TestBag = new TestBag[0];
            testRun.setRunID("DUMMYRUNID");
            xtr.xmlel_TestRun = new TestRun[] {testRun};
            SystemInfo info = new SystemInfo();
            info.setHost("DUMMYHOST");
            systemInfoId = dbs.storeSystemInfo(info);
            xtr.xmlel_SystemInfo = new SystemInfo[] {info};

            dbs.storeXTestResultsReport(xtr, false);
            connection.commit();
            //check TESTEDTYPE table
            testedTypeId = dBUtils.queryFirst("TESTEDTYPE", "ID", "NAME", "DUMMYTESTEDTYPE");
            assertNotNull("Not inserted into TESTEDTYPE", testedTypeId);
            // check XTESTRESULTSREPORT table
            xtestResultsReportId = dBUtils.queryFirst("XTESTRESULTSREPORT", "ID", "TESTEDTYPE_ID", testedTypeId);
            assertNotNull("Not inserted into XTESTRESULTSREPORT", xtestResultsReportId);
            Object value = dBUtils.queryFirst("XTESTRESULTSREPORT", "PROJECT_ID", "ID", xtestResultsReportId);
            assertEquals("XTESTRESULTSREPORT.PROJECT_ID has wrong value", xtr.getProject_id(), value);
            value = dBUtils.queryFirst("XTESTRESULTSREPORT", "BUILD", "ID", xtestResultsReportId);
            assertEquals("XTESTRESULTSREPORT.BUILD has wrong value", xtr.getBuild(), value);
            value = dBUtils.queryFirst("XTESTRESULTSREPORT", "RUNID", "ID", xtestResultsReportId);
            assertEquals("XTESTRESULTSREPORT.RUNID has wrong value", "DUMMYRUNID", value);
            value = dBUtils.queryFirst("XTESTRESULTSREPORT", "EXECTIME", "ID", xtestResultsReportId);
            assertEquals("XTESTRESULTSREPORT.EXECTIME has wrong value", xtr.getTime(), ((Number)value).longValue());
            value = dBUtils.queryFirst("XTESTRESULTSREPORT", "WEBLINK", "ID", xtestResultsReportId);
            assertEquals("XTESTRESULTSREPORT.WEBLINK has wrong value", xtr.getWebLink(), value);
        } finally {
            // delete test data
            dBUtils.deleteFromTable("XTESTRESULTSREPORT", "ID = '"+xtestResultsReportId+"'");
            dBUtils.deleteFromTable("TESTEDTYPE", "ID = '"+testedTypeId+"'");
            dBUtils.deleteFromTable("SYSTEMINFO", "ID = '"+systemInfoId+"'");
            connection.close();
        }
    }
}
