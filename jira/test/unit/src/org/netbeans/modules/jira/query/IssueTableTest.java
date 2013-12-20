/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.jira.query;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import junit.framework.Test;
import org.netbeans.modules.jira.*;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.api.Query;
import org.netbeans.modules.bugtracking.issuetable.IssueTable;
import org.netbeans.modules.bugtracking.issuetable.IssuetableTestFactory;
import org.netbeans.modules.jira.client.spi.FilterDefinition;
import org.netbeans.modules.jira.client.spi.JiraConnectorProvider;
import org.netbeans.modules.jira.client.spi.JiraConnectorSupport;
import org.netbeans.modules.jira.repository.JiraRepository;

/**
 *
 * @author tomas
 */
public class IssueTableTest extends IssuetableTestFactory {

    private Map<String, JiraQuery> queries = new HashMap<String, JiraQuery>();
    
    public IssueTableTest(Test test) {
        super(test);
    }
    
    @Override
    protected void setUp() throws Exception {    
        BugtrackingManager.getInstance();
        // need this to initialize cache -> server defined status values & co
        JiraTestUtil.cleanProject(JiraTestUtil.getProject());        
    }

    @Override
    protected void tearDown() throws Exception {        
    }

    @Override
    public Query createQuery() {
        final String queryName = "columnstest";

        JiraRepository repo = JiraTestUtil.getRepository();
        JiraConnectorProvider cp = JiraConnectorSupport.getInstance().getConnector();
        FilterDefinition fd = cp.createFilterDefinition();
        fd.setContentFilter(cp.createContentFilter("glb", true, true, true, true));
        final JiraQuery jq = new JiraQuery( queryName, repo, fd, false, true); // false = not saved
        assertEquals(0, jq.getIssues().size());
        queries.put(queryName, jq);
        return JiraTestUtil.getQuery(jq);
    }

    @Override
    public void setSaved(Query q) {
        JiraQuery jiraQuery = queries.get(q.getDisplayName());
        jiraQuery.getController().save("IssueTableTestQuery" + System.currentTimeMillis(), true);
    }
        
    @Override
    public IssueTable getTable(Query q) {
        try {
            JiraQuery jiraQuery = queries.get(q.getDisplayName());
            QueryController c = jiraQuery.getController();
            Field f = c.getClass().getDeclaredField("issueTable");
            f.setAccessible(true);
            return (IssueTable) f.get(c);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int getColumnsCountBeforeSave() {
        return 7;
    }

    @Override
    public int getColumnsCountAfterSave() {
        return 9;
    }

}
