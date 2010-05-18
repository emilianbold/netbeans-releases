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
package org.netbeans.lib.cvsclient.command.status;

import java.io.*;

import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.event.*;
import org.netbeans.lib.cvsclient.file.*;

/**
 * Handles the building of a status information object and the firing of
 * events when complete objects are built.
 *
 * @author  Milos Kleint
 * @author  Thomas Singer
 */
public class StatusBuilder implements Builder {
    private static final String UNKNOWN = ": nothing known about"; //NOI18N
    private static final String EXAM_DIR = ": Examining"; //NOI18N
    private static final String NOT_IN_REPOSITORY = "No revision control file"; //NOI18N

    private static final String FILE = "File: "; //NOI18N
    private static final String STATUS = "Status:"; //NOI18N
    private static final String NO_FILE_FILENAME = "no file"; //NOI18N
    private static final String WORK_REV = "   Working revision:"; //NOI18N
    private static final String REP_REV = "   Repository revision:"; //NOI18N
    private static final String TAG = "   Sticky Tag:"; //NOI18N
    private static final String DATE = "   Sticky Date:"; //NOI18N
    private static final String OPTIONS = "   Sticky Options:"; //NOI18N
    private static final String EXISTING_TAGS = "   Existing Tags:"; //NOI18N
    private static final String EMPTY_BEFORE_TAGS = "   "; //NOI18N
    private static final String NO_TAGS = "   No Tags Exist"; //NOI18N
    private static final String UNKNOWN_FILE = "? "; //NOI18N

    /**
     * The status object that is currently being built.
     */
    private StatusInformation statusInformation;

    /**
     * The event manager to use.
     */
    private EventManager eventManager;

    private final StatusCommand statusCommand;
    private String relativeDirectory;
    private final String localPath;

    private boolean beginning;

    private boolean readingTags;

    private final File[] fileArray;

    /**
     * Creates a StatusBuilder.
     */
    public StatusBuilder(EventManager eventManager,
                         StatusCommand statusCommand) {
        this.eventManager = eventManager;
        this.statusCommand = statusCommand;

        File[] fileArray = statusCommand.getFiles();
        if (fileArray != null) {
            this.fileArray = new File[fileArray.length];
            System.arraycopy(fileArray, 0, this.fileArray, 0, fileArray.length);
        }
        else {
            this.fileArray = null;
        }

        this.localPath = statusCommand.getLocalDirectory();

        this.beginning = true;
    }

    public void outputDone() {
        if (statusInformation != null) {
            eventManager.fireCVSEvent(new FileInfoEvent(this, statusInformation));
            statusInformation = null;
            readingTags = false;
        }
    }

    public void parseLine(String line, boolean isErrorMessage) {
        if (readingTags) {
            if (line.startsWith(NO_TAGS)) {
                outputDone();
                return;
            }

            int bracket = line.indexOf("\t(");
            if (bracket > 0) {
                // it's another tag..
                String tag = line.substring(0, bracket).trim();
                String rev = line.substring(bracket + 2, line.length() - 1);

                if (statusInformation == null) {
                    statusInformation = new StatusInformation();
                }
                statusInformation.addExistingTag(tag, rev);
            }
            else {
                outputDone();
                return;
            }
        }

        if (line.startsWith(UNKNOWN_FILE) && beginning) {
            File file = new File(localPath, line.substring(UNKNOWN_FILE.length()));
            statusInformation = new StatusInformation();
            statusInformation.setFile(file);
            statusInformation.setStatusString(FileStatus.UNKNOWN.toString());
            outputDone();
        }

        if (line.startsWith(UNKNOWN)) {
            outputDone();
            beginning = false;
        }
        else if (line.indexOf(EXAM_DIR) >= 0) {
            relativeDirectory = line.substring(line.indexOf(EXAM_DIR) + EXAM_DIR.length()).trim();
            beginning = false;
        }
        else if (line.startsWith(FILE)) {
            outputDone();
            statusInformation = new StatusInformation();
            processFileAndStatusLine(line.substring(FILE.length()));
            beginning = false;
        }
        else if (line.startsWith(WORK_REV)) {
            processWorkRev(line.substring(WORK_REV.length()));
        }
        else if (line.startsWith(REP_REV)) {
            processRepRev(line.substring(REP_REV.length()));
/*            if (statusInformation.getRepositoryRevision().startsWith(NOT_IN_REPOSITORY))
            {
                outputDone();
            }
 */
        }
        else if (line.startsWith(TAG)) {
            processTag(line.substring(TAG.length()));
        }
        else if (line.startsWith(DATE)) {
            processDate(line.substring(DATE.length()));
        }
        else if (line.startsWith(OPTIONS)) {
            processOptions(line.substring(OPTIONS.length()));
            if (!statusCommand.isIncludeTags()) {
                outputDone();
            }
        }
        else if (line.startsWith(EXISTING_TAGS)) {
            readingTags = true;
        }
    }

    private File createFile(String fileName) {
        File file = null;

        if (relativeDirectory != null) {
            if (relativeDirectory.trim().equals(".")) { //NOI18N
                file = new File(localPath, fileName);
            }
            else {
                file = new File(localPath, relativeDirectory + '/' + fileName);
            }
        }
        else if (fileArray != null) {
            for (int i = 0; i < fileArray.length; i++) {
                File currentFile = fileArray[i];
                if (currentFile == null || currentFile.isDirectory()) {
                    continue;
                }

                String currentFileName = currentFile.getName();
                if (fileName.equals(currentFileName)) {
                    fileArray[i] = null;
                    file = currentFile;
                    break;
                }
            }
        }

        if (file == null) {
            System.err.println("JAVACVS ERROR!! wrong algorithm for assigning path to single files(1)!!");
        }

        return file;
    }

    private void processFileAndStatusLine(String line) {
        int statusIndex = line.lastIndexOf(STATUS);
        String fileName = line.substring(0, statusIndex).trim();
        if (fileName.startsWith(NO_FILE_FILENAME)) {
            fileName = fileName.substring(8);
        }

        statusInformation.setFile(createFile(fileName));

        String status = new String(line.substring(statusIndex + 8).trim());
        statusInformation.setStatusString(status);
    }

    private boolean assertNotNull() {
        if (statusInformation == null) {
            System.err.println("Bug: statusInformation must not be null!");
            return false;
        }

        return true;
    }

    private void processWorkRev(String line) {
        if (!assertNotNull()) {
            return;
        }
        statusInformation.setWorkingRevision(line.trim().intern());
    }

    private void processRepRev(String line) {
        if (!assertNotNull()) {
            return;
        }
        line = line.trim();
        if (line.startsWith(NOT_IN_REPOSITORY)) {
            statusInformation.setRepositoryRevision(line.trim().intern());
            return;
        }

        int firstSpace = line.indexOf('\t');
        if (firstSpace > 0) {
            statusInformation.setRepositoryRevision(
                    line.substring(0, firstSpace).trim().intern());
            statusInformation.setRepositoryFileName(
                    new String(line.substring(firstSpace).trim()));
        }
        else {
            statusInformation.setRepositoryRevision(""); //NOI18N
            statusInformation.setRepositoryFileName(""); //NOI18N
        }
    }

    private void processTag(String line) {
        if (!assertNotNull()) {
            return;
        }
        statusInformation.setStickyTag(line.trim().intern());
    }

    private void processDate(String line) {
        if (!assertNotNull()) {
            return;
        }
        statusInformation.setStickyDate(line.trim().intern());
    }

    private void processOptions(String line) {
        if (!assertNotNull()) {
            return;
        }
        statusInformation.setStickyOptions(line.trim().intern());
    }

    public void parseEnhancedMessage(String key, Object value) {
    }

}
