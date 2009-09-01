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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaRepositoryConnector;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaVersion;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.jdesktop.layout.GroupLayout;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCache;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCacheUtils;
import org.netbeans.modules.bugtracking.util.BugtrackingOwnerSupport;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.BugzillaConfig;
import org.netbeans.modules.bugzilla.kenai.KenaiRepository;
import org.netbeans.modules.bugzilla.repository.BugzillaConfiguration;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
import org.netbeans.modules.kenai.ui.spi.KenaiUserUI;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Panel showing (and allowing to edit) details of an issue.
 *
 * @author Jan Stola
 */
public class IssuePanel extends javax.swing.JPanel {
    private static final DateFormat creationFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm"); // NOI18N
    private static final DateFormat modificationFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // NOI18N
    private static final Color HIGHLIGHT_COLOR = new Color(217, 255, 217);
    private BugzillaIssue issue;
    private CommentsPanel commentsPanel;
    private AttachmentsPanel attachmentsPanel;
    private int resolvedIndex;
    private Map<BugzillaIssue.IssueField,String> initialValues = new HashMap<BugzillaIssue.IssueField,String>();
    private List<String> keywords = new LinkedList<String>();
    private boolean reloading;
    private boolean skipReload;
    private boolean usingTargetMilestones;
    private PropertyChangeListener tasklistListener;

    public IssuePanel() {
        initComponents();
        reportedField.setBackground(getBackground());
        modifiedField.setBackground(getBackground());
        resolutionField.setBackground(getBackground());
        productField.setBackground(getBackground());
        messagePanel.setBackground(getBackground());
        Font font = headerLabel.getFont();
        headerLabel.setFont(font.deriveFont((float)(font.getSize()*1.7)));
        duplicateLabel.setVisible(false);
        duplicateField.setVisible(false);
        duplicateButton.setVisible(false);
        attachDocumentListeners();
        attachHideStatusListener();

        // Not supported yet
        issueTypeLabel.setVisible(false);
        issueTypeCombo.setVisible(false);
        statusWhiteboardLabel.setVisible(false);
        statusWhiteboardField.setVisible(false);

        // A11Y - Issues 163597 and 163598
        BugtrackingUtil.fixFocusTraversalKeys(addCommentArea);

        // Comments panel
        commentsPanel = new CommentsPanel();
        commentsPanel.setNewCommentHandler(new CommentsPanel.NewCommentHandler() {
            public void append(String text) {
                addCommentArea.append(text);
                addCommentArea.requestFocus();
                scrollRectToVisible(scrollPane1.getBounds());
            }
        });
        attachmentsPanel = new AttachmentsPanel();
        GroupLayout layout = (GroupLayout)getLayout();
        layout.replace(dummyCommentsPanel, commentsPanel);
        layout.replace(dummyAttachmentsPanel, attachmentsPanel);
        attachmentsLabel.setLabelFor(attachmentsPanel);

        BugtrackingUtil.issue163946Hack(scrollPane1);
    }

    void reloadFormInAWT(final boolean force) {
        if (EventQueue.isDispatchThread()) {
            reloadForm(force);
        } else {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    reloadForm(force);
                }
            });
        }
    }

    PropertyChangeListener cacheListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getSource() != IssuePanel.this.issue) {
                return;
            }
            if (IssueCache.EVENT_ISSUE_SEEN_CHANGED.equals(evt.getPropertyName())) {
                updateFieldStatuses();
            }
        }
    };

    public void setIssue(BugzillaIssue issue) {
        if (this.issue == null) {
            issue.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (Issue.EVENT_ISSUE_DATA_CHANGED.equals(evt.getPropertyName())) {
                        reloadFormInAWT(false);
                    }
                }
            });

            IssueCacheUtils.removeCacheListener(issue, cacheListener);
            IssueCacheUtils.addCacheListener(issue, cacheListener);

            summaryField.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    changedUpdate(e);
                }
                public void removeUpdate(DocumentEvent e) {
                    changedUpdate(e);
                }
                public void changedUpdate(DocumentEvent e) {
                    updateNoSummary();
                }
            });
            keywordsField.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    changedUpdate(e);
                }
                public void removeUpdate(DocumentEvent e) {
                    changedUpdate(e);
                }
                public void changedUpdate(DocumentEvent e) {
                    updateInvalidKeyword();
                }
            });
        }
        this.issue = issue;
        initCombos();
        List<String> kws = issue.getBugzillaRepository().getConfiguration().getKeywords();
        keywords.clear();
        for (String keyword : kws) {
            keywords.add(keyword.toUpperCase());
        }
        boolean showQAContact = !(issue.getBugzillaRepository() instanceof KenaiRepository);
        if (qaContactLabel.isVisible() != showQAContact) {
            GroupLayout layout = (GroupLayout)getLayout();
            JLabel temp = new JLabel();
            swap(layout, ccLabel, qaContactLabel, temp);
            swap(layout, ccField, qaContactField, temp);
            qaContactLabel.setVisible(showQAContact);
            qaContactField.setVisible(showQAContact);
        }
        tasklistButton.setEnabled(false);
        reloadForm(true);
    }

    private static void swap(GroupLayout layout, JComponent comp1, JComponent comp2, JComponent temp) {
        layout.replace(comp1, temp);
        layout.replace(comp2, comp1);
        layout.replace(temp, comp2);
    }

    private int oldCommentCount;
    private void reloadForm(boolean force) {
        if (skipReload) {
            return;
        }
        int noWarnings = fieldWarnings.size();
        int noErrors = fieldErrors.size();
        if (force) {
            fieldWarnings.clear();
            fieldErrors.clear();
        }
        reloading = true;
        boolean isNew = issue.getTaskData().isNew();
        GroupLayout layout = (GroupLayout)getLayout();
        if (isNew) {
            if (productCombo.getParent() == null) {
                layout.replace(productField, productCombo);
            }
        } else {
            if (productField.getParent() == null) {
                layout.replace(productCombo, productField);
            }
        }
        productLabel.setLabelFor(isNew ? productCombo : productField);
        headerLabel.setVisible(!isNew);
        statusCombo.setEnabled(!isNew);
        org.openide.awt.Mnemonics.setLocalizedText(addCommentLabel, NbBundle.getMessage(IssuePanel.class, isNew ? "IssuePanel.description" : "IssuePanel.addCommentLabel.text")); // NOI18N
        reportedLabel.setVisible(!isNew);
        reportedField.setVisible(!isNew);
        modifiedLabel.setVisible(!isNew);
        modifiedField.setVisible(!isNew);
        separator.setVisible(!isNew);
        commentsPanel.setVisible(!isNew);
        attachmentsLabel.setVisible(!isNew);
        attachmentsPanel.setVisible(!isNew);
        refreshButton.setVisible(!isNew);
        separatorLabel.setVisible(!isNew);
        cancelButton.setVisible(!isNew);
        assignedField.setEditable(issue.isNew() || issue.canReassign());
        org.openide.awt.Mnemonics.setLocalizedText(submitButton, NbBundle.getMessage(IssuePanel.class, isNew ? "IssuePanel.submitButton.text.new" : "IssuePanel.submitButton.text")); // NOI18N
        if (isNew && force) {
            // Preselect the first product
            productCombo.setSelectedIndex(0);
            initStatusCombo("NEW"); // NOI18N
        } else {
            String format = NbBundle.getMessage(IssuePanel.class, "IssuePanel.headerLabel.format"); // NOI18N
            String headerTxt = MessageFormat.format(format, issue.getID(), issue.getSummary());
            headerLabel.setText(headerTxt);
            Dimension dim = headerLabel.getPreferredSize();
            headerLabel.setMinimumSize(new Dimension(0, dim.height));
            headerLabel.setPreferredSize(new Dimension(0, dim.height));
            reloadField(force, summaryField, BugzillaIssue.IssueField.SUMMARY, summaryWarning, summaryLabel);
            reloadField(force, productCombo, BugzillaIssue.IssueField.PRODUCT, productWarning, productLabel);
            reloadField(force, productField, BugzillaIssue.IssueField.PRODUCT, null, null);
            reloadField(force, componentCombo, BugzillaIssue.IssueField.COMPONENT, componentWarning, componentLabel);
            reloadField(force, versionCombo, BugzillaIssue.IssueField.VERSION, versionWarning, versionLabel);
            reloadField(force, platformCombo, BugzillaIssue.IssueField.PLATFORM, platformWarning, platformLabel);
            reloadField(force, osCombo, BugzillaIssue.IssueField.OS, osWarning, platformLabel);
            reloadField(force, resolutionField, BugzillaIssue.IssueField.RESOLUTION, null, null); // Must be before statusCombo
            String status = reloadField(force, statusCombo, BugzillaIssue.IssueField.STATUS, statusWarning, statusLabel);
            initStatusCombo(status);
            reloadField(force, resolutionCombo, BugzillaIssue.IssueField.RESOLUTION, resolutionWarning, resolutionLabel);
            String initialResolution = initialValues.get(BugzillaIssue.IssueField.RESOLUTION);
            if ("DUPLICATE".equals(initialResolution)) { // NOI18N
                duplicateField.setEditable(false);
                duplicateField.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
                duplicateField.setBackground(getBackground());
            } else {
                JTextField field = new JTextField();
                duplicateField.setEditable(true);
                duplicateField.setBorder(field.getBorder());
                duplicateField.setBackground(field.getBackground());
            }
            reloadField(force, priorityCombo, BugzillaIssue.IssueField.PRIORITY, priorityWarning, priorityLabel);
            reloadField(force, severityCombo, BugzillaIssue.IssueField.SEVERITY, severityWarning, priorityLabel);
            if (usingTargetMilestones) {
                reloadField(force, targetMilestoneCombo, BugzillaIssue.IssueField.MILESTONE, milestoneWarning, targetMilestoneLabel);
            }
            reloadField(force, urlField, BugzillaIssue.IssueField.URL, urlWarning, urlLabel);
            reloadField(force, keywordsField, BugzillaIssue.IssueField.KEYWORDS, keywordsWarning, keywordsLabel);

            // reported field
            boolean isKenaiRepository = (issue.getRepository() instanceof KenaiRepository);
            if (!isNew) {
                format = NbBundle.getMessage(IssuePanel.class, "IssuePanel.reportedLabel.format"); // NOI18N
                String creationTxt = issue.getFieldValue(BugzillaIssue.IssueField.CREATION);
                try {
                    Date creation = creationFormat.parse(creationTxt);
                    creationTxt = DateFormat.getDateInstance(DateFormat.DEFAULT).format(creation);
                } catch (ParseException pex) {
                    Bugzilla.LOG.log(Level.INFO, null, pex);
                }
                String reporterName = issue.getFieldValue(BugzillaIssue.IssueField.REPORTER_NAME);
                String reporter = issue.getFieldValue(BugzillaIssue.IssueField.REPORTER);
                String reporterTxt = ((reporterName == null) || (reporterName.trim().length() == 0)) ? reporter : reporterName;
                String reportedTxt = MessageFormat.format(format, creationTxt, reporterTxt);
                reportedField.setText(reportedTxt);
                fixPrefSize(reportedField);
                if (isKenaiRepository && (reportedStatusLabel.getIcon() == null)) {
                    int index = reporter.indexOf('@');
                    String userName = (index == -1) ? reporter : reporter.substring(0,index);
                    JLabel label = KenaiUserUI.forName(userName).createUserWidget();
                    label.setText(null);
                    ((GroupLayout)getLayout()).replace(reportedStatusLabel, label);
                    reportedStatusLabel = label;
                }

                // modified field
                String modifiedTxt = issue.getFieldValue(BugzillaIssue.IssueField.MODIFICATION);
                try {
                    Date modification = modificationFormat.parse(modifiedTxt);
                    modifiedTxt = DateFormat.getDateTimeInstance().format(modification);
                } catch (ParseException pex) {
                    Bugzilla.LOG.log(Level.INFO, null, pex);
                }
                modifiedField.setText(modifiedTxt);
                fixPrefSize(modifiedField);
            }

            String assignee = issue.getFieldValue(BugzillaIssue.IssueField.ASSIGNED_TO);
            if (isKenaiRepository && (assignee.trim().length() > 0) && (force || !assignedField.getText().equals(assignee))) {
                int index = assignee.indexOf('@');
                String userName = (index == -1) ? assignee : assignee.substring(0,index);
                JLabel label = KenaiUserUI.forName(userName).createUserWidget();
                label.setText(null);
                ((GroupLayout)getLayout()).replace(assignedToStatusLabel, label);
                assignedToStatusLabel = label;
            }
            if (force) {
                assignedToStatusLabel.setVisible(assignee.trim().length() > 0);
            }
            reloadField(force, assignedField, BugzillaIssue.IssueField.ASSIGNED_TO, assignedToWarning, assignedLabel);
            reloadField(force, qaContactField, BugzillaIssue.IssueField.QA_CONTACT, qaContactWarning, qaContactLabel);
            reloadField(force, ccField, BugzillaIssue.IssueField.CC, ccWarning, ccLabel);
            reloadField(force, dependsField, BugzillaIssue.IssueField.DEPENDS_ON, dependsOnWarning, dependsLabel);
            reloadField(force, blocksField, BugzillaIssue.IssueField.BLOCKS, blocksWarning, blocksLabel);
        }
        int newCommentCount = issue.getComments().length;
        if (!force && oldCommentCount != newCommentCount) {
            String message = NbBundle.getMessage(IssuePanel.class, "IssuePanel.commentAddedWarning"); // NOI18N
            if (!fieldWarnings.contains(message)) {
                fieldWarnings.add(0, message);
            }
        }
        oldCommentCount = newCommentCount;
        if (!isNew) {
            commentsPanel.setIssue(issue);
        }
        attachmentsPanel.setIssue(issue);
        BugtrackingUtil.keepFocusedComponentVisible(commentsPanel);
        BugtrackingUtil.keepFocusedComponentVisible(attachmentsPanel);
        if (force) {
            addCommentArea.setText(""); // NOI18N
        }
        updateTasklistButton();
        updateFieldStatuses();
        updateNoSummary();
        if ((fieldWarnings.size() != noWarnings) || (fieldErrors.size() != noErrors)) {
            updateMessagePanel();
        }
        reloading = false;
    }

    private static void fixPrefSize(JTextField textField) {
        // The preferred size of JTextField on (Classic) Windows look and feel
        // is one pixel shorter. The following code is a workaround.
        textField.setPreferredSize(null);
        Dimension dim = textField.getPreferredSize();
        Dimension fixedDim = new Dimension(dim.width+1, dim.height);
        textField.setPreferredSize(fixedDim);
    }

    private String reloadField(boolean force, JComponent component, BugzillaIssue.IssueField field, JLabel warningLabel, JLabel fieldLabel) {
        String currentValue = null;
        if (issue.getTaskData().isNew()) {
            force = true;
        }
        if (!force) {
            if (component instanceof JComboBox) {
                Object value = ((JComboBox)component).getSelectedItem();
                currentValue  = (value == null) ? "" : value.toString(); // NOI18N
            } else if (component instanceof JTextField) {
                currentValue = ((JTextField)component).getText();
            }
        }
        String initialValue = initialValues.get(field);
        String newValue = issue.getFieldValue(field);
        boolean valueModifiedByUser = (currentValue != null) && (initialValue != null) && !currentValue.equals(initialValue);
        boolean valueModifiedByServer = (initialValue != null) && (newValue != null) && !initialValue.equals(newValue);
        if (force || !valueModifiedByUser) {
            if (component instanceof JComboBox) {
                JComboBox combo = (JComboBox)component;
                selectInCombo(combo, newValue, true);
            } else if (component instanceof JTextField) {
                ((JTextField)component).setText(newValue);
            }
            if (force) {
                if (warningLabel != null) {
                    warningLabel.setIcon(null);
                }
            } else {
                if (valueModifiedByServer) {
                    if (warningLabel != null) {
                        warningLabel.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/bugzilla/resources/warning.gif", true)); // NOI18N
                        String fieldName = fieldName(fieldLabel);
                        String messageFormat = NbBundle.getMessage(IssuePanel.class, "IssuePanel.fieldModifiedWarning"); // NOI18N
                        String message = MessageFormat.format(messageFormat, fieldName, currentValue, newValue);
                        fieldWarnings.add(message);
                        warningLabel.setToolTipText(message);
                    }
                }
            }
            currentValue = newValue;
        } else {
            if (valueModifiedByServer && (warningLabel != null)) {
                warningLabel.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/bugzilla/resources/error.gif", true)); // NOI18N
                String fieldName = fieldName(fieldLabel);
                String messageFormat = NbBundle.getMessage(IssuePanel.class, "IssuePanel.fieldModifiedError"); // NOI18N
                String message = MessageFormat.format(messageFormat, fieldName, newValue);
                fieldErrors.add(message);
                warningLabel.setToolTipText(message);
            }
        }
        if ((BugzillaIssue.IssueField.SUMMARY == field) || (BugzillaIssue.IssueField.PLATFORM == field) || (BugzillaIssue.IssueField.PRIORITY == field)) {
            warningLabel.setVisible(warningLabel.getIcon() != null);
        }
        initialValues.put(field, newValue);
        return currentValue;
    }

    private void selectInCombo(JComboBox combo, Object value, boolean forceInModel) {
        if (value == null) return;
        combo.setSelectedItem(value);
        if (forceInModel && !value.equals("") && !value.equals(combo.getSelectedItem())) { // NOI18N
            // Reload of server attributes is needed - workarounding it
            ComboBoxModel model = combo.getModel();
            if (model instanceof DefaultComboBoxModel) {
                ((DefaultComboBoxModel)model).insertElementAt(value, 0);
                combo.setSelectedIndex(0);
            }
        }
    }

    private String fieldName(JLabel fieldLabel) {
        String txt = fieldLabel.getText().trim();
        if (txt.endsWith(":")) { // NOI18N
            txt = txt.substring(0, txt.length()-1);
        }
        return txt;
    }

    private void initCombos() {
        BugzillaRepository repository = issue.getBugzillaRepository();
        BugzillaConfiguration bc = repository.getConfiguration();
        if(bc == null || !bc.isValid()) {
            // XXX nice error msg?
            return;
        }
        productCombo.setModel(toComboModel(bc.getProducts()));
        // componentCombo, versionCombo, targetMilestoneCombo are filled
        // automatically when productCombo is set/changed
        platformCombo.setModel(toComboModel(bc.getPlatforms()));
        osCombo.setModel(toComboModel(bc.getOSs()));
        // Do not support MOVED resolution (yet?)
        List<String> resolutions = new LinkedList<String>(bc.getResolutions());
        resolutions.remove("MOVED"); // NOI18N
        resolutionCombo.setModel(toComboModel(resolutions));
        priorityCombo.setModel(toComboModel(bc.getPriorities()));
        priorityCombo.setRenderer(new PriorityRenderer());
        severityCombo.setModel(toComboModel(bc.getSeverities()));
        // stausCombo and resolution fields are filled in reloadForm
    }

    private void initStatusCombo(String status) {
        // Init statusCombo - allowed transitions (heuristics):
        // Open -> Open-Unconfirmed-Reopened+Resolved
        // Resolved -> Reopened+Close
        // Close-Resolved -> Reopened+Resolved+(Close with higher index)
        BugzillaRepository repository = issue.getBugzillaRepository();
        BugzillaConfiguration bc = repository.getConfiguration();
        if(bc == null || !bc.isValid()) {
            // XXX nice error msg?
            return;
        }
        List<String> allStatuses = bc.getStatusValues();
        List<String> openStatuses = bc.getOpenStatusValues();
        List<String> statuses = new LinkedList<String>();
        boolean oldRepository = (issue.getBugzillaRepository().getConfiguration().getInstalledVersion().compareMajorMinorOnly(BugzillaVersion.BUGZILLA_3_2) < 0);
        String nev = "NEW"; // NOI18N
        String unconfirmed = "UNCONFIRMED"; // NOI18N
        String reopened = "REOPENED"; // NOI18N
        String resolved = "RESOLVED"; // NOI18N
        if (openStatuses.contains(status)) {
            statuses.addAll(openStatuses);
            if (!unconfirmed.equals(status)) {
                statuses.remove(unconfirmed);
            }
            if (!reopened.equals(status)) {
                statuses.remove(reopened);
            }
            if (oldRepository && !nev.equals(status)) {
                statuses.remove(nev);
            }
            statuses.add(resolved);
        } else {
            if (allStatuses.contains(reopened)) {
                statuses.add(reopened);
            } else {
                // Pure guess
                statuses.addAll(openStatuses);
                statuses.remove(unconfirmed);
                if (oldRepository) {
                    statuses.remove(nev);
                }
            }
            if (resolved.equals(status)) {
                List<String> closedStatuses = new LinkedList<String>(allStatuses);
                closedStatuses.removeAll(openStatuses);
                statuses.addAll(closedStatuses);
            } else {
                if (!oldRepository) {
                    statuses.add(resolved);
                }
                if(!status.equals("")) {
                    for (int i=allStatuses.indexOf(status); i<allStatuses.size(); i++) {
                        String s = allStatuses.get(i);
                        if (!openStatuses.contains(s)) {
                            statuses.add(s);
                        }
                    }
                }
            }
            resolvedIndex = statuses.indexOf(resolved);
        }
        statusCombo.setModel(toComboModel(statuses));
        statusCombo.setSelectedItem(status);
    }

    private ComboBoxModel toComboModel(List<String> items) {
        return new DefaultComboBoxModel(items.toArray());
    }

    private void updateFieldStatuses() {
        updateFieldStatus(BugzillaIssue.IssueField.PRODUCT, productLabel);
        updateFieldStatus(BugzillaIssue.IssueField.COMPONENT, componentLabel);
        updateFieldStatus(BugzillaIssue.IssueField.VERSION, versionLabel);
        updateFieldStatus(BugzillaIssue.IssueField.PLATFORM, platformLabel);
        updateFieldStatus(BugzillaIssue.IssueField.OS, platformLabel);
        updateFieldStatus(BugzillaIssue.IssueField.STATUS, statusLabel);
        updateFieldStatus(BugzillaIssue.IssueField.RESOLUTION, resolutionLabel);
        updateFieldStatus(BugzillaIssue.IssueField.PRIORITY, priorityLabel);
        updateFieldStatus(BugzillaIssue.IssueField.SEVERITY, priorityLabel);
        updateFieldStatus(BugzillaIssue.IssueField.MILESTONE, targetMilestoneLabel);
        updateFieldStatus(BugzillaIssue.IssueField.URL, urlLabel);
        updateFieldStatus(BugzillaIssue.IssueField.KEYWORDS, keywordsLabel);
        updateFieldStatus(BugzillaIssue.IssueField.ASSIGNED_TO, assignedLabel);
        updateFieldStatus(BugzillaIssue.IssueField.QA_CONTACT, qaContactLabel);
        updateFieldStatus(BugzillaIssue.IssueField.CC, ccLabel);
        updateFieldStatus(BugzillaIssue.IssueField.DEPENDS_ON, dependsLabel);
        updateFieldStatus(BugzillaIssue.IssueField.BLOCKS, blocksLabel);
    }

    private void updateFieldStatus(BugzillaIssue.IssueField field, JLabel label) {
        boolean highlight = !issue.getTaskData().isNew() && (issue.getFieldStatus(field) != BugzillaIssue.FIELD_STATUS_UPTODATE);
        label.setOpaque(highlight);
        if (highlight) {
            label.setBackground(HIGHLIGHT_COLOR);
        }
    }

    private void cancelHighlight(JLabel label) {
        if (!reloading) {
            label.setOpaque(false);
            label.getParent().repaint();
        }
    }

    private void storeFieldValue(BugzillaIssue.IssueField field, JComboBox combo) {
        Object value = combo.getSelectedItem();
        // It (normally) should not happen that value is null, but issue 159804 shows that
        // some strange configurations (or other bugs) can lead into this situation
        if (value != null) {
            storeFieldValue(field, value.toString());
        }
    }

    private void storeFieldValue(BugzillaIssue.IssueField field, JTextComponent textComponent) {
        storeFieldValue(field, textComponent.getText());
    }

    private void storeFieldValue(BugzillaIssue.IssueField field, String value) {
        if (issue.getTaskData().isNew() || !value.equals(initialValues.get(field))) {
            if (field == BugzillaIssue.IssueField.STATUS) {
                if (value.equals("CLOSED")) { // NOI18N
                    issue.close();
                } else if (value.equals("VERIFIED")) { // NOI18N
                    issue.verify();
                } else if (value.equals("REOPENED")) { // NOI18N
                    issue.reopen();
                } else if (value.equals("RESOLVED")) { // NOI18N
                    issue.resolve(resolutionCombo.getSelectedItem().toString());
                } else if (value.equals("ASSIGNED")) { // NOI18N
                    issue.accept();
                }
            } else if ((field == BugzillaIssue.IssueField.ASSIGNED_TO) && !issue.isNew()) {
                issue.reassign(value);
            }
            issue.setFieldValue(field, value);
        }
    }

    private void attachDocumentListeners() {
        urlField.getDocument().addDocumentListener(new CancelHighlightDocumentListener(urlLabel));
        keywordsField.getDocument().addDocumentListener(new CancelHighlightDocumentListener(keywordsLabel));
        assignedField.getDocument().addDocumentListener(new CancelHighlightDocumentListener(assignedLabel));
        qaContactField.getDocument().addDocumentListener(new CancelHighlightDocumentListener(qaContactLabel));
        ccField.getDocument().addDocumentListener(new CancelHighlightDocumentListener(ccLabel));
        blocksField.getDocument().addDocumentListener(new CancelHighlightDocumentListener(blocksLabel));
        dependsField.getDocument().addDocumentListener(new CancelHighlightDocumentListener(dependsLabel));
        CyclicDependencyDocumentListener cyclicDependencyListener = new CyclicDependencyDocumentListener();
        blocksField.getDocument().addDocumentListener(cyclicDependencyListener);
        dependsField.getDocument().addDocumentListener(cyclicDependencyListener);
        addCommentArea.getDocument().addDocumentListener(new RevalidatingListener());
    }

    private void attachHideStatusListener() {
        assignedField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            public void removeUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            public void changedUpdate(DocumentEvent e) {
                if (!reloading) {
                    assignedToStatusLabel.setVisible(false);
                }
            }
        });
    }

    private void updateNoSummary() {
        if (summaryField.getText().trim().length() == 0) {
            if (!noSummary) {
                noSummary = true;
                updateMessagePanel();
            }
        } else {
            if (noSummary) {
                noSummary = false;
                updateMessagePanel();
            }
        }
    }

    private void updateInvalidKeyword() {
        boolean invalidFound = false;
        StringTokenizer st = new StringTokenizer(keywordsField.getText(), ", \t\n\r\f"); // NOI18N
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (!keywords.contains(token.toUpperCase())) {
                invalidFound = true;
                break;
            }
        }
        if (invalidFound != invalidKeyword) {
            invalidKeyword = invalidFound;
            updateMessagePanel();
        }
    }

    private boolean noSummary = false;
    private boolean invalidKeyword = false;
    private boolean cyclicDependency = false;
    private List<String> fieldErrors = new LinkedList<String>();
    private List<String> fieldWarnings = new LinkedList<String>();
    private void updateMessagePanel() {
        messagePanel.removeAll();
        if (noSummary) {
            JLabel noSummaryLabel = new JLabel();
            noSummaryLabel.setText(NbBundle.getMessage(IssuePanel.class, "IssuePanel.noSummary")); // NOI18N
            String icon = issue.getTaskData().isNew() ? "org/netbeans/modules/bugzilla/resources/info.png" : "org/netbeans/modules/bugzilla/resources/error.gif"; // NOI18N
            noSummaryLabel.setIcon(new ImageIcon(ImageUtilities.loadImage(icon)));
            messagePanel.add(noSummaryLabel);
        }
        if (cyclicDependency) {
            JLabel cyclicDependencyLabel = new JLabel();
            cyclicDependencyLabel.setText(NbBundle.getMessage(IssuePanel.class, "IssuePanel.cyclicDependency")); // NOI18N
            cyclicDependencyLabel.setIcon(new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/bugzilla/resources/error.gif"))); // NOI18N
            messagePanel.add(cyclicDependencyLabel);
        }
        if (invalidKeyword) {
            JLabel invalidKeywordLabel = new JLabel();
            invalidKeywordLabel.setText(NbBundle.getMessage(IssuePanel.class, "IssuePanel.invalidKeyword")); // NOI18N
            invalidKeywordLabel.setIcon(new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/bugzilla/resources/error.gif"))); // NOI18N
            messagePanel.add(invalidKeywordLabel);
        }
        if (noSummary || cyclicDependency || invalidKeyword) {
            submitButton.setEnabled(false);
        } else {
            submitButton.setEnabled(true);
        }
        for (String fieldError : fieldErrors) {
            JLabel errorLabel = new JLabel(fieldError);
            errorLabel.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/bugzilla/resources/error.gif", true)); // NOI18N
            messagePanel.add(errorLabel);
        }
        for (String fieldWarning : fieldWarnings) {
            JLabel warningLabel = new JLabel(fieldWarning);
            warningLabel.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/bugzilla/resources/warning.gif", true)); // NOI18N
            messagePanel.add(warningLabel);
        }
        if (noSummary || cyclicDependency || invalidKeyword || (fieldErrors.size() + fieldWarnings.size() > 0)) {
            messagePanel.setVisible(true);
            messagePanel.revalidate();
        } else {
            messagePanel.setVisible(false);
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (issue != null) {
            // Hack - reset any previous modifications when the issue window is reopened
            // XXX any chance to get rid of the hack?
            reloadForm(true);

            issue.opened();
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if(issue != null) {
            issue.closed();
        }
    }

    private Map<Component, Boolean> enableMap = new HashMap<Component, Boolean>();
    private void enableComponents(boolean enable) {
        enableComponents(this, enable);
        if (enable) {
            enableMap.clear();
        }
    }

    private void enableComponents(Component comp, boolean enable) {
        if (comp instanceof Container) {
            for (Component subComp : ((Container)comp).getComponents()) {
                enableComponents(subComp, enable);
            }
        }
        if ((comp instanceof JComboBox)
                || ((comp instanceof JTextComponent) && ((JTextComponent)comp).isEditable())
                || (comp instanceof AbstractButton)) {
            if (enable) {
                Boolean b = enableMap.get(comp);
                if (b != null) {
                    comp.setEnabled(b);
                }
            } else {
                enableMap.put(comp, comp.isEnabled());
                comp.setEnabled(false);
            }
        }
    }

    private void attachTasklistListener (BugzillaIssueProvider provider) {
        if (tasklistListener == null) { // is not attached yet
            // listens on events comming from the tasklist, like when an issue is removed, etc.
            // needed to correctly update tasklistButton label and status
            tasklistListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (BugzillaIssueProvider.PROPERTY_ISSUE_REMOVED.equals(evt.getPropertyName()) && issue.equals(evt.getOldValue())) {
                        Runnable inAWT = new Runnable() {
                            public void run() {
                                updateTasklistButton();
                            }
                        };
                        if (EventQueue.isDispatchThread()) {
                            inAWT.run();
                        } else {
                            EventQueue.invokeLater(inAWT);
                        }
                    }
                }
            };
            provider.addPropertyChangeListener(org.openide.util.WeakListeners.propertyChange(tasklistListener, provider));
        }
    }

    private void updateTasklistButton() {
        tasklistButton.setEnabled(false);
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                BugzillaIssueProvider provider = BugzillaIssueProvider.getInstance();
                if (provider == null || issue.isNew()) { // do not enable button for new issues
                    return;
                }
                final boolean isInTasklist = provider.isAdded(issue);
                if (isInTasklist) {
                    attachTasklistListener(provider);
                }
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        String tasklistMessage = NbBundle.getMessage(IssuePanel.class,
                                isInTasklist ? "IssuePanel.tasklistButton.remove" : "IssuePanel.tasklistButton.add"); // NOI18N
                        tasklistButton.setText(tasklistMessage);
                        tasklistButton.setEnabled(true);
                    }
                });
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        productField = new javax.swing.JTextField();
        resolutionCombo = new javax.swing.JComboBox();
        reportedField = new javax.swing.JTextField();
        dependsOnWarning = new javax.swing.JLabel();
        osWarning = new javax.swing.JLabel();
        messagePanel = new javax.swing.JPanel();
        ccWarning = new javax.swing.JLabel();
        dummyAttachmentsPanel = new javax.swing.JPanel();
        qaContactWarning = new javax.swing.JLabel();
        assignedToWarning = new javax.swing.JLabel();
        reportedStatusLabel = new javax.swing.JLabel();
        blocksButton = new javax.swing.JButton();
        modifiedField = new javax.swing.JTextField();
        blocksLabel = new javax.swing.JLabel();
        modifiedLabel = new javax.swing.JLabel();
        dependsOnButton = new javax.swing.JButton();
        dependsField = new javax.swing.JTextField();
        dependsLabel = new javax.swing.JLabel();
        reportedLabel = new javax.swing.JLabel();
        resolutionField = new javax.swing.JTextField();
        duplicateButton = new javax.swing.JButton();
        statusCombo = new javax.swing.JComboBox();
        duplicateField = new javax.swing.JTextField();
        statusLabel = new javax.swing.JLabel();
        duplicateLabel = new javax.swing.JLabel();
        osCombo = new javax.swing.JComboBox();
        summaryWarning = new javax.swing.JLabel();
        ccField = new javax.swing.JTextField();
        ccLabel = new javax.swing.JLabel();
        qaContactLabel = new javax.swing.JLabel();
        blocksWarning = new javax.swing.JLabel();
        assignedField = new javax.swing.JTextField();
        resolutionWarning = new javax.swing.JLabel();
        assignedLabel = new javax.swing.JLabel();
        statusWarning = new javax.swing.JLabel();
        keywordsWarning = new javax.swing.JLabel();
        milestoneWarning = new javax.swing.JLabel();
        severityWarning = new javax.swing.JLabel();
        platformWarning = new javax.swing.JLabel();
        versionWarning = new javax.swing.JLabel();
        productWarning = new javax.swing.JLabel();
        addCommentLabel = new javax.swing.JLabel();
        attachmentsLabel = new javax.swing.JLabel();
        summaryLabel = new javax.swing.JLabel();
        assignedToStatusLabel = new javax.swing.JLabel();
        keywordsButton = new javax.swing.JButton();
        dummyLabel2 = new javax.swing.JLabel();
        keywordsLabel = new javax.swing.JLabel();
        targetMilestoneCombo = new javax.swing.JComboBox();
        blocksField = new javax.swing.JTextField();
        severityCombo = new javax.swing.JComboBox();
        priorityLabel = new javax.swing.JLabel();
        dummyLabel1 = new javax.swing.JLabel();
        componentCombo = new javax.swing.JComboBox();
        productCombo = new javax.swing.JComboBox();
        dummyCommentsPanel = new javax.swing.JPanel();
        separator = new javax.swing.JSeparator();
        headerLabel = new javax.swing.JLabel();
        refreshButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        reloadButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        separatorLabel = new javax.swing.JLabel();
        resolutionLabel = new javax.swing.JLabel();
        tasklistButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        separatorLabel2 = new javax.swing.JLabel();
        productLabel = new javax.swing.JLabel();
        componentLabel = new javax.swing.JLabel();
        versionLabel = new javax.swing.JLabel();
        versionCombo = new javax.swing.JComboBox();
        platformCombo = new javax.swing.JComboBox();
        platformLabel = new javax.swing.JLabel();
        priorityCombo = new javax.swing.JComboBox();
        priorityWarning = new javax.swing.JLabel();
        targetMilestoneLabel = new javax.swing.JLabel();
        qaContactField = new javax.swing.JTextField();
        urlLabel = new javax.swing.JLabel();
        keywordsField = new javax.swing.JTextField();
        urlField = new javax.swing.JTextField();
        statusWhiteboardLabel = new javax.swing.JLabel();
        statusWhiteboardField = new javax.swing.JTextField();
        issueTypeLabel = new javax.swing.JLabel();
        issueTypeCombo = new javax.swing.JComboBox();
        dummyLabel3 = new javax.swing.JLabel();
        summaryField = new javax.swing.JTextField();
        urlWarning = new javax.swing.JLabel();
        scrollPane1 = new javax.swing.JScrollPane();
        addCommentArea = new javax.swing.JTextArea() {
            @Override
            public Dimension getPreferredScrollableViewportSize() {
                Dimension dim = super.getPreferredScrollableViewportSize();
                JScrollPane scrollPane = (JScrollPane)SwingUtilities.getAncestorOfClass(JScrollPane.class, this);
                int delta = 0;
                if (scrollPane != null) {
                    Component comp = scrollPane.getHorizontalScrollBar();
                    delta = comp.isVisible() ? comp.getHeight() : 0;
                }
                dim = new Dimension(dim.width, delta + ((dim.height < 100) ? 100 : dim.height));
                return dim;
            }
        };
        submitButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        componentWarning = new javax.swing.JLabel();

        FormListener formListener = new FormListener();

        productField.setEditable(false);
        productField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        productField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.productField.AccessibleContext.accessibleDescription")); // NOI18N

        resolutionCombo.addActionListener(formListener);
        resolutionCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.resolutionCombo.AccessibleContext.accessibleDescription")); // NOI18N

        setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));

        reportedField.setEditable(false);
        reportedField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        messagePanel.setLayout(new javax.swing.BoxLayout(messagePanel, javax.swing.BoxLayout.PAGE_AXIS));

        org.openide.awt.Mnemonics.setLocalizedText(blocksButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.blocksButton.text")); // NOI18N
        blocksButton.setFocusPainted(false);
        blocksButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        blocksButton.addActionListener(formListener);

        modifiedField.setEditable(false);
        modifiedField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        blocksLabel.setLabelFor(blocksField);
        org.openide.awt.Mnemonics.setLocalizedText(blocksLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.blocksLabel.text")); // NOI18N

        modifiedLabel.setLabelFor(modifiedField);
        org.openide.awt.Mnemonics.setLocalizedText(modifiedLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.modifiedLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(dependsOnButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.dependsOnButton.text")); // NOI18N
        dependsOnButton.setFocusPainted(false);
        dependsOnButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        dependsOnButton.addActionListener(formListener);

        dependsField.setColumns(15);

        dependsLabel.setLabelFor(dependsField);
        org.openide.awt.Mnemonics.setLocalizedText(dependsLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.dependsLabel.text")); // NOI18N

        reportedLabel.setLabelFor(reportedField);
        org.openide.awt.Mnemonics.setLocalizedText(reportedLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.reportedLabel.text")); // NOI18N

        resolutionField.setEditable(false);
        resolutionField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(duplicateButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.duplicateButton.text")); // NOI18N
        duplicateButton.setFocusPainted(false);
        duplicateButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        duplicateButton.addActionListener(formListener);

        statusCombo.addActionListener(formListener);

        duplicateField.setColumns(15);

        statusLabel.setLabelFor(statusCombo);
        org.openide.awt.Mnemonics.setLocalizedText(statusLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.statusLabel.text")); // NOI18N

        duplicateLabel.setLabelFor(duplicateField);
        org.openide.awt.Mnemonics.setLocalizedText(duplicateLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.duplicateLabel.text")); // NOI18N

        osCombo.addActionListener(formListener);

        ccLabel.setLabelFor(ccField);
        org.openide.awt.Mnemonics.setLocalizedText(ccLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.ccLabel.text")); // NOI18N

        qaContactLabel.setLabelFor(qaContactField);
        org.openide.awt.Mnemonics.setLocalizedText(qaContactLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.qaContactLabel.text")); // NOI18N

        assignedLabel.setLabelFor(assignedField);
        org.openide.awt.Mnemonics.setLocalizedText(assignedLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.assignedLabel.text")); // NOI18N

        addCommentLabel.setLabelFor(addCommentArea);
        org.openide.awt.Mnemonics.setLocalizedText(addCommentLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.addCommentLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(attachmentsLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.attachmentsLabel.text")); // NOI18N

        summaryLabel.setLabelFor(summaryField);
        org.openide.awt.Mnemonics.setLocalizedText(summaryLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.summaryLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(keywordsButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.keywordsButton.text")); // NOI18N
        keywordsButton.setFocusPainted(false);
        keywordsButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        keywordsButton.addActionListener(formListener);

        keywordsLabel.setLabelFor(keywordsField);
        org.openide.awt.Mnemonics.setLocalizedText(keywordsLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.keywordsLabel.text")); // NOI18N

        targetMilestoneCombo.addActionListener(formListener);

        blocksField.setColumns(15);

        severityCombo.addActionListener(formListener);

        priorityLabel.setLabelFor(priorityCombo);
        org.openide.awt.Mnemonics.setLocalizedText(priorityLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.priorityLabel.text")); // NOI18N

        componentCombo.addActionListener(formListener);

        productCombo.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(refreshButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.refreshButton.text")); // NOI18N
        refreshButton.setToolTipText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.refreshButton.toolTipText")); // NOI18N
        refreshButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(reloadButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.reloadButton.text")); // NOI18N
        reloadButton.setToolTipText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.reloadButton.toolTipText")); // NOI18N
        reloadButton.addActionListener(formListener);

        separatorLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.openide.awt.Mnemonics.setLocalizedText(resolutionLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.resolutionLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(tasklistButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.tasklistButton.add")); // NOI18N
        tasklistButton.addActionListener(formListener);

        separatorLabel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.openide.awt.Mnemonics.setLocalizedText(productLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.productLabel.text")); // NOI18N

        componentLabel.setLabelFor(componentCombo);
        org.openide.awt.Mnemonics.setLocalizedText(componentLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.componentLabel.text")); // NOI18N

        versionLabel.setLabelFor(versionCombo);
        org.openide.awt.Mnemonics.setLocalizedText(versionLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.versionLabel.text")); // NOI18N

        versionCombo.addActionListener(formListener);

        platformCombo.addActionListener(formListener);

        platformLabel.setLabelFor(platformCombo);
        org.openide.awt.Mnemonics.setLocalizedText(platformLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.platformLabel.text")); // NOI18N

        priorityCombo.addActionListener(formListener);

        targetMilestoneLabel.setLabelFor(targetMilestoneCombo);
        org.openide.awt.Mnemonics.setLocalizedText(targetMilestoneLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.targetMilestoneLabel.text")); // NOI18N

        urlLabel.setLabelFor(urlField);
        org.openide.awt.Mnemonics.setLocalizedText(urlLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.urlLabel.text")); // NOI18N

        keywordsField.setColumns(15);

        urlField.setColumns(15);

        org.openide.awt.Mnemonics.setLocalizedText(statusWhiteboardLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.statusWhiteboardLabel.text")); // NOI18N

        statusWhiteboardField.setColumns(15);

        org.openide.awt.Mnemonics.setLocalizedText(issueTypeLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.issueTypeLabel.text")); // NOI18N

        scrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        addCommentArea.setRows(5);
        scrollPane1.setViewportView(addCommentArea);
        addCommentArea.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.addCommentArea.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(submitButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.submitButton.text")); // NOI18N
        submitButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(formListener);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, separator)
            .add(dummyCommentsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(headerLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(tasklistButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(separatorLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(refreshButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(separatorLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(reloadButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(urlLabel)
                            .add(componentLabel)
                            .add(productLabel)
                            .add(versionLabel)
                            .add(platformLabel)
                            .add(dummyLabel1)
                            .add(priorityLabel)
                            .add(targetMilestoneLabel)
                            .add(dummyLabel2)
                            .add(keywordsLabel)
                            .add(statusWhiteboardLabel)
                            .add(issueTypeLabel)
                            .add(dummyLabel3)
                            .add(summaryLabel)
                            .add(attachmentsLabel)
                            .add(addCommentLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(dummyAttachmentsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap())
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                    .add(issueTypeCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(statusWhiteboardField, 0, 0, Short.MAX_VALUE)
                                    .add(urlField, 0, 0, Short.MAX_VALUE)
                                    .add(layout.createSequentialGroup()
                                        .add(keywordsField, 0, 0, Short.MAX_VALUE)
                                        .add(0, 0, 0)
                                        .add(keywordsButton))
                                    .add(targetMilestoneCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(layout.createSequentialGroup()
                                        .add(priorityCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(priorityWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(severityCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(layout.createSequentialGroup()
                                        .add(platformCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(platformWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(osCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(versionCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(componentCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(productCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                    .add(layout.createSequentialGroup()
                                        .add(productWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .add(reportedLabel))
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                        .add(keywordsWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .add(blocksLabel))
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(urlWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(milestoneWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(severityWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(versionWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(componentWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(osWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                        .add(7, 7, 7)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(org.jdesktop.layout.GroupLayout.TRAILING, assignedLabel)
                                            .add(org.jdesktop.layout.GroupLayout.TRAILING, qaContactLabel)
                                            .add(org.jdesktop.layout.GroupLayout.TRAILING, ccLabel)
                                            .add(org.jdesktop.layout.GroupLayout.TRAILING, statusLabel)
                                            .add(org.jdesktop.layout.GroupLayout.TRAILING, resolutionLabel)
                                            .add(org.jdesktop.layout.GroupLayout.TRAILING, dependsLabel)
                                            .add(org.jdesktop.layout.GroupLayout.TRAILING, duplicateLabel))))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                            .add(layout.createSequentialGroup()
                                                .add(duplicateField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .add(0, 0, 0)
                                                .add(duplicateButton))
                                            .add(layout.createSequentialGroup()
                                                .add(blocksField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .add(0, 0, 0)
                                                .add(blocksButton))
                                            .add(layout.createSequentialGroup()
                                                .add(dependsField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .add(0, 0, 0)
                                                .add(dependsOnButton))
                                            .add(ccField)
                                            .add(qaContactField)
                                            .add(assignedField)
                                            .add(statusCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .add(resolutionField))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(layout.createSequentialGroup()
                                                .add(assignedToStatusLabel)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(assignedToWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                            .add(qaContactWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(ccWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(statusWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(resolutionWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 11, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(blocksWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(dependsOnWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                    .add(layout.createSequentialGroup()
                                        .add(reportedField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(reportedStatusLabel)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                        .add(modifiedLabel)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(modifiedField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                .add(11, 11, 11))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, scrollPane1)
                                    .add(layout.createSequentialGroup()
                                        .add(summaryField)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(summaryWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 9, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                        .add(submitButton)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(cancelButton))
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, messagePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .add(10, 10, 10))))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(headerLabel)
                    .add(separatorLabel2)
                    .add(tasklistButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(separatorLabel)
                    .add(reloadButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(refreshButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(productLabel)
                    .add(productCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(productWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(reportedLabel)
                    .add(reportedField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(modifiedLabel)
                    .add(modifiedField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(reportedStatusLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(componentLabel)
                    .add(componentCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(componentWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(assignedLabel)
                    .add(assignedField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(assignedToStatusLabel)
                    .add(assignedToWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(versionLabel)
                    .add(versionCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(versionWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(qaContactLabel)
                    .add(qaContactField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(qaContactWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(platformLabel)
                    .add(platformCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(ccLabel)
                    .add(ccField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(platformWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(osCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(ccWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(osWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(issueTypeLabel)
                    .add(issueTypeCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(dummyLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(priorityLabel)
                    .add(priorityCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(priorityWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(severityCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(severityWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(statusLabel)
                    .add(statusCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(statusWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(targetMilestoneLabel)
                    .add(targetMilestoneCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(milestoneWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(resolutionLabel)
                    .add(resolutionField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(resolutionWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 11, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(dummyLabel2)
                    .add(duplicateLabel)
                    .add(duplicateField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(duplicateButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(urlLabel)
                    .add(urlField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(urlWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(dependsLabel)
                    .add(dependsField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(dependsOnButton)
                    .add(dependsOnWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(keywordsLabel)
                    .add(keywordsField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(keywordsButton)
                    .add(keywordsWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(blocksLabel)
                    .add(blocksField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(blocksButton)
                    .add(blocksWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(statusWhiteboardLabel)
                    .add(statusWhiteboardField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(attachmentsLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(dummyLabel3))
                    .add(dummyAttachmentsPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(summaryLabel)
                    .add(summaryField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(summaryWarning, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 12, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(addCommentLabel)
                    .add(scrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(submitButton)
                    .add(cancelButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(messagePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(separator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(dummyCommentsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(new java.awt.Component[] {dummyLabel1, dummyLabel2, dummyLabel3, priorityCombo}, org.jdesktop.layout.GroupLayout.VERTICAL);

        layout.linkSize(new java.awt.Component[] {refreshButton, reloadButton, separatorLabel, separatorLabel2}, org.jdesktop.layout.GroupLayout.VERTICAL);

        reportedField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.reportedField.AccessibleContext.accessibleDescription")); // NOI18N
        blocksButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.blocksButton.AccessibleContext.accessibleDescription")); // NOI18N
        modifiedField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.modifiedField.AccessibleContext.accessibleDescription")); // NOI18N
        dependsOnButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.dependsOnButton.AccessibleContext.accessibleDescription")); // NOI18N
        dependsField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.dependsField.AccessibleContext.accessibleDescription")); // NOI18N
        resolutionField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.resolutionField.AccessibleContext.accessibleDescription")); // NOI18N
        duplicateButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.duplicateButton.AccessibleContext.accessibleDescription")); // NOI18N
        statusCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.statusCombo.AccessibleContext.accessibleDescription")); // NOI18N
        duplicateField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.duplicateField.AccessibleContext.accessibleDescription")); // NOI18N
        osCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.osCombo.AccessibleContext.accessibleDescription")); // NOI18N
        ccField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.ccField.AccessibleContext.accessibleDescription")); // NOI18N
        assignedField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.assignedField.AccessibleContext.accessibleDescription")); // NOI18N
        keywordsButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.keywordsButton.AccessibleContext.accessibleDescription")); // NOI18N
        targetMilestoneCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.targetMilestoneCombo.AccessibleContext.accessibleDescription")); // NOI18N
        blocksField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.blocksField.AccessibleContext.accessibleDescription")); // NOI18N
        severityCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.severityCombo.AccessibleContext.accessibleDescription")); // NOI18N
        componentCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.componentCombo.AccessibleContext.accessibleDescription")); // NOI18N
        productCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.productCombo.AccessibleContext.accessibleDescription")); // NOI18N
        refreshButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.refreshButton.AccessibleContext.accessibleDescription")); // NOI18N
        versionCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.versionCombo.AccessibleContext.accessibleDescription")); // NOI18N
        platformCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.platformCombo.AccessibleContext.accessibleDescription")); // NOI18N
        priorityCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.priorityCombo.AccessibleContext.accessibleDescription")); // NOI18N
        qaContactField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.qaContactField.AccessibleContext.accessibleDescription")); // NOI18N
        keywordsField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.keywordsField.AccessibleContext.accessibleDescription")); // NOI18N
        urlField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.urlField.AccessibleContext.accessibleDescription")); // NOI18N
        submitButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.submitButton.AccessibleContext.accessibleDescription")); // NOI18N
        cancelButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.cancelButton.AccessibleContext.accessibleDescription")); // NOI18N
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == blocksButton) {
                IssuePanel.this.blocksButtonActionPerformed(evt);
            }
            else if (evt.getSource() == dependsOnButton) {
                IssuePanel.this.dependsOnButtonActionPerformed(evt);
            }
            else if (evt.getSource() == duplicateButton) {
                IssuePanel.this.duplicateButtonActionPerformed(evt);
            }
            else if (evt.getSource() == statusCombo) {
                IssuePanel.this.statusComboActionPerformed(evt);
            }
            else if (evt.getSource() == osCombo) {
                IssuePanel.this.osComboActionPerformed(evt);
            }
            else if (evt.getSource() == keywordsButton) {
                IssuePanel.this.keywordsButtonActionPerformed(evt);
            }
            else if (evt.getSource() == targetMilestoneCombo) {
                IssuePanel.this.targetMilestoneComboActionPerformed(evt);
            }
            else if (evt.getSource() == severityCombo) {
                IssuePanel.this.severityComboActionPerformed(evt);
            }
            else if (evt.getSource() == componentCombo) {
                IssuePanel.this.componentComboActionPerformed(evt);
            }
            else if (evt.getSource() == productCombo) {
                IssuePanel.this.productComboActionPerformed(evt);
            }
            else if (evt.getSource() == refreshButton) {
                IssuePanel.this.refreshButtonActionPerformed(evt);
            }
            else if (evt.getSource() == reloadButton) {
                IssuePanel.this.reloadButtonActionPerformed(evt);
            }
            else if (evt.getSource() == tasklistButton) {
                IssuePanel.this.tasklistButtonActionPerformed(evt);
            }
            else if (evt.getSource() == versionCombo) {
                IssuePanel.this.versionComboActionPerformed(evt);
            }
            else if (evt.getSource() == platformCombo) {
                IssuePanel.this.platformComboActionPerformed(evt);
            }
            else if (evt.getSource() == priorityCombo) {
                IssuePanel.this.priorityComboActionPerformed(evt);
            }
            else if (evt.getSource() == submitButton) {
                IssuePanel.this.submitButtonActionPerformed(evt);
            }
            else if (evt.getSource() == cancelButton) {
                IssuePanel.this.cancelButtonActionPerformed(evt);
            }
            else if (evt.getSource() == resolutionCombo) {
                IssuePanel.this.resolutionComboActionPerformed(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void productComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_productComboActionPerformed
        cancelHighlight(productLabel);
        // Reload componentCombo, versionCombo and targetMilestoneCombo
        BugzillaRepository repository = issue.getBugzillaRepository();
        BugzillaConfiguration bc = repository.getConfiguration();
        if(bc == null || !bc.isValid()) {
            // XXX nice error msg?
            return;
        }
        String product = productCombo.getSelectedItem().toString();
        Object component = componentCombo.getSelectedItem();
        Object version = versionCombo.getSelectedItem();
        Object targetMilestone = targetMilestoneCombo.getSelectedItem();
        componentCombo.setModel(toComboModel(bc.getComponents(product)));
        versionCombo.setModel(toComboModel(bc.getVersions(product)));
        List<String> targetMilestones = bc.getTargetMilestones(product);
        usingTargetMilestones = (targetMilestones.size() != 0);
        targetMilestoneCombo.setModel(toComboModel(targetMilestones));
        // Attempt to keep selection
        boolean isNew = issue.getTaskData().isNew();
        selectInCombo(componentCombo, component, !isNew);
        selectInCombo(versionCombo, version, !isNew);
        if (usingTargetMilestones) {
            selectInCombo(targetMilestoneCombo, targetMilestone, !isNew);
        }
        targetMilestoneLabel.setVisible(usingTargetMilestones);
        targetMilestoneCombo.setVisible(usingTargetMilestones);
        milestoneWarning.setVisible(usingTargetMilestones);
        TaskData data = issue.getTaskData();
        if (data.isNew()) {
            String summary = summaryField.getText();
            issue.setFieldValue(BugzillaIssue.IssueField.PRODUCT, product);
            BugzillaRepositoryConnector connector = Bugzilla.getInstance().getRepositoryConnector();
            try {
                connector.getTaskDataHandler().initializeTaskData(issue.getBugzillaRepository().getTaskRepository(), data, connector.getTaskMapping(data), new NullProgressMonitor());
                reloadForm(false);
            } catch (CoreException cex) {
                cex.printStackTrace();
            }
            summaryField.setText(summary); // Issue 165107
        }
    }//GEN-LAST:event_productComboActionPerformed

    private void statusComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statusComboActionPerformed
        cancelHighlight(statusLabel);
        cancelHighlight(resolutionLabel);
        // Hide/show resolution combo
        String initialStatus = initialValues.get(BugzillaIssue.IssueField.STATUS);
        boolean resolvedInitial = "RESOLVED".equals(initialStatus); // NOI18N
        if (!resolvedInitial) {
            if ("RESOLVED".equals(statusCombo.getSelectedItem())) { // NOI18N
                if (resolutionCombo.getParent() == null) {
                    ((GroupLayout)getLayout()).replace(resolutionField, resolutionCombo);
                }
                resolutionCombo.setSelectedItem("FIXED"); // NOI18N
                resolutionCombo.setVisible(true);
            } else {
                resolutionCombo.setVisible(false);
                duplicateLabel.setVisible(false);
                duplicateField.setVisible(false);
                duplicateButton.setVisible(false);
            }
        }
        if (!resolutionField.getText().trim().equals("")) { // NOI18N
            if (statusCombo.getSelectedIndex() >= resolvedIndex) {
                if (resolutionField.getParent() == null) {
                    ((GroupLayout)getLayout()).replace(resolutionCombo, resolutionField);
                }
                resolutionField.setVisible(true);
            } else {
                resolutionField.setVisible(false);
            }
            duplicateLabel.setVisible(false);
            duplicateField.setVisible(false);
            duplicateButton.setVisible(false);
        }
        resolutionLabel.setLabelFor(resolutionCombo.isVisible() ? resolutionCombo : resolutionField);
    }//GEN-LAST:event_statusComboActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        reloadForm(true);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void submitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitButtonActionPerformed
        boolean isNew = issue.getTaskData().isNew();
        if (isNew) {
            storeFieldValue(BugzillaIssue.IssueField.DESCRIPTION, addCommentArea);
        }
        storeFieldValue(BugzillaIssue.IssueField.SUMMARY, summaryField);
        storeFieldValue(BugzillaIssue.IssueField.PRODUCT, productCombo);
        storeFieldValue(BugzillaIssue.IssueField.COMPONENT, componentCombo);
        storeFieldValue(BugzillaIssue.IssueField.VERSION, versionCombo);
        storeFieldValue(BugzillaIssue.IssueField.PLATFORM, platformCombo);
        storeFieldValue(BugzillaIssue.IssueField.OS, osCombo);
        storeFieldValue(BugzillaIssue.IssueField.STATUS, statusCombo);
        if (resolutionCombo.isVisible()) {
            storeFieldValue(BugzillaIssue.IssueField.RESOLUTION, resolutionCombo);
        } else if (!resolutionField.isVisible()) {
            storeFieldValue(BugzillaIssue.IssueField.RESOLUTION, ""); // NOI18N
        }
        if (duplicateField.isVisible() && duplicateField.isEditable()) {
            issue.duplicate(duplicateField.getText());
        }
        storeFieldValue(BugzillaIssue.IssueField.PRIORITY, priorityCombo);
        storeFieldValue(BugzillaIssue.IssueField.SEVERITY, severityCombo);
        if (usingTargetMilestones) {
            storeFieldValue(BugzillaIssue.IssueField.MILESTONE, targetMilestoneCombo);
        }
        storeFieldValue(BugzillaIssue.IssueField.URL, urlField);
        storeFieldValue(BugzillaIssue.IssueField.KEYWORDS, keywordsField);
        storeFieldValue(BugzillaIssue.IssueField.ASSIGNED_TO, assignedField);
        storeFieldValue(BugzillaIssue.IssueField.QA_CONTACT, qaContactField);
        storeCCValue();
        storeFieldValue(BugzillaIssue.IssueField.DEPENDS_ON, dependsField);
        storeFieldValue(BugzillaIssue.IssueField.BLOCKS, blocksField);
        if (!isNew && !"".equals(addCommentArea.getText().trim())) { // NOI18N
            issue.addComment(addCommentArea.getText());
        }
        String submitMessage;
        if (isNew) {
            submitMessage = NbBundle.getMessage(IssuePanel.class, "IssuePanel.submitNewMessage"); // NOI18N
        } else {
            String submitMessageFormat = NbBundle.getMessage(IssuePanel.class, "IssuePanel.submitMessage"); // NOI18N
            submitMessage = MessageFormat.format(submitMessageFormat, issue.getID());
        }
        final ProgressHandle handle = ProgressHandleFactory.createHandle(submitMessage);
        handle.start();
        handle.switchToIndeterminate();
        skipReload = true;
        enableComponents(false);
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                boolean ret = false;
                try {
                    ret = issue.submitAndRefresh();
                    for (AttachmentsPanel.AttachmentInfo attachment : attachmentsPanel.getNewAttachments()) {
                        if (attachment.file.exists()) {
                            if (attachment.description.trim().length() == 0) {
                                attachment.description = NbBundle.getMessage(IssuePanel.class, "IssuePanel.attachment.noDescription"); // NOI18N
                            }
                            issue.addAttachment(attachment.file, null, attachment.description, attachment.contentType, attachment.isPatch); // NOI18N
                        } else {
                            // PENDING notify user
                        }
                    }
                } finally {
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            enableComponents(true);
                            skipReload = false;
                        }
                    });
                    handle.finish();
                    if(ret) {
                        reloadFormInAWT(true);
                    }
                }
            }
        });
        if (isNew) {
            BugzillaRepository repository = issue.getBugzillaRepository();
            if (repository != null) {
                BugtrackingOwnerSupport.getInstance().setLooseAssociation(
                        BugtrackingOwnerSupport.ContextType.SELECTED_FILE_AND_ALL_PROJECTS,
                        repository);
            }
        }
    }//GEN-LAST:event_submitButtonActionPerformed

    private void storeCCValue() {
        Set<String> oldCCs = ccs(issue.getFieldValue(BugzillaIssue.IssueField.CC));
        Set<String> newCCs = ccs(ccField.getText());

        String removedCCs = getMissingCCs(oldCCs, newCCs);
        String addedCCs = getMissingCCs(newCCs, oldCCs);

        storeFieldValue(BugzillaIssue.IssueField.REMOVECC, removedCCs);
        storeFieldValue(BugzillaIssue.IssueField.NEWCC, addedCCs);
    }

    private Set<String> ccs(String values) {
        Set<String> ccs = new HashSet<String>();
        StringTokenizer st = new StringTokenizer(values, ", \t\n\r\f"); // NOI18N
        while (st.hasMoreTokens()) {
            ccs.add(st.nextToken());
        }
        return ccs;
    }

    private String getMissingCCs(Set<String> ccs, Set<String> missingIn) {
        StringBuffer ret = new StringBuffer();
        Iterator<String> it = ccs.iterator();
        while(it.hasNext()) {
            String cc = it.next();
            if(cc.trim().equals("")) continue;
            if(!missingIn.contains(cc)) {
                ret.append(cc);
                if(it.hasNext()) {
                    ret.append(',');
                }
            }
        }
        return ret.toString();
    }

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        String refreshMessageFormat = NbBundle.getMessage(IssuePanel.class, "IssuePanel.refreshMessage"); // NOI18N
        String refreshMessage = MessageFormat.format(refreshMessageFormat, issue.getID());
        final ProgressHandle handle = ProgressHandleFactory.createHandle(refreshMessage);
        handle.start();
        handle.switchToIndeterminate();
        skipReload = true;
        enableComponents(false);
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    issue.refresh();
                } finally {
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            enableComponents(true);
                            skipReload = false;
                        }
                    });
                    handle.finish();
                    reloadFormInAWT(true);
                }
            }
        });
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void resolutionComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resolutionComboActionPerformed
        cancelHighlight(resolutionLabel);
        if (resolutionCombo.getParent() == null) {
            return;
        }
        boolean shown = "DUPLICATE".equals(resolutionCombo.getSelectedItem()); // NOI18N
        duplicateLabel.setVisible(shown);
        duplicateField.setVisible(shown);
        duplicateButton.setVisible(shown && duplicateField.isEditable());
    }//GEN-LAST:event_resolutionComboActionPerformed

    private void keywordsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keywordsButtonActionPerformed
        String message = NbBundle.getMessage(IssuePanel.class, "IssuePanel.keywordsButton.message"); // NOI18N
        String kws = BugzillaUtil.getKeywords(message, keywordsField.getText(), issue.getBugzillaRepository());
        keywordsField.setText(kws);
    }//GEN-LAST:event_keywordsButtonActionPerformed

    private void blocksButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_blocksButtonActionPerformed
        Issue newIssue = BugtrackingUtil.selectIssue(NbBundle.getMessage(IssuePanel.class, "IssuePanel.blocksButton.message"), issue.getBugzillaRepository(), this); // NOI18N
        if (newIssue != null) {
            StringBuilder sb = new StringBuilder();
            if (!blocksField.getText().trim().equals("")) { // NOI18N
                sb.append(blocksField.getText()).append(',').append(' ');
            }
            sb.append(newIssue.getID());
            blocksField.setText(sb.toString());
        }
    }//GEN-LAST:event_blocksButtonActionPerformed

    private void dependsOnButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dependsOnButtonActionPerformed
        Issue newIssue = BugtrackingUtil.selectIssue(NbBundle.getMessage(IssuePanel.class, "IssuePanel.dependsOnButton.message"), issue.getBugzillaRepository(), this); // NOI18N
        if (newIssue != null) {
            StringBuilder sb = new StringBuilder();
            if (!dependsField.getText().trim().equals("")) { // NOI18N
                sb.append(dependsField.getText()).append(',').append(' ');
            }
            sb.append(newIssue.getID());
            dependsField.setText(sb.toString());
        }
    }//GEN-LAST:event_dependsOnButtonActionPerformed

    private void componentComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_componentComboActionPerformed
        cancelHighlight(componentLabel);
    }//GEN-LAST:event_componentComboActionPerformed

    private void versionComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_versionComboActionPerformed
        cancelHighlight(versionLabel);
    }//GEN-LAST:event_versionComboActionPerformed

    private void platformComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_platformComboActionPerformed
        cancelHighlight(platformLabel);
    }//GEN-LAST:event_platformComboActionPerformed

    private void priorityComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_priorityComboActionPerformed
        cancelHighlight(priorityLabel);
    }//GEN-LAST:event_priorityComboActionPerformed

    private void severityComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_severityComboActionPerformed
        cancelHighlight(priorityLabel);
    }//GEN-LAST:event_severityComboActionPerformed

    private void targetMilestoneComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_targetMilestoneComboActionPerformed
        cancelHighlight(targetMilestoneLabel);
    }//GEN-LAST:event_targetMilestoneComboActionPerformed

    private void osComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_osComboActionPerformed
        cancelHighlight(platformLabel);
    }//GEN-LAST:event_osComboActionPerformed

    private void reloadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reloadButtonActionPerformed
        String reloadMessage = NbBundle.getMessage(IssuePanel.class, "IssuePanel.reloadMessage"); // NOI18N
        final ProgressHandle handle = ProgressHandleFactory.createHandle(reloadMessage);
        handle.start();
        handle.switchToIndeterminate();
        skipReload = true;
        enableComponents(false);
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                issue.getBugzillaRepository().refreshConfiguration();
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        try {
                            reloading = true;
                            Object product = productCombo.getSelectedItem();
                            Object platform = platformCombo.getSelectedItem();
                            Object os = osCombo.getSelectedItem();
                            Object priority = priorityCombo.getSelectedItem();
                            Object severity = severityCombo.getSelectedItem();
                            Object resolution = resolutionCombo.getSelectedItem();
                            initCombos();
                            selectInCombo(productCombo, product, false);
                            selectInCombo(platformCombo, platform, false);
                            selectInCombo(osCombo, os, false);
                            selectInCombo(priorityCombo, priority, false);
                            selectInCombo(severityCombo, severity, false);
                            initStatusCombo(statusCombo.getSelectedItem().toString());
                            selectInCombo(resolutionCombo, resolution, false);
                        } finally {
                            reloading = false;
                            enableComponents(true);
                            skipReload = false;
                        }
                    }
                });
                handle.finish();
            }
        });
    }//GEN-LAST:event_reloadButtonActionPerformed

    private void duplicateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_duplicateButtonActionPerformed
        Issue newIssue = BugtrackingUtil.selectIssue(NbBundle.getMessage(IssuePanel.class, "IssuePanel.duplicateButton.message"), issue.getBugzillaRepository(), this); // NOI18N
        if (newIssue != null) {
            duplicateField.setText(newIssue.getID());
        }
    }//GEN-LAST:event_duplicateButtonActionPerformed

    private void tasklistButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tasklistButtonActionPerformed
        tasklistButton.setEnabled(false);
        BugzillaIssueProvider provider = BugzillaIssueProvider.getInstance();
        if (provider.isAdded(issue)) {
            provider.remove(issue);
        } else {
            attachTasklistListener(provider);
            provider.add(issue, true);
        }
        updateTasklistButton();
    }//GEN-LAST:event_tasklistButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea addCommentArea;
    private javax.swing.JLabel addCommentLabel;
    private javax.swing.JTextField assignedField;
    private javax.swing.JLabel assignedLabel;
    private javax.swing.JLabel assignedToStatusLabel;
    private javax.swing.JLabel assignedToWarning;
    private javax.swing.JLabel attachmentsLabel;
    private javax.swing.JButton blocksButton;
    private javax.swing.JTextField blocksField;
    private javax.swing.JLabel blocksLabel;
    private javax.swing.JLabel blocksWarning;
    private javax.swing.JButton cancelButton;
    private javax.swing.JTextField ccField;
    private javax.swing.JLabel ccLabel;
    private javax.swing.JLabel ccWarning;
    private javax.swing.JComboBox componentCombo;
    private javax.swing.JLabel componentLabel;
    private javax.swing.JLabel componentWarning;
    private javax.swing.JTextField dependsField;
    private javax.swing.JLabel dependsLabel;
    private javax.swing.JButton dependsOnButton;
    private javax.swing.JLabel dependsOnWarning;
    private javax.swing.JPanel dummyAttachmentsPanel;
    private javax.swing.JPanel dummyCommentsPanel;
    private javax.swing.JLabel dummyLabel1;
    private javax.swing.JLabel dummyLabel2;
    private javax.swing.JLabel dummyLabel3;
    private javax.swing.JButton duplicateButton;
    private javax.swing.JTextField duplicateField;
    private javax.swing.JLabel duplicateLabel;
    private javax.swing.JLabel headerLabel;
    private javax.swing.JComboBox issueTypeCombo;
    private javax.swing.JLabel issueTypeLabel;
    private javax.swing.JButton keywordsButton;
    private javax.swing.JTextField keywordsField;
    private javax.swing.JLabel keywordsLabel;
    private javax.swing.JLabel keywordsWarning;
    private javax.swing.JPanel messagePanel;
    private javax.swing.JLabel milestoneWarning;
    private javax.swing.JTextField modifiedField;
    private javax.swing.JLabel modifiedLabel;
    private javax.swing.JComboBox osCombo;
    private javax.swing.JLabel osWarning;
    private javax.swing.JComboBox platformCombo;
    private javax.swing.JLabel platformLabel;
    private javax.swing.JLabel platformWarning;
    private javax.swing.JComboBox priorityCombo;
    private javax.swing.JLabel priorityLabel;
    private javax.swing.JLabel priorityWarning;
    private javax.swing.JComboBox productCombo;
    private javax.swing.JTextField productField;
    private javax.swing.JLabel productLabel;
    private javax.swing.JLabel productWarning;
    private javax.swing.JTextField qaContactField;
    private javax.swing.JLabel qaContactLabel;
    private javax.swing.JLabel qaContactWarning;
    private org.netbeans.modules.bugtracking.util.LinkButton refreshButton;
    private org.netbeans.modules.bugtracking.util.LinkButton reloadButton;
    private javax.swing.JTextField reportedField;
    private javax.swing.JLabel reportedLabel;
    private javax.swing.JLabel reportedStatusLabel;
    private javax.swing.JComboBox resolutionCombo;
    private javax.swing.JTextField resolutionField;
    private javax.swing.JLabel resolutionLabel;
    private javax.swing.JLabel resolutionWarning;
    private javax.swing.JScrollPane scrollPane1;
    private javax.swing.JSeparator separator;
    private javax.swing.JLabel separatorLabel;
    private javax.swing.JLabel separatorLabel2;
    private javax.swing.JComboBox severityCombo;
    private javax.swing.JLabel severityWarning;
    private javax.swing.JComboBox statusCombo;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JLabel statusWarning;
    private javax.swing.JTextField statusWhiteboardField;
    private javax.swing.JLabel statusWhiteboardLabel;
    private javax.swing.JButton submitButton;
    private javax.swing.JTextField summaryField;
    private javax.swing.JLabel summaryLabel;
    private javax.swing.JLabel summaryWarning;
    private javax.swing.JComboBox targetMilestoneCombo;
    private javax.swing.JLabel targetMilestoneLabel;
    private org.netbeans.modules.bugtracking.util.LinkButton tasklistButton;
    private javax.swing.JTextField urlField;
    private javax.swing.JLabel urlLabel;
    private javax.swing.JLabel urlWarning;
    private javax.swing.JComboBox versionCombo;
    private javax.swing.JLabel versionLabel;
    private javax.swing.JLabel versionWarning;
    // End of variables declaration//GEN-END:variables

    class CancelHighlightDocumentListener implements DocumentListener {
        private JLabel label;
        
        CancelHighlightDocumentListener(JLabel label) {
            this.label = label;
        }

        public void insertUpdate(DocumentEvent e) {
            cancelHighlight(label);
        }

        public void removeUpdate(DocumentEvent e) {
            cancelHighlight(label);
        }

        public void changedUpdate(DocumentEvent e) {
            cancelHighlight(label);
        }
    }

    class CyclicDependencyDocumentListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        public void removeUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        public void changedUpdate(DocumentEvent e) {
            Set<Integer> bugs1 = bugs(blocksField.getText());
            Set<Integer> bugs2 = bugs(dependsField.getText());
            bugs1.retainAll(bugs2);
            if (bugs1.isEmpty()) {
                if (cyclicDependency) {
                    cyclicDependency = false;
                    updateMessagePanel();
                }
            } else {
                if (!cyclicDependency) {
                    cyclicDependency = true;
                    updateMessagePanel();
                }
            }
        }

        private Set<Integer> bugs(String values) {
            Set<Integer> bugs = new HashSet<Integer>();
            StringTokenizer st = new StringTokenizer(values, ", \t\n\r\f"); // NOI18N
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                try {
                    bugs.add(Integer.parseInt(token));
                } catch (NumberFormatException nfex) {}
            }
            return bugs;
        }
    }

    class RevalidatingListener implements DocumentListener, Runnable {
        private boolean ignoreUpdate;

        public void insertUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        public void removeUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        public void changedUpdate(DocumentEvent e) {
            if (ignoreUpdate) return;
            ignoreUpdate = true;
            EventQueue.invokeLater(this);
        }

        public void run() {
            revalidate();
            repaint();
            ignoreUpdate = false;
        }

    }

    private static class PriorityRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel renderer = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            renderer.setIcon(BugzillaConfig.getInstance().getPriorityIcon((String)value));
            return renderer;
        }

    }
}
