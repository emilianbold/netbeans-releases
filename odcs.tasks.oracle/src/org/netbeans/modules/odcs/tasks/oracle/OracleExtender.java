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
package org.netbeans.modules.odcs.tasks.oracle;

import com.tasktop.c2c.server.tasks.domain.Component;
import com.tasktop.c2c.server.tasks.domain.ExternalTaskRelation;
import com.tasktop.c2c.server.tasks.domain.FieldDescriptor;
import com.tasktop.c2c.server.tasks.domain.Iteration;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import oracle.eclipse.tools.cloud.dev.tasks.CloudDevAttribute;
import oracle.eclipse.tools.cloud.dev.tasks.CloudDevClient;
import oracle.eclipse.tools.cloud.dev.tasks.CloudDevRepositoryConnector;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.netbeans.modules.odcs.tasks.spi.C2CData;
import org.netbeans.modules.odcs.tasks.spi.C2CExtender;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/** Implementation of the extender based on C2C internal data.
 */
@ServiceProvider(service=C2CExtender.class, position=99999)
public final class OracleExtender extends C2CExtender<RepositoryConfiguration> {
    public OracleExtender() {
    }
    
    @Override
    protected AbstractRepositoryConnector spiCreate() {
        return new CloudDevRepositoryConnector();
    }

    @Override
    protected void spiAssignTaskRepositoryLocationFactory(AbstractRepositoryConnector rc, TaskRepositoryLocationFactory taskRepositoryLocationFactory) {
        CloudDevRepositoryConnector cfc = (CloudDevRepositoryConnector)rc;
        // XXX is there an equivalent? cfc.getClientManager().setLocationFactory(taskRepositoryLocationFactory);
    }
    
    @Override
    protected synchronized C2CData spiClientData(AbstractRepositoryConnector rc, TaskRepository taskRepository, boolean forceRefresh) {
        CloudDevRepositoryConnector cfc = (CloudDevRepositoryConnector)rc;
        CloudDevClient client = cfc.getCloudDevClient(taskRepository);
        RepositoryConfiguration clientData;
        try {
            clientData = client.getRepositoryConfiguration(forceRefresh, new NullProgressMonitor());
        } catch (CoreException ex) {
            // XXX
            Exceptions.printStackTrace(ex);
            return null;
            
        }

        // XXX is there an equivalent? 
//        if (forceRefresh || !clientData.isInitialized()) {
////            try {
//                client.updateRepositoryConfiguration(new NullProgressMonitor());
////            } catch (CoreException ex) {
////                // XXX
////                Exceptions.printStackTrace(ex);
////            }
//        }
        return createData(clientData);
    }

    @Override
    protected void spiRepositoryRemove(AbstractRepositoryConnector rc, TaskRepository r) {
        CloudDevRepositoryConnector cfc = (CloudDevRepositoryConnector)rc;
//      XXX is there an equivalent?   cfc.getClientManager().repositoryRemoved(r);
    }

    @Override
    protected IRepositoryQuery spiQuery(
        AbstractRepositoryConnector rc,
        PredefinedTaskQuery predefinedTaskQuery,
        String name, String connectorKind
    ) {
        RepositoryQuery q = new RepositoryQuery(connectorKind, name);
        q.setUrl(C2CData.PREDEFINED_QUERY);
        q.setAttribute(C2CData.QUERY_NAME, predefinedTaskQuery.toString());        
        return q;
    }

    @Override
    protected String spiProductKey(String component, String product) {
//        return RepositoryConfiguration.getProductKey(component, product);
        throw new UnsupportedOperationException("not implemented yet");
    }

    private Map<RepositoryConfiguration, RepositoryConfiguration> confs = new WeakHashMap<RepositoryConfiguration, RepositoryConfiguration>();
    @Override
    protected RepositoryConfiguration spiDataRepositoryConfiguration(RepositoryConfiguration rc) {
//        RepositoryConfiguration rc = confs.get(d);
//        if(rc == null) {
//            rc = new ExtenderRepositoryConfiguration(d);
//        }
        return rc;
    }

    @Override
    protected List<TaskStatus> spiDataStatuses(RepositoryConfiguration d) {
        return d.getStatuses();
    }

    @Override
    protected List<TaskStatus> spiDataValidStatuses(RepositoryConfiguration data, TaskStatus originalStatus) {
        return data.computeValidStatuses(originalStatus);
    }

    @Override
    protected List<TaskResolution> spiDataValidResolutions(RepositoryConfiguration data, TaskStatus status) {
        return data.computeValidResolutions(status);
    }

    @Override
    protected List<TaskSeverity> spiDataSeverities(RepositoryConfiguration data) {
        return data.getSeverities();
    }

    @Override
    protected List<Priority> spiDataPriorities(RepositoryConfiguration data) {
        return data.getPriorities();
    }

    @Override
    protected void spiDataupdate(RepositoryConfiguration data, RepositoryConfiguration repositoryConfiguration) {
//        data.update(repositoryConfiguration);
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    protected boolean spiDataInitialized(RepositoryConfiguration data) {
//        return data.isInitialized();
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    protected <T> T spiDataValue(RepositoryConfiguration data, String value, Class<T> type) {
//        return data.getValue(value, type);
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    protected List<Milestone> spiMilestones(RepositoryConfiguration data) {
        return data.getMilestones();
    }

    @Override
    protected List<TaskUserProfile> spiUsers(RepositoryConfiguration data) {
        return data.getUsers();
    }

    @Override
    protected List<Product> spiProducts(RepositoryConfiguration data) {
        return data.getProducts();
    }

    @Override
    protected List<Component> spiComponents(RepositoryConfiguration data) {
        return data.getComponents();
    }

    @Override
    protected List<TaskResolution> spiResolutions(RepositoryConfiguration data) {
        return data.getResolutions();
    }

    @Override
    protected List<Milestone> spiMilestones(RepositoryConfiguration data, Product product) {
        return data.getMilestones(product);
    }

    @Override
    protected List<Component> spiComponents(RepositoryConfiguration data, Product product) {
        return data.getComponents(product);
    }

    @Override
    protected TaskStatus spiStatusByValue(RepositoryConfiguration data, String value) {
        List<TaskStatus> statuses = data.getStatuses();
        for (TaskStatus taskStatus : statuses) {
            if(value.equals(taskStatus.getValue())) {
                return taskStatus;
            }
        }
        return null;
    }

    @Override
    protected FieldDescriptor spiFieldDescriptor(RepositoryConfiguration data, TaskAttribute attribute) {
//        return data.getFieldDescriptor(attribute);
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    protected List<FieldDescriptor> spiCustomFields(RepositoryConfiguration data) {
        return data.getCustomFields();
    }

    @Override
    protected Collection<Keyword> spiKeywords(RepositoryConfiguration data) {
        return data.getKeywords();
    }

    @Override
    protected Collection<String> spiTaskTypes(RepositoryConfiguration data) {
        return data.getTaskTypes();
    }

    @Override
    protected Collection<String> spiAllIterations(RepositoryConfiguration data) {
        return toStringCollection(data.getIterations());
    }

    @Override
    protected Collection<String> spiActiveIterations(RepositoryConfiguration data) {
        return toStringCollection(data.getActiveIterations());
    }

    @Override
    protected List<ExternalTaskRelation> spiValues(RepositoryConfiguration data, String value) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    protected void spiResolve(TaskData data, TaskResolution resolution) {
        TaskAttribute rta = data.getRoot();
        TaskAttribute ta = rta.getMappedAttribute(CloudDevAttribute.STATUS.getName());
        ta.setValue("RESOLVED");
        ta = rta.getMappedAttribute(CloudDevAttribute.RESOLUTION.getName());
        ta.setValue(resolution.getValue());
    }
    
    private Collection<String> toStringCollection(Collection<Iteration> c) {
        List<String> l = new ArrayList<String>();
        for (Iteration i : c) {
            if(i != null) {
                l.add(i.getValue());
            }
        }
        return l;
    }

}
