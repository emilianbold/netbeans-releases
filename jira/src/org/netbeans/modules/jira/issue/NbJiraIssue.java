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

package org.netbeans.modules.jira.issue;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.event.MouseInputListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.eclipse.mylyn.internal.jira.core.JiraAttribute;
import org.eclipse.mylyn.internal.jira.core.model.Attachment;
import org.eclipse.mylyn.internal.jira.core.model.Comment;
import org.eclipse.mylyn.internal.jira.core.model.IssueType;
import org.eclipse.mylyn.internal.jira.core.model.JiraStatus;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.netbeans.modules.bugtracking.spi.IssueNode;
import org.netbeans.modules.jira.Jira;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.spi.Query.ColumnDescriptor;
import org.netbeans.modules.bugtracking.util.StackTraceSupport;
import org.netbeans.modules.jira.repository.JiraRepository;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
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
        KEY(JiraAttribute.ISSUE_KEY.id()),
        SUMMARY(JiraAttribute.SUMMARY.id()),
        DESCRIPTION(JiraAttribute.DESCRIPTION.id()),
        STATUS(TaskAttribute.STATUS),
        PRIORITY(JiraAttribute.PRIORITY.id()),
        RESOLUTION(JiraAttribute.RESOLUTION.id()),
        PRODUCT(JiraAttribute.PROJECT.id()),
        COMPONENT(JiraAttribute.COMPONENTS.id()),
//        VERSION(JiraAttribute. .getName()),
//        PLATFORM(JiraAttribute.REP_PLATFORM.getName()),
//        OS(JiraAttribute.OP_SYS.getName()),
//        MILESTONE(JiraAttribute.TARGET_MILESTONE.getName()),
//        REPORTER(JiraAttribute.REPORTER.getName()),
//        REPORTER_NAME(JiraAttribute.REPORTER_NAME.getName()),
//        ASSIGNED_TO(JiraAttribute.ASSIGNED_TO.getName()),
//        ASSIGNED_TO_NAME(JiraAttribute.ASSIGNED_TO_NAME.getName()),
//        QA_CONTACT(JiraAttribute.QA_CONTACT.getName()),
//        QA_CONTACT_NAME(JiraAttribute.QA_CONTACT_NAME.getName()),
//        NEWCC(JiraAttribute.NEWCC.getName()),
//        REMOVECC(JiraAttribute.REMOVECC.getName()),
//        CC(JiraAttribute.CC.getName()),
//        DEPENDS_ON(JiraAttribute.DEPENDSON.getName()),
//        BLOCKS(JiraAttribute.BLOCKED.getName()),
//        URL(JiraAttribute.BUG_FILE_LOC.getName()),
//        KEYWORDS(JiraAttribute.KEYWORDS.getName()),
        TYPE(JiraAttribute.TYPE.id());
//        CREATION(TaskAttribute.DATE_CREATION),
//        MODIFICATION(TaskAttribute.DATE_MODIFICATION),
//        COMMENT_COUNT(TaskAttribute.TYPE_COMMENT, false),
//        ATTACHEMENT_COUNT(TaskAttribute.TYPE_ATTACHMENT, false);

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
    private IssueNode node;
    
    public NbJiraIssue(TaskData data, JiraRepository repo) {
        super(repo);
        this.taskData = data;
        this.repository = repo;
    }

    public void setTaskData(TaskData taskData) {
        assert !taskData.isPartial();
        this.taskData = taskData;
        attributes = null; // reset
        Jira.getInstance().getRequestProcessor().post(new Runnable() {
            public void run() {
                ((JiraIssueNode)getNode()).fireDataChanged();
                fireDataChanged();
            }
        });
    }


    public String getID() {
        if(taskData.isNew()) {
            return null;
        }
        return taskData.getTaskId(); // XXX id or key ???
    }

    String getKey() {
        return getFieldValue(IssueField.KEY);
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

    Comment[] getComments() {
        return null; // XXX
    }

    Attachment[] getAttachments() {
        return null; // XXX
    }

    public void addComment(String comment) {
//        try {
//            Jira.getInstance().getClient(repository.getTaskRepository()).addCommentToIssue(issue, comment, new NullProgressMonitor());
//        } catch (JiraException ex) {
//            Jira.LOG.log(Level.SEVERE, null, ex);
//        }
    }

    void addAttachment(File f) throws JiraException {
        // XXX
//        Jira.getInstance().getClient(repository.getTaskRepository()).addAttachment(issue, "attachment", f.getName(), f, "text/plain", new NullProgressMonitor());
    }

    public boolean refresh() {
//        try {
//            issue = Jira.getInstance().getClient(repository.getTaskRepository()).getIssueByKey(issue.getKey(), new NullProgressMonitor());
//        } catch (JiraException ex) {
//            Jira.LOG.log(Level.SEVERE, null, ex);
//            return false;
//        }
//        if(controller != null) {
//            controller.refreshViewData();
//        }
        return true;
    }

    public void resolve(Resolution resolution, String comment) throws JiraException {
//        issue.setResolution(resolution);
////		issue.setFixVersions(new Version[0]); // XXX set version
//        String resolveOperationId = Jira.getInstance().getResolveOperation(repository.getTaskRepository(), getKey());
//        if(resolveOperationId == null) {
//            Jira.LOG.severe("Can't resolve issue '" + getKey() + "'");
//        }
//        Jira.getInstance().getClient(repository.getTaskRepository()).advanceIssueWorkflow(issue, resolveOperationId, comment, new NullProgressMonitor());
    }

    public void resolveFixed(String val) {
//        try {
//            Resolution[] res = Jira.getInstance().getResolutions(getTaskRepository());
//            Resolution resolution = null;
//            if(res != null) {
//                for (Resolution r : res) {
//                    // XXX HACK
//                    if(r.getName().equals("Fixed")) {
//                        resolution = r;
//                    }
//                }
//            }
//            if(resolution == null) {
//                Jira.LOG.severe("Can't close issue " + getKey() + " as 'Fixed'");
//            }
//            resolve(resolution, val);
//        } catch (JiraException ex) {
//            Jira.LOG.log(Level.SEVERE, null, ex);
//        }
    }

    @Override
    public String getDisplayName() {
        return "Issue: " + getKey();
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

    @Override
    public void addComment(String comment, boolean closeAsFixed) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void attachPatch(File file, String description) {
        throw new UnsupportedOperationException("Not supported yet.");
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<String, String> getAttributes() {
//        if(attributes == null) {
//            attributes = new HashMap<String, String>();
//            String value;
//            for (IssueField field : IssueField.values()) {
//                switch(field) {
//                    case REPORTER_NAME:
//                    case QA_CONTACT_NAME:
//                    case ASSIGNED_TO_NAME:
//                        continue;
//                    default:
//                        value = getFieldValue(field);
//                }
//                if(value != null && !value.trim().equals("")) {                 // NOI18N
//                    attributes.put(field.key, value);
//                }
//            }
//        }
//        return attributes;
        return Collections.emptyMap();
    }   

    public static ColumnDescriptor[] getColumnDescriptors() {
        if(DESCRIPTORS == null) {
            ResourceBundle loc = NbBundle.getBundle(NbJiraIssue.class);
            DESCRIPTORS = new ColumnDescriptor[] {
                new ColumnDescriptor<String>(LABEL_NAME_ID, String.class,
                                                  loc.getString("CTL_Issue_ID_Title"), // NOI18N
                                                  loc.getString("CTL_Issue_ID_Desc")), // NOI18N
                new ColumnDescriptor<String>(LABEL_NAME_TYPE, String.class,
                                                  loc.getString("CTL_Issue_Type_Title"), // NOI18N
                                                  loc.getString("CTL_Issue_Type_Desc")), // NOI18N
                new ColumnDescriptor<String>(LABEL_NAME_PRIORITY, String.class,
                                                  loc.getString("CTL_Issue_Priority_Title"), // NOI18N
                                                  loc.getString("CTL_Issue_Priority_Desc")), // NOI18N
                new ColumnDescriptor<String>(LABEL_NAME_STATUS, String.class,
                                                  loc.getString("CTL_Issue_Status_Title"), // NOI18N
                                                  loc.getString("CTL_Issue_Status_Desc")), // NOI18N
                new ColumnDescriptor<String>(LABEL_NAME_RESOLUTION, String.class,
                                                  loc.getString("CTL_Issue_Resolution_Title"), // NOI18N
                                                  loc.getString("CTL_Issue_Resolution_Desc")), // NOI18N
                new ColumnDescriptor<String>(LABEL_NAME_SUMMARY, String.class,
                                                  loc.getString("CTL_Issue_Summary_Title"), // NOI18N
                                                  loc.getString("CTL_Issue_Summary_Desc")) // NOI18N
            };
        }
        return DESCRIPTORS;
    }

    /**
     * Returns the id from the given taskData or null if taskData.isNew()
     * @param taskData
     * @return id or null
     */
    public static String getID(TaskData taskData) {
        if(taskData.isNew()) {
            return null;
        }
        return taskData.getTaskId();
    }

    // XXX fields logic - 100% bugzilla overlap
    /**
     * Returns the value represented by the given field
     *
     * @param f
     * @return
     */
    String getFieldValue(IssueField f) {
        if(f.isSingleAttribute()) {
            TaskAttribute a = taskData.getRoot().getMappedAttribute(f.key);
            if(a != null && a.getValues().size() > 1) {
                return listValues(a);
            }
            return a != null ? a.getValue() : ""; // NOI18N
        } else {
            List<TaskAttribute> attrs = taskData.getAttributeMapper().getAttributesByType(taskData, f.key);
            // returning 0 would set status MODIFIED instead of NEW
            return "" + ( attrs != null && attrs.size() > 0 ?  attrs.size() : ""); // NOI18N
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

    void setFieldValue(IssueField f, String value) {
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
        if(f.isSingleAttribute()) {
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
        Map<String, String> seenAtributes = repository.getIssueCache().getSeenAttributes(getID());
        if(seenAtributes == null) {
            seenAtributes = new HashMap<String, String>();
        }
        return seenAtributes;
    }

    private class Controller extends BugtrackingController implements ActionListener, MouseMotionListener, MouseInputListener {
        private IssuePanel panel = new IssuePanel();
        private ResolvePanel resolvePanel = new ResolvePanel();
        private StyledDocument doc;
        private DefaultListModel attachmentsModel;

        private final static String HL_ATTRIBUTE = "linkact";

        public Controller() {
            doc = panel.commenstTextPane.getStyledDocument();

            resolvePanel.resolutionCBO.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    if(value != null) {
                        Resolution r = (Resolution) value;
                        value = r.getName();
                    }
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            });

            attachmentsModel = new DefaultListModel();
            panel.attachmentsList.setModel(attachmentsModel);
            panel.attachmentsList.setCellRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    if(value != null) {
                        Attachment a = (Attachment) value;
                        value = a.getName() + " : " + a.getAuthor() + " : " + dateFormat.format(a.getCreated());
                    }
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            });

            panel.addCommentButton.addActionListener(this);
            panel.resolveButton.addActionListener(this);
            panel.attachFileButton.addActionListener(this);
            panel.commenstTextPane.addMouseMotionListener(this);
            panel.commenstTextPane.addMouseListener(this);

            refreshViewData();
        }

        @Override
        public JComponent getComponent() {
            return panel;
        }

        @Override
        public boolean isValid() {
            return !panel.summaryField.getText().trim().equals("") &&
                   !panel.priorityField.getText().trim().equals("") &&
                   !panel.summaryField.getText().trim().equals("") &&
                   !panel.descTextArea.getText().trim().equals("") &&
                   !panel.typeField.getText().trim().equals("");
        }

        @Override
        public void applyChanges() {

        }

        public void mousePressed(MouseEvent e) { }
        public void mouseReleased(MouseEvent e) { }
        public void mouseEntered(MouseEvent e) { }
        public void mouseExited(MouseEvent e) { }
        public void mouseDragged(MouseEvent e) { }

        public void mouseClicked(MouseEvent e) {
            try{
                Element elem = doc.getCharacterElement(panel.commenstTextPane.viewToModel(e.getPoint()));
                AttributeSet as = elem.getAttributes();
                StackTraceAction a = (StackTraceAction) as.getAttribute(HL_ATTRIBUTE);
                if(a != null) {
                    a.openStackTrace(elem.getDocument().getText(elem.getStartOffset(), elem.getEndOffset() - elem.getStartOffset()));
                }
            } catch(Exception ex) {
                Jira.LOG.log(Level.SEVERE, null, ex);
            }
        }

        public void mouseMoved(MouseEvent e) {
            Element elem = doc.getCharacterElement( panel.commenstTextPane.viewToModel(e.getPoint()));
			AttributeSet as = elem.getAttributes();
			if(StyleConstants.isUnderline(as)) {
				panel.commenstTextPane.setCursor(new Cursor(Cursor.HAND_CURSOR));
            } else {
				panel.commenstTextPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        }

        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == panel.addCommentButton) {
                onAddComment();
            } else if(e.getSource() == panel.resolveButton) {
                onResolve();
            } else if(e.getSource() == panel.attachFileButton) {
                onAttach();
            }
        }

        private void onAddComment() {
            throw new UnsupportedOperationException();
        }

        private void onAttach() {
            throw new UnsupportedOperationException();
        }

        private void onResolve() {
            throw new UnsupportedOperationException();
        }

        private void refreshViewData() {
            panel.idTextField.setText(getKey());
            panel.priorityField.setText(getFieldValue(IssueField.PRIORITY));
            panel.summaryField.setText(getFieldValue(IssueField.SUMMARY));
            panel.statusField.setText(getFieldValue(IssueField.STATUS));
            panel.typeField.setText(getFieldValue(IssueField.TYPE));
            panel.descTextArea.setText(getDescription());
//            Comment[] comments = issue.getComments();
//            StringBuffer sb = new StringBuffer();
//            for (Comment comment : comments) {
//                sb.append(comment.getAuthor());
//                sb.append(" : ");
//                sb.append(comment.getCreated());
//                sb.append("\n");
//                sb.append(comment.getComment());
//                sb.append("\n\n");
//            }
//            panel.commenstTextPane.setText(sb.toString());
//            Attachment[] attachements = issue.getAttachments();
//            attachmentsModel.clear();
//            for (Attachment attachment : attachements) {
//                attachmentsModel.addElement(attachment);
//            }
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(org.netbeans.modules.jira.issue.NbJiraIssue.class);
        }

        private class StackTraceAction {
            void openStackTrace(String text) {
                StackTraceSupport.findAndOpen(text);
            }
        }

    }
}
