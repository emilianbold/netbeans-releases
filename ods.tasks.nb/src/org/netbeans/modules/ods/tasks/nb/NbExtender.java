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
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.netbeans.modules.ods.tasks.spi.C2CData;
import org.netbeans.modules.ods.tasks.spi.C2CExtender;
import org.openide.util.lookup.ServiceProvider;

/** NetBeans implementation of the issue connector to Oracle Developer Cloud.
 * Has position 1000 to take precedence over the TaskTop proprietary one.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ServiceProvider(service=C2CExtender.class, position=1000)
public final class NbExtender extends C2CExtender<NbExtender.Env> {
    static final String ATTR_PREDEFINED_TASK_QUERY = "PredefinedTaskQuery"; //NOI18N

    @Override
    protected AbstractRepositoryConnector spiCreate() {
        return new NbRepositoryConnector();
    }

    @Override
    protected void spiAssignTaskRepositoryLocationFactory(AbstractRepositoryConnector rc, TaskRepositoryLocationFactory f) {
        NbRepositoryConnector nbc = (NbRepositoryConnector)rc;
        nbc.setTaskRepositoryLocationFactory(f);
    }

    @Override
    protected C2CData spiClientData(AbstractRepositoryConnector rc, TaskRepository taskRepository) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void spiRepositoryRemove(AbstractRepositoryConnector rc, TaskRepository r) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected IRepositoryQuery spiQuery(AbstractRepositoryConnector rc, PredefinedTaskQuery predefinedTaskQuery, String name, String connectorKind) {
        RepositoryQuery q = new RepositoryQuery(connectorKind, name);
        q.setAttribute(ATTR_PREDEFINED_TASK_QUERY, predefinedTaskQuery.name());
        return q;
    }

    @Override
    protected RepositoryConfiguration spiDataRepositoryConfiguration(Env d) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected List<TaskStatus> spiDataStatuses(Env d) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected List<TaskStatus> spiDataValidStatuses(Env data, TaskStatus originalStatus) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected List<TaskResolution> spiDataValidResolutions(Env data, TaskStatus status) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected List<TaskSeverity> spiDataSeverities(Env data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected List<Priority> spiDataPriorities(Env data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void spiDataupdate(Env data, RepositoryConfiguration repositoryConfiguration) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected boolean spiDataInitialized(Env data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected <T> T spiDataValue(Env data, String value, Class<T> type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected List<Milestone> spiMilestones(Env data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected List<TaskUserProfile> spiUsers(Env data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected List<Product> spiProducts(Env data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected List<Component> spiComponents(Env data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected List<TaskResolution> spiResolutions(Env data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected List<Milestone> spiMilestones(Env data, Product product) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected List<Component> spiComponents(Env data, Product product) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected TaskStatus spiStatusByValue(Env data, String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected FieldDescriptor spiFieldDescriptor(Env data, TaskAttribute attribute) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected List<FieldDescriptor> spiCustomFields(Env data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected Collection<Keyword> spiKeywords(Env data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected Collection<String> spiTaskTypes(Env data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected Collection<String> spiAllIterations(Env data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected Collection<String> spiActiveIterations(Env data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected List<ExternalTaskRelation> spiValues(Env data, String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected String spiProductKey(String component, String product) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    protected static final class Env {
        
    }
}
