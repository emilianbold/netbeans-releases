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
import org.netbeans.modules.versioning.system.cvss.FileStatusCache;
import org.netbeans.modules.versioning.system.cvss.ExecutorSupport;
import org.netbeans.modules.versioning.system.cvss.ClientRuntime;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.DefaultFileInfoContainer;
import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.tag.TagCommand;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

import java.util.*;
import java.io.File;
import java.io.IOException;

/**
 * Executes a given 'tag' command and refreshes file statuses.
 * 
 * @author Maros Sandor
 */
public class TagExecutor extends ExecutorSupport {
    
    private static final ResourceBundle loc = NbBundle.getBundle(TagExecutor.class);
    
    /**
     * Executes the given command by posting it to CVS module engine. It returns immediately, the command is
     * executed in the background. This method may split the original command into more commands if the original
     * command would execute on incompatible files. See {@link #prepareBasicCommand(org.netbeans.lib.cvsclient.command.BasicCommand)} 
     * for more information.
     * 
     * @param cmd command o execute
     * @param cvs CVS engine to use
     * @param options global option for the command
     * @return array of executors that will execute the command (or array of splitted commands)
     */ 
    public static TagExecutor [] executeCommand(TagCommand cmd, CvsVersioningSystem cvs, GlobalOptions options) {
        Command [] cmds = new org.netbeans.lib.cvsclient.command.Command[0];
        if (cmd.getDisplayName() == null) cmd.setDisplayName(loc.getString("MSG_TagExecutor_CmdDisplayName"));
        try {
            cmds = prepareBasicCommand(cmd);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
            return null;
        }
        TagExecutor [] executors = new TagExecutor[cmds.length]; 
        for (int i = 0; i < cmds.length; i++) {
            Command command = cmds[i];
            executors[i] = new TagExecutor(cvs, (TagCommand) command, options);
            executors[i].execute();
        }
        return executors;
    }
    
    private TagExecutor(CvsVersioningSystem cvs, TagCommand cmd, GlobalOptions options) {
        super(cvs, cmd, options);
    }

    protected void commandFinished(ClientRuntime.Result result) {
        
        Set parents = new HashSet();
        for (Iterator i = toRefresh.iterator(); i.hasNext();) {
            DefaultFileInfoContainer info = (DefaultFileInfoContainer) i.next();
            File file = info.getFile();
            cache.refreshCached(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
            parents.add(file.getParentFile());
        }
        toRefresh.clear();
        
        for (Iterator i = parents.iterator(); i.hasNext();) {
            File dir = (File) i.next();
            cache.refreshCached(dir, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);            
        }
    }
}
