/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.web.jspparser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI.WebModule;
import org.netbeans.spi.java.project.classpath.ProjectClassPathExtender;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;

import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.jsps.parserapi.JspParserFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.junit.NbTestCase;

/**
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

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        clearWorkDir();
        TestUtil.setup(this);
    }

    public void testAnalysisBasicJspx() throws Exception {
        parserTestInProject("project2", "/web/basic.jspx");
    }

    public void testAnalysisMain() throws Exception {
        parserTestInProject("project2", "/web/main.jsp");
    }

    public void testAnalysisBean() throws Exception {
        parserTestInProject("project2", "/web/more_for_test/bean.jsp");
    }

    public void testAnalysisTagLinkList() throws Exception {
        parserTestInProject("project2", "/web/WEB-INF/tags/linklist.tag");
    }

    public void testAnalysisFaulty() throws Exception {
        parserTestInProject("project2", "/web/faulty.jsp");
    }

    public void testAnalysisOutsideWM() throws Exception {
        parserTestInProject("project2", "/outside/outsidewm.jsp");
    }

    public void testAnalysisFunction() throws Exception {
        parserTestInProject("project3", "/web/jsp2/el/functions.jsp");
    }

    public void testAnalysisXMLTextRotate_1_6() throws Exception {
        String javaVersion = System.getProperty("java.version");

        if (javaVersion.startsWith("1.6")){
            parserTestInProject("project3", "/web/jsp2/jspx/textRotate.jspx");
        }
    }

    public void testAnalysisXMLTextRotate_1_5() throws Exception {
        String javaVersion = System.getProperty("java.version");

        if (javaVersion.startsWith("1.5")){
            parserTestInProject("project3", "/web/jsp2/jspx/textRotate.jspx");
        }

    }

    public void testAnalysisXMLTextRotate_1_4() throws Exception {
        String javaVersion = System.getProperty("java.version");

        if (javaVersion.startsWith("1.4")){
            parserTestInProject("project3", "/web/jsp2/jspx/textRotate.jspx");
        }
    }

    public void testAnalysisTagLibFromTagFiles() throws Exception {
        String javaVersion = System.getProperty("java.version");
        if (!javaVersion.startsWith("1.6")){
            parserTestInProject("project2", "/web/testTagLibs.jsp");
        }
    }

    public void testAnalysisTagLibFromTagFiles_1_6() throws Exception {
        String javaVersion = System.getProperty("java.version");
        if (javaVersion.startsWith("1.6")){
            parserTestInProject("project2", "/web/testTagLibs.jsp");
        }
    }

    public void testJSPInclude() throws Exception {
        parserTestInProject("project2", "/web/jspInclude.jsp");
    }

    public void testInclude() throws Exception {
        parserTestInProject("project2", "/web/include.jsp");

    }

    public void testIncludePreludeCoda() throws Exception {
        JspParserAPI.ParseResult result = parserTestInProject("project2", "/web/includePreludeCoda.jsp");
        log("Prelude: " + result.getPageInfo().getIncludePrelude());
        log("Coda: " + result.getPageInfo().getIncludeCoda());
    }

     public void testTagFileAttribute() throws Exception {
        parserTestInProject("project3", "/web/WEB-INF/tags/displayProducts.tag");
        parserTestInProject("project3", "/web/WEB-INF/tags/displayProducts.tag");
    }

    // test for issue #70426
    public void testGetTagLibMap70426() throws Exception{
        File projectFile = new File(getDataDir(), "emptyWebProject");
        Project project = ProjectManager.getDefault().findProject(FileUtil.toFileObject(projectFile));
        FileObject jspFo = project.getProjectDirectory().getFileObject("web/index.jsp");
        JspParserAPI.WebModule wm = TestUtil.getWebModule(jspFo);
        Map library = JspParserFactory.getJspParser().getTaglibMap(wm);
        System.out.println("map->" + library);
        Library jstlLibrary = LibraryManager.getDefault().getLibrary("jstl11");
        assertNotNull("Library has to be found", jstlLibrary);
        ProjectClassPathExtender cpExtender = project.getLookup().lookup(ProjectClassPathExtender.class);
        cpExtender.addLibrary(jstlLibrary);
        library = JspParserFactory.getJspParser().getTaglibMap(wm);
        System.out.println("map->" + library);
        assertTrue("The JSTL/core library was not returned.", (library.get("http://java.sun.com/jsp/jstl/core")) != null);
    }

    public JspParserAPI.ParseResult parserTestInProject(String projectFolderName, String pagePath) throws Exception{
        log("Parsing test of page  " + pagePath + " in project " + projectFolderName + " started.");
        FileObject projectPath = FileUtil.toFileObject(new File(getDataDir(), projectFolderName));
        Project project = ProjectManager.getDefault().findProject(projectPath);
        assertNotNull("Project should exist", project);
        FileObject jspFo = projectPath.getFileObject(pagePath);
        assertNotNull("JSP file should exist", jspFo);
        log("Parsing page " + pagePath);

        WebModule webModule = TestUtil.getWebModule(jspFo);
        JspParserAPI jspParser = JspParserFactory.getJspParser();
        JspParserAPI.ParseResult result = jspParser.analyzePage(jspFo, webModule, JspParserAPI.ERROR_IGNORE);
        assertNotNull("The result from the parser was not obtained.", result);
        
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
        try {
            assertFile(outFile, goldenF, getWorkDir());
        } catch (Error e) {
            System.out.println("golden: " + goldenF);
            System.out.println("outFile: " + outFile);
            fail(e.getMessage());
        }
        
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
