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
import org.netbeans.xtest.pe.ResultsUtils;
import org.netbeans.xtest.pe.xmlbeans.*;
import org.netbeans.xtest.pes.xmlbeans.*;
import java.io.File;
import java.io.PrintStream;
import java.io.IOException;
import java.util.Properties;
import java.beans.IntrospectionException;
import org.netbeans.xtest.pes.PESLogger;
import java.util.logging.Level;
import org.netbeans.xtest.util.OSNameMappingTable;

/** Ant task performing storage of XTest results into MySQL database
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 0.2
 */
public class DbStorage {
    
    DbUtils utils;
    Connection connection;
    
    /** creates new DbStorageTask
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
            long xtestResultsReport_id = ((Number)utils.insertBeanAutoIncrement(xtr, "id")).longValue();
            // is this correct -> I don't think this report is valid, so this should not happen
            if (xtr.xmlel_TestRun==null) {
                throw new SQLException("XTest Result Report does not contain any testruns -- cannot be uploaded to db");
            }
            //
            for (int i=0;i<xtr.xmlel_TestRun.length;i++) {
                TestRun run = xtr.xmlel_TestRun[i];
                // if testrun is empty - skip it and write out warning
                if (run.xmlel_TestBag==null) {
                    PESLogger.logger.log(Level.WARNING,"Detected empty test run in report: from host"+xtr.getHost()
                            +", project_id: "+xtr.getProject_id()+", build: "+xtr.getBuild()+", not storing this testrun to database");
                } else {
                    for (int j=0;j<run.xmlel_TestBag.length;j++) {
                        // although this is highly unprobable, better check for nulls
                        if (run.xmlel_TestBag[j] != null) {
                            storeTestBag(run.xmlel_TestBag[j], xtestResultsReport_id, run.getRunID());
                        }
                    }
                }
            }
            // finally store attributes (optional)
            if (xtr.xmlel_Attribute != null) {
                for (int i=0; i < xtr.xmlel_Attribute.length; i++) {
                    storeReportAttribute(xtr.xmlel_Attribute[i], xtestResultsReport_id);
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
        updateOSName(info);
        id = (Number)utils.insertBeanAutoIncrement(info, "id");
        if (info.xmlel_SystemInfoExtra!=null) {
            for (int i=0;i<info.xmlel_SystemInfoExtra.length;i++) {
                // although this is highly unprobable, better check for nulls
                if (info.xmlel_SystemInfoExtra[i] != null) {
                    storeSystemInfoExtra(info.xmlel_SystemInfoExtra[i], id.longValue());
                }
            }
        }
        return id.longValue();
    }
    
    void storeSystemInfoExtra(SystemInfoExtra extra, long systemInfo_id) throws SQLException, IntrospectionException {
        extra.setSystemInfo_id(systemInfo_id);
        PESLogger.logger.finest("System Info Extra: "+extra.getName()+"="+extra.getValue());
        utils.insertBean(extra);
    }
    
    // update rows in OSNames table if neccessary
    void updateOSName(SystemInfo info) throws SQLException {
        String osName = info.getOsName();
        String osVersion = info.getOsVersion();
        String osArch = info.getOsArch();
        // query
        String osnameQuery = "SELECT name FROM OSnames WHERE osName = '"+osName+"' AND osVersion = '"
                +osVersion+"' AND osArch = '"+osArch+"'";
        PESLogger.logger.finest("Detecting whether this OS is alredy in OSnames table. Query:"+osnameQuery);
        if (!utils.anyResultsFromQuery(osnameQuery)) {
            // we need to insert the new osName row
            String fullOSName = OSNameMappingTable.getFullOSName(osName,osVersion,osArch);
            if (fullOSName.equals(OSNameMappingTable.UNKNOWN_OS)) {
                fullOSName = osName+"-"+osVersion+"-"+osArch;
                PESLogger.logger.severe("Unknown OS detected when inserting new full os name to OSnames table. host="+info.getHost()+", osName="
                        +osName+", osVersion="+osVersion+", osArch="+osArch
                        +". You should manually fix the row with name="+fullOSName);
                
            }
            String insert = "INSERT INTO OSnames VALUES ('"+fullOSName+"','"+osName+"','"
                    +osArch+"','"+osVersion+"')";
            PESLogger.logger.fine("OS not found in OSnames table, inserting a new row. Statement:"+insert);
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(insert);
            utils.closeStatement(stmt);
        } else {
            // nothing - so return
            return;
        }
    }
    
    
    // update the localteambuild table
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
    
    void storeUnitTestSuite(UnitTestSuite suite, long testBag_id) throws SQLException, IntrospectionException {
        suite.setTestBag_id(testBag_id);
        PESLogger.logger.finest("Unit Test Suite: "+suite.getName());
        long unitTestSuite_id = ((Number)utils.insertBeanAutoIncrement(suite, "id")).longValue();
        if (suite.xmlel_UnitTestCase==null) {
            // suite does not have to contain any testcase
            // is this possible - I think so
            return;
        }
        for (int i=0;i<suite.xmlel_UnitTestCase.length;i++) {
            // although this is highly unprobable, better check for nulls
            if (suite.xmlel_UnitTestCase[i] != null) {
                storeUnitTestCase(suite.xmlel_UnitTestCase[i], unitTestSuite_id);
            }
        }
        if (suite.xmlel_Data==null || suite.xmlel_Data.length<1 || suite.xmlel_Data[0]==null) return;
        PerformanceData d[] = suite.xmlel_Data[0].xmlel_PerformanceData;
        if ( d == null || d.length < 1) return;
        for (int i=0;i<d.length;i++) {
            // although this is highly unprobable, better check for nulls
            if (d[i] != null) {
                storePerformanceData(d[i], unitTestSuite_id);
            }
        }
    }
    
    void storeUnitTestCase(UnitTestCase test, long testSuite_id) throws SQLException, IntrospectionException {
        test.setUnitTestSuite_id(testSuite_id);
        PESLogger.logger.finest("Unit Test Case: "+test.getName());
        // This was used when there was only single UnitTestCase table.
        //utils.insertBean(test);
        
        // Now we have UnitTestCase_def and UnitTestCase_tbl tables
        
        // try to find classname and name pair in UnitTestCase_def table
        Object unitTestCase_def_id = utils.queryFirst("UnitTestCase_def", "id",
                        new String[] {"classname", "name"},
                        new String[] {test.getClassName(), test.getName()});
        if(unitTestCase_def_id == null) {
            // not found in UnitTestCase_def => insert a new record into UnitTestCase_def
            PESLogger.logger.finest("Inserting "+test.getClassName()+", "+test.getName()+" into UnitTestCase_def");
            unitTestCase_def_id = utils.insertAutoIncrement("UnitTestCase_def", "id", 
                        new String[] {"classname", "name"},
                        new String[] {test.getClassName(), test.getName()});
        }
        PESLogger.logger.finest("Inserting into UnitTestCase_tbl. unitTestCase_def_id="+unitTestCase_def_id);
        // insert into UnitTestCase_tbl with reference to UnitTestCase_def
        utils.insert("UnitTestCase_tbl", 
                        new String[] {
                            "UnitTestCase_def_id",
                            "result", 
                            "message",
                            "failreason",
                            "time",
                            "UnitTestSuite_id"
                        },
                        new Object[] {
                            unitTestCase_def_id, 
                            test.getResult(), 
                            test.getMessage(), 
                            test.getFailReason(),
                            new Long(test.getTime()), 
                            new Long(test.getUnitTestSuite_id())
                        }
        );
    }
    
    // is this correct -> shoudn't we agreed in term just plain 'data'
    void storePerformanceData(PerformanceData data, long testSuite_id) throws SQLException, IntrospectionException {
        data.setUnitTestSuite_id(testSuite_id);
        PESLogger.logger.finest("PerformanceData: "+data.xmlat_name);
        utils.insertBean(data);
    }
    
    // store attributes - this is going to be a little tricky ....
    void storeReportAttribute(Attribute attribute, long xtestResultsReport_id) throws SQLException {
        String name = attribute.getName();
        String value = attribute.getValue();
        PESLogger.logger.finest("Attribute: "+name+" : "+value);
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            long attributeID;
            Long idWrapper = getAttributeID(name,value,stmt);
            if (idWrapper == null) {
                // need to store the attribute;
                String attributeInsert =  "INSERT INTO Attribute (name,value) VALUES ('"+name+"','"+value+"')";
                stmt.executeUpdate(attributeInsert);
                attributeID = getAttributeID(name,value,stmt).longValue();
            } else {
                attributeID = idWrapper.longValue();
            }
            // now create the m:m relation ship
            String reportAttributeInsert = "INSERT INTO ReportAttribute VALUES ("+xtestResultsReport_id+","+attributeID+")";
            stmt.executeUpdate(reportAttributeInsert);
            // we're done
        } finally {
            DbUtils.closeStatement(stmt);
        }
    }
    
    // helper method for obtaining the id of attribute name/value
    // it uses Long wrapper, because it returns null when the attribute does not exist
    private Long getAttributeID(String name, String value, Statement stmt) throws SQLException {
        String attributeQuery = "SELECT id FROM Attribute WHERE name = '"+name+"' AND value = '"+value+"'";
        PESLogger.logger.finest("Detecting whether attribute is already in the table. Query:"+attributeQuery);
        ResultSet rs = stmt.executeQuery(attributeQuery);
        if (rs.next()) {
            // yes, it is in the table
            long attributeID = rs.getLong(1);
            return new Long(attributeID);
        } else {
            // nothing is in the table
            return null;
        }
        
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
                        "team = '"+xtr.getTeam()+"' AND "+
                        "testinggroup = '"+xtr.getTestingGroup()+"' AND "+
                        "testedtype = '"+xtr.getTestedType()+"'";
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
        String sqlQuery = "SELECT DISTINCT build FROM XTestResultsReport WHERE "+
                "project_id = '"+xtr.getProject_id()+"' AND "+
                "team = '"+xtr.getTeam()+"' AND "+
                "testinggroup = '"+xtr.getTestingGroup()+"' AND "+
                "testedtype = '"+xtr.getTestedType()+"' AND "+
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
