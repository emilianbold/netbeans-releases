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
package org.netbeans.lib.cvsclient.command.commit;

import java.io.*;
import java.util.*;

import org.netbeans.lib.cvsclient.*;
import org.netbeans.lib.cvsclient.admin.*;
import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.connection.*;
import org.netbeans.lib.cvsclient.event.*;
import org.netbeans.lib.cvsclient.request.*;

/**
 * The command to commit any changes that have been made.
 * @author  Robert Greig
 */
public class CommitCommand extends BasicCommand {
    /**
     * The argument requests that must be added at the end.
     * These argument requests indicate the files to be committed
     */
    private final List argumentRequests = new LinkedList();

    /**
     * The log message used for the commit.
     */
    private String message;

    /**
     * Forces the commit of the file(s) even if no changes were done.
     * the standard behaviour is NOT-TO-BE recursive in this case.
     */
    private boolean forceCommit;

    /**
     * The filename for the file that defines the message.
     */
    private String logMessageFromFile;

    /**
     * Determines that no module program should run on the server.
     */
    private boolean noModuleProgram;

    /** Holds value of property toRevisionOrBranch. */
    private String toRevisionOrBranch;

    /**
     * Construct a CommitCommand.
     */
    public CommitCommand() {
        resetCVSCommand();
    }

    /**
     * Returns the commit message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the commit message.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Indicates whether the commit should be forced even if there are no
     * changes.
     */
    public boolean isForceCommit() {
        return forceCommit;
    }

    /**
     * Sets whether the commit should be forced even if there are no changes.
     */
    public void setForceCommit(boolean forceCommit) {
        this.forceCommit = forceCommit;
    }

    /**
     * Adds the appropriate requests for a given directory.
     * Sends a directory request followed by as many Entry and Modified requests
     * as required.
     * @param directory the directory to send requests for
     * @throws IOException if an error occurs constructing the requests
     */
    protected void addRequestsForDirectory(File directory)
            throws IOException {
        if (!directory.exists()) {
            return;
        }
        // remove localPath prefix from directory. If left with
        // nothing, use dot (".") in the directory request. Also remove the
        // trailing slash
        String dir = getRelativeToLocalPathInUnixStyle(directory);

        try {
            String repository = clientServices.getRepositoryForDirectory(
                    directory.getAbsolutePath());
            requests.add(new DirectoryRequest(dir, repository));
            String tag = clientServices.getStickyTagForDirectory(directory);
            if (tag != null) {
                requests.add(new StickyRequest(tag));
            }
        }
        catch (IOException ex) {
            System.err.println("An error occurred reading the respository " +
                               "for the directory " + dir + ": " + ex);
            ex.printStackTrace();
        }

        // Obtain a set of all files known to CVS. We union
        // this set with the set of files in the actual filesystem directory
        // to obtain a set of files to commit (or at least attempt to commit).
        Set set = clientServices.getAllFiles(directory);

        // We must add the local files (and directories) because the above
        // command does *not* return cvs controlled directories
        final File[] files = directory.listFiles();

        // get the union of the files in the directory and the files retrieved
        // from the Entries file.
        set.addAll(Arrays.asList(files));

        List subdirectories = null;
        if (isRecursive()) {
            subdirectories = new LinkedList();
        }

        for (Iterator it = set.iterator(); it.hasNext();) {
            File file = (File)it.next();
            if (file.getName().equals("CVS")) { //NOI18N
                continue;
            }

            try {
                final Entry entry = clientServices.getEntry(file);
                // a non-null entry means the file does exist in the
                // Entries file for this directory
                if (entry == null) {
                    continue;
                }

                // here file.isFile() is *not* used, because not existing
                // files (removed ones) should also be sent
                if (file.isFile()) {
                    sendEntryAndModifiedRequests(entry, file);
                }
                else if (isRecursive() && file.isDirectory()) {
                    File cvsSubDir = new File(file, "CVS"); //NOI18N
                    if (cvsSubDir.exists()) {
                        subdirectories.add(file);
                    }
                }
            }
            catch (IOException ex) {
                System.err.println("An error occurred getting the " +
                                   "Entry for file " + file + ": " + ex);
                ex.printStackTrace();
            }
        }

        if (isRecursive()) {
            for (Iterator it = subdirectories.iterator(); it.hasNext();) {
                File subdirectory = (File)it.next();
                addRequestsForDirectory(subdirectory);
            }
        }
    }

    /**
     * Add the appropriate requests for a single file.
     * A directory request is sent, followed by an Entry and Modified request.
     * @param file the file to send requests for
     * @throws IOException if an error occurs constructing the requests
     */
    protected void addRequestsForFile(File file)
            throws IOException {
        final File parentDirectory = file.getParentFile();
        // remove localPath prefix from directory. If left with
        // nothing, use dot (".") in the directory request
        String dir = getRelativeToLocalPathInUnixStyle(parentDirectory);

        try {
            // send a argument request indicating the file to update
            requests.add(new DirectoryRequest(dir, clientServices.
                                                   getRepositoryForDirectory(parentDirectory.
                                                                             getAbsolutePath())));
            String tag = clientServices.getStickyTagForDirectory(parentDirectory);
            if (tag != null) {
                requests.add(new StickyRequest(tag));
            }
        }
        catch (IOException ex) {
            System.err.println("An error occurred reading the respository " +
                               "for the directory " + dir + ": " + ex);
            ex.printStackTrace();
        }

        try {
            final Entry entry = clientServices.getEntry(file);
            // a non-null entry means the file does exist in the
            // Entries file for this directory
            if (entry != null) {
                sendEntryAndModifiedRequests(entry, file);
            }
        }
        catch (IOException ex) {
            System.err.println("An error occurred getting the Entry " +
                               "for file " + file + ": " + ex);
            ex.printStackTrace();
        }
    }

    /**
     * Should return true if unchanged files should not be sent to server.
     * If false is returned, all files will be sent to server
     * This method is used by <code>sendEntryAndModifiedRequests</code>.
     */
    protected boolean doesCheckFileTime() {
        return !isForceCommit();
    }

    /**
     * Execute the command.
     * @param client the client services object that provides any necessary
     *               services to this command, including the ability to actually
     *               process all the requests
     */
    public void execute(ClientServices client, EventManager em)
            throws CommandException, AuthenticationException {
        client.ensureConnection();

        super.execute(client, em);

        try {
            // add arguments.
            if (isForceCommit()) {
                requests.add(1, new ArgumentRequest("-f")); //NOI18N
                if (isRecursive()) {
                    requests.add(1, new ArgumentRequest("-R")); //NOI18N
                }
            }
            if (isNoModuleProgram()) {
                requests.add(1, new ArgumentRequest("-n")); //NOI18N
            }
            if (getToRevisionOrBranch() != null) {
                requests.add(1, new ArgumentRequest("-r")); //NOI18N
                requests.add(2, new ArgumentRequest(getToRevisionOrBranch()));
            }

            // build the message to send
            String message = getMessage();
            if (getLogMessageFromFile() != null) {
                message = loadLogFile(getLogMessageFromFile());
            }
            if (message != null) {
                message = message.trim();
            }
            if (message == null
                    || message.length() == 0) {
                message = "no message"; //NOI18N
            }
            addMessageRequest(message);

            addRequestForWorkingDirectory(client);
            requests.addAll(argumentRequests);
            argumentRequests.clear(); // MK sanity check.
            addArgumentRequests();
            requests.add(CommandRequest.COMMIT);

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

    protected void addArgumentRequests() {
        if (isForceCommit()) {
            Iterator it = requests.iterator();
            String directory = "";
            List args = new LinkedList();
            while (it.hasNext()) {
                Object req = it.next();
                if (req instanceof org.netbeans.lib.cvsclient.request.DirectoryRequest) {
                    org.netbeans.lib.cvsclient.request.DirectoryRequest dirReq = (org.netbeans.lib.cvsclient.request.DirectoryRequest)req;
                    // haven't checked but I'm almost sure that within the Argument request always the local directory is used.
                    directory = dirReq.getLocalDirectory();
                }
                else if (req instanceof org.netbeans.lib.cvsclient.request.EntryRequest) {
                    org.netbeans.lib.cvsclient.request.EntryRequest entReq = (org.netbeans.lib.cvsclient.request.EntryRequest)req;
                    String argument = null;
                    if (directory.length() == 0) {
                        argument = entReq.getEntry().getName();
                    }
                    else {
                        argument = directory + '/' + entReq.getEntry().getName();
                    }
                    args.add(new ArgumentRequest(argument));
                }
            }
            it = args.iterator();
            while (it.hasNext()) {
                requests.add(it.next());
            }
        }
        else {
            super.addArgumentRequests();
        }
    }

    /**
     * This method returns how the command would looklike when typed on the command line.
     * Example: checkout -p CvsCommand.java
     * @returns <command's name> [<parameters>] files/dirs
     */
    public String getCVSCommand() {
        StringBuffer toReturn = new StringBuffer("commit "); //NOI18N
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
        if (opt == 'm') {
            setMessage(optArg);
        }
        else if (opt == 'l') {
            setRecursive(false);
        }
        else if (opt == 'R') {
            setRecursive(true);
        }
        else if (opt == 'f') {
            setForceCommit(true);
        }
        else if (opt == 'F') {
            setLogMessageFromFile(optArg);
        }
        else if (opt == 'r') {
            setToRevisionOrBranch(optArg);
        }
        else if (opt == 'n') {
            setNoModuleProgram(true);
        }
        else {
            return false;
        }
        return true;
    }

    /**
     * Returns a String defining which options are available for this command.
     */
    public String getOptString() {
        return "m:flRnF:r:"; //NOI18N
    }

    /**
     * Method that is called while the command is being executed.
     * Descendants can override this method to return a Builder instance
     * that will parse the server's output and create data structures.
     */
    public Builder createBuilder(EventManager eventMan) {
        return new CommitBuilder(eventMan, getLocalDirectory(), clientServices.getRepository());
    }

    /**
     * Generates the Argument/Argumentx series of requests depending
     * on the number of lines in the message request.
     */
    private void addMessageRequest(String message) {
        requests.add(new ArgumentRequest("-m")); //NOI18N
        StringTokenizer token = new StringTokenizer(message, "\n", false); //NOI18N
        boolean first = true;
        while (token.hasMoreTokens()) {
            if (first) {
                requests.add(new ArgumentRequest(token.nextToken()));
                first = false;
            }
            else {
                requests.add(new ArgumentxRequest(token.nextToken()));
            }
        }
    }

    /**
     * Returns the filename for the file that defines the message.
     */
    public String getLogMessageFromFile() {
        return logMessageFromFile;
    }

    /**
     * Sets the filename for the file that defines the message.
     */
    public void setLogMessageFromFile(String logMessageFromFile) {
        this.logMessageFromFile = logMessageFromFile;
    }

    /**
     * Returns whether no module program should be executed on the server.
     */
    public boolean isNoModuleProgram() {
        return noModuleProgram;
    }

    /**
     * Sets whether no module program should run on the server
     */
    public void setNoModuleProgram(boolean noModuleProgram) {
        this.noModuleProgram = noModuleProgram;
    }

    /** Getter for property toRevisionOrBranch.
     * @return Value of property toRevisionOrBranch.
     */
    public String getToRevisionOrBranch() {
        return toRevisionOrBranch;
    }

    /** Setter for property toRevisionOrBranch.
     * @param toRevisionOrBranch New value of property toRevisionOrBranch.
     */
    public void setToRevisionOrBranch(String toRevBranch) {
        this.toRevisionOrBranch = toRevBranch;
    }

    private String loadLogFile(String fileName)
            throws CommandException {
        StringBuffer buffer = new StringBuffer();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n"); //NOI18N
            }
        }
        catch (FileNotFoundException ex) {
            throw new CommandException(ex,
                                       CommandException.getLocalMessage("CommitCommand.logInfoFileNotExists", new Object[]{fileName})); //NOI18N
        }
        catch (IOException ex) {
            throw new CommandException(ex,
                                       CommandException.getLocalMessage("CommitCommand.errorReadingLogFile", new Object[]{fileName})); //NOI18N
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException exc) {
                }
            }
        }
        return buffer.toString();
    }

    /**
     * Resets all switches in the command.
     * After calling this method, the command should have no switches defined
     * and should behave defaultly.
     */
    public void resetCVSCommand() {
        setMessage(null);
        setRecursive(true);
        setForceCommit(false);
        setLogMessageFromFile(null);
        setNoModuleProgram(false);
        setToRevisionOrBranch(null);
    }

    /**
     * Returns the arguments of the command in the command-line style.
     * Similar to getCVSCommand() however without the files and command's name.
     */
    public String getCVSArguments() {
        StringBuffer toReturn = new StringBuffer();
        if (!isRecursive()) {
            toReturn.append("-l "); //NOI18N
        }
        if (isForceCommit()) {
            toReturn.append("-f "); //NOI18N
            if (isRecursive()) {
                toReturn.append("-R ");
            }
        }
        if (isNoModuleProgram()) {
            toReturn.append("-n "); //NOI18N
        }
        if (getToRevisionOrBranch() != null) {
            toReturn.append("-r "); //NOI18N
            toReturn.append(getToRevisionOrBranch() + " "); //NOI18N
        }
        if (getLogMessageFromFile() != null) {
            toReturn.append("-F "); //NOI18N
            toReturn.append(getLogMessageFromFile());
            toReturn.append(" "); //NOI18N
        }
        if (getMessage() != null) {
            toReturn.append("-m \""); //NOI18N
            toReturn.append(getMessage());
            toReturn.append("\" "); //NOI18N
        }
        return toReturn.toString();
    }
}
