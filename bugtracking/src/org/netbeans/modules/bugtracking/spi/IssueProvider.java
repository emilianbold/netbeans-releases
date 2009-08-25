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

package org.netbeans.modules.bugtracking.spi;

import java.net.URL;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.bugtracking.tasklist.TaskListProvider;

/**
 * Represents a provider of issues for the tasklist. Extend if you want to add your issues into the tasklist.
 * 
 * @author Ondra Vrabec
 */
public abstract class IssueProvider {

    /**
     * Adds issues to the task list.
     * @param openTaskList also opens the TaskList top component if set to <code>true</code>
     * @param issuesToAdd issues to add
     */
    protected void add (boolean openTaskList, LazyIssue... issuesToAdd) {
        TaskListProvider.getInstance().add(this, openTaskList, issuesToAdd);
    }

    /**
     * Adds issues to the task list.
     * @param issuesToAdd issues to add
     */
    protected void add (LazyIssue... issuesToAdd) {
        add(false, issuesToAdd);
    }

    /**
     * Removes issues from the task list.
     * @param issuesToAdd issues to remove
     */
    protected void remove (LazyIssue... issuesToAdd) {
        TaskListProvider.getInstance().remove(this, issuesToAdd);
    }

    /**
     * Removes all issues previously added by this instance
     */
    protected void removeAll () {
        TaskListProvider.getInstance().removeAll(this);
    }

    /**
     * Called when an issue is removed from the TaskList in other way than through {@link #remove(org.netbeans.modules.bugtracking.util.IssueTaskListProvider.LazyIssue[])}
     * or {@link #removeAll() }
     * @param lazyIssue
     */
    public abstract void removed(LazyIssue lazyIssue);

    /**
     * Represents an issue displayed in the tasklist.
     */
    public static abstract class LazyIssue {
        private final URL url;
        private String name;
        private boolean valid;

        /**
         *
         * @param url url of the issue, cannot be null
         * @param name displayed name showed in the tasklist
         * @throws NullPointerException if url or name is null
         */
        public LazyIssue (URL url, String name) {
            if (url == null) {
                throw new NullPointerException();
            }
            if (name == null) {
                throw new NullPointerException();
            }
            this.url = url;
            this.name = name;
        }

        /**
         * Returns displayed name of the issue
         * @return displayed name of the issue
         */
        public final String getName () {
            return name;
        }

        /**
         * Sets the issue's name and refreshes the tasklist
         * @param name new issue's name. If set to <code>null</code>, no action is taken.
         */
        protected final void setName (String name) {
            if (name != null) {
                this.name = name;
                setValid(false);
            }
        }

        /**
         * /**
         * Returns url of the issue
         * @return url of the issue
         */
        public final URL getUrl () {
            return url;
        }

        @Override
        public final boolean equals(Object obj) {
            if (obj != null && obj instanceof LazyIssue) {
                return this.url.toString().equals(((LazyIssue)obj).url.toString());
            }
            return false;
        }

        @Override
        public final int hashCode() {
            return url.toString().hashCode();
        }

        /**
         * Implement this to load and return the real issue.
         * @return the real issue or null
         */
        public abstract Issue getIssue ();

        /**
         * If the issue is not valid (previously invalidated by {@link #setValid(boolean)},
         * this will result in repainting the issue in the tasklist after it's next refresh.
         * @return true if the issue is valid, false otherwise
         */
        public final boolean isValid() {
            return valid;
        }

        /**
         * Sets issues validity status.
         * @param valid false will result in refreshing the issue in the next tasklist's refresh.
         */
        public final void setValid (boolean valid) {
            this.valid = valid;
            if (!valid) {
                TaskListProvider.getInstance().refresh();
            }
        }

        /**
         * Implement this and return the url of the repository the issue is in.
         * The value is used in deciding if the issue shall be displayed or hidden in the current tasklist's scope.
         * @return name of the issuetracking repository the issue is in.
         */
        public abstract String getRepositoryUrl();

        /**
         * Implement this and return a list of actions you wish to be displayed in the popup menu of the issue in the tasklist.
         * <strong>Note </strong> that actions <em>Open</em> and <em>Remove</em> are automatically displayed and they should not be
         * items of the list
         * @return list of actions displayed in the issue's popup menu.
         */
        public abstract List<? extends Action> getActions();

        @Override
        public String toString () {
            return super.toString() + ": " + getName();                 //NOI18N
        }
    }
}
