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
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.tigris.subversion.svnclientadapter.commandline.CmdLineClientAdapterFactory;

/**
 *
 * @author Tomas Stupka
 */
public class InteceptorTest extends NbTestCase {
   
    private File dataRootDir;
    private FileStatusCache cache;
    private SVNUrl repoUrl;
        
    public InteceptorTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {          
        super.setUp();
        dataRootDir = new File(System.getProperty("data.root.dir")); 
        cleanUpWC();
        initRepo();      
        System.setProperty("netbeans.user", System.getProperty("data.root.dir"));
        try {
            CmdLineClientAdapterFactory.setup13(null);                        
        } catch (SVNClientException ex) {
            Exceptions.printStackTrace(ex);
        }        
        cache = Subversion.getInstance().getStatusCache();          
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testDeleteNotVersionedFile() throws Exception {
        // init
        File wc = new File(dataRootDir, "testDeleteNotVersionedFile_wc");
        wc.mkdirs();        
        svnimport(wc);           
        File file = new File(wc, "file");
        file.createNewFile();             
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file).getTextStatus());

        // delete
        delete(file);
        
        // test
        assertFalse(file.exists());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file).getTextStatus());
        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(file));
        
        commitAndAssertNormalStatus(wc);        
    }

    public void testDeleteVersionedFile() throws Exception {
        // init
        File wc = new File(dataRootDir, "testDeleteVersionedFile_wc");
        wc.mkdirs();        
        File file = new File(wc, "file");
        file.createNewFile();        
        svnimport(wc);        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file).getTextStatus());

        // delete
        delete(file);
        
        // test
        assertFalse(file.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(file).getTextStatus());
        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCachedStatus(file));
        
        commitAndAssertNormalStatus(wc);
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file).getTextStatus());        
    }

    public void testDeleteVersionedFolder() throws Exception {
        // init
        File wc = new File(dataRootDir, "testDeleteVersionedFolder_wc");
        wc.mkdirs();        
        File folder = new File(wc, "folder");
        folder.mkdirs();
        svnimport(wc);      
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(folder).getTextStatus());

        // delete
        delete(folder);
        
        // test
        assertTrue(folder.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(folder).getTextStatus());
        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCachedStatus(folder));        
        
        commitAndAssertNormalStatus(wc);
        
        assertFalse(folder.exists());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());        
    }

    public void testDeleteNotVersionedFolder() throws IOException, SVNClientException {
        // init
        File wc = new File(dataRootDir, "testDeleteNotVersionedFolder_wc");
        wc.mkdirs();        
        svnimport(wc);              
        File folder = new File(wc, "folder");
        folder.mkdirs();
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());

        // delete
        delete(folder);
        
        // test
        assertFalse(folder.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(folder));
        
        commitAndAssertNormalStatus(wc);        
    }    
    
    public void testDeleteWCRoot() throws Exception {
        // init
        File folder = new File(dataRootDir, "testDeleteWCRoot_wc");
        folder.mkdirs();                
        svnimport(folder);        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(folder).getTextStatus());

        // delete
        delete(folder);
        
        // test
        assertTrue(!folder.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());        
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, getStatus(folder));        
    }

    public void testDeleteVersionedFileTree() throws Exception {
        // init
        File wc = new File(dataRootDir, "testDeleteVersionedFileTree_wc");
        wc.mkdirs();        
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
        
        svnimport(wc);      
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
        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCachedStatus(folder));        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCachedStatus(folder1));        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCachedStatus(folder2));        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(file11));        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(file12));        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(file21));        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(file22));        
        
        
        commitAndAssertNormalStatus(wc);
        assertFalse(folder.exists());
        assertFalse(folder1.exists());
        assertFalse(folder2.exists());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());                
    }
    
    public void testDeleteNotVersionedFileTree() throws Exception {
        // init
        File wc = new File(dataRootDir, "testDeleteNotVersionedFileTree_wc");
        wc.mkdirs();        
        svnimport(wc);
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
        
        commitAndAssertNormalStatus(wc);
        assertFalse(folder.exists());
        assertFalse(folder1.exists());
        assertFalse(folder2.exists());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());        
    }

    public void testCreateNewFile() throws Exception {
        // init
        File wc = new File(dataRootDir, "testCreateNewFile_wc");
        wc.mkdirs();        
        svnimport(wc);               

        File file = new File(wc, "file");
        
        // create
        FileObject fo = FileUtil.toFileObject(wc);
        fo.createData(file.getName());
                                        
        // test 
        assertTrue(file.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file).getTextStatus());        
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getCachedStatus(file));                
    }

    public void testCreateNewFolder() throws Exception {
        // init
        File wc = new File(dataRootDir, "testCreateNewFolder_wc");
        wc.mkdirs();        
        svnimport(wc);               

        File folder = new File(wc, "folder");
        
        // create
        FileObject fo = FileUtil.toFileObject(wc);
        fo.createFolder(folder.getName());
                                        
        // test 
        assertTrue(folder.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());        
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getCachedStatus(folder));                
    }
    
    public void testRecreateDeletedFile() throws IOException, SVNClientException {
        // init
        File wc = new File(dataRootDir, "testRecreateDeletedFile_wc");
        wc.mkdirs();        
        File file = new File(wc, "file");
        file.createNewFile();        
        svnimport(wc);        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file).getTextStatus());        

        // delete
        FileObject fo = FileUtil.toFileObject(file);
        fo.delete();
                
        // test if deleted
        assertFalse(file.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(file).getTextStatus());
        
        // create        
        fo.getParent().createData(fo.getName());
        
        // test 
        assertTrue(file.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file).getTextStatus());        
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(file));                
    }

    public void testRenameFile() throws Exception {
        // init
        File wc = new File(dataRootDir, "testRenameFile_wc");
        wc.mkdirs();        
        File fromFile = new File(wc, "from");
        fromFile.createNewFile();
        svnimport(wc);               
        
        // rename
        File toFile = new File(wc, "to");
        move(fromFile, toFile);
                                        
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile).getTextStatus());        
        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCachedStatus(fromFile));                
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getCachedStatus(toFile));   
        
        commitAndAssertNormalStatus(wc);
    }
    
    public void testRenameUnversionedFile() throws Exception {
        // init
        File wc = new File(dataRootDir, "testRenameUnversionedFile_wc");
        wc.mkdirs();        
        svnimport(wc);               
        File fromFile = new File(wc, "from");
        fromFile.createNewFile();
        
        // rename
        File toFile = new File(wc, "to");
        move(fromFile, toFile);
                                        
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFile).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());        
              
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFile));                
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getCachedStatus(toFile));                
        
        commitAndAssertNormalStatus(wc);
    }
    
    public void testRenameAddedFile() throws Exception {
        // init
        File wc = new File(dataRootDir, "testRenameAddedFile_wc");
        wc.mkdirs();        
        svnimport(wc);               
        File fromFile = new File(wc, "from");
        fromFile.createNewFile();
        
        // add
        getClient().addFile(fromFile);
        
        // rename
        File toFile = new File(wc, "to");
        move(fromFile, toFile);
                                        
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFile).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());        
        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFile));                
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getCachedStatus(toFile));                
        
        commitAndAssertNormalStatus(wc);
    }

    public void testRenameA2B_B2A() throws IOException, SVNClientException {
        // init
        File wc = new File(dataRootDir, "testRenameA2B_B2A_wc");
        wc.mkdirs();                     
        File fileA = new File(wc, "from");
        fileA.createNewFile();
        svnimport(wc);  
        
        // rename
        File fileB = new File(wc, "to");
        move(fileA, fileB);
        // rename back
        move(fileB, fileA);
        
        // test 
        assertFalse(fileB.exists());
        assertTrue(fileA.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());        
              
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));                
        
        commitAndAssertNormalStatus(wc);
    }

    public void testRenameA2B_B2C() throws Exception {
        // init
        File wc = new File(dataRootDir, "testRenameA2B_B2C_wc");
        wc.mkdirs();                     
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        svnimport(wc);  
        
        // rename
        File fileB = new File(wc, "B");
        move(fileA, fileB);
        File fileC = new File(wc, "C");
        move(fileB, fileC);
        
        // test 
        assertFalse(fileA.exists());
        assertFalse(fileB.exists());
        assertTrue(fileC.exists());
        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileC).getTextStatus());        
        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));                
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getCachedStatus(fileC));    
        
        commitAndAssertNormalStatus(wc);
    }

    public void testRenameA2B_B2C_C2A() throws IOException, SVNClientException {
        // init
        File wc = new File(dataRootDir, "testRenameA2B_B2C_C2A_wc");
        wc.mkdirs();                     
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        svnimport(wc);  
        
        // rename A to B
        File fileB = new File(wc, "B");
        move(fileA, fileB);
        // rename B to C
        File fileC = new File(wc, "C");
        move(fileB, fileC);
        // rename C to A
        move(fileC, fileA);
        
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
        
        commitAndAssertNormalStatus(wc);
    }

    public void testRenameA2B_CreateA() throws Exception {
        // init
        File wc = new File(dataRootDir, "testRenameA2B_CreateA_wc");
        wc.mkdirs();                     
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        svnimport(wc);  
        
        // rename
        File fileB = new File(wc, "B");
        move(fileA, fileB);
        // create from file
        FileUtil.toFileObject(fileA.getParentFile()).createData(fileA.getName());
        
        // test 
        assertTrue(fileB.exists());
        assertTrue(fileA.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileB).getTextStatus());        
        
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getCachedStatus(fileB));                
        
        commitAndAssertNormalStatus(wc);
    }

    // not fixed yet - see issue #129805
    public void testDeleteA_RenameB2A_not_fixed_yet_129805() throws Exception {
        // init
        File wc = new File(dataRootDir, "testDeleteA_RenameB2A_wc");
        wc.mkdirs();                     
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        File fileB = new File(wc, "B");
        fileB.createNewFile();
        svnimport(wc);  
        
        // delete A
        delete(fileA);
        // rename B to A
        move(fileB, fileA);
        
        // test 
        assertFalse(fileB.exists());
        assertTrue(fileA.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());                
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());        
             
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));                
        
        commitAndAssertNormalStatus(wc);
    }
    
    public void testRenameFolder() throws Exception {
        // init
        File wc = new File(dataRootDir, "testRenameFolder_wc");
        wc.mkdirs();        
        File fromFolder = new File(wc, "from");
        fromFolder.mkdirs();
        svnimport(wc);               
        
        // rename
        File toFolder = new File(wc, "to");
        move(fromFolder, toFolder);
                                        
        // test 
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCachedStatus(fromFolder));                
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getCachedStatus(toFolder));                
        commitAndAssertNormalStatus(wc);
        assertFalse(fromFolder.exists());
    }

    
    public void testRenameFileTree() throws Exception {
        // init
        File wc = new File(dataRootDir, "testRenameFileTree_wc");
        wc.mkdirs();        
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
        svnimport(wc);               
        
        // rename
        File toFolder = new File(wc, "to");
        move(fromFolder, toFolder);
                                        
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
        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCachedStatus(fromFolder));                
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCachedStatus(fromFolder1));                
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCachedStatus(fromFolder2));                
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCachedStatus(fromFile11));                
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCachedStatus(fromFile12));                
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCachedStatus(fromFile21));                
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCachedStatus(fromFile22));                
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getCachedStatus(toFolder));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getCachedStatus(toFolder1));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getCachedStatus(toFolder2));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getCachedStatus(toFile11));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getCachedStatus(toFile12));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getCachedStatus(toFile21));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getCachedStatus(toFile22));   
        
        commitAndAssertNormalStatus(wc);
        assertFalse(fromFolder.exists());
    }
        
    // XXX add move tests
    
//    public void testRecreateDeletedFolder() throws IOException, SVNClientException {
//        // init
//        File wc = new File(dataRootDir, "wc");
//        wc.mkdirs();        
//        File folder = new File(wc, "folder");
//        folder.mkdirs();
//        svnimport(wc);        
//        assertEquals(SVNStatusKind.NORMAL, getStatus(folder).getTextStatus());        
//
//        // delete
//        FileObject fo = FileUtil.toFileObject(folder);
//        fo.delete();
//                
//        // test if deleted
//        assertTrue(folder.exists());
//        assertEquals(SVNStatusKind.DELETED, getStatus(folder).getTextStatus());
//        
//        // create        
//        fo.getParent().createFolder("folder");
//        
//        // test 
//        assertTrue(folder.exists());
//        assertEquals(SVNStatusKind.NORMAL, getStatus(folder).getTextStatus());        
//    }
    
    private void commitAndAssertNormalStatus(File folder) throws SVNClientException {
        try {
            getClient().commit(new File[]{folder}, "commit", true);
        } catch (SVNClientException e) {
            fail("commit was supposed to work");
        }    
        assertStatus(SVNStatusKind.NORMAL, folder);
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

    private void cleanUpWC() {
        File[] files = dataRootDir.listFiles();
        if(files != null) {
            for (File file : files) {
                Utils.deleteRecursively(file);
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
        return getClient().getSingleStatus(file);        
    }
    
    private ISVNClientAdapter getClient() {
        return SVNClientAdapterFactory.createSVNClient(CmdLineClientAdapterFactory.COMMANDLINE_CLIENT);
    }   
    
    private int getCachedStatus(File file) throws Exception {
        FileInformation info = null;
        for (int i = 0; i < 300; i++) {                        
            try {
                Thread.sleep(100);  
            } catch (InterruptedException ex) {
                throw ex;
            }            
            info = cache.getCachedStatus(file);    
            if(info != null) {
                break;
            }
        }
        if(info == null) {            
            throw new Exception("Cache timeout!");
        }
        int status = info.getStatus();
        return status;
    }

    private int getStatus(File file) {
        return cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN).getStatus();
    }
    
    private int initRepo() throws MalformedURLException, IOException, InterruptedException {
        File repoDir = new File(dataRootDir, "repo");
        repoDir.mkdirs();
        repoUrl = new SVNUrl("file:///" + repoDir.getAbsolutePath());
        String[] cmd = {"svnadmin", "create", repoDir.getAbsolutePath()};
        Process p = Runtime.getRuntime().exec(cmd);
        return p.waitFor();   
    }
    
    private void svnimport(File folder) throws SVNClientException {
        ISVNClientAdapter client = getClient();        
        client.mkdir(repoUrl.appendPath(folder.getName()), "msg");        
        client.checkout(repoUrl.appendPath(folder.getName()), folder, SVNRevision.HEAD, true);        
        File[] files = folder.listFiles();
        if(files != null) {
            for (File file : files) {
                if(!SvnUtils.isAdministrative(file) && !SvnUtils.isPartOfSubversionMetadata(file)) {
                    client.addFile(new File[] {file}, true);   
                }                
            }
            client.commit(new File[] {folder}, "commit", true);                    
        }        
    }
    
    private void delete(File file) throws IOException {
        DataObject dao = DataObject.find(FileUtil.toFileObject(file));    
        dao.delete();        
    }

    private void move(File from, File to) throws IOException {
        FileObject fromFO = FileUtil.toFileObject(from);
        FileObject targetFO = FileUtil.toFileObject(to.getParentFile());
        FileLock lock = fromFO.lock();
        fromFO.move(lock ,targetFO, to.getName(), null);
        lock.releaseLock();
    }
    
    private void waitALittleBit(long t) {
        try {
            Thread.sleep(t);  
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
 
}
