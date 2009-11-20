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

import org.netbeans.modules.bugtracking.issuetable.IssueNode;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.Map;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCacheUtils;
import org.netbeans.modules.bugtracking.ui.issue.IssueTopComponent;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import static java.lang.Character.isSpaceChar;

/**
 * Represents a bugtracking Issue
 *
 * @author Tomas Stupka
 */
public abstract class Issue {

    private static final int SHORT_DISP_NAME_LENGTH = 15;

    private final PropertyChangeSupport support;

    /**
     * issue data were changed
     */
    public static final String EVENT_ISSUE_DATA_CHANGED = "issue.data_changed"; // NOI18N
    
    private Repository repository;

    private static final RequestProcessor rp = new RequestProcessor("Bugtracking Issue"); // NOI18N

    static {
        IssueAccessorImpl.create();
    }
    private Node[] selection;

    /**
     * Creates an issue
     */
    public Issue(Repository repository) {
        support = new PropertyChangeSupport(this);
        this.repository = repository;
    }

    /**
     * Returns this issues repository
     * 
     * @return
     */
    public Repository getRepository() {
        return repository;
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
        assert issueId != null;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final IssueTopComponent tc = IssueTopComponent.find(issueId);
                final boolean tcOpened = tc.isOpened();
                final Issue[] issue = new Issue[1];
                issue[0] = tc.getIssue();
                if (issue[0] == null) {
                    tc.initNoIssue(issueId);
                }
                if(!tcOpened) {
                    tc.open();
                }
                tc.requestActive();
                rp.post(new Runnable() {
                    public void run() {
                        ProgressHandle handle = null;
                        try {
                            if (issue[0] != null) {
                                handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(Issue.class, "LBL_REFRESING_ISSUE", new Object[]{issueId}));
                                handle.start();
                                issue[0].refresh();
                            } else {
                                handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(Issue.class, "LBL_OPENING_ISSUE", new Object[]{issueId}));
                                handle.start();
                                issue[0] = repository.getIssue(issueId);
                                if(issue[0] == null) {
                                    // lets hope the repository was able to handle this
                                    // because whatever happend, there is nothing else
                                    // we can do at this point
                                    SwingUtilities.invokeLater(new Runnable() {
                                        public void run() {
                                            if(!tcOpened) {
                                                tc.close();
                                            }
                                        }
                                    });
                                    return;
                                }
                                SwingUtilities.invokeLater(new Runnable() {

                                    public void run() {
                                        tc.setIssue(issue[0]);
                                    }
                                });
                                IssueCacheUtils.setSeen(issue[0], true);
                            }
                        } finally {
                            if(handle != null) handle.finish();
                        }
                    }
                });
            }
        });
    }

    /**
     * Opens this issue in the IDE
     */
    public final void open() {
        open(false);
    }

    /**
     * Opens this issue in the IDE
     * @param refresh also refreshes the issue after opening
     * 
     */
    public final void open(final boolean refresh) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                IssueTopComponent tc = IssueTopComponent.find(Issue.this);
                tc.open();
                tc.requestActive();
                rp.post(new Runnable() {
                    public void run() {
                        ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(Issue.class, "LBL_REFRESING_ISSUE", new Object[]{getID()}));
                        try {
                            handle.start();
                            if (refresh && !Issue.this.refresh()) {
                                return;
                            }
                            IssueCacheUtils.setSeen(Issue.this, true);
                        } finally {
                            if(handle != null) handle.finish();
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
    // XXX used only by issue table 
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
     * Returns this issues attributes. 
     * @return
     */
    // XXX used only by cache - move out from the spi
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

    void setSelection(Node[] nodes) {
        this.selection = nodes;
}

    protected Node[] getSelection() {
        return selection;
    }
}
