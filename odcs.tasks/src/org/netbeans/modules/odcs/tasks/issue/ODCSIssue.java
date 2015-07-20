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
package org.netbeans.modules.odcs.tasks.issue;

import com.tasktop.c2c.server.tasks.domain.AbstractReferenceValue;
import com.tasktop.c2c.server.tasks.domain.Iteration;
import com.tasktop.c2c.server.tasks.domain.Milestone;
import com.tasktop.c2c.server.tasks.domain.Priority;
import com.tasktop.c2c.server.tasks.domain.TaskResolution;
import com.tasktop.c2c.server.tasks.domain.TaskSeverity;
import com.tasktop.c2c.server.tasks.domain.TaskStatus;
import org.netbeans.modules.bugtracking.commons.AttachmentsPanel;
import java.awt.EventQueue;
import java.io.File;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import oracle.eclipse.tools.cloud.dev.tasks.CloudDevOperation;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.internal.tasks.core.data.FileTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskOperation;
import org.netbeans.modules.bugtracking.issuetable.ColumnDescriptor;
import org.netbeans.modules.bugtracking.issuetable.IssueNode;
import org.netbeans.modules.bugtracking.spi.IssueController;
import org.netbeans.modules.bugtracking.commons.UIUtils;
import org.netbeans.modules.bugtracking.spi.IssueScheduleInfo;
import org.netbeans.modules.mylyn.util.AbstractNbTaskWrapper;
import org.netbeans.modules.mylyn.util.MylynSupport;
import org.netbeans.modules.mylyn.util.NbTask;
import org.netbeans.modules.mylyn.util.NbTaskDataModel;
import org.netbeans.modules.mylyn.util.NbTaskDataState;
import org.netbeans.modules.mylyn.util.commands.PostAttachmentCommand;
import org.netbeans.modules.mylyn.util.commands.SubmitTaskCommand;
import org.netbeans.modules.mylyn.util.commands.SynchronizeTasksCommand;
import org.netbeans.modules.odcs.tasks.ODCS;
import org.netbeans.modules.odcs.tasks.repository.ODCSRepository;
import org.netbeans.modules.odcs.tasks.util.ODCSUtil;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Tomas Stupka
 */
public class ODCSIssue extends AbstractNbTaskWrapper {

    private final ODCSRepository repository;

    private ODCSIssueController controller;
    
    private WeakReference<ODCSIssueNode> nodeRef;
    
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
    
    private static final URL ICON_REMOTE_PATH = IssuePanel.class.getClassLoader().getResource("org/netbeans/modules/odcs/tasks/resources/remote.png"); //NOI18N
    private static final URL ICON_CONFLICT_PATH = IssuePanel.class.getClassLoader().getResource("org/netbeans/modules/odcs/tasks/resources/conflict.png"); //NOI18N
    private static final URL ICON_UNSUBMITTED_PATH = IssuePanel.class.getClassLoader().getResource("org/netbeans/modules/odcs/tasks/resources/unsubmitted.png"); //NOI18N
    private boolean loading;
    
    public ODCSIssue(NbTask task, ODCSRepository repo) {
        super(task);
        this.repository = repo;
        updateRecentChanges();
        updateTooltip();
    }

    public IssueNode getNode() {
        ODCSIssueNode n = nodeRef != null ? nodeRef.get() : null;
        if(n == null) {
            n = new ODCSIssueNode(this);
            nodeRef = new WeakReference<>(n);
        }
        return n;
    }
    
    public String getDisplayName() {
        return getDisplayName(getNbTask());
    }
    
    /**
     * Determines the issue display name depending on the issue new state
     * @param task
     * @return 
     */
    public static String getDisplayName (NbTask task) {
        return task.getSynchronizationState() == NbTask.SynchronizationState.OUTGOING_NEW ?
                task.getSummary() :
                NbBundle.getMessage(ODCSIssue.class, "CTL_Issue", new Object[] {getID(task), task.getSummary()}); //NOI18N
    }

    public String getTooltip() {
        return tooltip;
    }
    
    // XXX merge with bugzilla
    Comment[] getComments() {
        NbTaskDataModel m = getModel();
        List<TaskAttribute> attrs = m == null ? null : m.getLocalTaskData()
                .getAttributeMapper().getAttributesByType(m.getLocalTaskData(), TaskAttribute.TYPE_COMMENT);
        if (attrs == null) {
            return new Comment[0];
        }
        List<Comment> comments = new ArrayList<Comment>();
        for (TaskAttribute taskAttribute : attrs) {
            comments.add(new Comment(taskAttribute));
        }
        return comments.toArray(new Comment[comments.size()]);
    }    
    
    // XXX merge with bugzilla
    List<Attachment> getAttachments() {
        NbTaskDataModel m = getModel();
        List<TaskAttribute> attrs = m == null ? null : m.getLocalTaskData()
                .getAttributeMapper().getAttributesByType(m.getLocalTaskData(), TaskAttribute.TYPE_ATTACHMENT);
        if (attrs == null) {
            return Collections.emptyList();
        }
        List<Attachment> attachments = new ArrayList<Attachment>(attrs.size());
        for (TaskAttribute taskAttribute : attrs) {
            attachments.add(new Attachment(taskAttribute));
        }
        return attachments;
    }

    public String getRecentChanges() {
        return recentChanges;
    }
    
    @NbBundle.Messages({
        "CTL_Issue_Scheduling.Scheduled_Title=Scheduled",
        "CTL_Issue_Scheduling.Due_Title=Due",
        "CTL_Issue_Scheduling.Estimate_Title=Estimate"
    })
    private boolean updateTooltip () {
        String displayName = getDisplayName();
        String oldTooltip = tooltip;
        NbTask.SynchronizationState state = getSynchronizationState();
        URL iconPath = getStateIcon(state);
        String iconCode = "";
        if (iconPath != null) {
            iconCode = "<img src=\"" + iconPath + "\">&nbsp;"; //NOI18N
        }
        String stateName = getStateDisplayName(state);

        String priorityLabel = NbBundle.getMessage(ODCSIssue.class, "CTL_Issue_Priority_Title") //NOI18N
                + "/" + NbBundle.getMessage(ODCSIssue.class, "CTL_Issue_Severity_Title"); //NOI18N
        String priority = getRepositoryFieldValue(IssueField.PRIORITY)
                + "/" + getRepositoryFieldValue(IssueField.SEVERITY); // NOI18N

        String typeLabel = NbBundle.getMessage(ODCSIssue.class, "CTL_Issue_Type_Title"); //NOI18N
        String type = getRepositoryFieldValue(IssueField.TASK_TYPE);

        String productLabel = NbBundle.getMessage(ODCSIssue.class, "CTL_Issue_Product_Title"); //NOI18N
        String product = getRepositoryFieldValue(IssueField.PRODUCT);

        String componentLabel = NbBundle.getMessage(ODCSIssue.class, "CTL_Issue_Component_Title"); //NOI18N
        String component = getRepositoryFieldValue(IssueField.COMPONENT);

        String assigneeLabel = NbBundle.getMessage(ODCSIssue.class, "CTL_Issue_Owner_Title"); //NOI18N
        String assignee = getRepositoryFieldValue(IssueField.OWNER);

        String statusLabel = NbBundle.getMessage(ODCSIssue.class, "CTL_Issue_Status_Title"); //NOI18N
        String status = getRepositoryFieldValue(IssueField.STATUS);
        String resolution = getRepositoryFieldValue(IssueField.RESOLUTION);

        if (resolution != null && !resolution.trim().isEmpty()) {
            status += "/" + resolution; //NOI18N
        }


        String fieldTable = "<table>" //NOI18N
            + "<tr><td><b>" + priorityLabel + ":</b></td><td>" + priority + "</td><td style=\"padding-left:25px;\"><b>" + typeLabel + ":</b></td><td>" + type + "</td></tr>" //NOI18N
            + "<tr><td><b>" + productLabel + ":</b></td><td>" + product + "</td><td style=\"padding-left:25px;\"><b>" + componentLabel + ":</b></td><td>" + component + "</td></tr>" //NOI18N
            + "<tr><td><b>" + assigneeLabel + ":</b></td><td colspan=\"3\">" + assignee + "</td></tr>" //NOI18N
            + "<tr><td><b>" + statusLabel + ":</b></td><td colspan=\"3\">" + status + "</td></tr>" //NOI18N
            + "</table>"; //NOI18N

        String scheduledLabel = Bundle.CTL_Issue_Scheduling_Scheduled_Title();
        String scheduled = getScheduleDisplayString();

        String dueLabel = Bundle.CTL_Issue_Scheduling_Due_Title();
        String due = getDueDisplayString();
        

        String estimateLabel = Bundle.CTL_Issue_Scheduling_Estimate_Title();
        String estimate = getEstimateDisplayString();
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
        
        StringBuilder sb = new StringBuilder("<html>"); //NOI18N
        sb.append("<b>").append(displayName).append("</b><br>"); //NOI18N
        if (stateName != null && !stateName.isEmpty()) {
            sb.append("<p style=\"padding:5px;\">").append(iconCode).append(stateName).append("</p>"); //NOI18N
        }
        sb.append("<hr>"); //NOI18N
        sb.append(fieldTable);
        sb.append("</html>"); //NOI18N
        tooltip = sb.toString();
        return !oldTooltip.equals(tooltip);
    }
    
    private URL getStateIcon(NbTask.SynchronizationState state) {
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

    @NbBundle.Messages({
        "LBL_ConflictShort=Conflict - your unsubmitted changes conflict with remote changes",
        "LBL_RemoteShort=Incoming - contains remote changes",
        "LBL_RemoteNewShort=Incoming New - new task created in repository",
        "LBL_UnsubmittedShort=Unsubmitted - contains unsubmitted changes",
        "LBL_UnsubmittedNewShort=Unsubmitted New - newly created task, not yet submitted"})
    private String getStateDisplayName(NbTask.SynchronizationState state) {
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

    @Messages({"LBL_NEW_STATUS=New", "LBL_SUMMARY_CHANGED_STATUS=Summary changed",
        "# CC is the ODCS task trackers attributes name, do not translate",
        "LBL_CC_FIELD_CHANGED_STATUS=CC field changed",
        "LBL_TAGS_CHANGED_STATUS=Tags changed",
        "LBL_DEPENDENCE_CHANGED_STATUS=Associations changed",
        "LBL_ATTACHMENTS_CHANGED=Attachment(s) added",
        "# {0} - number of added comments", "LBL_COMMENTS_CHANGED={0} new comment(s)",
        "# {0} - task field name", "# {1} - task field value", "LBL_CHANGED_TO={0} changed to {1}",
        "# {0} - number of changes", "LBL_CHANGES_INCL_SUMMARY={0} changes, incl. summary",
        "# {0} - number of changes", "LBL_CHANGES={0} changes",
        "# {0} - number of changes", "LBL_CHANGES_INCL_PRIORITY={0} changes, incl. priority",
        "# {0} - number of changes", "LBL_CHANGES_INCL_SEVERITY={0} changes, incl. severity",
        "# {0} - number of changes", "LBL_CHANGES_INCL_ISSUE_TYPE={0} changes, incl. task type",
        "# {0} - number of changes", "LBL_CHANGES_INCL_PRODUCT={0} changes, incl. product",
        "# {0} - number of changes", "LBL_CHANGES_INCL_COMPONENT={0} changes, incl. component",
        "# {0} - number of changes", "LBL_CHANGES_INCL_MILESTONE={0} changes, incl. milestone",
        "# {0} - number of changes", "LBL_CHANGES_INCL_ITERATION={0} changes, incl. iteration",
        "# {0} - number of changes", "LBL_CHANGES_INCL_TAGS={0} changes, incl. keywords",
        "# {0} - number of changes", "LBL_CHANGES_INCL_OWNER={0} changes, incl. owner",
        "# {0} - number of changes", "LBL_CHANGES_INCL_ASSOCIATIONS={0} changes, incl. associations"})
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
                    List<IssueField> changedFields = new ArrayList<IssueField>();
                    for (IssueField f : IssueField.getFields()) {
                        if (f == IssueField.MODIFIED || f == IssueField.CREATED || f == IssueField.REPORTER) {
                            continue;
                        }
                        String value = getFieldValue(repositoryData, f);
                        String seenValue = getFieldValue(lastReadData, f);
                        if(!value.trim().equals(seenValue)) {
                            changedFields.add(f);
                        }
                    }
                    int changedCount = changedFields.size();
                    if(changedCount == 1) {
                        for (IssueField changedField : changedFields) {
                            String value = getFieldValue(repositoryData, changedField);
                            String seenValue = getFieldValue(lastReadData, changedField);
                            if (changedField == IssueField.SUMMARY) {
                                recentChanges = Bundle.LBL_SUMMARY_CHANGED_STATUS();
                            } else if (changedField == IssueField.CC) {
                                recentChanges = Bundle.LBL_CC_FIELD_CHANGED_STATUS();
                            } else if (changedField == IssueField.KEYWORDS) {
                                recentChanges = Bundle.LBL_TAGS_CHANGED_STATUS();
                            } else if (changedField == IssueField.SUBTASK || changedField == IssueField.PARENT) {
                                recentChanges = Bundle.LBL_DEPENDENCE_CHANGED_STATUS();
                            } else if (changedField == IssueField.COMMENT_COUNT) {
                                if (seenValue.isEmpty()) {
                                    seenValue = "0"; //NOI18N
                                }
                                int count = 0;
                                try {
                                    count = Integer.parseInt(value) - Integer.parseInt(seenValue);
                                } catch (NumberFormatException ex) {
                                    ODCS.LOG.log(Level.WARNING, recentChanges, ex);
                                }
                                recentChanges = Bundle.LBL_COMMENTS_CHANGED(count);
                            } else if (changedField == IssueField.ATTACHEMENT_COUNT) {
                                recentChanges = Bundle.LBL_ATTACHMENTS_CHANGED();
                            } else {
                                recentChanges = Bundle.LBL_CHANGED_TO(changedField.getDisplayName(), value);
                            }
                        }
                    } else {
                        for (IssueField changedField : changedFields) {
                            if (changedField == IssueField.SUMMARY) {
                                recentChanges = Bundle.LBL_CHANGES_INCL_SUMMARY(changedCount);
                            } else if (changedField == IssueField.PRIORITY) {
                                recentChanges = Bundle.LBL_CHANGES_INCL_PRIORITY(changedCount);
                            } else if (changedField == IssueField.SEVERITY) {
                                recentChanges = Bundle.LBL_CHANGES_INCL_SEVERITY(changedCount);
                            } else if (changedField == IssueField.TASK_TYPE) {
                                recentChanges = Bundle.LBL_CHANGES_INCL_ISSUE_TYPE(changedCount);
                            } else if (changedField == IssueField.PRODUCT) {
                                recentChanges = Bundle.LBL_CHANGES_INCL_PRODUCT(changedCount);
                            } else if (changedField == IssueField.COMPONENT) {
                                recentChanges = Bundle.LBL_CHANGES_INCL_COMPONENT(changedCount);
                            } else if (changedField == IssueField.MILESTONE) {
                                recentChanges = Bundle.LBL_CHANGES_INCL_MILESTONE(changedCount);
                            } else if (changedField == IssueField.ITERATION) {
                                recentChanges = Bundle.LBL_CHANGES_INCL_ITERATION(changedCount);
                            } else if (changedField == IssueField.KEYWORDS) {
                                recentChanges = Bundle.LBL_CHANGES_INCL_TAGS(changedCount);
                            } else if (changedField == IssueField.OWNER) {
                                recentChanges = Bundle.LBL_CHANGES_INCL_OWNER(changedCount);
                            } else if (changedField == IssueField.SUBTASK || changedField == IssueField.PARENT) {
                                recentChanges = Bundle.LBL_CHANGES_INCL_ASSOCIATIONS(changedCount);
                            }
                        }
                    }
                }
            } catch (CoreException ex) {
                ODCS.LOG.log(Level.WARNING, null, ex);
            }
        }
        return !oldChanges.equals(recentChanges);
    }

    void opened() {
        loading = true;
        ODCS.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run () {
                if (editorOpened()) {
//                    ensureConfigurationUptodate();
                    loading = false;
                    refreshViewData(true);
                } else {
                    // should close somehow
                }
            }
        });
    }

    void closed() {
        ODCS.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run () {
                editorClosed();
            }
        });
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
        NbTaskDataModel m = getModel();
        if (m == null) {
            return FIELD_STATUS_UPTODATE;
        }
        TaskAttribute ta = m.getLocalTaskData().getRoot().getMappedAttribute(f.getKey());
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

    public boolean refresh() {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N
        return refresh(false);
    }

    public static final String RESOLVE_FIXED = "FIXED";    
    public void addComment (final String comment, final boolean closeAsFixed) {
        assert !EventQueue.isDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N
        if(comment == null && !closeAsFixed) {
            return;
        }
        refresh();

        runWithModelLoaded(new Runnable() {
            @Override
            public void run () {
                // resolved attrs
                if (closeAsFixed) {
                    ODCS.LOG.log(Level.FINER, "resolving issue #{0} as fixed", new Object[]{getID()}); // NOI18N
                    resolve(RESOLVE_FIXED); // XXX constant?
                }
                if(comment != null) {
                    addComment(comment);
                }        

                submitAndRefresh();
            }
        });
    }

    private void addComment (final String comment) {
        if(comment != null && !comment.isEmpty()) {
            runWithModelLoaded(new Runnable() {
                @Override
                public void run () {
                    ODCS.LOG.log(Level.FINER, "adding comment [{0}] to issue #{1}", new Object[]{comment, getID()});
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
    
    void resolve (final String resolution) {
        assert !isNew();
        runWithModelLoaded(new Runnable() {
            @Override
            public void run () {
                String value = getFieldValue(IssueField.STATUS);
                if(!(value.equals("RESOLVED") && resolution.equals(getFieldValue(IssueField.RESOLUTION)))) { // NOI18N
                    setOperation(CloudDevOperation.RESOLVED);
                    NbTaskDataModel model = getModel();
                    TaskAttribute rta = model.getLocalTaskData().getRoot();
                    TaskAttribute ta = rta.getMappedAttribute(CloudDevOperation.RESOLVED.getInputId());
                    if(ta != null) { // ta can be null when changing status from CLOSED to RESOLVED
                        setTaskAttributeValue(model, ta, resolution);
                    }
                }
            }
        });
    }   
    
    private void setOperation (CloudDevOperation operation) {
        NbTaskDataModel m = getModel();
        TaskAttributeMapper mapper = m.getLocalTaskData().getAttributeMapper();
        for (TaskOperation op : mapper.getTaskOperations(m.getLocalTaskData().getRoot())) {
            if (op.getOperationId().equals(operation.toString())) {
                setOperation(op);
                return;
            }
        }
    }
    
    private void setOperation (TaskOperation operation) {
        NbTaskDataModel m = getModel();
        TaskAttribute rta = m.getLocalTaskData().getRoot();
        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.OPERATION);
        m.getLocalTaskData().getAttributeMapper().setTaskOperation(ta, operation);
        m.attributeChanged(ta);
    }
    
    public void attachPatch(File file, String description) {
        // HACK for attaching hg bundles - they are NOT patches
        boolean isPatch = !file.getName().endsWith(".hg"); //NOI18N
        addAttachment(file, description, description, null, isPatch);
    }
    
    void addAttachment (File file, final String comment, final String desc, String contentType, final boolean patch) {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; //NOI18N
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

        TaskData repositoryTaskData = getRepositoryTaskData();
        if (repositoryTaskData == null && (!synchronizeTask()
                || (repositoryTaskData = getRepositoryTaskData()) == null)) {
            // not fully initialized task, sync failed
            return;            
        }
        final TaskAttribute attAttribute = new TaskAttribute(repositoryTaskData.getRoot(),  TaskAttribute.TYPE_ATTACHMENT);
        TaskAttributeMapper mapper = attAttribute.getTaskData().getAttributeMapper();
        TaskAttribute a = attAttribute.createMappedAttribute(TaskAttribute.ATTACHMENT_DESCRIPTION);
        a.setValue(desc);
        a = attAttribute.createMappedAttribute(TaskAttribute.ATTACHMENT_CONTENT_TYPE);
        a.setValue(contentType);
        a = attAttribute.createMappedAttribute(TaskAttribute.ATTACHMENT_IS_PATCH);
        mapper.setBooleanValue(a, patch);
        a = attAttribute.createMappedAttribute(TaskAttribute.ATTACHMENT_FILENAME);
        a.setValue(file.getName());

        refresh(); // refresh might fail, but be optimistic and still try to force add attachment
        try {
            PostAttachmentCommand cmd = MylynSupport.getInstance().getCommandFactory().createPostAttachmentCommand(
                    repository.getTaskRepository(), getNbTask(), attAttribute, attachmentSource, comment);
            repository.getExecutor().execute(cmd);
            if (!cmd.hasFailed()) {
                refresh(true); // XXX to much refresh - is there no other way?
            }
        } catch (CoreException ex) {
            // should not happen
            ODCS.LOG.log(Level.WARNING, null, ex);
        }
    }

    @NbBundle.Messages({
        "# {0} - task id and summary", "MSG_ODCSIssue.statusBar.submitted=Task {0} submitted.",
        "ODCSIssue.attachment.noDescription=<no description>",
        "# {0} - the file to be attached", "LBL_AttachedPrefix=Attached file {0}"
    })
    public boolean submitAndRefresh() {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N

        final boolean[] result = new boolean[1];
        runWithModelLoaded(new Runnable() {

            @Override
            public void run () {
                assert !EventQueue.isDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N
                List<AttachmentsPanel.AttachmentInfo> newAttachments = getNewAttachments();
                if (!newAttachments.isEmpty()) {
                    // clear before submit, we do not know how connectors deal with internal attributes
                    setNewAttachments(Collections.<AttachmentsPanel.AttachmentInfo>emptyList());
                }

                final boolean wasNew = isNew();

                SubmitTaskCommand submitCmd;
                try {
                    if (saveChanges()) {
                        fireChanged();
                        submitCmd = MylynSupport.getInstance().getCommandFactory().createSubmitTaskCommand(getModel());
                    } else {
                        result[0] = false;
                        return;
                    }
                } catch (CoreException ex) {
                    ODCS.LOG.log(Level.WARNING, null, ex);
                    result[0] = false;
                    return;
                }
                repository.getExecutor().execute(submitCmd);
                if (!submitCmd.hasFailed()) {
                    taskSubmitted(submitCmd.getSubmittedTask());
                }

                if (!wasNew) {
                    refresh();
                } else {
                    RepositoryResponse rr = submitCmd.getRepositoryResponse();
                    if(!submitCmd.hasFailed()) {
                        updateRecentChanges();
                        updateTooltip();
                        fireDataChanged();
                        String id = getID();
                        repository.getIssueCache().setIssue(id, ODCSIssue.this);
                        ODCS.LOG.log(Level.FINE, "created issue #{0}", id);
                    } else {
                        ODCS.LOG.log(Level.FINE, "submiting failed");
                        if(rr != null) {
                            ODCS.LOG.log(Level.FINE, "repository response {0}", rr.getReposonseKind());
                        } else {
                            ODCS.LOG.log(Level.FINE, "no repository response available");
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
                        for (AttachmentsPanel.AttachmentInfo attachment : newAttachments) {
                            if (attachment.getFile().isFile()) {
                                if (attachment.getDescription().trim().length() == 0) {
                                    attachment.setDescription(Bundle.ODCSIssue_attachment_noDescription());
                                }
                                addAttachment(attachment.getFile(), Bundle.LBL_AttachedPrefix(attachment.getFile().getName()),
                                        attachment.getDescription(), attachment.getContentType(), attachment.isPatch()); // NOI18N
                            } else {
                                // PENDING notify user
                            }
                        }
                    }
                }
                StatusDisplayer.getDefault().setStatusText(Bundle.MSG_ODCSIssue_statusBar_submitted(
                        getDisplayName()));

                setUpToDate(true, false);
                result[0] = true;
            }
            
        });
        return result[0];
    }

    boolean updateModelAndRefresh () {
        return updateModel() && refresh();
    }

    public IssueController getController() {
        if(controller == null) {
            controller = new ODCSIssueController(this);
        }
        return controller;
    }

    public Collection<String> getSubtasks() {
        String value = getRepositoryFieldValue(IssueField.SUBTASK);
        value = value != null ? value.trim() : ""; // NOI18N
        if("".equals(value)) { // NOI18N
            return Collections.emptyList();
        } else {
            String[] ret = value.split(","); // NOI18N
            for (int i = 0; i < ret.length; i++) {
                ret[i] = ret[i].trim();
            }
            return Arrays.asList(ret);
        }
    }
    
    public boolean isSubtask() {
        String value = getRepositoryFieldValue(IssueField.PARENT);
        return value != null && !value.trim().isEmpty();
    }

    public boolean hasSubtasks() {
        return getSubtasks().size() > 0;
    }

    public String getParentId() {
        return getRepositoryFieldValue(IssueField.PARENT);
    }

    public ODCSRepository getRepository() {
        return repository;
    }
    
    public static ColumnDescriptor[] getColumnDescriptors(ODCSRepository repository) {
        ResourceBundle loc = NbBundle.getBundle(ODCSIssue.class);
        JTable t = new JTable();
        List<ColumnDescriptor> ret = new LinkedList<ColumnDescriptor>();
        // XXX is this complete ?
        
        ret.add(new IssueFieldColumnDescriptor(IssueField.ID, UIUtils.getColumnWidthInPixels(6, t)));
        ret.add(new ColumnDescriptor<String>(IssueNode.LABEL_NAME_SUMMARY, String.class,
                                          loc.getString("CTL_Issue_Summary_Title"),  // NOI18N
                                          loc.getString("CTL_Issue_Summary_Desc"))); // NOI18N           
        ret.add(new ARVColumnDescriptor(IssueField.SEVERITY));
        ret.add(new ARVColumnDescriptor(IssueField.PRIORITY));
        ret.add(new ARVColumnDescriptor(IssueField.TASK_TYPE));
        ret.add(new ARVColumnDescriptor(IssueField.STATUS));
        ret.add(new ARVColumnDescriptor(IssueField.RESOLUTION));
        ret.add(new IssueFieldColumnDescriptor(IssueField.PRODUCT, false));
        ret.add(new IssueFieldColumnDescriptor(IssueField.COMPONENT, false));
        ret.add(new ARVColumnDescriptor(IssueField.ITERATION, false));
        ret.add(new ARVColumnDescriptor(IssueField.MILESTONE, false));
        ret.add(new ARVColumnDescriptor(IssueField.DUEDATE));
        ret.add(new ARVColumnDescriptor(IssueField.ESTIMATE));
        ret.add(new ARVColumnDescriptor(IssueField.OWNER));
        ret.add(new ARVColumnDescriptor(IssueField.REPORTER));
        ret.add(new ARVColumnDescriptor(IssueField.PARENT));
        ret.add(new ARVColumnDescriptor(IssueField.SUBTASK));
        ret.add(new ARVColumnDescriptor(IssueField.DUPLICATE));
        ret.add(new IssueFieldColumnDescriptor(IssueField.MODIFIED, false));
        return ret.toArray(new ColumnDescriptor[ret.size()]);
    }

    TaskResolution getResolution() {
        String value = getRepositoryFieldValue(IssueField.RESOLUTION);
        return ODCSUtil.getResolutionByValue(repository.getRepositoryConfiguration(false), value);
    }

    Priority getPriority() {
        String value = getRepositoryFieldValue(IssueField.PRIORITY);
        return ODCSUtil.getPriorityByValue(repository.getRepositoryConfiguration(false), value);
    }

    TaskSeverity getSeverity() {
        String value = getRepositoryFieldValue(IssueField.SEVERITY);
        return ODCSUtil.getSeverityByValue(repository.getRepositoryConfiguration(false), value);
    }

    TaskStatus getTaskStatus() {
        String value = getRepositoryFieldValue(IssueField.STATUS);
        return ODCSUtil.getStatusByValue(repository.getRepositoryConfiguration(false), value);
    }

    Iteration getIteration() {
        String value = getRepositoryFieldValue(IssueField.ITERATION);
        return ODCSUtil.getIterationByValue(repository.getRepositoryConfiguration(false), value);
    }

    Milestone getMilestone() {
        String value = getRepositoryFieldValue(IssueField.MILESTONE);
        return ODCSUtil.getMilestoneByValue(repository.getRepositoryConfiguration(false), value);
    }

    @Override
    protected void taskDeleted (NbTask task) {
        getRepository().taskDeleted(getID(task));
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
    protected boolean synchronizeTask () {
        try {
            NbTask task = getNbTask();
            synchronized (task) {
                ODCS.LOG.log(Level.FINE, "refreshing issue #{0}", getID()); // NOI18N
                SynchronizeTasksCommand cmd = MylynSupport.getInstance().getCommandFactory().createSynchronizeTasksCommand(
                        getRepository().getTaskRepository(), Collections.<NbTask>singleton(task));
                getRepository().getExecutor().execute(cmd);
                return !cmd.hasFailed();
            }
        } catch (CoreException ex) {
            // should not happen
            ODCS.LOG.log(Level.WARNING, null, ex);
            return false;
        }
    }

    @Override
    protected String getSummary (TaskData taskData) {
        return getFieldValue(taskData, IssueField.SUMMARY);
    }

    @Override
    protected void taskDataUpdated () {
//        ensureConfigurationUptodate();
        ODCS.getInstance().getRequestProcessor().post(new Runnable() {
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
                    setFieldValue(IssueField.DUEDATE, "");
                } else {
                    setFieldValue(IssueField.DUEDATE, IssuePanel.INPUT_DATE_FORMAT.format(date));
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

    public String getPriorityID() {
        final Priority priority = getPriority();
        return priority != null ? priority.getId().toString() : ""; // NOI18N
    }

    private static class IssueFieldColumnDescriptor extends ColumnDescriptor<String> {
        public IssueFieldColumnDescriptor(IssueField f) {
            super(f.getKey(), 
                  String.class,
                  f.getDisplayName(),
                  f.getDescription()
                  ,0, false);
        }
        public IssueFieldColumnDescriptor(IssueField f, int width) {
            super(f.getKey(), 
                  String.class,
                  f.getDisplayName(),
                  f.getDescription()
                  ,width, true);
        }
        public IssueFieldColumnDescriptor(IssueField f, boolean visible) {
            super(f.getKey(), 
                  String.class,
                  f.getDisplayName(),
                  f.getDescription()
                  ,0, visible);
        }
    }
    private static class ARVColumnDescriptor extends ColumnDescriptor<AbstractReferenceValue> {
        public ARVColumnDescriptor(IssueField f) {
            super(f.getKey(), 
                  AbstractReferenceValue.class,
                  f.getDisplayName(),
                  f.getDescription()
                  ,0, false);
        }
        public ARVColumnDescriptor(IssueField f, int width) {
            super(f.getKey(), 
                  AbstractReferenceValue.class,
                  f.getDisplayName(),
                  f.getDescription()
                  ,width, true);
        }
        public ARVColumnDescriptor(IssueField f, boolean visible) {
            super(f.getKey(), 
                  AbstractReferenceValue.class,
                  f.getDisplayName(),
                  f.getDescription()
                  ,0, visible);
        }
    }
    
    /**************************************************************************
     * private
     **************************************************************************/

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
    
    /**
     * Returns the value represented by the given field
     *
     * @param f
     * @return
     */
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
        if(f.isSingleFieldAttribute()) {
            TaskAttribute a = taskData.getRoot().getMappedAttribute(f.getKey());
            if(a != null && a.getValues().size() > 1) {
                return listValues(a);
            }
            return a != null ? a.getValue() : ""; // NOI18N
        } else {
            List<TaskAttribute> attrs = taskData.getAttributeMapper().getAttributesByType(taskData, f.getKey());
            // returning 0 would set status MODIFIED instead of NEW
            return "" + ( attrs != null && attrs.size() > 0 ?  attrs.size() : ""); // NOI18N
        }
    }

    String getPersonName (IssueField f) {
        NbTaskDataModel m = getModel();
        TaskAttribute a = m == null ? null : m.getLocalTaskData().getRoot().getMappedAttribute(f.getKey());
        a = a!= null ? a.getMappedAttribute(TaskAttribute.PERSON_NAME) : null;
        return a != null ? a.getValue() : null; 
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
        if(f.isSingleFieldAttribute()) {
            TaskAttribute a = taskData.getRoot().getMappedAttribute(f.getKey());
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

    private static String getMappedValue(TaskAttribute a, String key) {
        TaskAttribute ma = a.getMappedAttribute(key);
        if(ma != null) {
            return ma.getValue();
        }
        return null;
    }
 
    private boolean refresh(boolean afterSubmitRefresh) { // XXX cacheThisIssue - we probalby don't need this, just always set the issue into the cache
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N
        NbTask task = getNbTask();
        boolean synced = synchronizeTask();
        assert this == getRepository().getIssueForTask(task);
//            getRepository().ensureConfigurationUptodate(this);
        if (!loading) {
            // refresh only when model is not currently being loaded
            // otherwise it most likely ends up in editor not fully initialized
            refreshViewData(afterSubmitRefresh);
        }
        return synced;
    }

    private void refreshViewData(boolean force) {
        if (controller != null) {
            // view might not exist yet and we won't unnecessarily create it
            controller.refreshViewData(force);
        }
    }
    
    void setFieldValue(IssueField f, String value) {
        NbTaskDataModel m = getModel();
        // should not happen, setFieldValue either runs with model lock
        // or it is called from issue editor in AWT - the editor could not be closed by user in the meantime
        assert m != null;
        TaskData taskData = m.getLocalTaskData();
        TaskAttribute a = taskData.getRoot().getMappedAttribute(f.getKey());
        if (a == null) {
            a = new TaskAttribute(taskData.getRoot(), f.getKey());
        }
        ODCS.LOG.log(Level.FINER, "setting value [{0}] on field [{1}]", new Object[]{value, f.getKey()}) ;
        if (!value.equals(a.getValue())) {
            setTaskAttributeValue(m, a, value);
        }
    }

    void setFieldValues(IssueField f, List<String> values) {
        NbTaskDataModel m = getModel();
        // should not happen, setFieldValue either runs with model lock
        // or it is called from issue editor in AWT - the editor could not be closed by user in the meantime
        assert m != null;
        TaskData taskData = m.getLocalTaskData();
        TaskAttribute a = taskData.getRoot().getMappedAttribute(f.getKey());
        if(a == null) {
            a = new TaskAttribute(taskData.getRoot(), f.getKey());
        }
        if (!values.equals(a.getValues())) {
            setTaskAttributeValues(m, a, values);
        }
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

    boolean setUnsubmittedAttachments (List<AttachmentsPanel.AttachmentInfo> newAttachments) {
        return super.setNewAttachments(newAttachments);
    }

    List<AttachmentsPanel.AttachmentInfo> getUnsubmittedAttachments () {
        return getNewAttachments();
    }

    void fireChangeEvent () {
        fireChanged();
    }

    class Comment {
        private final Date when;
        private final String author;
        private final String authorName;
        private final Long number;
        private final String text;

        public Comment(TaskAttribute a) {
            Date d = null;
            String s = getMappedValue(a, TaskAttribute.COMMENT_DATE);
            if (s != null && !s.trim().equals("")) {                         // NOI18N
                d = ODCSUtil.parseLongDate(s);
            }
            when = d;
            TaskAttribute authorAttr = a.getMappedAttribute(TaskAttribute.COMMENT_AUTHOR);
            if (authorAttr != null) {
                author = authorAttr.getValue();
                TaskAttribute nameAttr = authorAttr.getMappedAttribute(TaskAttribute.PERSON_NAME);
                authorName = nameAttr != null ? nameAttr.getValue() : null;
            } else {
                author = authorName = null;
            }
            String n = getMappedValue(a, TaskAttribute.COMMENT_NUMBER);
            number = n != null ? Long.parseLong(n) : null;
            text = getMappedValue(a, TaskAttribute.COMMENT_TEXT);
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

        public String getAuthor() {
            return author;
        }

        public String getAuthorName() {
            return authorName;
        }
    }

    class Time {
        private final Date when;
        private final String author;
        private final String authorName;
        private final Long number;
        private final String text;

        public Time(TaskAttribute a) {
            String s = getMappedValue(a, TaskAttribute.COMMENT_DATE);
            Date d = ODCSUtil.parseLongDate(s);
            when = d;
            TaskAttribute authorAttr = a.getMappedAttribute(TaskAttribute.COMMENT_AUTHOR);
            if (authorAttr != null) {
                author = authorAttr.getValue();
                TaskAttribute nameAttr = authorAttr.getMappedAttribute(TaskAttribute.PERSON_NAME);
                authorName = nameAttr != null ? nameAttr.getValue() : null;
            } else {
                author = authorName = null;
            }
            String n = getMappedValue(a, TaskAttribute.COMMENT_NUMBER);
            number = n != null ? Long.parseLong(n) : null;
            text = getMappedValue(a, TaskAttribute.COMMENT_TEXT);
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

        public String getAuthor() {
            return author;
        }

        public String getAuthorName() {
            return authorName;
        }
    }

    class Attachment extends AttachmentsPanel.AbstractAttachment {
        private final String desc;
        private final String filename;
        private final String author;
        private final String authorName;
        private final Date date;
        private final String id;
        private String contentType;
        private String isDeprected;
        private String size;
        private boolean isPatch;
        private String url;
        private final TaskAttribute ta;

        public Attachment(TaskAttribute ta) {
            this.ta = ta;
            id = ta.getValue();
            String s = getMappedValue(ta, TaskAttribute.ATTACHMENT_DATE);
            Date d = ODCSUtil.parseLongDate(s);
            date = d;
            filename = getMappedValue(ta, TaskAttribute.ATTACHMENT_FILENAME);
            desc = getMappedValue(ta, TaskAttribute.ATTACHMENT_DESCRIPTION);

            TaskAttribute authorAttr = ta.getMappedAttribute(TaskAttribute.ATTACHMENT_AUTHOR);
            if(authorAttr != null) {
                author = authorAttr.getValue();
                TaskAttribute nameAttr = authorAttr.getMappedAttribute(TaskAttribute.PERSON_NAME);
                authorName = nameAttr != null ? nameAttr.getValue() : null;
            } else {
                authorAttr = ta.getTaskData().getRoot().getMappedAttribute(IssueField.REPORTER.getKey()); 
                if(authorAttr != null) {
                    author = authorAttr.getValue();
                    TaskAttribute nameAttr = authorAttr.getMappedAttribute(TaskAttribute.PERSON_NAME);
                    authorName = nameAttr != null ? nameAttr.getValue() : null;
                } else {
                    author = authorName = null;
                }
            }
            contentType = getMappedValue(ta, TaskAttribute.ATTACHMENT_CONTENT_TYPE);
            isDeprected = getMappedValue(ta, TaskAttribute.ATTACHMENT_IS_DEPRECATED);
            if (ta.getMappedAttribute(TaskAttribute.ATTACHMENT_IS_PATCH) == null) {
                isPatch = filename.endsWith(".patch") || filename.endsWith(".diff"); //NOI18N
            } else {
                isPatch = !getMappedValue(ta, TaskAttribute.ATTACHMENT_IS_PATCH).isEmpty();
            }
            size = getMappedValue(ta, TaskAttribute.ATTACHMENT_SIZE);
            url = getMappedValue(ta, TaskAttribute.ATTACHMENT_URL);
        }

        @Override
        public String getAuthorName() {
            return authorName;
        }

        @Override
        public String getAuthor() {
            return author;
        }

        @Override
        public Date getDate() {
            return date;
        }

        @Override
        public String getDesc() {
            return desc;
        }

        @Override
        public String getFilename() {
            return filename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        public String getId() {
            return id;
        }

        public String getIsDeprected() {
            return isDeprected;
        }

        @Override
        public boolean isPatch() {
            return isPatch;
        }

        public String getSize() {
            return size;
        }

        public String getUrl() {
            return url;
        }

        @Override
        public void getAttachementData(final OutputStream os) {
            assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N            
            try {
                repository.getExecutor().execute(MylynSupport.getInstance().getCommandFactory()
                        .createGetAttachmentCommand(repository.getTaskRepository(), getNbTask(), ta, os));
            } catch (CoreException ex) {
                // should not happen
                ODCS.LOG.log(Level.WARNING, null, ex);
            }
        }

    }    

    public void setUpToDate (boolean seen) {
        setUpToDate(seen, true);
    }
}
