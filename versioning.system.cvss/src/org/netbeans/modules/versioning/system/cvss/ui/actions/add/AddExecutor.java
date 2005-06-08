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

package org.netbeans.modules.versioning.system.cvss.ui.actions.add;

import org.netbeans.modules.versioning.system.cvss.*;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.add.AddCommand;
import org.netbeans.lib.cvsclient.command.add.AddInformation;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

/**
 * Executes a given 'add' command and refreshes file statuses.
 * 
 * @author Maros Sandor
 */
public class AddExecutor extends ExecutorSupport {

    private static final ResourceBundle loc = NbBundle.getBundle(AddExecutor.class);

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
    public static AddExecutor [] executeCommand(AddCommand cmd, CvsVersioningSystem cvs, GlobalOptions options) {

        List fileSets = new ArrayList();
        
        File [] files = getNewDirectories(cmd.getFiles());
        if (files.length > 0) {
            try {
                File [][] sets = splitFiles(files);
                for (int i = 0; i < sets.length; i++) {
                    File[] set = sets[i];
                    Arrays.sort(set, byLengthComparator);
                    fileSets.add(set);
                }
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
                return null;
            }
        }
        
        try {
            File [][] sets = splitFiles(cmd.getFiles());
            fileSets.addAll(Arrays.asList(sets));
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
            return null;
        }
        
        AddCommand [] commands = new AddCommand[fileSets.size()];
        for (int i = 0; i < commands.length; i++) {
            commands[i] = (AddCommand) cmd.clone();
            commands[i].setFiles((File[]) fileSets.get(i));
        }
        
        AddExecutor [] executors = new AddExecutor[commands.length]; 
        for (int i = 0; i < commands.length; i++) {
            AddCommand command = commands[i];
            int len = command.getFiles().length;
            String param = len == 1 ? command.getFiles()[0].getName() : Integer.toString(len);
            command.setDisplayName(MessageFormat.format(loc.getString("MSG_AddExecutor_CmdDisplayName"), new Object [] { param }));
            executors[i] = new AddExecutor(cvs, command, options);
            executors[i].execute();
        }
        return executors;
    }

    private static File[] getNewDirectories(File[] files) {
        FileStatusCache cache = CvsVersioningSystem.getInstance().getStatusCache();
        Set newDirs = new HashSet();
        for (int i = 0; i < files.length; i++) {
            File parent = files[i].getParentFile();
            for (;;) {
                if (cache.getStatus(parent).getStatus() == FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) {
                    newDirs.add(parent);
                } else {
                    break;
                }
                parent = parent.getParentFile();
                if (parent == null) break;
            }
        }
        List dirs = new ArrayList(newDirs);
        return (File []) dirs.toArray(new File[dirs.size()]);
    }

    private static final Comparator byLengthComparator = new Comparator() {
        public int compare(Object o1, Object o2) {
            File a = (File) o1;
            File b = (File) o2;
            return a.getAbsolutePath().length() - b.getAbsolutePath().length();
        }
    };
    
    private AddExecutor(CvsVersioningSystem cvs, AddCommand cmd, GlobalOptions options) {
        super(cvs, cmd, options);
    }

    protected void commandFinished(ClientRuntime.Result result) {
        Set parents = new HashSet();
        // TODO: refresh ALL files that were given as arguments + their parent directories
        // TODO: refresh ONLY if those files are already cached
        for (Iterator i = toRefresh.iterator(); i.hasNext();) {
            AddInformation addInformation = (AddInformation) i.next();
            File file = addInformation.getFile();
            cache.refreshCached(file, addInformation.getType().charAt(0));
            parents.add(file.getParentFile());
        }
        toRefresh.clear();
        
        for (Iterator i = parents.iterator(); i.hasNext();) {
            File dir = (File) i.next();
            cache.refreshCached(dir, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);            
        }
    }
}
