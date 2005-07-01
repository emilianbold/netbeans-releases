/*
 *
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.xtest.testrunner;

import org.netbeans.xtest.testrunner.*;
import org.netbeans.xtest.pe.xmlbeans.*;
import org.netbeans.xtest.pe.*;
import org.netbeans.xtest.util.SerializeDOM;

import java.util.*;
import junit.framework.*;
import org.netbeans.junit.*;
import java.io.*;

/**
 *
 * @author  mb115822
 * @version 
 */
public class XMLReporter implements JUnitTestListener {

    private UnitTestSuite currentTestSuite;
    private UnitTestCase currentTestCase;
    private ArrayList runTestCases;
    private ArrayList performanceData;
    
    private long caseTime;
    private long suiteTime;
    
    private long testsTotal = 0;
    private long testsPassed = 0;
    private long testsFailed = 0;
    private long testsErrors = 0;
    private long testsUnexpectedPassed = 0;
    private long testsExpectedFailed = 0;
    
    private File resultsDirectory;
    private OutputStream outStream;
    private File outFile;
    
    private static String UNKNOWN_TEST = "unknown";
    private String currentTestName = UNKNOWN_TEST;
    private String currentClassName = UNKNOWN_TEST;
    private String currentSuiteName = UNKNOWN_TEST;
    
    /** Creates new XMLResultProcessor */
    public XMLReporter(File resultsDirectory) {
        //
        this.resultsDirectory = resultsDirectory;
        // 
    }
    
    public static String stackTraceToString(Throwable t) {
        StringWriter swr = new StringWriter();
        t.printStackTrace(new PrintWriter(swr,true));
        return swr.toString();        
    }
        
    
    public void addError(junit.framework.Test test, java.lang.Throwable throwable) {
        if (currentTestCase == null) {
            StringWriter s = new StringWriter();
            throwable.printStackTrace(new PrintWriter(s));
            currentTestSuite.xmlat_unexpectedFailure = s.toString();
            
            saveCurrentSuite();
            return;
        }
        // check whether result was already set (it can be when XTestErrorManager is registered)
        if(currentTestCase.xmlat_result == UnitTestCase.TEST_FAIL) {
            // error takes precedens before fail
            testsFailed--;
        }
        currentTestCase.xml_cdata = stackTraceToString(throwable);
        currentTestCase.xmlat_message = throwable.getMessage();
        currentTestCase.xmlat_result = UnitTestCase.TEST_ERROR;
        testsErrors ++;
    }
    
        
    public void addFailure(junit.framework.Test test, junit.framework.AssertionFailedError assertionFailedError) {
        // check whether result was already set (it can be when XTestErrorManager is registered)
        if(currentTestCase.xmlat_result == UnitTestCase.TEST_ERROR) {
            // error takes precedens before fail
            return;
        }
        currentTestCase.xml_cdata = stackTraceToString(assertionFailedError);
        currentTestCase.xmlat_message = assertionFailedError.getMessage();
        currentTestCase.xmlat_result = UnitTestCase.TEST_FAIL;
        testsFailed ++;

        if (test instanceof NbTest) {
           String exp_mesg = ((NbTest)test).getExpectedFail();
           if (exp_mesg != null) {
               currentTestCase.xmlat_result = UnitTestCase.TEST_EXPECTED_FAIL;
               currentTestCase.xmlat_failReason = exp_mesg;
               testsExpectedFailed++;
           }
        }
    }
    
    public void endTest(junit.framework.Test test) {       
        // moved to startTest
        //testsTotal++;        
        
        // test didn't hangs so it isn't error.
        testsErrors--;
            
        // did the test used workdir ?
        if (test instanceof NbTestCase) {
            NbTestCase nbtest = (NbTestCase)test;
            //try {
                String workdir = nbtest.getWorkDirPath();
                File workdirFile = new File(workdir);
                if (workdirFile.exists()) {
                    String rootWorkdir = Manager.getWorkDirPath()+File.separator;
                    String relativePath = XMLBean.cutPrefix(workdir,rootWorkdir);
                    currentTestCase.xmlat_workdir = relativePath;
                }
            //} catch (IOException ioe) {
                // no workdir is available ... 
            //}
        }
        
        currentTestCase.xmlat_time = System.currentTimeMillis() - caseTime;
        if (currentTestCase.xmlat_result.equals(UnitTestCase.TEST_UNKNOWN)) {
            // test didn't fail or finished with error -> it passed :-)
            currentTestCase.xmlat_result=UnitTestCase.TEST_PASS;
            testsPassed++;
            if (test instanceof NbTest) {
               String exp_mesg = ((NbTest)test).getExpectedFail();
               if (exp_mesg != null) {
                   currentTestCase.xmlat_result = UnitTestCase.TEST_UNEXPECTED_PASS;
                   currentTestCase.xmlat_failReason = exp_mesg;
                   testsUnexpectedPassed++;
               }
            }
        }
        //add data to the current suite
        if (test instanceof NbPerformanceTest) {
            NbPerformanceTest.PerformanceData pdata[] = ((NbPerformanceTest)test).getPerformanceData();
            for (int i=0; pdata!=null&&i<pdata.length; i++) {
                PerformanceData bean = new PerformanceData();
                bean.xmlat_name = pdata[i].name;
                bean.xmlat_value = pdata[i].value;
                bean.xmlat_unit = pdata[i].unit;
                bean.xmlat_runOrder = pdata[i].runOrder;
                bean.xmlat_threshold = pdata[i].threshold;
                performanceData.add(bean);
            }
            currentTestSuite.xmlel_Data[0].xmlel_PerformanceData = (PerformanceData[])(performanceData.toArray(new PerformanceData[0]));
        }
        
        // save the result
        saveCurrentSuite();
        
    }
    
    public void startTest(junit.framework.Test test) {
        testsTotal++;     
        // if test hangs, it will be counted as error.
        testsErrors++;
        
        //System.out.println("reporter:startTest()");
        currentTestCase = new UnitTestCase();
        if (test instanceof TestCase) {
            currentTestName = ((TestCase)test).getName();            
        } else {
            currentTestName = UNKNOWN_TEST;
        }
        currentTestCase.xmlat_name = currentTestName;
        
        currentClassName = test.getClass().getName();
        currentTestCase.xmlat_class = currentClassName;
        
        currentTestCase.xmlat_result = UnitTestCase.TEST_UNKNOWN;
        // anybody knows why we decided to use this ?
        //currentTestCase.xmlat_test = "x";        
        // add the testcase to the current suite
        runTestCases.add(currentTestCase);        
        currentTestSuite.xmlel_UnitTestCase = (UnitTestCase[])(runTestCases.toArray(new UnitTestCase[0]));
        
        // here comes the statistics for up to date results
        currentTestSuite.xmlat_testsTotal = testsTotal;
        currentTestSuite.xmlat_testsPass = testsPassed;
        currentTestSuite.xmlat_testsFail = testsFailed;
        currentTestSuite.xmlat_testsError = testsErrors;
        currentTestSuite.xmlat_testsUnexpectedPass = testsUnexpectedPassed;
        currentTestSuite.xmlat_testsExpectedFail = testsExpectedFailed;

        // now get the suite out -- suite is still not completed
        saveCurrentSuite();
        
        caseTime = System.currentTimeMillis();
    }

    
    
    private void recreateOutput() throws IOException {
        // cannot recreate output if outStream is not a file
        if (outFile == null) {
            throw new IOException("outFile is not specified - cannot create test results files");
        }
        // close the file first
        if (outStream != null) {
            outStream.close();
        }
        // if file exists, delete (is this required ???)
        if (outFile.exists()) {
            if (!outFile.delete()) {
                throw new IOException("cannot delete file "+outFile);
            }
            //outFile.createNewFile();
        }
        // create the file and output stream
        this.outStream = new FileOutputStream(outFile);
    }
    
    
    /**
     * The whole testsuite ended.
     */
    public void endTestSuite(TestSuite suite, TestResult suiteResult) {
        // add remaining data and write it to a file
        //System.out.println("reporter:endTestSuite()");
        //System.err.println("xmlel_UnitTestCase:"+currentTestSuite.xmlel_UnitTestCase);
        currentTestSuite.xmlat_time = System.currentTimeMillis() - suiteTime;
        // here comes the statistics
        currentTestSuite.xmlat_testsTotal = testsTotal;
        currentTestSuite.xmlat_testsPass = testsPassed;
        currentTestSuite.xmlat_testsFail = testsFailed;
        currentTestSuite.xmlat_testsError = testsErrors;
        currentTestSuite.xmlat_testsUnexpectedPass = testsUnexpectedPassed;
        currentTestSuite.xmlat_testsExpectedFail = testsExpectedFailed;
        // suite is finished
        currentTestSuite.xmlat_unexpectedFailure = null;
        
        // now get it out !!!
        saveCurrentSuite();
    }
    
    /**
     * The whole testsuite started.
     */
    public void startTestSuite(TestSuite suite) {
        //System.out.println("reporter:startTestSuite()");
        // reset the arrays ...
        //testSuites = new ArrayList();
        runTestCases = new ArrayList();
        performanceData = new ArrayList();                
        // create new results file
        outFile = new File(resultsDirectory, "TEST-"+suite.getName()+".xml");
        //
        currentTestSuite = new UnitTestSuite();
        suiteTime = System.currentTimeMillis();
        currentSuiteName = suite.getName();
        currentTestSuite.xmlat_name = currentSuiteName;
        currentTestSuite.xmlat_timeStamp = new java.sql.Timestamp(suiteTime);
        currentTestSuite.xmlat_unexpectedFailure = "Suite is not finished";
        currentTestSuite.xmlel_Data = new Data[]{new Data()};
        testsTotal = 0;
        testsPassed = 0;
        testsFailed = 0;
        testsErrors = 0;
        testsUnexpectedPassed = 0;
        testsExpectedFailed = 0;
        saveCurrentSuite();        
    }
    
    private boolean saveCurrentSuite() {
        try {
            //System.out.println("reporter:saveCurrentSuite()");
            org.w3c.dom.Document doc = null;
            if (currentTestSuite == null) {
                // there is nothing to save
                System.err.println("Trying to save a suite.xml, but no current test suite is defined");
                return false;
            }
            doc = currentTestSuite.toDocument();
            //System.err.println("Try to serialize doc to :"+outStream);
            // better to recreate output
            recreateOutput();
            // serialize !!!
            SerializeDOM.serializeToStream(doc,outStream);
            // make sure all data are flushed
            outStream.flush();
            // better close it as well
            outStream.close();
            outStream = null;
            
        } catch (IOException ioe) {
            System.err.println("XMLResultProcessor.endTestSuite(): Unable to write out test suite in XML: IOException:");
            ioe.printStackTrace(System.err);
            cleanOutStreamAndFile();
            return false;
        } catch (Exception e) {
            System.err.println("XMLResultProcessor.endTestSuite(): Unable to write out test suite in XML: XMLBean exception:");
            e.printStackTrace();
            cleanOutStreamAndFile();
            return false;
        }
        //System.out.println("reporter:suiteSavedOk()");
        return true;
    }
    
    
    private boolean cleanOutStreamAndFile() {
        boolean result = true;
        if (outStream != null) {
            try {
                outStream.close();
                outStream = null;
            } catch (IOException ioe) {
                System.err.println("XMLResultProcessor.cleanOutStreamAndFile - cannot close ative stream from file"+outFile);
                result = false;
            }
        }
        if (outFile.exists()) {
            if (outFile.length() == 0) {
                if (!outFile.delete()) {
                    System.err.println("XMLResultProcessor.cleanOutStreamAndFile - cannot delete empty file:"+outFile);
                    result = false;
                }
                
            }
        }
        return result;
    }
}
