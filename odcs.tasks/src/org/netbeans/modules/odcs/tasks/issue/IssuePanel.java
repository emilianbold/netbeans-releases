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
import org.netbeans.modules.bugtracking.commons.AttachmentsPanel;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.table.TableModel;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import javax.swing.text.NumberFormatter;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.api.IssueQuickSearch;
import org.netbeans.modules.bugtracking.issuetable.TableSorter;
import org.netbeans.modules.team.spi.TeamProject;
import org.netbeans.modules.bugtracking.spi.IssueStatusProvider;
import org.netbeans.modules.bugtracking.commons.LinkButton;
import org.netbeans.modules.bugtracking.commons.UIUtils;
import org.netbeans.modules.bugtracking.spi.SchedulePicker;
import org.netbeans.modules.mylyn.util.NbDateRange;
import org.netbeans.modules.mylyn.util.WikiPanel;
import org.netbeans.modules.mylyn.util.WikiUtils;
import org.netbeans.modules.odcs.tasks.ODCS;
import org.netbeans.modules.odcs.tasks.ODCSConfig;
import org.netbeans.modules.odcs.tasks.issue.ODCSIssue.Attachment;
import org.netbeans.modules.odcs.tasks.repository.ODCSRepository;
import org.netbeans.modules.odcs.tasks.util.NoUserProfile;
import org.netbeans.modules.odcs.tasks.util.ODCSUtil;
import org.netbeans.modules.spellchecker.api.Spellchecker;
import org.netbeans.modules.team.ide.spi.IDEServices;
import org.openide.awt.HtmlBrowser;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Pair;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author tomas
 */
@NbBundle.Messages({
    "LBL_Duplicate.fieldName=Duplicate of"
})
public class IssuePanel extends javax.swing.JPanel {
    final static SimpleDateFormat INPUT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd"); // NOI18N
    final static DateFormat READABLE_DATE_FORMAT = DateFormat.getDateInstance(DateFormat.MEDIUM);
    
    private static Color highlightColor = null;
    static {
        highlightColor = UIManager.getColor( "nb.bugtracking.label.highlight" ); //NOI18N
        if( null == highlightColor ) {
            highlightColor = new Color(217, 255, 217);
        }
    }
    
    private static final String RESOLUTION_RESOLVED = "RESOLVED";               // NOI18N    
    private static final String STATUS_FIXED = "FIXED";                         // NOI18N
    private static final String STATUS_UNCONFIRMED = "UNCONFIRMED";             // NOI18N
    private static final String RESOLUTION_DUPLICATE = "DUPLICATE";             // NOI18N
    private static final String DEFAULT_PRIORITY = "Normal";                    // NOI18N
    private static final String DEFAULT_SEVERITY = "normal";                    // NOI18N
    private static final String ICON_PATH_ERROR = "org/netbeans/modules/odcs/tasks/resources/error.gif"; //NOI18N
    private static final String ICON_PATH_INFO = "org/netbeans/modules/odcs/tasks/resources/info.png"; //NOI18N
    private static final URL ICON_REMOTE_PATH = IssuePanel.class.getClassLoader().getResource("org/netbeans/modules/odcs/tasks/resources/remote.png"); //NOI18N
    private static final ImageIcon ICON_REMOTE = ImageUtilities.loadImageIcon("org/netbeans/modules/odcs/tasks/resources/remote.png", true); //NOI18N
    private static final URL ICON_CONFLICT_PATH = IssuePanel.class.getClassLoader().getResource("org/netbeans/modules/odcs/tasks/resources/conflict.png"); //NOI18N
    private static final ImageIcon ICON_CONFLICT = ImageUtilities.loadImageIcon("org/netbeans/modules/odcs/tasks/resources/conflict.png", true); //NOI18N
    private static final URL ICON_UNSUBMITTED_PATH = IssuePanel.class.getClassLoader().getResource("org/netbeans/modules/odcs/tasks/resources/unsubmitted.png"); //NOI18N
    private static final ImageIcon ICON_UNSUBMITTED = ImageUtilities.loadImageIcon("org/netbeans/modules/odcs/tasks/resources/unsubmitted.png", true); //NOI18N
    private static final String SECTION_ATTRIBUTES = ".attributes"; //NOI18N
    private static final String SECTION_ATTACHMENTS = ".attachments"; //NOI18N
    private static final String SECTION_SUBTASKS = ".subtasks"; //NOI18N
    private static final String SECTION_COMMENTS = ".comments"; //NOI18N
    private static final String SECTION_PRIVATE = ".private"; //NOI18N
    private static final String ATTRIBUTE_PRIVATE_NOTES = "nb.private.notes"; //NOI18N
    private static final String ATTRIBUTE_ESTIMATE = "nb.estimate"; //NOI18N
    private static final String ATTRIBUTE_DUE_DATE = "nb.due.date"; //NOI18N
    private static final String ATTRIBUTE_SCHEDULE_DATE = "nb.schedule.date"; //NOI18N
    private Action[] attributesSectionActions;
    private Action[] attachmentsSectionActions;
    private Action[] commentsSectionActions;
    private Action[] privateSectionActions;

    private ODCSIssue issue;
    
    private boolean reloading;
    private boolean skipReload;
    
    private CommentsPanel commentsPanel;
    private AttachmentsPanel attachmentsPanel;
    
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
    private final Set<String> unsavedFields = new UnsavedFieldSet();
    private final Map<IssueField, String> fieldsConflict = new LinkedHashMap<IssueField, String>();
    private final Map<IssueField, String> fieldsIncoming = new LinkedHashMap<IssueField, String>();
    private final Map<IssueField, String> fieldsLocal = new LinkedHashMap<IssueField, String>();
    private final TooltipsMap tooltipsConflict = new TooltipsMap();
    private final TooltipsMap tooltipsIncoming = new TooltipsMap();
    private final TooltipsMap tooltipsLocal = new TooltipsMap();
    private int resolvedIndex;
    private WikiPanel descriptionPanel;
    private WikiPanel addCommentPanel;
    private static String wikiLanguage = "";
    private static final RequestProcessor RP = new RequestProcessor("ODCS Task Panel", 5, false); //NOI18N
    
    private JTable subTaskTable;
    private JScrollPane subTaskScrollPane;
    private final IDEServices.DatePickerComponent privateDueDatePicker;
    private final IDEServices.DatePickerComponent dueDatePicker;
    private final SchedulePicker scheduleDatePicker;
    private static final NumberFormatter estimateFormatter = new NumberFormatter(new java.text.DecimalFormat("#0")) { //NOI18N

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
    private final static NoUserProfile NO_USER_PROFILE = new NoUserProfile();
    
    /**
     * Creates new form IssuePanel
     */
    public IssuePanel() {
        initComponents();
        
        separatorLabel.setVisible(false);
        
        updateReadOnlyField(reportedField);
        updateReadOnlyField(modifiedField);
        updateReadOnlyField(headerField);
        
        messagePanel.setBackground(getBackground());
        
//        customFieldsPanelLeft.setBackground(getBackground());
//        customFieldsPanelRight.setBackground(getBackground());
        Font font = reportedLabel.getFont();
        headerField.setFont(font.deriveFont((float)(font.getSize()*1.7)));
        duplicateLabel.setVisible(false);
        duplicateField.setVisible(false);
        duplicateButton.setVisible(false);
        duplicateWarning.setVisible(false);
        attachDocumentListeners();

        // Comments panel
        commentsPanel = new CommentsPanel();
        commentsPanel.setNewCommentHandler(new CommentsPanel.NewCommentHandler() {
            @Override
            public void append(String text) {
                getAddCommentsPanel().appendCodeText(text);
                scrollRectToVisible(getAddCommentsPanel().getBounds());
            }
        });
        attachmentsPanel = new AttachmentsPanel(this);
        ((GroupLayout) commentsSectionPanel.getLayout()).replace(dummyCommentsPanel, commentsPanel);
        ((GroupLayout) attachmentsSectionPanel.getLayout()).replace(dummyAttachmentsPanel, attachmentsPanel);
        dueDatePicker = UIUtils.createDatePickerComponent();
        ((GroupLayout) attributesSectionPanel.getLayout()).replace(dummyDueDateField, dueDatePicker.getComponent());
        dueDateLabel.setLabelFor(dueDatePicker.getComponent());
        GroupLayout layout = (GroupLayout) privatePanel.getLayout();
        privateDueDatePicker = UIUtils.createDatePickerComponent();
        scheduleDatePicker = new SchedulePicker();
        layout.replace(dummyPrivateDueDateField, privateDueDatePicker.getComponent());
        privateDueDateLabel.setLabelFor(privateDueDatePicker.getComponent());
        layout.replace(dummyScheduleDateField, scheduleDatePicker.getComponent());
        scheduleDateLabel.setLabelFor(scheduleDatePicker.getComponent());
        initSpellChecker();
        initDefaultButton();
        
        font = UIManager.getFont("Label.font"); // NOI18N
        if (font != null) {
            mainScrollPane.getVerticalScrollBar().setUnitIncrement((int) (font.getSize() * 1.5));
            mainScrollPane.getHorizontalScrollBar().setUnitIncrement((int) (font.getSize() * 1.5));
        }
    }

    void opened() {
        restoreSections();
        enableComponents(false);
        issue.opened();
    }
    
    void closed() {
        if(issue != null) {
            commentsPanel.storeSettings();
            persistSections();
            issue.closed();
        }
    }
    
    private void persistSections () {
        if (!issue.isNew()) {
            ODCSConfig config = ODCSConfig.getInstance();
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
        ODCSConfig config = ODCSConfig.getInstance();
        String repositoryId = issue.getRepository().getID();
        String taskId = issue.getID();
        attributesSection.setExpanded(!config.isEditorSectionCollapsed(repositoryId, taskId, SECTION_ATTRIBUTES, false));
        attachmentsSection.setExpanded(!config.isEditorSectionCollapsed(repositoryId, taskId, SECTION_ATTACHMENTS, true));
        subtasksSection.setExpanded(!config.isEditorSectionCollapsed(repositoryId, taskId, SECTION_SUBTASKS, true));
        commentsSection.setExpanded(!config.isEditorSectionCollapsed(repositoryId, taskId, SECTION_COMMENTS, false));
        privateSection.setExpanded(!config.isEditorSectionCollapsed(repositoryId, taskId, SECTION_PRIVATE, true));
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

    private void initSpellChecker () {
        Spellchecker.register(summaryField);
        Spellchecker.register(privateNotesField);
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
        RepositoryConfiguration rc = issue.getRepository().getRepositoryConfiguration(false);
        if(rc != null) {
            wikiLanguage = rc.getMarkupLanguage();
            addCommentPanel = WikiUtils.getWikiPanel(wikiLanguage, true, true);
            WikiPanel addCommentsPanel = getAddCommentsPanel();            
            if (addCommentPanel != null) {
                ((GroupLayout) newCommentSectionPanel.getLayout()).replace(dummyAddCommentPanel, addCommentsPanel);
            }

            commentsPanel.setWikiLanguage(wikiLanguage);

            descriptionPanel = WikiUtils.getWikiPanel(wikiLanguage, false, true);
            ((GroupLayout) attributesSectionPanel.getLayout()).replace(dummyDescriptionPanel, descriptionPanel);
        } 
    }

    ODCSIssue getIssue() {
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
                
                if (isDirty) {
                    issue.fireChangeEvent();
                } 
            }
        });
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
        
        final RepositoryConfiguration rc = issue.getRepository().getRepositoryConfiguration(false);
        
        if(rc != null) {
            Collection<Keyword> kws = rc.getKeywords();
            keywords.clear();
            for (Keyword keyword : kws) {
                keywords.add(keyword.getName());
            }
            setupListeners();
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
        
        if(rc != null) {
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
    }

    private ComboBoxModel toComboModel(List items) {
        return new DefaultComboBoxModel(items.toArray());
    }

    private int oldCommentCount;
    void reloadForm(boolean force) {
        if (skipReload) { // XXX when set?
            return;
        }
        enableComponents(true);
        clearHighlights();
        reloading = true;
        boolean isNew = issue.isNew();
        
        newCommentSection.setLabel(NbBundle.getMessage(IssuePanel.class, isNew ? "IssuePanel.description" : "IssuePanel.newCommentSection.label")); // NOI18N
        reportedLabel.setVisible(!isNew);
        reportedField.setVisible(!isNew);
        modifiedLabel.setVisible(!isNew);
        modifiedField.setVisible(!isNew);
        resolutionLabel.setVisible(!isNew);
        resolutionCombo.setVisible(!isNew);
        resolutionWarning.setVisible(!isNew);
        commentsSection.setVisible(!isNew);
        attachmentsSection.setVisible(!isNew);
        refreshButton.setVisible(!isNew);
        separatorLabel.setVisible(!isNew);
        cancelButton.setVisible(!isNew);
        separatorLabel5.setVisible(!isNew);
        btnDeleteTask.setVisible(isNew);
        separatorLabelDismiss.setVisible(isNew);
        separatorLabel4.setVisible(!isNew);
        showInBrowserButton.setVisible(!isNew);
        parentLabel.setVisible(!isNew);
        parentField.setVisible(!isNew);
        parentButton.setVisible(!isNew);
        subtasksSection.setVisible(!isNew);
        externalLabel.setVisible(!isNew);
        externalField.setVisible(!isNew);
        dummySubtaskPanel.setVisible(false);
        org.openide.awt.Mnemonics.setLocalizedText(submitButton, NbBundle.getMessage(IssuePanel.class, isNew ? "IssuePanel.submitButton.text.new" : "IssuePanel.submitButton.text")); // NOI18N
        descriptionLabel.setVisible(!isNew);
        if(descriptionPanel != null) {
            descriptionPanel.setVisible(!isNew);
        }
        privateDueDatePicker.getComponent().setEnabled(false);
        
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
        reloadField(issueTypeCombo, IssueField.TASK_TYPE);
        reloadField(summaryField, IssueField.SUMMARY);
        reloadField(productCombo, IssueField.PRODUCT);
        reloadField(componentCombo, IssueField.COMPONENT);
        reloadField(releaseCombo, IssueField.MILESTONE);
        reloadField(descriptionPanel, IssueField.DESCRIPTION);
        reloadField(statusCombo, IssueField.STATUS);
        initStatusCombo(issue.getFieldValue(IssueField.STATUS));
        reloadField(resolutionCombo, IssueField.RESOLUTION);
        reloadField(duplicateField, IssueField.DUPLICATE);
        reloadField(priorityCombo, IssueField.PRIORITY);
        reloadField(severityCombo, IssueField.SEVERITY);
        reloadField(iterationCombo, IssueField.ITERATION);
        reloadField(keywordsField, IssueField.KEYWORDS);

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
            
            String privateNotes = issue.getPrivateNotes();                
            setPrivateSectionLabel(privateNotes);
            privateDueDatePicker.setDate(issue.getDueDate());
            NbDateRange scheduleDate = issue.getScheduleDate();
            scheduleDatePicker.setScheduleDate(scheduleDate == null ? null : scheduleDate.toSchedulingInfo());
            privateEstimateField.setValue(issue.getEstimate());
        }

        reloadField(ownerCombo, IssueField.OWNER);
        reloadField(ccField, IssueField.CC);
        reloadField(subtaskField, IssueField.SUBTASK);
        reloadField(parentField, IssueField.PARENT);
        reloadField(dueDatePicker, IssueField.DUEDATE);
        reloadField(estimateField, IssueField.ESTIMATE);
        reloadField(foundInField, IssueField.FOUNDIN);
        int newCommentCount = issue.getComments().length;
        if (!force) {
            if (oldCommentCount != newCommentCount) {
                String message = NbBundle.getMessage(IssuePanel.class, "IssuePanel.commentAddedWarning"); // NOI18N
                fieldsIncoming.put(IssueField.COMMENT_COUNT, message);
            } else {
                fieldsIncoming.remove(IssueField.COMMENT_COUNT);
            }
        }
        oldCommentCount = newCommentCount;
        oldCommentCount = newCommentCount;
        List<Attachment> attachments = issue.getAttachments();
        List<AttachmentsPanel.AttachmentInfo> unsubmitted = issue.getUnsubmittedAttachments();
        if (!isNew) {
            commentsPanel.setIssue(issue, attachments);
            commentsSection.setLabel(NbBundle.getMessage(IssuePanel.class, "IssuePanel.commentsSection.label", issue.getComments().length)); //NOI18N
        }
        attachmentsPanel.setAttachments(attachments, unsubmitted, null);
        attachmentsSection.setLabel(NbBundle.getMessage(IssuePanel.class, "IssuePanel.attachmentsLabel.text", attachments.size())); //NOI18N
        UIUtils.keepFocusedComponentVisible(commentsPanel, this);
        UIUtils.keepFocusedComponentVisible(attachmentsPanel, this);
        WikiPanel addCommentsPane = getAddCommentsPanel();
        if (addCommentsPane != null) {
            if (isNew) {
                reloadField(getAddCommentsPanel().getCodePane(), IssueField.DESCRIPTION);
            } else {
                reloadField(getAddCommentsPanel().getCodePane(), IssueField.COMMENT);
            }
        }
        
        boolean hasSubtasks = issue.hasSubtasks();
        if (subTaskScrollPane != null) {
            subTaskScrollPane.setVisible(hasSubtasks);
        }
        subtasksSection.setLabel(NbBundle.getMessage(IssuePanel.class, "IssuePanel.subtasksLabel.text", 0)); //NOI18N
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
                            subtasksSection.setLabel(NbBundle.getMessage(IssuePanel.class, "IssuePanel.subtasksLabel.text", tableModel.getRowCount())); //NOI18N
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
                                ((GroupLayout) subtasksSectionPanel.getLayout()).replace(dummySubtaskPanel, subTaskScrollPane);
                            }
                            revalidate();
                        }
                    });
                }
            });
        }
        updateFieldStatuses();
        updateNoSummary();
        updateMessagePanel();
        cancelButton.setEnabled(issue.hasLocalEdits() || !unsavedFields.isEmpty());
        reloading = false;
    }

    private void setPrivateSectionLabel(String privateNotes) {
        if(privateNotes != null && !privateNotes.isEmpty() ) {
            privateSection.setLabel("<html>" + // NOI18N
                    org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.privateAttributesSection.label") + // NOI18N
                    " (<i><b>" +  // NOI18N
                    org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.privateAttributesSection.containsNotes") +  // NOI18N
                    "</b></i>)</html>"); // NOI18N
        } else {
            privateSection.setLabel(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.privateAttributesSection.label")); // NOI18N
        }
    }
    
    private WikiPanel getAddCommentsPanel() {
        if(addCommentPanel == null) {
            initWikiPanels();
        }
        assert addCommentPanel != null;
        return addCommentPanel;
    }
    
    private static void fixPrefSize(JTextField textField) {
        // The preferred size of JTextField on (Classic) Windows look and feel
        // is one pixel shorter. The following code is a workaround.
        textField.setPreferredSize(null);
        Dimension dim = textField.getPreferredSize();
        Dimension fixedDim = new Dimension(dim.width+1, dim.height);
        textField.setPreferredSize(fixedDim);
    }
    
    private void reloadField (Object component, IssueField field) {
        String newValue;
        if (component instanceof JList) {
            newValue = mergeValues(issue.getFieldValues(field));
        } else {
            newValue = issue.getFieldValue(field);
        }
        boolean fieldDirty = unsavedFields.contains(field.getKey());
        if (!fieldDirty) {
            if (component instanceof JComboBox) {
                JComboBox combo = (JComboBox)component;
                selectInCombo(combo, newValue, true);
            } else if (component instanceof JTextComponent) {
                String value = newValue;
                
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
            } else if (component instanceof IDEServices.DatePickerComponent) {
                IDEServices.DatePickerComponent picker = (IDEServices.DatePickerComponent) component;
                picker.setDate(ODCSUtil.parseLongDate(newValue, new DateFormat[] { INPUT_DATE_FORMAT, READABLE_DATE_FORMAT }));
            }
        }
    }
    
    private void updateFieldDecorations (Object component, IssueField field, JLabel warningLabel, JComponent fieldLabel) {
        updateFieldDecorations(warningLabel, fieldLabel, fieldName(fieldLabel), Pair.of(field, component));
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
            Pair<IssueField, ? extends Object>... fields) {
        boolean isNew = issue.isNew();
        String newValue = "", lastSeenValue = "", repositoryValue = ""; //NOI18N
        boolean fieldDirty = false;
        boolean valueModifiedByUser = false;
        boolean valueModifiedByServer = false;
        for (Pair<IssueField, ? extends Object> p : fields) {
            Object component = p.second();
            IssueField field = p.first();
            if (component instanceof JList) {
                newValue += " " + mergeValues(toReadable(issue.getFieldValues(field), field));
                lastSeenValue += " " + mergeValues(toReadable(issue.getLastSeenFieldValues(field), field));
                repositoryValue += " " + mergeValues(toReadable(issue.getRepositoryFieldValues(field), field));
            } else {
                newValue += " " + toReadable(issue.getFieldValue(field), field);
                lastSeenValue += " " + toReadable(issue.getLastSeenFieldValue(field), field);
                repositoryValue += " " + toReadable(issue.getRepositoryFieldValue(field), field);
            }
            fieldDirty |= unsavedFields.contains(field.getKey());
            valueModifiedByUser |= (issue.getFieldStatus(field) & ODCSIssue.FIELD_STATUS_OUTGOING) != 0;
            valueModifiedByServer |= (issue.getFieldStatus(field) & ODCSIssue.FIELD_STATUS_MODIFIED) != 0;
        }
        newValue = newValue.substring(1);
        lastSeenValue = lastSeenValue.substring(1);
        repositoryValue = repositoryValue.substring(1);
        if (warningLabel != null) {
            boolean change = false;
            if (!isNew) {
                IssueField field = fields[0].first();
                removeTooltips(warningLabel, field);
                if (fieldLabel != null && fieldLabel.getFont().isBold()) {
                    fieldLabel.setFont(fieldLabel.getFont().deriveFont(fieldLabel.getFont().getStyle() & ~Font.BOLD));
                }
                boolean visible = warningLabel.isVisible();
                if (visible && valueModifiedByServer && (valueModifiedByUser || fieldDirty) && !newValue.equals(repositoryValue)) {
                    String message = Bundle.IssuePanel_fieldModifiedConflict(fieldName, lastSeenValue, repositoryValue);
                    // do not use ||
                    change = fieldsLocal.remove(field) != null | fieldsIncoming.remove(field) != null
                            | !message.equals(fieldsConflict.put(field, message));
                    tooltipsConflict.addTooltip(warningLabel, field, Bundle.IssuePanel_fieldModifiedConflictTT(
                            fieldName, lastSeenValue, repositoryValue, newValue, ICON_CONFLICT_PATH));
                } else if (visible && valueModifiedByServer) {
                    String message = Bundle.IssuePanel_fieldModifiedRemotely(fieldName, lastSeenValue, repositoryValue);
                    // do not use ||
                    change = fieldsLocal.remove(field) != null | fieldsConflict.remove(field) != null
                            | !message.equals(fieldsIncoming.put(field, message));
                    tooltipsIncoming.addTooltip(warningLabel, field, Bundle.IssuePanel_fieldModifiedRemotelyTT(
                            fieldName, lastSeenValue, repositoryValue, ICON_REMOTE_PATH));
                } else if (visible && (valueModifiedByUser || fieldDirty) && !newValue.equals(lastSeenValue)) {
                    String message;
                    if (field == IssueField.COMMENT) {
                        message = Bundle.IssuePanel_commentAddedLocally();
                        tooltipsLocal.addTooltip(warningLabel, field, Bundle.IssuePanel_commentAddedLocallyTT(ICON_UNSUBMITTED_PATH));
                    } else {
                        message = Bundle.IssuePanel_fieldModifiedLocally(fieldName, lastSeenValue, newValue);
                        tooltipsLocal.addTooltip(warningLabel, field, Bundle.IssuePanel_fieldModifiedLocallyTT(
                                fieldName, lastSeenValue, newValue, ICON_UNSUBMITTED_PATH));
                    }
                    // do not use ||
                    change = fieldsConflict.remove(field) != null | fieldsIncoming.remove(field) != null
                            | !message.equals(fieldsLocal.put(field, message));
                } else {
                    // do not use ||
                    change = fieldsLocal.remove(field) != null
                            | fieldsConflict.remove(field) != null
                            | fieldsIncoming.remove(field) != null;
                }
                updateIcon(warningLabel);
                if (fieldDirty && fieldLabel != null) {
                    fieldLabel.setFont(fieldLabel.getFont().deriveFont(fieldLabel.getFont().getStyle() | Font.BOLD));
                }
            }
            if (change && !reloading) {
                updateMessagePanel();
            }
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
            removeTooltips(attachmentsWarning, IssueField.NB_NEW_ATTACHMENTS);
            AbstractButton attachmentsLabel = attachmentsSection.getLabelComponent();
            if (attachmentsLabel.getFont().isBold()) {
                attachmentsLabel.setFont(attachmentsLabel.getFont().deriveFont(attachmentsLabel.getFont().getStyle() & ~Font.BOLD));
            }
            if (valueModifiedByUser) {
                String message = Bundle.IssuePanel_attachmentsAddedLocally();
                tooltipsLocal.addTooltip(attachmentsWarning, IssueField.NB_NEW_ATTACHMENTS,
                        Bundle.IssuePanel_attachmentsToSubmit(ICON_UNSUBMITTED_PATH));
                change = !message.equals(fieldsLocal.put(IssueField.NB_NEW_ATTACHMENTS, message));
            } else {
                change = fieldsLocal.remove(IssueField.NB_NEW_ATTACHMENTS) != null;
            }
            updateIcon(attachmentsWarning);
            if (unsavedFields.contains(IssueField.NB_NEW_ATTACHMENTS.getKey())) {
                attachmentsLabel.setFont(attachmentsLabel.getFont().deriveFont(attachmentsLabel.getFont().getStyle() | Font.BOLD));
            }
        }
        if (change && !reloading) {
            updateMessagePanel();
        }
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
    
    private String fieldName(JComponent fieldLabel) {
        assert fieldLabel instanceof JLabel || fieldLabel instanceof AbstractButton;
        String txt = "";
        if(fieldLabel instanceof JLabel) {
            txt = ((JLabel) fieldLabel).getText().trim();
            
        } else if(fieldLabel instanceof AbstractButton) {
            txt = ((AbstractButton) fieldLabel).getText().trim();
        } else {
            return null;
        }
        if (txt.endsWith(":")) { // NOI18N
            txt = txt.substring(0, txt.length()-1);
        }
        return txt;
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
    
    private boolean selectInCombo(JComboBox combo, Object value, boolean forceInModel) {
        if (value == null) {
            return false;
        }
        if(combo == ownerCombo && value.equals("")) {
            value = NO_USER_PROFILE;
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

    private void initStatusCombo(String status) {
        // Init statusCombo - allowed transitions (heuristics):
        // Open -> Open-Unconfirmed-Reopened+Resolved
        // Resolved -> Reopened+Close
        // Close-Resolved -> Reopened+Resolved+(Close with higher index)
        RepositoryConfiguration rc = issue.getRepository().getRepositoryConfiguration(false);
        
        if (rc != null) {
            String initialStatus = issue.getLastSeenFieldValue(IssueField.STATUS);
            if (initialStatus.isEmpty()) {
                initialStatus = status;
            }
            List<TaskStatus> statuses = rc.computeValidStatuses(ODCSUtil.getStatusByValue(rc, initialStatus));

            // XXX evaluate statuses for open and active
            resolvedIndex = statuses.size(); // if there is no RESOLVED
            for (int i = 0; i < statuses.size(); i++) {
                TaskStatus s = statuses.get(i);
                if (s.getValue().equals(RESOLUTION_RESOLVED)) {
                    resolvedIndex = i;
                    break;
                }
            }
            
            statusCombo.setModel(toComboModel(statuses));
            statusCombo.setRenderer(new ClientDataRenderer());
            selectInCombo(statusCombo, ODCSUtil.getStatusByValue(rc, status), false);
        }
    }    
    
    private void updateReadOnlyField(JTextField field) {
        if ("GTK".equals(UIManager.getLookAndFeel().getID())) { // NOI18N
            field.setUI(new BasicTextFieldUI());
        }
        field.setBackground(getBackground());
        Color bkColor = getBackground();
        if( null != bkColor ) {
            bkColor = new Color( bkColor.getRGB() );
        }
        field.setBackground(bkColor);
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
    }

    private void updateFieldStatuses() {
        updateFieldStatus(IssueField.SUMMARY, summaryLabel);
        updateFieldDecorations(summaryField, IssueField.SUMMARY, summaryWarning, summaryLabel);
        updateFieldStatus(IssueField.DESCRIPTION, descriptionLabel);
        updateFieldDecorations(descriptionPanel.getCodePane(), IssueField.DESCRIPTION, descriptionWarning, descriptionLabel);
        updateFieldStatus(IssueField.PRODUCT, productLabel);
        updateFieldDecorations(productCombo, IssueField.PRODUCT, productWarning, productLabel);
        updateFieldStatus(IssueField.COMPONENT, componentLabel);
        updateFieldDecorations(componentCombo, IssueField.COMPONENT, componentWarning, componentLabel);
        updateFieldStatus(IssueField.FOUNDIN, foundInLabel);
        updateFieldDecorations(foundInField, IssueField.FOUNDIN, foundInWarning, foundInLabel);
        updateFieldStatus(IssueField.STATUS, statusLabel);
        updateFieldDecorations(statusCombo, IssueField.STATUS, statusWarning, statusLabel);
        updateFieldStatus(IssueField.RESOLUTION, resolutionLabel);
        updateFieldDecorations(resolutionCombo, IssueField.RESOLUTION, resolutionWarning, resolutionLabel);
        updateFieldStatus(IssueField.DUPLICATE, duplicateLabel);
        updateFieldDecorations(duplicateWarning, duplicateLabel, Bundle.LBL_Duplicate_fieldName(), Pair.of(IssueField.DUPLICATE, duplicateField));
        updateFieldStatus(IssueField.PRIORITY, priorityLabel);
        updateFieldDecorations(priorityCombo, IssueField.PRIORITY, priorityWarning, priorityLabel);
        updateFieldStatus(IssueField.SEVERITY, severityLabel);
        updateFieldDecorations(severityCombo, IssueField.SEVERITY, severityWarning, severityLabel);
        updateFieldStatus(IssueField.MILESTONE, releaseLabel);
        updateFieldDecorations(releaseCombo, IssueField.MILESTONE, releaseWarning, releaseLabel);
        updateFieldStatus(IssueField.DUEDATE, dueDateLabel);
        updateFieldDecorations(dueDatePicker, IssueField.DUEDATE, dueDateWarning, dueDateLabel);
        updateFieldStatus(IssueField.ITERATION, iterationLabel);
        updateFieldDecorations(iterationCombo, IssueField.ITERATION, iterationWarning, iterationLabel);
        updateFieldStatus(IssueField.OWNER, ownerLabel);
        updateFieldDecorations(ownerCombo, IssueField.OWNER, ownerWarning, ownerLabel);
        updateFieldStatus(IssueField.ESTIMATE, estimateLabel);
        updateFieldDecorations(estimateField, IssueField.ESTIMATE, estimateWarning, estimateLabel);
        updateFieldStatus(IssueField.PARENT, parentLabel);
        updateFieldDecorations(parentField, IssueField.PARENT, parentWarning, parentLabel);
        updateFieldDecorations(subtaskField, IssueField.SUBTASK, subtaskWarning, subtasksSection.getLabelComponent());
        updateFieldStatus(IssueField.CC, ccLabel);
        updateFieldDecorations(ccField, IssueField.CC, ccWarning, ccLabel);
        updateFieldStatus(IssueField.KEYWORDS, keywordsLabel);
        updateFieldDecorations(keywordsField, IssueField.KEYWORDS, keywordsWarning, keywordsLabel);
        updateFieldStatus(IssueField.TASK_TYPE, issueTypeLabel);
        updateFieldDecorations(issueTypeCombo, IssueField.TASK_TYPE, issueTypeWarning, issueTypeLabel);
        updateFieldStatus(IssueField.MODIFIED, modifiedLabel);
        WikiPanel addCommentsPanel = getAddCommentsPanel();
        if (addCommentsPanel != null) {
            updateFieldDecorations(addCommentsPanel.getCodePane(), IssueField.COMMENT, addCommentWarning, newCommentSection.getLabelComponent());
        }
//        for (CustomFieldInfo field : customFields) {
//            updateFieldStatus(field.field, field.label);
//        }
        updateAttachmentsStatus();
        
        updateFieldDecorations(ATTRIBUTE_PRIVATE_NOTES, notesLabel);
        updateFieldDecorations(ATTRIBUTE_DUE_DATE, privateDueDateLabel);
        updateFieldDecorations(ATTRIBUTE_SCHEDULE_DATE, scheduleDateLabel);
        updateFieldDecorations(ATTRIBUTE_ESTIMATE, privateEstimateLabel);
        
        repaint();
    }

    private void updateFieldStatus(IssueField field, JComponent label) {
        assert label instanceof AbstractButton || label instanceof JLabel;
        boolean highlight = !issue.isNew() && (issue.getFieldStatus(field) & ODCSIssue.FIELD_STATUS_MODIFIED) != 0;
        label.setOpaque(highlight);
        if (highlight) {
            label.setBackground(highlightColor);
        }
    }

    PropertyChangeListener cacheListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getSource() != IssuePanel.this.issue) {
                return;
            }
            if (IssueStatusProvider.EVENT_STATUS_CHANGED.equals(evt.getPropertyName())) {
                Mutex.EVENT.readAccess(new Runnable() {
                    @Override
                    public void run () {
                        updateFieldStatuses();
                    }
                });
            }
        }
    };

    private void findIssue(JTextField fld, String msg, String helpCtx, boolean append) {
        Issue i = IssueQuickSearch.selectIssue(
            NbBundle.getMessage(IssuePanel.class, msg), 
            ODCSUtil.getRepository(issue.getRepository()),
            this,
            new HelpCtx(helpCtx));
        if (i != null) {
            if(append) {
                StringBuilder sb = new StringBuilder();
                if (!fld.getText().trim().equals("")) {                         // NOI18N
                    sb.append(fld.getText()).append(',').append(' ');           // NOI18N
                }
                sb.append(i.getID());
                fld.setText(sb.toString());                
            } else {
                fld.setText(i.getID());
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
        } else if (item == null) {
            return ""; // NOI18N
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
    
    private void setupListeners () {
        if (issue.isNew()) {
            getAddCommentsPanel().getCodePane().getDocument().addDocumentListener(new FieldChangeListener(
                    getAddCommentsPanel().getCodePane(), IssueField.DESCRIPTION) {
                @Override
                void fieldModified () {
                    // still new?
                    if (issue.isNew()) {
                        super.fieldModified();
                    }
                }
            });
        }
        getAddCommentsPanel().getCodePane().getDocument().addDocumentListener(new FieldChangeListener(
                getAddCommentsPanel().getCodePane(), IssueField.COMMENT, addCommentWarning, newCommentSection.getLabelComponent()) {
            @Override
            void fieldModified () {
                if (!(reloading || issue.isNew())) {
                    issue.setFieldValue(IssueField.COMMENT, getAddCommentsPanel().getCodePane().getText().trim());
                    unsavedFields.add(IssueField.COMMENT.getKey());
                    updateDecorations();
                }
            }
        });
        descriptionPanel.getCodePane().getDocument().addDocumentListener(new FieldChangeListener(
                descriptionPanel.getCodePane(), IssueField.DESCRIPTION, descriptionWarning, descriptionLabel));
        summaryField.getDocument().addDocumentListener(new FieldChangeListener(summaryField, IssueField.SUMMARY, summaryWarning, summaryLabel));
        productCombo.addActionListener(new FieldChangeListener(productCombo, IssueField.PRODUCT, productWarning, productLabel));
        componentCombo.addActionListener(new FieldChangeListener(componentCombo, IssueField.COMPONENT, componentWarning, componentLabel));
        releaseCombo.addActionListener(new FieldChangeListener(releaseCombo, IssueField.MILESTONE, releaseWarning, releaseLabel));
        iterationCombo.addActionListener(new FieldChangeListener(iterationCombo, IssueField.ITERATION, iterationWarning, iterationLabel));
        priorityCombo.addActionListener(new FieldChangeListener(priorityCombo, IssueField.PRIORITY, priorityWarning, priorityLabel));
        severityCombo.addActionListener(new FieldChangeListener(severityCombo, IssueField.SEVERITY, severityWarning, severityLabel));
        statusCombo.addActionListener(new FieldChangeListener(statusCombo, IssueField.STATUS, statusWarning, statusLabel));
        
        ownerCombo.addActionListener(new FieldChangeListener(ownerCombo, IssueField.OWNER, ownerWarning, ownerLabel) {
            @Override
            void fieldModified () {
                if (!reloading) {
                    storeFieldValue(IssueField.OWNER, ((TaskUserProfile) ownerCombo.getSelectedItem()).getLoginName());
                    updateDecorations();
                }
            }
        });
        parentField.getDocument().addDocumentListener(new FieldChangeListener(parentField, IssueField.PARENT, parentWarning, parentLabel) {
            @Override
            void fieldModified () {
                if (!reloading) {
                    storeFieldValues(IssueField.PARENT, bugs(parentField.getText()));
                    updateDecorations();
                }
            }
        });
        subtaskField.getDocument().addDocumentListener(new FieldChangeListener(subtaskField, IssueField.SUBTASK, subtaskWarning, subtasksSection.getLabelComponent()) {
            @Override
            void fieldModified () {
                if (!reloading) {
                    storeFieldValue(IssueField.SUBTASK, mergeValues(bugs(subtaskField.getText())));
                    updateDecorations();
                }
            }
        });
        keywordsField.getDocument().addDocumentListener(new FieldChangeListener(keywordsField, IssueField.KEYWORDS, keywordsWarning, keywordsLabel) {
            @Override
            void fieldModified () {
                if (!reloading) {
                    storeFieldValues(IssueField.KEYWORDS, keywords(keywordsField.getText()));
                    updateDecorations();
                }
            }
        });
        issueTypeCombo.addActionListener(new FieldChangeListener(issueTypeCombo, IssueField.TASK_TYPE, issueTypeWarning, issueTypeLabel));
        estimateField.getDocument().addDocumentListener(new FieldChangeListener(estimateField, IssueField.ESTIMATE, estimateWarning, estimateLabel));
        foundInField.getDocument().addDocumentListener(new FieldChangeListener(foundInField, IssueField.FOUNDIN, foundInWarning, foundInLabel));
        
        ccField.getDocument().addDocumentListener(new FieldChangeListener(ccField, IssueField.CC, ccWarning, ccLabel) {
            @Override
            public void fieldModified () {
                if (!reloading) {
                    storeFieldValues(IssueField.CC, list(ccField.getText()));
                    updateDecorations();
                }
            }
        });
        
        dueDatePicker.addChangeListener(new FieldChangeListener(dueDatePicker.getComponent(), IssueField.DUEDATE,
                dueDateWarning, dueDateLabel, fieldName(dueDateLabel), Pair.of(IssueField.DUEDATE, dueDatePicker)) {
            @Override
            public void fieldModified () {
                if (!reloading && isEnabled()) {
                    Date dueDate = dueDatePicker.getDate();
                    String value = dueDate == null ? "" : Long.toString(dueDate.getTime()); //NOI18N
                    storeFieldValue(IssueField.DUEDATE, value); //NOI18N
                    privateDueDatePicker.setDate(dueDate);
                    updateDecorations();
                }
            }
        });
        
        resolutionCombo.addActionListener(new FieldChangeListener(resolutionCombo, IssueField.RESOLUTION, resolutionWarning, resolutionLabel));
        duplicateField.getDocument().addDocumentListener(new FieldChangeListener(duplicateField, IssueField.DUPLICATE,
                duplicateWarning, duplicateLabel, Bundle.LBL_Duplicate_fieldName()) {
            @Override
            public void fieldModified () {
                if (!reloading && duplicateField.isVisible() && duplicateField.isEditable()) {
                    storeFieldValue(IssueField.DUPLICATE, duplicateField); //NOI18N
                    updateDecorations();
                }
            }
        });
        attachmentsPanel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged (ChangeEvent e) {
                if (!reloading && attachmentsPanel.isVisible()) {
                    if (issue.setUnsubmittedAttachments(attachmentsPanel.getNewAttachments())) {
                        unsavedFields.add(IssueField.NB_NEW_ATTACHMENTS.getKey());
                        updateAttachmentsStatus();
                    }
                }
            }
        });
        
        privateNotesField.getDocument().addDocumentListener(new TaskAttributeListener(privateNotesField, ATTRIBUTE_PRIVATE_NOTES, notesLabel) {

            @Override
            protected boolean storeValue () {
                String txt = privateNotesField.getText();
                issue.setTaskPrivateNotes(txt);
                setPrivateSectionLabel(txt);
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
                Object valueObj = privateEstimateField.getValue();
                int value = valueObj instanceof Number ? ((Number) valueObj).intValue() : 0;
                if (value != issue.getEstimate()) {
                    issue.setTaskEstimate(value, false);
                    return true;
                } else {
                    return false;
                }
            }
            
        });
    }

    private void storeFieldValue (IssueField field, JComboBox combo) {
        Object value = combo.getSelectedItem();
        if(value instanceof NoUserProfile) {
            value = null;
        }
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
        if (!issue.getFieldValue(field).equals(value)) {
            unsavedFields.add(field.getKey());
            issue.setFieldValue(field, value);
        }
    }
    
    private void storeFieldValues(IssueField field, List<String> values) {
        if (!issue.getFieldValues(field).equals(values)) {
            unsavedFields.add(field.getKey());
            issue.setFieldValues(field, values);
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
            this(component, field, warningLabel, fieldLabel,
                    fieldLabel == null ? null : fieldName(fieldLabel),
                    Pair.of(field, component));
        }

        public FieldChangeListener (JComponent component, IssueField field, JLabel warningLabel,
                JComponent fieldLabel, String fieldName) {
            this(component, field, warningLabel, fieldLabel, fieldName, Pair.of(field, component));
        }
        
        public FieldChangeListener (JComponent component, IssueField field, JLabel warningLabel,
                JComponent fieldLabel, String fieldName, Pair<IssueField, ? extends Object>... multiField) {
            this.component = component;
            this.field = field;
            this.warningLabel = warningLabel;
            this.fieldLabel = fieldLabel;
            this.fieldName = fieldName;
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
                if (component instanceof JTextComponent) {
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
                } else {
                    comp.setEnabled(true);
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
        if (noSummary || sameParent || cyclicDependency || invalidTag || noComponent || noIteration || noTargetMilestione || noDuplicateId) {
            submitButton.setEnabled(false);
        } else {
            submitButton.setEnabled(true);
        }
        for (Pair<Map<IssueField, String>, ImageIcon> p : new Pair[] {
            Pair.of(fieldsConflict, ICON_CONFLICT),
            Pair.of(fieldsIncoming, ICON_REMOTE),
            Pair.of(fieldsLocal, ICON_UNSUBMITTED)
        }) {
            for (Map.Entry<IssueField, String> e : p.first().entrySet()) {
                JLabel lbl = new JLabel(e.getValue());
                lbl.setIcon(p.second());
                messagePanel.add(lbl);
            }
        }
        if (noSummary || sameParent || cyclicDependency || invalidTag || noComponent || noIteration
                || noTargetMilestione || noDuplicateId
                || (fieldsConflict.size() + fieldsIncoming.size() + fieldsLocal.size() > 0)) {
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

        attributesSectionPanel = new javax.swing.JPanel();
        ownerWarning = new javax.swing.JLabel();
        estimateField = new javax.swing.JTextField();
        resolutionWarning = new javax.swing.JLabel();
        severityLabel = new javax.swing.JLabel();
        keywordsLabel = new javax.swing.JLabel();
        descriptionLabel = new javax.swing.JLabel();
        dueDateLabel = new javax.swing.JLabel();
        ccWarning = new javax.swing.JLabel();
        productWarning = new javax.swing.JLabel();
        componentLabel = new javax.swing.JLabel();
        duplicateLabel = new javax.swing.JLabel();
        releaseWarning = new javax.swing.JLabel();
        statusLabel = new javax.swing.JLabel();
        duplicateWarning = new javax.swing.JLabel();
        keywordsWarning = new javax.swing.JLabel();
        ownerCombo = new javax.swing.JComboBox();
        priorityWarning = new javax.swing.JLabel();
        descriptionWarning = new javax.swing.JLabel();
        estimateWarning = new javax.swing.JLabel();
        iterationWarning = new javax.swing.JLabel();
        dueDateWarning = new javax.swing.JLabel();
        foundInField = new javax.swing.JTextField();
        summaryLabel = new javax.swing.JLabel();
        severityWarning = new javax.swing.JLabel();
        issueTypeLabel = new javax.swing.JLabel();
        foundInWarning = new javax.swing.JLabel();
        dummyDueDateField = new javax.swing.JTextField();
        ownerLabel = new javax.swing.JLabel();
        externalLabel = new javax.swing.JLabel();
        dummyDescriptionPanel = new javax.swing.JPanel();
        componentWarning = new javax.swing.JLabel();
        estimateLabel = new javax.swing.JLabel();
        priorityLabel = new javax.swing.JLabel();
        ccLabel = new javax.swing.JLabel();
        productLabel = new javax.swing.JLabel();
        foundInLabel = new javax.swing.JLabel();
        iterationLabel = new javax.swing.JLabel();
        statusWarning = new javax.swing.JLabel();
        externalWarning = new javax.swing.JLabel();
        releaseLabel = new javax.swing.JLabel();
        parentWarning = new javax.swing.JLabel();
        resolutionCombo = new javax.swing.JComboBox();
        parentLabel = new javax.swing.JLabel();
        summaryWarning = new javax.swing.JLabel();
        resolutionLabel = new javax.swing.JLabel();
        newCommentSectionPanel = new javax.swing.JPanel();
        addCommentWarning = new javax.swing.JLabel();
        dummyAddCommentPanel = new javax.swing.JPanel();
        messagePanel = new javax.swing.JPanel();
        attachmentsSectionPanel = new javax.swing.JPanel();
        attachmentsWarning = new javax.swing.JLabel();
        dummyAttachmentsPanel = new javax.swing.JPanel();
        subtasksSectionPanel = new javax.swing.JPanel();
        subtaskWarning = new javax.swing.JLabel();
        dummySubtaskPanel = new javax.swing.JPanel();
        commentsSectionPanel = new javax.swing.JPanel();
        dummyCommentsPanel = new javax.swing.JPanel();
        privatePanel = new javax.swing.JPanel();
        privateDueDateLabel = new javax.swing.JLabel();
        dummyPrivateDueDateField = new javax.swing.JTextField();
        scheduleDateLabel = new javax.swing.JLabel();
        dummyScheduleDateField = new javax.swing.JTextField();
        privateEstimateLabel = new javax.swing.JLabel();
        privateEstimateField = new javax.swing.JFormattedTextField();
        notesLabel = new javax.swing.JLabel();
        privateNotesFieldScrollPane = new javax.swing.JScrollPane();
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
        }
        ;
        headerPanel = new javax.swing.JPanel();
        separatorLabel = new javax.swing.JLabel();
        parentHeaderPanel = new javax.swing.JPanel();
        headerLabel = new javax.swing.JLabel();
        headerField = new javax.swing.JTextField();
        separatorLabel4 = new javax.swing.JLabel();
        separatorLabel5 = new javax.swing.JLabel();
        separatorLabelDismiss = new javax.swing.JLabel();
        mainScrollPane = new javax.swing.JScrollPane();
        mainPanel = new javax.swing.JPanel() {

            @Override
            public Dimension getPreferredSize () {
                return getMinimumSize();
            }
        };
        attributesSection = new org.netbeans.modules.bugtracking.commons.CollapsibleSectionPanel();
        attachmentsSection = new org.netbeans.modules.bugtracking.commons.CollapsibleSectionPanel();
        subtasksSection = new org.netbeans.modules.bugtracking.commons.CollapsibleSectionPanel();
        privateSection = new org.netbeans.modules.bugtracking.commons.CollapsibleSectionPanel();
        newCommentSection = new org.netbeans.modules.bugtracking.commons.SectionPanel();
        jPanel1 = new javax.swing.JPanel();
        commentsSection = new org.netbeans.modules.bugtracking.commons.CollapsibleSectionPanel();

        attributesSectionPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        org.openide.awt.Mnemonics.setLocalizedText(reportedLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.reportedLabel.text_1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(modifiedLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.modifiedLabel.text_1")); // NOI18N

        iterationCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                iterationComboActionPerformed(evt);
            }
        });

        estimateField.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.estimateField.text")); // NOI18N

        keywordsField.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.keywordsField.text")); // NOI18N

        statusCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statusComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(severityLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.severityLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(keywordsLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.keywordsLabel.text")); // NOI18N

        issueTypeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                issueTypeComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(descriptionLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.descriptionLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(dueDateLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.dueDateLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(duplicateButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.duplicateButton.text_1")); // NOI18N
        duplicateButton.setFocusPainted(false);
        duplicateButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        duplicateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                duplicateButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(parentButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.parentButton.text_1")); // NOI18N
        parentButton.setFocusPainted(false);
        parentButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        parentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parentButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(componentLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.componentLabel.text_1")); // NOI18N

        duplicateLabel.setLabelFor(duplicateField);
        org.openide.awt.Mnemonics.setLocalizedText(duplicateLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.duplicateLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(statusLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.statusLabel.text_1")); // NOI18N

        releaseCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                releaseComboActionPerformed(evt);
            }
        });

        parentField.setColumns(15);

        ownerCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ownerComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(ccButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.ccButton.text")); // NOI18N
        ccButton.setFocusPainted(false);
        ccButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ccButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ccButtonActionPerformed(evt);
            }
        });

        duplicateField.setColumns(15);
        duplicateField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                duplicateFieldActionPerformed(evt);
            }
        });

        foundInField.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.foundInField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(summaryLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.summaryLabel.text_1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(issueTypeLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.issueTypeLabel.text_1")); // NOI18N

        modifiedField.setEditable(false);
        modifiedField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        modifiedField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modifiedFieldActionPerformed(evt);
            }
        });

        externalField.setColumns(15);

        reportedField.setEditable(false);
        reportedField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        reportedField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reportedFieldActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(ownerLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.ownerLabel.text_1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(externalLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.externalLabel.text")); // NOI18N

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

        org.openide.awt.Mnemonics.setLocalizedText(estimateLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.estimateLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(priorityLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.priorityLabel.text_1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(ccLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.ccLabel.text")); // NOI18N

        componentCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                componentComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(productLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.productLabel.text_1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(foundInLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.foundInLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(iterationLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.iterationLabel.text_1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(releaseLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.releaseLabel.text_1")); // NOI18N

        productCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                productComboActionPerformed(evt);
            }
        });

        resolutionCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resolutionComboActionPerformed(evt);
            }
        });

        severityCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                severityComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(parentLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.parentLabel.text_1")); // NOI18N

        priorityCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                priorityComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(keywordsButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.keywordsButton.text")); // NOI18N
        keywordsButton.setFocusPainted(false);
        keywordsButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        keywordsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keywordsButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(resolutionLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.resolutionLabel.text")); // NOI18N

        javax.swing.GroupLayout attributesSectionPanelLayout = new javax.swing.GroupLayout(attributesSectionPanel);
        attributesSectionPanel.setLayout(attributesSectionPanelLayout);
        attributesSectionPanelLayout.setHorizontalGroup(
            attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(summaryWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(externalWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(issueTypeWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(statusWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(priorityWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(keywordsWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(foundInWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dueDateWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ownerWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ccWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(parentWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(descriptionWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(5, 5, 5)
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(descriptionLabel)
                            .addComponent(summaryLabel)
                            .addComponent(externalLabel)
                            .addComponent(parentLabel)
                            .addComponent(ccLabel)
                            .addComponent(ownerLabel)
                            .addComponent(dueDateLabel)
                            .addComponent(foundInLabel)
                            .addComponent(keywordsLabel)
                            .addComponent(priorityLabel)
                            .addComponent(statusLabel)
                            .addComponent(issueTypeLabel)))
                    .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                        .addComponent(productWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(productLabel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dummyDescriptionPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(summaryField, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(statusCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(priorityCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(productCombo, 0, 167, Short.MAX_VALUE)
                            .addComponent(foundInField, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dummyDueDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(resolutionWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(severityWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(componentWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(iterationWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(estimateWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(5, 5, 5)
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(resolutionLabel)
                            .addComponent(severityLabel)
                            .addComponent(componentLabel)
                            .addComponent(iterationLabel)
                            .addComponent(estimateLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(resolutionCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(severityCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(componentCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(iterationCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(estimateField, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(duplicateWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(releaseWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(5, 5, 5)
                        .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                                .addComponent(releaseLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(releaseCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                                .addComponent(duplicateLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(duplicateField, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(duplicateButton))))
                    .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                        .addComponent(ccField, javax.swing.GroupLayout.PREFERRED_SIZE, 586, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ccButton))
                    .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                        .addComponent(issueTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(reportedLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(reportedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(modifiedLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(modifiedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                        .addComponent(keywordsField, javax.swing.GroupLayout.PREFERRED_SIZE, 594, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(keywordsButton))
                    .addComponent(externalField, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                        .addComponent(parentField, javax.swing.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(parentButton))
                    .addComponent(ownerCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        attributesSectionPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {componentCombo, issueTypeCombo, ownerCombo, priorityCombo, productCombo, releaseCombo, resolutionCombo, severityCombo, statusCombo});

        attributesSectionPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {externalField, parentField});

        attributesSectionPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {ccField, keywordsField});

        attributesSectionPanelLayout.setVerticalGroup(
            attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(attributesSectionPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(issueTypeLabel)
                    .addComponent(issueTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(issueTypeWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reportedLabel)
                    .addComponent(reportedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(modifiedLabel)
                    .addComponent(modifiedField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(statusWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(statusLabel)
                    .addComponent(statusCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(resolutionWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(resolutionLabel)
                    .addComponent(resolutionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(duplicateWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(duplicateLabel)
                    .addComponent(duplicateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(duplicateButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(priorityWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(priorityLabel)
                    .addComponent(priorityCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(severityWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(severityLabel)
                    .addComponent(severityCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(keywordsWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(keywordsLabel)
                    .addComponent(keywordsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(keywordsButton))
                .addGap(18, 18, 18)
                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(productWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(productLabel)
                    .addComponent(productCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(componentWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(componentLabel)
                    .addComponent(componentCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(releaseWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(releaseLabel)
                    .addComponent(releaseCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(foundInWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(foundInLabel)
                    .addComponent(foundInField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(iterationWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(iterationLabel)
                    .addComponent(iterationCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(dueDateWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dueDateLabel)
                    .addComponent(dummyDueDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(estimateWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(estimateLabel)
                    .addComponent(estimateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(ownerWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ownerLabel)
                    .addComponent(ownerCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(ccWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ccLabel)
                    .addComponent(ccField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ccButton))
                .addGap(18, 18, 18)
                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(parentLabel)
                    .addComponent(parentField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(parentButton)
                    .addComponent(parentWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(externalWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(externalLabel)
                    .addComponent(externalField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(summaryField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(summaryLabel)
                    .addComponent(summaryWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(attributesSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dummyDescriptionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(descriptionLabel)
                    .addComponent(descriptionWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );

        newCommentSectionPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        javax.swing.GroupLayout dummyAddCommentPanelLayout = new javax.swing.GroupLayout(dummyAddCommentPanel);
        dummyAddCommentPanel.setLayout(dummyAddCommentPanelLayout);
        dummyAddCommentPanelLayout.setHorizontalGroup(
            dummyAddCommentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        dummyAddCommentPanelLayout.setVerticalGroup(
            dummyAddCommentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        messagePanel.setLayout(new javax.swing.BoxLayout(messagePanel, javax.swing.BoxLayout.PAGE_AXIS));

        javax.swing.GroupLayout newCommentSectionPanelLayout = new javax.swing.GroupLayout(newCommentSectionPanel);
        newCommentSectionPanel.setLayout(newCommentSectionPanelLayout);
        newCommentSectionPanelLayout.setHorizontalGroup(
            newCommentSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(newCommentSectionPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(addCommentWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addGroup(newCommentSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(messagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 813, Short.MAX_VALUE)
                    .addComponent(dummyAddCommentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0))
        );
        newCommentSectionPanelLayout.setVerticalGroup(
            newCommentSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(newCommentSectionPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(newCommentSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addCommentWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dummyAddCommentPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0)
                .addComponent(messagePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        attachmentsSectionPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        javax.swing.GroupLayout attachmentsSectionPanelLayout = new javax.swing.GroupLayout(attachmentsSectionPanel);
        attachmentsSectionPanel.setLayout(attachmentsSectionPanelLayout);
        attachmentsSectionPanelLayout.setHorizontalGroup(
            attachmentsSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(attachmentsSectionPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(attachmentsWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(dummyAttachmentsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        attachmentsSectionPanelLayout.setVerticalGroup(
            attachmentsSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(attachmentsSectionPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(attachmentsSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dummyAttachmentsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(attachmentsWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );

        subtasksSectionPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        org.openide.awt.Mnemonics.setLocalizedText(subtaskButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.subtaskButton.text")); // NOI18N
        subtaskButton.setFocusPainted(false);
        subtaskButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        subtaskButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subtaskButtonActionPerformed(evt);
            }
        });

        subtaskField.setColumns(15);

        javax.swing.GroupLayout subtasksSectionPanelLayout = new javax.swing.GroupLayout(subtasksSectionPanel);
        subtasksSectionPanel.setLayout(subtasksSectionPanelLayout);
        subtasksSectionPanelLayout.setHorizontalGroup(
            subtasksSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(subtasksSectionPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(subtaskWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addGroup(subtasksSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dummySubtaskPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(subtasksSectionPanelLayout.createSequentialGroup()
                        .addComponent(subtaskField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(subtaskButton)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(0, 0, 0))
        );
        subtasksSectionPanelLayout.setVerticalGroup(
            subtasksSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(subtasksSectionPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(subtasksSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(subtaskWarning, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(subtaskField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(subtaskButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dummySubtaskPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        commentsSectionPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        javax.swing.GroupLayout commentsSectionPanelLayout = new javax.swing.GroupLayout(commentsSectionPanel);
        commentsSectionPanel.setLayout(commentsSectionPanelLayout);
        commentsSectionPanelLayout.setHorizontalGroup(
            commentsSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(commentsSectionPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(dummyCommentsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        commentsSectionPanelLayout.setVerticalGroup(
            commentsSectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(commentsSectionPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(dummyCommentsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        privatePanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        privateDueDateLabel.setLabelFor(dummyPrivateDueDateField);
        org.openide.awt.Mnemonics.setLocalizedText(privateDueDateLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.privateDueDateLabel.text")); // NOI18N
        privateDueDateLabel.setToolTipText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.privateDueDateLabel.TTtext")); // NOI18N

        scheduleDateLabel.setLabelFor(dummyScheduleDateField);
        org.openide.awt.Mnemonics.setLocalizedText(scheduleDateLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.scheduleDateLabel.text")); // NOI18N
        scheduleDateLabel.setToolTipText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.scheduleDateLabel.TTtext")); // NOI18N

        privateEstimateLabel.setLabelFor(privateEstimateField);
        org.openide.awt.Mnemonics.setLocalizedText(privateEstimateLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.privateEstimateLabel.text")); // NOI18N
        privateEstimateLabel.setToolTipText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.privateEstimateLabel.TTtext")); // NOI18N

        privateEstimateField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(estimateFormatter));
        privateEstimateField.setText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.privateEstimateField.text")); // NOI18N
        privateEstimateField.setToolTipText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.privateEstimateField.toolTipText")); // NOI18N

        notesLabel.setLabelFor(privateNotesField);
        org.openide.awt.Mnemonics.setLocalizedText(notesLabel, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.notesLabel.text")); // NOI18N
        notesLabel.setToolTipText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.notesLabel.TTtext")); // NOI18N

        privateNotesField.setColumns(20);
        privateNotesField.setLineWrap(true);
        privateNotesField.setWrapStyleWord(true);
        privateNotesFieldScrollPane.setViewportView(privateNotesField);

        javax.swing.GroupLayout privatePanelLayout = new javax.swing.GroupLayout(privatePanel);
        privatePanel.setLayout(privatePanelLayout);
        privatePanelLayout.setHorizontalGroup(
            privatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(privatePanelLayout.createSequentialGroup()
                .addGroup(privatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(notesLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(privateDueDateLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(privatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(privateNotesFieldScrollPane)
                    .addGroup(privatePanelLayout.createSequentialGroup()
                        .addComponent(dummyPrivateDueDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(scheduleDateLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dummyScheduleDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(privateEstimateLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(privateEstimateField, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        privatePanelLayout.setVerticalGroup(
            privatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(privatePanelLayout.createSequentialGroup()
                .addGroup(privatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(privateDueDateLabel)
                    .addComponent(dummyPrivateDueDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scheduleDateLabel)
                    .addComponent(dummyScheduleDateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(privateEstimateLabel)
                    .addComponent(privateEstimateField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(privatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(notesLabel)
                    .addComponent(privateNotesFieldScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        headerPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

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

        separatorLabel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.openide.awt.Mnemonics.setLocalizedText(submitButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.submitButton.text")); // NOI18N
        submitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        separatorLabel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        org.openide.awt.Mnemonics.setLocalizedText(btnDeleteTask, org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.btnDeleteTask.text")); // NOI18N
        btnDeleteTask.setToolTipText(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.btnDeleteTask.TTtext")); // NOI18N
        btnDeleteTask.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteTaskActionPerformed(evt);
            }
        });

        separatorLabelDismiss.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout headerPanelLayout = new javax.swing.GroupLayout(headerPanel);
        headerPanel.setLayout(headerPanelLayout);
        headerPanelLayout.setHorizontalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(parentHeaderPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(headerPanelLayout.createSequentialGroup()
                        .addComponent(headerLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(headerField)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separatorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(showInBrowserButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separatorLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(submitButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separatorLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separatorLabelDismiss, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDeleteTask, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10))
        );
        headerPanelLayout.setVerticalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(headerPanelLayout.createSequentialGroup()
                        .addComponent(parentHeaderPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, 0)
                        .addGroup(headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(headerLabel)
                            .addComponent(headerField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(headerPanelLayout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addGroup(headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(showInBrowserButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(separatorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(submitButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(separatorLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(separatorLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnDeleteTask, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(separatorLabelDismiss, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(6, 6, 6))
        );

        mainScrollPane.setBorder(null);

        mainPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        attributesSection.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));
        attributesSection.setActions(getAttributesSectionActions());
        attributesSection.setContent(attributesSectionPanel);
        attributesSection.setLabel(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.attributesSection.label")); // NOI18N

        attachmentsSection.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));
        attachmentsSection.setActions(getAttachmentsSectionActions());
        attachmentsSection.setContent(attachmentsSectionPanel);
        attachmentsSection.setExpanded(false);
        attachmentsSection.setLabel(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.attachmentsLabel.text")); // NOI18N

        subtasksSection.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));
        subtasksSection.setContent(subtasksSectionPanel);
        subtasksSection.setExpanded(false);
        subtasksSection.setLabel(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.subtasksLabel.text")); // NOI18N

        privateSection.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));
        privateSection.setActions(getPrivateSectionActions());
        privateSection.setContent(privatePanel);
        privateSection.setLabel(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.privateAttributesSection.label")); // NOI18N

        newCommentSection.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));
        newCommentSection.setContent(newCommentSectionPanel);
        newCommentSection.setLabel(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.newCommentSection.label")); // NOI18N

        jPanel1.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));

        commentsSection.setBackground(javax.swing.UIManager.getDefaults().getColor("TextArea.background"));
        commentsSection.setActions(getCommentsSectionActions());
        commentsSection.setContent(commentsSectionPanel);
        commentsSection.setLabel(org.openide.util.NbBundle.getMessage(IssuePanel.class, "IssuePanel.commentsSection.label")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(commentsSection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(commentsSection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(subtasksSection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(privateSection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(attributesSection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(newCommentSection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(attachmentsSection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
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
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mainScrollPane)
                .addGap(0, 0, 0))
        );
    }// </editor-fold>//GEN-END:initComponents

    
    
    private void showInBrowserButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showInBrowserButtonActionPerformed
        TeamProject kp = issue.getRepository().getTeamProject();
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
    private void reloadButtonActionPerformed(java.awt.event.ActionEvent evt) {                                             
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
                            initStatusCombo(issue.getFieldValue(IssueField.STATUS));
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
    }
    
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
        resolutionWarning.setVisible(false);
        // Hide/show resolution combo
        RepositoryConfiguration rc = issue.getRepository().getRepositoryConfiguration(false);
        Object item = statusCombo.getSelectedItem();
        if (!(item instanceof TaskStatus)) {
            return;
        }
        if (statusCombo.getSelectedIndex() >= resolvedIndex) {
            String resolution = issue.getFieldValue(IssueField.RESOLUTION);
            if (resolution.isEmpty()) {
                resolution = STATUS_FIXED;
            }
            TaskResolution fixedResolution = ODCSUtil.getResolutionByValue(rc, resolution);
            resolutionCombo.setVisible(true);
            resolutionLabel.setVisible(true);
            resolutionWarning.setVisible(true);
            resolutionCombo.setSelectedItem(fixedResolution);
            storeFieldValue(IssueField.RESOLUTION, resolutionCombo);
            updateFieldDecorations(resolutionCombo, IssueField.RESOLUTION, resolutionWarning, resolutionLabel);
        } else {
            issue.setFieldValue(IssueField.RESOLUTION, ""); //NOI18N
            resolutionCombo.setVisible(false);
            resolutionLabel.setVisible(false);
            resolutionWarning.setVisible(false);
            updateFieldDecorations(resolutionCombo, IssueField.RESOLUTION, resolutionWarning, resolutionLabel);
            duplicateField.setVisible(false);
            duplicateButton.setVisible(false);
            duplicateLabel.setVisible(false);
            duplicateWarning.setVisible(false);
            updateFieldDecorations(duplicateField, IssueField.DUPLICATE, duplicateWarning, duplicateLabel);
        }
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
        if (!selectInCombo(componentCombo, component, false)) {
            if (issue.isNew() && componentCombo.getModel().getSize() > 0
                    || componentCombo.getModel().getSize() == 1) {
                componentCombo.setSelectedIndex(0);
            } else {
                componentCombo.setSelectedItem(null);
            }
        }
        if (!selectInCombo(releaseCombo, version, false)) {
            if (issue.isNew() && releaseCombo.getModel().getSize() > 0
                    || releaseCombo.getModel().getSize() == 1) {
                releaseCombo.setSelectedIndex(0);
            } else {
                releaseCombo.setSelectedItem(null);
            }
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
                ownerCombo.removeItem(NO_USER_PROFILE);
            } else {
                ownerCombo.removeItem(NO_USER_PROFILE);
                ownerCombo.addItem(NO_USER_PROFILE);
                ownerCombo.setSelectedItem(NO_USER_PROFILE);  
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
        findIssue(parentField, "IssuePanel.parentButton.message", "org.netbeans.modules.odcs.parentChooser", false); // NOI18N
    }//GEN-LAST:event_parentButtonActionPerformed

    private void subtaskButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subtaskButtonActionPerformed
        findIssue(subtaskField, "IssuePanel.subtaskButton.message", "org.netbeans.modules.odcs.subtaskChooser", true); // NOI18N
    }//GEN-LAST:event_subtaskButtonActionPerformed

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
        if(duplicate != null) {
            boolean shown = resolutionCombo.isVisible() && duplicate.equals(resolutionCombo.getSelectedItem());
            duplicateField.setVisible(shown);
            duplicateLabel.setVisible(shown);
            duplicateButton.setVisible(shown && duplicateField.isEditable());
            duplicateWarning.setVisible(shown);
            updateNoDuplicateId();
        }
    }//GEN-LAST:event_resolutionComboActionPerformed

    private void ccButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ccButtonActionPerformed
        String message = NbBundle.getMessage(IssuePanel.class, "IssuePanel.ccButton.message"); // NOI18N
        String users = ODCSUtil.getUsers(message, ccField.getText(), issue.getRepository());
        ccField.setText(users);
    }//GEN-LAST:event_ccButtonActionPerformed

    private void headerFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_headerFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_headerFieldActionPerformed

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

    @Messages({
        "# {0} - id", "IssuePanel.submitMessage=Submitting changes to issue {0}"
    })
    private void submitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitButtonActionPerformed
        final boolean isNew = issue.isNew();
        String submitMessage;
        if (isNew) {
            submitMessage = NbBundle.getMessage(IssuePanel.class, "IssuePanel.submitNewMessage"); // NOI18N
        } else {
            submitMessage = Bundle.IssuePanel_submitMessage(issue.getID());
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
                    }
                }
            }
        });
    }//GEN-LAST:event_submitButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel addCommentWarning;
    private org.netbeans.modules.bugtracking.commons.CollapsibleSectionPanel attachmentsSection;
    private javax.swing.JPanel attachmentsSectionPanel;
    private javax.swing.JLabel attachmentsWarning;
    private org.netbeans.modules.bugtracking.commons.CollapsibleSectionPanel attributesSection;
    private javax.swing.JPanel attributesSectionPanel;
    final org.netbeans.modules.bugtracking.commons.LinkButton btnDeleteTask = new org.netbeans.modules.bugtracking.commons.LinkButton();
    final org.netbeans.modules.bugtracking.commons.LinkButton cancelButton = new org.netbeans.modules.bugtracking.commons.LinkButton();
    final javax.swing.JButton ccButton = new javax.swing.JButton();
    final javax.swing.JTextField ccField = new javax.swing.JTextField();
    private javax.swing.JLabel ccLabel;
    private javax.swing.JLabel ccWarning;
    private org.netbeans.modules.bugtracking.commons.CollapsibleSectionPanel commentsSection;
    private javax.swing.JPanel commentsSectionPanel;
    final javax.swing.JComboBox componentCombo = new javax.swing.JComboBox();
    private javax.swing.JLabel componentLabel;
    private javax.swing.JLabel componentWarning;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JLabel descriptionWarning;
    private javax.swing.JLabel dueDateLabel;
    private javax.swing.JLabel dueDateWarning;
    private javax.swing.JPanel dummyAddCommentPanel;
    private javax.swing.JPanel dummyAttachmentsPanel;
    private javax.swing.JPanel dummyCommentsPanel;
    private javax.swing.JPanel dummyDescriptionPanel;
    private javax.swing.JTextField dummyDueDateField;
    private javax.swing.JTextField dummyPrivateDueDateField;
    private javax.swing.JTextField dummyScheduleDateField;
    private javax.swing.JPanel dummySubtaskPanel;
    final javax.swing.JButton duplicateButton = new javax.swing.JButton();
    final javax.swing.JTextField duplicateField = new javax.swing.JTextField();
    private javax.swing.JLabel duplicateLabel;
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
    private javax.swing.JPanel headerPanel;
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
    private javax.swing.JPanel mainPanel;
    private javax.swing.JScrollPane mainScrollPane;
    private javax.swing.JPanel messagePanel;
    final javax.swing.JTextField modifiedField = new javax.swing.JTextField();
    final javax.swing.JLabel modifiedLabel = new javax.swing.JLabel();
    private org.netbeans.modules.bugtracking.commons.SectionPanel newCommentSection;
    private javax.swing.JPanel newCommentSectionPanel;
    private javax.swing.JLabel notesLabel;
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
    private javax.swing.JLabel privateDueDateLabel;
    private javax.swing.JFormattedTextField privateEstimateField;
    private javax.swing.JLabel privateEstimateLabel;
    private javax.swing.JTextArea privateNotesField;
    private javax.swing.JScrollPane privateNotesFieldScrollPane;
    private javax.swing.JPanel privatePanel;
    private org.netbeans.modules.bugtracking.commons.CollapsibleSectionPanel privateSection;
    final javax.swing.JComboBox productCombo = new javax.swing.JComboBox();
    private javax.swing.JLabel productLabel;
    private javax.swing.JLabel productWarning;
    final org.netbeans.modules.bugtracking.commons.LinkButton refreshButton = new org.netbeans.modules.bugtracking.commons.LinkButton();
    final javax.swing.JComboBox releaseCombo = new javax.swing.JComboBox();
    private javax.swing.JLabel releaseLabel;
    private javax.swing.JLabel releaseWarning;
    final javax.swing.JTextField reportedField = new javax.swing.JTextField();
    final javax.swing.JLabel reportedLabel = new javax.swing.JLabel();
    private javax.swing.JComboBox resolutionCombo;
    private javax.swing.JLabel resolutionLabel;
    private javax.swing.JLabel resolutionWarning;
    private javax.swing.JLabel scheduleDateLabel;
    private javax.swing.JLabel separatorLabel;
    private javax.swing.JLabel separatorLabel4;
    private javax.swing.JLabel separatorLabel5;
    private javax.swing.JLabel separatorLabelDismiss;
    final javax.swing.JComboBox severityCombo = new javax.swing.JComboBox();
    private javax.swing.JLabel severityLabel;
    private javax.swing.JLabel severityWarning;
    final org.netbeans.modules.bugtracking.commons.LinkButton showInBrowserButton = new org.netbeans.modules.bugtracking.commons.LinkButton();
    final javax.swing.JComboBox statusCombo = new javax.swing.JComboBox();
    private javax.swing.JLabel statusLabel;
    private javax.swing.JLabel statusWarning;
    final org.netbeans.modules.bugtracking.commons.LinkButton submitButton = new org.netbeans.modules.bugtracking.commons.LinkButton();
    final javax.swing.JButton subtaskButton = new javax.swing.JButton();
    final javax.swing.JTextField subtaskField = new javax.swing.JTextField();
    private javax.swing.JLabel subtaskWarning;
    private org.netbeans.modules.bugtracking.commons.CollapsibleSectionPanel subtasksSection;
    private javax.swing.JPanel subtasksSectionPanel;
    final javax.swing.JTextField summaryField = new javax.swing.JTextField();
    private javax.swing.JLabel summaryLabel;
    private javax.swing.JLabel summaryWarning;
    // End of variables declaration//GEN-END:variables

    private void clearHighlights () {
        fieldsConflict.clear();
        fieldsIncoming.clear();
        fieldsLocal.clear();
    }

    private void updateIcon (JLabel label) {
        label.setToolTipText(null);
        label.setIcon(null);
        Map<IssueField, String> conflicts = tooltipsConflict.get(label);
        Map<IssueField, String> local = tooltipsLocal.get(label);
        Map<IssueField, String> remote = tooltipsIncoming.get(label);
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

    private void appendTooltips (StringBuilder sb, Map<IssueField, String> tooltips) {
        if (tooltips != null) {
            for (Map.Entry<IssueField, String> e : tooltips.entrySet()) {
                sb.append(e.getValue());
            }
        }
    }

    private void removeTooltips (JLabel label, IssueField field) {
        tooltipsConflict.removeTooltip(label, field);
        tooltipsIncoming.removeTooltip(label, field);
        tooltipsLocal.removeTooltip(label, field);
    }

    private String toReadable (String value, IssueField field) {
        if (field == IssueField.DUEDATE) {
            Date date = ODCSUtil.parseLongDate(value, new DateFormat[] { INPUT_DATE_FORMAT, READABLE_DATE_FORMAT });
            if (date != null) {
                value = READABLE_DATE_FORMAT.format(date);
            }
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
    
    @NbBundle.Messages("IssuePanel.reloadButton.text=Reload Attributes")
    private Action[] getAttributesSectionActions () {
        if (attributesSectionActions == null) {
            attributesSectionActions = new Action[] {
                new AbstractAction(Bundle.IssuePanel_reloadButton_text()) {
                
                    @Override
                    public void actionPerformed (ActionEvent e) {
                        reloadButtonActionPerformed(e);
                    }
                }
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
            List<Action> actions = new ArrayList<>();
            actions.add(new AbstractAction(Bundle.CTL_Attachment_action_create()) {
                
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
                });
            attachmentsSectionActions = actions.toArray(new Action[actions.size()]);
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
                        ODCS.getInstance().getBugtrackingFactory().addToCategory(issue.getRepository(), issue);
                    }
                }
            };
        }
        return privateSectionActions;
    }

    private static class TooltipsMap extends HashMap<JLabel, Map<IssueField, String>> {

        private void removeTooltip (JLabel label, IssueField field) {
            Map<IssueField, String> fields = get(label);
            if (fields != null) {
                fields.remove(field);
                if (fields.isEmpty()) {
                    remove(label);
                }
            }
        }

        private void addTooltip (JLabel label, IssueField field, String tooltip) {
            Map<IssueField, String> fields = get(label);
            if (fields == null) {
                fields = new LinkedHashMap<IssueField, String>(2);
                put(label, fields);
            }
            fields.put(field, tooltip);
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
