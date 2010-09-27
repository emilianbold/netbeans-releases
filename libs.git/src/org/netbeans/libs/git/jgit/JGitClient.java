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

package org.netbeans.libs.git.jgit;

import org.netbeans.libs.git.jgit.commands.StatusCommand;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.eclipse.jgit.lib.Repository;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.libs.git.jgit.commands.AddCommand;
import org.netbeans.libs.git.progress.FileProgressMonitor;
import org.netbeans.libs.git.progress.StatusProgressMonitor;

/**
 *
 * @author ondra
 */
public class JGitClient extends GitClient {
    private final JGitRepository gitRepository;

    public JGitClient (JGitRepository gitRepository) {
        this.gitRepository = gitRepository;
    }

    /**
     * Adds all files under the given roots to the index
     * @param roots
     * @param monitor
     * @throws GitException an error occurs
     */
    @Override
    public void add (File[] roots, FileProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        AddCommand cmd = new AddCommand(repository, roots, monitor);
        cmd.execute();
    }

    @Override
    /**
     * Returns an array of statuses for files under given roots
     * @param roots root folders or files
     * @return status array
     * @throws GitException when an error occurs
     */
    public Map<File, GitStatus> getStatus (File[] roots, StatusProgressMonitor monitor) throws GitException {
        Repository repository = gitRepository.getRepository();
        StatusCommand cmd = new StatusCommand(repository, roots, monitor);
        cmd.execute();
        return cmd.getStatuses();
    }

    @Override
    /**
     * Returns an instance of {@link GitRepository} related to the given <code>workDir</code>
     * @param workDir local folder where the git repository is located or where it will be created
     * @param forceCreate if set to true, a new non-bare repository will be created inside the given workDir
     * @throws GitException if the repository could not be created either because it already exists inside <code>workDir</code> or cannot be created for other reasons.
     */
    public void init () throws GitException {
        try {
            Repository repository = gitRepository.getRepository();
            File workDir = repository.getWorkTree();
            if (!(workDir.exists() || workDir.mkdirs())) {
                throw new GitException("Cannot create local folder at " + workDir.getAbsolutePath());
            }
            repository.create();
        } catch (IllegalStateException ex) {
            throw new GitException(ex);
        } catch (IOException ex) {
            throw new GitException(ex);
        }
    }
}
