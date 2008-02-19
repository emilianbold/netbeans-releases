/*****************************************************************************
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
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
package org.netbeans.lib.cvsclient.command.add;

import java.io.*;
import java.util.*;

import org.netbeans.lib.cvsclient.*;
import org.netbeans.lib.cvsclient.util.SimpleStringPattern;
import org.netbeans.lib.cvsclient.admin.*;
import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.connection.*;
import org.netbeans.lib.cvsclient.event.*;
import org.netbeans.lib.cvsclient.request.*;

/**
 * Adds a file or directory.
 * @author  Robert Greig
 */
public class AddCommand extends BuildableCommand {
    /**
     * Constants that identify a message that creates a directory in
     * repository.
     */
    private static final String DIR_ADDED = " added to the repository"; //NOI18N
    private static final String DIRECTORY = "Directory "; //NOI18N

    /**
     * The requests that are sent and processed.
     */
    private List requests;

    /**
     * The argument requests that are collected and sent in the end just before the
     * add request.
     */
    private final List argumentRequests = new LinkedList();

    /**
     * The list of new directories.
     */
    /*
        private HashMap newDirList;
    */
    private final List newDirList = new LinkedList();

    /**
     * The client services that are provided to this command.
     */
    private ClientServices clientServices;

    /**
     * The files and/or directories to operate on.
     */
    private File[] files;

    /**
     * Holds value of property message, (add's switch -m).
     */
    private String message;

    /**
     * Holds value of property keywordSubst.
     */
    private KeywordSubstitutionOptions keywordSubst;



    /**
     *This is the merged wrapper map that contains has both server side
     *and client side wrapper settings merged except for the .cvswrappers
     *in the individual directories
     */
    private Map wrapperMap;

    /**
     * Holds the cvswrappers map for each directory, keyed
     * by directory name
     */
    private HashMap dir2WrapperMap = new HashMap(16);

    private static final Map EMPTYWRAPPER = new HashMap(1);
    /**
     * Constructor.
     */
    public AddCommand() {
        resetCVSCommand();
    }

    /**
     * Set the files and/or directories on which to execute the command.
     * Sorts the paameter so that directories are first and files follow.
     * That way a directory and it's content will be passed correctly.
     * The user of the library has to specify all the files+dirs being added though.
     * This is just a sanity check, so that no unnessesary errors occur.
     */
    public void setFiles(File[] files) {
        this.files = files;
        if (files == null) {
            return;
        }

        // sort array: directories first, files follow
        this.files = new File[files.length];
        int dirCount = 0;
        int fileCount = 0;
        int totalCount = files.length;
        for (int index = 0; index < totalCount; index++) {
            File currentFile = files[index];
            if (currentFile.isDirectory()) {
                this.files[dirCount] = currentFile;
                dirCount++;
            }
            else {
                this.files[totalCount - (1 + fileCount)] = currentFile;
                fileCount++;
            }
        }
    }

    /**
     * Get the files and/or directories specified for this command to operate
     * on.
     * @return the array of Files
     */
    public File[] getFiles() {
        return files;
    }

    /**
     * @param ending - the ending part of the file's pathname.. path separator is cvs's default '/'
     */
    public File getFileEndingWith(String ending) {
        String locEnding = ending.replace('\\', '/');
        String localDir = getLocalDirectory().replace('\\','/');
        int index = 0;
        for (index = 0; index < files.length; index++) {
            String path = files[index].getAbsolutePath();
            String parentPath = files[index].getParentFile().getAbsolutePath().replace('\\', '/');
            path = path.replace('\\', '/');
            if ((path.endsWith(locEnding) && locEnding.indexOf('/') >= 0) || 
                   (files[index].getName().equals(locEnding) && parentPath.equals(localDir))) {
                return files[index];
            }
        }
        return null;
    }

    /**
     * Getter for property message.
     * @return Value of property message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Setter for property message.
     * @param message New value of property message.
     */
    public void setMessage(String message) {
        this.message = message;
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
     * Add requests for a particular file or directory to be added.
     */
    protected void addRequests(File file)
            throws IOException, CommandException {
        if (file.isDirectory()) {
            addRequestsForDirectory(file, false);
        }
        else {
            addRequestsForFile(file);
        }
    }

    /**
     * Add requests for a particular directory.
     * @param directory the directory to add
     * @param adding - for the directory to be added, set to true.
     *                 used internally to recurse Directory requests.
     * @throws IOException if an error occurs
     */
    private void addRequestsForDirectory(File directory, boolean recursion)
            throws IOException {

        File parentDirectory = directory.getParentFile();
        String dir = recursion
                ? getRelativeToLocalPathInUnixStyle(directory)
                : getRelativeToLocalPathInUnixStyle(parentDirectory);

        String partPath;
        if (dir.equals(".")) { //NOI18N
            partPath = directory.getName();
        }
        else {
            // trim the leading slash from the pathname we end up with
            // (e.g. we end up with something like \banana\foo
            // and this gives us banana\foo). Also replace backslashes with
            // forward slashes. The standard CVS server doesn't like
            // backslashes very much.
            partPath = dir + "/" + directory.getName(); //NOI18N
            // recursively scroll back to the localPath..
            addRequestsForDirectory(parentDirectory, true);
        }

        if (recursion) {
            partPath = dir;
        }

        // Note that the repository file for the directory being added has not
        // been created yet, so we are forced to read the repository for
        // the parent directory and build the appropriate entry by tagging
        // on the directory name (called partPath here)
        String repository;
        String tag;

        if (recursion) {
            repository = clientServices.getRepositoryForDirectory(
                    directory.getAbsolutePath());
            tag = clientServices.getStickyTagForDirectory(directory);
        }
        else {
            repository = clientServices.getRepositoryForDirectory(
                    parentDirectory.getAbsolutePath());
            if (repository.endsWith(".")) {
                repository = repository.substring(0, repository.length() - 1) + directory.getName();
            } else {
                repository = repository + "/" + directory.getName(); //NOI18N
            }
            tag = clientServices.getStickyTagForDirectory(parentDirectory);
        }

        requests.add(new DirectoryRequest(partPath, repository));
        if (tag != null) {
            requests.add(new StickyRequest(tag));
        }

        if (!recursion) {
            argumentRequests.add(new ArgumentRequest(partPath));
/*
            newDirList.put(partPath, repository);
*/
            newDirList.add(new Paths(partPath, repository));
        }
        // MK argument after Dir request.. also with the rel path from the current working dir
    }

    /**
     * Add requests for a particular file.
     */
    protected void addRequestsForFile(File file)
            throws IOException, CommandException {
        File directory = file.getParentFile();
        String dir = getRelativeToLocalPathInUnixStyle(directory);

        String repository = clientServices.getRepositoryForDirectory(
                directory.getAbsolutePath());
        requests.add(new DirectoryRequest(dir, repository));
        String tag = clientServices.getStickyTagForDirectory(directory);
        if (tag != null) {
            requests.add(new StickyRequest(tag));
        }

        Entry entry = clientServices.getEntry(file);

        if (entry != null) {
            requests.add(new EntryRequest(entry));
        }
        else {

            Map directoryLevelWrapper = (Map) dir2WrapperMap.get(dir);
            if (directoryLevelWrapper == null) {

                // we have not parsed the cvs wrappers for this directory
                // read the wrappers for this directory

                File wrapperFile = new File(directory, ".cvswrappers"); // NOI18N
                if (wrapperFile.exists()) {
                    directoryLevelWrapper = new HashMap(5);
                    WrapperUtils.readWrappersFromFile(wrapperFile, directoryLevelWrapper);
                }
                else {
                    directoryLevelWrapper = EMPTYWRAPPER;
                }

                // store the wrapper map indexed by directory name
                dir2WrapperMap.put(dir, directoryLevelWrapper);
            }

            boolean isBinary = isBinary(clientServices, file.getName(), directoryLevelWrapper);

            if (isBinary) {
                requests.add(new KoptRequest("-kb"));  // NOI18N
            }
            requests.add(new IsModifiedRequest(file));
        }

        if (dir.equals(".")) { //NOI18N
            argumentRequests.add(new ArgumentRequest(file.getName(), true));
        }
        else {
            argumentRequests.add(new ArgumentRequest(dir + "/" + file.getName())); //NOI18N
        }
    }


    /**
     * Returns true, if the file for the specified filename should be treated as
     * a binary file.
     *
     * The information comes from the wrapper map and the set keywordsubstitution.
     */
    private boolean isBinary(ClientServices client, String filename, Map directoryLevelWrappers) throws CommandException {
        KeywordSubstitutionOptions keywordSubstitutionOptions = getKeywordSubst();

        if (keywordSubstitutionOptions == KeywordSubstitutionOptions.BINARY) {
            return true;
        }

        // The keyWordSubstitutions was set based on MIME-types  by
        // CVSAdd which had no notion of cvswrappers. Therefore some
        // filetypes returned as text may actually be binary within CVS
        // We check for those files here

        boolean wrapperFound = false;

        if (wrapperMap == null) {
            // process the wrapper settings as we have not done it before.
            wrapperMap = WrapperUtils.mergeWrapperMap(client);
        }


        for (Iterator it = wrapperMap.keySet().iterator(); it.hasNext();) {
            SimpleStringPattern pattern = (SimpleStringPattern)it.next();
            if (pattern.doesMatch(filename)) {
                keywordSubstitutionOptions = (KeywordSubstitutionOptions)wrapperMap.get(pattern);
                wrapperFound = true;
                break;
            }
        }

        // if no wrappers are found to match the server and local settings, try
        // the wrappers for this local directory
        if (!wrapperFound && (directoryLevelWrappers != null) && (directoryLevelWrappers!=EMPTYWRAPPER)) {
            for (Iterator it = directoryLevelWrappers.keySet().iterator(); it.hasNext();) {
                SimpleStringPattern pattern = (SimpleStringPattern)it.next();
                if (pattern.doesMatch(filename)) {
                    keywordSubstitutionOptions = (KeywordSubstitutionOptions)directoryLevelWrappers.get(pattern);
                    wrapperFound = true;
                    break;
                }
            }
        }

        return keywordSubstitutionOptions == KeywordSubstitutionOptions.BINARY;
    }

    /**
     * Execute a command.
     * @param client the client services object that provides any necessary
     * services to this command, including the ability to actually process
     * all the requests
     */
    public void execute(ClientServices client, EventManager em)
            throws CommandException, AuthenticationException {
        if (files == null || files.length == 0) {
            throw new CommandException("No files have been specified for " + //NOI18N
                                       "adding.", CommandException.getLocalMessage("AddCommand.noFilesSpecified", null)); //NOI18N
        }

        client.ensureConnection();

        clientServices = client;
        setLocalDirectory(client.getLocalPath());
        
        String directory = client.getLocalPath();
        File cvsfolder = new File(directory, "CVS");
        if (!cvsfolder.isDirectory()) {
            //setFailed();
            MessageEvent event = new MessageEvent(this, "cvs [add aborted]: there is no version here; do 'cvs checkout' first", true);
            messageSent(event);
            em.fireCVSEvent(event);
            return ;
        }
/*
        newDirList = new HashMap();
*/
        newDirList.clear();

        super.execute(client, em);

        requests = new LinkedList();

        if (client.isFirstCommand()) {
            requests.add(new RootRequest(client.getRepository()));
        }

        // sets the message argument -m .. one for all files being sent..
        String message = getMessage();
        if (message != null) {
            message = message.trim();
        }
        if (message != null
                && message.length() > 0) {
            addMessageRequest(message);
        }

        if (getKeywordSubst() != null && !getKeywordSubst().equals("")) { //NOI18N
            requests.add(new ArgumentRequest("-k" + getKeywordSubst())); //NOI18N
        }

        try {
            // current dir sent to server BEFORE and AFTER - kinda hack??
            for (int i = 0; i < files.length; i++) {
                addRequests(files[i]);
            }

            // now add the request that indicates the working directory for the
            // command
            requests.add(new DirectoryRequest(".", //NOI18N
                                              client.getRepositoryForDirectory(getLocalDirectory())));

            requests.addAll(argumentRequests);
            argumentRequests.clear(); // MK sanity check.
            requests.add(CommandRequest.ADD);
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
     * This method returns how the command would look like when typed on the
     * command line.
     * Each command is responsible for constructing this information.
     * @returns <command's name> [<parameters>] files/dirs. Example: checkout -p CvsCommand.java
     */
    public String getCVSCommand() {
        StringBuffer toReturn = new StringBuffer("add "); //NOI18N
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
     * Method that is called while the command is being executed.
     * Descendants can override this method to return a Builder instance
     * that will parse the server's output and create data structures.
     */
    public Builder createBuilder(EventManager eventManager) {
        return new AddBuilder(eventManager, this);
    }

    /**
     * Takes the arguments and sets the command.
     * To be mainly used for automatic settings (like parsing the .cvsrc file)
     * @return true if the option (switch) was recognized and set
     */
    public boolean setCVSCommand(char opt, String optArg) {
        if (opt == 'm') {
            setMessage(optArg);
        }
        else if (opt == 'k') {
            KeywordSubstitutionOptions keywordSubst =
                    KeywordSubstitutionOptions.findKeywordSubstOption(optArg);
            setKeywordSubst(keywordSubst);
        }
        else {
            return false;
        }
        return true;
    }

    /**
     * Returns a string indicating the available options.
     */
    public String getOptString() {
        return "m:k:"; //NOI18N
    }

    /**
     * Listens for output of the command.
     * If new directory is added, executes the createCvsFiles() method.
     */
    public void messageSent(MessageEvent e) {
        String str = e.getMessage();
        if (str.endsWith(DIR_ADDED)) {
            str = str.substring(DIRECTORY.length(), str.indexOf(DIR_ADDED)).trim();
            createCvsFiles(str);
        }
        super.messageSent(e);
    }

    /**
     * For new directory that was added to the repository, creates the admin
     * files in CVS subdir.
     */
    private void createCvsFiles(String newDirInRepository) {
        String repository = newDirInRepository;
        String dirName = repository;
        if (dirName.lastIndexOf('/') >= 0) {
            dirName = dirName.substring(dirName.lastIndexOf('/') + 1,
                                        dirName.length());
        }

        if (newDirList.size() == 0) {
            System.err.println("JavaCVS: Bug in AddCommand|createCvsFiles"); // NOI18N
            System.err.println("         newDirInRepository = " + newDirInRepository); // NOI18N
            return;
        }

        Paths paths = null;
        for (Iterator i = newDirList.iterator(); i.hasNext();) {
            paths = (Paths) i.next();
            if (paths.getRepositoryPath().equals(newDirInRepository)) {
                i.remove();
                break;
            }
        }
        
        String local = paths.getPartPath();
        String part = paths.getRepositoryPath();
        repository = paths.getRepositoryPath();
        
        String tempDirName = part;
        if (part.lastIndexOf('/') >= 0) {
            tempDirName = part.substring(part.lastIndexOf('/') + 1,
                                         part.length());
        }

        if (!tempDirName.equalsIgnoreCase(dirName)) {
            System.err.println("JavaCVS: Bug in AddCommand|createCvsFiles"); // NOI18N
            System.err.println("         newDirInRepository = " + newDirInRepository); // NOI18N
            System.err.println("         tempDirName = " + tempDirName); // NOI18N
            System.err.println("         dirName = " + dirName); // NOI18N
            return;
        }

        try {
            if (repository.startsWith(".")) { //NOI18N
                repository = repository.substring(1);
            }
            clientServices.updateAdminData(local, repository, null);
            createCvsTagFile(local, repository);
        }
        catch (IOException ex) {
            System.err.println("TODO: couldn't create/update Cvs admin files"); // NOI18N
        }
/*
        Iterator it = newDirList.keySet().iterator();
        while (it.hasNext())
        {
            String local = (String)it.next();
            String part = (String)newDirList.get(local);
            String tempDirName = part;
            if (part.lastIndexOf('/') >= 0)
            {
                tempDirName = part.substring(part.lastIndexOf('/') + 1,
                                             part.length());
            }

            if (tempDirName.equalsIgnoreCase(dirName))
            {
                try
                {
                    clientServices.updateAdminData(local, repository, null);
                    createCvsTagFile(local, repository);
                    it.remove(); // hack.. in case 2 dirs being added have the same name??
                    break;
                }
                catch (IOException exc)
                {
                    System.out.println("TODO: couldn't create/update Cvs admin files");
                }
            }
        }
*/
    }

    private void createCvsTagFile(String local, String repository) throws IOException {
        File current = new File(getLocalDirectory(), local);
        File parent = current.getParentFile();
        String tag = clientServices.getStickyTagForDirectory(parent);
        if (tag != null) {
            File tagFile = new File(current, "CVS/Tag"); // NOI18N
            tagFile.createNewFile();
            PrintWriter w = new PrintWriter(new BufferedWriter(new FileWriter(tagFile)));
            w.println(tag);
            w.close();
        }
    }

    /**
     * resets all switches in the command. After calling this method,
     * the command should have no switches defined and should behave defaultly.
     */
    public void resetCVSCommand() {
        setMessage(null);
        setKeywordSubst(null);
    }

    /**
     * Returns the arguments of the command in the command-line style.
     * Similar to getCVSCommand() however without the files and command's name
     */
    public String getCVSArguments() {
        StringBuffer toReturn = new StringBuffer();
        if (getMessage() != null) {
            toReturn.append("-m \""); //NOI18N
            toReturn.append(getMessage());
            toReturn.append("\" "); //NOI18N
        }
        if (getKeywordSubst() != null) {
            toReturn.append("-k"); //NOI18N
            toReturn.append(getKeywordSubst().toString());
            toReturn.append(" "); //NOI18N
        }
        return toReturn.toString();
    }

    private static class Paths {
        private final String partPath;
        private final String repositoryPath;

        public Paths(String partPath, String repositoryPath) {
            this.partPath = partPath;
            this.repositoryPath = repositoryPath;
        }

        public String getPartPath() {
            return partPath;
        }

        public String getRepositoryPath() {
            return repositoryPath;
        }
    }
}
