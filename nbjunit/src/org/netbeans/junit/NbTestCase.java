/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.junit;

import java.awt.EventQueue;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.AssertionFailedError;
import java.util.*;
import java.net.URL;

import org.netbeans.junit.diff.*;
//import junit.framework.TestResult;
/** NetBeans extension to JUnit's TestCase. This extension adds mostly
 * possibility to compare files (assertFile).
 */
public abstract class NbTestCase extends TestCase implements NbTest {
    
    /**
     * active filter
     */
    private Filter filter;
    
    
    /**
 * Constructs a test case with the given name.
 * @param name name of the testcase
 */
    public NbTestCase(String name) {
        super(name);
    }
    
    
    /**
     * Sets active filter.
     * @param filter Filter to be set as active for current test, null will reset filtering.
     */
    public void setFilter(Filter filter) {
        this.filter = filter;
    }
    
    /**
     * Returns expected fail message.
     * @return expected fail message if it's expected this test fail, null otherwise.
     */
    public String getExpectedFail() {
        if (filter == null) 
            return null;
        return filter.getExpectedFail(this.getName());
    }
    
    /**
 * Checks if a test isn't filtered out by the active filter.
 * @return true if the test can run
 */
    public boolean canRun() {
        if (null == filter) {
            //System.out.println("NBTestCase.canRun(): filter == null name=" + name ());
            return true; // no filter was aplied
        }
        boolean isIncluded = filter.isIncluded(this.getName());
        //System.out.println("NbTestCase.canRun(): filter.isIncluded(this.getName())="+isIncluded+" ; this="+this);
        return isIncluded;
    }
    
    /**
     * Provide ability for tests to request that they run only in the AWT event queue.
     * By default, false.
     * @return true to run all test methods in the EQ, false to run in whatever thread
     */
    protected boolean runInEQ() {
        return false;
    }
    
    /**
     * Runs the test case and collects the results in TestResult.
     * overrides JUnit run, because filter check
     * and handling of {@link #runInEQ}
     */
    public void run(final TestResult result) {
        if (canRun()) {
            if (runInEQ()) {
                try {
                    EventQueue.invokeAndWait(new Runnable() {
                        public void run() {
                            NbTestCase.super.run(result);
                        }
                    });
                } catch (InterruptedException e) {
                    result.addError(this, e);
                } catch (InvocationTargetException e) {
                    Throwable t = e.getCause();
                    if (t instanceof AssertionFailedError) {
                        result.addFailure(this, (AssertionFailedError)t);
                    } else if (t instanceof ThreadDeath) {
                        throw (ThreadDeath)t;
                    } else {
                        result.addError(this, t);
                    }
                }
            } else {
                // Regular test.
                super.run(result);
            }
        }
    }

    
    
    // additional asserts !!!!

    
            /**
         * Asserts that two files are the same (their content is identical), when files
         * differ {@link org.netbeans.junit.AssertionFileFailedError AssertionFileFailedError} exception is thrown. 
         * Depending on the Diff implementation additional output can be generated to the file/dir specified by the
         * <b>diff</b> param.
         * @param message the detail message for this assertion
         * @param test first file to be compared, by the convention this should be the test-generated file
         * @param pass second file to be comapred, it should be so called 'golden' file, which defines 
         * the correct content for the test-generated file. 
         * @param diff file, where differences will be stored, when null differences will not be stored. In case 
         * it points to directory the result file name is constructed from the <b>pass</b> argument and placed to that 
         * directory. Constructed file name consists from the name of pass file (without extension and path) appended 
         * by the '.diff'.
         * @param externalDiff instance of class implementing the {@link org.netbeans.junit.diff.Diff} interface, it has to be 
         * already initialized, when passed in this assertFile function.
         */
    static public void assertFile(String message, String test, String pass, String diff, Diff externalDiff) {
        Diff diffImpl = null == externalDiff ? Manager.getSystemDiff() : externalDiff;
        File    diffFile = getDiffName(pass, null == diff ? null : new File(diff));
        
        if (null == diffImpl) {
            fail("diff is not available");
        }
        else {
            try {
                if (null == diffFile) {
                    if (diffImpl.diff(test, pass, null))
                        throw new AssertionFileFailedError(message, "");
                }
                else {
                    if (diffImpl.diff(test, pass, diffFile.getAbsolutePath()))
                        throw new AssertionFileFailedError(message, diffFile.getAbsolutePath());
                }
            }
            catch (IOException e) {
                fail("exception in assertFile : " + e.getMessage());
            }
        }
    }
        /**
         * Asserts that two files are the same, it uses specific {@link org.netbeans.junit.diff.Diff Diff} implementation to 
         * compare two files and stores possible differencies in the output file.
         * @param test first file to be compared, by the convention this should be the test-generated file
         * @param pass second file to be comapred, it should be so called 'golden' file, which defines the 
         * correct content for the test-generated file. 
         * @param diff file, where differences will be stored, when null differences will not be stored. In case 
         * it points to directory the result file name is constructed from the <b>pass</b> argument and placed to that 
         * directory. Constructed file name consists from the name of pass file (without extension and path) appended 
         * by the '.diff'.
         * @param externalDiff instance of class implementing the {@link org.netbeans.junit.diff.Diff} interface, it has to be 
         * already initialized, when passed in this assertFile function.
         */
    static public void assertFile(String test, String pass, String diff, Diff externalDiff) {
        assertFile(null, test, pass, diff, externalDiff);
    }
        /**
         * Asserts that two files are the same, it compares two files and stores possible differencies 
         * in the output file, the message is displayed when assertion fails.
         * @param message the detail message for this assertion
         * @param test first file to be compared, by the convention this should be the test-generated file
         * @param pass second file to be comapred, it should be so called 'golden' file, which defines the 
         * correct content for the test-generated file. 
         * @param diff file, where differences will be stored, when null differences will not be stored. In case 
         * it points to directory the result file name is constructed from the <b>pass</b> argument and placed to that 
         * directory. Constructed file name consists from the name of pass file (without extension and path) appended 
         * by the '.diff'.
         */
    static public void assertFile(String message, String test, String pass, String diff) {
        assertFile(message, test, pass, diff, null);
    }
        /**
         * Asserts that two files are the same, it compares two files and stores possible differencies 
         * in the output file.
         * @param test first file to be compared, by the convention this should be the test-generated file
         * @param pass second file to be comapred, it should be so called 'golden' file, which defines the 
         * correct content for the test-generated file. 
         * @param diff file, where differences will be stored, when null differences will not be stored. In case 
         * it points to directory the result file name is constructed from the <b>pass</b> argument and placed to that 
         * directory. Constructed file name consists from the name of pass file (without extension and path) appended 
         * by the '.diff'.
         */
    static public void assertFile(String test, String pass, String diff) {
        assertFile(null, test, pass, diff, null);
    }
        /**
         * Asserts that two files are the same, it just compares two files and doesn't produce any additional output.
         * @param test first file to be compared, by the convention this should be the test-generated file
         * @param pass second file to be comapred, it should be so called 'golden' file, which defines the 
         * correct content for the test-generated file. 
         */
    static public void assertFile(String test, String pass) {
        assertFile(null, test, pass, null, null);
    }

        /**
         * Asserts that two files are the same (their content is identical), when files
         * differ {@link org.netbeans.junit.AssertionFileFailedError AssertionFileFailedError} exception is thrown. 
         * Depending on the Diff implementation additional output can be generated to the file/dir specified by the
         * <b>diff</b> param.
         * @param message the detail message for this assertion
         * @param test first file to be compared, by the convention this should be the test-generated file
         * @param pass second file to be comapred, it should be so called 'golden' file, which defines 
         * the correct content for the test-generated file. 
         * @param diff file, where differences will be stored, when null differences will not be stored. In case 
         * it points to directory the result file name is constructed from the <b>pass</b> argument and placed to that 
         * directory. Constructed file name consists from the name of pass file (without extension and path) appended 
         * by the '.diff'.
         * @param externalDiff instance of class implementing the {@link org.netbeans.junit.diff.Diff} interface, it has to be 
         * already initialized, when passed in this assertFile function.
         */
    static public void assertFile(String message, File test, File pass, File diff, Diff externalDiff) {
        Diff diffImpl = null == externalDiff ? Manager.getSystemDiff() : externalDiff;
        File    diffFile = getDiffName(pass.getAbsolutePath(), diff);
        
        /*
        System.out.println("NbTestCase.assertFile(): diffFile="+diffFile);
        System.out.println("NbTestCase.assertFile(): diffImpl="+diffImpl);
        System.out.println("NbTestCase.assertFile(): externalDiff="+externalDiff);
        */
        
        if (null == diffImpl) {
            fail("diff is not available");
        }
        else {
            try {
                if (diffImpl.diff(test, pass, diffFile)) {
                    throw new AssertionFileFailedError(message, null == diffFile ? "" : diffFile.getAbsolutePath());
                }
            }
            catch (IOException e) {
                fail("exception in assertFile : " + e.getMessage());
            }
        }
    }
        /**
         * Asserts that two files are the same, it uses specific {@link org.netbeans.junit.diff.Diff Diff} implementation to 
         * compare two files and stores possible differencies in the output file.
         * @param test first file to be compared, by the convention this should be the test-generated file
         * @param pass second file to be comapred, it should be so called 'golden' file, which defines the 
         * correct content for the test-generated file. 
         * @param diff file, where differences will be stored, when null differences will not be stored. In case 
         * it points to directory the result file name is constructed from the <b>pass</b> argument and placed to that 
         * directory. Constructed file name consists from the name of pass file (without extension and path) appended 
         * by the '.diff'.
         * @param externalDiff instance of class implementing the {@link org.netbeans.junit.diff.Diff} interface, it has to be 
         * already initialized, when passed in this assertFile function.
         */
    static public void assertFile(File test, File pass, File diff, Diff externalDiff) {
        assertFile(null, test, pass, diff, externalDiff);
    }
        /**
         * Asserts that two files are the same, it compares two files and stores possible differencies 
         * in the output file, the message is displayed when assertion fails.
         * @param message the detail message for this assertion
         * @param test first file to be compared, by the convention this should be the test-generated file
         * @param pass second file to be comapred, it should be so called 'golden' file, which defines the 
         * correct content for the test-generated file. 
         * @param diff file, where differences will be stored, when null differences will not be stored. In case 
         * it points to directory the result file name is constructed from the <b>pass</b> argument and placed to that 
         * directory. Constructed file name consists from the name of pass file (without extension and path) appended 
         * by the '.diff'.
         */
    static public void assertFile(String message, File test, File pass, File diff) {
        assertFile(message, test, pass, diff, null);
    }
        /**
         * Asserts that two files are the same, it compares two files and stores possible differencies 
         * in the output file.
         * @param test first file to be compared, by the convention this should be the test-generated file
         * @param pass second file to be comapred, it should be so called 'golden' file, which defines the 
         * correct content for the test-generated file. 
         * @param diff file, where differences will be stored, when null differences will not be stored. In case 
         * it points to directory the result file name is constructed from the <b>pass</b> argument and placed to that 
         * directory. Constructed file name consists from the name of pass file (without extension and path) appended 
         * by the '.diff'.
         */
    static public void assertFile(File test, File pass, File diff) {
        assertFile(null, test, pass, diff, null);
    }
        /**
         * Asserts that two files are the same, it just compares two files and doesn't produce any additional output.
         * @param test first file to be compared, by the convention this should be the test-generated file
         * @param pass second file to be comapred, it should be so called 'golden' file, which defines the 
         * correct content for the test-generated file. 
         */
    static public void assertFile(File test, File pass) {
        assertFile(null, test, pass, null, null);
    }

/**
 */
    static private File getDiffName(String pass, File diff) {
        if (null == diff)
            return null;
        
        if (!diff.exists() || diff.isFile())
            return diff;
        
        StringBuffer d = new StringBuffer();
        int i1, i2;
        
        d.append(diff.getAbsolutePath());
        i1 = pass.lastIndexOf('\\');
        i2 = pass.lastIndexOf('/');
        i1 = i1 > i2 ? i1 : i2;
        i1 = -1 == i1 ? 0 : i1 + 1;
        
        i2 = pass.lastIndexOf('.');
        i2 = -1 == i2 ? pass.length() : i2;
        
        if (0 < d.length())
            d.append("/");
        
        d.append(pass.substring(i1, i2));
        d.append(".diff");
        return new File(d.toString());
    }
    
    // methods for work with tests' workdirs

    
    /** Returns path to test method working directory as a String. Path is constructed
     * as ${nbjunit.workdir}/${package}/${classname}/${testmethodname}. (nbjunit.workdir
     * property have to be set in junit.properties file)
     * Please note, this method does not guarantee that working directory really exist.
     * @throws IOException if nbjunit.workdir property has not been set
     * @return path
     */    
    public String getWorkDirPath() throws IOException {
        String path = Manager.getWorkDirPath();
        if (path == null) {
            throw new IOException(Manager.NBJUNIT_WORKDIR+" is not set, cannot create workdir");
        }
        
        // package+class
        path += File.separator + this.getClass().getName().replace('.',File.separatorChar);
        // method name
        path += File.separator + getName();
        return path;
    }

    /** Returns unique working directory for a test (each test method have uniq1ue dir).
     * If not available, method tries to create it. This method uses getWorkDirPath()
     * method to determine the unique path.
     * @throws IOException if the directory cannot be created
     * @return file to the working directory directory
     */    
    public File getWorkDir() throws IOException {
        // construct path from workdir classpath + classname + methodname
               
        /*
        String path = this.getClass().getResource("").getFile().toString();
        String srcElement="src";
        String workdirElement="workdir";
        int srcStart = path.lastIndexOf(srcElement);
        // base path
        path = path.substring(0,srcStart)+workdirElement;
        // package+class
        path += "/"+this.getClass().getName().replace('.','/');
        // method name
        path += "/"+getName();
        */
        
        // new way how to get path - from defined property + classname +methodname
        
        
        
        // now we have path, so if not available, create workdir
        String path = getWorkDirPath();
        File workdir = new File(path);
        if (workdir.exists()) {
            if (!workdir.isDirectory()) {
                // work dir exists, but is not directory - this should not happen
                // trow exception
                throw new IOException("workdir exists, but is not a directory, workdir = "+path);
            } else {
                // everything looks correctly, return the path
                return workdir;
            }
        } else {
            // we need to create it
            boolean result = workdir.mkdirs();
            if (result == false) {
                // mkdirs() failed - throw an exception
                throw new IOException("workdir creation failed, workdir = "+path);
            } else {
                // everything looks ok - return path
                return workdir;
            }
        }
    }
    
    // private method for deleting a file/directory (and all its subdirectories/files)
    private void deleteFile(File file) throws IOException {
        if (file.isDirectory()) {
            // file is a directory - delete sub files first
            File files[] = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteFile(files[i]);
            }
            
        }        
        // file is a File :-)
        boolean result = file.delete();
        if (result == false ) {
            // a problem has appeared
            throw new IOException("Cannot delete file, file = "+file.getPath());
        }                
    }
    
    // private method for deleting every subfiles/subdirectories of a file object
    private void deleteSubFiles(File file) throws IOException {
        if (file.isDirectory()) {
            File files[] = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteFile(files[i]);
            }
        } else {
            // probably do nothing - file is not a directory
        }        
    }
    
    /** Deletes all files including subdirectories in test's working directory.
     * @throws IOException if any problem has occured during deleting files/directories
     */    
    public void clearWorkDir() throws IOException {;
        File workdir = getWorkDir();
        deleteSubFiles(workdir);
        //boolean result = workdir.delete();
        /*
        if (result == false) {
            throw new IOException("Workdir cannot be erased, workdir = "+path);
        } 
        */
    }
    
    
    // Logging stuff

        
    
    
    /** return PrintStream poiting at the log file. If the file cannot be created
     * (see getLogFile), PrintStream constructed from System.out is used.
     *
     * @return PrintStream to the log
     */    
/*
    
    public PrintStream getLog() {
       OutputStream logStream;
       try {
           logStream = new FileOutputStream(getLogFile());
       } catch (IOException ioe) {
           // if we can log to file, at least log to system.out
           logStream = System.out;
       }
       return new PrintStream(logStream,true);
    }
  */  
    
    /* 
     * get log file
     */
    /** Tries to create log file for a particular test method. This file is
     * located under working directory of the method (see getWorkDir method) and
     * has name methodname.log.
     * @throws IOException if the logfile cannot be created
     * @return log file
     */    
    /*
    public File getLogFile() throws IOException {        
        String logFilename = getName() + ".log";
        File logFile = new File(getWorkDir(),logFilename);
        return logFile;
    }
*/
    // we need to close all logs     
    

    private OutputStream gettOutputStreamInWorkDir(String filename) throws IOException {
        File aStreamFile = new File(getWorkDir(),filename);        
        return null;
    }
    
    private String lastTestMethod=null;
    
    private boolean hasTestMethodChanged() {
        if (!this.getName().equals(lastTestMethod)) {
            lastTestMethod=this.getName();
            return true;
        } else {
            return false;
        }
    }
    
    // hashtable holding all already used logs and correspondig printstreams
    private Hashtable logStreamTable = null;
    
    private PrintStream getFileLog(String logName) throws IOException {
        OutputStream outputStream;
        FileOutputStream fileOutputStream;
        
        if ((logStreamTable == null)|(hasTestMethodChanged())) {
            // we haven't used logging capability - create hashtables            
            logStreamTable = new Hashtable();
            //System.out.println("Created new hashtable");
        } else {
            if (logStreamTable.containsKey(logName)) {
                //System.out.println("Getting stream from cache:"+logName);
                return (PrintStream)logStreamTable.get(logName);
            } 
        }
        // we didn't used this log, so let's create it
        FileOutputStream fileLog = new FileOutputStream(new File(getWorkDir(),logName));
        PrintStream printStreamLog = new PrintStream(fileLog,true);
        logStreamTable.put(logName,printStreamLog);
        //System.out.println("Created new stream:"+logName);
        return printStreamLog;        
    }
    
    // private PrintStream wrapper for System.out
    PrintStream systemOutPSWrapper = new PrintStream(System.out);
    
    /** Returns named log stream. If log cannot be created as a file in the
     * testmethod working directory, PrintStream created from System.out is used. Please
     * note, that tests shoudn't call log.close() method, unless they really don't want
     * to use this log anymore.
     * @param logName name of the log - file in the working directory
     * @return Log PrintStream
     */    
    public PrintStream getLog(String logName) {
        try {
            return getFileLog(logName);
        } catch (IOException ioe) {
            /// hey, file is not available - log will be made to System.out
            // we should probably write a little note about it 
            //System.err.println("Test method "+this.getName()+" - cannot open file log to file:"+logName
            //                                +" - defaulting to System.out");
            return systemOutPSWrapper;            
        }
    }
    
    /** Return default log named as ${testmethod}.log. If the log cannot be created
     * as a file in testmethod working directory, PrinterStream to System.out is returned
     * @return log
     */    
    public PrintStream getLog() {
        return getLog(this.getName()+".log");
    }
    
    /** Simple and easy to use method for printing a message to a default log
     * @param message meesage to log
     */    
    public void log(String message) {
        getLog().println(message);
    }
    
    
    /** Easy to use method for logging a message to a named log
     * @param log which log to use
     * @param message message to log
     */    
    public void log(String log, String message) {
        getLog(log).println(message);
    }
    
    // reference file stuff ...
    
    
    /** Get PrintStream to log inteded for reference files comparision. Reference
     * log is stored as a file named ${testmethod}.ref in test method working directory.
     * If the file cannot be created, the testcase will automatically fail.
     * @return PrintStream to referencing log
     */    
    public PrintStream getRef() {
        String refFilename = this.getName()+".ref";
        try {
            return getFileLog(refFilename);
        } catch (IOException ioe) {
            // canot get ref file - return system.out 
            //System.err.println("Test method "+this.getName()+" - cannot open ref file:"+refFilename
            //                                +" - defaulting to System.out and failing test");
            fail("Could not open reference file: "+refFilename);
            return  systemOutPSWrapper;           
        }
    }
    
    /** Easy to use logging method for printing a message to a reference log.
     * @param message message to log
     */    
    public void ref(String message) {
        getRef().println(message);
    }
    
    /** Get the testmethod specific golden file from data/goldenfiles/${classname}
     * resource directory.
     * @param filename filename to get from golden files resource directory
     * @return file
     */    
    public File getGoldenFile(String filename) {
        // golden files are in data/goldenfiles/${classname}/* ...
        String fullClassName = this.getClass().getName();
        String className = fullClassName;
        int lastDot = fullClassName.lastIndexOf('.');
        if (lastDot != -1) {
            className = fullClassName.substring(lastDot+1);
        }  
        String goldenFileName = className+"/"+filename;
        URL url = this.getClass().getResource("data/goldenfiles/"+goldenFileName);
        assertNotNull("Golden file data/goldenfiles/"+goldenFileName+" cannot be found",url);
        String resString = convertNBFSURL(url);        
        File goldenFile = new File(resString);
        //System.out.println("Res: "+resString);
        return goldenFile;
    }
    
    /** Get the default testmethod specific golden file from
     * data/goldenfiles/${classname}/${testmethodname}.pass
     * @return filename to get from golden files resource directory
     */    
    public File getGoldenFile() {
        return getGoldenFile(this.getName()+".pass");
    }
    
    
    /** Compares golden file and reference log. If both files are the
     * same, test passes. If files differ, test fails and diff file is
     * created (diff is created only when using native diff, for details
     * see JUnit module documentation)
     * @param testFilename reference log file name
     * @param goldenFilename golden file name
     * @param diffFilename diff file name (optional, if null, then no diff is created)
     */    
    public void compareReferenceFiles(String testFilename, String goldenFilename, String diffFilename) {
        try {
            if (!getRef().equals(systemOutPSWrapper)) {
                // better flush the reference file
                getRef().flush();
                getRef().close();
            }
            File goldenFile = getGoldenFile(goldenFilename);
            File testFile = new File(getWorkDir(),testFilename);
            File diffFile = new File(getWorkDir(),diffFilename);
            assertFile("Files differ", testFile, goldenFile, diffFile);
        } catch (IOException ioe) {
            fail("Could not obtain working direcory");
        }
    }
    
    /** Compares default golden file and default reference log. If both files are the
     * same, test passes. If files differ, test fails and default diff (${methodname}.diff)
     * file is created (diff is created only when using native diff, for details
     * see JUnit module documentation)
     */    
    public void compareReferenceFiles() {
        compareReferenceFiles(this.getName()+".ref",this.getName()+".pass",this.getName()+".diff");
    }
    
    // utility stuff for getting resources from NetBeans' filesystems
    
     /** Converts NetBeans filesystem URL to absolute path.
      * @param url URL to convert
      * @return absolute path
      */
    
    public static String convertNBFSURL(URL url) {
        String externalForm = url.toExternalForm();
        if (externalForm.startsWith("nbfs://")) {
            // new nbfsurl format (post 06/2003)
            return convertNewNBFSURL(url);
        } else {
            // old nbfsurl (and non nbfs urls)
            return convertOldNBFSURL(url);            
        }
    }
    
    // radix for new nbfsurl
    private final static int radix = 16;
    // new nbfsurl decoder - assumes the external form 
    // begins with nbfs://
    private static String convertNewNBFSURL(URL url) {
        String externalForm = url.toExternalForm();
        String path;
        if (externalForm.startsWith("nbfs://nbhost/")) {
            // even newer nbfsurl (hope it does not change soon)
            // return path and omit first slash sign
            path = url.getPath().substring(1);
        } else {
            path = externalForm.substring("nbfs://".length());
        }
        // convert separators (%2f = /,  etc.)
        StringBuffer sb = new StringBuffer();
        int i = 0;
        int len = path.length();
        while (i < len) {
            char ch = path.charAt(i++);
            if (ch == '%' && (i+1) < len) {
                char h1 = path.charAt(i++);
                char h2 = path.charAt(i++);
                // convert d1+d2 hex number to char
                ch = (char)Integer.parseInt(new String(""+h1+h2), radix);
                
            }
            sb.append(ch);
        }
        return sb.toString();
        
    }
    
    // old nbfsurl decoder
    private static String convertOldNBFSURL(URL url) {
        String path = url.getFile();
        if(url.getProtocol().equals("nbfs")) {
            // delete prefix of special Filesystem (e.g. org.netbeans.modules.javacvs.JavaCvsFileSystem)
            String prefixFS = "FileSystem ";
            if(path.indexOf(prefixFS)>-1) {
                path = path.substring(path.indexOf(prefixFS)+prefixFS.length());
            }
            // convert separators ("QB="/" etc.)
            StringBuffer sb = new StringBuffer();
            int i = 0;
            int len = path.length();
            while (i < len) {
                char ch = path.charAt(i++);
                if (ch == 'Q' && i < len) {
                    ch = path.charAt(i++);
                    switch (ch) {
                        case 'B':
                            sb.append('/');
                            break;
                        case 'C':
                            sb.append(':');
                            break;
                        case 'D':
                            sb.append('\\');
                            break;
                        case 'E':
                            sb.append('#');
                            break;
                        default:
                            // not a control sequence
                            sb.append('Q');
                            sb.append(ch);
                            break;
                    }
                } else {
                    // not Q
                    sb.append(ch);
                }
            }
            path = sb.toString();
        }
        return path;
    }

    
    /** Assert GC. Tries to GC ref's referent.
     * @param text the text to show when test fails.
     * @param ref the referent to object that
     * should be GCed
     */
    public static void assertGC(String text, java.lang.ref.Reference ref) {
        ArrayList alloc = new ArrayList ();
        int size = 100000;
        for (int i = 0; i < 50; i++) {
            if (ref.get() == null) {
                return;
            }
            System.gc();
            System.runFinalization();
            try {
                alloc.add (new byte[size]);
                size = (int)(((double)size) * 1.3);
            } catch (OutOfMemoryError error) {
                size = size / 2;
            }
            try {
                if (i % 3 == 0) Thread.sleep(321);
            } catch (InterruptedException t) {
                // ignore
            }
        }
        fail(text + " " + ref.get());
    }

    /** Assert size of some structure. Traverses the whole reference
     * graph of objects accessible from given root object and check its size
     * against the limit.
     * @param message the text to show when test fails.
     * @param limit maximal allowed heap size of the structure
     * @param root the root object from which to traverse
     */
    public static void assertSize(String message, int limit, Object root ) {
	assertSize(message, Arrays.asList( new Object[] {root} ), limit);
    }

    /** Assert size of some structure. Traverses the whole reference
     * graph of objects accessible from given roots and check its size
     * against the limit.
     * @param message the text to show when test fails.
     * @param roots the collection of root objects from which to traverse
     * @param limit maximal allowed heap size of the structure
     */
    public static void assertSize(String message, Collection roots, int limit) {
	assertSize(message, roots, limit, null);
    }

    /** Assert size of some structure. Traverses the whole reference
     * graph of objects accessible from given roots and check its size
     * against the limit.
     * @param message the text to show when test fails.
     * @param roots the collection of root objects from which to traverse
     * @param limit maximal allowed heap size of the structure
     * @param skip Array of objects used as a boundary during heap scanning,
     *        neither these objects nor references from these objects
     *        are counted.
     */
    public static void assertSize(String message, Collection roots, int limit, Object[] skip) {
        try {
            Collection c = MemoryCounter.countInstances(roots, skip);
            int sum = 0;
            for(Iterator it = c.iterator(); it.hasNext(); ) {
                MemoryCounter.ClassInfo ci = (MemoryCounter.ClassInfo)it.next();
                sum += ci.getTotalSize();
            }
            if (sum > limit) {
                StringBuffer sb = new StringBuffer (4096);
                sb.append (message); 
                sb.append (": ");
                sb.append (sum);
                sb.append (">");
                sb.append (limit);
                sb.append ('\n');
                for(Iterator it = c.iterator(); it.hasNext(); ) {
                    sb.append ("  ");
                    MemoryCounter.ClassInfo ci = (MemoryCounter.ClassInfo)it.next();
                    sb.append (ci.toString ());
                    sb.append ('\n');
                }
		fail(sb.toString());
            }
        } catch (Exception e) {
            fail("Could not traverse reference graph");
        }
    }


}