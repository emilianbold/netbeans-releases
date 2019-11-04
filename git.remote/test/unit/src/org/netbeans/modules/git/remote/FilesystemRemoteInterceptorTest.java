/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.git.remote;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Set;
import junit.framework.Test;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.git.remote.FileInformation.Status;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ClassForAllEnvironments;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileObject;

/**
 *
 */
@ClassForAllEnvironments(section = "remote.svn")
public class FilesystemRemoteInterceptorTest extends AbstractRemoteGitTestCase {

    public static final String PROVIDED_EXTENSIONS_REMOTE_LOCATION = "ProvidedExtensions.RemoteLocation";

    public FilesystemRemoteInterceptorTest(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }
    
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        addTest(suite, FilesystemRemoteInterceptorTest.class, "seenRootsLogin");
        addTest(suite, FilesystemRemoteInterceptorTest.class, "getWrongAttribute");
        addTest(suite, FilesystemRemoteInterceptorTest.class, "getRemoteLocationAttribute");
        addTest(suite, FilesystemRemoteInterceptorTest.class, "modifyVersionedFile");
        addTest(suite, FilesystemRemoteInterceptorTest.class, "isModifiedAttributeFile");
        return(suite);
    }
    
    @Override
    protected boolean isFailed() {
        return Arrays.asList().contains(testName);
    }

    @Override
    protected boolean isRunAll() {return false;}

    public void seenRootsLogin () throws Exception {
        if (skipTest()) {
            return;
        }
        VCSFileProxy folderA = VCSFileProxy.createFileProxy(repositoryLocation, "folderA");
        VCSFileProxy fileA1 = VCSFileProxy.createFileProxy(folderA, "file1");
        VCSFileProxy fileA2 = VCSFileProxy.createFileProxy(folderA, "file2");
        VCSFileProxySupport.mkdirs(folderA);
        VCSFileProxySupport.createNew(fileA1);
        VCSFileProxySupport.createNew(fileA2);
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(repositoryLocation, "folderB");
        VCSFileProxy fileB1 = VCSFileProxy.createFileProxy(folderB, "file1");
        VCSFileProxy fileB2 = VCSFileProxy.createFileProxy(folderB, "file2");
        VCSFileProxySupport.mkdirs(folderB);
        VCSFileProxySupport.createNew(fileB1);
        VCSFileProxySupport.createNew(fileB2);
        VCSFileProxy folderC = VCSFileProxy.createFileProxy(repositoryLocation, "folderC");
        VCSFileProxy fileC1 = VCSFileProxy.createFileProxy(folderC, "file1");
        VCSFileProxy fileC2 = VCSFileProxy.createFileProxy(folderC, "file2");
        VCSFileProxySupport.mkdirs(folderC);
        VCSFileProxySupport.createNew(fileC1);
        VCSFileProxySupport.createNew(fileC2);

        FilesystemInterceptor interceptor = Git.getInstance().getVCSInterceptor();
        Field f = FilesystemInterceptor.class.getDeclaredField("gitFolderEventsHandler");
        f.setAccessible(true);
        Object hgFolderEventsHandler = f.get(interceptor);
        f = hgFolderEventsHandler.getClass().getDeclaredField("seenRoots");
        f.setAccessible(true);
        HashMap<VCSFileProxy, Set<VCSFileProxy>> map = (HashMap) f.get(hgFolderEventsHandler);

        LogHandler handler = new LogHandler();
        Git.STATUS_LOG.addHandler(handler);
        handler.setFilesToInitializeRoots(folderA);
        interceptor.pingRepositoryRootFor(folderA);
        // some time for bg threads
        assertTrue(handler.waitForFilesToInitializeRoots());
        Set<VCSFileProxy> files = map.get(repositoryLocation);
        assertEquals(1, files.size());
        assertTrue(files.contains(folderA));
        handler.setFilesToInitializeRoots(fileA1);
        interceptor.pingRepositoryRootFor(fileA1);
        // some time for bg threads
        assertTrue(handler.waitForFilesToInitializeRoots());
        files = map.get(repositoryLocation);
        assertEquals(1, files.size());
        assertTrue(files.contains(folderA));

        handler.setFilesToInitializeRoots(fileB1);
        interceptor.pingRepositoryRootFor(fileB1);
        // some time for bg threads
        assertTrue(handler.waitForFilesToInitializeRoots());
        assertEquals(2, files.size());
        assertTrue(files.contains(folderA));
        assertTrue(files.contains(fileB1));

        handler.setFilesToInitializeRoots(fileB2);
        interceptor.pingRepositoryRootFor(fileB2);
        // some time for bg threads
        assertTrue(handler.waitForFilesToInitializeRoots());
        assertEquals(3, files.size());
        assertTrue(files.contains(folderA));
        assertTrue(files.contains(fileB1));
        assertTrue(files.contains(fileB2));

        handler.setFilesToInitializeRoots(folderC);
        interceptor.pingRepositoryRootFor(folderC);
        // some time for bg threads
        assertTrue(handler.waitForFilesToInitializeRoots());
        assertEquals(4, files.size());
        assertTrue(files.contains(folderA));
        assertTrue(files.contains(fileB1));
        assertTrue(files.contains(fileB2));
        assertTrue(files.contains(folderC));

        handler.setFilesToInitializeRoots(folderB);
        interceptor.pingRepositoryRootFor(folderB);
        // some time for bg threads
        assertTrue(handler.waitForFilesToInitializeRoots());
        assertEquals(3, files.size());
        assertTrue(files.contains(folderA));
        assertTrue(files.contains(folderB));
        assertTrue(files.contains(folderC));

        handler.setFilesToInitializeRoots(repositoryLocation);
        interceptor.pingRepositoryRootFor(repositoryLocation);
        // some time for bg threads
        assertTrue(handler.waitForFilesToInitializeRoots());
        Git.STATUS_LOG.removeHandler(handler);
        assertEquals(1, files.size());
        assertTrue(files.contains(repositoryLocation));
    }

    public void getWrongAttribute () throws Exception {
        if (skipTest()) {
            return;
        }
        VCSFileProxy file = VCSFileProxy.createFileProxy(repositoryLocation, "attrfile");
        VCSFileProxySupport.createNew(file);
        FileObject fo = file.toFileObject();

        String str = (String) fo.getAttribute("peek-a-boo");
        assertNull(str);
    }

    public void getRemoteLocationAttribute () throws Exception {
        if (skipTest()) {
            return;
        }
        VCSFileProxy file = VCSFileProxy.createFileProxy(repositoryLocation, "attrfile");
        VCSFileProxySupport.createNew(file);
        FileObject fo = file.toFileObject();

        String str = (String) fo.getAttribute(PROVIDED_EXTENSIONS_REMOTE_LOCATION);
        // TODO implement getRemoteRepositoryURL
//        assertNotNull(str);
//        assertEquals(repositoryLocation.getAbsolutePath().toString(), str);
    }

    public void modifyVersionedFile () throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy file = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        refreshHandler.setFilesToRefresh(Collections.singleton(file));
        VCSFileProxySupport.createNew(file);
        add();
        commit();
        FileObject fo = file.normalizeFile().toFileObject();
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(file).getStatus());
        assertTrue(refreshHandler.waitForFilesToRefresh());

        refreshHandler.setFilesToRefresh(Collections.singleton(file));
        PrintWriter pw = new PrintWriter(fo.getOutputStream());
        pw.println("hello new file");
        pw.close();
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertEquals(EnumSet.of(Status.MODIFIED_HEAD_WORKING_TREE, Status.MODIFIED_INDEX_WORKING_TREE), getCache().getStatus(file).getStatus());
    }
    
    public void isModifiedAttributeFile () throws Exception {
        if (skipTest()) {
            return;
        }
        // file is outside of versioned space, attribute should be unknown
        VCSFileProxy file = VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(getWorkDir()), "file");
        VCSFileProxySupport.createNew(file);
        FileObject fo = file.normalizeFile().toFileObject();
        String attributeModified = "ProvidedExtensions.VCSIsModified";
        
        Object attrValue = fo.getAttribute(attributeModified);
        assertNull(attrValue);
        
        // file inside a git repo
        file = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        write(file, "init");
        fo = file.toFileObject();
        // new file, returns TRUE
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.TRUE, attrValue);
        
        add();
        // added file, returns TRUE
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.TRUE, attrValue);
        commit();
        
        // unmodified file, returns FALSE
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.FALSE, attrValue);
        
        write(file, "modification");
        // modified file, returns TRUE
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.TRUE, attrValue);
        
        write(file, "init");
        // back to up to date
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.FALSE, attrValue);
    }

}
