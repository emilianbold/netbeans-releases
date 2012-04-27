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

import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.text.DateFormat;
import java.util.*;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitRevisionInfo.GitFileInfo;
import org.netbeans.libs.git.GitRevisionInfo.GitFileInfo.Status;
import org.netbeans.libs.git.GitTag;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.util.RequestProcessor;

public class RepositoryRevision {

    private GitRevisionInfo message;


    /**
     * List of events associated with the revision.
     */ 
    private final List<Event> events = new ArrayList<Event>(5);
    private final List<Event> dummyEvents;
    private final Map<File, String> commonAncestors = new HashMap<File, String>();
    private final Set<GitTag> tags;
    private final Set<GitBranch> branches;
    private boolean eventsInitialized;
    private Search currentSearch;
    private final PropertyChangeSupport support;
    public static final String PROP_EVENTS_CHANGED = "eventsChanged"; //NOI18N
    private final File repositoryRoot;
    private final File[] selectionRoots;

    RepositoryRevision (GitRevisionInfo message, File repositoryRoot, File[] selectionRoots, Set<GitTag> tags, Set<GitBranch> branches, File dummyFile, String dummyFileRelativePath) {
        this.message = message;
        this.repositoryRoot = repositoryRoot;
        this.selectionRoots = selectionRoots;
        this.tags = tags;
        this.branches = branches;
        support = new PropertyChangeSupport(this);
        dummyEvents = new ArrayList<Event>(1);
        if (dummyFile != null && dummyFileRelativePath != null) {
            dummyEvents.add(new Event(dummyFile, dummyFileRelativePath));
        }
    }

    public Event[] getEvents() {
        return events.toArray(new Event[events.size()]);
    }

    Event[] getDummyEvents () {
        return dummyEvents.toArray(new Event[dummyEvents.size()]);
    }

    public GitRevisionInfo getLog() {
        return message;
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder();
        text.append(getLog().getRevision());
        text.append("\t"); //NOI18N
        text.append(DateFormat.getDateTimeInstance().format(new Date(getLog().getCommitTime())));
        text.append("\t"); //NOI18N
        text.append(getLog().getAuthor()); // NOI18N
        text.append("\n"); // NOI18N
        text.append(getLog().getShortMessage());
        return text.toString();
    }

    String getAncestorCommit (File file, GitClient client, ProgressMonitor pm) throws GitException {
        String ancestorCommit = commonAncestors.get(file);
        if (ancestorCommit == null && !commonAncestors.containsKey(file)) {
            GitRevisionInfo info = null;
            if (getLog().getParents().length == 1) {
                info = client.getPreviousRevision(file, getLog().getRevision(), pm);
            } else if (getLog().getParents().length > 1) {
                info = client.getCommonAncestor(getLog().getParents(), pm);
            }
            ancestorCommit = info == null ? null : info.getRevision();
            commonAncestors.put(file, ancestorCommit);
        }
        return ancestorCommit;
    }

    public GitBranch[] getBranches () {
        return branches == null ? new GitBranch[0] : branches.toArray(new GitBranch[branches.size()]);
    }

    public GitTag[] getTags () {
        return tags == null ? new GitTag[0] : tags.toArray(new GitTag[tags.size()]);
    }
    
    boolean expandEvents () {
        Search s = currentSearch;
        if (s == null && !eventsInitialized) {
            currentSearch = new Search();
            currentSearch.start(Git.getInstance().getRequestProcessor(repositoryRoot), repositoryRoot);
            return true;
        }
        return !eventsInitialized;
    }

    void cancelExpand () {
        Search s = currentSearch;
        if (s != null) {
            s.cancel();
            currentSearch = null;
        }
    }

    boolean isEventsInitialized () {
        return eventsInitialized;
    }
    
    public void addPropertyChangeListener (String propertyName, PropertyChangeListener listener) {
        support.addPropertyChangeListener(propertyName, listener);
    }
    
    public void removePropertyChangeListener (String propertyName, PropertyChangeListener listener) {
        support.removePropertyChangeListener(propertyName, listener);
    }

    File getRepositoryRoot () {
        return repositoryRoot;
    }
    
    public class Event {
        /**
         * The file or folder that this event is about. It may be null if the File cannot be computed.
         */ 
        private final File    file;
    
        private final String path;
        private final Status status;
        private boolean underRoots;
        private final File originalFile;
        private final String originalPath;

        public Event (GitFileInfo changedPath, boolean underRoots) {
            path = changedPath.getRelativePath();
            file = changedPath.getFile();
            originalPath = changedPath.getOriginalPath() == null ? path : changedPath.getOriginalPath();
            originalFile = changedPath.getOriginalFile() == null ? file : changedPath.getOriginalFile();
            status = changedPath.getStatus();
            this.underRoots = underRoots;
        }
        
        private Event (File dummyFile, String dummyPath) {
            this.path = dummyPath;
            this.file = dummyFile;
            this.originalPath = dummyPath;
            this.originalFile = dummyFile;
            this.status = Status.UNKNOWN;
            underRoots = true;
        }

        public RepositoryRevision getLogInfoHeader () {
            return RepositoryRevision.this;
        }

        public File getFile() {
            return file;
        }

        public File getOriginalFile () {
            return originalFile;
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

        boolean isUnderRoots () {
            return underRoots;
        }

        String getOriginalPath () {
            return originalPath;
        }
    }
    
    private class Search extends GitProgressSupport {

        @Override
        protected void perform () {
            Map<File, GitFileInfo> files;
            try {
                files = getLog().getModifiedFiles();
            } catch (GitException ex) {
                GitClientExceptionHandler.notifyException(ex, true);
                files = Collections.<File, GitFileInfo>emptyMap();
            }
            final List<Event> logEvents = prepareEvents(files);
            if (!isCanceled()) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run () {
                        if (!isCanceled()) {
                            events.clear();
                            dummyEvents.clear();
                            events.addAll(logEvents);
                            eventsInitialized = true;
                            currentSearch = null;
                            support.firePropertyChange(RepositoryRevision.PROP_EVENTS_CHANGED, null, new ArrayList<Event>(events));
                        }
                    }
                });
            }
        }

        @Override
        protected void finishProgress () {

        }

        @Override
        protected void startProgress () {

        }

        @Override
        protected ProgressHandle getProgressHandle () {
            return null;
        }

        private void start (RequestProcessor requestProcessor, File repositoryRoot) {
            start(requestProcessor, repositoryRoot, null);
        }

        private List<Event> prepareEvents (Map<File, GitFileInfo> files) {
            final List<Event> logEvents = new ArrayList<Event>(files.size());
            Set<File> renamedFilesOriginals = new HashSet<File>(files.size());
            for (Map.Entry<File, GitFileInfo> e : files.entrySet()) {
                if (e.getValue().getStatus() == Status.RENAMED) {
                    renamedFilesOriginals.add(e.getValue().getOriginalFile());
                }
            }
            
            for (Map.Entry<File, GitFileInfo> e : files.entrySet()) {
                File f = e.getKey();
                if (renamedFilesOriginals.contains(f)) {
                    // lets not track delete part of a rename and display only the rename itself
                    continue;
                }
                GitFileInfo info = e.getValue();
                boolean underRoots = false;
                for (File selectionRoot : selectionRoots) {
                    if (VersioningSupport.isFlat(selectionRoot)) {
                        underRoots = selectionRoot.equals(f.getParentFile());
                    } else {
                        underRoots = Utils.isAncestorOrEqual(selectionRoot, f);
                    }
                    if (underRoots) {
                        break;
                    }
                }
                logEvents.add(new Event(info, underRoots));
            }
            return logEvents;
        }
    }
}
