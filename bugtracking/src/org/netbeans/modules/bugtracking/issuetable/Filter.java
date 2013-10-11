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

package org.netbeans.modules.bugtracking.issuetable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.QueryImpl;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public abstract class Filter {

    private static final Map<QueryImpl, Map<Class, Filter>> queryToFilter = new WeakHashMap<QueryImpl, Map<Class, Filter>>();

    public abstract String getDisplayName();
    public abstract boolean accept(IssueNode issue);

    public static Filter getAllFilter(QueryImpl query) {
        return getFilter(query, AllFilter.class);
    }
    public static Filter getNotSeenFilter(QueryImpl query) {
        return getFilter(query, NotSeenFilter.class);
    }
    public static Filter getNewFilter() {
        return getFilter(null, NewFilter.class);
    }

    private static <T extends Filter> Filter getFilter(QueryImpl query, Class<T> clazz) {
        Map<Class, Filter> filters = queryToFilter.get(query);
        if(filters == null) {
            filters = new HashMap<Class, Filter>(5);
            queryToFilter.put(query, filters);
        }
        Filter filter = filters.get(clazz);
        if(filter == null) {
            try {
                Constructor<T> c = clazz.getDeclaredConstructor();
                filter = c.newInstance();
            } catch (NoSuchMethodException ex) {
                BugtrackingManager.LOG.log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                BugtrackingManager.LOG.log(Level.SEVERE, null, ex);
            } catch (InstantiationException ex) {
                BugtrackingManager.LOG.log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                BugtrackingManager.LOG.log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                BugtrackingManager.LOG.log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                BugtrackingManager.LOG.log(Level.SEVERE, null, ex);
            }            
            filters.put(clazz, filter);
        }
        return filter;
    }

    private static class AllFilter extends Filter {
        AllFilter() { }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(Filter.class, "LBL_AllIssuesFilter");     // NOI18N
        }
        @Override
        public boolean accept(IssueNode node) {
            return true;
        }
    }
    private static class NotSeenFilter extends Filter {
        NotSeenFilter() { }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(Filter.class, "LBL_UnseenIssuesFilter");  // NOI18N
        }
        @Override
        public boolean accept(IssueNode node) {
            return node.getIssue().getStatus() != IssueStatusProvider.Status.SEEN;
        }
    }
    private static class NewFilter extends Filter {
        NewFilter() { }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(Filter.class, "LBL_NewIssuesFilter");     // NOI18N
        }
        @Override
        public boolean accept(IssueNode node) {
            return node.getIssue().getStatus() == IssueStatusProvider.Status.INCOMING_NEW;
        }
    }
    
}
