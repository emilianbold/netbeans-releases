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
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
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
    private File wc;
    private File repoDir;
        
    public InteceptorTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {          
        super.setUp();
        dataRootDir = new File(System.getProperty("data.root.dir")); 
        FileUtil.refreshFor(dataRootDir);
        wc = new File(dataRootDir, getName() + "_wc");        
        repoDir = new File(dataRootDir, "repo");
        repoUrl = new SVNUrl("file:///" + repoDir.getAbsolutePath());
        
        System.setProperty("netbeans.user", System.getProperty("data.root.dir") + "/cache");
        cache = Subversion.getInstance().getStatusCache();
        cache.cleanUp();
        
        cleanUpWC();
        initRepo();      
        
        try {
            CmdLineClientAdapterFactory.setup13(null);                        
        } catch (SVNClientException ex) {
            Exceptions.printStackTrace(ex);
        }                        
        wc.mkdirs();        
        svnimport();                   
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        cleanUpWC();        
    }

    public void testDeleteNotVersionedFile() throws Exception {
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

    public void testDeleteVersionedFile() throws Exception {
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
        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCachedStatus(file));
        
        commit(wc);
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file).getTextStatus());        
    }

    public void testDeleteVersionedFolder() throws Exception {
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
        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCachedStatus(folder));        
        
        commit(wc);
        
        assertFalse(folder.exists());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());        
    }

    public void testDeleteNotVersionedFolder() throws IOException, SVNClientException {
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

    public void testDeleteWCRoot() throws Exception {
        // init        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(wc).getTextStatus());

        // delete
        delete(wc);
        
        // test
        assertTrue(!wc.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(wc).getTextStatus());        
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, getStatus(wc));        
    }

    public void testDeleteVersionedFileTree() throws Exception {
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
        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCachedStatus(folder));        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCachedStatus(folder1));        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCachedStatus(folder2));        
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

    public void testDeleteNotVersionedFileTree() throws Exception {
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

    public void testCreateNewFile() throws Exception {
        // init
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
        File folder = new File(wc, "folder");
        
        // create
        FileObject fo = FileUtil.toFileObject(wc);
        fo.createFolder(folder.getName());
                                        
        // test 
        assertTrue(folder.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());        
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getCachedStatus(folder));                
    }
    
    public void testDeleteA_CreateA() throws IOException, SVNClientException {
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

    public void testRenameVersionedFile() throws Exception {
        // init
        File fromFile = new File(wc, "fromFile");
        fromFile.createNewFile();
        commit(wc);                       
        File toFile = new File(wc, "toFile");
        
        // rename        
        mrVersionedFile(new Renamer(fromFile, toFile));
    }

    public void testMoveVersionedFile() throws Exception {
        // init
        File fromFile = new File(wc, "fromFile");
        fromFile.createNewFile();
        File toFolder = new File(wc, "toFolder");
        toFolder.mkdirs();
        commit(wc);               
        
        // rename        
        mrVersionedFile(new Mover(fromFile, toFolder));
    }
    
    public void testRenameUnversionedFile() throws Exception {
        // init
        File fromFile = new File(wc, "fromFile");
        fromFile.createNewFile();
        File toFile = new File(wc, "toFile");
                
        // rename
        mrUnversionedFile(new Renamer(fromFile, toFile));
    }
    
    public void testMoveUnversionedFile() throws Exception {
        // init
        File fromFile = new File(wc, "fromFile");
        fromFile.createNewFile();
        File toFolder = new File(wc, "toFolder");
        toFolder.mkdirs();
        
        // rename
        mrUnversionedFile(new Mover(fromFile, toFolder));
    }

    public void testRenameUnversionedFolder() throws Exception {
        // init
        File fromFolder = new File(wc, "fromFolder");
        fromFolder.mkdirs();
        
        // rename
        File toFolder = new File(wc, "toFolder");
        mrUnversionedFolder(new Renamer(fromFolder, toFolder));
    }
    
    public void testMoveUnversionedFolder() throws Exception {
        // init
        File fromFolder = new File(wc, "fromFolder");
        fromFolder.mkdirs();
        File toFolder = new File(new File(wc, "toFolder"), "toFolder");
        toFolder.mkdirs();
        
        // move        
        mrUnversionedFolder(new Mover(fromFolder, toFolder));
    }
    
    
    public void testRenameAddedFile() throws Exception {
        // init        
        File fromFile = new File(wc, "fromFile");
        fromFile.createNewFile();
        File toFile = new File(wc, "toFile");
        
        // add
        getClient().addFile(fromFile);                
        
        // rename
        mrAddedFile(new Renamer(fromFile, toFile));
    }
    
    public void testMoveAddedFile2UnversionedFolder() throws Exception {
        // init        
        File fromFile = new File(wc, "fromFile");
        fromFile.createNewFile();
        File toFolder = new File(wc, "toFodler");
        toFolder.mkdirs();
        
        // add
        getClient().addFile(fromFile);                
        
        // rename
        mrAddedFile(new Mover(fromFile, toFolder));
    }
    
    public void testMoveAddedFile2VersionedFolder() throws Exception {
        // init        
        File toFolder = new File(wc, "toFodler");
        toFolder.mkdirs();
        commit(wc);
        File fromFile = new File(wc, "fromFile");
        fromFile.createNewFile();        
        
        // add
        getClient().addFile(fromFile);                
        
        // rename
        mrAddedFile(new Mover(fromFile, toFolder));
    }

    public void testRenameA2B2A() throws Exception {
        // init
        File fileA = new File(wc, "from");
        fileA.createNewFile();
        commit(wc);  
        
        File fileB = new File(wc, "to");
        
        mrA2B2A(new Renamer(fileA, fileB), new Renamer(fileB, fileA));
    }
    
    public void testMoveA2B2A() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        File folder = new File(wc, "folder");
        folder.mkdirs();        
        commit(wc);  
        
        mrA2B2A(new Mover(fileA, folder), 
                new Mover(new File(folder, fileA.getName()), fileA.getParentFile()));
    }
    
    public void mrA2B2A(Handler handlerA2B, Handler handlerB2A) throws Exception {
        handlerA2B.handle();
        handlerB2A.handle();
        
        // test 
        assertTrue(handlerA2B.from.exists());
        assertFalse(handlerA2B.to.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(handlerA2B.from).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(handlerA2B.to).getTextStatus());        
              
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(handlerA2B.from));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(handlerA2B.to));                
        
        commit(wc);
    }

    public void testRenameA2B2C() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();        
        commit(wc);  
        
        // rename
        File fileB = new File(wc, "B");
        File fileC = new File(wc, "C");

        mrA2B2C(new Renamer(fileA, fileB), new Renamer(fileB, fileC));
    }
    
    public void testMoveA2B2C() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        File folderB = new File(wc, "folderB");
        folderB.mkdirs();
        File folderC = new File(wc, "folderC");
        folderC.mkdirs();
        commit(wc);  
        
        // rename
        mrA2B2C(new Mover(fileA, folderB), 
                new Mover(new File(folderB, fileA.getName()), folderC));
    }
    
    private void mrA2B2C(Handler handlerA2B, Handler handlerB2C) throws Exception {        
        handlerA2B.handle();
        handlerB2C.handle();
                
        // test 
        assertFalse(handlerA2B.from.exists());
        assertFalse(handlerA2B.to.exists());
        assertTrue(handlerB2C.to.exists());
        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(handlerA2B.from).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(handlerA2B.to).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(handlerB2C.from).getTextStatus());        
        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(handlerA2B.from));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(handlerA2B.to));                
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getCachedStatus(handlerB2C.from));    
        
        commit(wc);
    }

    public void testRenameA2B2C2A() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        commit(wc);  
        
        File fileB = new File(wc, "B");
        File fileC = new File(wc, "C");
        
        // rename A to B        
        mrA2B2C2A(new Renamer(fileA, fileB), // rename A to B
                  new Renamer(fileB, fileC), // rename B to C
                  new Renamer(fileC, fileA));// rename C to A
    }        
    
    public void testRenameA2B_CreateA() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        commit(wc);  
        
        // rename
        File fileB = new File(wc, "B");
        new Renamer(fileA, fileB).handle();
        // create from file
        FileUtil.toFileObject(fileA.getParentFile()).createData(fileA.getName());
        
        // test 
        assertTrue(fileB.exists());
        assertTrue(fileA.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileB).getTextStatus());        
        
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getCachedStatus(fileB));                
        
        commit(wc);
    }
    
    // not fixed yet - see issue #129805
    public void testDeleteA_RenameB2A_not_fixed_yet_129805() throws Exception {
        // init
        File fileA = new File(wc, "A");
        fileA.createNewFile();
        File fileB = new File(wc, "B");
        fileB.createNewFile();
        commit(wc);  
        
        // delete A
        delete(fileA);
        // rename B to A
        new Renamer(fileB, fileA).handle();
        
        // test 
        assertFalse(fileB.exists());
        assertTrue(fileA.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());                
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());        
             
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));                
        
        commit(wc);
    }
    
    public void testRenameVersionedFolder() throws Exception {
        // init
        File fromFolder = new File(wc, "from");
        fromFolder.mkdirs();
        commit(wc);               
        
        // rename
        File toFolder = new File(wc, "to");
        mrVersionedFolder(new Renamer(fromFolder, toFolder));
    }

    public void testMoveVersionedFolder() throws Exception {
        // init
        File fromFolder = new File(wc, "from");
        fromFolder.mkdirs();
        File toFolder = new File(wc, "toFolder");
        toFolder.mkdirs();
        commit(wc);               
        
        // rename
        mrVersionedFolder(new Mover(fromFolder, toFolder));
    }    
    
    public void testRenameFileTree() throws Exception {
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
        new Renamer(fromFolder, toFolder).handle();
                                        
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
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile11));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile12));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile21));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile22));   
        
        commit(wc);
        assertFalse(fromFolder.exists());
    }
    
    public void testMoveFileTree() throws Exception {
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
        File toFolder = new File(wc, "to");
        toFolder.mkdirs();
        commit(wc);               
        
        // rename
        new Mover(fromFolder, toFolder).handle();
                                        
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
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile11));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile12));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile21));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile22));   
        
        commit(wc);
        assertFalse(fromFolder.exists());
    }
     
    private void mrVersionedFile(Handler handler) throws Exception {               
        handler.handle();
                                        
        // test 
        assertFalse(handler.from.exists());
        assertTrue(handler.to.exists());
        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(handler.from).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(handler.to).getTextStatus());        
        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCachedStatus(handler.from));                
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getCachedStatus(handler.to));   
        
        commit(wc);
    }
    
    private void mrUnversionedFile(Handler handler) throws Exception {
        handler.handle();
                                        
        // test 
        assertFalse(handler.from.exists());
        assertTrue(handler.to.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(handler.from).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(handler.to).getTextStatus());        
              
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(handler.from));                
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getCachedStatus(handler.to));                
        
        commit(wc);
    }

    private void mrUnversionedFolder(Handler hanlder) throws Exception {
        hanlder.handle();
        // test 
        assertFalse(hanlder.from.exists());
        assertTrue(hanlder.to.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(hanlder.from).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(hanlder.to).getTextStatus());        
              
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(hanlder.from));                
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getCachedStatus(hanlder.to));                
        
        commit(wc);
    }


    private void mrAddedFile(Handler handler) throws Exception {
        handler.handle();
                                        
        // test 
        assertFalse(handler.from.exists());
        assertTrue(handler.to.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(handler.from).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(handler.to).getTextStatus());        
        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(handler.from));                
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getCachedStatus(handler.to));                
        
        commit(wc);
    }
    
    private void mrVersionedFolder(Handler handler) throws Exception {
        handler.handle();
                                        
        // test 
        assertTrue(handler.from.exists());
        assertTrue(handler.to.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(handler.from).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(handler.to).getTextStatus());        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCachedStatus(handler.from));                
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getCachedStatus(handler.to));                
        commit(wc);
        assertFalse(handler.from.exists());
    }

    private void mrA2B2C2A(Handler handlerA2B, Handler handlerB2C, Handler handlerC2A) throws Exception {
        handlerA2B.handle();
        handlerB2C.handle();
        handlerC2A.handle();
        
        // test 
        assertTrue(handlerA2B.from.exists());
        assertFalse(handlerA2B.to.exists());
        assertFalse(handlerB2C.to.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(handlerA2B.from).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(handlerA2B.to).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(handlerB2C.to).getTextStatus());  
        
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(handlerA2B.from));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(handlerA2B.to));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(handlerB2C.to));                
        
        commit(wc);
    }
    
    // XXX add move tests
    
//    public void testRecreateDeletedFolder() throws IOException, SVNClientException {
//        // init
//        File wc = new File(dataRootDir, "wc");
//        wc.mkdirs();        
//        File folder = new File(wc, "folder");
//        folder.mkdirs();
//        commit(wc);        
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
    
    private void commit(File folder) throws SVNClientException {
        add(folder);
        try {
            getClient().commit(new File[]{ folder }, "commit", true);
        } catch (SVNClientException e) {
            fail("commit was supposed to work");
        }    
        assertStatus(SVNStatusKind.NORMAL, folder);
    }

    private void add(File file) throws SVNClientException {
        ISVNStatus status = getSVNStatus(file);
        if(status.getTextStatus().equals(SVNStatusKind.UNVERSIONED)) {
            getClient().addFile(file);
        }
        if(file.isFile()) {
            return; 
        }
        File[] files = file.listFiles();
        if(files != null) {
            for (File f : files) {
                if(!isMetadata(f)) {
                    add(f);
                }
            }            
        }
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

    private void cleanUpWC() throws IOException {
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
    
    private void initRepo() throws MalformedURLException, IOException, InterruptedException, SVNClientException {        
        ISVNDirEntry[] list;
        if(!repoDir.exists()) {
            repoDir.mkdirs();            
            String[] cmd = {"svnadmin", "create", repoDir.getAbsolutePath()};
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor();   
        } else {
            list = getClient().getList(repoUrl, SVNRevision.HEAD, false);            
            if(list != null) {
                for (ISVNDirEntry entry : list) {
                    if(entry.getPath().equals(wc.getName())) {
                        getClient().remove(new SVNUrl[] {repoUrl.appendPath(wc.getName())}, "remove");
                    }
                }
            }
        }
    }
    
    private void svnimport() throws SVNClientException {
        ISVNClientAdapter client = getClient();        
        client.mkdir(repoUrl.appendPath(wc.getName()), "msg");        
        client.checkout(repoUrl.appendPath(wc.getName()), wc, SVNRevision.HEAD, true);        
        File[] files = wc.listFiles();
        if(files != null) {
            for (File file : files) {
                if(!isMetadata(file)) {
                    client.addFile(new File[] {file}, true);   
                }                
            }
            client.commit(new File[] {wc}, "commit", true);                    
        }        
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
        return SvnUtils.isAdministrative(file) || SvnUtils.isPartOfSubversionMetadata(file);
    }
    
    private abstract class Handler {
        protected final File from;
        protected final File to;
        public Handler(File from, File to) {
            this.from = from;
            this.to = to;
        }
        public abstract void handle() throws Exception;        
    }
    
    private class Renamer extends Handler{
        public Renamer(File from, File to) {
            super(from, to);
        }
        public void handle() throws Exception {
            DataObject daoFrom = DataObject.find(FileUtil.toFileObject(from));                
            daoFrom.rename(to.getName());        
        }
    }
    
    private class Mover extends Handler {
        public Mover(File from, File toFolder) {
            super(from, new File(toFolder, from.getName()));
        }
        public void handle() throws Exception {
            DataObject daoFrom = DataObject.find(FileUtil.toFileObject(from));    
            DataObject daoTarget = DataObject.find(FileUtil.toFileObject(to.getParentFile()));    
            daoFrom.move((DataFolder) daoTarget);        
        }
    }    
}
