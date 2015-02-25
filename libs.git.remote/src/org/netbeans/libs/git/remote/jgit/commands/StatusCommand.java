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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.libs.git.remote.GitConstants;
import org.netbeans.libs.git.remote.GitException;
import org.netbeans.libs.git.remote.GitStatus;
import org.netbeans.libs.git.remote.jgit.GitClassFactory;
import org.netbeans.libs.git.remote.jgit.JGitRepository;
import org.netbeans.libs.git.remote.progress.ProgressMonitor;
import org.netbeans.libs.git.remote.progress.StatusListener;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 * @author ondra
 */
public class StatusCommand extends StatusCommandBase {
    public static final boolean KIT = false;
    private final VCSFileProxy[] roots;
    private final ProgressMonitor monitor;
    private final String revision;
    private static final Logger LOG = Logger.getLogger(StatusCommand.class.getName());
    private static final Set<VCSFileProxy> logged = new HashSet<>();
    private final boolean isRevision;

    public StatusCommand (JGitRepository repository, String revision, VCSFileProxy[] roots, GitClassFactory gitFactory,
            ProgressMonitor monitor, StatusListener listener) {
        super(repository, revision, roots, gitFactory, monitor, listener);
        this.roots = roots;
        this.monitor = monitor;
        this.revision = revision;
        isRevision = !GitConstants.HEAD.equals(revision);
    }
    
    @Override
    protected boolean prepareCommand () throws GitException {
        final boolean exists = getRepository().getMetadataLocation().exists();
        if (exists) {
            prepare();
        }
        return exists;
    }

    @Override
    protected void run () throws GitException {
        if (KIT) {
            //runKit();
        } else {
            runCLI();
        }
    }

    @Override
    protected void prepare() throws GitException {
        if (isRevision) {
            setCommandsNumber(4);
        } else {
            setCommandsNumber(3);
        }
        super.prepare();
        if (isRevision) {
            addArgument(0, "diff"); //NOI18N
            addArgument(0, "--cached"); //NOI18N
            addArgument(0, "--raw"); //NOI18N
            addArgument(0, "--name-status"); //NOI18N
            addArgument(0, revision);
            addArgument(0, "--"); //NOI18N
            addFiles(0, roots);
            addArgument(1, "diff"); //NOI18N
            addArgument(1, "--raw"); //NOI18N
            addArgument(1, "--name-status"); //NOI18N
            addArgument(1, revision);
            addArgument(1, "--"); //NOI18N
            addFiles(1, roots);
            addArgument(2, "status"); //NOI18N
            addArgument(2, "--short"); //NOI18N
            addArgument(2, "--ignored"); //NOI18N
            addArgument(2, "--untracked-files=normal"); //NOI18N
            addArgument(2, "--"); //NOI18N
            addFiles(2, roots);
            addArgument(3, "ls-files"); //NOI18N
            addArgument(3, "--"); //NOI18N
            addFiles(3, roots);
        } else {
            addArgument(0, "status"); //NOI18N
            addArgument(0, "--short"); //NOI18N
            addArgument(0, "--ignored"); //NOI18N
            addArgument(0, "--untracked-files=normal"); //NOI18N
            addArgument(0, "--"); //NOI18N
            addFiles(0, roots);
            addArgument(1, "diff"); //NOI18N
            addArgument(1, "--raw"); //NOI18N
            addArgument(1, "--name-status"); //NOI18N
            addArgument(1, GitConstants.HEAD);
            addArgument(1, "--"); //NOI18N
            addFiles(1, roots);
            addArgument(2, "ls-files"); //NOI18N
            addArgument(2, "--"); //NOI18N
            addFiles(2, roots);
        }
    }

    private void runCLI () throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        String cmd = getCommandLine(0);
        try {
            if (isRevision) {
                final LinkedHashMap<String, StatusLine> list = new LinkedHashMap<>();
                new Runner(canceled, 0){

                    @Override
                    public void outputParser(String output) throws GitException {
                        parseDiffOutput(output, 1, list);
                    }
                }.runCLI();
                
                new Runner(canceled, 1){

                    @Override
                    public void outputParser(String output) throws GitException {
                        parseDiffOutput(output, 3, list);
                    }
                }.runCLI();

                new Runner(canceled, 2){

                    @Override
                    public void outputParser(String output) throws GitException {
                        parseStatusOutput(output, list, true);
                    }
                }.runCLI();

                new Runner(canceled, 3){

                    @Override
                    public void outputParser(String output) throws GitException {
                        parseLsOutput(output, list);
                    }
                }.runCLI();
                if (canceled.canceled()) {
                    return;
                }
                processOutput(list, canceled);
            } else {
                final LinkedHashMap<String, StatusLine> list = new LinkedHashMap<>();
                new Runner(canceled, 0){

                    @Override
                    public void outputParser(String output) throws GitException {
                        parseStatusOutput(output, list, false);
                    }
                }.runCLI();
                new Runner(canceled, 1){

                    @Override
                    public void outputParser(String output) throws GitException {
                        parseDiffOutput(output, 3, list);
                    }

                    @Override
                    protected void errorParser(String error) throws GitException {
                        if (error.contains("fatal: bad revision 'HEAD'")) {
                            for (Map.Entry<String, StatusLine> e : list.entrySet()) {
                                final char first = e.getValue().first;
                                if (first != '?' && first != '!') {
                                    e.getValue().third = first;
                                }
                            }
                        }
                    }

                }.runCLI();

                new Runner(canceled, 2){

                    @Override
                    public void outputParser(String output) throws GitException {
                        parseLsOutput(output, list);
                    }
                }.runCLI();
                if (canceled.canceled()) {
                    return;
                }
                processOutput(list, canceled);
            }
            //command.commandCompleted(exitStatus.exitCode);
        } catch (Throwable t) {
            if(canceled.canceled()) {
            } else {
                throw new GitException(t);
            }
        } finally {
            //command.commandFinished();
        }        
    }

    private void parseStatusOutput(String output, Map<String, StatusLine> list, boolean onlyIndexWC) {
        for (String line : output.split("\n")) { //NOI18N
            if (line.length() > 3) {
                char first = line.charAt(0);
                char second = line.charAt(1);
                String file;
                String renamed = null;
                int i = line.indexOf("->");
                if (i > 0) {
                    file = line.substring(2, i).trim();
                    renamed = line.substring(i + 1).trim();
                } else {
                    file = line.substring(2).trim();
                }
                StatusLine status = list.get(file);
                if (status == null) {
                    status = new StatusLine();
                    if (onlyIndexWC) {
                        if (first == '?' || first == '!') {
                            status.first = first;
                            status.second = second;
                        } else {
                            status.second = second;
                        }
                    } else {
                        status.first = first;
                        status.second = second;
                        status.to = renamed;
                    }
                    list.put(file, status);
                } else {
                    if (onlyIndexWC) {
                        if (first == '?' || first == '!') {
                            status.untracked = first;
                        } else {
                            status.second = second;
                        }
                    } else {
                        status.untracked = first;
                    }
                }
            }
        }
    }

    private void parseDiffOutput(String output, int n, Map<String, StatusLine> list) {
        for (String line : output.split("\n")) { //NOI18N
            if (line.length() > 2) {
                char c = line.charAt(0);
                String file = line.substring(2).trim();
                StatusLine status = list.get(file);
                if (status == null) {
                    status = new StatusLine();
                    if (n == 1) {
                        status.first = c;
                    }
                    if (n == 2) {
                        status.second = c;
                    }
                    if (n == 3) {
                        status.third = c;
                    }
                    list.put(file, status);
                } else {
                    if (n == 1) {
                        status.first = c;
                    }
                    if (n == 2) {
                        status.second = c;
                    }
                    if (n == 3) {
                        status.third = c;
                    }
                }
            }
        }
    }

    private void parseLsOutput(String output, Map<String, StatusLine> list) {
        for (String line : output.split("\n")) { //NOI18N
            if (line.length() > 0) {
                String file = line.trim();
                StatusLine status = list.get(file);
                if (status == null) {
                    status = new StatusLine();
                    list.put(file, status);
                }
            }
        }
    }
    
    private void processOutput(LinkedHashMap<String, StatusLine> parseOutput, ProcessUtils.Canceler canceled) {
        for(Map.Entry<String, StatusLine> entry : parseOutput.entrySet()) {
            String file = entry.getKey();
            StatusLine v = entry.getValue();
            char first = v.first;
            char second = v.second;
            char third = v.third;
            char untracked = v.untracked;
            String renamed = v.to;
            
            boolean tracked = !(first == '?' || first == '!' );
            GitStatus.Status statusHeadIndex = GitStatus.Status.STATUS_IGNORED;
            switch (first) {
                case 'A':
                    statusHeadIndex = GitStatus.Status.STATUS_ADDED;
                    break;
                case 'C':
                    statusHeadIndex = GitStatus.Status.STATUS_ADDED;
                    break;
                case 'R':
                case 'D':
                    statusHeadIndex = GitStatus.Status.STATUS_REMOVED;
                    break;
                case 'M':
                case 'U':
                    statusHeadIndex = GitStatus.Status.STATUS_MODIFIED;
                    break;
                case ' ':
                    statusHeadIndex = GitStatus.Status.STATUS_NORMAL;
                    break;
                case '?':
                case '!':
                    statusHeadIndex = GitStatus.Status.STATUS_NORMAL;
                    break;
            }
            GitStatus.Status statusIndexWC = GitStatus.Status.STATUS_IGNORED;
            switch (second) {
                case 'A':
                    statusIndexWC = GitStatus.Status.STATUS_ADDED;
                    break;
                case 'D':
                    statusIndexWC = GitStatus.Status.STATUS_REMOVED;
                    break;
                case 'M':
                case 'U':
                    statusIndexWC = GitStatus.Status.STATUS_MODIFIED;
                    break;
                case ' ':
                    if (untracked == '?') {
                        statusIndexWC = GitStatus.Status.STATUS_ADDED;
                    } else {
                        statusIndexWC = GitStatus.Status.STATUS_NORMAL;
                    }
                    break;
                case '?':
                    statusIndexWC = GitStatus.Status.STATUS_ADDED;
                    break;
                case '!':
                    statusIndexWC = GitStatus.Status.STATUS_IGNORED;
                    break;
            }
            GitStatus.Status statusHeadWC = GitStatus.Status.STATUS_IGNORED;
            switch (third) {
                case 'A':
                    statusHeadWC = GitStatus.Status.STATUS_ADDED;
                    break;
                case 'D':
                    if (untracked == '?') {
                        statusHeadWC = GitStatus.Status.STATUS_MODIFIED;
                    } else {
                        statusHeadWC = GitStatus.Status.STATUS_REMOVED;
                    }
                    break;
                case 'M':
                case 'U':
                    statusHeadWC = GitStatus.Status.STATUS_MODIFIED;
                    break;
                case ' ':
                    if (first == '?' || first == '!') {
                        statusHeadWC = GitStatus.Status.STATUS_ADDED;
                    } else {
                        statusHeadWC = GitStatus.Status.STATUS_NORMAL;
                    }
                    break;
                case '?':
                    statusHeadWC = GitStatus.Status.STATUS_ADDED;
                    break;
                case '!':
                    statusHeadWC = GitStatus.Status.STATUS_IGNORED;
                    break;
            }
            boolean isFolder = false;
            if (file.endsWith("/")) {
                file = file.substring(0, file.length()-1);
                isFolder = true;
            }
            if (!tracked) {
                if (statusIndexWC == GitStatus.Status.STATUS_IGNORED && isFolder) {
                    statusHeadWC = statusIndexWC;
                } else {
                    statusHeadWC = GitStatus.Status.STATUS_ADDED;
                }
            }
            VCSFileProxy vcsFile = VCSFileProxy.createFileProxy(getRepository().getLocation(), file);
            long indexTimestamp = -1;
            GitStatus status = getClassFactory().createStatus(tracked, file, getRepository().getLocation().getPath()+"/"+file, vcsFile,
                    statusHeadIndex, statusIndexWC, statusHeadWC,
                    null, isFolder, null/*renamed*/, indexTimestamp);
            addStatus(vcsFile, status);
            //command.outputText(line);
        }
    }

    private static final class StatusLine {
        char first = ' ';
        char second = ' ';
        char third = ' ';
        char untracked = ' ';
        String to;

        public StatusLine() {
        }

        @Override
        public String toString() {
            return ""+first+second+third+untracked;
        }
    }
}
