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
package org.netbeans.modules.odcs.ui.project;

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
import org.netbeans.modules.odcs.api.ODCSServer;
import org.netbeans.modules.odcs.api.ODCSProject;
import org.netbeans.modules.odcs.client.api.ODCSException;
import org.netbeans.modules.odcs.ui.ODCSDialogDescriptor;
import org.netbeans.modules.team.commons.treelist.TreeListUI;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Milan Kubec
 * @author jpeska
 */
public class ODCSSearchPanel extends JPanel {

    private final JLabel noSearchResultsLabel = new JLabel(NbBundle.getMessage(ODCSSearchPanel.class, "NoSearchResults.label.text"));
    private final JLabel noMatchingResultsLabel = new JLabel(NbBundle.getMessage(ODCSSearchPanel.class, "NoResultsMatching.label.text"));
    private final JLabel badRequest = new JLabel();
    private JPanel noSearchLabelPanel;
    private JPanel noMatchingLabelPanel;
    private JPanel badRequestPanel;
    private SearchField searchTextField;
    private ProgressHandle progressHandle;
    private boolean progressRunning;
    private boolean multiSelection;
    private ODCSProjectsListModel listModel;
    private RequestProcessor rp = new RequestProcessor("ODCS Search Panel", 3); //NOI18N
    private static ODCSServer odcsServer;

    /**
     * Creates new form KenaiProjectsListPanel
     */
    public ODCSSearchPanel(boolean multiSel, ODCSServer odcsServer) {
        multiSelection = multiSel;
        if (ODCSSearchPanel.odcsServer != odcsServer) {
            ODCSSearchPanel.odcsServer = odcsServer;
        }
        initComponents();
        setChildrenEnabled(this, ODCSSearchPanel.odcsServer != null);
        createNewProjectButton.setEnabled(false);
        initSearchField(odcsServer);
        searchTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (SearchField.SEARCH.equals(e.getActionCommand())) {
                    invokeSearch();
                } else {
                    ODCSSearchPanel.odcsServer = ((ODCSServer) searchTextField.getSelectedServer());
                    setChildrenEnabled(ODCSSearchPanel.this, ODCSSearchPanel.odcsServer != null);
                }
            }
        });

        odcsProjectsList.setUI(new TreeListUI());

        noSearchLabelPanel = createLabelPanel(noSearchResultsLabel);
        noMatchingLabelPanel = createLabelPanel(noMatchingResultsLabel);
        badRequestPanel = createLabelPanel(badRequest);

        // initially show <No Search Results>
        searchResultPanel.remove(scrollPane);
        searchResultPanel.add(noSearchLabelPanel, BorderLayout.CENTER);

        final ListSelectionModel selModel = odcsProjectsList.getSelectionModel();
        selModel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if (selModel.isSelectionEmpty()) {
                        firePropertyChange(ODCSDialogDescriptor.PROP_SELECTION_VALID, null, Boolean.FALSE);
                    } else {
                        firePropertyChange(ODCSDialogDescriptor.PROP_SELECTION_VALID, null, Boolean.TRUE);
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

    private void initSearchField(ODCSServer odcsServer) {
        searchTextField = new SearchField(odcsServer);
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
     * @return selected ODCSProject or null if no project selected or dialog
     * canceled
     */
    public ODCSProject getSelectedProject() {
        ODCSProjectSearchInfo searchInfo = (ODCSProjectSearchInfo) odcsProjectsList.getSelectedValue();
        return (searchInfo != null) ? searchInfo.odcsProject : null;
    }

    public ODCSProjectSearchInfo getSelectedProjectSearchInfo() {
        ODCSProjectSearchInfo searchInfo = (ODCSProjectSearchInfo) odcsProjectsList.getSelectedValue();
        return (searchInfo != null) ? searchInfo : null;
    }

    /**
     * Returns projects selected in search project dialog
     *
     * @return selected KenaiProjects or null if no project selected or dialog
     * canceled
     */
    public ODCSProject[] getSelectedProjects() {
        Object searchInfos[] = odcsProjectsList.getSelectedValues();
        ODCSProject selPrjs[] = new ODCSProject[searchInfos.length];
        int i = 0;
        for (Object searchInfo : searchInfos) {
            selPrjs[i++] = ((ODCSProjectSearchInfo) searchInfo).odcsProject;
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
        odcsProjectsList = new JList();

        setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        setPreferredSize(new Dimension(700, 500));
        setLayout(new BorderLayout());

        searchButtonPanel.setLayout(new GridBagLayout());

        Mnemonics.setLocalizedText(searchLabel, NbBundle.getMessage(ODCSSearchPanel.class, "ODCSSearchPanel.searchLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        searchButtonPanel.add(searchLabel, gridBagConstraints);
        searchLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ODCSSearchPanel.class, "ODCSSearchPanel.searchLabel.AccessibleContext.accessibleDescription")); // NOI18N

        Mnemonics.setLocalizedText(searchButton, NbBundle.getMessage(ODCSSearchPanel.class, "ODCSSearchPanel.searchButton.text")); // NOI18N
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
        searchButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ODCSSearchPanel.class, "ODCSSearchPanel.searchButton.AccessibleContext.accessibleDescription")); // NOI18N

        searchInfoLabel.setText(NbBundle.getMessage(ODCSSearchPanel.class, "ODCSSearchPanel.searchInfoLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 4, 0, 0);
        searchButtonPanel.add(searchInfoLabel, gridBagConstraints);
        searchInfoLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ODCSSearchPanel.class, "ODCSSearchPanel.searchInfoLabel.AccessibleContext.accessibleDescription")); // NOI18N

        projectsLabel.setText(NbBundle.getMessage(ODCSSearchPanel.class, "ODCSSearchPanel.projectsLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(6, 0, 4, 0);
        searchButtonPanel.add(projectsLabel, gridBagConstraints);
        projectsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ODCSSearchPanel.class, "ODCSSearchPanel.projectsLabel.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        searchButtonPanel.add(dummySearchField, gridBagConstraints);

        add(searchButtonPanel, BorderLayout.NORTH);

        createButtonPanel.setLayout(new GridBagLayout());

        Mnemonics.setLocalizedText(createNewProjectButton, NbBundle.getMessage(ODCSSearchPanel.class, "ODCSSearchPanel.createNewProjectButton.text")); // NOI18N
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
        createNewProjectButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ODCSSearchPanel.class, "ODCSSearchPanel.createNewProjectButton.AccessibleContext.accessibleDescription")); // NOI18N

        add(createButtonPanel, BorderLayout.SOUTH);

        searchResultPanel.setLayout(new BorderLayout());

        odcsProjectsList.setSelectionMode(getListSelMode());
        odcsProjectsList.setCellRenderer(new ODCSProjectsListRenderer2());
        scrollPane.setViewportView(odcsProjectsList);

        searchResultPanel.add(scrollPane, BorderLayout.CENTER);

        add(searchResultPanel, BorderLayout.CENTER);

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(ODCSSearchPanel.class, "ODCSSearchPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ODCSSearchPanel.class, "ODCSSearchPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void searchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        invokeSearch();
    }//GEN-LAST:event_searchButtonActionPerformed

    private void createNewProjectButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_createNewProjectButtonActionPerformed
        //new NewODCSProjectAction().actionPerformed(evt);
    }//GEN-LAST:event_createNewProjectButtonActionPerformed

    private class ODCSProjectsListRenderer2 implements ListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            return new ODCSProjectsListRenderer(list, value, index, isSelected, cellHasFocus);
        }
    }
    // added to support search by the projects root url...
    private static final int projectURLGroup = 1;

    private void invokeSearch() {
        //KenaiUIUtils.logKenaiUsage("PROJECT_SEARCH"); // NOI18N

        if (getListModel() != null) {
            getListModel().stopLoading();
        }

        odcsProjectsList.clearSelection();

        searchTextField.setText(searchTextField.getText().toLowerCase());
        searchButton.setEnabled(false);
        searchTextField.setEnabled(false);

        boolean showProgressAndRepaint = false;
        progressHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(ODCSSearchPanel.class,
                "ODCSSearchPanel.progressLabel")); // NOI18N
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
                Iterator<ODCSProject> projectsIterator = null;
                String searchPattern = searchTextField.getText();
                Pattern projectURLPattern = Pattern.compile(odcsServer.getUrl().toString().replaceFirst("^https://", "http://") + "/projects/([^/]*)/?.*");
                Matcher m = projectURLPattern.matcher(searchPattern);
                if (m.matches()) {
                    searchPattern = m.group(projectURLGroup);
                }
                try {
                    projectsIterator = odcsServer.findProjects(searchPattern).iterator();
                } catch (final ODCSException em) {
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            finishProgress();
                            searchResultPanel.remove(progressPanel);
                            searchButton.setEnabled(true);
                            searchTextField.setEnabled(true);
//                            if ("400 Bad Request".equals(em.getStatus())) { //NOI18N
//                                badRequest.setText(NbBundle.getMessage(ODCSSearchPanel.class, "BadRequest.label.text"));
//                            } else {
//                                badRequest.setText(NbBundle.getMessage(ODCSSearchPanel.class, "ServerError.label.text"));
//                            }
                            badRequest.setText(NbBundle.getMessage(ODCSSearchPanel.class, "ServerError.label.text"));
                            searchResultPanel.add(badRequestPanel, BorderLayout.CENTER);
                            revalidate();
                            repaint();
                            searchTextField.requestFocus();
                        }
                    });
                    Logger.getLogger(ODCSSearchPanel.class.getName()).log(Level.INFO, em.getMessage(), em);
                    return;
                }
                if (projectsIterator != null && projectsIterator.hasNext()) {
                    // XXX createModel
                    final ODCSProjectsListModel listModel = new ODCSProjectsListModel(projectsIterator, searchPattern, odcsProjectsList);
                    setListModel(listModel);
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            odcsProjectsList.setModel(listModel);
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

    private synchronized void setListModel(ODCSProjectsListModel model) {
        listModel = model;
    }

    private synchronized ODCSProjectsListModel getListModel() {
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
        ODCSProjectsListModel model = getListModel();
        if (model != null) {
            model.stopLoading();
        }
    }

    // ----------
    private class ODCSProjectsListModel extends DefaultListModel implements Runnable {

        private Iterator<ODCSProject> projects;
        private String pattern;
        private JList kpList;
        private static final int MAX_PROJECT_COUNT = 100;
        private boolean itemSelected = false;
        private boolean stopLoading;

        public ODCSProjectsListModel(Iterator<ODCSProject> projects, final String pattern, final JList kpLst) {
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
                    ODCSProject project = projects.next();
                    count++;
                    addElementLater(new ODCSProjectSearchInfo(project, pattern));
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

        private void addElementLater(final ODCSProjectSearchInfo odcsProjectSearchInfo) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    addElement(odcsProjectSearchInfo);
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
    public static class ODCSProjectSearchInfo {

        public ODCSProject odcsProject;
        public String searchPattern;

        public ODCSProjectSearchInfo(ODCSProject odcsProject, String searchPattern) {
            this.odcsProject = odcsProject;
            this.searchPattern = searchPattern;
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel createButtonPanel;
    private JButton createNewProjectButton;
    private JPanel dummySearchField;
    private JList odcsProjectsList;
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
