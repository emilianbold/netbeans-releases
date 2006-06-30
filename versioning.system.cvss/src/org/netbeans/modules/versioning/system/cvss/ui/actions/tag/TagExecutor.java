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
    
    /**
     * Splits the original command into more commands if the original
     * command would execute on incompatible files.
     * See {@link #prepareBasicCommand(org.netbeans.lib.cvsclient.command.BasicCommand)}
     * for more information.
     *
     * @param cmd command to execute
     * @param cvs CVS engine to use
     * @param options global option for the command
     * @return array of executors that will execute the command (or array of splitted commands)
     */
    public static TagExecutor [] splitCommand(TagCommand cmd, CvsVersioningSystem cvs, GlobalOptions options) {
        Command [] cmds = new org.netbeans.lib.cvsclient.command.Command[0];
        if (cmd.getDisplayName() == null) cmd.setDisplayName(NbBundle.getMessage(TagExecutor.class, "MSG_TagExecutor_CmdDisplayName"));
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
