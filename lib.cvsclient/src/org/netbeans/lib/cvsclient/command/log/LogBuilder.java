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
package org.netbeans.lib.cvsclient.command.log;

import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.event.*;
import org.netbeans.lib.cvsclient.util.*;

/**
 * Handles the building of a log information object and the firing of
 * events when complete objects are built.
 * @author Milos Kleint
 */
public class LogBuilder implements Builder {
    private static final String LOGGING_DIR = ": Logging "; //NOI18N
    private static final String RCS_FILE = "RCS file: "; //NOI18N
    private static final String WORK_FILE = "Working file: "; //NOI18N
    private static final String REV_HEAD = "head: "; //NOI18N
    private static final String BRANCH = "branch: "; //NOI18N
    private static final String LOCKS = "locks: "; //NOI18N
    private static final String ACCESS_LIST = "access list: "; //NOI18N
    private static final String SYM_NAME = "symbolic names:"; //NOI18N
    private static final String KEYWORD_SUBST = "keyword substitution: "; //NOI18N
    private static final String TOTAL_REV = "total revisions: "; //NOI18N
    private static final String SEL_REV = ";\tselected revisions: "; //NOI18N
    private static final String DESCRIPTION = "description:"; //NOI18N
    private static final String REVISION = "revision "; //NOI18N
    private static final String DATE = "date: "; //NOI18N
    private static final String BRANCHES = "branches: "; //NOI18N
    private static final String AUTHOR = "author: "; //NOI18N
    private static final String STATE = "state: "; //NOI18N
    private static final String LINES = "lines: "; //NOI18N
    private static final String COMMITID = "commitid: "; //NOI18N
    private static final String SPLITTER = "----------------------------"; //NOI18N
    private static final String FINAL_SPLIT = "============================================================================="; //NOI18N
    private static final String ERROR = ": nothing known about "; //NOI18N
    private static final String NO_FILE = "no file"; //NOI18N
    /**
     * The event manager to use
     */
    protected EventManager eventManager;
    protected BasicCommand logCommand;
    /**
     * The log object that is currently being built
     */
    protected LogInformation logInfo;
    protected LogInformation.Revision revision;
    /**
     * The directory in which the file being processed lives. This is
     * relative to the local directory
     */
    protected String fileDirectory;
    private boolean addingSymNames;
    private boolean addingDescription;
    private boolean addingLogMessage;
    private StringBuffer tempBuffer = null;
    
    private List messageList;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z"); //NOI18N
    
    public LogBuilder(EventManager eventMan, BasicCommand command) {
        logCommand = command;
        eventManager = eventMan;
        addingSymNames = false;
        addingDescription = false;
        addingLogMessage = false;
        logInfo = null;
        revision = null;
        messageList = new ArrayList(500);
    }

    public void outputDone() {
        if (logInfo != null) {
            eventManager.fireCVSEvent(new FileInfoEvent(this, logInfo));
            logInfo = null;
            messageList = null;
        }
    }

    public void parseLine(String line, boolean isErrorMessage) {
        if (line.equals(FINAL_SPLIT)) {
            if (addingDescription) {
                addingDescription = false;
                logInfo.setDescription(tempBuffer.toString());
            }
            if (addingLogMessage) {
                addingLogMessage = false;
                revision.setMessage(CommandUtils.findUniqueString(tempBuffer.toString(), messageList));
            }
            if (revision != null) {
                logInfo.addRevision(revision);
                revision = null;
            }
            // fire the event and exit
            if (logInfo != null) {
                eventManager.fireCVSEvent(new FileInfoEvent(this, logInfo));
                logInfo = null;
                tempBuffer = null;
            }
            return;
        }
        if (addingLogMessage) {
            // first check for the branches tag
            if (line.startsWith(BRANCHES)) {
                processBranches(line.substring(BRANCHES.length()));
            }
            else {
                processLogMessage(line);
                return;
            }
        }
        if (addingSymNames) {
            processSymbolicNames(line);
        }
        if (addingDescription) {
            processDescription(line);
        }
        // revision stuff first -> will be  the most common to parse
        if (line.startsWith(REVISION)) {
            processRevisionStart(line);
        }
        if (line.startsWith(DATE)) {
            processRevisionDate(line);
        }

        if (line.startsWith(KEYWORD_SUBST)) {
            logInfo.setKeywordSubstitution(line.substring(KEYWORD_SUBST.length()).trim().intern());
            addingSymNames = false;
            return;
        }

        if (line.startsWith(DESCRIPTION)) {
            tempBuffer = new StringBuffer(line.substring(DESCRIPTION.length()));
            addingDescription = true;
        }

        if (line.indexOf(LOGGING_DIR) >= 0) {
            fileDirectory = line.substring(line.indexOf(LOGGING_DIR) + LOGGING_DIR.length()).trim();
            return;
        }
        if (line.startsWith(RCS_FILE)) {
            processRcsFile(line.substring(RCS_FILE.length()));
            return;
        }
        if (line.startsWith(WORK_FILE)) {
            processWorkingFile(line.substring(WORK_FILE.length()));
            return;
        }
        if (line.startsWith(REV_HEAD)) {
            logInfo.setHeadRevision(line.substring(REV_HEAD.length()).trim().intern());
            return;
        }
        if (line.startsWith(BRANCH)) {
            logInfo.setBranch(line.substring(BRANCH.length()).trim().intern());
        }
        if (line.startsWith(LOCKS)) {
            logInfo.setLocks(line.substring(LOCKS.length()).trim().intern());
        }
        if (line.startsWith(ACCESS_LIST)) {
            logInfo.setAccessList(line.substring(ACCESS_LIST.length()).trim().intern());
        }
        if (line.startsWith(SYM_NAME)) {
            addingSymNames = true;
        }
        if (line.startsWith(TOTAL_REV)) {
            int ind = line.indexOf(';');
            if (ind < 0) {
                // no selected revisions here..
                logInfo.setTotalRevisions(line.substring(TOTAL_REV.length()).trim().intern());
                logInfo.setSelectedRevisions("0"); //NOI18N
            }
            else {
                String total = line.substring(0, ind);
                String select = line.substring(ind, line.length());
                logInfo.setTotalRevisions(total.substring(TOTAL_REV.length()).trim().intern());
                logInfo.setSelectedRevisions(select.substring(SEL_REV.length()).trim().intern());
            }
        }
    }

    private String findUniqueString(String name, List list) {
        if (name == null) {
            return null;
        }
        int index = list.indexOf(name);
        if (index >= 0) {
            return (String)list.get(index);
        }
        else {
            String newName = name;
            list.add(newName);
            return newName;
        }
    }
    
    private void processRcsFile(String line) {
        if (logInfo != null) {
            //do fire logcreated event;
        }
        logInfo = new LogInformation();
        logInfo.setRepositoryFilename(line.trim());
    }

    private void processWorkingFile(String line) {
        String fileName = line.trim();
        if (fileName.startsWith(NO_FILE)) {
            fileName = fileName.substring(8);
        }

        logInfo.setFile(createFile(line));
    }

    private void processBranches(String line) {
        int ind = line.lastIndexOf(';');
        if (ind > 0) {
            line = line.substring(0, ind);
        }
        revision.setBranches(line.trim());
    }

    private void processLogMessage(String line) {
        if (line.startsWith(SPLITTER)) {
            addingLogMessage = false;
            revision.setMessage(findUniqueString(tempBuffer.toString(), messageList));
            return;
        }
        tempBuffer.append(line + "\n"); //NOI18N
    }

    private void processSymbolicNames(String line) {
        if (!line.startsWith(KEYWORD_SUBST)) {
            line = line.trim();
            int index = line.indexOf(':');
            if (index > 0) {
                String symName = line.substring(0, index).trim();
                String revName = line.substring(index + 1, line.length()).trim();
                logInfo.addSymbolicName(symName.intern(), revName.intern());
            }
        }
    }

    private void processDescription(String line) {
        if (line.startsWith(SPLITTER)) {
            addingDescription = false;
            logInfo.setDescription(tempBuffer.toString());
            return;
        }
        tempBuffer.append(line);
    }

    private void processRevisionStart(String line) {
        if (revision != null) {
            logInfo.addRevision(revision);
        }
        revision = logInfo.createNewRevision(
                    line.substring(REVISION.length()).intern());
    }

    private void processRevisionDate(String line) {
        StringTokenizer tokenizer = new StringTokenizer(line, ";", false); //NOI18N
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().trim();
            if (token.startsWith(DATE)) {
                String dateString = token.substring(DATE.length());
                Date date = null;
                try {
                    // some servers use dashes to separate date components, so replace with slashes
                    // also add a default GMT timezone at the end, if the server already put one in this one will be ignored by the parser
                    dateString = dateString.replace('/', '-') + " +0000"; //NOI18N
                    date = dateFormat.parse(dateString);
                } catch (ParseException e) {
                    BugLog.getInstance().bug("Couldn't parse date " + dateString); //NOI18N
                }
                revision.setDate(date, dateString);
            }
            else if (token.startsWith(AUTHOR)) revision.setAuthor(token.substring(AUTHOR.length()));
            else if (token.startsWith(STATE)) revision.setState(token.substring(STATE.length()));
            else if (token.startsWith(LINES)) revision.setLines(token.substring(LINES.length()));
            else if (token.startsWith(COMMITID)) revision.setCommitID(token.substring(COMMITID.length()));
        }
        addingLogMessage = true;
        tempBuffer = new StringBuffer();
    }

    /**
     * @param fileName relative URL-path to command execution directory
     */
    protected File createFile(String fileName) {
        StringBuffer path = new StringBuffer();
        path.append(logCommand.getLocalDirectory());
        path.append(File.separator);

        path.append(fileName.replace('/', File.separatorChar));  // NOI18N
        return new File(path.toString());
    }

    public void parseEnhancedMessage(String key, Object value) {
    }
}
