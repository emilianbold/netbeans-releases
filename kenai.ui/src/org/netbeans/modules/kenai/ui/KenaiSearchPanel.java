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

/*
 * KenaiProjectsListPanel.java
 *
 * Created on Jan 20, 2009, 9:21:44 AM
 */

package org.netbeans.modules.kenai.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiFeature;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiProjectFeature;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Milan Kubec
 */
public class KenaiSearchPanel extends JPanel {

    private final JLabel noSearchResultsLabel = new JLabel(NbBundle.getMessage(KenaiSearchPanel.class, "NoSearchResults.label.text"));
    private final JLabel noMatchingResultsLabel = new JLabel(NbBundle.getMessage(KenaiSearchPanel.class, "NoResultsMatching.label.text"));

    private JPanel noSearchLabelPanel;
    private JPanel noMatchingLabelPanel;

    public enum PanelType { OPEN, BROWSE }

    private PanelType panelType;

    private ProgressHandle progressHandle;

    private boolean multiSelection;

    /** Creates new form KenaiProjectsListPanel */
    public KenaiSearchPanel(PanelType type, boolean multiSel) {

        panelType = type;
        multiSelection = multiSel;
        initComponents();

        noSearchLabelPanel = createLabelPanel(noSearchResultsLabel);
        noMatchingLabelPanel = createLabelPanel(noMatchingResultsLabel);

        // initially show <No Search Results>
        remove(scrollPane);
        add(BorderLayout.CENTER, noSearchLabelPanel);
        
        if (panelType == PanelType.BROWSE) {
            remove(createButtonPanel);
        }

    }
    
    public KenaiProject getSelectedProject() {
        KenaiProjectSearchInfo searchInfo = (KenaiProjectSearchInfo) kenaiProjectsList.getSelectedValue();
        return (searchInfo != null) ? searchInfo.kenaiProject : null;
    }

    /**
     * Returns projects selected in search project dialog
     *
     * @return selected KenaiProjects or null if no project selected or dialog canceled
     */
    public KenaiProject[] getSelectedProjects() {
        Object searchInfos[] = kenaiProjectsList.getSelectedValues();
        KenaiProject selPrjs[] = new KenaiProject[searchInfos.length];
        int i = 0;
        for (Object searchInfo : searchInfos) {
            selPrjs[i++] = ((KenaiProjectSearchInfo) searchInfo).kenaiProject;
        }
        return (searchInfos.length > 0) ? selPrjs : null;
    }

    private int getListSelMode() {
        if (!multiSelection) {
            return ListSelectionModel.SINGLE_SELECTION;
        }
        return ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        scrollPane = new JScrollPane();
        kenaiProjectsList = new JList();
        searchButtonPanel = new JPanel();
        searchLabel = new JLabel();
        searchButton = new JButton();
        searchInfoLabel = new JLabel();
        projectsLabel = new JLabel();
        searchTextField = new JTextField();
        createButtonPanel = new JPanel();
        createNewProjectButton = new JButton();

        setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        setPreferredSize(new Dimension(700, 500));
        setLayout(new BorderLayout());

        kenaiProjectsList.setSelectionMode(getListSelMode());
        kenaiProjectsList.setCellRenderer(new KenaiProjectsListRenderer());
        scrollPane.setViewportView(kenaiProjectsList);

        add(scrollPane, BorderLayout.CENTER);

        searchButtonPanel.setLayout(new GridBagLayout());

        searchLabel.setText(NbBundle.getMessage(KenaiSearchPanel.class, "KenaiSearchPanel.searchLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        searchButtonPanel.add(searchLabel, gridBagConstraints);

        searchButton.setText(NbBundle.getMessage(KenaiSearchPanel.class, "KenaiSearchPanel.searchButton.text")); // NOI18N
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

        searchInfoLabel.setText(NbBundle.getMessage(KenaiSearchPanel.class, "KenaiSearchPanel.searchInfoLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 4, 0, 0);
        searchButtonPanel.add(searchInfoLabel, gridBagConstraints);

        projectsLabel.setText(NbBundle.getMessage(KenaiSearchPanel.class, "KenaiSearchPanel.projectsLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(6, 0, 4, 0);
        searchButtonPanel.add(projectsLabel, gridBagConstraints);

        searchTextField.setText(NbBundle.getMessage(KenaiSearchPanel.class, "KenaiSearchPanel.searchTextField.text")); // NOI18N
        searchTextField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                searchTextFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        searchButtonPanel.add(searchTextField, gridBagConstraints);

        add(searchButtonPanel, BorderLayout.NORTH);

        createButtonPanel.setLayout(new GridBagLayout());

        createNewProjectButton.setText(NbBundle.getMessage(KenaiSearchPanel.class, "KenaiSearchPanel.createNewProjectButton.text")); // NOI18N
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

        add(createButtonPanel, BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void searchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        invokeSearch();
    }//GEN-LAST:event_searchButtonActionPerformed

    private void searchTextFieldActionPerformed(ActionEvent evt) {//GEN-FIRST:event_searchTextFieldActionPerformed
        invokeSearch();
    }//GEN-LAST:event_searchTextFieldActionPerformed

    private void createNewProjectButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_createNewProjectButtonActionPerformed
        new NewKenaiProjectAction().actionPerformed(evt);
    }//GEN-LAST:event_createNewProjectButtonActionPerformed
    
    private void invokeSearch() {

        boolean showProgressAndRepaint = false;
        final JPanel progressPanel = createProgressPanel();

        if (scrollPane.isShowing()) {
            remove(scrollPane);
            showProgressAndRepaint = true;
        } else if (noSearchLabelPanel != null && noSearchLabelPanel.isShowing()) {
            remove(noSearchLabelPanel);
            showProgressAndRepaint = true;
        } else if (noMatchingLabelPanel != null && noMatchingLabelPanel.isShowing()) {
            remove(noMatchingLabelPanel);
            showProgressAndRepaint = true;
        }

        if (showProgressAndRepaint) {
            add(BorderLayout.CENTER, progressPanel);
            progressHandle.start();
            revalidate();
            repaint();
        }

        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                Iterator<KenaiProject> projectsIterator = null;
                String searchPattern = searchTextField.getText();
                try {
                    projectsIterator = Kenai.getDefault().searchProjects(searchPattern).iterator();
                } catch (KenaiException ex) {
                    Exceptions.printStackTrace(ex);
                    // XXX show some error to user
                }
                if (projectsIterator != null && projectsIterator.hasNext()) {
                    // XXX createModel
                    final KenaiProjectsListModel listModel = new KenaiProjectsListModel(projectsIterator, searchPattern);
                    setListModel(listModel);
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            kenaiProjectsList.setModel(listModel);
                            progressHandle.finish();
                            remove(progressPanel);
                            add(BorderLayout.CENTER, scrollPane);
                            revalidate();
                            repaint();
                        }
                    });
                } else {
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            progressHandle.finish();
                            remove(progressPanel);
                            add(BorderLayout.CENTER, noMatchingLabelPanel);
                            revalidate();
                            repaint();
                        }
                    });
                }
            }
        });

    }

    private KenaiProjectsListModel listModel;

    private synchronized void setListModel(KenaiProjectsListModel model) {
        listModel = model;
    }

    private synchronized KenaiProjectsListModel getListModel() {
        return listModel;
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        // cancel running tasks
        KenaiProjectsListModel model = getListModel();
        if (model != null) {
            model.cancel();
        }
    }

    // ----------

    private class KenaiProjectsListModel extends DefaultListModel implements Runnable {

        private Iterator<KenaiProject> projects;
        private String pattern;

        private RequestProcessor.Task task;

        public KenaiProjectsListModel(Iterator<KenaiProject> projects, final String pattern) {
            this.projects = projects;
            this.pattern = pattern;
            task = RequestProcessor.getDefault().post(this);
        }

        public void run() {
            if (projects != null) {
                while(projects.hasNext()) {
                    KenaiProject project = projects.next();
                    if (PanelType.OPEN.equals(panelType)) {
                        addElement(new KenaiProjectSearchInfo(project, pattern));
                    } else if (PanelType.BROWSE.equals(panelType)) {
                        KenaiProjectFeature[] repos = project.getFeatures(KenaiFeature.SOURCE);
                        for (KenaiProjectFeature repo : repos) {
                            if (Utilities.SVN_REPO.equals(repo.getName()) || Utilities.HG_REPO.equals(repo.getName())) {
                                addElement(new KenaiProjectSearchInfo(project, repo, pattern));
                            }
                        }
                    }
                    if (Thread.interrupted()) {
                        return;
                    }
                }
            }
        }

        public void cancel() {
            task.cancel();
        }

    }

    private static class KenaiRepositoriesListModel extends DefaultListModel {

        public KenaiRepositoriesListModel() {
            
        }

    }

    // ----------

    private class KenaiProjectsListRenderer implements ListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            return new ListRendererPanel(list, ((KenaiProjectSearchInfo) value), index, isSelected, cellHasFocus, panelType);
        }

    }

    // ----------

    public static class KenaiProjectSearchInfo {

        public KenaiProject kenaiProject;
        public KenaiProjectFeature kenaiFeature;
        public String searchPattern;

        public KenaiProjectSearchInfo(KenaiProject kprj, String ptrn) {
            kenaiProject = kprj;
            searchPattern = ptrn;
            kenaiFeature = null;
        }

        public KenaiProjectSearchInfo(KenaiProject kprj, KenaiProjectFeature ftr, String ptrn) {
            kenaiProject = kprj;
            kenaiFeature = ftr;
            searchPattern = ptrn;
        }

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel createButtonPanel;
    private JButton createNewProjectButton;
    private JList kenaiProjectsList;
    private JLabel projectsLabel;
    private JScrollPane scrollPane;
    private JButton searchButton;
    private JPanel searchButtonPanel;
    private JLabel searchInfoLabel;
    private JLabel searchLabel;
    private JTextField searchTextField;
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

    private JPanel createProgressPanel() {
        JPanel panel = preparePanel();
        progressHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(KenaiSearchPanel.class,
                "KenaiSearchPanel.progressLabel"));
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
