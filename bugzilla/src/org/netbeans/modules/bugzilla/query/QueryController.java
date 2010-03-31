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

package org.netbeans.modules.bugzilla.query;

import org.netbeans.modules.bugtracking.util.SaveQueryPanel;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.logging.Level;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.eclipse.core.runtime.CoreException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.spi.QueryNotifyListener;
import org.netbeans.modules.bugtracking.issuetable.Filter;
import org.netbeans.modules.bugtracking.issuetable.IssueTable;
import org.netbeans.modules.bugtracking.issuetable.QueryTableCellRenderer;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.util.SaveQueryPanel.QueryNameValidator;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.BugzillaConfig;
import org.netbeans.modules.bugzilla.BugzillaConnector;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.bugzilla.commands.BugzillaCommand;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue;
import org.netbeans.modules.bugzilla.kenai.KenaiRepository;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
import org.netbeans.modules.bugzilla.query.QueryParameter.CheckBoxParameter;
import org.netbeans.modules.bugzilla.query.QueryParameter.ComboParameter;
import org.netbeans.modules.bugzilla.query.QueryParameter.ListParameter;
import org.netbeans.modules.bugzilla.query.QueryParameter.ParameterValue;
import org.netbeans.modules.bugzilla.query.QueryParameter.TextFieldParameter;
import org.netbeans.modules.bugzilla.repository.BugzillaConfiguration;
import org.netbeans.modules.bugzilla.util.BugzillaConstants;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Cancellable;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Tomas Stupka
 */
public class QueryController extends BugtrackingController implements DocumentListener, ItemListener, ListSelectionListener, ActionListener, FocusListener, KeyListener {
    protected QueryPanel panel;

    private static final String CHANGED_NOW = "Now";                            // NOI18N

    private final ComboParameter summaryParameter;
    private final ComboParameter commentsParameter;
    private final ComboParameter whiteboardParameter;
    private final ComboParameter keywordsParameter;
    private final ComboParameter peopleParameter;
    private final ListParameter productParameter;
    private final ListParameter componentParameter;
    private final ListParameter versionParameter;
    private final ListParameter statusParameter;
    private final ListParameter resolutionParameter;
    private final ListParameter priorityParameter;
    private final ListParameter changedFieldsParameter;
    private final ListParameter severityParameter;
    private final ListParameter issueTypeParameter;
    private final ListParameter tmParameter;

    private final Map<String, QueryParameter> parameters;

    private RequestProcessor rp = new RequestProcessor("Bugzilla query", 1, true);  // NOI18N

    private final BugzillaRepository repository;
    protected BugzillaQuery query;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // NOI18N
    private QueryTask refreshTask;
    private final IssueTable issueTable;
    private final boolean isNetbeans;

    public QueryController(BugzillaRepository repository, BugzillaQuery query, String urlParameters, boolean urlDef) {
        this(repository, query, urlParameters, false, true);
    }

    public QueryController(BugzillaRepository repository, BugzillaQuery query, String urlParameters, boolean urlDef, boolean populate) {
        this.repository = repository;
        this.query = query;

        issueTable = new IssueTable(query, query.getColumnDescriptors());
        setupRenderer(issueTable);
        panel = new QueryPanel(issueTable.getComponent(), this);

        isNetbeans = BugzillaUtil.isNbRepository(repository);
        panel.setNBFieldsVisible(isNetbeans);

        panel.productList.addListSelectionListener(this);
        panel.filterComboBox.addItemListener(this);
        panel.searchButton.addActionListener(this);
        panel.refreshCheckBox.addActionListener(this);
        panel.keywordsButton.addActionListener(this);
        panel.saveChangesButton.addActionListener(this);
        panel.cancelChangesButton.addActionListener(this);
        panel.gotoIssueButton.addActionListener(this);
        panel.webButton.addActionListener(this);
        panel.saveButton.addActionListener(this);
        panel.urlToggleButton.addActionListener(this);
        panel.refreshButton.addActionListener(this);
        panel.modifyButton.addActionListener(this);
        panel.seenButton.addActionListener(this);
        panel.removeButton.addActionListener(this);
        panel.refreshConfigurationButton.addActionListener(this);
        panel.findIssuesButton.addActionListener(this);
        panel.cloneQueryButton.addActionListener(this);
        panel.changedFromTextField.addFocusListener(this);

        panel.idTextField.addActionListener(this);
        panel.productList.addKeyListener(this);
        panel.componentList.addKeyListener(this);
        panel.versionList.addKeyListener(this);
        panel.statusList.addKeyListener(this);
        panel.resolutionList.addKeyListener(this);
        panel.severityList.addKeyListener(this);
        panel.priorityList.addKeyListener(this);
        panel.changedList.addKeyListener(this);
        panel.tmList.addKeyListener(this);

        panel.summaryTextField.addActionListener(this);
        panel.commentTextField.addActionListener(this);
        panel.whiteboardTextField.addActionListener(this);
        panel.keywordsTextField.addActionListener(this);
        panel.peopleTextField.addActionListener(this);
        panel.changedFromTextField.addActionListener(this);
        panel.changedToTextField.addActionListener(this);
        panel.changedToTextField.addActionListener(this);

        // setup parameters
        parameters = new LinkedHashMap<String, QueryParameter>();
        summaryParameter = createQueryParameter(ComboParameter.class, panel.summaryComboBox, "short_desc_type");    // NOI18N
        commentsParameter = createQueryParameter(ComboParameter.class, panel.commentComboBox, "long_desc_type");    // NOI18N
        whiteboardParameter = createQueryParameter(ComboParameter.class, panel.whiteboardComboBox, "status_whiteboard_type"); // NOI18N
        keywordsParameter = createQueryParameter(ComboParameter.class, panel.keywordsComboBox, "keywords_type");    // NOI18N
        peopleParameter = createQueryParameter(ComboParameter.class, panel.peopleComboBox, "emailtype1");           // NOI18N
        productParameter = createQueryParameter(ListParameter.class, panel.productList, "product");                 // NOI18N
        componentParameter = createQueryParameter(ListParameter.class, panel.componentList, "component");           // NOI18N
        versionParameter = createQueryParameter(ListParameter.class, panel.versionList, "version");                 // NOI18N
        statusParameter = createQueryParameter(ListParameter.class, panel.statusList, "bug_status");                // NOI18N
        resolutionParameter = createQueryParameter(ListParameter.class, panel.resolutionList, "resolution");        // NOI18N
        priorityParameter = createQueryParameter(ListParameter.class, panel.priorityList, "priority");              // NOI18N
        changedFieldsParameter = createQueryParameter(ListParameter.class, panel.changedList, "chfield");           // NOI18N
        if(isNetbeans) {
            issueTypeParameter = createQueryParameter(ListParameter.class, panel.issueTypeList, "cf_bug_type");     // NOI18N
            tmParameter = createQueryParameter(ListParameter.class, panel.tmList, "target_milestone");       // NOI18N
            severityParameter = null;

        } else {
            severityParameter = createQueryParameter(ListParameter.class, panel.severityList, "bug_severity");      // NOI18N
            issueTypeParameter = null;
            tmParameter = null;
        }

        createQueryParameter(TextFieldParameter.class, panel.summaryTextField, "short_desc");                       // NOI18N
        createQueryParameter(TextFieldParameter.class, panel.commentTextField, "long_desc");                        // NOI18N
        createQueryParameter(TextFieldParameter.class, panel.whiteboardTextField, "status_whiteboard");             // NOI18N
        createQueryParameter(TextFieldParameter.class, panel.keywordsTextField, "keywords");                        // NOI18N
        createQueryParameter(TextFieldParameter.class, panel.peopleTextField, "email1");                            // NOI18N
        createQueryParameter(CheckBoxParameter.class, panel.bugAssigneeCheckBox, "emailassigned_to1");              // NOI18N
        createQueryParameter(CheckBoxParameter.class, panel.reporterCheckBox, "emailreporter1");                    // NOI18N
        createQueryParameter(CheckBoxParameter.class, panel.ccCheckBox, "emailcc1");                                // NOI18N
        createQueryParameter(CheckBoxParameter.class, panel.commenterCheckBox, "emaillongdesc1");                   // NOI18N
        createQueryParameter(TextFieldParameter.class, panel.changedFromTextField, "chfieldfrom");                  // NOI18N
        createQueryParameter(TextFieldParameter.class, panel.changedToTextField, "chfieldto");                      // NOI18N
        createQueryParameter(TextFieldParameter.class, panel.newValueTextField, "chfieldvalue");                    // NOI18N

        panel.filterComboBox.setModel(new DefaultComboBoxModel(issueTable.getDefinedFilters()));

        if(query.isSaved()) {
            setAsSaved();
        }
        if(urlDef) {
            panel.switchQueryFields(false);
            panel.urlTextField.setText(urlParameters);
        } else {
            postPopulate(urlParameters, false);
        }
    }

    private void setupRenderer(IssueTable issueTable) {
        BugzillaQueryCellRenderer renderer = new BugzillaQueryCellRenderer(new QueryTableCellRenderer(query, issueTable));
        issueTable.setRenderer(renderer);
    }

    @Override
    public void opened() {
        boolean autoRefresh = BugzillaConfig.getInstance().getQueryAutoRefresh(query.getDisplayName());
        if(autoRefresh) {
            scheduleForRefresh();
        }
        if(query.isSaved()) {
            setIssueCount(query.getSize()); // XXX this probably won't work
                                            // if the query is alredy open and
                                            // a refresh is invoked on kenai
            if(!query.wasRun()) {
                onRefresh();
            }
        }
    }

    @Override
    public void closed() {
        onCancelChanges();
        if(refreshTask != null) {
            refreshTask.cancel();
        }
        if(query.isSaved()) {
            if(!(query.getRepository() instanceof KenaiRepository)) {
                repository.stopRefreshing(query);
            }
        }
    }

    protected void scheduleForRefresh() {
        if(query.isSaved()) {
            repository.scheduleForRefresh(query);
        }
    }

    private <T extends QueryParameter> T createQueryParameter(Class<T> clazz, Component c, String parameter) {
        try {
            Constructor<T> constructor = clazz.getConstructor(c.getClass(), String.class);
            T t = constructor.newInstance(c, parameter);
            parameters.put(parameter, t);
            return t;
        } catch (Exception ex) {
            Bugzilla.LOG.log(Level.SEVERE, parameter, ex);
        }
        return null;
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(org.netbeans.modules.bugzilla.query.BugzillaQuery.class);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void applyChanges() {

    }

    public String getUrlParameters() {
        if(panel.urlPanel.isVisible()) {
            return panel.urlTextField.getText();
        } else {
            StringBuffer sb = new StringBuffer();
            for (QueryParameter p : parameters.values()) {
                sb.append(p.get());
            }
            return sb.toString();
        }
    }

    protected BugzillaRepository getRepository() {
        return repository;
    }

    protected void postPopulate(final String urlParameters, final boolean forceRefresh) {
        enableFields(false);

        final Task[] t = new Task[1];
        Cancellable c = new Cancellable() {
            public boolean cancel() {
                if(t[0] != null) {
                    return t[0].cancel();
                }
                return true;
            }
        };

        final String msgPopulating = NbBundle.getMessage(QueryController.class, "MSG_Populating", new Object[]{repository.getDisplayName()});    // NOI18N
        final ProgressHandle handle = ProgressHandleFactory.createHandle(msgPopulating, c);
        panel.showRetrievingProgress(true, msgPopulating, !query.isSaved());
        t[0] = rp.post(new Runnable() {
            public void run() {
                handle.start();
                try {
                    if(forceRefresh) {
                        repository.refreshConfiguration();
                    }
                    populate(urlParameters);
                } finally {
                    enableFields(true);
                    handle.finish();
                    panel.showRetrievingProgress(false, null, !query.isSaved());
                }
            }
        });
    }

    protected void populate(final String urlParameters) {
        if(Bugzilla.LOG.isLoggable(Level.FINE)) {
            Bugzilla.LOG.fine("Starting populate query controller" + (query.isSaved() ? " - " + query.getDisplayName() : "")); // NOI18N
        }
        try {
            BugzillaCommand cmd = new BugzillaCommand() {
                @Override
                public void execute() throws CoreException, IOException, MalformedURLException {
                    BugzillaConfiguration bc = repository.getConfiguration();
                    if(bc == null || !bc.isValid()) {
                        // XXX nice errro msg?
                        return;
                    }
                    productParameter.setParameterValues(toParameterValues(bc.getProducts()));
                    if(isNetbeans) {
                        issueTypeParameter.setParameterValues(toParameterValues(bc.getIssueTypes()));
                    } else {
                        severityParameter.setParameterValues(toParameterValues(bc.getSeverities()));
                    }
                    statusParameter.setParameterValues(toParameterValues(bc.getStatusValues()));
                    resolutionParameter.setParameterValues(toParameterValues(bc.getResolutions()));
                    priorityParameter.setParameterValues(toParameterValues(bc.getPriorities()));
                    changedFieldsParameter.setParameterValues(QueryParameter.PV_LAST_CHANGE);
                    summaryParameter.setParameterValues(QueryParameter.PV_TEXT_SEARCH_VALUES);
                    commentsParameter.setParameterValues(QueryParameter.PV_TEXT_SEARCH_VALUES);
                    whiteboardParameter.setParameterValues(QueryParameter.PV_TEXT_SEARCH_VALUES);
                    keywordsParameter.setParameterValues(QueryParameter.PV_KEYWORDS_VALUES);
                    peopleParameter.setParameterValues(QueryParameter.PV_PEOPLE_VALUES);
                    panel.changedToTextField.setText(CHANGED_NOW);

                    setParameters(urlParameters != null ? urlParameters : getDefaultParameters());

                    if(query.isSaved()) {
                        final boolean autoRefresh = BugzillaConfig.getInstance().getQueryAutoRefresh(query.getDisplayName());
                        panel.refreshCheckBox.setSelected(autoRefresh);
                    }
                }

            };
            repository.getExecutor().execute(cmd);
        } finally {
            if(Bugzilla.LOG.isLoggable(Level.FINE)) {
                Bugzilla.LOG.fine("Finnished populate query controller" + (query.isSaved() ? " - " + query.getDisplayName() : "")); // NOI18N
            }
        }
    }

    private String getDefaultParameters() {
        return BugzillaUtil.isNbRepository(repository) ? BugzillaConstants.DEFAULT_NB_STATUS_PARAMETERS : BugzillaConstants.DEFAULT_STATUS_PARAMETERS;
    }
    protected void enableFields(boolean bl) {
        // set all non parameter fields
        panel.enableFields(bl);
        // set the parameter fields
        for (Map.Entry<String, QueryParameter> e : parameters.entrySet()) {
            QueryParameter pv = parameters.get(e.getKey());
            pv.setEnabled(bl);
        }
    }

    protected void disableProduct() { // XXX whatever field
        productParameter.setAlwaysDisabled(true);
    }

    protected void selectFirstProduct() {
        if(panel.productList.getModel().getSize() > 0) {
            panel.productList.setSelectedIndex(0);
        }
    }

    public void insertUpdate(DocumentEvent e) {
        fireDataChanged();
    }

    public void removeUpdate(DocumentEvent e) {
        fireDataChanged();
    }

    public void changedUpdate(DocumentEvent e) {
        fireDataChanged();
    }

    public void itemStateChanged(ItemEvent e) {
        fireDataChanged();
        if(e.getSource() == panel.filterComboBox) {
            onFilterChange((Filter)e.getItem());
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        if(e.getSource() == panel.productList) {
            onProductChanged(e);
        }
        fireDataChanged();            // XXX do we need this ???
    }

    public void focusGained(FocusEvent e) {
        if(panel.changedFromTextField.getText().equals("")) {                   // NOI18N
            String lastChangeFrom = BugzillaConfig.getInstance().getLastChangeFrom();
            panel.changedFromTextField.setText(lastChangeFrom);
            panel.changedFromTextField.setSelectionStart(0);
            panel.changedFromTextField.setSelectionEnd(lastChangeFrom.length());
        }
    }

    public void focusLost(FocusEvent e) {
        // do nothing
    }

    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == panel.searchButton) {
            onRefresh();
        } else if (e.getSource() == panel.gotoIssueButton) {
            onGotoIssue();
        } else if (e.getSource() == panel.keywordsButton) {
            onKeywords();
        } else if (e.getSource() == panel.searchButton) {
            onRefresh();
        } else if (e.getSource() == panel.saveChangesButton) {
            onSave(true); // refresh
        } else if (e.getSource() == panel.cancelChangesButton) {
            onCancelChanges();
        } else if (e.getSource() == panel.gotoIssueButton) {
            onGotoIssue();
        } else if (e.getSource() == panel.webButton) {
            onWeb();
        } else if (e.getSource() == panel.saveButton) {
            onSave(false); // do not refresh
        } else if (e.getSource() == panel.urlToggleButton) {
            onDefineAs();
        } else if (e.getSource() == panel.refreshButton) {
            onRefresh();
        } else if (e.getSource() == panel.modifyButton) {
            onModify();
        } else if (e.getSource() == panel.seenButton) {
            onMarkSeen();
        } else if (e.getSource() == panel.removeButton) {
            onRemove();
        } else if (e.getSource() == panel.refreshCheckBox) {
            onAutoRefresh();
        } else if (e.getSource() == panel.refreshConfigurationButton) {
            onRefreshConfiguration();
        } else if (e.getSource() == panel.findIssuesButton) {
            onFindIssues();
        } else if (e.getSource() == panel.cloneQueryButton) {
            onCloneQuery();
        } else if (e.getSource() == panel.idTextField) {
            if(!panel.idTextField.getText().trim().equals("")) {                // NOI18N
                onGotoIssue();
            }
        } else if (e.getSource() == panel.idTextField ||
                   e.getSource() == panel.summaryTextField ||
                   e.getSource() == panel.commentTextField ||
                   e.getSource() == panel.keywordsTextField ||
                   e.getSource() == panel.peopleTextField ||
                   e.getSource() == panel.changedFromTextField ||
                   e.getSource() == panel.newValueTextField ||
                   e.getSource() == panel.changedToTextField)
        {
            onRefresh();
        }
    }

    public void keyTyped(KeyEvent e) {
        // do nothing
    }

    public void keyPressed(KeyEvent e) {
        // do nothing
    }

    public void keyReleased(KeyEvent e) {
        if(e.getKeyCode() != KeyEvent.VK_ENTER) {
            return;
        }
        if(e.getSource() == panel.productList ||
           e.getSource() == panel.componentList ||
           e.getSource() == panel.versionList ||
           e.getSource() == panel.statusList ||
           e.getSource() == panel.resolutionList ||
           e.getSource() == panel.priorityList ||
           e.getSource() == panel.changedList)
        {
            onRefresh();
        }
    }

    private void onFilterChange(Filter filter) {
        query.setFilter(filter);
    }

    private void onSave(final boolean refresh) {
       Bugzilla.getInstance().getRequestProcessor().post(new Runnable() {
            public void run() {
                Bugzilla.LOG.fine("on save start");
                String name = query.getDisplayName();
                if(!query.isSaved()) {
                    name = getSaveName();
                    if(name == null) {
                        return;
                    }
                }
                assert name != null;
                save(name);
                Bugzilla.LOG.fine("on save finnish");

                if(refresh) {
                    onRefresh();
                }
            }

       });
    }

    /**
     * Saves the query under the given name
     * @param name
     */
    private void save(String name) {
        Bugzilla.LOG.log(Level.FINE, "saving query '{0}'", new Object[]{name});
        query.setName(name);
        repository.saveQuery(query);
        query.setSaved(true); // XXX
        setAsSaved();
        if (!query.wasRun()) {
            Bugzilla.LOG.log(Level.FINE, "refreshing query '{0}' after save", new Object[]{name});
            onRefresh();
        }
        Bugzilla.LOG.log(Level.FINE, "query '{0}' saved", new Object[]{name});
    }

    private String getSaveName() {
        QueryNameValidator v = new QueryNameValidator() {
            @Override
            public String isValid(String name) {
                Query[] queries = repository.getQueries();
                for (Query q : queries) {
                    if(q.getDisplayName().equals(name)) {
                        return NbBundle.getMessage(QueryController.class, "MSG_SAME_NAME");
                    }
                }
                return null;
            }
        };
        return SaveQueryPanel.show(v, new HelpCtx("org.netbeans.modules.bugzilla.query.savePanel"));
    }

    private void onCancelChanges() {
        if(query.getDisplayName() != null) { // XXX need a better semantic - isSaved?
            String urlParameters = BugzillaConfig.getInstance().getUrlParams(repository, query.getDisplayName());
            if(urlParameters != null) {
                setParameters(urlParameters);
            }
        }
        setAsSaved();
    }

    public void selectFilter(Filter filter) {
        if(filter != null) {
            panel.filterComboBox.setSelectedItem(filter);

            // XXX this part should be handled in the issues table - move the filtercombo and the label over
            Issue[] issues = query.getIssues();
            int c = 0;
            if(issues != null) {
                for (Issue issue : issues) {
                    if(filter.accept(issue)) c++;
                }
            }
            setIssueCount(c);
        }
        issueTable.setFilter(filter);
    }

    private void setAsSaved() {
        panel.setSaved(query.getDisplayName(), getLastRefresh());
        panel.setModifyVisible(false);
        panel.refreshCheckBox.setVisible(true);
    }

    private String getLastRefresh() throws MissingResourceException {
        long l = query.getLastRefresh();
        return l > 0 ?
            dateFormat.format(new Date(l)) :
            NbBundle.getMessage(QueryController.class, "LBL_Never"); // NOI18N
    }

    private void onGotoIssue() {
        final String id = panel.idTextField.getText().trim();
        if(id == null || id.trim().equals("") ) {                               // NOI18N
            return;
        }

        final Task[] t = new Task[1];
        Cancellable c = new Cancellable() {
            public boolean cancel() {
                if(t[0] != null) {
                    return t[0].cancel();
                }
                return true;
            }
        };
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(QueryController.class, "MSG_Opening", new Object[] {id}), c); // NOI18N
        t[0] = Bugzilla.getInstance().getRequestProcessor().create(new Runnable() {
            public void run() {
                handle.start();
                try {
                    openIssue((BugzillaIssue)repository.getIssue(id));
                } finally {
                    handle.finish();
                }
            }
        });
        t[0].schedule(0);
    }

    protected void openIssue(BugzillaIssue issue) {
        if (issue != null) {
            issue.open();
        } else {
            // XXX nice message?
        }
    }

    private void onWeb() {
        String params = getUrlParameters();
        String repoURL = repository.getTaskRepository().getRepositoryUrl() + "/query.cgi?format=advanced"; // NOI18N //XXX need constants

        final String urlString = repoURL + (params != null && !params.equals("") ? params : ""); // NOI18N
        Bugzilla.getInstance().getRequestProcessor().post(new Runnable() {
            public void run() {
                URL url;
                try {
                    url = new URL(urlString);
                } catch (MalformedURLException ex) {
                    Bugzilla.LOG.log(Level.SEVERE, null, ex);
                    return;
                }
                HtmlBrowser.URLDisplayer displayer = HtmlBrowser.URLDisplayer.getDefault ();
                if (displayer != null) {
                    displayer.showURL (url);
                } else {
                    // XXX nice error message?
                    Bugzilla.LOG.warning("No URLDisplayer found.");             // NOI18N
                }
            }
        });
    }

    private void onProductChanged(ListSelectionEvent e) {
        Object[] values =  panel.productList.getSelectedValues();
        String[] products = null;
        if(values != null) {
            products = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                products[i] = ((ParameterValue) values[i]).getValue();
            }
        }
        populateProductDetails(products);
    }

    private void onDefineAs() {
        panel.switchQueryFields(panel.urlPanel.isVisible());
    }

    private void onKeywords() {
        String keywords = BugzillaUtil.getKeywords(NbBundle.getMessage(QueryController.class, "LBL_SelectKeywords"), panel.keywordsTextField.getText(), repository); // NOI18N
        if(keywords != null) {
            panel.keywordsTextField.setText(keywords);
        }
    }

    public void autoRefresh() {
        onRefresh(true);
    }

    public void onRefresh() {
        onRefresh(false);
    }

    private void onRefresh(final boolean auto) {
        if(refreshTask == null) {
            refreshTask = new QueryTask();
        } else {
            refreshTask.cancel();
        }
        refreshTask.post(auto);
    }

    private void onModify() {
        panel.setModifyVisible(true);
    }

    private void onMarkSeen() {
        Bugzilla.getInstance().getRequestProcessor().post(new Runnable() {
            public void run() {
                Issue[] issues = query.getIssues();
                for (Issue issue : issues) {
                    try {
                        ((BugzillaIssue) issue).setSeen(true);
                    } catch (IOException ex) {
                        Bugzilla.LOG.log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }

    private void onRemove() {
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
            NbBundle.getMessage(QueryController.class, "MSG_RemoveQuery", new Object[] { query.getDisplayName() }), // NOI18N
            NbBundle.getMessage(QueryController.class, "CTL_RemoveQuery"),      // NOI18N
            NotifyDescriptor.OK_CANCEL_OPTION);

        if(DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {
            Bugzilla.getInstance().getRequestProcessor().post(new Runnable() {
                public void run() {
                    remove();
                }
            });
        }
    }

    private void onFindIssues() {
        Query.openNew(repository);
    }

    private void onCloneQuery() {
        String p = getUrlParameters();
        BugzillaQuery q = new BugzillaQuery(null, getRepository(), p, false, false, true);
        BugtrackingUtil.openQuery(q, getRepository(), false);
    }

    private void onAutoRefresh() {
        final boolean autoRefresh = panel.refreshCheckBox.isSelected();
        BugzillaConfig.getInstance().setQueryAutoRefresh(query.getDisplayName(), autoRefresh);
        logAutoRefreshEvent(autoRefresh);
        if(autoRefresh) {
            scheduleForRefresh();
        } else {
            repository.stopRefreshing(query);
        }
    }

    protected void logAutoRefreshEvent(boolean autoRefresh) {
        BugtrackingUtil.logAutoRefreshEvent(
            BugzillaConnector.getConnectorName(),
            query.getDisplayName(),
            false,
            autoRefresh
        );
    }


    private void onRefreshConfiguration() {
        postPopulate(getUrlParameters(), true);
    }

    private void remove() {
        if (refreshTask != null) {
            refreshTask.cancel();
        }
        query.remove();
    }

    private void populateProductDetails(String... products) {
        BugzillaConfiguration bc = repository.getConfiguration();
        if(bc == null || !bc.isValid()) {
            // XXX nice errro msg?
            return;
        }
        if(products == null || products.length == 0) {
            products = new String[] {null};
        }

        List<String> newComponents = new ArrayList<String>();
        List<String> newVersions = new ArrayList<String>();
        List<String> newTargetMilestone = new ArrayList<String>();
        for (String p : products) {
            List<String> productComponents = bc.getComponents(p);
            for (String c : productComponents) {
                if(!newComponents.contains(c)) {
                    newComponents.add(c);
                }
            }
            List<String> productVersions = bc.getVersions(p);
            for (String c : productVersions) {
                if(!newVersions.contains(c)) {
                    newVersions.add(c);
                }
            }
            if(isNetbeans) {
                List<String> targetMilestone = bc.getTargetMilestones(p);
                for (String c : targetMilestone) {
                    if(!newTargetMilestone.contains(c)) {
                        newTargetMilestone.add(c);
                    }
                }
            }
        }

        componentParameter.setParameterValues(toParameterValues(newComponents));
        versionParameter.setParameterValues(toParameterValues(newVersions));
        if(isNetbeans) {
            tmParameter.setParameterValues(toParameterValues(newTargetMilestone));
        }
    }

    private List<ParameterValue> toParameterValues(List<String> values) {
        List<ParameterValue> ret = new ArrayList<ParameterValue>(values.size());
        for (String v : values) {
            ret.add(new ParameterValue(v, v));
        }
        return ret;
    }

    private void setParameters(String urlParameters) {
        if(urlParameters == null) {
            return;
        }
        String[] params = urlParameters.split("&"); // NOI18N
        if(params == null || params.length == 0) return;
        Map<String, List<ParameterValue>> normalizedParams = new HashMap<String, List<ParameterValue>>();
        for (String p : params) {
            int idx = p.indexOf("="); // NOI18N
            if(idx > -1) {
                String parameter = p.substring(0, idx);
                String value = p.substring(idx + 1, p.length());

                ParameterValue pv = new ParameterValue(value, value);
                List<ParameterValue> values = normalizedParams.get(parameter);
                if(values == null) {
                    values = new ArrayList<ParameterValue>();
                    normalizedParams.put(parameter, values);
                }
                values.add(pv);
            } else {
                // XXX warning!!
            }
        }

        List<ParameterValue> componentPV = null;
        List<ParameterValue> versionPV = null;
        for (Map.Entry<String, List<ParameterValue>> e : normalizedParams.entrySet()) {
            QueryParameter pv = parameters.get(e.getKey());
            if(pv != null) {
                if(pv == componentParameter) {
                    componentPV = e.getValue();
                } else if(pv == versionParameter) {
                    versionPV = e.getValue();
                } else {
                    List<ParameterValue> pvs = e.getValue();
                    pv.setValues(pvs.toArray(new ParameterValue[pvs.size()]));
                }
            }
        }
        setDependentParameter(componentParameter, componentPV);
        setDependentParameter(versionParameter, versionPV);
    }

    private void setDependentParameter(QueryParameter p, List<ParameterValue> values) {
        if(values != null) {
            p.setValues(values.toArray(new ParameterValue[values.size()]));
        }
    }

    private void setIssueCount(final int count) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                String msg =
                    count == 1 ?
                        NbBundle.getMessage(QueryController.class, "LBL_MatchingIssue", new Object[] {count}) : // NOI18N
                        NbBundle.getMessage(QueryController.class, "LBL_MatchingIssues", new Object[] {count}); // NOI18N
                panel.tableSummaryLabel.setText(msg);
            }
        });
    }

    boolean isUrlDefined() {
        return panel.urlPanel.isVisible();
    }

    void switchToDeterminateProgress(long issuesCount) {
        if(refreshTask != null) {
            refreshTask.switchToDeterminateProgress(issuesCount);
        }
    }

    void addProgressUnit(String issueDesc) {
        if(refreshTask != null) {
            refreshTask.addProgressUnit(issueDesc);
        }
    }

    private class QueryTask implements Runnable, Cancellable, QueryNotifyListener {
        private ProgressHandle handle;
        private Task task;
        private int counter;
        private boolean autoRefresh;
        private long progressMaxWorkunits;
        private int progressWorkunits;

        public QueryTask() {
            query.addNotifyListener(this);
        }

        private synchronized void startQuery() {
            enableFields(false);
            handle = ProgressHandleFactory.createHandle(
                    NbBundle.getMessage(
                        QueryController.class,
                        "MSG_SearchingQuery",                                       // NOI18N
                        new Object[] {
                            query.getDisplayName() != null ?
                                query.getDisplayName() :
                                repository.getDisplayName()}),
                    this);
            panel.showSearchingProgress(true, NbBundle.getMessage(QueryController.class, "MSG_Searching")); // NOI18N
            handle.start();
        }

        private synchronized void finnishQuery() {
            task = null;
            if(handle != null) {
                handle.finish();
                handle = null;
            }
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    panel.setQueryRunning(false);
                    panel.setLastRefresh(getLastRefresh());
                    panel.showNoContentPanel(false);
                    enableFields(true);
                }
            });
        }

        synchronized void switchToDeterminateProgress(long progressMaxWorkunits) {
            if(handle != null) {
                handle.switchToDeterminate((int) progressMaxWorkunits);
                this.progressMaxWorkunits = progressMaxWorkunits;
                this.progressWorkunits = 0;
            }
        }

        synchronized void addProgressUnit(String issueDesc) {
            if(handle != null && progressWorkunits < progressMaxWorkunits) {
                handle.progress(
                    NbBundle.getMessage(
                        QueryController.class, "LBL_RetrievingIssue", new Object[] {issueDesc}),
                    ++progressWorkunits);
            }
        }

        public void executeQuery() {
            panel.setQueryRunning(true);
            // XXX isn't persistent and should be merged with refresh
            String lastChageFrom = panel.changedFromTextField.getText().trim();
            if(lastChageFrom != null && !lastChageFrom.equals("")) {    // NOI18N
                BugzillaConfig.getInstance().setLastChangeFrom(lastChageFrom);
            }
            try {
                if (panel.urlPanel.isVisible()) {
                    // XXX check url format etc...
                    // XXX what if there is a different host in queries repository as in the url?
                    query.refresh(panel.urlTextField.getText(), autoRefresh);
                } else {
                    query.refresh(getUrlParameters(), autoRefresh);
                }
            } finally {
                panel.setQueryRunning(false);
                task = null;
            }

        }

        public void run() {
            startQuery();
            try {
                executeQuery();
            } finally {
                finnishQuery();
            }
        }

        synchronized void post(boolean autoRefresh) {
            if(task != null) {
                task.cancel();
            }
            task = rp.create(this);
            this.autoRefresh = autoRefresh;
            task.schedule(0);
        }

        public boolean cancel() {
            if(task != null) {
                task.cancel();
                finnishQuery();
            }
            return true;
        }

        public void notifyData(final Issue issue) {
            if(!query.contains(issue)) {
                // XXX this is quite ugly - the query notifies an archoived issue
                // but it doesn't "contain" it!
                return;
            }
            setIssueCount(++counter);
            if(counter == 1) {
                panel.showNoContentPanel(false);
            }
        }

        public void started() {
            counter = 0;
            setIssueCount(counter);
        }

        public void finished() { }
    }

}
