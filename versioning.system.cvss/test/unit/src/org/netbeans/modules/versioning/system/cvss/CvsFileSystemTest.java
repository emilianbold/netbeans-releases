/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.versioning.system.cvss;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import junit.framework.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.lib.cvsclient.admin.Entry;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.add.AddCommand;
import org.netbeans.lib.cvsclient.command.commit.CommitCommand;
import org.netbeans.lib.cvsclient.command.importcmd.ImportCommand;
import org.netbeans.modules.masterfs.filebasedfs.BaseFileObjectTestHid;
import org.netbeans.modules.versioning.system.cvss.ui.actions.commit.CommitExecutor;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedURLMapper;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory;
import org.netbeans.modules.versioning.system.cvss.ui.actions.add.AddExecutor;
import org.netbeans.modules.versioning.util.FileUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileObjectTestHid;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystemFactoryHid;
import org.openide.filesystems.FileSystemTestHid;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileUtilTestHidden;
import org.openide.filesystems.URLMapperTestHidden;


/**
 * @author Tomas Stupka
 */
public class CvsFileSystemTest extends FileSystemFactoryHid {

    private Method createFileInformationMethod;

    public CvsFileSystemTest(Test test) {
        super(test);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        File workDir = getRepoDir().getParentFile();
        FileUtils.deleteRecursively(workDir);
        File userdir = new File(workDir, "userdir");
        userdir.mkdirs();
        System.setProperty("netbeans.user", userdir.getAbsolutePath());
        MockServices.setServices(new Class[] {FileBasedURLMapper.class});                
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();        
        suite.addTestSuite(FileSystemTestHid.class);
        suite.addTestSuite(FileObjectTestHid.class);
        suite.addTestSuite(URLMapperTestHidden.class);
        suite.addTestSuite(FileUtilTestHidden.class);                
        suite.addTestSuite(BaseFileObjectTestHid.class);            
        
        // XXX fails
//        suite.addTest(new FileObjectTestHid("testGetFolders3"));
//        suite.addTest(new FileObjectTestHid("testGetData1"));
//        suite.addTest(new FileObjectTestHid("testRefresh3"));
                
        return new CvsFileSystemTest(suite);
    }   
    
    private File getWorkDir() {
        String workDirProperty = System.getProperty("workdir");//NOI18N
        workDirProperty = (workDirProperty != null) ? workDirProperty : System.getProperty("java.io.tmpdir");//NOI18N
        return new File(workDirProperty);
    }

    private File getRepoDir() {
        return new File(new File(System.getProperty("work.root.dir")), "repo");
    }

    @Override
    protected FileSystem[] createFileSystem(String testName, String[] resources) throws IOException {
        try {                                 
            repoinit();            
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        } 
                    
        FileObjectFactory.reinitForTests();
        FileObject workFo = FileBasedFileSystem.getFileObject(getWorkDir());
        assertNotNull(workFo);
        List<File> files = new ArrayList<File>(resources.length);
        for (int i = 0; i < resources.length; i++) {            
            String res = resources[i];
            FileObject fo;
            if (res.endsWith("/")) {
                fo = FileUtil.createFolder(workFo,res);
                assertNotNull(fo);
            } else {
                fo = FileUtil.createData(workFo,res);
                assertNotNull(fo);
            }            
            files.add(FileUtil.toFile(fo));            
        }
        commit(files);               
        for (File f : files) {
            assertStatus(f);
        }
        return new FileSystem[]{workFo.getFileSystem()};
    }
    
    @Override
    protected void destroyFileSystem(String testName) throws IOException {}    

    @Override
    protected String getResourcePrefix(String testName, String[] resources) {
        return FileBasedFileSystem.getFileObject(getWorkDir()).getPath();
    }

    private void repoinit() throws IOException {                        
        try {
            File repoDir = getRepoDir();
            
            if (!repoDir.exists()) {
                repoDir.mkdirs();                
                String[] cmd = {"cvs", "-d", repoDir.getAbsolutePath(), "init"};
                Process p = Runtime.getRuntime().exec(cmd);
                p.waitFor();                
            }            
            
            ExecutorGroup group = new ExecutorGroup("tamaryokucha");            
            GlobalOptions gtx = CvsVersioningSystem.createGlobalOptions();
            gtx.setCVSRoot(getRepoDir().getAbsolutePath());
            ImportCommand importCommand = new ImportCommand();
            importCommand.setModule(getWorkDir().getName());
            importCommand.setLogMessage("sencha");            
            importCommand.setImportDirectory(getWorkDir().getAbsolutePath());
            importCommand.setVendorTag("aracha");
            importCommand.setReleaseTag("bancha");
            
            createImportExecutor(importCommand, gtx, group);
            group.execute();        
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        } 
    }
    
    private void commit(List<File> files) throws IOException {       
        try {
            File[] fileArray = files.toArray(new File[files.size()]);
            AddFiles(fileArray);
            commitFiles(fileArray);                       
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }         
    }    

    private void commitFiles(File[] fileArray) {       
        CommitCommand commit = new CommitCommand();
        commit.setFiles(fileArray);
        commit.setMessage("matcha");
        
        GlobalOptions gtx = CvsVersioningSystem.createGlobalOptions();
        gtx.setCVSRoot(getRepoDir().getAbsolutePath());            
        ExecutorSupport[] executors = CommitExecutor.splitCommand(commit, CvsVersioningSystem.getInstance(), gtx);
        for (ExecutorSupport e : executors) {
            e.execute();
        }
        ExecutorSupport.wait(executors);
    }
    
    private void AddFiles(File[] fileArray) {
        AddCommand add = new AddCommand();                    
        add.setFiles(fileArray);        
        GlobalOptions gtx = CvsVersioningSystem.createGlobalOptions();
        gtx.setCVSRoot(getRepoDir().getAbsolutePath());            
        ExecutorSupport[] executors = AddExecutor.splitCommand(add, CvsVersioningSystem.getInstance(), gtx);
        for (ExecutorSupport e : executors) e.execute();
        ExecutorSupport.wait(executors);
    }
    
    private void assertStatus(File f) throws IOException {        
        try {
            Entry entry = null;
            CvsVersioningSystem cvs = CvsVersioningSystem.getInstance();
            try {                
                entry = cvs.getAdminHandler().getEntry(f);
            } catch (IOException e) {
                fail("No entry for " + f);
            }            
            FileInformation fi = createFileInformation(cvs, f, entry);
            assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, fi.getStatus());
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        } 
    }

    private FileInformation createFileInformation(CvsVersioningSystem cvs, File f, Entry entry) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if(createFileInformationMethod == null) {
            createFileInformationMethod = FileStatusCache.class.getDeclaredMethod("createFileInformation", new Class[]{File.class, Entry.class, int.class});
            createFileInformationMethod.setAccessible(true);
        }
        return (FileInformation) createFileInformationMethod.invoke(cvs.getStatusCache(), new Object[] {f, entry, FileStatusCache.REPOSITORY_STATUS_UPTODATE});
    }

    private void createImportExecutor(ImportCommand importCommand, GlobalOptions gtx, ExecutorGroup group) throws IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InstantiationException, SecurityException, ClassNotFoundException, InvocationTargetException {
        Class iec = Class.forName("org.netbeans.modules.versioning.system.cvss.ui.actions.project.ImportExecutor");
        Constructor c = iec.getConstructor(new Class[]{ImportCommand.class, GlobalOptions.class, boolean.class, String.class, ExecutorGroup.class});
        c.setAccessible(true);
        c.newInstance(new Object[]{importCommand, gtx, true, getWorkDir().getAbsolutePath(), group});
    }

}
