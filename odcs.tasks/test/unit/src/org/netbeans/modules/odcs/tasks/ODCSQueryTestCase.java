/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.odcs.tasks;

import com.tasktop.c2c.server.common.service.domain.criteria.ColumnCriteria;
import com.tasktop.c2c.server.common.service.domain.criteria.Criteria;
import com.tasktop.c2c.server.common.service.domain.criteria.CriteriaBuilder;
import com.tasktop.c2c.server.tasks.domain.PredefinedTaskQuery;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.SavedTaskQuery;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import static junit.framework.Assert.assertEquals;
import junit.framework.Test;
import oracle.eclipse.tools.cloud.dev.tasks.CloudDevConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.odcs.tasks.query.QueryParameters;

/**
 *
 * @author tomas
 */
public class ODCSQueryTestCase extends AbstractODCSTestCase {

    private static final String UNIT_TEST_QUERY = "UnitTestQuery";
    private static final String UNIT_TEST_SUMMARY = "The first unit test task";
    
    public static Test suite() {
        return NbModuleSuite.emptyConfiguration()  
                .addTest(ODCSQueryTestCase.class)
                .gui(false)
                .suite();
    }
        
    public ODCSQueryTestCase(String arg0) {
        super(arg0);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp(); 
//        System.setProperty("httpclient.wire.level", "-1");
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

//    public void testQuery() throws Throwable {
//        
//        System.out.println(" ----------- query 1 ---------------- ");
//        IRepositoryQuery query = new RepositoryQuery(taskRepository.getConnectorKind(), "");            // NOI18N
//        query.setAttribute(C2CData.ATTR_TASK_TYPE, "Defect,Feature");
//        query.setAttribute("task.common.summary", "test");
//        query.setAttribute("task.common.description", "test");
//        TaskDataCollector collector = new TaskDataCollector() {
//            @Override
//            public void accept(TaskData td) {
//                System.out.println(" issue " + td.getTaskId() + " " + td.getRoot().getMappedAttribute(TaskAttribute.SUMMARY).getValue());
//            }
//        };
//        IStatus status = rc.performQuery(taskRepository, query, collector, null, new NullProgressMonitor());
//        System.out.println("status " + status);
//        System.out.println(" ----------- query 2 ---------------- ");
//        System.out.println(" ++++ Query URL : " + query.getUrl());
//        
//        query = new RepositoryQuery(taskRepository.getConnectorKind(), "");            // NOI18N
//        query.setAttribute(C2CData.ATTR_TASK_TYPE, "Defect,Feature");
//        query.setAttribute("task.common.summary", "test");
//        query.setAttribute("task.common.description", "test");
//        
//        status = rc.performQuery(taskRepository, query, collector, null, new NullProgressMonitor());
//        
//        System.out.println(" ++++ Query URL : " + query.getUrl());        
//        
//        System.out.println(" ----------- predefined query  ---------------- ");
//        
//        for (PredefinedTaskQuery predefinedId : PredefinedTaskQuery.values()) {
//            IRepositoryQuery q = C2CExtender.getQuery(rc, predefinedId, predefinedId.getLabel(), rc.getConnectorKind());
//            status = rc.performQuery(taskRepository, q, collector, null, new NullProgressMonitor());
//        }
//    }

    public void testSavedQueries() throws IOException, CoreException {
        RepositoryConfiguration conf = rc.getCloudDevClientManager().getCloudDevClient(taskRepository).getRepositoryConfiguration(true, nullProgressMonitor);
        List<SavedTaskQuery> l = conf.getSavedTaskQueries();
        assertNotNull(l);
        assertFalse(l.isEmpty());
        
        SavedTaskQuery stq = null;
        for (SavedTaskQuery q : l) {
            if(q.getName().equals(UNIT_TEST_QUERY)) {
                stq = q;
            }
        }
        assertNotNull(stq);
        
        IRepositoryQuery query = new RepositoryQuery(taskRepository.getConnectorKind(), stq.getName());
        query.setAttribute(CloudDevConstants.QUERY_CRITERIA, stq.getQueryString());
        query.setUrl(CloudDevConstants.CRITERIA_QUERY);
        
        Collector c = new Collector();
        IStatus status = rc.performQuery(taskRepository, query, c, null, new NullProgressMonitor());
        assertEquals("Status not OK", IStatus.OK, status.getCode());
        assertEquals(1, c.arr.size());
        assertEquals(UNIT_TEST_SUMMARY, c.arr.get(0).getRoot().getMappedAttribute(TaskAttribute.SUMMARY).getValue());
    }
    
    public void testPredefinedQueries() throws IOException {
        IRepositoryQuery query = new RepositoryQuery(taskRepository.getConnectorKind(), PredefinedTaskQuery.RECENT.toString());
        query.setUrl(CloudDevConstants.PREDEFINED_QUERY);
        query.setAttribute(CloudDevConstants.QUERY_NAME, PredefinedTaskQuery.RECENT.toString());
        
        Collector c = new Collector();
        IStatus status = rc.performQuery(taskRepository, query, c, null, new NullProgressMonitor());
        assertEquals("Status not OK", IStatus.OK, status.getCode());
        assertFalse(c.arr.isEmpty());
    }
    
    public void testEqualsQueryCriteria() throws IOException, CoreException {
        IRepositoryQuery query = new RepositoryQuery(taskRepository.getConnectorKind(), ""); // NOI18N
        
        String summary = UNIT_TEST_SUMMARY;
        CriteriaBuilder cb = new CriteriaBuilder();
        cb.column(QueryParameters.Column.SUMMARY.toString(), Criteria.Operator.EQUALS, summary);
        
        String crit = getCriteriaString(cb);
        query.setAttribute(CloudDevConstants.QUERY_CRITERIA, crit);
        query.setUrl(CloudDevConstants.CRITERIA_QUERY);
        System.out.println(" Query Criteria : " + crit);
        
        Collector c = new Collector();
        IStatus status = rc.performQuery(taskRepository, query, c, null, new NullProgressMonitor());
        assertEquals("Status not OK", IStatus.OK, status.getCode());
        assertEquals(1, c.arr.size());
        assertEquals(summary, c.arr.get(0).getRoot().getMappedAttribute(TaskAttribute.SUMMARY).getValue());
    }
    
    public void testContainsQueryCriteria() throws IOException, CoreException {
        IRepositoryQuery query = new RepositoryQuery(taskRepository.getConnectorKind(), ""); // NOI18N
        
        String containsSummary = "test task";
        CriteriaBuilder cb = new CriteriaBuilder();
        cb.column(QueryParameters.Column.SUMMARY.toString(), Criteria.Operator.STRING_CONTAINS, "test task");
        
        String crit = getCriteriaString(cb);
        query.setAttribute(CloudDevConstants.QUERY_CRITERIA, crit);
        query.setUrl(CloudDevConstants.CRITERIA_QUERY);
        System.out.println(" Query Criteria : " + crit);
        
        Collector c = new Collector();
        IStatus status = rc.performQuery(taskRepository, query, c, null, new NullProgressMonitor());
        assertEquals("Status not OK", status.getCode(), IStatus.OK);
        assertTrue(c.arr.size() >= 2);
        for (TaskData td : c.arr) {
            assertTrue(td.getRoot().getMappedAttribute(TaskAttribute.SUMMARY).getValue().contains(containsSummary));
        }
    }
    
    public void testQueryProduct() throws IOException, CoreException {
        IRepositoryQuery query = new RepositoryQuery(taskRepository.getConnectorKind(), ""); // NOI18N
        
        CriteriaBuilder cb = new CriteriaBuilder();
        cb.column(QueryParameters.Column.PRODUCT.toString(), Criteria.Operator.EQUALS, TEST_PRODUCT);
        
        String crit = getCriteriaString(cb);
        query.setAttribute(CloudDevConstants.QUERY_CRITERIA, crit);
        query.setUrl(CloudDevConstants.CRITERIA_QUERY);
        System.out.println(" Query Criteria : " + crit);
        
        Collector c = new Collector();
        IStatus status = rc.performQuery(taskRepository, query, c, null, new NullProgressMonitor());
        assertEquals("Status not OK", status.getCode(), IStatus.OK);
        assertFalse(c.arr.isEmpty());
    }
    
    public void testC1AndC2() throws IOException, CoreException {
        IRepositoryQuery query = new RepositoryQuery(taskRepository.getConnectorKind(), ""); // NOI18N
        
        CriteriaBuilder cb = new CriteriaBuilder();
        cb.column(QueryParameters.Column.PRODUCT.toString(), Criteria.Operator.EQUALS, TEST_PRODUCT);
        cb.and(QueryParameters.Column.COMPONENT.toString(), Criteria.Operator.EQUALS, TEST_COMPONENT2);
        
        String crit = getCriteriaString(cb);
        query.setAttribute(CloudDevConstants.QUERY_CRITERIA, crit);
        query.setUrl(CloudDevConstants.CRITERIA_QUERY);
        System.out.println(" Query Criteria : " + crit);
        
        Collector c = new Collector();
        IStatus status = rc.performQuery(taskRepository, query, c, null, new NullProgressMonitor());
        assertEquals("Status not OK", status.getCode(), IStatus.OK);
        assertEquals(1, c.arr.size());
        assertTrue(c.arr.get(0).getRoot().getMappedAttribute(TaskAttribute.COMPONENT).getValue().equals(TEST_COMPONENT2));
    }
    
    public void testC1OrC2() throws IOException, CoreException {
        IRepositoryQuery query = new RepositoryQuery(taskRepository.getConnectorKind(), ""); // NOI18N
        
        CriteriaBuilder cb = new CriteriaBuilder();
        cb.column(QueryParameters.Column.COMPONENT.toString(), Criteria.Operator.EQUALS, TEST_COMPONENT2);
        cb.or(QueryParameters.Column.COMPONENT.toString(), Criteria.Operator.EQUALS, TEST_COMPONENT3);
        
        String crit = getCriteriaString(cb);
        query.setAttribute(CloudDevConstants.QUERY_CRITERIA, crit);
        query.setUrl(CloudDevConstants.CRITERIA_QUERY);
        System.out.println(" Query Criteria : " + crit);
        
        Collector c = new Collector();
        IStatus status = rc.performQuery(taskRepository, query, c, null, new NullProgressMonitor());
        assertEquals("Status not OK", status.getCode(), IStatus.OK);
        assertEquals(3, c.arr.size());
        for (TaskData td : c.arr) {
            assertTrue(td.getRoot().getMappedAttribute(TaskAttribute.COMPONENT).getValue().equals(TEST_COMPONENT2) ||
                       td.getRoot().getMappedAttribute(TaskAttribute.COMPONENT).getValue().equals(TEST_COMPONENT3));
        }
    }
    
    public void testC1And_C2OrC3() throws IOException, CoreException {
        IRepositoryQuery query = new RepositoryQuery(taskRepository.getConnectorKind(), ""); // NOI18N
        
        CriteriaBuilder cb = new CriteriaBuilder();
        cb.column(QueryParameters.Column.PRODUCT.toString(), Criteria.Operator.EQUALS, TEST_PRODUCT);
        cb.and(new CriteriaBuilder()
                    .column(QueryParameters.Column.COMPONENT.toString(), Criteria.Operator.EQUALS, TEST_COMPONENT2)
                    .or(QueryParameters.Column.COMPONENT.toString(), Criteria.Operator.EQUALS, TEST_COMPONENT3)
               .toCriteria());
        
        String crit = getCriteriaString(cb);
        query.setAttribute(CloudDevConstants.QUERY_CRITERIA, crit);
        query.setUrl(CloudDevConstants.CRITERIA_QUERY);
        System.out.println(" Query Criteria : " + crit);
        
        Collector c = new Collector();
        IStatus status = rc.performQuery(taskRepository, query, c, null, new NullProgressMonitor());
        assertEquals("Status not OK", status.getCode(), IStatus.OK);
        assertEquals(3, c.arr.size());
        for (TaskData td : c.arr) {
            assertTrue(td.getRoot().getMappedAttribute(TaskAttribute.COMPONENT).getValue().equals(TEST_COMPONENT2) ||
                       td.getRoot().getMappedAttribute(TaskAttribute.COMPONENT).getValue().equals(TEST_COMPONENT3));
        }
    }

    public void testGetManyTasks() throws IOException, CoreException {
        IRepositoryQuery query = new RepositoryQuery(taskRepository.getConnectorKind(), PredefinedTaskQuery.RECENT.toString());
        query.setUrl(CloudDevConstants.PREDEFINED_QUERY);
        query.setAttribute(CloudDevConstants.QUERY_NAME, PredefinedTaskQuery.RECENT.toString());
        
        Collector c = new Collector();
        IStatus status = rc.performQuery(taskRepository, query, c, null, new NullProgressMonitor());
        assertEquals("Status not OK", IStatus.OK, status.getCode());
        assertTrue(c.arr.size() > 50);
    }    
    
    public void testQueryByDate() throws IOException, CoreException {
        long t1 = System.currentTimeMillis();
        String summary = "summary-" + t1;
        TaskData td = createTaskData(summary, "This is the description of bug " + summary, "bug");
        String id = td.getTaskId();
        
        Date d = new Date(t1);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE,      0);
        cal.set(Calendar.SECOND,      0);
        cal.set(Calendar.MILLISECOND, 0);
   
        Date dateGreater = new Date(cal.getTimeInMillis() - (25* 60 * 60 * 1000));
        Date dateLess = new Date(cal.getTimeInMillis() + (25 * 60 * 60 * 1000));
        ColumnCriteria cGreaterThan = new ColumnCriteria(QueryParameters.Column.CREATION.toString(), Criteria.Operator.GREATER_THAN, dateGreater);
        ColumnCriteria cLessThan = new ColumnCriteria(QueryParameters.Column.CREATION.toString(), Criteria.Operator.LESS_THAN, dateLess);
        
        IRepositoryQuery query = new RepositoryQuery(taskRepository.getConnectorKind(), ""); // NOI18N
        
        CriteriaBuilder cb = new CriteriaBuilder();
        cb.result = cGreaterThan;
        cb.and(cLessThan);
         
        String crit = getCriteriaString(cb);
        query.setAttribute(CloudDevConstants.QUERY_CRITERIA, crit);
        query.setUrl(CloudDevConstants.CRITERIA_QUERY);
        System.out.println("==============================================");
        System.out.println(" Query Criteria : " + crit);
        System.out.println(" dateGreater    : " + dateGreater);
        System.out.println(" dateLess       : " + dateLess);
        System.out.println("==============================================");
        
        Collector c = new Collector();
        IStatus status = rc.performQuery(taskRepository, query, c, null, new NullProgressMonitor());
        assertEquals("Status not OK", status.getCode(), IStatus.OK);
        assertFalse(c.arr.isEmpty());
        System.out.println(" ret size : " + c.arr.size());
        System.out.println("==============================================");
        for (TaskData data : c.arr) {
            if(id.equals(data.getTaskId())) {
                return;
            }
        }
        fail("query should return TaskData with sumary '" + summary + "'");
    }    
    
    private String getCriteriaString(CriteriaBuilder cb) throws IOException, CoreException {
        return cb.toCriteria().toQueryString(); // serializeAsString(cb.toCriteria());
    }
    
    private class Collector extends TaskDataCollector {
        List<TaskData> arr = new ArrayList<TaskData>();
        @Override
        public void accept(TaskData td) {
            arr.add(td);
            System.out.println(" task data: " + td.getTaskId() + " " + td.getRoot().getMappedAttribute(TaskAttribute.SUMMARY).getValue());
        }
    }
}
