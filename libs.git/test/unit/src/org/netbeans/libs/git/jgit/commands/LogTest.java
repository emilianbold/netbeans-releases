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
import org.eclipse.jgit.lib.Repository;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRevisionInfo;
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

//    public void testLogRevisionRange () throws Exception {
//        File f = new File(workDir, "testcat1");
//        write(f, "initial content");
//        File[] files = new File[] { f };
//        add(files);
//        commit(files);
//
//        write(f, "modification1");
//        add(files);
//
//        GitClient client = getClient(workDir);
//        GitRevisionInfo revision1 = client.commit(files, "modification1", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
//
//        write(f, "modification2");
//        add(files);
//        GitRevisionInfo revision2 = client.commit(files, "modification2", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
//        GitRevisionInfo[] revisions = client.log(revision1.getRevision(), revision2.getRevision(), ProgressMonitor.NULL_PROGRESS_MONITOR);
//        assertEquals(2, revisions.length);
//        assertRevisions(revision1, revisions[0]);
//        assertRevisions(revision2, revisions[1]);
//
//        write(f, "modification3");
//        add(files);
//        GitRevisionInfo revision3 = client.commit(files, "modification3", null, null, ProgressMonitor.NULL_PROGRESS_MONITOR);
//        revisions = client.log(revision1.getRevision(), revision3.getRevision(), ProgressMonitor.NULL_PROGRESS_MONITOR);
//        assertEquals(3, revisions.length);
//        assertRevisions(revision1, revisions[0]);
//        assertRevisions(revision2, revisions[1]);
//        assertRevisions(revision3, revisions[2]);
//    }

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
