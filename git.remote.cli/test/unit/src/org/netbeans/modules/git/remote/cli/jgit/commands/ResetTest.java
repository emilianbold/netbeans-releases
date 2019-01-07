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

package org.netbeans.modules.git.remote.cli.jgit.commands;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import org.netbeans.modules.git.remote.cli.GitClient;
import org.netbeans.modules.git.remote.cli.GitClient.ResetType;
import org.netbeans.modules.git.remote.cli.GitRevisionInfo;
import org.netbeans.modules.git.remote.cli.GitStatus;
import org.netbeans.modules.git.remote.cli.GitStatus.Status;
import org.netbeans.modules.git.remote.cli.SearchCriteria;
import org.netbeans.modules.git.remote.cli.jgit.AbstractGitTestCase;
import org.netbeans.modules.git.remote.cli.jgit.JGitRepository;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 */
public class ResetTest extends AbstractGitTestCase {
    private VCSFileProxy workDir;
    private JGitRepository repository;

    public ResetTest (String testName) throws IOException {
        super(testName);
    }
    
    @Override
    protected boolean isFailed() {
        return Arrays.asList("testResetHardTypeConflict").contains(getName());
    }
    
    @Override
    protected boolean isRunAll() {return false;}

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getLocalGitRepository();
    }

    public void testResetSoft () throws Exception {
        VCSFileProxy file1 = VCSFileProxy.createFileProxy(workDir, "file1");
        write(file1, "blablablabla");
        VCSFileProxy file2 = VCSFileProxy.createFileProxy(workDir, "file2");
        write(file2, "blablablabla in file2");
        VCSFileProxy[] files = new VCSFileProxy[] { file1, file2 };
        add(files);
        commit(files);

        GitClient client = getClient(workDir);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(files,NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        write(file1, "change in content");
        add(file1);
        commit(files);
        GitRevisionInfo[] logs = client.log(new SearchCriteria(), NULL_PROGRESS_MONITOR);
        String revision = logs[1].getRevision();
        client.reset(revision, ResetType.SOFT, NULL_PROGRESS_MONITOR);

        logs = client.log(new SearchCriteria(), NULL_PROGRESS_MONITOR);
        assertEquals(revision, logs[0].getRevision());
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
    }

    public void testResetMixed () throws Exception {
        VCSFileProxy file1 = VCSFileProxy.createFileProxy(workDir, "file1");
        write(file1, "blablablabla");
        VCSFileProxy file2 = VCSFileProxy.createFileProxy(workDir, "file2");
        write(file2, "blablablabla in file2");
        VCSFileProxy[] files = new VCSFileProxy[] { file1, file2 };
        add(files);
        commit(files);

        GitClient client = getClient(workDir);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(files,NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        write(file1, "change in content");
        add(file1);
        commit(files);

        GitRevisionInfo[] logs = client.log(new SearchCriteria(), NULL_PROGRESS_MONITOR);
        String revision = logs[1].getRevision();
        client.reset(revision, ResetType.MIXED, NULL_PROGRESS_MONITOR);

        logs = client.log(new SearchCriteria(), NULL_PROGRESS_MONITOR);
        assertEquals(revision, logs[0].getRevision());
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
    }

    public void testResetHard () throws Exception {
        VCSFileProxy file1 = VCSFileProxy.createFileProxy(workDir, "file1");
        write(file1, "blablablabla");
        VCSFileProxy file2 = VCSFileProxy.createFileProxy(workDir, "file2");
        write(file2, "blablablabla in file2");
        VCSFileProxy[] files = new VCSFileProxy[] { file1, file2 };
        add(files);
        commit(files);

        GitClient client = getClient(workDir);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(files,NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        write(file1, "change in content");
        add(file1);
        commit(files);

        GitRevisionInfo[] logs = client.log(new SearchCriteria(), NULL_PROGRESS_MONITOR);
        String revision = logs[1].getRevision();
        long ts = file2.lastModified();
        Thread.sleep(1000);
        client.reset(revision, ResetType.HARD, NULL_PROGRESS_MONITOR);
        assertEquals(ts, file2.lastModified());

        logs = client.log(new SearchCriteria(), NULL_PROGRESS_MONITOR);
        assertEquals(revision, logs[0].getRevision());
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
    }

    public void testResetHardTypeConflict () throws Exception {
        VCSFileProxy file1 = VCSFileProxy.createFileProxy(workDir, "file1");
        write(file1, "blablablabla");
        VCSFileProxy file2 = VCSFileProxy.createFileProxy(file1, "f");
        VCSFileProxy[] files = new VCSFileProxy[] { file1, file2 };
        add(files);
        commit(files);

        GitClient client = getClient(workDir);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(files,NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        client.remove(files, false, NULL_PROGRESS_MONITOR);
        VCSFileProxySupport.mkdirs(file1);
        assertTrue(file1.isDirectory());
        write(file2, "ssss");
        write(VCSFileProxy.createFileProxy(file1, "untracked"), "ssss");
        add(file2);
        commit(files);

        GitRevisionInfo[] logs = client.log(new SearchCriteria(), NULL_PROGRESS_MONITOR);
        String currentRevision = logs[0].getRevision();
        String revision = logs[1].getRevision();
        client.reset(revision, ResetType.HARD, NULL_PROGRESS_MONITOR);

        logs = client.log(new SearchCriteria(), NULL_PROGRESS_MONITOR);
        assertEquals(revision, logs[0].getRevision());
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertTrue(file1.isFile());

        client.reset(currentRevision, ResetType.HARD, NULL_PROGRESS_MONITOR);

        logs = client.log(new SearchCriteria(), NULL_PROGRESS_MONITOR);
        assertEquals(currentRevision, logs[0].getRevision());
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertTrue(file1.isDirectory());
    }

    public void testResetHardOverwritesModification () throws Exception {
        VCSFileProxy file1 = VCSFileProxy.createFileProxy(workDir, "file1");
        write(file1, "blablablabla");
        VCSFileProxy[] files = new VCSFileProxy[] { file1 };
        add(files);
        commit(files);

        GitClient client = getClient(workDir);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(files,NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        write(file1, "change in content");
        add(file1);
        commit(files);
        write(file1, "hello, i have local modifications");

        GitRevisionInfo[] logs = client.log(new SearchCriteria(), NULL_PROGRESS_MONITOR);
        String revision = logs[1].getRevision();
        client.reset(revision, ResetType.HARD, NULL_PROGRESS_MONITOR);

        logs = client.log(new SearchCriteria(), NULL_PROGRESS_MONITOR);
        assertEquals(revision, logs[0].getRevision());
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertEquals("blablablabla", read(file1));
    }

    public void testResetHardRemoveFile () throws Exception {
        VCSFileProxy file1 = VCSFileProxy.createFileProxy(workDir, "file1");
        write(file1, "blablablabla");
        VCSFileProxy file2 = VCSFileProxy.createFileProxy(workDir, "file2");
        write(file2, "blablablabla");
        VCSFileProxy[] files = new VCSFileProxy[] { file1 };
        add(files);
        commit(files);

        GitClient client = getClient(workDir);
        add(file2);
        files = new VCSFileProxy[] { file1, file2 };
        commit(files);

        GitRevisionInfo[] logs = client.log(new SearchCriteria(), NULL_PROGRESS_MONITOR);
        String revision = logs[1].getRevision();
        assertTrue(file2.exists());
        client.reset(revision, ResetType.HARD, NULL_PROGRESS_MONITOR);

        logs = client.log(new SearchCriteria(), NULL_PROGRESS_MONITOR);
        assertEquals(revision, logs[0].getRevision());
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertFalse(file2.exists());
    }

    public void testResetPaths () throws Exception {
        VCSFileProxy file1 = VCSFileProxy.createFileProxy(workDir, "file1"); // index entry will be modified
        write(file1, "blablablabla");
        VCSFileProxy file2 = VCSFileProxy.createFileProxy(workDir, "file2"); // index entry will be left alone
        write(file2, "blablablabla in file2");
        VCSFileProxy file3 = VCSFileProxy.createFileProxy(workDir, "file3"); // index entry will be added
        write(file3, "blablablabla in file3");
        VCSFileProxy file4 = VCSFileProxy.createFileProxy(workDir, "file4"); // index entry will be removed
        VCSFileProxy[] files = new VCSFileProxy[] { file1, file2, file3, file4 };
        add(files);
        commit(files);

        GitClient client = getClient(workDir);

        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertEquals(3, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file3, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        String content = "change in content";
        write(file1, content);
        write(file4, "blablablabla in file4");
        client.add(files, NULL_PROGRESS_MONITOR);
        commit(files);
        write(file2, "change in content in file 2");
        client.add(new VCSFileProxy[] { file2 }, NULL_PROGRESS_MONITOR);
        client.remove(new VCSFileProxy[] { file3 }, false,NULL_PROGRESS_MONITOR);

        GitRevisionInfo[] logs = client.log(new SearchCriteria(), NULL_PROGRESS_MONITOR);
        String revision = logs[1].getRevision();
        client.reset(new VCSFileProxy[] { file1, file3, file4 }, revision, true, NULL_PROGRESS_MONITOR);

        // file1: modified HEAD-INDEX
        // file2: stays modified HEAD-INDEX
        // file3: removed in WT, normal HEAD-INDEX
        // file4: removed in index, normal in WT

        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertEquals(4, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file3, true, Status.STATUS_NORMAL, Status.STATUS_REMOVED, Status.STATUS_REMOVED, false);
if(false)assertStatus(statuses, workDir, file4, true, Status.STATUS_REMOVED, Status.STATUS_ADDED, Status.STATUS_NORMAL, false);
else     assertStatus(statuses, workDir, file4, true, Status.STATUS_REMOVED, Status.STATUS_ADDED, Status.STATUS_MODIFIED, false);
        assertEquals(content, read(file1));
    }
    
    public void testResetPaths_NonRecursive () throws Exception {
        VCSFileProxy folder = VCSFileProxy.createFileProxy(workDir, "folder");
        VCSFileProxySupport.mkdirs(folder);
        VCSFileProxy file1 = VCSFileProxy.createFileProxy(folder, "file1"); // index entry will be modified
        write(file1, "blablablabla");
        VCSFileProxy subfolder = VCSFileProxy.createFileProxy(folder, "subfolder");
        VCSFileProxySupport.mkdirs(subfolder);
        VCSFileProxy file2 = VCSFileProxy.createFileProxy(subfolder, "file2"); // index entry will be left alone
        write(file2, "blablablabla in file2");
        VCSFileProxy[] files = new VCSFileProxy[] { file1, file2 };
        add(files);
        commit(files);

        GitClient client = getClient(workDir);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
        String content = "change in content";
        write(file1, content);
        write(file2, content);
        client.add(files, NULL_PROGRESS_MONITOR);
        
        // children
        client.reset(new VCSFileProxy[] { folder }, "HEAD", false, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false);
if(false)assertStatus(statuses, workDir, file2, true, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, false);
else     assertStatus(statuses, workDir, file2, true, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false);

        write(file1, content);
        // recursive
        client.reset(new VCSFileProxy[] { folder }, "HEAD", true, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, file2, true, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false);
        
        write(file1, content);
        add(file1);
        // non recursive on file
        client.reset(new VCSFileProxy[] { file1 }, "HEAD", false, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, file1, true, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false);
    }

    public void testResetPathsChangeType () throws Exception {
        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "f"); // index entry will be modified
        VCSFileProxy file2 = VCSFileProxy.createFileProxy(file, "file");
        write(file, "blablablabla");
        VCSFileProxy[] files = new VCSFileProxy[] { file, file2 };
        add(files);
        commit(files);

        GitClient client = getClient(workDir);
        client.remove(files, false, NULL_PROGRESS_MONITOR);
        commit(files);
        VCSFileProxySupport.mkdirs(file);
        write(file2, "aaaa");
        add(file2);
        commit(files);

        GitRevisionInfo[] logs = client.log(new SearchCriteria(), NULL_PROGRESS_MONITOR);
        String revisionCurrent = logs[0].getRevision();
        String revisionPrevious = logs[1].getRevision();

        client.reset(files, revisionPrevious, true, NULL_PROGRESS_MONITOR);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
if(false)assertEquals(2, statuses.size());
else     assertEquals(1, statuses.size());
if(false)assertStatus(statuses, workDir, file, true, Status.STATUS_ADDED, Status.STATUS_REMOVED, Status.STATUS_NORMAL, false);
//else     assertStatus(statuses, workDir, file, false, Status.STATUS_NORMAL, Status.STATUS_ADDED, Status.STATUS_ADDED, false);
if(false)assertStatus(statuses, workDir, file2, true, Status.STATUS_REMOVED, Status.STATUS_ADDED, Status.STATUS_NORMAL, false);
else     assertStatus(statuses, workDir, file2, true, Status.STATUS_REMOVED, Status.STATUS_ADDED, Status.STATUS_MODIFIED, false);

        client.reset(files, revisionCurrent, true, NULL_PROGRESS_MONITOR);
        statuses = client.getStatus(files, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, workDir, file2, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
    }

    // must not checkout from nested repositories
    public void testResetNested () throws Exception {
        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
        write(f, "file");
        
        GitClient client = getClient(workDir);
        client.add(new VCSFileProxy[] { f }, NULL_PROGRESS_MONITOR);
        client.commit(new VCSFileProxy[] { f }, "init commit", null, null, NULL_PROGRESS_MONITOR);
        
        VCSFileProxy nested = VCSFileProxy.createFileProxy(workDir, "nested");
        VCSFileProxySupport.mkdirs(nested);
        VCSFileProxy f2 = VCSFileProxy.createFileProxy(nested, "f");
        write(f2, "file");
        GitClient clientNested = getClient(nested);
        clientNested.init(NULL_PROGRESS_MONITOR);
        clientNested.add(new VCSFileProxy[] { f2 }, NULL_PROGRESS_MONITOR);
        clientNested.commit(new VCSFileProxy[] { f2 }, "init commit", null, null, NULL_PROGRESS_MONITOR);
        
        write(f, "change");
        add(f);
        write(f2, "change");
        clientNested.add(new VCSFileProxy[] { f2 }, NULL_PROGRESS_MONITOR);
        
        client.reset(new VCSFileProxy[] { workDir, nested }, "HEAD", true, NULL_PROGRESS_MONITOR);
        Map<VCSFileProxy, GitStatus> statuses = client.getStatus(new VCSFileProxy[] { workDir }, NULL_PROGRESS_MONITOR);
        assertEquals(2, statuses.size());
        assertStatus(statuses, workDir, f, true, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false);
        assertStatus(statuses, workDir, nested, false, Status.STATUS_NORMAL, Status.STATUS_ADDED, Status.STATUS_ADDED, false);
        statuses = clientNested.getStatus(new VCSFileProxy[] { nested }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, nested, f2, true, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, false);
        
        client.reset("master", ResetType.MIXED, NULL_PROGRESS_MONITOR);
        statuses = clientNested.getStatus(new VCSFileProxy[] { nested }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, nested, f2, true, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, false);
        
        client.reset("master", ResetType.HARD, NULL_PROGRESS_MONITOR);
        statuses = clientNested.getStatus(new VCSFileProxy[] { nested }, NULL_PROGRESS_MONITOR);
        assertEquals(1, statuses.size());
        assertStatus(statuses, nested, f2, true, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, false);
    }
    
//    public void testLineEndingsWindows () throws Exception {
//        if (!isWindows()) {
//            return;
//        }
//        // lets turn autocrlf on
//        Thread.sleep(1100);
//        JGitConfig cfg = repository.getConfig();
//        cfg.setString(JGitConfig.CONFIG_CORE_SECTION, null, JGitConfig.CONFIG_KEY_AUTOCRLF, "true");
//        cfg.save();
//        
//        VCSFileProxy f = VCSFileProxy.createFileProxy(workDir, "f");
//        write(f, "a\r\nb\r\n");
//        VCSFileProxy[] roots = new VCSFileProxy[] { f };
//        
//        GitClient client = getClient(workDir);
//        runExternally(workDir, Arrays.asList("git.cmd", "add", "f"));
//        List<String> res = runExternally(workDir, Arrays.asList("git.cmd", "status", "-s"));
//        assertEquals(Arrays.asList("A  f"), res);
//        DirCacheEntry e1 = repository.readDirCache().getEntry("f");
//        runExternally(workDir, Arrays.asList("git.cmd", "commit", "-m", "hello"));
//        
//        write(f, "a\r\nb\r\nc\r\n");
//        res = runExternally(workDir, Arrays.asList("git.cmd", "status", "-s"));
//        assertEquals(Arrays.asList(" M f"), res);
//        runExternally(workDir, Arrays.asList("git.cmd", "add", "f"));
//        res = runExternally(workDir, Arrays.asList("git.cmd", "status", "-s"));
//        assertEquals(Arrays.asList("M  f"), res);
//        assertStatus(client.getStatus(roots, NULL_PROGRESS_MONITOR), workDir, f, true, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, false);
//        
//        client.reset(roots, "HEAD", true, NULL_PROGRESS_MONITOR);
//        assertStatus(client.getStatus(roots, NULL_PROGRESS_MONITOR), workDir, f, true, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, Status.STATUS_MODIFIED, false);
//        assertEquals(e1.getObjectId(), repository.readDirCache().getEntry("f").getObjectId());
//        res = runExternally(workDir, Arrays.asList("git.cmd", "status", "-s"));
//        assertEquals(Arrays.asList(" M f"), res);
//        
//        runExternally(workDir, Arrays.asList("git.cmd", "add", "f"));
//        res = runExternally(workDir, Arrays.asList("git.cmd", "status", "-s"));
//        assertEquals(Arrays.asList("M  f"), res);
//        assertStatus(client.getStatus(roots, NULL_PROGRESS_MONITOR), workDir, f, true, Status.STATUS_MODIFIED, Status.STATUS_NORMAL, Status.STATUS_MODIFIED, false);
//        
//        client.reset("HEAD", ResetType.HARD, NULL_PROGRESS_MONITOR);
//        assertStatus(client.getStatus(roots, NULL_PROGRESS_MONITOR), workDir, f, true, Status.STATUS_NORMAL, Status.STATUS_NORMAL, Status.STATUS_NORMAL, false);
//        assertEquals(e1.getObjectId(), repository.readDirCache().getEntry("f").getObjectId());
//        res = runExternally(workDir, Arrays.asList("git.cmd", "status", "-s"));
//        assertEquals(0, res.size());
//    }
    
//    public void testResetConflict () throws Exception {
//        VCSFileProxy file = VCSFileProxy.createFileProxy(workDir, "file");
//        write(file, "init");
//        VCSFileProxy[] files = new VCSFileProxy[] { file };
//        add(files);
//        commit(files);
//
//        DirCache index = repository.lockDirCache();
//        DirCacheBuilder builder = index.builder();
//        DirCacheEntry e = index.getEntry(file.getName());
//        DirCacheEntry e1 = new DirCacheEntry(file.getName(), 1);
//        e1.setCreationTime(e.getCreationTime());
//        e1.setFileMode(e.getFileMode());
//        e1.setLastModified(e.getLastModified());
//        e1.setLength(e.getLength());
//        e1.setObjectId(e.getObjectId());
//        builder.add(e1);
//        builder.finish();
//        builder.commit();
//        
//        GitClient client = getClient(workDir);
//        Map<VCSFileProxy, GitStatus> status = client.getStatus(files, NULL_PROGRESS_MONITOR);
//        assertTrue(status.get(file).isConflict());
//        assertEquals(GitConflictDescriptor.Type.BOTH_DELETED, status.get(file).getConflictDescriptor().getType());
//        
//        client.reset(files, "HEAD", true, NULL_PROGRESS_MONITOR);
//        status = client.getStatus(files, NULL_PROGRESS_MONITOR);
//        assertFalse(status.get(file).isConflict());
//    }
    
}
