/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.debugger.ui.views;

import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import javax.swing.JMenuItem;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position.Bias;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Session;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.SessionProvider;
import org.netbeans.spi.viewmodel.Model;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.ExtendedNodeModelFilter;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.NodeModelFilter;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeExpansionModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.Models.CompoundModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.datatransfer.PasteType;


/**
 * This delegating CompoundModelImpl loads all models from DebuggerManager.
 * getDefault ().getCurrentEngine ().lookup (viewType, ..) lookup.
 *
 * <p>
 * This class is identical to org.netbeans.modules.debugger.jpda.ui.views.ViewModelListener.
 *
 * @author   Jan Jancura
 */
public class ViewModelListener extends DebuggerManagerAdapter {
    
    private String          viewType;
    private JComponent      view;
    private JComponent      buttonsPane;
    private List models = new ArrayList(11);

    private List<? extends SessionProvider> sessionProviders;
    private Session currentSession;
    private List treeModels;
    private List treeModelFilters;
    private List treeExpansionModels;
    private List nodeModels;
    private List nodeModelFilters;
    private List tableModels;
    private List tableModelFilters;
    private List nodeActionsProviders;
    private List nodeActionsProviderFilters;
    private List columnModels;
    private List mm;
    private RequestProcessor rp;

    private List<? extends javax.swing.AbstractButton> buttons;
    private javax.swing.JTabbedPane tabbedPane;
    private Image viewIcon;
    private SessionProvider providerToDisplay;
    private List<ViewModelListener> subListeners = new ArrayList<ViewModelListener>();

    private Preferences preferences = NbPreferences.forModule(ContextProvider.class).node("variables_view");
    private ViewPreferenceChangeListener prefListener = new ViewPreferenceChangeListener();
    
    // <RAVE>
    // Store the propertiesHelpID to pass to the Model object that is
    // used in generating the nodes for the view
    private String propertiesHelpID = null;
    
    ViewModelListener(
        String viewType,
        JComponent view,
        JComponent buttonsPane,
        String propertiesHelpID,
        Image viewIcon
    ) {
        this.viewType = viewType;
        this.view = view;
        this.buttonsPane = buttonsPane;
        this.propertiesHelpID = propertiesHelpID;
        this.viewIcon = viewIcon;
        setUp();
    }
    // </RAVE>
    
    void setUp() {
        DebuggerManager.getDebuggerManager ().addDebuggerListener (
            DebuggerManager.PROP_CURRENT_ENGINE,
            this
        );
        preferences.addPreferenceChangeListener(prefListener);
        updateModel ();
    }

    synchronized void destroy () {
        DebuggerManager.getDebuggerManager ().removeDebuggerListener (
            DebuggerManager.PROP_CURRENT_ENGINE,
            this
        );
        preferences.removePreferenceChangeListener(prefListener);
        models.clear();
        treeModels = null;
        treeModelFilters = null;
        treeExpansionModels = null;
        nodeModels = null;
        nodeModelFilters = null;
        tableModels = null;
        tableModelFilters = null;
        nodeActionsProviders = null;
        nodeActionsProviderFilters = null;
        columnModels = null;
        mm = null;
        rp = null;
        sessionProviders = null;
        currentSession = null;
        providerToDisplay = null;
        buttonsPane.removeAll();
        buttons = null;
        view.removeAll();
        for (ViewModelListener l : subListeners) {
            l.destroy();
        }
        subListeners.clear();
    }

    @Override
    public void propertyChange (PropertyChangeEvent e) {
        if (e.getNewValue() != null) {
            synchronized (this) {
                // Reset the provider to display the current one.
                providerToDisplay = null;
            }
        }
        updateModel ();
    }
    
    private synchronized void updateModel () {
        DebuggerManager dm = DebuggerManager.getDebuggerManager ();
        DebuggerEngine e = dm.getCurrentEngine ();
        if (e == null) {
            sessionProviders = dm.lookup (viewType, SessionProvider.class);
        } else {
            sessionProviders = DebuggerManager.join(e, dm).lookup (viewType, SessionProvider.class);
        }
        if (!sessionProviders.contains(providerToDisplay)) {
            providerToDisplay = null;
        }
        if (e == null && providerToDisplay == null && sessionProviders.size() > 0) {
            providerToDisplay = sessionProviders.get(0);
        }
        ContextProvider cp;
        String viewPath;
        if (providerToDisplay != null) {
            e = null;
            cp = dm;
            viewPath = viewType + "/" + providerToDisplay.getTypeID();
        } else {
            cp = e != null ? DebuggerManager.join(e, dm) : dm;
            viewPath = viewType;
        }

        currentSession =        dm.getCurrentSession();
        
        treeModels =            cp.lookup (viewPath, TreeModel.class);
        treeModelFilters =      cp.lookup (viewPath, TreeModelFilter.class);
        treeExpansionModels =   cp.lookup (viewPath, TreeExpansionModel.class);
        nodeModels =            cp.lookup (viewPath, NodeModel.class);
        nodeModelFilters =      cp.lookup (viewPath, NodeModelFilter.class);
        tableModels =           cp.lookup (viewPath, TableModel.class);
        tableModelFilters =     cp.lookup (viewPath, TableModelFilter.class);
        nodeActionsProviders =  cp.lookup (viewPath, NodeActionsProvider.class);
        nodeActionsProviderFilters = cp.lookup (viewPath, NodeActionsProviderFilter.class);
        columnModels =          cp.lookup (viewPath, ColumnModel.class);
        mm =                    cp.lookup (viewPath, Model.class);
        rp = (e != null) ? e.lookupFirst(null, RequestProcessor.class) : null;

        if ("LocalsView".equals(viewType)) {
            if (preferences.getBoolean(VariablesViewButtons.SHOW_WATCHES, false)) {
                TreeModelFilter showWatchesFilter = createWatchesViewCompoundModel(cp);
                treeModelFilters.add(showWatchesFilter);
                nodeModelFilters.add(showWatchesFilter);
                nodeActionsProviderFilters.add(showWatchesFilter);
            }
//            if (preferences.getBoolean(VariablesViewButtons.SHOW_EVALUTOR_RESULT, false)) {
//                TreeModelFilter showResultFilter = createResultViewCompoundModel(cp);
//                treeModelFilters.add(showResultFilter);
//                nodeModelFilters.add(showResultFilter);
//                nodeActionsProviderFilters.add(showResultFilter);
//            }
        }

        buttons = cp.lookup(viewPath, javax.swing.AbstractButton.class);
        tabbedPane = cp.lookupFirst(viewPath, javax.swing.JTabbedPane.class);
        
        ModelsChangeRefresher mcr = new ModelsChangeRefresher();
        Customizer[] modelListCustomizers = new Customizer[] {
            (Customizer) treeModels,
            (Customizer) treeModelFilters,
            (Customizer) treeExpansionModels,
            (Customizer) nodeModels,
            (Customizer) nodeModelFilters,
            (Customizer) tableModels,
            (Customizer) tableModelFilters,
            (Customizer) nodeActionsProviders,
            (Customizer) nodeActionsProviderFilters,
            (Customizer) columnModels,
            (Customizer) mm
        };
        for (int i = 0; i < modelListCustomizers.length; i++) {
            Customizer c = modelListCustomizers[i];
            if (c != null) { // Can be null when debugger is finishing
                c.addPropertyChangeListener(mcr);
                c.setObject("load first"); // NOI18N
                c.setObject("unload last"); // NOI18N
            }
        }
        
        refreshModel();
    }
    
    private synchronized void refreshModel() {
        models.clear();
        if (treeModels == null) {
            // Destroyed
            return ;
        }
        synchronized (treeModels) {
            models.add(new ArrayList(treeModels));
        }
        synchronized (treeModelFilters) {
            models.add(new ArrayList(treeModelFilters));
        }
        synchronized (treeExpansionModels) {
            models.add(new ArrayList(treeExpansionModels));
        }
        synchronized (nodeModels) {
            models.add(new ArrayList(nodeModels));
        }
        synchronized (nodeModelFilters) {
            models.add(new ArrayList(nodeModelFilters));
        }
        synchronized (tableModels) {
            models.add(new ArrayList(tableModels));
        }
        synchronized (tableModelFilters) {
            models.add(new ArrayList(tableModelFilters));
        }
        synchronized (nodeActionsProviders) {
            models.add(new ArrayList(nodeActionsProviders));
        }
        synchronized (nodeActionsProviderFilters) {
            models.add(new ArrayList(nodeActionsProviderFilters));
        }
        synchronized (columnModels) {
            models.add(new ArrayList(columnModels));
        }
        synchronized (mm) {
            models.add(new ArrayList(mm));
        }
        if (rp != null) {
            models.add(rp);
        }

        final JComponent buttonsSubPane;
        synchronized (buttons) {
            buttonsPane.removeAll();
            if (buttons.size() == 0 && sessionProviders.size() == 0) {
                buttonsPane.setVisible(false);
                buttonsSubPane = null;
            } else {
                buttonsPane.setVisible(true);
                int i = 0;
                if (sessionProviders.size() > 0) {
                    javax.swing.AbstractButton b = createSessionsSwitchButton();
                    GridBagConstraints c = new GridBagConstraints(0, i, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, 0, new Insets(5, 5, 5, 5), 0, 0);
                    buttonsPane.add(b, c);
                    i++;
                    javax.swing.JSeparator s = new javax.swing.JSeparator(SwingConstants.HORIZONTAL);
                    c = new GridBagConstraints(0, i, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, 0, new Insets(5, 0, 5, 0), 0, 0);
                    buttonsPane.add(s, c);
                    i++;
                }
                if (tabbedPane != null) {
                    buttonsSubPane = new javax.swing.JPanel();
                    buttonsSubPane.setLayout(new java.awt.GridBagLayout());
                    GridBagConstraints c = new GridBagConstraints(0, i, 1, 1, 0.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.VERTICAL, new Insets(5, 0, 5, 0), 0, 0);
                    buttonsPane.add(buttonsSubPane, c);
                    i++;
                } else {
                    buttonsSubPane = null;
                    for (javax.swing.AbstractButton b : buttons) {
                        GridBagConstraints c = new GridBagConstraints(0, i, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, 0, new Insets(5, 5, 5, 5), 0, 0);
                        buttonsPane.add(b, c);
                        i++;
                    }
                }
                //GridBagConstraints c = new GridBagConstraints(0, i, 1, 1, 0.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.VERTICAL, new Insets(5, 5, 5, 5), 0, 0);
                //buttonsPane.add(new javax.swing.JPanel(), c); // Push-panel

                // [TODO]
                //GridBagConstraints c = new GridBagConstraints(1, 0, 1, i + 1, 0.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0);
                //buttonsPane.add(new javax.swing.JSeparator(SwingConstants.VERTICAL), c); // Components separator, border-like
            }
        }
        
        // <RAVE>
        // Store the propertiesHelpID in the tree model to be retrieved later
        // by the TreeModelNode objects
        // Models.setModelsToView (
        //    view,
        //    Models.createCompoundModel (models)
        // );
        // ====

        final boolean haveModels = treeModels.size() > 0 || nodeModels.size() > 0 || tableModels.size() > 0;
        final Models.CompoundModel newModel;
        if (haveModels) {
            newModel = Models.createCompoundModel (models, propertiesHelpID);
        } else {
            newModel = null;
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (view.getComponentCount() > 0) {
                    if (tabbedPane == null && view.getComponent(0) instanceof javax.swing.JTabbedPane) {
                        view.removeAll();
                    } else if (tabbedPane != null) {
                        view.removeAll();
                    }
                }
                if (view.getComponentCount() == 0) {
                    if (haveModels) {
                        view.add(Models.createView(newModel));
                        view.revalidate();
                        view.repaint();
                    } else if (tabbedPane != null) {
                        int n = tabbedPane.getTabCount();
                        for (int i = 0; i < n; i++) {
                            java.awt.Component c = tabbedPane.getComponentAt(i);
                            if (c instanceof javax.swing.JPanel) {
                                c = (java.awt.Component) ((javax.swing.JPanel) c).getClientProperty(javax.swing.JLabel.class.getName());
                            }
                            if (c instanceof javax.swing.JLabel) {
                                String id = ((javax.swing.JLabel) c).getText();
                                if (providerToDisplay != null) {
                                    id = providerToDisplay.getTypeID() + "/" + id;
                                }
                                javax.swing.JPanel contentComponent = new javax.swing.JPanel(new java.awt.BorderLayout ());
                                subListeners.add(new ViewModelListener (
                                    viewType + "/" + id,
                                    contentComponent,
                                    buttonsSubPane,
                                    propertiesHelpID,
                                    viewIcon
                                ));
                                tabbedPane.setComponentAt(i, contentComponent);
                                contentComponent.putClientProperty(javax.swing.JLabel.class.getName(), c);
                            }
                        }
                        view.add(tabbedPane);
                        view.revalidate();
                        view.repaint();
                    }
                } else if (tabbedPane == null) {
                    if (!haveModels) {
                        view.removeAll();
                        view.revalidate();
                        view.repaint();
                    } else {
                        JComponent tree = (JComponent) view.getComponent(0);
                        Models.setModelsToView (
                            tree,
                            newModel
                        );
                    }
                }
            }
        });
        // </RAVE>
    }

    private javax.swing.JButton createSessionsSwitchButton() {
        final javax.swing.JButton b = new javax.swing.JButton(new ImageIcon(viewIcon));
        b.setMargin(new Insets(2, 2, 2, 2));
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == b) {
                    javax.swing.JPopupMenu m = new javax.swing.JPopupMenu();
                    if (currentSession != null) {
                        JMenuItem mi = new JMenuItem(currentSession.getName());
                        mi.putClientProperty("SESSION", currentSession);
                        mi.addActionListener(this);
                        m.add(mi);
                    }
                    for (SessionProvider sp : sessionProviders) {
                        JMenuItem mi = new JMenuItem(sp.getSessionName());
                        mi.putClientProperty("SESSION", sp);
                        mi.addActionListener(this);
                        m.add(mi);
                    }
                    java.awt.Point pos = b.getMousePosition();
                    if (pos == null) {
                        pos = new java.awt.Point(b.getWidth(), b.getHeight());
                    }
                    m.show(b, pos.x, pos.y);
                } else {
                    JMenuItem mi = (JMenuItem) e.getSource();
                    Object s = mi.getClientProperty("SESSION");
                    synchronized (ViewModelListener.this) {
                        if (s instanceof Session) {
                            providerToDisplay = null;
                        } else {
                            providerToDisplay = (SessionProvider) s;
                        }
                    }
                    updateModel();
                }
            }
        });
        return b;
    }
    
    // innerclasses ............................................................

    private TreeModelFilter createWatchesViewCompoundModel (ContextProvider cp) {
        List treeModels;
        List treeModelFilters;
        List treeExpansionModels;
        List nodeModels;
        List nodeModelFilters;
        List tableModels;
        List tableModelFilters;
        List nodeActionsProviders;
        List nodeActionsProviderFilters;
        List columnModels;
        List mm;
        String viewName = View.WATCHES_VIEW_NAME;
        treeModels =            cp.lookup (viewName, TreeModel.class);
        treeModelFilters =      cp.lookup (viewName, TreeModelFilter.class);
        treeExpansionModels =   cp.lookup (viewName, TreeExpansionModel.class);
        nodeModels =            cp.lookup (viewName, NodeModel.class);
        nodeModelFilters =      cp.lookup (viewName, NodeModelFilter.class);
        tableModels =           cp.lookup (viewName, TableModel.class);
        tableModelFilters =     cp.lookup (viewName, TableModelFilter.class);
        nodeActionsProviders =  cp.lookup (viewName, NodeActionsProvider.class);
        nodeActionsProviderFilters = cp.lookup (viewName, NodeActionsProviderFilter.class);
        columnModels =          cp.lookup (viewName, ColumnModel.class);
        mm =                    cp.lookup (viewName, Model.class);

        List treeNodeModelsCompound = new ArrayList(11);
        treeNodeModelsCompound.add(treeModels);
        treeNodeModelsCompound.add(treeModelFilters);
        treeNodeModelsCompound.add(Collections.EMPTY_LIST); // TreeExpansionModel
        treeNodeModelsCompound.add(nodeModels);
        treeNodeModelsCompound.add(nodeModelFilters);
        treeNodeModelsCompound.add(Collections.EMPTY_LIST); // TableModel
        treeNodeModelsCompound.add(Collections.EMPTY_LIST); // TableModelFilter
        treeNodeModelsCompound.add(nodeActionsProviders);
        treeNodeModelsCompound.add(nodeActionsProviderFilters);
        treeNodeModelsCompound.add(Collections.EMPTY_LIST); // ColumnModel
        treeNodeModelsCompound.add(Collections.EMPTY_LIST); // Model

        CompoundModel treeNodeModel = Models.createCompoundModel(treeNodeModelsCompound);
        List nodeModelsCompound = new ArrayList(11);
        nodeModelsCompound.add(nodeModels);
        // nodeModelsCompound.add(new ArrayList()); // An empty tree model will be added
//        for (int i = 0; i < 2; i++) {
//            nodeModelsCompound.add(Collections.EMPTY_LIST);
//        }
//        nodeModelsCompound.add(nodeModels);
//        for (int i = 0; i < 7; i++) {
//            nodeModelsCompound.add(Collections.EMPTY_LIST);
//        }
//        CompoundModel nodeModel = Models.createCompoundModel(nodeModelsCompound);

        TreeModelFilter nestedTreeModel = new NestedTreeModelFilter(
                treeNodeModel, treeNodeModel, treeNodeModel, false);

        return nestedTreeModel;
    }

    private TreeModelFilter createResultViewCompoundModel (ContextProvider cp) {
        List treeModels;
        List treeModelFilters;
        List treeExpansionModels;
        List nodeModels;
        List nodeModelFilters;
        List tableModels;
        List tableModelFilters;
        List nodeActionsProviders;
        List nodeActionsProviderFilters;
        List columnModels;
        List mm;
        String viewName = View.RESULTS_VIEW_NAME;
        treeModels =            cp.lookup (viewName, TreeModel.class);
        treeModelFilters =      cp.lookup (viewName, TreeModelFilter.class);
        treeExpansionModels =   cp.lookup (viewName, TreeExpansionModel.class);
        nodeModels =            cp.lookup (viewName, NodeModel.class);
        nodeModelFilters =      cp.lookup (viewName, NodeModelFilter.class);
        tableModels =           cp.lookup (viewName, TableModel.class);
        tableModelFilters =     cp.lookup (viewName, TableModelFilter.class);
        nodeActionsProviders =  cp.lookup (viewName, NodeActionsProvider.class);
        nodeActionsProviderFilters = cp.lookup (viewName, NodeActionsProviderFilter.class);
        columnModels =          cp.lookup (viewName, ColumnModel.class);
        mm =                    cp.lookup (viewName, Model.class);

        List treeNodeModelsCompound = new ArrayList(11);
        treeNodeModelsCompound.add(treeModels);
        treeNodeModelsCompound.add(treeModelFilters != null ? treeModelFilters : Collections.EMPTY_LIST);
        treeNodeModelsCompound.add(Collections.EMPTY_LIST); // TreeExpansionModel
        treeNodeModelsCompound.add(nodeModels != null ? nodeModels : Collections.EMPTY_LIST);
        treeNodeModelsCompound.add(Collections.EMPTY_LIST);
        treeNodeModelsCompound.add(tableModels != null ? tableModels : Collections.EMPTY_LIST); // TableModel
        treeNodeModelsCompound.add(tableModelFilters != null ? tableModelFilters : Collections.EMPTY_LIST); // TableModelFilter
        treeNodeModelsCompound.add(Collections.EMPTY_LIST);
        treeNodeModelsCompound.add(Collections.EMPTY_LIST);
        treeNodeModelsCompound.add(Collections.EMPTY_LIST); // ColumnModel
        treeNodeModelsCompound.add(Collections.EMPTY_LIST); // Model

        CompoundModel treeNodeModel = Models.createCompoundModel(treeNodeModelsCompound);
        List nodeModelsCompound = new ArrayList(11);
        nodeModelsCompound.add(nodeModels);
        // nodeModelsCompound.add(new ArrayList()); // An empty tree model will be added
//        for (int i = 0; i < 2; i++) {
//            nodeModelsCompound.add(Collections.EMPTY_LIST);
//        }
//        nodeModelsCompound.add(nodeModels);
//        for (int i = 0; i < 7; i++) {
//            nodeModelsCompound.add(Collections.EMPTY_LIST);
//        }
//        CompoundModel nodeModel = Models.createCompoundModel(nodeModelsCompound);

        TreeModelFilter nestedTreeModel = new NestedTreeModelFilter(
                treeNodeModel, treeNodeModel, treeNodeModel, false);

        return nestedTreeModel;
    }

    private static class NestedTreeModelFilter implements TreeModelFilter, ExtendedNodeModelFilter,
        NodeActionsProviderFilter {

        protected CompoundModel treeModel;
        protected CompoundModel nodeModel;
        protected CompoundModel actionModel;
        private boolean isResultModel;

        NestedTreeModelFilter (CompoundModel treeModel, CompoundModel nodeModel,
                CompoundModel actionModel, boolean isResultModel) {
            this.treeModel = treeModel;
            this.nodeModel = nodeModel;
            this.actionModel = actionModel;
            this.isResultModel = isResultModel;
        }

        public Object getRoot(TreeModel original) {
            return original.getRoot();
        }

        public Object[] getChildren(TreeModel original, Object parent, int from, int to) throws UnknownTypeException {
            Object[] children = original.getChildren(parent, from, to);
            if (parent == TreeModel.ROOT) {
//                if (isResultModel) {
//                    Object[] wChildren = treeModel.getChildren(parent, from, to);
//                    Object[] union = new Object[children.length + wChildren.length];
//                    System.arraycopy(wChildren, 0, union, 0, wChildren.length);
//                    System.arraycopy(children, 0, union, wChildren.length, children.length);
//                    children = union;
//                } else {
                    Object[] wChildren = treeModel.getChildren(parent, from, to);
                    Object[] union = new Object[children.length + wChildren.length];
                    System.arraycopy(children, 0, union, 0, children.length);
                    System.arraycopy(wChildren, 0, union, children.length, wChildren.length);
                    children = union;
//                }
            }
            return children;
        }

        public int getChildrenCount(TreeModel original, Object node) throws UnknownTypeException {
            return Integer.MAX_VALUE; // [TODO]
        }

        public boolean isLeaf(TreeModel original, Object node) throws UnknownTypeException {
            try {
                return treeModel.isLeaf(node);
            } catch (UnknownTypeException e) {
                return original.isLeaf(node);
            }
        }

        public void addModelListener(ModelListener listener) {
            treeModel.addModelListener(listener);
        }

        public void removeModelListener(ModelListener listener) {
            treeModel.removeModelListener(listener);
        }

        public boolean canRename(ExtendedNodeModel original, Object node) throws UnknownTypeException {
            try {
                return nodeModel.canRename(node);
            } catch (UnknownTypeException e) {
                return original.canRename(node);
            }
        }

        public boolean canCopy(ExtendedNodeModel original, Object node) throws UnknownTypeException {
            try {
                return nodeModel.canCopy(node);
            } catch (UnknownTypeException e) {
                return original.canCopy(node);
            }
        }

        public boolean canCut(ExtendedNodeModel original, Object node) throws UnknownTypeException {
            try {
                return nodeModel.canCut(node);
            } catch (UnknownTypeException e) {
                return original.canCut(node);
            }
        }

        public Transferable clipboardCopy(ExtendedNodeModel original, Object node) throws IOException, UnknownTypeException {
            try {
                return nodeModel.clipboardCopy(node);
            } catch (UnknownTypeException e) {
                return original.clipboardCopy(node);
            }
        }

        public Transferable clipboardCut(ExtendedNodeModel original, Object node) throws IOException, UnknownTypeException {
            try {
                return nodeModel.clipboardCut(node);
            } catch (UnknownTypeException e) {
                return original.clipboardCut(node);
            }
        }

        public PasteType[] getPasteTypes(ExtendedNodeModel original, Object node, Transferable t) throws UnknownTypeException {
            try {
                return nodeModel.getPasteTypes(node, t);
            } catch (UnknownTypeException e) {
                return original.getPasteTypes(node, t);
            }
        }

        public void setName(ExtendedNodeModel original, Object node, String name) throws UnknownTypeException {
            try {
                nodeModel.setName(node, name);
            } catch (UnknownTypeException e) {
                original.setName(node, name);
            }
        }

        public String getIconBaseWithExtension(ExtendedNodeModel original, Object node) throws UnknownTypeException {
            try {
                return nodeModel.getIconBaseWithExtension(node);
            } catch (UnknownTypeException e) {
                return original.getIconBaseWithExtension(node);
            }
        }

        public String getDisplayName(NodeModel original, Object node) throws UnknownTypeException {
            try {
                return nodeModel.getDisplayName(node);
            } catch (UnknownTypeException e) {
                return original.getDisplayName(node);
            }
        }

        public String getIconBase(NodeModel original, Object node) throws UnknownTypeException {
            try {
                return nodeModel.getIconBase(node);
            } catch (UnknownTypeException e) {
                return original.getIconBase(node);
            }
        }

        public String getShortDescription(NodeModel original, Object node) throws UnknownTypeException {
            try {
                return nodeModel.getShortDescription(node);
            } catch (UnknownTypeException e) {
                return original.getShortDescription(node);
            }
        }

        public void performDefaultAction(NodeActionsProvider original, Object node) throws UnknownTypeException {
            try {
                actionModel.performDefaultAction(node);
            } catch (UnknownTypeException e) {
                original.performDefaultAction(node);
            }
        }

        public Action[] getActions(NodeActionsProvider original, Object node) throws UnknownTypeException {
            Action[] origActions = original.getActions(node);
            try {
                Action[] actions = actionModel.getActions(node);
                Action[] result = new Action[origActions.length + actions.length];
                System.arraycopy(origActions, 0, result, 0, origActions.length);
                System.arraycopy(actions, 0, result, origActions.length, actions.length);
                return result;
            } catch (UnknownTypeException e) {
                return origActions;
            }
        }

    }

    private class ModelsChangeRefresher implements PropertyChangeListener, Runnable {
        
        private RequestProcessor.Task task;

        public synchronized void propertyChange(PropertyChangeEvent evt) {
            if (task == null) {
                task = new RequestProcessor(ModelsChangeRefresher.class.getName(), 1).create(this);
            }
            task.schedule(1);
        }

        public void run() {
            refreshModel();
        }
        
    }

    private class ViewPreferenceChangeListener implements PreferenceChangeListener {

        public void preferenceChange(PreferenceChangeEvent evt) {
            String key = evt.getKey();
            if (VariablesViewButtons.SHOW_WATCHES.equals(key) ||
                    VariablesViewButtons.SHOW_EVALUTOR_RESULT.equals(key)) {
                updateModel();
            }
        }

    }

}
