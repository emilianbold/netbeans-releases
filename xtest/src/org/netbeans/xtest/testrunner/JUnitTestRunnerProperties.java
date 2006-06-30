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


import java.io.*;
import java.util.*;
import org.netbeans.junit.Filter;


/**
 *
 * @author  breh
 */
public class JUnitTestRunnerProperties {


    // junit runner understood properties
    // suites and their filters - in real property file
    // suites are present as:
    // xtest.junit-test-runner.testsuite/1=my.test.MySuite
    // xtest.junit-test-runner.testsuite.filter.exclude/1/1=my.test.MySuite.myExclFilteredMethod
    // xtest.junit-test-runner.testsuite.filter.include/1/2=my.test.MySuite.myInclFilteredMethod
    static final String TEST_TO_EXECUTE="xtest.junit-test-runner.test/";
    static final String TEST_FILTER_INCLUDE="xtest.junit-test-runner.test-filter.include/";
    static final String TEST_FILTER_EXCLUDE="xtest.junit-test-runner.test-filter.exclude/";
    static final String TEST_FILTER_EXPECTED_FAIL="xtest.junit-test-runner.test-filter.expected-fail/";
    static final String TESTBAG_SETUP_CLASS="xtest.testbag.setup.class";
    static final String TESTBAG_TEARDOWN_CLASS="xtest.testbag.teardown.class";
    static final String TESTBAG_SETUP_METHOD="xtest.testbag.setup.method";
    static final String TESTBAG_TEARDOWN_METHOD="xtest.testbag.teardown.method";
    
    // testrun type stuff
    static final String TESTRUN_TYPE="xtest.junit-test-runner.testrun-type";

    
    // dir property where xml results should be places
    public static final String RESULTS_DIRECTORY="xtest.junit-test-runner.result-dir";    
    
    private static final String HEADER="JUnitTestRunner properties";
    
    // index of added test names
    private int testIndex;
    
    // indicates whether properties need to be parsed
    private boolean needToParse;
    
    /** Creates a new instance of JUnitTestRunnerProperties */
    public JUnitTestRunnerProperties() {
        runnerProperties = new Properties();
        needToParse = true;
        testIndex=0;
    }
    
    
    public static JUnitTestRunnerProperties load(String filename) throws IOException {
        return load(new File(filename));
    }
    
    public static JUnitTestRunnerProperties load(File file) throws IOException {
        JUnitTestRunnerProperties jutrProperties = new JUnitTestRunnerProperties();
        jutrProperties.runnerProperties =  new Properties();
        InputStream is = new FileInputStream(file);
        jutrProperties.runnerProperties.load(is);
        // parse the properties
        jutrProperties.parseRunnerProperties();
        is.close();
        return jutrProperties;             
    }
    
    public void save(File propFile, boolean overwrite) throws IOException {

        if (propFile.exists()) {
            if (!overwrite) {                
                throw new IOException("File "+propFile+" already exists");
            }
        }
        OutputStream os = new FileOutputStream(propFile);
        runnerProperties.store(os, HEADER);
        os.close();
    }
    
    public void save(File propFile) throws IOException {
        save(propFile,true);
    }    

    public void save(String filename, boolean overwrite) throws IOException {
        File propFile = new File(filename);
        save(propFile,overwrite);
    }
    
    public void save(String filename) throws IOException {
        save(filename,true);
    }
    
    

    // API
    public void addTestName(String testName) {
        addTestNameWithFilter(testName,null,null);
    }
    
    public void addTestNameWithFilter(String testName, Filter.IncludeExclude[] includes, Filter.IncludeExclude[] excludes) {
        runnerProperties.setProperty(getTestPropertyName(testIndex),testName);
        if (includes != null) {
            for (int i=0; i < includes.length; i++) {
                if (includes[i].getName() != null)
                    runnerProperties.setProperty(getIncludeFilterPropertyName(testIndex, i),includes[i].getName());
                if (includes[i].getExpectedFail() != null)
                    runnerProperties.setProperty(getExpectedFailFilterPropertyName(testIndex, i),includes[i].getExpectedFail());
            }
        }
        if (excludes != null) {
            for (int i=0; i < excludes.length; i++) {
                runnerProperties.setProperty(getExcludeFilterPropertyName(testIndex, i),excludes[i].getName());
            }
        }
        testIndex++;
        needToParse = true;
    }
    
    public String[] getTestNames() {
        parseRunnerProperties();
        return testNames;
    }
    
    public Filter.IncludeExclude[] getTestFilterIncludes(int testIndex) {
        parseRunnerProperties();
        if ((testIndex >= 0) & (testIndex < testFilterIncludes.length)) {            
            return testFilterIncludes[testIndex];
        } else {
            return null;
        }
    }
        
    public Filter.IncludeExclude[] getTestFilterExcludes(int testIndex) {
        parseRunnerProperties();
        if ((testIndex >= 0) & (testIndex < testFilterExcludes.length)) {
            /*
            for (int i=0; i < testFilterExcludes[testIndex].length; i++) {
                System.out.println("**** Returning exclude:"+testFilterExcludes[testIndex][i]);
            }
             */
            return testFilterExcludes[testIndex];
        } else {
            return null;
        }        
    }
    
    public String getTestRunType() {
        return runnerProperties.getProperty(TESTRUN_TYPE);
    }
    
    public void setTestRunType(String value) {
        if (value != null) {
            runnerProperties.setProperty(TESTRUN_TYPE,value);
        }
    }
    
    public String getTestbagSetupClassName() {
        return setupClassName;
    }
    
    public String getTestbagSetupMethodName() {
        return setupMethodName;
    }
    
    public String getTestbagTeardownClassName() {
        return teardownClassName;
    }
    
    public String getTestbagTeardownMethodName() {
        return teardownMethodName;
    }
    
    public void setTestbagSetup(String className, String methodName) {
        if ((className == null) || (methodName == null)) {
            throw new NullPointerException("className or methodName cannot be null");
        }
        setupClassName = className;        
        setupMethodName = methodName;
        runnerProperties.setProperty(TESTBAG_SETUP_CLASS,className);
        runnerProperties.setProperty(TESTBAG_SETUP_METHOD,methodName);
    }

    
    public void setTestbagTeardown(String className, String methodName) {
        if ((className == null) || (methodName == null)) {
            throw new NullPointerException("className or methodName cannot be null");
        }
        teardownClassName = className;
        teardownMethodName = methodName;
        runnerProperties.setProperty(TESTBAG_TEARDOWN_CLASS,className);
        runnerProperties.setProperty(TESTBAG_TEARDOWN_METHOD,methodName);
    }    
    
    
    public String getResultsDirName() {
        return runnerProperties.getProperty(RESULTS_DIRECTORY);
    }
    
    public void setResultsDirName(String value) {
        if (value != null) {
            runnerProperties.setProperty(RESULTS_DIRECTORY,value);
        }
    }
    
    public JUnitTestRunnerProperties[] divideByTests() {
        Vector properties = new Vector();
        parseRunnerProperties();
        // testbag setup
        if (getTestbagSetupClassName() != null) {
            JUnitTestRunnerProperties prop = new JUnitTestRunnerProperties();
            prop.setTestbagSetup(getTestbagSetupClassName(), getTestbagSetupMethodName());
            prop.setResultsDirName(getResultsDirName());
            properties.add(prop);
        }
        // testbag tests
        for (int i=0; i<testNames.length; i++) {
            JUnitTestRunnerProperties prop = new JUnitTestRunnerProperties();
            prop.setResultsDirName(getResultsDirName());            
            prop.setTestRunType(getTestRunType());
            prop.addTestNameWithFilter(testNames[i], testFilterIncludes[i], testFilterExcludes[i]);
            // is parsing really necessary ????
            prop.parseRunnerProperties();
            properties.add(prop);
        }
        // testbag teardown
        if (getTestbagTeardownClassName() != null) {
            JUnitTestRunnerProperties prop = new JUnitTestRunnerProperties();
            prop.setTestbagTeardown(getTestbagTeardownClassName(),getTestbagTeardownMethodName());
            prop.setResultsDirName(getResultsDirName());
            properties.add(prop);
        }
        
        return (JUnitTestRunnerProperties[])properties.toArray(new JUnitTestRunnerProperties[0]);
    }
    
    
    
    public void list(PrintStream out) {
        runnerProperties.list(out);
    }
    
    //public String 
    
    // private stuff
    
    // properties - they have it all :-)
    private Properties runnerProperties;
    // test names
    private String[] testNames;
    // includes
    private Filter.IncludeExclude[][] testFilterIncludes;
    // excludes
    private Filter.IncludeExclude[][] testFilterExcludes;
    
    // setup/teardown classes and methods
    private String setupClassName;
    private String setupMethodName;
    private String teardownClassName;
    private String teardownMethodName;
    
    
    
    
   // get the first number from tests/filterr in format "TEST_/testID{/filterID)"
    static Integer getTestID(String propertyName) {
        if (propertyName == null) throw new NullPointerException("propertyName cannot be null");
        Integer testID = null;
        String testIDString = null;
        if (propertyName.startsWith(TEST_TO_EXECUTE)) {
            testIDString = propertyName.substring(TEST_TO_EXECUTE.length());
            // we should have suite number string - convert it to suiteNumber            

        } else if (propertyName.startsWith(TEST_FILTER_INCLUDE)) {
            // filter is a bit more complicated
            String filterSuffix = propertyName.substring(TEST_FILTER_INCLUDE.length());
            testIDString = filterSuffix.substring(0,filterSuffix.lastIndexOf('/'));
        } else if (propertyName.startsWith(TEST_FILTER_EXCLUDE)) {
            // filter is a bit more complicated
            String filterSuffix = propertyName.substring(TEST_FILTER_EXCLUDE.length());
            testIDString = filterSuffix.substring(0,filterSuffix.lastIndexOf('/'));
        } else if (propertyName.startsWith(TEST_FILTER_EXPECTED_FAIL)) {
            // filter is a bit more complicated
            String filterSuffix = propertyName.substring(TEST_FILTER_EXPECTED_FAIL.length());
            testIDString = filterSuffix.substring(0,filterSuffix.lastIndexOf('/'));
        }
        try {
            if (testIDString != null) {
                testID = new Integer(testIDString);
            }
        } catch (NumberFormatException nfe) {
            // id cannot be converted
            // just swallow the exception and return null;
        }
        return testID;
    }
    
    // get the second number from filter in format "TEST_FILTER/testID/filterID"
    static Integer getFilterID(String propertyName) {
        if (propertyName == null) throw new NullPointerException("propertyName cannot be null");
        Integer filterID = null;
        String filterIDString = null;
        if (propertyName.startsWith(JUnitTestRunnerProperties.TEST_FILTER_INCLUDE)) {            
            filterIDString = propertyName.substring(propertyName.lastIndexOf('/')+1);
        } else if (propertyName.startsWith(JUnitTestRunnerProperties.TEST_FILTER_EXCLUDE)) {
            filterIDString = propertyName.substring(propertyName.lastIndexOf('/')+1);
        } else if (propertyName.startsWith(JUnitTestRunnerProperties.TEST_FILTER_EXPECTED_FAIL)) {
            filterIDString = propertyName.substring(propertyName.lastIndexOf('/')+1);
        }
        try {
            if (filterIDString != null) {
                filterID = new Integer(filterIDString);
            }
        } catch (NumberFormatException nfe) {
            // id cannot be converted
            // just swallow the exception and return null;
        }        
        return filterID; 
    }
    
    // private method, which parses the property object and fills arrays test, filters ...
    void parseRunnerProperties() {
        if (needToParse) {            
            HashMap testNames = new HashMap();
            HashMap filterIncludes = new HashMap();
            HashMap filterExcludes = new HashMap();
            Enumeration keys = runnerProperties.keys();
            while (keys.hasMoreElements()) {
                String key = (String)keys.nextElement();
                if (key.startsWith(TEST_TO_EXECUTE)) {
                    // get the suite number
                    Integer testID = getTestID(key);
                    String testName = runnerProperties.getProperty(key);
                    if (testName != null) {
                        testNames.put(testID, testName);
                    } else {
                        // test name is null - this suite is ignored
                        // there is also no reason to notify user
                        // only for debugging purposes
                        System.err.println("Cannot find test class for key "+key);
                    }
                } else if (key.startsWith(TESTBAG_SETUP_CLASS)) {
                    setupClassName = runnerProperties.getProperty(key);
                } else if (key.startsWith(TESTBAG_SETUP_METHOD)) {
                    setupMethodName = runnerProperties.getProperty(key);
                } else if (key.startsWith(TESTBAG_TEARDOWN_CLASS)) {
                    teardownClassName = runnerProperties.getProperty(key);
                } else if (key.startsWith(TESTBAG_TEARDOWN_METHOD)) {
                    teardownMethodName = runnerProperties.getProperty(key);                
                    
                } else if (key.startsWith(TEST_FILTER_INCLUDE) | key.startsWith(TEST_FILTER_EXCLUDE) | 
                           key.startsWith(TEST_FILTER_EXPECTED_FAIL)) {
                    String value = runnerProperties.getProperty(key);
                    if (value != null) {
                        Integer testID = getTestID(key);
                        if (key.startsWith(TEST_FILTER_INCLUDE)) {
                            // install include filter if not yet installed
                            TreeMap includeFilters = (TreeMap)filterIncludes.get(testID);
                            if (includeFilters == null) {
                                includeFilters = new TreeMap();
                                filterIncludes.put(testID,includeFilters);
                            }
                            Integer filterID = getFilterID(key);
                            Filter.IncludeExclude valueObject = (Filter.IncludeExclude)includeFilters.get(filterID);
                            if (valueObject == null) {
                                valueObject = new Filter.IncludeExclude();
                                includeFilters.put(filterID, valueObject);
                            }
                            valueObject.setName(value);
                        } else if (key.startsWith(TEST_FILTER_EXCLUDE)) {
                            // install exclude filter
                            TreeMap excludeFilters = (TreeMap)filterExcludes.get(testID);
                            if (excludeFilters == null) {
                                excludeFilters = new TreeMap();
                                filterExcludes.put(testID,excludeFilters);
                            }
                            excludeFilters.put(getFilterID(key), new Filter.IncludeExclude(value, null));
                        } else if (key.startsWith(TEST_FILTER_EXPECTED_FAIL)) {
                            // install expected fails
                            TreeMap includeFilters = (TreeMap)filterIncludes.get(testID);
                            if (includeFilters == null) {
                                includeFilters = new TreeMap();
                                filterIncludes.put(testID,includeFilters);
                            }
                            Integer filterID = getFilterID(key);
                            Filter.IncludeExclude valueObject = (Filter.IncludeExclude)includeFilters.get(filterID);
                            if (valueObject == null) {
                                valueObject = new Filter.IncludeExclude();
                                includeFilters.put(filterID, valueObject);
                            }
                            valueObject.setExpectedFail(value);
                        }
                    }
                }
            }
            
            // now create arrays from test names and filter includes/excludes
            this.testNames = new String[testNames.size()];
            // incldues/excludes arrays
            this.testFilterIncludes = new Filter.IncludeExclude[testNames.size()][];
            this.testFilterExcludes = new Filter.IncludeExclude[testNames.size()][];
            int i=0;
            // sort the keys in ascending order
            List keyList = new ArrayList();
            keyList.addAll(testNames.keySet());
            Collections.sort(keyList);            
            Iterator keyListIterator = keyList.iterator();
            while (keyListIterator.hasNext()) {
                Integer key = (Integer)keyListIterator.next();
                String testName = (String)testNames.get(key);
                this.testNames[i] = testName;
                // now includes/excludes
                if (filterIncludes.containsKey(key)) {
                    TreeMap includes = (TreeMap)filterIncludes.get(key);
                    // convert the includes into String array which then assign to testFilterIncludes;
                    this.testFilterIncludes[i] = (Filter.IncludeExclude[])(includes.values().toArray(new Filter.IncludeExclude[0]));
                }
                if (filterExcludes.containsKey(key)) {
                    TreeMap excludes = (TreeMap)filterExcludes.get(key);
                    // convert the excludes into String array which then assign to testFilterExcludes;
                    this.testFilterExcludes[i] = (Filter.IncludeExclude[])(excludes.values().toArray(new Filter.IncludeExclude[0]));
                }
                i++;
            }
            testIndex = i;
            needToParse = false;
        }
    }
    
    //
    private static String getTestPropertyName(int testIndex) {
        return TEST_TO_EXECUTE+testIndex;
    }
    
    private static String getIncludeFilterPropertyName(int testIndex, int filterIndex) {
        return TEST_FILTER_INCLUDE+testIndex+'/'+filterIndex;
    }
    
    private static String getExcludeFilterPropertyName(int testIndex, int filterIndex) {
        return TEST_FILTER_EXCLUDE+testIndex+'/'+filterIndex;
    }    
    
    private static String getExpectedFailFilterPropertyName(int testIndex, int filterIndex) {
        return TEST_FILTER_EXPECTED_FAIL+testIndex+'/'+filterIndex;
    }
    
    
}
