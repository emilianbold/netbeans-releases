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
package org.netbeans.lib.cvsclient.command.update;

import java.io.*;

import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.event.*;

/**
 * Handles the building of update information object and the firing of
 * events when complete objects are built.
 *
 * @author  Milos Kleint, Thomas Singer
 */
public class UpdateBuilder
        implements Builder {

    /**
     * Parsing constants..
     */
    public static final String UNKNOWN = ": nothing known about"; //NOI18N
    public static final String EXAM_DIR = ": Updating"; //NOI18N
    public static final String TO_ADD = ": use `cvs add' to create an entry for"; //NOI18N
    public static final String STATES = "U P A R M C ? "; //NOI18N
    public static final String WARNING = ": warning: "; //NOI18N
    public static final String SERVER = "server: "; //NOI18N
    public static final String UPDATE = "update: ";                     //NOI18N
    public static final String PERTINENT = "is not (any longer) pertinent"; //NOI18N
    public static final String REMOVAL = "for removal"; //NOI18N
    public static final String SERVER_SCHEDULING = "server: scheduling"; //NOI18N
    private static final String SERVER_SCHEDULING_12 = "update: scheduling `"; //NOI18N
    private static final String REMOVAL_12 = "' for removal"; //NOI18N
    public static final String CONFLICTS = "rcsmerge: warning: conflicts during merge"; //NOI18N
    public static final String NOT_IN_REPOSITORY = "is no longer in the repository"; //NOI18N;
    //cotacao/src/client/net/riobranco/common/client/gui/BaseDialogThinlet.java already contains the differences between 1.17 and 1.18
    private static final String MERGE_SAME =  " already contains the differences between";
    private static final String MERGED = "Merging differences between"; //NOI18N;

    /**
     * The status object that is currently being built.
     */
    private DefaultFileInfoContainer fileInfoContainer;

    /**
     * The event manager to use.
     */
    private EventManager eventManager;

    /**
     * The local path the command run in.
     */
    private final String localPath;

    private String diagnostics;
    
    /**
     * Holds 'G' or 'C' if the current file was merged or conflicted, respectively.
     */ 
    private String fileMergedOrConflict;


    public UpdateBuilder(EventManager eventManager, String localPath) {
        this.eventManager = eventManager;
        this.localPath = localPath;
    }

    public void outputDone() {
        fileMergedOrConflict = null;
        if (fileInfoContainer != null) {
            if (fileInfoContainer.getFile() == null) {
                System.err.println("#65387 CVS: firing invalid event while processing: " + diagnostics);
            }
            eventManager.fireCVSEvent(new FileInfoEvent(this, fileInfoContainer));
            fileInfoContainer = null;
        }
    }

    public void parseLine(String line, boolean isErrorMessage) {
        diagnostics = line;
        if (line.indexOf(UNKNOWN) >= 0) {
            processUnknownFile(line, line.indexOf(UNKNOWN) + UNKNOWN.length());
        }
        else if (line.indexOf(TO_ADD) >= 0) {
            processUnknownFile(line, line.indexOf(TO_ADD) + TO_ADD.length());
        }
        else if (line.indexOf(EXAM_DIR) >= 0) {  // never comes with :local; connection method
            return;
        }
        else if (line.startsWith(CONFLICTS)) {
            if (fileInfoContainer != null) {
                fileInfoContainer.setType("C"); //NOI18N
                // fire from Merged response which follows
            }
            fileMergedOrConflict = "C";
        }
        else if (line.indexOf(WARNING) >= 0) {
            if (line.indexOf(PERTINENT) > 0) {
                String filename = line.substring(line.indexOf(WARNING) + WARNING.length(),
                                                 line.indexOf(PERTINENT)).trim();
                processNotPertinent(filename);
            }
        }
        else if (line.indexOf(SERVER_SCHEDULING_12) >= 0) {
            if (line.indexOf(REMOVAL_12) > 0) {
                String filename = line.substring(line.indexOf(SERVER_SCHEDULING_12) + SERVER_SCHEDULING_12.length(),
                                                 line.indexOf(REMOVAL_12)).trim();
                processNotPertinent(filename);
            }
        }
        else if (line.indexOf(SERVER_SCHEDULING) >= 0) {
            if (line.indexOf(REMOVAL) > 0) {
                String filename = line.substring(line.indexOf(SERVER_SCHEDULING) + SERVER_SCHEDULING.length(),
                                                 line.indexOf(REMOVAL)).trim();
                processNotPertinent(filename);
            }
        }
        else if (line.indexOf(MERGE_SAME) >= 0) {  // not covered by parseEnhancedMessage
            ensureExistingFileInfoContainer();
            fileInfoContainer.setType(DefaultFileInfoContainer.MERGED_FILE);
            String path = line.substring(0, line.indexOf(MERGE_SAME));
            fileInfoContainer.setFile(createFile(path));
            outputDone();
        }
        else if (line.startsWith(MERGED)) {  // not covered by parseEnhancedMessage
            outputDone();
            fileMergedOrConflict = "G"; 
        }
        else if (line.indexOf(NOT_IN_REPOSITORY) > 0) {
            int pos;
            String filename = null;
            if ((pos = line.indexOf(SERVER)) > -1) {
                filename = line.substring(pos + SERVER.length(), line.indexOf(NOT_IN_REPOSITORY)).trim();
            } else if ((pos = line.indexOf(UPDATE)) > -1) {
                filename = line.substring(pos + UPDATE.length(), line.indexOf(NOT_IN_REPOSITORY)).trim();
            }
            if (filename != null) {
                processNotPertinent(filename);
            }
            return;
        }
        else {
            // otherwise
            if (line.length() > 2) {
                String firstChar = line.substring(0, 2);
                if (STATES.indexOf(firstChar) >= 0) {
                    processFile(line);
                    return;
                }
            }
        }
    }

    private File createFile(String fileName) {
        if (fileName.length() > 1 && fileName.charAt(0) == '`' && fileName.charAt(fileName.length() - 1) == '\'') {
            fileName = fileName.substring(1, fileName.length() - 1);
        }
        return new File(localPath, fileName);
    }

    private void ensureExistingFileInfoContainer() {
        if (fileInfoContainer != null) {
            return;
        }
        fileInfoContainer = new DefaultFileInfoContainer();
    }

    private void processUnknownFile(String line, int index) {
        outputDone();
        fileInfoContainer = new DefaultFileInfoContainer();
        fileInfoContainer.setType("?"); //NOI18N
        String fileName = (line.substring(index)).trim();
        fileInfoContainer.setFile(createFile(fileName));
    }

    private void processFile(String line) {
        String fileName = line.substring(2).trim();

        if (fileName.startsWith("no file")) { //NOI18N
            fileName = fileName.substring(8);
        }

        if (fileName.startsWith("./")) { //NOI18N
            fileName = fileName.substring(2);
        }

        File file = createFile(fileName);
        if (fileInfoContainer != null) {
            // sometimes (when locally modified.. the merged response is followed by mesage M <file> or C <file>..
            // check the file.. if equals.. it's the same one.. don't send again.. the prior type has preference
            if (fileInfoContainer.getFile() == null) {
                // is null in case the global switch -n is used - then no Enhanced message is sent, and no
                // file is assigned the merged file..
                fileInfoContainer.setFile(file);
            }
            if (file.equals(fileInfoContainer.getFile())) {
                // if the previous information does not say anything, prefer newer one
                if (fileInfoContainer.getType().equals("?")) {
                    fileInfoContainer = null;
                } else {
                    outputDone();
                    return;
                }
            }
        }

        if (fileMergedOrConflict != null && line.charAt(0) == 'M') {
            line = fileMergedOrConflict; // can be done this way, see below
        }
        
        outputDone();
        ensureExistingFileInfoContainer();

        fileInfoContainer.setType(line.substring(0, 1));
        fileInfoContainer.setFile(file);
    }

    private void processNotPertinent(String fileName) {
        outputDone();
        File fileToDelete = createFile(fileName);

        ensureExistingFileInfoContainer();

        // HACK - will create a non-cvs status in order to be able to have consistent info format
        fileInfoContainer.setType(DefaultFileInfoContainer.PERTINENT_STATE);
        fileInfoContainer.setFile(fileToDelete);
    }

    /** <tt>Merged</tt> response handler. */
    public void parseEnhancedMessage(String key, Object value) {
        if (key.equals(EnhancedMessageEvent.MERGED_PATH)) {
            ensureExistingFileInfoContainer();
            String path = value.toString();
            File newFile = new File(path);
            // #70106 Merged responce must not rewrite CONFLICTS
            if (newFile.equals(fileInfoContainer.getFile()) == false) {
                fileInfoContainer.setFile(newFile);
                fileInfoContainer.setType(DefaultFileInfoContainer.MERGED_FILE);
                if (fileMergedOrConflict != null) {
                    fileInfoContainer.setType(fileMergedOrConflict);
                }
            }
            outputDone();
        }
    }
}

