/*
 *
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
package org.netbeans.xtest.testrunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import junit.extensions.TestDecorator;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.netbeans.junit.Manager;
import org.netbeans.junit.NbPerformanceTest;
import org.netbeans.junit.NbTest;
import org.netbeans.junit.NbTestCase;
import org.netbeans.xtest.pe.xmlbeans.Data;
import org.netbeans.xtest.pe.xmlbeans.PerformanceData;
import org.netbeans.xtest.pe.xmlbeans.UnitTestCase;
import org.netbeans.xtest.pe.xmlbeans.UnitTestSuite;
import org.netbeans.xtest.pe.xmlbeans.XMLBean;
import org.netbeans.xtest.util.SerializeDOM;

/**
 * Creates XML results. Listens for failures and errors and handles error
 * messages, times and success rates.
 *
 * @author  mb115822
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

    private static final String DID_NOT_FINISH_TEST = "Did not finish.";
    private static final String DID_NOT_START_TEST = "Did not start.";
    private static final String UNKNOWN_TEST = "unknown";
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
       // check whether result was already set to pass (it can happen when addFailure is called after endTest)
        if(currentTestCase.xmlat_result == UnitTestCase.TEST_PASS) {
            testsPassed--;
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
        // test didn't hangs so it isn't error.
        testsErrors--;
            
        // did the test used workdir ?
        if (test instanceof NbTestCase) {
            NbTestCase nbtest = (NbTestCase)test;
            String workdir = nbtest.getWorkDirPath();
            File workdirFile = new File(workdir);
            if (workdirFile.exists()) {
                String rootWorkdir = Manager.getWorkDirPath()+File.separator;
                String relativePath = XMLBean.cutPrefix(workdir,rootWorkdir);
                currentTestCase.xmlat_workdir = relativePath;
            }
        }
        
        currentTestCase.xmlat_time = System.currentTimeMillis() - caseTime;
        if (currentTestCase.xmlat_result.equals(UnitTestCase.TEST_UNKNOWN)) {
            // test didn't fail or finished with error -> it passed :-)
            currentTestCase.xmlat_result=UnitTestCase.TEST_PASS;
            // reset message
            currentTestCase.xmlat_message = "";
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
        //System.out.println("reporter:startTest() test="+test);
        if (test instanceof TestCase) {
            currentTestName = ((TestCase)test).getName();            
        } else {
            currentTestName = UNKNOWN_TEST;
        }
        currentClassName = test.getClass().getName();
        currentTestCase = null;
        // finds test case in list of test cases to be executed
        for (Iterator it = runTestCases.iterator(); it.hasNext();) {
            UnitTestCase testCaseBean = (UnitTestCase)it.next();
            if(testCaseBean.xmlat_class.equals(currentClassName) &&
                    testCaseBean.xmlat_name.equals(currentTestName) &&
                    testCaseBean.getMessage().equals(DID_NOT_START_TEST)) {
                currentTestCase = testCaseBean;
                break;
            }
        }
        if(currentTestCase == null) {
            // Test case not found. It can happen when the same test run for the second time
            // or a new test case is added in runtime.
            // we need to create a new test case bean
            currentTestCase = new UnitTestCase();
            currentTestCase.xmlat_name = currentTestName;
            currentTestCase.xmlat_class = currentClassName;
            currentTestCase.xmlat_result = UnitTestCase.TEST_UNKNOWN;
            // add the testcase to the current suite
            runTestCases.add(currentTestCase);
            currentTestSuite.xmlel_UnitTestCase = (UnitTestCase[])(runTestCases.toArray(new UnitTestCase[0]));
            testsTotal++;
            testsErrors++;
        }
        // Change message from "Did not start" to "Did not finish". Message is
        // then updated in endTest, addFailure or addError.
        currentTestCase.xmlat_message = DID_NOT_FINISH_TEST;

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

    /** The whole testsuite started. */
    public void startTestSuite(TestSuite suite) {
        //System.out.println("reporter:startTestSuite()-"+suite.getName());
        // reset the arrays ...
        runTestCases = new ArrayList();
        performanceData = new ArrayList();                
        // create new results file
        outFile = new File(resultsDirectory, "TEST-"+suite.getName()+".xml");

        currentTestSuite = new UnitTestSuite();
        suiteTime = System.currentTimeMillis();
        currentSuiteName = suite.getName();
        currentTestSuite.xmlat_name = currentSuiteName;
        currentTestSuite.xmlat_timeStamp = new java.sql.Timestamp(suiteTime);
        currentTestSuite.xmlat_unexpectedFailure = "Suite is not finished";
        currentTestSuite.xmlel_Data = new Data[]{new Data()};

        addTestCaseBeans(suite);
        currentTestSuite.xmlel_UnitTestCase = (UnitTestCase[])(runTestCases.toArray(new UnitTestCase[0]));

        testsTotal = runTestCases.size();
        testsPassed = 0;
        testsFailed = 0;
        testsErrors = testsTotal;
        testsUnexpectedPassed = 0;
        testsExpectedFailed = 0;
        saveCurrentSuite();
    }

    /** Recursively add test cases from suite to the list of test case beans. */
    private void addTestCaseBeans(TestSuite suite) {
        for (Enumeration e = suite.tests(); e.hasMoreElements(); ) {
            Test test = (Test)e.nextElement();
            // get a real test if it is only TestDecorator
            while(test instanceof TestDecorator) {
                test = ((TestDecorator)test).getTest();
            }
            if(test instanceof TestSuite) {
                TestSuite subSuite = (TestSuite)test;
                // add test cases from suite recursively
                addTestCaseBeans(subSuite);
                continue;
            }
            if(test instanceof NbTestCase) {
                NbTestCase nbTestCase = (NbTestCase)test;
                if(!nbTestCase.canRun()) {
                    // test case excluded in cfg file => do not add it to suite
                    continue;
                }
            }
            // now add test case bean to the list
            UnitTestCase testCaseBean = new UnitTestCase();
            if (test instanceof TestCase) {
                testCaseBean.xmlat_name = ((TestCase)test).getName();
            } else {
                testCaseBean.xmlat_name = UNKNOWN_TEST;
            }
            testCaseBean.xmlat_class = test.getClass().getName();
            testCaseBean.xmlat_result = UnitTestCase.TEST_UNKNOWN;
            testCaseBean.xmlat_message = DID_NOT_START_TEST;
            // add the testcase to the current suite
            runTestCases.add(testCaseBean);
        }
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
