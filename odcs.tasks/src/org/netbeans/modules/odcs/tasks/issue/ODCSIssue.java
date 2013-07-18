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
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import org.netbeans.modules.bugtracking.util.AttachmentsPanel;
import com.tasktop.c2c.server.tasks.domain.TaskResolution;
import com.tasktop.c2c.server.tasks.domain.TaskSeverity;
import com.tasktop.c2c.server.tasks.domain.TaskStatus;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
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
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import oracle.eclipse.tools.cloud.dev.tasks.CloudDevAttribute;
import org.eclipse.mylyn.internal.tasks.core.data.FileTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.netbeans.modules.bugtracking.cache.IssueCache;
import org.netbeans.modules.bugtracking.issuetable.ColumnDescriptor;
import org.netbeans.modules.bugtracking.issuetable.IssueNode;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.spi.IssueProvider;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.netbeans.modules.bugtracking.util.UIUtils;
import org.netbeans.modules.mylyn.util.GetAttachmentCommand;
import org.netbeans.modules.mylyn.util.PostAttachmentCommand;
import org.netbeans.modules.mylyn.util.SubmitCommand;
import org.netbeans.modules.odcs.tasks.ODCS;
import org.netbeans.modules.odcs.tasks.repository.ODCSRepository;
import org.netbeans.modules.odcs.tasks.util.ODCSUtil;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Tomas Stupka
 */
public class ODCSIssue {

    private TaskData data;
    private final ODCSRepository repository;
    private final PropertyChangeSupport support;

    private ODCSIssueController controller;
    
    private String initialProduct = null;
    
    private ODCSIssueNode node;
    private static final Set<IssueField> UNAVAILABLE_FIELDS_IF_PARTIAL_DATA = new HashSet<IssueField>(Arrays.asList(
            IssueField.SUBTASK
    ));
    private Map<String, String> seenAtributes;
    private boolean open;
    
    /**
     * IssueProvider wasn't seen yet
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
    private HashMap<String, String> attributes;
    
    public ODCSIssue(TaskData data, ODCSRepository repo) {
        this.data = data;
        this.repository = repo;
        support = new PropertyChangeSupport(this);
    }

    public IssueNode getNode() {
        if(node == null) {
            node = new ODCSIssueNode(this);
        }
        return node;
    }
    
    TaskData getTaskData() {
        return data;
    }
    
    public String getDisplayName() {
        return getDisplayName(data);
    }
    
    /**
     * Determines the issue display name depending on the issue new state
     * @param td
     * @return 
     */
    public static String getDisplayName(TaskData td) {
        return td.isNew() ?
                NbBundle.getMessage(ODCSIssue.class, "CTL_NewIssue") : // NOI18N
                NbBundle.getMessage(ODCSIssue.class, "CTL_Issue", new Object[] {getID(td), getSummary(td)}); // NOI18N
    }

    /**
     * Determines the issue id
     * @param td
     * @return 
     */
    public String getID() {
        return getID(data);
    }
       
    /**
     * determines the given TaskData id
     * @param td
     * @return 
     */
    public static String getID(TaskData td) {
        return td.getTaskId();
    }

    public String getTooltip() {
        return getDisplayName();
    }
    
    /**
     * Returns the id from the given taskData or null if taskData.isNew()
     * @param taskData
     * @return id or null
     */
    public static String getSummary(TaskData taskData) {
        if(taskData.isNew()) {
            return null;
        }
        return getFieldValue(IssueField.SUMMARY, taskData);
    }    
    
    // XXX merge with bugzilla
    Comment[] getComments() {
        List<TaskAttribute> attrs = data.getAttributeMapper().getAttributesByType(data, TaskAttribute.TYPE_COMMENT);
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
        List<TaskAttribute> attrs = data.getAttributeMapper().getAttributesByType(data, TaskAttribute.TYPE_ATTACHMENT);
        if (attrs == null) {
            return Collections.emptyList();
        }
        List<Attachment> attachments = new ArrayList<Attachment>(attrs.size());
        for (TaskAttribute taskAttribute : attrs) {
            attachments.add(new Attachment(taskAttribute));
        }
        return attachments;
    }

    public void setSeen(boolean seen) {
        try {
            repository.getIssueCache().setSeen(getID(), seen);
        } catch (IOException ex) {
            ODCS.LOG.log(Level.WARNING, null, ex);
        }
    }
    
    private boolean wasSeen() {
        return repository.getIssueCache().wasSeen(getID());
    }

    public void setTaskData(TaskData taskData) {
//        assert !taskData.isPartial();
        data = taskData;
        
        // XXX
        attributes = null; // reset
//        availableOperations = null;
        ODCS.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
//        XXX        ((ODCSIssueNode)getNode()).fireDataChanged();
                fireDataChanged();
                refreshViewData(false);
            }
        });
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
    public String getRecentChanges() {
        if(wasSeen()) {
            return ""; //NOI18N
        }
        IssueStatusProvider.Status status = getIssueStatus();
        if(status == IssueStatusProvider.Status.NEW) {
            return Bundle.LBL_NEW_STATUS();
        } else if(status == IssueStatusProvider.Status.MODIFIED) {
            List<IssueField> changedFields = new ArrayList<IssueField>();
            assert getSeenAttributes() != null;
            for (IssueField f : IssueField.getFields()) {
                if (f == IssueField.MODIFIED || f == IssueField.CREATED || f == IssueField.REPORTER
                        || f == IssueField.ATTACHEMENT_COUNT && data.isPartial() // attachments not available with partial data
                        ) {
                    continue;
                }
                String value = getFieldValue(f);
                String seenValue = getSeenValue(f);
                if(!value.trim().equals(seenValue)) {
                    changedFields.add(f);
                }
            }
            int changedCount = changedFields.size();
            if (changedCount == 1) {
                String ret = null;
                for (IssueField changedField : changedFields) {
                    if (changedField == IssueField.SUMMARY) {
                        ret = Bundle.LBL_SUMMARY_CHANGED_STATUS();
                    } else if (changedField == IssueField.CC) {
                        ret = Bundle.LBL_CC_FIELD_CHANGED_STATUS();
                    } else if (changedField == IssueField.KEYWORDS) {
                        ret = Bundle.LBL_TAGS_CHANGED_STATUS();
                    } else if (changedField == IssueField.SUBTASK || changedField == IssueField.PARENT) {
                        ret = Bundle.LBL_DEPENDENCE_CHANGED_STATUS();
                    } else if (changedField == IssueField.COMMENT_COUNT) {
                        String value = getFieldValue(changedField);
                        String seenValue = getSeenValue(changedField);
                        if(seenValue.equals("")) {
                            seenValue = "0"; //NOI18N
                        }
                        int count = 0;
                        try {
                            count = Integer.parseInt(value) - Integer.parseInt(seenValue);
                        } catch(NumberFormatException ex) {
                            ODCS.LOG.log(Level.WARNING, ret, ex);
                        }
                        ret = Bundle.LBL_COMMENTS_CHANGED(count);
                    } else if (changedField == IssueField.ATTACHEMENT_COUNT) {
                        ret = Bundle.LBL_ATTACHMENTS_CHANGED();
                    } else {
                        ret = Bundle.LBL_CHANGED_TO(changedField.getDisplayName(), getFieldValue(changedField));
                    }
                }
                return ret;
            } else {
                String ret = Bundle.LBL_CHANGES(changedCount);
                for (IssueField changedField : changedFields) {
                    String msg = null;
                    if (changedField == IssueField.SUMMARY) {
                        msg = Bundle.LBL_CHANGES_INCL_SUMMARY(changedCount);
                    } else if (changedField == IssueField.PRIORITY) {
                        msg = Bundle.LBL_CHANGES_INCL_PRIORITY(changedCount);
                    } else if (changedField == IssueField.SEVERITY) {
                        msg = Bundle.LBL_CHANGES_INCL_SEVERITY(changedCount);
                    } else if (changedField == IssueField.TASK_TYPE) {
                        msg = Bundle.LBL_CHANGES_INCL_ISSUE_TYPE(changedCount);
                    } else if (changedField == IssueField.PRODUCT) {
                        msg = Bundle.LBL_CHANGES_INCL_PRODUCT(changedCount);
                    } else if (changedField == IssueField.COMPONENT) {
                        msg = Bundle.LBL_CHANGES_INCL_COMPONENT(changedCount);
                    } else if (changedField == IssueField.MILESTONE) {
                        msg = Bundle.LBL_CHANGES_INCL_MILESTONE(changedCount);
                    } else if (changedField == IssueField.ITERATION) {
                        msg = Bundle.LBL_CHANGES_INCL_ITERATION(changedCount);
                    } else if (changedField == IssueField.KEYWORDS) {
                        msg = Bundle.LBL_CHANGES_INCL_TAGS(changedCount);
                    } else if (changedField == IssueField.OWNER) {
                        msg = Bundle.LBL_CHANGES_INCL_OWNER(changedCount);
                    } else if (changedField == IssueField.SUBTASK || changedField == IssueField.PARENT) {
                        msg = Bundle.LBL_CHANGES_INCL_ASSOCIATIONS(changedCount);
                    }
                    if (msg != null) {
                        return msg;
                    }
                }
                return ret;
            }
        }
        return ""; //NOI18N
    }

    public Date getLastModifyDate() {
        String value = getFieldValue(IssueField.MODIFIED);
        if(value != null && !value.trim().equals("")) {
            return ODCSUtil.parseLongDate(value);
        }
        return null;
    }

    public long getLastModify() {
        Date lastModifyDate = getLastModifyDate();
        if(lastModifyDate != null) {
            return lastModifyDate.getTime();
        } else {
            return -1;
        }
    }

    public Date getCreatedDate() {
        String value = getFieldValue(IssueField.CREATED);
        if(value != null && !value.trim().equals("")) { // NOI18N
            return ODCSUtil.parseLongDate(value);
        }
        return null;
    }

    public long getCreated() {
        Date createdDate = getCreatedDate();
        if (createdDate != null) {
            return createdDate.getTime();
        } else {
            return -1;
        }
    }

    public Map<String, String> getAttributes() {
        if(attributes == null) {
            attributes = new HashMap<String, String>();
            String value;
            for (IssueField field : IssueField.getFields()) {
                value = getFieldValue(field);
                if(value != null && !value.trim().equals("")) {                 // NOI18N
                    attributes.put(field.getKey(), value);
                }
            }
        }
        return attributes;
    }

    public String getSummary() {
        return getFieldValue(IssueField.SUMMARY);
    }

    public boolean isNew() {
        return data == null || data.isNew();
    }

    void opened() {
        open = true;
        if(!data.isNew()) {
            // 1.) to get seen attributes makes no sense for new issues
            // 2.) set seenAtributes on issue open, before its actuall
            //     state is written via setSeen().
            seenAtributes = repository.getIssueCache().getSeenAttributes(getID());
        }
    }

    void closed() {
        open = false;
        seenAtributes = null;
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
        String seenValue = getSeenValue(f);
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

    String getSeenValue(IssueField f) {
        Map<String, String> attr = getSeenAttributes();
        String seenValue = attr != null ? attr.get(f.getKey()) : null;
        if(seenValue == null) {
            seenValue = "";                                                     // NOI18N
        }
        return seenValue;
    }

    public boolean isOpened () {
        return open;
    }

    public boolean isFinished() {
        String value = getFieldValue(IssueField.STATUS);
        // XXX shouldn't this be resolved via some repository settings?
        return Arrays.asList("RESOLVED", "VERIFIED", "CLOSED").contains(value); // NOI18N
    }

    public boolean refresh() {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N
        return refresh(getID(), false);
    }

    public static final String RESOLVE_FIXED = "FIXED";    
    public void addComment(String comment, boolean closeAsFixed) {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N
        if(comment == null && !closeAsFixed) {
            return;
        }
        refresh();

        // resolved attrs
        if(closeAsFixed) {
            ODCS.LOG.log(Level.FINER, "resolving issue #{0} as fixed", new Object[]{getID()}); // NOI18N
            resolve(RESOLVE_FIXED); // XXX constant?
        }
        if(comment != null) {
            addComment(comment);
        }        

        submitAndRefresh();
    }

    public void addComment(String comment) {
        if(comment != null) {
            ODCS.LOG.log(Level.FINER, "adding comment [{0}] to issue #{1}", new Object[]{comment, getID()}); // NOI18N
            TaskAttribute ta = data.getRoot().createMappedAttribute(TaskAttribute.COMMENT_NEW);
            ta.setValue(comment);
        }
    }
    
    void resolve(String resolution) {
        assert !data.isNew();

        String value = getFieldValue(IssueField.STATUS);
        if(!value.equals("RESOLVED")) { // NOI18N
            RepositoryConfiguration clientData = repository.getRepositoryConfiguration(false);
            List<TaskResolution> resolutions = clientData.getResolutions(); 
            for (TaskResolution r : resolutions) {
                if(r.getValue().equals(resolution)) {
                    TaskAttribute rta = data.getRoot();
                    TaskAttribute ta = rta.getMappedAttribute(CloudDevAttribute.STATUS.getTaskName());
                    ta.setValue("RESOLVED"); // NOI18N
                    ta = rta.getMappedAttribute(CloudDevAttribute.RESOLUTION.getTaskName());
                    ta.setValue(r.getValue());
                }
            }
        }
    }   
    
    public void attachPatch(File file, String description) {
        // HACK for attaching hg bundles - they are NOT patches
        boolean isPatch = !file.getName().endsWith(".hg"); //NOI18N
        addAttachment(file, null, description, null, isPatch);
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

        final TaskAttribute attAttribute = new TaskAttribute(data.getRoot(),  TaskAttribute.TYPE_ATTACHMENT);
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
        PostAttachmentCommand cmd = new PostAttachmentCommand(ODCS.getInstance().getRepositoryConnector(),
                repository.getTaskRepository(), getAsTask(), attAttribute, attachmentSource, comment);
        repository.getExecutor().execute(cmd);
        if (!cmd.hasFailed()) {
            refresh(getID(), true); // XXX to much refresh - is there no other way?
        }
    }

    boolean submitAndRefresh() {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N

//        prepareSubmit(); XXX
        final boolean wasNew = data.isNew();
        final boolean wasSeenAlready = wasNew || repository.getIssueCache().wasSeen(getID());
        
        SubmitCommand submitCmd = 
            new SubmitCommand(
                ODCS.getInstance().getRepositoryConnector(),
                getRepository().getTaskRepository(), 
                data);
        repository.getExecutor().execute(submitCmd);

        if (!wasNew) {
            refresh();
        } else {
            RepositoryResponse rr = submitCmd.getRepositoryResponse();
            if(!submitCmd.hasFailed()) {
                assert rr != null;
                String id = rr.getTaskId();
                ODCS.LOG.log(Level.FINE, "created issue #{0}", id); // NOI18N
                refresh(id, true);
            } else {
                ODCS.LOG.log(Level.FINE, "submiting failed"); // NOI18N
                if(rr != null) {
                    ODCS.LOG.log(Level.FINE, "repository response {0}", rr.getReposonseKind()); // NOI18N
                } else { 
                    ODCS.LOG.log(Level.FINE, "no repository response available"); // NOI18N
                }
            }
        }

        if(submitCmd.hasFailed()) {
            return false;
        }

        // it was the user who made the changes, so preserve the seen status if seen already
        if (wasSeenAlready) {
            try {
                repository.getIssueCache().setSeen(getID(), true);
                // it was the user who made the changes, so preserve the seen status if seen already
            } catch (IOException ex) {
                ODCS.LOG.log(Level.SEVERE, null, ex);
            }
        }
        if(wasNew) {
            // a new issue was created -> refresh all queries
            // XXX repository.refreshAllQueries();
        }

        seenAtributes = null;
        setSeen(true);
        return true;
    }

    public BugtrackingController getController() {
        if(controller == null) {
            controller = new ODCSIssueController(this);
        }
        return controller;
    }

    public String[] getSubtasks() {
        String value = getFieldValue(IssueField.SUBTASK);
        if(value != null) {
            String[] ret = value.split(",");
            for (int i = 0; i < ret.length; i++) {
                ret[i] = ret[i].trim();
            }
            return ret;
        } else {
            return new String[0];
        }
    }
    
    public boolean isSubtask() {
        String value = getFieldValue(IssueField.PARENT);
        return value != null && !value.trim().isEmpty();
    }

    public boolean hasSubtasks() {
        return getSubtasks().length > 0;
    }

    public String getParentId() {
        return getFieldValue(IssueField.PARENT);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
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
        ret.add(new ARVColumnDescriptor(IssueField.STATUS));
        ret.add(new ARVColumnDescriptor(IssueField.RESOLUTION));
        ret.add(new IssueFieldColumnDescriptor(IssueField.PRODUCT, false));
        ret.add(new IssueFieldColumnDescriptor(IssueField.COMPONENT, false));
        ret.add(new ARVColumnDescriptor(IssueField.ITERATION, false));
        ret.add(new ARVColumnDescriptor(IssueField.MILESTONE, false));
        ret.add(new IssueFieldColumnDescriptor(IssueField.MODIFIED, false));
        return ret.toArray(new ColumnDescriptor[ret.size()]);
    }

    TaskResolution getResolution() {
        String value = getFieldValue(IssueField.RESOLUTION);
        return ODCSUtil.getResolutionByValue(repository.getRepositoryConfiguration(false), value);
    }

    Priority getPriority() {
        String value = getFieldValue(IssueField.PRIORITY);
        return ODCSUtil.getPriorityByValue(repository.getRepositoryConfiguration(false), value);
    }
    
    String getType() {
        return getFieldValue(IssueField.TASK_TYPE);
    }

    TaskSeverity getSeverity() {
        String value = getFieldValue(IssueField.SEVERITY);
        return ODCSUtil.getSeverityByValue(repository.getRepositoryConfiguration(false), value);
    }

    TaskStatus getStatus() {
        String value = getFieldValue(IssueField.STATUS);
        return ODCSUtil.getStatusByValue(repository.getRepositoryConfiguration(false), value);
    }

    Iteration getIteration() {
        String value = getFieldValue(IssueField.ITERATION);
        return ODCSUtil.getIterationByValue(repository.getRepositoryConfiguration(false), value);
    }

    Milestone getMilestone() {
        String value = getFieldValue(IssueField.MILESTONE);
        return ODCSUtil.getMilestoneByValue(repository.getRepositoryConfiguration(false), value);
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

    /**
     * Returns the value represented by the given field
     *
     * @param f
     * @return
     */
    public String getFieldValue(IssueField f) {
        return getFieldValue(f, data);
    }

    private static String getFieldValue(IssueField f, TaskData taskData) {
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

    public String getPersonName(IssueField f) {
        TaskAttribute a = data.getRoot().getMappedAttribute(f.getKey());
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

    public List<String> getFieldValues(IssueField f) {
        if(f.isSingleFieldAttribute()) {
            TaskAttribute a = data.getRoot().getMappedAttribute(f.getKey());
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

    boolean isFieldValueAvailable (IssueField field) {
        return !(data.isPartial() && UNAVAILABLE_FIELDS_IF_PARTIAL_DATA.contains(field));
    }

    private String getMappedValue(TaskAttribute a, String key) {
        TaskAttribute ma = a.getMappedAttribute(key);
        if(ma != null) {
            return ma.getValue();
        }
        return null;
    }

    /**
     * Notify listeners on this issue that its data were changed
     */
    private void fireDataChanged() {
        support.firePropertyChange(IssueProvider.EVENT_ISSUE_REFRESHED, null, null);
    }

    private boolean refresh(String id, boolean afterSubmitRefresh) { // XXX cacheThisIssue - we probalby don't need this, just always set the issue into the cache
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt"; // NOI18N
        // XXX 
        // XXX gettaskdata the same for bugzilla, jira, odcs, ...
        try {
            ODCS.LOG.log(Level.FINE, "refreshing issue #{0}", id); // NOI18N
            TaskData td = ODCSUtil.getTaskData(repository, id);
            if (td == null) {
                return false;
            }
            setTaskData(td);
            getRepository().getIssueCache().setIssueData(td.getTaskId(), this); // XXX
//            getRepository().ensureConfigurationUptodate(this);
            refreshViewData(afterSubmitRefresh);
        } catch (IOException ex) {
            ODCS.LOG.log(Level.SEVERE, null, ex);
        }
        return true;
    }

    private void refreshViewData(boolean force) {
        if (controller != null) {
            // view might not exist yet and we won't unnecessarily create it
            controller.refreshViewData(force);
        }
    }
    
    void setFieldValue(IssueField f, String value) {
//        if(f.isReadOnly()) { XXX
//            assert false : "can't set value into IssueField " + f.getKey();       // NOI18N
//            return;
//        }
        TaskAttribute a = data.getRoot().getMappedAttribute(f.getKey());
        if(a == null) {
            a = new TaskAttribute(data.getRoot(), f.getKey());
        }
        if(f == IssueField.PRODUCT) {
            handleProductChange(a);
        }
        ODCS.LOG.log(Level.FINER, "setting value [{0}] on field [{1}]", new Object[]{value, f.getKey()}); // NOI18N
        a.setValue(value);
    }

    void setFieldValues(IssueField f, List<String> ccs) {
        TaskAttribute a = data.getRoot().getMappedAttribute(f.getKey());
        if(a == null) {
            a = new TaskAttribute(data.getRoot(), f.getKey());
        }
        a.setValues(ccs);
    }
    
    private void handleProductChange(TaskAttribute a) {
        if(!data.isNew() && initialProduct == null) {
            initialProduct = a.getValue();
        }
    }    

    private ITask getAsTask () {
        return new ITask() {
            //<editor-fold defaultstate="collapsed" desc="dummy impl">
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
            
            @Override
            public Object getAdapter(Class type) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            
            @Override
            public Map<String, String> getAttributes() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            //</editor-fold>
        };
    }

    private RepositoryConfiguration getConfiguration () {
        return repository.getRepositoryConfiguration(false);
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
                authorAttr = data.getRoot().getMappedAttribute(IssueField.REPORTER.getKey()); 
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
            repository.getExecutor().execute(new GetAttachmentCommand(ODCS.getInstance().getRepositoryConnector(), 
                    repository.getTaskRepository(),
                    null, ta, os));
        }

    }    
    
    public IssueStatusProvider.Status getIssueStatus() {
        IssueCache.Status status = getRepository().getIssueCache().getStatus(getID());
        IssueStatusProvider.Status ret = null;
        switch(status) {
            case ISSUE_STATUS_NEW:
                ret = IssueStatusProvider.Status.NEW;
                break;
            case ISSUE_STATUS_MODIFIED:
                ret = IssueStatusProvider.Status.MODIFIED;
                break;
            case ISSUE_STATUS_SEEN:
                ret = IssueStatusProvider.Status.SEEN;
                break;
        }
        return ret;        
    }
}
