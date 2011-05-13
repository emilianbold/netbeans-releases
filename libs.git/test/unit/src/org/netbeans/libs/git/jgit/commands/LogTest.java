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

package org.netbeans.libs.git.jgit.commands;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import org.eclipse.jgit.lib.Repository;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitFileInfo;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitUser;
import org.netbeans.libs.git.SearchCriteria;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class LogTest extends AbstractGitTestCase {
    private Repository repository;
    private File workDir;

    public LogTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getRepository(getLocalGitRepository());
    }

    public void testLogRevision () throws Exception {
        File f = new File(workDir, "testcat1");
        write(f, "initial content");
        File[] files = new File[] { f };
        add(files);

        GitClient client = getClient(workDir);
        GitRevisionInfo revision1 = client.commit(files, "commit1", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        GitRevisionInfo revision = client.log(revision1.getRevision(), ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertRevisions(revision1, revision);

        write(f, "modification");
        add(files);
        GitRevisionInfo revision2 = client.commit(files, "commit2", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        revision = client.log(revision1.getRevision(), ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertRevisions(revision1, revision);

        revision = client.log(revision2.getRevision(), ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertRevisions(revision2, revision);
    }

    public void testLogRevisionTo () throws Exception {
        File f = new File(workDir, "testcat1");
        write(f, "initial content");
        File[] files = new File[] { f };
        add(files);
        GitClient client = getClient(workDir);
        GitRevisionInfo revision0 = client.commit(files, "initial commit", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);

        write(f, "modification1");
        add(files);

        GitRevisionInfo revision1 = client.commit(files, "modification1", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        
        write(f, "modification2");
        add(files);
        GitRevisionInfo revision2 = client.commit(files, "modification2", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        
        write(f, "modification3");
        add(files);
        GitRevisionInfo revision3 = client.commit(files, "modification3", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);

        write(f, "modification4");
        add(files);
        GitRevisionInfo revision4 = client.commit(files, "modification4", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        SearchCriteria crit = new SearchCriteria();
        crit.setRevisionTo(revision4.getRevision());
        GitRevisionInfo[] revisions = client.log(crit, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(5, revisions.length);
        assertRevisions(revision4, revisions[0]);
        assertRevisions(revision3, revisions[1]);
        assertRevisions(revision2, revisions[2]);
        assertRevisions(revision1, revisions[3]);
        assertRevisions(revision0, revisions[4]);
    }
    
    public void testLogRevisionRange () throws Exception {
        File f = new File(workDir, "testcat1");
        write(f, "initial content");
        File[] files = new File[] { f };
        add(files);
        commit(files);

        write(f, "modification1");
        add(files);

        GitClient client = getClient(workDir);
        GitRevisionInfo revision1 = client.commit(files, "modification1", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);

        
        write(f, "modification2");
        add(files);
        GitRevisionInfo revision2 = client.commit(files, "modification2", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        
        write(f, "modification3");
        add(files);
        GitRevisionInfo revision3 = client.commit(files, "modification3", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        SearchCriteria crit = new SearchCriteria();
        crit.setRevisionFrom(revision2.getRevision());
        crit.setRevisionTo(revision3.getRevision());
        GitRevisionInfo[] revisions = client.log(crit, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(2, revisions.length);
        assertRevisions(revision3, revisions[0]);
        assertRevisions(revision2, revisions[1]);

        write(f, "modification4");
        add(files);
        GitRevisionInfo revision4 = client.commit(files, "modification4", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        crit = new SearchCriteria();
        crit.setRevisionFrom(revision2.getRevision());
        crit.setRevisionTo(revision4.getRevision());
        revisions = client.log(crit, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(3, revisions.length);
        assertRevisions(revision4, revisions[0]);
        assertRevisions(revision3, revisions[1]);
        assertRevisions(revision2, revisions[2]);
    }
    
    public void testLogSingleBranch () throws Exception {
        File f = new File(workDir, "file");
        write(f, "initial content");
        File[] files = new File[] { f };
        add(files);
        GitClient client = getClient(workDir);
        GitRevisionInfo revision0 = client.commit(files, "initial commit", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);

        write(f, "modification1");
        add(files);

        GitRevisionInfo revision1 = client.commit(files, "modification1", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        
        write(new File(workDir, ".git/refs/heads/A"), revision1.getRevision());
        write(new File(workDir, ".git/refs/heads/B"), revision1.getRevision());
        write(new File(workDir, ".git/HEAD"), "ref: refs/heads/A");
        Thread.sleep(1000);
        write(f, "modificationOnA-1");
        add(files);
        GitRevisionInfo revisionA1 = client.commit(files, "modificationOnA-1", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        // to B
        write(new File(workDir, ".git/HEAD"), "ref: refs/heads/B");
        client.reset(revision1.getRevision(), GitClient.ResetType.SOFT, ProgressMonitor.NULL_PROGRESS_MONITOR);
        Thread.sleep(1000);
        write(f, "modificationOnB-1");
        add(files);
        GitRevisionInfo revisionB1 = client.commit(files, "modificationOnB-1", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        
        // to A
        write(new File(workDir, ".git/HEAD"), "ref: refs/heads/A");
        client.reset(revisionA1.getRevision(), GitClient.ResetType.SOFT, ProgressMonitor.NULL_PROGRESS_MONITOR);
        Thread.sleep(1000);
        write(f, "modificationOnA-2");
        add(files);
        GitRevisionInfo revisionA2 = client.commit(files, "modificationOnA-2", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);

        // to B
        write(new File(workDir, ".git/HEAD"), "ref: refs/heads/B");
        client.reset(revisionB1.getRevision(), GitClient.ResetType.SOFT, ProgressMonitor.NULL_PROGRESS_MONITOR);
        Thread.sleep(1000);
        write(f, "modificationOnB-2");
        add(files);
        GitRevisionInfo revisionB2 = client.commit(files, "modificationOnB-2", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        SearchCriteria crit = new SearchCriteria();
        crit.setRevisionTo("A");
        GitRevisionInfo[] revisions = client.log(crit, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(4, revisions.length);
        assertRevisions(revisionA2, revisions[0]);
        assertRevisions(revisionA1, revisions[1]);
        assertRevisions(revision1, revisions[2]);
        assertRevisions(revision0, revisions[3]);
        
        crit = new SearchCriteria();
        crit.setRevisionTo("B");
        revisions = client.log(crit, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(4, revisions.length);
        assertRevisions(revisionB2, revisions[0]);
        assertRevisions(revisionB1, revisions[1]);
        assertRevisions(revision1, revisions[2]);
        assertRevisions(revision0, revisions[3]);
        
        // try both branches, how are the revisions sorted?
        revisions = client.log(new SearchCriteria(), ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(6, revisions.length);
        assertRevisions(revisionB2, revisions[0]);
        assertRevisions(revisionA2, revisions[1]);
        assertRevisions(revisionB1, revisions[2]);
        assertRevisions(revisionA1, revisions[3]);
        assertRevisions(revision1, revisions[4]);
        assertRevisions(revision0, revisions[5]);
    }
    
    public void testLogLimit () throws Exception {
        File f = new File(workDir, "testcat1");
        write(f, "initial content");
        File[] files = new File[] { f };
        add(files);
        commit(files);

        write(f, "modification1");
        add(files);

        GitClient client = getClient(workDir);
        GitRevisionInfo revision1 = client.commit(files, "modification1", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        
        write(f, "modification2");
        add(files);
        GitRevisionInfo revision2 = client.commit(files, "modification2", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        
        write(f, "modification3");
        add(files);
        GitRevisionInfo revision3 = client.commit(files, "modification3", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);

        write(f, "modification4");
        add(files);
        GitRevisionInfo revision4 = client.commit(files, "modification4", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        SearchCriteria crit = new SearchCriteria();
        crit.setRevisionFrom(revision2.getRevision());
        crit.setRevisionTo(revision4.getRevision());
        GitRevisionInfo[] revisions = client.log(crit, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(3, revisions.length);
        assertRevisions(revision4, revisions[0]);
        assertRevisions(revision3, revisions[1]);
        assertRevisions(revision2, revisions[2]);
        
        crit = new SearchCriteria();
        crit.setRevisionFrom(revision2.getRevision());
        crit.setRevisionTo(revision4.getRevision());
        crit.setLimit(2);
        revisions = client.log(crit, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(2, revisions.length);
        assertRevisions(revision4, revisions[0]);
        assertRevisions(revision3, revisions[1]);
    }
    
    public void testLogFiles () throws Exception {
        File f1 = new File(workDir, "file1");
        write(f1, "initial content");
        File f2 = new File(workDir, "file2");
        write(f2, "initial content");
        File f3 = new File(workDir, "file3");
        File[] files = new File[] { f1, f2 };
        add(files);
        GitClient client = getClient(workDir);
        commit(files);

        write(f1, "modification1");
        add(files);

        GitRevisionInfo revision1 = client.commit(files, "modification1", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        
        write(f2, "modification2");
        add(files);
        GitRevisionInfo revision2 = client.commit(files, "modification2", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        
        write(f1, "modification3");
        write(f2, "modification3");
        add(files);
        GitRevisionInfo revision3 = client.commit(files, "modification3", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);

        write(f3, "modification4");
        add(new File[] { f3 });
        GitRevisionInfo revision4 = client.commit(new File[] { f3 }, "modification4", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        
        SearchCriteria crit = new SearchCriteria();
        crit.setFiles(new File[] { f1 });
        crit.setRevisionFrom(revision1.getRevision());
        GitRevisionInfo[] revisions = client.log(crit, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(2, revisions.length);
        assertRevisions(revision3, revisions[0]);
        assertRevisions(revision1, revisions[1]);
        
        crit = new SearchCriteria();
        crit.setFiles(new File[] { f2 });
        crit.setRevisionFrom(revision1.getRevision());
        revisions = client.log(crit, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(2, revisions.length);
        assertRevisions(revision3, revisions[0]);
        assertRevisions(revision2, revisions[1]);
        
        crit = new SearchCriteria();
        crit.setFiles(files);
        crit.setRevisionFrom(revision1.getRevision());
        revisions = client.log(crit, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(3, revisions.length);
        assertRevisions(revision3, revisions[0]);
        assertRevisions(revision2, revisions[1]);
        assertRevisions(revision1, revisions[2]);
        
        crit = new SearchCriteria();
        crit.setFiles(new File[] { f1, f2, f3 });
        crit.setRevisionFrom(revision1.getRevision());
        revisions = client.log(crit, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(4, revisions.length);
        assertRevisions(revision4, revisions[0]);
        assertRevisions(revision3, revisions[1]);
        assertRevisions(revision2, revisions[2]);
        assertRevisions(revision1, revisions[3]);
        
        crit = new SearchCriteria();
        crit.setFiles(new File[] { workDir });
        crit.setRevisionFrom(revision1.getRevision());
        revisions = client.log(crit, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(4, revisions.length);
        assertRevisions(revision4, revisions[0]);
        assertRevisions(revision3, revisions[1]);
        assertRevisions(revision2, revisions[2]);
        assertRevisions(revision1, revisions[3]);
    }
    
    public void testLogUsername () throws Exception {
        File f = new File(workDir, "f");
        write(f, "initial content");
        File[] files = new File[] { f };
        add(files);
        commit(files);

        write(f, "modification1");
        add(files);

        GitClient client = getClient(workDir);
        GitUser user1 = new GitUser() {
            @Override
            public String getName () {
                return "git-test-user";
            }

            @Override
            public String getEmailAddress () {
                return "git-test-user@domain.com";
            }
        };
        GitRevisionInfo revision1 = client.commit(files, "modification1", user1, null, ProgressMonitor.NULL_PROGRESS_MONITOR);

        write(f, "modification2");
        add(files);
        GitRevisionInfo revision2 = client.commit(files, "modification2", null, user1, ProgressMonitor.NULL_PROGRESS_MONITOR);
        
        SearchCriteria crit = new SearchCriteria();
        GitRevisionInfo[] revisions = client.log(crit, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(3, revisions.length);
        
        crit = new SearchCriteria();
        crit.setUsername("git-test-user");
        revisions = client.log(crit, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(2, revisions.length);
        assertRevisions(revision2, revisions[0]);
        assertRevisions(revision1, revisions[1]);
        
        crit = new SearchCriteria();
        crit.setUsername("git-test-user@domain.com");
        revisions = client.log(crit, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(2, revisions.length);
        assertRevisions(revision2, revisions[0]);
        assertRevisions(revision1, revisions[1]);
        
        crit = new SearchCriteria();
        crit.setUsername("test-user");
        revisions = client.log(crit, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(2, revisions.length);
        assertRevisions(revision2, revisions[0]);
        assertRevisions(revision1, revisions[1]);
        
        crit = new SearchCriteria();
        crit.setUsername("git-test-user222@domain.com");
        revisions = client.log(crit, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(0, revisions.length);
        
        crit = new SearchCriteria();
        crit.setUsername("git-test-user <git-test-user@domain.com>");
        revisions = client.log(crit, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(2, revisions.length);
        assertRevisions(revision2, revisions[0]);
        assertRevisions(revision1, revisions[1]);
    }
    
    public void testLogMessage () throws Exception {
        File f = new File(workDir, "f");
        write(f, "initial content");
        File[] files = new File[] { f };
        add(files);
        commit(files);

        write(f, "modification1");
        add(files);

        GitClient client = getClient(workDir);
        GitRevisionInfo revision1 = client.commit(files, "modification1\non master\n", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        
        SearchCriteria crit = new SearchCriteria();
        GitRevisionInfo[] revisions = client.log(crit, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(2, revisions.length);
        
        crit = new SearchCriteria();
        crit.setMessage("blablabla");
        revisions = client.log(crit, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(0, revisions.length);
        
        crit = new SearchCriteria();
        crit.setMessage("modification");
        revisions = client.log(crit, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(1, revisions.length);
        assertRevisions(revision1, revisions[0]);
        
        crit = new SearchCriteria();
        crit.setMessage("modification1\non master");
        revisions = client.log(crit, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(1, revisions.length);
        assertRevisions(revision1, revisions[0]);
    }
    
    public void testLogShowMerges () throws Exception {
        File f = new File(workDir, "f");
        write(f, "a\nb\nc");
        File[] files = new File[] { f };
        add(files);
        commit(files);

        GitClient client = getClient(workDir);
        client.createBranch("b", "master", ProgressMonitor.NULL_PROGRESS_MONITOR);
        client.checkoutRevision("b", true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        
        write(f, "modification on branch\nb\nc");
        add(files);
        GitRevisionInfo revisionBranch = client.commit(files, "modification on branch", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        
        Thread.sleep(1100);
        
        client.checkoutRevision("master", true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        write(f, "a\nb\nmodification on master");
        add(files);
        GitRevisionInfo revisionMaster = client.commit(files, "modification on master", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        
        GitRevisionInfo revisionMerge = client.log(client.merge("b", ProgressMonitor.NULL_PROGRESS_MONITOR).getNewHead(), ProgressMonitor.NULL_PROGRESS_MONITOR);
        
        SearchCriteria crit = new SearchCriteria();
        crit.setRevisionTo("master");
        GitRevisionInfo[] revisions = client.log(crit, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(4, revisions.length);
        assertRevisions(revisionMerge, revisions[0]);
        
        crit = new SearchCriteria();
        crit.setIncludeMerges(true);
        revisions = client.log(crit, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(4, revisions.length);
        assertRevisions(revisionMerge, revisions[0]);
        
        crit = new SearchCriteria();
        crit.setIncludeMerges(false);
        revisions = client.log(crit, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(3, revisions.length);
        assertRevisions(revisionMaster, revisions[0]);
        assertRevisions(revisionBranch, revisions[1]);
    }
    
    public void testLogDateCriteria () throws Exception {
        File f = new File(workDir, "f");
        write(f, "initial content");
        File[] files = new File[] { f };
        add(files);
        commit(files);

        write(f, "modification1");
        add(files);

        GitClient client = getClient(workDir);
        GitRevisionInfo revision1 = client.commit(files, "modification1", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);

        Thread.sleep(1100);
        
        write(f, "modification2");
        add(files);
        GitRevisionInfo revision2 = client.commit(files, "modification2", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);

        Thread.sleep(1100);

        write(f, "modification3");
        add(files);
        GitRevisionInfo revision3 = client.commit(files, "modification3", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        SearchCriteria crit = new SearchCriteria();
        crit.setFrom(new Date(revision2.getCommitTime()));
        crit.setTo(new Date(revision3.getCommitTime()));
        GitRevisionInfo[] revisions = client.log(crit, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(2, revisions.length);
        assertRevisions(revision3, revisions[0]);
        assertRevisions(revision2, revisions[1]);

        Thread.sleep(1100);

        write(f, "modification4");
        add(files);
        GitRevisionInfo revision4 = client.commit(files, "modification4", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        crit = new SearchCriteria();
        crit.setFrom(new Date(revision2.getCommitTime()));
        crit.setTo(new Date(revision3.getCommitTime()));
        revisions = client.log(crit, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(2, revisions.length);
        assertRevisions(revision3, revisions[0]);
        assertRevisions(revision2, revisions[1]);
        
        crit = new SearchCriteria();
        crit.setFrom(new Date(revision2.getCommitTime()));
        crit.setTo(new Date(revision4.getCommitTime()));
        revisions = client.log(crit, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(3, revisions.length);
        assertRevisions(revision4, revisions[0]);
        assertRevisions(revision3, revisions[1]);
        assertRevisions(revision2, revisions[2]);
        
        crit = new SearchCriteria();
        crit.setFrom(new Date(revision2.getCommitTime()));
        revisions = client.log(crit, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(3, revisions.length);
        assertRevisions(revision4, revisions[0]);
        assertRevisions(revision3, revisions[1]);
        assertRevisions(revision2, revisions[2]);
    }
    
    public void testLogFollowRename () throws Exception {
        File f = new File(workDir, "f");
        File to = new File(workDir, "renamed");
        write(f, "initial content");
        File[] files = new File[] { f, to };
        add(files);
        commit(files);

        write(f, "modification1");
        add(files);

        GitClient client = getClient(workDir);
        GitRevisionInfo revision1 = client.commit(files, "modification1", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        
        client.rename(f, to, false, ProgressMonitor.NULL_PROGRESS_MONITOR);
        GitRevisionInfo revision2 = client.commit(files, "rename", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);

        write(to, "modification2");
        add(files);
        GitRevisionInfo revision3 = client.commit(files, "modification2", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        
        SearchCriteria crit = new SearchCriteria();
        crit.setFiles(new File[] { to });
        GitRevisionInfo[] revisions = client.log(crit, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(2, revisions.length);
        assertRevisions(revision3, revisions[0]);
        assertRevisions(revision2, revisions[1]);
        Map<File, GitFileInfo> modifiedFiles = revision2.getModifiedFiles();
        assertEquals(2, modifiedFiles.size());
        assertEquals(GitFileInfo.Status.RENAMED, modifiedFiles.get(to).getStatus());
        assertEquals(GitFileInfo.Status.REMOVED, modifiedFiles.get(f).getStatus());
        
        crit = new SearchCriteria();
        crit.setFiles(new File[] { to });
        crit.setFollowRenames(true);
        revisions = client.log(crit, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals(4, revisions.length);
        assertRevisions(revision3, revisions[0]);
        assertRevisions(revision2, revisions[1]);
        assertRevisions(revision1, revisions[2]);
    }

    public void testLogMergeFilesFromAllParents () throws Exception {
        File f = new File(workDir, "f");
        File f2 = new File(workDir, "f2");
        write(f, "init");
        write(f2, "init");
        File[] files = new File[] { f, f2 };
        add(files);
        commit(files);

        GitClient client = getClient(workDir);
        client.createBranch("b", "master", ProgressMonitor.NULL_PROGRESS_MONITOR);
        client.checkoutRevision("b", true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        
        write(f, "modification on branch");
        add(files);
        client.commit(files, "modification on branch", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        
        client.checkoutRevision("master", true, ProgressMonitor.NULL_PROGRESS_MONITOR);
        write(f2, "modification");
        add(files);
        client.commit(files, "modification on master", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
        
        GitRevisionInfo revisionMerge = client.log(client.merge("b", ProgressMonitor.NULL_PROGRESS_MONITOR).getNewHead(), ProgressMonitor.NULL_PROGRESS_MONITOR);
        Map<File, GitFileInfo> modifiedFiles = revisionMerge.getModifiedFiles();
        assertEquals(2, modifiedFiles.size());
        assertEquals(GitFileInfo.Status.MODIFIED, modifiedFiles.get(f).getStatus());
        assertEquals(GitFileInfo.Status.MODIFIED, modifiedFiles.get(f2).getStatus());
    }

    private void assertRevisions (GitRevisionInfo expected, GitRevisionInfo info) throws GitException {
        assertEquals(expected.getRevision(), info.getRevision());
        assertEquals(expected.getAuthor().toString(), info.getAuthor().toString());
        assertEquals(expected.getCommitTime(), info.getCommitTime());
        assertEquals(expected.getCommitter().toString(), info.getCommitter().toString());
        assertEquals(expected.getFullMessage(), info.getFullMessage());
        assertEquals(expected.getModifiedFiles().size(), info.getModifiedFiles().size());
        assertEquals(expected.getShortMessage(), info.getShortMessage());
    }

}
