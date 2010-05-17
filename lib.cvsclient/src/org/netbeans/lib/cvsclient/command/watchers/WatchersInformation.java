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
import java.util.*;

import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.util.*;

/**
 * Describes "cvs watchers" commands' parsed information for a file.
 * The fields in instances of this object are populated
 * by response handlers.
 *
 * @author  Milos Kleint
 */
public class WatchersInformation extends FileInfoContainer {

    public static final String WATCH_EDIT = "edit"; //NOI18N
    public static final String WATCH_UNEDIT = "unedit"; //NOI18N
    public static final String WATCH_COMMIT = "commit"; //NOI18N
    public static final String WATCH_TEMP_EDIT = "tedit"; //NOI18N
    public static final String WATCH_TEMP_UNEDIT = "tunedit"; //NOI18N
    public static final String WATCH_TEMP_COMMIT = "tcommit"; //NOI18N

    /**
     * Holds the file that this info belongs to.
     */
    private final File file;

    /**
     * List of users (Watchers instances) that are listening
     * on events for this file.
     */
    private final List userList = new LinkedList();

    /**
     * Creates new istance of the WatchersInformation class.
     */
    public WatchersInformation(File file) {
        this.file = file;
    }

    /**
     * Getter for file concerned in this instance.
     */
    public File getFile() {
        return file;
    }

    /**
     * Adds a watcher to the watchers list.
     * @param watchingInfo a String that's first word is a user name and the
     *                     rest are watching types.
     */
    void addWatcher(String watchingInfo) {
        String temp = watchingInfo.trim();
        temp = temp.replace('\t', ' ');
        int spaceIndex = temp.indexOf(' ');
        if (spaceIndex < 0) {
            //BUGLOG assert.
        }
        else {
            String user = temp.substring(0, spaceIndex);
            String watches = temp.substring(spaceIndex + 1);
            this.userList.add(new WatchersInformation.Watcher(user, watches));
        }
    }

    /**
     * Returns the Iterator with WatchersInformation.Watcher instances.
     * Never returns null.
     */
    public Iterator getWatchersIterator() {
        return this.userList.iterator();
    }

    /**
     * Inner class that holds information about single user and his watches
     * on the file.
     */
    public static class Watcher {

        private final String userName;
        private final String watches;
        private boolean watchingEdit;
        private boolean watchingUnedit;
        private boolean watchingCommit;
        private boolean temporaryEdit;
        private boolean temporaryUnedit;
        private boolean temporaryCommit;

        /**
         * Package private constuctor that creates a new instance of the Watcher.
         * To Be called from outerclass only.
         */
        Watcher(String userName, String watches) {
            this.userName = userName;
            this.watches = watches;

            final StringTokenizer tok = new StringTokenizer(watches, " ", false);
            while (tok.hasMoreTokens()) {
                String token = tok.nextToken();
                if (WATCH_EDIT.equals(token)) {
                    watchingEdit = true;
                }
                else if (WATCH_UNEDIT.equals(token)) {
                    watchingUnedit = true;
                }
                else if (WATCH_COMMIT.equals(token)) {
                    watchingCommit = true;
                }
                else if (WATCH_TEMP_COMMIT.equals(token)) {
                    temporaryCommit = true;
                }
                else if (WATCH_TEMP_EDIT.equals(token)) {
                    temporaryEdit = true;
                }
                else if (WATCH_TEMP_UNEDIT.equals(token)) {
                    temporaryUnedit = true;
                }
                else {
                    BugLog.getInstance().bug("unknown = " + token);
                }
            }
        }

        /**
         * Gets the user that is watching the file.
         */
        public String getUserName() {
            return userName;
        }

        /**
         * Returns all the watches defined on the file.
         */
        public String getWatches() {
            return watches;
        }

        /**
         * User is/isn't watching commit opration.
         */
        public boolean isWatchingCommit() {
            return watchingCommit;
        }

        /**
         * User is/isn't watching edit opration.
         */
        public boolean isWatchingEdit() {
            return watchingEdit;
        }

        /**
         * User is/isn't watching unedit opration.
         */
        public boolean isWatchingUnedit() {
            return watchingUnedit;
        }

        /**
         * User is/isn't temporary watching commit opration.
         */
        public boolean isTempWatchingCommit() {
            return temporaryCommit;
        }

        /**
         * User is/isn't temporary watching edit opration.
         */
        public boolean isTempWatchingEdit() {
            return temporaryEdit;
        }

        /**
         * User is/isn't temporary watching unedit opration.
         */
        public boolean isTempWatchingUnedit() {
            return temporaryUnedit;
        }
    }
}
