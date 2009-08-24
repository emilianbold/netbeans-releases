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

package org.netbeans.modules.jira.kenai;

import org.eclipse.mylyn.internal.jira.core.model.JiraFilter;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.jira.JiraConfig;
import org.netbeans.modules.jira.JiraConnector;
import org.netbeans.modules.jira.query.JiraQuery;
import org.netbeans.modules.jira.query.QueryController;
import org.netbeans.modules.jira.repository.JiraRepository;

/**
 *
 * @author Tomas Stupka
 */
public class KenaiQuery extends JiraQuery {
    private boolean predefinedQuery = false;
    private String project;

    public KenaiQuery(String name, JiraRepository repository, JiraFilter jf, String project, boolean saved, boolean predefined) {
        super(name, repository, jf, saved, false);
        this.predefinedQuery = predefined;
        this.project = project;
        controller = createControler(repository, this, jf);
        boolean autoRefresh = JiraConfig.getInstance().getQueryAutoRefresh(getDisplayName());
        if(autoRefresh) {
            getRepository().scheduleForRefresh(this);
        }
    }

    @Override
    protected QueryController createControler(JiraRepository r, JiraQuery q, JiraFilter jiraFilter) {
        KenaiQueryController c = new KenaiQueryController(r, q, jiraFilter, project, predefinedQuery);
        return c;
    }

    @Override
    protected void logQueryEvent(int count, boolean autoRefresh) {
        BugtrackingUtil.logQueryEvent(
            JiraConnector.getConnectorName(),
            getDisplayName(),
            count,
            true,
            autoRefresh);
    }

    @Override
    protected String getStoredQueryName() {
        return super.getStoredQueryName() + "-" + project;
    }

}
