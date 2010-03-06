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
import javax.swing.event.*;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.kenai.api.*;
import org.netbeans.modules.kenai.api.KenaiService.Type;
import org.netbeans.modules.kenai.ui.spi.UIUtils;
import org.netbeans.modules.kenai.ui.treelist.TreeListUI;
import org.openide.awt.Mnemonics;
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
    private final JLabel badRequest = new JLabel();

    private JPanel noSearchLabelPanel;
    private JPanel noMatchingLabelPanel;
    private JPanel badRequestPanel;

    // lists for featured/recent projects for normal open dialog and for "get sources" dialog
    private static JList kenaiFeaturedProjectsList = null;
    private static JList kenaiFeaturedProjectsListWithRepos = null;
    private static JList kenaiRecentProjectsList = null;
    private static JList kenaiRecentProjectsListWithRepos = null;

    public enum PanelType { OPEN, BROWSE }

    private PanelType panelType;

    private ProgressHandle progressHandle;
    private ProgressHandle progressHandleFeatured;
    private ProgressHandle progressHandleRecent;
    private boolean progressRunning;

    private boolean multiSelection;

    private KenaiProjectsListModel listModel;

    private static Kenai kenai;

    /** Creates new form KenaiProjectsListPanel */
    public KenaiSearchPanel(PanelType type, boolean multiSel, Kenai k) {

        panelType = type;
        multiSelection = multiSel;
        boolean clear = false;
        if (kenai!= k) {
            kenai = k;
            clear = true;
        }
        initComponents();
        setChildrenEnabled(this,kenai!=null);

        if (clear) {
            clearRecentAndFeatured();
        }
        searchTextField.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (SearchField.SEARCH.equals(e.getActionCommand())) {
                    invokeSearch();
                } else {
                    kenai = ((Kenai) searchTextField.getSelectedKenai());
                    setChildrenEnabled(KenaiSearchPanel.this,kenai!=null);
                    clearRecentAndFeatured();
                }
            }

        });

        kenaiProjectsList.setUI(new TreeListUI());

        noSearchLabelPanel = createLabelPanel(noSearchResultsLabel);
        noMatchingLabelPanel = createLabelPanel(noMatchingResultsLabel);
        badRequestPanel = createLabelPanel(badRequest);

        // initially show <No Search Results>
        searchResultsPanel.remove(scrollPane);
        searchResultsPanel.add(noSearchLabelPanel, BorderLayout.CENTER);
        
        if (panelType == PanelType.BROWSE) {
            remove(createButtonPanel);
        }

        final ListSelectionModel selModel = kenaiProjectsList.getSelectionModel();
        selModel.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if (selModel.isSelectionEmpty()) {
                        firePropertyChange(KenaiDialogDescriptor.PROP_SELECTION_VALID, null, Boolean.FALSE);
                    } else {
                        firePropertyChange(KenaiDialogDescriptor.PROP_SELECTION_VALID, null, Boolean.TRUE);
                    }
                }
            }
        });
        kenaiProjectsTabPane.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent evt) {
                if (kenaiFeaturedProjectsList != null) {
                    kenaiFeaturedProjectsList.clearSelection();
                }
                if (kenaiRecentProjectsList != null) {
                    kenaiRecentProjectsList.clearSelection();
                }
                if (kenaiFeaturedProjectsListWithRepos != null) {
                    kenaiFeaturedProjectsListWithRepos.clearSelection();
                }
                if (kenaiRecentProjectsListWithRepos != null) {
                    kenaiRecentProjectsListWithRepos.clearSelection();
                }
                kenaiProjectsList.clearSelection();
            }
        });
        if (panelType == PanelType.OPEN) {
            setOpenPanels();
        } else {
            setBrowsePanels();
        }
//        kenai.addPropertyChangeListener(Kenai.PROP_URL_CHANGED, new PropertyChangeListener() {
//
//            public void propertyChange(PropertyChangeEvent evt) {
//                kenaiFeaturedProjectsList=null;
//                kenaiRecentProjectsList=null;
//            }
//        });
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
    
    private void clearRecentAndFeatured() {
        kenaiFeaturedProjectsList = null;
        kenaiRecentProjectsList = null;
        kenaiFeaturedProjectsListWithRepos = null;
        kenaiRecentProjectsListWithRepos = null;
        if (panelType == PanelType.OPEN) {
            setOpenPanels();
        } else {
            setBrowsePanels();
        }
    }

    private void setOpenPanels() {
        if (kenaiFeaturedProjectsList == null || kenaiFeaturedProjectsList.getModel().getSize() == 0) {
            // initialize Featured project list
            kenaiFeaturedProjectsList = new JList();
            kenaiFeaturedProjectsList.setUI(new TreeListUI());
            kenaiFeaturedProjectsList.setSelectionMode(getListSelMode());
            kenaiFeaturedProjectsList.setCellRenderer(new KenaiProjectsListRenderer2());
            progressHandleFeatured = ProgressHandleFactory.createHandle(NbBundle.getMessage(KenaiSearchPanel.class, "KenaiSearchPanel.progressLabelFeatured")); // NOI18N
            presentSpecialProjects(featuredProjectPanel, scrollPaneFeatured, kenaiFeaturedProjectsList, "featured", progressHandleFeatured); //NOI18N
        }
        if (kenaiRecentProjectsList == null || kenaiRecentProjectsList.getModel().getSize() == 0) {
            // initialize Recent project list
            kenaiRecentProjectsList = new JList();
            kenaiRecentProjectsList.setUI(new TreeListUI());
            kenaiRecentProjectsList.setSelectionMode(getListSelMode());
            kenaiRecentProjectsList.setCellRenderer(new KenaiProjectsListRenderer2());
            progressHandleRecent = ProgressHandleFactory.createHandle(NbBundle.getMessage(KenaiSearchPanel.class, "KenaiSearchPanel.progressLabelRecent")); // NOI18N
            presentSpecialProjects(recentProjectPanel, scrollPaneRecent, kenaiRecentProjectsList, "recent", progressHandleRecent); //NOI18N
        }
        final ListSelectionModel selModelRecent = kenaiRecentProjectsList.getSelectionModel(); // attach selection listeners
        selModelRecent.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if (selModelRecent.isSelectionEmpty()) {
                        firePropertyChange(KenaiDialogDescriptor.PROP_SELECTION_VALID, null, Boolean.FALSE);
                    } else {
                        firePropertyChange(KenaiDialogDescriptor.PROP_SELECTION_VALID, null, Boolean.TRUE);
                    }
                }
            }
        });
        final ListSelectionModel selModelFeatured = kenaiFeaturedProjectsList.getSelectionModel();
        selModelFeatured.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if (selModelFeatured.isSelectionEmpty()) {
                        firePropertyChange(KenaiDialogDescriptor.PROP_SELECTION_VALID, null, Boolean.FALSE);
                    } else {
                        firePropertyChange(KenaiDialogDescriptor.PROP_SELECTION_VALID, null, Boolean.TRUE);
                    }
                }
            }
        });
        kenaiFeaturedProjectsList.clearSelection();
        kenaiRecentProjectsList.clearSelection();
        scrollPaneRecent.setViewportView(kenaiRecentProjectsList);
        scrollPaneFeatured.setViewportView(kenaiFeaturedProjectsList);
    }

    private void setBrowsePanels() {
        if (kenaiFeaturedProjectsListWithRepos == null || kenaiFeaturedProjectsListWithRepos.getModel().getSize() == 0) {
            // initialize Featured project list
            kenaiFeaturedProjectsListWithRepos = new JList();
            kenaiFeaturedProjectsListWithRepos.setUI(new TreeListUI());
            kenaiFeaturedProjectsListWithRepos.setSelectionMode(getListSelMode());
            kenaiFeaturedProjectsListWithRepos.setCellRenderer(new KenaiProjectsListRenderer2());
            progressHandleFeatured = ProgressHandleFactory.createHandle(NbBundle.getMessage(KenaiSearchPanel.class, "KenaiSearchPanel.progressLabelFeatured")); // NOI18N
            presentSpecialProjects(featuredProjectPanel, scrollPaneFeatured, kenaiFeaturedProjectsListWithRepos, "featured", progressHandleFeatured); //NOI18N
        }
        if (kenaiRecentProjectsListWithRepos == null || kenaiRecentProjectsListWithRepos.getModel().getSize() == 0) {
            // initialize Recent project list
            kenaiRecentProjectsListWithRepos = new JList();
            kenaiRecentProjectsListWithRepos.setUI(new TreeListUI());
            kenaiRecentProjectsListWithRepos.setSelectionMode(getListSelMode());
            kenaiRecentProjectsListWithRepos.setCellRenderer(new KenaiProjectsListRenderer2());
            progressHandleRecent = ProgressHandleFactory.createHandle(NbBundle.getMessage(KenaiSearchPanel.class, "KenaiSearchPanel.progressLabelRecent")); // NOI18N
            presentSpecialProjects(recentProjectPanel, scrollPaneRecent, kenaiRecentProjectsListWithRepos, "recent", progressHandleRecent); //NOI18N
        }
        final ListSelectionModel selModelRecent = kenaiRecentProjectsListWithRepos.getSelectionModel(); // attach selection listeners
        selModelRecent.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if (selModelRecent.isSelectionEmpty()) {
                        firePropertyChange(KenaiDialogDescriptor.PROP_SELECTION_VALID, null, Boolean.FALSE);
                    } else {
                        firePropertyChange(KenaiDialogDescriptor.PROP_SELECTION_VALID, null, Boolean.TRUE);
                    }
                }
            }
        });
        final ListSelectionModel selModelFeatured = kenaiFeaturedProjectsListWithRepos.getSelectionModel();
        selModelFeatured.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if (selModelFeatured.isSelectionEmpty()) {
                        firePropertyChange(KenaiDialogDescriptor.PROP_SELECTION_VALID, null, Boolean.FALSE);
                    } else {
                        firePropertyChange(KenaiDialogDescriptor.PROP_SELECTION_VALID, null, Boolean.TRUE);
                    }
                }
            }
        });
        kenaiFeaturedProjectsListWithRepos.clearSelection();
        kenaiRecentProjectsListWithRepos.clearSelection();
        scrollPaneRecent.setViewportView(kenaiRecentProjectsListWithRepos);
        scrollPaneFeatured.setViewportView(kenaiFeaturedProjectsListWithRepos);
    }

    /**
     * Returns project selected in search project dialog
     *
     * @return selected KenaiProject or null if no project selected or dialog canceled
     */
    public KenaiProject getSelectedProject() {
        KenaiProjectSearchInfo searchInfo = null;
        if (kenaiProjectsTabPane.getSelectedComponent().equals(featuredProjectPanel)) {
            searchInfo = (KenaiProjectSearchInfo) kenaiFeaturedProjectsList.getSelectedValue();
        } else if (kenaiProjectsTabPane.getSelectedComponent().equals(recentProjectPanel)) {
            searchInfo = (KenaiProjectSearchInfo) kenaiRecentProjectsList.getSelectedValue();
        } else {
            searchInfo = (KenaiProjectSearchInfo) kenaiProjectsList.getSelectedValue();
        }
        return (searchInfo != null) ? searchInfo.kenaiProject : null;
    }

    public KenaiProjectSearchInfo getSelectedProjectSearchInfo() {
        KenaiProjectSearchInfo searchInfo = null;
        if (kenaiProjectsTabPane.getSelectedComponent().equals(featuredProjectPanel)) {
            searchInfo = (KenaiProjectSearchInfo) (panelType == PanelType.OPEN ? kenaiFeaturedProjectsList.getSelectedValue() : kenaiFeaturedProjectsListWithRepos.getSelectedValue());
        } else if (kenaiProjectsTabPane.getSelectedComponent().equals(recentProjectPanel)) {
            searchInfo = (KenaiProjectSearchInfo) (panelType == PanelType.OPEN ? kenaiRecentProjectsList.getSelectedValue() : kenaiRecentProjectsListWithRepos.getSelectedValue());
        } else {
            searchInfo = (KenaiProjectSearchInfo) kenaiProjectsList.getSelectedValue();
        }
        return (searchInfo != null) ? searchInfo : null;
    }
    
    /**
     * Returns projects selected in search project dialog
     *
     * @return selected KenaiProjects or null if no project selected or dialog canceled
     */
    public KenaiProject[] getSelectedProjects() {
        Object searchInfos[] = null;
        if (kenaiProjectsTabPane.getSelectedComponent().equals(featuredProjectPanel)) {
            searchInfos = panelType == PanelType.OPEN?kenaiFeaturedProjectsList.getSelectedValues():kenaiFeaturedProjectsListWithRepos.getSelectedValues();
        } else if (kenaiProjectsTabPane.getSelectedComponent().equals(recentProjectPanel)) {
            searchInfos = panelType == PanelType.OPEN?kenaiRecentProjectsList.getSelectedValues():kenaiRecentProjectsListWithRepos.getSelectedValues();
        } else {
            searchInfos = kenaiProjectsList.getSelectedValues();
        }
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

        searchButtonPanel = new JPanel();
        searchTextField = new SearchField(kenai);
        searchLabel = new JLabel();
        searchButton = new JButton();
        searchInfoLabel = new JLabel();
        projectsLabel = new JLabel();
        createButtonPanel = new JPanel();
        createNewProjectButton = new JButton();
        kenaiProjectsTabPane = new JTabbedPane();
        featuredProjectPanel = new JPanel();
        scrollPaneFeatured = new JScrollPane();
        recentProjectPanel = new JPanel();
        scrollPaneRecent = new JScrollPane();
        searchResultsPanel = new JPanel();
        scrollPane = new JScrollPane();
        kenaiProjectsList = new JList();

        setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        setPreferredSize(new Dimension(700, 500));
        setLayout(new BorderLayout());

        searchButtonPanel.setLayout(new GridBagLayout());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        searchButtonPanel.add(searchTextField, gridBagConstraints);

        searchLabel.setLabelFor(searchTextField);
        Mnemonics.setLocalizedText(searchLabel, NbBundle.getMessage(KenaiSearchPanel.class, "KenaiSearchPanel.searchLabel.text"));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        searchButtonPanel.add(searchLabel, gridBagConstraints);

        searchLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(KenaiSearchPanel.class, "KenaiSearchPanel.searchLabel.AccessibleContext.accessibleDescription")); // NOI18N
        Mnemonics.setLocalizedText(searchButton, NbBundle.getMessage(KenaiSearchPanel.class, "KenaiSearchPanel.searchButton.text"));
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

        searchButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(KenaiSearchPanel.class, "KenaiSearchPanel.searchButton.AccessibleContext.accessibleDescription")); // NOI18N
        searchInfoLabel.setText(NbBundle.getMessage(KenaiSearchPanel.class, "KenaiSearchPanel.searchInfoLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 4, 0, 0);
        searchButtonPanel.add(searchInfoLabel, gridBagConstraints);

        searchInfoLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(KenaiSearchPanel.class, "KenaiSearchPanel.searchInfoLabel.AccessibleContext.accessibleDescription")); // NOI18N
        projectsLabel.setText(NbBundle.getMessage(KenaiSearchPanel.class, "KenaiSearchPanel.projectsLabel.text")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(6, 0, 4, 0);
        searchButtonPanel.add(projectsLabel, gridBagConstraints);

        projectsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(KenaiSearchPanel.class, "KenaiSearchPanel.projectsLabel.AccessibleContext.accessibleDescription")); // NOI18N
        add(searchButtonPanel, BorderLayout.NORTH);

        createButtonPanel.setLayout(new GridBagLayout());
        Mnemonics.setLocalizedText(createNewProjectButton, NbBundle.getMessage(KenaiSearchPanel.class, "KenaiSearchPanel.createNewProjectButton.text"));
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



        createNewProjectButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(KenaiSearchPanel.class, "KenaiSearchPanel.createNewProjectButton.AccessibleContext.accessibleDescription")); // NOI18N
        add(createButtonPanel, BorderLayout.SOUTH);

        featuredProjectPanel.setLayout(new BorderLayout());
        featuredProjectPanel.add(scrollPaneFeatured, BorderLayout.CENTER);

        kenaiProjectsTabPane.addTab(NbBundle.getMessage(KenaiSearchPanel.class, "KenaiSearchPanel.featuredProjectPanel.TabConstraints.tabTitle"), featuredProjectPanel); // NOI18N
        recentProjectPanel.setLayout(new BorderLayout());
        recentProjectPanel.add(scrollPaneRecent, BorderLayout.CENTER);

        kenaiProjectsTabPane.addTab(NbBundle.getMessage(KenaiSearchPanel.class, "KenaiSearchPanel.recentProjectPanel.TabConstraints.tabTitle"), recentProjectPanel); // NOI18N
        searchResultsPanel.setLayout(new BorderLayout());

        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        kenaiProjectsList.setSelectionMode(getListSelMode());
        kenaiProjectsList.setCellRenderer(new KenaiProjectsListRenderer2());
        scrollPane.setViewportView(kenaiProjectsList);


        kenaiProjectsList.getAccessibleContext().setAccessibleName(NbBundle.getMessage(KenaiSearchPanel.class, "KenaiSearchPanel.kenaiProjectsList.AccessibleContext.accessibleName")); // NOI18N
        kenaiProjectsList.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(KenaiSearchPanel.class, "KenaiSearchPanel.kenaiProjectsList.AccessibleContext.accessibleDescription")); // NOI18N
        searchResultsPanel.add(scrollPane, BorderLayout.CENTER);

        kenaiProjectsTabPane.addTab(NbBundle.getMessage(KenaiSearchPanel.class, "KenaiSearchPanel.searchResultsPanel.TabConstraints.tabTitle"), searchResultsPanel); // NOI18N
        add(kenaiProjectsTabPane, BorderLayout.CENTER);

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(KenaiSearchPanel.class, "KenaiSearchPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(KenaiSearchPanel.class, "KenaiSearchPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void searchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        invokeSearch();
    }//GEN-LAST:event_searchButtonActionPerformed

    private void createNewProjectButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_createNewProjectButtonActionPerformed
        new NewKenaiProjectAction().actionPerformed(evt);
    }//GEN-LAST:event_createNewProjectButtonActionPerformed

    private void presentSpecialProjects(final JPanel wherePanel, final JScrollPane whereScrollPane, final JList whereList, final String type, final ProgressHandle ph) {
        if (kenai==null) {
            return;
        }
        final JPanel progressPanel = createProgressPanel(ph);
        wherePanel.add(progressPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
        ph.start();
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                Iterator<KenaiProject> projectsIterator = null;
                String searchPattern = "&filter=" + type; //NOI18N
                try {
                    projectsIterator = kenai.searchProjects(searchPattern).iterator();
                } catch (final KenaiException em) {
                    // SHOULD NOT HAPPEN - THIS IS WELL KNOWN REQUEST CALLED FROM IDE (featured, recent, ...)
                    // REQUEST IS WELL FORMED
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                ph.finish();
                                wherePanel.remove(progressPanel);
                                if ("400 Bad Request".equals(em.getStatus())) { //NOI18N
                                    badRequest.setText(NbBundle.getMessage(KenaiSearchPanel.class, "BadRequest.label.text"));
                                } else {
                                    badRequest.setText(NbBundle.getMessage(KenaiSearchPanel.class, "ServerError.label.text"));
                                }
                                wherePanel.add(badRequestPanel, BorderLayout.CENTER);
                                revalidate();
                                repaint();
                            }
                        });
                        Logger.getLogger(KenaiSearchPanel.class.getName()).log(Level.INFO, em.getMessage(), em);
                        return;
                }
                if (projectsIterator != null && projectsIterator.hasNext()) {
                    // XXX createModel
                    final KenaiProjectsListModel listModel = new KenaiProjectsListModel(projectsIterator, searchPattern, whereList);
                    setListModel(listModel);
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            ph.finish();
                            wherePanel.remove(progressPanel);
                            whereList.setModel(listModel);
                            wherePanel.add(whereScrollPane, BorderLayout.CENTER);
                            revalidate();
                            repaint();
                        }
                    });
                } else {
                    // SHOULD NOT HAPPEN - THIS IS WELL KNOWN REQUEST CALLED FROM IDE (featured, recent, ...)
                    // IT SHOULD RETURN SOME RESULTS
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            ph.finish();
                            wherePanel.remove(progressPanel);
                            wherePanel.add(noMatchingLabelPanel, BorderLayout.CENTER);
                            revalidate();
                            repaint();
                        }
                    });
                }
            }
        });
    }

    private class KenaiProjectsListRenderer2 implements ListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            return new KenaiProjectsListRenderer(list, value, index, isSelected, cellHasFocus);
        }
    }
    
    // added to support search by the projects root url...
    private static final int projectURLGroup = 1;

    private void invokeSearch() {
        UIUtils.logKenaiUsage("PROJECT_SEARCH"); // NOI18N

        kenaiProjectsTabPane.setSelectedComponent(searchResultsPanel);
        kenaiProjectsTabPane.validate();

        if (getListModel() != null) {
            getListModel().stopLoading();
        }

        searchTextField.setText(searchTextField.getText().toLowerCase());
        searchButton.setEnabled(false);
        searchTextField.setEnabled(false);

        boolean showProgressAndRepaint = false;
        progressHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(KenaiSearchPanel.class,
                "KenaiSearchPanel.progressLabel")); // NOI18N
        final JPanel progressPanel = createProgressPanel(progressHandle);

        if (scrollPane.isShowing()) {
            searchResultsPanel.remove(scrollPane);
            showProgressAndRepaint = true;
        } else if (noSearchLabelPanel != null && noSearchLabelPanel.isShowing()) {
            searchResultsPanel.remove(noSearchLabelPanel);
            showProgressAndRepaint = true;
        } else if (noMatchingLabelPanel != null && noMatchingLabelPanel.isShowing()) {
            searchResultsPanel.remove(noMatchingLabelPanel);
            showProgressAndRepaint = true;
        } else if (badRequestPanel != null && badRequestPanel.isShowing()) {
            searchResultsPanel.remove(badRequestPanel);
            showProgressAndRepaint = true;
        }

        if (showProgressAndRepaint) {
            searchResultsPanel.add(progressPanel, BorderLayout.CENTER);
            startProgress();
            revalidate();
            repaint();
        }

        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                Iterator<KenaiProject> projectsIterator = null;
                String searchPattern = searchTextField.getText();
                Pattern projectURLPattern = Pattern.compile(kenai.getUrl().toString().replaceFirst("^https://", "http://") + "/projects/([^/]*)/?.*");
                Matcher m = projectURLPattern.matcher(searchPattern);
                if (m.matches()) {
                    searchPattern = m.group(projectURLGroup);
                }
                try {
                    projectsIterator = kenai.searchProjects(searchPattern).iterator();
                } catch (final KenaiException em) {
                    EventQueue.invokeLater(new Runnable() {

                        public void run() {
                            finishProgress();
                            searchResultsPanel.remove(progressPanel);
                            searchButton.setEnabled(true);
                            searchTextField.setEnabled(true);
                            if ("400 Bad Request".equals(em.getStatus())) { //NOI18N
                                badRequest.setText(NbBundle.getMessage(KenaiSearchPanel.class, "BadRequest.label.text"));
                            } else {
                                badRequest.setText(NbBundle.getMessage(KenaiSearchPanel.class, "ServerError.label.text"));
                            }
                            searchResultsPanel.add(badRequestPanel, BorderLayout.CENTER);
                            revalidate();
                            repaint();
                            searchTextField.requestFocus();
                        }
                    });
                    Logger.getLogger(KenaiSearchPanel.class.getName()).log(Level.INFO, em.getMessage(), em);
                    return;
                }
                if (projectsIterator != null && projectsIterator.hasNext()) {
                    // XXX createModel
                    final KenaiProjectsListModel listModel = new KenaiProjectsListModel(projectsIterator, searchPattern, kenaiProjectsList);
                    setListModel(listModel);
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            kenaiProjectsList.setModel(listModel);
                            finishProgress();
                            searchResultsPanel.remove(progressPanel);
                            searchButton.setEnabled(true);
                            searchTextField.setEnabled(true);
                            searchResultsPanel.add(scrollPane, BorderLayout.CENTER);
                            revalidate();
                            repaint();
                        }
                    });
                } else {
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            finishProgress();
                            searchResultsPanel.remove(progressPanel);
                            searchButton.setEnabled(true);
                            searchTextField.setEnabled(true);
                            searchResultsPanel.add(noMatchingLabelPanel, BorderLayout.CENTER);
                            revalidate();
                            repaint();
                            searchTextField.requestFocus();
                        }
                    });
                }
            }
        });

    }

    private synchronized void setListModel(KenaiProjectsListModel model) {
        listModel = model;
    }

    private synchronized KenaiProjectsListModel getListModel() {
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
        KenaiProjectsListModel model = getListModel();
        if (model != null) {
            model.stopLoading();
        }
    }

    // ----------

    private class KenaiProjectsListModel extends DefaultListModel implements Runnable {

        private Iterator<KenaiProject> projects;
        private String pattern;
        private JList kpList;
        private static final int MAX_PROJECT_COUNT = 100;

        private boolean itemSelected = false;

        private boolean stopLoading;

        public KenaiProjectsListModel(Iterator<KenaiProject> projects, final String pattern, final JList kpLst) {
            this.projects = projects;
            this.pattern = pattern;
            this.kpList = kpLst;
            RequestProcessor.getDefault().post(this);
        }

        public void run() {
            if (projects != null) {
                int count=0;
                while(projects.hasNext() && count<MAX_PROJECT_COUNT) {
                    KenaiProject project = projects.next();
                    count++;
                    try {
                        project.getProjectIcon(); // a project image will be needed, prepare it in advance
                    } catch (KenaiException ex) { // problem with icon loading
                        Logger.getLogger(KenaiSearchPanel.class.getName()).log(Level.INFO, "There are problems with getting a project icon - maybe see http://www.netbeans.org/issues/show_bug.cgi?id=172649", ex); //NOI18N
                    }
                    if (PanelType.OPEN.equals(panelType)) {
                        addElementLater(new KenaiProjectSearchInfo(project, pattern));
                    } else if (PanelType.BROWSE.equals(panelType)) {
                        try {
                            KenaiFeature[] repos = project.getFeatures(Type.SOURCE);
                            for (KenaiFeature repo : repos) {
                                if (KenaiService.Names.SUBVERSION.equals(repo.getService()) || KenaiService.Names.MERCURIAL.equals(repo.getService())) {
                                    addElementLater(new KenaiProjectSearchInfo(project, repo, pattern));
                                }
                            }
                        } catch (KenaiException kenaiException) {
                            Exceptions.printStackTrace(kenaiException);
                        }
                    }
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

        private void addElementLater(final KenaiProjectSearchInfo kenaiProjectSearchInfo) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    addElement(kenaiProjectSearchInfo);
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

    public static class KenaiProjectSearchInfo {

        public KenaiProject kenaiProject;
        public KenaiFeature kenaiFeature;
        public String searchPattern;

        public KenaiProjectSearchInfo(KenaiProject kprj, String ptrn) {
            kenaiProject = kprj;
            searchPattern = ptrn;
            kenaiFeature = null;
        }

        public KenaiProjectSearchInfo(KenaiProject kprj, KenaiFeature ftr, String ptrn) {
            kenaiProject = kprj;
            kenaiFeature = ftr;
            searchPattern = ptrn;
        }

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel createButtonPanel;
    private JButton createNewProjectButton;
    private JPanel featuredProjectPanel;
    private JList kenaiProjectsList;
    private JTabbedPane kenaiProjectsTabPane;
    private JLabel projectsLabel;
    private JPanel recentProjectPanel;
    private JScrollPane scrollPane;
    private JScrollPane scrollPaneFeatured;
    private JScrollPane scrollPaneRecent;
    private JButton searchButton;
    private JPanel searchButtonPanel;
    private JLabel searchInfoLabel;
    private JLabel searchLabel;
    private JPanel searchResultsPanel;
    private SearchField searchTextField;
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
