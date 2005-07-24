/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
     * Executes the given command by posting it to CVS module engine. It returns immediately, the command is
     * executed in the background. This method may split the original command into more commands if the original
     * command would execute on incompatible files.
     * 
     * @param cmd command o execute
     * @param roots folders that represent remote repositories to operate on
     * @param options global option for the command
     * @return array of executors that will execute the command (or array of splitted commands)
     */ 
    public static RTagExecutor [] executeCommand(RtagCommand cmd, File [] roots, GlobalOptions options) {
        if (cmd.getDisplayName() == null) cmd.setDisplayName(NbBundle.getMessage(RTagExecutor.class, "MSG_RTagExecutor_CmdDisplayName"));
        
        File [][] splitRoots;
        try {
            splitRoots = splitByCvsRoot(roots);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
            return null;
        }
        if (options == null) options = new GlobalOptions();
        
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
                    String repository = ah.getRepositoryForDirectory(directory.getAbsolutePath(), "").substring(1);
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
            executors[i].execute();
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
