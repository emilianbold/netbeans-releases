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
package org.netbeans.modules.tasks.ui;

import org.netbeans.modules.tasks.ui.dashboard.DashboardViewer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JComponent;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.modules.tasks.ui.filter.DisplayTextTaskFilter;
import org.netbeans.modules.tasks.ui.model.Category;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.WindowManager;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.bugtracking.api.RepositoryManager;
import org.netbeans.modules.tasks.ui.dashboard.TaskNode;
import org.netbeans.modules.tasks.ui.settings.DashboardSettings;
import org.netbeans.modules.tasks.ui.treelist.TreeListModelListener;
import org.netbeans.modules.tasks.ui.treelist.TreeListNode;
import org.netbeans.modules.tasks.ui.utils.DashboardRefresher;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//org.netbeans.modules.demotasklist//Dashboard//EN",
autostore = false)
@TopComponent.Description(preferredID = "DashboardTopComponent",
iconBase = "org/netbeans/modules/tasks/ui/resources/dashboard.png",
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "explorer", openAtStartup = true, position = 350)
@ActionID(category = "Window", id = "org.netbeans.modules.tasks.ui.DashboardTopComponent")
@ActionReference(path = "Menu/Window", position = 501)
@TopComponent.OpenActionRegistration(displayName = "#CTL_DashboardAction",
preferredID = "DashboardTopComponent")
@NbBundle.Messages({
    "CTL_DashboardAction=Task Dashboard",
    "CTL_DashboardTopComponent=Task Dashboard",
    "HINT_DashboardTopComponent=This is a Task Dashboard"
})
public final class DashboardTopComponent extends TopComponent {

    private static DashboardTopComponent instance;
    private ComponentAdapter componentAdapter;
    private JComponent dashboardComponent;
    private FilterDocumentListener filterListener;
    private CategoryNameDocumentListener categoryNameListener;
    private Timer filterTimer;
    private ActiveTaskPanel activeTaskPanel;
    private final GridBagConstraints activeTaskConstrains;
    private FilterPanel filterPanel;
    private DisplayTextTaskFilter displayTextTaskFilter = null;
    private CategoryNamePanel categoryNamePanel;
    private NotifyDescriptor categoryNameDialog;
    private DashboardSelectionListener dashboardSelectionListener;
    private Timer dashboardRefreshTime;
    private final DashboardRefresher refresher;

    public DashboardTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(DashboardTopComponent.class, "CTL_DashboardTopComponent")); //NOI18N
        filterTimer = new Timer(500, new FilterTimerListener());
        filterTimer.stop();
        refresher = DashboardRefresher.getInstance();
        dashboardRefreshTime = new Timer(10000, new RefreshTimerListener());
        dashboardSelectionListener = new DashboardSelectionListener();
        activeTaskConstrains = new GridBagConstraints(0, 1, 2, 1, 1.0, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 3, 0, 0), 0, 0);
        componentAdapter = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                if (activeTaskPanel != null) {
                    activeTaskPanel.setTaskNameText();
                }
            }
        };
    }

    public static synchronized DashboardTopComponent getDefault() {
        if (instance == null) {
            instance = new DashboardTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the DashboardTopComponent instance. Never call {@link #getDefault}
     * directly!
     */
    public static synchronized DashboardTopComponent findInstance() {
        final String PREFERRED_ID = "DashboardTopComponent"; //NOI18N
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);

        if (win == null) {
            return getDefault();
        }
        if (win instanceof DashboardTopComponent) {
            return (DashboardTopComponent) win;
        }
        return getDefault();
    }

    public void activateTask(TaskNode taskNode) {
        deactivateTask();
        DashboardViewer.getInstance().setActiveTaskNode(taskNode);
        if (activeTaskPanel == null) {
            activeTaskPanel = new ActiveTaskPanel(taskNode);
        } else {
            activeTaskPanel.setTaskNode(taskNode);
        }
        add(activeTaskPanel, activeTaskConstrains);
        repaint();
        validate();
    }

    public void deactivateTask() {
        if (activeTaskPanel != null) {
            DashboardViewer.getInstance().setActiveTaskNode(null);
            this.remove(activeTaskPanel);
            repaint();
            validate();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBackground(new java.awt.Color(255, 255, 255));
        setLayout(new java.awt.GridBagLayout());
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files
     * only, i.e. deserialization routines; otherwise you could get a
     * non-deserialized instance. To obtain the singleton instance, use
     * {@link #findInstance}.
     */
    @Override
    public void componentOpened() {
        removeAll();
        filterPanel = new FilterPanel();
        if (filterListener == null) {
            filterListener = new FilterDocumentListener(filterTimer);
        }
        filterPanel.addDocumentListener(filterListener);
        add(filterPanel, new GridBagConstraints(0, 3, 2, 1, 1.0, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 1, 0, 0), 0, 0));

        final DashboardViewer dashboard = DashboardViewer.getInstance();
        dashboardComponent = dashboard.getComponent();
        add(dashboardComponent, new GridBagConstraints(0, 5, 2, 1, 1.0, 0.8, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(1, 1, 0, 0), 0, 0));
        RepositoryManager.getInstance().addPropertChangeListener(dashboard);

        addComponentListener(componentAdapter);
        DashboardSettings.getInstance().addPropertyChangedListener(dashboard);
        dashboard.addDashboardSelectionListener(dashboardSelectionListener);
        dashboard.addModelListener(dashboardSelectionListener);
        refresher.setRefreshEnabled(true);
        dashboardRefreshTime.restart();
        //load data after the component is displayed
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                dashboard.loadData();
            }
        });
    }

    @Override
    protected void componentClosed() {
        filterPanel.removeDocumentListener(filterListener);
        RepositoryManager.getInstance().removePropertChangeListener(DashboardViewer.getInstance());
        DashboardSettings.getInstance().removePropertyChangedListener(DashboardViewer.getInstance());
        DashboardViewer.getInstance().removeDashboardSelectionListener(dashboardSelectionListener);
        DashboardViewer.getInstance().removeModelListener(dashboardSelectionListener);
        refresher.setRefreshEnabled(false);
        dashboardRefreshTime.stop();
        super.componentClosed();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean requestFocusInWindow() {
        // Needed to transfer focus to dashboard e.g. when switched to it from Window menu.
        boolean b = super.requestFocusInWindow();
        if (dashboardComponent != null) {
            b = dashboardComponent.requestFocusInWindow();
        }
        return b;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void requestFocus() {
        // Needed to transfer focus to dashboard e.g. if it is the active TopComponent after restart.
        super.requestFocus();
        if (dashboardComponent != null) {
            dashboardComponent.requestFocus();
        }
    }

    public Category createCategory() {
        categoryNamePanel = new CategoryNamePanel(NbBundle.getMessage(DashboardTopComponent.class, "LBL_CreateCatNameLabel"), ""); //NOI18N

        boolean confirm = showCategoryNameDialog(categoryNamePanel, NbBundle.getMessage(DashboardTopComponent.class, "LBL_CreateCatTitle")); //NOI18N
        if (confirm) {
            Category category = new Category(categoryNamePanel.getCategoryName());
            DashboardViewer.getInstance().addCategory(category);
            return category;
        }
        return null;
    }

    public void renameCategory(Category category) {
        categoryNamePanel = new CategoryNamePanel(NbBundle.getMessage(DashboardTopComponent.class, "LBL_RenameCatNameLabel"), category.getName()); //NOI18N

        boolean confirm = showCategoryNameDialog(categoryNamePanel, NbBundle.getMessage(DashboardTopComponent.class, "LBL_RenameCatTitle")); //NOI18N
        if (confirm) {
            DashboardViewer.getInstance().renameCategory(category, categoryNamePanel.getCategoryName());
        }
    }

    private boolean showCategoryNameDialog(CategoryNamePanel panel, String message) {
        categoryNameDialog = new NotifyDescriptor(
                panel,
                message,
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.PLAIN_MESSAGE,
                null,
                NotifyDescriptor.OK_OPTION);
        categoryNameDialog.setValid(false);
        if (categoryNameListener == null) {
            categoryNameListener = new CategoryNameDocumentListener();
        }
        panel.addDocumentListener(categoryNameListener);
        boolean confirm = DialogDisplayer.getDefault().notify(categoryNameDialog) == NotifyDescriptor.OK_OPTION;
        panel.removeDocumentListener(categoryNameListener);
        return confirm;
    }

    public void addTask(TaskNode... taskNodes) {
        List<Category> categories = DashboardViewer.getInstance().getCategories(true);
        for (TaskNode taskNode : taskNodes) {
            if (taskNode.isCategorized()) {
                categories.remove(taskNode.getCategory());
            }
        }
        CategoryPicker picker = new CategoryPicker(categories);
        NotifyDescriptor nd = new NotifyDescriptor(
                picker,
                NbBundle.getMessage(DashboardTopComponent.class, "LBL_AddTaskToCat"), //NOI18N
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.PLAIN_MESSAGE,
                null,
                NotifyDescriptor.OK_OPTION);

        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {
            Category category = picker.getChosenCategory();
            DashboardViewer.getInstance().addTaskToCategory(category, taskNodes);
        }

    }

    public String getFilterText() {
        return filterPanel.getFilterText();
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    private class FilterTimerListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == filterTimer) {
                DashboardViewer dashboard = DashboardViewer.getInstance();
                filterTimer.stop();
                if (!filterPanel.getFilterText().isEmpty()) {
                    DisplayTextTaskFilter newTaskFilter = new DisplayTextTaskFilter(filterPanel.getFilterText());
                    int hits = dashboard.updateTaskFilter(displayTextTaskFilter, newTaskFilter);
                    displayTextTaskFilter = newTaskFilter;
                    filterPanel.setHitsCount(hits);
                } else {
                    if (displayTextTaskFilter != null) {
                        dashboard.removeTaskFilter(displayTextTaskFilter, true);
                        displayTextTaskFilter = null;
                    }
                    filterPanel.clear();
                }
            }
        }
    }

    private class FilterDocumentListener implements DocumentListener {

        private Timer timer;

        public FilterDocumentListener(Timer timer) {
            this.timer = timer;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            timer.restart();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            timer.restart();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            timer.restart();
        }
    }

    private class CategoryNameDocumentListener implements DocumentListener {

        public CategoryNameDocumentListener() {
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            checkCategoryName();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            checkCategoryName();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            checkCategoryName();
        }

        private void checkCategoryName() {
            if (categoryNamePanel.getCategoryName().isEmpty()) {
                categoryNamePanel.setErrorText(NbBundle.getMessage(DashboardTopComponent.class, "LBL_CatNameErrEmpty")); //NOI18N
                categoryNameDialog.setValid(false);
            } else if (!DashboardViewer.getInstance().isCategoryNameUnique(categoryNamePanel.getCategoryName())) {
                categoryNamePanel.setErrorText(NbBundle.getMessage(DashboardTopComponent.class, "LBL_CatNameErrUnique")); //NOI18N
                categoryNameDialog.setValid(false);
            } else {
                categoryNamePanel.setErrorText("");
                categoryNameDialog.setValid(true);
            }
        }
    }

    private class DashboardSelectionListener implements ListSelectionListener, TreeListModelListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            dashboardBusy();
        }

        @Override
        public void nodeExpanded(TreeListNode node) {
            dashboardBusy();
        }

        private void dashboardBusy() {
            // dashboard selection changed, user is using the dashboard
            refresher.setDashboardBusy(true);
            dashboardRefreshTime.restart();
        }
    }

    private class RefreshTimerListener implements ActionListener {

        public RefreshTimerListener() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // user didnt use the dasboard for some time, refresh could be performed
            if (e.getSource() == dashboardRefreshTime) {
                dashboardRefreshTime.stop();
                refresher.setDashboardBusy(false);
            }
        }
    }
}
