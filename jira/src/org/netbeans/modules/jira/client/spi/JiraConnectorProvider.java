/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.jira.client.spi;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.netbeans.modules.mylyn.util.RepositoryConnectorProvider;
import org.openide.util.Lookup;

/**
 *
 * @author Tomas Stupka
 */
public abstract class JiraConnectorProvider {

    protected static final Logger LOG = Logger.getLogger(JiraConnectorProvider.class.getName());

    public enum Type {
        XMLRPC("org.netbeans.modules.jira.xmlrpc", "JIRA XML-RPC"),
        REST("org.netbeans.modules.jira.rest", "JIRA REST");
        private final String cnb;
        private final String displayName;
        private Type(String cnb, String displayName) {
            this.cnb = cnb;
            this.displayName = displayName;
        }
        public String getCnb() {
            return cnb;
        }
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public interface JiraConnectorFactory {
        public JiraConnectorProvider create();
        public Type forType();
    } 

    public abstract AbstractRepositoryConnector getRepositoryConnector();
 
    public abstract JiraClient getClient(TaskRepository repo);

    public abstract void validateConnection(TaskRepository taskRepository) throws IOException;
 
    public abstract JiraConstants getJiraConstants();
    
    public abstract boolean isJiraException(Throwable t);
    
    public abstract boolean isJiraServiceUnavailableException(Throwable t);

    public abstract void setQuery(TaskRepository taskRepository, IRepositoryQuery iquery, JiraFilter fd);
    
    public abstract JiraWorkLog createWorkLog();
    public abstract JiraWorkLog createWorkLogFrom(TaskAttribute workLogTA);
    
    public abstract JiraVersion createJiraVersion(String version);
    
    public abstract ProjectFilter createProjectFilter(Project project);

    public abstract FilterDefinition createFilterDefinition();
    
    public abstract ContentFilter createContentFilter(
            String queryString, 
            boolean searchSummary, 
            boolean searchDescription,
            boolean searchEnvironment, 
            boolean searchComments);
    
    public abstract ProjectFilter createProjectFilter(Project[] toArray);

    public abstract UserFilter createNobodyFilter();

    public abstract UserFilter createCurrentUserFilter();

    public abstract UserFilter createSpecificUserFilter(String text);

    public abstract UserFilter createUserInGroupFilter(String text);

    public abstract DateRangeFilter createDateRangeFilter(Date from, Date to);

    public abstract IssueTypeFilter createIssueTypeFilter(IssueType[] toArray);

    public abstract ComponentFilter createComponentFilter(Component[] toArray, boolean empty);

    public abstract VersionFilter createVersionFilter(Version[] toArray, boolean empty, boolean b, boolean b0);

    public abstract StatusFilter createStatusFilter(JiraStatus[] toArray);

    public abstract ResolutionFilter createResolutionFilter(Resolution[] toArray);

    public abstract PriorityFilter createPriorityFilter(Priority[] toArray);

    public abstract EstimateVsActualFilter createEstimateVsActualFilter(long l, long l0);
    
    public interface JiraClient {

        public NamedFilter[] getNamedFilters() throws IOException;

        public void setDateTimePattern(String value);

        public void setDatePattern(String value);

        public void setLocale(Locale locale);

        public Project getProjectById(String id);

        public Project getProjectByKey(String key);
        
        public Project[] getProjects();

        public User getUser(String name);

        public JiraStatus[] getStatuses();

        public JiraStatus getStatusById(String id);

        public Priority[] getPriorities();

        public Priority getPriorityById(String id);

        public Resolution getResolutionById(String id);

        public Resolution[] getResolutions();

        public IssueType getIssueTypeById(String id);

        public IssueType[] getIssueTypes();

        public String getServerVersion();

        public int getWorkHoursPerDay();

        public int getWorkDaysPerWeek();

        public void refreshProjectDetails(String id) throws IOException;

        public boolean hasDetails();

        public void delete(String taskId) throws IOException;
        
    }
    
    protected boolean ofWrappedType(Object obj) {
        String className = obj.getClass().getName();
        return  className.equals("com.atlassian.connector.eclipse.internal.jira.core.model.Version") ||
                className.equals("com.atlassian.connector.eclipse.internal.jira.core.model.Component") ||
                className.equals("com.atlassian.connector.eclipse.internal.jira.core.model.JiraStatus") ||
                className.equals("com.atlassian.connector.eclipse.internal.jira.core.model.JiraVersion") ||
                className.equals("com.atlassian.connector.eclipse.internal.jira.core.model.Project") ||
                className.equals("com.atlassian.connector.eclipse.internal.jira.core.model.Resolution") ||
                className.equals("com.atlassian.connector.eclipse.internal.jira.core.model.IssueType") ||
                className.equals("com.atlassian.connector.eclipse.internal.jira.core.model.filter.ComponentFilter") ||
                className.equals("com.atlassian.connector.eclipse.internal.jira.core.model.filter.ContentFilter") ||
                className.equals("com.atlassian.connector.eclipse.internal.jira.core.model.filter.CurrentUserFilter") ||
                className.equals("com.atlassian.connector.eclipse.internal.jira.core.model.filter.DateFilter") ||
                className.equals("com.atlassian.connector.eclipse.internal.jira.core.model.filter.DateRangeFilter") ||
                className.equals("com.atlassian.connector.eclipse.internal.jira.core.model.filter.EstimateVsActualFilter") ||
                className.equals("com.atlassian.connector.eclipse.internal.jira.core.model.filter.FilterDefinition") ||
                className.equals("com.atlassian.connector.eclipse.internal.jira.core.model.filter.IssueTypeFilter") ||
                className.equals("com.atlassian.connector.eclipse.internal.jira.core.model.filter.PriorityFilter") ||
                className.equals("com.atlassian.connector.eclipse.internal.jira.core.model.filter.SpecificUserFilter") ||
                className.equals("com.atlassian.connector.eclipse.internal.jira.core.model.filter.NobodyFilter") ||
                className.equals("com.atlassian.connector.eclipse.internal.jira.core.model.filter.ResolutionFilter") ||
                className.equals("com.atlassian.connector.eclipse.internal.jira.core.model.filter.ProjectFilter") ||
                className.equals("com.atlassian.connector.eclipse.internal.jira.core.model.filter.StatusFilter") ||
                className.equals("com.atlassian.connector.eclipse.internal.jira.core.model.filter.UserInGroupFilter") ||
                className.equals("com.atlassian.connector.eclipse.internal.jira.core.model.filter.UserFilter") ||
                className.equals("com.atlassian.connector.eclipse.internal.jira.core.model.filter.VersionFilter") ||
                className.equals("com.atlassian.connector.eclipse.internal.jira.core.model.NamedFilter") ||
                className.equals("com.atlassian.connector.eclipse.internal.jira.core.model.JiraFilter");
    }
    
    protected <C, D> C createWrapper(Class<C> interfaceClass, final D d) {
        
        Class<C> proxyClass = (Class<C>) Proxy.getProxyClass(interfaceClass.getClassLoader(), new Class[] {interfaceClass});
        
        try {
            
            Constructor<C> c = proxyClass.getConstructor(InvocationHandler.class);
            
            Wrapper<D> w = new Wrapper<D>() {
                private final D delegate = d;
                @Override
                public D getDelegate() {
                    return delegate;
                }
            };
            
            return c.newInstance(new WrapperInvocationHandler<>(w));
            
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return null;
    }
    
    protected interface Wrapper<D> {
        D getDelegate();
    }
    
    protected <D> D getDelegate(Proxy proxy) {
        WrapperInvocationHandler<D> w = (WrapperInvocationHandler<D>) Proxy.getInvocationHandler(proxy);
        return w.wrapper.getDelegate();
    }
    
    protected class WrapperInvocationHandler<D> implements InvocationHandler {
        private final Wrapper<D> wrapper;

        public WrapperInvocationHandler(Wrapper<D> wrapper) {
            this.wrapper = wrapper;
        }
        
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                if(args != null) {
                    for (int i = 0; i < args.length; i++) {
                        args[i] = toDelegate(args[i]);
                    }
                }
                
                Method wrapperMethod = null;
                for(Method m : wrapper.getDelegate().getClass().getMethods()) {
                    if(m.getName().equals(method.getName())) {
                        if(m.getName().equals("equals")) {
                             wrapperMethod = m;
                             break;
                        }
                        Class<?>[] pts = m.getParameterTypes();
                        if(  ( args == null || args.length == 0 ) &&
                             (  pts == null ||  pts.length == 0 ) ) {
                            wrapperMethod = m;
                            break;
                        } else if(pts.length == args.length) {
                            boolean allTypes = true;
                            for (int i = 0; i < pts.length; i++) {
                                Class<?> pt = pts[i];
                                if(args[i] != null && !pt.getSimpleName().equals(args[i].getClass().getSimpleName())) {
                                    // XXX hack!!! we kind of relly on the type names being unique over all packages
                                    allTypes = false;
                                    break;
                                }
                            }
                            if(allTypes) {
                                wrapperMethod = m;
                                break;
                            }
                        }
                    }
                }
                if(wrapperMethod == null) {
                    throw new NoSuchMethodException(wrapper.getDelegate().getClass().getName() + "." + method.getName() + " " + Arrays.toString(method.getParameterTypes()));
                }
                Object ret = wrapperMethod.invoke(wrapper.getDelegate(), args);
                Class<?> ct = method.getReturnType().getComponentType();
                if(ret instanceof Object[]) {
                    Object[] objs = (Object[])ret;
                    Object ao = Array.newInstance(ct, objs.length);
                    for (int i = 0; i < objs.length; i++) {
                        Array.set(ao, i, convert(ct, objs[i]));
                    }
                    return ao;
                } else {
                    return convert(ct, ret);
                }
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }

        private Object toDelegate(Object obj) {
            if(obj instanceof Proxy) {
                return getDelegate((Proxy) obj);
            } else if(obj instanceof Object[]) {
                Object[] objs = (Object[]) obj;
                for (int j = 0; j < objs.length; j++) {
                    objs[j] = toDelegate(obj);
                }
            }
            return obj;
        }
    }    
    
    protected <C> C[] convert(Class<C> wrapperClass, Object[] proxies) {
        Object a = Array.newInstance(wrapperClass, proxies.length);
        for (int i = 0; i < proxies.length; i++) {
            Array.set(a, i, getDelegate((Proxy)proxies[i]));
        }
        return (C[]) a;
    }
    
    protected <C, D> C[] convert( Class<C> wrapperClass, C[] ret, D[] delegates ) {
        if(delegates == null) {
            return null;
        }
        for (int i = 0; i < delegates.length; i++) {
            ret[i] = createWrapper( wrapperClass, delegates[i] );
        }
        return ret;            
    }        
    
    protected <C> Object convert( Class<C> wrapperClass, Object obj ) {
        if(obj == null) {
            return null;
        }
        if( ofWrappedType(obj) ) {
            return createWrapper(wrapperClass, obj);
        }
        return obj;
    }  
}
