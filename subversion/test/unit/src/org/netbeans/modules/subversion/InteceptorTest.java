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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.subversion;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.subversion.client.SvnClientFactory;
import org.netbeans.modules.subversion.utils.TestUtilities;
import org.netbeans.modules.versioning.VersioningAnnotationProvider;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public class InteceptorTest extends NbTestCase {
    public static final String PROVIDED_EXTENSIONS_IS_VERSIONED = "ProvidedExtensions.VCSManaged";
    public static final String PROVIDED_EXTENSIONS_REMOTE_LOCATION = "ProvidedExtensions.RemoteLocation";
   
    private File dataRootDir;
    private FileStatusCache cache;
    private SVNUrl repoUrl;
    private File wc;
    private File wc2;
    private File repoDir;
    private File repo2Dir;
    private SVNUrl repo2Url;
        
    public InteceptorTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {          
        super.setUp();
        MockServices.setServices(new Class[] {
            VersioningAnnotationProvider.class,
            SubversionVCS.class});
        dataRootDir = new File(System.getProperty("data.root.dir")); 
        FileUtil.refreshFor(dataRootDir);
        wc = new File(dataRootDir, getName() + "_wc");
        wc2 = new File(dataRootDir, getName() + "_wc2");
        repoDir = new File(dataRootDir, "repo");
        String repoPath = repoDir.getAbsolutePath();
        if(repoPath.startsWith("/")) repoPath = repoPath.substring(1, repoPath.length());
        repoUrl = new SVNUrl("file:///" + repoPath);
        
        repo2Dir = new File(dataRootDir, "repo2");
        repo2Url = new SVNUrl(TestUtilities.formatFileURL(repo2Dir));

        System.setProperty("netbeans.user", System.getProperty("data.root.dir") + "/userdir");
        cache = Subversion.getInstance().getStatusCache();
        cache.cleanUp();
        
        cleanUpWC(wc);
        cleanUpWC(wc2);
        initRepo();      
        
        wc.mkdirs();
        wc2.mkdirs();
        svnimport();                   
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        cleanUpWC(wc);
        cleanUpWC(wc2);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite();

        suite.addTest(getAttributeSuite());

//        suite.addTest(createSuite());
//
//        suite.addTest(deleteSuite());
//
//        suite.addTest(renameViaDataObjectSuite());
//        suite.addTest(renameViaFileObjectSuite());
//
//        suite.addTest(moveViaDataObjectSuite());
//        suite.addTest(moveViaFileObjectSuite());

        return suite;
    }
    
    public static Test getAttributeSuite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new InteceptorTest("getWrongAttribute"));
        suite.addTest(new InteceptorTest("getRemoteLocationAttribute"));
        suite.addTest(new InteceptorTest("getIsVersionedAttribute"));
        return(suite);
    }

    public static Test deleteSuite() {
	TestSuite suite = new TestSuite();
        suite.addTest(new InteceptorTest("deleteCreateChangeCase_issue_157373"));
        suite.addTest(new InteceptorTest("deleteNotVersionedFile"));
        suite.addTest(new InteceptorTest("deleteVersionedFileExternally"));
        suite.addTest(new InteceptorTest("deleteVersionedFile"));
        suite.addTest(new InteceptorTest("deleteVersionedFolder"));
        suite.addTest(new InteceptorTest("deleteNotVersionedFolder"));
        suite.addTest(new InteceptorTest("deleteWCRoot"));
        suite.addTest(new InteceptorTest("deleteVersionedFileTree"));
        suite.addTest(new InteceptorTest("deleteNotVersionedFileTree"));
        return(suite);
    }
   
    public static Test createSuite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new InteceptorTest("createNewFile"));
        suite.addTest(new InteceptorTest("createNewFolder"));
        suite.addTest(new InteceptorTest("deleteA_CreateA"));
        suite.addTest(new InteceptorTest("deleteA_CreateA_RunAtomic"));
        return(suite);
    }
    
    public static Test renameViaDataObjectSuite() {
	TestSuite suite = new TestSuite();
        suite.addTest(new InteceptorTest("renameVersionedFile_DO"));
        suite.addTest(new InteceptorTest("renameUnversionedFile_DO"));
        suite.addTest(new InteceptorTest("renameUnversionedFolder_DO"));
        suite.addTest(new InteceptorTest("renameAddedFile_DO"));
        suite.addTest(new InteceptorTest("renameA2B2A_DO"));
        suite.addTest(new InteceptorTest("renameA2B2C_DO"));
        suite.addTest(new InteceptorTest("renameA2B2C2A_DO"));
        suite.addTest(new InteceptorTest("renameA2B_CreateA_DO"));
        suite.addTest(new InteceptorTest("deleteA_RenameB2A_DO_129805"));
        suite.addTest(new InteceptorTest("renameVersionedFolder_DO"));
        suite.addTest(new InteceptorTest("renameFileTree_DO"));
        suite.addTest(new InteceptorTest("renameA2CB2A_DO"));
        return(suite);
    }
    
    public static Test moveViaDataObjectSuite() {
	TestSuite suite = new TestSuite();
        suite.addTest(new InteceptorTest("moveVersionedFile_DO"));
        suite.addTest(new InteceptorTest("moveUnversionedFile_DO"));
        suite.addTest(new InteceptorTest("moveUnversionedFolder_DO"));
        suite.addTest(new InteceptorTest("moveAddedFile2UnversionedFolder_DO"));
        suite.addTest(new InteceptorTest("moveAddedFile2VersionedFolder_DO"));
        suite.addTest(new InteceptorTest("moveA2B2A_DO"));
        suite.addTest(new InteceptorTest("moveA2B2C_DO"));
        suite.addTest(new InteceptorTest("moveA2B2C2A_DO"));
        suite.addTest(new InteceptorTest("moveA2B_CreateA_DO"));
        suite.addTest(new InteceptorTest("moveVersionedFolder_DO"));
        suite.addTest(new InteceptorTest("moveFileTree_DO"));
        suite.addTest(new InteceptorTest("moveVersionedFile2Repos_DO"));
        suite.addTest(new InteceptorTest("moveVersionedFolder2Repos_DO"));
        suite.addTest(new InteceptorTest("moveFileTree2Repos_DO"));
        suite.addTest(new InteceptorTest("moveA2CB2A_DO"));
        return(suite);
    }
    
    public static Test renameViaFileObjectSuite() {
	TestSuite suite = new TestSuite();
        suite.addTest(new InteceptorTest("renameVersionedFile_FO"));
        suite.addTest(new InteceptorTest("renameUnversionedFile_FO"));
        suite.addTest(new InteceptorTest("renameUnversionedFolder_FO"));
        suite.addTest(new InteceptorTest("renameAddedFile_FO"));
        suite.addTest(new InteceptorTest("renameA2B2A_FO"));
        suite.addTest(new InteceptorTest("renameA2B2C_FO"));
        suite.addTest(new InteceptorTest("renameA2B2C2A_FO"));
        suite.addTest(new InteceptorTest("renameA2B_CreateA_FO"));
        suite.addTest(new InteceptorTest("deleteA_RenameB2A_FO_129805"));
        suite.addTest(new InteceptorTest("renameVersionedFolder_FO"));
        suite.addTest(new InteceptorTest("renameFileTree_FO"));
        suite.addTest(new InteceptorTest("renameA2CB2A_FO"));
        return(suite);
    }
    
    public static Test moveViaFileObjectSuite() {
	TestSuite suite = new TestSuite();
        suite.addTest(new InteceptorTest("moveVersionedFile_FO"));
        suite.addTest(new InteceptorTest("moveUnversionedFile_FO"));
        suite.addTest(new InteceptorTest("moveUnversionedFolder_FO"));
        suite.addTest(new InteceptorTest("moveAddedFile2UnversionedFolder_FO"));
        suite.addTest(new InteceptorTest("moveAddedFile2VersionedFolder_FO"));
        suite.addTest(new InteceptorTest("moveA2B2A_FO"));
        suite.addTest(new InteceptorTest("moveA2B2C_FO"));
        suite.addTest(new InteceptorTest("moveA2B2C2A_FO"));
        suite.addTest(new InteceptorTest("moveA2B_CreateA_FO"));
        suite.addTest(new InteceptorTest("moveVersionedFolder_FO"));
        suite.addTest(new InteceptorTest("moveFileTree_FO"));
        suite.addTest(new InteceptorTest("moveVersionedFile2Repos_FO"));
        suite.addTest(new InteceptorTest("moveVersionedFolder2Repos_FO"));
        suite.addTest(new InteceptorTest("moveFileTree2Repos_FO"));
        suite.addTest(new InteceptorTest("moveA2CB2A_FO"));
        
        return(suite);
    }
    
    public void getWrongAttribute() throws Exception {
        File file = new File(wc, "attrfile");
        file.createNewFile();
        FileObject fo = FileUtil.toFileObject(file);

        String str = (String) fo.getAttribute("peek-a-boo");
        assertNull(str);
    }

    public void getRemoteLocationAttribute() throws Exception {
        File file = new File(wc, "attrfile");
        file.createNewFile();
        FileObject fo = FileUtil.toFileObject(file);

        String str = (String) fo.getAttribute(PROVIDED_EXTENSIONS_REMOTE_LOCATION);
        assertNotNull(str);
        assertEquals(repoUrl.toString(), str);
    }

    public void getIsVersionedAttribute() throws Exception {
        // unversioned file
        File file = new File(dataRootDir, "unversionedfile");
        file.createNewFile();
        FileObject fo = FileUtil.toFileObject(file);

        Boolean versioned = (Boolean) fo.getAttribute(PROVIDED_EXTENSIONS_IS_VERSIONED);
        assertFalse(versioned);

        // metadata folder
        file = new File(wc, ".svn");
        fo = FileUtil.toFileObject(file);

        versioned = (Boolean) fo.getAttribute(PROVIDED_EXTENSIONS_IS_VERSIONED);
        assertTrue(versioned);

        // metadata file
        file = new File(new File(wc, ".svn"), "entries");
        fo = FileUtil.toFileObject(file);

        versioned = (Boolean) fo.getAttribute(PROVIDED_EXTENSIONS_IS_VERSIONED);
        assertTrue(versioned);

        // versioned file
        file = new File(wc, "attrfile");
        file.createNewFile();
        fo = FileUtil.toFileObject(file);

        versioned = (Boolean) fo.getAttribute(PROVIDED_EXTENSIONS_IS_VERSIONED);
        assertTrue(versioned);
    }

    public void deleteNotVersionedFile() throws Exception {
        // init        
        File file = new File(wc, "file");
        file.createNewFile();             
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file).getTextStatus());

        // delete
        delete(file);

        // test
        assertFalse(file.exists());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file).getTextStatus());
        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(file));
        
        commit(wc);        
    }

    public void deleteVersionedFile() throws Exception {
        // init
        File file = new File(wc, "file");
        file.createNewFile();
        commit(wc);
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file).getTextStatus());

        // delete
        delete(file);

        // test
        assertFalse(file.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(file).getTextStatus());

        assertCachedStatus(file, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);

        commit(wc);

        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file).getTextStatus());
    }

    public void deleteVersionedFileExternally() throws Exception {
        // init
        File file = new File(wc, "file");
        FileUtil.toFileObject(wc).createData(file.getName());
        assertCachedStatus(file, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        commit(wc);
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(file));

        // delete externally
        file.delete();

        // test
        assertFalse(file.exists());
        assertEquals(SVNStatusKind.MISSING, getSVNStatus(file).getTextStatus());

        // notify changes
        FileUtil.refreshFor(file);
        assertCachedStatus(file, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(file).getTextStatus());
        commit(wc);
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file).getTextStatus());
    }

    public void deleteVersionedFolder() throws Exception {
        // init        
        File folder = new File(wc, "folder1");
        folder.mkdirs();
        commit(wc);      
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(folder).getTextStatus());

        // delete
        delete(folder);

        // test
        assertTrue(folder.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(folder).getTextStatus());
        
        assertCachedStatus(folder, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);        
        
        commit(wc);
        
        assertFalse(folder.exists());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());        
    }

    public void deleteNotVersionedFolder() throws IOException, SVNClientException {
        // init        
        File folder = new File(wc, "folder2");
        folder.mkdirs();
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());

        // delete
        delete(folder);
        
        // test
        assertFalse(folder.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(folder));
        
        commit(wc);        
    }    

    public void deleteWCRoot() throws Exception {
        // init        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(wc).getTextStatus());

        // delete
        delete(wc);
        
        // test
        assertTrue(!wc.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(wc).getTextStatus());        
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, getStatus(wc));        
    }

    public void deleteVersionedFileTree() throws Exception {
        // init
        File folder = new File(wc, "folder");
        folder.mkdirs();
        File folder1 = new File(folder, "folder1");
        folder1.mkdirs();
        File folder2 = new File(folder, "folder2");
        folder2.mkdirs();        
        File file11 = new File(folder1, "file1");
        file11.createNewFile();
        File file12 = new File(folder1, "file2");
        file12.createNewFile();
        File file21 = new File(folder2, "file1");
        file21.createNewFile();
        File file22 = new File(folder2, "file2");
        file22.createNewFile();
        
        commit(wc);      
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(folder).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(folder1).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(folder2).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file11).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file12).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file21).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file22).getTextStatus());

        // delete
        delete(folder);
        
        // test
        assertTrue(folder.exists());
        assertTrue(folder1.exists());
        assertTrue(folder2.exists());
        assertFalse(file11.exists());
        assertFalse(file12.exists());
        assertFalse(file21.exists());
        assertFalse(file22.exists());
        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(folder).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(folder1).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(folder2).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(file11).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(file12).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(file21).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(file22).getTextStatus());                
        
        assertCachedStatus(folder, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);        
        assertCachedStatus(folder1, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);        
        assertCachedStatus(folder2, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(file11));        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(file12));        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(file21));        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(file22));        
        
        
        commit(wc);
        assertFalse(folder.exists());
        assertFalse(folder1.exists());
        assertFalse(folder2.exists());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());                
    }

    public void deleteNotVersionedFileTree() throws Exception {
        // init
        File folder = new File(wc, "folder");
        folder.mkdirs();
        File folder1 = new File(folder, "folder1");
        folder1.mkdirs();
        File folder2 = new File(folder, "folder2");
        folder2.mkdirs();        
        File file11 = new File(folder1, "file1");
        file11.createNewFile();
        File file12 = new File(folder1, "file2");
        file12.createNewFile();
        File file21 = new File(folder2, "file1");
        file21.createNewFile();
        File file22 = new File(folder2, "file2");
        file22.createNewFile();
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder1).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder2).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file11).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file12).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file21).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file22).getTextStatus());

        // delete
        delete(folder);
        
        // test
        assertFalse(folder.exists());
        assertFalse(folder1.exists());
        assertFalse(folder2.exists());
        assertFalse(file11.exists());
        assertFalse(file12.exists());
        assertFalse(file21.exists());
        assertFalse(file22.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder1).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder2).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file11).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file12).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file21).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file22).getTextStatus());
        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(folder));        
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, getStatus(folder1));        
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, getStatus(folder2));        
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, getStatus(file11));        
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, getStatus(file12));        
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, getStatus(file21));        
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, getStatus(file22));        
        
        commit(wc);
        assertFalse(folder.exists());
        assertFalse(folder1.exists());
        assertFalse(folder2.exists());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());        
    }

    public void createNewFile() throws Exception {
        // init
        File file = new File(wc, "file");
        
        // create
        FileObject fo = FileUtil.toFileObject(wc);
        fo.createData(file.getName());
                                        
        // test 
        assertTrue(file.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file).getTextStatus());        
        assertCachedStatus(file, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
    }

    public void createNewFolder() throws Exception {
        // init
        File folder = new File(wc, "folder");
        
        // create
        FileObject fo = FileUtil.toFileObject(wc);
        fo.createFolder(folder.getName());
                                        
        // test 
        assertTrue(folder.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());        
        assertCachedStatus(folder, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
    }
    
    public void deleteA_CreateA() throws IOException, SVNClientException {
        
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();        
        commit(wc);        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        
        // delete                
        FileObject fo = FileUtil.toFileObject(fileA);
        fo.delete();

        // test if deleted
        assertFalse(fileA.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());

        // create        
        fo.getParent().createData(fo.getName());       
        
        // test 
        assertTrue(fileA.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        
    }

    public void deleteA_CreateA_RunAtomic() throws IOException, SVNClientException {
        // init
        final File fileA = new File(wc, "A");
        fileA.createNewFile();        
        commit(wc);        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        
        final FileObject fo = FileUtil.toFileObject(fileA);
        AtomicAction a = new AtomicAction() {
            public void run() throws IOException {             
                fo.delete();
                fo.getParent().createData(fo.getName());
            }
        };
        fo.getFileSystem().runAtomicAction(a);        
        
        waitALittleBit(500); // after create 
        
        // test 
        assertTrue(fileA.exists());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
    }    
    
    public void renameVersionedFile_DO() throws Exception {
        // init
        File fromFile = new File(wc, "fromFile");
        fromFile.createNewFile();
        commit(wc);                       
        File toFile = new File(wc, "toFile");
        
        // rename    
        renameDO(fromFile, toFile);
        
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile).getTextStatus());
        
        assertCachedStatus(fromFile, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(toFile, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        
        commit(wc);
    }

    public void moveVersionedFile_DO() throws Exception {
        // init
        File fromFile = new File(wc, "file");
        fromFile.createNewFile();
        File toFolder = new File(wc, "toFolder");
        toFolder.mkdirs();
        commit(wc);
        File toFile = new File(toFolder, fromFile.getName());

        // move
        moveDO(fromFile, toFile);

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile).getTextStatus());

        assertCachedStatus(fromFile, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(toFile, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        commit(wc);
    }

    public void moveVersionedFile2Repos_DO() throws Exception {
        // init
        File fromFolder = new File(wc, "folder");
        fromFolder.mkdirs();
        File toFolder = new File(wc2, "toFolder");
        toFolder.mkdirs();
        commit(wc2);
        File fromFile = new File(fromFolder, "file");
        fromFile.createNewFile();
        commit(wc);
        File toFile = new File(toFolder, "file");
        // move
        moveDO(fromFile, toFile);

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());

        assertCachedStatus(fromFile, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);

        commit(wc);
        commit(wc2);
    }

    public void moveVersionedFolder2Repos_DO() throws Exception {
        // init
        File fromFolder = new File(wc, "folder");
        fromFolder.mkdirs();
        File toFolderParent = new File(wc2, "folderParent");
        toFolderParent.mkdirs();
        File toFolder = new File(toFolderParent, fromFolder.getName());
        File toFile = new File(toFolder, "file");
        commit(wc2);
        File fromFile = new File(fromFolder, toFile.getName());
        fromFile.createNewFile();
        commit(wc);
        // move
        moveDO(fromFolder, toFolder);

        // test
        assertTrue(fromFolder.exists()); // TODO later delete from folder
        assertFalse(fromFile.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());

        assertCachedStatus(fromFile, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder));
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        commit(wc);
        commit(wc2);
    }

    public void moveFileTree2Repos_DO() throws Exception {
        // init
        File fromFolder = new File(wc, "folder");
        fromFolder.mkdirs();
        File fromFolder1 = new File(fromFolder, "folder1");
        fromFolder1.mkdirs();
        File fromFolder2 = new File(fromFolder, "folder2");
        fromFolder2.mkdirs();
        File fromFile11 = new File(fromFolder1, "file11");
        fromFile11.createNewFile();
        File fromFile12 = new File(fromFolder1, "file12");
        fromFile12.createNewFile();
        File fromFile21 = new File(fromFolder2, "file21");
        fromFile21.createNewFile();
        File fromFile22 = new File(fromFolder2, "file22");
        fromFile22.createNewFile();

        File toFolderParent = new File(wc2, "toFolder");
        toFolderParent.mkdirs();
        File toFolder = new File(toFolderParent, fromFolder.getName());
        commit(wc);
        commit(wc2);

        // move
        moveDO(fromFolder, toFolder);

//        // test         t.
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        File toFolder1 = new File(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        File toFolder2 = new File(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        File toFile11 = new File(toFolder1, "file11");
        assertTrue(toFile11.exists());
        File toFile12 = new File(toFolder1, "file12");
        assertTrue(toFile12.exists());
        File toFile21 = new File(toFolder2, "file21");
        assertTrue(toFile21.exists());
        File toFile22 = new File(toFolder2, "file22");
        assertTrue(toFile22.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder1).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder2).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile11).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile12).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile21).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile22).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder1).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder2).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile11).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile12).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile21).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile22).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder1));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder2));
        assertCachedStatus(fromFile11, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(fromFile12, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(fromFile21, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(fromFile22, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFolder1, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFolder2, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFile11, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertCachedStatus(toFile12, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertCachedStatus(toFile21, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertCachedStatus(toFile22, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);

        commit(wc);
        commit(wc2);

        assertTrue(fromFolder.exists());
        assertTrue(fromFolder1.exists());
        assertTrue(fromFolder2.exists());
        assertFalse(fromFile11.exists());
        assertFalse(fromFile12.exists());
        assertFalse(fromFile21.exists());
        assertFalse(fromFile22.exists());

    }

    public void moveVersionedFile2Repos_FO() throws Exception {
        // init
        File fromFolder = new File(wc, "folder");
        fromFolder.mkdirs();
        File toFolder = new File(wc2, "toFolder");
        toFolder.mkdirs();
        commit(wc2);
        File fromFile = new File(fromFolder, "file");
        fromFile.createNewFile();
        commit(wc);
        File toFile = new File(toFolder, "file");
        // move
        moveFO(fromFile, toFile);

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());

        assertCachedStatus(fromFile, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);

        commit(wc);
        commit(wc2);
    }

    public void moveVersionedFolder2Repos_FO() throws Exception {
        // init
        File fromFolder = new File(wc, "folder");
        fromFolder.mkdirs();
        File toFolderParent = new File(wc2, "folderParent");
        toFolderParent.mkdirs();
        File toFolder = new File(toFolderParent, fromFolder.getName());
        File toFile = new File(toFolder, "file");
        commit(wc2);
        File fromFile = new File(fromFolder, toFile.getName());
        fromFile.createNewFile();
        commit(wc);
        // move
        moveFO(fromFolder, toFolder);

        // test
        assertTrue(fromFolder.exists()); // TODO later delete from folder
        assertFalse(fromFile.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder));
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        commit(wc);
        commit(wc2);
    }

    public void moveFileTree2Repos_FO() throws Exception {
        // init
        File fromFolder = new File(wc, "folder");
        fromFolder.mkdirs();
        File fromFolder1 = new File(fromFolder, "folder1");
        fromFolder1.mkdirs();
        File fromFolder2 = new File(fromFolder, "folder2");
        fromFolder2.mkdirs();
        File fromFile11 = new File(fromFolder1, "file11");
        fromFile11.createNewFile();
        File fromFile12 = new File(fromFolder1, "file12");
        fromFile12.createNewFile();
        File fromFile21 = new File(fromFolder2, "file21");
        fromFile21.createNewFile();
        File fromFile22 = new File(fromFolder2, "file22");
        fromFile22.createNewFile();

        File toFolderParent = new File(wc2, "toFolder");
        toFolderParent.mkdirs();
        File toFolder = new File(toFolderParent, fromFolder.getName());
        commit(wc);
        commit(wc2);

        // move
        moveFO(fromFolder, toFolder);

//        // test         t.
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        File toFolder1 = new File(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        File toFolder2 = new File(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        File toFile11 = new File(toFolder1, "file11");
        assertTrue(toFile11.exists());
        File toFile12 = new File(toFolder1, "file12");
        assertTrue(toFile12.exists());
        File toFile21 = new File(toFolder2, "file21");
        assertTrue(toFile21.exists());
        File toFile22 = new File(toFolder2, "file22");
        assertTrue(toFile22.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder1).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder2).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile11).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile12).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile21).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile22).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder1).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder2).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile11).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile12).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile21).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile22).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder1));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder2));
        assertCachedStatus(fromFile11, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(fromFile12, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(fromFile21, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(fromFile22, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFolder1, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFolder2, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFile11, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertCachedStatus(toFile12, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertCachedStatus(toFile21, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertCachedStatus(toFile22, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);

        commit(wc);
        commit(wc2);

        assertTrue(fromFolder.exists());
        assertTrue(fromFolder1.exists());
        assertTrue(fromFolder2.exists());
        assertFalse(fromFile11.exists());
        assertFalse(fromFile12.exists());
        assertFalse(fromFile21.exists());
        assertFalse(fromFile22.exists());

    }
    
    public void renameUnversionedFile_DO() throws Exception {
        // init
        File fromFile = new File(wc, "fromFile");
        fromFile.createNewFile();
        File toFile = new File(wc, "toFile");
                
        // rename
        renameDO(fromFile, toFile);
        
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFile).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());        
              
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFile));                
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        
        commit(wc);
    }
    
    public void moveUnversionedFile_DO() throws Exception {
        // init
        File fromFile = new File(wc, "file");
        fromFile.createNewFile();
        File toFolder = new File(wc, "toFolder");
        toFolder.mkdirs();
        
        File toFile = new File(toFolder, fromFile.getName());
        
        // rename
        moveDO(fromFile, toFile);
        
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFile).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());        
              
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFile));                
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        
        commit(wc);
    }

    public void renameUnversionedFolder_DO() throws Exception {
        // init
        File fromFolder = new File(wc, "fromFolder");
        fromFolder.mkdirs();
        File toFolder = new File(wc, "toFolder");
        
        // rename
        renameDO(fromFolder, toFolder);
        
        // test 
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFolder).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFolder).getTextStatus());        
              
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFolder));                
        assertCachedStatus(toFolder, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        
        commit(wc);
    }
    
    public void moveUnversionedFolder_DO() throws Exception {
        // init
        File fromFolder = new File(wc, "folder");
        fromFolder.mkdirs();
        File toParent = new File(wc, "toFolder");
        toParent.mkdirs();
        File toFolder = new File(toParent, fromFolder.getName());
        
        // move        
        moveDO(fromFolder, toFolder);
        
        // test 
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFolder).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFolder).getTextStatus());        
              
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFolder));                
        assertCachedStatus(toFolder, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        
        commit(wc);
    }
    
    
    public void renameAddedFile_DO() throws Exception {
        // init        
        File fromFile = new File(wc, "fromFile");
        fromFile.createNewFile();
        File toFile = new File(wc, "toFile");
        
        // add
        getClient().addFile(fromFile);                
        
        // rename
        renameDO(fromFile, toFile);
        
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());        

        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFile));
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        
        commit(wc);
    }
    
    public void moveAddedFile2UnversionedFolder_DO() throws Exception {
        // init        
        File fromFile = new File(wc, "file");
        fromFile.createNewFile();
        File toFolder = new File(wc, "toFodler");
        toFolder.mkdirs();
        
        File toFile = new File(toFolder, fromFile.getName());
        
        // add
        getClient().addFile(fromFile);                
        
        // move
        moveDO(fromFile, toFile);
                
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFile).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());        
        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFile));                
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        
        commit(wc);
    }
    
    public void moveAddedFile2VersionedFolder_DO() throws Exception {
        // init        
        File toFolder = new File(wc, "toFodler");
        toFolder.mkdirs();
        commit(wc);
        File fromFile = new File(wc, "fromFile");
        fromFile.createNewFile();        
        
        File toFile = new File(toFolder, fromFile.getName());  
        
        // add
        getClient().addFile(fromFile);                
        
        // rename
        moveDO(fromFile, toFile);
        
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFile).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());        
        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFile));                
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        
        commit(wc);
    }

    public void renameA2B2A_DO() throws Exception {
        // init
        File fileA = new File(wc, "from");
        fileA.createNewFile();
        commit(wc);  
        
        File fileB = new File(wc, "to");
        
        // rename
        renameDO(fileA, fileB);
        renameDO(fileB, fileA);
        
        // test 
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());        
              
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));                
        
        commit(wc);        
    }
    
    public void moveA2B2A_DO() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        File folder = new File(wc, "folder");
        folder.mkdirs();        
        commit(wc);  
        
        File fileB = new File(folder, fileA.getName());
        
        // move
        moveDO(fileA, fileB);
        Thread.sleep(500);
        moveDO(fileB, fileA);
        
        // test 
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());        
              
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));                
        
        commit(wc);
    }
    
    public void renameA2B2C_DO() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();        
        commit(wc);  
        
        File fileB = new File(wc, "B");
        File fileC = new File(wc, "C");

        // rename
        renameDO(fileA, fileB);
        renameDO(fileB, fileC);
        
        // test 
        assertFalse(fileA.exists());
        assertFalse(fileB.exists());
        assertTrue(fileC.exists());
        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileC).getTextStatus());
        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));                
        assertCachedStatus(fileC, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        
        commit(wc);
    }
    
    public void moveA2B2C_DO() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        File folderB = new File(wc, "folderB");
        folderB.mkdirs();
        File folderC = new File(wc, "folderC");
        folderC.mkdirs();
        commit(wc);  
        
        File fileB = new File(folderB, fileA.getName());
        File fileC = new File(folderC, fileA.getName());
        
        // move
        moveDO(fileA, fileB);
        moveDO(fileB, fileC);
        
        // test 
        assertFalse(fileA.exists());
        assertFalse(fileB.exists());
        assertTrue(fileC.exists());
        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileC).getTextStatus());
        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));                
        assertCachedStatus(fileC, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        
        commit(wc);
    }

    public void renameA2B2C2A_DO() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        commit(wc);  
        
        File fileB = new File(wc, "B");
        File fileC = new File(wc, "C");
        
        // rename 
        renameDO(fileA, fileB);
        renameDO(fileB, fileC);
        renameDO(fileC, fileA);
        
        // test 
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        assertFalse(fileC.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileC).getTextStatus());  
        
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileC));                
        
        commit(wc);
        
    }

    public void moveA2CB2A_DO() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        File folderB = new File(wc, "folderB");
        folderB.mkdirs();
        File fileB = new File(folderB, fileA.getName());
        fileB.createNewFile();
        File folderC = new File(wc, "folderC");
        folderC.mkdirs();
        commit(wc);

        File fileC = new File(folderC, fileA.getName());

        // move
        moveDO(fileA, fileC);
        Thread.sleep(500);
        moveDO(fileB, fileA);

        // test
        assertTrue(fileA.exists());
        assertTrue(fileC.exists());
        assertFalse(fileB.exists());

        assertEquals(SVNStatusKind.REPLACED, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileB).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileC).getTextStatus());

        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileB));
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileC));

        commit(wc);

        // test
        assertTrue(fileA.exists());
        assertTrue(fileC.exists());
        assertFalse(fileB.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileC).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileC));
    }

    public void moveA2CB2A_FO() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        File folderB = new File(wc, "folderB");
        folderB.mkdirs();
        File fileB = new File(folderB, fileA.getName());
        fileB.createNewFile();
        File folderC = new File(wc, "folderC");
        folderC.mkdirs();
        commit(wc);

        File fileC = new File(folderC, fileA.getName());

        // move
        moveFO(fileA, fileC);
        Thread.sleep(500);
        moveFO(fileB, fileA);

        // test
        assertTrue(fileA.exists());
        assertTrue(fileC.exists());
        assertFalse(fileB.exists());

        assertEquals(SVNStatusKind.REPLACED, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileB).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileC).getTextStatus());

        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileB));
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileC));

        commit(wc);

        // test
        assertTrue(fileA.exists());
        assertTrue(fileC.exists());
        assertFalse(fileB.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileC).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileC));
    }

    public void renameA2CB2A_DO() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        File fileB = new File(wc, "B");
        fileB.createNewFile();
        commit(wc);

        File fileC = new File(wc, "C");

        // move
        renameDO(fileA, fileC);
        Thread.sleep(500);
        renameDO(fileB, fileA);

        // test
        assertTrue(fileA.exists());
        assertTrue(fileC.exists());
        assertFalse(fileB.exists());

        assertEquals(SVNStatusKind.REPLACED, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileB).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileC).getTextStatus());

        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileB));
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileC));

        commit(wc);

        // test
        assertTrue(fileA.exists());
        assertTrue(fileC.exists());
        assertFalse(fileB.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileC).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileC));
    }

    public void renameA2CB2A_FO() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        File fileB = new File(wc, "B");
        fileB.createNewFile();
        commit(wc);

        File fileC = new File(wc, "C");

        // move
        renameFO(fileA, fileC);
        Thread.sleep(500);
        renameFO(fileB, fileA);

        // test
        assertTrue(fileA.exists());
        assertTrue(fileC.exists());
        assertFalse(fileB.exists());

        assertEquals(SVNStatusKind.REPLACED, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileB).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileC).getTextStatus());

        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileB));
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileC));

        commit(wc);

        // test
        assertTrue(fileA.exists());
        assertTrue(fileC.exists());
        assertFalse(fileB.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileC).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileC));
    }
    
    public void moveA2B2C2A_DO() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        File folderB = new File(wc, "folderB");
        folderB.mkdirs();
        File folderC = new File(wc, "folderC");
        folderC.mkdirs();
        commit(wc);  
        
        File fileB = new File(folderB, fileA.getName());
        File fileC = new File(folderC, fileA.getName());
        
        // move
        moveDO(fileA, fileB);
        moveDO(fileB, fileC);
        moveDO(fileC, fileA);
        
        // test 
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        assertFalse(fileC.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileC).getTextStatus());  
        
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileC));                
        
        commit(wc);
        
    }        
    
    public void renameA2B_CreateA_DO() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        commit(wc);  
        
        // rename
        File fileB = new File(wc, "B");
        renameDO(fileA, fileB);
        
        // create from file
        FileUtil.toFileObject(fileA.getParentFile()).createData(fileA.getName());
        
        // test 
        assertTrue(fileB.exists());
        assertTrue(fileA.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileB).getTextStatus());        
        
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        assertCachedStatus(fileB, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);                
        
        commit(wc);
    }
    
    public void moveA2B_CreateA_DO() throws Exception {
        // init
        File fileA = new File(wc, "file");
        fileA.createNewFile();
        File folderB = new File(wc, "folderB");
        folderB.mkdirs();
        commit(wc);  
        
        File fileB = new File(folderB, fileA.getName());
        
        // move
        moveDO(fileA, fileB);
        Thread.sleep(500);
        
        // create from file
        FileUtil.toFileObject(fileA.getParentFile()).createData(fileA.getName());
        
        // test 
        assertTrue(fileB.exists());
        assertTrue(fileA.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileB).getTextStatus());        
        
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        assertCachedStatus(fileB, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);                
        
        commit(wc);
    }
    
    // fixed - see issue #129805
    public void deleteA_RenameB2A_DO_129805() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        File fileB = new File(wc, "B");
        fileB.createNewFile();
        commit(wc);  
        
        // delete A
        delete(fileA);
        // rename B to A
        renameDO(fileB, fileA);
        
        // test 
        assertFalse(fileB.exists());
        assertTrue(fileA.exists());
        
        assertEquals(SVNStatusKind.REPLACED, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileB).getTextStatus());
             
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileB));
        
        commit(wc);

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));
    }
    
    public void renameVersionedFolder_DO() throws Exception {
        // init
        File fromFolder = new File(wc, "from");
        fromFolder.mkdirs();
        commit(wc);               
       
        File toFolder = new File(wc, "to");
        
        // rename       
        renameDO(fromFolder, toFolder);
        
        // test 
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());        
        assertCachedStatus(fromFolder, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);                
        commit(wc);
        assertFalse(fromFolder.exists());        
    }

    public void moveVersionedFolder_DO() throws Exception {
        // init
        File fromFolder = new File(wc, "from");
        fromFolder.mkdirs();
        File toParent = new File(wc, "toFolder");
        toParent.mkdirs();
        commit(wc);               
        
        File toFolder = new File(toParent, fromFolder.getName());
        
        // move
        moveDO(fromFolder, toFolder);
        
        // test 
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFolder).getTextStatus());        
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder));                
        assertCachedStatus(toFolder, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        commit(wc);
        assertTrue(fromFolder.exists()); 
    }    
    
    public void renameFileTree_DO() throws Exception {
        // init
        File fromFolder = new File(wc, "from");
        fromFolder.mkdirs();
        File fromFolder1 = new File(fromFolder, "folder1");
        fromFolder1.mkdirs();
        File fromFolder2 = new File(fromFolder, "folder2");
        fromFolder2.mkdirs();
        File fromFile11 = new File(fromFolder1, "file11");
        fromFile11.createNewFile();
        File fromFile12 = new File(fromFolder1, "file12");
        fromFile12.createNewFile();
        File fromFile21 = new File(fromFolder2, "file21");
        fromFile21.createNewFile();
        File fromFile22 = new File(fromFolder2, "file22");
        fromFile22.createNewFile();
        commit(wc);               
        
        // rename
        File toFolder = new File(wc, "to");
        renameDO(fromFolder, toFolder);
                                        
        // test 
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        File toFolder1 = new File(toFolder, "folder1");
        assertTrue(toFolder1.exists());
        File toFolder2 = new File(toFolder, "folder2");
        assertTrue(toFolder2.exists());
        File toFile11 = new File(toFolder1, "file11");
        assertTrue(toFile11.exists());
        File toFile12 = new File(toFolder1, "file12");
        assertTrue(toFile12.exists());
        File toFile21 = new File(toFolder2, "file21");
        assertTrue(toFile21.exists());
        File toFile22 = new File(toFolder2, "file22");
        assertTrue(toFile22.exists());
        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder1).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder2).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile11).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile12).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile21).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile22).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFolder1).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFolder2).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile11).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile12).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile21).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile22).getTextStatus());    
        
        assertCachedStatus(fromFolder, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFolder1, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFolder2, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFile11, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFile12, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFile21, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFile22, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);                
        assertCachedStatus(toFolder1, FileInformation.STATUS_VERSIONED_UPTODATE);                
        assertCachedStatus(toFolder2, FileInformation.STATUS_VERSIONED_UPTODATE);                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile11));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile12));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile21));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile22));   
        
        commit(wc);
        assertFalse(fromFolder.exists());
    }
    
    public void moveFileTree_DO() throws Exception {
        // init
        File fromFolder = new File(wc, "from");
        fromFolder.mkdirs();
        File fromFolder1 = new File(fromFolder, "folder1");
        fromFolder1.mkdirs();
        File fromFolder2 = new File(fromFolder, "folder2");
        fromFolder2.mkdirs();
        File fromFile11 = new File(fromFolder1, "file11");
        fromFile11.createNewFile();
        File fromFile12 = new File(fromFolder1, "file12");
        fromFile12.createNewFile();
        File fromFile21 = new File(fromFolder2, "file21");
        fromFile21.createNewFile();
        File fromFile22 = new File(fromFolder2, "file22");
        fromFile22.createNewFile();

        File toFolderParent = new File(wc, "to");
        toFolderParent.mkdirs();

        commit(wc);

        File toFolder = new File(toFolderParent, fromFolder.getName());

        // move
        moveDO(fromFolder, toFolder);

        // test         t.
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        File toFolder1 = new File(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        File toFolder2 = new File(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        File toFile11 = new File(toFolder1, "file11");
        assertTrue(toFile11.exists());
        File toFile12 = new File(toFolder1, "file12");
        assertTrue(toFile12.exists());
        File toFile21 = new File(toFolder2, "file21");
        assertTrue(toFile21.exists());
        File toFile22 = new File(toFolder2, "file22");
        assertTrue(toFile22.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder1).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder2).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile11).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile12).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile21).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile22).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder1).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder2).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile11).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile12).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile21).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile22).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder1));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder2));
        assertCachedStatus(fromFile11, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(fromFile12, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(fromFile21, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(fromFile22, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFolder1, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFolder2, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFile11, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFile12, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFile21, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFile22, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        commit(wc);

        assertTrue(fromFolder.exists());
        assertTrue(fromFolder1.exists());
        assertTrue(fromFolder2.exists());
        assertFalse(fromFile11.exists());
        assertFalse(fromFile12.exists());
        assertFalse(fromFile21.exists());
        assertFalse(fromFile22.exists());

    }

    public void renameVersionedFile_FO() throws Exception {
        // init
        File fromFile = new File(wc, "fromFile");
        fromFile.createNewFile();
        commit(wc);                       
        File toFile = new File(wc, "toFile");
        
        // rename    
        renameFO(fromFile, toFile);
        
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile).getTextStatus());        
        
        assertCachedStatus(fromFile, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(toFile, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);   
        
        commit(wc);
    }

    public void moveVersionedFile_FO() throws Exception {
        // init
        File fromFile = new File(wc, "file");
        fromFile.createNewFile();
        File toFolder = new File(wc, "toFolder");
        toFolder.mkdirs();
        commit(wc);               
        File toFile = new File(toFolder, fromFile.getName());
        
        // move
        moveFO(fromFile, toFile);
        
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile).getTextStatus());        
        
        assertCachedStatus(fromFile, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(toFile, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);   
        
        commit(wc);
    }
    
    public void renameUnversionedFile_FO() throws Exception {
        // init
        File fromFile = new File(wc, "fromFile");
        fromFile.createNewFile();
        File toFile = new File(wc, "toFile");
                
        // rename
        renameFO(fromFile, toFile);
        
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFile).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());        
              
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFile));                
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        
        commit(wc);
    }
    
    public void moveUnversionedFile_FO() throws Exception {
        // init
        File fromFile = new File(wc, "file");
        fromFile.createNewFile();
        File toFolder = new File(wc, "toFolder");
        toFolder.mkdirs();
        
        File toFile = new File(toFolder, fromFile.getName());
        
        // rename
        moveFO(fromFile, toFile);
        
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFile).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());        
              
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFile));                
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        
        commit(wc);
    }

    public void renameUnversionedFolder_FO() throws Exception {
        // init
        File fromFolder = new File(wc, "fromFolder");
        fromFolder.mkdirs();
        File toFolder = new File(wc, "toFolder");
        
        // rename
        renameFO(fromFolder, toFolder);
        
        // test 
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFolder).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFolder).getTextStatus());        
              
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFolder));                
        assertCachedStatus(toFolder, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        
        commit(wc);
    }
    
    public void moveUnversionedFolder_FO() throws Exception {
        // init
        File fromFolder = new File(wc, "folder");
        fromFolder.mkdirs();
        File toParent = new File(wc, "toFolder");
        toParent.mkdirs();
        File toFolder = new File(toParent, fromFolder.getName());
        
        
        // move        
        IOException e = null;
        try {
            moveFO(fromFolder, toFolder);
        } catch (IOException ex) {
            e = ex;
        }
        // expected to fail 
        // if it should change in the future enble the following tests 
        assertNotNull(e);
        
        // test 
//        assertFalse(fromFolder.exists());
//        assertTrue(toFolder.exists());
//
//        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFolder).getTextStatus());
//        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFolder).getTextStatus());
//
//        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFolder));
//        assertCachedStatus(toFolder, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
//
//        commit(wc);
    }
    
    
    public void renameAddedFile_FO() throws Exception {
        // init        
        File fromFile = new File(wc, "fromFile");
        fromFile.createNewFile();
        File toFile = new File(wc, "toFile");
        
        // add
        getClient().addFile(fromFile);                
        
        // rename
        renameFO(fromFile, toFile);
        
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFile).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());        
        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFile));                
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        
        commit(wc);
    }
    
    public void moveAddedFile2UnversionedFolder_FO() throws Exception {
        // init        
        File fromFile = new File(wc, "file");
        fromFile.createNewFile();
        File toFolder = new File(wc, "toFodler");
        toFolder.mkdirs();
        
        File toFile = new File(toFolder, fromFile.getName());
        
        // add
        getClient().addFile(fromFile);                
        
        // move
        moveFO(fromFile, toFile);
                
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFile).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());        
        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFile));                
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        
        commit(wc);
    }
       
    public void moveAddedFile2VersionedFolder_FO() throws Exception {
        // init        
        File toFolder = new File(wc, "toFodler");
        toFolder.mkdirs();
        commit(wc);
        File fromFile = new File(wc, "fromFile");
        fromFile.createNewFile();        
        
        File toFile = new File(toFolder, fromFile.getName());  
        
        // add
        getClient().addFile(fromFile);                
        
        // rename
        moveFO(fromFile, toFile);
        
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFile).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());        
        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFile));                
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        
        commit(wc);
    }

    public void renameA2B2A_FO() throws Exception {
        // init
        File fileA = new File(wc, "from");
        fileA.createNewFile();
        commit(wc);  
        
        File fileB = new File(wc, "to");
        
        // rename
        renameFO(fileA, fileB);
        renameFO(fileB, fileA);
        
        // test 
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());        
              
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));                
        
        commit(wc);        
    }
    
    public void moveA2B2A_FO() throws Exception {
        // init
        File fileA = new File(wc, "A");
        assertFalse(fileA.exists());
        fileA.createNewFile();
        File folder = new File(wc, "folder");
        assertFalse(folder.exists());
        folder.mkdirs();        
        commit(wc);  
        
        File fileB = new File(folder, fileA.getName());
        assertFalse(fileB.exists());
        
        // move
        moveFO(fileA, fileB);
        Thread.sleep(500);
        moveFO(fileB, fileA);
        
        // test 
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());        
              
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));                
        
        commit(wc);
    }

    public void renameA2B2C_FO() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();        
        commit(wc);  
        
        File fileB = new File(wc, "B");
        File fileC = new File(wc, "C");

        // rename
        renameFO(fileA, fileB);
        renameFO(fileB, fileC);
        
        // test 
        assertFalse(fileA.exists());
        assertFalse(fileB.exists());
        assertTrue(fileC.exists());
        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileC).getTextStatus());
        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));                
        assertCachedStatus(fileC, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        
        commit(wc);
    }
    
    public void moveA2B2C_FO() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        File folderB = new File(wc, "folderB");
        folderB.mkdirs();
        File folderC = new File(wc, "folderC");
        folderC.mkdirs();
        commit(wc);  
        
        File fileB = new File(folderB, fileA.getName());
        File fileC = new File(folderC, fileA.getName());
        
        // move
        moveFO(fileA, fileB);
        moveFO(fileB, fileC);
        
        // test 
        assertFalse(fileA.exists());
        assertFalse(fileB.exists());
        assertTrue(fileC.exists());
        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileC).getTextStatus());
        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));                
        assertCachedStatus(fileC, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        
        commit(wc);
    }

    public void renameA2B2C2A_FO() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        commit(wc);  
        
        File fileB = new File(wc, "B");
        File fileC = new File(wc, "C");
        
        // rename 
        renameFO(fileA, fileB);
        renameFO(fileB, fileC);
        renameFO(fileC, fileA);
        
        // test 
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        assertFalse(fileC.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileC).getTextStatus());  
        
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileC));                
        
        commit(wc);
        
    }        
    
    public void moveA2B2C2A_FO() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        File folderB = new File(wc, "folderB");
        folderB.mkdirs();
        File folderC = new File(wc, "folderC");
        folderC.mkdirs();
        commit(wc);  
        
        File fileB = new File(folderB, fileA.getName());
        File fileC = new File(folderC, fileA.getName());
        
        // move
        moveFO(fileA, fileB);
        moveFO(fileB, fileC);
        moveFO(fileC, fileA);
        
        // test 
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        assertFalse(fileC.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileC).getTextStatus());  
        
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileC));                
        
        commit(wc);
        
    }        
    
    public void renameA2B_CreateA_FO() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        commit(wc);  
        
        // rename
        File fileB = new File(wc, "B");
        renameFO(fileA, fileB);
        
        // create from file
        FileUtil.toFileObject(fileA.getParentFile()).createData(fileA.getName());
        
        // test 
        assertTrue(fileB.exists());
        assertTrue(fileA.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileB).getTextStatus());        
        
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        assertCachedStatus(fileB, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);                
        
        commit(wc);
    }
    
    public void moveA2B_CreateA_FO() throws Exception {
        // init
        File fileA = new File(wc, "file");
        fileA.createNewFile();
        File folderB = new File(wc, "folderB");
        folderB.mkdirs();
        commit(wc);  
        
        File fileB = new File(folderB, fileA.getName());
        
        // move
        moveFO(fileA, fileB);
        Thread.sleep(500);
        
        // create from file
        FileUtil.toFileObject(fileA.getParentFile()).createData(fileA.getName());
        
        // test 
        assertTrue(fileB.exists());
        assertTrue(fileA.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileB).getTextStatus());
        
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        assertCachedStatus(fileB, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);                
        
        commit(wc);
    }
    
    // fixed - see issue #129805
    public void deleteA_RenameB2A_FO_129805() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        File fileB = new File(wc, "B");
        fileB.createNewFile();
        commit(wc);  
        
        // delete A
        delete(fileA);
        // rename B to A
        renameFO(fileB, fileA);
        
        // test 
        assertFalse(fileB.exists());
        assertTrue(fileA.exists());
        
        assertEquals(SVNStatusKind.REPLACED, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileB).getTextStatus());
             
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileB));
        
        commit(wc);

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));
    }
    
    public void renameVersionedFolder_FO() throws Exception {
        // init
        File fromFolder = new File(wc, "from");
        fromFolder.mkdirs();
        commit(wc);               
       
        File toFolder = new File(wc, "to");
        
        // rename       
        renameFO(fromFolder, toFolder);
        
        // test 
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());        
        assertCachedStatus(fromFolder, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);                
        commit(wc);
        assertFalse(fromFolder.exists());        
    }

    public void moveVersionedFolder_FO() throws Exception {
        // init
        File fromFolder = new File(wc, "from");
        fromFolder.mkdirs();
        File toParent = new File(wc, "toFolder");
        toParent.mkdirs();
        commit(wc);               
        
        File toFolder = new File(toParent, fromFolder.getName());
        
        // move
        moveFO(fromFolder, toFolder);
        
        // test 
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());        
        assertCachedStatus(fromFolder, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);                
        commit(wc);
        assertFalse(fromFolder.exists()); 
    }    
    
    public void renameFileTree_FO() throws Exception {
        // init
        File fromFolder = new File(wc, "from");
        fromFolder.mkdirs();
        File fromFolder1 = new File(fromFolder, "folder1");
        fromFolder1.mkdirs();
        File fromFolder2 = new File(fromFolder, "folder2");
        fromFolder2.mkdirs();
        File fromFile11 = new File(fromFolder1, "file11");
        fromFile11.createNewFile();
        File fromFile12 = new File(fromFolder1, "file12");
        fromFile12.createNewFile();
        File fromFile21 = new File(fromFolder2, "file21");
        fromFile21.createNewFile();
        File fromFile22 = new File(fromFolder2, "file22");
        fromFile22.createNewFile();
        commit(wc);               
        
        // rename
        File toFolder = new File(wc, "to");
        renameFO(fromFolder, toFolder);
                                        
        // test 
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        File toFolder1 = new File(toFolder, "folder1");
        assertTrue(toFolder1.exists());
        File toFolder2 = new File(toFolder, "folder2");
        assertTrue(toFolder2.exists());
        File toFile11 = new File(toFolder1, "file11");
        assertTrue(toFile11.exists());
        File toFile12 = new File(toFolder1, "file12");
        assertTrue(toFile12.exists());
        File toFile21 = new File(toFolder2, "file21");
        assertTrue(toFile21.exists());
        File toFile22 = new File(toFolder2, "file22");
        assertTrue(toFile22.exists());
        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder1).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder2).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile11).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile12).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile21).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile22).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFolder1).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFolder2).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile11).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile12).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile21).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile22).getTextStatus());    
        
        assertCachedStatus(fromFolder, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFolder1, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFolder2, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFile11, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFile12, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFile21, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFile22, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);                
        assertCachedStatus(toFolder1, FileInformation.STATUS_VERSIONED_UPTODATE);                
        assertCachedStatus(toFolder2, FileInformation.STATUS_VERSIONED_UPTODATE);                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile11));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile11));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile11));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile11));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile12));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile21));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile22));   
        
        commit(wc);
        assertFalse(fromFolder.exists());
    }
    
    public void moveFileTree_FO() throws Exception {
        // init
        File fromFolder = new File(wc, "from");
        fromFolder.mkdirs();
        File fromFolder1 = new File(fromFolder, "folder1");
        fromFolder1.mkdirs();
        File fromFolder2 = new File(fromFolder, "folder2");
        fromFolder2.mkdirs();
        File fromFile11 = new File(fromFolder1, "file11");
        fromFile11.createNewFile();
        File fromFile12 = new File(fromFolder1, "file12");
        fromFile12.createNewFile();
        File fromFile21 = new File(fromFolder2, "file21");
        fromFile21.createNewFile();
        File fromFile22 = new File(fromFolder2, "file22");
        fromFile22.createNewFile();
        
        File toFolderParent = new File(wc, "to");
        toFolderParent.mkdirs();
        
        commit(wc);               
        
        File toFolder = new File(toFolderParent, fromFolder.getName());
        
        // move
        moveFO(fromFolder, toFolder);
                                                
        // test         t.
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        File toFolder1 = new File(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        File toFolder2 = new File(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        File toFile11 = new File(toFolder1, "file11");
        assertTrue(toFile11.exists());
        File toFile12 = new File(toFolder1, "file12");
        assertTrue(toFile12.exists());
        File toFile21 = new File(toFolder2, "file21");
        assertTrue(toFile21.exists());
        File toFile22 = new File(toFolder2, "file22");
        assertTrue(toFile22.exists());
        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder1).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder2).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile11).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile12).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile21).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile22).getTextStatus());        
        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFolder1).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFolder2).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile11).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile12).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile21).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile22).getTextStatus());    
        
        assertCachedStatus(fromFolder, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFolder1, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFolder2, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFile11, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFile12, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFile21, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFile22, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);     
        
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);                
        assertCachedStatus(toFolder1, FileInformation.STATUS_VERSIONED_UPTODATE);                
        assertCachedStatus(toFolder2, FileInformation.STATUS_VERSIONED_UPTODATE);                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile11));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile12));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile21));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile22));                
        
        commit(wc);
        assertFalse(fromFolder.exists());
    }

    public void deleteCreateChangeCase_issue_157373 () throws Exception {
        // init
        final File fileA = new File(wc, "file");
        FileUtil.toFileObject(wc).createData(fileA.getName());
        assertCachedStatus(fileA, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        commit(wc);
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));

        // rename
        fileA.delete();
        Handler h = new SVNInterceptor();
        Subversion.LOG.addHandler(h);
        RequestProcessor.Task r = Subversion.getInstance().getParallelRequestProcessor().create(new Runnable() {
            public void run() {
                FileUtil.refreshFor(fileA);
            }
        });
        r.run();
        assertFalse(fileA.exists());
        final File fileB = new File(wc, fileA.getName().toUpperCase());
        fileB.createNewFile();
        Thread.sleep(3000);
        assertTrue(fileB.exists());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getStatus(fileB));
        Subversion.LOG.removeHandler(h);
    }

    class SVNInterceptor extends Handler {
        public void publish(LogRecord rec) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public void flush() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void close() throws SecurityException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
    protected void commit(File folder) throws SVNClientException {
        TestKit.commit(folder);
    }

    protected void add(File file) throws SVNClientException {
        TestKit.add(file);
    }
    
    private void cleanUpRepo() throws SVNClientException {
        ISVNClientAdapter client = getClient();
        ISVNDirEntry[] entries = client.getList(repoUrl, SVNRevision.HEAD, false);
        SVNUrl[] urls = new SVNUrl[entries.length];
        for (int i = 0; i < entries.length; i++) {
            urls[i] = repoUrl.appendPath(entries[i].getPath());            
        }        
        client.remove(urls, "cleanup");
    }

    private void cleanUpWC(File wc) throws IOException {
        if(wc.exists()) {
            File[] files = wc.listFiles();
            if(files != null) {
                for (File file : files) {
                    if(!file.getName().equals("cache")) { // do not delete the cache
                        FileObject fo = FileUtil.toFileObject(file);
                        if (fo != null) {
                            fo.delete();
                        }
                    }
                }
            }
        }
    }

    private void assertStatus(SVNStatusKind status, File wc) throws SVNClientException {
        ISVNStatus[] values = getClient().getStatus(new File[]{wc});
        for (ISVNStatus iSVNStatus : values) {
            assertEquals(status, iSVNStatus.getTextStatus());
        }
    }
 
    private ISVNStatus getSVNStatus(File file) throws SVNClientException {
        return TestKit.getSVNStatus(file);
    }
    
    private ISVNClientAdapter getClient() throws SVNClientException  {
        return TestKit.getClient();
    }   
    
    private void assertCachedStatus(File file, int expectedStatus) throws Exception {
        assert !file.isFile() || expectedStatus != FileInformation.STATUS_VERSIONED_UPTODATE : "doesn't work for dirs with FileInformation.STATUS_VERSIONED_UPTODATE. Use getStatus instead";
        int status = getCachedStatus(file, expectedStatus);
        assertEquals(expectedStatus, status);
    }        

    private int getCachedStatus(File file, int exceptedStatus) throws Exception, InterruptedException {
        FileInformation info = null;
        for (int i = 0; i < 600; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                throw ex;
            }
            info = cache.getCachedStatus(file);
            if (info != null && info.getStatus() == exceptedStatus) {
                break;
            }            
        }
        if (info == null) {
            throw new Exception("Cache timeout!");
        }
        return info.getStatus();
    }
    
    private int getStatus(File file) {
        return cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN).getStatus();
    }
    
    private void initRepo() throws MalformedURLException, IOException, InterruptedException, SVNClientException {        
        TestKit.initRepo(repoDir, wc);
        TestKit.initRepo(repo2Dir, wc);
    }
    
    private void svnimport() throws SVNClientException, MalformedURLException {
        TestKit.svnimport(repoDir, wc);
        TestKit.svnimport(repo2Dir, wc2);
    }        
    
    private void delete(File file) throws IOException {
        DataObject dao = DataObject.find(FileUtil.toFileObject(file));    
        dao.delete();   
    }   
    
    private void waitALittleBit(long t) {
        try {
            Thread.sleep(t);  
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private boolean isMetadata(File file) {
        return TestKit.isMetadata(file);
    }
    
    private void renameDO(File from, File to) throws DataObjectNotFoundException, IOException {
        DataObject daoFrom = DataObject.find(FileUtil.toFileObject(from));                
        daoFrom.rename(to.getName());               
    }
    
    private void renameFO(File from, File to) throws DataObjectNotFoundException, IOException {
        FileObject foFrom = FileUtil.toFileObject(from);
        FileLock lock = foFrom.lock();
        try {
            foFrom.rename(lock, to.getName(), null);
        } finally {
            lock.releaseLock();
        }
    }
    
    private void moveDO(File from, File to) throws DataObjectNotFoundException, IOException {
        DataObject daoFrom = DataObject.find(FileUtil.toFileObject(from));    
        DataObject daoTarget = DataObject.find(FileUtil.toFileObject(to.getParentFile()));    
        daoFrom.move((DataFolder) daoTarget);    
    }
    
    private void moveFO(File from, File to) throws DataObjectNotFoundException, IOException {
        FileObject foFrom = FileUtil.toFileObject(from);
        assertNotNull(foFrom);
        FileObject foTarget = FileUtil.toFileObject(to.getParentFile());
        assertNotNull(foTarget);
        FileLock lock = foFrom.lock();
        try {
            foFrom.move(lock, foTarget, to.getName(), null);
        } finally {
            lock.releaseLock();
        }        
    }
    
}
