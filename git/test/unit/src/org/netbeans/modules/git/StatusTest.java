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

package org.netbeans.modules.git;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.libs.git.progress.StatusProgressMonitor;
import org.netbeans.modules.git.FileInformation.Status;

/**
 *
 * @author ondra
 */
public class StatusTest extends AbstractGitTest {

    public StatusTest (String name) {
        super(name);
    }

    public void testStatusOnNoRepository () throws Exception {
        File folder = createFolder(repositoryLocation.getParentFile(), "folder");
        GitClient client = getClient(folder);
        Map<File, GitStatus> statuses = client.getStatus(new File[] { folder }, StatusProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(statuses.isEmpty());
    }

    public void testStatusDifferentTree () throws IOException {
        try {
            File folder = createFolder("folder");
            getCache().refreshAllRoots(Collections.singletonMap(repositoryLocation, Collections.singleton(folder)));
            fail("different tree, exception should be thrown");
        } catch (IllegalArgumentException ex) {
            // OK
        }
    }

    public void testCacheRefresh () throws Exception {
        FileStatusCache cache = getCache();
        File unversionedFile = createFile("file");
        // create new files
        Set<File> newFiles = new HashSet<File>();
        File newFile;
        newFiles.add(newFile = createFile(repositoryLocation, "file"));
        assertTrue(cache.getCachedStatus(unversionedFile).getStatus().equals(EnumSet.of(Status.STATUS_NOTVERSIONED_NOTMANAGED)));
        assertTrue(cache.getCachedStatus(newFile).getStatus().equals(EnumSet.of(Status.STATUS_VERSIONED_UPTODATE)));
        
        cache.refreshAllRoots(Collections.singletonMap(repositoryLocation, Collections.singleton(repositoryLocation)));
        assertSameStatus(newFiles, Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE);
        assertEquals(1, cache.listFiles(Collections.singleton(repositoryLocation), FileInformation.STATUS_LOCAL_CHANGES).length);
        assertTrue(cache.containsFiles(Collections.singleton(repositoryLocation), FileInformation.STATUS_LOCAL_CHANGES, true));

        newFiles.add(newFile = createFile(repositoryLocation, "file2"));
        assertTrue(cache.getCachedStatus(unversionedFile).getStatus().equals(EnumSet.of(Status.STATUS_NOTVERSIONED_NOTMANAGED)));
        assertTrue(cache.getCachedStatus(newFile).getStatus().equals(EnumSet.of(Status.STATUS_VERSIONED_UPTODATE)));
        cache.refreshAllRoots(Collections.singletonMap(repositoryLocation, Collections.singleton(repositoryLocation)));
        assertSameStatus(newFiles, Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE);
        assertEquals(2, cache.listFiles(Collections.singleton(repositoryLocation), FileInformation.STATUS_LOCAL_CHANGES).length);
        assertTrue(cache.containsFiles(Collections.singleton(repositoryLocation), FileInformation.STATUS_LOCAL_CHANGES, true));

        // try refresh on a single file, other statuses should not change
        newFiles.add(newFile = createFile(repositoryLocation, "file3"));
        assertTrue(cache.getCachedStatus(unversionedFile).getStatus().equals(EnumSet.of(Status.STATUS_NOTVERSIONED_NOTMANAGED)));
        assertTrue(cache.getCachedStatus(newFile).getStatus().equals(EnumSet.of(Status.STATUS_VERSIONED_UPTODATE)));
        cache.refreshAllRoots(Collections.singletonMap(repositoryLocation, Collections.singleton(newFile)));
        assertSameStatus(newFiles, Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE);
        assertEquals(3, cache.listFiles(Collections.singleton(repositoryLocation), FileInformation.STATUS_LOCAL_CHANGES).length);
        assertTrue(cache.containsFiles(Collections.singleton(repositoryLocation), FileInformation.STATUS_LOCAL_CHANGES, true));

        // try refresh on a subfolder file, other statuses should not change
        File folder = createFolder(repositoryLocation, "folder");
        assertTrue(cache.getCachedStatus(unversionedFile).getStatus().equals(EnumSet.of(Status.STATUS_NOTVERSIONED_NOTMANAGED)));
        cache.refreshAllRoots(Collections.singletonMap(repositoryLocation, Collections.singleton(folder)));
        assertSameStatus(newFiles, Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE);
        assertEquals(3, cache.listFiles(Collections.singleton(repositoryLocation), FileInformation.STATUS_LOCAL_CHANGES).length);
        assertTrue(cache.containsFiles(Collections.singleton(repositoryLocation), FileInformation.STATUS_LOCAL_CHANGES, true));

        // try refresh on a subfolder file, other statuses should not change
        newFiles.add(newFile = createFile(folder, "file4"));
        assertTrue(cache.getCachedStatus(unversionedFile).getStatus().equals(EnumSet.of(Status.STATUS_NOTVERSIONED_NOTMANAGED)));
        assertTrue(cache.getCachedStatus(newFile).getStatus().equals(EnumSet.of(Status.STATUS_VERSIONED_UPTODATE)));
        cache.refreshAllRoots(Collections.singletonMap(repositoryLocation, Collections.singleton(newFile)));
        assertSameStatus(newFiles, Status.STATUS_NOTVERSIONED_NEW_IN_WORKING_TREE);
        assertEquals(4, cache.listFiles(Collections.singleton(repositoryLocation), FileInformation.STATUS_LOCAL_CHANGES).length);
        assertTrue(cache.containsFiles(Collections.singleton(repositoryLocation), FileInformation.STATUS_LOCAL_CHANGES, true));
    }
    
    // TODO add more tests when add is implemented
    // TODO add more tests when remove is implemented
    // TODO add more tests when commit is implemented
    // TODO add more tests when exclusions are supported
    // TODO test statuses between HEAD-WC: when commit is implemented

    private void assertSameStatus(Set<File> files, Status status) {
        for (File f : files) {
            assertTrue(getCache().getCachedStatus(f).getStatus().equals(EnumSet.of(status)));
        }
    }
}
