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

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.event.MouseInputListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import org.apache.commons.httpclient.HttpException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaAttribute;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaClient;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaRepositoryConnector;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaTaskAttachmentHandler;
import org.eclipse.mylyn.internal.tasks.core.data.FileTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
import org.netbeans.modules.bugtracking.util.StackTraceSupport;
import org.netbeans.modules.bugtracking.util.StackTraceSupport.StackTracePosition;
import org.netbeans.modules.bugtracking.spi.IssueNode;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query.ColumnDescriptor;
import org.openide.nodes.Node.Property;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class BugzillaIssue extends Issue {
    private TaskData data;
    private TaskRepository repo;
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";
    private static final SimpleDateFormat CC_DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT);
    private Controller controller;
    private IssueNode node;

    private static String LABEL_NAME_ID         = "bugzilla.issue.id";
    private static String LABEL_NAME_SEVERITY   = "bugzilla.issue.severity";
    private static String LABEL_NAME_PRIORITY   = "bugzilla.issue.priority";
    private static String LABEL_NAME_STATUS     = "bugzilla.issue.status";
    private static String LABEL_NAME_RESOLUTION = "bugzilla.issue.resolution";
    private static String LABEL_NAME_SUMMARY    = "bugzilla.issue.summary";

    /**
     * Defines column labels for a view table.
     */
    public static ColumnDescriptor[] DESCRIPTORS;

    public BugzillaIssue(TaskData data, TaskRepository repo) {
        this.data = data;
        this.repo = repo;
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
            controller = new Controller();
        }
        return controller;
    }

    public void save() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void update() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String toString() {
        String str = getID() + " : "  + getSeverity() + " : " + getStatus() + " : " + getPriority() + " : " + getSummary();
        return str;
    }

    void addAttachment(File f, String comment, String desc) {
        FileTaskAttachmentSource attachmentSource = new FileTaskAttachmentSource(f);
        attachmentSource.setContentType("text/plain");
        BugzillaTaskAttachmentHandler.AttachmentPartSource source = new BugzillaTaskAttachmentHandler.AttachmentPartSource(attachmentSource);

        try {
            Bugzilla.getInstance().getRepositoryConnector().getClientManager().getClient(getRepository(), new NullProgressMonitor()).
                postAttachment(getID(), comment, desc, attachmentSource.getContentType(), false, source, new NullProgressMonitor());
        } catch (HttpException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
        } catch (CoreException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
        }
    }

    public String getID() {
        try {
            return Integer.toString(BugzillaRepositoryConnector.getBugId(data.getTaskId()));
        } catch (CoreException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
        }
        return "";
    }
    
    String getDescription() {
        return getMappedValue(TaskAttribute.DESCRIPTION); // XXX WTF???!!!
    }

    TaskRepository getRepository() {
        return repo;
    }
     
    String getStatus() {
        return getMappedValue(TaskAttribute.STATUS);
    }

    String getPriority() {
        return getMappedValue(TaskAttribute.PRIORITY);
    }

    public String getSummary() {
        return getMappedValue(TaskAttribute.SUMMARY);
    }

    String getResolution() {
        return getMappedValue(TaskAttribute.RESOLUTION);
    }

    String getSeverity() {
        return getValue(BugzillaAttribute.BUG_SEVERITY.getKey());
    }

    private Set<TaskAttribute> getResolveAttributes(String resolution) {
        Set<TaskAttribute> attrs = new HashSet<TaskAttribute>();
        TaskAttribute rta = data.getRoot();
        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.OPERATION);
        ta.setValue("resolve");
        attrs.add(ta);
        ta = rta.getMappedAttribute(TaskAttribute.RESOLUTION);
        ta.setValue(resolution);
        attrs.add(ta);
        return attrs;
    }

    private IssueNode createNode() {
        return new IssueNode(this) {
            @Override
            protected Property<?>[] getProperties() {
                return new Property<?>[] {
                    new IDProperty(),
                    new SeverityProperty(),
                    new PriorityProperty(),
                    new StatusProperty(),
                    new ResolutionProperty(),
                    new SummaryProperty(),
                    new SeenProperty()
                };
            };
        };
    }

    private String getMappedValue(String key) {
        TaskAttribute a = data.getRoot().getMappedAttribute(key);
        return a != null ? a.getValue() : "";
    }

    private String getValue(String key) {
        TaskAttribute a = data.getRoot().getMappedAttribute(key);
        return a != null ? a.getValue() : "";
    }

    Comment[] getComments() {
        List<TaskAttribute> attributes = data.getAttributeMapper().getAttributesByType(data, TaskAttribute.TYPE_COMMENT);
        if (attributes == null) {
            return new Comment[0];
        }
        List<Comment> comments = new ArrayList<Comment>();
        for (TaskAttribute taskAttribute : attributes) {
            comments.add(new Comment(taskAttribute));
        }
        return comments.toArray(new Comment[comments.size()]);
    }

    Attachment[] getAttachments() {
        List<TaskAttribute> attributes = data.getAttributeMapper().getAttributesByType(data, TaskAttribute.TYPE_ATTACHMENT);
        if (attributes == null) {
            return new Attachment[0];
        }
        List<Attachment> attachments = new ArrayList<Attachment>(attributes.size());
        for (TaskAttribute taskAttribute : attributes) {
            attachments.add(new Attachment(taskAttribute));
        }
        return attachments.toArray(new Attachment[attachments.size()]);
    }

    // XXX carefull - implicit refresh
    public void addComment(String comment, boolean close) {
        if(comment == null && !close) {
            return;
        }
        refresh();

        // resolved attrs
        Set<TaskAttribute> attrs = null;
        if(close) {
            attrs = getResolveAttributes("FIXED");
        }
        // commet attrs
        if(comment != null) {
            TaskAttribute ta = data.getRoot().createMappedAttribute(TaskAttribute.COMMENT_NEW);
            ta.setValue(comment);
            attrs.add(ta);
        }
        try {
            // done
            RepositoryResponse rr = Bugzilla.getInstance().getRepositoryConnector().getTaskDataHandler().postTaskData(getRepository(), data, attrs, new NullProgressMonitor());
        } catch (CoreException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
        }
    }

    public void refresh() {
        try {
            data = Bugzilla.getInstance().getRepositoryConnector().getTaskData(repo, data.getTaskId(), new NullProgressMonitor());
        } catch (CoreException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
        }
        if(controller != null) {
            controller.refreshViewData();
        }
    }

    @Override
    public IssueNode getNode() {
        if(node == null) {
            node = createNode();
        }
        return node;
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

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("hh24:mmm:ss dd.mm.yyyy");
    private class Controller extends BugtrackingController implements ActionListener, MouseMotionListener, MouseInputListener {
        private IssuePanel panel = new IssuePanel();
        private AddCommentPanel addCommentPanel = new AddCommentPanel();
        private ResolvePanel resolvePanel = new ResolvePanel();
        private StyledDocument doc;
        private DefaultListModel attachmentsModel;

        private final static String HL_ATTRIBUTE = "linkact";

        public Controller() {
            doc = panel.commenstPane.getStyledDocument();

            attachmentsModel = new DefaultListModel();
            panel.attachmentsList.setModel(attachmentsModel);
            panel.attachmentsList.setCellRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    if(value != null) {
                        BugzillaIssue.Attachment a = (BugzillaIssue.Attachment) value;
                        value = a.getFilename() + " : " + a.getAuthor() + " : " + dateFormat.format(a.getDate());
                    }
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            });

            panel.addCommentButton.addActionListener(this);
            panel.resolveButton.addActionListener(this);
            panel.attachFileButton.addActionListener(this);
            panel.commenstPane.addMouseMotionListener(this);
            panel.commenstPane.addMouseListener(this);

            refreshViewData();
        }

        @Override
        public JComponent getComponent() {
            return panel;
        }

        @Override
        public HelpCtx getHelpContext() {
            return new HelpCtx(org.netbeans.modules.bugzilla.issue.BugzillaIssue.class);
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
                Element elem = doc.getCharacterElement(panel.commenstPane.viewToModel(e.getPoint()));
                AttributeSet as = elem.getAttributes();
                StackTraceAction a = (StackTraceAction) as.getAttribute(HL_ATTRIBUTE);
                if(a != null) {
                    a.openStackTrace(elem.getDocument().getText(elem.getStartOffset(), elem.getEndOffset() - elem.getStartOffset()));
                }
            } catch(Exception ex) {
                Bugzilla.LOG.log(Level.SEVERE, null, ex);
            }
        }

        public void mouseMoved(MouseEvent e) {
            Element elem = doc.getCharacterElement( panel.commenstPane.viewToModel(e.getPoint()));
			AttributeSet as = elem.getAttributes();
			if(StyleConstants.isUnderline(as)) {
				panel.commenstPane.setCursor(new Cursor(Cursor.HAND_CURSOR));
            } else {
				panel.commenstPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
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
            // XXX use controller

            if (!BugzillaUtil.show(addCommentPanel, "Got comment?", "Submit")) {
                return;
            }

            // XXX don't use this when submitting changes
            addComment(addCommentPanel.commentTextArea.getText(), false);
            refresh();
            refreshViewData();
        }

        private void onAttach() {
            JFileChooser fileChooser = new JFileChooser("", null);  // NOI18N
            fileChooser.setDialogTitle("Got file?");                // NOI18N
            fileChooser.setMultiSelectionEnabled(false);
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.showDialog(panel, "");                       // NOI18N
            File f = fileChooser.getSelectedFile();
            if (f == null) return;
            addAttachment(f, "Attaching", "attachment");      // NOI18N
            refresh();
            refreshViewData();
        }

        private void onResolve() {
            Set<TaskAttribute> attrs;
            try {
                BugzillaClient client = Bugzilla.getInstance().getRepositoryConnector().getClientManager().getClient(getRepository(), new NullProgressMonitor());
                List<String> res = client.getRepositoryConfiguration().getResolutions();
                resolvePanel.resolutionCBO.setModel(new DefaultComboBoxModel(res.toArray(new String[res.size()])));
                if (!BugzillaUtil.show(resolvePanel, "Got resolution?", "submit")) {
                    return;
                }
                attrs = getResolveAttributes((String) resolvePanel.resolutionCBO.getSelectedItem());
                RepositoryResponse rr = Bugzilla.getInstance().getRepositoryConnector().getTaskDataHandler().postTaskData(getRepository(), data, attrs, new NullProgressMonitor());
                refresh();
                refreshViewData();
            } catch (MalformedURLException ex) {
                Bugzilla.LOG.log(Level.SEVERE, null, ex);
            } catch (CoreException ex) {
                Bugzilla.LOG.log(Level.SEVERE, null, ex);
            }
        }

        private void refreshViewData() {
            panel.idTextField.setText(getID());
            panel.priorityField.setText(getPriority());
            panel.summaryField.setText(getSummary());
            String status = getStatus();
            String res = getResolution();
            if(res != null && !res.trim().equals("")) {
                status += ":" + res;
            }
            panel.statusField.setText(status);
            panel.typeField.setText(getSeverity());
            panel.descTextArea.setText(getDescription());
            BugzillaIssue.Comment[] comments = getComments();
            refreshComents(comments);
            BugzillaIssue.Attachment[] attachements = getAttachments();
            attachmentsModel.clear();
            for (BugzillaIssue.Attachment attachment : attachements) {
                attachmentsModel.addElement(attachment);
            }
        }

        private void refreshComents(BugzillaIssue.Comment[] comments) {
            StringBuffer sb = new StringBuffer();
            for (BugzillaIssue.Comment comment : comments) {
                sb.append(comment.getWho());
                sb.append(" : ");
                sb.append(comment.getWhen());
                sb.append("\n");
                sb.append(comment.getText());
                sb.append("\n\n");
            }
            String text = sb.toString();

            List<StackTracePosition> stacktraces = StackTraceSupport.find(sb.toString());
            if(stacktraces.isEmpty()) {
                panel.commenstPane.setText(text);
            } else {
                Style defStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
                Style hlStyle = doc.addStyle("regularBlue", defStyle);
                StyleConstants.setForeground(hlStyle, Color.BLUE);
                StyleConstants.setUnderline(hlStyle, true);

                int last = 0;
                panel.commenstPane.setText("");
                for (StackTracePosition stp : stacktraces) {
                    int start = stp.getStartOffset();
                    int end = stp.getEndOffset();

                    String st = text.substring(start, end);
                    hlStyle.addAttribute(HL_ATTRIBUTE, new StackTraceAction());
                    try {
                        doc.insertString(doc.getLength(), text.substring(last, start), defStyle);
                        doc.insertString(doc.getLength(), st, hlStyle);
                    } catch (BadLocationException ex) {
                        Bugzilla.LOG.log(Level.SEVERE, null, ex);
                    }
                    last = end;
                }
                try {
                    doc.insertString(doc.getLength(), text.substring(last), defStyle);
                } catch (BadLocationException ex) {
                    Bugzilla.LOG.log(Level.SEVERE, null, ex);
                }
            }

        }

        private class StackTraceAction {
            void openStackTrace(String text) {
                StackTraceSupport.findAndOpen(text);
            }
        }

    }

    private class IDProperty extends IssueProperty {
        public IDProperty() {
            super(LABEL_NAME_ID,
                  String.class,
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_ID_Title"), // NOI18N
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_ID_Desc")); // NOI18N
        }
        public Object getValue() {
            return BugzillaIssue.this.getID();
        }
    }

    private class SeverityProperty extends IssueProperty {
        public SeverityProperty() {
            super(LABEL_NAME_SEVERITY,
                  String.class,
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Severity_Title"), // NOI18N
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Severity_Desc")); // NOI18N
        }
        public Object getValue() {
            return BugzillaIssue.this.getSeverity();
        }
    }

    private class PriorityProperty extends IssueProperty {
        public PriorityProperty() {
            super(LABEL_NAME_PRIORITY,
                  String.class,
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Priority_Title"), // NOI18N
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Priority_Desc")); // NOI18N
        }
        public Object getValue() {
            return BugzillaIssue.this.getPriority();
        }
    }

    private class StatusProperty extends IssueProperty {
        public StatusProperty() {
            super(LABEL_NAME_STATUS,
                  String.class,
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Status_Title"), // NOI18N
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Status_Desc")); // NOI18N
        }
        public Object getValue() {
            return BugzillaIssue.this.getStatus();
        }
    }

    private class ResolutionProperty extends IssueProperty {
        public ResolutionProperty() {
            super(LABEL_NAME_RESOLUTION,
                  String.class,
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Resolution_Title"), // NOI18N
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_ID_Desc")); // NOI18N
        }
        public Object getValue() {
            return BugzillaIssue.this.getResolution();
        }
    }

    private class SummaryProperty extends IssueProperty {
        public SummaryProperty() {
            super(LABEL_NAME_SUMMARY,
                  String.class,
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Summary_Title"), // NOI18N
                  NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue_Summary_Desc")); // NOI18N
        }
        public Object getValue() {
            return BugzillaIssue.this.getSummary();
        }
    }

}
