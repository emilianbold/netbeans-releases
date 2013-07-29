/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.odcs.tasks.issue;

import com.tasktop.c2c.server.tasks.domain.Iteration;
import org.netbeans.modules.bugtracking.util.AttachmentsPanel;
import com.tasktop.c2c.server.tasks.domain.Keyword;
import com.tasktop.c2c.server.tasks.domain.Milestone;
import com.tasktop.c2c.server.tasks.domain.Priority;
import com.tasktop.c2c.server.tasks.domain.Product;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.TaskResolution;
import com.tasktop.c2c.server.tasks.domain.TaskSeverity;
import com.tasktop.c2c.server.tasks.domain.TaskStatus;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;
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
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
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
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.ListModel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.table.TableModel;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.cache.IssueCache;
import org.netbeans.modules.bugtracking.issuetable.TableSorter;
import org.netbeans.modules.bugtracking.team.spi.TeamProject;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.util.LinkButton;
import org.netbeans.modules.bugtracking.util.OwnerUtils;
import org.netbeans.modules.bugtracking.util.UIUtils;
import org.netbeans.modules.mylyn.util.WikiPanel;
import org.netbeans.modules.mylyn.util.WikiUtils;
import org.netbeans.modules.odcs.tasks.ODCS;
import org.netbeans.modules.odcs.tasks.issue.ODCSIssue.Attachment;
import org.netbeans.modules.odcs.tasks.repository.ODCSRepository;
import org.netbeans.modules.odcs.tasks.util.ODCSUtil;
import org.netbeans.modules.spellchecker.api.Spellchecker;
import org.openide.awt.HtmlBrowser;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author tomas
 */
public class IssuePanel extends javax.swing.JPanel implements Scrollable {
    final SimpleDateFormat INPUT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd"); // NOI18N
    
    private static final Color HIGHLIGHT_COLOR = new Color(217, 255, 217);
    private static final String RESOLUTION_RESOLVED = "RESOLVED";               // NOI18N    
    private static final String STATUS_FIXED = "FIXED";                         // NOI18N
    private static final String STATUS_UNCONFIRMED = "UNCONFIRMED";             // NOI18N
    private static final String RESOLUTION_DUPLICATE = "DUPLICATE";             // NOI18N
    private static final String DEFAULT_PRIORITY = "Normal";                    // NOI18N
    private static final String DEFAULT_SEVERITY = "normal";                    // NOI18N
    private static final String ICON_PATH_WARNING = "org/netbeans/modules/odcs/tasks/resources/warning.gif"; //NOI18N
    private static final String ICON_PATH_ERROR = "org/netbeans/modules/odcs/tasks/resources/error.gif"; //NOI18N
    private static final String ICON_PATH_INFO = "org/netbeans/modules/odcs/tasks/resources/info.png"; //NOI18N

    private ODCSIssue issue;
    
    private boolean reloading;
    private boolean skipReload;
    
    private CommentsPanel commentsPanel;
    private AttachmentsPanel attachmentsPanel;
    
    private Map<String,String> initialValues = new HashMap<String,String>();
    private List<String> keywords = new LinkedList<String>();
    
    // message panel 
    private boolean cyclicDependency = false;
    private boolean sameParent = false;
    private boolean noSummary = false;
    private boolean invalidTag = false;
    private boolean noComponent = false;
    private boolean noIteration = false;
    private boolean noTargetMilestione = false;
    private boolean noDuplicateId = false;
    private List<String> fieldErrors = new LinkedList<String>();
    private List<String> fieldWarnings = new LinkedList<String>();
    private int resolvedIndex;
    private WikiPanel descriptionPanel;
    private WikiPanel addCommentPanel;
    private static String wikiLanguage = "";
    private static final RequestProcessor RP = new RequestProcessor("ODCS Task Panel", 5, false); //NOI18N
    private Set<String> invalidDateFields = new HashSet<String>(2);
    private static final Set<IssueField> DATE_INPUT_FIELDS = new HashSet<IssueField>(Arrays.asList(IssueField.DUEDATE));
    
    private JTable subTaskTable;
    private JScrollPane subTaskScrollPane;
    
    /**
     * Creates new form IssuePanel
     */
    public IssuePanel() {
        initComponents();
        
        separatorLabel.setVisible(false);
        separatorLabel3.setVisible(false);
        
        updateReadOnlyField(reportedField);
        updateReadOnlyField(modifiedField);
        updateReadOnlyField(headerField);
        
        messagePanel.setBackground(getBackground());
        
//        customFieldsPanelLeft.setBackground(getBackground());
//        customFieldsPanelRight.setBackground(getBackground());
        Font font = reportedLabel.getFont();
        headerField.setFont(font.deriveFont((float)(font.getSize()*1.7)));
        duplicateField.setVisible(false);
        duplicateButton.setVisible(false);
        attachDocumentListeners();

        GroupLayout layout = (GroupLayout) getLayout();

        // Comments panel
        commentsPanel = new CommentsPanel();
        commentsPanel.setNewCommentHandler(new CommentsPanel.NewCommentHandler() {
            @Override
            public void append(String text) {
                addCommentPanel.appendCodeText(text);
                scrollRectToVisible(addCommentPanel.getBounds());
            }
        });
        attachmentsPanel = new AttachmentsPanel(this);
        layout.replace(dummyCommentsPanel, commentsPanel);
        layout.replace(dummyAttachmentsPanel, attachmentsPanel);
        attachmentsLabel.setLabelFor(attachmentsPanel);
        initSpellChecker();
        initDefaultButton();
    }

    void opened() {
        // Hack - reset any previous modifications when the issue window is reopened
        reloadForm(true);
        if(issue.getTaskData().isPartial()) {
            // XXX HACK! ahoj ondra - this is meant to be a temporary hack until 
            // odcs tasks are rewritten to work with offline mode.
            // see also issue #231205
            refreshIssue(true);
        }
    }
    
    void closed() {
        if(issue != null) {
            commentsPanel.storeSettings();
        }
    }

    private void initSpellChecker () {
        Spellchecker.register(summaryField);
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

    private void initWikiPanels(){
        GroupLayout layout = (GroupLayout) getLayout();
        RepositoryConfiguration rc = issue.getRepository().getRepositoryConfiguration(false);
        wikiLanguage = rc.getMarkupLanguage();
        addCommentPanel = WikiUtils.getWikiPanel(wikiLanguage, true, true);
        layout.replace(dummyAddCommentPanel, addCommentPanel);

        commentsPanel.setWikiLanguage(wikiLanguage);

        descriptionPanel = WikiUtils.getWikiPanel(wikiLanguage, false, true);
        layout.replace(dummyDescriptionPanel, descriptionPanel);
    }

    ODCSIssue getIssue() {
        return issue;
    }

    public void setIssue(ODCSIssue issue) {
        assert SwingUtilities.isEventDispatchThread() : "Accessing Swing components. Do not call outside event-dispatch thread!"; // NOI18N
        headerField.setText(issue.getDisplayName());
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
                    updateInvalidTag();
                }
            });
        }
        this.issue = issue;
        initCombos();
        initWikiPanels();
//        initCustomFields(); XXX
        
        Collection<Keyword> kws = issue.getRepository().getRepositoryConfiguration(false).getKeywords();
        keywords.clear();
        for (Keyword keyword : kws) {
            keywords.add(keyword.getName());
        }
                
        reloadForm(true);

        if (issue.isNew()) {
            selectProduct();
        }

        // Hack to "link" the width of both columns
//        Dimension dim = ccField.getPreferredSize();
//        int width1 = Math.max(osCombo.getPreferredSize().width, platformCombo.getPreferredSize().width);
//        int width2 = Math.max(priorityCombo.getPreferredSize().width, showIssueType ? issueTypeCombo.getPreferredSize().width : severityCombo.getPreferredSize().width);
//        int gap = LayoutStyle.getInstance().getPreferredGap(osCombo, platformCombo, LayoutStyle.ComponentPlacement.RELATED, SwingConstants.EAST, this);
//        ccField.setPreferredSize(new Dimension(2*Math.max(width1,width2)+gap,dim.height));
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
    
    private void selectProduct() {
//        if (ownerInfo != null) {
//            String owner = findInModel(productCombo, ownerInfo.getOwner());
//            selectInCombo(productCombo, owner, true);
//            List<String> data = ownerInfo.getExtraData();
//            if (data != null && data.size() > 0) {
//                String component = findInModel(componentCombo, data.get(0));
//                selectInCombo(componentCombo, component, true);
//            }
//        } else {
//            BugzillaRepository repository = issue.getRepository();
//            if (repository instanceof KenaiRepository) {
//                String productName = ((KenaiRepository)repository).getProductName();
//                selectInCombo(productCombo, productName, true);
//            } else if (BugzillaUtil.isNbRepository(repository)) {
//                // IssueProvider 181224
//                String defaultProduct = "ide"; // NOI18N
//                String defaultComponent = "Code"; // NOI18N
//                productCombo.setSelectedItem(defaultProduct);
//                componentCombo.setSelectedItem(defaultComponent);
//            } else {
//                productCombo.setSelectedIndex(0);
//            }
//        }
        
        productCombo.setSelectedIndex(0);
    }    
    
    private void initCombos() {
        RepositoryConfiguration rc = issue.getRepository().getRepositoryConfiguration(false);
        
        productCombo.setModel(toComboModel(rc.getProducts()));
        productCombo.setRenderer(new ClientDataRenderer());
        
        // componentCombo, versionCombo, targetMilestoneCombo are filled
        // automatically when productCombo is set/changed

        // List<String> resolutions = new LinkedList<String>(cd.getResolutions());
        resolutionCombo.setModel(toComboModel(rc.getResolutions()));
        resolutionCombo.setRenderer(new ClientDataRenderer());
        
        initPriorityCombo(rc);
        
        initSeverityCombo(rc);

        ownerCombo.setModel(toComboModel(rc.getUsers()));
        ownerCombo.setRenderer(new ClientDataRenderer());

        iterationCombo.setModel(toComboModel(new ArrayList<Iteration>(issue.isNew() 
                ? rc.getActiveIterations()
                : rc.getIterations())));
        iterationCombo.setRenderer(new ClientDataRenderer());

        issueTypeCombo.setModel(toComboModel(new ArrayList<String>(rc.getTaskTypes())));
        
        // statusCombo and resolution fields are filled in reloadForm
    }

    private ComboBoxModel toComboModel(List items) {
        return new DefaultComboBoxModel(items.toArray());
    }

    private int oldCommentCount;
    void reloadForm(boolean force) {
        if (skipReload) { // XXX when set?
            return;
        }
        int noWarnings = fieldWarnings.size();
        int noErrors = fieldErrors.size();
        if (force) {
            fieldWarnings.clear();
            fieldErrors.clear();
        }
        reloading = true;
        boolean isNew = issue.isNew();
        
        org.openide.awt.Mnemonics.setLocalizedText(addCommentLabel, NbBundle.getMessage(IssuePanel.class, isNew ? "IssuePanel.description" : "IssuePanel.addCommentLabel.text")); // NOI18N
        reportedLabel.setVisible(!isNew);
        reportedField.setVisible(!isNew);
        modifiedLabel.setVisible(!isNew);
        modifiedField.setVisible(!isNew);
        resolutionLabel.setVisible(!isNew);
        resolutionCombo.setVisible(!isNew);
        separator.setVisible(!isNew);
        commentsPanel.setVisible(!isNew);
        attachmentsLabel.setVisible(!isNew);
        attachmentsPanel.setVisible(!isNew);
        refreshButton.setVisible(!isNew);
        separatorLabel.setVisible(!isNew);
        cancelButton.setVisible(!isNew);
        separatorLabel3.setVisible(!isNew);
        showInBrowserButton.setVisible(!isNew);
        parentLabel.setVisible(!isNew);
        parentField.setVisible(!isNew);
        parentButton.setVisible(!isNew);
        subtaskField.setVisible(!isNew);
        subtaskButton.setVisible(!isNew);
        externalLabel.setVisible(!isNew);
        externalField.setVisible(!isNew);
        dummySubtaskPanel.setVisible(false);
        org.openide.awt.Mnemonics.setLocalizedText(submitButton, NbBundle.getMessage(IssuePanel.class, isNew ? "IssuePanel.submitButton.text.new" : "IssuePanel.submitButton.text")); // NOI18N
        descriptionLabel.setVisible(!isNew);
        descriptionPanel.setVisible(!isNew);
        
        final String parentId = issue.getParentId();
        boolean hasParent = (parentId != null) && (parentId.trim().length() > 0);
        if  (hasParent) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    ODCSIssue parentIssue = issue.getRepository().getIssueCache().getIssue(parentId);
                    if (parentIssue == null) {
                        parentIssue = issue.getRepository().getIssue(parentId);
                    }
                    final ODCSIssue parent = parentIssue;
                    if(parent == null) {
                        // how could this be possible? parent removed?
                        ODCS.LOG.log(Level.INFO, "issue {0} is referencing not available parent with key {1}", new Object[]{issue.getID(), parentId}); // NOI18N
                        return;
                    }
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            parentHeaderPanel.setVisible(true);
                            parentHeaderPanel.removeAll();
                            headerLabel.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/odcs/tasks/resources/subtask.png", true)); // NOI18N
                            GroupLayout layout = new GroupLayout(parentHeaderPanel);
                            JLabel parentLabel = new JLabel();
                            parentLabel.setText(parent.getSummary());
                            LinkButton parentButton = new LinkButton(new AbstractAction() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    ODCSUtil.openIssue(parent);
                                }
                            });
                            parentButton.setText(parentId + ':'); // NOI18N
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
        
        if (isNew && force) {
            // Preselect the first product
            selectProduct();
            initStatusCombo(STATUS_UNCONFIRMED, null);
        } else {
            String format = NbBundle.getMessage(IssuePanel.class, "IssuePanel.headerLabel.format"); // NOI18N
            if(isNew) {
                headerField.setText(""); // NOI18N
            } else {
                String headerTxt = MessageFormat.format(format, issue.getID(), issue.getSummary());
                headerField.setText(headerTxt);
            }
            Dimension dim = headerField.getPreferredSize();
            headerField.setMinimumSize(new Dimension(0, dim.height));
            headerField.setPreferredSize(new Dimension(0, dim.height));
            reloadField(force, issueTypeCombo, IssueField.TASK_TYPE, issueTypeWarning, issueTypeLabel);
            reloadField(force, summaryField, IssueField.SUMMARY, summaryWarning, summaryLabel);
            reloadField(force, productCombo, IssueField.PRODUCT, productWarning, productLabel);
            reloadField(force, componentCombo, IssueField.COMPONENT, componentWarning, componentLabel);
            reloadField(force, releaseCombo, IssueField.MILESTONE, releaseWarning, releaseLabel);
            reloadField(force, descriptionPanel, IssueField.DESCRIPTION, descriptionPanel.getWarningLabel(), descriptionLabel); // NOI18N
            String status = reloadField(force, statusCombo, IssueField.STATUS, statusWarning, statusLabel);
            initStatusCombo(status, issue.getFieldValue(IssueField.STATUS));
            reloadField(force, resolutionCombo, IssueField.RESOLUTION, resolutionWarning, "resolution"); // NOI18N
            String initialResolution = initialValues.get(IssueField.RESOLUTION.getKey());
            if (RESOLUTION_DUPLICATE.equals(initialResolution)) { // NOI18N // XXX no string gvalues
                duplicateField.setEditable(false);
                duplicateButton.setVisible(false);
                duplicateField.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
                duplicateField.setBackground(getBackground());
            } else {
                JTextField field = new JTextField();
                duplicateField.setEditable(true);
                duplicateField.setBorder(field.getBorder());
                duplicateField.setBackground(field.getBackground());
            }
            reloadField(force, priorityCombo, IssueField.PRIORITY, priorityWarning, priorityLabel);
            reloadField(force, severityCombo, IssueField.SEVERITY, severityWarning, priorityLabel);
            reloadField(force, iterationCombo, IssueField.ITERATION, iterationWarning, iterationLabel);
            reloadField(force, keywordsField, IssueField.KEYWORDS, keywordsWarning, keywordsLabel);

            if (!isNew) {
                // reported field
                format = NbBundle.getMessage(IssuePanel.class, "IssuePanel.reportedLabel.format"); // NOI18N
                Date creation = issue.getCreatedDate();
                String creationTxt = creation != null ? DateFormat.getDateTimeInstance().format(creation) : ""; // NOI18N
                
                String reporter = issue.getFieldValue(IssueField.REPORTER);
                String reporterName = issue.getPersonName(IssueField.REPORTER);
                String reporterTxt = ((reporterName == null) || (reporterName.trim().length() == 0)) ? reporter : reporterName;
                String reportedTxt = MessageFormat.format(format, creationTxt, reporterTxt);
                reportedField.setText(reportedTxt);
                
                fixPrefSize(reportedField);

                // modified field
                Date modification = issue.getCreatedDate();
                String modifiedTxt = modification != null ? DateFormat.getDateTimeInstance().format(modification) : ""; // NOI18N
                modifiedField.setText(modifiedTxt);
                fixPrefSize(modifiedField);
            }

            String assignee = issue.getFieldValue(IssueField.OWNER);
//          XXX  String selectedAssignee = (assignedField.getParent() == null) ? ownerCombo.getSelectedItem().toString() : assignedField.getText();
//            if (isKenaiRepository && (assignee.trim().length() > 0) && (force || !selectedAssignee.equals(assignee))) {
//                int index = assignee.indexOf('@');
//                String userName = (index == -1) ? assignee : assignee.substring(0,index);
//                String host = ((KenaiRepository) issue.getRepository()).getHost();
//                JLabel label = KenaiUtil.createUserWidget(userName, host, KenaiUtil.getChatLink(issue.getID()));
//                label.setText(null);
//                ((javax.swing.GroupLayout)getLayout()).replace(assignedToStatusLabel, label);
//                label.setVisible(assignedToStatusLabel.isVisible());
//                assignedToStatusLabel = label;
//            }
//            if (force) {
//                assignedToStatusLabel.setVisible(assignee.trim().length() > 0);
//            }
//            if (assignedField.getParent() == null) {
//                reloadField(force, ownerCombo, IssueField.ASSIGNED_TO, assignedToWarning, assignedLabel);
//            } else {
//                reloadField(force, assignedField, IssueField.ASSIGNED_TO, assignedToWarning, assignedLabel);
//            }
                
            reloadField(force, ownerCombo, IssueField.OWNER, ownerWarning, ownerLabel);
            
            reloadField(force, ccField, IssueField.CC, ccWarning, ccLabel);
            reloadField(force, subtaskField, IssueField.SUBTASK, subtaskWarning, subtaskLabel);
            reloadField(force, parentField, IssueField.PARENT, parentWarning, parentLabel);
            reloadField(force, dueDateField, IssueField.DUEDATE, dueDateWarning, dueDateLabel);
            reloadField(force, estimateField, IssueField.ESTIMATE, estimateWarning, estimateLabel);
            reloadField(force, foundInField, IssueField.FOUNDIN, foundInWarning, foundInLabel);
//            reloadCustomFields(force); XXX
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
        attachmentsPanel.setAttachments(attachments);
        UIUtils.keepFocusedComponentVisible(commentsPanel, this);
        UIUtils.keepFocusedComponentVisible(attachmentsPanel, this);
        if (force && !isNew) {
            addCommentPanel.clear();
        }
        
        boolean hasSubtasks = issue.hasSubtasks();
        if (subTaskScrollPane != null) {
            subTaskScrollPane.setVisible(hasSubtasks);
        }        
        if (hasSubtasks) {
            if (subTaskTable == null) {
                subTaskTable = new JTable();
//                subTaskTable.setDefaultRenderer(JiraStatus.class, new StatusRenderer());
//                subTaskTable.setDefaultRenderer(Priority.class, new PriorityRenderer());
//                subTaskTable.setDefaultRenderer(IssueType.class, new TypeRenderer());
                subTaskTable.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 2) {
                            Point p = e.getPoint();
                            int row = subTaskTable.rowAtPoint(p);
                            TableModel model = subTaskTable.getModel();
                            String id = (String)model.getValueAt(row,0);
                            ODCSRepository repository = issue.getRepository();
                            ODCSIssue subTask = repository.getIssueCache().getIssue(id);
                            if (subTask == null) {
                                subTask = repository.getIssue(id);
                            }
                            ODCSUtil.openIssue(subTask);
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

    private String reloadField(boolean force, JComponent component, IssueField field, JLabel warningLabel, JLabel fieldLabel) {
        return reloadField(force, component, field, warningLabel, fieldName(fieldLabel));
    }
    
    private String reloadField(boolean force, JComponent component, IssueField field, JLabel warningLabel, String fieldName) {
        String currentValue = null;
        if (!issue.isFieldValueAvailable(field)) {
            return currentValue;
        }
        boolean isNew = issue.isNew();
        if (!force) {
            if (component instanceof JComboBox) {
                currentValue = getSelectedValue((JComboBox)component);
                if (currentValue == null) {
                    currentValue  = ""; // NOI18N
                }
            } else if (component instanceof JTextComponent) {
                currentValue = ((JTextComponent)component).getText();
                if (DATE_INPUT_FIELDS.contains(field) && !currentValue.trim().isEmpty()) {
                    Date date = ODCSUtil.parseTextDate(currentValue.trim(), INPUT_DATE_FORMAT);
                    if (date != null) {
                        currentValue = Long.toString(date.getTime());
                    }
                }
            } else if (component instanceof JList) {
                JList list = (JList)component;
                StringBuilder sb = new StringBuilder();
                for (Object value : list.getSelectedValues()) {
                    if (sb.length()!=0) {
                        sb.append(',');
                    }
                    sb.append(value);
                }
                currentValue = sb.toString();
            } else if (component instanceof WikiPanel) {
                currentValue = ((WikiPanel)component).getWikiFormatText();
            }
        }
        String initialValue = initialValues.get(field.getKey());
        String newValue;
        if (component instanceof JList) {
            StringBuilder sb = new StringBuilder();
            for (String value : issue.getFieldValues(field)) {
                if (sb.length()!=0) {
                    sb.append(',');
                }
                sb.append(value);
            }
            newValue = sb.toString();
        } else {
            newValue = issue.getFieldValue(field);
        }
        boolean valueModifiedByUser = (currentValue != null) && (initialValue != null) && !currentValue.equals(initialValue);
        boolean valueModifiedByServer = (initialValue != null) && (newValue != null) && !initialValue.equals(newValue);
        if (force || !valueModifiedByUser) {
            if (component instanceof JComboBox) {
                JComboBox combo = (JComboBox)component;
                selectInCombo(combo, newValue, true);
            } else if (component instanceof JTextComponent) {
                String value = newValue;
                if (DATE_INPUT_FIELDS.contains(field) && !newValue.isEmpty()) {
                    Date date = ODCSUtil.parseLongDate(newValue, new DateFormat[] { INPUT_DATE_FORMAT });
                    if (date != null) {
                        value = INPUT_DATE_FORMAT.format(date);
                    }
                }
                ((JTextComponent)component).setText(value);
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
            } else if (component instanceof WikiPanel) {
                ((WikiPanel)component).setWikiFormatText(newValue);
            }
            if (force) {
                if (warningLabel != null) {
                    warningLabel.setIcon(null);
                }
            } else {
                if (!isNew && valueModifiedByServer && (warningLabel != null)) {
                    warningLabel.setIcon(ImageUtilities.loadImageIcon(ICON_PATH_WARNING, true));
                    String messageFormat = NbBundle.getMessage(IssuePanel.class, "IssuePanel.fieldModifiedWarning"); // NOI18N
                    String message = MessageFormat.format(messageFormat, fieldName, currentValue, newValue);
                    fieldWarnings.add(message);
                    warningLabel.setToolTipText(message);
                }
            }
            currentValue = newValue;
        } else {
            if (!isNew && valueModifiedByServer && (warningLabel != null)) {
                warningLabel.setIcon(ImageUtilities.loadImageIcon(ICON_PATH_ERROR, true));
                String messageFormat = NbBundle.getMessage(IssuePanel.class, "IssuePanel.fieldModifiedError"); // NOI18N
                String message = MessageFormat.format(messageFormat, fieldName, newValue);
                fieldErrors.add(message);
                warningLabel.setToolTipText(message);
            }
        }
        if ((IssueField.SUMMARY == field) || (IssueField.PRIORITY == field) /* XXX || (field instanceof CustomIssueField)*/) {
            warningLabel.setVisible(warningLabel.getIcon() != null);
        }
        initialValues.put(field.getKey(), newValue);
        return currentValue;
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
    
    private boolean selectInCombo(JComboBox combo, Object value, boolean forceInModel) {
        if (value == null) {
            return false;
        }
  
        combo.setSelectedItem(value);
        if (!value.equals(combo.getSelectedItem())) {
            ComboBoxModel model = combo.getModel();
            for (int i = 0; i < model.getSize(); ++i) {
                Object item = model.getElementAt(i);
                if (value.toString().equals(convert(item))) {
                    combo.setSelectedItem(item);
                    break;
                }
            }
        }
        
        if (forceInModel && !value.toString().isEmpty() && !value.toString().equals(getSelectedValue(combo))) { // NOI18N
            // Reload of server attributes is needed - workarounding it
            ComboBoxModel model = combo.getModel();
            if (model instanceof DefaultComboBoxModel) {
                ((DefaultComboBoxModel)model).insertElementAt(value, 0);
                combo.setSelectedIndex(0);
            }
        }
        return value.toString().equals(getSelectedValue(combo));
    }    

    private String getSelectedValue(JComboBox combo) {
        Object item = combo.getSelectedItem();
        if(item == null) {
            return null;
        }
        String value = convert(item);
        return value;
    }

    private void initStatusCombo(String status, String initialValue) {
        // Init statusCombo - allowed transitions (heuristics):
        // Open -> Open-Unconfirmed-Reopened+Resolved
        // Resolved -> Reopened+Close
        // Close-Resolved -> Reopened+Resolved+(Close with higher index)
        RepositoryConfiguration rc = issue.getRepository().getRepositoryConfiguration(false);
        
        List<TaskStatus> statuses = rc.computeValidStatuses(initialValue == null ? null : ODCSUtil.getStatusByValue(rc, initialValue));
        
        // XXX evaluate statuses for open and active
        resolvedIndex = statuses.size(); // if there is no RESOLVED
        for (int i = 0; i < statuses.size(); i++) {
            TaskStatus s = statuses.get(i);
            if(s.getValue().equals(RESOLUTION_RESOLVED)) {
                resolvedIndex = i;
                break;
            }
        }
        
        statusCombo.setModel(toComboModel(statuses));
        statusCombo.setRenderer(new ClientDataRenderer());
        selectInCombo(statusCombo, ODCSUtil.getStatusByValue(rc, status), false);
    }    
    
    private void updateReadOnlyField(JTextField field) {
        if ("GTK".equals(UIManager.getLookAndFeel().getID())) { // NOI18N
            field.setUI(new BasicTextFieldUI());
        }
        field.setBackground(getBackground());
        Caret caret = field.getCaret();
        if (caret instanceof DefaultCaret) {
            ((DefaultCaret)caret).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        }
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

    private void updateInvalidTag() {
        boolean invalidFound = false;
        StringTokenizer st = new StringTokenizer(keywordsField.getText(), ", \t\n\r\f"); // NOI18N
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (!keywords.contains(token)) {
                invalidFound = true;
                break;
            }
        }
        if (invalidFound != invalidTag) {
            invalidTag = invalidFound;
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

    private void updateNoIteration () {
        boolean newNoVersion = (releaseCombo.getSelectedItem() == null);
        if (noIteration != newNoVersion) {
            noIteration = newNoVersion;
            updateMessagePanel();
        }
    }

    private void updateNoMilestone() {
        boolean newNoTargetMilestone = (iterationCombo.getSelectedItem() == null);
        if (noTargetMilestione != newNoTargetMilestone) {
            noTargetMilestione = newNoTargetMilestone;
            updateMessagePanel();
        }
    }
    
    private void attachDocumentListeners() {
        // XXX test, add missing fields
        ccField.getDocument().addDocumentListener(new CancelHighlightDocumentListener(ccLabel));        
        parentField.getDocument().addDocumentListener(new CancelHighlightDocumentListener(parentLabel));
        subtaskField.getDocument().addDocumentListener(new CancelHighlightDocumentListener(parentLabel));
        CyclicDependencyDocumentListener cyclicDependencyListener = new CyclicDependencyDocumentListener();
        parentField.getDocument().addDocumentListener(cyclicDependencyListener);
        subtaskField.getDocument().addDocumentListener(cyclicDependencyListener);
        duplicateField.getDocument().addDocumentListener(new DuplicateListener());
        dueDateField.getDocument().addDocumentListener(new DateFieldListener(dueDateField, dueDateLabel));
    }

    private void updateFieldStatuses() {
        updateFieldStatus(IssueField.SUMMARY, summaryLabel);
        updateFieldStatus(IssueField.PRODUCT, productLabel);
        updateFieldStatus(IssueField.COMPONENT, componentLabel);
        updateFieldStatus(IssueField.FOUNDIN, foundInLabel);
        updateFieldStatus(IssueField.STATUS, statusLabel);
        updateFieldStatus(IssueField.RESOLUTION, resolutionLabel);
        updateFieldStatus(IssueField.PRIORITY, priorityLabel);
        updateFieldStatus(IssueField.SEVERITY, severityLabel);
        updateFieldStatus(IssueField.MILESTONE, releaseLabel);
        updateFieldStatus(IssueField.DUEDATE, dueDateLabel);
        updateFieldStatus(IssueField.ITERATION, iterationLabel);
        updateFieldStatus(IssueField.OWNER, ownerLabel);
        updateFieldStatus(IssueField.ESTIMATE, estimateLabel);
        updateFieldStatus(IssueField.PARENT, parentLabel);
        updateFieldStatus(IssueField.SUBTASK, subtaskLabel);
        updateFieldStatus(IssueField.CC, ccLabel);
        updateFieldStatus(IssueField.KEYWORDS, keywordsLabel);
        updateFieldStatus(IssueField.TASK_TYPE, issueTypeLabel);
        updateFieldStatus(IssueField.MODIFIED, modifiedLabel);
//        for (CustomFieldInfo field : customFields) {
//            updateFieldStatus(field.field, field.label);
//        }
    }

    private void updateFieldStatus(IssueField field, JComponent label) {
        assert label instanceof JButton || label instanceof JLabel;
        boolean highlight = !issue.getTaskData().isNew() && (issue.getFieldStatus(field) != ODCSIssue.FIELD_STATUS_UPTODATE);
        label.setOpaque(highlight);
        if (highlight) {
            label.setBackground(HIGHLIGHT_COLOR);
        }
    }

    PropertyChangeListener cacheListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getSource() != IssuePanel.this.issue) {
                return;
            }
            if (IssueStatusProvider.EVENT_SEEN_CHANGED.equals(evt.getPropertyName())) {
                updateFieldStatuses();
            }
        }
    };

    private void findIssue(JTextField fld, String msg, String helpCtx, boolean append) {
        String newIssueID = BugtrackingUtil.selectIssue(
            NbBundle.getMessage(IssuePanel.class, msg), 
            ODCSUtil.getRepository(issue.getRepository()),
            this,
            new HelpCtx(helpCtx));
        if (newIssueID != null) {
            if(append) {
                StringBuilder sb = new StringBuilder();
                if (!fld.getText().trim().equals("")) {                         // NOI18N
                    sb.append(fld.getText()).append(',').append(' ');           // NOI18N
                }
                sb.append(newIssueID);
                fld.setText(sb.toString());                
            } else {
                fld.setText(newIssueID);
            }
        }
    }

    private void initPriorityCombo (RepositoryConfiguration rc) {
        List<Priority> priorities = rc.getPriorities();
        Priority defaultPriority = null;
        for (Priority p : priorities) {
            if (DEFAULT_PRIORITY.equals(p.getValue())) {
                defaultPriority = p;
            }
        }
        priorityCombo.setModel(toComboModel(priorities));
        priorityCombo.setRenderer(new ClientDataRenderer());
        if (defaultPriority != null) {
            priorityCombo.setSelectedItem(defaultPriority);
        }
    }

    private void initSeverityCombo (RepositoryConfiguration rc) {
        List<TaskSeverity> severities = rc.getSeverities();
        TaskSeverity defaultSeverity = null;
        for (TaskSeverity s : severities) {
            if (DEFAULT_SEVERITY.equals(s.getValue())) {
                defaultSeverity = s;
            }
        }
        severityCombo.setModel(toComboModel(severities));
        severityCombo.setRenderer(new ClientDataRenderer());
        if (defaultSeverity != null) {
            severityCombo.setSelectedItem(defaultSeverity);
        }
    }

    @Messages({
        "# {0} - issue id",
        "IssuePanel.refreshMessage=Refreshing issue {0}"
    })
    private void refreshIssue (final boolean force) {
        final ProgressHandle handle = ProgressHandleFactory.createHandle(Bundle.IssuePanel_refreshMessage(issue.getID()));
        handle.start();
        handle.switchToIndeterminate();
        skipReload = true;
        enableComponents(false);
        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
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
                    reloadFormInAWT(force);
                }
            }
        });
    }

    private static String convert(Object item) {
        if(item instanceof Priority) {
            return ((Priority) item).getValue();
        } else if(item instanceof Iteration) {
            return ((Iteration) item).getValue();
        } else if(item instanceof TaskSeverity) {
            return ((TaskSeverity) item).getValue();
        } else if(item instanceof TaskResolution) {
            return ((TaskResolution) item).getValue();
        } else if(item instanceof Product) {
            return ((Product) item).getName();
        } else if(item instanceof TaskUserProfile) {
            return ((TaskUserProfile) item).getLoginName();
        } else if(item instanceof TaskStatus) {
            return ((TaskStatus) item).getValue();
        } else if(item instanceof com.tasktop.c2c.server.tasks.domain.Component) {
            return ((com.tasktop.c2c.server.tasks.domain.Component) item).getName();
        } else if (item instanceof Milestone) {
            return ((Milestone) item).getValue();
        } else {
            assert item instanceof String : "Wrong value type : " + item.getClass().getName(); // NOI18N
        }
        return item.toString();
    }

    private static class ClientDataRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            return super.getListCellRendererComponent(list, convert(value), index, isSelected, cellHasFocus);
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
    
    private void cancelHighlight(JComponent label) {
        if (!reloading) {
            label.setOpaque(false);
            label.getParent().repaint();
        }
    }

    private void storeFieldValue (IssueField field, JComboBox combo) {
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
        issue.setFieldValues(field, values);
    }

    private void storeFieldValue(IssueField field, String value) {
        if (issue.getTaskData().isNew() || !value.equals(initialValues.get(field.getKey()))) {
//            if (field == IssueField.STATUS) {
//                if (value.equals("CLOSED")) { // NOI18N
//                    issue.close();
//                } else if (value.equals("VERIFIED")) { // NOI18N
//                    issue.verify();
//                } else if (value.equals("REOPENED")) { // NOI18N
//                    issue.reopen();
//                } else if (value.equals("RESOLVED")) { // NOI18N
//                    issue.resolve(resolutionCombo.getSelectedItem().toString());
//                } else if (value.equals("ASSIGNED")) { // NOI18N
//                    issue.accept();
//                }
//            } else if ((field == IssueField.ASSIGNED_TO) && !issue.isNew()) {
//                issue.reassign(value);
//            }
            issue.setFieldValue(field, value);
        }
    }
    
    private void storeFieldValues(IssueField field, List<String> values) {
        if (issue.getTaskData().isNew() || !values.toString().equals(initialValues.get(field.getKey()))) {
            issue.setFieldValues(field, values);
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
            Set<String> bugs1 = new HashSet<String>(bugs(parentField.getText()));
            Set<String> bugs2 = new HashSet<String>(bugs(subtaskField.getText()));
            boolean change = false;
            if (!issue.isNew()) {
                if (bugs1.contains(issue.getID()) || bugs2.contains(issue.getID())) {
                    if (!sameParent) {
                        sameParent = true;
                        change = true;
                    }
                } else {
                    if (sameParent) {
                        sameParent = false;
                        change = true;
                    }
                }
            }
            bugs1.retainAll(bugs2);
            if (bugs1.isEmpty()) {
                if (cyclicDependency) {
                    cyclicDependency = false;
                    change = true;
                }
            } else {
                if (!cyclicDependency) {
                    cyclicDependency = true;
                    change = true;
                }
            }
            if (change) {
                updateMessagePanel();
            }
        }
    }

    private static List<String> bugs (String values) {
        List<String> bugs = new LinkedList<String>();
        StringTokenizer st = new StringTokenizer(values, ", \t\n\r\f"); // NOI18N
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            try {
                bugs.add(Integer.decode(token).toString());
            } catch (NumberFormatException nfex) {}
        }
        return bugs;
    }
    
    private List<String> keywords (String values) {
        List<String> items = new LinkedList<String>();
        StringTokenizer st = new StringTokenizer(values, ", \t\n\r\f"); //NOI18N
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (keywords.contains(token)) {
                items.add(token);
            }
        }
        return items;
    }
    
    private List<String> list (String values) {
        List<String> items = new LinkedList<String>();
        StringTokenizer st = new StringTokenizer(values, ", \t\n\r\f"); //NOI18N
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            items.add(token);
        }
        return items;
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

    private class DateFieldListener implements DocumentListener {
        private final JTextComponent comp;
        private final JLabel fieldLabel;

        public DateFieldListener (JTextComponent comp, JLabel fieldLabel) {
            this.comp = comp;
            this.fieldLabel = fieldLabel;
        }
        
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
            invalidDateFields.remove(fieldName(fieldLabel));
            String dateText = comp.getText().trim();
            if (!dateText.isEmpty()) {
                Date date = ODCSUtil.parseTextDate(dateText, INPUT_DATE_FORMAT);
                if (date == null) {
                    invalidDateFields.add(fieldName(fieldLabel));
                }
            }
            updateMessagePanel();
        }
    }
    
    private void updateNoDuplicateId() {
        boolean newNoDuplicateId = "DUPLICATE".equals(getSelectedValue(resolutionCombo)) && "".equals(duplicateField.getText().trim());
        if(newNoDuplicateId != noDuplicateId) {
            noDuplicateId = newNoDuplicateId;
            updateMessagePanel();
        }
    }    
    
    private void updateMessagePanel() {
        messagePanel.removeAll();
        if (noComponent) {
            addMessage("IssuePanel.noComponent"); // NOI18N
        }
        if (noIteration) {
            addMessage("IssuePanel.noIteration"); // NOI18N
        }
        if (noTargetMilestione) {
            addMessage("IssuePanel.noTargetMilestone"); // NOI18N
        }
        if (noSummary) {
            JLabel noSummaryLabel = new JLabel();
            noSummaryLabel.setText(NbBundle.getMessage(IssuePanel.class, "IssuePanel.noSummary")); // NOI18N
            String icon = issue.isNew() ? ICON_PATH_INFO : ICON_PATH_ERROR;
            noSummaryLabel.setIcon(new ImageIcon(ImageUtilities.loadImage(icon)));
            messagePanel.add(noSummaryLabel);
        }
        if (sameParent) {
            JLabel sameParentLabel = new JLabel();
            sameParentLabel.setText(NbBundle.getMessage(IssuePanel.class, "IssuePanel.sameParent")); // NOI18N
            sameParentLabel.setIcon(new ImageIcon(ImageUtilities.loadImage(ICON_PATH_ERROR)));
            messagePanel.add(sameParentLabel);
        }
        if (!sameParent && cyclicDependency) {
            JLabel cyclicDependencyLabel = new JLabel();
            cyclicDependencyLabel.setText(NbBundle.getMessage(IssuePanel.class, "IssuePanel.cyclicDependency")); // NOI18N
            cyclicDependencyLabel.setIcon(new ImageIcon(ImageUtilities.loadImage(ICON_PATH_ERROR)));
            messagePanel.add(cyclicDependencyLabel);
        }
        if (invalidTag) {
            JLabel invalidKeywordLabel = new JLabel();
            invalidKeywordLabel.setText(NbBundle.getMessage(IssuePanel.class, "IssuePanel.invalidTag")); // NOI18N
            invalidKeywordLabel.setIcon(new ImageIcon(ImageUtilities.loadImage(ICON_PATH_ERROR)));
            messagePanel.add(invalidKeywordLabel);
        }
        if (noDuplicateId) {
            JLabel noDuplicateLabel = new JLabel();
            noDuplicateLabel.setText(NbBundle.getMessage(IssuePanel.class, "IssuePanel.noDuplicateId")); // NOI18N
            noDuplicateLabel.setIcon(new ImageIcon(ImageUtilities.loadImage(ICON_PATH_ERROR)));
            messagePanel.add(noDuplicateLabel);
        }
        if (!invalidDateFields.isEmpty()) {
            JLabel invalidDateLabel = new JLabel();
            invalidDateLabel.setText(NbBundle.getMessage(IssuePanel.class, "IssuePanel.invalidDateField", invalidDateFields.iterator().next(), INPUT_DATE_FORMAT.toPattern())); // NOI18N
            invalidDateLabel.setIcon(new ImageIcon(ImageUtilities.loadImage(ICON_PATH_ERROR)));
            messagePanel.add(invalidDateLabel);
        }
        if (noSummary || sameParent || cyclicDependency || invalidTag || noComponent || noIteration || noTargetMilestione || noDuplicateId || !invalidDateFields.isEmpty()) {
            submitButton.setEnabled(false);
        } else {
            submitButton.setEnabled(true);
        }
        for (String fieldError : fieldErrors) {
            JLabel errorLabel = new JLabel(fieldError);
            errorLabel.setIcon(ImageUtilities.loadImageIcon(ICON_PATH_ERROR, true));
            messagePanel.add(errorLabel);
        }
        for (String fieldWarning : fieldWarnings) {
            JLabel warningLabel = new JLabel(fieldWarning);
            warningLabel.setIcon(ImageUtilities.loadImageIcon(ICON_PATH_WARNING, true));
            messagePanel.add(warningLabel);
        }
        if (noSummary || sameParent || cyclicDependency || invalidTag || noComponent || noIteration || noTargetMilestione || noDuplicateId || (fieldErrors.size() + fieldWarnings.size() > 0)
                || !invalidDateFields.isEmpty()) {
            messagePanel.setVisible(true);
            messagePanel.revalidate();
        } else {
            messagePanel.setVisible(false);
        }
    }    
    
    void addMessage(String messageKey) {
        JLabel messageLabel = new JLabel();
        messageLabel.setText(NbBundle.getMessage(IssuePanel.class, messageKey));
        String icon = issue.isNew() ? ICON_PATH_INFO : ICON_PATH_ERROR;
        messageLabel.setIcon(new ImageIcon(ImageUtilities.loadImage(icon)));
        messagePanel.add(messageLabel);
    }    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        issueTypeLabel = new javax.swing.JLabel();
        statusLabel = new javax.swing.JLabel();
        statusWarning = new javax.swing.JLabel();
        resolutionLabel = new javax.swing.JLabel();
        resolutionCombo = new javax.swing.JComboBox();
        resolutionWarning = new javax.swing.JLabel();
        duplicateWarning = new javax.swing.JLabel();
        priorityLabel = new javax.swing.JLabel();
        priorityWarning = new javax.swing.JLabel();
        severityLabel = new javax.swing.JLabel();
        severityWarning = new javax.swing.JLabel();
        keywordsLabel = new javax.swing.JLabel();
        keywordsWarning = new javax.swing.JLabel();
        productLabel = new javax.swing.JLabel();
        productWarning = new javax.swing.JLabel();
        componentLabel = new javax.swing.JLabel();
        componentWarning = new javax.swing.JLabel();
        releaseLabel = new javax.swing.JLabel();
        releaseWarning = new javax.swing.JLabel();
        foundInLabel = new javax.swing.JLabel();
        foundInField = new javax.swing.JTextField();
        foundInWarning = new javax.swing.JLabel();
        iterationLabel = new javax.swing.JLabel();
        iterationWarning = new javax.swing.JLabel();
        estimateLabel = new javax.swing.JLabel();
        estimateField = new javax.swing.JTextField();
        estimateWarning = new javax.swing.JLabel();
        dueDateLabel = new javax.swing.JLabel();
        dueDateField = new javax.swing.JTextField();
        dueDateWarning = new javax.swing.JLabel();
        parentLabel = new javax.swing.JLabel();
        parentWarning = new javax.swing.JLabel();
        ownerLabel = new javax.swing.JLabel();
        ownerCombo = new javax.swing.JComboBox();
        ownerWarning = new javax.swing.JLabel();
        subtaskWarning = new javax.swing.JLabel();
        externalLabel = new javax.swing.JLabel();
        externalWarning = new javax.swing.JLabel();
        ccLabel = new javax.swing.JLabel();
        ccWarning = new javax.swing.JLabel();
        attachmentsLabel = new javax.swing.JLabel();
        dummyAttachmentsPanel = new javax.swing.JPanel();
        summaryLabel = new javax.swing.JLabel();
        summaryWarning = new javax.swing.JLabel();
        descriptionLabel = new javax.swing.JLabel();
        addCommentLabel = new javax.swing.JLabel();
        dummyCommentsPanel = new javax.swing.JPanel();
        separator = new javax.swing.JSeparator();
        messagePanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        separatorLabel3 = new javax.swing.JLabel();
        separatorLabel = new javax.swing.JLabel();
        parentHeaderPanel = new javax.swing.JPanel();
        headerLabel = new javax.swing.JLabel();
        headerField = new javax.swing.JTextField();
        dummyDescriptionPanel = new javax.swing.JPanel();
        dummyAddCommentPanel = new javax.swing.JPanel();
        subtaskLabel = new javax.swing.JLabel();
        dummySubtaskPanel = new javax.swing.JPanel();

        setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        org.openide.awt.Mnemonics.setLocalizedText(issueTypeLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.issueTypeLabel.text_1")); // NOI18N

        issueTypeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                issueTypeComboActionPerformed(evt);
            }
        });

        reportedLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(reportedLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.reportedLabel.text_1")); // NOI18N

        reportedField.setEditable(false);
        reportedField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        reportedField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reportedFieldActionPerformed(evt);
            }
        });

        modifiedLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(modifiedLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.modifiedLabel.text_1")); // NOI18N

        modifiedField.setEditable(false);
        modifiedField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        modifiedField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modifiedFieldActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(statusLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.statusLabel.text_1")); // NOI18N

        statusCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statusComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(resolutionLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.resolutionLabel.text")); // NOI18N

        resolutionCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resolutionComboActionPerformed(evt);
            }
        });

        duplicateField.setColumns(15);
        duplicateField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                duplicateFieldActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(duplicateButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.duplicateButton.text_1")); // NOI18N
        duplicateButton.setFocusPainted(false);
        duplicateButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        duplicateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                duplicateButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(priorityLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.priorityLabel.text_1")); // NOI18N

        priorityCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                priorityComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(severityLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.severityLabel.text")); // NOI18N

        severityCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                severityComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(keywordsLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.keywordsLabel.text")); // NOI18N

        keywordsField.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.keywordsField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(keywordsButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.keywordsButton.text")); // NOI18N
        keywordsButton.setFocusPainted(false);
        keywordsButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        keywordsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keywordsButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(productLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.productLabel.text_1")); // NOI18N

        productCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                productComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(componentLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.componentLabel.text_1")); // NOI18N

        componentCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                componentComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(releaseLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.releaseLabel.text_1")); // NOI18N

        releaseCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                releaseComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(foundInLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.foundInLabel.text")); // NOI18N

        foundInField.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.foundInField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(iterationLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.iterationLabel.text_1")); // NOI18N

        iterationCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                iterationComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(estimateLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.estimateLabel.text")); // NOI18N

        estimateField.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.estimateField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(dueDateLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.dueDateLabel.text")); // NOI18N

        dueDateField.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.dueDateField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(parentLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.parentLabel.text_1")); // NOI18N

        parentField.setColumns(15);

        org.openide.awt.Mnemonics.setLocalizedText(parentButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.parentButton.text_1")); // NOI18N
        parentButton.setFocusPainted(false);
        parentButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        parentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parentButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(ownerLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.ownerLabel.text_1")); // NOI18N

        ownerCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ownerComboActionPerformed(evt);
            }
        });

        subtaskField.setColumns(15);

        org.openide.awt.Mnemonics.setLocalizedText(subtaskButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.subtaskButton.text")); // NOI18N
        subtaskButton.setFocusPainted(false);
        subtaskButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        subtaskButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subtaskButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(externalLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.externalLabel.text")); // NOI18N

        externalField.setColumns(15);
        externalField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                externalFieldActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(ccLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.ccLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(ccButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.ccButton.text")); // NOI18N
        ccButton.setFocusPainted(false);
        ccButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ccButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ccButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(attachmentsLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.attachmentsLabel.text_1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(summaryLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.summaryLabel.text_1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(descriptionLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.descriptionLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addCommentLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.addCommentLabel.text_1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(submitButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.submitButton.text_1")); // NOI18N
        submitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.cancelButton.text_1")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        messagePanel.setLayout(new javax.swing.BoxLayout(messagePanel, javax.swing.BoxLayout.PAGE_AXIS));

        jPanel1.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        org.openide.awt.Mnemonics.setLocalizedText(reloadButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.reloadButton.text_1")); // NOI18N
        reloadButton.setToolTipText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.reloadButton.toolTipText_1")); // NOI18N
        reloadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reloadButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(showInBrowserButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.showInBrowserButton.text_1")); // NOI18N
        showInBrowserButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showInBrowserButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(refreshButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.refreshButton.text_1")); // NOI18N
        refreshButton.setToolTipText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.refreshButton.toolTipText_1")); // NOI18N
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        separatorLabel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        separatorLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        parentHeaderPanel.setOpaque(false);

        javax.swing.GroupLayout parentHeaderPanelLayout = new javax.swing.GroupLayout(parentHeaderPanel);
        parentHeaderPanel.setLayout(parentHeaderPanelLayout);
        parentHeaderPanelLayout.setHorizontalGroup(
            parentHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        parentHeaderPanelLayout.setVerticalGroup(
            parentHeaderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 19, Short.MAX_VALUE)
        );

        org.openide.awt.Mnemonics.setLocalizedText(headerLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.headerLabel.text")); // NOI18N

        headerField.setEditable(false);
        headerField.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.headerField.text")); // NOI18N
        headerField.setBorder(null);
        headerField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                headerFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(parentHeaderPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(headerLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(headerField)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separatorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(reloadButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separatorLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(showInBrowserButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(refreshButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(separatorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(reloadButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(separatorLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(showInBrowserButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(24, 24, 24))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(parentHeaderPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(headerLabel)
                            .addComponent(headerField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        javax.swing.GroupLayout dummyDescriptionPanelLayout = new javax.swing.GroupLayout(dummyDescriptionPanel);
        dummyDescriptionPanel.setLayout(dummyDescriptionPanelLayout);
        dummyDescriptionPanelLayout.setHorizontalGroup(
            dummyDescriptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        dummyDescriptionPanelLayout.setVerticalGroup(
            dummyDescriptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 15, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout dummyAddCommentPanelLayout = new javax.swing.GroupLayout(dummyAddCommentPanel);
        dummyAddCommentPanel.setLayout(dummyAddCommentPanelLayout);
        dummyAddCommentPanelLayout.setHorizontalGroup(
            dummyAddCommentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        dummyAddCommentPanelLayout.setVerticalGroup(
            dummyAddCommentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 26, Short.MAX_VALUE)
        );

        org.openide.awt.Mnemonics.setLocalizedText(subtaskLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.subtaskLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(separator)
                    .addComponent(dummyCommentsPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(ownerLabel)
                    .addComponent(externalLabel)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(subtaskLabel)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(19, 19, 19)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(descriptionLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(summaryLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(foundInLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(productLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(keywordsLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(priorityLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(statusLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(issueTypeLabel, javax.swing.GroupLayout.Alignment.TRAILING)))
                                    .addComponent(addCommentLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(attachmentsLabel)))
                            .addComponent(parentLabel))
                        .addComponent(dueDateLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                    .addComponent(ccLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(messagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(20, 20, 20))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(dummyAddCommentPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(dummyDescriptionPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(dummyAttachmentsPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(summaryField, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(dummySubtaskPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(6, 6, 6)
                                .addComponent(summaryWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(submitButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cancelButton))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(issueTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(issueTypeWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(reportedLabel)
                                        .addGap(18, 18, 18)
                                        .addComponent(reportedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(modifiedLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(modifiedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(statusCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(statusWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(priorityCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(priorityWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addGap(24, 24, 24)
                                                .addComponent(severityLabel))
                                            .addGroup(layout.createSequentialGroup()
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(resolutionLabel)))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(severityCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(severityWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(resolutionCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(resolutionWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(duplicateField, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(duplicateButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(duplicateWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(keywordsField, javax.swing.GroupLayout.PREFERRED_SIZE, 582, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(keywordsButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(keywordsWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(externalField, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(externalWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(ownerCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(ownerWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(parentField, javax.swing.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                                            .addComponent(subtaskField, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE))
                                        .addGap(6, 6, 6)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(parentButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(parentWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(subtaskButton)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(subtaskWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(foundInField, javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(productCombo, javax.swing.GroupLayout.Alignment.LEADING, 0, 167, Short.MAX_VALUE)
                                            .addComponent(dueDateField, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(dueDateWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(estimateLabel))
                                            .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                    .addGroup(layout.createSequentialGroup()
                                                        .addComponent(foundInWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(iterationLabel))
                                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                        .addComponent(productWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(componentLabel)))
                                                .addGap(0, 3, Short.MAX_VALUE)))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(iterationCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(iterationWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(estimateField, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(estimateWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(componentCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(componentWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(releaseLabel)))))
                                .addComponent(releaseCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(releaseWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(ccField, javax.swing.GroupLayout.PREFERRED_SIZE, 586, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ccButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ccWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {issueTypeCombo, priorityCombo, releaseCombo, resolutionCombo, severityCombo, statusCombo});

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {externalField, parentField, subtaskField});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(issueTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(issueTypeLabel)
                            .addComponent(reportedLabel)
                            .addComponent(reportedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(modifiedLabel)
                            .addComponent(modifiedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(statusCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(statusLabel))
                            .addComponent(statusWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(duplicateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(duplicateButton)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                .addComponent(resolutionLabel)
                                .addComponent(resolutionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(resolutionWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(duplicateWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(issueTypeWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(priorityCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(priorityLabel))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(severityCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(severityLabel)))
                            .addComponent(severityWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(keywordsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(keywordsLabel)
                                    .addComponent(keywordsButton))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                        .addComponent(productLabel)
                                        .addComponent(productCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(componentLabel)
                                        .addComponent(componentCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(releaseLabel)
                                        .addComponent(releaseCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(componentWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(releaseWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(productWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(keywordsWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(iterationWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(foundInWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(foundInField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(foundInLabel))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(iterationCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(iterationLabel))))
                    .addComponent(priorityWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(estimateWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(estimateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(estimateLabel))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(dueDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(dueDateLabel))
                    .addComponent(dueDateWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(ownerLabel)
                        .addComponent(ownerCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(ownerWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ccWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(ccField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(ccLabel)
                        .addComponent(ccButton)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(parentLabel)
                        .addComponent(parentField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(parentButton))
                    .addComponent(parentWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(subtaskField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(subtaskButton)
                        .addComponent(subtaskLabel))
                    .addComponent(subtaskWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dummySubtaskPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(externalLabel)
                        .addComponent(externalField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(externalWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dummyAttachmentsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(attachmentsLabel))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(summaryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(summaryLabel))
                    .addComponent(summaryWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(descriptionLabel)
                    .addComponent(dummyDescriptionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addCommentLabel)
                    .addComponent(dummyAddCommentPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(17, 17, 17)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(submitButton)
                    .addComponent(cancelButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(messagePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dummyCommentsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 12, Short.MAX_VALUE)
                .addGap(12, 12, 12))
        );
    }// </editor-fold>//GEN-END:initComponents

    
    
    private void showInBrowserButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showInBrowserButtonActionPerformed
        TeamProject kp = issue.getRepository().getLookup().lookup(TeamProject.class);
        assert kp != null; // all odcs repositories hould come from team support
        if (kp == null) {
            return;
        }
        try {
            URL url = new URL(kp.getWebLocation() + ODCSUtil.URL_FRAGMENT_TASK + issue.getID());
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        } catch (MalformedURLException muex) {
            ODCS.LOG.log(Level.INFO, "Unable to show the issue in the browser.", muex); // NOI18N
        }
    }//GEN-LAST:event_showInBrowserButtonActionPerformed

    @NbBundle.Messages("IssuePanel.reloadMessage=Reloading server attributes")
    private void reloadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reloadButtonActionPerformed
        final ProgressHandle handle = ProgressHandleFactory.createHandle(Bundle.IssuePanel_reloadMessage());
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
                            Object iteration = iterationCombo.getSelectedItem();
                            Object milestone = releaseCombo.getSelectedItem();
                            Object owner = ownerCombo.getSelectedItem();
                            Object priority = priorityCombo.getSelectedItem();
                            Object severity = severityCombo.getSelectedItem();
                            Object resolution = resolutionCombo.getSelectedItem();
                            Object issueType = issueTypeCombo.getSelectedItem();
                            initCombos();
//                            initCustomFields();
                            selectInCombo(productCombo, product, false);
                            selectInCombo(iterationCombo, iteration, false);
                            selectInCombo(releaseCombo, milestone, false);
                            selectInCombo(ownerCombo, owner, false);
                            selectInCombo(priorityCombo, priority, false);
                            selectInCombo(severityCombo, severity, false);
                            initStatusCombo(statusCombo.getSelectedItem().toString(), issue.getFieldValue(IssueField.STATUS));
                            selectInCombo(resolutionCombo, resolution, false);
                            selectInCombo(issueTypeCombo, issueType, false);
//                            reloadCustomFields(true);
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

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        refreshIssue(true);
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void issueTypeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_issueTypeComboActionPerformed
        cancelHighlight(issueTypeLabel);
    }//GEN-LAST:event_issueTypeComboActionPerformed

    private void statusComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statusComboActionPerformed
        cancelHighlight(statusLabel);
        resolutionCombo.setVisible(false);
        resolutionLabel.setVisible(false);
        // Hide/show resolution combo
        RepositoryConfiguration rc = issue.getRepository().getRepositoryConfiguration(false);
        String initialStatus = initialValues.get(IssueField.STATUS.getKey());
        boolean resolvedInitial = RESOLUTION_RESOLVED.equals(initialStatus); // NOI18N
        if (!resolvedInitial) {
            Object item = statusCombo.getSelectedItem();
            if(!(item instanceof TaskStatus)) {
                return;
            }
            TaskStatus status = (TaskStatus) statusCombo.getSelectedItem();
            TaskStatus resolvedStatus = ODCSUtil.getStatusByValue(rc, RESOLUTION_RESOLVED);
            if (resolvedStatus.equals(status)) { // NOI18N
                TaskResolution fixedResolution = ODCSUtil.getResolutionByValue(rc, STATUS_FIXED);
                resolutionCombo.setSelectedItem(fixedResolution); 
                resolutionCombo.setVisible(true);
                resolutionLabel.setVisible(true);
            } else {
                resolutionCombo.setVisible(false);
                resolutionLabel.setVisible(false);
                duplicateField.setVisible(false);
                duplicateButton.setVisible(false);
            }
        }
        if (statusCombo.getSelectedIndex() >= resolvedIndex) {
            resolutionCombo.setVisible(true);
            resolutionLabel.setVisible(true);
        } else {
            resolutionCombo.setVisible(false);
            resolutionLabel.setVisible(false);
        }
        duplicateField.setVisible(false);
        duplicateButton.setVisible(false);
    }//GEN-LAST:event_statusComboActionPerformed

    private void duplicateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_duplicateButtonActionPerformed
        findIssue(duplicateField, "IssuePanel.duplicateButton.message", "org.netbeans.modules.odcs.duplicateChooser", false);
    }//GEN-LAST:event_duplicateButtonActionPerformed

    private void priorityComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_priorityComboActionPerformed
        cancelHighlight(priorityLabel);
    }//GEN-LAST:event_priorityComboActionPerformed

    private void severityComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_severityComboActionPerformed
        cancelHighlight(priorityLabel);
    }//GEN-LAST:event_severityComboActionPerformed

    private void productComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_productComboActionPerformed
        cancelHighlight(productLabel);
        
        Object o = productCombo.getSelectedItem();
        if(o == null || !(o instanceof Product)) {
            return;
        }
        Product product = (Product) o;
        
        // Reload componentCombo, versionCombo and targetMilestoneCombo
        
        Object component = componentCombo.getSelectedItem();
        Object version = releaseCombo.getSelectedItem();
        componentCombo.setModel(toComboModel(product.getComponents()));
        releaseCombo.setModel(toComboModel(product.getMilestones()));
        
        // Attempt to keep selection
        boolean isNew = issue.isNew();
        if (!isNew && !selectInCombo(componentCombo, component, false) && (componentCombo.getModel().getSize()>1)) {
            componentCombo.setSelectedItem(product.getDefaultComponent());
        }
        if (!isNew && !selectInCombo(releaseCombo, version, false) && (releaseCombo.getModel().getSize() > 1)) {
            releaseCombo.setSelectedItem(product.getDefaultMilestone());
        }
        if (issue.isNew()) {
            releaseCombo.setSelectedItem(product.getDefaultMilestone());
            componentCombo.setSelectedItem(product.getDefaultComponent());
            issue.setFieldValue(IssueField.PRODUCT, product.getName());
            AbstractRepositoryConnector connector = ODCS.getInstance().getRepositoryConnector();
            
            TaskData data = issue.getTaskData();
//            try {
                // throws NPE
//                connector.getTaskDataHandler().initializeTaskData(issue.getRepository().getTaskRepository(), data, connector.getTaskMapping(data), new NullProgressMonitor());
                initialValues.remove(IssueField.COMPONENT.getKey());
                initialValues.remove(IssueField.MILESTONE.getKey());
                reloadForm(false);
//            } catch (CoreException cex) {
//                ODCS.LOG.log(Level.INFO, cex.getMessage(), cex);
//            }
        }
    }//GEN-LAST:event_productComboActionPerformed

    private void componentComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_componentComboActionPerformed
        cancelHighlight(componentLabel);
        
        // Reload component owner
        
        Object component = componentCombo.getSelectedItem();
        Object owner = ownerCombo.getSelectedItem();
        
        // Attempt to keep selection
        boolean isNew = issue.isNew();
        if (isNew || !isNew && (component instanceof com.tasktop.c2c.server.tasks.domain.Component) 
                && !selectInCombo(ownerCombo, owner, false) && (ownerCombo.getModel().getSize() > 1)) {
            TaskUserProfile userProfile = ((com.tasktop.c2c.server.tasks.domain.Component) component).getInitialOwner();
            if (userProfile != null) {
                ownerCombo.setSelectedItem(userProfile);
            }
        }
        
        updateNoComponent();
    }//GEN-LAST:event_componentComboActionPerformed

    private void releaseComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_releaseComboActionPerformed
        cancelHighlight(releaseLabel);
        updateNoMilestone();
    }//GEN-LAST:event_releaseComboActionPerformed

    private void iterationComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_iterationComboActionPerformed
        cancelHighlight(iterationLabel);
        updateNoIteration();
    }//GEN-LAST:event_iterationComboActionPerformed

    private void parentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_parentButtonActionPerformed
        findIssue(parentField, "IssuePanel.parentButton.message", "org.netbeans.modules.odcs.parentChooser", true); // NOI18N
    }//GEN-LAST:event_parentButtonActionPerformed

    private void subtaskButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subtaskButtonActionPerformed
        findIssue(subtaskField, "IssuePanel.subtaskButton.message", "org.netbeans.modules.odcs.subtaskChooser", true); // NOI18N
    }//GEN-LAST:event_subtaskButtonActionPerformed

    private void externalFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_externalFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_externalFieldActionPerformed

    @NbBundle.Messages({"# {0} - the file to be attached", "LBL_AttachedPrefix=Attached file {0}"})
    private void submitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitButtonActionPerformed
        assert !issue.getTaskData().isPartial();
        final boolean isNew = issue.getTaskData().isNew();
        storeFieldValue(IssueField.DESCRIPTION, isNew
                ? addCommentPanel.getCodePane()
                : descriptionPanel.getCodePane());
        storeFieldValue(IssueField.SUMMARY, summaryField);
        storeFieldValue(IssueField.PRODUCT, productCombo);
        storeFieldValue(IssueField.COMPONENT, componentCombo);
        storeFieldValue(IssueField.MILESTONE, releaseCombo);
        storeFieldValue(IssueField.ITERATION, iterationCombo);
        storeFieldValue(IssueField.PRIORITY, priorityCombo);
        storeFieldValue(IssueField.SEVERITY, severityCombo);
        storeFieldValue(IssueField.STATUS, statusCombo);
        storeFieldValues(IssueField.CC, list(ccField.getText()));
        Date dueDate = ODCSUtil.parseTextDate(dueDateField.getText().trim(), INPUT_DATE_FORMAT);
        storeFieldValue(IssueField.DUEDATE, dueDate == null ? "" : Long.toString(dueDate.getTime())); //NOI18N
        storeFieldValue(IssueField.OWNER, ((TaskUserProfile) ownerCombo.getSelectedItem()).getLoginName());
        storeFieldValues(IssueField.PARENT, bugs(parentField.getText()));
        storeFieldValues(IssueField.SUBTASK, bugs(subtaskField.getText()));
        storeFieldValues(IssueField.KEYWORDS, keywords(keywordsField.getText()));
        storeFieldValue(IssueField.TASK_TYPE, issueTypeCombo);
        storeFieldValue(IssueField.ESTIMATE, estimateField);
        storeFieldValue(IssueField.FOUNDIN, foundInField);
        if (resolutionCombo.isVisible()) {
            storeFieldValue(IssueField.RESOLUTION, resolutionCombo);
        } else {
            storeFieldValue(IssueField.RESOLUTION, ""); //NOI18N
        }
        if (duplicateField.isVisible() && duplicateField.isEditable()) {
            storeFieldValue(IssueField.DUPLICATE, duplicateField); //NOI18N
        }
        if (!isNew && !"".equals(addCommentPanel.getCodePane().getText().trim())) { // NOI18N
            issue.addComment(addCommentPanel.getCodePane().getText().trim());
        }
//        // Store custom fields
//        for (CustomFieldInfo field : customFields) {
//            if (field.comp instanceof JTextComponent) {
//                storeFieldValue(field.field, (JTextComponent)field.comp);
//            } else if (field.comp instanceof JComboBox) {
//                storeFieldValue(field.field, (JComboBox)field.comp);
//            } else if (field.comp instanceof JList) {
//                storeFieldValue(field.field, (JList)field.comp);
//            } else {
//                Bugzilla.LOG.log(Level.INFO, "Custom field component {0} is not supported!", field.comp); // NOI18N
//            }
//        }
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
                boolean ret = false;
                try {
                    ret = issue.submitAndRefresh();
                    for (AttachmentsPanel.AttachmentInfo attachment : attachmentsPanel.getNewAttachments()) {
                        if (attachment.getFile().exists() && attachment.getFile().isFile()) {
                            issue.addAttachment(attachment.getFile(), Bundle.LBL_AttachedPrefix(attachment.getFile().getName()), attachment.getDescription(),
                                    attachment.getContentType(), attachment.isPatch());
                        } else {
                            // PENDING notify user
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
                    if(ret) {
//                        if (isNew) {
//                            // Show all custom fields, not only the ones shown on bug creation
//                            EventQueue.invokeLater(new Runnable() {
//                                @Override
//                                public void run() {
//                                    initCustomFields();
//                                }
//                            });
//                        }
                        reloadFormInAWT(true);
                    }
                }
            }
        });
        if (isNew) {
            ODCSRepository repository = issue.getRepository();
            if (repository != null) {
                OwnerUtils.setLooseAssociation(ODCSUtil.getRepository(repository), false);
            }
        }
    }//GEN-LAST:event_submitButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        reloadForm(true);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void keywordsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keywordsButtonActionPerformed
        String message = NbBundle.getMessage(IssuePanel.class, "IssuePanel.keywordsButton.message"); // NOI18N
        String keywords = ODCSUtil.getKeywords(message, keywordsField.getText(), issue.getRepository());
        keywordsField.setText(keywords);
    }//GEN-LAST:event_keywordsButtonActionPerformed

    private void reportedFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reportedFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_reportedFieldActionPerformed

    private void modifiedFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modifiedFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_modifiedFieldActionPerformed

    private void duplicateFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_duplicateFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_duplicateFieldActionPerformed

    private void ownerComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ownerComboActionPerformed
        cancelHighlight(ownerLabel);
    }//GEN-LAST:event_ownerComboActionPerformed

    private void resolutionComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resolutionComboActionPerformed
        cancelHighlight(resolutionCombo);
        if (resolutionCombo.getParent() == null) {
            return;
        }
        TaskResolution duplicate = ODCSUtil.getResolutionByValue(issue.getRepository().getRepositoryConfiguration(false), RESOLUTION_DUPLICATE);
        boolean shown = duplicate.equals(resolutionCombo.getSelectedItem()); // NOI18N
        duplicateField.setVisible(shown);
        duplicateButton.setVisible(shown && duplicateField.isEditable());
        updateNoDuplicateId();
    }//GEN-LAST:event_resolutionComboActionPerformed

    private void ccButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ccButtonActionPerformed
        String message = NbBundle.getMessage(IssuePanel.class, "IssuePanel.ccButton.message"); // NOI18N
        String users = ODCSUtil.getUsers(message, ccField.getText(), issue.getRepository());
        ccField.setText(users);
    }//GEN-LAST:event_ccButtonActionPerformed

    private void headerFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_headerFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_headerFieldActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel addCommentLabel;
    private javax.swing.JLabel attachmentsLabel;
    final javax.swing.JButton cancelButton = new javax.swing.JButton();
    final javax.swing.JButton ccButton = new javax.swing.JButton();
    final javax.swing.JTextField ccField = new javax.swing.JTextField();
    private javax.swing.JLabel ccLabel;
    private javax.swing.JLabel ccWarning;
    final javax.swing.JComboBox componentCombo = new javax.swing.JComboBox();
    private javax.swing.JLabel componentLabel;
    private javax.swing.JLabel componentWarning;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JTextField dueDateField;
    private javax.swing.JLabel dueDateLabel;
    private javax.swing.JLabel dueDateWarning;
    private javax.swing.JPanel dummyAddCommentPanel;
    private javax.swing.JPanel dummyAttachmentsPanel;
    private javax.swing.JPanel dummyCommentsPanel;
    private javax.swing.JPanel dummyDescriptionPanel;
    private javax.swing.JPanel dummySubtaskPanel;
    final javax.swing.JButton duplicateButton = new javax.swing.JButton();
    final javax.swing.JTextField duplicateField = new javax.swing.JTextField();
    private javax.swing.JLabel duplicateWarning;
    private javax.swing.JTextField estimateField;
    private javax.swing.JLabel estimateLabel;
    private javax.swing.JLabel estimateWarning;
    final javax.swing.JTextField externalField = new javax.swing.JTextField();
    private javax.swing.JLabel externalLabel;
    private javax.swing.JLabel externalWarning;
    private javax.swing.JTextField foundInField;
    private javax.swing.JLabel foundInLabel;
    private javax.swing.JLabel foundInWarning;
    private javax.swing.JTextField headerField;
    private javax.swing.JLabel headerLabel;
    final javax.swing.JComboBox issueTypeCombo = new javax.swing.JComboBox();
    private javax.swing.JLabel issueTypeLabel;
    final javax.swing.JLabel issueTypeWarning = new javax.swing.JLabel();
    final javax.swing.JComboBox iterationCombo = new javax.swing.JComboBox();
    private javax.swing.JLabel iterationLabel;
    private javax.swing.JLabel iterationWarning;
    private javax.swing.JPanel jPanel1;
    final javax.swing.JButton keywordsButton = new javax.swing.JButton();
    final javax.swing.JTextField keywordsField = new javax.swing.JTextField();
    private javax.swing.JLabel keywordsLabel;
    private javax.swing.JLabel keywordsWarning;
    private javax.swing.JPanel messagePanel;
    final javax.swing.JTextField modifiedField = new javax.swing.JTextField();
    final javax.swing.JLabel modifiedLabel = new javax.swing.JLabel();
    private javax.swing.JComboBox ownerCombo;
    private javax.swing.JLabel ownerLabel;
    private javax.swing.JLabel ownerWarning;
    final javax.swing.JButton parentButton = new javax.swing.JButton();
    final javax.swing.JTextField parentField = new javax.swing.JTextField();
    private javax.swing.JPanel parentHeaderPanel;
    private javax.swing.JLabel parentLabel;
    private javax.swing.JLabel parentWarning;
    final javax.swing.JComboBox priorityCombo = new javax.swing.JComboBox();
    private javax.swing.JLabel priorityLabel;
    private javax.swing.JLabel priorityWarning;
    final javax.swing.JComboBox productCombo = new javax.swing.JComboBox();
    private javax.swing.JLabel productLabel;
    private javax.swing.JLabel productWarning;
    final org.netbeans.modules.bugtracking.util.LinkButton refreshButton = new org.netbeans.modules.bugtracking.util.LinkButton();
    final javax.swing.JComboBox releaseCombo = new javax.swing.JComboBox();
    private javax.swing.JLabel releaseLabel;
    private javax.swing.JLabel releaseWarning;
    final org.netbeans.modules.bugtracking.util.LinkButton reloadButton = new org.netbeans.modules.bugtracking.util.LinkButton();
    final javax.swing.JTextField reportedField = new javax.swing.JTextField();
    final javax.swing.JLabel reportedLabel = new javax.swing.JLabel();
    private javax.swing.JComboBox resolutionCombo;
    private javax.swing.JLabel resolutionLabel;
    private javax.swing.JLabel resolutionWarning;
    private javax.swing.JSeparator separator;
    private javax.swing.JLabel separatorLabel;
    private javax.swing.JLabel separatorLabel3;
    final javax.swing.JComboBox severityCombo = new javax.swing.JComboBox();
    private javax.swing.JLabel severityLabel;
    private javax.swing.JLabel severityWarning;
    final org.netbeans.modules.bugtracking.util.LinkButton showInBrowserButton = new org.netbeans.modules.bugtracking.util.LinkButton();
    final javax.swing.JComboBox statusCombo = new javax.swing.JComboBox();
    private javax.swing.JLabel statusLabel;
    private javax.swing.JLabel statusWarning;
    final javax.swing.JButton submitButton = new javax.swing.JButton();
    final javax.swing.JButton subtaskButton = new javax.swing.JButton();
    final javax.swing.JTextField subtaskField = new javax.swing.JTextField();
    private javax.swing.JLabel subtaskLabel;
    private javax.swing.JLabel subtaskWarning;
    final javax.swing.JTextField summaryField = new javax.swing.JTextField();
    private javax.swing.JLabel summaryLabel;
    private javax.swing.JLabel summaryWarning;
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

}
