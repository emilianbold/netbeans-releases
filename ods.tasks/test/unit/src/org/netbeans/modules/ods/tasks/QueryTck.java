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
package org.netbeans.modules.ods.tasks;

import com.tasktop.c2c.server.tasks.domain.PredefinedTaskQuery;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.netbeans.modules.ods.tasks.spi.C2CData;
import org.netbeans.modules.ods.tasks.spi.C2CExtender;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class QueryTck extends C2CTestBase {
    public QueryTck(String n) {
        super(n);
    }

    public void testSimpleQuery() throws Exception {
        IRepositoryQuery query = new RepositoryQuery(repository.getConnectorKind(), ""); // NOI18N
        query.setAttribute(C2CData.ATTR_TASK_TYPE, "Defect,Feature");
        query.setAttribute("task.common.summary", "test");
        query.setAttribute("task.common.description", "test");
        final List<TaskData> arr = new ArrayList<TaskData>();
        TaskDataCollector collector = new TaskDataCollector() {
            @Override
            public void accept(TaskData td) {
                arr.add(td);
            }
        };
        String serverRequest = "{\"querySpec\":" + "{\"sortInfo\":null," + "\"thin\":true," + "\"region\":{" + "\"size\":50," + "\"offset\":0" + "}" + "}," + "\"criteria\":{" + "\"@class\":\".NaryCriteria\"," + "\"subCriteria\":[" + "{" + "\"@class\":\".ColumnCriteria\"," + "\"columnValue\":\"test\"," + "\"columnName\":\"description\"," + "\"operator\":\"STRING_CONTAINS\"" + "},{" + "\"@class\":\".ColumnCriteria\"," + "\"columnValue\":\"test\"," + "\"columnName\":\"summary\"," + "\"operator\":\"STRING_CONTAINS\"" + "},{" + "\"@class\":\".NaryCriteria\"," + "\"subCriteria\":[" + "{" + "\"@class\":\".ColumnCriteria\"," + "\"columnValue\":\"Defect\"," + "\"columnName\":\"tasktype\"," + "\"operator\":\"STRING_CONTAINS\"" + "},{" + "\"@class\":\".ColumnCriteria\"," + "\"columnValue\":\"Feature\"," + "\"columnName\":\"tasktype\"," + "\"operator\":\"STRING_CONTAINS\"" + "}" + "]," + "\"operator\":\"OR\"" + "}" + "]," + "\"operator\":\"AND\"" + "}" + "}";
        String serverReply = "{\"queryResult\":{\"offset\":0,\"pageSize\":50,\"totalResultSize\":1," + "\"resultPage\":[{\"priority\":{\"active\":false,\"value\":" + "\"Normal\",\"sortkey\":300,\"id\":3},\"status\":{\"open\":true," + "\"active\":true,\"value\":\"UNCONFIRMED\",\"sortkey\":100,\"id\":1}," + "\"description\":\"test feature\",\"reporter\":{" + "\"loginName\":\"tstupka\",\"realname\":\"Tomas Stupka\",\"gravatarHash\":" + "\"eb3b1d3818c31fd5b61a9e58da70f685\",\"id\":2},\"component\":{\"name\":" + "\"Default\",\"description\":\"default component\",\"initialOwner\":{" + "\"loginName\":\"ovrabec\",\"realname\":\"Ondrej Vrabec\"," + "\"gravatarHash\":\"063a55b94edfbc51aab5d9b8b3316207\",\"id\":1},\"id\":1}," + "\"creationDate\":1339668370000,\"shortDescription\":\"this is a test feature\"," + "\"modificationDate\":1339668370000,\"url\":\"https://q.tasktop.com/alm/#projects/anagramgame/task/15\"," + "\"product\":{\"name\":\"Default\",\"description\":\"default product\",\"isActive\":true,\"id\":1}," + "\"remainingTime\":0.00,\"taskType\":\"Feature\",\"externalTaskRelations\":[]," + "\"commits\":[],\"severity\":{\"value\":\"normal\",\"sortkey\":400,\"id\":4}," + "\"resolution\":{\"value\":\"\",\"sortkey\":100,\"id\":1}," + "\"milestone\":{\"product\":{\"id\":1},\"value\":\"---\",\"sortkey\":0,\"id\":1}," + "\"assignee\":{\"loginName\":\"ovrabec\",\"realname\":\"Ondrej Vrabec\",\"gravatarHash\":" + "\"063a55b94edfbc51aab5d9b8b3316207\",\"id\":1},\"comments\":[],\"customFields\":{" + "\"cf1\":null},\"iteration\":{\"value\":\"---\"},\"keywords\":[],\"watchers\":[],\"workLogs\":[]," + "\"wikiRenderedDescription\":\"<p>test feature</p>\",\"sumOfSubtasksEstimatedTime\":0," + "\"sumOfSubtasksTimeSpent\":0,\"version\":\"1339668370000\",\"id\":15}]}}";
        StringBuilder sb = new StringBuilder();
        expectQuery(repositoryURL() + "/findTasksWithCriteria", sb, serverReply);
        IStatus status = rc.performQuery(repository, query, collector, null, new NullProgressMonitor());
        assertEquals("Status is OK", status.getCode(), IStatus.OK);
        assertJSON("The server query is as expected", serverRequest, sb.toString());
        assertEquals("One task data is found", 1, arr.size());
        TaskData td = arr.get(0);
        assertAttribute("test feature", td.getRoot(), TaskAttribute.DESCRIPTION);
        assertAttribute("this is a test feature", td.getRoot(), TaskAttribute.SUMMARY);
    }

    public void testPredefinedQueries() throws Exception {
        for (PredefinedTaskQuery predefinedId : PredefinedTaskQuery.values()) {
            String name = "Predefined query - " + predefinedId.getLabel();
            IRepositoryQuery query = C2CExtender.getQuery(rc, predefinedId, name, rc.getConnectorKind());
            assertNotNull(query);
            assertEquals(name, query.getSummary());
            assertEquals(1, query.getAttributes().size());
            assertEquals("Attribute PredefinedTaskQuery should have the right value", predefinedId.name(), query.getAttribute("PredefinedTaskQuery"));
            
            final List<TaskData> arr = new ArrayList<TaskData>();
            TaskDataCollector collector = new TaskDataCollector() {
                @Override
                public void accept(TaskData td) {
                    arr.add(td);
                }
            };
            String serverRequest = "{\"predefinedTaskQuery\":\"" + predefinedId.name() + "\"," + "\"querySpec\":" + "{\"region\":{" + "\"offset\":0," + "\"size\":50" + "}," + "\"sortInfo\":null," + "\"thin\":true" + "}" + "}";
            String serverReply = "{\"queryResult\":{\"offset\":0,\"pageSize\":50,\"totalResultSize\":1," + "\"resultPage\":[{\"priority\":{\"active\":false,\"value\":" + "\"Normal\",\"sortkey\":300,\"id\":3},\"status\":{\"open\":true," + "\"active\":true,\"value\":\"UNCONFIRMED\",\"sortkey\":100,\"id\":1}," + "\"description\":\"test feature\",\"reporter\":{" + "\"loginName\":\"tstupka\",\"realname\":\"Tomas Stupka\",\"gravatarHash\":" + "\"eb3b1d3818c31fd5b61a9e58da70f685\",\"id\":2},\"component\":{\"name\":" + "\"Default\",\"description\":\"default component\",\"initialOwner\":{" + "\"loginName\":\"ovrabec\",\"realname\":\"Ondrej Vrabec\"," + "\"gravatarHash\":\"063a55b94edfbc51aab5d9b8b3316207\",\"id\":1},\"id\":1}," + "\"creationDate\":1339668370000,\"shortDescription\":\"this is a test feature\"," + "\"modificationDate\":1339668370000,\"url\":\"https://q.tasktop.com/alm/#projects/anagramgame/task/15\"," + "\"product\":{\"name\":\"Default\",\"description\":\"default product\",\"isActive\":true,\"id\":1}," + "\"remainingTime\":0.00,\"taskType\":\"Feature\",\"externalTaskRelations\":[]," + "\"commits\":[],\"severity\":{\"value\":\"normal\",\"sortkey\":400,\"id\":4}," + "\"resolution\":{\"value\":\"\",\"sortkey\":100,\"id\":1}," + "\"milestone\":{\"product\":{\"id\":1},\"value\":\"---\",\"sortkey\":0,\"id\":1}," + "\"assignee\":{\"loginName\":\"ovrabec\",\"realname\":\"Ondrej Vrabec\",\"gravatarHash\":" + "\"063a55b94edfbc51aab5d9b8b3316207\",\"id\":1},\"comments\":[],\"customFields\":{" + "\"cf1\":null},\"iteration\":{\"value\":\"---\"},\"keywords\":[],\"watchers\":[],\"workLogs\":[]," + "\"wikiRenderedDescription\":\"<p>test feature</p>\",\"sumOfSubtasksEstimatedTime\":0," + "\"sumOfSubtasksTimeSpent\":0,\"version\":\"1339668370000\",\"id\":15}]}}";
            StringBuilder sb = new StringBuilder();
            expectQuery(repositoryURL() + "/findTasksWithQuery", sb, serverReply);
            IStatus status = rc.performQuery(repository, query, collector, null, new NullProgressMonitor());
            assertEquals("Status is OK", status.getCode(), IStatus.OK);
            assertJSON("The server query is as expected", serverRequest, sb.toString());
            assertEquals("One task data is found", 1, arr.size());
            TaskData td = arr.get(0);
            assertAttribute("test feature", td.getRoot(), TaskAttribute.DESCRIPTION);
            assertAttribute("this is a test feature", td.getRoot(), TaskAttribute.SUMMARY);
        }
    }
}
