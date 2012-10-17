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

package org.netbeans.modules.ods.tasks;

import com.tasktop.c2c.server.common.service.domain.criteria.ColumnCriteria;
import com.tasktop.c2c.server.common.service.domain.criteria.Criteria;
import com.tasktop.c2c.server.common.service.domain.criteria.CriteriaBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import junit.framework.Test;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.ods.tasks.query.QueryParameters;
import org.netbeans.modules.ods.tasks.spi.C2CData;

/**
 *
 * @author tomas
 */
public class ODSQueryTestCase extends AbstractC2CTestCase {

    public static Test suite() {
        return NbModuleSuite.emptyConfiguration()  
                .addTest(ODSQueryTestCase.class)
                .gui(false)
                .suite();
    }
        
    public ODSQueryTestCase(String arg0) {
        super(arg0);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp(); 
        System.setProperty("httpclient.wire.level", "-1");
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

    public void testEqualsQueryCriteria() throws IOException {
        IRepositoryQuery query = new RepositoryQuery(taskRepository.getConnectorKind(), ""); // NOI18N
        
        String summary = "The first unit test task";
        CriteriaBuilder cb = new CriteriaBuilder();
        cb.column(QueryParameters.Column.SUMMARY.toString(), Criteria.Operator.EQUALS, summary);
        
        query.setAttribute(C2CData.ATTR_QUERY_CRITERIA, cb.toCriteria().toQueryString());
        
        System.out.println(" Query Criteria : " + cb.toCriteria().toQueryString());
        
        Collector c = new Collector();
        IStatus status = rc.performQuery(taskRepository, query, c, null, new NullProgressMonitor());
        assertEquals("Status is OK", status.getCode(), IStatus.OK);
        assertEquals(1, c.arr.size());
        assertEquals(summary, c.arr.get(0).getRoot().getMappedAttribute(TaskAttribute.SUMMARY).getValue());
    }
    
    public void testContainsQueryCriteria() throws IOException {
        IRepositoryQuery query = new RepositoryQuery(taskRepository.getConnectorKind(), ""); // NOI18N
        
        String containsSummary = "test task";
        CriteriaBuilder cb = new CriteriaBuilder();
        cb.column(QueryParameters.Column.SUMMARY.toString(), Criteria.Operator.STRING_CONTAINS, "test task");
        
        query.setAttribute(C2CData.ATTR_QUERY_CRITERIA, cb.toCriteria().toQueryString());
        
        System.out.println(" Query Criteria : " + cb.toCriteria().toQueryString());
        
        Collector c = new Collector();
        IStatus status = rc.performQuery(taskRepository, query, c, null, new NullProgressMonitor());
        assertEquals("Status is OK", status.getCode(), IStatus.OK);
        assertEquals(2, c.arr.size());
        for (TaskData td : c.arr) {
            assertTrue(td.getRoot().getMappedAttribute(TaskAttribute.SUMMARY).getValue().contains(containsSummary));
        }
    }
    
    public void testQueryProduct() throws IOException {
        IRepositoryQuery query = new RepositoryQuery(taskRepository.getConnectorKind(), ""); // NOI18N
        
        CriteriaBuilder cb = new CriteriaBuilder();
        cb.column(QueryParameters.Column.PRODUCT.toString(), Criteria.Operator.EQUALS, TEST_PRODUCT);
        
        query.setAttribute(C2CData.ATTR_QUERY_CRITERIA, cb.toCriteria().toQueryString());
        
        System.out.println(" Query Criteria : " + cb.toCriteria().toQueryString());
        
        Collector c = new Collector();
        IStatus status = rc.performQuery(taskRepository, query, c, null, new NullProgressMonitor());
        assertEquals("Status is OK", status.getCode(), IStatus.OK);
        assertFalse(c.arr.isEmpty());
    }
    
    public void testC1AndC2() throws IOException {
        IRepositoryQuery query = new RepositoryQuery(taskRepository.getConnectorKind(), ""); // NOI18N
        
        CriteriaBuilder cb = new CriteriaBuilder();
        cb.column(QueryParameters.Column.PRODUCT.toString(), Criteria.Operator.EQUALS, TEST_PRODUCT);
        cb.and(QueryParameters.Column.COMPONENT.toString(), Criteria.Operator.EQUALS, TEST_COMPONENT2);
        
        query.setAttribute(C2CData.ATTR_QUERY_CRITERIA, cb.toCriteria().toQueryString());
        
        System.out.println(" Query Criteria : " + cb.toCriteria().toQueryString());
        
        Collector c = new Collector();
        IStatus status = rc.performQuery(taskRepository, query, c, null, new NullProgressMonitor());
        assertEquals("Status is OK", status.getCode(), IStatus.OK);
        assertEquals(1, c.arr.size());
        assertTrue(c.arr.get(0).getRoot().getMappedAttribute(TaskAttribute.COMPONENT).getValue().equals(TEST_COMPONENT2));
    }
    
    public void testC1OrC2() throws IOException {
        IRepositoryQuery query = new RepositoryQuery(taskRepository.getConnectorKind(), ""); // NOI18N
        
        CriteriaBuilder cb = new CriteriaBuilder();
        cb.column(QueryParameters.Column.COMPONENT.toString(), Criteria.Operator.EQUALS, TEST_COMPONENT2);
        cb.or(QueryParameters.Column.COMPONENT.toString(), Criteria.Operator.EQUALS, TEST_COMPONENT3);
        
        query.setAttribute(C2CData.ATTR_QUERY_CRITERIA, cb.toCriteria().toQueryString());
        
        System.out.println(" Query Criteria : " + cb.toCriteria().toQueryString());
        
        Collector c = new Collector();
        IStatus status = rc.performQuery(taskRepository, query, c, null, new NullProgressMonitor());
        assertEquals("Status is OK", status.getCode(), IStatus.OK);
        assertEquals(2, c.arr.size());
        for (TaskData td : c.arr) {
            assertTrue(td.getRoot().getMappedAttribute(TaskAttribute.COMPONENT).getValue().equals(TEST_COMPONENT2) ||
                       td.getRoot().getMappedAttribute(TaskAttribute.COMPONENT).getValue().equals(TEST_COMPONENT3));
        }
    }
    
    public void testC1And_C2OrC3() throws IOException {
        IRepositoryQuery query = new RepositoryQuery(taskRepository.getConnectorKind(), ""); // NOI18N
        
        CriteriaBuilder cb = new CriteriaBuilder();
        cb.column(QueryParameters.Column.PRODUCT.toString(), Criteria.Operator.EQUALS, TEST_PRODUCT);
        cb.and(new CriteriaBuilder()
                    .column(QueryParameters.Column.COMPONENT.toString(), Criteria.Operator.EQUALS, TEST_COMPONENT2)
                    .or(QueryParameters.Column.COMPONENT.toString(), Criteria.Operator.EQUALS, TEST_COMPONENT3)
               .toCriteria());
        
        query.setAttribute(C2CData.ATTR_QUERY_CRITERIA, cb.toCriteria().toQueryString());
        
        System.out.println(" Query Criteria : " + cb.toCriteria().toQueryString());
        
        Collector c = new Collector();
        IStatus status = rc.performQuery(taskRepository, query, c, null, new NullProgressMonitor());
        assertEquals("Status is OK", status.getCode(), IStatus.OK);
        assertEquals(2, c.arr.size());
        for (TaskData td : c.arr) {
            assertTrue(td.getRoot().getMappedAttribute(TaskAttribute.COMPONENT).getValue().equals(TEST_COMPONENT2) ||
                       td.getRoot().getMappedAttribute(TaskAttribute.COMPONENT).getValue().equals(TEST_COMPONENT3));
        }
    }

    public void testQueryByDate() throws IOException, CoreException {
        long t1 = System.currentTimeMillis();
        String summary = "summary-" + t1;
        TaskData td = createTaskData(summary, "This is the description of bug " + summary, "bug");
        
        ColumnCriteria dateGreaterThan = new ColumnCriteria(QueryParameters.Column.CREATION.toString(), Criteria.Operator.GREATER_THAN, new Date(t1 - 24 * 60 * 60 * 1000));
        ColumnCriteria dateLessThan = new ColumnCriteria(QueryParameters.Column.CREATION.toString(), Criteria.Operator.LESS_THAN, new Date(t1 + 24 * 60 * 60 * 1000));

        IRepositoryQuery query = new RepositoryQuery(taskRepository.getConnectorKind(), ""); // NOI18N
        
        CriteriaBuilder cb = new CriteriaBuilder();
        cb.result = dateGreaterThan;
        cb.and(dateLessThan);
        
        query.setAttribute(C2CData.ATTR_QUERY_CRITERIA, cb.toCriteria().toQueryString());
        
        System.out.println(" Query Criteria : " + cb.toCriteria().toQueryString());
        
        Collector c = new Collector();
        IStatus status = rc.performQuery(taskRepository, query, c, null, new NullProgressMonitor());
        assertEquals("Status is OK", status.getCode(), IStatus.OK);
        assertFalse(c.arr.isEmpty());
        for (TaskData data : c.arr) {
            if(summary.equals(data.getRoot().getMappedAttribute(C2CData.ATTR_SUMMARY).getValue())) {
                return;
            }
        }
        fail("query should return TaskData with sumary '" + summary + "'");
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
