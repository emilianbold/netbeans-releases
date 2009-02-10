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

package org.netbeans.modules.bugzilla.query;

import javax.swing.JComponent;
import org.netbeans.modules.bugzilla.*;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaAttribute;
import org.eclipse.mylyn.internal.bugzilla.core.IBugzillaConstants;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;

/**
 *
 * @author Tomas Stupka
 */
public class BugzillaQuery extends Query {

    private String name;
    private final BugzillaRepository repository;
    private List<BugzillaIssue> issues;
    private QueryController controller;
    private long lastRefresh = -1;

    private String urlParameters;

    public BugzillaQuery(BugzillaRepository repository) {
        super();
        this.repository = repository;
    }

    public BugzillaQuery(String name, BugzillaRepository repository, String urlParameters, long lastRefresh) {
        this(repository);
        this.name = name;
        this.urlParameters = urlParameters;
        this.lastRefresh = lastRefresh;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public String getTooltip() {
        return name + " - " + repository.getDisplayName();
    }

    @Override
    public QueryController getController() {
        if (controller == null) {
            controller = new QueryController(repository, this, urlParameters);
        }
        return controller;
    }


    @Override
    public ColumnDescriptor[] getColumnDescriptors() {
        return BugzillaIssue.getColumnDescriptors();
    }

    public void refresh(String urlParameters) {
        lastRefresh = System.currentTimeMillis(); // XXX 
//        if (product == null) {
//            // XXX
//            tgethrow new IllegalStateException("need product");
//        }
//        return getIssuesIntern();
        assert urlParameters != null;
        performQuery(urlParameters);
    }


    public String getUrlParameters() {
        return getController().getUrlParameters();
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLastRefresh() {
        return lastRefresh;
    }

    @Override
    public void setSaved(boolean saved) {
        super.setSaved(saved);
    }

    @Override
    public void dataChanged() {
        super.dataChanged();
    }

    /**
     * XXX Shouldn't be called while running
     * 
     * @param refresh
     * @return
     */
    public Issue[] getIssues() {
        if (issues == null) {
            return new Issue[0];
        }
        return issues.toArray(new Issue[issues.size()]);
    }

    @Override
    // XXX create repo wih product if kenai project and use in queries
    public void simpleSearch(String criteria) {
        String[] keywords = criteria.split(" ");

        fireStarted(); // XXX don't fire this explcitly for every connector impl
        try {
            issues = new ArrayList<BugzillaIssue>();
            StringBuffer url = new StringBuffer();
            if(keywords.length == 1 && isNumber(keywords[0])) {
                // only one search criteria -> might be we are looking for the bug with id=values[0]
                url.append(IBugzillaConstants.URL_GET_SHOW_BUG);
                url.append("="); // XXX ???
                url.append(keywords[0]);

                performQueryIntern(url.toString());
            }

            url = new StringBuffer();
            url.append(IBugzillaConstants.URL_BUGLIST);
            url.append("?"); // XXX ???
            url.append("query_format=advanced&short_desc_type=allwordssubstr&short_desc=");
            for (int i = 0; i < keywords.length; i++) {
                String val = keywords[i].trim();
                if(val.equals("")) continue;
                url.append(val);
                if(i < keywords.length - 1) {
                    url.append("+");
                }
            }
            performQueryIntern(url.toString());
        } finally {
            fireFinnished(); // XXX don't fire this explcitly for every connector impl
        }
    }

    void performQuery(String urlParameters)  {
        this.urlParameters = urlParameters; // XXX ths is crap - store parameters, ...

        fireStarted(); // XXX don't fire this explicitly for every connector impl
        try {
            issues = new ArrayList<BugzillaIssue>();
            StringBuffer url = new StringBuffer();
            url.append(IBugzillaConstants.URL_BUGLIST);
            url.append("?"); // XXX ???
            url.append("query_format=advanced");
            url.append(urlParameters);
            performQueryIntern(url.toString());
        } finally {
            fireFinnished(); // XXX don't fire this explcitly for every connector impl
        }
    }

    private boolean isNumber(String str) {
        for (int i = 0; i < str.length() -1; i++) {
            if(!Character.isDigit(str.charAt(i))) return false;
        }
        return true;
    }

    private void performQueryIntern(String url)  {
        final TaskRepository taskRepository = repository.getTaskRepository();
        IRepositoryQuery query = new RepositoryQuery(taskRepository.getConnectorKind(), "");
	query.setUrl(url);
        TaskDataCollector collector = new TaskDataCollector() {
            public void accept(TaskData taskData) {
                BugzillaIssue i = new BugzillaIssue(taskData, taskRepository);
		issues.add(i);
                fireNotifyData(i);
            }
	};
        Bugzilla.getInstance().getRepositoryConnector().performQuery( taskRepository, query, collector, null, new NullProgressMonitor());
        lastRefresh = System.currentTimeMillis();
    }


}
