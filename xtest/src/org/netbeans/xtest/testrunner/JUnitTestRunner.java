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
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * JUnitTestRunner.java
 *
 * Created on November 19, 2002, 1:51 PM
 */

package org.netbeans.xtest.testrunner;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

import junit.framework.*;

import java.util.*;

import org.netbeans.junit.Filter;
import org.netbeans.junit.NbTest;
import org.netbeans.junit.NbTestSuite;

import org.netbeans.xtest.pe.*;

import java.io.*;



/**
 *
 * @author  breh
 */
public class JUnitTestRunner {
    
    // debug method for testing purposes - DEBUG should be set to false in production code
    private static final boolean DEBUG = false;
    private static void debugInfo(final String message) {
        if (DEBUG) {
            System.out.println("JUnitTestRunner:"+message);
        }
    }
    
    // test suite method name - taken from junit.runner.BaseTestRunner (should 
    // we take it directly from this place ?
    public static final String SUITE_METHODNAME= "suite";
        
    public static final String RUNNER_PROPERTIES_KEY = "runnerProperties";
    
    // test runner property file name
    public static final String TESTRUNNER_PROPERTY_FILENAME = "testrunner.properties";
    
    
    // exit states
    public static final int OK = 0;
    public static final int ERROR = -1;

    
    // our printwriter to which all messages are printed out
    // currently this is just stdout;
    private PrintWriter out = new PrintWriter(System.out,true);
    //private PrintStream out = System.out;
    
    // test runner properties - it is better to use
    // property file than to transfer parameters via command line, 
    // because on some OSes there is a very short command line length
    private JUnitTestRunnerProperties runnerProperties;    
    
    // result processor used for tracking this testrun
    // currently set here, but we should allow user to 
    // set the processor by own selection
    private JUnitTestListener[] resultProcessors;
    
    
    // custom classloader used for loading tests (if not defined,
    // tests are loaded via Class.forName();
    private ClassLoader testLoader;
    
    
    
    /** Creates a new instance of JUnitTestRunner */
    public JUnitTestRunner(JUnitTestRunnerProperties runnerProperties, ClassLoader testLoader) 
                throws IllegalArgumentException {
        
        //System.out.println("JUnitTestRunner: runnerProperties = "+runnerProperties);        
        this.runnerProperties = runnerProperties;
        this.testLoader = testLoader;
        // this should be placed elsewhere, just a hack for now
        checkRunnerPropertiesValidity();        
    }
    
    protected JUnitTestListener[] getJUnitTestListeners() {
        JUnitTestListener[] listeners;
        if ( getResultsDirectory() != null ) {
            // create the result processors
            listeners = new JUnitTestListener[] {
                new ConsoleSummaryReporter(out),
                new XMLReporter(getResultsDirectory())
            };
        } else {
            out.println("! Results Directory is not available - not storing results to xml files");
            listeners = new JUnitTestListener[] {
                new ConsoleSummaryReporter(out)
            };            
        }        
        return listeners;
    }
    
    /** run the junit tests */
    public void runTests() {

        // get result processors
       resultProcessors = getJUnitTestListeners();
        
        TestSuite[] suites = getTestSuites();
        if (suites == null) {
            // something is wrong - throw Exception ?
            return;
        }
        
        // test whether we run testbag or just test suite
        String testrunType = runnerProperties.getTestRunType();
        /*
        if (testrunType.equals(TESTRUN_TYPE_TESTBAG)) {
            // possibly fire start testbag 
            // TBD !!!
        }
         */
        
        // start the each suite ....
        for (int i=0; i < suites.length; i++) {
            TestSuite currentSuite = suites[i];
            TestResult suiteResult = new TestResult();
            // add listeners for the testsuite
            addTestListeners(suiteResult);
            // suite start
            fireStartTestSuite(currentSuite);
            // run the suite (NbTestSuites should have attached filter if applicable)
            currentSuite.run(suiteResult);
            // suite done
            fireEndTestSuite(currentSuite, suiteResult);
        }
        /*
        if (testrunType.equals(TESTRUN_TYPE_TESTBAG)) {
            // possibly fire end testbag 
            // TBD !!!
        }
         */
    }
    
    
    
    // main is called only when running tests in their own VM (in XTest 'code' mode)
    public static void main(String[] args) {
        try {
            System.out.println("Running JUnit tests");
            final String runnerPropertiesString = RUNNER_PROPERTIES_KEY+"=";
            if (args.length == 0) {
                System.err.println("JUnitTestRunner expects runnerProperties='path/to/property/file' parameter");
                System.exit(ERROR);
            }
            JUnitTestRunnerProperties runnerProperties = null;
            String propertiesFilename = null;
            for (int i=0; i<args.length; i++) {
                //System.out.println("Args["+i+"] is "+args[i]);
                if (args[i].startsWith(runnerPropertiesString)) {
                    propertiesFilename = args[i].substring(runnerPropertiesString.length());
                    // now try to load the properties
                    try {
                        //System.out.println("Main: propertiesFilename = "+propertiesFilename);
                        runnerProperties = JUnitTestRunnerProperties.load(propertiesFilename);
                    } catch (IOException ioe) {
                        System.err.println("Cannot load property file "+propertiesFilename);
                        System.exit(ERROR);
                    }
                }
            }
            
            if (runnerProperties == null) {
                // bad thing
                System.err.println("Could not find property file or the file is not a valid propertie file:"
                + propertiesFilename);
                System.exit(ERROR);
            }
            
            
            // everything looks ok, construct the runner object and run tests
            // we don't use any special classloader and we never run the main method from IDE
            try {
                JUnitTestRunner runner = new JUnitTestRunner(runnerProperties, null);
                runner.runTests();
            } catch (IllegalArgumentException iae) {
                System.err.println("Supplied properties are not correct: "+iae.getMessage());
            }
        } catch (Throwable t) {
            // something strange happened
            System.err.println("Throwable caught in JUnitTestRunner.main():"+t.getMessage());
            t.printStackTrace(System.err);
        }
        System.out.println("JUnit tests finished");
        System.exit(OK);
    }
    
    //
    // private methods
    //
    
    private void checkRunnerPropertiesValidity() throws IllegalArgumentException  {
        // TDB !!!!
    }


    // get all test suites to execute (in the case of nbtestsuites, also with filters)
    TestSuite[] getTestSuites() {
        // get all testnames from properties
        String[] testNames = runnerProperties.getTestNames();
        ArrayList testSuites = new ArrayList(testNames.length);
        for (int i=0; i < testNames.length; i++) {
            String testName = testNames[i];
            try {
                // get the testsuite and store it in hashmap
                TestSuite testSuite = getTestSuiteForName(testName);
                //out.println("XXX got test suite:"+testName+"/"+i);
                if (testSuite instanceof NbTestSuite) {
                    //out.println("is NbTestSuite");
                    Filter.IncludeExclude[] includes = runnerProperties.getTestFilterIncludes(i);
                    Filter.IncludeExclude[] excludes = runnerProperties.getTestFilterExcludes(i);
                    Filter filter = getFilter(includes, excludes);
                    ((NbTestSuite)testSuite).setFilter(filter);
                }
                // now continue with filter
                testSuites.add(testSuite);                
            } catch (ClassNotFoundException cnfe) {
                // Houston we have the problem - test class is not found !!!!
                out.println("! Cannot find test class "+testName
                + " ignoring this test");
            }            
        }
        // reurn the array
        return (TestSuite[])(testSuites.toArray(new TestSuite[0]));
    }

    
    // get testsuite for the supplied classname
    TestSuite getTestSuiteForName(String className) throws ClassNotFoundException {
        // load test class via appropritate classloader
        Class testClass = getTestClassForName(className);
        // try to extract suite method
        Method suiteMethod = null;
        try {
            suiteMethod = testClass.getMethod("suite", new Class[0]);
        } catch(Exception e) {
            // test suite() method not found - swallow the exception
            // similarly as JUnit runners do
        }
        // now try to prepate the TestSuite        
        if (suiteMethod != null) {
            // call suite method and see what happens
            try {
                Test aTest = (Test)(suiteMethod.invoke(null, new Class[0]));
                if (aTest instanceof TestSuite) {
                    // ok, we have the suite - everything is ok
                    TestSuite aTestSuite = (TestSuite)aTest;
                    if (aTestSuite.getName() == null) {
                        /*
                        String newSuiteName = testClass.getName();
                        */
                        String newSuiteName = className;
                        out.println("Suite name is null, installing "+newSuiteName);
                        aTestSuite.setName(newSuiteName);
                    }                    
                    return aTestSuite;
                } else {
                    // we need to construct or own suite
                    TestSuite suite;
                    /*
                    String suiteName = aTest.getClass().getName();
                     */
                    String suiteName = className;
                    if (aTest instanceof NbTest) {
                        // need to create NbTestSuite for NbTests -> we can use filter
                        suite = new NbTestSuite(suiteName);
                    } else {
                        // just plain Test - create TestSuite
                        suite = new TestSuite(suiteName);
                    }
                    // add the test we got and return it
                    suite.addTest(aTest);
                    return suite;
                }
            } catch (Exception e) {
                // this is bad -> print out the exception
                // and create suite from the class
                // as the suite() method was not defined at all
                out.println("Caught Exception when tried to extract suite method from class "+className);
                e.printStackTrace(out);
                out.println("Creating suite directly from the class.");
            }
        }
        // suite method not found - let's create or own testsuite
        // for the test class
        if (NbTest.class.isAssignableFrom(testClass)) {
            // need to create NbTestSuite for NbTests -> we can use filter
            //out.println("XX: suite not found, but test implements NbTest : class name "+testClass.getName());
            //TestSuite aSuite = new NbTestSuite(testClass.getName());
            //aSuite.addTest(instintiateTestFromClass(testClass));
            TestSuite aSuite = new NbTestSuite(testClass);
            return aSuite;
        } else {
            //out.println("XX: suite not found, test does not implement NbTest : class name "+testClass.getName());
            if (Test.class.isAssignableFrom(testClass)) {
                //out.println("XX: suite not found, test at least implements Test");
                // just plain Test - create TestSuite
                //TestSuite aSuite = new TestSuite(testClass.getName());
                //aSuite.addTest(instintiateTestFromClass(testClass));
                TestSuite aSuite = new TestSuite(testClass);
                return aSuite;
            } else {
                // 
                //  there should be also added possibility to run tests which 
                //      implements just Test or NbTest interfaces
                //      - this needs some more investigation .... !!!!!
                //
                
                // this happend when a class which is not test is used as a test
                throw new ClassCastException("Specified class: "+testClass+" is not assignable from junit.framework.Test "
                    + "interface - cannot run such test");
            }
        }
    }
    
    // load test class via appropriate class loader
    private Class getTestClassForName(String className) throws ClassNotFoundException {
        if (testLoader == null) {
            return Class.forName(className);
        } else {
            return testLoader.loadClass(className);
        }
    }
    
    private String getClassnameFromFilename(String filename) {
        String shortFilename;
        if (filename.endsWith(".java")) {
            shortFilename = filename.substring(0, filename.length() - 5);
        } else if (filename.endsWith(".class")) {
            shortFilename = filename.substring(0, filename.length() - 6);              
        } else {
            shortFilename = filename;
        }
        // now convert separator chars to dots (fully qualified java name)
        return shortFilename.replace(File.separatorChar, '.');
    }
    
    private Test instintiateTestFromClass(Class testClass) throws ClassCastException {
        if (Test.class.isAssignableFrom(testClass)) {
            // try the standard junit constructor with name parameter
            try {
                Constructor constructorWithString = testClass.getConstructor(new Class[] {String.class});
                // call the constructor
                Test aTest = (Test)constructorWithString.newInstance(new Object[] {testClass.getName()});
                return aTest;
            } catch (Exception e) {
                // constructor not found -> search for a constructor without any parameter
                // or instantiation was not succesfull
                try {
                    Constructor simpleConstructor = testClass.getConstructor(new Class[0]);
                    // call the constructor
                    Test aTest = (Test)simpleConstructor.newInstance(new Object[0]);
                    return aTest;
                } catch (Exception e2) {
                    // even this time we were not succesfull -> class is not suitable
                    // throw exception
                    throw new ClassCastException("Class "+testClass+" does not contain constructor with <String> parameter, or constructor without parameter at all. Cannot use this class for test.");
                }
                
            }
            // ok, constructor not found, try the normal constructor, without any parameter
        } else {
            throw new ClassCastException("Class "+testClass+" does not implement interface junit.framework.Test");
        }
    }
    

    
    // get Filter from include and exclude ArrayLists ...
    private Filter getFilter(Filter.IncludeExclude[] includes, Filter.IncludeExclude[] excludes) {        
        Filter filter = new Filter();
        boolean includesSet = false;
        if (includes != null) {
            filter.setIncludes(includes);
            includesSet = true;
        } 
        if (excludes != null) {
            filter.setExcludes(excludes);            
            includesSet = true;
        }
        if (includesSet) {
            return filter;
        } else {
            return null;
        }
    }
    
    // get the results directoru
    private File getResultsDirectory() {
        String resultsDirname = runnerProperties.getResultsDirName();
        if (resultsDirname != null) {
            File resultsDir = new File(resultsDirname);
            if (resultsDir.isDirectory()) {
                // directory exists, just return it
                return resultsDir;
            } else {
                // need to create one
                if (resultsDir.mkdirs()) {
                    // directory created - return it
                    return resultsDir;
                } else {
                    // directory cannot be created
                    // throw new IOException("Cannot create results directory "+resultsDirname);
                    return null;
                }
            }
        } else {
            return null;
        }
    }
    
    // add test listeners to testrun object
    private void addTestListeners(TestResult testResult) {
        if (testResult == null) throw new NullPointerException("testResult cannot be null");
        for (int i=0 ; i < resultProcessors.length; i++) {
            testResult.addListener(resultProcessors[i]);
        }        
    }
    
    // fire start test suite to all result processors
    private void fireStartTestSuite(TestSuite testSuite) {
        for (int i=0 ; i < resultProcessors.length; i++) {
            resultProcessors[i].startTestSuite(testSuite);
        }        
    }

    // fire end test suite to all result processors
    private void fireEndTestSuite(TestSuite testSuite, TestResult suiteResult) {
         for (int i=0 ; i < resultProcessors.length; i++) {
            resultProcessors[i].endTestSuite(testSuite,suiteResult);
        }        
    }
    

    
        
}
