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

package org.netbeans.modules.odcs.tasks.query;

import com.tasktop.c2c.server.common.service.domain.criteria.Criteria;
import com.tasktop.c2c.server.common.service.domain.criteria.CriteriaBuilder;
import com.tasktop.c2c.server.common.service.domain.criteria.CriteriaParser;
import com.tasktop.c2c.server.tasks.domain.Iteration;
import com.tasktop.c2c.server.tasks.domain.Milestone;
import com.tasktop.c2c.server.tasks.domain.Product;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import org.netbeans.modules.bugtracking.util.SaveQueryPanel;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.api.Util;
import org.netbeans.modules.bugtracking.issuetable.Filter;
import org.netbeans.modules.bugtracking.issuetable.IssueTable;
import org.netbeans.modules.bugtracking.issuetable.QueryTableCellRenderer;
import org.netbeans.modules.bugtracking.team.spi.TeamProject;
import org.netbeans.modules.bugtracking.spi.QueryController;
import org.netbeans.modules.bugtracking.spi.QueryController.QueryMode;
import org.netbeans.modules.bugtracking.util.*;
import org.netbeans.modules.bugtracking.util.SaveQueryPanel.QueryNameValidator;
import org.netbeans.modules.odcs.tasks.ODCS;
import org.netbeans.modules.odcs.tasks.ODCSConfig;
import org.netbeans.modules.odcs.tasks.ODCSConnector;
import org.netbeans.modules.odcs.tasks.issue.ODCSIssue;
import org.netbeans.modules.odcs.tasks.query.QueryParameters.ByDateParameter;
import org.netbeans.modules.odcs.tasks.query.QueryParameters.Parameter;
import org.netbeans.modules.odcs.tasks.repository.ODCSRepository;
import org.netbeans.modules.odcs.tasks.util.ODCSUtil;
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
public class ODCSQueryController extends QueryController implements ItemListener, ListSelectionListener, ActionListener, FocusListener, KeyListener {

    protected QueryPanel panel;

    private RequestProcessor rp = new RequestProcessor("ODCS query", 1, true);  // NOI18N

    private final ODCSRepository repository;
    protected ODCSQuery query;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // NOI18N
    private QueryTask refreshTask;

    private final Object REFRESH_LOCK = new Object();
    private final Object CRITERIA_LOCK = new Object();
    private final Semaphore querySemaphore = new Semaphore(1);
    
    private final IssueTable<ODCSQuery> issueTable;
    private boolean modifiable;
    private Criteria criteria;
    private Criteria originalCriteria;
    private final QueryParameters parameters;
    private boolean populated = false;
        
    ODCSQueryController(ODCSRepository repository, ODCSQuery query, Criteria criteria, boolean modifiable) {
        this.repository = repository;
        this.query = query;
        this.modifiable = modifiable;
        this.criteria = criteria;
        
        issueTable = new IssueTable<ODCSQuery>(ODCSUtil.getRepository(repository), query, query.getColumnDescriptors(), false);
        setupRenderer(issueTable);
        panel = new QueryPanel(issueTable.getComponent());

        panel.productList.addListSelectionListener(this);
        panel.filterComboBox.addItemListener(this);
        panel.searchButton.addActionListener(this);
        panel.saveChangesButton.addActionListener(this);
        panel.cancelChangesButton.addActionListener(this);
        panel.gotoIssueButton.addActionListener(this);
        panel.webButton.addActionListener(this);
        panel.saveButton.addActionListener(this);
        panel.refreshButton.addActionListener(this);
        panel.modifyButton.addActionListener(this);
        panel.seenButton.addActionListener(this);
        panel.removeButton.addActionListener(this);
        panel.refreshConfigurationButton.addActionListener(this);
        panel.findIssuesButton.addActionListener(this);
        panel.cloneQueryButton.addActionListener(this);

        panel.idTextField.addActionListener(this);
        panel.productList.addKeyListener(this);
        panel.componentList.addKeyListener(this);
        panel.releaseList.addKeyListener(this);
        panel.statusList.addKeyListener(this);
        panel.resolutionList.addKeyListener(this);
        panel.severityList.addKeyListener(this);
        panel.priorityList.addKeyListener(this);
        panel.iterationList.addKeyListener(this);

        panel.byTextTextField.addActionListener(this);

        panel.endTextField.addFocusListener(this);
        panel.startTextField.addFocusListener(this);
        
        // setup parameters
        parameters = new QueryParameters();
        
        parameters.createParameter(QueryParameters.Column.PRODUCT, panel.productList);  
        parameters.createParameter(QueryParameters.Column.COMPONENT, panel.componentList);
        parameters.createParameter(QueryParameters.Column.RELEASE, panel.releaseList);    
        parameters.createParameter(QueryParameters.Column.ITERATION, panel.iterationList);
        parameters.createParameter(QueryParameters.Column.TASK_TYPE, panel.issueTypeList);
        parameters.createParameter(QueryParameters.Column.PRIORITY, panel.priorityList);  
        parameters.createParameter(QueryParameters.Column.SEVERITY, panel.severityList);  
        parameters.createParameter(QueryParameters.Column.STATUS, panel.statusList);      
        parameters.createParameter(QueryParameters.Column.RESOLUTION, panel.resolutionList);
        parameters.createParameter(QueryParameters.Column.KEYWORDS, panel.keywordsList);                   
        
        parameters.createByTextParameter(panel.byTextTextField, panel.searchBySummaryCheckBox, panel.searchByDescriptionCheckBox);  
        parameters.createByPeopleParameter(panel.userList, panel.creatorCheckBox, panel.ownerCheckBox, panel.commenterCheckBox, panel.ccCheckBox);                   
        parameters.createByDateParameter(panel.byDateComboBox, panel.startTextField, panel.endTextField);
        
        panel.filterComboBox.setModel(new DefaultComboBoxModel(issueTable.getDefinedFilters()));

        setEndNow();
        
        if(query.isSaved()) {
            setAsSaved();
        }
        if (modifiable) {
            querySemaphore.acquireUninterruptibly();
            postPopulate(false);
        } else {
            hideModificationFields();
            populated = true;
        }
    }

    private void setupRenderer(IssueTable issueTable) {
        QueryCellRenderer renderer = new QueryCellRenderer(query, issueTable, (QueryTableCellRenderer)issueTable.getRenderer());
        issueTable.setRenderer(renderer);
    }

    @Override
    public void opened() {
        boolean autoRefresh = ODCSConfig.getInstance().getQueryAutoRefresh(query.getDisplayName());
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
        synchronized(REFRESH_LOCK) {
            if(refreshTask != null) {
                refreshTask.cancel();
            }
        }
        // XXX
//        if(query.isSaved()) {
//            if(!(query.getRepository() instanceof KenaiRepository)) {
//                repository.stopRefreshing(query);
//            }
//        }
    }

    protected void scheduleForRefresh() {
        // XXX
//        if(query.isSaved()) {
//            repository.scheduleForRefresh(query);
//        }
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.odcs.tasks.query.ODCSQueryController"); // NOI18N
    }

    @Override
    public void setMode(QueryMode mode) {
        switch(mode) {
            case EDIT:
                onModify();
                break;            
            case SHOW_ALL:
                onCancelChanges();
                selectFilter(issueTable.getAllFilter());
                break;
            case SHOW_NEW_OR_CHANGED:
                onCancelChanges();
                selectFilter(issueTable.getNewOrChangedFilter());
                break;
            default: 
                throw new IllegalStateException("Unsupported mode " + mode);
        }

    }
        
    protected ODCSRepository getRepository() {
        return repository;
    }

    private void postPopulate(final boolean forceRefresh) {

        final Task[] t = new Task[1];
        Cancellable c = new Cancellable() {
            @Override
            public boolean cancel() {
                if(t[0] != null) {
                    return t[0].cancel();
                }
                return true;
            }
        };

        final String msgPopulating = NbBundle.getMessage(ODCSQueryController.class, "MSG_Populating", new Object[]{repository.getDisplayName()});    // NOI18N
        final ProgressHandle handle = ProgressHandleFactory.createHandle(msgPopulating, c);

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                enableFields(false);
                panel.showRetrievingProgress(true, msgPopulating, !query.isSaved());
                handle.start();
            }
        });

        t[0] = rp.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if(forceRefresh) {
                        repository.refreshConfiguration();
                    }
                    logPopulate("Starting populate query controller{0}"); // NOI18N
                    repository.ensureCredentials();
                    final RepositoryConfiguration rc = repository.getRepositoryConfiguration(false);
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                synchronized(CRITERIA_LOCK) {                                
                                    // XXX preselect default values
                                    parameters.getListParameter(QueryParameters.Column.PRODUCT).populate(rc.getProducts());
                                    populateProductDetails(rc);

                                    parameters.getListParameter(QueryParameters.Column.TASK_TYPE).populate(rc.getTaskTypes());
                                    parameters.getListParameter(QueryParameters.Column.PRIORITY).populate(rc.getPriorities());
                                    parameters.getListParameter(QueryParameters.Column.SEVERITY).populate(rc.getSeverities());

                                    parameters.getListParameter(QueryParameters.Column.STATUS).populate(rc.getStatuses());
                                    parameters.getListParameter(QueryParameters.Column.RESOLUTION).populate(rc.getResolutions());

                                    parameters.getListParameter(QueryParameters.Column.KEYWORDS).populate(rc.getKeywords());

                                    parameters.getByPeopleParameter().populatePeople(rc.getUsers());
                                

                                    if(criteria != null) {
                                        parameters.setCriteriaValues(criteria);
                                    }
                                    
                                    populated = true;
                                    logPopulate("populated query {0}"); // NOI18N
                                }
                            } finally {
                                querySemaphore.release();
                                logPopulate("released lock on query {0}"); // NOI18N
                                logPopulate("Finnished populate query controller {0}"); // NOI18N
                            }
                        }
                    });
                } finally {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            enableFields(true);
                            handle.finish();
                            panel.showRetrievingProgress(false, null, !query.isSaved());
                        }
                    });
                }
            }
        });
    }
    
    private void logPopulate(String msg) {
        if(ODCS.LOG.isLoggable(Level.FINE)) {
            ODCS.LOG.log(Level.FINE, msg, (query.isSaved() ? " - " + query.getDisplayName() : "")); // NOI18N
        }
    }

    protected void enableFields(boolean bl) {
        // set all non parameter fields
        panel.enableFields(bl);
        if(!modifiable) {
            hideModificationFields();
        }
        // set the parameter fields
        for (QueryParameters.Parameter qp : parameters.getAll()) {
            qp.setEnabled(bl);
        }
    }

    protected void selectFirstProduct() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                if(panel.productList.getModel().getSize() > 0) {
                    panel.productList.setSelectedIndex(0);
                }
            }
        });
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if(e.getSource() == panel.filterComboBox) {
            onFilterChange((Filter)e.getItem());
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if(e.getSource() == panel.productList) {
            onProductChanged(e);
        }
    }

    @Override
    public void focusGained(FocusEvent e) {
        if(e.getComponent() == panel.endTextField) {
            panel.endTextField.setForeground(javax.swing.UIManager.getDefaults().getColor("TextField.activeForeground")); // NOI18N
            String txt = panel.endTextField.getText();
            if(txt.trim().equals(Bundle.LBL_Now())) {
                panel.endTextField.setText(""); // NOI18N
            } else {
                selectText(panel.endTextField);
            }
        } else if(e.getComponent() == panel.startTextField) {
            selectText(panel.startTextField);
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        if(e.getComponent() == panel.endTextField) {
            String txt = panel.endTextField.getText();
            if(txt == null || txt.trim().isEmpty()) {
                setEndNow(); 
            } 
        }
    }
    
    @NbBundle.Messages({"LBL_Now=Now"})
    private void setEndNow() {
        panel.endTextField.setText(Bundle.LBL_Now());
        panel.endTextField.setForeground(javax.swing.UIManager.getDefaults().getColor("TextField.inactiveForeground")); // NOI18N
    }

    private void selectText(JTextField fld) {
        String txt = fld.getText();
        if(txt == null || txt.trim().isEmpty()) {
            return;
        }
        fld.setSelectionStart(0);
        fld.setSelectionEnd(txt.length());
    }    

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == panel.searchButton) {
            onRefresh();
        } else if (e.getSource() == panel.gotoIssueButton) {
            onGotoIssue();
        } else if (e.getSource() == panel.saveChangesButton) {
            onSave(true); // refresh
        } else if (e.getSource() == panel.cancelChangesButton) {
            onCancelChanges();
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
                   e.getSource() == panel.byTextTextField 
//                XXX any other field ???
//                   e.getSource() == panel.peopleTextField ||
//                   e.getSource() == panel.changedFromTextField ||
//                   e.getSource() == panel.newValueTextField ||
//                   e.getSource() == panel.changedToTextField
                )
        {
            onRefresh();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // do nothing
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // do nothing
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if(e.getKeyCode() != KeyEvent.VK_ENTER) {
            return;
        }
        if(e.getSource() == panel.productList ||
           e.getSource() == panel.componentList ||
           e.getSource() == panel.releaseList ||
           e.getSource() == panel.statusList ||
           e.getSource() == panel.resolutionList ||
           e.getSource() == panel.priorityList 
//           XXX any other field ???
//           e.getSource() == panel.changedList
                )
        {
            onRefresh();
        }
    }

    private void onFilterChange(Filter filter) {
        selectFilter(filter);
    }

    private void onSave(final boolean refresh) {
       ODCS.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                ODCS.LOG.fine("on save start");
                String name = query.getDisplayName();
                if(!query.isSaved()) {
                    name = getSaveName();
                    if(name == null) {
                        return;
                    }
                }
                assert name != null;
                save(name);
                ODCS.LOG.fine("on save finnish");

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
        ODCS.LOG.log(Level.FINE, "saving query '{0}'", new Object[]{name});
        try {
            panel.setRemoteInvocationRunning(true);
            enableFields(false);
            if(query.save(name)) {
                setAsSaved();
                if (!query.wasRun()) {
                    ODCS.LOG.log(Level.FINE, "refreshing query '{0}' after save", new Object[]{name});
                    onRefresh();
                }
                parameters.resetChanged();
            }
        } finally {
            panel.setRemoteInvocationRunning(false);
            enableFields(true);
        }
        ODCS.LOG.log(Level.FINE, "query '{0}' saved", new Object[]{name});
    }

    private String getSaveName() {
        QueryNameValidator v = new QueryNameValidator() {
            @Override
            public String isValid(String name) {
                Collection<ODCSQuery> queries = repository.getQueries ();
                for (ODCSQuery q : queries) {
                    if(q.getDisplayName().equals(name)) {
                        return NbBundle.getMessage(ODCSQueryController.class, "MSG_SAME_NAME");
                    }
                }
                return null;
            }
        };
        return SaveQueryPanel.show(v, new HelpCtx("org.netbeans.modules.odcs.tasks.query.savePanel"));
    }

    private void onCancelChanges() {
        assert EventQueue.isDispatchThread();
        
        synchronized(CRITERIA_LOCK) {
            criteria = originalCriteria;
            originalCriteria = null;
        }
        
        setAsSaved();
    }

    public void selectFilter(final Filter filter) {
        if(filter != null) {
            // XXX this part should be handled in the issues table - move the filtercombo and the label over
            Collection<ODCSIssue> issues = query.getIssues();
            int c = 0;
            if(issues != null) {
                for (ODCSIssue issue : issues) {
                    if(filter.accept(issue.getNode())) {
                        c++;
                    }
                }
            }
            final int issueCount = c;

            Runnable r = new Runnable() {
                @Override
                public void run() {
                    panel.filterComboBox.setSelectedItem(filter);
                    setIssueCount(issueCount);
                }
            };
            if(EventQueue.isDispatchThread()) {
                r.run();
            } else {
                EventQueue.invokeLater(r);
            }
        }
        issueTable.setFilter(filter);
    }

    private void setAsSaved() {
        panel.setSaved(query.getDisplayName(), getLastRefresh());
        panel.setModifyVisible(false);
    }

    private String getLastRefresh() throws MissingResourceException {
        long l = query.getLastRefresh();
        return l > 0 ?
            dateFormat.format(new Date(l)) :
            NbBundle.getMessage(ODCSQueryController.class, "LBL_Never"); // NOI18N
    }

    private void onGotoIssue() {
        String idText = panel.idTextField.getText().trim();
        if(idText == null || idText.trim().equals("") ) {                       // NOI18N
            return;
        }

        final String id = idText.replaceAll("\\s", "");                         // NOI18N
        
        final Task[] t = new Task[1];
        Cancellable c = new Cancellable() {
            @Override
            public boolean cancel() {
                if(t[0] != null) {
                    return t[0].cancel();
                }
                return true;
            }
        };
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(ODCSQueryController.class, "MSG_Opening", new Object[] {id}), c); // NOI18N
        t[0] = ODCS.getInstance().getRequestProcessor().create(new Runnable() {
            @Override
            public void run() {
                handle.start();
                try {
                    openIssue(repository.getIssue(id));
                } finally {
                    handle.finish();
                }
            }
        });
        t[0].schedule(0);
    }

    protected void openIssue(ODCSIssue issue) {
        if (issue != null) {
            ODCSUtil.openIssue(issue);
        } else {
            // XXX nice message?
        }
    }

    private void onWeb() {
        TeamProject kp = repository.getLookup().lookup(TeamProject.class);
        assert kp != null; // all odcs repositories should come from team support
        if (kp == null) {
            return;
        }
        try {
            URL url;
            if(!query.isSaved()) {
                String queryString = getQueryString();
                if(queryString == null) {
                    return;
                }
                url = new URL(kp.getWebLocation() + ODCSUtil.URL_FRAGMENT_QUERY + "(" + queryString.replace(' ', '+') + ")"); // NOI18N
            } else {
                url = new URL(kp.getWebLocation() + ODCSUtil.URL_FRAGMENT_QUERY + query.getDisplayName().replace(' ', '+')); // NOI18N
            }
            HtmlBrowser.URLDisplayer.getDefault().showURLExternal(url);
        } catch (MalformedURLException muex) {
            ODCS.LOG.log(Level.INFO, "Unable to show the issue in the browser.", muex); // NOI18N
        }
    }

    private void onProductChanged(ListSelectionEvent e) {
        populateProductDetails(repository.getRepositoryConfiguration(false));
    }

    public void autoRefresh() {
        refresh(true, false);
    }

    public void refresh(boolean synchronously) {
        refresh(false, synchronously);
    }
    
    @NbBundle.Messages({"MSG_Changed=The query was changed and has to be saved before refresh.",
                        "LBL_Save=Save",
                        "LBL_Discard=Discard"})    
    public void onRefresh() {
        rp.post(new Runnable() {
            @Override
            public void run() {
                if(query.isSaved() && parameters.parametersChanged()) {
                    NotifyDescriptor desc = new NotifyDescriptor.Confirmation(
                        Bundle.MSG_Changed(), NotifyDescriptor.YES_NO_CANCEL_OPTION
                    );
                    Object[] choose = { Bundle.LBL_Save(), Bundle.LBL_Discard(), NotifyDescriptor.CANCEL_OPTION };
                    desc.setOptions(choose);
                    Object ret = DialogDisplayer.getDefault().notify(desc);
                    if(ret == choose[0]) {
                        save(query.getDisplayName());
                    } else if (ret == choose[1]) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                onCancelChanges();
                            }
                        });
                        return;
                    } else {
                        return;
                    }            
                }
                if(validate()) {
                    refresh(false, false);
                }
            }
        });
    }

    private void refresh(final boolean auto, boolean synchronously) {
        Task t;
        synchronized(REFRESH_LOCK) {
            if(refreshTask == null) {
                refreshTask = new QueryTask();
            } 
            t = refreshTask.post(auto);
        }
        if(synchronously) {
            t.waitFinished();
        }
    }

    @NbBundle.Messages({"MSG_WrongFromDate=Wrong date format in Start field.",
                        "MSG_WrongToDate=Wrong date format in End field."})
    private boolean validate() {
        ByDateParameter p = parameters.getByDateParameter();
        try {
            p.getDateFrom();
        } catch (ParseException ex) {
            ODCSUtil.notifyErrorMsg(Bundle.MSG_WrongFromDate());
            return false;
        }
        try {
            p.getDateTo();
        } catch (ParseException ex) {
            ODCSUtil.notifyErrorMsg(Bundle.MSG_WrongToDate());
            return false;
        }
        return true;
    }    
    
    private void onModify() {
        assert EventQueue.isDispatchThread();
        
        synchronized(CRITERIA_LOCK) {
            if(criteria != null) {
                parameters.setCriteriaValues(criteria);
            }

    //      XXX anything interesting here?         
    //      changedFieldsParameter.setParameterValues(QueryParameter.PV_LAST_CHANGE);
    //      peopleParameter.setParameterValues(QueryParameter.PV_PEOPLE_VALUES);
    //      panel.changedToTextField.setText(CHANGED_NOW);
        
            originalCriteria = criteria;
            criteria = null;
        }
        
        panel.setModifyVisible(true);
    }

    private void onMarkSeen() {
        ODCS.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                Collection<ODCSIssue> issues = query.getIssues();
                for (ODCSIssue issue : issues) {
                    issue.setSeen(true);
                }
            }
        });
    }

    private void onRemove() {
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
            NbBundle.getMessage(ODCSQueryController.class, "MSG_RemoveQuery", new Object[] { query.getDisplayName() }), // NOI18N
            NbBundle.getMessage(ODCSQueryController.class, "CTL_RemoveQuery"),      // NOI18N
            NotifyDescriptor.OK_CANCEL_OPTION);

        if(DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {
            ODCS.getInstance().getRequestProcessor().post(new Runnable() {
                @Override
                public void run() {
                    remove();
                }
            });
        }
    }

    private void onFindIssues() {
        Util.createNewQuery(ODCSUtil.getRepository(repository));
    }

    private void onCloneQuery() {
        String queryString = getQueryString();
        Criteria c = queryString != null ? CriteriaParser.parse(queryString) : null;
        ODCSQuery q = ODCSQuery.createNew(repository, c);
        ODCSUtil.openQuery(q);
    }

    protected void logAutoRefreshEvent(boolean autoRefresh) {
        LogUtils.logAutoRefreshEvent(
            ODCSConnector.ID,
            query.getDisplayName(),
            false,
            autoRefresh
        );
    }

    private void onRefreshConfiguration() {
        if(modifiable) {
            postPopulate(true);
        }
    }

    private void remove() {
        synchronized(REFRESH_LOCK) {
            if (refreshTask != null) {
                refreshTask.cancel();
            }
        }
        query.remove();
    }

    private void populateProductDetails(RepositoryConfiguration rc) {
        Set<com.tasktop.c2c.server.tasks.domain.Component> newComponents = new HashSet<com.tasktop.c2c.server.tasks.domain.Component>();
        Set<Iteration> newIterations = new HashSet<Iteration>();
        Set<Milestone> newMilestones = new HashSet<Milestone>();
        
        // XXX why not product specific?
        newIterations.addAll(rc.getIterations());
        
        Object[] values = panel.productList.getSelectedValues();
        if(values != null && values.length > 0)  {
            for (Object v : values) {    
                assert v instanceof Product;
                if(!(v instanceof Product)) {
                    continue;
                }
                newComponents.addAll(rc.getComponents((Product) v));
                newMilestones.addAll(rc.getMilestones((Product) v));
            }
        } else {
            newComponents.addAll(rc.getComponents());
            newMilestones.addAll(rc.getMilestones());
        }
        
        parameters.getListParameter(QueryParameters.Column.COMPONENT).populate(newComponents);
        parameters.getListParameter(QueryParameters.Column.ITERATION).populate(newIterations);
        parameters.getListParameter(QueryParameters.Column.RELEASE).populate(newMilestones);
    }

    private void setIssueCount(final int count) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                String msg =
                    count == 1 ?
                        NbBundle.getMessage(ODCSQueryController.class, "LBL_MatchingIssue", new Object[] {count}) : // NOI18N
                        NbBundle.getMessage(ODCSQueryController.class, "LBL_MatchingIssues", new Object[] {count}); // NOI18N
                panel.tableSummaryLabel.setText(msg);
            }
        });
    }

    void switchToDeterminateProgress(long issuesCount) {
        synchronized(REFRESH_LOCK) {
            if(refreshTask != null) {
                refreshTask.switchToDeterminateProgress(issuesCount);
            }
        }
    }

    void addProgressUnit(String issueDesc) {
        synchronized(REFRESH_LOCK) {
            if(refreshTask != null) {
                refreshTask.addProgressUnit(issueDesc);
            }
        }
    }

    private void hideModificationFields () {
        // can't change the controllers data
        // so alwasy keep those fields disabled
        panel.modifyButton.setEnabled(false);
        panel.removeButton.setEnabled(false);
        panel.refreshConfigurationButton.setEnabled(false);
        panel.cloneQueryButton.setEnabled(false);
    }

    String getQueryString() {
        String queryString = null;
        synchronized(CRITERIA_LOCK) {
            if(criteria != null && !parameters.parametersChanged()) {
                return criteria.toQueryString();
            }
            CriteriaBuilder cb = new CriteriaBuilder();
            for(Parameter p : parameters.getAll()) {
                Criteria c = p.getCriteria();
                if(c == null) {
                    continue;
                }
                if(cb.result == null) {
                    cb.result = c;
                } else {
                    cb.and(p.getCriteria());
                }
            }
            criteria = cb.toCriteria();
            queryString = criteria == null ? null : criteria.toQueryString();
        }
        ODCS.LOG.log(Level.FINE, "returning queryString [{0}]", queryString); // NOI18N        
        return queryString;
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

        private void startQuery() {

            // XXX isn't persistent and should be merged with refresh
////            XXX String lastChageFrom = panel.changedFromTextField.getText().trim();
////            if(lastChageFrom != null && !lastChageFrom.equals("")) {    // NOI18N
////                ODCSConfig.getInstance().setLastChangeFrom(lastChageFrom);
////            }
            
            setQueryRunning(true);
            handle = ProgressHandleFactory.createHandle(
                    NbBundle.getMessage(
                        ODCSQueryController.class,
                        "MSG_SearchingQuery",                                       // NOI18N
                        new Object[] {
                            query.getDisplayName() != null ?
                                query.getDisplayName() :
                                repository.getDisplayName()}),
                    this);
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    enableFields(false);
                    panel.showSearchingProgress(true, NbBundle.getMessage(ODCSQueryController.class, "MSG_Searching")); // NOI18N
                }
            });
            handle.start();
        } 

        private void finnishQuery() {
            setQueryRunning(false); // XXX do we need this? its called in finishQuery anyway
            synchronized(REFRESH_LOCK) {
                task = null;
            }
            if(handle != null) {
                handle.finish();
                handle = null;
            }
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    panel.setRemoteInvocationRunning(false);
                    panel.setLastRefresh(getLastRefresh());
                    panel.showNoContentPanel(false);
                    enableFields(true);
                }
            });
        }

        void switchToDeterminateProgress(long progressMaxWorkunits) {
            if(handle != null) {
                handle.switchToDeterminate((int) progressMaxWorkunits);
                this.progressMaxWorkunits = progressMaxWorkunits;
                this.progressWorkunits = 0;
            }
        }

        void addProgressUnit(String issueDesc) {
            if(handle != null && progressWorkunits < progressMaxWorkunits) {
                handle.progress(
                    NbBundle.getMessage(
                        ODCSQueryController.class, "LBL_RetrievingIssue", new Object[] {issueDesc}),
                    ++progressWorkunits);
            }
        }

        private void setQueryRunning(final boolean running) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    panel.setRemoteInvocationRunning(running);
                }
            });
        }

        @Override
        public void run() {
            startQuery();
            try {
                ODCS.LOG.log(Level.FINE, "waiting until lock releases in query {0}", query.getDisplayName()); // NOI18N
                long t = System.currentTimeMillis();
                try {
                    querySemaphore.acquire();
                } catch (InterruptedException ex) {
                    ODCS.LOG.log(Level.INFO, "interuped while trying to lock query", ex); // NOI18N
                    return;
                } 
                querySemaphore.release();
                ODCS.LOG.log(Level.FINE, "lock aquired for query {0} after {1}", new Object[]{query.getDisplayName(), System.currentTimeMillis() - t}); // NOI18N
                if(!populated) {
                    ODCS.LOG.log(Level.WARNING, "Skipping refresh of query {0} because isn''t populated.", query.getDisplayName()); // NOI18N
                    // something went wrong during populate - skip execute
                    return;
                }                
                query.refresh(autoRefresh);
            } finally {
                finnishQuery();
            }
        }

        Task post(boolean autoRefresh) {
            if(task != null && !task.isFinished()) {
                return task;
            }
            task = rp.create(this);
            this.autoRefresh = autoRefresh;
            task.schedule(0);
            return task;
        }

        @Override
        public boolean cancel() {
            if(task != null) {
                task.cancel();
                finnishQuery();
            }
            return true;
        }

        @Override
        public void notifyData(final ODCSIssue issue) {
            issueTable.addNode(issue.getNode());
            if(!query.contains(issue.getID())) {
                // XXX this is quite ugly - the query notifies an archived issue
                // but it doesn't "contain" it!
                return;
            }
            setIssueCount(++counter);
            if(counter == 1) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        panel.showNoContentPanel(false);
                    }
                });
            }
        }

        @Override
        public void started() {
            issueTable.started();
            counter = 0;
            setIssueCount(counter);
            // XXX move to API
            OwnerUtils.setLooseAssociation(ODCSUtil.getRepository(getRepository()), false);                 
        }

        @Override
        public void finished() { }
    }

}