/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.versioning.system.cvss.ui.actions.tag;

import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.ExecutorSupport;
import org.netbeans.modules.versioning.system.cvss.ClientRuntime;
import org.netbeans.modules.versioning.system.cvss.util.CommandDuplicator;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.tag.RtagCommand;
import org.netbeans.lib.cvsclient.admin.AdminHandler;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

/**
 * Executes a given 'rtag' command.
 * 
 * @author Maros Sandor
 */
public class RTagExecutor extends ExecutorSupport {
    
    /**
     * Splits the original command into more commands if the original
     * command would execute on incompatible files.
     * See {@link #prepareBasicCommand(org.netbeans.lib.cvsclient.command.BasicCommand)}
     * for more information.
     *
     * @param cmd command to execute
     * @param options global option for the command
     * @return array of executors that will execute the command (or array of splitted commands)
     */
    public static RTagExecutor [] splitCommand(RtagCommand cmd, File [] roots, GlobalOptions options) {
        if (cmd.getDisplayName() == null) cmd.setDisplayName(NbBundle.getMessage(RTagExecutor.class, "MSG_RTagExecutor_CmdDisplayName"));
        
        File [][] splitRoots;
        try {
            splitRoots = splitByCvsRoot(roots);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
            return null;
        }
        if (options == null) options = CvsVersioningSystem.createGlobalOptions();
        
        CvsVersioningSystem cvs = CvsVersioningSystem.getInstance();
        AdminHandler ah = cvs.getAdminHandler();

        RTagExecutor [] executors = new RTagExecutor[splitRoots.length];
        CommandDuplicator cloner = CommandDuplicator.getDuplicator(cmd);
        Set remoteRepositories = new HashSet(roots.length);
        for (int i = 0; i < splitRoots.length; i++) {
            File [] files = splitRoots[i];
            for (int j = 0; j < files.length; j++) {
                File file = files[j];
                File directory = file.isDirectory() ? file : file.getParentFile();
                try {
                    String repository = ah.getRepositoryForDirectory(directory.getAbsolutePath(), "").substring(1); // NOI18N
                    remoteRepositories.add(repository);
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(e);
                    return null;
                }
            }
            GlobalOptions currentOptions = (GlobalOptions) options.clone();
            try {
                currentOptions.setCVSRoot(Utils.getCVSRootFor(files[0]));
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
                return null;
            }

            RtagCommand command = (RtagCommand) cloner.duplicate();
            command.setModules((String[]) remoteRepositories.toArray(new String[remoteRepositories.size()]));
            String commandContext = NbBundle.getMessage(RTagExecutor.class, "MSG_RTagExecutor_CmdContext", Integer.toString(files.length));
            command.setDisplayName(MessageFormat.format(cmd.getDisplayName(), new Object [] { commandContext }));
            executors[i] = new RTagExecutor(cvs, command, currentOptions);
        }
        return executors;
    }

    private RTagExecutor(CvsVersioningSystem cvs, RtagCommand cmd, GlobalOptions options) {
        super(cvs, cmd, options);
    }

    protected void commandFinished(ClientRuntime.Result result) {
        // repository command, nothing to do here
    }
}
