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

import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Insets;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import javax.swing.JMenuItem;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
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
import org.netbeans.spi.viewmodel.ModelEvent;
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
import org.openide.util.NbBundle;
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

    private List<AbstractButton> buttons;
    private javax.swing.JTabbedPane tabbedPane;
    private Image viewIcon;
    private SessionProvider providerToDisplay;
    private List<ViewModelListener> subListeners = new ArrayList<ViewModelListener>();

    private Preferences preferences = NbPreferences.forModule(ContextProvider.class).node(VariablesViewButtons.PREFERENCES_NAME);
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
        final boolean haveModels = treeModels.size() > 0 || nodeModels.size() > 0 || tableModels.size() > 0;
        if (haveModels && view.getComponentCount() > 0) {
            JComponent tree = (JComponent) view.getComponent(0);
            if (!(tree instanceof javax.swing.JTabbedPane)) {
                Models.setModelsToView(tree, null);
            }
        }
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

     private synchronized void updateModel() {
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

        if (View.LOCALS_VIEW_NAME.equals(viewType) && (VariablesViewButtons.isResultsViewNested() ||
                VariablesViewButtons.isWatchesViewNested())) {
            List modelsList = new ArrayList();
            Set treeModelFiltersSet = new HashSet();
            Set nodeModelFiltersSet = new HashSet();
            Set tableModelFiltersSet = new HashSet();
            Set nodeActionsProviderFiltersSet = new HashSet();
            CompoundModel localsCompound = createCompound(View.LOCALS_VIEW_NAME,
                    treeModelFiltersSet, nodeModelFiltersSet, tableModelFiltersSet,
                    nodeActionsProviderFiltersSet);
            modelsList.add(localsCompound);
            if (VariablesViewButtons.isResultsViewNested()) {
                modelsList.add(createCompound(View.RESULTS_VIEW_NAME,
                        treeModelFiltersSet, nodeModelFiltersSet, tableModelFiltersSet,
                        nodeActionsProviderFiltersSet));
            }
            if (VariablesViewButtons.isWatchesViewNested()) {
                modelsList.add(createCompound(View.WATCHES_VIEW_NAME,
                        treeModelFiltersSet, nodeModelFiltersSet, tableModelFiltersSet,
                        nodeActionsProviderFiltersSet));
            }
            UnionTreeModel unionModel = new UnionTreeModel(modelsList, localsCompound);
            treeModels.clear(); treeModels.add(unionModel);
            treeModelFilters.clear();
            // treeExpansionModels.clear(); treeExpansionModels.add(unionModel);
            nodeModels.clear(); nodeModels.add(unionModel);
            nodeModelFilters.clear();
            tableModels.clear(); tableModels.add(unionModel);
            tableModelFilters.clear();
            nodeActionsProviders.clear(); nodeActionsProviders.add(unionModel);
            nodeActionsProviderFilters.clear();
        }

        List<? extends AbstractButton> bList = cp.lookup(viewPath, AbstractButton.class);
        buttons = new ArrayList<AbstractButton>();
        List tempList = new ArrayList<AbstractButton>();
        for (AbstractButton b : bList) {
            if (b instanceof JToggleButton) { // [TODO]
                buttons.add(b);
            } else {
                tempList.add(b);
            }
        }
        buttons.addAll(tempList);
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
        b.setToolTipText(NbBundle.getMessage(ViewModelListener.class, "Tooltip_SelectSrc"));
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
    
    private CompoundModel createCompound(String viewName, Set treeModelsSet,
            Set nodeModelsSet, Set tableModelsSet, Set actionModelsSet) {
        DebuggerManager dm = DebuggerManager.getDebuggerManager ();
        DebuggerEngine e = dm.getCurrentEngine ();
        List<? extends SessionProvider> localSessionProviders;
        if (e == null) {
            localSessionProviders = dm.lookup (viewName, SessionProvider.class);
        } else {
            localSessionProviders = DebuggerManager.join(e, dm).lookup (viewName, SessionProvider.class);
        }
        if (!localSessionProviders.contains(providerToDisplay)) {
            providerToDisplay = null;
        }
        if (e == null && providerToDisplay == null && localSessionProviders.size() > 0) {
            providerToDisplay = localSessionProviders.get(0);
        }
        ContextProvider cp;
        String viewPath;
        if (providerToDisplay != null) {
            e = null;
            cp = dm;
            viewPath = viewName + "/" + providerToDisplay.getTypeID();
        } else {
            cp = e != null ? DebuggerManager.join(e, dm) : dm;
            viewPath = viewName;
        }

        List treeModels;
        List treeModelFilters;
        List treeExpansionModels;
        List nodeModels;
        List nodeModelFilters;
        List tableModels;
        List tableModelFilters;
        List nodeActionsProviders;
        List nodeActionsProviderFilters;
        //List columnModels;
        //List mm;

        treeModels =            cp.lookup (viewPath, TreeModel.class);
        treeModelFilters =      cp.lookup (viewPath, TreeModelFilter.class);
        treeExpansionModels =   cp.lookup (viewPath, TreeExpansionModel.class);
        nodeModels =            cp.lookup (viewPath, NodeModel.class);
        nodeModelFilters =      cp.lookup (viewPath, NodeModelFilter.class);
        tableModels =           cp.lookup (viewPath, TableModel.class);
        tableModelFilters =     cp.lookup (viewPath, TableModelFilter.class);
        nodeActionsProviders =  cp.lookup (viewPath, NodeActionsProvider.class);
        nodeActionsProviderFilters = cp.lookup (viewPath, NodeActionsProviderFilter.class);
        //columnModels =          cp.lookup (viewPath, ColumnModel.class);
        //mm =                    cp.lookup (viewPath, Model.class);

        treeModelFilters = excludeKnownFilters(treeModelFilters, treeModelsSet);
        nodeModelFilters = excludeKnownFilters(nodeModelFilters, nodeModelsSet);
        tableModelFilters = excludeKnownFilters(tableModelFilters, tableModelsSet);
        nodeActionsProviderFilters = excludeKnownFilters(nodeActionsProviderFilters, actionModelsSet);

        List treeNodeModelsCompound = new ArrayList(11);
        treeNodeModelsCompound.add(treeModels);
        treeNodeModelsCompound.add(treeModelFilters);
        treeNodeModelsCompound.add(treeExpansionModels); // TreeExpansionModel
        treeNodeModelsCompound.add(nodeModels);
        treeNodeModelsCompound.add(nodeModelFilters);
        treeNodeModelsCompound.add(tableModels); // TableModel
        treeNodeModelsCompound.add(tableModelFilters); // TableModelFilter
        treeNodeModelsCompound.add(nodeActionsProviders);
        treeNodeModelsCompound.add(nodeActionsProviderFilters);
        treeNodeModelsCompound.add(Collections.EMPTY_LIST); // ColumnModel
        treeNodeModelsCompound.add(Collections.EMPTY_LIST); // Model

        CompoundModel treeNodeModel = Models.createCompoundModel(treeNodeModelsCompound);

        return treeNodeModel;
    }

    private List excludeKnownFilters(List filters, Set knownFilters) {
        List result = new ArrayList();
        for (Object obj : filters) {
            if (!knownFilters.contains(obj)) {
                result.add(obj);
                knownFilters.add(obj);
            }
        }
        return result;
    }

    // innerclasses .............................................................

    private static class UnionTreeModel implements TreeModel, ExtendedNodeModel,
            NodeActionsProvider, TableModel, TreeExpansionModel, ModelListener {

        private List<CompoundModel> compoundModels;
        private List<CompoundModel> orderedModels; // order for getChildren()
        private final Collection<ModelListener> modelListeners = new HashSet<ModelListener>();

        UnionTreeModel(List<CompoundModel> treeModels, CompoundModel localsCompound) {
            this.compoundModels = treeModels;
            orderedModels = new ArrayList<CompoundModel>(treeModels);
            orderedModels.remove(localsCompound);
            orderedModels.add(localsCompound);
        }

        public Object getRoot() {
            return ROOT;
        }

        public Object[] getChildren(Object parent, int from, int to) throws UnknownTypeException {
            if (parent == TreeModel.ROOT) {
                List chList = new ArrayList();
                for (CompoundModel model : orderedModels) {
                    try {
                        Object[] children = model.getChildren(parent, 0, model.getChildrenCount(parent));
                        for (int x = 0; x < children.length; x++) {
                            // exclude HistoryNode
                            if (!"HistoryNode".equals(children[x].getClass().getSimpleName())) { // NOI18N [TODO]
                                chList.add(children[x]);
                            }
                        }
                    } catch (UnknownTypeException e) {
                    }
                }
                Object[] result = new Object[chList.size()];
                chList.toArray(result);
                if (to > result.length) {
                    to = result.length;
                }
                if (from <= to && (from > 0 || to < result.length)) {
                    Object[] objs = new Object[to - from];
                    System.arraycopy(result, from, objs, 0, objs.length);
                    return objs;
                } else {
                    return result;
                }
            } else {
                for (CompoundModel model : compoundModels) {
                    try {
                        return model.getChildren(parent, from, to);
                    } catch (UnknownTypeException e) {
                    }
                }
            }
            throw new UnknownTypeException(parent);
        }

        public boolean isLeaf(Object node) throws UnknownTypeException {
            boolean isLeaf = false;
            boolean recognized = false;
            for (TreeModel model : compoundModels) {
                try {
                    isLeaf |= model.isLeaf(node);
                    recognized = true;
                } catch (UnknownTypeException e) {
                }
            }
            if (!recognized) {
                throw new UnknownTypeException(node);
            } else {
                return isLeaf;
            }
        }

        public int getChildrenCount(Object node) throws UnknownTypeException {
            if (node == TreeModel.ROOT) {
                int count = 0;
                for (CompoundModel model : compoundModels) {
                    int c = model.getChildrenCount(node);
                    if (c == Integer.MAX_VALUE) {
                        return Integer.MAX_VALUE;
                    }
                    count += c;
                }
                return count;
            } else {
                for (CompoundModel model : compoundModels) {
                    try {
                        return model.getChildrenCount(node);
                    } catch (UnknownTypeException e) {
                    }
                }
            }
            throw new UnknownTypeException(node);
        }

        public void addModelListener (ModelListener listener) {
            synchronized (modelListeners) {
                if (modelListeners.size() == 0) {
                    for (CompoundModel model : compoundModels) {
                        model.addModelListener(listener);
                    }
                }
                modelListeners.add(listener);
            }
        }

        public void removeModelListener (ModelListener listener) {
            synchronized (modelListeners) {
                modelListeners.remove(listener);
                if (modelListeners.size() == 0) {
                    for (CompoundModel model : compoundModels) {
                        model.addModelListener(listener);
                    }
                }
            }
        }

        public void modelChanged(ModelEvent event) {
            ModelEvent newEvent = translateEvent(event, this);
            Collection<ModelListener> listeners;
            synchronized (modelListeners) {
                listeners = new ArrayList<ModelListener>(modelListeners);
            }
            for (Iterator<ModelListener> it = listeners.iterator(); it.hasNext(); ) {
                it.next().modelChanged(newEvent);
            }
        }

        private ModelEvent translateEvent(ModelEvent event, Object newSource) {
            ModelEvent newEvent;
            if (event instanceof ModelEvent.NodeChanged) {
                newEvent = new ModelEvent.NodeChanged(newSource,
                        ((ModelEvent.NodeChanged) event).getNode(),
                        ((ModelEvent.NodeChanged) event).getChange());
            } else if (event instanceof ModelEvent.TableValueChanged) {
                newEvent = new ModelEvent.TableValueChanged(newSource,
                        ((ModelEvent.TableValueChanged) event).getNode(),
                        ((ModelEvent.TableValueChanged) event).getColumnID());
            } else if (event instanceof ModelEvent.TreeChanged) {
                newEvent = new ModelEvent.TreeChanged(newSource);
            } else {
                newEvent = event;
            }
            return newEvent;
        }

        public boolean canRename(Object node) throws UnknownTypeException {
            boolean canRename = false;
            for (CompoundModel model : compoundModels) {
                try {
                    canRename |= model.canRename(node);
                } catch (UnknownTypeException e) {
                }
            }
            return canRename;
        }

        public boolean canCopy(Object node) throws UnknownTypeException {
            boolean copyRename = false;
            for (CompoundModel model : compoundModels) {
                try {
                    copyRename |= model.canCopy(node);
                } catch (UnknownTypeException e) {
                }
            }
            return copyRename;
        }

        public boolean canCut(Object node) throws UnknownTypeException {
            boolean canCut = false;
            for (CompoundModel model : compoundModels) {
                try {
                    canCut |= model.canCut(node);
                } catch (UnknownTypeException e) {
                }
            }
            return canCut;
        }

        public Transferable clipboardCopy(Object node) throws IOException, UnknownTypeException {
            for (CompoundModel model : compoundModels) {
                try {
                    return model.clipboardCopy(node);
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException(node);
        }

        public Transferable clipboardCut(Object node) throws IOException, UnknownTypeException {
            for (CompoundModel model : compoundModels) {
                try {
                    return model.clipboardCut(node);
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException(node);
        }

        public PasteType[] getPasteTypes(Object node, Transferable t) throws UnknownTypeException {
            for (CompoundModel model : compoundModels) {
                try {
                    return model.getPasteTypes(node, t);
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException(node);
        }

        public void setName(Object node, String name) throws UnknownTypeException {
            for (CompoundModel model : compoundModels) {
                try {
                    model.setName(node, name);
                    return;
                } catch (UnknownTypeException e) {
                } catch (UnsupportedOperationException e) {
                }
            }
            throw new UnknownTypeException(node);
        }

        public String getIconBaseWithExtension(Object node) throws UnknownTypeException {
            for (CompoundModel model : compoundModels) {
                try {
                    return model.getIconBaseWithExtension(node);
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException(node);
        }

        public String getDisplayName(Object node) throws UnknownTypeException {
            String result = null;
            for (CompoundModel model : compoundModels) {
                try {
                    result = model.getDisplayName(node);
                    if (result != null && result.trim().length() > 0) {
                        return result;
                    }
                } catch (UnknownTypeException e) {
                }
            }
            if (result == null) {
                throw new UnknownTypeException(node);
            } else {
                return result;
            }
        }

        public String getIconBase(Object node) throws UnknownTypeException {
            for (CompoundModel model : compoundModels) {
                try {
                    return model.getIconBase(node);
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException(node);
        }

        public String getShortDescription(Object node) throws UnknownTypeException {
            for (CompoundModel model : compoundModels) {
                try {
                    return model.getShortDescription(node);
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException(node);
        }

        public void performDefaultAction(Object node) throws UnknownTypeException {
            for (CompoundModel model : compoundModels) {
                try {
                    model.performDefaultAction(node);
                    return;
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException(node);
        }

        public Action[] getActions(Object node) throws UnknownTypeException {
            boolean nodeRecognized = false;
            List<Action> actionsList = new ArrayList();
            for (CompoundModel model : compoundModels) {
                try {
                    Action[] actions = model.getActions(node);
                    for (int x = 0; x < actions.length; x++) {
                        actionsList.add(actions[x]);
                    }
                    nodeRecognized = true;
                } catch (UnknownTypeException e) {
                }
            }
            if (!nodeRecognized) {
                throw new UnknownTypeException(node);
            }
            Action[] result = new Action[actionsList.size()];
            actionsList.toArray(result);
            return result;
        }

        public Object getValueAt(Object node, String columnID) throws UnknownTypeException {
            for (CompoundModel model : compoundModels) {
                try {
                    return model.getValueAt(node, columnID);
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException(node);
        }

        public boolean isReadOnly(Object node, String columnID) throws UnknownTypeException {
            for (CompoundModel model : compoundModels) {
                try {
                    return model.isReadOnly(node, columnID);
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException(node);
        }

        public void setValueAt(Object node, String columnID, Object value) throws UnknownTypeException {
            for (CompoundModel model : compoundModels) {
                try {
                    model.setValueAt(node, columnID, value);
                    return;
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException(node);
        }

        public boolean isExpanded(Object node) throws UnknownTypeException {
            for (CompoundModel model : compoundModels) {
                try {
                    return model.isExpanded(node);
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException(node);
        }

        public void nodeExpanded(Object node) {
            for (CompoundModel model : compoundModels) {
                model.nodeExpanded(node);
            }
        }

        public void nodeCollapsed(Object node) {
            for (CompoundModel model : compoundModels) {
                model.nodeCollapsed(node);
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
