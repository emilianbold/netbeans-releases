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
package org.netbeans.lib.cvsclient.command.importcmd;

import java.io.*;
import java.util.*;

import org.netbeans.lib.cvsclient.*;
import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.connection.*;
import org.netbeans.lib.cvsclient.event.*;
import org.netbeans.lib.cvsclient.request.*;
import org.netbeans.lib.cvsclient.response.WrapperSendResponse;
import org.netbeans.lib.cvsclient.util.*;

/**
 * The import command imports local directory structures into the repository.
 *
 * @author  Thomas Singer
 */
public class ImportCommand extends BuildableCommand {
    private Map wrapperMap = new HashMap();
    private String logMessage;
    private String module;
    private String releaseTag;
    private String vendorBranch;
    private String vendorTag;
    private String importDirectory;
    private KeywordSubstitutionOptions keywordSubstitutionOptions;
    private boolean useFileModifTime;
    private List ignoreList = new LinkedList();

    public ImportCommand() {
        resetCVSCommand();
    }

    public void addWrapper(String filenamePattern, KeywordSubstitutionOptions keywordSubstitutionOptions) {
        if (keywordSubstitutionOptions == null) {
            throw new IllegalArgumentException("keywordSubstitutionOptions must not be null");
        }

        wrapperMap.put(new SimpleStringPattern(filenamePattern), keywordSubstitutionOptions);
    }

    public void addWrapper(StringPattern filenamePattern, KeywordSubstitutionOptions keywordSubstitutionOptions) {
        if (keywordSubstitutionOptions == null) {
            throw new IllegalArgumentException("keywordSubstitutionOptions must not be null");
        }

        wrapperMap.put(filenamePattern, keywordSubstitutionOptions);
    }

    /**
     * Compliant method to addWrapper. It replaces the whole list of cvswrappers.
     * The Map's structure should be following:
     *   Key: instance of StringPattern(fileName wildpattern)
     *   Value: instance of KeywordSubstitutionOptions
     */
    public void setWrappers(Map wrapperMap) {
        this.wrapperMap = wrapperMap;
    }

    /**
     * Returns a map with all wrappers.
     * For map descriptions see setWrapper()
     */
    public Map getWrappers() {
        return wrapperMap;
    }

    /**
     * Returns the keyword substitution option.
     */
    public KeywordSubstitutionOptions getKeywordSubstitutionOptions() {
        return keywordSubstitutionOptions;
    }

    /**
     * Sets the keywords substitution option.
     */
    public void setKeywordSubstitutionOptions(KeywordSubstitutionOptions keywordSubstitutionOptions) {
        this.keywordSubstitutionOptions = keywordSubstitutionOptions;
    }

    /**
     * Returns the release tag.
     */
    public String getReleaseTag() {
        return releaseTag;
    }

    /**
     * Sets the necessary release tag.
     */
    public void setReleaseTag(String releaseTag) {
        this.releaseTag = getTrimmedString(releaseTag);
    }

    /**
     * Returns the log message.
     */
    public String getLogMessage() {
        return logMessage;
    }

    /**
     * Sets the log message.
     */
    public void setLogMessage(String logMessage) {
        this.logMessage = getTrimmedString(logMessage);
    }

    /**
     * Returns the module (the in-repository path, where the files should be
     * stored.
     */
    public String getModule() {
        return module;
    }

    /**
     * Sets the module (the in-repository path, where the files should be
     * stored).
     */
    public void setModule(String module) {
        this.module = getTrimmedString(module);
    }

    /**
     * Pints to directoty to import.
     */
    public void setImportDirectory(String directory) {
        importDirectory = directory;
    }

    public String getImportDirectory() {
        return importDirectory;
    }

    /**
     * Returns the vendor branch.
     */
    public String getVendorBranch() {
        return vendorBranch;
    }

    /**
     * Returns the vendor branch.
     * If not set, then 1.1.1 is returned.
     */
    private String getVendorBranchNotNull() {
        if (vendorBranch == null) {
            return "1.1.1"; //NOI18N
        }

        return vendorBranch;
    }

    /**
     * Sets the vendor branch.
     * If null is set, the default branch 1.1.1 is used automatically.
     */
    public void setVendorBranch(String vendorBranch) {
        this.vendorBranch = getTrimmedString(vendorBranch);
    }

    /**
     * Returns the vendor tag.
     */
    public String getVendorTag() {
        return vendorTag;
    }

    /**
     * Sets the necessary vendor tag.
     */
    public void setVendorTag(String vendorTag) {
        this.vendorTag = getTrimmedString(vendorTag);
    }
    
    /**
     * Tells, whether the file modification time is to be used as the time of the import.
     */
    public boolean isUseFileModifTime() {
        return useFileModifTime;
    }
    
    /**
     * Sets whether the file modification time is to be used as the time of the import.
     */
    public void setUseFileModifTime(boolean useFileModifTime) {
        this.useFileModifTime = useFileModifTime;
    }
    
    /**
     * Get a list of files that are ignored by import.
     */
    public List getIgnoreFiles() {
        return Collections.unmodifiableList(ignoreList);
    }
    
    /**
     * Add a file name that is to be ignored by the import.
     */
    public void addIgnoredFile(String ignoredFileName) {
        ignoreList.add(ignoredFileName);
    }

    /**
     * Executes thiz command using the set options.
     */
    public void execute(ClientServices client, EventManager eventManager)
            throws CommandException, AuthenticationException {
        // check necessary fields
        if (getLogMessage() == null) {
            String localizedMsg = CommandException.getLocalMessage("ImportCommand.messageEmpty"); //NOI18N
            throw new CommandException("message may not be null nor empty", //NOI18N
                                       localizedMsg);
        }
        if (getModule() == null) {
            String localizedMsg = CommandException.getLocalMessage("ImportCommand.moduleEmpty"); //NOI18N
            throw new CommandException("module may not be null nor empty", //NOI18N
                                       localizedMsg);
        }
        if (getReleaseTag() == null) {
            String localizedMsg = CommandException.getLocalMessage("ImportCommand.releaseTagEmpty"); //NOI18N
            throw new CommandException("release tag may not be null nor empty", //NOI18N
                                       localizedMsg);
        }
        if (getVendorTag() == null) {
            String localizedMsg = CommandException.getLocalMessage("ImportCommand.vendorTagEmpty"); //NOI18N
            throw new CommandException("vendor tag may not be null nor empty", //NOI18N
                                       localizedMsg);
        }

        client.ensureConnection();

        // get the connection wrappers here
        Map allWrappersMap = new HashMap(client.getWrappersMap());
        allWrappersMap.putAll(getWrappers());
        setWrappers(allWrappersMap);

        // start working
        super.execute(client, eventManager);
        assert getLocalDirectory() != null : "local directory may not be null";

        List requestList = new ArrayList();

        try {
            // add requests
            requestList.add(new ArgumentRequest("-b")); //NOI18N
            requestList.add(new ArgumentRequest(getVendorBranchNotNull()));

            if (getKeywordSubstitutionOptions() != null) {
                requestList.add(new ArgumentRequest("-k")); //NOI18N
                requestList.add(new ArgumentRequest(getKeywordSubstitutionOptions().toString()));
            }

            addMessageRequests(requestList, getLogMessage());

            addWrapperRequests(requestList, this.wrapperMap);
            
            if (isUseFileModifTime()) {
                requestList.add(new ArgumentRequest("-d")); //NOI18N
            }
            
            for (int i = 0; i < ignoreList.size(); i++) {
                requestList.add(new ArgumentRequest("-I")); //NOI18N
                requestList.add(new ArgumentRequest((String) ignoreList.get(i)));
            }

            requestList.add(new ArgumentRequest(getModule()));
            requestList.add(new ArgumentRequest(getVendorTag()));
            requestList.add(new ArgumentRequest(getReleaseTag()));

            addFileRequests(new File(getLocalDirectory()),
                            requestList, client);

            requestList.add(new DirectoryRequest(".", getRepositoryRoot(client))); //NOI18N

            requestList.add(CommandRequest.IMPORT);

            // process the requests
            client.processRequests(requestList);
        }
        catch (CommandException ex) {
            throw ex;
        }
        catch (EOFException ex) {
            String localizedMsg = CommandException.getLocalMessage("CommandException.EndOfFile", null); //NOI18N
            throw new CommandException(ex, localizedMsg);
        }
        catch (Exception ex) {
            throw new CommandException(ex, ex.getLocalizedMessage());
        }
    }

    public String getCVSCommand() {
        StringBuffer toReturn = new StringBuffer("import "); //NOI18N
        toReturn.append(getCVSArguments());
        if (getModule() != null) {
            toReturn.append(" "); //NOI18N
            toReturn.append(getModule());
        }
        else {
            String localizedMsg = CommandException.getLocalMessage("ImportCommand.moduleEmpty.text"); //NOI18N
            toReturn.append(" "); //NOI18N
            toReturn.append(localizedMsg);
        }
        if (getVendorTag() != null) {
            toReturn.append(" "); //NOI18N
            toReturn.append(getVendorTag());
        }
        else {
            String localizedMsg = CommandException.getLocalMessage("ImportCommand.vendorTagEmpty.text"); //NOI18N
            toReturn.append(" "); //NOI18N
            toReturn.append(localizedMsg);
        }
        if (getReleaseTag() != null) {
            toReturn.append(" "); //NOI18N
            toReturn.append(getReleaseTag());
        }
        else {
            String localizedMsg = CommandException.getLocalMessage("ImportCommand.releaseTagEmpty.text"); //NOI18N
            toReturn.append(" "); //NOI18N
            toReturn.append(localizedMsg);
        }
        return toReturn.toString();
    }

    public String getCVSArguments() {
        StringBuffer toReturn = new StringBuffer(""); //NOI18N
        if (getLogMessage() != null) {
            toReturn.append("-m \""); //NOI18N
            toReturn.append(getLogMessage());
            toReturn.append("\" "); //NOI18N
        }
        if (getKeywordSubstitutionOptions() != null) {
            toReturn.append("-k"); //NOI18N
            toReturn.append(getKeywordSubstitutionOptions().toString());
            toReturn.append(" "); //NOI18N
        }
        if (getVendorBranch() != null) {
            toReturn.append("-b "); //NOI18N
            toReturn.append(getVendorBranch());
            toReturn.append(" "); //NOI18N
        }
        if (isUseFileModifTime()) {
            toReturn.append("-d "); // NOI18N
        }
        if (wrapperMap.size() > 0) {
            Iterator it = wrapperMap.keySet().iterator();
            while (it.hasNext()) {
                StringPattern pattern = (StringPattern)it.next();
                KeywordSubstitutionOptions keywordSubstitutionOptions = (KeywordSubstitutionOptions)wrapperMap.get(pattern);
                toReturn.append("-W "); //NOI18N
                toReturn.append(pattern.toString());
                toReturn.append(" -k '"); //NOI18N
                toReturn.append(keywordSubstitutionOptions.toString());
                toReturn.append("' "); //NOI18N
            }
        }
        for (Iterator it = ignoreList.iterator(); it.hasNext(); ) {
            toReturn.append("-I "); //NOI18N
            toReturn.append((String) it.next());
            toReturn.append(" "); //NOI18N
        }
        return toReturn.toString();
    }

    public boolean setCVSCommand(char opt, String optArg) {
        if (opt == 'b') {
            setVendorBranch(optArg);
        }
        else if (opt == 'm') {
            setLogMessage(optArg);
        }
        else if (opt == 'k') {
            setKeywordSubstitutionOptions(KeywordSubstitutionOptions.findKeywordSubstOption(optArg));
        }
        else if (opt == 'W') {
            Map wrappers = WrapperSendResponse.parseWrappers(optArg);
            for (Iterator it = wrappers.keySet().iterator(); it.hasNext(); ) {
                StringPattern pattern = (StringPattern) it.next();
                KeywordSubstitutionOptions keywordOption = (KeywordSubstitutionOptions) wrappers.get(pattern);
                addWrapper(pattern, keywordOption);
            }
        }
        else if (opt == 'd') {
            setUseFileModifTime(true);
        }
        else if (opt == 'I') {
            addIgnoredFile(optArg);
        }
        else {
            return false;
        }
        return true;
    }

    public void resetCVSCommand() {
        setLogMessage(null);
        setModule(null);
        setReleaseTag(null);
        setVendorTag(null);
        setVendorBranch(null);
        setUseFileModifTime(false);
        ignoreList.clear();
        wrapperMap.clear();
    }

    public String getOptString() {
        return "m:W:b:k:dI:"; //NOI18N
    }

    /**
     * Adds requests for the specified logMessage to the specified requestList.
     */
    private void addMessageRequests(List requestList, String logMessage) {
        requestList.add(new ArgumentRequest("-m")); //NOI18N

        StringTokenizer token = new StringTokenizer(logMessage, "\n", false); //NOI18N
        boolean first = true;
        while (token.hasMoreTokens()) {
            if (first) {
                requestList.add(new ArgumentRequest(token.nextToken()));
                first = false;
            }
            else {
                requestList.add(new ArgumentxRequest(token.nextToken()));
            }
        }
    }

    /**
     * Adds requests for specified wrappers to the specified requestList.
     */
    private void addWrapperRequests(List requestList, Map wrapperMap) {
        for (Iterator it = wrapperMap.keySet().iterator(); it.hasNext();) {
            StringPattern pattern = (StringPattern) it.next();
            KeywordSubstitutionOptions keywordSubstitutionOptions = (KeywordSubstitutionOptions)wrapperMap.get(pattern);

            StringBuffer buffer = new StringBuffer();
            buffer.append(pattern.toString());
            buffer.append(" -k '"); //NOI18N
            buffer.append(keywordSubstitutionOptions.toString());
            buffer.append("'"); //NOI18N

            requestList.add(new ArgumentRequest("-W")); //NOI18N
            requestList.add(new ArgumentRequest(buffer.toString()));
        }
    }

    /**
     * Adds recursively all request for files and directories in the specified
     * directory to the specified requestList.
     */
    private void addFileRequests(File directory,
                                 List requestList,
                                 ClientServices clientServices)
            throws IOException {
        String relativePath = getRelativeToLocalPathInUnixStyle(directory);
        String repository = getRepositoryRoot(clientServices);
        if (!relativePath.equals(".")) { //NOI18N
            repository += '/' + relativePath;
        }
        requestList.add(new DirectoryRequest(relativePath, repository));

        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        List subdirectories = null;

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            String filename = file.getName();

            if (clientServices.shouldBeIgnored(directory, filename)) {
                continue;
            }

            if (file.isDirectory()) {
                if (subdirectories == null) {
                    subdirectories = new LinkedList();
                }
                subdirectories.add(file);
            }
            else {
                boolean isBinary = isBinary(filename);
                requestList.add(new ModifiedRequest(file, isBinary));
            }
        }

        if (subdirectories != null) {
            for (Iterator it = subdirectories.iterator(); it.hasNext();) {
                File subdirectory = (File)it.next();
                addFileRequests(subdirectory, requestList, clientServices);
            }
        }
    }

    /**
     * Returns the used root path in the repository.
     * It's built from the repository stored in the clientService and the
     * module.
     */
    private String getRepositoryRoot(ClientServices clientServices) {
        String repository = clientServices.getRepository() + '/' + getModule();
        return repository;
    }

    /**
     * Returns true, if the file for the specified filename should be treated as
     * a binary file.
     *
     * The information comes from the wrapper map and the set keywordsubstitution.
     */
    private boolean isBinary(String filename) {
        KeywordSubstitutionOptions keywordSubstitutionOptions = getKeywordSubstitutionOptions();

        for (Iterator it = wrapperMap.keySet().iterator(); it.hasNext();) {
            StringPattern pattern = (StringPattern)it.next();
            if (pattern.doesMatch(filename)) {
                keywordSubstitutionOptions = (KeywordSubstitutionOptions)wrapperMap.get(pattern);
                break;
            }
        }

        return keywordSubstitutionOptions == KeywordSubstitutionOptions.BINARY;
    }

    /**
     * Creates the ImportBuilder.
     */
    public Builder createBuilder(EventManager eventManager) {
        return new ImportBuilder(eventManager, this);
    }
}
