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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Document;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.mylyn.internal.jira.core.model.Component;
import org.eclipse.mylyn.internal.jira.core.model.IssueType;
import org.eclipse.mylyn.internal.jira.core.model.JiraFilter;
import org.eclipse.mylyn.internal.jira.core.model.JiraStatus;
import org.eclipse.mylyn.internal.jira.core.model.NamedFilter;
import org.eclipse.mylyn.internal.jira.core.model.Priority;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.Resolution;
import org.eclipse.mylyn.internal.jira.core.model.Version;
import org.eclipse.mylyn.internal.jira.core.model.filter.ComponentFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.ContentFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.DateRangeFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.EstimateVsActualFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.jira.core.model.filter.IssueTypeFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.PriorityFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.ProjectFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.ResolutionFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.StatusFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.UserFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.VersionFilter;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.spi.QueryNotifyListener;
import org.netbeans.modules.bugtracking.issuetable.Filter;
import org.netbeans.modules.bugtracking.issuetable.IssueTable;
import org.netbeans.modules.bugtracking.issuetable.QueryTableCellRenderer;
import org.netbeans.modules.bugtracking.ui.issue.cache.IssueCacheUtils;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.util.SaveQueryPanel;
import org.netbeans.modules.bugtracking.util.SaveQueryPanel.QueryNameValidator;
import org.netbeans.modules.jira.Jira;
import org.netbeans.modules.jira.JiraConfig;
import org.netbeans.modules.jira.JiraConnector;
import org.netbeans.modules.jira.commands.JiraCommand;
import org.netbeans.modules.jira.issue.NbJiraIssue;
import org.netbeans.modules.jira.kenai.KenaiRepository;
import org.netbeans.modules.jira.repository.JiraConfiguration;
import org.netbeans.modules.jira.repository.JiraRepository;
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
    private QueryPanel panel;

    private RequestProcessor rp = new RequestProcessor("Jira query", 1, true);  // NOI18N

    private final JiraRepository repository;
    protected JiraQuery query;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // NOI18N

    private QueryTask refreshTask;
    private final boolean modifiable;
    private final JiraFilter jiraFilter;
    private final IssueTable issueTable;
    private JiraQueryCellRenderer renderer;

    private UserSearch reporterUserSearch;
    private UserSearch assigneeUserSearch;
    
    private static SimpleDateFormat dateRangeDateFormat = new SimpleDateFormat("yyyy-MM-dd"); // NOI18N

    public QueryController(JiraRepository repository, JiraQuery query, FilterDefinition fd) {
        this(repository, query, fd, true);
    }

    public QueryController(JiraRepository repository, JiraQuery query, JiraFilter jiraFilter, boolean modifiable) {
        this.repository = repository;
        this.query = query;
        this.modifiable = modifiable;
        this.jiraFilter = jiraFilter;

        issueTable = new IssueTable(query, query.getColumnDescriptors());
        setupRenderer(issueTable);
        panel = new QueryPanel(issueTable.getComponent(), this, isNamedFilter(jiraFilter));
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
        panel.findIssuesButton.addActionListener(this);
        panel.cloneQueryButton.addActionListener(this);

        panel.idTextField.addActionListener(this);
        panel.projectList.addKeyListener(this);
        panel.typeList.addKeyListener(this);
        panel.statusList.addKeyListener(this);
        panel.resolutionList.addKeyListener(this);
        panel.priorityList.addKeyListener(this);
        panel.queryTextField.addActionListener(this);
        panel.assigneeTextField.addActionListener(this);
        panel.reporterTextField.addActionListener(this);
        panel.idTextField.getDocument().addDocumentListener(this);

        panel.createdFromTextField.getDocument().addDocumentListener(this);
        panel.createdToTextField.getDocument().addDocumentListener(this);
        panel.updatedFromTextField.getDocument().addDocumentListener(this);
        panel.updatedToTextField.getDocument().addDocumentListener(this);
        panel.dueFromTextField.getDocument().addDocumentListener(this);
        panel.dueToTextField.getDocument().addDocumentListener(this);

        panel.ratioMinTextField.getDocument().addDocumentListener(this);
        panel.ratioMaxTextField.getDocument().addDocumentListener(this);

        panel.filterComboBox.setModel(new DefaultComboBoxModel(issueTable.getDefinedFilters()));
                    
        if(query.isSaved()) {
            setAsSaved();
        }
        if(modifiable) {
            if(jiraFilter != null) {
                 assert jiraFilter instanceof FilterDefinition;
            }
            postPopulate((FilterDefinition) jiraFilter, false);
        }
    }


    private static boolean isNamedFilter(JiraFilter jiraFilter) {
        return jiraFilter instanceof NamedFilter;
    }

    private void setupRenderer(IssueTable issueTable) {
        renderer = new JiraQueryCellRenderer(query, issueTable, new QueryTableCellRenderer(query, issueTable));
        issueTable.setRenderer(renderer);
    }

    protected JiraFilter getJiraFilter() {
        if(modifiable) {
            return getFilterDefinition();
        } else {
            return jiraFilter;
        }
    }

    public FilterDefinition getFilterDefinition() {
        assert modifiable;
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
        List<Component> components = getValues(panel.componentsList);
        if(components.size() > 0) {
            fd.setComponentFilter(new ComponentFilter(components.toArray(new Component[components.size()])));
        }
        List<Version> versions = getValues(panel.fixForList);
        if(versions.size() > 0) {
            fd.setFixForVersionFilter(new VersionFilter(versions.toArray(new Version[versions.size()])));
        }
        versions = getValues(panel.affectsVersionList);
        if(versions.size() > 0) {
            fd.setReportedInVersionFilter(new VersionFilter(versions.toArray(new Version[versions.size()])));
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

        UserFilter userFilter = reporterUserSearch.getFilter();
        if(userFilter != null) {
            fd.setReportedByFilter(userFilter);
        }
        userFilter = assigneeUserSearch.getFilter();
        if(userFilter != null) {
            fd.setAssignedToFilter(userFilter);
        }

        Long min = getLongValue(panel.ratioMinTextField);
        Long max = getLongValue(panel.ratioMaxTextField);
        if(min != null || max != null) {
            EstimateVsActualFilter estimateFilter = new EstimateVsActualFilter(min != null ? min : 0, max != null ? max : 0);
            fd.setEstimateVsActualFilter(estimateFilter);
        }

        DateRangeFilter rf = getDateRangeFilter(panel.createdFromTextField, panel.createdToTextField);
        if(rf != null) {
            fd.setCreatedDateFilter(rf);
        }

        rf = getDateRangeFilter(panel.updatedFromTextField, panel.updatedToTextField);
        if(rf != null) {
            fd.setUpdatedDateFilter(rf);
        }

        rf = getDateRangeFilter(panel.dueFromTextField, panel.dueToTextField);
        if(rf != null) {
            fd.setDueDateFilter(rf);
        }

        return fd;
    }

    private Long getLongValue(JTextField txt) {
        try {
            return Long.parseLong(txt.getText().trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Date getDateValue(JTextField txt) {
        try {
            return dateRangeDateFormat.parse(txt.getText().trim());
        } catch (ParseException ex) {
            return null;
        }
    }

    private DateRangeFilter getDateRangeFilter(JTextField fromTxt, JTextField toTxt) {
        Date from = getDateValue(fromTxt);
        Date to = getDateValue(toTxt);
        if (from != null || to != null) {
            return new DateRangeFilter(from, to);
        }
        return null;
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

    private void populate(final FilterDefinition filterDefinition) {
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
                    if (jc.getProjects().length == 1) {
                        panel.setIssuePrefixText(jc.getProjects()[0].getKey() + "-"); //NOI18N
                    } else if (filterDefinition != null) {
                        ProjectFilter pf = filterDefinition.getProjectFilter();
                        if (pf != null && pf.getProjects().length == 1) {
                            panel.setIssuePrefixText(pf.getProjects()[0].getKey() + "-"); //NOI18N
                        }
                    }
                    populateList(panel.typeList, jc.getIssueTypes());
                    populateList(panel.statusList, jc.getStatuses());
                    populateList(panel.resolutionList, jc.getResolutions());
                    populateList(panel.priorityList, jc.getPriorities());
                    reporterUserSearch = new UserSearch(panel.reporterComboBox, panel.reporterTextField, "No Reporter");
                    assigneeUserSearch = new UserSearch(panel.assigneeComboBox, panel.assigneeTextField, "Unassigned");

                    if(filterDefinition != null && filterDefinition instanceof FilterDefinition) {
                        setFilterDefinition(filterDefinition);
                    }
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

    private void setFilterDefinition(FilterDefinition fd) {
        if(fd == null) {
            return;
        }
        // lists
        ProjectFilter pf = fd.getProjectFilter();
        if(pf != null) {
            setSelected(panel.projectList, pf.getProjects());
        }
        ComponentFilter compf = fd.getComponentFilter();
        if(compf != null) {
            setSelected(panel.componentsList, compf.getComponents());
        }
        VersionFilter vf = fd.getFixForVersionFilter();
        if(vf != null) {
            setSelected(panel.fixForList, vf.getVersions());
        }
        vf = fd.getReportedInVersionFilter();
        if(vf != null) {
            setSelected(panel.affectsVersionList, vf.getVersions());
        }
        IssueTypeFilter itf = fd.getIssueTypeFilter();
        if(itf != null) {
            setSelected(panel.typeList, itf.getIsueTypes());
        }
        StatusFilter sf = fd.getStatusFilter();
        if(sf != null) {
            setSelected(panel.statusList, sf.getStatuses());
        }
        ResolutionFilter rf = fd.getResolutionFilter();
        if(rf != null) {
            setSelected(panel.resolutionList, rf.getResolutions());
        }
        PriorityFilter prf = fd.getPriorityFilter();
        if(prf != null) {
            setSelected(panel.priorityList, prf.getPriorities());
        }
        // find by text
        ContentFilter cf = fd.getContentFilter();
        if (cf != null) {
            panel.queryTextField.setText(cf.getQueryString());
            panel.summaryCheckBox.setSelected(cf.isSearchingSummary());
            panel.descriptionCheckBox.setSelected(cf.isSearchingDescription());
            panel.commentsCheckBox.setSelected(cf.isSearchingComments());
            panel.environmentCheckBox.setSelected(cf.isSearchingEnvironment());
        }
        // user filters
        UserFilter uf = fd.getReportedByFilter();
        if (uf != null) {
            reporterUserSearch.setFilter(uf);
        }
        uf = fd.getAssignedToFilter();
        if (uf != null) {
            assigneeUserSearch.setFilter(uf);
        }

        EstimateVsActualFilter estimateFilter = fd.getEstimateVsActualFilter();
        if(estimateFilter != null) {
            panel.ratioMinTextField.setText(Long.toString(estimateFilter.getMinVariation()));
            panel.ratioMaxTextField.setText(Long.toString(estimateFilter.getMaxVariation()));
        }

        setDateRangeFilter((DateRangeFilter) fd.getCreatedDateFilter(), panel.createdFromTextField, panel.createdToTextField);
        setDateRangeFilter((DateRangeFilter) fd.getUpdatedDateFilter(), panel.updatedFromTextField, panel.updatedToTextField);
        setDateRangeFilter((DateRangeFilter) fd.getDueDateFilter(),     panel.dueFromTextField,     panel.dueToTextField);
       
    }

    private void setDateRangeFilter(DateRangeFilter dateRangeFilter, JTextField fromTxt, JTextField toTxt) {
        if(dateRangeFilter != null) {
            Date from = dateRangeFilter.getFromDate();
            Date to = dateRangeFilter.getToDate();
            fromTxt.setText(dateRangeDateFormat.format(from));
            toTxt.setText(dateRangeDateFormat.format(to));
        }
    }

    private void setSelected (JList list, Object[] selectedItems) {
        if(selectedItems != null) {
            List<Integer> toSelect = new ArrayList<Integer>();
            DefaultListModel model = (DefaultListModel) list.getModel();
            for (Object o : selectedItems) {
                if(o == null) continue;
                int idx = model.indexOf(o);
                if (idx > -1) {
                    toSelect.add(idx);
                } else {
                    // for whatever reason - component doesn't implement equals.
                    if(o instanceof Component) {
                        Component c = (Component) o;
                        for (int i = 0; i < model.getSize(); i++) {
                            Component mc = (Component) model.get(i);
                            if(mc != null && mc.getId().equals(c.getId())) {
                                toSelect.add(i);
                                break;
                            }
                        }
                    }
                }
            }
            int[] idx = new int[toSelect.size()];
            for (int i = 0; i < idx.length; i++) {
                idx[i] = toSelect.get(i);
            }
            list.setSelectedIndices(idx);
        }
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

    protected void setIssueCount(final int count) {
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

    protected void enableFields(boolean bl) {
        // set all non parameter fields
        panel.enableFields(bl);
        if(!modifiable) {
            // can't change the controllers data
            // so alwasy keep those fields disabled
            panel.modifyButton.setEnabled(false);
            panel.removeButton.setEnabled(false);
            panel.reloadAttributesButton.setEnabled(false);
        }
    }

    protected void disableProject() {
        panel.projectList.setEnabled(false);
        panel.projectLabel.setEnabled(false);
    }
    
    public void insertUpdate(DocumentEvent e) {
        documentChanged(e);
    }

    public void removeUpdate(DocumentEvent e) {
        documentChanged(e);
    }

    public void changedUpdate(DocumentEvent e) {
        documentChanged(e);
    }

    public void itemStateChanged(ItemEvent e) {
        fireDataChanged();
        if(e.getSource() == panel.filterComboBox) {
            onFilterChange((Filter)e.getItem());
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        if(e.getSource() == panel.projectList) {
            onProjectChanged(e);
        }
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
            onRefresh();
        } else if (e.getSource() == panel.gotoIssueButton) {
            onGotoIssue();
        } else if (e.getSource() == panel.searchButton) {
            onRefresh();
        } else if (e.getSource() == panel.saveChangesButton) {
            onSave(true);   // invoke refresh after save
        } else if (e.getSource() == panel.cancelChangesButton) {
            onCancelChanges();
        } else if (e.getSource() == panel.gotoIssueButton) {
            onGotoIssue();
        } else if (e.getSource() == panel.webButton) {
            onWeb();
        } else if (e.getSource() == panel.saveButton) {
            onSave(false); // do not refresh
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
        } else if (e.getSource() == panel.cloneQueryButton) {
            onCloneQuery();
        } else if (e.getSource() == panel.findIssuesButton) {
            onFindIssues();
        } else if (e.getSource() == panel.idTextField) {
            if(!panel.idTextField.getText().trim().equals("")) {                // NOI18N
                onGotoIssue();
            }
        } else if (e.getSource() == panel.idTextField ||
                   e.getSource() == panel.queryTextField ||
                   e.getSource() == panel.reporterTextField ||
                   e.getSource() == panel.assigneeTextField )
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
        if(e.getSource() == panel.projectList ||
           e.getSource() == panel.typeList ||
           e.getSource() == panel.statusList ||
           e.getSource() == panel.resolutionList ||
           e.getSource() == panel.priorityList)
        {
            onRefresh();
        }
    }

    private void onFilterChange(Filter filter) {
        query.setFilter(filter);
    }

    private void onSave(final boolean refresh) {
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
                }
                assert name != null;
                save(name, firstTime);

                if(refresh) {
                    onRefresh();
                }
            }
       });
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
        return SaveQueryPanel.show(v, new HelpCtx("org.netbeans.modules.jira.query.savePanel")); // NOI18N
    }

    private void save(String name, boolean firstTime) {
        query.setName(name);
        repository.saveQuery(query);
        query.setSaved(true); // XXX
        setAsSaved();
        if(!query.wasRun()) {
            onRefresh();
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

    /**
     * Returns a modified id entered by user.
     * e.g.: adds prefix, suffix or whatever
     * @param id pure id from the textfield
     * @return
     */
    protected String getIdTextField () {
        return panel.getIssuePrefixText() + panel.idTextField.getText().trim();
    }

    private void setAsSaved() {
        panel.setSaved(query.getDisplayName(), getLastRefresh());
        panel.setModifyVisible(false);
        panel.refreshCheckBox.setVisible(true);
    } 

    protected String getLastRefresh() throws MissingResourceException {
        long l = query.getLastRefresh();
        return l > 0 ?
            dateFormat.format(new Date(l)) :
            NbBundle.getMessage(QueryController.class, "LBL_Never"); // NOI18N
    }

    private boolean validateIssueKey (String key) {
        boolean retval = false;
        // TODO more sofisticated: e.g. with a JiraIssueFinder?
        try {
            Long.parseLong(key);
        } catch (NumberFormatException e) {
            // not a number, will not cause an InsufficientRightsException in mylyn
            retval = true;
        }
        if (!retval) {
            panel.lblIssueKeyWarning.setText(org.openide.util.NbBundle.getMessage(QueryPanel.class, "MSG_InvalidIssueKey", new Object[] {key})); //NOI18N
            panel.lblIssueKeyWarning.setVisible(true);
        }
        return retval;
    }

    private void documentChanged (DocumentEvent e) {
        final Document document = e.getDocument();
        panel.searchButton.setEnabled(true);
        panel.saveButton.setEnabled(true);
        panel.warningLabel.setVisible(false);
        panel.warningLabel.setText(""); // NOI18N
        if (document == panel.idTextField.getDocument()) {
            panel.lblIssueKeyWarning.setVisible(false);
        } else if (document == panel.createdFromTextField.getDocument()) {
            validateDateField(panel.createdFromTextField);
        } else if (document == panel.createdToTextField.getDocument()) {
            validateDateField(panel.createdToTextField);
        } else if (document == panel.updatedFromTextField.getDocument()) {
            validateDateField(panel.updatedFromTextField);
        } else if (document == panel.updatedToTextField.getDocument()) {
            validateDateField(panel.updatedToTextField);
        } else if (document == panel.dueFromTextField.getDocument()) {
            validateDateField(panel.dueFromTextField);
        } else if (document == panel.dueToTextField.getDocument()) {
            validateDateField(panel.dueToTextField);
        } else if (document == panel.ratioMaxTextField.getDocument()) {
            validateLongField(panel.ratioMaxTextField);
        } else if (document == panel.ratioMinTextField.getDocument()) {
            validateLongField(panel.ratioMinTextField);
        }

        fireDataChanged();
    }

    private void validateDateField(JTextField txt) {
        try {
            String str = txt.getText().trim();
            if(str.equals("")) {
                return;
            }
            dateRangeDateFormat.parse(str);
        } catch (ParseException ex) {
            panel.searchButton.setEnabled(false);
            panel.saveButton.setEnabled(false);
            panel.warningLabel.setVisible(true);
            panel.warningLabel.setText(NbBundle.getMessage(QueryPanel.class, "MSG_VALUE_MUST_BE_A_DATE")); // NOI18N
        }
    }

    private void validateLongField(JTextField txt) {
        String str = txt.getText().trim();
        if(str.equals("")) {
            return;
        }
        boolean isValid = true;
        try {
            long l = Long.parseLong(str);
            if(l < 0 || l > 100) {
                isValid = false;
            }
        } catch (NumberFormatException ex) {
            isValid = false;
        }
        if(!isValid) {
            panel.searchButton.setEnabled(false);
            panel.saveButton.setEnabled(false);
            panel.warningLabel.setVisible(true);
            panel.warningLabel.setText(NbBundle.getMessage(QueryPanel.class, "MSG_VALUE_MUST_BE_A_BETWEEN_1_100")); // NOI18N
        }
    }

    private void onGotoIssue() {
        final String key = getIdTextField();
        if(key == null || key.trim().equals("") || !validateIssueKey(key)) { //NOI18N
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
                    openIssue((NbJiraIssue) repository.getIssue(key.toUpperCase()));
                } finally {
                    handle.finish();
                }
            }
        });
        t[0].schedule(0);
    }

    protected void openIssue(NbJiraIssue issue) {
        if (issue != null) {
            issue.open();
        } else {
            // XXX nice message?
        }
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

    public void autoRefresh() {
        onRefresh(true);
    }

    public void onRefresh() {
        onRefresh(false);
    }

    private void onRefresh(final boolean autoRefresh) {
        if(refreshTask == null) {            
            refreshTask = new QueryTask();
        }
        refreshTask.post(autoRefresh);
    }    

    private void onModify() {
        panel.setModifyVisible(true);
    }

    private void onMarkSeen() {
        Jira.getInstance().getRequestProcessor().post(new Runnable() {
            public void run() {
                Issue[] issues = query.getIssues();
                for (Issue issue : issues) {
                    IssueCacheUtils.setSeen(issue, true);
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

    private void onProjectChanged(ListSelectionEvent e) {
        Object[] values =  panel.projectList.getSelectedValues();
        Project[] projects = null;
        if(values != null) {
            projects = new Project[values.length];
            for (int i = 0; i < values.length; i++) {
                if(values[i] instanceof Project) {
                    projects[i] = (Project) values[i];
                } else {
                    Jira.LOG.warning("project list item [" + values[i] + " has wrong type [" + values[i].getClass() + "]. Try to reload attributes." );
                }
            }
        }
        populateProjectDetails(projects);
    }

    private void populateProjectDetails(Project... projects) {
        if(projects == null || projects.length == 0) {
            return;
        }

        Set<Version> versions = new HashSet<Version>();
        Set<Component> components = new HashSet<Component>();
        for (Project p : projects) {
            Component[] cs = p.getComponents();
            if(cs != null) {
                for (Component c : cs) {
                    // for what ever reason - component doesn't implement equals!
                    boolean found = false;
                    for (Component knownComponent : components) {
                        if(knownComponent.getId().equals(c.getId())) {
                            found = true;
                            break;
                        }
                    }
                    if(!found) {
                        components.add(c);
                    }
                }
            }
            Version[] vs = p.getVersions();
            if(vs != null) {
                for (Version v : vs) {
                    versions.add(v);
                }
            }
        }

        Version[] versionsArray = versions.toArray(new Version[versions.size()]);
        Component[] componentsArray = components.toArray(new Component[components.size()]);
        populateList(panel.fixForList, versionsArray);
        populateList(panel.affectsVersionList, versionsArray);
        populateList(panel.componentsList, componentsArray);

        panel.fixForScrollPane.setVisible(versionsArray.length != 0);
        panel.fixForLabel.setVisible(versionsArray.length != 0);
        panel.affectsVersionsScrollPane.setVisible(versionsArray.length != 0);
        panel.affectsVersionsLabel.setVisible(versionsArray.length != 0);
        panel.componentsScrollPane.setVisible(componentsArray.length != 0);
        panel.componentsLabel.setVisible(componentsArray.length != 0);
            
        panel.byDetailsPanel.validate();
    }

    private void remove() {
        if (refreshTask != null) {
            refreshTask.cancel();
        }
        query.remove();
    }

    protected void onReloadAttributes() {
        if(modifiable) {
            postPopulate(getFilterDefinition(), true);
        }
    }

    private void onFindIssues() {
        Query.openNew(repository);
    }

    private void onCloneQuery() {
        FilterDefinition fd = getFilterDefinition();
        JiraQuery q = new JiraQuery(null, repository, fd, false, true);
        BugtrackingUtil.openQuery(q, repository, false);
    }

    void progress(String issueDesc) {
        if(refreshTask != null) {
            refreshTask.progress(issueDesc);
        }
    }

    private class QueryTask implements Runnable, Cancellable, QueryNotifyListener {
        private ProgressHandle handle;
        private int counter;
        private Task task;
        private boolean autoRefresh;

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
            QueryController.this.renderer.resetDefaultRowHeight();
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

        synchronized void progress(String issueDesc) {
            if(handle != null) {
                handle.progress(
                    NbBundle.getMessage(
                        QueryController.class, "LBL_RetrievingIssue", new Object[] {issueDesc}));
            }
        }

        public void executeQuery() {
            panel.setQueryRunning(true);
            try {
                query.refresh(getJiraFilter(), autoRefresh);
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
