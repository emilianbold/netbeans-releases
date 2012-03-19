/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.tasks.ui.dashboard;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.bugtracking.api.Query;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.tasks.ui.actions.Actions;
import org.netbeans.modules.tasks.ui.filter.AppliedFilters;
import org.netbeans.modules.tasks.ui.treelist.TreeListNode;

/**
 *
 * @author jpeska
 */
public abstract class AbstractRepositoryNode extends TreeListNode implements Comparable<AbstractRepositoryNode> {

    private final Repository repository;
    private List<QueryNode> queryNodes;
    private List<QueryNode> filteredQueryNodes;

    public AbstractRepositoryNode(boolean expandable, Repository repository) {
        super(expandable, null);
        this.repository = repository;
        updateNodes();
    }

    protected final void updateNodes() {
        AppliedFilters appliedFilters = DashboardViewer.getInstance().getAppliedFilters();
        queryNodes = new ArrayList<QueryNode>();
        filteredQueryNodes = new ArrayList<QueryNode>();
        for (Query query : repository.getQueries()) {
            QueryNode queryNode = new QueryNode(query, this);
            queryNodes.add(queryNode);
            if (appliedFilters.isEmpty() || !queryNode.getFilteredTaskNodes().isEmpty()) {
                filteredQueryNodes.add(queryNode);
            }
        }
    }

    public final Repository getRepository() {
        return repository;
    }

    @Override
    public final Action[] getPopupActions() {
        List<Action> actions = new ArrayList<Action>();
        actions.add(getRepositoryAction());
        actions.addAll(Actions.getRepositoryPopupActions(this));
        return actions.toArray(new Action[actions.size()]);
    }

    public List<QueryNode> getQueryNodes() {
        return queryNodes;
    }

    public List<QueryNode> getFilteredQueryNodes() {
        return filteredQueryNodes;
    }

    public void setFilteredQueryNodes(List<QueryNode> filteredQueryNodes) {
        this.filteredQueryNodes = filteredQueryNodes;
    }

    public int getFilterHits() {
        int hits = 0;
        for (QueryNode queryNode : filteredQueryNodes) {
            hits += queryNode.getTotalTaskCount();
        }
        return hits;
    }

    protected abstract Action getRepositoryAction();

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractRepositoryNode other = (AbstractRepositoryNode) obj;
        return repository.getDisplayName().equalsIgnoreCase(other.repository.getDisplayName());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (this.repository != null ? this.repository.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(AbstractRepositoryNode toCompare) {
        return repository.getDisplayName().compareToIgnoreCase(toCompare.repository.getDisplayName());
    }

    @Override
    public String toString() {
        return repository.getDisplayName();
    }
}
