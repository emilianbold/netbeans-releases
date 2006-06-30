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
 * GetTestRunDirTask.java
 *
 * Created on November 21, 2001, 2:53 PM
 */

package org.netbeans.xtest.pe;

import org.apache.tools.ant.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.io.*;

/**
 *
 * @author  mb115822
 * @version
 *
 * in principle this works in the following way:
 * if the property is already set (getTestRunDir does not return null),
 * then nothing is changed and the task quits
 * if there is no value in the property, a new testrun directory
 * is created in the format testrun_YYMMDD_HHMMSS
 */
public class GetResultsDirsTask extends Task{
    
    private static final boolean DEBUG = false;
    private final static void debugInfo(String message) {
        if (DEBUG) System.out.println("GetTestRunDirTask."+message);        
    }
    
    /** Creates new GetTestRunDirTask */
    public GetResultsDirsTask() {
    }
    
    private String testRunDirProperty = null;
    private String testBagDirProperty = null;
    
    
    public void setTestRunDirProperty(String property) {
        this.testRunDirProperty = property;
    }
    
    public void setTestBagDirProperty(String property) {
        this.testBagDirProperty = property;
    }    
        
    
    private void setTestRunDir(String value) {
         getProject().setProperty(testRunDirProperty,value);
    }
    
    private String getTestRunDir() {
         return getProject().getProperty(testRunDirProperty);
    }
    
    private void setTestBagDir(String value) {
         getProject().setProperty(testBagDirProperty,value);
    }
    
    private String getTestBagDir() {
         return getProject().getProperty(testBagDirProperty);
    }
    
    private static String roundTo2Digits(int value) {
        String result = Integer.toString(value);
        int valueLength = result.length();
        if (valueLength<2) {
            result = "0" + result;
        } else {
            if (valueLength>2) {
                result = result.substring(valueLength - 2);
            }
        }
        return result;
    }
    
    
    private static String getYYMMDD_HHMMSS() {
        // better sleep for a second, so result is unique
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
        }
        // now generate output
        SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd-HHmmss");
        Date currentTime = new Date();
        String resultString = formatter.format(currentTime);
        return resultString;
    }    
    
    public static File createReportNode(File rootDir) throws BuildException {
        boolean result = rootDir.mkdirs();
        if (result == false) {
            throw new BuildException("Could not create directory "+rootDir);
        }
        try {
            ResultsUtils.getXMLResultDir(rootDir);
            ResultsUtils.getHTMLResultDir(rootDir);
        } catch (IOException ioe) {
            throw new BuildException(ioe);
        }
        return rootDir;
    }
    
    
    public File createAndSetTestBag() throws BuildException {
        // always try to create testrun first !!!
        if ( testBagDirProperty != null) {
            if (getTestBagDir() == null) {
                File testRunDir = createAndSetTestRun();
                debugInfo("createAndSetTestBag(): Got parent test run dir:"+testRunDir);
                // list all testbag_* dirs and try to create a new one (with the hishest number)
                String[] existingTestBags = testRunDir.list();
                int lastTestBagNumber = 0;
                if (existingTestBags == null) {
                    debugInfo("createAndSetTestBag(): testrundir:"+testRunDir+" is not a valid directory");
                    throw new BuildException("createAndSetTestBag(): testrundir:"+testRunDir+" is not a valid directory");
                }
                for (int i=0; i<existingTestBags.length; i++) {
                    String testBagName = existingTestBags[i];
                    if (testBagName.startsWith(PEConstants.TESTBAG_DIR_PREFIX)) {
                        debugInfo("createAndSetTestBag(): processing testbag: "+testBagName);
                        // cut the name
                        String number = testBagName.substring(PEConstants.TESTBAG_DIR_PREFIX.length());
                        try {
                            int testBagNumber = Integer.parseInt(number);
                            if (testBagNumber > lastTestBagNumber) {
                                lastTestBagNumber = testBagNumber;
                                debugInfo("createAndSetTestBag(): got higher value: "+testBagNumber);
                            }
                        } catch (NumberFormatException nfe) {
                            debugInfo("createAndSetTestBag(): testbag does not ends with number");
                        }
                    }
                }
                // try to create a new tetsbag
                lastTestBagNumber++;
                String newTestBagName  = PEConstants.TESTBAG_DIR_PREFIX+Integer.toString(lastTestBagNumber);
                debugInfo("createAndSetTestBag(): trying to create new testbag:"+newTestBagName);
                File newTestBagDir = new File(testRunDir,newTestBagName);
                createReportNode(newTestBagDir);                
                setTestBagDir(newTestBagDir.getAbsolutePath());
                return newTestBagDir;
            } else {
                debugInfo("createAndSetTestBag(): property "+testBagDirProperty+" already exists and is set at: "+getTestBagDir());
                return new File(getTestBagDir());
            }
        } else {
           debugInfo("createAndSetTestBag(): testBagDirProperty not set, no testbag dir created !!!");
           return null;
        }
    }
       
    public File createAndSetTestRun() throws BuildException {
        if ( testRunDirProperty == null) {
            debugInfo("createAndSetTestRun(): testRunDirProperty not set, this have to be set to a valid property name");
            throw new BuildException("testRunDirProperty not set, this have to be set to a valid property name (but property itself does not have to exist)");
        }
        if ( getTestRunDir() == null ) {
            debugInfo("createAndSetTestRun(): property does not exist, need to create new dir and fill the property");
            String resultsDirname = getProject().getProperty(PEConstants.XTEST_RESULTS);
            debugInfo("createAndSetTestRun(): got xtest.results dirname, value= "+resultsDirname);
            if (resultsDirname == null) {
                throw new BuildException("Results directory property ("+PEConstants.XTEST_RESULTS+") not set");
            }
            File resultsDir = new File(resultsDirname);
            if (!resultsDir.isDirectory()) {
                throw new BuildException("Results directory property ("+PEConstants.XTEST_RESULTS+" = "+
                    resultsDirname+") does not point to directory");
            }
            // now everything looks ok, create new dir and set it to desired property
            String testRunDirName = PEConstants.TESTRUN_DIR_PREFIX+getYYMMDD_HHMMSS();
            File testRunDir = new File(resultsDir,testRunDirName);
            debugInfo("createAndSetTestRun(): try to create dir: "+testRunDir);
            createReportNode(testRunDir);         
            // now store it in the property
            debugInfo("createAndSetTestRun(): storing path to dir in "+testRunDirProperty+" property");
            setTestRunDir(testRunDir.getAbsolutePath());
            // exit !!!
        } else {
            // nothing
            debugInfo("createAndSetTestRun(): property "+testRunDirProperty+" already exists and is set at: "+getTestRunDir());            
        }
        return new File(getTestRunDir());
    }
    
    
    public void execute () throws BuildException {
        log("Getting fresh testrun and testbag directories");
        createAndSetTestRun();
        createAndSetTestBag();
    }
    
    public static void main(String[] args) {       
        System.out.println("Year = "+getYYMMDD_HHMMSS());
    }

}
