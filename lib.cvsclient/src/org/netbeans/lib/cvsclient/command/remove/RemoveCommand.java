/*****************************************************************************
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
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):

 * The Original Software is the CVS Client Library.
 * The Initial Developer of the Original Software is Robert Greig.
 * Portions created by Robert Greig are Copyright (C) 2000.
 * All Rights Reserved.
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

 * Contributor(s): Robert Greig.
 *****************************************************************************/
package org.netbeans.lib.cvsclient.command.remove;

import java.io.*;

import org.netbeans.lib.cvsclient.*;
import org.netbeans.lib.cvsclient.admin.*;
import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.connection.*;
import org.netbeans.lib.cvsclient.event.*;
import org.netbeans.lib.cvsclient.request.*;
import org.netbeans.lib.cvsclient.util.*;

/**
 * The remove command is used to remove files and directories from the
 * repository.
 * @author  Robert Greig
 */
public class RemoveCommand extends BasicCommand {
    /**
     * If true, will delete the file in working dir before it gets removed.
     */
    private boolean deleteBeforeRemove;

    private boolean ignoreLocallyExistingFiles;

    /**
     * Returns true if the local files will be deleted automatically.
     */
    public boolean isDeleteBeforeRemove() {
        return deleteBeforeRemove;
    }

    /**
     * Sets whether the local files will be deleted before.
     */
    public void setDeleteBeforeRemove(boolean deleteBeforeRemove) {
        this.deleteBeforeRemove = deleteBeforeRemove;
    }

    /**
     * Returns true to indicate that locally existing files are treated as they
     * would not exist.
     * This is a extension to the standard cvs-behaviour!
     * @deprecated
     */
    public boolean doesIgnoreLocallyExistingFiles() {
        return ignoreLocallyExistingFiles;
    }

    /**
     * Returns true to indicate that locally existing files are treated as they
     * would not exist.
     * This is a extension to the standard cvs-behaviour!
     */
    public boolean isIgnoreLocallyExistingFiles() {
        return ignoreLocallyExistingFiles;
    }

    /**
     * Sets whether locally existing files will be treated as they were deleted
     * before.
     * This is a extension to the standard cvs-behaviour!
     */
    public void setIgnoreLocallyExistingFiles(boolean ignoreLocallyExistingFiles) {
        this.ignoreLocallyExistingFiles = ignoreLocallyExistingFiles;
    }

    /**
     * Method that is called while the command is being executed.
     * Descendants can override this method to return a Builder instance
     * that will parse the server's output and create data structures.
     */
    public Builder createBuilder(EventManager eventMan) {
        return new RemoveBuilder(eventMan, this);
    }

    /**
     * Executes this command.
     *
     * @param client the client services object that provides any necessary
     *               services to this command, including the ability to actually
     *               process all the requests
     */
    public void execute(ClientServices client, EventManager em)
            throws CommandException, AuthenticationException {
        if (files == null || files.length == 0) {
            throw new CommandException("No files have been specified for " + //NOI18N
                                       "removal.", CommandException.getLocalMessage("RemoveCommand.noFilesSpecified", null)); //NOI18N
        }

        client.ensureConnection();

        if (isDeleteBeforeRemove()) {
            removeAll(files);
        }
        super.execute(client, em);

        try {
            addRequestForWorkingDirectory(client);
            addArgumentRequests();
            addRequest(CommandRequest.REMOVE);

            client.processRequests(requests);
        }
        catch (CommandException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new CommandException(ex, ex.getLocalizedMessage());
        }
        finally {
            requests.clear();
        }
    }

    protected void sendEntryAndModifiedRequests(Entry entry, File file) {
        super.sendEntryAndModifiedRequests(entry,
                                           isIgnoreLocallyExistingFiles() ? null : file);
        if (entry.getRevision().equals("0")) {
             // zero means a locally added file, not yet commited.
            try {
                clientServices.removeEntry(file);
            } catch (IOException exc) {
                BugLog.getInstance().showException(exc);
            }
            
        }
    }

    /**
     * This method returns how the command would looks like when typed on the
     * command line.
     * Each command is responsible for constructing this information.
     * @returns <command's name> [<parameters>] files/dirs. Example: checkout -p CvsCommand.java
     */
    public String getCVSCommand() {
        StringBuffer toReturn = new StringBuffer("remove "); //NOI18N
        toReturn.append(getCVSArguments());
        File[] files = getFiles();
        if (files != null) {
            for (int index = 0; index < files.length; index++) {
                toReturn.append(files[index].getName() + " "); //NOI18N
            }
        }
        return toReturn.toString();
    }

    /**
     * Takes the arguments and sets the command.
     * To be mainly used for automatic settings (like parsing the .cvsrc file).
     * @return true if the option (switch) was recognized and set
     */
    public boolean setCVSCommand(char opt, String optArg) {
        if (opt == 'l') {
            setRecursive(false);
        }
        else if (opt == 'R') {
            setRecursive(true);
        }
        else if (opt == 'f') {
            setDeleteBeforeRemove(true);
        }
        else {
            return false;
        }
        return true;
    }

    /**
     * Deletes all files being removed from the working directory.
     * Doesn't delete directories.
     * Attempts a recursive delete
     * @throws CommandException - in case the file cannot be deleted.
     */
    private void removeAll(File[] filesToDel)
            throws CommandException {
        if (filesToDel == null) {
            return;
        }
        for (int index = 0; index < filesToDel.length; index++) {
            File file = filesToDel[index];
            if (file.exists() && file.isFile()) {
                if (!file.delete()) {
                    throw new CommandException("Cannot delete file " + file.getAbsolutePath(), //NOI18N
                                               CommandException.getLocalMessage("RemoveCommand.cannotDelete", new Object[]{file.getAbsolutePath()})); //NOI18N
                }
            }
            else {
                // For directories remove only it's files.
                //  Preserve the cvs structure though.
                if (isRecursive() &&
                        !file.getName().equalsIgnoreCase("CVS")) { //NOI18N
                    removeAll(file.listFiles());
                }
            }
        }
    }

    /**
     * String returned by this method defines which options are available for this particular command
     */
    public String getOptString() {
        return "flR"; //NOI18N
    }

    /**
     * resets all switches in the command. After calling this method,
     * the command should have no switches defined and should behave defaultly.
     */
    public void resetCVSCommand() {
        setRecursive(true);
        setDeleteBeforeRemove(false);
    }

    /**
     * Returns the arguments of the command in the command-line style.
     * Similar to getCVSCommand() however without the files and command's name
     */
    public String getCVSArguments() {
        StringBuffer toReturn = new StringBuffer(""); //NOI18N
        if (!isRecursive()) {
            toReturn.append("-l "); //NOI18N
        }
        if (isDeleteBeforeRemove()) {
            toReturn.append("-f "); //NOI18N
        }
        return toReturn.toString();
    }
}
