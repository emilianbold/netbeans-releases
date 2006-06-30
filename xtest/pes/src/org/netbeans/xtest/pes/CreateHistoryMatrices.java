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
 * CreateHistoryMatrix.java
 *
 * Created on January 7, 2002, 2:37 PM
 */

package org.netbeans.xtest.pes;

import java.util.*;
import java.io.*;
import org.netbeans.xtest.pe.xmlbeans.*;
import org.netbeans.xtest.pes.xmlbeans.*;
import org.netbeans.xtest.pe.*;
import javax.xml.transform.*;
import org.netbeans.xtest.util.*;
/**
 *
 * @author  mb115822
 */
public class CreateHistoryMatrices {

    // debugging flag - should be set to false :-)
    private static final boolean DEBUG = false;
    private static final void debugInfo(String message) {
        if (DEBUG) System.out.println("CreateHistoryMatrices."+message);
    }
    

    // xsl file for transformations
    private Transformer xslTransformer = null;
    private ManagedGroup managedGroup = null;
    private int buildHistoryAge = 10;
    
    /** Creates a new instance of CreateHistoryMatrix */    
    public CreateHistoryMatrices(ManagedGroup mg, Transformer xslTransformer) {
        this.managedGroup = mg;
        this.xslTransformer = xslTransformer;
    }
    
    public void setBuildHistoryAge(int age) {
        this.buildHistoryAge = age;
    }
    
    // iterate over projects and hosts and create history matrices
    public void createMatrices(String project) throws Exception {
        debugInfo("createMatrices()");
        String currentProject = project;
        Iterator groupIterator = managedGroup.getUniqueTestingGroups(currentProject).iterator();
        while (groupIterator.hasNext()) {
            String currentGroup = (String)groupIterator.next();
            Iterator typeIterator = managedGroup.getUniqueTestedTypes(currentProject,currentGroup).iterator();
            while (typeIterator.hasNext()) {
                String currentType = (String)typeIterator.next();
                Collection uniqueHosts = managedGroup.getUniqueHosts(currentProject);
                debugInfo("createMatrices(): for project "+currentProject+" got uniqueHosts, size="+uniqueHosts.size());
                // now for each host create history matrix
                Iterator hostsIterator = uniqueHosts.iterator();
                while (hostsIterator.hasNext()) {
                    String currentHost = (String)hostsIterator.next();
                    // get all builds for this project and host
                    String uniqueBuilds[] = (String[])(managedGroup.getUniqueBuilds(currentProject,currentGroup,currentType,currentHost).toArray(new String[0]));
                    if (uniqueBuilds.length > 0) {
                        debugInfo("createMatrices(): got uniqueBuilds, size="+uniqueBuilds.length);
                        Arrays.sort(uniqueBuilds);
                        // now for each host create history matrix - this is ascending - we need descending
                        // and a limited number
                        int youngSize = (uniqueBuilds.length < buildHistoryAge) ? uniqueBuilds.length : buildHistoryAge;
                        debugInfo("createMatrices(): limited 'young' size = "+youngSize);
                        String youngBuilds[] = new String[youngSize];
                        // do reverse on sorted array
                        for (int i=0;i<youngSize;i++) {
                            youngBuilds[i] = uniqueBuilds[uniqueBuilds.length-i-1];
                            debugInfo("createMatrices() : will use build " + youngBuilds[i] + " at position "+i);
                        }
                        // now construct the matrix
                        debugInfo("createMatrices(): for project "+currentProject+", host "+currentHost+" and "+buildHistoryAge+" builds");
                        // now create matrix for this project and host over defined number of
                        // need to be added !!!!!!!!!!!!!!!!!
                        createMatrix(currentProject,youngBuilds,currentGroup,currentType,currentHost);
                    } else {
                        
                    }
                }
            }
        }

    }
    
    
    // create history matrix for given host and projects over defined builds
    public void createMatrix(String project, String builds[], String testingGroup, String testedType, String host) throws Exception {
        debugInfo("createMatrix(): project="+project+" host="+host);
        
        UniqueTestList gatheredTests = new UniqueTestList();
        
        // for each watched build
        for (int i=0; i< builds.length; i++) {
            debugInfo("createMatrix(): gathering, working on build "+builds[i]);
            ManagedReport relevantReports[] = managedGroup.filterBy(project,builds[i],testingGroup,testedType,host);
            debugInfo("createMatrix(): gathering, got "+relevantReports.length+" reports");
            // for each relevant managed report
            for (int j=0; j< relevantReports.length; j++) {
                // get failures report
                File reportRootDir = new File(managedGroup.getPESWeb().getWebRoot(),relevantReports[j].getPathToResultsRoot());
                File failuresReportFile = new File(ResultsUtils.getXMLResultDir(reportRootDir),PEConstants.TESTREPORT_FAILURES_XML_FILE);
                debugInfo("createMatrix(): gathering, processing information from "+failuresReportFile);
                XTestResultsReport failuresReport = ResultsUtils.getXTestResultsReport(failuresReportFile);
                // add all failures/errors to our test list
                addFailedIDs(gatheredTests,failuresReport);
            }
        }
        debugInfo("!!!!\n\n\ncreateMatrix(): project="+project+" host="+host+" gathered testIDs="+gatheredTests.size()+"\n\n\n!!!!!");
        
        
        // create failures matrix
        FailuresMatrix failuresMatrix = new FailuresMatrix(gatheredTests,builds);
        
        // we have all watched failures gathered - now it is time to
        // search in which build the tests failed/passed/didn't run ....
        // for each watched build
        
        for (int i=0; i< builds.length; i++) {
            debugInfo("createMatrix(): exploring details, working on build "+builds[i]);
            ManagedReport relevantReports[] = managedGroup.filterBy(project,builds[i],testingGroup,testedType,host);
            debugInfo("createMatrix(): exploring details, got "+relevantReports.length+" reports");
            // for each relevant managed report
            for (int j=0; j< relevantReports.length; j++) {
                // get failures report
                File reportRootDir = new File(managedGroup.getPESWeb().getWebRoot(),relevantReports[j].getPathToResultsRoot());
                File reportFile = new File(ResultsUtils.getXMLResultDir(reportRootDir),PEConstants.TESTREPORT_XML_FILE);
                debugInfo("createMatrix(): exploring details, processing information from "+reportFile);
                XTestResultsReport xtr = ResultsUtils.getXTestResultsReport(reportFile);
                // add all failures/errors to our test list
                checkTestResults(failuresMatrix,xtr,relevantReports[j].getPathToResultsRoot());
            }
        }
        debugInfo("*****!!!!\n\n\ncreateMatrix(): matricx created!!! project="+project+" host="+host);
        XTestHistoryMatrix xmlMatrix = failuresMatrix.toXTestHistoryMatrix();
        xmlMatrix.xmlat_project = project;
        xmlMatrix.xmlat_host = host;
        xmlMatrix.xmlat_testingGroup = testingGroup;
        xmlMatrix.xmlat_testedType = testedType;
        if (xslTransformer == null) {
            // serialize to XML
            String matrixFilename = FileUtils.normalizeName("matrix-"+managedGroup.getName()+"-"+testingGroup+"-"+testedType+"-"+host+".xml");
            File matrixFile = new File(managedGroup.getPESWeb().getDataDir(),matrixFilename);
            xmlMatrix.saveXMLBean(matrixFile);            
            debugInfo("createMatrix(): matrix serialized to "+matrixFile+"\n\n\n*****!!!!!");
        } else {
            // serialize to HTML via transformations
            String matrixFilename = FileUtils.normalizeName("matrix-"+managedGroup.getName()+"-"+testingGroup+"-"+testedType+"-"+host+".html");
            File projectDir = new File(managedGroup.getPESWeb().getWebRoot(),FileUtils.normalizeName(project));
            File matrixFile = new File(projectDir,matrixFilename);
            XSLUtils.transform(xslTransformer,xmlMatrix.toDocument(),matrixFile);
            debugInfo("createMatrix(): matrix transformed to "+matrixFile+"\n\n\n*****!!!!!");
        }

    }
    
    // add all failures/errors from this report
    public void addFailedIDs(UniqueTestList testList, XTestResultsReport xtr) {
        debugInfo("addFailedIDs(): adding failures/errors");
        if (xtr.xmlel_TestRun != null) {
            for (int i=0; i < xtr.xmlel_TestRun.length ; i++) {
                TestRun run = xtr.xmlel_TestRun[i];
                if (run.xmlel_TestBag != null) {
                    for (int j=0; j < run.xmlel_TestBag.length; j++) {
                        TestBag bag = run.xmlel_TestBag[j];
                        if (bag != null) {
                            for (int k = 0; k < bag.xmlel_UnitTestSuite.length; k++) {
                                UnitTestSuite suite = bag.xmlel_UnitTestSuite[k];
                                if ((suite != null) && (suite.xmlel_UnitTestCase != null)) {                                    
                                    for (int l = 0 ; l < suite.xmlel_UnitTestCase.length; l++) {
                                        UnitTestCase testCase = suite.xmlel_UnitTestCase[l];
                                        //System.out.println("TestCase:"+testCase.xmlat_class+"."+testCase.xmlat_name+":"+testCase.xmlat_result);
                                        if (UnitTestCase.TEST_FAIL.equals(testCase.xmlat_result)|
                                            UnitTestCase.TEST_ERROR.equals(testCase.xmlat_result)|
                                            UnitTestCase.TEST_EXPECTED_FAIL.equals(testCase.xmlat_result)|
                                            UnitTestCase.TEST_UNEXPECTED_PASS.equals(testCase.xmlat_result)) {
                                            // failure is here !!!
                                            TestID newID = new TestID();
                                            newID.setRepositoryName(run.xmlat_name);
                                            newID.setModuleName(bag.xmlat_module);
                                            newID.setTestBagName(bag.xmlat_name);
                                            newID.setTestType(bag.xmlat_testType);
                                            newID.setSuiteName(suite.xmlat_name);
                                            newID.setTestClass(testCase.xmlat_class);
                                            newID.setTestName(testCase.xmlat_name);
                                            if (testList.add(newID)) {
                                                debugInfo("addFailedIDs(): adding new testID: "+newID);
                                            } else {
                                                debugInfo("addFailedIDs(): testID already in the set: "+newID);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    
    
    
    // check the build results against our tests
    public void checkTestResults(FailuresMatrix failuresMatrix, XTestResultsReport xtr, String pathToReport) throws Exception {
        File reportRootDir = new File(managedGroup.getPESWeb().getWebRoot(),pathToReport);
        String checkedBuild = xtr.xmlat_build;
        debugInfo("checkTestResults(): adding failures/errors/N/A tests on build"+checkedBuild);
        if (failuresMatrix.setCheckedBuild(checkedBuild) == FailuresMatrix.NO_BUILD) {
            // we don't have this build in out failures matrix - it is weird !!!
            debugInfo("checkTestResults(): NO_BUILD !!!! - not doing check for this report");
            return;
        }
        HashSet checkedRepositories = failuresMatrix.getTestIDs().getUniqueRepositories();
        if (xtr.xmlel_TestRun != null) {
            for (int i=0; i<xtr.xmlel_TestRun.length; i++) {
                TestRun run = xtr.xmlel_TestRun[i];
                // is this test run included in checked tests
                if (checkedRepositories.contains(run.xmlat_name)) {
                    debugInfo("checkTestResults(): checking "+run.xmlat_name+" :"+i);
                    TestID repositoryFilter = TestID.createFilter();
                    repositoryFilter.setRepositoryName(run.xmlat_name);
                    UniqueTestList repositoryTestList = failuresMatrix.getTestIDs().filterBy(repositoryFilter);
                    HashSet checkedModules = repositoryTestList.getUniqueModules();
                    HashSet checkedTestTypes = repositoryTestList.getUniqueTestTypes();
                    HashSet checkedTestBags = repositoryTestList.getUniqueTestBags();
                    // try to load this testrun xmlbean (but only if does not have children already)
                    if (run.xmlel_TestBag == null) {
                        // if there are no testbags in the top report,
                        // then something is probably wrong
                        // it looks like there are no results for this testrun 
                        // it might be cause by compile errors, etc ...
                        
                        debugInfo("checkTestResults(): no results available ... ");                        
                        
                        //throw new Exception("loading testBags info not yet implemented (pathToReport="+pathToReport+") ");
                    } else {
                        // we have testBags info - let's examine them
                        for (int j=0; j<run.xmlel_TestBag.length; j++) {
                            TestBag testBag = run.xmlel_TestBag[j];
                            if (checkedModules.contains(testBag.xmlat_module)&
                            checkedTestTypes.contains(testBag.xmlat_testType)&
                            checkedTestBags.contains(testBag.xmlat_name)) {
                                debugInfo("checkTestResults(): this testBag is a candidate "+testBag.xmlat_bagID);
                                // filter the testIDs if we need to examine it in details
                                TestID testBagFilter = TestID.createFilter(repositoryFilter);
                                testBagFilter.setModuleName(testBag.xmlat_module);
                                testBagFilter.setTestType(testBag.xmlat_testType);
                                testBagFilter.setTestBagName(testBag.xmlat_name);
                                UniqueTestList testBagTestList = repositoryTestList.filterBy(testBagFilter);
                                HashSet checkedSuites = testBagTestList.getUniqueSuites();
                                if (testBagTestList.size()>0) {
                                    debugInfo("checkTestResults(): need to examine testBag in details ");
                                    // now load the testBag info file and process suites !!!!
                                    if (testBag.xmlel_UnitTestSuite == null) {
                                        // try to load the UnitTestSuite info - from testRun
                                        File testBagFile = ResultsUtils.getTestBagFile(reportRootDir,run,testBag);
                                        TestBag loadedTestBag = ResultsUtils.getTestBag(testBagFile);
                                        debugInfo("checkTestResults(): testBag loaded");
                                        // since loadedTestBag is at least existing object, assign it
                                        testBag = loadedTestBag;
                                    }
                                    // 2nd try - we might loaded suites a while ago
                                    if (testBag.xmlel_UnitTestSuite != null) {
                                        // now process suites
                                        for (int k = 0; k < testBag.xmlel_UnitTestSuite.length ; k++) {
                                            UnitTestSuite testSuite = testBag.xmlel_UnitTestSuite[k];
                                            if (checkedSuites.contains(testSuite.xmlat_name)) {
                                                debugInfo("checkTestResults(): examining suite "+testSuite.xmlat_name);
                                                TestID suiteFilter = TestID.createFilter(testBagFilter);
                                                suiteFilter.setSuiteName(testSuite.xmlat_name);
                                                UniqueTestList suiteTestList = testBagTestList.filterBy(suiteFilter);
                                                if (testSuite.xmlel_UnitTestCase == null) {
                                                    // try to load the UnitTestCases info - from testSuite
                                                    File testSuiteFile = ResultsUtils.getTestSuiteFile(reportRootDir,run,testBag,testSuite);
                                                    UnitTestSuite loadedTestSuite = ResultsUtils.getUnitTestSuite(testSuiteFile);
                                                    debugInfo("checkTestResults(): testSuite loaded");
                                                    // since loadedTestBag is at least existing object, assign it
                                                    testSuite = loadedTestSuite;
                                                }
                                                // 2nd try - we might loaded a test cases suite a while ago
                                                if (testSuite.xmlel_UnitTestCase != null) {
                                                    // now check all the testcases which shoule be failing
                                                    for (int l=0; l<testSuite.xmlel_UnitTestCase.length; l++) {
                                                        UnitTestCase testCase = testSuite.xmlel_UnitTestCase[l];
                                                        TestID testCaseID = new TestID(run,testBag,testSuite,testCase);
                                                        if (suiteTestList.contains(testCaseID)) {
                                                            // we have the test case - add status to matrix
                                                            debugInfo("checkTestResults(): getting status of test "+testCaseID+" :" +testCase.xmlat_result);
                                                            if (failuresMatrix.setCheckedTest(testCaseID) != FailuresMatrix.NO_TEST) {     
                                                                // this is just a hack - this should be redesigned
                                                                String pathToFailure = pathToReport+"/"+run.xmlat_runID+"/"+testBag.xmlat_bagID+"/"+
                                                                           PEConstants.HTMLRESULTS_DIR+"/"+PEConstants.TESTSUITES_SUBDIR+"/"+
                                                                           "TEST-"+testSuite.xmlat_name+".html"+"#"+testCase.xmlat_name;
                                                                failuresMatrix.setResult(testCase.xmlat_result,pathToFailure);
                                                                debugInfo("checkTestResults(): result set!");
                                                            } else {
                                                                debugInfo("checkTestResults(): cannot find TestID in Matrix = weird !!!");
                                                            }
                                                        }
                                                        
                                                    }
                                                }
                                            }
                                            // 2nd try - we might loaded suites a while ago
                                            if (testBag.xmlel_UnitTestSuite != null) {
                                            }
                                        }
                                    }
                                } else {
                                    debugInfo("checkTestResults(): don't have to care about this testBag ");
                                }
                            } else {
                                debugInfo("checkTestResults(): don't need to check testbag "+testBag.xmlat_bagID);
                            }
                        }
                    }
                } else {
                    debugInfo("checkTestResults(): don't need to check "+run.xmlat_name+" :"+i);
                }
            }
        }        
    }
    
    public static class FailuresMatrix {
        //public static final int
        public final static int NO_BUILD = -1;
        public final static int NO_TEST = -1;
        private String builds[];
        private ArrayList testIDs;
        private UniqueTestList testList;
        private String failuresMatrix[][];
        private String pathsMatrix[][];
        private int checkedBuildIDX = NO_BUILD;
        private int checkedTestIDX = NO_TEST;
        
        public FailuresMatrix(UniqueTestList testIDs, String[] builds) {
            this.builds = builds;
            this.testList = testIDs;
            this.testIDs = testIDs.toArrayList();
            failuresMatrix = new String[testIDs.size()][builds.length];
            pathsMatrix = new String[testIDs.size()][builds.length];
        }
        
        public int setCheckedBuild(String checkedBuild) {
            if (checkedBuild != null) {
                for (int i=0; i<builds.length ; i++) {
                    if (checkedBuild.equals(builds[i])) {
                        checkedBuildIDX = i;
                        return i;
                    }
                }
            }
            checkedBuildIDX = NO_BUILD;
            return NO_BUILD;
        }
        
        public int setCheckedTest(TestID testID) {
            if (testID != null) {
                checkedTestIDX = testIDs.indexOf(testID);
            } else {
                checkedTestIDX = NO_TEST;
            }
            return checkedTestIDX;
        }
        
        
                
        /*
        public void setBuilds(String builds[]) {
            this.builds = builds;
        }
         */
        
        public String[] getBuilds() {
            return builds;
        }
        
        /*
        public void setTestIDs(UniqueTestList testIDs) {
            this.testIDs = testIDs;
        }
         */
        
        
        public UniqueTestList getTestIDs() {
            return testList;
        }
         
        
        public boolean setResult(TestID testID, String result, String path) {
            setCheckedTest(testID);
            return setResult(result, path);
        }
        
        public boolean setResult(String result, String path) {
            return setResult(checkedTestIDX,checkedBuildIDX,result, path);
        }
        
        private boolean setResult(int testID, int buildID, String result, String path) {
            if ((testID!=NO_TEST)&(buildID!=NO_BUILD)) {
                failuresMatrix[testID][buildID] = result;
                pathsMatrix[testID][buildID] = path;
                return true;
            } else {
                return false;
            }
        }
        
        public XTestHistoryMatrix toXTestHistoryMatrix() {
            XTestHistoryMatrix xthm = new XTestHistoryMatrix();
            HMTest tests[] = new HMTest[failuresMatrix.length];
            xthm.xmlel_HMTest = tests;
            for (int i=0; i < failuresMatrix.length; i++) {
                HMTest test = new HMTest();
                tests[i] = test;
                TestID id = (TestID) testIDs.get(i);
                test.xmlat_class = id.testClass;
                test.xmlat_name = id.testName;
                test.xmlat_testType = id.testType;
                test.xmlat_testBagName = id.testBagName;
                test.xmlat_module = id.moduleName;
                test.xmlat_repositoryName = id.repositoryName;
                test.xmlat_suiteName = id.suiteName;
                // probably others will need to be added                
                HMTestedBuild testedBuilds[] = new HMTestedBuild[failuresMatrix[i].length];
                test.xmlel_HMTestedBuild = testedBuilds;
                for (int j=0; j < failuresMatrix[i].length; j++) {                    
                    HMTestedBuild testedBuild = new HMTestedBuild();
                    testedBuild.xmlat_build = builds[j];
                    testedBuild.xmlat_result = failuresMatrix[i][j];
                    testedBuild.xmlat_path = pathsMatrix[i][j];
                    testedBuilds[j] = testedBuild;
                }       
            }
            return xthm;
        }
        
    }
    
    
    
    // TestID class - this class should be a full ID of a XTest based Test (hopefully)
    public static class TestID {
        public static final String EMPTY_NAME = "";
        public static final String ALL = "*";
        private String testName=EMPTY_NAME;
        private String testClass=EMPTY_NAME;
        private String suiteName=EMPTY_NAME;
        private String testType=EMPTY_NAME;
        private String testBagName=EMPTY_NAME;
        private String moduleName=EMPTY_NAME;
        private String repositoryName=EMPTY_NAME;
        
        // empty constructor
        public TestID() {
        }
        
        public TestID(TestRun testRun, TestBag testBag, UnitTestSuite testSuite, UnitTestCase testCase) {
            testName = testCase.xmlat_name;
            testClass = testCase.xmlat_class;
            suiteName = testSuite.xmlat_name;
            testType = testBag.xmlat_testType;
            testBagName = testBag.xmlat_name;
            moduleName = testBag.xmlat_module;
            repositoryName = testRun.xmlat_name;
        }
        
        // create new TestID object for filtering purposes
        public static TestID createFilter() {
            TestID filter = new TestID();
            filter.testName = ALL;
            filter.testClass = ALL;
            filter.testBagName = ALL;
            filter.suiteName = ALL;
            filter.testType = ALL;
            filter.moduleName = ALL;
            filter.repositoryName = ALL;
            return filter;
        }
        
        // create a new filter from exisiting one
        // I should be more wise and use Cloneable interface !!!
        // TBD!
        public static TestID createFilter(TestID existingFilter) {
            TestID filter = new TestID();
            filter.testName = existingFilter.testName;
            filter.testClass = existingFilter.testClass;
            filter.testBagName = existingFilter.testBagName;
            filter.suiteName = existingFilter.suiteName;
            filter.testType = existingFilter.testType;
            filter.moduleName = existingFilter.moduleName;
            filter.repositoryName = existingFilter.repositoryName;
            return filter;
        }
        
        private String handleNullString(String aString) {
            if (aString == null) {
                return EMPTY_NAME;
            } else {
                return aString;
            }
        }
        
        private boolean wildCardStringEqual(String s1, String s2) {
            if (s1.equals(ALL)|s2.equals(ALL)) return true;
            return s1.equals(s2);
        }
        
        // getters/setters
        public void setTestName(String testName) {
            this.testName = handleNullString(testName);
        }
        
        public String getTestName() {
            return this.testName;
        }
        
        public void setTestClass(String testClass) {
            this.testClass = handleNullString(testClass);
        }
        
        public String getTestClass() {
            return this.testClass;
        }
        
        public void setSuiteName(String suiteName) {
            this.suiteName = handleNullString(suiteName);
        }
        
        public String getSuiteName() {
            return this.suiteName;
        }
        
        public void setTestType(String testType) {
            this.testType = handleNullString(testType);
        }
        
        public String getTestType() {
            return this.testType;
        }
        
        public void setTestBagName(String testBagName) {
            this.testBagName = handleNullString(testBagName);
        }
        
        public String getTestBagName() {
            return this.testBagName;
        }
        
        public void setModuleName(String moduleName) {
            this.moduleName = handleNullString(moduleName);
        }
        
        public String getModuleName() {
            return this.moduleName;
        }
        
        public void setRepositoryName(String repositoryName) {
            this.repositoryName = handleNullString(repositoryName);
        }
        
        public String getRepositoryName() {
            return this.repositoryName;
        }
        
        // equals
        public boolean equals(Object obj) {
            //System.out.println("!!!! XXX");
            if (obj == null) return false;
            if (!(obj instanceof TestID)) return false;
            TestID id = (TestID)obj;
            if (!wildCardStringEqual(testName,id.getTestName())) return false;
            if (!wildCardStringEqual(testClass,id.getTestClass())) return false;
            if (!wildCardStringEqual(suiteName,id.getSuiteName())) return false;
            if (!wildCardStringEqual(testType,id.getTestType())) return false;
            if (!wildCardStringEqual(testBagName,id.getTestBagName())) return false;
            if (!wildCardStringEqual(moduleName,id.getModuleName())) return false;
            if (!wildCardStringEqual(repositoryName,id.getRepositoryName())) return false;
            //System.out.println("!!!! XXXX TESTID equals:"+this+"::"+id);
            return true;
        }
        
        // hashCode - for hashMap
        public int hashCode() {
            return this.toString().hashCode();
        }
        
        // toString method
        public String toString() {
            StringBuffer result = new StringBuffer("TestID: repositoryName=");
            result.append(repositoryName);
            result.append(", moduleName=");
            result.append(moduleName);
            result.append(", testBagName=");
            result.append(testBagName);
            result.append(", testType=");
            result.append(testType);
            result.append(", suiteName=");
            result.append(suiteName);
            result.append(", testClass=");
            result.append(testClass);
            result.append(", testName=");
            result.append(testName);
            return result.toString();
        }
    }
    
    
    
    // Unique testlist class
    public static class UniqueTestList {
        private HashSet testList = new HashSet();
        
        public boolean add(TestID testID) {
            return testList.add(testID);
        }
        
        public int size() {
            return testList.size();
        }
        
        public boolean contains(Object o) {
            return testList.contains(o);
        }
        
        public ArrayList toArrayList() {
            return new ArrayList(testList);
        }
        
        public HashSet getUniqueRepositories() {
            Iterator i = testList.iterator();
            HashSet result = new HashSet();
            while (i.hasNext()) {
                result.add(((TestID)i.next()).repositoryName);
            }
            return result;
        }
        
        public HashSet getUniqueModules() {
            Iterator i = testList.iterator();
            HashSet result = new HashSet();
            while (i.hasNext()) {
                result.add(((TestID)i.next()).moduleName);
            }
            return result;
        }
        
        public HashSet getUniqueTestBags() {
            Iterator i = testList.iterator();
            HashSet result = new HashSet();
            while (i.hasNext()) {
                result.add(((TestID)i.next()).testBagName);
            }
            return result;
        }
        
        public HashSet getUniqueTestTypes() {
            Iterator i = testList.iterator();
            HashSet result = new HashSet();
            while (i.hasNext()) {
                result.add(((TestID)i.next()).testType);
            }
            return result;
        }
        
        public HashSet getUniqueSuites() {
            Iterator i = testList.iterator();
            HashSet result = new HashSet();
            while (i.hasNext()) {
                result.add(((TestID)i.next()).suiteName);
            }
            return result;
        }
        
        public HashSet getUniqueTestClasses() {
            Iterator i = testList.iterator();
            HashSet result = new HashSet();
            while (i.hasNext()) {
                result.add(((TestID)i.next()).testClass);
            }
            return result;
        }
        
        public HashSet getUniqueTestNames() {
            Iterator i = testList.iterator();
            HashSet result = new HashSet();
            while (i.hasNext()) {
                result.add(((TestID)i.next()).testName);
            }
            return result;
        }
        
        public UniqueTestList filterBy(TestID id) {
            Iterator i = testList.iterator();
            UniqueTestList result = new UniqueTestList();
            while (i.hasNext()) {
                TestID aID = (TestID)i.next();
                if (id.equals(aID)) {
                    result.add(aID);
                }
            }
            return result;
        }
    }
    
    // not used stuff
    /*
    public static class UniqueTestList {
        private HashMap testListStructure;
        private final static String EMPTY_NAME = "";
        public UniqueTestList() {
        }
     
        public boolean addTest(TestID id) {
            return false;
        }
     
     
        private HashMap addTestSuite(String suiteName, String testRunName, String moduleName) {
            String name = checkName(suiteName);
     
            return null;
        }
     
        private HashMap addTestBag(String testRunName, String testType, String testBagName, String moduleName) {
            return null;
        }
     
     
        private HashMap addTestRun(String testRunName) {
            String name = checkName(testRunName);
            if (!testListStructure.containsKey(name)) {
                testListStructure.put(name,new HashMap());
            }
            return (HashMap)testListStructure.get(name);
        }
     
        private static String checkName(String name) {
            if (name == null) {
                return EMPTY_NAME;
            } else {
                return name;
            }
        }
    }
     */
    
    
}
