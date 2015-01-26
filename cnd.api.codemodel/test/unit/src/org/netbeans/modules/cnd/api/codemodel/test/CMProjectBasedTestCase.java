/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.api.codemodel.test;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.codemodel.CMDiagnostic;
import org.netbeans.modules.cnd.api.codemodel.CMIndex;
import org.netbeans.modules.cnd.api.codemodel.visit.CMDeclaration;
import org.netbeans.modules.cnd.api.codemodel.visit.CMEntityReference;
import org.netbeans.modules.cnd.api.codemodel.visit.CMInclude;
import org.netbeans.modules.cnd.api.codemodel.visit.CMVisitQuery;
import org.netbeans.modules.cnd.api.project.NativeFileItemSet;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.codemodel.bridge.impl.NativeProjectCompilationDataBase;
import org.netbeans.modules.cnd.codemodel.storage.api.CMStorageManager;
import org.netbeans.modules.cnd.codemodel.storage.spi.CMStorage;
import org.netbeans.modules.cnd.spi.codemodel.providers.CMCompilationDataBase;
import org.netbeans.modules.cnd.spi.codemodel.support.SPIUtilities;
import org.netbeans.modules.cnd.spi.codemodel.support.SimpleCompilationDataBase;
import org.netbeans.modules.cnd.test.CndCoreTestUtils;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 *
 * @author petrk
 */
public abstract class CMProjectBasedTestCase extends CMBaseTestCase {

    protected static final CMVisitQuery.VisitOptions INDEX_OPTIONS = CMVisitQuery.VisitOptions.valueOf(
            CMVisitQuery.VisitOptions.SuppressWarnings,
            //CMVisitQuery.VisitOptions.SkipParsedBodiesInSession
            CMVisitQuery.VisitOptions.None);
    private ProjectData pd;

    private final Map<String, CMCompilationDataBase> projects = new HashMap<String, CMCompilationDataBase>();
    private final Map<String, List<String>> sysIncludes = new HashMap<String, List<String>>();
    private final Map<String, List<String>> usrIncludes = new HashMap<String, List<String>>();
    private final Map<String, List<String>> projectDependencies = new HashMap<String, List<String>>();

    protected PrintWriter outputWriter  = null;
    
    protected PrintWriter logWriter = null;
    
    private final boolean performInWorkDir;
    private final boolean createProjectData;
    private File workDirBasedProject = null;
    
    /**
     * Creates a new instance of CompletionBaseTestCase
     */
    public CMProjectBasedTestCase(String testName) {
        this(testName, false);
    }
    
    /**
     * if test performs any modifications in data files or create new files
     * => pass performInWorkDir as 'true' to create local copy of project in work dir
     */
    public CMProjectBasedTestCase(String testName, boolean performInWorkDir) {
        this(testName, performInWorkDir, true);
    }
    
    public CMProjectBasedTestCase(String testName, boolean performInWorkDir, boolean createProjectData) {
        super(testName);
        this.performInWorkDir = performInWorkDir;
        this.createProjectData = createProjectData;
    }    

    protected final List<String> getSysIncludes(String prjPath) {
        return getProjectPaths(this.sysIncludes, prjPath);
    }

    protected void setSysIncludes(String prjPath, List<String> sysIncludes) {
        this.sysIncludes.put(prjPath, sysIncludes);
    }

    protected final List<String> getUsrIncludes(String prjPath) {
        return getProjectPaths(this.usrIncludes, prjPath);
    }

    protected void setLibProjectsPaths(String prjPath, List<String> dependentProjects) {
        this.projectDependencies.put(prjPath, dependentProjects);
    }

    protected List<String> getLibProjectsPaths(String prjPath) {
        return getProjectPaths(this.projectDependencies, prjPath);
    }

    protected void setUsrIncludes(String prjPath, List<String> usrIncludes) {
        this.usrIncludes.put(prjPath, usrIncludes);
    }
    
    @Override
    protected void setUp() throws Exception {
        CndUtils.clearLastAssertion();
        super.setUp();
        super.clearWorkDir();
        
        outputWriter  = new PrintWriter(getRef());
        logWriter = new PrintWriter(getLog());
        
        log("setUp preparing project.");
        File projectDir;
        if (performInWorkDir) {
            workDirBasedProject = new File(getWorkDir(), "project"); // NOI18N
            // copy data dir
            CndCoreTestUtils.copyDirToWorkDir(getTestCaseDataDir(), workDirBasedProject);
            projectDir = workDirBasedProject; 
        } else {
            projectDir = getTestCaseDataDir();
        }
        File[] changedDirs = changeDefProjectDirBeforeParsingProjectIfNeeded(projectDir);
        for (int i = 0; i < changedDirs.length; i++) {
            File file = changedDirs[i];
            String prjPath = file.getAbsolutePath();          
            CMCompilationDataBase cdb = createCompilationDatabase(file, getSysIncludes(prjPath), getUsrIncludes(prjPath), getLibProjectsPaths(prjPath));
            projects.put(prjPath, cdb);
            if (createProjectData) {
                pd = prepareProject(cdb, "jdbc:h2");
            }
        }
        log("setUp finished preparing project.");
        log("Test "+getName()+  "started");
    }      
    
    /**
     * change the folder if needed from test folder to subfolder
     * i.e. if test folder has several folders: for project and libs =>
     * change dir to subfolders corresponding to projects dirs
     * @param projectDir current project dir
     * @return folders that should be used as project directories
     */
    protected File[] changeDefProjectDirBeforeParsingProjectIfNeeded(File projectDir) {
        return new File[] {projectDir};
    }

    protected void checkDir(File srcDir) {
        assertTrue("Not existing directory" + srcDir, srcDir.exists());
        assertTrue("Not directory" + srcDir, srcDir.isDirectory());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
//        Iterator<TestModelHelper> iterator = projectHelpers.values().iterator();
//        while (iterator.hasNext()) {
//            TestModelHelper testModelHelper = iterator.next();
//            testModelHelper.shutdown(!iterator.hasNext());
//        }
        if (outputWriter != null) {
            outputWriter.flush();
            outputWriter.close();
        }
        if (logWriter != null) {
            logWriter.flush();
            logWriter.close();
        }
        projects.clear();
        sysIncludes.clear();
        usrIncludes.clear();
        projectDependencies.clear();
        
        if (createProjectData) {
            pd.dispose();
        }
        
        cleanUserDir();
        
        assertTrue("unexpected exception " + CndUtils.getLastAssertion(), CndUtils.getLastAssertion() == null);
    }

    @Override
    protected File getDataFile(String filename) {
        if (performInWorkDir) {
            return new File(workDirBasedProject, filename);
        } else {
            return super.getDataFile(filename);
        }
    }     
    
    protected CMCompilationDataBase getProject() {
        for (CMCompilationDataBase compilationDB : projects.values()) {
            return compilationDB;
        }
        assert false : "no initialized projects";
        return null;
    }    

    protected CMCompilationDataBase getProject(String path) {
        return projects.get(path);
    }
//
//    protected CsmModel getModel() {
//        for (TestModelHelper testModelHelper : projectHelpers.values()) {
//            return testModelHelper.getModel();
//        }
//        assert false : "no initialized projects";
//        return null;
//    }
//
//    protected void reopenProject(String name, boolean waitParse) {
//        for (TestModelHelper testModelHelper : projectHelpers.values()) {
//            if (name.contentEquals(testModelHelper.getProjectName())) {
//                testModelHelper.reopenProject();
//                if (waitParse) {
//                    waitAllProjectsParsed();
//                }
//                return;
//            }
//        }
//    }
//
//    protected void reparseAllProjects() {
//        Collection<CsmProject> projects = getModel().projects();
//        int expectedNrProjects = projects.size();
//        getModel().scheduleReparse(projects);
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//        projects = getModel().projects();
//        assertEquals("projects " + projects, expectedNrProjects, projects.size());
//        waitAllProjectsParsed();
//    }
//
//    protected void closeProject(String name) {
//        for (TestModelHelper testModelHelper : projectHelpers.values()) {
//            if (name.contentEquals(testModelHelper.getProjectName())) {
//                testModelHelper.resetProject();
//                return;
//            }
//        }
//        assertFalse("Project not found or getProject was not called for this name before: " + name, true);
//    }
//    
//    protected int getOffset(File testSourceFile, int lineIndex, int colIndex) throws Exception {
//        BaseDocument doc = getBaseDocument(testSourceFile);
//        assert doc != null;
//        int offset = CndCoreTestUtils.getDocumentOffset(doc, lineIndex, colIndex);  
//        return offset;
//    }
//
//
//    protected void dumpModel() {
//        for (CsmProject prj : getModel().projects()) {
//            new CsmTracer(System.err).dumpModel(prj);
//            for (CsmProject lib : prj.getLibraries()) {
//                new CsmTracer(System.err).dumpModel(lib);
//            }
//        }
//        LibraryManager.dumpInfo(new PrintWriter(System.err), true);
//    }
//
//    protected void waitAllProjectsParsed() {
//        sleep(1000);
//        Collection<CsmProject> projects;
//        projects = getModel().projects();
//        for (CsmProject csmProject : projects) {
//            TraceModelBase.waitProjectParsed(((ProjectBase) csmProject), true);
//        }
//    }   
    
    private List<String> getProjectPaths(Map<String, List<String>> map, String prjPath) {
        List<String> dependentProjects = map.get(prjPath);
        if (dependentProjects == null) {
            return Collections.emptyList();
        }
        return dependentProjects;
    }    
    
    private CMCompilationDataBase createCompilationDatabase(File projectRoot, List<String> sysIncludes, List<String> usrIncludes, List<String> libPaths) {
        SimpleCompilationDataBase.Builder builder = new SimpleCompilationDataBase.Builder();
        builder.setBaseDir(projectRoot);
        builder.setDefaultCompileCommand(getCompileCommand(sysIncludes, usrIncludes, libPaths));
        
        List<File> projectFiles = getProjectFiles(projectRoot);
        for (File file : projectFiles) {
            if (file.getName().endsWith(".c") || file.getName().endsWith(".cpp") || file.getName().endsWith(".cc")) {
                builder.addEntry(file);
            }
        }
        
        return builder.createDataBase();          
    }
    
    private String getCompileCommand(List<String> sysIncludes, List<String> usrIncludes, List<String> libPaths) {
        StringBuilder builder = new StringBuilder(getDefaultCompileCommand());
        builder.append(" ");
        for (String include : sysIncludes) {
            builder.append("-I").append(include);
        }
        for (String include : usrIncludes) {
            builder.append("-I").append(include);
        }        
        return builder.toString();
    }
    
    private List<File> getProjectFiles(File projectRoot) {
        List<File> files = new ArrayList<>();
        fillFiles(files, projectRoot);
        return files;
    }
    
    private void fillFiles(List<File> files, File file) {
        if (file.isDirectory()) {
            String[] list = file.list();
            for (int i = 0; i < list.length; i++) {
                fillFiles(files, new File(file, list[i]));
            }
        } else {
            files.add(file);
        }
    }
    
    protected int getOffset(File testSourceFile, int lineIndex, int colIndex) throws Exception {
        BaseDocument doc = getBaseDocument(testSourceFile);
        assert doc != null;
        int offset = CndCoreTestUtils.getDocumentOffset(doc, lineIndex, colIndex);  
        return offset;
    }
    
    protected BaseDocument getBaseDocument(File testSourceFile) throws Exception {
        FileObject testFileObject = CndFileUtils.toFileObject(testSourceFile);
        assertNotNull("Unresolved test file " + testSourceFile, testFileObject);//NOI18N
        DataObject testDataObject = DataObject.find(testFileObject);
        assertNotNull("Unresolved data object for file " + testFileObject, testDataObject);//NOI18N
        BaseDocument doc = CndCoreTestUtils.getBaseDocument(testDataObject);
        assertNotNull("Unresolved document for data object " + testDataObject, doc);//NOI18N
        NativeFileItemSet nfis = testDataObject.getLookup().lookup(NativeFileItemSet.class);
        assertNotNull("Not found NativeFileItemSet in data object " + testDataObject, nfis);//NOI18N
        assertNotNull("Not found registered items in NativeFileItemSet " + testDataObject, !nfis.getItems().isEmpty());//NOI18N
        return doc;
    }

    protected static class ProjectData {

        private final Object indexKey;
        private final CMStorage storage;

        public ProjectData(Object key, CMStorage storage) {
            this.indexKey = key;
            this.storage = storage;
        }

        public void dispose() {
            SPIUtilities.unregisterIndex(indexKey);
            storage.shutdown();
            CMStorageManager.testShutdown(storage);
        }
    }

    protected ProjectData prepareProject(FileObject projectFileObject, String odbcUrl) throws Exception {
        assertNotNull(projectFileObject);
        Project findProject = ProjectManager.getDefault().findProject(projectFileObject);
        assertNotNull(findProject);

        NativeProject nativeProject = findProject.getLookup().lookup(NativeProject.class);
        assertNotNull(nativeProject);
        NativeProjectCompilationDataBase nativeProjectCompilationDataBase = new NativeProjectCompilationDataBase(nativeProject);
        return prepareProject(nativeProjectCompilationDataBase, odbcUrl);
    }

    protected ProjectData prepareProject(CMCompilationDataBase nativeProjectCompilationDataBase, String odbcUrl) throws Exception {
        long startTime = System.currentTimeMillis();
        CMIndex idx = SPIUtilities.parse(nativeProjectCompilationDataBase);
        final Object key = nativeProjectCompilationDataBase;
        SPIUtilities.registerIndex(key, idx);
        Collection<CMCompilationDataBase.Entry> entries = nativeProjectCompilationDataBase.getEntries();
        long endIndexingPart = System.currentTimeMillis();
        System.out.println("indexing time " + toTimeString(endIndexingPart, startTime));
        final String storageName = "CursorVisitorProjectTest.References." + key;
        final CMStorage storage = CMStorageManager.getInstance(storageName, odbcUrl);
        final AtomicInteger counter = new AtomicInteger(0);
        //CMVisitQuery.visitReferences(null, Arrays.asList(idx), new CMVisitQuery.IndexCallback() {
        CMVisitQuery.visitIndex(idx, new CMVisitQuery.IndexCallback() {
            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public void onDiagnostics(Iterable<CMDiagnostic> diagnostics) {
            }

            @Override
            public void onIndclude(CMInclude include) {
                //System.out.println("new include:" + include.toString());
            }

            @Override
            public void onTranslationUnit() {
                //  System.out.println("new translation unit");
            }

            @Override
            public void onDeclaration(CMDeclaration declaration) {
                counter.incrementAndGet();
                storage.addDeclaration(declaration);
            }

            @Override
            public void onReference(CMEntityReference entityReference) {
                //   System.out.println("On reference : " + counter.incrementAndGet());
                counter.incrementAndGet();
                storage.addEntityReference(entityReference);
            }
        }, INDEX_OPTIONS);
        long endObjectCreation = System.currentTimeMillis();
        long callbackDuration = endObjectCreation - startTime;
        long startDatabase = System.currentTimeMillis();
        System.out.println("Will start database flush, "
                + "      callbackDuration=" + toTimeString(callbackDuration, 0));
        storage.flush();
        long endTime = System.currentTimeMillis();

        //long objectCreationDuration = startDatabase -startObjectCreation ;
        long databaseFlush = endTime - startDatabase;
        long totalTime = endTime - startTime;
        System.out.println("name=" + key + " decl count=" + counter.get() + "\n"
                + "      totalTime=" + toTimeString(totalTime, 0)
                + "      callbackDuration=" + toTimeString(callbackDuration, 0)
                //                + "      objectCreationDuration=" + objectCreationDuration +
                //                " ms which is " + (objectCreationDuration/1000) + "." + (objectCreationDuration%1000) + " s "
                + "      flush duration=" + toTimeString(databaseFlush, 0));

        return new ProjectData(key, storage);
    }

    protected String toTimeString(long end, long start) {
        return (end - start)+ "ms which is " +
                ((end - start) / 1000) + "." + ((end - start) % 1000) + "s";
    }
}
