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
package org.netbeans.lib.cvsclient.command.checkout;

import java.io.*;
import java.util.*;

import org.netbeans.lib.cvsclient.*;
import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.command.update.*;
import org.netbeans.lib.cvsclient.connection.*;
import org.netbeans.lib.cvsclient.event.*;
import org.netbeans.lib.cvsclient.request.*;

/**
 * The checkout command.
 * This handles the sending of the requests and the processing of the responses
 * from the server.
 *
 * @author  Robert Greig
 */
public class CheckoutCommand extends BasicCommand
        implements TemporaryFileCreator {
            
    private static final String UPDATING = ": Updating "; // NOI18N
    /**
     * A store of potentially empty directories. When a directory has a file
     * in it, it is removed from this set. This set allows the prune option
     * to be implemented.
     */
    private final Set emptyDirectories = new HashSet();

    /**
     * The modules to checkout. These names are unexpanded and will be passed
     * to a module-expansion request.
     */
    private final List modules = new LinkedList();

    /**
     * The expanded modules.
     */
    private final List expandedModules = new LinkedList();

    /**
     * Will force the checkout command to display only a list of modules.
     */
    private boolean showModules;

    /**
     * if set, will display just a list of modules with statuses.
     */
    private boolean showModulesWithStatus;

    /**
     * if set, will redirect the output of the command to standard output.
     */
    private boolean pipeToOutput;

    /**
     * Whether to prune directories, i.e. remove any directories that do not
     * contain any files. This is the -P option in command-line CVS.
     */
    private boolean pruneDirectories;

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
     * Don't shorten module paths if -d specified.
     */
    private boolean notShortenPaths;
    
    /**
     * Whether <code>notShortenPaths</code> was explicitly set.
     */
    private boolean isNotShortenSet;

    /**
     * Forces a checkout of a revision that was current at specified date.
     */
    private String checkoutByDate;

    /**
     * Forces a checkout of specified revision. Can be a number/tag/branch
     */
    private String checkoutByRevision;

    /** 
     * performs checkout to specified directory other then the module.
     */
    private String checkoutDirectory;

    /**
     * Use this keyword substitution for the command.
     * does not include the -k switch part.
     */
    private KeywordSubstitutionOptions keywordSubst;
    
    /**
     * Do not run module program (if any).
     */
    private boolean notRunModuleProgram;

    /** Active during execute. */
    private ClientServices client;

    /**
     * Construct a new checkout command.
     * @param recursive whether to do a recursive checkout
     * @param modules an array of modules names to checkout
     */
    public CheckoutCommand(boolean recursive, String[] modules) {
        resetCVSCommand();
        setRecursive(recursive);
        setModules(modules);
    }

    /**
     * Construct a new checkout command.
     * @param recursive whether to do a recursive checkout
     * @param module the module to checkout
     */
    public CheckoutCommand(boolean recursive, String module) {
        resetCVSCommand();
        setRecursive(recursive);
        setModule(module);
    }

    /**
     * Construct a checkout command, with default values for options.
     */
    public CheckoutCommand() {
        resetCVSCommand();
        setRecursive(true);
    }

    /**
     * Set the modules to checkout.
     * @param theModules the names (it's like relative path) of the modules to checkout
     */
    public void setModule(String module) {
        modules.add(module);
    }

    /**
     * clears the list of modules for checkout.
     */

    public void clearModules() {
        this.modules.clear();
    }

    /**
     * Set the modules to checkout.
     * @param theModules the names of the modules to checkout
     */
    public void setModules(String[] modules) {
        clearModules();
        for (int i = 0; i < modules.length; i++) {
            String module = modules[i];
            this.modules.add(module);
        }
    }

    public String[] getModules() {
        String[] mods = new String[modules.size()];
        mods = (String[])modules.toArray(mods);
        return mods;
    }

    /**
     * Handle modules that are already checked out. We check whether a
     * module has been checked out and if so we add it to the list of
     * directories that the superclass must send Modified requests for etc.
     */
    private void processExistingModules(String localPath) {
        if (expandedModules.size() == 0) {
            return;
        }

        List list = new ArrayList(expandedModules.size());
        for (Iterator it = expandedModules.iterator(); it.hasNext();) {
            String moduleName = (String)it.next();
            if (moduleName.equals(".")) { //NOI18N
                list.add(new File(localPath));
                break;
            }
            File moduleDir = null;
            final File moduleFile = new File(localPath, moduleName);
            if (moduleFile.isFile()) {
                moduleDir = moduleFile.getParentFile();
            }
            else {
                moduleDir = moduleFile;
            }
            final File moduleCVSDir = new File(moduleDir, "CVS/Repository"); //NOI18N
            if (moduleCVSDir.exists()) {
                list.add(moduleFile);
            }
        }
        File[] directories = new File[list.size()];
        directories = (File[])list.toArray(directories);
        setFiles(directories);
    }

    /**
     * Execute this command.
     * @param client the client services object that provides any necessary
     * services to this command, including the ability to actually process
     * all the requests
     */
    public void execute(ClientServices client, EventManager em)
            throws CommandException, AuthenticationException {

        client.ensureConnection();
        this.client = client;

        try {
            requests = new LinkedList();
            if (client.isFirstCommand()) {
                requests.add(new RootRequest(client.getRepository()));
            }

            if (showModules || showModulesWithStatus) {
                // we need to initialize the builder first (is done in BuildableCommand.execute()
                // but we can't run it because of teh BasicCommand's execute and
                // it's feature that adds the files request to the request list.
                if (builder == null && !isBuilderSet()) {
                    builder = createBuilder(em);
                }

                // special handling for -c -s switches.
                if (showModules) {
                    requests.add(new ArgumentRequest("-c")); //NOI18N
                }
                if (showModulesWithStatus) {
                    requests.add(new ArgumentRequest("-s")); //NOI18N
                }
                requests.add(CommandRequest.CHECKOUT);
                try {
                    client.processRequests(requests);
                    requests.clear();
                }
                catch (CommandException ex) {
                    throw ex;
                }
                catch (EOFException ex) {
                    throw new CommandException(ex, CommandException.getLocalMessage("CommandException.EndOfFile", null));  //NOI18N
                }
                catch (Exception ex) {
                    throw new CommandException(ex, ex.getLocalizedMessage());
                }
                return;
            }

            for (Iterator it = modules.iterator(); it.hasNext();) {
                String module = (String)it.next();
                requests.add(new ArgumentRequest(module));
            }

            expandedModules.clear();
            requests.add(new DirectoryRequest(".", client.getRepository())); //NOI18N
            requests.add(new RootRequest(client.getRepository()));
            requests.add(new ExpandModulesRequest());
            try {
                client.processRequests(requests);
            }
            catch (CommandException ex) {
                throw ex;
            }
            catch (Exception ex) {
                throw new CommandException(ex, ex.getLocalizedMessage());
            }
            requests.clear();
            postExpansionExecute(client, em);
        } finally {
            this.client = null;
        }
    }

    /**
     * The result from this command is used only when the getFiles() returns null or empty array.
     * in such a case and when this method returns true, it is assumed the localpath should be taken
     * as the 'default' file for the building of requests.
     * in checkout we operate with modules rather then files. This produces problems in the following situation.
     * If you have something already checked out and want to checkout another module that is not checked out yet,
     * then there's nothing to be translated from modules to files. and in such a case the localpathis assumed,
     * which includes non-relevant already checked out directories..
     */
    protected boolean assumeLocalPathWhenUnspecified() {
        return false;
    }

    /**
     * This is called when the server has responded to an expand-modules
     * request.
     */
    public void moduleExpanded(ModuleExpansionEvent e) {
        expandedModules.add(e.getModule());
    }

    /**
     * Execute this command
     * @param client the client services object that provides any necessary
     * services to this command, including the ability to actually process
     * all the requests
     */
    private void postExpansionExecute(ClientServices client, EventManager em)
            throws CommandException, AuthenticationException {
        // we first test whether the modules specified actually exist
        // checked out already. If so, we must work something like an update
        // command and send modified files to the server.
        processExistingModules(client.getLocalPath());
        // the sending of the Modified requests and so on is handled in the
        // superclass.
        super.execute(client, em);

        //
        // moved modules code to the end of the other arguments --GAR
        //
        int index = requests.size();
        final int FIRST_INDEX = 0;
        final int SECOND_INDEX = 1;
        if (!isRecursive()) {
            requests.add(FIRST_INDEX, new ArgumentRequest("-l")); //NOI18N
        }
        if (pipeToOutput) {
            requests.add(FIRST_INDEX, new ArgumentRequest("-p")); //NOI18N
        }
        if (resetStickyOnes) {
            requests.add(FIRST_INDEX, new ArgumentRequest("-A")); //NOI18N
        }
        if (useHeadIfNotFound) {
            requests.add(FIRST_INDEX, new ArgumentRequest("-f")); //NOI18N
        }
        if (isNotShortenPaths()) {
            requests.add(FIRST_INDEX, new ArgumentRequest("-N")); //NOI18N
        }
        if (notRunModuleProgram) {
            requests.add(FIRST_INDEX, new ArgumentRequest("-n")); //NOI18N
        }
        if (checkoutByDate != null && checkoutByDate.length() > 0) {
            requests.add(FIRST_INDEX, new ArgumentRequest("-D")); //NOI18N
            requests.add(SECOND_INDEX, new ArgumentRequest(getCheckoutByDate()));
        }
        if (checkoutByRevision != null && checkoutByRevision.length() > 0) {
            requests.add(FIRST_INDEX, new ArgumentRequest("-r")); //NOI18N
            requests.add(SECOND_INDEX, new ArgumentRequest(getCheckoutByRevision()));
        }
        if (checkoutDirectory != null && (!checkoutDirectory.equals(""))) {
            requests.add(FIRST_INDEX, new ArgumentRequest("-d")); //NOI18N
            requests.add(SECOND_INDEX, new ArgumentRequest(getCheckoutDirectory()));
        }        
        if (getKeywordSubst() != null) {
            requests.add(FIRST_INDEX, new ArgumentRequest("-k" + getKeywordSubst())); //NOI18N
        }
        
        index = requests.size() - index; // The end of our arguments
        // Add a -- before the first file name just in case it looks like an option.
        requests.add(index++, new ArgumentRequest("--")); // NOI18N

        // Note that modules might be null and still be valid because
        // for -c, -s switches no module has to be selected
        // You might also think that we should pass in expandedModules here
        // but according to the spec that would be wrong because of the -d
        // flag.
        for (Iterator it = modules.iterator(); it.hasNext();) {
            String module = (String)it.next();
            requests.add(index++, new ArgumentRequest(module));
        }

        requests.add(new DirectoryRequest(".", client.getRepository())); //NOI18N
        requests.add(CommandRequest.CHECKOUT);
        try {
            client.processRequests(requests);
            if (pruneDirectories) {
                pruneEmptyDirectories();
            }
            requests.clear();

        }
        catch (CommandException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new CommandException(ex, ex.getLocalizedMessage());
        }
    }

    /**
     * Getter for property showModules.
     * @return Value of property showModules.
     */
    public boolean isShowModules() {
        return showModules;
    }

    /**
     * Setter for property showModules.
     * @param showModules New value of property showModules.
     */
    public void setShowModules(boolean showModules) {
        this.showModules = showModules;
    }

    /**
     * Getter for property showModulesWithStatus.
     * @return Value of property showModulesWithStatus.
     */
    public boolean isShowModulesWithStatus() {
        return showModulesWithStatus;
    }

    /**
     * Setter for property showModulesWithStatus.
     * @param showModulesWithStatus New value of property showModulesWithStatus.
     */
    public void setShowModulesWithStatus(boolean showModulesWithStatus) {
        this.showModulesWithStatus = showModulesWithStatus;
    }

    /**
     * Set whether to prune directories.
     * This is the -P option in the command-line CVS.
     */
    public void setPruneDirectories(boolean pruneDirectories) {
        this.pruneDirectories = pruneDirectories;
    }

    /**
     * Get whether to prune directories.
     * @return true if directories should be removed if they contain no files,
     * false otherwise.
     */
    public boolean getPruneDirectories() {
        return pruneDirectories;
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
     * Getter for property notShortenPaths.
     * @return Value of property notShortenPaths.
     */
    public boolean isNotShortenPaths() {
        // Use the same logic as cvs from cvshome.org.
        return notShortenPaths || (!isNotShortenSet && checkoutDirectory == null);
    }
    
    /**
     * Setter for property notShortenPaths.
     * @param notShortenPaths New value of property notShortenPaths.
     */
    public void setNotShortenPaths(boolean notShortenPaths) {
        this.notShortenPaths = notShortenPaths;
        isNotShortenSet = true;
    }
    
    /**
     * Getter for property notRunModuleProgram.
     * @return Value of property notRunModuleProgram.
     */
    public boolean isNotRunModuleProgram() {
        return notRunModuleProgram;
    }
    
    /**
     * Setter for property notRunModuleProgram.
     * @param notRunModuleProgram New value of property notRunModuleProgram.
     */
    public void setNotRunModuleProgram(boolean notRunModuleProgram) {
        this.notRunModuleProgram = notRunModuleProgram;
    }
    
    /**
     * Getter for property checkoutByDate.
     * @return Value of property checkoutByDate.
     */
    public String getCheckoutByDate() {
        return checkoutByDate;
    }

    /**
     * Setter for property checkoutByDate.
     * @param checkoutByDate New value of property checkoutByDate.
     */
    public void setCheckoutByDate(String checkoutByDate) {
        this.checkoutByDate = checkoutByDate;
    }

    /**
     * Getter for property checkoutByRevision.
     * @return Value of property checkoutByRevision.
     */
    public String getCheckoutByRevision() {
        return checkoutByRevision;
    }

    /**
     * Setter for property checkoutByRevision.
     * @param checkoutByRevision New value of property checkoutByRevision.
     */
    public void setCheckoutByRevision(String checkoutByRevision) {
        this.checkoutByRevision = checkoutByRevision;
    }

    /** Getter for property checkoutDirectory.
     * @return Value of property checkoutDirectory.
     */
    public String getCheckoutDirectory() {
        return this.checkoutDirectory;
    }

    /** Setter for property checkoutDirectory.
     * @param checkoutDirectory New value of property checkoutDirectory.
     */
    public void setCheckoutDirectory(String checkoutDirectory) {
        this.checkoutDirectory = checkoutDirectory;
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

    public Builder createBuilder(EventManager eventMan) {
        if (isShowModules() || isShowModulesWithStatus()) {
            return new ModuleListBuilder(eventMan, this);
        }
        if (isPipeToOutput()) {
            return new PipedFilesBuilder(eventMan, this, this);
        }
        return new UpdateBuilder(eventMan, getLocalDirectory());
    }

    public File createTempFile(String filename) throws IOException {
        File temp = File.createTempFile("cvs", ".dff", getGlobalOptions().getTempDir());  //NOI18N
        temp.deleteOnExit();
        return temp;
    }

    /**
     * This method returns how the command would looklike when typed on the command line.
     * Each command is responsible for constructing this information.
     * @returns <command's name> [<parameters>] files/dirs. Example: checkout -p CvsCommand.java
     */
    public String getCVSCommand() {
        StringBuffer toReturn = new StringBuffer("checkout "); //NOI18N
        toReturn.append(getCVSArguments());
        if (!isShowModules() && !isShowModulesWithStatus()) {
            for (Iterator it = modules.iterator(); it.hasNext();) {
                String module = (String)it.next();
                toReturn.append(module);
                toReturn.append(' ');
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
        if (opt == 'c') {
            setShowModules(true);
        }
        else if (opt == 's') {
            setShowModulesWithStatus(true);
        }
        else if (opt == 'p') {
            setPipeToOutput(true);
        }
        else if (opt == 'R') {
            setRecursive(true);
        }
        else if (opt == 'l') {
            setRecursive(false);
        }
        else if (opt == 'A') {
            setResetStickyOnes(true);
        }
        else if (opt == 'f') {
            setUseHeadIfNotFound(true);
        }
        else if (opt == 'P') {
            setPruneDirectories(true);
        }
        else if (opt == 'D') {
            setCheckoutByDate(optArg.trim());
        }
        else if (opt == 'r') {
            setCheckoutByRevision(optArg.trim());
        }
        else if (opt == 'd') {
            setCheckoutDirectory(optArg);
        }
        else if (opt == 'N') {
            setNotShortenPaths(true);
        }
        else if (opt == 'n') {
            setNotRunModuleProgram(true);
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
     * String returned by this method defines which options are available for this particular command
     */
    public String getOptString() {
        return "cnpslNPRAD:r:fk:d:"; //NOI18N
    }

    /**
     * Resets all switches in the command.
     * After calling this method, the command should have no switches defined
     * and should behave defaultly.
     */
    public void resetCVSCommand() {
        setShowModules(false);
        setShowModulesWithStatus(false);
        setPipeToOutput(false);
        setRecursive(true);
        setResetStickyOnes(false);
        setUseHeadIfNotFound(false);
        setCheckoutByDate(null);
        setCheckoutByRevision(null);
        setKeywordSubst(null);
        setPruneDirectories(false);
        setNotShortenPaths(false);
        isNotShortenSet = false;
        setNotRunModuleProgram(false);
        setCheckoutDirectory(null);        
    }

    /**
     * Returns the arguments of the command in the command-line style.
     * Similar to getCVSCommand() however without the files and command's name
     */
    public String getCVSArguments() {
        StringBuffer toReturn = new StringBuffer(""); //NOI18N
        if (isShowModules()) {
            toReturn.append("-c "); //NOI18N
        }
        if (isShowModulesWithStatus()) {
            toReturn.append("-s "); //NOI18N
        }
        if (isPipeToOutput()) {
            toReturn.append("-p "); //NOI18N
        }
        if (!isRecursive()) {
            toReturn.append("-l "); //NOI18N
        }
        if (isResetStickyOnes()) {
            toReturn.append("-A "); //NOI18N
        }
        if (isUseHeadIfNotFound()) {
            toReturn.append("-f "); //NOI18N
        }
        if (getPruneDirectories()) {
            toReturn.append("-P "); //NOI18N
        }
        if (isNotShortenPaths()) {
            toReturn.append("-N "); // NOI18N
        }
        if (isNotRunModuleProgram()) {
            toReturn.append("-n "); // NOI18N
        }
        if (getKeywordSubst() != null) {
            toReturn.append("-k"); //NOI18N
            toReturn.append(getKeywordSubst());
            toReturn.append(' ');
        }
        if (getCheckoutByRevision() != null && getCheckoutByRevision().length() > 0) {
            toReturn.append("-r "); //NOI18N
            toReturn.append(getCheckoutByRevision());
            toReturn.append(' ');
        }
        if (getCheckoutByDate() != null && getCheckoutByDate().length() > 0) {
            toReturn.append("-D "); //NOI18N
            toReturn.append(getCheckoutByDate());
            toReturn.append(' ');
        }
        if (getCheckoutDirectory() != null) {
            toReturn.append("-d "); //NOI18N
            toReturn.append(getCheckoutDirectory());
            toReturn.append(" "); //NOI18N
        }
         return toReturn.toString();    
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
        if (pruneDirectories &&
                e.getMessage().indexOf(UPDATING) > 0) {
            String relPath = e.getMessage().substring(e.getMessage().indexOf(UPDATING) + UPDATING.length());
            File file = new File(getLocalDirectory(), relPath);
            // do not consider the topmost directory for pruning
            if (relPath.indexOf('/') != -1) {
                emptyDirectories.add(file);
            }
        }
    }

    /**
     * Prunes a directory, recursively pruning its subdirectories
     * @param directory the directory to prune
     */
    private boolean pruneEmptyDirectory(File directory) throws IOException {
        boolean empty = true;

        final File[] contents = directory.listFiles();

        // should never be null, but just in case...
        if (contents != null) {
            for (int i = 0; i < contents.length; i++) {
                if (contents[i].isFile()) {
                    empty = false;
                }
                else {
                    if (!contents[i].getName().equals("CVS")) { //NOI18N
                        empty = pruneEmptyDirectory(contents[i]);
                    }
                }

                if (!empty) {
                    break;
                }
            }

            if (empty) {
                // check this is a CVS directory and not some directory the user
                // has stupidly called CVS...
                final File entriesFile = new File(directory, "CVS/Entries"); //NOI18N
                if (entriesFile.exists()) {
                    final File adminDir = new File(directory, "CVS"); //NOI18N
                    final File[] adminFiles = adminDir.listFiles();
                    for (int i = 0; i < adminFiles.length; i++) {
                        adminFiles[i].delete();
                    }
                    adminDir.delete();
                    directory.delete();
                    client.removeEntry(directory);
                }
            }
        }

        return empty;
    }

    /**
     * Remove any directories that don't contain any files
     */
    private void pruneEmptyDirectories() throws IOException {
        final Iterator it = emptyDirectories.iterator();
        while (it.hasNext()) {
            final File dir = (File)it.next();
            // we might have deleted it already (due to recursive delete)
            // so we need to check existence
            if (dir.exists()) {
                pruneEmptyDirectory(dir);
            }
        }
        emptyDirectories.clear();
    }
    
}
