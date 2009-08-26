/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * your version of this file aunder either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.jira.issue;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.jira.core.IJiraConstants;
import org.eclipse.mylyn.internal.jira.core.JiraAttribute;
import org.eclipse.mylyn.internal.jira.core.WorkLogConverter;
import org.eclipse.mylyn.internal.jira.core.model.Component;
import org.eclipse.mylyn.internal.jira.core.model.IssueType;
import org.eclipse.mylyn.internal.jira.core.model.JiraStatus;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.internal.jira.core.model.Version;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.data.FileTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskCommentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskOperation;
import org.netbeans.modules.bugtracking.spi.IssueNode;
import org.netbeans.modules.jira.Jira;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.issuetable.ColumnDescriptor;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.util.TextUtils;
import org.netbeans.modules.jira.commands.JiraCommand;
import org.netbeans.modules.jira.repository.JiraConfiguration;
import org.netbeans.modules.jira.repository.JiraRepository;
import org.netbeans.modules.jira.util.JiraUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka, Jan Stola
 */
public class NbJiraIssue extends Issue {
    private TaskData taskData;
    private JiraRepository repository;
    private Controller controller;

    static final String LABEL_NAME_ID           = "jira.issue.id";          // NOI18N
    static final String LABEL_NAME_TYPE         = "jira.issue.type";        // NOI18N
    static final String LABEL_NAME_PRIORITY     = "jira.issue.priority";    // NOI18N
    static final String LABEL_NAME_STATUS       = "jira.issue.status";      // NOI18N
    static final String LABEL_NAME_RESOLUTION   = "jira.issue.resolution";  // NOI18N
    static final String LABEL_NAME_SUMMARY      = "jira.issue.summary";     // NOI18N
    static final String LABEL_NAME_ASSIGNED_TO  = "jira.issue.assigned";    // NOI18N

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";               // NOI18N
    private static final int SHORTENED_SUMMARY_LENGTH = 22;

    private static final String FIXED = "Fixed";                        //NOI18N

    /**
     * Issue wasn't seen yet
     */
    static final int FIELD_STATUS_IRELEVANT = -1;

    /**
     * Field wasn't changed since the issue was seen the last time
     */
    static final int FIELD_STATUS_UPTODATE = 1;

    /**
     * Field has a value in oposite to the last time when it was seen
     */
    static final int FIELD_STATUS_NEW = 2;

    /**
     * Field was changed since the issue was seen the last time
     */
    static final int FIELD_STATUS_MODIFIED = 4;
    private Map<String, String> seenAtributes;

    public enum IssueField {
        KEY(JiraAttribute.ISSUE_KEY.id(), "LBL_KEY"),
        SUMMARY(JiraAttribute.SUMMARY.id(), "LBL_SUMMARY"),
        DESCRIPTION(JiraAttribute.DESCRIPTION.id(), "LBL_DESCRIPTION"),
        STATUS(TaskAttribute.STATUS, "LBL_STATUS"),
        PRIORITY(JiraAttribute.PRIORITY.id(), "LBL_PRIORITY"),
        RESOLUTION(JiraAttribute.RESOLUTION.id(), "LBL_RESOLUTION"),
        PROJECT(JiraAttribute.PROJECT.id(), "LBL_PROJECT"),
        COMPONENT(JiraAttribute.COMPONENTS.id(), "LBL_COMPONENT", false),
        AFFECTSVERSIONS(JiraAttribute.AFFECTSVERSIONS.id(),"LBL_AFFECTSVERSIONS", false),
        FIXVERSIONS(JiraAttribute.FIXVERSIONS.id(), "LBL_FIXVERSIONS", false),
        ENVIRONMENT(JiraAttribute.ENVIRONMENT.id(), "LBL_ENVIRONMENT"),
        REPORTER(JiraAttribute.USER_REPORTER.id(), "LBL_REPORTER"),
        ASSIGNEE(JiraAttribute.USER_ASSIGNED.id(), "LBL_ASSIGNEE"),
        TYPE(JiraAttribute.TYPE.id(), "LBL_TYPE"),
        CREATION(JiraAttribute.CREATION_DATE.id(), null),
        MODIFICATION(JiraAttribute.MODIFICATION_DATE.id(), null),
        DUE(JiraAttribute.DUE_DATE.id(), "LBL_DUE"),
        ESTIMATE(JiraAttribute.ESTIMATE.id(), "LBL_ESTIMATE"),
        INITIAL_ESTIMATE(JiraAttribute.INITIAL_ESTIMATE.id(), "LBL_INITIAL_ESTIMATE"),
        ACTUAL(JiraAttribute.ACTUAL.id(), "LBL_ACTUALL"),
        PARENT_ID(JiraAttribute.PARENT_ID.id(), null),
        PARENT_KEY(JiraAttribute.PARENT_KEY.id(), null),
        SUBTASK_IDS(JiraAttribute.SUBTASK_IDS.id(), null, false),
        SUBTASK_KEYS(JiraAttribute.SUBTASK_KEYS.id(), null, false),
        COMMENT_COUNT(TaskAttribute.TYPE_COMMENT, null, false),
        ATTACHEMENT_COUNT(TaskAttribute.TYPE_ATTACHMENT, null, false);

        private final String key;
        private boolean singleAttribute;
        private final String displayNameKey;

        IssueField(String key, String displayNameKey) {
            this(key, displayNameKey, true);
        }

        IssueField(String key, String displayNameKey, boolean singleAttribute) {
            this.key = key;
            this.singleAttribute = singleAttribute;
            this.displayNameKey = displayNameKey;
        }
        public String getKey() {
            return key;
        }
        public boolean isSingleAttribute() {
            return singleAttribute;
        }
        public boolean isReadOnly() {
            return !singleAttribute;
        }
        public String getDisplayName() {
            assert displayNameKey != null; // shouldn't be called for a field with a null display name
            return NbBundle.getMessage(NbJiraIssue.class, displayNameKey);
        }
    }

    private Map<String, String> attributes;
    private Map<String, TaskOperation> availableOperations;

    private static final SimpleDateFormat CC_DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT);
    
    /**
     * Defines columns for a view table.
     */
    public static ColumnDescriptor[] DESCRIPTORS;
    private IssueNode node;
    
    public NbJiraIssue(TaskData data, JiraRepository repo) {
        super(repo);
        this.taskData = data;
        this.repository = repo;
    }

    void opened() {
        if(Jira.LOG.isLoggable(Level.FINE)) Jira.LOG.log(Level.FINE, "issue {0} open start", new Object[] {getID()});
        if(!taskData.isNew()) {
            // 1.) to get seen attributes makes no sense for new issues
            // 2.) set seenAtributes on issue open, before its actuall
            //     state is written via setSeen().
            seenAtributes = repository.getIssueCache().getSeenAttributes(getID());
        }
        String refresh = System.getProperty("org.netbeans.modules.bugzilla.noIssueRefresh"); // NOI18N
        if(refresh != null && refresh.equals("true")) {                                      // NOI18N
            return;
        }
        repository.scheduleForRefresh(getID());
        if(Jira.LOG.isLoggable(Level.FINE)) Jira.LOG.log(Level.FINE, "issue {0} open finish", new Object[] {getID()});
    }

    void closed() {
        if(Jira.LOG.isLoggable(Level.FINE)) Jira.LOG.log(Level.FINE, "issue {0} close start", new Object[] {getID()});
        repository.stopRefreshing(getID());
        seenAtributes = null;
        if(Jira.LOG.isLoggable(Level.FINE)) Jira.LOG.log(Level.FINE, "issue {0} close finish", new Object[] {getID()});
    }

    @Override
    public boolean isNew() {
        return taskData == null || taskData.isNew();
    }

    public void setTaskData(TaskData taskData) {
        assert !taskData.isPartial();
        this.taskData = taskData;
        attributes = null; // reset
        availableOperations = null;
        Jira.getInstance().getRequestProcessor().post(new Runnable() {
            public void run() {
                ((JiraIssueNode)getNode()).fireDataChanged();
                fireDataChanged();
            }
        });
    }

    public JiraRepository getRepository() {
        return repository;
    }

    public String getID() {
//        return taskData.getTaskId(); // XXX id or key ???
        return getID(taskData);
    }

    String getKey() {
        return getID(taskData);
    }

    public String getSummary() {
        return getFieldValue(IssueField.SUMMARY);
    }
    
    String getDescription() {
        return getFieldValue(IssueField.DESCRIPTION);
    }

    IssueType getType() {
        String id = getFieldValue(IssueField.TYPE);
        return repository.getConfiguration().getIssueTypeById(id);
    }

    Priority getPriority() {
        String id = getFieldValue(IssueField.PRIORITY);
        return repository.getConfiguration().getPriorityById(id);
    }

    JiraStatus getStatus() {
        String id = getFieldValue(IssueField.STATUS);
        return repository.getConfiguration().getStatusById(id);
    }

    Resolution getResolution() {
        String id = getFieldValue(IssueField.RESOLUTION);
        return repository.getConfiguration().getResolutionById(id);
    }

    TaskRepository getTaskRepository() {
        return repository.getTaskRepository();
    }

    public boolean isSubtask() {
        String key = getParentKey();
        return key != null && !key.trim().equals("");
    }

    public boolean hasSubtasks() {
        List<String> keys = getSubtaskKeys();
        return keys != null && keys.size() > 0;
    }

    public String getParentKey() {
        return getFieldValue(IssueField.PARENT_KEY);
    }

    public List<String> getSubtaskKeys() {
        return getFieldValues(IssueField.SUBTASK_KEYS);
    }

    public String getParentID() {
        return getFieldValue(IssueField.PARENT_ID);
    }

    public List<String> getSubtaskID() {
        return getFieldValues(IssueField.SUBTASK_IDS);
    }

    Comment[] getComments() {
        List<TaskAttribute> attrs = taskData.getAttributeMapper().getAttributesByType(taskData, TaskAttribute.TYPE_COMMENT);
        if (attrs == null) {
            return new Comment[0];
        }
        List<Comment> comments = new ArrayList<Comment>(attrs.size());
        for (TaskAttribute taskAttribute : attrs) {
            comments.add(new Comment(taskAttribute));
        }
        return comments.toArray(new Comment[comments.size()]);
    }

    Attachment[] getAttachments() {
        List<TaskAttribute> attrs = taskData.getAttributeMapper().getAttributesByType(taskData, TaskAttribute.TYPE_ATTACHMENT);
        if (attrs == null) {
            return new Attachment[0];
        }
        List<Attachment> attachments = new ArrayList<Attachment>(attrs.size());
        for (TaskAttribute taskAttribute : attrs) {
            attachments.add(new Attachment(taskAttribute));
        }
        return attachments.toArray(new Attachment[attachments.size()]);
    }

    CustomField[] getCustomFields () {
        Map<String, TaskAttribute> attrs = taskData.getRoot().getAttributes();
        if (attrs == null) {
            return new CustomField[0];
        }
        List<CustomField> fields = new ArrayList<CustomField>(10);
        
        for (TaskAttribute attribute : attrs.values()) {
            if (attribute.getId().startsWith(IJiraConstants.ATTRIBUTE_CUSTOM_PREFIX)) {
                CustomField field = new CustomField(attribute);
                fields.add(field);
            }
        }
        return fields.toArray(new CustomField[fields.size()]);
    }

    void setCustomField(CustomField customField) {
        Map<String, TaskAttribute> attrs = taskData.getRoot().getAttributes();
        if (attrs == null) {
            return;
        }
        for (TaskAttribute attribute : attrs.values()) {
            if (attribute.getId().startsWith(IJiraConstants.ATTRIBUTE_CUSTOM_PREFIX)
                    && customField.getId().equals(attribute.getId().substring(IJiraConstants.ATTRIBUTE_CUSTOM_PREFIX.length()))) {
                attribute.setValues(customField.getValues());
            }
        }
    }

    /**
     * Returns an array of worklogs under the issue.
     * @return
     */
    WorkLog[] getWorkLogs () {
        List<TaskAttribute> attrs = taskData.getAttributeMapper().getAttributesByType(taskData, WorkLogConverter.TYPE_WORKLOG);
        if (attrs == null) {
            return new WorkLog[0];
        }
        List<WorkLog> workLogs = new ArrayList<WorkLog>(attrs.size());
        for (TaskAttribute taskAttribute : attrs) {
            workLogs.add(new WorkLog(taskAttribute));
        }
        return workLogs.toArray(new WorkLog[workLogs.size()]);
    }

    /**
     * Adds a new worklog. Just one worklog can be added before committing the issue.
     * Don't forget to commit the issue.
     * @param startDate
     * @param spentTime in seconds
     * @param comment
     */
    public void addWorkLog (Date startDate, long spentTime, String comment) {
        if(startDate != null) {
            TaskAttribute attribute = taskData.getRoot().createMappedAttribute(WorkLogConverter.ATTRIBUTE_WORKLOG_NEW);
            TaskAttributeMapper mapper = taskData.getAttributeMapper();
            mapper.setLongValue(attribute.createMappedAttribute(WorkLogConverter.TIME_SPENT.key()), spentTime);
            mapper.setDateValue(attribute.createMappedAttribute(WorkLogConverter.START_DATE.key()), startDate);
            mapper.setValue(attribute.createMappedAttribute(WorkLogConverter.COMMENT.key()), comment);
        }
    }

    /**
     * Reloads the task data
     * @return true if successfully refreshed
     */
    public boolean refresh() {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N
        return refresh(getID(), false);
    }

    /**
     * Reloads the task data and refreshes the issue cache
     * @param key key of the issue
     * @return true if successfully refreshed
     */
    public boolean refresh(String key, boolean cacheThisIssue) { // XXX cacheThisIssue - we probalby don't need this, just always set the issue into the cache
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N
        try {
            TaskData td = JiraUtils.getTaskDataByKey(repository, key);
            if(td == null) {
                return false;
            }
            getRepository().getIssueCache().setIssueData(key, td, this); // XXX
            if (controller != null) {
                controller.refreshViewData();
            }
        } catch (IOException ex) {
            Jira.LOG.log(Level.SEVERE, null, ex);
        }
        return true;
    }

    /**
     * Reloads the task data and refreshes the issue cache
     * @param id id of the issue
     * @return true if successfully refreshed
     */
    public boolean refreshById(String id) {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N
        try {
            TaskData td = JiraUtils.getTaskDataById(repository, id);
            if(td == null) {
                return false;
            }
            String key = getID(td);
            getRepository().getIssueCache().setIssueData(key, td, this); // XXX
            if (controller != null) {
                controller.refreshViewData();
            }
        } catch (IOException ex) {
            Jira.LOG.log(Level.SEVERE, null, ex);
        }
        return true;
    }

    /**
     * Tries to update the taskdata and set issue to Resolved:resolution.
     * <strong>Do not forget to submit the issue</strong>
     * @param resolution
     * @param comment can be null, in such case no comment will be set
     * @throws org.eclipse.mylyn.internal.jira.core.service.JiraException
     * @throws java.lang.IllegalStateException if resolve operation is not permitted for this issue
     */
    public void resolve(Resolution resolution, String comment) {
        if (Jira.LOG.isLoggable(Level.FINE)) {
            Jira.LOG.fine(getClass().getName() + ": resolve issue " + getKey() + ": " + resolution.getName());    //NOI18N
        }
        TaskAttribute rta = taskData.getRoot();

        TaskOperation operation = getResolveOperation();
        if (operation == null) {
            throw new IllegalStateException("Resolve operation not permitted"); //NOI18N
        } else {
            setOperation(operation);
        }

        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.RESOLUTION);
        ta.setValue(resolution.getId());
        addComment(comment);
    }

    /**
     * Tries to update the taskdata and set issue to Open.
     * <strong>Do not forget to submit the issue</strong>
     * @param comment can be null, in such case no comment will be set
     * @throws org.eclipse.mylyn.internal.jira.core.service.JiraException
     * @throws java.lang.IllegalStateException if resolve operation is not permitted for this issue
     */
    void reopen(String comment) {
        if (Jira.LOG.isLoggable(Level.FINE)) {
            Jira.LOG.fine(getClass().getName() + ": reopening issue" + getKey()); //NOI18N
        }
        TaskAttribute rta = taskData.getRoot();

        Map<String, TaskOperation> operations = getAvailableOperations();
        TaskOperation operation = null;
        for (Map.Entry<String, TaskOperation> entry : operations.entrySet()) {
            String operationLabel = entry.getValue().getLabel();
            if (Jira.LOG.isLoggable(Level.FINEST)) {
                Jira.LOG.finest(getClass().getName() + ": reopening issue" + getKey() + ": available operation: " + operationLabel + "(" + entry.getValue().getOperationId() + ")"); //NOI18N
            }
            if (JiraUtils.isReopenOperation(operationLabel)) {
                operation = entry.getValue();
                break;
            }
        }
        if (operation == null) {
            throw new IllegalStateException("Reopen operation not permitted"); //NOI18N
        } else {
            setOperation(operation);
        }
        addComment(comment);
    }

    /**
     * Tries to update the taskdata and set issue to closed:resolution.
     * <strong>Do not forget to submit the issue</strong>
     * @param resolution
     * @param comment can be null, in such case no comment will be set
     * @throws org.eclipse.mylyn.internal.jira.core.service.JiraException
     * @throws java.lang.IllegalStateException if resolve operation is not permitted for this issue
     */
    public void close(Resolution resolution, String comment) {
        if (Jira.LOG.isLoggable(Level.FINE)) {
            Jira.LOG.fine(getClass().getName() + ": close issue " + getKey() + ": " + resolution.getName());    //NOI18N
        }
        TaskAttribute rta = taskData.getRoot();

        Map<String, TaskOperation> operations = getAvailableOperations();
        TaskOperation operation = null;
        for (Map.Entry<String, TaskOperation> entry : operations.entrySet()) {
            String operationLabel = entry.getValue().getLabel();
            if (Jira.LOG.isLoggable(Level.FINEST)) {
                Jira.LOG.finest(getClass().getName() + ": closing issue" + getKey() + ": available operation: " + operationLabel + "(" + entry.getValue().getOperationId() + ")"); //NOI18N
            }
            if (JiraUtils.isCloseOperation(operationLabel)) {
                operation = entry.getValue();
                break;
            }
        }
        if (operation == null) {
            throw new IllegalStateException("Close operation not permitted"); //NOI18N
        } else {
            setOperation(operation);
        }

        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.RESOLUTION);
        ta.setValue(resolution.getId());
        addComment(comment);
    }

    /**
     * Tries to update the taskdata and set issue to started.
     * <strong>Do not forget to submit the issue</strong>
     * @throws org.eclipse.mylyn.internal.jira.core.service.JiraException
     * @throws java.lang.IllegalStateException if resolve operation is not permitted for this issue
     */
    public void startProgress() {
        if (Jira.LOG.isLoggable(Level.FINE)) {
            Jira.LOG.fine(getClass().getName() + ": starting issue " + getKey());    //NOI18N
        }
        TaskAttribute rta = taskData.getRoot();

        Map<String, TaskOperation> operations = getAvailableOperations();
        TaskOperation operation = null;
        for (Map.Entry<String, TaskOperation> entry : operations.entrySet()) {
            String operationLabel = entry.getValue().getLabel();
            if (Jira.LOG.isLoggable(Level.FINEST)) {
                Jira.LOG.finest(getClass().getName() + ": starting issue" + getKey() + ": available operation: " + operationLabel + "(" + entry.getValue().getOperationId() + ")"); //NOI18N
            }
            if (JiraUtils.isStartProgressOperation(operationLabel)) {
                operation = entry.getValue();
                break;
            }
        }
        if (operation == null) {
            throw new IllegalStateException("Start progress operation not permitted"); //NOI18N
        } else {
            setOperation(operation);
        }
    }

    /**
     * Tries to update the taskdata and stops the progress.
     * <strong>Do not forget to submit the issue</strong>
     * @throws org.eclipse.mylyn.internal.jira.core.service.JiraException
     * @throws java.lang.IllegalStateException if resolve operation is not permitted for this issue
     */
    public void stopProgress() {
        if (Jira.LOG.isLoggable(Level.FINE)) {
            Jira.LOG.fine(getClass().getName() + ": starting issue " + getKey());    //NOI18N
        }
        TaskAttribute rta = taskData.getRoot();

        Map<String, TaskOperation> operations = getAvailableOperations();
        TaskOperation operation = null;
        for (Map.Entry<String, TaskOperation> entry : operations.entrySet()) {
            String operationLabel = entry.getValue().getLabel();
            if (Jira.LOG.isLoggable(Level.FINEST)) {
                Jira.LOG.finest(getClass().getName() + ": starting issue" + getKey() + ": available operation: " + operationLabel + "(" + entry.getValue().getOperationId() + ")"); //NOI18N
            }
            if (JiraUtils.isStopProgressOperation(operationLabel)) {
                operation = entry.getValue();
                break;
            }
        }
        if (operation == null) {
            throw new IllegalStateException("Stop progress operation not permitted"); //NOI18N
        } else {
            setOperation(operation);
        }
    }

    /**
     * Updates task data and sets the operation field
     * @param operationId id of requested operation
     * @throws java.lang.IllegalArgumentException if the operation is not permitted for this issue
     */

    public void setOperation (String operationId) {
        Map<String, TaskOperation> operations = getAvailableOperations();
        TaskOperation operation = null;
        for (Map.Entry<String, TaskOperation> entry : operations.entrySet()) {
            String operationLabel = entry.getValue().getLabel().toLowerCase();
            if (Jira.LOG.isLoggable(Level.FINEST)) {
                Jira.LOG.finest(getClass().getName() + ": setOperation: operation " + operationLabel + "(" + entry.getValue().getOperationId() + ") is available");
            }
            if (entry.getValue().getOperationId().equals(operationId)) {
                operation = entry.getValue();
                break;
            }
        }
        if (operation == null) {
            throw new IllegalArgumentException("Operation with id " + operationId + " is not permitted"); //NOI18N
        }
        setOperation(operation);
    }

    private void setOperation (TaskOperation operation) {
        TaskAttribute ta = taskData.getRoot().getMappedAttribute(TaskAttribute.OPERATION);
        ta.setValue(operation.getOperationId());
    }

    @Override
    public String getDisplayName() {
        return taskData.isNew() ?
            NbBundle.getMessage(NbJiraIssue.class, "CTL_NewIssue") : // NOI18N
            NbBundle.getMessage(NbJiraIssue.class, "CTL_Issue", new Object[] {getID(), getSummary()}); // NOI18N
    }

    @Override
    public String getShortenedDisplayName() {
        if (taskData.isNew()) {
            return getDisplayName();
        }

        String shortSummary = TextUtils.shortenText(getSummary(),
                                                    2,    //try at least 2 words
                                                    SHORTENED_SUMMARY_LENGTH);
        return NbBundle.getMessage(NbJiraIssue.class,
                                   "CTL_Issue",                         //NOI18N
                                   new Object[] {getID(), shortSummary});
    }

    @Override
    public String getTooltip() {
        return "Issue: " + getKey(); // + " " + getType() + " " + getPriority() + " " + getStatus();
    }

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("hh24:mmm:ss dd.mm.yyyy");

    @Override
    public IssueNode getNode() {
        if(node == null) {
            node = new JiraIssueNode(this);
        }
        return node;
    }

    // XXX carefull - implicit double refresh
    public void addComment(String comment, boolean close) {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N
        if (Jira.LOG.isLoggable(Level.FINE)) {
            Jira.LOG.fine(getClass().getName() + ": adding comment to issue: " + getKey());    //NOI18N
        }
        if(comment == null && !close) {
            return;
        }
        refresh();

        // resolved attrs
        if(close) {
            try {
                resolve(JiraUtils.getResolutionByName(repository, FIXED), comment); // XXX constant, what about setting in options?
            } catch (IllegalStateException ise) {
                // so do not set to close if already closed
                Jira.LOG.log(Level.INFO, "Close not permitted, current status is " + getStatus().getName() + ", leaving status the same", ise);
            }
        }

        JiraCommand submitCmd = new JiraCommand() {
            public void execute() throws CoreException, IOException {
                submitAndRefresh();
            }
        };
        repository.getExecutor().execute(submitCmd);
    }

    /**
     * Add comment to isseue.
     * <strong>Do not forget to submit</strong>
     * @param comment
     */
    public void addComment(String comment) {
        if(comment != null) {
            if (Jira.LOG.isLoggable(Level.FINE)) {
                Jira.LOG.fine(getClass().getName() + ": adding comment to issue " + getKey());    //NOI18N
            }
            TaskAttribute ta = taskData.getRoot().createMappedAttribute(TaskAttribute.COMMENT_NEW);
            ta.setValue(comment);
        }
    }

    void addAttachment(File file, final String comment, String contentType) {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N
        final FileTaskAttachmentSource attachmentSource = new FileTaskAttachmentSource(file);
        if (contentType == null) {
            file = FileUtil.normalizeFile(file);
            String ct = FileUtil.getMIMEType(FileUtil.toFileObject(file));
            if ((ct != null) && (!"content/unknown".equals(ct))) { // NOI18N
                contentType = ct;
            } else {
                contentType = FileTaskAttachmentSource.getContentTypeFromFilename(file.getName());
            }
        }
        attachmentSource.setContentType(contentType);

        TaskAttachmentMapper mapper = new TaskAttachmentMapper();
        mapper.setContentType(contentType);
        final TaskAttribute attAttribute = new TaskAttribute(taskData.getRoot(),  TaskAttribute.TYPE_ATTACHMENT);
        mapper.applyTo(attAttribute);
        JiraCommand cmd = new JiraCommand() {
            public void execute() throws CoreException, IOException {
//                refresh(); // XXX no refreshing may cause a midair collision - we should refresh in such a case and attach then
                if (Jira.LOG.isLoggable(Level.FINER)) {
                    Jira.LOG.finer("adding an attachment: issue: " + getKey());
                }
                IssueTask task = new IssueTask(repository.getUrl(), NbJiraIssue.this.getTaskData().getTaskId(), "Attachment upload task", getKey());
                if (!Jira.getInstance().getRepositoryConnector().getTaskAttachmentHandler().canPostContent(repository.getTaskRepository(), task)) {
                    Jira.LOG.warning("adding an attachment: cannot post content: issue: " + getKey());
                    return;
                }
                Jira.getInstance().getRepositoryConnector().getTaskAttachmentHandler().postContent(repository.getTaskRepository(),
                        task, attachmentSource, comment, attAttribute, new NullProgressMonitor());
                refresh(); // XXX to much refresh - is there no other way?
            }
        };
        repository.getExecutor().execute(cmd);
    }

    @Override
    public void attachPatch(File file, String comment) {
        addAttachment(file, comment, null);
    }

    @Override
    public BugtrackingController getController() {
        if(controller == null) {
            controller = new Controller();
        }
        return controller;
    }

    @Override
    public String getRecentChanges() {
        if(wasSeen()) {
            return "";                                                          // NOI18N
        }
        int status = repository.getIssueCache().getStatus(getID());
        if(status == Issue.ISSUE_STATUS_NEW) {
            return NbBundle.getMessage(NbJiraIssue.class, "LBL_NEW_STATUS");
        } else if(status == Issue.ISSUE_STATUS_MODIFIED) {
            List<IssueField> changedFields = new ArrayList<IssueField>();
            Map<String, String> seenAtributes = getSeenAttributes();
            assert seenAtributes != null;
            for (IssueField f : IssueField.values()) {
                switch(f) {
                    case MODIFICATION :
                        continue;
                }
                String value = getFieldValue(f);
                String seenValue = seenAtributes.get(f.key);
                if(seenValue == null) {
                    seenValue = "";                                             // NOI18N
                }
                if(!value.trim().equals(seenValue)) {
                    changedFields.add(f);
                }
            }
            int changedCount = changedFields.size();
            if(changedCount == 1) {
                String ret = null;
                for (IssueField changedField : changedFields) {
                    switch(changedField) {
                        case SUMMARY :
                            ret = NbBundle.getMessage(NbJiraIssue.class, "LBL_SUMMARY_CHANGED_STATUS");
                            break;
//                        XXX
//                        case DEPENDS_ON :
//                        case BLOCKS :
//                            ret = NbBundle.getMessage(NbJiraIssue.class, "LBL_DEPENDENCE_CHANGED_STATUS");
//                            break;
                        case COMMENT_COUNT :
                            String value = getFieldValue(changedField);
                            String seenValue = seenAtributes.get(changedField.key);
                            if(seenValue == null || seenValue.trim().equals("")) {
                                seenValue = "0";
                            }
                            int count = 0;
                            try {
                                count = Integer.parseInt(value) - Integer.parseInt(seenValue);
                            } catch(NumberFormatException ex) {
                                Jira.LOG.log(Level.WARNING, ret, ex);
                            }
                            ret = NbBundle.getMessage(NbJiraIssue.class, "LBL_COMMENTS_CHANGED", new Object[] {count});
                            break;
                        case ATTACHEMENT_COUNT :
                            ret = NbBundle.getMessage(NbJiraIssue.class, "LBL_ATTACHMENTS_CHANGED");
                            break;
                        default :
                            ret = NbBundle.getMessage(NbJiraIssue.class, "LBL_CHANGED_TO", new Object[] {changedField.getDisplayName(), getFieldDisplayValue(changedField)});
                    }
                }
                return ret;
            } else {
                String ret = null;
                for (IssueField changedField : changedFields) {
                    switch(changedField) {
                        case SUMMARY :
                            ret = NbBundle.getMessage(NbJiraIssue.class, "LBL_CHANGES_INCL_SUMMARY", new Object[] {changedCount});
                            break;
                        case PRIORITY :
                            ret = NbBundle.getMessage(NbJiraIssue.class, "LBL_CHANGES_INCL_PRIORITY", new Object[] {changedCount});
                            break;
                        case TYPE :
                            ret = NbBundle.getMessage(NbJiraIssue.class, "LBL_CHANGES_INCL_TYPE", new Object[] {changedCount});
                            break;
                        case PROJECT :
                            ret = NbBundle.getMessage(NbJiraIssue.class, "LBL_CHANGES_INCL_PROJECT", new Object[] {changedCount});
                            break;
                        case COMPONENT :
                            ret = NbBundle.getMessage(NbJiraIssue.class, "LBL_CHANGES_INCL_COMPONENT", new Object[] {changedCount});
                            break;
                        case ENVIRONMENT :
                            ret = NbBundle.getMessage(NbJiraIssue.class, "LBL_CHANGES_INCL_ENVIRONMENT", new Object[] {changedCount});
                            break;
// XXX
//                        case VERSION :
//                            ret = NbBundle.getMessage(NbJiraIssue.class, "LBL_CHANGES_INCL_VERSION", new Object[] {changedCount});
//                            break;
//                        case MILESTONE :
//                            ret = NbBundle.getMessage(NbJiraIssue.class, "LBL_CHANGES_INCL_MILESTONE", new Object[] {changedCount});
//                            break;
                        case ASSIGNEE :
                            ret = NbBundle.getMessage(NbJiraIssue.class, "LBL_CHANGES_INCL_ASSIGNEE", new Object[] {changedCount});
                            break;                        
//                        case DEPENDS_ON :
//                        case BLOCKS :
//                            ret = NbBundle.getMessage(NbJiraIssue.class, "LBL_CHANGES_INCLUSIVE_DEPENDENCE", new Object[] {changedCount});
//                            break;
                        default :
                            ret = NbBundle.getMessage(NbJiraIssue.class, "LBL_CHANGES", new Object[] {changedCount});
                    }
                    return ret;
                }
            }
        }
        return "";
    }

    @Override
    public Map<String, String> getAttributes() {
        if(attributes == null) {
            attributes = new HashMap<String, String>();
            for (IssueField field : IssueField.values()) {
                String value = getFieldValue(field);
                if(value != null && !value.trim().equals("")) {                 // NOI18N
                    attributes.put(field.key, value);
                }
            }
        }
        return attributes;
    }   

    public static ColumnDescriptor[] getColumnDescriptors(JiraRepository repository) {
        if(DESCRIPTORS == null) {
            ResourceBundle loc = NbBundle.getBundle(NbJiraIssue.class);
            JiraConfiguration conf = repository.getConfiguration();
            JTable t = new JTable();
            DESCRIPTORS = new ColumnDescriptor[] {
                new ColumnDescriptor<String>(LABEL_NAME_ID, String.class,
                                                  loc.getString("CTL_Issue_ID_Title"), // NOI18N
                                                  loc.getString("CTL_Issue_ID_Desc"), // NOI18N
                                                  BugtrackingUtil.getColumnWidthInPixels(20, t)),
                new ColumnDescriptor<String>(LABEL_NAME_SUMMARY, String.class,
                                                  loc.getString("CTL_Issue_Summary_Title"), // NOI18N
                                                  loc.getString("CTL_Issue_Summary_Desc")), // NOI18N
                new ColumnDescriptor<String>(LABEL_NAME_TYPE, String.class,
                                                  loc.getString("CTL_Issue_Type_Title"), // NOI18N
                                                  loc.getString("CTL_Issue_Type_Desc"), // NOI18N
                                                  BugtrackingUtil.getLongestWordWidth(
                                                    loc.getString("CTL_Issue_Type_Title"),      // NOI18N
                                                    JiraUtils.toStrings(conf.getIssueTypes()), t)),
                new ColumnDescriptor<String>(LABEL_NAME_PRIORITY, String.class,
                                                  loc.getString("CTL_Issue_Priority_Title"), // NOI18N
                                                  loc.getString("CTL_Issue_Priority_Desc"), // NOI18N
                                                  BugtrackingUtil.getLongestWordWidth(
                                                    loc.getString("CTL_Issue_Priority_Title"),      // NOI18N
                                                    JiraUtils.toStrings(conf.getPriorities()), t, true)),
                new ColumnDescriptor<String>(LABEL_NAME_STATUS, String.class,
                                                  loc.getString("CTL_Issue_Status_Title"), // NOI18N
                                                  loc.getString("CTL_Issue_Status_Desc"), // NOI18N
                                                  BugtrackingUtil.getLongestWordWidth(
                                                    loc.getString("CTL_Issue_Status_Title"),      // NOI18N
                                                    JiraUtils.toStrings(conf.getPriorities()), t)),
                new ColumnDescriptor<String>(LABEL_NAME_RESOLUTION, String.class,
                                                  loc.getString("CTL_Issue_Resolution_Title"), // NOI18N
                                                  loc.getString("CTL_Issue_Resolution_Desc"), // NOI18N
                                                  BugtrackingUtil.getLongestWordWidth(
                                                    loc.getString("CTL_Issue_Status_Title"),      // NOI18N
                                                    JiraUtils.toStrings(conf.getResolutions()), t)),
                new ColumnDescriptor<String>(LABEL_NAME_ASSIGNED_TO, String.class,
                                              loc.getString("CTL_Issue_Assigned_Title"),        // NOI18N
                                              loc.getString("CTL_Issue_Assigned_Desc"),         // NOI18N
                                              BugtrackingUtil.getColumnWidthInPixels(20, t))
            };
        }
        return DESCRIPTORS;
    }

    /**
     * Returns the key from the given taskData or null if taskData.isNew()
     * @param taskData
     * @return key or null
     */
    public static String getID(TaskData taskData) {
        if(taskData.isNew()) {
            return null;
        }
        return getFieldValue(taskData, IssueField.KEY);
    }

    /**
     * Returns true if resolve currently is allowed
     * @return
     */
    public boolean isResolveAllowed() {
        return getResolveOperation() != null;
    }

    // XXX fields logic - 100% bugzilla overlap
    /**
     * Returns the value represented by the given field
     *
     * @param f
     * @return
     */
    String getFieldValue(IssueField f) {
        return getFieldValue(taskData, f);
    }

    /**
     * Returns the given fields diplay value
     * @param f
     * @return
     */
    private String getFieldDisplayValue(IssueField f) {
        String value = getFieldValue(taskData, f);
        if(value == null || value.trim().equals("")) {
            return "";                                                          // NOI18N
        }
        JiraConfiguration config = repository.getConfiguration();
        switch(f) {
            case STATUS:
                JiraStatus status = config != null ? config.getStatusById(value) : null;
                return status != null ? status.getName() : "";                  // NOI18N
            case PRIORITY:
                Priority prio = config != null ? config.getPriorityById(value) : null;
                return prio != null ? prio.getName() : "";                      // NOI18N
            case RESOLUTION:
                Resolution res = config != null ? config.getResolutionById(value) : null;
                return res != null ? res.getName() : "";                        // NOI18N
            case PROJECT:
                Project project = config != null ? config.getProjectById(value) : null;
                return project != null ? project.getName() : "";                // NOI18N
            case COMPONENT:
                String projectId = getFieldValue(IssueField.PROJECT);
                // Component and version are multi-value fields, cannot use directly getFieldValue()
                List<String> values = new LinkedList<String>();
                for (String v : getFieldValues(f)) {
                    Component version = config != null ? config.getComponentById(projectId, v) : null;
                    if (version != null) {
                        values.add(version.getName());
                    }
                }
                return values.toString();
            case AFFECTSVERSIONS:
            case FIXVERSIONS:
                projectId = getFieldValue(IssueField.PROJECT);
                values = new LinkedList<String>();
                for (String v : getFieldValues(f)) {
                    Version version = config != null ? config.getVersionById(projectId, v) : null;
                    if (version != null) {
                        values.add(version.getName());
                    }
                }
                return values.toString();
            case TYPE:
                IssueType type = config != null ? config.getIssueTypeById(value) : null;
                return type != null ? type.getName() : "";                      // NOI18N
            default:
                return value;
        }
    }


    static String getFieldValue(TaskData taskData, IssueField f) {
        TaskAttribute a = taskData.getRoot().getMappedAttribute(f.key);
        if(f.isSingleAttribute()) {
            if(a != null && a.getValues().size() > 1) {
                return listValues(a);
            }
            return a != null ? a.getValue() : ""; // NOI18N
        } else {
            String value = "";                                          //NOI18N
            if (a != null) {
                ArrayList<String> attrs = new ArrayList<String>(a.getValues());
                Collections.sort(attrs);
                value += "" + attrs.size() + attrs.toString();          //NOI18N
            }
            return value;
        }
    }

    /**
     * Returns a comma separated list created
     * from the values returned by TaskAttribute.getValues()
     *
     * @param a
     * @return
     */
    private static String listValues(TaskAttribute a) {
        if(a == null) {
            return "";                                                          // NOI18N
        }
        StringBuffer sb = new StringBuffer();
        List<String> l = a.getValues();
        for (int i = 0; i < l.size(); i++) {
            String s = l.get(i);
            sb.append(s);
            if(i < l.size() -1) {
                sb.append(",");                                                 // NOI18N
            }
        }
        return sb.toString();
    }

    private TaskOperation getResolveOperation () {
        TaskOperation operation = null;
        Map<String, TaskOperation> operations = getAvailableOperations();
        for (Map.Entry<String, TaskOperation> entry : operations.entrySet()) {
            String operationLabel = entry.getValue().getLabel();
            if (Jira.LOG.isLoggable(Level.FINEST)) {
                Jira.LOG.finest(getClass().getName() + ": resolving issue" + getKey() + ": available operation: " + operationLabel + "(" + entry.getValue().getOperationId() + ")"); //NOI18N
            }
            if (JiraUtils.isResolveOperation(operationLabel)) {
                operation = entry.getValue();
                break;
            }
        }
        return operation;
    }

    public void setFieldValue(IssueField f, String value) {
        if(f.isReadOnly()) {
            assert false : "can't set value into IssueField " + f.name();       // NOI18N
            return;
        }
        TaskAttribute a = taskData.getRoot().getMappedAttribute(f.key);
        if(a == null) {
            a = new TaskAttribute(taskData.getRoot(), f.key);
        }
        a.setValue(value);
    }

    List<String> getFieldValues(IssueField f) {
        if(!f.isSingleAttribute()) {
            TaskAttribute a = taskData.getRoot().getMappedAttribute(f.key);
            if(a != null) {
                return a.getValues();
            } else {
                return Collections.emptyList();
            }
        } else {
            List<String> ret = new ArrayList<String>();
            ret.add(getFieldValue(f));
            return ret;
        }
    }

    void setFieldValues(IssueField f, List<String> ccs) {
        TaskAttribute a = taskData.getRoot().getMappedAttribute(f.key);
        if(a == null) {
            a = new TaskAttribute(taskData.getRoot(), f.key);
        }
        a.setValues(ccs);
    }

    TaskData getTaskData() {
        return taskData;
    }

    public boolean submitAndRefresh() {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N

        final boolean wasNew = taskData.isNew();
        final boolean wasSeenAlready = wasNew || repository.getIssueCache().wasSeen(getID());
        final RepositoryResponse[] rr = new RepositoryResponse[1];
        if (Jira.LOG.isLoggable(Level.FINEST)) {
            Jira.LOG.finest("submitAndRefresh: id: " + getID() + ", new: " + wasNew);
        }
        JiraCommand submitCmd = new JiraCommand() {
            public void execute() throws CoreException {
                // submit
                if (Jira.LOG.isLoggable(Level.FINEST)) {
                    Jira.LOG.finest("submitAndRefresh, submitCmd: id: " + getID() + ", new: " + wasNew);
                }
                Set<TaskAttribute> attrs = new HashSet<TaskAttribute>(); // XXX what is this for
                rr[0] = Jira.getInstance().getRepositoryConnector().getTaskDataHandler().postTaskData(getTaskRepository(), taskData,
                        attrs, new NullProgressMonitor());
                // XXX evaluate rr
            }
        };
        repository.getExecutor().execute(submitCmd);
        if(submitCmd.hasFailed()) {
            return false;
        }
        
        JiraCommand refreshCmd = new JiraCommand() {
            public void execute() throws CoreException {
                if (Jira.LOG.isLoggable(Level.FINEST)) {
                    Jira.LOG.finest("submitAndRefresh, refreshCmd: id: " + getID() + ", new: " + wasNew);
                }
                if (!wasNew) {
                    refresh();
                } else {
                    refreshById(rr[0].getTaskId());
                }
            }
        };
        repository.getExecutor().execute(refreshCmd);
        if(refreshCmd.hasFailed()) {
            return false;
        }

        // it was the user who made the changes, so preserve the seen status if seen already
        if (wasSeenAlready) {
            try {
                repository.getIssueCache().setSeen(getID(), true);
                // it was the user who made the changes, so preserve the seen status if seen already
            } catch (IOException ex) {
                Jira.LOG.log(Level.SEVERE, null, ex);
            }
        }
        if(wasNew) {
            // a new issue was created -> refresh all queries
            repository.refreshAllQueries();
        }

        try {
            seenAtributes = null;
            setSeen(true);
        } catch (IOException ex) {
            Jira.LOG.log(Level.SEVERE, null, ex);
        }

        return true;
    }

    /**
     * Returns a status value for the given field<br>
     * <ul>
     *  <li>{@link #FIELD_STATUS_IRELEVANT} - issue wasn't seen yet
     *  <li>{@link #FIELD_STATUS_UPTODATE} - field value wasn't changed
     *  <li>{@link #FIELD_STATUS_MODIFIED} - field value was changed
     *  <li>{@link #FIELD_STATUS_NEW} - field has a value for the first time since it was seen
     * </ul>
     * @param f IssueField
     * @return a status value
     */
    int getFieldStatus(IssueField f) {
        Map<String, String> a = getSeenAttributes();
        String seenValue = a != null ? a.get(f.key) : null;
        if(seenValue == null) {
            seenValue = "";                                                     // NOI18N
        }
        if(seenValue.equals("") && !seenValue.equals(getFieldValue(f))) {       // NOI18N
            return FIELD_STATUS_NEW;
        } else if (!seenValue.equals(getFieldValue(f))) {
            return FIELD_STATUS_MODIFIED;
        }
        return FIELD_STATUS_UPTODATE;
    }

    private Map<String, String> getSeenAttributes() {
        if(seenAtributes == null) {
            seenAtributes = repository.getIssueCache().getSeenAttributes(getID());
            if(seenAtributes == null) {
                seenAtributes = new HashMap<String, String>();
            }
        }
        return seenAtributes;
    }

    /**
     * Returns available operations for this issue
     * @return
     */
    public Map<String, TaskOperation> getAvailableOperations () {
        if (availableOperations == null) {
            HashMap<String, TaskOperation> operations = new HashMap<String, TaskOperation>(5);
            List<TaskAttribute> allOperations = taskData.getAttributeMapper().getAttributesByType(taskData, TaskAttribute.TYPE_OPERATION);
            for (TaskAttribute operation : allOperations) {
                // the test must be here, 'operation' (applying writable action) is also among allOperations
                if (operation.getId().startsWith(TaskAttribute.PREFIX_OPERATION)) {
                    operations.put(operation.getId().substring(TaskAttribute.PREFIX_OPERATION.length()), TaskOperation.createFrom(operation));
                }
            }
            availableOperations = operations;
        }

        return availableOperations;
    }

    private class Controller extends BugtrackingController {
        private JComponent issuePanel;

        public Controller() {
            IssuePanel panel = new IssuePanel();
            panel.setIssue(NbJiraIssue.this);
            JScrollPane scrollPane = new JScrollPane(panel);
            scrollPane.setBorder(null);
            Font font = UIManager.getFont("Label.font"); // NOI18N
            if (font != null) {
                int size = (int)(font.getSize()*1.5);
                scrollPane.getHorizontalScrollBar().setUnitIncrement(size);
                scrollPane.getVerticalScrollBar().setUnitIncrement(size);
            }
            BugtrackingUtil.keepFocusedComponentVisible(scrollPane);
            issuePanel = scrollPane;

            refreshViewData();
        }

        @Override
        public JComponent getComponent() {
            return issuePanel;
        }

        @Override
        public boolean isValid() {
            return true; // PENDING
        }

        @Override
        public void applyChanges() {
        }

        private void refreshViewData() {
            // PENDING
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(org.netbeans.modules.jira.issue.NbJiraIssue.class);
        }
    }

    public static final class Comment {
        private final Date when;
        private final String who;
        private final Long number;
        private final String text;
        private final String url;

        public Comment(TaskAttribute a) {
            TaskCommentMapper comment = TaskCommentMapper.createFrom(a);
            when = comment.getCreationDate();
            IRepositoryPerson person = comment.getAuthor();
            who = person == null ? null : person.getName();
            number = a.getTaskData().getAttributeMapper().getLongValue(a.getMappedAttribute(TaskAttribute.COMMENT_NUMBER));
            url = comment.getUrl();
            text = comment.getText();
        }

        public Long getNumber() {
            return number;
        }

        public String getText() {
            return text;
        }

        public Date getWhen() {
            return when;
        }

        public String getWho() {
            return who;
        }

        public String getUrl () {
            return url;
        }
    }

    public final class Attachment {
        private final String filename;
        private final String author;
        private final Date date;
        private final String id;
        private String size;
        private String url;
        private final TaskAttribute attachmentAttribute;


        public Attachment(TaskAttribute ta) {
            attachmentAttribute = ta;
            TaskAttachmentMapper taskAttachment = TaskAttachmentMapper.createFrom(ta);
            id = taskAttachment.getAttachmentId();
            date = taskAttachment.getCreationDate();
            filename = taskAttachment.getFileName();
            IRepositoryPerson person = taskAttachment.getAuthor();
            author = person == null ? null : person.getName();
            size = JiraUtils.getMappedValue(ta, TaskAttribute.ATTACHMENT_SIZE);
            url = taskAttachment.getUrl();
        }

        public String getAuthor() {
            return author;
        }

        public Date getDate() {
            return date;
        }

        public String getFilename() {
            return filename;
        }

        public String getId() {
            return id;
        }

        public String getSize() {
            return size;
        }

        public String getUrl() {
            return url;
        }

        /**
         *
         * @param os is always closed before return
         */
        public void getAttachementData(final OutputStream os) {
            assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N
            JiraCommand cmd = new JiraCommand() {
                public void execute() throws CoreException, IOException {
                    if (Jira.LOG.isLoggable(Level.FINER)) {
                        Jira.LOG.finer("getAttachmentData: id: " + Attachment.this.getId() + ", issue: " + getKey());
                    }
                    try {
                        IssueTask task = new IssueTask(repository.getUrl(), NbJiraIssue.this.getTaskData().getTaskId(), "Attachment download task", getKey());
                        if (!Jira.getInstance().getRepositoryConnector().getTaskAttachmentHandler().canGetContent(repository.getTaskRepository(), task)) {
                            Jira.LOG.warning("getAttachmentData: cannot get content: id: " + Attachment.this.getId() + ", issue: " + getKey());
                            return;
                        }
                        InputStream is = Jira.getInstance().getRepositoryConnector().getTaskAttachmentHandler().getContent(repository.getTaskRepository(),
                                task, attachmentAttribute, new NullProgressMonitor());
                        if (is != null) {
                            JiraUtils.copyStreamsCloseAll(os, is);
                        }
                    } finally {
                        os.close();
                    }
                }
            };
            repository.getExecutor().execute(cmd);
        }
    }

    @Override
    public String toString() {
        return "[" + getKey() + ", " + getSummary() + "]";
    }

    private class IssueTask extends AbstractTask {
        private final String key;

        public IssueTask(String repositoryUrl, String taskId, String summary, String key) {
            super(repositoryUrl, taskId, summary);
            this.key = key;
        }

        @Override
        public boolean isLocal() {
            return true;
        }

        @Override
        public String getConnectorKind() {
            return super.getRepositoryUrl();
        }

        @Override
        public String getTaskKey() {
            return key;
        }
    }

    public static final class CustomField {
        private final String id;
        private final String label;
        private final String type;
        private List<String> values;
        private final boolean readOnly;

        private CustomField(TaskAttribute attribute) {
            id = attribute.getId().substring(IJiraConstants.ATTRIBUTE_CUSTOM_PREFIX.length());
            label = attribute.getMetaData().getValue(TaskAttribute.META_LABEL);
            type = attribute.getMetaData().getValue(IJiraConstants.META_TYPE);
            values = attribute.getValues();
            readOnly = attribute.getMetaData().isReadOnly();
        }

        public String getId() {
            return id;
        }

        public String getLabel() {
            return label;
        }

        public String getType() {
            return type;
        }

        public boolean isReadOnly () {
            return readOnly;
        }

        public List<String> getValues() {
            return values;
        }

        public void setValues (List<String> values) {
            this.values = values;
        }
    }

    public static final class WorkLog {
        private final Date startDate;
        private final String author;
        private final long timeSpent;
        private final String comment;

        public WorkLog(TaskAttribute workLogTA) {
            TaskAttributeMapper mapper = workLogTA.getTaskData().getAttributeMapper();
            startDate = mapper.getDateValue(workLogTA.getMappedAttribute(WorkLogConverter.START_DATE.key()));
            IRepositoryPerson person = mapper.getRepositoryPerson(workLogTA.getMappedAttribute(WorkLogConverter.AUTOR.key()));
            author = person == null ? null : person.getPersonId();
            comment = mapper.getValue(workLogTA.getMappedAttribute(WorkLogConverter.COMMENT.key()));
            Long timeSpentValue = mapper.getLongValue(workLogTA.getMappedAttribute(WorkLogConverter.TIME_SPENT.key()));
            this.timeSpent = timeSpentValue == null ? 0 : timeSpentValue.longValue();
        }

        public Date getStartDate () {
            return startDate;
        }

        /**
         *
         * @return author's ID
         */
        public String getAuthor () {
            return author;
        }

        /**
         *
         * @return spent time on the issue in seconds
         */
        public long getTimeSpent () {
            return timeSpent;
        }

        public String getComment () {
            return comment;
        }
    }
}
