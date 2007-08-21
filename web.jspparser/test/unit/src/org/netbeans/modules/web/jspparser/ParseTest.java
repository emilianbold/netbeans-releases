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

package org.netbeans.modules.web.jspparser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Sources;
import org.netbeans.junit.*;
import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.core.jsploader.JspParserAccess;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.jsps.parserapi.JspParserFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;

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
    
    public void testAnalysisMain() throws Exception {
        parserTestInProject("project2", Manager.getWorkDirPath() + "/project2/web/main.jsp");
    }
    
    public void testAnalysisBean() throws Exception {
        parserTestInProject("project2", Manager.getWorkDirPath() + "/project2/web/more_for_test/bean.jsp");
    }
    
    public void testAnalysisTagLinkList() throws Exception {
        parserTestInProject("project2", Manager.getWorkDirPath() + "/project2/web/WEB-INF/tags/linklist.tag");
    }
    
    public void testAnalysisFaulty() throws Exception {
        parserTestInProject("project2", Manager.getWorkDirPath() + "/project2/web/faulty.jsp");
    }
    
    public void testAnalysisOutsideWM() throws Exception {
        parserTestInProject("project2", Manager.getWorkDirPath() + "/project2/outside/outsidewm.jsp");
    }
        
    public void testAnalysisFunction() throws Exception {
        parserTestInProject("project3", Manager.getWorkDirPath() + "/project3/web/jsp2/el/functions.jsp");
    }
        
    public void testAnalysisXMLTextRotate_1_6() throws Exception {
        String javaVersion = System.getProperty("java.version"); //NOI18N
        
        if (javaVersion.startsWith("1.6")){ //NOI18N
            parserTestInProject("project3", Manager.getWorkDirPath() + "/project3/web/jsp2/jspx/textRotate.jspx"); //NOI18N
        }
    }
    
    public void testAnalysisXMLTextRotate_1_5() throws Exception {
        String javaVersion = System.getProperty("java.version"); //NOI18N
        
        if (javaVersion.startsWith("1.5")){ //NOI18N
            parserTestInProject("project3", Manager.getWorkDirPath() + "/project3/web/jsp2/jspx/textRotate.jspx"); //NOI18N
        }
        
    }
    
    public void testAnalysisXMLTextRotate_1_4() throws Exception {
        String javaVersion = System.getProperty("java.version"); //NOI18N
        
        if (javaVersion.startsWith("1.4")){ //NOI18N
            parserTestInProject("project3", Manager.getWorkDirPath() + "/project3/web/jsp2/jspx/textRotate.jspx"); //NOI18N
        }
    }
    
    public void testAnalysisTagLibFromTagFiles() throws Exception {
        String javaVersion = System.getProperty("java.version"); //NOI18N
        if (!javaVersion.startsWith("1.6")){ //NOI18N
            parserTestInProject("project2", Manager.getWorkDirPath() + "/project2/web/testTagLibs.jsp");
        }
    }

    public void testAnalysisTagLibFromTagFiles_1_6() throws Exception {
        String javaVersion = System.getProperty("java.version"); //NOI18N
        if (javaVersion.startsWith("1.6")){ //NOI18N
            parserTestInProject("project2", Manager.getWorkDirPath() + "/project2/web/testTagLibs.jsp");
        }
    }
    
    public void testJSPInclude() throws Exception {
        parserTestInProject("project2", Manager.getWorkDirPath() + "/project2/web/jspInclude.jsp");
    }
    
    public void testInclude() throws Exception {
        parserTestInProject("project2", Manager.getWorkDirPath() + "/project2/web/include.jsp");
    
    }
    
    public void testIncludePreludeCoda() throws Exception {
        JspParserAPI.ParseResult result = parserTestInProject("project2", Manager.getWorkDirPath() + "/project2/web/includePreludeCoda.jsp");
        log("Prelude: " + result.getPageInfo().getIncludePrelude());
        log("Coda: " + result.getPageInfo().getIncludeCoda());        
    }
    
    // test for issue #70426
    public void testGetTagLibMap70426() throws Exception{
        Object o = ProjectSupport.openProject(Manager.getWorkDirPath()+"/emptyWebProject");
        Project project = (Project)o;
        File f = new File(Manager.getWorkDirPath() + "/emptyWebProject/web/index.jsp");
        FileObject jspFo = FileUtil.fromFile(f)[0];
        JspParserAPI.WebModule wm = TestUtil.getWebModule(jspFo);
        Map library = JspParserFactory.getJspParser().getTaglibMap(wm);
        System.out.println("map->" + library);
        Library jstlLibrary = LibraryManager.getDefault().getLibrary("jstl11");
        ProjectClassPathExtender cpExtender = (ProjectClassPathExtender) project.getLookup().lookup(ProjectClassPathExtender.class);
        cpExtender.addLibrary(jstlLibrary);
        library = JspParserFactory.getJspParser().getTaglibMap(wm);
        System.out.println("map->" + library);
        assertTrue("The JSTL/core library was not returned.", (library.get("http://java.sun.com/jsp/jstl/core")) != null);
    }
    
    public JspParserAPI.ParseResult parserTestInProject(String projectFolderName, String pagePath) throws Exception{
        log("Parsing test of page  " + pagePath + " in project " + projectFolderName + " started.");
        String projectPath = Manager.getWorkDirPath() + "/" + projectFolderName;
        Object o = ProjectSupport.openProject(projectPath);
        if ( o != null)
            log("Project " + projectPath + " opened.");
        else
            log("Project " + projectPath + " was not opened.");
        Project project = (Project)o;
        File f = new File(pagePath);
        FileObject jspFo = FileUtil.fromFile(f)[0];
        if (jspFo == null) 
            log (pagePath + " not found.");
        log("Parsing page " + pagePath);

        JspParserAPI.ParseResult result = JspParserFactory.getJspParser()
                    .analyzePage(jspFo, TestUtil.getWebModule(jspFo), JspParserAPI.ERROR_IGNORE);        
        if (ProjectSupport.closeProject(ProjectUtils.getInformation(project).getName()))
            log ("Project closed.");
        assertFalse("The result from the parser was not obtained.", result == null);
        File goldenF = null;
        File outFile = null;
        try {
            goldenF = getGoldenFile();
        }
        finally {
            String fName = (goldenF == null) ? ("temp" + fileNr++ + ".result") : getBrotherFile(goldenF, "result");
            outFile = new File(getWorkDir(), fName);
            writeOutResult(result, outFile);
        }
        
        assertNotNull(outFile);
        assertFile(outFile, goldenF, getWorkDir());
        return result;
    }
    
    private static int fileNr = 1;
    
    /*private void analyzeIt(FileObject root, FileObject jspFile) throws Exception {
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
    }*/
    
    
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
    
}
