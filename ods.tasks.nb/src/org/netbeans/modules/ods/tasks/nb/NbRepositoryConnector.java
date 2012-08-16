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
package org.netbeans.modules.ods.tasks.nb;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.tasktop.c2c.server.common.service.domain.QueryResult;
import com.tasktop.c2c.server.common.service.domain.Region;
import com.tasktop.c2c.server.common.service.domain.SortInfo;
import com.tasktop.c2c.server.common.service.domain.criteria.ColumnCriteria;
import com.tasktop.c2c.server.common.service.domain.criteria.Criteria;
import com.tasktop.c2c.server.common.service.domain.criteria.NaryCriteria;
import com.tasktop.c2c.server.tasks.domain.PredefinedTaskQuery;
import com.tasktop.c2c.server.tasks.domain.QuerySpec;
import com.tasktop.c2c.server.tasks.domain.Task;
import com.tasktop.c2c.server.tasks.service.CriteriaQueryArguments;
import com.tasktop.c2c.server.tasks.service.PredefinedQueryArguments;
import java.util.Set;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.eclipse.mylyn.tasks.core.sync.ISynchronizationSession;
import org.netbeans.modules.ods.tasks.spi.C2CData;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class NbRepositoryConnector extends AbstractRepositoryConnector {
    // the same logger as used by apache http commons
    private static final Logger LOG = Logger.getLogger("httpclient.wire"); // NOI18N
        
    private TaskRepositoryLocationFactory taskRepositoryLocaltionFactory;
    
    public NbRepositoryConnector() {
    }

    @Override
    public boolean canCreateNewTask(TaskRepository tr) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean canCreateTaskFromKey(TaskRepository tr) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getConnectorKind() {
        return "com.tasktop.alm.tasks";
    }

    @Override
    public String getLabel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getRepositoryUrlFromTaskUrl(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TaskData getTaskData(TaskRepository tr, String string, IProgressMonitor ipm) throws CoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getTaskIdFromTaskUrl(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getTaskUrl(String string, String string1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasTaskChanged(TaskRepository tr, ITask itask, TaskData td) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IStatus performQuery(TaskRepository tr, IRepositoryQuery irq, TaskDataCollector tdc, ISynchronizationSession iss, IProgressMonitor ipm) {
        String predefined = irq.getAttribute(NbExtender.ATTR_PREDEFINED_TASK_QUERY);
        QueryTaskResultWrapper ret;
        if (predefined == null) {
            ret = performCriteriaQuery(irq, tr, tdc);
        } else {
            ret = performPredefinedQuery(predefined, irq, tr, tdc);
        }
        TaskAttributeMapper m = new TaskAttributeMapper(tr);
        for (Task t : ret.queryResult.getResultPage()) {
            TaskData d = new TaskData(m, tr.getConnectorKind(), tr.getRepositoryUrl(), "" + t.getId());
            d.getRoot().createMappedAttribute(TaskAttribute.DESCRIPTION).setValue(t.getDescription());
            d.getRoot().createMappedAttribute(TaskAttribute.SUMMARY).setValue(t.getShortDescription());
            tdc.accept(d);
        }
        return Status.OK_STATUS;
    }

    private void addColumnCriteria(NaryCriteria c, String value, String name) {
        if (value == null) {
            return;
        }
        String[] arr = value.split(",");
        if (arr.length == 1) {
            c.addSubCriteria(new ColumnCriteria(name, Criteria.Operator.STRING_CONTAINS, value));
        } else {
            NaryCriteria nc = new NaryCriteria(Criteria.Operator.OR);
            for (String v : arr) {
                addColumnCriteria(nc, v, name);
            }
            c.addSubCriteria(nc);
        }
    }

    @Override
    public void updateRepositoryConfiguration(TaskRepository tr, IProgressMonitor ipm) throws CoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateTaskFromTaskData(TaskRepository tr, ITask itask, TaskData td) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    void setTaskRepositoryLocationFactory(TaskRepositoryLocationFactory f) {
        this.taskRepositoryLocaltionFactory = f;
    }

    final WebResource createResource(TaskRepository tr, String path) {
        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getFeatures().put("com.sun.jersey.api.json.POJOMappingFeature", Boolean.TRUE); // NOI18N
        Set<Class<? extends ContextResolver>> all = Lookup.getDefault().lookupResult(ContextResolver.class).allClasses();
        clientConfig.getClasses().addAll(all);
        Client client = Client.create(clientConfig);
        client.addFilter(new LoggingFilter(LOG));
        client.addFilter(new HTTPBasicAuthFilter(tr.getUserName(), tr.getPassword()));
        return client.resource(tr.getUrl()).path(path);
    }

    protected QueryTaskResultWrapper performCriteriaQuery(IRepositoryQuery irq, TaskRepository tr, TaskDataCollector tdc) throws ClientHandlerException, UniformInterfaceException {
        Region r = new Region(0, 50);
        SortInfo si = null;
        QuerySpec qs = new QuerySpec(r, si, true);
        
        NaryCriteria c = new NaryCriteria(Criteria.Operator.AND);
        addColumnCriteria(c, irq.getAttribute(TaskAttribute.DESCRIPTION), "description"); // NOI18N
        addColumnCriteria(c, irq.getAttribute(TaskAttribute.SUMMARY), "summary"); // NOI18N
        addColumnCriteria(c, irq.getAttribute(C2CData.ATTR_TASK_TYPE), "tasktype"); // NOI18N
        
        CriteriaQueryArguments args = new CriteriaQueryArguments(c, qs);

        WebResource findTasks = createResource(tr, "findTasksWithCriteria"); // NOI18N
        return findTasks.accept(
            MediaType.APPLICATION_JSON_TYPE
        ).entity(args, MediaType.APPLICATION_JSON_TYPE).post(QueryTaskResultWrapper.class);
    }

    private QueryTaskResultWrapper performPredefinedQuery(String predefinedName, IRepositoryQuery irq, TaskRepository tr, TaskDataCollector tdc) {
        Region r = new Region(0, 50);
        SortInfo si = null;
        QuerySpec qs = new QuerySpec(r, si, true);
        
        PredefinedQueryArguments pqa = new PredefinedQueryArguments();
        pqa.setPredefinedTaskQuery(PredefinedTaskQuery.valueOf(predefinedName));
        pqa.setQuerySpec(qs);
        
        WebResource findTasks = createResource(tr, "findTasksWithQuery"); // NOI18N
        return findTasks.accept(
            MediaType.APPLICATION_JSON_TYPE
        ).entity(pqa, MediaType.APPLICATION_JSON_TYPE).post(QueryTaskResultWrapper.class);
    }

    public static final class QueryTaskResultWrapper {
        public QueryResult<Task> queryResult;
    }
}
