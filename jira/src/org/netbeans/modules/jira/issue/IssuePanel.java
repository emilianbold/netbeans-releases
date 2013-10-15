/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jira.issue;

import com.atlassian.connector.eclipse.internal.jira.core.model.IssueType;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraStatus;
import com.atlassian.connector.eclipse.internal.jira.core.model.Priority;
import com.atlassian.connector.eclipse.internal.jira.core.model.Project;
import com.atlassian.connector.eclipse.internal.jira.core.model.Resolution;
import com.atlassian.connector.eclipse.internal.jira.core.model.User;
import com.atlassian.connector.eclipse.internal.jira.core.model.Version;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.eclipse.mylyn.tasks.core.data.TaskOperation;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.LayoutStyle;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DateFormatter;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.issuetable.TableSorter;
import org.netbeans.modules.bugtracking.team.spi.RepositoryUser;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.team.spi.TeamUtil;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.netbeans.modules.bugtracking.util.LinkButton;
import org.netbeans.modules.bugtracking.util.RepositoryUserRenderer;
import org.netbeans.modules.bugtracking.util.UIUtils;
import org.netbeans.modules.bugtracking.util.UndoRedoSupport;
import org.netbeans.modules.jira.Jira;
import org.netbeans.modules.jira.issue.NbJiraIssue.IssueField;
import org.netbeans.modules.jira.kenai.KenaiRepository;
import org.netbeans.modules.jira.repository.JiraConfiguration;
import org.netbeans.modules.jira.repository.JiraRepository.Cache;
import org.netbeans.modules.jira.util.JiraUtils;
import org.netbeans.modules.jira.util.PriorityRenderer;
import org.netbeans.modules.jira.util.ProjectRenderer;
import org.netbeans.modules.jira.util.ResolutionRenderer;
import org.netbeans.modules.jira.util.StatusRenderer;
import org.netbeans.modules.jira.util.TypeRenderer;
import org.netbeans.modules.mylyn.util.AbstractNbTaskWrapper;
import org.netbeans.modules.spellchecker.api.Spellchecker;
import org.openide.awt.HtmlBrowser;
import org.openide.util.ImageUtilities;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Jan Stola
 */
public class IssuePanel extends javax.swing.JPanel implements Scrollable {
    private static final RequestProcessor RP = new RequestProcessor("JIRA Issue Panel", 5, false); // NOI18N
    private static final Color ORIGINAL_ESTIMATE_COLOR = new Color(137, 175, 215);
    private static final Color REMAINING_ESTIMATE_COLOR = new Color(236, 142, 0);
    private static final Color TIME_SPENT_COLOR = new Color(81, 168, 37);
    private static Color highlightColor = null;
    static {
        highlightColor = UIManager.getColor( "nb.bugtracking.label.highlight" ); //NOI18N
        if( null == highlightColor ) {
            highlightColor = new Color(217, 255, 217);
        }
    }
    private NbJiraIssue issue;
    private CommentsPanel commentsPanel;
    private AttachmentsPanel attachmentsPanel;
    private IssueLinksPanel issueLinksPanel;
    private boolean skipReload;
    private boolean reloading;
    private UndoRedoSupport undoRedoSupport;
    private final Set<String> unsavedFields = new HashSet<>();
    private static final String WORKLOG = "WORKLOG"; //NOI18N
    private static final String NEW_ATTACHMENTS = AbstractNbTaskWrapper.NEW_ATTACHMENT_ATTRIBUTE_ID;
    private boolean open;
    private final Map<String, String> fieldsConflict = new LinkedHashMap<>();
    private final Map<String, String> fieldsIncoming = new LinkedHashMap<>();
    private final Map<String, String> fieldsLocal = new LinkedHashMap<>();
    private final TooltipsMap tooltipsConflict = new TooltipsMap();
    private final TooltipsMap tooltipsIncoming = new TooltipsMap();
    private final TooltipsMap tooltipsLocal = new TooltipsMap();
    
    private static final URL ICON_REMOTE_PATH = IssuePanel.class.getClassLoader().getResource("org/netbeans/modules/jira/resources/remote.png"); //NOI18N
    private static final ImageIcon ICON_REMOTE = ImageUtilities.loadImageIcon("org/netbeans/modules/jira/resources/remote.png", true); //NOI18N
    private static final URL ICON_CONFLICT_PATH = IssuePanel.class.getClassLoader().getResource("org/netbeans/modules/jira/resources/conflict.png"); //NOI18N
    private static final ImageIcon ICON_CONFLICT = ImageUtilities.loadImageIcon("org/netbeans/modules/jira/resources/conflict.png", true); //NOI18N
    private static final URL ICON_UNSUBMITTED_PATH = IssuePanel.class.getClassLoader().getResource("org/netbeans/modules/jira/resources/unsubmitted.png"); //NOI18N
    private static final ImageIcon ICON_UNSUBMITTED = ImageUtilities.loadImageIcon("org/netbeans/modules/jira/resources/unsubmitted.png", true); //NOI18N
    
    private static final DateFormatter dueDateFormatter = new javax.swing.text.DateFormatter() {
        @Override
        public Object stringToValue(String text) throws java.text.ParseException {
            if (text == null || text.trim().isEmpty()) {
                return null;
            }
            return super.stringToValue(text);
        }
    };

    public IssuePanel() {
        initComponents();
        updateReadOnlyField(createdField);
        updateReadOnlyField(updatedField);
        updateReadOnlyField(originalEstimateField);
        updateReadOnlyField(remainingEstimateField);
        updateReadOnlyField(timeSpentField);
        updateReadOnlyField(resolutionField);
        updateReadOnlyField(projectField);
        updateReadOnlyField(statusField);
        customFieldPanelLeft.setBackground(getBackground());
        customFieldPanelRight.setBackground(getBackground());
        parentHeaderPanel.setBackground(getBackground());
        UIUtils.fixFocusTraversalKeys(environmentArea);
        UIUtils.fixFocusTraversalKeys(addCommentArea);
        UIUtils.issue163946Hack(componentScrollPane);
        UIUtils.issue163946Hack(affectsVersionScrollPane);
        UIUtils.issue163946Hack(fixVersionScrollPane);
        UIUtils.issue163946Hack(environmentScrollPane);
        UIUtils.issue163946Hack(addCommentScrollPane);
        summaryField.setPreferredSize(summaryField.getMinimumSize());
        initAttachmentsPanel();
        initIssueLinksPanel();
        initSpellChecker();
        initDefaultButton();
        attachFieldStatusListeners();
        attachHideStatusListener();
        if( "Metal".equals( UIManager.getLookAndFeel().getID() ) || "Nimbus".equals( UIManager.getLookAndFeel().getID() ) )
            actionPanel.setBackground( UIUtils.getSectionPanelBackground() );
    }

    private void initDefaultButton() {
        InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "submit"); // NOI18N
        ActionMap actionMap = getActionMap();
        Action submitAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (submitButton.isEnabled()) {
                    submitButtonActionPerformed(null);
                }
            }
        };
        actionMap.put("submit", submitAction); // NOI18N
    }

    private void updateReadOnlyField(JTextField field) {
        if ("GTK".equals(UIManager.getLookAndFeel().getID())) { // NOI18N
            field.setUI(new BasicTextFieldUI());
        }
        Color bkColor = getBackground();
        if( null != bkColor )
            bkColor = new Color( bkColor.getRGB() );
        field.setBackground(bkColor);
    }

    NbJiraIssue getIssue() {
        return issue;
    }
    
    void modelStateChanged (final boolean isDirty, final boolean isModified) {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run () {
                if (!reloading && isDirty) {
                    issue.markUserChange();
                }
                if (!isDirty) {
                    unsavedFields.clear();
                }
                if (enableMap.isEmpty()) {
                    btnSaveChanges.setEnabled(isDirty);
                    cancelButton.setEnabled(isModified);
                } else {
                    enableMap.put(btnSaveChanges, isDirty);
                    enableMap.put(cancelButton, isModified);
                }
            }
        });
    }

    void setIssue(NbJiraIssue issue) {
        if (this.issue == null) {
            attachIssueListener(issue);
        }
        this.issue = issue;
        try {
            reloading = true;
            initRenderers();
            initProjectCombo();
            initPriorityCombo();
            initResolutionCombo();
            initHeaderLabel();
            initCommentsPanel();
            initAssigneeCombo();
        } finally {
            reloading = false;
        }

        setupListeners();
    }

    private void initIssueLinksPanel() {
        issueLinksPanel = new IssueLinksPanel();
        GroupLayout layout = (GroupLayout)getLayout();
        layout.replace(dummyIssueLinksPanel, issueLinksPanel);
        issueLinksLabel.setLabelFor(issueLinksPanel);
    }

    private void initAttachmentsPanel() {
        attachmentsPanel = new AttachmentsPanel(this);
        GroupLayout layout = (GroupLayout)getLayout();
        layout.replace(dummyAttachmentPanel, attachmentsPanel);
        attachmentLabel.setLabelFor(attachmentsPanel);
    }

    private void initRenderers() {
        // Project combo
        projectCombo.setRenderer(new ProjectRenderer());

        // NbJiraIssue type combo
        issueTypeCombo.setRenderer(new TypeRenderer());

        // Priority combo
        priorityCombo.setRenderer(new PriorityRenderer());

        // Component list
        componentList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof com.atlassian.connector.eclipse.internal.jira.core.model.Component) {
                    value = ((com.atlassian.connector.eclipse.internal.jira.core.model.Component)value).getName();
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });

        // Status combo
        statusCombo.setRenderer(new StatusRenderer());

        // Resolution combo
        resolutionCombo.setRenderer(new ResolutionRenderer());
    }

    private void initProjectCombo() {
        Project[] projects = issue.getRepository().getConfiguration().getProjects();
        DefaultComboBoxModel model = new DefaultComboBoxModel(projects);
        model.setSelectedItem(null); // Make sure nothing is pre-selected
        projectCombo.setModel(model);
    }

    private void initPriorityCombo() {
        Priority[] priority = issue.getRepository().getConfiguration().getPriorities();
        DefaultComboBoxModel model = new DefaultComboBoxModel(priority);
        priorityCombo.setModel(model);
    }

    private void initStatusCombo(JiraStatus status) {
        List<JiraStatus> statusList = allowedStatusTransitions(status);
        JiraStatus[] statuses = statusList.toArray(new JiraStatus[statusList.size()]);
        DefaultComboBoxModel model = new DefaultComboBoxModel(statuses);
        statusCombo.setModel(model);
    }

    private void initResolutionCombo() {
        Resolution[] resolution = issue.getRepository().getConfiguration().getResolutions();
        DefaultComboBoxModel model = new DefaultComboBoxModel(resolution);
        resolutionCombo.setModel(model);
    }

    private void initAssigneeCombo() {
        assigneeCombo.setRenderer(new RepositoryUserRenderer());
        RP.post(new Runnable() {
            @Override
            public void run() {
                final Collection<RepositoryUser> users = issue.getRepository().getUsers();
                final DefaultComboBoxModel assignedModel = new DefaultComboBoxModel();
                for (RepositoryUser user: users) {
                    assignedModel.addElement(user);
                }
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        reloading = true;
                        try {
                            Object assignee = (assigneeField.getParent() == null) ? assigneeCombo.getSelectedItem() : assigneeField.getText();
                            if (assignee == null) {
                                assignee = ""; //NOI18N
                            }
                            assigneeCombo.setModel(assignedModel);
                            GroupLayout layout = (GroupLayout)getLayout();
                            if ((assigneeCombo.getParent()==null) != users.isEmpty()) {
                                layout.replace(users.isEmpty() ? assigneeCombo : assigneeField, users.isEmpty() ? assigneeField : assigneeCombo);
                                assigneeLabel.setLabelFor(users.isEmpty() ? assigneeField : assigneeCombo);
                            }
                            if (assigneeField.getParent() == null) {
                                assigneeCombo.setSelectedItem(assignee);
                            } else {
                                assigneeField.setText(assignee.toString());
                            }
                        } finally {
                            reloading = false;
                        }
                    }
                });
            }
        });
    }

    private void initHeaderLabel() {
        Font font = headerLabel.getFont();
        headerLabel.setFont(font.deriveFont((float)(font.getSize()*1.7)));
    }

    private void initCommentsPanel() {
        commentsPanel = new CommentsPanel();
        commentsPanel.setNewCommentHandler(new CommentsPanel.NewCommentHandler() {
            @Override
            public void append(String text) {
                addCommentArea.append(text);
                addCommentArea.requestFocus();
                scrollRectToVisible(addCommentScrollPane.getBounds());
            }
        });
        GroupLayout layout = (GroupLayout)getLayout();
        layout.replace(dummyCommentPanel, commentsPanel);
    }

    private void initSpellChecker () {
        Spellchecker.register(summaryField);
        Spellchecker.register(addCommentArea);
    }

    private Map<String,JLabel> customFieldLabels = new HashMap<>();
    private Map<String,JComponent> customFieldComponents = new HashMap<>();
    private Map<String,JLabel> customFieldWarnings = new HashMap<>();
    private void initCustomFields() {
        customFieldPanelLeft.removeAll();
        customFieldPanelRight.removeAll();
        List<NbJiraIssue.CustomField> supportedFields = getSupportedCustomFields();
        customFieldPanelLeft.setVisible(!supportedFields.isEmpty());
        customFieldPanelRight.setVisible(!supportedFields.isEmpty());
        if (!supportedFields.isEmpty()) {
            GroupLayout labelLayout = new GroupLayout(customFieldPanelLeft);
            customFieldPanelLeft.setLayout(labelLayout);
            GroupLayout fieldLayout = new GroupLayout(customFieldPanelRight);
            customFieldPanelRight.setLayout(fieldLayout);
            GroupLayout.ParallelGroup labelHorizontalGroup = labelLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
            GroupLayout.SequentialGroup labelVerticalGroup = labelLayout.createSequentialGroup();
            GroupLayout.ParallelGroup fieldHorizontalGroup = fieldLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
            GroupLayout.SequentialGroup fieldVerticalGroup = fieldLayout.createSequentialGroup();
            boolean first = true;
            for (NbJiraIssue.CustomField cField : supportedFields) {
                JLabel label = new JLabel(cField.getLabel());
                JLabel warning = new JLabel();
                warning.setMinimumSize(new Dimension(16,16));
                warning.setPreferredSize(new Dimension(16,16));
                warning.setMaximumSize(new Dimension(16,16));
                JTextField field = new JTextField();
                customFieldLabels.put(cField.getId(), label);
                customFieldComponents.put(cField.getId(), field);
                customFieldWarnings.put(cField.getId(), warning);
                label.setLabelFor(field);
                label.setPreferredSize(new Dimension(label.getPreferredSize().width, field.getPreferredSize().height));
                if (!first) {
                    labelVerticalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
                    fieldVerticalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
                }
                GroupLayout.SequentialGroup sGroup = labelLayout.createSequentialGroup();
                sGroup.addComponent(warning);
                sGroup.addGap(5);
                sGroup.addComponent(label);
                labelHorizontalGroup.addGroup(sGroup);
                
                GroupLayout.ParallelGroup pGroup = labelLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
                pGroup.addComponent(warning);
                pGroup.addComponent(label);
                labelVerticalGroup.addGroup(pGroup);

                fieldHorizontalGroup.addComponent(field);
                fieldVerticalGroup.addComponent(field, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
                first = false;
            }
            labelLayout.setHorizontalGroup(labelHorizontalGroup);
            labelLayout.setVerticalGroup(labelVerticalGroup);
            fieldLayout.setHorizontalGroup(fieldHorizontalGroup);
            fieldLayout.setVerticalGroup(fieldVerticalGroup);
        }
        setupCustomFieldListeners();
    }

    private List<NbJiraIssue.CustomField> getSupportedCustomFields() {
        NbJiraIssue.CustomField[] cFields = issue.getCustomFields();
        List<NbJiraIssue.CustomField> supportedFields = new LinkedList<NbJiraIssue.CustomField>();
        for (NbJiraIssue.CustomField cField : cFields) {
            if (isSupportedCustomField(cField)) {
                supportedFields.add(cField);
            }
        }
        return supportedFields;
    }

    private void attachFieldStatusListeners() {
        issueTypeCombo.addActionListener(new CancelHighlightListener(issueTypeLabel));
        statusCombo.addActionListener(new CancelHighlightListener(statusLabel));
        statusCombo.addActionListener(new CancelHighlightListener(resolutionLabel));
        priorityCombo.addActionListener(new CancelHighlightListener(priorityLabel));
        assigneeCombo.addActionListener(new CancelHighlightListener(assigneeLabel));
        dueField.getDocument().addDocumentListener(new CancelHighlightListener(dueLabel));
        assigneeField.getDocument().addDocumentListener(new CancelHighlightListener(assigneeLabel));
        summaryField.getDocument().addDocumentListener(new CancelHighlightListener(summaryLabel));
        environmentArea.getDocument().addDocumentListener(new CancelHighlightListener(environmentLabel));
        componentList.addListSelectionListener(new CancelHighlightListener(componentLabel));
        affectsVersionList.addListSelectionListener(new CancelHighlightListener(affectsVersionLabel));
        fixVersionList.addListSelectionListener(new CancelHighlightListener(fixVersionLabel));
        addCommentArea.getDocument().addDocumentListener(new RevalidatingListener());
        addCommentArea.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                makeCaretVisible(addCommentArea);
            }
        });
    }

    private void attachHideStatusListener() {
        assigneeField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (!reloading) {
                    assigneeStatusLabel.setVisible(false);
                }
            }
        });
    }

    void reloadForm(boolean force) {
        if (skipReload) {
            return;
        }
        enableComponents(true);
        reloading = true;

        boolean isNew = issue.isNew();
        headerLabel.setVisible(!isNew || issue.isSubtask());
        createdLabel.setVisible(!isNew);
        createdField.setVisible(!isNew);
        updatedLabel.setVisible(!isNew);
        updatedField.setVisible(!isNew);
        separator.setVisible(!isNew);
        commentsPanel.setVisible(!isNew);
        attachmentLabel.setVisible(!isNew);
        attachmentsPanel.setVisible(!isNew);
        issueLinksLabel.setVisible(!isNew);
        issueLinksPanel.setVisible(!isNew);
        resolutionCombo.setVisible(!isNew);
        resolutionField.setVisible(!isNew);
        originalEstimateLabel.setVisible(!isNew);
        originalEstimateField.setVisible(!isNew);
        originalEstimatePanel.setVisible(!isNew);
        remainingEstimateLabel.setVisible(!isNew);
        remainingEstimateField.setVisible(!isNew);
        remainingEstimatePanel.setVisible(!isNew);
        timeSpentLabel.setVisible(!isNew);
        timeSpentField.setVisible(!isNew);
        timeSpentPanel.setVisible(!isNew);
        originalEstimateLabelNew.setVisible(isNew);
        originalEstimateFieldNew.setVisible(isNew);
        originalEstimateHint.setVisible(isNew);
        logWorkButton2.setVisible(!isNew);
        subtaskLabel.setVisible(!isNew);
        btnDeleteTask.setVisible(isNew);

        createSubtaskButton.setVisible(false);
        convertToSubtaskButton.setVisible(false);
        dummySubtaskPanel.setVisible(false);
        cancelButton.setEnabled(issue.hasLocalEdits());

        if (force) {
            initCustomFields();
        }

        // Operations
        boolean startProgressAvailable = false;
        boolean stopProgressAvailable = false;
        boolean resolveIssueAvailable = false;
        boolean closeIssueAvailable = false;
        boolean reopenIssueAvailable = false;
        for (TaskOperation operation : issue.getAvailableOperations().values()) {
            String label = operation.getLabel();
            if (JiraUtils.isStartProgressOperation(label)) {
                startProgressAvailable = true;
            } else if (JiraUtils.isStopProgressOperation(label)) {
                stopProgressAvailable = true;
            } else if (JiraUtils.isResolveOperation(label)) {
                resolveIssueAvailable = true;
            } else if (JiraUtils.isCloseOperation(label)) {
                closeIssueAvailable = true;
            } else if (JiraUtils.isReopenOperation(label)) {
                reopenIssueAvailable = true;
            }
        }
        startProgressButton.setVisible(startProgressAvailable);
        stopProgressButton.setVisible(stopProgressAvailable);
        resolveIssueButton.setVisible(resolveIssueAvailable);
        closeIssueButton.setVisible(closeIssueAvailable);
        reopenIssueButton.setVisible(reopenIssueAvailable);

        org.openide.awt.Mnemonics.setLocalizedText(addCommentLabel, NbBundle.getMessage(IssuePanel.class, isNew ? "IssuePanel.description" : "IssuePanel.addCommentLabel.text")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(submitButton, NbBundle.getMessage(IssuePanel.class, isNew ? "IssuePanel.submitButton.text.new" : "IssuePanel.submitButton.text")); // NOI18N
        boolean showProjectCombo = isNew && !issue.isSubtask();
        if (showProjectCombo != (projectCombo.getParent() != null)) {
            GroupLayout layout = (GroupLayout)getLayout();
            layout.replace(showProjectCombo ? projectField : projectCombo, showProjectCombo ? projectCombo : projectField);
        }
        if (isNew != (statusField.getParent() != null)) {
            GroupLayout layout = (GroupLayout)getLayout();
            layout.replace(isNew ? statusCombo : statusField, isNew ? statusField : statusCombo);
        }
        if (isNew != (actionPanel.getParent() == null)) {
            GroupLayout layout = (GroupLayout)getLayout();
            layout.replace(isNew ? actionPanel : dummyActionPanel, isNew ? dummyActionPanel : actionPanel);
        }
        cancelButton.setVisible(!isNew);

        reloadCustomFields();

        final String parentKey = issue.getParentKey();
        boolean hasParent = (parentKey != null) && (parentKey.trim().length() > 0);
        if  (hasParent) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    final NbJiraIssue parent = issue.getRepository().getIssue(parentKey);
                    if(parent == null) {
                        // how could this be possible? parent removed?
                        Jira.LOG.log(Level.INFO, "issue {0} is referencing not available parent with key {1}", new Object[]{issue.getKey(), parentKey}); // NOI18N
                        return;
                    }
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            parentHeaderPanel.setVisible(true);
                            parentHeaderPanel.removeAll();
                            headerLabel.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/jira/resources/subtask.png", true)); // NOI18N
                            GroupLayout layout = new GroupLayout(parentHeaderPanel);
                            JLabel parentLabel = new JLabel();
                            parentLabel.setText(parent.getSummary());
                            LinkButton parentButton = new LinkButton(new AbstractAction() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    JiraUtils.openIssue(parent);
                                }
                            });
                            parentButton.setText(parentKey+':');
                            layout.setHorizontalGroup(
                                layout.createSequentialGroup()
                                    .addComponent(parentButton)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(parentLabel)
                            );
                            layout.setVerticalGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(parentButton)
                                    .addComponent(parentLabel)
                            );
                            parentHeaderPanel.setLayout(layout);
                        }
                    });
                }
            });
        } else {
            parentHeaderPanel.setVisible(false);
            headerLabel.setIcon(null);
        }

        JiraConfiguration config = issue.getRepository().getConfiguration();
        ResourceBundle bundle = NbBundle.getBundle(IssuePanel.class);
        String projectId = issue.getFieldValue(NbJiraIssue.IssueField.PROJECT);
        Project project = config.getProjectById(projectId);
        reloadField(projectCombo, project, NbJiraIssue.IssueField.PROJECT);
        reloadField(projectField, project.getName(), NbJiraIssue.IssueField.PROJECT);
        reloadField(issueTypeCombo, config.getIssueTypeById(issue.getFieldValue(NbJiraIssue.IssueField.TYPE)), NbJiraIssue.IssueField.TYPE);
        reloadField(summaryField, issue.getFieldValue(NbJiraIssue.IssueField.SUMMARY), NbJiraIssue.IssueField.SUMMARY);
        reloadField(priorityCombo, config.getPriorityById(issue.getFieldValue(NbJiraIssue.IssueField.PRIORITY)), NbJiraIssue.IssueField.PRIORITY);
        List<String> componentIds = issue.getFieldValues(NbJiraIssue.IssueField.COMPONENT);
        reloadField(componentList, componentsByIds(projectId, componentIds), NbJiraIssue.IssueField.COMPONENT);
        List<String> affectsVersionIds = issue.getFieldValues(NbJiraIssue.IssueField.AFFECTSVERSIONS);
        reloadField(affectsVersionList, versionsByIds(projectId, affectsVersionIds),NbJiraIssue.IssueField.AFFECTSVERSIONS);
        List<String> fixVersionIds = issue.getFieldValues(NbJiraIssue.IssueField.FIXVERSIONS);
        reloadField(fixVersionList, versionsByIds(projectId, fixVersionIds), NbJiraIssue.IssueField.FIXVERSIONS);
        Resolution resolution = config.getResolutionById(issue.getLastSeenFieldValue(NbJiraIssue.IssueField.RESOLUTION));
        reloadField(resolutionField, (resolution==null) ? "" : resolution.getName(), NbJiraIssue.IssueField.RESOLUTION); // NOI18N
        fixPrefSize(resolutionField);
        reloadField(environmentArea, issue.getFieldValue(NbJiraIssue.IssueField.ENVIRONMENT), NbJiraIssue.IssueField.ENVIRONMENT);
        reloadField(updatedField, JiraUtils.dateByMillis(issue.getFieldValue(NbJiraIssue.IssueField.MODIFICATION), true), NbJiraIssue.IssueField.MODIFICATION);
        fixPrefSize(updatedField);
        reloadField(dueField, JiraUtils.dateByMillis(issue.getFieldValue(NbJiraIssue.IssueField.DUE)), NbJiraIssue.IssueField.DUE);
        String assignee = issue.getFieldValue(NbJiraIssue.IssueField.ASSIGNEE);
        String selectedAssignee = (assigneeField.getParent() == null) ? assigneeCombo.getSelectedItem().toString() : assigneeField.getText();
        boolean isKenaiRepository = (issue.getRepository() instanceof KenaiRepository);
        if (isKenaiRepository && (assignee.trim().length() > 0) && (force || !selectedAssignee.equals(assignee))) {
            String host = ((KenaiRepository) issue.getRepository()).getHost();
            JLabel label = TeamUtil.createUserWidget(issue.getRepository().getUrl(), assignee, host, TeamUtil.getChatLink(issue.getKey()));
            if (label != null) {
                label.setText(null);
                ((GroupLayout)getLayout()).replace(assigneeStatusLabel, label);
                assigneeStatusLabel = label;
            }
        }
        if (force) {
            assigneeStatusLabel.setVisible(assignee.trim().length() > 0);
        }
        reloadField(assigneeField, assignee, NbJiraIssue.IssueField.ASSIGNEE);
        reloadField(assigneeCombo, assignee, NbJiraIssue.IssueField.ASSIGNEE);
        String originalEstimateTxt = issue.getFieldValue(NbJiraIssue.IssueField.INITIAL_ESTIMATE);
        if (isNew && originalEstimateTxt.isEmpty()) {
            originalEstimateTxt = issue.getFieldValue(NbJiraIssue.IssueField.ESTIMATE);
        }
        int originalEstimate = toInt(originalEstimateTxt);
        int daysPerWeek = issue.getRepository().getConfiguration().getWorkDaysPerWeek();
        int hoursPerDay = issue.getRepository().getConfiguration().getWorkHoursPerDay();
            
        if (isNew) {
            statusField.setText(STATUS_OPEN);
            fixPrefSize(statusField);
            if (issue.isSubtask()) {
                headerLabel.setText(NbBundle.getMessage(IssuePanel.class, "IssuePanel.headerLabel.newSubtask")); // NOI18N
            }
            reloadField(originalEstimateFieldNew, JiraUtils.getWorkLogCode(originalEstimate, daysPerWeek, hoursPerDay), NbJiraIssue.IssueField.INITIAL_ESTIMATE);
            fixPrefSize(originalEstimateFieldNew);
            reloadField(addCommentArea, issue.getFieldValue(IssueField.DESCRIPTION), IssueField.DESCRIPTION);
        } else {
            // Header label
            String headerFormat = bundle.getString("IssuePanel.headerLabel.format"); // NOI18N
            String headerTxt = MessageFormat.format(headerFormat, issue.getKey(), issue.getSummary());
            headerLabel.setText(headerTxt);
            Dimension dim = headerLabel.getPreferredSize();
            headerLabel.setMinimumSize(new Dimension(0, dim.height));
            headerLabel.setPreferredSize(new Dimension(0, dim.height));
            // Created field
            String createdFormat = bundle.getString("IssuePanel.createdField.format"); // NOI18N
            String reporter = issue.getFieldValue(NbJiraIssue.IssueField.REPORTER);
            User user = config.getUser(reporter);
            if (user != null) {
                reporter = user.getFullName();
            }
            String creation = JiraUtils.dateByMillis(issue.getFieldValue(NbJiraIssue.IssueField.CREATION), true);
            String createdTxt = MessageFormat.format(createdFormat, creation, reporter);
            createdField.setText(createdTxt);
            fixPrefSize(createdField);
            if ((reporterStatusLabel.getIcon() == null) && isKenaiRepository) {
                String host = ((KenaiRepository) issue.getRepository()).getHost();
                JLabel label = TeamUtil.createUserWidget(issue.getRepository().getUrl(), reporter, host, TeamUtil.getChatLink(issue.getKey()));
                if (label != null) {
                    label.setText(null);
                    ((GroupLayout)getLayout()).replace(reporterStatusLabel, label);
                    reporterStatusLabel = label;
                }
            }
            String status = issue.getRepositoryFieldValue(NbJiraIssue.IssueField.STATUS);
            if (status.isEmpty()) {
                status = issue.getFieldValue(NbJiraIssue.IssueField.STATUS);
            }
            initStatusCombo(config.getStatusById(status));
            reloadField(statusCombo, config.getStatusById(issue.getFieldValue(NbJiraIssue.IssueField.STATUS)), NbJiraIssue.IssueField.STATUS);
            // Work-log
            int remainintEstimate = toInt(issue.getFieldValue(NbJiraIssue.IssueField.ESTIMATE));
            int timeSpent = toInt(issue.getFieldValue(NbJiraIssue.IssueField.ACTUAL));
            if ((originalEstimateTxt.length() == 0) && (remainintEstimate + timeSpent > 0)) {
                // originalEstimate is sometimes empty incorrectly
                originalEstimate = remainintEstimate + timeSpent;
            }
            reloadField(originalEstimateField, JiraUtils.getWorkLogText(originalEstimate, daysPerWeek, hoursPerDay, false), NbJiraIssue.IssueField.INITIAL_ESTIMATE);
            reloadField(remainingEstimateField, JiraUtils.getWorkLogText(remainintEstimate, daysPerWeek, hoursPerDay, true), NbJiraIssue.IssueField.ESTIMATE);
            reloadField(timeSpentField, JiraUtils.getWorkLogText(timeSpent, daysPerWeek, hoursPerDay, false), NbJiraIssue.IssueField.ACTUAL);
            fixPrefSize(originalEstimateField);
            fixPrefSize(remainingEstimateField);
            fixPrefSize(timeSpentField);
            int scale = Math.max(originalEstimate, timeSpent+remainintEstimate);
            Color bgColor = UIManager.getDefaults().getColor("TextArea.background"); // NOI18N
            setupWorkLogPanel(originalEstimatePanel, ORIGINAL_ESTIMATE_COLOR, Color.lightGray, Color.lightGray, originalEstimate, scale-originalEstimate, 0);
            setupWorkLogPanel(remainingEstimatePanel, Color.lightGray, REMAINING_ESTIMATE_COLOR, bgColor, timeSpent, remainintEstimate, scale-timeSpent-remainintEstimate);
            setupWorkLogPanel(timeSpentPanel, TIME_SPENT_COLOR, Color.lightGray, bgColor, timeSpent, remainintEstimate, scale-timeSpent-remainintEstimate);

            // Comments
            commentsPanel.setIssue(issue);
            UIUtils.keepFocusedComponentVisible(commentsPanel, this);
            reloadField(addCommentArea, issue.getFieldValue(IssueField.COMMENT), IssueField.COMMENT);

            // Attachments
            attachmentsPanel.setIssue(issue);
            UIUtils.keepFocusedComponentVisible(attachmentsPanel, this);

            // NbJiraIssue-links
            boolean anyLink = (issue.getLinkedIssues().length != 0);
            issueLinksLabel.setVisible(anyLink);
            issueLinksPanel.setVisible(anyLink);
            issueLinksPanel.setIssue(issue);

            // Sub-tasks
            boolean hasSubtasks = issue.hasSubtasks();
            subtaskLabel.setVisible(hasSubtasks);
            if (subTaskScrollPane != null) {
                subTaskScrollPane.setVisible(hasSubtasks);
            }
            if (hasSubtasks) {
                if (subTaskTable == null) {
                    subTaskTable = new JTable();
                    subTaskTable.setDefaultRenderer(JiraStatus.class, new StatusRenderer());
                    subTaskTable.setDefaultRenderer(Priority.class, new PriorityRenderer());
                    subTaskTable.setDefaultRenderer(IssueType.class, new TypeRenderer());
                    subTaskTable.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (e.getClickCount() == 2) {
                                Point p = e.getPoint();
                                int row = subTaskTable.rowAtPoint(p);
                                TableModel model = subTaskTable.getModel();
                                final String issueKey = (String)model.getValueAt(row,0);
                                RP.post(new Runnable() {
                                    @Override
                                    public void run () {
                                        NbJiraIssue subTask = (NbJiraIssue) issue.getRepository().getIssue(issueKey);
                                        if (subTask != null) {
                                            JiraUtils.openIssue(subTask);
                                        }
                                    }
                                });
                            }
                        }
                    });
                    subTaskScrollPane = new JScrollPane(subTaskTable);
                }
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        final SubtaskTableModel tableModel = new SubtaskTableModel(issue);
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                TableSorter sorter = new TableSorter(tableModel);
                                subTaskTable.setModel(sorter);
                                sorter.setTableHeader(subTaskTable.getTableHeader());

                                // Table height tweaks
                                int height = 0;
                                for(int row=0; row<tableModel.getRowCount(); row++) {
                                    height += subTaskTable.getRowHeight(row);
                                }
                                subTaskTable.setPreferredScrollableViewportSize(new Dimension(
                                        subTaskTable.getPreferredScrollableViewportSize().width,
                                        height
                                ));

                                if (subTaskScrollPane.getParent() == null) {
                                    ((GroupLayout)getLayout()).replace(dummySubtaskPanel, subTaskScrollPane);
                                }
                                revalidate();
                            }
                        });
                    }
                });
            }
        }
        updateFieldStatuses();
        reloading = false;
    }
    private JComponent dummyActionPanel = new JLabel();
    private JTable subTaskTable;
    private JScrollPane subTaskScrollPane;

    private void reloadCustomFields() {
        for (NbJiraIssue.CustomField cField : getSupportedCustomFields()) {
            String type = cField.getType();
            if ("com.atlassian.jira.plugin.labels:labels".equals(type) //NOI18N
                    || "com.atlassian.jira.plugin.system.customfieldtypes:textfield".equals(type)) { //NOI18N
                JTextField field = (JTextField)customFieldComponents.get(cField.getId());
                if (field != null) {
                    field.setText(cField.getValues().get(0));
                }
            }
        }
    }

    private boolean isSupportedCustomField(NbJiraIssue.CustomField field) {
        return "com.atlassian.jira.plugin.labels:labels".equals(field.getType()) //NOI18N
                || "com.atlassian.jira.plugin.system.customfieldtypes:textfield".equals(field.getType()); //NOI18N
    }
    
    private void reloadField (JComponent fieldComponent, Object fieldValue, IssueField field) {
        reloadField(fieldComponent, fieldValue, field, false);
    }
    
    private void reloadField (JComponent fieldComponent, Object fieldValue, IssueField field, boolean force) {
        boolean fieldDirty = unsavedFields.contains(field.getKey());
        if (!fieldDirty || force) {
            if (fieldComponent instanceof JComboBox) {
                ((JComboBox)fieldComponent).setSelectedItem(fieldValue);
            } else if (fieldComponent instanceof JFormattedTextField) {
                ((JFormattedTextField)fieldComponent).setValue(fieldValue);
            } else if (fieldComponent instanceof JTextComponent) {
                ((JTextComponent)fieldComponent).setText(fieldValue.toString());
            } else if (fieldComponent instanceof JList) {
                JList list = (JList)fieldComponent;
                list.clearSelection();
                ListModel model = list.getModel();
                for (Object value : (List)fieldValue) {
                    for (int i=0; i<model.getSize(); i++) {
                        if (model.getElementAt(i).equals(value)) {
                            list.getSelectionModel().addSelectionInterval(i, i);
                        }
                    }
                }
            }
        }
    }
    
    private void updateFieldDecorations (JComponent component, IssueField field, JLabel warningLabel, JComponent fieldLabel) {
        updateFieldDecorations(warningLabel, fieldLabel, fieldName(fieldLabel), Pair.of(field, component));
    }
    
    private void updateFieldDecorations (JLabel warningLabel, JComponent fieldLabel, String fieldName,
            Pair<IssueField, ? extends JComponent>... fields) {
        String newValue = "", lastSeenValue = "", repositoryValue = ""; //NOI18N
        boolean fieldDirty = false;
        boolean valueModifiedByUser = false;
        boolean valueModifiedByServer = false;
        for (Pair<IssueField, ? extends JComponent> p : fields) {
            JComponent component = p.second();
            IssueField field = p.first();
            if (component instanceof JList) {
                newValue += " " + JiraUtils.mergeValues(toReadable(issue.getFieldValues(field), field));
                lastSeenValue += " " + JiraUtils.mergeValues(toReadable(issue.getLastSeenFieldValues(field), field));
                repositoryValue += " " + JiraUtils.mergeValues(toReadable(issue.getRepositoryFieldValues(field), field));
            } else {
                newValue += " " + toReadable(issue.getFieldValue(field), field);
                lastSeenValue += " " + toReadable(issue.getLastSeenFieldValue(field), field);
                repositoryValue += " " + toReadable(issue.getRepositoryFieldValue(field), field);
            }
            fieldDirty |= unsavedFields.contains(field.getKey());
            valueModifiedByUser |= (issue.getFieldStatus(field) & NbJiraIssue.FIELD_STATUS_OUTGOING) != 0;
            valueModifiedByServer |= (issue.getFieldStatus(field) & NbJiraIssue.FIELD_STATUS_MODIFIED) != 0;
        }
        newValue = newValue.substring(1);
        lastSeenValue = lastSeenValue.substring(1);
        repositoryValue = repositoryValue.substring(1);
        String fieldKey = fields[0].first().getKey();
        
        updateFieldDecorations(warningLabel, fieldLabel, fieldName, fieldKey, fieldDirty,
                valueModifiedByUser, valueModifiedByServer, lastSeenValue, repositoryValue, newValue);
    }
        
    @NbBundle.Messages({
        "# {0} - field name", "# {1} - old value", "# {2} - new value", 
        "IssuePanel.fieldModifiedRemotely={0} field was changed in repository from \"{1}\" to \"{2}\"",
        "# {0} - field name", "# {1} - old value", "# {2} - new value", "# {3} - icon path",
        "IssuePanel.fieldModifiedRemotelyTT=<p><img src=\"{3}\">&nbsp;Remote change - {0}</p>"
            + "<table>"
            + "<tr><td style=\"padding-left:10px;\">remote value:</td><td style=\"padding-left:10px;\"><b>{2}</b></td></tr>"
            + "<tr><td style=\"padding-left:10px;\">base value:</td><td style=\"padding-left:10px;\"><b>{1}</b><br></td></tr>"
            + "</table>"
            + "<p>Field {0} was changed in repository from \"<b>{1}</b>\" to \"<b>{2}</b>\".</p>"
            + "<p></p>",
        "# {0} - field name", "# {1} - old value", "# {2} - new value",
        "IssuePanel.fieldModifiedConflict={0} field was changed in repository from \"{1}\" to \"{2}\" "
            + "before you submitted your local changes. "
            + "Local value will be submitted.",
        "# {0} - field name", "# {1} - old value", "# {2} - incoming value", "# {3} - local value", "# {4} - icon path",
        "IssuePanel.fieldModifiedConflictTT=<p><img src=\"{4}\">&nbsp;Conflict - {0}</p>"
            + "<table>"
            + "<tr><td style=\"padding-left:10px;\">incoming value:</td><td style=\"padding-left:10px;\"><b>{2}</b></td></tr>"
            + "<tr><td style=\"padding-left:10px;\">local value:</td><td style=\"padding-left:10px;\"><b>{3}</b></td></tr>"
            + "<tr><td style=\"padding-left:10px;\">base value:</td><td style=\"padding-left:10px;\"><b>{1}</b></td></tr>"
            + "</table>"
            + "<p>Field {0} was changed in repository from \"<b>{1}</b>\" to \"<b>{2}</b>\" "
            + "before you submitted your local changes."
            + "Local value will be submitted.</p>"
            + "<p></p>",
        "# {0} - field name", "# {1} - old value", "# {2} - new value",
        "IssuePanel.fieldModifiedLocally={0} field was changed locally from \"{1}\" to \"{2}\" but not yet submitted.",
        "# {0} - field name", "# {1} - old value", "# {2} - new value", "# {3} - icon path",
        "IssuePanel.fieldModifiedLocallyTT=<p><img src=\"{3}\">&nbsp;Unsubmitted change - {0}</p>"
            + "<table>"
            + "<tr><td style=\"padding-left:10px;\">local value:</td><td style=\"padding-left:10px;\"><b>{2}</b></td></tr>"
            + "<tr><td style=\"padding-left:10px;\">base value:</td><td style=\"padding-left:10px;\"><b>{1}</b><br></td></tr>"
            + "</table>"
            + "<p>Field {0} was changed locally from \"<b>{1}</b>\" to \"<b>{2}</b>\" but not yet submitted.</p>"
            + "<p></p>",
        "IssuePanel.commentAddedLocally=Comment was added but not yet submitted.",
        "# {0} - icon path",
        "IssuePanel.commentAddedLocallyTT=<p><img src=\"{0}\">&nbsp;Unsubmitted change - New Comment</p>"
            + "<p>A new comment was added but not yet submitted.</p>"
    })
    private void updateFieldDecorations (JLabel warningLabel, JComponent fieldLabel, String fieldName,
            String fieldKey, boolean fieldDirty, boolean valueModifiedByUser, boolean valueModifiedByServer,
            String lastSeenValue, String repositoryValue, String newValue) {
        if (warningLabel != null) {
            boolean change = false;
            if (!issue.isNew()) {
                boolean visible = warningLabel.isVisible();
                removeTooltips(warningLabel, fieldKey);
                if (fieldLabel != null && fieldLabel.getFont().isBold()) {
                    fieldLabel.setFont(fieldLabel.getFont().deriveFont(fieldLabel.getFont().getStyle() & ~Font.BOLD));
                }
                if (visible && valueModifiedByServer && (valueModifiedByUser || fieldDirty) && !newValue.equals(repositoryValue)) {
                    String message = Bundle.IssuePanel_fieldModifiedConflict(fieldName, lastSeenValue, repositoryValue);
                    // do not use ||
                    change = fieldsLocal.remove(fieldKey) != null | fieldsIncoming.remove(fieldKey) != null
                            | !message.equals(fieldsConflict.put(fieldKey, message));
                    tooltipsConflict.addTooltip(warningLabel, fieldKey, Bundle.IssuePanel_fieldModifiedConflictTT(
                            fieldName, lastSeenValue, repositoryValue, newValue, ICON_CONFLICT_PATH));
                } else if (visible && valueModifiedByServer) {
                    String message = Bundle.IssuePanel_fieldModifiedRemotely(fieldName, lastSeenValue, repositoryValue);
                    // do not use ||
                    change = fieldsLocal.remove(fieldKey) != null | fieldsConflict.remove(fieldKey) != null
                            | !message.equals(fieldsIncoming.put(fieldKey, message));
                    tooltipsIncoming.addTooltip(warningLabel, fieldKey, Bundle.IssuePanel_fieldModifiedRemotelyTT(
                            fieldName, lastSeenValue, repositoryValue, ICON_REMOTE_PATH));
                } else if (visible && (valueModifiedByUser || fieldDirty) && !newValue.equals(lastSeenValue)) {
                    String message;
                    if (fieldKey.equals(IssueField.COMMENT.getKey())) {
                        message = Bundle.IssuePanel_commentAddedLocally();
                        tooltipsLocal.addTooltip(warningLabel, fieldKey, Bundle.IssuePanel_commentAddedLocallyTT(ICON_UNSUBMITTED_PATH));
                    } else {
                        message = Bundle.IssuePanel_fieldModifiedLocally(fieldName, lastSeenValue, newValue);
                        tooltipsLocal.addTooltip(warningLabel, fieldKey, Bundle.IssuePanel_fieldModifiedLocallyTT(
                                fieldName, lastSeenValue, newValue, ICON_UNSUBMITTED_PATH));
                    }
                    // do not use ||
                    change = fieldsConflict.remove(fieldKey) != null | fieldsIncoming.remove(fieldKey) != null
                            | !message.equals(fieldsLocal.put(fieldKey, message));
                } else {
                    // do not use ||
                    change = fieldsLocal.remove(fieldKey) != null
                            | fieldsConflict.remove(fieldKey) != null
                            | fieldsIncoming.remove(fieldKey) != null;
                }
                updateIcon(warningLabel);
                if (fieldDirty && fieldLabel != null) {
                    fieldLabel.setFont(fieldLabel.getFont().deriveFont(fieldLabel.getFont().getStyle() | Font.BOLD));
                }
            }
            if (change && !reloading) {
//                updateMessagePanel();
            }
        }
    }

    private void storeFieldValue(NbJiraIssue.IssueField field, JComboBox combo) {
        Object value = combo.getSelectedItem();
        if (value != null) {
            String key;
            switch (field) {
                case PROJECT: key = ((Project)value).getId(); break;
                case TYPE: key = ((IssueType)value).getId(); break;
                case STATUS: key = ((JiraStatus)value).getId(); break;
                case RESOLUTION: key = ((Resolution)value).getId(); break;
                case PRIORITY: key = ((Priority)value).getId(); break;
                case ASSIGNEE: key = value.toString(); break;
                default: throw new UnsupportedOperationException();
            }
            storeFieldValue(field, key);
        }
    }

    private void storeFieldValue(NbJiraIssue.IssueField field, JList combo) {
        Object[] values = combo.getSelectedValues();
        List<String> keys = new ArrayList<String>(values.length);
        for (int i=0; i<values.length; i++) {
            switch (field) {
                case COMPONENT: keys.add(((com.atlassian.connector.eclipse.internal.jira.core.model.Component)values[i]).getId()); break;
                case AFFECTSVERSIONS: keys.add(((Version)values[i]).getId()); break;
                case FIXVERSIONS: keys.add(((Version)values[i]).getId()); break;
                default: throw new UnsupportedOperationException();
            }
        }
        storeFieldValue(field, keys);
    }

    private void storeFieldValue(NbJiraIssue.IssueField field, JFormattedTextField formattedField) {
        if (open) {
            Object value = formattedField.getValue();
            storeFieldValue(field, (value == null) ? "" : ((Date)value).getTime()+""); // NOI18N
        }
    }

    private void storeFieldValue(NbJiraIssue.IssueField field, JTextComponent textComponent) {
        storeFieldValue(field, textComponent.getText());
    }

    private void storeFieldValue(NbJiraIssue.IssueField field, String value) {
        if (!issue.getFieldValue(field).equals(value)) {
            unsavedFields.add(field.getKey());
            issue.setFieldValue(field, value);
        }
    }

    private void storeFieldValue(NbJiraIssue.IssueField field, List<String> values) {
        List<String> initValues = issue.getFieldValues(field);
        if (!values.containsAll(initValues) || !initValues.containsAll(values)) {
            unsavedFields.add(field.getKey());
            issue.setFieldValues(field, values);
        }
    }

    private void storeCustomFieldValue(NbJiraIssue.CustomField cField) {
        String type = cField.getType();
        if ("com.atlassian.jira.plugin.labels:labels".equals(type) //NOI18N
                || "com.atlassian.jira.plugin.system.customfieldtypes:textfield".equals(type)) { //NOI18N
            JTextField field = (JTextField)customFieldComponents.get(cField.getId());
            if (field != null) {
                List<String> values = Collections.singletonList(field.getText());
                if (!values.equals(cField.getValues())) {
                    cField.setValues(values);
                    unsavedFields.add(cField.getId());
                    issue.setCustomField(cField);
                }
            }
        }
    }

    private void setupWorkLogPanel(JPanel panel, Color color1,  Color color2, Color color3, int val1, int val2, int val3) {
        panel.setLayout(new GridBagLayout());

        JLabel label1 = new JLabel();
        label1.setOpaque(true);
        label1.setBackground(color1);
        label1.setPreferredSize(new Dimension(0,10));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = val1;
        panel.add(label1, c);

        JLabel label2 = new JLabel();
        label2.setOpaque(true);
        label2.setBackground(color2);
        label2.setPreferredSize(new Dimension(0,10));
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = val2;
        panel.add(label2, c);

        JLabel label3 = new JLabel();
        label3.setOpaque(true);
        label3.setBackground(color3);
        label3.setPreferredSize(new Dimension(0,10));
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = val3;
        panel.add(label3, c);
    }

    private List<com.atlassian.connector.eclipse.internal.jira.core.model.Component> componentsByIds(String projectId, List<String> componentIds) {
        JiraConfiguration config = issue.getRepository().getConfiguration();
        List<com.atlassian.connector.eclipse.internal.jira.core.model.Component> components = new ArrayList<com.atlassian.connector.eclipse.internal.jira.core.model.Component>(componentIds.size());
        for (String id : componentIds) {
            com.atlassian.connector.eclipse.internal.jira.core.model.Component component = config.getComponentById(projectId, id);
            if(component != null) {
                components.add(component);
            }
        }
        return components;
    }

    private List<Version> versionsByIds(String projectId, List<String> versionIds) {
        JiraConfiguration config = issue.getRepository().getConfiguration();
        List<Version> versions = new ArrayList<Version>(versionIds.size());
        for (String id : versionIds) {
            Version version = config.getVersionById(projectId, id);
            if(version != null) {
                versions.add(version);
            }
        }
        return versions;
    }

    private int toInt(String text) {
        if (text.trim().length() > 0) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException nfex) {
                Jira.LOG.log(Level.INFO, nfex.getMessage(), nfex);
            }
        }
        return 0;
    }

    void reloadFormInAWT(final boolean force) {
        if (EventQueue.isDispatchThread()) {
            reloadForm(force);
        } else {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    reloadForm(force);
                }
            });
        }
    }

    PropertyChangeListener cacheListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getSource() != issue) {
                return;
            }
            if (IssueStatusProvider.EVENT_STATUS_CHANGED.equals(evt.getPropertyName())) {
                updateFieldStatuses();
            }
        }
    };

    private void attachIssueListener(final NbJiraIssue issue) {
        issue.removePropertyChangeListener(cacheListener);
        issue.addPropertyChangeListener(cacheListener);
    }

    private static void fixPrefSize(JTextField textField) {
        // The preferred size of JTextField on (Classic) Windows look and feel
        // is one pixel shorter. The following code is a workaround.
        textField.setPreferredSize(null);
        Dimension dim = textField.getPreferredSize();
        Dimension fixedDim = new Dimension(dim.width+1, dim.height);
        textField.setPreferredSize(fixedDim);
    }

    private void updateFieldStatuses() {
        updateFieldStatus(NbJiraIssue.IssueField.PROJECT, projectLabel);
        updateFieldStatus(NbJiraIssue.IssueField.TYPE, issueTypeLabel);
        updateFieldDecorations(issueTypeCombo, IssueField.TYPE, issueTypeWarning, issueTypeLabel);
        updateFieldStatus(NbJiraIssue.IssueField.STATUS, statusLabel);
        updateFieldDecorations(statusCombo, IssueField.STATUS, statusWarning, statusLabel);
        updateFieldStatus(NbJiraIssue.IssueField.RESOLUTION, resolutionLabel);
        updateFieldDecorations(resolutionCombo, IssueField.RESOLUTION, resolutionWarning, resolutionLabel);
        updateFieldStatus(NbJiraIssue.IssueField.PRIORITY, priorityLabel);
        updateFieldDecorations(priorityCombo, IssueField.PRIORITY, priorityWarning, priorityLabel);
        updateFieldStatus(NbJiraIssue.IssueField.DUE, dueLabel);
        updateFieldDecorations(dueField, IssueField.DUE, dueWarning, dueLabel);
        updateFieldStatus(NbJiraIssue.IssueField.ASSIGNEE, assigneeLabel);
        if (assigneeField.getParent() == null) {
            updateFieldDecorations(assigneeCombo, IssueField.ASSIGNEE, assigneeWarning, assigneeLabel);
        } else {
            updateFieldDecorations(assigneeField, IssueField.ASSIGNEE, assigneeWarning, assigneeLabel);
        }
        updateFieldStatus(NbJiraIssue.IssueField.COMPONENT, componentLabel);
        updateFieldDecorations(componentList, IssueField.COMPONENT, componentWarning, componentLabel);
        updateFieldStatus(NbJiraIssue.IssueField.AFFECTSVERSIONS, affectsVersionLabel);
        updateFieldDecorations(affectsVersionList, IssueField.AFFECTSVERSIONS, affectsVersionWarning, affectsVersionLabel);
        updateFieldStatus(NbJiraIssue.IssueField.FIXVERSIONS, fixVersionLabel);
        updateFieldDecorations(fixVersionList, IssueField.FIXVERSIONS, fixVersionWarning, fixVersionLabel);
        updateFieldStatus(NbJiraIssue.IssueField.INITIAL_ESTIMATE, originalEstimateLabel);
        updateFieldDecorations(originalEstimateField, IssueField.INITIAL_ESTIMATE, originalEstimateWarning, originalEstimateLabel);
        updateFieldStatus(NbJiraIssue.IssueField.ESTIMATE, remainingEstimateLabel);
        updateFieldDecorations(remainingEstimateField, IssueField.ESTIMATE, remainingEstimateWarning, remainingEstimateLabel);
        updateFieldStatus(NbJiraIssue.IssueField.ACTUAL, timeSpentLabel);
        updateFieldDecorations(timeSpentField, IssueField.ACTUAL, timeSpentWarning, timeSpentLabel);
        updateFieldStatus(NbJiraIssue.IssueField.SUMMARY, summaryLabel);
        updateFieldDecorations(summaryField, IssueField.SUMMARY, summaryWarning, summaryLabel);
        updateFieldStatus(NbJiraIssue.IssueField.ENVIRONMENT, environmentLabel);
        updateFieldDecorations(environmentArea, IssueField.ENVIRONMENT, environmentWarning, environmentLabel);
        
        updateFieldDecorations(addCommentArea, IssueField.COMMENT, addCommentWarning, addCommentLabel);
        
        updateCustomFieldStatuses();
        updateWorkLogStatus();
        updateAttachmentsStatus();
        repaint();
    }
    
    private void updateCustomFieldStatuses () {
        for (NbJiraIssue.CustomField field : getSupportedCustomFields()) {
            updateCustomFieldDecoration(field);
        }
    }

    private void updateFieldStatus(NbJiraIssue.IssueField field, JLabel label) {
        boolean highlight = !issue.isNew() && (issue.getFieldStatus(field) & NbJiraIssue.FIELD_STATUS_MODIFIED) != 0;
        label.setOpaque(highlight);
        if (highlight) {
            label.setBackground(highlightColor);
        }
        label.repaint();
    }

    private static final String STATUS_OPEN = "Open"; // NOI18N
    private static final String STATUS_IN_PROGRESS = "In Progress"; // NOI18N
    private static final String STATUS_REOPENED = "Reopened"; // NOI18N
    private static final String STATUS_RESOLVED = "Resolved"; // NOI18N
    private static final String STATUS_CLOSED = "Closed"; // NOI18N
    private List<JiraStatus> allowedStatusTransitions(JiraStatus status) {
        // Available operations
        boolean startProgressAvailable = false;
        boolean stopProgressAvailable = false;
        boolean resolveIssueAvailable = false;
        boolean closeIssueAvailable = false;
        boolean reopenIssueAvailable = false;
        for (TaskOperation operation : issue.getAvailableOperations().values()) {
            String label = operation.getLabel();
            if (JiraUtils.isStartProgressOperation(label)) {
                startProgressAvailable = true;
            } else if (JiraUtils.isStopProgressOperation(label)) {
                stopProgressAvailable = true;
            } else if (JiraUtils.isResolveOperation(label)) {
                resolveIssueAvailable = true;
            } else if (JiraUtils.isCloseOperation(label)) {
                closeIssueAvailable = true;
            } else if (JiraUtils.isReopenOperation(label)) {
                reopenIssueAvailable = true;
            }
        }

        String statusName = status.getName();
        List<String> allowedNames = new ArrayList<String>(3);
        allowedNames.add(statusName);
        if (stopProgressAvailable) {
            allowedNames.add(STATUS_OPEN);
        }
        if (startProgressAvailable) {
            allowedNames.add(STATUS_IN_PROGRESS);
        }
        if (reopenIssueAvailable) {
            allowedNames.add(STATUS_REOPENED);
        }
        if (resolveIssueAvailable) {
            allowedNames.add(STATUS_RESOLVED);
        }
        if (closeIssueAvailable) {
            allowedNames.add(STATUS_CLOSED);
        }

        List<JiraStatus> allowedStatuses = new ArrayList<JiraStatus>(allowedNames.size());
        for (JiraStatus s : issue.getRepository().getConfiguration().getStatuses()) {
            if (allowedNames.contains(s.getName())) {
                allowedStatuses.add(s);
            }
        }
        return allowedStatuses;
    }

    private void submitChange(final Runnable change, String progressMessage)  {
        final ProgressHandle handle = ProgressHandleFactory.createHandle(progressMessage);
        handle.start();
        handle.switchToIndeterminate();
        skipReload = true;
        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    change.run();
                } finally {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            skipReload = false;
                        }
                    });
                    handle.finish();
                    reloadFormInAWT(true);
                }
            }
        });
    }

    private void cancelHighlight(JLabel label) {
        if (!reloading) {
            label.setOpaque(false);
            label.getParent().repaint();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        resolutionField = new javax.swing.JTextField();
        projectField = new javax.swing.JTextField();
        statusField = new javax.swing.JTextField();
        assigneeCombo = new javax.swing.JComboBox();
        dummyIssueLinksPanel = new javax.swing.JPanel();
        parentHeaderPanel = new javax.swing.JPanel();
        customFieldPanelLeft = new javax.swing.JPanel();
        customFieldPanelRight = new javax.swing.JPanel();
        headerLabel = new javax.swing.JLabel();
        projectLabel = new javax.swing.JLabel();
        projectCombo = new javax.swing.JComboBox();
        issueTypeLabel = new javax.swing.JLabel();
        issueTypeCombo = new javax.swing.JComboBox();
        statusLabel = new javax.swing.JLabel();
        statusCombo = new javax.swing.JComboBox();
        resolutionLabel = new javax.swing.JLabel();
        resolutionCombo = new javax.swing.JComboBox();
        priorityLabel = new javax.swing.JLabel();
        priorityCombo = new javax.swing.JComboBox();
        createdLabel = new javax.swing.JLabel();
        createdField = new javax.swing.JTextField();
        updatedLabel = new javax.swing.JLabel();
        updatedField = new javax.swing.JTextField();
        dueLabel = new javax.swing.JLabel();
        dueField = new javax.swing.JFormattedTextField();
        assigneeLabel = new javax.swing.JLabel();
        assigneeField = new javax.swing.JTextField();
        componentLabel = new javax.swing.JLabel();
        componentScrollPane = new javax.swing.JScrollPane();
        componentList = new javax.swing.JList();
        affectsVersionLabel = new javax.swing.JLabel();
        affectsVersionScrollPane = new javax.swing.JScrollPane();
        affectsVersionList = new javax.swing.JList();
        fixVersionLabel = new javax.swing.JLabel();
        fixVersionScrollPane = new javax.swing.JScrollPane();
        fixVersionList = new javax.swing.JList();
        originalEstimateLabel = new javax.swing.JLabel();
        originalEstimateField = new javax.swing.JTextField();
        remainingEstimateLabel = new javax.swing.JLabel();
        remainingEstimateField = new javax.swing.JTextField();
        timeSpentLabel = new javax.swing.JLabel();
        timeSpentField = new javax.swing.JTextField();
        attachmentLabel = new javax.swing.JLabel();
        subtaskLabel = new javax.swing.JLabel();
        summaryLabel = new javax.swing.JLabel();
        summaryField = new javax.swing.JTextField();
        environmentLabel = new javax.swing.JLabel();
        environmentScrollPane = new javax.swing.JScrollPane();
        environmentArea = new javax.swing.JTextArea();
        addCommentLabel = new javax.swing.JLabel();
        addCommentScrollPane = new javax.swing.JScrollPane();
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
                Insets insets = getInsets();
                int prefHeight = 5 * getRowHeight() + insets.top + insets.bottom;
                dim = new Dimension(dim.width, delta + ((dim.height < prefHeight) ? prefHeight : dim.height));
                return dim;
            }
        };
        separator = new javax.swing.JSeparator();
        submitButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        dummyCommentPanel = new javax.swing.JPanel();
        dummySubtaskPanel = new javax.swing.JPanel();
        dummyAttachmentPanel = new javax.swing.JPanel();
        actionPanel = new javax.swing.JPanel();
        actionLabel = new javax.swing.JLabel();
        startProgressButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        stopProgressButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        resolveIssueButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        closeIssueButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        createSubtaskButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        convertToSubtaskButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        refreshButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        reopenIssueButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        addToCategoryButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        showInBrowserButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        originalEstimatePanel = new javax.swing.JPanel();
        remainingEstimatePanel = new javax.swing.JPanel();
        timeSpentPanel = new javax.swing.JPanel();
        originalEstimateFieldNew = new javax.swing.JTextField();
        originalEstimateLabelNew = new javax.swing.JLabel();
        originalEstimateHint = new javax.swing.JLabel();
        logWorkButton2 = new org.netbeans.modules.bugtracking.util.LinkButton();
        reporterStatusLabel = new javax.swing.JLabel();
        assigneeStatusLabel = new javax.swing.JLabel();
        issueLinksLabel = new javax.swing.JLabel();
        btnSaveChanges = new javax.swing.JButton();
        btnDeleteTask = new javax.swing.JButton();
        projectWarning = new javax.swing.JLabel();
        issueTypeWarning = new javax.swing.JLabel();
        componentWarning = new javax.swing.JLabel();
        affectsVersionWarning = new javax.swing.JLabel();
        fixVersionWarning = new javax.swing.JLabel();
        statusWarning = new javax.swing.JLabel();
        resolutionWarning = new javax.swing.JLabel();
        priorityWarning = new javax.swing.JLabel();
        dueWarning = new javax.swing.JLabel();
        assigneeWarning = new javax.swing.JLabel();
        originalEstimateWarning = new javax.swing.JLabel();
        remainingEstimateWarning = new javax.swing.JLabel();
        timeSpentWarning = new javax.swing.JLabel();
        summaryWarning = new javax.swing.JLabel();
        environmentWarning = new javax.swing.JLabel();
        addCommentWarning = new javax.swing.JLabel();
        originalEstimateNewWarning = new javax.swing.JLabel();
        workLogWarning = new javax.swing.JLabel();
        attachmentsWarning = new javax.swing.JLabel();

        resolutionField.setEditable(false);
        resolutionField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        projectField.setEditable(false);
        projectField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        statusField.setEditable(false);
        statusField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        assigneeCombo.setEditable(true);
        assigneeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                assigneeComboActionPerformed(evt);
            }
        });

        setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        projectLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.projectLabel.text")); // NOI18N

        projectCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectComboActionPerformed(evt);
            }
        });

        issueTypeLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.issueTypeLabel.text")); // NOI18N

        statusLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.statusLabel.text")); // NOI18N

        statusCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statusComboActionPerformed(evt);
            }
        });

        resolutionLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.resolutionLabel.text")); // NOI18N

        priorityLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.priorityLabel.text")); // NOI18N

        createdLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.createdLabel.text")); // NOI18N

        createdField.setEditable(false);
        createdField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        updatedLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.updatedLabel.text")); // NOI18N

        updatedField.setEditable(false);
        updatedField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        dueLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.dueLabel.text")); // NOI18N

        dueField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(dueDateFormatter));

        assigneeLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.assigneeLabel.text")); // NOI18N

        assigneeField.setColumns(15);

        componentLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.componentLabel.text")); // NOI18N

        componentList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Dummy" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        componentScrollPane.setViewportView(componentList);

        affectsVersionLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.affectsVersionLabel.text")); // NOI18N

        affectsVersionList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Dummy" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        affectsVersionScrollPane.setViewportView(affectsVersionList);

        fixVersionLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.fixVersionLabel.text")); // NOI18N

        fixVersionList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Dummy" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        fixVersionScrollPane.setViewportView(fixVersionList);

        originalEstimateLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.originalEstimateLabel.text")); // NOI18N

        originalEstimateField.setEditable(false);
        originalEstimateField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        remainingEstimateLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.remainingEstimateLabel.text")); // NOI18N

        remainingEstimateField.setEditable(false);
        remainingEstimateField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        timeSpentLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.timeSpentLabel.text")); // NOI18N

        timeSpentField.setEditable(false);
        timeSpentField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        attachmentLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.attachmentLabel.text")); // NOI18N

        subtaskLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.subtaskLabel.text")); // NOI18N

        summaryLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.summaryLabel.text")); // NOI18N

        environmentLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.environmentLabel.text")); // NOI18N

        environmentArea.setLineWrap(true);
        environmentArea.setRows(5);
        environmentScrollPane.setViewportView(environmentArea);

        addCommentLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.addCommentLabel.text")); // NOI18N

        addCommentScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        addCommentArea.setLineWrap(true);
        addCommentScrollPane.setViewportView(addCommentArea);

        submitButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.submitButton.text")); // NOI18N
        submitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitButtonActionPerformed(evt);
            }
        });

        cancelButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        dummySubtaskPanel.setOpaque(false);

        dummyAttachmentPanel.setOpaque(false);

        actionPanel.setBackground(new java.awt.Color(233, 236, 245));

        actionLabel.setFont(actionLabel.getFont().deriveFont(actionLabel.getFont().getStyle() | java.awt.Font.BOLD));
        actionLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.actionLabel.text")); // NOI18N

        startProgressButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.startProgressButton.text")); // NOI18N
        startProgressButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startProgressButtonActionPerformed(evt);
            }
        });

        stopProgressButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.stopProgressButton.text")); // NOI18N
        stopProgressButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopProgressButtonActionPerformed(evt);
            }
        });

        resolveIssueButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.resolveIssueButton.text")); // NOI18N
        resolveIssueButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resolveIssueButtonActionPerformed(evt);
            }
        });

        closeIssueButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.closeIssueButton.text")); // NOI18N
        closeIssueButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeIssueButtonActionPerformed(evt);
            }
        });

        createSubtaskButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.createSubtaskButton.text")); // NOI18N
        createSubtaskButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createSubtaskButtonActionPerformed(evt);
            }
        });

        convertToSubtaskButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.convertToSubtaskButton.text")); // NOI18N

        refreshButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.refreshButton.text")); // NOI18N
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        reopenIssueButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.reopenIssueButton.text")); // NOI18N
        reopenIssueButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reopenIssueButtonActionPerformed(evt);
            }
        });

        addToCategoryButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.tasklistButton.add")); // NOI18N
        addToCategoryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addToCategoryButtonActionPerformed(evt);
            }
        });

        showInBrowserButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.showInBrowserButton.text")); // NOI18N
        showInBrowserButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showInBrowserButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout actionPanelLayout = new javax.swing.GroupLayout(actionPanel);
        actionPanel.setLayout(actionPanelLayout);
        actionPanelLayout.setHorizontalGroup(
            actionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(actionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(actionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(actionLabel)
                    .addComponent(startProgressButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(resolveIssueButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(createSubtaskButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(convertToSubtaskButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(stopProgressButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(closeIssueButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reopenIssueButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addToCategoryButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(showInBrowserButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(13, Short.MAX_VALUE))
        );
        actionPanelLayout.setVerticalGroup(
            actionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(actionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(actionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(startProgressButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(stopProgressButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(reopenIssueButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resolveIssueButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(closeIssueButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(createSubtaskButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(convertToSubtaskButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addToCategoryButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(showInBrowserButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        originalEstimatePanel.setOpaque(false);

        remainingEstimatePanel.setOpaque(false);

        timeSpentPanel.setOpaque(false);

        originalEstimateFieldNew.setColumns(15);

        originalEstimateLabelNew.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.originalEstimateLabelNew.text")); // NOI18N

        originalEstimateHint.setFont(originalEstimateHint.getFont().deriveFont(originalEstimateHint.getFont().getSize()-2f));
        originalEstimateHint.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.originalEstimateHint.text")); // NOI18N

        logWorkButton2.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.logWorkButton2.text")); // NOI18N
        logWorkButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logWorkButtonActionPerformed(evt);
            }
        });

        issueLinksLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.issueLinksLabel.text")); // NOI18N

        btnSaveChanges.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.btnSaveChanges.text")); // NOI18N
        btnSaveChanges.setToolTipText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.btnSaveChanges.TTtext")); // NOI18N
        btnSaveChanges.setEnabled(false);
        btnSaveChanges.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveChangesActionPerformed(evt);
            }
        });

        btnDeleteTask.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.btnDeleteTask.text")); // NOI18N
        btnDeleteTask.setToolTipText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.btnDeleteTask.TTtext")); // NOI18N
        btnDeleteTask.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteTaskActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(separator)
            .addComponent(dummyCommentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(workLogWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(logWorkButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(remainingEstimateWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(timeSpentWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(originalEstimateWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(5, 5, 5)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(timeSpentLabel)
                            .addComponent(remainingEstimateLabel)
                            .addComponent(originalEstimateLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(timeSpentField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(timeSpentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(remainingEstimateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(remainingEstimatePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(originalEstimateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(originalEstimatePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(headerLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(parentHeaderPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(projectWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(issueTypeWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(statusWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(resolutionWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(priorityWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(5, 5, 5)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(priorityLabel)
                                            .addComponent(resolutionLabel)
                                            .addComponent(statusLabel)
                                            .addComponent(issueTypeLabel)
                                            .addComponent(projectLabel))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(priorityCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(projectCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(issueTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(statusCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(resolutionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(18, 18, 18)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(dueWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(assigneeWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(5, 5, 5)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(createdLabel)
                                                    .addComponent(updatedLabel)
                                                    .addComponent(dueLabel)
                                                    .addComponent(assigneeLabel))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(layout.createSequentialGroup()
                                                        .addComponent(assigneeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(assigneeStatusLabel))
                                                    .addComponent(updatedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addGroup(layout.createSequentialGroup()
                                                        .addComponent(createdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(reporterStatusLabel))
                                                    .addComponent(dueField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(componentWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(5, 5, 5)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(componentScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(componentLabel))
                                        .addGap(18, 18, 18)
                                        .addComponent(affectsVersionWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(5, 5, 5)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(affectsVersionScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(affectsVersionLabel))
                                        .addGap(18, 18, 18)
                                        .addComponent(fixVersionWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(5, 5, 5)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(fixVersionScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(fixVersionLabel))))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(actionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(summaryWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(environmentWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(addCommentWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(originalEstimateNewWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(5, 5, 5))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(attachmentsWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(5, 5, 5)))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(originalEstimateLabelNew, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(addCommentLabel)
                                    .addComponent(environmentLabel)
                                    .addComponent(summaryLabel)
                                    .addComponent(subtaskLabel)
                                    .addComponent(attachmentLabel)
                                    .addComponent(issueLinksLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(customFieldPanelLeft, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(79, 79, 79)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(environmentScrollPane)
                            .addComponent(summaryField)
                            .addComponent(addCommentScrollPane)
                            .addComponent(dummyAttachmentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(dummySubtaskPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(originalEstimateHint, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(dummyIssueLinksPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(originalEstimateFieldNew, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(submitButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnSaveChanges)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cancelButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnDeleteTask)))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(customFieldPanelRight, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {issueTypeCombo, priorityCombo, projectCombo, resolutionCombo, statusCombo});

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {affectsVersionScrollPane, componentScrollPane, fixVersionScrollPane});

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cancelButton, submitButton});

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {originalEstimateField, remainingEstimateField, timeSpentField});

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {assigneeField, dueField});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(parentHeaderPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addComponent(headerLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(projectLabel)
                            .addComponent(projectCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(createdLabel)
                            .addComponent(createdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(reporterStatusLabel)
                            .addComponent(projectWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(issueTypeWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(issueTypeLabel)
                            .addComponent(issueTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(updatedLabel)
                            .addComponent(updatedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(statusWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(statusLabel)
                            .addComponent(statusCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dueWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dueLabel)
                            .addComponent(dueField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(resolutionWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(resolutionLabel)
                            .addComponent(resolutionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(assigneeWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(assigneeLabel)
                            .addComponent(assigneeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(priorityWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(priorityLabel)
                            .addComponent(priorityCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(componentLabel)
                            .addComponent(componentWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(affectsVersionWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(affectsVersionLabel)
                            .addComponent(fixVersionWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(fixVersionLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(componentScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(affectsVersionScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(fixVersionScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(actionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(103, 103, 103)
                        .addComponent(assigneeStatusLabel)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(originalEstimateWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(originalEstimateLabel)
                    .addComponent(originalEstimatePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(originalEstimateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(remainingEstimateWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(remainingEstimateLabel)
                    .addComponent(remainingEstimatePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(remainingEstimateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(timeSpentWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(timeSpentLabel)
                    .addComponent(timeSpentPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(timeSpentField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(workLogWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(logWorkButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dummyIssueLinksPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(issueLinksLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(attachmentLabel)
                    .addComponent(dummyAttachmentPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(attachmentsWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(subtaskLabel)
                    .addComponent(dummySubtaskPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(summaryWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(summaryLabel)
                    .addComponent(summaryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(environmentLabel)
                    .addComponent(environmentScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(environmentWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(customFieldPanelLeft, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(addCommentScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(addCommentLabel)
                            .addComponent(addCommentWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(originalEstimateFieldNew, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(originalEstimateLabelNew))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(originalEstimateHint, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(submitButton)
                                    .addComponent(cancelButton)
                                    .addComponent(btnSaveChanges)
                                    .addComponent(btnDeleteTask)))
                            .addComponent(originalEstimateNewWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(separator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dummyCommentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(customFieldPanelRight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {originalEstimateField, originalEstimatePanel});

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {timeSpentField, timeSpentPanel});

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {remainingEstimateField, remainingEstimatePanel});

    }// </editor-fold>//GEN-END:initComponents

    private void projectComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectComboActionPerformed
        Object value = projectCombo.getSelectedItem();
        assert value instanceof Project;
        if (!(value instanceof Project)) return;
        final Project cachedProject = (Project)value;
        
        String msgPattern = NbBundle.getMessage(IssuePanel.class, "IssuePanel.projectMetaData"); // NOI18N
        String msg = MessageFormat.format(msgPattern, cachedProject.getName());
        final boolean wasReloading = reloading;
        final ProgressHandle handle = ProgressHandleFactory.createHandle(msg);
        handle.start();
        handle.switchToIndeterminate();
        enableComponents(false);
        RP.post(new Runnable() {
            @Override
            public void run() {

                final Project project;
                // The project meta-data may not be initialized.
                // Their intialization must be performed outside event-dispatch thread
                try {
                    JiraConfiguration config = issue.getRepository().getConfiguration();
                    project = config.getProjectById(cachedProject.getId()); // lets make sure we hold the rigth instance
                    config.ensureProjectLoaded(project);
                    config.ensureIssueTypes(project);
                } finally {
                    handle.finish();
                }

                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run () {
                        boolean oldReloading = reloading;
                        reloading = wasReloading;

                        // --- Reload dependent combos
                        JiraConfiguration config =  issue.getRepository().getConfiguration();
                        boolean subtask = issue.isSubtask();
                        boolean anySubtaskType = false;
                        IssueType[] issueTypes = config.getIssueTypes(project);
                        List<IssueType> types = new ArrayList<IssueType>(issueTypes.length);
                        for (IssueType issueType : issueTypes) {
                            if (issueType.isSubTaskType() == subtask) {
                                types.add(issueType);
                            }
                            anySubtaskType |= issueType.isSubTaskType();
                        }
                        issueTypeCombo.setModel(new DefaultComboBoxModel(types.toArray(new IssueType[types.size()])));
                        reloadField(issueTypeCombo, config.getIssueTypeById(issue.getFieldValue(NbJiraIssue.IssueField.TYPE)), NbJiraIssue.IssueField.TYPE, true);
                        storeFieldValue(IssueField.TYPE, issueTypeCombo);
                        createSubtaskButton.setVisible(!subtask && anySubtaskType);

                        // Reload components
                        DefaultListModel componentModel = new DefaultListModel();
                        for (com.atlassian.connector.eclipse.internal.jira.core.model.Component component : config.getComponents(project)) {
                            componentModel.addElement(component);
                        }
                        componentList.setModel(componentModel);
                        List<String> componentIds = issue.getFieldValues(NbJiraIssue.IssueField.COMPONENT);
                        reloadField(componentList, componentsByIds(project.getId(), componentIds), NbJiraIssue.IssueField.COMPONENT, true);
                        storeFieldValue(IssueField.COMPONENT, componentList);

                        // Reload versions
                        DefaultListModel versionModel = new DefaultListModel();
                        for (Version version : config.getVersions(project)) {
                            versionModel.addElement(version);
                        }
                        affectsVersionList.setModel(versionModel);
                        fixVersionList.setModel(versionModel);
                        List<String> affectsVersionIds = issue.getFieldValues(NbJiraIssue.IssueField.AFFECTSVERSIONS);
                        reloadField(affectsVersionList, versionsByIds(project.getId(), affectsVersionIds),NbJiraIssue.IssueField.AFFECTSVERSIONS, true);
                        storeFieldValue(IssueField.AFFECTSVERSIONS, affectsVersionList);
                        List<String> fixVersionIds = issue.getFieldValues(NbJiraIssue.IssueField.FIXVERSIONS);
                        reloadField(fixVersionList, versionsByIds(project.getId(), fixVersionIds), NbJiraIssue.IssueField.FIXVERSIONS, true);
                        storeFieldValue(IssueField.FIXVERSIONS, fixVersionList);

                        reloading = oldReloading;
                        enableComponents(true);
                    }
                });
            }
        });
    }//GEN-LAST:event_projectComboActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        String refreshMessageFormat = NbBundle.getMessage(IssuePanel.class, "IssuePanel.refreshMessage"); // NOI18N
        String refreshMessage = MessageFormat.format(refreshMessageFormat, issue.getKey());
        final ProgressHandle handle = ProgressHandleFactory.createHandle(refreshMessage);
        handle.start();
        handle.switchToIndeterminate();
        skipReload = true;
        enableComponents(false);
        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Cache cache = issue.getRepository().getIssueCache();
                    String parentKey = issue.getParentKey();
                    if ((parentKey != null) && (parentKey.trim().length()>0)) {
                        NbJiraIssue parentIssue = cache.getIssue(parentKey);
                        if (parentIssue != null) {
                            parentIssue.refresh();
                        }
                    }
                    for (String subTaskKey : issue.getSubtaskKeys()) {
                        NbJiraIssue subTask = cache.getIssue(subTaskKey);
                        if (subTask != null) {
                            subTask.refresh();
                        }
                    }
                    issue.refresh();
                } finally {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
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

    @NbBundle.Messages({
        "LBL_IssuePanel.cancelChanges.title=Cancel Local Edits?",
        "MSG_IssuePanel.cancelChanges.message=Do you want to cancel all your local changes to this task?"
    })
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(),
                Bundle.MSG_IssuePanel_cancelChanges_message(),
                Bundle.LBL_IssuePanel_cancelChanges_title(), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
            return;
        }
        skipReload = true;
        enableComponents(false);
        RP.post(new Runnable() {
            @Override
            public void run() {
                boolean cleared = false;
                try {
                    cleared = issue.discardLocalEdits();
                } finally {
                    final boolean fCleared = cleared;
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            unsavedFields.clear();
                            enableComponents(true);
                            btnSaveChanges.setEnabled(!fCleared);
                            cancelButton.setEnabled(!fCleared);                            
                            skipReload = false;
                            reloadForm(true);
                        }
                    });
                }
            }
        });
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void submitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitButtonActionPerformed
        boolean isNew = issue.isNew();
        String submitMessage;
        if (isNew) {
            submitMessage = NbBundle.getMessage(IssuePanel.class, "IssuePanel.submitNewMessage"); // NOI18N
        } else {
            String submitMessageFormat = NbBundle.getMessage(IssuePanel.class, "IssuePanel.submitMessage"); // NOI18N
            submitMessage = MessageFormat.format(submitMessageFormat, issue.getKey());
        }
        final ProgressHandle handle = ProgressHandleFactory.createHandle(submitMessage);
        handle.start();
        handle.switchToIndeterminate();
        skipReload = true;
        enableComponents(false);
        RP.post(new Runnable() {
            @Override
            public void run() {
                boolean ret = false;
                boolean wasNew = issue.isNew();
                try {
                    ret = issue.submitAndRefresh();
                } finally {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            enableComponents(true);
                            skipReload = false;
                        }
                    });
                    handle.finish();
                    if(ret) {
                        reloadFormInAWT(true);
                        if (wasNew && (issue.getParentKey() != null) && (issue.getParentKey().trim().length() > 0)) {
                            NbJiraIssue parent = issue.getRepository().getIssue(issue.getParentKey());
                            parent.refresh();
                        }
                    }
                }
            }
        });
    }//GEN-LAST:event_submitButtonActionPerformed

    private void statusComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statusComboActionPerformed
        Object selection = statusCombo.getSelectedItem();
        if (!(selection instanceof JiraStatus)) {
            return;
        }
        String selectedStatus = ((JiraStatus)selection).getName();
        String irs = issue.getLastSeenFieldValue(NbJiraIssue.IssueField.RESOLUTION);
        if (irs == null) {
            irs = issue.getFieldValue(NbJiraIssue.IssueField.RESOLUTION);
        }
        Object ir = issue.getRepository().getConfiguration().getResolutionById(irs);
        boolean initiallyWithResolution = (ir != null) && (ir.toString().trim().length() > 0);
        boolean nowWithResolution = STATUS_CLOSED.equals(selectedStatus) || STATUS_RESOLVED.equals(selectedStatus);
        boolean showCombo = !initiallyWithResolution && nowWithResolution;
        boolean showField = initiallyWithResolution && nowWithResolution;
        GroupLayout layout = (GroupLayout)getLayout();
        if (showCombo && (resolutionCombo.getParent() == null)) {
            layout.replace(resolutionField, resolutionCombo);
        }
        if (showField && (resolutionField.getParent() == null)) {
            layout.replace(resolutionCombo, resolutionField);
        }
        resolutionCombo.setVisible(nowWithResolution);
        resolutionField.setVisible(nowWithResolution);
        revalidate();
    }//GEN-LAST:event_statusComboActionPerformed

    private void startProgressButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startProgressButtonActionPerformed
        String pattern = NbBundle.getMessage(IssuePanel.class, "IssuePanel.startProgressMessage"); // NOI18N
        String message = MessageFormat.format(pattern, issue.getKey());
        submitChange(new Runnable() {
            @Override
            public void run() {
                issue.startProgress();
                issue.submitAndRefresh();
            }
        }, message);
    }//GEN-LAST:event_startProgressButtonActionPerformed

    private void stopProgressButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopProgressButtonActionPerformed
        String pattern = NbBundle.getMessage(IssuePanel.class, "IssuePanel.stopProgressMessage"); // NOI18N
        String message = MessageFormat.format(pattern, issue.getKey());
        submitChange(new Runnable() {
            @Override
            public void run() {
                issue.stopProgress();
                issue.submitAndRefresh();
            }
        }, message);
    }//GEN-LAST:event_stopProgressButtonActionPerformed

    private void resolveIssueButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resolveIssueButtonActionPerformed
        ResolveIssuePanel panel = new ResolveIssuePanel(issue);
        String title = NbBundle.getMessage(IssuePanel.class, "IssuePanel.resolveIssueButton.text"); // NOI18N
        if (BugtrackingUtil.show(panel, title, title)) {
            String pattern = NbBundle.getMessage(IssuePanel.class, "IssuePanel.resolveIssueMessage"); // NOI18N
            String message = MessageFormat.format(pattern, issue.getKey());
            final Resolution resolution = panel.getSelectedResolution();
            final String comment = panel.getComment();
            submitChange(new Runnable() {
                @Override
                public void run() {
                    issue.resolve(resolution, comment);
                    issue.submitAndRefresh();
                }
            }, message);
        }
    }//GEN-LAST:event_resolveIssueButtonActionPerformed

    private void closeIssueButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeIssueButtonActionPerformed
        ResolveIssuePanel panel = new ResolveIssuePanel(issue);
        String resolution = issue.getFieldValue(NbJiraIssue.IssueField.RESOLUTION);
        final Resolution newResolution;
        if ((resolution == null) || resolution.trim().equals("")) { // NOI18N
            String title = NbBundle.getMessage(IssuePanel.class, "IssuePanel.closeIssueButton.text"); // NOI18N
            if (BugtrackingUtil.show(panel, title, title)) {
                newResolution = panel.getSelectedResolution();
            } else {
                newResolution = null;
            }
        } else {
            newResolution = issue.getRepository().getConfiguration().getResolutionById(resolution);
        }
        if (newResolution != null) {
            String pattern = NbBundle.getMessage(IssuePanel.class, "IssuePanel.closeIssueMessage"); // NOI18N
            String message = MessageFormat.format(pattern, issue.getKey());
            final String comment = panel.getComment();
            submitChange(new Runnable() {
                @Override
                public void run() {
                    issue.close(newResolution, comment);
                    issue.submitAndRefresh();
                }
            }, message);
        }

    }//GEN-LAST:event_closeIssueButtonActionPerformed

    private void reopenIssueButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reopenIssueButtonActionPerformed
        String pattern = NbBundle.getMessage(IssuePanel.class, "IssuePanel.reopenIssueMessage"); // NOI18N
        String message = MessageFormat.format(pattern, issue.getKey());
        submitChange(new Runnable() {
            @Override
            public void run() {
                issue.reopen(null);
                issue.submitAndRefresh();
            }
        }, message);
    }//GEN-LAST:event_reopenIssueButtonActionPerformed

    private void logWorkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logWorkButtonActionPerformed
        WorkLogPanel panel = new WorkLogPanel(issue);
        NbJiraIssue.NewWorkLog log = panel.showDialog();
        if (log != null) {
            issue.addWorkLog(log);
            unsavedFields.add(WORKLOG);
            updateWorkLogStatus();
        }
    }//GEN-LAST:event_logWorkButtonActionPerformed

    private void addToCategoryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addToCategoryButtonActionPerformed
        Jira.getInstance().getBugtrackingFactory().addToCategory(JiraUtils.getRepository(issue.getRepository()), issue); 
    }//GEN-LAST:event_addToCategoryButtonActionPerformed

    @NbBundle.Messages({
        "# {0} - parent task description", "MSG_IssuePanel.creatingSubtask=Creating subtask of {0}"
    })
    private void createSubtaskButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createSubtaskButtonActionPerformed
        final ProgressHandle handle = ProgressHandleFactory.createHandle(
                Bundle.MSG_IssuePanel_creatingSubtask(issue.getDisplayName()));
        handle.start();
        handle.switchToIndeterminate();
        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    NbJiraIssue subTask = issue.createSubtask();
                    if (subTask != null) {
                        JiraUtils.openIssue(subTask);
                    }
                } finally {
                    handle.finish();
                }
            }
        });
    }//GEN-LAST:event_createSubtaskButtonActionPerformed

    private void assigneeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_assigneeComboActionPerformed
        if (!reloading) {
            assigneeStatusLabel.setVisible(false);
        }
        Object value = assigneeCombo.getSelectedItem();
        if (value instanceof RepositoryUser) {
            String assignee = ((RepositoryUser)value).getUserName();
            assigneeCombo.setSelectedItem(assignee);
        }
    }//GEN-LAST:event_assigneeComboActionPerformed

    private void showInBrowserButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showInBrowserButtonActionPerformed
        try {
            URL url = new URL(issue.getRepository().getUrl() + "/browse/" + issue.getKey()); // NOI18N
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        } catch (MalformedURLException muex) {
            Jira.LOG.log(Level.INFO, "Unable to show the issue in the browser.", muex); // NOI18N
        }
    }//GEN-LAST:event_showInBrowserButtonActionPerformed

    private void btnSaveChangesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveChangesActionPerformed
        skipReload = true;
        enableComponents(false);
        RP.post(new Runnable() {
            @Override
            public void run() {
                boolean saved = false;
                try {
                    saved = issue.save();
                } finally {
                    final boolean fSaved = saved;
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            unsavedFields.clear();
                            enableComponents(true);
                            btnSaveChanges.setEnabled(!fSaved);
                            updateFieldStatuses();
                            skipReload = false;
                        }
                    });
                }
            }
        });
    }//GEN-LAST:event_btnSaveChangesActionPerformed

    @NbBundle.Messages({
        "LBL_IssuePanel.deleteTask.title=Delete New Task?",
        "MSG_IssuePanel.deleteTask.message=Do you want to delete the new task permanently?"
    })
    private void btnDeleteTaskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteTaskActionPerformed
        if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(),
                Bundle.MSG_IssuePanel_deleteTask_message(),
                Bundle.LBL_IssuePanel_deleteTask_title(), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
            return;
        }
        Container tc = SwingUtilities.getAncestorOfClass(TopComponent.class, this);
        if (tc instanceof TopComponent) {
            ((TopComponent) tc).close();
        }        
        RP.post(new Runnable() {
            @Override
            public void run() {
                issue.delete();
            }
        });
    }//GEN-LAST:event_btnDeleteTaskActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel actionLabel;
    private javax.swing.JPanel actionPanel;
    private javax.swing.JTextArea addCommentArea;
    private javax.swing.JLabel addCommentLabel;
    private javax.swing.JScrollPane addCommentScrollPane;
    private javax.swing.JLabel addCommentWarning;
    private org.netbeans.modules.bugtracking.util.LinkButton addToCategoryButton;
    private javax.swing.JLabel affectsVersionLabel;
    private javax.swing.JList affectsVersionList;
    private javax.swing.JScrollPane affectsVersionScrollPane;
    private javax.swing.JLabel affectsVersionWarning;
    private javax.swing.JComboBox assigneeCombo;
    private javax.swing.JTextField assigneeField;
    private javax.swing.JLabel assigneeLabel;
    private javax.swing.JLabel assigneeStatusLabel;
    private javax.swing.JLabel assigneeWarning;
    private javax.swing.JLabel attachmentLabel;
    private javax.swing.JLabel attachmentsWarning;
    private javax.swing.JButton btnDeleteTask;
    private javax.swing.JButton btnSaveChanges;
    private javax.swing.JButton cancelButton;
    private org.netbeans.modules.bugtracking.util.LinkButton closeIssueButton;
    private javax.swing.JLabel componentLabel;
    private javax.swing.JList componentList;
    private javax.swing.JScrollPane componentScrollPane;
    private javax.swing.JLabel componentWarning;
    private org.netbeans.modules.bugtracking.util.LinkButton convertToSubtaskButton;
    private org.netbeans.modules.bugtracking.util.LinkButton createSubtaskButton;
    private javax.swing.JTextField createdField;
    private javax.swing.JLabel createdLabel;
    private javax.swing.JPanel customFieldPanelLeft;
    private javax.swing.JPanel customFieldPanelRight;
    private javax.swing.JFormattedTextField dueField;
    private javax.swing.JLabel dueLabel;
    private javax.swing.JLabel dueWarning;
    private javax.swing.JPanel dummyAttachmentPanel;
    private javax.swing.JPanel dummyCommentPanel;
    private javax.swing.JPanel dummyIssueLinksPanel;
    private javax.swing.JPanel dummySubtaskPanel;
    private javax.swing.JTextArea environmentArea;
    private javax.swing.JLabel environmentLabel;
    private javax.swing.JScrollPane environmentScrollPane;
    private javax.swing.JLabel environmentWarning;
    private javax.swing.JLabel fixVersionLabel;
    private javax.swing.JList fixVersionList;
    private javax.swing.JScrollPane fixVersionScrollPane;
    private javax.swing.JLabel fixVersionWarning;
    private javax.swing.JLabel headerLabel;
    private javax.swing.JLabel issueLinksLabel;
    private javax.swing.JComboBox issueTypeCombo;
    private javax.swing.JLabel issueTypeLabel;
    private javax.swing.JLabel issueTypeWarning;
    private org.netbeans.modules.bugtracking.util.LinkButton logWorkButton2;
    private javax.swing.JTextField originalEstimateField;
    private javax.swing.JTextField originalEstimateFieldNew;
    private javax.swing.JLabel originalEstimateHint;
    private javax.swing.JLabel originalEstimateLabel;
    private javax.swing.JLabel originalEstimateLabelNew;
    private javax.swing.JLabel originalEstimateNewWarning;
    private javax.swing.JPanel originalEstimatePanel;
    private javax.swing.JLabel originalEstimateWarning;
    private javax.swing.JPanel parentHeaderPanel;
    private javax.swing.JComboBox priorityCombo;
    private javax.swing.JLabel priorityLabel;
    private javax.swing.JLabel priorityWarning;
    private javax.swing.JComboBox projectCombo;
    private javax.swing.JTextField projectField;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JLabel projectWarning;
    private org.netbeans.modules.bugtracking.util.LinkButton refreshButton;
    private javax.swing.JTextField remainingEstimateField;
    private javax.swing.JLabel remainingEstimateLabel;
    private javax.swing.JPanel remainingEstimatePanel;
    private javax.swing.JLabel remainingEstimateWarning;
    private org.netbeans.modules.bugtracking.util.LinkButton reopenIssueButton;
    private javax.swing.JLabel reporterStatusLabel;
    private javax.swing.JComboBox resolutionCombo;
    private javax.swing.JTextField resolutionField;
    private javax.swing.JLabel resolutionLabel;
    private javax.swing.JLabel resolutionWarning;
    private org.netbeans.modules.bugtracking.util.LinkButton resolveIssueButton;
    private javax.swing.JSeparator separator;
    private org.netbeans.modules.bugtracking.util.LinkButton showInBrowserButton;
    private org.netbeans.modules.bugtracking.util.LinkButton startProgressButton;
    private javax.swing.JComboBox statusCombo;
    private javax.swing.JTextField statusField;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JLabel statusWarning;
    private org.netbeans.modules.bugtracking.util.LinkButton stopProgressButton;
    private javax.swing.JButton submitButton;
    private javax.swing.JLabel subtaskLabel;
    private javax.swing.JTextField summaryField;
    private javax.swing.JLabel summaryLabel;
    private javax.swing.JLabel summaryWarning;
    private javax.swing.JTextField timeSpentField;
    private javax.swing.JLabel timeSpentLabel;
    private javax.swing.JPanel timeSpentPanel;
    private javax.swing.JLabel timeSpentWarning;
    private javax.swing.JTextField updatedField;
    private javax.swing.JLabel updatedLabel;
    private javax.swing.JLabel workLogWarning;
    // End of variables declaration//GEN-END:variables

    @Override
    public Dimension getPreferredSize() {
        return getMinimumSize(); // NbJiraIssue 176085
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return getUnitIncrement();
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return (orientation==SwingConstants.VERTICAL) ? visibleRect.height : visibleRect.width;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        JScrollPane scrollPane = (JScrollPane)SwingUtilities.getAncestorOfClass(JScrollPane.class, this);
        if (scrollPane!=null) {
             // NbJiraIssue 176085
            int minWidth = getMinimumSize().width;
            int width = scrollPane.getSize().width;
            Insets insets = scrollPane.getInsets();
            width -= insets.left+insets.right;
            Border border = scrollPane.getViewportBorder();
            if (border != null) {
                insets = border.getBorderInsets(scrollPane);
                width -= insets.left+insets.right;
            }
            JComponent vsb = scrollPane.getVerticalScrollBar();
            if (vsb!=null && vsb.isVisible()) {
                width -= vsb.getSize().width;
            }
            if (minWidth>width) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    private int unitIncrement;
    private int getUnitIncrement() {
        if (unitIncrement == 0) {
            Font font = UIManager.getFont("Label.font"); // NOI18N
            if (font != null) {
                unitIncrement = (int)(font.getSize()*1.5);
            }
        }
        return unitIncrement;
    }

    void makeCaretVisible(JTextArea textArea) {
        int pos = textArea.getCaretPosition();
        try {
            Rectangle rec = textArea.getUI().modelToView(textArea, pos);
            if (rec != null) {
                Point p = SwingUtilities.convertPoint(textArea, rec.x, rec.y, this);
                scrollRectToVisible(new Rectangle(p.x, p.y, rec.width, rec.height));
            }
        } catch (BadLocationException blex) {
            Jira.LOG.log(Level.INFO, blex.getMessage(), blex);
        }
    }

    void opened() {
        open = true;
        undoRedoSupport = Jira.getInstance().getUndoRedoSupport(issue);
        undoRedoSupport.register(addCommentArea); 
        undoRedoSupport.register(environmentArea); 
        
        enableComponents(false);
        issue.opened();
    }
    
    void closed() {
        open = false;
        if(issue != null) {
            commentsPanel.storeSettings();
            if (undoRedoSupport != null) {
                undoRedoSupport.unregisterAll();
                undoRedoSupport = null;
            }
            issue.closed();
        }
    }

    private void setupListeners () {
        if (issue.isNew()) {
            addCommentArea.getDocument().addDocumentListener(new FieldChangeListener(
                    addCommentArea, IssueField.DESCRIPTION) {
                @Override
                void fieldModified () {
                    // still new?
                    if (issue.isNew()) {
                        super.fieldModified();
                    }
                }
            });
            originalEstimateFieldNew.getDocument().addDocumentListener(new FieldChangeListener(originalEstimateFieldNew,
                    IssueField.INITIAL_ESTIMATE, originalEstimateLabelNew, originalEstimateLabelNew) {
                @Override
                void fieldModified () {
                    // still new?
                    if (issue.isNew() && !reloading) {
                        String estimateCode = originalEstimateFieldNew.getText();
                        String estimateTxt = JiraUtils.getWorkLogSeconds(
                                estimateCode,
                                issue.getRepository().getConfiguration().getWorkDaysPerWeek(),
                                issue.getRepository().getConfiguration().getWorkHoursPerDay()) + ""; // NOI18N
                        storeFieldValue(NbJiraIssue.IssueField.INITIAL_ESTIMATE, estimateTxt);
                        storeFieldValue(NbJiraIssue.IssueField.ESTIMATE, estimateTxt);
                    }
                }
            });
        }
        addCommentArea.getDocument().addDocumentListener(new FieldChangeListener(
                addCommentArea, IssueField.COMMENT, addCommentWarning, addCommentLabel));
        environmentArea.getDocument().addDocumentListener(new FieldChangeListener(
                environmentArea, IssueField.ENVIRONMENT, environmentWarning, environmentLabel));
        summaryField.getDocument().addDocumentListener(new FieldChangeListener(summaryField, IssueField.SUMMARY, summaryWarning, summaryLabel));
        projectCombo.addActionListener(new FieldChangeListener(projectCombo, IssueField.PROJECT, projectWarning, projectLabel));
        componentList.addListSelectionListener(new FieldChangeListener(componentList, IssueField.COMPONENT, componentWarning, componentLabel));
        affectsVersionList.addListSelectionListener(new FieldChangeListener(affectsVersionList, IssueField.AFFECTSVERSIONS, affectsVersionWarning, affectsVersionLabel));
        fixVersionList.addListSelectionListener(new FieldChangeListener(fixVersionList, IssueField.FIXVERSIONS, fixVersionWarning, fixVersionLabel));
        priorityCombo.addActionListener(new FieldChangeListener(priorityCombo, IssueField.PRIORITY, priorityWarning, priorityLabel));
        statusCombo.addActionListener(new FieldChangeListener(statusCombo, IssueField.STATUS, statusWarning, statusLabel) {
            @Override
            void fieldModified () {
                if (reloading) {
                    return;
                }
                Object statusValue = issue.getRepository().getConfiguration().getStatusById(
                        issue.getFieldValue(IssueField.STATUS));
                Object selectedValue = statusCombo.getSelectedItem();
                if (!(statusValue instanceof JiraStatus) || (!(selectedValue instanceof JiraStatus))) {
                    return; // should not happen
                }
                JiraStatus initialStatus = (JiraStatus)statusValue;
                JiraStatus selectedStatus = (JiraStatus)selectedValue;
                if (initialStatus.equals(selectedStatus)) {
                    return; // no change
                }
                String statusName = selectedStatus.getName();
                Resolution resolution = (Resolution) resolutionCombo.getSelectedItem();
                String oldResolution = issue.getFieldValue(IssueField.RESOLUTION);
                switch (statusName) {
                    case STATUS_OPEN:
                        unsavedFields.add(IssueField.STATUS.getKey());
                        issue.stopProgress();
                        storeFieldValue(IssueField.STATUS, statusCombo);
                        break;
                    case STATUS_IN_PROGRESS:
                        unsavedFields.add(IssueField.STATUS.getKey());
                        issue.startProgress();
                        storeFieldValue(IssueField.STATUS, statusCombo);
                        break;
                    case STATUS_REOPENED:
                        unsavedFields.add(IssueField.STATUS.getKey());
                        issue.reopen(null);
                        storeFieldValue(IssueField.STATUS, statusCombo);
                        break;
                    case STATUS_RESOLVED:
                        unsavedFields.add(IssueField.STATUS.getKey());
                        issue.resolve(resolution, null);
                        storeFieldValue(IssueField.STATUS, statusCombo);
                        break;
                    case STATUS_CLOSED:
                        unsavedFields.add(IssueField.STATUS.getKey());
                        issue.close(resolution, null);
                        storeFieldValue(IssueField.STATUS, statusCombo);
                        storeFieldValue(IssueField.RESOLUTION, resolutionCombo);
                        break;
                }
                if (!oldResolution.equals(issue.getFieldValue(IssueField.RESOLUTION))) {
                    unsavedFields.add(IssueField.RESOLUTION.getKey());
                    updateFieldDecorations(resolutionCombo, IssueField.RESOLUTION, resolutionWarning, resolutionLabel);
                }
                updateDecorations();
            }
        });
        resolutionCombo.addActionListener(new FieldChangeListener(resolutionCombo, IssueField.RESOLUTION, resolutionWarning, resolutionLabel));
        issueTypeCombo.addActionListener(new FieldChangeListener(issueTypeCombo, IssueField.TYPE, issueTypeWarning, issueTypeLabel));
        dueField.getDocument().addDocumentListener(new FieldChangeListener(dueField, IssueField.DUE, dueWarning, dueLabel));
        assigneeField.getDocument().addDocumentListener(new FieldChangeListener(assigneeField, IssueField.ASSIGNEE, assigneeWarning, assigneeLabel));
        assigneeCombo.addActionListener(new FieldChangeListener(assigneeCombo, IssueField.ASSIGNEE, assigneeWarning, assigneeLabel));
        
        setupCustomFieldListeners();
        attachmentsPanel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged (ChangeEvent e) {
                if (!reloading && attachmentsPanel.isVisible()) {
                    if (issue.setUnsubmittedAttachments(attachmentsPanel.getNewAttachments())) {
                        unsavedFields.add(NEW_ATTACHMENTS);
                        updateAttachmentsStatus();
                    }
                }
            }
        });
    }
    
    private void setupCustomFieldListeners () {
        for (final NbJiraIssue.CustomField cField : getSupportedCustomFields()) {
            String type = cField.getType();
            if ("com.atlassian.jira.plugin.labels:labels".equals(type) //NOI18N
                    || "com.atlassian.jira.plugin.system.customfieldtypes:textfield".equals(type)) { //NOI18N
                JTextField field = (JTextField) customFieldComponents.get(cField.getId());
                field.getDocument().addDocumentListener(new FieldChangeListener(field, null,
                        customFieldWarnings.get(cField.getId()),
                        customFieldLabels.get(cField.getId())) {
                    @Override
                    void fieldModified () {
                        if (!reloading) {
                            storeCustomFieldValue(cField);
                            updateCustomFieldDecoration(cField);
                        }
                    }
                });
            }
        }
    }

    private void updateIcon (JLabel label) {
        label.setToolTipText(null);
        label.setIcon(null);
        Map<String, String> conflicts = tooltipsConflict.get(label);
        Map<String, String> local = tooltipsLocal.get(label);
        Map<String, String> remote = tooltipsIncoming.get(label);
        if (conflicts != null || local != null || remote != null) {
            if (conflicts != null) {
                label.setIcon(ICON_CONFLICT);
            } else if (local != null) {
                label.setIcon(ICON_UNSUBMITTED);
            } else {
                label.setIcon(ICON_REMOTE);
            }
            StringBuilder sb = new StringBuilder("<html>"); //NOI18N
            appendTooltips(sb, conflicts);
            appendTooltips(sb, local);
            appendTooltips(sb, remote);
            sb.append("</html>"); //NOI18N
            label.setToolTipText(sb.toString());
        }
    }

    private void appendTooltips (StringBuilder sb, Map<String, String> tooltips) {
        if (tooltips != null) {
            for (Map.Entry<String, String> e : tooltips.entrySet()) {
                sb.append(e.getValue());
            }
        }
    }

    private void removeTooltips (JLabel label, String field) {
        tooltipsConflict.removeTooltip(label, field);
        tooltipsIncoming.removeTooltip(label, field);
        tooltipsLocal.removeTooltip(label, field);
    }

    private String toReadable (String value, IssueField field) {
        JiraConfiguration config = issue.getRepository().getConfiguration();
        String projectId = issue.getFieldValue(IssueField.PROJECT);
        switch (field) {
            case DUE:
                Date date = JiraUtils.dateByMillis(value);
                if (date != null) {
                    try {
                        value = dueDateFormatter.valueToString(date);
                    } catch (ParseException ex) { }
                }
                break;
            default:
                value = JiraUtils.toReadable(config, projectId, field, value);
                break;
        }
        return value;
    }

    private List<String> toReadable (List<String> values, IssueField field) {
        List<String> readable = new ArrayList<>(values.size());
        for (String value : values) {
            readable.add(toReadable(value, field));
        }
        return readable;
    }

    private void updateCustomFieldDecoration (NbJiraIssue.CustomField field) {
        String id = field.getId();
        JLabel warningLabel = customFieldWarnings.get(id);
        JLabel label = customFieldLabels.get(id);
        JComponent comp = customFieldComponents.get(id);

        if (warningLabel != null && label != null && comp != null) {
            boolean fieldDirty = false;
            boolean valueModifiedByUser = false;
            boolean valueModifiedByServer = false;
            String newValue = JiraUtils.mergeValues(field.getValues());
            String lastSeenValue = JiraUtils.mergeValues(field.getLastSeenValues());
            String repositoryValue = JiraUtils.mergeValues(field.getRepositoryValues());
            fieldDirty |= unsavedFields.contains(id);
            valueModifiedByUser |= (issue.getFieldStatus(id) & NbJiraIssue.FIELD_STATUS_OUTGOING) != 0;
            valueModifiedByServer |= (issue.getFieldStatus(id) & NbJiraIssue.FIELD_STATUS_MODIFIED) != 0;
            
            updateFieldDecorations(warningLabel, label, field.getLabel(), id,
                    fieldDirty, valueModifiedByUser, valueModifiedByServer,
                    lastSeenValue, repositoryValue, newValue);
        }
    }
    
    @NbBundle.Messages({
        "# {0} - icon path",
        "IssuePanel.workLogToSubmit=<p><img src=\"{0}\">&nbsp;Unsubmitted Work Log</p>"
            + "<p>A new Work Log has been created but not yet submitted</p>"
    })
    private void updateWorkLogStatus () {
        boolean change = false;
        if (!issue.isNew()) {
            NbJiraIssue.NewWorkLog log = issue.getEditedWorkLog();
            boolean valueModifiedByUser = log != null && log.isToSubmit();
            removeTooltips(workLogWarning, WORKLOG);
            if (valueModifiedByUser) {
                String message = Bundle.IssuePanel_commentAddedLocally();
                tooltipsLocal.addTooltip(workLogWarning, WORKLOG, Bundle.IssuePanel_workLogToSubmit(ICON_UNSUBMITTED_PATH));
                change = !message.equals(fieldsLocal.put(WORKLOG, message));
            } else {
                change = fieldsLocal.remove(WORKLOG) != null;
            }
            updateIcon(workLogWarning);
        }
        if (change && !reloading) {
//            updateMessagePanel();
        }
    }
    
    @NbBundle.Messages({
        "# {0} - icon path",
        "IssuePanel.attachmentsToSubmit=<p><img src=\"{0}\">&nbsp;Unsubmitted Attachments</p>"
            + "<p>New attachments were added but not yet submitted</p>",
        "IssuePanel.attachmentsAddedLocally=Attachments were added but not yet submitted"
    })
    private void updateAttachmentsStatus () {
        boolean change = false;
        if (!issue.isNew()) {
            boolean valueModifiedByUser = !issue.getUnsubmittedAttachments().isEmpty();
            removeTooltips(attachmentsWarning, NEW_ATTACHMENTS);
            if (attachmentLabel.getFont().isBold()) {
                attachmentLabel.setFont(attachmentLabel.getFont().deriveFont(attachmentLabel.getFont().getStyle() & ~Font.BOLD));
            }
            if (valueModifiedByUser) {
                String message = Bundle.IssuePanel_attachmentsAddedLocally();
                tooltipsLocal.addTooltip(attachmentsWarning, NEW_ATTACHMENTS,
                        Bundle.IssuePanel_attachmentsToSubmit(ICON_UNSUBMITTED_PATH));
                change = !message.equals(fieldsLocal.put(NEW_ATTACHMENTS, message));
            } else {
                change = fieldsLocal.remove(NEW_ATTACHMENTS) != null;
            }
            updateIcon(attachmentsWarning);
            if (unsavedFields.contains(NEW_ATTACHMENTS)) {
                attachmentLabel.setFont(attachmentLabel.getFont().deriveFont(attachmentLabel.getFont().getStyle() | Font.BOLD));
            }
        }
        if (change && !reloading) {
//            updateMessagePanel();
        }
    }

    private static class TooltipsMap extends HashMap<JLabel, Map<String, String>> {

        private void removeTooltip (JLabel label, String fieldKey) {
            Map<String, String> fields = get(label);
            if (fields != null) {
                fields.remove(fieldKey);
                if (fields.isEmpty()) {
                    remove(label);
                }
            }
        }

        private void addTooltip (JLabel label, String fieldKey, String tooltip) {
            Map<String, String> fields = get(label);
            if (fields == null) {
                fields = new LinkedHashMap<>(2);
                put(label, fields);
            }
            fields.put(fieldKey, tooltip);
        }
        
    }
    
    private class FieldChangeListener implements DocumentListener, ActionListener, ListSelectionListener {
        private final IssueField field;
        private final JComponent component;
        private final JLabel warningLabel;
        private final JComponent fieldLabel;
        private final String fieldName;
        private Pair<IssueField, ? extends JComponent>[] decoratedFields;

        public FieldChangeListener (JComponent component, IssueField field) {
            this(component, field, null, null);
        }

        public FieldChangeListener (JComponent component, IssueField field, JLabel warningLabel,
                JComponent fieldLabel) {
            this(component, field, warningLabel, fieldLabel, Pair.of(field, component));
        }
        
        public FieldChangeListener (JComponent component, IssueField field, JLabel warningLabel,
                JComponent fieldLabel, Pair<IssueField, ? extends JComponent>... multiField) {
            this.component = component;
            this.field = field;
            this.warningLabel = warningLabel;
            this.fieldLabel = fieldLabel;
            this.fieldName = fieldLabel == null ? null : fieldName(fieldLabel);
            this.decoratedFields = multiField;
        }

        @Override
        public final void insertUpdate (DocumentEvent e) {
            fieldModified();
        }

        @Override
        public final void removeUpdate (DocumentEvent e) {
            fieldModified();
        }

        @Override
        public final void changedUpdate (DocumentEvent e) {
            fieldModified();
        }

        @Override
        public void actionPerformed (ActionEvent e) {
            if (e.getSource() == component) {
                fieldModified();
            }
        }

        @Override
        public void valueChanged (ListSelectionEvent e) {
            if (!e.getValueIsAdjusting() && e.getSource() == component) {
                fieldModified();
            }
        }
        
        void fieldModified () {
            if (!reloading && isEnabled()) {
                if (component instanceof JFormattedTextField) {
                    storeFieldValue(field, (JFormattedTextField) component);
                    updateDecorations();
                } else if (component instanceof JTextComponent) {
                    storeFieldValue(field, (JTextComponent) component);
                    updateDecorations();
                } else if (component instanceof JList) {
                    storeFieldValue(field, (JList) component);
                    updateDecorations();
                } else if (component instanceof JComboBox) {
                    storeFieldValue(field, (JComboBox) component);
                    updateDecorations();
                }
            }
        }
        
        public boolean isEnabled () {
            return component.isVisible() && component.isEnabled();
        }
        
        protected final void updateDecorations () {
            updateFieldDecorations(warningLabel, fieldLabel, fieldName, decoratedFields);
        }
    }
    
    private Map<Component, Boolean> enableMap = new HashMap<>();
    private void enableComponents(boolean enable) {
        if (enable) {
            for (Map.Entry<Component, Boolean> e : enableMap.entrySet()) {
                e.getKey().setEnabled(e.getValue());
            }
            enableMap.clear();
        } else {
            disableComponents(this);
        }
    }

    private void disableComponents(Component comp) {
        if (comp instanceof Container) {
            for (Component subComp : ((Container)comp).getComponents()) {
                disableComponents(subComp);
            }
        }
        if ((comp instanceof JComboBox)
                || ((comp instanceof JTextComponent) && ((JTextComponent)comp).isEditable())
                || (comp instanceof AbstractButton) || (comp instanceof JList)) {
            enableMap.put(comp, comp.isEnabled());
            comp.setEnabled(false);
        }
    }
    
    private String fieldName(JComponent fieldLabel) {
        assert fieldLabel instanceof JLabel || fieldLabel instanceof JButton;
        String txt;
        if(fieldLabel instanceof JLabel) {
            txt = ((JLabel) fieldLabel).getText().trim();
            
        } else if(fieldLabel instanceof JButton) {
            txt = ((JButton) fieldLabel).getText().trim();
        } else {
            return null;
        }
        if (txt.endsWith(":")) { // NOI18N
            txt = txt.substring(0, txt.length()-1);
        }
        return txt;
    }

    class CancelHighlightListener implements DocumentListener, ActionListener, ListSelectionListener {
        private JLabel label;

        CancelHighlightListener(JLabel label) {
            this.label = label;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            cancelHighlight(label);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            cancelHighlight(label);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            cancelHighlight(label);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            cancelHighlight(label);
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting()) {
                return;
            }
            cancelHighlight(label);
        }
    }

    class RevalidatingListener implements DocumentListener, Runnable {
        private boolean ignoreUpdate;

        @Override
        public void insertUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            if (ignoreUpdate) return;
            ignoreUpdate = true;
            EventQueue.invokeLater(this);
        }

        @Override
        public void run() {
            revalidate();
            repaint();
            ignoreUpdate = false;
        }
    }

}
