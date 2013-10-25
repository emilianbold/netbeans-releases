/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bugtracking.api;

import java.beans.PropertyChangeListener;
import java.io.File;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.spi.IssueProvider;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.netbeans.modules.bugtracking.ui.issue.IssueAction;

/**
 *
 * @author Tomas Stupka
 */
public final class Issue {
    
    public enum Status {
        /**
         * the user hasn't seen this issue yet
         */
        INCOMING_NEW,
        /**
         * the issue was modified since the issue was seen the last time
         */
        INCOMING_MODIFIED,
        /**
         * the issue is new on client and haven't been submited yet
         */
        OUTGOING_NEW,
        /**
         * there are outgoing changes in the issue
         */
        OUTGOING_MODIFIED,
        /**
         * there are incoming and outgoing changes at one
         */
        CONFLICT,        
        /**
         * the user has seen the issue and there haven't been any changes since then
         */
        SEEN
    }
    
    /**
     * issue data were refreshed
     */
    public static final String EVENT_ISSUE_DATA_CHANGED = IssueImpl.EVENT_ISSUE_DATA_CHANGED;
    
    /**
     * status has changed 
     */
    public static final String EVENT_STATUS_CHANGED = IssueStatusProvider.EVENT_STATUS_CHANGED;
    
    private final IssueImpl impl;

    Issue(IssueImpl impl) {
        this.impl = impl;
    }

    /**
     * Returns the issue id
     * 
     * @return 
     */
    public String getID() {
        return impl.getID();
    }

    /**
     * Returns the tooltip text describing the issue.
     * 
     * @return 
     */
    public String getTooltip() {
        return impl.getTooltip();
    }

    /**
     * Registers a PropertyChangeListener
     * @param listener 
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        impl.addPropertyChangeListener(listener);
    }
    
    /**
     * Unregisters a PropertyChangeListener
     * @param listener 
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        impl.removePropertyChangeListener(listener);
    }

    /**
     * Refresh the issues state from the remote repository
     * 
     * @return 
     */
    public boolean refresh() {
        return impl.refresh();
    }

    /**
     * Returns the issues display name. Typicaly this should be the issue id and summary.
     * 
     * @return 
     */
    public String getDisplayName() {
        return impl.getDisplayName();
    }
    
    /**
     * Returns a short variant of the display name. The short variant is used
     * in cases where the full display name might be too long, such as when used
     * as a title of a tab. The default implementation uses the
     * the {@linkplain #getDisplayName full display name} as a base and trims
     * it to maximum of {@value #SHORT_DISP_NAME_LENGTH} characters if
     * necessary. If it was necessary to trim the name (i.e. if the full name
     * was longer then {@value #SHORT_DISP_NAME_LENGTH}), then an ellipsis
     * is appended to the end of the trimmed display name.
     *
     * @return  short variant of the display name
     * @see #getDisplayName
     */
    public String getShortenedDisplayName() {
        return impl.getShortenedDisplayName();
    }    

    /**
     * Opens this issue in the IDE
     */
    public void open() {
        impl.open();
    }

    /**
     * Opens the issue with the given issueId in the IDE. In case that issueId
     * is null a new issue will be created.
     *
     * @param repository
     * @param issueId
     */
    public static void open(final Repository repository, final String issueId) {
        if(issueId == null) {
            IssueAction.createIssue(repository.getImpl());
        } else {            
            IssueAction.openIssue(repository.getImpl(), issueId);
        }
    }

    IssueImpl getImpl() {
        return impl;
    }

    /**
     * Returns this issues summary
     * 
     * @return 
     */
    public String getSummary() {
        return impl.getSummary();
    }

    public boolean isFinished() {
        return impl.isFinished();
    }
    
    public Status getStatus() {
        IssueStatusProvider.Status status = impl.getStatus();
        if(status == null) {
            // no status provided -> lets handle as if it was seen (uptodate)
            return Status.SEEN;
        }
        switch(status) {
            case SEEN:
                return Status.SEEN;
            case INCOMING_NEW:
                return Status.INCOMING_NEW;
            case INCOMING_MODIFIED:
                return Status.INCOMING_MODIFIED;
            case OUTGOING_NEW:
                return Status.OUTGOING_NEW;
            case OUTGOING_MODIFIED:
                return Status.OUTGOING_MODIFIED;
            case CONFLICT:
                return Status.CONFLICT;
            default:
                throw new IllegalStateException("Unexpected status value " + status);
        }
    }
    
    /**
     * 
     * @param file
     * @param description 
     * @param isPatch 
     */
    public void attachFile(File file, String description, boolean isPatch) {
        impl.attachFile(file, description, isPatch);
    }
    
    public void addComment(String msg, boolean closeAsFixed) {
        impl.addComment(msg, closeAsFixed);
    }
    
    public Repository getRepository() {
        return impl.getRepositoryImpl().getRepository();
    }

}
