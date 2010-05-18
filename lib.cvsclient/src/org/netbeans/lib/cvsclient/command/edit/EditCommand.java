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
package org.netbeans.lib.cvsclient.command.edit;

import java.io.*;
import java.util.*;

import org.netbeans.lib.cvsclient.*;
import org.netbeans.lib.cvsclient.admin.*;
import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.connection.*;
import org.netbeans.lib.cvsclient.event.*;
import org.netbeans.lib.cvsclient.file.*;
import org.netbeans.lib.cvsclient.request.*;

/**
 * @author  Thomas Singer
 */
public class EditCommand extends BasicCommand {

    /**
     * Returns the file used for backup the specified file in the edit command.
     */
    public static File getEditBackupFile(File file) {
        return new File(file.getParent(),
                        "CVS/Base/" + file.getName()); // NOI18N
    }


    private boolean checkThatUnedited;
    private boolean forceEvenIfEdited;
    private Watch temporaryWatch;

    private transient ClientServices clientServices;

    /**
     * Construct a new editors command.
     */
    public EditCommand() {
        resetCVSCommand();
    }

    /**
     * Executes this command.
     *
     * @param client the client services object that provides any necessary
     *               services to this command, including the ability to actually
     *               process all the requests.
     */
    public void execute(ClientServices clientServices, EventManager eventManager)
            throws CommandException {
        this.clientServices = clientServices;
        try {
            clientServices.ensureConnection();

            super.execute(clientServices, eventManager);

            addArgumentRequest(isCheckThatUnedited(), "-c"); // NOI18N
            addArgumentRequest(isForceEvenIfEdited(), "-f"); // NOI18N

            // now add the request that indicates the working directory for the
            // command
            addRequestForWorkingDirectory(clientServices);
            addRequest(CommandRequest.NOOP);

            clientServices.processRequests(requests);
        }
        catch (AuthenticationException ex) {
            //TODO: handle case, where connection wasn't possible to establish
        }
        catch (CommandException ex) {
            throw ex;
        }
        catch (EOFException ex) {
            throw new CommandException(ex, CommandException.getLocalMessage("CommandException.EndOfFile", null)); //NOI18N
        }
        catch (Exception ex) {
            throw new CommandException(ex, ex.getLocalizedMessage());
        }
        finally {
            requests.clear();
            this.clientServices = null;
        }
    }

    protected void addRequestForFile(File file, Entry entry) {
        String temporaryWatch = Watch.getWatchString(getTemporaryWatch());
        requests.add(new NotifyRequest(file, "E", temporaryWatch)); // NOI18N

        try {
            editFile(clientServices, file);
        }
        catch (IOException ex) {
            // ignore
        }
    }

    /**
     * Called when server responses with "ok" or "error", (when the command
     * finishes).
     */
    public void commandTerminated(TerminationEvent e) {
        if (builder != null) {
            builder.outputDone();
        }
    }

    /**
     * This method returns how the tag command would looklike when typed on the
     * command line.
     */
    public String getCVSCommand() {
        StringBuffer cvsCommandLine = new StringBuffer("edit "); //NOI18N
        cvsCommandLine.append(getCVSArguments());
        appendFileArguments(cvsCommandLine);
        return cvsCommandLine.toString();
    }

    /**
     * Takes the arguments and sets the command.
     * To be mainly used for automatic settings (like parsing the .cvsrc file)
     * @return true if the option (switch) was recognized and set
     */
    public boolean setCVSCommand(char opt, String optArg) {
        if (opt == 'R') {
            setRecursive(true);
        }
        else if (opt == 'l') {
            setRecursive(false);
        }
        else {
            return false;
        }
        return true;
    }

    /**
     * String returned by this method defines which options are available for
     * this command.
     */
    public String getOptString() {
        return "Rl"; //NOI18N
    }

    /**
     * Resets all switches in the command.
     * After calling this method, the command should have no switches defined
     * and should behave defaultly.
     */
    public void resetCVSCommand() {
        setRecursive(true);
        setCheckThatUnedited(false);
        setForceEvenIfEdited(true);
        setTemporaryWatch(null);
    }

    /**
     * Returns the arguments of the command in the command-line style.
     * Similar to getCVSCommand() however without the files and command's name
     */
    public String getCVSArguments() {
        StringBuffer cvsArguments = new StringBuffer();
        if (!isRecursive()) {
            cvsArguments.append("-l "); //NOI18N
        }
        return cvsArguments.toString();
    }

    /**
     * Returns whether to check for unedited files.
     */
    public boolean isCheckThatUnedited() {
        return checkThatUnedited;
    }

    /**
     * Sets whether to check for unedited files.
     * This is cvs' -c option.
     */
    public void setCheckThatUnedited(boolean checkThatUnedited) {
        this.checkThatUnedited = checkThatUnedited;
    }

    /**
     * Returns whether the edit is forces even if the files are edited.
     */
    public boolean isForceEvenIfEdited() {
        return forceEvenIfEdited;
    }

    /**
     * Sets whether the edit is forces even if the files are edited.
     * This is cvs' -f option.
     */
    public void setForceEvenIfEdited(boolean forceEvenIfEdited) {
        this.forceEvenIfEdited = forceEvenIfEdited;
    }

    /**
     * Returns the temporary watch.
     */
    public Watch getTemporaryWatch() {
        return temporaryWatch;
    }

    /**
     * Sets the temporary watch.
     * This is cvs' -a option.
     */
    public void setTemporaryWatch(Watch temporaryWatch) {
        this.temporaryWatch = temporaryWatch;
    }

    private void editFile(ClientServices clientServices, File file) throws IOException {
        addBaserevEntry(clientServices, file);
        FileUtils.copyFile(file, EditCommand.getEditBackupFile(file));
        FileUtils.setFileReadOnly(file, false);
    }

    /**
     * Create file CVS/Baserev with entries like
     * BEntry.java/1.2/
     */
    private void addBaserevEntry(ClientServices clientServices, File file) throws IOException {
        final Entry entry = clientServices.getEntry(file);
        if (entry == null || entry.getRevision() == null || entry.isNewUserFile() || entry.isUserFileToBeRemoved()) {
            throw new IllegalArgumentException("File does not have an Entry or Entry is invalid!"); // NOI18N
        }

        File baserevFile = new File(file.getParentFile(), "CVS/Baserev"); // NOI18N
        File backupFile = new File(baserevFile.getAbsolutePath() + '~');
        BufferedReader reader = null;
        BufferedWriter writer = null;
        boolean append = true;
        boolean writeFailed = true;
        final String entryStart = 'B' + file.getName() + '/';
        try {
            writer = new BufferedWriter(new FileWriter(backupFile));
            writeFailed = false;
            reader = new BufferedReader(new FileReader(baserevFile));

            for (String line = reader.readLine();
                 line != null;
                 line = reader.readLine()) {

                if (line.startsWith(entryStart)) {
                    append = false;
                }
                writeFailed = true;
                writer.write(line);
                writer.newLine();
                writeFailed = false;
            }
        }
        catch (IOException ex) {
            if (writeFailed) {
                throw ex;
            }
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException ex) {
                    // ignore
                }
            }
            if (writer != null) {
                try {
                    if (append && !writeFailed) {
                        writer.write(entryStart + entry.getRevision() + '/');
                        writer.newLine();
                    }
                } finally {
                    try {
                        writer.close();
                    }
                    catch (IOException ex) {
                        // ignore
                    }
                }
            }
        }
        baserevFile.delete();
        backupFile.renameTo(baserevFile);
    }
}
