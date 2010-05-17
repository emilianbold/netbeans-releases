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

/**
 * Executes a given 'add' command and refreshes file statuses.
 *
 * @author Maros Sandor
 */
public class AddExecutor extends ExecutorSupport {

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
    public static AddExecutor[] splitCommand(AddCommand cmd, CvsVersioningSystem cvs, GlobalOptions options) {

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
            String param = len == 1 ? 
                    command.getFiles()[0].getName() : 
                    NbBundle.getMessage(AddExecutor.class, "MSG_AddExecutor_CmdDisplayXfiles", Integer.toString(len));
            command.setDisplayName(NbBundle.getMessage(AddExecutor.class, "MSG_AddExecutor_CmdDisplayName", param));
            executors[i] = new AddExecutor(cvs, command, options);
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
