/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bugtracking.ui.query;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
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
import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.LayoutStyle;
import static javax.swing.SwingConstants.WEST;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.APIAccessor;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.QueryImpl;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.RepositoryRegistry;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.team.spi.TeamUtil;
import org.netbeans.modules.bugtracking.team.spi.OwnerInfo;
import org.netbeans.modules.bugtracking.spi.QueryController;
import org.netbeans.modules.bugtracking.spi.QueryProvider;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.util.LinkButton;
import org.netbeans.modules.bugtracking.util.LogUtils;
import org.netbeans.modules.bugtracking.util.NBBugzillaUtils;
import org.netbeans.modules.bugtracking.util.NoContentPanel;
import org.netbeans.modules.bugtracking.util.RepositoryComboSupport;
import org.openide.awt.Mnemonics;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

public final class QueryTopComponent extends TopComponent
                                     implements PropertyChangeListener, FocusListener {

    private static QueryTopComponent instance;

    /** Set of opened {@code QueryTopComponent}s. */
    private static final Set<QueryTopComponent> openQueries = new HashSet<QueryTopComponent>();

    private final RepoSelectorPanel repoPanel;
    private final LinkButton newButton;
    private final JComboBox repositoryComboBox;

    private static final String PREFERRED_ID = "QueryTopComponent"; // NOI18N
    private QueryImpl query; // XXX synchronized

    private final RequestProcessor rp = new RequestProcessor("Bugtracking query", 1, true); // NOI18N
    private RequestProcessor.Task prepareTask;
    private RepositoryComboSupport rs;
    private File context;

    QueryTopComponent() {
        initComponents();
        RepositoryRegistry.getInstance().addPropertyChangeListener(this);
        repositoryComboBox = new javax.swing.JComboBox();
        newButton = new LinkButton();

        /* layout */
        Font titleFont = title.getFont();
        title.setFont(titleFont.deriveFont(1.7f * titleFont.getSize()));
        title.setBorder(BorderFactory.createEmptyBorder(
                0, getLeftContainerGap(title), 0, 0));

        leftRepoPanel.setVisible(false);
        repoPanel = new RepoSelectorPanel(repositoryComboBox, newButton);
        
        GroupLayout layout = (GroupLayout) headerPanel.getLayout();
        leftRepoPanel.setVisible(true);
        layout.replace(leftRepoPanel, repoPanel);
                
        addNoContentPanel();
        
        /* texts */
        Mnemonics.setLocalizedText(
                title,
                getBundleText("QueryTopComponent.findIssuesLabel.text"));//NOI18N
        Mnemonics.setLocalizedText(newButton,
                getBundleText("QueryTopComponent.newButton.text_1"));   //NOI18N

        /* accessibility texts */
        repositoryComboBox.getAccessibleContext().setAccessibleDescription(
                getBundleText("QueryTopComponent.repositoryComboBox.AccessibleContext.accessibleDescription")); //NOI18N
        newButton.getAccessibleContext().setAccessibleDescription(
                getBundleText("QueryTopComponent.newButton.AccessibleContext.accessibleDescription")); //NOI18N

        /* background colors */
        Color editorBgColor = UIManager.getDefaults()
                              .getColor("EditorPane.background");       //NOI18N
        repoPanel.setBackground(editorBgColor);
        headerPanel.setBackground(editorBgColor);
        queryPanel.setBackground(editorBgColor);
        mainPanel.setBackground(editorBgColor);

        /* focus */
        repoPanel.setNextFocusableComponent(newButton);

        /* scrolling */
        int unitIncrement = (int) (1.5f * titleFont.getSize() + 0.5f);
        jScrollPane1.getHorizontalScrollBar().setUnitIncrement(unitIncrement);
        jScrollPane1.getVerticalScrollBar().setUnitIncrement(unitIncrement);
    }

    private static int getLeftContainerGap(JComponent comp) {
        LayoutStyle layoutStyle = LayoutStyle.getInstance();
        return layoutStyle.getContainerGap(comp, WEST, null);
    }

    public static Set<QueryTopComponent> getOpenQueries() {
        return openQueries;
    }
    
    public QueryImpl getQuery() {
        return query;
    }

    void init(QueryImpl query, RepositoryImpl defaultRepository, File context, boolean suggestedSelectionOnly) {
        this.query = query;
        this.context = context;

        setNameAndTooltip();

        if(suggestedSelectionOnly) {
            repositoryComboBox.setEnabled(false);
            newButton.setEnabled(false);
        }

        if (query != null) {
            if(query.isSaved()) {
                setSaved();
            } else {
                if(!suggestedSelectionOnly) {
                    rs = RepositoryComboSupport.setup(this, repositoryComboBox, defaultRepository.getRepository());
                }
            }
            QueryController c = getController(query);
            addQueryComponent(c);
            this.query.addPropertyChangeListener(this);
        } else {
            newButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onNewClick();
                }
            });
            repositoryComboBox.addItemListener(new ItemListener() {
                @Override
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
            if(defaultRepository == null) {
                rs = RepositoryComboSupport.setup(this, repositoryComboBox, true);
            } else {
                rs = RepositoryComboSupport.setup(this, repositoryComboBox, defaultRepository.getRepository());
            }
            newButton.addFocusListener(this);
            repositoryComboBox.addFocusListener(this);
        }
        Repository repo = null;
        if(query != null) {
            repo = query.getRepositoryImpl().getRepository();
        } else if(defaultRepository != null) {
            repo = defaultRepository.getRepository();
        }
        LogUtils.logBugtrackingUsage(repo, "ISSUE_QUERY"); // NOI18N
    }

    private QueryController getController(QueryImpl query) {
        return query.getController();
    }

    private static String getBundleText(String key) {
        return NbBundle.getMessage(QueryTopComponent.class, key);
    }

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
    public static synchronized QueryTopComponent find(QueryImpl query) {
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
        if(query != null) {
            getController(query).opened();
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
            getController(query).closed();
        }
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

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(QueryProvider.EVENT_QUERY_SAVED)) {
            setSaved();
        } else if(evt.getPropertyName().equals(QueryProvider.EVENT_QUERY_REMOVED)) {
            if(query != null && query.isData(evt.getSource())) {
                // removed
                closeInAwt();
            }
        } else if(evt.getPropertyName().equals(RepositoryRegistry.EVENT_REPOSITORIES_CHANGED)) {
            if(query != null) {
                Object cOld = evt.getOldValue();
                if(cOld != null &&
                   cOld instanceof Collection)
                {
                    RepositoryImpl thisRepo = query.getRepositoryImpl();
                    if(contains((Collection) cOld, thisRepo)) {
                        // removed
                        closeInAwt();
                    }
                } else if(cOld == null) {
                    RepositoryImpl thisRepo = query.getRepositoryImpl();
                    Collection<RepositoryImpl> knownRepos = RepositoryRegistry.getInstance().getKnownRepositories(true);
                    if(!contains((Collection) knownRepos, thisRepo)) {
                        // removed
                        closeInAwt();
                    }
                }
            }
            if(!repositoryComboBox.isEnabled()) {
                // well, looks like there should be only one repository available
                return;
            }
            runInAWT(new Runnable() {
                @Override
                public void run() {
                    if(rs != null) {
                        rs.refreshRepositoryModel();
                    }
                }
            });
        }
    }

    private boolean contains(Collection c, RepositoryImpl r) {
        for (Object o : c) {
            assert o instanceof RepositoryImpl;
            if(((RepositoryImpl)o).getId().equals(r.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void focusGained(FocusEvent e) {
        Component c = e.getComponent();
        if(c instanceof JComponent) {
            Point p = SwingUtilities.convertPoint(c.getParent(), c.getLocation(), repoPanel);
            final Rectangle r = new Rectangle(p, c.getSize());
            runInAWT(new Runnable() {
                @Override
                public void run() {
                    repoPanel.scrollRectToVisible(r);
                }
            });
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        // do nothing
    }

    private void closeInAwt() {
        runInAWT(new Runnable() {
            @Override
            public void run() {
                close();
            }
        });
    }

    @NbBundle.Messages("LBL_NoRepositorySelected=<no repository selected>")
    private void addNoContentPanel() {
        NoContentPanel ncp = new NoContentPanel();
        ncp.setText(Bundle.LBL_NoRepositorySelected());
        queryPanel.add(ncp);
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
        RepositoryImpl repoImpl = BugtrackingUtil.createRepository();
        if(repoImpl != null) {
            Repository repo = repoImpl.getRepository();
            repositoryComboBox.addItem(repo);
            repositoryComboBox.setSelectedItem(repo);
        }
    }

    private void onRepoSelected() {
        if(prepareTask != null) {
            prepareTask.cancel();
        }
        Cancellable c = new Cancellable() {
            @Override
            public boolean cancel() {
                if(prepareTask != null) {
                    prepareTask.cancel();
                }
                return true;
            }
        };
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(QueryTopComponent.class, "CTL_PreparingQuery"), c); // NOI18N
        prepareTask = rp.post(new Runnable() {
            @Override
            public void run() {
                try {
                    handle.start();
                    RepositoryImpl repo = getRepository();
                    if(repo == null) {
                        return;
                    }
                    repo.addPropertyChangeListener(QueryTopComponent.this);

                    if(query != null) {
                        query.removePropertyChangeListener(QueryTopComponent.this);
                    }

                    query = repo.createNewQuery();
                    if (query == null) {
                        return;
                    }

                    if(context != null && NBBugzillaUtils.isNbRepository(repo.getUrl())) {
                        OwnerInfo ownerInfo = TeamUtil.getOwnerInfo(context);
                        if(ownerInfo != null) {
                            query.setContext(ownerInfo);
                        }
                    }
                    query.addPropertyChangeListener(QueryTopComponent.this);

                    final QueryController addController = getController(query);
                    runInAWT(new Runnable() {
                        @Override
                        public void run() {
                            addQueryComponent(addController);
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

    private void addQueryComponent(QueryController controller) {
        JComponent cmp = controller.getComponent();
        queryPanel.removeAll();
        queryPanel.add(cmp);
        controller.opened();
    }

    private RepositoryImpl getRepository() {
        Object item = repositoryComboBox.getSelectedItem();
        if (item == null || !(item instanceof Repository)) {
            return null;
        }
        return APIAccessor.IMPL.getImpl((Repository)item);
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
        runInAWT(new Runnable() {
            @Override
            public void run() {
                if(query != null && query.getDisplayName() != null) {
                    setName(NbBundle.getMessage(QueryTopComponent.class, "LBL_QueryName", new Object[]{query.getRepositoryImpl().getDisplayName(), query.getDisplayName()})); // NOI18N
                    setToolTipText(NbBundle.getMessage(QueryTopComponent.class, "LBL_QueryName", new Object[]{query.getRepositoryImpl().getDisplayName(), query.getTooltip()})); // NOI18N
                } else {
                    setName(NbBundle.getMessage(QueryTopComponent.class, "CTL_QueryTopComponent")); // NOI18N
                    setToolTipText(NbBundle.getMessage(QueryTopComponent.class, "HINT_QueryTopComponent")); // NOI18N
                }
            }
        });
    }

    private void setSaved() {
        runInAWT(new Runnable() {
            @Override
            public void run() {
                headerPanel.setVisible(false);
                mainPanel.revalidate();
                mainPanel.repaint();
                setNameAndTooltip();
            }
        });
    }

    @Override
    public boolean requestFocusInWindow() {
        return mainPanel.requestFocusInWindow();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        mainPanel = new javax.swing.JPanel();
        headerPanel = new javax.swing.JPanel();
        title = new javax.swing.JLabel();
        leftRepoPanel = new javax.swing.JPanel();
        queryPanel = new javax.swing.JPanel();

        org.openide.awt.Mnemonics.setLocalizedText(title, org.openide.util.NbBundle.getMessage(QueryTopComponent.class, "QueryTopComponent.findIssuesLabel.text")); // NOI18N

        leftRepoPanel.setOpaque(false);

        javax.swing.GroupLayout leftRepoPanelLayout = new javax.swing.GroupLayout(leftRepoPanel);
        leftRepoPanel.setLayout(leftRepoPanelLayout);
        leftRepoPanelLayout.setHorizontalGroup(
            leftRepoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 66, Short.MAX_VALUE)
        );
        leftRepoPanelLayout.setVerticalGroup(
            leftRepoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout headerPanelLayout = new javax.swing.GroupLayout(headerPanel);
        headerPanel.setLayout(headerPanelLayout);
        headerPanelLayout.setHorizontalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addComponent(title)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 203, Short.MAX_VALUE)
                .addComponent(leftRepoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        headerPanelLayout.setVerticalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(leftRepoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(title))
                .addContainerGap())
        );

        queryPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(headerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(queryPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(headerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(queryPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(mainPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel headerPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel leftRepoPanel;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel queryPanel;
    private javax.swing.JLabel title;
    // End of variables declaration//GEN-END:variables

    private static void runInAWT(Runnable r) {
        if(SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

}
