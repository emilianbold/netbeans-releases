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
import java.text.DateFormat;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
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
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.LayoutStyle;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.NumberFormatter;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.issuetable.TableSorter;
import org.netbeans.modules.team.spi.RepositoryUser;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.netbeans.modules.bugtracking.commons.LinkButton;
import org.netbeans.modules.team.spi.RepositoryUserRenderer;
import org.netbeans.modules.bugtracking.commons.UIUtils;
import org.netbeans.modules.bugtracking.spi.SchedulePicker;
import org.netbeans.modules.jira.Jira;
import org.netbeans.modules.jira.JiraConfig;
import org.netbeans.modules.jira.client.spi.IssueType;
import org.netbeans.modules.jira.client.spi.JiraStatus;
import org.netbeans.modules.jira.client.spi.Priority;
import org.netbeans.modules.jira.client.spi.Project;
import org.netbeans.modules.jira.client.spi.Resolution;
import org.netbeans.modules.jira.client.spi.User;
import org.netbeans.modules.jira.client.spi.Version;
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
import org.netbeans.modules.mylyn.util.NbDateRange;
import org.netbeans.modules.spellchecker.api.Spellchecker;
import org.netbeans.modules.team.ide.spi.IDEServices;
import org.netbeans.modules.team.spi.TeamAccessorUtils;
import org.openide.awt.HtmlBrowser;
import org.openide.util.ImageUtilities;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.HelpCtx;

/**
 *
 * @author Jan Stola
 */
public class IssuePanel extends javax.swing.JPanel {
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
    private final Set<String> unsavedFields = new UnsavedFieldSet();
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
    private static final String SECTION_ATTRIBUTES = ".attributes"; //NOI18N
    private static final String SECTION_ATTACHMENTS = ".attachments"; //NOI18N
    private static final String SECTION_COMMENTS = ".comments"; //NOI18N
    private static final String SECTION_SUBTASKS = ".subtasks"; //NOI18N
    private static final String SECTION_PRIVATE = ".private"; //NOI18N
    private static final String ATTRIBUTE_PRIVATE_NOTES = "nb.private.notes"; //NOI18N
    private static final String ATTRIBUTE_ESTIMATE = "nb.estimate"; //NOI18N
    private static final String ATTRIBUTE_DUE_DATE = "nb.due.date"; //NOI18N
    private static final String ATTRIBUTE_SCHEDULE_DATE = "nb.schedule.date"; //NOI18N
    private final IDEServices.DatePickerComponent privateDueDatePicker;
    private final IDEServices.DatePickerComponent dueDatePicker;
    private final SchedulePicker scheduleDatePicker;
    private static final NumberFormatter estimateFormatter = new NumberFormatter(new java.text.DecimalFormat("#0")) {

        @Override
        public Object stringToValue (String text) throws ParseException {
            Number value = (Number) super.stringToValue(text);
            if (value == null) {
                value = 0;
            }
            if (value.intValue() < 0) {
                return 0;
            } else {
                return value.intValue();
            }
        }

    };
    private static final DateFormat DUE_DATE_FORMAT = DateFormat.getDateInstance(DateFormat.MEDIUM);
    
    private Action[] attributesSectionActions;
    private Action[] commentsSectionActions;
    private Action[] attachmentsSectionActions;
    private Action[] privateSectionActions;
    private Action[] subtasksSectionActions;
    private Action[] moreActions;

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
        
        dueDatePicker = UIUtils.createDatePickerComponent();
        ((GroupLayout) attributesSectionPanel.getLayout()).replace(dummyDueField, dueDatePicker.getComponent());
        dueLabel.setLabelFor(dueDatePicker.getComponent());
        attachFieldStatusListeners();
        attachHideStatusListener();
        
        Font font = UIManager.getFont("Label.font"); //NOI18N
        if (font != null) {
            int size = (int)(font.getSize()*1.5);
            mainScrollPane.getHorizontalScrollBar().setUnitIncrement(size);
            mainScrollPane.getVerticalScrollBar().setUnitIncrement(size);
        }
        UIUtils.keepFocusedComponentVisible(mainPanel);
        
        GroupLayout layout = (GroupLayout) privateSectionPanel.getLayout();
        privateDueDatePicker = UIUtils.createDatePickerComponent();
        scheduleDatePicker = new SchedulePicker();
        layout.replace(privateDummyDueDateField, privateDueDatePicker.getComponent());
        privateDueDateLabel.setLabelFor(privateDueDatePicker.getComponent());
        layout.replace(dummyScheduleDateField, scheduleDatePicker.getComponent());
        scheduleDateLabel.setLabelFor(scheduleDatePicker.getComponent());
        privateNotesField.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate (CaretEvent e) {
                makeCaretVisible(privateNotesField);
            }
        });
        // A11Y - Issues 163597 and 163598
        UIUtils.fixFocusTraversalKeys(privateNotesField);
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
                    cancelButton.setEnabled(isModified || isDirty);
                } else {
                    enableMap.put(cancelButton, isModified || isDirty);
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
        GroupLayout layout = (GroupLayout) attributesSectionPanel.getLayout();
        layout.replace(dummyIssueLinksPanel, issueLinksPanel);
        issueLinksLabel.setLabelFor(issueLinksPanel);
    }

    private void initAttachmentsPanel() {
        attachmentsPanel = new AttachmentsPanel(this);
        GroupLayout layout = (GroupLayout) attachmentsSectionPanel.getLayout();
        layout.replace(dummyAttachmentPanel, attachmentsPanel);
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
                if (value instanceof org.netbeans.modules.jira.client.spi.Component) {
                    value = ((org.netbeans.modules.jira.client.spi.Component)value).getName();
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
        JiraConfiguration rc = issue.getRepository().getConfiguration();
        Project[] projects = rc != null ? rc.getProjects() : new Project[0];
        DefaultComboBoxModel model = new DefaultComboBoxModel(projects);
        model.setSelectedItem(null); // Make sure nothing is pre-selected
        projectCombo.setModel(model);
    }

    private void initPriorityCombo() {
        JiraConfiguration rc = issue.getRepository().getConfiguration();        
        Priority[] priority = rc != null ? rc.getPriorities() : new Priority[0];
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
        JiraConfiguration rc = issue.getRepository().getConfiguration();
        Resolution[] resolution = rc != null ? rc.getResolutions() : new Resolution[0];
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
                            GroupLayout layout = (GroupLayout) attributesSectionPanel.getLayout();
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
        GroupLayout layout = (GroupLayout) commentsSectionPanel.getLayout();
        layout.replace(dummyCommentPanel, commentsPanel);
    }

    private void initSpellChecker () {
        Spellchecker.register(summaryField);
        Spellchecker.register(addCommentArea);
        Spellchecker.register(privateNotesField);
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
        dueDatePicker.addChangeListener(new CancelHighlightListener(dueLabel));
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
        commentsSection.setVisible(!isNew);
        attachmentsSection.setVisible(!isNew);
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
        btnDeleteTask.setVisible(isNew);
        separatorLabelDismiss.setVisible(isNew);

        subtasksSection.setVisible(false);
        cancelButton.setEnabled(issue.hasLocalEdits() || !unsavedFields.isEmpty());
        btnMoreActions.setVisible(!isNew);

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
        startProgressAction.setEnabled(startProgressAvailable);
        stopProgressAction.setEnabled(stopProgressAvailable);
        resolveIssueAction.setEnabled(resolveIssueAvailable);
        closeIssueAction.setEnabled(closeIssueAvailable);
        reopenIssueAction.setEnabled(reopenIssueAvailable);

        newCommentSection.setLabel(NbBundle.getMessage(IssuePanel.class, isNew ? "IssuePanel.description" : "IssuePanel.newCommentsSection.label")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(submitButton, NbBundle.getMessage(IssuePanel.class, isNew ? "IssuePanel.submitButton.text.new" : "IssuePanel.submitButton.text")); // NOI18N
        boolean showProjectCombo = isNew && !issue.isSubtask();
        GroupLayout layout = (GroupLayout) attributesSectionPanel.getLayout();
        if (showProjectCombo != (projectCombo.getParent() != null)) {
            layout.replace(showProjectCombo ? projectField : projectCombo, showProjectCombo ? projectCombo : projectField);
        }
        if (isNew != (statusField.getParent() != null)) {
            layout.replace(isNew ? statusCombo : statusField, isNew ? statusField : statusCombo);
        }
        cancelButton.setVisible(!isNew);
        separatorLabelCancel.setVisible(!isNew);

        privateNotesField.setText(issue.getPrivateNotes());
        privateDueDatePicker.setDate(issue.getDueDate());
        NbDateRange scheduleDate = issue.getScheduleDate();
        scheduleDatePicker.setScheduleDate(scheduleDate == null ? null : scheduleDate.toSchedulingInfo());
        privateEstimateField.setValue(issue.getEstimate());
        privateDueDatePicker.getComponent().setEnabled(false); // due date part of jira
                
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
        if(config == null) {
            return;
        }
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
        reloadField(dueDatePicker, JiraUtils.dateByMillis(issue.getFieldValue(NbJiraIssue.IssueField.DUE)), NbJiraIssue.IssueField.DUE);
        String assignee = issue.getFieldValue(NbJiraIssue.IssueField.ASSIGNEE);
        String selectedAssignee = (assigneeField.getParent() == null) ? assigneeCombo.getSelectedItem().toString() : assigneeField.getText();
        boolean isKenaiRepository = (issue.getRepository() instanceof KenaiRepository);
        if (isKenaiRepository && (assignee.trim().length() > 0) && (force || !selectedAssignee.equals(assignee))) {
            String host = ((KenaiRepository) issue.getRepository()).getHost();
            JLabel label = TeamAccessorUtils.createUserWidget(issue.getRepository().getUrl(), assignee, host, TeamAccessorUtils.getChatLink(issue.getKey()));
            if (label != null) {
                label.setText(null);
                ((GroupLayout) attributesSectionPanel.getLayout()).replace(assigneeStatusLabel, label);
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
                JLabel label = TeamAccessorUtils.createUserWidget(issue.getRepository().getUrl(), reporter, host, TeamAccessorUtils.getChatLink(issue.getKey()));
                if (label != null) {
                    label.setText(null);
                    ((GroupLayout) attributesSectionPanel.getLayout()).replace(reporterStatusLabel, label);
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

            NbJiraIssue.Attachment[] attachments = issue.getAttachments();
            // Comments
            commentsPanel.setIssue(issue);
            commentsSection.setLabel(NbBundle.getMessage(IssuePanel.class, "IssuePanel.commentsSection.label", issue.getComments().length + 1)); //NOI18N
            UIUtils.keepFocusedComponentVisible(commentsPanel, this);
            reloadField(addCommentArea, issue.getFieldValue(IssueField.COMMENT), IssueField.COMMENT);

            // Attachments
            attachmentsPanel.setIssue(issue, attachments);
            attachmentsSection.setLabel(NbBundle.getMessage(IssuePanel.class, "IssuePanel.attachmentsLabel.text", attachments.length)); //NOI18N
            UIUtils.keepFocusedComponentVisible(attachmentsPanel, this);

            // NbJiraIssue-links
            boolean anyLink = (issue.getLinkedIssues().length != 0);
            issueLinksLabel.setVisible(anyLink);
            issueLinksPanel.setVisible(anyLink);
            issueLinksPanel.setIssue(issue);

            // Sub-tasks
            boolean hasSubtasks = issue.hasSubtasks();
            subtasksSection.setVisible(hasSubtasks);
            subtasksSection.setLabel(NbBundle.getMessage(IssuePanel.class, "IssuePanel.subtasksSection.label", 0)); //NOI18N
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
                                subtasksSection.setLabel(NbBundle.getMessage(IssuePanel.class, "IssuePanel.subtasksSection.label", tableModel.getRowCount())); //NOI18N
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
                                    ((GroupLayout) subtasksSectionpanel.getLayout()).replace(dummySubtaskPanel, subTaskScrollPane);
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
    private JTable subTaskTable;
    private JScrollPane subTaskScrollPane;

    private void reloadCustomFields() {
        for (NbJiraIssue.CustomField cField : getSupportedCustomFields()) {
            String type = cField.getType();
            if ("com.atlassian.jira.plugin.labels:labels".equals(type) //NOI18N
                    || "com.atlassian.jira.plugin.system.customfieldtypes:textfield".equals(type)) { //NOI18N
                JTextField field = (JTextField)customFieldComponents.get(cField.getId());
                if (field != null) {
                    List<String> values = cField.getValues();
                    if(values == null || values.isEmpty()) {
                        Jira.LOG.log(Level.WARNING, "custom field {0} : {1} contains no values", new Object[]{cField.getId(), cField.getType()});
                    } else {
                        field.setText(values.get(0));
                    }
                }
            }
        }
    }

    private boolean isSupportedCustomField(NbJiraIssue.CustomField field) {
        return "com.atlassian.jira.plugin.labels:labels".equals(field.getType()) //NOI18N
                || "com.atlassian.jira.plugin.system.customfieldtypes:textfield".equals(field.getType()); //NOI18N
    }
    
    private void reloadField (Object fieldComponent, Object fieldValue, IssueField field) {
        reloadField(fieldComponent, fieldValue, field, false);
    }
    
    private void reloadField (Object fieldComponent, Object fieldValue, IssueField field, boolean force) {
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
            } else if (fieldComponent instanceof IDEServices.DatePickerComponent) {
                IDEServices.DatePickerComponent picker = (IDEServices.DatePickerComponent) fieldComponent;
                picker.setDate((Date) fieldValue);
            }
        }
    }
    
    private void updateFieldDecorations (Object component, IssueField field, JLabel warningLabel, JComponent fieldLabel) {
        updateFieldDecorations(warningLabel, fieldLabel, fieldName(fieldLabel), Pair.of(field, component));
    }
    
    private void updateFieldDecorations (JLabel warningLabel, JComponent fieldLabel, String fieldName,
            Pair<IssueField, ? extends Object>... fields) {
        String newValue = "", lastSeenValue = "", repositoryValue = ""; //NOI18N
        boolean fieldDirty = false;
        boolean valueModifiedByUser = false;
        boolean valueModifiedByServer = false;
        for (Pair<IssueField, ? extends Object> p : fields) {
            Object component = p.second();
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
    
    private void updateFieldDecorations (String key, JComponent fieldLabel) {
        boolean fieldDirty = unsavedFields.contains(key);
        if (fieldLabel != null) {
            if (fieldDirty) {
                fieldLabel.setFont(fieldLabel.getFont().deriveFont(fieldLabel.getFont().getStyle() | Font.BOLD));
            } else {
                fieldLabel.setFont(fieldLabel.getFont().deriveFont(fieldLabel.getFont().getStyle() & ~Font.BOLD));
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
                case COMPONENT: keys.add(((org.netbeans.modules.jira.client.spi.Component)values[i]).getId()); break;
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

    private List<org.netbeans.modules.jira.client.spi.Component> componentsByIds(String projectId, List<String> componentIds) {
        JiraConfiguration config = issue.getRepository().getConfiguration();
        List<org.netbeans.modules.jira.client.spi.Component> components = new ArrayList<org.netbeans.modules.jira.client.spi.Component>(componentIds.size());
        for (String id : componentIds) {
            org.netbeans.modules.jira.client.spi.Component component = config.getComponentById(projectId, id);
            if(component != null) {
                components.add(component);
            }
        }
        return components;
    }

    private List<Version> versionsByIds(String projectId, List<String> versionIds) {
        JiraConfiguration config = issue.getRepository().getConfiguration();
        List<Version> versions = new ArrayList<>(versionIds.size());
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
                RP.post(new Runnable() { 
                    // HACK! see issue #253592                    
                    @Override
                    public void run() {
                        Mutex.EVENT.readAccess(new Runnable() {
                            @Override
                            public void run () {
                                updateFieldStatuses();
                            }
                        });
                    }
                }, 500);
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
        updateFieldDecorations(dueDatePicker, IssueField.DUE, dueWarning, dueLabel);
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
        
        updateFieldDecorations(addCommentArea, IssueField.COMMENT, addCommentWarning, newCommentSection.getLabelComponent());
        
        updateCustomFieldStatuses();
        updateWorkLogStatus();
        updateAttachmentsStatus();
        
        updateFieldDecorations(ATTRIBUTE_PRIVATE_NOTES, privateNotesLabel);
        updateFieldDecorations(ATTRIBUTE_DUE_DATE, privateDueDateLabel);
        updateFieldDecorations(ATTRIBUTE_SCHEDULE_DATE, scheduleDateLabel);
        updateFieldDecorations(ATTRIBUTE_ESTIMATE, privateEstimateLabel);
        
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
        attributesSectionPanel = new javax.swing.JPanel();
        dummyIssueLinksPanel = new javax.swing.JPanel();
        customFieldPanelLeft = new javax.swing.JPanel();
        customFieldPanelRight = new javax.swing.JPanel();
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
        dummyDueField = new javax.swing.JFormattedTextField();
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
        summaryLabel = new javax.swing.JLabel();
        summaryField = new javax.swing.JTextField();
        environmentLabel = new javax.swing.JLabel();
        environmentScrollPane = new javax.swing.JScrollPane();
        environmentArea = new javax.swing.JTextArea();
        environmentScrollPane1 = new javax.swing.JScrollPane();
        environmentArea1 = new javax.swing.JTextArea();
        originalEstimatePanel = new javax.swing.JPanel();
        remainingEstimatePanel = new javax.swing.JPanel();
        timeSpentPanel = new javax.swing.JPanel();
        originalEstimateFieldNew = new javax.swing.JTextField();
        originalEstimateLabelNew = new javax.swing.JLabel();
        originalEstimateHint = new javax.swing.JLabel();
        logWorkButton2 = new org.netbeans.modules.bugtracking.commons.LinkButton();
        reporterStatusLabel = new javax.swing.JLabel();
        assigneeStatusLabel = new javax.swing.JLabel();
        issueLinksLabel = new javax.swing.JLabel();
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
        originalEstimateNewWarning = new javax.swing.JLabel();
        workLogWarning = new javax.swing.JLabel();
        attachmentsSectionPanel = new javax.swing.JPanel();
        attachmentsWarning = new javax.swing.JLabel();
        dummyAttachmentPanel = new javax.swing.JPanel();
        subtasksSectionpanel = new javax.swing.JPanel();
        dummySubtaskPanel = new javax.swing.JPanel();
        newCommentSectionPanel = new javax.swing.JPanel();
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
        addCommentWarning = new javax.swing.JLabel();
        privateSectionPanel = new javax.swing.JPanel();
        privateDueDateLabel = new javax.swing.JLabel();
        privateDummyDueDateField = new javax.swing.JTextField();
        scheduleDateLabel = new javax.swing.JLabel();
        dummyScheduleDateField = new javax.swing.JTextField();
        privateEstimateLabel = new javax.swing.JLabel();
        privateEstimateField = new javax.swing.JFormattedTextField();
        privateNotesLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        privateNotesField = new javax.swing.JTextArea() {
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
                dim = new Dimension(0, delta + ((dim.height < prefHeight) ? prefHeight : dim.height));
                return dim;
            }
        };
        commentsSectionPanel = new javax.swing.JPanel();
        dummyCommentPanel = new javax.swing.JPanel();
        headerPanel = new javax.swing.JPanel();
        parentHeaderPanel = new javax.swing.JPanel();
        headerLabel = new javax.swing.JLabel();
        buttonsPanel = new javax.swing.JPanel();
        refreshButton = new org.netbeans.modules.bugtracking.commons.LinkButton();
        separatorLabelRefresh = new javax.swing.JLabel();
        showInBrowserButton = new org.netbeans.modules.bugtracking.commons.LinkButton();
        separatorLabelShowInBrowser = new javax.swing.JLabel();
        submitButton = new org.netbeans.modules.bugtracking.commons.LinkButton();
        separatorLabelSubmit = new javax.swing.JLabel();
        cancelButton = new org.netbeans.modules.bugtracking.commons.LinkButton();
        separatorLabelCancel = new javax.swing.JLabel();
        btnDeleteTask = new org.netbeans.modules.bugtracking.commons.LinkButton();
        separatorLabelDismiss = new javax.swing.JLabel();
        btnMoreActions = new org.netbeans.modules.bugtracking.commons.LinkButton();
        mainScrollPane = new javax.swing.JScrollPane();
        mainPanel = new javax.swing.JPanel() {

            @Override
            public Dimension getPreferredSize () {
                return super.getMinimumSize();
            }
        };
        attributesSection = new org.netbeans.modules.bugtracking.commons.CollapsibleSectionPanel();
        attachmentsSection = new org.netbeans.modules.bugtracking.commons.CollapsibleSectionPanel();
        subtasksSection = new org.netbeans.modules.bugtracking.commons.CollapsibleSectionPanel();
        commentsSection = new org.netbeans.modules.bugtracking.commons.CollapsibleSectionPanel();
        privateSection = new org.netbeans.modules.bugtracking.commons.CollapsibleSectionPanel();
        newCommentSection = new org.netbeans.modules.bugtracking.commons.SectionPanel();

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

        attributesSectionPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

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

        summaryLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.summaryLabel.text")); // NOI18N

        environmentLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.environmentLabel.text")); // NOI18N

        environmentArea.setLineWrap(true);
        environmentArea.setRows(5);
        environmentScrollPane.setViewportView(environmentArea);

        environmentArea1.setLineWrap(true);
        environmentArea1.setRows(5);
        environmentScrollPane1.setViewportView(environmentArea1);

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

        javax.swing.GroupLayout attributesSectionPanelLayout = new javax.swing.GroupLayout(attributesSectionPanel);
        attributesSectionPanel.setLayout(attributesSectionPanelLayout);
        attributesSectionPanelLayout.setHorizontalGroup(
            attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, attributesSectionPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(workLogWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(logWorkButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(remainingEstimateWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(timeSpentWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(originalEstimateWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(5, 5, 5)
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(timeSpentLabel)
                            .addComponent(remainingEstimateLabel)
                            .addComponent(originalEstimateLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                                .addComponent(timeSpentField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(timeSpentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                                .addComponent(remainingEstimateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(remainingEstimatePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                                .addComponent(originalEstimateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(originalEstimatePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(summaryWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(environmentWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(originalEstimateNewWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(5, 5, 5)
                                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(originalEstimateLabelNew, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                                    .addComponent(environmentLabel)
                                    .addComponent(summaryLabel)
                                    .addComponent(issueLinksLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                                .addComponent(customFieldPanelLeft, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(79, 79, 79)))
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(environmentScrollPane)
                            .addComponent(summaryField)
                            .addComponent(originalEstimateHint)
                            .addComponent(dummyIssueLinksPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                                .addComponent(originalEstimateFieldNew, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(customFieldPanelRight, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(projectWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(issueTypeWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(statusWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(resolutionWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(priorityWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(5, 5, 5)
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(priorityLabel)
                            .addComponent(resolutionLabel)
                            .addComponent(statusLabel)
                            .addComponent(issueTypeLabel)
                            .addComponent(projectLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(statusCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(issueTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(projectCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(resolutionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(priorityCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(assigneeWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dueWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(assigneeLabel)
                            .addComponent(dueLabel)
                            .addComponent(updatedLabel)
                            .addComponent(createdLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(assigneeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(updatedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(createdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dummyDueField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(assigneeStatusLabel)
                            .addComponent(reporterStatusLabel))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                        .addComponent(componentWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(componentScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(componentLabel))
                        .addGap(18, 18, 18)
                        .addComponent(affectsVersionWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(affectsVersionScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(affectsVersionLabel))
                        .addGap(18, 18, 18)
                        .addComponent(fixVersionWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(fixVersionScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(fixVersionLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 339, Short.MAX_VALUE)))
                .addGap(0, 0, 0))
        );

        attributesSectionPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {issueTypeCombo, priorityCombo, projectCombo, resolutionCombo, statusCombo});

        attributesSectionPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {affectsVersionScrollPane, componentScrollPane, fixVersionScrollPane});

        attributesSectionPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {originalEstimateField, remainingEstimateField, timeSpentField});

        attributesSectionPanelLayout.setVerticalGroup(
            attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(projectLabel)
                            .addComponent(projectCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(createdLabel)
                            .addComponent(createdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(reporterStatusLabel)
                            .addComponent(projectWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(issueTypeLabel)
                            .addComponent(issueTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(updatedLabel)
                            .addComponent(issueTypeWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(updatedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(statusWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(statusLabel)
                            .addComponent(statusCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dueLabel)
                            .addComponent(dueWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dummyDueField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(resolutionWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(resolutionLabel)
                            .addComponent(resolutionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(assigneeWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(assigneeLabel)
                            .addComponent(assigneeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(priorityWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(priorityLabel)
                            .addComponent(priorityCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(componentLabel)
                            .addComponent(componentWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(affectsVersionWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(affectsVersionLabel)
                            .addComponent(fixVersionWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(fixVersionLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(componentScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(affectsVersionScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(fixVersionScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(assigneeStatusLabel))
                .addGap(18, 18, 18)
                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(originalEstimateWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(originalEstimateLabel)
                    .addComponent(originalEstimatePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(originalEstimateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(remainingEstimateWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(remainingEstimateLabel)
                    .addComponent(remainingEstimatePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(remainingEstimateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(timeSpentWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(timeSpentLabel)
                    .addComponent(timeSpentPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(timeSpentField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(workLogWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(logWorkButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dummyIssueLinksPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(issueLinksLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(summaryWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(summaryLabel)
                    .addComponent(summaryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(environmentLabel)
                    .addComponent(environmentScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(environmentWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(customFieldPanelLeft, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(customFieldPanelRight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(originalEstimateFieldNew, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(originalEstimateLabelNew))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(originalEstimateHint, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(originalEstimateNewWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );

        attributesSectionPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {originalEstimateField, originalEstimatePanel});

        attributesSectionPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {timeSpentField, timeSpentPanel});

        attributesSectionPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {remainingEstimateField, remainingEstimatePanel});

        attachmentsSectionPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        dummyAttachmentPanel.setOpaque(false);

        javax.swing.GroupLayout attachmentsSectionPanelLayout = new javax.swing.GroupLayout(attachmentsSectionPanel);
        attachmentsSectionPanel.setLayout(attachmentsSectionPanelLayout);
        attachmentsSectionPanelLayout.setHorizontalGroup(
            attachmentsSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(attachmentsSectionPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(attachmentsWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(dummyAttachmentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        attachmentsSectionPanelLayout.setVerticalGroup(
            attachmentsSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(attachmentsSectionPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(attachmentsSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(attachmentsWarning, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(dummyAttachmentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0))
        );

        subtasksSectionpanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        dummySubtaskPanel.setOpaque(false);

        javax.swing.GroupLayout subtasksSectionpanelLayout = new javax.swing.GroupLayout(subtasksSectionpanel);
        subtasksSectionpanel.setLayout(subtasksSectionpanelLayout);
        subtasksSectionpanelLayout.setHorizontalGroup(
            subtasksSectionpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(subtasksSectionpanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(dummySubtaskPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 696, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        subtasksSectionpanelLayout.setVerticalGroup(
            subtasksSectionpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(subtasksSectionpanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(dummySubtaskPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        newCommentSectionPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        addCommentScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        addCommentArea.setLineWrap(true);
        addCommentScrollPane.setViewportView(addCommentArea);

        javax.swing.GroupLayout newCommentSectionPanelLayout = new javax.swing.GroupLayout(newCommentSectionPanel);
        newCommentSectionPanel.setLayout(newCommentSectionPanelLayout);
        newCommentSectionPanelLayout.setHorizontalGroup(
            newCommentSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(newCommentSectionPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(addCommentWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(addCommentScrollPane)
                .addGap(0, 0, 0))
        );
        newCommentSectionPanelLayout.setVerticalGroup(
            newCommentSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(newCommentSectionPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(newCommentSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addCommentScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addCommentWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );

        privateSectionPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        privateDueDateLabel.setLabelFor(privateDummyDueDateField);
        privateDueDateLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.privateDueDateLabel.text")); // NOI18N
        privateDueDateLabel.setToolTipText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.privateDueDateLabel.TTtext")); // NOI18N

        scheduleDateLabel.setLabelFor(dummyScheduleDateField);
        scheduleDateLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.scheduleDateLabel.text")); // NOI18N
        scheduleDateLabel.setToolTipText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.scheduleDateLabel.TTtext")); // NOI18N

        privateEstimateLabel.setLabelFor(privateEstimateField);
        privateEstimateLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.privateEstimateLabel.text")); // NOI18N
        privateEstimateLabel.setToolTipText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.privateEstimateLabel.TTtext")); // NOI18N

        privateEstimateField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(estimateFormatter));
        privateEstimateField.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.privateEstimateField.text")); // NOI18N

        privateNotesLabel.setLabelFor(privateNotesField);
        privateNotesLabel.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.privateNotesLabel.text")); // NOI18N
        privateNotesLabel.setToolTipText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.privateNotesLabel.TTtext")); // NOI18N

        privateNotesField.setColumns(20);
        privateNotesField.setLineWrap(true);
        privateNotesField.setWrapStyleWord(true);
        jScrollPane1.setViewportView(privateNotesField);

        javax.swing.GroupLayout privateSectionPanelLayout = new javax.swing.GroupLayout(privateSectionPanel);
        privateSectionPanel.setLayout(privateSectionPanelLayout);
        privateSectionPanelLayout.setHorizontalGroup(
            privateSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(privateSectionPanelLayout.createSequentialGroup()
                .addComponent(privateNotesLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1))
            .addGroup(privateSectionPanelLayout.createSequentialGroup()
                .addComponent(privateDueDateLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(privateDummyDueDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(scheduleDateLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dummyScheduleDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(privateEstimateLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(privateEstimateField, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        privateSectionPanelLayout.setVerticalGroup(
            privateSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(privateSectionPanelLayout.createSequentialGroup()
                .addGroup(privateSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(privateDueDateLabel)
                    .addComponent(privateDummyDueDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scheduleDateLabel)
                    .addComponent(dummyScheduleDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(privateEstimateLabel)
                    .addComponent(privateEstimateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(privateSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(privateNotesLabel)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        commentsSectionPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        javax.swing.GroupLayout commentsSectionPanelLayout = new javax.swing.GroupLayout(commentsSectionPanel);
        commentsSectionPanel.setLayout(commentsSectionPanelLayout);
        commentsSectionPanelLayout.setHorizontalGroup(
            commentsSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(dummyCommentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 696, Short.MAX_VALUE)
        );
        commentsSectionPanelLayout.setVerticalGroup(
            commentsSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(dummyCommentPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        headerPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        buttonsPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        refreshButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.refreshButton.text")); // NOI18N
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        separatorLabelRefresh.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        showInBrowserButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.showInBrowserButton.text")); // NOI18N
        showInBrowserButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showInBrowserButtonActionPerformed(evt);
            }
        });

        separatorLabelShowInBrowser.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        submitButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.submitButton.text")); // NOI18N
        submitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitButtonActionPerformed(evt);
            }
        });

        separatorLabelSubmit.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        cancelButton.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        separatorLabelCancel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        btnDeleteTask.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.btnDeleteTask.text")); // NOI18N
        btnDeleteTask.setToolTipText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.btnDeleteTask.TTtext")); // NOI18N
        btnDeleteTask.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteTaskActionPerformed(evt);
            }
        });

        separatorLabelDismiss.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        btnMoreActions.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.btnMoreActions.text")); // NOI18N
        btnMoreActions.setToolTipText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.btnMoreActions.toolTipText")); // NOI18N
        btnMoreActions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoreActionsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout buttonsPanelLayout = new javax.swing.GroupLayout(buttonsPanel);
        buttonsPanel.setLayout(buttonsPanelLayout);
        buttonsPanelLayout.setHorizontalGroup(
            buttonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separatorLabelRefresh)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(showInBrowserButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separatorLabelShowInBrowser)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(submitButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separatorLabelSubmit)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separatorLabelCancel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDeleteTask, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separatorLabelDismiss)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnMoreActions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        buttonsPanelLayout.setVerticalGroup(
            buttonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonsPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(buttonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(separatorLabelRefresh, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(showInBrowserButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(separatorLabelShowInBrowser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(submitButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(separatorLabelSubmit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(separatorLabelCancel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnDeleteTask, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(separatorLabelDismiss, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnMoreActions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        javax.swing.GroupLayout headerPanelLayout = new javax.swing.GroupLayout(headerPanel);
        headerPanel.setLayout(headerPanelLayout);
        headerPanelLayout.setHorizontalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(parentHeaderPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(headerLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18))
        );
        headerPanelLayout.setVerticalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(parentHeaderPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addGroup(headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(headerLabel)
                    .addComponent(buttonsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        mainScrollPane.setBorder(null);

        mainPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        attributesSection.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));
        attributesSection.setContent(attributesSectionPanel);
        attributesSection.setLabel(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.attributesSection.label")); // NOI18N

        attachmentsSection.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));
        attachmentsSection.setActions(getAttachmentsSectionActions());
        attachmentsSection.setContent(attachmentsSectionPanel);
        attachmentsSection.setExpanded(false);
        attachmentsSection.setLabel(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.attachmentsLabel.text", 0)); // NOI18N

        subtasksSection.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));
        subtasksSection.setActions(getSubtasksSectionActions());
        subtasksSection.setContent(subtasksSectionpanel);
        subtasksSection.setExpanded(false);
        subtasksSection.setLabel(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.subtasksSection.label", 0)); // NOI18N

        commentsSection.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));
        commentsSection.setActions(getCommentsSectionActions());
        commentsSection.setContent(commentsSectionPanel);
        commentsSection.setLabel(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.commentsSection.label", 0)); // NOI18N

        privateSection.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));
        privateSection.setActions(getPrivateSectionActions());
        privateSection.setContent(privateSectionPanel);
        privateSection.setExpanded(false);
        privateSection.setLabel(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.privateSection.label")); // NOI18N

        newCommentSection.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));
        newCommentSection.setContent(newCommentSectionPanel);
        newCommentSection.setLabel(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.newCommentsSection.label")); // NOI18N

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(attributesSection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(attachmentsSection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(subtasksSection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(commentsSection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(privateSection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(newCommentSection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(attributesSection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(attachmentsSection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(subtasksSection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(privateSection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(newCommentSection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(commentsSection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        mainScrollPane.setViewportView(mainPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(headerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(mainScrollPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(headerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(mainScrollPane)
                .addGap(0, 0, 0))
        );
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
                        if(issueTypes == null) {
                            issueTypes = new IssueType[0];
                        }
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
                        subtasksSection.setVisible(!subtask && anySubtaskType);

                        // Reload components
                        DefaultListModel componentModel = new DefaultListModel();
                        for (org.netbeans.modules.jira.client.spi.Component component : config.getComponents(project)) {
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
        GroupLayout layout = (GroupLayout) attributesSectionPanel.getLayout();
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

    private void logWorkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logWorkButtonActionPerformed
        WorkLogPanel panel = new WorkLogPanel(issue);
        NbJiraIssue.NewWorkLog log = panel.showDialog();
        if (log != null) {
            issue.addWorkLog(log);
            unsavedFields.add(WORKLOG);
            updateWorkLogStatus();
        }
    }//GEN-LAST:event_logWorkButtonActionPerformed

    @NbBundle.Messages({
        "# {0} - parent task description", "MSG_IssuePanel.creatingSubtask=Creating subtask of {0}"
    })
    private void createSubtaskButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                    
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
    }                                                   

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
        discardUnsavedChanges();
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

    private void btnMoreActionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoreActionsActionPerformed
        JPopupMenu popup = new JPopupMenu();
        for (Action a : getMoreActions()) {
            if (a.isEnabled()) {
                popup.add(a);
            }
        }
        Point loc = new Point(
                btnMoreActions.getLocation().x,
                btnMoreActions.getLocation().y + btnMoreActions.getSize().height + 1);
        popup.show(buttonsPanel, loc.x, loc.y);
    }//GEN-LAST:event_btnMoreActionsActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea addCommentArea;
    private javax.swing.JScrollPane addCommentScrollPane;
    private javax.swing.JLabel addCommentWarning;
    private javax.swing.JLabel affectsVersionLabel;
    private javax.swing.JList affectsVersionList;
    private javax.swing.JScrollPane affectsVersionScrollPane;
    private javax.swing.JLabel affectsVersionWarning;
    private javax.swing.JComboBox assigneeCombo;
    private javax.swing.JTextField assigneeField;
    private javax.swing.JLabel assigneeLabel;
    private javax.swing.JLabel assigneeStatusLabel;
    private javax.swing.JLabel assigneeWarning;
    private org.netbeans.modules.bugtracking.commons.CollapsibleSectionPanel attachmentsSection;
    private javax.swing.JPanel attachmentsSectionPanel;
    private javax.swing.JLabel attachmentsWarning;
    private org.netbeans.modules.bugtracking.commons.CollapsibleSectionPanel attributesSection;
    private javax.swing.JPanel attributesSectionPanel;
    private org.netbeans.modules.bugtracking.commons.LinkButton btnDeleteTask;
    private org.netbeans.modules.bugtracking.commons.LinkButton btnMoreActions;
    private javax.swing.JPanel buttonsPanel;
    private org.netbeans.modules.bugtracking.commons.LinkButton cancelButton;
    private org.netbeans.modules.bugtracking.commons.CollapsibleSectionPanel commentsSection;
    private javax.swing.JPanel commentsSectionPanel;
    private javax.swing.JLabel componentLabel;
    private javax.swing.JList componentList;
    private javax.swing.JScrollPane componentScrollPane;
    private javax.swing.JLabel componentWarning;
    private javax.swing.JTextField createdField;
    private javax.swing.JLabel createdLabel;
    private javax.swing.JPanel customFieldPanelLeft;
    private javax.swing.JPanel customFieldPanelRight;
    private javax.swing.JLabel dueLabel;
    private javax.swing.JLabel dueWarning;
    private javax.swing.JPanel dummyAttachmentPanel;
    private javax.swing.JPanel dummyCommentPanel;
    private javax.swing.JFormattedTextField dummyDueField;
    private javax.swing.JPanel dummyIssueLinksPanel;
    private javax.swing.JTextField dummyScheduleDateField;
    private javax.swing.JPanel dummySubtaskPanel;
    private javax.swing.JTextArea environmentArea;
    private javax.swing.JTextArea environmentArea1;
    private javax.swing.JLabel environmentLabel;
    private javax.swing.JScrollPane environmentScrollPane;
    private javax.swing.JScrollPane environmentScrollPane1;
    private javax.swing.JLabel environmentWarning;
    private javax.swing.JLabel fixVersionLabel;
    private javax.swing.JList fixVersionList;
    private javax.swing.JScrollPane fixVersionScrollPane;
    private javax.swing.JLabel fixVersionWarning;
    private javax.swing.JLabel headerLabel;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JLabel issueLinksLabel;
    private javax.swing.JComboBox issueTypeCombo;
    private javax.swing.JLabel issueTypeLabel;
    private javax.swing.JLabel issueTypeWarning;
    private javax.swing.JScrollPane jScrollPane1;
    private org.netbeans.modules.bugtracking.commons.LinkButton logWorkButton2;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JScrollPane mainScrollPane;
    private org.netbeans.modules.bugtracking.commons.SectionPanel newCommentSection;
    private javax.swing.JPanel newCommentSectionPanel;
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
    private javax.swing.JLabel privateDueDateLabel;
    private javax.swing.JTextField privateDummyDueDateField;
    private javax.swing.JFormattedTextField privateEstimateField;
    private javax.swing.JLabel privateEstimateLabel;
    private javax.swing.JTextArea privateNotesField;
    private javax.swing.JLabel privateNotesLabel;
    private org.netbeans.modules.bugtracking.commons.CollapsibleSectionPanel privateSection;
    private javax.swing.JPanel privateSectionPanel;
    private javax.swing.JComboBox projectCombo;
    private javax.swing.JTextField projectField;
    private javax.swing.JLabel projectLabel;
    private javax.swing.JLabel projectWarning;
    private org.netbeans.modules.bugtracking.commons.LinkButton refreshButton;
    private javax.swing.JTextField remainingEstimateField;
    private javax.swing.JLabel remainingEstimateLabel;
    private javax.swing.JPanel remainingEstimatePanel;
    private javax.swing.JLabel remainingEstimateWarning;
    private javax.swing.JLabel reporterStatusLabel;
    private javax.swing.JComboBox resolutionCombo;
    private javax.swing.JTextField resolutionField;
    private javax.swing.JLabel resolutionLabel;
    private javax.swing.JLabel resolutionWarning;
    private javax.swing.JLabel scheduleDateLabel;
    private javax.swing.JLabel separatorLabelCancel;
    private javax.swing.JLabel separatorLabelDismiss;
    private javax.swing.JLabel separatorLabelRefresh;
    private javax.swing.JLabel separatorLabelShowInBrowser;
    private javax.swing.JLabel separatorLabelSubmit;
    private org.netbeans.modules.bugtracking.commons.LinkButton showInBrowserButton;
    private javax.swing.JComboBox statusCombo;
    private javax.swing.JTextField statusField;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JLabel statusWarning;
    private org.netbeans.modules.bugtracking.commons.LinkButton submitButton;
    private org.netbeans.modules.bugtracking.commons.CollapsibleSectionPanel subtasksSection;
    private javax.swing.JPanel subtasksSectionpanel;
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
        restoreSections();
        enableComponents(false);
        issue.opened();
    }
    
    void closed() {
        open = false;
        if(issue != null) {
            commentsPanel.storeSettings();
            persistSections();
            issue.closed();
        }
    }
    
    private void persistSections () {
        if (!issue.isNew()) {
            JiraConfig config = JiraConfig.getInstance();
            String repositoryId = issue.getRepository().getID();
            String taskId = issue.getID();
            config.setEditorSectionCollapsed(repositoryId, taskId, SECTION_ATTRIBUTES, !attributesSection.isExpanded());
            config.setEditorSectionCollapsed(repositoryId, taskId, SECTION_ATTACHMENTS, !attachmentsSection.isExpanded());
            config.setEditorSectionCollapsed(repositoryId, taskId, SECTION_SUBTASKS, !subtasksSection.isExpanded());
            config.setEditorSectionCollapsed(repositoryId, taskId, SECTION_COMMENTS, !commentsSection.isExpanded());
            config.setEditorSectionCollapsed(repositoryId, taskId, SECTION_PRIVATE, !privateSection.isExpanded());
        }
    }

    private void restoreSections () {
        if (!issue.isNew()) {
            JiraConfig config = JiraConfig.getInstance();
            String repositoryId = issue.getRepository().getID();
            String taskId = issue.getID();
            attributesSection.setExpanded(!config.isEditorSectionCollapsed(repositoryId, taskId, SECTION_ATTRIBUTES, false));
            attachmentsSection.setExpanded(!config.isEditorSectionCollapsed(repositoryId, taskId, SECTION_ATTACHMENTS, true));
            subtasksSection.setExpanded(!config.isEditorSectionCollapsed(repositoryId, taskId, SECTION_SUBTASKS, true));
            commentsSection.setExpanded(!config.isEditorSectionCollapsed(repositoryId, taskId, SECTION_COMMENTS, false));
            privateSection.setExpanded(!config.isEditorSectionCollapsed(repositoryId, taskId, SECTION_PRIVATE, true));
        }
    }
    
    boolean saveChanges () {
        skipReload = true;
        enableComponents(false);
        final AtomicBoolean retval = new AtomicBoolean(true);
        Runnable outOfAWT = new Runnable() {
            @Override
            public void run () {
                retval.set(false);
                try {
                    retval.set(issue.save());
                } finally {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            unsavedFields.clear();
                            enableComponents(true);
                            updateFieldStatuses();
                            cancelButton.setEnabled(issue.hasLocalEdits());
                            skipReload = false;
                        }
                    });
                }
            }
        };
        if (EventQueue.isDispatchThread()) {
            RP.post(outOfAWT);
            return true;
        } else {
            outOfAWT.run();
            return retval.get();
        }
    }

    boolean discardUnsavedChanges () {
        issue.clearUnsavedChanges();
        unsavedFields.clear();
        reloadForm(false);
        return true;
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
                addCommentArea, IssueField.COMMENT, addCommentWarning, newCommentSection.getLabelComponent()));
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
        dueDatePicker.addChangeListener(new FieldChangeListener(dueDatePicker.getComponent(), IssueField.DUE, dueWarning, dueLabel,
                Pair.of(IssueField.DUE, dueDatePicker)) {

            @Override
            void fieldModified () {
                if (!reloading && isEnabled()) {
                    Date date = dueDatePicker.getDate();
                    String value = date == null ? "" : String.valueOf(date.getTime());
                    if (!issue.getFieldValue(IssueField.DUE).equals(value)) {
                        unsavedFields.add(IssueField.DUE.getKey());
                        issue.setFieldValue(IssueField.DUE, value);
                        updateDecorations();
                    }
                }
            }

        });
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
        privateNotesField.getDocument().addDocumentListener(new TaskAttributeListener(privateNotesField,
                ATTRIBUTE_PRIVATE_NOTES, privateNotesLabel) {

            @Override
            protected boolean storeValue () {
                issue.setTaskPrivateNotes(privateNotesField.getText());
                return true;
            }
        });
        privateDueDatePicker.addChangeListener(new DatePickerListener(privateDueDatePicker.getComponent(),
                ATTRIBUTE_DUE_DATE, privateDueDateLabel) {

            @Override
            protected boolean storeValue () {
                issue.setTaskDueDate(privateDueDatePicker.getDate(), false);
                return true;
            }
        });
        scheduleDatePicker.addChangeListener(new DatePickerListener(scheduleDatePicker.getComponent(),
                ATTRIBUTE_SCHEDULE_DATE, scheduleDateLabel) {

            @Override
            protected boolean storeValue () {
                issue.setTaskScheduleDate(scheduleDatePicker.getScheduleDate(), false);
                return true;
            }
        });
        privateEstimateField.getDocument().addDocumentListener(new TaskAttributeListener(privateEstimateField,
                ATTRIBUTE_ESTIMATE, privateEstimateLabel) {

            @Override
            protected boolean storeValue () {
                int value = ((Number) privateEstimateField.getValue()).intValue();
                if (value != issue.getEstimate()) {
                    issue.setTaskEstimate(value, false);
                    return true;
                } else {
                    return false;
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
                    value = DUE_DATE_FORMAT.format(date);
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
            AbstractButton attachmentLabel = attachmentsSection.getLabelComponent();
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
    
    private class FieldChangeListener implements DocumentListener, ActionListener,
            ListSelectionListener, ChangeListener {
        private final IssueField field;
        private final JComponent component;
        private final JLabel warningLabel;
        private final JComponent fieldLabel;
        private final String fieldName;
        private Pair<IssueField, ? extends Object>[] decoratedFields;

        public FieldChangeListener (JComponent component, IssueField field) {
            this(component, field, null, null);
        }

        public FieldChangeListener (JComponent component, IssueField field, JLabel warningLabel,
                JComponent fieldLabel) {
            this(component, field, warningLabel, fieldLabel, Pair.of(field, component));
        }
        
        public FieldChangeListener (JComponent component, IssueField field, JLabel warningLabel,
                JComponent fieldLabel, Pair<IssueField, ? extends Object>... multiField) {
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

        @Override
        public void stateChanged (ChangeEvent e) {
            fieldModified();
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
    
    private abstract class TaskAttributeListener implements DocumentListener {

        private final String attributeName;
        private final JComponent component;
        private final JComponent fieldLabel;

        public TaskAttributeListener (JComponent component, String attributeName, JComponent fieldLabel) {
            this.component = component;
            this.attributeName = attributeName;
            this.fieldLabel = fieldLabel;
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

        void fieldModified () {
            if (!reloading && isEnabled() && storeValue()) {
                unsavedFields.add(attributeName);
                updateDecorations();
            }
        }

        public boolean isEnabled () {
            return component.isVisible() && component.isEnabled();
        }

        protected final void updateDecorations () {
            updateFieldDecorations(attributeName, fieldLabel);
        }

        protected abstract boolean storeValue ();
    }

    private abstract class DatePickerListener implements ChangeListener {

        private final String attributeName;
        private final JComponent component;
        private final JComponent fieldLabel;

        public DatePickerListener (JComponent component,
                String attributeName, JComponent fieldLabel) {
            this.component = component;
            this.attributeName = attributeName;
            this.fieldLabel = fieldLabel;
        }

        void fieldModified () {
            if (!reloading && isEnabled() && storeValue()) {
                unsavedFields.add(attributeName);
                updateDecorations();
            }
        }

        @Override
        public void stateChanged (ChangeEvent e) {
            fieldModified();
        }
        
        public boolean isEnabled () {
            return component.isVisible() && component.isEnabled();
        }

        protected final void updateDecorations () {
            updateFieldDecorations(attributeName, fieldLabel);
        }

        protected abstract boolean storeValue ();
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
        assert fieldLabel instanceof JLabel || fieldLabel instanceof AbstractButton;
        String txt;
        if(fieldLabel instanceof JLabel) {
            txt = ((JLabel) fieldLabel).getText().trim();
            
        } else if (fieldLabel instanceof AbstractButton) {
            txt = ((AbstractButton) fieldLabel).getText().trim();
        } else {
            return null;
        }
        if (txt.endsWith(":")) { // NOI18N
            txt = txt.substring(0, txt.length()-1);
        }
        return txt;
    }
    
    @NbBundle.Messages("IssuePanel.reloadButton.text=Reload Attributes")
    private Action[] getAttributesSectionActions () {
        if (attributesSectionActions == null) {
            attributesSectionActions = new Action[] {
//                new AbstractAction(Bundle.IssuePanel_reloadButton_text()) {
//                
//                    @Override
//                    public void actionPerformed (ActionEvent e) {
//                        reloadButtonActionPerformed(e);
//                    }
//                }
            };
        }
        return attributesSectionActions;
    }
    
    @NbBundle.Messages({
        "IssuePanel.commentsSectionAction.collapse.text=Collapse All",
        "IssuePanel.commentsSectionAction.expand.text=Expand All"
    })
    private Action[] getCommentsSectionActions () {
        if (commentsSectionActions == null) {
            commentsSectionActions = new Action[] {
                new AbstractAction(Bundle.IssuePanel_commentsSectionAction_collapse_text()) {
                
                    @Override
                    public void actionPerformed (ActionEvent e) {
                        commentsPanel.collapseAll();
                        commentsSection.setExpanded(false);
                    }
                },
                new AbstractAction(Bundle.IssuePanel_commentsSectionAction_expand_text()) {
                
                    @Override
                    public void actionPerformed (ActionEvent e) {
                        commentsPanel.expandAll();
                        commentsSection.setExpanded(true);
                    }
                }
            };
        }
        return commentsSectionActions;
    }
    
    @NbBundle.Messages({
        "CTL_Attachment.action.create=Add Attachment",
        "CTL_Attachment.action.attachLog=Attach Log"
    })
    private Action[] getAttachmentsSectionActions () {
        if (attachmentsSectionActions == null) {
            attachmentsSectionActions = new Action[] {
                new AbstractAction(Bundle.CTL_Attachment_action_create()) {

                    @Override
                    public void actionPerformed (ActionEvent e) {
                        attachmentsSection.setExpanded(true);
                        EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run () {
                                attachmentsPanel.createAttachment();
                            }
                        });
                    }
                }
            };
        }
        return attachmentsSectionActions;
    }
    
    @NbBundle.Messages("IssuePanel.addToCategory.text=Add to Category")
    private Action[] getPrivateSectionActions () {
        if (privateSectionActions == null) {
            privateSectionActions = new Action[] {
                new AbstractAction(Bundle.IssuePanel_addToCategory_text()) {
                    @Override
                    public void actionPerformed (ActionEvent e) {
                        Jira.getInstance().getBugtrackingFactory().addToCategory(issue.getRepository(), issue);
                    }
                }
            };
        }
        return privateSectionActions;
    }
    
    @NbBundle.Messages("IssuePanel.createSubtaskButton.text=Create Sub-task")
    private Action[] getSubtasksSectionActions () {
        if (subtasksSectionActions == null) {
            subtasksSectionActions = new Action[] {
                new AbstractAction(Bundle.IssuePanel_createSubtaskButton_text()) {
                    @Override
                    public void actionPerformed (ActionEvent e) {
                        createSubtaskButtonActionPerformed(e);
                    }
                }
            };
        }
        return subtasksSectionActions;
    }

    @NbBundle.Messages({
        "IssuePanel.action.startProgress.name=Start Progress",
        "# {0} - task id", "IssuePanel.startProgressMessage=Task {0} - Starting Progress"
    })
    private final Action startProgressAction = new AbstractAction(Bundle.IssuePanel_action_startProgress_name()) {
        
        @Override
        public void actionPerformed (ActionEvent e) {
            String message = Bundle.IssuePanel_startProgressMessage(issue.getKey());
            submitChange(new Runnable() {
                @Override
                public void run() {
                    issue.startProgress();
                    issue.submitAndRefresh();
                }
            }, message);
        }
    };

    @NbBundle.Messages({
        "IssuePanel.action.stopProgress.name=Stop Progress",
        "# {0} - task id", "IssuePanel.stopProgressMessage=Task {0} - Stopping Progress"
    })
    private final Action stopProgressAction = new AbstractAction(Bundle.IssuePanel_action_stopProgress_name()) {
        
        @Override
        public void actionPerformed (ActionEvent e) {
            String message = Bundle.IssuePanel_stopProgressMessage(issue.getKey());
            submitChange(new Runnable() {
                @Override
                public void run() {
                    issue.stopProgress();
                    issue.submitAndRefresh();
                }
            }, message);
        }
    };

    @NbBundle.Messages({
        "IssuePanel.action.reopenIssue.name=Reopen Task",
        "# {0} - task id", "IssuePanel.reopenIssueMessage=Reopening task {0}"
    })
    private final Action reopenIssueAction = new AbstractAction(Bundle.IssuePanel_action_reopenIssue_name()) {
        
        @Override
        public void actionPerformed (ActionEvent e) {
            String message = Bundle.IssuePanel_reopenIssueMessage(issue.getKey());
            submitChange(new Runnable() {
                @Override
                public void run() {
                    issue.reopen(null);
                    issue.submitAndRefresh();
                }
            }, message);
        }
    };

    @NbBundle.Messages({
        "IssuePanel.action.closeIssue.name=Close Task", 
        "# {0} - task id", "IssuePanel.closeIssueMessage=Closing task {0}"
    })
    private final Action closeIssueAction = new AbstractAction(Bundle.IssuePanel_action_closeIssue_name()) {
        
        @Override
        public void actionPerformed (ActionEvent e) {
            ResolveIssuePanel panel = new ResolveIssuePanel(issue);
            String resolution = issue.getFieldValue(NbJiraIssue.IssueField.RESOLUTION);
            final Resolution newResolution;
            if ((resolution == null) || resolution.trim().equals("")) { // NOI18N
                String title = Bundle.IssuePanel_action_closeIssue_name();
                if (JiraUtils.show(panel, title, title, new HelpCtx(panel.getClass()))) {
                    newResolution = panel.getSelectedResolution();
                } else {
                    newResolution = null;
                }
            } else {
                newResolution = issue.getRepository().getConfiguration().getResolutionById(resolution);
            }
            if (newResolution != null) {
                String message = Bundle.IssuePanel_closeIssueMessage(issue.getKey());
                final String comment = panel.getComment();
                submitChange(new Runnable() {
                    @Override
                    public void run() {
                        issue.close(newResolution, comment);
                        issue.submitAndRefresh();
                    }
                }, message);
            }
        }
    };

    @NbBundle.Messages({
        "IssuePanel.action.resolveIssue.name=Resolve Task",
        "# {0} - task id", "IssuePanel.resolveIssueMessage=Resolving task {0}"
    })
    private final Action resolveIssueAction = new AbstractAction(Bundle.IssuePanel_action_resolveIssue_name()) {
        
        @Override
        public void actionPerformed (ActionEvent e) {
            ResolveIssuePanel panel = new ResolveIssuePanel(issue);
            String title = Bundle.IssuePanel_action_resolveIssue_name();
            if (JiraUtils.show(panel, title, title, new HelpCtx(panel.getClass()))) {
                String message = Bundle.IssuePanel_resolveIssueMessage(issue.getKey());
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
        }
    };
    
    private Action[] getMoreActions () {
        if (moreActions == null) {
            moreActions = new Action[] {
                startProgressAction,
                stopProgressAction,
                reopenIssueAction,
                resolveIssueAction,
                closeIssueAction
            };
        }
        return moreActions;
    }

    class CancelHighlightListener implements DocumentListener, ActionListener, ListSelectionListener, ChangeListener {
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

        @Override
        public void stateChanged (ChangeEvent e) {
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
    
    private class UnsavedFieldSet extends HashSet<String> {

        @Override
        public boolean add (String value) {
            boolean added = super.add(value);
            if (added) {
                issue.fireChangeEvent();
            }
            return added;
        }

        @Override
        public boolean remove (Object o) {
            boolean removed = super.remove(o);
            if (removed && isEmpty()) {
                issue.fireChangeEvent();
            }
            return removed;
        }

        @Override
        public void clear () {
            boolean fire = !isEmpty();
            super.clear();
            if (fire) {
                issue.fireChangeEvent();
            }
        }
        
    }

}
