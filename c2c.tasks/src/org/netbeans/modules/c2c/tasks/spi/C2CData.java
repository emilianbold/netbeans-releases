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
package org.netbeans.modules.c2c.tasks.spi;

import com.tasktop.c2c.internal.client.tasks.core.client.CfcClientData;
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
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class C2CData implements Serializable {
    private final CfcClientData delegate;

    C2CData(CfcClientData clientData) {
        delegate = clientData;
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
        return com.tasktop.c2c.internal.client.tasks.core.client.CfcClientData.getProductKey(component, product);
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

    public boolean equals(Object o) {
        Object target = o;
        if (o instanceof C2CData) {
            target = ((C2CData) o).delegate;
        }
        return this.delegate.equals(target);
    }

    public int hashCode() {
        return this.delegate.hashCode();
    }
    
}
