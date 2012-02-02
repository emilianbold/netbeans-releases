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

package org.netbeans.modules.subversion;

import junit.framework.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.masterfs.filebasedfs.BaseFileObjectTestHid;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedURLMapper;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory;
import org.netbeans.modules.versioning.util.FileUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileObjectTestHid;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystemFactoryHid;
import org.openide.filesystems.FileSystemTestHid;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileUtilTestHidden;
import org.openide.filesystems.URLMapperTestHidden;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * @author Tomas Stupka
 */
public class SvnFileSystemTest extends FileSystemFactoryHid {
    private String workDirPath;

    public SvnFileSystemTest(Test test) {
        super(test);
    }
    
    protected void setUp() throws Exception {
        super.setUp();

        File f = getWorkDir("userdir");
        // cleanup first
        FileUtils.deleteRecursively(f.getParentFile());

        f.mkdirs();
        System.setProperty("netbeans.user", f.getAbsolutePath());
        MockServices.setServices(new Class[] {FileBasedURLMapper.class});

        // ensure test files are handled by LH
        System.setProperty("netbeans.localhistory.historypath", System.getProperty("work.dir"));

        workDirPath = System.getProperty("work.dir");//NOI18N
        workDirPath = (workDirPath != null) ? workDirPath : System.getProperty("java.io.tmpdir");//NOI18N
        if(!workDirPath.isEmpty()) {
            f = new File(workDirPath);
            FileUtils.deleteRecursively(f);
        }
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();        
//        suite.addTestSuite(FileSystemTestHid.class);
        suite.addTestSuite(FileObjectTestHid.class);
//        suite.addTestSuite(URLMapperTestHidden.class);
//        suite.addTestSuite(FileUtilTestHidden.class);
//        suite.addTestSuite(FileUtilJavaIOFileHidden.class);
//        suite.addTestSuite(BaseFileObjectTestHid.class);
        return new SvnFileSystemTest(suite);
    }
    
    private File getWorkDir(String testName) {
        File f = new File(workDirPath + "/" + testName + "/wc");
        f.mkdirs();
        return f;
    }

    private File getRepoDir(String testName) {
        return new File(workDirPath + "/" + testName + "/repo");
    }

    protected FileSystem[] createFileSystem(String testName, String[] resources) throws IOException {        
        // cleanup just in case there will be two test with the same name
        // destroyfilesytem() doesnt seem to be called
        File f = new File(workDirPath + "/" + testName);
        if(f.exists()) {
            FileUtils.deleteRecursively(f.getParentFile());
        }

        try {                                 
            repoinit(testName);
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        } 
                    
        FileObjectFactory.reinitForTests();
        FileObject workFo = FileBasedFileSystem.getFileObject(getWorkDir(testName));
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
        commit(files, testName);
        return new FileSystem[]{workFo.getFileSystem()};
    }
    
    protected void destroyFileSystem(String testName) throws IOException {
        FileUtils.deleteRecursively(getWorkDir(testName));
    }

    protected String getResourcePrefix(String testName, String[] resources) {
        return FileBasedFileSystem.getFileObject(getWorkDir(testName)).getPath();
    }

    private void repoinit(String testName) throws IOException {
        try {
            File repoDir = getRepoDir(testName);
            File wc = getWorkDir(testName);            
            if (!repoDir.exists()) {
                repoDir.mkdirs();                
                String[] cmd = {"svnadmin", "create", repoDir.getAbsolutePath()};
                Process p = Runtime.getRuntime().exec(cmd);
                p.waitFor();                
            }            
            
            ISVNClientAdapter client = getClient(getRepoUrl(testName));
            SVNUrl url = getRepoUrl(testName).appendPath(getWorkDir(testName).getName());
            client.mkdir(url, "mkdir");
            client.checkout(url, wc, SVNRevision.HEAD, true);
        } catch (Exception ex) {
            throw new IOException(ex.getMessage());
        } 
    }
    
    private void commit(List<File> files, String testName) throws IOException {
        try {   
            ISVNClientAdapter client = getClient(getRepoUrl(testName));
            List<File> filesToAdd = new ArrayList<File>();
            for (File file : files) {
                
                ISVNStatus status = getStatus(file, testName);
                if(status.getTextStatus().equals(SVNStatusKind.UNVERSIONED)) {                   
                    filesToAdd.add(file);

                    File parent = file.getParentFile();
                    while (!getWorkDir(testName).equals(parent)) {
                        status = getStatus(parent, testName);
                        if(status.getTextStatus().equals(SVNStatusKind.UNVERSIONED)) {
                            filesToAdd.add(0, parent);
                            parent = parent.getParentFile();
                        } else {
                            break;
                        }
                    }                                    
                }    
            }
            for (File file : filesToAdd) {
                try {
                    client.addFile(file);
                } catch (SVNClientException e) {
                    if(!e.getMessage().toLowerCase().contains("entry already exists")) { // javahl
                        throw e;
                    }
                }
            }
                
            client.commit(new File[] {getWorkDir(testName)}, "commit", true);
            FileStatusCache cache = Subversion.getInstance().getStatusCache();
            for (File file : filesToAdd) {
                cache.refresh(file, null);
            }
            // block until uptodate
            while(true) {
                boolean fine = true;
                for (File file : filesToAdd) {                    
                    FileInformation s = cache.getCachedStatus(file);
                    if(s == null) break; // means uptodate
                    if(s.getStatus() != FileInformation.STATUS_VERSIONED_UPTODATE) {
                        fine = false;
                        break;
                    }
                }
                if(fine) {
                    break;
                }
                try { Thread.sleep(200); } catch (InterruptedException e) { }                
            }
            
        } catch (SVNClientException ex) {
            throw new IOException(ex.getMessage());
        }
    }
    
    private ISVNClientAdapter getClient(SVNUrl url) throws SVNClientException {
        return Subversion.getInstance().getClient(url);
    }

    private void assertStatus(File f, String testName) throws IOException {
        try {
            ISVNStatus status = getStatus(f, testName);
            assertEquals(SVNStatusKind.NORMAL, status.getTextStatus());
        } catch (SVNClientException ex) {
            throw new IOException(ex.getMessage());
        }
    }    

    private ISVNStatus getStatus(File f, String testName) throws SVNClientException, MalformedURLException {
        return getClient(getRepoUrl(testName)).getSingleStatus(f);
    }
    
    private SVNUrl getRepoUrl(String testName) throws MalformedURLException {
        return new SVNUrl("file:///" + getRepoDir(testName).getAbsolutePath().replaceAll("\\\\", "/"));
    }    

}
