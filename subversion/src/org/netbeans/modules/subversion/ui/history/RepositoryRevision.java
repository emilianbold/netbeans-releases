/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.subversion.ui.history;

import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.ISVNLogMessageChangePath;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import java.io.File;
import java.util.*;

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

    public RepositoryRevision(ISVNLogMessage message, SVNUrl rootUrl) {
        this.message = message;
        this.repositoryRootUrl = rootUrl;
        initEvents();
    }

    public SVNUrl getRepositoryRootUrl() {
        return repositoryRootUrl;
    }

    private void initEvents() {
        ISVNLogMessageChangePath [] paths = message.getChangedPaths();
        if (paths == null) return;
        for (ISVNLogMessageChangePath path : paths) {
            events.add(new Event(path));
        }
    }

    public List<Event> getEvents() {
        return events;
    }

    public ISVNLogMessage getLog() {
        return message;
    }

    public String toString() {        
        StringBuffer text = new StringBuffer();
        text.append(getLog().getRevision().getNumber());
        text.append("\t");
        text.append(getLog().getDate());
        text.append("\t");
        text.append(getLog().getAuthor()); // NOI18N
        text.append("\n"); // NOI18N
        text.append(getLog().getMessage());
        return text.toString();
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
        
        public String toString() {
            StringBuffer text = new StringBuffer();            
            text.append("\t");
            text.append(getPath());
            return text.toString();
        }

        
    }
}
