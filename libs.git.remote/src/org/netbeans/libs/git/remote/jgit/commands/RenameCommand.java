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

package org.netbeans.libs.git.remote.jgit.commands;

import java.text.MessageFormat;
import org.netbeans.libs.git.remote.GitException;
import org.netbeans.libs.git.remote.jgit.GitClassFactory;
import org.netbeans.libs.git.remote.jgit.JGitRepository;
import org.netbeans.libs.git.remote.jgit.Utils;
import org.netbeans.libs.git.remote.progress.FileListener;
import org.netbeans.libs.git.remote.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 * @author ondra
 */
public class RenameCommand extends GitCommand {

    private final VCSFileProxy source;
    private final VCSFileProxy target;
    private final boolean after;
    private final ProgressMonitor monitor;
    private final FileListener listener;

    public RenameCommand (JGitRepository repository, GitClassFactory gitFactory, VCSFileProxy source, VCSFileProxy target, boolean after, ProgressMonitor monitor, FileListener listener){
        super(repository, gitFactory, monitor);
        this.source = source;
        this.target = target;
        this.after = after;
        this.monitor = monitor;
        this.listener = listener;
    }

    @Override
    protected boolean prepareCommand() throws GitException {
        boolean retval = super.prepareCommand();
        if (retval) {
            if (source.equals(getRepository().getLocation())) {
                throw new GitException(MessageFormat.format(Utils.getBundle(RenameCommand.class).getString("MSG_Exception_CannotMoveWT"), source.getPath())); //NOI18N
            }
            if (!source.exists() && !after) {
                throw new GitException(MessageFormat.format(Utils.getBundle(RenameCommand.class).getString("MSG_Exception_SourceDoesNotExist"), source.getPath())); //NOI18N
            }
            if (target.exists()) {
                if (!after) {
                    throw new GitException(MessageFormat.format(Utils.getBundle(RenameCommand.class).getString("MSG_Exception_TargetExists"), target.getPath())); //NOI18N
                }
            } else if (after) {
                throw new GitException(MessageFormat.format(Utils.getBundle(RenameCommand.class).getString("MSG_Exception_TargetDoesNotExist"), target.getPath())); //NOI18N
            }
        }
        return retval;
    }
    
    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument(0, "mv"); //NOI18N
        addArgument(0, "--verbose"); //NOI18N
        addArgument(0, Utils.getRelativePath(getRepository().getLocation(), source));
        addArgument(0, Utils.getRelativePath(getRepository().getLocation(), target));
    }
    
    @Override
    protected void run() throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        try {
            if (!after) {
                rename();
            }
            new Runner(canceled, 0){

                @Override
                public void outputParser(String output) throws GitException {
                    parseMoveOutput(output);
                }

                @Override
                protected void errorParser(String error) throws GitException {
                    System.err.println(error);
                }
                
            }.runCLI();
        } catch (GitException t) {
            throw t;
        } catch (Throwable t) {
            if (canceled.canceled()) {
            } else {
                throw new GitException(t);
            }
        }
    }
    
     private void rename () throws GitException {
        VCSFileProxy parentFile = target.getParentFile();
        if (!parentFile.exists() && !VCSFileProxySupport.mkdirs(parentFile)) {
            throw new GitException(MessageFormat.format(Utils.getBundle(RenameCommand.class).getString("MSG_Exception_CannotCreateFolder"), parentFile.getPath())); //NOI18N
        }
        if (!VCSFileProxySupport.renameTo(source, target)) {
            throw new GitException(MessageFormat.format(Utils.getBundle(RenameCommand.class).getString("MSG_Exception_CannotRenameTo"), source.getPath(), target.getPath())); //NOI18N
        }
    }

    private void parseMoveOutput(String output) {
        //Renaming file to folder/file
        for (String line : output.split("\n")) { //NOI18N
            if (line.startsWith("Renaming")) {
                String[] s = line.split(" ");
                String file = s[s.length-1];
                listener.notifyFile(VCSFileProxy.createFileProxy(getRepository().getLocation(), file), file);
            }
        }
    }
     
}
