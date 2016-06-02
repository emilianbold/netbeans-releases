/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */

package org.netbeans.modules.team.server.ui.spi;

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
