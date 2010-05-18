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

package org.netbeans.lib.cvsclient.command.remove;

import java.io.*;

import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.event.*;

/**
 * Handles the building of remove information object and the firing of
 * events when complete objects are built.
 *
 * @author  Milos Kleint
 */
public class RemoveBuilder implements Builder {
    private static final String UNKNOWN = ": nothing known about"; //NOI18N
    private static final String WARNING = ": warning: "; //NOI18N
    private static final String SCHEDULING = ": scheduling `"; //NOI18N
    private static final String USE_COMMIT = ": use 'cvs commit' "; //NOI18N
    private static final String DIRECTORY = ": Removing "; //NOI18N
    private static final String STILL_IN_WORKING = ": file `"; //NOI18N
    private static final String REMOVE_FIRST = "first"; //NOI18N
    private static final String UNKNOWN_FILE = "?"; //NOI18N

    /**
     * The status object that is currently being built
     */
    private RemoveInformation removeInformation;

    /**
     * The directory in which the file being processed lives. This is
     * relative to the local directory
     */
    private String fileDirectory;

    /**
     * The event manager to use
     */
    private final EventManager eventManager;

    private final RemoveCommand removeCommand;

    public RemoveBuilder(EventManager eventManager, RemoveCommand removeCommand) {
        this.eventManager = eventManager;
        this.removeCommand = removeCommand;
    }

    public void outputDone() {
        if (removeInformation != null) {
            eventManager.fireCVSEvent(new FileInfoEvent(this, removeInformation));
            removeInformation = null;
        }
    }

    public void parseLine(String line, boolean isErrorMessage) {
        if (line.indexOf(SCHEDULING) >= 0) {
            int endingIndex = line.indexOf('\'');
            String fn = line.substring(line.indexOf(SCHEDULING) + SCHEDULING.length(), endingIndex).trim();
            addFile(fn);
            removeInformation.setRemoved(true);
            outputDone();
        }
        if (line.startsWith(UNKNOWN_FILE)) {
            addFile(line.substring(UNKNOWN_FILE.length()));
            removeInformation.setRemoved(false);
            outputDone();
        }
        if (line.indexOf(STILL_IN_WORKING) >= 0) {
            int endingIndex = line.indexOf('\'');
            String fn = line.substring(line.indexOf(STILL_IN_WORKING) + STILL_IN_WORKING.length(), endingIndex).trim();
            addFile(fn);
            removeInformation.setRemoved(false);
            outputDone();
        }
        // ignore the rest..
    }

    protected File createFile(String fileName) {
        StringBuffer path = new StringBuffer();
        path.append(removeCommand.getLocalDirectory());
        path.append(File.separator);
        if (fileDirectory == null) {
            // happens for single files only
            // (for directories, the dir name is always sent before the actual files)
            File locFile = removeCommand.getFileEndingWith(fileName);
            if (locFile == null) {
                path.append(fileName);
            }
            else {
                path = new StringBuffer(locFile.getAbsolutePath());
            }
        }
        else {
//            path.append(fileDirectory);
//            path.append(File.separator);
            path.append(fileName);
        }
        String toReturn = path.toString();
        toReturn = toReturn.replace('/', File.separatorChar);
        return new File(path.toString());
    }

    protected void addFile(String name) {
        removeInformation = new RemoveInformation();
        removeInformation.setFile(createFile(name));
    }

    public void parseEnhancedMessage(String key, Object value) {
    }
}
