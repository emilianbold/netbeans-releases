/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.subversion.remote.client.cli;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.api.extexecution.ProcessBuilder;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.SvnModuleConfig;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils.Canceler;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.openide.filesystems.FileSystem;

/**
 * Encapsulates svn shell process. 
 * 
 * 
 */
class Commandline {

    private String executable;
    private Canceler canceled = new Canceler();
    private final FileSystem fileSystem;
    
    /**
     * Creates a new cleartool shell process.
     */
    Commandline(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
        executable = SvnModuleConfig.getDefault(fileSystem).getExecutableBinaryPath();
        if(executable == null || executable.trim().equals("")) { //NOI18N
            executable = "svn"; // NOI18N
        } else {
            if (!executable.endsWith("/svn")) { //NOI18N
                executable += "/svn"; //NOI18N
            }
        }
    }

    /**
     * Forcibly closes the cleartool console, just like using Ctrl-C.
     */
    public void interrupt() {
        try {
            destroy();
        } catch (IOException e) {
            // swallow, we are not interested
        }
    }
   
    private void destroy() throws IOException {
        canceled.cancel();
        if (Subversion.LOG.isLoggable(Level.FINE)) {
            Subversion.LOG.fine("cli: Process destroyed");                          // NOI18N
        }
    }

    // why synchronized? with 1.7, all commands go through this method, even those parallelizable ones
    // so it may (and it does) happen that two commands run simultaneously and eventually a wrong output is read (because cli is an instance field)
    // this is a hotfix, cli should probably be turned into a local var, but how would we interrupt the command in that case???
    synchronized void exec(SvnCommand command) throws IOException {
        canceled = new Canceler();
        command.prepareCommand();        
        
        String cmd = executable + " " + command.getStringCommand(); //NOI18N
        if (Subversion.LOG.isLoggable(Level.FINE)) {
            Subversion.LOG.log(Level.FINE, "cli: Executing \"{0}\"", cmd);          // NOI18N
            Subversion.LOG.fine("cli: Creating process...");                        // NOI18N
        }
        
        command.commandStarted();
        try {
            String[] args = command.getCliArguments();
            if (Subversion.LOG.isLoggable(Level.FINE)) {
                Subversion.LOG.fine("cli: process created");                        // NOI18N
            }
            ProcessUtils.ExitStatus exitStatus = ProcessUtils.executeInDir("/", getEnvVar(), command.hasBinaryOutput(), canceled, VCSFileProxy.createFileProxy(fileSystem.getRoot()), executable, args); //NOI18N

            if(command.hasBinaryOutput()) {
                if (Subversion.LOG.isLoggable(Level.FINE)) {
                    Subversion.LOG.fine("cli: ready for binary OUTPUT \"");         // NOI18N          
                }
                if(Subversion.LOG.isLoggable(Level.FINER)) {
                    // supose that encoding is UTF-8.
                    // it can be wrong for cat command (ignore because it is a logging)
                    Subversion.LOG.log(Level.FINER, "cli: BIN OUTPUT \"{0}\"", new String(exitStatus.bytes, "UTF-8")); // NOI18N
                }
                command.output(exitStatus.bytes);
            } else {             
                if (Subversion.LOG.isLoggable(Level.FINE)) {
                    Subversion.LOG.fine("cli: ready for OUTPUT \"");                // NOI18N     
                }
                if(Subversion.LOG.isLoggable(Level.FINER)) {
                    Subversion.LOG.log(Level.FINE, "cli: OUTPUT \"{0}\"", exitStatus.output);// NOI18N
                }
                if (exitStatus.output!= null && !exitStatus.output.isEmpty()) {
                    for(String line : exitStatus.output.split("\n")) { //NOI18N
                        command.outputText(line);
                    }
                }
            }
            if (exitStatus.error != null && !exitStatus.error.isEmpty()) {
                for(String line : exitStatus.error.split("\n")) { //NOI18N
                    if (!line.isEmpty()) {
                        command.errorText(line);
                    }
                }
            }
            if(canceled.canceled()) {
                return;
            }
            command.commandCompleted(exitStatus.exitCode);
        } catch (Throwable t) {
            if(canceled.canceled()) {
                if (Subversion.LOG.isLoggable(Level.FINE)) {
                    Subversion.LOG.fine(t.getMessage());
                }
            } else {
                if(t instanceof IOException) {
                    throw (IOException) t;
                } else {
                    throw new IOException(t);
                }
            }
        } finally {
            if (Subversion.LOG.isLoggable(Level.FINE)) {
                Subversion.LOG.fine("cli: process finnished");                      // NOI18N
            }
            command.commandFinished();
        }        
    }    

    private Map<String, String> getEnvVar() {
        Map<String,String> ret = new HashMap<>();
        ret.put("LC_ALL", "");                // NOI18N    
        ret.put("LC_MESSAGES", "C");          // NOI18N    
        ret.put("LC_TIME", "C");              // NOI18N    
        return ret;
    }	    
}
