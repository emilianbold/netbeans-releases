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

/*
 * KenaiProjectsListPanel.java
 *
 * Created on Jan 20, 2009, 9:21:44 AM
 */
package org.netbeans.modules.ods.ui.project;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.ods.api.CloudServer;
import org.netbeans.modules.ods.api.ODSProject;
import org.netbeans.modules.ods.client.api.ODSException;
import org.netbeans.modules.ods.ui.ODSDialogDescriptor;
import org.netbeans.modules.team.ui.treelist.TreeListUI;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Milan Kubec
 * @author jpeska
 */
public class ODSSearchPanel extends JPanel {

    private final JLabel noSearchResultsLabel = new JLabel(NbBundle.getMessage(ODSSearchPanel.class, "NoSearchResults.label.text"));
    private final JLabel noMatchingResultsLabel = new JLabel(NbBundle.getMessage(ODSSearchPanel.class, "NoResultsMatching.label.text"));
    private final JLabel badRequest = new JLabel();
    private JPanel noSearchLabelPanel;
    private JPanel noMatchingLabelPanel;
    private JPanel badRequestPanel;
    private SearchField searchTextField;
    private ProgressHandle progressHandle;
    private boolean progressRunning;
    private boolean multiSelection;
    private ODSProjectsListModel listModel;
    private RequestProcessor rp = new RequestProcessor("ODS Search Panel", 3); //NOI18N
    private static CloudServer odsServer;

    /**
     * Creates new form KenaiProjectsListPanel
     */
    public ODSSearchPanel(boolean multiSel, CloudServer odsServer) {
        multiSelection = multiSel;
        if (ODSSearchPanel.odsServer != odsServer) {
            ODSSearchPanel.odsServer = odsServer;
        }
        initComponents();
        setChildrenEnabled(this, ODSSearchPanel.odsServer != null);
        createNewProjectButton.setEnabled(false);
        initSearchField(odsServer);
        searchTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (SearchField.SEARCH.equals(e.getActionCommand())) {
                    invokeSearch();
                } else {
                    ODSSearchPanel.odsServer = ((CloudServer) searchTextField.getSelectedCloud());
                    setChildrenEnabled(ODSSearchPanel.this, ODSSearchPanel.odsServer != null);
                }
            }
        });

        odsProjectsList.setUI(new TreeListUI());

        noSearchLabelPanel = createLabelPanel(noSearchResultsLabel);
        noMatchingLabelPanel = createLabelPanel(noMatchingResultsLabel);
        badRequestPanel = createLabelPanel(badRequest);

        // initially show <No Search Results>
        searchResultPanel.remove(scrollPane);
        searchResultPanel.add(noSearchLabelPanel, BorderLayout.CENTER);

        final ListSelectionModel selModel = odsProjectsList.getSelectionModel();
        selModel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if (selModel.isSelectionEmpty()) {
                        firePropertyChange(ODSDialogDescriptor.PROP_SELECTION_VALID, null, Boolean.FALSE);
                    } else {
                        firePropertyChange(ODSDialogDescriptor.PROP_SELECTION_VALID, null, Boolean.TRUE);
                    }
                }
            }
        });
    }

    private void setChildrenEnabled(Component root, boolean enabled) {
        root.setEnabled(enabled);
        if (root instanceof java.awt.Container) {
            for (Component c : ((java.awt.Container) root).getComponents()) {
                if (c != searchTextField) {
                    setChildrenEnabled(c, enabled);
                }
            }
        }
    }

    private void initSearchField(CloudServer odsServer) {
        searchTextField = new SearchField(odsServer);
        searchButtonPanel.remove(dummySearchField);
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        searchButtonPanel.add(searchTextField, gridBagConstraints);
    }

    /**
     * Returns project selected in search project dialog
     *
     * @return selected ODSProject or null if no project selected or dialog
     * canceled
     */
    public ODSProject getSelectedProject() {
        ODSProjectSearchInfo searchInfo = (ODSProjectSearchInfo) odsProjectsList.getSelectedValue();
        return (searchInfo != null) ? searchInfo.odsProject : null;
    }

    public ODSProjectSearchInfo getSelectedProjectSearchInfo() {
        ODSProjectSearchInfo searchInfo = (ODSProjectSearchInfo) odsProjectsList.getSelectedValue();
        return (searchInfo != null) ? searchInfo : null;
    }

    /**
     * Returns projects selected in search project dialog
     *
     * @return selected KenaiProjects or null if no project selected or dialog
     * canceled
     */
    public ODSProject[] getSelectedProjects() {
        Object searchInfos[] = odsProjectsList.getSelectedValues();
        ODSProject selPrjs[] = new ODSProject[searchInfos.length];
        int i = 0;
        for (Object searchInfo : searchInfos) {
            selPrjs[i++] = ((ODSProjectSearchInfo) searchInfo).odsProject;
        }
        return (searchInfos.length > 0) ? selPrjs : null;
    }

    private int getListSelMode() {
        if (!multiSelection) {
            return ListSelectionModel.SINGLE_SELECTION;
        }
        return ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        searchButtonPanel = new JPanel();
        searchLabel = new JLabel();
        searchButton = new JButton();
        searchInfoLabel = new JLabel();
        projectsLabel = new JLabel();
        dummySearchField = new JPanel();
        createButtonPanel = new JPanel();
        createNewProjectButton = new JButton();
        searchResultPanel = new JPanel();
        scrollPane = new JScrollPane();
        odsProjectsList = new JList();

        setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        setPreferredSize(new Dimension(700, 500));
        setLayout(new BorderLayout());

        searchButtonPanel.setLayout(new GridBagLayout());

        Mnemonics.setLocalizedText(searchLabel, NbBundle.getMessage(ODSSearchPanel.class, "ODSSearchPanel.searchLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        searchButtonPanel.add(searchLabel, gridBagConstraints);
        searchLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ODSSearchPanel.class, "ODSSearchPanel.searchLabel.AccessibleContext.accessibleDescription")); // NOI18N

        Mnemonics.setLocalizedText(searchButton, NbBundle.getMessage(ODSSearchPanel.class, "ODSSearchPanel.searchButton.text")); // NOI18N
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 4, 0, 0);
        searchButtonPanel.add(searchButton, gridBagConstraints);
        searchButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ODSSearchPanel.class, "ODSSearchPanel.searchButton.AccessibleContext.accessibleDescription")); // NOI18N

        searchInfoLabel.setText(NbBundle.getMessage(ODSSearchPanel.class, "ODSSearchPanel.searchInfoLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 4, 0, 0);
        searchButtonPanel.add(searchInfoLabel, gridBagConstraints);
        searchInfoLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ODSSearchPanel.class, "ODSSearchPanel.searchInfoLabel.AccessibleContext.accessibleDescription")); // NOI18N

        projectsLabel.setText(NbBundle.getMessage(ODSSearchPanel.class, "ODSSearchPanel.projectsLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(6, 0, 4, 0);
        searchButtonPanel.add(projectsLabel, gridBagConstraints);
        projectsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ODSSearchPanel.class, "ODSSearchPanel.projectsLabel.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        searchButtonPanel.add(dummySearchField, gridBagConstraints);

        add(searchButtonPanel, BorderLayout.NORTH);

        createButtonPanel.setLayout(new GridBagLayout());

        Mnemonics.setLocalizedText(createNewProjectButton, NbBundle.getMessage(ODSSearchPanel.class, "ODSSearchPanel.createNewProjectButton.text")); // NOI18N
        createNewProjectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                createNewProjectButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(4, 0, 0, 0);
        createButtonPanel.add(createNewProjectButton, gridBagConstraints);
        createNewProjectButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ODSSearchPanel.class, "ODSSearchPanel.createNewProjectButton.AccessibleContext.accessibleDescription")); // NOI18N

        add(createButtonPanel, BorderLayout.SOUTH);

        searchResultPanel.setLayout(new BorderLayout());

        odsProjectsList.setSelectionMode(getListSelMode());
        odsProjectsList.setCellRenderer(new ODSProjectsListRenderer2());
        scrollPane.setViewportView(odsProjectsList);

        searchResultPanel.add(scrollPane, BorderLayout.CENTER);

        add(searchResultPanel, BorderLayout.CENTER);

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(ODSSearchPanel.class, "ODSSearchPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ODSSearchPanel.class, "ODSSearchPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void searchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        invokeSearch();
    }//GEN-LAST:event_searchButtonActionPerformed

    private void createNewProjectButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_createNewProjectButtonActionPerformed
        //new NewODSProjectAction().actionPerformed(evt);
    }//GEN-LAST:event_createNewProjectButtonActionPerformed

    private class ODSProjectsListRenderer2 implements ListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            return new ODSProjectsListRenderer(list, value, index, isSelected, cellHasFocus);
        }
    }
    // added to support search by the projects root url...
    private static final int projectURLGroup = 1;

    private void invokeSearch() {
        //KenaiUIUtils.logKenaiUsage("PROJECT_SEARCH"); // NOI18N

        if (getListModel() != null) {
            getListModel().stopLoading();
        }

        odsProjectsList.clearSelection();

        searchTextField.setText(searchTextField.getText().toLowerCase());
        searchButton.setEnabled(false);
        searchTextField.setEnabled(false);

        boolean showProgressAndRepaint = false;
        progressHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(ODSSearchPanel.class,
                "ODSSearchPanel.progressLabel")); // NOI18N
        final JPanel progressPanel = createProgressPanel(progressHandle);

        if (scrollPane.isShowing()) {
            searchResultPanel.remove(scrollPane);
            showProgressAndRepaint = true;
        } else if (noSearchLabelPanel != null && noSearchLabelPanel.isShowing()) {
            searchResultPanel.remove(noSearchLabelPanel);
            showProgressAndRepaint = true;
        } else if (noMatchingLabelPanel != null && noMatchingLabelPanel.isShowing()) {
            searchResultPanel.remove(noMatchingLabelPanel);
            showProgressAndRepaint = true;
        } else if (badRequestPanel != null && badRequestPanel.isShowing()) {
            searchResultPanel.remove(badRequestPanel);
            showProgressAndRepaint = true;
        }

        if (showProgressAndRepaint) {
            searchResultPanel.add(progressPanel, BorderLayout.CENTER);
            startProgress();
            revalidate();
            repaint();
        }

        rp.post(new Runnable() {
            public void run() {
                Iterator<ODSProject> projectsIterator = null;
                String searchPattern = searchTextField.getText();
                Pattern projectURLPattern = Pattern.compile(odsServer.getUrl().toString().replaceFirst("^https://", "http://") + "/projects/([^/]*)/?.*");
                Matcher m = projectURLPattern.matcher(searchPattern);
                if (m.matches()) {
                    searchPattern = m.group(projectURLGroup);
                }
                try {
                    projectsIterator = odsServer.findProjects(searchPattern).iterator();
                } catch (final ODSException em) {
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            finishProgress();
                            searchResultPanel.remove(progressPanel);
                            searchButton.setEnabled(true);
                            searchTextField.setEnabled(true);
//                            if ("400 Bad Request".equals(em.getStatus())) { //NOI18N
//                                badRequest.setText(NbBundle.getMessage(ODSSearchPanel.class, "BadRequest.label.text"));
//                            } else {
//                                badRequest.setText(NbBundle.getMessage(ODSSearchPanel.class, "ServerError.label.text"));
//                            }
                            badRequest.setText(NbBundle.getMessage(ODSSearchPanel.class, "ServerError.label.text"));
                            searchResultPanel.add(badRequestPanel, BorderLayout.CENTER);
                            revalidate();
                            repaint();
                            searchTextField.requestFocus();
                        }
                    });
                    Logger.getLogger(ODSSearchPanel.class.getName()).log(Level.INFO, em.getMessage(), em);
                    return;
                }
                if (projectsIterator != null && projectsIterator.hasNext()) {
                    // XXX createModel
                    final ODSProjectsListModel listModel = new ODSProjectsListModel(projectsIterator, searchPattern, odsProjectsList);
                    setListModel(listModel);
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            odsProjectsList.setModel(listModel);
                            finishProgress();
                            searchResultPanel.remove(progressPanel);
                            searchButton.setEnabled(true);
                            searchTextField.setEnabled(true);
                            searchResultPanel.add(scrollPane, BorderLayout.CENTER);
                            revalidate();
                            repaint();
                        }
                    });
                } else {
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            finishProgress();
                            searchResultPanel.remove(progressPanel);
                            searchButton.setEnabled(true);
                            searchTextField.setEnabled(true);
                            searchResultPanel.add(noMatchingLabelPanel, BorderLayout.CENTER);
                            revalidate();
                            repaint();
                            searchTextField.requestFocus();
                        }
                    });
                }
            }
        });

    }

    private synchronized void setListModel(ODSProjectsListModel model) {
        listModel = model;
    }

    private synchronized ODSProjectsListModel getListModel() {
        return listModel;
    }

    private synchronized void startProgress() {
        if (!progressRunning && progressHandle != null) {
            progressHandle.start();
            progressRunning = true;
        }
    }

    private synchronized void finishProgress() {
        if (progressRunning && progressHandle != null) {
            progressHandle.finish();
            progressRunning = false;
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        // cancel running tasks
        ODSProjectsListModel model = getListModel();
        if (model != null) {
            model.stopLoading();
        }
    }

    // ----------
    private class ODSProjectsListModel extends DefaultListModel implements Runnable {

        private Iterator<ODSProject> projects;
        private String pattern;
        private JList kpList;
        private static final int MAX_PROJECT_COUNT = 100;
        private boolean itemSelected = false;
        private boolean stopLoading;

        public ODSProjectsListModel(Iterator<ODSProject> projects, final String pattern, final JList kpLst) {
            this.projects = projects;
            this.pattern = pattern;
            this.kpList = kpLst;
            rp.post(this);
        }

        @Override
        public void run() {
            if (projects != null) {
                int count = 0;
                while (projects.hasNext() && count < MAX_PROJECT_COUNT) {
                    ODSProject project = projects.next();
                    count++;
                    addElementLater(new ODSProjectSearchInfo(project, pattern));
                    Thread.yield();
                    if (loadingStopped()) {
                        return;
                    }
                }
            }
        }

        public synchronized void stopLoading() {
            stopLoading = true;
        }

        public synchronized boolean loadingStopped() {
            return stopLoading;
        }

        private void addElementLater(final ODSProjectSearchInfo odsProjectSearchInfo) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    addElement(odsProjectSearchInfo);
                    if (!itemSelected) {
                        kpList.requestFocus();
                        kpList.setSelectedIndex(0);
                        itemSelected = true;
                    }
                }
            });
        }
    }

    // ----------
    public static class ODSProjectSearchInfo {

        public ODSProject odsProject;
        public String searchPattern;

        public ODSProjectSearchInfo(ODSProject odsProject, String searchPattern) {
            this.odsProject = odsProject;
            this.searchPattern = searchPattern;
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel createButtonPanel;
    private JButton createNewProjectButton;
    private JPanel dummySearchField;
    private JList odsProjectsList;
    private JLabel projectsLabel;
    private JScrollPane scrollPane;
    private JButton searchButton;
    private JPanel searchButtonPanel;
    private JLabel searchInfoLabel;
    private JLabel searchLabel;
    private JPanel searchResultPanel;
    // End of variables declaration//GEN-END:variables

    private JPanel createLabelPanel(JLabel label) {
        JPanel panel = preparePanel();
        label.setEnabled(false);
        label.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(0, 0, 20, 0);
        panel.add(label, constraints);
        JScrollPane scrlPane = new JScrollPane();
        scrlPane.setViewportView(panel);
        JPanel pnl = new JPanel();
        pnl.setLayout(new BorderLayout());
        pnl.add(scrlPane, BorderLayout.CENTER);
        return pnl;
    }

    private JPanel createProgressPanel(ProgressHandle progressHandle) {
        JPanel panel = preparePanel();
        JComponent progressComponent = ProgressHandleFactory.createProgressComponent(progressHandle);
        JLabel progressLabel = ProgressHandleFactory.createMainLabelComponent(progressHandle);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 0.5;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 40, 0, 40);
        panel.add(progressLabel, constraints);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 0.5;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 40, 20, 40);
        panel.add(progressComponent, constraints);
        JScrollPane scrlPane = new JScrollPane();
        scrlPane.setViewportView(panel);
        JPanel pnl = new JPanel();
        pnl.setLayout(new BorderLayout());
        pnl.add(scrlPane, BorderLayout.CENTER);
        return pnl;
    }

    private JPanel preparePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        Color bgColor = UIManager.getColor("window"); // NOI18N
        panel.setBackground(bgColor != null ? bgColor : Color.WHITE);
        return panel;
    }
}
