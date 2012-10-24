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
package org.netbeans.modules.ods.tasks.spi;

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
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.openide.util.Lookup;

/** Provides access to extended methods not available on {@link AbstractRepositoryConnector}
 * but needed for connecting to C2C instance.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public abstract class C2CExtender<Data> {

    protected C2CExtender() {
    }

    public static AbstractRepositoryConnector create() {
        return getDefault().spiCreate();
    }
    public static void assignTaskRepositoryLocationFactory(AbstractRepositoryConnector rc, TaskRepositoryLocationFactory taskRepositoryLocationFactory) {
        getDefault().spiAssignTaskRepositoryLocationFactory(rc, taskRepositoryLocationFactory);
    }
    
    public static C2CData getData(AbstractRepositoryConnector rc, TaskRepository r, boolean forceRefresh) {
        return getDefault().spiClientData(rc, r, forceRefresh);
    }
        
    public static void repositoryRemoved(AbstractRepositoryConnector rc, TaskRepository r) {
        getDefault().spiRepositoryRemove(rc, r);
    }
    
    public static IRepositoryQuery getQuery(
        AbstractRepositoryConnector rc,
        PredefinedTaskQuery predefinedTaskQuery,
        String name,
        String connectorKind) {
        return getDefault().spiQuery(rc, predefinedTaskQuery, name, connectorKind);
    }

    static String getProductKey(String component, String product) {
        return getDefault().spiProductKey(component, product);
    }


    private static C2CExtender getDefault() {
        return Lookup.getDefault().lookup(C2CExtender.class);
    }

    /** For subclasses to create instance of C2CData data.
     * All methods are then called back to spiDataXYZ methods of this class.
     */
    protected final C2CData createData(Data data) {
        return C2CData.create(data, this);
    }
    
    public static void resolve(TaskData data, TaskResolution resolution) {
        getDefault().spiResolve(data, resolution);
    }
    
    //
    // implemented in separate modules
    //
    
    
    
    protected abstract AbstractRepositoryConnector spiCreate();
    protected abstract void spiAssignTaskRepositoryLocationFactory(AbstractRepositoryConnector rc, TaskRepositoryLocationFactory f);
    protected abstract C2CData spiClientData(AbstractRepositoryConnector rc, TaskRepository taskRepository, boolean forceRefresh);
    protected abstract void spiRepositoryRemove(AbstractRepositoryConnector rc, TaskRepository r);
    protected abstract IRepositoryQuery spiQuery(AbstractRepositoryConnector rc, PredefinedTaskQuery predefinedTaskQuery, String name, String connectorKind);

    protected abstract void spiResolve(TaskData data, TaskResolution resolution);
    
    
    
    //
    // operations on individual data
    // 
    
    protected abstract RepositoryConfiguration spiDataRepositoryConfiguration(Data d);
    protected abstract List<TaskStatus> spiDataStatuses(Data d);
    protected abstract List<TaskStatus> spiDataValidStatuses(Data data, TaskStatus originalStatus);
    protected abstract List<TaskResolution> spiDataValidResolutions(Data data, TaskStatus status);
    protected abstract List<TaskSeverity> spiDataSeverities(Data data);
    protected abstract List<Priority> spiDataPriorities(Data data);
    protected abstract void spiDataupdate(Data data, RepositoryConfiguration repositoryConfiguration);
    protected abstract boolean spiDataInitialized(Data data);
    protected abstract <T> T spiDataValue(Data data, String value, Class<T> type);
    protected abstract List<Milestone> spiMilestones(Data data);
    protected abstract List<TaskUserProfile> spiUsers(Data data);
    protected abstract List<Product> spiProducts(Data data);
    protected abstract List<Component> spiComponents(Data data);
    protected abstract List<TaskResolution> spiResolutions(Data data);
    protected abstract List<Milestone> spiMilestones(Data data, Product product);
    protected abstract List<Component> spiComponents(Data data, Product product);
    protected abstract TaskStatus spiStatusByValue(Data data, String value);
    protected abstract FieldDescriptor spiFieldDescriptor(Data data, TaskAttribute attribute);
    protected abstract List<FieldDescriptor> spiCustomFields(Data data);
    protected abstract Collection<Keyword> spiKeywords(Data data);
    protected abstract Collection<String> spiTaskTypes(Data data);
    protected abstract Collection<String> spiAllIterations(Data data);
    protected abstract Collection<String> spiActiveIterations(Data data);
    protected abstract List<ExternalTaskRelation> spiValues(Data data, String value);
    protected abstract String spiProductKey(String component, String product);
}
