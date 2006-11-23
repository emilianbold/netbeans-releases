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

/*
 * DbStorageTask.java
 *
 * Created on February 7, 2002, 11:57 AM
 */

package org.netbeans.xtest.pes.dbfeeder;


import java.sql.*;
import java.util.ArrayList;
import org.netbeans.xtest.pe.xmlbeans.*;
import org.netbeans.xtest.pes.xmlbeans.*;
import java.beans.IntrospectionException;
import org.netbeans.xtest.pes.PESLogger;
import java.util.logging.Level;

/** Utility class to store xml beans to a database. It is called from DbFeeder class.
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @author Jiri.Skrivanek@sun.com
 */
public class DbStorage {
    
    DbUtils utils;
    Connection connection;
    
    /** creates new DbStorage
     */
    public DbStorage(Connection connection) {
        this.connection = connection;
        utils = new DbUtils(connection);
    }
    
    
    
    
    /** performs storage of results using prepared connection
     * @param xtr - report to be stored
     * @param webLink - optional link to the report on the local pes
     * @throws Exception Exception
     */
    public synchronized void storeXTestResultsReport(XTestResultsReport xtr, boolean replace) throws SQLException {
        
        try {
            PESLogger.logger.fine("Storing XTestResultsReport: "+xtr.getHost()+" "+xtr.getProject()+" "+xtr.getBuild());
            
            // is this correct -> I don't think this report is valid, so this should not happen
            if (xtr.xmlel_TestRun==null) {
                throw new SQLException("XTest Result Report does not contain any testruns -- cannot be uploaded to db");
            }
            // store system info
            long systemInfo_id = storeSystemInfo(xtr.xmlel_SystemInfo[0]);
            
            xtr.setSystemInfo_id(systemInfo_id);
            
            Object obj = utils.queryFirst(xtr, "id","id");
            if (obj != null) {
                if (replace) {
                    long storedReportID = ((Number)obj).longValue();
                    PESLogger.logger.fine("Replacing XTestResultsReport with id: "+storedReportID);
                    utils.deleteFromTable("XTestResultsReport","ID = "+storedReportID);
                } else {
                    throw new SQLException("XTest Result Report already in database");
                }
            }
            //
            for (int i=0;i<xtr.xmlel_TestRun.length;i++) {
                TestRun run = xtr.xmlel_TestRun[i];
                // if testrun is empty - skip it and write out warning
                if (run.xmlel_TestBag==null) {
                    PESLogger.logger.log(Level.WARNING,"Detected empty test run in report: from host"+xtr.getHost()
                            +", project_id: "+xtr.getProject_id()+", build: "+xtr.getBuild()+", not storing this testrun to database");
                } else {
                    // store XTestResultsReport. In normal case TestRun is only one.
                    // We should investigate whether we should support more test runs.
            
                    // This was used when there was only single XTestResultsReport table.
                    //long xtestResultsReport_id = ((Number)utils.insertBeanAutoIncrement(xtr, "id")).longValue();
        
                    // Now we have XTESTRESULTSREPORT and TESTEDTYPE tables
        
                    // insert into TESTEDTYPE table if record not exist
                    Object testedTypeId = utils.insertAutoIncrementIfNotExist(
                            "TESTEDTYPE",
                            "ID",
                            new String[] {"NAME"},
                            new Object[] {xtr.getTestedType()}
                    );
                    // insert into XTESTRESULTSREPORT with reference to TESTEDTYPE
                    PESLogger.logger.finest("Inserting into XTESTRESULTSREPORT. TESTEDTYPE_ID="+testedTypeId);
                    long xtestResultsReport_id = ((Number)utils.insertAutoIncrement(
                                        "XTESTRESULTSREPORT",
                                        "ID",
                                        new String[] {
                                            "PROJECT_ID",
                                            "BUILD",
                                            "RUNID",
                                            "TESTEDTYPE_ID",
                                            "STARTDATE",
                                            "EXECTIME",
                                            "SYSTEMINFO_ID",
                                            "WEBLINK"
                                        },
                                        new Object[] {
                                            xtr.getProject_id(),
                                            xtr.getBuild(),
                                            run.getRunID(),
                                            testedTypeId,
                                            xtr.getTimeStamp(),
                                            Long.valueOf(xtr.getTime()),
                                            Long.valueOf(xtr.getSystemInfo_id()),
                                            xtr.getWebLink()
                                        }
                            )).longValue();
                    for (int j=0;j<run.xmlel_TestBag.length;j++) {
                        // although this is highly unprobable, better check for nulls
                        if (run.xmlel_TestBag[j] != null) {
                            storeTestBag(run.xmlel_TestBag[j], xtestResultsReport_id, run.getRunID());
                        }
                    }
                }
            }
        } catch (IntrospectionException ie) {
            PESLogger.logger.log(Level.SEVERE,"Caught IntrospectionException when storing XTestResultsReport in database",ie);
            throw new SQLException("Caught IntrospectionException when storing XTestResultsReport in database: "+ie.getMessage());
        }
    }
    
    
    long storeSystemInfo(SystemInfo info) throws SQLException, IntrospectionException {
        PESLogger.logger.finer("System Info: "+info.getHost());
        Number id = (Number)utils.queryFirst(info,"id","id");
        if (id!=null) {
            PESLogger.logger.finest("System Info already in database.");
            return id.longValue();
        }
        id = (Number)utils.insertBeanAutoIncrement(info, "id");
        return id.longValue();
    }
    
    /** Currently not used. See coment at DbFeeder.updateLocalTeamBuild().
     * Updates the localteambuild table.
     */
    void updateLocalTeamBuild(String team, String project, String lastBuild) throws SQLException {
        PESLogger.logger.fine("Updating LocalTeamBuild table for project="+project+", team="+team+" with lastBuild = "+lastBuild);
        //
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            String buildQuery = "SELECT build FROM LocalTeamBuild WHERE project='"+project+"' AND team='"+team+"'";
            PESLogger.logger.finest("Detecting whether builds are already in the table. Query:"+buildQuery);
            ResultSet rs = stmt.executeQuery(buildQuery);
            if (rs.next()) {
                // get the build number, whether the update is neccessary
                String lastDbBuild = rs.getString(1);
                
                if ( !lastDbBuild.equals(lastBuild)) {
                    // update required
                    String updateQuery = "UPDATE LocalTeamBuild SET build = '"+lastBuild
                            +"' WHERE project='"+project+"' AND team='"+team+"'";
                    PESLogger.logger.fine("Updating LocalTeamBuild table with query:"+updateQuery);
                    stmt.executeUpdate(updateQuery);
                    connection.commit();
                }
            } else {
                // insert needed
                String insertQuery = "INSERT INTO LocalTeamBuild VALUES ('"+team+"','"+project+"','"+lastBuild+"')";
                PESLogger.logger.fine("Inserting to LocalTeamBuild table:"+insertQuery);
                stmt.executeUpdate(insertQuery);
                connection.commit();
            }
        } catch (SQLException sqle) {
            // hmm, something went wrong
            PESLogger.logger.log(Level.SEVERE,"Caught SQLException when updating LocalTeamBuild table: ",sqle);
            connection.rollback();
            throw sqle;
        } finally {
            utils.closeStatement(stmt);
        }
    }
    
    /** Stores given test bag and all included test suites and test cases. */
    void storeTestBag(TestBag bag, long xtestResultsReport_id, String runID) throws SQLException, IntrospectionException {
        bag.setXTestResultsReport_id(xtestResultsReport_id);
        bag.setRunID(runID);
        PESLogger.logger.finer("Test Bag: "+bag.getBagID());
        long testBag_id = ((Number)utils.insertBeanAutoIncrement(bag, "id")).longValue();
        if (bag.xmlel_UnitTestSuite==null) {
            // testbag could contain no test suite
            return ;
        }
        for (int i=0;i<bag.xmlel_UnitTestSuite.length;i++) {
            // although this is highly unprobable, better check for nulls
            if (bag.xmlel_UnitTestSuite[i] != null) {
                storeUnitTestSuite(bag.xmlel_UnitTestSuite[i], testBag_id);
            }
        }
    }
    
    /** Stores given test suite and all included performance data and test cases. */
    void storeUnitTestSuite(UnitTestSuite testSuite, long testBag_id) throws SQLException, IntrospectionException {
        testSuite.setTestBag_id(testBag_id);
        PESLogger.logger.finest("Unit Test Suite: "+testSuite.getName());
        // This was used when there was only single UnitTestSuite table.
        // long testSuiteId = ((Number)utils.insertBeanAutoIncrement(suite, "id")).longValue();
        
        // Now we have TESTSUITE and TESTSUITENAME tables
        
        // insert into TESTSUITENAME table if record not exist
        Object testSuiteNameId = utils.insertAutoIncrementIfNotExist(
                "TESTSUITENAME",
                "ID",
                new String[] {"NAME"},
                new Object[] {testSuite.getName()}
        );
        // insert into TESTSUITE with reference to TESTSUITENAME
        PESLogger.logger.finest("Inserting into TESTSUITE. TESTSUITENAME_ID="+testSuiteNameId);
        long testSuiteId = ((Number)utils.insertAutoIncrement("TESTSUITE", "ID",
                                        new String[] {
                                            "TESTSUITENAME_ID",
                                            "TESTBAG_ID"
                                        },
                                        new Object[] {
                                            testSuiteNameId,
                                            Long.valueOf(testSuite.getTestBag_id())
                                        }
                    )).longValue();
        if (testSuite.xmlel_UnitTestCase==null) {
            // suite does not have to contain any testcase
            // is this possible - I think so
            return;
        }
        for (int i=0;i<testSuite.xmlel_UnitTestCase.length;i++) {
            // although this is highly unprobable, better check for nulls
            if (testSuite.xmlel_UnitTestCase[i] != null) {
                storeUnitTestCase(testSuite.xmlel_UnitTestCase[i], testSuiteId);
            }
        }
        if (testSuite.xmlel_Data==null || testSuite.xmlel_Data.length<1 || testSuite.xmlel_Data[0]==null) return;
        PerformanceData d[] = testSuite.xmlel_Data[0].xmlel_PerformanceData;
        if ( d == null || d.length < 1) return;
        for (int i=0;i<d.length;i++) {
            // although this is highly unprobable, better check for nulls
            if (d[i] != null) {
                storePerformanceData(d[i], testSuiteId);
            }
        }
    }
    
    /** Stores given test case. */
    void storeUnitTestCase(UnitTestCase testCase, long testSuite_id) throws SQLException, IntrospectionException {
        testCase.setUnitTestSuite_id(testSuite_id);
        PESLogger.logger.finest("Unit Test Case: "+testCase.getName());
        // This was used when there was only single UnitTestCase table.
        //utils.insertBean(test);
        
        // Now we have TESTCASENAME, TESTCASERESULT and TESTCASE tables
        
        // try to find CLASSNAME and NAME pair in TESTCASENAME table
        String[] columns = {"CLASSNAME", "NAME"};
        Object[] values = {
            testCase.getClassName(),
            testCase.getName()
        };
        Object testCaseNameId = utils.insertAutoIncrementIfNotExist("TESTCASENAME", "ID", columns, values);

        // try to find RESULT in TESTCASERESULT table
        Object testCaseResultId = utils.insertAutoIncrementIfNotExist(
                        "TESTCASERESULT",
                        "ID",
                        new String[] {"RESULT"},
                        new String[] {testCase.getResult()});
        PESLogger.logger.finest("Inserting into TESTCASE. TESTCASENAME_ID="+testCaseNameId+", TESTCASERESULT_ID="+testCaseResultId);
        // insert into TESTCASE with reference to TESTCASENAME and TESTCASERESULT
        utils.insert("TESTCASE", 
                        new String[] {
                            "MESSAGE",
                            "EXECTIME",
                            "FAILREASON",
                            "STACKTRACE",
                            "TESTCASENAME_ID",
                            "TESTCASERESULT_ID",
                            "TESTSUITE_ID"
                        },
                        new Object[] {
                            testCase.getMessage(),
                            Long.valueOf(testCase.getTime()),
                            testCase.getFailReason(),
                            testCase.getStackTrace(),
                            testCaseNameId,
                            testCaseResultId,
                            Long.valueOf(testCase.getUnitTestSuite_id())
                        }
        );
    }
    
    /** Stores given performance data. */
    void storePerformanceData(PerformanceData performanceData, long testSuite_id) throws SQLException, IntrospectionException {
        performanceData.setUnitTestSuite_id(testSuite_id);
        PESLogger.logger.finest("PerformanceData: "+performanceData.xmlat_name);
        // This was used when there was only single PerformanceData table.
        // utils.insertBean(performanceData);
        
        // Now we have PERFDATANAME and PERFORMANCEDATA tables
        
        // try to find performance data in PERFDATANAME table
        String[] columns = {"NAME", "UNIT", "RUNORDER", "THRESHOLD"};
        Object[] values = {
            performanceData.getName(),
            performanceData.getUnit(),
            Integer.valueOf(performanceData.getRunOrder()),
            Long.valueOf(performanceData.getThreshold())
        };
        Object perfDataNameId = utils.insertAutoIncrementIfNotExist("PERFDATANAME", "ID", columns, values);
        PESLogger.logger.finest("Inserting into PERFORMANCEDATA. PERFDATANAME_ID="+perfDataNameId);
        // insert into PERFORMANCEDATA with reference to PERFDATANAME
        utils.insert("PERFORMANCEDATA", 
                        new String[] {
                            "PERFDATANAME_ID",
                            "TESTSUITE_ID", 
                            "VALUE"
                        },
                        new Object[] {
                            perfDataNameId,
                            Long.valueOf(performanceData.getUnitTestSuite_id()),
                            Long.valueOf(performanceData.getValue())
                        }
        );
    }
    
    /** Deletes results for builds beyond deleteAge threshold. It deletes only
     * builds which have the same attributes like given XTestResultsReport. It
     * also ignores milestone builds.
     */
    public void deleteOldResults(XTestResultsReport xtr, int deleteAge) throws SQLException {
        String[] builds = getBuilds(xtr);
        if(builds.length > deleteAge) {
            // remove older than deleteAge
            for(int i=deleteAge;i<builds.length;i++) {
                String whereClause = "build = '"+builds[i]+"' AND "+
                        "project_id = '"+xtr.getProject_id()+"' AND "+
                        "testedType_id = (select id from TestedType where name = '"+xtr.getTestedType()+"')";
                PESLogger.logger.finest("Going to delete out-dated results where:\n"+whereClause);
                utils.deleteFromTable("XTestResultsReport", whereClause);
            }
        }
    }
    
    /** Returns array of build numbers available in database for given report
     * (i.e. same project, team, testinggroup and testedtype). It ignores
     * milestone builds.
     */
    private String[] getBuilds(XTestResultsReport xtr) throws SQLException {
        String sqlQuery = "SELECT DISTINCT build FROM XTestResultsReport, TestedType WHERE "+
                "project_id = '"+xtr.getProject_id()+"' AND "+
                "testedType_id = TestedType.id AND "+
                "TestedType.name = '"+xtr.getTestedType()+"' AND "+
                "build NOT IN (SELECT build FROM MilestoneBuild WHERE project_id = '"+xtr.getProject_id()+"')"+
                "order by build desc";
        Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet resultSet = stmt.executeQuery(sqlQuery);
        ArrayList list = new ArrayList();
        resultSet.beforeFirst();
        while (resultSet.next()) {
            list.add(resultSet.getObject(1));
        }
        resultSet.close();
        stmt.close();
        return (String[])list.toArray(new String[list.size()]);
    }
}
