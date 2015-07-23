/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 * single choicge of license, a recipient has the option to distribute
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

import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import org.eclipse.core.runtime.CoreException;
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
import org.netbeans.modules.bugtracking.issuetable.IssueNode;
import org.netbeans.modules.jira.Jira;
import org.netbeans.modules.bugtracking.spi.IssueController;
import org.netbeans.modules.bugtracking.issuetable.ColumnDescriptor;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.netbeans.modules.bugtracking.commons.AttachmentsPanel.AttachmentInfo;
import org.netbeans.modules.bugtracking.commons.TextUtils;
import org.netbeans.modules.bugtracking.commons.UIUtils;
import org.netbeans.modules.bugtracking.spi.IssueScheduleInfo;
import org.netbeans.modules.jira.JiraConfig;
import org.netbeans.modules.jira.client.spi.Component;
import org.netbeans.modules.jira.client.spi.IssueType;
import org.netbeans.modules.jira.client.spi.JiraConnectorSupport;
import org.netbeans.modules.jira.client.spi.JiraConstants;
import org.netbeans.modules.jira.client.spi.JiraStatus;
import org.netbeans.modules.jira.client.spi.JiraWorkLog;
import static org.netbeans.modules.jira.client.spi.JiraWorkLog.AdjustEstimateMethod.LEAVE;
import static org.netbeans.modules.jira.client.spi.JiraWorkLog.AdjustEstimateMethod.REDUCE;
import static org.netbeans.modules.jira.client.spi.JiraWorkLog.AdjustEstimateMethod.SET;
import org.netbeans.modules.jira.client.spi.Priority;
import org.netbeans.modules.jira.client.spi.Project;
import org.netbeans.modules.jira.client.spi.Resolution;
import org.netbeans.modules.jira.client.spi.User;
import org.netbeans.modules.jira.client.spi.Version;
import org.netbeans.modules.jira.repository.JiraConfiguration;
import org.netbeans.modules.jira.repository.JiraRepository;
import org.netbeans.modules.jira.util.JiraUtils;
import org.netbeans.modules.mylyn.util.AbstractNbTaskWrapper;
import org.netbeans.modules.mylyn.util.BugtrackingCommand;
import org.netbeans.modules.mylyn.util.MylynSupport;
import org.netbeans.modules.mylyn.util.NbTask;
import org.netbeans.modules.mylyn.util.NbTask.SynchronizationState;
import org.netbeans.modules.mylyn.util.NbTaskDataModel;
import org.netbeans.modules.mylyn.util.NbTaskDataState;
import org.netbeans.modules.mylyn.util.commands.PostAttachmentCommand;
import org.netbeans.modules.mylyn.util.commands.SubmitTaskCommand;
import org.netbeans.modules.mylyn.util.commands.SynchronizeTasksCommand;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka, Jan Stola
 */
public class NbJiraIssue extends AbstractNbTaskWrapper {
    private JiraRepository repository;
    private Controller controller;

    static final String LABEL_NAME_ID               = "jira.issue.id";          // NOI18N
    static final String LABEL_NAME_TYPE             = "jira.issue.type";        // NOI18N
    static final String LABEL_NAME_PRIORITY         = "jira.issue.priority";    // NOI18N
    static final String LABEL_NAME_STATUS           = "jira.issue.status";      // NOI18N
    static final String LABEL_NAME_RESOLUTION       = "jira.issue.resolution";  // NOI18N    
    static final String LABEL_NAME_ASSIGNED_TO      = "jira.issue.assigned";    // NOI18N

    static final String LABEL_NAME_PROJECT          = "jira.issue.project";     // NOI18N
    static final String LABEL_NAME_COMPONENTS       = "jira.issue.components";  // NOI18N
    static final String LABEL_NAME_AFFECTS_VERSION  = "jira.issue.affectsversion"; // NOI18N
    static final String LABEL_NAME_FIX_VERSION      = "jira.issue.fixversion";  // NOI18N
    static final String LABEL_NAME_CREATED          = "jira.issue.created";     // NOI18N
    static final String LABEL_NAME_UPDATED          = "jira.issue.updated";     // NOI18N
    static final String LABEL_NAME_DUE              = "jira.issue.due";         // NOI18N
    static final String LABEL_NAME_ESTIMATE         = "jira.issue.estimate";    // NOI18N
    static final String LABEL_NAME_INITIAL_ESTIMATE = "jira.issue.initestimte"; // NOI18N
    static final String LABEL_NAME_TIME_SPENT       = "jira.issue.timespent";   // NOI18N

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";               // NOI18N
    private static final int SHORTENED_SUMMARY_LENGTH = 22;

    private static final String FIXED = "Fixed";                        //NOI18N

    /**
     * IssueProvider wasn't seen yet
     */
    static final int FIELD_STATUS_IRELEVANT = -1;

    /**
     * Field wasn't changed since the issue was seen the last time
     */
    static final int FIELD_STATUS_UPTODATE = 1;

    /**
     * Field was changed since the issue was seen the last time
     */
    static final int FIELD_STATUS_MODIFIED = 2;

    /**
     * Field was changed since the issue was seen the last time
     */
    static final int FIELD_STATUS_OUTGOING = 4;

    /**
     * Field was changed both locally and in repository
     */
    static final int FIELD_STATUS_CONFLICT = FIELD_STATUS_MODIFIED | FIELD_STATUS_OUTGOING;
    
    private String recentChanges = "";
    private String tooltip = "";
    private boolean open;
    private static final String NB_WORK_LOGNEW_ESTIMATE_TIME = "NB.WorkLog.newEstimateTime"; //NOI18N

    private static final URL ICON_REMOTE_PATH = IssuePanel.class.getClassLoader().getResource("org/netbeans/modules/jira/resources/remote.png"); //NOI18N
    private static final URL ICON_CONFLICT_PATH = IssuePanel.class.getClassLoader().getResource("org/netbeans/modules/jira/resources/conflict.png"); //NOI18N
    private static final URL ICON_UNSUBMITTED_PATH = IssuePanel.class.getClassLoader().getResource("org/netbeans/modules/jira/resources/unsubmitted.png"); //NOI18N
    private boolean loading;
   
    private final static JiraConstants jiraConstants = JiraConnectorSupport.getInstance().getConnector().getJiraConstants();;
    
    @Override
    protected void taskDeleted (NbTask task) {
        getRepository().taskDeleted(getKey(task));
    }

    @Override
    protected void attributeChanged (NbTaskDataModel.NbTaskDataModelEvent event, NbTaskDataModel model) {
        if (controller != null) {
            // view might not exist yet and we won't unnecessarily create it
            controller.modelStateChanged(model.isDirty(), model.isDirty() || !model.getChangedAttributes().isEmpty());
        }
    }

    @Override
    protected void modelSaved (NbTaskDataModel model) {
        if (controller != null) {
            controller.modelStateChanged(model.isDirty(), model.hasOutgoingChanged());
        }
    }

    @Override
    protected String getSummary (TaskData taskData) {
        return getFieldValue(taskData, IssueField.SUMMARY);
    }

    @Override
    protected void taskDataUpdated () {
        availableOperations = null;
        Jira.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                dataChanged();
            }
        });
    }

    private void dataChanged () {
        updateTooltip();
        fireDataChanged();
        refreshViewData(false);
    }

    @Override
    protected void taskModified (boolean syncStateChanged) {
        boolean seen = isSeen();
        if (updateRecentChanges() | updateTooltip()) {
            fireDataChanged();
        }
        if (syncStateChanged) {
            fireStatusChanged();
        }
    }

    @Override
    protected void repositoryTaskDataLoaded (TaskData repositoryTaskData) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run () {
                if (updateTooltip()) {
                    fireDataChanged();
                }
            }
        });
    }
    
    @Override
    protected boolean synchronizeTask () {
        try {
            NbTask task = getNbTask();
            synchronized (task) {
                SynchronizeTasksCommand cmd = MylynSupport.getInstance().getCommandFactory().createSynchronizeTasksCommand(
                        getRepository().getTaskRepository(), Collections.<NbTask>singleton(task));
                getRepository().getExecutor().execute(cmd);
                return !cmd.hasFailed();
            }
        } catch (CoreException ex) {
            // should not happen
            Jira.LOG.log(Level.WARNING, null, ex);
            return false;
        }
    }

    boolean save () {
        return saveChanges();
    }
    
    void markUserChange () {
        if (isMarkedNewUnread()) {
            markNewRead();
        }
    }

    void delete () {
        deleteTask();
    }
    
    void setTaskPrivateNotes (String notes) {
        super.setPrivateNotes(notes);
        if (controller != null) {
            controller.modelStateChanged(true, hasLocalEdits());
        }
    }

    void fireChangeEvent () {
        fireChanged();
    }
    
    public void setTaskDueDate (final Date date, final boolean persistChange) {
        runWithModelLoaded(new Runnable() {

            @Override
            public void run () {
                setDueDateAndSubmit(date);
            }
        });
    }
    
    public void setTaskScheduleDate (IssueScheduleInfo date, boolean persistChange) {
        super.setScheduleDate(date, persistChange);
        if (controller != null) {
            controller.modelStateChanged(hasUnsavedChanges(), hasLocalEdits());
        }
        if (persistChange) {
            dataChanged();
        }
    }

    public void setTaskEstimate (int estimate, boolean persistChange) {
        super.setEstimate(estimate, persistChange);
        if (controller != null) {
            controller.modelStateChanged(hasUnsavedChanges(), hasLocalEdits());
        }
        if (persistChange) {
            dataChanged();
        }
    }
    
    private void setDueDateAndSubmit (final Date date) {
        refresh();
        runWithModelLoaded(new Runnable() {
            @Override
            public void run () {
                if (date == null) {
                    setFieldValue(IssueField.DUE, ""); //NOI18N
                } else {
                    setFieldValue(IssueField.DUE, String.valueOf(date.getTime()));
                }
                submitAndRefresh();
            }
        });
    }
    
    public boolean discardLocalEdits () {
        final boolean retval[] = new boolean[1];
        runWithModelLoaded(new Runnable() {
            @Override
            public void run () {
                clearUnsavedChanges();
                retval[0] = cancelChanges();
                if (controller != null) {
                    controller.modelStateChanged(hasUnsavedChanges(), hasLocalEdits());
                    controller.refreshViewData(false);
                }
            }
        });
        return retval[0];
    }

    NbJiraIssue createSubtask () {
        assert !EventQueue.isDispatchThread();
        NbTask task;
        try {
            task = MylynSupport.getInstance().createSubtask(getNbTask());
            return repository.getIssueForTask(task);
        } catch (CoreException ex) {
            Jira.LOG.log(Level.WARNING, null, ex);
            return null;
        }
    }

    NewWorkLog getEditedWorkLog () {
        NbTaskDataModel model = getModel();
        TaskData td = model == null ? null : model.getLocalTaskData();
        if (td != null) {
            TaskAttribute ta = td.getRoot().getMappedAttribute(jiraConstants.getWorkLogConverter_ATTRIBUTE_WORKLOG_NEW());
            if (ta != null) {
                return new NewWorkLog(ta);
            }
        }
        return null;
    }

    boolean setUnsubmittedAttachments (List<AttachmentInfo> newAttachments) {
        return super.setNewAttachments(newAttachments);
    }

    List<AttachmentInfo> getUnsubmittedAttachments () {
        return getNewAttachments();
    }


    public String getPriorityID() {
        final Priority priority = getPriority();
        return priority != null ? priority.getId() : null;
    }
    
    int getSortOrder() {
        return getSortOrder(getPriority());
    }

    private int getSortOrder(Priority priority) {
        return repository.getConfiguration().getPrioritySortOrder(priority);
    }
    
    public enum IssueField {
        KEY(jiraConstants.getJiraAttribute_ISSUE_KEY_id(), "LBL_KEY"),
        SUMMARY(jiraConstants.getJiraAttribute_SUMMARY_id(), "LBL_SUMMARY"),
        DESCRIPTION(jiraConstants.getJiraAttribute_DESCRIPTION_id(), "LBL_DESCRIPTION"),
        STATUS(TaskAttribute.STATUS, "LBL_STATUS"),
        PRIORITY(jiraConstants.getJiraAttribute_PRIORITY_id(), "LBL_PRIORITY"),
        RESOLUTION(jiraConstants.getJiraAttribute_RESOLUTION_id(), "LBL_RESOLUTION"),
        PROJECT(jiraConstants.getJiraAttribute_PROJECT_id(), "LBL_PROJECT"),
        COMPONENT(jiraConstants.getJiraAttribute_COMPONENTS_id(), "LBL_COMPONENT", false),
        AFFECTSVERSIONS(jiraConstants.getJiraAttribute_AFFECTSVERSIONS_id(),"LBL_AFFECTSVERSIONS", false),
        FIXVERSIONS(jiraConstants.getJiraAttribute_FIXVERSIONS_id(), "LBL_FIXVERSIONS", false),
        ENVIRONMENT(jiraConstants.getJiraAttribute_ENVIRONMENT_id(), "LBL_ENVIRONMENT"),
        REPORTER(jiraConstants.getJiraAttribute_USER_REPORTER_id(), "LBL_REPORTER"),
        ASSIGNEE(jiraConstants.getJiraAttribute_USER_ASSIGNED_id(), "LBL_ASSIGNED_TO"),
        TYPE(jiraConstants.getJiraAttribute_TYPE_id(), "LBL_TYPE"),
        CREATION(jiraConstants.getJiraAttribute_CREATION_DATE_id(), null),
        MODIFICATION(jiraConstants.getJiraAttribute_MODIFICATION_DATE_id(), null),
        DUE(jiraConstants.getJiraAttribute_DUE_DATE_id(), "LBL_DUE"),
        ESTIMATE(jiraConstants.getJiraAttribute_ESTIMATE_id(), "LBL_ESTIMATE"),
        INITIAL_ESTIMATE(jiraConstants.getJiraAttribute_INITIAL_ESTIMATE_id(), "LBL_INITIAL_ESTIMATE"),
        ACTUAL(jiraConstants.getJiraAttribute_ACTUAL_id(), "LBL_ACTUALL"),
        PARENT_ID(jiraConstants.getJiraAttribute_PARENT_ID_id(), null),
        PARENT_KEY(jiraConstants.getJiraAttribute_PARENT_KEY_id(), null),
        SUBTASK_IDS(jiraConstants.getJiraAttribute_SUBTASK_IDS_id(), null, false),
        SUBTASK_KEYS(jiraConstants.getJiraAttribute_SUBTASK_KEYS_id(), null, false),
        COMMENT_COUNT(TaskAttribute.TYPE_COMMENT, null, false),
        COMMENT(TaskAttribute.COMMENT_NEW, null),
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
    private WeakReference<JiraIssueNode> nodeRef;
    
    public NbJiraIssue (NbTask task, JiraRepository repo) {
        super(task);
        this.repository = repo;
        updateRecentChanges();
        updateTooltip();
    }
 
    void opened() {
        if(Jira.LOG.isLoggable(Level.FINE)) Jira.LOG.log(Level.FINE, "issue {0} open start", new Object[] {getKey()});
        open = true;
        loading = true;
        Jira.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run () {
                if (editorOpened()) {
                    loading = false;
                    refreshViewData(true);
                } else {
                    // should close somehow
                }
            }
        });
        String refresh = System.getProperty("org.netbeans.modules.bugzilla.noIssueRefresh"); // NOI18N
        if(refresh != null && refresh.equals("true")) {                                      // NOI18N
            return;
        }
        if(Jira.LOG.isLoggable(Level.FINE)) Jira.LOG.log(Level.FINE, "issue {0} open finish", new Object[] {getKey()});
    }

    void closed() {
        if(Jira.LOG.isLoggable(Level.FINE)) Jira.LOG.log(Level.FINE, "issue {0} close start", new Object[] {getKey()});
        open = false;
        Jira.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run () {
                editorClosed();
            }
        });
        if(Jira.LOG.isLoggable(Level.FINE)) Jira.LOG.log(Level.FINE, "issue {0} close finish", new Object[] {getKey()});
    }

    public JiraRepository getRepository() {
        return repository;
    }

    public String getKey() {
        return getKey(getNbTask());
    }
    
    public static String getKey (NbTask task) {
        if (task.getSynchronizationState() == NbTask.SynchronizationState.OUTGOING_NEW) {
            return "-" + task.getTaskId();
        }
        return task.getTaskKey();
    }
    
    public String getDescription() {
        return getRepositoryFieldValue(IssueField.DESCRIPTION);
    }

    IssueType getType() {
        String id = getRepositoryFieldValue(IssueField.TYPE);
        return repository.getConfiguration().getIssueTypeById(id);
    }

    Priority getPriority() {
        String id = getRepositoryFieldValue(IssueField.PRIORITY);
        return repository.getConfiguration().getPriorityById(id);
    }

    public JiraStatus getJiraStatus() {
        String id = getRepositoryFieldValue(IssueField.STATUS);
        return repository.getConfiguration().getStatusById(id);
    }

    public Resolution getResolution() {
        String id = getRepositoryFieldValue(IssueField.RESOLUTION);
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
        return getRepositoryFieldValue(IssueField.PARENT_KEY);
    }

    public List<String> getSubtaskKeys() {
        return getRepositoryFieldValues(IssueField.SUBTASK_KEYS);
    }

    public String getParentID() {
        return getRepositoryFieldValue(IssueField.PARENT_ID);
    }

    public List<String> getSubtaskID() {
        return getRepositoryFieldValues(IssueField.SUBTASK_IDS);
    }

    public Comment[] getComments() {
        NbTaskDataModel m = getModel();
        List<TaskAttribute> attrs = m == null ? null : m.getLocalTaskData()
                .getAttributeMapper().getAttributesByType(m.getLocalTaskData(), TaskAttribute.TYPE_COMMENT);
        if (attrs == null) {
            return new Comment[0];
        }
        List<Comment> comments = new ArrayList<Comment>(attrs.size());
        for (TaskAttribute taskAttribute : attrs) {
            comments.add(new Comment(taskAttribute));
        }
        return comments.toArray(new Comment[comments.size()]);
    }

    public Attachment[] getAttachments() {
        NbTaskDataModel m = getModel();
        List<TaskAttribute> attrs = m == null ? null : m.getLocalTaskData()
                .getAttributeMapper().getAttributesByType(m.getLocalTaskData(), TaskAttribute.TYPE_ATTACHMENT);
        if (attrs == null) {
            return new Attachment[0];
        }
        List<Attachment> attachments = new ArrayList<Attachment>(attrs.size());
        for (TaskAttribute taskAttribute : attrs) {
            attachments.add(new Attachment(taskAttribute));
        }
        return attachments.toArray(new Attachment[attachments.size()]);
    }

    public CustomField[] getCustomFields () {
        NbTaskDataModel m = getModel();
        Map<String, TaskAttribute> attrs = m == null ? null : m.getLocalTaskData().getRoot().getAttributes();
        if (attrs == null) {
            return new CustomField[0];
        }
        List<CustomField> fields = new ArrayList<CustomField>(10);
        TaskAttribute[] attrValues = attrs.values().toArray(new TaskAttribute[attrs.size()]);
        
        for (TaskAttribute attribute : attrValues) {
            String prefix = jiraConstants.getATTRIBUTE_CUSTOM_PREFIX();
            if (attribute.getId().startsWith(prefix)) {
                CustomField field = new CustomField(attribute);
                fields.add(field);
            }
        }
        return fields.toArray(new CustomField[fields.size()]);
    }

    void setCustomField(CustomField customField) {
        NbTaskDataModel m = getModel();
        Map<String, TaskAttribute> attrs = m == null ? null : m.getLocalTaskData().getRoot().getAttributes();
        if (attrs == null) {
            return;
        }
        for (TaskAttribute attribute : attrs.values()) {
            if (attribute.getId().startsWith(jiraConstants.getATTRIBUTE_CUSTOM_PREFIX())
                    && customField.getId().equals(attribute.getId())) {
                setTaskAttributeValues(m, attribute, customField.getValues());
            }
        }
    }

    LinkedIssue[] getLinkedIssues() {
        NbTaskDataModel m = getModel();
        Map<String, TaskAttribute> attrs = m == null ? null : m.getLocalTaskData().getRoot().getAttributes();
        if (attrs == null) {
            return new LinkedIssue[0];
        }
        List<LinkedIssue> linkedIssues = new ArrayList<LinkedIssue>();

        for (TaskAttribute attribute : attrs.values()) {
            if (attribute.getId().startsWith(jiraConstants.getATTRIBUTE_LINK_PREFIX())) {
                LinkedIssue linkedIssue = new LinkedIssue(attribute);
                linkedIssues.add(linkedIssue);
            }
        }
        return linkedIssues.toArray(new LinkedIssue[linkedIssues.size()]);
    }

    /**
     * Returns an array of worklogs under the issue.
     * @return
     */
    public WorkLog[] getWorkLogs () {
        NbTaskDataModel m = getModel();
        List<TaskAttribute> attrs = m == null ? null : m.getLocalTaskData()
                .getAttributeMapper().getAttributesByType(m.getLocalTaskData(), jiraConstants.getWorkLogConverter_TYPE_WORKLOG());
        if (attrs == null) {
            return new WorkLog[0];
        }
        List<WorkLog> workLogs = new ArrayList<WorkLog>(attrs.size());
        for (TaskAttribute taskAttribute : attrs) {
            if (!jiraConstants.getWorkLogConverter_ATTRIBUTE_WORKLOG_NEW().equals(taskAttribute.getId())) {
                workLogs.add(new WorkLog(taskAttribute));
            }
        }
        return workLogs.toArray(new WorkLog[workLogs.size()]);
    }

    /**
     * Adds a new worklog. Just one worklog can be added before committing the issue.
     * Don't forget to commit the issue.
     */
    public void addWorkLog (NewWorkLog log) {
        NbTaskDataModel m = getModel();
        TaskData taskData = m == null ? null : m.getLocalTaskData();
        if (taskData == null) {
            return;
        }
        TaskAttribute attribute = taskData.getRoot().getMappedAttribute(jiraConstants.getWorkLogConverter_ATTRIBUTE_WORKLOG_NEW());
        if (!log.isToSubmit()) {
            if (attribute != null && !attribute.getAttributes().isEmpty()) {
                attribute.clearAttributes();
                m.attributeChanged(attribute);
            }
        } else if (log.getStartDate() != null) {
            if (attribute == null) {
                attribute = taskData.getRoot().createMappedAttribute(jiraConstants.getWorkLogConverter_ATTRIBUTE_WORKLOG_NEW());
            }
            JiraWorkLog workLog = JiraConnectorSupport.getInstance().getConnector().createWorkLog();
            workLog.setComment(log.getComment());
            workLog.setStartDate(log.getStartDate());
            workLog.setTimeSpent(log.getTimeSpent());
            if (log.isAutoAdjust()) {
                workLog.setAdjustEstimate(JiraWorkLog.AdjustEstimateMethod.AUTO);
            } else if (log.isLeaveEstimate()) {
                workLog.setAdjustEstimate(JiraWorkLog.AdjustEstimateMethod.LEAVE);
            } else if (log.isReduceEstimate()) {
                workLog.setAdjustEstimate(JiraWorkLog.AdjustEstimateMethod.REDUCE);
            } else if (log.isSetEstimate()) {
                workLog.setAdjustEstimate(JiraWorkLog.AdjustEstimateMethod.SET);
            }
            workLog.applyTo(attribute);
            attribute.createMappedAttribute(jiraConstants.getWorkLogConverter_ATTRIBUTE_WORKLOG_NEW_SUBMIT_FLAG()).setValue("true"); //NOI18N
            attribute.createMappedAttribute(NB_WORK_LOGNEW_ESTIMATE_TIME).setValue(String.valueOf(log.getEstimatedTime()));
            m.attributeChanged(attribute);
        }
    }

    /**
     * Reloads the task data
     * @return true if successfully refreshed
     */
    public boolean refresh () {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N
        return refresh(false);
    }

    public IssueStatusProvider.Status getIssueStatus() {
        return getStatus();
    }

    public void setUpToDate(boolean seen) {
        setUpToDate(seen, true);
    }    
    
    /**
     * Reloads the task data and refreshes the issue cache
     * @param key key of the issue
     * @return true if successfully refreshed
     */
    private boolean refresh (boolean afterSubmitRefresh) { // XXX cacheThisIssue - we probalby don't need this, just always set the issue into the cache
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N
        NbTask task = getNbTask();
        boolean synced = synchronizeTask();
        assert this == getRepository().getIssueForTask(task);
        if (!loading) {
            // refresh only when model is not currently being loaded
            // otherwise it most likely ends up in editor not fully initialized
            refreshViewData(afterSubmitRefresh);
        }
        return synced;
    }

    /**
     * Tries to update the taskdata and set issue to Resolved:resolution.
     * <strong>Do not forget to submit the issue</strong>
     * @param resolution
     * @param comment can be null, in such case no comment will be set
     * @throws org.eclipse.mylyn.internal.jira.core.service.JiraException
     * @throws java.lang.IllegalStateException if resolve operation is not permitted for this issue
     */
    public void resolve (final Resolution resolution, final String comment) {
        if (Jira.LOG.isLoggable(Level.FINE)) {
            Jira.LOG.log(Level.FINE, "{0}: resolve issue {1}: {2}", new Object[]{getClass().getName(), getKey(), resolution.getName()});    //NOI18N
        }
        assert !isNew();
        runWithModelLoaded(new Runnable() {
            @Override
            public void run () {
                NbTaskDataModel model = getModel();
                TaskAttribute rta = model.getLocalTaskData().getRoot();
                TaskOperation operation = getResolveOperation();
                if (operation == null) {
                    throw new IllegalStateException("Resolve operation not permitted"); //NOI18N
                } else {
                    setOperation(operation);
                }
                setTaskAttributeValue(model, rta.getMappedAttribute(TaskAttribute.RESOLUTION), resolution.getId());
                addComment(comment);
            }
        });
    }

    /**
     * Tries to update the taskdata and set issue to Open.
     * <strong>Do not forget to submit the issue</strong>
     * @param comment can be null, in such case no comment will be set
     * @throws org.eclipse.mylyn.internal.jira.core.service.JiraException
     * @throws java.lang.IllegalStateException if resolve operation is not permitted for this issue
     */
    public void reopen (final String comment) {
        if (Jira.LOG.isLoggable(Level.FINE)) {
            Jira.LOG.log(Level.FINE, "{0}: reopening issue{1}", new Object[]{getClass().getName(), getKey()}); //NOI18N
        }
        assert !isNew();
        runWithModelLoaded(new Runnable() {
            @Override
            public void run () {
                Map<String, TaskOperation> operations = getAvailableOperations();
                TaskOperation operation = null;
                for (Map.Entry<String, TaskOperation> entry : operations.entrySet()) {
                    String operationLabel = entry.getValue().getLabel();
                    if (Jira.LOG.isLoggable(Level.FINEST)) {
                        Jira.LOG.log(Level.FINEST, "{0}: reopening issue{1}: available operation: {2}({3})", new Object[]{getClass().getName(), getKey(), operationLabel, entry.getValue().getOperationId()}); //NOI18N
                    }
                    if (JiraUtils.isReopenOperation(operationLabel)) {
                        operation = entry.getValue();
                        break;
                    } else if (JiraUtils.isLeaveOperation(entry.getValue())) {
                        // falback on leave operation. Should check if the original status was reopened to be presize
                        operation = entry.getValue();
                    }
                }
                if (operation == null) {
                    throw new IllegalStateException("Reopen operation not permitted"); //NOI18N
                } else {
                    setOperation(operation);
                }
                setFieldValue(IssueField.RESOLUTION, ""); //NOI18N
                addComment(comment);
            }
        });
    }

    /**
     * Tries to update the taskdata and set issue to closed:resolution.
     * <strong>Do not forget to submit the issue</strong>
     * @param resolution
     * @param comment can be null, in such case no comment will be set
     * @throws org.eclipse.mylyn.internal.jira.core.service.JiraException
     * @throws java.lang.IllegalStateException if resolve operation is not permitted for this issue
     */
    public void close(final Resolution resolution, final String comment) {
        if (Jira.LOG.isLoggable(Level.FINE)) {
            Jira.LOG.log(Level.FINE, "{0}: close issue {1}: {2}", new Object[]{getClass().getName(), getKey(), resolution.getName()});    //NOI18N
        }
        assert !isNew();
        runWithModelLoaded(new Runnable() {
            @Override
            public void run () {
                NbTaskDataModel model = getModel();
                TaskAttribute rta = model.getLocalTaskData().getRoot();

                Map<String, TaskOperation> operations = getAvailableOperations();
                TaskOperation operation = null;
                for (Map.Entry<String, TaskOperation> entry : operations.entrySet()) {
                    String operationLabel = entry.getValue().getLabel();
                    if (Jira.LOG.isLoggable(Level.FINEST)) {
                        Jira.LOG.log(Level.FINEST, "{0}: closing issue{1}: available operation: {2}({3})", //NOI18N
                                new Object[]{getClass().getName(), getKey(), operationLabel,
                                    entry.getValue().getOperationId()});
                    }
                    if (JiraUtils.isCloseOperation(operationLabel)) {
                        operation = entry.getValue();
                        break;
                    } else if (JiraUtils.isLeaveOperation(entry.getValue())) {
                        // falback on leave operation. Should check if the original status was closed to be presize
                        operation = entry.getValue();
                    }
                }
                if (operation == null) {
                    throw new IllegalStateException("Close operation not permitted"); //NOI18N
                } else {
                    setOperation(operation);
                }
                setTaskAttributeValue(model, rta.getMappedAttribute(TaskAttribute.RESOLUTION), resolution.getId());
                addComment(comment);
            }
        });
    }

    private void setTaskAttributeValue (NbTaskDataModel model, TaskAttribute ta, String value) {
        TaskData repositoryTaskData = model.getRepositoryTaskData();
        if (value.isEmpty() && repositoryTaskData != null) {
            // should be empty or set to ""???
            TaskAttribute a = repositoryTaskData.getRoot().getAttribute(ta.getId());
            if (a == null || a.getValues().isEmpty()) {
                // repository value is also empty list, so let's set to the same
                ta.clearValues();
            } else {
                ta.setValue(value);
            }
        } else {
            ta.setValue(value);
        }
        model.attributeChanged(ta);
    }

    private void setTaskAttributeValues (NbTaskDataModel model, TaskAttribute ta, List<String> values) {
        TaskData repositoryTaskData = model.getRepositoryTaskData();
        if (values.isEmpty() && repositoryTaskData != null) {
            // should be empty or set to ""???
            TaskAttribute a = repositoryTaskData.getRoot().getAttribute(ta.getId());
            if (a == null || a.getValues().isEmpty()) {
                // repository value is also empty list, so let's set to the same
                ta.clearValues();
            } else {
                ta.setValues(values);
            }
        } else {
            ta.setValues(values);
        }
        model.attributeChanged(ta);
    }

    /**
     * Tries to update the taskdata and set issue to started.
     * <strong>Do not forget to submit the issue</strong>
     * @throws org.eclipse.mylyn.internal.jira.core.service.JiraException
     * @throws java.lang.IllegalStateException if resolve operation is not permitted for this issue
     */
    public void startProgress() {
        if (Jira.LOG.isLoggable(Level.FINE)) {
            Jira.LOG.log(Level.FINE, "{0}: starting issue {1}", new Object[]{getClass().getName(), getKey()});    //NOI18N
        }
        assert !isNew();
        runWithModelLoaded(new Runnable() {
            @Override
            public void run () {
                Map<String, TaskOperation> operations = getAvailableOperations();
                TaskOperation operation = null;
                for (Map.Entry<String, TaskOperation> entry : operations.entrySet()) {
                    String operationLabel = entry.getValue().getLabel();
                    if (Jira.LOG.isLoggable(Level.FINEST)) {
                        Jira.LOG.log(Level.FINEST, "{0}: starting issue{1}: available operation: {2}({3})", new Object[]{getClass().getName(), getKey(), operationLabel, entry.getValue().getOperationId()}); //NOI18N
                    }
                    if (JiraUtils.isStartProgressOperation(operationLabel)) {
                        operation = entry.getValue();
                        break;
                    } else if (JiraUtils.isLeaveOperation(entry.getValue())) {
                        // falback on leave operation. Should check if the original status was inprogress to be presize
                        operation = entry.getValue();
                    }
                }
                if (operation == null) {
                    throw new IllegalStateException("Start progress operation not permitted"); //NOI18N
                } else {
                    setOperation(operation);
                }
                setFieldValue(IssueField.RESOLUTION, ""); //NOI18N
            }
        });
    }

    /**
     * Tries to update the taskdata and stops the progress.
     * <strong>Do not forget to submit the issue</strong>
     * @throws org.eclipse.mylyn.internal.jira.core.service.JiraException
     * @throws java.lang.IllegalStateException if resolve operation is not permitted for this issue
     */
    public void stopProgress() {
        if (Jira.LOG.isLoggable(Level.FINE)) {
            Jira.LOG.log(Level.FINE, "{0}: starting issue {1}", new Object[]{getClass().getName(), getKey()});    //NOI18N
        }
        assert !isNew();
        runWithModelLoaded(new Runnable() {
            @Override
            public void run () {
                Map<String, TaskOperation> operations = getAvailableOperations();
                TaskOperation operation = null;
                for (Map.Entry<String, TaskOperation> entry : operations.entrySet()) {
                    String operationLabel = entry.getValue().getLabel();
                    if (Jira.LOG.isLoggable(Level.FINEST)) {
                        Jira.LOG.log(Level.FINEST, "{0}: starting issue{1}: available operation: {2}({3})", new Object[]{getClass().getName(), getKey(), operationLabel, entry.getValue().getOperationId()}); //NOI18N
                    }
                    if (JiraUtils.isStopProgressOperation(operationLabel)) {
                        operation = entry.getValue();
                        break;
                    } else if (JiraUtils.isLeaveOperation(entry.getValue())) {
                        // falback on leave operation. Should check if the original status was open to be presize
                        operation = entry.getValue();
                    }
                }
                if (operation == null) {
                    throw new IllegalStateException("Stop progress operation not permitted"); //NOI18N
                } else {
                    setOperation(operation);
                }
                setFieldValue(IssueField.RESOLUTION, ""); //NOI18N
            }
        });
        
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
                Jira.LOG.log(Level.FINEST, "{0}: setOperation: operation {1}({2}) is available", new Object[]{getClass().getName(), operationLabel, entry.getValue().getOperationId()});
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
        NbTaskDataModel m = getModel();
        TaskAttribute rta = m.getLocalTaskData().getRoot();
        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.OPERATION);
        m.getLocalTaskData().getAttributeMapper().setTaskOperation(ta, operation);
        m.attributeChanged(ta);
    }

    public String getDisplayName() {
        return getDisplayName(getNbTask());
    }

    public static String getDisplayName(NbTask task) {
        return task.getSynchronizationState() == NbTask.SynchronizationState.OUTGOING_NEW
                ? task.getSummary()
                : NbBundle.getMessage(NbJiraIssue.class, "CTL_Issue", new Object[] {getKey(task), task.getSummary()}); // NOI18N
    }

    public String getShortenedDisplayName() {
        if (isNew()) {
            return getDisplayName();
        }

        String shortSummary = TextUtils.shortenText(getSummary(),
                                                    2,    //try at least 2 words
                                                    SHORTENED_SUMMARY_LENGTH);
        return NbBundle.getMessage(NbJiraIssue.class,
                                   "CTL_Issue",                         //NOI18N
                                   new Object[] {getKey(), shortSummary});
    }

    public String getTooltip() {
        return tooltip;
    }

    public IssueNode getNode() {
        JiraIssueNode n = nodeRef != null ? nodeRef.get() : null;
        if(n == null) {
            n = new JiraIssueNode(this);
            nodeRef = new WeakReference<>(n);
        }
        return n;
    }

    // XXX carefull - implicit double refresh
    public void addComment(String comment, boolean close) {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N
        if (Jira.LOG.isLoggable(Level.FINE)) {
            Jira.LOG.log(Level.FINE, "{0}: adding comment to issue: {1}", new Object[]{getClass().getName(), getKey()});    //NOI18N
        }        
        if(comment == null && !close) {
            // nothing to do => so don't even refresh
            return;
        }
        refresh();
        
        // check if not already resolved
        if(close && getResolution() != null) {
            close = false; // already resolved 
        }
        if(comment == null && !close) {
            // so there is nothing to do 
            // if already closed and no comment set
            return;
        }
        
        // resolved attrs
        if(close) {
            try {
                resolve(JiraUtils.getResolutionByName(repository, FIXED), comment); // XXX constant, what about setting in options?
            } catch (IllegalStateException ise) {
                // so do not set to close if already closed
                Jira.LOG.log(Level.INFO, "Close not permitted, current status is " + getJiraStatus().getName() + ", leaving status the same", ise);
            }
        } else {
            // comment must be added even when not resolving
            addComment(comment);
        }

        submitAndRefresh();
    }

    /**
     * Add comment to isseue.
     * <strong>Do not forget to submit</strong>
     * @param comment
     */
    public void addComment (final String comment) {
        if(comment != null && !comment.isEmpty()) {
            runWithModelLoaded(new Runnable() {
                @Override
                public void run () {
                    if (Jira.LOG.isLoggable(Level.FINE)) {
                        Jira.LOG.log(Level.FINE, "{0}: adding comment to issue {1}", new Object[]{getClass().getName(), getKey()});    //NOI18N
                    }
                    NbTaskDataModel model = getModel();
                    TaskAttribute ta = model.getLocalTaskData().getRoot().getMappedAttribute(IssueField.COMMENT.getKey());
                    String value = ta.getValue();
                    if (value == null || value.trim().isEmpty()) {
                        value = comment;
                    } else {
                        value += "\n\n" + comment; //NOI18N
                    }
                    setTaskAttributeValue(model, ta, value);
                }
            });
        }
    }

    public void addAttachment(File file, final String comment, String contentType) {
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
        TaskData repositoryTaskData = getRepositoryTaskData();
        if (repositoryTaskData == null && (!synchronizeTask()
                || (repositoryTaskData = getRepositoryTaskData()) == null)) {
            // not fully initialized task, sync failed
            return;            
        }
        final TaskAttribute attAttribute = new TaskAttribute(repositoryTaskData.getRoot(), TaskAttribute.TYPE_ATTACHMENT);
        mapper.applyTo(attAttribute);
        try {
            PostAttachmentCommand cmd = MylynSupport.getInstance().getCommandFactory().createPostAttachmentCommand(
                    repository.getTaskRepository(), getNbTask(), attAttribute, attachmentSource, comment);
            repository.getExecutor().execute(cmd);
            if (!cmd.hasFailed()) {
                refresh(true); // XXX to much refresh - is there no other way?
            }
        } catch (CoreException ex) {
            // should not happen
            Jira.LOG.log(Level.WARNING, null, ex);
        }
    }

    public void attachPatch(File file, String comment) {
        addAttachment(file, comment, null);
    }

    public IssueController getController() {
        if(controller == null) {
            controller = new Controller();
        }
        return controller;
    }
    
    public String getRecentChanges() {
        return recentChanges;
    }
    
    private boolean updateTooltip () {
        String displayName = getDisplayName();
        String oldTooltip = tooltip;
        SynchronizationState state = getSynchronizationState();
        URL iconPath = getStateIcon(state);
        String iconCode = "";
        if (iconPath != null) {
            iconCode = "<img src=\"" + iconPath + "\">&nbsp;"; //NOI18N
        }
        String stateName = getStateDisplayName(state);

        StringBuilder sb = new StringBuilder("<html>"); //NOI18N
        sb.append("<b>").append(displayName).append("</b><br>"); //NOI18N
        if (stateName != null && !stateName.isEmpty()) {
            sb.append("<p style=\"padding:5px;\">").append(iconCode).append(stateName).append("</p>"); //NOI18N
        }
        
        JiraConfiguration config = getRepository().getConfiguration();
        String projectId = getRepositoryFieldValue(IssueField.PROJECT);        
        if (config != null && !projectId.isEmpty()) {
            String projectLabel = NbBundle.getMessage(NbJiraIssue.class, "CTL_Issue_Project_Title"); //NOI18N
            String project = JiraUtils.toReadable(config, projectId, IssueField.PROJECT, projectId);
            
            String priorityLabel = NbBundle.getMessage(NbJiraIssue.class, "CTL_Issue_Priority_Title"); //NOI18N
            String priority = getRepositoryFieldValue(IssueField.PRIORITY);
            String priorityIcon = JiraConfig.getInstance().getPriorityIconURL(priority);
            priority = JiraUtils.toReadable(config, projectId, IssueField.PRIORITY, priority);

            String componentLabel = NbBundle.getMessage(NbJiraIssue.class, "CTL_Issue_Components_Title"); //NOI18N
            List<String> components = new ArrayList<>(getRepositoryFieldValues(IssueField.COMPONENT));
            for (ListIterator<String> it = components.listIterator(); it.hasNext();) {
                String value = it.next();
                it.set(JiraUtils.toReadable(config, projectId, IssueField.COMPONENT, value));
            }
            String component = JiraUtils.mergeValues(components);

            String typeLabel = NbBundle.getMessage(NbJiraIssue.class, "CTL_Issue_Type_Title"); //NOI18N
            String type = JiraUtils.toReadable(config, projectId, IssueField.TYPE, getRepositoryFieldValue(IssueField.TYPE));

            String assigneeLabel = NbBundle.getMessage(NbJiraIssue.class, "CTL_Issue_Assigned_Title"); //NOI18N
            String assignee = JiraUtils.toReadable(config, projectId, IssueField.ASSIGNEE, getRepositoryFieldValue(IssueField.ASSIGNEE));

            String statusLabel = NbBundle.getMessage(NbJiraIssue.class, "CTL_Issue_Status_Title"); //NOI18N
            String status = JiraUtils.toReadable(config, projectId, IssueField.STATUS, getRepositoryFieldValue(IssueField.STATUS));
            String resolution = JiraUtils.toReadable(config, projectId, IssueField.RESOLUTION, getRepositoryFieldValue(IssueField.RESOLUTION));

            if (resolution != null && !resolution.trim().isEmpty()) {
                status += "/" + resolution; //NOI18N
            }
            String scheduledLabel = NbBundle.getMessage(NbJiraIssue.class, "CTL_Issue_Scheduled_Title"); //NOI18N
            String scheduled = getScheduleDisplayString();

            String dueLabel = NbBundle.getMessage(NbJiraIssue.class, "CTL_Issue_Due_Title"); //NOI18N
            String due = getDueDisplayString();

            String estimateLabel = NbBundle.getMessage(NbJiraIssue.class, "CTL_Issue_Estimate_Title_Short"); //NOI18N
            String estimate = getEstimateDisplayString();

            String fieldTable = "<table>" //NOI18N
                    + "<tr><td><b>" + priorityLabel + ":</b></td><td><img src=\"" + priorityIcon + "\">&nbsp;" + priority + "</td><td style=\"padding-left:25px;\"><b>" + typeLabel + ":</b></td><td>" + type + "</td></tr>" //NOI18N
                    + "<tr><td><b>" + projectLabel + ":</b></td><td>" + project + "</td><td style=\"padding-left:25px;\"><b>" + componentLabel + ":</b></td><td>" + component + "</td></tr>" //NOI18N
                    + "<tr><td><b>" + assigneeLabel + ":</b></td><td colspan=\"3\">" + assignee + "</td></tr>"
                    + "<tr><td><b>" + statusLabel + ":</b></td><td colspan=\"3\">" + status + "</td></tr>"; //NOI18N

            if (!scheduled.isEmpty()) {
                fieldTable += "<tr><td><b>" + scheduledLabel + ":</b></td><td colspan=\"3\">" + scheduled + "</td></tr>"; //NOI18N
            }
            boolean addNewLine = !due.isEmpty() || !estimate.isEmpty();
            if (addNewLine) {
                fieldTable += "<tr>"; //NOI18N
            }
            if (!due.isEmpty()) {
                fieldTable += "<tr><td><b>" + dueLabel + ":</b></td><td>" + due + "</td>"; //NOI18N
            }
            if (!estimate.isEmpty()) {
                fieldTable += "<td style=\"padding-left:25px;\"><b>" + estimateLabel + ":</b></td><td>" + estimate + "</td>"; //NOI18N
            }
            if (addNewLine) {
                fieldTable += "</tr>"; //NOI18N
            }
            fieldTable += "</table>"; //NOI18N

            sb.append("<hr>"); //NOI18N
            sb.append(fieldTable);
        }
        sb.append("</html>"); //NOI18N
        tooltip = sb.toString();
        return !oldTooltip.equals(tooltip);
    }
    
    private URL getStateIcon (NbTask.SynchronizationState state) {
        URL iconPath = null;
        if (state.equals(NbTask.SynchronizationState.CONFLICT)) {
            iconPath = ICON_CONFLICT_PATH;
        } else if (state.equals(NbTask.SynchronizationState.INCOMING) || state.equals(NbTask.SynchronizationState.INCOMING_NEW)) {
            iconPath = ICON_REMOTE_PATH;
        } else if (state.equals(NbTask.SynchronizationState.OUTGOING) || state.equals(NbTask.SynchronizationState.OUTGOING_NEW)) {
            iconPath = ICON_UNSUBMITTED_PATH;
        }
        return iconPath;
    }

    private String formatDate(Calendar date) {
        Calendar now = Calendar.getInstance();
        if (now.get(Calendar.YEAR) == date.get(Calendar.YEAR)) {

            return DateFormat.getDateInstance(DateFormat.SHORT).format(date.getTime());
        } else {
            return DateFormat.getDateInstance(DateFormat.DEFAULT).format(date.getTime());

        }
    }

    @NbBundle.Messages({
        "LBL_ConflictShort=Conflict - your unsubmitted changes conflict with remote changes",
        "LBL_RemoteShort=Incoming - contains remote changes",
        "LBL_RemoteNewShort=Incoming New - new task created in repository",
        "LBL_UnsubmittedShort=Unsubmitted - contains unsubmitted changes",
        "LBL_UnsubmittedNewShort=Unsubmitted New - newly created task, not yet submitted"})
    private String getStateDisplayName (NbTask.SynchronizationState state) {
        String displayName = "";
        if (state.equals(NbTask.SynchronizationState.CONFLICT)) {
            displayName = Bundle.LBL_ConflictShort();
        } else if (state.equals(NbTask.SynchronizationState.INCOMING)) {
            displayName = Bundle.LBL_RemoteShort();
        } else if (state.equals(NbTask.SynchronizationState.INCOMING_NEW)) {
            displayName = Bundle.LBL_RemoteNewShort();
        } else if (state.equals(NbTask.SynchronizationState.OUTGOING)) {
            displayName = Bundle.LBL_UnsubmittedShort();
        } else if (state.equals(NbTask.SynchronizationState.OUTGOING_NEW)) {
            displayName = Bundle.LBL_UnsubmittedNewShort();
        }
        return displayName;
    }

    private boolean updateRecentChanges () {
        String oldChanges = recentChanges;
        recentChanges = "";
        NbTask.SynchronizationState syncState = getSynchronizationState();
        if (syncState == NbTask.SynchronizationState.INCOMING
                || syncState == NbTask.SynchronizationState.CONFLICT) {
            try {
                NbTaskDataState taskDataState = getNbTask().getTaskDataState();
                if (taskDataState != null) {
                    TaskData repositoryData = taskDataState.getRepositoryData();
                    TaskData lastReadData = taskDataState.getLastReadData();
                    List<IssueField> changedFields = new ArrayList<>();
                    for (IssueField f : IssueField.values()) {
                        switch(f) {
                            case MODIFICATION :
                                continue;
                        }
                        String value = getFieldValue(repositoryData, f);
                        String seenValue = getFieldValue(lastReadData, f);
                        if(!value.trim().equals(seenValue)) {
                            changedFields.add(f);
                        }
                        
//                        System.out.println(" +++ " + f.getDisplayName());
                        
                    }
                    int changedCount = changedFields.size();
                    if(changedCount == 1) {
                        for (IssueField changedField : changedFields) {
                            String value = getFieldValue(repositoryData, changedField);
                            String seenValue = getFieldValue(lastReadData, changedField);
                            switch(changedField) {
                                case SUMMARY :
                                    recentChanges = NbBundle.getMessage(NbJiraIssue.class, "LBL_SUMMARY_CHANGED_STATUS");
                                    break;
                                case COMMENT_COUNT :
                                    if (seenValue.trim().isEmpty()) {
                                        seenValue = "0";
                                    }
                                    int count = 0;
                                    try {
                                        count = Integer.parseInt(value) - Integer.parseInt(seenValue);
                                    } catch(NumberFormatException ex) {
                                        Jira.LOG.log(Level.WARNING, value + ":" + seenValue, ex);
                                    }
                                    recentChanges = NbBundle.getMessage(NbJiraIssue.class, "LBL_COMMENTS_CHANGED", new Object[] {count});
                                    break;
                                case ATTACHEMENT_COUNT :
                                    recentChanges = NbBundle.getMessage(NbJiraIssue.class, "LBL_ATTACHMENTS_CHANGED");
                                    break;
                                default :
                                    if(changedField.displayNameKey != null) {
                                        recentChanges = NbBundle.getMessage(NbJiraIssue.class, "LBL_CHANGED_TO", new Object[] {changedField.getDisplayName(), getFieldDisplayValue(changedField)});
                                    } else {
                                        recentChanges = NbBundle.getMessage(NbJiraIssue.class, "LBL_CHANGES", new Object[] {changedCount});
                                    }
                            }
                        }
                    } else {
                        for (IssueField changedField : changedFields) {
                            switch(changedField) {
                                case SUMMARY :
                                    recentChanges = NbBundle.getMessage(NbJiraIssue.class, "LBL_CHANGES_INCL_SUMMARY", new Object[] {changedCount});
                                    break;
                                case PRIORITY :
                                    recentChanges = NbBundle.getMessage(NbJiraIssue.class, "LBL_CHANGES_INCL_PRIORITY", new Object[] {changedCount});
                                    break;
                                case TYPE :
                                    recentChanges = NbBundle.getMessage(NbJiraIssue.class, "LBL_CHANGES_INCL_TYPE", new Object[] {changedCount});
                                    break;
                                case PROJECT :
                                    recentChanges = NbBundle.getMessage(NbJiraIssue.class, "LBL_CHANGES_INCL_PROJECT", new Object[] {changedCount});
                                    break;
                                case COMPONENT :
                                    recentChanges = NbBundle.getMessage(NbJiraIssue.class, "LBL_CHANGES_INCL_COMPONENT", new Object[] {changedCount});
                                    break;
                                case ENVIRONMENT :
                                    recentChanges = NbBundle.getMessage(NbJiraIssue.class, "LBL_CHANGES_INCL_ENVIRONMENT", new Object[] {changedCount});
                                    break;
                                case ASSIGNEE :
                                    recentChanges = NbBundle.getMessage(NbJiraIssue.class, "LBL_CHANGES_INCL_ASSIGNEE", new Object[] {changedCount});
                                    break;                        
                                default :
                                    recentChanges = NbBundle.getMessage(NbJiraIssue.class, "LBL_CHANGES", new Object[] {changedCount});
                            }
                        }
                    }
                }
            } catch (CoreException ex) {
                Jira.LOG.log(Level.WARNING, null, ex);
            }
        }
        return !oldChanges.equals(recentChanges);
    }
    
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
            JTable t = new JTable();
            DESCRIPTORS = new ColumnDescriptor[] {
                new ColumnDescriptor<String>(LABEL_NAME_ID, String.class,
                                              loc.getString("CTL_Issue_ID_Title"), // NOI18N
                                              loc.getString("CTL_Issue_ID_Desc"), // NOI18N
                                              UIUtils.getColumnWidthInPixels(20, t)),
                new ColumnDescriptor<String>(IssueNode.LABEL_NAME_SUMMARY, String.class,
                                              loc.getString("CTL_Issue_Summary_Title"), // NOI18N
                                              loc.getString("CTL_Issue_Summary_Desc")), // NOI18N
                new ColumnDescriptor<String>(LABEL_NAME_TYPE, String.class,
                                              loc.getString("CTL_Issue_Type_Title"), // NOI18N
                                              loc.getString("CTL_Issue_Type_Desc"), // NOI18N
                                              0),
                new ColumnDescriptor<String>(LABEL_NAME_PRIORITY, String.class,
                                              loc.getString("CTL_Issue_Priority_Title"), // NOI18N
                                              loc.getString("CTL_Issue_Priority_Desc"), // NOI18N
                                              0),
                new ColumnDescriptor<String>(LABEL_NAME_STATUS, String.class,
                                              loc.getString("CTL_Issue_Status_Title"), // NOI18N
                                              loc.getString("CTL_Issue_Status_Desc"), // NOI18N
                                              0),
                new ColumnDescriptor<String>(LABEL_NAME_RESOLUTION, String.class,
                                              loc.getString("CTL_Issue_Resolution_Title"), // NOI18N
                                              loc.getString("CTL_Issue_Resolution_Desc"), // NOI18N
                                              0),
                new ColumnDescriptor<String>(LABEL_NAME_ASSIGNED_TO, String.class,
                                              loc.getString("CTL_Issue_Assigned_Title"),        // NOI18N
                                              loc.getString("CTL_Issue_Assigned_Desc"),         // NOI18N
                                              0),
                new ColumnDescriptor<String>(LABEL_NAME_PROJECT, String.class,
                                              loc.getString("CTL_Issue_Project_Title"),        // NOI18N
                                              loc.getString("CTL_Issue_Project_Desc"),         // NOI18N
                                              0, false),
                new ColumnDescriptor<String>(LABEL_NAME_COMPONENTS, String.class,
                                              loc.getString("CTL_Issue_Components_Title"),        // NOI18N
                                              loc.getString("CTL_Issue_Components_Desc"),         // NOI18N
                                              0, false),
                new ColumnDescriptor<String>(LABEL_NAME_AFFECTS_VERSION, String.class,
                                              loc.getString("CTL_Issue_Affects_Version_Title"),        // NOI18N
                                              loc.getString("CTL_Issue_Affects_Version_Desc"),         // NOI18N
                                              0, false),
                new ColumnDescriptor<String>(LABEL_NAME_FIX_VERSION, String.class,
                                              loc.getString("CTL_Issue_Fix_Version_Title"),        // NOI18N
                                              loc.getString("CTL_Issue_Fix_Version_Desc"),         // NOI18N
                                              0, false),
                new ColumnDescriptor<String>(LABEL_NAME_CREATED, String.class,
                                              loc.getString("CTL_Issue_Created_Title"),        // NOI18N
                                              loc.getString("CTL_Issue_Created_Desc"),         // NOI18N
                                              0, false),
                new ColumnDescriptor<String>(LABEL_NAME_UPDATED, String.class,
                                              loc.getString("CTL_Issue_Updated_Title"),        // NOI18N
                                              loc.getString("CTL_Issue_Updated_Desc"),         // NOI18N
                                              0, false),
                new ColumnDescriptor<String>(LABEL_NAME_DUE, String.class,
                                              loc.getString("CTL_Issue_Due_Title"),        // NOI18N
                                              loc.getString("CTL_Issue_Due_Desc"),         // NOI18N
                                              0, false),
                new ColumnDescriptor<String>(LABEL_NAME_ESTIMATE, String.class,
                                              loc.getString("CTL_Issue_Estimate_Title"),        // NOI18N
                                              loc.getString("CTL_Issue_Estimate_Desc"),         // NOI18N
                                              0, false),
                new ColumnDescriptor<String>(LABEL_NAME_INITIAL_ESTIMATE, String.class,
                                              loc.getString("CTL_Issue_Initial_Estimate_Title"),        // NOI18N
                                              loc.getString("CTL_Issue_Initial_Estimate_Desc"),         // NOI18N
                                              0, false),
                new ColumnDescriptor<String>(LABEL_NAME_TIME_SPENT, String.class,
                                              loc.getString("CTL_Issue_Time_Spent_Title"),        // NOI18N
                                              loc.getString("CTL_Issue_Time_Spent_Desc"),         // NOI18N
                                              0, false)
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
    
    /**
     * Returns the given fields diplay value
     * @param f
     * @return
     */
    String getFieldDisplayValue(IssueField f) {
        String value = getRepositoryFieldValue(f);
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
                return toString(values);
            case AFFECTSVERSIONS:
                return toString(getVersionValues(f, config));
            case FIXVERSIONS:
                return toString(getVersionValues(f, config));
            case TYPE:
                IssueType type = config != null ? config.getIssueTypeById(value) : null;
                return type != null ? type.getName() : "";                      // NOI18N
            default:
                return value;
        }
    }

    private List<String> getVersionValues(IssueField f, JiraConfiguration config) {
        String projectId = getFieldValue(IssueField.PROJECT);
        List<String> values = new LinkedList<String>();
        for (String v : getFieldValues(f)) {
            Version version = config != null ? config.getVersionById(projectId, v) : null;
            if (version != null) {
                values.add(version.getName());
            }
        }
        return values;
    }

    private String toString(List<String> l) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < l.size(); i++) {
            sb.append(l.get(i));
            if(i < l.size() -1) {
                sb.append(',');
            } //  NOI18N
        }
        return sb.toString();
    }
    
    public String getRepositoryFieldValue (IssueField f) {
        NbTaskDataModel m = getModel();
        TaskData td;
        if (m == null) {
            td = getRepositoryTaskData();
            if (td == null) {
                return ""; //NOI18N
            }
        } else {
            td = m.getRepositoryTaskData();
        }
        return getFieldValue(td, f);
    }

    public String getFieldValue(IssueField f) {
        NbTaskDataModel m = getModel();
        return getFieldValue(m == null ? null : m.getLocalTaskData(), f);
    }

    String getLastSeenFieldValue (IssueField f) {
        NbTaskDataModel m = getModel();
        return getFieldValue(m == null ? null : m.getLastReadTaskData(), f);
    }

    private static String getFieldValue (TaskData taskData, IssueField f) {
        if (taskData == null) {
            return "";
        }
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
        StringBuilder sb = new StringBuilder();
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
                Jira.LOG.log(Level.FINEST, "{0}: resolving issue{1}: available operation: {2}({3})", new Object[]{getClass().getName(), getKey(), operationLabel, entry.getValue().getOperationId()}); //NOI18N
            }
            if (JiraUtils.isResolveOperation(operationLabel)) {
                operation = entry.getValue();
                break;
            } else if (JiraUtils.isLeaveOperation(entry.getValue())) {
                // falback on leave operation. Should check if the original status was resolved to be presize
                operation = entry.getValue();
            }
        }
        return operation;
    }

    /**
     * public for tests
     */
    public void setFieldValue(IssueField f, String value) {
        NbTaskDataModel m = getModel();
        // should not happen, setFieldValue either runs with model lock
        // or it is called from issue editor in AWT - the editor could not be closed by user in the meantime
        assert m != null;
        TaskData taskData = m.getLocalTaskData();
        TaskAttribute a = taskData.getRoot().getMappedAttribute(f.key);
        if (a == null) {
            a = new TaskAttribute(taskData.getRoot(), f.key);
        }
        if (!value.equals(a.getValue())) {
            setTaskAttributeValue(m, a, value);
        }
    }
    
    /**
     * Tests only, <b>NEVER</b> call this method.
     */
    public void loadModel () {
        editorOpened();
    }

    public List<String> getRepositoryFieldValues (IssueField f) {
        NbTaskDataModel m = getModel();
        return getFieldValues(m == null ? getRepositoryTaskData() : m.getRepositoryTaskData(), f);
    }

    public List<String> getFieldValues(IssueField f) {
        NbTaskDataModel m = getModel();
        return getFieldValues(m == null ? null : m.getLocalTaskData(), f);
    }

    List<String> getLastSeenFieldValues (IssueField f) {
        NbTaskDataModel m = getModel();
        return getFieldValues(m == null ? null : m.getLastReadTaskData(), f);
    }
    
    private static List<String> getFieldValues(TaskData taskData, IssueField f) {
        if (taskData == null) {
            return Collections.<String>emptyList();
        }
        if(!f.isSingleAttribute()) {
            TaskAttribute a = taskData.getRoot().getMappedAttribute(f.key);
            if(a != null) {
                return a.getValues();
            } else {
                return Collections.emptyList();
            }
        } else {
            List<String> ret = new ArrayList<String>();
            ret.add(getFieldValue(taskData, f));
            return ret;
        }
    }

    /**
     * public for tests
     */
    public void setFieldValues(IssueField f, List<String> values) {
        NbTaskDataModel m = getModel();
        // should not happen, setFieldValue either runs with model lock
        // or it is called from issue editor in AWT - the editor could not be closed by user in the meantime
        assert m != null;
        TaskData taskData = m.getLocalTaskData();
        TaskAttribute a = taskData.getRoot().getMappedAttribute(f.key);
        if(a == null) {
            a = new TaskAttribute(taskData.getRoot(), f.key);
        }
        if (!values.equals(a.getValues())) {
            setTaskAttributeValues(m, a, values);
        }
    }

    @NbBundle.Messages({
        "# {0} - task id and summary", "MSG_JiraIssue.statusBar.submitted=Task {0} submitted."
    })
    public boolean submitAndRefresh() {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N
        final boolean[] result = new boolean[1];
        runWithModelLoaded(new Runnable() {

            @Override
            public void run () {
                final boolean wasNew = isNew();
                if (Jira.LOG.isLoggable(Level.FINEST)) {
                    Jira.LOG.log(Level.FINEST, "submitAndRefresh: id: {0}, new: {1}", new Object[]{getKey(), wasNew});
                }
                List<AttachmentInfo> newAttachments = getNewAttachments();
                if (!newAttachments.isEmpty()) {
                    // clear before submit, we do not know how connectors deal with internal attributes
                    setNewAttachments(Collections.<AttachmentInfo>emptyList());
                }
                
                SubmitTaskCommand submitCmd;
                try {
                    // fix status according to the selected operation
                    fixStatus();
                    // fix worklog's remaining estimate time
                    fixWorkLog();
                    if (saveChanges()) {
                        fireChanged();
                        submitCmd = MylynSupport.getInstance().getCommandFactory().createSubmitTaskCommand(getModel());
                    } else {
                        result[0] = false;
                        return;
                    }
                } catch (CoreException ex) {
                    Jira.LOG.log(Level.WARNING, null, ex);
                    result[0] = false;
                    return;
                }
                if (Jira.LOG.isLoggable(Level.FINEST)) {
                    Jira.LOG.log(Level.FINEST, "submitAndRefresh, submitCmd: id: {0}, new: {1}", new Object[]{getKey(), wasNew});
                }
                repository.getExecutor().execute(submitCmd);
                if (!submitCmd.hasFailed()) {
                    taskSubmitted(submitCmd.getSubmittedTask());
                }

                if (!wasNew) {
                    // should not be needed, mylyn updates data upon submit itself
//                    if (Jira.LOG.isLoggable(Level.FINEST)) {
//                        Jira.LOG.log(Level.FINEST, "submitAndRefresh, refreshCmd: id: {0}, new: {1}", new Object[]{getID(), wasNew});
//                    }
//                    if (!wasNew) {
//                        refresh();
//                    } else {
//                        refresh(true);
//                    }
                } else {
                    RepositoryResponse rr = submitCmd.getRepositoryResponse();
                    if(!submitCmd.hasFailed()) {
                        updateRecentChanges();
                        updateTooltip();
                        fireDataChanged();
                        String key = getKey();
                        repository.getIssueCache().setIssue(key, NbJiraIssue.this);
                        Jira.LOG.log(Level.FINE, "created issue #{0}", key);
                        // a new issue was created -> refresh all queries
                        repository.refreshAllQueries();
                    } else {
                        Jira.LOG.log(Level.FINE, "submiting failed");
                        if(rr != null) {
                            Jira.LOG.log(Level.FINE, "repository response {0}", rr.getReposonseKind());
                        } else {
                            Jira.LOG.log(Level.FINE, "no repository response available");
                        }
                    }
                }

                if(submitCmd.hasFailed()) {
                    result[0] = false;
                    if (!newAttachments.isEmpty()) {
                        setNewAttachments(newAttachments);
                        saveChanges();
                    }
                    return;
                } else {
                    if (!newAttachments.isEmpty()) {
                        for (AttachmentInfo attachment : newAttachments) {
                            File f = attachment.getFile();
                            if (f.isFile()) {
                                addAttachment(f, null, null);
                            } else {
                                // PENDING notify user
                            }
                        }
                    }
                }
                StatusDisplayer.getDefault().setStatusText(Bundle.MSG_JiraIssue_statusBar_submitted(getDisplayName()));

                setUpToDate(true, false);
                if(wasNew) {
                    // a new issue was created -> refresh all queries
                    repository.refreshAllQueries();
                }
                result[0] = true;
            }

            private void fixStatus () {
                NbTaskDataModel m = getModel();
                TaskAttribute status = m.getLocalTaskData().getRoot().getMappedAttribute(IssueField.STATUS.key);
                if (status != null && status.getOptions().size() > 0 && !status.getOptions().containsKey(status.getValue())) {
                    status.setValue(status.getOptions().keySet().iterator().next());
                    getModel().attributeChanged(status);
                }
            }
            
            private void fixWorkLog () {
                NewWorkLog log = getEditedWorkLog();
                NbTaskDataModel m = getModel();
                if (log != null && log.isToSubmit() && log.getStartDate() != null) {
                    TaskAttribute attribute = m.getLocalTaskData().getRoot().getMappedAttribute(jiraConstants.getWorkLogConverter_ATTRIBUTE_WORKLOG_NEW());
                    JiraWorkLog workLog = JiraConnectorSupport.getInstance().getConnector().createWorkLog();
                    workLog.setComment(log.getComment());
                    workLog.setStartDate(log.getStartDate());
                    workLog.setTimeSpent(log.getTimeSpent());
                    if (log.isAutoAdjust()) {
                        workLog.setAdjustEstimate(JiraWorkLog.AdjustEstimateMethod.AUTO);
                    } else if (log.isLeaveEstimate()) {
                        workLog.setAdjustEstimate(JiraWorkLog.AdjustEstimateMethod.LEAVE);
                    } else if (log.isReduceEstimate()) {
                        workLog.setAdjustEstimate(JiraWorkLog.AdjustEstimateMethod.LEAVE);
                        setFieldValue(NbJiraIssue.IssueField.ESTIMATE, String.valueOf(getCurrentRemainingEstimate() - log.getEstimatedTime()));
                    } else if (log.isSetEstimate()) {
                        workLog.setAdjustEstimate(JiraWorkLog.AdjustEstimateMethod.LEAVE);
                        setFieldValue(NbJiraIssue.IssueField.ESTIMATE, String.valueOf(log.getEstimatedTime()));
                    }
                    workLog.applyTo(attribute);
                    attribute.createMappedAttribute(jiraConstants.getWorkLogConverter_ATTRIBUTE_WORKLOG_NEW_SUBMIT_FLAG()).setValue("true"); //NOI18N
                    m.attributeChanged(attribute);
                }
            }

            private int getCurrentRemainingEstimate () {
                String estimateTxt = getFieldValue(NbJiraIssue.IssueField.ESTIMATE);
                int estimate = 0;
                if (estimateTxt != null) {
                    try {
                        estimate = Integer.parseInt(estimateTxt);
                    } catch (NumberFormatException nfex) {
                        estimate = 0;
                    }
                }
                return estimate;
            }
            
        });
        return result[0];
    }

    /**
     * Returns a status value for the given field<br>
     * <ul>
     *  <li>{@link #FIELD_STATUS_IRELEVANT} - issue wasn't seen yet
     *  <li>{@link #FIELD_STATUS_UPTODATE} - field value wasn't changed
     *  <li>{@link #FIELD_STATUS_MODIFIED} - field value was changed in repository
     *  <li>{@link #FIELD_STATUS_OUTGOING} - field value was changed locally
     *  <li>{@link #FIELD_STATUS_CONFLICT} - field value was changed both locally and remotely
     * </ul>
     * @param f IssueField
     * @return a status value
     */
    public int getFieldStatus(IssueField f) {
        return getFieldStatus(f.getKey());
    }

    public int getFieldStatus (String fieldKey) {
        NbTaskDataModel m = getModel();
        if (m == null) {
            return FIELD_STATUS_UPTODATE;
        }
        TaskAttribute ta = m.getLocalTaskData().getRoot().getMappedAttribute(fieldKey);
        boolean incoming = ta != null && m.hasIncomingChanges(ta, true);
        boolean outgoing = ta != null && m.hasOutgoingChanges(ta);
        if (ta == null) {
            return FIELD_STATUS_UPTODATE;
        } else if (incoming & outgoing) {
            return FIELD_STATUS_CONFLICT;
        } else if (incoming) {
            return FIELD_STATUS_MODIFIED;
        } else if (outgoing) {
            return FIELD_STATUS_OUTGOING;
        }
        return FIELD_STATUS_UPTODATE;
    }

    /**
     * Returns available operations for this issue
     * @return
     */
    public Map<String, TaskOperation> getAvailableOperations () {
        if (availableOperations == null) {
            HashMap<String, TaskOperation> operations = new HashMap<String, TaskOperation>(5);
            NbTaskDataModel model = getModel();
            List<TaskAttribute> allOperations = model.getLocalTaskData().getAttributeMapper().getAttributesByType(model.getLocalTaskData(), TaskAttribute.TYPE_OPERATION);
            for (TaskAttribute operation : allOperations) {
                // the test must be here, 'operation' (applying writable action) is also among allOperations
                if (operation != null && operation.getId().startsWith(TaskAttribute.PREFIX_OPERATION)) {
                    operations.put(operation.getId().substring(TaskAttribute.PREFIX_OPERATION.length()), TaskOperation.createFrom(operation));
                }
            }
            availableOperations = operations;
        }

        return availableOperations;
    }

    private void refreshViewData(boolean force) {
        if (controller != null) {
            // view might not exist yet and we won't unnecessarily create it
            controller.refreshViewData(force);
        }
    }

    private class Controller implements IssueController {
        private IssuePanel issuePanel;

        public Controller() {
            IssuePanel panel = new IssuePanel();
            panel.setIssue(NbJiraIssue.this);
            issuePanel = panel;
        }

        @Override
        public JComponent getComponent() {
            return issuePanel;
        }

        @Override
        public void opened() {
            NbJiraIssue issue = issuePanel.getIssue();
            if (issue != null) {
                issuePanel.opened();
            }
        }
        
        @Override
        public void closed() {
            NbJiraIssue issue = issuePanel.getIssue();
            if (issue != null) {
                issuePanel.closed();
            }
        }

        private void refreshViewData (final boolean force) {
            Mutex.EVENT.readAccess(new Runnable() {
                @Override
                public void run () {
                    if (open) {
                        issuePanel.reloadFormInAWT(force);
                    }
                }
            });
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx("org.netbeans.modules.jira.issue.NbJiraIssue"); // NOI18N
        }

        private void modelStateChanged (boolean modelDirty, boolean modelHasLocalChanges) {
            issuePanel.modelStateChanged(modelDirty, modelHasLocalChanges);
            NbJiraIssue.this.fireChanged();
        }

        @Override
        public boolean saveChanges() {
            return issuePanel.saveChanges();
        }

        @Override
        public boolean discardUnsavedChanges() {
            return issuePanel.discardUnsavedChanges();
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            NbJiraIssue.this.addPropertyChangeListener(l);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            NbJiraIssue.this.removePropertyChangeListener(l);
        }

        @Override
        public boolean isChanged() {
            return NbJiraIssue.this.hasUnsavedChanges();
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
        private final String email;
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
            User user = person == null ? null : getRepository().getConfiguration().getUser(person.getPersonId());
            email = user == null ? null : user.getEmail();
            size = JiraUtils.getMappedValue(ta, TaskAttribute.ATTACHMENT_SIZE);
            url = taskAttachment.getUrl();
        }

        public String getEmail() {
            return email;
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
            BugtrackingCommand cmd = new BugtrackingCommand() {
                @Override
                public void execute() throws CoreException, IOException {
                    if (Jira.LOG.isLoggable(Level.FINER)) {
                        Jira.LOG.log(Level.FINER, "getAttachmentData: id: {0}, issue: {1}", new Object[]{Attachment.this.getId(), getKey()});
                    }
                    try {
                        repository.getExecutor().execute(MylynSupport.getInstance().getCommandFactory()
                                .createGetAttachmentCommand(repository.getTaskRepository(), getNbTask(), attachmentAttribute, os));
                    } catch (CoreException ex) {
                        // should not happen
                        Jira.LOG.log(Level.WARNING, null, ex);
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

    public final class CustomField {
        private final String id;
        private final String label;
        private final String type;
        private List<String> values;
        private final boolean readOnly;

        private CustomField(TaskAttribute attribute) {
            id = attribute.getId();
            label = attribute.getMetaData().getValue(TaskAttribute.META_LABEL);
            type = attribute.getMetaData().getValue(jiraConstants.getMETA_TYPE());
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

        public List<String> getLastSeenValues () {
            NbTaskDataModel model = getModel();
            TaskData td = model == null ? null : model.getLastReadTaskData();
            return getValues(td);
        }

        public List<String> getRepositoryValues () {
            NbTaskDataModel model = getModel();
            TaskData td = model == null ? null : model.getRepositoryTaskData();
            return getValues(td);
        }

        private List<String> getValues (TaskData td) {
            if (td != null) {
                TaskAttribute ta = td.getRoot().getMappedAttribute(id);
                if (ta != null) {
                    return ta.getValues();
                }
            }
            return Collections.<String>emptyList();
        }
    }

    public static final class LinkedIssue {
        private final String linkId;
        private final String label;
        private final String issueKey;
        private final boolean inward;

        private LinkedIssue(TaskAttribute attribute) {
            String suffix = attribute.getId().substring(jiraConstants.getATTRIBUTE_LINK_PREFIX().length());
            inward = suffix.endsWith("inward"); // NOI18N
            linkId = suffix.substring(0, suffix.length()-(inward?6:7));
            label = attribute.getMetaData().getValue(TaskAttribute.META_LABEL);
            issueKey = attribute.getValue();
        }

        public String getLinkId() {
            return linkId;
        }

        public String getLabel() {
            return label;
        }

        public String getIssueKey() {
            return issueKey;
        }

        public boolean isInward() {
            return inward;
        }

    }

    public static final class WorkLog {
        private final Date startDate;
        private final String author;
        private final long timeSpent;
        private final String comment;

        public WorkLog(TaskAttribute workLogTA) {
            TaskAttributeMapper mapper = workLogTA.getTaskData().getAttributeMapper();
            startDate = mapper.getDateValue(workLogTA.getMappedAttribute(jiraConstants.getWorkLogConverter_START_DATE_key()));
            IRepositoryPerson person = mapper.getRepositoryPerson(workLogTA.getMappedAttribute(jiraConstants.getWorkLogConverter_AUTOR_key()));
            author = person == null ? null : person.getPersonId();
            comment = mapper.getValue(workLogTA.getMappedAttribute(jiraConstants.getWorkLogConverter_COMMENT_key()));
            Long timeSpentValue = mapper.getLongValue(workLogTA.getMappedAttribute(jiraConstants.getWorkLogConverter_TIME_SPENT_key()));
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
    
    
    public static final class NewWorkLog {
        private Date startDate;
        private String author;
        private long timeSpent;
        private String comment;
        private boolean toSubmit;
        private long estimatedTime;
        private boolean setEstimate;
        private boolean reduceEstimate;
        private boolean leaveEstimate;
        private boolean autoAdjust;

        private NewWorkLog (TaskAttribute workLogTA) {
            JiraWorkLog workLog = JiraConnectorSupport.getInstance().getConnector().createWorkLogFrom(workLogTA);
            startDate = workLog.getStartDate();
            author = workLog.getAuthor();
            timeSpent = workLog.getTimeSpent();
            comment = workLog.getComment();
            TaskAttribute toSubmitFlag = workLogTA.getMappedAttribute(jiraConstants.getWorkLogConverter_ATTRIBUTE_WORKLOG_NEW_SUBMIT_FLAG());
            toSubmit = toSubmitFlag != null && Boolean.parseBoolean(toSubmitFlag.getValue());
            TaskAttribute att = workLogTA.getAttribute(NB_WORK_LOGNEW_ESTIMATE_TIME);
            if (att != null) {
                try {
                    estimatedTime = Long.valueOf(att.getValue());
                } catch (NumberFormatException ex) { }
            }
            switch (workLog.getAdjustEstimate()) {
                case LEAVE:
                    leaveEstimate = true;
                    break;
                case REDUCE:
                    reduceEstimate = true;
                    break;
                case SET:
                    setEstimate = true;
                    break;
                default:
                    autoAdjust = true;
            }
        }
    
        public NewWorkLog () {
            startDate = null;
            author = "";
            timeSpent = 0;
            comment = "";
            toSubmit = false;
            estimatedTime = 0;
            leaveEstimate = false;
            reduceEstimate = false;
            setEstimate = false;
            autoAdjust = false;
        }

        public Date getStartDate () {
            return startDate;
        }

        public String getAuthor () {
            return author;
        }

        public long getTimeSpent () {
            return timeSpent;
        }

        public String getComment () {
            return comment;
        }

        boolean isToSubmit () {
            return toSubmit;
        }

        public long getEstimatedTime () {
            return estimatedTime;
        }

        public boolean isAutoAdjust () {
            return autoAdjust;
        }

        public boolean isLeaveEstimate () {
            return leaveEstimate;
        }

        public boolean isReduceEstimate () {
            return reduceEstimate;
        }

        public boolean isSetEstimate () {
            return setEstimate;
        }

        public void setToSubmit (boolean submit) {
            toSubmit = submit;
        }

        public void setTimeSpent (long timeSpent) {
            this.timeSpent = timeSpent;
        }

        public void setStartDate (Date startDate) {
            this.startDate = startDate;
        }

        public void setComment (String description) {
            this.comment = description;
        }

        public void setEstimateTime (long remainingEstimate) {
            this.estimatedTime = remainingEstimate;
        }

        public void setSetEstimate (boolean flag) {
            setEstimate = flag;
        }

        public void setReduceEstimate (boolean flag) {
            reduceEstimate = flag;
        }

        public void setLeaveEstimate (boolean flag) {
            leaveEstimate = flag;
        }

        public void setAutoAdjust (boolean flag) {
            autoAdjust = flag;
        }
    }
}
