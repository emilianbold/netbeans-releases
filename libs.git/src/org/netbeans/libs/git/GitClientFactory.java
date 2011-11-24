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

package org.netbeans.libs.git;

import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.netbeans.libs.git.jgit.JGitRepository;
import org.netbeans.libs.git.jgit.JGitSshSessionFactory;

/**
 * Factory class used to create git clients bound to a specific local repository
 * @author ondra
 */
public final class GitClientFactory {

    private static GitClientFactory instance;
    private final Map<File, JGitRepository> repositoryPool;

    private GitClientFactory () {
        repositoryPool = new WeakHashMap<File, JGitRepository>(5);
    }

    /**
     * Returns the instance of {@link GitClientFactory}.
     * @return instance of <code>GitClientFactory</code>
     */
    public static synchronized GitClientFactory getInstance () {
        if (instance == null) {
            instance = instance = new GitClientFactory();
        }
        return instance;
    }

    /**
     * Returns a git client bound to a given local git repository
     * @param repositoryLocation repository root location
     * @return git client
     * @throws GitException
     */
    public GitClient getClient (File repositoryLocation) throws GitException {
        synchronized (repositoryPool) {
            JGitRepository repository = repositoryPool.get(repositoryLocation);
            if (repository == null) {
                // careful about keeping the reference to the repositoryRoot, rather create a new instance
                repositoryPool.put(repositoryLocation, repository = new JGitRepository(new File(repositoryLocation.getParentFile(), repositoryLocation.getName())));
            }
            SshSessionFactory.setInstance(JGitSshSessionFactory.getDefault());
            return createClient(repository);
        }
    }

    void clearRepositoryPool() {
        synchronized(repositoryPool) {
            repositoryPool.clear();
        }
    }

    private GitClient createClient (JGitRepository repository) {
        return new GitClient(repository);
    }

}
