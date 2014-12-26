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

package org.netbeans.modules.subversion.remote;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import junit.framework.Test;
import org.netbeans.api.queries.VersioningQuery;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.remote.impl.fs.RemoteFileTestBase;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.netbeans.modules.remotefs.versioning.spi.FilesystemInterceptorProviderImpl;
import org.netbeans.modules.remotefs.versioning.spi.VersioningAnnotationProviderImpl;
import org.netbeans.modules.subversion.remote.api.ISVNStatus;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.api.SVNStatusKind;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.client.SvnClient;
import org.netbeans.modules.subversion.remote.util.VCSFileProxySupport;
import org.netbeans.modules.subversion.remote.utils.TestUtilities;
import org.netbeans.modules.versioning.core.VersioningManager;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * Test is written for 1.7 subversion support, do not even try to run it with 1.6 client
 * @author Tomas Stupka
 */
public class InterceptorTestCase extends RemoteVersioningTestBase {

    public InterceptorTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }
        
    @Override
    protected void setUp() throws Exception {          
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    public static Test suite() {
        return RemoteApiTest.createSuite(InterceptorTestCase.class);
//        TestSuite suite = new TestSuite();
//
//        suite.addTest(getAttributeSuite());
//
//        suite.addTest(createSuite());
//
//        suite.addTest(deleteSuite());
//
//        suite.addTest(renameViaDataObjectSuite());
//        suite.addTest(renameViaFileObjectSuite());
//
//        suite.addTest(moveViaDataObjectSuite());
//        suite.addTest(moveViaFileObjectSuite());
//
//        suite.addTest(copyViaDataObjectSuite());
//        suite.addTest(copyViaFileObjectSuite());
//        
//        suite.addTest(modifySuite());
//
//        return suite;
    }
    
//    public static Test modifySuite() {
//        TestSuite suite = new TestSuite();
//        suite.addTest(new InterceptorTestCase("testModifyFileOnDemandLock"));
//        return(suite);
//    }
    
//    public static Test getAttributeSuite() {
//        TestSuite suite = new TestSuite();
//        suite.addTest(new InterceptorTestCase("getWrongAttribute"));
//        suite.addTest(new InterceptorTestCase("getRemoteLocationAttribute"));
//        suite.addTest(new InterceptorTestCase("getIsManaged"));
//        return(suite);
//    }
//
//    public static Test deleteSuite() {
//	TestSuite suite = new TestSuite();
//        suite.addTest(new InterceptorTestCase("deleteCreateChangeCase_issue_157373"));
//        suite.addTest(new InterceptorTestCase("deleteNotVersionedFile"));
//        suite.addTest(new InterceptorTestCase("deleteVersionedFileExternally"));
//        suite.addTest(new InterceptorTestCase("deleteVersionedFile"));
//        suite.addTest(new InterceptorTestCase("deleteVersionedFolder"));
//        suite.addTest(new InterceptorTestCase("deleteNotVersionedFolder"));
//        suite.addTest(new InterceptorTestCase("deleteWCRoot"));
//        suite.addTest(new InterceptorTestCase("deleteVersionedFileTree"));
//        suite.addTest(new InterceptorTestCase("deleteNotVersionedFileTree"));
//        return(suite);
//    }
//   
//    public static Test createSuite() {
//        TestSuite suite = new TestSuite();
//        suite.addTest(new InterceptorTestCase("testCreateNewFile"));
//        suite.addTest(new InterceptorTestCase("createNewFolder"));
//        suite.addTest(new InterceptorTestCase("deleteA_CreateA"));
//        suite.addTest(new InterceptorTestCase("deleteA_CreateAOnDemandLocking"));
//        suite.addTest(new InterceptorTestCase("deleteA_CreateA_RunAtomic"));
//        suite.addTest(new InterceptorTestCase("afterDelete_AfterCreate_194998"));
//        return(suite);
//    }
//    
//    public static Test renameViaDataObjectSuite() {
//	TestSuite suite = new TestSuite();
//        suite.addTest(new InterceptorTestCase("renameVersionedFile_DO"));
//        suite.addTest(new InterceptorTestCase("renameUnversionedFile_DO"));
//        suite.addTest(new InterceptorTestCase("renameUnversionedFolder_DO"));
//        suite.addTest(new InterceptorTestCase("renameAddedFile_DO"));
//        suite.addTest(new InterceptorTestCase("renameA2B2A_DO"));
//        suite.addTest(new InterceptorTestCase("renameA2B2C_DO"));
//        suite.addTest(new InterceptorTestCase("renameA2B2C2A_DO"));
//        suite.addTest(new InterceptorTestCase("renameA2B_CreateA_DO"));
//        suite.addTest(new InterceptorTestCase("deleteA_RenameB2A_DO_129805"));
//        suite.addTest(new InterceptorTestCase("renameVersionedFolder_DO"));
//        suite.addTest(new InterceptorTestCase("renameFileTree_DO"));
//        suite.addTest(new InterceptorTestCase("renameA2CB2A_DO"));
//        suite.addTest(new InterceptorTestCase("deleteA_renameB2A2B_DO"));
//        suite.addTest(new InterceptorTestCase("deleteA_renameUnversioned2A_DO"));
//        return(suite);
//    }
//    
//    public static Test moveViaDataObjectSuite() {
//	TestSuite suite = new TestSuite();
//        suite.addTest(new InterceptorTestCase("moveVersionedFile_DO"));
//        suite.addTest(new InterceptorTestCase("moveUnversionedFile_DO"));
//        suite.addTest(new InterceptorTestCase("moveUnversionedFolder_DO"));
//        suite.addTest(new InterceptorTestCase("moveAddedFile2UnversionedFolder_DO"));
//        suite.addTest(new InterceptorTestCase("moveVersionedFile2IgnoredFolder_DO"));
//        suite.addTest(new InterceptorTestCase("moveAddedFile2VersionedFolder_DO"));
//        suite.addTest(new InterceptorTestCase("moveA2B2A_DO"));
//        suite.addTest(new InterceptorTestCase("moveA2B2C_DO"));
//        suite.addTest(new InterceptorTestCase("moveA2B2C2A_DO"));
//        suite.addTest(new InterceptorTestCase("moveA2B_CreateA_DO"));
//        suite.addTest(new InterceptorTestCase("moveVersionedFolder_DO"));
//        suite.addTest(new InterceptorTestCase("moveFileTree_DO"));
//        suite.addTest(new InterceptorTestCase("moveVersionedFile2Repos_DO"));
//        suite.addTest(new InterceptorTestCase("moveVersionedFolder2Repos_DO"));
//        suite.addTest(new InterceptorTestCase("moveFileTree2Repos_DO"));
//        suite.addTest(new InterceptorTestCase("moveA2CB2A_DO"));
//        suite.addTest(new InterceptorTestCase("deleteA_moveB2A2B_DO"));
//        suite.addTest(new InterceptorTestCase("deleteA_moveUnversioned2A_DO"));
//        return(suite);
//    }
//    
//    public static Test renameViaFileObjectSuite() {
//	TestSuite suite = new TestSuite();
//        suite.addTest(new InterceptorTestCase("renameVersionedFile_FO"));
//        suite.addTest(new InterceptorTestCase("renameUnversionedFile_FO"));
//        suite.addTest(new InterceptorTestCase("renameUnversionedFolder_FO"));
//        suite.addTest(new InterceptorTestCase("renameAddedFile_FO"));
//        suite.addTest(new InterceptorTestCase("renameA2B2A_FO"));
//        suite.addTest(new InterceptorTestCase("renameA2B2C_FO"));
//        suite.addTest(new InterceptorTestCase("renameA2B2C2A_FO"));
//        suite.addTest(new InterceptorTestCase("renameA2B_CreateA_FO"));
//        suite.addTest(new InterceptorTestCase("deleteA_RenameB2A_FO_129805"));
//        suite.addTest(new InterceptorTestCase("renameVersionedFolder_FO"));
//        suite.addTest(new InterceptorTestCase("renameFileTree_FO"));
//        suite.addTest(new InterceptorTestCase("renameA2CB2A_FO"));
//        suite.addTest(new InterceptorTestCase("renameA2a_FO"));
//        suite.addTest(new InterceptorTestCase("deleteA_renameB2A2B_FO"));
//        suite.addTest(new InterceptorTestCase("deleteA_renameUnversioned2A_FO"));
//        return(suite);
//    }
//    
//    public static Test moveViaFileObjectSuite() {
//	TestSuite suite = new TestSuite();
//        suite.addTest(new InterceptorTestCase("moveVersionedFile_FO"));
//        suite.addTest(new InterceptorTestCase("moveUnversionedFile_FO"));
//        suite.addTest(new InterceptorTestCase("moveUnversionedFolder_FO"));
//        suite.addTest(new InterceptorTestCase("moveAddedFile2UnversionedFolder_FO"));
//        suite.addTest(new InterceptorTestCase("moveVersionedFile2IgnoredFolder_FO"));
//        suite.addTest(new InterceptorTestCase("moveAddedFile2VersionedFolder_FO"));
//        suite.addTest(new InterceptorTestCase("moveA2B2A_FO"));
//        suite.addTest(new InterceptorTestCase("moveA2B2C_FO"));
//        suite.addTest(new InterceptorTestCase("moveA2B2C2A_FO"));
//        suite.addTest(new InterceptorTestCase("moveA2B_CreateA_FO"));
//        suite.addTest(new InterceptorTestCase("moveVersionedFolder_FO"));
//        suite.addTest(new InterceptorTestCase("moveFileTree_FO"));
//        suite.addTest(new InterceptorTestCase("moveVersionedFile2Repos_FO"));
//        suite.addTest(new InterceptorTestCase("moveVersionedFolder2Repos_FO"));
//        suite.addTest(new InterceptorTestCase("moveFileTree2Repos_FO"));
//        suite.addTest(new InterceptorTestCase("moveA2CB2A_FO"));
//        suite.addTest(new InterceptorTestCase("deleteA_moveB2A2B_FO"));
//        suite.addTest(new InterceptorTestCase("deleteA_moveUnversioned2A_FO"));
//        
//        return(suite);
//    }


//    public static Test copyViaDataObjectSuite() {
//	TestSuite suite = new TestSuite();
//        suite.addTest(new InterceptorTestCase("copyVersionedFile_DO"));
//        suite.addTest(new InterceptorTestCase("copyUnversionedFile_DO"));
//        suite.addTest(new InterceptorTestCase("copyUnversionedFolder_DO"));
//        suite.addTest(new InterceptorTestCase("copyAddedFile2UnversionedFolder_DO"));
//        suite.addTest(new InterceptorTestCase("copyAddedFile2VersionedFolder_DO"));
//
//        suite.addTest(new InterceptorTestCase("copyVersionedFile2UnversionedFolder_DO"));
//        suite.addTest(new InterceptorTestCase("copyVersionedFile2IgnoredFolder_DO"));
//        suite.addTest(new InterceptorTestCase("copyVersionedFolder2UnversionedFolder_DO"));
//
//        suite.addTest(new InterceptorTestCase("copyA2B2C_DO"));
//        suite.addTest(new InterceptorTestCase("copyVersionedFolder_DO"));
//        suite.addTest(new InterceptorTestCase("copyFileTree_DO"));
//        suite.addTest(new InterceptorTestCase("copyVersionedFile2Repos_DO"));
//        suite.addTest(new InterceptorTestCase("copyVersionedFolder2Repos_DO"));
//        suite.addTest(new InterceptorTestCase("copyFileTree2Repos_DO"));
//        suite.addTest(new InterceptorTestCase("deleteA_copyUnversioned2A_DO"));
//
//        return(suite);
//    }
//
//    // XXX add tests for move/copy of ignored files
//    public static Test copyViaFileObjectSuite() {
//	TestSuite suite = new TestSuite();
//        suite.addTest(new InterceptorTestCase("copyVersionedFile_FO"));
//        suite.addTest(new InterceptorTestCase("copyUnversionedFile_FO"));
//        suite.addTest(new InterceptorTestCase("copyUnversionedFolder_FO"));
//        suite.addTest(new InterceptorTestCase("copyAddedFile2UnversionedFolder_FO"));
//        suite.addTest(new InterceptorTestCase("copyVersionedFolder2UnversionedFolder_FO"));
//        suite.addTest(new InterceptorTestCase("copyVersionedFile2UnversionedFolder_FO"));
//        suite.addTest(new InterceptorTestCase("copyVersionedFile2IgnoredFolder_FO"));
//        suite.addTest(new InterceptorTestCase("copyAddedFile2VersionedFolder_FO"));
//        suite.addTest(new InterceptorTestCase("copyA2B2C_FO"));
//        suite.addTest(new InterceptorTestCase("copyVersionedFolder_FO"));
//        suite.addTest(new InterceptorTestCase("copyFileTree_FO"));
//        suite.addTest(new InterceptorTestCase("copyVersionedFile2Repos_FO"));
//        suite.addTest(new InterceptorTestCase("copyVersionedFolder2Repos_FO"));
//        suite.addTest(new InterceptorTestCase("copyFileTree2Repos_FO"));
//        suite.addTest(new InterceptorTestCase("deleteA_copyUnversioned2A_FO"));
//
//        return(suite);
//    }
    
    //@ForAllEnvironments
    public void testModifyFileOnDemandLock () throws Exception {
        // init
        VCSFileProxy file = VCSFileProxy.createFileProxy(wc, "file");
        VCSFileProxySupport.createNew(file);
        commit(wc);
        getClient().propertySet(file, "svn:needs-lock", "true", false);
        commit(file);
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file).getTextStatus());

        SvnModuleConfig.getDefault(fs).setAutoLock(true);
        // modify
        OutputStream os = file.toFileObject().getOutputStream();
        os.write(new byte[] { 'a', 0 });
        os.close();

        // test
        assertTrue(file.exists());
        assertEquals(SVNStatusKind.MODIFIED, getSVNStatus(file).getTextStatus());

        assertCachedStatus(file, FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY_CONTENT | FileInformation.STATUS_LOCKED);

        commit(wc);

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file).getTextStatus());
    }

    public void getWrongAttribute() throws Exception {
        VCSFileProxy file = VCSFileProxy.createFileProxy(wc, "attrfile");
        VCSFileProxySupport.createNew(file);
        FileObject fo = file.toFileObject();

        String str = (String) fo.getAttribute("peek-a-boo");
        assertNull(str);
    }

    public void getRemoteLocationAttribute() throws Exception {
        VCSFileProxy file = VCSFileProxy.createFileProxy(wc, "attrfile");
        VCSFileProxySupport.createNew(file);
        FileObject fo = file.toFileObject();

        String str = (String) fo.getAttribute(PROVIDED_EXTENSIONS_REMOTE_LOCATION);
        assertNotNull(str);
        assertEquals(repoUrl.toString(), str);
    }

    public void getIsManaged() throws Exception {
        // unversioned file
        VCSFileProxy file = VCSFileProxy.createFileProxy(dataRootDir, "unversionedfile");
        VCSFileProxySupport.createNew(file);

        boolean versioned = VersioningQuery.isManaged(file.toURI());
        assertFalse(versioned);

        // metadata folder
        file = VCSFileProxy.createFileProxy(wc, ".svn");

        versioned = VersioningQuery.isManaged(file.toURI());
        assertTrue(versioned);

        // metadata file
        file = VCSFileProxy.createFileProxy(file, "entries");

        versioned = VersioningQuery.isManaged(file.toURI());
        assertTrue(versioned);

        // versioned file
        file = VCSFileProxy.createFileProxy(wc, "attrfile");
        VCSFileProxySupport.createNew(file);

        versioned = VersioningQuery.isManaged(file.toURI());
        assertTrue(versioned);
    }

    public void deleteNotVersionedFile() throws Exception {
        // init        
        VCSFileProxy file = VCSFileProxy.createFileProxy(wc, "file");
        VCSFileProxySupport.createNew(file);
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file).getTextStatus());

        // delete
        delete(file);

        // test
        assertFalse(file.exists());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file).getTextStatus());
        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(file));
        
//        commit(wc);
    }

    public void deleteVersionedFile() throws Exception {
        // init
        VCSFileProxy file = VCSFileProxy.createFileProxy(wc, "file");
        VCSFileProxySupport.createNew(file);
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
        VCSFileProxy file = VCSFileProxy.createFileProxy(wc, "file");
        VCSFileProxySupport.createNew(file);
        assertCachedStatus(file, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        commit(wc);
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(file));

        String prop = System.getProperty("org.netbeans.modules.subversion.deleteMissingFiles", "");
        try {
            System.setProperty("org.netbeans.modules.subversion.deleteMissingFiles", "true");
            // delete externally
            VCSFileProxySupport.delete(file);

            // test
            assertFalse(file.exists());
            assertEquals(SVNStatusKind.MISSING, getSVNStatus(file).getTextStatus());

            // notify changes
            file.toFileObject().refresh();
            assertCachedStatus(file, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        } finally {
            System.setProperty("org.netbeans.modules.subversion.deleteMissingFiles", prop);
        }
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(file).getTextStatus());
        commit(wc);
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file).getTextStatus());
    }

    public void deleteVersionedFolder() throws Exception {
        // init        
        VCSFileProxy folder = VCSFileProxy.createFileProxy(wc, "folder1");
        VCSFileProxySupport.mkdirs(folder);
        commit(wc);      
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(folder).getTextStatus());

        // delete
        delete(folder);

        // test
        assertFalse(folder.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(folder).getTextStatus());
        
        assertCachedStatus(folder, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);        
        
        commit(wc);
        
        assertFalse(folder.exists());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());        
    }

    public void deleteNotVersionedFolder() throws IOException, SVNClientException {
        // init        
        VCSFileProxy folder = VCSFileProxy.createFileProxy(wc, "folder2");
        VCSFileProxySupport.mkdirs(folder);
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());

        // delete
        delete(folder);
        
        // test
        assertFalse(folder.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(folder));
        
//        commit(wc);
    }    

    public void deleteWCRoot() throws Exception {
        // init        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(wc).getTextStatus());

        // delete
        delete(wc);
        
        // test
        assertTrue(!wc.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(wc).getTextStatus());        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(wc));
    }

    public void deleteVersionedFileTree() throws Exception {
        // init
        VCSFileProxy folder = VCSFileProxy.createFileProxy(wc, "folder");
        VCSFileProxySupport.mkdirs(folder);
        VCSFileProxy folder1 = VCSFileProxy.createFileProxy(folder, "folder1");
        VCSFileProxySupport.mkdirs(folder1);
        VCSFileProxy folder2 = VCSFileProxy.createFileProxy(folder, "folder2");
        VCSFileProxySupport.mkdirs(folder2);        
        VCSFileProxy file11 = VCSFileProxy.createFileProxy(folder1, "file1");
        VCSFileProxySupport.createNew(file11);
        VCSFileProxy file12 = VCSFileProxy.createFileProxy(folder1, "file2");
        VCSFileProxySupport.createNew(file12);
        VCSFileProxy file21 = VCSFileProxy.createFileProxy(folder2, "file1");
        VCSFileProxySupport.createNew(file21);
        VCSFileProxy file22 = VCSFileProxy.createFileProxy(folder2, "file2");
        VCSFileProxySupport.createNew(file22);
        
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
        assertFalse(folder.exists());
        assertFalse(folder1.exists());
        assertFalse(folder2.exists());
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
        VCSFileProxy folder = VCSFileProxy.createFileProxy(wc, "folder");
        VCSFileProxySupport.mkdirs(folder);
        VCSFileProxy folder1 = VCSFileProxy.createFileProxy(folder, "folder1");
        VCSFileProxySupport.mkdirs(folder1);
        VCSFileProxy folder2 = VCSFileProxy.createFileProxy(folder, "folder2");
        VCSFileProxySupport.mkdirs(folder2);        
        VCSFileProxy file11 = VCSFileProxy.createFileProxy(folder1, "file1");
        VCSFileProxySupport.createNew(file11);
        VCSFileProxy file12 = VCSFileProxy.createFileProxy(folder1, "file2");
        VCSFileProxySupport.createNew(file12);
        VCSFileProxy file21 = VCSFileProxy.createFileProxy(folder2, "file1");
        VCSFileProxySupport.createNew(file21);
        VCSFileProxy file22 = VCSFileProxy.createFileProxy(folder2, "file2");
        VCSFileProxySupport.createNew(file22);
        
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
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(folder1));        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(folder2));        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(file11));        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(file12));        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(file21));        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(file22));        
        
        commit(wc);
        assertFalse(folder.exists());
        assertFalse(folder1.exists());
        assertFalse(folder2.exists());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());        
    }

    @ForAllEnvironments
    public void testCreateNewFile() throws Exception {
        // init
        VCSFileProxy file = VCSFileProxy.createFileProxy(wc, "file");
        
        // create
        FileObject fo = wc.toFileObject();
        fo.createData(file.getName());
                                        
        // test 
        assertTrue(file.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file).getTextStatus());        
        assertCachedStatus(file, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
    }
    
    public void afterDelete_AfterCreate_194998 () throws Exception {
        // init
        VCSFileProxy file = VCSFileProxy.createFileProxy(wc, "file");
        
        // create
        FileObject fo = wc.toFileObject();
        fo.createData(file.getName());
        add(file);
        commit(file);
        
        // test 
        assertTrue(file.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file).getTextStatus());
        
        VCSFileProxySupport.delete(file);
        file.toFileObject().refresh();
        assertEquals(SVNStatusKind.MISSING, getSVNStatus(file).getTextStatus());
        assertCachedStatus(file, FileInformation.STATUS_VERSIONED_DELETEDLOCALLY);
        
        TestKit.write(file, "modification");
        file.getParentFile().toFileObject().refresh();
        assertCachedStatus(file, FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY_CONTENT);
        assertEquals(SVNStatusKind.MODIFIED, getSVNStatus(file).getTextStatus());
    }

    public void createNewFolder() throws Exception {
        // init
        VCSFileProxy folder = VCSFileProxy.createFileProxy(wc, "folder");
        
        // create
        FileObject fo = wc.toFileObject();
        fo.createFolder(folder.getName());
                                        
        // test 
        assertTrue(folder.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());        
        assertCachedStatus(folder, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
    }
    
    public void deleteA_CreateA() throws IOException, SVNClientException {
        
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "A");
        VCSFileProxySupport.createNew(fileA);        
        commit(wc);        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        
        // delete                
        FileObject fo = fileA.toFileObject();
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

    public void deleteA_CreateAOnDemandLocking() throws IOException, SVNClientException {
        // init
        VCSFileProxy file = VCSFileProxy.createFileProxy(wc, "A");
        VCSFileProxySupport.createNew(file);
        commit(wc);
        SvnModuleConfig.getDefault(fs).setAutoLock(true);
        getClient().propertySet(file, "svn:needs-lock", "true", false);
        commit(file);
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file).getTextStatus());
        
        // delete
        FileObject fo = file.toFileObject();
        fo.delete();

        // test if deleted
        assertFalse(file.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(file).getTextStatus());

        // create        
        fo.getParent().createData(fo.getName());       
        
        // test 
        assertTrue(file.exists());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file).getTextStatus());        
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE | FileInformation.STATUS_LOCKED, getStatus(file));                
    }

    public void deleteA_CreateA_RunAtomic() throws IOException, SVNClientException {
        // init
        final VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "A");
        VCSFileProxySupport.createNew(fileA);        
        commit(wc);        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        
        final FileObject fo = fileA.toFileObject();
        AtomicAction a = new AtomicAction() {
            @Override
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
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "fromFile");
        VCSFileProxySupport.createNew(fromFile);
        commit(wc);                       
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(wc, "toFile");
        
        // rename    
        renameDO(fromFile, toFile);
        
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile).getTextStatus());
        
        assertCachedStatus(fromFile, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(toFile, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        
//        commit(wc);
    }

    public void moveVersionedFile_DO() throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        commit(wc);
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // move
        moveDO(fromFile, toFile);

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile).getTextStatus());

        assertCachedStatus(fromFile, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(toFile, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

//        commit(wc);
    }

    public void moveVersionedFile2Repos_DO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc2, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        commit(wc2);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);
        commit(wc);
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, "file");
        // move
        moveDO(fromFile, toFile);

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());

        assertCachedStatus(fromFile, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);

//        commit(wc);
//        commit(wc2);
    }

    public void moveVersionedFolder2Repos_DO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy toFolderParent = VCSFileProxy.createFileProxy(wc2, "folderParent");
        VCSFileProxySupport.mkdirs(toFolderParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toFolderParent, fromFolder.getName());
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, "file");
        commit(wc2);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, toFile.getName());
        VCSFileProxySupport.createNew(fromFile);
        commit(wc);
        // move
        moveDO(fromFolder, toFolder);

        // test
        assertFalse(fromFolder.exists());
        assertFalse(fromFile.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());

        assertCachedStatus(fromFile, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fromFolder));
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

//        commit(wc);
//        commit(wc2);
    }

    public void moveFileTree2Repos_DO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFolder1 = VCSFileProxy.createFileProxy(fromFolder, "folder1");
        VCSFileProxySupport.mkdirs(fromFolder1);
        VCSFileProxy fromFolder2 = VCSFileProxy.createFileProxy(fromFolder, "folder2");
        VCSFileProxySupport.mkdirs(fromFolder2);
        VCSFileProxy fromFile11 = VCSFileProxy.createFileProxy(fromFolder1, "file11");
        VCSFileProxySupport.createNew(fromFile11);
        VCSFileProxy fromFile12 = VCSFileProxy.createFileProxy(fromFolder1, "file12");
        VCSFileProxySupport.createNew(fromFile12);
        VCSFileProxy fromFile21 = VCSFileProxy.createFileProxy(fromFolder2, "file21");
        VCSFileProxySupport.createNew(fromFile21);
        VCSFileProxy fromFile22 = VCSFileProxy.createFileProxy(fromFolder2, "file22");
        VCSFileProxySupport.createNew(fromFile22);

        VCSFileProxy toFolderParent = VCSFileProxy.createFileProxy(wc2, "toFolder");
        VCSFileProxySupport.mkdirs(toFolderParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toFolderParent, fromFolder.getName());
        commit(wc);
        commit(wc2);

        // move
        moveDO(fromFolder, toFolder);

//        // test         t.
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        VCSFileProxy toFolder1 = VCSFileProxy.createFileProxy(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        VCSFileProxy toFolder2 = VCSFileProxy.createFileProxy(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        VCSFileProxy toFile11 = VCSFileProxy.createFileProxy(toFolder1, "file11");
        assertTrue(toFile11.exists());
        VCSFileProxy toFile12 = VCSFileProxy.createFileProxy(toFolder1, "file12");
        assertTrue(toFile12.exists());
        VCSFileProxy toFile21 = VCSFileProxy.createFileProxy(toFolder2, "file21");
        assertTrue(toFile21.exists());
        VCSFileProxy toFile22 = VCSFileProxy.createFileProxy(toFolder2, "file22");
        assertTrue(toFile22.exists());

        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder1).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder2).getTextStatus());
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

        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fromFolder));
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fromFolder1));
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fromFolder2));
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

        assertFalse(fromFolder.exists());
        assertFalse(fromFolder1.exists());
        assertFalse(fromFolder2.exists());
        assertFalse(fromFile11.exists());
        assertFalse(fromFile12.exists());
        assertFalse(fromFile21.exists());
        assertFalse(fromFile22.exists());

    }

    public void moveVersionedFile2Repos_FO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc2, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        commit(wc2);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);
        commit(wc);
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, "file");
        // move
        moveFO(fromFile, toFile);

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());

        assertCachedStatus(fromFile, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);

//        commit(wc);
//        commit(wc2);
    }

    public void moveVersionedFolder2Repos_FO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy toFolderParent = VCSFileProxy.createFileProxy(wc2, "folderParent");
        VCSFileProxySupport.mkdirs(toFolderParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toFolderParent, fromFolder.getName());
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, "file");
        commit(wc2);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, toFile.getName());
        VCSFileProxySupport.createNew(fromFile);
        commit(wc);
        // move
        moveFO(fromFolder, toFolder);

        // test
        assertFalse(fromFolder.exists()); // TODO later delete from folder
        assertFalse(fromFile.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());

        assertCachedStatus(fromFolder, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

//        commit(wc);
//        commit(wc2);
    }

    public void moveFileTree2Repos_FO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFolder1 = VCSFileProxy.createFileProxy(fromFolder, "folder1");
        VCSFileProxySupport.mkdirs(fromFolder1);
        VCSFileProxy fromFolder2 = VCSFileProxy.createFileProxy(fromFolder, "folder2");
        VCSFileProxySupport.mkdirs(fromFolder2);
        VCSFileProxy fromFile11 = VCSFileProxy.createFileProxy(fromFolder1, "file11");
        VCSFileProxySupport.createNew(fromFile11);
        VCSFileProxy fromFile12 = VCSFileProxy.createFileProxy(fromFolder1, "file12");
        VCSFileProxySupport.createNew(fromFile12);
        VCSFileProxy fromFile21 = VCSFileProxy.createFileProxy(fromFolder2, "file21");
        VCSFileProxySupport.createNew(fromFile21);
        VCSFileProxy fromFile22 = VCSFileProxy.createFileProxy(fromFolder2, "file22");
        VCSFileProxySupport.createNew(fromFile22);

        VCSFileProxy toFolderParent = VCSFileProxy.createFileProxy(wc2, "toFolder");
        VCSFileProxySupport.mkdirs(toFolderParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toFolderParent, fromFolder.getName());
        commit(wc);
        commit(wc2);

        // move
        moveFO(fromFolder, toFolder);

//        // test         t.
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        VCSFileProxy toFolder1 = VCSFileProxy.createFileProxy(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        VCSFileProxy toFolder2 = VCSFileProxy.createFileProxy(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        VCSFileProxy toFile11 = VCSFileProxy.createFileProxy(toFolder1, "file11");
        assertTrue(toFile11.exists());
        VCSFileProxy toFile12 = VCSFileProxy.createFileProxy(toFolder1, "file12");
        assertTrue(toFile12.exists());
        VCSFileProxy toFile21 = VCSFileProxy.createFileProxy(toFolder2, "file21");
        assertTrue(toFile21.exists());
        VCSFileProxy toFile22 = VCSFileProxy.createFileProxy(toFolder2, "file22");
        assertTrue(toFile22.exists());

        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder1).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder2).getTextStatus());
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

        assertCachedStatus(fromFolder, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(fromFolder1, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(fromFolder2, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
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

        assertFalse(fromFolder.exists());
        assertFalse(fromFolder1.exists());
        assertFalse(fromFolder2.exists());
        assertFalse(fromFile11.exists());
        assertFalse(fromFile12.exists());
        assertFalse(fromFile21.exists());
        assertFalse(fromFile22.exists());

    }
    
    public void renameUnversionedFile_DO() throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "fromFile");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(wc, "toFile");
                
        // rename
        renameDO(fromFile, toFile);
        
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFile).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());        
              
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFile));                
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        
//        commit(wc);
    }
    
    public void moveUnversionedFile_DO() throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());
        
        // rename
        moveDO(fromFile, toFile);
        
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFile).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());        
              
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFile));                
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        
//        commit(wc);
    }

    public void renameUnversionedFolder_DO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "fromFolder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc, "toFolder");
        
        // rename
        renameDO(fromFolder, toFolder);
        
        // test 
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFolder).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFolder).getTextStatus());        
              
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFolder));                
        assertCachedStatus(toFolder, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        
//        commit(wc);
    }
    
    public void moveUnversionedFolder_DO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy toParent = VCSFileProxy.createFileProxy(wc, "toFolder");
        VCSFileProxySupport.mkdirs(toParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toParent, fromFolder.getName());
        
        // move        
        moveDO(fromFolder, toFolder);
        
        // test 
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFolder).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFolder).getTextStatus());        
              
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFolder));                
        assertCachedStatus(toFolder, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        
//        commit(wc);
    }
    
    public void copyVersionedFile_DO() throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        commit(wc);
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // copy
        copyDO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile).getTextStatus());


        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile));
        assertCachedStatus(toFile, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertTrue(getSVNStatus(toFile).isCopied());

//        commit(wc);
    }

    public void copyUnversionedFile_DO() throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // copy
        copyDO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());

        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getStatus(fromFile));
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);

        assertFalse(getSVNStatus(toFile).isCopied());

//        commit(wc);
    }

    public void copyUnversionedFolder_DO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy toParent = VCSFileProxy.createFileProxy(wc, "toFolder");
        VCSFileProxySupport.mkdirs(toParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toParent, fromFolder.getName());

        // copy
        copyDO(fromFolder, toFolder);

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());

        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFolder).getTextStatus());

        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getStatus(fromFolder));
        assertCachedStatus(toFolder, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);

        assertFalse(getSVNStatus(toFolder).isCopied());

//        commit(wc);
    }

    public void copyAddedFile2UnversionedFolder_DO() throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc, "toFodler");
        VCSFileProxySupport.mkdirs(toFolder);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // add
        getClient().addFile(fromFile);

        // copy
        copyDO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fromFile));
        assertCachedStatus(toFile, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        assertFalse(getSVNStatus(toFile).isCopied());

//        commit(wc);
    }

    public void copyVersionedFile2UnversionedFolder_DO() throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy unversionedFolder = VCSFileProxy.createFileProxy(dataRootDir, getName() + "_unversioned");
        VCSFileProxySupport.mkdirs(unversionedFolder);
        
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(unversionedFolder, fromFile.getName());

        // add
        getClient().addFile(fromFile);
        commit(wc);

        // copy
        copyDO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile));
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, getStatus(toFile));

        assertFalse(getSVNStatus(toFile).isCopied());

//        commit(wc);
    }

    public void copyVersionedFile2IgnoredFolder_DO() throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // commit
        commit(fromFile);
        //ignore
        getClient().setIgnoredPatterns(wc, Collections.singletonList(toFolder.getName()));

        // copy
        copyDO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile));
        assertEquals(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, getStatus(toFile));
        assertFalse(getSVNStatus(toFile).isCopied());
    }

    public void copyVersionedFolder2UnversionedFolder_DO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "folder");
        VCSFileProxySupport.mkdir(fromFolder);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);

        VCSFileProxy unversionedFolder = VCSFileProxy.createFileProxy(dataRootDir, getName() + "_unversioned");
        VCSFileProxySupport.mkdirs(unversionedFolder);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(unversionedFolder, fromFolder.getName());
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // add
        getClient().addFile(fromFolder);
        commit(wc);

        // copy
        copyDO(fromFolder, toFolder);

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFolder).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder));
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, getStatus(toFolder));

        assertFalse(getSVNStatus(toFolder).isCopied());

//        commit(wc);
    }

    public void copyAddedFile2VersionedFolder_DO() throws Exception {
        // init
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        commit(wc);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "fromFile");
        VCSFileProxySupport.createNew(fromFile);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // add
        getClient().addFile(fromFile);

        // rename
        copyDO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fromFile));
        assertCachedStatus(toFile, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        assertFalse(getSVNStatus(toFile).isCopied());

//        commit(wc);
    }

    public void copyA2B2C_DO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "A");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(wc, "folderB");
        VCSFileProxySupport.mkdirs(folderB);
        VCSFileProxy folderC = VCSFileProxy.createFileProxy(wc, "folderC");
        VCSFileProxySupport.mkdirs(folderC);
        commit(wc);

        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folderB, fileA.getName());
        VCSFileProxy fileC = VCSFileProxy.createFileProxy(folderC, fileA.getName());

        // move
        copyDO(fileA, fileB);
        copyDO(fileB, fileC);

        // test
        assertTrue(fileA.exists());
        assertTrue(fileB.exists());
        assertTrue(fileC.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileB).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileC).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileB));
        assertCachedStatus(fileC, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        assertTrue(getSVNStatus(fileB).isCopied());
        assertTrue(getSVNStatus(fileC).isCopied());
        
//        commit(wc);
    }

    public void copyVersionedFolder_DO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "from");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy toParent = VCSFileProxy.createFileProxy(wc, "toFolder");
        VCSFileProxySupport.mkdirs(toParent);
        commit(wc);

        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toParent, fromFolder.getName());

        // copy
        copyDO(fromFolder, toFolder);

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder));
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFolder).getTextStatus());
        assertCachedStatus(toFolder, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);

        // XXX will fail after fixing in fileentry.copy() !!!
        assertFalse(getSVNStatus(toFolder).isCopied());
//        commit(wc);
    }

    public void copyFileTree_DO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "from");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFolder1 = VCSFileProxy.createFileProxy(fromFolder, "folder1");
        VCSFileProxySupport.mkdirs(fromFolder1);
        VCSFileProxy fromFolder2 = VCSFileProxy.createFileProxy(fromFolder, "folder2");
        VCSFileProxySupport.mkdirs(fromFolder2);
        VCSFileProxy fromFile11 = VCSFileProxy.createFileProxy(fromFolder1, "file11");
        VCSFileProxySupport.createNew(fromFile11);
        VCSFileProxy fromFile12 = VCSFileProxy.createFileProxy(fromFolder1, "file12");
        VCSFileProxySupport.createNew(fromFile12);
        VCSFileProxy fromFile21 = VCSFileProxy.createFileProxy(fromFolder2, "file21");
        VCSFileProxySupport.createNew(fromFile21);
        VCSFileProxy fromFile22 = VCSFileProxy.createFileProxy(fromFolder2, "file22");
        VCSFileProxySupport.createNew(fromFile22);

        VCSFileProxy toFolderParent = VCSFileProxy.createFileProxy(wc, "to");
        VCSFileProxySupport.mkdirs(toFolderParent);

        commit(wc);

        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toFolderParent, fromFolder.getName());

        // move
        copyDO(fromFolder, toFolder);

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        VCSFileProxy toFolder1 = VCSFileProxy.createFileProxy(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        VCSFileProxy toFolder2 = VCSFileProxy.createFileProxy(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        VCSFileProxy toFile11 = VCSFileProxy.createFileProxy(toFolder1, "file11");
        assertTrue(toFile11.exists());
        VCSFileProxy toFile12 = VCSFileProxy.createFileProxy(toFolder1, "file12");
        assertTrue(toFile12.exists());
        VCSFileProxy toFile21 = VCSFileProxy.createFileProxy(toFolder2, "file21");
        assertTrue(toFile21.exists());
        VCSFileProxy toFile22 = VCSFileProxy.createFileProxy(toFolder2, "file22");
        assertTrue(toFile22.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder1).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder2).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile11).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile12).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile21).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile22).getTextStatus());
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
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile11));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile12));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile21));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile22));
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFolder1, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFolder2, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFile11, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFile12, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFile21, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFile22, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        // XXX should be fixed first in fileentry.copy
        //     afterwards assertTrue(...)
        assertFalse(getSVNStatus(toFolder).isCopied());
        assertFalse(getSVNStatus(toFolder1).isCopied());
        assertFalse(getSVNStatus(toFolder2).isCopied());

        assertTrue(getSVNStatus(toFile11).isCopied());
        assertTrue(getSVNStatus(toFile12).isCopied());
        assertTrue(getSVNStatus(toFile21).isCopied());
        assertTrue(getSVNStatus(toFile22).isCopied());

//        commit(wc);
    }

    public void copyVersionedFile2Repos_DO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc2, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        commit(wc2);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);
        commit(wc);
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, "file");

        // copy
        copyDO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile));
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);

        assertFalse(getSVNStatus(toFile).isCopied());

//        commit(wc);
//        commit(wc2);
    }

    public void copyVersionedFolder2Repos_DO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy toFolderParent = VCSFileProxy.createFileProxy(wc2, "folderParent");
        VCSFileProxySupport.mkdirs(toFolderParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toFolderParent, fromFolder.getName());
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, "file");
        commit(wc2);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, toFile.getName());
        VCSFileProxySupport.createNew(fromFile);
        commit(wc);

        // copy
        copyDO(fromFolder, toFolder);

        // test
        assertTrue(fromFolder.exists()); // TODO later delete from folder
        assertTrue(fromFile.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder));
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        assertFalse(getSVNStatus(toFolder).isCopied());
        assertFalse(getSVNStatus(toFile).isCopied());

//        commit(wc);
//        commit(wc2);
    }

    public void copyFileTree2Repos_DO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFolder1 = VCSFileProxy.createFileProxy(fromFolder, "folder1");
        VCSFileProxySupport.mkdirs(fromFolder1);
        VCSFileProxy fromFolder2 = VCSFileProxy.createFileProxy(fromFolder, "folder2");
        VCSFileProxySupport.mkdirs(fromFolder2);
        VCSFileProxy fromFile11 = VCSFileProxy.createFileProxy(fromFolder1, "file11");
        VCSFileProxySupport.createNew(fromFile11);
        VCSFileProxy fromFile12 = VCSFileProxy.createFileProxy(fromFolder1, "file12");
        VCSFileProxySupport.createNew(fromFile12);
        VCSFileProxy fromFile21 = VCSFileProxy.createFileProxy(fromFolder2, "file21");
        VCSFileProxySupport.createNew(fromFile21);
        VCSFileProxy fromFile22 = VCSFileProxy.createFileProxy(fromFolder2, "file22");
        VCSFileProxySupport.createNew(fromFile22);

        VCSFileProxy toFolderParent = VCSFileProxy.createFileProxy(wc2, "toFolder");
        VCSFileProxySupport.mkdirs(toFolderParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toFolderParent, fromFolder.getName());
        commit(wc);
        commit(wc2);

        // copy
        copyDO(fromFolder, toFolder);

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        VCSFileProxy toFolder1 = VCSFileProxy.createFileProxy(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        VCSFileProxy toFolder2 = VCSFileProxy.createFileProxy(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        VCSFileProxy toFile11 = VCSFileProxy.createFileProxy(toFolder1, "file11");
        assertTrue(toFile11.exists());
        VCSFileProxy toFile12 = VCSFileProxy.createFileProxy(toFolder1, "file12");
        assertTrue(toFile12.exists());
        VCSFileProxy toFile21 = VCSFileProxy.createFileProxy(toFolder2, "file21");
        assertTrue(toFile21.exists());
        VCSFileProxy toFile22 = VCSFileProxy.createFileProxy(toFolder2, "file22");
        assertTrue(toFile22.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder1).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder2).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile11).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile12).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile21).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile22).getTextStatus());
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
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile11));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile12));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile21));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile22));

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
        assertTrue(fromFile11.exists());
        assertTrue(fromFile12.exists());
        assertTrue(fromFile21.exists());
        assertTrue(fromFile22.exists());

        assertFalse(getSVNStatus(fromFolder).isCopied());
        assertFalse(getSVNStatus(fromFolder1).isCopied());
        assertFalse(getSVNStatus(fromFolder2).isCopied());
        assertFalse(getSVNStatus(fromFile11).isCopied());
        assertFalse(getSVNStatus(fromFile12).isCopied());
        assertFalse(getSVNStatus(fromFile21).isCopied());
        assertFalse(getSVNStatus(fromFile22).isCopied());

    }

    public void copyVersionedFile_FO() throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        commit(wc);
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // copy
        copyFO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile));
        assertCachedStatus(toFile, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        assertTrue(getSVNStatus(toFile).isCopied());

//        commit(wc);
    }

    public void copyUnversionedFile_FO() throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // copy
        copyFO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getStatus(fromFile));

        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);

        assertFalse(getSVNStatus(toFile).isCopied());

//        commit(wc);
    }

    public void copyUnversionedFolder_FO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy toParent = VCSFileProxy.createFileProxy(wc, "toFolder");
        VCSFileProxySupport.mkdirs(toParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toParent, fromFolder.getName());

        // copy
        copyFO(fromFolder, toFolder);

        // test 
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());

        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getStatus(fromFolder));

        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFolder).getTextStatus());
        assertCachedStatus(toFolder, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);

        assertFalse(getSVNStatus(toFolder).isCopied());

//        commit(wc);
    }

    public void copyAddedFile2UnversionedFolder_FO() throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // add
        getClient().addFile(fromFile);
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fromFile).getTextStatus());

        // copy
        copyFO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fromFile));
        assertCachedStatus(toFile, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        assertFalse(getSVNStatus(toFolder).isCopied());

//        commit(wc);
    }

    public void copyAddedFile2VersionedFolder_FO() throws Exception {
        // init
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc, "toFodler");
        VCSFileProxySupport.mkdirs(toFolder);
        commit(wc);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "fromFile");
        VCSFileProxySupport.createNew(fromFile);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // add
        getClient().addFile(fromFile);

        // copy
        copyFO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fromFile));
        assertCachedStatus(toFile, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        assertFalse(getSVNStatus(toFolder).isCopied());

//        commit(wc);
    }

    public void copyA2B2C_FO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "A");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(wc, "folderB");
        VCSFileProxySupport.mkdirs(folderB);
        VCSFileProxy folderC = VCSFileProxy.createFileProxy(wc, "folderC");
        VCSFileProxySupport.mkdirs(folderC);
        commit(wc);

        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folderB, fileA.getName());
        VCSFileProxy fileC = VCSFileProxy.createFileProxy(folderC, fileA.getName());

        // copy
        copyFO(fileA, fileB);
        copyFO(fileB, fileC);

        // test
        assertTrue(fileA.exists());
        assertTrue(fileB.exists());
        assertTrue(fileC.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileB).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileC).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
        assertCachedStatus(fileB, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(fileC, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        assertTrue(getSVNStatus(fileB).isCopied());
        assertTrue(getSVNStatus(fileC).isCopied());

//        commit(wc);
    }

    public void copyVersionedFile2UnversionedFolder_FO() throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(dataRootDir, getName() + "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // add
        getClient().addFile(fromFile);
        commit(wc);

        // copy
        copyFO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile));
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NOTMANAGED);

        assertFalse(getSVNStatus(toFile).isCopied());

//        commit(wc);
    }

    public void copyVersionedFile2IgnoredFolder_FO() throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // commit
        commit(fromFile);
        //ignore
        getClient().setIgnoredPatterns(wc, Collections.singletonList(toFolder.getName()));

        // copy
        copyFO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile));
        assertEquals(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, getStatus(toFile));
        assertFalse(getSVNStatus(toFile).isCopied());
    }

    public void copyVersionedFolder2UnversionedFolder_FO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "folder");
        VCSFileProxySupport.mkdir(fromFolder);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(dataRootDir, getName() + "toFolder");

        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // add
        getClient().addFile(fromFolder);
        commit(wc);

        // copy
        copyFO(fromFolder, toFolder);

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFolder).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder));
        assertCachedStatus(toFolder, FileInformation.STATUS_NOTVERSIONED_NOTMANAGED);

        assertFalse(getSVNStatus(toFolder).isCopied());

//        commit(wc);
    }

    public void copyVersionedFolder_FO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "from");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy toParent = VCSFileProxy.createFileProxy(wc, "toFolder");
        VCSFileProxySupport.mkdirs(toParent);
        commit(wc);

        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toParent, fromFolder.getName());

        // copy
        copyFO(fromFolder, toFolder);
        
        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder));
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        assertTrue(getSVNStatus(toFolder).isCopied());
        
//        commit(wc);
        
    }

    public void copyFileTree_FO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "from");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFolder1 = VCSFileProxy.createFileProxy(fromFolder, "folder1");
        VCSFileProxySupport.mkdirs(fromFolder1);
        VCSFileProxy fromFolder2 = VCSFileProxy.createFileProxy(fromFolder, "folder2");
        VCSFileProxySupport.mkdirs(fromFolder2);
        VCSFileProxy fromFile11 = VCSFileProxy.createFileProxy(fromFolder1, "file11");
        VCSFileProxySupport.createNew(fromFile11);
        VCSFileProxy fromFile12 = VCSFileProxy.createFileProxy(fromFolder1, "file12");
        VCSFileProxySupport.createNew(fromFile12);
        VCSFileProxy fromFile21 = VCSFileProxy.createFileProxy(fromFolder2, "file21");
        VCSFileProxySupport.createNew(fromFile21);
        VCSFileProxy fromFile22 = VCSFileProxy.createFileProxy(fromFolder2, "file22");
        VCSFileProxySupport.createNew(fromFile22);

        VCSFileProxy toFolderParent = VCSFileProxy.createFileProxy(wc, "to");
        VCSFileProxySupport.mkdirs(toFolderParent);

        commit(wc);

        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toFolderParent, fromFolder.getName());

        // copy
        copyFO(fromFolder, toFolder);

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        VCSFileProxy toFolder1 = VCSFileProxy.createFileProxy(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        VCSFileProxy toFolder2 = VCSFileProxy.createFileProxy(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        VCSFileProxy toFile11 = VCSFileProxy.createFileProxy(toFolder1, "file11");
        assertTrue(toFile11.exists());
        VCSFileProxy toFile12 = VCSFileProxy.createFileProxy(toFolder1, "file12");
        assertTrue(toFile12.exists());
        VCSFileProxy toFile21 = VCSFileProxy.createFileProxy(toFolder2, "file21");
        assertTrue(toFile21.exists());
        VCSFileProxy toFile22 = VCSFileProxy.createFileProxy(toFolder2, "file22");
        assertTrue(toFile22.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder1).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder2).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile11).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile12).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile21).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile22).getTextStatus());

        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFolder1).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFolder2).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile11).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile12).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile21).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile22).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder1));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder2));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile11));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile12));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile21));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile22));

        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFolder1, FileInformation.STATUS_VERSIONED_UPTODATE);
        assertCachedStatus(toFolder2, FileInformation.STATUS_VERSIONED_UPTODATE);
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile11));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile12));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile21));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile22));

        assertTrue(getSVNStatus(toFolder).isCopied());
        assertTrue(getSVNStatus(toFolder1).isCopied());
        assertTrue(getSVNStatus(toFolder2).isCopied());
        assertTrue(getSVNStatus(toFile11).isCopied());
        assertTrue(getSVNStatus(toFile12).isCopied());
        assertTrue(getSVNStatus(toFile21).isCopied());
        assertTrue(getSVNStatus(toFile22).isCopied());

//        commit(wc);
        
    }

    public void copyVersionedFile2Repos_FO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc2, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        commit(wc2);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, "file");
        VCSFileProxySupport.createNew(fromFile);
        commit(wc);
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, "file");

        // copy
        copyFO(fromFile, toFile);

        // test
        assertTrue(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile));
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);

        assertFalse(getSVNStatus(toFile).isCopied());

//        commit(wc);
//        commit(wc2);
    }

    public void copyVersionedFolder2Repos_FO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy toFolderParent = VCSFileProxy.createFileProxy(wc2, "folderParent");
        VCSFileProxySupport.mkdirs(toFolderParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toFolderParent, fromFolder.getName());
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, "file");
        commit(wc2);

        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(fromFolder, toFile.getName());
        VCSFileProxySupport.createNew(fromFile);
        commit(wc);

        // copy
        copyFO(fromFolder, toFolder);

        // test
        assertTrue(fromFolder.exists());
        assertTrue(fromFile.exists());
        assertTrue(toFolder.exists());
        assertTrue(toFile.exists());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFolder));
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);

        assertFalse(getSVNStatus(toFolder).isCopied());
        assertFalse(getSVNStatus(toFile).isCopied());

//        commit(wc);
//        commit(wc2);
    }

    public void copyFileTree2Repos_FO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFolder1 = VCSFileProxy.createFileProxy(fromFolder, "folder1");
        VCSFileProxySupport.mkdirs(fromFolder1);
        VCSFileProxy fromFolder2 = VCSFileProxy.createFileProxy(fromFolder, "folder2");
        VCSFileProxySupport.mkdirs(fromFolder2);
        VCSFileProxy fromFile11 = VCSFileProxy.createFileProxy(fromFolder1, "file11");
        VCSFileProxySupport.createNew(fromFile11);
        VCSFileProxy fromFile12 = VCSFileProxy.createFileProxy(fromFolder1, "file12");
        VCSFileProxySupport.createNew(fromFile12);
        VCSFileProxy fromFile21 = VCSFileProxy.createFileProxy(fromFolder2, "file21");
        VCSFileProxySupport.createNew(fromFile21);
        VCSFileProxy fromFile22 = VCSFileProxy.createFileProxy(fromFolder2, "file22");
        VCSFileProxySupport.createNew(fromFile22);

        VCSFileProxy toFolderParent = VCSFileProxy.createFileProxy(wc2, "toFolder");
        VCSFileProxySupport.mkdirs(toFolderParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toFolderParent, fromFolder.getName());
        commit(wc);
        commit(wc2);

        // copy
        copyFO(fromFolder, toFolder);

        // test
        assertTrue(fromFolder.exists());
        assertTrue(toFolder.exists());
        VCSFileProxy toFolder1 = VCSFileProxy.createFileProxy(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        VCSFileProxy toFolder2 = VCSFileProxy.createFileProxy(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        VCSFileProxy toFile11 = VCSFileProxy.createFileProxy(toFolder1, "file11");
        assertTrue(toFile11.exists());
        VCSFileProxy toFile12 = VCSFileProxy.createFileProxy(toFolder1, "file12");
        assertTrue(toFile12.exists());
        VCSFileProxy toFile21 = VCSFileProxy.createFileProxy(toFolder2, "file21");
        assertTrue(toFile21.exists());
        VCSFileProxy toFile22 = VCSFileProxy.createFileProxy(toFolder2, "file22");
        assertTrue(toFile22.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder1).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFolder2).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile11).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile12).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile21).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fromFile22).getTextStatus());
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
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile11));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile12));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile21));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fromFile22));
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFolder1, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFolder2, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        assertCachedStatus(toFile11, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertCachedStatus(toFile12, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertCachedStatus(toFile21, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        assertCachedStatus(toFile22, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);

        assertFalse(getSVNStatus(fromFolder).isCopied());
        assertFalse(getSVNStatus(fromFolder1).isCopied());
        assertFalse(getSVNStatus(fromFolder2).isCopied());
        assertFalse(getSVNStatus(fromFile11).isCopied());
        assertFalse(getSVNStatus(fromFile12).isCopied());
        assertFalse(getSVNStatus(fromFile21).isCopied());
        assertFalse(getSVNStatus(fromFile22).isCopied());

//        commit(wc);
//        commit(wc2);

    }

    public void copyA2CB2A_FO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "A");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(wc, "folderB");
        VCSFileProxySupport.mkdirs(folderB);
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folderB, fileA.getName());
        VCSFileProxySupport.createNew(fileB);
        VCSFileProxy folderC = VCSFileProxy.createFileProxy(wc, "folderC");
        VCSFileProxySupport.mkdirs(folderC);
        commit(wc);

        VCSFileProxy fileC = VCSFileProxy.createFileProxy(folderC, fileA.getName());

        // copy
        copyFO(fileA, fileC);
        Thread.sleep(500);
        copyFO(fileB, fileA);

        // test
        assertTrue(fileA.exists());
        assertTrue(fileC.exists());
        assertTrue(fileB.exists());

        assertEquals(SVNStatusKind.REPLACED, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileB).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileC).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileB));
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileC));

        commit(wc);

        // test
        assertTrue(fileA.exists());
        assertTrue(fileC.exists());
        assertTrue(fileB.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileB).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileC).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileB));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileC));

        assertTrue(getSVNStatus(fileB).isCopied());
        assertTrue(getSVNStatus(fileA).isCopied());
    }

    public void renameAddedFile_DO() throws Exception {
        // init        
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "fromFile");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(wc, "toFile");
        
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
        
//        commit(wc);
    }
    
    public void moveAddedFile2UnversionedFolder_DO() throws Exception {
        // init        
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc, "toFodler");
        VCSFileProxySupport.mkdirs(toFolder);
        
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());
        
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
        
//        commit(wc);
    }
    
    public void moveVersionedFile2IgnoredFolder_DO() throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // add
        commit(fromFile);
        getClient().setIgnoredPatterns(wc, Collections.singletonList(toFolder.getName()));

        // move
        moveDO(fromFile, toFile);

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fromFile));
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_EXCLUDED);
    }
    
    public void moveAddedFile2VersionedFolder_DO() throws Exception {
        // init        
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc, "toFodler");
        VCSFileProxySupport.mkdirs(toFolder);
        commit(wc);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "fromFile");
        VCSFileProxySupport.createNew(fromFile);        
        
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());  
        
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
        
//        commit(wc);
    }

    public void renameA2B2A_DO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "from");
        VCSFileProxySupport.createNew(fileA);
        commit(wc);  
        
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(wc, "to");
        
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
        
//        commit(wc);
    }
    
    public void moveA2B2A_DO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "A");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy folder = VCSFileProxy.createFileProxy(wc, "folder");
        VCSFileProxySupport.mkdirs(folder);        
        commit(wc);  
        
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folder, fileA.getName());
        
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
        
//        commit(wc);
    }
    
    public void renameA2B2C_DO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "A");
        VCSFileProxySupport.createNew(fileA);        
        commit(wc);  
        
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(wc, "B");
        VCSFileProxy fileC = VCSFileProxy.createFileProxy(wc, "C");

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
        
//        commit(wc);
    }
    
    public void moveA2B2C_DO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "A");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(wc, "folderB");
        VCSFileProxySupport.mkdirs(folderB);
        VCSFileProxy folderC = VCSFileProxy.createFileProxy(wc, "folderC");
        VCSFileProxySupport.mkdirs(folderC);
        commit(wc);  
        
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folderB, fileA.getName());
        VCSFileProxy fileC = VCSFileProxy.createFileProxy(folderC, fileA.getName());
        
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
        
//        commit(wc);
    }

    public void renameA2B2C2A_DO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "A");
        VCSFileProxySupport.createNew(fileA);
        commit(wc);  
        
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(wc, "B");
        VCSFileProxy fileC = VCSFileProxy.createFileProxy(wc, "C");
        
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
        
//        commit(wc);
        
    }

    public void moveA2CB2A_DO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "A");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(wc, "folderB");
        VCSFileProxySupport.mkdirs(folderB);
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folderB, fileA.getName());
        VCSFileProxySupport.createNew(fileB);
        VCSFileProxy folderC = VCSFileProxy.createFileProxy(wc, "folderC");
        VCSFileProxySupport.mkdirs(folderC);
        commit(wc);

        VCSFileProxy fileC = VCSFileProxy.createFileProxy(folderC, fileA.getName());

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

        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileA));
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
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "A");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(wc, "folderB");
        VCSFileProxySupport.mkdirs(folderB);
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folderB, fileA.getName());
        VCSFileProxySupport.createNew(fileB);
        VCSFileProxy folderC = VCSFileProxy.createFileProxy(wc, "folderC");
        VCSFileProxySupport.mkdirs(folderC);
        commit(wc);

        VCSFileProxy fileC = VCSFileProxy.createFileProxy(folderC, fileA.getName());

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

        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileA));
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
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "A");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(wc, "B");
        VCSFileProxySupport.createNew(fileB);
        commit(wc);

        VCSFileProxy fileC = VCSFileProxy.createFileProxy(wc, "C");

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

        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileA));
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
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "A");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(wc, "B");
        VCSFileProxySupport.createNew(fileB);
        commit(wc);

        VCSFileProxy fileC = VCSFileProxy.createFileProxy(wc, "C");

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

        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileA));
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
    
    public void renameA2a_FO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "A");
        VCSFileProxySupport.createNew(fileA);
        commit(wc);

        VCSFileProxy fileB = VCSFileProxy.createFileProxy(wc, "a");

        // move
        renameFO(fileA, fileB);

        // test
        // test
        if (!Utilities.isMac() && !Utilities.isWindows()) {
            assertFalse(fileA.exists());
        }
        assertTrue(fileB.exists());

        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileB).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileB));
    }
    
    public void moveA2B2C2A_DO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "A");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(wc, "folderB");
        VCSFileProxySupport.mkdirs(folderB);
        VCSFileProxy folderC = VCSFileProxy.createFileProxy(wc, "folderC");
        VCSFileProxySupport.mkdirs(folderC);
        commit(wc);  
        
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folderB, fileA.getName());
        VCSFileProxy fileC = VCSFileProxy.createFileProxy(folderC, fileA.getName());
        
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
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "A");
        VCSFileProxySupport.createNew(fileA);
        commit(wc);  
        
        // rename
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(wc, "B");
        renameDO(fileA, fileB);
        
        // create from file
        fileA.getParentFile().toFileObject().createData(fileA.getName());
        
        // test 
        assertTrue(fileB.exists());
        assertTrue(fileA.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileB).getTextStatus());        
        
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        assertCachedStatus(fileB, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);                
        
//        commit(wc);
    }
    
    public void moveA2B_CreateA_DO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "file");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(wc, "folderB");
        VCSFileProxySupport.mkdirs(folderB);
        commit(wc);  
        
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folderB, fileA.getName());
        
        // move
        moveDO(fileA, fileB);
        Thread.sleep(500);
        
        // create from file
        fileA.getParentFile().toFileObject().createData(fileA.getName());
        
        // test 
        assertTrue(fileB.exists());
        assertTrue(fileA.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileB).getTextStatus());        
        
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        assertCachedStatus(fileB, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);                
        
//        commit(wc);
    }
    
    // fixed - see issue #129805
    public void deleteA_RenameB2A_DO_129805() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "A");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(wc, "B");
        VCSFileProxySupport.createNew(fileB);
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
             
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileB));
        
        commit(wc);

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));
    }
    
    public void renameVersionedFolder_DO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "from");
        VCSFileProxySupport.mkdirs(fromFolder);
        commit(wc);               
       
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc, "to");
        
        // rename       
        renameDO(fromFolder, toFolder);
        
        // test 
        assertFalse(fromFolder.exists());
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
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "from");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy toParent = VCSFileProxy.createFileProxy(wc, "toFolder");
        VCSFileProxySupport.mkdirs(toParent);
        commit(wc);               
        
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toParent, fromFolder.getName());
        
        // move
        moveDO(fromFolder, toFolder);
        
        // test 
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFolder).getTextStatus());        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fromFolder));
        assertCachedStatus(toFolder, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        commit(wc);
        assertFalse(fromFolder.exists());
    }    
    
    public void renameFileTree_DO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "from");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFolder1 = VCSFileProxy.createFileProxy(fromFolder, "folder1");
        VCSFileProxySupport.mkdirs(fromFolder1);
        VCSFileProxy fromFolder2 = VCSFileProxy.createFileProxy(fromFolder, "folder2");
        VCSFileProxySupport.mkdirs(fromFolder2);
        VCSFileProxy fromFile11 = VCSFileProxy.createFileProxy(fromFolder1, "file11");
        VCSFileProxySupport.createNew(fromFile11);
        VCSFileProxy fromFile12 = VCSFileProxy.createFileProxy(fromFolder1, "file12");
        VCSFileProxySupport.createNew(fromFile12);
        VCSFileProxy fromFile21 = VCSFileProxy.createFileProxy(fromFolder2, "file21");
        VCSFileProxySupport.createNew(fromFile21);
        VCSFileProxy fromFile22 = VCSFileProxy.createFileProxy(fromFolder2, "file22");
        VCSFileProxySupport.createNew(fromFile22);
        commit(wc);               
        
        // rename
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc, "to");
        renameDO(fromFolder, toFolder);
                                        
        // test 
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        VCSFileProxy toFolder1 = VCSFileProxy.createFileProxy(toFolder, "folder1");
        assertTrue(toFolder1.exists());
        VCSFileProxy toFolder2 = VCSFileProxy.createFileProxy(toFolder, "folder2");
        assertTrue(toFolder2.exists());
        VCSFileProxy toFile11 = VCSFileProxy.createFileProxy(toFolder1, "file11");
        assertTrue(toFile11.exists());
        VCSFileProxy toFile12 = VCSFileProxy.createFileProxy(toFolder1, "file12");
        assertTrue(toFile12.exists());
        VCSFileProxy toFile21 = VCSFileProxy.createFileProxy(toFolder2, "file21");
        assertTrue(toFile21.exists());
        VCSFileProxy toFile22 = VCSFileProxy.createFileProxy(toFolder2, "file22");
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
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "from");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFolder1 = VCSFileProxy.createFileProxy(fromFolder, "folder1");
        VCSFileProxySupport.mkdirs(fromFolder1);
        VCSFileProxy fromFolder2 = VCSFileProxy.createFileProxy(fromFolder, "folder2");
        VCSFileProxySupport.mkdirs(fromFolder2);
        VCSFileProxy fromFile11 = VCSFileProxy.createFileProxy(fromFolder1, "file11");
        VCSFileProxySupport.createNew(fromFile11);
        VCSFileProxy fromFile12 = VCSFileProxy.createFileProxy(fromFolder1, "file12");
        VCSFileProxySupport.createNew(fromFile12);
        VCSFileProxy fromFile21 = VCSFileProxy.createFileProxy(fromFolder2, "file21");
        VCSFileProxySupport.createNew(fromFile21);
        VCSFileProxy fromFile22 = VCSFileProxy.createFileProxy(fromFolder2, "file22");
        VCSFileProxySupport.createNew(fromFile22);

        VCSFileProxy toFolderParent = VCSFileProxy.createFileProxy(wc, "to");
        VCSFileProxySupport.mkdirs(toFolderParent);

        commit(wc);

        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toFolderParent, fromFolder.getName());

        // move
        moveDO(fromFolder, toFolder);

        // test         t.
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        VCSFileProxy toFolder1 = VCSFileProxy.createFileProxy(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        VCSFileProxy toFolder2 = VCSFileProxy.createFileProxy(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        VCSFileProxy toFile11 = VCSFileProxy.createFileProxy(toFolder1, "file11");
        assertTrue(toFile11.exists());
        VCSFileProxy toFile12 = VCSFileProxy.createFileProxy(toFolder1, "file12");
        assertTrue(toFile12.exists());
        VCSFileProxy toFile21 = VCSFileProxy.createFileProxy(toFolder2, "file21");
        assertTrue(toFile21.exists());
        VCSFileProxy toFile22 = VCSFileProxy.createFileProxy(toFolder2, "file22");
        assertTrue(toFile22.exists());

        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder1).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder2).getTextStatus());
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

        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fromFolder));
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fromFolder1));
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fromFolder2));
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

        assertFalse(fromFolder.exists());
        assertFalse(fromFolder1.exists());
        assertFalse(fromFolder2.exists());
        assertFalse(fromFile11.exists());
        assertFalse(fromFile12.exists());
        assertFalse(fromFile21.exists());
        assertFalse(fromFile22.exists());

    }

    public void renameVersionedFile_FO() throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "fromFile");
        VCSFileProxySupport.createNew(fromFile);
        commit(wc);                       
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(wc, "toFile");
        
        // rename    
        renameFO(fromFile, toFile);
        
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile).getTextStatus());        
        
        assertCachedStatus(fromFile, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(toFile, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);   
        
//        commit(wc);
    }

    public void moveVersionedFile_FO() throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        commit(wc);               
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());
        
        // move
        moveFO(fromFile, toFile);
        
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile).getTextStatus());        
        
        assertCachedStatus(fromFile, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(toFile, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);   
        
//        commit(wc);
    }
    
    public void renameUnversionedFile_FO() throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "fromFile");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(wc, "toFile");
                
        // rename
        renameFO(fromFile, toFile);
        
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFile).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());        
              
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFile));                
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        
//        commit(wc);
    }
    
    public void moveUnversionedFile_FO() throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());
        
        // rename
        moveFO(fromFile, toFile);
        
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFile).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());        
              
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFile));                
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        
//        commit(wc);
    }

    public void renameUnversionedFolder_FO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "fromFolder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc, "toFolder");
        
        // rename
        renameFO(fromFolder, toFolder);
        
        // test 
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFolder).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFolder).getTextStatus());        
              
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFolder));                
        assertCachedStatus(toFolder, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        
//        commit(wc);
    }
    
    public void moveUnversionedFolder_FO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "folder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy toParent = VCSFileProxy.createFileProxy(wc, "toFolder");
        VCSFileProxySupport.mkdirs(toParent);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toParent, fromFolder.getName());
        
        
        // move        
        moveFO(fromFolder, toFolder);
        
        // test 
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());

        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFolder).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFolder).getTextStatus());

        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFolder));
        assertCachedStatus(toFolder, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);

//        commit(wc);
    }
    
    public void renameAddedFile_FO() throws Exception {
        // init        
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "fromFile");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(wc, "toFile");
        
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
        
//        commit(wc);
    }
    
    public void moveAddedFile2UnversionedFolder_FO() throws Exception {
        // init        
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc, "toFodler");
        VCSFileProxySupport.mkdirs(toFolder);
        
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());
        
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
        
//        commit(wc);
    }
    
    public void moveVersionedFile2IgnoredFolder_FO() throws Exception {
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "file");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc, "toFolder");
        VCSFileProxySupport.mkdirs(toFolder);
        
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());

        // add
        commit(fromFile);
        getClient().setIgnoredPatterns(wc, Collections.singletonList(toFolder.getName()));

        // move
        moveFO(fromFile, toFile);

        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());

        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fromFile));
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_EXCLUDED);
    }
       
    public void moveAddedFile2VersionedFolder_FO() throws Exception {
        // init        
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc, "toFodler");
        VCSFileProxySupport.mkdirs(toFolder);
        commit(wc);
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "fromFile");
        VCSFileProxySupport.createNew(fromFile);        
        
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());  
        
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
        
//        commit(wc);
    }

    public void renameA2B2A_FO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "from");
        VCSFileProxySupport.createNew(fileA);
        commit(wc);  
        
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(wc, "to");
        
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
        
//        commit(wc);
    }
    
    public void moveA2B2A_FO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "A");
        assertFalse(fileA.exists());
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy folder = VCSFileProxy.createFileProxy(wc, "folder");
        assertFalse(folder.exists());
        VCSFileProxySupport.mkdirs(folder);        
        commit(wc);  
        
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folder, fileA.getName());
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
        
//        commit(wc);
    }

    public void renameA2B2C_FO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "A");
        VCSFileProxySupport.createNew(fileA);        
        commit(wc);  
        
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(wc, "B");
        VCSFileProxy fileC = VCSFileProxy.createFileProxy(wc, "C");

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
        
//        commit(wc);
    }
    
    public void moveA2B2C_FO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "A");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(wc, "folderB");
        VCSFileProxySupport.mkdirs(folderB);
        VCSFileProxy folderC = VCSFileProxy.createFileProxy(wc, "folderC");
        VCSFileProxySupport.mkdirs(folderC);
        commit(wc);  
        
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folderB, fileA.getName());
        VCSFileProxy fileC = VCSFileProxy.createFileProxy(folderC, fileA.getName());
        
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
        
//        commit(wc);
    }

    public void renameA2B2C2A_FO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "A");
        VCSFileProxySupport.createNew(fileA);
        commit(wc);  
        
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(wc, "B");
        VCSFileProxy fileC = VCSFileProxy.createFileProxy(wc, "C");
        
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
        
//        commit(wc);
        
    }        
    
    public void moveA2B2C2A_FO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "A");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(wc, "folderB");
        VCSFileProxySupport.mkdirs(folderB);
        VCSFileProxy folderC = VCSFileProxy.createFileProxy(wc, "folderC");
        VCSFileProxySupport.mkdirs(folderC);
        commit(wc);  
        
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folderB, fileA.getName());
        VCSFileProxy fileC = VCSFileProxy.createFileProxy(folderC, fileA.getName());
        
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
        
//        commit(wc);
        
    }        
    
    public void renameA2B_CreateA_FO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "A");
        VCSFileProxySupport.createNew(fileA);
        commit(wc);  
        
        // rename
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(wc, "B");
        renameFO(fileA, fileB);
        
        // create from file
        fileA.getParentFile().toFileObject().createData(fileA.getName());
        
        // test 
        assertTrue(fileB.exists());
        assertTrue(fileA.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileB).getTextStatus());        
        
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        assertCachedStatus(fileB, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);                
        
//        commit(wc);
    }
    
    public void moveA2B_CreateA_FO() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "file");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(wc, "folderB");
        VCSFileProxySupport.mkdirs(folderB);
        commit(wc);  
        
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folderB, fileA.getName());
        
        // move
        moveFO(fileA, fileB);
        Thread.sleep(500);
        
        // create from file
        fileA.getParentFile().toFileObject().createData(fileA.getName());
        
        // test 
        assertTrue(fileB.exists());
        assertTrue(fileA.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileB).getTextStatus());
        
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        assertCachedStatus(fileB, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);                
        
//        commit(wc);
    }
    
    // fixed - see issue #129805
    public void deleteA_RenameB2A_FO_129805() throws Exception {
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "A");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(wc, "B");
        VCSFileProxySupport.createNew(fileB);
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
             
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileB));
        
        commit(wc);

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));
    }
    
    //@ForAllEnvironments
    public void testIsModifiedAttributeFile () throws Exception {
        // file is outside of versioned space, attribute should be unknown
        
        FileObject fo = fs.createTempFile(fs.getTempFolder(), "testIsModifiedAttributeFile", "txt", true);
        String attributeModified = "ProvidedExtensions.VCSIsModified";
        
        Object attrValue = fo.getAttribute(attributeModified);
        assertNull(attrValue);
        
        // file inside a svn repo
        VCSFileProxy file = VCSFileProxy.createFileProxy(wc, "file");
        TestKit.write(file, "init");
        fo = file.toFileObject();
        // new file, returns TRUE
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.TRUE, attrValue);
        
        getClient().addFile(file);
        // added file, returns TRUE
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.TRUE, attrValue);
        commit(file);
        
        // unmodified file, returns FALSE
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.FALSE, attrValue);
        
        TestKit.write(file, "modification");
        // modified file, returns TRUE
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.TRUE, attrValue);
        
        TestKit.write(file, "init");
        // back to up to date
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.FALSE, attrValue);
    }
    
    public void renameVersionedFolder_FO() throws Exception {
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "from");
        VCSFileProxySupport.mkdirs(fromFolder);
        commit(wc);               
       
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc, "to");
        
        // rename       
        renameFO(fromFolder, toFolder);
        
        // test 
        assertFalse(fromFolder.exists());
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
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "from");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy toParent = VCSFileProxy.createFileProxy(wc, "toFolder");
        VCSFileProxySupport.mkdirs(toParent);
        commit(wc);               
        
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toParent, fromFolder.getName());
        
        // move
        moveFO(fromFolder, toFolder);
        
        // test 
        assertFalse(fromFolder.exists());
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
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "from");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFolder1 = VCSFileProxy.createFileProxy(fromFolder, "folder1");
        VCSFileProxySupport.mkdirs(fromFolder1);
        VCSFileProxy fromFolder2 = VCSFileProxy.createFileProxy(fromFolder, "folder2");
        VCSFileProxySupport.mkdirs(fromFolder2);
        VCSFileProxy fromFile11 = VCSFileProxy.createFileProxy(fromFolder1, "file11");
        VCSFileProxySupport.createNew(fromFile11);
        VCSFileProxy fromFile12 = VCSFileProxy.createFileProxy(fromFolder1, "file12");
        VCSFileProxySupport.createNew(fromFile12);
        VCSFileProxy fromFile21 = VCSFileProxy.createFileProxy(fromFolder2, "file21");
        VCSFileProxySupport.createNew(fromFile21);
        VCSFileProxy fromFile22 = VCSFileProxy.createFileProxy(fromFolder2, "file22");
        VCSFileProxySupport.createNew(fromFile22);
        commit(wc);               
        
        // rename
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc, "to");
        renameFO(fromFolder, toFolder);
                                        
        // test 
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        VCSFileProxy toFolder1 = VCSFileProxy.createFileProxy(toFolder, "folder1");
        assertTrue(toFolder1.exists());
        VCSFileProxy toFolder2 = VCSFileProxy.createFileProxy(toFolder, "folder2");
        assertTrue(toFolder2.exists());
        VCSFileProxy toFile11 = VCSFileProxy.createFileProxy(toFolder1, "file11");
        assertTrue(toFile11.exists());
        VCSFileProxy toFile12 = VCSFileProxy.createFileProxy(toFolder1, "file12");
        assertTrue(toFile12.exists());
        VCSFileProxy toFile21 = VCSFileProxy.createFileProxy(toFolder2, "file21");
        assertTrue(toFile21.exists());
        VCSFileProxy toFile22 = VCSFileProxy.createFileProxy(toFolder2, "file22");
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
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "from");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFolder1 = VCSFileProxy.createFileProxy(fromFolder, "folder1");
        VCSFileProxySupport.mkdirs(fromFolder1);
        VCSFileProxy fromFolder2 = VCSFileProxy.createFileProxy(fromFolder, "folder2");
        VCSFileProxySupport.mkdirs(fromFolder2);
        VCSFileProxy fromFile11 = VCSFileProxy.createFileProxy(fromFolder1, "file11");
        VCSFileProxySupport.createNew(fromFile11);
        VCSFileProxy fromFile12 = VCSFileProxy.createFileProxy(fromFolder1, "file12");
        VCSFileProxySupport.createNew(fromFile12);
        VCSFileProxy fromFile21 = VCSFileProxy.createFileProxy(fromFolder2, "file21");
        VCSFileProxySupport.createNew(fromFile21);
        VCSFileProxy fromFile22 = VCSFileProxy.createFileProxy(fromFolder2, "file22");
        VCSFileProxySupport.createNew(fromFile22);
        
        VCSFileProxy toFolderParent = VCSFileProxy.createFileProxy(wc, "to");
        VCSFileProxySupport.mkdirs(toFolderParent);
        
        commit(wc);               
        
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(toFolderParent, fromFolder.getName());
        
        // move
        moveFO(fromFolder, toFolder);
                                                
        // test         t.
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        VCSFileProxy toFolder1 = VCSFileProxy.createFileProxy(toFolder, fromFolder1.getName());
        assertTrue(toFolder1.exists());
        VCSFileProxy toFolder2 = VCSFileProxy.createFileProxy(toFolder, fromFolder2.getName());
        assertTrue(toFolder2.exists());
        VCSFileProxy toFile11 = VCSFileProxy.createFileProxy(toFolder1, "file11");
        assertTrue(toFile11.exists());
        VCSFileProxy toFile12 = VCSFileProxy.createFileProxy(toFolder1, "file12");
        assertTrue(toFile12.exists());
        VCSFileProxy toFile21 = VCSFileProxy.createFileProxy(toFolder2, "file21");
        assertTrue(toFile21.exists());
        VCSFileProxy toFile22 = VCSFileProxy.createFileProxy(toFolder2, "file22");
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
        final VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "file");
        wc.toFileObject().createData(fileA.getName());
        assertCachedStatus(fileA, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        commit(wc);
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));

        // rename
        VCSFileProxySupport.delete(fileA);
        Handler h = new SVNInterceptor();
        Subversion.LOG.addHandler(h);
        RequestProcessor.Task r = Subversion.getInstance().getParallelRequestProcessor().create(new Runnable() {
            @Override
            public void run() {
                fileA.toFileObject().refresh();
            }
        });
        r.run();
        assertFalse(fileA.exists());
        final VCSFileProxy fileB = VCSFileProxy.createFileProxy(wc, fileA.getName().toUpperCase());
        VCSFileProxySupport.createNew(fileB);
        Thread.sleep(3000);
        assertTrue(fileB.exists());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getStatus(fileB));
        Subversion.LOG.removeHandler(h);
    }
    
    public void deleteA_renameB2A2B_DO() throws Exception {
        // init
        VCSFileProxy folder = VCSFileProxy.createFileProxy(wc, "folder");
        VCSFileProxySupport.mkdir(folder);
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(folder, "A");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folder, "B");
        VCSFileProxySupport.createNew(fileB);
        commit(wc);
        
        //delete
        delete(fileA);
        assertFalse(fileA.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));
        // move
        renameDO(fileB, fileA);
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        assertEquals(SVNStatusKind.REPLACED, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileB).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileB));
        // move second
        renameDO(fileA, fileB);
        assertFalse(fileA.exists());
        assertTrue(fileB.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileB).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileB));
    }
    
    public void deleteA_renameUnversioned2A_DO() throws Exception {
        // init
        VCSFileProxy folder = VCSFileProxy.createFileProxy(wc, "folder");
        VCSFileProxySupport.mkdir(folder);
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(folder, "A");
        VCSFileProxySupport.createNew(fileA);
        commit(wc);
        
        VCSFileProxy fileUnversioned = VCSFileProxy.createFileProxy(folder, "Unversioned");
        VCSFileProxySupport.createNew(fileUnversioned);

        //delete
        delete(fileA);
        assertFalse(fileA.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));
        // move
        renameDO(fileUnversioned, fileA);

        // test
        assertTrue(fileA.exists());
        assertFalse(fileUnversioned.exists());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
    }
    
    public void deleteA_renameB2A2B_FO() throws Exception {
        // init
        VCSFileProxy folder = VCSFileProxy.createFileProxy(wc, "folder");
        VCSFileProxySupport.mkdir(folder);
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(folder, "A");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folder, "B");
        VCSFileProxySupport.createNew(fileB);
        commit(wc);
        
        //delete
        delete(fileA);
        assertFalse(fileA.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));
        // move
        renameFO(fileB, fileA);
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        assertEquals(SVNStatusKind.REPLACED, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileB).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileB));
        // move second
        renameFO(fileA, fileB);
        assertFalse(fileA.exists());
        assertTrue(fileB.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileB).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileB));
    }
    
    public void deleteA_renameUnversioned2A_FO() throws Exception {
        // init
        VCSFileProxy folder = VCSFileProxy.createFileProxy(wc, "folder");
        VCSFileProxySupport.mkdir(folder);
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(folder, "A");
        VCSFileProxySupport.createNew(fileA);
        commit(wc);
        
        VCSFileProxy fileUnversioned = VCSFileProxy.createFileProxy(folder, "Unversioned");
        VCSFileProxySupport.createNew(fileUnversioned);

        //delete
        delete(fileA);
        assertFalse(fileA.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));
        // move
        renameFO(fileUnversioned, fileA);

        // test
        assertTrue(fileA.exists());
        assertFalse(fileUnversioned.exists());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
    }
    
    public void deleteA_moveB2A2B_DO() throws Exception {
        // init
        VCSFileProxy folderA = VCSFileProxy.createFileProxy(wc, "folderA");
        VCSFileProxySupport.mkdir(folderA);
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(folderA, "f");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(wc, "folderB");
        VCSFileProxySupport.mkdir(folderB);
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folderB, "f");
        VCSFileProxySupport.createNew(fileB);
        commit(wc);
        
        //delete
        delete(fileA);
        assertFalse(fileA.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));
        // move
        moveDO(fileB, fileA);
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        assertEquals(SVNStatusKind.REPLACED, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileB).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileB));
        // move second
        moveDO(fileA, fileB);
        assertFalse(fileA.exists());
        assertTrue(fileB.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileB).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileB));
    }
    
    public void deleteA_moveUnversioned2A_DO() throws Exception {
        // init
        VCSFileProxy folderA = VCSFileProxy.createFileProxy(wc, "folderA");
        VCSFileProxySupport.mkdir(folderA);
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(folderA, "f");
        VCSFileProxySupport.createNew(fileA);
        commit(wc);
        
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(wc, "folderB");
        VCSFileProxySupport.mkdir(folderB);
        VCSFileProxy fileUnversioned = VCSFileProxy.createFileProxy(folderB, "f");
        VCSFileProxySupport.createNew(fileUnversioned);

        //delete
        delete(fileA);
        assertFalse(fileA.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));
        // move
        moveDO(fileUnversioned, fileA);

        // test
        assertTrue(fileA.exists());
        assertFalse(fileUnversioned.exists());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
    }
    
    public void deleteA_moveB2A2B_FO() throws Exception {
        // init
        VCSFileProxy folderA = VCSFileProxy.createFileProxy(wc, "folderA");
        VCSFileProxySupport.mkdir(folderA);
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(folderA, "f");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(wc, "folderB");
        VCSFileProxySupport.mkdir(folderB);
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folderB, "f");
        VCSFileProxySupport.createNew(fileB);
        commit(wc);
        
        //delete
        delete(fileA);
        assertFalse(fileA.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));
        // move
        moveFO(fileB, fileA);
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        assertEquals(SVNStatusKind.REPLACED, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileB).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileB));
        // move second
        moveFO(fileA, fileB);
        assertFalse(fileA.exists());
        assertTrue(fileB.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileB).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileB));
    }
    
    public void deleteA_moveUnversioned2A_FO() throws Exception {
        // init
        VCSFileProxy folderA = VCSFileProxy.createFileProxy(wc, "folderA");
        VCSFileProxySupport.mkdir(folderA);
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(folderA, "f");
        VCSFileProxySupport.createNew(fileA);
        commit(wc);
        
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(wc, "folderB");
        VCSFileProxySupport.mkdir(folderB);
        VCSFileProxy fileUnversioned = VCSFileProxy.createFileProxy(folderB, "f");
        VCSFileProxySupport.createNew(fileUnversioned);

        //delete
        delete(fileA);
        assertFalse(fileA.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));
        // move
        moveFO(fileUnversioned, fileA);

        // test
        assertTrue(fileA.exists());
        assertFalse(fileUnversioned.exists());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
    }
    
    public void deleteA_copyUnversioned2A_DO() throws Exception {
        // init
        VCSFileProxy folderA = VCSFileProxy.createFileProxy(wc, "folderA");
        VCSFileProxySupport.mkdir(folderA);
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(folderA, "f");
        VCSFileProxySupport.createNew(fileA);
        commit(wc);
        
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(wc, "folderB");
        VCSFileProxySupport.mkdir(folderB);
        VCSFileProxy fileUnversioned = VCSFileProxy.createFileProxy(folderB, "f");
        VCSFileProxySupport.createNew(fileUnversioned);

        //delete
        delete(fileA);
        assertFalse(fileA.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));
        // move
        copyDO(fileUnversioned, fileA);

        // test
        assertTrue(fileA.exists());
        assertTrue(fileUnversioned.exists());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
    }
    
    public void deleteA_copyUnversioned2A_FO() throws Exception {
        // init
        final VCSFileProxy folderA = VCSFileProxy.createFileProxy(wc, "folderA");
        VCSFileProxySupport.mkdir(folderA);

        VCSFileProxy fileA = VCSFileProxy.createFileProxy(folderA, "f");
        VCSFileProxySupport.createNew(fileA);
        commit(wc);
        
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(wc, "folderB");
        VCSFileProxySupport.mkdir(folderB);
        VCSFileProxy fileUnversioned = VCSFileProxy.createFileProxy(folderB, "f");
        VCSFileProxySupport.createNew(fileUnversioned);

        //delete
        delete(fileA);
        assertFalse(fileA.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));
        // move
        copyFO(fileUnversioned, fileA);

        // test
        assertTrue(fileA.exists());
        assertTrue(fileUnversioned.exists());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
    }
    
}
