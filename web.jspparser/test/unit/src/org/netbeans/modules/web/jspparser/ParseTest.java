/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.jspparser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import junit.framework.*;
import org.netbeans.junit.*;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.jsps.parserapi.JspParserFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

/** JUnit test suite with Jemmy support
 *
 * @author pj97932
 * @version 1.0
 */
public class ParseTest extends NbTestCase {
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public ParseTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static junit.framework.Test suite() {
        TestSuite suite = new NbTestSuite(ParseTest.class);
        return suite;
    }
    
    public void testAnalysisMain() throws Exception {
        midnightAppTest("jspparser-data2/midnight-jsp2.0/main.jsp");
    }
    
    public void testAnalysisBean() throws Exception {
        midnightAppTest("jspparser-data2/midnight-jsp2.0/more_for_test/bean.jsp");
    }
    
    public void testAnalysisTagLinkList() throws Exception {
        midnightAppTest("jspparser-data2/midnight-jsp2.0/WEB-INF/tags/linklist.tag");
    }
    
    public void testAnalysisFaulty() throws Exception {
        midnightAppTest("jspparser-data2/midnight-jsp2.0/faulty.jsp");
    }
    
    public void testAnalysisOutsideWM() throws Exception {
        midnightAppTest("jspparser-data2/outsidewm.jsp");
    }
    
    public void testAnalysisFunction() throws Exception {
        jspExamplesAppTest("jspparser-data3/jsp-examples/jsp2/el/functions.jsp");
    }
    
    public void testAnalysisXMLTextRotate() throws Exception {
        jspExamplesAppTest("jspparser-data3/jsp-examples/jsp2/jspx/textRotate.jspx");
    }
    
    /** Runs the test for a file from the midnight test application (data2.zip).
     *  @param path resource path of the file within the web module, separated by /
     */
    public void midnightAppTest(String path) throws Exception {
        try{
            log(Manager.getWorkDirPath());
            FileObject wmRoot = TestUtil.getFileInWorkDir("jspparser-data2/midnight-jsp2.0", this);
            FileObject res = TestUtil.getFileInWorkDir(path, this);
            if (!FileUtil.isParentOf (wmRoot, res)) {
                wmRoot = null;
            }
            analyzeIt(wmRoot, res);
        }catch(RuntimeException e){
            e.printStackTrace();
            e.printStackTrace(getRef());
            fail("Initialization of test failed! ->" + e);
        }
        
        log("FileParse called");
    }
    
    /** Runs the test for a file from JSP examples test application (data3.zip).
     *  @param path resource path of the file within the web module, separated by /
     */
    public void jspExamplesAppTest(String path) throws Exception {
        try{
            FileObject wmRoot = TestUtil.getFileInWorkDir("jspparser-data3/jsp-examples", this);
            FileObject res = TestUtil.getFileInWorkDir(path, this);
            if (!FileUtil.isParentOf (wmRoot, res)) {
                wmRoot = null;
            }
            analyzeIt(wmRoot, res);
            
        }catch(RuntimeException e){
            e.printStackTrace();
            e.printStackTrace(getRef());
            fail("Initialization of test failed! ->" + e);
        }
    }
    
    private static int fileNr = 1;
    
    private void analyzeIt(FileObject root, FileObject jspFile) throws IOException {
        log("calling parseIt, root: " + root + "  file: " + jspFile);
        JspParserAPI api = JspParserFactory.getJspParser();
        JspParserAPI.ParseResult result = api.analyzePage(jspFile, TestUtil.getWebModule(root, jspFile), JspParserAPI.ERROR_IGNORE);
        
        File goldenF = null;
        File outFile = null;
        try {
            //log(convertNBFSURL(getClass().getResource("/org/netbeans/modules/web/jspparser/data/goldenfiles/ParseTest/testAnalysisMain.pass")));
            goldenF = getGoldenFile();
            log("golden file exists 1: " + goldenF.exists());
        }
        finally {
            String fName = (goldenF == null) ? ("temp" + fileNr++ + ".result") : getBrotherFile(goldenF, "result");
            outFile = new File(getWorkDir(), fName);
            writeOutResult(result, outFile);
        }
        log("golden file: " + goldenF);
        log("golden file exists 2: " + goldenF.exists());
        assertNotNull(outFile);
        assertFile(outFile, goldenF, getWorkDir());
    }
    
    
    
    private void writeOutResult(JspParserAPI.ParseResult result, File outFile) throws IOException {
        PrintWriter pw = new PrintWriter(new FileWriter(outFile));
        pw.write(result.toString());
        pw.close();
    }
    
    private String getBrotherFile(File f, String ext) {
        String goldenFile = f.getName();
        int i = goldenFile.lastIndexOf('.');
        if (i == -1) {
            i = goldenFile.length();
        }
        return goldenFile.substring(0, i) + "." + ext;
    }
    
    /** method called before each testcase
     */
    protected void setUp() throws IOException {
    }
    
    /** method called after each testcase<br>
     * resets Jemmy WaitComponentTimeout
     */
    protected void tearDown() {
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
