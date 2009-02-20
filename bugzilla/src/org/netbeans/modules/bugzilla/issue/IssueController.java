/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.List;
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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaClient;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.util.StackTraceSupport;
import org.netbeans.modules.bugtracking.util.StackTraceSupport.StackTracePosition;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
import org.openide.util.HelpCtx;

/**
 *
 * @author Tomas Stupka
 */
public class IssueController extends BugtrackingController implements ActionListener, MouseMotionListener, MouseInputListener {
    private IssuePanel panel = new IssuePanel();
    private AddCommentPanel addCommentPanel = new AddCommentPanel();
    private ResolvePanel resolvePanel = new ResolvePanel();
    private StyledDocument doc;
    private DefaultListModel attachmentsModel;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("hh24:mmm:ss dd.mm.yyyy");
    private final static String HL_ATTRIBUTE = "linkact";
    private BugzillaIssue issue;

    public IssueController(BugzillaIssue issue) {
        this.issue = issue;
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
        issue.addComment(addCommentPanel.commentTextArea.getText(), false);
        issue.refresh();
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
        issue.addAttachment(f, "Attaching", "attachment");      // NOI18N
        issue.refresh();
        refreshViewData();
    }

    private void onResolve() {
        Set<TaskAttribute> attrs;
        try {
            BugzillaClient client = Bugzilla.getInstance().getRepositoryConnector().getClientManager().getClient(issue.getTaskRepository(), new NullProgressMonitor());
            List<String> res = client.getRepositoryConfiguration().getResolutions();
            resolvePanel.resolutionCBO.setModel(new DefaultComboBoxModel(res.toArray(new String[res.size()])));
            if (!BugzillaUtil.show(resolvePanel, "Got resolution?", "submit")) {
                return;
            }
            attrs = issue.getResolveAttributes((String) resolvePanel.resolutionCBO.getSelectedItem());
            RepositoryResponse rr = Bugzilla.getInstance().getRepositoryConnector().getTaskDataHandler().postTaskData(issue.getTaskRepository(), issue.getData(), attrs, new NullProgressMonitor());
            issue.refresh();
            refreshViewData();
        } catch (MalformedURLException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
        } catch (CoreException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
        }
    }

    void refreshViewData() {
        panel.idTextField.setText(issue.getID());
        panel.priorityField.setText(issue.getPriority());
        panel.summaryField.setText(issue.getSummary());
        String status = issue.getStatus();
        String res = issue.getResolution();
        if(res != null && !res.trim().equals("")) {
            status += ":" + res;
        }
        panel.statusField.setText(status);
        panel.typeField.setText(issue.getSeverity());
        panel.descTextArea.setText(issue.getDescription());
        BugzillaIssue.Comment[] comments = issue.getComments();
        refreshComents(comments);
        BugzillaIssue.Attachment[] attachements = issue.getAttachments();
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
