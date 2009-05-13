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

package org.netbeans.modules.jira.query;

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
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.MissingResourceException;
import java.util.logging.Level;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.internal.jira.core.model.IssueType;
import org.eclipse.mylyn.internal.jira.core.model.JiraFilter;
import org.eclipse.mylyn.internal.jira.core.model.JiraStatus;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.internal.jira.core.model.filter.ContentFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.jira.core.model.filter.IssueTypeFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.PriorityFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.ProjectFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.ResolutionFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.StatusFilter;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.spi.Query.Filter;
import org.netbeans.modules.bugtracking.spi.QueryNotifyListener;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.jira.Jira;
import org.netbeans.modules.jira.JiraConfig;
import org.netbeans.modules.jira.JiraConnector;
import org.netbeans.modules.jira.commands.JiraCommand;
import org.netbeans.modules.jira.issue.NbJiraIssue;
import org.netbeans.modules.jira.repository.JiraConfiguration;
import org.netbeans.modules.jira.repository.JiraRepository;
import org.netbeans.modules.jira.util.JiraUtils;
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

    private RequestProcessor rp = new RequestProcessor("Jira query", 1, true);  // NOI18N
    private Task task;

    private final JiraRepository repository;
    protected JiraQuery query;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss, EEE MMM d yyyy"); // NOI18N
    private QueryTask searchTask;
    private QueryTask refreshTask;

    public QueryController(JiraRepository repository, JiraQuery query, JiraFilter jiraFilter) {
        this.repository = repository;
        this.query = query;
        
        panel = new QueryPanel(query.getTableComponent(), this);

        panel.projectList.addListSelectionListener(this);
        panel.filterComboBox.addItemListener(this);
        panel.searchButton.addActionListener(this);
        panel.refreshCheckBox.addActionListener(this);
        panel.saveChangesButton.addActionListener(this);
        panel.cancelChangesButton.addActionListener(this);
        panel.gotoIssueButton.addActionListener(this);
        panel.webButton.addActionListener(this);
        panel.saveButton.addActionListener(this);
        panel.refreshButton.addActionListener(this);
        panel.modifyButton.addActionListener(this);
        panel.seenButton.addActionListener(this);
        panel.removeButton.addActionListener(this);
        panel.reloadAttributesButton.addActionListener(this);
        panel.reporterTextField.addFocusListener(this);
        panel.assigneeTextField.addFocusListener(this);

        panel.idTextField.addActionListener(this);
        panel.projectList.addKeyListener(this);
        panel.typeList.addKeyListener(this);
        panel.statusList.addKeyListener(this);
        panel.resolutionList.addKeyListener(this);
        panel.priorityList.addKeyListener(this);
        panel.queryTextField.addActionListener(this);
        panel.assigneeTextField.addActionListener(this);
        panel.reporterTextField.addActionListener(this);

        if(query.isSaved()) {
            setAsSaved();
        }
        postPopulate((FilterDefinition) jiraFilter, false);
    }

    @Override
    public void opened() {
        boolean autoRefresh = JiraConfig.getInstance().getQueryAutoRefresh(query.getDisplayName());
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

    private void setIssueCount(final int count) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                panel.tableSummaryLabel.setText(
                        NbBundle.getMessage(
                            QueryController.class,
                            NbBundle.getMessage(QueryController.class, "LBL_MATCHINGISSUES"),                           // NOI18N
                            new Object[] { count }
                        )
                );
            }
        });
    }


    @Override
    public void closed() {
        onCancelChanges();
        if(task != null) {
            task.cancel();
        }
        if(query.isSaved()) {
            repository.stopRefreshing(query);
        }
    }

    protected void scheduleForRefresh() {
        if(query.isSaved()) {
            repository.scheduleForRefresh(query);
        }
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(JiraQuery.class);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void applyChanges() {
        
    }

    public FilterDefinition getFilterDefinition() {
        FilterDefinition fd = new FilterDefinition();

        // text search
        String text = panel.queryTextField.getText().trim();
        if(!text.equals("")) {                                                  // NOI18N
            fd.setContentFilter(new ContentFilter(
                    text,
                    panel.summaryCheckBox.isSelected(),
                    panel.descriptionCheckBox.isSelected(),
                    panel.commentsCheckBox.isSelected(),
                    panel.environmentCheckBox.isSelected()));
        }

        List<Project> projects = getValues(panel.projectList);
        if(projects.size() > 0) {
            fd.setProjectFilter(new ProjectFilter(projects.toArray(new Project[projects.size()])));
        }
        List<IssueType> types = getValues(panel.typeList);
        if(types.size() > 0) {
            fd.setIssueTypeFilter(new IssueTypeFilter(types.toArray(new IssueType[types.size()])));
        }
        List<JiraStatus> statuses = getValues(panel.statusList);
        if(statuses.size() > 0) {
            fd.setStatusFilter(new StatusFilter(statuses.toArray(new JiraStatus[statuses.size()])));
        }
        List<Resolution> resolutions = getValues(panel.resolutionList);
        if(resolutions.size() > 0) {
            fd.setResolutionFilter(new ResolutionFilter(resolutions.toArray(new Resolution[resolutions.size()])));
        }
        List<Priority> priorities = getValues(panel.priorityList);
        if(priorities.size() > 0) {
            fd.setPriorityFilter(new PriorityFilter(priorities.toArray(new Priority[priorities.size()])));
        }

        return fd;
    }

    private <T> List<T> getValues(JList list) {
        Object[] values = list.getSelectedValues();
        if(values == null || values.length == 0) {
            return Collections.emptyList();
        }
        List<T> l = new ArrayList<T>(values.length);
        for (Object o : values) {
            l.add((T) o);
        }
        return l;
    }

    private void postPopulate(final FilterDefinition filterDefinition, final boolean forceRefresh) {
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

        final String msgPopulating = NbBundle.getMessage(QueryController.class, "MSG_Populating");    // NOI18N
        final ProgressHandle handle = ProgressHandleFactory.createHandle(msgPopulating, c);
        panel.showRetrievingProgress(true, msgPopulating, !query.isSaved());
        t[0] = rp.post(new Runnable() {
            public void run() {
                handle.start();
                try {
                    if(forceRefresh) {
                        repository.refreshConfiguration();
                    }
                    populate(filterDefinition);
                } finally {
                    enableFields(true);
                    handle.finish();
                    panel.showRetrievingProgress(false, null, !query.isSaved());
                }
            }
        });
    }

    public void populate(final FilterDefinition filterDefinition) {
        if(Jira.LOG.isLoggable(Level.FINE)) {
            Jira.LOG.fine("Starting populate query controller" + (query.isSaved() ? " - " + query.getDisplayName() : "")); // NOI18N
        }
        try {
            JiraCommand cmd = new JiraCommand() {
                @Override
                public void execute() throws JiraException, CoreException, IOException, MalformedURLException {
                    JiraConfiguration jc = repository.getConfiguration();
                    if(jc == null) {
                        // XXX nice errro msg?
                        return;
                    }
                    
                    populateList(panel.projectList, jc.getProjects());
                    populateList(panel.typeList, jc.getIssueTypes());
                    populateList(panel.statusList, jc.getStatuses());
                    populateList(panel.resolutionList, jc.getResolutions());
                    populateList(panel.priorityList, jc.getPriorities());

                    if(filterDefinition != null && filterDefinition instanceof FilterDefinition) {
                        setFilterDefinition(filterDefinition);
                    }

                    panel.filterComboBox.setModel(new DefaultComboBoxModel(query.getFilters()));
//
//                    if(query.isSaved()) {
//                        final boolean autoRefresh = JiraConfig.getInstance().getQueryAutoRefresh(query.getDisplayName());
//                        panel.refreshCheckBox.setSelected(autoRefresh);
//                    }

//                    JiraConfiguration bc = repository.getConfiguration();
//                    if(bc == null) {
//                        // XXX nice errro msg?
//                        return;
//                    }
//                    productParameter.setParameterValues(toParameterValues(bc.getProducts()));
//                    if (panel.productList.getModel().getSize() > 0) {
//                        panel.productList.setSelectedIndex(0);
//                        populateProductDetails(((ParameterValue) panel.productList.getSelectedValue()).getValue());
//                    }
//                    severityParameter.setParameterValues(toParameterValues(bc.getSeverities()));
//                    statusParameter.setParameterValues(toParameterValues(bc.getStatusValues()));
//                    resolutionParameter.setParameterValues(toParameterValues(bc.getResolutions()));
//                    priorityParameter.setParameterValues(toParameterValues(bc.getPriorities()));
//                    changedFieldsParameter.setParameterValues(QueryParameter.PV_LAST_CHANGE);
//                    summaryParameter.setParameterValues(QueryParameter.PV_TEXT_SEARCH_VALUES);
//                    commentsParameter.setParameterValues(QueryParameter.PV_TEXT_SEARCH_VALUES);
//                    keywordsParameter.setParameterValues(QueryParameter.PV_KEYWORDS_VALUES);
//                    peopleParameter.setParameterValues(QueryParameter.PV_PEOPLE_VALUES);
//                    panel.changedToTextField.setText(CHANGED_NOW);
//
//                    // XXX
//                    if (urlParameters != null) {
//                        setParameters(urlParameters);
//                    }
//
//                    panel.filterComboBox.setModel(new DefaultComboBoxModel(query.getFilters()));
//
//                    if(query.isSaved()) {
//                        final boolean autoRefresh = JiraConfig.getInstance().getQueryAutoRefresh(query.getDisplayName());
//                        panel.refreshCheckBox.setSelected(autoRefresh);
//                    }
                }
            };
            repository.getExecutor().execute(cmd);
        } finally {
            if(Jira.LOG.isLoggable(Level.FINE)) {
                Jira.LOG.fine("Finnished populate query controller" + (query.isSaved() ? " - " + query.getDisplayName() : "")); // NOI18N
            }
        }
    }

    private void populateList(JList list, Object[] values) {
        DefaultListModel model = new DefaultListModel();
        for (Object v : values) {
            model.addElement(v);
        }
        list.setModel(model);
    }

    protected void enableFields(boolean bl) {
        // set all non parameter fields
        panel.enableFields(bl);        
    }

    public void disableProject() {
        panel.projectList.setEnabled(false);
        panel.projectLabel.setEnabled(false);
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
            onFilterChange((Query.Filter)e.getItem());
        }
    }

    public void valueChanged(ListSelectionEvent e) {        
        fireDataChanged();            // XXX do we need this ???
    }

    public void focusGained(FocusEvent e) {
//        if(panel.changedFromTextField.getText().equals("")) {                   // NOI18N
//            String lastChangeFrom = JiraConfig.getInstance().getLastChangeFrom();
//            panel.changedFromTextField.setText(lastChangeFrom);
//            panel.changedFromTextField.setSelectionStart(0);
//            panel.changedFromTextField.setSelectionEnd(lastChangeFrom.length());
//        }
    }

    public void focusLost(FocusEvent e) {
        // do nothing
    }

    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == panel.searchButton) {
            onSearch();
        } else if (e.getSource() == panel.gotoIssueButton) {
            onGotoIssue();
        } else if (e.getSource() == panel.searchButton) {
            onSearch();
        } else if (e.getSource() == panel.saveChangesButton) {
            onSave();
        } else if (e.getSource() == panel.cancelChangesButton) {
            onCancelChanges();
        } else if (e.getSource() == panel.gotoIssueButton) {
            onGotoIssue();
        } else if (e.getSource() == panel.webButton) {
            onWeb();
        } else if (e.getSource() == panel.saveButton) {
            onSave();
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
        } else if (e.getSource() == panel.reloadAttributesButton) {
            onReloadAttributes();
        } else if (e.getSource() == panel.idTextField) {
            if(!panel.idTextField.getText().trim().equals("")) {                // NOI18N
                onGotoIssue();
            }
        } else if (e.getSource() == panel.idTextField ||
                   e.getSource() == panel.queryTextField ||
                   e.getSource() == panel.reporterTextField ||
                   e.getSource() == panel.assigneeTextField )
        {
            onSearch();
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
        if(e.getSource() == panel.projectList ||
           e.getSource() == panel.typeList ||
           e.getSource() == panel.statusList ||
           e.getSource() == panel.resolutionList ||
           e.getSource() == panel.priorityList)
        {
            onSearch();
        }
    }

    private void onFilterChange(Query.Filter filter) {
        query.setFilter(filter);
    }

    private void onSave() {
       Jira.getInstance().getRequestProcessor().post(new Runnable() {
            public void run() {
                String name = query.getDisplayName();
                boolean firstTime = false;
                if(!query.isSaved()) {
                    firstTime = true;
                    name = getSaveName();
                    if(name == null) {
                        return;
                    }
                    panel.queryNameTextField.setText("");                       // NOI18N
                }
                assert name != null;
                save(name, firstTime);
            }
       });
    }

    private String getSaveName() {
        String name = null;
        if(JiraUtils.show(
                panel.savePanel,
                NbBundle.getMessage(QueryController.class, "LBL_SaveQuery"),    // NOI18N
                NbBundle.getMessage(QueryController.class, "LBL_Save"),         // NOI18N
                new HelpCtx("org.netbeans.modules.jira.query.savePanel")))  // NOI18N
        {
            name = panel.queryNameTextField.getText();
            if(name == null || name.trim().equals("")) { // NOI18N
                return null;
            }
            Query[] queries = repository.getQueries();
            for (Query q : queries) {
                if(q.getDisplayName().equals(name)) {
                    panel.saveErrorLabel.setVisible(true);
                    name = getSaveName();
                    panel.saveErrorLabel.setVisible(false);
                    break;
                }
            }
        } else {
            return null;
        }
        return name;
    }

    private void save(String name, boolean firstTime) {
        query.setName(name);
        repository.saveQuery(query);
        query.setSaved(true); // XXX
        setAsSaved();
        if(!query.wasRun()) {
            if (firstTime) {
                onSearch();
            } else {
                onRefresh();
            }
        }
    }

    private void onCancelChanges() {
        if(query.getDisplayName() != null) { // XXX need a better semantic - isSaved?
            // XXX
//            String urlParameters = JiraConfig.getInstance().getUrlParams(repository, query.getDisplayName());
//            if(urlParameters != null) {
//                setfilterDefinition(fil);
//            }
        }
        setAsSaved();
    }

    public void selectFilter(Filter filter) {
        if(filter != null) {
            panel.filterComboBox.setSelectedItem(filter);
        }
    }

    private void setAsSaved() {
        panel.setSaved(query.getDisplayName(), getLastRefresh());
        panel.setModifyVisible(false);
        panel.refreshCheckBox.setVisible(true);
    } 

    private String getLastRefresh() throws MissingResourceException {
        long l = query.getLastRefresh();
        return l > -1 ?
            dateFormat.format(new Date(l)) :
            NbBundle.getMessage(QueryController.class, "LBL_Never"); // NOI18N
    }

    private void onGotoIssue() {
        final String key = panel.idTextField.getText().trim();
        if(key == null || key.trim().equals("") ) {                               // NOI18N
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
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(QueryController.class, "MSG_Opening", new Object[] {key}), c); // NOI18N
        t[0] = Jira.getInstance().getRequestProcessor().create(new Runnable() {
            public void run() {
                handle.start();
                try {
                    Issue issue = repository.getIssue(key.toUpperCase()); // XXX always uppercase?
                    if (issue != null) {
                        issue.open();
                    } else {
                        // XXX nice message?
                    }
                } finally {
                    handle.finish();
                }
            }
        });
        t[0].schedule(0);
    }

    private void onWeb() {
        final String repoURL = repository.getTaskRepository().getRepositoryUrl() + "/secure/IssueNavigator.jspa"; // NOI18N //XXX need constants
        Jira.getInstance().getRequestProcessor().post(new Runnable() {
            public void run() {
                URL url;
                try {
                    url = new URL(repoURL);
                } catch (MalformedURLException ex) {
                    Jira.LOG.log(Level.SEVERE, null, ex);
                    return;
                }
                HtmlBrowser.URLDisplayer displayer = HtmlBrowser.URLDisplayer.getDefault ();
                if (displayer != null) {
                    displayer.showURL (url);
                } else {
                    // XXX nice error message?
                    Jira.LOG.warning("No URLDisplayer found.");             // NOI18N
                }
            }
        });
    }

    private void onSearch() {
        if(searchTask == null) {
            searchTask = new QueryTask() {
                public void executeQuery() {
                    try {
                        refreshIntern(false);
                    } finally {
                        
                    }
                }
            };
        }
        post(searchTask);
    }

    public void autoRefresh() {
        onRefresh(true);
    }

    public void onRefresh() {
        onRefresh(false);
    }

    private void onRefresh(final boolean auto) {
        if(refreshTask == null) {            
            refreshTask = new QueryTask() {
                public void executeQuery() {
                    panel.setQueryRunning(true);
                    try {
                        refreshIntern(auto);
                    } finally {
                        panel.setQueryRunning(false);
                        task = null;
                    }
                }
            };
        }
        post(refreshTask);
    }

    private void refreshIntern(boolean autoRefresh) {
        query.refresh(getFilterDefinition(), autoRefresh);
    }

    private void post(Runnable r) {
        if(task != null) {
            task.cancel();
        }
        task = rp.create(r);
        task.schedule(0);
    }

    private void onModify() {
        panel.setModifyVisible(true);
    }

    private void onMarkSeen() {
        Jira.getInstance().getRequestProcessor().post(new Runnable() {
            public void run() {
                Issue[] issues = query.getIssues();
                for (Issue issue : issues) {
                    try {
                        ((NbJiraIssue) issue).setSeen(true);
                    } catch (IOException ex) {
                        Jira.LOG.log(Level.SEVERE, null, ex);
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
            Jira.getInstance().getRequestProcessor().post(new Runnable() {
                public void run() {
                    remove();
                }
            });
        }
    }

    private void onAutoRefresh() {
        final boolean autoRefresh = panel.refreshCheckBox.isSelected();
        JiraConfig.getInstance().setQueryAutoRefresh(query.getDisplayName(), autoRefresh);
        logAutoRefreshEvent(autoRefresh);
        if(autoRefresh) {
            scheduleForRefresh();
        } else {
            repository.stopRefreshing(query);
        }
    }

    protected void logAutoRefreshEvent(boolean autoRefresh) {
        BugtrackingUtil.logAutoRefreshEvent(
            JiraConnector.getConnectorName(),
            query.getDisplayName(),
            false,
            autoRefresh
        );
    }

    private void remove() {
        if (task != null) {
            task.cancel();
        }
        query.remove();
    }

    private void setFilterDefinition(FilterDefinition fd) {
        if(fd == null) {
            return;
        }
        ProjectFilter pf = fd.getProjectFilter();
        if(pf != null) {
            Project[] projects = pf.getProjects();
            if(projects != null) {
                List<Integer> toSelect = new ArrayList<Integer>();
                DefaultListModel model = (DefaultListModel) panel.projectList.getModel();
                for (Project p : projects) {
                    int idx = model.indexOf(p);
                    if(idx > -1) {
                        toSelect.add(idx);
                    }
                }
                int[] idx = new int[toSelect.size()];
                for (int i = 0; i < idx.length; i++) {
                    idx[i] = toSelect.get(i);
                }
                panel.projectList.setSelectedIndices(idx);
            }
        }
        // XXX finish me
       
    }

    private void onReloadAttributes() {
        postPopulate(getFilterDefinition(), true);
    }

    private abstract class QueryTask implements Runnable, Cancellable, QueryNotifyListener {
        private ProgressHandle handle;
        private int counter;

        public QueryTask() {
            query.addNotifyListener(this);
        }

        private void startQuery() {
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

        private void finnishQuery() {
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

        public abstract void executeQuery();

        public void run() {
            startQuery();
            try {
                executeQuery();
            } finally {
                finnishQuery();
            }
        }

        public boolean cancel() {
            if(task != null) {
                task.cancel();
            }
            finnishQuery();
            return true;
        }

        public void notifyData(final Issue issue) {
            setIssueCount(++counter);
        }

        public void started() {
            counter = 0;
            setIssueCount(counter);
        }

        public void finished() { }

    }

}
