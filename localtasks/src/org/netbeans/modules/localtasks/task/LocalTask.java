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
package org.netbeans.modules.localtasks.task;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.eclipse.mylyn.internal.tasks.core.data.FileTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.spi.IssueController;
import org.netbeans.modules.localtasks.LocalRepository;
import org.netbeans.modules.localtasks.util.FileUtils;
import org.netbeans.modules.bugtracking.spi.IssueProvider;
import org.netbeans.modules.bugtracking.spi.IssueScheduleInfo;
import org.netbeans.modules.bugtracking.util.AttachmentsPanel;
import org.netbeans.modules.bugtracking.util.AttachmentsPanel.AttachmentInfo;
import org.netbeans.modules.mylyn.util.NbTask;
import org.netbeans.modules.mylyn.util.NbTaskDataModel;
import org.netbeans.modules.mylyn.util.localtasks.AbstractLocalTask;
import org.netbeans.modules.mylyn.util.localtasks.IssueField;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;

/**
 *
 * @author Ondrej Vrabec
 */
public final class LocalTask extends AbstractLocalTask {

    private final NbTask task;
    private final PropertyChangeSupport support;
    private final LocalRepository repository;
    private TaskController controller;
    private boolean loading;
    private static final RequestProcessor RP = LocalRepository.getInstance().getRequestProcessor();
    private static final String NB_ATTACHMENT = "nb.attachment."; //NOI18N
    private static final String NB_TASK_REFERENCE = "nb.taskreference."; //NOI18N

    private List<AttachmentInfo> unsavedAttachments;

    public LocalTask (NbTask task) {
        super(task);
        this.task = task;
        this.repository = LocalRepository.getInstance();
        support = new PropertyChangeSupport(this);
    }

    @Override
    protected void taskDeleted (NbTask task) {
        repository.taskDeleted(getID());
    }

    @Override
    protected void attributeChanged (NbTaskDataModel.NbTaskDataModelEvent event, NbTaskDataModel model) {
        getTaskController().modelStateChanged(model.isDirty() || hasUnsavedAttributes());
    }

    @Override
    protected void modelSaved (NbTaskDataModel model) {
        getTaskController().modelStateChanged(model.isDirty() || hasUnsavedAttributes());
    }

    @Override
    protected String getSummary (TaskData taskData) {
        return getFieldValue(taskData, IssueField.SUMMARY);
    }

    @Override
    protected void taskDataUpdated () {
        RP.post(new Runnable() {
            @Override
            public void run () {
                fireDataChanged();
                getTaskController().refreshViewData();
            }
        });
    }

    @Override
    protected void taskModified (boolean syncStateChanged) {
        fireDataChanged();
    }

    @Override
    protected void repositoryTaskDataLoaded (TaskData repositoryTaskData) {
        // NO OP
    }

    @Override
    public boolean synchronizeTask () {
        return true;
    }

    @NbBundle.Messages({
        "# {0} - task id", "# {1} - task summary", "LBL_LocalTask.displayName=#{0} - {1}"
    })
    public String getDisplayName () {
        return Bundle.LBL_LocalTask_displayName(getID(task), task.getSummary());
    }

    public String getTooltip () {
        return getDisplayName();
    }

    public void addPropertyChangeListener (PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener (PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public boolean searchFor (String[] keywords) {
        String summary = getSummary().toLowerCase();
        for (String kw : keywords) {
            kw = kw.toLowerCase();
            if (summary.contains(kw)) {
                return true;
            }
        }
        return false;
    }

    public IssueController getController () {
        return getTaskController();
    }
    
    TaskController getTaskController () {
        if (controller == null) {
            controller = new TaskController(this);
        }
        return controller;
    }

    void opened () {
        loading = true;
        LocalRepository.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run () {
                if (editorOpened()) {
                    loading = false;
                    getTaskController().refreshViewData();
                } else {
                    // should close somehow
                }
            }
        });
    }

    void closed () {
        LocalRepository.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run () {
                if (hasUnsavedAttributes() || !isMarkedNewUnread()) {
                    save();
                    getTaskController().modelStateChanged(hasUnsavedChanges());
                }
                editorClosed();
            }
        });
    }

    public void delete () {
        if (controller != null) {
            controller.taskDeleted();
        }
        deleteTask();
    }

    void markUserChange () {
        markNewRead();
    }

    String getFieldValue (IssueField field) {
        NbTaskDataModel model = getModel();
        return getFieldValue(model == null ? null : model.getLocalTaskData(), field);
    }

    List<String> getFieldValues (IssueField field) {
        NbTaskDataModel model = getModel();
        return getFieldValues(model == null ? null : model.getLocalTaskData(), field);
    }

    void setFieldValue (IssueField field, String value) {
        NbTaskDataModel m = getModel();
        // should not happen, setFieldValue either runs with model lock
        // or it is called from issue editor in AWT - the editor could not be closed by user in the meantime
        assert m != null;
        TaskData taskData = m.getLocalTaskData();
        TaskAttribute a = taskData.getRoot().getMappedAttribute(field.getKey());
        assert a != null : field.getKey();
        if (!value.equals(a.getValue())) {
            setValue(m, a, value);
        }
    }

    private static String getFieldValue (TaskData taskData, IssueField field) {
        if (taskData == null) {
            return "";
        }
        TaskAttribute a = taskData.getRoot().getAttribute(field.getKey());
        if (a == null) {
            return ""; //NOI18N
        } else if (a.getValues().size() > 1) {
            return listValues(a);
        } else {
            return a.getValue();
        }
    }

    private static List<String> getFieldValues (TaskData taskData, IssueField field) {
        if (taskData == null) {
            return Collections.<String>emptyList();
        }
        TaskAttribute a = taskData.getRoot().getAttribute(field.getKey());
        if (a == null) {
            return Collections.<String>emptyList();
        } else {
            return a.getValues();
        }
    }

    private static String listValues (TaskAttribute a) {
        if (a == null) {
            return ""; //NOI18N
        }
        StringBuilder sb = new StringBuilder();
        for (String s : a.getValues()) {
            sb.append(s);
            sb.append(","); //NOI18N
        }
        if (sb.length() > 0) {
            sb.delete(sb.length() - 1, sb.length());
        }
        return sb.toString();
    }

    private void setValue (NbTaskDataModel model, TaskAttribute ta, String value) {
        ta.setValue(value);
        model.attributeChanged(ta);
    }

    private void fireDataChanged () {
        support.firePropertyChange(IssueProvider.EVENT_ISSUE_REFRESHED, null, null);
    }

    private boolean hasUnsavedAttributes () {
        return unsavedAttachments != null || hasUnsavedPrivateTaskAttributes();
    }
    
    public boolean save () {
        NbTaskDataModel model = getModel();
        TaskData taskData = model == null ? null : model.getLocalTaskData();
        if (taskData != null) {
            getNbTask().setSummary(taskData.getRoot().getAttribute(IssueField.SUMMARY.getKey()).getValue());
            persistAttachments(model, taskData);
            fireDataChanged();
        }
        return saveChanges();
    }

    void clearModifications () {
        unsavedAttachments = null;
        clearUnsavedChanges();
    }

    boolean hasSubtasks () {
        return !getTaskReferences().isEmpty();
    }

    List<Attachment> getAttachments () {
        NbTaskDataModel model = getModel();
        TaskData taskData = model == null ? null : model.getLocalTaskData();
        List<Attachment> attachments = new ArrayList<>();
        TaskAttribute parentTA = taskData == null ? null : taskData.getRoot().getAttribute(IssueField.ATTACHMENTS.getKey());
        if (parentTA != null) {
            for (TaskAttribute ta : parentTA.getAttributes().values()) {
                if (ta.getId().startsWith(NB_ATTACHMENT)) {
                    attachments.add(new Attachment(ta));
                }
            }
        }
        return attachments;
    }

    List<AttachmentInfo> getUnsubmittedAttachments () {
        return unsavedAttachments == null
                ? Collections.<AttachmentInfo>emptyList()
                : Collections.unmodifiableList(unsavedAttachments);
    }

    void setUnsubmittedAttachments (List<AttachmentInfo> attachments) {
        unsavedAttachments = new ArrayList<>(attachments);
        getTaskController().modelStateChanged(true);
    }

    private void persistAttachments (NbTaskDataModel model, TaskData td) {
        if (unsavedAttachments != null) {
            TaskAttribute parentTA = td.getRoot().getAttribute(IssueField.ATTACHMENTS.getKey());
            if (parentTA == null) {
                parentTA = td.getRoot().createAttribute(IssueField.ATTACHMENTS.getKey());
            }
            for (AttachmentInfo att : unsavedAttachments) {
                File file = att.getFile();
                if (file != null) {
                    String desc = att.getDescription();
                    String contentType = att.getContentType();
                    boolean isPatch = att.isPatch();
                    addAttachment(model, parentTA, file, desc, contentType, isPatch);
                }
            }
            unsavedAttachments.clear();
        }
    }

    private void addAttachment (NbTaskDataModel model, TaskAttribute parentTA,
            File file, String desc, String contentType, boolean isPatch) {
        if (desc == null) {
            desc = "";
        }
        if (contentType == null) {
            file = FileUtil.normalizeFile(file);
            String ct = FileUtil.getMIMEType(FileUtil.toFileObject(file));
            if ((ct != null) && (!"content/unknown".equals(ct))) { // NOI18N
                contentType = ct;
            } else {
                contentType = FileTaskAttachmentSource.getContentTypeFromFilename(file.getName());
            }
        }
        int attachmentIndex = parentTA.getAttributes().size();
        TaskAttribute attachment = parentTA.createAttribute(NB_ATTACHMENT + attachmentIndex);
        TaskAttachmentMapper mapper = new TaskAttachmentMapper();
        mapper.setAttachmentId(String.valueOf(attachmentIndex));
        mapper.setDescription(desc);
        mapper.setFileName(file.getName());
        mapper.setPatch(isPatch);
        mapper.setCreationDate(new Date());
        mapper.setContentType(contentType);
        mapper.setUrl(Utilities.toURI(file).toString());
        mapper.applyTo(attachment);
        model.attributeChanged(parentTA);
    }
    
    private void deleteAttachment (TaskAttribute ta) {
        NbTaskDataModel model = getModel();
        TaskData taskData = model == null ? null : model.getLocalTaskData();
        TaskAttribute parentTA = taskData == null ? null : taskData.getRoot().getAttribute(IssueField.ATTACHMENTS.getKey());
        if (parentTA != null) {
            if (parentTA.getAttribute(ta.getId()) != null) {
                parentTA.removeAttribute(ta.getId());
                model.attributeChanged(parentTA);
                getTaskController().attachmentDeleted();
            }
        }
    }

    void addTaskReference (Issue task) {
        NbTaskDataModel model = getModel();
        TaskData taskData = model == null ? null : model.getLocalTaskData();
        if (taskData != null) {
            TaskAttribute parentTA = taskData.getRoot().getAttribute(IssueField.REFERENCES.getKey());
            if (parentTA == null) {
                parentTA = taskData.getRoot().createAttribute(IssueField.REFERENCES.getKey());
            }
            int index = parentTA.getAttributes().size();
            TaskAttribute attr = parentTA.createAttribute(NB_TASK_REFERENCE + index);
            TaskReference taskRef = new TaskReference(task);
            taskRef.applyTo(attr);
            model.attributeChanged(parentTA);
        }
    }

    List<TaskReference> getTaskReferences () {
        NbTaskDataModel model = getModel();
        TaskData taskData = model == null ? null : model.getLocalTaskData();
        List<TaskReference> references = new ArrayList<>();
        TaskAttribute parentTA = taskData == null ? null : taskData.getRoot().getAttribute(IssueField.REFERENCES.getKey());
        if (parentTA != null) {
            for (TaskAttribute ta : parentTA.getAttributes().values()) {
                if (ta.getId().startsWith(NB_TASK_REFERENCE)) {
                    references.add(TaskReference.createFrom(ta));
                }
            }
        }
        return references;
    }

    void removeTaskReference (String repositoryId, String taskId) {
        NbTaskDataModel model = getModel();
        TaskData taskData = model == null ? null : model.getLocalTaskData();
        if (taskData != null) {
            TaskAttribute parentTA = taskData.getRoot().getAttribute(IssueField.REFERENCES.getKey());
            if (parentTA != null) {
                for (TaskAttribute ta : parentTA.getAttributes().values()) {
                    if (ta.getId().startsWith(NB_TASK_REFERENCE)) {
                        TaskReference ref = TaskReference.createFrom(ta);
                        if (repositoryId.equals(ref.getRepositoryId()) && taskId.equals(ref.getTaskId())) {
                            parentTA.removeAttribute(ta.getId());
                            break;
                        }
                    }
                }
            }
            model.attributeChanged(parentTA);
        }
    }
    
    void setTaskPrivateNotes (String notes) {
        super.setPrivateNotes(notes);
        getTaskController().modelStateChanged(true);
    }
    
    public void setTaskDueDate (Date date, boolean persistChange) {
        super.setDueDate(date, persistChange);
        if (controller != null) {
            controller.modelStateChanged(hasUnsavedChanges());
            if (persistChange) {
                controller.refreshViewData();
            }
        }
    }
    
    public void setTaskScheduleDate (IssueScheduleInfo date, boolean persistChange) {
        super.setScheduleDate(date, persistChange);
        if (controller != null) {
            controller.modelStateChanged(hasUnsavedChanges());
            if (persistChange) {
                controller.refreshViewData();
            }
        }
    }
    
    public void setTaskEstimate (int estimate, boolean persistChange) {
        super.setEstimate(estimate, persistChange);
        if (controller != null) {
            controller.modelStateChanged(hasUnsavedChanges());
            if (persistChange) {
                controller.refreshViewData();
            }
        }
    }

    public void addComment (String comment, boolean closeAsFixed) {
        if (comment != null && !comment.isEmpty()) {
            String notes = getPrivateNotes();
            notes += "\n\n" + comment;
            setPrivateNotes(notes);
        }
        if (closeAsFixed) {
            finish();
        }
        save();
        getTaskController().modelStateChanged(false);
        getTaskController().refreshViewData();
    }

    public void attachPatch (final File file, final String description) {
        if (file != null) {
            runWithModelLoaded(new Runnable() {

                @Override
                public void run () {
                    NbTaskDataModel model = getModel();
                    TaskData td = model == null ? null : model.getLocalTaskData();
                    if (td != null) {
                        TaskAttribute parentTA = td.getRoot().getAttribute(IssueField.ATTACHMENTS.getKey());
                        if (parentTA == null) {
                            parentTA = td.getRoot().createAttribute(IssueField.ATTACHMENTS.getKey());
                        }
                        addAttachment(model, parentTA, file, description, null, true);
                        save();
                        getTaskController().modelStateChanged(false);
                        getTaskController().refreshViewData();
                    }
                }
            });
        }
    }
    
    final static class TaskReference {

        private final String repositoryId;
        private final String taskId;

        public TaskReference (String repositoryId, String taskId) {
            this.repositoryId = repositoryId;
            this.taskId = taskId;
        }

        private TaskReference (Issue task) {
            this.repositoryId = task.getRepository().getId();
            this.taskId = task.getID();
        }

        public String getTaskId () {
            return taskId;
        }

        public String getRepositoryId () {
            return repositoryId;
        }

        private void applyTo (TaskAttribute attr) {
            attr.createAttribute("task.repository").setValue(repositoryId);
            attr.createAttribute("task.id").setValue(taskId);
        }
        
        private static TaskReference createFrom (TaskAttribute ta) {
            String repositoryId = "";
            String taskId = "";
            TaskAttribute attr = ta.getAttribute("task.repository");
            if (attr != null) {
                repositoryId = attr.getValue();
            }
            attr = ta.getAttribute("task.id");
            if (attr != null) {
                taskId = attr.getValue();
            }
            return new TaskReference(repositoryId, taskId);
        }
    }

    class Attachment extends AttachmentsPanel.AbstractAttachment {

        private final TaskAttribute ta;
        private final String desc;
        private final String filename;
        private final Date date;
        private final String contentType;
        private final boolean isPatch;
        private final String uri;
        private Action deleteAction;

        public Attachment (TaskAttribute ta) {
            TaskAttachmentMapper taskAttachment = TaskAttachmentMapper.createFrom(ta);
            this.ta = ta;
            this.desc = taskAttachment.getDescription();
            this.filename = taskAttachment.getFileName();
            this.date = taskAttachment.getCreationDate();
            this.contentType = taskAttachment.getContentType();
            this.isPatch = taskAttachment.isPatch();
            this.uri = taskAttachment.getUrl();
        }

        @Override
        public String getAuthorName () {
            return "";
        }

        @Override
        public String getAuthor () {
            return "";
        }

        @Override
        public Date getDate () {
            return date;
        }

        @Override
        public String getDesc () {
            return desc;
        }

        @Override
        public String getFilename () {
            return filename;
        }

        @Override
        public String getContentType () {
            return contentType;
        }

        @Override
        public boolean isPatch () {
            return isPatch;
        }

        @Override
        public void getAttachementData (final OutputStream os) {
            try {
                File f = Utilities.toFile(new URI(uri));
                FileUtils.copyStreamsCloseAll(os, FileUtils.createInputStream(f));
            } catch (URISyntaxException | IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public boolean canBeDeleted () {
            return true;
        }

        @Override
        public Action getDeleteAction () {
            if (deleteAction == null) {
                deleteAction = new DeleteAttachmentAction();
            }
            return deleteAction;
        }

        @NbBundle.Messages({
            "CTL_AttachmentAction.Remove=Remove",
            "MSG_AttachmentAction.Remove.confirm.text=Do you want to permanently remove the attachment?",
            "LBL_AttachmentAction.Remove.confirm.title=Remove Attachment"
        })
        private class DeleteAttachmentAction extends AbstractAction {

            public DeleteAttachmentAction() {
                putValue(NAME, Bundle.CTL_AttachmentAction_Remove());
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(),
                        Bundle.MSG_AttachmentAction_Remove_confirm_text(),
                        Bundle.LBL_AttachmentAction_Remove_confirm_title(),
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                    deleteAttachment(ta);
                }
            }
        }

    }

}
