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

package org.netbeans.modules.bugtracking.ui.query;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.RepositoriesSupport;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.spi.QueryNotifyListener;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.util.BugtrackingOwnerSupport;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.util.LinkButton;
import org.netbeans.modules.bugtracking.util.RepositoryComboSupport;
import org.netbeans.modules.kenai.api.Kenai;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
public final class QueryTopComponent extends TopComponent
                              implements PropertyChangeListener, QueryNotifyListener, FocusListener {

    private static QueryTopComponent instance;
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";

    /** Set of opened {@code QueryTopComponent}s. */
    private static Set<QueryTopComponent> openQueries = new HashSet<QueryTopComponent>();
    private Query[] savedQueries = null;
    
    private static final String PREFERRED_ID = "QueryTopComponent"; // NOI18N
    private Query query; // XXX synchronized
    private static final Object LOCK = new Object();

    private RequestProcessor rp = new RequestProcessor("Bugtracking query", 1, true); // NOI18N
    private Task prepareTask;
    private RepositoryComboSupport rs;

    QueryTopComponent() {
        RepositoriesSupport.getInstance().addPropertyChangeListener(this);

        initComponents();
        Font f = new JLabel().getFont();
        int s = f.getSize();
        findIssuesLabel.setFont(new Font(f.getName(), f.getStyle(), (int) (s * 1.7)));
        int unitIncrement = (int)(s*1.5);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(unitIncrement);
        scrollPane.getVerticalScrollBar().setUnitIncrement(unitIncrement);
    }

    public static Set<QueryTopComponent> getOpenQueries() {
        return openQueries;
    }
    
    private Query getQuery() {
        return query;
    }

    void init(Query query, Repository defaultRepository, boolean suggestedSelectionOnly) {
        this.query = query;
        setNameAndTooltip();

        if(suggestedSelectionOnly) {
            repositoryComboBox.setEnabled(false);
            newButton.setEnabled(false);
        }

        if (query != null) {
            setSaved();
            BugtrackingController c = query.getController();
            panel.add(c.getComponent());
            this.query.addPropertyChangeListener(this);
            this.query.addNotifyListener(this);
        } else {
            newButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    onNewClick();
                }
            });
            if(defaultRepository == null) {
                rs = RepositoryComboSupport.setup(this, repositoryComboBox, true);
            } else {
                rs = RepositoryComboSupport.setup(this, repositoryComboBox, defaultRepository);
            }
            repositoryComboBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        Object item = e.getItem();
                        if (item instanceof Repository) {
                            onRepoSelected();
                        }
                    } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                        Object item = e.getItem();
                        if (item instanceof Repository) {
                            ((Repository) item).removePropertyChangeListener(QueryTopComponent.this);
                        }
                    }
                }
            });
            newButton.addFocusListener(this);
            repositoryComboBox.addFocusListener(this);
            queriesPanel.setVisible(false);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollPane = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        panel = new javax.swing.JPanel();
        repoPanel = new javax.swing.JPanel();
        repositoryComboBox = new javax.swing.JComboBox();
        queriesPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        findIssuesLabel = new javax.swing.JLabel();
        repoLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        newButton = new org.netbeans.modules.bugtracking.util.LinkButton();

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        scrollPane.setBorder(null);

        jPanel2.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));

        panel.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));
        panel.setOpaque(false);
        panel.setLayout(new java.awt.BorderLayout());

        repoPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("EditorPane.background"));
        repoPanel.setNextFocusableComponent(newButton);

        queriesPanel.setBackground(new java.awt.Color(224, 224, 224));
        queriesPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                queriesPanelComponentResized(evt);
            }
        });
        queriesPanel.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(QueryTopComponent.class, "QueryTopComponent.jLabel1.text_1")); // NOI18N
        queriesPanel.add(jLabel1, java.awt.BorderLayout.LINE_START);

        org.openide.awt.Mnemonics.setLocalizedText(findIssuesLabel, org.openide.util.NbBundle.getMessage(QueryTopComponent.class, "QueryTopComponent.findIssuesLabel.text")); // NOI18N

        repoLabel.setLabelFor(repositoryComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(repoLabel, org.openide.util.NbBundle.getMessage(QueryTopComponent.class, "QueryTopComponent.repoLabel.text")); // NOI18N
        repoLabel.setFocusCycleRoot(true);

        jPanel1.setOpaque(false);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 64, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 8, Short.MAX_VALUE)
        );

        org.openide.awt.Mnemonics.setLocalizedText(newButton, org.openide.util.NbBundle.getMessage(QueryTopComponent.class, "QueryTopComponent.newButton.text_1")); // NOI18N

        org.jdesktop.layout.GroupLayout repoPanelLayout = new org.jdesktop.layout.GroupLayout(repoPanel);
        repoPanel.setLayout(repoPanelLayout);
        repoPanelLayout.setHorizontalGroup(
            repoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(repoPanelLayout.createSequentialGroup()
                .add(repoLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(repositoryComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(newButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(queriesPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .add(findIssuesLabel)
        );
        repoPanelLayout.setVerticalGroup(
            repoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(repoPanelLayout.createSequentialGroup()
                .add(13, 13, 13)
                .add(findIssuesLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(repoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(repoLabel)
                    .add(repositoryComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(newButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
            .add(repoPanelLayout.createSequentialGroup()
                .add(42, 42, 42)
                .add(repoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(queriesPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );

        repositoryComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryTopComponent.class, "QueryTopComponent.repositoryComboBox.AccessibleContext.accessibleDescription")); // NOI18N
        newButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(QueryTopComponent.class, "QueryTopComponent.newButton.AccessibleContext.accessibleDescription")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(panel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(repoPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(repoPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(14, 14, 14)
                .add(panel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        scrollPane.setViewportView(jPanel2);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(scrollPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(scrollPane)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void queriesPanelComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_queriesPanelComponentResized

}//GEN-LAST:event_queriesPanelComponentResized

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        updateSavedQueriesPanel();
    }//GEN-LAST:event_formComponentResized


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel findIssuesLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private org.netbeans.modules.bugtracking.util.LinkButton newButton;
    private javax.swing.JPanel panel;
    private javax.swing.JPanel queriesPanel;
    private javax.swing.JLabel repoLabel;
    private javax.swing.JPanel repoPanel;
    private javax.swing.JComboBox repositoryComboBox;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized QueryTopComponent getDefault() {
        if (instance == null) {
            instance = new QueryTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the QueryTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized QueryTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(QueryTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system."); // NOI18N
            return getDefault();
        }
        if (win instanceof QueryTopComponent) {
            return (QueryTopComponent) win;
        }
        Logger.getLogger(QueryTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID + // NOI18N
                "' ID. That is a potential source of errors and unexpected behavior."); // NOI18N
        return getDefault();
    }

    /**
     * Returns top-component that should display the given query.
     *
     * @param query query for which the top-component should be found.
     * @return top-component that should display the given query.
     */
    public static synchronized QueryTopComponent find(Query query) {
        for (QueryTopComponent tc : openQueries) {
            if (query.equals(tc.getQuery())) {
                return tc;
            }
        }
        return null;
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public void componentOpened() {
        openQueries.add(this);
        Kenai.getDefault().addPropertyChangeListener(this);
        if(query != null) {
            query.getController().opened();
        }
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                repositoryComboBox.requestFocusInWindow();
//            }
//        });
        BugtrackingManager.LOG.log(Level.FINE, "{0} - {1} opened", new Object[] {this.getClass().getName(), query != null ? query.getDisplayName() : null});
    }

    @Override
    public void componentClosed() {
        openQueries.remove(this);
        if(query != null) {
            query.removePropertyChangeListener(this);
            query.removeNotifyListener(this);
            query.getController().closed();
        }
        Kenai.getDefault().removePropertyChangeListener(this);
        if(prepareTask != null) {
            prepareTask.cancel();
        }
        BugtrackingManager.LOG.log(Level.FINE, "{0} - {1} closed", new Object[] {this.getClass().getName(), query != null ? query.getDisplayName() : null});
    }

    /** replaces this in object stream */
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    @Override
    protected String preferredID() {
        return query != null && query.getDisplayName() != null ? query.getDisplayName() : PREFERRED_ID;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(Query.EVENT_QUERY_SAVED)) {
            setSaved();
        } else if(evt.getPropertyName().equals(Query.EVENT_QUERY_REMOVED)) {
            if(query != null && evt.getSource() == query) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        close();
                    }
                });
            }
        } else if(evt.getPropertyName().equals(Repository.EVENT_QUERY_LIST_CHANGED)) {
            updateSavedQueries();
        } else if(evt.getPropertyName().equals(RepositoriesSupport.EVENT_REPOSITORIES_CHANGED)) {
            if(!repositoryComboBox.isEnabled()) {
                // well, looks like there shuold be only one repository available
                return;
            }
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    rs.refreshRepositoryModel();
                }
            });
        }
    }

    public void started() {
        /* the query was started */
        assert query != null;
        if (query == null) {
            return;
        }

        assert query.getRepository() != null;

        BugtrackingOwnerSupport.getInstance().setLooseAssociation(
                BugtrackingOwnerSupport.ContextType.SELECTED_FILE_AND_ALL_PROJECTS,
                query.getRepository());
    }

    public void notifyData(Issue issue) {
        /* some (partial) results for the query are available */
    }

    public void finished() {
        /* the query was finished */
    }

    public void focusGained(FocusEvent e) {
        Component c = e.getComponent();
        if(c instanceof JComponent) {
            Point p = SwingUtilities.convertPoint(c.getParent(), c.getLocation(), repoPanel);
            final Rectangle r = new Rectangle(p, c.getSize());
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    repoPanel.scrollRectToVisible(r);
                }
            });
        }
    }

    public void focusLost(FocusEvent e) {
        // do nothing
    }

    final static class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;

        public Object readResolve() {
            return QueryTopComponent.getDefault();
        }
    }

    /***********
     * PRIVATE *
     ***********/

    private void onNewClick() {
        Repository repo = BugtrackingUtil.createRepository();
        if(repo != null) {
            repositoryComboBox.addItem(repo);
            repositoryComboBox.setSelectedItem(repo);
        }
    }

    private void onRepoSelected() {
        if(prepareTask != null) {
            prepareTask.cancel();
        }
        Cancellable c = new Cancellable() {
            public boolean cancel() {
                if(prepareTask != null) {
                    prepareTask.cancel();
                }
                return true;
            }
        };
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(QueryTopComponent.class, "CTL_PreparingQuery"), c); // NOI18N
        prepareTask = rp.post(new Runnable() {
            public void run() {
                try {
                    handle.start();
                    Repository repo = getRepository();
                    if(repo == null) {
                        return;
                    }
                    repo.addPropertyChangeListener(QueryTopComponent.this);

                    final BugtrackingController removeController = query != null ? query.getController() : null;
                    if(query != null) {
                        query.removePropertyChangeListener(QueryTopComponent.this);
                        query.removeNotifyListener(QueryTopComponent.this);
                    }

                    query = repo.createQuery();
                    if (query == null) {
                        return;
                    }
                    query.addPropertyChangeListener(QueryTopComponent.this);
                    query.addNotifyListener(QueryTopComponent.this);

                    updateSavedQueriesIntern(repo);

                    final BugtrackingController addController = query.getController();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            if(removeController != null) {
                                panel.remove(removeController.getComponent());
                            }
                            panel.add(addController.getComponent());
                            panel.revalidate();
                            panel.repaint();

                            focusFirstEnabledComponent();

                        }
                    });
                } finally {
                    handle.finish();
                    prepareTask = null;
                }
            }

        });
    }

    private Repository getRepository() {
        Object item = repositoryComboBox.getSelectedItem();
        if (item == null || !(item instanceof Repository)) {
            return null;
        }
        return (Repository) item;
    }

    private void focusFirstEnabledComponent() {
        repositoryComboBox.requestFocusInWindow();
        if(!repositoryComboBox.isEnabled()) {
            newButton.requestFocusInWindow();
            if(!newButton.isEnabled()) {
                newButton.transferFocus();
            }
        }
    }

    private void setNameAndTooltip() throws MissingResourceException {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if(query != null && query.getDisplayName() != null) {
                    setName(NbBundle.getMessage(QueryTopComponent.class, "LBL_QueryName", new Object[]{query.getRepository().getDisplayName(), query.getDisplayName()})); // NOI18N
                    setToolTipText(NbBundle.getMessage(QueryTopComponent.class, "LBL_QueryName", new Object[]{query.getRepository().getDisplayName(), query.getTooltip()})); // NOI18N
                } else {
                    setName(NbBundle.getMessage(QueryTopComponent.class, "CTL_QueryTopComponent")); // NOI18N
                    setToolTipText(NbBundle.getMessage(QueryTopComponent.class, "HINT_QueryTopComponent")); // NOI18N
                }
            }
        });
    }

    private void setSaved() {
        repoPanel.setVisible(false);
        setNameAndTooltip();
    }

    public void updateSavedQueries() {
        final Repository repo = getRepository();
        if(repo == null) {
            return;
        }
        rp.post(new Runnable() {
            public void run() {
                updateSavedQueriesIntern(repo);
            }
        });        
    }

    private void updateSavedQueriesIntern(Repository repo) {
        if(repo == null) {
            return;
        }
        BugtrackingManager.LOG.log(Level.FINE, "updateSavedQueries for {0} start", new Object[] {repo.getDisplayName()} );
        synchronized (LOCK) {
            if(savedQueries != null) {
                for (Query q : savedQueries) {
                    q.removePropertyChangeListener(this);
                }
            }
        }
        Query[] queries = repo.getQueries();
        synchronized (LOCK) {
            savedQueries = queries;
            if(savedQueries == null || savedQueries.length == 0) {
                queriesPanel.setVisible(false);
                BugtrackingManager.LOG.log(Level.FINE, "updateSavedQueries for {0} finnished. No queries.", new Object[] {repo.getDisplayName()} );
                return;
            }
            queriesPanel.setVisible(true);
            Component[] componenets = queriesPanel.getComponents();
            for (Component c : componenets) {
                if(c instanceof QueryButton || c instanceof JSeparator) {
                    queriesPanel.remove(c);
                }
            }
            queriesPanel.setLayout(new GroupieFlowLayout(GroupieFlowLayout.LEFT));
            QueryButton ql = null;
            Arrays.sort(savedQueries);
            for (int i = 0; i < savedQueries.length; i++) {
                Query q = savedQueries[i];
                q.addPropertyChangeListener(this);
                ql = new QueryButton(repo, q);
                ql.addFocusListener(this);
                ql.setText(q.getDisplayName());
                queriesPanel.add(ql);
                if(i < savedQueries.length - 1) {
                    JSeparator s = new JSeparator();
                    s.setOrientation(javax.swing.SwingConstants.VERTICAL);
                    s.setPreferredSize(new Dimension(2, ql.getPreferredSize().height));
                    s.setBorder(new LineBorder(Color.BLACK, 1));
                    queriesPanel.add(s);
                }
            }
            updateSavedQueriesPanel();
            BugtrackingManager.LOG.log(Level.FINE, "updateSavedQueries for {0} finnished. {1} saved queries.", new Object[] {repo.getDisplayName(), savedQueries.length} );
        }
    }

    private void updateSavedQueriesPanel() {
        LayoutManager lm = queriesPanel.getLayout();
        if (lm instanceof GroupieFlowLayout) {
            GroupieFlowLayout dl = (GroupieFlowLayout) lm;
            int h = dl.getHeight(queriesPanel);
            if(h == 0) return;
            Dimension d = queriesPanel.getSize();
            d.height = h;
            queriesPanel.setSize(d);
            d = queriesPanel.getPreferredSize();
            d.height = h;
            queriesPanel.setPreferredSize(d);

            queriesPanel.revalidate();
            queriesPanel.repaint();
        }
    }

    public class GroupieFlowLayout extends FlowLayout {

        public GroupieFlowLayout(int a) {
            super(a);
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            Dimension d = super.minimumLayoutSize(target);
            d.width = 0;
            return d;
        }

        public int getHeight(Container target) {
            if(target.getWidth() == 0) {
                return 0;
            }
            Insets insets = target.getInsets();
            int maxwidth = target.getWidth() - (insets.left + insets.right + getHgap()*2);
            int nmembers = target.getComponentCount();
            int x = 0, y = insets.top + getVgap();
            int rowh = 0;

            for (int i = 0 ; i < nmembers ; i++) {
                Component m = target.getComponent(i);
                if (m.isVisible()) {
                    Dimension d = m.getPreferredSize();
                    m.setSize(d.width, d.height);

                    if ((x == 0) || ((x + d.width) <= maxwidth)) {
                        if (x > 0) {
                            x += getHgap();
                        }
                        x += d.width;
                        rowh = Math.max(rowh, d.height);
                    } else {
                        x = d.width;
                        y += getVgap() + rowh;
                        rowh = d.height;
                    }
                }
            }
            return y + rowh + getVgap();
        }
    }

    private class QueryButton extends LinkButton {
        public QueryButton(final Repository repo, final Query query) {
            super();
            setText(query.getDisplayName());
            getAccessibleContext().setAccessibleDescription(query.getTooltip());
            this.setAction(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    QueryAction.openQuery(query, repo);
                }
            });
        }
    }

}

