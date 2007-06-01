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

package org.netbeans.modules.j2ee.ejbcore.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;

/**
 * Common ancestor for all test classes.
 *
 * @author Andrei Badea
 * @author Martin Adamek
 */
public class TestBase extends NbTestCase {
    
    private EjbJarProviderImpl ejbJarProvider;
    private ClassPathProviderImpl classPathProvider;
    private FileOwnerQueryImpl fileOwnerQuery;
    
    protected FileObject dataDir;
    protected FileObject testFO;
    
    static {
        setLookups();
        assertEquals(RepositoryImpl.class, Lookup.getDefault().lookup(Repository.class).getClass());
        assertEquals("The default Repository is not our repository!", RepositoryImpl.class, Repository.getDefault().getClass());
    }
    
    public TestBase(String name) {
        super(name);
    }
    
    /**
     * Creates copy of EJB 2.1 project in test's working directory
     * and returns TestModule wrapper for that
     */
    public TestModule createEjb21Module() throws IOException {
        return createTestModule("EJBModule_1_4", "2.1");
    }
    
    /**
     * Creates copy of EJB 3.0 project in test's working directory
     * and returns TestModule wrapper for that
     */
    public TestModule createEjb30Module() throws IOException {
        return createTestModule("EJBModule_5_0", "3.0");
    }

    /**
     * Creates new copy of project in test's working directory instead of using one froo data dir,
     * co it can be called multiple times on 'clean' project (without generated code)
     */
    private TestModule createTestModule(String projectDirName, String ejbVersion) throws IOException {
        
        File projectDir = new File(getDataDir(), projectDirName);
        File tempProjectDir = copyFolder(projectDir);
        
        TestModule testModule = new TestModule(FileUtil.toFileObject(tempProjectDir), ejbVersion);
        activate(testModule);
        
        return testModule;
        
    }
    
    protected void setUp() throws IOException {
        clearWorkDir();
        File file = new File(getWorkDir(),"cache");	//NOI18N
        file.mkdirs();
        IndexUtil.setCacheFolder(file);
        ejbJarProvider = new EjbJarProviderImpl();
        classPathProvider = new ClassPathProviderImpl();
        fileOwnerQuery = new FileOwnerQueryImpl();
        setLookups(
                ejbJarProvider,
                classPathProvider,
                fileOwnerQuery,
                new FakeJavaDataLoaderPool(),
                new TestSourceLevelQueryImplementation()
                );
        dataDir = FileUtil.toFileObject(getDataDir());
        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        testFO = workDir.createData("TestClass.java");
    }
    
    public static void setLookups(Object... instances) {
        Object[] allInstances = new Object[instances.length + 2];
        ClassLoader classLoader = TestBase.class.getClassLoader();
        allInstances[0] = classLoader;
        allInstances[1] = new RepositoryImpl();
        System.arraycopy(instances, 0, allInstances, 2, instances.length);
        MockLookup.setInstances(allInstances);
    }
    
    private void activate(TestModule testModule) {
        fileOwnerQuery.setProject(testModule.project);
        ejbJarProvider.setEjbModule(testModule.ejbLevel, testModule.deploymentDescriptor, testModule.sources);
        classPathProvider.setClassPath(testModule.sources);
        try {
            for (FileObject fileObject : testModule.sources) {
                RepositoryUpdater.getDefault().scheduleCompilationAndWait(fileObject, fileObject).await();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
  
    /**
     * Make a temporary copy of a whole folder into some new dir in the scratch area.<br>
     * Copy from /ant/freeform/test/unit/src/org/netbeans/modules/ant/freeform/TestBase.java
     */
    private File copyFolder(File d) throws IOException {
        assert d.isDirectory();
        File workdir = getWorkDir();
        String name = d.getName();
        while (name.length() < 3) {
            name = name + "x";
        }
        File todir = workdir.createTempFile(name, null, workdir);
        todir.delete();
        doCopy(d, todir);
        return todir;
    }
    
    private static void doCopy(File from, File to) throws IOException {
        if (from.isDirectory()) {
            to.mkdir();
            String[] kids = from.list();
            for (int i = 0; i < kids.length; i++) {
                doCopy(new File(from, kids[i]), new File(to, kids[i]));
            }
        } else {
            assert from.isFile();
            InputStream is = new FileInputStream(from);
            try {
                OutputStream os = new FileOutputStream(to);
                try {
                    FileUtil.copy(is, os);
                } finally {
                    os.close();
                }
            } finally {
                is.close();
            }
        }
    }
    
    protected static class TestModule {
        
        private final FileObject projectDir;
        private final String ejbLevel;
        private final FileObject deploymentDescriptor;
        private final FileObject[] sources;
        private final ProjectImpl project;
        
        public TestModule(FileObject projectDir, String ejbLevel) {
            this.projectDir = projectDir;
            this.ejbLevel = ejbLevel;
            this.deploymentDescriptor = projectDir.getFileObject("src/conf/ejb-jar.xml");
            this.sources = new FileObject[] { projectDir.getFileObject("src/java") };
            this.project = new ProjectImpl(ejbLevel);
            project.setProjectDirectory(projectDir);
        }
        
        public FileObject getDeploymentDescriptor() { return deploymentDescriptor; }
        public FileObject[] getSources() { return sources; }
        public Project getProject() { return project; }
        public FileObject getConfigFilesFolder() { return projectDir.getFileObject("src/conf"); }
        
    }
    
    public static final class TestSourceLevelQueryImplementation implements SourceLevelQueryImplementation {
        
        public String getSourceLevel(FileObject javaFile) {
            return "1.5";
        }
        
    }
    
}
