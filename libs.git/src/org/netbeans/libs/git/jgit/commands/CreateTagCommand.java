/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.libs.git.jgit.commands;

import java.io.IOException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.RefUpdate.Result;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRefUpdateResult;
import org.netbeans.libs.git.GitTag;
import org.netbeans.libs.git.jgit.JGitRevisionInfo;
import org.netbeans.libs.git.jgit.JGitTag;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class CreateTagCommand extends GitCommand {
    private final boolean forceUpdate;
    private final String tagName;
    private final String taggedObject;
    private final String message;
    private final boolean signed;
    private JGitTag tag;

    public CreateTagCommand (Repository repository, String tagName, String taggedObject, String message, boolean signed, boolean forceUpdate, ProgressMonitor monitor) {
        super(repository, monitor);
        this.tagName = tagName;
        this.taggedObject = taggedObject;
        this.message = message;
        this.signed = signed;
        this.forceUpdate = forceUpdate;
    }

    @Override
    protected void run () throws GitException {
        Repository repository = getRepository();
        try {
            RevObject obj = Utils.findObject(repository, taggedObject);
            if ((message == null || message.isEmpty()) && signed == false) {
                tag = createLightWeight(obj, repository);
            } else {
                TagCommand cmd = new Git(repository).tag();
                cmd.setName(tagName);
                cmd.setMessage(message);
                cmd.setObjectId(obj);
                cmd.setForceUpdate(forceUpdate);
                cmd.setSigned(signed);
                RevTag revTag = cmd.call();
                tag = new JGitTag(revTag);
            }
        } catch (JGitInternalException ex) {
            throw new GitException(ex);
        } catch (GitAPIException ex) {
            throw new GitException(ex);
        } catch (IOException ex) {
            throw new GitException(ex);
        }
    }

    @Override
    protected String getCommandDescription () {
        StringBuilder sb = new StringBuilder("git tag");
        if (signed) {
            sb.append(" -s");
        }
        if (forceUpdate) {
            sb.append(" -f");
        }
        if (message != null && !message.isEmpty()) {
            sb.append(" -m ").append(message.replace("\n", "\\n"));
        }
        sb.append(' ').append(tagName);
        if (taggedObject != null) {
            sb.append(' ').append(taggedObject);
        }
        return sb.toString();
    }

    public GitTag getTag () {
        return tag;
    }

    private JGitTag createLightWeight (RevObject revObject, Repository repository) throws GitException, IOException {
        RevWalk revWalk = new RevWalk(repository);
        try {
            String refName = Constants.R_TAGS + tagName;
            RefUpdate tagRef = repository.updateRef(refName);
            tagRef.setNewObjectId(revObject);
            tagRef.setForceUpdate(forceUpdate);
            tagRef.setRefLogMessage("tagged " + tagName, false);
            Result updateResult = tagRef.update(revWalk);
            switch (updateResult) {
                case NEW:
                case FORCED:
                    return revObject instanceof RevCommit ? new JGitTag(tagName, new JGitRevisionInfo((RevCommit) revObject, repository)) : new JGitTag(tagName, revObject);
                case LOCK_FAILURE:
                    throw new GitException.RefUpdateException("Cannot lock ref " + refName, GitRefUpdateResult.valueOf(updateResult.name()));
                default:
                    throw new GitException.RefUpdateException("Updating ref " + refName + " failed", GitRefUpdateResult.valueOf(updateResult.name()));
            }

        } finally {
            revWalk.release();
        }
    }
}
