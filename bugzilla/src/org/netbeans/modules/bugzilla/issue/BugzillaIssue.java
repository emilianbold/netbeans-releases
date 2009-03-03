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
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
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
import org.netbeans.modules.bugtracking.spi.Query.ColumnDescriptor;
import org.netbeans.modules.bugzilla.BugzillaRepository;
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
        ASSIGNED_TO(BugzillaAttribute.ASSIGNED_TO.getKey()),
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
        MODIFICATION(TaskAttribute.DATE_MODIFICATION),
        COMMENT_COUNT(TaskAttribute.TYPE_COMMENT, false),
        ATTACHEMENT_COUNT(TaskAttribute.TYPE_ATTACHMENT, false);

        private final String key;
        private boolean singleAttribute;

        IssueField(String key) {
            this(key, true);
        }
        IssueField(String key, boolean singleAttribute) {
            this.key = key;
            this.singleAttribute = singleAttribute;
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
    }

    private Map<String, String> attributes;

    /**
     * Defines columns for a view table.
     */
    public static ColumnDescriptor[] DESCRIPTORS;

    public BugzillaIssue(TaskData data, BugzillaRepository repo) {
        super(repo);
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
                String value = getFieldValue(field);
                if(value != null && !value.trim().equals("")) {
                    attributes.put(field.key, value);
                }
            }
        }
        return attributes;
    }

    @Override
    public void setSeen(boolean seen) throws IOException {
        super.setSeen(seen);
    }

    @Override
    public String getRecentChanges() {
        if(wasSeen()) {
            return "";
        }
        int status = repository.getIssueCache().getStatus(getID());
        if(status == Issue.ISSUE_STATUS_NEW) {
            return "New";
        } else if(status == Issue.ISSUE_STATUS_MODIFIED) {
            List<IssueField> changedFields = new ArrayList<IssueField>();
            Map<String, String> seenAtributes = getSeenAttributes();
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
                            break;
                        case CC :
                            ret = "CC field changed";
                            break;
                        case KEYWORDS :
                            ret ="Keywords changed";
                            break;
                        case DEPENDS_ON :
                        case BLOCKS :
                            ret ="Dependence changed";
                            break;
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
                            ret = changedCount + " changes, inclusive summary";
                            break;
                        case PRIORITY :
                            ret = changedCount + " changes, inclusive priority";
                            break;
                        case SEVERITY :
                            ret = changedCount + " changes, inclusive severity";
                            break;
                        case PRODUCT :
                            ret = changedCount + " changes, inclusive product";
                            break;
                        case COMPONENT :
                            ret = changedCount + " changes, inclusive component";
                            break;
                        case PLATFORM :
                            ret = changedCount + " changes, inclusive platform";
                            break;
                        case VERSION :
                            ret = changedCount + " changes, inclusive version";
                            break;
                        case MILESTONE :
                            ret = changedCount + " changes, inclusive milestone";
                            break;
                        case KEYWORDS :
                            ret = changedCount + " changes, inclusive keywords";
                            break;
                        case URL :
                            ret = changedCount + " changes, inclusive url";
                            break;
                        case ASSIGNED_TO :
                            ret = changedCount + " changes, inclusive Assignee";
                            break;
                        case QA_CONTACT :
                            ret = changedCount + " changes, inclusive qa contact";
                            break;
                        case DEPENDS_ON :
                        case BLOCKS :
                            ret = changedCount + " changes, inclusive dependence";
                            break;
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

    /**
     * Returns the value represented by the given field
     *
     * @param f
     * @return
     */
    String getFieldValue(IssueField f) {
        if(f.isSingleAttribute()) {
            TaskAttribute a = data.getRoot().getMappedAttribute(f.key);
            if(a != null && a.getValues().size() > 1) {
                return listValues(a);
            }
            return a != null ? a.getValue() : "";
        } else {
            List<TaskAttribute> attrs = data.getAttributeMapper().getAttributesByType(data, f.key);
            return "" + ( attrs != null && attrs.size() > 0 ?  attrs.size() : ""); // returning 0 would set status MODIFIED instead of NEW
        }
    }

    /**
     * Returns a comma separated list created
     * from the values returned by TaskAttribute.getValues()
     * 
     * @param a
     * @return
     */
    private String listValues(TaskAttribute a) {
        if(a == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        List<String> l = a.getValues();
        for (int i = 0; i < l.size(); i++) {
            String s = l.get(i);
            sb.append(s);
            if(i < l.size() -1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    void setFieldValue(IssueField f, String value) {
        if(f.isReadOnly()) {
            assert false : "can't set value into IssueField " + f.name();
            return;
        }
        TaskAttribute a = data.getRoot().getMappedAttribute(f.key);
        if(a == null) {
            a = new TaskAttribute(data.getRoot(), f.key);
        }
        a.setValue(value);
    }

    List<String> getFieldValues(IssueField f) {
        if(f.isSingleAttribute()) {
            TaskAttribute a = data.getRoot().getMappedAttribute(f.key);
            return a != null ? a.getValues() : Collections.EMPTY_LIST;
        } else {
            List<String> ret = new ArrayList<String>();
            ret.add(getFieldValue(f));
            return ret;
        }
    }

    void setFieldValues(IssueField f, List<String> ccs) {
        TaskAttribute a = data.getRoot().getMappedAttribute(f.key);
        if(a == null) {
            a = new TaskAttribute(data.getRoot(), f.key);
        }
        a.setValues(ccs);
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
        if(!wasSeen()) {
            return FIELD_STATUS_IRELEVANT;
        }
        Map<String, String> a = getSeenAttributes();
        String seenValue = a != null ? a.get(f.key) : null;
        if(seenValue == null) {
            seenValue = "";
        }
        if(seenValue.equals("") && !seenValue.equals(getFieldValue(f))) {
            return FIELD_STATUS_NEW;
        } else if (!seenValue.equals(getFieldValue(f))) {
            return FIELD_STATUS_MODIFIED;
        }
        return FIELD_STATUS_UPTODATE;
    }

    private IssueNode createNode() {
        return new BugzillaIssueNode(this);
    }

    void resolve(String resolution) {
        setOperation(BugzillaOperation.resolve);
        TaskAttribute rta = data.getRoot();
        TaskAttribute ta = rta.getMappedAttribute(BugzillaOperation.resolve.getInputId());
        ta.setValue(resolution);
    }

    void accept() {
        setOperation(BugzillaOperation.accept);
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
        assert !SwingUtilities.isEventDispatchThread() : "Accesing remote host. Do not call in awt";
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
        assert !SwingUtilities.isEventDispatchThread() : "Accesing remote host. Do not call in awt";
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
        assert !SwingUtilities.isEventDispatchThread() : "Accesing remote host. Do not call in awt";
        RepositoryResponse rr = Bugzilla.getInstance().getRepositoryConnector().getTaskDataHandler().postTaskData(getTaskRepository(), data, null, new NullProgressMonitor());
        // XXX evaluate rr
    }

    public void refresh() {
        assert !SwingUtilities.isEventDispatchThread() : "Accesing remote host. Do not call in awt";
        try {
            TaskData td = Bugzilla.getInstance().getRepositoryConnector().getTaskData(repository.getTaskRepository(), data.getTaskId(), new NullProgressMonitor());
            getRepository().getIssueCache().setIssueData(getID(), td); // XXX
        } catch (IOException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
        } catch (CoreException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
        }
        if(controller != null) {
            controller.refreshViewData();
        }
    }

    private Map<String, String> getSeenAttributes() {
        Map<String, String> seenAtributes = repository.getIssueCache().getSeenAttributes(getID());
        if(seenAtributes == null) {
            seenAtributes = new HashMap<String, String>();
        }
        return seenAtributes;
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
                Bugzilla.LOG.log(Level.SEVERE, null, ex);
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
        private final String id;
        private String contentType;
        private String isDeprected;
        private String size;
        private String isPatch;
        private String url;


        public Attachment(TaskAttribute ta) {
            id = ta.getValue();
            Date d = null;
            try {
                d = CC_DATE_FORMAT.parse(ta.getMappedAttribute(TaskAttribute.ATTACHMENT_DATE).getValues().get(0));// XXX value or values?
            } catch (ParseException ex) {
                Bugzilla.LOG.log(Level.SEVERE, null, ex);
            }
            date = d;
            filename = ta.getMappedAttribute(TaskAttribute.ATTACHMENT_FILENAME).getValue();
            desc = ta.getMappedAttribute(TaskAttribute.ATTACHMENT_DESCRIPTION).getValues().get(0);// XXX value or values?
            author = ta.getMappedAttribute(TaskAttribute.ATTACHMENT_AUTHOR).getMappedAttribute(TaskAttribute.PERSON_NAME).getValue();
            contentType = ta.getMappedAttribute(TaskAttribute.ATTACHMENT_CONTENT_TYPE).getValue();
            isDeprected = ta.getMappedAttribute(TaskAttribute.ATTACHMENT_IS_DEPRECATED).getValue();
            isPatch = ta.getMappedAttribute(TaskAttribute.ATTACHMENT_IS_PATCH).getValue();
            size = ta.getMappedAttribute(TaskAttribute.ATTACHMENT_SIZE).getValue();
            url = ta.getMappedAttribute(TaskAttribute.ATTACHMENT_URL).getValue();
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

        public String getContentType() {
            return contentType;
        }

        public String getId() {
            return id;
        }

        public String getIsDeprected() {
            return isDeprected;
        }

        public String getIsPatch() {
            return isPatch;
        }

        public String getSize() {
            return size;
        }

        public String getUrl() {
            return url;
        }

        public void getAttachementData(OutputStream os) throws MalformedURLException, IOException, CoreException {
            assert !SwingUtilities.isEventDispatchThread() : "Accesing remote host. Do not call in awt";
            Bugzilla.getInstance().getClient(repository).getAttachmentData(id, os, new NullProgressMonitor());
        }
    }

}
