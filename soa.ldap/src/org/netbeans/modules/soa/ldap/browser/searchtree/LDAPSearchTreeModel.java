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

package org.netbeans.modules.soa.ldap.browser.searchtree;

import java.util.concurrent.locks.ReentrantLock;
import javax.naming.ldap.LdapName;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeModel;
import org.netbeans.modules.soa.ldap.LDAPConnection;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author anjeleevich
 */
public class LDAPSearchTreeModel extends DefaultTreeModel {

    private LDAPSearchTreeNode rootNode;
    private LDAPConnection connection;
    private String filter;

    private final ReentrantLock lock = new ReentrantLock();

    private LDAPSearchState searchState = null;

    private int visibleResultCount = 0;
    private int addResultCount = 0;

    private RequestProcessor.Task searchTask = null;

    private String userNameAttribute;
    private String groupNameAttribute;

    public LDAPSearchTreeModel(LDAPConnection connection, String filter,
            String userNameAttribute,
            String groupNameAttribute)
    {
        super(null);
        this.connection = connection;
        this.filter = filter;
        this.userNameAttribute = userNameAttribute;
        this.groupNameAttribute = groupNameAttribute;

        rootNode = new LDAPSearchTreeNode(this);

        setRoot(rootNode);
    }

    public String getUserNameAttribute() {
        return userNameAttribute;
    }

    public String getGroupNameAttribute() {
        return groupNameAttribute;
    }

    public void startSearch(RequestProcessor processor) {
        searchTask = processor.post(new SearchRunnable(this));
    }

    public void abortSearch() {
        if (searchTask != null) {
            searchTask.cancel();
        }
    }

    public String getTitle() {
        if (searchState == LDAPSearchState.IN_PROGRESS) {
            return NbBundle.getMessage(getClass(), 
                    "SEARCH_IN_PROGRESS", // NOI18N
                    visibleResultCount);
        } else if (searchState == LDAPSearchState.ABORTED) {
            return NbBundle.getMessage(getClass(), "SEARCH_ABORTED"); // NOI18N
        } else if (searchState == LDAPSearchState.COMPLETE) {
            return NbBundle.getMessage(getClass(), 
                    "SEARCH_COMPLETE", // NOI18N
                    visibleResultCount);
        } else if (searchState == LDAPSearchState.FAILED) {
            return NbBundle.getMessage(getClass(), "SEARCH_FAILED"); // NOI18N
        }

        return NbBundle.getMessage(getClass(), "SEARCHING"); // NOI18N
    }

    public LDAPConnection getConnection() {
        return connection;
    }

    public LDAPSearchState getSearchState() {
        return searchState;
    }

    public String getFilter() {
        return filter;
    }

    AddEntryResult addEntry(LdapName ldapName) {
        assert !SwingUtilities.isEventDispatchThread();
        assert !isLockedByCurrentThread();

        AddEntryResult addResult = new AddEntryResult();

        lock.lock();
        try {
            int size = ldapName.size();

            LDAPSearchTreeNode parent = rootNode;

            for (int i = 0; i < size; i++) {
                parent = parent.getChild(ldapName.getRdn(i), addResult);
            }

            parent.setSearchResult();

            addResultCount++;

            addResult.setUnpublishedResultCount(addResultCount);
            addResult.setNewResultNode(parent);
        } finally {
            lock.unlock();
        }
        
        return addResult;
    }

    void setAttributes(LDAPSearchTreeNode node, String[] objectClasses,
            String userNameAttributeValue, String groupNameAttributeValue)
    {
        lock.lock();
        try {
            node.setAttributes(objectClasses, userNameAttributeValue,
                    groupNameAttributeValue);
        } finally {
            lock.unlock();
        }
    }

    void sync() {
        assert SwingUtilities.isEventDispatchThread();
        assert !isLockedByCurrentThread();

        lock.lock();
        try {
            rootNode.sync(true);

            visibleResultCount += addResultCount;

            addResultCount = 0;

            nodeChanged(rootNode);
        } finally {
            lock.unlock();
        }
    }

    void setSearchState(LDAPSearchState newSearchState) {
        if (this.searchState != newSearchState) {
            this.searchState = newSearchState;

            if ((newSearchState != null)
                    && (newSearchState != LDAPSearchState.IN_PROGRESS))
            {
                searchTask = null;
            }

            nodeChanged(rootNode);
        }
    }

    boolean isLockedByCurrentThread() {
        return lock.isHeldByCurrentThread();
    }
}
