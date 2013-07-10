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

package org.netbeans.modules.bugzilla.issue;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
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
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaVersion;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.team.spi.OwnerInfo;
import org.netbeans.modules.bugtracking.team.spi.RepositoryUser;
import org.netbeans.modules.bugtracking.util.RepositoryUserRenderer;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.team.spi.TeamUtil;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.netbeans.modules.bugtracking.util.*;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.BugzillaConfig;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue.Attachment;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue.Comment;
import org.netbeans.modules.bugzilla.kenai.KenaiRepository;
import org.netbeans.modules.bugzilla.repository.BugzillaConfiguration;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.bugzilla.repository.CustomIssueField;
import org.netbeans.modules.bugzilla.repository.IssueField;
import org.netbeans.modules.bugzilla.util.BugzillaConstants;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
import org.netbeans.modules.bugzilla.util.NbBugzillaConstants;
import org.netbeans.modules.spellchecker.api.Spellchecker;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Panel showing (and allowing to edit) details of an issue.
 *
 * @author Jan Stola
 */
public class IssuePanel extends javax.swing.JPanel implements Scrollable {
    private static Color incomingChangesColor = null;
    private static Color outgoingChangesColor = null;
    private static final RequestProcessor RP = new RequestProcessor("Bugzilla Issue Panel", 5, false); // NOI18N
    private static final String YYYY_MM_DD = NbBundle.getMessage(IssuePanel.class, "IssuePanel.deadlineField.text");
    private BugzillaIssue issue;
    private CommentsPanel commentsPanel;
    private AttachmentsPanel attachmentsPanel;
    private int resolvedIndex;
    private List<String> keywords = new LinkedList<String>();
    private boolean reloading;
    private boolean skipReload;
    private boolean usingTargetMilestones;
    private OwnerInfo ownerInfo;
    private String assignee = null;
    private UndoRedoSupport undoRedoSupport;
    private final Set<IssueField> unsavedFields = new HashSet<IssueField>();

    static {
        incomingChangesColor = UIManager.getColor( "nb.bugtracking.label.highlight" ); //NOI18N
        if( null == incomingChangesColor ) {
            incomingChangesColor = new Color(217, 255, 217);
        }
        outgoingChangesColor = UIManager.getColor( "nb.bugtracking.comment.background" ); //NOI18N
        if (null == outgoingChangesColor) {
            outgoingChangesColor = new Color(0xf3f6fd);
        }
    }
    private boolean initializingProduct;
    
    public IssuePanel() {
        initComponents();
        updateReadOnlyField(reportedField);
        updateReadOnlyField(modifiedField);
        updateReadOnlyField(resolutionField);
        updateReadOnlyField(productField);
        updateReadOnlyField(headerField);
        messagePanel.setBackground(getBackground());
        customFieldsPanelLeft.setBackground(getBackground());
        customFieldsPanelRight.setBackground(getBackground());
        Font font = reportedLabel.getFont();
        headerField.setFont(font.deriveFont((float)(font.getSize()*1.7)));
        duplicateLabel.setVisible(false);
        duplicateField.setVisible(false);
        duplicateButton.setVisible(false);
        attachDocumentListeners();
        attachHideStatusListener();
        addCommentArea.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                makeCaretVisible(addCommentArea);
            }
        });

        // A11Y - Issues 163597 and 163598
        UIUtils.fixFocusTraversalKeys(addCommentArea);

        // Comments panel
        commentsPanel = new CommentsPanel();
        commentsPanel.setNewCommentHandler(new CommentsPanel.NewCommentHandler() {
            @Override
            public void append(String text) {
                addCommentArea.append(text);
                addCommentArea.requestFocus();
                scrollRectToVisible(scrollPane1.getBounds());
            }
        });
        attachmentsPanel = new AttachmentsPanel(this);
        GroupLayout layout = (GroupLayout)getLayout();
        layout.replace(dummyCommentsPanel, commentsPanel);
        layout.replace(dummyAttachmentsPanel, attachmentsPanel);
        layout.replace(dummyTimetrackingPanel, timetrackingPanel);
        attachmentsLabel.setLabelFor(attachmentsPanel);
        initSpellChecker();
        initDefaultButton();

        UIUtils.issue163946Hack(scrollPane1);
    }

    private void initDefaultButton() {
        if(Boolean.getBoolean("bugtracking.suppressActionKeys")) {
            return;
        }
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
        Caret caret = field.getCaret();
        if (caret instanceof DefaultCaret) {
            ((DefaultCaret)caret).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        }
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
            if(evt.getSource() != IssuePanel.this.issue) {
                return;
            }
            if (IssueStatusProvider.EVENT_SEEN_CHANGED.equals(evt.getPropertyName())) {
                Mutex.EVENT.readAccess(new Runnable() {
                    @Override
                    public void run () {
                        updateFieldStatuses();
                    }
                });
            }
        }
    };

    BugzillaIssue getIssue() {
        return issue;
    }

    void modelStateChanged (final boolean isDirty, final boolean isModified) {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run () {
                if (!reloading && isDirty && issue.isMarkedNewUnread()) {
                    issue.markNewRead();
                }
                btnSaveChanges.setEnabled(isDirty);
                if (!isDirty) {
                    unsavedFields.clear();
                }
                cancelButton.setEnabled(isModified);
            }
        });
    }

    public void setIssue(BugzillaIssue issue) {
        assert SwingUtilities.isEventDispatchThread() : "Accessing Swing components. Do not call outside event-dispatch thread!"; // NOI18N
        if (this.issue == null) {
            issue.removePropertyChangeListener(cacheListener);
            issue.addPropertyChangeListener(cacheListener);

            summaryField.getDocument().addDocumentListener(new DocumentListener() {
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
                    updateNoSummary();
                }
            });
            keywordsField.getDocument().addDocumentListener(new DocumentListener() {
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
                    updateInvalidKeyword();
                }
            });
        }
        this.issue = issue;
        initCombos();
        initCustomFields();
        List<String> kws = issue.getRepository().getConfiguration().getKeywords();
        keywords.clear();
        for (String keyword : kws) {
            keywords.add(keyword.toUpperCase());
        }
        setupListeners();
        boolean showQAContact = BugzillaUtil.showQAContact(issue.getRepository());
        if (qaContactLabel.isVisible() != showQAContact) {
            GroupLayout layout = (GroupLayout)getLayout();
            JLabel temp = new JLabel();
            swap(layout, ccLabel, qaContactLabel, temp);
            swap(layout, ccField, qaContactField, temp);
            qaContactLabel.setVisible(showQAContact);
            qaContactField.setVisible(showQAContact);
        }
        boolean showStatusWhiteboard = BugzillaUtil.showStatusWhiteboard(issue.getRepository());
        statusWhiteboardLabel.setVisible(showStatusWhiteboard);
        statusWhiteboardField.setVisible(showStatusWhiteboard);
        statusWhiteboardWarning.setVisible(showStatusWhiteboard);
        boolean showIssueType = BugzillaUtil.showIssueType(issue.getRepository());
        issueTypeLabel.setVisible(false);
        issueTypeCombo.setVisible(showIssueType);
        issueTypeWarning.setVisible(showIssueType);
        severityCombo.setVisible(!showIssueType);
        severityWarning.setVisible(!showIssueType);
        // Replace severity by issue-type
        if (showIssueType) {
            GroupLayout layout = (GroupLayout)getLayout();
            JLabel temp = new JLabel();
            swap(layout, severityCombo, issueTypeCombo, temp);
            swap(layout, severityWarning, issueTypeWarning, temp);
        }
                
        if (issue.isNew()) {
            if(NBBugzillaUtils.isNbRepository(issue.getRepository().getUrl())) {
                ownerInfo = issue.getOwnerInfo();
                if(ownerInfo == null) {
                    // XXX not sure why we need this - i'm going to keep it for now,
                    // doesn't seem to harm
                    Node[] selection = WindowManager.getDefault().getRegistry().getActivatedNodes();
                    ownerInfo = issue.getRepository().getOwnerInfo(selection);
                }
            }
        }

        // Hack to "link" the width of both columns
        Dimension dim = ccField.getPreferredSize();
        int width1 = Math.max(osCombo.getPreferredSize().width, platformCombo.getPreferredSize().width);
        int width2 = Math.max(priorityCombo.getPreferredSize().width, showIssueType ? issueTypeCombo.getPreferredSize().width : severityCombo.getPreferredSize().width);
        int gap = LayoutStyle.getInstance().getPreferredGap(osCombo, platformCombo, LayoutStyle.ComponentPlacement.RELATED, SwingConstants.EAST, this);
        ccField.setPreferredSize(new Dimension(2*Math.max(width1,width2)+gap,dim.height));
    }

    private void selectProduct() {
        initializingProduct = true;
        if (ownerInfo != null) {
            String owner = findInModel(productCombo, ownerInfo.getOwner());
            productCombo.setSelectedItem(owner);
            List<String> data = ownerInfo.getExtraData();
            if (data != null && data.size() > 0) {
                String component = findInModel(componentCombo, data.get(0));
                selectInCombo(componentCombo, component, true);
            }
        } else {
            BugzillaRepository repository = issue.getRepository();
            if (repository instanceof KenaiRepository) {
                String productName = ((KenaiRepository)repository).getProductName();
                productCombo.setSelectedItem(productName);
            } else if (BugzillaUtil.isNbRepository(repository)) {
                // IssueProvider 181224
                String defaultProduct = "ide"; // NOI18N
                String defaultComponent = "Code"; // NOI18N
                productCombo.setSelectedItem(defaultProduct);
                componentCombo.setSelectedItem(defaultComponent);
            } else {
                productCombo.setSelectedIndex(0);
            }
        }
        storeFieldValueForNewIssue(IssueField.COMPONENT, componentCombo);
        initializingProduct = false;
    }

    private String findInModel(JComboBox combo, String value) {
        ComboBoxModel model = combo.getModel();
        for(int i = 0; i < model.getSize(); i++) {
            String element = model.getElementAt(i).toString();
            if(value.toLowerCase().equals(element.toString().toLowerCase())) {
                return element;
            }
        }
        return null;
    }

    private static void swap(GroupLayout layout, JComponent comp1, JComponent comp2, JComponent temp) {
        layout.replace(comp1, temp);
        layout.replace(comp2, comp1);
        layout.replace(temp, comp2);
    }

    private int oldCommentCount;
    void reloadForm(boolean force) {
        if (skipReload) {
            return;
        }
        enableComponents(true);
        fieldWarnings.clear();
        fieldErrors.clear();
        reloading = true;

        boolean isNew = issue.isNew();
        boolean showProductCombo = isNew || !(issue.getRepository() instanceof KenaiRepository) || NBBugzillaUtils.isNbRepository(issue.getRepository().getUrl());
        boolean hasTimeTracking = !isNew && issue.hasTimeTracking();
        GroupLayout layout = (GroupLayout)getLayout();
        if (showProductCombo) {
            if (productCombo.getParent() == null) {
                layout.replace(productField, productCombo);
            }
        } else {
            if (productField.getParent() == null) {
                layout.replace(productCombo, productField);
            }
        }
        productLabel.setLabelFor(isNew ? productCombo : productField);
        boolean isNetbeans = NBBugzillaUtils.isNbRepository(issue.getRepository().getUrl());
        if(isNew && isNetbeans) {
            attachLogCheckBox.setVisible(true);
            viewLogButton.setVisible(true);
            attachLogCheckBox.setSelected(BugzillaConfig.getInstance().getAttachLogFile());
        } else {
            attachLogCheckBox.setVisible(false);
            viewLogButton.setVisible(false);
        }
        switchViewLog();
        headerField.setVisible(!isNew);
        statusCombo.setEnabled(!isNew);
        org.openide.awt.Mnemonics.setLocalizedText(addCommentLabel, NbBundle.getMessage(IssuePanel.class, isNew ? "IssuePanel.description" : "IssuePanel.addCommentLabel.text")); // NOI18N
        reportedLabel.setVisible(!isNew);
        reportedField.setVisible(!isNew);
        modifiedLabel.setVisible(!isNew);
        modifiedField.setVisible(!isNew);
        assignToDefaultCheckBox.setVisible(!isNew && issue.canAssignToDefault());
        assignToDefaultCheckBox.setSelected(false);        
        statusLabel.setVisible(!isNew);
        statusCombo.setVisible(!isNew);
        resolutionLabel.setVisible(!isNew);
        timetrackingLabel.setVisible(hasTimeTracking);
        timetrackingPanel.setVisible(hasTimeTracking);
        dummyTimetrackingLabel.setVisible(hasTimeTracking);
        separator.setVisible(!isNew);
        commentsPanel.setVisible(!isNew);
        attachmentsLabel.setVisible(!isNew);
        attachmentsPanel.setVisible(!isNew);
        dummyLabel3.setVisible(!isNew);
        refreshButton.setVisible(!isNew);
        separatorLabel.setVisible(!isNew);
        cancelButton.setVisible(!isNew);
        btnDeleteTask.setVisible(isNew);
        separatorLabel3.setVisible(!isNew);
        addToCategoryButton.setVisible(!isNew);
        separatorLabel2.setVisible(!isNew);
        showInBrowserButton.setVisible(!isNew);
        if (!isNew) {
            Border sep2Border = BorderFactory.createLineBorder(Color.BLACK);
            separatorLabel2.setBorder(sep2Border); // IssueProvider 180431
        }
        assignedField.setEditable(issue.isNew() || issue.canReassign());
        assignedCombo.setEnabled(assignedField.isEditable());
        org.openide.awt.Mnemonics.setLocalizedText(submitButton, NbBundle.getMessage(IssuePanel.class, isNew ? "IssuePanel.submitButton.text.new" : "IssuePanel.submitButton.text")); // NOI18N
        if (isNew && force && issue.isMarkedNewUnread()) {
            // this should not be called when reopening task to submit
            if(BugzillaUtil.isNbRepository(issue.getRepository())) {
                addNetbeansInfo();
            }
            // Preselect the first product
            selectProduct();
            initStatusCombo("NEW"); // NOI18N
        } else {
            String format = NbBundle.getMessage(IssuePanel.class, "IssuePanel.headerLabel.format"); // NOI18N
            String headerTxt = MessageFormat.format(format, issue.getID(), issue.getSummary());
            headerField.setText(headerTxt);
            Dimension dim = headerField.getPreferredSize();
            headerField.setMinimumSize(new Dimension(0, dim.height));
            headerField.setPreferredSize(new Dimension(0, dim.height));
            reloadField(force, summaryField, IssueField.SUMMARY, summaryWarning, summaryLabel);
            reloadField(force, productCombo, IssueField.PRODUCT, productWarning, productLabel);
            reloadField(productField, IssueField.PRODUCT, null, (String) null);
            reloadField(force, componentCombo, IssueField.COMPONENT, componentWarning, componentLabel);
            reloadField(force, versionCombo, IssueField.VERSION, versionWarning, versionLabel);
            reloadField(force, platformCombo, IssueField.PLATFORM, platformWarning, platformLabel);
            reloadField(force, osCombo, IssueField.OS, osWarning, platformLabel);
            reloadField(resolutionField, IssueField.RESOLUTION, null, (String) null); // Must be before statusCombo
            initStatusCombo(issue.getRepositoryFieldValue(IssueField.STATUS));
            reloadField(force, statusCombo, IssueField.STATUS, statusWarning, statusLabel);
            reloadField(force, resolutionCombo, IssueField.RESOLUTION, resolutionWarning, resolutionLabel);
            
            reloadField(force, duplicateField, IssueField.DUPLICATE_ID, null, duplicateLabel);
            JTextField field = new JTextField();
            duplicateField.setBorder(field.getBorder());
            duplicateField.setBackground(field.getBackground());
            
            reloadField(force, priorityCombo, IssueField.PRIORITY, priorityWarning, priorityLabel);
            if (BugzillaUtil.isNbRepository(issue.getRepository())) {
                reloadField(force, issueTypeCombo, IssueField.ISSUE_TYPE, issueTypeWarning, issueTypeLabel);
            }
            reloadField(force, severityCombo, IssueField.SEVERITY, severityWarning, priorityLabel);
            if (usingTargetMilestones) {
                reloadField(force, targetMilestoneCombo, IssueField.MILESTONE, milestoneWarning, targetMilestoneLabel);
            }
            reloadField(assignToDefaultCheckBox, IssueField.REASSIGN_TO_DEFAULT, null, null);
            reloadField(urlField, IssueField.URL, urlWarning, fieldName(urlLabel));
            reloadField(force, statusWhiteboardField, IssueField.WHITEBOARD, statusWhiteboardWarning, statusWhiteboardLabel);
            reloadField(force, keywordsField, IssueField.KEYWORDS, keywordsWarning, keywordsLabel);
            if (isNew) {
                if (addCommentArea.getText().isEmpty()) {
                    reloadField(addCommentArea, IssueField.DESCRIPTION, addCommentLabel, null);
                }
            } else {
                reloadField(addCommentArea, IssueField.COMMENT, addCommentLabel, null);
            }

            boolean isKenaiRepository = (issue.getRepository() instanceof KenaiRepository);
            if (!isNew) {
                // reported field
                format = NbBundle.getMessage(IssuePanel.class, "IssuePanel.reportedLabel.format"); // NOI18N
                Date creation = issue.getCreatedDate();
                String creationTxt = creation != null ? DateFormat.getDateInstance(DateFormat.DEFAULT).format(creation) : ""; // NOI18N
                String reporterName = issue.getFieldValue(IssueField.REPORTER_NAME);
                String reporter = issue.getFieldValue(IssueField.REPORTER);
                String reporterTxt = ((reporterName == null) || (reporterName.trim().length() == 0)) ? reporter : reporterName;
                String reportedTxt = MessageFormat.format(format, creationTxt, reporterTxt);
                reportedField.setText(reportedTxt);
                fixPrefSize(reportedField);
                if (isKenaiRepository && (reportedStatusLabel.getIcon() == null)) {
                    int index = reporter.indexOf('@');
                    String userName = (index == -1) ? reporter : reporter.substring(0,index);
                    String host = ((KenaiRepository) issue.getRepository()).getHost();
                    JLabel label = TeamUtil.createUserWidget(issue.getRepository().getUrl(), userName, host, TeamUtil.getChatLink(issue.getID()));
                    if (label != null) {
                        label.setText(null);
                        ((GroupLayout)getLayout()).replace(reportedStatusLabel, label);
                        reportedStatusLabel = label;
                    }
                }

                // modified field
                Date modification = issue.getLastModifyDate();
                String modifiedTxt = modification != null ? DateFormat.getDateTimeInstance().format(modification) : ""; // NOI18N
                modifiedField.setText(modifiedTxt);
                fixPrefSize(modifiedField);
                
                // time tracking
                if(hasTimeTracking) {
                    reloadField(force, estimatedField, IssueField.ESTIMATED_TIME, estimatedWarning, estimatedLabel);
                    reloadField(force, workedField, IssueField.WORK_TIME, workedWarning, workedLabel);
                    reloadField(force, remainingField, IssueField.REMAINING_TIME, remainingWarning, remainingLabel);
                    reloadField(force, deadlineField, IssueField.DEADLINE, deadlineWarning, deadlineLabel);
                    if("".equals(deadlineField.getText().trim())) {
                        deadlineField.setText(YYYY_MM_DD); // NOI18N
                        deadlineField.setForeground(javax.swing.UIManager.getDefaults().getColor("TextField.inactiveForeground")); // NOI18N
                    }

                    String actualString = issue.getFieldValue(IssueField.ACTUAL_TIME);
                    if(actualString.trim().equals("")) {                            // NOI18N    
                        actualString = "0";                                         // NOI18N
                    }                                                         
                    actualField.setText(String.valueOf(Double.parseDouble(actualString) + getDoubleValue(remainingField)));
                    double worked = 0;
                    Comment[] comments = issue.getComments();
                    for (Comment comment : comments) {
                        worked += comment.getWorked();
                    }
                    workedSumField.setText(String.valueOf(worked));
                    gainField.setText(String.valueOf(getDoubleValue(estimatedField) - getDoubleValue(remainingField)));
                    completeField.setText(String.valueOf((int)Math.floor(getDoubleValue(workedSumField) / getDoubleValue(actualField) * 100)));
                }
            }

            String assignee = issue.getFieldValue(IssueField.ASSIGNED_TO);
            String selectedAssignee = (assignedField.getParent() == null) ? assignedCombo.getSelectedItem().toString() : assignedField.getText();
            if (isKenaiRepository && (assignee.trim().length() > 0) && (force || !selectedAssignee.equals(assignee))) {
                int index = assignee.indexOf('@');
                String userName = (index == -1) ? assignee : assignee.substring(0,index);
                String host = ((KenaiRepository) issue.getRepository()).getHost();
                JLabel label = TeamUtil.createUserWidget(issue.getRepository().getUrl(), userName, host, TeamUtil.getChatLink(issue.getID()));
                if (label != null) {
                    label.setText(null);
                    ((GroupLayout)getLayout()).replace(assignedToStatusLabel, label);
                    label.setVisible(assignedToStatusLabel.isVisible());
                    assignedToStatusLabel = label;
                }
            }
            if (force) {
                assignedToStatusLabel.setVisible(assignee.trim().length() > 0);
            }
            if (assignedField.getParent() == null) {
                reloadField(force, assignedCombo, IssueField.ASSIGNED_TO, assignedToWarning, assignedLabel);
            } else {
                reloadField(force, assignedField, IssueField.ASSIGNED_TO, assignedToWarning, assignedLabel);
            }
            reloadField(force, qaContactField, IssueField.QA_CONTACT, qaContactWarning, qaContactLabel);
            reloadField(force, ccField, IssueField.CC, ccWarning, ccLabel);
            reloadField(force, dependsField, IssueField.DEPENDS_ON, dependsOnWarning, dependsLabel);
            reloadField(force, blocksField, IssueField.BLOCKS, blocksWarning, blocksLabel);
            reloadCustomFields(force);
        }
        int newCommentCount = issue.getComments().length;
        if (!force && oldCommentCount != newCommentCount) {
            String message = NbBundle.getMessage(IssuePanel.class, "IssuePanel.commentAddedWarning"); // NOI18N
            if (!fieldWarnings.contains(message)) {
                fieldWarnings.add(0, message);
            }
        }
        oldCommentCount = newCommentCount;
        List<Attachment> attachments = issue.getAttachments();
        if (!isNew) {
            commentsPanel.setIssue(issue, attachments);
        }
        if(isNetbeans) {
            AttachmentsPanel.NBBugzillaCallback callback = 
                new AttachmentsPanel.NBBugzillaCallback() {
                    @Override
                    public String getLogFilePath() {
                        return NbBugzillaConstants.NB_LOG_FILE_PATH;
                    }
                    @Override
                    public String getLogFileContentType() {
                        return NbBugzillaConstants.NB_LOG_FILE_ATT_CONT_TYPE;
                    }
                    @Override
                    public String getLogFileDescription() {
                        return NbBundle.getMessage(IssuePanel.class, "MSG_LOG_FILE_DESC");
                    }
                    @Override
                    public void showLogFile() {
                        IssuePanel.showLogFile(null);
                    }
                };
            attachmentsPanel.setAttachments(attachments, callback);
        } else {
            attachmentsPanel.setAttachments(attachments);
        }
        UIUtils.keepFocusedComponentVisible(commentsPanel, this);
        UIUtils.keepFocusedComponentVisible(attachmentsPanel, this);
        updateFieldStatuses();
        updateNoSummary();
        updateMessagePanel();
        cancelButton.setEnabled(issue.hasLocalEdits());
        reloading = false;
        repaint();
    }

    private void reloadCustomFields(boolean force) {
        // Reload custom fields
        for (CustomFieldInfo field : customFields) {
            reloadField(force, field.comp, field.field, field.warning, field.label);
        }
    }

    private static void fixPrefSize(JTextField textField) {
        // The preferred size of JTextField on (Classic) Windows look and feel
        // is one pixel shorter. The following code is a workaround.
        textField.setPreferredSize(null);
        Dimension dim = textField.getPreferredSize();
        Dimension fixedDim = new Dimension(dim.width+1, dim.height);
        textField.setPreferredSize(fixedDim);
    }

    private void reloadField(boolean force, JComponent component, IssueField field, JLabel warningLabel, JLabel fieldLabel) {
        reloadField(component, field, warningLabel, fieldName(fieldLabel));
    }
    
    private void reloadField (JComponent component, IssueField field, JLabel warningLabel, String fieldName) {
        boolean isNew = issue.isNew();
        String newValue, lastSeenValue, repositoryValue;
        if (component instanceof JList) {
            newValue = mergeValues(issue.getFieldValues(field));
            lastSeenValue = mergeValues(issue.getLastSeenFieldValues(field));
            repositoryValue = mergeValues(issue.getRepositoryFieldValues(field));
        } else {
            newValue = issue.getFieldValue(field);
            lastSeenValue = issue.getLastSeenFieldValue(field);
            repositoryValue = issue.getRepositoryFieldValue(field);
        }
        boolean fieldDirty = unsavedFields.contains(field);
        boolean valueModifiedByUser = (issue.getFieldStatus(field) & BugzillaIssue.FIELD_STATUS_OUTGOING) != 0;
        boolean valueModifiedByServer = (issue.getFieldStatus(field) & BugzillaIssue.FIELD_STATUS_MODIFIED) != 0;
        if (!fieldDirty) {
            if (component instanceof JComboBox) {
                JComboBox combo = (JComboBox)component;
                selectInCombo(combo, newValue, true);
            } else if (component instanceof JTextComponent) {
                ((JTextComponent)component).setText(newValue);
            } else if (component instanceof JList) {
                JList list = (JList)component;
                list.clearSelection();
                ListModel model = list.getModel();
                for (String value : issue.getFieldValues(field)) {
                    for (int i=0; i<model.getSize(); i++) {
                        if (value.equals(model.getElementAt(i))) {
                            list.addSelectionInterval(i, i);
                        }
                    }
                }
            } else if (component instanceof JCheckBox) {
                ((JCheckBox) component).setSelected("1".equals(newValue));
            }
        }
        if (warningLabel != null) {
            warningLabel.setIcon(null);
        }
        if (!fieldDirty && !valueModifiedByUser) {
            if (!isNew && valueModifiedByServer && (warningLabel != null)) {
                warningLabel.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/bugzilla/resources/warning.gif", true)); // NOI18N
                String messageFormat = NbBundle.getMessage(IssuePanel.class, "IssuePanel.fieldModifiedWarning"); // NOI18N
                String message = MessageFormat.format(messageFormat, fieldName, lastSeenValue, newValue);
                fieldWarnings.add(message);
                warningLabel.setToolTipText(message);
            }
        } else {
            if (!isNew && valueModifiedByServer && (warningLabel != null)) {
                warningLabel.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/bugzilla/resources/error.gif", true)); // NOI18N
                String messageFormat = NbBundle.getMessage(IssuePanel.class, "IssuePanel.fieldModifiedError"); // NOI18N
                String message = MessageFormat.format(messageFormat, fieldName, lastSeenValue, repositoryValue);
                fieldErrors.add(message);
                warningLabel.setToolTipText(message);
            }
        }
        if ((IssueField.SUMMARY == field) || (IssueField.PLATFORM == field) || (IssueField.PRIORITY == field) || (field instanceof CustomIssueField)) {
            warningLabel.setVisible(warningLabel.getIcon() != null);
        }
    }

    private boolean selectInCombo(JComboBox combo, Object value, boolean forceInModel) {
        if (value == null) {
            return false;
        }
        if (!value.equals(combo.getSelectedItem())) {
            combo.setSelectedItem(value);
        }
        if (forceInModel && !value.equals("") && !value.equals(combo.getSelectedItem())) { // NOI18N
            // Reload of server attributes is needed - workarounding it
            ComboBoxModel model = combo.getModel();
            if (model instanceof DefaultComboBoxModel) {
                ((DefaultComboBoxModel)model).insertElementAt(value, 0);
                combo.setSelectedIndex(0);
            }
        }
        return value.equals(combo.getSelectedItem());
    }

    private String fieldName(JComponent fieldLabel) {
        assert fieldLabel instanceof JLabel || fieldLabel instanceof JButton;
        String txt = "";
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

    private void initCombos() {
        BugzillaRepository repository = issue.getRepository();
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

        initAssignedCombo();

        if (BugzillaUtil.isNbRepository(repository)) {
            issueTypeCombo.setModel(toComboModel(bc.getIssueTypes()));
        }

        // stausCombo and resolution fields are filled in reloadForm
    }

    private void initAssignedCombo() {
        assignedCombo.setRenderer(new RepositoryUserRenderer());
        RP.post(new Runnable() {
            @Override
            public void run() {
                BugzillaRepository repository = issue.getRepository();
                final Collection<RepositoryUser> users = repository.getUsers();
                final DefaultComboBoxModel assignedModel = new DefaultComboBoxModel();
                for (RepositoryUser user: users) {
                    assignedModel.addElement(user);
                }
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        reloading = true;
                        try {
                            Object assignee = (assignedField.getParent() == null) ? assignedCombo.getSelectedItem() : assignedField.getText();
                            if (assignee == null) {
                                assignee = ""; //NOI18N
                            }
                            assignedCombo.setModel(assignedModel);
                            GroupLayout layout = (GroupLayout)getLayout();
                            if ((assignedCombo.getParent()==null) != users.isEmpty()) {
                                layout.replace(users.isEmpty() ? assignedCombo : assignedField, users.isEmpty() ? assignedField : assignedCombo);
                                assignedLabel.setLabelFor(users.isEmpty() ? assignedField : assignedCombo);
                            }
                            if (assignedField.getParent() == null) {
                                assignedCombo.setSelectedItem(assignee);
                            } else {
                                assignedField.setText(assignee.toString());
                            }
                        } finally {
                            reloading = false;
                        }
                    }
                });
            }
        });
    }

    private void initStatusCombo(String status) {
        // Init statusCombo - allowed transitions (heuristics):
        // Open -> Open-Unconfirmed-Reopened+Resolved
        // Resolved -> Reopened+Close
        // Close-Resolved -> Reopened+Resolved+(Close with higher index)
        BugzillaRepository repository = issue.getRepository();
        BugzillaConfiguration bc = repository.getConfiguration();
        if(bc == null || !bc.isValid()) {
            // XXX nice error msg?
            return;
        }
        List<String> allStatuses = bc.getStatusValues();
        List<String> openStatuses = bc.getOpenStatusValues();
        List<String> statuses = new LinkedList<String>();
        boolean oldRepository = (issue.getRepository().getConfiguration().getInstalledVersion().compareMajorMinorOnly(BugzillaVersion.BUGZILLA_3_2) < 0);
        String nev = "NEW"; // NOI18N
        String unconfirmed = "UNCONFIRMED"; // NOI18N
        String reopened = "REOPENED"; // NOI18N
        String resolved = "RESOLVED"; // NOI18N
        if(status != null) {
            status = status.trim();
        }
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
                if (allStatuses.contains(status)) {
                    if (!status.equals("")) {
                        for (int i = allStatuses.indexOf(status); i < allStatuses.size(); i++) {
                            String s = allStatuses.get(i);
                            if (!openStatuses.contains(s)) {
                                statuses.add(s);
                            }
                        }
                    }
                } else {
                    Bugzilla.LOG.log(Level.WARNING, "status value {0} not between all statuses: {1}", new Object[]{status, allStatuses}); // NOI18N
                }
            }
        }
        resolvedIndex = statuses.indexOf(resolved);
        statusCombo.setModel(toComboModel(statuses));
        statusCombo.setSelectedItem(status);
    }

    private ComboBoxModel toComboModel(List<String> items) {
        return new DefaultComboBoxModel(items.toArray());
    }

    private void updateFieldStatuses() {
        updateFieldStatus(summaryLabel, IssueField.SUMMARY);
        updateFieldStatus(productLabel, IssueField.PRODUCT);
        updateFieldStatus(componentLabel, IssueField.COMPONENT);
        updateFieldStatus(versionLabel, IssueField.VERSION);
        updateFieldStatus(platformLabel, IssueField.PLATFORM, IssueField.OS);
        updateFieldStatus(statusLabel, IssueField.STATUS);
        updateFieldStatus(resolutionLabel, IssueField.RESOLUTION);
        updateFieldStatus(priorityLabel, IssueField.PRIORITY, IssueField.SEVERITY);
        updateFieldStatus(targetMilestoneLabel, IssueField.MILESTONE);
        updateFieldStatus(urlLabel, IssueField.URL);
        updateFieldStatus(statusWhiteboardLabel, IssueField.WHITEBOARD);
        updateFieldStatus(keywordsLabel, IssueField.KEYWORDS);
        updateFieldStatus(assignedLabel, IssueField.ASSIGNED_TO);
        updateFieldStatus(qaContactLabel, IssueField.QA_CONTACT);
        updateFieldStatus(ccLabel, IssueField.CC);
        updateFieldStatus(dependsLabel, IssueField.DEPENDS_ON);
        updateFieldStatus(blocksLabel, IssueField.BLOCKS);
        updateFieldStatus(estimatedLabel, IssueField.ESTIMATED_TIME);
        updateFieldStatus(remainingLabel, IssueField.REMAINING_TIME);
        updateFieldStatus(timetrackingLabel, IssueField.WORK_TIME);
        updateFieldStatus(deadlineLabel, IssueField.DEADLINE);
        updateFieldStatus(addCommentLabel, IssueField.COMMENT);
        if (BugzillaUtil.isNbRepository(issue.getRepository())) {
            updateFieldStatus(issueTypeLabel, IssueField.ISSUE_TYPE);
        }
        for (CustomFieldInfo field : customFields) {
            updateFieldStatus(field.label, field.field);
        }
        repaint();
    }

    private void updateFieldStatus(JComponent label, IssueField... fields) {
        assert label instanceof JButton || label instanceof JLabel;
        label.setOpaque(false);
        for (IssueField field : fields) {
            int status = BugzillaIssue.FIELD_STATUS_UPTODATE;
            boolean highlight = !issue.isNew() && (status = issue.getFieldStatus(field)) != BugzillaIssue.FIELD_STATUS_UPTODATE;
            if (highlight) {
                label.setOpaque(true);
                label.setBackground(status == BugzillaIssue.FIELD_STATUS_OUTGOING
                        ? outgoingChangesColor
                        : incomingChangesColor);
                break;
            }
        }
    }

    private void cancelHighlight(JComponent label) {
        if (!reloading) {
            label.setOpaque(false);
            label.getParent().repaint();
        }
    }

    private void storeFieldValue(IssueField field, JComboBox combo) {
        Object value = combo.getSelectedItem();
        // It (normally) should not happen that value is null, but issue 159804 shows that
        // some strange configurations (or other bugs) can lead into this situation
        if (value != null) {
            storeFieldValue(field, value.toString());
        }
    }

    private void storeFieldValue(IssueField field, JTextComponent textComponent) {
        storeFieldValue(field, textComponent.getText());
    }

    private void storeFieldValue(IssueField field, JList list) {
        List<String> values = new ArrayList<String>();
        for (Object value : list.getSelectedValues()) {
            values.add(value.toString());
        }
        if (!issue.getFieldValues(field).equals(values)) {
            unsavedFields.add(field);
            issue.setFieldValues(field, values);
        }
    }

    private void storeFieldValue(IssueField field, String value) {
        boolean changed = false;
        if (field == IssueField.STATUS) {
            changed = true;
            if (value.equals("CLOSED")) { // NOI18N
                issue.close();
            } else if (value.equals("VERIFIED")) { // NOI18N
                issue.verify();
            } else if (value.equals("REOPENED")) { // NOI18N
                issue.reopen();
            } else if (value.equals("RESOLVED")) { // NOI18N
                issue.resolve(resolutionCombo.getSelectedItem().toString());
                unsavedFields.add(IssueField.RESOLUTION);
                issue.setFieldValue(IssueField.RESOLUTION, resolutionCombo.getSelectedItem().toString());
            } else if (value.equals("ASSIGNED")) { // NOI18N
                issue.accept();
            } else {
                changed = false;
            }
        } else if (field == IssueField.RESOLUTION && "RESOLVED".equals(statusCombo.getSelectedItem())) {
            changed = true;
            if (value.equals("DUPLICATE")) {
                issue.duplicate(duplicateField.getText().trim());
                unsavedFields.add(IssueField.DUPLICATE_ID);
                issue.setFieldValue(IssueField.DUPLICATE_ID, duplicateField.getText().trim());
            } else {
                issue.resolve(value);
            }
        } else if (field == IssueField.DUPLICATE_ID && "RESOLVED".equals(statusCombo.getSelectedItem())
                && "DUPLICATE".equals(resolutionCombo.getSelectedItem())) {
            issue.duplicate(value);
        } else if ((field == IssueField.ASSIGNED_TO) && !issue.isNew()) {
            issue.reassign(value);
        }
        if (changed || !issue.getFieldValue(field).equals(value)) {
            unsavedFields.add(field);
            issue.setFieldValue(field, value);
        }
    }

    private void attachDocumentListeners() {
        urlField.getDocument().addDocumentListener(new CancelHighlightDocumentListener(urlLabel));
        statusWhiteboardField.getDocument().addDocumentListener(new CancelHighlightDocumentListener(statusWhiteboardLabel));
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
        duplicateField.getDocument().addDocumentListener(new DuplicateListener());
    }

    private void attachHideStatusListener() {
        assignedField.getDocument().addDocumentListener(new DocumentListener() {
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

    private void updateNoComponent() {
        boolean newNoComponent = (componentCombo.getSelectedItem() == null);
        if (noComponent != newNoComponent) {
            noComponent = newNoComponent;
            updateMessagePanel();
        }
    }

    private void updateNoVersion() {
        boolean newNoVersion = (versionCombo.getSelectedItem() == null);
        if (noVersion != newNoVersion) {
            noVersion = newNoVersion;
            updateMessagePanel();
        }
    }

    private void updateNoTargetMilestone() {
        boolean newNoTargetMilestone = (targetMilestoneCombo.getSelectedItem() == null);
        if (noTargetMilestione != newNoTargetMilestone) {
            noTargetMilestione = newNoTargetMilestone;
            updateMessagePanel();
        }
    }

    private boolean noSummary = false;
    private boolean invalidKeyword = false;
    private boolean cyclicDependency = false;
    private boolean noComponent = false;
    private boolean noVersion = false;
    private boolean noTargetMilestione = false;
    private boolean noDuplicateId = false;
    private List<String> fieldErrors = new LinkedList<String>();
    private List<String> fieldWarnings = new LinkedList<String>();
    private void updateMessagePanel() {
        messagePanel.removeAll();
        if (noComponent) {
            addMessage("IssuePanel.noComponent"); // NOI18N
        }
        if (noVersion) {
            addMessage("IssuePanel.noVersion"); // NOI18N
        }
        if (noTargetMilestione) {
            addMessage("IssuePanel.noTargetMilestone"); // NOI18N
        }
        if (noSummary) {
            JLabel noSummaryLabel = new JLabel();
            noSummaryLabel.setText(NbBundle.getMessage(IssuePanel.class, "IssuePanel.noSummary")); // NOI18N
            String icon = issue.isNew() ? "org/netbeans/modules/bugzilla/resources/info.png" : "org/netbeans/modules/bugzilla/resources/error.gif"; // NOI18N
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
        if (noDuplicateId) {
            JLabel noDuplicateLabel = new JLabel();
            noDuplicateLabel.setText(NbBundle.getMessage(IssuePanel.class, "IssuePanel.noDuplicateId")); // NOI18N
            noDuplicateLabel.setIcon(new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/bugzilla/resources/error.gif"))); // NOI18N
            messagePanel.add(noDuplicateLabel);
        }
        if (noSummary || cyclicDependency || invalidKeyword || noComponent || noVersion || noTargetMilestione || noDuplicateId) {
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
        if (noSummary || cyclicDependency || invalidKeyword || noComponent || noVersion || noTargetMilestione || noDuplicateId || (fieldErrors.size() + fieldWarnings.size() > 0)) {
            messagePanel.setVisible(true);
            messagePanel.revalidate();
        } else {
            messagePanel.setVisible(false);
        }
    }

    void addMessage(String messageKey) {
        JLabel messageLabel = new JLabel();
        messageLabel.setText(NbBundle.getMessage(IssuePanel.class, messageKey));
        String icon = issue.isNew() ? "org/netbeans/modules/bugzilla/resources/info.png" : "org/netbeans/modules/bugzilla/resources/error.gif"; // NOI18N
        messageLabel.setIcon(new ImageIcon(ImageUtilities.loadImage(icon)));
        messagePanel.add(messageLabel);
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
                || (comp instanceof AbstractButton) || (comp instanceof JList)) {
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

    private void initSpellChecker () {
        Spellchecker.register(summaryField);
        Spellchecker.register(addCommentArea);
    }

    private List<CustomFieldInfo> customFields = new LinkedList<CustomFieldInfo>();
    private void initCustomFields() {
        customFields.clear();
        customFieldsPanelLeft.removeAll();
        customFieldsPanelRight.removeAll();
        GroupLayout labelLayout = new GroupLayout(customFieldsPanelLeft);
        customFieldsPanelLeft.setLayout(labelLayout);
        GroupLayout fieldLayout = new GroupLayout(customFieldsPanelRight);
        customFieldsPanelRight.setLayout(fieldLayout);
        GroupLayout.ParallelGroup labelHorizontalGroup = labelLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.SequentialGroup labelVerticalGroup = labelLayout.createSequentialGroup();
        GroupLayout.ParallelGroup fieldHorizontalGroup = fieldLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.SequentialGroup fieldVerticalGroup = fieldLayout.createSequentialGroup();
        boolean nbRepository = BugzillaUtil.isNbRepository(issue.getRepository());
        boolean newIssue = issue.isNew();
        boolean anyField = false;
        for (IssueField field : issue.getRepository().getConfiguration().getFields()) {
            if (field instanceof CustomIssueField) {
                CustomIssueField cField = (CustomIssueField)field;
                if ((nbRepository && cField.getKey().equals(IssueField.ISSUE_TYPE.getKey()))
                    || (newIssue && !cField.getShowOnBugCreation())  // NB IssueProvider type is already among non-custom fields
                    || (isNbExceptionReport(field) && (newIssue || "".equals(issue.getFieldValue(field).trim()))))     // NOI18N do not show exception reporter field - issue #212182
                {
                    continue;
                }
                JLabel label = new JLabel(cField.getDisplayName()+":"); // NOI18N
                JComponent comp;
                JComponent editor;
                boolean rigid = false;
                switch (cField.getType()) {
                    case LargeText:
                        JScrollPane scrollPane = new JScrollPane();
                        JTextArea textArea = new JTextArea();
                        textArea.setRows(5);
                        scrollPane.setViewportView(textArea);
                        comp = scrollPane;
                        editor = textArea;
                        label.setVerticalAlignment(SwingConstants.TOP);
                        UIUtils.fixFocusTraversalKeys(textArea);
                        UIUtils.issue163946Hack(scrollPane);
                        break;
                    case FreeText:
                        if(isNbExceptionReport(field)) {
                            final String val = issue.getFieldValue(field);
                            LinkButton lb = new LinkButton(val);
                            lb.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    try {
                                        URL url = new URL("http://statistics.netbeans.org/exceptions/detail.do?id=" + val); // NOI18N
                                        HtmlBrowser.URLDisplayer.getDefault().showURL(url);
                                    } catch (MalformedURLException muex) {
                                        Bugzilla.LOG.log(Level.INFO, "Unable to show the exception report in the browser.", muex); // NOI18N
                                    }
                                }
                            });
                            comp = editor = lb;
                        } else {
                            comp = editor = new JTextField();
                        }
                        break;
                    case MultipleSelection:
                        JList list = new JList();
                        DefaultComboBoxModel model = new DefaultComboBoxModel(cField.getOptions().toArray());
                        list.setModel(model);
                        if (model.getSize()<list.getVisibleRowCount()) {
                            list.setVisibleRowCount(model.getSize());
                        }
                        scrollPane = new JScrollPane();
                        scrollPane.setViewportView(list);
                        comp = scrollPane;
                        editor = list;
                        label.setVerticalAlignment(SwingConstants.TOP);
                        rigid = true;
                        UIUtils.issue163946Hack(scrollPane);
                        break;
                    case DropDown:
                        comp = editor = new JComboBox();
                        model = new DefaultComboBoxModel(cField.getOptions().toArray());
                        ((JComboBox)comp).setModel(model);
                        rigid = true;
                        break;
                    case DateTime:
                        comp = editor = new JTextField();
                        break;
                    default:
                        Bugzilla.LOG.log(Level.INFO, "Custom field type {0} is not supported!", cField.getType()); // NOI18N
                        continue;
                }
                JLabel warning = new JLabel();
                warning.setMinimumSize(new Dimension(16,16));
                warning.setPreferredSize(new Dimension(16,16));
                warning.setMaximumSize(new Dimension(16,16));
                warning.setVisible(false);
                customFields.add(new CustomFieldInfo(cField, label, editor, warning));
                label.setLabelFor(editor);
                label.setPreferredSize(new Dimension(label.getPreferredSize().width, comp.getPreferredSize().height));
                if (anyField) {
                    labelVerticalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
                    fieldVerticalGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
                }
                labelHorizontalGroup.addComponent(label);
                labelVerticalGroup.addComponent(label, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
                GroupLayout.SequentialGroup sGroup = fieldLayout.createSequentialGroup();
                if (rigid) {
                    sGroup.addComponent(comp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
                } else {
                    sGroup.addComponent(comp);
                }
                sGroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
                sGroup.addComponent(warning);
                fieldHorizontalGroup.addGroup(sGroup);
                GroupLayout.ParallelGroup pGroup = fieldLayout.createParallelGroup(GroupLayout.Alignment.BASELINE);
                pGroup.addComponent(comp);
                pGroup.addComponent(warning);
                fieldVerticalGroup.addGroup(pGroup);
                anyField = true;
            }
        }
        labelLayout.setHorizontalGroup(labelHorizontalGroup);
        labelLayout.setVerticalGroup(labelVerticalGroup);
        fieldLayout.setHorizontalGroup(fieldHorizontalGroup);
        fieldLayout.setVerticalGroup(fieldVerticalGroup);
        customFieldsPanelLeft.setVisible(anyField);
        customFieldsPanelRight.setVisible(anyField);
    }
    
    private boolean isNbExceptionReport(IssueField field) {
        return field.getKey().equals("cf_autoreporter_id"); // NOI18N
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
        assignedCombo = new javax.swing.JComboBox();
        timetrackingPanel = new javax.swing.JPanel();
        estimatedLabel = new javax.swing.JLabel();
        estimatedField = new javax.swing.JTextField();
        estimatedWarning = new javax.swing.JLabel();
        actualLabel = new javax.swing.JLabel();
        workedLabel = new javax.swing.JLabel();
        workedField = new javax.swing.JTextField();
        workedWarning = new javax.swing.JLabel();
        remainingField = new javax.swing.JTextField();
        remainingLabel = new javax.swing.JLabel();
        remainingWarning = new javax.swing.JLabel();
        completeLabel = new javax.swing.JLabel();
        workedSumField = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        actualField = new javax.swing.JTextField();
        completeField = new javax.swing.JTextField();
        gainLabel = new javax.swing.JLabel();
        gainField = new javax.swing.JTextField();
        deadlineLabel = new javax.swing.JLabel();
        deadlineField = new javax.swing.JTextField();
        actualWarning = new javax.swing.JLabel();
        deadlineWarning = new javax.swing.JLabel();
        completeWarning = new javax.swing.JLabel();
        gainWarning = new javax.swing.JLabel();
        customFieldsPanelLeft = new javax.swing.JPanel();
        customFieldsPanelRight = new javax.swing.JPanel();
        reportedField = new javax.swing.JTextField();
        dependsOnWarning = new javax.swing.JLabel();
        osWarning = new javax.swing.JLabel();
        messagePanel = new javax.swing.JPanel();
        ccWarning = new javax.swing.JLabel();
        dummyAttachmentsPanel = new javax.swing.JPanel();
        dummyTimetrackingPanel = new javax.swing.JPanel();
        timetrackingLabel = new javax.swing.JLabel();
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
        headerField = new javax.swing.JTextField();
        refreshButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        reloadButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        showInBrowserButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        separatorLabel = new javax.swing.JLabel();
        resolutionLabel = new javax.swing.JLabel();
        addToCategoryButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        separatorLabel2 = new javax.swing.JLabel();
        separatorLabel3 = new javax.swing.JLabel();
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
        urlLabel = new org.netbeans.modules.bugtracking.util.LinkButton();
        keywordsField = new javax.swing.JTextField();
        urlField = new javax.swing.JTextField();
        statusWhiteboardLabel = new javax.swing.JLabel();
        statusWhiteboardField = new javax.swing.JTextField();
        issueTypeLabel = new javax.swing.JLabel();
        issueTypeCombo = new javax.swing.JComboBox();
        dummyTimetrackingLabel = new javax.swing.JLabel();
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
                Insets insets = getInsets();
                int prefHeight = 5 * getRowHeight() + insets.top + insets.bottom;
                dim = new Dimension(dim.width, delta + ((dim.height < prefHeight) ? prefHeight : dim.height));
                return dim;
            }
        };
        submitButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        componentWarning = new javax.swing.JLabel();
        statusWhiteboardWarning = new javax.swing.JLabel();
        issueTypeWarning = new javax.swing.JLabel();
        assignToDefaultCheckBox = new javax.swing.JCheckBox();
        attachLogCheckBox = new javax.swing.JCheckBox();
        viewLogButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        btnSaveChanges = new javax.swing.JButton();
        btnDeleteTask = new javax.swing.JButton();

        FormListener formListener = new FormListener();

        productField.setEditable(false);
        productField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        productField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.productField.AccessibleContext.accessibleDescription")); // NOI18N

        resolutionCombo.addActionListener(formListener);
        resolutionCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.resolutionCombo.AccessibleContext.accessibleDescription")); // NOI18N

        assignedCombo.setEditable(true);
        assignedCombo.addActionListener(formListener);

        timetrackingPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        org.openide.awt.Mnemonics.setLocalizedText(estimatedLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.estimatedLabel.text")); // NOI18N

        estimatedField.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.estimatedField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(actualLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.actualLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(workedLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.workedLabel.text")); // NOI18N

        workedField.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.workedField.text")); // NOI18N
        workedField.addFocusListener(formListener);

        remainingField.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.remainingField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(remainingLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.remainingLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(completeLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.completeLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(workedSumField, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.workedSumField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.jLabel7.text")); // NOI18N

        actualField.setEditable(false);
        actualField.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.actualField.text")); // NOI18N

        completeField.setEditable(false);
        completeField.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.completeField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(gainLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.gainLabel.text")); // NOI18N

        gainField.setEditable(false);
        gainField.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.gainField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(deadlineLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.deadlineLabel.text")); // NOI18N

        deadlineField.setForeground(javax.swing.UIManager.getDefaults().getColor("TextField.inactiveForeground"));
        deadlineField.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.deadlineField.text")); // NOI18N
        deadlineField.addFocusListener(formListener);

        javax.swing.GroupLayout timetrackingPanelLayout = new javax.swing.GroupLayout(timetrackingPanel);
        timetrackingPanel.setLayout(timetrackingPanelLayout);
        timetrackingPanelLayout.setHorizontalGroup(
            timetrackingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(timetrackingPanelLayout.createSequentialGroup()
                .addGroup(timetrackingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(estimatedLabel)
                    .addComponent(estimatedField, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(estimatedWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(timetrackingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(actualLabel)
                    .addComponent(actualField, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(actualWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(timetrackingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(timetrackingPanelLayout.createSequentialGroup()
                        .addComponent(workedSumField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(workedField, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(workedLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(workedWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(timetrackingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(remainingLabel)
                    .addGroup(timetrackingPanelLayout.createSequentialGroup()
                        .addComponent(remainingField, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(remainingWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(timetrackingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(completeLabel)
                    .addComponent(completeField, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(completeWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(timetrackingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(timetrackingPanelLayout.createSequentialGroup()
                        .addComponent(gainField, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(gainWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(9, 9, 9)
                        .addGroup(timetrackingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(deadlineField, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(deadlineLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deadlineWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(gainLabel)))
        );
        timetrackingPanelLayout.setVerticalGroup(
            timetrackingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(timetrackingPanelLayout.createSequentialGroup()
                .addGroup(timetrackingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(completeLabel)
                    .addGroup(timetrackingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(actualLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(estimatedLabel)
                        .addComponent(remainingLabel))
                    .addComponent(workedLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(timetrackingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(actualWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(remainingField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(workedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(estimatedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deadlineWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(completeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(actualField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(estimatedWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(remainingWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(workedWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(workedSumField)
                    .addComponent(completeWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGroup(timetrackingPanelLayout.createSequentialGroup()
                .addComponent(gainLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(timetrackingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(gainField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gainWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGroup(timetrackingPanelLayout.createSequentialGroup()
                .addComponent(deadlineLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deadlineField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        reportedField.setEditable(false);
        reportedField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        messagePanel.setLayout(new javax.swing.BoxLayout(messagePanel, javax.swing.BoxLayout.PAGE_AXIS));

        org.openide.awt.Mnemonics.setLocalizedText(timetrackingLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.timetrackingLabel.text")); // NOI18N

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

        headerField.setEditable(false);
        headerField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(refreshButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.refreshButton.text")); // NOI18N
        refreshButton.setToolTipText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.refreshButton.toolTipText")); // NOI18N
        refreshButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(reloadButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.reloadButton.text")); // NOI18N
        reloadButton.setToolTipText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.reloadButton.toolTipText")); // NOI18N
        reloadButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(showInBrowserButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.showInBrowserButton.text")); // NOI18N
        showInBrowserButton.addActionListener(formListener);

        separatorLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.openide.awt.Mnemonics.setLocalizedText(resolutionLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.resolutionLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addToCategoryButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.tasklistButton.add")); // NOI18N
        addToCategoryButton.addActionListener(formListener);

        separatorLabel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        separatorLabel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

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

        org.openide.awt.Mnemonics.setLocalizedText(urlLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.urlLabel.text")); // NOI18N
        urlLabel.addActionListener(formListener);

        keywordsField.setColumns(15);

        urlField.setColumns(15);

        org.openide.awt.Mnemonics.setLocalizedText(statusWhiteboardLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.statusWhiteboardLabel.text")); // NOI18N

        statusWhiteboardField.setColumns(15);

        org.openide.awt.Mnemonics.setLocalizedText(issueTypeLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.issueTypeLabel.text")); // NOI18N

        issueTypeCombo.addActionListener(formListener);

        scrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        addCommentArea.setLineWrap(true);
        addCommentArea.setWrapStyleWord(true);
        scrollPane1.setViewportView(addCommentArea);
        addCommentArea.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.addCommentArea.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(submitButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.submitButton.text")); // NOI18N
        submitButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(formListener);

        assignToDefaultCheckBox.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));
        org.openide.awt.Mnemonics.setLocalizedText(assignToDefaultCheckBox, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.assignToDefaultCheckBox.text")); // NOI18N

        attachLogCheckBox.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));
        org.openide.awt.Mnemonics.setLocalizedText(attachLogCheckBox, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.attachLogCheckBox.text")); // NOI18N
        attachLogCheckBox.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(viewLogButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.viewLogButton.text")); // NOI18N
        viewLogButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(btnSaveChanges, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.btnSaveChanges.text")); // NOI18N
        btnSaveChanges.setToolTipText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.btnSaveChanges.TTtext")); // NOI18N
        btnSaveChanges.setEnabled(false);
        btnSaveChanges.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(btnDeleteTask, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.btnDeleteTask.text")); // NOI18N
        btnDeleteTask.setToolTipText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.btnDeleteTask.TTtext")); // NOI18N
        btnDeleteTask.addActionListener(formListener);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(separator, javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(dummyCommentsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(headerField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addToCategoryButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(separatorLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(separatorLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(reloadButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(separatorLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(showInBrowserButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(urlLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(componentLabel)
                            .addComponent(productLabel)
                            .addComponent(versionLabel)
                            .addComponent(platformLabel)
                            .addComponent(dummyLabel1)
                            .addComponent(priorityLabel)
                            .addComponent(targetMilestoneLabel)
                            .addComponent(dummyLabel2)
                            .addComponent(keywordsLabel)
                            .addComponent(statusWhiteboardLabel)
                            .addComponent(issueTypeLabel)
                            .addComponent(dummyLabel3)
                            .addComponent(dummyTimetrackingLabel)
                            .addComponent(summaryLabel)
                            .addComponent(timetrackingLabel)
                            .addComponent(attachmentsLabel)
                            .addComponent(addCommentLabel)
                            .addComponent(customFieldsPanelLeft, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(dummyTimetrackingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(dummyAttachmentsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(scrollPane1)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(summaryField)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(summaryWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(customFieldsPanelRight, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(messagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(issueTypeCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(statusWhiteboardField, 0, 1, Short.MAX_VALUE)
                                            .addComponent(urlField, 0, 1, Short.MAX_VALUE)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(keywordsField, 0, 1, Short.MAX_VALUE)
                                                .addGap(0, 0, 0)
                                                .addComponent(keywordsButton))
                                            .addComponent(targetMilestoneCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(priorityCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(priorityWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(severityCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(platformCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(platformWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(osCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addComponent(versionCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(componentCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(productCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(componentWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(productWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(versionWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(osWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(issueTypeWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(severityWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(milestoneWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(urlWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(keywordsWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(statusWhiteboardWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(reportedLabel)
                                            .addComponent(blocksLabel)
                                            .addComponent(assignedLabel)
                                            .addComponent(statusLabel)
                                            .addComponent(resolutionLabel)
                                            .addComponent(dependsLabel)
                                            .addComponent(duplicateLabel)
                                            .addComponent(ccLabel)
                                            .addComponent(qaContactLabel))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(reportedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(reportedStatusLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(modifiedLabel)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(modifiedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                    .addComponent(ccField)
                                                    .addComponent(assignedField)
                                                    .addComponent(statusCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(resolutionField)
                                                    .addGroup(layout.createSequentialGroup()
                                                        .addComponent(duplicateField)
                                                        .addGap(0, 0, 0)
                                                        .addComponent(duplicateButton))
                                                    .addGroup(layout.createSequentialGroup()
                                                        .addComponent(dependsField)
                                                        .addGap(0, 0, 0)
                                                        .addComponent(dependsOnButton))
                                                    .addGroup(layout.createSequentialGroup()
                                                        .addComponent(blocksField)
                                                        .addGap(0, 0, 0)
                                                        .addComponent(blocksButton))
                                                    .addComponent(assignToDefaultCheckBox)
                                                    .addComponent(qaContactField))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(layout.createSequentialGroup()
                                                        .addComponent(assignedToStatusLabel)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(assignedToWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                    .addComponent(qaContactWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(ccWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(statusWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(resolutionWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(blocksWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(dependsOnWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(submitButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnSaveChanges)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cancelButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnDeleteTask)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(attachLogCheckBox)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(viewLogButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addGap(24, 24, 24))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {osCombo, platformCombo, priorityCombo, severityCombo});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(headerField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(separatorLabel2)
                    .addComponent(addToCategoryButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(separatorLabel)
                    .addComponent(reloadButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(separatorLabel3)
                    .addComponent(showInBrowserButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(productLabel)
                    .addComponent(productCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(productWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reportedLabel)
                    .addComponent(reportedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(modifiedLabel)
                    .addComponent(modifiedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reportedStatusLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(componentLabel)
                    .addComponent(componentCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(componentWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(assignedLabel)
                    .addComponent(assignedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(assignedToStatusLabel)
                    .addComponent(assignedToWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(versionLabel)
                    .addComponent(versionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(versionWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(assignToDefaultCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(platformLabel)
                    .addComponent(platformCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(platformWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(osCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(osWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(qaContactLabel)
                    .addComponent(qaContactField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(qaContactWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(issueTypeLabel)
                    .addComponent(issueTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(issueTypeWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ccLabel)
                    .addComponent(ccField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ccWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dummyLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(priorityLabel)
                    .addComponent(priorityCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(priorityWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(severityCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(severityWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(statusLabel)
                    .addComponent(statusCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(statusWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(targetMilestoneLabel)
                    .addComponent(targetMilestoneCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(milestoneWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(resolutionLabel)
                    .addComponent(resolutionField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(resolutionWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dummyLabel2)
                    .addComponent(duplicateLabel)
                    .addComponent(duplicateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(duplicateButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(urlLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(urlField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(urlWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dependsLabel)
                    .addComponent(dependsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dependsOnWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dependsOnButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(keywordsLabel)
                    .addComponent(keywordsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(keywordsButton)
                    .addComponent(keywordsWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(blocksLabel)
                    .addComponent(blocksField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(blocksWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(blocksButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusWhiteboardLabel)
                    .addComponent(statusWhiteboardField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(statusWhiteboardWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(timetrackingLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dummyTimetrackingLabel))
                    .addComponent(dummyTimetrackingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(attachmentsLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dummyLabel3))
                    .addComponent(dummyAttachmentsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(customFieldsPanelRight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(customFieldsPanelLeft, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(summaryLabel)
                    .addComponent(summaryWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(summaryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addCommentLabel)
                    .addComponent(scrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(viewLogButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(attachLogCheckBox)))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(submitButton)
                        .addComponent(cancelButton)
                        .addComponent(btnSaveChanges)
                        .addComponent(btnDeleteTask)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(messagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 3, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dummyCommentsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {refreshButton, reloadButton, separatorLabel, separatorLabel2, separatorLabel3, showInBrowserButton});

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {dummyLabel1, dummyLabel2, dummyLabel3, dummyTimetrackingLabel, priorityCombo});

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

    private class FormListener implements java.awt.event.ActionListener, java.awt.event.FocusListener {
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
            else if (evt.getSource() == showInBrowserButton) {
                IssuePanel.this.showInBrowserButtonActionPerformed(evt);
            }
            else if (evt.getSource() == addToCategoryButton) {
                IssuePanel.this.addToCategoryButtonActionPerformed(evt);
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
            else if (evt.getSource() == urlLabel) {
                IssuePanel.this.urlButtonActionPerformed(evt);
            }
            else if (evt.getSource() == issueTypeCombo) {
                IssuePanel.this.issueTypeComboActionPerformed(evt);
            }
            else if (evt.getSource() == submitButton) {
                IssuePanel.this.submitButtonActionPerformed(evt);
            }
            else if (evt.getSource() == cancelButton) {
                IssuePanel.this.cancelButtonActionPerformed(evt);
            }
            else if (evt.getSource() == attachLogCheckBox) {
                IssuePanel.this.attachLogCheckBoxActionPerformed(evt);
            }
            else if (evt.getSource() == viewLogButton) {
                IssuePanel.this.viewLogButtonActionPerformed(evt);
            }
            else if (evt.getSource() == btnSaveChanges) {
                IssuePanel.this.btnSaveChangesActionPerformed(evt);
            }
            else if (evt.getSource() == btnDeleteTask) {
                IssuePanel.this.btnDeleteTaskActionPerformed(evt);
            }
            else if (evt.getSource() == resolutionCombo) {
                IssuePanel.this.resolutionComboActionPerformed(evt);
            }
            else if (evt.getSource() == assignedCombo) {
                IssuePanel.this.assignedComboActionPerformed(evt);
            }
        }

        public void focusGained(java.awt.event.FocusEvent evt) {
            if (evt.getSource() == deadlineField) {
                IssuePanel.this.deadlineFieldFocusGained(evt);
            }
        }

        public void focusLost(java.awt.event.FocusEvent evt) {
            if (evt.getSource() == workedField) {
                IssuePanel.this.workedFieldFocusLost(evt);
            }
            else if (evt.getSource() == deadlineField) {
                IssuePanel.this.deadlineFieldFocusLost(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void productComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_productComboActionPerformed
        cancelHighlight(productLabel);
        // Reload componentCombo, versionCombo and targetMilestoneCombo
        BugzillaRepository repository = issue.getRepository();
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
        usingTargetMilestones = !targetMilestones.isEmpty();
        targetMilestoneCombo.setModel(toComboModel(targetMilestones));
        // Attempt to keep selection
        if (!selectInCombo(componentCombo, component, false)) {
            if (issue.isNew() && componentCombo.getModel().getSize() > 0
                    || componentCombo.getModel().getSize() == 1) {
                componentCombo.setSelectedIndex(0);
            } else {
                componentCombo.setSelectedItem(null);
            }
            storeFieldValueForNewIssue(IssueField.COMPONENT, componentCombo);
        }
        if (!selectInCombo(versionCombo, version, false)) {
            if (issue.isNew() && versionCombo.getModel().getSize() > 0
                    || versionCombo.getModel().getSize() == 1) {
                versionCombo.setSelectedIndex(0);
            } else {
                versionCombo.setSelectedItem(null);
            }
            storeFieldValueForNewIssue(IssueField.VERSION, versionCombo);
        }
        if (usingTargetMilestones) {
            if (!selectInCombo(targetMilestoneCombo, targetMilestone, false)) {
                if (issue.isNew() && targetMilestoneCombo.getModel().getSize() > 0
                        || targetMilestoneCombo.getModel().getSize() == 1) {
                    targetMilestoneCombo.setSelectedIndex(0);
                } else {
                    targetMilestoneCombo.setSelectedItem(null);
                }
                storeFieldValueForNewIssue(IssueField.MILESTONE, targetMilestoneCombo);
            }
        }
        targetMilestoneLabel.setVisible(usingTargetMilestones);
        targetMilestoneCombo.setVisible(usingTargetMilestones);
        milestoneWarning.setVisible(usingTargetMilestones);
        if (issue.isNew()) {
            issue.setFieldValue(IssueField.PRODUCT, product);
            if (BugzillaUtil.isNbRepository(repository)) { // IssueProvider 180467, 184412
                // Default target milestone
                List<String> milestones = repository.getConfiguration().getTargetMilestones(product);
                String defaultMilestone = "TBD"; // NOI18N
                if (milestones.contains(defaultMilestone)) {
                    issue.setFieldValue(IssueField.MILESTONE, defaultMilestone);
                }
                // Default version
                List<String> versions = repository.getConfiguration().getVersions(product);
                String defaultVersion = getCurrentNetBeansVersion();
                for (String v : versions) {
                    if (v.trim().toLowerCase().equals(defaultVersion.toLowerCase())) {
                        issue.setFieldValue(IssueField.VERSION, v);
                    }                        
                }
            }
            if (reloading) {
                // reload when current refresh of components finishes
                EventQueue.invokeLater(new Runnable () {
                    @Override
                    public void run () {
                        reloadForm(false);
                    }
                });
            } else {
                reloadForm(false);
            }
        }
    }//GEN-LAST:event_productComboActionPerformed

    private void statusComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statusComboActionPerformed
        cancelHighlight(statusLabel);
        cancelHighlight(resolutionLabel);
        // Hide/show resolution combo
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
        if (!resolutionField.getText().trim().equals("")) { // NOI18N
            if (statusCombo.getSelectedIndex() > resolvedIndex) {
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

    private void submitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitButtonActionPerformed
        final boolean isNew = issue.isNew();
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
        RP.post(new Runnable() {
            @Override
            public void run() {
                boolean submitOK = false;
                try {
                    submitOK = issue.submitAndRefresh();
                    if(submitOK) {
                        for (AttachmentsPanel.AttachmentInfo attachment : attachmentsPanel.getNewAttachments()) {
                            if (attachment.getFile().exists() && attachment.getFile().isFile()) {
                                if (attachment.getDescription().trim().length() == 0) {
                                    attachment.setDescription(NbBundle.getMessage(IssuePanel.class, "IssuePanel.attachment.noDescription")); // NOI18N
                                }
                                issue.addAttachment(attachment.getFile(), null, attachment.getDescription(), attachment.getContentType(), attachment.isPatch()); // NOI18N
                            } else {
                                // PENDING notify user
                            }
                        }
                        if(attachLogCheckBox.isVisible() && attachLogCheckBox.isSelected()) {
                            File f = new File(Places.getUserDirectory(), NbBugzillaConstants.NB_LOG_FILE_PATH); 
                            if(f.exists()) {
                                issue.addAttachment(f, "", NbBundle.getMessage(IssuePanel.class, "MSG_LOG_FILE_DESC"), NbBugzillaConstants.NB_LOG_FILE_ATT_CONT_TYPE, false); // NOI18N
                            }
                            BugzillaConfig.getInstance().putAttachLogFile(true);
                        } else {
                            BugzillaConfig.getInstance().putAttachLogFile(false);
                        }
                    }
                } finally {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            enableComponents(true);
                            skipReload = false;
                        }
                    });
                    handle.finish();
                    if(submitOK) {
                        if (isNew) {
                            // Show all custom fields, not only the ones shown on bug creation
                            EventQueue.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    initCustomFields();
                                }
                            });
                        }
                        reloadFormInAWT(true);
                    }
                }
            }
        });
        if (isNew) {
            BugzillaRepository repository = issue.getRepository();
            if (repository != null) {
                OwnerUtils.setLooseAssociation(BugzillaUtil.getRepository(repository), false);
            }
        }
    }//GEN-LAST:event_submitButtonActionPerformed

    private void storeCCValue() {
        Set<String> oldCCs = ccs(issue.getRepositoryFieldValue(IssueField.CC));
        Set<String> newCCs = ccs(ccField.getText());

        String removedCCs = getMissingCCs(oldCCs, newCCs);
        String addedCCs = getMissingCCs(newCCs, oldCCs);

        unsavedFields.add(IssueField.CC);
        issue.setFieldValue(IssueField.CC, ccField.getText());
        storeFieldValue(IssueField.REMOVECC, removedCCs);
        storeFieldValue(IssueField.NEWCC, addedCCs);
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
        StringBuilder ret = new StringBuilder();
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
        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    issue.updateModelAndRefresh();
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

    private void resolutionComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resolutionComboActionPerformed
        cancelHighlight(resolutionLabel);
        if (resolutionCombo.getParent() == null) {
            return;
        }
        boolean shown = "DUPLICATE".equals(resolutionCombo.getSelectedItem()); // NOI18N
        duplicateLabel.setVisible(shown);
        duplicateField.setVisible(shown);
        duplicateButton.setVisible(shown && duplicateField.isEditable());
        updateNoDuplicateId();
    }//GEN-LAST:event_resolutionComboActionPerformed

    private void keywordsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keywordsButtonActionPerformed
        String message = NbBundle.getMessage(IssuePanel.class, "IssuePanel.keywordsButton.message"); // NOI18N
        String kws = BugzillaUtil.getKeywords(message, keywordsField.getText(), issue.getRepository());
        keywordsField.setText(kws);
    }//GEN-LAST:event_keywordsButtonActionPerformed

    private void blocksButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_blocksButtonActionPerformed
        String newIssueID = BugtrackingUtil.selectIssue(
                NbBundle.getMessage(IssuePanel.class, "IssuePanel.blocksButton.message"), // NOI18N
                BugzillaUtil.getRepository(issue.getRepository()),
                this,
                new HelpCtx("org.netbeans.modules.bugzilla.blocksChooser")); // NOI18N
        if (newIssueID != null) {
            StringBuilder sb = new StringBuilder();
            if (!blocksField.getText().trim().equals("")) { // NOI18N
                sb.append(blocksField.getText()).append(',').append(' ');
            }
            sb.append(newIssueID);
            blocksField.setText(sb.toString());
        }
    }//GEN-LAST:event_blocksButtonActionPerformed

    private void dependsOnButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dependsOnButtonActionPerformed
        String newIssueID = BugtrackingUtil.selectIssue(
                NbBundle.getMessage(IssuePanel.class, "IssuePanel.dependsOnButton.message"), // NOI18N
                BugzillaUtil.getRepository(issue.getRepository()),
                this,
                new HelpCtx("org.netbeans.modules.bugzilla.dependsOnChooser")); // NOI18N
        if (newIssueID != null) {
            StringBuilder sb = new StringBuilder();
            if (!dependsField.getText().trim().equals("")) { // NOI18N
                sb.append(dependsField.getText()).append(',').append(' ');
            }
            sb.append(newIssueID);
            dependsField.setText(sb.toString());
        }
    }//GEN-LAST:event_dependsOnButtonActionPerformed

    private void componentComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_componentComboActionPerformed
        cancelHighlight(componentLabel);
        updateNoComponent();
    }//GEN-LAST:event_componentComboActionPerformed

    private void versionComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_versionComboActionPerformed
        cancelHighlight(versionLabel);
        updateNoVersion();
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
        updateNoTargetMilestone();
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
        RP.post(new Runnable() {
            @Override
            public void run() {
                issue.getRepository().refreshConfiguration();
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            reloading = true;
                            Object product = productCombo.getSelectedItem();
                            Object platform = platformCombo.getSelectedItem();
                            Object os = osCombo.getSelectedItem();
                            Object priority = priorityCombo.getSelectedItem();
                            Object severity = severityCombo.getSelectedItem();
                            Object resolution = resolutionCombo.getSelectedItem();
                            Object issueType = issueTypeCombo.getSelectedItem();
                            initCombos();
                            initCustomFields();
                            selectInCombo(productCombo, product, false);
                            selectInCombo(platformCombo, platform, false);
                            selectInCombo(osCombo, os, false);
                            selectInCombo(priorityCombo, priority, false);
                            selectInCombo(severityCombo, severity, false);
                            initStatusCombo(statusCombo.getSelectedItem().toString());
                            selectInCombo(resolutionCombo, resolution, false);
                            if (BugzillaUtil.isNbRepository(issue.getRepository())) {
                                issueTypeCombo.setSelectedItem(issueType);
                            }
                            reloadCustomFields(true);
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
        String newIssueID = BugtrackingUtil.selectIssue(
                NbBundle.getMessage(IssuePanel.class, "IssuePanel.duplicateButton.message"), //NOI18N
                BugzillaUtil.getRepository(issue.getRepository()),
                this,
                new HelpCtx("org.netbeans.modules.bugzilla.duplicateChooser")); // NOI18N
        if (newIssueID != null) {
            duplicateField.setText(newIssueID);
        }
    }//GEN-LAST:event_duplicateButtonActionPerformed

    private void addToCategoryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addToCategoryButtonActionPerformed
        Bugzilla.getInstance().getBugtrackingFactory().addToCategory(BugzillaUtil.getRepository(issue.getRepository()), issue);
    }//GEN-LAST:event_addToCategoryButtonActionPerformed

    private void assignedComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_assignedComboActionPerformed
        cancelHighlight(assignedLabel);
        if (!reloading) {
            assignedToStatusLabel.setVisible(false);
        }
        Object value = assignedCombo.getSelectedItem();
        if (value instanceof RepositoryUser) {
            String assignee = ((RepositoryUser)value).getUserName();
            BugzillaRepository repository = issue.getRepository();
            if (repository instanceof KenaiRepository) {
                assignee += '@' + ((KenaiRepository)repository).getHost();
            }
            assignedCombo.setSelectedItem(assignee);
        }
    }//GEN-LAST:event_assignedComboActionPerformed

    private void issueTypeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_issueTypeComboActionPerformed
        cancelHighlight(issueTypeLabel);
    }//GEN-LAST:event_issueTypeComboActionPerformed

    private void showInBrowserButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showInBrowserButtonActionPerformed
        try {
            URL url = new URL(issue.getRepository().getUrl() + BugzillaConstants.URL_SHOW_BUG + issue.getID());
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        } catch (MalformedURLException muex) {
            Bugzilla.LOG.log(Level.INFO, "Unable to show the issue in the browser.", muex); // NOI18N
        }
    }//GEN-LAST:event_showInBrowserButtonActionPerformed

private void urlButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_urlButtonActionPerformed
    String urlString = urlField.getText();
    if(urlString.isEmpty()) {
        return;
    }
    URL url = null;
    try {
        url = new URL(urlString);
    } catch (MalformedURLException muex) {
        if(issue != null) {
            String repoUrlString = issue.getRepository().getUrl();
            urlString = repoUrlString + (repoUrlString.endsWith("/") ? "" : "/") + urlString; // NOI18N
            try {
                url = new URL(urlString);
            } catch (MalformedURLException ex) {
                Bugzilla.LOG.log(Level.INFO, "Unable to open " + urlString, muex); // NOI18N
            }
        }
    }
    if(url != null) {
        HtmlBrowser.URLDisplayer.getDefault().showURL(url);
    }
}//GEN-LAST:event_urlButtonActionPerformed

private void deadlineFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_deadlineFieldFocusGained
    if(deadlineField.getText().trim().equals(YYYY_MM_DD)) { // NOI18N
        deadlineField.setText("");
    }
    deadlineField.setForeground(workedField.getForeground()); 
}//GEN-LAST:event_deadlineFieldFocusGained

private void deadlineFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_deadlineFieldFocusLost
    if("".equals(deadlineField.getText().trim())) { 
        deadlineField.setText(YYYY_MM_DD);
        deadlineField.setForeground(javax.swing.UIManager.getDefaults().getColor("TextField.inactiveForeground")); // NOI18N
    }
}//GEN-LAST:event_deadlineFieldFocusLost

private void workedFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_workedFieldFocusLost
    if(!"".equals(workedField.getText().trim())) { 
        String workedString = workedField.getText().trim();
        if(!workedString.trim().equals("")) {
            try {
                Double.parseDouble(workedString);
            } catch (NumberFormatException e) {
                return;
            }
        }
        double actual = getDoubleValue(actualField);
        double worked = getDoubleValue(workedField);
        double workedSum = getDoubleValue(workedSumField);
        
        double remaining = actual - worked - workedSum;
        if(remaining > 0) {
            remainingField.setText(String.valueOf(remaining));
        } else {
            remainingField.setText("0");                                        // NOI18N
        }
    }
}//GEN-LAST:event_workedFieldFocusLost

    private void attachLogCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attachLogCheckBoxActionPerformed
        switchViewLog();
    }//GEN-LAST:event_attachLogCheckBoxActionPerformed

    private void viewLogButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewLogButtonActionPerformed
        showLogFile(evt);
    }//GEN-LAST:event_viewLogButtonActionPerformed

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
                    cleared = issue.cancelChanges();
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

    private void btnSaveChangesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveChangesActionPerformed
        skipReload = true;
        enableComponents(false);
        RP.post(new Runnable() {
            @Override
            public void run() {
                boolean saved = false;
                try {
                    saved = issue.saveChanges();
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
                issue.deleteTask();
            }
        });
    }//GEN-LAST:event_btnDeleteTaskActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField actualField;
    private javax.swing.JLabel actualLabel;
    private javax.swing.JLabel actualWarning;
    private javax.swing.JTextArea addCommentArea;
    private javax.swing.JLabel addCommentLabel;
    private org.netbeans.modules.bugtracking.util.LinkButton addToCategoryButton;
    private javax.swing.JCheckBox assignToDefaultCheckBox;
    private javax.swing.JComboBox assignedCombo;
    private javax.swing.JTextField assignedField;
    private javax.swing.JLabel assignedLabel;
    private javax.swing.JLabel assignedToStatusLabel;
    private javax.swing.JLabel assignedToWarning;
    private javax.swing.JCheckBox attachLogCheckBox;
    private javax.swing.JLabel attachmentsLabel;
    private javax.swing.JButton blocksButton;
    private javax.swing.JTextField blocksField;
    private javax.swing.JLabel blocksLabel;
    private javax.swing.JLabel blocksWarning;
    private javax.swing.JButton btnDeleteTask;
    private javax.swing.JButton btnSaveChanges;
    private javax.swing.JButton cancelButton;
    private javax.swing.JTextField ccField;
    private javax.swing.JLabel ccLabel;
    private javax.swing.JLabel ccWarning;
    private javax.swing.JTextField completeField;
    private javax.swing.JLabel completeLabel;
    private javax.swing.JLabel completeWarning;
    private javax.swing.JComboBox componentCombo;
    private javax.swing.JLabel componentLabel;
    private javax.swing.JLabel componentWarning;
    private javax.swing.JPanel customFieldsPanelLeft;
    private javax.swing.JPanel customFieldsPanelRight;
    private javax.swing.JTextField deadlineField;
    private javax.swing.JLabel deadlineLabel;
    private javax.swing.JLabel deadlineWarning;
    private javax.swing.JTextField dependsField;
    private javax.swing.JLabel dependsLabel;
    private javax.swing.JButton dependsOnButton;
    private javax.swing.JLabel dependsOnWarning;
    private javax.swing.JPanel dummyAttachmentsPanel;
    private javax.swing.JPanel dummyCommentsPanel;
    private javax.swing.JLabel dummyLabel1;
    private javax.swing.JLabel dummyLabel2;
    private javax.swing.JLabel dummyLabel3;
    private javax.swing.JLabel dummyTimetrackingLabel;
    private javax.swing.JPanel dummyTimetrackingPanel;
    private javax.swing.JButton duplicateButton;
    private javax.swing.JTextField duplicateField;
    private javax.swing.JLabel duplicateLabel;
    private javax.swing.JTextField estimatedField;
    private javax.swing.JLabel estimatedLabel;
    private javax.swing.JLabel estimatedWarning;
    private javax.swing.JTextField gainField;
    private javax.swing.JLabel gainLabel;
    private javax.swing.JLabel gainWarning;
    private javax.swing.JTextField headerField;
    private javax.swing.JComboBox issueTypeCombo;
    private javax.swing.JLabel issueTypeLabel;
    private javax.swing.JLabel issueTypeWarning;
    private javax.swing.JLabel jLabel7;
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
    private javax.swing.JTextField remainingField;
    private javax.swing.JLabel remainingLabel;
    private javax.swing.JLabel remainingWarning;
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
    private javax.swing.JLabel separatorLabel3;
    private javax.swing.JComboBox severityCombo;
    private javax.swing.JLabel severityWarning;
    private org.netbeans.modules.bugtracking.util.LinkButton showInBrowserButton;
    private javax.swing.JComboBox statusCombo;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JLabel statusWarning;
    private javax.swing.JTextField statusWhiteboardField;
    private javax.swing.JLabel statusWhiteboardLabel;
    private javax.swing.JLabel statusWhiteboardWarning;
    private javax.swing.JButton submitButton;
    private javax.swing.JTextField summaryField;
    private javax.swing.JLabel summaryLabel;
    private javax.swing.JLabel summaryWarning;
    private javax.swing.JComboBox targetMilestoneCombo;
    private javax.swing.JLabel targetMilestoneLabel;
    private javax.swing.JLabel timetrackingLabel;
    private javax.swing.JPanel timetrackingPanel;
    private javax.swing.JTextField urlField;
    private org.netbeans.modules.bugtracking.util.LinkButton urlLabel;
    private javax.swing.JLabel urlWarning;
    private javax.swing.JComboBox versionCombo;
    private javax.swing.JLabel versionLabel;
    private javax.swing.JLabel versionWarning;
    private org.netbeans.modules.bugtracking.util.LinkButton viewLogButton;
    private javax.swing.JTextField workedField;
    private javax.swing.JLabel workedLabel;
    private javax.swing.JLabel workedSumField;
    private javax.swing.JLabel workedWarning;
    // End of variables declaration//GEN-END:variables

    @Override
    public Dimension getPreferredSize() {
        return getMinimumSize(); // IssueProvider 176085
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
             // IssueProvider 176085
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
    
    void makeCaretVisible(JTextArea textArea) {
        int pos = textArea.getCaretPosition();
        try {
            Rectangle rec = textArea.getUI().modelToView(textArea, pos);
            if (rec != null) {
                Point p = SwingUtilities.convertPoint(textArea, rec.x, rec.y, this);
                scrollRectToVisible(new Rectangle(p.x, p.y, rec.width, rec.height));
            }
        } catch (BadLocationException blex) {
            Bugzilla.LOG.log(Level.INFO, blex.getMessage(), blex);
        }
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

    private static final String CURRENT_NB_VERSION = "7.4";                     // NOI18N
    private String getCurrentNetBeansVersion() {        
        String version = parseProductVersion(getProductVersionValue());        
        if(version != null) {
            if(version.toLowerCase().equals("dev")) {                           // NOI18N
                return CURRENT_NB_VERSION;
            } else {                
                return version;
            }
        }
        return CURRENT_NB_VERSION;
    }

    static String parseProductVersion(String productVersionValue) {
        Pattern p = Pattern.compile("NetBeans IDE\\s([a-zA-Z0-9\\.?]*)\\s?.*"); // NOI18N
        Matcher m = p.matcher(productVersionValue);
        if(m.matches()) {
            String version = m.group(1);
            if(version != null && !version.trim().isEmpty()) {
                return version;
            }
        }
        return null;
    }

    private void addNetbeansInfo() {
        String format = NbBundle.getMessage(IssuePanel.class, "IssuePanel.newIssue.netbeansInfo"); // NOI18N
        Object[] info = new Object[] {
            getProductVersionValue(),
            System.getProperty("os.name", "unknown"), // NOI18N
            System.getProperty("os.version", "unknown"), // NOI18N
            System.getProperty("os.arch", "unknown"),  // NOI18N
            System.getProperty("java.version", "unknown"), // NOI18N
            System.getProperty("java.vm.name", "unknown"), // NOI18N
            System.getProperty("java.vm.version", "") // NOI18N
        };
        String infoTxt = MessageFormat.format(format, info);
        addCommentArea.setText(infoTxt);
    }

    public static String getProductVersionValue () {
        return MessageFormat.format(
            NbBundle.getBundle("org.netbeans.core.startup.Bundle").getString("currentVersion"), // NOI18N
            new Object[] {System.getProperty("netbeans.buildnumber")});                         // NOI18N
    }

    void opened() {
        undoRedoSupport = Bugzilla.getInstance().getUndoRedoSupport(issue);
        undoRedoSupport.register(addCommentArea);
        enableComponents(false);
        issue.opened();
    }
    
    void closed() {
        if(issue != null) {
            commentsPanel.storeSettings();
            if (undoRedoSupport != null) {
                undoRedoSupport.unregisterAll();
                undoRedoSupport = null;
            }
            issue.closed();
        }
    }

    private double getDoubleValue(JComponent field) {
        assert field instanceof JTextField || field instanceof JLabel;
        
        String txt;
        if(field instanceof JTextField) {
            txt = ((JTextField)field).getText();
        } else {
            txt = ((JLabel)field).getText();
        }
        if(txt.isEmpty()) return 0;
        try {
            return Double.parseDouble(txt);
        } catch (NumberFormatException e) {
            Bugzilla.LOG.log(Level.WARNING, txt, e);
            return 0;
        }
    }

    static void showLogFile(ActionEvent evt) {
        Action a = getShowLogAction();
        if(a != null) {
            a.actionPerformed(null);
        }
    }

    static Action getShowLogAction() {
        return FileUtil.getConfigObject("Actions/View/org-netbeans-core-actions-LogAction.instance", Action.class); // NOI18N
    }
    
    private void switchViewLog() {
        viewLogButton.setVisible(attachLogCheckBox.isSelected());
    }

    private String mergeValues (List<String> values) {
        String newValue;
        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            if (sb.length()!=0) {
                sb.append(',');
            }
            sb.append(value);
        }
        newValue = sb.toString();
        return newValue;
    }

    private void setupListeners () {
        addCommentArea.getDocument().addDocumentListener(new FieldChangeListener(addCommentArea, IssueField.DESCRIPTION));
        summaryField.getDocument().addDocumentListener(new FieldChangeListener(summaryField, IssueField.SUMMARY));
        productCombo.addActionListener(new FieldChangeListener(productCombo, IssueField.PRODUCT));
        componentCombo.addActionListener(new FieldChangeListener(componentCombo, IssueField.COMPONENT));
        versionCombo.addActionListener(new FieldChangeListener(versionCombo, IssueField.VERSION));
        platformCombo.addActionListener(new FieldChangeListener(platformCombo, IssueField.PLATFORM));
        osCombo.addActionListener(new FieldChangeListener(osCombo, IssueField.OS));
        statusCombo.addActionListener(new FieldChangeListener(statusCombo, IssueField.STATUS));
        
        resolutionCombo.addActionListener(new FieldChangeListener(resolutionCombo, IssueField.RESOLUTION));
        duplicateField.getDocument().addDocumentListener(new FieldChangeListener(duplicateField, IssueField.DUPLICATE_ID));
        
        priorityCombo.addActionListener(new FieldChangeListener(priorityCombo, IssueField.PRIORITY));
        issueTypeCombo.addActionListener(new FieldChangeListener(issueTypeCombo, IssueField.ISSUE_TYPE));
        severityCombo.addActionListener(new FieldChangeListener(severityCombo, IssueField.SEVERITY));
        targetMilestoneCombo.addActionListener(new FieldChangeListener(targetMilestoneCombo, IssueField.MILESTONE));
        assignToDefaultCheckBox.addActionListener(new FieldChangeListener(assignToDefaultCheckBox, IssueField.REASSIGN_TO_DEFAULT));
        urlField.getDocument().addDocumentListener(new FieldChangeListener(urlField, IssueField.URL));
        statusWhiteboardField.getDocument().addDocumentListener(new FieldChangeListener(statusWhiteboardField, IssueField.WHITEBOARD));
        keywordsField.getDocument().addDocumentListener(new FieldChangeListener(keywordsField, IssueField.KEYWORDS));
        qaContactField.getDocument().addDocumentListener(new FieldChangeListener(qaContactField, IssueField.QA_CONTACT));
        ccField.getDocument().addDocumentListener(new FieldChangeListener(ccField, null) {
            @Override
            public void fieldModified () {
                if (!reloading) {
                    storeCCValue();
                }
            }
        });
        dependsField.getDocument().addDocumentListener(new FieldChangeListener(dependsField, IssueField.DEPENDS_ON));
        blocksField.getDocument().addDocumentListener(new FieldChangeListener(blocksField, IssueField.BLOCKS));
        assignedField.getDocument().addDocumentListener(new FieldChangeListener(assignedField, IssueField.ASSIGNED_TO));
        assignedCombo.addActionListener(new FieldChangeListener(assignedCombo, IssueField.ASSIGNED_TO));
        
        addCommentArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate (DocumentEvent e) {
                changedUpdate(e);
            }
            @Override
            public void removeUpdate (DocumentEvent e) {
                changedUpdate(e);
            }
            @Override
            public void changedUpdate (DocumentEvent e) {
                if (!reloading && !issue.isNew() && !addCommentArea.getText().trim().isEmpty()) {
                    issue.setFieldValue(IssueField.COMMENT, addCommentArea.getText().trim());
                }
            }
        });
        
        estimatedField.getDocument().addDocumentListener(new FieldChangeListener(estimatedField, IssueField.ESTIMATED_TIME));
        workedField.getDocument().addDocumentListener(new FieldChangeListener(workedField, IssueField.WORK_TIME));
        remainingField.getDocument().addDocumentListener(new FieldChangeListener(remainingField, IssueField.REMAINING_TIME));
        deadlineField.getDocument().addDocumentListener(new FieldChangeListener(deadlineField, IssueField.DEADLINE) {
            @Override
            public boolean isEnabled () {
                return super.isEnabled() && !deadlineField.getText().trim().equals(YYYY_MM_DD);
            }
        });

        // custom fields
        for (CustomFieldInfo field : customFields) {
            if (field.comp instanceof JTextComponent) {
                ((JTextComponent) field.comp).getDocument().addDocumentListener(
                        new FieldChangeListener(field.comp, field.field));
            } else if (field.comp instanceof JComboBox) {
                ((JComboBox) field.comp).addActionListener(
                        new FieldChangeListener(field.comp, field.field));
            } else if (field.comp instanceof JList) {
                ((JList) field.comp).addListSelectionListener(
                        new FieldChangeListener(field.comp, field.field));
            } else {
                Bugzilla.LOG.log(Level.INFO, "Custom field component {0} is not supported!", field.comp); // NOI18N
            }
        }
    }

    private void storeFieldValueForNewIssue (IssueField f, JComboBox combo) {
        if (reloading && initializingProduct) {
            Object value = combo.getSelectedItem();
            issue.setFieldValue(f, value == null ? "" : value.toString());
        }
    }

    private class FieldChangeListener implements DocumentListener, ActionListener, ListSelectionListener {
        private final IssueField field;
        private final JComponent component;

        public FieldChangeListener (JComponent component, IssueField field) {
            this.component = component;
            this.field = field;
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
                if (component instanceof JTextComponent) {
                    storeFieldValue(field, (JTextComponent) component);
                } else if (component instanceof JList) {
                    storeFieldValue(field, (JList) component);
                } else if (component instanceof JComboBox) {
                    Object value = ((JComboBox) component).getSelectedItem();
                    if (value != null && assignToDefaultCheckBox.isVisible() && !assignToDefaultCheckBox.isSelected()) {
                        // when changing component or product, assign to default should be automatically selected
                        // as it is in browser 
                        if (component == productCombo && !value.equals(issue.getFieldValue(IssueField.PRODUCT))) {
                            assignToDefaultCheckBox.doClick();
                        } else if (component == componentCombo && !value.equals(issue.getFieldValue(IssueField.COMPONENT))) {
                            assignToDefaultCheckBox.doClick();
                        }
                    }
                    storeFieldValue(field, (JComboBox) component);
                } else if (component instanceof JCheckBox) {
                    storeFieldValue(field, ((JCheckBox) component).isSelected() ? "1" : "0");
                }
            }
        }
        
        public boolean isEnabled () {
            return component.isVisible() && component.isEnabled();
        }
    }

    class CancelHighlightDocumentListener implements DocumentListener {
        private JComponent label;
        
        CancelHighlightDocumentListener(JComponent label) {
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
    }

    class CyclicDependencyDocumentListener implements DocumentListener {

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

    private class DuplicateListener implements DocumentListener {
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
            updateNoDuplicateId();
        }
    }

    private void updateNoDuplicateId() {
        boolean newNoDuplicateId = "DUPLICATE".equals(resolutionCombo.getSelectedItem()) && "".equals(duplicateField.getText().trim());
        if(newNoDuplicateId != noDuplicateId) {
            noDuplicateId = newNoDuplicateId;
            updateMessagePanel();
        }
    }
    
    private static class CustomFieldInfo {
        CustomIssueField field;
        JLabel label;
        JComponent comp;
        JLabel warning;

        CustomFieldInfo(CustomIssueField field, JLabel label, JComponent comp, JLabel warning) {
            this.field = field;
            this.label = label;
            this.comp = comp;
            this.warning = warning;
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