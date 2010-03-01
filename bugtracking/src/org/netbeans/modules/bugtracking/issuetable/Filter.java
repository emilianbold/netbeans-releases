
package org.netbeans.modules.bugtracking.issuetable;

import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCacheUtils;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCache;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public abstract class Filter {

    private static Map<Query, Map<Class, Filter>> queryToFilter = new WeakHashMap<Query, Map<Class, Filter>>();

    public abstract String getDisplayName();
    public abstract boolean accept(Issue issue);

    public static Filter getAllFilter(Query query) {
        return getFilter(query, AllFilter.class);
    }
    public static Filter getNotSeenFilter(Query query) {
        return getFilter(query, NotSeenFilter.class);
    }
    public static Filter getNewFilter(Query query) {
        return getFilter(query, NewFilter.class);
    }
    public static Filter getObsoleteDateFilter(Query query) {
        return getFilter(query, ObsoleteDateFilter.class);
    }
    public static Filter getAllButObsoleteDateFilter(Query query) {
        return getFilter(query, AllButObsoleteDateFilter.class);
    }

    private static <T extends Filter> Filter getFilter(Query query, Class<T> clazz) {
        Map<Class, Filter> filters = queryToFilter.get(query);
        if(filters == null) {
            filters = new HashMap<Class, Filter>(5);
            queryToFilter.put(query, filters);
        }
        Filter filter = filters.get(clazz);
        if(filter == null) {
            try {
                Constructor<T> c;
                if(query == null) {
                    c = clazz.getDeclaredConstructor();
                    filter = c.newInstance();
                } else {
                    c = clazz.getDeclaredConstructor(Query.class);
                    filter = c.newInstance(query);
                }
            } catch (Exception ex) {
                BugtrackingManager.LOG.log(Level.SEVERE, null, ex);
            }            
            filters.put(clazz, filter);
        }
        return filter;
    }

    private static class AllFilter extends Filter {
        private final Query query;
        AllFilter(Query query) {
            this.query = query;
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(Query.class, "LBL_AllIssuesFilter");     // NOI18N
        }
        @Override
        public boolean accept(Issue issue) {
            return query.contains(issue);
        }
    }
    private static class NotSeenFilter extends Filter {
        private final Query query;
        NotSeenFilter(Query query) {
            this.query = query;
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(Query.class, "LBL_UnseenIssuesFilter");  // NOI18N
        }
        @Override
        public boolean accept(Issue issue) {
            return !IssueCacheUtils.wasSeen(issue) && query.contains(issue);
        }
    }
    private static class NewFilter extends Filter {
        private final Query query;
        NewFilter(Query query) {
            this.query = query;
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(Query.class, "LBL_NewIssuesFilter");     // NOI18N
        }
        @Override
        public boolean accept(Issue issue) {
            return query.getIssueStatus(issue) == IssueCache.ISSUE_STATUS_NEW;
        }
    }
    private static class ObsoleteDateFilter extends Filter {
        private final Query query;
        ObsoleteDateFilter(Query query) {
            this.query = query;
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(Query.class, "LBL_ObsoleteIssuesFilter");// NOI18N
        }
        @Override
        public boolean accept(Issue issue) {
            return !query.contains(issue);
        }
    }
    private static class AllButObsoleteDateFilter extends Filter {
        private final Query query;
        AllButObsoleteDateFilter(Query query) {
            this.query = query;
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(Query.class, "LBL_AllButObsoleteIssuesFilter");  // NOI18N
        }
        @Override
        public boolean accept(Issue issue) {
            return query.contains(issue);
        }
    }

}
