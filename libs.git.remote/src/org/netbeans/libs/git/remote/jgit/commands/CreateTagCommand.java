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
package org.netbeans.libs.git.remote.jgit.commands;

import java.util.Map;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevObject;
import org.netbeans.libs.git.remote.GitException;
import org.netbeans.libs.git.remote.GitTag;
import org.netbeans.libs.git.remote.jgit.DelegatingGitProgressMonitor;
import org.netbeans.libs.git.remote.jgit.GitClassFactory;
import org.netbeans.libs.git.remote.jgit.JGitRepository;
import org.netbeans.libs.git.remote.jgit.Utils;
import org.netbeans.libs.git.remote.progress.ProgressMonitor;

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
    private GitTag tag;
    private final ProgressMonitor monitor;

    public CreateTagCommand (JGitRepository repository, GitClassFactory gitFactory, String tagName, String taggedObject, String message, boolean signed, boolean forceUpdate, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.monitor = monitor;
        this.tagName = tagName;
        this.taggedObject = taggedObject;
        this.message = message;
        this.signed = signed;
        this.forceUpdate = forceUpdate;
    }

    @Override
    protected void run () throws GitException {
        Repository repository = getRepository().getRepository();
        try {
            RevObject obj = Utils.findObject(repository, taggedObject);
            TagCommand cmd = new Git(repository).tag();
            cmd.setName(tagName);
            cmd.setForceUpdate(forceUpdate);
            cmd.setObjectId(obj);
            cmd.setAnnotated(message != null && !message.isEmpty() || signed);
            if (cmd.isAnnotated()) {
                cmd.setMessage(message);
                cmd.setSigned(signed);
            }
            cmd.call();
            ListTagCommand tagCmd = new ListTagCommand(getRepository(), getClassFactory(), false, new DelegatingGitProgressMonitor(monitor));
            tagCmd.run();
            Map<String, GitTag> tags = tagCmd.getTags();
            tag = tags.get(tagName);
        } catch (JGitInternalException | GitAPIException ex) {
            throw new GitException(ex);
        }
    }
    
    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument(0, "tag"); //NOI18N
        if (signed) {
            addArgument(0, "-s"); //NOI18N
        }
        if (forceUpdate) {
            addArgument(0, "-f"); //NOI18N
        }
        if (message != null && !message.isEmpty()) {
            addArgument(0, "-m"); //NOI18N
            addArgument(0, message.replace("\n", "\\n")); //NOI18N
        }
        addArgument(0, tagName);
        if (taggedObject != null) {
            addArgument(0, taggedObject);
        }
    }

    public GitTag getTag () {
        return tag;
    }
}
