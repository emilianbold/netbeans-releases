/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.lib.cvsclient.command.commit;

import java.io.*;

import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.event.*;

/**
 * Handles the building of update information object and the firing of
 * events when complete objects are built.
 *
 * @author  Milos Kleint
 */
public class CommitBuilder
        implements Builder {

    /**
     * Parsing constants.
     */
    public static final String UNKNOWN = "commit: nothing known about `"; //NOI18N
    public static final String EXAM_DIR = ": Examining"; //NOI18N
    public static final String REMOVING = "Removing "; //NOI18N
    public static final String NEW_REVISION = "new revision:"; //NOI18N
    public static final String INITIAL_REVISION = "initial revision:"; //NOI18N
    public static final String DELETED_REVISION = "delete"; //NOI18N
    public static final String DONE = "done"; //NOI18N
    public static final String RCS_FILE = "RCS file: "; //NOI18N
    public static final String ADD = "commit: use `cvs add' to create an entry for "; //NOI18N
    public static final String COMMITTED = " <-- "; // NOI18N

    /**
     * The status object that is currently being built.
     */
    private CommitInformation commitInformation;

    /**
     * The directory in which the file being processed lives. This is
     * absolute inside the local directory
     */
    private File fileDirectory;

    /**
     * The event manager to use.
     */
    private final EventManager eventManager;

    private final String localPath;
    
    private final String repositoryRoot;

    private boolean isAdding;

    public CommitBuilder(EventManager eventManager, String localPath, String repositoryRoot) {
        this.eventManager = eventManager;
        this.localPath = localPath;
        this.repositoryRoot = repositoryRoot;
    }

    public void outputDone() {
        if (commitInformation != null) {
            eventManager.fireCVSEvent(new FileInfoEvent(this, commitInformation));
            commitInformation = null;
        }
    }

    public void parseLine(String line, boolean isErrorMessage) {
        int c;
        if (line.indexOf(UNKNOWN) >= 0) {
            outputDone();
            processUnknownFile(line.substring(line.indexOf(UNKNOWN) + UNKNOWN.length()).trim());
        }
        else if (line.indexOf(ADD) > 0) {
            processToAddFile(line.substring(line.indexOf(ADD) + ADD.length()).trim());
        }
        else if ((c = line.indexOf(COMMITTED)) > 0) {
            outputDone();
            String fileName = line.substring(c + COMMITTED.length()).trim();
            int nameIndex = fileName.lastIndexOf('/');
            if (nameIndex != -1) {  //#73181 happens with 1.12 servers: /usr/cvsrepo/Java112/nbproject/project.properties,v  <--  nbproject/project.properties
                fileName = fileName.substring(nameIndex+1);
            }
            File file;
            if (fileDirectory == null) {
                String reposPath = line.substring(0, c).trim();
                if (reposPath.startsWith(repositoryRoot)) {
                    reposPath = reposPath.substring(repositoryRoot.length());
                    if (reposPath.startsWith("/")) reposPath = reposPath.substring(1);
                }
                c = reposPath.lastIndexOf('/');
                if (c > 0) reposPath = reposPath.substring(0, c); // remove the file name
                file = findFile(fileName, reposPath);
            } else {
                file = new File(fileDirectory, fileName);
            }
            processFile(file);
            if (isAdding) {
                commitInformation.setType(CommitInformation.ADDED);
                isAdding = false;
            }
            else {
                commitInformation.setType(CommitInformation.CHANGED);
            }
        }
        else if (line.startsWith(REMOVING)) {
            outputDone();
            processFile(line.substring(REMOVING.length(), line.length() - 1));
            // - 1 means to cut the ';' character
            commitInformation.setType(CommitInformation.REMOVED);
        }
        else if (line.indexOf(EXAM_DIR) >= 0) {
            fileDirectory = new File(localPath, line.substring(line.indexOf(EXAM_DIR) + EXAM_DIR.length()).trim());
        }
        else if (line.startsWith(RCS_FILE)) {
            isAdding = true;
        }
        else if (line.startsWith(DONE)) {
            outputDone();
        }
        else if (line.startsWith(INITIAL_REVISION)) {
            processRevision(line.substring(INITIAL_REVISION.length()));
            commitInformation.setType(CommitInformation.ADDED);
        }
        else if (line.startsWith(NEW_REVISION)) {
            processRevision(line.substring(NEW_REVISION.length()));
        }
    }

    private File createFile(String fileName) {
        return new File(localPath, fileName);
    }

    private void processUnknownFile(String line) {
        commitInformation = new CommitInformation();
        commitInformation.setType(CommitInformation.UNKNOWN);
        int index = line.indexOf('\'');
        String fileName = line.substring(0, index).trim();
        commitInformation.setFile(createFile(fileName));
        outputDone();
    }

    private void processToAddFile(String line) {
        commitInformation = new CommitInformation();
        commitInformation.setType(CommitInformation.TO_ADD);
        String fileName = line.trim();
        if (fileName.endsWith(";")) { //NOI18N
            fileName = fileName.substring(0, fileName.length() - 2);
        }
        commitInformation.setFile(createFile(fileName));
        outputDone();
    }

    private void processFile(String filename) {
        if (commitInformation == null) {
            commitInformation = new CommitInformation();
        }

        if (filename.startsWith("no file")) { //NOI18N
            filename = filename.substring(8);
        }
        commitInformation.setFile(createFile(filename));
    }

    private void processFile(File file) {
        if (commitInformation == null) {
            commitInformation = new CommitInformation();
        }

        commitInformation.setFile(file);
    }

    private void processRevision(String revision) {
        int index = revision.indexOf(';');
        if (index >= 0) {
            revision = revision.substring(0, index);
        }
        revision = revision.trim();
        if (DELETED_REVISION.equals(revision)) {
            commitInformation.setType(CommitInformation.REMOVED);
        }
        commitInformation.setRevision(revision);
    }

    public void parseEnhancedMessage(String key, Object value) {
    }
    
    private File findFile(String fileName, String reposPath) {
        File dir = new File(localPath);
        // happens when adding a new file to a branch
        if (reposPath.endsWith("/Attic")) {
            reposPath = reposPath.substring(0, reposPath.length() - 6);
        }
        // use quick finder and fallback to original algorithm just in case
        File file = quickFindFile(dir, fileName, reposPath);
        if (file != null) return file;
        return findFile(dir, fileName, reposPath);
    }
    
    private File findFile(File dir, String fileName, String reposPath) {
        if (isWorkForRepository(dir, reposPath)) {
            return new File(dir, fileName);
        } else {
            File file = null;
            File[] subFiles = dir.listFiles();
            if (subFiles != null) {
                for (int i = 0; i < subFiles.length; i++) {
                    if (subFiles[i].isDirectory()) {
                        file = findFile(subFiles[i], fileName, reposPath);
                        if (file != null) break;
                    }
                }
            }
            return file;
        }
    }
    
    private File quickFindFile(File dir, String fileName, String reposPath) {
        for (;;) {
            File deepDir = new File(dir, reposPath);
            if (isWorkForRepository(deepDir, reposPath) && isUnderLocalPath(deepDir)) {
                return new File(deepDir, fileName);
            }
            dir = dir.getParentFile();
            if (dir == null) return null;
        }
    }
    
    private boolean isWorkForRepository(File dir, String reposPath) {
        try {
            String repository = eventManager.getClientServices().getRepositoryForDirectory(dir);
            String root = eventManager.getClientServices().getRepository();
            if (repository.startsWith(root)) repository = repository.substring(root.length() + 1);
            return reposPath.equals(repository);
        } catch (IOException e) {
            return false;
        }
    }

    private boolean isUnderLocalPath (File dir) {
        File localDir = new File(localPath);
        while (dir != null) {
            if (dir.equals(localDir)) {
                return true;
            }
            dir = dir.getParentFile();
        }
        return false;
    }
}
