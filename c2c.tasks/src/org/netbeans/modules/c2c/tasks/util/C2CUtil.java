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
package org.netbeans.modules.c2c.tasks.util;

import com.tasktop.c2c.internal.client.tasks.core.CfcRepositoryConnector;
import com.tasktop.c2c.internal.client.tasks.core.client.CfcClientData;
import com.tasktop.c2c.internal.client.tasks.core.client.ICfcClient;
import com.tasktop.c2c.internal.client.tasks.core.data.CfcTaskAttribute;
import com.tasktop.c2c.server.tasks.domain.Product;
import com.tasktop.c2c.server.tasks.domain.TaskResolution;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.c2c.tasks.C2C;
import org.netbeans.modules.c2c.tasks.C2CConnector;
import org.netbeans.modules.c2c.tasks.issue.C2CIssue;
import org.netbeans.modules.c2c.tasks.query.C2CQuery;
import org.netbeans.modules.c2c.tasks.repository.C2CRepository;
import org.netbeans.modules.mylyn.GetTaskDataCommand;

/**
 *
 * @author Tomas Stupka
 */
public class C2CUtil {
     public static TaskData createTaskData(CfcRepositoryConnector cfcrc, TaskRepository repository, String summary, String desc, String typeName) throws MalformedURLException, CoreException {
        
        ICfcClient client = cfcrc.getClientManager().getClient(repository);
        client.updateRepositoryConfiguration(new NullProgressMonitor());
        CfcClientData clientData = client.getCalmClientData();
        
        TaskAttributeMapper attributeMapper = cfcrc.getTaskDataHandler().getAttributeMapper(repository);
        TaskData data = new TaskData(attributeMapper, repository.getConnectorKind(), repository.getRepositoryUrl(), "");
        
        TaskAttribute rta = data.getRoot();
        TaskAttribute ta = rta.createMappedAttribute(TaskAttribute.USER_ASSIGNED);
        ta = rta.createMappedAttribute(CfcTaskAttribute.SUMMARY.getKey());
        ta.setValue(summary);
        ta = rta.createMappedAttribute(CfcTaskAttribute.DESCRIPTION.getKey());
        ta.setValue(desc);
        
        ta = rta.createMappedAttribute(CfcTaskAttribute.TASK_TYPE.getKey());
        ta.setValue(clientData.getTaskTypes().iterator().next());
        
        Product product = clientData.getProducts().get(0);
        ta = rta.createMappedAttribute(CfcTaskAttribute.PRODUCT.getKey());
        ta.setValue(product.getName());
        
        ta = rta.createMappedAttribute(CfcTaskAttribute.COMPONENT.getKey());
        ta.setValue(product.getComponents().get(0).getName());
        
        ta = rta.createMappedAttribute(CfcTaskAttribute.MILESTONE.getKey());
        ta.setValue(product.getMilestones().get(0).getValue());
        
        ta = rta.createMappedAttribute(CfcTaskAttribute.ITERATION.getKey());
        Collection<String> c = clientData.getActiveIterations();
        if(!c.isEmpty()) {
            ta.setValue(c.iterator().next());
        }
        
        ta = rta.createMappedAttribute(CfcTaskAttribute.PRIORITY.getKey());
        ta.setValue(clientData.getPriorities().get(0).getValue());
        
        ta = rta.createMappedAttribute(CfcTaskAttribute.SEVERITY.getKey());
        ta.setValue(clientData.getSeverities().get(0).getValue());
        
        ta = rta.createMappedAttribute(CfcTaskAttribute.STATUS.getKey());
        ta.setValue(clientData.getStatusByValue("UNCONFIRMED").getValue());
        
        ta = rta.createMappedAttribute(CfcTaskAttribute.RESOLUTION.getKey());
//        ta.setValue(clientData.getResolutions("UNCONFIRMED").getValue());
        ta = rta.createMappedAttribute(CfcTaskAttribute.DUPLICATE_OF.getKey());
        ta = rta.createMappedAttribute(CfcTaskAttribute.DUEDATE.getKey());
        ta = rta.createMappedAttribute(CfcTaskAttribute.FOUND_IN_RELEASE.getKey());
        ta = rta.createMappedAttribute(CfcTaskAttribute.TAGS.getKey());
        ta = rta.createMappedAttribute(CfcTaskAttribute.EXTERNAL_LINKS.getKey());
        ta = rta.createMappedAttribute(CfcTaskAttribute.OWNER.getKey());
        ta = rta.createMappedAttribute(CfcTaskAttribute.VERSION.getKey());
        ta = rta.createMappedAttribute(CfcTaskAttribute.ESTIMATE_WITH_UNITS.getKey());
        ta = rta.createMappedAttribute(CfcTaskAttribute.REPORTER.getKey());
        ta = rta.createMappedAttribute(CfcTaskAttribute.CC.getKey());
        ta = rta.createMappedAttribute(CfcTaskAttribute.NEWCC.getKey());
        ta = rta.createMappedAttribute(CfcTaskAttribute.PARENT.getKey());
        ta = rta.createMappedAttribute(CfcTaskAttribute.SUBTASK.getKey());
        ta = rta.createMappedAttribute(CfcTaskAttribute.NEWCOMMENT.getKey());
        
        
//        ta.setValue(clientData.get().get(0).getValue());
        
//        ta = rta.createMappedAttribute(BugzillaAttribute.PRODUCT.getKey());
//        ta.setValue(TEST_PROJECT);
//
//        String platform = client.getRepositoryConfiguration().getPlatforms().get(0);
//        ta = rta.createMappedAttribute(BugzillaAttribute.REP_PLATFORM.getKey());
//        ta.setValue(platform);
//
//        String version = client.getRepositoryConfiguration().getVersions(TEST_PROJECT).get(0);
//        ta = rta.createMappedAttribute(BugzillaAttribute.VERSION.getKey());
//        ta.setValue(version);
//
//        String component = client.getRepositoryConfiguration().getComponents(TEST_PROJECT).get(0);
//        ta = rta.createMappedAttribute(BugzillaAttribute.COMPONENT.getKey());
//        ta.setValue(component);

        return data;
    }
     
    public static RepositoryResponse postTaskData(CfcRepositoryConnector cfcrc, TaskRepository repository, TaskData data) throws CoreException {
        Set<TaskAttribute> attrs = new HashSet<TaskAttribute>(); // XXX what is this for
        return  cfcrc.getTaskDataHandler().postTaskData(repository, data, attrs, new NullProgressMonitor());
    }     

    /**
     * Returns TaskData for the given issue id or null if an error occured
     * @param repository
     * @param id
     * @return
     */
    public static TaskData getTaskData(final C2CRepository repository, final String id) {
        return getTaskData(repository, id, true);
    }

    /**
     * Returns TaskData for the given issue id or null if an error occured
     * @param repository
     * @param id
     * @return
     */
    public static TaskData getTaskData(final C2CRepository repository, final String id, boolean handleExceptions) {
        GetTaskDataCommand cmd = new GetTaskDataCommand(C2C.getInstance().getRepositoryConnector(), repository.getTaskRepository(), id);
        repository.getExecutor().execute(cmd, handleExceptions);
        if(cmd.hasFailed() && C2C.LOG.isLoggable(Level.FINE)) {
            C2C.LOG.log(Level.FINE, cmd.getErrorMessage());
        }
        return cmd.getTaskData();
    }
    
    public static void openIssue(C2CIssue c2cIssue) {
        C2C.getInstance().getBugtrackingFactory().openIssue(getRepository(c2cIssue.getRepository()), c2cIssue);
    }
    
    public static void openQuery(C2CQuery c2cQuery) {
        C2C.getInstance().getBugtrackingFactory().openQuery(getRepository(c2cQuery.getRepository()), c2cQuery);
    }

    public static Repository getRepository(C2CRepository c2cRepository) {
        Repository repository = C2C.getInstance().getBugtrackingFactory().getRepository(C2CConnector.ID, c2cRepository.getID());
        if(repository == null) {
            repository = C2C.getInstance().getBugtrackingFactory().createRepository(
                    c2cRepository, 
                    C2C.getInstance().getRepositoryProvider(), 
                    C2C.getInstance().getQueryProvider(),
                    C2C.getInstance().getIssueProvider());
        }
        return repository;
    }

    public static TaskResolution getResolutionByValue(CfcClientData cd, String value) {
        List<TaskResolution> resolutions = cd.getResolutions();
        for (TaskResolution r : resolutions) {
            if(r.getValue().equals(value)) {
                return r;
            }
        }
        return null;
    }
}
