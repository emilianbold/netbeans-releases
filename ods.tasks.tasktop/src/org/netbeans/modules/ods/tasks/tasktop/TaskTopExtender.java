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
package org.netbeans.modules.ods.tasks.tasktop;

import com.tasktop.c2c.internal.client.tasks.core.C2CRepositoryConnector;
import com.tasktop.c2c.internal.client.tasks.core.client.CfcClientData;
import com.tasktop.c2c.internal.client.tasks.core.client.IC2CClient;
import com.tasktop.c2c.internal.client.tasks.core.util.C2CQueryUtil;
import com.tasktop.c2c.server.tasks.domain.Component;
import com.tasktop.c2c.server.tasks.domain.ExternalTaskRelation;
import com.tasktop.c2c.server.tasks.domain.FieldDescriptor;
import com.tasktop.c2c.server.tasks.domain.Keyword;
import com.tasktop.c2c.server.tasks.domain.Milestone;
import com.tasktop.c2c.server.tasks.domain.PredefinedTaskQuery;
import com.tasktop.c2c.server.tasks.domain.Priority;
import com.tasktop.c2c.server.tasks.domain.Product;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.TaskResolution;
import com.tasktop.c2c.server.tasks.domain.TaskSeverity;
import com.tasktop.c2c.server.tasks.domain.TaskStatus;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;
import java.util.Collection;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.netbeans.modules.ods.tasks.spi.C2CData;
import org.netbeans.modules.ods.tasks.spi.C2CExtender;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/** Implementation of the extender based on C2C internal data.
 */
@ServiceProvider(service=C2CExtender.class, position=99999)
public final class TaskTopExtender extends C2CExtender<CfcClientData> {
    public TaskTopExtender() {
    }
    
    @Override
    protected AbstractRepositoryConnector spiCreate() {
        return new C2CRepositoryConnector();
    }

    @Override
    protected void spiAssignTaskRepositoryLocationFactory(AbstractRepositoryConnector rc, TaskRepositoryLocationFactory taskRepositoryLocationFactory) {
        C2CRepositoryConnector cfc = (C2CRepositoryConnector)rc;
        cfc.getClientManager().setTaskRepositoryLocationFactory(taskRepositoryLocationFactory);
    }
    
    @Override
    protected synchronized C2CData spiClientData(AbstractRepositoryConnector rc, TaskRepository taskRepository) {
        C2CRepositoryConnector cfc = (C2CRepositoryConnector)rc;
        IC2CClient client = cfc.getClientManager().getClient(taskRepository);
        CfcClientData clientData = client.getC2CClientData();

        if (!clientData.isInitialized()) {
            try {
                client.updateRepositoryConfiguration(new NullProgressMonitor());
            } catch (CoreException ex) {
                // XXX
                Exceptions.printStackTrace(ex);
            }
        }
        return createData(clientData);
    }

    @Override
    protected void spiRepositoryRemove(AbstractRepositoryConnector rc, TaskRepository r) {
        C2CRepositoryConnector cfc = (C2CRepositoryConnector)rc;
        cfc.getClientManager().repositoryRemoved(r);
    }

    @Override
    protected IRepositoryQuery spiQuery(
        AbstractRepositoryConnector rc,
        PredefinedTaskQuery predefinedTaskQuery,
        String name, String connectorKind
    ) {
        return C2CQueryUtil.getQuery(predefinedTaskQuery, name, connectorKind);
    }

    @Override
    protected String spiProductKey(String component, String product) {
        return CfcClientData.getProductKey(component, product);
    }

    @Override
    protected RepositoryConfiguration spiDataRepositoryConfiguration(CfcClientData d) {
        return d.getRepositoryConfiguration();
    }

    @Override
    protected List<TaskStatus> spiDataStatuses(CfcClientData d) {
        return d.getStatuses();
    }

    @Override
    protected List<TaskStatus> spiDataValidStatuses(CfcClientData data, TaskStatus originalStatus) {
        return data.computeValidStatuses(originalStatus);
    }

    @Override
    protected List<TaskResolution> spiDataValidResolutions(CfcClientData data, TaskStatus status) {
        return data.computeValidResolutions(status);
    }

    @Override
    protected List<TaskSeverity> spiDataSeverities(CfcClientData data) {
        return data.getSeverities();
    }

    @Override
    protected List<Priority> spiDataPriorities(CfcClientData data) {
        return data.getPriorities();
    }

    @Override
    protected void spiDataupdate(CfcClientData data, RepositoryConfiguration repositoryConfiguration) {
        data.update(repositoryConfiguration);
    }

    @Override
    protected boolean spiDataInitialized(CfcClientData data) {
        return data.isInitialized();
    }

    @Override
    protected <T> T spiDataValue(CfcClientData data, String value, Class<T> type) {
        return data.getValue(value, type);
    }

    @Override
    protected List<Milestone> spiMilestones(CfcClientData data) {
        return data.getMilestones();
    }

    @Override
    protected List<TaskUserProfile> spiUsers(CfcClientData data) {
        return data.getUsers();
    }

    @Override
    protected List<Product> spiProducts(CfcClientData data) {
        return data.getProducts();
    }

    @Override
    protected List<Component> spiComponents(CfcClientData data) {
        return data.getComponents();
    }

    @Override
    protected List<TaskResolution> spiResolutions(CfcClientData data) {
        return data.getResolutions();
    }

    @Override
    protected List<Milestone> spiMilestones(CfcClientData data, Product product) {
        return data.getMilestones(product);
    }

    @Override
    protected List<Component> spiComponents(CfcClientData data, Product product) {
        return data.getComponents(product);
    }

    @Override
    protected TaskStatus spiStatusByValue(CfcClientData data, String value) {
        return data.getStatusByValue(value);
    }

    @Override
    protected FieldDescriptor spiFieldDescriptor(CfcClientData data, TaskAttribute attribute) {
        return data.getFieldDescriptor(attribute);
    }

    @Override
    protected List<FieldDescriptor> spiCustomFields(CfcClientData data) {
        return data.getCustomFields();
    }

    @Override
    protected Collection<Keyword> spiKeywords(CfcClientData data) {
        return data.getKeywords();
    }

    @Override
    protected Collection<String> spiTaskTypes(CfcClientData data) {
        return data.getTaskTypes();
    }

    @Override
    protected Collection<String> spiAllIterations(CfcClientData data) {
        return data.getAllIterations();
    }

    @Override
    protected Collection<String> spiActiveIterations(CfcClientData data) {
        return data.getActiveIterations();
    }

    @Override
    protected List<ExternalTaskRelation> spiValues(CfcClientData data, String value) {
        return data.getValues(value);
    }
}
