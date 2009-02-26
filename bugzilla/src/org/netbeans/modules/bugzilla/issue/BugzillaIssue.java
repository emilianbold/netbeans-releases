/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.bugzilla.issue;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import org.apache.commons.httpclient.HttpException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaAttribute;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaOperation;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaRepositoryConnector;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaTaskAttachmentHandler;
import org.eclipse.mylyn.internal.tasks.core.data.FileTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugtracking.spi.IssueNode;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.spi.Query.ColumnDescriptor;
import org.netbeans.modules.bugzilla.BugzillaRepository;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class BugzillaIssue extends Issue {
    private TaskData data;
    private BugzillaRepository repository;
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";
    private static final SimpleDateFormat CC_DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT);
    private IssueController controller;
    private IssueNode node;
    
    static final String LABEL_NAME_ID           = "bugzilla.issue.id";
    static final String LABEL_NAME_SEVERITY     = "bugzilla.issue.severity";
    static final String LABEL_NAME_PRIORITY     = "bugzilla.issue.priority";
    static final String LABEL_NAME_STATUS       = "bugzilla.issue.status";
    static final String LABEL_NAME_RESOLUTION   = "bugzilla.issue.resolution";
    static final String LABEL_NAME_SUMMARY      = "bugzilla.issue.summary";

    enum IssueField {
        SUMMARY(TaskAttribute.SUMMARY),
        STATUS(TaskAttribute.STATUS),
        PRIORITY(TaskAttribute.PRIORITY),
        RESOLUTION(TaskAttribute.RESOLUTION),
        PRODUCT(BugzillaAttribute.PRODUCT.getKey()),
        COMPONENT(BugzillaAttribute.COMPONENT.getKey()),
        VERSION(BugzillaAttribute.VERSION.getKey()),
        PLATFORM(BugzillaAttribute.REP_PLATFORM.getKey()),
        MILESTONE(BugzillaAttribute.TARGET_MILESTONE.getKey()),
        REPORTER(BugzillaAttribute.REPORTER.getKey()),
        ASSIGEND_TO(BugzillaAttribute.ASSIGNED_TO.getKey()),
        ASSIGNED_TO_NAME(BugzillaAttribute.ASSIGNED_TO_NAME.getKey()),
        QA_CONTACT(BugzillaAttribute.QA_CONTACT.getKey()),
        QA_CONTACT_NAME(BugzillaAttribute.QA_CONTACT_NAME.getKey()),
        NEWCC(BugzillaAttribute.NEWCC.getKey()),        
        REMOVECC(BugzillaAttribute.REMOVECC.getKey()),
        CC(BugzillaAttribute.CC.getKey()),
        DEPENDS_ON(BugzillaAttribute.DEPENDSON.getKey()),
        BLOCKS(BugzillaAttribute.BLOCKED.getKey()),
        URL(BugzillaAttribute.BUG_FILE_LOC.getKey()),
        KEYWORDS(BugzillaAttribute.KEYWORDS.getKey()),
        SEVERITY(BugzillaAttribute.BUG_SEVERITY.getKey()),
        DESCRIPTION(TaskAttribute.DESCRIPTION),
        CREATION(TaskAttribute.DATE_CREATION),
        MODIFICATION(TaskAttribute.DATE_MODIFICATION);

        private final String key;

        public static int STATUS_UPTODATE = 1;
        public static int STATUS_NEW = 2;
        public static int STATUS_MODIFIED = 4;

        IssueField(String key) {
            this.key = key;
        }
    }

    private Map<String, String> attributes;

    /**
     * Defines columns for a view table.
     */
    public static ColumnDescriptor[] DESCRIPTORS;

    public BugzillaIssue(TaskData data, BugzillaRepository repo) {
        this.data = data;
        this.repository = repo;
    }

    public static ColumnDescriptor[] getColumnDescriptors() {
        if(DESCRIPTORS == null) {
            ResourceBundle loc = NbBundle.getBundle(BugzillaIssue.class);
            DESCRIPTORS = new ColumnDescriptor[] {
                new ColumnDescriptor(LABEL_NAME_ID, String.class,
                                                  loc.getString("CTL_Issue_ID_Title"),
                                                  loc.getString("CTL_Issue_ID_Desc")),
                new ColumnDescriptor(LABEL_NAME_SEVERITY, String.class,
                                                  loc.getString("CTL_Issue_Severity_Title"),
                                                  loc.getString("CTL_Issue_Severity_Desc")),
                new ColumnDescriptor(LABEL_NAME_PRIORITY, String.class,
                                                  loc.getString("CTL_Issue_Priority_Title"),
                                                  loc.getString("CTL_Issue_Priority_Desc")),
                new ColumnDescriptor(LABEL_NAME_STATUS, String.class,
                                                  loc.getString("CTL_Issue_Status_Title"),
                                                  loc.getString("CTL_Issue_Status_Desc")),
                new ColumnDescriptor(LABEL_NAME_RESOLUTION, String.class,
                                                  loc.getString("CTL_Issue_Resolution_Title"),
                                                  loc.getString("CTL_Issue_Resolution_Desc")),
                new ColumnDescriptor(LABEL_NAME_SUMMARY, String.class,
                                                  loc.getString("CTL_Issue_Summary_Title"),
                                                  loc.getString("CTL_Issue_Summary_Desc"))
            };
        }
        return DESCRIPTORS;
    }

    @Override
    public BugtrackingController getControler() {
        if (controller == null) {
            controller = new IssueController(this);
        }
        return controller;
    }

    @Override
    public void setSeen(boolean seen) {
        setSeen(seen, true);
    }

    @Override
    public String toString() {
        String str = getID() + " : "  + getSummary();
        return str;
    }

    @Override
    public IssueNode getNode() {
        if(node == null) {
            node = createNode();
        }
        return node;
    }

    @Override
    public Map<String, String> getAttributes() {
        if(attributes == null) {
            attributes = new HashMap<String, String>();
            attributes.put("id", getID());
            for (IssueField field : IssueField.values()) {
                attributes.put(field.key, getFieldValue(field));
            }
        }
        attributes.put("seen", wasSeen() ? "1" : "0"); // XXX
        return attributes;
    }

    @Override
    public String getRecentChanges() {
        if(wasSeen()) {
            return "";
        }
        int status = repository.getIssuesCache().getStatus(getID());
        if(status == Query.ISSUE_STATUS_NEW) {
            return "New";
        } else if(status == Query.ISSUE_STATUS_MODIFIED) {
            List<IssueField> changedFields = new ArrayList<IssueField>();
            Map<String, String> seenAtributes = repository.getIssuesCache().getSeenAttributes(getID());
            assert seenAtributes != null;
            for (IssueField f : IssueField.values()) {
                String value = getFieldValue(f);
                String seenValue = seenAtributes.get(f.key);
                if(seenValue == null) {
                    seenValue = "";
                }
                if(!value.trim().equals(seenValue)) {
                    changedFields.add(f);
                }
            }
            int changedCount = changedFields.size();
            assert changedCount > 0 : "status MODIFIED yet zero changes found";
            if(changedCount == 1) {
                String ret = null;
                for (IssueField changedField : changedFields) {
                    switch(changedField) {
                        case SUMMARY :
                            ret = "Summary changed";
                        case CC :
                            ret = "CC field changed";
                        case KEYWORDS :
                            ret ="Keywords changed";
                        case DEPENDS_ON :
                        case BLOCKS :
                            ret ="Dependence changed";
                        default :
                            ret = changedField.name() + " changed to " + getFieldValue(changedField);
                    }
                }
                return ret;
            } else {
                String ret = null;
                for (IssueField changedField : changedFields) {
                    switch(changedField) {
                        case SUMMARY :
                            ret = changedCount + " changes, incl. summary";
                        case PRIORITY :
                            ret = changedCount + " changes, incl. priority";
                        case SEVERITY :
                            ret = changedCount + " changes, incl. severity";
                        case PRODUCT :
                            ret = changedCount + " changes, incl. product";
                        case COMPONENT :
                            ret = changedCount + " changes, incl. component";
                        case PLATFORM :
                            ret = changedCount + " changes, incl. platform";
                        case VERSION :
                            ret = changedCount + " changes, incl. version";
                        case MILESTONE :
                            ret = changedCount + " changes, incl. milestone";
                        case KEYWORDS :
                            ret = changedCount + " changes, incl. keywords";
                        case URL :
                            ret = changedCount + " changes, incl. url";
                        case ASSIGEND_TO :
                            ret = changedCount + " changes, incl. Assignee";
                        case QA_CONTACT :
                            ret = changedCount + " changes, incl. qa contact";
                        case DEPENDS_ON :
                        case BLOCKS :
                            ret = changedCount + " changes, incl. dependence";
                        default :
                            ret = changedCount + " changes";
                    }
                    return ret;
                }
            }            
        }
        return "";
    }

    public static String getID(TaskData taskData) {
        try {
            return Integer.toString(BugzillaRepositoryConnector.getBugId(taskData.getTaskId()));
        } catch (CoreException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
        }
        return "";
    }

    TaskRepository getTaskRepository() {
        return repository.getTaskRepository();
    }

    BugzillaRepository getRepository() {
        return repository;
    }

    public String getID() {
        return getID(data);
    }

    public String getSummary() {
        return getFieldValue(IssueField.SUMMARY);
    }

    public void setTaskData(TaskData taskData) {
        data = taskData;
        attributes = null; // reset
        ((BugzillaIssueNode)getNode()).fireDataChanged();
        fireDataChanged();
    }

    TaskData getTaskData() {
        return data;
    }

    public void setSeen(boolean seen, boolean cacheRefresh) {
        super.setSeen(seen);
        if(cacheRefresh) {
            repository.getIssuesCache().setSeen(getID(), seen, getAttributes());
        }
    }

    String getFieldValue(IssueField f) {
        TaskAttribute a = data.getRoot().getMappedAttribute(f.key);
        return a != null ? a.getValue() : "";
    }

    void setFieldValue(IssueField f, String value) {
        TaskAttribute a = data.getRoot().getMappedAttribute(f.key);
        if(a == null) {
            a = new TaskAttribute(data.getRoot(), f.key);
        }
        a.setValue(value);
    }


    List<String> getFieldValues(IssueField f) {
        TaskAttribute a = data.getRoot().getMappedAttribute(f.key);
        return a != null ? a.getValues() : Collections.EMPTY_LIST;
    }

    void setFieldValues(IssueField f, List<String> ccs) {
        TaskAttribute a = data.getRoot().getMappedAttribute(f.key);
        if(a == null) {
            a = new TaskAttribute(data.getRoot(), f.key);
        }
        a.setValues(ccs);
    }

    int getFieldStatus(IssueField f) {
        Map<String, String> a = getAttributes();
        String seenValue = a.get(f.key);
        if(seenValue == null) {
            return IssueField.STATUS_NEW;
        } else if (!seenValue.equals(getFieldValue(f))) {
            return IssueField.STATUS_MODIFIED;
        }
        return IssueField.STATUS_UPTODATE;
    }


    // XXX get rid of this
    Set<TaskAttribute> getResolveAttributes(String resolution) {
        Set<TaskAttribute> attrs = new HashSet<TaskAttribute>();
        TaskAttribute rta = data.getRoot();
        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.OPERATION);
        ta.setValue(BugzillaOperation.resolve.getInputId());
        attrs.add(ta);
        ta = rta.getMappedAttribute(TaskAttribute.RESOLUTION);
        ta.setValue(resolution);
        attrs.add(ta);
        return attrs;
    }

    private IssueNode createNode() {
        return new BugzillaIssueNode(this);
    }

    void resolve(String resolution) {
        setOperation(BugzillaOperation.resolve);
        TaskAttribute rta = data.getRoot();
        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.RESOLUTION);
        ta.setValue(resolution);        
    }

    void duplicate(String id) {
        setOperation(BugzillaOperation.duplicate);
        TaskAttribute rta = data.getRoot();
        TaskAttribute ta = rta.getMappedAttribute(BugzillaOperation.duplicate.getInputId());
        ta.setValue(id);
    }

    void reassigne(String user) {
        setOperation(BugzillaOperation.reassign);
        TaskAttribute rta = data.getRoot();
        TaskAttribute ta = rta.getMappedAttribute(BugzillaOperation.reassign.getInputId());
        ta.setValue(user);
    }

    void verify() {
        setOperation(BugzillaOperation.verify);
    }

    void close() {
        setOperation(BugzillaOperation.close);
    }

    void reopen() {
        setOperation(BugzillaOperation.reopen);
    }

    private void setOperation(BugzillaOperation operation) {
        TaskAttribute rta = data.getRoot();
        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.OPERATION);
        ta.setValue(operation.name());
    }

    Attachment[] getAttachments() {
        List<TaskAttribute> attrs = data.getAttributeMapper().getAttributesByType(data, TaskAttribute.TYPE_ATTACHMENT);
        if (attrs == null) {
            return new Attachment[0];
        }
        List<Attachment> attachments = new ArrayList<Attachment>(attrs.size());
        for (TaskAttribute taskAttribute : attrs) {
            attachments.add(new Attachment(taskAttribute));
        }
        return attachments.toArray(new Attachment[attachments.size()]);
    }


    void addAttachment(File f, String comment, String desc, String contentType) throws HttpException, IOException, CoreException  {
        FileTaskAttachmentSource attachmentSource = new FileTaskAttachmentSource(f);
        attachmentSource.setContentType(contentType);
        BugzillaTaskAttachmentHandler.AttachmentPartSource source = new BugzillaTaskAttachmentHandler.AttachmentPartSource(attachmentSource);

//        try {
            Bugzilla.getInstance().getClient(repository).postAttachment(
                    getID(), 
                    comment, 
                    desc, 
                    attachmentSource.getContentType(), 
                    false, 
                    source, 
                    new NullProgressMonitor());
//        } catch (HttpException ex) {
//            Bugzilla.LOG.log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Bugzilla.LOG.log(Level.SEVERE, null, ex);
//        } catch (CoreException ex) {
//            Bugzilla.LOG.log(Level.SEVERE, null, ex);
//        }
    }

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

    // XXX carefull - implicit refresh
    public void addComment(String comment, boolean close) {
        if(comment == null && !close) {
            return;
        }
        refresh();

        // resolved attrs
        if(close) {
            resolve("FIXED"); // XXX constant?
        }
        if(comment != null) {
            addComment(comment);
        }
        try {
            submit();
        } catch (CoreException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
        }
    }

    public void addComment(String comment) {
        if(comment != null) {
            TaskAttribute ta = data.getRoot().createMappedAttribute(TaskAttribute.COMMENT_NEW);
            ta.setValue(comment);
        }
    }

    void submit() throws CoreException {
        RepositoryResponse rr = Bugzilla.getInstance().getRepositoryConnector().getTaskDataHandler().postTaskData(getTaskRepository(), data, null, new NullProgressMonitor());
    }

    public void refresh() {
        try {
            TaskData td = Bugzilla.getInstance().getRepositoryConnector().getTaskData(repository.getTaskRepository(), data.getTaskId(), new NullProgressMonitor());
            setTaskData(data);
        } catch (CoreException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
        }
        if(controller != null) {
            controller.refreshViewData();
        }
    }
    
    class Comment {
        private final Date when;
        private final String who;
        private final Long number;
        private final String text;

        public Comment(TaskAttribute a) {            
            Date d = null;
            try {
                d = CC_DATE_FORMAT.parse(a.getMappedAttribute(TaskAttribute.COMMENT_DATE).getValue());
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
            when = d;            
            // XXX check for NULL
            who = a.getMappedAttribute(TaskAttribute.COMMENT_AUTHOR).getMappedAttribute(TaskAttribute.PERSON_NAME).getValue();
            number = Long.parseLong(a.getMappedAttribute(TaskAttribute.COMMENT_NUMBER).getValues().get(0));// XXX value or values?
            text = a.getMappedAttribute(TaskAttribute.COMMENT_TEXT).getValue();
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
    }

    class Attachment {
        private final String desc;
        private final String filename;
        private final String author;
        private final Date date;

        public Attachment(TaskAttribute ta) {
            Date d = null;
            try {
                d = CC_DATE_FORMAT.parse(ta.getMappedAttribute(TaskAttribute.ATTACHMENT_DATE).getValues().get(0));// XXX value or values?
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
            date = d;
            filename = ta.getMappedAttribute(TaskAttribute.ATTACHMENT_FILENAME).getValue();
            desc = ta.getMappedAttribute(TaskAttribute.ATTACHMENT_DESCRIPTION).getValues().get(0);// XXX value or values?
            author = ta.getMappedAttribute(TaskAttribute.ATTACHMENT_AUTHOR).getMappedAttribute(TaskAttribute.PERSON_NAME).getValue();
        }

        public String getAuthor() {
            return author;
        }

        public Date getDate() {
            return date;
        }

        public String getDesc() {
            return desc;
        }

        public String getFilename() {
            return filename;
        }
    }

}
