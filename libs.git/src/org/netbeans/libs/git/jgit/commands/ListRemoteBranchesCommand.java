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

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jgit.errors.NotSupportedException;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefComparator;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.FetchConnection;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.URIish;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.jgit.JGitBranch;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 * TODO merge with listRemoteTagsCommand when it's implemented
 * @author ondra
 */
public class ListRemoteBranchesCommand extends GitCommand {
    private HashMap<String, GitBranch> remoteBranches;
    private final String remoteUrl;

    public ListRemoteBranchesCommand (Repository repository, String remoteRepositoryUrl, ProgressMonitor monitor) {
        super(repository, monitor);
        this.remoteUrl = remoteRepositoryUrl;
    }

    @Override
    protected void run () throws GitException {
        Repository repository = getRepository();
        Transport t = null;
        FetchConnection conn = null;
        Map<String, Ref> refs;
        try {
            t = Transport.open(repository, new URIish(remoteUrl));
            conn = t.openFetch();
            refs = conn.getRefsMap();
        } catch (URISyntaxException ex) {
            throw new GitException(ex);
        } catch (NotSupportedException ex) {
            throw new GitException(ex);
        } catch (TransportException ex) {
            throw new GitException(ex);
        } finally {
            if (conn != null) {
                conn.close();
            }
            if (t != null) {
                t.close();
            }
        }
        remoteBranches = new HashMap<String, GitBranch>();
        remoteBranches.putAll(getRefs(refs.values(), Constants.R_HEADS));
    }

    private Map<String, GitBranch> getRefs (Collection<Ref> allRefs, String prefix) {
        Map<String, GitBranch> branches = new HashMap<String, GitBranch>();
        for (final Ref ref : RefComparator.sort(allRefs)) {
            String refName = ref.getLeaf().getName();
            if (refName.startsWith(prefix)) {
                String name = refName.substring(refName.indexOf('/', 5) + 1);
                branches.put(name, new JGitBranch(name, false, false, ref.getLeaf().getObjectId()));
            }
        }
        return branches;
    }

    @Override
    protected String getCommandDescription () {
        return "git ls-remote --heads " + remoteUrl.toString(); //NOI18N
    }

    public Map<String, GitBranch> getBranches () {
        return remoteBranches;
    }

}
