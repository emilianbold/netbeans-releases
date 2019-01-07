/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.subversion.remote;

import junit.framework.Test;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ClassForAllEnvironments;
import static org.netbeans.modules.subversion.remote.RemoteVersioningTestBase.addTest;
import org.netbeans.modules.subversion.remote.api.SVNStatusKind;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.util.Utilities;

/**
 *
 * 
 */
@ClassForAllEnvironments(section = "remote.svn")
public class InterceptorRenameFOTest extends RemoteVersioningTestBase {

    public InterceptorRenameFOTest(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }
    
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        addTest(suite, InterceptorRenameFOTest.class, "renameVersionedFile_FO");
        addTest(suite, InterceptorRenameFOTest.class, "renameUnversionedFile_FO");
        addTest(suite, InterceptorRenameFOTest.class, "renameUnversionedFolder_FO");
        addTest(suite, InterceptorRenameFOTest.class, "renameAddedFile_FO");
        addTest(suite, InterceptorRenameFOTest.class, "renameA2B2A_FO");
        addTest(suite, InterceptorRenameFOTest.class, "renameA2B2C_FO");
        addTest(suite, InterceptorRenameFOTest.class, "renameA2B2C2A_FO");
        addTest(suite, InterceptorRenameFOTest.class, "renameA2B_CreateA_FO");
        addTest(suite, InterceptorRenameFOTest.class, "deleteA_RenameB2A_FO_129805"); // failed
        addTest(suite, InterceptorRenameFOTest.class, "renameVersionedFolder_FO");
        addTest(suite, InterceptorRenameFOTest.class, "renameFileTree_FO"); // failed
        addTest(suite, InterceptorRenameFOTest.class, "renameA2CB2A_FO");
        addTest(suite, InterceptorRenameFOTest.class, "renameA2a_FO");
        addTest(suite, InterceptorRenameFOTest.class, "deleteA_renameB2A2B_FO"); // failed
        addTest(suite, InterceptorRenameFOTest.class, "deleteA_renameUnversioned2A_FO"); // failed
        return(suite);
    }
    
    public void renameVersionedFile_FO() throws Exception {
        if (skipTest()) {
            return;
        }
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

    public void renameUnversionedFile_FO() throws Exception {
        if (skipTest()) {
            return;
        }
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
    
    public void renameUnversionedFolder_FO() throws Exception {
        if (skipTest()) {
            return;
        }
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
    
    public void renameAddedFile_FO() throws Exception {
        if (skipTest()) {
            return;
        }
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
    
    public void renameA2B2A_FO() throws Exception {
        if (skipTest()) {
            return;
        }
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
    
    public void renameA2B2C_FO() throws Exception {
        if (skipTest()) {
            return;
        }
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
    
    public void renameA2B2C2A_FO() throws Exception {
        if (skipTest()) {
            return;
        }
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
    
    public void renameA2B_CreateA_FO() throws Exception {
        if (skipTest()) {
            return;
        }
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
    
    // fixed - see issue #129805
    public void deleteA_RenameB2A_FO_129805() throws Exception {
        if (skipTest()) {
            return;
        }
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
    
    public void renameVersionedFolder_FO() throws Exception {
        if (skipTest()) {
            return;
        }
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

    public void renameFileTree_FO() throws Exception {
        if (skipTest()) {
            return;
        }
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
    
    public void renameA2CB2A_FO() throws Exception {
        if (skipTest()) {
            return;
        }
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
        if (skipTest()) {
            return;
        }
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
    
    public void deleteA_renameB2A2B_FO() throws Exception {
        if (skipTest()) {
            return;
        }
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
        if (skipTest()) {
            return;
        }
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
}
