/*
 * ResultsUtils.java
 *
 * Created on November 22, 2001, 2:58 PM
 */

package org.netbeans.xtest.pe;

import java.io.*;

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
    
    
    public static final File getHTMLResultDir(File rootDir) throws IOException {
        File resultDir = new File(rootDir,PEConstants.HTMLRESULTS_DIR);
        if (resultDir.exists()) {
            if (resultDir.isDirectory()) {
                return resultDir;
            } else {
                throw new IOException("File "+PEConstants.HTMLRESULTS_DIR+" exist, but is not a directory");
            }
        } else {
            if (resultDir.mkdirs()) {
                return resultDir;
            } else {
                throw new IOException("Cannot create :"+PEConstants.HTMLRESULTS_DIR+" directory");
            }
        }        
    }
    
    public static final File getXMLResultDir(File rootDir) throws IOException {
        File resultDir = new File(rootDir,PEConstants.XMLRESULTS_DIR);
        if (resultDir.exists()) {
            if (resultDir.isDirectory()) {
                return resultDir;
            } else {
                throw new IOException("File "+PEConstants.XMLRESULTS_DIR+" exist, but is not a directory");
            }
        } else {
            if (resultDir.mkdirs()) {
                return resultDir;
            } else {
                throw new IOException("Cannot create :"+PEConstants.HTMLRESULTS_DIR+" directory");
            }
        }        
    }    
}
