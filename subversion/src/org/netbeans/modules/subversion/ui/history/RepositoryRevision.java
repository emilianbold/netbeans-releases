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
package org.netbeans.modules.subversion.ui.history;

import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.ISVNLogMessageChangePath;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision.Number;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import java.io.File;
import java.io.FileFilter;
import java.util.*;
import java.util.ArrayList;
import java.util.logging.Level;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.util.SvnUtils;

/**
 * Describes log information for a file. This is the result of doing a
 * cvs log command. The fields in instances of this object are populated
 * by response handlers.
 *
 * @author Maros Sandor
 */
class RepositoryRevision {

    private ISVNLogMessage message;

    private SVNUrl repositoryRootUrl;

    /**
     * List of events associated with the revision.
     */
    private final List<Event> events = new ArrayList<Event>(1);
    private EventDetails eventDetails;
    private List<Event> fakeRootEvent;

    public RepositoryRevision(ISVNLogMessage message, SVNUrl rootUrl) {
        this.message = message;
        this.repositoryRootUrl = rootUrl;
        initEvents();
    }

    public SVNUrl getRepositoryRootUrl() {
        return repositoryRootUrl;
    }

    /**
     * Sets a filter for shown events. Affects the <code>getEvents</code> method, which returns only events falling with the filter.
     * Filter is disabled by default.
     * @param filter a file which is included in filtered events. If <code>null</code> is passed, filter will be disabled.
     */
    public void setEventDetails(EventDetails filter) {
        this.eventDetails = filter;
    }

    private void initEvents() {
        ISVNLogMessageChangePath [] paths = message.getChangedPaths();
        if (paths == null) return;
        for (ISVNLogMessageChangePath path : paths) {
            events.add(new Event(path));
        }
    }

    public boolean showDetails() {
        return eventDetails != null ? eventDetails.showDetails() : true;
    }

    List<Event> getEvents() {
        return getEvents(false);
    }

    /**
     * Returns a list of filtered repository events for this revision. If a filter has been set (by <code>setFilter</code> method),
     * events on files different from the filter will not be returned. To disable the filter, set the filter to <code>null</code>.
     * @return a list of filtered repository events
     */
    List<Event> getEvents(boolean forDiff) {
        if (showDetails()) {
            return events;
        } else {
            if(forDiff) {
                return getFakeRootEvent();
            }
            return Collections.EMPTY_LIST;
        }       
    }

    public ISVNLogMessage getLog() {
        return message;
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder();
        text.append(getLog().getRevision().getNumber());
        text.append("\t");
        text.append(getLog().getDate());
        text.append("\t");
        text.append(getLog().getAuthor()); // NOI18N
        text.append("\n"); // NOI18N
        text.append(getLog().getMessage());
        return text.toString();
    }

    public void sort (Comparator<RepositoryRevision.Event> comparator) {
        if (events == null) {
            return;
        }
        Collections.sort(events, comparator);
    }

    public class Event {

        /**
         * The file or folder that this event is about. It may be null if the File cannot be computed.
         */
        private File    file;

        private ISVNLogMessageChangePath changedPath;

        private String name;
        private String path;

        public Event(ISVNLogMessageChangePath changedPath) {
            this.changedPath = changedPath;
            name = changedPath.getPath().substring(changedPath.getPath().lastIndexOf('/') + 1);
            path = changedPath.getPath().substring(0, changedPath.getPath().lastIndexOf('/'));
        }

        public RepositoryRevision getLogInfoHeader() {
            return RepositoryRevision.this;
        }

        public ISVNLogMessageChangePath getChangedPath() {
            return changedPath;
        }

        public boolean isFakeRoot() {
            return false;
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
        public void setFile(File file) {
            this.file = file;
        }

        public String getName() {
            return name;
        }

        public String getPath() {
            return path;
        }

        @Override
        public String toString() {
            StringBuilder text = new StringBuilder();
            text.append("\t");
            text.append(getPath());
            return text.toString();
        }
    }

    public static class EventFullNameComparator implements Comparator<Event> {
        @Override
        public int compare(Event e1, Event e2) {
            if (e1 == null || e2 == null || e1.getChangedPath() == null || e2.getChangedPath() == null) {
                return 0;
            }
            return e1.getChangedPath().getPath().compareTo(e1.getChangedPath().getPath());
        }
    }

    public static class EventBaseNameComparator implements Comparator<Event> {
        @Override
        public int compare(Event e1, Event e2) {
            if (e1 == null || e2 == null || e1.getName() == null || e2.getName() == null) {
                return 0;
            }
            return e1.getName().compareTo(e2.getName());
        }
    }

    public List<Event> getFakeRootEvent() {
        if(fakeRootEvent == null) {
            Event e = new Event(new ISVNLogMessageChangePath() {
                private String path;
                @Override
                public String getPath() {
                    if(path == null) {
                        try {
                            return SvnUtils.getRelativePath(eventDetails.root);
                        } catch (SVNClientException ex) {
                            Subversion.LOG.log(Level.WARNING, eventDetails.root.getAbsolutePath(), ex);
                        }
                    }
                    return path;
                }
                @Override
                public Number getCopySrcRevision() {
                    return message.getRevision();
                }
                @Override
                public String getCopySrcPath() {
                    return "";
                }
                @Override
                public char getAction() {
                    return '?';
                }
            }) {
                @Override
                public boolean isFakeRoot() {
                    return true;
                }
            };
            e.setFile(eventDetails.root);
            fakeRootEvent = new LinkedList<Event>();
            fakeRootEvent.add(e);
        }
        return fakeRootEvent;
    }

    static abstract class EventDetails {
        private final File root;
        EventDetails(File root) {
            this.root = root;
        }
        protected abstract boolean showDetails();
    }
}
