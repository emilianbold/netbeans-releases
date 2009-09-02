/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.impl.indexing;

import java.net.URL;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater.Work;
import org.openide.filesystems.FileEvent;

/**
 *
 * @author Tomas Zezula
 */
class FileEventLog implements Runnable {

    private static final Logger LOG = Logger.getLogger(FileEventLog.class.getName());

    public static enum FileOp {
       DELETE,
       CREATE
    };

    private final ThreadLocal<Map<URL,Map<String,Pair<FileEventLog.FileOp,Work>>>> changes;

    public FileEventLog() {
        this.changes = new ThreadLocal<Map<URL, Map<String, Pair<FileOp, Work>>>>();
    }

   
    public void record (final FileOp operation, final URL root, String relativePath, FileEvent event, final Work work) {
        assert operation != null;
        assert root != null;
        if (relativePath == null) {
            relativePath = "";  //NOI18N
        }
        final Map<URL,Map<String,Pair<FileEventLog.FileOp,Work>>> myChanges = getChanges(true);
        Map<String,Pair<FileOp,Work>> rootSlot = myChanges.get(root);
        if (rootSlot == null) {
            rootSlot = new HashMap<String,Pair<FileOp,Work>>();
            myChanges.put(root, rootSlot);
        }
        rootSlot.put(relativePath, Pair.<FileOp,Work>of(operation,work));
        event.runWhenDeliveryOver(this);
    }

    public void run () {
        try {
            commit();
        } finally {
            cleanUp();
        }
    }

    private Map<URL,Map<String,Pair<FileEventLog.FileOp,Work>>> getChanges(final boolean create) {
        Map<URL,Map<String,Pair<FileEventLog.FileOp,Work>>> res = changes.get();
        if (res == null && create) {
            res = new HashMap<URL,Map<String,Pair<FileEventLog.FileOp,Work>>>();
            changes.set(res);
        }
        return res;
    }

    private void commit () {
        final List<Work> first = new LinkedList<Work>();
        final List<Work> rest = new LinkedList<Work>();
        final IdentityHashMap<Work,Work> seenDelete = new IdentityHashMap<Work, Work>();
        final Map<URL,Map<String,Pair<FileEventLog.FileOp,Work>>> myChanges = getChanges(false);
        if (myChanges != null) {
            for (Map<String,Pair<FileOp,Work>> changesInRoot : myChanges.values()) {
                for (Pair<FileOp,Work> desc : changesInRoot.values()) {
                    if (desc.first == FileOp.DELETE) {
                        if (!seenDelete.containsKey(desc.second)) {
                            first.add(desc.second);
                            seenDelete.put(desc.second, desc.second);
                        }
                    }
                    else {
                        rest.add(desc.second);
                    }
                }
            }
        }
        final RepositoryUpdater ru = RepositoryUpdater.getDefault();
        for (Work wrk : first) {
            LOG.finer("SCHEDULING: " + wrk);    //NOI18N
            ru.scheduleWork(wrk, false);
        }
        for (Work wrk : rest) {
            LOG.finer("SCHEDULING: " + wrk);    //NOI18N
            ru.scheduleWork(wrk, false);
        }
    }

    private void cleanUp() {
        this.changes.remove();
    }


}
