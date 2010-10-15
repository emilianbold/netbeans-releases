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
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheCheckout;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;

/**
 *
 * @author ondra
 */
public class CheckoutTest extends AbstractGitTestCase {

    private File workDir;
    private Repository repository;

    public CheckoutTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
        repository = getRepository(getLocalGitRepository());
    }

    public void testJGitCheckout () throws Exception {
        File file1 = new File(workDir, "file1");
        write(file1, "blablablabla");
        File file2 = new File(workDir, "file2");
        Git git = new Git(repository);
        org.eclipse.jgit.api.AddCommand cmd = git.add();
        cmd.addFilepattern("file1");
        cmd.call();

        org.eclipse.jgit.api.CommitCommand commitCmd = git.commit();
        commitCmd.setAuthor("author", "author@something");
        commitCmd.setMessage("commit message");
        commitCmd.call();

        String commitId = git.log().call().iterator().next().getId().getName();
        DirCache cache = repository.lockDirCache();
        try {
            DirCacheCheckout checkout = new DirCacheCheckout(repository, null, cache, new RevWalk(repository).parseCommit(repository.resolve(commitId)).getTree());
            checkout.checkout();
        } finally {
            cache.unlock();
        }
        cache = repository.lockDirCache();
        try {
            write(file2, "blablablabla in file2");
            DirCacheCheckout checkout = new DirCacheCheckout(repository, null, cache, new RevWalk(repository).parseCommit(repository.resolve(commitId)).getTree());
            try {
                checkout.checkout();
                fail("If stops failing, implement checkout with DirCacheCheckout");
            } catch (NullPointerException ex) {
                // sadly fails
            }
        } finally {
            cache.unlock();
        }
    }
}
