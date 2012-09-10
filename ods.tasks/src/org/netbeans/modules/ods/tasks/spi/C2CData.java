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
import com.tasktop.c2c.server.tasks.domain.Priority;
import com.tasktop.c2c.server.tasks.domain.Product;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.TaskResolution;
import com.tasktop.c2c.server.tasks.domain.TaskSeverity;
import com.tasktop.c2c.server.tasks.domain.TaskStatus;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;
import java.util.Collection;
import java.util.List;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class C2CData {
    public static final String ATTR_OWNER = "task.common.user.assigned";
    public static final String ATTR_PARENT = "blocks";
    public static final String ATTR_SUBTASK = "dependson";
    public static final String ATTR_MILESTONE = "milestone";
    public static final String ATTR_ITERATION = "iteration";
    public static final String ATTR_TASK_TYPE = "task_type";
    public static final String ATTR_DUPLICATE_OF = "duplicate_of";
    public static final String ATTR_DUEDATE = "task.common.date.due";
    public static final String ATTR_FOUND_IN_RELEASE = "softwareVersion";
    public static final String ATTR_TAGS = "keywords";
    public static final String ATTR_EXTERNAL_LINKS = "task_relations";
    public static final String ATTR_ESTIMATE_WITH_UNITS = "estimate_with_units";
    public static final String ATTR_REPORTER = "task.common.user.reporter";
    public static final String ATTR_CC = "cc";
    public static final String ATTR_NEWCC = "newcc";
    public static final String ATTR_NEWCOMMENT = "task.common.comment.new";
    public static final String ATTR_MODIFIED = "task.common.date.modified";
    public static final String ATTR_VERSION = "version";
    
    public static final String CUSTOM_FIELD_PREFIX = "cf.";
    
    
    private final Impl<?> delegate;

    static <Data> C2CData create(Data d, C2CExtender<Data> e) {
        return new C2CData(new Impl<Data>(d, e));
    }

    C2CData(Impl<?> delegate) {
        this.delegate = delegate;
    }
    
    public RepositoryConfiguration getRepositoryConfiguration() {
        return delegate.getRepositoryConfiguration();
    }

    public synchronized List<TaskStatus> getStatuses() {
        return delegate.getStatuses();
    }

    public synchronized List<TaskStatus> computeValidStatuses(TaskStatus originalStatus) {
        return delegate.computeValidStatuses(originalStatus);
    }

    public synchronized List<TaskResolution> computeValidResolutions(TaskStatus status) {
        return delegate.computeValidResolutions(status);
    }

    public synchronized List<TaskSeverity> getSeverities() {
        return delegate.getSeverities();
    }

    public synchronized List<Priority> getPriorities() {
        return delegate.getPriorities();
    }

    public synchronized void update(RepositoryConfiguration repositoryConfiguration) {
        delegate.update(repositoryConfiguration);
    }

    public synchronized boolean isInitialized() {
        return delegate.isInitialized();
    }

    public synchronized <T> T getValue(String value, Class<T> type) {
        return delegate.<T>getValue(value, type);
    }

    public static String getProductKey(String component, String product) {
        return C2CExtender.getProductKey(component, product);
    }

    public List<Milestone> getMilestones() {
        return delegate.getMilestones();
    }

    public List<TaskUserProfile> getUsers() {
        return delegate.getUsers();
    }

    public List<Product> getProducts() {
        return delegate.getProducts();
    }

    public List<Component> getComponents() {
        return delegate.getComponents();
    }

    public List<TaskResolution> getResolutions() {
        return delegate.getResolutions();
    }

    public List<Milestone> getMilestones(Product product) {
        return delegate.getMilestones(product);
    }

    public List<Component> getComponents(Product product) {
        return delegate.getComponents(product);
    }

    public TaskStatus getStatusByValue(String value) {
        return delegate.getStatusByValue(value);
    }

    public FieldDescriptor getFieldDescriptor(TaskAttribute attribute) {
        return delegate.getFieldDescriptor(attribute);
    }

    public List<FieldDescriptor> getCustomFields() {
        return delegate.getCustomFields();
    }

    public Collection<Keyword> getKeywords() {
        return delegate.getKeywords();
    }

    public Collection<String> getTaskTypes() {
        return delegate.getTaskTypes();
    }

    public Collection<String> getAllIterations() {
        return delegate.getAllIterations();
    }

    public Collection<String> getActiveIterations() {
        return delegate.getActiveIterations();
    }

    public List<ExternalTaskRelation> getValues(String value) {
        return delegate.getValues(value);
    }

    @Override
    public boolean equals(Object o) {
        Object target = o;
        if (o instanceof C2CData) {
            target = ((C2CData) o).delegate;
        }
        return this.delegate.equals(target);
    }

    @Override
    public int hashCode() {
        return this.delegate.hashCode();
    }

    private static class Impl<Data> {
        private final Data data;
        private final C2CExtender<Data> extender;
        
        public Impl(Data d, C2CExtender<Data> e) {
            this.data = d;
            this.extender = e;
        }
        
        public RepositoryConfiguration getRepositoryConfiguration() {
            return extender.spiDataRepositoryConfiguration(data);
        }

        public List<TaskStatus> getStatuses() {
            return extender.spiDataStatuses(data);
        }

        public List<TaskStatus> computeValidStatuses(TaskStatus originalStatus) {
            return extender.spiDataValidStatuses(data, originalStatus);
        }

        public List<TaskResolution> computeValidResolutions(TaskStatus status) {
            return extender.spiDataValidResolutions(data, status);
        }

        public List<TaskSeverity> getSeverities() {
            return extender.spiDataSeverities(data);
        }

        public List<Priority> getPriorities() {
            return extender.spiDataPriorities(data);
        }

        public void update(RepositoryConfiguration repositoryConfiguration) {
            extender.spiDataupdate(data, repositoryConfiguration);
        }

        public boolean isInitialized() {
            return extender.spiDataInitialized(data);
        }

        public <T> T getValue(String value, Class<T> type) {
            return extender.<T>spiDataValue(data, value, type);
        }

        public List<Milestone> getMilestones() {
            return extender.spiMilestones(data);
        }

        public List<TaskUserProfile> getUsers() {
            return extender.spiUsers(data);
        }

        public List<Product> getProducts() {
            return extender.spiProducts(data);
        }

        public List<Component> getComponents() {
            return extender.spiComponents(data);
        }

        public List<TaskResolution> getResolutions() {
            return extender.spiResolutions(data);
        }

        public List<Milestone> getMilestones(Product product) {
            return extender.spiMilestones(data, product);
        }

        public List<Component> getComponents(Product product) {
            return extender.spiComponents(data, product);
        }

        public TaskStatus getStatusByValue(String value) {
            return extender.spiStatusByValue(data, value);
        }

        public FieldDescriptor getFieldDescriptor(TaskAttribute attribute) {
            return extender.spiFieldDescriptor(data, attribute);
        }

        public List<FieldDescriptor> getCustomFields() {
            return extender.spiCustomFields(data);
        }

        public Collection<Keyword> getKeywords() {
            return extender.spiKeywords(data);
        }

        public Collection<String> getTaskTypes() {
            return extender.spiTaskTypes(data);
        }

        public Collection<String> getAllIterations() {
            return extender.spiAllIterations(data);
        }

        public Collection<String> getActiveIterations() {
            return extender.spiActiveIterations(data);
        }

        public List<ExternalTaskRelation> getValues(String value) {
            return extender.spiValues(data, value);
        }

        @Override
        public boolean equals(Object o) {
            Object target = o;
            if (o instanceof Impl) {
                Impl i = (Impl)o;
                return extender == i.extender && data.equals(i.data);
            }
            return data.equals(target);
        }

        @Override
        public int hashCode() {
            return data.hashCode();
        }
        
    }
    
}
