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
package org.netbeans.lib.cvsclient.command.watchers;

import java.io.*;

import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.event.*;
import org.netbeans.lib.cvsclient.util.*;

/**
 * Handles the building of a watchers information object and the firing of
 * events when complete objects are built.
 *
 * @author  Milos Kleint
 */
public class WatchersBuilder implements Builder {

    private static final String UNKNOWN_FILE = "? "; //NOI18N

    /**
     * The status object that is currently being built.
     */
    private WatchersInformation watchersInfo;

    /**
     * The event manager to use.
     */
    private final EventManager eventManager;

    /**
     * The directory where the command was executed.
     * Used to compute absolute path to the file.
     */
    private final String localPath;

    /**
     * Creates a WatchersBuilder.
     * @param eventManager the event manager that will fire events.
     * @param localPath absolute path to the directory where the command was executed.
     */
    public WatchersBuilder(EventManager eventManager, String localPath) {
        this.eventManager = eventManager;
        this.localPath = localPath;
    }

    public void outputDone() {
        if (watchersInfo != null) {
            eventManager.fireCVSEvent(new FileInfoEvent(this, watchersInfo));
            watchersInfo = null;
        }
    }

    public void parseLine(String line, boolean isErrorMessage) {
        if (line.startsWith(UNKNOWN_FILE)) {
            File file = new File(localPath, line.substring(UNKNOWN_FILE.length()));
            watchersInfo = new WatchersInformation(file);
            outputDone();
            return;
        }

        if (isErrorMessage) {
            return;
        }

        if (line.startsWith(" ") || line.startsWith("\t")) { // NOI18N
            BugLog.getInstance().assertNotNull(watchersInfo);

            watchersInfo.addWatcher(line);
            return;
        }

        // the line starts with file..
        outputDone();
        String trimmedLine = line.trim().replace('\t', ' ');
        int spaceIndex = trimmedLine.indexOf(' ');

        BugLog.getInstance().assertTrue(spaceIndex > 0, "Wrong line = " + line);

        File file = new File(localPath,
                             trimmedLine.substring(0, spaceIndex));
        String watcher = trimmedLine.substring(spaceIndex + 1);
        watchersInfo = new WatchersInformation(file);
        watchersInfo.addWatcher(watcher);
    }

    public void parseEnhancedMessage(String key, Object value) {
    }
}
