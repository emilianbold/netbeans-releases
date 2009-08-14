/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bugtracking.spi;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.ui.issue.IssueTopComponent;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import static java.lang.Character.isSpaceChar;

/**
 * Represens a bugtracking Issue
 *
 * @author Tomas Stupka
 */
public abstract class Issue {

    private static final int SHORT_DISP_NAME_LENGTH = 15;
    private final PropertyChangeSupport support;

    // XXX do we need this?
    public static final String ATTR_DATE_MODIFICATION = "date.modification";    // NOI18N
    /**
     * Seen property id
     */
    public static final String LABEL_NAME_SEEN = "issue.seen";                        // NOI18N
    /**
     * Recetn Changes property id
     */
    public static final String LABEL_RECENT_CHANGES = "issue.recent_changes";         // NOI18N
    /**
     * issue data were changed
     */
    public static final String EVENT_ISSUE_DATA_CHANGED = "issue.data_changed"; // NOI18N
    /**
     * issues seen state changed
     */
    public static final String EVENT_ISSUE_SEEN_CHANGED = "issue.seen_changed"; // NOI18N
    /**
     * No information available
     */
    public static final int ISSUE_STATUS_UNKNOWN = 0;
    /**
     * Issue was seen
     */
    public static final int ISSUE_STATUS_SEEN = 2;
    /**
     * Issue wasn't seen yet
     */
    public static final int ISSUE_STATUS_NEW = 4;
    /**
     * Issue was remotely modified since the last time it was seen
     */
    public static final int ISSUE_STATUS_MODIFIED = 8;
    /**
     * Seen, New or Modified
     */
    public static final int ISSUE_STATUS_ALL =
            ISSUE_STATUS_NEW |
            ISSUE_STATUS_MODIFIED |
            ISSUE_STATUS_SEEN;
    /**
     * New or modified
     */
    public static final int ISSUE_STATUS_NOT_SEEN =
            ISSUE_STATUS_NEW |
            ISSUE_STATUS_MODIFIED;
    
    private Repository repository;

    private static final RequestProcessor rp = new RequestProcessor("Bugtracking Issue"); // NOI18N

    /**
     * Creates an issue
     */
    public Issue(Repository repository) {
        support = new PropertyChangeSupport(this);
        this.repository = repository;
    }

    /**
     * Returns this issues display name
     * @return
     */
    public abstract String getDisplayName();

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
        String displayName = getDisplayName();

        int length = displayName.length();
        int limit = SHORT_DISP_NAME_LENGTH;

        if (length <= limit) {
            return displayName;
        }

        String trimmed = displayName.substring(0, limit).trim();

        StringBuilder buf = new StringBuilder(limit + 4);
        buf.append(trimmed);
        if ((length > (limit + 1)) && isSpaceChar(displayName.charAt(limit))) {
            buf.append(' ');
        }
        buf.append("...");                                              //NOI18N

        return buf.toString();
    }

    /**
     * Returns this issues tooltip
     * @return
     */
    public abstract String getTooltip();

    /**
     * Returns true if the issue isn't stored in a arepository yet. Otherwise false.
     * @return
     */
    public abstract boolean isNew();

    /**
     * Refreshes this Issues data from its bugtracking repositry
     *
     * @return true if the issue was refreshed, otherwise false
     */
    public abstract boolean refresh();

    // XXX throw exception
    public abstract void addComment(String comment, boolean closeAsFixed);

    // XXX throw exception; attach Patch or attachFile?
    public abstract void attachPatch(File file, String description);

    /**
     * Returns this issues controller
     * XXX we don't need this. use get component instead and get rid of the BugtrackingController
     * @return
     */
    public abstract BugtrackingController getController();

    /**
     * Opens the issue with the given issueId in the IDE
     *
     * @param repository
     * @param issueId 
     */
    public static void open(final Repository repository, final String issueId) {
        final ProgressHandle[] handle = new ProgressHandle[1];
        handle[0] = ProgressHandleFactory.createHandle(NbBundle.getMessage(Issue.class, "LBL_OPENING_ISSUE", new Object[]{issueId}));
        handle[0].start();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    final IssueTopComponent tc = IssueTopComponent.find(issueId);
                    final Issue issue = tc.getIssue();
                    if (issue == null) {
                        tc.initNoIssue();
                    }
                    final boolean tcOpened = tc.isOpened();
                    if(!tcOpened) {
                        tc.open();
                    }
                    tc.requestActive();

                    rp.post(new Runnable() {

                        public void run() {
                            try {
                                if (issue != null) {
                                    handle[0].finish();
                                    handle[0] = ProgressHandleFactory.createHandle(NbBundle.getMessage(Issue.class, "LBL_REFRESING_ISSUE", new Object[]{issueId}));
                                    handle[0].start();
                                    issue.refresh();
                                } else {
                                    final Issue refIssue = repository.getIssue(issueId);
                                    SwingUtilities.invokeLater(new Runnable() {
                                        public void run() {
                                            if(refIssue == null) {
                                                // lets hope the repository was able to handle this
                                                // because whatever happend, there is nothing else
                                                // we can do at this point
                                                if(!tcOpened) {
                                                    tc.close();
                                                }
                                                return;
                                            }
                                            tc.setIssue(refIssue);
                                        }
                                    });
                                    try {
                                        if(refIssue != null) {
                                            refIssue.setSeen(true);
                                        }
                                    } catch (IOException ex) {
                                        BugtrackingManager.LOG.log(Level.SEVERE, null, ex);
                                    }
                                }
                            } finally {
                                handle[0].finish();
                            }
                        }
                    });
                } catch (NullPointerException e) { // tc.find(...) on not initialized TC
                    handle[0].finish();
                    throw e;
                }
            }
        });
    }

    /**
     * Opens this issue in the IDE
     */
    final public void open() {
        open(false);
    }

    /**
     * Opens this issue in the IDE
     * @param refresh also refreshes the issue after opening
     * 
     */
    final void open(final boolean refresh) {
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(Issue.class, "LBL_OPENING_ISSUE", new Object[]{getID()}));
        handle.start();
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                IssueTopComponent tc = IssueTopComponent.find(Issue.this);

                tc.open();
                tc.requestActive();

                rp.post(new Runnable() {

                    public void run() {
                        try {
                            try {
                                if (refresh && !Issue.this.refresh()) {
                                    return;
                                }
                                Issue.this.setSeen(true);
                            } catch (IOException ex) {
                                BugtrackingManager.LOG.log(Level.SEVERE, null, ex);
                            }
                        } finally {
                            handle.finish();
                        }
                    }
                });
            }
        });
    }

    /**
     * Returns a Node representing this issue
     * @return
     */
    public abstract IssueNode getNode();

    /**
     * Returns this issues unique ID
     * @return
     */
    public abstract String getID();

    /**
     * Returns this issues summary
     * @return
     */
    public abstract String getSummary();

    /**
     * Returns a description summarizing the changes made
     * in this issue since the last time it was as seen.
     */
    public abstract String getRecentChanges();

    /**
     * Returns true if issue was already seen or marked as seen by the user
     * @return
     */
    public boolean wasSeen() {
        return repository.getCache().wasSeen(getID());
    }

    /**
     * Sets the seen flag
     * @param seen
     */
    public void setSeen(boolean seen) throws IOException {
        boolean oldValue = wasSeen();
        repository.getCache().setSeen(getID(), seen);
        fireSeenChanged(oldValue, seen);
    }

    /**
     * Returns this issues attributes. 
     * @return
     */
    public abstract Map<String, String> getAttributes();

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    /**
     * Notify listeners on this issue that its data were changed
     */
    protected void fireDataChanged() {
        support.firePropertyChange(EVENT_ISSUE_DATA_CHANGED, null, null);
    }

    /**
     * Notify listeners on this issue that the seen state has chaged
     *
     * @param oldSeen the old seen state
     * @param newSeen the new seen state
     * @see #EVENT_ISSUE_SEEN_CHANGED
     */
    protected void fireSeenChanged(boolean oldSeen, boolean newSeen) {
        support.firePropertyChange(EVENT_ISSUE_SEEN_CHANGED, oldSeen, newSeen);
    }
}
