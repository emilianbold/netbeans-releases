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

import java.io.IOException;
import java.util.logging.Handler;
import static junit.framework.Assert.assertEquals;
import junit.framework.Test;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ClassForAllEnvironments;
import static org.netbeans.modules.subversion.remote.RemoteVersioningTestBase.addTest;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.api.SVNStatusKind;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.openide.util.RequestProcessor;

/**
 *
 * 
 */
@ClassForAllEnvironments(section = "remote.svn")
public class InterceptorDeleteTest extends RemoteVersioningTestBase {

    public InterceptorDeleteTest(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }
    
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        addTest(suite, InterceptorDeleteTest.class, "deleteCreateChangeCase_issue_157373");
        addTest(suite, InterceptorDeleteTest.class, "deleteNotVersionedFile");
        addTest(suite, InterceptorDeleteTest.class, "deleteVersionedFileExternally");
        addTest(suite, InterceptorDeleteTest.class, "deleteVersionedFile");
        addTest(suite, InterceptorDeleteTest.class, "deleteVersionedFolder");
        addTest(suite, InterceptorDeleteTest.class, "deleteNotVersionedFolder");
        addTest(suite, InterceptorDeleteTest.class, "deleteWCRoot");
        addTest(suite, InterceptorDeleteTest.class, "deleteVersionedFileTree");
        addTest(suite, InterceptorDeleteTest.class, "deleteNotVersionedFileTree");
        return(suite);
    }
        
    public void deleteCreateChangeCase_issue_157373 () throws Exception {
        if (skipTest()) {
            return;
        }
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
                VersioningSupport.refreshFor(new VCSFileProxy[]{fileA});
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
    
    public void deleteNotVersionedFile() throws Exception {
        if (skipTest()) {
            return;
        }
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
    
    public void deleteVersionedFileExternally() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy file = VCSFileProxy.createFileProxy(wc, "file");
        wc.toFileObject().createData(file.getName());
        assertCachedStatus(file, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        commit(wc);
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(file));

        String prop = System.getProperty("org.netbeans.modules.subversion.deleteMissingFiles", "");
        try {
            System.setProperty("org.netbeans.modules.subversion.deleteMissingFiles", "true");
            // delete externally
            VCSFileProxySupport.deleteExternally(file);

            // test
            assertFalse(file.exists());
            assertEquals(SVNStatusKind.MISSING, getSVNStatus(file).getTextStatus());

            // notify changes
            VersioningSupport.refreshFor(new VCSFileProxy[]{file});
            assertCachedStatus(file, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        } finally {
            System.setProperty("org.netbeans.modules.subversion.deleteMissingFiles", prop);
        }
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(file).getTextStatus());
        commit(wc);
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file).getTextStatus());
    }

    public void deleteVersionedFile() throws Exception {
        if (skipTest()) {
            return;
        }
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

    public void deleteVersionedFolder() throws Exception {
        if (skipTest()) {
            return;
        }
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
        if (skipTest()) {
            return;
        }
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
        if (skipTest()) {
            return;
        }
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
        if (skipTest()) {
            return;
        }
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
        if (skipTest()) {
            return;
        }
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
}
