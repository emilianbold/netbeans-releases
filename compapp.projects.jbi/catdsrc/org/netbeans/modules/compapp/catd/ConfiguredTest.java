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

package org.netbeans.modules.compapp.catd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.Source;
import javax.xml.transform.OutputKeys;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.soap.SOAPException;
import javax.xml.soap.MimeHeaders;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerException;
import javax.xml.parsers.ParserConfigurationException;
import junit.framework.TestCase;
import junit.framework.TestResult;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.IgnoreTextAndAttributeValuesDifferenceListener;
import org.xml.sax.SAXException;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.netbeans.modules.xml.xdm.diff.Add;
import org.netbeans.modules.xml.xdm.diff.XDMUtil;
import org.netbeans.modules.xml.xdm.diff.Difference;
import org.netbeans.modules.xml.xdm.diff.Change;
import org.netbeans.modules.xml.xdm.diff.Change.AttributeChange;
import org.netbeans.modules.xml.xdm.diff.Delete;
import org.netbeans.modules.xml.xdm.nodes.Attribute;
import javax.swing.text.BadLocationException;

//import org.netbeans.junit.NbTestCase;
//import org.netbeans.modules.compapp.catd.jbimanager.EnvironmentVariableHandler;
import org.netbeans.modules.sun.manager.jbi.management.AdministrationService;

/**
 * Test the HTTP SOAP processing
 * @author aegloff
 * @author rselvaraj
 */
public class ConfiguredTest extends TestCase {
    public final static String COMPARISON_TYPE_IDENTICAL = "identical"; // NOI18N
    public final static String COMPARISON_TYPE_BINARY = "binary"; // NOI18N
    public final static String COMPARISON_TYPE_EQUALS = "equals"; // NOI18N
    public final static String COMPARISON_TYPE_SKELETON = "skeleton"; // compares xml elements ignoring text and attribute values
    
    private static final String TEST_IN_PROGRESS_VAL = "progress"; // NOI18N
    private static final String TEST_IN_PROGRESS_KEY = "featurestatus"; // NOI18N
    
    private static final SimpleDateFormat mSDF = new SimpleDateFormat("yyyyMMddHHmmss"); // NOI18N
    
    private static final String OVERWRITE_EMPTY_OUTPUT_MSG =
            "The expected output file for this test case was empty. " +
            "The most recent output is saved as the test case's expected output file for comparison during later test runs."; // NOI18N
    
    public final static String NS_PREFIX = "xmlns"; // NOI18N
    public final static String SCHEMA_LOCATION = "schemaLocation"; // NOI18N
        
    private SOAPConnectionFactory mSoapConnFactory;
    private SOAPConnection mConnection;
    private MessageFactory mMessageFactory;
    
    private String mName;
    private Properties mProperties;
    private boolean mGenerateOutputOnSuccess = true;
    private static final XDMUtil xdmUtil = new XDMUtil();
    private String mIndent;
    static String EMPTY = ""; // NOI18N
    
    public ConfiguredTest(String name, String methodName) {
        super(methodName);
        mName = name;
    }
    
    public ConfiguredTest(String name, String methodName, Properties testProperties) {
        this(name, methodName);
        this.mProperties = testProperties;
        mIndent = "  "; // NOI18N
    }
    
    /**
     * Gets the name of a TestCase
     * @return returns a String
     */
    public String getName() {
        return mName;
    }
    
    /**
     * Sets the name of a TestCase
     * @param name The name to set
     */
    public void setName(String name) {        
        mName= name;        
    }
        
    protected void setUp() throws java.lang.Exception {
        // Set up the mConnection and factories
        mSoapConnFactory = SOAPConnectionFactory.newInstance();
        mConnection = mSoapConnFactory.createConnection();
        mMessageFactory = MessageFactory.newInstance();
        
        // TEST
        //System.setProperty ("sun.net.client.defaultReadTimeout", "2000");
        //java.net.Socket.setSoLinger(true, 0);
    }
    
    protected void tearDown() throws java.lang.Exception {
        mConnection.close();
        mConnection = null;
        // TEST close of socket
        // Thread.sleep(10000);
    }
    
    public static junit.framework.Test suite() throws Exception {
        
        Properties props = System.getProperties();
        System.out.println(props);
        
        //Load the list of tests that needs to be exercised. The list is a comma seperated
        //value list. Refer to org.netbeans.modules.compapp.test.ui.actions.TestcaseTestAction
        //to know more about how the properties are set.
        Properties testcasesProps = loadProperties("test/selected-tests.properties");
        String testCasesCSV = (String)testcasesProps.get("testcases");
        String [] testCaseNames = testCasesCSV.split(",");
        
        junit.framework.TestSuite suite = new junit.framework.TestSuite();
        
        // Create a test for each relevant .properties file
        final String testPropertiesPostfix = ".properties"; // NOI18N
        
        FileFilter testPropertiesFilter = new FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory()) return false;
                return f.getName().endsWith(testPropertiesPostfix);
            }
        };
        
        // We want to make sure we don't test the same test case more than once.
        // This is for backward compatibility. Some existing projects might have the 
        // same test case defined more than once in the selected-tests.properties 
        // file due to an old bug, which was forgiven previously when we didn't
        // keep the test case order.
        Set<String> processedTestCases = new HashSet<String>();         
        
        // We want to keep the order specified in the selected-tests.properties file
        for (int i = 0; i < testCaseNames.length; i++) { 
            String testCaseName = testCaseNames[i];
            
            if (testCaseName == null || testCaseName.trim().length() == 0 ||
                    processedTestCases.contains(testCaseName)) {
                continue;
            }
            
            File testCaseDir = new File("test/" + testCaseName);
            if (!testCaseDir.isDirectory()) {
                continue;
            }
            
            String inputDirName = testCaseDir.getName();
            String inputDirAbsolutePath = testCaseDir.getAbsolutePath();
            
            File[] testPropertiesFiles = testCaseDir.listFiles(testPropertiesFilter);
            if (testPropertiesFiles == null) {
                continue;
            }
            
            processedTestCases.add(testCaseName);
            
            for (int testCnt = 0; testCnt < testPropertiesFiles.length; testCnt++) {
                String testPropertiesFile = testPropertiesFiles[testCnt].getAbsolutePath();
                String testName = inputDirName;
                Properties testProps = loadProperties(testPropertiesFile);
                testProps.put("testpropertiesfilename", testPropertiesFiles[testCnt].getName());
                testProps.put("absoluteinputdir", inputDirAbsolutePath);
                testProps.put("inputdirname", inputDirName);                
                
                // Invoke.properties files define simple service invocation tests
                if (testPropertiesFile.endsWith("Invoke.properties")) {
                    suite.addTest(new ConfiguredTest(testName, "testInboundSOAPRequest", testProps));
                }
                
                // Concurrent.properties files define concurrency service invocation tests
                else if (testPropertiesFile.endsWith("Concurrent.properties")) {
                    suite.addTest(new ConfiguredTest(testName, "testConcurrentSOAPRequest", testProps));
                }
                
                // FaultHandling.properties files define tests of BC error handling
                else if (testPropertiesFile.endsWith("FaultHandling.properties")) {
                    suite.addTest(new ConfiguredTest(testName, "testFaultHandlingSOAPRequest", testProps));
                }
                
                // FaultHandling.properties files define tests of BC error handling
                else if (testPropertiesFile.endsWith("N2M.properties")) {
                    suite.addTest(new ConfiguredTest(testName, "testN2MInboundSOAPRequest", testProps));
                }
                
                // Feed.properties files define file feeder tests
                else if (testPropertiesFile.endsWith("Feed.properties")) {
                    suite.addTest(new ConfiguredTest(testName, "testFileRequest", testProps));
                }
                
                // Ftp.properties files define ftp bc driver tests
                else if (testPropertiesFile.endsWith("Ftp.properties")) {
                    suite.addTest(new ConfiguredTest(testName, "testFtpRequest", testProps));
                }

                // Correlation.properties files define concurrency service invocation tests
                else if (testPropertiesFile.endsWith("Correlation.properties")) {
                    suite.addTest(new ConfiguredTest(testName, "testCorrelationSOAPRequest", testProps));
                }
                
                // Correlation.properties files define concurrency service invocation tests
                else if (testPropertiesFile.endsWith("ConcurrentCorrelation.properties")) {
                    suite.addTest(new ConfiguredTest(testName, "testConcurrentCorrelationSOAPRequest", testProps));
                }

                // conc_correlation.properties files define concurrency service invocation tests
                else if (testPropertiesFile.endsWith("conc_correlation.properties")) {
                    suite.addTest(new ConfiguredTest(testName, "testConcCorrelationSOAPRequest", testProps));
                }
            }
        }
        
        return suite;
    }

    protected static String stackTraceElementToString( StackTraceElement [] ste ) {
    	String s = "";
    	for ( int i = 0; i < ste.length; i++ ) {
    	    s = s  + ste[i].toString() + "\n";
    	}
    	return s;
    }
    
    protected static void initSuite(String testSuitePath, String suiteName ) throws Exception {
        boolean success = (new File(testSuitePath + File.separator + suiteName)).mkdir();
        if ( !success ) {
            throw new Exception("Failed to create directory " + testSuitePath + File.separator + suiteName);
        }
    }
    
    protected static void initTest(String testSuitePath, String suiteName, String testName ) throws Exception {
    	String testDir = testSuitePath + File.separator + suiteName + File.separator + testName;
        boolean success = (new File(testDir)).mkdir();
        if ( !success ) {
            throw new Exception("Failed to create directory " + testDir);
        }        
    }
    
    protected static void copyTestResourceFile(File srcFile, File destFile)  throws Exception {

    	InputStream in = null;
        OutputStream out = null;

        boolean isExists = destFile.exists();
        if ( isExists ) {
            destFile.delete();
        }
        destFile.createNewFile();
		
    	try {

            in = new FileInputStream(srcFile);
            out = new FileOutputStream(destFile);

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) >= 0) {
                    out.write(buffer, 0, bytesRead);
            }
            out.close();
            in.close();
    	} catch ( Throwable th ) {
            try { 
                if ( out != null ) {
                        out.close();
                }
                if ( out != null ) {
                        in.close();
                }
            } catch ( Throwable th2 ) {
            }
    	}
    }

    protected static void writeStringToFile(File destFile, String content) throws Exception  {

        boolean isExists = destFile.exists();
        if ( isExists ) {
            destFile.delete();
        }
        destFile.createNewFile();

        OutputStream out = null;
        OutputStreamWriter ow = null;

    	try {

            out = new FileOutputStream(destFile);
            ow = new OutputStreamWriter(out);

            ow.write(content);
            ow.flush();
            ow.close();

            out.close();
    	} catch ( Throwable th ) {
            try { 
                if (out != null) {
                    out.close();
                }
                if (ow != null) {
                    ow.close();
                }
            } catch ( Throwable th2 ) {    			
            }    		
    	}
    }
    
    protected static void logResultsForBuild(Properties properties, String suitePath, String suiteName, String testName, String value, String errorString) throws Exception {    
        String outputHome = System.getenv("BUILD_OUTPUT_HOME");
        try {
            if ( outputHome != null ) {
                logResults( properties, suitePath, suiteName, testName, value, errorString);
            }
        } catch ( Exception e) {
            throw e;
    	}
    }

    protected static void logResults(Properties properties, String suitePath, String suiteName, String testName, String aValue, String errorString) throws Exception {
    	// first, copy all the files to a given dir 
    	String destDir = suitePath + File.separator + suiteName + File.separator + testName;
        String srcDir = (String)properties.get("absoluteinputdir");

        File source = new File(srcDir);
        
        File[] inputDir = source.listFiles();
        
        if (inputDir != null) {
            for (int count = 0; count < inputDir.length; count++) {
                File currentFile = inputDir[count];

                if ( !currentFile.isDirectory() ) { 
                    copyTestResourceFile(currentFile, new File(destDir + File.separator + currentFile.getName()));
                }
            }
        }

        if ( errorString != null ) {
            File errorFile = new File(suitePath + File.separator + suiteName + File.separator + testName + File.separator + "error.txt");
            writeStringToFile(errorFile, errorString);
        }
        
    	// Comment the cycle part. It will be used Later
    	//String valuePropertyFile = mSuitePath + File.separator + mCycle + "." + value + ".properties";
    	String valuePropertyFile = suitePath + File.separator + aValue + ".properties";
    	    	
    	// create one if is does not exists
    	boolean exists = (new File(valuePropertyFile)).exists();
        if (exists) {
 
        } else {
            File propFile = new File(valuePropertyFile);
            try {
                 propFile.createNewFile();
            } catch ( Exception e ) {
            }
        }
        // Update Information.
        Properties prop = new Properties();
        FileInputStream fis = new FileInputStream(valuePropertyFile);
        prop.load(fis);
        fis.close();
        
        String tests = prop.getProperty("list");
        
        String content = suiteName + ":" + testName;
       
        if (tests == null) {
            tests = content;
        } else {
            tests = tests + ", " + content;
        }
        
        FileOutputStream fos = new FileOutputStream(valuePropertyFile);
        prop.setProperty("list",tests);
        prop.store(fos,"");
        fos.close();
        
    }    
    
    
    protected static File[] getDirList(File baseDir) {
        
        List<File> retValue = new ArrayList<File>();
        if (baseDir.isDirectory()) {
            retValue.add(baseDir);
            
            File[] children = baseDir.listFiles();
            for (int ii = 0; ii < children.length; ii++) {
                retValue.addAll(Arrays.asList(getDirList(children[ii])));
            }
        }
        return retValue.toArray(new File[0]);
    }
    
    protected static String getRelativePath(File root, File subDir)
    throws Exception {
        String rootPath = root.getCanonicalPath();
        String subDirPath = subDir.getCanonicalPath();
        int index = subDirPath.indexOf(rootPath);
        if (index != -1) {
            return subDirPath.substring(index + rootPath.length() + 1,
                    subDirPath.length());
        } else {
            // Error!
        }
        return subDirPath;
    }
    
    /**
     * Test of inbound SOAP Request processing.
     */
    public void testInboundSOAPRequest() throws Exception {
        String destination = mProperties.getProperty("destination");
        String description = mProperties.getProperty("description");
        String soapAction = mProperties.getProperty("soapaction");
        String inputFileName = mProperties.getProperty("inputfile");
        String outputFileName = mProperties.getProperty("outputfile");
        String comparisonType = mProperties.getProperty("comparisontype");
        String testPropertiesFileName = mProperties.getProperty("testpropertiesfilename");
        String inputDir = mProperties.getProperty("absoluteinputdir");
        String inputDirName = mProperties.getProperty("inputdirname");
        String debugStr = mProperties.getProperty("debug");
        String featureProgressVal = mProperties.getProperty(TEST_IN_PROGRESS_KEY);
        if (featureProgressVal != null && featureProgressVal.equals(TEST_IN_PROGRESS_VAL)) {
            System.out.println("Test " + inputDirName + "\\" + testPropertiesFileName
                    + " in Development");
            return;
        }
        boolean logDetails = false;
        if (debugStr != null && Boolean.valueOf(debugStr) == Boolean.TRUE) {
            logDetails = true;
        }
        
        System.out.print("Test " + inputDirName + "\\" + testPropertiesFileName);
        System.out.flush();
        
        try {
            if (logDetails) {
                System.out.println("testInboundSOAPRequest Running " + testPropertiesFileName + " : " + description);
                System.out.println("Test destination: " + destination);
            }
            
            String inputFile = inputDir + File.separator + inputFileName;
            //--String expectedOutputFile = new File("output" + File.separator + inputDirName, outputFileName).getAbsolutePath();
            String expectedOutputFile = null;
            if (outputFileName != null) {
                expectedOutputFile = new File(inputDir, outputFileName).getAbsolutePath();
            }
            
            String logPrefix = inputDirName + "\\" + testPropertiesFileName + ":";
            sendAndCheck(logPrefix, logDetails, destination, inputFile, expectedOutputFile, null, null, comparisonType, soapAction);
            
            //System.out.println("Test " + inputDirName + "\\" + testPropertiesFileName + " Passed.");
            System.out.println(" Passed.");
        } catch (Exception ex) {
            System.out.println(" Failed.");
            throw ex;
        } catch (Error er) {
            System.out.println(" Failed.");
            throw er;
        }
    }
    
    /**
     * Test of BC error handling: setting of appropriate HTTP status or Faults
     */
    public void testFaultHandlingSOAPRequest() throws Exception {
        String description = mProperties.getProperty("description");
        String destination = mProperties.getProperty("destination");
        String soapAction = mProperties.getProperty("soapaction");
        String inputFileName = mProperties.getProperty("inputfile");
        String expectedHttpStatus = mProperties.getProperty("httpstatuscode");
        String expectedHttpWarning = mProperties.getProperty("httpwarning");
        String outputFileName = mProperties.getProperty("outputfile");
        String comparisonType = mProperties.getProperty("comparisontype");
        String testPropertiesFileName = mProperties.getProperty("testpropertiesfilename");
        String inputDir = mProperties.getProperty("absoluteinputdir");
        String inputDirName = mProperties.getProperty("inputdirname");
        String debugStr = mProperties.getProperty("debug");
        String featureProgressVal = mProperties.getProperty(TEST_IN_PROGRESS_KEY);
        if (featureProgressVal != null && featureProgressVal.equals(TEST_IN_PROGRESS_VAL)) {
            System.out.println("Test " + inputDirName + "\\" + testPropertiesFileName
                    + " in Development");
            return;
        }
        boolean logDetails = false;
        if (debugStr != null && Boolean.valueOf(debugStr) == Boolean.TRUE) {
            logDetails = true;
        }
        
        System.out.print("Test " + inputDirName + "\\" + testPropertiesFileName);
        
        try {
            if (logDetails) {
                System.out.println("testFaultHandlingSOAPRequest Running " + testPropertiesFileName + " : " + description);
                System.out.println("Test destination: " + destination);
            }
            
            String inputFile = inputDir + File.separator + inputFileName;
            String expectedOutputFile = null;
            if (expectedOutputFile != null) {
                //--expectedOutputFile = new File("output" + File.separator + inputDirName, outputFileName).getAbsolutePath();
                expectedOutputFile = new File(inputDir, outputFileName).getAbsolutePath();
            }
            String logPrefix = inputDirName + "\\" + testPropertiesFileName + ":";
            
            sendAndCheck(logPrefix, logDetails, destination, inputFile, expectedOutputFile, expectedHttpStatus, expectedHttpWarning, comparisonType, soapAction);
            
            System.out.println(" Passed.");
        } catch (Exception ex) {
            System.out.println(" Failed.");
            throw ex;
        } catch (Error er) {
            System.out.println(" Failed.");
            throw er;
        }
    }
    
    /**
     * Test of inbound SOAP Request processing.
     */
    public void testConcurrentSOAPRequest() throws Exception {
        
        String description = mProperties.getProperty("description");
        String destination = mProperties.getProperty("destination");
        String soapAction = mProperties.getProperty("soapaction");
        //String concurrentInvokesStr = mProperties.getProperty("concurrentinvokes");
        String concurrentInvokesStr = mProperties.getProperty("concurrentthreads");
        String invokesPerThreadStr = mProperties.getProperty("invokesperthread");
        String comparisonType = mProperties.getProperty("comparisontype");
        String testTimeoutStr = mProperties.getProperty("testtimeout");
        String testPropertiesFileName = mProperties.getProperty("testpropertiesfilename");
        String inputDir = mProperties.getProperty("absoluteinputdir");
        String inputDirName = mProperties.getProperty("inputdirname");
        //--String testDirName = inputDir + File.separator + "test" + File.separator;
        String calculateThroughputStr = mProperties.getProperty("calculatethroughput");
        String debugStr = mProperties.getProperty("debug");
        String featureProgressVal = mProperties.getProperty(TEST_IN_PROGRESS_KEY);
        if (featureProgressVal != null && featureProgressVal.equals(TEST_IN_PROGRESS_VAL)) {
            System.out.println("Test " + inputDirName + "\\" + testPropertiesFileName
                    + " in Development");
            return;
        }
        int concurrentInvokes = Integer.parseInt(concurrentInvokesStr);
        int testTimeout = Integer.parseInt(testTimeoutStr);        
        if ("true".equals(System.getProperty("inDebug"))) {
            testTimeout = 0; // disable timeout
        }
        //System.out.println("timeout: " + testTimeout);
        
        int invokesPerThread = 1;
        if (invokesPerThreadStr != null) {
            invokesPerThread = Integer.parseInt(invokesPerThreadStr);
        }
        boolean calculateThroughput = false;
        if (calculateThroughputStr != null && Boolean.valueOf(calculateThroughputStr) == Boolean.TRUE) {
            calculateThroughput = true;
        }
        boolean logDetails = false;
        if (debugStr != null && Boolean.valueOf(debugStr) == Boolean.TRUE) {
            logDetails = true;
        }
        
        System.out.print("Test " + inputDirName + "\\" + testPropertiesFileName);
        System.out.flush();
        
        try {
            // Determine all input and corresponding output files
            LinkedHashMap<String, String> inputToOutputFileNames = new LinkedHashMap<String, String>();
            Set entries = mProperties.entrySet();
            Iterator iter = entries.iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                String key = (String) entry.getKey();
                if (key.startsWith("inputfile")) {
                    String inputFileName = (String) entry.getValue();
                    // Find the corresponding outputfile
                    String fileId = key.substring("inputfile".length());
                    String expectedOutputProperty = "outputfile" + fileId;
                    String outputFileName = mProperties.getProperty(expectedOutputProperty);
                    if (outputFileName != null) {
                        inputToOutputFileNames.put(inputFileName, outputFileName);
                    } else {
                        throw new Exception("Test set up error. No corresponding property to define output file found for input file property " + key + ". Identified unique id is: " + fileId + " expected output file property: " + expectedOutputProperty);
                    }
                }
            }
            
            if (logDetails) {
                System.out.println("testConcurrentSOAPRequest Running " + testPropertiesFileName + " : " + description);
                System.out.println("Test destination: " + destination);
            }
            
            Map.Entry[] inOutEntries = inputToOutputFileNames.entrySet().toArray(new Map.Entry[0]);
            
            Thread[] threads = new Thread[concurrentInvokes];
            ConcurrentTestSendOnlyRunnable[] runnables = new ConcurrentTestSendOnlyRunnable[concurrentInvokes];
            
            for (int threadCount = 0; threadCount < concurrentInvokes; threadCount++) {
                // round robin of input/output files
                Map.Entry entry = inOutEntries[(threadCount%inOutEntries.length)];
                String inputFileName = (String) entry.getKey();
                String outputFileName = (String) entry.getValue();
                String threadName = "Concurrent test thread " + threadCount;
                String logPrefix = inputDirName + "\\" + testPropertiesFileName + " - " + threadName + ":";
                String inputFile = inputDir + File.separator + inputFileName;
                SOAPMessage message = loadMessage(logPrefix, logDetails, inputFile);
                //--String expectedOutputFile = new File("output" + File.separator + inputDirName, outputFileName).getAbsolutePath();
                String expectedOutputFile = new File(inputDir, outputFileName).getAbsolutePath();
                ConcurrentTestSendOnlyRunnable runnable = new ConcurrentTestSendOnlyRunnable(inputDirName, message, expectedOutputFile, destination, testPropertiesFileName, invokesPerThread, soapAction);
                runnables[threadCount] = runnable;
                threads[threadCount] = new Thread(runnable);
                threads[threadCount].setName(threadName);
            }
            
            for (int threadCount = 0; threadCount < concurrentInvokes; threadCount++) {
                threads[threadCount].start();
            }
            
            long startTime = System.currentTimeMillis();
            long timeoutTime = startTime + (testTimeout * 1000);
            long threadCountStuck=0;
            long threadCountSuccess=0;
            long threadCountError=0;
            long duration = 0;
            String detail = "";
            for (int threadCount = 0; threadCount < concurrentInvokes; threadCount++) {
                long remainingTime = (timeoutTime - System.currentTimeMillis());
                // if infinite timeout or there is remaining time, wait for thread join.
                if (testTimeout == 0 || remainingTime >= 0) {
                    threads[threadCount].join(testTimeout * 1000);
                }
                if (threads[threadCount].isAlive()) {
                    threadCountStuck++;
                    detail = detail + "<" + threads[threadCount].getName() + ">"
                            + " did not complete. Success rate "
                            + runnables[threadCount].getInvocationsDone() + "/"
                            + runnables[threadCount].getInvocationCount() + " \n";
                }  else {
                    //System.out.println("Thread " + threadCount + " completed.");
                    Throwable encountered = runnables[threadCount].getThrowableEncountered();
                    if (encountered != null) {
                        threadCountError++;
                        String message = encountered.getMessage();
                        detail = detail + "<" + threads[threadCount].getName() + ">"
                                + " is Errored. Success rate "
                                + runnables[threadCount].getInvocationsDone() + "/"
                                + runnables[threadCount].getInvocationCount() + " \n";
                    } else{
                        threadCountSuccess++;
                        duration = duration + runnables[threadCount].getDuration();
                        SOAPMessage[] replies = runnables[threadCount].getReplies();
                        for (int replyCount = 0; replyCount < replies.length; replyCount++) {
                            String testExpectedOutputFileName = runnables[threadCount].getExpectedOutputFileName();
                            String logPrefix = inputDirName + "\\" + testPropertiesFileName
                                    + " - " + threads[threadCount].getName() + ":";
                            SOAPMessage reply = replies[replyCount];
                            checkExpectedOutput(logPrefix, logDetails, reply, testExpectedOutputFileName, comparisonType);
                        }
                        
                    }
                }
            }
            String result = " Passed.";
            result += " Threads count Success: <" + threadCountSuccess + "> Error: <"
                    + threadCountError + "> Not completed: <" + threadCountStuck + ">";
            
            if (calculateThroughput) {
                int totalInvocations = invokesPerThread * concurrentInvokes;
                double timeInSecs = duration/1000.0;
                double invocationsPerSecond = totalInvocations / timeInSecs;
                result += " Throughput: <" + totalInvocations + "> invocations in <"
                        + timeInSecs + ">s = <" + invocationsPerSecond +  "> invokes/s";
            }
            if(detail.length() > 0){
                result = result + "\nDetails: \n" + detail ;
                //test case failed so asser it to false
                assertTrue(result, false);
            } else {
                System.out.println(result);
            }
        } catch (Exception ex) {
            System.out.println(" Failed.");
            throw ex;
        } catch (Error er) {
            System.out.println(" Failed.");
            throw er;
        }
    }
    
    /**
     * Test of inbound SOAP Request processing.
     */
    public void testCorrelationSOAPRequest() throws Exception {
        
        String featureProgressVal = mProperties.getProperty(TEST_IN_PROGRESS_KEY);
        //String featureProgressVal = null;
        String testPropertiesFileName = mProperties.getProperty("testpropertiesfilename");
        String inputDirName = mProperties.getProperty("inputdirname");
        if (featureProgressVal != null && featureProgressVal.equals(TEST_IN_PROGRESS_VAL)) {
            System.out.println("Test " + inputDirName + "\\" + testPropertiesFileName
                    + " in Development");
            return;
        }
        String description = mProperties.getProperty("description");        
        //String concurrentInvokesStr = mProperties.getProperty("concurrentinvokes");
        String comparisonType = "#" + mProperties.getProperty("comparisontype");
        String testTimeoutStr = mProperties.getProperty("testtimeout");
        String inputDir = mProperties.getProperty("absoluteinputdir");
        String calculateThroughputStr = mProperties.getProperty("calculatethroughput");
        String debugStr = mProperties.getProperty("debug");
        boolean calculateThroughput = false;
        if (calculateThroughputStr != null && Boolean.valueOf(calculateThroughputStr) == Boolean.TRUE) {
            calculateThroughput = true;
        }
        boolean logDetails = /*true*/false;
        if (debugStr != null && Boolean.valueOf(debugStr) == Boolean.TRUE) {
            logDetails = true;
        }
        int testTimeout = 0;
        if (testTimeoutStr != null && !testTimeoutStr.trim().equals("")) {
            testTimeout = Integer.parseInt(testTimeoutStr);
        }
        if ("true".equals(System.getProperty("inDebug"))) {
            testTimeout = 0; // disable timeout
        }
        System.out.print("Test " + inputDirName + "\\" + testPropertiesFileName);
        System.out.flush();
        
        try {
            // Determine all input and corresponding output files
            List<Map<String, String>> individualPropMaps = new ArrayList<Map<String, String>>();
            Map<String, String> individualPropMap = null;
            Set<Map.Entry<Object, Object>> entries = mProperties.entrySet();
            Iterator<Map.Entry<Object, Object>> iter = entries.iterator();
            while (iter.hasNext()) {
                Map.Entry<Object, Object> entry = iter.next();
                String key = (String) entry.getKey();
                if (key.startsWith("inputfile")) {
                    String inputFileName = (String) entry.getValue();
                    // Find the corresponding outputfile
                    String fileId = key.substring("inputfile".length());
                    String expectedOutputProperty = "outputfile" + fileId;
                    String outputFileName = mProperties.getProperty(expectedOutputProperty);
                    String expectedDestination = "destination" + fileId;
                    String expectedDestinationVal = mProperties.getProperty(expectedDestination);
                    String soapAction = "soapaction" + fileId;
                    String soapActionVal = mProperties.getProperty(soapAction);
                    
                    if (outputFileName == null || expectedDestinationVal == null) {
                        throw new Exception("Test set up error. No corresponding property to define output file found for input file property " + key + ". Identified unique id is: " + fileId + " expected output file property: " + expectedOutputProperty);
                    } else {
                        individualPropMap = new HashMap<String, String>();
                        individualPropMap.put("inputfile", inputFileName);
                        individualPropMap.put("outputfile", outputFileName);
                        individualPropMap.put("destination", expectedDestinationVal);
                        individualPropMap.put("soapaction", soapActionVal);
                        
                        individualPropMaps.add(individualPropMap);
                    }
                }
            }
            
            if (logDetails) {
                System.out.println("testCorrelationSOAPRequest Running " + testPropertiesFileName + " : " + description);
            }
            int totalNumberOfDestinations = individualPropMaps.size();
            Thread[] threads = new Thread[totalNumberOfDestinations];
            ConcurrentTestSendOnlyRunnable[] runnables = new ConcurrentTestSendOnlyRunnable[totalNumberOfDestinations];
            Map<String, String> individualDest = null;
            for (int threadCount = 0; threadCount < totalNumberOfDestinations; threadCount++) {
                // round robin of input/output files
                individualDest = individualPropMaps.get(threadCount);
                String inputFileName = individualDest.get("inputfile");
                String outputFileName = individualDest.get("outputfile");
                String destination = individualDest.get("destination");
                String soapAction = individualDest.get("soapaction");
                
                String threadName = "Correlation test thread " + threadCount;
                String logPrefix = inputDirName + "\\" + testPropertiesFileName + " - " + threadName + ":";
                String inputFile = inputDir + File.separator + inputFileName;
                SOAPMessage message = loadMessage(logPrefix, logDetails, inputFile);
                String expectedOutputFile = "";
                if(! ("".equals(outputFileName))){
                    //--expectedOutputFile = new File("output" + File.separator + inputDirName, outputFileName).getAbsolutePath();
                    expectedOutputFile = new File(inputDir, outputFileName).getAbsolutePath();
                }
                ConcurrentTestSendOnlyRunnable runnable =
                        new ConcurrentTestSendOnlyRunnable(inputDirName, message,
                        expectedOutputFile, destination, testPropertiesFileName,
                        1, soapAction);
                runnables[threadCount] = runnable;
                threads[threadCount] = new Thread(runnable);
                threads[threadCount].setName(threadName);
            }
            
            for (int threadCount = 0; threadCount < totalNumberOfDestinations; threadCount++) {
                threads[threadCount].start();
            }
            
            long startTime = System.currentTimeMillis();
            long timeoutTime = startTime + (testTimeout * 1000);
            for (int threadCount = 0; threadCount < totalNumberOfDestinations; threadCount++) {
                long remainingTime = (timeoutTime - System.currentTimeMillis());
                // if infinite timeout or there is remaining time, wait for thread join.
                if (testTimeout == 0 || remainingTime >= 0) {
                    threads[threadCount].join(testTimeout * 1000);
                }
                if (threads[threadCount].isAlive()) {
                    throw new Exception("Test has reached the maximum timeout allowed of " + testTimeout);
                }  else {
                    Throwable encountered = runnables[threadCount].getThrowableEncountered();
                    if (encountered != null) {
                        if (encountered instanceof junit.framework.AssertionFailedError) {
                            throw (junit.framework.AssertionFailedError) encountered;
                        } else {
                            fail("Exception reported by thread " + threads[threadCount].getName() + encountered.getMessage());
                        }
                    }
                }
            }
            long endTime = System.currentTimeMillis();
            
            // Check all the replies
            for (int threadCount = 0; threadCount < totalNumberOfDestinations; threadCount++) {
                SOAPMessage[] replies = runnables[threadCount].getReplies();
                for (int replyCount = 0; replyCount < replies.length; replyCount++) {
                    String testExpectedOutputFileName = runnables[threadCount].getExpectedOutputFileName();
                    String logPrefix = inputDirName + "\\" + testPropertiesFileName + " - " + threads[threadCount].getName() + ":";
                    SOAPMessage reply = replies[replyCount];
                    checkExpectedOutput(logPrefix, logDetails, reply, testExpectedOutputFileName, comparisonType);
                }
            }
            
            String result = " Passed.";
            System.out.println(result);
            
        } catch (Exception ex) {
            System.out.println(" Failed.");
            throw ex;
        } catch (Error er) {
            System.out.println(" Failed.");
            throw er;
        }
    }

    /**
     * Test of inbound SOAP Request processing.
     */
    public void testConcurrentCorrelationSOAPRequest() throws Exception {
        
        String featureProgressVal = mProperties.getProperty(TEST_IN_PROGRESS_KEY);
        //String featureProgressVal = null;
        String testPropertiesFileName = mProperties.getProperty("testpropertiesfilename");
        String inputDirName = mProperties.getProperty("inputdirname");
        if (featureProgressVal != null && featureProgressVal.equals(TEST_IN_PROGRESS_VAL)) {
            System.out.println("Test " + inputDirName + "\\" + testPropertiesFileName
                    + " in Development");
            return;
        }
        String description = mProperties.getProperty("description");
        
        //String concurrentInvokesStr = mProperties.getProperty("concurrentinvokes");
        String comparisonType = "#" + mProperties.getProperty("comparisontype");
        String testTimeoutStr = mProperties.getProperty("testtimeout");
        String inputDir = mProperties.getProperty("absoluteinputdir");
        String calculateThroughputStr = mProperties.getProperty("calculatethroughput");
        String debugStr = mProperties.getProperty("debug");
        boolean calculateThroughput = false;
        if (calculateThroughputStr != null && Boolean.valueOf(calculateThroughputStr) == Boolean.TRUE) {
            calculateThroughput = true;
        }
        boolean logDetails = /*true*/false;
        if (debugStr != null && Boolean.valueOf(debugStr) == Boolean.TRUE) {
            logDetails = true;
        }
        int testTimeout = 0;
        if (testTimeoutStr != null && !testTimeoutStr.trim().equals("")) {
            testTimeout = Integer.parseInt(testTimeoutStr);
        }
        if ("true".equals(System.getProperty("inDebug"))) {
            testTimeout = 0; // disable timeout
        }
        System.out.print("Test " + inputDirName + "\\" + testPropertiesFileName);
        System.out.flush();
        
        try {
            List<Map<String, String>> individualPropMaps = new ArrayList<Map<String, String>>();
            
            Map properties = concurrentCorrelationBuildInfo(mProperties);
            
            Iterator itr =  properties.entrySet().iterator();

            while ( itr.hasNext() ) {
            	Map<String, String> prop = (Map<String, String>)((Map.Entry)itr.next()).getValue();                
                individualPropMaps.add(prop);
            }    
            
            if (logDetails) {
                System.out.println("testCorrelationSOAPRequest Running " + testPropertiesFileName + " : " + description);
            }
            int totalNumberOfDestinations = individualPropMaps.size();
            Thread[] threads = new Thread[totalNumberOfDestinations];
            ConcurrentTestSendOnlyRunnable[] runnables = new ConcurrentTestSendOnlyRunnable[totalNumberOfDestinations];
            Map<String, String> individualDest = null;
            for (int threadCount = 0; threadCount < totalNumberOfDestinations; threadCount++) {
                // round robin of input/output files
                individualDest = individualPropMaps.get(threadCount);
                String inputFileName = individualDest.get("inputfile");
                String outputFileName = individualDest.get("outputfile");
                String destination = individualDest.get("destination");
                String soapAction = individualDest.get("soapaction");
                
                String threadName = "Correlation test thread " + threadCount;
                String logPrefix = inputDirName + "\\" + testPropertiesFileName + " - " + threadName + ":";
                String inputFile = inputDir + File.separator + inputFileName;
                SOAPMessage message = loadMessage(logPrefix, logDetails, inputFile);
                String expectedOutputFile = "";
                if(! ("".equals(outputFileName))){
                    //--expectedOutputFile = new File("output" + File.separator + inputDirName, outputFileName).getAbsolutePath();
                    expectedOutputFile = new File(inputDir, outputFileName).getAbsolutePath();
                }
                ConcurrentTestSendOnlyRunnable runnable =
                        new ConcurrentTestSendOnlyRunnable(inputDirName, message,
                        expectedOutputFile, destination, testPropertiesFileName,
                        1, soapAction);
                runnables[threadCount] = runnable;
                threads[threadCount] = new Thread(runnable);
                threads[threadCount].setName(threadName);
            }
            
            for (int threadCount = 0; threadCount < totalNumberOfDestinations; threadCount++) {
                threads[threadCount].start();
            }
            
            long startTime = System.currentTimeMillis();
            long timeoutTime = startTime + (testTimeout * 1000);
            for (int threadCount = 0; threadCount < totalNumberOfDestinations; threadCount++) {
                long remainingTime = (timeoutTime - System.currentTimeMillis());
                // if infinite timeout or there is remaining time, wait for thread join.
                if (testTimeout == 0 || remainingTime >= 0) {
                    threads[threadCount].join(testTimeout * 1000);
                }
                if (threads[threadCount].isAlive()) {
                    throw new Exception("Test has reached the maximum timeout allowed of " + testTimeout);
                }  else {
                    Throwable encountered = runnables[threadCount].getThrowableEncountered();
                    if (encountered != null) {
                        if (encountered instanceof junit.framework.AssertionFailedError) {
                            throw (junit.framework.AssertionFailedError) encountered;
                        } else {
                            fail("Exception reported by thread " + threads[threadCount].getName() + encountered.getMessage());
                        }
                    }
                }
            }
            long endTime = System.currentTimeMillis();
            
            // Check all the replies
            for (int threadCount = 0; threadCount < totalNumberOfDestinations; threadCount++) {
                SOAPMessage[] replies = runnables[threadCount].getReplies();
                for (int replyCount = 0; replyCount < replies.length; replyCount++) {
                    String testExpectedOutputFileName = runnables[threadCount].getExpectedOutputFileName();
                    String logPrefix = inputDirName + "\\" + testPropertiesFileName + " - " + threads[threadCount].getName() + ":";
                    SOAPMessage reply = replies[replyCount];
                    checkExpectedOutput(logPrefix, logDetails, reply, testExpectedOutputFileName, comparisonType);
                }
            }
            
            String result = " Passed.";
            System.out.println(result);
            
        } catch (Exception ex) {
            System.out.println(" Failed.");
            throw ex;
        } catch (Error er) {
            System.out.println(" Failed.");
            throw er;
        }
    }
    
    
    /**
     * Test of inbound SOAP Request processing.
     */
    public void testConcCorrelationSOAPRequest() throws Exception {
        
        String featureProgressVal = mProperties.getProperty(TEST_IN_PROGRESS_KEY);
        String testPropertiesFileName = mProperties.getProperty("testpropertiesfilename");
        String inputDirName = mProperties.getProperty("inputdirname");
        if (featureProgressVal != null && featureProgressVal.equals(TEST_IN_PROGRESS_VAL)) {
            System.out.println("Test " + inputDirName + "\\" + testPropertiesFileName
                    + " in Development");
            return;
        }
        String description = mProperties.getProperty("description");
        
        //String concurrentInvokesStr = mProperties.getProperty("concurrentinvokes");
        String comparisonType = mProperties.getProperty("comparisontype");
        String testTimeoutStr = mProperties.getProperty("testtimeout");
        String inputDir = mProperties.getProperty("absoluteinputdir");
        String concurrentInvokesStr = mProperties.getProperty("concurrentthreads");
        if (concurrentInvokesStr == null) {
            System.out.println("Test " + inputDirName + "\\" + testPropertiesFileName
                    + " failed" + " property 'concurrentthreads' not specified");
        }
        int concurrentInvokes = Integer.parseInt(concurrentInvokesStr);
        String numberofInvokesPerBPELStr = mProperties.getProperty("numberOfInvokesPerBPEL");
        if (numberofInvokesPerBPELStr == null) {
            System.out.println("Test " + inputDirName + "\\" + testPropertiesFileName
                    + " failed" + " property 'numberofInvokesPerBPEL' not specified");
        }
        int numberofInvokesPerBPEL = Integer.parseInt(numberofInvokesPerBPELStr);
        String calculateThroughputStr = mProperties.getProperty("calculatethroughput");
        String debugStr = mProperties.getProperty("debug");
        boolean calculateThroughput = false;
        if (calculateThroughputStr != null && Boolean.valueOf(calculateThroughputStr) == Boolean.TRUE) {
            calculateThroughput = true;
        }
        boolean logDetails = false;
        if (debugStr != null && Boolean.valueOf(debugStr) == Boolean.TRUE) {
            logDetails = true;
        }
        int testTimeout = 0;
        if (testTimeoutStr != null && !testTimeoutStr.trim().equals("")) {
            testTimeout = Integer.parseInt(testTimeoutStr);
        }
        if ("true".equals(System.getProperty("inDebug"))) {
            testTimeout = 0; // disable timeout
        }
        System.out.print("Test " + inputDirName + "\\" + testPropertiesFileName);
        System.out.flush();
        
        String inputFileKeyPart = "inputfile.invoke";
        String outputFileKeyPart = "outputfile.invoke";
        String destinationKeyPart = "destination.invoke";
        String soapActionKeyPart = "soapaction.invoke";
        String ipOpFolderName = testPropertiesFileName.substring(0,
                testPropertiesFileName.indexOf('.'));
        
        try {
            // Determine all input and corresponding output files
            // List<list<Map>> It holds a list of items equivalent to the number of
            // BPEL instances this test would result in. Each Item holds a list of
            // values that are used to invoke the BPEL.
            List<List<Map<String, String>>> individualPropMaps = new ArrayList<List<Map<String, String>>>();
            Map<String, String> individualPropMap;
            int totalNumberOfDestinations = 0;
            
            for (int threadCount = 1; threadCount <= concurrentInvokes; threadCount++) {
                StringBuffer threadInputKeyPart = new StringBuffer();
                threadInputKeyPart.append(threadCount);
                threadInputKeyPart.append(".");
                StringBuffer threadOutputKeyPart = new StringBuffer();
                threadOutputKeyPart.append(threadCount);
                threadOutputKeyPart.append(".");
                List<Map<String, String>> noOfInvokesList = new ArrayList<Map<String, String>>();
                
                for (int i = 1; i <= numberofInvokesPerBPEL; i++) {
                    StringBuffer threadIPKeyPart = new StringBuffer();
                    StringBuffer threadOPKeyPart = new StringBuffer();
                    
                    threadIPKeyPart.append(threadInputKeyPart);
                    threadIPKeyPart.append(inputFileKeyPart);
                    threadIPKeyPart.append(i);
                    
                    threadOPKeyPart.append(threadOutputKeyPart);
                    threadOPKeyPart.append(outputFileKeyPart);
                    threadOPKeyPart.append(i);
                    
                    StringBuffer destKeyPart = new StringBuffer(destinationKeyPart);
                    destKeyPart.append(i);
                    
                    StringBuffer soapActionKey = new StringBuffer(soapActionKeyPart);
                    soapActionKey.append(i);
                    
                    String iputFileName = mProperties.getProperty(threadIPKeyPart.toString());
                    String outputFileName = mProperties.getProperty(threadOPKeyPart.toString());
                    String destination = mProperties.getProperty(destKeyPart.toString());
                    String soapAction = mProperties.getProperty(soapActionKey.toString());
                    
                    if (outputFileName == null) {
                        throw new Exception("Test set up error. output file not" +
                                " defined for Thread Number " + threadCount +
                                " and invoke Number " + i + " expected output file property: "
                                + threadOutputKeyPart);
                    }
                    if (iputFileName == null) {
                        throw new Exception("Test set up error. input file not" +
                                " defined for Thread Number " + threadCount +
                                " and invoke Number " + i + " expected input file property: "
                                + threadInputKeyPart);
                    }
                    if (destination == null) {
                        throw new Exception("Test set up error. destination not" +
                                " defined invoke Number " + i + " expected " +
                                "destination property: " + destKeyPart);
                    }
                    
                    individualPropMap = new HashMap<String, String>();
                    individualPropMap.put("inputfile", iputFileName);
                    individualPropMap.put("outputfile", outputFileName);
                    individualPropMap.put("destination", destination);
                    individualPropMap.put("soapaction", soapAction);
                    
                    totalNumberOfDestinations++;
                    noOfInvokesList.add(individualPropMap);
                }
                individualPropMaps.add(noOfInvokesList);
            }
            if (logDetails) {
                System.out.println("testCorrelationSOAPRequest Running " + testPropertiesFileName + " : " + description);
            }
            Thread[] threads = new Thread[totalNumberOfDestinations];
            CorrelationTestRunnable[] runnables = new CorrelationTestRunnable[totalNumberOfDestinations];
            Map<String, String> individualDest = null;
            List<Map<String, String>> threadDestinations = null;
            for (int j = 0, threadCount = 0; j < individualPropMaps.size(); j++) {
                threadDestinations = individualPropMaps.get(j);
                for (int i = 0, size = threadDestinations.size(); i < size; i++) {
                    
                    individualDest = threadDestinations.get(i);
                    String inputFileName = individualDest.get("inputfile");
                    String outputFileName = individualDest.get("outputfile");
                    String destination = individualDest.get("destination");
                    String soapAction = individualDest.get("soapaction");
                    
                    String threadName = "Correlation test thread " + threadCount;
                    String logPrefix = inputDirName + "\\" + testPropertiesFileName + " - " + threadName + ":";
                    String inputFile = inputDir + File.separator + ipOpFolderName
                            + File.separator + inputFileName;
                    SOAPMessage message = loadMessage(logPrefix, logDetails, inputFile);
                    //--String expectedOutputFile = new File("output" + File.separator +
                    //--        inputDirName + File.separator + ipOpFolderName, outputFileName).getAbsolutePath();
                    String expectedOutputFile = new File(inputDir + File.separator + ipOpFolderName, outputFileName).getAbsolutePath();
                    
                    CorrelationTestRunnable runnable =
                            new CorrelationTestRunnable(inputDirName, message,
                            expectedOutputFile, destination, testPropertiesFileName,
                            1, soapAction);
                    runnables[threadCount] = runnable;
                    threads[threadCount] = new Thread(runnable);
                    threads[threadCount].setName(threadName);
                    threadCount++;
                }
            }
            
            for (int threadCount = 0; threadCount < totalNumberOfDestinations; threadCount++) {
                threads[threadCount].start();
            }
            
            long startTime = System.currentTimeMillis();
            long timeoutTime = startTime + (testTimeout * 1000);
            for (int threadCount = 0; threadCount < totalNumberOfDestinations; threadCount++) {
                long remainingTime = (timeoutTime - System.currentTimeMillis());
                // if infinite timeout or there is remaining time, wait for thread join.
                if (testTimeout == 0 || remainingTime >= 0) {
                    threads[threadCount].join(testTimeout * 1000);
                }
                if (threads[threadCount].isAlive()) {
                    throw new Exception("Test has reached the maximum timeout allowed of " + testTimeout);
                }  else {
                    Throwable encountered = runnables[threadCount].getThrowableEncountered();
                    if (encountered != null) {
                        if (encountered instanceof junit.framework.AssertionFailedError) {
                            throw (junit.framework.AssertionFailedError) encountered;
                        } else {
                            fail("Exception reported by thread " + threads[threadCount].getName() + encountered.getMessage());
                        }
                    }
                }
            }
            long endTime = System.currentTimeMillis();
            
            // Check all the replies
            for (int threadCount = 0; threadCount < totalNumberOfDestinations; threadCount++) {
                SOAPMessage[] replies = runnables[threadCount].getReplies();
                for (int replyCount = 0; replyCount < replies.length; replyCount++) {
                    String testExpectedOutputFileName = runnables[threadCount].getExpectedOutputFileName();
                    String logPrefix = inputDirName + "\\" + testPropertiesFileName + " - " + threads[threadCount].getName() + ":";
                    SOAPMessage reply = replies[replyCount];
                    checkExpectedOutput(logPrefix, logDetails, reply, testExpectedOutputFileName, comparisonType);
                }
            }
            
            String result = " Passed.";
            System.out.println(result);
            
        } catch (Exception ex) {
            System.out.println(" Failed.");
            throw ex;
        } catch (Error er) {
            System.out.println(" Failed.");
            throw er;
        }
    }
    
    /**
     * Test of inbound file Request processing.
     *
     * Properties file xxxFeed.properties example
     * # The description of the Test
     * description=Testing basic filebc inbound by triggering it though a file, compare the file output to expected result
     * # The directory (if relative in test input dir) to place feeder files into
     * feederdirectory=testruninput
     * # Whether to empty the feeder directory before the test run
     * clearfeederdirectory=true
     * # The input file to copy to the feeder directory
     * feederfile.1=DocElemInput.xml
     * # The directory (if relative in test output dir) where to expect output files
     * eaterdirectory=testruninput
     * # Whether to empty the output ('eater') directory before the test run
     * cleareaterdirectory=true
     * # The output ('eater') file containing the result of the test run
     * eaterfile.1=TestOutput.xml
     * # The file containing the expected result to compare to the actual result
     * outputfile.1=DocElemOutput.xml
     */
    public void testFileRequest() throws Exception {
        
        String description = mProperties.getProperty("description");
        String clearFeeder = mProperties.getProperty("clearfeederdirectory");
        String clearEater = mProperties.getProperty("cleareaterdirectory");
        String feederDir = mProperties.getProperty("feederdirectory");
        String feederFileName = mProperties.getProperty("feederfile");
        String eaterDir = mProperties.getProperty("eaterdirectory");
        String eaterFileName = mProperties.getProperty("eaterfile");
        String comparisonType = mProperties.getProperty("comparisontype");
        String testPropertiesFileName = mProperties.getProperty("testpropertiesfilename");
        String outputFileName = mProperties.getProperty("outputfile");
        String testTimeoutStr = mProperties.getProperty("testtimeout");
        String inputDir = mProperties.getProperty("absoluteinputdir");
        String inputDirName = mProperties.getProperty("inputdirname");
        String debugStr = mProperties.getProperty("debug");
        String featureProgressVal = mProperties.getProperty(TEST_IN_PROGRESS_KEY);
        if (featureProgressVal != null && featureProgressVal.equals(TEST_IN_PROGRESS_VAL)) {
            System.out.println("Test " + inputDirName + "\\" + testPropertiesFileName
                    + " in Development");
            return;
        }
        boolean logDetails = false;
        if (debugStr != null && Boolean.valueOf(debugStr) == Boolean.TRUE) {
            logDetails = true;
        }
        
        // 0 marks no timeout
        int testTimeoutSecs = 0;
        if (testTimeoutStr != null) {
            testTimeoutSecs = Integer.parseInt(testTimeoutStr);
        }
        if ("true".equals(System.getProperty("inDebug"))) {
            testTimeoutSecs = 0; // disable timeout
        }
        System.out.print("Test " + inputDirName + "\\" + testPropertiesFileName);
        System.out.flush();
        try {
            if (logDetails) {
                System.out.println("testFileRequest Running " + testPropertiesFileName + " : " + description);
            }
            
            String inputFile = inputDir + File.separator + feederFileName;
            String feederDirName = inputDir + File.separator + feederDir;
            String feederFile = new File(feederDirName, feederFileName).getAbsolutePath();
            //--String eaterDirName = "output" + File.separator + inputDirName + File.separator + eaterDir;
            String eaterDirName = inputDir + File.separator + eaterDir;
            
            String testRunOutputFile = new File(eaterDirName, eaterFileName).getAbsolutePath();
            String expectedOutputFile = new File(inputDir, outputFileName).getAbsolutePath();
            String logPrefix = inputDirName + "\\" + testPropertiesFileName + ":";
            //sendAndCheck(logPrefix, logDetails, destination, inputFile, expectedOutputFile, null, null, comparisonType, soapAction);
            
            // Prepare test run directories if applicable
            if (clearFeeder != null && Boolean.valueOf(clearFeeder).equals(Boolean.TRUE)) {
                // Empty the directory. For safety reasons don't recurse
                if (feederDir != null && feederDir.length() > 0) {
                    File dir = new File(feederDirName);
                    deleteDirAndContents(dir, false);
                    dir.mkdirs();
                }
            }
            if (clearEater != null && Boolean.valueOf(clearEater).equals(Boolean.TRUE)) {
                // Delete the directory and direct contents. For safety reasons don't recurse
                if (eaterDir != null && eaterDir.length() > 0) {
                    File dir = new File(eaterDirName);
                    deleteDirAndContents(dir, false);
                    dir.mkdirs();
                }
            }
            
            // Copy inputfile to feeder dir
            copyFile(new File(inputFile), new File(feederFile));
            
            // Wait for eater file to appear, or time out
            boolean filePresent = waitForfile(testRunOutputFile, testTimeoutSecs);
            if (!filePresent) {
                fail("Timed out waiting for file " + testRunOutputFile + " to appear");
            }
            
            // Compare eater file to expected output
            ByteArrayOutputStream outputStream = getFileContentsAsOS(testRunOutputFile);
            File testExpectedOutputFile = new File(expectedOutputFile);
            checkExpectedOutput(logPrefix, logDetails, outputStream, testExpectedOutputFile, comparisonType);
            
            System.out.println(" Passed.");
        } catch (Exception ex) {
            System.out.println(" Failed.");
            throw ex;
        } catch (Error er) {
            System.out.println(" Failed.");
            throw er;
        }
    }
    
    
    /**
     * Test of Request processing via FTP BC.
     *
     * Properties file Ftp.properties example
     * # The description of the Test
     * test.desc=<string> a brief description of the test
     * # The local directory (if relative in test input dir) to place input files into
     * input.dir=<local-directory>
     * # Whether to empty the input directory before the test run
     * clear.input.dir=true/false
     * # The input file to copy to the input directory
     * input.message=input.xml
     * # The directory (if relative in test output dir) where to expect output files
     * output.dir=<local-directory>
     * # Whether to empty the output directory before the test run
     * clear.out.dir=true/false
     * # The output file containing the result of the test run
     * output.message=output.xml
     * # The file containing the expected result to compare to the actual result
     * output.expected=expected.xml
     */
    public void testFtpRequest() throws Exception {
                
        String description = mProperties.getProperty("test.desc");
        String clearInputDir = mProperties.getProperty("clear.input.dir");
        String clearOutputDir = mProperties.getProperty("clear.output.dir");
        String inputDir = mProperties.getProperty("input.dir");
        String outputDir = mProperties.getProperty("output.dir");
        String comparisonType = mProperties.getProperty("comparisontype");
        String testPropertiesFile = mProperties.getProperty("testpropertiesfilename");
        String dataDir = mProperties.getProperty("data.dir");
        String expectedOutputDir = mProperties.getProperty("expected.dir");
        String testTimeoutStr = mProperties.getProperty("testtimeout");
        String testCaseDirectoryFullPath = mProperties.getProperty("absoluteinputdir");
        String testCaseDirectory = mProperties.getProperty("inputdirname");
        String debugStr = mProperties.getProperty("debug");

        String inputCountStr = mProperties.getProperty("number.input");
        String outputCountStr = mProperties.getProperty("number.output");

        String inputFilter = mProperties.getProperty("filter.input");
        String outputFilter = mProperties.getProperty("filter.output");
        String expectedFilter = mProperties.getProperty("filter.expected");

        String relaxStr = mProperties.getProperty("relax");
        String cleanupStr = mProperties.getProperty("cleanup");

        int inputCount = 0;
        int outputCount = 0;
        
        String featureProgressVal = mProperties.getProperty(TEST_IN_PROGRESS_KEY);
        
        if (featureProgressVal != null && featureProgressVal.equals(TEST_IN_PROGRESS_VAL)) {
            System.out.println("Test ".concat(testCaseDirectory).concat("\\").concat(testPropertiesFile).concat(" in Development"));
            return;
        }
        
        boolean relax = relaxStr != null && relaxStr.equalsIgnoreCase("true");
        boolean cleanup = cleanupStr != null && cleanupStr.equalsIgnoreCase("true");
        
        boolean logDetails = false;
        
        if (debugStr != null && Boolean.valueOf(debugStr) == Boolean.TRUE) {
            logDetails = true;
        }
        
        // 0 marks no timeout
        int testTimeoutSecs = 0;
        if (testTimeoutStr != null) {
            testTimeoutSecs = Integer.parseInt(testTimeoutStr);
        }
        if ("true".equals(System.getProperty("inDebug"))) {
            testTimeoutSecs = 0; // disable timeout
        }
        
        if (inputCountStr != null) {
            inputCount = Integer.parseInt(inputCountStr);
        }

        if (outputCountStr != null) {
            outputCount = Integer.parseInt(outputCountStr);
        }

        String dirIn = null;
        String dirOut = null;
        String dirData = null;
        String dirExpected = null;
        String logPrefix = testCaseDirectory + "\\" + testPropertiesFile + ":";

        try {
            if (logDetails) {
                System.out.println("testFtpRequest Running " + testPropertiesFile + " : " + description);
            }
            
            dirIn = testCaseDirectoryFullPath + File.separator + inputDir;
            File dirInObj = new File(dirIn);
            if (clearInputDir != null && Boolean.valueOf(clearInputDir).equals(Boolean.TRUE)) {
                if (dirIn != null && dirIn.length() > 0) {
                    deleteDirAndContents(dirInObj, false);
                    dirInObj.mkdirs();
                }
            }

            dirOut = testCaseDirectoryFullPath + File.separator + outputDir;
            File dirOutObj = new File(dirOut);
            if (clearOutputDir != null && Boolean.valueOf(clearOutputDir).equals(Boolean.TRUE)) {
                if ( dirOut != null && dirOut.length() > 0) {
                    deleteDirAndContents(dirOutObj, false);
                    dirOutObj.mkdirs();
                }
            }
            
            dirData = testCaseDirectoryFullPath + File.separator + dataDir;
            dirExpected = testCaseDirectoryFullPath + File.separator + expectedOutputDir;
            
            // Copy input data (message file(s)) to input dir where the file bc is polling 
            int inputCopied = 0;
            
            if ( inputCount > 0 ) {
                inputCopied = copyFiles(dirData, dirIn);
                if ( logDetails ) {
                    System.out.print("Test ".concat(testCaseDirectory).concat("\\").concat(testPropertiesFile).concat(inputCount + " input required..." + inputCopied + " feeded..."));
                    System.out.flush();
                }
            }
            else {
                if ( logDetails ) {
                    System.out.print("Test ".concat(testCaseDirectory).concat("\\").concat(testPropertiesFile).concat(" no input required..."));
                    System.out.flush();
                }
            }

            if ( logDetails )
                System.out.println(inputCount + " message copied to from [" + dirData + "] to [" + dirIn + "]");
            
            RegexFileFilter outputFileFilter = new RegexFileFilter(outputFilter);
            
            if ( outputCount > 0 ) {
                // Wait for output file(s) to appear, or time out
                // assume each input should get an output
                // so wait until expected number of output messages show up
                int outputReceived = waitForOutput(dirOutObj, testTimeoutSecs, outputCount, outputFileFilter);
                if ( outputReceived < outputCount ) {
                    if ( !relax ) {
                        // test failed
                        if ( cleanup ) {
                            // though currently the in and out are pointing to the same directory
                            // but not necessary
                            if ( logDetails )
                                System.out.println("clean up the input output directories....");
                            emptyDir(dirOutObj);
                            emptyDir(dirInObj);
                        }
                        fail("Timed out waiting for outputs to appear in directory [" + dirOutObj.getPath() + "]");
                    }
                    else {
                        // if we get some - then OK
                        if ( outputReceived == 0 ) {
                            if ( cleanup ) {
                                // though currently the in and out are pointing to the same directory
                                // but not necessary
                                if ( logDetails )
                                    System.out.println("clean up the input output directories....");
                                emptyDir(dirOutObj);
                                emptyDir(dirInObj);
                            }
                            fail("Timed out waiting for outputs to appear in directory [" + dirOutObj.getPath() + "]");
                        }
                    }
                    if ( logDetails )
                        System.out.println("+++++ ============ got some of the expected outputs =========== ++++++");
                }
                else {
                    if ( logDetails )
                         System.out.println(outputCount + " expected in [" + dirOut + "] " + outputReceived + " received before time out");
                }

                // Compare output files against expected outputs
                if ( COMPARISON_TYPE_EQUALS.equals(comparisonType) ) {
                    // XML compare - currently limited to one output file and one corresponding expected file
                    if ( expectedFilter == null )
                        throw new Exception("filter.expected not specified, required for XML result compare.");
                    RegexFileFilter expectedFileFilter = new RegexFileFilter(expectedFilter);
                    File dirExpectedObj = new File(dirExpected);
                    File[] expected = dirExpectedObj.listFiles(expectedFileFilter);
                    File[] outputs = dirOutObj.listFiles(outputFileFilter);
                    if ( outputs.length == 1 && expected.length == 1 ) {
                        String outXML = getFileContents(outputs[0]);
                        String expXML = getFileContents(expected[0]);
                        if ( outXML == null || outXML.trim().length() == 0 )
                            throw new Exception("NULL content from output file: " + outputs[0].getAbsolutePath());
                        if ( expXML == null || expXML.trim().length() == 0 )
                            throw new Exception("NULL content from expected file: " + expected[0].getAbsolutePath());
                        boolean isEqual = testCompareXMLIdentical(outXML, expXML);
                        if ( logDetails )
                            System.out.print("XML compare = " + isEqual);
                        assertTrue("Result not equal to expected ...", isEqual);
                    }
                    else {
                        throw new Exception("When comparison type is XML equal - expect 1 output and 1 expected - but # of output =" + outputs.length + "and # of expected =" + expected.length);
                    }
                }
                else { 
                    // string compare
                    checkExpectedOutput(logPrefix, logDetails, dirOut, dirIn, dirExpected, outputFileFilter, comparisonType, cleanup, relax, logDetails);
                }
            }
            else {
                if ( logDetails ) {
                    System.out.print("Test ".concat(testCaseDirectory).concat("\\").concat(testPropertiesFile).concat(inputCount + " no output expected..."));
                    System.out.flush();
                }
            }
            if ( cleanup ) {
                // though currently the in and out are pointing to the same directory
                // but not necessarily so
                if ( logDetails )
                    System.out.println("clean up the input output directories....");
                emptyDir(dirOutObj);
                emptyDir(dirInObj);
            }
            System.out.println();
            System.out.println("Test ".concat(testCaseDirectory).concat("\\").concat(testPropertiesFile).concat(" Passed."));
            System.out.flush();
        } catch (Exception ex) {
            System.out.println();
            System.out.println("Test ".concat(testCaseDirectory).concat("\\").concat(testPropertiesFile).concat(" Failed."));
            System.out.flush();
            if ( logDetails )
                ex.printStackTrace();
            throw ex;
        } catch (Error er) {
            System.out.println();
            System.out.println("Test ".concat(testCaseDirectory).concat("\\").concat(testPropertiesFile).concat(" Failed."));
            System.out.flush();
            if ( logDetails )
                er.printStackTrace();
            throw er;
        }
    }

    /**
     * Check the output received against the expected output.
     */

    void checkExpectedOutput(
            String logPrefix, 
            boolean logDetails, 
            String dirOut,
            String dirIn,
            String dirExpected,
            RegexFileFilter outputFilter,
            String comparisonType,
            boolean cleanup,
            boolean relax,
            boolean debug)
            throws java.io.IOException {
        boolean isEmptyOutputAllowed = false;
        if(comparisonType != null && comparisonType.startsWith("#")) {
            isEmptyOutputAllowed = true;
            comparisonType = comparisonType.substring(1);
        }

        File dirOutObj = new File(dirOut);
        File dirInObj = new File(dirIn);
        File dirExpectedObj = new File(dirExpected);
        
        String inputDirName = mProperties.getProperty("inputdirname");  // currently also serves as test case name

        File actualOutputDir = new File(dirOutObj.getParent() + "/../results/" + inputDirName);    // FIXME
        if (actualOutputDir.exists()) {
            if (!actualOutputDir.isDirectory()) {
                // TODO: throw some exception here
                return;
            }
        } else {
            actualOutputDir.mkdir();
        }
        
        // only support identical match with expected
        if ( COMPARISON_TYPE_BINARY.equals(comparisonType) ||
                COMPARISON_TYPE_EQUALS.equals(comparisonType) ||
                COMPARISON_TYPE_SKELETON.equals(comparisonType) ){
            System.out.println("Only supports IDENTICAL comparison !!!!!!!!!!!!!!");
            comparisonType = COMPARISON_TYPE_IDENTICAL;
        }
        
        // for each of the expected message, check the output messages, if there is an identical match,
        // then the test succeeded, otherwise, it is a failure
        if (comparisonType.equals(COMPARISON_TYPE_IDENTICAL)) {
            //System.out.println("Do IDENTICAL comparison !!!!!!!!!!!!!!");
            File[] expected = dirExpectedObj.listFiles();
            File[] outputs = dirOutObj.listFiles(outputFilter);
            // check each every output has a match in expected
            for ( int i = 0; i < outputs.length; i++ ) {
                String outMsg = getFileContents(outputs[i]);
                boolean found = false;
                for ( int j = 0; j < expected.length; j++ ) {
                    File exp = expected[j];
                    if ( exp == null || exp.isDirectory() )
                        continue;
                    String expMsg = getFileContents(exp);
                    if ( outMsg.length() == expMsg.length() ) {
                        if ( outMsg.equals(expMsg) ) {
                            found = true;
                            expected[j] = null;
                            break;
                        }
                    }
                }
                if ( !found ) {
                    // fail, need to clean up
                    if ( cleanup ) {
                        if ( debug )
                            System.out.println("clean up the input output directories....");
                        emptyDir(dirOutObj);
                        emptyDir(dirInObj);
                    }
                }
                assertTrue("Output message " + outputs[i].getPath() + " is not found among expected.", found);
            }
        }
        else {
            System.out.println("For FTP BC driver tests, only supports idenitcal comparison between outputs and expected outputs, unsupported comparison type:[" + comparisonType + "]");
        }
        //System.out.println("Exit output checking ==================================");
    }

    class CorrelationTestRunnable extends ConcurrentTestSendOnlyRunnable {
        
        CorrelationTestRunnable(String anInputDirName, SOAPMessage
                inputMessage, String anExpectedOutputFile, String aDestination,
                String aTestPropertiesFile, int aNoOfInvokes, String aSoapAction) {
            
            super(anInputDirName, inputMessage, anExpectedOutputFile,
                    aDestination, aTestPropertiesFile, aNoOfInvokes, aSoapAction);
        }
    }
    
    /**
     * Send requested number of messages, store all replies - or an encountered exception
     */
    class ConcurrentTestSendOnlyRunnable implements Runnable {
        String mInputDirName;
        SOAPMessage mInputMessage;
        SOAPMessage[] mReplies;
        String mExpectedOutputFile;
        String mDestination;
        String mTestPropertiesFile;
        String mSoapAction;
        int mNoOfInvokes;
        Throwable mThrowableEncountered;
        long startTime, endTime;
        int invokeCount;
        
        ConcurrentTestSendOnlyRunnable(String anInputDirName, SOAPMessage
                inputMessage, String anExpectedOutputFile, String aDestination,
                String aTestPropertiesFile, int aNoOfInvokes, String aSoapAction) {
            
            mInputDirName = anInputDirName;
            mInputMessage = inputMessage;
            mExpectedOutputFile = anExpectedOutputFile;
            mDestination = aDestination;
            mTestPropertiesFile = aTestPropertiesFile;
            mSoapAction = aSoapAction;
            mNoOfInvokes = aNoOfInvokes;
            mReplies = new SOAPMessage[mNoOfInvokes];
        }
        
        public void run() {
            try {
                String logPrefix = mInputDirName + "\\" + mTestPropertiesFile + " - " + Thread.currentThread().getName() + ":";
                boolean logDetails = false;
                startTime = System.currentTimeMillis();
                for (; invokeCount < mNoOfInvokes; invokeCount++) {
                    mReplies[invokeCount] = sendMessage(logPrefix, logDetails, mDestination, mInputMessage, null, null, mSoapAction);
                }
                endTime = System.currentTimeMillis();
            } catch (Throwable ex) {
                mThrowableEncountered = ex;
                ex.printStackTrace();
            }
        }
        
        public Throwable getThrowableEncountered() {
            return mThrowableEncountered;
        }
        
        public SOAPMessage[] getReplies() {
            return mReplies;
        }
        
        public String getExpectedOutputFileName() {
            return mExpectedOutputFile;
        }
        public long getDuration(){
            return endTime-startTime;
        }
        public long getInvocationsDone(){
            return invokeCount;
        }
        public long getInvocationCount(){
            return mNoOfInvokes;
        }
        
    };
    
    /**
     * Send a soap message read in from the given input file, compare the output
     * to the given output file
     */
    void sendAndCheck(String logPrefix, boolean logDetails, String destination, File testMsgFile, File testExpectedOutputFile, String expectedHttpStatus, String expectedHttpWarning, String comparisonType, String soapAction) throws Exception {
        if (logDetails) {
            System.out.println(logPrefix + " destination: " + destination + " input message file: " + testMsgFile.getAbsolutePath() + " comparison output file: " + testExpectedOutputFile.getAbsolutePath());
        }
        SOAPMessage message = loadMessage(logPrefix, logDetails, testMsgFile);
        SOAPMessage response = sendMessage(logPrefix, logDetails, destination, message, expectedHttpStatus, expectedHttpWarning, soapAction);
        checkExpectedOutput(logPrefix, logDetails, response, testExpectedOutputFile, comparisonType);
    }
    
    /**
     * Send a soap message read in from the given input file, compare the output
     * to the given output file
     * TODO:[rselvaraj] refactor two overloaded methods into one.
     */
    void sendAndCheck(String logPrefix, boolean logDetails, String destination, String testMsgFileName, String testExpectedOutputFileName, String expectedHttpStatus, String expectedHttpWarning, String comparisonType, String soapAction) throws Exception {
        if (logDetails) {
            System.out.println(logPrefix + " destination: " + destination + " input message file: " + testMsgFileName + " comparison output file: " + testExpectedOutputFileName);
        }
        SOAPMessage message = loadMessage(logPrefix, logDetails, testMsgFileName);
        SOAPMessage response = sendMessage(logPrefix, logDetails, destination, message, expectedHttpStatus, expectedHttpWarning, soapAction);
        checkExpectedOutput(logPrefix, logDetails, response, testExpectedOutputFileName, comparisonType);
    }
    
    /**
     * read in a soap message from the given input file
     */
    SOAPMessage loadMessage(String logPrefix, boolean logDetails, File testMsgFile) throws SOAPException, IOException {
        //Create and populate the message from a file
        SOAPMessage message = mMessageFactory.createMessage();
        SOAPPart soapPart = message.getSOAPPart();
        StreamSource preppedMsgSrc = new StreamSource(new FileInputStream(testMsgFile));
        soapPart.setContent(preppedMsgSrc);
        message.saveChanges();
        // Check the input
        if (logDetails) {
            java.io.OutputStream os = new java.io.ByteArrayOutputStream();
            message.writeTo(os);
            System.out.println("\n" + logPrefix + " REQUEST:\n" + os.toString() + "\n");
        }
        return message;
    }
    
    /**
     * read in a soap message from the given input file
     * TODO:[rselvaraj] refactor two overloaded methods into one.
     */
    SOAPMessage loadMessage(String logPrefix, boolean logDetails, String testMsgFileName) throws SOAPException, IOException {
        //Create and populate the message from a file
        SOAPMessage message = mMessageFactory.createMessage();
        SOAPPart soapPart = message.getSOAPPart();
        
//        SOAPEnvelope soapEnvelope = soapPart.getEnvelope();
//        soapEnvelope.addNamespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");//
//        Iterator iterator = soapEnvelope.getNamespacePrefixes();//
//        while (iterator.hasNext()) {
//            String prefix = (String) iterator.next();
//            String uri = soapEnvelope.getNamespaceURI(prefix);
//            System.out.println(prefix + "-> " + uri);
//        }
        
        StreamSource preppedMsgSrc = new StreamSource(new FileInputStream(testMsgFileName));
        soapPart.setContent(preppedMsgSrc);
        message.saveChanges();
        // Check the input
        if (logDetails) {
            java.io.OutputStream os = new java.io.ByteArrayOutputStream();
            message.writeTo(os);
            System.out.println("\n" + logPrefix + " REQUEST:\n" + os.toString() + "\n");
        }
        return message;
    }
    
    /**
     * Send a soap message
     * @param destination URL to send to
     * @param message message to send
     * @param expectedHttpStatus expected http status code or null if success is expected
     * @return reply soap message
     */
    SOAPMessage sendMessage(String logPrefix, 
            boolean logDetails, 
            String destination, 
            SOAPMessage message, 
            String expectedHttpStatus, 
            String expectedHttpWarning, 
            String soapAction) throws SOAPException, Exception {
        
        // Add soapAction if not null
        if (soapAction != null) {
            MimeHeaders hd = message.getMimeHeaders();
            hd.setHeader("SOAPAction", soapAction);
        }
        
        // Store standard error output temporarily if we expect a certain error as we do not want
        // to see the SAAJ output in this case
        java.io.PrintStream origErr = null;
        java.io.ByteArrayOutputStream bufferedErr = null;
        java.io.PrintStream stdErr = null;
        if ((expectedHttpStatus != null && !expectedHttpStatus.startsWith("2")) || expectedHttpWarning != null) {
            origErr =  System.err;
            bufferedErr = new java.io.ByteArrayOutputStream();
            stdErr = new java.io.PrintStream(bufferedErr);
            System.setErr(stdErr);
        }
        
        // Send the message and get a reply
        SOAPMessage reply = null;
        long start = 0;
        if (logDetails) {
            start = System.currentTimeMillis();
        }
        
        if (destination.indexOf("${") != -1 && destination.indexOf("}") != -1) {
            try {
                String nbUserDir = System.getProperty("NetBeansUserDir");
                AdministrationService adminService = new AdministrationService(nbUserDir);
                // Currently only deal with http soap bc because soap binding is the 
                // only supported binding type in test driver.
                destination = //EnvironmentVariableHandler.translate(destination);
                        adminService.translate(destination, "bindingComponents", "sun-http-binding");
            } catch (Exception ex) {
                if (stdErr != null) {
                    System.setErr(origErr);
                    stdErr.flush();
                    stdErr.close();
                    origErr.print(bufferedErr.toString());
                }
                throw ex;
            }
        }
        
        boolean httpSuccess = true;
        try {
            reply = mConnection.call(message, destination);
        } catch (SOAPException ex) {
            httpSuccess = false;
            // This currently relies on the implementation details
            // to check for the HTTP status as no standard way is currently provide by saaj
            // It expectes an exception message of the format "Bad response: (404Error"
            // - where 404 is the status code in this example
            if (expectedHttpStatus == null || (expectedHttpWarning != null && bufferedErr.toString().indexOf(expectedHttpWarning) < 0)) {
                if (stdErr != null) {
                    System.setErr(origErr);
                    stdErr.flush();
                    stdErr.close();
                    origErr.print(bufferedErr.toString());
                }
                throw ex;
            } else {
                if (ex.getMessage().indexOf(expectedHttpStatus) > -1) {
                    if (logDetails) {
                        System.out.println(logPrefix + " Expected HTTP status code " + expectedHttpStatus + " found in reply. ");
                    }
                } else {
                    if (stdErr != null) {
                        System.setErr(origErr);
                        stdErr.flush();
                        stdErr.close();
                        origErr.print(bufferedErr.toString());
                    }
                    fail(logPrefix + " Expected HTTP status code " + expectedHttpStatus + " NOT found in reply: " + ex.getMessage());
                }
            }
        }
        long end = 0;
        if (logDetails) {
            end = System.currentTimeMillis();
        }
        
        // Ensure standard error isn't redirected/buffered anymore
        if (origErr != null) {
            System.setErr(origErr);
            if (stdErr != null) {
                stdErr.close();
            }
        }
        
        if (logDetails) {
            System.out.println(logPrefix + " Call took " + (end - start) + " ms");
        }
        
        // If the test expected the call to fail, check that it did.
        if (expectedHttpStatus != null && httpSuccess && !expectedHttpStatus.startsWith("2")) {
            fail(logPrefix + " Call returned an unexpected 'success' HTTP status code instead of the expected HTTP status code " + expectedHttpStatus);
        }
        
        return reply;
    }
    
    /**
     * Check the output received against the expected output in a file.
     * @param reply the outputStream containing the SOAP reply
     * @param testExpectedOutputFileName the file name of the file to compare the response against.
     * TODO:[rselvaraj] refactor all four variations of checkExpectedOutput to one if possible.
     */
    void checkExpectedOutput(String logPrefix, boolean logDetails, SOAPMessage reply,
            File testExpectedOutputFile, String comparisonType)
            throws IOException, TransformerException, SAXException, ParserConfigurationException, SOAPException {
        // Check against the file that the expected SOAP reply was received
        if (testExpectedOutputFile != null) {
            ByteArrayOutputStream outputStream = replyAsByteArrayOS(logPrefix, logDetails, reply);
            checkExpectedOutput(logPrefix, logDetails, outputStream, testExpectedOutputFile, comparisonType);
        }
    }
    
    /**
     * Check the output received against the expected output in a file.
     * @param reply the outputStream containing the SOAP reply
     * @param testExpectedOutputFileName the file name of the file to compare the response against.
     */
    void checkExpectedOutput(String logPrefix, boolean logDetails, SOAPMessage reply,
            String testExpectedOutputFileName, String comparisonType)
            throws IOException, TransformerException, SAXException, ParserConfigurationException, SOAPException {
        // Check against the file that the expected SOAP reply was received
        if((testExpectedOutputFileName == null) || EMPTY.equals(testExpectedOutputFileName)){
            return;
        }
        ByteArrayOutputStream outputStream = replyAsByteArrayOS(logPrefix, logDetails, reply);
        File testExpectedOutputFile = new File(testExpectedOutputFileName);
        checkExpectedOutput(logPrefix, logDetails, outputStream, testExpectedOutputFile, comparisonType);
    }
    
    /**
     * Check the output received against the expected output in a file.
     * @param outputStream the stream containing the SOAP reply
     * @param testExpectedOutputFileName the file name of the file to compare the response against.
     */
    void checkExpectedOutput(String logPrefix, boolean logDetails, ByteArrayOutputStream outputStream,
            File testExpectedOutputFile, String comparisonType)
            throws java.io.IOException, org.xml.sax.SAXException, javax.xml.parsers.ParserConfigurationException {
        boolean isEmptyOutputAllowed = false;
        if(comparisonType != null && comparisonType.startsWith("#")) {
            isEmptyOutputAllowed = true;
            comparisonType = comparisonType.substring(1);
        }
        String testXML = outputStream.toString("UTF-8").trim();
                
        // Temporary fix to make the output file validatable.
        // The real fix should come from httpsoap bc.
        
        // Replace old namespace by new namespace
        testXML = testXML.replaceFirst("xmlns:xsd=\"http://www.w3.org/1999/XMLSchema\"",
                "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"");
        testXML = testXML.replaceFirst("xmlns:xsi=\"http://www.w3.org/1999/XMLSchema-instance\"",
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://schemas.xmlsoap.org/soap/envelope/ http://schemas.xmlsoap.org/soap/envelope/\"");
        
        // It's possible that the old namespace didn't exist. 
        // In that case, we insert the new namespace
        if (testXML.indexOf("xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"") == -1) {
            testXML = testXML.replaceFirst("<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">",
                    "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://schemas.xmlsoap.org/soap/envelope/ http://schemas.xmlsoap.org/soap/envelope/\">");
        }
        
        if(isEmptyOutputAllowed && (EMPTY.equals(testXML))){
            return;
        }
        
        String inputDirName = mProperties.getProperty("inputdirname");  // currently also serves as test case name
        File actualOutputDir = new File(testExpectedOutputFile.getParent() + "/../results/" + inputDirName);    // FIXME
        if (actualOutputDir.exists()) {
            if (!actualOutputDir.isDirectory()) {
                // TODO: throw some exception here
                return;
            }
        } else {
            actualOutputDir.mkdir();
        }
        
        String testExpectedOutputAbsoluteFileName = testExpectedOutputFile.getAbsolutePath();
        String controlXML = null;
        try {
            
            controlXML = getFileContents(testExpectedOutputAbsoluteFileName);
           
        } catch (Exception e) {
            e.printStackTrace();
        }
        controlXML = (controlXML == null)? EMPTY: controlXML.trim();
        
        if (isEmptyOutputAllowed && (EMPTY.equals(testXML)) && (EMPTY.equals(controlXML))){
            return;
        }
        if ((!COMPARISON_TYPE_BINARY.equals(comparisonType)) &&
                (!COMPARISON_TYPE_EQUALS.equals(comparisonType)) &&
                (!COMPARISON_TYPE_SKELETON.equals(comparisonType))){
            comparisonType = COMPARISON_TYPE_IDENTICAL;
        }
        if (comparisonType.equals(COMPARISON_TYPE_EQUALS)) {
//            // TODO: shouldn't we populate the expected output here too?
//            if (controlXML.equals(EMPTY)) {
//                writeToFile(testExpectedOutputFile, testXML);
//                assertTrue(OVERWRITE_EMPTY_OUTPUT_MSG /*+ testExpectedOutputFile.getAbsolutePath() + " is updated.*/, false);
//                return;
//            }
            
            boolean isSimilar = false;
            
            if (! controlXML.equals(EMPTY)) {
                // Skip diff computation to avoid SAXParserException because of empty file                
                isSimilar = this.testCompareXMLEquals(controlXML, testXML);
            } else {
                isSimilar = testXML.equals(EMPTY);
            }

            if (!isSimilar || mGenerateOutputOnSuccess) {
                File actualOutputFile = getActualOutputFile(actualOutputDir, isSimilar);

                writeToFile(actualOutputFile, removeIndent(formatString(testXML, mIndent),mIndent));
                
                String timeStampPrefix = getActualOutputTimeStampPrefix(actualOutputFile);
                //--assertTrue("Response is not similar enough to be considered 'equal' to expected output. \nreceived: " + testXML + "\nexpected: " + controlXML + "\ndifference:" + difference, difference.similar());
                assertTrue(timeStampPrefix + " The response is not similar enough to be considered 'equal' to the expected output.", isSimilar);
            }
        } else if (comparisonType.equals(COMPARISON_TYPE_IDENTICAL)) {
//            if (controlXML.equals(EMPTY)) {
//                writeToFile(testExpectedOutputFile, testXML);
//                assertTrue(OVERWRITE_EMPTY_OUTPUT_MSG /*+ testExpectedOutputFile.getAbsolutePath() + " is updated.*/, false);
//                return;
//            }
            
            boolean isIdentical = false;
            
            if (! controlXML.equals(EMPTY)) {
                // Skip diff computation to avoid SAXParserException because of empty file
                isIdentical = this.testCompareXMLIdentical(controlXML, testXML);
            } else {
                isIdentical = testXML.equals(EMPTY);
            }
            
            if (!isIdentical || mGenerateOutputOnSuccess) {
                File actualOutputFile = getActualOutputFile(actualOutputDir, isIdentical);
                writeToFile(actualOutputFile, removeIndent(formatString(testXML, mIndent),mIndent));

                String timeStampPrefix = getActualOutputTimeStampPrefix(actualOutputFile);
                //--assertTrue("Response is not identical to expected output. \nreceived: " + testXML + "\nexpected: " + controlXML + "\ndifference:" + difference, difference.identical());
                assertTrue(timeStampPrefix + " The response is not identical to the expected output.", isIdentical);
            }
            
        } else if (comparisonType.equals(COMPARISON_TYPE_BINARY)) {
            if (logDetails) {
                System.out.println(logPrefix + " Test expected output message file: " + testExpectedOutputFile.getAbsolutePath()); // NOI18N
            }
            
            FileInputStream fis = new FileInputStream(testExpectedOutputFile);
            byte[] output = outputStream.toByteArray();
            byte[] compare = new byte[output.length];
            int bytesRead = fis.read(compare);
            int nextByte = fis.read();
            fis.close();
            
            //FileOutputStream fos = new FileOutputStream(expectedOutputFile + ".current");
            //fos.write(output);
            //fos.close();
            
            if (bytesRead == -1) {
                writeToFile(testExpectedOutputFile, output);
                assertTrue(OVERWRITE_EMPTY_OUTPUT_MSG /*+ testExpectedOutputFile.getAbsolutePath() + " is updated."*/, false);
                return;
            }
            boolean responseIsTooLong = bytesRead != -1;
            boolean responseIsTooShort = nextByte == -1;
            boolean responseNotMatch = !Arrays.equals(output, compare);
            
            boolean isFailure = responseIsTooLong || responseIsTooShort || responseNotMatch;
            if (isFailure || mGenerateOutputOnSuccess) {
                File actualOutputFile = getActualOutputFile(actualOutputDir, !isFailure);
                writeToFile(actualOutputFile, output);
                
                String timeStampPrefix = getActualOutputTimeStampPrefix(actualOutputFile);
                //--assertTrue(logPrefix + " Response is longer than expected. \nreceived: " + outputStream.toString() + "\nexpected: " + new String(compare), bytesRead != -1);
                //--assertTrue(logPrefix + " Response is shorter than expected \nreceived: " + outputStream.toString() + "\nexpected: " + new String(compare), nextByte == -1);
                //--assertTrue(logPrefix + " The response received does not match the expected response. \nreceived: " + outputStream.toString() + "\nexpected: " + new String(compare), Arrays.equals(output, compare));
                assertTrue(timeStampPrefix + " " + logPrefix + " The response is longer than expected.", responseIsTooLong);
                assertTrue(timeStampPrefix + " " + logPrefix + " The response is shorter than expected.", responseIsTooShort);
                assertTrue(timeStampPrefix + " " + logPrefix + " The response received does not match the expected response.", responseNotMatch);
            }
            
            if (logDetails) {
                if (!isFailure) {
                    System.out.println(logPrefix + " Success: response matches expected output.");
                }
            }
        } else if (comparisonType.equals(COMPARISON_TYPE_SKELETON)) {
            DifferenceListener myDifferenceListener = new IgnoreTextAndAttributeValuesDifferenceListener();
            Diff difference = new Diff(controlXML, testXML);
            difference.overrideDifferenceListener(myDifferenceListener);
            
            boolean isSimilar = difference.similar();
            
            if (!isSimilar || mGenerateOutputOnSuccess) {
                File actualOutputFile = getActualOutputFile(actualOutputDir, isSimilar);
                writeToFile(actualOutputFile, testXML);
                
                String timeStampPrefix = getActualOutputTimeStampPrefix(actualOutputFile);
                //--assertTrue("Response 'skeleton' is not similar enough to be considered 'equal' to expected output skelton. \nreceived: " + testXML + "\nexpected: " + controlXML + "\ndifference:" + difference, difference.similar());
                assertTrue(timeStampPrefix + " The response 'skeleton' is not similar enough to be considered 'equal' to expected output skelton.", isSimilar);
            }
        }
    }
    
    private static File getActualOutputFile(File actualOutputDir, boolean success) {
        String actualOutputFileName = "Actual_" + mSDF.format(new Date()) + (success ? "_S" : "_F") + ".xml";
        return new File(actualOutputDir, actualOutputFileName);
    }
    
    private static String getActualOutputTimeStampPrefix(File actualOutputFile) {
        // e.x., Actual_20060803211027.xml, Actual_20060803211027_F.xml, Actual_20060803211027_S.xml
        String fileName = actualOutputFile.getName();
        String timeStamp;
        try {
            String yearStr = fileName.substring(7, 11);
            String monthStr = fileName.substring(11, 13);
            String dayStr = fileName.substring(13, 15);
            String hourStr = fileName.substring(15, 17);
            String minuteStr = fileName.substring(17, 19);
            String secondStr = fileName.substring(19, 21);
            
            int year = Integer.parseInt(yearStr);
            int month = Integer.parseInt(monthStr);
            int day = Integer.parseInt(dayStr);
            int hour = Integer.parseInt(hourStr);
            int minute = Integer.parseInt(minuteStr);
            int second = Integer.parseInt(secondStr);
            
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month-1, day, hour, minute, second);
            Date date = calendar.getTime();
            Object[] arguments = { date };
            String pattern = "{0, date} {0, time}";     // NOI18N
            timeStamp = MessageFormat.format(pattern, arguments);
        } catch (Exception e) {
            timeStamp = "<Unknown Time Stamp>"; // NOI18N
        }
        return "[" + timeStamp + "]";   // NOI18N
    }
    static String formatString(String inStr, String indent) throws IOException,UnsupportedEncodingException {
        String inputStr = inStr;
        try {
            inputStr = xdmUtil.prettyPrintXML(inputStr, indent);
        } catch (UnsupportedEncodingException ex) {
            System.out.println(ex.toString());
            ex.printStackTrace();
            throw ex;
        } catch (BadLocationException ex) {
            System.out.println(" BadLocation from formatting xml.");
        } catch (IOException ex) {
            System.out.println(" Failed.");
            throw ex;
        }
        return inputStr;
    }
    

    static String formatString(File file) throws TransformerException {
        // Check the output
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute("indent-number", new Integer(2));
        
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        
        StreamSource source = new StreamSource(file);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        transformer.transform(source, new StreamResult(out));
        try {
            return out.toString("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
        
        return null;
    }


    /**
     * Utility method to extract the SOAPMessge content into a ByteArrayOutputStream
     */
    static ByteArrayOutputStream replyAsByteArrayOS(String logPrefix, boolean logDetails, SOAPMessage reply) throws TransformerException, SOAPException {
        // Check the output
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute("indent-number", new Integer(2));
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if (reply != null) {
            SOAPPart replySOAPPart = reply.getSOAPPart();
//            SOAPEnvelope soapEnvelope = replySOAPPart.getEnvelope();
//            Iterator iterator = soapEnvelope.getNamespacePrefixes();
//            while (iterator.hasNext()) {
//                String prefix = (String) iterator.next();
//                String uri = soapEnvelope.getNamespaceURI(prefix);
//                System.out.println(prefix + "-> " + uri);
//            }
            
            Source sourceContent = replySOAPPart.getContent();
            try {
                StreamResult result = new StreamResult(new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8")));
                transformer.transform(sourceContent, result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (logDetails) {


            System.out.println("\n" + logPrefix + " RESPONSE:\n" + outputStream.toString());
        }
        return outputStream;
    }
    
    
    /**
     * Utility method to load the contents of a file as a String
     */
    public static String getFileContents(String fileName) throws IOException {
        int chunksize = 512;
        //println(fileName);
        //FileReader reader = new FileReader(fileName);
        InputStreamReader reader = new InputStreamReader(new FileInputStream(fileName), "UTF-8");
        StringBuffer output = new StringBuffer();
        char[] buff = new char[chunksize];
        int len = reader.read(buff);
        while (len > 0) {
            output.append(buff, 0, len);
            len = reader.read(buff);
        }
        return output.toString();
    }
    
    /**
     * Utility method to load the contents of a file as ByteArrayOutputStream
     */
    ByteArrayOutputStream getFileContentsAsOS(String fileName) throws IOException {
        InputStream in = new FileInputStream(fileName);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int i;
        while ((i = in.read()) != -1) {
            out.write(i);
        }
        in.close();
        return out;
    }
    
    static void writeToFile(File file, byte[] content) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(content), "UTF-8");
            char[] buf = new char[512];
            int n = 0;
            while ((n = reader.read(buf)) != -1) {
                writer.write(buf, 0, n);
            }
            writer.flush();
        } catch (Exception e) {
            try {
                writer.close();
            } catch (Exception e1) {
            }
        }
    }
    static void writeToFile(File file, String content) {
        // The following version generates garbaged output.
        FileOutputStream writer = null;
        try {
            writer = new FileOutputStream(file);
            writer.write(content.getBytes("UTF-8"));
            writer.flush();
        } catch (Exception e) {
            try {
                writer.close();
            } catch (Exception e1) {
            }
        }
    }
    
    /**
     * Utility method to load the contents of a file as a String
     */
    static String getFileContents(File file) throws IOException {
        int chunksize = 512;
        FileReader reader = new FileReader(file);
        StringBuffer output = new StringBuffer();
        char[] buff = new char[chunksize];
        int len = reader.read(buff);
        while (len > 0) {
            output.append(buff, 0, len);
            len = reader.read(buff);
        }
        return output.toString();
    }
    
    /**
     * Utility method to load a properties file
     */
    static Properties loadProperties(String propertiesFile) throws IOException{
        FileInputStream fis = null;
        Properties props = null;
        try {
            fis = new FileInputStream(new File(propertiesFile));
            props = new Properties();
            props.load(fis);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception ex) {
                // ignore
            }
        }
        return props;
    }
    
    /**
     * Utility method to copy a file
     */
    void copyFile(File in, File out) throws IOException {
        FileChannel sourceChannel = new FileInputStream(in).getChannel();
        FileChannel destinationChannel = new FileOutputStream(out).getChannel();
        sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
        sourceChannel.close();
        destinationChannel.close();
    }
    
    /**
     * Utility method to copy files under src dir to dest dir
     * and return the number of files copied
     */
    int copyFiles(String srcDir, String destDir) throws IOException, Exception {
        int count = 0;
        File src = new File(srcDir);
        File dest = new File(destDir);
        if ( src.isDirectory() && dest.isDirectory() ) {
            File[] msgFiles = src.listFiles();
            if ( msgFiles != null && msgFiles.length > 0 ) {
                for ( int i = 0; i < msgFiles.length; i++ ) {
                    if ( msgFiles[i].isDirectory() )
                        continue;
                    count++;
                    File destFile = new File(dest, msgFiles[i].getName());
                    FileChannel sourceChannel = new FileInputStream(msgFiles[i]).getChannel();
                    FileChannel destinationChannel = new FileOutputStream(destFile).getChannel();
                    sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
                    sourceChannel.close();
                    destinationChannel.close();
                }
            }
        }
        else {
            throw new Exception("copyFiles(srcDir, destDir) requires that both srcDir and destDir be directory.");
        }
        return count;
    }
    
    boolean deleteDirAndContents(File path, boolean recursive) {
        if(path.exists()) {
            File[] files = path.listFiles();
            for(int i=0; i<files.length; i++) {
                if(files[i].isDirectory()) {
                    if (recursive) {
                        deleteDirAndContents(files[i], recursive);
                    }
                } else {
                    files[i].delete();
                }
            }
        }
        return(path.delete());
    }
    
    // empty the dir
    void emptyDir(File dir) {
        if(dir.exists() && dir.isDirectory() ) {
            File[] files = dir.listFiles();
            for(int i=0; i<files.length; i++) {
                if(files[i].isFile()) {
                    boolean b = files[i].delete();
                }
            }
        }
    }
    /**
     * Utility method to wait for a file to appear. If it doesn't appear, stop waiting after given number of seconds.
     * Wparam fileNameToWaitFor the file to wait for
     * @param testTimeoutSecs the number of seconds to wait for it to appear. 0= wait infinitely
     * @return true if file exists, false if the wait timed out and it doesn't exist
     */
    boolean waitForfile(String fileNameToWaitFor, int testTimeoutSecs) throws InterruptedException{
        File fileToWaitFor = new File(fileNameToWaitFor);
        long timeLimit = System.currentTimeMillis() + (testTimeoutSecs * 1000);
        boolean timedout = false;
        while (!fileToWaitFor.exists() && !timedout) {
            // Check every second
            Thread.currentThread().sleep(1000);
            if (testTimeoutSecs > 0 && System.currentTimeMillis() > timeLimit) {
                timedout = true;
            }
        }
        return !timedout;
    }
    
    /**
     * Utility method to wait for <code>expectedCount</code> output files to appear
     * under directory <code></code>. If it doesn't appear, stop waiting after given number of seconds.
     * @param fileNameToWaitFor the file to wait for
     * @param testTimeoutSecs the number of seconds to wait for it to appear. 0= wait infinitely
     * @return true if file exists, false if the wait timed out and it doesn't exist
     */
    int waitForOutput(File outDir, int testTimeoutSecs, int expectedCount, RegexFileFilter outputFilter) throws InterruptedException{
        long timeLimit = System.currentTimeMillis() + (testTimeoutSecs * 1000);
        int received = 0;
        boolean timedout = false;
        File[] entries = null;
        while ( !timedout ) {
            // Check every second
            Thread.currentThread().sleep(1000);
            //System.out.println("timeout=" + testTimeoutSecs + " limit=" + timeLimit + " curr time=" + System.currentTimeMillis());
            if (testTimeoutSecs > 0 && System.currentTimeMillis() > timeLimit) {
                timedout = true;
            }
            entries = outDir.listFiles(outputFilter);
            if ( entries != null ) {
                if ( entries.length >= expectedCount ) {
                    received = entries.length;
                    break;
                }
            }
        }
        return received;
    }

    /**
     * Test of inbound SOAP Request processing.
     *
     * ================Example property file================
     * description=?
     * destination=?
     * ######### Inputs #########
     * input.count=?
     * input.0.dataFile=?
     * input.0.templateFile=?
     * input.0.batchSize=?
     * ....
     * input.$N.templateFile=?
     * input.$N.dataFile=?
     * input.$N.batchSize=?
     *
     * ######### Output #########
     * output.count=?
     * output.0.resultFile=?
     * output.0.expResultFile=?
     * ....
     * output.M.actualResultFile=?
     * output.M.expectedResultFile=?
     *
     * ######## Script ###########
     * scriptFile=?
     * ===============End of example property file===========
     *
     * see Input.java for example data file and example template file
     *
     * ===============Example script file====================
     * send $inputName $numberOfBatches (for example: "send input.1 3")
     * wait $seconds (for example: "wait 2")
     * ===============End of example script file=============
     */
    public void testN2MInboundSOAPRequest() throws Exception {
        String description = mProperties.getProperty("description");
        String destination = mProperties.getProperty("destination");
        String testPropertiesFileName = mProperties.getProperty("testpropertiesfilename");
        String inputDir = mProperties.getProperty("absoluteinputdir");
        String inputDirName = mProperties.getProperty("inputdirname");
        String featureProgressVal = mProperties.getProperty(TEST_IN_PROGRESS_KEY);
        if (featureProgressVal != null && featureProgressVal.equals(TEST_IN_PROGRESS_VAL)) {
            System.out.println("Test " + inputDirName + "\\" + testPropertiesFileName
                    + " in Development");
            return;
        }
        System.out.print("Test " + inputDirName + "\\" + testPropertiesFileName);
        try {
            // inputs
            int inputCount = Integer.parseInt(mProperties.getProperty("input.count"));
            Map inputTable = new HashMap();
            for (int i = 0; i < inputCount; i++) {
                String name = "input." + i;
                String action = mProperties.getProperty(name + ".action");
                String dataFile = inputDir + File.separator + mProperties.getProperty(name + ".dataFile");
                String templateFile = inputDir + File.separator + mProperties.getProperty(name + ".templateFile");
                int batchSize = Integer.parseInt(mProperties.getProperty(name + ".batchSize"));
                inputTable.put(name, new Input(name, action, templateFile, dataFile, batchSize));
            }
            // outputs
            int outputCount = Integer.parseInt(mProperties.getProperty("output.count"));
            Map outputTable = new HashMap();
            for (int i = 0; i < outputCount; i++) {
                String name = "output." + i;
                File actual = new File(inputDir, mProperties.getProperty(name + ".actualResultFile"));
                File expected = new File(inputDir, mProperties.getProperty(name + ".expectedResultFile"));
                Output output = new Output(name, actual, expected);
                output.removeActual();
                outputTable.put(name, output);
            }
            // script
            File scriptFile = new File(inputDir + File.separator + mProperties.getProperty("scriptFile"));
            List taskList = loadScript(scriptFile, inputTable);
            for (int i = 0, I = taskList.size(); i < I; i++) {
                Runnable r = (Runnable)taskList.get(i);
                r.run();
            }
            for (int i = 0; i < outputCount; i++) {
                String name = "output." + i;
                Output output = (Output)outputTable.get(name);
                String exp = output.getExpectedWithoutCRNL();
                String act = output.getActualWithoutCRNL();
                assertEquals(name, exp, act);
                output.removeActual();
            }
            System.out.println(" Passed.");
        } catch (Exception ex) {
            System.out.println(" Failed.");
            throw ex;
        } catch (Error er) {
            System.out.println(" Failed.");
            throw er;
        }
    }
    
    public boolean testCompareXMLEquals(String controlStr, String actualStr) {
        return testCompareXMLIdentical(controlStr, actualStr, XDMUtil.ComparisonCriteria.EQUAL);
    }
    
    public boolean testCompareXMLIdentical(String controlStr, String actualStr) {
        return testCompareXMLIdentical(controlStr, actualStr, XDMUtil.ComparisonCriteria.IDENTICAL);
    }
    
    public boolean testCompareXMLIdentical(String controlStr, String actualStr, XDMUtil.ComparisonCriteria aCriteria) {
        boolean result=false;
        try {
            List diffs = xdmUtil.compareXML(controlStr, actualStr, aCriteria);
            
            // Note: XDMUtil has already filtered out unnecessary ns attribute 
            // differences. The rest of ns attribute differences are important 
            // and therefore should no longer be filtered out. See #108234.
            // this.filterNSAttrDiffs(diffs);
            
            this.filterNSPrefixDiffs(diffs);
            this.filterAttrWhitespaceDiffs(diffs);
            this.filterAttributeOrderChange(diffs);
            this.filterEmptySoapHeaderElements(diffs);

            if (diffs.size() == 0)
                result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
    private Map concurrentCorrelationBuildInfo(Properties properties) throws Exception {

        // first read all the input invokes : invoke1, invoke2 invoke3 etc.....
        Map invokePropertiesMap = new HashMap();

        String tokens = properties.getProperty("invokeList");

        StringTokenizer st = new StringTokenizer(tokens,",");
        while ( st.hasMoreTokens() ) {
                concurrentCorrelationAddToInvokeList(invokePropertiesMap,st.nextToken().trim());
        }
        return invokePropertiesMap;
    }
	

    private void concurrentCorrelationAddToInvokeList(Map invokePropertiesMap, String key) throws Exception {
        String inputFileTemplateString = "";
        String outputFileTemplateString = "";

        List inputValueList = null;
        List outputValueList = null;
        List soapActionValueList = null;
        List destinationValueList = null;

        inputValueList = new ArrayList();
        outputValueList = new ArrayList();

        // also read the substitution values
        String soapAction = mProperties.getProperty(key + ".soapAction");
        if ( soapAction == null ) {
        	soapAction = "";
        	//throw new Exception("soapAction not defined for " + key );
        }

        // also read the substitution values
        String destination = mProperties.getProperty(key + ".destination");
        if ( destination == null ) {
                throw new Exception("destination not defined for " + key);
        }

        FileReader fileReader  = null;
        BufferedReader in = null;
        String str = "";

        String inputFileTemplate = mProperties.getProperty(key + ".inputTemplate");
        String outputFileTemplate = mProperties.getProperty(key + ".outputTemplate");

        try {
            fileReader  = new FileReader(mProperties.getProperty("absoluteinputdir") + File.separator + inputFileTemplate);
            in = new BufferedReader(fileReader);

            while ((str = in.readLine()) != null) {
                inputFileTemplateString = inputFileTemplateString + str;
            }
        in.close();
        fileReader.close();

        str = "";
        fileReader  = new FileReader(mProperties.getProperty("absoluteinputdir") + File.separator + outputFileTemplate);
        in = new BufferedReader(fileReader);

        while ((str = in.readLine()) != null) {
            outputFileTemplateString = outputFileTemplateString + str;
        }
        in.close();
        fileReader.close();

        } catch (IOException e) {
            try { 
                if ( in != null ) in.close();
                if ( fileReader != null ) fileReader.close();

            } catch ( Exception e2 ) {				
            }
            throw e;
        }

        List allInputValuesList = new ArrayList();

        String inputValues = mProperties.getProperty(key + ".inputValues");

        StringTokenizer st = new StringTokenizer(inputValues,",");

        int sizeInner = 0;
        boolean sizeDefined = false;
        List inputValueID = new ArrayList();
        while ( st.hasMoreTokens() ) {
            String token = st.nextToken().trim();
            inputValueID.add(token);
            String inputValuesContent = mProperties.getProperty(key + ".inputValues." + token);
            StringTokenizer stInner = new StringTokenizer(inputValuesContent,"|");
            List inputValuesList = new ArrayList();
            while ( stInner.hasMoreTokens() ) {
                    inputValuesList.add(stInner.nextToken().trim());
            }
            if ( !sizeDefined ) {
                    sizeInner = inputValuesList.size();
                    sizeDefined = true;
            } else if ( sizeInner != inputValuesList.size() ) {
                    throw new Exception("inputValues for " + key + " should have a count :" + sizeInner );
            }
            allInputValuesList.add(inputValuesList);
        }

        //****************************** 
        // create the inputFiles for the given values.  
        // Size inner will tell the number of files to be created
        for ( int i=0; i < sizeInner; i++ ) { 
            String s =inputFileTemplateString;
            for ( int j=0; j < allInputValuesList.size(); j++ ) {
                    s = replaceValue(s, (String)inputValueID.get(j), (String)((List)allInputValuesList.get(j)).get(i)); 
            }

            // write the inputFileTemplateString to a file.
            File outFile = new File(mProperties.getProperty("absoluteinputdir") + File.separator + "input." + key + "." + i + ".xml");

            if( outFile.exists() ) {
                    outFile.delete();
            }

            outFile.createNewFile();

            FileWriter outputFw = new FileWriter(outFile);
            outputFw.write(s,0,s.length());
            outputFw.flush();
            outputFw.close();

            // create the InputSet 
            // Put all these values in a Property Object and return
            Map singleInvokeMap = new HashMap();

            singleInvokeMap.put("inputfile","input." + key + "." + i + ".xml");    			
            singleInvokeMap.put("soapaction",soapAction);
            singleInvokeMap.put("destination",destination);

            invokePropertiesMap.put(key + "." + i, singleInvokeMap);

        }

        //*******************************/

        List allOutputValuesList = new ArrayList();

        String outputValues = mProperties.getProperty(key + ".outputValues");

        sizeInner = 0;
        sizeDefined = false;

        st = new StringTokenizer(outputValues,",");

        List outputValueID = new ArrayList();

        while ( st.hasMoreTokens() ) {
            String token = st.nextToken().trim();
            outputValueID.add(token);
            String outputValuesContent = mProperties.getProperty(key + ".outputValues." + token);
            StringTokenizer stInner = new StringTokenizer(outputValuesContent,"|");
            List outputValuesList = new ArrayList();
            while ( stInner.hasMoreTokens() ) { 
                    outputValuesList.add(stInner.nextToken().trim());
            }
            if ( !sizeDefined ) {
                    sizeInner = outputValuesList.size();
                    sizeDefined = true;
            } else if ( sizeInner != outputValuesList.size() ) {
                    throw new Exception("outputValues for " + key + " should have a count :" + sizeInner );
            }
            allOutputValuesList.add(outputValuesList);
        }
        //****************************** 
        // create the outputFiles for the given values.  
        // Size inner will tell the number of files to be created
        for ( int i=0; i < sizeInner; i++ ) { 
            String s =outputFileTemplateString;
            for ( int j=0; j < allOutputValuesList.size(); j++ ) {
                    s = replaceValue(s, (String)outputValueID.get(j), (String)((List)allOutputValuesList.get(j)).get(i)); 
            }

            // write the inputFileTemplateString to a file.
            File outFile = new File(mProperties.getProperty("absoluteinputdir") + File.separator + "output." + key + "." + i + ".xml");

            if( outFile.exists() ) {
                    outFile.delete();
            }

            outFile.createNewFile();

            FileWriter outputFw = new FileWriter(outFile);
            outputFw.write(s,0,s.length());
            outputFw.flush();
            outputFw.close();

            Map singleInvokeMap = (Map)invokePropertiesMap.get(key + "." + i);

            singleInvokeMap.put("outputfile","output." + key + "." + i + ".xml");

        }

        return;

    }

    String replaceValue(String string, String id, String value) { 
		return string.replaceFirst("#" + id + "#", value);
    }
    
    
    /*
     * filters or removes diffs that are attr position changes
     */
    private void filterAttributeOrderChange(final List diffs) {
        List removeDiffs = new ArrayList();
        Iterator itr = diffs.iterator();
        while (itr.hasNext()) {
            Difference dif = (Difference) itr.next();
            if(dif instanceof Change) {
                Change c = (Change)dif;
                //filter attibute position changes only
                if(c.isAttributeChanged() && !c.isPositionChanged() && !c.isTokenChanged()) {
                    List attrdiffs = c.getAttrChanges();
                    int size = attrdiffs.size();
                    List removeAttrs = new ArrayList();
                    
                    Iterator attrDiffItr = attrdiffs.iterator();
                    while (attrDiffItr.hasNext()){
                        Change.AttributeDiff attrdif = (Change.AttributeDiff) attrDiffItr.next();
                        if(attrdif instanceof Change.AttributeChange) {
                            Change.AttributeChange attrChange =
                                    (AttributeChange) attrdif;
                            if(attrChange.isPositionChanged() && !attrChange.isTokenChanged())
                                removeAttrs.add(attrdif);
                        }
                    }
                    Iterator removeAttrItr = removeAttrs.iterator();
                    while (removeAttrItr.hasNext()) {
                        c.removeAttrChanges((Change.AttributeDiff) removeAttrItr.next());
                    }
                    
                    if(size > 0 && c.getAttrChanges().size() == 0)
                        removeDiffs.add(dif);
                }
            }
        }
        Iterator removeItr = removeDiffs.iterator();
        while (removeItr.hasNext()) {
            diffs.remove((Difference) removeItr.next());
        }
    }
    
    
                /*
                 * filters or removes diffs that are ns attr "xmlns:prefix='some url'"
                 */
    /*
    private void filterNSAttrDiffs(final List diffs) {
        List removeDiffs = new ArrayList();
        Iterator itr = diffs.iterator();
        while (itr.hasNext()) {
            Difference dif = (Difference) itr.next();
            if(dif instanceof Change) {
                Change c = (Change)dif;
                //filter namespace attibute changes only
                if(c.isAttributeChanged() && !c.isPositionChanged() &&
                        !c.isTokenChanged() && (removeNSAttrDiffs(c) ||
                        removeSchemaLocationAttrDiffs(c))) {
                    removeDiffs.add(dif);
                }
            }
        }
        Iterator removeItr = removeDiffs.iterator();
        while (removeItr.hasNext()) {
            diffs.remove((Difference) removeItr.next());
        }
    }
    */
    
    
                /*
                 * filters or removes diffs that are token changes <h:somename> -> <a:somename>
                 */
    private void filterNSPrefixDiffs(final List diffs) {
        List removeDiffs = new ArrayList();
        Iterator itr = diffs.iterator();
        while (itr.hasNext()) {
            Difference dif = (Difference) itr.next();
            if(dif instanceof Change) {
                Change c = (Change)dif;
                if(c.isTokenChanged() && !c.isPositionChanged()) {
                    String oName = c.getOldNodeInfo().getNode().getNodeName().trim();
                    oName = oName.substring(oName.indexOf(':')!=-1?oName.indexOf(':')+1:0);
                    String nName = c.getNewNodeInfo().getNode().getNodeName().trim();
                    nName = nName.substring(nName.indexOf(':')!=-1?nName.indexOf(':')+1:0);
                    if(oName.equals(nName) && (!c.isAttributeChanged() ||
                            c.isAttributeChanged() && (removeNSAttrDiffs(c) ||
                            removeSchemaLocationAttrDiffs(c))))
                        removeDiffs.add(dif);
                }
            }
        }
        Iterator removeItr = removeDiffs.iterator();
        while (removeItr.hasNext()) {
            diffs.remove((Difference) removeItr.next());
        }
    }
    
    
        /*
         * filters or removes diffs that are attr whitespace changes x="y" -> x ="y" or x= "y"
         */
    private void filterAttrWhitespaceDiffs(final List diffs) {
        List removeDiffs = new ArrayList();
        Iterator itr = diffs.iterator();
        while (itr.hasNext()) {
            Difference dif = (Difference) itr.next();
            if(dif instanceof Change) {
                Change c = (Change)dif;
                //filter whitespace between attibute changes only
                if(c.isAttributeChanged() && !c.isPositionChanged() && !c.isTokenChanged()) {
                    List attrdiffs = c.getAttrChanges();
                    int size = attrdiffs.size();
                    List removeAttrs = new ArrayList();
                    Iterator attrDiffItr = attrdiffs.iterator();
                    while (attrDiffItr.hasNext()){
                        Change.AttributeDiff attrdif = (Change.AttributeDiff) attrDiffItr.next();
                        if(attrdif instanceof Change.AttributeChange) {
                            Change.AttributeChange attrChange =
                                    (AttributeChange) attrdif;
                            if(!attrChange.isPositionChanged()) {
                                Attribute oldAttr = attrdif.getOldAttribute();
                                Attribute newAttr = attrdif.getNewAttribute();
                                if(oldAttr != null && newAttr != null &&
                                        oldAttr.getNodeValue().trim().equals(
                                        newAttr.getNodeValue().trim()))
                                    removeAttrs.add(attrdif);
                            }
                        }
                    }
                    Iterator removeAttrsItr = removeAttrs.iterator();
                    while (removeAttrsItr.hasNext()) {
                        c.removeAttrChanges((Change.AttributeDiff) removeAttrsItr.next());
                    }
                    if(size > 0 && attrdiffs.size() == 0)
                        removeDiffs.add(dif);
                }
            }
        }
        Iterator removeItr = removeDiffs.iterator();
        while (removeItr.hasNext()) {
            diffs.remove((Difference) removeItr.next());
        }
    }
    

    private void filterEmptySoapHeaderElements(final List diffs) {
        List removeDiffs = new ArrayList();
        Iterator itr = diffs.iterator();
        while (itr.hasNext()) {
            Difference dif = (Difference) itr.next();
            if (dif instanceof Delete) {
                Delete d = (Delete)dif;
                Node node = d.getOldNodeInfo().getNode();
                if (isEmptySoapHeaderNode(node)) {
                    removeDiffs.add(dif);
                }
            } else if (dif instanceof Add) {
                Add add = (Add)dif;
                Node node = add.getNewNodeInfo().getNode();
                if (isEmptySoapHeaderNode(node)) {
                    removeDiffs.add(dif);
                }
            }
        }
        Iterator removeItr = removeDiffs.iterator();
        while (removeItr.hasNext()) {
            diffs.remove((Difference) removeItr.next());
        }
    }
 

    private boolean isEmptySoapHeaderNode(Node node) {
        if (node instanceof Element &&
            "http://schemas.xmlsoap.org/soap/envelope/".equals(node.getNamespaceURI()) &&
            "Header".equals(node.getLocalName()) &&
            node.getChildNodes().getLength() == 0) {
            return true;
        }
        return false;
    }

        /*
         * removes attr diffs that are ns attr "xmlns:prefix='some url'"
         */
    private boolean removeNSAttrDiffs_(Change c) {
        List attrdiffs = c.getAttrChanges();
        int size = attrdiffs.size();
        List removeAttrs = new ArrayList();
        Iterator attrdiffsItr = attrdiffs.iterator();
        while (attrdiffsItr.hasNext()) {
            Change.AttributeDiff attrdif = (Change.AttributeDiff) attrdiffsItr.next();
            Attribute oldAttr = attrdif.getOldAttribute();
            Attribute newAttr = attrdif.getNewAttribute();
            if(oldAttr != null && oldAttr.getName().startsWith(NS_PREFIX))
                removeAttrs.add(attrdif);
            else if(newAttr != null && newAttr.getName().startsWith(NS_PREFIX))
                removeAttrs.add(attrdif);
        }
        Iterator removeAttrItr = removeAttrs.iterator();
        while (removeAttrItr.hasNext()) {
            c.removeAttrChanges((Change.AttributeDiff) removeAttrItr.next());
        }
        
        if(size > 0 && attrdiffs.size() == 0)
            return true;
        return false;
    }
        /*
         * removes attr diffs that are ns attr "xmlns:prefix='some url'"
         */
    private boolean removeNSAttrDiffs(Change c) {
        List attrdiffs = c.getAttrChanges();
        int size = attrdiffs.size();
        List removeAttrs = new ArrayList();
        Iterator attrdiffsItr = attrdiffs.iterator();
        while (attrdiffsItr.hasNext()) {
            Change.AttributeDiff attrdif = (Change.AttributeDiff) attrdiffsItr.next();
            Attribute oldAttr = attrdif.getOldAttribute();
            Attribute newAttr = attrdif.getNewAttribute();
            if(oldAttr != null && oldAttr.getName().startsWith(NS_PREFIX))
                removeAttrs.add(attrdif);
            else if(newAttr != null && newAttr.getName().startsWith(NS_PREFIX))
                removeAttrs.add(attrdif);
        }
        Iterator removeAttrItr = removeAttrs.iterator();
        while (removeAttrItr.hasNext()) {
            c.removeAttrChanges((Change.AttributeDiff) removeAttrItr.next());
        }
        
        if(size > 0 && attrdiffs.size() == 0)
            return true;
        return false;
    }
    
        /*
         * removes attr diffs that are ns attr "prefix:schemaLocation='some url'"
         */
    private boolean removeSchemaLocationAttrDiffs(Change c) {
        List attrdiffs = c.getAttrChanges();
        int size = attrdiffs.size();
        List removeAttrs = new ArrayList();
        Iterator attrdiffsItr = attrdiffs.iterator();
        while (attrdiffsItr.hasNext()) {
            Change.AttributeDiff attrdif = (Change.AttributeDiff) attrdiffsItr.next();
            Attribute oldAttr = attrdif.getOldAttribute();
            Attribute newAttr = attrdif.getNewAttribute();
            if(oldAttr != null && oldAttr.getName().endsWith(SCHEMA_LOCATION))
                removeAttrs.add(attrdif);
            else if(newAttr != null && newAttr.getName().endsWith(SCHEMA_LOCATION))
                removeAttrs.add(attrdif);
        }
        Iterator removeAttrItr = removeAttrs.iterator();
        while (removeAttrItr.hasNext()) {
            c.removeAttrChanges((Change.AttributeDiff) removeAttrItr.next());
        }
        if(size > 0 && attrdiffs.size() == 0)
            return true;
        return false;
    }

    private String removeIndent(String inputStr, String indent) {
        //workaround method bug on XDMUtil pretty print. remove newline after prolog and extra indent.
        int indentSize = indent.length();
        String inputLine;
        StringBuffer strBuf = new StringBuffer();
        
        try {
            BufferedReader inLines = new BufferedReader(new StringReader(inputStr));
            inputLine=inLines.readLine();
            //prolog
            if (inputLine != null) {
                strBuf.append(inputLine+"\n");
            }
            
            // skip second line if it's empty
            inputLine=inLines.readLine();
            if (inputLine != null && inputLine.trim().length() > 0) {
                strBuf.append(inputLine+"\n");
            }
            
            // remove indent at first element
            inputLine=inLines.readLine();
            if (inputLine != null) {
                if (inputLine.substring(0,indentSize).equals(indent)) {
                    strBuf.append(inputLine.substring(indentSize) + "\n");
                } else {
                    strBuf.append(inputLine + "\n");
                }
            }
            
            // read rest of contents
            int chunksize = 512;
            char[] buff = new char[chunksize];
            int len = inLines.read(buff);
            while (len > 0) {
                strBuf.append(buff, 0, len);
                len = inLines.read(buff);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strBuf.toString();
    }
    
    private static String[] parseCommand(String command) {
        StringTokenizer st = new StringTokenizer(command, " ");
        List<String> list = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            list.add(st.nextToken());
        }
        return list.toArray(new String[0]);
    }
    
    private List loadScript(File scriptFile, Map<String, Input> inputTable) {
        List<Runnable> taskList = new ArrayList<Runnable>();
        BufferedReader fileIn = null;
        try {
            fileIn = new BufferedReader(new InputStreamReader(new FileInputStream(scriptFile)));
            while (true) {
                String line = fileIn.readLine();
                if (line == null) {
                    break;
                }
                if (line.startsWith("#")) {
                    continue; // skip comment
                }
                String[] cmd = parseCommand(line);
                if ("send".startsWith(cmd[0])) {
                    // send input.0 3
                    taskList.add(new Send(inputTable.get(cmd[1]), cmd[2]));
                } else if ("wait".startsWith(cmd[0])) {
                    // wait 3
                    taskList.add(new Wait(cmd[1]));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileIn != null) {
                    fileIn.close();
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return taskList;
    }
    
    class Send implements Runnable {
        String mExpectedHttpWarning;
        String mDestination;
        Input mInput;
        int mBatches;
        
        public Send(Input input, String batches) throws Exception {
            mDestination = mProperties.getProperty("destination");
            mExpectedHttpWarning = mProperties.getProperty("httpwarning");
            
            mInput = input;
            mBatches = Integer.parseInt(batches);
        }
        
        public void run() {
            String action = mInput.getAction();
            for (int i = 0; i < mBatches; i++) {
                try {
                    String data = mInput.nextData();
//                    System.out.println("data: " + data);
                    SOAPMessage message = mMessageFactory.createMessage();
                    message.getMimeHeaders().addHeader("soapaction", action);
                    SOAPPart soapPart = message.getSOAPPart();
                    soapPart.setContent(new StreamSource(new StringReader(data)));
                    message.saveChanges();
                    sendMessage(mInput.getName(), false, mDestination, message, null, mExpectedHttpWarning, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    class Wait implements Runnable {
        long miliSec = 1000L;
        public Wait(String secondsStr) throws Exception {
            miliSec = Math.max(1L, Math.round(1000*Double.parseDouble(secondsStr)));
        }
        
        public synchronized void run() {
            try {
                wait(miliSec);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Short form of the standard println API.
     * @param mesg The message.
     */
    static void println(Object mesg) {
        System.out.println(mesg);
    }
    
    
    /**
     * Short form of the standard print API.
     * @param mesg The message.
     */
    static void print(Object mesg) {
        System.out.print(mesg);
    }

    
    public final static void main(String[] args) {
        //new ConfiguredTest("testInboundSOAPRequest").run();
        //new ConfiguredTest("testInboundSOAPRequest", "testInboundSOAPRequest").run();
        //System.out.println("Hello");
        try {
            junit.framework.Test test = ConfiguredTest.suite();
            //new ConfiguredTest("testCorrelationSOAPRequest", "testCorrelationSOAPRequest").run();
            test.run(new TestResult());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    
    public final static void main1(String[] args) throws Exception {
        Properties props = System.getProperties();
        System.out.println(props);

        System.out.println("@@@@@@@@@@@Current path : " + System.getProperty("user.dir"));
        
        String path = "C:\\Documents and Settings\\jqian\\Desktop\\108234\\AssignNamespacesJBI\\";
        //String path = args[0]; //"C:\\Alaska_DriverTest\\catdsrc\\";
        
        Properties testcasesProps = loadProperties(path + "test/selected-tests.properties");
        String testCasesCSV = (String)testcasesProps.get("testcases");
        String [] testCaseNames = testCasesCSV.split(",");
        List testCases = Arrays.asList(testCaseNames);

         File[] inputDir = new File(path + "test").listFiles();
        if (inputDir != null) {
            for (int count = 0; count < inputDir.length; count++) {
                final String testPropertiesPostfix = ".properties";

                File currentDir = inputDir[count];
                if (currentDir.isDirectory() && testCases.contains(currentDir.getName())) {
                    String inputDirName = inputDir[count].getName();
                    String inputDirAbsolutePath = inputDir[count].getAbsolutePath();

                    FileFilter testPropertiesFilter = new FileFilter() {
                        public boolean accept(File f) {
                            if (f.isDirectory()) return false;
                            return f.getName().endsWith(testPropertiesPostfix);
                        }
                    };

                    File[] testPropertiesFiles = inputDir[count].listFiles(testPropertiesFilter);

                    if (testPropertiesFiles != null) {
                        for (int testCnt = 0; testCnt < testPropertiesFiles.length; testCnt++) {
                            String testPropertiesFile = testPropertiesFiles[testCnt].getAbsolutePath();
                            String testName = inputDirName;
                            Properties testProps = loadProperties(testPropertiesFile);
                            testProps.put("testpropertiesfilename", testPropertiesFiles[testCnt].getName());
                            testProps.put("absoluteinputdir", inputDirAbsolutePath);
                            testProps.put("inputdirname", inputDirName);

                            ConfiguredTest ct = new ConfiguredTest(testName, "testConcurrentSOAPRequest", testProps);
                            ct.setUp();
                            //ct.testConcurrentCorrelationSOAPRequest();
                            ct.testConcurrentCorrelationSOAPRequest();
                        }
                    }
                }
            }
        }
    	
    }
    
    class RegexFileFilter implements FileFilter {
        private Pattern pattern;
        public RegexFileFilter(String regex) {
            this.pattern = Pattern.compile(regex);
        } 
        public boolean accept(File f) {
            if (f.isDirectory()) return false;
            Matcher m = this.pattern.matcher(f.getName());
            return m.matches();
        }
    };
}
