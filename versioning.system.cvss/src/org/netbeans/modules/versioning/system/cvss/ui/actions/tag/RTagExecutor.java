/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
