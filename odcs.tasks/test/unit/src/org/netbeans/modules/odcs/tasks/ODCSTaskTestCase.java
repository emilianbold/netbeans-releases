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

import com.tasktop.c2c.server.tasks.domain.Component;
import com.tasktop.c2c.server.tasks.domain.Iteration;
import com.tasktop.c2c.server.tasks.domain.Keyword;
import com.tasktop.c2c.server.tasks.domain.Milestone;
import com.tasktop.c2c.server.tasks.domain.Priority;
import com.tasktop.c2c.server.tasks.domain.Product;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.TaskSeverity;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import oracle.eclipse.tools.cloud.dev.tasks.CloudDevAttribute;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.internal.tasks.core.data.FileTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskAttachmentHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.odcs.tasks.issue.IssueField;
import org.netbeans.modules.odcs.tasks.util.ODCSUtil;

/**
 *
 * @author tomas
 */
public class ODCSTaskTestCase extends NbTestSuite {

    private static final IssueField[] SIMPLE_TEXT_FIELDS = new IssueField[] {
        IssueField.SUMMARY,
        IssueField.DESCRIPTION,
        IssueField.FOUNDIN,
    };
    private static final IssueField[] DATE_FIELDS = new IssueField[] {
        IssueField.DUEDATE
    };
    private static final IssueField[] NUMBER_FIELDS = new IssueField[] {
        IssueField.ESTIMATE
    };
            
    private static final String ATTACHMENT_DATA = "attachment data";
    
    private TaskData taskData;
    
    public ODCSTaskTestCase() throws IllegalArgumentException, IllegalAccessException {
        addTest(new CreateTaskTestCase());
        addTest(new ChangeTypeTestCase());
        addTest(new MidairTestCase());
        for (IssueField f : SIMPLE_TEXT_FIELDS) {
            addTest(new ChangeTextTestCase(escape(f.getDisplayName()), f));
        }
        for (IssueField f : DATE_FIELDS) {
            addTest(new ChangeDateTestCase(escape(f.getDisplayName()), f));
        }
        for (IssueField f : NUMBER_FIELDS) {
            addTest(new ChangeNumberTestCase(escape(f.getDisplayName()), f));
        }
        addTest(new ChangeCCTestCase());
        addTest(new ChangePriorityTestCase());
        addTest(new ChangeSeverityTestCase());
        addTest(new ChangeIterationTestCase());
        addTest(new ChangeKeywordTestCase());
        addTest(new ChangeProductComponentMilestoneTestCase());
        addTest(new ChangeCustomFieldTestCase());
        addTest(new CommentTestCase());
        addTest(new ReassignTestCase());
        addTest(new AttachementsTestCase());
        addTest(new ResolveTestCase());
        addTest(new RemoveParentFromSubtaskTestCase());
        addTest(new RemoveSubtaskFromParentTestCase());
        addTest(new DuplicateTestCase());
    }
    
    private String getDifferentUser(String user, List<TaskUserProfile> users) {
        for (TaskUserProfile tup : users) {
            if(tup.getLoginName().toLowerCase().contains(AbstractODCSTestCase.TEST_USER2)) {
                return tup.getLoginName();
            }
        }
        fail("expected to find a user with login " + AbstractODCSTestCase.TEST_USER2);
        return null;
    }

    private static void printTaskData(TaskData data) {
        ODCS.LOG.log(Level.INFO, " *************************************************** ");
        ODCS.LOG.log(Level.INFO, " id : " + data.getTaskId());
        ODCS.LOG.log(Level.INFO, "   summary : " + data.getRoot().getAttribute(TaskAttribute.SUMMARY).getValue());
        ODCS.LOG.log(Level.INFO, "   owner : " + data.getRoot().getAttribute(TaskAttribute.USER_ASSIGNED).getValue());
        ODCS.LOG.log(Level.INFO, "   status : " + data.getRoot().getAttribute(TaskAttribute.STATUS).getValue());
//        ODCS.LOG.log(Level.INFO, "   parent : " + data.getRoot().getAttribute(C2CData.ATTR_PARENT).getValue());
//        ODCS.LOG.log(Level.INFO, "   subtask : " + data.getRoot().getAttribute(C2CData.ATTR_SUBTASK).getValue());
        
        List<TaskAttribute> attrs = data.getAttributeMapper().getAttributesByType(data, TaskAttribute.TYPE_ATTACHMENT);
        ODCS.LOG.log(Level.INFO, "   attachmnets : " + (attrs != null ? attrs.size() : "null"));
    }

    private String escape(String displayName) {
        return displayName.replaceAll(" ", "_");
    }

    private abstract class TaskTestCase extends AbstractODCSTestCase {
        public TaskTestCase(String arg0) {
            super(arg0);
        }
        @Override
        protected Level logLevel() {
            return Level.ALL;
        }
        TaskData getTaskData(String id) throws CoreException {
            TaskData taskData = rc.getTaskData(taskRepository, id, nullProgressMonitor);
            assertNotNull(taskData);        
            printTaskData(taskData);            
            return taskData;
        }
    }
    
    private class CreateTaskTestCase extends TaskTestCase {
        public CreateTaskTestCase() {
            super("testTaskCreate");
        }
        @Override
        public void runTest() throws Throwable {
            taskData = createTaskData("this is my bug", "a bug", "bug");
//            taskData = getTaskData("1006");
        }
    }
    
    private abstract class ChangeTestCase extends TaskTestCase {
        private final IssueField field;
        public ChangeTestCase(String fieldName, IssueField field) {
            super("testChange" + fieldName);
            this.field = field;
        }
        @Override
        public void runTest() throws Throwable {
            // change
            TaskAttribute rta = taskData.getRoot();
            TaskAttribute ta = rta.getMappedAttribute(field.getKey());
            String newValue = getDifferentValue(ta.getValue());
            ta.setValue(newValue);

            RepositoryResponse rr = ODCSUtil.postTaskData(rc, taskRepository, taskData);
            assertEquals(RepositoryResponse.ResponseKind.TASK_UPDATED, rr.getReposonseKind());

            String taskId = rr.getTaskId();
            assertNotNull(taskId);
            taskData = getTaskData(taskData.getTaskId());
            assertNotNull(taskData);
            printTaskData(taskData); 
            rta = taskData.getRoot();
            assertEquals(newValue, rta.getMappedAttribute(field.getKey()).getValue());
        }  
        protected abstract String getDifferentValue(String value) throws CoreException;
    }
    
     private class MidairTestCase extends TaskTestCase {
        public MidairTestCase() {
            super("testMidair");
        }
        @Override
        public void runTest() throws Throwable {
            TaskData midAirTaskData1 = createTaskData("this is a bug", "a bug", "bug");
            
            // change 1
            TaskAttribute rta1 = midAirTaskData1.getRoot();
            TaskAttribute ta1 = rta1.getMappedAttribute(IssueField.SUMMARY.getKey());
            String newValue1 = ta1.getValue() + ".change1";
            ta1.setValue(newValue1);
            
            // change 2
            TaskData midAirTaskData2 = getTaskData(midAirTaskData1.getTaskId());
            TaskAttribute rta2 = midAirTaskData2.getRoot();
            TaskAttribute ta2 = rta2.getMappedAttribute(IssueField.SUMMARY.getKey());
            String newValue2 = ta2.getValue() + ".change2";
            ta2.setValue(newValue2);
            
            // post 1
            RepositoryResponse rr1 = ODCSUtil.postTaskData(rc, taskRepository, midAirTaskData1);
            assertEquals(RepositoryResponse.ResponseKind.TASK_UPDATED, rr1.getReposonseKind());

            // post 2
            Throwable catched = null;
            try {
                RepositoryResponse rr2 = ODCSUtil.postTaskData(rc, taskRepository, midAirTaskData2);
            } catch (Throwable t) {
                catched = t;
                String msg = t.getMessage().toLowerCase();
                if( msg.contains("mid-air collision") ||
                    (msg.contains("db version") && msg.contains("version provided")) ) 
                {
                    return;
                }
            }
            assertNotNull("expected a midair exception to be raised", catched);
        }  
    }
    
    private class ChangeTextTestCase extends ChangeTestCase {
        public ChangeTextTestCase(String fieldName, IssueField field) {
            super(fieldName, field);
        }
        @Override
        protected String getDifferentValue(String value) {
            return value + "-changed";
        }
    }
    
    private class ChangeDateTestCase extends ChangeTestCase {
        public ChangeDateTestCase(String fieldName, IssueField field) {
            super(fieldName, field);
        }
        @Override
        protected String getDifferentValue(String value) {
            Date d;
            if(value == null || "".equals(value.trim())) {
                d = new Date(System.currentTimeMillis());
            } else {
                d = ODCSUtil.parseLongDate(value);
            }
            return Long.toString(new Date(d.getTime() + (24 * 60 * 60 * 1000)).getTime());
        }
    }
    
    private class ChangeNumberTestCase extends ChangeTestCase {
        public ChangeNumberTestCase(String fieldName, IssueField field) {
            super(fieldName, field);
        }
        @Override
        protected String getDifferentValue(String value) {
            if(value == null || "".equals(value.trim())) { 
                return "1";
            }
            return Long.toString(Long.parseLong(value) + 1);
        }
    }
    
    private class ChangeTypeTestCase extends ChangeTestCase {
        public ChangeTypeTestCase() {
            super("Type", IssueField.TASK_TYPE);
        }

        @Override
        protected String getDifferentValue(String value) throws CoreException {
            RepositoryConfiguration clientData = rc.getCloudDevClientManager().getCloudDevClient(taskRepository).getRepositoryConfiguration(false, nullProgressMonitor);
            for (String t : clientData.getTaskTypes()) {
                if(!t.equals(value)) {
                    return t;
                }
            }
            fail();
            return null;
        }
    }
    
    private class ChangePriorityTestCase extends ChangeTestCase {
        public ChangePriorityTestCase() {
            super("Priority", IssueField.PRIORITY);
        }

        @Override
        protected String getDifferentValue(String value) throws CoreException {
            RepositoryConfiguration clientData = rc.getCloudDevClientManager().getCloudDevClient(taskRepository).getRepositoryConfiguration(false, nullProgressMonitor);
            for (Priority p : clientData.getPriorities()) {
                if(!p.getValue().equals(value)) {
                    return p.getValue();
                }
            }
            fail();
            return null;
        }
    }
    
    private class ChangeSeverityTestCase extends ChangeTestCase {
        public ChangeSeverityTestCase() {
            super("Severity", IssueField.SEVERITY);
        }

        @Override
        protected String getDifferentValue(String value) throws CoreException {
            RepositoryConfiguration clientData = rc.getCloudDevClientManager().getCloudDevClient(taskRepository).getRepositoryConfiguration(false, nullProgressMonitor);
            for (TaskSeverity s : clientData.getSeverities()) {
                if(!s.getValue().equals(value)) {
                    return s.getValue();
                }
            }
            fail();
            return null;
        }
    }
    
    private class ChangeIterationTestCase extends ChangeTestCase {
        public ChangeIterationTestCase() {
            super("Iteration", IssueField.ITERATION);
        }

        @Override
        protected String getDifferentValue(String value) throws CoreException {
            RepositoryConfiguration clientData = rc.getCloudDevClientManager().getCloudDevClient(taskRepository).getRepositoryConfiguration(false, nullProgressMonitor);
            for (Iteration i : clientData.getIterations()) {
                if(!i.getValue().equals(value)) {
                    return i.getValue();
                }
            }
            fail();
            return null;
        }
    }
    
    private class ChangeKeywordTestCase extends ChangeTestCase {
        public ChangeKeywordTestCase() {
            super("Keyword", IssueField.KEYWORDS);
        }

        @Override
        protected String getDifferentValue(String value) throws CoreException {
            RepositoryConfiguration clientData = rc.getCloudDevClientManager().getCloudDevClient(taskRepository).getRepositoryConfiguration(false, nullProgressMonitor);
            for (Keyword k : clientData.getKeywords()) {
                if(!k.getName().equals(value)) {
                    return k.getName();
                }
            }
            fail();
            return null;
        }
    }
    
    private class ChangeProductComponentMilestoneTestCase extends TaskTestCase {
        public ChangeProductComponentMilestoneTestCase() {
            super("testChangeProductComponentMilestone");
        }

        @Override
        protected void runTest() throws Throwable {
            RepositoryConfiguration clientData = rc.getCloudDevClientManager().getCloudDevClient(taskRepository).getRepositoryConfiguration(false, nullProgressMonitor);
            
            TaskAttribute rta = taskData.getRoot();
            TaskAttribute ta = rta.getMappedAttribute(IssueField.PRODUCT.getKey());
            String product = null;
            String component = null;
            String milestone = null;
            for (Product p : clientData.getProducts()) {
                if(p.getName().equals("Default")) {
                    product = p.getName();
                    for (Component c : clientData.getComponents(p)) {
                        component = c.getName();
                        break;
                    }
                    for (Milestone m : clientData.getMilestones(p)) {
                        milestone = m.getValue();
                        break;
                    }
                }
            }
            assertNotNull(product);
            assertNotNull(component);
            assertNotNull(milestone);
            
            ta.setValue(product);
            ta = rta.getMappedAttribute(IssueField.COMPONENT.getKey());
            ta.setValue(component);
            ta = rta.getMappedAttribute(IssueField.MILESTONE.getKey());
            ta.setValue(milestone);
            RepositoryResponse rr = ODCSUtil.postTaskData(rc, taskRepository, taskData);
            assertEquals(RepositoryResponse.ResponseKind.TASK_UPDATED, rr.getReposonseKind());

            taskData = getTaskData(taskData.getTaskId());
            assertNotNull(taskData);
            printTaskData(taskData); 
            assertEquals(product, rta.getMappedAttribute(IssueField.PRODUCT.getKey()).getValue());
            assertEquals(component, rta.getMappedAttribute(IssueField.COMPONENT.getKey()).getValue());
            assertEquals(milestone, rta.getMappedAttribute(IssueField.MILESTONE.getKey()).getValue());
        }

    }
    
    private class ChangeCustomFieldTestCase extends TaskTestCase {
        public ChangeCustomFieldTestCase() {
            super("testChangeCustomField");
        }
        @Override
        public void runTest() throws Throwable {
            RepositoryConfiguration clientData = rc.getCloudDevClientManager().getCloudDevClient(taskRepository).getRepositoryConfiguration(false, nullProgressMonitor);
            
            // change custom field
            TaskAttribute rta = taskData.getRoot();
            String customFieldName = clientData.getCustomFields().get(0).getName();
            TaskAttribute ta = rta.getMappedAttribute(customFieldName);
            String newValue = "custom value";
            ta.setValue(newValue);
            
            RepositoryResponse rr = ODCSUtil.postTaskData(rc, taskRepository, taskData);
            assertEquals(RepositoryResponse.ResponseKind.TASK_UPDATED, rr.getReposonseKind());

            taskData = getTaskData(taskData.getTaskId());
            assertNotNull(taskData);
            printTaskData(taskData); 
            
            ta = taskData.getRoot().getMappedAttribute(customFieldName);
            assertNotNull(ta);
            ODCS.LOG.log(Level.FINE, "   custom field name : " + customFieldName);
            ODCS.LOG.log(Level.FINE, "   custom field value : " + ta.getValue());
            assertEquals(newValue, ta.getValue());
        }        
    }
        
    private class ChangeCCTestCase extends ChangeTestCase {
        public ChangeCCTestCase() {
            super("CC", IssueField.CC);
        }

        @Override
        protected String getDifferentValue(String value) throws CoreException {
            RepositoryConfiguration clientData = rc.getCloudDevClientManager().getCloudDevClient(taskRepository).getRepositoryConfiguration(false, nullProgressMonitor);
            return getDifferentUser(value, clientData.getUsers());
        }
    }
    
    private class ReassignTestCase extends TaskTestCase {
        public ReassignTestCase() {
            super("testReassign");
        }
        @Override
        public void runTest() throws Throwable {
            RepositoryConfiguration clientData = rc.getCloudDevClientManager().getCloudDevClient(taskRepository).getRepositoryConfiguration(false, nullProgressMonitor);
            
            // reassign
            TaskAttribute rta = taskData.getRoot();
            TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.USER_ASSIGNED);
            String newValue = getDifferentUser("vrabec", clientData.getUsers());
            ta.setValue(newValue);
            
            RepositoryResponse rr = ODCSUtil.postTaskData(rc, taskRepository, taskData);
            assertEquals(RepositoryResponse.ResponseKind.TASK_UPDATED, rr.getReposonseKind());

            String taskId = rr.getTaskId();
            assertNotNull(taskId);

            taskData = getTaskData(taskData.getTaskId());
            assertNotNull(taskData);
            printTaskData(taskData);
            assertEquals(newValue, rta.getMappedAttribute(TaskAttribute.USER_ASSIGNED).getValue());
        }        
    }
    
    private class AttachementsTestCase extends TaskTestCase {
        public AttachementsTestCase() {
            super("testAttachements");
        }
        @Override
        public void runTest() throws Throwable {
            // create attachment
            File f = File.createTempFile("attachment", "txt");
            FileOutputStream fos = new FileOutputStream(f);
            String attachmentData = ATTACHMENT_DATA + System.currentTimeMillis();
            fos.write(attachmentData.getBytes());
            fos.flush();
            fos.close();

            AbstractTaskAttachmentHandler ah = rc.getTaskAttachmentHandler();
            TaskAttribute attAttribute = new TaskAttribute(taskData.getRoot(),  TaskAttribute.TYPE_ATTACHMENT);
            TaskAttribute ta = attAttribute.createMappedAttribute(TaskAttribute.ATTACHMENT_DESCRIPTION);
            ta.setValue("adding attachment");
            ta = attAttribute.createMappedAttribute(TaskAttribute.ATTACHMENT_CONTENT_TYPE);
            ta.setValue("text/plain");
            ta = attAttribute.createMappedAttribute(TaskAttribute.ATTACHMENT_FILENAME);
            ta.setValue(f.getName());

            ah.postContent(taskRepository, new ITaskImpl(taskData), new FileTaskAttachmentSource(f), "adding attachment", attAttribute, nullProgressMonitor);
            taskData = getTaskData(taskData.getTaskId());
            assertNotNull(taskData);

            printTaskData(taskData);

            List<TaskAttribute> attrs = taskData.getAttributeMapper().getAttributesByType(taskData, TaskAttribute.TYPE_ATTACHMENT);
            int bid = -1;
            TaskAttribute attr = null;
            for (TaskAttribute taskAttribute : attrs) {
                int aid = Integer.parseInt(taskAttribute.getValue());
                if(bid < aid) {
                    bid = aid;
                    attr = taskAttribute;
                }
            }
            assertNotNull(attr);
            InputStream is = ah.getContent(taskRepository, new ITaskImpl(taskData), attr, nullProgressMonitor);

            byte[] b = new byte[attachmentData.length()];
            is.read(b);
            is.close();
            assertEquals(attachmentData, new String(b));

            ODCS.LOG.log(Level.FINE, "   attachment data : " + new String(b));
        }        
    }
    
    private class CommentTestCase extends TaskTestCase {
        public CommentTestCase() {
            super("testComments");
        }
        @Override
        public void runTest() throws Throwable {
            // get curretn count of comments
            List<TaskAttribute> attrs = taskData.getAttributeMapper().getAttributesByType(taskData, TaskAttribute.TYPE_COMMENT);
            assertTrue(attrs.isEmpty());
            
            // create new comment attribute
            String comment = "new comment";
            TaskAttribute ta = taskData.getRoot().createMappedAttribute(TaskAttribute.COMMENT_NEW);
            ta.setValue(comment);
            
            // post 
            Date now = new Date(System.currentTimeMillis());
            RepositoryResponse rr = ODCSUtil.postTaskData(rc, taskRepository, taskData);
            assertEquals(RepositoryResponse.ResponseKind.TASK_UPDATED, rr.getReposonseKind());

            String taskId = rr.getTaskId();
            assertNotNull(taskId);

            taskData = getTaskData(taskData.getTaskId());
            assertNotNull(taskData);
            printTaskData(taskData);
            
            // assert 
            attrs = taskData.getAttributeMapper().getAttributesByType(taskData, TaskAttribute.TYPE_COMMENT);
            assertFalse(attrs.isEmpty());
            ta = attrs.get(attrs.size() - 1);
            
            Date d = ODCSUtil.parseLongDate(getMappedValue(ta, TaskAttribute.COMMENT_DATE));
            TaskAttribute authorAttr = ta.getMappedAttribute(TaskAttribute.COMMENT_AUTHOR);
            String author = authorAttr.getValue();
            String authorName = authorAttr.getMappedAttribute(TaskAttribute.PERSON_NAME).getValue();
            long count = Long.parseLong(getMappedValue(ta, TaskAttribute.COMMENT_NUMBER));
            String commentText = getMappedValue(ta, TaskAttribute.COMMENT_TEXT);
            
            assertEquals(1, count);
            assertEquals(comment, commentText);
            assertEquals(AbstractODCSTestCase.TEST_USER1, author);
            assertEquals("Tina Testsuite", authorName);
            assertEquals(SimpleDateFormat.getDateInstance().format(now), SimpleDateFormat.getDateInstance().format(d));
        }  
        
        private String getMappedValue(TaskAttribute a, String key) {
            TaskAttribute ma = a.getMappedAttribute(key);
            if(ma != null) {
                return ma.getValue();
            }
            return null;
        }        
    }

    private class ResolveTestCase extends TaskTestCase {
        public ResolveTestCase() {
            super("testResolve");
        }
        @Override
        public void runTest() throws Throwable {

            // resolve
            TaskAttribute rta = taskData.getRoot();
            TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.STATUS);
            ta.setValue("RESOLVED");
            
            ta = rta.getMappedAttribute(TaskAttribute.OPERATION);
            ta.setValue("RESOLVED");
            
            RepositoryResponse rr = ODCSUtil.postTaskData(rc, taskRepository, taskData);
            assertEquals(RepositoryResponse.ResponseKind.TASK_UPDATED, rr.getReposonseKind());

            taskData = getTaskData(taskData.getTaskId());
            assertNotNull(taskData);
            assertEquals("RESOLVED", taskData.getRoot().getMappedAttribute(TaskAttribute.STATUS).getValue());
            assertEquals("FIXED", taskData.getRoot().getMappedAttribute(TaskAttribute.RESOLUTION).getValue());

            printTaskData(taskData);
        }        
    }

    private abstract class AbstractSubtaskTestCase extends TaskTestCase {
        protected TaskData subTaskData;
        public AbstractSubtaskTestCase(String name) {
            super(name);
        }
        @Override
        public void runTest() throws Throwable {
            // subtask 
            createSubtask();
            
            // removeSubtask
            removeSubtask();
        }

        protected abstract void removeSubtask() throws CoreException;

        private void createSubtask() throws IOException, CoreException {
            assertNotNull(taskData);

            subTaskData = createTaskData("this is a subbug", "a subbug", "subbug");
            printTaskData(subTaskData); 

            TaskAttribute rta = taskData.getRoot();
            TaskAttribute ta = rta.getMappedAttribute(CloudDevAttribute.SUBTASKS.getTaskName());
            ta.setValue(subTaskData.getTaskId());
            RepositoryResponse rr = ODCSUtil.postTaskData(rc, taskRepository, taskData);
            assertEquals(RepositoryResponse.ResponseKind.TASK_UPDATED, rr.getReposonseKind());
            subTaskData = getTaskData(subTaskData.getTaskId());
            taskData = getTaskData(taskData.getTaskId());
            
            // assert
            assertEquals(taskData.getTaskId(), subTaskData.getRoot().getMappedAttribute(CloudDevAttribute.PARENT_TASK.getTaskName()).getValue());
            assertEquals(subTaskData.getTaskId(), taskData.getRoot().getMappedAttribute(CloudDevAttribute.SUBTASKS.getTaskName()).getValue());
            assertNotNull(taskData);

            printTaskData(taskData); 
            printTaskData(subTaskData);
        }
    }

    private class RemoveSubtaskFromParentTestCase extends AbstractSubtaskTestCase {
        public RemoveSubtaskFromParentTestCase() {
            super("testSubtaskFromParentTestCase");
        }

        @Override
        protected void removeSubtask() throws CoreException {
            // removing subtask
            TaskAttribute rta = taskData.getRoot();
            TaskAttribute ta = rta.getMappedAttribute(CloudDevAttribute.SUBTASKS.getTaskName());
            ta.setValues(Collections.<String>emptyList());
            RepositoryResponse rr = ODCSUtil.postTaskData(rc, taskRepository, taskData);
            assertEquals(RepositoryResponse.ResponseKind.TASK_UPDATED, rr.getReposonseKind());

            taskData = getTaskData(subTaskData.getTaskId());
            assertNotNull(taskData);
            assertEquals("", taskData.getRoot().getMappedAttribute(CloudDevAttribute.SUBTASKS.getTaskName()).getValue());
            subTaskData = getTaskData(subTaskData.getTaskId());
            assertNotNull(subTaskData);
            assertEquals("", subTaskData.getRoot().getMappedAttribute(CloudDevAttribute.PARENT_TASK.getTaskName()).getValue());
        }
    }
    
    private class RemoveParentFromSubtaskTestCase extends AbstractSubtaskTestCase {
        public RemoveParentFromSubtaskTestCase() {
            super("testRemoveParentFromSubtaskTestCase");
        }

        @Override
        protected void removeSubtask() throws CoreException {
            // removing subtask
            TaskAttribute rta = subTaskData.getRoot();
            TaskAttribute ta = rta.getMappedAttribute(CloudDevAttribute.PARENT_TASK.getTaskName());
            ta.setValues(Collections.<String>emptyList());
            RepositoryResponse rr = ODCSUtil.postTaskData(rc, taskRepository, subTaskData);
            assertEquals(RepositoryResponse.ResponseKind.TASK_UPDATED, rr.getReposonseKind());

            subTaskData = getTaskData(subTaskData.getTaskId());
            assertNotNull(subTaskData);
            assertEquals("", subTaskData.getRoot().getMappedAttribute(CloudDevAttribute.PARENT_TASK.getTaskName()).getValue());
            taskData = getTaskData(taskData.getTaskId());
            assertEquals("", taskData.getRoot().getMappedAttribute(CloudDevAttribute.SUBTASKS.getTaskName()).getValue());
            assertNotNull(taskData);
        }
    }

    private class DuplicateTestCase extends TaskTestCase {
        
        public DuplicateTestCase() {
            super("testDuplicate");
        }

        @Override
        public void runTest() throws Throwable {
            
            TaskData duplicateTaskData = createTaskData("this is a duplicate", "a duplicate", "duplicate");
            
            // duplicate
            TaskAttribute rta = duplicateTaskData.getRoot();
            TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.STATUS);
            ta.setValue("RESOLVED");
            ta = rta.getMappedAttribute(TaskAttribute.OPERATION);
            ta.setValue("DUPLICATE");
            ta = rta.getMappedAttribute(CloudDevAttribute.DUPLICATE_OF.getTaskName());
            ta.setValue(taskData.getTaskId());
            
            RepositoryResponse rr = ODCSUtil.postTaskData(rc, taskRepository, duplicateTaskData);
            assertEquals(RepositoryResponse.ResponseKind.TASK_UPDATED, rr.getReposonseKind());

            duplicateTaskData = getTaskData(rr.getTaskId());
            assertNotNull(duplicateTaskData);
            assertEquals("RESOLVED", duplicateTaskData.getRoot().getMappedAttribute(TaskAttribute.STATUS).getValue());
            assertEquals("DUPLICATE", duplicateTaskData.getRoot().getMappedAttribute(TaskAttribute.RESOLUTION).getValue());
            assertEquals(taskData.getTaskId(), duplicateTaskData.getRoot().getMappedAttribute(CloudDevAttribute.DUPLICATE_OF.getTaskName()).getValue());
        }        
    }

    private class ITaskImpl implements ITask {

        private final TaskData data;

        public ITaskImpl(TaskData data) {
            this.data = data;
        }

        @Override
        public String getAttribute(String id) {
            TaskAttribute rta = data.getRoot();
            return rta.getMappedAttribute(id).getValue();
        }

        @Override
        public void setAttribute(String id, String value) {
            TaskAttribute rta = data.getRoot();
            rta.getMappedAttribute(id).setValue(value);
        }
        
        @Override
        public String getTaskId() {
            return data.getTaskId();
        }        
        
        @Override
        public Date getCompletionDate() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getConnectorKind() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Date getCreationDate() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Date getDueDate() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getHandleIdentifier() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Date getModificationDate() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getOwner() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getPriority() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getRepositoryUrl() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getSummary() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public ITask.SynchronizationState getSynchronizationState() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getTaskKey() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getTaskKind() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isActive() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isCompleted() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setCompletionDate(Date date) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setCreationDate(Date date) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setDueDate(Date date) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setModificationDate(Date date) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setOwner(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setPriority(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setSummary(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setTaskKind(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setUrl(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setTaskKey(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getUrl() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int compareTo(IRepositoryElement o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public Object getAdapter(Class type) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Map<String, String> getAttributes() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getOwnerId() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setOwnerId(String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

}
