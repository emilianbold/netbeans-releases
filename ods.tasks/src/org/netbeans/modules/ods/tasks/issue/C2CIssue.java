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
package org.netbeans.modules.ods.tasks.issue;

import org.netbeans.modules.bugtracking.util.AttachmentsPanel;
import com.tasktop.c2c.server.tasks.domain.TaskResolution;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import org.eclipse.mylyn.internal.tasks.core.data.FileTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.IRepositoryElement;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.netbeans.modules.bugtracking.issuetable.ColumnDescriptor;
import org.netbeans.modules.bugtracking.issuetable.IssueNode;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.spi.IssueProvider;
import org.netbeans.modules.bugtracking.util.UIUtils;
import org.netbeans.modules.mylyn.util.GetAttachmentCommand;
import org.netbeans.modules.mylyn.util.PostAttachmentCommand;
import org.netbeans.modules.mylyn.util.SubmitCommand;
import org.netbeans.modules.ods.tasks.C2C;
import org.netbeans.modules.ods.tasks.repository.C2CRepository;
import org.netbeans.modules.ods.tasks.spi.C2CData;
import org.netbeans.modules.ods.tasks.spi.C2CExtender;
import org.netbeans.modules.ods.tasks.util.C2CUtil;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
public class C2CIssue {

    private TaskData data;
    private final C2CRepository repository;
    private final PropertyChangeSupport support;

    static final String LABEL_NAME_ID           = "c2c.issue.id";               // NOI18N
    static final String LABEL_NAME_SEVERITY     = "c2c.issue.severity";         // NOI18N
    static final String LABEL_NAME_TASK_TYPE    = "c2c.issue.task_type";        // NOI18N
    static final String LABEL_NAME_PRIORITY     = "c2c.issue.priority";         // NOI18N
    static final String LABEL_NAME_STATUS       = "c2c.issue.status";           // NOI18N
    static final String LABEL_NAME_RESOLUTION   = "c2c.issue.resolution";       // NOI18N
    static final String LABEL_NAME_PRODUCT      = "c2c.issue.product";          // NOI18N
    static final String LABEL_NAME_COMPONENT    = "c2c.issue.component";        // NOI18N
    static final String LABEL_NAME_VERSION      = "c2c.issue.version";          // NOI18N
    static final String LABEL_NAME_MILESTONE    = "c2c.issue.milestone";        // NOI18N
    static final String LABEL_NAME_MODIFIED     = "c2c.issue.modified";         // NOI18N 
            
    private C2CIssueController controller;
    
    private String initialProduct = null;
    
    private static final RequestProcessor parallelRP = new RequestProcessor("C2CIssue", 5); //NOI18N
    private C2CIssueNode node;
    private static final Set<IssueField> UNAVAILABLE_FIELDS_IF_PARTIAL_DATA = new HashSet<IssueField>(Arrays.asList(
            IssueField.SUBTASK
    ));
    
    public C2CIssue(TaskData data, C2CRepository repo) {
        this.data = data;
        this.repository = repo;
        support = new PropertyChangeSupport(this);
    }

    public IssueNode getNode() {
        if(node == null) {
            node = new C2CIssueNode(this);
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
                NbBundle.getMessage(C2CIssue.class, "CTL_NewIssue") : // NOI18N
                NbBundle.getMessage(C2CIssue.class, "CTL_Issue", new Object[] {getID(td), getSummary(td)}); // NOI18N
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
    List<C2CAttachment> getAttachments() {
        List<TaskAttribute> attrs = data.getAttributeMapper().getAttributesByType(data, TaskAttribute.TYPE_ATTACHMENT);
        if (attrs == null) {
            return Collections.emptyList();
        }
        List<C2CAttachment> attachments = new ArrayList<C2CAttachment>(attrs.size());
        for (TaskAttribute taskAttribute : attrs) {
            attachments.add(new C2CAttachment(taskAttribute));
        }
        return attachments;
    }

    public void setSeen(boolean seen) throws IOException {
        repository.getIssueCache().setSeen(getID(), seen);
    }

    public void setTaskData(TaskData taskData) {
//        assert !taskData.isPartial();
        data = taskData;
        
        // XXX
//        attributes = null; // reset
//        availableOperations = null;
        C2C.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
//        XXX        ((C2CIssueNode)getNode()).fireDataChanged();
                fireDataChanged();
                refreshViewData(false);
            }
        });
    }

    public String getRecentChanges() {
        return ""; // XXX 
    }

    public Date getLastModifyDate() {
        String value = getFieldValue(IssueField.MODIFIED);
        if(value != null && !value.trim().equals("")) {
            return C2CUtil.parseDate(value);
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
        if(value != null && !value.trim().equals("")) {
            return C2CUtil.parseDate(value);
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
        return Collections.emptyMap();
    }

    public String getSummary() {
        return getFieldValue(IssueField.SUMMARY);
    }

    public boolean isNew() {
        return data == null || data.isNew();
    }

    public boolean isFinished() {
        String value = getFieldValue(IssueField.STATUS);
        return Arrays.asList("RESOLVED", "VERIFIED", "CLOSED").contains(value);
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
            C2C.LOG.log(Level.FINER, "resolving issue #{0} as fixed", new Object[]{getID()});
            resolve(RESOLVE_FIXED); // XXX constant?
        }
        if(comment != null) {
            addComment(comment);
        }        

        submitAndRefresh();
    }

    public void addComment(String comment) {
        if(comment != null) {
            C2C.LOG.log(Level.FINER, "adding comment [{0}] to issue #{1}", new Object[]{comment, getID()});
            TaskAttribute ta = data.getRoot().createMappedAttribute(TaskAttribute.COMMENT_NEW);
            ta.setValue(comment);
        }
    }
    
    void resolve(String resolution) {
        assert !data.isNew();

        String value = getFieldValue(IssueField.STATUS);
        if(!value.equals("RESOLVED")) {                                         // NOI18N
            // XXX hacked!
            // see https://q.tasktop.com/alm/#projects/netbeanssupport/wiki/p/C2C+Mylyn+connector+usage+in+NetBeans-Query 
            // and implement appropriatelly
            final C2CData clientData = C2C.getInstance().getClientData(repository);
            List<TaskResolution> resolutions = clientData.getResolutions(); 
            for (TaskResolution r : resolutions) {
                if(r.getValue().equals(resolution)) {
                    C2CExtender.resolve(data, r);
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
        PostAttachmentCommand cmd = new PostAttachmentCommand(C2C.getInstance().getRepositoryConnector(),
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
                C2C.getInstance().getRepositoryConnector(),
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
                C2C.LOG.log(Level.FINE, "created issue #{0}", id);
                refresh(id, true);
            } else {
                C2C.LOG.log(Level.FINE, "submiting failed");
                if(rr != null) {
                    C2C.LOG.log(Level.FINE, "repository response {0}", rr.getReposonseKind());
                } else {
                    C2C.LOG.log(Level.FINE, "no repository response available");
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
                C2C.LOG.log(Level.SEVERE, null, ex);
            }
        }
        if(wasNew) {
            // a new issue was created -> refresh all queries
            // XXX repository.refreshAllQueries();
        }

        try {
            // XXX seenAtributes = null;
            setSeen(true);
        } catch (IOException ex) {
            C2C.LOG.log(Level.SEVERE, null, ex);
        }
        return true;
    }

    public BugtrackingController getController() {
        if(controller == null) {
            controller = new C2CIssueController(this);
        }
        return controller;
    }

    public String[] getSubtasks() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public C2CRepository getRepository() {
        return repository;
    }
    
    public static ColumnDescriptor[] getColumnDescriptors(C2CRepository repository) {
        ResourceBundle loc = NbBundle.getBundle(C2CIssue.class);
        JTable t = new JTable();
        List<ColumnDescriptor<String>> ret = new LinkedList<ColumnDescriptor<String>>();
        
        // XXX is this complete ?
        ret.add(new ColumnDescriptor<String>(LABEL_NAME_ID, String.class,
                                          loc.getString("CTL_Issue_ID_Title"),                // NOI18N
                                          loc.getString("CTL_Issue_ID_Desc"),                 // NOI18N
                                          UIUtils.getColumnWidthInPixels(6, t)));
        ret.add(new ColumnDescriptor<String>(IssueNode.LABEL_NAME_SUMMARY, String.class,
                                          loc.getString("CTL_Issue_Summary_Title"),           // NOI18N
                                          loc.getString("CTL_Issue_Summary_Desc")));          // NOI18N
        ret.add(new ColumnDescriptor<String>(LABEL_NAME_SEVERITY, String.class,
                                          loc.getString("CTL_Issue_Severity_Title"),          // NOI18N
                                          loc.getString("CTL_Issue_Severity_Desc"),           // NOI18N
                                          0));
        ret.add(new ColumnDescriptor<String>(LABEL_NAME_PRIORITY, String.class,
                                          loc.getString("CTL_Issue_Priority_Title"),          // NOI18N
                                          loc.getString("CTL_Issue_Priority_Desc"),           // NOI18N
                                          0));
        ret.add(new ColumnDescriptor<String>(LABEL_NAME_STATUS, String.class,
                                          loc.getString("CTL_Issue_Status_Title"),            // NOI18N
                                          loc.getString("CTL_Issue_Status_Desc"),             // NOI18N
                                          0));
        ret.add(new ColumnDescriptor<String>(LABEL_NAME_RESOLUTION, String.class,
                                          loc.getString("CTL_Issue_Resolution_Title"),        // NOI18N
                                          loc.getString("CTL_Issue_Resolution_Desc"),         // NOI18N
                                          0));
        ret.add(new ColumnDescriptor<String>(LABEL_NAME_PRODUCT, String.class,
                                          loc.getString("CTL_Issue_Product_Title"),          // NOI18N
                                          loc.getString("CTL_Issue_Product_Desc"),           // NOI18N
                                          0, false));
        ret.add(new ColumnDescriptor<String>(LABEL_NAME_COMPONENT, String.class,
                                          loc.getString("CTL_Issue_Component_Title"),          // NOI18N
                                          loc.getString("CTL_Issue_Component_Desc"),           // NOI18N
                                          0, false));
        ret.add(new ColumnDescriptor<String>(LABEL_NAME_VERSION, String.class,
                                          loc.getString("CTL_Issue_Version_Title"),          // NOI18N
                                          loc.getString("CTL_Issue_Version_Desc"),           // NOI18N
                                          0, false));
        ret.add(new ColumnDescriptor<String>(LABEL_NAME_MILESTONE, String.class,
                                          loc.getString("CTL_Issue_Milestone_Title"),          // NOI18N
                                          loc.getString("CTL_Issue_Milestone_Desc"),           // NOI18N
                                          0, false));
        ret.add(new ColumnDescriptor<String>(LABEL_NAME_MODIFIED, String.class,
                                          loc.getString("CTL_Issue_Modification_Title"),          // NOI18N
                                          loc.getString("CTL_Issue_Modification_Desc"),           // NOI18N
                                          0, false));
        return ret.toArray(new ColumnDescriptor[ret.size()]);
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
        // XXX gettaskdata the same for bugzilla, jira, c2c, ...
        try {
            C2C.LOG.log(Level.FINE, "refreshing issue #{0}", id);
            TaskData td = C2CUtil.getTaskData(repository, id);
            if (td == null) {
                return false;
            }
            getRepository().getIssueCache().setIssueData(this, td); // XXX
//            getRepository().ensureConfigurationUptodate(this);
            refreshViewData(afterSubmitRefresh);
        } catch (IOException ex) {
            C2C.LOG.log(Level.SEVERE, null, ex);
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
        C2C.LOG.log(Level.FINER, "setting value [{0}] on field [{1}]", new Object[]{value, f.getKey()}) ;
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
                d = C2CUtil.parseDate(s);
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
            Date d = C2CUtil.parseDate(s);
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

    class C2CAttachment extends AttachmentsPanel.AbstractAttachment {
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

        public C2CAttachment(TaskAttribute ta) {
            this.ta = ta;
            id = ta.getValue();
            String s = getMappedValue(ta, TaskAttribute.ATTACHMENT_DATE);
            Date d = C2CUtil.parseDate(s);
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
            repository.getExecutor().execute(new GetAttachmentCommand(C2C.getInstance().getRepositoryConnector(), 
                    repository.getTaskRepository(),
                    null, ta, os));
        }

    }    
}
