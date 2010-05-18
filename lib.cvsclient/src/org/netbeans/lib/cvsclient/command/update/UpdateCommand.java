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
 *
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
 *
 * Contributor(s): Robert Greig.
 *****************************************************************************/
package org.netbeans.lib.cvsclient.command.update;

import java.io.*;
import java.text.*;
import java.util.*;

import org.netbeans.lib.cvsclient.*;
import org.netbeans.lib.cvsclient.file.FileUtils;
import org.netbeans.lib.cvsclient.admin.*;
import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.connection.*;
import org.netbeans.lib.cvsclient.event.*;
import org.netbeans.lib.cvsclient.request.*;

/**
 * The Update command. Updates files that have previously been checked out
 * from the repository with the checkout command. Modified files are not
 * overwritten.
 * @author  Robert Greig
 */
public class UpdateCommand extends BasicCommand
        implements TemporaryFileCreator {

    // This format possibly may be set by the user of the library later.
    private static final String RENAME_FORMAT = "{0}/.#{1}.{2}"; //NOI18N
    private static final Object[] FORMAT_PARAMETER = new Object[3]; // { path, filename, revision }

    /**
     * A store of potentially empty directories. When a directory has a file
     * in it, it is removed from this set. This set allows the prune option
     * to be implemented.
     */
    private final Set emptyDirectories = new HashSet();
    /**
     * Whether to build directories, like checkout does (this is the -d option
     * in command-line CVS).
     */
    private boolean buildDirectories;

    /**
     * Determines whether to get a clean copy from the server.
     * This overrides even locally modified files.
     */
    private boolean cleanCopy;

    /**
     * Whether to prune directories, i.e. remove any directories that do not
     * contain any files. This is the -P option in command-line CVS).
     */
    private boolean pruneDirectories;

    /**
     * Determines wheather the output of the command is processed on standard output.
     * Default is false. If true, nothing is done to local working files.
     */
    private boolean pipeToOutput;

    /**
     * Resets any sticky tags/dates/options imposed on the updated file(s).
     */
    private boolean resetStickyOnes;

    /**
     * Use head revision if a revision meeting criteria set by switches -r/-D
     * (tag/date) is not found.
     */
    private boolean useHeadIfNotFound;

    /**
     * equals the -D switch of command line cvs.
     */
    private String updateByDate;

    /**
     * Equals the -r switch of command-line cvs.
     */
    private String updateByRevision;

    /**
     * Use this keyword substitution for the command.
     * does not include the -k switch part.
     */
    private KeywordSubstitutionOptions keywordSubst;

    /**
     * First of the 2 possible -j switches that merge 2 different revisions.
     * If only this property is set, the current working file is merged
     * with the specified one.
     */
    private String mergeRevision1;

    /**
     * Second of the 2 possible -j switches that merge 2 different revisions.
     * Assumes the first -j switch (mergeRevision1 property) is set.
     * Then the update commands merges the sources of these 2 revisons specified
     * by the -j switches.
     */
    private String mergeRevision2;

    /**
     * Construct a new update command.
     */
    public UpdateCommand() {
        // TODO: move up the hierarchy ?
        resetCVSCommand();
    }

    /**
     * Method that is called while the command is being executed.
     * Descendants can override this method to return a Builder instance
     * that will parse the server's output and create data structures.
     */
    public Builder createBuilder(EventManager eventManager) {
        if (isPipeToOutput()) {
            return new PipedFilesBuilder(eventManager, this, this);
        }
        return new UpdateBuilder(eventManager, getLocalDirectory());
    }

    /**
     * If <code>getCleanCopy()</code> returns true, the files will be treated
     * as not existing.
     */
    protected void sendEntryAndModifiedRequests(Entry entry, File file) {
        if (isCleanCopy() && file != null && entry != null) {
            if (!isPipeToOutput()) {
                FORMAT_PARAMETER[0] = file.getParent();
                FORMAT_PARAMETER[1] = file.getName();
                FORMAT_PARAMETER[2] = entry.getRevision();

                String filename = MessageFormat.format(RENAME_FORMAT, FORMAT_PARAMETER);
                try {
                    FileUtils.copyFile(file, new File(filename));
                } catch (IOException e) {
                    // backup copy will not be created
                }
            }
            file = null;
        }
        super.sendEntryAndModifiedRequests(entry, file);
    }

    /**
     * Set whether to build directories. This is the -d option in command-line
     * CVS.
     */
    public void setBuildDirectories(boolean buildDirectories) {
        this.buildDirectories = buildDirectories;
    }

    /**
     * Returns whether to build directories.
     * @return true if directories are to be built, false otherwise
     */
    public boolean isBuildDirectories() {
        return buildDirectories;
    }

    /**
     * Sets whether to get a clean copy from the server.
     * Even locally modified files will not merged but overridden.
     * This is the -C option in the command-line CVS.
     */
    public void setCleanCopy(boolean cleanCopy) {
        this.cleanCopy = cleanCopy;
    }

    /**
     * Returns whether to get a clean copy from the server.
     */
    public boolean isCleanCopy() {
        return cleanCopy;
    }

    /**
     * Set whether to prune directories. This is the -P option in the command-
     * line CVS.
     */
    public void setPruneDirectories(boolean pruneDirectories) {
        this.pruneDirectories = pruneDirectories;
    }

    /**
     * Returns whether to prune directories.
     * @return true if directories should be removed if they contain no files,
     * false otherwise.
     */
    public boolean isPruneDirectories() {
        return pruneDirectories;
    }

    /**
     * Execute the command.
     * @param client the client services object that provides any necessary
     *               services to this command, including the ability to actually
     *               process all the requests
     */
    public void execute(ClientServices client, EventManager eventManager)
            throws CommandException, AuthenticationException {
        client.ensureConnection();

        super.execute(client, eventManager);

        emptyDirectories.clear();

        try {
            // now add the request that indicates the working directory for the
            // command
            if (!isRecursive()) {
                requests.add(1, new ArgumentRequest("-l")); //NOI18N
            }
            if (isBuildDirectories()) {
                requests.add(1, new ArgumentRequest("-d")); //NOI18N
            }
            if (isCleanCopy() && !isPipeToOutput()) {
                requests.add(1, new ArgumentRequest("-C")); //NOI18N
            }
            if (isPipeToOutput()) {
                requests.add(1, new ArgumentRequest("-p")); //NOI18N
            }
            if (isResetStickyOnes()) {
                requests.add(1, new ArgumentRequest("-A")); //NOI18N
            }
            if (isUseHeadIfNotFound()) {
                requests.add(1, new ArgumentRequest("-f")); //NOI18N
            }
            if (getUpdateByDate() != null) {
                requests.add(1, new ArgumentRequest("-D")); //NOI18N
                requests.add(2, new ArgumentRequest(getUpdateByDate()));
            }
            else if (getUpdateByRevision() != null) {
                requests.add(1, new ArgumentRequest("-r")); //NOI18N
                requests.add(2, new ArgumentRequest(getUpdateByRevision()));
            }
            if (getMergeRevision1() != null) {
                requests.add(1, new ArgumentRequest("-j")); //NOI18N
                requests.add(2, new ArgumentRequest(getMergeRevision1()));

                if (getMergeRevision2() != null) {
                    requests.add(3, new ArgumentRequest("-j")); //NOI18N
                    requests.add(4, new ArgumentRequest(getMergeRevision2()));
                }
            }
            if (getKeywordSubst() != null) {
                requests.add(1, new ArgumentRequest("-k")); //NOI18N
                requests.add(2, new ArgumentRequest(getKeywordSubst().toString()));
            }
            requests.add(1, new ArgumentRequest("-u")); //NOI18N

            addRequestForWorkingDirectory(client);
            addArgumentRequests();
            addRequest(CommandRequest.UPDATE);

            // hack - now check for the entry request for removed files
            // only when p with -r or -D is on
            if (isPipeToOutput() && (getUpdateByRevision() != null || getUpdateByDate() != null)) {
                ListIterator it = requests.listIterator();
                while (it.hasNext()) {
                    Object req = it.next();
                    if (req instanceof EntryRequest) {
                        EntryRequest eReq = (EntryRequest)req;
                        Entry entry = eReq.getEntry();
                        if (entry.getRevision().startsWith("-")) {//NOI18N
                            entry.setRevision(entry.getRevision().substring(1));
                        }
                        it.set(new EntryRequest(entry));
                        it.add(new UnchangedRequest(entry.getName()));
                    }
                }
            }
            // end of hack..
            client.processRequests(requests);
            if (pruneDirectories && (getGlobalOptions() == null || !getGlobalOptions().isDoNoChanges())) {
                pruneEmptyDirectories(client);
            }
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
        }
    }

    /**
     * Getter for property pipeToOutput.
     * @return Value of property pipeToOutput.
     */
    public boolean isPipeToOutput() {
        return pipeToOutput;
    }

    /**
     * Setter for property pipeToOutput.
     * @param pipeToOutput New value of property pipeToOutput.
     */
    public void setPipeToOutput(boolean pipeToOutput) {
        this.pipeToOutput = pipeToOutput;
    }

    /**
     * Getter for property resetStickyOnes.
     * @return Value of property resetStickyOnes.
     */
    public boolean isResetStickyOnes() {
        return resetStickyOnes;
    }

    /**
     * Setter for property resetStickyOnes.
     * @param resetStickyOnes New value of property resetStickyOnes.
     */
    public void setResetStickyOnes(boolean resetStickyOnes) {
        this.resetStickyOnes = resetStickyOnes;
    }

    /**
     * Getter for property useHeadIfNotFound.
     * @return Value of property useHeadIfNotFound.
     */
    public boolean isUseHeadIfNotFound() {
        return useHeadIfNotFound;
    }

    /**
     * Setter for property useHeadIfNotFound.
     * @param useHeadIfNotFound New value of property useHeadIfNotFound.
     */
    public void setUseHeadIfNotFound(boolean useHeadIfNotFound) {
        this.useHeadIfNotFound = useHeadIfNotFound;
    }

    /**
     * Getter for property updateByDate.
     * @return Value of property updateByDate.
     */
    public String getUpdateByDate() {
        return updateByDate;
    }

    /**
     * Setter for property updateByDate.
     * @param updateByDate New value of property updateByDate.
     */
    public void setUpdateByDate(String updateByDate) {
        this.updateByDate = getTrimmedString(updateByDate);
    }

    /**
     * Getter for property updateByRevision.
     * @return Value of property updateByRevision.
     */
    public String getUpdateByRevision() {
        return updateByRevision;
    }

    /**
     * Setter for property updateByRevision.
     * @param updateByRevision New value of property updateByRevision.
     */
    public void setUpdateByRevision(String updateByRevision) {
        this.updateByRevision = getTrimmedString(updateByRevision);
    }

    /**
     * Getter for property keywordSubst.
     * @return Value of property keywordSubst.
     */
    public KeywordSubstitutionOptions getKeywordSubst() {
        return keywordSubst;
    }

    /**
     * Setter for property keywordSubst.
     * @param keywordSubst New value of property keywordSubst.
     */
    public void setKeywordSubst(KeywordSubstitutionOptions keywordSubst) {
        this.keywordSubst = keywordSubst;
    }

    /**
     * Method that creates a temporary file.
     */
    public File createTempFile(String filename) throws IOException {
        File temp = File.createTempFile("cvs", ".dff", getGlobalOptions().getTempDir()); //NOI18N
        temp.deleteOnExit();
        return temp;
    }

    /**
     * This method returns how the command would looklike when typed on the
     * command line.
     * Each command is responsible for constructing this information.
     * @returns <command's name> [<parameters>] files/dirs.
     * Example: checkout -p CvsCommand.java
     */
    public String getCVSCommand() {
        StringBuffer toReturn = new StringBuffer("update "); //NOI18N
        toReturn.append(getCVSArguments());
        File[] files = getFiles();
        if (files != null) {
            for (int index = 0; index < files.length; index++) {
                toReturn.append(files[index].getName());
                toReturn.append(' ');
            }
        }
        return toReturn.toString();
    }

    /**
     * Returns the arguments of the command in the command-line style.
     * Similar to getCVSCommand() however without the files and command's name
     */
    public String getCVSArguments() {
        StringBuffer toReturn = new StringBuffer(""); //NOI18N
        if (isPipeToOutput()) {
            toReturn.append("-p "); //NOI18N
        }
        if (isCleanCopy()) {
            toReturn.append("-C "); //NOI18N
        }
        if (!isRecursive()) {
            toReturn.append("-l "); //NOI18N
        }
        if (isBuildDirectories()) {
            toReturn.append("-d "); //NOI18N
        }
        if (isPruneDirectories()) {
            toReturn.append("-P "); //NOI18N
        }
        if (isResetStickyOnes()) {
            toReturn.append("-A "); //NOI18N
        }
        if (isUseHeadIfNotFound()) {
            toReturn.append("-f "); //NOI18N
        }
        if (getKeywordSubst() != null) {
            toReturn.append("-k"); //NOI18N
            toReturn.append(getKeywordSubst().toString());
            toReturn.append(' ');
        }
        if (getUpdateByRevision() != null) {
            toReturn.append("-r "); //NOI18N
            toReturn.append(getUpdateByRevision());
            toReturn.append(' ');
        }
        if (getUpdateByDate() != null) {
            toReturn.append("-D "); //NOI18N
            toReturn.append(getUpdateByDate());
            toReturn.append(' ');
        }
        if (getMergeRevision1() != null) {
            toReturn.append("-j "); //NOI18N
            toReturn.append(getMergeRevision1());
            toReturn.append(' ');

            if (getMergeRevision2() != null) {
                toReturn.append("-j "); //NOI18N
                toReturn.append(getMergeRevision2());
                toReturn.append(' ');
            }
        }
        return toReturn.toString();
    }

    /**
     * Takes the arguments and by parsing them, sets the command. To be mainly
     * used for automatic settings (like parsing the .cvsrc file)
     */
    public boolean setCVSCommand(char opt, String optArg) {
        if (opt == 'R') {
            setRecursive(true);
        }
        else if (opt == 'C') {
            setCleanCopy(true);
        }
        else if (opt == 'l') {
            setRecursive(false);
        }
        else if (opt == 'd') {
            setBuildDirectories(true);
        }
        else if (opt == 'P') {
            setPruneDirectories(true);
        }
        else if (opt == 'A') {
            setResetStickyOnes(true);
        }
        else if (opt == 'f') {
            setUseHeadIfNotFound(true);
        }
        else if (opt == 'D') {
            setUpdateByDate(optArg.trim());
        }
        else if (opt == 'r') {
            setUpdateByRevision(optArg.trim());
        }
        else if (opt == 'k') {
            KeywordSubstitutionOptions keywordSubst =
                    KeywordSubstitutionOptions.findKeywordSubstOption(optArg);
            setKeywordSubst(keywordSubst);
        }
        else if (opt == 'p') {
            setPipeToOutput(true);
        }
        else if (opt == 'j') {
            if (getMergeRevision1() == null) {
                setMergeRevision1(optArg);
            }
            else {
                setMergeRevision2(optArg);
            }
        }
        else {
            // TODO - now silently ignores not recognized switches.
            return false;
        }
        return true;
    }

    /**
     * Resets all switches in the command.
     * After calling this method, the command should have no switches defined
     * and should behave defaultly.
     */
    public void resetCVSCommand() {
        setRecursive(true);
        setCleanCopy(false);
        setBuildDirectories(false);
        setPruneDirectories(false);
        setResetStickyOnes(false);
        setUseHeadIfNotFound(false);
        setUpdateByDate(null);
        setUpdateByRevision(null);
        setKeywordSubst(null);
        setPipeToOutput(false);
        setMergeRevision1(null);
        setMergeRevision2(null);
    }

    /**
     * Called when the server wants to send a message to be displayed to
     * the user. The message is only for information purposes and clients
     * can choose to ignore these messages if they wish.
     * @param e the event
     */
    public void messageSent(MessageEvent e) {
        super.messageSent(e);
        // we use this event to determine which directories need to be checked
        // for updating
        if (!pruneDirectories) {
            return;
        }

        final String relativePath = CommandUtils.getExaminedDirectory(e.getMessage(),
                                                                      UpdateBuilder.EXAM_DIR);
        if (relativePath == null) {
            return;
        }

        // dont delete the current directory, even if it's empty
        if (relativePath.equals(".")) { //NOI18N
            return;
        }

        emptyDirectories.add(new File(getLocalDirectory(), relativePath));
    }

    /**
     * Prunes a directory, recursively pruning its subdirectories
     * @param directory the directory to prune
     */
    private boolean pruneEmptyDirectory(File directory, ClientServices client)
            throws IOException {
        final File[] contents = directory.listFiles();

        // should never be null, but just in case...
        if (contents == null) {
            return true;
        }

        for (int i = 0; i < contents.length; i++) {
            if (contents[i].isFile()) {
                return false;
            }

            // Skip the cvs directory
            if (contents[i].getName().equals("CVS")) {
                //NOI18N
                continue;
            }

            if (!pruneEmptyDirectory(contents[i], client)) {
                return false;
            }
        }

        // check this is a CVS directory and not some directory the user
        // has stupidly called CVS...
        if (new File(directory, "CVS/Entries").isFile() && new File(directory, "CVS/Repository").isFile()) {
            final File adminDir = new File(directory, "CVS"); //NOI18N
            // we must NOT delete a directory if it contains valuable entries
            for (Iterator i = clientServices.getEntries(directory); i.hasNext(); ) {
                Entry entry = (Entry) i.next();
                if (entry.getName() != null && entry.isUserFileToBeRemoved()) return false;
            }
            deleteRecursively(adminDir);
            directory.delete();
            // if the client still makes this directory's entries available, do not delete its entry
            if (!client.exists(directory)) client.removeEntry(directory);
            return true;
        }

        return false;
    }

    /**
     * Deletes a directory and all files and directories inside the directory.
     *
     * @param dir directory to delete
     */
    private void deleteRecursively(File dir) {
      File [] files = dir.listFiles();
      for (int i = 0; i < files.length; i++) {
        File file = files[i];
        if (file.isDirectory()) {
          deleteRecursively(file);
        } else {
          file.delete();
        }
      }
      dir.delete();
    }

    /**
     * Remove any directories that don't contain any files
     */
    private void pruneEmptyDirectories(ClientServices client)
            throws IOException {
        for (Iterator it = emptyDirectories.iterator(); it.hasNext();) {
            final File dir = (File)it.next();
            // we might have deleted it already (due to recursive delete)
            // so we need to check existence
            if (dir.exists()) {
                pruneEmptyDirectory(dir, client);
            }
        }
        emptyDirectories.clear();
    }

    /**
     * String returned by this method defines which options are available for this particular command
     */
    public String getOptString() {
        return "RCnldPAfD:r:pj:k:"; //NOI18N
    }

    /** Getter for property mergeRevision1.
     * @return Value of property mergeRevision1.
     */
    public String getMergeRevision1() {
        return mergeRevision1;
    }

    /** Setter for property mergeRevision1.
     * @param mergeRevision1 New value of property mergeRevision1.
     */
    public void setMergeRevision1(String mergeRevision1) {
        this.mergeRevision1 = getTrimmedString(mergeRevision1);
    }

    /** Getter for property mergeRevision2.
     * @return Value of property mergeRevision2.
     */
    public String getMergeRevision2() {
        return mergeRevision2;
    }

    /** Setter for property mergeRevision2.
     * @param mergeRevision2 New value of property mergeRevision2.
     */
    public void setMergeRevision2(String mergeRevision2) {
        this.mergeRevision2 = getTrimmedString(mergeRevision2);
    }
}
