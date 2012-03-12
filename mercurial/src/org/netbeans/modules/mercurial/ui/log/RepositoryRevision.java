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
package org.netbeans.modules.mercurial.ui.log;

import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.*;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.HistoryRegistry;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.modules.versioning.util.Utils;

/**
 * Describes log information for a file. This is the result of doing a
 * cvs log command. The fields in instances of this object are populated
 * by response handlers.
 *
 * @author Maros Sandor
 */
public class RepositoryRevision {

    private HgLogMessage message;

    private final File repositoryRoot;
    private final File[] selectionRoots;
    private boolean eventsInitialized;
    private Search currentSearch;
    private final PropertyChangeSupport support;
    public static final String PROP_EVENTS_CHANGED = "eventsChanged";

    /**
     * List of events associated with the revision.
     */ 
    private final List<Event> events = new ArrayList<Event>(5);
    private final List<Event> dummyEvents;
    private final boolean incoming;
    private final Set<String> headOfBranches;

    public RepositoryRevision(HgLogMessage message, File repositoryRoot, File[] selectionRoots, boolean isIncoming, Set<String> headOfBranches) {
        this.message = message;
        this.repositoryRoot = repositoryRoot;
        this.selectionRoots = selectionRoots;
        this.incoming = isIncoming;
        this.headOfBranches = headOfBranches;
        support = new PropertyChangeSupport(this);
        dummyEvents = prepareEvents(message.getDummyChangedPaths());
    }

    public File getRepositoryRoot() {
        return repositoryRoot;
    }

    Event[] getEvents() {
        return events.toArray(new Event[events.size()]);
    }

    Event[] getDummyEvents () {
        return dummyEvents.toArray(new Event[dummyEvents.size()]);
    }

    public HgLogMessage getLog() {
        return message;
    }

    @Override
    public String toString() {        
        StringBuilder text = new StringBuilder();
        text.append(getLog().getRevisionNumber());
        text.append("\t");
        text.append(getLog().getCSetShortID());
        text.append("\t");
        text.append(getLog().getDate());
        text.append("\t");
        text.append(getLog().getAuthor()); // NOI18N
        text.append("\n"); // NOI18N
        text.append(getLog().getMessage());
        return text.toString();
    }

    boolean expandEvents () {
        Search s = currentSearch;
        if (s == null && !eventsInitialized) {
            currentSearch = new Search();
            currentSearch.start(Mercurial.getInstance().getRequestProcessor(repositoryRoot));
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

    boolean isHeadOfBranch (String branchName) {
        return headOfBranches.contains(branchName);
    }
    
    public class Event {
    
        /**
         * The file that this event is about. It may be null if the File cannot be computed.
         */ 
        private File    file;
        private File originalFile;
    
        private HgLogMessageChangedPath changedPath;

        private String name;
        private String path;
        private boolean underRoots;

        Event (HgLogMessageChangedPath changedPath) {
            this.changedPath = changedPath;
            name = changedPath.getPath().substring(changedPath.getPath().lastIndexOf('/') + 1);
            
            int indexPath = changedPath.getPath().lastIndexOf('/');
            if(indexPath > -1)
                path = changedPath.getPath().substring(0, indexPath);
            else
                path = "";
        }

        public RepositoryRevision getLogInfoHeader() {
            return RepositoryRevision.this;
        }

        HgLogMessageChangedPath getChangedPath() {
            return changedPath;
        }

        /** Getter for property file.
         * @return Value of property file.
         */
        public File getFile() {
            return file;
        }

        /** Setter for property file.
         * @param file New value of property file.
         */
        public void setFile(File file, boolean isUnderRoots) {
            this.file = file;
            this.underRoots = isUnderRoots;
        }

        public File getOriginalFile() {
            return originalFile;
        }

        void setOriginalFile (File file) {
            this.originalFile = file;
        }

        public String getName() {
            return name;
        }

        public String getPath() {
            return path;
        }
        
        @Override
        public String toString() {
            return changedPath.getPath();
        }

        boolean isUnderRoots () {
            return underRoots;
        }
    }

    private List<Event> prepareEvents (HgLogMessageChangedPath[] paths) {
        final List<Event> logEvents = new ArrayList<Event>(paths.length);
        for (HgLogMessageChangedPath path : paths) {
            logEvents.add(new Event(path));
        }
        for (RepositoryRevision.Event event : logEvents) {
            String filePath = event.getChangedPath().getPath();
            File f = new File(repositoryRoot, filePath);
            File cachedRename = HistoryRegistry.getInstance().getHistoryFile(repositoryRoot, f, message.getCSetShortID(), true);
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
            if (cachedRename != null) {
                f = cachedRename;
            }
            event.setFile(f, underRoots);
            event.setOriginalFile(f);
        }
        for (RepositoryRevision.Event event : logEvents) {
            if ((event.getChangedPath().getAction() == HgLogMessage.HgCopyStatus || event.getChangedPath().getAction() == HgLogMessage.HgRenameStatus)
                    && event.getChangedPath().getCopySrcPath() != null) {
                File originalFile = new File(repositoryRoot, event.getChangedPath().getCopySrcPath());
                event.setOriginalFile(originalFile);
            }
        }
        return logEvents;
    }

    private class Search extends HgProgressSupport {

        @Override
        protected void perform () {
            getLog().refreshChangedPaths(this, incoming);
            HgLogMessageChangedPath [] paths = getLog().getChangedPaths();            
            final List<Event> logEvents = prepareEvents(paths);
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
        protected void finnishProgress () {

        }

        @Override
        protected void startProgress () {

        }

        @Override
        protected ProgressHandle getProgressHandle () {
            return null;
        }
    }
}
