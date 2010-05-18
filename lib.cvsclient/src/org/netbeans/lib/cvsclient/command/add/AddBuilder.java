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

package org.netbeans.lib.cvsclient.command.add;

import java.io.*;

import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.event.*;

/**
 * Handles the building of add information object and the firing of
 * events when complete objects are built.
 *
 * @author  Milos Kleint
 */
public class AddBuilder implements Builder {
    private static final String UNKNOWN = ": nothing known about"; //NOI18N
    private static final String ADDED = " added to the repository"; //NOI18N
    private static final String WARNING = ": warning: "; //NOI18N
    private static final String ALREADY_ENTERED = " has already been entered"; //NOI18N
    private static final String SCHEDULING = ": scheduling file `"; //NOI18N
    private static final String USE_COMMIT = ": use 'cvs commit' "; //NOI18N
    private static final String DIRECTORY = "Directory "; //NOI18N
    private static final String READDING = ": re-adding file "; //NOI18N
    private static final String RESURRECTED = ", resurrected"; //NOI18N
    private static final String RESUR_VERSION = ", version "; //NOI18N

    private static final boolean DEBUG = false;

    /**
     * The status object that is currently being built.
     */
    private AddInformation addInformation;

    /**
     * The event manager to use.
     */
    private EventManager eventManager;

    /**
     * The directory in which the file being processed lives.
     * This is relative to the local directory
     */
    private String fileDirectory;

    private AddCommand addCommand;

    private boolean readingTags;

    public AddBuilder(EventManager eventManager, AddCommand addCommand) {
        this.eventManager = eventManager;
        this.addCommand = addCommand;
    }

    public void outputDone() {
        if (addInformation != null) {
            FileInfoEvent event = new FileInfoEvent(this, addInformation);
            eventManager.fireCVSEvent(event);
            addInformation = null;
        }
    }

    public void parseLine(String line, boolean isErrorMessage) {
        if (line.endsWith(ADDED)) {
            String directory =
                    line.substring(DIRECTORY.length(), line.indexOf(ADDED));
            addDirectory(directory);
        }
        else if (line.indexOf(SCHEDULING) >= 0) {
            String filename =
                    line.substring(line.indexOf(SCHEDULING) + SCHEDULING.length(), line.indexOf('\'')).trim();
            addFile(filename);
        }
        else if (line.indexOf(READDING) >= 0) {
            String filename =
                    line.substring(line.indexOf(READDING) + READDING.length(), line.indexOf('(')).trim();
            addFile(filename);
        }
        else if (line.endsWith(RESURRECTED)) {
            String filename =
                    line.substring(0, line.length() - RESURRECTED.length());
            resurrectFile(filename);
        }
        // ignore the rest..
    }

    private File createFile(String fileName) {
        File locFile = addCommand.getFileEndingWith(fileName);
        if (locFile == null) {
            // in case the exact match was not  achieved using the getFileEndingWith method
            // let's try to find the best match possible.
            // iterate from the back of the filename string and try to match the endings
            // of getFiles(). the best match is picked then. 
            // Works ok for files and directories in add, should not probably be used
            // elsewhere where it's possible to have recursive commands and where resulting files
            // are not listed in getFiles()
            String name = fileName.replace('\\', '/');
            File[] files = addCommand.getFiles();
            int maxLevel = name.length();
            File bestMatch = null;
            String[] paths = new String[files.length];
            for (int index = 0; index < files.length; index++) {
                paths[index] = files[index].getAbsolutePath().replace('\\', '/');
            }
            int start = name.lastIndexOf('/');
            String part = null;
            if (start < 0) {
                part = name;
            } else {
                part = name.substring(start + 1);
            }
            while (start >= 0 || part != null) {
                boolean wasMatch = false;
                for (int index = 0; index < paths.length; index++) {
                    if (paths[index].endsWith(part)) {
                        bestMatch = files[index];
                        wasMatch = true;
                    }
                }
                start = name.lastIndexOf('/', start - 1);
                if (start < 0 || !wasMatch) {
                    break;
                }
                part = name.substring(start + 1);
            }
            return bestMatch;
        }
        return locFile;
    }

    private void addDirectory(String name) {
        addInformation = new AddInformation();
        addInformation.setType(AddInformation.FILE_ADDED);
        String dirName = name.replace('\\', '/');
/*        int index = dirName.lastIndexOf('/');
        if (index > 0) {
            dirName = dirName.substring(index + 1, dirName.length());
        }
 */
        addInformation.setFile(createFile(dirName));
        outputDone();
    }

    private void addFile(String name) {
        addInformation = new AddInformation();
        addInformation.setFile(createFile(name));
        addInformation.setType(AddInformation.FILE_ADDED);
        outputDone();
    }

    private void resurrectFile(String line) {
        int versionIndex = line.lastIndexOf(RESUR_VERSION);
        String version = line.substring(versionIndex + RESUR_VERSION.length()).trim();
        String cutLine = line.substring(0, versionIndex).trim();
        int fileIndex = cutLine.lastIndexOf(' ');
        String name = cutLine.substring(fileIndex).trim();

        if (DEBUG) {
            System.out.println("line1=" + line);  //NOI18N
            System.out.println("versionIndex=" + versionIndex);  //NOI18N
            System.out.println("version=" + version);  //NOI18N
            System.out.println("fileindex=" + fileIndex); //NOI18N
            System.out.println("filename=" + name); //NOI18N
        }

        addInformation = new AddInformation();
        addInformation.setType(AddInformation.FILE_RESURRECTED);
        addInformation.setFile(createFile(name));
        outputDone();
    }

    public void parseEnhancedMessage(String key, Object value) {
    }
}
