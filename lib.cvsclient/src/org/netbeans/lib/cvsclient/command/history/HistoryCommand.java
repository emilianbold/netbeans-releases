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
package org.netbeans.lib.cvsclient.command.history;

import java.util.*;

import org.netbeans.lib.cvsclient.*;
import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.connection.*;
import org.netbeans.lib.cvsclient.event.*;
import org.netbeans.lib.cvsclient.request.*;

/**
 * The history command provides information history of activities in repository.
 * @author  Milos Kleint
 */
public class HistoryCommand extends Command {
    /**
     * The requests that are sent and processed.
     */
    private final List requests = new LinkedList();

    /**
     * The event manager to use
     */
    private EventManager eventManager;

    /** Holds value of property forAllUsers. */
    private boolean forAllUsers;

    /** Holds value of property goBackToRecord. */
    private String showBackToRecordContaining;

    /** Holds value of property reportCommits. */
    private boolean reportCommits;

    /** Holds value of property sinceDate. */
    private String sinceDate;

    /** Holds value of property reportEverything. */
    private boolean reportEverything;

    /** Holds value of property lastEventOfProject. */
    private boolean lastEventOfProject;

    /** Holds value of property reportCheckout. */
    private boolean reportCheckouts;

    /** Holds value of property sinceRevision. */
    private String sinceRevision;

    /** Holds value of property reportTags. */
    private boolean reportTags;

    /** Holds value of property sinceTag. */
    private String sinceTag;

    /** Holds value of property forWorkingDirectory. */
    private boolean forWorkingDirectory;

    /** Holds value of property reportEventType. */
    private String reportEventType;

    /** Holds value of property timeZone. */
    private String timeZone;

    /** Holds value of property lastEventForFile. */
    private String[] lastEventForFile;

    /** Holds value of property reportOnModule. */
    private String[] reportOnModule;

    /** Holds value of property reportLastEventForModule. */
    private String[] reportLastEventForModule;

    /** Holds value of property forUsers. */
    private String[] forUsers;

    /**
     * Construct a new history command
     */
    public HistoryCommand() {
    }

    /**
     * Create a builder for this command.
     * @param eventMan the event manager used to receive events.
     */
    public Builder createBuilder(EventManager eventMan) {
        return null;
    }

    /**
     * Execute a command
     * @param client the client services object that provides any necessary
     * services to this command, including the ability to actually process
     * all the requests.
     */
    public void execute(ClientServices client, EventManager em)
            throws CommandException, AuthenticationException {

        client.ensureConnection();

        eventManager = em;
        requests.clear();

        super.execute(client, em);

        try {
            if (client.isFirstCommand()) {
                requests.add(new RootRequest(client.getRepository()));
                requests.add(new UseUnchangedRequest());
            }

            addBooleanArgument(requests, isForAllUsers(), "-a"); //NOI18N
            addBooleanArgument(requests, isForWorkingDirectory(), "-w"); //NOI18N
            addBooleanArgument(requests, isLastEventOfProject(), "-l"); //NOI18N
            addBooleanArgument(requests, isReportCheckouts(), "-o"); //NOI18N
            addBooleanArgument(requests, isReportCommits(), "-c"); //NOI18N
            addBooleanArgument(requests, isReportEverything(), "-e"); //NOI18N
            addBooleanArgument(requests, isReportTags(), "-T"); //NOI18N
            addStringArgument(requests, getReportEventType(), "-x"); //NOI18N
            addStringArgument(requests, getShowBackToRecordContaining(), "-b"); //NOI18N
            addStringArgument(requests, getSinceDate(), "-D"); //NOI18N
            addStringArgument(requests, getSinceRevision(), "-r"); //NOI18N
            addStringArgument(requests, getSinceTag(), "-t"); //NOI18N
            addStringArrayArgument(requests, getForUsers(), "-u"); //NOI18N
            addStringArrayArgument(requests, getReportLastEventForModule(), "-n"); //NOI18N
            addStringArrayArgument(requests, getReportOnModule(), "-m"); //NOI18N
            addStringArrayArgument(requests, getLastEventForFile(), "-f"); //NOI18N
            if (!isReportCheckouts() && !isReportCommits() && !isReportTags() &&
                    !isReportEverything() && getReportEventType() == null && getReportOnModule() == null) {
                // this is the default switch if nothing else is specified.
                addBooleanArgument(requests, true, "-c"); //NOI18N
            }
            if (getTimeZone() != null) {
                addStringArgument(requests, getTimeZone(), "-z"); //NOI18N
            }
            else {
                addStringArgument(requests, "+0000", "-z"); //NOI18N
            }
            requests.add(CommandRequest.HISTORY);
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

    private void addStringArgument(List reqList, String property, String cvsSwitch) {
        if (property != null) {
            reqList.add(new ArgumentRequest(cvsSwitch));
            reqList.add(new ArgumentRequest(property));
        }
    }

    private void addStringArrayArgument(List reqList, String[] property, String cvsSwitch) {
        if (property != null) {
            for (int i = 0; i < property.length; i++) {
                reqList.add(new ArgumentRequest(cvsSwitch));
                reqList.add(new ArgumentRequest(property[i]));
            }
        }
    }

    private void addBooleanArgument(List reqList, boolean property, String cvsSwitch) {
        if (property == true) {
            reqList.add(new ArgumentRequest(cvsSwitch));
        }
    }

    /** called when server responses with "ok" or "error", (when the command finishes)
     */
    public void commandTerminated(TerminationEvent e) {
    }

    /** This method returns how the command would looklike when typed on the command line.
     * Each command is responsible for constructing this information.
     * @returns <command's name> [<parameters>] files/dirs. Example: checkout -p CvsCommand.java
     *
     */
    public String getCVSCommand() {
        StringBuffer toReturn = new StringBuffer("history "); //NOI18N
        toReturn.append(getCVSArguments());
        return toReturn.toString();
    }

    /** takes the arguments and sets the command. To be mainly
     * used for automatic settings (like parsing the .cvsrc file)
     * @return true if the option (switch) was recognized and set
     */
    public boolean setCVSCommand(char opt, String optArg) {
        if (opt == 'a') {
            setForAllUsers(true);
        }
        else if (opt == 'b') {
            setShowBackToRecordContaining(optArg);
        }
        else if (opt == 'c') {
            setReportCommits(true);
        }
        else if (opt == 'D') {
            setSinceDate(optArg);
        }
        else if (opt == 'e') {
            setReportEverything(true);
        }
        else if (opt == 'l') {
            setLastEventOfProject(true);
        }
        else if (opt == 'o') {
            setReportCheckouts(true);
        }
        else if (opt == 'r') {
            setSinceRevision(optArg);
        }
        else if (opt == 'T') {
            setReportTags(true);
        }
        else if (opt == 't') {
            setSinceTag(optArg);
        }
        else if (opt == 'w') {
            setForWorkingDirectory(true);
        }
        else if (opt == 'x') {
            setReportEventType(optArg);
        }
        else if (opt == 'z') {
            setTimeZone(optArg);
        }
        else if (opt == 'f') {
            addLastEventForFile(optArg);
        }
        else if (opt == 'm') {
            addReportOnModule(optArg);
        }
        else if (opt == 'n') {
            addReportLastEventForModule(optArg);
        }
        else if (opt == 'u') {
            addForUsers(optArg);
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
        return "ab:cD:ef:lm:n:or:Tt:u:wx:z:"; //NOI18N
    }

    /**
     * resets all switches in the command. After calling this method,
     * the command should have no switches defined and should behave defaultly.
     */
    public void resetCVSCommand() {
        setForAllUsers(false);
        setForUsers(null);
        setForWorkingDirectory(false);
        setLastEventForFile(null);
        setLastEventOfProject(false);
        setReportCheckouts(false);
        setReportCommits(false);
        setReportEventType(null);
        setReportEverything(false);
        setReportLastEventForModule(null);
        setReportOnModule(null);
        setReportTags(false);
        setShowBackToRecordContaining(null);
        setSinceDate(null);
        setSinceRevision(null);
        setSinceTag(null);
        setTimeZone(null);
    }

    /**
     * Returns the arguments of the command in the command-line style.
     * Similar to getCVSCommand() however without the files and command's name
     */
    public String getCVSArguments() {
        StringBuffer toReturn = new StringBuffer(""); //NOI18N
        if (isForAllUsers()) {
            toReturn.append("-a "); //NOI18N
        }
        if (isForWorkingDirectory()) {
            toReturn.append("-w "); //NOI18N
        }
        if (isLastEventOfProject()) {
            toReturn.append("-l "); //NOI18N
        }
        if (isReportCheckouts()) {
            toReturn.append("-o "); //NOI18N
        }
        if (isReportCommits()) {
            toReturn.append("-c "); //NOI18N
        }
        if (isReportEverything()) {
            toReturn.append("-e "); //NOI18N
        }
        if (isReportTags()) {
            toReturn.append("-T "); //NOI18N
        }
        if (getForUsers() != null) {
            appendArrayToSwitches(toReturn, getForUsers(), "-u "); //NOI18N
        }
        if (getLastEventForFile() != null) {
            appendArrayToSwitches(toReturn, getLastEventForFile(), "-f "); //NOI18N
        }
        if (getReportEventType() != null) {
            toReturn.append("-x "); //NOI18N
            toReturn.append(getReportEventType());
            toReturn.append(" "); //NOI18N
        }
        if (getReportLastEventForModule() != null) {
            appendArrayToSwitches(toReturn, getReportLastEventForModule(), "-n "); //NOI18N
        }
        if (getReportOnModule() != null) {
            appendArrayToSwitches(toReturn, getReportOnModule(), "-m "); //NOI18N
        }
        if (getShowBackToRecordContaining() != null) {
            toReturn.append("-b "); //NOI18N
            toReturn.append(getShowBackToRecordContaining());
            toReturn.append(" "); //NOI18N
        }
        if (getSinceDate() != null) {
            toReturn.append("-D "); //NOI18N
            toReturn.append(getSinceDate());
            toReturn.append(" "); //NOI18N
        }
        if (getSinceRevision() != null) {
            toReturn.append("-r "); //NOI18N
            toReturn.append(getSinceRevision());
            toReturn.append(" "); //NOI18N
        }
        if (getSinceTag() != null) {
            toReturn.append("-t "); //NOI18N
            toReturn.append(getSinceTag());
            toReturn.append(" "); //NOI18N
        }
        if (getTimeZone() != null) {
            toReturn.append("-z "); //NOI18N
            toReturn.append(getTimeZone());
            toReturn.append(" "); //NOI18N
        }
        return toReturn.toString();
    }

    private void appendArrayToSwitches(StringBuffer buff, String[] arr, String cvsSwitch) {
        if (arr == null) {
            return;
        }

        for (int i = 0; i < arr.length; i++) {
            buff.append(cvsSwitch);
            buff.append(arr[i]);
            buff.append(" "); //NOI18N
        }
    }

    /** Getter for property forAllUsers. (cvs switch -a)
     * @return Value of property forAllUsers.
     */
    public boolean isForAllUsers() {
        return this.forAllUsers;
    }

    /** Setter for property forAllUsers. (cvs switch -a)
     * @param forAllUsers New value of property forAllUsers.
     */
    public void setForAllUsers(boolean forAllUsers) {
        this.forAllUsers = forAllUsers;
    }

    /** Getter for property goBackToRecord. (cvs switch -b)
     * @return Value of property goBackToRecord.
     */
    public String getShowBackToRecordContaining() {
        return this.showBackToRecordContaining;
    }

    /** Setter for property goBackToRecord. (cvs switch -b)
     * @param goBackToRecord New value of property goBackToRecord.
     */
    public void setShowBackToRecordContaining(String goBackToRecord) {
        this.showBackToRecordContaining = goBackToRecord;
    }

    /** Getter for property reportCommits. (cvs switch -c)
     * @return Value of property reportCommits.
     */
    public boolean isReportCommits() {
        return this.reportCommits;
    }

    /** Setter for property reportCommits. (cvs switch -b)
     * @param reportCommits New value of property reportCommits.
     */
    public void setReportCommits(boolean reportCommits) {
        this.reportCommits = reportCommits;
    }

    /** Getter for property sinceDate. (cvs switch -D)
     * @return Value of property sinceDate.
     */
    public String getSinceDate() {
        return this.sinceDate;
    }

    /** Setter for property sinceDate. (cvs switch -D)
     * @param sinceDate New value of property sinceDate.
     */
    public void setSinceDate(String sinceDate) {
        this.sinceDate = sinceDate;
    }

    /** Getter for property reportEverything. (cvs switch -e)
     * @return Value of property reportEverything.
     */
    public boolean isReportEverything() {
        return this.reportEverything;
    }

    /** Setter for property reportEverything. (cvs switch -e)
     * @param reportEverything New value of property reportEverything.
     */
    public void setReportEverything(boolean reportEverything) {
        this.reportEverything = reportEverything;
    }

    /** Getter for property lastEventOfProject. (cvs switch -l)
     * @return Value of property lastEventOfProject.
     */
    public boolean isLastEventOfProject() {
        return this.lastEventOfProject;
    }

    /** Setter for property lastEventOfProject. (cvs switch -l)
     * @param lastEventOfProject New value of property lastEventOfProject.
     */
    public void setLastEventOfProject(boolean lastEventOfProject) {
        this.lastEventOfProject = lastEventOfProject;
    }

    /** Getter for property reportCheckout. (cvs switch -o)
     * @return Value of property reportCheckout.
     */
    public boolean isReportCheckouts() {
        return this.reportCheckouts;
    }

    /** Setter for property reportCheckout. (cvs switch -o)
     * @param reportCheckout New value of property reportCheckout.
     */
    public void setReportCheckouts(boolean reportCheckout) {
        this.reportCheckouts = reportCheckout;
    }

    /** Getter for property sinceRevision. (cvs switch -r)
     * @return Value of property sinceRevision.
     */
    public String getSinceRevision() {
        return this.sinceRevision;
    }

    /** Setter for property sinceRevision. (cvs switch -r)
     * @param sinceRevision New value of property sinceRevision.
     */
    public void setSinceRevision(String sinceRevision) {
        this.sinceRevision = sinceRevision;
    }

    /** Getter for property reportTags. (cvs switch -T)
     * @return Value of property reportTags.
     */
    public boolean isReportTags() {
        return this.reportTags;
    }

    /** Setter for property reportTags. (cvs switch -T)
     * @param reportTags New value of property reportTags.
     */
    public void setReportTags(boolean reportTags) {
        this.reportTags = reportTags;
    }

    /** Getter for property sinceTag. (cvs switch -t)
     * @return Value of property sinceTag.
     */
    public String getSinceTag() {
        return this.sinceTag;
    }

    /** Setter for property sinceTag. (cvs switch -t)
     * @param sinceTag New value of property sinceTag.
     */
    public void setSinceTag(String sinceTag) {
        this.sinceTag = sinceTag;
    }

    /** Getter for property forWorkingDirectory. (cvs switch -w)
     * @return Value of property forWorkingDirectory.
     */
    public boolean isForWorkingDirectory() {
        return this.forWorkingDirectory;
    }

    /** Setter for property forWorkingDirectory. (cvs switch -w)
     * @param forWorkingDirectory New value of property forWorkingDirectory.
     */
    public void setForWorkingDirectory(boolean forWorkingDirectory) {
        this.forWorkingDirectory = forWorkingDirectory;
    }

    /** Getter for property reportEventType. (cvs switch -x)
     * @return Value of property reportEventType.
     */
    public String getReportEventType() {
        return this.reportEventType;
    }

    /** Setter for property reportEventType. (cvs switch -x)
     * @param reportEventType New value of property reportEventType.
     */
    public void setReportEventType(String reportEventType) {
        this.reportEventType = reportEventType;
    }

    /** Getter for property timeZone. (cvs switch -z)
     * @return Value of property timeZone.
     */
    public String getTimeZone() {
        return this.timeZone;
    }

    /** Setter for property timeZone. (cvs switch -z)
     * @param timeZone New value of property timeZone.
     */
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /** Getter for property lastEventForFile. (cvs switch -f)
     * @return Value of property lastEventForFile.
     */
    public String[] getLastEventForFile() {
        return this.lastEventForFile;
    }

    /** Setter for property lastEventForFile. (cvs switch -f)
     * @param lastEventForFile New value of property lastEventForFile.
     */
    public void setLastEventForFile(String[] lastEventForFile) {
        this.lastEventForFile = lastEventForFile;
    }

    public void addLastEventForFile(String newFile) {
        this.lastEventForFile = addNewValue(this.lastEventForFile, newFile);
    }

    /** Getter for property reportOnModule. (cvs switch -m)
     * @return Value of property reportOnModule.
     */
    public String[] getReportOnModule() {
        return this.reportOnModule;
    }

    /** Setter for property reportOnModule. (cvs switch -m)
     * @param reportOnModule New value of property reportOnModule.
     */
    public void setReportOnModule(String[] reportOnModule) {
        this.reportOnModule = reportOnModule;
    }

    public void addReportOnModule(String newReportOnModule) {
        this.reportOnModule = addNewValue(this.reportOnModule, newReportOnModule);
    }

    /** Getter for property reportLastEventForModule. (cvs switch -n)
     * @return Value of property reportLastEventForModule.
     */
    public String[] getReportLastEventForModule() {
        return this.reportLastEventForModule;
    }

    /** Setter for property reportLastEventForModule. (cvs switch -n)
     * @param reportLastEventForModule New value of property reportLastEventForModule.
     */
    public void setReportLastEventForModule(String[] reportLastEventForModule) {
        this.reportLastEventForModule = reportLastEventForModule;
    }

    public void addReportLastEventForModule(String newModule) {
        this.reportLastEventForModule = addNewValue(this.reportLastEventForModule, newModule);
    }

    /** Getter for property forUsers. (cvs switch -u)
     * @return Value of property forUsers.
     */
    public String[] getForUsers() {
        return this.forUsers;
    }

    /** Setter for property forUsers. (cvs switch -u)
     * @param forUsers New value of property forUsers.
     */
    public void setForUsers(String[] forUsers) {
        this.forUsers = forUsers;
    }

    public void addForUsers(String forUser) {
        this.forUsers = addNewValue(this.forUsers, forUser);
    }

    private String[] addNewValue(String[] arr, String newVal) {
        if (arr == null) {
            arr = new String[]{newVal};
            return arr;
        }
        String[] newValue = new String[arr.length + 1];
        for (int i = 0; i < arr.length; i++) {
            newValue[i] = arr[i];
        }
        newValue[newValue.length] = newVal;
        return newValue;
    }
}
