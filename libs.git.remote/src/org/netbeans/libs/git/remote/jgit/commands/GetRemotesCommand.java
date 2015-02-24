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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.libs.git.remote.GitException;
import org.netbeans.libs.git.remote.GitRemoteConfig;
import org.netbeans.libs.git.remote.jgit.GitClassFactory;
import org.netbeans.libs.git.remote.jgit.JGitRepository;
import org.netbeans.libs.git.remote.progress.ProgressMonitor;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;
import org.netbeans.modules.versioning.core.api.VersioningSupport;

/**
 *
 * @author ondra
 */
public class GetRemotesCommand extends GitCommand {
    public static final boolean KIT = false;
    private final ProgressMonitor monitor;
    private Map<String, GitRemoteConfig> remotes;
    
    public GetRemotesCommand (JGitRepository repository, GitClassFactory gitFactory, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.monitor = monitor;
    }
    
    @Override
    protected void run () throws GitException {
        if (KIT) {
            //runKit();
        } else {
            runCLI();
        }
    }

    protected void runKit () throws GitException {
//        Repository repository = getRepository().getRepository();
//        try {
//            List<RemoteConfig> configs = RemoteConfig.getAllRemoteConfigs(repository.getConfig());
//            remotes = new HashMap<String, GitRemoteConfig>(configs.size());
//            for (RemoteConfig remote : configs) {
//                remotes.put(remote.getName(), getClassFactory().createRemoteConfig(remote));
//            }
//        } catch (IllegalArgumentException ex) {
//            if (ex.getMessage().contains("Invalid wildcards")) {
//                throw new GitException("Unsupported remote definition in " 
//                        + VCSFileProxy.createFileProxy(getRepository().getMetadataLocation(), "config")
//                        + ". Please fix the definition before using remotes.", ex);
//            }
//            throw ex;
//        } catch (URISyntaxException ex) {
//            throw new GitException(ex);
//        }
    }
    
    public Map<String, GitRemoteConfig> getRemotes () {
        return remotes;
    }
    
    @Override
    protected void prepare() throws GitException {
        super.prepare();
        addArgument(0, "remote"); //NOI18N
        addArgument(0, "-v"); //NOI18N
    }

    private void runCLI() throws GitException {
        ProcessUtils.Canceler canceled = new ProcessUtils.Canceler();
        if (monitor != null) {
            monitor.setCancelDelegate(canceled);
        }
        String cmd = getCommandLine();
        try {
            remotes = new LinkedHashMap<>();
            runner(canceled, 0, new Parser() {

                @Override
                public void outputParser(String output) {
                    parseRemoteOutput(output);
                }

                @Override
                public void errorParser(String error) {
                    parseAddError(error);
                }
            });
            //command.commandCompleted(exitStatus.exitCode);
        } catch (Throwable t) {
            if (canceled.canceled()) {
            } else {
                throw new GitException(t);
            }
        } finally {
            //command.commandFinished();
        }
    }
    
    private void runner(ProcessUtils.Canceler canceled, int command, Parser parser) {
        if(canceled.canceled()) {
            return;
        }
        org.netbeans.api.extexecution.ProcessBuilder processBuilder = VersioningSupport.createProcessBuilder(getRepository().getLocation());
        String executable = getExecutable();
        String[] args = getCliArguments(command);
        
        ProcessUtils.ExitStatus exitStatus = ProcessUtils.executeInDir(getRepository().getLocation().getPath(), getEnvVar(), false, canceled, processBuilder, executable, args); //NOI18N
        if(canceled.canceled()) {
            return;
        }
        if (exitStatus.output!= null && exitStatus.isOK()) {
            parser.outputParser(exitStatus.output);
        }
        if (exitStatus.error != null && !exitStatus.isOK()) {
            parser.errorParser(exitStatus.error);
        }
    }
    
    private void parseRemoteOutput(String output) {
        //$ git remote -v
        //origin	https://github.com/git/git (fetch)
        //origin	https://github.com/git/git (push)
        Map<String, RemoteContainer> list = new LinkedHashMap<>();
        for (String line : output.split("\n")) { //NOI18N
            if (!line.isEmpty()) {
                line = line.replace('\t', ' ').trim();
                String[] s = line.split(" ");
                String remoteName = s[0];
                RemoteContainer conf = list.get(remoteName);
                if (conf == null) {
                    conf = new RemoteContainer();
                    list.put(remoteName, conf);
                }
                if (s.length == 3) {
                    if ("(fetch)".equals(s[2])) {
                        conf.fetchSpecs.add(s[1]);
                    } else if ("(push)".equals(s[2])) {
                        conf.pushSpecs.add(s[1]);
                    }  
                }
            }
        }
        for(Map.Entry<String, RemoteContainer> e : list.entrySet()) {
            GitRemoteConfig conf = new GitRemoteConfig(e.getKey(), e.getValue().uris, e.getValue().pushUris, e.getValue().fetchSpecs, e.getValue().pushSpecs);
            remotes.put(e.getKey(), conf);
        }
    }
    
    private void parseAddError(String error) {
        processMessages(error);
    }
    
    private abstract class Parser {
        public abstract void outputParser(String output);
        public void errorParser(String error){
        }
    }
    
    private static final class RemoteContainer {
        List<String> uris = new ArrayList<>();
        List<String> pushUris = new ArrayList<>();
        List<String> fetchSpecs = new ArrayList<>();
        List<String> pushSpecs = new ArrayList<>();
    }

}
