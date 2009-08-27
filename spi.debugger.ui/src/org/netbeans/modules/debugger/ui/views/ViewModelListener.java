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
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
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
import org.netbeans.spi.debugger.ui.Constants;
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
            Map<String, CompoundModel> modelsMap = new LinkedHashMap<String, CompoundModel>();
            Set treeModelFiltersSet = new HashSet();
            Set nodeModelFiltersSet = new HashSet();
            Set tableModelFiltersSet = new HashSet();
            Set nodeActionsProviderFiltersSet = new HashSet();
            CompoundModel localsCompound = createCompound(View.LOCALS_VIEW_NAME,
                    treeModelFiltersSet, nodeModelFiltersSet, tableModelFiltersSet,
                    nodeActionsProviderFiltersSet);
            modelsMap.put(View.LOCALS_VIEW_NAME, localsCompound);
            if (VariablesViewButtons.isResultsViewNested()) {
                modelsMap.put(View.RESULTS_VIEW_NAME, createCompound(View.RESULTS_VIEW_NAME,
                        treeModelFiltersSet, nodeModelFiltersSet, tableModelFiltersSet,
                        nodeActionsProviderFiltersSet));
            }
            if (VariablesViewButtons.isWatchesViewNested()) {
                modelsMap.put(View.WATCHES_VIEW_NAME, createCompound(View.WATCHES_VIEW_NAME,
                        treeModelFiltersSet, nodeModelFiltersSet, tableModelFiltersSet,
                        nodeActionsProviderFiltersSet));
            }
            UnionTreeModel unionModel = new UnionTreeModel(modelsMap, localsCompound);
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

    // innerclasses .............................................................

    private static class UnionTreeModel implements TreeModel, ExtendedNodeModel,
            NodeActionsProvider, TableModel, TreeExpansionModel {

        private Collection<CompoundModel> compoundModels;
        private Map<String, CompoundModel> compoundModelsByViewName;
        private List<CompoundModel> orderedModels; // order for getChildren()
        private final Collection<ModelListener> modelListeners = new HashSet<ModelListener>();
        private final Map<Object, CompoundModel> objectModels = Collections.synchronizedMap(new WeakHashMap<Object, CompoundModel>());
        private final Map<CompoundModel, ModelListener> compoundModelListeners = new HashMap<CompoundModel, ModelListener>();
        // We do cache all model properties because of refreshes.
        // Refresh of the tree requested by one model should not refresh data in other models.
        private final Map<CompoundModel, CachedProperties> cachedProperties = new HashMap<CompoundModel, CachedProperties>();

        UnionTreeModel(Map<String, CompoundModel> treeModels, CompoundModel localsCompound) {
            this.compoundModelsByViewName = treeModels;
            this.compoundModels = treeModels.values();
            orderedModels = new ArrayList<CompoundModel>(this.compoundModels);
            orderedModels.remove(localsCompound);
            orderedModels.add(localsCompound);
            for (CompoundModel cm : compoundModels) {
                cachedProperties.put(cm, new CachedProperties());
            }
        }

        public Object getRoot() {
            return ROOT;
        }

        private String findViewName(CompoundModel model) {
            for (String viewName : compoundModelsByViewName.keySet()) {
                if (compoundModelsByViewName.get(viewName) == model) return viewName;
            }
            return null;
        }

        NodeProperties getPropertiesFor(CompoundModel model, Object node) {
            CachedProperties cp = cachedProperties.get(model);
            return cp.getPropertiesFor(node);
        }

        public Object[] getChildren(Object parent, int from, int to) throws UnknownTypeException {
            if (parent == TreeModel.ROOT) {
                List chList = new ArrayList();
                for (CompoundModel model : orderedModels) {
                    NodeProperties np = getPropertiesFor(model, parent);
                    Object[] children = np.getChildren();
                    if (children == null) {
                        try {
                            children = model.getChildren(parent, 0, model.getChildrenCount(parent));
                        } catch (UnknownTypeException e) {
                        }
                        if (children != null) {
                            np.setChildren(children);
                        }
                    }
                    if (children != null) {
                        for (int x = 0; x < children.length; x++) {
                            // exclude HistoryNode
                            if (!"HistoryNode".equals(children[x].getClass().getSimpleName())) { // NOI18N [TODO]
                                chList.add(children[x]);
                                objectModels.put(children[x], model);
                            }
                        }
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
                {
                    CompoundModel model = objectModels.get(parent);
                    if (model != null) {
                        NodeProperties np = getPropertiesFor(model, parent);
                        Object[] children = np.getChildren();
                        if (children == null) {
                            children = model.getChildren(parent, from, to);
                            np.setChildren(children);
                        }
                        for (int x = 0; x < children.length; x++) {
                            objectModels.put(children[x], model);
                        }
                        return children;
                    }
                }
                for (CompoundModel model : compoundModels) {
                    try {
                        NodeProperties np = getPropertiesFor(model, parent);
                        Object[] children = np.getChildren();
                        if (children == null) {
                            children = model.getChildren(parent, from, to);
                            np.setChildren(children);
                        }
                        for (int x = 0; x < children.length; x++) {
                            objectModels.put(children[x], model);
                        }
                        return children;
                    } catch (UnknownTypeException e) {
                    }
                }
            }
            throw new UnknownTypeException(parent);
        }

        private boolean isLeaf(CompoundModel model, Object node) throws UnknownTypeException {
            NodeProperties np = getPropertiesFor(model, node);
            Boolean isLeaf;
            synchronized (np.LEAF_LOCK) {
                isLeaf = np.isLeaf;
                if (isLeaf == null) {
                    isLeaf = model.isLeaf(node);
                    np.isLeaf = isLeaf;
                }
                return isLeaf;
            }
        }

        public boolean isLeaf(Object node) throws UnknownTypeException {
            {
                CompoundModel model = objectModels.get(node);
                if (model != null) {
                    return isLeaf(model, node);
                }
            }
            boolean isLeaf = false;
            boolean recognized = false;
            for (CompoundModel model : compoundModels) {
                try {
                    isLeaf |= isLeaf(model, node);
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

        private int getChildrenCount(CompoundModel model, Object node) throws UnknownTypeException {
            NodeProperties np = getPropertiesFor(model, node);
            Integer childrenCount;
            synchronized (np.CHILDREN_LOCK) {
                childrenCount = np.childrenCount;
                if (childrenCount == null) {
                    childrenCount = model.getChildrenCount(node);
                    np.childrenCount = childrenCount;
                }
                return childrenCount;
            }
        }

        public int getChildrenCount(Object node) throws UnknownTypeException {
            if (node == TreeModel.ROOT) {
                int count = 0;
                for (CompoundModel model : compoundModels) {
                    int c = getChildrenCount(model, node);
                    if (c == Integer.MAX_VALUE) {
                        return Integer.MAX_VALUE;
                    }
                    count += c;
                }
                return count;
            } else {
                {
                    CompoundModel model = objectModels.get(node);
                    if (model != null) {
                        return getChildrenCount(model, node);
                    }
                }
                for (CompoundModel model : compoundModels) {
                    try {
                        return getChildrenCount(model, node);
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
                        CompoundModelListener cml = new CompoundModelListener(model);
                        compoundModelListeners.put(model, cml);
                        model.addModelListener(cml);
                    }
                }
                modelListeners.add(listener);
            }
        }

        public void removeModelListener (ModelListener listener) {
            synchronized (modelListeners) {
                if (modelListeners.size() == 0) return ; // Everything was already removed.
                modelListeners.remove(listener);
                if (modelListeners.size() == 0) {
                    for (CompoundModel model : compoundModels) {
                        model.removeModelListener(compoundModelListeners.remove(model));
                    }
                }
            }
        }

        public void modelChanged(CompoundModel cm, ModelEvent event) {
            //System.err.println("UnionTreeModel.modelChanged("+event+") from "+event.getSource());
            Collection<ModelListener> listeners;
            synchronized (modelListeners) {
                listeners = new ArrayList<ModelListener>(modelListeners);
            }
            if (event == null) {
                cachedProperties.get(cm).clear();
            } else if (event instanceof ModelEvent.TreeChanged) {
                cachedProperties.get(cm).clear();
                /*List<Reference<Object>> cachedChildren;
                synchronized (rootChildren) {
                    cachedChildren = rootChildren.remove(cm);
                }
                System.err.println("REMOVED cached children = "+cachedChildren);
                //Refresh the root children + all nodes of this model recursively
                if (cachedChildren != null) {
                    for (Reference<Object> child : cachedChildren) {
                        Object node = child.get();
                        if (node != null) {
                            //nodesToRefresh.add(node);
                            System.err.println("  refreshing node "+node);
                            ModelEvent newEvent = new ModelEvent.NodeChanged(this, node, 0xFFFFFFFF); // Everything changed with the node.
                            for (Iterator<ModelListener> it = listeners.iterator(); it.hasNext(); ) {
                                it.next().modelChanged(newEvent);
                            }
                        }
                    }
                }
                ModelEvent newEvent = new ModelEvent.NodeChanged(this, getRoot(), ModelEvent.NodeChanged.CHILDREN_MASK);
                for (Iterator<ModelListener> it = listeners.iterator(); it.hasNext(); ) {
                    it.next().modelChanged(newEvent);
                }
                return ;*/
            } else if (event instanceof ModelEvent.NodeChanged) {
                ModelEvent.NodeChanged ne = (ModelEvent.NodeChanged) event;
                cachedProperties.get(cm).clearNode(ne.getNode(), ne.getChange());
            } else if (event instanceof ModelEvent.TableValueChanged) {
                ModelEvent.TableValueChanged te = (ModelEvent.TableValueChanged) event;
                cachedProperties.get(cm).clearTable(te.getNode(), te.getColumnID());
            }
            ModelEvent newEvent = translateEvent(event, this);
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

        private boolean canRename(CompoundModel model, Object node) throws UnknownTypeException {
            NodeProperties np = getPropertiesFor(model, node);
            Boolean canRename;
            synchronized (np.RENAME_LOCK) {
                canRename = np.canRename;
                if (canRename == null) {
                    canRename = model.canRename(node);
                    np.canRename = canRename;
                }
                return canRename;
            }
        }

        public boolean canRename(Object node) throws UnknownTypeException {
            {
                CompoundModel model = objectModels.get(node);
                if (model != null) {
                    return canRename(model, node);
                }
            }
            boolean canRename = false;
            for (CompoundModel model : compoundModels) {
                try {
                    canRename |= canRename(model, node);
                } catch (UnknownTypeException e) {
                }
            }
            return canRename;
        }

        private boolean canCopy(CompoundModel model, Object node) throws UnknownTypeException {
            NodeProperties np = getPropertiesFor(model, node);
            Boolean canCopy;
            synchronized (np.COPY_CUT_LOCK) {
                canCopy = np.canCopy;
                if (canCopy == null) {
                    canCopy = model.canCopy(node);
                    np.canCopy = canCopy;
                }
                return canCopy;
            }
        }

        public boolean canCopy(Object node) throws UnknownTypeException {
            {
                CompoundModel model = objectModels.get(node);
                if (model != null) {
                    return canCopy(model, node);
                }
            }
            boolean canCopy = false;
            for (CompoundModel model : compoundModels) {
                try {
                    canCopy |= canCopy(model, node);
                } catch (UnknownTypeException e) {
                }
            }
            return canCopy;
        }

        private boolean canCut(CompoundModel model, Object node) throws UnknownTypeException {
            NodeProperties np = getPropertiesFor(model, node);
            Boolean canCut;
            synchronized (np.COPY_CUT_LOCK) {
                canCut = np.canCut;
                if (canCut == null) {
                    canCut = model.canCut(node);
                    np.canCut = canCut;
                }
                return canCut;
            }
        }

        public boolean canCut(Object node) throws UnknownTypeException {
            {
                CompoundModel model = objectModels.get(node);
                if (model != null) {
                    return canCut(model, node);
                }
            }
            boolean canCut = false;
            for (CompoundModel model : compoundModels) {
                try {
                    canCut |= canCut(model, node);
                } catch (UnknownTypeException e) {
                }
            }
            return canCut;
        }

        public Transferable clipboardCopy(Object node) throws IOException, UnknownTypeException {
            {
                CompoundModel model = objectModels.get(node);
                if (model != null) {
                    return model.clipboardCopy(node);
                }
            }
            for (CompoundModel model : compoundModels) {
                try {
                    return model.clipboardCopy(node);
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException(node);
        }

        public Transferable clipboardCut(Object node) throws IOException, UnknownTypeException {
            {
                CompoundModel model = objectModels.get(node);
                if (model != null) {
                    return model.clipboardCut(node);
                }
            }
            for (CompoundModel model : compoundModels) {
                try {
                    return model.clipboardCut(node);
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException(node);
        }

        public PasteType[] getPasteTypes(Object node, Transferable t) throws UnknownTypeException {
            {
                CompoundModel model = objectModels.get(node);
                if (model != null) {
                    return model.getPasteTypes(node, t);
                }
            }
            for (CompoundModel model : compoundModels) {
                try {
                    return model.getPasteTypes(node, t);
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException(node);
        }

        public void setName(Object node, String name) throws UnknownTypeException {
            {
                CompoundModel model = objectModels.get(node);
                if (model != null) {
                    model.setName(node, name);
                    return;
                }
            }
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

        private String getIconBaseWithExtension(CompoundModel model, Object node) throws UnknownTypeException {
            NodeProperties np = getPropertiesFor(model, node);
            String iconBaseWithExtension;
            synchronized (np.ICON_LOCK) {
                iconBaseWithExtension = np.iconBaseWithExtension;
                if (iconBaseWithExtension == null) {
                    iconBaseWithExtension = model.getIconBaseWithExtension(node);
                    np.iconBaseWithExtension = iconBaseWithExtension;
                }
                return iconBaseWithExtension;
            }
        }

        public String getIconBaseWithExtension(Object node) throws UnknownTypeException {
            {
                CompoundModel model = objectModels.get(node);
                if (model != null) {
                    return getIconBaseWithExtension(model, node);
                }
            }
            for (CompoundModel model : compoundModels) {
                try {
                    return getIconBaseWithExtension(model, node);
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException(node);
        }

        private String getDisplayName(CompoundModel model, Object node) throws UnknownTypeException {
            NodeProperties np = getPropertiesFor(model, node);
            String displayName;
            synchronized (np.DISPLAY_NAME_LOCK) {
                displayName = np.displayName;
                if (displayName == null) {
                    displayName = model.getDisplayName(node);
                    np.displayName = displayName;
                }
                return displayName;
            }
        }

        public String getDisplayName(Object node) throws UnknownTypeException {
            {
                CompoundModel model = objectModels.get(node);
                if (model != null) {
                    return getDisplayName(model, node);
                }
            }
            String result = null;
            for (CompoundModel model : compoundModels) {
                try {
                    result = getDisplayName(model, node);
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

        private String getIconBase(CompoundModel model, Object node) throws UnknownTypeException {
            NodeProperties np = getPropertiesFor(model, node);
            String iconBase;
            synchronized (np.ICON_LOCK) {
                iconBase = np.iconBase;
                if (iconBase == null) {
                    iconBase = model.getIconBase(node);
                    np.iconBase = iconBase;
                }
                return iconBase;
            }
        }

        public String getIconBase(Object node) throws UnknownTypeException {
            {
                CompoundModel model = objectModels.get(node);
                if (model != null) {
                    return getIconBase(model, node);
                }
            }
            for (CompoundModel model : compoundModels) {
                try {
                    return getIconBase(model, node);
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException(node);
        }

        private String getShortDescription(CompoundModel model, Object node) throws UnknownTypeException {
            NodeProperties np = getPropertiesFor(model, node);
            String shortDescription;
            synchronized (np.SHORT_DESCRIPTION_LOCK) {
                shortDescription = np.shortDescription;
                if (shortDescription == null) {
                    shortDescription = model.getShortDescription(node);
                    np.shortDescription = shortDescription;
                }
                return shortDescription;
            }
        }

        public String getShortDescription(Object node) throws UnknownTypeException {
            {
                CompoundModel model = objectModels.get(node);
                if (model != null) {
                    return getShortDescription(model, node);
                }
            }
            for (CompoundModel model : compoundModels) {
                try {
                    return getShortDescription(model, node);
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException(node);
        }

        public void performDefaultAction(Object node) throws UnknownTypeException {
            {
                CompoundModel model = objectModels.get(node);
                if (model != null) {
                    model.performDefaultAction(node);
                    return ;
                }
            }
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
            {
                CompoundModel model = objectModels.get(node);
                if (model != null) {
                    return model.getActions(node);
                }
            }
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

        private String adjustColumn(String viewName, String columnID) {
            if (View.WATCHES_VIEW_NAME.equals(viewName)) {
                if (Constants.LOCALS_TYPE_COLUMN_ID.equals(columnID)) {
                    columnID = Constants.WATCH_TYPE_COLUMN_ID;
                } else if (Constants.LOCALS_VALUE_COLUMN_ID.equals(columnID)) {
                    columnID = Constants.WATCH_VALUE_COLUMN_ID;
                } else if (Constants.LOCALS_TO_STRING_COLUMN_ID.equals(columnID)) {
                    columnID = Constants.WATCH_TO_STRING_COLUMN_ID;
                }
            }
            return columnID;
        }

        private Object getValueAt(CompoundModel model, Object node, String columnID) throws UnknownTypeException {
            NodeProperties np = getPropertiesFor(model, node);
            synchronized (np.VALUE_LOCK) {
                Object value = np.valueAt.get(columnID);
                if (value == null) {
                    value = model.getValueAt(node, columnID);
                    np.valueAt.put(columnID, value);
                }
                return value;
            }
        }

        public Object getValueAt(Object node, String columnID) throws UnknownTypeException {
            {
                CompoundModel model = objectModels.get(node);
                if (model != null) {
                    String viewName = findViewName(model);
                    String c = adjustColumn(viewName, columnID);
                    return getValueAt(model, node, c);
                }
            }
            for (String viewName : compoundModelsByViewName.keySet()) {
                CompoundModel model = compoundModelsByViewName.get(viewName);
                String c = adjustColumn(viewName, columnID);
                try {
                    return getValueAt(model, node, c);
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException(node);
        }

        private boolean isReadOnly(CompoundModel model, Object node, String columnID) throws UnknownTypeException {
            NodeProperties np = getPropertiesFor(model, node);
            synchronized (np.READ_LOCK) {
                Boolean value = np.isReadOnly.get(columnID);
                if (value == null) {
                    value = model.isReadOnly(node, columnID);
                    np.isReadOnly.put(columnID, value);
                }
                return value;
            }
        }

        public boolean isReadOnly(Object node, String columnID) throws UnknownTypeException {
            {
                CompoundModel model = objectModels.get(node);
                if (model != null) {
                    String viewName = findViewName(model);
                    String c = adjustColumn(viewName, columnID);
                    return isReadOnly(model, node, c);
                }
            }
            for (String viewName : compoundModelsByViewName.keySet()) {
                CompoundModel model = compoundModelsByViewName.get(viewName);
                String c = adjustColumn(viewName, columnID);
                try {
                    return isReadOnly(model, node, c);
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException(node);
        }

        public void setValueAt(Object node, String columnID, Object value) throws UnknownTypeException {
            {
                CompoundModel model = objectModels.get(node);
                if (model != null) {
                    String viewName = findViewName(model);
                    String c = adjustColumn(viewName, columnID);
                    model.setValueAt(node, c, value);
                    NodeProperties np = getPropertiesFor(model, node);
                    synchronized (np.VALUE_LOCK) {
                        np.valueAt.put(columnID, value);
                    }
                    return;
                }
            }
            for (String viewName : compoundModelsByViewName.keySet()) {
                CompoundModel model = compoundModelsByViewName.get(viewName);
                String c = adjustColumn(viewName, columnID);
                try {
                    model.setValueAt(node, c, value);
                    NodeProperties np = getPropertiesFor(model, node);
                    synchronized (np.VALUE_LOCK) {
                        np.valueAt.put(columnID, value);
                    }
                    return;
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException(node);
        }

        public boolean isExpanded(Object node) throws UnknownTypeException {
            {
                CompoundModel model = objectModels.get(node);
                if (model != null) {
                    return model.isExpanded(node);
                }
            }
            for (CompoundModel model : compoundModels) {
                try {
                    return model.isExpanded(node);
                } catch (UnknownTypeException e) {
                }
            }
            throw new UnknownTypeException(node);
        }

        public void nodeExpanded(Object node) {
            {
                CompoundModel model = objectModels.get(node);
                if (model != null) {
                    model.nodeExpanded(node);
                    return ;
                }
            }
            for (CompoundModel model : compoundModels) {
                model.nodeExpanded(node);
            }
        }

        public void nodeCollapsed(Object node) {
            {
                CompoundModel model = objectModels.get(node);
                if (model != null) {
                    model.nodeCollapsed(node);
                    return ;
                }
            }
            for (CompoundModel model : compoundModels) {
                model.nodeCollapsed(node);
            }
        }

        @Override
        public String toString () {
            return super.toString () + "\n" + compoundModelsByViewName.toString();
        }

        private class CompoundModelListener implements ModelListener {

            private CompoundModel cm;

            public CompoundModelListener(CompoundModel cm) {
                this.cm = cm;
            }

            public void modelChanged(ModelEvent event) {
                UnionTreeModel.this.modelChanged(cm, event);
            }

        }

        private static final class CachedProperties extends Object {
            private final Map<Object, NodeProperties> nodeProperties = new WeakHashMap<Object, NodeProperties>();
            /**
             * Cached node properties
             * @param node the node.
             * @return the node properties
             */
            public synchronized NodeProperties getPropertiesFor(Object node) {
                NodeProperties np = nodeProperties.get(node);
                if (np == null) {
                    np = new NodeProperties();
                    nodeProperties.put(node, np);
                }
                return np;
            }

            public synchronized void clear() {
                nodeProperties.clear();
            }

            private synchronized void clearRecursively(Object node) {
                NodeProperties np = nodeProperties.remove(node);
                if (np != null) {
                    Object[] children = np.getChildren();
                    if (children != null) {
                        for (int i = 0; i < children.length; i++) {
                            clearRecursively(children[i]);
                        }
                    }
                }
            }

            public void clearNode(Object node, int changeMask) {
                if (node != null) {
                    NodeProperties np;
                    synchronized (this) {
                        if (changeMask == 0xFFFFFFFF) {
                            clearRecursively(node);
                            return ;
                        }
                        np = nodeProperties.get(node);
                    }
                    if (np != null) {
                        if ((ModelEvent.NodeChanged.DISPLAY_NAME_MASK & changeMask) != 0) {
                            np.displayName = null;
                        }
                        if ((ModelEvent.NodeChanged.ICON_MASK & changeMask) != 0) {
                            np.iconBase = null;
                            np.iconBaseWithExtension = null;
                        }
                        if ((ModelEvent.NodeChanged.SHORT_DESCRIPTION_MASK & changeMask) != 0) {
                            np.shortDescription = null;
                        }
                        if ((ModelEvent.NodeChanged.CHILDREN_MASK & changeMask) != 0) {
                            np.childrenCount = null;
                            np.children = null;
                            np.isLeaf = null;
                        }
                    }
                } else { // Clear all nodes
                    synchronized (this) {
                        for (Object n : nodeProperties.keySet()) {
                            clearNode(n, changeMask);
                        }
                    }
                }
            }

            public void clearTable(Object node, String column) {
                if (node != null) {
                    NodeProperties np;
                    synchronized (this) {
                        if (column == null) {
                            clearRecursively(node);
                            return ;
                        }
                        np = nodeProperties.get(node);
                    }
                    if (np != null) {
                        np.clearValues();
                    }
                }
            }
        }

        /**
         * Unknown properties are null
         */
        private static final class NodeProperties extends Object {
            Integer childrenCount;
            private Reference[] children;
            Boolean isLeaf;
            Boolean canRename;
            Boolean canCopy;
            Boolean canCut;
            String displayName;
            String shortDescription;
            String iconBase;
            String iconBaseWithExtension;
            Map<String, Object> valueAt = new HashMap<String, Object>();
            Map<String, Boolean> isReadOnly = new HashMap<String, Boolean>();

            final Object CHILDREN_LOCK = new Object();
            final Object LEAF_LOCK = new Object();
            final Object RENAME_LOCK = new Object();
            final Object COPY_CUT_LOCK = new Object();
            final Object DISPLAY_NAME_LOCK = new Object();
            final Object SHORT_DESCRIPTION_LOCK = new Object();
            final Object ICON_LOCK = new Object();
            final Object VALUE_LOCK = new Object();
            final Object READ_LOCK = new Object();

            Object[] getChildren() {
                Reference[] chr = children;
                if (chr == null) {
                    return null;
                }
                Object[] ch = new Object[chr.length];
                for (int i = 0; i < ch.length; i++) {
                    ch[i] = chr[i].get();
                    if (ch[i] == null) {
                        children = null;
                        ch = null;
                        break;
                    }
                }
                return ch;
            }

            void setChildren(Object[] ch) {
                synchronized (CHILDREN_LOCK) {
                    children = new Reference[ch.length];
                    for (int i = 0; i < ch.length; i++) {
                        children[i] = new WeakReference(ch[i]);
                    }
                }
            }

            void clearValues() {
                valueAt = new HashMap<String, Object>();
                isReadOnly = new HashMap<String, Boolean>();
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
