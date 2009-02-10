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

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaCustomField;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.spi.QueryNotifyListener;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.BugzillaConfig;
import org.netbeans.modules.bugzilla.BugzillaRepository;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
import org.netbeans.modules.bugzilla.query.QueryParameter.CheckBoxParameter;
import org.netbeans.modules.bugzilla.query.QueryParameter.ComboParameter;
import org.netbeans.modules.bugzilla.query.QueryParameter.ListParameter;
import org.netbeans.modules.bugzilla.query.QueryParameter.ParameterValue;
import org.netbeans.modules.bugzilla.query.QueryParameter.TextFieldParameter;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Tomas Stupka
 */
public class QueryController extends BugtrackingController implements DocumentListener, ItemListener, ListSelectionListener, ActionListener, FocusListener {
    private QueryPanel panel;

    private final ComboParameter summaryParameter;
    private final ComboParameter commentsParameter;
    private final ComboParameter keywordsParameter;
    private final ComboParameter peopleParameter;
    private final ListParameter productParameter;
    private final ListParameter componentParameter;
    private final ListParameter versionParameter;
    private final ListParameter statusParameter;
    private final ListParameter resolutionParameter;
    private final ListParameter priorityParameter;
    private final ListParameter changedFieldsParameter;

    private final Map<String, QueryParameter> parameters;

    private RequestProcessor rp = new RequestProcessor("Bugzilla queries", 20);

    private final BugzillaRepository repository;
    private BugzillaQuery query;

//    private IssueTable issueTable;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss, EEE MMM d yyyy");

    public QueryController(BugzillaRepository repository, BugzillaQuery query, String urlParameters) {
        this.repository = repository;
        this.query = query;
        
        panel = new QueryPanel(query.getTableComponent());
        panel.productList.addListSelectionListener(this);
        panel.filterComboBox.addItemListener(this);
        panel.searchButton.addActionListener(this);
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
        panel.changedFromTextField.addFocusListener(this);

        parameters = new LinkedHashMap<String, QueryParameter>();
        // setup parameters
        summaryParameter = createQueryParameter(ComboParameter.class, panel.summaryComboBox, "short_desc_type");
        commentsParameter = createQueryParameter(ComboParameter.class, panel.commentComboBox, "long_desc_type");
        keywordsParameter = createQueryParameter(ComboParameter.class, panel.keywordsComboBox, "keywords_type");
        peopleParameter = createQueryParameter(ComboParameter.class, panel.peopleComboBox, "emailtype1");
        productParameter = createQueryParameter(ListParameter.class, panel.productList, "product");
        componentParameter = createQueryParameter(ListParameter.class, panel.componentList, "component");
        versionParameter = createQueryParameter(ListParameter.class, panel.versionList, "version");
        statusParameter = createQueryParameter(ListParameter.class, panel.statusList, "bug_status");
        resolutionParameter = createQueryParameter(ListParameter.class, panel.resolutionList, "resolution");
        priorityParameter = createQueryParameter(ListParameter.class, panel.priorityList, "priority");
        changedFieldsParameter = createQueryParameter(ListParameter.class, panel.changedList, "chfield");
        
        createQueryParameter(TextFieldParameter.class, panel.summaryTextField, "short_desc");
        createQueryParameter(TextFieldParameter.class, panel.commentTextField, "long_desc");
        createQueryParameter(TextFieldParameter.class, panel.keywordsTextField, "keywords");
        createQueryParameter(TextFieldParameter.class, panel.peopleTextField, "email1");
        createQueryParameter(CheckBoxParameter.class, panel.bugAssigneeCheckBox, "emailassigned_to1");
        createQueryParameter(CheckBoxParameter.class, panel.reporterCheckBox, "emailreporter1");
        createQueryParameter(CheckBoxParameter.class, panel.ccCheckBox, "emailcc1");
        createQueryParameter(CheckBoxParameter.class, panel.commenterCheckBox, "emaillongdesc1");
        createQueryParameter(TextFieldParameter.class, panel.changedFromTextField, "chfieldfrom");
        createQueryParameter(TextFieldParameter.class, panel.changedToTextField, "chfieldto");
        createQueryParameter(TextFieldParameter.class, panel.changedToTextField, "chfieldvalue");

        populate(urlParameters);
    }

    private <T extends QueryParameter> T createQueryParameter(Class<T> clazz, Component c, String parameter) {
        try {
            Constructor<T> constructor = clazz.getConstructor(c.getClass(), String.class);
            T t = constructor.newInstance(c, parameter);
            parameters.put(parameter, t);
            return t;
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public HelpCtx getHelpContext() {
        return new HelpCtx(org.netbeans.modules.bugzilla.query.BugzillaQuery.class);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void applyChanges() {
        
    }

    @Override
    public void opened() {
        super.opened();
    }

    @Override
    public void closed() {
        super.closed();
        onCancelChanges();
    }

    public String getUrlParameters() {
        StringBuffer sb = new StringBuffer();
        for (QueryParameter p : parameters.values()) {
            sb.append(p.get());
        }
        return sb.toString();
    }

    void populate(final String urlParameters) {
        panel.enableFields(false);        
        rp.post(new Runnable() {
            public void run() {
                try {                    
                    Bugzilla bgz = Bugzilla.getInstance();
                    productParameter.setParameterValues(toStringParameterValues(bgz.getProducts(repository)));
                    if(panel.productList.getModel().getSize() > 0) {
                        panel.productList.setSelectedIndex(0);
                        populateProductDetails(((ParameterValue) panel.productList.getSelectedValue()).getValue());
                    }

                    statusParameter.setParameterValues(toStringParameterValues(bgz.getStatusValues(repository)));
                    resolutionParameter.setParameterValues(toStringParameterValues(bgz.getResolutions(repository)));
                    priorityParameter.setParameterValues(toStringParameterValues(bgz.getPriorities(repository)));
                    changedFieldsParameter.setParameterValues(QueryParameter.PV_LAST_CHANGE);

                    summaryParameter.setParameterValues(QueryParameter.PV_TEXT_SEARCH_VALUES);
                    commentsParameter.setParameterValues(QueryParameter.PV_TEXT_SEARCH_VALUES);
                    keywordsParameter.setParameterValues(QueryParameter.PV_KEYWORDS_VALUES);
                    peopleParameter.setParameterValues(QueryParameter.PV_PEOPLE_VALUES);
                    panel.changedToTextField.setText("Now"); // NOI18N
                    
                    if(urlParameters != null) {
                        setParameters(urlParameters);
                        setAsSaved();
                        // XXX load issues
                    }

                    panel.filterComboBox.setModel(new DefaultComboBoxModel(query.getFilters()));

                } catch (MalformedURLException ex) {
                    Bugzilla.LOG.log(Level.SEVERE, null, ex);
                } catch (CoreException ex) {
                    Bugzilla.LOG.log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Bugzilla.LOG.log(Level.SEVERE, null, ex);
                } finally {
                    panel.enableFields(true);
                }
            }
        });
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
        if(e.getSource() == panel.productList) {
            onProductChanged(e);
        }
        fireDataChanged();            // XXX do we need this ???
    }

    public void focusGained(FocusEvent e) {
        if(panel.changedFromTextField.getText().equals("")) {
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
        }
    }

    private void onFilterChange(Query.Filter filter) {
        query.setFilter(filter);
    }

    private void onSave() {
        // XXX assync

        String name = query.getDisplayName();
        boolean firstTime = false;
        if(query.getDisplayName() == null) { // XXX flag!!!
            firstTime = true;
            if(BugzillaUtil.show(panel.savePanel, NbBundle.getMessage(QueryController.class, "LBL_SaveQuery"),  NbBundle.getMessage(QueryController.class, "LBL_Save"))) {
                // XXX validate name
                name = panel.queryNameTextField.getText();
                if(name == null || name.trim().equals("")) {
                    return; // XXX nice error?
                }
                query.setName(name);
            } else {
                return;
            }
        } 
        assert name != null;
        repository.saveQuery(query);
        setAsSaved();
        fireDataSaved();
        if(firstTime) {
            onSearch();
        } else {
            onRefresh();
        }
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

    private void setAsSaved() {
        panel.setSaved(query.getDisplayName(), getLastRefresh());
        query.setSaved(true);
    } 

    private String getLastRefresh() throws MissingResourceException {
        long l = query.getLastRefresh();
        return l > -1 ? dateFormat.format(new Date(l)) : NbBundle.getMessage(QueryController.class, "LBL_Never");
    }

    private void onGotoIssue() {
        // XXX progress, assync, disable fields?
        Issue issue = repository.getIssue(panel.idTextField.getText());
        if(issue != null) {
            issue.open();
        } else {
            // XXX nice message?
        }
    }

    private void onWeb() {
        final String repoURL = repository.getTaskRepository().getRepositoryUrl() + "/query.cgi" + "?format=advanced"; //XXX need constants
        rp.post(new Runnable() {
            public void run() {
                URL url;
                try {
                    url = new URL(repoURL);
                } catch (MalformedURLException ex) {
                    Bugzilla.LOG.log(Level.SEVERE, null, ex);
                    return;
                }
                HtmlBrowser.URLDisplayer displayer = HtmlBrowser.URLDisplayer.getDefault ();
                if (displayer != null) {
                    displayer.showURL (url);
                } else {
                    // XXX nice error message?
                    Bugzilla.LOG.warning("No URLDisplayer found.");
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

    private void onSearch() {
        post(new Runnable() {
            public void run() {
                String lastChageFrom = panel.changedFromTextField.getText().trim();
                if(lastChageFrom != null && !lastChageFrom.equals("")) {
                    BugzillaConfig.getInstance().setLastChangeFrom(lastChageFrom);
                }
                // XXX isn't thread safe
                if(panel.urlPanel.isVisible()) {
                    // XXX check url format etc...
                    // XXX what if there is a different host in queries repository as in the url?
                    query.performQuery(panel.urlTextField.getText());
                } else {
                    query.performQuery(getUrlParameters());
                    // XXX querydataChanged
                }
            }
        });
    }

    private void onRefresh() {
        post(new Runnable() {
            public void run() {
                // XXX do we need this? unify with .performQuery
                if(panel.urlPanel.isVisible()) {
                    // XXX check url format etc...
                    // XXX what if there is a different host in queries repository as in the url?
                    query.refresh(panel.urlTextField.getText());
                } else {
                    query.refresh(getUrlParameters());
                }
            }
        });        
    }

    private void onModify() {
        panel.setModifyVisible(true);
    }

    private void onMarkSeen() {
        // XXX async
        Issue[] issues = query.getIssues();
        for (Issue issue : issues) {
            issue.setSeen(true);
        }
        query.dataChanged();
    }

    private void onRemove() {
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
            NbBundle.getMessage(QueryController.class, "MSG_RemoveQuery", new Object[] { query.getDisplayName() }),
            NbBundle.getMessage(QueryController.class, "CTL_RemoveQuery"),
            NotifyDescriptor.OK_CANCEL_OPTION);
        if(DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {
            repository.removeQuery(query);
            fireDataRemoved();
        }
    }

    private void post(Runnable r) {
        panel.enableFields(false);        
        final Task task = rp.create(r);

        // XXX need progress suport
        // XXX need query lifecycle, isRunning, cancel, ...
        Cancellable c = new Cancellable() {
            public boolean cancel() {
                task.cancel();
                return true;
            }
        };

        final ProgressHandle handle = ProgressHandleFactory.createHandle("Searching " + query.getDisplayName(), c);
        final JComponent progressBar = ProgressHandleFactory.createProgressComponent(handle);
        panel.showNoContentPanel(true, progressBar, NbBundle.getMessage(this.getClass(), "MSG_Searching"));

        // XXX !!! remove !!!
        query.addNotifyListener(new QueryNotifyListener() {
            private int c = 0;
            public void notifyData(final Issue issue) {                                
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        panel.showNoContentPanel(false);
                        panel.tableSummaryLabel.setText(NbBundle.getMessage(QueryController.class, "LBL_MatchingIssues", new Object[] {++c})); // XXX
                    }
                });
            }

            public void started() {
                handle.start();
            }

            public void finnished() {
                handle.finish();
                query.removeNotifyListener(this); // XXX
                final Issue[] issues = query.getIssues();
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        panel.enableFields(true);
                        panel.setLastRefresh(getLastRefresh());
                        if(issues.length == 0) {
                            panel.showNoContentPanel(true, null, NbBundle.getMessage(QueryController.class, "MSG_NoResults"));
                        }
                    }
                });
            }
        });
        task.schedule(0);
    }

    private void populateProductDetails(String... products) {
        try {
            Bugzilla bgz = Bugzilla.getInstance();
            if(products == null || products.length == 0) {
                products = new String[] {null};
            }

            List<String> components = new ArrayList<String>();
            List<String> versions = new ArrayList<String>();
            for (String p : products) {
                components.addAll(bgz.getComponents(repository, p));
                versions.addAll(bgz.getVersions(repository, p));
            }
            
            componentParameter.setParameterValues(toStringParameterValues(components));
            versionParameter.setParameterValues(toStringParameterValues(versions));

        } catch (IOException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
        } catch (CoreException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
        }
    }

    private List<ParameterValue> toStringParameterValues(List<String> values) {
        List<ParameterValue> ret = new ArrayList<ParameterValue>(values.size());
        for (String v : values) {
            ret.add(new ParameterValue(v, v));
        }
        return ret;
    }

    private List<ParameterValue> toCFParameterValues(List<BugzillaCustomField> values) {
        List<ParameterValue> ret = new ArrayList<ParameterValue>(values.size());
        for (BugzillaCustomField v : values) {
            ret.add(new ParameterValue(v.getDescription(), v.getName()));
        }
        return ret;
    }

    private void setParameters(String urlParameters) {
        if(urlParameters == null) {
            return;
        }
        String[] params = urlParameters.split("&");
        if(params == null || params.length == 0) return;
        Map<String, List<ParameterValue>> normalizedParams = new HashMap<String, List<ParameterValue>>();
        for (String p : params) {
            int idx = p.indexOf("=");
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

        for (Map.Entry<String, List<ParameterValue>> e : normalizedParams.entrySet()) {
            QueryParameter pv = parameters.get(e.getKey());
            if(pv != null) {
                List<ParameterValue> pvs = e.getValue();

                // XXX won't work for combo
                pv.setValues(pvs.toArray(new ParameterValue[pvs.size()]));
            }
        }
    }

}
