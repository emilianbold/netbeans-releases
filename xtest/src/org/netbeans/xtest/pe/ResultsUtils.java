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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * ResultsUtils.java
 *
 * Created on November 22, 2001, 2:58 PM
 */

package org.netbeans.xtest.pe;

import org.netbeans.xtest.pe.xmlbeans.*;
import org.netbeans.xtest.util.FileUtils;
import org.netbeans.xtest.util.SerializeDOM;
import org.w3c.dom.*;
import java.io.*;
import java.util.*;





/**
 *
 * @author  mb115822
 * @version
 */
public class ResultsUtils {

    // debugging flag - should be set to false :-)
    private static final boolean DEBUG = false;
    private static final void debugInfo(String message) {
        if (DEBUG) System.out.println("ResultsUtils."+message);
    }
      
    
    public static final int NOT_A_DIR = -1;
    public static final int UNKNOWN_DIR = 0;
    public static final int TESTREPORT_DIR = 1;
    public static final int TESTRUN_DIR = 2;
    public static final int TESTBAG_DIR = 3;   
    public static final int TESTBAG_WORKDIR = 4;
    
    public static int resolveResultsDir(File dir) { 
        debugInfo("resolveResultsDir(): resolving dir:"+dir);
        // test for not a dir
        if (!dir.isDirectory()) {
            debugInfo("resolveResultsDir(): not a dir");
            return NOT_A_DIR;
        }
        //
        String dirName = dir.getName();
        // test for testreport dir
        if ((new File(dir,PEConstants.XMLRESULTS_DIR+File.separator+PEConstants.TESTREPORT_XML_FILE)).exists()) {
            debugInfo("resolveResultsDir(): testreport dir");
            return TESTREPORT_DIR;
        }
        // test for testrun dir
        if (dirName.startsWith(PEConstants.TESTRUN_DIR_PREFIX)) {
            if ((new File(dir,PEConstants.TESTRUN_XML_FILE)).exists()) {
                debugInfo("resolveResultsDir(): testrun dir");
                return TESTRUN_DIR;
            }
        }        
        // test for testbag dir
        if (dirName.startsWith(PEConstants.TESTBAG_DIR_PREFIX)) {
            if ((new File(dir,PEConstants.XMLRESULTS_DIR+File.separator+PEConstants.TESTBAG_XML_FILE)).exists()) {
                debugInfo("resolveResultsDir(): testbag dir");
                return TESTBAG_DIR;
            }            
        }
        // test for testbag workdir
        if ((new File(dir,PEConstants.XMLRESULTS_DIR+File.separator+PEConstants.TESTBAG_XML_FILE)).exists()) {
            debugInfo("resolveResultsDir(): possible testbag workdir");
            return TESTBAG_DIR;
        }         
        // not recognized dir
        debugInfo("resolveResultsDir(): unknown dir :-(");
        return UNKNOWN_DIR;
    }
    
    
    // lists suites in suite dir - it easilly takes only filenames and 
    // get their names - suite has usually TEST-${suitename}.xml 
    // this method returns only ${suitename}s
    // this method believes, that in suite dir can be only suite files    
    public static String[] listSuites(File suiteDir) {
        String[] suiteFilenames = suiteDir.list();
        if (suiteFilenames == null) return null;
        String[] suiteNames = new String[suiteFilenames.length];
        int count=0;
        for (int i=0;i<suiteFilenames.length;i++) {        
            if ((suiteFilenames[i].startsWith("TEST-")&&(suiteFilenames[i].endsWith(".xml")))) {
                String suiteName=suiteFilenames[i].substring(5,suiteFilenames[i].length()-4);
                debugInfo("listSuites(): suiteName = "+suiteName);
                suiteNames[count]=suiteName;
                count++;
            }
        }
        //
        if (count!=suiteFilenames.length) {
            debugInfo("listSuites() in directory were also different files than suites");
        }
        //
        return suiteNames;
    }
    
    
    // looks for all testbag under this test run
    // examines only directories beginning with testbag_ prefix
    // it should also check presence testbag.xml in the testbag root dir
    public static File[] listTestBags(File testRunRoot) {
        File[] testBagDirs = testRunRoot.listFiles();
        Arrays.sort(testBagDirs);
        int count=0;
        for (int i=0; i<testBagDirs.length;i++) {
            debugInfo("listTestBags(): examining "+testBagDirs[i]);
            if (testBagDirs[i].getName().startsWith(PEConstants.TESTBAG_DIR_PREFIX)) {
                // also check whether testbag.xml exist in this testbag root dir
                File testbagFile = new File(testBagDirs[i],PEConstants.XMLRESULTS_DIR+File.separator+PEConstants.TESTBAG_XML_FILE);
                if (testbagFile.exists()) {
                    debugInfo("listTestBags(): testbag looks valid");
                    count++;
                } else {
                    debugInfo("listTestBags(): testbag is not valid - does not contain testbag.xml file");
                    testBagDirs[i]=null;
                }
            } else {
                debugInfo("listTestBags(): testbag directory name is not valid - does not begin with testbag_ prefix");
                testBagDirs[i]=null;
            }
        }
        // now create new array without nulls
        File[] validTestBags = new File[count];        
        int newCount = 0;
        for (int i=0;i<testBagDirs.length;i++) {
            if (testBagDirs[i]!=null) {
                validTestBags[newCount]=testBagDirs[i];
                newCount++;
            }
        }
        return validTestBags;
    }

    
    // looks for all testruns under for this reports
    // examines only directories beginning with testrun_ prefix
    // it should also check presence testrun.xml in the testrun dir
    public static File[] listTestRuns(File testReportRoot) {
        File[] testRunDirs = testReportRoot.listFiles();
        Arrays.sort(testRunDirs);
        int count=0;
        for (int i=0; i<testRunDirs.length;i++) {
            debugInfo("listTestRuns(): examining "+testRunDirs[i]);
            if (testRunDirs[i].getName().startsWith(PEConstants.TESTRUN_DIR_PREFIX)) {
                count++;
                debugInfo("listTestRuns(): testRun dir looks ok - although it does not have to contain testrun.xml");
            } else {
                debugInfo("listTestRuns(): testrun directory name is not valid - does not begin with testrun_ prefix");
                testRunDirs[i]=null;
            }
        }
        // now create new array without nulls
        File[] validTestRuns = new File[count];        
        int newCount = 0;
        for (int i=0;i<testRunDirs.length;i++) {
            if (testRunDirs[i]!=null) {
                validTestRuns[newCount]=testRunDirs[i];
                newCount++;
            }
        }
        return validTestRuns;
    }
    
    // get HTML results dir - if does not exist and create=true, create it
    public static final File getHTMLResultDir(File rootDir, boolean create) throws IOException {
        File resultDir = new File(rootDir,PEConstants.HTMLRESULTS_DIR);
        if (resultDir.exists()) {
            if (resultDir.isDirectory()) {
                return resultDir;
            } else {
                throw new IOException("File "+PEConstants.HTMLRESULTS_DIR+" exist, but is not a directory");
            }
        } else {
            if (create) {
                if (resultDir.mkdirs()) {
                    return resultDir;
                } else {
                    throw new IOException("Cannot create :"+PEConstants.HTMLRESULTS_DIR+" directory");
                }
            } else {
                throw new IOException("Cannot open "+rootDir+"/"+PEConstants.HTMLRESULTS_DIR+" directory");
            }
        }        
    }
    
    // backward compatibility method
    public static final File getHTMLResultDir(File rootDir) throws IOException {
        return getHTMLResultDir(rootDir,true);
    }
    
    
    // get XML results dir - if does not exist and create=true, create it
    public static final File getXMLResultDir(File rootDir, boolean create) throws IOException {
        File resultDir = new File(rootDir,PEConstants.XMLRESULTS_DIR);
        if (resultDir.exists()) {
            if (resultDir.isDirectory()) {
                return resultDir;
            } else {
                throw new IOException("File "+PEConstants.XMLRESULTS_DIR+" exist, but is not a directory");
            }
        } else {
            if (create) {
                if (resultDir.mkdirs()) {
                    return resultDir;
                } else {
                    throw new IOException("Cannot create :"+PEConstants.XMLRESULTS_DIR+" directory");
                }
            } else {
                throw new IOException("Cannot open "+rootDir+"/"+PEConstants.XMLRESULTS_DIR+" directory");
            }
        }        
    }
    
    // backward compatibility method
    public static final File getXMLResultDir(File rootDir) throws IOException {
        return getXMLResultDir(rootDir,true);
    }
    
    

   public static Document getDOMDocFromFile(File file) throws IOException {
        //return SerializeDOM.parseFile(file);
        return SerializeDOM.parseFile(file);
    }
    
    public static XTestResultsReport getXTestResultsReport(File reportFile) {
        try {
            debugInfo("getXTestResultsReport(): file="+reportFile);
            Document doc = getDOMDocFromFile(reportFile);
            debugInfo("getXTestResultsReport(): god Document");
            //XMLBean.DEBUG=true;
            XMLBean xmlBean = XMLBean.getXMLBean(doc);    
            //XMLBean.DEBUG=false;
            debugInfo("getXTestResultsReport(): got XMLBean");  
            if (xmlBean instanceof XTestResultsReport) {
                debugInfo("getXTestResultsReport(): got XTestResultsReport");  
                return (XTestResultsReport)xmlBean;
            } else {
                debugInfo("getXTestResultsReport(): have to create new XTestResultsReport (XMLBean is not the required type)");  
                return new XTestResultsReport();
            }
        } catch (Exception e) {
            debugInfo("getXTestResultsReport(): EXCEPTION!!!"+e);
            //e.printStackTrace();
            //XMLBean.DEBUG=false;
            debugInfo("getXTestResultsReport(): have to create new XTestResultsReport!");  
            return new XTestResultsReport();
        }
     }
    
    
    public static TestRun getTestRun(File testRunFile) {
        try {
            Document doc = getDOMDocFromFile(testRunFile);
            XMLBean xmlBean = XMLBean.getXMLBean(doc);    
            if (xmlBean instanceof TestRun) {
                return (TestRun)xmlBean;
            } else {
                return new TestRun();
            }
        } catch (Exception e) {
            return new TestRun();
        }
    }
    
    
    public static TestBag getTestBag(File testBag) throws Exception {
        if (!testBag.exists()) {
            return new TestBag();
        }
        try {
            return TestBag.loadFromFile(testBag);
        } catch (Exception e) {
            // move the old testbag file to *.broken file
            File brokenTestBagFile = new File(testBag.getAbsolutePath()+".broken");
            FileUtils.copyFile(testBag, brokenTestBagFile);
            // install a new testbag ...
            TestBag aTestBag = new TestBag();
            String testBagID = testBag.getParentFile().getParentFile().getName();
            aTestBag.setUnexpectedFailure("XTest results reporter installed empty testbag.xml file, because the original one was corrupted. Exception caught :"
                        +e.getMessage());
            aTestBag.setBagID(testBagID);
            aTestBag.setModule("Unknown");
            aTestBag.setExecutor("Unknown");
            aTestBag.setName("Unknown");
            aTestBag.saveXMLBean(testBag);
            return aTestBag;
        }
    }
    
    
    public static UnitTestSuite getUnitTestSuite(File suiteFile) throws Exception {
        try {
            UnitTestSuite aSuite = UnitTestSuite.loadFromFile(suiteFile);
            return aSuite;
        } catch (Exception e) {
            // there was som problem with getting the suite - create the new one
            // and put the message of the exception to the unexpcectedMessage field
            UnitTestSuite aSuite = new UnitTestSuite();
            // move the old suite file to *.broken file
            File brokenSuiteFile = new File(suiteFile.getAbsolutePath()+".broken");
            FileUtils.copyFile(suiteFile, brokenSuiteFile);
            // get the name of the suite (from the filename)
            // assume the suite is always named as TEST-{suitename}.xml
            int beginIndex = "TEST-".length();
            int endIndex = suiteFile.getName().lastIndexOf(".xml");
            String suiteName = suiteFile.getName().substring(beginIndex,endIndex);
            aSuite.setName(suiteName);
            aSuite.setUnexpectedFailure("XTest results reporter installed empty suite file, because the original one was corrupted. Exception caught :"
                        +e.getMessage());
            // save the suite
            aSuite.saveXMLBean(suiteFile);
            // return it 
            return aSuite;
        }
    }
    
    
    
    public static UnitTestSuite[] getUnitTestSuites(File suiteDir) throws Exception {       
        //File suiteDir = inputDir;
        // scan directory
        File[] suiteFiles = FileUtils.listFiles(suiteDir,null,".xml");
        Arrays.sort(suiteFiles);
        debugInfo("getUnitTestSuites(File):"+suiteFiles);
        ArrayList suiteList = new ArrayList();
        for (int i=0; i< suiteFiles.length; i++) {
            try {
                suiteList.add(getUnitTestSuite(suiteFiles[i]));
            } catch (Exception e) {
                // exception !!!
            }
        }
        // now convert the arraylist into plain array
        return (UnitTestSuite[])(suiteList.toArray(new UnitTestSuite[0]));
    }
        
    //
    public static File getTestRunFile(File reportRoot, TestRun testRun) {
        File testRunDir = new File(reportRoot,testRun.xmlat_runID);
        File testRunXMLDir = new File(testRunDir,PEConstants.XMLRESULTS_DIR);
        File testRunFile = new File(testRunXMLDir,PEConstants.TESTRUN_XML_FILE);
        return testRunFile;
    }
    
    
    public static File getTestBagFile(File reportRoot, TestRun testRun, TestBag testBag) {
        File testRunDir = new File(reportRoot,testRun.xmlat_runID);
        File testBagDir = new File(testRunDir,testBag.xmlat_bagID);
        File testBagXMLDir = new File(testBagDir,PEConstants.XMLRESULTS_DIR);
        File testBagFile = new File(testBagXMLDir,PEConstants.TESTBAG_XML_FILE);
        return testBagFile;
    }
    
    public static File getUnitTestSuiteFile(File reportRoot, TestRun testRun, TestBag testBag, UnitTestSuite testSuite) {
        File testRunDir = new File(reportRoot,testRun.xmlat_runID);
        File testBagDir = new File(testRunDir,testBag.xmlat_bagID);
        File testBagXMLDir = new File(testBagDir,PEConstants.XMLRESULTS_DIR);
        File testSuitesDir = new File(testBagXMLDir,PEConstants.TESTSUITES_SUBDIR);
        File testSuiteFile = new File(testSuitesDir,"TEST-"+testSuite.xmlat_name+".xml");
        return testSuiteFile;
    }
    
    // backward compatibiliy method- should be deprecated
    public static File getTestSuiteFile(File reportRoot, TestRun testRun, TestBag testBag, UnitTestSuite testSuite) {
        return getUnitTestSuiteFile(reportRoot,testRun,testBag,testSuite);
    }
    
    
    
    /** loads whole report from the given reportRoot file (directory) 
     * @param reportRoot - directory where report is stored
     * @param divided - if false, method expects the whole report is saved in a single file, if true
     *      the usuall directory structure is searched
     * @returns the whole report 
     */
    public static XTestResultsReport loadXTestResultsReport(File reportRoot, boolean divided) throws IOException, ClassNotFoundException {
        if (!reportRoot.isDirectory()) {
            throw new IOException("specified reportRoot is not a valid directory: "+reportRoot);
        }
        // get the root XTestResultsReport
        File xtrFile = new File(getXMLResultDir(reportRoot,false),PEConstants.TESTREPORT_XML_FILE);
        FileUtils.checkFileIsFile(xtrFile);
        
        // get the results report 
        XTestResultsReport xtr = XTestResultsReport.loadFromFile(xtrFile);               
        
        // report is not divided, so we're done :-)
        if (!divided) {
            return xtr;
        }
        
        // there's nothing to do if test run is empty (broken report?)
        if (xtr.xmlel_TestRun == null) {
            // is this correct?
            return xtr;
        }
                
        // load test runs
        for (int i=0; i< xtr.xmlel_TestRun.length; i++) {            
            File aTestRunFile = getTestRunFile(reportRoot, xtr.xmlel_TestRun[i]);
            FileUtils.checkFileIsFile(aTestRunFile);
            // ok - replace the the test run with the testrun with children
            TestRun aTestRun = TestRun.loadFromFile(aTestRunFile);
            xtr.xmlel_TestRun[i] = aTestRun;

            // continue with testbags
            if (aTestRun.xmlel_TestBag != null) {                
                for (int j=0; j < aTestRun.xmlel_TestBag.length; j++) {
                    File aTestBagFile = getTestBagFile(reportRoot, aTestRun, aTestRun.xmlel_TestBag[j]);
                    FileUtils.checkFileIsFile(aTestBagFile);
                    TestBag aTestBag = TestBag.loadFromFile(aTestBagFile);
                    aTestRun.xmlel_TestBag[j] = aTestBag;
                       
                    // continue with suites
                    if (aTestBag.xmlel_UnitTestSuite != null) {
                        for (int k=0; k < aTestBag.xmlel_UnitTestSuite.length ; k++) {
                            File anUnitTestSuiteFile = getUnitTestSuiteFile(reportRoot,aTestRun,aTestBag,aTestBag.xmlel_UnitTestSuite[k]);
                            FileUtils.checkFileIsFile(anUnitTestSuiteFile);
                            UnitTestSuite anUnitTestSuite = UnitTestSuite.loadFromFile(anUnitTestSuiteFile);
                            aTestBag.xmlel_UnitTestSuite[k] = anUnitTestSuite;
                        }
                    }
                    // test suites done
                }
                
            }
            // test bags done
            
        }   
        // test runs done
        // indicate the report is full
        xtr.xmlat_fullReport = true;
        return xtr;
    }
    
    
    /** loads whole report from the given reportRoot file (directory) 
     * @param reportRoot - directory where xtest report is stored (the whole structure is expected)
     * @returns loaded report
     */
    public static XTestResultsReport loadXTestResultsReport(File reportRoot) throws IOException, ClassNotFoundException {    
        return loadXTestResultsReport(reportRoot, true);
    }
}
