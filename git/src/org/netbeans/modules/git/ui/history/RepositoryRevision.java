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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.git.ui.history;

import java.io.File;
import java.text.DateFormat;
import java.util.*;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitFileInfo;
import org.netbeans.libs.git.GitFileInfo.Status;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitTag;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.modules.git.client.GitClientExceptionHandler;

public class RepositoryRevision {

    private GitRevisionInfo message;


    /**
     * List of events associated with the revision.
     */ 
    private final List<Event> events = new ArrayList<Event>(1);
    private String commonAncestor;
    private final Set<GitTag> tags;
    private final Set<GitBranch> branches;

    public RepositoryRevision (GitRevisionInfo message, Set<GitTag> tags, Set<GitBranch> branches) {
        this.message = message;
        this.tags = tags;
        this.branches = branches;
        initEvents();
        
    }

    RepositoryRevision (GitRevisionInfo message, Set<GitTag> tags, Set<GitBranch> branches, File dummyFile, String dummyFileRelativePath) {
        this.message = message;
        this.tags = tags;
        this.branches = branches;
        if (dummyFile != null && dummyFileRelativePath != null) {
            events.add(new Event(dummyFile, dummyFileRelativePath));
        }
    }

    private void initEvents () {
        try {
            Map<File, GitFileInfo> paths = message.getModifiedFiles();
            if (paths == null || paths.isEmpty()) return;
            for (Map.Entry<File, GitFileInfo> path : paths.entrySet()) {
                events.add(new Event(path.getValue()));
            }
        } catch (GitException ex) {
            GitClientExceptionHandler.notifyException(ex, false);
        }
    }

    public List<Event> getEvents() {
        return events;
    }

    public GitRevisionInfo getLog() {
        return message;
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder();
        text.append(getLog().getRevision());
        text.append("\t");
        text.append(DateFormat.getDateTimeInstance().format(new Date(getLog().getCommitTime())));
        text.append("\t");
        text.append(getLog().getAuthor()); // NOI18N
        text.append("\n"); // NOI18N
        text.append(getLog().getShortMessage());
        return text.toString();
    }

    String getAncestorCommit (GitClient client, ProgressMonitor pm) throws GitException {
        if (commonAncestor == null) {
            if (getLog().getParents().length == 1) {
                commonAncestor = getLog().getParents()[0];
            } else if (getLog().getParents().length > 1) {
                GitRevisionInfo info;
                info = client.getCommonAncestor(getLog().getParents(), pm);
                if (info != null) {
                    commonAncestor = info.getRevision();
                }
            }
        }
        return commonAncestor;
    }

    public GitBranch[] getBranches () {
        return branches == null ? new GitBranch[0] : branches.toArray(new GitBranch[branches.size()]);
    }

    public GitTag[] getTags () {
        return tags == null ? new GitTag[0] : tags.toArray(new GitTag[tags.size()]);
    }
    
    public class Event {
        /**
         * The file or folder that this event is about. It may be null if the File cannot be computed.
         */ 
        private final File    file;
    
        private final String path;
        private final Status status;

        public Event (GitFileInfo changedPath) {
            path = changedPath.getRelativePath();
            file = changedPath.getFile();
            status = changedPath.getStatus();
        }
        
        private Event (File dummyFile, String dummyPath) {
            this.path = dummyPath;
            this.file = dummyFile;
            this.status = Status.UNKNOWN;
        }

        public RepositoryRevision getLogInfoHeader () {
            return RepositoryRevision.this;
        }

        public File getFile() {
            return file;
        }

        public String getName() {
            return getFile().getName();
        }

        public String getPath() {
            return path;
        }
        
        public char getAction () {
            switch (status) {
                case ADDED:
                    return 'A';
                case MODIFIED:
                    return 'M';
                case RENAMED:
                    return 'R';
                case COPIED:
                    return 'C';
                case REMOVED:
                    return 'D';
                default:
                    return '?';
            }
        }
        
        @Override
        public String toString() {
            return path;
        }
    }
}
