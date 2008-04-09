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
import org.netbeans.modules.versioning.Utils;
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
        
    public InteceptorTest(String testName) throws IOException, MalformedURLException, InterruptedException {
        super(testName);
        dataRootDir = new File(System.getProperty("data.root.dir"));        
        try {
            CmdLineClientAdapterFactory.setup13(null);                        
        } catch (SVNClientException ex) {
            Exceptions.printStackTrace(ex);
        }        
        cache = Subversion.getInstance().getStatusCache();        
    }            

    @Override
    protected void setUp() throws Exception {               
        cleanUpWC();
        initRepo();        
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testDeleteNotVersionedFile() throws IOException, SVNClientException {
        // init
        File wc = new File(dataRootDir, "wc");
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
        
        FileInformation info = cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
        int status = info.getStatus();
        assertEquals(FileInformation.STATUS_UNKNOWN, status);
        commit(wc);        
    }

    public void testDeleteVersionedFile() throws IOException, SVNClientException {
        // init
        File wc = new File(dataRootDir, "wc");
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
        waitALittleBit();
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCachedStatus(file));
        commit(wc);
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file).getTextStatus());        
    }

    public void testDeleteVersionedFolder() throws IOException, SVNClientException {
        // init
        File wc = new File(dataRootDir, "wc");
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
        waitALittleBit();
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCachedStatus(folder));        
        commit(wc);
        assertFalse(folder.exists());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());        
    }

    public void testDeleteNotVersionedFolder() throws IOException, SVNClientException {
        // init
        File wc = new File(dataRootDir, "wc");
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
        FileInformation info = cache.refresh(folder, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
        assertEquals(FileInformation.STATUS_UNKNOWN, info.getStatus());
        commit(wc);        
    }    
    
    public void testDeleteWCRoot() throws IOException, SVNClientException {
        // init
        File folder = new File(dataRootDir, "wc");
        folder.mkdirs();                
        svnimport(folder);        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(folder).getTextStatus());

        // delete
        delete(folder);
        
        // test
        assertTrue(!folder.exists());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());        
        FileInformation info = cache.refresh(folder, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, info.getStatus());        
    }

    public void testDeleteVersionedFileTree() throws IOException, SVNClientException {
        // init
        File wc = new File(dataRootDir, "wc");
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
        
        waitALittleBit();
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCachedStatus(folder));        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCachedStatus(folder1));        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCachedStatus(folder2));        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, cache.refresh(file11, FileStatusCache.REPOSITORY_STATUS_UNKNOWN).getStatus());        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, cache.refresh(file12, FileStatusCache.REPOSITORY_STATUS_UNKNOWN).getStatus());        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, cache.refresh(file21, FileStatusCache.REPOSITORY_STATUS_UNKNOWN).getStatus());        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, cache.refresh(file22, FileStatusCache.REPOSITORY_STATUS_UNKNOWN).getStatus());        
        
        
        commit(wc);
        assertFalse(folder.exists());
        assertFalse(folder1.exists());
        assertFalse(folder2.exists());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());                
    }
    
    public void testDeleteNotVersionedFileTree() throws IOException, SVNClientException {
        // init
        File wc = new File(dataRootDir, "wc");
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
        assertEquals(FileInformation.STATUS_UNKNOWN, cache.refresh(folder, FileStatusCache.REPOSITORY_STATUS_UNKNOWN).getStatus());        
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, cache.refresh(folder1, FileStatusCache.REPOSITORY_STATUS_UNKNOWN).getStatus());        
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, cache.refresh(folder2, FileStatusCache.REPOSITORY_STATUS_UNKNOWN).getStatus());        
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, cache.refresh(file11, FileStatusCache.REPOSITORY_STATUS_UNKNOWN).getStatus());        
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, cache.refresh(file12, FileStatusCache.REPOSITORY_STATUS_UNKNOWN).getStatus());        
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, cache.refresh(file21, FileStatusCache.REPOSITORY_STATUS_UNKNOWN).getStatus());        
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, cache.refresh(file22, FileStatusCache.REPOSITORY_STATUS_UNKNOWN).getStatus());        
        
        commit(wc);
        assertFalse(folder.exists());
        assertFalse(folder1.exists());
        assertFalse(folder2.exists());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());        
    }
    
    public void testRecreateDeletedFile() throws IOException, SVNClientException {
        // init
        File wc = new File(dataRootDir, "wc");
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
        FileInformation info = cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, info.getStatus());                
    }

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
    
    private void commit(File folder) throws SVNClientException {
        try {
            getClient().commit(new File[]{folder}, "commit", true);
        } catch (SVNClientException e) {
            fail("commit was supposed to work");
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

    private void cleanUpWC() {
        File[] files = dataRootDir.listFiles();
        if(files != null) {
            for (File file : files) {
                Utils.deleteRecursively(file);
            }
        }
    }

    private void delete(File file) throws IOException {
        DataObject dao = DataObject.find(FileUtil.toFileObject(file));    
        dao.delete();        
    }

    private int getCachedStatus(File file) {
        FileInformation info = cache.getCachedStatus(file);
        int status = info.getStatus();
        return status;
    }
    
    private void waitALittleBit() {
        try {
            Thread.sleep(3000);  
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
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

    private ISVNStatus getSVNStatus(File file) throws SVNClientException {
        return getClient().getSingleStatus(file);        
    }
    
    private ISVNClientAdapter getClient() {
        return SVNClientAdapterFactory.createSVNClient(CmdLineClientAdapterFactory.COMMANDLINE_CLIENT);
    }    
}
