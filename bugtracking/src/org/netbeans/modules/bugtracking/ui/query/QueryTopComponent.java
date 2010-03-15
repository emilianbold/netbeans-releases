/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009-2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.ui.query;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
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
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.spi.QueryNotifyListener;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.util.BugtrackingOwnerSupport;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.util.LinkButton;
import org.netbeans.modules.bugtracking.util.PlaceholderPanel;
import org.netbeans.modules.bugtracking.util.RepositoryComboSupport;
import org.openide.awt.Mnemonics;
import org.openide.nodes.Node;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import static javax.swing.SwingConstants.NORTH;
import static javax.swing.SwingConstants.SOUTH;
import static javax.swing.SwingConstants.WEST;
import static org.jdesktop.layout.LayoutStyle.RELATED;

/**
 * Top component which displays something.
 */
public final class QueryTopComponent extends TopComponent
                                     implements PropertyChangeListener, FocusListener, QueryNotifyListener {

    private static QueryTopComponent instance;
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";

    /** Set of opened {@code QueryTopComponent}s. */
    private static Set<QueryTopComponent> openQueries = new HashSet<QueryTopComponent>();
    private final FindInQuerySupport findInQuerySupport;

    private final RepoPanel repoPanel;
    private final JPanel jPanel2;
    private final LinkButton newButton;
    private final PlaceholderPanel panel;
    private final JComboBox repositoryComboBox;
    private final JScrollPane scrollPane;

    private Query[] savedQueries = null;
    
    private static final String PREFERRED_ID = "QueryTopComponent"; // NOI18N
    private Query query; // XXX synchronized
    private static final Object LOCK = new Object();

    private RequestProcessor rp = new RequestProcessor("Bugtracking query", 1, true); // NOI18N
    private Task prepareTask;
    private RepositoryComboSupport rs;
    private Node[] context;

    QueryTopComponent() {
        BugtrackingConnector[] connectors = BugtrackingManager.getInstance().getConnectors();
        for (BugtrackingConnector c : connectors) {
            c.addPropertyChangeListener(this);
        }
        repositoryComboBox = new javax.swing.JComboBox();
        newButton = new LinkButton();

        /* layout */
        JLabel title = new JLabel();
        Font titleFont = title.getFont();
        title.setFont(titleFont.deriveFont(1.7f * titleFont.getSize()));
        title.setBorder(BorderFactory.createEmptyBorder(
                0, getLeftContainerGap(title), 0, 0));

        repoPanel = new RepoPanel(repositoryComboBox, newButton);
        panel = new PlaceholderPanel();
        jPanel2 = new ViewportWidthAwarePanel(null) {
            @Override
            protected void notifyChildrenOfVisibleWidth() {
                repoPanel.setAvailableWidth(getAvailableWidth());
            }
        };
        jPanel2.setLayout(new BoxLayout(jPanel2, BoxLayout.Y_AXIS));
        jPanel2.add(createVerticalStrut(null, title));
        jPanel2.add(title);
        jPanel2.add(createVerticalStrut(title, repoPanel));
        jPanel2.add(repoPanel);
        jPanel2.add(createVerticalStrut(repoPanel, panel));
        jPanel2.add(panel);

        title    .setAlignmentX(0.0f);
        repoPanel.setAlignmentX(0.0f);
        panel    .setAlignmentX(0.0f);

        scrollPane = new QueryTopComponentScrollPane(jPanel2);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(scrollPane);

         /* find bar */
        findInQuerySupport = FindInQuerySupport.create(this);
        FindInQueryBar findBar = findInQuerySupport.getFindBar();
        findBar.setVisible(false);       
        add(findBar);

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
        panel    .setBackground(editorBgColor);
        jPanel2  .setBackground(editorBgColor);

        /* focus */
        repoPanel.setNextFocusableComponent(newButton);

        /* scrolling */
        int unitIncrement = (int) (1.5f * titleFont.getSize() + 0.5f);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(unitIncrement);
        scrollPane.getVerticalScrollBar().setUnitIncrement(unitIncrement);
    }

    private static int getLeftContainerGap(JComponent comp) {
        LayoutStyle layoutStyle = LayoutStyle.getSharedInstance();
        return layoutStyle.getContainerGap(comp, WEST, null);
    }

    private static Component createVerticalStrut(JComponent above,
                                                 JComponent below) {
        LayoutStyle layoutStyle = LayoutStyle.getSharedInstance();
        int height;
        if (above == null) {
            height = layoutStyle.getContainerGap(below, NORTH, null);
        } else if (below == null) {
            height = layoutStyle.getContainerGap(above, SOUTH, null);
        } else {
            height = layoutStyle.getPreferredGap(above, below,
                                                 RELATED, SOUTH, null);
        }
        return Box.createVerticalStrut(height);
    }

    public static Set<QueryTopComponent> getOpenQueries() {
        return openQueries;
    }
    
    private Query getQuery() {
        return query;
    }

    void init(Query query, Repository defaultRepository, Node[] context, boolean suggestedSelectionOnly) {
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
                    rs = RepositoryComboSupport.setup(this, repositoryComboBox, defaultRepository);
                }
            }
            BugtrackingController c = query.getController();
            panel.setComponent(c.getComponent());
            this.query.addPropertyChangeListener(this);
            this.query.addNotifyListener(this);
            findInQuerySupport.setQuery(query);
        } else {
            newButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    onNewClick();
                }
            });
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
            if(defaultRepository == null) {
                rs = RepositoryComboSupport.setup(this, repositoryComboBox, true);
            } else {
                rs = RepositoryComboSupport.setup(this, repositoryComboBox, defaultRepository);
            }
            newButton.addFocusListener(this);
            repositoryComboBox.addFocusListener(this);
        }
        BugtrackingUtil.logBugtrackingUsage(query != null ? query.getRepository() : defaultRepository, "ISSUE_QUERY"); // NOI18N
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
        } else if(evt.getPropertyName().equals(BugtrackingConnector.EVENT_REPOSITORIES_CHANGED)) {
            if(!repositoryComboBox.isEnabled()) {
                // well, looks like there shuold be only one repository available
                return;
            }
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if(rs != null) {
                        rs.refreshRepositoryModel();
                    }
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

                    if(query != null) {
                        query.removePropertyChangeListener(QueryTopComponent.this);
                        query.removeNotifyListener(QueryTopComponent.this);
                    }

                    query = repo.createQuery();
                    if (query == null) {
                        return;
                    }

                    findInQuerySupport.setQuery(query);

                    QueryAccessor.getInstance().setSelection(query, context);
                    query.addPropertyChangeListener(QueryTopComponent.this);
                    query.addNotifyListener(QueryTopComponent.this);

                    updateSavedQueriesIntern(repo);

                    final BugtrackingController addController = query.getController();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            panel.setComponent(addController.getComponent());

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
        jPanel2.removeAll();
        jPanel2.add(panel);
        jPanel2.revalidate();
        jPanel2.repaint();
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

    private void updateSavedQueriesIntern(final Repository repo) {
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
        final Query[] finQueries;
        synchronized (LOCK) {
            savedQueries = queries;
            Arrays.sort(savedQueries);
            finQueries = savedQueries;
        }

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                repoPanel.setQueries(finQueries);
                if(finQueries == null || finQueries.length == 0) {
                    BugtrackingManager.LOG.log(Level.FINE, "updateSavedQueries for {0} finnished. No queries.", new Object[] {repo.getDisplayName()} );
                } else {
                    BugtrackingManager.LOG.log(Level.FINE, "updateSavedQueries for {0} finnished. {1} saved queries.", new Object[] {repo.getDisplayName(), savedQueries.length} );
                }
            }
        });
    }

    @Override
    public boolean requestFocusInWindow() {
        return jPanel2.requestFocusInWindow();
    }

}
