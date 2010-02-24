/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.modelimpl.test;

import java.io.File;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.trace.TestModelHelper;
import org.netbeans.modules.cnd.test.CndCoreTestUtils;

/**
 * IMPORTANT NOTE:
 * If This class is not compiled with the notification about not resolved
 * BaseTestCase class => cnd/core tests are not compiled
 *
 * To solve this problem compile or run tests for cnd/core
 */

/**
 * test case for working with projects
 * test has possibility to copy project data into working test dir to prevent changes
 * in source folders when test makes any changes in content of files
 * 
 * @author Vladimir Voskresensky
 */
public abstract class ProjectBasedTestCase extends ModelBasedTestCase {

    private TestModelHelper projectHelper = null;
    private List<String>    sysIncludes = Collections.<String>emptyList();
    private List<String>    usrIncludes = Collections.<String>emptyList();
    
    protected PrintWriter outputWriter  = null;
    
    protected PrintWriter logWriter = null;
    
    private final boolean performInWorkDir;
    private File workDirBasedProject = null;
    
    /**
     * Creates a new instance of CompletionBaseTestCase
     */
    public ProjectBasedTestCase(String testName) {
        this(testName, false);
    }
    
    /**
     * if test performs any modifications in data files or create new files
     * => pass performInWorkDir as 'true' to create local copy of project in work dir
     */
    public ProjectBasedTestCase(String testName, boolean performInWorkDir) {
        super(testName);
        this.performInWorkDir = performInWorkDir;
    }

    protected final List<String> getSysIncludes() {
        return sysIncludes;
    }

    protected void setSysIncludes(List<String> sysIncludes) {
        this.sysIncludes = sysIncludes;
    }

    protected final List<String> getUsrIncludes() {
        return usrIncludes;
    }

    protected void setUsrIncludes(List<String> usrIncludes) {
        this.usrIncludes = usrIncludes;
    }
    
//    protected final void initDocumentSettings() {
//        String methodName = ProjectBasedTestCase.class.getName() + ".getIdentifierAcceptor";
//        Preferences prefs;
//        prefs = MimeLookup.getLookup(MIMENames.CPLUSPLUS_MIME_TYPE).lookup(Preferences.class);
//        prefs.put(EditorPreferencesKeys.IDENTIFIER_ACCEPTOR, methodName);
//        prefs = MimeLookup.getLookup(MIMENames.HEADER_MIME_TYPE).lookup(Preferences.class);
//        prefs.put(EditorPreferencesKeys.IDENTIFIER_ACCEPTOR, methodName);
//        prefs = MimeLookup.getLookup(MIMENames.C_MIME_TYPE).lookup(Preferences.class);
//        prefs.put(EditorPreferencesKeys.IDENTIFIER_ACCEPTOR, methodName);
//    }

//    public static Acceptor getIdentifierAcceptor() {
//        return AcceptorFactory.JAVA_IDENTIFIER;
//    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("cnd.modelimpl.persistent", "false");           
        //initDocumentSettings();
        super.clearWorkDir();
        
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        
        log("setUp preparing project.");
        projectHelper = new TestModelHelper(true);
        File projectDir;
        if (performInWorkDir) {
            workDirBasedProject = new File(getWorkDir(), "project"); // NOI18N
            // copy data dir
            CndCoreTestUtils.copyDirToWorkDir(getTestCaseDataDir(), workDirBasedProject);
            projectDir = workDirBasedProject; 
        } else {
            projectDir = getTestCaseDataDir();
        }
        projectDir = changeDefProjectDirBeforeParsingProjectIfNeeded(projectDir);
        projectHelper.initParsedProject(projectDir.getAbsolutePath(), getSysIncludes(), getUsrIncludes());
        log("setUp finished preparing project.");
        log("Test "+getName()+  "started");
    }
    
    /**
     * change the folder if needed from test folder to subfolder
     * i.e. if test folder has several folders: for project and libs =>
     * change dir to subfolder corresponding to project dir
     * @param projectDir current project dir
     * @return folder that should be used as project dir
     */
    protected File changeDefProjectDirBeforeParsingProjectIfNeeded(File projectDir) {
        return projectDir;
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        projectHelper.shutdown(true);
        outputWriter.flush();
        logWriter.flush();
        outputWriter.close();
        logWriter.close();
        sysIncludes = Collections.<String>emptyList();
        usrIncludes = Collections.<String>emptyList();
    }

    @Override
    protected File getDataFile(String filename) {
        if (performInWorkDir) {
            return new File(workDirBasedProject, filename);
        } else {
            return super.getDataFile(filename);
        }
    }     
    
    protected CsmProject getProject() {
        return projectHelper.getProject();
    }
    
    protected CsmFile getCsmFile(File testSourceFile) throws Exception {
        CsmFile csmFile = CsmModelAccessor.getModel().findFile(testSourceFile.getAbsolutePath(), false);
        assertNotNull("Unresolved CsmFile for test file " + testSourceFile, csmFile);//NOI18N     
        return csmFile;
    }

    protected int getOffset(File testSourceFile, int lineIndex, int colIndex) throws Exception {
        BaseDocument doc = getBaseDocument(testSourceFile);
        assert doc != null;
        int offset = CndCoreTestUtils.getDocumentOffset(doc, lineIndex, colIndex);  
        return offset;
    }
}
