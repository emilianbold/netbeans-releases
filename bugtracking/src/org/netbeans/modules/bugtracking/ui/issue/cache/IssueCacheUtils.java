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

package org.netbeans.modules.bugtracking.ui.issue.cache;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.logging.Level;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.openide.util.NbBundle;

/**
 * Issue cache utility methods
 * 
 * @author Tomas Stupka
 */
public class IssueCacheUtils {

    /**
     * Returns if the given issue was seen
     * @param issue issue
     * @return true if issue was seen otherwise false
     */
    public static boolean wasSeen(Issue issue) {
        return getCache(issue).wasSeen(issue.getID());
    }

    /**
     * Changes the given issues seen value to ist opposite value
     * @param issue
     */
    public static void switchSeen(Issue issue) {
        try {
            IssueCache cache = getCache(issue);
            String id = issue.getID();
            cache.setSeen(id, !cache.wasSeen(id));
        } catch (IOException ex) {
            BugtrackingManager.LOG.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Sets the given issues seen status
     * @param issue issue
     * @param seen value to be set
     */
    public static void setSeen(Issue issue, boolean seen) {
        try {
            getCache(issue).setSeen(issue.getID(), seen);
        } catch (IOException ex) {
            BugtrackingManager.LOG.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a description summarizing the changes made
     * in the given issue since the last time it was as seen.
     *
     * @param issue
     * @return
     */
    public static String getRecentChanges(Issue issue) {
        String changes = getCache(issue).getRecentChanges(issue);
        if(changes == null) {
            changes = "";
        } else {
            changes = changes.trim();
    }
        if(changes.equals("") && getCache(issue).getStatus(issue.getID()) == IssueCache.ISSUE_STATUS_MODIFIED) {
            changes = NbBundle.getMessage(IssueCacheUtils.class, "LBL_IssueModified");
        }
        return changes;
    }

    /**
     * Registers the given listener with the given issue
     * @param issue
     * @param l
     */
    public static void addCacheListener (Issue issue, PropertyChangeListener l) {
        getCache(issue).addPropertyChangeListener(issue, l);
    }

    /**
     * Unregisters the given listener with the given issue
     * @param issue
     * @param l
     */
    public static void removeCacheListener (Issue issue, PropertyChangeListener l) {
        getCache(issue).removePropertyChangeListener(issue, l);
    }

    private static IssueCache getCache(Issue issue) {
        Repository repo = issue.getRepository();
        IssueCache cache = repo.getLookup().lookup(IssueCache.class);
        assert cache != null;
        return cache;
    }
}
