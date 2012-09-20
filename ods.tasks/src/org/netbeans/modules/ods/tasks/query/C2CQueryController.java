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

package org.netbeans.modules.ods.tasks.query;

import com.tasktop.c2c.server.tasks.domain.AbstractReferenceValue;
import com.tasktop.c2c.server.tasks.domain.Keyword;
import com.tasktop.c2c.server.tasks.domain.Milestone;
import com.tasktop.c2c.server.tasks.domain.Product;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.api.Util;
import org.netbeans.modules.bugtracking.issuetable.Filter;
import org.netbeans.modules.bugtracking.issuetable.IssueTable;
import org.netbeans.modules.bugtracking.spi.QueryController;
import org.netbeans.modules.bugtracking.spi.QueryController.QueryMode;
import org.netbeans.modules.bugtracking.util.*;
import org.netbeans.modules.bugtracking.util.SaveQueryPanel.QueryNameValidator;
import org.netbeans.modules.ods.tasks.C2C;
import org.netbeans.modules.ods.tasks.C2CConfig;
import org.netbeans.modules.ods.tasks.C2CConnector;
import org.netbeans.modules.ods.tasks.issue.C2CIssue;
import org.netbeans.modules.ods.tasks.query.QueryParameter.ComboParameter;
import org.netbeans.modules.ods.tasks.query.QueryParameter.ListParameter;
import org.netbeans.modules.ods.tasks.repository.C2CRepository;
import org.netbeans.modules.ods.tasks.spi.C2CData;
import org.netbeans.modules.ods.tasks.util.C2CUtil;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Cancellable;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Tomas Stupka
 */
public class C2CQueryController extends QueryController implements ItemListener, ListSelectionListener, ActionListener, FocusListener, KeyListener, IssueTable.IssueTableProvider {

    protected QueryPanel panel;

    private static final String CHANGED_NOW = "Now";                            // NOI18N

//    private final ComboParameter nameParameter;
    private final ComboParameter tagsParameter;
    private final ListParameter productParameter;
    private final ListParameter componentParameter;
    private final ListParameter releasesParameter;
    private final ListParameter iterationsParameter;
    
    private final ListParameter issueTypeParameter;
    private final ListParameter priorityParameter;
    private final ListParameter severityParameter;
    
    private final ListParameter statusParameter;
    private final ListParameter resolutionParameter;
    
    private final List<QueryParameter> parameters;

    private RequestProcessor rp = new RequestProcessor("C2C query", 1, true);  // NOI18N

    private final C2CRepository repository;
    protected C2CQuery query;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // NOI18N
    private QueryTask refreshTask;
//    private final IssueTable issueTable;

    private final Object REFRESH_LOCK = new Object();
    private final IssueTable issueTable;
    private boolean modifiable;
        
    C2CQueryController(C2CRepository repository, C2CQuery query) {
        this(repository, query, null);
    }

    C2CQueryController(C2CRepository repository, C2CQuery query, String parametersString) {
        this.repository = repository;
        this.query = query;
        this.modifiable = parametersString != null;
        
        issueTable = new IssueTable(C2CUtil.getRepository(repository), query, query.getColumnDescriptors());
//      XXX  setupRenderer(issueTable);
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

        // setup parameters
        parameters = new LinkedList<QueryParameter>();
        productParameter = createQueryParameter(ListParameter.class, panel.productList, TaskAttribute.PRODUCT);              // NOI18N
        componentParameter = createQueryParameter(ListParameter.class, panel.componentList, TaskAttribute.COMPONENT);        // NOI18N
        releasesParameter = createQueryParameter(ListParameter.class, panel.releaseList, C2CData.ATTR_MILESTONE);           // NOI18N
        iterationsParameter = createQueryParameter(ListParameter.class, panel.iterationList, C2CData.ATTR_ITERATION);       // NOI18N
           
        issueTypeParameter = createQueryParameter(ListParameter.class, panel.issueTypeList, C2CData.ATTR_TASK_TYPE);        // NOI18N
        priorityParameter = createQueryParameter(ListParameter.class, panel.priorityList, TaskAttribute.PRIORITY);           // NOI18N
        severityParameter = createQueryParameter(ListParameter.class, panel.severityList, TaskAttribute.SEVERITY);           // NOI18N
        
        statusParameter = createQueryParameter(ListParameter.class, panel.statusList, TaskAttribute.STATUS);                 // NOI18N
        resolutionParameter = createQueryParameter(ListParameter.class, panel.resolutionList, TaskAttribute.RESOLUTION);     // NOI18N
        
        tagsParameter = createQueryParameter(ComboParameter.class, panel.tagsComboBox, C2CData.ATTR_TAGS);                  // NOI18N
        
        panel.filterComboBox.setModel(new DefaultComboBoxModel(issueTable.getDefinedFilters()));

        if(query.isSaved()) {
            setAsSaved();
        }
        if (modifiable) {
            postPopulate(parametersString, false);
        } else {
            hideModificationFields();
        }
    }

    // XXX probably will need a redenderer like in jira to show parent - subtask relation
//    private void setupRenderer(IssueTable issueTable) {
//        C2CQueryCellRenderer renderer = new C2CQueryCellRenderer((QueryTableCellRenderer)issueTable.getRenderer());
//        issueTable.setRenderer(renderer);
//    }

    @Override
    public void opened() {
        boolean autoRefresh = C2CConfig.getInstance().getQueryAutoRefresh(query.getDisplayName());
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

    private <T extends QueryParameter> T createQueryParameter(Class<T> clazz, Component c, String attribute) {
        try {
            Constructor<T> constructor = clazz.getConstructor(c.getClass(), String.class);
            T t = constructor.newInstance(c, attribute);
            parameters.add(t);
            return t;
        } catch (Exception ex) {
            C2C.LOG.log(Level.SEVERE, attribute, ex);
        }
        return null;
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(org.netbeans.modules.ods.tasks.query.C2CQueryController.class);
    }

    @Override
    public void setMode(QueryMode mode) {
        Filter filter;
        switch(mode) {
            case SHOW_ALL:
                filter = issueTable.getAllFilter();
                break;
            case SHOW_NEW_OR_CHANGED:
                filter = issueTable.getNewOrChangedFilter();
                break;
            default: 
                throw new IllegalStateException("Unsupported mode " + mode);
        }
        selectFilter(filter);
    }
        
    public String getUrlParameters(boolean encode) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    protected C2CRepository getRepository() {
        return repository;
    }

    private void postPopulate(final String parametersString, final boolean forceRefresh) {

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

        final String msgPopulating = NbBundle.getMessage(C2CQueryController.class, "MSG_Populating", new Object[]{repository.getDisplayName()});    // NOI18N
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
                    populate(parametersString);
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

    protected void populate(final String parametersString) {
        if(C2C.LOG.isLoggable(Level.FINE)) {
            C2C.LOG.log(Level.FINE, "Starting populate query controller{0}", (query.isSaved() ? " - " + query.getDisplayName() : "")); // NOI18N
        }
        repository.ensureCredentials();
        final C2CData clientData = C2C.getInstance().getClientData(repository);
        if(clientData == null) {
            // XXX nice errro msg?
            return;
        }
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    productParameter.setParameterValues(toParameterValues(clientData.getProducts()));
                    populateProductDetails(clientData, clientData.getProducts());
                    
                    
                    issueTypeParameter.setParameterValues(toParameterValues(clientData.getTaskTypes()));
                    priorityParameter.setParameterValues(toParameterValues(clientData.getPriorities()));
                    severityParameter.setParameterValues(toParameterValues(clientData.getSeverities()));
                    
                    statusParameter.setParameterValues(toParameterValues(clientData.getStatuses()));
                    resolutionParameter.setParameterValues(toParameterValues(clientData.getResolutions()));
                    
                    tagsParameter.setParameterValues(toParameterValues(clientData.getKeywords()));
                    panel.tagsComboBox.setSelectedIndex(-1); // ensure none is selected
                    
//                    changedFieldsParameter.setParameterValues(QueryParameter.PV_LAST_CHANGE);
//                    peopleParameter.setParameterValues(QueryParameter.PV_PEOPLE_VALUES);
//                    panel.changedToTextField.setText(CHANGED_NOW);

//                    setParameters(parametersString != null ? parametersString : C2CConfig.getInstance().getDefaultParameters());

                    // XXX
//                    if(query.isSaved()) {
//                        final boolean autoRefresh = C2CConfig.getInstance().getQueryAutoRefresh(query.getDisplayName());
//                        panel.refreshCheckBox.setSelected(autoRefresh);
//                    }
                } finally {
                    if(C2C.LOG.isLoggable(Level.FINE)) {
                        C2C.LOG.log(Level.FINE, "Finnished populate query controller {0}", (query.isSaved() ? " - " + query.getDisplayName() : "")); // NOI18N
                    }
                }
            }
        });
    }

    protected void enableFields(boolean bl) {
        // set all non parameter fields
        panel.enableFields(bl);
        if(!modifiable) {
            hideModificationFields();
        }
        // set the parameter fields
        for (QueryParameter qp : parameters) {
            qp.setEnabled(bl);
        }
    }

    protected void disableProduct() { // XXX whatever field
        productParameter.setAlwaysDisabled(true);
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
        // XXX
//        if(panel.changedFromTextField.getText().equals("")) {                   // NOI18N
//            String lastChangeFrom = C2CConfig.getInstance().getLastChangeFrom();
//            panel.changedFromTextField.setText(lastChangeFrom);
//            panel.changedFromTextField.setSelectionStart(0);
//            panel.changedFromTextField.setSelectionEnd(lastChangeFrom.length());
//        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        // do nothing
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == panel.searchButton) {
            onRefresh();
        } else if (e.getSource() == panel.gotoIssueButton) {
            onGotoIssue();
        } else if (e.getSource() == panel.searchButton) {
            onRefresh();
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
//                ||
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
//                ||
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
       C2C.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                C2C.LOG.fine("on save start");
                String name = query.getDisplayName();
                if(!query.isSaved()) {
                    name = getSaveName();
                    if(name == null) {
                        return;
                    }
                }
                assert name != null;
                save(name);
                C2C.LOG.fine("on save finnish");

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
        C2C.LOG.log(Level.FINE, "saving query '{0}'", new Object[]{name});
        query.setName(name);
        repository.saveQuery(query);
        query.setSaved(true); // XXX
        setAsSaved();
        if (!query.wasRun()) {
            C2C.LOG.log(Level.FINE, "refreshing query '{0}' after save", new Object[]{name});
            onRefresh();
        }
        C2C.LOG.log(Level.FINE, "query '{0}' saved", new Object[]{name});
    }

    private String getSaveName() {
        QueryNameValidator v = new QueryNameValidator() {
            @Override
            public String isValid(String name) {
                Collection<C2CQuery> queries = repository.getQueries ();
                for (C2CQuery q : queries) {
                    if(q.getDisplayName().equals(name)) {
                        return NbBundle.getMessage(C2CQueryController.class, "MSG_SAME_NAME");
                    }
                }
                return null;
            }
        };
        return SaveQueryPanel.show(v, new HelpCtx("org.netbeans.modules.c2c.tasks.query.savePanel"));
    }

    private void onCancelChanges() {
        if(query.getDisplayName() != null) { // XXX need a better semantic - isSaved?
            String urlParameters = C2CConfig.getInstance().getUrlParams(repository, query.getDisplayName());
            if(urlParameters != null) {
//                XXX setParameters(urlParameters);
            }
        }
        setAsSaved();
    }

    public void selectFilter(final Filter filter) {
        if(filter != null) {
            // XXX this part should be handled in the issues table - move the filtercombo and the label over
            Collection<C2CIssue> issues = query.getIssues();
            int c = 0;
            if(issues != null) {
                for (C2CIssue issue : issues) {
                    if(filter.accept(issue.getNode())) c++;
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
            NbBundle.getMessage(C2CQueryController.class, "LBL_Never"); // NOI18N
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
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(C2CQueryController.class, "MSG_Opening", new Object[] {id}), c); // NOI18N
        t[0] = C2C.getInstance().getRequestProcessor().create(new Runnable() {
            @Override
            public void run() {
                handle.start();
                try {
                    openIssue( (C2CIssue) repository.getIssue(id));
                } finally {
                    handle.finish();
                }
            }
        });
        t[0].schedule(0);
    }

    protected void openIssue(C2CIssue issue) {
        if (issue != null) {
            C2CUtil.openIssue(issue);
        } else {
            // XXX nice message?
        }
    }

    private void onWeb() {
//        String params = getUrlParameters(true);
//        String repoURL = repository.getTaskRepository().getRepositoryUrl() + "/query.cgi?format=advanced"; // NOI18N //XXX need constants
//
//        final String urlString = repoURL + (params != null && !params.equals("") ? params : ""); // NOI18N
//        C2C.getInstance().getRequestProcessor().post(new Runnable() {
//            @Override
//            public void run() {
//                URL url;
//                try {
//                    url = new URL(urlString);
//                } catch (MalformedURLException ex) {
//                    C2C.LOG.log(Level.SEVERE, null, ex);
//                    return;
//                }
//                HtmlBrowser.URLDisplayer displayer = HtmlBrowser.URLDisplayer.getDefault ();
//                if (displayer != null) {
//                    displayer.showURL (url);
//                } else {
//                    // XXX nice error message?
//                    C2C.LOG.warning("No URLDisplayer found.");             // NOI18N
//                }
//            }
//        });
    }

    private void onProductChanged(ListSelectionEvent e) {
//        Object[] values =  panel.productList.getSelectedValues();
//        String[] products = null;
//        if(values != null) {
//            products = new String[values.length];
//            for (int i = 0; i < values.length; i++) {
//                products[i] = ((ParameterValue) values[i]).getValue();
//            }
//        }
//        populateProductDetails(products);
    }

    public void autoRefresh() {
        refresh(true, false);
    }

    public void refresh(boolean synchronously) {
        refresh(false, synchronously);
    }
    
    public void onRefresh() {
        refresh(false, false);
    }

    private void refresh(final boolean auto, boolean synchronously) {
        Task t;
        synchronized(REFRESH_LOCK) {
            if(refreshTask == null) {
                refreshTask = new QueryTask();
            } else {
                refreshTask.cancel();
            }
            t = refreshTask.post(auto);
        }
        if(synchronously) {
            t.waitFinished();
        }
    }

    private void onModify() {
        panel.setModifyVisible(true);
    }

    private void onMarkSeen() {
        C2C.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                Collection<C2CIssue> issues = query.getIssues();
                for (C2CIssue issue : issues) {
                    try {
                        ((C2CIssue) issue).setSeen(true);
                    } catch (IOException ex) {
                        C2C.LOG.log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }

    private void onRemove() {
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
            NbBundle.getMessage(C2CQueryController.class, "MSG_RemoveQuery", new Object[] { query.getDisplayName() }), // NOI18N
            NbBundle.getMessage(C2CQueryController.class, "CTL_RemoveQuery"),      // NOI18N
            NotifyDescriptor.OK_CANCEL_OPTION);

        if(DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {
            C2C.getInstance().getRequestProcessor().post(new Runnable() {
                @Override
                public void run() {
                    remove();
                }
            });
        }
    }

    private void onFindIssues() {
        Util.createNewQuery(C2CUtil.getRepository(repository));
    }

    private void onCloneQuery() {
//        String p = getUrlParameters(false);
//        C2CQuery q = new C2CQuery(null, getRepository(), p, false, false, true);
//        C2CUtil.openQuery(q);
    }

    protected void logAutoRefreshEvent(boolean autoRefresh) {
        LogUtils.logAutoRefreshEvent(
            C2CConnector.ID,
            query.getDisplayName(),
            false,
            autoRefresh
        );
    }

    private void onRefreshConfiguration() {
        postPopulate(getUrlParameters(false), true);
    }

    private void remove() {
        synchronized(REFRESH_LOCK) {
            if (refreshTask != null) {
                refreshTask.cancel();
            }
        }
        query.remove();
    }

    private void populateProductDetails(C2CData clientData, Collection<Product> products) {
        Set<com.tasktop.c2c.server.tasks.domain.Component> newComponents = new HashSet<com.tasktop.c2c.server.tasks.domain.Component>();
        Set<String> newIterations = new HashSet<String>();
        Set<Milestone> newMilestones = new HashSet<Milestone>();
        
        // XXX why not product specific?
        newIterations.addAll(clientData.getActiveIterations());
        
        for (Product p : products) {    
            newComponents.addAll(clientData.getComponents(p));
            newMilestones.addAll(clientData.getMilestones(p));
        }

        componentParameter.setParameterValues(toParameterValues(newComponents));
        iterationsParameter.setParameterValues(toParameterValues(newIterations));
        releasesParameter.setParameterValues(toParameterValues(newMilestones));
    }

    private String toParameterValues(Collection values) {
        List<String> l = new ArrayList<String>(values.size());
        for (Object o : values) {
            if(o instanceof Product) {
                l.add(((Product) o).getName());
            } else if(o instanceof com.tasktop.c2c.server.tasks.domain.Component) {
                l.add(((com.tasktop.c2c.server.tasks.domain.Component) o).getName());
            } else if(o instanceof Keyword) {
                l.add(((Keyword) o).getName());
            } else if(o instanceof AbstractReferenceValue) {
                l.add(((AbstractReferenceValue) o).getValue());
            } else if(o instanceof String) {
                l.add(o.toString());
            } else {
                throw new IllegalStateException("Unknown parameter type " + o.getClass()); // NOI18N
            }
        }
        
        Collections.sort(l); // XXX e.g. Milestone has a sortkey
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < l.size(); i++) {
            sb.append(l.get(i));
            if(i < values.size() - 1) {
                sb.append(",");
            }
        }    
        return sb.toString();
    }
//
//    private void setParameters(String urlParameters) {
//        if(urlParameters == null) {
//            return;
//        }
//        String[] params = urlParameters.split("&"); // NOI18N
//        if(params == null || params.length == 0) return;
//        Map<String, List<ParameterValue>> normalizedParams = new HashMap<String, List<ParameterValue>>();
//        for (String p : params) {
//            int idx = p.indexOf("="); // NOI18N
//            if(idx > -1) {
//                String parameter = p.substring(0, idx);
//                String value = p.substring(idx + 1, p.length());
//
//                ParameterValue pv = new ParameterValue(value, value);
//                List<ParameterValue> values = normalizedParams.get(parameter);
//                if(values == null) {
//                    values = new ArrayList<ParameterValue>();
//                    normalizedParams.put(parameter, values);
//                }
//                values.add(pv);
//            } else {
//                // XXX warning!!
//            }
//        }
//
//        List<ParameterValue> componentPV = null;
//        List<ParameterValue> versionPV = null;
//        for (Map.Entry<String, List<ParameterValue>> e : normalizedParams.entrySet()) {
//            QueryParameter qp = parameters.get(e.getKey());
//            if(qp != null) {
//                if(qp == componentParameter) {
//                    componentPV = e.getValue();
//                } else if(qp == versionParameter) {
//                    versionPV = e.getValue();
//                } else {
//                    List<ParameterValue> pvs = e.getValue();
//                    qp.setValues(pvs.toArray(new ParameterValue[pvs.size()]));
//                }
//            }
//        }
//        setDependentParameter(componentParameter, componentPV);
//        setDependentParameter(versionParameter, versionPV);
//    }
//                
//    private void setDependentParameter(QueryParameter qp, List<ParameterValue> values) {
//        if(values != null) {
//            qp.setValues(values.toArray(new ParameterValue[values.size()]));
//        }
//    }

    private void setIssueCount(final int count) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                String msg =
                    count == 1 ?
                        NbBundle.getMessage(C2CQueryController.class, "LBL_MatchingIssue", new Object[] {count}) : // NOI18N
                        NbBundle.getMessage(C2CQueryController.class, "LBL_MatchingIssues", new Object[] {count}); // NOI18N
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

    @Override
    public IssueTable getIssueTable() {
        return null; // XXX issueTable;
    }

    private void hideModificationFields () {
        // can't change the controllers data
        // so alwasy keep those fields disabled
        panel.modifyButton.setEnabled(false);
        panel.removeButton.setEnabled(false);
        panel.refreshConfigurationButton.setEnabled(false);
        panel.cloneQueryButton.setEnabled(false);
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
            handle = ProgressHandleFactory.createHandle(
                    NbBundle.getMessage(
                        C2CQueryController.class,
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
                    panel.showSearchingProgress(true, NbBundle.getMessage(C2CQueryController.class, "MSG_Searching")); // NOI18N
                }
            });
            handle.start();
        }

        private void finnishQuery() {
            task = null;
            if(handle != null) {
                handle.finish();
                handle = null;
            }
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    panel.setQueryRunning(false);
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
                        C2CQueryController.class, "LBL_RetrievingIssue", new Object[] {issueDesc}),
                    ++progressWorkunits);
            }
        }

        private void executeQuery() {
            setQueryRunning(true);
            // XXX isn't persistent and should be merged with refresh
//            XXX String lastChageFrom = panel.changedFromTextField.getText().trim();
//            if(lastChageFrom != null && !lastChageFrom.equals("")) {    // NOI18N
//                C2CConfig.getInstance().setLastChangeFrom(lastChageFrom);
//            }
            try {
                query.refresh(parameters, autoRefresh);
            } finally {
                setQueryRunning(false); // XXX do we need this? its called in finishQuery anyway
                task = null;
            }
        }

        private void setQueryRunning(final boolean running) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    panel.setQueryRunning(running);
                }
            });
        }

        @Override
        public void run() {
            startQuery();
            try {
                executeQuery();
            } finally {
                finnishQuery();
            }
        }

        Task post(boolean autoRefresh) {
            if(task != null) {
                task.cancel();
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
        public void notifyData(final C2CIssue issue) {
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
            OwnerUtils.setLooseAssociation(C2CUtil.getRepository(getRepository()), false);                 
        }

        @Override
        public void finished() { }
    }

}
