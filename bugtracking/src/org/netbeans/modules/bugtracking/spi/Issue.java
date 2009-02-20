/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.spi;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Map;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bugtracking.ui.issue.IssueTopComponent;
import org.openide.util.NbBundle;

/**
 * Represens a bugtracking Issue
 *
 * @author Tomas Stupka
 * // XXX lifecycle not defined yet - might live in different queries etc. ...
 */
public abstract class Issue {

    private final PropertyChangeSupport support;
    
    /**
     * Seen property id
     */
    public static String LABEL_NAME_SEEN = "issue.seen";

    private boolean seen;
    private String EVENT_ISSUE_DATA_CHANGED = "issue.data_changed";
    
    /**
     * Creates an issue
     */
    public Issue() { 
        support = new PropertyChangeSupport(this);
    }

    /**
     * Returns this issues display name
     * @return
     */
    public String getDisplayName() {
        return NbBundle.getMessage(Issue.class, "LBL_Issue") + " " +  getID(); // NOI18N
    }

    /**
     * Returns this issues tooltip
     * @return
     */
    public String getTooltip() {
        return NbBundle.getMessage(Issue.class, "LBL_Issue") + " " + getID() + " : " + getSummary(); // NOI18N
    }

    /**
     * Refreshes this Issues data from its bugtracking repositry
     */
    public abstract void refresh();


    // XXX throw exception
    public abstract void addComment(String comment, boolean closeAsFixed);

    /**
     * Returns this issues controller
     * @return
     */
    public abstract BugtrackingController getControler();

    /**
     * Opens this issue in the IDE
     */
    final public void open() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                IssueTopComponent tc = new IssueTopComponent();
                Issue.this.setSeen(true);
                tc.setIssue(Issue.this);
                tc.open();
                tc.requestActive();
            }
        });
    }

    /**
     * Returns a NOde representing this issue
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
     * Returns true if issue was already seen or marked as seen by the user
     * @return
     */
    public boolean wasSeen() {
        return seen;
    }

    /**
     * Sets the seen flag
     * @param seen
     */
    protected void setSeen(boolean seen) {
        this.seen = seen;
    }

    public abstract Map<String, String> getAttributes();

    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    protected void fireDataChanged() {
        support.firePropertyChange(EVENT_ISSUE_DATA_CHANGED, null, null);
    }
}
