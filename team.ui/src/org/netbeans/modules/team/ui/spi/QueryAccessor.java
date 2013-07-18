/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.team.ui.spi;

import java.util.List;
import javax.swing.Action;

/**
 * Main access point to Team's Query&Issues API.
 * All methods except those returning an Action or ActionListener are allowed
 * to block indefinetely as they will be called outside AWT thread.
 * However the Dashboard UI may declare appropriate service(s) as unreachable
 * after some configurable time out interval.
 *
 * @author S. Aubrecht
 */
public abstract class QueryAccessor<P> {

    public abstract Class<P> type();
    
    /**
     * Determines whether this project has a tasks service or not
     * 
     * @param project
     * @return <code>true</code> in case the given project has a tasks service otherwise <code>false</code>
     */
    public abstract boolean hasTasks(ProjectHandle<P> project);
    
    /**
     * Retrieve the handle for a query listing all new or changed issues in
     * the given project
     *
     * @param project
     * @return a QueryHandle or null if not available
     */
    public abstract QueryHandle getAllIssuesQuery( ProjectHandle<P> project );

    /**
     * Retrieve the list of queries defined for given project.
     * @param project
     * @return
     */
    public abstract List<QueryHandle> getQueries( ProjectHandle<P> project );

    /**
     * Execute given query and retrieve the results.
     * @param query
     * @return
     */
    public abstract List<QueryResultHandle> getQueryResults( QueryHandle query );


    /**
     *
     * @param project
     * @return Action to invoke when user clicks 'Find Issue...' button.
     */
    public abstract Action getFindIssueAction( ProjectHandle<P> project );

    /**
     *
     * @param project
     * @return Action to invoke when user clicks 'Create Issue...' button.
     */
    public abstract Action getCreateIssueAction( ProjectHandle<P> project );

    /**
     *
     * @param project
     * @return Action to open a task from a given project and with a given id
     */
    public abstract Action getOpenTaskAction ( ProjectHandle<P> project, String taskId );

    /**
     *
     * @param result
     * @return Action to invoke when user clicks given query result link.
     */
    public abstract Action getOpenQueryResultAction( QueryResultHandle result );

    /**
     *
     * @param query
     * @return Action to invoke when user pressed Enter key on given query line.
     */
    public abstract Action getDefaultAction( QueryHandle query );

    /**
     * Notify listeners registered in given Project that the list of project queries
     * has changed.
     * 
     * @param project
     * @param newQueryList
     */
    protected final void fireQueryListChanged( ProjectHandle<P> project, List<QueryHandle> newQueryList ) {
        project.firePropertyChange(ProjectHandle.PROP_QUERY_LIST, null, newQueryList);
    }
}
