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

package org.netbeans.modules.versioning.system.cvss.ui.actions.log;

import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.ExecutorSupport;
import org.netbeans.modules.versioning.system.cvss.ClientRuntime;
import org.netbeans.modules.versioning.system.cvss.util.CommandDuplicator;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.log.RlogCommand;
import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.netbeans.lib.cvsclient.admin.AdminHandler;
import org.netbeans.lib.cvsclient.event.FileInfoEvent;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

/**
 * Executes a given 'rlog' command.
 * 
 * @author Maros Sandor
 */
public class RLogExecutor extends ExecutorSupport {
    
    private final File localRoot;

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
    public static RLogExecutor [] executeCommand(RlogCommand cmd, File [] roots, GlobalOptions options) {
        if (cmd.getDisplayName() == null) cmd.setDisplayName(NbBundle.getMessage(RLogExecutor.class, "MSG_RLogExecutor_CmdDisplayName"));
        if (options == null) options = new GlobalOptions();
        
        CvsVersioningSystem cvs = CvsVersioningSystem.getInstance();
        AdminHandler ah = cvs.getAdminHandler();

        RLogExecutor [] executors = new RLogExecutor[roots.length];
        CommandDuplicator cloner = CommandDuplicator.getDuplicator(cmd);
        Set remoteRepositories = new HashSet(roots.length);
        for (int i = 0; i < roots.length; i++) {
            File file = roots[i];
            File directory = file.isDirectory() ? file : file.getParentFile();
            try {
                String repository = ah.getRepositoryForDirectory(directory.getAbsolutePath(), "").substring(1);
                remoteRepositories.add(repository);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
                return null;
            }
            GlobalOptions currentOptions = (GlobalOptions) options.clone();
            try {
                currentOptions.setCVSRoot(Utils.getCVSRootFor(file));
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
                return null;
            }

            RlogCommand command = (RlogCommand) cloner.duplicate();
            command.setModules((String[]) remoteRepositories.toArray(new String[remoteRepositories.size()]));
            String commandContext = NbBundle.getMessage(RLogExecutor.class, "MSG_RLogExecutor_CmdContext", file.getName());
            command.setDisplayName(MessageFormat.format(cmd.getDisplayName(), new Object [] { commandContext }));
            executors[i] = new RLogExecutor(cvs, command, directory, currentOptions);
            executors[i].execute();
        }
        return executors;
    }

    private RLogExecutor(CvsVersioningSystem cvs, RlogCommand cmd, File localRoot, GlobalOptions options) {
        super(cvs, cmd, options);
        this.localRoot = localRoot;
    }

    public void fileInfoGenerated(FileInfoEvent e) {
        LogInformation information = (LogInformation) e.getInfoContainer();
        String remotePath = information.getRepositoryFilename();
        File f = remote2local(remotePath);
        information.setFile(f);
        super.fileInfoGenerated(e);
    }

    protected void commandFinished(ClientRuntime.Result result) {
        // repository command, nothing to do here
    }

    /**
     * Translates repository path of a file to local path in user workdir. This includes some heuristics
     * because we cannot do not know the actual path to repository on server.
     * 
     * @param remotePath remote path on server as reposted by the rlog command
     * @return File local location of the remote file (may not exist if the file is not checked out)
     */ 
    private File remote2local(String remotePath) {
        AdminHandler ah = CvsVersioningSystem.getInstance().getAdminHandler();
        String repository;
        try {
            // /javacvs/cvsmodule
            repository = ah.getRepositoryForDirectory(localRoot.getAbsolutePath(), "").substring(1);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
            return null;
        }
        
        int idx = remotePath.indexOf(repository);
        if (idx == -1) {
            return null;
        }
        idx = remotePath.indexOf('/', idx + repository.length());
        if (idx == -1) {
            return null;
        }
        String remoteRelativePath = remotePath.substring(idx);
        
        idx = remoteRelativePath.indexOf(',');
        if (idx != -1) {
            remoteRelativePath = remoteRelativePath.substring(0, idx);
        }

        idx = remoteRelativePath.lastIndexOf('/');
        if (idx != -1 && idx >= 6) {
            if ("/Attic".equals(remoteRelativePath.substring(idx - 6, idx))) {
                remoteRelativePath = remoteRelativePath.substring(0, idx - 6) + remoteRelativePath.substring(idx);
            }
        }
        
        return new File(localRoot, remoteRelativePath);
    }

    public List getLogEntries() {
        return toRefresh;
    }
}
